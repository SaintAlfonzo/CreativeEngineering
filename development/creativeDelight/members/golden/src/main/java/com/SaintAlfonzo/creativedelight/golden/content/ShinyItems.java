package com.SaintAlfonzo.creativedelight.golden.content;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/** The enchanted forms that keep a vanilla item class's use (cake block) and always show the glint. */
public final class ShinyItems {
	private ShinyItems() {
	}

	public static class Block extends BlockItem {
		public Block(net.minecraft.world.level.block.Block block, Properties properties) {
			super(block, properties);
		}

		@Override
		public boolean isFoil(ItemStack stack) {
			return true;
		}
	}
}
