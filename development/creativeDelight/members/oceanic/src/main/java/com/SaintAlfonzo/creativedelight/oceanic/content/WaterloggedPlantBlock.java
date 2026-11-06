package com.SaintAlfonzo.creativedelight.oceanic.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The sea grape blocks (and the two plates, through {@link ServingPlateBlock}): waterloggable, no visual shape, light
 * passes, and gone the moment they cannot stay.
 */
public abstract class WaterloggedPlantBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected WaterloggedPlantBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return state.canSurvive(level, pos) ? super.updateShape(state, facing, facingState, level, pos, facingPos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    /** Water (source or flowing, or a waterlogged block) at the position. */
    protected static boolean inWater(LevelReader level, BlockPos pos) {
        return level.getFluidState(pos).getType().isSame(Fluids.WATER);
    }

    /** Water with nothing in it: a water block, not a waterlogged block. */
    protected static boolean openWater(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return inWater(level, pos) && !(state.hasProperty(WATERLOGGED) && state.getValue(WATERLOGGED));
    }

    /** Where vanilla seagrass could stand (a sturdy top below, not magma). */
    protected static boolean seagrassCouldStand(LevelReader level, BlockPos pos) {
        return Blocks.SEAGRASS.defaultBlockState().canSurvive(level, pos);
    }
}
