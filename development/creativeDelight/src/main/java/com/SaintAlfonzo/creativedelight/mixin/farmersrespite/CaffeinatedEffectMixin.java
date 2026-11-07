package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.LivingEntity;

/**
 * Farmer's Respite's {@code CaffeinatedEffect#applyEffectTick} ({@code m_6742_}, FarmersRespite-1.19-2.0, vineflower-checked)
 * only set the player's Time Since Rest stat to -24000 per level every tick. Cancelled: Caffeinated keeps phantoms away
 * through its Phantom Bane part instead ({@code potion/PhantomBaneRules}). Its speed and attack speed modifiers are
 * attribute modifiers and unaffected.
 */
@Pseudo
@Mixin(targets = "umpaz.farmersrespite.common.effect.CaffeinatedEffect", remap = false)
public abstract class CaffeinatedEffectMixin {

    @Inject(method = "m_6742_", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void creativeengineering$noRestReset(LivingEntity entity, int amplifier, CallbackInfo ci) {
        ci.cancel();
    }
}
