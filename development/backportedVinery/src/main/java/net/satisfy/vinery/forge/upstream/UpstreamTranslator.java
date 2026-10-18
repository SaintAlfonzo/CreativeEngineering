package net.satisfy.vinery.forge.upstream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.satisfy.vinery.core.Vinery;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Builds the port's view of the upstream jars, in memory (feature_writeup/alcohol.md B4.8):
 * <ul>
 *     <li>{@link #assets()}: 1.3.10 assets, then Foundation's, then 1.5.5's on top. Lang files and sounds.json merge
 *     by key. Port ids whose art is 1.5.5's under another name (D1, B4.1) get that blockstate, item model and name.</li>
 *     <li>{@link #data()}: 1.5.5 and Foundation data converted to 1.19.2 (folder names, result stacks, loot and
 *     advancement predicates, worldgen types), with the B4.1 id map applied; plus the 1.3.10 data of the D2 keepers
 *     and its biome modifiers. Anything that names an item or block this game doesn't have is left out.</li>
 * </ul>
 * Nothing here is written to disk.
 */
public final class UpstreamTranslator {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    /** 1.3.10 items 1.5.5 removed (D2): their 1.3.10 recipes and loot tables are read. */
    static final Set<String> KEEPERS = Set.of("white_grape_crate", "red_grape_crate", "cherry_crate", "apple_crate",
            "grapevine_lattice", "chair", "table", "big_table", "shelf", "wine_rack_1", "wine_rack_2", "wine_rack_3",
            "wine_rack_5", "loam", "loam_slab", "loam_stairs", "flower_box", "flower_pot", "basket", "calendar",
            "wood_fired_oven", "stove", "kitchen_sink", "barrel", "praetorian_wine", "king_danis_wine");

    /** 1.20+ vanilla items -> what 1.19.2 (and Quark, D12) has. Applied before the unknown-id filter. */
    private static final Map<String, String> VANILLA_SUBSTITUTES = Map.of(
            "minecraft:bamboo_planks", "quark:bamboo_planks",
            "minecraft:bamboo_slab", "quark:bamboo_planks_slab");

    /** 1.21 data folder -> 1.19.2 data folder. */
    private static final Map<String, String> FOLDERS = new LinkedHashMap<>();

    static {
        FOLDERS.put("recipe/", "recipes/");
        FOLDERS.put("loot_table/", "loot_tables/");
        FOLDERS.put("advancement/", "advancements/");
        FOLDERS.put("structure/", "structures/");
        FOLDERS.put("tags/item/", "tags/items/");
        FOLDERS.put("tags/block/", "tags/blocks/");
        FOLDERS.put("tags/entity_type/", "tags/entity_types/");
        FOLDERS.put("tags/fluid/", "tags/fluids/");
        FOLDERS.put("tags/game_event/", "tags/game_events/");
    }

    private static Map<String, byte[]> assets;
    private static Map<String, byte[]> data;
    private static int dropped;

    private UpstreamTranslator() {
    }

    public static synchronized Map<String, byte[]> assets() {
        if (assets == null) {
            assets = buildAssets();
        }
        return assets;
    }

    public static synchronized Map<String, byte[]> data() {
        if (data == null) {
            dropped = 0;
            data = buildData();
            Vinery.LOGGER.info("[vinery-upstream] {} data files from the upstream jars, {} left out (1.20+ content or ids this game doesn't have)", data.size(), dropped);
        }
        return data;
    }

    // ---------------------------------------------------------------- assets

    private static Map<String, byte[]> buildAssets() {
        Map<String, byte[]> out = new HashMap<>();
        Map<String, JsonObject> merged = new HashMap<>();
        for (Map<String, byte[]> layer : List.of(UpstreamJars.vinery1310(), UpstreamJars.foundation(), UpstreamJars.vinery155())) {
            for (Map.Entry<String, byte[]> entry : layer.entrySet()) {
                String path = entry.getKey();
                if (!path.startsWith("assets/")) {
                    continue;
                }
                if (isMergedByKey(path)) {
                    JsonObject json = parseObject(entry.getValue());
                    if (json != null) {
                        JsonObject target = merged.computeIfAbsent(path, p -> new JsonObject());
                        json.entrySet().forEach(e -> target.add(e.getKey(), e.getValue()));
                    }
                } else {
                    out.put(path, entry.getValue());
                }
            }
        }

        Map<String, byte[]> v155 = UpstreamJars.vinery155();
        IdMap.assetAliases().forEach((port, upstream) -> {
            for (String folder : new String[]{"blockstates/", "models/item/"}) {
                byte[] art = v155.get("assets/vinery/" + folder + upstream + ".json");
                if (art != null) {
                    out.put("assets/vinery/" + folder + port + ".json", art);
                }
            }
            merged.forEach((path, json) -> {
                if (path.contains("/lang/")) {
                    for (String kind : new String[]{"block", "item"}) {
                        JsonElement name = json.get(kind + ".vinery." + upstream);
                        if (name != null) {
                            json.add(kind + ".vinery." + port, name);
                        }
                    }
                }
            });
        });
        merged.forEach((path, json) -> out.put(path, bytes(json)));
        return out;
    }

    private static boolean isMergedByKey(String path) {
        return path.endsWith("/sounds.json") || (path.contains("/lang/") && path.endsWith(".json"));
    }

    // ---------------------------------------------------------------- data

    private static Map<String, byte[]> buildData() {
        Map<String, byte[]> out = new HashMap<>();
        Map<String, JsonObject> tags = new HashMap<>();

        // 1.3.10 first (keepers and biome placement only); 1.5.5 wins where both have a file
        for (Map.Entry<String, byte[]> entry : UpstreamJars.vinery1310().entrySet()) {
            String path = entry.getKey();
            if (!path.startsWith("data/") || !keeperData(path, entry.getValue())) {
                continue;
            }
            JsonObject json = parseObject(entry.getValue());
            if (json == null) {
                continue;
            }
            if (path.contains("/tags/")) {
                mergeTag(tags, path, json);
            } else {
                rewriteIds(json);
                if (keep(path, json)) {
                    out.put(path, bytes(json));
                }
            }
        }

        for (Map<String, byte[]> layer : List.of(UpstreamJars.foundation(), UpstreamJars.vinery155())) {
            for (Map.Entry<String, byte[]> entry : layer.entrySet()) {
                String path = entry.getKey();
                if (!path.startsWith("data/") || !path.endsWith(".json")) {
                    continue;
                }
                String converted = convertPath(path);
                if (converted == null) {
                    continue;
                }
                JsonObject json = parseObject(entry.getValue());
                if (json == null) {
                    continue;
                }
                rewriteIds(json);
                if (converted.contains("/tags/")) {
                    mergeTag(tags, converted, json);
                    continue;
                }
                if (converted.contains("/recipes/")) {
                    json = convertRecipe(json);
                } else if (converted.contains("/loot_tables/")) {
                    convertLootTable(json);
                } else if (converted.contains("/advancements/")) {
                    json = convertAdvancement(json);
                } else if (converted.contains("/worldgen/")) {
                    convertWorldgen(json);
                }
                if (json != null && keep(converted, json)) {
                    out.put(converted, bytes(json));
                } else {
                    dropped++;
                }
            }
        }
        tags.forEach((path, json) -> out.put(path, bytes(json)));
        return out;
    }

    /** 1.3.10 data the port reads: keeper recipes and loot, tags, biome placement and its biome tags. */
    private static boolean keeperData(String path, byte[] content) {
        if (path.contains("/forge/biome_modifier/") || path.contains("/tags/")) {
            return true;
        }
        if (path.startsWith("data/vinery/loot_tables/blocks/")) {
            return KEEPERS.contains(fileName(path));
        }
        if (path.startsWith("data/vinery/recipes/")) {
            JsonObject json = parseObject(content);
            if (json == null || !json.has("result")) {
                return false;
            }
            JsonElement result = json.get("result");
            String item = result.isJsonObject() && result.getAsJsonObject().has("item") ? result.getAsJsonObject().get("item").getAsString()
                    : result.isJsonPrimitive() ? result.getAsString() : "";
            String type = json.has("type") ? json.get("type").getAsString() : "";
            return item.startsWith("vinery:") && KEEPERS.contains(item.substring(7)) && !type.startsWith("vinery:");
        }
        return false;
    }

    /** 1.21 folder names to 1.19.2; null for what the port doesn't read. Block loot tables follow the id map. */
    private static String convertPath(String path) {
        String[] parts = path.split("/", 3);
        if (parts.length < 3) {
            return null;
        }
        String rest = parts[2];
        for (Map.Entry<String, String> folder : FOLDERS.entrySet()) {
            if (rest.startsWith(folder.getKey())) {
                rest = folder.getValue() + rest.substring(folder.getKey().length());
                break;
            }
        }
        if (rest.startsWith("structures/")) {
            return null; // F8: 1.5.5's village houses are unused upstream and not ported
        }
        if (parts[1].equals("vinery") && rest.startsWith("loot_tables/blocks/")) {
            String name = fileName(rest);
            if (IdMap.DROPPED.contains(name)) {
                return null;
            }
            rest = "loot_tables/blocks/" + IdMap.renamePath(name) + ".json";
        }
        return "data/" + parts[1] + "/" + rest;
    }

    // ---------------------------------------------------------------- per kind

    private static JsonObject convertRecipe(JsonObject json) {
        String type = json.has("type") ? json.get("type").getAsString() : "";
        if (type.equals("minecraft:smithing_transform")) {
            JsonObject legacy = new JsonObject();
            legacy.addProperty("type", "minecraft:smithing");
            legacy.add("base", json.get("base"));
            legacy.add("addition", json.get("addition"));
            legacy.add("result", json.get("result"));
            json = legacy;
        }
        if (!type.startsWith("vinery:") && json.has("result") && json.get("result").isJsonObject()) {
            JsonObject result = json.getAsJsonObject("result");
            if (result.has("id") && !result.has("item")) {
                result.add("item", result.remove("id"));
            }
            result.remove("components");
        }
        return json;
    }

    private static void convertLootTable(JsonObject json) {
        json.remove("random_sequence");
        convertConditions(json);
    }

    /** 1.21 loot conditions and item / block / enchantment predicates to their 1.19.2 shapes, everywhere in the tree. */
    private static void convertConditions(JsonElement element) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(UpstreamTranslator::convertConditions);
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject json = element.getAsJsonObject();
        if (json.has("condition")) {
            String condition = json.get("condition").getAsString();
            if (condition.equals("minecraft:any_of")) {
                json.addProperty("condition", "minecraft:alternative");
            } else if (condition.equals("minecraft:match_tool") && json.has("predicate")) {
                convertItemPredicate(json.getAsJsonObject("predicate"));
            } else if (condition.equals("minecraft:location_check") && json.has("predicate")) {
                convertLocationPredicate(json.getAsJsonObject("predicate"));
            } else if (condition.equals("minecraft:entity_properties") && json.has("predicate")
                    && json.getAsJsonObject("predicate").has("location")) {
                convertLocationPredicate(json.getAsJsonObject("predicate").getAsJsonObject("location"));
            }
        }
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            convertConditions(entry.getValue());
        }
    }

    /** {@code "items": "x"} / {@code "#tag"} / list -> {@code "items": [...]} or {@code "tag"}; enchantment predicates. */
    private static void convertItemPredicate(JsonObject predicate) {
        JsonElement items = predicate.remove("items");
        if (items != null) {
            JsonArray list = new JsonArray();
            if (items.isJsonPrimitive() && items.getAsString().startsWith("#")) {
                predicate.addProperty("tag", items.getAsString().substring(1));
            } else {
                (items.isJsonArray() ? items.getAsJsonArray() : single(items)).forEach(list::add);
                predicate.add("items", list);
            }
        }
        JsonElement components = predicate.remove("predicates");
        if (components != null && components.isJsonObject()) {
            JsonElement enchantments = components.getAsJsonObject().get("minecraft:enchantments");
            if (enchantments != null && enchantments.isJsonArray()) {
                JsonArray converted = new JsonArray();
                for (JsonElement e : enchantments.getAsJsonArray()) {
                    JsonObject in = e.getAsJsonObject();
                    JsonObject legacy = new JsonObject();
                    if (in.has("enchantments")) {
                        legacy.add("enchantment", in.get("enchantments").isJsonArray() ? in.getAsJsonArray("enchantments").get(0) : in.get("enchantments"));
                    }
                    if (in.has("levels")) {
                        legacy.add("levels", in.get("levels"));
                    }
                    converted.add(legacy);
                }
                predicate.add("enchantments", converted);
            }
        }
    }

    /** Block predicate as for items; 1.21 {@code "biomes"} / {@code "structures"} (one id) -> 1.19.2 {@code "biome"} / {@code "structure"}. */
    private static void convertLocationPredicate(JsonObject predicate) {
        for (String key : new String[]{"biome", "structure"}) {
            JsonElement plural = predicate.remove(key + "s");
            if (plural == null) {
                continue;
            }
            JsonElement first = plural.isJsonArray() && !plural.getAsJsonArray().isEmpty() ? plural.getAsJsonArray().get(0) : plural;
            if (first.isJsonPrimitive() && !first.getAsString().startsWith("#")) {
                predicate.add(key, first);
            } else {
                Vinery.LOGGER.warn("[vinery-upstream] location predicate {}s {} has no 1.19.2 form; left unrestricted", key, plural);
            }
        }
        if (predicate.has("block") && predicate.get("block").isJsonObject()) {
            JsonObject block = predicate.getAsJsonObject("block");
            JsonElement blocks = block.remove("blocks");
            if (blocks != null) {
                if (blocks.isJsonPrimitive() && blocks.getAsString().startsWith("#")) {
                    block.addProperty("tag", blocks.getAsString().substring(1));
                } else {
                    block.add("blocks", blocks.isJsonArray() ? blocks : single(blocks));
                }
            }
        }
    }

    private static JsonObject convertAdvancement(JsonObject json) {
        json.remove("sends_telemetry_event");
        if (json.has("display")) {
            JsonObject display = json.getAsJsonObject("display");
            if (display.has("icon") && display.get("icon").isJsonObject()) {
                JsonObject icon = display.getAsJsonObject("icon");
                if (icon.has("id")) {
                    icon.add("item", icon.remove("id"));
                }
                icon.remove("components");
            }
        }
        if (json.has("criteria")) {
            for (Map.Entry<String, JsonElement> criterion : json.getAsJsonObject("criteria").entrySet()) {
                JsonObject c = criterion.getValue().getAsJsonObject();
                String trigger = c.has("trigger") ? c.get("trigger").getAsString() : "";
                JsonObject conditions = c.has("conditions") ? c.getAsJsonObject("conditions") : new JsonObject();
                switch (trigger) {
                    case "minecraft:inventory_changed" -> {
                        if (conditions.has("items")) {
                            conditions.getAsJsonArray("items").forEach(p -> convertItemPredicate(p.getAsJsonObject()));
                        }
                    }
                    case "minecraft:placed_block" -> {
                        JsonElement location = conditions.remove("location");
                        String block = firstBlock(location);
                        if (block == null) {
                            return null;
                        }
                        conditions.addProperty("block", block);
                    }
                    case "minecraft:item_used_on_block" -> {
                        JsonElement location = conditions.remove("location");
                        if (location != null && location.isJsonArray()) {
                            for (JsonElement cond : location.getAsJsonArray()) {
                                JsonObject o = cond.getAsJsonObject();
                                String type = o.has("condition") ? o.get("condition").getAsString() : "";
                                if (type.equals("minecraft:location_check") && o.has("predicate")) {
                                    JsonObject predicate = o.getAsJsonObject("predicate");
                                    convertLocationPredicate(predicate);
                                    conditions.add("location", predicate);
                                } else if (type.equals("minecraft:match_tool") && o.has("predicate")) {
                                    JsonObject predicate = o.getAsJsonObject("predicate");
                                    convertItemPredicate(predicate);
                                    conditions.add("item", predicate);
                                }
                            }
                        }
                    }
                    case "minecraft:recipe_unlocked" -> {
                    }
                    default -> convertConditions(conditions);
                }
            }
        }
        return json;
    }

    private static String firstBlock(JsonElement location) {
        if (location == null || !location.isJsonArray()) {
            return null;
        }
        for (JsonElement cond : location.getAsJsonArray()) {
            JsonObject o = cond.getAsJsonObject();
            if (o.has("block")) {
                return o.get("block").getAsString();
            }
        }
        return null;
    }

    /** User decision (B4.8): 1.20+ worldgen types become the nearest 1.19.2 vanilla type; nothing is backported. */
    private static void convertWorldgen(JsonElement element) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(UpstreamTranslator::convertWorldgen);
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject json = element.getAsJsonObject();
        // Tree configs: 1.19.2's TreeConfiguration has no sapling_provider (a stale field upstream still writes). Its
        // vinery:apple_sapling id doesn't exist here (the port's is apple_tree_sapling), so keep() would drop the whole
        // configured feature and leave placed_feature/tree_apple unbound, which stops existing worlds loading.
        if (json.has("trunk_provider")) {
            json.remove("sapling_provider");
        }
        if (json.has("type") && json.get("type").isJsonPrimitive() && json.get("type").getAsString().equals("minecraft:cherry_foliage_placer")) {
            json.addProperty("type", "minecraft:fancy_foliage_placer");
            json.remove("corner_hole_chance");
            json.remove("hanging_leaves_chance");
            json.remove("hanging_leaves_extension_chance");
            json.remove("wide_bottom_layer_hole_chance");
        }
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            convertWorldgen(entry.getValue());
        }
    }

    /** Tags merge as a union; every entry optional, so an id this game doesn't have never breaks the whole tag. */
    private static void mergeTag(Map<String, JsonObject> tags, String path, JsonObject json) {
        JsonObject target = tags.computeIfAbsent(path, p -> {
            JsonObject tag = new JsonObject();
            tag.addProperty("replace", false);
            tag.add("values", new JsonArray());
            return tag;
        });
        JsonArray values = target.getAsJsonArray("values");
        Set<String> present = new HashSet<>();
        values.forEach(v -> present.add(v.getAsJsonObject().get("id").getAsString()));
        if (!json.has("values")) {
            return;
        }
        for (JsonElement value : json.getAsJsonArray("values")) {
            String id = value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString() : value.getAsString();
            id = id.startsWith("#") ? id : IdMap.rewrite(id);
            if (present.add(id)) {
                JsonObject entry = new JsonObject();
                entry.addProperty("id", id);
                entry.addProperty("required", false);
                values.add(entry);
            }
        }
    }

    // ---------------------------------------------------------------- ids

    /** The B4.1 id map and the 1.20+ vanilla substitutes, on every string in the tree. */
    private static void rewriteIds(JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                JsonElement value = array.get(i);
                if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                    array.set(i, new JsonPrimitive(rewrite(value.getAsString())));
                } else {
                    rewriteIds(value);
                }
            }
        } else if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                JsonElement value = entry.getValue();
                if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                    entry.setValue(new JsonPrimitive(rewrite(value.getAsString())));
                } else {
                    rewriteIds(value);
                }
            }
        }
    }

    private static String rewrite(String value) {
        String substitute = VANILLA_SUBSTITUTES.get(value);
        return substitute != null ? substitute : IdMap.rewrite(value);
    }

    /** False if the file names an item or block this game doesn't have (1.20+ vanilla content, dropped ids). */
    private static boolean keep(String path, JsonObject json) {
        Set<String> missing = new HashSet<>();
        collectIds(json, null, missing);
        if (!missing.isEmpty()) {
            // A dropped worldgen file can leave a placed feature or biome modifier pointing at nothing (world won't load)
            if (path.contains("/worldgen/") || path.contains("/biome_modifier/")) {
                Vinery.LOGGER.warn("[vinery-upstream] left out {}: {}", path, missing);
            } else {
                Vinery.LOGGER.debug("[vinery-upstream] left out {}: {}", path, missing);
            }
            return false;
        }
        return true;
    }

    private static final Set<String> ITEM_KEYS = Set.of("item", "name", "id", "base", "addition");
    private static final Set<String> BLOCK_KEYS = Set.of("block", "Name");

    private static void collectIds(JsonElement element, String key, Set<String> missing) {
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                collectIds(entry.getValue(), entry.getKey(), missing);
            }
        } else if (element.isJsonArray()) {
            for (JsonElement value : element.getAsJsonArray()) {
                collectIds(value, key, missing);
            }
        } else if (key != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String value = element.getAsString();
            if (value.startsWith("#") || !value.contains(":")) {
                return;
            }
            ResourceLocation id = ResourceLocation.tryParse(value);
            if (id == null) {
                return;
            }
            boolean itemKey = ITEM_KEYS.contains(key) || key.equals("items");
            boolean blockKey = BLOCK_KEYS.contains(key) || key.equals("blocks");
            if (itemKey && !known(id, ForgeRegistries.ITEMS::containsKey) && !isNonItemName(key, id)) {
                missing.add(value);
            } else if (blockKey && !known(id, ForgeRegistries.BLOCKS::containsKey)) {
                missing.add(value);
            }
        }
    }

    /** "name" and "id" also name loot tables, functions, triggers...: only vinery / minecraft item-looking ids are checked. */
    private static boolean isNonItemName(String key, ResourceLocation id) {
        if (!key.equals("name") && !key.equals("id")) {
            return false;
        }
        return id.getPath().contains("/") || ForgeRegistries.BLOCKS.containsKey(id) || !(id.getNamespace().equals("vinery") || id.getNamespace().equals("minecraft"));
    }

    private static boolean known(ResourceLocation id, Predicate<ResourceLocation> registry) {
        return registry.test(id);
    }

    // ---------------------------------------------------------------- helpers

    private static JsonArray single(JsonElement element) {
        JsonArray array = new JsonArray();
        array.add(element);
        return array;
    }

    private static String fileName(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.endsWith(".json") ? name.substring(0, name.length() - 5) : name;
    }

    private static JsonObject parseObject(byte[] content) {
        try {
            JsonElement element = JsonParser.parseString(new String(content, StandardCharsets.UTF_8));
            return element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static byte[] bytes(JsonObject json) {
        return GSON.toJson(json).getBytes(StandardCharsets.UTF_8);
    }
}
