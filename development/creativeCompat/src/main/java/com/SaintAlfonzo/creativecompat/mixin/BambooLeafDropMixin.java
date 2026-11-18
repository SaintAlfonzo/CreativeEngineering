package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.BambooLeafDrops;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Grown bamboo drops Twigs bamboo leaves (plan_post_scratch_1115.md G7.9, {@link BambooLeafDrops}): RETURN of
 * {@code isRandomlyTicking} ({@code m_6724_}) adds the leafy states (a grown stalk never ticks in vanilla); HEAD of
 * {@code randomTick} ({@code m_213898_}) rolls the drops, then vanilla's growth runs as before. Vanilla-class target,
 * {@code remap = false}; applied only with Twigs loaded.
 */
@Mixin(value = BambooBlock.class, remap = false)
public abstract class BambooLeafDropMixin {

	@Inject(method = "m_6724_", at = @At("RETURN"), cancellable = true)
	private void creativecompat$leafyTicks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() && BambooLeafDrops.ticks(state)) cir.setReturnValue(true);
	}

	@Inject(method = "m_213898_", at = @At("HEAD"))
	private void creativecompat$dropLeaves(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
										   CallbackInfo ci) {
		try {
			BambooLeafDrops.randomTick(state, level, pos, random);
		} catch (Throwable ignored) {
			// no drop this tick
		}
	}
}
