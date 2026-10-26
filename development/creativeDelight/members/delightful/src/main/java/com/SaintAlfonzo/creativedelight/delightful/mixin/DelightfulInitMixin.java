package com.SaintAlfonzo.creativedelight.delightful.mixin;

import com.SaintAlfonzo.creativedelight.delightful.DelightfulBackports;
import net.brnbrd.delightful.Delightful;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hooks the backports into Delightful's own construction, so they register under its mod container and event bus. */
@Mixin(value = Delightful.class, remap = false)
public class DelightfulInitMixin {

	@Inject(method = "<init>", at = @At("TAIL"), remap = false)
	private void creativedelight$initBackports(CallbackInfo ci) {
		DelightfulBackports.init();
	}
}
