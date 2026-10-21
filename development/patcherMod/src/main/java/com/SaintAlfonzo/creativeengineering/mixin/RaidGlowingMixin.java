package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Glowing Illagers port (feature_writeup/feature_porting.md 2.1, jar retired 2026-10-20): every raider a raid adds to a
 * wave ({@code Raid#addWaveMob}, {@code m_37718_}) glows for good, so a raid can be followed through walls. Glowing II
 * as the mod (the level changes nothing for Glowing). {@code [raids] glowingRaiders}.
 */
@Mixin(Raid.class)
public abstract class RaidGlowingMixin {

    @Inject(method = "m_37718_", at = @At("HEAD"), remap = false)
    private void creativeengineering$glow(int wave, Raider raider, boolean recruited, CallbackInfoReturnable<Boolean> cir) {
        if (CEConfig.RAIDS.glowingRaiders.get()) {
            raider.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, CEConfig.RAIDS.glowingAmplifier.get(), false, false));
        }
    }
}
