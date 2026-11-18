package com.SaintAlfonzo.creativecompat.surface;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Full bamboo leaves, {@code creativecompat:bamboo_leaves_full} (plan_post_scratch_1115.md G7.9): Twigs bamboo leaves
 * stacked past their 4 layers. Layers 5 to 8 ({@code layers}); the model is Twigs' 4-layer pile with a second pile of
 * {@code layers - 4} on top (own model JSONs, Twigs' textures referenced; Twigs is All Rights Reserved, so no model
 * or texture file of Twigs is copied). No item: placing Twigs bamboo leaves on a
 * 4-layer pile makes this block ({@code mixin/TwigsBambooLeavesMixin}), more raise it to 8; it drops one Twigs bamboo
 * leaves per layer and picks as them. No collision, walk-through like any leaf layer (tags
 * {@code creativecompat:passthrough_layers}, {@code _open}); falls and stacks ({@link FallingLayers}).
 */
public class FullBambooLeavesBlock extends Block implements SimpleWaterloggedBlock {

	/** Twigs bamboo leaves' most layers. */
	public static final int TWIGS_MAX = 4;
	public static final int MAX = 8;
	public static final IntegerProperty LAYERS = IntegerProperty.create("layers", TWIGS_MAX + 1, MAX);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	/** Outline per layer 5..8 (pixels): the 5 px base plus the top pile of {@code layers - 4}. */
	private static final VoxelShape[] SHAPES = {
			Block.box(0, 0, 0, 16, 6, 16), Block.box(0, 0, 0, 16, 7, 16),
			Block.box(0, 0, 0, 16, 8, 16), Block.box(0, 0, 0, 16, 10, 16)};

	public FullBambooLeavesBlock() {
		super(BlockBehaviour.Properties.of(Material.PLANT).noCollission().noOcclusion().strength(0.1F)
				.sound(SoundType.AZALEA_LEAVES));
		registerDefaultState(stateDefinition.any().setValue(LAYERS, TWIGS_MAX + 1).setValue(WATERLOGGED, false));
	}

	/** The state with {@code n} layers (5 to 8), or null when the block is not registered yet or n is out of range. */
	@Nullable
	public static BlockState state(int n) {
		if (n <= TWIGS_MAX || n > MAX || !FallingLayerEntities.BAMBOO_LEAVES_FULL.isPresent()) return null;
		return FallingLayerEntities.BAMBOO_LEAVES_FULL.get().defaultBlockState().setValue(LAYERS, n);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LAYERS, WATERLOGGED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return SHAPES[state.getValue(LAYERS) - TWIGS_MAX - 1];
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return Shapes.empty();
	}

	/** Like Twigs' pile: on a block whose top has a collision face. */
	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		BlockState under = level.getBlockState(below);
		return under.isFaceSturdy(level, below, Direction.UP)
				|| !under.getCollisionShape(level, below).getFaceShape(Direction.UP).isEmpty();
	}

	@Override
	public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos,
								  BlockPos neighborPos) {
		if (FallingLayers.holdAndQueue(state, level, pos)) return state;
		if (!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
		if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
		super.onPlace(state, level, pos, old, moving);
		if (level instanceof ServerLevel sl && state.is(FallingLayers.TAG) && FallingLayers.enabled()) {
			FallingLayers.queue(sl, pos);
		}
	}

	/** More Twigs bamboo leaves add a layer up to 8 ({@code mixin/TwigsBambooLeavesMixin} makes the state). */
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
		Block twigs = FallingLayers.twigsLeaves();
		return twigs != null && !ctx.isSecondaryUseActive() && ctx.getItemInHand().is(twigs.asItem())
				&& state.getValue(LAYERS) < MAX;
	}

	/**
	 * One Twigs bamboo leaves per layer, in code rather than a loot table so the jar loads without Twigs (nothing then
	 * drops; the block cannot exist without Twigs anyway). Explosions keep vanilla's survival roll.
	 */
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		Block twigs = FallingLayers.twigsLeaves();
		if (twigs == null) return List.of();
		Float radius = builder.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);
		if (radius != null && radius > 0.0F && builder.getLevel().getRandom().nextFloat() > 1.0F / radius) return List.of();
		return List.of(new ItemStack(twigs, state.getValue(LAYERS)));
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos,
									   net.minecraft.world.entity.player.Player player) {
		Block twigs = FallingLayers.twigsLeaves();
		return twigs == null ? ItemStack.EMPTY : new ItemStack(twigs);
	}

	@Override
	public boolean skipRendering(BlockState state, BlockState adjacent, Direction dir) {
		return adjacent.is(this) && adjacent.getValue(LAYERS) >= state.getValue(LAYERS);
	}
}
