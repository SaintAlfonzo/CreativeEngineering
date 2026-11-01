package net.satisfy.foundation.util;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The classic "just give me a list" container. Implement {@link #getItems()}, done.
 * All slots accessible from all sides by default.
 */
@FunctionalInterface
public interface ImplementedInventory extends WorldlyContainer {
    NonNullList<ItemStack> getItems();

    static ImplementedInventory of(NonNullList<ItemStack> items) {
        return () -> items;
    }

    static ImplementedInventory ofSize(int size) {
        return of(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    @Override
    default void setChanged() {
    }

    @Override
    default int getContainerSize() {
        return this.getItems().size();
    }

    @Override
    default boolean isEmpty() {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            if (!this.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default @NotNull ItemStack getItem(int slot) {
        return this.getItems().get(slot);
    }

    @Override
    default @NotNull ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(this.getItems(), slot, count);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    default @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.getItems(), slot);
    }

    @Override
    default void setItem(int slot, ItemStack stack) {
        this.getItems().set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    default void clearContent() {
        this.getItems().clear();
    }

    @Override
    default int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        int[] result = new int[this.getItems().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }
        return result;
    }

    @Override
    default boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, @Nullable Direction side) {
        return true;
    }

    @Override
    default boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, @NotNull Direction side) {
        return true;
    }

    @Override
    default boolean stillValid(@NotNull Player player) {
        return true;
    }
}
