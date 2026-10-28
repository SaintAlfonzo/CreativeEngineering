package ce.shared;

import net.minecraft.world.item.Item;

import java.util.function.Predicate;

/**
 * Items kept out of every creative tab and the creative search: {@code mixin.ItemHiddenMixin} answers false from
 * {@code Item.allowedIn} for an item that matches the test set here (vanilla's and Blueprint's fillItemCategory both
 * check it, including for the search tab). The mod sets the test once, at construction. Shared source
 * (development/shared/itemhidden, compiled into each jar under its own package; feature_writeup/pack_tooling.md).
 */
public final class HiddenItems {
    private static volatile Predicate<Item> test = item -> false;

    private HiddenItems() {
    }

    public static void set(Predicate<Item> hidden) {
        test = hidden;
    }

    public static boolean isHidden(Item item) {
        return test.test(item);
    }
}
