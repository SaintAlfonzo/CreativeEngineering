package com.SaintAlfonzo.creativecommon.compat.jei.world;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * One parsed world-interaction row: inputs (what is in the world or in hand), an optional action (the tool, held
 * item or trigger, drawn over the arrow), outputs, and note lines. Built from the JSON format below; anything that
 * doesn't resolve (a missing mod's item, an empty tag) is dropped, and a row left without inputs or outputs is dropped.
 *
 * <pre>
 * {"rows": [{
 *   "requires": ["modid"],                       optional: skip the row unless every mod is loaded
 *   "inputs":  [ING, ...],                       1 to 3
 *   "action":  ING,                              optional
 *   "outputs": [ING, ...],                       1 to 6
 *   "notes":   ["text", ...],                    optional, plain English, wrapped, 3 lines drawn
 *   "linked":  true                              optional: inputs[0] and outputs[0] cycle together
 * }]}
 * ING: one of {"item": id} {"items": [id, ...]} {"tag": item_tag} {"block": id} {"blocks": [id, ...]}
 *      {"block_tag": block_tag} {"fluid": id, "amount": mB} {"entity": id} {"entities": [id, ...]}
 *      {"effect": mob_effect_id} {"stacks": [{"item": id, "nbt": SNBT}, ...]} (each stack its own NBT)
 *      plus optional "count", "nbt" (SNBT, items or the fluid), "chance" (0..1), "label" (extra tooltip line)
 * </pre>
 * Blocks with no item (fire, water, lava) fall back to the fluid's bucket or a labelled stand-in, drawn as the block's
 * model where it has one; air is an empty "Nothing" slot; an effect is its icon; entities are their spawn egg (or a
 * name tag named after the mob), drawn as the mob ({@link WorldSlotRenderer}).
 * Nothing and effect slots are drawn by the category, not JEI, so they can't be looked up.
 */
