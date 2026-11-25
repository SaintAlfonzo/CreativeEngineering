// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.HookFaults;
import com.SaintAlfonzo.creativecompat.compat.SnowRealMagic;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A snow layer placed by hand onto snow whose snowy look is off turns the look back on (ruling R19): RETURN of
 * {@code SnowLayerBlock.getStateForPlacement} ({@code m_5573_}, which Snow! Real Magic overwrites to add a layer to the
 * block in place). Plain snow with its look off goes back to a plain snow layer. Vanilla-class target, gated on
 * {@code snowrealmagic}.
 */
@Mixin(value = SnowLayerBlock.class, remap = false)
public abstract class SrmSnowPlaceLookMixin {

	@Inject(method = "m_5573_", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
	private void creativecompat$placeLookOn(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
		try {
			BlockState placed = cir.getReturnValue();
			if (placed == null) return;
			BlockState swapped = SnowRealMagic.placedLookOn(context.getLevel(), context.getClickedPos(), placed);
			if (swapped != placed) cir.setReturnValue(swapped);
		} catch (Throwable t) {
			HookFaults.once("SrmSnowPlaceLookMixin", t);
		}
	}
}
