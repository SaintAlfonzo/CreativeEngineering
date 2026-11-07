package com.SaintAlfonzo.cdevperfbot;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * One CSV per JVM start, {@code <out>/ticks-<epoch ms>.csv}, read by tools/perf/suite/ticklog.py:
 * <pre>
 *   t,&lt;tick&gt;,&lt;tick duration in microseconds&gt;
 *   s,&lt;epoch ms&gt;,&lt;tick&gt;,&lt;players&gt;,&lt;entities&gt;,&lt;loaded chunks&gt;,&lt;heap used MB&gt;   every 20 ticks
 *   m,&lt;epoch ms&gt;,&lt;tick&gt;,&lt;label&gt;                                        markers
 * </pre>
 * The file is flushed with every status line, so its modification time is the server thread's heartbeat: the suite
 * treats a file that stops changing while the JVM lives as a hung server.
 */
final class TickLog {

    private final Path out;
    private BufferedWriter w;
    private MinecraftServer server;
    private long tickStart;

    TickLog(Path out) {
        this.out = out;
    }

    @SubscribeEvent
    public void onStarting(ServerStartingEvent event) {
        server = event.getServer();
        try {
            Files.createDirectories(out);
            w = Files.newBufferedWriter(out.resolve("ticks-" + System.currentTimeMillis() + ".csv"),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new UncheckedIOException("cdevperfbot: can't open the tick log in " + out, e);
        }
        mark("server_starting");
    }

    @SubscribeEvent
    public void onStopping(ServerStoppingEvent event) {
        mark("server_stopping");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTickStart(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) tickStart = System.nanoTime();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTickEnd(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || w == null || tickStart == 0) return;
        long us = (System.nanoTime() - tickStart) / 1000;
        int tick = server.getTickCount();
        try {
            w.write("t," + tick + "," + us + "\n");
            if (tick % 20 == 0) {
                w.write(status(tick));
                w.flush();
            }
        } catch (IOException ignored) {
            // disk full or closed: the suite sees the stale file and records it
        }
    }

    private String status(int tick) {
        int entities = 0, chunks = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (var ignored : level.getAllEntities()) entities++;
            chunks += level.getChunkSource().getLoadedChunksCount();
        }
        Runtime rt = Runtime.getRuntime();
        long heapMb = (rt.totalMemory() - rt.freeMemory()) >> 20;
        return "s," + System.currentTimeMillis() + "," + tick + "," + server.getPlayerCount() + "," + entities + ","
                + chunks + "," + heapMb + "\n";
    }

    synchronized void mark(String label) {
        if (w == null) return;
        int tick = server == null ? -1 : server.getTickCount();
        try {
            w.write("m," + System.currentTimeMillis() + "," + tick + "," + label.replace('\n', ' ') + "\n");
            w.flush();
        } catch (IOException ignored) {
        }
    }

    String fileHint() {
        return out.toString();
    }
}
