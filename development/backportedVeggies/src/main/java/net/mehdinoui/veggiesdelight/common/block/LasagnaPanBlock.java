package net.mehdinoui.veggiesdelight.common.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.FeastBlock;

public class LasagnaPanBlock extends FeastBlock {
    private static final VoxelShape EMPTY_PAN = Shapes.or(
            box(1.0, 0.0, 3.0, 3.0, 5.0, 15.0),
            box(1.0, 0.0, 1.0, 13.0, 5.0, 3.0),
            box(3.0, 0.0, 13.0, 13.0, 5.0, 15.0),
            box(13.0, 0.0, 1.0, 15.0, 5.0, 15.0),
            box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0));
    private static final VoxelShape FULL_PAN = Shapes.or(EMPTY_PAN, box(3.0, 1.0, 3.0, 13.0, 5.0, 13.0));
    protected static final VoxelShape[] SHAPES = new VoxelShape[]{EMPTY_PAN, FULL_PAN, FULL_PAN, FULL_PAN, FULL_PAN};

    public LasagnaPanBlock(Properties properties, Supplier<Item> servingItem, boolean hasLeftovers) {
        super(properties, servingItem, hasLeftovers);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int servings = state.getValue(this.getServingsProperty());
        return servings >= 0 && servings < SHAPES.length ? SHAPES[servings] : FULL_PAN;
    }
}
