package com.SaintAlfonzo.creativedelight.oceanic.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** The Oceanic Delight creative tab (upstream's label key, so its lang names it), no search bar. */
public final class OceanicTab {
    public static final CreativeModeTab TAB = new CreativeModeTab("tab_oceanic_delight") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(OceanicItems.STUFFED_PUFFERFISH.get());
        }
    };

    private OceanicTab() {
    }
}
