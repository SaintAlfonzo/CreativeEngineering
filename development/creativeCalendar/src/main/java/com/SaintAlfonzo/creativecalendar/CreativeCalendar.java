package com.SaintAlfonzo.creativecalendar;

import com.SaintAlfonzo.creativecommon.swap.ConfigRename;
import com.SaintAlfonzo.creativecalendar.time.SeasonClock;
import com.SaintAlfonzo.creativecalendar.time.TimeCommand;
import com.SaintAlfonzo.creativecalendar.time.TimePresets;
import com.SaintAlfonzo.creativecalendar.time.TimePush;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;


/**
 * Creative Calendar (feature_writeup/creative_calendar.md): the pack's calendar, day and night length, seasons, season
 * types by biome and crop seasons, carved out of patcherMod on 2026-11-01. Creative Delight and Creative Brewing require
 * it; it requires patcherMod ({@code creativeengineering}, later Creative Common).
 *
 * Tags (season types, snow seasons, crop seasons) are under this jar's own namespace ({@link #NAMESPACE}) since
 * 2026-11-10 (namespace_move.md step 6); they were {@code creativeengineering:} until then. Tags are not saved, so no
 * alias. Lang keys keep their {@code creativeengineering.} prefix (text only); the command stays {@code /ccaltime}.
 */
@Mod(CreativeCalendar.MOD_ID)
public class CreativeCalendar {

    public static final String MOD_ID = "creativecalendar";
    /** The namespace of every tag this mod keys (its own mod id since 2026-11-10). */
    public static final String NAMESPACE = MOD_ID;
    public static final Logger LOGGER = LogUtils.getLogger();

    /** patcherMod's file, which held [seasons], [crop_seasons], [time] and the three strays before the carve. */
    private static final String OLD_FILE = "creativeengineering-common.toml";
    /** The tables taken from it; [perf] and [wthit] hold the strays, and Forge's correction drops their other keys. */
    private static final List<String> SECTIONS = List.of("seasons", "crop_seasons", "time", "perf", "wthit");

    public CreativeCalendar() {
        // First boot without this jar's file: the player's values come over (Common's ConfigRename, before Forge reads
        // the COMMON configs).
        ConfigRename.carrySections(OLD_FILE, MOD_ID + "-common.toml", SECTIONS);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CalendarConfig.SPEC, MOD_ID + "-common.toml");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // [time] master (feature_writeup/time.md): pushes into Serene Seasons / SeasonHUD at server start and on every
        // toml save (the Vinery port reads time/WineTime itself), the sun-driven season counter, and /ccaltime.
        // Calendar tooltip: time/TimeClient (client); season types, crop seasons and their tooltip subscribe themselves.
        modEventBus.addListener(TimePush::onConfigReload);
        modEventBus.addListener(TimePresets::onConfigLoading);
        MinecraftForge.EVENT_BUS.register(TimePush.class);
        MinecraftForge.EVENT_BUS.register(SeasonClock.class);
        MinecraftForge.EVENT_BUS.register(TimeCommand.class);
        // Serene Seasons' per-biome answers kept until a tag or config reload (seasons/SeasonAnswers, perf.md 19)
        com.SaintAlfonzo.creativecalendar.seasons.SeasonAnswers.register(modEventBus);

        LOGGER.info("[creativecalendar] time, seasons and crop seasons loaded (config/{}-common.toml)", MOD_ID);
    }
}
