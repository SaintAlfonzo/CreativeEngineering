package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.time.SeasonClock;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sereneseasons.handler.season.TimeSkipHandler;

/**
 * {@code [time.seasons] clock = SUN}: Serene Seasons' time-skip handler (dayTime jumps above 1 per tick) stands down;
 * {@link SeasonClock} already counts every forward step. Left alone it would double count sleeping, and every Better
 * Days speed above 1.
 */
@Mixin(value = TimeSkipHandler.class, remap = false)
public class SereneSeasonsTimeSkipMixin {

	@Inject(method = "onWorldTick", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$sunSkips(TickEvent.LevelTickEvent event, CallbackInfo ci) {
		if (SeasonClock.enabled()) ci.cancel();
	}
}
