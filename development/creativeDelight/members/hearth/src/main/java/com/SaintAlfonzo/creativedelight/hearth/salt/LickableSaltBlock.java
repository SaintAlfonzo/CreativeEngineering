package com.SaintAlfonzo.creativedelight.hearth.salt;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A stage of a salt lick: the placed Rock Salt Block ({@link PlacedRockSaltBlock}, stage 0) or a worn or waxed stage.
 * Every interaction lives in {@link SaltEvents}, written when stage 0 was The Salt's {@code salt:raw_rock_salt_block}
 * itself (plan_post_scratch_1115 G2b). Waxed stages are separate blocks for the same reason: the foreign
 * block cannot take a {@code waxed} property, and a waxed block must stay out of the POI type.
 * <p>
 * Each stage is a layer like snow (plan_bugfix_1114 3.7): 16 / 12 / 8 / 4 px for full / lightly / well / heavily licked;
 * a waxed stage is as high as its unwaxed twin. Salt layers on top no longer refill a stage (G6-10): only Rock
 * Salt does, by hand or dispenser.
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

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return height < 16;
    }
}
