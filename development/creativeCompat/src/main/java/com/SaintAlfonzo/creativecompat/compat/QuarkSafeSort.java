package com.SaintAlfonzo.creativecompat.compat;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.slf4j.Logger;
import vazkii.quark.base.handler.SortingHandler;

/**
 * Item-safe replacement for Quark's {@code SortingHandler.sortInventory(IItemHandler, int, int, int[])}.
 *
 * <p>Quark's version deletes items when a slot holds more than the item's max stack size (stacks
 * stored before the 1.0.5 stack-size policy lowered bowl meals / drinks to 32, golden foods to 16,
 * etc.). It merges the stacks, extracts every slot, then re-inserts; Forge's InvWrapper only
 * accepts {@code min(maxStackSize, slotLimit)} per slot, so the oversized stack fails half
 * inserted. Quark then "restores" the original layout through the same insert path, which fails on
 * the same oversized stack, ignores that result, and stops: the overflow and every later slot's
 * items are gone. A 2-tall fridge (54 slots) loses the most because more slots follow the failure.
 *
 * <p>Here the merged, sorted list is split into legal stacks first (so sorting also repairs
 * oversized stacks), and the whole layout is validated before anything is touched: if it would not
 * fit, the sort does nothing. Anything an unusual handler still refuses is pushed back into free
 * slots and logged, never dropped silently.
 */
public final class QuarkSafeSort {

    private static final Logger LOGGER = LogUtils.getLogger();

    private QuarkSafeSort() {
    }

    public static void sort(IItemHandler handler, int iStart, int iEnd, int[] lockedSlots) {
        List<ItemStack> stacks = new ArrayList<>();
        List<Integer> freeSlots = new ArrayList<>();
        for (int i = iStart; i < iEnd; i++) {
            if (isLocked(i, lockedSlots)) {
                continue;
            }
            freeSlots.add(i);
            ItemStack stackAt = handler.getStackInSlot(i);
            if (stackAt.isEmpty()) {
                continue;
            }
            // every unlocked slot is emptied below; one we cannot fully extract means no sort
            ItemStack extractTest = handler.extractItem(i, stackAt.getCount(), true);
            if (extractTest.getCount() != stackAt.getCount()) {
                return;
            }
            stacks.add(stackAt.copy());
        }

        SortingHandler.mergeStacks(stacks);
        SortingHandler.sortStackList(stacks);
        stacks = splitOversized(stacks);

        if (stacks.size() > freeSlots.size()) {
            return;
        }
        for (int j = 0; j < stacks.size(); j++) {
            int slot = freeSlots.get(j);
            ItemStack stack = stacks.get(j);
            if (stack.getCount() > Math.min(stack.getMaxStackSize(), handler.getSlotLimit(slot))
                    || !handler.isItemValid(slot, stack)) {
                return;
            }
        }

        List<ItemStack> leftovers = new ArrayList<>();
        for (int slot : freeSlots) {
            handler.extractItem(slot, handler.getStackInSlot(slot).getCount(), false);
        }
        for (int j = 0; j < stacks.size(); j++) {
            ItemStack rest = handler.insertItem(freeSlots.get(j), stacks.get(j), false);
            if (!rest.isEmpty()) {
                leftovers.add(rest);
            }
        }
        for (ItemStack rest : leftovers) {
            ItemStack lost = ItemHandlerHelper.insertItemStacked(handler, rest, false);
            if (!lost.isEmpty()) {
                LOGGER.error("[quark-sort] {} refused {} after validation; items lost", handler.getClass().getName(), lost);
            }
        }
    }

    private static List<ItemStack> splitOversized(List<ItemStack> stacks) {
        List<ItemStack> out = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            int max = Math.max(1, stack.getMaxStackSize());
            ItemStack rest = stack.copy();
            while (rest.getCount() > max) {
                out.add(rest.split(max));
            }
            out.add(rest);
        }
        return out;
    }

    private static boolean isLocked(int slot, int[] locked) {
        if (locked == null) {
            return false;
        }
        for (int i : locked) {
            if (slot == i) {
                return true;
            }
        }
        return false;
    }
}
