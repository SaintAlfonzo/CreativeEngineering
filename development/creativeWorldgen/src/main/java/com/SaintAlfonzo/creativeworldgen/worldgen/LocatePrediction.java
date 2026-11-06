package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The "could it start here?" prediction of structure searches (StructureCheck#canCreateStructure), with the pack's
 * rules folded in so fewer candidates need a chunk load (feature_writeup/locate.md 3.1). Every stage is exact: it
 * only says "no" where generation would certainly say no, because a wrong "no" makes /locate and maps skip the
 * nearest real structure.
 *
 *  - Stage A (CWGConfig [worldgen.locate] foldPlacement): the structure set's own frequency and exclusion zone,
 *    StructurePlacement#isStructureChunk, the same call createStructures makes. Vanilla's search walks
 *    getPotentialStructureChunk only, so it skips both. A structure in several sets (or converted from anchors in
 *    other sets) is only rejected when no set would try it here.
 *  - Per-member frequency (StructureFrequencies, already in the prediction before this class); a member's biome
 *    factor (a group's biome_frequency) once the generation point is known, in plain().
 *  - Stage B (foldBiomes): the biome at the generation point, with the biome rules and fits, the same test
 *    Structure#generate makes; vanilla's prediction only finds the point. Where biome entries tie, every tied biome
 *    must fail (TiedBiomes), since the lookup depends on the thread's history.
 *  - Meta structures and replacements keep their own prediction (MetaSlots), which already tested biomes.
 *
 * Each "no" is counted per stage for the current search (LocateStats).
 */
public final class LocatePrediction {

	private LocatePrediction() {}

	/** canCreateStructure HEAD (StructureCheckFrequencyMixin): true = certainly no start here. */
	public static boolean rejectsAtHead(StructureCheck check, Structure structure, ChunkPos pos) {
		LocateStats.predicted();
		if (WorldgenConfig.locateFoldPlacement() && placementRejects(check, structure, pos)) {
			LocateStats.predictedNo(LocateStats.Stage.PLACEMENT);
			return true;
		}
		if (StructureFrequencies.predictRejects(check, structure, pos)) {
			LocateStats.predictedNo(LocateStats.Stage.FREQUENCY);
			return true;
		}
		return false;
	}

	/** Stage A: no placement this structure can come from would try it in this chunk. */
	static boolean placementRejects(StructureCheck check, Structure structure, ChunkPos pos) {
		ChunkGenerator generator = LocateReflect.generator(check);
		RandomState randomState = LocateReflect.randomState(check);
		long seed = LocateReflect.seed(check);
		List<StructurePlacement> placements = new ArrayList<>(placementsOf(generator, structure, randomState));
		for (Structure anchor : MetaSlots.anchorsOf(LocateReflect.registryAccess(check), structure)) {
			placements.addAll(placementsOf(generator, anchor, randomState));
		}
		if (placements.isEmpty()) return false;   // nothing known about it: leave it to the chunk load
		for (StructurePlacement p : placements) {
			if (p.isStructureChunk(generator, randomState, seed, pos.x, pos.z)) return false;
		}
		return true;
	}

	private static List<StructurePlacement> placementsOf(ChunkGenerator generator, Structure structure, RandomState randomState) {
		return LocateReflect.placementsFor(generator, Holder.direct(structure), randomState);
	}

	/** The prediction of a structure that is neither a converted meta nor a replacement (MetaSlots). */
	public static Optional<Structure.GenerationStub> plain(Structure structure, Structure.GenerationContext ctx) {
		Optional<Structure.GenerationStub> stub;
		if (WorldgenConfig.locateFoldBiomes()) {
			stub = fits(structure, ctx, b -> true, true);
		} else {
			stub = structure.findGenerationPoint(ctx);
			if (stub.isEmpty()) LocateStats.predictedNo(LocateStats.Stage.POINT);
		}
		return biomeFactor(structure, ctx, stub);
	}

	/**
	 * A member of a group with biome_frequency rolls frequency x the factor of the biome at its generation point
	 * (StructureFrequencies), the same roll generation makes: a miss for every biome the point could be (TiedBiomes)
	 * is a certain "no". Not for the dummy point a
	 * failed prediction returns (no pieces; the chunk load decides).
	 */
	static Optional<Structure.GenerationStub> biomeFactor(Structure structure, Structure.GenerationContext ctx, Optional<Structure.GenerationStub> stub) {
		if (stub.isEmpty() || !StructureFrequencies.hasBiomeFactors(structure)) return stub;
		try {
			BlockPos p = stub.get().position();
			if (!TiedBiomes.any(ctx.chunkGenerator().getBiomeSource(), ctx.randomState().sampler(), QuartPos.fromBlock(p.getX()),
					QuartPos.fromBlock(p.getY()), QuartPos.fromBlock(p.getZ()),
					b -> !StructureFrequencies.factorRejects(structure, ctx.seed(), ctx.chunkPos(), b))) {
				LocateStats.predictedNo(LocateStats.Stage.FREQUENCY);
				return Optional.empty();
			}
		} catch (RuntimeException e) {
			// leave it to the chunk load
		}
		return stub;
	}

	/**
	 * Stage B: the structure's generation point, if that lies in its biomes (with the biome rules) and in
	 * {@code also}. Empty if not; a dummy point at the chunk when the prediction throws (the chunk load decides).
	 * {@code count}: record a "no" for the current search (false for MetaSlots' per-anchor tries).
	 */
	public static Optional<Structure.GenerationStub> fits(Structure s, Structure.GenerationContext ctx, Predicate<Holder<Biome>> also,
			boolean count) {
		Predicate<Holder<Biome>> biomes = StructureBiomeRules.filter(s, s.biomes()::contains).and(also);
		Structure.GenerationContext sub = new Structure.GenerationContext(ctx.registryAccess(), ctx.chunkGenerator(), ctx.biomeSource(), ctx.randomState(),
				ctx.structureTemplateManager(), ctx.seed(), ctx.chunkPos(), ctx.heightAccessor(), biomes);
		try {
			Optional<Structure.GenerationStub> stub = s.findGenerationPoint(sub);
			if (stub.isEmpty()) {
				if (count) LocateStats.predictedNo(LocateStats.Stage.POINT);
				return stub;
			}
			BlockPos p = stub.get().position();
			if (TiedBiomes.any(ctx.chunkGenerator().getBiomeSource(), ctx.randomState().sampler(), QuartPos.fromBlock(p.getX()),
					QuartPos.fromBlock(p.getY()), QuartPos.fromBlock(p.getZ()), biomes)) return stub;
			if (count) LocateStats.predictedNo(LocateStats.Stage.BIOME);
			return Optional.empty();
		} catch (RuntimeException e) {
			return Optional.of(dummy(ctx.chunkPos()));
		}
	}

	/** "Maybe here": a point at the chunk with no pieces; the chunk load that follows decides. */
	static Structure.GenerationStub dummy(ChunkPos pos) {
		return new Structure.GenerationStub(pos.getWorldPosition(), Either.<Consumer<StructurePiecesBuilder>, StructurePiecesBuilder>left(b -> {}));
	}
}
