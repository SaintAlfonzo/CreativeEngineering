package com.SaintAlfonzo.creativedelight.farmers.block;

import com.SaintAlfonzo.creativedelight.farmers.FDBackportRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Rope Fence (1.3.4 RopeFenceBlock): a thin post that connects to other rope fences, rope fence gates and solid
 * faces, collides at fence height and takes a lead. Wooden and other fences do not connect to it
 * ({@code RopeFenceConnectionMixin}).
 */
public class RopeFenceBlock extends CrossCollisionBlock {

	public static final VoxelShape POST = Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0);

	public RopeFenceBlock(Properties properties) {
		super(1.0F, 1.0F, 16.0F, 16.0F, 24.0F, properties);
		registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false)
				.setValue(WEST, false).setValue(WATERLOGGED, false));
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) {
			ItemStack stack = player.getItemInHand(hand);
			return stack.is(Items.LEAD) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		}
		return LeadItem.bindPlayerMobs(player, level, pos);
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return POST;
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return POST;
	}

	public boolean connectsTo(BlockState state, boolean isSideSolid, Direction direction) {
		boolean ropeGate = state.is(FDBackportRegistry.ROPE_FENCE_GATE.get()) && FenceGateBlock.connectsToDirection(state, direction);
		return !isExceptionForConnection(state) && isSideSolid || state.is(this) || ropeGate;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockGetter level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		FluidState fluid = context.getLevel().getFluidState(pos);
		BlockState state = super.getStateForPlacement(context);
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos side = pos.relative(dir);
			BlockState sideState = level.getBlockState(side);
			state = state.setValue(PROPERTY_BY_DIRECTION.get(dir),
					connectsTo(sideState, sideState.isFaceSturdy(level, side, dir.getOpposite()), dir.getOpposite()));
		}
		return state.setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED)) level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		if (facing.getAxis().getPlane() != Direction.Plane.HORIZONTAL) {
			return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
		}
		return state.setValue(PROPERTY_BY_DIRECTION.get(facing),
				connectsTo(facingState, facingState.isFaceSturdy(level, facingPos, facing.getOpposite()), facing.getOpposite()));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
	}
}
