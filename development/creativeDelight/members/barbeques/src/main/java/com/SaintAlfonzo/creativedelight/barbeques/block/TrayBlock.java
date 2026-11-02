package com.SaintAlfonzo.creativedelight.barbeques.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Tray (1.3.0 TrayBlock): stacks on itself ({@code support=true} is the model with legs, used when resting on a tray),
 * breaks when nothing holds it up. Ported for evaluation (O12, largecontent/mod-additions #38).
 */
public class TrayBlock extends StorageBlock {
    public static final VoxelShape OUTER = Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
    private static final VoxelShape SHAPE = Shapes.join(OUTER, Block.box(1.0, 1.0, 1.0, 15.0, 3.0, 15.0), BooleanOp.ONLY_FIRST);
    public static final BooleanProperty SUPPORT = BooleanProperty.create("support");

    public TrayBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SUPPORT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SUPPORT);
    }

    private static boolean supported(LevelReader level, BlockPos pos) {
        return Block.canSupportRigidBlock(level, pos.below());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx).setValue(SUPPORT, !supported(ctx.getLevel(), ctx.getClickedPos()));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return supported(level, pos) || level.getBlockState(pos.below()).is(this);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction facing, BlockState neighbour, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (facing.getAxis() != Direction.Axis.Y) {
            return state;
        }
        if (supported(level, pos)) {
            return state.setValue(SUPPORT, false);
        }
        return level.getBlockState(pos.below()).is(this) ? state.setValue(SUPPORT, true) : Blocks.AIR.defaultBlockState();
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrayBlockEntity(pos, state);
    }
}
