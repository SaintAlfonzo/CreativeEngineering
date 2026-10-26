package com.SaintAlfonzo.creativedelight.delightful.block;

import com.SaintAlfonzo.creativedelight.delightful.DelightfulBackportRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Cantaloupe plant (3.8.1 CantaloupePlantBlock): planted on sand next to water, four ages, light 8 or more to grow.
 * When grown, right-click picks a Cantaloupe and the plant goes back to age 0 (like a berry bush); breaking it
 * gives the seeds, and a Cantaloupe when grown.
 */
public class CantaloupePlantBlock extends BushBlock implements BonemealableBlock {
	public static final int MAX_AGE = 3;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
	private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[] {
			Block.box(3.0, 0.0, 3.0, 13.0, 2.0, 13.0),
			Block.box(2.0, 0.0, 2.0, 14.0, 5.0, 14.0),
			Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
			Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
	};

	public CantaloupePlantBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(AGE, 0));
	}

	/** delightful:cantaloupe is 3.4.1's block item. */
	private static ItemStack cantaloupe() {
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("delightful", "cantaloupe"));
		return new ItemStack(item != null ? item : Items.AIR);
	}

	public static boolean isMaxAge(BlockState state) {
		return state.getValue(AGE) >= MAX_AGE;
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		return isMaxAge(state) ? cantaloupe() : new ItemStack(DelightfulBackportRegistry.CANTALOUPE_SEEDS.get());
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(Tags.Blocks.SAND);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE_BY_AGE[state.getValue(AGE)];
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.canSurvive(level, pos)) level.destroyBlock(pos, true);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return !isMaxAge(state);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int age = state.getValue(AGE);
		if (age < MAX_AGE && level.getRawBrightness(pos.above(), 0) >= 8
				&& ForgeHooks.onCropsGrowPre(level, pos, state, random.nextInt(16) == 0)) {
			BlockState grown = state.setValue(AGE, age + 1);
			level.setBlock(pos, grown, 2);
			level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(grown));
			ForgeHooks.onCropsGrowPost(level, pos, state);
		}
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		if (level.getBlockState(below).canSustainPlant(level, below, Direction.UP, this)) return true;
		if (!level.getBlockState(below).is(Tags.Blocks.SAND)) return false;
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos side = below.relative(direction);
			if (level.getFluidState(side).is(FluidTags.WATER) || level.getBlockState(side).is(Blocks.FROSTED_ICE)) return true;
		}
		return false;
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		boolean grown = isMaxAge(state);
		if (!grown && player.getItemInHand(hand).is(Items.BONE_MEAL)) return InteractionResult.PASS;
		if (grown) {
			popResource(level, pos, cantaloupe());
			level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
			BlockState reset = state.setValue(AGE, 0);
			level.setBlock(pos, reset, 2);
			level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, reset));
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return super.use(state, level, pos, player, hand, hit);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean isClient) {
		return !isMaxAge(state);
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
		level.setBlock(pos, state.setValue(AGE, Math.min(MAX_AGE, state.getValue(AGE) + 1)), 2);
	}

	@Override
	public PlantType getPlantType(BlockGetter level, BlockPos pos) {
		return PlantType.BEACH;
	}
}
