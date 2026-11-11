package com.SaintAlfonzo.creativecalendar;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Creative Calendar's config, {@code config/creativecalendar-common.toml} (feature_writeup/creative_calendar.md 4.3).
 * Every section moved from creativeengineering-common.toml on 2026-11-01 with its keys and comments unchanged (rule 3.3
 * of creative_split.md): {@code [seasons]}, {@code [crop_seasons]}, {@code [time]} (with {@code .day}, {@code .seasons},
 * {@code .wine}, {@code .wine.boosts}), and the two keys of other sections that only Calendar reads, kept under their
 * old section names: {@code [perf] seasonsMeltIntervalTicks} and {@code [wthit] cropSeasons} / {@code cropOutOfSeason}.
 *
 * The {@code enabled} keys of {@code [seasons]}, {@code [crop_seasons]} and {@code [time]} are the feature switches
 * ({@link #feature}, {@code global.CAL.feature(id)} in scripts).
 */
public final class CalendarConfig {

    public static final ForgeConfigSpec SPEC;
    public static final Seasons SEASONS;
    public static final CropSeasons CROP_SEASONS;
    public static final Time TIME;
    public static final Perf PERF;
    public static final Wthit WTHIT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SEASONS = new Seasons(builder);
        CROP_SEASONS = new CropSeasons(builder);
        TIME = new Time(builder);
        PERF = new Perf(builder);
        WTHIT = new Wthit(builder);
        SPEC = builder.build();
    }

    private CalendarConfig() {}

    /**
     * The feature switches: {@code seasons}, {@code crop_seasons}, {@code time} (each section's {@code enabled}). True
     * before the file is loaded (the default); false for an unknown id.
     */
    public static boolean feature(String id) {
        ForgeConfigSpec.BooleanValue v = switch (id) {
            case "seasons" -> SEASONS.enabled;
            case "crop_seasons" -> CROP_SEASONS.enabled;
            case "time" -> TIME.enabled;
            default -> null;
        };
        if (v == null) return false;
        return !SPEC.isLoaded() || v.get();
    }


    /**
     * Season types for Serene Seasons ({@code seasons/SeasonTypes}): arid, frozen and tropical biomes by
     * tag, and rivers/beaches that take on the land around them. feature_writeup/weather.md.
     */
    public static final class Seasons {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue adaptiveRadius;
        public final ForgeConfigSpec.IntValue snowLineY;

        private Seasons(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Season types on top of Serene Seasons. The biome lists are the tags creativecalendar:seasons/",
                    "arid, frozen, tropical and adaptive (kubejs/generation_scripts/worldgen/season_types.json).",
                    "Arid: no rain all year. Frozen: snow all year. Tropical: wet and dry seasons, savannas included.",
                    "Adaptive (rivers, beaches): each column behaves like the nearest land biome. SeasonHUD shows",
                    "'Arid' / 'Frozen' with the day of the year. Read live on both sides."
            ).push("seasons");

            enabled = b.comment("false: Serene Seasons' own rules everywhere, the tags are ignored.")
                    .define("enabled", true);

            adaptiveRadius = b.comment(
                    "How far (blocks) a river or beach column looks for land to copy, in rings of 8 directions",
                    "at 8, 16, 24, 32, 48, 64, ... blocks. The first ring that reaches land decides by majority.",
                    "Nothing found: the river or beach keeps its own biome."
            ).defineInRange("adaptiveRadius", 64, 8, 256);

            snowLineY = b.comment(
                    "The snow line (default 320, the overworld build height). Below it height alone never turns rain",
                    "into snow (vanilla cools 0.05 per 40 blocks above y80): a column snows only if its biome snows at",
                    "y80 in the current sub-season. At and above it temperate and cold biomes always snow, so falling",
                    "snow tells a flyer they are above build height. Frozen and permanent-season biomes keep their own",
                    "rules. Also the ceiling for the snow_seasons lists in season_types.json."
            ).defineInRange("snowLineY", 320, -64, 4096);

            b.pop(); // seasons
        }
    }

    /**
     * Crop growing seasons by crop type ({@code seasons/CropSeasons}): fertile, neutral or off for the season of the
     * biome the crop stands in. feature_writeup/crops.md 2.12.1. Read live on both sides (the client for tooltips).
     */
    public static final class CropSeasons {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.DoubleValue fertileBonus;
        public final ForgeConfigSpec.DoubleValue offPenalty;
        public final ForgeConfigSpec.DoubleValue grassOffPenalty;
        public final ForgeConfigSpec.BooleanValue greenhouse;
        public final ForgeConfigSpec.IntValue caveFertileBelowY;
        public final ForgeConfigSpec.IntValue caveOffAboveY;

        private CropSeasons(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Every crop has a type (block tags creativecalendar:crop_seasons/<type>, filled from",
                    "kubejs/generation_scripts/worldgen/crop_seasons.json). Each season is fertile (grows faster),",
                    "neutral or off (grows slower) for that type, read from the biome's own calendar: spring to winter",
                    "in temperate and cold biomes, wet and dry in tropical, dry and arid ones. Replaces Serene Seasons'",
                    "in/out of season rule (its fertility.toml seasonal_crops is off). A crop in no type grows as usual."
            ).push("crop_seasons");

            enabled = b.comment("false: no crop seasons at all (crops grow at their own speed everywhere).")
                    .define("enabled", true);
            fertileBonus = b.comment(
                    "Fertile: this share of growth checks gets a second growth check right after (0.25 = 25 % faster)."
            ).defineInRange("fertileBonus", 0.25, 0.0, 1.0);
            offPenalty = b.comment("Off: this share of growth checks is refused (0.25 = 25 % slower).")
                    .defineInRange("offPenalty", 0.25, 0.0, 1.0);
            grassOffPenalty = b.comment(
                    "Grass spread (the grass type: vanilla, Immersive Weathering, Botania, Vinery slab, Vintage lush grass) in its off",
                    "season, winter: this share of spread tries is refused (0.5 = half speed)."
            ).defineInRange("grassOffPenalty", 0.5, 0.0, 1.0);
            greenhouse = b.comment(
                    "Greenhouse glass: any block in #sereneseasons:greenhouse_glass (filled from crop_seasons.json",
                    "greenhouse_glass: glass blocks, panes, doors and trapdoors) in the 16 blocks straight above a seasonal",
                    "crop makes every season neutral (no fertile bonus, no off penalty). Only that column counts."
            ).define("greenhouse", true);
            caveFertileBelowY = b.comment("Cave crops (cave carrots, glow berries) are fertile below this height.")
                    .defineInRange("caveFertileBelowY", 0, -64, 320);
            caveOffAboveY = b.comment("Cave crops are off above this height; neutral in between.")
                    .defineInRange("caveOffAboveY", 128, -64, 320);

            b.pop(); // crop_seasons
        }
    }

    /**
     * The one place day length, seasons and wine aging are set ({@code time/} package, feature_writeup/time.md).
     * Creative Calendar pushes the derived values into Better Days (getter mixin), Serene Seasons and SeasonHUD (their
     * per-world server configs); the Vinery port reads its wine values through {@code time.WineTime}. Their own
     * files stop being edited by hand. Every length is in world days of {@code dayMinutes + nightMinutes} real minutes.
     */
    public static final class Time {
        public enum Clock { SUN, TICKS }
        public enum WineClock { SUN, PLAYTIME }
        /** {@code time/TimePresets}: CUSTOM is only a label, shown when the values match neither preset. */
        public enum Preset { SINGLEPLAYER, SERVER, CUSTOM }

        public final ForgeConfigSpec.EnumValue<Preset> preset;
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue pauseWhenEmpty;
        public final ForgeConfigSpec.BooleanValue hudTimeOfDay;
        public final ForgeConfigSpec.DoubleValue dayMinutes;
        public final ForgeConfigSpec.DoubleValue nightMinutes;
        public final ForgeConfigSpec.DoubleValue dayStart;
        public final ForgeConfigSpec.DoubleValue nightStart;
        public final ForgeConfigSpec.EnumValue<Clock> seasonClock;
        public final ForgeConfigSpec.IntValue daysPerSubSeason;
        public final ForgeConfigSpec.IntValue startingSubSeason;
        public final ForgeConfigSpec.EnumValue<WineClock> wineClock;
        public final ForgeConfigSpec.IntValue monthsPerLevel;
        public final ForgeConfigSpec.IntValue maxLevel;
        public final ForgeConfigSpec.DoubleValue cellarMultiplier;
        public final ForgeConfigSpec.DoubleValue coldMultiplier;
        public final ForgeConfigSpec.DoubleValue warmMultiplier;

        private Time(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Day length, seasons and wine aging in one place (feature_writeup/time.md). While enabled, the",
                    "time values in betterdays-common.toml, the world's sereneseasons-server.toml and seasonhud-server.toml",
                    "are overwritten from here at server start and on every edit. The Vinery port reads [time.wine] and the",
                    "lengths from here and ignores the matching vinery-common.toml [wine]/[aging] keys while enabled.",
                    "/ccaltime shows every derived value."
            ).push("time");

            preset = b.comment(
                    "SINGLEPLAYER or SERVER sets every value below to that preset (they differ only in monthsPerLevel:",
                    "1 in single player, 3 on a server). Editing any value by hand turns this into CUSTOM, unless the",
                    "values then match a preset again. CUSTOM itself changes nothing."
            ).defineEnum("preset", Preset.SINGLEPLAYER);
            enabled = b.comment("Master switch. false: Better Days, Serene Seasons, SeasonHUD and Vinery use their own files again.")
                    .define("enabled", true);
            pauseWhenEmpty = b.comment(
                    "Dedicated server with nobody online: the sun stops, so the seasons and wine aging stop with it.",
                    "Game time, weather and random ticks keep running. Single player is unaffected."
            ).define("pauseWhenEmpty", true);
            hudTimeOfDay = b.comment("SeasonHUD shows the time of day after the season, \"Mid Spring (Day 3/24) 7:30 pm\" (client side).")
                    .define("hudTimeOfDay", true);

            b.push("day");
            dayMinutes = b.comment("Real minutes from dayStart to nightStart.").defineInRange("dayMinutes", 45.0, 1.0, 10000.0);
            nightMinutes = b.comment("Real minutes from nightStart to the next dayStart.").defineInRange("nightMinutes", 15.0, 1.0, 10000.0);
            dayStart = b.comment(
                    "dayTime of sunrise; the clock reads it as 6:00 am. Better Days reads it once, so a change needs a restart."
            ).defineInRange("dayStart", 22300.0, 0.0, 24000.0);
            nightStart = b.comment("dayTime of sunset. Needs a restart, like dayStart.").defineInRange("nightStart", 13000.0, 0.0, 24000.0);
            b.pop();

            b.push("seasons");
            seasonClock = b.comment(
                    "SUN: the seasons count sunrises; sleeping and /time move them as far as the sun.",
                    "TICKS: Serene Seasons' own counter (one world day of real ticks per day; sleeping skips ahead)."
            ).defineEnum("clock", Clock.SUN);
            daysPerSubSeason = b.comment("World days per sub-season (12 sub-seasons a year). Also the length of a wine month.")
                    .defineInRange("daysPerSubSeason", 8, 1, 1000);
            startingSubSeason = b.comment("Sub-season a new world starts in: 1 = Early Spring ... 12 = Late Winter, 0 = random.")
                    .defineInRange("startingSubSeason", 1, 0, 12);
            b.pop();

            b.push("wine");
            wineClock = b.comment(
                    "SUN: wine ages with the world days, sleeping and /time included. PLAYTIME: while a player is online."
            ).defineEnum("clock", WineClock.SUN);
            monthsPerLevel = b.comment("A wine month is one sub-season. Months per effect level (SINGLEPLAYER 1, SERVER 3 = one season).")
                    .defineInRange("monthsPerLevel", 1, 1, 100);
            maxLevel = b.comment("Highest effect level from aging.").defineInRange("maxLevel", 5, 0, 10);
            b.push("boosts");
            cellarMultiplier = b.comment("Aging speed in storage with no sky light.").defineInRange("cellarMultiplier", 2.0, 1.0, 5.0);
            coldMultiplier = b.comment("Aging speed in cold storage (Brewin' keg freeze sources outweigh heat sources nearby).",
                    "Multiplies with cellarMultiplier: a cold cellar ages at both.").defineInRange("coldMultiplier", 2.0, 1.0, 5.0);
            warmMultiplier = b.comment("Aging speed in warm storage (Brewin' keg heat sources outweigh freeze sources nearby).",
                    "Multiplies with cellarMultiplier; below 1.0 slows aging.").defineInRange("warmMultiplier", 0.75, 0.1, 1.0);
            b.pop();
            b.pop();

            b.pop(); // time
        }
    }

    /**
     * The Serene Seasons melt throttle, the one {@code [perf]} key that is Calendar's (it was in patcherMod's
     * {@code [perf]}; same name and comment). Read live.
     */
    public static final class Perf {
        public final ForgeConfigSpec.IntValue seasonsMeltIntervalTicks;

        private Perf(ForgeConfigSpec.Builder b) {
            b.comment("Performance throttles for other mods' per-tick work. See feature_writeup/perf.md section 7.")
                    .push("perf");

            seasonsMeltIntervalTicks = b.comment(
                    "Serene Seasons snow/ice melt: the per-chunk melt roll runs once every N ticks instead of every",
                    "tick, with its chance multiplied by N so the average melt rate is unchanged (a roll's chance",
                    "is capped at 1, so N above 4 melts slightly slower in summer). Walking every loaded chunk on",
                    "every tick was the cost. Cost: melting is a little lumpier. 1 = off."
            ).defineInRange("seasonsMeltIntervalTicks", 4, 1, 40);

            b.pop(); // perf
        }
    }

    /** The crop season WTHIT lines ({@code compat/WthitCropSeasons}); the rest of {@code [wthit]} stays patcherMod's. */
    public static final class Wthit {
        public final ForgeConfigSpec.BooleanValue cropSeasons;
        public final ForgeConfigSpec.BooleanValue cropOutOfSeason;

        private Wthit(ForgeConfigSpec.Builder b) {
            b.comment("WTHIT look-at overlay lines (client side). See feature_writeup/wthit_lines.md.").push("wthit");

            cropSeasons = b.comment(
                    "Every crop with a crop season type: its seasons as on the planting item ('Fertile: Spring',",
                    "'Offseason: Winter, Dry', 'Season: Nether', 'Fertile: Cold biomes'). [crop_seasons]."
            ).define("cropSeasons", true);

            cropOutOfSeason = b.comment(
                    "Under it, green 'Fertile here: grows faster' or red 'Offseason here: grows slower' for where the",
                    "crop stands (greenhouse glass counted). Nothing when it is neutral there."
            ).define("cropOutOfSeason", true);

            b.pop(); // wthit
        }
    }
}
