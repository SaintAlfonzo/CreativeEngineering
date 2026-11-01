package com.SaintAlfonzo.creativedelight.treatedstick;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.block.TomatoVineBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.List;

/**
 * A Farmer's Delight tomato vine on a placed treated stick (feature_writeup/treated_sticks.md), the counterpart of
 * Supplementaries' {@code stick_tomatoes}: FD's vine (growth, picking, loot) with the stick kept when the vine is
 * broken or dies. The vertical stick is implied; {@code axis_x}/{@code axis_z} are crossing sticks.
 */
public class TreatedStickTomatoBlock extends TomatoVineBlock {

	public static final ResourceLocation SUPP_STICK = new ResourceLocation("supplementaries", "stick");
	public static final ResourceLocation SUPP_STICK_TOMATOES = new ResourceLocation("supplementaries", "stick_tomatoes");
	public static final ResourceLocation SUPP_ROPE = new ResourceLocation("supplementaries", "rope");
	public static final ResourceLocation SUPP_ROPE_TOMATOES = new ResourceLocation("supplementaries", "rope_tomatoes");

	public TreatedStickTomatoBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(ROPELOGGED, true)
				.setValue(TreatedStickBlock.AXIS_X, false).setValue(TreatedStickBlock.AXIS_Z, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(TreatedStickBlock.AXIS_X, TreatedStickBlock.AXIS_Z);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return TreatedStickBlock.stickShape(state.getValue(TreatedStickBlock.AXIS_X), true,
				state.getValue(TreatedStickBlock.AXIS_Z));
	}

	/** Climbs onto a treated stick, a Supplementaries stick or rope, or any other tagged rope above. */
	@Override
	public void attemptRopeClimb(ServerLevel level, BlockPos pos, RandomSource random) {
		if (TreatedSticks.tryClimb(level, pos, random)) return;
		if (random.nextFloat() >= 0.3F) return;
		BlockPos above = pos.above();
		BlockState stateAbove = level.getBlockState(above);
		boolean canClimb = Configuration.ENABLE_TOMATO_VINE_CLIMBING_TAGGED_ROPES.get()
				? stateAbove.is(ModTags.ROPES) : stateAbove.is(ModBlocks.ROPE.get());
		if (!canClimb || TreatedSticks.vineHeight(level, pos) >= 3) return;
		BlockState toPlace;
		ResourceLocation aboveId = ForgeRegistries.BLOCKS.getKey(stateAbove.getBlock());
		Block suppStickTomatoes = ForgeRegistries.BLOCKS.getValue(SUPP_STICK_TOMATOES);
		Block suppRopeTomatoes = ForgeRegistries.BLOCKS.getValue(SUPP_ROPE_TOMATOES);
		if (SUPP_STICK.equals(aboveId) && suppStickTomatoes != null && suppStickTomatoes != net.minecraft.world.level.block.Blocks.AIR) {
			toPlace = suppStickTomatoes.withPropertiesOf(stateAbove);
		} else if (SUPP_ROPE.equals(aboveId) && suppRopeTomatoes != null && suppRopeTomatoes != net.minecraft.world.level.block.Blocks.AIR) {
			toPlace = Block.updateFromNeighbourShapes(suppRopeTomatoes.withPropertiesOf(stateAbove), level, above);
		} else {
			toPlace = ModBlocks.TOMATO_CROP.get().defaultBlockState().setValue(ROPELOGGED, true);
		}
		level.setBlockAndUpdate(above, toPlace);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockState below = level.getBlockState(pos.below());
		return (below.getBlock() instanceof TomatoVineBlock || super.canSurvive(state.setValue(ROPELOGGED, false), level, pos))
				&& hasGoodCropConditions(level, pos);
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		if (!context.isSecondaryUseActive() && TreatedSticks.isTreatedStick(context.getItemInHand())) {
			return switch (context.getClickedFace().getAxis()) {
				case X -> !state.getValue(TreatedStickBlock.AXIS_X);
				case Z -> !state.getValue(TreatedStickBlock.AXIS_Z);
				default -> false;
			};
		}
		return super.canBeReplaced(state, context);
	}

	/** The stick that stays when the vine goes. */
	private static BlockState stickOf(BlockState state) {
		return TreatedSticks.TREATED_STICK.get().withPropertiesOf(state);
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
			ItemStack stack) {
		// FD would put its rope back for a ropelogged vine; the stick is already back (onDestroyedByPlayer)
		super.playerDestroy(level, player, pos, state.setValue(ROPELOGGED, false), blockEntity, stack);
	}

	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest,
			FluidState fluid) {
		playerWillDestroy(level, pos, state, player);
		return level.setBlock(pos, stickOf(state), level.isClientSide ? Block.UPDATE_ALL_IMMEDIATE : Block.UPDATE_ALL);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.canSurvive(level, pos)) {
			level.levelEvent(2001, pos, Block.getId(state));
			Block.dropResources(state, level, pos, null, null, ItemStack.EMPTY);
			level.setBlockAndUpdate(pos, stickOf(state));
		}
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
			BlockPos pos, BlockPos facingPos) {
		if (!state.canSurvive(level, pos)) level.scheduleTick(pos, this, 1);
		return state;
	}

	/** FD's tomato loot (its vine state, ropelogged). */
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		return ModBlocks.TOMATO_CROP.get().withPropertiesOf(state).getDrops(builder);
	}
}
