package com.SaintAlfonzo.creativedelight.golden.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** The Golden Food creative tab (upstream's label key, so its lang names it), the golden cake as icon. */
public final class GoldenTab {
    public static final CreativeModeTab TAB = new CreativeModeTab("gf_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(GoldenItems.GOLDEN_CAKE.get());
        }
    };

    private GoldenTab() {
    }
}
