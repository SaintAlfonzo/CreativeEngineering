package com.SaintAlfonzo.creativedelight.delightful.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import vectorwing.farmersdelight.common.item.ConsumableItem;

/**
 * A soup in a cup (3.8.1 CupItem): gives back Miner's Delight's Copper Cup, or a bowl without it. Looked up when
 * used, not at registration, because Miner's Delight may register its items after Delightful.
 */
public class CupItem extends ConsumableItem {
	private static final ResourceLocation COPPER_CUP = new ResourceLocation("miners_delight", "copper_cup");

	public CupItem(Properties properties) {
		super(properties, true);
	}

	@Override
	public boolean hasCraftingRemainingItem(ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack getCraftingRemainingItem(ItemStack stack) {
		Item cup = ForgeRegistries.ITEMS.getValue(COPPER_CUP);
		return new ItemStack(cup != null && cup != Items.AIR ? cup : Items.BOWL);
	}
}
