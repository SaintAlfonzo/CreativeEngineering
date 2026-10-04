package com.SaintAlfonzo.creativeengineering.worldgen;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CEConfig.GroundMode;
import com.SaintAlfonzo.creativeengineering.CEConfig.VerticalAlign;

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
	public static boolean poolTuningEnabled() { return get(() -> wg().systems.poolTuning.get(), true); }
	public static boolean lootContainerSwapsEnabled() { return get(() -> wg().systems.lootContainerSwaps.get(), true); }
	public static boolean biomeLayoutEnabled() { return get(() -> wg().systems.biomeLayout.get(), true); }
	public static boolean biomeMergesEnabled() { return get(() -> wg().systems.biomeMerges.get(), true); }
	public static boolean deadPlacementFilterEnabled() { return get(() -> wg().systems.deadPlacementFilter.get(), true); }
	public static boolean groundMatchEnabled() { return get(() -> wg().systems.groundMatch.get(), true); }
	public static boolean earthFillEnabled() { return get(() -> wg().systems.earthFill.get(), true); }
	public static boolean wildCropsEnabled() { return get(() -> wg().systems.wildCrops.get(), true); }

	// ── debug ────────────────────────────────────────────────────────────────
	public static double structureDensity() { return get(() -> wg().debug.structureDensity.get(), 1.0); }

	// ── solvers ──────────────────────────────────────────────────────────────
	public static int poolMaxFractionScale() { return get(() -> wg().pools.maxFractionScale.get(), 10); }
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
	public static int earthMaxOverAir() { return get(() -> wg().earthFill.maxOverAir.get(), 128); }
	public static int earthBaseLayers() { return get(() -> wg().earthFill.baseLayers.get(), 3); }
	public static List<? extends String> earthBlocks() { return get(() -> wg().earthFill.blocks.get(), List.of("creativeengineering:earth_fill/earth")); }
	public static List<? extends String> earthOverrides() { return get(() -> wg().earthFill.overrides.get(), List.<String>of()); }
	public static int earthSampleGrid() { return get(() -> wg().earthFill.sampleGrid.get(), 2); }
	public static CEConfig.EarthFillLog earthLog() { return get(() -> wg().earthFill.log.get(), CEConfig.EarthFillLog.CANCELLED); }

	// ── /ceplace ─────────────────────────────────────────────────────────────
	public static int placeGap() { return get(() -> wg().massPlace.gap.get(), 12); }
	public static int placeRowGap() { return get(() -> wg().massPlace.rowGap.get(), 16); }
	public static boolean placeLabels() { return get(() -> wg().massPlace.labels.get(), true); }
	public static VerticalAlign placeVerticalAlign() { return get(() -> wg().massPlace.verticalAlign.get(), VerticalAlign.AUTO); }
	public static int placeAutoAlignBand() { return get(() -> wg().massPlace.autoAlignBand.get(), 8); }
	public static int placeMaxItems() { return get(() -> wg().massPlace.maxItems.get(), 1000); }
	public static int placeTickBudgetMs() { return get(() -> wg().massPlace.tickBudgetMs.get(), 40); }
}
