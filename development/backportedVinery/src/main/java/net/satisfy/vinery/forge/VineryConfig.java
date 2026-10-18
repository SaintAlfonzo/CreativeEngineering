package net.satisfy.vinery.forge;

import net.minecraftforge.common.ForgeConfigSpec;
import net.satisfy.vinery.core.util.InfoOverlayMode;
import net.satisfy.vinery.core.wine.WineEffects;

import java.util.ArrayList;
import java.util.List;

/**
 * Port: upstream's Cloth AutoConfig file as {@code vinery-common.toml} (ForgeConfigSpec), with 1.5.5's key names.
 * Additions (feature_writeup/alcohol.md): {@code [aging]} is the playtime vintage clock (B4.5) that replaces 1.5.5's
 * game-time days, and {@code [winemaker_set]} the set bonus settings (B4.7). Defaults are the pack's values.
 */
public final class VineryConfig {
    public static final ForgeConfigSpec SPEC;

    // [blocks]
    public static final ForgeConfigSpec.IntValue TOTAL_FERMENTATION_TIME;
    public static final ForgeConfigSpec.IntValue MAX_FLUID_LEVEL;
    public static final ForgeConfigSpec.IntValue MAX_FLUID_INCREASE;
    public static final ForgeConfigSpec.IntValue APPLE_PRESS_MASHING_TIME;
    public static final ForgeConfigSpec.IntValue APPLE_PRESS_FERMENTATION_TIME;
    public static final ForgeConfigSpec.DoubleValue CHERRY_GROWTH_CHANCE;
    public static final ForgeConfigSpec.DoubleValue APPLE_GROWTH_CHANCE;
    public static final ForgeConfigSpec.DoubleValue ROTTEN_CHERRY_CHANCE;
    public static final ForgeConfigSpec.DoubleValue GRAPE_GROWTH_CHANCE;
    public static final ForgeConfigSpec.DoubleValue GRAPE_GROWTH_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue GRAPEVINE_POT_STOMPS;
    public static final ForgeConfigSpec.IntValue GRAPEVINE_POT_HEAVY_ARMOR_BONUS;
    public static final ForgeConfigSpec.BooleanValue GRAPEVINE_POT_SPLASH_PARTICLES;
    public static final ForgeConfigSpec.BooleanValue GRAPEVINE_LEAVES_ENABLED;

    // [wine]
    public static final ForgeConfigSpec.BooleanValue AGING_ENABLED;
    public static final ForgeConfigSpec.BooleanValue AGE_ONLY_IN_STORAGE;
    public static final ForgeConfigSpec.IntValue MAX_DURATION;
    public static final ForgeConfigSpec.IntValue MAX_LEVEL;
    public static final ForgeConfigSpec.IntValue DURATION_PER_YEAR;
    public static final ForgeConfigSpec.DoubleValue CELLAR_AGING_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue DAYS_PER_YEAR;
    public static final ForgeConfigSpec.IntValue YEARS_PER_EFFECT_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EFFECTS;

    // [aging] (vintage clock, B4.5)
    public static final ForgeConfigSpec.EnumValue<ClockMode> CLOCK_MODE;
    public static final ForgeConfigSpec.DoubleValue PLAYTIME_HOURS_PER_YEAR;
    public static final ForgeConfigSpec.EnumValue<DayUnit> DAY_UNIT;
    public static final ForgeConfigSpec.DoubleValue WORLD_DAY_MINUTES;
    public static final ForgeConfigSpec.IntValue LEGACY_YEAR_LENGTH_DAYS;

    // [banner]
    public static final ForgeConfigSpec.BooleanValue BANNER_GIVE_EFFECT;
    public static final ForgeConfigSpec.BooleanValue BANNER_SHOW_TOOLTIP;

    // [trader]
    public static final ForgeConfigSpec.DoubleValue TRADER_SPAWN_CHANCE;
    public static final ForgeConfigSpec.BooleanValue TRADER_SPAWN_WITH_MULES;
    public static final ForgeConfigSpec.IntValue TRADER_SPAWN_DELAY;

    // [villager]
    public static final List<ForgeConfigSpec.ConfigValue<List<? extends String>>> VILLAGER_TRADES = new ArrayList<>();

    // [winemaker_set] (B4.7)
    public static final ForgeConfigSpec.BooleanValue SET_ENABLED;
    public static final ForgeConfigSpec.DoubleValue SET_KEEP_CHANCE;
    public static final ForgeConfigSpec.EnumValue<DamageMode> SET_DAMAGE_MODE;
    public static final ForgeConfigSpec.DoubleValue SET_DAMAGE_CHANCE;
    public static final ForgeConfigSpec.IntValue SET_DAMAGE_AMOUNT;

    // [infoOverlay]: upstream's HUD isn't ported (WTHIT shows the same lines, alcohol.md B4.6); kept for the API
    public static final ForgeConfigSpec.EnumValue<InfoOverlayMode> INFO_OVERLAY_MODE;

