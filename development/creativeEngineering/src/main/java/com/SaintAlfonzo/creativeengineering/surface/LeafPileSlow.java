package com.SaintAlfonzo.creativeengineering.surface;

import java.util.function.IntSupplier;

/**
 * Layers an Immersive Weathering leaf pile needs before it slows walking, for the WTHIT Run Speed line
 * ({@link StuckInBlock}). The rule itself is Creative Compat's (mixin LeafPileSlowMixin, [leaf_piles] slowFromLayers in
 * creativecompat-common.toml), which sets its config reader here at construction. Unset: 4, IW's own rule (more than
 * 3 layers), which is what applies without Compat.
 */
public final class LeafPileSlow {

    public static final int DEFAULT = 4;

    private static volatile IntSupplier fromLayers = () -> DEFAULT;

    private LeafPileSlow() {}

    public static void set(IntSupplier slowFromLayers) {
        fromLayers = slowFromLayers;
    }

    public static int fromLayers() {
        return fromLayers.getAsInt();
    }
}
