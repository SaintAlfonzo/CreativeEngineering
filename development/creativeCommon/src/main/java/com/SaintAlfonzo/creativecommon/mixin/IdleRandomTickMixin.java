// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.SaintAlfonzo.creativecommon.perf.IdleRandomTicks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Idle random ticks ({@link IdleRandomTicks}, feature_writeup/random_tick_audit.md): BlockStateBase.isRandomlyTicking
 * ({@code m_60823_}, what ServerLevel.tickChunk and LevelChunkSection's ticking count ask) answers false for states a
 * {@code [perf] idleRandomTickRules} line stops. The answer is cached per state in one byte, so other states pay one
 * field read. updateShape ({@code m_60728_}) and tick ({@code m_222963_}) carry the {@code +wake_above} retries.
 */
@Mixin(value = BlockBehaviour.BlockStateBase.class, remap = false)
public abstract class IdleRandomTickMixin {

	@Unique
	private byte creativecommon$idleTick;

	@Inject(method = "m_60823_", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void creativecommon$idleRandomTick(CallbackInfoReturnable<Boolean> cir) {
		byte s = creativecommon$idleTick;
		if (s == IdleRandomTicks.UNKNOWN) {
			s = IdleRandomTicks.classify((BlockState) (Object) this);
			creativecommon$idleTick = s;
		}
		if (s == IdleRandomTicks.STOP) cir.setReturnValue(false);
	}

	@Inject(method = "m_60728_", at = @At("HEAD"), remap = false, require = 1)
	private void creativecommon$idleWakeAbove(Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos,
			BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
		if (creativecommon$idleTick == IdleRandomTicks.STOP && dir == Direction.UP && level instanceof ServerLevel sl) {
			IdleRandomTicks.onAboveChanged((BlockState) (Object) this, sl, pos, neighbor);
		}
	}

	@Inject(method = "m_222963_", at = @At("HEAD"), remap = false, require = 1)
	private void creativecommon$idleWakeTick(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		if (creativecommon$idleTick == IdleRandomTicks.STOP) {
			IdleRandomTicks.onScheduledTick((BlockState) (Object) this, level, pos, random);
		}
	}
}
