package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.perf.KubeJSHandlerTiming;
import dev.latvian.mods.kubejs.event.EventHandlerContainer;
import dev.latvian.mods.kubejs.event.IEventHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wraps every KubeJS handler in KubeJSHandlerTiming as its container is built, so the handler's time is
 * recorded under its script and line (creativeengineering.KubeJSHandler / KubeJSHandlerStats, perf.md 3.6).
 * The container keeps source and line for KubeJS's own error messages; only the handler field changes.
 */
@Mixin(value = EventHandlerContainer.class, remap = false)
public class KubeJSEventHandlerContainerMixin {
	@Shadow(remap = false)
	@Final
	@Mutable
	public IEventHandler handler;

	@Inject(method = "<init>", remap = false, require = 0, at = @At("RETURN"))
	private void creativeengineering$perfWrapHandler(Object extraId, IEventHandler h, String source, int line, CallbackInfo ci) {
		handler = KubeJSHandlerTiming.wrap(handler, source, line);
	}
}
