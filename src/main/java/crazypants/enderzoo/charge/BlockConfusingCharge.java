package crazypants.enderzoo.charge;

import java.util.List;
import java.util.Random;

import crazypants.enderzoo.EnderZoo;
import crazypants.enderzoo.EnderZooTab;
import crazypants.enderzoo.PacketHandler;
import crazypants.enderzoo.config.Config;
import crazypants.enderzoo.entity.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntitySpellParticleFX;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockConfusingCharge extends BlockTNT implements ICharge {

  public static final String NAME = "blockConfusingCharge";

  private int chargeId;

  public static BlockConfusingCharge create() {

    PacketHandler.INSTANCE.registerMessage(PacketExplodeEffect.class, PacketExplodeEffect.class, PacketHandler.nextID(), Side.CLIENT);

    EntityRegistry.registerModEntity(EntityPrimedCharge.class, "EntityPrimedCharge", Config.entityPrimedChargeId, EnderZoo.instance, 64, 100, false);
    if (!Config.confusingChargeEnabled) {
      return null;
    }

    BlockConfusingCharge res = new BlockConfusingCharge();
    res.init();
    return res;
  }

  private String name;

  protected BlockConfusingCharge() {
    this(NAME);
  }

  protected BlockConfusingCharge(String name) {
    setCreativeTab(EnderZooTab.tabEnderZoo);
    // setBlockName(name);
    setUnlocalizedName(name);
    this.name = name;
  }

  protected void init() {
    GameRegistry.registerBlock(this, name);
    ChargeRegister.instance.registerCharge(this);
  }

  @Override
  public int getID() {
    return chargeId;
  }

  @Override
  public void setID(int id) {
    chargeId = id;
  }

  @Override
  public Block getBlock() {
    return this;
  }

  @Override
  public void explode(EntityPrimedCharge entity) {
    World world = entity.worldObj;

    world.playSoundEffect(entity.posX, entity.posY, entity.posZ, "random.explode", 3F, 1.4f + ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F));

    PacketHandler.sendToAllAround(new PacketExplodeEffect(entity, this), entity);
  }

  public void explode(World world, BlockPos pos, IBlockState state, EntityLivingBase igniter) {
    if (!world.isRemote) {
      if (((Boolean) state.getValue(EXPLODE)).booleanValue()) {
        // EntityTNTPrimed entitytntprimed = new EntityTNTPrimed(worldIn,
        // (double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() +
        // 0.5F), (double)((float)pos.getZ() + 0.5F), igniter);
        // worldIn.spawnEntityInWorld(entitytntprimed);
        // worldIn.playSoundAtEntity(entitytntprimed, "game.tnt.primed", 1.0F,
        // 1.0F);

        EntityPrimedCharge entity = new EntityPrimedCharge(this, world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, igniter);
        world.spawnEntityInWorld(entity);
        world.playSoundAtEntity(entity, "game.tnt.primed", 1.0F, 1.0F);
        world.updateEntity(entity);
      }
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void explodeEffect(World world, double x, double y, double z) {

    List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, EntityUtil.getBoundsAround(x, y, z, Config.confusingChargeRange));
    if (players != null) {

      double maxDistanceSq = Config.confusingChargeRange * Config.confusingChargeRange;
      for (EntityPlayer player : players) {
        double playerDistSq = player.getDistanceSq(x, y, z);
        if (playerDistSq < maxDistanceSq) {
          double scale = 1 - playerDistSq / maxDistanceSq;
          scale = Math.exp(scale) / Math.E;
          int duration = (int) Math.ceil(Config.confusingChargeEffectDuration * scale);
          EnderZoo.proxy.setInstantConfusionOnPlayer(player, duration);
        }
      }
    }

    // world.spawnParticle("hugeexplosion", x, y, z, 1.0D, 0.0D, 0.0D);
    world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, x, y, z, 1.0D, 0.0D, 0.0D);

    int col = Items.potionitem.getColorFromDamage(8231);
    float r = (col >> 16 & 255) / 255.0F;
    float g = (col >> 8 & 255) / 255.0F;
    float b = (col >> 0 & 255) / 255.0F;
    Random random = world.rand;
    for (int i = 0; i < 100; ++i) {
      // double seed = random.nextDouble() * 20.0D;

      double d = random.nextDouble() * 2D;
      double mag = 25;
      double motionX = (0.5 - random.nextDouble()) * mag * d;
      double motionY = (0.5 - random.nextDouble()) * mag;
      double motionZ = (0.5 - random.nextDouble()) * mag * d;

      EntitySpellParticleFX entityfx = (EntitySpellParticleFX) new EntitySpellParticleFX.InstantFactory().getEntityFX(i, world, x + motionX * 0.1,
          y + motionY * 0.1, z + motionZ * 0.1, motionX, motionY, motionZ, (int[]) null);
      float colRan = 0.75F + random.nextFloat() * 0.25F;
      entityfx.setRBGColorF(r * colRan, g * colRan, b * colRan);
      // entityfx.multiplyVelocity((float) (random.nextDouble() * 4.0D));
      // entityfx.multiplyVelocity(0.1f);
      Minecraft.getMinecraft().effectRenderer.addEffect(entityfx);

    }

  }

  // @Override
  // public IIcon getIcon(int side, int meta) {
  // return side == 0 ? bottomIcon : (side == 1 ? topIcon : blockIcon);
  // }
  //
  // @Override
  // public void registerBlockIcons(IIconRegister p_149651_1_) {
  // blockIcon = p_149651_1_.registerIcon("enderzoo:" + name + "_side");
  // topIcon = p_149651_1_.registerIcon("enderzoo:" + name + "_top");
  // bottomIcon = p_149651_1_.registerIcon("enderzoo:" + name + "_bottom");
  // }

  // @Override
  // public void func_150114_a(World world, int x, int y, int z, int meta,
  // EntityLivingBase placedBy) {
  // if(!world.isRemote) {
  // if((meta & 1) == 1) {
  // EntityPrimedCharge entity = new EntityPrimedCharge(this, world, x + 0.5F, y
  // + 0.5F, z + 0.5F, placedBy);
  // world.spawnEntityInWorld(entity);
  // world.playSoundAtEntity(entity, "game.tnt.primed", 1.0F, 1.0F);
  // world.updateEntity(entity);
  // }
  // }
  // }

  @Override
  public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
    if (!world.isRemote) {
      EntityLivingBase placedBy = explosion.getExplosivePlacedBy();
      onIgnitedByNeighbour(world, pos.getX(), pos.getY(), pos.getZ(), placedBy);
    }
  }

  protected void onIgnitedByNeighbour(World world, int x, int y, int z, EntityLivingBase placedBy) {
    EntityPrimedCharge entity = new EntityPrimedCharge(this, world, x + 0.5F, y + 0.5F, z + 0.5F, placedBy);
    entity.setFuse(world.rand.nextInt(entity.getFuse() / 4) + entity.getFuse() / 8);
    world.spawnEntityInWorld(entity);
  }

}
