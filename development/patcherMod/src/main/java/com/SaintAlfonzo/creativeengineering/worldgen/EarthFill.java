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
import net.minecraft.tags.FluidTags;
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
 * shared with other structures falls through to the next pick, as for a wrong biome).
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

	private EarthFill() {}

	// ── what is where ────────────────────────────────────────────────────────

	enum Kind { SOLID, WATER, AIR }

	/** What the terrain holds at a block position. */
	interface Terrain { Kind at(int x, int y, int z); }

	/** The outcome of one structure. {@code complete} is false when counting stopped at the first limit crossed. */
	record Result(int earth, int water, int air, boolean cancelled, boolean complete) {}

	/**
	 * The level-free core: each long[] is one piece's earth positions (BlockPos.asLong). A position
	 * appearing in two pieces counts once. A limit below 0 is no limit. With stopEarly, counting ends
	 * at the first limit crossed.
	 */
	static Result count(Iterable<long[]> pieces, Terrain terrain, int maxWater, int maxAir, boolean stopEarly) {
		LongOpenHashSet seen = new LongOpenHashSet();
		int earth = 0, water = 0, air = 0;
		for (long[] piece : pieces) {
			for (long p : piece) {
				if (!seen.add(p)) continue;
				earth++;
				switch (terrain.at(BlockPos.getX(p), BlockPos.getY(p), BlockPos.getZ(p))) {
					case WATER -> water++;
					case AIR -> air++;
					default -> { }
				}
				if (stopEarly && (over(water, maxWater) || over(air, maxAir))) return new Result(earth, water, air, true, false);
			}
		}
		return new Result(earth, water, air, over(water, maxWater) || over(air, maxAir), true);
	}

	private static boolean over(int n, int limit) { return limit >= 0 && n > limit; }

	// ── the earth blocks of a piece ──────────────────────────────────────────

	private static Field field(Class<?> c, String srg, String mojmap) {
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
	private static final Field SINGLE_TEMPLATE = field(SinglePoolElement.class, "f_210411_", "template");
	private static final Field LIST_ELEMENTS = field(ListPoolElement.class, "f_210360_", "elements");

	/** Local positions of a template's earth blocks (bottom layers, first palette), by template; cleared with the config. */
	private static final Map<StructureTemplate, long[]> LOCAL = new ConcurrentHashMap<>();
	private static volatile String localKey = "";

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) { LOCAL.clear(); }

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) { LOCAL.clear(); }

	@SuppressWarnings("unchecked")
	static long[] localEarth(StructureTemplate template, List<TagKey<Block>> earth, int layers) {
		String key = layers + "|" + earth;
		if (!key.equals(localKey)) { LOCAL.clear(); localKey = key; }
		return LOCAL.computeIfAbsent(template, t -> {
			LongArrayList out = new LongArrayList();
			try {
				List<?> palettes = (List<?>) PALETTES.get(t);
				if (!palettes.isEmpty()) {
					for (StructureTemplate.StructureBlockInfo info : ((StructureTemplate.Palette) palettes.get(0)).blocks()) {
						if (info.pos.getY() >= layers || info.pos.getY() < 0) continue;
						if (isEarth(info.state, earth)) out.add(info.pos.asLong());
					}
				}
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
			return out.toLongArray();
		});
	}

	static boolean isEarth(BlockState state, List<TagKey<Block>> earth) {
		for (TagKey<Block> tag : earth) if (state.is(tag)) return true;
		return false;
	}

	/** World positions of one template's earth blocks, placed at origin with these settings. */
	static void addTemplate(StructureTemplate template, BlockPos origin, StructurePlaceSettings settings, List<TagKey<Block>> earth, int layers, LongArrayList out) {
		for (long l : localEarth(template, earth, layers)) {
			out.add(StructureTemplate.calculateRelativePosition(settings, BlockPos.of(l)).offset(origin).asLong());
		}
	}

	@SuppressWarnings("unchecked")
	static void addElement(StructurePoolElement element, BlockPos origin, Rotation rotation, StructureTemplateManager templates,
			List<TagKey<Block>> earth, int layers, LongArrayList out) {
		if (element.getProjection() == StructureTemplatePool.Projection.TERRAIN_MATCHING) return;
		try {
			if (element instanceof ListPoolElement list) {
				for (StructurePoolElement sub : (List<StructurePoolElement>) LIST_ELEMENTS.get(list)) addElement(sub, origin, rotation, templates, earth, layers, out);
			} else if (element instanceof SinglePoolElement single) {
				Either<ResourceLocation, StructureTemplate> either = (Either<ResourceLocation, StructureTemplate>) SINGLE_TEMPLATE.get(single);
				StructureTemplate template = either.map(rl -> templates.getOrCreate(rl), Function.identity());
				addTemplate(template, origin, new StructurePlaceSettings().setRotation(rotation), earth, layers, out);
			}
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/** World positions of a piece's earth blocks; empty for pieces that aren't templates (or follow the terrain). */
	static long[] pieceEarth(StructurePiece piece, StructureTemplateManager templates, List<TagKey<Block>> earth, int layers) {
		LongArrayList out = new LongArrayList();
		if (piece instanceof PoolElementStructurePiece pool) {
			addElement(pool.getElement(), pool.getPosition(), pool.getRotation(), templates, earth, layers, out);
		} else if (piece instanceof TemplateStructurePiece tp) {
			addTemplate(tp.template(), tp.templatePosition(), tp.placeSettings(), earth, layers, out);
		}
		return out.toLongArray();
	}

	// ── predicted terrain ────────────────────────────────────────────────────

	/** The generator's noise terrain, read once per grid x grid cell and cached for this start. */
	static Terrain predicted(ChunkGenerator generator, RandomState randomState, LevelHeightAccessor level, int grid) {
		Long2ObjectOpenHashMap<NoiseColumn> columns = new Long2ObjectOpenHashMap<>();
		return (x, y, z) -> {
			int cx = Math.floorDiv(x, grid) * grid + grid / 2, cz = Math.floorDiv(z, grid) * grid + grid / 2;
			long key = ChunkPos.asLong(cx, cz);
			NoiseColumn column = columns.get(key);
			if (column == null) columns.put(key, column = generator.getBaseColumn(cx, cz, level, randomState));
			BlockState state = column.getBlock(y);
			if (state.isAir()) return Kind.AIR;
			return state.getFluidState().is(FluidTags.WATER) ? Kind.WATER : Kind.SOLID;
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

	/** Water and air limits, -1 = no limit. */
	record Limits(int water, int air) {}

	/** "id=water,air" or "#tag=water,air". */
	record Override(String key, int water, int air) {}

	private static volatile List<? extends String> overridesSource;
	private static volatile List<Override> parsedOverrides = List.of();

	static List<Override> parseOverrides(List<? extends String> src) {
		List<Override> out = new ArrayList<>();
		for (String line : src) {
			int eq = line.lastIndexOf('=');
			String[] nums = eq < 0 ? new String[0] : line.substring(eq + 1).split(",");
			try {
				if (eq <= 0 || nums.length != 2) throw new NumberFormatException();
				out.add(new Override(line.substring(0, eq).trim(), Integer.parseInt(nums[0].trim()), Integer.parseInt(nums[1].trim())));
			} catch (NumberFormatException e) {
				LOG.warn("[earth-fill] overrides: \"{}\" is not \"<structure id or #tag>=<water>,<air>\", skipped", line);
			}
		}
		return List.copyOf(out);
	}

	private static List<Override> overrides() {
		List<? extends String> src = WorldgenConfig.earthOverrides();
		if (src != overridesSource) {
			parsedOverrides = parseOverrides(src);
			overridesSource = src;
		}
		return parsedOverrides;
	}

	/** The limits for one structure: the first matching override, else the global pair. */
	static Limits limitsFor(Holder<Structure> structure) {
		for (Override o : overrides()) {
			boolean hit;
			if (o.key().startsWith("#")) {
				ResourceLocation rl = ResourceLocation.tryParse(o.key().substring(1));
				hit = rl != null && structure.is(TagKey.create(Registry.STRUCTURE_REGISTRY, rl));
			} else {
				hit = structure.unwrapKey().map(k -> k.location().toString().equals(o.key())).orElse(false);
			}
			if (hit) return new Limits(o.water(), o.air());
		}
		return new Limits(WorldgenConfig.earthMaxOverWater(), WorldgenConfig.earthMaxOverAir());
	}

	// ── the hook ─────────────────────────────────────────────────────────────

	/**
	 * ChunkGenerator#tryGenerateStructure, the start just generated: the start itself, or an invalid one
	 * when its foundation needs too much earth over water or air (the caller then treats the pick as failed).
	 */
	public static StructureStart filter(StructureStart start, StructureSet.StructureSelectionEntry entry, ChunkGenerator generator,
			RandomState randomState, StructureTemplateManager templates, long seed, ChunkAccess chunk, ChunkPos chunkPos) {
		if (!start.isValid() || !WorldgenConfig.earthFillEnabled()) return start;
		Holder<Structure> structure = entry.structure();
		if (structure.is(EXEMPT)) return start;
		EarthFillLog log = WorldgenConfig.earthLog();
		Limits limits = limitsFor(structure);
		if (limits.water() < 0 && limits.air() < 0 && log != EarthFillLog.ALL) return start;
		List<TagKey<Block>> earth = earthTags();
		if (earth.isEmpty()) return start;
		// a frequency roll that is going to veto this start anyway: don't spend the terrain reads on it
		if (StructureFrequencies.rejects(entry, seed, chunkPos, chunk)) return start;

		int layers = WorldgenConfig.earthBaseLayers();
		Iterable<long[]> pieces = () -> new Iterator<>() {
			final Iterator<StructurePiece> it = start.getPieces().iterator();
			public boolean hasNext() { return it.hasNext(); }
			public long[] next() {
				if (!hasNext()) throw new NoSuchElementException();
				return pieceEarth(it.next(), templates, earth, layers);
			}
		};
		Result r = count(pieces, predicted(generator, randomState, chunk, WorldgenConfig.earthSampleGrid()),
				limits.water(), limits.air(), log != EarthFillLog.ALL);
		if (r.earth() == 0) return start;

		if (r.cancelled() ? log != EarthFillLog.OFF : log == EarthFillLog.ALL) {
			String plus = r.complete() ? "" : "+";   // counting stopped at the first limit crossed
			LOG.info("[earth-fill] {} at chunk [{}, {}]: {}{} earth base blocks, {}{} over water (limit {}), {}{} over air (limit {}) -> {}",
					structure.unwrapKey().map(ResourceKey::location).map(ResourceLocation::toString).orElse("?"), chunkPos.x, chunkPos.z,
					r.earth(), plus, r.water(), plus, limits.water(), r.air(), plus, limits.air(), r.cancelled() ? "CANCELLED" : "kept");
		}
		return r.cancelled() ? StructureStart.INVALID_START : start;
	}
}
