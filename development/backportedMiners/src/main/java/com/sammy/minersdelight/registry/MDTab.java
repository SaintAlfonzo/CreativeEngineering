package com.sammy.minersdelight.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** The Miner's Delight creative tab (1.19.2 tabs are CreativeModeTab subclasses); items list in registration order. */
public final class MDTab {
    public static final CreativeModeTab TAB = new CreativeModeTab("miners_delight") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(MDItems.COPPER_POT.get());
        }
    };

    private MDTab() {
    }
}
