package com.SaintAlfonzo.creativeengineering.worldgen;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CEConfig.GroundMode;

import java.util.List;
import java.util.function.Supplier;

/**
 * Read access to CEConfig [worldgen] for the worldgen code and the KubeJS worldgen scripts. Every getter
 * falls back to the default when the config isn't loaded (headless tests, or a read before Forge loads the
 * file), so a worldgen path never throws over a config read. Plain static methods: KubeJS calls them as
 * {@code WorldgenConfig.structureDensity()} (no fields, so no Rhino field/method name clash).
 */
public final class WorldgenConfig {

	private WorldgenConfig() {}

	static <T> T get(Supplier<T> value, T fallback) {
		try {
			T v = value.get();
			return v == null ? fallback : v;
		} catch (RuntimeException | LinkageError e) {
			return fallback;
		}
	}

	private static CEConfig.Worldgen wg() { return CEConfig.WORLDGEN; }

	// ── systems ──────────────────────────────────────────────────────────────
	public static boolean structureGroupsEnabled() { return get(() -> wg().systems.structureGroups.get(), true); }
	public static boolean structureFrequenciesEnabled() { return get(() -> wg().systems.structureFrequencies.get(), true); }
	public static boolean structureBiomeRulesEnabled() { return get(() -> wg().systems.structureBiomeRules.get(), true); }
	public static boolean structureBiomeFitsEnabled() { return get(() -> wg().systems.structureBiomeFits.get(), true); }
	public static boolean poolTuningEnabled() { return get(() -> wg().systems.poolTuning.get(), true); }
	public static boolean lootContainerSwapsEnabled() { return get(() -> wg().systems.lootContainerSwaps.get(), true); }
	public static boolean biomeLayoutEnabled() { return get(() -> wg().systems.biomeLayout.get(), true); }
	public static boolean deadPlacementFilterEnabled() { return get(() -> wg().systems.deadPlacementFilter.get(), true); }
	public static boolean groundMatchEnabled() { return get(() -> wg().systems.groundMatch.get(), true); }
	public static boolean earthFillEnabled() { return get(() -> wg().systems.earthFill.get(), true); }
	public static boolean waterFillEnabled() { return get(() -> wg().systems.waterFill.get(), true); }
	public static boolean terrainAdaptationEnabled() { return get(() -> wg().systems.terrainAdaptation.get(), true); }
	public static boolean templateTrimEnabled() { return get(() -> wg().systems.templateTrim.get(), true); }
	public static boolean metaStructuresEnabled() { return get(() -> wg().systems.metaStructures.get(), true); }
	public static boolean structureReplacementsEnabled() { return get(() -> wg().systems.structureReplacements.get(), true); }
	public static boolean wildFlowersEnabled() { return get(() -> wg().systems.wildFlowers.get(), true); }
	public static boolean mobSpawnsEnabled() { return get(() -> wg().systems.mobSpawns.get(), true); }
	public static boolean beeNestsEnabled() { return get(() -> wg().systems.beeNests.get(), true); }
	public static boolean mushroomSpawnsEnabled() { return get(() -> wg().systems.mushroomSpawns.get(), true); }
	public static boolean permafrostMergeEnabled() { return get(() -> wg().systems.permafrostMerge.get(), true); }
	public static boolean templateBlockSwapsEnabled() { return get(() -> wg().systems.templateBlockSwaps.get(), true); }
	public static boolean spawnBoxesEnabled() { return get(() -> wg().systems.spawnBoxes.get(), true); }
	public static java.util.List<? extends String> removeSpawners() { return get(() -> wg().systems.removeSpawners.get(), java.util.List.of("minecraft:pillager")); }
	public static boolean oceanGeodesEnabled() { return get(() -> wg().systems.oceanGeodes.get(), true); }
	public static boolean placementPriorityEnabled() { return get(() -> wg().systems.placementPriority.get(), true); }
	public static boolean mulberrySaplingEnabled() { return get(() -> wg().systems.mulberrySapling.get(), true); }
	public static boolean firstTickWaterEnabled() { return get(() -> wg().systems.firstTickWater.get(), true); }
	public static boolean holdGeneratedFallingBlocksEnabled() { return get(() -> wg().systems.holdGeneratedFallingBlocks.get(), true); }
	public static boolean dimensionFilterEnabled() { return get(() -> wg().systems.dimensionFilter.get(), true); }
	public static boolean sharedTerrainCacheEnabled() { return get(() -> wg().systems.sharedTerrainCache.get(), true); }
	public static long firstTickWaterWindowTicks() { return get(() -> wg().firstTickWater.windowTicks.get(), 600); }
	public static boolean placementPriorityLog() { return get(() -> wg().placementPriority.log.get(), false); }

