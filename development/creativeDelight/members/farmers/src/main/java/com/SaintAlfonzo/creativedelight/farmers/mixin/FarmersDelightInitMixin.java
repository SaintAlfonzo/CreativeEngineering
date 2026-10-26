package com.SaintAlfonzo.creativedelight.farmers.mixin;

import com.SaintAlfonzo.creativedelight.farmers.FDBackports;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.FarmersDelight;

/** Hooks the backports into FD's own construction, so they register under FD's mod container and event bus. */
@Mixin(value = FarmersDelight.class, remap = false)
public class FarmersDelightInitMixin {

	@Inject(method = "<init>", at = @At("TAIL"), remap = false)
	private void creativedelight$initBackports(CallbackInfo ci) {
		FDBackports.init();
	}
}
