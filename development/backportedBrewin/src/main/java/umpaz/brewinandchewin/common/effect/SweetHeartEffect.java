package umpaz.brewinandchewin.common.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class SweetHeartEffect extends MobEffect {
   public SweetHeartEffect() {
      super(MobEffectCategory.BENEFICIAL, 16711769);
   }

   @Override
   public boolean isDurationEffectTick(int duration, int amplifier) {
      int k = 20 >> amplifier;
      return k > 0 ? duration % k == 0 : true;
   }

   @Override
   public void applyEffectTick(LivingEntity entity, int amplifier) {
      if (entity instanceof ServerPlayer player) {
         float saturation = player.getFoodData().getSaturationLevel();
         if (saturation > 0.0F && player.getHealth() < player.getMaxHealth()) {
            float healingAmount = Math.min(saturation, 1.0F);
            player.heal(healingAmount);
            player.getFoodData().setSaturation(saturation - healingAmount);
         }
      }
   }
}
