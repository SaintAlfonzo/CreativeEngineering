package com.SaintAlfonzo.creativecalendar.time;

import com.SaintAlfonzo.creativecalendar.CalendarConfig;

/**
 * Public read-only view of {@code [time]} for the Vinery port's vintage clock (feature_writeup/time.md 3.7). Creative
 * Brewing requires Creative Calendar, so the port calls this directly and uses it instead of its own [wine]/[aging]
 * lengths and boosts while {@link #active()}. Calendar never writes the port's config. Every getter reads the toml live.
 *
 * The class name and getters are the contract with the Vinery port's PackTime.
 */
public final class WineTime {
	private WineTime() {}

	/** The [time] master is on and loaded: the port takes the values below. */
	public static boolean active() {
		return TimeSettings.active();
	}

	/** true: wine ages on the sun (the port's WORLD_TIME clock, DAYTIME days). false: while a player is online. */
	public static boolean sunClock() {
		return CalendarConfig.TIME.wineClock.get() == CalendarConfig.Time.WineClock.SUN;
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
		return CalendarConfig.TIME.monthsPerLevel.get();
	}

	public static int maxLevel() {
		return CalendarConfig.TIME.maxLevel.get();
	}

	public static double cellarMultiplier() {
		return CalendarConfig.TIME.cellarMultiplier.get();
	}

	public static double coldMultiplier() {
		return CalendarConfig.TIME.coldMultiplier.get();
	}

	public static double warmMultiplier() {
		return CalendarConfig.TIME.warmMultiplier.get();
	}
}
