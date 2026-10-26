package com.SaintAlfonzo.creativeengineering.perf;

/** Added to EntityType and BlockEntityType (EntityTypePerfStatsMixin, BlockEntityTypePerfStatsMixin): the type's per-second tick totals. */
public interface PerfStatsHolder {
	PerfEvents.TickStats ce$perfStats();
}
