package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.perf.MobCapLevels;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The spawn area shrinks at mob cap level 1 or 2 (perf.md 17.3 and 17.4): a chunk outside the level's spawn radius
 * of every player skips its spawn attempt for the category, and a spawn position too far above or below the
 * nearest player is rejected before the biome spawn list, placement and spawn rule checks run.
 */
@Mixin(value = NaturalSpawner.class, remap = false)
public abstract class MobCapSpawnAreaMixin {

	// m_47045_ = spawnCategoryForChunk (no refmap: SRG name, remap = false)
	@Inject(method = "m_47045_", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$spawnRadius(MobCategory category, ServerLevel level, LevelChunk chunk,
			NaturalSpawner.SpawnPredicate predicate, NaturalSpawner.AfterSpawnCallback callback, CallbackInfo ci) {
		if (MobCapLevels.outsideRadius(level, category, chunk.getPos())) ci.cancel();
	}

	// m_220421_ = isValidSpawnPostitionForType
	@Inject(method = "m_220421_", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$verticalBand(ServerLevel level, MobCategory category,
			StructureManager structures, ChunkGenerator generator, MobSpawnSettings.SpawnerData data,
			BlockPos.MutableBlockPos pos, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
		if (MobCapLevels.outsideBand(level, category, pos)) cir.setReturnValue(false);
	}
}
