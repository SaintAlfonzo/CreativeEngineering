package com.SaintAlfonzo.creativeworldgen.forgivingvoid;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Query surface {@code mixin/ForgivingVoidMixin} reads at each void-fall bounce.
 *
 * <p>A pure read-through layer over {@link CWGConfig#FORGIVING_VOID} - Forge's config spec
 * is the actual source of truth (live-reloadable, editable in-game), so nothing here caches
 * or needs to be pushed into from KubeJS.
 */
public final class ForgivingVoidBridge {

    private ForgivingVoidBridge() {}

    public static boolean isEnabled() {
        return CWGConfig.FORGIVING_VOID.enabled.get();
    }

    public static boolean isPullToward() {
        return CWGConfig.FORGIVING_VOID.direction.get() == CWGConfig.PullDirection.TOWARD;
    }

    /** Uniform in [min, max]; returns the constant directly when min == max (no randomness). */
    public static double randomDistance() {
        double min = CWGConfig.FORGIVING_VOID.minDistance.get();
        double max = CWGConfig.FORGIVING_VOID.maxDistance.get();
        if (max <= min) return min;
        return min + ThreadLocalRandom.current().nextDouble() * (max - min);
    }

    /** The anchor for this dimension: its override if enabled, else the default anchor. */
    public static double[] anchorFor(String dimensionId) {
        CWGConfig.ForgivingVoid cfg = CWGConfig.FORGIVING_VOID;
        if ("minecraft:overworld".equals(dimensionId) && cfg.overworldOverride.get()) {
            return new double[]{cfg.overworldAnchorX.get(), cfg.overworldAnchorZ.get()};
        }
        if ("minecraft:the_nether".equals(dimensionId) && cfg.netherOverride.get()) {
            return new double[]{cfg.netherAnchorX.get(), cfg.netherAnchorZ.get()};
        }
        if ("minecraft:the_end".equals(dimensionId) && cfg.endOverride.get()) {
            return new double[]{cfg.endAnchorX.get(), cfg.endAnchorZ.get()};
        }
        return new double[]{cfg.defaultAnchorX.get(), cfg.defaultAnchorZ.get()};
    }

    /** A uniformly random unit vector, for the away-direction fallback when a player lands on the anchor. */
    public static double[] randomUnitVector() {
        double theta = ThreadLocalRandom.current().nextDouble() * 2.0 * Math.PI;
        return new double[]{Math.cos(theta), Math.sin(theta)};
    }

    /** One-line summary for the startup log. */
    public static String summary() {
        CWGConfig.ForgivingVoid cfg = CWGConfig.FORGIVING_VOID;
        return String.format(
                "enabled=%s direction=%s range=[%.1f,%.1f] defaultAnchor=(%.1f,%.1f) overrides={overworld=%s,nether=%s,end=%s}",
                cfg.enabled.get(), cfg.direction.get(), cfg.minDistance.get(), cfg.maxDistance.get(),
                cfg.defaultAnchorX.get(), cfg.defaultAnchorZ.get(),
                cfg.overworldOverride.get(), cfg.netherOverride.get(), cfg.endOverride.get());
    }
}
