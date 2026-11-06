package net.ribs.vintagedelight.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * Sided view of the Pickling Jar's slots, the Farmer's Delight stations' layout (remainder_buffer.md 3.8): every face
 * inserts, a container (what the container slot takes, FermentingJarBlockEntity.isJarContainer) into the container slot
 * and anything else into the 6 ingredient slots (KOTS 0.3.1's spread: one item per empty slot first, then the smallest
 * matching stack); the top extracts the ingredients, the sides and the bottom the two outputs and then the leftover
 * slot. The container slot is never extracted, so automation cannot pull a batch's container. A null side (the
 * screen) sees every slot. Was (KOTS): only the top fed ingredients, the sides fed and drained the container slot,
 * only the bottom drained the outputs.
 */
public class FermentingJarItemHandler {
    private static final int CONTAINER_SLOT = 6;
    private static final int[] INGREDIENTS = {0, 1, 2, 3, 4, 5};
    private static final int[] OUTPUTS = {7, 8, 9};
    /** What a face shows: the top its ingredients and the container slot, the other faces everything, outputs first. */
    private static final int[] TOP_VIEW = {0, 1, 2, 3, 4, 5, 6};
    private static final int[] SIDE_VIEW = {7, 8, 9, 6, 0, 1, 2, 3, 4, 5};

    private final IItemHandler itemHandler;

    public FermentingJarItemHandler(IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
    }

    public IItemHandler forSide(@Nullable Direction side) {
        if (side == null) {
            return this.itemHandler;
        }
        final int[] view = side == Direction.UP ? TOP_VIEW : SIDE_VIEW;
        final int[] pull = side == Direction.UP ? INGREDIENTS : OUTPUTS;
        return new IItemHandler() {
            @Override
            public int getSlots() {
                return view.length;
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return itemHandler.getStackInSlot(map(slot, view));
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                map(slot, view);
                return insertRouted(stack, simulate);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                int real = map(slot, view);
                for (int p : pull) {
                    if (p == real) {
                        return itemHandler.extractItem(real, amount, simulate);
                    }
                }
                return ItemStack.EMPTY;
            }

            @Override
            public int getSlotLimit(int slot) {
                return itemHandler.getSlotLimit(map(slot, view));
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return itemHandler.isItemValid(map(slot, view), stack);
            }
        };
    }

    private static int map(int slot, int[] view) {
        if (slot < 0 || slot >= view.length) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + view.length + ")");
        }
        return view[slot];
    }

    /** A container into the container slot, anything else spread over the ingredient slots. */
    private ItemStack insertRouted(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return stack;
        }
        if (this.itemHandler.isItemValid(CONTAINER_SLOT, stack)) {
            return this.itemHandler.insertItem(CONTAINER_SLOT, stack, simulate);
        }
        return insertSpread(stack, INGREDIENTS, simulate);
    }

    private ItemStack insertSpread(ItemStack stack, int[] exposed, boolean simulate) {
        if (stack.isEmpty()) {
            return stack;
        }
        ItemStack remaining = stack.copy();
        ItemStack single = stack.copy();
        single.setCount(1);
        for (int slot : exposed) {
            if (remaining.isEmpty()) {
                break;
            }
            if (this.itemHandler.getStackInSlot(slot).isEmpty() && this.itemHandler.insertItem(slot, single, true).isEmpty()
                && this.itemHandler.insertItem(slot, single, simulate).isEmpty()) {
                remaining.shrink(1);
            }
        }
        while (!remaining.isEmpty()) {
            int best = -1;
            int bestCount = Integer.MAX_VALUE;
            for (int slot : exposed) {
                ItemStack inSlot = this.itemHandler.getStackInSlot(slot);
                if (!inSlot.isEmpty() && ItemStack.isSameItemSameTags(inSlot, remaining)
                    && this.itemHandler.insertItem(slot, single, true).isEmpty() && inSlot.getCount() < bestCount) {
                    bestCount = inSlot.getCount();
                    best = slot;
                }
            }
            if (best == -1) {
                break;
            }
            int before = remaining.getCount();
            remaining = this.itemHandler.insertItem(best, remaining, simulate);
            if (remaining.getCount() == before || simulate) {
                break;
            }
        }
        return remaining;
    }
}
