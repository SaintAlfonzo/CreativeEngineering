package com.SaintAlfonzo.creativeengineering.items;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import javax.annotation.Nullable;

public class CreosoteSoakedSawdustItem extends Item {
    public CreosoteSoakedSawdustItem() {
        super(new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(64));
    }
    
    @Override
    public int getBurnTime(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.item.crafting.RecipeType<?> recipeType) {
        return 250; // 200 = 1 item smelted, so 400 = 2 items
    }
}
