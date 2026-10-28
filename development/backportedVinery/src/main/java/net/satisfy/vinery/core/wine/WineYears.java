package net.satisfy.vinery.core.wine;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.satisfy.vinery.forge.VineryConfig;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Locale;

/**
 * Wine age on the {@link VintageClock} (feature_writeup/alcohol.md B4.5), keeping upstream's method names.
 * <p>
 * A wine stack carries {@code WineYear {brewedAt, storedSince, storedTicks, bonusTicks, storageRate, storageKind}}, all in clock
 * ticks (upstream's WINE_YEAR component in days). Unlike upstream, the day / year lengths and level settings are read
 * from config when needed rather than copied into each stack, so changing the scale re-scales every wine at once.
 */
public class WineYears {
    public static final String TAG = "WineYear";
    private static final String LABEL = "label.vinery_backport.storage.";
    /**
     * storageKind bits: no sky light (cellar, otherwise sunny) and a cold or warm spot (Brewin' and Chewin's keg
     * temperature, otherwise neither).
     */
    public static final int CELLAR = 1;
    public static final int COLD = 2;
    public static final int WARM = 4;
    private static final MethodHandle BNC_CLIMATE = bncClimate();

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

    private static CompoundTag fresh(long brewedAt) {
        CompoundTag data = new CompoundTag();
        data.putLong("brewedAt", brewedAt);
        data.putLong("storedSince", -1);
        data.putLong("storedTicks", 0);
        data.putLong("bonusTicks", 0);
        data.putInt("storageRate", 100);
        data.putInt("storageKind", 0);
        return data;
    }

