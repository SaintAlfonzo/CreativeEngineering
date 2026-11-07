// Port glue (ours, GPLv3), TP-D6: the in-memory view of the upstream Tempad 3.0.4 jar, served as the built-in packs
// tempad_upstream_assets / tempad_upstream_data (BuiltInPacks.addUpstream). Nothing is written to disk.
package earth.terrarium.tempad.forge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import earth.terrarium.tempad.Tempad;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Assets are served as they are: 1.19.2 reads the models, blockstates, lang, sounds and textures (the 1.21 GUI sprite
 * layout under textures/gui/ is only read by our own screens, which bind those files directly). The assets/olympus/
 * files of the Olympus jar nested in the upstream jar (META-INF/jarjar) are added the same way; the core shader
 * rendertype_timedoor loads on 1.19.2 unchanged. One addition: the lang lines {@code config.jade.plugin_tempad.*} are
 * also served as {@code config.waila.plugin_tempad.*}, the names of the WTHIT toggles that replace upstream's Jade plugin
 * (common/compat/WthitPlugin). Lang lines written as component arrays are flattened to plain strings
 * ({@link #flattenRichLang}).
 * <p>
 * Data (TP-D6):
 * <ul>
 * <li>folders renamed to 1.19.2's: recipe/ -> recipes/, loot_table/ -> loot_tables/, tags/item|block|entity_type/ ->
 * tags/items|blocks|entity_types/;</li>
 * <li>{@code c:} tags -> the pack's {@code forge:} tags ({@link #C_TAGS}); upstream's own {@code data/c/} tag file moves
 * to {@code data/forge/}; tag references to them are made optional so a pack without the tag still loads;</li>
 * <li>recipes: result {@code id} -> {@code item}, {@code category} dropped, {@code neoforge:conditions} -> Forge
 * {@code conditions}; recipes that make or use the New Location Upgrade get the {@code tempad:required_location_upgrade}
 * condition (the replacement of upstream's feature flag, config {@code require_location_upgrade});</li>
 * <li>dropped: advancements (pack rule), the NeoForge global loot modifier list (its one entry,
 * tempad:tempad_loot_modifier, is not registered upstream either), upstream's data/tempad/curios/ slot and entity files
 * (the 1.20+ Curios format; Curios 5.1 takes slot types by IMC, common/compat/CuriosCompat), and every file that names a
 * {@code tempad:} item, block or entity this build does not register (phased port); tag entries naming one are removed
 * instead, and a tag left empty is dropped. The Curios item tags data/curios/tags/item/{belt, bracelet, charm} are kept
 * (folder renamed like every tag); without Curios they are unused.</li>
 * </ul>
 */
public final class UpstreamTranslator {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    /** c: tag -> forge: tag of this pack (checked against kubejs/exported/tags, 2026-11-06). */
    static final Map<String, String> C_TAGS = new LinkedHashMap<>();

    static {
        C_TAGS.put("c:ender_pearls", "forge:ender_pearls");
        C_TAGS.put("c:glass_blocks/tinted", "forge:glass/tinted");
        C_TAGS.put("c:glass_blocks/colorless", "forge:glass/colorless");
        C_TAGS.put("c:gems/quartz", "forge:gems/quartz");
        C_TAGS.put("c:gems/amethyst", "forge:gems/amethyst");
        C_TAGS.put("c:ingots/iron", "forge:ingots/iron");
        C_TAGS.put("c:ingots/copper", "forge:ingots/copper");
        C_TAGS.put("c:storage_blocks/iron", "forge:storage_blocks/iron");
        C_TAGS.put("c:dusts/redstone", "forge:dusts/redstone");
        // phase 2: card_cheap, card_wallet, location_broadcaster, screening_device
        C_TAGS.put("c:dyes", "forge:dyes");
        C_TAGS.put("c:leathers", "forge:leather");
        C_TAGS.put("c:gems/emerald", "forge:gems/emerald");
        C_TAGS.put("c:bosses", "forge:bosses");
        C_TAGS.put("c:teleporting_not_supported", "forge:teleporting_not_supported");
    }

    /**
     * Keys whose tempad: values are not registry ids (upgrade ids, serializer types, a loot table's own id in the 1.20
     * key random_sequence, which 1.19.2 ignores). Before random_sequence was listed, every block loot table was dropped
     * as naming an unregistered item (TODO gameplay/tnt-and-portal #10).
     */
    private static final Set<String> NON_REGISTRY_KEYS = Set.of("upgrade", "type", "random_sequence");

    private static final String NEW_LOCATION_UPGRADE = "tempad:new_location_upgrade";

    private static Map<String, byte[]> assets;
    private static Map<String, byte[]> data;

    private UpstreamTranslator() {
    }

    public static synchronized Map<String, byte[]> assets() {
        if (assets == null) {
            assets = new HashMap<>();
            UpstreamJars.tempad304().forEach((path, bytes) -> {
                if (path.startsWith("assets/")) assets.put(path, bytes);
            });
            // The Olympus GUI art and lang (dropdown chevrons, modal, colour picker), from the Olympus jar upstream nests.
            UpstreamJars.olympus().forEach(assets::putIfAbsent);
            flattenRichLang(assets);
            aliasWthitConfigNames(assets);
            Tempad.LOGGER.info("Tempad upstream assets: {} files", assets.size());
        }
        return assets;
    }

    public static synchronized Map<String, byte[]> data() {
        if (data == null) {
            data = new HashMap<>();
            Set<String> dropped = new TreeSet<>();
            UpstreamJars.tempad304().forEach((path, bytes) -> {
                if (!path.startsWith("data/")) return;
                String reason = skipReason(path);
                if (reason != null) {
                    dropped.add(path + " (" + reason + ")");
                    return;
                }
                String target = convertPath(path);
                if (!target.endsWith(".json")) {
                    data.put(target, bytes);
                    return;
                }
                JsonElement json;
                try {
                    json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
                } catch (RuntimeException e) {
                    dropped.add(path + " (unreadable JSON)");
                    return;
                }
                JsonObject converted = target.contains("/tags/") ? convertTag(json.getAsJsonObject())
                        : target.contains("/recipes/") ? convertRecipe(json.getAsJsonObject()) : json.getAsJsonObject();
                if (converted == null) {
                    dropped.add(path + " (empty after removing unregistered entries)");
                    return;
                }
                String missing = firstUnregistered(converted, null);
                if (missing != null) {
                    dropped.add(path + " (names " + missing + ", not registered in this phase)");
                    return;
                }
                data.put(target, GSON.toJson(converted).getBytes(StandardCharsets.UTF_8));
            });
            Tempad.LOGGER.info("Tempad upstream data: {} files served, {} dropped", data.size(), dropped.size());
            dropped.forEach(line -> Tempad.LOGGER.debug("Tempad upstream data dropped: {}", line));
        }
        return data;
    }

    static String skipReason(String path) {
        if (path.contains("/advancement/") || path.contains("/advancements/")) return "advancement";
        if (path.startsWith("data/neoforge/")) return "NeoForge global loot modifier list";
        if (path.startsWith("data/tempad/curios/")) return "Curios 1.20+ slot data, slots come by IMC";
        return null;
    }

    /**
     * Resourceful Lib lets a lang line be a component array (text parts with colours). 1.19.2's Language.loadFromJson
     * only takes strings and stops at the first array, so the rest of the file never loads (in tempad_static: the
     * timedoor failure messages, "location saved", the player error). Each array line becomes the plain concatenation
     * of its parts' text; the part colours are dropped.
     */
    static void flattenRichLang(Map<String, byte[]> assets) {
        for (Map.Entry<String, byte[]> entry : assets.entrySet()) {
            String path = entry.getKey();
            if (!path.startsWith("assets/") || !path.contains("/lang/") || !path.endsWith(".json")) continue;
            JsonObject lang;
            try {
                lang = JsonParser.parseString(new String(entry.getValue(), StandardCharsets.UTF_8)).getAsJsonObject();
            } catch (RuntimeException e) {
                continue;
            }
            boolean changed = false;
            JsonObject out = new JsonObject();
            for (Map.Entry<String, JsonElement> line : lang.entrySet()) {
                JsonElement value = line.getValue();
                if (value.isJsonArray() || value.isJsonObject()) {
                    StringBuilder text = new StringBuilder();
                    appendText(value, text);
                    value = new com.google.gson.JsonPrimitive(text.toString());
                    changed = true;
                }
                out.add(line.getKey(), value);
            }
            if (changed) entry.setValue(GSON.toJson(out).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void appendText(JsonElement part, StringBuilder text) {
        if (part.isJsonPrimitive()) {
            text.append(part.getAsString());
        } else if (part.isJsonArray()) {
            for (JsonElement child : part.getAsJsonArray()) appendText(child, text);
        } else if (part.isJsonObject()) {
            JsonObject object = part.getAsJsonObject();
            if (object.has("text")) text.append(object.get("text").getAsString());
            if (object.has("extra")) appendText(object.get("extra"), text);
        }
    }

    /** Upstream's Jade toggle names (tempad_static lang) also under WTHIT's key, config.waila.plugin_<namespace>.<path>. */
    static void aliasWthitConfigNames(Map<String, byte[]> assets) {
        String jade = "config.jade.plugin_tempad.";
        String waila = "config.waila.plugin_tempad.";
        for (Map.Entry<String, byte[]> entry : assets.entrySet()) {
            String path = entry.getKey();
            if (!path.startsWith("assets/tempad_static/lang/") || !path.endsWith(".json")) continue;
            JsonObject lang;
            try {
                lang = JsonParser.parseString(new String(entry.getValue(), StandardCharsets.UTF_8)).getAsJsonObject();
            } catch (RuntimeException e) {
                continue;
            }
            JsonObject out = lang.deepCopy();
            for (Map.Entry<String, JsonElement> line : lang.entrySet()) {
                String key = line.getKey();
                if (key.startsWith(jade) && !out.has(waila + key.substring(jade.length()))) out.add(waila + key.substring(jade.length()), line.getValue());
            }
            entry.setValue(GSON.toJson(out).getBytes(StandardCharsets.UTF_8));
        }
    }

    static String convertPath(String path) {
        String[] parts = path.split("/", 4); // data, namespace, folder, rest
        if (parts.length < 4) return path;
        String namespace = parts[1];
        String rest = parts[2] + "/" + parts[3];
        if (rest.startsWith("recipe/")) rest = "recipes/" + rest.substring("recipe/".length());
        else if (rest.startsWith("loot_table/")) rest = "loot_tables/" + rest.substring("loot_table/".length());
        else if (rest.startsWith("tags/item/")) rest = "tags/items/" + rest.substring("tags/item/".length());
        else if (rest.startsWith("tags/block/")) rest = "tags/blocks/" + rest.substring("tags/block/".length());
        else if (rest.startsWith("tags/entity_type/")) rest = "tags/entity_types/" + rest.substring("tags/entity_type/".length());
        if (namespace.equals("c") && rest.startsWith("tags/")) {
            // data/c/tags/<type>/<path>.json -> the forge: tag C_TAGS maps it to
            String type = rest.substring("tags/".length(), rest.indexOf('/', "tags/".length()));
            String tagPath = rest.substring(("tags/" + type + "/").length(), rest.length() - ".json".length());
            String mapped = mapCTag("c:" + tagPath);
            ResourceLocation id = new ResourceLocation(mapped);
            return "data/" + id.getNamespace() + "/tags/" + type + "/" + id.getPath() + ".json";
        }
        return "data/" + namespace + "/" + rest;
    }

    static String mapCTag(String tag) {
        String mapped = C_TAGS.get(tag);
        if (mapped != null) return mapped;
        String path = tag.substring(2);
        if (path.startsWith("glass_blocks/")) path = "glass/" + path.substring("glass_blocks/".length());
        Tempad.LOGGER.warn("Tempad upstream data: no pack mapping for #{}, using #forge:{}", tag, path);
        return "forge:" + path;
    }

    /** Tags: c: references -> optional forge: references; unregistered tempad: entries removed; null when empty. */
    static JsonObject convertTag(JsonObject json) {
        JsonArray values = json.getAsJsonArray("values");
        if (values == null) return json;
        JsonArray out = new JsonArray();
        for (JsonElement value : values) {
            String id = value.isJsonPrimitive() ? value.getAsString()
                    : value.isJsonObject() && value.getAsJsonObject().has("id") ? value.getAsJsonObject().get("id").getAsString() : null;
            if (id == null) {
                out.add(value);
                continue;
            }
            if (id.startsWith("#c:")) {
                JsonObject optional = new JsonObject();
                optional.addProperty("id", "#" + mapCTag(id.substring(1)));
                optional.addProperty("required", false);
                out.add(optional);
            } else if (id.startsWith("tempad:") && !isRegistered(id)) {
                // phased port: entry for an item, block or entity of a later phase
            } else {
                out.add(value);
            }
        }
        if (out.size() == 0) return null;
        JsonObject result = json.deepCopy();
        result.add("values", out);
        return result;
    }

    static JsonObject convertRecipe(JsonObject json) {
        JsonObject result = json.deepCopy();
        result.remove("category");
        List<JsonElement> conditions = new ArrayList<>();
        if (result.has("neoforge:conditions")) {
            for (JsonElement condition : result.getAsJsonArray("neoforge:conditions")) conditions.add(convertCondition(condition));
            result.remove("neoforge:conditions");
        }
        convertResults(result);
        rewriteCTags(result);
        if (mentions(result, NEW_LOCATION_UPGRADE)) {
            JsonObject condition = new JsonObject();
            condition.addProperty("type", RequiredLocationUpgradeCondition.ID.toString());
            conditions.add(condition);
        }
        if (!conditions.isEmpty()) {
            JsonArray array = new JsonArray();
            conditions.forEach(array::add);
            result.add("conditions", array);
        }
        return result;
    }

    private static JsonElement convertCondition(JsonElement element) {
        if (!element.isJsonObject()) return element;
        JsonObject condition = element.getAsJsonObject().deepCopy();
        String type = condition.has("type") ? condition.get("type").getAsString() : "";
        switch (type) {
            case "neoforge:mod_loaded" -> condition.addProperty("type", "forge:mod_loaded");
            case "neoforge:not" -> {
                condition.addProperty("type", "forge:not");
                condition.add("value", convertCondition(condition.get("value")));
            }
            case "neoforge:and", "neoforge:or" -> {
                condition.addProperty("type", "forge:" + type.substring("neoforge:".length()));
                JsonArray values = new JsonArray();
                for (JsonElement inner : condition.getAsJsonArray("values")) values.add(convertCondition(inner));
                condition.add("values", values);
            }
            default -> {
            }
        }
        return condition;
    }

    /** 1.21 ItemStack JSON {"id", "count"} -> 1.19.2 {"item", "count"} in "result" and "results". */
    private static void convertResults(JsonObject recipe) {
        if (recipe.has("result") && recipe.get("result").isJsonObject()) renameId(recipe.getAsJsonObject("result"));
        if (recipe.has("results") && recipe.get("results").isJsonArray()) {
            for (JsonElement entry : recipe.getAsJsonArray("results")) {
                if (entry.isJsonObject()) renameId(entry.getAsJsonObject());
            }
        }
    }

    private static void renameId(JsonObject stack) {
        if (stack.has("id") && !stack.has("item")) {
            stack.add("item", stack.get("id"));
            stack.remove("id");
        }
    }

    private static void rewriteCTags(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                JsonElement value = entry.getValue();
                if (entry.getKey().equals("tag") && value.isJsonPrimitive() && value.getAsString().startsWith("c:")) {
                    entry.setValue(new JsonPrimitive(mapCTag(value.getAsString())));
                } else {
                    rewriteCTags(value);
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement inner : element.getAsJsonArray()) rewriteCTags(inner);
        }
    }

    private static boolean mentions(JsonElement element, String id) {
        if (element.isJsonPrimitive()) return element.getAsString().equals(id);
        if (element.isJsonArray()) {
            for (JsonElement inner : element.getAsJsonArray()) {
                if (mentions(inner, id)) return true;
            }
        } else if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> inner : element.getAsJsonObject().entrySet()) {
                if (mentions(inner.getValue(), id)) return true;
            }
        }
        return false;
    }

    /** The first tempad: id under a registry-id key that this build does not register, or null. */
    private static String firstUnregistered(JsonElement element, String key) {
        if (element.isJsonPrimitive()) {
            if (key != null && NON_REGISTRY_KEYS.contains(key)) return null;
            String value = element.getAsString().startsWith("#") ? null : element.getAsString();
            return value != null && value.startsWith("tempad:") && !isRegistered(value) ? value : null;
        }
        if (element.isJsonArray()) {
            for (JsonElement inner : element.getAsJsonArray()) {
                String found = firstUnregistered(inner, key);
                if (found != null) return found;
            }
        } else if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                String found = firstUnregistered(entry.getValue(), entry.getKey());
                if (found != null) return found;
            }
        }
        return null;
    }

    private static boolean isRegistered(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) return false;
        return ForgeRegistries.ITEMS.containsKey(location) || ForgeRegistries.BLOCKS.containsKey(location)
                || ForgeRegistries.ENTITY_TYPES.containsKey(location);
    }
}
