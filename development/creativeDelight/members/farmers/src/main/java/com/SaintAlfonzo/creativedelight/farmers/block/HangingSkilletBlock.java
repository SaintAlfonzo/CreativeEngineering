package com.SaintAlfonzo.creativedelight.farmers.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.registry.ModItems;

/**
 * A skillet hung on a wall (TODO cooking/kitchen #39, kitchen.md 2.13, pack addition). Placed by the skillet item
 * itself (mixin SkilletHangMixin): a sneak right-click on the side of a block hangs it when the space above is clear
 * (the handle reaches into it). Same item as the skillet: no item of its own. It keeps the skillet stack (name,
 * enchantments, damage) in Farmer's Delight's skillet block entity (added to that type's valid blocks), which the
 * loot table's {@code copy_skillet} and pick block read back. It does not cook: no ticker, no use with food.
 * Right-click with an empty hand takes it down into the hand. Falls (drops) when the wall behind goes.
 * FACING is the direction it faces out from the wall (the clicked face).
 */
public class HangingSkilletBlock extends BaseEntityBlock {

	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

	// Wall on the north side (facing south): pan 14 x 14, 4 deep; the handle above is not part of the shape.
	private static final VoxelShape NORTH = Block.box(1.0, 1.0, 0.0, 15.0, 15.0, 4.0);
	private static final VoxelShape SOUTH = Block.box(1.0, 1.0, 12.0, 15.0, 15.0, 16.0);
	private static final VoxelShape WEST = Block.box(0.0, 1.0, 1.0, 4.0, 15.0, 15.0);
	private static final VoxelShape EAST = Block.box(12.0, 1.0, 1.0, 16.0, 15.0, 15.0);

	public HangingSkilletBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
	}

	/** Whether a skillet may hang at {@code pos} facing {@code facing}: sturdy wall behind, space above clear. */
	public static boolean canHang(LevelReader level, BlockPos pos, Direction facing) {
		if (!facing.getAxis().isHorizontal()) return false;
		BlockPos wall = pos.relative(facing.getOpposite());
		if (!level.getBlockState(wall).isFaceSturdy(level, wall, facing)) return false;
		BlockState above = level.getBlockState(pos.above());
		return above.isAir() || above.getMaterial().isReplaceable();
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos wall = pos.relative(state.getValue(FACING).getOpposite());
		return level.getBlockState(wall).isFaceSturdy(level, wall, state.getValue(FACING));
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		if (dir == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
		return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case NORTH -> SOUTH; // facing north: the wall is on the south side
			case EAST -> WEST;
			case WEST -> EAST;
			default -> NORTH;
		};
	}

	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
		if (level.isClientSide) return InteractionResult.SUCCESS;
		if (!player.mayBuild() || !level.mayInteract(player, pos)) return InteractionResult.PASS;
		ItemStack skillet = skilletOf(level, pos);
		level.removeBlock(pos, false);
		level.playSound(null, pos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 0.7F, 1.0F);
		level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
		player.setItemInHand(InteractionHand.MAIN_HAND, skillet);
		return InteractionResult.CONSUME;
	}

	/** The hung skillet as an item: the stored stack, or a plain skillet. */
	private static ItemStack skilletOf(BlockGetter level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof SkilletBlockEntity be) {
			CompoundTag nbt = be.writeSkilletItem(new CompoundTag());
			if (nbt.contains("Skillet")) {
				ItemStack stack = ItemStack.of(nbt.getCompound("Skillet"));
				if (!stack.isEmpty()) return stack;
			}
		}
		return new ItemStack(ModItems.SKILLET.get());
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
		return skilletOf(level, pos);
	}

	@Override
	@SuppressWarnings("deprecation")
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	@Nullable
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SkilletBlockEntity(pos, state);
	}
}
