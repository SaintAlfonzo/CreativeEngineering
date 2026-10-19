package com.SaintAlfonzo.creativeengineering.time;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * {@code [time] pauseWhenEmpty} (feature_writeup/time.md 3.4): on a dedicated server with nobody online the sun
 * stops (vanilla's dayTime step and Better Days' tick), so SUN seasons and SUN wine stop with it. No gamerule is
 * touched, so a crash can't leave doDaylightCycle off. Game time keeps running: scheduled ticks depend on it.
 */
public final class TimePause {
    private TimePause() {}

    public static boolean paused(@Nullable MinecraftServer server) {
        return server != null && server.isDedicatedServer() && server.getPlayerCount() == 0
                && TimeSettings.active() && CEConfig.TIME.pauseWhenEmpty.get();
    }
}
