package com.SaintAlfonzo.creativedelight.delightful.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.item.DrinkableItem;

/** A milkshake (3.8.1 ShakeItem): drunk in 2 s with the honey drinking sound, like Neapolitan's. */
public class ShakeItem extends DrinkableItem {

	public ShakeItem(Properties properties) {
		super(properties, false, false);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public SoundEvent getDrinkingSound() {
		return SoundEvents.HONEY_DRINK;
	}

	@Override
	public SoundEvent getEatingSound() {
		return SoundEvents.HONEY_DRINK;
	}
}
