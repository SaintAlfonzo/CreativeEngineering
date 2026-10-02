package io.github.mortuusars.exposure.backport.util;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.minecraft.util.Mth;

/** Backport of 1.19.4+ {@code net.minecraft.util.ByIdMap}: int id -> value lookups for enums and the like. */
public final class ByIdMap {
    private ByIdMap() {
    }

    public enum OutOfBoundsStrategy {
        ZERO,
        WRAP,
        CLAMP
    }

    public static <T> IntFunction<T> continuous(ToIntFunction<T> keyExtractor, T[] values, OutOfBoundsStrategy outOfBoundsStrategy) {
        T[] sorted = sortedById(keyExtractor, values);
        int size = sorted.length;
        return switch (outOfBoundsStrategy) {
            case ZERO -> id -> id >= 0 && id < size ? sorted[id] : sorted[0];
            case WRAP -> id -> sorted[Mth.positiveModulo(id, size)];
            case CLAMP -> id -> sorted[Mth.clamp(id, 0, size - 1)];
        };
    }

    private static <T> T[] sortedById(ToIntFunction<T> keyExtractor, T[] values) {
        int size = values.length;
        if (size == 0) {
            throw new IllegalArgumentException("Empty value list");
        }
        T[] sorted = Arrays.copyOf(values, size);
        Arrays.fill(sorted, null);
        for (T value : values) {
            int id = keyExtractor.applyAsInt(value);
            if (id < 0 || id >= size) {
                throw new IllegalArgumentException("Values are not continous, found index " + id + " for value " + value);
            }
            if (sorted[id] != null) {
                throw new IllegalArgumentException("Duplicate entry on id " + id + ": current=" + value + ", previous=" + sorted[id]);
            }
            sorted[id] = value;
        }
        return sorted;
    }
}
