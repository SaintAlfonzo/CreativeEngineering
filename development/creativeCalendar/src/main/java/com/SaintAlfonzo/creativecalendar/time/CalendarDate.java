package com.SaintAlfonzo.creativecalendar.time;

import net.minecraft.world.level.Level;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;

/**
 * The calendar date on the SUN clock (feature_writeup/time.md 3.7). Serene Seasons keeps only the time within the
 * year, so the year number comes from the world's day count: the days since the first sunrise
 * ({@link TimeSettings#firstSunrise()}), less the day of the year, rounded up so a world that starts after Early Spring
 * reaches year 2 at its first Early Spring. A /season set moves the day but not the sun, so after one the year can read
 * one off until the next new year; /cetime date ({@link TimeDate}) moves both.
 */
public final class CalendarDate {
    private CalendarDate() {}

    public static long worldDay(Level level) {
        return Math.floorDiv(level.getDayTime() - TimeSettings.firstSunrise(), (long) TimeSettings.DAY_UNITS);
    }

    public static int year(Level level, ISeasonState state) {
        long years = -Math.floorDiv(state.getDay() - worldDay(level), (long) TimeSettings.yearDays()); // ceiling
        return (int) Math.max(1, years + 1);
    }

    public static Season.SubSeason next(ISeasonState state) {
        return Season.SubSeason.VALUES[(state.getSubSeason().ordinal() + 1) % Season.SubSeason.VALUES.length];
    }

    /** Days to the next sub-season (1 on its last day) and the real minutes until then, at the configured speeds. */
    public static int daysLeft(ISeasonState state) {
        int ticksLeft = state.getSubSeasonDuration() - state.getSeasonCycleTicks() % state.getSubSeasonDuration();
        return (int) Math.ceil(ticksLeft / (double) state.getDayDuration());
    }

    public static double minutesLeft(Level level, ISeasonState state) {
        int ticksLeft = state.getSubSeasonDuration() - state.getSeasonCycleTicks() % state.getSubSeasonDuration();
        return TimeSettings.sunSeasons() ? TimeSettings.realMinutes(level.getDayTime(), ticksLeft) : ticksLeft / 1200.0;
    }
}
