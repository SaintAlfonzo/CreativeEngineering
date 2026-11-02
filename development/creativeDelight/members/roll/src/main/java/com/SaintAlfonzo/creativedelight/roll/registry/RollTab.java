package com.SaintAlfonzo.creativedelight.roll.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** The Roll Delight creative tab; items list in registration order. */
public final class RollTab {
    public static final CreativeModeTab TAB = new CreativeModeTab("roll_delight") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(RollItems.SALMON_MAKI_ROLL.get());
        }
    };

    private RollTab() {
    }
}
