package net.satisfy.vinery.core.wine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.satisfy.vinery.forge.VineryConfig;
import org.jetbrains.annotations.Nullable;

/**
 * Wine age on the {@link VintageClock} (feature_writeup/alcohol.md B4.5), keeping upstream's method names.
 * <p>
 * A wine stack carries {@code WineYear {brewedAt, storedSince, storedTicks, bonusTicks, storageRate}}, all in clock
 * ticks (upstream's WINE_YEAR component in days). Unlike upstream, the day / year lengths and level settings are read
 * from config when needed rather than copied into each stack, so changing the scale re-scales every wine at once.
 * 1.3.10 wines ({@code Year} = world-day year) convert once, keeping their age in years (W8).
 */
public class WineYears {
    public static final String TAG = "WineYear";
    private static final String LEGACY_YEAR = "Year";

    public static boolean isAgingEnabled() {
        return VineryConfig.AGING_ENABLED.get();
    }

    /** Current aging day on the clock. */
    public static int getDays(Level level) {
        VintageClock.Snapshot clock = VintageClock.of(level);
        return (int) (clock.ticks() / clock.ticksPerDay());
    }

    public static int getYear(Level level, int daysPerYear) {
        return getDays(level) / Math.max(1, daysPerYear);
    }

    public static @Nullable CompoundTag data(ItemStack wine) {
        CompoundTag tag = wine.getTag();
        return tag != null && tag.contains(TAG, 10) ? tag.getCompound(TAG) : null;
    }

    public static boolean hasWineYear(ItemStack wine) {
        return data(wine) != null;
    }

    public static boolean hasLegacyYear(ItemStack wine) {
        CompoundTag tag = wine.getTag();
        return tag != null && !tag.contains(TAG, 10) && tag.contains(LEGACY_YEAR, 3);
    }

    /** W8: a 1.3.10 wine gets a brew time that keeps its age in years. Server side; the client shows the same estimate. */
    public static void convertLegacy(ItemStack wine, Level level) {
        if (!hasLegacyYear(wine) || level == null) {
            return;
        }
        CompoundTag tag = wine.getOrCreateTag();
        int legacyYearLength = Math.max(1, VineryConfig.LEGACY_YEAR_LENGTH_DAYS.get());
        long legacyNow = level.getDayTime() / 24000L / legacyYearLength;
        long ageYears = Math.max(0, legacyNow - tag.getInt(LEGACY_YEAR));
        VintageClock.Snapshot clock = VintageClock.of(level);
        long ageTicks = (long) (ageYears * clock.ticksPerYear());
        tag.remove(LEGACY_YEAR);
        CompoundTag data = fresh(clock.ticks() - ageTicks);
        tag.put(TAG, data);
    }

    private static CompoundTag fresh(long brewedAt) {
        CompoundTag data = new CompoundTag();
        data.putLong("brewedAt", brewedAt);
        data.putLong("storedSince", -1);
        data.putLong("storedTicks", 0);
        data.putLong("bonusTicks", 0);
        data.putInt("storageRate", 100);
        return data;
    }

    /** Clock ticks of age, with the cellar bonus. */
    public static long getWineAgeTicks(ItemStack wine, Level level) {
        if (level == null || !isAgingEnabled()) {
            return 0;
        }
        if (hasLegacyYear(wine)) {
            ItemStack copy = wine.copy();
            convertLegacy(copy, level);
            return getWineAgeTicks(copy, level);
        }
        CompoundTag data = data(wine);
        if (data == null) {
            return 0;
        }
        VintageClock.Snapshot clock = VintageClock.of(level);
        long now = clock.ticks();
        long storedSince = data.getLong("storedSince");
        long running = storedSince >= 0 ? Math.max(0, now - storedSince) : 0;
        long bonus = data.getLong("bonusTicks") + bonusTicks(running, data.getInt("storageRate"));
        if (clock.ageOnlyInStorage()) {
            return Math.max(0, data.getLong("storedTicks") + running + bonus);
        }
        return Math.max(0, now - data.getLong("brewedAt") + bonus);
    }

    private static long bonusTicks(long ticks, int ratePercent) {
        return Math.max(0, ticks * (ratePercent - 100) / 100);
    }

    public static int getWineAgeDays(ItemStack wine, Level level) {
        return (int) (getWineAgeTicks(wine, level) / VintageClock.of(level).ticksPerDay());
    }

    public static int getWineAgeYears(ItemStack wine, Level level) {
        return (int) (getWineAgeTicks(wine, level) / VintageClock.of(level).ticksPerYear());
    }

