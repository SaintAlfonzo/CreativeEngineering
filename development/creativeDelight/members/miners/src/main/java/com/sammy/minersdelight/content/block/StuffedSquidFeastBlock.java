package com.sammy.minersdelight.content.block;

import com.sammy.minersdelight.registry.MDItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.FeastBlock;

/** Stuffed Squid: a five-serving feast on a plate, served into bowls. */
public class StuffedSquidFeastBlock extends FeastBlock {
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 5);
    private static final VoxelShape PLATE = Block.box(1, 0, 1, 15, 2, 15);
    private static final VoxelShape FULL = Shapes.or(PLATE, Block.box(2, 2, 2, 14, 8, 14));

    public StuffedSquidFeastBlock(Properties properties) {
        super(properties, MDItems.BOWL_OF_STUFFED_SQUID::get, true);
    }

    @Override
    public IntegerProperty getServingsProperty() {
        return SERVINGS;
    }

    @Override
    public int getMaxServings() {
        return 5;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(SERVINGS) == 0 ? PLATE : FULL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SERVINGS);
    }
}
