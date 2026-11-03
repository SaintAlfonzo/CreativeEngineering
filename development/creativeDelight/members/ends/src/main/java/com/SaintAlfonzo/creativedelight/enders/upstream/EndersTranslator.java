package com.SaintAlfonzo.creativedelight.enders.upstream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Picks this member's files out of the Ender's Delight 1.1.4 donor jar and turns them into 1.19.2 data
 * (feature_writeup/ends_delight.md 5.3). Only the listed paths are taken: Ender's Delight 1.2.2 is installed under the same
 * namespace, and none of these paths exists in its jar, so its own look and data stay untouched.
 * <p>
 * Changes made in memory (the same edits the member shipped as files until 2026-11-01):
 * <ul>
 * <li>{@code voidpepper_block} blockstate: every multipart case states {@code pepper} ({@code true} for the
 * {@code voidpepper_on} model, else {@code false}), so the plain and the fruiting vine never draw over each other.</li>
 * <li>{@code end_house} structure: 1.1.4's own structure type becomes {@code minecraft:jigsaw}; biome ids get their
 * {@code minecraft:} namespace.</li>
 * <li>Block loot tables: the 1.20 {@code random_sequence} field dropped, {@code type} set to {@code minecraft:block};
 * the Chorus Crate's pool gets {@code survives_explosion} like every other block table.</li>
 * </ul>
 */
public final class EndersTranslator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final String A = "assets/endersdelight/";
    private static final String D = "data/endersdelight/";

    /** Every donor file the member uses: its own blocks and items, plus loot tables for two 1.2.2 blocks that have none. */
    public static final List<String> ASSETS = List.of(
            A + "blockstates/amberveil_mushroom.json", A + "blockstates/chorus_flame_block.json",
            A + "blockstates/ethereal_saffron_bush.json", A + "blockstates/voidpepper_block.json",
            A + "models/block/amberveil_mushroom.json", A + "models/block/chorusflame_bush_0.json",
            A + "models/block/chorusflame_bush_1.json", A + "models/block/chorusflame_bush_2.json",
            A + "models/block/ethereal_saffron_bush.json", A + "models/block/voidpepper.json", A + "models/block/voidpepper_on.json",
            A + "models/item/amberveiled_curry.json", A + "models/item/amberveil.json", A + "models/item/amberveil_stew.json",
            A + "models/item/chicken_curry.json", A + "models/item/chorusflame.json", A + "models/item/crushed_voidpepper.json",
            A + "models/item/ethereal_saffron.json", A + "models/item/steak_fries.json", A + "models/item/veil_of_flames_risotto.json",
            A + "models/item/voidpepper.json",
            A + "textures/block/amberveil.png", A + "textures/block/chorusflame_0.png", A + "textures/block/chorusflame_1.png",
            A + "textures/block/chorusflame_2.png", A + "textures/block/ethereal_saffron_block.png",
            A + "textures/block/voidpepper_vines_0.png", A + "textures/block/voidpepper_vines_1.png",
            A + "textures/item/amberveiled_curry.png", A + "textures/item/amberveils.png", A + "textures/item/amberveil_stew.png",
            A + "textures/item/chicken_curry.png", A + "textures/item/chorusflame.png", A + "textures/item/ethereal_saffron.png",
            A + "textures/item/steak_fries.png", A + "textures/item/veil_of_flames_risotto.png", A + "textures/item/voidpepper.png",
            A + "textures/item/voidpepper_sprout.png");

    public static final List<String> DATA = List.of(
            D + "loot_tables/blocks/amberveil_mushroom.json", D + "loot_tables/blocks/chorus_crate.json",
            D + "loot_tables/blocks/chorus_flame_block.json", D + "loot_tables/blocks/endstone_stove.json",
            D + "loot_tables/blocks/ethereal_saffron_bush.json", D + "loot_tables/blocks/voidpepper_block.json",
            D + "structures/end_house.nbt", D + "worldgen/structure/end_house.json",
            D + "worldgen/structure_set/end_house.json", D + "worldgen/template_pool/end_house.json");

    private static final Set<String> SURVIVES_EXPLOSION = Set.of(D + "loot_tables/blocks/chorus_crate.json");

    private static Map<String, byte[]> assets;
    private static Map<String, byte[]> data;

    private EndersTranslator() {
    }

    public static synchronized Map<String, byte[]> assets() {
        if (assets == null) assets = translate(ASSETS);
        return assets;
    }

    public static synchronized Map<String, byte[]> data() {
        if (data == null) data = translate(DATA);
        return data;
    }

    private static Map<String, byte[]> translate(List<String> paths) {
        Map<String, byte[]> read = UpstreamJars.enders114(paths.toArray(String[]::new));
        Map<String, byte[]> out = new HashMap<>();
        for (String path : paths) {
            byte[] bytes = read.get(path);
            if (bytes == null) throw new IllegalStateException("Ender's Delight 1.1.4 donor jar has no " + path);
            out.put(path, translate(path, bytes));
        }
        return out;
    }

    /** One file; public for the offline harness. */
    public static byte[] translate(String path, byte[] bytes) {
        if (path.equals(A + "blockstates/voidpepper_block.json")) {
            JsonObject root = parse(bytes);
            for (JsonElement part : root.getAsJsonArray("multipart")) {
                JsonObject o = part.getAsJsonObject();
                boolean on = o.getAsJsonObject("apply").get("model").getAsString().endsWith("/voidpepper_on");
                o.getAsJsonObject("when").addProperty("pepper", on ? "true" : "false");
            }
            return write(root);
        }
        if (path.equals(D + "worldgen/structure/end_house.json")) {
            JsonObject root = parse(bytes);
            root.addProperty("type", "minecraft:jigsaw");
            JsonArray biomes = new JsonArray();
            for (JsonElement b : root.getAsJsonArray("biomes")) {
                String id = b.getAsString();
                biomes.add(id.contains(":") ? id : "minecraft:" + id);
            }
            root.add("biomes", biomes);
            return write(root);
        }
        if (path.startsWith(D + "loot_tables/blocks/")) {
            JsonObject root = parse(bytes);
            root.remove("random_sequence");
            JsonObject typed = new JsonObject();
            typed.addProperty("type", "minecraft:block");
            root.entrySet().forEach(e -> { if (!e.getKey().equals("type")) typed.add(e.getKey(), e.getValue()); });
            if (SURVIVES_EXPLOSION.contains(path)) {
                for (JsonElement pool : typed.getAsJsonArray("pools")) {
                    JsonObject p = pool.getAsJsonObject();
                    if (!p.has("conditions")) {
                        JsonArray conditions = new JsonArray();
                        JsonObject c = new JsonObject();
                        c.addProperty("condition", "minecraft:survives_explosion");
                        conditions.add(c);
                        p.add("conditions", conditions);
                    }
                }
            }
            return write(typed);
        }
        return bytes;
    }

    private static JsonObject parse(byte[] bytes) {
        return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static byte[] write(JsonObject root) {
        return GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
    }
}
