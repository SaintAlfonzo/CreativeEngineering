package com.sammy.minersdelight.upstream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sammy.minersdelight.MinersDelightMod;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * The in-memory view of the upstream jar (feature_writeup/miners_delight.md P3): its assets as the client pack, its data
 * as the server pack, translated to what the port registers on 1.19.2. Nothing here is shipped in our jar; every change
 * to upstream data goes through this class, never through a file at an upstream path.
 */
public final class MinersTranslator {
    /** Upstream ids the port does not register (D3: the hot cocoa cup; later drops from MND-40 go here too). */
    public static final Set<String> REMOVED = Set.of("hot_cocoa_cup");
    /** Cup conversion rows whose bowl does not exist on 1.19.2 (FD 1.19.2-1.2.4 has no onion soup; D9 adds its own row). */
    private static final Set<String> MISSING_BOWLS = Set.of("farmersdelight:onion_soup");
    /** 1.20 renamed the loot condition {@code alternative} to {@code any_of}; 1.19.2 only knows the old name. */
    private static final String ANY_OF = "\"minecraft:any_of\"";
    private static final String ALTERNATIVE = "\"minecraft:alternative\"";
    /** Per-file data edits for later pack decisions, keyed by the translated path. */
    private static final Map<String, UnaryOperator<JsonObject>> PATCH_DATA = Map.of();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static Map<String, byte[]> assets;
    private static Map<String, byte[]> data;

    private MinersTranslator() {
    }

    public static synchronized Map<String, byte[]> assets() {
        if (assets == null) {
            assets = new HashMap<>();
            UpstreamJars.miners145().forEach((path, bytes) -> {
                if (!path.startsWith("assets/") || namesRemoved(path)) {
                    return;
                }
                if (path.contains("/lang/")) {
                    bytes = withoutRemovedLangKeys(bytes);
                }
                assets.put(path, bytes);
            });
            MinersDelightMod.LOGGER.info("Miner's Delight upstream assets: {} files", assets.size());
        }
        return assets;
    }

    public static synchronized Map<String, byte[]> data() {
        if (data == null) {
            data = new HashMap<>();
            UpstreamJars.miners145().forEach((path, bytes) -> {
                if (!path.startsWith("data/")) {
                    return;
                }
                // Ten cooking recipes (and their advancements) sit under a stray minersdelight namespace upstream.
                if (path.startsWith("data/minersdelight/")) {
                    path = "data/miners_delight/" + path.substring("data/minersdelight/".length());
                }
                if (namesRemoved(path)) {
                    return;
                }
                byte[] translated = translateData(path, bytes);
                if (translated != null) {
                    data.put(path, translated);
                }
            });
            dropDanglingLootModifiers(data);
            MinersDelightMod.LOGGER.info("Miner's Delight upstream data: {} files", data.size());
        }
        return data;
    }

    /**
     * Upstream's global list names five modifiers it never ships (cooked bat wing, squid, tentacles, arthropods and raw
     * arthropods): Forge would log a parse error for each. Keep only the listed modifiers that have a file.
     */
    private static void dropDanglingLootModifiers(Map<String, byte[]> files) {
        String listPath = "data/forge/loot_modifiers/global_loot_modifiers.json";
        byte[] bytes = files.get(listPath);
        if (bytes == null) {
            return;
        }
        JsonObject list = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonArray kept = new JsonArray();
        for (JsonElement entry : list.getAsJsonArray("entries")) {
            String[] id = entry.getAsString().split(":", 2);
            if (files.containsKey("data/" + id[0] + "/loot_modifiers/" + id[1] + ".json")) {
                kept.add(entry);
            }
        }
        list.add("entries", kept);
        files.put(listPath, GSON.toJson(list).getBytes(StandardCharsets.UTF_8));
    }

    /** Null drops the file. */
    private static byte[] translateData(String path, byte[] bytes) {
        if (!path.endsWith(".json")) {
            return bytes;
        }
        String text = new String(bytes, StandardCharsets.UTF_8);
        if (mentionsRemoved(text) && !path.contains("/tags/") && !path.contains("/cup_conversion/")) {
            return null;
        }
        if (path.contains("/loot_modifiers/") || path.contains("/loot_tables/")) {
            text = text.replace(ANY_OF, ALTERNATIVE);
        }
        JsonObject json = null;
        if (path.contains("/tags/")) {
            json = JsonParser.parseString(text).getAsJsonObject();
            removeValues(json.getAsJsonArray("values"));
        } else if (path.contains("/cup_conversion/")) {
            json = JsonParser.parseString(text).getAsJsonObject();
            JsonArray kept = new JsonArray();
            for (JsonElement entry : json.getAsJsonArray("entries")) {
                JsonObject row = entry.getAsJsonObject();
                String bowl = row.get("bowl").getAsString();
                if (!MISSING_BOWLS.contains(bowl) && !isRemoved(bowl) && !isRemoved(row.get("cup").getAsString())) {
                    kept.add(row);
                }
            }
            json.add("entries", kept);
        }
        UnaryOperator<JsonObject> patch = PATCH_DATA.get(path);
        if (patch != null) {
            json = patch.apply(json != null ? json : JsonParser.parseString(text).getAsJsonObject());
        }
        return (json != null ? GSON.toJson(json) : text).getBytes(StandardCharsets.UTF_8);
    }

    private static void removeValues(JsonArray values) {
        if (values == null) {
            return;
        }
        for (int i = values.size() - 1; i >= 0; i--) {
            JsonElement value = values.get(i);
            String id = value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString() : value.getAsString();
            if (isRemoved(id)) {
                values.remove(i);
            }
        }
    }

    private static byte[] withoutRemovedLangKeys(byte[] bytes) {
        JsonObject lang = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        lang.keySet().removeIf(key -> REMOVED.stream().anyMatch(id -> key.endsWith("." + MinersDelightMod.MODID + "." + id)));
        return GSON.toJson(lang).getBytes(StandardCharsets.UTF_8);
    }

    private static boolean isRemoved(String id) {
        String bare = id.startsWith("#") ? id.substring(1) : id;
        return bare.startsWith(MinersDelightMod.MODID + ":") && REMOVED.contains(bare.substring(MinersDelightMod.MODID.length() + 1));
    }

    private static boolean namesRemoved(String path) {
        String file = path.substring(path.lastIndexOf('/') + 1);
        return REMOVED.stream().anyMatch(id -> file.equals(id + ".json") || file.equals(id + ".png") || file.equals(id + ".png.mcmeta"));
    }

    private static boolean mentionsRemoved(String text) {
        return REMOVED.stream().anyMatch(id -> text.contains("\"" + MinersDelightMod.MODID + ":" + id + "\""));
    }
}
