package umpaz.brewinandchewin.common.fluid;

import net.minecraft.resources.ResourceLocation;
import umpaz.brewinandchewin.BrewinAndChewin;

public class BnCFluidConstants {
   public static class Colors {
      public static final int DEFAULT = -1;
      public static final int BEER = -282345;
      public static final int VODKA = -1573386;
      public static final int MEAD = -11475;
      public static final int EGG_GROG = -1;
      public static final int STRONGROOT_ALE = -4437425;
      public static final int RICE_WINE = -1;
      public static final int GLITTERING_GRENADINE = -678562;
      public static final int STEEL_TOE_STOUT = -6845556;
      public static final int DREAD_NOG = -14296393;
      public static final int KOMBUCHA = -7171528;
      public static final int SACCHARINE_RUM = -3323270;
      public static final int PALE_JANE = -2572629;
      public static final int SALTY_FOLLY = -13080787;
      public static final int BLOODY_MARY = -8120819;
      public static final int RED_RUM = -11397104;
      public static final int WITHERING_DROSS = -15133679;
   }

   public static class Textures {
      public static final ResourceLocation FLUID_STILL_TEXTURE = new ResourceLocation("block/water_still");
      public static final ResourceLocation FLUID_FLOWING_TEXTURE = new ResourceLocation("block/water_flow");
      public static final ResourceLocation FLAXEN_STILL_TEXTURE = BrewinAndChewin.asResource("block/flaxen_cheese_still");
      public static final ResourceLocation FLAXEN_FLOWING_TEXTURE = BrewinAndChewin.asResource("block/flaxen_cheese_flow");
      public static final ResourceLocation SCARLET_STILL_TEXTURE = BrewinAndChewin.asResource("block/scarlet_cheese_still");
      public static final ResourceLocation SCARLET_FLOWING_TEXTURE = BrewinAndChewin.asResource("block/scarlet_cheese_flow");
      public static final ResourceLocation HONEY_FLUID_STILL_TEXTURE = new ResourceLocation("block/honey_block_top");
      public static final ResourceLocation HONEY_FLUID_FLOWING_TEXTURE = new ResourceLocation("block/honey_block_top");
   }
}
