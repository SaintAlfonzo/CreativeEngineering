package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.CalendarConfig;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sereneseasons.handler.season.RandomUpdateHandler;

/**
 * Serene Seasons' snow and ice melting, run every N ticks with N times the chance.
 *
 * THE COST:
 *   {@code RandomUpdateHandler#onWorldTick} runs every tick in every whitelisted level outside winter. Each
 *   time it copies every chunk the server holds into a list, shuffles it, and for each chunk near a player
 *   rolls a 1-in-4..16 chance to test one column. The list building and shuffle is most of the cost.
 *
 * THE FIX:
 *   The handler is skipped except on every Nth game tick (creativecalendar-common.toml perf.seasonsMeltIntervalTicks, default
 *   4), and the chance roll {@code nextInt(bound)} inside {@code meltInChunk} becomes
 *   {@code nextInt(bound / N)}, so each chunk is tested N times less often with N times the chance. The
 *   average melt rate is the same for N that divides the bound (4 divides every one of the mod's bounds
 *   16, 12, 8, 4); a bigger N caps at a certain roll and melts slower in summer. The weather-frequency
 *   adjustment in the same handler also runs every Nth tick, which changes nothing it does.
 */
@Mixin(value = RandomUpdateHandler.class, remap = false)
public class SereneSeasonsMeltThrottleMixin {

	@Inject(method = "onWorldTick", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$throttle(TickEvent.LevelTickEvent event, CallbackInfo ci) {
		int interval = CalendarConfig.PERF.seasonsMeltIntervalTicks.get();
		if (interval > 1 && event.level.getGameTime() % interval != 0) ci.cancel();
	}

	@ModifyArg(method = "meltInChunk", remap = false, index = 0,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/util/RandomSource;m_188503_(I)I"))
	private static int creativeengineering$scaleChance(int bound) {
		int interval = CalendarConfig.PERF.seasonsMeltIntervalTicks.get();
		return interval > 1 ? Math.max(1, bound / interval) : bound;
	}
}
