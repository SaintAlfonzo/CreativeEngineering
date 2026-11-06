package com.SaintAlfonzo.creativedelight.enders.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An End bush (Ender's Delight 1.1.4 CustomBushBlock): survives only on End plant soil ({@link EndSoil}) below it, breaks
 * otherwise (BushBlock's updateShape). Amberveils and Ethereal Saffron use it as is: no growth, so no random ticks.
 */
public class EndPlantBlock extends BushBlock {
    private final VoxelShape shape;

    public EndPlantBlock(Properties properties, VoxelShape shape) {
        super(properties);
        this.shape = shape;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return EndSoil.isSoil(state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return EndSoil.isSoil(level.getBlockState(pos.below()));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }
}
