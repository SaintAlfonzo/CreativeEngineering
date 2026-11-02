package com.SaintAlfonzo.creativedelight.roll.upstream;

import com.SaintAlfonzo.creativedelight.roll.RollDelightMod;
import com.SaintAlfonzo.creativedelight.roll.content.RollMedleyBlock;
import com.SaintAlfonzo.creativedelight.roll.content.WrappedChopsticksBlock;
import com.SaintAlfonzo.creativedelight.roll.registry.RollBlocks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * The in-memory view of the upstream jar (feature_writeup/roll_delight.md 4): its assets as the client pack, translated
 * to what the port registers on 1.19.2. Upstream data is not used: its recipes are replaced by the pack's scripts, its
 * enchantable tag is the 1.21 mechanism (B8) and its block loot tables are empty (the MCreator procedures dropped the
 * items). The data pack holds only our own loot tables, written here.
 */
public final class RollTranslator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String LANG = "assets/roll_delight/lang/en_us.json";
    /** Names fixed or changed by the port (B1, D4), and the keys of our own lines (3.3). */
    private static final Map<String, String> LANG_SET = Map.of(
            "item.roll_delight.rice_roll_slice", "Rice Roll Slice",
            "item.roll_delight.amazing_salmon_roll", "Salmon Maki Roll",
            "item.roll_delight.amazing_salmon_roll_slice", "Salmon Maki",
            "itemGroup.roll_delight", "Roll Delight",
            "item.roll_delight.chopsticks.tooltip.0", "Lengthens effects of a food when eaten in offhand",
            "item.roll_delight.chopsticks.tooltip.1", "Get the juices and sauces in your mouth, not on your hands",
            "tooltip.roll_delight.chopstick_food", "Can be eaten in the offhand w/ chopsticks",
            "tooltip.roll_delight.chopstick_food.effects", "Can be eaten in the offhand w/ chopsticks, lengthens effect length");
    private static final List<String> LANG_DROP = List.of("item.roll_delight.chopsticks.description_0", "item_group.roll_delight.roll_delight");

    private static Map<String, byte[]> assets;
    private static Map<String, byte[]> data;

    private RollTranslator() {
    }

    public static synchronized Map<String, byte[]> assets() {
        if (assets == null) {
            assets = new HashMap<>();
            UpstreamJars.roll131().forEach((path, bytes) -> {
                if (!path.startsWith("assets/")) return;
                if (path.equals(LANG)) {
                    bytes = lang(bytes);
                } else if (path.equals("assets/roll_delight/blockstates/legendary_roll_medley.json")
                        || path.equals("assets/roll_delight/blockstates/mythical_roll_medley.json")) {
                    bytes = blockstate(bytes, b -> RollMedleyBlock.SERVINGS.getName() + "=" + (RollMedleyBlock.MAX - b));
                } else if (path.equals("assets/roll_delight/blockstates/wrapped_chopsticks.json")) {
                    bytes = blockstate(bytes, b -> WrappedChopsticksBlock.PAIRS.getName() + "=" + (b + 1));
                }
                assets.put(path, bytes);
            });
            RollDelightMod.LOGGER.info("Roll Delight upstream assets: {} files", assets.size());
        }
        return assets;
    }

    /** Built when the server first reads data, after every registry is complete. */
    public static synchronized Map<String, byte[]> data() {
        if (data == null) {
            data = new HashMap<>();
            for (var medley : List.of(RollBlocks.LEGENDARY_ROLL_MEDLEY, RollBlocks.MYTHICAL_ROLL_MEDLEY)) {
                data.put(loot(medley.getId().getPath()), json(medleyLoot(medley.getId().toString(), (RollMedleyBlock) medley.get())));
            }
            data.put(loot("wrapped_chopsticks"), json(wrappedLoot()));
        }
        return data;
    }

    private static byte[] lang(byte[] bytes) {
        JsonObject lang = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        LANG_DROP.forEach(lang::remove);
        LANG_SET.forEach(lang::addProperty);
        return json(lang);
    }

    /** Upstream keys its states by an MCreator {@code blockstate} integer; the port's blocks have named properties. */
    private static byte[] blockstate(byte[] bytes, IntFunction<String> property) {
        JsonObject root = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject variants = root.getAsJsonObject("variants");
        JsonObject out = new JsonObject();
        for (Entry<String, JsonElement> variant : variants.entrySet()) {
            StringBuilder key = new StringBuilder();
            for (String part : variant.getKey().split(",")) {
                if (key.length() > 0) key.append(',');
                key.append(part.startsWith("blockstate=") ? property.apply(Integer.parseInt(part.substring("blockstate=".length()))) : part);
            }
            out.add(key.toString(), variant.getValue());
        }
        root.add("variants", out);
        return json(root);
    }

    /**
     * Full: the medley itself. Part-eaten or empty: the bowl, plus every serving still on the plate (servings 1 to 7;
     * the serving at index i is there while more than i are left).
     */
    private static JsonObject medleyLoot(String block, RollMedleyBlock medley) {
        JsonArray pools = new JsonArray();
        pools.add(pool(itemEntry(block), stateRange(block, RollMedleyBlock.MAX, RollMedleyBlock.MAX)));
        pools.add(pool(itemEntry("minecraft:bowl"), stateRange(block, 0, RollMedleyBlock.MAX - 1)));
        List<Supplier<Item>> servings = medley.servings();
        for (int i = 0; i < RollMedleyBlock.MAX - 1; i++) {
            pools.add(pool(itemEntry(id(servings.get(i).get())), stateRange(block, i + 1, RollMedleyBlock.MAX - 1)));
        }
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");
        root.add("pools", pools);
        return root;
    }

    /** One wrapped pair per pair held. */
    private static JsonObject wrappedLoot() {
        String block = RollBlocks.WRAPPED_CHOPSTICKS.getId().toString();
        JsonObject entry = itemEntry(block);
        JsonArray functions = new JsonArray();
        for (int n = 2; n <= WrappedChopsticksBlock.MAX; n++) {
            JsonObject setCount = new JsonObject();
            setCount.addProperty("function", "minecraft:set_count");
            setCount.addProperty("count", n);
            JsonArray conditions = new JsonArray();
            JsonObject exact = new JsonObject();
            exact.addProperty("condition", "minecraft:block_state_property");
            exact.addProperty("block", block);
            JsonObject properties = new JsonObject();
            properties.addProperty(WrappedChopsticksBlock.PAIRS.getName(), String.valueOf(n));
            exact.add("properties", properties);
            conditions.add(exact);
            setCount.add("conditions", conditions);
            functions.add(setCount);
        }
        entry.add("functions", functions);
        JsonObject survives = new JsonObject();
        survives.addProperty("condition", "minecraft:survives_explosion");
        JsonArray pools = new JsonArray();
        pools.add(pool(entry, survives));
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");
        root.add("pools", pools);
        return root;
    }

    private static JsonObject pool(JsonObject entry, JsonObject condition) {
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        JsonArray entries = new JsonArray();
        entries.add(entry);
        pool.add("entries", entries);
        JsonArray conditions = new JsonArray();
        conditions.add(condition);
        pool.add("conditions", conditions);
        return pool;
    }

    private static JsonObject itemEntry(String item) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", item);
        return entry;
    }

    private static JsonObject stateRange(String block, int min, int max) {
        JsonObject range = new JsonObject();
        range.addProperty("min", String.valueOf(min));
        range.addProperty("max", String.valueOf(max));
        JsonObject properties = new JsonObject();
        properties.add(RollMedleyBlock.SERVINGS.getName(), range);
        JsonObject condition = new JsonObject();
        condition.addProperty("condition", "minecraft:block_state_property");
        condition.addProperty("block", block);
        condition.add("properties", properties);
        return condition;
    }

    private static String id(Item item) {
        return String.valueOf(ForgeRegistries.ITEMS.getKey(item));
    }

    private static String loot(String block) {
        return "data/roll_delight/loot_tables/blocks/" + block + ".json";
    }

    private static byte[] json(JsonElement element) {
        return GSON.toJson(element).getBytes(StandardCharsets.UTF_8);
    }
}
