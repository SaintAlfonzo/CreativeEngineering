package ce.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Where a cooking station's ingredient leftovers (glass bottles, bowls, buckets) go: the one rule every Farmer's
 * Delight style station follows (FD cooking pot, Miner's copper pot, Farmer's Respite kettle, the oven, the Pickling
 * Jar; feature_writeup/remainder_buffer.md 3.8). For each leftover, in order:
 * <ol>
 *   <li>the container slot, when it already holds the same item: merged up to the slot's stack size;</li>
 *   <li>the overflow (leftover) slots: into a stack of the same item first, then an empty slot;</li>
 *   <li>an empty container slot, when the leftover is what the recipe is served in ({@code servedIn}) and the overflow
 *       slots are full;</li>
 *   <li>what is still left is returned: the station throws it out, or, with stallWhenFull, does not start the craft
 *       (it checks with a dry run first).</li>
 * </ol>
 * A leftover waiting in an overflow slot moves to the container slot when it can plate the waiting meal
 * ({@link OverflowPull}). Pure planning on copies, so the same call serves the dry run before a craft and the real
 * store after it. Shared source (development/shared/stations), compiled into each jar under its own package.
 */
public final class LeftoverPlan {

    private LeftoverPlan() {}

    /**
     * Places {@code leftovers} into {@code slots} (index 0 = the container slot, the rest = overflow slots; each a copy
     * the caller owns, changed in place) with {@code limits[i]} as each slot's limit. Returns what did not fit.
     * A null {@code slots[0]} means the station has no container slot.
     */
    public static List<ItemStack> place(List<ItemStack> leftovers, ItemStack[] slots, int[] limits, Predicate<ItemStack> servedIn) {
        List<ItemStack> rest = new ArrayList<>();
        for (ItemStack leftover : leftovers) {
            if (leftover.isEmpty()) continue;
            ItemStack left = leftover.copy();
            if (slots[0] != null && !slots[0].isEmpty()) {
                left = merge(slots, 0, left, limits[0]);
            }
            for (int i = 1; i < slots.length && !left.isEmpty(); ++i) {
                if (!slots[i].isEmpty()) left = merge(slots, i, left, limits[i]);
            }
            for (int i = 1; i < slots.length && !left.isEmpty(); ++i) {
                if (slots[i].isEmpty()) left = merge(slots, i, left, limits[i]);
            }
            if (!left.isEmpty() && slots[0] != null && slots[0].isEmpty() && servedIn.test(left)) {
                left = merge(slots, 0, left, limits[0]);
            }
            if (!left.isEmpty()) rest.add(left);
        }
        return rest;
    }

    /** Copies of the stacks, for a {@link #place} dry run (null stays null). */
    public static ItemStack[] copies(ItemStack... stacks) {
        ItemStack[] out = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; ++i) {
            out[i] = stacks[i] == null ? null : stacks[i].copy();
        }
        return out;
    }

    /** Moves as much of {@code stack} as fits into {@code slots[index]}; returns the rest. */
    private static ItemStack merge(ItemStack[] slots, int index, ItemStack stack, int slotLimit) {
        ItemStack current = slots[index];
        int limit = Math.min(slotLimit, stack.getMaxStackSize());
        if (current.isEmpty()) {
            int moved = Math.min(limit, stack.getCount());
            slots[index] = ItemHandlerHelper.copyStackWithSize(stack, moved);
            return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - moved);
        }
        if (!ItemHandlerHelper.canItemStacksStack(current, stack)) {
            return stack;
        }
        int moved = Math.min(limit - current.getCount(), stack.getCount());
        if (moved <= 0) {
            return stack;
        }
        current.grow(moved);
        return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - moved);
    }
}
