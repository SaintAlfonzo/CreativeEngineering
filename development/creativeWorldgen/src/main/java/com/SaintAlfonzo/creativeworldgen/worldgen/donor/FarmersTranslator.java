package com.SaintAlfonzo.creativeworldgen.worldgen.donor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Rewrites the worldgen data of Farmer's Structures 1.0.6 (NeoForge 1.21.1) into what Forge 1.19.2 reads, in memory
 * (feature_writeup/farmers_structures.md 5). Same rules as tools/port_structure_jars.py, which does them on files for
 * the MIT / unlicensed jars; this one never writes the result to disk because the jar is All Rights Reserved.
 *
 * <ul>
 * <li>Paths: {@code structure/} -> {@code structures/}, {@code tags/block/} -> {@code tags/blocks/}.</li>
 * <li>Foreign dimensions (Aether, Undergarden, Afterdark, Deeper and Darker, flesh, Bumblezone) are left out: the
 * installed 1.0.0 data keeps its (disabled) versions of them.</li>
 * <li>Templates: items {@code count} -> {@code Count} with the few components 1.19.2 has as {@code tag}; blocks 1.19.2
 * lacks are mapped ({@link #mapBlock}) or become air with their block entity dropped; entities of mods or keys 1.19.2
 * does not know are stripped.</li>
 * <li>The cod pond and the fish stew hut get variants where some fish are Upgrade Aquatic pike and perch.</li>
 * <li>The carp pond (gone from 1.0.6, spawns without fish here) gets an empty structure set.</li>
 * </ul>
 */
final class FarmersTranslator {
    private static final Logger LOG = LogManager.getLogger("creativeengineering/farmers-donor");

    private static final String NS = "data/farmers_structures/";

    private static final Set<String> SKIP_STRUCTURES = Set.of("aether", "afterdark_farms", "deeper_farms", "flesh_farms",
            "honey_cake_farms", "undergardens", "undergardens_sea");
    private static final Set<String> SKIP_POOLS = Set.of("aether_spawn_pool", "afterdark_farm_spawn_pool", "deeper_farm_spawn_pool",
            "flesh_farm_spawn_pool", "honey_cake_farm_spawn_pool", "undergarden_spawn_pool", "undergarden_sea_spawn_pool");
    private static final Set<String> SKIP_TEMPLATE_DIRS = Set.of("aether/", "undergarden/");
    private static final Set<String> SKIP_TEMPLATES = Set.of("afterdark_farm", "deeper_farm", "flesh_farm", "honey_cake_farm");

    /** Template (path under structure/) -> the fish species a variant may swap for pike and perch. */
    private static final Map<String, String> FISH_TEMPLATES = Map.of(
            "aquaculture/aquaculture_cod_spawn", "minecraft:cod",
            "fish_stew_spawn", "minecraft:salmon");
    private static final String PIKE = "upgrade_aquatic:pike";
    private static final String PERCH = "upgrade_aquatic:perch";
    /** Variant suffix -> the fish at index i (of the template's fish, in order) becomes ... (null = stays). */
    private static final String[] FISH_VARIANTS = {"pike", "perch", "pike_perch"};

    /** 1.21 entity keys 1.19.2 reads differently or not at all. */
    private static final Set<String> ENTITY_DROP = Set.of("attributes", "variant", "Brain", "drop_chances", "equipment",
            "active_effects", "attachments", "CustomName");

    private final Map<String, byte[]> out = new HashMap<>();
    private final Map<String, Integer> counts = new TreeMap<>();

    private FarmersTranslator() {
    }

    static Map<String, byte[]> translate(Map<String, byte[]> source) throws IOException {
        FarmersTranslator t = new FarmersTranslator();
        for (Map.Entry<String, byte[]> e : source.entrySet()) {
            if (e.getKey().startsWith(NS)) {
                t.file(e.getKey().substring(NS.length()), e.getValue());
            }
        }
        t.carpSet();
        LOG.info("[farmers-donor] {} files from Farmer's Structures 1.0.6 {}", t.out.size(), t.counts);
        return t.out;
    }

    private void count(String what) {
        counts.merge(what, 1, Integer::sum);
    }

    private void put(String rel, byte[] bytes) {
        out.put(NS + rel, bytes);
    }

    private void file(String rel, byte[] bytes) throws IOException {
        if (rel.startsWith("structure/") && rel.endsWith(".nbt")) {
            String name = rel.substring("structure/".length(), rel.length() - 4);
            if (SKIP_TEMPLATES.contains(name) || SKIP_TEMPLATE_DIRS.stream().anyMatch(name::startsWith)) {
                return;
            }
            byte[] done = template(bytes);
            put("structures/" + name + ".nbt", done);
            count("templates");
            String fish = FISH_TEMPLATES.get(name);
            if (fish != null && ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(PIKE))
                    && ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(PERCH))) {
                for (String variant : FISH_VARIANTS) {
                    put("structures/" + name + "_" + variant + ".nbt", fishVariant(done, fish, variant));
                    count("fish variants");
                }
            }
        } else if (rel.startsWith("worldgen/structure/") && rel.endsWith(".json")) {
            if (SKIP_STRUCTURES.contains(rel.substring("worldgen/structure/".length(), rel.length() - 5))) {
                return;
            }
            JsonObject json = JsonParser.parseString(utf8(bytes)).getAsJsonObject();
            json.remove("liquid_settings");   // 1.20.6 field
            put(rel, json(json));
        } else if (rel.startsWith("worldgen/structure_set/") && rel.endsWith(".json")) {
            JsonObject json = JsonParser.parseString(utf8(bytes)).getAsJsonObject();
            for (JsonElement s : json.getAsJsonArray("structures")) {
                String id = s.getAsJsonObject().get("structure").getAsString();
                if (SKIP_STRUCTURES.contains(id.substring(id.indexOf(':') + 1))) {
                    return;
                }
            }
            put(rel, bytes);
        } else if (rel.startsWith("worldgen/template_pool/") && rel.endsWith(".json")) {
            if (SKIP_POOLS.contains(rel.substring("worldgen/template_pool/".length(), rel.length() - 5))) {
                return;
            }
            put(rel, json(withFishVariants(JsonParser.parseString(utf8(bytes)).getAsJsonObject())));
        } else if (rel.startsWith("tags/block/") && rel.endsWith(".json")) {
            put("tags/blocks/" + rel.substring("tags/block/".length()), bytes);
        } else if (rel.startsWith("worldgen/processor_list/") || rel.startsWith("tags/worldgen/biome/")) {
            put(rel, bytes);
        }
    }

    // ---------------------------------------------------------------- json

    private static String utf8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] json(JsonElement json) {
        return (json + "\n").getBytes(StandardCharsets.UTF_8);
    }

    /** A pool entry for a fish template gets one entry per variant, so each pond rolls its fish when it generates. */
    private static JsonObject withFishVariants(JsonObject pool) {
        JsonArray elements = pool.getAsJsonArray("elements");
        if (elements == null) {
            return pool;
        }
        JsonArray grown = new JsonArray();
        for (JsonElement entry : elements) {
            grown.add(entry);
            JsonObject element = entry.getAsJsonObject().getAsJsonObject("element");
            String location = element.has("location") ? element.get("location").getAsString() : "";
            String template = location.startsWith("farmers_structures:") ? location.substring("farmers_structures:".length()) : "";
            if (FISH_TEMPLATES.containsKey(template)
                    && ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(PIKE))
                    && ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(PERCH))) {
                for (String variant : FISH_VARIANTS) {
                    JsonObject copy = entry.getAsJsonObject().deepCopy();
                    copy.getAsJsonObject("element").addProperty("location", location + "_" + variant);
                    grown.add(copy);
                }
            }
        }
        pool.add("elements", grown);
        return pool;
    }

    /** 1.0.6 removed the carp pond (its fish need Aquaculture); 1.0.0's set for it must not place the empty pond. */
    private void carpSet() {
        JsonObject placement = new JsonObject();
        placement.addProperty("type", "minecraft:random_spread");
        placement.addProperty("salt", 1);
        placement.addProperty("spacing", 48);
        placement.addProperty("separation", 31);
        JsonObject set = new JsonObject();
        set.add("structures", new JsonArray());
        set.add("placement", placement);
        put("worldgen/structure_set/aquacultures_carp_with_all.json", json(set));
    }

    // ---------------------------------------------------------------- templates

    private byte[] template(byte[] raw) throws IOException {
        CompoundTag root = NbtIo.readCompressed(new ByteArrayInputStream(raw));
        List<ListTag> palettes = new ArrayList<>();
        if (root.contains("palettes", Tag.TAG_LIST)) {
            ListTag all = root.getList("palettes", Tag.TAG_LIST);
            for (int i = 0; i < all.size(); i++) {
                palettes.add(all.getList(i));
            }
        } else {
            palettes.add(root.getList("palette", Tag.TAG_COMPOUND));
        }
        Set<Integer> noBlockEntity = new HashSet<>();
        for (ListTag palette : palettes) {
            for (int i = 0; i < palette.size(); i++) {
                if (mapBlock(palette.getCompound(i))) {
                    noBlockEntity.add(i);
                }
            }
        }
        ListTag blocks = root.getList("blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag block = blocks.getCompound(i);
            if (noBlockEntity.contains(block.getInt("state"))) {
                block.remove("nbt");
            } else if (block.contains("nbt", Tag.TAG_COMPOUND)) {
                CompoundTag be = block.getCompound("nbt");
                be.remove("components");
                be.remove("bees");   // beehive occupants: 1.21 layout
                fixItems(be);
            }
        }
        ListTag entities = root.getList("entities", Tag.TAG_COMPOUND);
        ListTag kept = new ListTag();
        for (int i = 0; i < entities.size(); i++) {
            CompoundTag entity = entities.getCompound(i);
            CompoundTag nbt = entity.getCompound("nbt");
            String id = nbt.getString("id");
            if (!ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(id))) {
                count("entities dropped " + id);
                continue;
            }
            for (String key : new ArrayList<>(nbt.getAllKeys())) {
                if (ENTITY_DROP.contains(key) || key.indexOf(':') >= 0) {
                    nbt.remove(key);
                }
            }
            fixItems(nbt);
            kept.add(entity);
        }
        root.put("entities", kept);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(root, bytes);
        return bytes.toByteArray();
    }

    /** The fish of a pond/hut template, swapped for pike and perch by the variant's pattern. */
    private static byte[] fishVariant(byte[] translated, String fish, String variant) throws IOException {
        CompoundTag root = NbtIo.readCompressed(new ByteArrayInputStream(translated));
        ListTag entities = root.getList("entities", Tag.TAG_COMPOUND);
        int index = 0;
        for (int i = 0; i < entities.size(); i++) {
            CompoundTag nbt = entities.getCompound(i).getCompound("nbt");
            if (!fish.equals(nbt.getString("id"))) {
                continue;
            }
            String swap = switch (variant) {
                case "pike" -> index % 2 == 1 ? PIKE : null;
                case "perch" -> index % 2 == 1 ? PERCH : null;
                default -> index % 3 == 1 ? PIKE : index % 3 == 2 ? PERCH : null;
            };
            index++;
            if (swap != null) {
                nbt.putString("id", swap);
                nbt.remove("Variant");
                nbt.remove("Type");
            }
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(root, bytes);
        return bytes.toByteArray();
    }

    // ---------------------------------------------------------------- blocks

    private static boolean blockExists(String id) {
        return ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(id));
    }

    /** The 1.19.2 / pack id standing in for a 1.20+ one, or the id itself. Items and blocks share the names. */
    static String mapName(String id) {
        switch (id) {
            case "minecraft:short_grass":
                return "minecraft:grass";
            case "minecraft:bamboo_block":
                return "quark:bamboo_block";
            case "minecraft:stripped_bamboo_block":
                return "quark:stripped_bamboo_block";
            case "minecraft:tuff_bricks":
                return "quark:tuff_bricks";
            case "minecraft:tuff_brick_slab":
                return "quark:tuff_bricks_slab";
            case "minecraft:tuff_brick_stairs":
                return "quark:tuff_bricks_stairs";
            case "minecraft:tuff_brick_wall":
                return "quark:tuff_bricks_wall";
            case "farmersdelight:wooden_basket":
            case "farmersdelight:bamboo_basket":
                return "farmersdelight:basket";
            case "minecraft:suspicious_sand":
                return "minecraft:sand";
            case "minecraft:suspicious_gravel":
                return "minecraft:gravel";
            default:
                break;
        }
        if (id.startsWith("minecraft:cherry_") || id.startsWith("minecraft:stripped_cherry_")) {
            return "vinery:" + id.substring("minecraft:".length());
        }
        return id;
    }

    /**
     * Rewrites one palette entry in place. Returns true when the block's block entity data must go (the block became
     * another block or air, so the old data would not fit).
     */
    private boolean mapBlock(CompoundTag state) {
        String name = state.getString("Name");
        String to = mapName(name);
        boolean changed = !to.equals(name);
        CompoundTag props = state.contains("Properties", Tag.TAG_COMPOUND) ? state.getCompound("Properties") : null;
        switch (name) {
            case "minecraft:pink_petals" -> {
                // Immersive Weathering's azalea flower pile stands in: flower_amount 1..4 -> layers 0..3
                int amount = props != null ? parse(props.getString("flower_amount"), 1) : 1;
                state.remove("Properties");
                CompoundTag layers = new CompoundTag();
                layers.putString("layers", Integer.toString(Math.max(0, Math.min(8, amount - 1))));
                state.put("Properties", layers);
                to = "immersive_weathering:azalea_flower_pile";
                changed = true;
            }
            case "minecraft:decorated_pot" -> {
                state.remove("Properties");
                to = "supplementaries:urn";
                changed = true;
            }
            case "minecraft:suspicious_sand", "minecraft:suspicious_gravel" -> state.remove("Properties");
            default -> {
            }
        }
        if (!blockExists(to)) {
            count("blocks to air " + name);
            state.putString("Name", "minecraft:air");
            state.remove("Properties");
            return true;
        }
        if (changed) {
            count("blocks mapped " + name);
            state.putString("Name", to);
        }
        return changed && (name.equals("minecraft:decorated_pot") || name.startsWith("minecraft:suspicious_"));
    }

    private static int parse(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // ---------------------------------------------------------------- items

    private static boolean isStack(CompoundTag tag) {
        return tag.contains("id", Tag.TAG_STRING) && tag.contains("Count", Tag.TAG_ANY_NUMERIC);
    }

    private static boolean knownItem(CompoundTag stack) {
        return ForgeRegistries.ITEMS.containsKey(new ResourceLocation(stack.getString("id")));
    }

    /** 1.20.5+ stacks ({@code count}, {@code components}) -> 1.19.2 ({@code Count}, {@code tag}); unknown items are dropped. */
    private void fixItems(Tag tag) {
        if (tag instanceof ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                fixItems(list.get(i));
            }
        } else if (tag instanceof CompoundTag compound) {
            if (compound.contains("id", Tag.TAG_STRING) && compound.contains("count", Tag.TAG_ANY_NUMERIC) && !compound.contains("Count")) {
                convertStack(compound);
            }
            for (String key : new ArrayList<>(compound.getAllKeys())) {
                Tag child = compound.get(key);
                fixItems(child);
                if (child instanceof CompoundTag c && isStack(c) && !knownItem(c)) {
                    count("items dropped " + c.getString("id"));
                    compound.remove(key);
                } else if (child instanceof ListTag list && !list.isEmpty() && list.get(0) instanceof CompoundTag first && isStack(first)) {
                    boolean positional = key.equals("HandItems") || key.equals("ArmorItems");
                    for (int i = list.size() - 1; i >= 0; i--) {
                        if (list.get(i) instanceof CompoundTag c && isStack(c) && !knownItem(c)) {
                            count("items dropped " + c.getString("id"));
                            if (positional) {
                                list.set(i, new CompoundTag());
                            } else {
                                list.remove(i);
                            }
                        }
                    }
                }
            }
        }
    }

    private void convertStack(CompoundTag stack) {
        int amount = stack.getInt("count");
        stack.remove("count");
        stack.putByte("Count", (byte) Math.max(0, Math.min(127, amount)));
        stack.putString("id", mapName(stack.getString("id")));
        CompoundTag components = stack.getCompound("components");
        stack.remove("components");
        CompoundTag tag = new CompoundTag();
        enchantments(components.getCompound("minecraft:enchantments"), "Enchantments", tag);
        enchantments(components.getCompound("minecraft:stored_enchantments"), "StoredEnchantments", tag);
        if (components.contains("minecraft:repair_cost", Tag.TAG_ANY_NUMERIC)) {
            tag.putInt("RepairCost", components.getInt("minecraft:repair_cost"));
        }
        if (components.contains("minecraft:custom_name", Tag.TAG_STRING)) {
            CompoundTag display = new CompoundTag();
            display.putString("Name", components.getString("minecraft:custom_name"));
            tag.put("display", display);
        }
        CompoundTag potion = components.getCompound("minecraft:potion_contents");
        if (potion.contains("potion", Tag.TAG_STRING)) {
            tag.putString("Potion", potion.getString("potion"));
        }
        if (!tag.isEmpty()) {
            stack.put("tag", tag);
        }
    }

    private static void enchantments(CompoundTag component, String key, CompoundTag tag) {
        CompoundTag levels = component.contains("levels", Tag.TAG_COMPOUND) ? component.getCompound("levels") : component;
        ListTag list = new ListTag();
        for (String id : levels.getAllKeys()) {
            if (levels.get(id) instanceof net.minecraft.nbt.NumericTag level && id.indexOf(':') >= 0) {
                CompoundTag enchantment = new CompoundTag();
                enchantment.putString("id", id);
                enchantment.put("lvl", ShortTag.valueOf(level.getAsShort()));
                list.add(enchantment);
            }
        }
        if (!list.isEmpty()) {
            tag.put(key, list);
        }
    }
}
