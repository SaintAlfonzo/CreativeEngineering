package com.SaintAlfonzo.creativeengineering.util;

import net.minecraft.util.RandomSource;

import java.util.function.Supplier;

/**
 * The random a new Level gets ({@code LevelThreadSafeRandomMixin}): the thread-safe one, unless a debug factory is
 * registered. Creative Devtools' {@code LevelRandomProbe} registers one that returns the logging probe when
 * {@code [debug.level_random] probe} is on (feature_writeup/creative_devtools.md 1). A factory that returns null
 * falls back to the thread-safe random. This class never references Devtools.
 */
public final class LevelRandomHook {

    private static volatile Supplier<RandomSource> factory;

    private LevelRandomHook() {}

    public static void register(Supplier<RandomSource> f) {
        factory = f;
    }

    public static RandomSource create() {
        Supplier<RandomSource> f = factory;
        RandomSource r = f == null ? null : f.get();
        return r != null ? r : RandomSource.createThreadSafe();
    }
}
