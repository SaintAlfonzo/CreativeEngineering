package com.SaintAlfonzo.creativeworldgen.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Sky Structures' floating islands placed above the ground and the sea, one per hit (TODO structures/terrain #5).
 *
 * Sky Structures 1.0.0 is not a structure mod: its 9 islands (grass_island, the oak / spruce / birch / acacia / jungle /
 * dark oak / bees variants and the blacksmith island) are features added to every biome at raw_generation
 * (forge:add_features, forge:any). Each feature, per chunk, rolls 150 in 1,000,000 (blacksmith 130), then places its
 * template 1 or 2 times, each at a random spot of the chunk at OCEAN_FLOOR_WG + 16..79. Two faults:
 * <ul>
 *   <li>OCEAN_FLOOR_WG is the seabed: over an ocean (floor y 30 to 45) the island lands at y 46 to 124, often under the
 *       sea surface (y 63), and floods.</li>
 *   <li>The second placement in the same chunk puts a second copy within 16 blocks of the first: two islands stacked
 *       or overlapping.</li>
 * </ul>
 * mixin/ConfiguredFeatureSkyIslandsMixin answers ConfiguredFeature#place for any feature registered under
 * {@code sky_structures} with {@link #place}: the same roll, dimension check (Overworld only), template, mirror,
 * rotation and processor as the mod, but one island per hit, and its height measured from the highest of the ground and
 * any water under its footprint (centre and corners), never below sea level. The height band (16 + 0..63 above that)
 * is the mod's; TODO structures/sky #3 is where it changes.
 *
 * {@code /place feature sky_structures:<island>} skips the 150-in-a-million roll (the level is not a WorldGenRegion
 * there), so one command places one island. Islands placed are counted per template and by base (sea or land) and
 * logged at server stop ({@code [sky-islands] placed N: ...}).
 */
public final class SkyIslands {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/sky-islands");

	public static final String NAMESPACE = "sky_structures";
	/** The mod's band: 16 + nextInt(64) above the base. */
	static final int MIN_GAP = 16;
	static final int GAP_SPREAD = 64;

	record Island(ResourceLocation template, int chance) {}

	/** Feature -> its island, or empty for every other feature. Features are registry singletons (identity keys). */
	private static final Map<Feature<?>, Optional<Island>> ISLANDS = new ConcurrentHashMap<>();

	/** "template over sea|land" -> islands placed since the server started. */
	private static final Map<String, LongAdder> PLACED = new ConcurrentHashMap<>();

	private SkyIslands() {}

	static Optional<Island> island(Feature<?> feature) {
		Optional<Island> known = ISLANDS.get(feature);
		if (known != null) return known;
		return ISLANDS.computeIfAbsent(feature, f -> {
			ResourceLocation key = ForgeRegistries.FEATURES.getKey(f);
			if (key == null || !NAMESPACE.equals(key.getNamespace())) return Optional.empty();
			// The blacksmith island's feature and template names differ; the others share the name.
			boolean smith = key.getPath().equals("grass_island_blacksmith");
			return Optional.of(new Island(new ResourceLocation(NAMESPACE, smith ? "sky_grass_blacksmith" : key.getPath()), smith ? 130 : 150));
		});
	}

	/** ConfiguredFeature#place for a Sky Structures feature: whether an island was placed; null for any other feature. */
	public static Boolean place(Feature<?> feature, WorldGenLevel level, RandomSource random, BlockPos origin) {
		Optional<Island> found = island(feature);
		if (found.isEmpty()) return null;
		Island island = found.get();
		ServerLevel server = level.getLevel();
		if (server.dimension() != Level.OVERWORLD) return false;
		if (level instanceof WorldGenRegion && random.nextInt(1_000_000) + 1 > island.chance()) return false;
		StructureTemplate template = server.getStructureManager().getOrCreate(island.template());
		int x = origin.getX() + random.nextInt(16);
		int z = origin.getZ() + random.nextInt(16);
		StructurePlaceSettings settings = new StructurePlaceSettings()
				.setMirror(Mirror.values()[random.nextInt(2)])
				.setRotation(Rotation.values()[random.nextInt(3)])
				.setRandom(random)
				.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
				.setIgnoreEntities(false);
		BoundingBox box = template.getBoundingBox(settings, new BlockPos(x, 0, z));
		// WORLD_SURFACE_WG counts water, so the base is the sea surface over water and the ground on land.
		int base = server.getSeaLevel();
		boolean overWater = false;
		int[][] points = { { (box.minX() + box.maxX()) / 2, (box.minZ() + box.maxZ()) / 2 }, { box.minX(), box.minZ() },
				{ box.minX(), box.maxZ() }, { box.maxX(), box.minZ() }, { box.maxX(), box.maxZ() } };
		for (int[] p : points) {
			int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, p[0], p[1]);
			if (surface > level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, p[0], p[1])) overWater = true;
			base = Math.max(base, surface);
		}
		int y = base + MIN_GAP + random.nextInt(GAP_SPREAD);
		if (y + box.getYSpan() >= level.getMaxBuildHeight()) return false;
		BlockPos pos = new BlockPos(x, y, z);
		boolean placed = template.placeInWorld(level, pos, pos, settings, random, 2);
		if (placed) PLACED.computeIfAbsent(island.template().getPath() + (overWater ? " over water" : " over land"), k -> new LongAdder()).increment();
		if (placed) LOG.debug("[sky-islands] {} at {} {} {} (base y {})", island.template(), x, y, z, base);
		return placed;
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		Map<String, Long> counts = new TreeMap<>();
		PLACED.forEach((k, v) -> counts.put(k, v.sum()));
		PLACED.clear();
		long total = counts.values().stream().mapToLong(Long::longValue).sum();
		LOG.info("[sky-islands] placed {}: {}", total, counts);
	}
}
