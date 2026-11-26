// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativeoxidation.mixin;

import com.SaintAlfonzo.creativeoxidation.OxidationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Supplementaries soap (R9): wax and every stage off in one use. Hooks the first check tryWash makes
 * (tryWashWithInterface, Supplementaries 1.19.2-2.4.20), so a washed staged block counts as washed there and gets
 * Supplementaries' own bubble particles and soap use. Applied only with Supplementaries (OxidationMixinPlugin).
 */
@Pseudo
@Mixin(targets = "net.mehvahdjukaar.supplementaries.common.misc.SoapWashableHelper", remap = false)
public abstract class SoapWashableHelperMixin {
	@Inject(method = "tryWashWithInterface", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeoxidation$washStaged(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (OxidationManager.wash(level, pos, state)) cir.setReturnValue(true);
	}
}
