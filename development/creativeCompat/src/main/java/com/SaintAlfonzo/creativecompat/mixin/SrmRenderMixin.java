// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.HookFaults;
import com.SaintAlfonzo.creativecompat.compat.SnowRealMagicClient;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

/**
 * Snow! Real Magic's block render ({@code snownee.snow.client.SnowClient}, used by both its vanilla and its Rubidium
 * chunk render hooks), snow_real_magic.md 4c:
 * <ul>
 *   <li>{@code shouldRedirect}: plain snow layers go through SRM's render too, so the snow-drip sides (SRM's
 *       {@code block/overlay} model) are drawn on the block under every snow layer (R11).</li>
 *   <li>{@code renderHook}: the drip sides on every snowy plant by default (R11); a block whose snowy look is off (R19)
 *       draws its held block plain and its snow layer, without the snowy variant, the drip sides or the snow cap.</li>
 * </ul>
 * {@link SnowRealMagicClient} draws. Client only, gated on {@code snowrealmagic}; {@code require = 0}.
 */
@Pseudo
@Mixin(targets = "snownee.snow.client.SnowClient", remap = false)
public abstract class SrmRenderMixin {

	@Inject(method = "shouldRedirect", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private static void creativecompat$plainSnow(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (SnowRealMagicClient.drawsPlainSnow(state)) cir.setReturnValue(true);
	}

	@Inject(method = "renderHook", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private static void creativecompat$render(BlockAndTintGetter world, BlockPos pos, BlockState state, BlockState camo,
											  snownee.snow.block.entity.SnowBlockEntity.Options options, RenderType layer,
											  Supplier<RandomSource> random, boolean cullSides,
											  snownee.snow.client.RenderAPI api, CallbackInfoReturnable<Boolean> cir) {
		try {
			Boolean drawn = SnowRealMagicClient.render(world, pos, state, camo, options, layer, random, cullSides, api);
			if (drawn != null) cir.setReturnValue(drawn);
		} catch (Throwable t) {
			HookFaults.once("SrmRenderMixin", t);
			// SRM's render
		}
	}
}
