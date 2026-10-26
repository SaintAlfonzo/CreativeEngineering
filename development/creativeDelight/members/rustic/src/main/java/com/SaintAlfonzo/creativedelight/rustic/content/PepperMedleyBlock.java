package com.SaintAlfonzo.creativedelight.rustic.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.FeastBlock;

import java.util.List;
import java.util.function.Supplier;

/** A medley: three stuffed peppers on a bowl, one colour per serving (the last pepper in the list is served first). */
public class PepperMedleyBlock extends FeastBlock {
    public static final int MAX = 3;
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, MAX);

    private static final VoxelShape BOWL = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D);
    private static final VoxelShape FULL = Shapes.join(BOWL, Block.box(3.0D, 2.0D, 3.0D, 13.0D, 9.0D, 13.0D), BooleanOp.OR);

    private final List<Supplier<Item>> peppers;

    public PepperMedleyBlock(Properties properties, List<Supplier<Item>> peppers) {
        super(properties, peppers.get(0), true);
        this.peppers = peppers;
    }

    @Override
    public IntegerProperty getServingsProperty() {
        return SERVINGS;
    }

    @Override
    public int getMaxServings() {
        return MAX;
    }

    @Override
    public ItemStack getServingItem(BlockState state) {
        int servings = state.getValue(SERVINGS);
        return new ItemStack(peppers.get(Math.max(servings, 1) - 1).get());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(SERVINGS) == 0 ? BOWL : FULL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SERVINGS);
    }
}
