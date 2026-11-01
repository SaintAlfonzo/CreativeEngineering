package com.SaintAlfonzo.creativedelight.treatedstick;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A placed treated stick (feature_writeup/treated_sticks.md): up to three sticks in one block, one per axis, like
 * Supplementaries' stick block (same state names {@code axis_x/axis_y/axis_z}, so its models can be parents). Drops
 * one treated stick per axis. Has no block item: {@link TreatedSticks#useOn} places it from the IE item.
 */
public class TreatedStickBlock extends Block implements SimpleWaterloggedBlock {

	public static final BooleanProperty AXIS_X = BooleanProperty.create("axis_x");
	public static final BooleanProperty AXIS_Y = BooleanProperty.create("axis_y");
	public static final BooleanProperty AXIS_Z = BooleanProperty.create("axis_z");
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private static final VoxelShape Y_SHAPE = Block.box(7, 0, 7, 9, 16, 9);
	private static final VoxelShape Z_SHAPE = Block.box(7, 7, 0, 9, 9, 16);
	private static final VoxelShape X_SHAPE = Block.box(0, 7, 7, 16, 9, 9);

	public TreatedStickBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false)
				.setValue(AXIS_X, false).setValue(AXIS_Y, true).setValue(AXIS_Z, false));
	}

	public static BooleanProperty axisProperty(Direction.Axis axis) {
		return switch (axis) {
			case X -> AXIS_X;
			case Y -> AXIS_Y;
			case Z -> AXIS_Z;
		};
	}

	public static VoxelShape stickShape(boolean x, boolean y, boolean z) {
		VoxelShape shape = Shapes.empty();
		if (x) shape = Shapes.or(shape, X_SHAPE);
		if (y) shape = Shapes.or(shape, Y_SHAPE);
		if (z) shape = Shapes.or(shape, Z_SHAPE);
		return shape.isEmpty() ? Y_SHAPE : shape;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED, AXIS_X, AXIS_Y, AXIS_Z);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return stickShape(state.getValue(AXIS_X), state.getValue(AXIS_Y), state.getValue(AXIS_Z));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
		Direction.Axis axis = context.getClickedFace().getAxis();
		if (existing.is(this)) return existing.setValue(axisProperty(axis), true);
		if (existing.getBlock() instanceof TreatedStickTomatoBlock && axis != Direction.Axis.Y) {
			return existing.setValue(axisProperty(axis), true);
		}
		boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
		return defaultBlockState().setValue(WATERLOGGED, water).setValue(AXIS_Y, false).setValue(axisProperty(axis), true);
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		if (!context.isSecondaryUseActive() && TreatedSticks.isTreatedStick(context.getItemInHand())
				&& !state.getValue(axisProperty(context.getClickedFace().getAxis()))) {
			return true;
		}
		return super.canBeReplaced(state, context);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
			BlockPos pos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		return super.updateShape(state, facing, facingState, level, pos, facingPos);
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
		return false;
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
		return state.getValue(WATERLOGGED) ? 0 : 60;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
		return state.getValue(WATERLOGGED) ? 0 : 60;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		if (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180) return state;
		return state.setValue(AXIS_X, state.getValue(AXIS_Z)).setValue(AXIS_Z, state.getValue(AXIS_X));
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		int n = (state.getValue(AXIS_X) ? 1 : 0) + (state.getValue(AXIS_Y) ? 1 : 0) + (state.getValue(AXIS_Z) ? 1 : 0);
		return n == 0 ? List.of() : List.of(new ItemStack(TreatedSticks.item(), n));
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		return new ItemStack(TreatedSticks.item());
	}
}
