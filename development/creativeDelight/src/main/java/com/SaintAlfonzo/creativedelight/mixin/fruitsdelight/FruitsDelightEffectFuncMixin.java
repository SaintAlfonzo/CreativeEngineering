package com.SaintAlfonzo.creativedelight.mixin.fruitsdelight;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.SaintAlfonzo.creativecommon.effect.EffectRules;

import net.minecraft.world.effect.MobEffectInstance;

/**
 * Fruits Delight builds every jam, jelly and jello effect through {@code EffectFunc#getEffect(int)} (fruitsdelight 0.5.11,
 * vineflower-checked): the jellied foods' NBT food properties and tooltips, and the Create open-ended pipe. Remapping the
 * instance there ({@link EffectRules}, {@code [effects] remap}) puts Teleport, not Chorus, in those tooltips too; the
 * {@code addEffect} mixin would swap the applied effect either way. Moved from Creative Common 2026-11-07; the
 * {@code fruitsdelight.} compartment applies it only with Fruits Delight 0.5.11 (CDMixinPlugin).
 */
@Pseudo
@Mixin(targets = "dev.xkmc.fruitsdelight.init.food.EffectFunc", remap = false)
public abstract class FruitsDelightEffectFuncMixin {

    @Inject(method = "getEffect", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private void creativedelight$remap(int effectLevel, CallbackInfoReturnable<MobEffectInstance> cir) {
        MobEffectInstance instance = cir.getReturnValue();
        if (instance != null) cir.setReturnValue(EffectRules.remap(instance));
    }
}
