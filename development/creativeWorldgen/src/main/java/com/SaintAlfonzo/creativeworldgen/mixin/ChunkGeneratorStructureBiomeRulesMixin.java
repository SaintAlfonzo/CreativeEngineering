package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.LocateStats;
import com.SaintAlfonzo.creativeworldgen.worldgen.MetaSlots;
import com.SaintAlfonzo.creativeworldgen.worldgen.StructureBiomeRules;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Predicate;

/**
 * Structure biome rules (see worldgen/StructureBiomeRules).
 *
 * ChunkGenerator#tryGenerateStructure (m_223104_, private) stores {@code structure.biomes()::contains} in a
 * local (slot 14) and passes it to Structure#generate, which rejects a start whose biome fails it. The
 * rules narrow that predicate for every structure a rule keeps out of some biomes, and a replacement taking
 * another structure's slot (worldgen/MetaSlots) also needs that structure's biomes.
 *
 * The generated start (slot 15, as in ChunkGeneratorEarthFillMixin) goes through the rules' surface check:
 * INVALID_START makes the pick fail the way a wrong-biome start does.
 */
@Mixin(value = ChunkGenerator.class, remap = false)
public class ChunkGeneratorStructureBiomeRulesMixin {

	private static final String TRY_GENERATE = "m_223104_(Lnet/minecraft/world/level/levelgen/structure/StructureSet$StructureSelectionEntry;"
			+ "Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/levelgen/RandomState;"
			+ "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;JLnet/minecraft/world/level/chunk/ChunkAccess;"
			+ "Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/core/SectionPos;)Z";

	@ModifyVariable(method = TRY_GENERATE, at = @At("STORE"), index = 14, require = 1, remap = false)
	private Predicate<Holder<Biome>> creativeengineering$applyBiomeRules(Predicate<Holder<Biome>> valid, StructureSet.StructureSelectionEntry entry) {
		return MetaSlots.narrowBiomes(entry.structure().value(), StructureBiomeRules.filter(entry.structure().value(), valid));
	}

	@ModifyVariable(method = TRY_GENERATE, at = @At("STORE"), index = 15, require = 1, remap = false)
	private StructureStart creativeengineering$surfaceCheck(StructureStart start, StructureSet.StructureSelectionEntry entry,
			StructureManager structureManager, RegistryAccess registryAccess, RandomState randomState,
			StructureTemplateManager templateManager, long seed, ChunkAccess chunk) {
		StructureStart out = StructureBiomeRules.surfaceCheck(start, entry.structure().value(), (ChunkGenerator) (Object) this, randomState, chunk);
		if (out != start && start.isValid() && !out.isValid()) LocateStats.reject(entry.structure().value(), chunk.getPos(), LocateStats.Reason.SURFACE_CHECK);
		return out;
	}
}
