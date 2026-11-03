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
    /**
     * Upstream ids the port does not register: the hot cocoa cup (D3) and, since TODO dishes/meals #53 (2026-11-02), the
     * Copper Cup and every cup (water, milk, powder snow, the 15 soup and stew cups). Files named after them are left out,
     * files naming them are dropped (tags lose the entry instead), the cup conversion data is not read at all.
     */
    public static final Set<String> REMOVED = Set.of("hot_cocoa_cup", "copper_cup", "water_cup", "milk_cup", "powder_snow_cup",
            "beetroot_soup_cup", "mushroom_stew_cup", "rabbit_stew_cup", "baked_cod_stew_cup", "noodle_soup_cup", "beef_stew_cup",
            "chicken_soup_cup", "fish_stew_cup", "pumpkin_soup_cup", "vegetable_soup_cup", "bone_broth_cup", "cave_soup_cup",
            "bat_soup_cup", "insect_stew_cup", "onion_soup_cup");
    /** 1.20 renamed the loot condition {@code alternative} to {@code any_of}; 1.19.2 only knows the old name. */
    private static final String ANY_OF = "\"minecraft:any_of\"";
    private static final String ALTERNATIVE = "\"minecraft:alternative\"";
    /**
     * Textures 1.4.5 redrew that keep their 1.1.2 look (owner 2026-11-01, TODO creativedelight/members #31): read from the
     * 1.1.2 jar over the 1.4.5 ones. The other nine redraws (cave carrot stages, item and wild patch, gossypium, weird
     * caviar, bowl of stuffed squid) stay 1.4.5's.
     */
    static final String[] OLD_LOOK = {
            "assets/miners_delight/textures/block/sticky_basket_side.png",
            "assets/miners_delight/textures/item/baked_cave_carrot.png",
            "assets/miners_delight/textures/item/tentacles.png",
            "assets/miners_delight/textures/item/baked_tentacles.png",
    };
    /** Per-file data edits for later pack decisions, keyed by the translated path; applied before the removed-id check. */
    private static final Map<String, UnaryOperator<JsonObject>> PATCH_DATA = Map.of(
            // The Copper Pot takes a water bucket or a water cup; without cups only the bucket is left (dishes/meals #53).
            "data/miners_delight/recipes/copper_pot.json", MinersTranslator::withoutRemovedAlternatives);

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
            Map<String, byte[]> oldLook = UpstreamJars.miners112(OLD_LOOK);
            if (oldLook.size() != OLD_LOOK.length) {
                throw new IllegalStateException("Miner's Delight 1.1.2 jar lacks some of the old-look textures: found " + oldLook.keySet());
            }
            assets.putAll(oldLook);
            MinersDelightMod.LOGGER.info("Miner's Delight upstream assets: {} files ({} in the 1.1.2 look)", assets.size(), oldLook.size());
        }
        return assets;
    }

    /**
     * Upstream data as served. Its loot modifiers (data/forge and data/miners_delight loot_modifiers, the global list
     * included) are left out: the pack's loot rules give those drops (creativedelight members #51, miners_delight.md D8;
     * kubejs/data/creativedelight/kubejs/miners_delight/loot.js).
     */
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
                if (namesRemoved(path) || path.contains("/loot_modifiers/")) {
                    return;
                }
                byte[] translated = translateData(path, bytes);
                if (translated != null) {
                    data.put(path, translated);
                }
            });
            MinersDelightMod.LOGGER.info("Miner's Delight upstream data: {} files", data.size());
        }
        return data;
    }

    /** Null drops the file. */
    private static byte[] translateData(String path, byte[] bytes) {
        if (!path.endsWith(".json")) {
            return bytes;
        }
        if (path.contains("/cup_conversion/")) {
            return null;
        }
        String text = new String(bytes, StandardCharsets.UTF_8);
        JsonObject json = null;
        UnaryOperator<JsonObject> patch = PATCH_DATA.get(path);
        if (patch != null) {
            json = patch.apply(JsonParser.parseString(text).getAsJsonObject());
            text = GSON.toJson(json);
        }
        if (mentionsRemoved(text) && !path.contains("/tags/")) {
            return null;
        }
        if (path.contains("/loot_tables/")) {
            text = text.replace(ANY_OF, ALTERNATIVE);
            json = null;
        }
        if (path.contains("/tags/")) {
            json = JsonParser.parseString(text).getAsJsonObject();
            removeValues(json.getAsJsonArray("values"));
        }
        return (json != null ? GSON.toJson(json) : text).getBytes(StandardCharsets.UTF_8);
    }

    /** A shaped recipe whose key entries are lists of alternatives: the alternatives naming a removed item are left out. */
    private static JsonObject withoutRemovedAlternatives(JsonObject json) {
        for (Map.Entry<String, JsonElement> key : json.getAsJsonObject("key").entrySet()) {
            if (key.getValue().isJsonArray()) {
                JsonArray options = key.getValue().getAsJsonArray();
                for (int i = options.size() - 1; i >= 0; i--) {
                    JsonObject option = options.get(i).getAsJsonObject();
                    if (option.has("item") && isRemoved(option.get("item").getAsString())) {
                        options.remove(i);
                    }
                }
            }
        }
        return json;
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
