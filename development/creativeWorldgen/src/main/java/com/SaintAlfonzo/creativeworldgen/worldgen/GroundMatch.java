package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CWGConfig.GroundMode;
import com.SaintAlfonzo.creativeworldgen.CreativeWorldgen;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 * Swaps a structure's grass/dirt base for the ground that is really around it. Structures are
 * built on the biome they were designed for (Philip's ruins and towers sit on a grass + dirt
 * pad), and look pasted on anywhere else: grass on volcanic peaks, dirt in a desert.
 *
 * <p>Runs on every template placement that belongs to a structure (mixin/StructureTemplateGroundMatchMixin,
 * on Forge's processBlockInfos, the same hook as {@link LootContainerSwaps}; mixin/StructureStartGroundMatchMixin
 * records which structure start is placing): world generation and /place structure. Structure blocks and
 * /place template are left alone. For the chunk being placed it samples the natural ground: the top solid block and the three below it, on a grid over
 * this chunk and its 8 neighbours, skipping every column where a structure piece reaches the sampled ground's
 * height (a piece deep below or high above that ground leaves it natural). A base family (a block
 * tag, default {@code creativeworldgen:ground_match/grass}) is native when enough of those tops belong
 * to it; otherwise its blocks in the template's bottom layers are replaced (CWGConfig [worldgen.ground_match]):
 * exposed ones by a sampled top block, covered ones by a sampled under-block, both picked by position
 * hash with the sample's own proportions. Plants standing on a replaced block are dropped with it.
 *
 * <p>So nothing is configured per biome or per structure: a plains ruin stays grassy, the same ruin on
 * volcanic peaks sits on basalt/blackstone, in a desert on sand and sandstone. The structure tag
 * {@code creativeworldgen:ground_match/exempt} opts a structure out.
 */
public final class GroundMatch {

	private static final Logger LOG = LogManager.getLogger("creativeworldgen/ground-match");

