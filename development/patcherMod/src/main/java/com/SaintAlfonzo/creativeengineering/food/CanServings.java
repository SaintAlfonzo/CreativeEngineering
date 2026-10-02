package com.SaintAlfonzo.creativeengineering.food;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.mojang.logging.LogUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.registries.ForgeRegistries;

import org.slf4j.Logger;

/**
 * Multi-serving cans. A can in {@code #creativeengineering:cans/servings/<n>} holds n servings;
 * each eat or craft takes one, and the last one leaves an empty can, a crushed can or nothing
 * ({@link CEConfig.Canning} chances).
 *
 * <p>Servings left live in the {@value #NBT_KEY} NBT int, written only on a partly eaten can: a
 * full can carries no NBT, so full cans keep stacking to 64 (real durability would force a stack
 * size of 1 and make cans enchantable). The bar is drawn from the same int
 * ({@code mixin.ItemStackCanServingsMixin}).
 *
 * <p>Entry points: {@link #finishServing} (eating, from the mixin on {@code ItemStack#finishUsingItem}),
 * {@link #craftRemainder} (KubeJS shaped/shapeless recipes, {@code compat.KubeJSCanAction}).
 */
public final class CanServings {

    private CanServings() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String NBT_KEY = "CanServings";
    /** Tag path prefix; the servings count follows it: {@code cans/servings/4}. */
    public static final String TAG_PREFIX = "cans/servings/";
    public static final String NAMESPACE = "creativeengineering";

    /** Max servings per item, 0 = not a can. Cleared whenever tags reload. */
    private static final Map<Item, Integer> MAX_CACHE = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        MAX_CACHE.clear();
    }

    /** Servings a full can of this item holds; 0 when the item isn't a managed can (or the system is off). */
    public static int maxServings(ItemStack stack) {
        if (stack.isEmpty() || !CEConfig.CANNING.enabled.get()) return 0;
        return MAX_CACHE.computeIfAbsent(stack.getItem(), item -> readMax(stack));
    }

    private static int readMax(ItemStack stack) {
        int max = 0;
        for (TagKey<Item> tag : (Iterable<TagKey<Item>>) stack.getTags()::iterator) {
            ResourceLocation id = tag.location();
            if (!NAMESPACE.equals(id.getNamespace()) || !id.getPath().startsWith(TAG_PREFIX)) continue;
            try {
                max = Math.max(max, Integer.parseInt(id.getPath().substring(TAG_PREFIX.length())));
            } catch (NumberFormatException e) {
                LOGGER.warn("[can servings] ignoring tag #{}: the part after {} must be a number", id, TAG_PREFIX);
            }
        }
        return max;
    }

    public static boolean isCan(ItemStack stack) {
        return maxServings(stack) > 0;
    }

    /** Servings left in one can of this stack (a full can when it carries no {@value #NBT_KEY}). */
    public static int servingsLeft(ItemStack stack) {
        int max = maxServings(stack);
        CompoundTag tag = stack.getTag();
        if (max <= 0 || tag == null || !tag.contains(NBT_KEY)) return max;
        return Mth.clamp(tag.getInt(NBT_KEY), 1, max);
    }

    /** A partly eaten can: the only cans that show a bar. Cheap NBT check first, it runs every frame. */
    public static boolean isPartial(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(NBT_KEY) && servingsLeft(stack) < maxServings(stack);
    }

    /** One can (count 1) of this stack with {@code left} servings; a full can gets no NBT so it stacks with new ones. */
    public static ItemStack withServings(ItemStack stack, int left) {
        ItemStack can = stack.copy();
        can.setCount(1);
        if (left >= maxServings(stack)) {
            CompoundTag tag = can.getTag();
            if (tag != null) {
                tag.remove(NBT_KEY);
                if (tag.isEmpty()) can.setTag(null);
            }
        } else {
            can.getOrCreateTag().putInt(NBT_KEY, left);
        }
        return can;
    }

    /**
     * What one can becomes after a serving: the same can with one less, or, on the last serving,
     * the rolled return. The client never rolls (the server's result replaces its guess).
     */
    public static ItemStack afterServing(ItemStack can, RandomSource random, boolean clientSide) {
        int left = servingsLeft(can) - 1;
        if (left > 0) return withServings(can, left);
        return clientSide ? ItemStack.EMPTY : rollReturn(random);
    }

    public static ItemStack rollReturn(RandomSource random) {
        double empty = CEConfig.CANNING.emptyCanChance.get();
        double crushed = Math.min(CEConfig.CANNING.crushedCanChance.get(), 1.0 - empty);
        double roll = random.nextDouble();
        if (roll < empty) return stackOf(CEConfig.CANNING.emptyCanItem.get());
        if (roll < empty + crushed) return stackOf(CEConfig.CANNING.crushedCanItem.get());
        return ItemStack.EMPTY;
    }

    private static ItemStack stackOf(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        Item item = rl == null ? null : ForgeRegistries.ITEMS.getValue(rl);
        if (item == null || item == Items.AIR) {
            LOGGER.warn("[can servings] return item '{}' (CEConfig [canning]) is not an item; nothing handed back", id);
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    /**
     * Eating one serving, called instead of {@code ItemStack#finishUsingItem} for a can held by a
     * player. The can's own finishUsingItem still runs on a one-can copy, so food values, effects
     * and Canned Goods' procedures apply per serving; the empty can it hands back is thrown away.
     *
     * <p>An opened can of the same item is eaten before a new one is opened: with a full stack in
     * hand, the fullest-eaten partial can in the inventory loses the serving instead.
     *
     * @return the stack left in the hand
     */
    public static ItemStack finishServing(ItemStack stack, Level level, Player player) {
        ItemStack single = stack.copy();
        single.setCount(1);
        single.getItem().finishUsingItem(single, level, player);

        if (player.getAbilities().instabuild) return stack;

        Inventory inv = player.getInventory();
        int slot = isPartial(stack) ? -1 : findOpenCan(inv, stack);
        ItemStack source = slot < 0 ? stack : inv.getItem(slot);

        ItemStack leftover = afterServing(source, level.getRandom(), level.isClientSide);
        source.shrink(1);

        if (source == stack && stack.isEmpty()) return leftover;
        if (source != stack && source.isEmpty()) {
            inv.setItem(slot, leftover);
        } else if (!leftover.isEmpty() && !inv.add(leftover)) {
            player.drop(leftover, false);
        }
        return stack;
    }

    /** Inventory slot of the most-eaten partial can of the same item, or -1. */
    private static int findOpenCan(Inventory inv, ItemStack held) {
        int best = -1;
        int bestLeft = Integer.MAX_VALUE;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s == held || s.getItem() != held.getItem() || !isPartial(s)) continue;
            int left = servingsLeft(s);
            if (left < bestLeft) {
                best = i;
                bestLeft = left;
            }
        }
        return best;
    }

    /**
     * Crafting remainder for a can used in a shaped/shapeless recipe: one serving is used up. Any
     * other item keeps its normal remainder, so a broad filter can't break other ingredients.
     */
    public static ItemStack craftRemainder(ItemStack stack) {
        if (!isCan(stack)) return stack.getCraftingRemainingItem();
        return afterServing(stack, RandomSource.create(), EffectiveSide.get().isClient());
    }

    // ---- durability-style bar (vanilla's width and green-to-red colour) ----

    public static int barWidth(ItemStack stack) {
        return Math.round(13.0F * servingsLeft(stack) / Math.max(1, maxServings(stack)));
    }

    public static int barColor(ItemStack stack) {
        float f = (float) servingsLeft(stack) / Math.max(1, maxServings(stack));
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }
}
