package com.SaintAlfonzo.creativedelight.oceanic.content;

import com.SaintAlfonzo.creativedelight.oceanic.registry.OceanicBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The ripe sea grapes on top of a plant column (ocean_delight.md 5.6); drops through its loot table. Glows faintly. */
public class SeaGrapeFruitBlock extends WaterloggedPlantBlock {
    private static final VoxelShape SHAPE = box(4.75, 0, 3.5, 12.75, 7, 11.5);

    public SeaGrapeFruitBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return inWater(level, pos) && level.getBlockState(pos.below()).is(OceanicBlocks.SEA_GRAPE_PLANT.get());
    }
}
