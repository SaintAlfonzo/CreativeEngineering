package com.SaintAlfonzo.cdevcapture.mixin;

import com.SaintAlfonzo.cdevcapture.VirtualInput;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Code that polls the modifier keys through {@code InputConstants.isKeyDown(window, key)} (m_84830_) instead of
 * {@code Screen.hasShiftDown()}: {@code AbstractContainerScreen.mouseClicked} reads 340/344 there to pick a quick
 * move, so a {@code click_slot} with shift is a real shift-click. Left and right shift / ctrl / alt report held
 * while the virtual key is set.
 */
@Mixin(value = InputConstants.class, remap = false)
public abstract class InputConstantsMixin {

	@Inject(method = "m_84830_", at = @At("HEAD"), cancellable = true, remap = false)
	private static void cdevcapture$isKeyDown(long window, int key, CallbackInfoReturnable<Boolean> cir) {
		if (VirtualInput.shift && (key == 340 || key == 344)
				|| VirtualInput.ctrl && (key == 341 || key == 345)
				|| VirtualInput.alt && (key == 342 || key == 346)) {
			cir.setReturnValue(true);
		}
	}
}
