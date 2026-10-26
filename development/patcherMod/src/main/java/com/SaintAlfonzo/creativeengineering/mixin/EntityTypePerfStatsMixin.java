package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.perf.PerfEvents;
import com.SaintAlfonzo.creativeengineering.perf.PerfStatsHolder;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Per-type tick totals for LevelEntityTickPerfMixin, kept on the type itself so the tick hook needs no map lookup. */
@Mixin(value = EntityType.class, remap = false)
public class EntityTypePerfStatsMixin implements PerfStatsHolder {
	@Unique
	private PerfEvents.TickStats ce$perfStats;

	@Override
	public PerfEvents.TickStats ce$perfStats() {
		if (ce$perfStats == null) ce$perfStats = new PerfEvents.TickStats(EntityType.getKey((EntityType<?>) (Object) this).toString());
		return ce$perfStats;
	}
}
