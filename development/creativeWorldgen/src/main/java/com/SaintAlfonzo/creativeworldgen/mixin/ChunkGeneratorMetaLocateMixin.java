package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.LocateLoads;
import com.SaintAlfonzo.creativeworldgen.worldgen.LocateStats;
import com.SaintAlfonzo.creativeworldgen.worldgen.MetaSlots;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Structure searches (/locate, explorer maps, dolphins, Async Locator) also find structures inside meta structures
 * (worldgen/MetaSlots): a tower beside a village is stored as part of the meta's start, not as a tower start.
 *
 *  - findNearestMapStructure (m_223037_) HEAD: when a meta can contain an asked-for structure, the search runs
 *    again with those metas added (their placements join the search).
 *  - getStructureGeneratingAt (m_223198_, private static) HEAD, only during such a search: vanilla's check for
 *    the asked-for structures; for a meta, its start in that chunk and the position of the asked-for part.
 *
 * Also the search counters and chunk loads (worldgen/LocateStats, LocateLoads; feature_writeup/locate.md 3.2, 3.4):
 * every search counts its candidates, loads and misses, and a miss's chunk load doesn't block the server thread.
 * A search past its deadline (or stopped) gets "nothing here" for every further candidate, so vanilla's ring walk
 * runs out in milliseconds and the search ends as not found (structure_tuning.md 7.3.2).
 */
@Mixin(value = ChunkGenerator.class, remap = false)
public abstract class ChunkGeneratorMetaLocateMixin {

	@Shadow(remap = false)
	public abstract Pair<BlockPos, Holder<Structure>> m_223037_(ServerLevel level, HolderSet<Structure> structures, BlockPos pos, int radius, boolean skipKnown);

	@Inject(method = "m_223037_(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/HolderSet;Lnet/minecraft/core/BlockPos;IZ)"
			+ "Lcom/mojang/datafixers/util/Pair;", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private void creativeengineering$searchMetaParts(ServerLevel level, HolderSet<Structure> structures, BlockPos pos, int radius, boolean skipKnown,
			CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir) {
		LocateStats.begin(structures, pos);
		HolderSet<Structure> expanded = MetaSlots.beginLocate(level, structures);
		if (expanded == null) return;   // the RETURN handler ends the search
		Pair<BlockPos, Holder<Structure>> result = null;
		try {
			result = m_223037_(level, expanded, pos, radius, skipKnown);
			cir.setReturnValue(result);
		} finally {
			MetaSlots.endLocate();
			LocateStats.end(result);
		}
	}

	@Inject(method = "m_223037_(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/HolderSet;Lnet/minecraft/core/BlockPos;IZ)"
			+ "Lcom/mojang/datafixers/util/Pair;", at = @At("RETURN"), require = 1, remap = false)
	private void creativeengineering$endSearch(ServerLevel level, HolderSet<Structure> structures, BlockPos pos, int radius, boolean skipKnown,
			CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir) {
		LocateStats.end(cir.getReturnValue());
	}

	@Inject(method = "m_223198_(Ljava/util/Set;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/level/StructureManager;Z"
			+ "Lnet/minecraft/world/level/levelgen/structure/placement/StructurePlacement;Lnet/minecraft/world/level/ChunkPos;)"
			+ "Lcom/mojang/datafixers/util/Pair;", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private static void creativeengineering$metaPartAt(Set<Holder<Structure>> structures, LevelReader level, StructureManager manager, boolean skipKnown,
			StructurePlacement placement, ChunkPos pos, CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir) {
		if (LocateStats.expired()) {
			cir.setReturnValue(null);   // not counted as a candidate; the RETURN handler doesn't run
			return;
		}
		LocateStats.beginCall();
		if (!MetaSlots.locating()) return;   // the RETURN handler closes the call
		Pair<BlockPos, Holder<Structure>> result = MetaSlots.locateAt(structures, level, manager, skipKnown, placement, pos);
		LocateStats.endCall(result);
		cir.setReturnValue(result);
	}

	@Inject(method = "m_223198_(Ljava/util/Set;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/level/StructureManager;Z"
			+ "Lnet/minecraft/world/level/levelgen/structure/placement/StructurePlacement;Lnet/minecraft/world/level/ChunkPos;)"
			+ "Lcom/mojang/datafixers/util/Pair;", at = @At("RETURN"), require = 1, remap = false)
	private static void creativeengineering$endCandidate(Set<Holder<Structure>> structures, LevelReader level, StructureManager manager, boolean skipKnown,
			StructurePlacement placement, ChunkPos pos, CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir) {
		LocateStats.endCall(cir.getReturnValue());
	}

	@Redirect(method = "m_223198_(Ljava/util/Set;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/level/StructureManager;Z"
			+ "Lnet/minecraft/world/level/levelgen/structure/placement/StructurePlacement;Lnet/minecraft/world/level/ChunkPos;)"
			+ "Lcom/mojang/datafixers/util/Pair;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;m_46819_(IILnet/minecraft/world/level/chunk/ChunkStatus;)"
					+ "Lnet/minecraft/world/level/chunk/ChunkAccess;"), require = 1, remap = false)
	private static ChunkAccess creativeengineering$loadCandidate(LevelReader level, int x, int z, ChunkStatus status) {
		return LocateLoads.structureStarts(level, x, z);
	}
}
