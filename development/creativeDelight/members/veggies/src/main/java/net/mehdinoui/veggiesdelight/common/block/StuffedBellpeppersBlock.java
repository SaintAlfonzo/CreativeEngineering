package net.mehdinoui.veggiesdelight.common.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.FeastBlock;

// Three servings instead of FD's four. FeastBlock reads servings through getServingsProperty(), so the own property
// replaces FD's in the block state.
public class StuffedBellpeppersBlock extends FeastBlock {
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 3);
    protected static final VoxelShape[] SHAPES = new VoxelShape[]{
            box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0),
            box(1.0, 0.0, 1.0, 15.0, 7.0, 15.0),
            box(1.0, 0.0, 1.0, 15.0, 7.0, 15.0),
            box(1.0, 0.0, 1.0, 15.0, 7.0, 15.0)
    };

    public StuffedBellpeppersBlock(Properties properties, Supplier<Item> servingItem, boolean hasLeftovers) {
        super(properties, servingItem, hasLeftovers);
    }

    @Override
    public IntegerProperty getServingsProperty() {
        return SERVINGS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(this.getServingsProperty())];
    }

    @Override
    public int getMaxServings() {
        return 3;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SERVINGS);
    }

    @Override
    public ItemStack getServingItem(BlockState state) {
        return new ItemStack(this.servingItem.get());
    }
}
