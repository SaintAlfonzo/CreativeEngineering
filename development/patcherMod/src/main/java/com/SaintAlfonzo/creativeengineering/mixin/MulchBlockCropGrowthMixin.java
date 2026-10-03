package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.crops.CropParity;
import com.ordana.immersive_weathering.blocks.soil.MulchBlock;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fertile Soil (Immersive Weathering's mulch block) in bone-meal growth mode only grows
 * {@code CropBlock}s: soaked, light 9+, each soil random tick calls {@code crop.growCrops}. Plants in
 * {@link CropParity#FARMLAND_CROPS} that aren't CropBlocks get one bone-meal step under the same
 * conditions. In random-tick mode IW already ticks every plant above, so nothing is added there.
 * Injected at the head of randomTick (m_213898_); IW's own code then skips the plant as before.
 */
@Mixin(value = MulchBlock.class, remap = false)
public abstract class MulchBlockCropGrowthMixin {

    @Inject(method = "m_213898_", at = @At("HEAD"), remap = false)
    private void creativeengineering$growFarmlandCrops(BlockState state, ServerLevel level, BlockPos pos,
                                                       RandomSource random, CallbackInfo ci) {
        if (!CommonConfigs.MULCH_GROWS_CROPS.get() || CommonConfigs.MULCH_GROWTH_TYPE_RANDOM_TICK.get()
                || !CommonConfigs.MULCH_GROWTH_TYPE_BONEMEAL.get()) {
            return;
        }
        if (state.getValue(FarmBlock.MOISTURE) != 7) {
            return;
        }
        BlockPos above = pos.above();
        BlockState crop = level.getBlockState(above);
        if (crop.getBlock() instanceof CropBlock || !CropParity.isFarmlandCrop(crop)
                || level.getRawBrightness(above, 0) < 9) {
            return;
        }
        if (crop.getBlock() instanceof BonemealableBlock growable && growable.isValidBonemealTarget(level, above, crop, false)) {
            growable.performBonemeal(level, random, above, crop);
        }
    }
}
