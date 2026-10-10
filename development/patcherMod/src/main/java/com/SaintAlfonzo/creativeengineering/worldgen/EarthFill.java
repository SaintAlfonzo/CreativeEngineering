package com.SaintAlfonzo.creativeengineering.worldgen;

import com.SaintAlfonzo.creativeengineering.CEConfig.EarthFillLog;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
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
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
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
 * or something solid. Earth in water counts toward {@code maxOverWater}, earth in air toward
 * {@code maxOverAir}; past either limit the whole start is dropped and the slot stays empty (a slot
 * shared with other structures falls through to the next pick, as for a wrong biome). For a structure in
 * {@code creativeengineering:template_trim} only earth that {@link TemplateTrim} still places counts.
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
 * and {@code /locate} don't go through this check (the same as the biome rules).
 */
public final class EarthFill {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/earth-fill");

	static final TagKey<Structure> EXEMPT = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "earth_fill/exempt"));
	static final TagKey<Structure> WATER_STRUCTURES = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "water_fill/structures"));

	private EarthFill() {}

	// ── what is where ────────────────────────────────────────────────────────

	/** SOLID is dry ground; SEABED is a solid block whose column is topped by water (sea, lake or river bed). */
	enum Kind { SOLID, SEABED, WATER, AIR }

	/** What the terrain holds at a block position. */
	interface Terrain { Kind at(int x, int y, int z); }

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

	/** Local positions of a template's earth blocks (bottom layers, first palette), by template; cleared with the config. */
	private static final Map<StructureTemplate, long[]> LOCAL = new ConcurrentHashMap<>();
	private static volatile String localKey = "";
	/** The same for structures in creativeengineering:template_trim: earth in pad columns the trim drops isn't placed. */
	private static final Map<StructureTemplate, long[]> LOCAL_TRIMMED = new ConcurrentHashMap<>();
	/** Local positions of a template's water blocks (all layers, first palette), by template. */
	private static final Map<StructureTemplate, long[]> LOCAL_WATER = new ConcurrentHashMap<>();

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) { LOCAL.clear(); LOCAL_TRIMMED.clear(); LOCAL_WATER.clear(); }

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) { LOCAL.clear(); LOCAL_TRIMMED.clear(); }

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
		String key = layers + "|" + earth;
		if (!key.equals(localKey)) { LOCAL.clear(); LOCAL_TRIMMED.clear(); localKey = key; }
		return (trimmed ? LOCAL_TRIMMED : LOCAL).computeIfAbsent(template, t -> {
			TemplateTrim.Columns columns = trimmed ? TemplateTrim.columns(t) : null;
			return localBlocks(t, info -> info.pos.getY() >= 0 && info.pos.getY() < layers && isEarth(info.state, earth)
					&& (columns == null || columns.placed(info.pos.getX(), info.pos.getZ())));
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

	/** One predicted column and, once asked, whether its topmost non-air block is water. */
	private static final class Column {
		final NoiseColumn blocks;
		Boolean underWater;

		Column(NoiseColumn blocks) { this.blocks = blocks; }

		boolean underWater(LevelHeightAccessor level) {
			if (underWater == null) underWater = topIsWater(blocks, level);
			return underWater;
		}
	}

	/** Whether the topmost non-air block of a column is water (a sea, lake or river), not dry land. */
	static boolean topIsWater(NoiseColumn column, LevelHeightAccessor level) {
		for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
			BlockState state = column.getBlock(y);
			if (!state.isAir()) return isWater(state);
		}
		return false;
	}

	/** The generator's noise terrain, read once per grid x grid cell and cached for this start. */
	static Terrain predicted(ChunkGenerator generator, RandomState randomState, LevelHeightAccessor level, int grid) {
		Long2ObjectOpenHashMap<Column> columns = new Long2ObjectOpenHashMap<>();
		return (x, y, z) -> {
			int cx = Math.floorDiv(x, grid) * grid + grid / 2, cz = Math.floorDiv(z, grid) * grid + grid / 2;
			long key = ChunkPos.asLong(cx, cz);
			Column column = columns.get(key);
			if (column == null) columns.put(key, column = new Column(generator.getBaseColumn(cx, cz, level, randomState)));
			BlockState state = column.blocks.getBlock(y);
			if (state.isAir()) return Kind.AIR;
			if (isWater(state)) return Kind.WATER;
			return column.underWater(level) ? Kind.SEABED : Kind.SOLID;
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

	/** "id=a,b" or "#tag=a,b": earth fill reads (water, air), water fill (ground, air). */
	record Override(String key, int a, int b) {}

	static List<Override> parseOverrides(String system, List<? extends String> src) {
		List<Override> out = new ArrayList<>();
		for (String line : src) {
			int eq = line.lastIndexOf('=');
			String[] nums = eq < 0 ? new String[0] : line.substring(eq + 1).split(",");
			try {
				if (eq <= 0 || nums.length != 2) throw new NumberFormatException();
				out.add(new Override(line.substring(0, eq).trim(), Integer.parseInt(nums[0].trim()), Integer.parseInt(nums[1].trim())));
			} catch (NumberFormatException e) {
				LOG.warn("[{}] overrides: \"{}\" is not \"<structure id or #tag>=<number>,<number>\", skipped", system, line);
			}
		}
		return List.copyOf(out);
	}

	/** One config overrides list, parsed again whenever the config hands out a new list. */
	private static final class Overrides {
		private final String system;
		private final Supplier<List<? extends String>> config;
		private volatile List<? extends String> source;
		private volatile List<Override> parsed = List.of();

		Overrides(String system, Supplier<List<? extends String>> config) { this.system = system; this.config = config; }

		/** The first entry matching the structure, or null. */
		Override match(Holder<Structure> structure) {
			List<? extends String> src = config.get();
			if (src != source) {
				parsed = parseOverrides(system, src);
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

	private static final Overrides EARTH_OVERRIDES = new Overrides("earth-fill", WorldgenConfig::earthOverrides);
	private static final Overrides WATER_OVERRIDES = new Overrides("water-fill", WorldgenConfig::waterOverrides);

	/** Earth limits for one structure: the first matching override, else the global pair. */
	static Limits limitsFor(Holder<Structure> structure) {
		Override o = EARTH_OVERRIDES.match(structure);
		return o != null ? new Limits(-1, o.a(), o.b()) : new Limits(-1, WorldgenConfig.earthMaxOverWater(), WorldgenConfig.earthMaxOverAir());
	}

	/** Water limits for one structure: the first matching override, else the global pair. */
	static Limits waterLimitsFor(Holder<Structure> structure) {
		Override o = WATER_OVERRIDES.match(structure);
		return o != null ? new Limits(o.a(), -1, o.b()) : new Limits(WorldgenConfig.waterMaxIntoGround(), -1, WorldgenConfig.waterMaxIntoAir());
	}

	// ── the hook ─────────────────────────────────────────────────────────────

	/** One of the two checks, ready to run on a start. */
	private record Check(String name, Limits limits, EarthFillLog log, Function<StructurePiece, long[]> blocks) {}

	/**
	 * ChunkGenerator#tryGenerateStructure, the start just generated: the start itself, or an invalid one
	 * when its foundation needs too much earth over water or air, or its water would replace too much dry
	 * ground or air (the caller then treats the pick as failed).
	 */
	public static StructureStart filter(StructureStart start, StructureSet.StructureSelectionEntry entry, ChunkGenerator generator,
			RandomState randomState, StructureTemplateManager templates, long seed, ChunkAccess chunk, ChunkPos chunkPos) {
		if (!start.isValid()) return start;
		Holder<Structure> structure = entry.structure();
		Check earth = earthCheck(structure, templates), water = waterCheck(structure, templates);
		if (earth == null && water == null) return start;
		// a frequency roll that is going to veto this start anyway: don't spend the terrain reads on it
		if (StructureFrequencies.rejects(entry, seed, chunkPos, chunk)) return start;

		Terrain terrain = predicted(generator, randomState, chunk, WorldgenConfig.earthSampleGrid());
		for (Check check : new Check[]{earth, water}) {
			if (check != null && cancels(check, start, structure, terrain, chunkPos)) return StructureStart.INVALID_START;
		}
		return start;
	}

	private static Check earthCheck(Holder<Structure> structure, StructureTemplateManager templates) {
		if (!WorldgenConfig.earthFillEnabled() || structure.is(EXEMPT)) return null;
		EarthFillLog log = WorldgenConfig.earthLog();
		Limits limits = limitsFor(structure);
		if (limits.none() && log != EarthFillLog.ALL) return null;
		List<TagKey<Block>> earth = earthTags();
		if (earth.isEmpty()) return null;
		int layers = WorldgenConfig.earthBaseLayers();
		boolean trimmed = WorldgenConfig.templateTrimEnabled() && TerrainAdaptation.trims(structure.value());
		return new Check("earth-fill", limits, log, piece -> pieceEarth(piece, templates, earth, layers, trimmed));
	}

	private static Check waterCheck(Holder<Structure> structure, StructureTemplateManager templates) {
		if (!WorldgenConfig.waterFillEnabled() || !structure.is(WATER_STRUCTURES)) return null;
		EarthFillLog log = WorldgenConfig.waterLog();
		Limits limits = waterLimitsFor(structure);
		if (limits.none() && log != EarthFillLog.ALL) return null;
		return new Check("water-fill", limits, log, piece -> pieceWater(piece, templates));
	}

	private static boolean cancels(Check check, StructureStart start, Holder<Structure> structure, Terrain terrain, ChunkPos chunkPos) {
		Iterable<long[]> pieces = () -> new Iterator<>() {
			final Iterator<StructurePiece> it = start.getPieces().iterator();
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
			String verdict = r.cancelled() ? "CANCELLED" : "kept";
			if (check.name().equals("earth-fill")) {
				LOG.info("[earth-fill] {} at chunk [{}, {}]: {}{} earth base blocks, {}{} over water (limit {}), {}{} over air (limit {}) -> {}",
						id, chunkPos.x, chunkPos.z, r.blocks(), plus, r.water(), plus, l.water(), r.air(), plus, l.air(), verdict);
			} else {
				LOG.info("[water-fill] {} at chunk [{}, {}]: {}{} water blocks, {}{} into dry ground (limit {}), {}{} into air (limit {}) -> {}",
						id, chunkPos.x, chunkPos.z, r.blocks(), plus, r.ground(), plus, l.ground(), r.air(), plus, l.air(), verdict);
			}
		}
		return r.cancelled();
	}
}
