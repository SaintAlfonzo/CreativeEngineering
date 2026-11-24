// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Alex's Mobs' giant squid no longer flees cachalot whales (wave 1 ruling W17, creative-compat-publish #9): its
 * AIAvoidWhales goal never starts ({@code canUse}, m_8036_). It fights whales instead ({@code mobs/SquidsAndWhales},
 * feature_writeup/mob_block_tweaks.md section 2.10). The goal class is package-private, so it is named by string. SRG
 * names, remap = false.
 */
@Mixin(targets = "com.github.alexthe666.alexsmobs.entity.EntityGiantSquid$AIAvoidWhales", remap = false)
public abstract class AmGiantSquidAvoidWhalesMixin {

	@Inject(method = "m_8036_", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private void creativecompat$neverFlee(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}
}
