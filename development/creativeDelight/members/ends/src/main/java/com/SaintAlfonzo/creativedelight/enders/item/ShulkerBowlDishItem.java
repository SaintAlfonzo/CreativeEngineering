package com.SaintAlfonzo.creativedelight.enders.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import vectorwing.farmersdelight.common.item.ConsumableItem;

/**
 * A Farmer's Delight dish served in Ender's Delight's Shulker Bowl, which is left behind when eaten or crafted. The bowl
 * belongs to Ender's Delight's jar and may not exist yet when these items are built, so it is looked up on first use
 * instead of set through Properties.craftRemainder.
 */
public class ShulkerBowlDishItem extends ConsumableItem {
    private static final ResourceLocation SHULKER_BOWL = new ResourceLocation("endersdelight", "shulker_bowl");
    private static Item bowl;

    public ShulkerBowlDishItem(Properties properties, boolean hasFoodEffectTooltip) {
        super(properties, hasFoodEffectTooltip);
    }

    private static Item bowl() {
        if (bowl == null) {
            Item found = ForgeRegistries.ITEMS.getValue(SHULKER_BOWL);
            if (found == null || found == Items.AIR) return null;
            bowl = found;
        }
        return bowl;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return bowl() != null;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        Item b = bowl();
        return b == null ? ItemStack.EMPTY : new ItemStack(b);
    }
}
