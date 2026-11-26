// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation.mixin.client;

import com.SaintAlfonzo.creativeoxidation.client.OxidizedPartialModels;
import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceRenderer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Portable fluid interface top and middle follow its stage (the item interface has no properties, so keeps Create's). */
@Mixin(value = PortableStorageInterfaceRenderer.class, remap = false)
abstract class PortableStorageInterfaceRendererMixin {
	@Inject(method = "getTopForState", remap = false, at = @At("RETURN"), cancellable = true)
	private static void creativeoxidation$top(BlockState state, CallbackInfoReturnable<PartialModel> cir) {
		cir.setReturnValue(OxidizedPartialModels.forState(OxidizedPartialModels.Part.PORTABLE_INTERFACE_TOP, state, cir.getReturnValue()));
	}

	@Inject(method = "getMiddleForState", remap = false, at = @At("RETURN"), cancellable = true)
	private static void creativeoxidation$middle(BlockState state, boolean powered, CallbackInfoReturnable<PartialModel> cir) {
		OxidizedPartialModels.Part part = powered ? OxidizedPartialModels.Part.PORTABLE_INTERFACE_MIDDLE_POWERED
				: OxidizedPartialModels.Part.PORTABLE_INTERFACE_MIDDLE;
		cir.setReturnValue(OxidizedPartialModels.forState(part, state, cir.getReturnValue()));
	}
}
