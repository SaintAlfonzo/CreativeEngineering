package com.SaintAlfonzo.creativedelight.farmers.block;

import com.SaintAlfonzo.creativedelight.farmers.FDBackportRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.FeastBlock;

import java.util.function.Supplier;

/** Gleaming Salad (1.3.4 GleamingSaladBlock): a 4-serving bowl feast that glows and sparkles while servings are left. */
public class GleamingSaladBlock extends FeastBlock {

	protected static final VoxelShape SHAPE = Shapes.or(Block.box(3.0, 0.0, 3.0, 13.0, 2.0, 13.0), Block.box(2.0, 2.0, 2.0, 14.0, 6.0, 14.0));

	public GleamingSaladBlock(Properties properties, Supplier<Item> servingItem, boolean hasLeftovers) {
		super(properties, servingItem, hasLeftovers);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		int servings = state.getValue(SERVINGS);
		if (servings == 0 || random.nextFloat() >= 0.05F * servings) return;
		double x = pos.getX() + 0.5 + (random.nextDouble() * 0.5 - 0.25);
		double y = pos.getY() + 0.5;
		double z = pos.getZ() + 0.5 + (random.nextDouble() * 0.5 - 0.25);
		level.addParticle(FDBackportRegistry.SPARKLE.get(), x, y, z, 0.0, 0.0, 0.0);
	}
}
