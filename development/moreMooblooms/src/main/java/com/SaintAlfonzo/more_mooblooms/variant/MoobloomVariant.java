package com.SaintAlfonzo.more_mooblooms.variant;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * One moobloom variant, from {@code data/<ns>/moobloom_variants/<path>.json}. The format and defaults are in
 * feature_writeup/mooblooms.md 4.4; {@link #parse} fills every default, so the rest of the mod never checks for
 * missing fields. {@code json} is the source text, which is what the server sends to clients
 * ({@link com.SaintAlfonzo.more_mooblooms.network.SyncVariantsPacket}); the client parses it the same way.
 *
 * <pre>{
 *   "flowers": [ {"state": "minecraft:rose_bush[half=upper]", "weight": 1}, "minecraft:poppy",
 *                {"state": "farmersdelight:wild_onions", "optional": true, "feed_weight": 1} ],
 *   "feed": ["#more_mooblooms:feed/onionbloom"],
 *   "scale": 1.0, "any_weight": 1, "breed_weight": 1,
 *   "spawns_in_dark_on": ["#more_mooblooms:spawnable_on_in_dark"],
 *   "egg_colors": ["#9B221A", "#ED302C"]
 * }</pre>
 */
public record MoobloomVariant(
        ResourceLocation id,
        List<Flower> pool,
        int poolWeight,
        float scale,
        Set<Item> flowerItems,
        List<TagKey<Item>> feedTags,
        Set<Item> feedItems,
        int anyWeight,
        int breedWeight,
        Set<Block> darkSpawnBlocks,
        List<TagKey<Block>> darkSpawnTags,
        int eggBase,
        int eggSpots,
        String json) {

    /** The three fixed flower positions on a grown moobloom (4.7); each one grows its own roll from the pool. */
    public enum Slot {
        BACK_FRONT("back_front"), BACK_REAR("back_rear"), HEAD("head");

        public static final int COUNT = values().length;

        public final String key;

        Slot(String key) {
            this.key = key;
        }
    }

    /**
     * One pool entry: the block drawn in a position, its weight in the per-position roll, what shearing it off
     * drops ({@code drop} at {@code chance}, both shears), and its weight when a cow is fed {@code drop} and the
     * item is food for several variants ({@code feed_weight}).
     */
    public record Flower(BlockState state, int weight, float chance, Item drop, int feedWeight) {
        public ItemStack rollDrop(RandomSource random) {
            return random.nextFloat() < chance ? new ItemStack(drop) : ItemStack.EMPTY;
        }
    }

    /** Cow egg colours, for a variant without {@code egg_colors}. */
    private static final int DEFAULT_EGG_BASE = 0x443626, DEFAULT_EGG_SPOTS = 0xA1A1A1;

    /** A pool entry picked by weight (one position's flower, or one bare-shear drop). */
    public Flower roll(RandomSource random) {
        int r = random.nextInt(poolWeight);
        for (Flower f : pool) {
            r -= f.weight();
            if (r < 0) return f;
        }
        return pool.get(pool.size() - 1);
    }

    /** The pool entry that grows {@code state}, or null (data changed since it grew). */
    @Nullable
    public Flower entryFor(BlockState state) {
        for (Flower f : pool) {
            if (f.state() == state) return f;
        }
        return null;
    }

    /**
     * This variant's weight when a cow is fed {@code stack} and several variants count it as food: the pool
     * entry's {@code feed_weight} (the highest, if two entries drop the item), 1 for {@code feed} items and tags,
     * 0 when it isn't this variant's food.
     */
    public int feedWeight(ItemStack stack) {
        if (!isFlower(stack)) return 0;
        int best = 0;
        for (Flower f : pool) {
            if (stack.is(f.drop())) best = Math.max(best, f.feedWeight());
        }
        return best > 0 ? best : 1;
    }

    /**
     * True for this variant's food: every pool flower's item and the {@code feed} items and tags. Food feeds a
     * cow into this variant, breeds adult mooblooms of it and grows its calves (D6, D45).
     */
    public boolean isFlower(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (flowerItems.contains(stack.getItem())) return true;
        for (TagKey<Item> tag : feedTags) {
            if (stack.is(tag)) return true;
        }
        return false;
    }

    /**
     * True when this variant may spawn naturally on {@code below} with no grass or light needed
     * ({@code spawns_in_dark_on}; the witherbloom on soul sand and soul soil).
     */
    public boolean spawnsInDarkOn(BlockState below) {
        if (darkSpawnBlocks.contains(below.getBlock())) return true;
        for (TagKey<Block> tag : darkSpawnTags) {
            if (below.is(tag)) return true;
        }
        return false;
    }

    /** Lang key of the moobloom's name: {@code entity.more_mooblooms.moobloom.<path>} for our own namespace. */
    public String translationKey() {
        return "entity.more_mooblooms.moobloom." + (id.getNamespace().equals("more_mooblooms")
                ? id.getPath() : id.getNamespace() + "." + id.getPath());
    }

    /** Built in, used when the data has no variants at all (so an entity always has something to draw). */
    public static MoobloomVariant fallback(ResourceLocation id) {
        Flower dandelion = new Flower(Blocks.DANDELION.defaultBlockState(), 1, 1.0F, Items.DANDELION, 1);
        return new MoobloomVariant(id, List.of(dandelion), 1, 1.0F, Set.of(Items.DANDELION), List.of(), Set.of(), 1, 1,
                Set.of(), List.of(), 0xFDD500, 0xF7EDC1, "{\"flowers\":\"minecraft:dandelion\"}");
    }

    public static MoobloomVariant parse(ResourceLocation id, JsonObject root) {
        List<Flower> pool = new ArrayList<>();
        JsonElement flowers = root.get("flowers");
        if (flowers == null) throw new JsonParseException("missing flowers");
        if (flowers.isJsonArray()) {
            for (JsonElement e : flowers.getAsJsonArray()) {
                Flower f = parseFlower(e);
                if (f != null) pool.add(f);
            }
        } else {
            Flower f = parseFlower(flowers);
            if (f != null) pool.add(f);
        }
        if (pool.isEmpty()) throw new JsonParseException("flowers is empty (or every entry is optional and missing)");
        int poolWeight = 0;
        for (Flower f : pool) poolWeight += f.weight();

        float scale = GsonHelper.getAsFloat(root, "scale", 1.0F);

        Set<Item> flowerItems = new LinkedHashSet<>();
        for (Flower f : pool) flowerItems.add(f.drop());
        Set<Item> feedItems = new LinkedHashSet<>();
        List<TagKey<Item>> feedTags = new ArrayList<>();
        if (root.has("feed")) {
            for (JsonElement e : GsonHelper.getAsJsonArray(root, "feed")) {
                String s = GsonHelper.convertToString(e, "feed entry");
                if (s.startsWith("#")) {
                    feedTags.add(TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(s.substring(1))));
                } else {
                    Item item = item(s);
                    feedItems.add(item);
                    flowerItems.add(item);
                }
            }
        }

        int anyWeight = GsonHelper.getAsInt(root, "any_weight", 1);
        if (anyWeight < 0) throw new JsonParseException("any_weight must be 0 or more");
        int breedWeight = GsonHelper.getAsInt(root, "breed_weight", anyWeight);
        if (breedWeight < 0) throw new JsonParseException("breed_weight must be 0 or more");

        Set<Block> darkSpawnBlocks = new LinkedHashSet<>();
        List<TagKey<Block>> darkSpawnTags = new ArrayList<>();
        if (root.has("spawns_in_dark_on")) {
            for (JsonElement e : GsonHelper.getAsJsonArray(root, "spawns_in_dark_on")) {
                String s = GsonHelper.convertToString(e, "spawns_in_dark_on entry");
                if (s.startsWith("#")) {
                    darkSpawnTags.add(TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(s.substring(1))));
                } else {
                    ResourceLocation rl = new ResourceLocation(s);
                    if (!ForgeRegistries.BLOCKS.containsKey(rl)) throw new JsonParseException("unknown block " + s);
                    darkSpawnBlocks.add(ForgeRegistries.BLOCKS.getValue(rl));
                }
            }
        }

        int eggBase = DEFAULT_EGG_BASE, eggSpots = DEFAULT_EGG_SPOTS;
        if (root.has("egg_colors")) {
            JsonArray colors = GsonHelper.getAsJsonArray(root, "egg_colors");
            if (colors.size() != 2) throw new JsonParseException("egg_colors needs two colours");
            eggBase = parseColor(GsonHelper.convertToString(colors.get(0), "egg colour"));
            eggSpots = parseColor(GsonHelper.convertToString(colors.get(1), "egg colour"));
        }

        return new MoobloomVariant(id, List.copyOf(pool), poolWeight, scale, Set.copyOf(flowerItems),
                List.copyOf(feedTags), Set.copyOf(feedItems), anyWeight, breedWeight, Set.copyOf(darkSpawnBlocks),
                List.copyOf(darkSpawnTags), eggBase, eggSpots,
                root.toString());
    }

    /**
     * A pool entry: a block state string, or {state, weight, chance, drop, optional, feed_weight}. Null = optional
     * and its block isn't registered (another mod's flower, that mod not installed).
     */
    @Nullable
    private static Flower parseFlower(JsonElement e) {
        JsonObject o;
        if (e.isJsonPrimitive()) {
            o = new JsonObject();
            o.add("state", e);
        } else {
            o = GsonHelper.convertToJsonObject(e, "flowers entry");
        }
        String stateString = GsonHelper.getAsString(o, "state");
        if (GsonHelper.getAsBoolean(o, "optional", false) && !blockExists(stateString)) return null;
        BlockState state = parseState(stateString);
        int weight = GsonHelper.getAsInt(o, "weight", 1);
        if (weight < 1) throw new JsonParseException("weight of " + stateString + " must be 1 or more");
        float chance = GsonHelper.getAsFloat(o, "chance", 1.0F);
        if (chance < 0.0F || chance > 1.0F) throw new JsonParseException("chance of " + stateString + " must be 0-1");
        Item drop;
        if (o.has("drop")) {
            drop = item(GsonHelper.getAsString(o, "drop"));
        } else {
            drop = state.getBlock().asItem();
            if (drop == Items.AIR) throw new JsonParseException(stateString + " has no item; give \"drop\"");
        }
        int feedWeight = GsonHelper.getAsInt(o, "feed_weight", 1);
        if (feedWeight < 1) throw new JsonParseException("feed_weight of " + stateString + " must be 1 or more");
        return new Flower(state, weight, chance, drop, feedWeight);
    }

    private static boolean blockExists(String stateString) {
        int bracket = stateString.indexOf('[');
        ResourceLocation id = ResourceLocation.tryParse(bracket < 0 ? stateString : stateString.substring(0, bracket));
        return id != null && ForgeRegistries.BLOCKS.containsKey(id);
    }

    private static Item item(String id) {
        ResourceLocation rl = new ResourceLocation(id);
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null || item == Items.AIR || !ForgeRegistries.ITEMS.containsKey(rl)) {
            throw new JsonParseException("unknown item " + id);
        }
        return item;
    }

    public static BlockState parseState(String s) {
        try {
            return BlockStateParser.parseForBlock(Registry.BLOCK, s, false).blockState();
        } catch (CommandSyntaxException e) {
            throw new JsonParseException("bad block state " + s + ": " + e.getMessage());
        }
    }

    private static int parseColor(String s) {
        String hex = s.startsWith("#") ? s.substring(1) : s;
        try {
            return Integer.parseInt(hex, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            throw new JsonParseException("bad colour " + s);
        }
    }
}
