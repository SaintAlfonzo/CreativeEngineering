package com.SaintAlfonzo.creativedelight.hearth.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** An unripe wheel (1.4.1 UnripeCheeseWheelBlock): two random-tick stages at 10 % each, then the ripe wheel. */
public class UnripeCheeseWheelBlock extends Block {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 1);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 6, 14);
    private final Supplier<Block> ripe;

    public UnripeCheeseWheelBlock(Supplier<Block> ripe, Properties properties) {
        super(properties);
        this.ripe = ripe;
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        return facing == Direction.DOWN && !canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() > 0.1F) return;
        if (state.getValue(AGE) == 0) level.setBlock(pos, state.setValue(AGE, 1), Block.UPDATE_ALL);
        else level.setBlock(pos, ripe.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
