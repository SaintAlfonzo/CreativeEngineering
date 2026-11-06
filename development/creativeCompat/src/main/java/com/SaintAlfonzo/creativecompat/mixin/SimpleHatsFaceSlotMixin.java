package com.SaintAlfonzo.creativecompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Simple Hats 0.3.2 draws hats on the player only from the Curios slot whose identifier is "head" ({@code HatLayer}:
 * {@code "head".equals(stacksHandler.getIdentifier())} in the per-slot lambda). The pack's Face slot
 * (patcherMod compat/CuriosSlots) takes the Simple Hats masks and chalk stick, so a mask worn there was invisible.
 * Here the same check also passes for "face", so a hat in Face is drawn the same way as one in Head (cosmetic stack and
 * render toggle included). feature_writeup/curios_slots.md, TODO gameplay/misc #20.
 * Pseudo: Simple Hats may be removed; the plugin also gates it on the mod.
 */
@Pseudo
@Mixin(targets = "fonnymunkey.simplehats.client.hat.HatLayer", remap = false)
public class SimpleHatsFaceSlotMixin {

	@Redirect(method = "lambda$render$0", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z"))
	private boolean creativeengineering$headOrFace(String head, Object identifier) {
		return head.equals(identifier) || "face".equals(identifier);
	}
}
