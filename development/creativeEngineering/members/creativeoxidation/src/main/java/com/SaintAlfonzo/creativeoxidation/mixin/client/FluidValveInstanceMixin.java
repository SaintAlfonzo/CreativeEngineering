// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation.mixin.client;

import com.SaintAlfonzo.creativeoxidation.client.OxidizedPartialModels;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Valve pointer (Flywheel 0.6 instance; upstream's FluidValveVisual) follows the valve's stage. */
@Mixin(value = FluidValveInstance.class, remap = false)
abstract class FluidValveInstanceMixin {
	@Redirect(method = "<init>", remap = false, at = @At(value = "FIELD",
			target = "Lcom/simibubi/create/AllPartialModels;FLUID_VALVE_POINTER:Lcom/jozufozu/flywheel/core/PartialModel;"))
	private PartialModel creativeoxidation$pointer(MaterialManager materialManager, FluidValveBlockEntity be) {
		return OxidizedPartialModels.forState(OxidizedPartialModels.Part.FLUID_VALVE_POINTER, be.getBlockState(), AllPartialModels.FLUID_VALVE_POINTER);
	}
}
