package com.SaintAlfonzo.creativeengineering.worldgen;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
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
 * <p><b>The tags.</b> {@code creativeengineering:terrain_adaptation/<mode>} (kubejs worldgen/terrain_adaptation.js)
 * replaces the structure's own {@code terrain_adaptation}: {@code none}, {@code bury}, {@code beard_thin},
 * {@code beard_box} are vanilla's, {@code soft} is the pack's own (below). mixin/StructureTerrainAdaptationMixin
 * answers Structure#terrainAdaptation from this table, so every vanilla use follows it: the Beardifier, and the
 * 12-block reference margin a terrain-adapted start gets. A structure in two mode tags takes the first in
 * {@link Mode} order and is logged. Structures whose class answers terrainAdaptation itself (YUNG's jigsaw
 * structures, which have their own enhanced adaptation) can't be changed this way; that is logged too.
 * {@code creativeengineering:template_trim} is read by {@link TemplateTrim}.
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

	private static final Logger LOG = LogManager.getLogger("creativeengineering/terrain-adaptation");

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
			this.tag = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "terrain_adaptation/" + name));
			this.vanilla = vanilla;
		}
	}

	static final TagKey<Structure> TRIM = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "template_trim"));

	/** The soft beard's skirt with skirtBump 0, in blocks from the footprint. */
	static final int MIN_SKIRT = 4;
	/** Vanilla's beard_thin skirt by the same measure (its kernel is down to 2% 8 blocks out). */
	static final double VANILLA_SKIRT = 8.0;
	/** Vanilla's kernel reach (Beardifier only gathers pieces within 12 blocks of a chunk), so the skirt can't exceed it. */
	static final int REACH = 12;

	private TerrainAdaptation() {}

	// ── tag table ────────────────────────────────────────────────────────────

	/** A tagged structure: its mode (null = its own) and whether it is trimmed. */
	record Shape(Mode mode, boolean trim) {}

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
		int trimmed = 0;
		for (Holder<Structure> h : registry.get().holders().toList()) {
			Mode mode = resolve(h::is);
			boolean trim = h.is(TRIM);
			if (mode == null && !trim) continue;
			if (mode != null) {
				counts.merge(mode, 1, Integer::sum);
				List<String> all = new ArrayList<>();
				for (Mode m : Mode.values()) if (h.is(m.tag)) all.add(m.tag.location().getPath());
				if (all.size() > 1) LOG.warn("[terrain-adaptation] {} is in {}; {} wins", id(h), all, mode.tag.location().getPath());
			}
			if (trim) trimmed++;
			map.put(h.value(), new Shape(mode, trim));
		}
		shapes = Collections.unmodifiableMap(map);
		LOG.info("[terrain-adaptation] {}: {} structures tagged, modes {}, {} trimmed{}", when, map.size(), counts, trimmed,
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

	/** In creativeengineering:template_trim. */
	public static boolean trims(Structure structure) {
		Shape s = shapes.get(structure);
		return s != null && s.trim();
	}

	static boolean soft(Structure structure) {
		Shape s = shapes.get(structure);
		return s != null && s.mode() == Mode.SOFT && WorldgenConfig.terrainAdaptationEnabled();
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
		if (start != null && (adjustment == TerrainAdjustment.BEARD_THIN || adjustment == TerrainAdjustment.BEARD_BOX) && soft(start.getStructure())) {
			try {
				boolean trimmed = WorldgenConfig.templateTrimEnabled() && trims(start.getStructure());
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
