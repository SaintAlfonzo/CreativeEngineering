package com.SaintAlfonzo.creativedelight.farmers.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.PieBlock;

import java.util.function.Supplier;

/** The placed vanilla Pumpkin Pie (1.3.4 ModBlocks.PUMPKIN_PIE): an FD pie whose pick-block is the vanilla pie. */
public class PumpkinPieBlock extends PieBlock {

	public PumpkinPieBlock(Properties properties, Supplier<Item> pieSlice) {
		super(properties, pieSlice);
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
		return new ItemStack(Items.PUMPKIN_PIE);
	}
}
