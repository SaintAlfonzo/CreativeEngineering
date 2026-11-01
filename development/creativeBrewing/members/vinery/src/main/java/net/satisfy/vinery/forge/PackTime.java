package net.satisfy.vinery.forge;

import com.SaintAlfonzo.creativeengineering.time.WineTime;
import net.minecraftforge.fml.ModList;

/**
 * The Creative Engineering pack's {@code [time]} master (feature_writeup/time.md 3.7), a soft dependency: when
 * patcherMod (mod id creativeengineering) is loaded and its master is on, the vintage clock takes the wine clock,
 * the lengths and the boosts from there ({@code WineTime}) instead of this mod's own [wine]/[aging] keys. Without
 * patcherMod, or with the master off, {@link VineryConfig} is used as it is. patcherMod ships with this jar, so
 * there is no older-API branch: the calls below are the whole contract.
 */
public final class PackTime {
    private static final boolean LOADED = ModList.get().isLoaded("creativeengineering");

    private PackTime() {
    }

    public static boolean active() {
        return LOADED && WineTime.active();
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
