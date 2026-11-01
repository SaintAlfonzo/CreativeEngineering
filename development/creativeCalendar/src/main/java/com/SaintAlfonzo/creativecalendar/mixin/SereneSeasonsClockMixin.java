package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.time.SeasonClock;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sereneseasons.handler.season.SeasonHandler;

/**
 * {@code [time.seasons] clock = SUN} (feature_writeup/time.md 3.3), server half: Serene Seasons' +1 per tick stands
 * down; {@link SeasonClock} moves the counter by the sun instead. The client half is
 * {@link SereneSeasonsClientClockMixin} (a client mixin: it touches {@code Minecraft}, which a dedicated server lacks).
 */
@Mixin(value = SeasonHandler.class, remap = false)
public class SereneSeasonsClockMixin {

	@Inject(method = "onWorldTick", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$sunServer(TickEvent.LevelTickEvent event, CallbackInfo ci) {
		if (!event.level.isClientSide && SeasonClock.enabled()) ci.cancel();
	}
}
