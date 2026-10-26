package com.SaintAlfonzo.creativedelight.rustic.content;

import com.SaintAlfonzo.creativedelight.rustic.registry.RDItems;
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
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.List;
import java.util.function.Supplier;

/**
 * Rice Roll Royale: a platter of eight rolls, served top first: three kelp roll slices, the green, red and yellow
 * pepper rolls, a calamari roll, a cherry blossom roll.
 */
public class RiceRollRoyaleBlock extends FeastBlock {
    public static final int MAX = 8;
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, MAX);

    private static final VoxelShape PLATE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D);
    private static final VoxelShape FULL = Shapes.join(PLATE, Block.box(2.0D, 2.0D, 2.0D, 14.0D, 6.0D, 14.0D), BooleanOp.OR);

    /** Index = servings left - 1. */
    private static final List<Supplier<Item>> ROLLS = List.of(RDItems.CHERRY_BLOSSOM_ROLL, RDItems.CALAMARI_ROLL,
            RDItems.BELL_PEPPER_ROLL_YELLOW, RDItems.BELL_PEPPER_ROLL_RED, RDItems.BELL_PEPPER_ROLL_GREEN,
            ModItems.KELP_ROLL_SLICE, ModItems.KELP_ROLL_SLICE, ModItems.KELP_ROLL_SLICE);

    public RiceRollRoyaleBlock(Properties properties) {
        super(properties, ModItems.KELP_ROLL_SLICE, true);
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
        return new ItemStack(ROLLS.get(Math.max(state.getValue(SERVINGS), 1) - 1).get());
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
