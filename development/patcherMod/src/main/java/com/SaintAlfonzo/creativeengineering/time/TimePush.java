package com.SaintAlfonzo.creativeengineering.time;

import club.iananderson.seasonhud.config.SeasonHudServer;
import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import sereneseasons.config.ServerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Pushes {@code [time]} into the other mods' configs (feature_writeup/time.md 3.2): Serene Seasons and SeasonHUD
 * (per-world SERVER configs, loaded just before {@link ServerAboutToStartEvent}; Forge sends the saved files to every
 * client at login). Better Days needs no push: {@code BetterDaysConfigMixin} answers its getters. The Vinery port
 * needs none either: it reads {@link WineTime} itself (time.md 3.7). Runs at server start and after every save of
 * the toml. Each changed value is logged old -> new.
 */
public final class TimePush {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Serene Seasons' lengths before this run's first push, for the SUN migration ({@link SeasonClock}). */
    static int startDayDuration = -1;
    static int startSubSeasonDuration = -1;

    private TimePush() {}

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        if (!TimeSettings.active()) {
            return;
        }
        if (ModList.get().isLoaded("sereneseasons")) {
            startDayDuration = ServerConfig.dayDuration.get();
            startSubSeasonDuration = ServerConfig.subSeasonDuration.get();
        }
        LOGGER.info("[time] Better Days from [time]: daySpeed {}, nightSpeed {}, dayStart {}, nightStart {} "
                        + "(the time values in betterdays-common.toml are ignored while [time] enabled)",
                fmt(TimeSettings.daySpeed()), fmt(TimeSettings.nightSpeed()), TimeSettings.dayStart(), TimeSettings.nightStart());
        push();
    }

    /** A save of creativeengineering-common.toml (by hand or in Create's Configs screen): push again. */
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != CEConfig.COMMON_SPEC) {
            return;
        }
        TimePresets.reconcile();
        if (!TimeSettings.active()) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(TimePush::push);
        }
    }

    /** Sets every derived value that differs; returns one line per change. */
    public static List<String> push() {
        List<String> changes = new ArrayList<>();
        if (!TimeSettings.active()) {
            return changes;
        }
        boolean offline = !CEConfig.TIME.pauseWhenEmpty.get();
        if (ModList.get().isLoaded("sereneseasons") && ServerConfig.SPEC.isLoaded()) {
            set(changes, "sereneseasons day_duration", ServerConfig.dayDuration, TimeSettings.seasonDayDuration());
            set(changes, "sereneseasons sub_season_duration", ServerConfig.subSeasonDuration, TimeSettings.daysPerSubSeason());
            set(changes, "sereneseasons starting_sub_season", ServerConfig.startingSubSeason, CEConfig.TIME.startingSubSeason.get());
            set(changes, "sereneseasons progress_season_while_offline", ServerConfig.progressSeasonWhileOffline, offline);
            ServerConfig.SPEC.save();
        }
        if (ModList.get().isLoaded("seasonhud") && SeasonHudServer.SERVER_SPEC.isLoaded()) {
            if (SeasonHudServer.getDayLength() != TimeSettings.seasonDayDuration()) {
                changes.add("seasonhud day_length " + SeasonHudServer.getDayLength() + " -> " + TimeSettings.seasonDayDuration());
                SeasonHudServer.setDayLength(TimeSettings.seasonDayDuration());
            }
            if (SeasonHudServer.getSubSeasonLength() != TimeSettings.daysPerSubSeason()) {
                changes.add("seasonhud sub_season_length " + SeasonHudServer.getSubSeasonLength() + " -> " + TimeSettings.daysPerSubSeason());
                SeasonHudServer.setSubSeasonLength(TimeSettings.daysPerSubSeason());
            }
            SeasonHudServer.SERVER_SPEC.save();
        }
        changes.forEach(line -> LOGGER.info("[time] {}", line));
        return changes;
    }

    private static <T> void set(List<String> changes, String name, ForgeConfigSpec.ConfigValue<T> value, T target) {
        T current = value.get();
        boolean same = current instanceof Double d && target instanceof Double t ? Math.abs(d - t) < 1e-9 : Objects.equals(current, target);
        if (!same) {
            value.set(target);
            changes.add(name + " " + current + " -> " + target);
        }
    }

    static String fmt(double value) {
        return String.format("%.3f", value);
    }
}
