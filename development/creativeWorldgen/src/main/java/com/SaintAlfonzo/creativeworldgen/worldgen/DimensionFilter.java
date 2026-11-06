package com.SaintAlfonzo.creativeworldgen.worldgen;

import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Structures that cannot start in a dimension are not tried there (feature_writeup/perf.md 10.7 option 1).
 *
 * 1.19.2 runs every structure set in every noise dimension and tests the biome only after
 * Structure#findGenerationPoint, so a Nether or End structure tried in the Overworld still pays for its generation
 * point (Twigs' obelisk reads 121 base columns), its jigsaw assembly and our earth-fill reads before it fails.
 * ChunkGeneratorStructureFrequencyMixin asks {@link #impossible} at the HEAD of tryGenerateStructure, after the meta
 * slot decision, and returns false (the pick failed) when no biome the generator's biome source can produce passes
 * the structure's biome test. The biome rules and the meta-slot replacement narrowing only narrow that test, so
 * everything skipped would have failed anyway: the same starts generate either way. The pick's randoms are rolled by
 * the caller, so skipping costs no random draws.
 *
 * Meta structures are left alone (their biomes are their anchor's, and they have their own placement).
 * Cached per biome source and structure; cleared when tags change (biome tags) and when the server stops.
 */
public final class DimensionFilter {

	private static final Map<BiomeSource, Map<Structure, Boolean>> IMPOSSIBLE = new ConcurrentHashMap<>();

	private DimensionFilter() {}

	/** Whether {@code structure} can never start in the generator's dimension (no biome there passes its biome test). */
	public static boolean impossible(ChunkGenerator generator, Structure structure) {
		if (!WorldgenConfig.dimensionFilterEnabled() || structure instanceof MetaStructure) return false;
		BiomeSource source = generator.getBiomeSource();
		return IMPOSSIBLE.computeIfAbsent(source, k -> new ConcurrentHashMap<>())
				.computeIfAbsent(structure, s -> !MetaSlots.canSpawnAmong(s, source.possibleBiomes()));
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) { IMPOSSIBLE.clear(); }

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) { IMPOSSIBLE.clear(); }
}
