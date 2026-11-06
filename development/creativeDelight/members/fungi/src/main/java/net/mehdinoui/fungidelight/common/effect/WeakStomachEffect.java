package net.mehdinoui.fungidelight.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class WeakStomachEffect extends MobEffect {
   public WeakStomachEffect() {
      super(MobEffectCategory.HARMFUL, 5002616);
   }

   public boolean isDurationEffectTick(int duration, int amplifier) {
      return false;
   }
}
