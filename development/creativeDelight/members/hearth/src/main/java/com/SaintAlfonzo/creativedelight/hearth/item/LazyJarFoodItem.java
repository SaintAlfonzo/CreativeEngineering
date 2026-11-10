package com.SaintAlfonzo.creativedelight.hearth.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import vectorwing.farmersdelight.common.item.ConsumableItem;

/** A pickle that gives back the pack's mason jar. Resolved by id: the jar is registered by another member. */
public class LazyJarFoodItem extends ConsumableItem {
    private static final ResourceLocation JAR = new ResourceLocation("vintagedelight", "empty_mason_jar");

    public LazyJarFoodItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(JAR));
    }
}
