// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation.mixin.client;

import com.SaintAlfonzo.creativeoxidation.client.OxidizedPartialModels;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Boiler gauge and dial follow the tank's stage. */
@Mixin(value = FluidTankRenderer.class, remap = false)
abstract class FluidTankRendererMixin {
	@Redirect(method = "renderAsBoiler", remap = false, at = @At(value = "FIELD",
			target = "Lcom/simibubi/create/AllPartialModels;BOILER_GAUGE:Lcom/jozufozu/flywheel/core/PartialModel;"))
	private PartialModel creativeoxidation$gauge(FluidTankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		return OxidizedPartialModels.forState(OxidizedPartialModels.Part.BOILER_GAUGE, be.getBlockState(), AllPartialModels.BOILER_GAUGE);
	}

	@Redirect(method = "renderAsBoiler", remap = false, at = @At(value = "FIELD",
			target = "Lcom/simibubi/create/AllPartialModels;BOILER_GAUGE_DIAL:Lcom/jozufozu/flywheel/core/PartialModel;"))
	private PartialModel creativeoxidation$gaugeDial(FluidTankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		return OxidizedPartialModels.forState(OxidizedPartialModels.Part.BOILER_GAUGE_DIAL, be.getBlockState(), AllPartialModels.BOILER_GAUGE_DIAL);
	}
}
