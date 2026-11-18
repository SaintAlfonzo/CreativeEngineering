package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.FallingLayers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Snow and salt layers fall (plan_post_scratch_1115.md G7.5, {@link FallingLayers}): HEAD of {@code updateShape}
 * ({@code m_7417_}) keeps a tagged layer over a free block instead of breaking it and queues its fall; HEAD of
 * {@code randomTick} ({@code m_213898_}) lets a floating layer (an old save) fall. Salt layers ({@code vintagedelight:salt})
 * are a {@link SnowLayerBlock} and come through here by tag. Vanilla-class target, {@code remap = false}.
 */
@Mixin(value = SnowLayerBlock.class, remap = false)
public abstract class SnowLayerFallMixin {

	@Inject(method = "m_7417_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$holdToFall(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level,
										   BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
		try {
			if (FallingLayers.holdAndQueue(state, level, pos)) cir.setReturnValue(state);
		} catch (Throwable ignored) {
			// vanilla: an unsupported layer breaks
		}
	}

	@Inject(method = "m_213898_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$fallOnTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
										   CallbackInfo ci) {
		try {
			if (FallingLayers.randomTick(state, level, pos)) ci.cancel();
		} catch (Throwable ignored) {
			// tick as vanilla
		}
	}
}
