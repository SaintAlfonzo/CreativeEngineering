package com.SaintAlfonzo.creativeengineering.campfire;

/**
 * Per-campfire count of consecutive ticks in rain, added to {@code CampfireBlockEntity} by
 * Creative Compat's {@code mixin/CampfireRainMixin}. Not saved: a reload restarts the count, as Unlit Campfire's own did.
 */
public interface CampfireRainTicks {

    int ce$getRainTicks();

    void ce$setRainTicks(int ticks);
}
