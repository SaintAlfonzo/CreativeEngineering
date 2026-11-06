package com.SaintAlfonzo.creativedelight.golden.upstream;

import com.SaintAlfonzo.creativedelight.golden.GoldenFoodMod;

import java.util.HashMap;
import java.util.Map;

/**
 * The in-memory view of the upstream jar (feature_writeup/golden_food.md). Upstream is a native Forge 1.19.2 build with
 * the same ids as this rebuild, so its files are used as they are, with two changes:
 * <ul>
 * <li>the generator script upstream left under {@code assets/goldenfood/python/} is not served (not a resource);</li>
 * <li>the 16 village chest table copies ({@code data/minecraft/loot_tables/chests/village/}) are not served: each is the
 * vanilla table plus one Golden Food Enchantment book entry (checked 2026-11-02), and the enchantment is removed.</li>
 * </ul>
 * The 24 crafting recipes (all in the {@code minecraft} namespace; one replaces vanilla's golden carrot recipe) are
 * served as they are; the pack's recipe scripts handle them as they handled the upstream jar.
 */
public final class GoldenTranslator {
    private static Map<String, byte[]> assets;
    private static Map<String, byte[]> data;

    private GoldenTranslator() {
    }

    public static synchronized Map<String, byte[]> assets() {
        if (assets == null) {
            assets = new HashMap<>();
            UpstreamJars.golden200().forEach((path, bytes) -> {
                if (path.startsWith("assets/") && !skipAsset(path)) assets.put(path, bytes);
            });
            GoldenFoodMod.LOGGER.info("Golden Food upstream assets: {} files", assets.size());
        }
        return assets;
    }

    public static synchronized Map<String, byte[]> data() {
        if (data == null) {
            data = new HashMap<>();
            UpstreamJars.golden200().forEach((path, bytes) -> {
                if (path.startsWith("data/") && !skipData(path)) data.put(path, bytes);
            });
            GoldenFoodMod.LOGGER.info("Golden Food upstream data: {} files", data.size());
        }
        return data;
    }

    static boolean skipData(String path) {
        return path.startsWith("data/minecraft/loot_tables/chests/village/");
    }

    static boolean skipAsset(String path) {
        return path.startsWith("assets/goldenfood/python/");
    }
}
