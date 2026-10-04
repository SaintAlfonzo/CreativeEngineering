package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.EarthFill;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Earth-fill check (see worldgen/EarthFill).
 *
 * ChunkGenerator#tryGenerateStructure (m_223104_, private) stores the generated StructureStart in a local
 * (slot 15: after the 9 parameters, one of them a long, and the structure, reference count, biome set and
 * biome predicate locals) and stores it if isValid(). Swapping in StructureStart.INVALID_START makes the
 * pick fail the same way a start in a wrong biome does.
 */
@Mixin(value = ChunkGenerator.class, remap = false)
public class ChunkGeneratorEarthFillMixin {

	private static final String TRY_GENERATE = "m_223104_(Lnet/minecraft/world/level/levelgen/structure/StructureSet$StructureSelectionEntry;"
			+ "Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/levelgen/RandomState;"
			+ "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;JLnet/minecraft/world/level/chunk/ChunkAccess;"
			+ "Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/core/SectionPos;)Z";

	@ModifyVariable(method = TRY_GENERATE, at = @At("STORE"), index = 15, require = 1, remap = false)
	private StructureStart creativeengineering$checkEarthFill(StructureStart start, StructureSet.StructureSelectionEntry entry,
			StructureManager structureManager, RegistryAccess registryAccess, RandomState randomState,
			StructureTemplateManager templateManager, long seed, ChunkAccess chunk, ChunkPos chunkPos) {
		return EarthFill.filter(start, entry, (ChunkGenerator) (Object) this, randomState, templateManager, seed, chunk, chunkPos);
	}
}
