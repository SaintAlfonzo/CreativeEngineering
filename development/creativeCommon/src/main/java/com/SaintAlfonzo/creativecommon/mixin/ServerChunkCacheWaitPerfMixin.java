package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.perf.PerfEvents;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Times the server thread's chunk waits: ServerChunkCache#getChunk (m_7587_) on a cache miss, and
 * getChunkFuture (m_8431_) when called on the server thread, park in managedBlock (m_18701_) until the
 * chunk future is done, in 0.1 ms slices that JFR's park events never
 * see. Feeds creativecommon.ChunkWait (with the requester's stack) and ChunkWaitStats (perf.md 3.6).
 * Off-thread callers hand the call to the server thread first, so only the server thread gets here.
 */
@Mixin(value = ServerChunkCache.class, remap = false)
public class ServerChunkCacheWaitPerfMixin {
	private static final String MANAGED_BLOCK =
			"Lnet/minecraft/server/level/ServerChunkCache$MainThreadExecutor;m_18701_(Ljava/util/function/BooleanSupplier;)V";

	@Inject(method = {"m_7587_", "m_8431_"}, remap = false, require = 0,
			at = @At(value = "INVOKE", remap = false, target = MANAGED_BLOCK))
	private void creativecommon$perfWaitStart(int x, int z, ChunkStatus status, boolean load,
			CallbackInfoReturnable<?> cir) {
		PerfEvents.chunkWaitStart();
	}

	@Inject(method = {"m_7587_", "m_8431_"}, remap = false, require = 0,
			at = @At(value = "INVOKE", remap = false, target = MANAGED_BLOCK, shift = At.Shift.AFTER))
	private void creativecommon$perfWaitEnd(int x, int z, ChunkStatus status, boolean load,
			CallbackInfoReturnable<?> cir) {
		PerfEvents.chunkWaitEnd(((ServerChunkCache) (Object) this).getLevel(), x, z, status);
	}
}
