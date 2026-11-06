package com.SaintAlfonzo.creativedelight.oceanic.upstream;

import com.SaintAlfonzo.creativedelight.oceanic.OceanicDelightMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The in-memory view of the upstream jar (feature_writeup/ocean_delight.md 5.6). Upstream is a native Forge 1.19.2 build
 * with the same ids as this rebuild, so its files are used as they are, with three changes:
 * <ul>
 * <li>the English lang gains the keys of our own lines (the bowl message, the effect tooltips);</li>
 * <li>advancements are left out (the pack drops them all at load; the rebuild grants none);</li>
 * <li>the two fishing net tables are left out: the net rolls the pack's tables (Creative Delight,
 * {@code creativedelight:gameplay/fishing_net/*}), so the upstream ones would only be dead data.</li>
 * </ul>
 */
public final class OceanicTranslator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String LANG = "assets/oceanic_delight/lang/en_us.json";
    /** Keys of our own lines, added to upstream's English lang. */
    private static final Map<String, String> LANG_ADD = Map.of(
            "message.oceanic_delight.needs_bowl", "You need a Bowl to eat this.",
            "tooltip.oceanic_delight.effect", "%s (%s)");

    private static Map<String, byte[]> assets;
    private static Map<String, byte[]> data;

    private OceanicTranslator() {
    }

    public static synchronized Map<String, byte[]> assets() {
        if (assets == null) {
            assets = new HashMap<>();
            UpstreamJars.oceanic101().forEach((path, bytes) -> {
                if (!path.startsWith("assets/")) return;
                assets.put(path, path.equals(LANG) ? lang(bytes) : bytes);
            });
            OceanicDelightMod.LOGGER.info("Oceanic Delight upstream assets: {} files", assets.size());
        }
        return assets;
    }

    public static synchronized Map<String, byte[]> data() {
        if (data == null) {
            data = new HashMap<>();
            UpstreamJars.oceanic101().forEach((path, bytes) -> {
                if (!path.startsWith("data/") || skipData(path)) return;
                data.put(path, bytes);
            });
            OceanicDelightMod.LOGGER.info("Oceanic Delight upstream data: {} files", data.size());
        }
        return data;
    }

    static boolean skipData(String path) {
        return path.startsWith("data/oceanic_delight/advancements/")
                || path.startsWith("data/oceanic_delight/loot_tables/gameplay/fishing_net_");
    }

    private static byte[] lang(byte[] bytes) {
        JsonObject lang = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        LANG_ADD.forEach(lang::addProperty);
        return GSON.toJson(lang).getBytes(StandardCharsets.UTF_8);
    }
}
