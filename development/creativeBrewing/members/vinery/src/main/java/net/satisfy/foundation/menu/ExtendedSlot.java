package net.satisfy.foundation.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class ExtendedSlot extends Slot {
    private final Predicate<ItemStack> filter;

    public ExtendedSlot(Container container, int index, int x, int y) {
        this(container, index, x, y, stack -> true);
    }

    public ExtendedSlot(Container container, int index, int x, int y, Predicate<ItemStack> filter) {
        super(container, index, x, y);
        this.filter = filter;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return filter.test(stack);
    }
}
