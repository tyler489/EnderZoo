package crazypants.enderzoo.entity.render;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelHorse;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderHorse;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.LayeredTexture;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderFallenMount extends RenderHorse {

  public static final Factory FACTORY = new Factory();
  
  private static final String[] horseArmorTextures = new String[] {
      null,
      "textures/entity/horse/armor/horse_armor_iron.png",
      "textures/entity/horse/armor/horse_armor_gold.png",
      "textures/entity/horse/armor/horse_armor_diamond.png"
  };
  private static final String textureName = "textures/entity/horse/horse_zombie.png";
  private static final ResourceLocation zombieHorseTexture = new ResourceLocation(textureName);
  private static final Map<String, ResourceLocation> textureCache = Maps.newHashMap();

  public RenderFallenMount(RenderManager rm) {
    super(rm, new ModelHorse(), 0.75F);
  }

  @Override
  protected ResourceLocation getEntityTexture(EntityHorse horse) {
    if(horse.getTotalArmorValue() == 0) {
      return zombieHorseTexture;
    } else {
      return getArmoredTexture(horse);
    }
  }

  private ResourceLocation getArmoredTexture(EntityHorse horse) {
    String s = horseArmorTextures[horse.getHorseArmorIndexSynced()];
    ResourceLocation resourcelocation = textureCache.get(s);
    if(resourcelocation == null) {
      resourcelocation = new ResourceLocation("Layered:" + s);
      Minecraft.getMinecraft().getTextureManager().loadTexture(resourcelocation, new LayeredTexture(textureName, s));
      textureCache.put(s, resourcelocation);
    }
    return resourcelocation;
  }
  
  public static class Factory implements IRenderFactory<EntityHorse> {

    @Override
    public Render<? super EntityHorse> createRenderFor(RenderManager manager) {
      return new RenderFallenMount(manager);
    }
  }

}
