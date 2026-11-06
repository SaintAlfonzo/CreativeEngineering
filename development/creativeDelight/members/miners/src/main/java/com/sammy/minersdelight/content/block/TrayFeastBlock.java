package com.sammy.minersdelight.content.block;

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
 * A four-serving feast on a tray (Fake Meatloaf, Glazed Arachnid Limbs). FD 1.19.2 has no tray shape, so it is defined
 * here: a 14x14 rim one pixel deep around a sunken middle.
 */
public class TrayFeastBlock extends FeastBlock {
    public static final VoxelShape TRAY = Shapes.join(Block.box(1, 0, 1, 15, 2, 15), Block.box(2, 1, 2, 14, 2, 14), BooleanOp.ONLY_FIRST);
    public static final VoxelShape[] FAKE_MEATLOAF = byServings(Block.box(5, 2, 5, 11, 5, 11));
    public static final VoxelShape[] GLAZED_ARACHNID_LIMBS = {
            TRAY,
            Shapes.or(TRAY, Block.box(4, 2, 4, 12, 4, 12)),
            Shapes.or(TRAY, Block.box(2, 2, 2, 14, 4, 14)),
            Shapes.or(TRAY, Block.box(2, 2, 2, 14, 6, 14), Block.box(6, 6, 6, 10, 11, 10)),
            Shapes.or(TRAY, Block.box(2, 2, 2, 14, 7, 14), Block.box(6, 7, 6, 10, 13, 10))
    };

    private final VoxelShape[] shapes;

    public TrayFeastBlock(Properties properties, Supplier<Item> serving, VoxelShape[] shapes) {
        super(properties, serving, true);
        this.shapes = shapes;
    }

    /** The bare tray when eaten up, tray plus {@code food} otherwise. */
    private static VoxelShape[] byServings(VoxelShape food) {
        VoxelShape full = Shapes.or(TRAY, food);
        return new VoxelShape[]{TRAY, full, full, full, full};
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes[state.getValue(SERVINGS)];
    }
}
