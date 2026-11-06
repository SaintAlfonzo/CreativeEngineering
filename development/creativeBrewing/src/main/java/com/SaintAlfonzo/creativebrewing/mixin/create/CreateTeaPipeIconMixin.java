package com.SaintAlfonzo.creativebrewing.mixin.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Create's open-ended pipe spilling Builder's Tea ({@code OpenEndedPipe$TeaEffectHandler#applyEffects}, create 0.5.1.i,
 * vineflower-checked) gives Haste with {@code showIcon} false, every 5 ticks for 21 ticks. The icon is turned on, as for
 * Builder's Tea drunk from the bottle ({@code [effects] forceHudIconItems}, {@code potions/effects #8}).
 */
@Pseudo
@Mixin(targets = "com.simibubi.create.content.fluids.OpenEndedPipe$TeaEffectHandler", remap = false)
public abstract class CreateTeaPipeIconMixin {

    @ModifyArg(method = "applyEffects", require = 0, remap = false,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/effect/MobEffectInstance;<init>(Lnet/minecraft/world/effect/MobEffect;IIZZZ)V"),
            index = 5)
    private boolean creativeengineering$showIcon(boolean showIcon) {
        return true;
    }
}
