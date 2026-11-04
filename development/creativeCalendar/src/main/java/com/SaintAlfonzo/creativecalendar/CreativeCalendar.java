package com.SaintAlfonzo.creativecalendar;

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


/**
 * Creative Calendar (feature_writeup/creative_calendar.md): the pack's calendar, day and night length, seasons, season
 * types by biome and crop seasons, carved out of patcherMod on 2026-11-01. Creative Delight and Creative Brewing require
 * it; it requires patcherMod ({@code creativeengineering}, later Creative Common).
 *
 * Ids stay: tags, WTHIT option ids and lang keys keep the {@code creativeengineering} namespace ({@link #NAMESPACE}),
 * the command stays {@code /cetime}. This mod id is only the jar's own identity, its config file and its script packs.
 */
@Mod(CreativeCalendar.MOD_ID)
public class CreativeCalendar {

    public static final String MOD_ID = "creativecalendar";
    /** The namespace of every id this mod uses (tags, WTHIT lines): patcherMod's, unchanged by the carve. */
    public static final String NAMESPACE = "creativeengineering";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreativeCalendar() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CalendarConfig.SPEC, MOD_ID + "-common.toml");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // [time] master (feature_writeup/time.md): pushes into Serene Seasons / SeasonHUD at server start and on every
        // toml save (the Vinery port reads time/WineTime itself), the sun-driven season counter, and /cetime.
        // Calendar tooltip: time/TimeClient (client); season types, crop seasons and their tooltip subscribe themselves.
        modEventBus.addListener(CalendarConfigMigration::onConfigLoading); // before TimePresets reconciles the preset
        modEventBus.addListener(TimePush::onConfigReload);
        modEventBus.addListener(TimePresets::onConfigLoading);
        MinecraftForge.EVENT_BUS.register(TimePush.class);
        MinecraftForge.EVENT_BUS.register(SeasonClock.class);
        MinecraftForge.EVENT_BUS.register(TimeCommand.class);

        LOGGER.info("[creativecalendar] time, seasons and crop seasons loaded (config/{}-common.toml)", MOD_ID);
        CalendarConfigMigration.readAndStrip();
    }
}
