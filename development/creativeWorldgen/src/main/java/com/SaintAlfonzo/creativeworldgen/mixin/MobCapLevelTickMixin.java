package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.perf.MobCapLevels;
import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Picks the dimension's mob cap level (perf.md 17.2) from the spawnable chunk count vanilla passes to
 * {@code NaturalSpawner.createState}, right before that tick's spawning. The count itself is not changed.
 */
@Mixin(value = ServerChunkCache.class, remap = false)
public abstract class MobCapLevelTickMixin {

	// m_8490_ = tickChunks(), m_186524_ = NaturalSpawner.createState (no refmap: SRG names, remap = false)
	@ModifyArg(method = "m_8490_", remap = false, index = 0,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/NaturalSpawner;m_186524_(ILjava/lang/Iterable;Lnet/minecraft/world/level/NaturalSpawner$ChunkGetter;Lnet/minecraft/world/level/LocalMobCapCalculator;)Lnet/minecraft/world/level/NaturalSpawner$SpawnState;"))
	private int creativeengineering$pickMobCapLevel(int spawnableChunks) {
		MobCapLevels.update(((ServerChunkCache) (Object) this).level, spawnableChunks);
		return spawnableChunks;
	}
}
