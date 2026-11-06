package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.perf.PerfEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Times each block entity tick on the server (LevelChunk$BoundTickingBlockEntity#tick, m_142224_) by
 * registered block entity type, plus slow single ticks with a position (perf.md 3.6).
 */
@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity", remap = false)
public class BlockEntityTickPerfMixin {
	@Shadow(remap = false)
	@Final
	private BlockEntity f_156428_;

	@Unique
	private long ce$tickStart;

	@Inject(method = "m_142224_", remap = false, require = 0, at = @At("HEAD"))
	private void creativecommon$perfBlockEntityStart(CallbackInfo ci) {
		PerfEvents.seen(PerfEvents.HOOK_BLOCK_ENTITY);
		if (!PerfEvents.blockEntityTicks) return;
		Level level = f_156428_.getLevel();
		ce$tickStart = level != null && !level.isClientSide ? System.nanoTime() : 0L;
	}

	@Inject(method = "m_142224_", remap = false, require = 0, at = @At("RETURN"))
	private void creativecommon$perfBlockEntityEnd(CallbackInfo ci) {
		long start = ce$tickStart;
		if (start == 0L) return;
		ce$tickStart = 0L;
		Level level = f_156428_.getLevel();
		if (level != null) PerfEvents.blockEntityTicked(level, f_156428_, System.nanoTime() - start);
	}
}
