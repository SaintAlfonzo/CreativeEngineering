package net.satisfy.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Block that connects to same blocks left and right (relative to its facing),
 * tracked in the {@link #TYPE} property. Used for benches, counters and so on.
 */
public class LineConnectingBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<LineConnectingType> TYPE = EnumProperty.create("type", LineConnectingType.class);

    public LineConnectingBlock(BlockBehaviour.Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, LineConnectingType.NONE));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState blockState = this.defaultBlockState().setValue(FACING, facing);
        return blockState.setValue(TYPE, typeAt(blockState, context.getLevel(), context.getClickedPos()));
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Block sourceBlock, @NotNull BlockPos sourcePos, boolean notify) {
        if (!world.isClientSide) {
            LineConnectingType type = typeAt(state, world, pos);
            if (state.getValue(TYPE) != type) {
                world.setBlock(pos, state.setValue(TYPE, type), 3);
            }
        }
    }

    private LineConnectingType typeAt(BlockState state, Level world, BlockPos pos) {
        return switch (state.getValue(FACING)) {
            case EAST -> this.getType(state, world.getBlockState(pos.south()), world.getBlockState(pos.north()));
            case SOUTH -> this.getType(state, world.getBlockState(pos.west()), world.getBlockState(pos.east()));
            case WEST -> this.getType(state, world.getBlockState(pos.north()), world.getBlockState(pos.south()));
            default -> this.getType(state, world.getBlockState(pos.east()), world.getBlockState(pos.west()));
        };
    }

    /**
     * Figures out the connection type from the two side neighbours.
     * Only counts as connected if same block AND same facing.
     */
    public LineConnectingType getType(BlockState state, BlockState left, BlockState right) {
        boolean shapeLeftSame = left.getBlock() == state.getBlock() && left.getValue(FACING) == state.getValue(FACING);
        boolean shapeRightSame = right.getBlock() == state.getBlock() && right.getValue(FACING) == state.getValue(FACING);
        if (shapeLeftSame && shapeRightSame) {
            return LineConnectingType.MIDDLE;
        } else if (shapeLeftSame) {
            return LineConnectingType.LEFT;
        }
        return shapeRightSame ? LineConnectingType.RIGHT : LineConnectingType.NONE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE);
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
