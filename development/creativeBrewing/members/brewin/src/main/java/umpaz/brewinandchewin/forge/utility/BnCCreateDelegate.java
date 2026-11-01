package umpaz.brewinandchewin.forge.utility;

import com.simibubi.create.AllFluids;
import net.minecraft.world.level.material.Fluid;

/** Create classes live only here, so nothing loads them unless Create is present. */
public class BnCCreateDelegate {
   public static Fluid getPotionSource() {
      return AllFluids.POTION.get().getSource();
   }
}
