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
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        modEventBus.addListener(TimePush::onConfigReload);
        modEventBus.addListener(TimePresets::onConfigLoading);
        MinecraftForge.EVENT_BUS.register(TimePush.class);
        MinecraftForge.EVENT_BUS.register(SeasonClock.class);
        MinecraftForge.EVENT_BUS.register(TimeCommand.class);

        LOGGER.info("[creativecalendar] time, seasons and crop seasons loaded (config/{}-common.toml)", MOD_ID);
        warnOldKeys();
    }

    /** Lines of creativeengineering-common.toml that held the keys moved here: section headers and the two strays. */
    private static final Pattern OLD_KEY = Pattern.compile(
            "^\\s*(\\[(?:time|seasons|crop_seasons)(?:\\.[a-z.]+)?\\]|(?:seasonsMeltIntervalTicks|cropSeasons|cropOutOfSeason)\\s*=)",
            Pattern.MULTILINE);

    /**
     * The keys moved out of patcherMod's file (creative_calendar.md 4.3). Forge drops them from that file on the first
     * boot with the new patcherMod, so an edited value left there is lost: say which ones were still there. Runs at
     * construction, before Forge loads and corrects the COMMON configs.
     */
    private static void warnOldKeys() {
        Path old = FMLPaths.CONFIGDIR.get().resolve("creativeengineering-common.toml");
        if (!Files.isRegularFile(old)) return;
        String text;
        try {
            text = Files.readString(old, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return;
        }
        List<String> found = new ArrayList<>();
        Matcher m = OLD_KEY.matcher(text);
        while (m.find()) found.add(m.group(1).replaceAll("\\s*=$", ""));
        if (found.isEmpty()) return;
        LOGGER.warn("[creativecalendar] config/creativeengineering-common.toml still has keys that moved to config/{}-common.toml: {}. "
                + "Creative Calendar reads only its own file; copy any edited value over (patcherMod drops these from its file).",
                MOD_ID, found);
    }
}
