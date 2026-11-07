package com.SaintAlfonzo.cdevcapture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The plan run_captures.py writes ({@code capture_plan.json} in its game dir). Everything is already resolved
 * on the host: macros inlined, {@code $X $Y $Z} replaced by the test's plot origin, {@code item_dict:} keys
 * replaced by ids. The driver only executes it.
 *
 * <pre>
 * {"world": {"name": "flat_creative", "biome": "minecraft:plains", "seed": 1},
 *  "raw_dir": ".../raw", "results": ".../results.jsonl", "progress": ".../progress.json",
 *  "step_timeout_ticks": 600, "test_timeout_ticks": 2400,
 *  "setup": [steps run once after joining],
 *  "tests": [{"id": "FX-43", "origin": [0, -60, 0], "steps": [...], "capture": [...]}]}
 * </pre>
 */
public record CapturePlan(String worldName, String worldBiome, long worldSeed, Path rawDir, Path results,
                          Path progress, int stepTimeoutTicks, int testTimeoutTicks, List<JsonObject> setup,
                          List<Test> tests) {

    public record Test(String id, int[] origin, List<JsonObject> steps, List<JsonObject> capture) {}

    public static CapturePlan load(Path file) {
        JsonObject o;
        try {
            o = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            throw new UncheckedIOException("cdevcapture: can't read plan " + file, e);
        }
        JsonObject world = o.getAsJsonObject("world");
        List<Test> tests = new ArrayList<>();
        for (JsonElement e : o.getAsJsonArray("tests")) {
            JsonObject t = e.getAsJsonObject();
            JsonArray org = t.getAsJsonArray("origin");
            tests.add(new Test(t.get("id").getAsString(),
                    new int[]{org.get(0).getAsInt(), org.get(1).getAsInt(), org.get(2).getAsInt()},
                    objects(t.getAsJsonArray("steps")), objects(t.getAsJsonArray("capture"))));
        }
        return new CapturePlan(world.get("name").getAsString(),
                world.has("biome") ? world.get("biome").getAsString() : "minecraft:plains",
                world.has("seed") ? world.get("seed").getAsLong() : 1L,
                Path.of(o.get("raw_dir").getAsString()), Path.of(o.get("results").getAsString()),
                Path.of(o.get("progress").getAsString()),
                o.has("step_timeout_ticks") ? o.get("step_timeout_ticks").getAsInt() : 600,
                o.has("test_timeout_ticks") ? o.get("test_timeout_ticks").getAsInt() : 2400,
                objects(o.getAsJsonArray("setup")), tests);
    }

    private static List<JsonObject> objects(JsonArray a) {
        List<JsonObject> out = new ArrayList<>();
        if (a != null) a.forEach(e -> out.add(e.getAsJsonObject()));
        return out;
    }
}
