package com.SaintAlfonzo.cecapture.mixin;

import com.SaintAlfonzo.cecapture.VirtualInput;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The {@code key} step and the tooltip stage's shift / ctrl options: {@code Screen.hasControlDown()} (m_96637_),
 * {@code hasShiftDown()} (m_96638_), {@code hasAltDown()} (m_96639_) report the virtual key as held. Tooltip
 * code that polls GLFW directly instead isn't covered.
 */
@Mixin(value = Screen.class, remap = false)
public abstract class ScreenModifierMixin {

    @Inject(method = "m_96637_", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cecapture$ctrl(CallbackInfoReturnable<Boolean> cir) {
        if (VirtualInput.ctrl) cir.setReturnValue(true);
    }

    @Inject(method = "m_96638_", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cecapture$shift(CallbackInfoReturnable<Boolean> cir) {
        if (VirtualInput.shift) cir.setReturnValue(true);
    }

    @Inject(method = "m_96639_", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cecapture$alt(CallbackInfoReturnable<Boolean> cir) {
        if (VirtualInput.alt) cir.setReturnValue(true);
    }
}
