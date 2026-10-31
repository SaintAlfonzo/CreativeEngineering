package com.SaintAlfonzo.creativeengineering.time;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import sereneseasons.api.SSGameRules;
import sereneseasons.handler.season.SeasonHandler;
import sereneseasons.season.SeasonSavedData;
import sereneseasons.season.SeasonTime;

import java.util.HashMap;
import java.util.Map;

/**
 * Seasons that follow the sun ({@code [time.seasons] clock = SUN}, feature_writeup/time.md 3.3). Serene Seasons' own
 * +1 per tick and its time-skip handler stand down ({@code SereneSeasonsClockMixin}); here every forward dayTime step
 * goes into {@code seasonCycleTicks}, with {@code day_duration} pushed as 24000. One season day is one sun day whatever
 * Better Days' speeds, and sleeping or /time add move the seasons exactly as far as the sun. Backward steps
 * (/time set earlier, Better Days' overflow wrap) are ignored.
 * <p>
 * The counter is lined up with sunrise ({@code [time.day] dayStart}), so the day number changes at 6:00 am. A level
 * switching to SUN keeps its sub-season and day number once ({@link Marker} remembers it in the level's data).
 */
public final class SeasonClock {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MARKER = "creativeengineering_season_clock";
    private static final int SYNC_INTERVAL = 20;

    private static final Map<ResourceKey<Level>, Long> LAST_DAY_TIME = new HashMap<>();

    private SeasonClock() {}

    public static boolean enabled() {
        return TimeSettings.sunSeasons() && ModList.get().isLoaded("sereneseasons");
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        LAST_DAY_TIME.clear();
        if (enabled()) {
            event.getServer().getAllLevels().forEach(SeasonClock::migrate);
        } else if (TimeSettings.active() && ModList.get().isLoaded("sereneseasons")) {
            event.getServer().getAllLevels().forEach(SeasonClock::markTicks);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        LAST_DAY_TIME.clear();
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || !enabled()) {
            return;
        }
        long dayTime = level.getDayTime();
        Long last = LAST_DAY_TIME.put(level.dimension(), dayTime);
        if (last == null) {
            migrate(level); // switched to SUN while running
            return;
        }
        if (!level.getGameRules().getBoolean(SSGameRules.RULE_DOSEASONCYCLE)) {
            return;
        }
        long step = dayTime - last;
        if (step <= 0) {
            return;
        }
        SeasonSavedData data = SeasonHandler.getSeasonSavedData(level);
        if (data == null) {
            return;
        }
        int cycle = SeasonTime.ZERO.getCycleDuration();
        data.seasonCycleTicks = (int) Math.floorMod(data.seasonCycleTicks + step, (long) cycle);
        data.setDirty();
        if (step > SYNC_INTERVAL || level.getServer().getTickCount() % SYNC_INTERVAL == 0) {
            SeasonHandler.sendSeasonUpdate(level); // a sleep or /time add shows at once
        }
    }

    /** After /cetime date moved the sun and the counter together: the jump is not a step to add. */
    static void jumped(ServerLevel level) {
        LAST_DAY_TIME.put(level.dimension(), level.getDayTime());
    }

    /**
     * First SUN tick of a level (or the first since it ran on TICKS): keep its sub-season and day number, with the
     * time inside the day from the sun. Old lengths are Serene Seasons' values before this run's push.
     */
    static void migrate(ServerLevel level) {
        Marker marker = level.getDataStorage().computeIfAbsent(Marker::load, Marker::new, MARKER);
        if (marker.sun) {
            return;
        }
        SeasonSavedData data = SeasonHandler.getSeasonSavedData(level);
        if (data == null) {
            return;
        }
        int oldDay = TimePush.startDayDuration > 0 ? TimePush.startDayDuration : TimeSettings.DAY_UNITS;
        int oldSub = TimePush.startSubSeasonDuration > 0 ? TimePush.startSubSeasonDuration : TimeSettings.daysPerSubSeason();
        int newSub = TimeSettings.daysPerSubSeason();
        int days = data.seasonCycleTicks / Math.max(1, oldDay);
        int subSeason = (days / Math.max(1, oldSub)) % 12;
        int dayInSub = Math.min(days % Math.max(1, oldSub), newSub - 1);
        long timeInDay = Math.floorMod(level.getDayTime() - (long) TimeSettings.dayStart(), (long) TimeSettings.DAY_UNITS);
        int before = data.seasonCycleTicks;
        data.seasonCycleTicks = (int) (((long) subSeason * newSub + dayInSub) * TimeSettings.DAY_UNITS + timeInDay);
        data.setDirty();
        marker.sun = true;
        marker.setDirty();
        SeasonHandler.sendSeasonUpdate(level);
        LOGGER.info("[time] {}: seasons now follow the sun; sub-season {} day {} kept (seasonCycleTicks {} -> {})",
                level.dimension().location(), subSeason + 1, dayInSub + 1, before, data.seasonCycleTicks);
    }

    /** Called when the clock is set back to TICKS, so a later switch to SUN migrates again. */
    public static void markTicks(ServerLevel level) {
        Marker marker = level.getDataStorage().computeIfAbsent(Marker::load, Marker::new, MARKER);
        if (marker.sun) {
            marker.sun = false;
            marker.setDirty();
        }
    }

    /** Remembers that a level's season counter already runs on the sun. */
    static final class Marker extends SavedData {
        boolean sun;

        static Marker load(CompoundTag tag) {
            Marker marker = new Marker();
            marker.sun = tag.getBoolean("Sun");
            return marker;
        }

        @Override
        public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
            tag.putBoolean("Sun", sun);
            return tag;
        }
    }
}
