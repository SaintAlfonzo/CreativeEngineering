package com.SaintAlfonzo.creativecalendar.time;

import com.SaintAlfonzo.creativecalendar.CalendarConfig;

/**
 * The values every clock derives from {@code [time]} (feature_writeup/time.md 3.2). Everything here reads the toml
 * live, so a change in Create's Configs screen shows at once.
 */
public final class TimeSettings {
    public static final int DAY_UNITS = 24000;

    private TimeSettings() {}

    /** The master is on and the toml is loaded (Better Days asks before configs load on some paths). */
    public static boolean active() {
        return CalendarConfig.SPEC.isLoaded() && CalendarConfig.TIME.enabled.get();
    }

    public static double dayStart() {
        return CalendarConfig.TIME.dayStart.get();
    }

    public static double nightStart() {
        return CalendarConfig.TIME.nightStart.get();
    }

    /** dayTime units from sunrise to sunset (the pack's 22300 -> 13000 is 14,700). */
    public static double dayUnits() {
        double units = Math.floorMod((long) (nightStart() - dayStart()), DAY_UNITS);
        return units == 0 ? DAY_UNITS / 2.0 : units;
    }

    /** Better Days RATIO speeds: dayTime units per tick. */
    public static double daySpeed() {
        return dayUnits() / (CalendarConfig.TIME.dayMinutes.get() * 1200);
    }

    public static double nightSpeed() {
        return (DAY_UNITS - dayUnits()) / (CalendarConfig.TIME.nightMinutes.get() * 1200);
    }

    public static double worldDayMinutes() {
        return CalendarConfig.TIME.dayMinutes.get() + CalendarConfig.TIME.nightMinutes.get();
    }

    public static boolean sunSeasons() {
        return active() && CalendarConfig.TIME.seasonClock.get() == CalendarConfig.Time.Clock.SUN;
    }

    /** Serene Seasons' day_duration: one dayTime day (SUN) or one world day of real ticks (TICKS). */
    public static int seasonDayDuration() {
        return sunSeasons() ? DAY_UNITS : (int) Math.round(worldDayMinutes() * 1200);
    }

    public static int daysPerSubSeason() {
        return CalendarConfig.TIME.daysPerSubSeason.get();
    }

    public static int yearDays() {
        return daysPerSubSeason() * 12;
    }

    // ---------------------------------------------------------------- clock text

    /**
     * "6:00 am" at sunrise, "8:42 pm" at the pack's sunset (T12): 1000 dayTime units are one hour, counted from
     * dayStart = 6:00 am. Only the pack's displays use this; Better Days' bed clock and /time query keep vanilla's.
     */
    public static String clock(long dayTime) {
        int minutesOfDay = minutesOfDay(dayTime);
        int hour = minutesOfDay / 60;
        int minute = minutesOfDay % 60;
        int hour12 = hour % 12 == 0 ? 12 : hour % 12;
        return String.format("%d:%02d %s", hour12, minute, hour < 12 ? "am" : "pm");
    }

    /** The same clock in 24 h form, "19:30" (Xaero's minimap time line). */
    public static String clock24(long dayTime) {
        int minutesOfDay = minutesOfDay(dayTime);
        return String.format("%02d:%02d", minutesOfDay / 60, minutesOfDay % 60);
    }

    private static int minutesOfDay(long dayTime) {
        double start = active() ? dayStart() : 0;
        long units = Math.floorMod(dayTime - (long) start, (long) DAY_UNITS);
        return (int) ((units * 60 / 1000 + 6 * 60) % (24 * 60));
    }

    /**
     * Day number since the world began, 1 at dayTime 0, stepping at the pack's sunrise like SeasonHUD's "Day x/24"
     * rather than at vanilla's 6000-before-midnight. The sunrise counted from is the one nearest dayTime 0 (the pack's
     * 22300 is -1700), so the number matches vanilla's for the hours both call daytime.
     */
    public static long dayNumber(long dayTime) {
        return Math.floorDiv(dayTime - firstSunrise(), (long) DAY_UNITS) + 1;
    }

    /** The sunrise nearest dayTime 0 (the pack's 22300 is -1700): day 1 of the world and of the calendar starts here. */
    public static long firstSunrise() {
        long start = active() ? (long) dayStart() : 0;
        return Math.floorMod(start + DAY_UNITS / 4, (long) DAY_UNITS) - DAY_UNITS / 4; // into [-6000, 18000)
    }

    /** Real minutes as "5 h 20 min" / "45 min". */
    public static String realTime(double minutes) {
        long total = Math.max(0, Math.round(minutes));
        if (total >= 60) {
            return (total / 60) + " h " + (total % 60) + " min";
        }
        return total + " min";
    }

    /** Real minutes a stretch of dayTime units takes, at the configured day and night speeds (no sleeping). */
    public static double realMinutes(long fromDayTime, long units) {
        double ticks = 0;
        long t = fromDayTime;
        long left = units;
        double start = dayStart();
        while (left > 0) {
            long ofDay = Math.floorMod(t - (long) start, (long) DAY_UNITS); // 0 = sunrise
            boolean day = ofDay < dayUnits();
            long toSwitch = day ? (long) dayUnits() - ofDay : DAY_UNITS - ofDay;
            long step = Math.min(left, Math.max(1, toSwitch));
            ticks += step / (day ? daySpeed() : nightSpeed());
            t += step;
            left -= step;
        }
        return ticks / 1200.0;
    }
}
