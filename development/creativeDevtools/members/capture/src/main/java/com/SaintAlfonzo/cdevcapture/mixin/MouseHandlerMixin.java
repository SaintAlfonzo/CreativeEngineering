package com.SaintAlfonzo.cdevcapture.mixin;

import com.SaintAlfonzo.cdevcapture.VirtualInput;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The {@code hover} step: {@code MouseHandler.xpos()} (m_91589_) and {@code ypos()} (m_91594_) return the
 * virtual mouse while it is active. GameRenderer scales these to GUI coordinates for the open screen's render,
 * so hovered slots and their tooltips follow it.
 */
@Mixin(value = MouseHandler.class, remap = false)
public abstract class MouseHandlerMixin {

    @Inject(method = "m_91589_", at = @At("HEAD"), cancellable = true, remap = false)
    private void cdevcapture$x(CallbackInfoReturnable<Double> cir) {
        if (VirtualInput.mouseActive) cir.setReturnValue(VirtualInput.mouseX);
    }

    @Inject(method = "m_91594_", at = @At("HEAD"), cancellable = true, remap = false)
    private void cdevcapture$y(CallbackInfoReturnable<Double> cir) {
        if (VirtualInput.mouseActive) cir.setReturnValue(VirtualInput.mouseY);
    }
}
