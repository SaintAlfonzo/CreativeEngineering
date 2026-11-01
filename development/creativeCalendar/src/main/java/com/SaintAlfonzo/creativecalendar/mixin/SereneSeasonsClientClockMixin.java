package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.time.TimeSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sereneseasons.api.season.Season;
import sereneseasons.config.ServerConfig;
import sereneseasons.handler.season.SeasonHandler;
import sereneseasons.season.SeasonTime;

/**
 * {@code [time.seasons] clock = SUN} (feature_writeup/time.md 3.3), client half: between the server's syncs (every
 * second) the client counter follows its own dayTime, not +1 per tick, so the day shown doesn't run ahead by day and
 * snap back, and the sub-season change (a full chunk re-render) happens once. Listed under "client" in the mixin
 * config: a common mixin referencing {@code Minecraft} fails to apply on a dedicated server (patcherMod 1.0.8 boot crash).
 */
@Mixin(value = SeasonHandler.class, remap = false)
public class SereneSeasonsClientClockMixin {

	@Shadow(remap = false) private Season.SubSeason lastSeason;

	@Unique private static long creativeengineering$lastClientDayTime = Long.MIN_VALUE;

	@Inject(method = "onClientTick", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$sunClient(TickEvent.ClientTickEvent event, CallbackInfo ci) {
		if (!TimeSettings.sunSeasons()) return;
		ci.cancel();
		Minecraft mc = Minecraft.getInstance();
		if (event.phase != TickEvent.Phase.END || mc.player == null || mc.level == null) return;
		ResourceKey<Level> dimension = mc.level.dimension();
		if (!ServerConfig.isDimensionWhitelisted(dimension)) return;
		long dayTime = mc.level.getDayTime();
		long last = creativeengineering$lastClientDayTime;
		creativeengineering$lastClientDayTime = dayTime;
		long step = last == Long.MIN_VALUE ? 0 : dayTime - last;
		int cycle = SeasonTime.ZERO.getCycleDuration();
		SeasonHandler.clientSeasonCycleTicks.compute(dimension,
				(key, ticks) -> ticks == null ? 0 : step > 0 ? (int) Math.floorMod(ticks + step, (long) cycle) : ticks);
		SeasonTime calendar = new SeasonTime(SeasonHandler.clientSeasonCycleTicks.get(dimension));
		if (calendar.getSubSeason() != lastSeason) {
			mc.levelRenderer.allChanged();
			lastSeason = calendar.getSubSeason();
		}
	}
}
