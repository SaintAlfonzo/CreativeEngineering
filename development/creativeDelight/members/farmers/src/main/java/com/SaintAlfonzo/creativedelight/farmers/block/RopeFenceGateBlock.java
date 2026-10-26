package com.SaintAlfonzo.creativedelight.farmers.block;

import com.SaintAlfonzo.creativedelight.farmers.FDBackportRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Rope Fence Gate (1.3.4 RopeFenceGateBlock): a thin gate with the lead-knot sounds; it drops into a wall only
 * between two walls. 1.19.2's FenceGateBlock has no sound constructor (the wooden sounds are level events 1008 /
 * 1014 in use and neighborChanged), so both are vanilla's with the sound swapped.
 */
public class RopeFenceGateBlock extends FenceGateBlock {

	protected static final VoxelShape Z_SHAPE = Block.box(0.0, 0.0, 7.0, 16.0, 14.0, 9.0);
	protected static final VoxelShape X_SHAPE = Block.box(7.0, 0.0, 0.0, 9.0, 14.0, 16.0);
	protected static final VoxelShape Z_COLLISION_SHAPE = Block.box(0.0, 0.0, 7.0, 16.0, 24.0, 9.0);
	protected static final VoxelShape X_COLLISION_SHAPE = Block.box(7.0, 0.0, 0.0, 9.0, 24.0, 16.0);

	public RopeFenceGateBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (state.getValue(OPEN)) {
			state = state.setValue(OPEN, false);
		} else {
			Direction facing = player.getDirection();
			if (state.getValue(FACING) == facing.getOpposite()) state = state.setValue(FACING, facing);
			state = state.setValue(OPEN, true);
		}
		level.setBlock(pos, state, 10);
		boolean open = state.getValue(OPEN);
		playToggleSound(level, pos, player, open);
		level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		if (level.isClientSide) return;
		boolean powered = level.hasNeighborSignal(pos);
		if (state.getValue(POWERED) == powered) return;
		level.setBlock(pos, state.setValue(POWERED, powered).setValue(OPEN, powered), 2);
		if (state.getValue(OPEN) != powered) {
			playToggleSound(level, pos, null, powered);
			level.gameEvent(null, powered ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
		}
	}

	private static void playToggleSound(Level level, BlockPos pos, Player player, boolean open) {
		level.playSound(player, pos, (open ? FDBackportRegistry.ROPE_FENCE_GATE_OPEN : FDBackportRegistry.ROPE_FENCE_GATE_CLOSE).get(),
				SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(FACING).getClockWise().getAxis() != facing.getAxis()) {
			return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
		}
		boolean walls = isWall(facingState) && isWall(level.getBlockState(currentPos.relative(facing.getOpposite())));
		return state.setValue(IN_WALL, walls);
	}

	private static boolean isWall(BlockState state) {
		return state.is(BlockTags.WALLS);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (state.getValue(OPEN)) return Shapes.empty();
		return state.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE : X_COLLISION_SHAPE;
	}
}
