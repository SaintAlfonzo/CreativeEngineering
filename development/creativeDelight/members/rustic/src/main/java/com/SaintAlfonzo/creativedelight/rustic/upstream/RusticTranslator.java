package com.SaintAlfonzo.creativedelight.rustic.upstream;

import com.SaintAlfonzo.creativedelight.rustic.RusticDelightMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * The in-memory view of the upstream jar (feature_writeup/rustic_delight.md 5): its assets as the client pack, its data
 * as the server pack, translated to what the port registers on 1.19.2. Nothing here is shipped in our jar; every change
 * to upstream data goes through this class, never through a file at an upstream path.
 */
public final class RusticTranslator {
    /**
     * Upstream ids the port does not register: Fruit Beignet (owner 2026-10-25), the plain single Pancake and Fried Dough
     * (owner 2026-11-01, TODO creativedelight/members #15, #25). Data naming one is dropped, after {@link #REPLACED}.
     */
    public static final Set<String> REMOVED = Set.of("fruit_beignet", "pancake", "fried_dough");
    /**
     * Removed ids whose place another mod's item takes in upstream data: the plain plate's recipes, cut and loot use
     * Supplementaries' pancake (owner 2026-11-01, TODO creativedelight/members #15). A data file naming the other mod's
     * item is still dropped when that mod is absent ({@link #missingRecipeId}).
     */
    public static final Map<String, String> REPLACED = Map.of("pancake", "supplementaries:pancake");
    /**
     * Whole upstream folders the pack does not use: advancements (the pack drops them all), the biome modifiers (the
     * pack places every wild plant through Creative Delight's wild_crops.json), the chest seed and bean swaps (the pack's loot scripts own
     * chest loot; the Pale and Dark seeds come from there), and upstream's edits of five Farmer's Delight recipes (1.20
     * recipes; 1.3.0 left them alone).
     */
    private static final List<String> DROPPED_FOLDERS = List.of("/advancements/", "data/rusticdelight/forge/biome_modifier/",
            "data/farmersdelight/recipes/");
    private static final Set<String> DROPPED_LOOT_MODIFIERS = Set.of("coffee_beans_from_jungle_temple", "pale_bell_pepper_seeds_from_jungle_temple",
            "dark_bell_pepper_seeds_from_jungle_temple", "cotton_seeds_from_village_taiga", "cotton_seeds_from_village_snowy",
            "cotton_seeds_from_village_savanna", "cotton_seeds_from_abandoned_mineshaft", "cotton_seeds_from_shipwreck_supply");
    /** Wild and giant patches keep the three base colours: Pale and Dark seeds only come from chest loot (owner). */
    private static final Set<String> RARE_PATCH_BLOCKS = Set.of("wild_pale_bell_peppers", "wild_dark_bell_peppers", "bell_pepper_orange_block",
            "bell_pepper_white_block", "bell_pepper_pink_block", "bell_pepper_blue_block", "bell_pepper_purple_block", "bell_pepper_black_block");
    /** 1.19.2 has no cherry blossoms; the pack's pink leaves stand in (owner: Vinery cherry leaves or Quark pink blossom leaves). */
    private static final List<String> CHERRY_BLOSSOMS = List.of("vinery:cherry_leaves", "quark:pink_blossom_leaves");

    private static final Map<String, UnaryOperator<JsonObject>> PATCH_DATA = Map.of(
            "data/rusticdelight/tags/items/cherry_blossom_ingredients.json", RusticTranslator::cherryBlossoms,
            "data/rusticdelight/worldgen/configured_feature/wild_bell_peppers.json", RusticTranslator::basePatch,
            "data/rusticdelight/worldgen/configured_feature/bell_pepper_block_patch.json", RusticTranslator::basePatch);

    /**
     * Farmer's Delight 1.20 model ids the upstream models name, to their 1.19.2 names (same model). FD 1.20's platter
     * texture (Bell Pepper Medley) has no 1.19.2 counterpart; repackedFarmers ships it at FD's own path instead.
     */
    private static final Map<String, String> RENAMED_MODEL_REFS = Map.of(
            "\"farmersdelight:block/template_crop_cross\"", "\"farmersdelight:block/crop_cross\"");

    private static final String OLD_TOOLTIP_PREFIX = "tooltip.farmersdelight.";
    private static final String NEW_TOOLTIP_PREFIX = "farmersdelight.tooltip.";

    private static final Gson GSON =new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static Map<String, byte[]> assets;
    private static Map<String, byte[]> data;

    private RusticTranslator() {
    }

