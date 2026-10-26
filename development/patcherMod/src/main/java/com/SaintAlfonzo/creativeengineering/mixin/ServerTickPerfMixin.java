package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.perf.PerfEvents;
import com.SaintAlfonzo.creativeengineering.perf.PerfJfrEvents;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

/**
 * Times every server tick (MinecraftServer#tickServer, m_5705_) as a creativeengineering.ServerTick JFR event,
 * and writes the per-second perf totals at its end (PerfEvents, feature_writeup/perf.md 3.6).
 * Minecraft's own minecraft.ServerTickTime is a 100-tick average that smears short spikes.
 */
@Mixin(value = MinecraftServer.class, remap = false)
public class ServerTickPerfMixin {
	@Unique
	private PerfJfrEvents.ServerTick ce$tickEvent;

	@Inject(method = "m_5705_", remap = false, require = 0, at = @At("HEAD"))
	private void creativeengineering$perfTickStart(BooleanSupplier hasTime, CallbackInfo ci) {
		ce$tickEvent = PerfEvents.beginServerTick();
	}

	@Inject(method = "m_5705_", remap = false, require = 0, at = @At("RETURN"))
	private void creativeengineering$perfTickEnd(BooleanSupplier hasTime, CallbackInfo ci) {
		PerfJfrEvents.ServerTick e = ce$tickEvent;
		ce$tickEvent = null;
		PerfEvents.endServerTick((MinecraftServer) (Object) this, e);
	}
}
