package com.SaintAlfonzo.creativedelight.rustic.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** The Rustic Delight creative tab (1.19.2 tabs are CreativeModeTab subclasses); items list in registration order. */
public final class RDTab {
    public static final CreativeModeTab TAB = new CreativeModeTab("rusticdelight") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(RDItems.PEPPERS.get("red").get());
        }
    };

    private RDTab() {
    }
}
