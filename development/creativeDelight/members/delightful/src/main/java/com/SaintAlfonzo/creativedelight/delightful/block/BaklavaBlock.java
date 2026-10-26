package com.SaintAlfonzo.creativedelight.delightful.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.PieBlock;

import java.util.function.Supplier;

/** A flatter pie (3.8.1 BaklavaBlock): four slices, 5 px high. */
public class BaklavaBlock extends PieBlock {
	private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 5.0, 15.0);

	public BaklavaBlock(Properties properties, Supplier<Item> slice) {
		super(properties, slice);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
}
