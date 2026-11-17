package net.satisfy.vinery.forge;

import com.SaintAlfonzo.creativecalendar.time.DateMoves;
import com.SaintAlfonzo.creativecalendar.time.WineTime;
import net.satisfy.vinery.core.wine.VintageClock;

/**
 * The Creative Engineering pack's {@code [time]} master (feature_writeup/time.md 3.7), held by Creative Calendar (mod id
 * creativecalendar, feature_writeup/creative_calendar.md): while its master is on, the vintage clock takes the wine
 * clock, the lengths and the boosts from there ({@code WineTime}) instead of this mod's own [wine]/[aging] keys. With the
 * master off, {@link VineryConfig} is used as it is. Creative Brewing, which nests this port, requires Creative Calendar
 * (mandatory dependency), so the class is always there; no older-API branch: the calls below are the whole contract.
 */
public final class PackTime {
    private PackTime() {
    }

    /**
     * Registers the vintage clock with Calendar's {@code /ccaltime date} (plan_rules_fixes.md 12f step 3): a backward
     * date move on the sun clock moves the clock back by the same amount (forward moves reach it on its next tick).
     */
    static void registerDateMoves() {
        DateMoves.register((server, delta) -> {
            VintageClock.add(server, delta);
            return "wine clock moved back";
        });
    }

    public static boolean active() {
        return WineTime.active();
    }

    // Only call the getters below after active() returned true.

    static boolean sunClock() {
        return WineTime.sunClock();
    }

    static double worldDayMinutes() {
        return WineTime.worldDayMinutes();
    }

    static int daysPerMonth() {
        return WineTime.daysPerMonth();
    }

    static int monthsPerLevel() {
        return WineTime.monthsPerLevel();
    }

    static int maxLevel() {
        return WineTime.maxLevel();
    }

    static double cellarMultiplier() {
        return WineTime.cellarMultiplier();
    }

    static double coldMultiplier() {
        return WineTime.coldMultiplier();
    }

    static double warmMultiplier() {
        return WineTime.warmMultiplier();
    }
}
