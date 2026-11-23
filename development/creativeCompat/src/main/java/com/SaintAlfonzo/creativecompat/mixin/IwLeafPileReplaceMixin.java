// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.SurfaceLayers;
import com.ordana.immersive_weathering.blocks.LeafPileBlock;
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
 * Immersive Weathering leaf piles are replaced by a placed block up to {@code replaceMaxLayers} layers
 * ({@link SurfaceLayers#replaceable}; was a single layer only): {@code canBeReplaced} ({@code m_6864_}). SRG names,
 * {@code remap = false}; applied only with the mod.
 */
@Mixin(value = LeafPileBlock.class, remap = false)
public abstract class IwLeafPileReplaceMixin {

	@Inject(method = "m_6864_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$replace(BlockState state, BlockPlaceContext ctx, CallbackInfoReturnable<Boolean> cir) {
		try {
			Boolean r = SurfaceLayers.replaceable(state, ctx);
			if (r != null) cir.setReturnValue(r);
		} catch (Throwable t) {
			com.SaintAlfonzo.creativecompat.compat.HookFaults.once("IwLeafPileReplaceMixin", t);
			// the block's own rule
		}
	}
}
