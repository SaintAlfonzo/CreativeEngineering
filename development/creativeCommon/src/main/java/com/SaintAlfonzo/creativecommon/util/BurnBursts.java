package com.SaintAlfonzo.creativecommon.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * A shrapnel burst other jars can fire without knowing who draws it. Creative Compat's burn-explosion rule
 * (itemphysic.ItemBurnTransform, {@code shrapnel: true}) calls {@link #burst}; patcherMod's shrapnel side registers
 * shrapnel.ShrapnelSpawner here at construction. With no handler registered a burst does nothing.
 */
public final class BurnBursts {

    @FunctionalInterface
    public interface Handler {
        void burst(Level level, Vec3 center, @Nullable Entity owner);
    }

    private static volatile Handler handler = (level, center, owner) -> {};

    private BurnBursts() {}

    public static void register(Handler h) {
        handler = h;
    }

    public static void burst(Level level, Vec3 center, @Nullable Entity owner) {
        handler.burst(level, center, owner);
    }
}
