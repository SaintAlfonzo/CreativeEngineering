package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.LocateStats;
import com.SaintAlfonzo.creativeengineering.worldgen.PlacementPriority;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Placement priority (worldgen/PlacementPriority).
 *
 *  - tryGenerateStructure (m_223104_) made callable, so a prediction replays a set's pick on a scratch chunk.
 *  - The generated StructureStart (local slot 15, as in ChunkGeneratorEarthFillMixin) becomes INVALID_START when a
 *    higher-ranked structure is predicted to overlap it; the pick then fails like a wrong-biome start.
 *  - tryGenerateStructure HEAD: remember the call, so the jigsaw shaping (JigsawPlacementPriorityMixin) knows whose
 *    pieces it is fitting.
 *  - createStructures (m_223164_) RETURN: companion starts (villages around a fortified town) for this chunk.
 */
@Mixin(value = ChunkGenerator.class, remap = false)
public abstract class ChunkGeneratorPlacementPriorityMixin implements PlacementPriority.Generator {

	private static final String TRY_GENERATE = "m_223104_(Lnet/minecraft/world/level/levelgen/structure/StructureSet$StructureSelectionEntry;"
			+ "Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/levelgen/RandomState;"
			+ "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;JLnet/minecraft/world/level/chunk/ChunkAccess;"
			+ "Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/core/SectionPos;)Z";

	@Shadow(remap = false)
	private boolean m_223104_(StructureSet.StructureSelectionEntry entry, StructureManager structureManager, RegistryAccess registryAccess,
			RandomState randomState, StructureTemplateManager templateManager, long seed, ChunkAccess chunk, ChunkPos chunkPos, SectionPos sectionPos) {
		throw new AssertionError();
	}

	@Override
	public boolean creativeengineering$tryGenerate(StructureSet.StructureSelectionEntry entry, StructureManager manager, RegistryAccess access,
			RandomState random, StructureTemplateManager templates, long seed, ChunkAccess chunk, ChunkPos pos, SectionPos section) {
		return m_223104_(entry, manager, access, random, templates, seed, chunk, pos, section);
	}

	@Inject(method = TRY_GENERATE, at = @At("HEAD"), require = 1, remap = false)
	private void creativeengineering$rememberCall(StructureSet.StructureSelectionEntry entry, StructureManager structureManager,
			RegistryAccess registryAccess, RandomState randomState, StructureTemplateManager templateManager, long seed,
			ChunkAccess chunk, ChunkPos chunkPos, SectionPos sectionPos, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
		PlacementPriority.current(entry, (ChunkGenerator) (Object) this, structureManager, registryAccess, randomState, templateManager, seed, chunk, chunkPos);
	}

	@ModifyVariable(method = TRY_GENERATE, at = @At("STORE"), index = 15, require = 1, remap = false)
	private StructureStart creativeengineering$placementPriority(StructureStart start, StructureSet.StructureSelectionEntry entry,
			StructureManager structureManager, RegistryAccess registryAccess, RandomState randomState,
			StructureTemplateManager templateManager, long seed, ChunkAccess chunk, ChunkPos chunkPos) {
		StructureStart out = PlacementPriority.filter(start, entry, (ChunkGenerator) (Object) this, structureManager, registryAccess, randomState,
				templateManager, seed, chunk, chunkPos);
		if (out != start && start.isValid() && !out.isValid()) LocateStats.reject(entry.structure().value(), chunkPos, LocateStats.Reason.PRIORITY);
		return out;
	}

	@Inject(method = "m_223164_(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/levelgen/RandomState;"
			+ "Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;"
			+ "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;J)V",
			at = @At("RETURN"), require = 1, remap = false)
	private void creativeengineering$companions(RegistryAccess registryAccess, RandomState randomState, StructureManager structureManager,
			ChunkAccess chunk, StructureTemplateManager templateManager, long seed, CallbackInfo ci) {
		PlacementPriority.companions((ChunkGenerator) (Object) this, registryAccess, randomState, structureManager, chunk, templateManager, seed);
	}
}
