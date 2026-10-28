package com.SaintAlfonzo.creativeengineering.time;

import com.SaintAlfonzo.creativeengineering.CEConfig;

/**
 * Public read-only view of {@code [time]} for the Vinery port's vintage clock (feature_writeup/time.md 3.7). The port
 * calls this when patcherMod is loaded (soft dependency) and uses it instead of its own [wine]/[aging] lengths and
 * boosts while {@link #active()}. patcherMod never writes the port's config. Every getter reads the toml live.
 */
public final class WineTime {
    private WineTime() {}

    /** The [time] master is on and loaded: the port takes the values below. */
    public static boolean active() {
        return TimeSettings.active();
    }

    /** true: wine ages on the sun (the port's WORLD_TIME clock, DAYTIME days). false: while a player is online. */
    public static boolean sunClock() {
        return CEConfig.TIME.wineClock.get() == CEConfig.Time.WineClock.SUN;
    }

    /** Real minutes per world day (dayMinutes + nightMinutes). */
    public static double worldDayMinutes() {
        return TimeSettings.worldDayMinutes();
    }

    /** A wine month is one sub-season. */
    public static int daysPerMonth() {
        return TimeSettings.daysPerSubSeason();
    }

    public static int monthsPerLevel() {
        return CEConfig.TIME.monthsPerLevel.get();
    }

    public static int maxLevel() {
        return CEConfig.TIME.maxLevel.get();
    }

    public static double cellarMultiplier() {
        return CEConfig.TIME.cellarMultiplier.get();
    }

    public static double coldMultiplier() {
        return CEConfig.TIME.coldMultiplier.get();
    }

    public static double warmMultiplier() {
        return CEConfig.TIME.warmMultiplier.get();
    }
}
