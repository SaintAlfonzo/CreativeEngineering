package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.FirstTickWater;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Settling fluid from a freshly ticking chunk ({@link FirstTickWater}): {@code tick} (m_6292_) flags the tick,
 * {@code spreadTo} (m_6364_) carries the flag to the target. The drops are skipped in {@code BlockPopResourceSettleMixin}.
 * Lava's {@code spreadTo} override calls super, so it is covered.
 */
@Mixin(value = FlowingFluid.class, remap = false)
public abstract class FlowingFluidSettleMixin {

    @Inject(method = "m_6292_", at = @At("HEAD"), remap = false)
    private void creativeengineering$beginSettle(Level level, BlockPos pos, FluidState state, CallbackInfo ci) {
        FirstTickWater.beginTick(level, pos);
    }

    @Inject(method = "m_6292_", at = @At("RETURN"), remap = false)
    private void creativeengineering$endSettle(Level level, BlockPos pos, FluidState state, CallbackInfo ci) {
        FirstTickWater.endTick();
    }

    @Inject(method = "m_6364_", at = @At("HEAD"), remap = false)
    private void creativeengineering$carrySettle(LevelAccessor level, BlockPos pos, BlockState state, Direction dir,
                                                 FluidState fluid, CallbackInfo ci) {
        FirstTickWater.onSpread(level, pos);
    }
}