    public enum ClockMode {
        /** Ticks while at least one player is online, in any dimension. */
        PLAYTIME,
        /** Every server tick, like 1.5.5. */
        SERVER_TICKS,
        /** Follows the overworld day time, like 1.3.10 (sleeping and /time move it). */
        WORLD_TIME
    }

    public enum DayUnit {
        /** A day is playtimeHoursPerYear / daysPerYear real hours. */
        AGING,
        /** A day is one world day (worldDayMinutes, or measured when 0). */
        WORLD
    }

    public enum DamageMode {
        NONE, ALL_PIECES, ONE_PIECE
    }

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("blocks");
        TOTAL_FERMENTATION_TIME = b.comment("Ticks an Aging Barrel takes for one wine").defineInRange("totalFermentationTime", 6000, 1, 10000);
        MAX_FLUID_LEVEL = b.comment("Juice units an Aging Barrel holds").defineInRange("maxFluidLevel", 100, 100, 10000);
        MAX_FLUID_INCREASE = b.comment("Juice units one juice bottle adds").defineInRange("maxFluidIncrease", 25, 1, 10000);
        APPLE_PRESS_MASHING_TIME = b.defineInRange("applePressMashingTime", 600, 1, 10000);
        APPLE_PRESS_FERMENTATION_TIME = b.defineInRange("applePressFermentationTime", 800, 1, 10000);
        CHERRY_GROWTH_CHANCE = b.defineInRange("cherryGrowthChance", 0.4, 0, 1);
        APPLE_GROWTH_CHANCE = b.defineInRange("appleGrowthChance", 0.4, 0, 1);
        ROTTEN_CHERRY_CHANCE = b.comment("Port (D10): chance that picking cherries also drops a rotten cherry, as in 1.3.10. 0 = never (1.5.5)")
                .defineInRange("rottenCherryChance", 0.15, 0, 1);
        GRAPE_GROWTH_CHANCE = b.defineInRange("grapeGrowthChance", 0.5, 0, 1);
        GRAPE_GROWTH_MULTIPLIER = b.comment("Multiplies the growth chance of grape bushes, stems and lattices").defineInRange("grapeGrowthMultiplier", 1.0, 0, 10);
        GRAPEVINE_POT_STOMPS = b.comment("Stomps a full Grapevine Pot needs").defineInRange("grapevinePotStomps", 12, 1, 100);
        GRAPEVINE_POT_HEAVY_ARMOR_BONUS = b.comment("Extra stomp progress in percent for an entity with 15 or more armor").defineInRange("grapevinePotHeavyArmorBonus", 10, 0, 100);
        GRAPEVINE_POT_SPLASH_PARTICLES = b.define("grapevinePotSplashParticles", true);
        GRAPEVINE_LEAVES_ENABLED = b.comment("Red and white grapevine stems grow grapevine leaves around them").define("grapevineLeavesEnabled", true);
        b.pop();

        b.push("wine");
        AGING_ENABLED = b.comment("Wines age at all").define("agingEnabled", true);
        AGE_ONLY_IN_STORAGE = b.comment("Wines only age while they sit in a storage block (rack, shelf, placed bottle...)").define("ageOnlyInStorage", false);
        MAX_DURATION = b.comment("Longest effect a wine can reach, in ticks").defineInRange("maxDuration", 15000, 1, 100000);
        MAX_LEVEL = b.comment("Highest effect level from aging").defineInRange("maxLevel", 5, 0, 10);
        DURATION_PER_YEAR = b.comment("Effect ticks a wine gains per year of age").defineInRange("durationPerYear", 200, 0, 10000);
        CELLAR_AGING_MULTIPLIER = b.comment("Aging speed in a storage block with no sky light").defineInRange("cellarAgingMultiplier", 1.5, 1, 5);
        DAYS_PER_YEAR = b.comment("Aging days per wine year (1.3.10 pack value: 16)").defineInRange("daysPerYear", 16, 1, 100);
        YEARS_PER_EFFECT_LEVEL = b.comment("Wine years per effect level (1.3.10 pack value: 2)").defineInRange("yearsPerEffectLevel", 2, 1, 100);
        EFFECTS = b.comment("wine|effect|amplifier|durationTicks per wine (1.5.5 defaults plus the two 1.3.10 wines the port keeps)")
                .defineList("effects", defaultEffects(), o -> o instanceof String);
        b.pop();