    /** Clock ticks of age, with the cellar bonus. */
    public static long getWineAgeTicks(ItemStack wine, Level level) {
        if (level == null || !isAgingEnabled()) {
            return 0;
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
        return ticks * (ratePercent - 100) / 100; // negative below x1.0 (warm); the age itself is clamped at 0
    }

    public static int getWineAgeDays(ItemStack wine, Level level) {
        return (int) (getWineAgeTicks(wine, level) / VintageClock.of(level).ticksPerDay());
    }

    public static int getWineAgeYears(ItemStack wine, Level level) {
        return (int) (getWineAgeTicks(wine, level) / VintageClock.of(level).ticksPerYear());
    }

    /** CELLAR | COLD / WARM bits for a storage block at pos. */
    public static int storageKind(Level level, BlockPos pos) {
        int kind = level.getBrightness(LightLayer.SKY, pos) > 0 ? 0 : CELLAR;
        int climate = climate(level, pos);
        return climate < 0 ? kind | COLD : climate > 0 ? kind | WARM : kind;
    }

    /** Aging speed in percent for a storage kind: the cellar, cold and warm factors multiply (sunny is x1.0). */
    public static int storageRate(Level level, int kind) {
        VintageClock.Snapshot clock = VintageClock.of(level);
        double rate = 100;
        if ((kind & CELLAR) != 0) {
            rate *= clock.cellarPercent() / 100.0;
        }
        if ((kind & COLD) != 0) {
            rate *= clock.coldPercent() / 100.0;
        }
        if ((kind & WARM) != 0) {
            rate *= clock.warmPercent() / 100.0;
        }
        return (int) Math.round(rate);
    }

    public static int storageRate(Level level, BlockPos pos) {
        return storageRate(level, storageKind(level, pos));
    }

    /** "cold cellar", "warm sunny", "sunny"...: the temperature word (if any), then the light word. */
    public static MutableComponent storageLabel(int kind) {
        MutableComponent light = Component.translatable(LABEL + ((kind & CELLAR) != 0 ? "cellar" : "sunny"));
        String climate = (kind & COLD) != 0 ? "cold" : (kind & WARM) != 0 ? "warm" : null;
        return climate == null ? light : Component.translatable(LABEL + "climate_light", Component.translatable(LABEL + climate), light);
    }

    /** A rate in percent as a factor: 100 -> "1.0", 75 -> "0.75", 400 -> "4.0". */
    public static String formatRate(int percent) {
        String text = String.format(Locale.ROOT, "%.2f", percent / 100.0);
        return text.endsWith("0") ? text.substring(0, text.length() - 1) : text;
    }

    /**
     * Cold and warm come from the Brewin' and Chewin' port's keg temperature (BnCHeatSources.climate: its heat and
     * freeze source tags, biome and dimension), looked up by reflection so Vinery runs without it.
     */
    private static int climate(Level level, BlockPos pos) {
        if (BNC_CLIMATE == null) {
            return 0;
        }
        try {
            return (int) BNC_CLIMATE.invokeExact(level, pos);
        } catch (Throwable e) {
            return 0;
        }
    }

    private static @Nullable MethodHandle bncClimate() {
        try {
            Class<?> heat = Class.forName("umpaz.brewinandchewin.common.utility.BnCHeatSources");
            return MethodHandles.publicLookup().findStatic(heat, "climate", MethodType.methodType(int.class, Level.class, BlockPos.class));
        } catch (ClassNotFoundException e) {
            return null; // Brewin' not installed
        } catch (NoSuchMethodException | IllegalAccessException e) {
            // both ports ship together: a mismatch is a broken build, not a supported setup
            throw new IllegalStateException("the Brewin' and Chewin' port has no public BnCHeatSources.climate(Level, BlockPos)", e);
        }
    }

    /** Tooltip line on the blocks wine ages in. */
    public static void addStorageHint(List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.vinery_backport.storage_hint").withStyle(ChatFormatting.GRAY));
    }

    public static int getEffectLevel(ItemStack wine, Level level) {
        if (!hasWineYear(wine)) {
            return 0;
        }
        VintageClock.Snapshot clock = VintageClock.of(level);
        int computed = getWineAgeYears(wine, level) / clock.yearsPerEffectLevel();
        return Math.max(0, Math.min(clock.maxLevel(), computed));
    }

    public static int getEffectDuration(ItemStack wine, Level level, int baseDuration) {
        if (!hasWineYear(wine)) {
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
        wine.getOrCreateTag().put(TAG, fresh((long) (day * clock.ticksPerDay())));
    }

    public static void startStorage(ItemStack wine, Level level, BlockPos pos) {
        if (!hasWineYear(wine)) {
            setWineYear(wine, level);
        }
        CompoundTag data = data(wine);
        if (data != null && data.getLong("storedSince") < 0) {
            int kind = storageKind(level, pos);
            data.putLong("storedSince", VintageClock.of(level).ticks());
            data.putInt("storageRate", storageRate(level, kind));
            data.putInt("storageKind", kind);
        }
    }

    public static void stopStorage(ItemStack wine, Level level) {
        CompoundTag data = data(wine);
        if (data != null && data.getLong("storedSince") >= 0) {
            long stored = Math.max(0, VintageClock.of(level).ticks() - data.getLong("storedSince"));
            data.putLong("bonusTicks", data.getLong("bonusTicks") + bonusTicks(stored, data.getInt("storageRate")));
            data.putLong("storedTicks", data.getLong("storedTicks") + stored);
            data.putLong("storedSince", -1);
            data.putInt("storageRate", 100);
            data.putInt("storageKind", 0);
        }
    }

    public static void setWineAgeDays(ItemStack wine, Level level, int days) {
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
        if (!hasWineYear(wine) || getEffectLevel(wine, level) >= clock.maxLevel()) {
            return -1;
        }
        double ticksPerLevel = clock.ticksPerYear() * clock.yearsPerEffectLevel();
        double remaining = (getEffectLevel(wine, level) + 1) * ticksPerLevel - getWineAgeTicks(wine, level);
        CompoundTag data = data(wine);
        int rate = data != null && data.getLong("storedSince") >= 0 ? Math.max(1, data.getInt("storageRate")) : 100;
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

    /** storageKind of a stored wine; wines stored before cold storage existed only had the cellar bonus. */
    public static int currentStorageKind(ItemStack wine) {
        CompoundTag data = data(wine);
        if (data == null) {
            return 0;
        }
        return data.contains("storageKind", 3) ? data.getInt("storageKind") : data.getInt("storageRate") > 100 ? CELLAR : 0;
    }
}
