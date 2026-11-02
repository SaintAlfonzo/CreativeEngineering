package com.sammy.minersdelight.content.block.copper_pot;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity.CONTAINER_SLOT;
import static com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity.INPUT_SLOTS;
import static com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity.LEFTOVER_SLOT;
import static com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity.OUTPUT_SLOT;

/** Automation view of the pot: from above, the four inputs; from the sides and below, containers in and meals out. */
public class CopperPotItemHandler implements IItemHandler {
    private final IItemHandler inventory;
    private final boolean top;

    public CopperPotItemHandler(IItemHandler inventory, @Nullable Direction side) {
        this.inventory = inventory;
        this.top = side == null || side == Direction.UP;
    }

    @Override
    public int getSlots() {
        return inventory.getSlots();
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        boolean allowed = top ? slot < INPUT_SLOTS : slot == CONTAINER_SLOT;
        return allowed ? inventory.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        boolean allowed = top ? slot < INPUT_SLOTS : slot == OUTPUT_SLOT || slot == LEFTOVER_SLOT;
        return allowed ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return inventory.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return inventory.isItemValid(slot, stack);
    }
}
