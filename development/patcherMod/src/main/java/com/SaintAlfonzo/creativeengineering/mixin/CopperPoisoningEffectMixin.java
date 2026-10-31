package com.SaintAlfonzo.creativeengineering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.SaintAlfonzo.creativeengineering.potion.CopperPoisoningHandler;

import net.minecraft.world.entity.LivingEntity;

/**
 * Everything Copper's Copper Poisoning ({@code CopperPoisoning#applyEffectTick}, SRG {@code m_6742_},
 * checked with vineflower on everythingcopper-1.19.2-2.3.4) dealt {@code 2 << amplifier} damage through
 * its own sources. It gives Poison instead ({@code potions/effects #11},
 * {@link CopperPoisoningHandler#poisonFromEffect}). Instant effect, so this runs once per application
 * (a copper nugget). {@code @Pseudo}: no compile dependency on the jar.
 */
@Pseudo
@Mixin(targets = "cy.jdkdigital.everythingcopper.common.effect.CopperPoisoning", remap = false)
public abstract class CopperPoisoningEffectMixin {

    @Inject(method = "m_6742_", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void creativeengineering$poisonInstead(LivingEntity entity, int amplifier, CallbackInfo ci) {
        CopperPoisoningHandler.poisonFromEffect(entity, amplifier);
        ci.cancel();
    }
}
