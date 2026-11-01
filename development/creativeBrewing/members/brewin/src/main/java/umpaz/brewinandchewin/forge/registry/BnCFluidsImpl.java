package umpaz.brewinandchewin.forge.registry;

import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;
import umpaz.brewinandchewin.common.registry.BnCFluids;

public class BnCFluidsImpl {
   public static final Properties HONEY_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.HONEY, () -> BnCFluids.HONEY, () -> BnCFluids.FLOWING_HONEY);
   public static final Properties BEER_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.BEER, () -> BnCFluids.BEER, () -> BnCFluids.FLOWING_BEER);
   public static final Properties VODKA_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.VODKA, () -> BnCFluids.VODKA, () -> BnCFluids.FLOWING_VODKA);
   public static final Properties MEAD_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.MEAD, () -> BnCFluids.MEAD, () -> BnCFluids.FLOWING_MEAD);
   public static final Properties RICE_WINE_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.RICE_WINE, () -> BnCFluids.RICE_WINE, () -> BnCFluids.FLOWING_RICE_WINE);
   public static final Properties PALE_JANE_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.PALE_JANE, () -> BnCFluids.PALE_JANE, () -> BnCFluids.FLOWING_PALE_JANE);
   public static final Properties EGG_GROG_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.EGG_GROG, () -> BnCFluids.EGG_GROG, () -> BnCFluids.FLOWING_EGG_GROG);
   public static final Properties GLITTERING_GRENADINE_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.GLITTERING_GRENADINE, () -> BnCFluids.GLITTERING_GRENADINE, () -> BnCFluids.FLOWING_GLITTERING_GRENADINE);
   public static final Properties SACCHARINE_RUM_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.SACCHARINE_RUM, () -> BnCFluids.SACCHARINE_RUM, () -> BnCFluids.FLOWING_SACCHARINE_RUM);
   public static final Properties SALTY_FOLLY_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.SALTY_FOLLY, () -> BnCFluids.SALTY_FOLLY, () -> BnCFluids.FLOWING_SALTY_FOLLY);
   public static final Properties BLOODY_MARY_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.BLOODY_MARY, () -> BnCFluids.BLOODY_MARY, () -> BnCFluids.FLOWING_BLOODY_MARY);
   public static final Properties RED_RUM_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.RED_RUM, () -> BnCFluids.RED_RUM, () -> BnCFluids.FLOWING_RED_RUM);
   public static final Properties STRONGROOT_ALE_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.STRONGROOT_ALE, () -> BnCFluids.STRONGROOT_ALE, () -> BnCFluids.FLOWING_STRONGROOT_ALE);
   public static final Properties STEEL_TOE_STOUT_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.STEEL_TOE_STOUT, () -> BnCFluids.STEEL_TOE_STOUT, () -> BnCFluids.FLOWING_STEEL_TOE_STOUT);
   public static final Properties DREAD_NOG_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.DREAD_NOG, () -> BnCFluids.DREAD_NOG, () -> BnCFluids.FLOWING_DREAD_NOG);
   public static final Properties WITHERING_DROSS_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.WITHERING_DROSS, () -> BnCFluids.WITHERING_DROSS, () -> BnCFluids.FLOWING_WITHERING_DROSS);
   public static final Properties KOMBUCHA_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.KOMBUCHA, () -> BnCFluids.KOMBUCHA, () -> BnCFluids.FLOWING_KOMBUCHA);
   public static final Properties FLAXEN_CHEESE_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.FLAXEN_CHEESE, () -> BnCFluids.FLAXEN_CHEESE, () -> BnCFluids.FLOWING_FLAXEN_CHEESE);
   public static final Properties SCARLET_CHEESE_FLUID_PROPERTIES = new Properties(() -> BnCFluidTypes.SCARLET_CHEESE, () -> BnCFluids.SCARLET_CHEESE, () -> BnCFluids.FLOWING_SCARLET_CHEESE);

   public static void init() {
      // Fluids take an intrusive registry holder when built, so honey is only built when it will be registered.
      if (BnCFluids.hasOwnHoney()) {
         BnCFluids.HONEY = new Source(HONEY_FLUID_PROPERTIES);
         BnCFluids.FLOWING_HONEY = new Flowing(HONEY_FLUID_PROPERTIES);
      }
      BnCFluids.BEER = new Source(BEER_FLUID_PROPERTIES);
      BnCFluids.FLOWING_BEER = new Flowing(BEER_FLUID_PROPERTIES);
      BnCFluids.VODKA = new Source(VODKA_FLUID_PROPERTIES);
      BnCFluids.FLOWING_VODKA = new Flowing(VODKA_FLUID_PROPERTIES);
      BnCFluids.MEAD = new Source(MEAD_FLUID_PROPERTIES);
      BnCFluids.FLOWING_MEAD = new Flowing(MEAD_FLUID_PROPERTIES);
      BnCFluids.RICE_WINE = new Source(RICE_WINE_FLUID_PROPERTIES);
      BnCFluids.FLOWING_RICE_WINE = new Flowing(RICE_WINE_FLUID_PROPERTIES);
      BnCFluids.PALE_JANE = new Source(PALE_JANE_FLUID_PROPERTIES);
      BnCFluids.FLOWING_PALE_JANE = new Flowing(PALE_JANE_FLUID_PROPERTIES);
      BnCFluids.EGG_GROG = new Source(EGG_GROG_FLUID_PROPERTIES);
      BnCFluids.FLOWING_EGG_GROG = new Flowing(EGG_GROG_FLUID_PROPERTIES);
      BnCFluids.GLITTERING_GRENADINE = new Source(GLITTERING_GRENADINE_FLUID_PROPERTIES);
      BnCFluids.FLOWING_GLITTERING_GRENADINE = new Flowing(GLITTERING_GRENADINE_FLUID_PROPERTIES);
      BnCFluids.SACCHARINE_RUM = new Source(SACCHARINE_RUM_FLUID_PROPERTIES);
      BnCFluids.FLOWING_SACCHARINE_RUM = new Flowing(SACCHARINE_RUM_FLUID_PROPERTIES);
      BnCFluids.SALTY_FOLLY = new Source(SALTY_FOLLY_FLUID_PROPERTIES);
      BnCFluids.FLOWING_SALTY_FOLLY = new Flowing(SALTY_FOLLY_FLUID_PROPERTIES);
      BnCFluids.BLOODY_MARY = new Source(BLOODY_MARY_FLUID_PROPERTIES);
      BnCFluids.FLOWING_BLOODY_MARY = new Flowing(BLOODY_MARY_FLUID_PROPERTIES);
      BnCFluids.RED_RUM = new Source(RED_RUM_FLUID_PROPERTIES);
      BnCFluids.FLOWING_RED_RUM = new Flowing(RED_RUM_FLUID_PROPERTIES);
      BnCFluids.STRONGROOT_ALE = new Source(STRONGROOT_ALE_FLUID_PROPERTIES);
      BnCFluids.FLOWING_STRONGROOT_ALE = new Flowing(STRONGROOT_ALE_FLUID_PROPERTIES);
      BnCFluids.STEEL_TOE_STOUT = new Source(STEEL_TOE_STOUT_FLUID_PROPERTIES);
      BnCFluids.FLOWING_STEEL_TOE_STOUT = new Flowing(STEEL_TOE_STOUT_FLUID_PROPERTIES);
      BnCFluids.DREAD_NOG = new Source(DREAD_NOG_FLUID_PROPERTIES);
      BnCFluids.FLOWING_DREAD_NOG = new Flowing(DREAD_NOG_FLUID_PROPERTIES);
      BnCFluids.WITHERING_DROSS = new Source(WITHERING_DROSS_FLUID_PROPERTIES);
      BnCFluids.FLOWING_WITHERING_DROSS = new Flowing(WITHERING_DROSS_FLUID_PROPERTIES);
      BnCFluids.KOMBUCHA = new Source(KOMBUCHA_FLUID_PROPERTIES);
      BnCFluids.FLOWING_KOMBUCHA = new Flowing(KOMBUCHA_FLUID_PROPERTIES);
      BnCFluids.FLAXEN_CHEESE = new Source(FLAXEN_CHEESE_FLUID_PROPERTIES);
      BnCFluids.FLOWING_FLAXEN_CHEESE = new Flowing(FLAXEN_CHEESE_FLUID_PROPERTIES);
      BnCFluids.SCARLET_CHEESE = new Source(SCARLET_CHEESE_FLUID_PROPERTIES);
      BnCFluids.FLOWING_SCARLET_CHEESE = new Flowing(SCARLET_CHEESE_FLUID_PROPERTIES);
   }
}
