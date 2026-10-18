package umpaz.brewinandchewin.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

// Port: 1.19.2 effects have no particle of their own; LivingEntityMixin swaps the swirl for the stage particle.
public class RagingEffect extends MobEffect {
   public RagingEffect() {
      super(MobEffectCategory.BENEFICIAL, 6948353);
   }
}
