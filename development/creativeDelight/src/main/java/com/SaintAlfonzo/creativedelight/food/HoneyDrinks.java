// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.food;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * {@code #creativedelight:honey_drink_sound}: items drunk with the honey bottle's sounds
 * (mixin.minecraft.ItemStackHoneyDrinkMixin; the smoothies, TODO sweets #8).
 */
public final class HoneyDrinks {

	public static final TagKey<Item> TAG = ItemTags.create(new ResourceLocation("creativedelight", "honey_drink_sound"));

	private HoneyDrinks() {}

	public static boolean is(ItemStack stack) {
		return !stack.isEmpty() && stack.is(TAG);
	}
}
