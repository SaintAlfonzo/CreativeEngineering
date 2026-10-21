package net.satisfy.vinery.core.registry;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Port: one 1.19.2 creative tab. Items appear in registration order (upstream's essentials / wines / dark cherry /
 * decoration side tabs are a Foundation client feature that isn't ported, P22).
 */
public class TabRegistry {
    public static final CreativeModeTab VINERY_TAB = new CreativeModeTab("vinery.tab") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(ObjectRegistry.CHENET_WINE_ITEM.get());
        }

        @Override
        public void fillItemList(@NotNull NonNullList<ItemStack> items) {
            super.fillItemList(items);
        }
    };
}
