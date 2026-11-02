package com.SaintAlfonzo.creativedelight.barbeques.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Ingredients Basin (1.3.0 BasinBlock): two wells, shape by facing axis. */
public class BasinBlock extends StorageBlock {
    public static final VoxelShape OUTER = Block.box(1.0, 0.0, 1.0, 15.0, 4.0, 15.0);
    public static final VoxelShape SHAPE_X = Shapes.join(OUTER,
            Shapes.or(Block.box(2.0, 1.0, 2.0, 14.0, 4.0, 7.5), Block.box(2.0, 1.0, 8.5, 14.0, 4.0, 14.0)), BooleanOp.ONLY_FIRST);
    public static final VoxelShape SHAPE_Z = Shapes.join(OUTER,
            Shapes.or(Block.box(2.0, 1.0, 2.0, 7.5, 4.0, 14.0), Block.box(8.5, 1.0, 2.0, 14.0, 4.0, 14.0)), BooleanOp.ONLY_FIRST);

    public BasinBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BasinBlockEntity(pos, state);
    }
}
