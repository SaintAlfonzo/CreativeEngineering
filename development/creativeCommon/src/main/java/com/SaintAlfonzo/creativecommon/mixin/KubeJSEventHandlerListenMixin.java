package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.perf.KubeJSHandlerTiming;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.IEventHandler;
import dev.latvian.mods.kubejs.script.ScriptType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tells KubeJSEventHandlerContainerMixin which event (e.g. LevelEvents.tick) the handler being registered
 * belongs to: EventHandler.listen builds the EventHandlerContainer, which only knows script and line.
 */
@Mixin(value = EventHandler.class, remap = false)
public class KubeJSEventHandlerListenMixin {
	@Inject(method = {"listen", "listenJava"}, remap = false, require = 0, at = @At("HEAD"))
	private void creativecommon$perfListenStart(ScriptType type, Object extraId, IEventHandler handler, CallbackInfo ci) {
		EventHandler self = (EventHandler) (Object) this;
		EventGroup group = self.group;
		KubeJSHandlerTiming.pendingEvent = (group == null ? "?" : group.name) + "." + self.name
				+ (extraId == null ? "" : "[" + extraId + "]");
	}

	@Inject(method = {"listen", "listenJava"}, remap = false, require = 0, at = @At("RETURN"))
	private void creativecommon$perfListenEnd(ScriptType type, Object extraId, IEventHandler handler, CallbackInfo ci) {
		KubeJSHandlerTiming.pendingEvent = null;
	}
}
