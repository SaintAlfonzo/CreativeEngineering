package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.perf.PerfEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * Times each top-level entity tick on the server (Level#guardEntityTick, m_46653_; passengers are inside
 * their vehicle's tick) by registered entity type, plus slow single ticks with a position (perf.md 3.6).
 * An entity tick that throws out of here crashes the server, so the start field never goes stale.
 */
@Mixin(value = Level.class, remap = false)
public class LevelEntityTickPerfMixin {
	@Unique
	private long ce$entityTickStart;

	@Inject(method = "m_46653_", remap = false, require = 0, at = @At("HEAD"))
	private void creativeengineering$perfEntityStart(Consumer<Entity> tick, Entity entity, CallbackInfo ci) {
		PerfEvents.seen(PerfEvents.HOOK_ENTITY);
		ce$entityTickStart = PerfEvents.entityTicks && !((Level) (Object) this).isClientSide ? System.nanoTime() : 0L;
	}

	@Inject(method = "m_46653_", remap = false, require = 0, at = @At("RETURN"))
	private void creativeengineering$perfEntityEnd(Consumer<Entity> tick, Entity entity, CallbackInfo ci) {
		long start = ce$entityTickStart;
		if (start == 0L) return;
		ce$entityTickStart = 0L;
		PerfEvents.entityTicked((Level) (Object) this, entity, System.nanoTime() - start);
	}
}
