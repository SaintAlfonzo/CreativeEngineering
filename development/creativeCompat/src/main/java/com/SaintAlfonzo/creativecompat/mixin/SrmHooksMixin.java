// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.HookFaults;
import com.SaintAlfonzo.creativecompat.compat.SnowRealMagic;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Snow! Real Magic's {@code Hooks} (snow_real_magic.md 4c):
 * <ul>
 *   <li>{@code placeLayersOn} (storm piling, falling snow landing, snow put into a plant by hand): new snow turns the
 *       block's snowy look back on (R19). Onto a layer block that is not snow (a salt layer) it adds nothing: SRM would
 *       grow the salt with snow layers (bug B3).</li>
 *   <li>{@code randomTick} (SRM's replacement of every snow-layer block's random tick): only snow and SRM's snowy
 *       plant pile up in storms; a salt layer keeps its own rules (B3).</li>
 * </ul>
 * Gated on {@code snowrealmagic}; {@code require = 0}.
 */
@Pseudo
@Mixin(targets = "snownee.snow.Hooks", remap = false)
public abstract class SrmHooksMixin {

	@Inject(method = "placeLayersOn", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private static void creativecompat$newSnow(Level level, BlockPos pos, int layers, boolean fallingEffect,
											   BlockPlaceContext context, boolean playSound, boolean canConvert,
											   CallbackInfoReturnable<Boolean> cir) {
		try {
			if (SnowRealMagic.notSnowLayer(level.getBlockState(pos))) {
				cir.setReturnValue(false);
				return;
			}
			SnowRealMagic.lookOn(level, pos);
		} catch (Throwable t) {
			HookFaults.once("SrmHooksMixin#placeLayersOn", t);
		}
	}

	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private static void creativecompat$snowOnly(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
												CallbackInfo ci) {
		if (SnowRealMagic.notSnowLayer(state)) ci.cancel();
	}
}