	static final TagKey<Block> PLANTS = BlockTags.create(new ResourceLocation(CreativeWorldgen.NS, "ground_match/plants"));
	static final TagKey<Block> NOT_GROUND = BlockTags.create(new ResourceLocation(CreativeWorldgen.NS, "ground_match/not_ground"));
	static final TagKey<Structure> EXEMPT = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeWorldgen.NS, "ground_match/exempt"));

	// Sampling grid, under-block depth, dig limit and plant height: CWGConfig [worldgen.ground_match.sampling].

	private GroundMatch() {}

	// ── sample of the natural ground around one chunk ───────────────────────

	/** Tops and under-blocks as flat lists: duplicates are the weights. */
	record Sample(List<BlockState> tops, List<BlockState> subs) {}

	/** The structure start placing on this thread (StructureStart#placeInChunk, set by StructureStartGroundMatchMixin). */
	private static final ThreadLocal<StructureStart> PLACING = new ThreadLocal<>();

	public static void beginPlacing(StructureStart start) { PLACING.set(start); }

	public static void endPlacing() { PLACING.remove(); }

	/** The structure start placing on this thread, or null outside StructureStart#placeInChunk. */
	static StructureStart placing() { return PLACING.get(); }

	/** The sampling settings are part of the key, so a config edit never reuses a stale sample. */
	private record Key(ResourceKey<Level> dim, long chunk, int centerStride, int neighbourStride, int subDepth, int maxDig) {}

	// A chunk's decoration places all its pieces back to back, so a small LRU covers them.
	private static final Map<Key, Sample> CACHE = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
		@Override protected boolean removeEldestEntry(Map.Entry<Key, Sample> e) { return size() > 256; }
	});

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		CACHE.clear();   // a new world (other seed) reuses chunk positions
	}

	/**
	 * Worldgen samples are cached: a chunk's decoration places all its pieces back to back. /place samples
	 * fresh every time, since the chunk may have changed since it generated.
	 */
	static Sample sample(WorldGenLevel level, ChunkPos center, StructureStart placing, boolean cache) {
		int centerStride = WorldgenConfig.groundCenterStride(), neighbourStride = WorldgenConfig.groundNeighbourStride();
		int subDepth = WorldgenConfig.groundSubDepth(), maxDig = WorldgenConfig.groundMaxDig();
		Key key = new Key(level.getLevel().dimension(), center.toLong(), centerStride, neighbourStride, subDepth, maxDig);
		Sample s = cache ? CACHE.get(key) : null;
		if (s != null) return s;

		// Every piece of every structure touching the 3x3 area (and of the one being placed, which /place
		// never registers as a reference): their columns aren't natural ground.
		List<BoundingBox> exclude = new ArrayList<>();
		Map<StructureStart, Boolean> seen = new IdentityHashMap<>();
		if (placing != null) addPieces(placing, seen, exclude);
		for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
			ChunkAccess c = level.getChunk(center.x + dx, center.z + dz, ChunkStatus.STRUCTURE_REFERENCES, false);
			if (c == null) continue;
			for (Map.Entry<Structure, LongSet> ref : c.getAllReferences().entrySet()) {
				for (long startChunk : ref.getValue()) {
					ChunkAccess sc = level.getChunk(ChunkPos.getX(startChunk), ChunkPos.getZ(startChunk), ChunkStatus.STRUCTURE_STARTS, false);
					StructureStart start = sc == null ? null : sc.getStartForStructure(ref.getKey());
					if (start != null && start.isValid()) addPieces(start, seen, exclude);
				}
			}
		}

		List<BlockState> tops = new ArrayList<>(), subs = new ArrayList<>();
		for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
			ChunkAccess c = level.getChunk(center.x + dx, center.z + dz, ChunkStatus.SURFACE, false);
			if (c == null) continue;
			int stride = dx == 0 && dz == 0 ? centerStride : neighbourStride;
			int x0 = c.getPos().getMinBlockX(), z0 = c.getPos().getMinBlockZ();
			for (int lx = stride / 2; lx < 16; lx += stride) for (int lz = stride / 2; lz < 16; lz += stride) {
				sampleColumn(c, x0 + lx, z0 + lz, subDepth, maxDig, exclude, tops, subs);
			}
		}
		s = new Sample(tops, subs);
		if (cache) CACHE.put(key, s);
		return s;
	}

	private static void addPieces(StructureStart start, Map<StructureStart, Boolean> seen, List<BoundingBox> exclude) {
		if (seen.put(start, Boolean.TRUE) != null) return;
		for (StructurePiece piece : start.getPieces()) exclude.add(piece.getBoundingBox());
	}

	/**
	 * True when a piece covers column x z (1 block of margin) at heights minY..maxY. Only XZ was checked before, so an
	 * ancient city at y -51, Philip's underground ruins or a mineshaft under a site hid nearly every column, and the
	 * sample fell under minSamples (03_phase_3.md 4.1).
	 */
	static boolean covered(List<BoundingBox> boxes, int x, int z, int minY, int maxY) {
		for (BoundingBox b : boxes) {
			if (x >= b.minX() - 1 && x <= b.maxX() + 1 && z >= b.minZ() - 1 && z <= b.maxZ() + 1
					&& b.minY() <= maxY && b.maxY() >= minY) return true;
		}
		return false;
	}

	/**
	 * The column's top ground block and the ones under it, unless a structure piece reaches that ground (its top,
	 * its under-blocks, or the block right above it: a piece standing on the ground, or the top being the piece's own
	 * blocks).
	 */
	private static void sampleColumn(ChunkAccess c, int x, int z, int subDepth, int maxDig, List<BoundingBox> exclude,
			List<BlockState> tops, List<BlockState> subs) {
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		// Top motion-blocking block, water ignored. Generated chunks only keep the non-WG heightmaps.
		Heightmap.Types type = c.getStatus().isOrAfter(ChunkStatus.FULL) ? Heightmap.Types.OCEAN_FLOOR : Heightmap.Types.OCEAN_FLOOR_WG;
		int y = c.getHeight(type, x, z);
		int floor = c.getMinBuildHeight();
		for (int dig = 0; dig <= maxDig && y >= floor; dig++, y--) {
			BlockState st = c.getBlockState(m.set(x, y, z));
			if (!isGround(st)) continue;
			if (covered(exclude, x, z, y - subDepth - 1, y + 1)) return;
			tops.add(st);
			for (int d = 1; d <= subDepth && y - d >= floor; d++) {
				BlockState below = c.getBlockState(m.set(x, y - d, z));
				if (isGround(below)) subs.add(below);
			}
			return;
		}
	}

	/** Natural-looking solid ground: not air, liquid, a plant, or tree/huge-mushroom parts spilling from neighbours. */
	static boolean isGround(BlockState st) {
		return !st.isAir() && st.getFluidState().isEmpty() && st.getMaterial().isSolid() && !st.is(NOT_GROUND);
	}

	// ── per-family decision ──────────────────────────────────────────────────

	/** Replacement palettes for one family, or null when that family is native / undecidable here. */
	record Palette(BlockState[] top, BlockState[] sub, int familyTops, int columns) {}

	static Palette palette(Sample s, TagKey<Block> family, int minSamples, double nativeShare) {
		int n = s.tops().size();
		if (n < minSamples) return null;
		int inFamily = 0;
		List<BlockState> top = new ArrayList<>();
		for (BlockState st : s.tops()) {
			if (st.is(family)) inFamily++;
			else top.add(st);
		}
		if ((double) inFamily / n >= nativeShare || top.isEmpty()) return null;
		List<BlockState> sub = new ArrayList<>();
		for (BlockState st : s.subs()) if (!st.is(family)) sub.add(st);
		if (sub.isEmpty()) sub = top;
		return new Palette(top.toArray(BlockState[]::new), sub.toArray(BlockState[]::new), inFamily, n);
	}

	static BlockState pick(BlockState[] pal, BlockPos pos) {
		return pal[Math.floorMod((int) (Mth.getSeed(pos) >>> 16), pal.length)];
	}

	// ── config ───────────────────────────────────────────────────────────────

	private static volatile List<? extends String> familySource;
	private static volatile List<TagKey<Block>> familyTags = List.of();

	private static List<TagKey<Block>> families() {
		List<? extends String> src = WorldgenConfig.groundFamilies();
		if (src != familySource) {
			List<TagKey<Block>> out = new ArrayList<>();
			for (String id : src) {
				ResourceLocation rl = ResourceLocation.tryParse(id.startsWith("#") ? id.substring(1) : id);
				if (rl != null) out.add(BlockTags.create(rl));
				else LOG.warn("[ground-match] families: \"{}\" is not a tag id, skipped", id);
			}
			familyTags = List.copyOf(out);
			familySource = src;
		}
		return familyTags;
	}

	// ── the hook ─────────────────────────────────────────────────────────────

	/** The processed block list with mismatched base blocks swapped, or null when nothing changed. */
	public static List<StructureBlockInfo> apply(LevelAccessor level, BlockPos offset, StructurePlaceSettings settings, List<StructureBlockInfo> infos) {
		if (infos == null || infos.isEmpty()) return null;
		StructureStart placing = PLACING.get();
		// Worldgen (always inside placeInChunk) or /place structure (placeInChunk on the live level).
		WorldGenLevel region;
		if (level instanceof WorldGenRegion r) region = r;
		else if (placing != null && level instanceof ServerLevel sl) region = sl;
		else return null;
		if (!WorldgenConfig.groundMatchEnabled()) return null;
		List<TagKey<Block>> families = families();
		if (families.isEmpty()) return null;
		if (placing != null && exempt(region, MetaStructure.structureAt(placing, offset))) return null;   // a meta part: its own structure

		BoundingBox box = settings.getBoundingBox();
		ChunkPos center = region instanceof WorldGenRegion r ? r.getCenter()
				: box != null ? new ChunkPos(box.minX() >> 4, box.minZ() >> 4) : new ChunkPos(offset);
		int minSamples = WorldgenConfig.groundMinSamples();
		double nativeShare = WorldgenConfig.groundNativeShare();
		Sample[] sample = new Sample[1];   // only sampled once a base block is actually found
		Palette[] used = new Palette[families.size()];
		IntFunction<Palette> paletteFor = f -> {
			if (sample[0] == null) sample[0] = sample(region, center, placing, region instanceof WorldGenRegion);
			return used[f] = palette(sample[0], families.get(f), minSamples, nativeShare);
		};
		Predicate<BlockPos> terrainAt = pos -> region.hasChunk(pos.getX() >> 4, pos.getZ() >> 4) && isGround(region.getBlockState(pos));

		Rewrite rw = rewrite(infos, offset.getY(), box, families, WorldgenConfig.groundBaseLayers(), WorldgenConfig.groundMode(),
				WorldgenConfig.groundMaxPlantHeight(), paletteFor, terrainAt);
		if (WorldgenConfig.groundLog() && sample[0] != null && sample[0].tops().size() < minSamples) {
			// a base block was found but too few columns were sampled to decide: say so, or the skip is silent
			// (03_phase_3.md 4.1)
			LOG.info("[ground-match] chunk {} template at {}: only {} ground columns sampled (minSamples {}), base left as is",
					center, offset.toShortString(), sample[0].tops().size(), minSamples);
		}
		if (rw == null) return null;

		if (WorldgenConfig.groundLog()) {
			for (int f = 0; f < families.size(); f++) {
				Palette pal = used[f];
				if (pal == null) continue;
				LOG.info("[ground-match] chunk {} template at {}: {} {}% of {} ground columns -> replaced {}, kept terrain {}, removed {}, plants dropped {}; top {} / under {}",
						center, offset.toShortString(), families.get(f).location(), Math.round(100.0 * pal.familyTops() / pal.columns()),
						pal.columns(), rw.replaced(), rw.keptTerrain(), rw.removed(), rw.plants(), histogram(pal.top()), histogram(pal.sub()));
			}
		}
		return rw.infos();
	}

	/** One template's rewritten block list plus what happened to it (for the log line). */
	record Rewrite(List<StructureBlockInfo> infos, int replaced, int keptTerrain, int removed, int plants) {}

	/**
	 * The level-free part of {@link #apply}, so it can be tested headless. A base block is a family block
	 * in the bottom {@code layers} template layers, inside {@code box} (the chunk being placed). paletteFor
	 * gives a family's replacement palette, or null when that family is native here; it's called at most
	 * once per family and only when that family has a base block. Returns null when nothing changed.
	 */
	static Rewrite rewrite(List<StructureBlockInfo> infos, int offsetY, BoundingBox box, List<TagKey<Block>> families, int layers,
			GroundMode mode, int maxPlantHeight, IntFunction<Palette> paletteFor, Predicate<BlockPos> terrainAt) {
		Palette[] palettes = new Palette[families.size()];
		boolean[] decided = new boolean[families.size()];
		Map<BlockPos, Integer> index = null;
		List<StructureBlockInfo> out = null;
		List<BlockPos> changed = new ArrayList<>();
		int replaced = 0, keptTerrain = 0, removed = 0;

		for (int i = 0; i < infos.size(); i++) {
			StructureBlockInfo info = infos.get(i);
			if (box != null && !box.isInside(info.pos)) continue;
			int layer = info.pos.getY() - offsetY;   // rotation/mirror never move Y
			if (layer < 0 || layer >= layers) continue;
			int f = 0;
			while (f < families.size() && !info.state.is(families.get(f))) f++;
			if (f == families.size()) continue;
			if (!decided[f]) { palettes[f] = paletteFor.apply(f); decided[f] = true; }
			Palette pal = palettes[f];
			if (pal == null) continue;

			if (out == null) {
				out = new ArrayList<>(infos);
				index = new HashMap<>(infos.size() * 2);
				for (int k = 0; k < infos.size(); k++) index.put(infos.get(k).pos, k);
			}
			BlockPos pos = info.pos;
			changed.add(pos);

			if (mode == GroundMode.REMOVE) { out.set(i, null); removed++; continue; }
			if (mode == GroundMode.TERRAIN && terrainAt.test(pos)) { out.set(i, null); keptTerrain++; continue; }
			// Exposed (nothing solid on top in the template): a top block; covered: an under-block.
			Integer above = index.get(pos.above());
			BlockState aboveState = above == null ? null : infos.get(above).state;
			boolean exposed = aboveState == null || aboveState.isAir() || aboveState.is(PLANTS) || !aboveState.getMaterial().isSolid();
			out.set(i, new StructureBlockInfo(pos, pick(exposed ? pal.top() : pal.sub(), pos), null));
			replaced++;
		}
		if (out == null) return null;

		// Grass, flowers, saplings... on a block that's no longer grass: drop them (both halves of tall ones).
		int plants = 0;
		for (BlockPos pos : changed) {
			BlockPos p = pos.above();
			for (int h = 0; h < maxPlantHeight; h++, p = p.above()) {
				Integer j = index.get(p);
				if (j == null || out.get(j) == null || !out.get(j).state.is(PLANTS)) break;
				out.set(j, null);
				plants++;
			}
		}

		List<StructureBlockInfo> result = new ArrayList<>(out.size());
		for (StructureBlockInfo info : out) if (info != null) result.add(info);
		return new Rewrite(result, replaced, keptTerrain, removed, plants);
	}

	/** True when the structure is tagged ground_match/exempt. */
	private static boolean exempt(WorldGenLevel level, Structure structure) {
		Registry<Structure> reg = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		return reg.getResourceKey(structure).flatMap(reg::getHolder).map(h -> h.is(EXEMPT)).orElse(false);
	}

	private static String histogram(BlockState[] pal) {
		Map<String, Integer> counts = new TreeMap<>();
		for (BlockState st : pal) counts.merge(Registry.BLOCK.getKey(st.getBlock()).getPath(), 1, Integer::sum);
		return counts.toString();
	}
}
