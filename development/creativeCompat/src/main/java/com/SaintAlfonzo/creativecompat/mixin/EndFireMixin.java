// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.SaintAlfonzo.creativecommon.melting.Melting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * No ordinary fire in the End (plan_post_scratch_1115 G11, [melting] endNoFire): Level.setBlock(BlockPos, BlockState,
 * int, int) ({@code m_6933_}, which the three-argument form and setBlockAndUpdate call) refuses {@code minecraft:fire}
 * there, whoever places it: flint and steel, fire charges, lava spread, End crystals, commands. Refused at the source,
 * not removed after, so an End crystal's every-tick fire costs one check and makes no sound. Fire already in the End
 * goes out on its next scheduled tick ({@link Melting#onScheduledTick}). Soul fire is another block and stays.
 */
@Mixin(value = Level.class, remap = false)
public abstract class EndFireMixin {

	@Inject(method = "m_6933_", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void creativecommon$noFireInEnd(BlockPos pos, BlockState state, int flags, int recursion,
			CallbackInfoReturnable<Boolean> cir) {
		if (Melting.refusesFire((Level) (Object) this, state)) cir.setReturnValue(false);
	}
}
