package com.SaintAlfonzo.creativedelight.mixin.minecraft;

import com.SaintAlfonzo.creativedelight.salt.SaltBlockTicks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vintage's Salt Layer is a SnowLayerBlock, whose randomTick (m_213898_) melts it by block light over 11. It now
 * random ticks ({@link SaltBlockTicks}), so the salt tick replaces the snow one. Snow pays one identity-set lookup.
 */
@Mixin(value = SnowLayerBlock.class, remap = false)
public abstract class SaltLayerRandomTickMixin {

    @Inject(method = "m_213898_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$saltLayerTick(BlockState state, ServerLevel level, BlockPos pos,
                                                   RandomSource random, CallbackInfo ci) {
        if (SaltBlockTicks.isSalt(state)) {
            SaltBlockTicks.randomTick(state, level, pos, random);
            ci.cancel();
        }
    }
}
