package com.SaintAlfonzo.creativecompat.compat.jei.world;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

/**
 * Rows for the world-interaction tabs that are known at runtime instead of from a JSON file: KubeJS startup scripts
 * call {@link #add} (rows as a JSON string, the same format as the files; see {@link WorldInteractionJEI}), and Java
 * code registers a {@link #provider} that is asked when JEI starts, so config and tags are read late.
 *
 * <p>Plain JSON only, no JEI or client classes: KubeJS startup scripts also run on a dedicated server.
 */
public final class WorldRows {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, List<JsonObject>> ADDED = new LinkedHashMap<>();
    private static final Map<String, List<Supplier<List<JsonObject>>>> PROVIDERS = new LinkedHashMap<>();

    private WorldRows() {}

    /** One row object or an array of them, as JSON text. Called from KubeJS: {@code add('sample_syringe', JSON.stringify(rows))}. */
    public static synchronized void add(String tab, String json) {
        if (WorldTab.byId(tab) == null) {
            LOGGER.warn("[jei_world] add: unknown tab '{}'", tab);
            return;
        }
        try {
            JsonElement parsed = JsonParser.parseString(json);
            List<JsonObject> rows = ADDED.computeIfAbsent(tab, k -> new ArrayList<>());
            if (parsed.isJsonArray()) {
                for (JsonElement row : (JsonArray) parsed) {
                    if (row.isJsonObject()) rows.add(row.getAsJsonObject());
                }
            } else if (parsed.isJsonObject()) {
                rows.add(parsed.getAsJsonObject());
            }
        } catch (RuntimeException e) {
            LOGGER.warn("[jei_world] add: bad JSON for tab '{}': {}", tab, e.toString());
        }
    }

    /** Clears a tab's {@link #add}ed rows, so a startup script reload doesn't double them. */
    public static synchronized void clear(String tab) {
        ADDED.remove(tab);
    }

    public static synchronized void provider(WorldTab tab, Supplier<List<JsonObject>> provider) {
        PROVIDERS.computeIfAbsent(tab.id, k -> new ArrayList<>()).add(provider);
    }

    /** Every runtime row for a tab: {@link #add}ed ones, then each provider's. A failing provider is logged and skipped. */
    static synchronized List<JsonObject> rows(WorldTab tab) {
        List<JsonObject> out = new ArrayList<>(ADDED.getOrDefault(tab.id, List.of()));
        for (Supplier<List<JsonObject>> provider : PROVIDERS.getOrDefault(tab.id, List.of())) {
            try {
                out.addAll(provider.get());
            } catch (RuntimeException e) {
                LOGGER.warn("[jei_world] provider for '{}' failed: {}", tab.id, e.toString());
            }
        }
        return out;
    }
}
