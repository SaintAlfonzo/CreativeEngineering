package net.mehdinoui.veggiesdelight.common.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

// 1.19.2 creative tabs are CreativeModeTab subclasses (upstream registers a 1.20 tab with an explicit item list).
public class ModCreativeTab {
    public static final CreativeModeTab TAB = new CreativeModeTab("veggiesdelight_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.BROCCOLI.get());
        }
    };
}
