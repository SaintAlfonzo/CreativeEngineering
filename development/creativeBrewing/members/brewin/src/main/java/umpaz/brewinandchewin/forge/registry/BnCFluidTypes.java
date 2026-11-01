package umpaz.brewinandchewin.forge.registry;

import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegisterEvent.RegisterHelper;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.fluid.BnCFluidConstants.Colors;
import umpaz.brewinandchewin.common.fluid.BnCFluidConstants.Textures;
import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.forge.fluid.BnCFluidType;

// Port: the textures and tints upstream sets in RegisterClientExtensionsEvent live on each BnCFluidType (1.19.2's
// FluidType#initializeClient).
public class BnCFluidTypes {
   public static final FluidType HONEY = BnCFluidType.honey(Colors.DEFAULT);
   public static final FluidType BEER = BnCFluidType.alcohol(Colors.BEER);
   public static final FluidType VODKA = BnCFluidType.alcohol(Colors.VODKA);
   public static final FluidType MEAD = BnCFluidType.honey(Colors.MEAD);
   public static final FluidType RICE_WINE = BnCFluidType.alcohol(Colors.RICE_WINE);
   public static final FluidType PALE_JANE = BnCFluidType.alcohol(Colors.PALE_JANE);
   public static final FluidType EGG_GROG = BnCFluidType.alcohol(Colors.EGG_GROG);
   public static final FluidType GLITTERING_GRENADINE = BnCFluidType.alcohol(Colors.GLITTERING_GRENADINE);
   public static final FluidType SACCHARINE_RUM = BnCFluidType.alcohol(Colors.SACCHARINE_RUM);
   public static final FluidType SALTY_FOLLY = BnCFluidType.alcohol(Colors.SALTY_FOLLY);
   public static final FluidType BLOODY_MARY = BnCFluidType.alcohol(Colors.BLOODY_MARY);
   public static final FluidType RED_RUM = BnCFluidType.alcohol(Colors.RED_RUM);
   public static final FluidType STRONGROOT_ALE = BnCFluidType.alcohol(Colors.STRONGROOT_ALE);
   public static final FluidType STEEL_TOE_STOUT = BnCFluidType.alcohol(Colors.STEEL_TOE_STOUT);
   public static final FluidType DREAD_NOG = BnCFluidType.alcohol(Colors.DREAD_NOG);
   public static final FluidType WITHERING_DROSS = BnCFluidType.alcohol(Colors.WITHERING_DROSS);
   public static final FluidType KOMBUCHA = BnCFluidType.alcohol(Colors.KOMBUCHA);
   public static final FluidType FLAXEN_CHEESE = new BnCFluidType(Textures.FLAXEN_STILL_TEXTURE, Textures.FLAXEN_FLOWING_TEXTURE, Colors.DEFAULT);
   public static final FluidType SCARLET_CHEESE = new BnCFluidType(Textures.SCARLET_STILL_TEXTURE, Textures.SCARLET_FLOWING_TEXTURE, Colors.DEFAULT);

   public static void registerAll(RegisterHelper<FluidType> helper) {
      if (BnCFluids.hasOwnHoney()) {
         helper.register(BrewinAndChewin.asResource("honey"), HONEY);
      }
      helper.register(BrewinAndChewin.asResource("beer"), BEER);
      helper.register(BrewinAndChewin.asResource("vodka"), VODKA);
      helper.register(BrewinAndChewin.asResource("mead"), MEAD);
      helper.register(BrewinAndChewin.asResource("rice_wine"), RICE_WINE);
      helper.register(BrewinAndChewin.asResource("pale_jane"), PALE_JANE);
      helper.register(BrewinAndChewin.asResource("egg_grog"), EGG_GROG);
      helper.register(BrewinAndChewin.asResource("glittering_grenadine"), GLITTERING_GRENADINE);
      helper.register(BrewinAndChewin.asResource("saccharine_rum"), SACCHARINE_RUM);
      helper.register(BrewinAndChewin.asResource("salty_folly"), SALTY_FOLLY);
      helper.register(BrewinAndChewin.asResource("bloody_mary"), BLOODY_MARY);
      helper.register(BrewinAndChewin.asResource("red_rum"), RED_RUM);
      helper.register(BrewinAndChewin.asResource("strongroot_ale"), STRONGROOT_ALE);
      helper.register(BrewinAndChewin.asResource("steel_toe_stout"), STEEL_TOE_STOUT);
      helper.register(BrewinAndChewin.asResource("dread_nog"), DREAD_NOG);
      helper.register(BrewinAndChewin.asResource("withering_dross"), WITHERING_DROSS);
      helper.register(BrewinAndChewin.asResource("kombucha"), KOMBUCHA);
      helper.register(BrewinAndChewin.asResource("flaxen_cheese"), FLAXEN_CHEESE);
      helper.register(BrewinAndChewin.asResource("scarlet_cheese"), SCARLET_CHEESE);
   }
}