	// ── locate ───────────────────────────────────────────────────────────────
	public static boolean locateFoldPlacement() { return get(() -> wg().locate.foldPlacement.get(), true); }
	public static boolean locateFoldBiomes() { return get(() -> wg().locate.foldBiomes.get(), true); }
	public static boolean locateNonBlockingLoads() { return get(() -> wg().locate.nonBlockingLoads.get(), true); }
	public static boolean locateThreadSafeStructureCheck() { return get(() -> wg().locate.threadSafeStructureCheck.get(), true); }
	public static int locateLogMissesAtLeast() { return get(() -> wg().locate.logMissesAtLeast.get(), 8); }
	public static int locateBatchRadius() { return get(() -> wg().locate.batchRadius.get(), 100); }
	public static int locateBatchTimeoutSeconds() { return get(() -> wg().locate.batchTimeoutSeconds.get(), 120); }
	public static int locateCensusRegionChunks() { return get(() -> wg().locate.censusRegionChunks.get(), 32); }
	public static int locateLoadTimeoutSeconds() { return get(() -> wg().locate.loadTimeoutSeconds.get(), 300); }
	public static int locateSearchTimeoutSeconds() { return get(() -> wg().locate.searchTimeoutSeconds.get(), 300); }

	// ── distant (Distant Horizons generation) ────────────────────────────────
	public static boolean distantClip() { return get(() -> wg().distant.clip.get(), true); }
	public static int distantClipBelowY() { return get(() -> wg().distant.clipBelowY.get(), 48); }
	public static int distantOceanFloorY() { return get(() -> wg().distant.oceanFloorY.get(), 48); }
	public static int distantDeepOceanFloorY() { return get(() -> wg().distant.deepOceanFloorY.get(), 8); }
	public static int distantFloorVariation() { return get(() -> wg().distant.floorVariation.get(), 4); }
	public static int distantFloorScale() { return get(() -> wg().distant.floorScale.get(), 24); }
	public static boolean distantSkipFeatures() { return get(() -> wg().distant.skipFeatures.get(), true); }

	// ── meta structures ──────────────────────────────────────────────────────
	public static boolean metaStructuresLog() { return get(() -> wg().metaStructures.log.get(), false); }
	public static boolean metaAvoidCoasts() { return get(() -> wg().metaStructures.avoidCoasts.get(), true); }
	public static boolean metaKeepShortParts() { return get(() -> wg().metaStructures.keepShortParts.get(), true); }

	// ── ocean geodes ─────────────────────────────────────────────────────────
	public static int oceanGeodeLowestFloorY() { return get(() -> wg().oceanGeodes.lowestFloorY.get(), -8); }
	public static int oceanGeodeClearance() { return get(() -> wg().oceanGeodes.clearance.get(), 2); }
	public static int oceanGeodeSpread() { return get(() -> wg().oceanGeodes.spread.get(), 8); }
	public static double oceanGeodeStayShare() { return get(() -> wg().oceanGeodes.stayShare.get(), 0.1); }
	public static CEConfig.GeodeFloor oceanGeodeFloorPatch() { return get(() -> wg().oceanGeodes.floorPatch.get(), CEConfig.GeodeFloor.SWAP); }
	public static java.util.List<? extends String> oceanGeodeUntouched() {
		return get(() -> wg().oceanGeodes.untouched.get(), java.util.List.of("geode:budding_prismarine"));
	}

	// ── debug ────────────────────────────────────────────────────────────────
	public static double structureDensity() { return get(() -> wg().debug.structureDensity.get(), 1.0); }
	public static boolean testOverrides() { return get(() -> wg().debug.testOverrides.get(), false); }

	// ── solvers ──────────────────────────────────────────────────────────────
	public static int poolMaxFractionScale() { return get(() -> wg().pools.maxFractionScale.get(), 10); }
	public static boolean poolCanonicalOrder() { return get(() -> wg().pools.canonicalOrder.get(), true); }
	public static int biomeMaxCandidates() { return get(() -> wg().biomeSolver.maxCandidates.get(), 24); }
	public static int biomeRounds() { return get(() -> wg().biomeSolver.rounds.get(), 20); }

