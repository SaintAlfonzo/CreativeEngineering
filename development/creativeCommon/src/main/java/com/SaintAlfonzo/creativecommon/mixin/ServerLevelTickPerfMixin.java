package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.perf.PerfEvents;
import com.SaintAlfonzo.creativecommon.perf.PerfJfrEvents;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

/** Times each dimension's tick (ServerLevel#tick, m_8793_) as a creativecommon.LevelTick JFR event (perf.md 3.6). */
@Mixin(value = ServerLevel.class, remap = false)
public class ServerLevelTickPerfMixin {
	@Unique
	private PerfJfrEvents.LevelTick ce$tickEvent;

	@Inject(method = "m_8793_", remap = false, require = 0, at = @At("HEAD"))
	private void creativecommon$perfLevelTickStart(BooleanSupplier hasTime, CallbackInfo ci) {
		ce$tickEvent = PerfEvents.beginLevelTick();
	}

	@Inject(method = "m_8793_", remap = false, require = 0, at = @At("RETURN"))
	private void creativecommon$perfLevelTickEnd(BooleanSupplier hasTime, CallbackInfo ci) {
		PerfJfrEvents.LevelTick e = ce$tickEvent;
		ce$tickEvent = null;
		PerfEvents.endLevelTick((ServerLevel) (Object) this, e);
	}
}
