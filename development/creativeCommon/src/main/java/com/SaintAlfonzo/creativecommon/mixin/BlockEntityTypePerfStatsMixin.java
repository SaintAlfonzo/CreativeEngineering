package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.perf.PerfEvents;
import com.SaintAlfonzo.creativecommon.perf.PerfStatsHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Per-type tick totals for BlockEntityTickPerfMixin, kept on the type itself so the tick hook needs no map lookup. */
@Mixin(value = BlockEntityType.class, remap = false)
public class BlockEntityTypePerfStatsMixin implements PerfStatsHolder {
	@Unique
	private PerfEvents.TickStats creativecommon$perfStats;

	@Override
	public PerfEvents.TickStats creativecommon$perfStats() {
		if (creativecommon$perfStats == null) {
			ResourceLocation id = BlockEntityType.getKey((BlockEntityType<?>) (Object) this);
			creativecommon$perfStats = new PerfEvents.TickStats(id == null ? "unregistered" : id.toString());
		}
		return creativecommon$perfStats;
	}
}
