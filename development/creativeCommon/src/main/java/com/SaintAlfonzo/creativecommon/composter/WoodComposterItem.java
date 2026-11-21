// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.composter;

import javax.annotation.Nullable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

/** The composter's item: furnace fuel like vanilla's composter (300 ticks), none for the nether woods. */
public class WoodComposterItem extends BlockItem {

	private final int burnTime;

	public WoodComposterItem(Block block, Properties props, int burnTime) {
		super(block, props);
		this.burnTime = burnTime;
	}

	@Override
	public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
		return burnTime;
	}
}
