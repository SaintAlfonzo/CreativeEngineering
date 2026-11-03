package com.SaintAlfonzo.creativedelight.mixin.immersive_weathering;

import com.SaintAlfonzo.creativedelight.CDConfig;
import com.SaintAlfonzo.creativedelight.crops.CropParity;
import com.ordana.immersive_weathering.blocks.soil.MulchBlock;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fertile Soil (Immersive Weathering's mulch block) in bone-meal growth mode only grows
 * {@code CropBlock}s: soaked, light 9+, each soil random tick calls {@code crop.growCrops}. Plants in
 * {@link CropParity#FARMLAND_CROPS} that aren't CropBlocks get one bone-meal step under the same
 * conditions. In random-tick mode IW already ticks every plant above, so nothing is added there.
 * Injected at the head of randomTick (m_213898_); IW's own code then skips the plant as before.
 *
 * <p>Stems (farming/crops #21): in bone-meal mode IW still gives a stem (melon, pumpkin, cantaloupe) on soaked
 * Fertile Soil {@code mulch_growth_random_ticks} (10) random ticks per soil tick. With
 * {@code [stem_fruit] fertileSoilBoost = false} those extra ticks are skipped; the soil still counts as moist
 * farmland for the stem's own growth roll. Random-tick mode (every plant) is left alone.
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

    @Redirect(method = "m_213898_", remap = false, require = 1, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;m_222972_(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
    private void creativedelight$noStemBoost(BlockState plant, ServerLevel level, BlockPos pos, RandomSource random) {
        if (plant.getBlock() instanceof StemBlock && !CommonConfigs.MULCH_GROWTH_TYPE_RANDOM_TICK.get()
                && !CDConfig.STEM_FRUIT.fertileSoilBoost.get()) {
            return;
        }
        plant.randomTick(level, pos, random);
    }
}