    public static int storageRate(Level level, BlockPos pos) {
        if (level.getBrightness(LightLayer.SKY, pos) > 0) {
            return 100;
        }
        return VintageClock.of(level).cellarPercent();
    }

    public static int getEffectLevel(ItemStack wine, Level level) {
        if (!hasWineYear(wine) && !hasLegacyYear(wine)) {
            return 0;
        }
        VintageClock.Snapshot clock = VintageClock.of(level);
        int computed = getWineAgeYears(wine, level) / clock.yearsPerEffectLevel();
        return Math.max(0, Math.min(clock.maxLevel(), computed));
    }

    public static int getEffectDuration(ItemStack wine, Level level, int baseDuration) {
        if (!hasWineYear(wine) && !hasLegacyYear(wine)) {
            return baseDuration;
        }
        VintageClock.Snapshot clock = VintageClock.of(level);
        long duration = (long) baseDuration + (long) clock.durationPerYear() * getWineAgeYears(wine, level);
        int clamped = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, duration));
        return Math.max(baseDuration, Math.min(clock.maxDuration(), clamped));
    }

    /** A freshly brewed wine: brewedAt is the start of the current aging day, so one day's wines stack. */
    public static void setWineYear(ItemStack wine, Level level) {
        VintageClock.Snapshot clock = VintageClock.of(level);
        long day = (long) (clock.ticks() / clock.ticksPerDay());
        wine.getOrCreateTag().remove(LEGACY_YEAR);
        wine.getOrCreateTag().put(TAG, fresh((long) (day * clock.ticksPerDay())));
    }

    public static void startStorage(ItemStack wine, Level level, BlockPos pos) {
        convertLegacy(wine, level);
        if (!hasWineYear(wine)) {
            setWineYear(wine, level);
        }
        CompoundTag data = data(wine);
        if (data != null && data.getLong("storedSince") < 0) {
            data.putLong("storedSince", VintageClock.of(level).ticks());
            data.putInt("storageRate", storageRate(level, pos));
        }
    }

    public static void stopStorage(ItemStack wine, Level level) {
        convertLegacy(wine, level);
        CompoundTag data = data(wine);
        if (data != null && data.getLong("storedSince") >= 0) {
            long stored = Math.max(0, VintageClock.of(level).ticks() - data.getLong("storedSince"));
            data.putLong("bonusTicks", data.getLong("bonusTicks") + bonusTicks(stored, data.getInt("storageRate")));
            data.putLong("storedTicks", data.getLong("storedTicks") + stored);
            data.putLong("storedSince", -1);
            data.putInt("storageRate", 100);
        }
    }

    public static void setWineAgeDays(ItemStack wine, Level level, int days) {
        convertLegacy(wine, level);
        if (!hasWineYear(wine)) {
            setWineYear(wine, level);
        }
        CompoundTag data = data(wine);
        long target = (long) (Math.max(0, days) * VintageClock.of(level).ticksPerDay());
        long delta = target - getWineAgeTicks(wine, level);
        data.putLong("bonusTicks", data.getLong("bonusTicks") + delta);
    }

    /** Clock ticks until the next effect level (at the current storage rate), -1 when fully aged. */
    public static long getTicksUntilNextLevel(ItemStack wine, Level level) {
        VintageClock.Snapshot clock = VintageClock.of(level);
        if (!hasWineYear(wine) && !hasLegacyYear(wine) || getEffectLevel(wine, level) >= clock.maxLevel()) {
            return -1;
        }
        double ticksPerLevel = clock.ticksPerYear() * clock.yearsPerEffectLevel();
        double remaining = (getEffectLevel(wine, level) + 1) * ticksPerLevel - getWineAgeTicks(wine, level);
        CompoundTag data = data(wine);
        int rate = data != null && data.getLong("storedSince") >= 0 ? Math.max(100, data.getInt("storageRate")) : 100;
        return Math.max(1, (long) Math.ceil(remaining * 100 / rate));
    }

    /** Aging days until the next effect level, -1 when fully aged. */
    public static int getDaysUntilNextLevel(ItemStack wine, Level level) {
        long ticks = getTicksUntilNextLevel(wine, level);
        return ticks < 0 ? -1 : (int) Math.max(1, Math.ceil(ticks / VintageClock.of(level).ticksPerDay()));
    }

    public static boolean isStored(ItemStack wine) {
        CompoundTag data = data(wine);
        return data != null && data.getLong("storedSince") >= 0;
    }

    public static int currentStorageRate(ItemStack wine) {
        CompoundTag data = data(wine);
        return data == null ? 100 : data.getInt("storageRate");
    }
}
