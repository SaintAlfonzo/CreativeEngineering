package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.BeeNests;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bee nest counters (see worldgen/BeeNests): one chunk per ChunkGenerator#applyBiomeDecoration (m_213609_), for
 * the biome at its centre surface, so /dumpbeenests can report nests per chunk.
 */
@Mixin(value = ChunkGenerator.class, remap = false)
public class ChunkGeneratorBeeNestsMixin {

	@Inject(method = "m_213609_(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/StructureManager;)V",
			at = @At("HEAD"), require = 1, remap = false)
	private void creativeengineering$countChunk(WorldGenLevel level, ChunkAccess chunk, StructureManager structures, CallbackInfo ci) {
		BeeNests.chunkDecorated(level, chunk);
	}
}
