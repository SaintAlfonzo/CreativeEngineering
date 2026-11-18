package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.FallingLayers;

import blusunrize.immersiveengineering.common.blocks.wooden.SawdustBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Immersive Engineering sawdust falls (plan_post_scratch_1115.md G7.5, {@link FallingLayers}): HEAD of
 * {@code updateShape} ({@code m_7417_}) keeps a pile over a free block and queues its fall. SRG names,
 * {@code remap = false}; applied only with Immersive Engineering loaded.
 */
@Mixin(value = SawdustBlock.class, remap = false)
public abstract class IeSawdustFallMixin {

	@Inject(method = "m_7417_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$holdToFall(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level,
										   BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
		try {
			if (FallingLayers.holdAndQueue(state, level, pos)) cir.setReturnValue(state);
		} catch (Throwable ignored) {
			// IE: an unsupported pile breaks
		}
	}
}
