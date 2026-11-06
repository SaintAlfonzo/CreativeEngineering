package com.SaintAlfonzo.creativedelight.largemeals.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.FeastBlock;

import java.util.function.Supplier;

/**
 * A four-serving feast on Farmer's Delight's {@link FeastBlock} (large_meals.md 3.2): servings 0 to 4, leftovers on, a
 * bowl handed out per serving. Only the collision shape differs between the three feasts: a plate with a body of the
 * given extent on top, the bare plate at 0 servings.
 */
public class MealFeastBlock extends FeastBlock {
    private static final VoxelShape PLATE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D);

    private final VoxelShape full;

    /** The body is the box (x0, 2, z0) to (x1, top, z1) on the plate. */
    public MealFeastBlock(Properties properties, Supplier<Item> serving, double x0, double z0, double x1, double z1, double top) {
        super(properties, serving, true);
        this.full = Shapes.join(PLATE, Block.box(x0, 2.0D, z0, x1, top, z1), BooleanOp.OR);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(SERVINGS) == 0 ? PLATE : full;
    }
}
