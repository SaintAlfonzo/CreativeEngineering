package com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.kitchen;

import javax.annotation.Nullable;

import net.blay09.mods.cookingforblockheads.block.BlockKitchen;
import net.blay09.mods.cookingforblockheads.block.FridgeBlock;
import net.blay09.mods.cookingforblockheads.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Double fridges (TODO cooking/kitchen #36, owner Q27): two Cooking for Blockheads fridges side by side join like a
 * double door. Visual only: each keeps its own inventory and its own door.
 * <p>
 * CfB's {@code flipped} state picks the door side. In CfB's model frame (front at north, model +x is
 * {@code facing.getClockWise()} in the world) an unflipped door has its handle at -x and its hinge at +x; a flipped door
 * the other way round. Two fridges are a pair when they face the same way, have the same height (1-tall, or both 2-tall)
 * and each one's handle side points at the other, so the hinges sit on the outer edges. A fridge's handle side holds one
 * neighbour only, so the pairing needs no scan along a row. A 2-tall fridge's door side comes from its lower block, as
 * CfB's renderer draws both door halves from it.
 * <p>
 * Placement: a fridge placed next to an unpaired fridge whose handle points at it takes the mirrored door side, as
 * vanilla doors pick their hinge ({@code mixin/cookingforblockheads/CfbFridgePlaceMixin}). Drawing:
 * {@code client/FridgeDouble}.
 */
public final class FridgePairs {
    private FridgePairs() {
    }

    /** The world side of this fridge's door handle. */
    public static Direction handleSide(Direction facing, boolean flipped) {
        return flipped ? facing.getClockWise() : facing.getCounterClockWise();
    }

    /** The door side of a fridge block: the lower block's for the upper half of a 2-tall fridge. */
    public static boolean flipped(BlockGetter level, BlockPos pos, BlockState state) {
        if (state.getValue(FridgeBlock.MODEL_TYPE) == FridgeBlock.FridgeModelType.LARGE_UPPER) {
            BlockState below = level.getBlockState(pos.below());
            if (below.is(ModBlocks.fridge)
                    && below.getValue(FridgeBlock.MODEL_TYPE) == FridgeBlock.FridgeModelType.LARGE_LOWER) {
                return below.getValue(BlockKitchen.FLIPPED);
            }
        }
        return state.getValue(BlockKitchen.FLIPPED);
    }

    /** The world side this fridge block is joined on, or null when it has no partner. */
    @Nullable
    public static Direction joinSide(BlockGetter level, BlockPos pos, BlockState state) {
        if (!state.is(ModBlocks.fridge)) return null;
        Direction facing = state.getValue(BlockKitchen.FACING);
        boolean flipped = flipped(level, pos, state);
        Direction side = handleSide(facing, flipped);
        BlockPos other = pos.relative(side);
        BlockState n = level.getBlockState(other);
        if (!n.is(ModBlocks.fridge)
                || n.getValue(BlockKitchen.FACING) != facing
                || n.getValue(FridgeBlock.MODEL_TYPE) != state.getValue(FridgeBlock.MODEL_TYPE)
                || flipped(level, other, n) == flipped) {
            return null;
        }
        return side;
    }

    /**
     * The placement state with its door side mirrored to a neighbouring fridge whose handle points at {@code pos}; CfB's
     * own choice (from where the player stands) wins when both neighbours qualify or neither does.
     */
    public static BlockState mirrorOnPlace(BlockState placed, BlockGetter level, BlockPos pos) {
        if (placed == null || !placed.is(ModBlocks.fridge)) return placed;
        Direction facing = placed.getValue(BlockKitchen.FACING);
        boolean own = placed.getValue(BlockKitchen.FLIPPED);
        for (boolean flipped : new boolean[]{own, !own}) {
            BlockPos other = pos.relative(handleSide(facing, flipped));
            BlockState n = level.getBlockState(other);
            if (n.is(ModBlocks.fridge) && n.getValue(BlockKitchen.FACING) == facing
                    && flipped(level, other, n) != flipped) {
                return flipped == own ? placed : placed.setValue(BlockKitchen.FLIPPED, flipped);
            }
        }
        return placed;
    }
}
