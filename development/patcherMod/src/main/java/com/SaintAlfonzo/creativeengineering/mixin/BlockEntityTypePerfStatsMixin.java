package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.perf.PerfEvents;
import com.SaintAlfonzo.creativeengineering.perf.PerfStatsHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Per-type tick totals for BlockEntityTickPerfMixin, kept on the type itself so the tick hook needs no map lookup. */
@Mixin(value = BlockEntityType.class, remap = false)
public class BlockEntityTypePerfStatsMixin implements PerfStatsHolder {
	@Unique
	private PerfEvents.TickStats ce$perfStats;

	@Override
	public PerfEvents.TickStats ce$perfStats() {
		if (ce$perfStats == null) {
			ResourceLocation id = BlockEntityType.getKey((BlockEntityType<?>) (Object) this);
			ce$perfStats = new PerfEvents.TickStats(id == null ? "unregistered" : id.toString());
		}
		return ce$perfStats;
	}
}
