package crazypants.enderzoo.entity.ai;

import crazypants.enderzoo.entity.EntityUtil;
import crazypants.enderzoo.vec.Point3i;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.BlockPos;

public class EntityAIFlyingLand extends EntityAIBase {

  private EntityCreature entity;
  protected double speed;
  private double targetX;
  private double targetY;
  private double targetZ;

  private int onGroundCount = 0;
  private int defSearchRange = 3;
  private int maxSearchRange = 16;
  private int searchRange = 4;

  public EntityAIFlyingLand(EntityCreature creature, double speedIn) {
    entity = creature;
    speed = speedIn;
    setMutexBits(1);
  }

  @Override
  public boolean shouldExecute() {
    
    if (entity.onGround || !entity.getNavigator().noPath()) {      
      return false;
    }    
    
    int ySearchRange = 4;
    Point3i target = null;

    BlockPos ep = entity.getPosition();     
    Point3i blockLocationResult = EntityUtil.getClearSurfaceLocation(entity, ep.getX(), ep.getZ(), 1, ep.getY());
    if (blockLocationResult != null) {
      int distFromGround = ep.getY() - blockLocationResult.y;
      if (distFromGround < 2) {        
        target = blockLocationResult;
      } else {
        ySearchRange += ep.getY() - blockLocationResult.y;
      }
    }
    if (target == null) {
      target = findLandingTarget(searchRange, ySearchRange);
    }
    if (target == null) {
      searchRange = Math.min(searchRange + 1, maxSearchRange);
      return false;
    }
    
    if(target.equals(entity.getPosition())) {
      return false;
    }
    
    searchRange = defSearchRange;    
    targetX = target.x + 0.5;
    targetY = target.y;
    targetZ = target.z + 0.5;
    return true;
  }

  private Point3i findLandingTarget(int horizSearchRange, int ySearchRange) {    
    BlockPos ep = entity.getPosition();    
    for (int x = -horizSearchRange; x <= horizSearchRange; x++) {
      for (int z = -horizSearchRange; z <= horizSearchRange; z++) {
        Point3i res = EntityUtil.getClearSurfaceLocation(entity, ep.getX() + x, ep.getZ() + z, 1, ep.getY());
        if(res != null) {
          return res;
        }        
      }
    }

    return null;
  }

  @Override
  public void startExecuting() {
    onGroundCount = 0;
    if(!entity.getNavigator().tryMoveToXYZ(targetX, targetY, targetZ, speed)) {
      //System.out.println("EntityAIFlyingLand.startExecuting: No path to target");
    }
  }

  @Override
  public boolean continueExecuting() {
    
    if (entity.onGround) {      
      onGroundCount++;
      if(onGroundCount >= 40) {
        //If we have been on the ground for a couple of seconds
        //time to chill no matter what
        entity.getNavigator().clearPathEntity();
        return false;
      }
      
      //Stop if we are on the ground in the middle of a block
      double fx = entity.posX - Math.floor(entity.posX);
      double fz = entity.posX - Math.floor(entity.posX);
      if (fx > 0.4 && fx < 0.6 && fz > 0.4 && fz < 0.6) {
        // System.out.println("EntityAIFlyingLand.continueExecuting: Stop");
        BlockPos bellow = entity.getPosition().down();
        IBlockState bs = entity.worldObj.getBlockState(bellow);
        if (!bs.getBlock().isAir(entity.worldObj, bellow)) {
          entity.getNavigator().clearPathEntity();
          return false;
        }
      }
    }

    boolean isStillNavigating = !entity.getNavigator().noPath();
    if (!isStillNavigating) {
      entity.onGround = EntityUtil.isOnGround(entity);
      entity.isAirBorne = !entity.onGround;
    }    
    return isStillNavigating;
  }

}