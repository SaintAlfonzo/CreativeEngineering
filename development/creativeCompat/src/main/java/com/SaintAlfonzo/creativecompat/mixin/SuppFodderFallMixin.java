package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.FallingLayers;

import net.mehvahdjukaar.supplementaries.common.block.blocks.FodderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Supplementaries fodder falls (plan_post_scratch_1115.md G7.5, {@link FallingLayers}): fodder never breaks when
 * unsupported, so HEAD of {@code updateShape} ({@code m_7417_}) only queues the fall of a pile over a free block and
 * lets the method run. SRG names, {@code remap = false}; applied only with Supplementaries loaded.
 */
@Mixin(value = FodderBlock.class, remap = false)
public abstract class SuppFodderFallMixin {

	@Inject(method = "m_7417_", at = @At("HEAD"))
	private void creativecompat$queueFall(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level,
										  BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
		try {
			FallingLayers.holdAndQueue(state, level, pos);
		} catch (Throwable ignored) {
			// stays floating
		}
	}
}
