package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.LocateStats;
import com.SaintAlfonzo.creativeworldgen.worldgen.SeabedClearance;
import net.minecraft.core.RegistryAccess;
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
 * Seabed clearance (worldgen/SeabedClearance): the generated start of ChunkGenerator#tryGenerateStructure (m_223104_,
 * slot 15, as in ChunkGeneratorStructureBiomeRulesMixin and ChunkGeneratorEarthFillMixin) of a structure tagged
 * creativeengineering:seabed_clearance becomes INVALID_START when its box reaches a water-covered floor.
 */
@Mixin(value = ChunkGenerator.class, remap = false)
public class ChunkGeneratorSeabedClearanceMixin {

	private static final String TRY_GENERATE = "m_223104_(Lnet/minecraft/world/level/levelgen/structure/StructureSet$StructureSelectionEntry;"
			+ "Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/levelgen/RandomState;"
			+ "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;JLnet/minecraft/world/level/chunk/ChunkAccess;"
			+ "Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/core/SectionPos;)Z";

	@ModifyVariable(method = TRY_GENERATE, at = @At("STORE"), index = 15, require = 1, remap = false)
	private StructureStart creativeengineering$seabedClearance(StructureStart start, StructureSet.StructureSelectionEntry entry,
			StructureManager structureManager, RegistryAccess registryAccess, RandomState randomState,
			StructureTemplateManager templateManager, long seed, ChunkAccess chunk) {
		StructureStart out = SeabedClearance.check(start, entry.structure(), (ChunkGenerator) (Object) this, randomState, chunk);
		if (out != start) LocateStats.reject(entry.structure().value(), chunk.getPos(), LocateStats.Reason.SURFACE_CHECK);
		return out;
	}
}
