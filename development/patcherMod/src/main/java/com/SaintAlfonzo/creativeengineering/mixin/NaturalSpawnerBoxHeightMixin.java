package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.StructureSpawnBoxes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spawn boxes (worldgen/StructureSpawnBoxes#boxHeight): in a column a box covers, half the natural spawn attempts start
 * at a height inside the box instead of anywhere from the world bottom. getRandomPosWithin (m_47062_) has no category,
 * so spawnCategoryForChunk (m_47045_, its only caller) notes it first. Server thread only (levels tick in turn).
 */
@Mixin(value = NaturalSpawner.class, remap = false)
public abstract class NaturalSpawnerBoxHeightMixin {

	@Unique
	private static MobCategory creativeengineering$boxCategory;

	@Inject(method = "m_47045_", at = @At("HEAD"), remap = false)
	private static void creativeengineering$noteCategory(MobCategory category, ServerLevel level, LevelChunk chunk,
			NaturalSpawner.SpawnPredicate predicate, NaturalSpawner.AfterSpawnCallback callback, CallbackInfo ci) {
		creativeengineering$boxCategory = category;
	}

	@Inject(method = "m_47062_", at = @At("RETURN"), cancellable = true, remap = false)
	private static void creativeengineering$boxHeight(Level level, LevelChunk chunk, CallbackInfoReturnable<BlockPos> cir) {
		MobCategory category = creativeengineering$boxCategory;
		creativeengineering$boxCategory = null;
		if (category == null || !(level instanceof ServerLevel server)) return;
		BlockPos pos = cir.getReturnValue();
		BlockPos moved = StructureSpawnBoxes.boxHeight(server, category, chunk, pos);
		if (moved != pos) cir.setReturnValue(moved);
	}
}
