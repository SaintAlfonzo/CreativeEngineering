package com.axperty.storagedelight.block.half;

import com.axperty.storagedelight.block.CabinetCountertopBlock;
import com.axperty.storagedelight.block.CabinetVariantBlock;
import com.axperty.storagedelight.block.GlassCabinetBlock;
import com.axperty.storagedelight.block.entity.CabinetCountertopBlockEntity;
import com.axperty.storagedelight.block.entity.CabinetVariantBlockEntity;
import com.axperty.storagedelight.block.entity.GlassCabinetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Half cabinets (TODO kitchen #41, storage_delight.md section 6): each Storage Delight cabinet kind at half depth, the
 * shape of Hearth and Harvest's half cabinet (the back half of the block space, against the wall it is placed on).
 * Everything else is the full cabinet's: 27 slots, block entity type, placement, open state, contents spilled on break.
 * The block entity is created here because the upstream jar is SRG-named (javac does not see its newBlockEntity).
 */
public final class HalfCabinetBlocks {
    private static final VoxelShape NORTH = Block.box(0, 0, 8, 16, 16, 16);
    private static final VoxelShape EAST = Block.box(0, 0, 0, 8, 16, 16);
    private static final VoxelShape SOUTH = Block.box(0, 0, 0, 16, 16, 8);
    private static final VoxelShape WEST = Block.box(8, 0, 0, 16, 16, 16);

    private HalfCabinetBlocks() {
    }

    static VoxelShape shape(BlockState state) {
        return switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    /** Half of a glass cabinet (glass_W_half_cabinet). */
    public static class Glass extends GlassCabinetBlock {
        public Glass(Properties properties) {
            super(properties);
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return shape(state);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new GlassCabinetBlockEntity(pos, state);
        }
    }

    /** Half of a cabinet with glass doors or a single door cabinet. */
    public static class Variant extends CabinetVariantBlock {
        public Variant(Properties properties) {
            super(properties);
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return shape(state);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new CabinetVariantBlockEntity(pos, state);
        }
    }

    /** Half of a countertop cabinet. */
    public static class Countertop extends CabinetCountertopBlock {
        public Countertop(Properties properties) {
            super(properties);
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return shape(state);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new CabinetCountertopBlockEntity(pos, state);
        }
    }
}
