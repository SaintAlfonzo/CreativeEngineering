package com.SaintAlfonzo.creativecommon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.SaintAlfonzo.creativecommon.effect.SteadyRules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BigDripleafBlock;

/** Steady ({@code food-tuning #51}, {@link SteadyRules}): big dripleaf does not tilt under its holder. */
@Mixin(value = BigDripleafBlock.class, remap = false)
public abstract class SteadyDripleafMixin {

	@Inject(method = "m_152301_", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativecommon$steadyNoTilt(BlockPos pos, Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (SteadyRules.isSteady(entity)) cir.setReturnValue(false);
	}
}
