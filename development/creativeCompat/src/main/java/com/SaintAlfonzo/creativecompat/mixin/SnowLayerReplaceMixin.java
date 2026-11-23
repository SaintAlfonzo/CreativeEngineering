// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.SurfaceLayers;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Snow layers are replaced by a placed block up to {@code replaceMaxLayers} layers ({@link SurfaceLayers#replaceable};
 * vanilla: a single layer): HEAD of {@code canBeReplaced} ({@code m_6864_}). Salt layers override it in Vintage
 * Delight. Vanilla-class target, {@code remap = false}.
 */
@Mixin(value = SnowLayerBlock.class, remap = false)
public abstract class SnowLayerReplaceMixin {

	@Inject(method = "m_6864_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$replace(BlockState state, BlockPlaceContext ctx, CallbackInfoReturnable<Boolean> cir) {
		try {
			Boolean r = SurfaceLayers.replaceable(state, ctx);
			if (r != null) cir.setReturnValue(r);
		} catch (Throwable t) {
			com.SaintAlfonzo.creativecompat.compat.HookFaults.once("SnowLayerReplaceMixin", t);
			// the block's own rule
		}
	}
}
