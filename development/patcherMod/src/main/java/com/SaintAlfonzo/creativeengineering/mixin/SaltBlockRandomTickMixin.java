package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.block.SaltBlockTicks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BlockBehaviour.randomTick (m_213898_) for Vintage's salt family ({@link SaltBlockTicks}): plain blocks, slabs,
 * walls, the pillar and the lamps keep this method, stairs pass their own state into it from StairBlock.randomTick,
 * and Vintage's SaltBlock (Evaporator) grows its layers in the tick() it calls. Other blocks pay one identity-set
 * lookup per random tick.
 */
@Mixin(value = BlockBehaviour.class, remap = false)
public abstract class SaltBlockRandomTickMixin {

    @Inject(method = "m_213898_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$saltTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
                                              CallbackInfo ci) {
        if (SaltBlockTicks.isSalt(state) && SaltBlockTicks.randomTick(state, level, pos, random)) ci.cancel();
    }
}
