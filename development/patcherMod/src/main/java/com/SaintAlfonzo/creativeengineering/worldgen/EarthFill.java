package com.SaintAlfonzo.creativeengineering.worldgen;

import com.SaintAlfonzo.creativeengineering.CEConfig.EarthFillLog;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.FeaturePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Cancels a structure whose foundation would need too much earth over water or air. Some structures
 * carry their own ground: a grass and dirt pad under a ruin, a dirt skirt under a house. Placed on a
 * shore, a cliff or a lake, that pad hangs in the air or fills the water, which looks wrong and is
 * exactly what a structure that "doesn't fit here" looks like.
 *
 * <p>The check runs on the finished structure start, right before it is stored (mixin/
 * ChunkGeneratorEarthFillMixin on ChunkGenerator#tryGenerateStructure, after the frequency roll's veto
 * would apply). For every template piece it takes the earth blocks (block tags, CEConfig
 * [worldgen.earth_fill] blocks) in the bottom {@code baseLayers} template layers, finds where each one
 * will stand, and asks the generator's predicted terrain column there what is in that spot: water, air
 * or something solid. Earth in water counts toward {@code maxOverWater}, earth in air toward the air limit, but
 * only where the structure's beard won't fill under it (its terrain adaptation: ground deeper than
 * {@code beardReach} below the piece, {@code buryReach} for bury, always with none), and every column whose ground lies more than
 * {@code maxMoundHeight} below a piece counts once as a mound column. The air and mound limits come from
 * the start's size, its own earth base block count ([worldgen.earth_fill.sizes]). Past any limit the whole
 * start is dropped and the slot stays empty (a slot shared with other structures falls through to the next
 * pick, as for a wrong biome). For a structure in {@code creativeengineering:template_trim} only earth that
 * {@link TemplateTrim} still places counts.
 *
 * <p>The footprint's water share, after the limits: the bounding box columns of every earth piece, at the layer the
 * start was set onto the surface with (the piece's bottom, or higher by the sink of {@link TerrainAdaptation#sink}),
 * are read from the same predicted terrain. When the water there (filled by the template's earth or the beard, or
 * left standing where the template has no block) outnumbers the dry ground (replaced or left alone) by more than
 * {@code maxWaterSurplus} (default 0: any water majority; 0.5 = water 50% over ground), the start is dropped too: a camp on an island, a ruin
 * reaching far out over a lake. A spot under more than {@code baseLayers} blocks of predicted terrain counts as neither
 * (underground structures in aquifers or under the sea). Water structures ({@code creativeengineering:water_fill/structures})
 * are not checked.
 *
 * <p>Pruning (structures in {@code creativeengineering:earth_fill/prune}, the villages): before the start is judged,
 * each jigsaw piece but the first is judged alone against [worldgen.earth_fill] pruneMaxHanging and pruneMaxMound. A
 * piece past either is dropped with every piece attached below it in the jigsaw tree (a house over deep water, with its
 * villagers), and its parent forgets the jigsaw junction to it. The rest is then judged as a whole, as usual. So one
 * house over a river no longer costs the whole village.
 *
 * <p>Water fill is the same check the other way round, for the structures in the
 * {@code creativeengineering:water_fill/structures} tag (underwater ruins, ships, stilted huts: they carry
 * their own water). Every water block of their templates (water, waterlogged blocks, kelp, seagrass; all
 * layers) is compared with the same predicted terrain: water landing in water is fine, and so is water
 * replacing a sea, lake or river bed (a solid block under a water column). Water replacing dry ground
 * counts toward {@code maxIntoGround}, water landing in air (above the sea, on land) toward
 * {@code maxIntoAir}, CEConfig [worldgen.water_fill].
 *
 * <p>Terrain-matching pieces (village streets, which a gravity processor lays on the ground column by column) are
 * skipped, and so are structures in the {@code creativeengineering:earth_fill/exempt} tag (floating
 * ones). The predicted terrain is the noise terrain from {@code ChunkGenerator#getBaseColumn}: no
 * carvers, surface rules, features or other structures' terrain adaptation. {@code /place structure}
 * and {@code /locate} don't go through this check (the same as the biome rules). A meta structure is checked part
 * by part, each part by its own tags and limits, as it would be alone.
 */
public final class EarthFill {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/earth-fill");

	static final TagKey<Structure> EXEMPT = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "earth_fill/exempt"));
	static final TagKey<Structure> PRUNE = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "earth_fill/prune"));
	/** Jigsaw structures whose pieces only make sense together: never pruned, judged whole (pruneWetJigsaws skips them). */
	static final TagKey<Structure> NO_PRUNE = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "earth_fill/no_prune"));
	/** Structures built on a stone or brick base (the drum tower's andesite plate): their base blocks count as earth too. */
	static final TagKey<Structure> STONE_BASE = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "earth_fill/stone_base"));
	static final TagKey<Block> STONE_BLOCKS = BlockTags.create(new ResourceLocation(CreativeEngineering.MODID, "earth_fill/stone_base"));
	/** Desert and badlands structures on sandstone or terracotta floors: those blocks count as earth too (2026-11-02). */
	static final TagKey<Structure> SAND_BASE = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "earth_fill/sand_base"));
	static final TagKey<Block> SAND_BLOCKS = BlockTags.create(new ResourceLocation(CreativeEngineering.MODID, "earth_fill/sand_base"));
	static final TagKey<Structure> WATER_STRUCTURES = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "water_fill/structures"));
	/** Structures that stand in water on purpose (stilted huts): no footprint water share, the other limits as usual. */
	static final TagKey<Structure> WET_OK = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "earth_fill/wet_ok"));

	private EarthFill() {}

	// ── what is where ────────────────────────────────────────────────────────

	/** SOLID is dry ground; SEABED is a solid block whose column is topped by water (sea, lake or river bed). */
	enum Kind { SOLID, SEABED, WATER, AIR }

	/** What the terrain holds at a block position. */
	interface Terrain {
		Kind at(int x, int y, int z);

		/** The y of the column's topmost non-air block (water included), or {@link #NO_TOP} when this terrain doesn't know. */
		default int top(int x, int z) { return NO_TOP; }

		int NO_TOP = Integer.MIN_VALUE;

		/** Open blocks (air or water: what a beard fills) from y downwards, y itself first, at most max. */
		default int openBelow(int x, int y, int z, int max) {
			int n = 0;
			for (Kind k; n < max && ((k = at(x, y - n, z)) == Kind.AIR || k == Kind.WATER); ) n++;
			return n;
		}
	}

	/** How many blocks may land in each kind of terrain, -1 = no limit. Seabed is never limited. */
	record Limits(int ground, int water, int air) {
		boolean none() { return ground < 0 && water < 0 && air < 0; }
	}

	/** The outcome of one structure. {@code complete} is false when counting stopped at the first limit crossed. */
	record Result(int blocks, int ground, int water, int air, boolean cancelled, boolean complete) {}

	/**
	 * The level-free core: each long[] is one piece's block positions (BlockPos.asLong). A position
	 * appearing in two pieces counts once. With stopEarly, counting ends at the first limit crossed.
	 */
	static Result count(Iterable<long[]> pieces, Terrain terrain, Limits limits, boolean stopEarly) {
		LongOpenHashSet seen = new LongOpenHashSet();
		int blocks = 0, ground = 0, water = 0, air = 0;
		for (long[] piece : pieces) {
			for (long p : piece) {
				if (!seen.add(p)) continue;
				blocks++;
				switch (terrain.at(BlockPos.getX(p), BlockPos.getY(p), BlockPos.getZ(p))) {
					case SOLID -> ground++;
					case WATER -> water++;
					case AIR -> air++;
					default -> { }
				}
				if (stopEarly && over(ground, water, air, limits)) return new Result(blocks, ground, water, air, true, false);
			}
		}
		return new Result(blocks, ground, water, air, over(ground, water, air, limits), true);
	}

	// ── earth: the beard and mound columns ───────────────────────────────────

	/** How a piece's terrain adaptation treats the air under it: NONE fills nothing, BEARD to beardReach, BURY to buryReach. */
	enum Beard { NONE, BEARD, BURY }

	static Beard beardOf(Structure structure) {
		return switch (structure.terrainAdaptation()) {
			case NONE -> Beard.NONE;
			case BURY -> Beard.BURY;
			default -> Beard.BEARD;   // beard_thin, beard_box and the soft beard fill under a piece the same way
		};
	}

	/** One piece's earth blocks (BlockPos.asLong), its bottom layer and its beard. */
	record EarthPiece(long[] blocks, int bottom, Beard beard) {}

	/** Earth limits, -1 = no limit: blocks over water, blocks over air after the beard, mound columns. */
	record EarthLimits(int water, int air, int mound) {
		boolean none() { return water < 0 && air < 0 && mound < 0; }
	}

	/**
	 * How deep the beard fills (beardReach, buryReach) and the mound column depth (maxMoundHeight), in blocks below a
	 * piece; water: how deep water under earth may be before it counts (waterReach, never past the beard's reach).
	 */
	record Reach(int beard, int bury, int mound, int water) {
		Reach(int beard, int bury, int mound) { this(beard, bury, mound, Integer.MAX_VALUE); }
		int of(Beard b) { return b == Beard.BURY ? bury : beard; }
		int ofWater(Beard b) { return Math.min(of(b), water); }
	}

	record EarthResult(int blocks, int water, int air, int mound, boolean cancelled, boolean complete) {}

	/** One bearded rigid piece's solid bottom blocks (BlockPos.asLong) and its beard's ground level, for beardFootprint. */
	record BasePiece(long[] blocks, int bottom) {}

	/**
	 * beardFootprint: the mound columns under the built bottom of bearded pieces that {@link #countEarth} doesn't see
	 * (columns holding no earth of any piece). A column counts once, when its ground lies more than reach.mound() below
	 * the piece's bottom.
	 */
	static int baseMound(List<BasePiece> bases, List<EarthPiece> earth, Terrain terrain, Reach reach) {
		LongOpenHashSet columns = new LongOpenHashSet();
		for (EarthPiece piece : earth) for (long p : piece.blocks()) columns.add(ChunkPos.asLong(BlockPos.getX(p), BlockPos.getZ(p)));
		int mound = 0;
		for (BasePiece piece : bases) {
			for (long p : piece.blocks()) {
				int x = BlockPos.getX(p), z = BlockPos.getZ(p);
				if (columns.add(ChunkPos.asLong(x, z)) && terrain.openBelow(x, piece.bottom() - 1, z, reach.mound() + 1) > reach.mound()) mound++;
			}
		}
		return mound;
	}

	/** The earth result with beardFootprint's extra mound columns added and the limits judged again. */
	static EarthResult withBaseMound(EarthResult r, int extra, EarthLimits limits) {
		if (extra == 0) return r;
		int mound = r.mound() + extra;
		return new EarthResult(r.blocks(), r.water(), r.air(), mound, over(r.water(), r.air(), mound, limits), r.complete());
	}

	/** Unique earth positions over all pieces: the start's size. */
	static int earthBlocks(List<EarthPiece> pieces) {
		LongOpenHashSet seen = new LongOpenHashSet();
		for (EarthPiece piece : pieces) for (long p : piece.blocks()) seen.add(p);
		return seen.size();
	}

	/**
	 * The level-free earth count. Earth over water counts toward water, earth over air toward air: always when the
	 * piece has no beard, else only where the ground under it lies more than the beard's reach below the piece's
	 * bottom layer. The beard fills shallower air and water with ground before the template is placed (vanilla
	 * villages set their house floors into the top water layer of a river or the sea this way). Each column whose
	 * ground lies more than reach.mound() below its piece's bottom counts once toward mound. A position in two pieces
	 * counts once.
	 */
	static EarthResult countEarth(List<EarthPiece> pieces, Terrain terrain, EarthLimits limits, Reach reach, boolean stopEarly) {
		LongOpenHashSet seen = new LongOpenHashSet(), columns = new LongOpenHashSet();
		int blocks = 0, water = 0, air = 0, mound = 0;
		for (EarthPiece piece : pieces) {
			int fills = piece.beard() == Beard.NONE ? -1 : reach.of(piece.beard());
			int fillsWater = piece.beard() == Beard.NONE ? -1 : reach.ofWater(piece.beard());
			for (long p : piece.blocks()) {
				if (!seen.add(p)) continue;
				blocks++;
				int x = BlockPos.getX(p), y = BlockPos.getY(p), z = BlockPos.getZ(p);
				Kind kind = terrain.at(x, y, z);
				if (kind == Kind.WATER || kind == Kind.AIR) {
					// open down to the piece's bottom (y - bottom of its own layers), then more than 'fills' below it;
					// water by its own, shorter reach (waterReach, 2026-11-02)
					int f = kind == Kind.WATER ? fillsWater : fills;
					int deeper = f + (y - piece.bottom()) + 2;
					if (f < 0 || terrain.openBelow(x, y, z, deeper) >= deeper) {
						if (kind == Kind.WATER) water++; else air++;
					}
				}
				if (columns.add(ChunkPos.asLong(x, z)) && terrain.openBelow(x, piece.bottom() - 1, z, reach.mound() + 1) > reach.mound()) mound++;
				if (stopEarly && over(water, air, mound, limits)) return new EarthResult(blocks, water, air, mound, true, false);
			}
		}
		return new EarthResult(blocks, water, air, mound, over(water, air, mound, limits), true);
	}

	// ── earth: the footprint's water share ───────────────────────────────────

	/** One piece's footprint: its box's columns at layer y (the layer the start was set onto the surface with). */
	record Layer(int minX, int minZ, int maxX, int maxZ, int y) {}

	/** A piece with no earth: its box's columns, counted where the surface lies between minY and maxY. */
	record Span(int minX, int minZ, int maxX, int maxZ, int minY, int maxY) {}

	/** The footprint's water blocks (filled by earth or the beard, or left standing) and dry ground blocks. */
	record Footprint(int water, int ground) {
		/** More water than ground by more than {@code surplus} (0.5 = water 50% over ground); never with surplus < 0. */
		boolean tooWet(double surplus) { return surplus >= 0 && water > ground * (1.0 + surplus); }

		/** {@link #tooWet(double)} with at least minWater water columns (a cliff-edge start with 1 water / 0 ground stays). */
		boolean tooWet(double surplus, int minWater) { return water >= minWater && tooWet(surplus); }
	}

	/**
	 * The level-free footprint count: every column of every layer once, at its lowest layer that is not buried (stacked
	 * pieces count a column once). Seabed and air count as neither, and so does a position with more than {@code depth}
	 * blocks of terrain above it (a structure underground, in an aquifer or under the sea, is no camp on an island).
	 */
	static Footprint footprint(List<Layer> layers, Terrain terrain, int depth) {
		return footprint(layers, List.of(), terrain, depth);
	}

	/**
	 * With spans (pieces with no earth: plank or sandstone floors, legacy pieces): each column not already in a layer
	 * counts once, by the predicted surface (the column's top) when it lies from {@code depth} below the span's bottom to
	 * its top: water when that surface is water, ground when solid. A span buried under the surface or floating above it
	 * counts as neither.
	 */
	static Footprint footprint(List<Layer> layers, List<Span> spans, Terrain terrain, int depth) {
		LongOpenHashSet counted = new LongOpenHashSet(), columns = new LongOpenHashSet();
		int water = 0, ground = 0;
		List<Layer> lowestFirst = layers.size() < 2 ? layers : new ArrayList<>(layers);
		if (lowestFirst != layers) lowestFirst.sort(java.util.Comparator.comparingInt(Layer::y));
		for (Layer l : lowestFirst) {
			for (int x = l.minX(); x <= l.maxX(); x++) for (int z = l.minZ(); z <= l.maxZ(); z++) {
				long column = ChunkPos.asLong(x, z);
				columns.add(column);
				if (counted.contains(column)) continue;
				int top = terrain.top(x, z);
				if (top != Terrain.NO_TOP && top - l.y() > depth) continue;   // buried here: a higher layer may still count it
				counted.add(column);
				switch (terrain.at(x, l.y(), z)) {
					case WATER -> water++;
					case SOLID -> ground++;
					default -> { }
				}
			}
		}
		for (Span sp : spans) {
			for (int x = sp.minX(); x <= sp.maxX(); x++) for (int z = sp.minZ(); z <= sp.maxZ(); z++) {
				if (!columns.add(ChunkPos.asLong(x, z))) continue;
				int top = terrain.top(x, z);
				if (top == Terrain.NO_TOP || top < sp.minY() - depth || top > sp.maxY()) continue;
				switch (terrain.at(x, top, z)) {
					case WATER -> water++;
					case SOLID -> ground++;
					default -> { }
				}
			}
		}
		return new Footprint(water, ground);
	}

	/**
	 * A piece that stands on the ground as built: not a street that follows the terrain, not an empty or feature pool
	 * element, not a meta index or meta feature spot. Its box can join the footprint when it has no earth.
	 */
	static boolean rigid(StructurePiece piece) {
		if (piece instanceof MetaStructure.Index || piece instanceof MetaFeaturePiece) return false;
		if (piece instanceof PoolElementStructurePiece pool) {
			StructurePoolElement e = pool.getElement();
			return e.getProjection() != StructureTemplatePool.Projection.TERRAIN_MATCHING
					&& !(e instanceof EmptyPoolElement) && !(e instanceof FeaturePoolElement);
		}
		return true;
	}

	private static boolean over(int water, int air, int mound, EarthLimits l) {
		return over(water, l.water()) || over(air, l.air()) || over(mound, l.mound());
	}

	private static boolean over(int ground, int water, int air, Limits l) {
		return over(ground, l.ground()) || over(water, l.water()) || over(air, l.air());
	}

	private static boolean over(int n, int limit) { return limit >= 0 && n > limit; }

	// ── the earth and water blocks of a piece ────────────────────────────────

	static Field field(Class<?> c, String srg, String mojmap) {
		try {
			return ObfuscationReflectionHelper.findField(c, srg);
		} catch (RuntimeException | LinkageError e) {
			try {
				Field f = c.getDeclaredField(mojmap);
				f.setAccessible(true);
				return f;
			} catch (NoSuchFieldException e2) {
				throw new IllegalStateException("EarthFill: no field " + srg + "/" + mojmap + " on " + c.getName(), e);
			}
		}
	}

	private static final Field PALETTES = field(StructureTemplate.class, "f_74482_", "palettes");
	static final Field SINGLE_TEMPLATE = field(SinglePoolElement.class, "f_210411_", "template");
	static final Field LIST_ELEMENTS = field(ListPoolElement.class, "f_210360_", "elements");

	/**
	 * Local positions of a template's earth blocks (bottom layers, first palette), by template, one map per base layers,
	 * earth tag list and trim (structures in creativeengineering:template_trim: earth in pad columns the trim drops isn't
	 * placed). Stone-base structures use a second tag list, so the lists live side by side instead of clearing each other.
	 */
	private static final Map<String, Map<StructureTemplate, long[]>> LOCAL = new ConcurrentHashMap<>();
	/** Local positions of a template's water blocks (all layers, first palette), by template. */
	private static final Map<StructureTemplate, long[]> LOCAL_WATER = new ConcurrentHashMap<>();

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) { LOCAL.clear(); LOCAL_WATER.clear(); SHARED.clear(); }

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) {
		LOCAL.clear();
		LOCAL_WATER.clear();
		for (TagKey<Block> tag : earthTags()) {
			boolean empty = Registry.BLOCK.getTag(tag).map(t -> t.size() == 0).orElse(true);
			if (empty && EMPTY_WARNED.add(tag)) LOG.warn("[earth-fill] blocks: tag #{} holds no block (a block id instead of a tag?); it counts nothing", tag.location());
		}
	}

	private static final Set<TagKey<Block>> EMPTY_WARNED = ConcurrentHashMap.newKeySet();

	/** A template's first palette (local positions), or an empty list. */
	static List<StructureTemplate.StructureBlockInfo> firstPalette(StructureTemplate template) {
		try {
			List<?> palettes = (List<?>) PALETTES.get(template);
			return palettes.isEmpty() ? List.of() : ((StructureTemplate.Palette) palettes.get(0)).blocks();
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/** Local positions of the blocks of a template's first palette that pass the test. */
	private static long[] localBlocks(StructureTemplate template, Predicate<StructureTemplate.StructureBlockInfo> test) {
		LongArrayList out = new LongArrayList();
		for (StructureTemplate.StructureBlockInfo info : firstPalette(template)) {
			if (test.test(info)) out.add(info.pos.asLong());
		}
		return out.toLongArray();
	}

	static long[] localEarth(StructureTemplate template, List<TagKey<Block>> earth, int layers) {
		return localEarth(template, earth, layers, false);
	}

	/** With trimmed, earth in the columns {@link TemplateTrim} drops (no built block above) is left out: it is never placed. */
	static long[] localEarth(StructureTemplate template, List<TagKey<Block>> earth, int layers, boolean trimmed) {
		String key = layers + "|" + trimmed + "|" + earth;
		return LOCAL.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).computeIfAbsent(template, t -> {
			TemplateTrim.Columns columns = trimmed ? TemplateTrim.columns(t) : null;
			return localBlocks(t, info -> info.pos.getY() >= 0 && info.pos.getY() < layers && isEarth(info.state, earth)
					&& (columns == null || columns.placed(info.pos.getX(), info.pos.getZ())));
		});
	}

	/**
	 * The solid blocks of a template's bottom layers (not air, not a structure block / jigsaw / void), for beardFootprint;
	 * with trimmed, only those in columns {@link TemplateTrim} keeps.
	 */
	static long[] localBase(StructureTemplate template, int layers, boolean trimmed) {
		return LOCAL.computeIfAbsent("base|" + layers + "|" + trimmed, k -> new ConcurrentHashMap<>()).computeIfAbsent(template, t -> {
			TemplateTrim.Columns columns = trimmed ? TemplateTrim.columns(t) : null;
			return localBlocks(t, info -> {
				if (info.pos.getY() < 0 || info.pos.getY() >= layers) return false;
				TemplateTrim.Kind k = TemplateTrim.kind(info.state);
				return k != TemplateTrim.Kind.AIR && k != TemplateTrim.Kind.MARKER && (columns == null || columns.placed(info.pos.getX(), info.pos.getZ()));
			});
		});
	}

	/** Water, waterlogged blocks, kelp, seagrass: anything that places water. */
	static long[] localWater(StructureTemplate template) {
		return LOCAL_WATER.computeIfAbsent(template, t -> localBlocks(t, info -> isWater(info.state)));
	}

	/** Holds water (a water block, waterlogged, kelp...). By fluid type, not the fluid tag: tag-free and water only. */
	static boolean isWater(BlockState state) { return state.getFluidState().getType().isSame(Fluids.WATER); }

	static boolean isEarth(BlockState state, List<TagKey<Block>> earth) {
		for (TagKey<Block> tag : earth) if (state.is(tag)) return true;
		return false;
	}

	/** World positions of one template's selected blocks, placed at origin with these settings. */
	static void addTemplate(StructureTemplate template, BlockPos origin, StructurePlaceSettings settings, Function<StructureTemplate, long[]> local, LongArrayList out) {
		for (long l : local.apply(template)) {
			out.add(StructureTemplate.calculateRelativePosition(settings, BlockPos.of(l)).offset(origin).asLong());
		}
	}

	@SuppressWarnings("unchecked")
	static void addElement(StructurePoolElement element, BlockPos origin, Rotation rotation, StructureTemplateManager templates,
			Function<StructureTemplate, long[]> local, LongArrayList out) {
		if (element.getProjection() == StructureTemplatePool.Projection.TERRAIN_MATCHING) return;
		try {
			if (element instanceof ListPoolElement list) {
				for (StructurePoolElement sub : (List<StructurePoolElement>) LIST_ELEMENTS.get(list)) addElement(sub, origin, rotation, templates, local, out);
			} else if (element instanceof SinglePoolElement single) {
				Either<ResourceLocation, StructureTemplate> either = (Either<ResourceLocation, StructureTemplate>) SINGLE_TEMPLATE.get(single);
				StructureTemplate template = either.map(rl -> templates.getOrCreate(rl), Function.identity());
				addTemplate(template, origin, new StructurePlaceSettings().setRotation(rotation), local, out);
			}
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/** World positions of a piece's selected blocks; empty for pieces that aren't templates (or follow the terrain). */
	static long[] pieceBlocks(StructurePiece piece, StructureTemplateManager templates, Function<StructureTemplate, long[]> local) {
		LongArrayList out = new LongArrayList();
		if (piece instanceof PoolElementStructurePiece pool) {
			addElement(pool.getElement(), pool.getPosition(), pool.getRotation(), templates, local, out);
		} else if (piece instanceof TemplateStructurePiece tp) {
			addTemplate(tp.template(), tp.templatePosition(), tp.placeSettings(), local, out);
		}
		return out.toLongArray();
	}

	static long[] pieceEarth(StructurePiece piece, StructureTemplateManager templates, List<TagKey<Block>> earth, int layers) {
		return pieceEarth(piece, templates, earth, layers, false);
	}

	static long[] pieceEarth(StructurePiece piece, StructureTemplateManager templates, List<TagKey<Block>> earth, int layers, boolean trimmed) {
		return pieceBlocks(piece, templates, t -> localEarth(t, earth, layers, trimmed));
	}

	static long[] pieceWater(StructurePiece piece, StructureTemplateManager templates) {
		return pieceBlocks(piece, templates, EarthFill::localWater);
	}

	// ── predicted terrain ────────────────────────────────────────────────────

	/**
	 * One predicted column, packed 2 bits per y over the level's build height (air, water or anything else; outside
	 * it reads as air, as NoiseColumn does), with its topmost non-air block and whether that is water. About 100
	 * bytes instead of a 384-entry NoiseColumn, so the shared cache can hold many.
	 */
	static final class Column {
		private static final int AIR = 0, WATER = 1, OTHER = 2;
		final int minY, height, top;
		final boolean topWater;
		private final long[] kinds;

		Column(NoiseColumn blocks, LevelHeightAccessor level) {
			minY = level.getMinBuildHeight();
			height = level.getHeight();
			kinds = new long[(height * 2 + 63) / 64];
			int topY = minY - 1;
			boolean water = false;
			for (int i = height - 1; i >= 0; i--) {
				BlockState state = blocks.getBlock(minY + i);
				int k = state.isAir() ? AIR : isWater(state) ? WATER : OTHER;
				if (k == AIR) continue;
				kinds[i >> 5] |= (long) k << ((i & 31) << 1);
				if (topY < minY) { topY = minY + i; water = k == WATER; }
			}
			top = topY;
			topWater = water;
		}

		Kind at(int y) {
			int i = y - minY;
			if (i < 0 || i >= height) return Kind.AIR;
			int k = (int) (kinds[i >> 5] >>> ((i & 31) << 1)) & 3;
			if (k == AIR) return Kind.AIR;
			if (k == WATER) return Kind.WATER;
			return topWater ? Kind.SEABED : Kind.SOLID;
		}
	}

	/**
	 * Predicted columns shared by every start, placement-priority prediction and meta retry of a dimension: the same
	 * chunk's columns are otherwise read again by each (perf.md 10.7). Keyed by everything a column depends on, so a
	 * cached column is exactly what a fresh read returns. Bounded per key; cleared when the server stops.
	 */
	private record ColumnSource(ChunkGenerator generator, RandomState randomState, int grid, int minY, int height) {}

	private static final int SHARED_COLUMNS = 65_536;
	private static final Map<ColumnSource, Cache<Long, Column>> SHARED = new ConcurrentHashMap<>();

	private static Cache<Long, Column> shared(ChunkGenerator generator, RandomState randomState, LevelHeightAccessor level, int grid) {
		if (!WorldgenConfig.sharedTerrainCacheEnabled()) return null;
		return SHARED.computeIfAbsent(new ColumnSource(generator, randomState, grid, level.getMinBuildHeight(), level.getHeight()),
				k -> CacheBuilder.newBuilder().maximumSize(SHARED_COLUMNS).build());
	}

	/** The y of a column's topmost non-air block, or one below the bottom for an all-air column. */
	static int topOf(NoiseColumn column, LevelHeightAccessor level) {
		for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
			if (!column.getBlock(y).isAir()) return y;
		}
		return level.getMinBuildHeight() - 1;
	}

	/** Whether the topmost non-air block of a column is water (a sea, lake or river), not dry land. */
	static boolean topIsWater(NoiseColumn column, LevelHeightAccessor level) {
		int top = topOf(column, level);
		return top >= level.getMinBuildHeight() && isWater(column.getBlock(top));
	}

	/**
	 * The generator's noise terrain, read once per grid x grid cell. Columns are kept for this start and, with
	 * [worldgen.systems] sharedTerrainCache, in the dimension's shared cache.
	 */
	static Terrain predicted(ChunkGenerator generator, RandomState randomState, LevelHeightAccessor level, int grid) {
		Long2ObjectOpenHashMap<Column> columns = new Long2ObjectOpenHashMap<>();
		Cache<Long, Column> shared = shared(generator, randomState, level, grid);
		return new Terrain() {
			private Column column(int x, int z) {
				int cx = Math.floorDiv(x, grid) * grid + grid / 2, cz = Math.floorDiv(z, grid) * grid + grid / 2;
				long key = ChunkPos.asLong(cx, cz);
				Column column = columns.get(key);
				if (column != null) return column;
				if (shared != null) column = shared.getIfPresent(key);
				if (column == null) {
					// two threads may read the same column at once: both get the same answer, either is kept
					column = new Column(generator.getBaseColumn(cx, cz, level, randomState), level);
					if (shared != null) shared.put(key, column);
				}
				columns.put(key, column);
				return column;
			}

			public Kind at(int x, int y, int z) { return column(x, z).at(y); }

			public int top(int x, int z) { return column(x, z).top; }
		};
	}

	// ── config ───────────────────────────────────────────────────────────────

	private static volatile List<? extends String> blocksSource;
	private static volatile List<TagKey<Block>> blockTags = List.of();

	private static List<TagKey<Block>> earthTags() {
		List<? extends String> src = WorldgenConfig.earthBlocks();
		if (src != blocksSource) {
			List<TagKey<Block>> out = new ArrayList<>();
			for (String id : src) {
				ResourceLocation rl = ResourceLocation.tryParse(id.startsWith("#") ? id.substring(1) : id);
				if (rl != null) out.add(BlockTags.create(rl));
				else LOG.warn("[earth-fill] blocks: \"{}\" is not a tag id, skipped", id);
			}
			blockTags = List.copyOf(out);
			blocksSource = src;
		}
		return blockTags;
	}

	/** The earth tags with the base tags added, per earth list: [stone, sand, stone + sand]. */
	private record BaseTags(List<TagKey<Block>> source, List<List<TagKey<Block>>> lists) {}
	private static volatile BaseTags baseTags;

	/**
	 * The earth tags plus creativeengineering:earth_fill/stone_base and / or earth_fill/sand_base, for structures in the
	 * structure tags of those names.
	 */
	static List<TagKey<Block>> withBases(List<TagKey<Block>> earth, boolean stone, boolean sand) {
		if (!stone && !sand) return earth;
		BaseTags b = baseTags;
		if (b == null || b.source() != earth) {
			List<List<TagKey<Block>>> lists = new ArrayList<>();
			for (List<TagKey<Block>> add : List.of(List.of(STONE_BLOCKS), List.of(SAND_BLOCKS), List.of(STONE_BLOCKS, SAND_BLOCKS))) {
				List<TagKey<Block>> out = new ArrayList<>(earth);
				for (TagKey<Block> t : add) if (!out.contains(t)) out.add(t);
				lists.add(List.copyOf(out));
			}
			baseTags = b = new BaseTags(earth, List.copyOf(lists));
		}
		return b.lists().get(stone && sand ? 2 : sand ? 1 : 0);
	}

	/** "id=a,b[,c]" or "#tag=a,b[,c]": earth fill reads (water, air, mound), water fill (ground, air). c is UNSET when left out. */
	record Override(String key, int a, int b, int c) {
		static final int UNSET = Integer.MIN_VALUE;
	}

	/** Each line takes 2 numbers, or 3 with {@code third}. */
	static List<Override> parseOverrides(String system, List<? extends String> src, boolean third) {
		List<Override> out = new ArrayList<>();
		for (String line : src) {
			int eq = line.lastIndexOf('=');
			String[] nums = eq < 0 ? new String[0] : line.substring(eq + 1).split(",");
			try {
				if (eq <= 0 || nums.length < 2 || nums.length > (third ? 3 : 2)) throw new NumberFormatException();
				String key = line.substring(0, eq).trim();
				boolean tag = key.startsWith("#");
				// an id without a namespace is minecraft's, as everywhere else ("village_plains" = "minecraft:village_plains")
				ResourceLocation rl = ResourceLocation.tryParse(tag ? key.substring(1) : key);
				if (rl == null) throw new NumberFormatException();
				out.add(new Override((tag ? "#" : "") + rl, Integer.parseInt(nums[0].trim()), Integer.parseInt(nums[1].trim()),
						nums.length == 3 ? Integer.parseInt(nums[2].trim()) : Override.UNSET));
			} catch (NumberFormatException e) {
				LOG.warn("[{}] overrides: \"{}\" is not \"<structure id or #tag>=<number>,<number>{}\", skipped", system, line, third ? "[,<number>]" : "");
			}
		}
		return List.copyOf(out);
	}

	/** One config overrides list, parsed again whenever the config hands out a new list. */
	private static final class Overrides {
		private final String system;
		private final boolean third;
		private final Supplier<List<? extends String>> config;
		private volatile List<? extends String> source;
		private volatile List<Override> parsed = List.of();

		Overrides(String system, boolean third, Supplier<List<? extends String>> config) { this.system = system; this.third = third; this.config = config; }

		/** The first entry matching the structure, or null. */
		Override match(Holder<Structure> structure) {
			List<? extends String> src = config.get();
			if (src != source) {
				parsed = parseOverrides(system, src, third);
				source = src;
			}
			for (Override o : parsed) {
				boolean hit;
				if (o.key().startsWith("#")) {
					ResourceLocation rl = ResourceLocation.tryParse(o.key().substring(1));
					hit = rl != null && structure.is(TagKey.create(Registry.STRUCTURE_REGISTRY, rl));
				} else {
					hit = structure.unwrapKey().map(k -> k.location().toString().equals(o.key())).orElse(false);
				}
				if (hit) return o;
			}
			return null;
		}
	}

	private static final Overrides EARTH_OVERRIDES = new Overrides("earth-fill", true, WorldgenConfig::earthOverrides);
	private static final Overrides WATER_OVERRIDES = new Overrides("water-fill", false, WorldgenConfig::waterOverrides);

	static final String[] SIZES = {"small", "medium", "large", "huge"};

	/** The size of a start with this many earth base blocks (an index into SIZES), by [worldgen.earth_fill.sizes] bounds. */
	static int sizeOf(int earthBlocks, List<? extends Integer> bounds) {
		if (!sorted(bounds)) {
			if (UNSORTED_WARNED.compareAndSet(false, true)) LOG.warn("[earth-fill] sizes.bounds {} is not in rising order; sorted", bounds);
			List<Integer> copy = new ArrayList<>(bounds);
			java.util.Collections.sort(copy);
			bounds = copy;
		}
		int size = 0;
		for (int i = 0; i < bounds.size() && i < SIZES.length - 1; i++) if (earthBlocks >= bounds.get(i)) size = i + 1;
		return size;
	}

	private static final java.util.concurrent.atomic.AtomicBoolean UNSORTED_WARNED = new java.util.concurrent.atomic.AtomicBoolean();

	private static boolean sorted(List<? extends Integer> bounds) {
		for (int i = 1; i < bounds.size(); i++) if (bounds.get(i) < bounds.get(i - 1)) return false;
		return true;
	}

	/** Earth limits for one start: the size's air and mound limits and the global water limit; a matching override replaces them. */
	static EarthLimits earthLimitsFor(Holder<Structure> structure, int size) {
		List<? extends Integer> sized = WorldgenConfig.earthSizeLimits().get(size);
		int air = sized.size() > 0 ? sized.get(0) : -1, mound = sized.size() > 1 ? sized.get(1) : -1;
		Override o = EARTH_OVERRIDES.match(structure);
		if (o == null) return new EarthLimits(WorldgenConfig.earthMaxOverWater(), air, mound);
		return new EarthLimits(o.a(), o.b(), o.c() == Override.UNSET ? mound : o.c());
	}

	/** An override that lifts every earth limit: the structure needs no terrain reads unless log = ALL. */
	static boolean earthUnlimited(Holder<Structure> structure) {
		return unlimited(EARTH_OVERRIDES.match(structure));
	}

	/** Every limit -1; a left-out mound (UNSET) keeps the size's mound limit, so "id=-1,-1" is not unlimited. */
	static boolean unlimited(Override o) {
		return o != null && o.a() < 0 && o.b() < 0 && o.c() != Override.UNSET && o.c() < 0;
	}

	/**
	 * A start's verdict: over its limits or its footprint too wet; never for an override that lifts every limit,
	 * which log = ALL only measures (the other log levels skip it before any terrain read).
	 */
	static boolean earthCancelled(boolean unlimited, EarthResult r, Footprint f, double surplus) {
		return earthCancelled(unlimited, r, f, surplus, 0);
	}

	/** With the footprint's minimum water sample (footprintMinWater, TODO structures/earthfill #15 E7). */
	static boolean earthCancelled(boolean unlimited, EarthResult r, Footprint f, double surplus, int minWater) {
		return !unlimited && (r.cancelled() || f != null && f.tooWet(surplus, minWater));
	}

	// ── pruning: which starts ────────────────────────────────────────────────

	/**
	 * How a start is pruned. FULL (earth_fill/prune: villages): a piece over pruneMaxHanging / pruneMaxMound or with a
	 * too-wet footprint goes. WET (pruneWetJigsaws: every other jigsaw start on the surface): only a too-wet piece goes,
	 * so a camp, outpost or ruin reaching out over a lake loses that piece instead of the whole start. NONE: judged whole.
	 */
	enum Prune { NONE, WET, FULL }

	/**
	 * The level-free part of the pick: prune on, not unlimited, not in earth_fill/no_prune; FULL when tagged prune, WET
	 * for a surface start of two or more pieces whose first is a jigsaw piece.
	 */
	static Prune pruneMode(boolean prune, boolean wetJigsaws, boolean tagged, boolean excluded, boolean unlimited, boolean surface,
			List<StructurePiece> pieces) {
		if (!prune || excluded || unlimited) return Prune.NONE;
		if (tagged) return Prune.FULL;
		boolean jigsaw = pieces.size() > 1 && pieces.get(0) instanceof PoolElementStructurePiece;
		return wetJigsaws && surface && jigsaw ? Prune.WET : Prune.NONE;
	}

	// ── pruning: the jigsaw tree of a start ─────────────────────────────────

	/** One jigsaw junction of a piece: where it is (x, z) and its deltaY. */
	record Joint(int x, int z, int deltaY) {}

	/**
	 * The jigsaw tree of a start from its pieces' boxes and junctions, in piece order (JigsawPlacement adds every
	 * piece after its parent). A piece's first junction is the one to its parent, at the parent's jigsaw block; the
	 * parent holds the matching junction at the child's jigsaw block, the next block over (or the same column for an
	 * up/down jigsaw), with the opposite deltaY. Returns {parent index, index of that junction in the parent} per piece;
	 * -1 for the root, a piece with no junctions, or no match (never pruned with anything).
	 */
	static int[][] links(List<BoundingBox> boxes, List<List<Joint>> joints) {
		int n = boxes.size();
		int[] parent = new int[n], joint = new int[n];
		java.util.Arrays.fill(parent, -1);
		java.util.Arrays.fill(joint, -1);
		for (int c = 1; c < n; c++) {
			if (joints.get(c).isEmpty()) continue;
			Joint up = joints.get(c).get(0);
			BoundingBox cb = boxes.get(c);
			search:
			for (int p = c - 1; p >= 0; p--) {
				BoundingBox pb = boxes.get(p);
				if (up.x() < pb.minX() || up.x() > pb.maxX() || up.z() < pb.minZ() || up.z() > pb.maxZ()) continue;
				List<Joint> pj = joints.get(p);
				for (int k = 0; k < pj.size(); k++) {
					Joint down = pj.get(k);
					if (down.deltaY() != -up.deltaY() || Math.abs(down.x() - up.x()) + Math.abs(down.z() - up.z()) > 1) continue;
					if (down.x() < cb.minX() || down.x() > cb.maxX() || down.z() < cb.minZ() || down.z() > cb.maxZ()) continue;
					parent[c] = p;
					joint[c] = k;
					break search;
				}
			}
		}
		return new int[][]{parent, joint};
	}

	/** The offending pieces and everything below them in the tree (parents come before their children). */
	static boolean[] withChildren(boolean[] offending, int[] parent) {
		boolean[] drop = offending.clone();
		for (int i = 0; i < drop.length; i++) if (!drop[i] && parent[i] >= 0 && drop[parent[i]]) drop[i] = true;
		return drop;
	}

	private static final Field JUNCTIONS = field(PoolElementStructurePiece.class, "f_72602_", "junctions");

	@SuppressWarnings("unchecked")
	private static List<JigsawJunction> junctions(StructurePiece piece) {
		if (!(piece instanceof PoolElementStructurePiece pool)) return List.of();
		try {
			return (List<JigsawJunction>) JUNCTIONS.get(pool);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static Reach reach() {
		return new Reach(WorldgenConfig.earthBeardReach(), WorldgenConfig.earthBuryReach(), WorldgenConfig.earthMaxMoundHeight(),
				WorldgenConfig.earthWaterReach());
	}

	/** Water limits for one structure: the first matching override, else the global pair. */
	static Limits waterLimitsFor(Holder<Structure> structure) {
		Override o = WATER_OVERRIDES.match(structure);
		return o != null ? new Limits(o.a(), -1, o.b()) : new Limits(WorldgenConfig.waterMaxIntoGround(), -1, WorldgenConfig.waterMaxIntoAir());
	}

	// ── the hook ─────────────────────────────────────────────────────────────

	/** The water check, ready to run on a start. */
	private record Check(Limits limits, EarthFillLog log, Function<StructurePiece, long[]> blocks) {}

	/**
	 * ChunkGenerator#tryGenerateStructure, the start just generated: the start itself, or an invalid one
	 * when its foundation needs too much earth over water or air, or its water would replace too much dry
	 * ground or air (the caller then treats the pick as failed).
	 */
	public static StructureStart filter(StructureStart start, StructureSet.StructureSelectionEntry entry, ChunkGenerator generator,
			RandomState randomState, StructureTemplateManager templates, long seed, ChunkAccess chunk, ChunkPos chunkPos) {
		if (!start.isValid()) return start;
		Holder<Structure> structure = entry.structure();
		List<MetaStructure.PartPieces> parts = MetaStructure.parts(start);
		if (parts != null) return filterParts(start, parts, entry, generator, randomState, templates, seed, chunk, chunkPos);
		EarthCheck earth = earthCheck(structure, templates, generator.getSeaLevel());
		Check water = waterCheck(structure, templates);
		if (earth == null && water == null) return start;
		// a frequency roll that is going to veto this start anyway: don't spend the terrain reads on it
		if (StructureFrequencies.rejects(entry, seed, chunkPos, chunk)) return start;

		Terrain terrain = predicted(generator, randomState, chunk, WorldgenConfig.earthSampleGrid());
		if (earth != null) start = earth.prune(start, structure, terrain, chunkPos);
		if (earth != null && earth.cancels(start.getPieces(), structure, terrain, chunkPos)) return StructureStart.INVALID_START;
		if (water != null && cancels(water, start.getPieces(), structure, terrain, chunkPos)) return StructureStart.INVALID_START;
		return start;
	}

	/**
	 * How these pieces of this structure are pruned (see {@link Prune}). An override that lifts every limit is never
	 * pruned: it is only measured, under log = ALL too, so the log level never changes the world.
	 */
	private static Prune prunes(Holder<Structure> structure, List<StructurePiece> pieces) {
		return pruneMode(WorldgenConfig.earthPrune(), WorldgenConfig.earthPruneWetJigsaws(), structure.is(PRUNE), structure.is(NO_PRUNE),
				earthUnlimited(structure), structure.value().step() == GenerationStep.Decoration.SURFACE_STRUCTURES, pieces);
	}

	/** " (predicted)" on a line logged while PlacementPriority replays a neighbour on a scratch chunk (E10). */
	private static String predictedTag() {
		return PlacementPriority.predicting() ? " (predicted)" : "";
	}

	/**
	 * A meta structure: each structure part is checked as it would be alone (its own exempt and water-fill tags and
	 * limits, its own pieces), so a sunken ruin in a cluster passes exactly when it would pass on its own: a village or
	 * other jigsaw part is pruned first, anchor included, as it would be alone. With [worldgen.meta_structures]
	 * keepShortParts a satellite still over its limits is dropped from the meta; the anchor over its limits, or no
	 * satellite left, cancels the meta (the slot is then tried as the plain anchor). Without it one part over its
	 * limits cancels the whole meta.
	 */
	private static StructureStart filterParts(StructureStart start, List<MetaStructure.PartPieces> parts, StructureSet.StructureSelectionEntry entry,
			ChunkGenerator generator, RandomState randomState, StructureTemplateManager templates, long seed, ChunkAccess chunk, ChunkPos chunkPos) {
		boolean keepShort = WorldgenConfig.metaKeepShortParts();
		boolean anchored = start.getStructure() instanceof MetaStructure m && m.anchored();
		Terrain terrain = null;
		List<List<StructurePiece>> kept = new ArrayList<>(parts.size());
		List<String> dropped = new ArrayList<>();
		boolean changed = false;
		for (MetaStructure.PartPieces part : parts) {
			List<StructurePiece> pieces = part.pieces();
			EarthCheck earth = earthCheck(part.structure(), templates, generator.getSeaLevel());
			Check water = waterCheck(part.structure(), templates);
			if (earth != null || water != null) {
				if (terrain == null) {
					if (StructureFrequencies.rejects(entry, seed, chunkPos, chunk)) return start;
					terrain = predicted(generator, randomState, chunk, WorldgenConfig.earthSampleGrid());
				}
				if (earth != null) {
					List<StructurePiece> p = earth.prunePieces(pieces, part.structure(), terrain, chunkPos);
					if (p != pieces) { pieces = p; changed = true; }
				}
				boolean bad = earth != null && earth.cancels(pieces, part.structure(), terrain, chunkPos)
						|| water != null && cancels(water, pieces, part.structure(), terrain, chunkPos);
				if (bad) {
					boolean anchor = anchored && part.entry() == 0;
					if (!keepShort || anchor) return StructureStart.INVALID_START;
					dropped.add(idOf(part.structure()));
					pieces = null;
					changed = true;
				}
			}
			kept.add(pieces);
		}
		if (!changed) return start;
		int satellites = 0, left = 0;
		for (int i = 0; i < parts.size(); i++) {
			if (anchored && parts.get(i).entry() == 0) continue;
			satellites++;
			if (kept.get(i) != null) left++;
		}
		// nothing but the anchor left: the plain anchor is the same thing without the meta's name
		if (satellites > 0 && left == 0) return StructureStart.INVALID_START;
		if (!dropped.isEmpty() && WorldgenConfig.earthLog() != EarthFillLog.OFF) {
			LOG.info("[earth-fill] {} at chunk [{}, {}]{}: dropped {} of {} parts: {}", idOf(entry.structure()), chunkPos.x, chunkPos.z,
					predictedTag(), dropped.size(), parts.size(), String.join(", ", dropped));
		}
		return MetaStructure.withParts(start, parts, kept);
	}

	private static String idOf(Holder<Structure> structure) {
		return structure.unwrapKey().map(ResourceKey::location).map(ResourceLocation::toString).orElse("?");
	}

	/**
	 * The earth half, ready to run on a start: the pieces' earth, the structure's beard, limits by the start's size,
	 * the footprint water surplus (-1 = not checked) and how many layers the start height sinks the pieces (the
	 * footprint layer is that far above a piece's bottom).
	 */
	private record EarthCheck(EarthFillLog log, Beard beard, Function<StructurePiece, long[]> blocks, double surplus, int sink, int spanFloor,
			Function<StructurePiece, long[]> base) {

		/** Pieces with no earth join the footprint as spans (footprintAllPieces); spanFloor = Integer.MAX_VALUE: never. */
		boolean spans() { return spanFloor != Integer.MAX_VALUE && surplus >= 0; }

		boolean cancels(List<StructurePiece> startPieces, Holder<Structure> structure, Terrain terrain, ChunkPos chunkPos) {
			List<EarthPiece> pieces = new ArrayList<>(startPieces.size());
			List<Layer> layers = new ArrayList<>(startPieces.size());
			List<Span> spans = new ArrayList<>();
			List<BasePiece> bases = base == null ? List.of() : new ArrayList<>();   // beardFootprint
			for (StructurePiece piece : startPieces) {
				collect(piece, pieces, layers, spans);
				if (base != null && rigid(piece)) {
					long[] b = base.apply(piece);
					if (b.length > 0) bases.add(new BasePiece(b, piece.getBoundingBox().minY() + sink));
				}
			}
			if (pieces.isEmpty() && spans.isEmpty() && bases.isEmpty()) return false;
			int size = sizeOf(earthBlocks(pieces), WorldgenConfig.earthSizeBounds());
			EarthLimits limits = earthLimitsFor(structure, size);
			if (limits.none() && surplus < 0 && log != EarthFillLog.ALL) return false;
			EarthResult r = countEarth(pieces, terrain, limits, reach(), log != EarthFillLog.ALL);
			int baseMound = bases.isEmpty() || !r.complete() ? 0 : baseMound(bases, pieces, terrain, reach());
			r = withBaseMound(r, baseMound, limits);
			// the water share: only when the limits kept it (or everything is logged)
			Footprint f = surplus >= 0 && (!r.cancelled() || log == EarthFillLog.ALL) ? footprint(layers, spans, terrain, WorldgenConfig.earthBaseLayers()) : null;
			// an all-off override is only measured under log = ALL, never cancelled (as at log = CANCELLED, where it is skipped)
			boolean unlimited = earthUnlimited(structure);
			int minWater = WorldgenConfig.earthFootprintMinWater();
			boolean wet = !unlimited && f != null && f.tooWet(surplus, minWater);
			boolean cancelled = earthCancelled(unlimited, r, f, surplus, minWater);

			if (cancelled ? log != EarthFillLog.OFF : log == EarthFillLog.ALL) {
				String plus = r.complete() ? "" : "+";   // counting stopped at the first limit crossed
				String id = structure.unwrapKey().map(ResourceKey::location).map(ResourceLocation::toString).orElse("?");
				String share = f == null ? "" : ", footprint " + f.water() + " water / " + f.ground() + " ground (surplus limit " + surplus
						+ (wet ? ", too wet)" : f.water() < minWater && f.tooWet(surplus) ? ", under " + minWater + " water)" : ")");
				LOG.info("[earth-fill] {} at chunk [{}, {}]{} ({}, {}): {}{} earth, {}{} over water (limit {}), {}{} over air after beard (limit {}), "
								+ "{}{} mound columns{} (limit {}){} -> {}",
						id, chunkPos.x, chunkPos.z, predictedTag(), SIZES[size], beard.name().toLowerCase(java.util.Locale.ROOT), r.blocks(), plus, r.water(), plus,
						limits.water(), r.air(), plus, limits.air(), r.mound(), plus, baseMound > 0 ? " (" + baseMound + " under built floors)" : "", limits.mound(), share,
						cancelled ? "CANCELLED" : "kept");
			}
			return cancelled;
		}

		/**
		 * One piece into the lists: with earth, its earth at the sunk ground level and its layer; without, its box as a
		 * span (only for rigid pieces reaching above spanFloor: pieces wholly under the sea level are underground).
		 */
		void collect(StructurePiece piece, List<EarthPiece> pieces, List<Layer> layers, List<Span> spans) {
			long[] earth = blocks.apply(piece);
			BoundingBox box = piece.getBoundingBox();
			if (earth.length > 0) {
				// the beard's ground level is raised by the sink (TerrainAdaptation.rigid), so its reach starts there
				pieces.add(new EarthPiece(earth, box.minY() + sink, beard));
				layers.add(new Layer(box.minX(), box.minZ(), box.maxX(), box.maxZ(), box.minY() + sink));
			} else if (spans() && box.maxY() >= spanFloor && rigid(piece)) {
				spans.add(new Span(box.minX(), box.minZ(), box.maxX(), box.maxZ(), box.minY() + sink, box.maxY()));
			}
		}

		/**
		 * The start without its offending pieces and everything attached below them, or the start itself when none
		 * offends. The first piece (the town centre) is never dropped: the whole-start check judges it.
		 */
		StructureStart prune(StructureStart start, Holder<Structure> structure, Terrain terrain, ChunkPos chunkPos) {
			List<StructurePiece> pieces = start.getPieces();
			List<StructurePiece> kept = prunePieces(pieces, structure, terrain, chunkPos);
			if (kept == pieces) return start;
			return new StructureStart(start.getStructure(), start.getChunkPos(), start.getReferences(), new PiecesContainer(kept));
		}

		/**
		 * {@link #prune} on a piece list (a village or jigsaw part of a meta structure too), as {@link #prunes} picks:
		 * the same list when it isn't pruned or nothing offends.
		 */
		List<StructurePiece> prunePieces(List<StructurePiece> pieces, Holder<Structure> structure, Terrain terrain, ChunkPos chunkPos) {
			Prune mode = prunes(structure, pieces);
			if (mode == Prune.NONE) return pieces;
			List<String> why = new ArrayList<>();
			int minWet = WorldgenConfig.earthPruneMinWetColumns();
			Function<StructurePiece, Footprint> wet = surplus < 0 || minWet < 0 ? null : piece -> {
				List<EarthPiece> e = new ArrayList<>(1);
				List<Layer> l = new ArrayList<>(1);
				List<Span> sp = new ArrayList<>(1);
				collect(piece, e, l, sp);
				return footprint(l, sp, terrain, WorldgenConfig.earthBaseLayers());
			};
			if (mode == Prune.WET && wet == null) return pieces;
			List<StructurePiece> kept = pruned(pieces, blocks, beard, sink, terrain, mode == Prune.FULL, wet, surplus, minWet, why);
			if (kept != pieces && log != EarthFillLog.OFF) {
				LOG.info("[earth-fill] {} at chunk [{}, {}]{}: pruned {} of {} pieces ({}); over the piece limits: {}", idOf(structure), chunkPos.x, chunkPos.z,
						predictedTag(), pieces.size() - kept.size(), pieces.size(), mode == Prune.FULL ? "village" : "wet only", String.join(", ", why));
			}
			return kept;
		}
	}

	/** {@link #pruned(List, Function, Beard, int, Terrain, boolean, Function, double, int, List)} with the piece limits (FULL). */
	static List<StructurePiece> pruned(List<StructurePiece> pieces, Function<StructurePiece, long[]> blocks, Beard beard, int sink, Terrain terrain,
			Function<StructurePiece, Footprint> wet, double surplus, int minWet, List<String> why) {
		return pruned(pieces, blocks, beard, sink, terrain, true, wet, surplus, minWet, why);
	}

	/**
	 * The pieces without the offending ones and everything attached below them (the same list when none offends);
	 * the kept parent of a dropped piece loses its junction to it. The first piece is never dropped. {@code why}
	 * gets a line per offending piece. Without {@code limits} only the footprint water share drops a piece (WET).
	 */
	static List<StructurePiece> pruned(List<StructurePiece> pieces, Function<StructurePiece, long[]> blocks, Beard beard, int sink, Terrain terrain,
			boolean limits, Function<StructurePiece, Footprint> wet, double surplus, int minWet, List<String> why) {
		int n = pieces.size();
		if (n < 2) return pieces;
		int maxHanging = limits ? WorldgenConfig.earthPruneMaxHanging() : -1, maxMound = limits ? WorldgenConfig.earthPruneMaxMound() : -1;
		if (maxHanging < 0 && maxMound < 0 && wet == null) return pieces;
		Reach reach = reach();
		EarthLimits open = new EarthLimits(-1, -1, -1);
		boolean[] offending = new boolean[n];
		boolean any = false;
		for (int i = 1; i < n; i++) {
			StructurePiece piece = pieces.get(i);
			long[] earth = maxHanging < 0 && maxMound < 0 ? new long[0] : blocks.apply(piece);
			if (earth.length > 0) {
				// at the sunk ground level, as the whole-start check (TODO structures/earthfill #13 E3)
				EarthResult r = countEarth(List.of(new EarthPiece(earth, piece.getBoundingBox().minY() + sink, beard)), terrain, open, reach, false);
				if (over(r.water() + r.air(), maxHanging) || over(r.mound(), maxMound)) {
					offending[i] = any = true;
					why.add(pieceName(piece) + " (" + r.water() + " water, " + r.air() + " air, " + r.mound() + " mound)");
					continue;
				}
			}
			Footprint f = wet == null ? null : wet.apply(piece);
			if (f != null && f.water() >= minWet && f.tooWet(surplus)) {
				offending[i] = any = true;
				why.add(pieceName(piece) + " (footprint " + f.water() + " water / " + f.ground() + " ground)");
			}
		}
		if (!any) return pieces;

		List<BoundingBox> boxes = new ArrayList<>(n);
		List<List<Joint>> joints = new ArrayList<>(n);
		for (StructurePiece piece : pieces) {
			boxes.add(piece.getBoundingBox());
			List<Joint> js = new ArrayList<>();
			for (JigsawJunction j : junctions(piece)) js.add(new Joint(j.getSourceX(), j.getSourceZ(), j.getDeltaY()));
			joints.add(js);
		}
		int[][] tree = links(boxes, joints);
		boolean[] drop = withChildren(offending, tree[0]);
		List<StructurePiece> kept = new ArrayList<>(n);
		for (int i = 0; i < n; i++) if (!drop[i]) kept.add(pieces.get(i));
		List<List<JigsawJunction>> live = new ArrayList<>(n);
		for (StructurePiece piece : pieces) live.add(junctions(piece));
		forgetJunctions(boxes, joints, tree, drop, live);
		return kept;
	}

	/**
	 * Every kept piece forgets its junctions to dropped pieces, so the beard adds no ground at a dead street end: a
	 * linked child's junction by identity (any junction order, custom placers too), and for a dropped piece the tree
	 * could not link (parent -1), any kept piece's junction whose point lies in that piece's box and in no kept box.
	 * {@code live} holds each piece's own junction list (removed from in place), indexed as {@code joints}.
	 */
	static <J> void forgetJunctions(List<BoundingBox> boxes, List<List<Joint>> joints, int[][] tree, boolean[] drop, List<List<J>> live) {
		int n = boxes.size();
		List<java.util.Set<J>> gone = new ArrayList<>(n);
		for (int i = 0; i < n; i++) gone.add(null);
		for (int i = 1; i < n; i++) {
			if (!drop[i]) continue;
			int p = tree[0][i];
			if (p >= 0) {
				if (!drop[p] && tree[1][i] < live.get(p).size()) mark(gone, p, live.get(p).get(tree[1][i]));
				continue;
			}
			// unlinked: drop any kept junction that points into this piece and nowhere kept
			BoundingBox b = boxes.get(i);
			for (int k = 0; k < n; k++) {
				if (drop[k]) continue;
				List<Joint> kj = joints.get(k);
				for (int j = 0; j < kj.size() && j < live.get(k).size(); j++) {
					Joint at = kj.get(j);
					if (!inColumns(b, at.x(), at.z()) || inKept(boxes, drop, at.x(), at.z())) continue;
					mark(gone, k, live.get(k).get(j));
				}
			}
		}
		for (int k = 0; k < n; k++) {
			java.util.Set<J> g = gone.get(k);
			if (g != null) live.get(k).removeIf(g::contains);
		}
	}

	private static <J> void mark(List<java.util.Set<J>> gone, int k, J junction) {
		if (gone.get(k) == null) gone.set(k, java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>()));
		gone.get(k).add(junction);
	}

	private static boolean inColumns(BoundingBox b, int x, int z) {
		return x >= b.minX() && x <= b.maxX() && z >= b.minZ() && z <= b.maxZ();
	}

	private static boolean inKept(List<BoundingBox> boxes, boolean[] drop, int x, int z) {
		for (int k = 0; k < boxes.size(); k++) if (!drop[k] && inColumns(boxes.get(k), x, z)) return true;
		return false;
	}

	/** A piece for the log: its template (pool pieces) or its class. */
	private static String pieceName(StructurePiece piece) {
		if (piece instanceof PoolElementStructurePiece pool && pool.getElement() instanceof SinglePoolElement single) {
			try {
				Object t = SINGLE_TEMPLATE.get(single);
				if (t instanceof Either<?, ?> either) return either.map(String::valueOf, x -> "template");
			} catch (IllegalAccessException ignored) {
			}
		}
		return piece.getClass().getSimpleName();
	}

	private static EarthCheck earthCheck(Holder<Structure> structure, StructureTemplateManager templates, int seaLevel) {
		if (!WorldgenConfig.earthFillEnabled() || structure.is(EXEMPT)) return null;
		EarthFillLog log = WorldgenConfig.earthLog();
		if (log != EarthFillLog.ALL && earthUnlimited(structure)) return null;
		// a stone base counts too: its size and its overhang (02_new_mods_tests.md 4.2: the drum tower's 49x45
		// andesite plate was "small, 10 earth" and could hang over a cliff unchecked)
		// and a sandstone or terracotta floor in the desert and badlands (2026-11-02, earth_base_scan.py)
		List<TagKey<Block>> earth = withBases(earthTags(), structure.is(STONE_BASE), structure.is(SAND_BASE));
		if (earth.isEmpty()) return null;
		int layers = WorldgenConfig.earthBaseLayers();
		boolean trimmed = WorldgenConfig.templateTrimEnabled() && TerrainAdaptation.trims(structure.value());
		// water structures and stilted huts stand in water on purpose: no footprint share for them
		double surplus = structure.is(WATER_STRUCTURES) || structure.is(WET_OK) ? -1 : WorldgenConfig.earthMaxWaterSurplus();
		// pieces without earth join the footprint for surface structures only (mineshafts, strongholds and fortresses
		// are underground: no terrain reads for them), and only where they reach above the sea level less the base layers
		boolean spans = WorldgenConfig.earthFootprintAllPieces() && structure.value().step() == GenerationStep.Decoration.SURFACE_STRUCTURES;
		int spanFloor = spans ? seaLevel - layers : Integer.MAX_VALUE;
		// beardFootprint: the solid bottom of bearded pieces counts toward the mound limit too (TODO structures/terrain #2)
		Function<StructurePiece, long[]> base = WorldgenConfig.earthBeardFootprint() && beardOf(structure.value()) == Beard.BEARD
				? piece -> pieceBlocks(piece, templates, t -> localBase(t, layers, trimmed)) : null;
		return new EarthCheck(log, beardOf(structure.value()), piece -> pieceEarth(piece, templates, earth, layers, trimmed), surplus,
				TerrainAdaptation.sink(structure.value()), spanFloor, base);
	}

	private static Check waterCheck(Holder<Structure> structure, StructureTemplateManager templates) {
		if (!WorldgenConfig.waterFillEnabled() || !structure.is(WATER_STRUCTURES)) return null;
		EarthFillLog log = WorldgenConfig.waterLog();
		Limits limits = waterLimitsFor(structure);
		if (limits.none() && log != EarthFillLog.ALL) return null;
		return new Check(limits, log, piece -> pieceWater(piece, templates));
	}

	private static boolean cancels(Check check, List<StructurePiece> startPieces, Holder<Structure> structure, Terrain terrain, ChunkPos chunkPos) {
		Iterable<long[]> pieces = () -> new Iterator<>() {
			final Iterator<StructurePiece> it = startPieces.iterator();
			public boolean hasNext() { return it.hasNext(); }
			public long[] next() {
				if (!hasNext()) throw new NoSuchElementException();
				return check.blocks().apply(it.next());
			}
		};
		Result r = count(pieces, terrain, check.limits(), check.log() != EarthFillLog.ALL);
		if (r.blocks() == 0) return false;

		if (r.cancelled() ? check.log() != EarthFillLog.OFF : check.log() == EarthFillLog.ALL) {
			String plus = r.complete() ? "" : "+";   // counting stopped at the first limit crossed
			String id = structure.unwrapKey().map(ResourceKey::location).map(ResourceLocation::toString).orElse("?");
			Limits l = check.limits();
			LOG.info("[water-fill] {} at chunk [{}, {}]{}: {}{} water blocks, {}{} into dry ground (limit {}), {}{} into air (limit {}) -> {}",
					id, chunkPos.x, chunkPos.z, predictedTag(), r.blocks(), plus, r.ground(), plus, l.ground(), r.air(), plus, l.air(), r.cancelled() ? "CANCELLED" : "kept");
		}
		return r.cancelled();
	}
}
