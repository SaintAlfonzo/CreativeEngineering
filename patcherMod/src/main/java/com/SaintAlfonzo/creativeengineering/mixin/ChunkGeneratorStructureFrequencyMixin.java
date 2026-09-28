package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.StructureFrequencies;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Per-structure frequency inside a structure_set (see worldgen/StructureFrequencies).
 *
 * ChunkGenerator#tryGenerateStructure (m_223104_, private) is what the per-set lambda in
 * createStructures calls for each weighted pick: it generates the structure's start and, if valid,
 * stores it with StructureManager#setStartForStructure (m_220516_) and returns true; a false makes the
 * lambda try the next pick. It is the only caller path for structure starts, single-member sets included.
 *
 *  - right before the start is stored: roll the member's frequency. A miss returns false and vetoes
 *    the slot for the rest of that set's picks in this chunk.
 *  - at HEAD: a vetoed slot returns false immediately (no generation work).
 */
@Mixin(value = ChunkGenerator.class, remap = false)
public class ChunkGeneratorStructureFrequencyMixin {

	private static final String TRY_GENERATE = "m_223104_(Lnet/minecraft/world/level/levelgen/structure/StructureSet$StructureSelectionEntry;"
			+ "Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/levelgen/RandomState;"
			+ "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;JLnet/minecraft/world/level/chunk/ChunkAccess;"
			+ "Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/core/SectionPos;)Z";

	@Inject(method = TRY_GENERATE, at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private void creativeengineering$skipVetoedSlot(StructureSet.StructureSelectionEntry entry, StructureManager structureManager,
			RegistryAccess registryAccess, RandomState randomState, StructureTemplateManager templateManager, long seed,
			ChunkAccess chunk, ChunkPos chunkPos, SectionPos sectionPos, CallbackInfoReturnable<Boolean> cir) {
		if (StructureFrequencies.slotVetoed(entry, chunk)) cir.setReturnValue(false);
	}

	@Inject(method = TRY_GENERATE,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/StructureManager;m_220516_(Lnet/minecraft/core/SectionPos;"
					+ "Lnet/minecraft/world/level/levelgen/structure/Structure;Lnet/minecraft/world/level/levelgen/structure/StructureStart;"
					+ "Lnet/minecraft/world/level/chunk/StructureAccess;)V"),
			cancellable = true, require = 1, remap = false)
	private void creativeengineering$rollFrequency(StructureSet.StructureSelectionEntry entry, StructureManager structureManager,
			RegistryAccess registryAccess, RandomState randomState, StructureTemplateManager templateManager, long seed,
			ChunkAccess chunk, ChunkPos chunkPos, SectionPos sectionPos, CallbackInfoReturnable<Boolean> cir) {
		if (StructureFrequencies.rejects(entry, seed, chunkPos, chunk)) cir.setReturnValue(false);
	}
}