        b.push("aging");
        CLOCK_MODE = b.comment("What moves the vintage clock wines age on: PLAYTIME (a player is online), SERVER_TICKS (always, 1.5.5), WORLD_TIME (overworld day time, 1.3.10)")
                .defineEnum("clockMode", ClockMode.PLAYTIME);
        PLAYTIME_HOURS_PER_YEAR = b.comment("Real hours of clock time per wine year (dayUnit AGING). 28.2 = the pack's 1.3.10 pace: 16 Better Days days of about 106 min")
                .defineInRange("playtimeHoursPerYear", 28.2, 0.01, 10000);
        DAY_UNIT = b.comment("AGING: a day is playtimeHoursPerYear / daysPerYear. WORLD: a day is one world day as players see it (worldDayMinutes)")
                .defineEnum("dayUnit", DayUnit.AGING);
        WORLD_DAY_MINUTES = b.comment("dayUnit WORLD: real minutes per world day. 0 = measure it from the overworld day time (follows Better Days)")
                .defineInRange("worldDayMinutes", 0.0, 0, 100000);
        LEGACY_YEAR_LENGTH_DAYS = b.comment("1.3.10 wines (Year NBT): the year_length_in_days the old jar used, to convert their age once (pack: 16)")
                .defineInRange("legacyYearLengthDays", 16, 1, 1000);
        b.pop();

        b.push("banner");
        BANNER_GIVE_EFFECT = b.comment("The Vinery banner gives Regeneration II to players within 8 blocks").define("giveEffect", true);
        BANNER_SHOW_TOOLTIP = b.define("showTooltip", true);
        b.pop();

        b.push("trader");
        TRADER_SPAWN_CHANCE = b.comment("Chance that a Wandering Trader spawn is a Wandering Winemaker. The pack spawns the winemaker itself (patcherMod WinemakerSpawner): 0")
                .defineInRange("spawnChance", 0.0, 0, 1);
        TRADER_SPAWN_WITH_MULES = b.define("spawnWithMules", true);
        TRADER_SPAWN_DELAY = b.defineInRange("spawnDelay", 48000, 0, 72000);
        b.pop();

        b.push("villager");
        b.comment("Winemaker villager trades per level: item|BUY or SELL|price|count|maxUses|experience");
        VILLAGER_TRADES.add(b.defineList("level1", List.of(
                "vinery:red_grape|BUY|15|1|4|5", "vinery:white_grape|BUY|15|1|4|5",
                "vinery:red_grape_seeds|SELL|2|1|1|5", "vinery:white_grape_seeds|SELL|2|1|1|5"), o -> o instanceof String));
        VILLAGER_TRADES.add(b.defineList("level2", List.of(
                "vinery:wine_bottle|SELL|1|1|4|7", "vinery:cherry|BUY|12|1|4|6", "vinery:apple_mash|SELL|1|1|4|6"), o -> o instanceof String));
        VILLAGER_TRADES.add(b.defineList("level3", List.of(
                "vinery:white_grape_bag|SELL|7|1|2|10", "vinery:red_grape_bag|SELL|7|1|2|10",
                "vinery:cherry_bag|SELL|7|1|2|10", "vinery:apple_bag|SELL|7|1|2|10"), o -> o instanceof String));
        VILLAGER_TRADES.add(b.defineList("level4", List.of(
                "vinery:window|SELL|12|1|2|10", "vinery:cherry_beam|SELL|6|4|2|10", "vinery:grapevine_pot|SELL|6|1|2|10",
                "vinery:taiga_grape_seeds_red|SELL|2|1|2|5", "vinery:taiga_grape_seeds_white|SELL|2|1|2|5"), o -> o instanceof String));
        VILLAGER_TRADES.add(b.defineList("level5", List.of(
                "vinery:wine_box|SELL|10|1|2|10", "vinery:lilitu_wine|SELL|4|1|2|10",
                "vinery:vinemaker_apron|SELL|18|1|1|15", "vinery:straw_hat|SELL|12|1|1|12"), o -> o instanceof String));
        b.pop();

        b.push("winemaker_set");
        SET_ENABLED = b.comment("Full winemaker set: bone meal isn't used up").define("enabled", true);
        SET_KEEP_CHANCE = b.comment("Chance the bone meal is kept").defineInRange("keepChance", 1.0, 0, 1);
        SET_DAMAGE_MODE = b.comment("NONE (no damage), ALL_PIECES (1.3.10 / 1.5.5), ONE_PIECE (one random piece)").defineEnum("damageMode", DamageMode.ALL_PIECES);
        SET_DAMAGE_CHANCE = b.comment("Chance a use deals damage").defineInRange("damageChance", 0.30, 0, 1);
        SET_DAMAGE_AMOUNT = b.comment("Damage per piece per damaging use (1.5.5 hardcodes 2, the pack used 1)").defineInRange("damageAmount", 1, 0, 100);
        b.pop();

        b.push("infoOverlay");
        INFO_OVERLAY_MODE = b.comment("Not used by the port: the block info lines are in WTHIT").defineEnum("mode", InfoOverlayMode.OFF);
        b.pop();

        SPEC = b.build();
    }

    private static List<String> defaultEffects() {
        List<String> effects = new ArrayList<>(WineEffects.DEFAULTS);
        effects.add("vinery:praetorian_wine|minecraft:glowing|0|1800");
        effects.add("vinery:king_danis_wine|minecraft:instant_health|0|1");
        return effects;
    }

    private VineryConfig() {
    }
}
