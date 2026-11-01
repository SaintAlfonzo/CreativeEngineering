package net.satisfy.vinery.forge.upstream;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The B4.1 id map: 1.5.5 path -> the id the port registers (D1, D2). Applied to every id in upstream data, and in
 * reverse to find the 1.5.5 art for a port id ({@link #assetAliases()}).
 */
public final class IdMap {
    /** 1.5.5 path -> port path (vinery namespace). */
    private static final Map<String, String> RENAMES = new LinkedHashMap<>();
    /** 1.5.5 ids with no port counterpart (dropped; anything naming them is left out). */
    public static final Set<String> DROPPED = Set.of("dark_cherry_hanging_sign", "dark_cherry_wall_hanging_sign");

    static {
        for (String part : new String[]{"planks", "log", "wood", "beam", "floorboard", "stairs", "slab", "fence", "fence_gate",
                "button", "pressure_plate", "door", "trapdoor", "sign", "wall_sign", "leaves", "sapling", "boat", "chest_boat",
                "barrel", "cabinet", "drawer", "chair", "table", "big_table", "shelf", "lattice", "wine_rack_big",
                "wine_rack_small", "wine_rack_mid"}) {
            RENAMES.put("dark_cherry_" + part, "cherry_" + part);
        }
        RENAMES.put("stripped_dark_cherry_log", "stripped_cherry_log");
        RENAMES.put("stripped_dark_cherry_wood", "stripped_cherry_wood");
        RENAMES.put("potted_dark_cherry_tree_sapling", "potted_cherry_tree_sapling");
        RENAMES.put("apple_press", "wine_press");
        RENAMES.put("red_grapejuice", "red_grapejuice_wine_bottle");
        RENAMES.put("white_grapejuice", "white_grapejuice_wine_bottle");
        for (String biome : new String[]{"savanna", "taiga", "jungle"}) {
            RENAMES.put("red_" + biome + "_grapejuice", biome + "_red_grapejuice_bottle");
            RENAMES.put("white_" + biome + "_grapejuice", biome + "_white_grapejuice_bottle");
        }
        RENAMES.put("winemaker_apron", "vinemaker_apron");
        RENAMES.put("winemaker_leggings", "vinemaker_leggings");
        RENAMES.put("winemaker_boots", "vinemaker_boots");
    }

    private IdMap() {
    }

    /** {@code vinery:<1.5.5 path>} -> {@code vinery:<port path>}; anything else unchanged. Tags ({@code #...}) are never renamed. */
    public static String rewrite(String value) {
        if (!value.startsWith("vinery:")) {
            return value;
        }
        String path = value.substring("vinery:".length());
        String renamed = RENAMES.get(path);
        if (renamed != null) {
            return "vinery:" + renamed;
        }
        if (path.startsWith("blocks/")) {
            String block = RENAMES.get(path.substring("blocks/".length()));
            if (block != null) {
                return "vinery:blocks/" + block;
            }
        }
        return value;
    }

    /** 1.5.5 path -> port path, for a file name. */
    public static String renamePath(String path) {
        return RENAMES.getOrDefault(path, path);
    }

    /** port path -> 1.5.5 path whose blockstate, item model and lang name the port id uses. */
    public static Map<String, String> assetAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        RENAMES.forEach((upstream, port) -> aliases.put(port, upstream));
        return aliases;
    }
}
