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
 * snap back, and the sub-season change (a full chunk re-render) happens once.
 * <p>
 * Each server sync is an anchor: the client shows the synced value plus how far its dayTime has moved past the dayTime
 * it had when the sync arrived, never less. Adding every forward client step instead (before 2026-11-02) counted a
 * jump twice when it came with its sync ({@code /ccaltime date}, /time add): the server's value already held it, and
 * Better Days then glides the client's time of day up to the new one, adding it again. With {@code doDaylightCycle
 * false} no later sync corrected it (TODO {@code gameplay/time #7}, scratch r1101 CQ-TD-06: SeasonHUD read Summer
 * day 1 for Mid Autumn day 13). Listed under "client" in the mixin
 * config: a common mixin referencing {@code Minecraft} fails to apply on a dedicated server (patcherMod 1.0.8 boot crash).
 */
@Mixin(value = SeasonHandler.class, remap = false)
public class SereneSeasonsClientClockMixin {

	@Shadow(remap = false) private Season.SubSeason lastSeason;

	/** The value this handler last wrote, and for which dimension: any other value came from a server sync. */
	@Unique private static Integer creativeengineering$lastWritten = null;
	@Unique private static ResourceKey<Level> creativeengineering$lastDimension = null;
	/** The last sync: its value and the client's dayTime when it arrived. */
	@Unique private static int creativeengineering$anchorTicks = 0;
	@Unique private static long creativeengineering$anchorDayTime = Long.MIN_VALUE;

	@Inject(method = "onClientTick", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$sunClient(TickEvent.ClientTickEvent event, CallbackInfo ci) {
		if (!TimeSettings.sunSeasons()) return;
		ci.cancel();
		Minecraft mc = Minecraft.getInstance();
		if (event.phase != TickEvent.Phase.END || mc.player == null || mc.level == null) return;
		ResourceKey<Level> dimension = mc.level.dimension();
		if (!ServerConfig.isDimensionWhitelisted(dimension)) return;
		long dayTime = mc.level.getDayTime();
		Integer held = SeasonHandler.clientSeasonCycleTicks.get(dimension);
		if (held == null || dimension != creativeengineering$lastDimension || !held.equals(creativeengineering$lastWritten)
				|| creativeengineering$anchorDayTime == Long.MIN_VALUE) {
			creativeengineering$anchorTicks = held == null ? 0 : held;
			creativeengineering$anchorDayTime = dayTime;
		}
		long ahead = Math.max(0, dayTime - creativeengineering$anchorDayTime);
		int now = (int) Math.floorMod(creativeengineering$anchorTicks + ahead, (long) SeasonTime.ZERO.getCycleDuration());
		SeasonHandler.clientSeasonCycleTicks.put(dimension, now);
		creativeengineering$lastWritten = now;
		creativeengineering$lastDimension = dimension;
		SeasonTime calendar = new SeasonTime(now);
		if (calendar.getSubSeason() != lastSeason) {
			mc.levelRenderer.allChanged();
			lastSeason = calendar.getSubSeason();
		}
	}
}
