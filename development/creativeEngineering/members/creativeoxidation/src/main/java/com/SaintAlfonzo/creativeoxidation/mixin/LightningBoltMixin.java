// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation.mixin;

import com.SaintAlfonzo.creativeoxidation.OxidationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Lightning cleans staged blocks as it cleans vanilla copper (P4), after vanilla's own pass. */
@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin {
	@Inject(method = "clearCopperOnLightningStrike", at = @At("TAIL"))
	private static void creativeoxidation$cleanStagedCopper(Level level, BlockPos pos, CallbackInfo ci) {
		OxidationManager.lightningStrike(level, pos);
	}
}
