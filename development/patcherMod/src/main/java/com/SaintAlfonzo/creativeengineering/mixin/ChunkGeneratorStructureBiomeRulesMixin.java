package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.StructureBiomeRules;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Predicate;

/**
 * Structure biome rules (see worldgen/StructureBiomeRules).
 *
 * ChunkGenerator#tryGenerateStructure (m_223104_, private) stores {@code structure.biomes()::contains} in a
 * local (slot 14) and passes it to Structure#generate, which rejects a start whose biome fails it. The
 * rules narrow that predicate for every structure a rule keeps out of some biomes.
 */
@Mixin(value = ChunkGenerator.class, remap = false)
public class ChunkGeneratorStructureBiomeRulesMixin {

	private static final String TRY_GENERATE = "m_223104_(Lnet/minecraft/world/level/levelgen/structure/StructureSet$StructureSelectionEntry;"
			+ "Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/levelgen/RandomState;"
			+ "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;JLnet/minecraft/world/level/chunk/ChunkAccess;"
			+ "Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/core/SectionPos;)Z";

	@ModifyVariable(method = TRY_GENERATE, at = @At("STORE"), index = 14, require = 1, remap = false)
	private Predicate<Holder<Biome>> creativeengineering$applyBiomeRules(Predicate<Holder<Biome>> valid, StructureSet.StructureSelectionEntry entry) {
		return StructureBiomeRules.filter(entry.structure().value(), valid);
	}
}
