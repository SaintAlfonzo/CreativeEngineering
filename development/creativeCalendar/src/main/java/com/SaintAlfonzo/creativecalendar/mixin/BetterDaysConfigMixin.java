package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.time.TimeSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Better Days' speeds and sunrise / sunset come from {@code [time.day]} while {@code [time] enabled}
 * (feature_writeup/time.md 3.2). Its config is WhiteNoise, not Forge, so the getters are answered instead of the
 * values being set; betterdays-common.toml keeps its own numbers, ignored. Always RATIO speeds: MINUTES assumes a
 * 12,000-unit half day. dayStart / nightStart are cached by TimeService when it loads (restart to change them).
 */
@Pseudo
@Mixin(targets = "betterdays.config.ConfigHandler$Common", remap = false)
public class BetterDaysConfigMixin {

	@Inject(method = "daySpeed", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$daySpeed(CallbackInfoReturnable<Double> cir) {
		if (TimeSettings.active()) cir.setReturnValue(TimeSettings.daySpeed());
	}

	@Inject(method = "nightSpeed", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$nightSpeed(CallbackInfoReturnable<Double> cir) {
		if (TimeSettings.active()) cir.setReturnValue(TimeSettings.nightSpeed());
	}

	@Inject(method = "dayStart", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$dayStart(CallbackInfoReturnable<Double> cir) {
		if (TimeSettings.active()) cir.setReturnValue(TimeSettings.dayStart());
	}

	@Inject(method = "nightStart", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$nightStart(CallbackInfoReturnable<Double> cir) {
		if (TimeSettings.active()) cir.setReturnValue(TimeSettings.nightStart());
	}
}
