package net.satisfy.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

public class WindowBlock extends IronBarsBlock {
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 3);

    public WindowBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false).setValue(WATERLOGGED, false).setValue(PART, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state.setValue(PART, part(state, context.getLevel(), context.getClickedPos()));
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state, Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        return direction.getAxis().isVertical() ? updated.setValue(PART, part(updated, level, pos)) : updated;
    }

    private int part(BlockState state, BlockGetter level, BlockPos pos) {
        boolean below = level.getBlockState(pos.below()).is(state.getBlock());
        boolean above = level.getBlockState(pos.above()).is(state.getBlock());
        if (below && above) {
            return 2;
        }
        if (above) {
            return 1;
        }
        return below ? 3 : 0;
    }
}
