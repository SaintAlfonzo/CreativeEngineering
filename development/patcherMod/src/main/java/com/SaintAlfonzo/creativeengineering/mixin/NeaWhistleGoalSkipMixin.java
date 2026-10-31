package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.perf.WhistleArrowTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nock Enough Arrows 1.2.0: TemptByWhistleGoal.canUse (on every mob) queries for whistling arrows within 12 blocks
 * each time the goal selector checks it. With no whistling arrow on the server it cannot succeed, so answer false
 * before the query ({@link WhistleArrowTracker}). canContinueToUse ends with canUse, so a running goal still stops
 * when the last arrow goes. Pseudo: the mod may be removed.
 */
@Pseudo
@Mixin(targets = "com.itayfeder.nock_enough_arrows.arrows.whistling.goal.TemptByWhistleGoal", remap = false)
public abstract class NeaWhistleGoalSkipMixin {

	// m_8036_ = Goal.canUse()
	@Inject(method = "m_8036_", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void creativeengineering$noArrows(CallbackInfoReturnable<Boolean> cir) {
		if (!WhistleArrowTracker.anyArrows()) cir.setReturnValue(false);
	}
}
