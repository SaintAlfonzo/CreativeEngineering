package io.github.mortuusars.exposure.backport.client;

import net.minecraft.client.Minecraft;

/**
 * Backport of 1.21 {@code net.minecraft.client.DeltaTracker} (frame timing handed to GUI/HUD renderers). 1.19.2
 * passes the partial tick around as a float; {@link #of} wraps one.
 */
public interface DeltaTracker {
    float getGameTimeDeltaTicks();

    float getGameTimeDeltaPartialTick(boolean runsNormally);

    float getRealtimeDeltaTicks();

    static DeltaTracker of(float partialTick) {
        return new DeltaTracker() {
            @Override
            public float getGameTimeDeltaTicks() {
                return Minecraft.getInstance().getDeltaFrameTime();
            }

            @Override
            public float getGameTimeDeltaPartialTick(boolean runsNormally) {
                return runsNormally || !Minecraft.getInstance().isPaused() ? partialTick : Minecraft.getInstance().getFrameTime();
            }

            @Override
            public float getRealtimeDeltaTicks() {
                return Minecraft.getInstance().getDeltaFrameTime();
            }
        };
    }
}
