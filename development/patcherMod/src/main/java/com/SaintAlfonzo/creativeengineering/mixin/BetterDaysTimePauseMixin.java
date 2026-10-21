package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.time.TimePause;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code [time] pauseWhenEmpty}: Better Days' whole tick (time step, time effects, its -1 compensation for vanilla's
 * +1, which {@link ServerLevelTimePauseMixin} skips too) is skipped while {@link TimePause#paused} says so.
 */
@Pseudo
@Mixin(targets = "betterdays.time.TimeService", remap = false)
public class BetterDaysTimePauseMixin {

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$pauseWhenEmpty(CallbackInfo ci) {
		if (TimePause.paused(ServerLifecycleHooks.getCurrentServer())) ci.cancel();
	}
}
