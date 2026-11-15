package com.SaintAlfonzo.creativedelight.hearth.salt;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * A worn or waxed stage of a salt lick. Every interaction lives in {@link SaltEvents}, because stage 0 is The Salt's
 * {@code salt:salt_block} and cannot carry code of ours. Waxed stages are separate blocks for the same reason: the foreign
 * block cannot take a {@code waxed} property, and a waxed block must stay out of the POI type.
 * <p>
 * Each stage is a layer like snow (plan_bugfix_1114 3.7): 16 / 12 / 8 / 4 px for full / lightly / well / heavily licked;
 * a waxed stage is as high as its unwaxed twin.
 */
public class LickableSaltBlock extends Block {
    private final int height;
    private final VoxelShape shape;

    public LickableSaltBlock(Properties properties, int height) {
        super(properties);
        this.height = height;
        this.shape = height >= 16 ? Shapes.block() : Block.box(0.0D, 0.0D, 0.0D, 16.0D, height, 16.0D);
    }

    /** Height in pixels, 4 to 16. */
    public int height() {
        return height;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    /**
     * A Vintage Delight salt layer placed on a worn stage drops into it: one layer restores one stage (heavily, well,
     * lightly, full), a waxed stage stays waxed, and layers left over stay on the full block (plan_bugfix_1114 3.7). The
     * layer's own onPlace sends this update even for placements that send none.
     */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide || !fromPos.equals(pos.above())) {
            return;
        }
        BlockState above = level.getBlockState(fromPos);
        if (!(above.getBlock() instanceof SnowLayerBlock) || !SaltLick.SALT_LAYER_ID.equals(ForgeRegistries.BLOCKS.getKey(above.getBlock()))) {
            return;
        }
        int layers = above.getValue(SnowLayerBlock.LAYERS);
        Block stage = state.getBlock();
        while (layers > 0 && SaltLick.restored(stage) != null) {
            stage = SaltLick.restored(stage);
            layers--;
        }
        if (stage == state.getBlock()) {
            return;
        }
        // Order matters: an empty layer goes first so nothing pops off a partial stage; leftover layers are set after
        // the block is full, so they survive and this does not run twice.
        if (layers == 0) {
            level.setBlockAndUpdate(fromPos, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos, stage.defaultBlockState());
        } else {
            level.setBlockAndUpdate(pos, stage.defaultBlockState());
            level.setBlockAndUpdate(fromPos, above.setValue(SnowLayerBlock.LAYERS, layers));
        }
        level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 1.0F, 1.4F);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return height < 16;
    }
}
