// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.SaintAlfonzo.creativecompat.melting.Melting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;

/**
 * Magma keeps snow and ice away ({@link Melting#magmaBlocksFreezing}, plan_post_scratch_1115 G7.13, G6 A): Biome.shouldSnow
 * ({@code m_47519_}) and Biome.shouldFreeze(LevelReader, BlockPos, boolean) ({@code m_47480_}, which the two-argument
 * form calls) answer false on the server when a magma block is within [melting] fireRange. ServerLevel.tickChunk's
 * snowfall and ice placement go through them; world generation (a WorldGenRegion) is not checked.
 */
@Mixin(value = Biome.class, remap = false)
public abstract class BiomeMeltMixin {

	@Inject(method = "m_47519_", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void creativecompat$noSnowNearMagma(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (Melting.magmaBlocksFreezing(level, pos)) cir.setReturnValue(false);
	}

	@Inject(method = "m_47480_", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void creativecompat$noIceNearMagma(LevelReader level, BlockPos pos, boolean mustBeAtEdge,
			CallbackInfoReturnable<Boolean> cir) {
		if (Melting.magmaBlocksFreezing(level, pos)) cir.setReturnValue(false);
	}
}
