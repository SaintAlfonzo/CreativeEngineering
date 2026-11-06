package com.SaintAlfonzo.creativedelight.largemeals.upstream;

import com.SaintAlfonzo.creativedelight.largemeals.LargeMealsMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The in-memory view of the upstream jar (feature_writeup/large_meals.md 4): its assets as the client pack, and from its
 * data only what still means the same on 1.19.2 and in the pack: the three block loot tables (servings 4 drops the feast,
 * otherwise a bowl) and the two sheep knife-drop loot modifiers with the global modifier list that names them (D3).
 * Upstream recipes (replaced by the pack's scripts), advancements (the pack drops them) and Diet tags (no Diet mod) are
 * not loaded. Blockstates and models need no change: the feasts keep Farmer's Delight's {@code facing} and
 * {@code servings} properties.
 */
public final class LargeMealsTranslator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String LANG = "assets/largemeals/lang/en_us.json";
    /** Names fixed or added by the port (D6, large_meals.md 3.5 B1) and the tab name. */
    private static final Map<String, String> LANG_SET = Map.of(
            "item.largemeals.mushroom_pot_pie", "Bowl of Mushroom Pot Pie",
            "item.largemeals.roasted_mutton_rack", "Bowl of Roasted Rack of Mutton",
            "itemGroup.largemeals", "Large Meals");
    private static final List<String> LANG_DROP = List.of("largemeals.advancement.get_mutton_rack", "largemeals.advancement.get_mutton_rack.desc");
    private static final List<String> DATA_KEEP = List.of("data/largemeals/loot_tables/", "data/largemeals/loot_modifiers/",
            "data/forge/loot_modifiers/");

    private static Map<String, byte[]> assets;
    private static Map<String, byte[]> data;

    private LargeMealsTranslator() {
    }

    public static synchronized Map<String, byte[]> assets() {
        if (assets == null) {
            assets = new HashMap<>();
            UpstreamJars.largemeals130().forEach((path, bytes) -> {
                if (!path.startsWith("assets/")) return;
                if (path.equals(LANG)) bytes = lang(bytes);
                else if (path.startsWith("assets/largemeals/lang/") && !path.equals(LANG)) return;
                assets.put(path, bytes);
            });
            LargeMealsMod.LOGGER.info("Large Meals upstream assets: {} files", assets.size());
        }
        return assets;
    }

    public static synchronized Map<String, byte[]> data() {
        if (data == null) {
            data = new HashMap<>();
            UpstreamJars.largemeals130().forEach((path, bytes) -> {
                if (DATA_KEEP.stream().anyMatch(path::startsWith)) data.put(path, bytes);
            });
        }
        return data;
    }

    private static byte[] lang(byte[] bytes) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        if (text.startsWith("﻿")) text = text.substring(1);
        JsonObject lang = JsonParser.parseString(text).getAsJsonObject();
        LANG_DROP.forEach(lang::remove);
        LANG_SET.forEach(lang::addProperty);
        return GSON.toJson(lang).getBytes(StandardCharsets.UTF_8);
    }
}
