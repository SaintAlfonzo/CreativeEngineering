package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.crops.DandelionMaturing;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ordana.immersive_weathering.blocks.soil.MulchBlock;

/**
 * Fertile Soil's (mulch) random tick (m_213898_): a Dandelion on top may mature ({@link DandelionMaturing}).
 */
@Mixin(value = MulchBlock.class, remap = false)
public abstract class DandelionFertileSoilMixin {

    @Inject(method = "m_213898_", at = @At("HEAD"), remap = false)
    private void creativeengineering$matureDandelion(BlockState state, ServerLevel level, BlockPos pos,
                                                     RandomSource random, CallbackInfo ci) {
        DandelionMaturing.soilTick(level, pos, random, false);
    }
}
