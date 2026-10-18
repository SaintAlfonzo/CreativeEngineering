package umpaz.brewinandchewin.common.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.RegisterEvent.RegisterHelper;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.effect.IntoxicationEffect;
import umpaz.brewinandchewin.common.effect.RagingEffect;
import umpaz.brewinandchewin.common.effect.SweetHeartEffect;
import umpaz.brewinandchewin.common.effect.TipsyEffect;

// Port: plain MobEffect fields (1.21's Holder<MobEffect> doesn't exist for effects on 1.19.2). Forge fires the effect
// RegisterEvent before blocks and items, so these exist by the time BnCFoods builds.
public class BnCEffects {
   public static final MobEffect INTOXICATION = new IntoxicationEffect();
   public static final MobEffect SWEET_HEART = new SweetHeartEffect();
   public static final MobEffect RAGING = new RagingEffect();
   public static final MobEffect TIPSY = new TipsyEffect();

   public static void registerAll(RegisterHelper<MobEffect> helper) {
      helper.register(BrewinAndChewin.asResource("intoxication"), INTOXICATION);
      helper.register(BrewinAndChewin.asResource("sweet_heart"), SWEET_HEART);
      helper.register(BrewinAndChewin.asResource("raging"), RAGING);
      helper.register(BrewinAndChewin.asResource("tipsy"), TIPSY);
   }
}
