package com.SaintAlfonzo.creativeengineering.time;

import com.SaintAlfonzo.creativeengineering.CEConfig;

/**
 * The values every clock derives from {@code [time]} (feature_writeup/time.md 3.2). Everything here reads the toml
 * live, so a change in Create's Configs screen shows at once.
 */
public final class TimeSettings {
    public static final int DAY_UNITS = 24000;

    private TimeSettings() {}

    /** The master is on and the toml is loaded (Better Days asks before configs load on some paths). */
    public static boolean active() {
        return CEConfig.COMMON_SPEC.isLoaded() && CEConfig.TIME.enabled.get();
    }

    public static double dayStart() {
        return CEConfig.TIME.dayStart.get();
    }

    public static double nightStart() {
        return CEConfig.TIME.nightStart.get();
    }

    /** dayTime units from sunrise to sunset (the pack's 22300 -> 13000 is 14,700). */
    public static double dayUnits() {
        double units = Math.floorMod((long) (nightStart() - dayStart()), DAY_UNITS);
        return units == 0 ? DAY_UNITS / 2.0 : units;
    }

    /** Better Days RATIO speeds: dayTime units per tick. */
    public static double daySpeed() {
        return dayUnits() / (CEConfig.TIME.dayMinutes.get() * 1200);
    }

    public static double nightSpeed() {
        return (DAY_UNITS - dayUnits()) / (CEConfig.TIME.nightMinutes.get() * 1200);
    }

    public static double worldDayMinutes() {
        return CEConfig.TIME.dayMinutes.get() + CEConfig.TIME.nightMinutes.get();
    }

    public static boolean sunSeasons() {
        return active() && CEConfig.TIME.seasonClock.get() == CEConfig.Time.Clock.SUN;
    }

    /** Serene Seasons' day_duration: one dayTime day (SUN) or one world day of real ticks (TICKS). */
    public static int seasonDayDuration() {
        return sunSeasons() ? DAY_UNITS : (int) Math.round(worldDayMinutes() * 1200);
    }

    public static int daysPerSubSeason() {
        return CEConfig.TIME.daysPerSubSeason.get();
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
        double start = active() ? dayStart() : 0;
        long units = Math.floorMod(dayTime - (long) start, (long) DAY_UNITS);
        int minutesOfDay = (int) ((units * 60 / 1000 + 6 * 60) % (24 * 60));
        int hour = minutesOfDay / 60;
        int minute = minutesOfDay % 60;
        int hour12 = hour % 12 == 0 ? 12 : hour % 12;
        return String.format("%d:%02d %s", hour12, minute, hour < 12 ? "am" : "pm");
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
