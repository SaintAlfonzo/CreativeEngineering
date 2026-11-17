package com.SaintAlfonzo.cdevcapture;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * File channel between a host tool (qrun.py client, run_captures.py) and the driver (client_capture.md 2.11).
 * Polled once per client tick; nothing here blocks.
 *
 * <pre>
 * &lt;dir&gt;/in/&lt;seq&gt;.json    request, written by the host as .tmp then renamed:
 *                        {"seq": 7, "op": "run", "id": "CQ-PTH-01", "reset": false, "timeout_ticks": 2400,
 *                         "steps": [{"cmd": "..."}, {"walk": 40, "keys": "forward"}, ...]}
 *                        op "ping" (reply only) and "stop" (reply, then the client quits) take no steps
 * &lt;dir&gt;/out/&lt;seq&gt;.json   reply, written by the driver as .tmp then renamed:
 *                        {"seq": 7, "id": ..., "status": "ok"|"fail", "error": ..., "files": [...],
 *                         "reads": [...], "steps": [...], "ticks": n}
 * &lt;dir&gt;/state.json      heartbeat every 20 ticks: {"phase", "current", "seq", "tick", "time"}
 * &lt;dir&gt;/raw/            frames of capture steps (the plan's raw_dir in channel mode)
 * </pre>
 * Requests run one at a time in seq order; a request file is deleted when the driver takes it.
 */
final class Channel {

    private static final Gson GSON = new Gson();

    final Path dir, in, out;

    Channel(Path dir) {
        this.dir = dir;
        this.in = dir.resolve("in");
        this.out = dir.resolve("out");
        try {
            Files.createDirectories(in);
            Files.createDirectories(out);
        } catch (IOException e) {
            CaptureDriver.LOG.error("[cdevcapture] channel {} not usable", dir, e);
        }
    }

    /** The lowest-seq request waiting in {@code in/}, removed from the folder; null when there is none. */
    JsonObject poll() {
        List<Path> files = new ArrayList<>();
        try (Stream<Path> s = Files.list(in)) {
            s.filter(p -> p.getFileName().toString().endsWith(".json")).forEach(files::add);
        } catch (IOException e) {
            return null;
        }
        if (files.isEmpty()) return null;
        files.sort(Comparator.comparingLong(Channel::seqOf));
        Path p = files.get(0);
        try {
            JsonObject o = JsonParser.parseString(Files.readString(p, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!o.has("seq")) o.addProperty("seq", seqOf(p));
            return o;
        } catch (Exception e) {
            JsonObject bad = new JsonObject();
            bad.addProperty("seq", seqOf(p));
            bad.addProperty("op", "bad");
            bad.addProperty("error", "unreadable request: " + e);
            return bad;
        } finally {
            try {
                Files.deleteIfExists(p);
            } catch (IOException ignored) {
                // a request left behind would run twice; logged by the next poll's failure if it persists
            }
        }
    }

    void reply(long seq, JsonObject r) {
        r.addProperty("seq", seq);
        write(out.resolve(seq + ".json"), r);
    }

    void heartbeat(String phase, String current, long seq, long tick) {
        JsonObject o = new JsonObject();
        o.addProperty("phase", phase);
        o.addProperty("current", current);
        o.addProperty("seq", seq);
        o.addProperty("tick", tick);
        o.addProperty("time", System.currentTimeMillis());
        write(dir.resolve("state.json"), o);
    }

    private static void write(Path target, JsonObject o) {
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            Files.writeString(tmp, GSON.toJson(o), StandardCharsets.UTF_8);
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            CaptureDriver.LOG.error("[cdevcapture] channel write {} failed", target, e);
        }
    }

    private static long seqOf(Path p) {
        String n = p.getFileName().toString();
        try {
            return Long.parseLong(n.substring(0, n.length() - 5));
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }
}
