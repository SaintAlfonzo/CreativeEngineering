package com.SaintAlfonzo.creativedelight.cooking;

import com.SaintAlfonzo.creativeengineering.compat.RemainderView;
import com.SaintAlfonzo.creativedelight.CDConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Holding slots for ingredient leftovers (glass bottles, bowls, buckets) on Farmer's Delight style stations, so they
 * stay in the station instead of being thrown out of it (feature_writeup/remainder_buffer.md). Used by the FD cooking
 * pot and the Farmer's Respite kettle (mixin/CookingPotRemainderMixin, KettleRemainderMixin) and by the oven.
 *
 * One leftover goes, in order:
 *  1. to the station's container slot, if it is what the waiting meal is served in, the slot is empty or holds the
 *     same item, and the waiting meals still need containers there (meals waiting minus containers already in the
 *     slot): the next tick plates them with it. Spares don't pile up in the container slot, which automation
 *     can't empty;
 *  2. to these slots, merging into a matching stack before taking an empty one;
 *  3. out of the station as before, for whatever is left. With {@code [cooking] stallWhenFull} the station checks
 *     {@link #fits} before it starts a craft, so this only happens with that switch off.
 *
 * A step-1 recycle holds the station's plating for {@code [cooking] recycleShowTicks} ({@link #holdPlating}), so the
 * container is seen in its slot before the meal takes it.
 */
public class RemainderBuffer extends ItemStackHandler implements RemainderView {

    public static final int SLOTS = 2;

    private final Runnable onChanged;
    // Game time of the last craft check that failed for want of room here (not saved): WTHIT's "slots full" line.
    private long stalledAt = -1000L;
    // Plating calls still to skip after a recycle into the container slot (not saved: a reload plates at once).
    private int plateHold;

    public RemainderBuffer(Runnable onChanged) {
        super(SLOTS);
        this.onChanged = onChanged;
    }

    @Override
    protected void onContentsChanged(int slot) {
        onChanged.run();
    }

    public static boolean enabled() {
        return CDConfig.COOKING.remainderBuffer.get();
    }

    public static boolean stalls() {
        return enabled() && CDConfig.COOKING.stallWhenFull.get();
    }

    /** The station's room check refused a craft this tick (it runs every tick while a recipe waits). */
    public void markStalled(long gameTime) {
        stalledAt = gameTime;
    }

    /** Whether a craft is waiting for room in these slots (refused within the last couple of ticks). */
    public boolean isStalled(long gameTime) {
        return gameTime - stalledAt <= 2;
    }

    /**
     * Called where the station would plate the waiting meal (once a tick while the meal and its container wait):
     * true = skip plating this tick, because a recycled container is still on show.
     */
    public boolean holdPlating() {
        if (plateHold <= 0) return false;
        --plateHold;
        return true;
    }

    /**
     * Stores one leftover. {@code inventory}/{@code containerSlot} is the station's container (plating) slot,
     * {@code servedIn} tells whether an item is what the waiting meal is served in and {@code mealsWaiting} is how
     * many meals wait for it. Returns what didn't fit.
     */
    public ItemStack store(ItemStack leftover, IItemHandlerModifiable inventory, int containerSlot,
                           Predicate<ItemStack> servedIn, int mealsWaiting) {
        List<ItemStack> overflow = new ArrayList<>();
        ItemStack[] planned = plan(List.of(leftover), inventory.getStackInSlot(containerSlot),
                inventory.getSlotLimit(containerSlot), servedIn, mealsWaiting, overflow);
        if (planned[0].getCount() > inventory.getStackInSlot(containerSlot).getCount()) {
            plateHold = CDConfig.COOKING.recycleShowTicks.get();
        }
        if (!ItemStack.matches(planned[0], inventory.getStackInSlot(containerSlot))) {
            inventory.setStackInSlot(containerSlot, planned[0]);
        }
        for (int i = 0; i < SLOTS; ++i) {
            if (!ItemStack.matches(planned[i + 1], getStackInSlot(i))) {
                setStackInSlot(i, planned[i + 1]);
            }
        }
        return overflow.isEmpty() ? ItemStack.EMPTY : overflow.get(0);
    }

    /**
     * Whether all of one craft's leftovers would fit (container slot + these slots), checked before the craft.
     * {@code mealsWaiting} counts the meals that will wait after it (those waiting now + the recipe's result).
     */
    public boolean fits(List<ItemStack> leftovers, IItemHandler inventory, int containerSlot,
                        Predicate<ItemStack> servedIn, int mealsWaiting) {
        if (leftovers.isEmpty()) return true;
        List<ItemStack> overflow = new ArrayList<>();
        plan(leftovers, inventory.getStackInSlot(containerSlot), inventory.getSlotLimit(containerSlot), servedIn,
                mealsWaiting, overflow);
        return overflow.isEmpty();
    }

    /** Copies of [container slot, buffer slots] after taking {@code leftovers}; what doesn't fit goes to {@code overflow}. */
    private ItemStack[] plan(List<ItemStack> leftovers, ItemStack containerStack, int containerLimit,
                             Predicate<ItemStack> servedIn, int mealsWaiting, List<ItemStack> overflow) {
        ItemStack[] slots = new ItemStack[SLOTS + 1];
        slots[0] = containerStack.copy();
        for (int i = 0; i < SLOTS; ++i) {
            slots[i + 1] = getStackInSlot(i).copy();
        }
        for (ItemStack leftover : leftovers) {
            ItemStack rest = leftover.copy();
            int containersNeeded = mealsWaiting - slots[0].getCount();
            if (containersNeeded > 0 && servedIn.test(rest)) {
                rest = merge(slots, 0, rest, Math.min(containerLimit, slots[0].getCount() + containersNeeded));
            }
            for (int i = 1; i <= SLOTS && !rest.isEmpty(); ++i) {
                if (!slots[i].isEmpty()) rest = merge(slots, i, rest, getSlotLimit(i - 1));
            }
            for (int i = 1; i <= SLOTS && !rest.isEmpty(); ++i) {
                if (slots[i].isEmpty()) rest = merge(slots, i, rest, getSlotLimit(i - 1));
            }
            if (!rest.isEmpty()) {
                overflow.add(rest);
            }
        }
        return slots;
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

    /**
     * The leftovers one craft gives back from ingredient slots {@code [from, to)}, by the stations' own rule: the
     * stack's crafting remainder, else {@code overrides} (FD's INGREDIENT_REMAINDER_OVERRIDES; null for none).
     */
    public static List<ItemStack> leftovers(IItemHandler inventory, int from, int to, Map<Item, Item> overrides) {
        return leftovers(inventory, from, to, overrides, null);
    }

    /** As above, with {@code counts[slot - from]} items used from each slot (one leftover each); null = 1 per slot. */
    public static List<ItemStack> leftovers(IItemHandler inventory, int from, int to, Map<Item, Item> overrides,
                                            int[] counts) {
        List<ItemStack> leftovers = new ArrayList<>();
        for (int i = from; i < to; ++i) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            int used = counts == null ? 1 : counts[i - from];
            for (int n = 0; n < used; ++n) {
                if (stack.hasCraftingRemainingItem()) {
                    leftovers.add(stack.getCraftingRemainingItem());
                } else if (overrides != null && overrides.containsKey(stack.getItem())) {
                    leftovers.add(overrides.get(stack.getItem()).getDefaultInstance());
                }
            }
        }
        return leftovers;
    }

    /**
     * "Served in" for a recipe that hasn't run yet: its container, or else its result's crafting remainder (the same
     * rule as the stations' isContainerValid once the meal is waiting).
     */
    public static Predicate<ItemStack> servedIn(ItemStack recipeContainer, ItemStack result) {
        ItemStack container = recipeContainer.isEmpty() ? result.getCraftingRemainingItem() : recipeContainer;
        return stack -> !container.isEmpty() && container.sameItem(stack);
    }
}
