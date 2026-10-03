package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.crops.CropParity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FarmBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Dry vanilla farmland reverts to dirt on a random tick unless {@code isUnderCrops} (m_53250_,
 * private static) says a crop sits on it, and Forge's version only counts {@code IPlantable}s. So
 * dry farmland under tobacco turned to dirt and popped the plant, while wheat keeps its farmland.
 * Plants in {@link CropParity#FARMLAND_CROPS} now count as crops. Rich Farmland and Fertile Soil
 * override randomTick and never revert, so this only matters for vanilla farmland.
 */
@Mixin(value = FarmBlock.class, remap = false)
public abstract class FarmBlockUnderCropsMixin {

    @Inject(method = "m_53250_", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativeengineering$farmlandCrops(BlockGetter level, BlockPos pos,
                                                          CallbackInfoReturnable<Boolean> cir) {
        if (CropParity.isFarmlandCrop(level.getBlockState(pos.above()))) {
            cir.setReturnValue(true);
        }
    }
}
