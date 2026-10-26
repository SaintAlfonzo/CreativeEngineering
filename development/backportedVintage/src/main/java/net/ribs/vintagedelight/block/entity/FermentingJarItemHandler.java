package net.ribs.vintagedelight.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * KOTS 0.3.1's sided view of the Pickling Jar's slots: the top face feeds the 6 ingredient slots (spreading one item
 * per empty slot first, then topping up the smallest matching stack), the sides feed the container slot, the bottom
 * extracts the two outputs and (port) the leftover slot. A null side (the screen) sees every slot.
 */
public class FermentingJarItemHandler {
    private final IItemHandler itemHandler;

    public FermentingJarItemHandler(IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
    }

    public IItemHandler forSide(@Nullable Direction side) {
        if (side == null) {
            return this.itemHandler;
        }
        final int[] exposed = exposedSlots(side);
        final boolean inputView = side == Direction.UP;
        return new IItemHandler() {
            @Override
            public int getSlots() {
                return exposed.length;
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return itemHandler.getStackInSlot(map(slot, exposed));
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return inputView ? insertSpread(stack, exposed, simulate) : itemHandler.insertItem(map(slot, exposed), stack, simulate);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return itemHandler.extractItem(map(slot, exposed), amount, simulate);
            }

            @Override
            public int getSlotLimit(int slot) {
                return itemHandler.getSlotLimit(map(slot, exposed));
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return itemHandler.isItemValid(map(slot, exposed), stack);
            }
        };
    }

    private static int[] exposedSlots(Direction side) {
        if (side == Direction.UP) {
            return new int[]{0, 1, 2, 3, 4, 5};
        }
        return side == Direction.DOWN ? new int[]{7, 8, 9} : new int[]{6};
    }

    private static int map(int slot, int[] exposed) {
        if (slot < 0 || slot >= exposed.length) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + exposed.length + ")");
        }
        return exposed[slot];
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
