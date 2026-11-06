package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.RingPositionCache;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Stronghold ring positions read from the world's data instead of recomputed every start
 * (feature_writeup/perf.md 11.2 #2; the key and file are in {@link RingPositionCache}).
 *
 * generateRingPositions (m_223148_) is private and returns a future the generator keeps in ringPositions: on a stored
 * key HEAD returns a completed future with the stored list; on a miss RETURN chains a store onto vanilla's future.
 */
@Mixin(value = ChunkGenerator.class, remap = false)
public class ChunkGeneratorRingCacheMixin {

	@Shadow(remap = false) @Final protected BiomeSource f_62137_;

	@Inject(method = "m_223148_(Lnet/minecraft/core/Holder;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/structure/placement/ConcentricRingsStructurePlacement;)Ljava/util/concurrent/CompletableFuture;",
			at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private void creativeengineering$readStoredRings(Holder<StructureSet> set, RandomState randomState,
			ConcentricRingsStructurePlacement placement, CallbackInfoReturnable<CompletableFuture<List<ChunkPos>>> cir) {
		List<ChunkPos> stored = RingPositionCache.lookup((ChunkGenerator) (Object) this, f_62137_, set, randomState, placement);
		if (stored != null) cir.setReturnValue(CompletableFuture.completedFuture(stored));
	}

	@Inject(method = "m_223148_(Lnet/minecraft/core/Holder;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/structure/placement/ConcentricRingsStructurePlacement;)Ljava/util/concurrent/CompletableFuture;",
			at = @At("RETURN"), cancellable = true, require = 1, remap = false)
	private void creativeengineering$storeComputedRings(Holder<StructureSet> set, RandomState randomState,
			ConcentricRingsStructurePlacement placement, CallbackInfoReturnable<CompletableFuture<List<ChunkPos>>> cir) {
		String key = RingPositionCache.takePendingKey();
		if (key == null) return;
		String setId = set.unwrapKey().map(k -> k.location().toString()).orElse("<direct>");
		cir.setReturnValue(cir.getReturnValue().thenApply(positions -> {
			RingPositionCache.store(key, setId, positions);
			return positions;
		}));
	}
}
