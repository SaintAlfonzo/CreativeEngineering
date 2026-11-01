package umpaz.brewinandchewin.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.common.effect.TipsyEffect;
import umpaz.brewinandchewin.common.registry.BnCEffects;

/**
 * Port: 1.21 gives each effect its own particle; 1.19.2 spawns one swirl coloured by all visible effects. Each swirl is
 * swapped, at random among the visible effects, for Tipsy's drunk bubble or Raging's stage particle (a stage-1 particle
 * with no stacks shows 1 time in 12, as upstream). Clients only know their own player's effects plus synced Raging stacks,
 * so other entities show the swirl for Tipsy.
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
   @WrapOperation(
      method = "tickEffects",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
   )
   private void brewinandchewin$effectParticles(
      Level level, ParticleOptions particle, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, Operation<Void> original
   ) {
      LivingEntity self = (LivingEntity)(Object)this;
      RagingAttachment raging = BrewinAndChewin.getHelper().getRagingAttachment(self);
      List<Object> sources = new ArrayList<>();
      int others = 0;
      for (MobEffectInstance instance : self.getActiveEffects()) {
         if (!instance.isVisible()) {
            continue;
         }
         if (instance.getEffect() == BnCEffects.TIPSY || instance.getEffect() == BnCEffects.RAGING) {
            sources.add(instance.getEffect());
         } else {
            others++;
         }
      }
      if (raging != null && raging.getStacks() > 0 && !sources.contains(BnCEffects.RAGING)) {
         sources.add(BnCEffects.RAGING);
      }
      if (sources.isEmpty()) {
         original.call(level, particle, x, y, z, xSpeed, ySpeed, zSpeed);
         return;
      }
      int pick = level.getRandom().nextInt(sources.size() + others);
      if (pick >= sources.size()) {
         original.call(level, particle, x, y, z, xSpeed, ySpeed, zSpeed);
      } else if (sources.get(pick) == BnCEffects.TIPSY) {
         original.call(level, TipsyEffect.getParticle(TipsyEffect.COLOR), x, y, z, 0.0, 0.0, 0.0);
      } else {
         int stacks = raging == null ? 0 : raging.getStacks();
         if (stacks > 0 || level.getRandom().nextInt(12) == 0) {
            original.call(level, RagingAttachment.getParticleType(stacks, 0.75F), x, y, z, 0.0, 0.0, 0.0);
         }
      }
   }
}
