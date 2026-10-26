package com.SaintAlfonzo.creativedelight.delightful.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/** A milkshake-filled cauldron (3.8.1 DelightfulMilkshakeCauldronBlock): three levels, never filled by rain or snow. */
public class MilkshakeCauldronBlock extends LayeredCauldronBlock {

	public MilkshakeCauldronBlock(Map<Item, CauldronInteraction> interactions) {
		super(Properties.copy(Blocks.CAULDRON), precipitation -> false, interactions);
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
		return new ItemStack(Items.CAULDRON);
	}
}
