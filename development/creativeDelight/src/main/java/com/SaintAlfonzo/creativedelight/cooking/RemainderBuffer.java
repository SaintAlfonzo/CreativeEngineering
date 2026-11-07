package com.SaintAlfonzo.creativedelight.cooking;

import com.SaintAlfonzo.creativecompat.compat.RemainderView;
import com.SaintAlfonzo.creativedelight.CDConfig;
import com.SaintAlfonzo.creativedelight.shared.LeftoverPlan;
import com.SaintAlfonzo.creativedelight.shared.OverflowPull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Holding slots for ingredient leftovers (glass bottles, bowls, buckets) on Farmer's Delight style stations, so they
 * stay in the station instead of being thrown out of it (feature_writeup/remainder_buffer.md). Used by the FD cooking
 * pot and the Farmer's Respite kettle (mixin/CookingPotRemainderMixin, KettleRemainderMixin) and by the oven.
 *
 * Where a leftover goes is the shared rule {@link LeftoverPlan} (remainder_buffer.md 3.8), the same on the copper pot
 * and the Pickling Jar:
 *  1. the station's container slot, if it already holds the same item, up to the slot's stack size;
 *  2. these slots, merging into a matching stack before taking an empty one;
 *  3. an empty container slot, if these slots are full and it is what the waiting meal is served in;
 *  4. out of the station as before, for whatever is left. With {@code [cooking] stallWhenFull} the station checks
 *     {@link #fits} before it starts a craft, so this only happens with that switch off.
 *
 * A leftover in these slots that can plate the waiting meal moves to the container slot after
 * {@code [cooking] recycleShowTicks} ({@link #pullForPlating}, shared {@link OverflowPull}); a container that reaches
 * the container slot this way or by step 1 or 3 holds the station's plating for the same ticks ({@link #holdPlating}),
 * so it is seen in its slot before the meal takes it.
 */
public class RemainderBuffer extends ItemStackHandler implements RemainderView {

    public static final int SLOTS = 2;

    private final Runnable onChanged;
    // Game time of the last craft check that failed for want of room here (not saved): WTHIT's "slots full" line.
    private long stalledAt = -1000L;
    // Plating calls still to skip after a recycle into the container slot (not saved: a reload plates at once).
    private int plateHold;
    private final OverflowPull pull = new OverflowPull();

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
     * Once a server tick: a leftover in these slots that the waiting meal is plated with ({@code platesMeal}) moves to
     * the station's container slot after {@code [cooking] recycleShowTicks}, as many as the {@code mealsWaiting}
     * still need; then plating waits the same ticks ({@link #holdPlating}). Returns true when it moved something.
     */
    public boolean pullForPlating(IItemHandlerModifiable inventory, int containerSlot, int mealsWaiting, Predicate<ItemStack> platesMeal) {
        int delay = CDConfig.COOKING.recycleShowTicks.get();
        RemainderBuffer self = this;
        boolean moved = pull.tick(new OverflowPull.Slots() {
            @Override
            public ItemStack get(int slot) {
                return slot == 0 ? inventory.getStackInSlot(containerSlot) : self.getStackInSlot(slot - 1);
            }

            @Override
            public void set(int slot, ItemStack stack) {
                if (slot == 0) inventory.setStackInSlot(containerSlot, stack);
                else self.setStackInSlot(slot - 1, stack);
            }

            @Override
            public int limit(int slot) {
                return slot == 0 ? inventory.getSlotLimit(containerSlot) : self.getSlotLimit(slot - 1);
            }
        }, 0, OVERFLOW_VIEW, mealsWaiting, platesMeal, delay);
        if (moved) plateHold = delay;
        return moved;
    }

    /** The buffer slots in pullForPlating's view (0 = the container slot). */
    private static final int[] OVERFLOW_VIEW = {1, 2};

    /**
     * Stores one leftover. {@code inventory}/{@code containerSlot} is the station's container (plating) slot and
     * {@code servedIn} tells whether an item is what the waiting meal is served in. Returns what didn't fit.
     */
    public ItemStack store(ItemStack leftover, IItemHandlerModifiable inventory, int containerSlot, Predicate<ItemStack> servedIn) {
        ItemStack[] slots = plan(inventory, containerSlot);
        List<ItemStack> rest = LeftoverPlan.place(List.of(leftover), slots, limits(inventory, containerSlot), servedIn);
        if (slots[0].getCount() > inventory.getStackInSlot(containerSlot).getCount()) {
            plateHold = CDConfig.COOKING.recycleShowTicks.get();
        }
        if (!ItemStack.matches(slots[0], inventory.getStackInSlot(containerSlot))) {
            inventory.setStackInSlot(containerSlot, slots[0]);
        }
        for (int i = 0; i < SLOTS; ++i) {
            if (!ItemStack.matches(slots[i + 1], getStackInSlot(i))) {
                setStackInSlot(i, slots[i + 1]);
            }
        }
        return rest.isEmpty() ? ItemStack.EMPTY : rest.get(0);
    }

    /** Whether all of one craft's leftovers would fit (container slot + these slots), checked before the craft. */
    public boolean fits(List<ItemStack> leftovers, IItemHandler inventory, int containerSlot, Predicate<ItemStack> servedIn) {
        if (leftovers.isEmpty()) return true;
        return LeftoverPlan.place(leftovers, plan(inventory, containerSlot), limits(inventory, containerSlot), servedIn).isEmpty();
    }

    /** Copies of [container slot, buffer slots]. */
    private ItemStack[] plan(IItemHandler inventory, int containerSlot) {
        ItemStack[] slots = new ItemStack[SLOTS + 1];
        slots[0] = inventory.getStackInSlot(containerSlot).copy();
        for (int i = 0; i < SLOTS; ++i) {
            slots[i + 1] = getStackInSlot(i).copy();
        }
        return slots;
    }

    private int[] limits(IItemHandler inventory, int containerSlot) {
        int[] limits = new int[SLOTS + 1];
        limits[0] = inventory.getSlotLimit(containerSlot);
        for (int i = 0; i < SLOTS; ++i) {
            limits[i + 1] = getSlotLimit(i);
        }
        return limits;
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
