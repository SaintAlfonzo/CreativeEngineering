// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation.mixin.client;

import com.SaintAlfonzo.creativeoxidation.client.OxidizedPartialModels;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Valve pointer (no-Flywheel path) follows the valve's stage. */
@Mixin(value = FluidValveRenderer.class, remap = false)
abstract class FluidValveRendererMixin {
	@Redirect(method = "renderSafe(Lcom/simibubi/create/content/fluids/pipes/valve/FluidValveBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
			remap = false, at = @At(value = "FIELD",
			target = "Lcom/simibubi/create/AllPartialModels;FLUID_VALVE_POINTER:Lcom/jozufozu/flywheel/core/PartialModel;"))
	private PartialModel creativeoxidation$pointer(FluidValveBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		return OxidizedPartialModels.forState(OxidizedPartialModels.Part.FLUID_VALVE_POINTER, be.getBlockState(), AllPartialModels.FLUID_VALVE_POINTER);
	}
}
