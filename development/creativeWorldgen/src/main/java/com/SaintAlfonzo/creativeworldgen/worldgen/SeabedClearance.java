package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CreativeWorldgen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Underground structures kept clear of the seabed (TODO structures/terrain #7).
 *
 * Terralith's cave structures (absolute y -40..40) and Subterrestrial's cabins (y 6..48) pick a fixed height, not one
 * below the terrain. Under an ocean or a deep lake the floor sits at y 30 to 50, so the top of such a start can reach
 * the seabed. Their beard (beard_box / beard_thin) then carves the piece box and up to about 11 blocks above the piece
 * bottom into air, the floor over the structure is gone, and the sea floods it (seen with Subterrestrial's jungle
 * cabin under an ocean).
 *
 * A structure in the structure tag {@code creativeengineering:seabed_clearance} (kubejs/server_scripts/worldgen/
 * terrain_adaptation.js) is dropped when, at the centre or a corner of its box widened by {@link #REACH}, the ground
 * (OCEAN_FLOOR_WG) is no more than {@link #CLEARANCE} blocks above the box top and a fluid lies on that ground
 * (WORLD_SURFACE_WG above OCEAN_FLOOR_WG). Starts deeper under the seabed, and every start under dry land, are kept.
 *
 * HOOK: mixin/ChunkGeneratorSeabedClearanceMixin, on the generated start of ChunkGenerator#tryGenerateStructure
 * (slot 15, the same store the surface check and earth fill use): INVALID_START fails the pick the way a wrong-biome
 * start does. Counted in /celocate stats under SURFACE_CHECK. The noise terrain is read only for tagged structures,
 * and the floor first: a deep start costs one column read per point. Drops are counted per structure and logged at
 * server stop ({@code [seabed-clearance] dropped N starts: ...}); each drop is also a debug line.
 */
public final class SeabedClearance {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/seabed-clearance");

	public static final TagKey<Structure> TAG = TagKey.create(Registry.STRUCTURE_REGISTRY,
			new ResourceLocation(CreativeWorldgen.NS, "seabed_clearance"));

	/** Solid blocks wanted between the box top and the seabed: covers the beard's carve above a 5-block piece. */
	static final int CLEARANCE = 8;
	/** The box is widened by this much for the sample points (the beard's skirt). */
	static final int REACH = 4;

	/** Structure id -> starts dropped since the server started. */
	private static final Map<String, LongAdder> DROPPED = new ConcurrentHashMap<>();

	private SeabedClearance() {}

	/** The generated start, or INVALID_START when a tagged structure's box reaches a water-covered floor. */
	public static StructureStart check(StructureStart start, Holder<Structure> structure, ChunkGenerator generator, RandomState random,
			LevelHeightAccessor height) {
		if (!start.isValid() || !structure.is(TAG)) return start;
		BoundingBox b = start.getBoundingBox();
		int top = b.maxY() + CLEARANCE;
		int[][] points = { { (b.minX() + b.maxX()) / 2, (b.minZ() + b.maxZ()) / 2 },
				{ b.minX() - REACH, b.minZ() - REACH }, { b.minX() - REACH, b.maxZ() + REACH },
				{ b.maxX() + REACH, b.minZ() - REACH }, { b.maxX() + REACH, b.maxZ() + REACH } };
		for (int[] p : points) {
			int floor = generator.getBaseHeight(p[0], p[1], Heightmap.Types.OCEAN_FLOOR_WG, height, random);
			if (floor > top) continue;
			int surface = generator.getBaseHeight(p[0], p[1], Heightmap.Types.WORLD_SURFACE_WG, height, random);
			if (surface > floor) {
				DROPPED.computeIfAbsent(structure.unwrapKey().map(k -> k.location().toString()).orElse("?"), k -> new LongAdder()).increment();
				LOG.debug("[seabed-clearance] dropped {} at {} {}: box top y {}, seabed y {} under water to y {}",
						structure.unwrapKey().map(k -> k.location().toString()).orElse("?"), p[0], p[1], b.maxY(), floor, surface);
				return StructureStart.INVALID_START;
			}
		}
		return start;
	}

	/** World load line: how many structures the tag holds (the tag is filled by terrain_adaptation.js). */
	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		event.getServer().registryAccess().registry(Registry.STRUCTURE_REGISTRY).ifPresent(r -> {
			int n = r.getTag(TAG).map(t -> t.size()).orElse(0);
			LOG.info("[seabed-clearance] world load: {} structures tagged (clearance {}, reach {})", n, CLEARANCE, REACH);
		});
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		Map<String, Long> counts = new TreeMap<>();
		DROPPED.forEach((k, v) -> counts.put(k, v.sum()));
		DROPPED.clear();
		long total = counts.values().stream().mapToLong(Long::longValue).sum();
		LOG.info("[seabed-clearance] dropped {} starts: {}", total, counts);
	}
}
