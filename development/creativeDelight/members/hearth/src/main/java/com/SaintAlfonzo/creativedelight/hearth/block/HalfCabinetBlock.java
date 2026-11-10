package com.SaintAlfonzo.creativedelight.hearth.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.CabinetBlock;

/** Half-depth Farmer's Delight cabinet (1.4.1 HalfCabinetBlock): 27 slots, the back half of the block space. */
public class HalfCabinetBlock extends CabinetBlock {
    private static final VoxelShape NORTH = Block.box(0, 0, 8, 16, 16, 16);
    private static final VoxelShape EAST = Block.box(0, 0, 0, 8, 16, 16);
    private static final VoxelShape SOUTH = Block.box(0, 0, 0, 16, 16, 8);
    private static final VoxelShape WEST = Block.box(8, 0, 0, 16, 16, 16);

    public HalfCabinetBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        return switch (direction) {
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
