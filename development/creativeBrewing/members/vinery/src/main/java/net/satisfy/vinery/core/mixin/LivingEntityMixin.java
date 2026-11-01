package net.satisfy.vinery.core.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.satisfy.vinery.core.registry.MobEffectRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Double jump: jump power 0.1 per level, one less fall damage.
 * Port: upstream's custom-food effect hook is left out (nothing in Vinery sets that component).
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "calculateFallDamage", at = @At("RETURN"), cancellable = true)
    private void vinery$modifyJumpBoostFallDamage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasEffect(MobEffectRegistry.get(MobEffectRegistry.IMPROVED_JUMP_BOOST)) && !entity.hasEffect(MobEffects.JUMP)) {
            cir.setReturnValue(Math.max(0, cir.getReturnValue() - 1));
        }
    }

    /**
     * Pack: the higher of Double Jump's power and Jump Boost's (vanilla's return value), like two Jump Boost sources.
     * Upstream replaced Jump Boost at HEAD, so Double Jump I with Jump Boost II jumped lower than Jump Boost II alone.
     */
    @Inject(method = "getJumpBoostPower", at = @At(value = "RETURN"), cancellable = true)
    private void vinery$improvedJumpBoost(CallbackInfoReturnable<Double> cir) {
        MobEffectInstance effect = ((LivingEntity) (Object) this).getEffect(MobEffectRegistry.get(MobEffectRegistry.IMPROVED_JUMP_BOOST));
        if (effect != null) {
            cir.setReturnValue(Math.max(cir.getReturnValue(), 0.1D * (effect.getAmplifier() + 1)));
        }
    }
}
