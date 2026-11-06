package com.SaintAlfonzo.creativedelight.oceanic.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The wild sea grape of warm oceans (ocean_delight.md 5.6): stands in still water where seagrass could. */
public class WildSeaGrapeBlock extends WaterloggedPlantBlock {
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 16, 14);

    public WildSeaGrapeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return SHAPE.move(offset.x, offset.y, offset.z);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getFluidState(pos).isSource() && inWater(level, pos) && seagrassCouldStand(level, pos);
    }
}
