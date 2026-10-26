package com.SaintAlfonzo.creativedelight.delightful.item;

import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.item.ConsumableItem;

/** A gummy (3.8.1 GummyItem): eaten in 14 ticks, effects in the tooltip. */
public class GummyItem extends ConsumableItem {

	public GummyItem(Properties properties) {
		super(properties, true);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 14;
	}
}
