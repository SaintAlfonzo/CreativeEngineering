package com.SaintAlfonzo.cdevperfbot;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.nio.file.Path;

/**
 * CE Perf Bot: a dev-only helper for the scratch-server performance suite (feature_writeup/perf_suite.md).
 * tools/perf/suite/perf_suite.py copies this jar into its scratch server and passes {@code -Dcdevperfbot.out=<dir>};
 * without that property, or on a client, the mod does nothing.
 * <ul>
 *   <li>{@link TickLog}: every tick's duration, a status line per second and the suite's markers, flushed each
 *       second, so a killed server loses at most one second.</li>
 *   <li>{@link Bots}: bot players on a dummy connection that the server treats as real players (chunk loading,
 *       random ticks, mob spawning, player-gated scripts).</li>
 *   <li>{@link BotCommands}: {@code /pbot}.</li>
 * </ul>
 */
@Mod(DevPerfBot.ID)
public class DevPerfBot {

    public static final String ID = "cdevperfbot";

    public DevPerfBot() {
        String out = System.getProperty("cdevperfbot.out");
        if (FMLEnvironment.dist != Dist.DEDICATED_SERVER || out == null || out.isBlank()) return;
        TickLog log = new TickLog(Path.of(out));
        Bots bots = new Bots();
        MinecraftForge.EVENT_BUS.register(log);
        MinecraftForge.EVENT_BUS.register(bots);
        MinecraftForge.EVENT_BUS.register(new BotCommands(log, bots, Path.of(out)));
    }
}
