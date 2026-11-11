package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CreativeWorldgen;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Per-structure terrain adaptation, chosen by structure tags instead of each structure's own json.
 *
 * <p><b>The tags.</b> {@code creativeworldgen:terrain_adaptation/<mode>} (kubejs worldgen/terrain_adaptation.js)
 * replaces the structure's own {@code terrain_adaptation}: {@code none}, {@code bury}, {@code beard_thin},
 * {@code beard_box} are vanilla's, {@code soft} is the pack's own (below). mixin/StructureTerrainAdaptationMixin
 * answers Structure#terrainAdaptation from this table, so every vanilla use follows it: the Beardifier, and the
 * 12-block reference margin a terrain-adapted start gets. A structure in two mode tags takes the first in
 * {@link Mode} order and is logged. Structures whose class answers terrainAdaptation itself (YUNG's jigsaw
 * structures, which have their own enhanced adaptation) can't be changed this way; that is logged too.
 * {@code creativeworldgen:template_trim} is read by {@link TemplateTrim}.
 *
 * <p><b>Sunk starts.</b> The beard (vanilla's and the soft one) levels the ground to a piece's bottom layer: right
 * for a jigsaw start set onto the heightmap with start height 0, whose bottom layer replaces the surface block. A
 * start height below 0 sinks the template that many layers into the ground (Campsite Structures' camps sit on a dirt
 * dome 4 layers deep under their grass layer), and the beard then digs the ground down to the dome's
 * bottom around it: the buried layers stand out of a flat ring as a plinth. For a structure in
 * {@code creativeworldgen:terrain_adaptation/sunk_start} with a constant start height below 0 and a heightmap
 * projection, every piece's beard is raised by that many layers ({@link #sink}), so it levels the ground to the
 * surface the start was projected to. Opt-in: other mods sink starts with beard_box on purpose (Philip's crypts).
 *
 * <p><b>Why soft.</b> Vanilla's beard (Beardifier, beard_thin) fills under and carves above the piece's whole
 * bounding box, then fades out over about 8 blocks of straight-line distance from that box. A small structure
 * whose template carries a flat pad or saved air therefore sits on a flat rectangle the size of the whole
 * template plus an 8-block skirt, cut into a curved hillside. The soft beard has the same vertical profile (the
 * same fill under the floor and carve above it, so the structure stands on ground the way beard_thin makes it),
 * but:
 * <ul>
 *   <li>it covers the template's footprint, not its bounding box: the columns that place a block, or with
 *       template_trim only the built ones (see {@link TemplateTrim.Columns}), so it follows the building's
 *       outline;</li>
 *   <li>the skirt is {@link #MIN_SKIRT} blocks plus [worldgen.terrain_adaptation] skirtBump (vanilla's is 8):
 *       vanilla's horizontal falloff, squeezed by {@code 8 / skirt}. At skirt 8 and a box-shaped footprint it is
 *       exactly vanilla's beard_thin.</li>
 * </ul>
 * Pieces that aren't templates (or whose template can't be read) get the soft profile around their bounding
 * box instead: still the short round skirt, just a rectangular footprint.
 *
 * <p><b>Hooks</b> (mixin/BeardifierSoftMixin). Beardifier#forStructuresInChunk builds one {@code Rigid} per
 * nearby piece; for a soft structure's piece the Rigid gets terrain adjustment NONE (vanilla adds nothing for
 * it) and a {@link SoftBox} as its box, which carries the footprint. Beardifier#compute then adds every
 * SoftBox's density after its own rigid loop. Keeping the soft pieces inside vanilla's own rigid list matters:
 * YUNG's API replaces the Beardifier with a new one built from that list, so anything stored on the Beardifier
 * itself would be lost.
 */
public final class TerrainAdaptation {

	private static final Logger LOG = LogManager.getLogger("creativeworldgen/terrain-adaptation");

	/** Tag modes, in precedence order. SOFT is a beard_thin to every vanilla caller; only the Beardifier hook tells it apart. */
	public enum Mode {
		NONE("none", TerrainAdjustment.NONE),
		BURY("bury", TerrainAdjustment.BURY),
		BEARD_BOX("beard_box", TerrainAdjustment.BEARD_BOX),
		BEARD_THIN("beard_thin", TerrainAdjustment.BEARD_THIN),
		SOFT("soft", TerrainAdjustment.BEARD_THIN);

		final TagKey<Structure> tag;
		final TerrainAdjustment vanilla;

		Mode(String name, TerrainAdjustment vanilla) {
			this.tag = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeWorldgen.NS, "terrain_adaptation/" + name));
			this.vanilla = vanilla;
		}
	}

	static final TagKey<Structure> TRIM = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeWorldgen.NS, "template_trim"));
	static final TagKey<Structure> TRIM_AIR = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeWorldgen.NS, "template_trim/air"));
	static final TagKey<Structure> SUNK = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeWorldgen.NS, "terrain_adaptation/sunk_start"));

	/** The soft beard's skirt with skirtBump 0, in blocks from the footprint. */
	static final int MIN_SKIRT = 4;
	/** Vanilla's beard_thin skirt by the same measure (its kernel is down to 2% 8 blocks out). */
	static final double VANILLA_SKIRT = 8.0;
	/** Vanilla's kernel reach (Beardifier only gathers pieces within 12 blocks of a chunk), so the skirt can't exceed it. */
	static final int REACH = 12;

	private TerrainAdaptation() {}

	// ── tag table ────────────────────────────────────────────────────────────

	/**
	 * A tagged structure: its mode (null = its own), whether it is trimmed, whether only its air is trimmed
	 * (template_trim/air), and how many layers its start sinks (0 = none).
	 */
	record Shape(Mode mode, boolean trim, boolean airTrim, int sink) {}

	private static volatile Map<Structure, Shape> shapes = Map.of();

	/** The first mode whose tag the structure is in, or null. */
	static Mode resolve(Predicate<TagKey<Structure>> in) {
		for (Mode m : Mode.values()) if (in.test(m.tag)) return m;
		return null;
	}

	static void rebuild(RegistryAccess access, String when) {
		Optional<? extends Registry<Structure>> registry = access.registry(Registry.STRUCTURE_REGISTRY);
		if (registry.isEmpty()) return;
		Map<Structure, Shape> map = new IdentityHashMap<>();
		Map<Mode, Integer> counts = new EnumMap<>(Mode.class);
		int trimmed = 0, airTrimmed = 0, sunk = 0;
		for (Holder<Structure> h : registry.get().holders().toList()) {
			Mode mode = resolve(h::is);
			boolean trim = h.is(TRIM);
			boolean airTrim = !trim && h.is(TRIM_AIR);   // the full trim drops air too
			int sink = 0;
			if (h.is(SUNK)) {
				sink = sinkOf(h.value());
				if (sink > 0 && followsTerrain(h.value())) {
					// a terrain-matching start has no beard, and its rigid children sit at the surface: sinking them buries them
					LOG.warn("[terrain-adaptation] {} is in terrain_adaptation/sunk_start but its start pool has a terrain_matching piece; ignored", id(h));
					sink = 0;
				} else if (sink > 0) sunk++;
				else LOG.warn("[terrain-adaptation] {} is in terrain_adaptation/sunk_start but has no constant start height below 0 projected to a heightmap; ignored", id(h));
			}
			if (mode == null && !trim && !airTrim && sink == 0) continue;
			if (mode != null) {
				counts.merge(mode, 1, Integer::sum);
				List<String> all = new ArrayList<>();
				for (Mode m : Mode.values()) if (h.is(m.tag)) all.add(m.tag.location().getPath());
				if (all.size() > 1) LOG.warn("[terrain-adaptation] {} is in {}; {} wins", id(h), all, mode.tag.location().getPath());
			}
			if (trim) trimmed++;
			if (airTrim) airTrimmed++;
			map.put(h.value(), new Shape(mode, trim, airTrim, sink));
		}
		shapes = Collections.unmodifiableMap(map);
		LOG.info("[terrain-adaptation] {}: {} structures tagged, modes {}, {} trimmed, {} air-trimmed, {} sunk starts{}", when, map.size(), counts, trimmed, airTrimmed, sunk,
				WorldgenConfig.terrainAdaptationEnabled() ? "" : " (modes off: [worldgen.systems] terrainAdaptation = false)");

		if (!WorldgenConfig.terrainAdaptationEnabled()) return;
		for (Holder<Structure> h : registry.get().holders().toList()) {
			Shape s = map.get(h.value());
			if (s != null && s.mode() != null && h.value().terrainAdaptation() != s.mode().vanilla) {
				LOG.warn("[terrain-adaptation] {} ({}) answers terrainAdaptation itself; its {} tag has no effect",
						id(h), h.value().getClass().getSimpleName(), s.mode().tag.location().getPath());
			}
		}
	}

	private static String id(Holder<Structure> h) {
		return h.unwrapKey().map(ResourceKey::location).map(ResourceLocation::toString).orElse("?");
	}

	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		FIELDS.clear();
		rebuild(event.getServer().registryAccess(), "world load");
	}

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) {
		if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) return;
		FIELDS.clear();
		rebuild(event.getRegistryAccess(), "tags loaded");
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		shapes = Map.of();
		FIELDS.clear();
	}

	/** In creativeworldgen:template_trim. */
	public static boolean trims(Structure structure) {
		Shape s = shapes.get(structure);
		return s != null && s.trim();
	}

	/**
	 * In creativeworldgen:template_trim/air (and not template_trim): only air in columns with nothing solid is
	 * dropped. The footprint and earth count stay the untrimmed ones (nothing solid is dropped).
	 */
	public static boolean airTrims(Structure structure) {
		Shape s = shapes.get(structure);
		return s != null && s.airTrim();
	}

	static boolean soft(Structure structure) {
		Shape s = shapes.get(structure);
		return s != null && s.mode() == Mode.SOFT && WorldgenConfig.terrainAdaptationEnabled();
	}

	/**
	 * How many layers the beard of a structure in terrain_adaptation/sunk_start is raised (its start height sinks its
	 * pieces that far below the surface), 0 for every other structure. Follows [worldgen.systems] terrainAdaptation.
	 */
	public static int sink(Structure structure) {
		Shape s = shapes.get(structure);
		return s == null || !WorldgenConfig.terrainAdaptationEnabled() ? 0 : s.sink();
	}

	private static final Field START_HEIGHT = EarthFill.field(JigsawStructure.class, "f_227608_", "startHeight");
	private static final Field PROJECT_START = EarthFill.field(JigsawStructure.class, "f_227610_", "projectStartToHeightmap");

	/** A jigsaw structure's sink: minus its start height, when that is a constant absolute height below 0 and the start is projected to a heightmap. */
	@SuppressWarnings("unchecked")
	private static final Field START_POOL = EarthFill.field(JigsawStructure.class, "f_227605_", "startPool");

	/** A jigsaw structure whose start pool holds a terrain-matching element (a village base plate that follows the ground). */
	@SuppressWarnings("unchecked")
	static boolean followsTerrain(Structure structure) {
		if (!(structure instanceof JigsawStructure jigsaw)) return false;
		try {
			Holder<StructureTemplatePool> pool = (Holder<StructureTemplatePool>) START_POOL.get(jigsaw);
			for (StructurePoolElement e : pool.value().getShuffledTemplates(net.minecraft.util.RandomSource.create(0L))) {
				if (e.getProjection() == StructureTemplatePool.Projection.TERRAIN_MATCHING) return true;
			}
			return false;
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static int sinkOf(Structure structure) {
		if (!(structure instanceof JigsawStructure jigsaw)) return 0;
		try {
			Optional<Heightmap.Types> projected = (Optional<Heightmap.Types>) PROJECT_START.get(jigsaw);
			HeightProvider height = (HeightProvider) START_HEIGHT.get(jigsaw);
			if (projected.isEmpty() || !(height instanceof ConstantHeight constant) || !(constant.getValue() instanceof VerticalAnchor.Absolute absolute)) return 0;
			return Math.max(0, -absolute.y());
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/** Structure#terrainAdaptation: the tagged mode's vanilla value, or null to keep the structure's own. */
	public static TerrainAdjustment override(Structure structure) {
		Shape s = shapes.get(structure);
		if (s == null || s.mode() == null || !WorldgenConfig.terrainAdaptationEnabled()) return null;
		return s.mode().vanilla;
	}

	// ── distance fields ──────────────────────────────────────────────────────

	/**
	 * Distance (blocks, column centre to column centre) from each column to the nearest footprint column, over the
	 * footprint's bounds plus {@link #REACH}; coordinates relative to the piece's placement origin.
	 */
	record DistanceField(int x0, int z0, int w, int d, float[] dist) {
		float at(int rx, int rz) {
			int i = rx - x0, k = rz - z0;
			if (i < 0 || k < 0 || i >= w || k >= d) return Float.POSITIVE_INFINITY;
			return dist[k * w + i];
		}
	}

	/** Every (dx, dz) within REACH, nearest first, so a scan stops at its first footprint hit. */
	private static final int[][] OFFSETS;
	static {
		List<int[]> offs = new ArrayList<>();
		for (int dx = -REACH; dx <= REACH; dx++) for (int dz = -REACH; dz <= REACH; dz++) {
			if (dx * dx + dz * dz <= REACH * REACH) offs.add(new int[]{dx, dz, dx * dx + dz * dz});
		}
		offs.sort((a, b) -> Integer.compare(a[2], b[2]));
		OFFSETS = offs.toArray(int[][]::new);
	}

	/** The distance field of a set of columns (packed with TemplateTrim.column), or null for no columns. */
	static DistanceField field(LongSet columns) {
		if (columns.isEmpty()) return null;
		int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for (long c : columns) {
			int x = ChunkPos.getX(c), z = ChunkPos.getZ(c);
			minX = Math.min(minX, x); maxX = Math.max(maxX, x);
			minZ = Math.min(minZ, z); maxZ = Math.max(maxZ, z);
		}
		int x0 = minX - REACH, z0 = minZ - REACH, w = maxX - minX + 1 + 2 * REACH, d = maxZ - minZ + 1 + 2 * REACH;
		boolean[] in = new boolean[w * d];
		for (long c : columns) in[(ChunkPos.getZ(c) - z0) * w + (ChunkPos.getX(c) - x0)] = true;
		float[] dist = new float[w * d];
		for (int k = 0; k < d; k++) for (int i = 0; i < w; i++) {
			float best = Float.POSITIVE_INFINITY;
			for (int[] o : OFFSETS) {
				int ii = i + o[0], kk = k + o[1];
				if (ii >= 0 && kk >= 0 && ii < w && kk < d && in[kk * w + ii]) { best = (float) Math.sqrt(o[2]); break; }
			}
			dist[k * w + i] = best;
		}
		return new DistanceField(x0, z0, w, d, dist);
	}

	private record FieldKey(StructureTemplate template, Rotation rotation, Mirror mirror, BlockPos pivot, boolean trimmed) {}

	/** Per template placement transform, so each field is computed once per world; cleared with the tags. */
	private static final Map<FieldKey, Optional<DistanceField>> FIELDS = new ConcurrentHashMap<>();

	/** A template's footprint field, relative to its placement origin, with these settings (rotation, mirror, pivot). */
	static DistanceField templateField(StructureTemplate template, StructurePlaceSettings settings, boolean trimmed) {
		FieldKey key = new FieldKey(template, settings.getRotation(), settings.getMirror(), settings.getRotationPivot(), trimmed);
		return FIELDS.computeIfAbsent(key, k -> Optional.ofNullable(field(placedColumns(TemplateTrim.columns(template).footprint(trimmed), settings)))).orElse(null);
	}

	/** Local template columns moved through a placement transform (the one StructureTemplate#placeInWorld uses). */
	static LongSet placedColumns(LongSet local, StructurePlaceSettings settings) {
		LongOpenHashSet out = new LongOpenHashSet(local.size());
		for (long c : local) {
			BlockPos p = StructureTemplate.calculateRelativePosition(settings, new BlockPos(ChunkPos.getX(c), 0, ChunkPos.getZ(c)));
			out.add(TemplateTrim.column(p.getX(), p.getZ()));
		}
		return out;
	}

	// ── the soft beard ───────────────────────────────────────────────────────

	/** One template's field and where it was placed. */
	record Placed(DistanceField field, int originX, int originZ) {}

	/** The soft skirt with the current bump, in blocks. */
	static int skirt() { return MIN_SKIRT + Mth.clamp(WorldgenConfig.softSkirtBump(), 0, 8); }

	/**
	 * A soft piece's beard, riding in a Rigid as its box (the bounds are the piece's own). {@code placed} empty =
	 * no template: the footprint is the bounding box.
	 */
	public static final class SoftBox extends BoundingBox {
		final List<Placed> placed;
		final int groundY;
		final double scale;   // horizontal squeeze: VANILLA_SKIRT / skirt
		final double cutoff;  // no effect from this distance on (vanilla's kernel ends at 12, squeezed)

		SoftBox(BoundingBox box, List<Placed> placed, int groundY, int skirt) {
			super(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
			this.placed = placed;
			this.groundY = groundY;
			this.scale = VANILLA_SKIRT / skirt;
			this.cutoff = Math.min(REACH, REACH / scale);
		}

		/** Distance from this column to the footprint. */
		double distance(int x, int z) {
			if (placed.isEmpty()) {
				int dx = Math.max(0, Math.max(minX() - x, x - maxX())), dz = Math.max(0, Math.max(minZ() - z, z - maxZ()));
				return Math.sqrt(dx * dx + dz * dz);
			}
			double best = Double.POSITIVE_INFINITY;
			for (Placed p : placed) best = Math.min(best, p.field().at(x - p.originX(), z - p.originZ()));
			return best;
		}

		/** This piece's density at a block: vanilla's beard_thin formula with the horizontal distance replaced. */
		double density(int x, int y, int z) {
			int dy = y - groundY;
			if (dy < -REACH || dy >= REACH) return 0.0;
			double h = distance(x, z);
			if (!(h < cutoff)) return 0.0;
			return beard(h * scale, dy) * 0.8;
		}
	}

	/**
	 * Vanilla's Beardifier#getBeardContribution(dx, dy, dz, dy) for a horizontal distance {@code h} (where vanilla has
	 * sqrt(dx^2 + dz^2)): negative above the floor (carve), positive below (fill), fading with distance. Exact form,
	 * where vanilla reads its float kernel table.
	 */
	static double beard(double h, int dy) {
		double d0 = dy + 0.5;
		double r2 = h * h + d0 * d0;
		return -d0 * Mth.fastInvSqrt(r2 / 2.0) / 2.0 * Math.exp(-r2 / 16.0);
	}

	private static final Field TEMPLATE_MANAGER = EarthFill.field(PoolElementStructurePiece.class, "f_226493_", "structureTemplateManager");

	/** The structure start whose pieces Beardifier#forStructuresInChunk is gathering on this thread. */
	private static final ThreadLocal<StructureStart> GATHERING = new ThreadLocal<>();

	public static void beginGathering(StructureStart start) { GATHERING.set(start); }

	public static void endGathering() { GATHERING.remove(); }

	/**
	 * Beardifier's Rigid for one piece of the start being gathered: a soft structure's beard pieces get a SoftBox
	 * and adjustment NONE (vanilla adds nothing for them; {@link #addSoft} does), the rest stay as vanilla made them.
	 */
	public static Beardifier.Rigid rigid(BoundingBox box, TerrainAdjustment adjustment, int groundLevelDelta) {
		StructureStart start = GATHERING.get();
		Structure structure = start == null ? null : start.getStructure();
		if (structure instanceof MetaStructure) {
			// a meta structure's part beards as the structure it came from (its own adaptation and tags)
			structure = MetaStructure.structureOf(start, box);
			if (!(structure instanceof MetaStructure)) adjustment = structure.terrainAdaptation();
		}
		// a sunk start: the beard levels the ground at the projected surface, not at the sunk bottom layer
		if (structure != null) groundLevelDelta += sink(structure);
		if (structure != null && (adjustment == TerrainAdjustment.BEARD_THIN || adjustment == TerrainAdjustment.BEARD_BOX) && soft(structure)) {
			try {
				boolean trimmed = WorldgenConfig.templateTrimEnabled() && trims(structure);
				return new Beardifier.Rigid(softBox(start, box, groundLevelDelta, trimmed, skirt()), TerrainAdjustment.NONE, groundLevelDelta);
			} catch (RuntimeException e) {
				LOG.error("[terrain-adaptation] soft beard failed for a piece at {}; using vanilla's", box, e);
			}
		}
		return new Beardifier.Rigid(box, adjustment, groundLevelDelta);
	}

	static SoftBox softBox(StructureStart start, BoundingBox box, int groundLevelDelta, boolean trimmed, int skirt) {
		StructurePiece piece = null;
		for (StructurePiece p : start.getPieces()) if (p.getBoundingBox() == box) { piece = p; break; }
		List<Placed> placed = new ArrayList<>(1);
		if (piece instanceof PoolElementStructurePiece pool) {
			try {
				StructureTemplateManager templates = (StructureTemplateManager) TEMPLATE_MANAGER.get(pool);
				addElement(pool.getElement(), pool.getPosition(), pool.getRotation(), templates, trimmed, placed);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		} else if (piece instanceof TemplateStructurePiece tp) {
			addTemplate(tp.template(), tp.templatePosition(), tp.placeSettings(), trimmed, placed);
		}
		return new SoftBox(box, List.copyOf(placed), box.minY() + groundLevelDelta, skirt);
	}

	@SuppressWarnings("unchecked")
	static void addElement(StructurePoolElement element, BlockPos origin, Rotation rotation, StructureTemplateManager templates,
			boolean trimmed, List<Placed> out) throws IllegalAccessException {
		if (element instanceof ListPoolElement list) {
			for (StructurePoolElement sub : (List<StructurePoolElement>) EarthFill.LIST_ELEMENTS.get(list)) addElement(sub, origin, rotation, templates, trimmed, out);
		} else if (element instanceof SinglePoolElement single) {
			Either<ResourceLocation, StructureTemplate> either = (Either<ResourceLocation, StructureTemplate>) EarthFill.SINGLE_TEMPLATE.get(single);
			StructureTemplate template = either.map(templates::getOrCreate, Function.identity());
			addTemplate(template, origin, new StructurePlaceSettings().setRotation(rotation), trimmed, out);
		}
	}

	static void addTemplate(StructureTemplate template, BlockPos origin, StructurePlaceSettings settings, boolean trimmed, List<Placed> out) {
		DistanceField f = templateField(template, settings, trimmed);
		if (f != null) out.add(new Placed(f, origin.getX(), origin.getZ()));
	}

	/**
	 * Beardifier#compute, right after vanilla's rigid loop (the iterator is at its end): the density plus every
	 * SoftBox's. Walks the iterator back to its start, which is where vanilla's next call (back) leaves it anyway.
	 */
	public static double addSoft(double density, ObjectListIterator<Beardifier.Rigid> rigids, int x, int y, int z) {
		while (rigids.hasPrevious()) {
			if (rigids.previous().box() instanceof SoftBox soft) density += soft.density(x, y, z);
		}
		return density;
	}

	/** For tests: a SoftBox straight from columns placed at an origin. */
	static SoftBox softBox(BoundingBox box, LongSet placedColumns, int originX, int originZ, int groundY, int skirt) {
		DistanceField f = field(placedColumns);
		return new SoftBox(box, f == null ? List.of() : List.of(new Placed(f, originX, originZ)), groundY, skirt);
	}

}
