package com.SaintAlfonzo.creativedelight.rustic.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * A plate of pancakes: placed with {@value #PLACED} on it, right-click takes one, sneak-right-click with that pancake
 * puts one back, up to {@value #MAX}. The last one taken removes the plate.
 *
 * <p>The {@code servings} state keeps Rustic Delight's numbering, so plates placed with 1.3.0 load unchanged:
 * 0 to 5 are 6 down to 1 pancakes (1.3.0's bites eaten), 6 to 11 are 7 to 12 pancakes.
 */
public class PancakeStackBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 11);
    public static final int PLACED = 6;
    public static final int MAX = 12;

    private static final VoxelShape PLATE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D);
    private static final VoxelShape[] SHAPES = new VoxelShape[MAX + 1];

    static {
        for (int count = 0; count <= MAX; count++) {
            SHAPES[count] = Shapes.join(PLATE, Block.box(3.0D, 2.0D, 3.0D, 13.0D, 2.0D + Math.max(count, 1), 13.0D), BooleanOp.OR);
        }
    }

    private final Supplier<Item> serving;

    public PancakeStackBlock(Supplier<Item> serving, Properties properties) {
        super(properties);
        this.serving = serving;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SERVINGS, 0));
    }

    public static int pancakes(BlockState state) {
        int servings = state.getValue(SERVINGS);
        return servings < PLACED ? PLACED - servings : servings + 1;
    }

    private static BlockState withPancakes(BlockState state, int count) {
        return state.setValue(SERVINGS, count <= PLACED ? PLACED - count : count - 1);
    }

    public ItemStack getServingItem() {
        return new ItemStack(serving.get());
    }

    public boolean isServing(ItemStack stack) {
        return stack.is(serving.get());
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && isServing(held)) {
            return putBack(level, pos, state, held, player);
        }
        return take(level, pos, state, player);
    }

    private InteractionResult take(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            ItemStack pancake = getServingItem();
            if (!player.getInventory().add(pancake)) {
                player.drop(pancake, false);
            }
            if (level instanceof ServerLevel server) {
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D,
                        3, 0.1D, 0.1D, 0.1D, 0.001D);
            }
            int count = pancakes(state);
            if (count > 1) {
                level.setBlock(pos, withPancakes(state, count - 1), Block.UPDATE_ALL);
            } else {
                level.destroyBlock(pos, false);
            }
        }
        level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private InteractionResult putBack(Level level, BlockPos pos, BlockState state, ItemStack held, Player player) {
        int count = pancakes(state);
        if (count >= MAX) {
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide) {
            level.setBlock(pos, withPancakes(state, count + 1), Block.UPDATE_ALL);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
        }
        level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 0.8F, 0.8F);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[pancakes(state)];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        return facing == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).getMaterial().isSolid();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SERVINGS);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return pancakes(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

}
