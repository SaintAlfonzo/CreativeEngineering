package com.SaintAlfonzo.creativedelight.mixin.farmersdelight;

import com.SaintAlfonzo.creativedelight.crops.DandelionMaturing;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.RichSoilFarmlandBlock;

/**
 * Rich Soil Farmland's random tick (m_213898_): a Dandelion on top may mature ({@link DandelionMaturing}).
 */
@Mixin(value = RichSoilFarmlandBlock.class, remap = false)
public abstract class DandelionRichFarmlandMixin {

    @Inject(method = "m_213898_", at = @At("HEAD"), remap = false)
    private void creativeengineering$matureDandelion(BlockState state, ServerLevel level, BlockPos pos,
                                                     RandomSource random, CallbackInfo ci) {
        DandelionMaturing.soilTick(level, pos, random, true);
    }
}