    public static synchronized Map<String, byte[]> assets() {
        if (assets == null) {
            assets = new HashMap<>();
            UpstreamJars.rustic170().forEach((path, bytes) -> {
                if (!path.startsWith("assets/") || namesRemoved(path)) {
                    return;
                }
                if (path.contains("/lang/")) {
                    bytes = withoutRemovedLangKeys(bytes);
                } else if (path.contains("/models/")) {
                    bytes = withRenamedModelRefs(bytes);
                }
                assets.put(path, bytes);
            });
            RusticDelightMod.LOGGER.info("Rustic Delight upstream assets: {} files", assets.size());
        }
        return assets;
    }

    /** Built when the server first reads data, after every registry is complete. */
    public static synchronized Map<String, byte[]> data() {
        if (data == null) {
            data = new HashMap<>();
            int[] dropped = {0};
            UpstreamJars.rustic170().forEach((path, bytes) -> {
                if (!path.startsWith("data/") || namesRemoved(path) || DROPPED_FOLDERS.stream().anyMatch(path::contains)) {
                    return;
                }
                byte[] translated = translateData(path, bytes);
                if (translated != null) {
                    data.put(path, translated);
                } else {
                    dropped[0]++;
                }
            });
            dropDanglingLootModifiers(data);
            RusticDelightMod.LOGGER.info("Rustic Delight upstream data: {} files ({} dropped as not loadable on 1.19.2)", data.size(), dropped[0]);
        }
        return data;
    }

