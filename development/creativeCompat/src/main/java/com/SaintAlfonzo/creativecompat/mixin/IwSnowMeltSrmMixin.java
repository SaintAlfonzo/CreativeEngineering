// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.HookFaults;
import com.SaintAlfonzo.creativecompat.compat.SnowRealMagic;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Immersive Weathering's rain melt keeps the plant (snow_real_magic.md 4 step 8, bug B5). IW's
 * {@code SnowLayerBlockMixin} removes a snow layer that rain falls on (HEAD of {@code SnowLayerBlock.randomTick},
 * {@code m_213898_}); on Snow! Real Magic's snowy plant that would delete the plant with the snow. This HEAD hook is
 * applied first ({@code priority = 900}, IW uses 1000, so its callback runs before IW's) and, in the same rain, sets the
 * held plant back and ends the tick. Plain snow layers are left to IW. Needs both mods ({@code CompatMixinPlugin}
 * NEEDS_ALL).
 */
@Mixin(value = SnowLayerBlock.class, remap = false, priority = 900)
public abstract class IwSnowMeltSrmMixin {

	@Inject(method = "m_213898_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativecompat$rainKeepsPlant(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
											   CallbackInfo ci) {
		try {
			if (!SnowRealMagic.isSnowLayer(state) || !level.isRainingAt(pos.above())) return;
			BlockState held = SnowRealMagic.held(level, pos, state);
			if (held == null) return;
			level.setBlockAndUpdate(pos, held);
			ci.cancel();
		} catch (Throwable t) {
			HookFaults.once("IwSnowMeltSrmMixin", t);
			// IW's melt as it is
		}
	}
}
