// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.HookFaults;
import com.SaintAlfonzo.creativecompat.compat.SnowRealMagic;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Saves and syncs the "snowy look off" flag (ruling R19) with Snow! Real Magic's block entities: {@code saveState} and
 * {@code loadState} serve both the save file and the client update packet. {@code SnowCoveredBlockEntity} (the
 * snow-topped fence, gate, wall, slab and stairs) overrides both without calling up, so it is a target too. A client
 * that reads a changed flag redraws the block. Gated on {@code snowrealmagic}; {@code require = 0}.
 */
@Pseudo
@Mixin(targets = {"snownee.snow.block.entity.SnowBlockEntity", "snownee.snow.block.entity.SnowCoveredBlockEntity"},
		remap = false)
public abstract class SrmLookSaveMixin {

	@Inject(method = "saveState", at = @At("TAIL"), remap = false, require = 0)
	private void creativecompat$saveLook(CompoundTag data, boolean network, CallbackInfo ci) {
		try {
			SnowRealMagic.saveLook(this, data);
		} catch (Throwable t) {
			HookFaults.once("SrmLookSaveMixin#save", t);
		}
	}

	@Inject(method = "loadState", at = @At("TAIL"), remap = false, require = 0)
	private void creativecompat$loadLook(CompoundTag data, boolean network, CallbackInfo ci) {
		try {
			SnowRealMagic.loadLook(this, data);
		} catch (Throwable t) {
			HookFaults.once("SrmLookSaveMixin#load", t);
		}
	}
}
