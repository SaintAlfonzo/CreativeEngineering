package com.SaintAlfonzo.creativedelight.largemeals.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** The Large Meals creative tab; items list in registration order. */
public final class MealTab {
    public static final CreativeModeTab TAB = new CreativeModeTab("largemeals") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(MealItems.OMURICE_BLOCK.get());
        }
    };

    private MealTab() {
    }
}