    /** Null drops the file. */
    private static byte[] translateData(String path, byte[] bytes) {
        if (!path.endsWith(".json")) {
            return bytes;
        }
        if (path.startsWith("data/rusticdelight/loot_modifiers/")) {
            String name = path.substring(path.lastIndexOf('/') + 1, path.length() - 5);
            if (DROPPED_LOOT_MODIFIERS.contains(name)) {
                return null;
            }
        }
        String text = withReplacedIds(new String(bytes, StandardCharsets.UTF_8));
        if (mentionsRemoved(text) && !path.contains("/tags/")) {
            return null;
        }
        JsonObject json = JsonParser.parseString(text).getAsJsonObject();
        if (path.contains("/tags/")) {
            tagValues(json.getAsJsonArray("values"));
        } else if (path.contains("/recipes/")) {
            String missing = missingRecipeId(json);
            if (missing != null) {
                RusticDelightMod.LOGGER.debug("Rustic Delight: {} names {}, not on this game", path, missing);
                return null;
            }
        } else if (path.contains("/loot_modifiers/") || path.contains("/loot_tables/")) {
            json = (JsonObject) lootConditions(json);
        }
        UnaryOperator<JsonObject> patch = PATCH_DATA.get(path);
        if (patch != null) {
            json = patch.apply(json);
        }
        return GSON.toJson(json).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 1.20 renamed the loot condition {@code alternative} to {@code any_of} and added {@code all_of}; 1.19.2 knows
     * neither. {@code any_of} becomes {@code alternative}; {@code all_of} becomes "not any of the negations".
     */
    private static JsonElement lootConditions(JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                array.set(i, lootConditions(array.get(i)));
            }
            return array;
        }
        if (!element.isJsonObject()) {
            return element;
        }
        JsonObject object = element.getAsJsonObject();
        for (Entry<String, JsonElement> entry : object.entrySet()) {
            entry.setValue(lootConditions(entry.getValue()));
        }
        JsonElement condition = object.get("condition");
        String name = condition != null && condition.isJsonPrimitive() ? condition.getAsString() : "";
        if (name.equals("minecraft:any_of")) {
            object.addProperty("condition", "minecraft:alternative");
        } else if (name.equals("minecraft:all_of")) {
            JsonArray negated = new JsonArray();
            for (JsonElement term : object.getAsJsonArray("terms")) {
                negated.add(not(term));
            }
            JsonObject any = new JsonObject();
            any.addProperty("condition", "minecraft:alternative");
            any.add("terms", negated);
            return not(any);
        }
        return object;
    }

    private static JsonObject not(JsonElement term) {
        JsonObject inverted = new JsonObject();
        inverted.addProperty("condition", "minecraft:inverted");
        inverted.add("term", term);
        return inverted;
    }

    /**
     * The first item, block, or recipe type a recipe names that this game does not have (1.20-only items, mods not in
     * the pack), or null. Such a recipe would only log a parse error.
     */
    private static String missingRecipeId(JsonObject recipe) {
        String type = recipe.has("type") ? recipe.get("type").getAsString() : null;
        if (type != null && !type.equals("forge:conditional") && !ForgeRegistries.RECIPE_SERIALIZERS.containsKey(new ResourceLocation(type))) {
            return type;
        }
        return missingItem(recipe);
    }

    private static String missingItem(JsonElement element) {
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                String missing = missingItem(child);
                if (missing != null) {
                    return missing;
                }
            }
        } else if (element.isJsonObject()) {
            for (Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                JsonElement value = entry.getValue();
                if (entry.getKey().equals("item") && value.isJsonPrimitive()) {
                    if (!ForgeRegistries.ITEMS.containsKey(new ResourceLocation(value.getAsString()))) {
                        return value.getAsString();
                    }
                } else if (!entry.getKey().equals("conditions")) {
                    String missing = missingItem(value);
                    if (missing != null) {
                        return missing;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Drops removed ids; every entry from another namespace becomes optional, so a tag naming a 1.20 item or an absent
     * mod still loads (1.19.2 rejects the whole tag for one unknown entry).
     */
    private static void tagValues(JsonArray values) {
        if (values == null) {
            return;
        }
        for (int i = values.size() - 1; i >= 0; i--) {
            JsonElement value = values.get(i);
            String id = value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString() : value.getAsString();
            if (isRemoved(id)) {
                values.remove(i);
            } else if (!value.isJsonObject() && !id.startsWith(RusticDelightMod.MODID + ":") && !id.startsWith("#" + RusticDelightMod.MODID + ":")) {
                values.set(i, optional(id));
            }
        }
    }

    private static JsonObject optional(String id) {
        JsonObject entry = new JsonObject();
        entry.addProperty("id", id);
        entry.addProperty("required", false);
        return entry;
    }

    private static JsonObject cherryBlossoms(JsonObject tag) {
        JsonArray values = new JsonArray();
        CHERRY_BLOSSOMS.forEach(id -> values.add(optional(id)));
        tag.add("values", values);
        return tag;
    }

    /** Keeps the base-colour entries of a patch's weighted state provider. */
    private static JsonObject basePatch(JsonObject feature) {
        stripRare(feature);
        return feature;
    }

    private static void stripRare(JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (int i = array.size() - 1; i >= 0; i--) {
                JsonElement entry = array.get(i);
                if (entry.isJsonObject() && entry.getAsJsonObject().has("weight") && entry.getAsJsonObject().has("data")) {
                    String name = entry.getAsJsonObject().getAsJsonObject("data").get("Name").getAsString();
                    if (RARE_PATCH_BLOCKS.contains(name.substring(name.indexOf(':') + 1))) {
                        array.remove(i);
                        continue;
                    }
                }
                stripRare(entry);
            }
        } else if (element.isJsonObject()) {
            element.getAsJsonObject().entrySet().forEach(entry -> stripRare(entry.getValue()));
        }
    }

    /** Keep only the listed modifiers that still have a file (the translation drops some). */
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

    private static byte[] withoutRemovedLangKeys(byte[] bytes) {
        JsonObject lang = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        lang.keySet().removeIf(key -> REMOVED.stream().anyMatch(id -> key.endsWith("." + RusticDelightMod.MODID + "." + id)));
        // FD 1.20 names item tooltips tooltip.farmersdelight.<item>; FD 1.19.2 reads farmersdelight.tooltip.<item>.
        JsonObject renamed = new JsonObject();
        lang.entrySet().forEach(e -> renamed.add(e.getKey().startsWith(OLD_TOOLTIP_PREFIX)
                ? NEW_TOOLTIP_PREFIX + e.getKey().substring(OLD_TOOLTIP_PREFIX.length()) : e.getKey(), e.getValue()));
        return GSON.toJson(renamed).getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] withRenamedModelRefs(byte[] bytes) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        String renamed = text;
        for (Entry<String, String> e : RENAMED_MODEL_REFS.entrySet()) {
            renamed = renamed.replace(e.getKey(), e.getValue());
        }
        return renamed.equals(text) ? bytes : renamed.getBytes(StandardCharsets.UTF_8);
    }

    private static boolean isRemoved(String id) {
        String bare = id.startsWith("#") ? id.substring(1) : id;
        return bare.startsWith(RusticDelightMod.MODID + ":") && REMOVED.contains(bare.substring(RusticDelightMod.MODID.length() + 1));
    }

    private static boolean namesRemoved(String path) {
        String file = path.substring(path.lastIndexOf('/') + 1);
        return REMOVED.stream().anyMatch(id -> file.equals(id + ".json") || file.equals(id + ".png") || file.equals(id + ".png.mcmeta"));
    }

    private static String withReplacedIds(String text) {
        for (Entry<String, String> entry : REPLACED.entrySet()) {
            text = text.replace("\"" + RusticDelightMod.MODID + ":" + entry.getKey() + "\"", "\"" + entry.getValue() + "\"");
        }
        return text;
    }

    private static boolean mentionsRemoved(String text) {
        return REMOVED.stream().anyMatch(id -> text.contains("\"" + RusticDelightMod.MODID + ":" + id + "\""));
    }
}