	// ── ground matching ──────────────────────────────────────────────────────
	public static GroundMode groundMode() { return get(() -> wg().groundMatch.mode.get(), GroundMode.TERRAIN); }
	public static int groundBaseLayers() { return get(() -> wg().groundMatch.baseLayers.get(), 3); }
	public static double groundNativeShare() { return get(() -> wg().groundMatch.nativeShare.get(), 0.35); }
	public static int groundMinSamples() { return get(() -> wg().groundMatch.minSamples.get(), 16); }
	public static List<? extends String> groundFamilies() { return get(() -> wg().groundMatch.families.get(), List.of("creativeengineering:ground_match/grass")); }
	public static boolean groundLog() { return get(() -> wg().groundMatch.log.get(), false); }
	public static int groundCenterStride() { return get(() -> wg().groundMatch.centerStride.get(), 2); }
	public static int groundNeighbourStride() { return get(() -> wg().groundMatch.neighbourStride.get(), 4); }
	public static int groundSubDepth() { return get(() -> wg().groundMatch.subDepth.get(), 3); }
	public static int groundMaxDig() { return get(() -> wg().groundMatch.maxDig.get(), 24); }
	public static int groundMaxPlantHeight() { return get(() -> wg().groundMatch.maxPlantHeight.get(), 4); }

	// ── earth fill ───────────────────────────────────────────────────────────
	public static int earthMaxOverWater() { return get(() -> wg().earthFill.maxOverWater.get(), 24); }
	public static double earthMaxWaterSurplus() { return get(() -> wg().earthFill.maxWaterSurplus.get(), 0.0); }
	public static boolean earthFootprintAllPieces() { return get(() -> wg().earthFill.footprintAllPieces.get(), true); }
	public static int earthBeardReach() { return get(() -> wg().earthFill.beardReach.get(), 6); }
	public static int earthBuryReach() { return get(() -> wg().earthFill.buryReach.get(), 10); }
	public static int earthMaxMoundHeight() { return get(() -> wg().earthFill.maxMoundHeight.get(), 8); }
	public static boolean earthBeardFootprint() { return get(() -> wg().earthFill.beardFootprint.get(), false); }
	public static boolean earthPrune() { return get(() -> wg().earthFill.prune.get(), true); }
	public static int earthPruneMaxHanging() { return get(() -> wg().earthFill.pruneMaxHanging.get(), 8); }
	public static int earthPruneMaxMound() { return get(() -> wg().earthFill.pruneMaxMound.get(), 4); }
	public static int earthPruneMinWetColumns() { return get(() -> wg().earthFill.pruneMinWetColumns.get(), 16); }
	public static boolean earthPruneWetJigsaws() { return get(() -> wg().earthFill.pruneWetJigsaws.get(), true); }
	public static int earthFootprintMinWater() { return get(() -> wg().earthFill.footprintMinWater.get(), 9); }
	public static List<? extends Integer> earthSizeBounds() { return get(() -> wg().earthFill.sizes.bounds.get(), List.of(150, 600, 2000)); }
	/** [maxOverAir, maxMound] of each size, smallest first. */
	public static List<List<? extends Integer>> earthSizeLimits() {
		return List.of(get(() -> wg().earthFill.sizes.small.get(), List.of(24, 8)), get(() -> wg().earthFill.sizes.medium.get(), List.of(64, 24)),
				get(() -> wg().earthFill.sizes.large.get(), List.of(160, 64)), get(() -> wg().earthFill.sizes.huge.get(), List.of(320, 128)));
	}
	public static int earthBaseLayers() { return get(() -> wg().earthFill.baseLayers.get(), 3); }
	public static List<? extends String> earthBlocks() { return get(() -> wg().earthFill.blocks.get(), List.of("creativeengineering:earth_fill/earth")); }
	public static List<? extends String> earthOverrides() { return get(() -> wg().earthFill.overrides.get(), List.<String>of()); }
	public static int earthSampleGrid() { return get(() -> wg().earthFill.sampleGrid.get(), 4); }
	public static CEConfig.EarthFillLog earthLog() { return get(() -> wg().earthFill.log.get(), CEConfig.EarthFillLog.CANCELLED); }

	// ── water fill ───────────────────────────────────────────────────────────
	public static int waterMaxIntoGround() { return get(() -> wg().waterFill.maxIntoGround.get(), 32); }
	public static int waterMaxIntoAir() { return get(() -> wg().waterFill.maxIntoAir.get(), 16); }
	public static List<? extends String> waterOverrides() { return get(() -> wg().waterFill.overrides.get(), List.of("#creativeengineering:water_fill/ponds=-1,16")); }
	public static CEConfig.EarthFillLog waterLog() { return get(() -> wg().waterFill.log.get(), CEConfig.EarthFillLog.CANCELLED); }

	// ── terrain adaptation ───────────────────────────────────────────────────
	public static int softSkirtBump() { return get(() -> wg().terrainAdaptation.skirtBump.get(), 0); }

}