public record WorldRow(List<Ing> inputs, Ing action, List<Ing> outputs, List<String> notes, boolean linked) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int MAX_INPUTS = 3;
    public static final int MAX_OUTPUTS = 6;

    /**
     * One slot: items or fluids (one list is empty), its chance, and extra tooltip lines. With both empty it is a
     * drawn-only slot: {@code effect}'s icon, or empty for {@code nothing} (air).
     */
    public record Ing(List<ItemStack> items, List<FluidStack> fluids, MobEffect effect, boolean nothing, float chance,
                      List<Component> tooltip) {
        boolean drawnOnly() {
            return items.isEmpty() && fluids.isEmpty();
        }
    }

    /** Null when the row is skipped. {@code where} names the source in log lines. */
    public static WorldRow parse(JsonObject json, String where) {
        if (json.has("requires")) {
            for (JsonElement mod : json.getAsJsonArray("requires")) {
                if (!ModList.get().isLoaded(mod.getAsString())) return null;
            }
        }
        List<Ing> inputs = list(json.getAsJsonArray("inputs"), MAX_INPUTS, where);
        List<Ing> outputs = list(json.getAsJsonArray("outputs"), MAX_OUTPUTS, where);
        if (inputs.isEmpty() || outputs.isEmpty()) return null;
        Ing action = json.has("action") ? ing(json.getAsJsonObject("action"), where) : null;
        List<String> notes = new ArrayList<>();
        if (json.has("notes")) {
            for (JsonElement note : json.getAsJsonArray("notes")) notes.add(note.getAsString());
        }
        boolean linked = json.has("linked") && json.get("linked").getAsBoolean();
        return new WorldRow(inputs, action, outputs, notes, linked);
    }

    private static List<Ing> list(JsonArray array, int max, String where) {
        List<Ing> out = new ArrayList<>();
        if (array == null) return out;
        for (JsonElement element : array) {
            Ing ing = ing(element.getAsJsonObject(), where);
            if (ing == null) continue;
            if (out.size() == max) {
                LOGGER.warn("[jei_world] {}: more than {} slots, rest dropped", where, max);
                break;
            }
            out.add(ing);
        }
        return out;
    }

    /** Null when nothing resolves. */
    static Ing ing(JsonObject json, String where) {
        int count = json.has("count") ? json.get("count").getAsInt() : 1;
        List<ItemStack> items = new ArrayList<>();
        List<FluidStack> fluids = new ArrayList<>();
        List<Component> tooltip = new ArrayList<>();

        if (json.has("item")) addItem(items, json.get("item").getAsString(), count);
        if (json.has("items")) {
            for (JsonElement id : json.getAsJsonArray("items")) addItem(items, id.getAsString(), count);
        }
        if (json.has("stacks")) {
            for (JsonElement element : json.getAsJsonArray("stacks")) {
                JsonObject stack = element.getAsJsonObject();
                int before = items.size();
                addItem(items, stack.get("item").getAsString(), count);
                if (items.size() > before && stack.has("nbt")) {
                    try {
                        items.get(before).setTag(TagParser.parseTag(stack.get("nbt").getAsString()));
                    } catch (CommandSyntaxException e) {
                        LOGGER.warn("[jei_world] {}: bad nbt {}", where, stack.get("nbt"));
                    }
                }
            }
        }
        if (json.has("tag")) {
            TagKey<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(strip(json.get("tag").getAsString())));
            for (Holder<Item> holder : Registry.ITEM.getTagOrEmpty(tag)) items.add(new ItemStack(holder.value(), count));
        }
        Set<Block> blocks = new LinkedHashSet<>();
        if (json.has("block")) addBlock(blocks, json.get("block").getAsString());
        if (json.has("blocks")) {
            for (JsonElement id : json.getAsJsonArray("blocks")) addBlock(blocks, id.getAsString());
        }
        if (json.has("block_tag")) {
            TagKey<Block> tag = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(strip(json.get("block_tag").getAsString())));
            for (Holder<Block> holder : Registry.BLOCK.getTagOrEmpty(tag)) blocks.add(holder.value());
        }
        boolean nothing = blocks.removeIf(block -> block.defaultBlockState().isAir());
        for (Block block : blocks) blockStack(items, fluids, block, count);
        if (json.has("fluid")) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(json.get("fluid").getAsString()));
            int amount = json.has("amount") ? json.get("amount").getAsInt() : 1000;
            if (fluid != null && fluid != Fluids.EMPTY) fluids.add(new FluidStack(fluid, amount));
        }
        if (json.has("entity")) addEntity(items, json.get("entity").getAsString());
        if (json.has("entities")) {
            for (JsonElement id : json.getAsJsonArray("entities")) addEntity(items, id.getAsString());
        }
        MobEffect effect = json.has("effect") ? ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(json.get("effect").getAsString())) : null;
        if (json.has("nbt") && !(items.isEmpty() && fluids.isEmpty())) {
            try {
                var tag = TagParser.parseTag(json.get("nbt").getAsString());
                for (ItemStack stack : items) stack.getOrCreateTag().merge(tag);
                if (items.isEmpty()) for (FluidStack stack : fluids) stack.getOrCreateTag().merge(tag);
            } catch (CommandSyntaxException e) {
                LOGGER.warn("[jei_world] {}: bad nbt {}", where, json.get("nbt"));
            }
        }
        // Fluids and items can't share a JEI slot; a slot with both keeps the items.
        if (!items.isEmpty()) fluids.clear();

        float chance = json.has("chance") ? json.get("chance").getAsFloat() : 1f;
        boolean drawnOnly = items.isEmpty() && fluids.isEmpty();
        if (drawnOnly && effect != null) tooltip.add(effect.getDisplayName());
        else if (drawnOnly && nothing) tooltip.add(Component.literal("Nothing"));
        if (json.has("label")) tooltip.add(Component.literal(json.get("label").getAsString()).withStyle(ChatFormatting.GRAY));
        if (chance < 1f) tooltip.add(Component.literal(percent(chance) + " chance").withStyle(ChatFormatting.GOLD));

        if (drawnOnly && effect == null && !nothing) return null;
        return new Ing(items, fluids, drawnOnly ? effect : null, drawnOnly && nothing, chance, tooltip);
    }

    static String percent(float chance) {
        float pct = chance * 100f;
        if (pct >= 10f || pct == Math.round(pct)) return Math.round(pct) + "%";
        return String.format(Locale.ROOT, "%.1f%%", pct);
    }

    private static String strip(String tag) {
        return tag.startsWith("#") ? tag.substring(1) : tag;
    }

    private static void addItem(List<ItemStack> items, String id, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item != null && item != Items.AIR) items.add(new ItemStack(item, count));
    }

    private static void addBlock(Set<Block> blocks, String id) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
        if (block != null && ForgeRegistries.BLOCKS.getKey(block).toString().equals(new ResourceLocation(id).toString())) {
            blocks.add(block);
        }
    }

    /**
     * A block as its item; a liquid block as its fluid; fire as flint and steel; a bubble column as a water bucket.
     * Any other itemless block (frosted ice, candle cakes, crops, Create's dyed sails) is a Barrier named after the
     * block and marked with {@link WorldSlotRenderer#BLOCK_TAG}, so the slot draws the block's model instead.
     */
    private static void blockStack(List<ItemStack> items, List<FluidStack> fluids, Block block, int count) {
        Item item = block.asItem();
        if (item != Items.AIR) {
            items.add(new ItemStack(item, count));
            return;
        }
        if (block instanceof net.minecraft.world.level.block.LiquidBlock liquid) {
            fluids.add(new FluidStack(liquid.getFluid(), 1000));
            return;
        }
        Item standIn = block instanceof net.minecraft.world.level.block.BaseFireBlock ? Items.FLINT_AND_STEEL
                : block instanceof net.minecraft.world.level.block.BubbleColumnBlock ? Items.WATER_BUCKET
                : Items.BARRIER;
        ItemStack stack = new ItemStack(standIn);
        stack.setHoverName(block.getName().withStyle(s -> s.withItalic(false)));
        if (standIn == Items.BARRIER) {
            stack.getOrCreateTag().putString(WorldSlotRenderer.BLOCK_TAG, ForgeRegistries.BLOCKS.getKey(block).toString());
        }
        items.add(stack);
    }

    private static void addEntity(List<ItemStack> items, String id) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id));
        if (type == null || !ForgeRegistries.ENTITY_TYPES.getKey(type).toString().equals(new ResourceLocation(id).toString())) return;
        items.add(entityStack(type));
    }

    /**
     * The mob's spawn egg, or a name tag named after it, marked with {@link WorldSlotRenderer#ENTITY_TAG} so a world
     * tab slot draws the mob itself (the stack stays what JEI looks up and names in the tooltip).
     */
    public static ItemStack entityStack(EntityType<?> type) {
        SpawnEggItem egg = SpawnEggItem.byId(type);
        if (egg == null) egg = ForgeSpawnEggItem.fromEntityType(type);
        ItemStack stack;
        if (egg != null) {
            stack = new ItemStack(egg);
        } else {
            stack = new ItemStack(Items.NAME_TAG);
            stack.setHoverName(type.getDescription().copy().withStyle(s -> s.withItalic(false)));
        }
        stack.getOrCreateTag().putString(WorldSlotRenderer.ENTITY_TAG, ForgeRegistries.ENTITY_TYPES.getKey(type).toString());
        return stack;
    }
}
