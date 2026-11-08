package com.SaintAlfonzo.creativecompat.compat;

import java.util.Set;

/**
 * Per-dimension Distant Horizons settings (perf.md 19). DH 3.0.2 has only global ones; the pack narrows them here.
 * No DH classes are referenced, so common code may read this without DH installed.
 *
 * The Nether: no distant generation (mixin.DhNoGenerationMixin), LODs drawn to {@link #NETHER_LOD_RADIUS} chunks
 * instead of the config's lodChunkRenderDistanceRadius, with DH's far fog reaching full thickness at that edge
 * (compat.DhDimensionView, client). Nether terrain under the roof is mostly hidden by fog anyway, and its generation
 * (basalt deltas especially) was a large part of DH's world-gen thread time on 2026-11-07.
 */
public final class DhDimensions {

    public static final Set<String> NO_GENERATION = Set.of("minecraft:the_nether");

    public static final String NETHER = "minecraft:the_nether";
    public static final int NETHER_LOD_RADIUS = 32;

    private DhDimensions() {}
}
