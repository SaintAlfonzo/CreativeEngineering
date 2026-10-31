package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.StructureSpawnBoxes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spawn boxes (worldgen/StructureSpawnBoxes): ChunkGenerator#getMobsAt (m_223133_) answers with the box pool of the
 * structure (or meta part) at the position; with none, vanilla's structure overrides and the biome list as before.
 */
@Mixin(value = ChunkGenerator.class, remap = false)
public class ChunkGeneratorSpawnBoxesMixin {

	@Inject(method = "m_223133_(Lnet/minecraft/core/Holder;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/util/random/WeightedRandomList;",
			at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private void creativeengineering$spawnBoxes(Holder<Biome> biome, StructureManager structures, MobCategory category, BlockPos pos,
			CallbackInfoReturnable<WeightedRandomList<MobSpawnSettings.SpawnerData>> cir) {
		WeightedRandomList<MobSpawnSettings.SpawnerData> box = StructureSpawnBoxes.mobsAt(structures, category, pos);
		if (box != null) cir.setReturnValue(box);
	}
}
