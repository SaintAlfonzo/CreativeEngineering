package com.SaintAlfonzo.creativeengineering.items;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PrecisionSawbladeItem extends Item {

    public PrecisionSawbladeItem() {
        super(new Item.Properties()
        		.tab(CreativeModeTab.TAB_MISC)
            .stacksTo(1)
            .durability(5000));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }
}
