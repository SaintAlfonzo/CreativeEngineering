package com.SaintAlfonzo.creativeengineering.food;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Powdered sugar block: powder snow's sinking, leather-boots walking, no fall damage and bucket pickup, without the
 * freezing, fire melting or snow particles. Bucket pickup gives the powdered sugar bucket. TODO dishes/ingredients #16.
 */
public class PowderedSugarBlock extends PowderSnowBlock {

    public PowderedSugarBlock(Properties properties) {
        super(properties);
    }

    // PowderSnowBlock.entityInside minus setIsInPowderSnow (freezing), the burning-entity melt and the particles.
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity) || entity.getFeetBlockState().is(this)) {
            entity.makeStuckInBlock(state, new Vec3(0.9F, 1.5D, 0.9F));
        }
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        if (!level.isClientSide()) level.levelEvent(2001, pos, Block.getId(state));
        return new ItemStack(PowderedSugarRegistry.POWDERED_SUGAR_BUCKET.get());
    }
}
