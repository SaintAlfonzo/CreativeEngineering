package com.SaintAlfonzo.creativedelight.cooking;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * A station's automation handler with the {@link RemainderBuffer} slots added after its own: slots below
 * {@code inner.getSlots()} behave as before, the buffer slots extract only. Hoppers and funnels on the station's
 * output faces then take the leftovers too (after the output slot, which comes first).
 */
public class RemainderOutputHandler implements IItemHandler {

    private final IItemHandler inner;
    private final IItemHandler buffer;

    public RemainderOutputHandler(IItemHandler inner, IItemHandler buffer) {
        this.inner = inner;
        this.buffer = buffer;
    }

    @Override
    public int getSlots() {
        return inner.getSlots() + buffer.getSlots();
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        int n = inner.getSlots();
        return slot < n ? inner.getStackInSlot(slot) : buffer.getStackInSlot(slot - n);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return slot < inner.getSlots() ? inner.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        int n = inner.getSlots();
        return slot < n ? inner.extractItem(slot, amount, simulate) : buffer.extractItem(slot - n, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        int n = inner.getSlots();
        return slot < n ? inner.getSlotLimit(slot) : buffer.getSlotLimit(slot - n);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return slot < inner.getSlots() && inner.isItemValid(slot, stack);
    }
}
