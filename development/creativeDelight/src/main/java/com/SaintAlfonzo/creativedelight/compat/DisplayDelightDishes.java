package com.SaintAlfonzo.creativedelight.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

/**
 * Display Delight placed blocks for foods Display Delight 1.2.0 has none for (food_families.md 3.1 {@code egg/dish},
 * placeable_food.md). The list is {@code creativedelight/displaydelight_dishes.json} in this jar; each entry is a
 * Display Delight {@code FoodBlock} named {@code creativeengineering:<block>}, linked to its food on server world load
 * the way Display Delight links its own (sneak + right click a block top with the food puts it down; an empty hand
 * takes it back). The food also needs {@code #displaydelight:displayable} (data/displaydelight/tags/items in this jar).
 * Blockstates, models, loot tables, the tag and the names are written by tools/gen_dd_dishes.py from the same list.
 *
 * Without Display Delight nothing is registered. This class names no Display Delight class; that is
 * {@link DisplayDelightDishBlocks}, loaded only when the mod is.
 */
public final class DisplayDelightDishes {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LIST = "/creativedelight/displaydelight_dishes.json";

    /** (block path, food item id) from the list. */
    public record Dish(String block, ResourceLocation item) {}

    /** The dish block ids; empty without Display Delight. */
    private static final Set<ResourceLocation> BLOCK_IDS = ConcurrentHashMap.newKeySet();

    private DisplayDelightDishes() {}

    public static void register(IEventBus modEventBus) {
        if (!ModList.get().isLoaded("displaydelight")) return;
        List<Dish> dishes = read();
        dishes.forEach(d -> BLOCK_IDS.add(new ResourceLocation(CreativeDelight.MOD_ID, d.block())));
        DisplayDelightDishBlocks.register(modEventBus, dishes);
    }

    public static boolean isDish(ResourceLocation blockId) {
        return BLOCK_IDS.contains(blockId);
    }

    private static List<Dish> read() {
        List<Dish> out = new ArrayList<>();
        try (InputStream in = DisplayDelightDishes.class.getResourceAsStream(LIST)) {
            if (in == null) {
                LOGGER.error("[creativeengineering] {} missing from the jar: no Display Delight dishes", LIST);
                return out;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            for (JsonElement e : root.getAsJsonArray("dishes")) {
                JsonObject o = e.getAsJsonObject();
                out.add(new Dish(o.get("block").getAsString(), new ResourceLocation(o.get("item").getAsString())));
            }
        } catch (Exception e) {
            LOGGER.error("[creativeengineering] could not read {}: {}", LIST, e.toString());
        }
        return Collections.unmodifiableList(out);
    }
}
