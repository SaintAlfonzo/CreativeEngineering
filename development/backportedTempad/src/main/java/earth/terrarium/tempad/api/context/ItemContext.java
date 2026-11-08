// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/context/ItemContext.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.api.context;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public interface ItemContext {
    ItemStack getStack();

    void setStack(ItemStack stack);

    void addStack(ItemStack stack);

    default void exchange(ItemStack toInsert) {
        ItemStack old = getStack().copy();
        if (old.getCount() > toInsert.getCount()) {
            old.shrink(toInsert.getCount());
            setStack(old);
            addStack(toInsert);
        } else if (old.getCount() == toInsert.getCount()) {
            setStack(toInsert);
        } else {
            ItemStack copy = toInsert.copy();
            copy.shrink(old.getCount());
            setStack(copy);
        }
    }

    /** Upstream {@code ItemContext.modify}: runs on the stack, then writes it back. */
    default void modify(Consumer<ItemStack> execute) {
        ItemStack stack = getStack();
        execute.accept(stack);
        setStack(stack);
    }

    /** Upstream {@code Container.ctx(slot, dropper)}. */
    static ItemContext of(Container container, int slot, Consumer<ItemStack> dropper) {
        return new ItemContext() {
            @Override
            public ItemStack getStack() {
                return container.getItem(slot);
            }

            @Override
            public void setStack(ItemStack stack) {
                container.setItem(slot, stack);
            }

            @Override
            public void addStack(ItemStack stack) {
                dropper.accept(stack);
            }
        };
    }
}
