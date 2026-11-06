package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.DimensionFilter;
import com.SaintAlfonzo.creativeworldgen.worldgen.LocateStats;
import com.SaintAlfonzo.creativeworldgen.worldgen.MetaSlots;
import com.SaintAlfonzo.creativeworldgen.worldgen.StructureFrequencies;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Per-structure frequency inside a structure_set (see worldgen/StructureFrequencies), and meta structures
 * taking over slots their anchors won (worldgen/MetaSlots).
 *
 * ChunkGenerator#tryGenerateStructure (m_223104_, private) is what the per-set lambda in
 * createStructures calls for each weighted pick: it generates the structure's start and, if valid,
 * stores it with StructureManager#setStartForStructure (m_220516_) and returns true; a false makes the
 * lambda try the next pick. It is the only caller path for structure starts, single-member sets included.
 *
 *  - right before the start is stored: roll the member's frequency (x its biome factor at its generation point,
 *    for a group with biome_frequency). A miss returns false and vetoes the slot for the rest of that set's picks
 *    in this chunk.
 *  - at HEAD: a vetoed slot returns false immediately (no generation work). A converted meta structure
 *    or replacement picked directly returns false too (it only comes from its anchors' slots). An anchor
 *    whose slot converts first runs this method for the meta or replacement (all the other mixins on it
 *    apply to that structure), and goes on as itself only when that isn't built. Last, a structure no biome of
 *    this dimension can hold returns false (worldgen/DimensionFilter).
 */
@Mixin(value = ChunkGenerator.class, remap = false)
public abstract class ChunkGeneratorStructureFrequencyMixin {

	private static final String TRY_GENERATE = "m_223104_(Lnet/minecraft/world/level/levelgen/structure/StructureSet$StructureSelectionEntry;"
			+ "Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/levelgen/RandomState;"
			+ "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;JLnet/minecraft/world/level/chunk/ChunkAccess;"
			+ "Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/core/SectionPos;)Z";

	@Shadow(remap = false)
	private boolean m_223104_(StructureSet.StructureSelectionEntry entry, StructureManager structureManager, RegistryAccess registryAccess,
			RandomState randomState, StructureTemplateManager templateManager, long seed, ChunkAccess chunk, ChunkPos chunkPos, SectionPos sectionPos) {
		throw new AssertionError();
	}

	@Inject(method = TRY_GENERATE, at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private void creativeengineering$skipVetoedSlot(StructureSet.StructureSelectionEntry entry, StructureManager structureManager,
			RegistryAccess registryAccess, RandomState randomState, StructureTemplateManager templateManager, long seed,
			ChunkAccess chunk, ChunkPos chunkPos, SectionPos sectionPos, CallbackInfoReturnable<Boolean> cir) {
		if (StructureFrequencies.slotVetoed(entry, chunk)) {
			LocateStats.reject(entry.structure().value(), chunkPos, LocateStats.Reason.VETO);
			cir.setReturnValue(false);
			return;
		}
		MetaSlots.Decision d = MetaSlots.decide(entry, registryAccess, seed, chunkPos);
		if (d.skip()) {
			LocateStats.reject(entry.structure().value(), chunkPos, LocateStats.Reason.META_ONLY);
			cir.setReturnValue(false);
			return;
		}
		if (d.convert()) {
			boolean built;
			MetaSlots.beginConversion(d.targetEntry(), entry, chunkPos);
			try {
				built = m_223104_(d.targetEntry(), structureManager, registryAccess, randomState, templateManager, seed, chunk, chunkPos, sectionPos);
			} finally {
				MetaSlots.endConversion();
			}
			if (built) {
				LocateStats.reject(entry.structure().value(), chunkPos, LocateStats.Reason.CONVERTED);
				cir.setReturnValue(true);
				return;
			}
		}
		// no biome of this dimension passes the structure's biome test: it would fail after all the generation work
		if (DimensionFilter.impossible((ChunkGenerator) (Object) this, entry.structure().value())) {
			LocateStats.reject(entry.structure().value(), chunkPos, LocateStats.Reason.DIMENSION);
			cir.setReturnValue(false);
		}
	}

	/** Census (worldgen/LocateCensus): this pick built, or failed with no reason recorded by a reject point. */
	@Inject(method = TRY_GENERATE, at = @At("RETURN"), require = 1, remap = false)
	private void creativeengineering$censusAttempt(StructureSet.StructureSelectionEntry entry, StructureManager structureManager,
			RegistryAccess registryAccess, RandomState randomState, StructureTemplateManager templateManager, long seed,
			ChunkAccess chunk, ChunkPos chunkPos, SectionPos sectionPos, CallbackInfoReturnable<Boolean> cir) {
		LocateStats.attempted(entry.structure().value(), chunkPos, cir.getReturnValueZ());
	}

	@Inject(method = TRY_GENERATE,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/StructureManager;m_220516_(Lnet/minecraft/core/SectionPos;"
					+ "Lnet/minecraft/world/level/levelgen/structure/Structure;Lnet/minecraft/world/level/levelgen/structure/StructureStart;"
					+ "Lnet/minecraft/world/level/chunk/StructureAccess;)V"),
			cancellable = true, require = 1, remap = false)
	private void creativeengineering$rollFrequency(StructureSet.StructureSelectionEntry entry, StructureManager structureManager,
			RegistryAccess registryAccess, RandomState randomState, StructureTemplateManager templateManager, long seed,
			ChunkAccess chunk, ChunkPos chunkPos, SectionPos sectionPos, CallbackInfoReturnable<Boolean> cir) {
		// members with biome factors (a group's biome_frequency) roll at the biome of their generation point
		ChunkGenerator self = (ChunkGenerator) (Object) this;
		java.util.function.Supplier<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> sample =
				StructureFrequencies.hasBiomeFactors(entry.structure().value())
						? () -> StructureFrequencies.sampleBiome(entry.structure().value(), registryAccess, self, randomState, templateManager, seed, chunkPos, chunk)
						: null;
		if (StructureFrequencies.rejects(entry, seed, chunkPos, chunk, sample)) {
			LocateStats.reject(entry.structure().value(), chunkPos, LocateStats.Reason.FREQUENCY);
			cir.setReturnValue(false);
		}
	}
}
