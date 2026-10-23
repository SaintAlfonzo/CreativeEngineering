package com.SaintAlfonzo.creativeengineering.compat;

import com.SaintAlfonzo.creativeengineering.CEConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Farmer's Delight cutting board on a Cooking for Blockheads counter (TODO cooking/kitchen #8). The counter and corner are
 * 15 px tall, so a board on top would float 1 px above the worktop: its model ({@code BlockStateCuttingBoardOffsetMixin}
 * on {@code BlockState#getOffset}) and its item renderer ({@code CuttingBoardRendererOffsetMixin}) are shifted by
 * {@code [kitchen] cuttingBoardOffset*}. Support itself is {@code CuttingBoardSupportMixin}. The hitbox is not moved.
 */
public final class CuttingBoardOnCounter {

    public static final TagKey<Block> SUPPORTS =
            BlockTags.create(new ResourceLocation("creativeengineering", "cutting_board_supports"));
    private static final ResourceLocation BOARD_ID = new ResourceLocation("farmersdelight", "cutting_board");

    private static final Object NO_BOARD = new Object();

    private static volatile Object board;

    private CuttingBoardOnCounter() {}

    /**
     * FD's cutting board block, compared by reference (looked up once; no FD class is referenced). Without FD it is a
     * sentinel no block equals: the registry's getValue would answer air for a missing id.
     */
    public static Object board() {
        Object b = board;
        if (b == null) {
            b = ForgeRegistries.BLOCKS.containsKey(BOARD_ID) ? ForgeRegistries.BLOCKS.getValue(BOARD_ID) : NO_BOARD;
            board = b;
        }
        return b;
    }

    /** Offset in blocks for a cutting board at {@code pos}: the configured shift when it sits on a support, else zero. */
    public static Vec3 offset(BlockGetter level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (!below.is(SUPPORTS)) {
            return Vec3.ZERO;
        }
        double x = CEConfig.KITCHEN.cuttingBoardOffsetX.get() / 16.0;
        double y = CEConfig.KITCHEN.cuttingBoardOffsetY.get() / 16.0;
        double z = CEConfig.KITCHEN.cuttingBoardOffsetZ.get() / 16.0;
        // Config frame = a counter facing north (+z toward its back); turn it clockwise with the counter.
        return switch (facing(below)) {
            case SOUTH -> new Vec3(-x, y, -z);
            case EAST -> new Vec3(-z, y, x);
            case WEST -> new Vec3(z, y, -x);
            default -> new Vec3(x, y, z);
        };
    }

    private static Direction facing(BlockState state) {
        Property<?> p = state.getBlock().getStateDefinition().getProperty("facing");
        return p instanceof DirectionProperty d ? state.getValue(d) : Direction.NORTH;
    }
}
