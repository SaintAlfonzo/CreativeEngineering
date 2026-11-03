package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Placement priority: when two structures from different groups would overlap, the higher rank wins and the lower
 * one never starts (it is dropped before its terrain is shaped, so no half-buildings or stray pads are left). Ranks
 * come from structure_groups.json "priority" (settlements > pillager settlements > dungeons > remote houses and camps >
 * ruins; at sea monuments > ocean ruins > boats > flotsam; fortified towns above villages), staged by
 * structure_overrides.js.
 *
 * DETERMINISTIC: chunks generate in any order, so a start never looks at what its neighbours already built. When a
 * ranked start is generated (tryGenerateStructure, after its own biome / earth-fill / surface checks, if it keeps its
 * frequency roll), every higher-ranked structure set's grid slots whose start could reach it (8 chunks, the vanilla
 * reference radius) are PREDICTED: the set's weighted pick is replayed on a scratch chunk with every pack mixin applied
 * (frequency rolls, meta conversion, biome rules, earth fill, and their own priority checks), so the prediction is
 * exactly what that chunk will generate. If any predicted higher-ranked piece box overlaps one of this start's piece
 * boxes (3D: a dungeon under a village is fine), this start becomes INVALID_START and the pick falls through, as
 * for a wrong biome. Predictions are cached per (generator, set, chunk).
 *
 * COMPANIONS: a meta structure may bring "companions": structures started in chunks of their own around it (a
 * fortified town with 1 to 3 villages, meta/town_villages). Each companion is a full start in its own chunk, so a
 * village 150 blocks from the town keeps its whole reach (a meta part must stay within 7 chunks of the meta's start).
 * A chunk makes its companions at the end of createStructures when a predicted companion-bearing meta in range
 * names it; companions skip frequency rolls and meta conversion, and rank just below their group (the town and grid
 * villages win; pillagers, dungeons, houses and ruins lose to them, and predict them too).
 *
 * SHAPING: a jigsaw structure of a shaped group (priority.shape: the settlements) grows around the higher-ranked
 * structures predicted near it instead of being dropped: they are cut out of the free space its jigsaw assembler
 * fits pieces into (shape()), so streets end or turn at a town's edge and overlapping houses are not chosen. Only a
 * start piece that overlaps one still drops the whole start.
 *
 * PEACEFUL RADIUS: big settlements, villages and remote camps/houses keep a square radius (chunks, priority.peaceful)
 * around their piece boxes free of hostile structures (pillagers, dungeons, witch huts), whatever the ranks: a hostile
 * start predicts the peaceful sets' slots around it and drops when one of its boxes comes within that radius of a
 * predicted peaceful start's boxes. Peaceful structures in turn ignore hostile ones in their own rank check and
 * shaping (any hostile that would overlap them is within their radius, so it drops), which keeps the two checks from
 * predicting each other.
 *
 * Mixins: ChunkGeneratorPlacementPriorityMixin, JigsawPlacementPriorityMixin. Switch: CWGConfig [worldgen.systems] placementPriority.
 */
public final class PlacementPriority {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/placement-priority");
	/** Starts reach at most this many chunks from their start chunk (ChunkStatus.STRUCTURE_REFERENCES' radius). */
	static final int REACH = 8;
	private static final int CACHE_SIZE = 16384;
	/** Predictions inside predictions: past this depth a predicted start skips its own priority check. Ranks strictly
	 *  increase per level, and real chains are 1 or 2 deep (a village near a fortified town's slot), so this only
	 *  guards the stack. */
	private static final int MAX_DEPTH = 3;

	private PlacementPriority() {}

	/** ChunkGenerator#tryGenerateStructure, made callable (ChunkGeneratorPlacementPriorityMixin). */
	public interface Generator {
		boolean creativeengineering$tryGenerate(StructureSet.StructureSelectionEntry entry, StructureManager manager, RegistryAccess access,
				RandomState random, StructureTemplateManager templates, long seed, ChunkAccess chunk, ChunkPos pos, SectionPos section);
	}

	// ── staging (structure_overrides.js) ─────────────────────────────────────

	record CompanionSpec(List<String> structures, int min, int max, int distMin, int distMax, int rank) {}

	private static volatile Map<String, Integer> stagedRanks = Map.of();
	private static volatile Map<String, CompanionSpec> stagedCompanions = Map.of();
	private static volatile List<String> stagedShaped = List.of();
	private static volatile Map<String, Integer> stagedRadius = Map.of();
	private static volatile List<String> stagedHostile = List.of();
	/** Largest peaceful radius accepted (chunks). */
	static final int MAX_RADIUS = 16;

	/** {"ranks": {id: n}, "shaped": [ids], "companions": {meta id: {structures: [ids], count: [a, b], distance: [a, b] chunks, rank: n}},
	 *  "peace": {"radius": {id: chunks}, "hostile": [ids]}}. */
	public static void stage(String json) {
		Map<String, Integer> ranks = new LinkedHashMap<>();
		Map<String, CompanionSpec> companions = new LinkedHashMap<>();
		List<String> shaped = new ArrayList<>();
		Map<String, Integer> radius = new LinkedHashMap<>();
		List<String> hostile = new ArrayList<>();
		try {
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			if (root.has("ranks")) for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("ranks").entrySet()) ranks.put(e.getKey(), e.getValue().getAsInt());
			if (root.has("shaped")) root.getAsJsonArray("shaped").forEach(x -> shaped.add(x.getAsString()));
			if (root.has("companions")) for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("companions").entrySet()) {
				JsonObject o = e.getValue().getAsJsonObject();
				List<String> ids = new ArrayList<>();
				o.getAsJsonArray("structures").forEach(x -> ids.add(x.getAsString()));
				int[] count = { o.getAsJsonArray("count").get(0).getAsInt(), o.getAsJsonArray("count").get(1).getAsInt() };
				int[] dist = { o.getAsJsonArray("distance").get(0).getAsInt(), o.getAsJsonArray("distance").get(1).getAsInt() };
				if (count[0] < 0 || count[1] < count[0] || count[1] > 8) throw new IllegalArgumentException(e.getKey() + " count");
				if (dist[0] < 1 || dist[1] < dist[0] || dist[1] > 32) throw new IllegalArgumentException(e.getKey() + " distance");
				companions.put(e.getKey(), new CompanionSpec(List.copyOf(ids), count[0], count[1], dist[0], dist[1], o.get("rank").getAsInt()));
			}
			if (root.has("peace")) {
				JsonObject peace = root.getAsJsonObject("peace");
				if (peace.has("radius")) for (Map.Entry<String, JsonElement> e : peace.getAsJsonObject("radius").entrySet()) {
					int n = e.getValue().getAsInt();
					if (n < 1 || n > MAX_RADIUS) throw new IllegalArgumentException(e.getKey() + " radius " + n);
					radius.put(e.getKey(), n);
				}
				if (peace.has("hostile")) peace.getAsJsonArray("hostile").forEach(x -> { if (!radius.containsKey(x.getAsString())) hostile.add(x.getAsString()); });
			}
		} catch (RuntimeException e) {
			LOG.error("[placement-priority] bad table from structure_overrides.js, staging nothing: {}", e.toString());
			ranks.clear();
			companions.clear();
			shaped.clear();
			radius.clear();
			hostile.clear();
		}
		synchronized (PlacementPriority.class) {
			stagedRanks = ranks;
			stagedCompanions = companions;
			stagedShaped = List.copyOf(shaped);
			stagedRadius = radius;
			stagedHostile = List.copyOf(hostile);
			builtFor = new WeakReference<>(null);
			CACHE.clear();
		}
	}

	// ── resolved state ───────────────────────────────────────────────────────

	record Companion(MetaStructure meta, StructureSet set, List<StructureSet.StructureSelectionEntry> entries, CompanionSpec spec) {}

	static final class Active {
		final Map<Structure, Integer> rank = new IdentityHashMap<>();
		final List<Companion> companions = new ArrayList<>();
		final Map<StructureSet, Integer> setRank = new IdentityHashMap<>();
		/** Jigsaw structures that grow around higher-ranked ones (priority.shape). */
		final java.util.Set<Structure> shaped = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
		int maxCompanionRank = Integer.MIN_VALUE;
		/** Peaceful radius (chunks) per structure, the hostile structures it keeps away, the sets and companion rules
		 *  that can start a peaceful structure, and the largest radius (0: no peaceful rule). */
		final Map<Structure, Integer> radius = new IdentityHashMap<>();
		final java.util.Set<Structure> hostile = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
		final java.util.Set<StructureSet> peaceSets = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
		final List<Companion> peaceCompanions = new ArrayList<>();
		int maxRadius = 0;
	}

	private static volatile WeakReference<Registry<StructureSet>> builtFor = new WeakReference<>(null);
	private static volatile Active current = new Active();

	static Active active(RegistryAccess access) {
		Registry<StructureSet> sets = access.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
		if (builtFor.get() == sets) return current;
		synchronized (PlacementPriority.class) {
			if (builtFor.get() == sets) return current;
			Registry<Structure> structures = access.registryOrThrow(Registry.STRUCTURE_REGISTRY);
			Active a = new Active();
			stagedRanks.forEach((id, r) -> {
				ResourceLocation key = ResourceLocation.tryParse(id);
				Structure s = key == null ? null : structures.get(key);
				if (s != null) a.rank.put(s, r);
			});
			stagedRadius.forEach((id, n) -> {
				ResourceLocation key = ResourceLocation.tryParse(id);
				Structure s = key == null ? null : structures.get(key);
				if (s != null) a.radius.put(s, n);
			});
			for (String id : stagedHostile) {
				ResourceLocation key = ResourceLocation.tryParse(id);
				Structure s = key == null ? null : structures.get(key);
				if (s != null && !a.radius.containsKey(s)) a.hostile.add(s);
			}
			if (!a.hostile.isEmpty()) for (int n : a.radius.values()) a.maxRadius = Math.max(a.maxRadius, n);
			for (StructureSet set : sets) {
				if (a.maxRadius > 0 && set.structures().stream().anyMatch(e -> a.radius.containsKey(e.structure().value()))) a.peaceSets.add(set);
				int max = Integer.MIN_VALUE;
				for (StructureSet.StructureSelectionEntry e : set.structures()) {
					Integer r = a.rank.get(e.structure().value());
					if (r != null) max = Math.max(max, r);
				}
				if (max != Integer.MIN_VALUE) a.setRank.put(set, max);
			}
			for (String id : stagedShaped) {
				ResourceLocation key = ResourceLocation.tryParse(id);
				Structure s = key == null ? null : structures.get(key);
				if (s != null) a.shaped.add(s);
			}
			stagedCompanions.forEach((id, spec) -> {
				ResourceLocation key = ResourceLocation.tryParse(id);
				if (key == null || !(structures.get(key) instanceof MetaStructure meta)) {
					LOG.warn("[placement-priority] companions of {}: not a loaded meta structure; ignored", id);
					return;
				}
				for (StructureSet set : sets) {
					List<StructureSet.StructureSelectionEntry> entries = new ArrayList<>();
					boolean hasMeta = false;
					for (StructureSet.StructureSelectionEntry e : set.structures()) {
						if (e.structure().value() == meta) hasMeta = true;
						ResourceLocation sk = structures.getKey(e.structure().value());
						if (sk != null && spec.structures().contains(sk.toString())) entries.add(e);
					}
					if (!hasMeta) continue;
					if (entries.isEmpty()) LOG.warn("[placement-priority] companions of {}: none of its companion structures is in its set", id);
					else {
						a.companions.add(new Companion(meta, set, List.copyOf(entries), spec));
						a.maxCompanionRank = Math.max(a.maxCompanionRank, spec.rank());
					}
				}
			});
			if (a.maxRadius > 0) for (Companion c : a.companions) {
				if (c.entries().stream().anyMatch(e -> a.radius.containsKey(e.structure().value()))) a.peaceCompanions.add(c);
			}
			if (a.maxRadius > 0) LOG.info("[placement-priority] {} peaceful structure(s) in {} set(s) keep {} hostile structure(s) away (radius up to {} chunks)",
					a.radius.size(), a.peaceSets.size(), a.hostile.size(), a.maxRadius);
			if (!a.rank.isEmpty()) LOG.info("[placement-priority] {} structure(s) ranked in {} set(s); {} companion rule(s)", a.rank.size(), a.setRank.size(), a.companions.size());
			current = a;
			builtFor = new WeakReference<>(sets);
			CACHE.clear();
			return a;
		}
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		synchronized (PlacementPriority.class) {
			current = new Active();
			builtFor = new WeakReference<>(null);
			stagedRanks = Map.of();
			stagedCompanions = Map.of();
			stagedShaped = List.of();
			stagedRadius = Map.of();
			stagedHostile = List.of();
			CACHE.clear();
			SETS.clear();
		}
	}

	// ── thread state ─────────────────────────────────────────────────────────

	/** Set while a companion generates: its rank (frequency rolls and meta conversion are skipped). */
	private static final ThreadLocal<Integer> COMPANION = new ThreadLocal<>();

	public static boolean companionMode() { return COMPANION.get() != null; }

	/** True while this thread replays generation on a scratch chunk to predict a neighbour (not real generation). */
	public static boolean predicting() { return DEPTH.get()[0] > 0; }

	private static final ThreadLocal<int[]> DEPTH = ThreadLocal.withInitial(() -> new int[1]);

	/** Runs a prediction with the generation thread-locals of the real start set aside, and restores them. */
	private static <T> T isolated(java.util.function.Supplier<T> work) {
		Object[] freq = StructureFrequencies.saveThreadState();
		Object forced = MetaSlots.saveForced();
		Integer companion = COMPANION.get();
		Current cur = CURRENT.get();
		COMPANION.remove();
		DEPTH.get()[0]++;
		try {
			return work.get();
		} finally {
			DEPTH.get()[0]--;
			StructureFrequencies.restoreThreadState(freq);
			MetaSlots.restoreForced(forced);
			if (companion != null) COMPANION.set(companion);
			else COMPANION.remove();
			if (cur != null) CURRENT.set(cur);
			else CURRENT.remove();
		}
	}

	// ── prediction ───────────────────────────────────────────────────────────

	/** What a chunk will start for one set (or one companion slot): the structure, its rank, its piece boxes. */
	record Predicted(Structure structure, int rank, List<BoundingBox> boxes) {
		static final Predicted NONE = new Predicted(null, Integer.MIN_VALUE, List.of());
	}

	private record Key(Object generator, Object what, int x, int z) {}

	private static final Map<Key, Predicted> CACHE = Collections.synchronizedMap(new LinkedHashMap<>(256, 0.75F, true) {
		@Override protected boolean removeEldestEntry(Map.Entry<Key, Predicted> eldest) { return size() > CACHE_SIZE; }
	});

	/** The sets this generator can place (its biome source), per generator. */
	private static final Map<ChunkGenerator, List<StructureSet>> SETS = Collections.synchronizedMap(new WeakHashMap<>());

	private static List<StructureSet> setsOf(ChunkGenerator generator) {
		return SETS.computeIfAbsent(generator, g -> g.possibleStructureSets().map(h -> h.value()).toList());
	}

	/** Context of one generation call, passed down to predictions. */
	record Ctx(ChunkGenerator generator, StructureManager manager, RegistryAccess access, RandomState random,
			StructureTemplateManager templates, long seed, ChunkAccess heights, Active active) {}

	private static Predicted predictSet(Ctx c, StructureSet set, ChunkPos pos) {
		Key key = new Key(c.generator, set, pos.x, pos.z);
		Predicted p = CACHE.get(key);
		if (p != null) return p;
		p = isolated(() -> {
			ProtoChunk scratch = scratch(c, pos);
			SectionPos section = SectionPos.bottomOf(scratch);
			Generator g = (Generator) c.generator;
			List<StructureSet.StructureSelectionEntry> list = set.structures();
			if (list.size() == 1) {
				g.creativeengineering$tryGenerate(list.get(0), c.manager, c.access, c.random, c.templates, c.seed, scratch, pos, section);
			} else if (!list.isEmpty()) {
				// ChunkGenerator#createStructures' weighted pick, re-picking among the rest after a failure
				List<StructureSet.StructureSelectionEntry> left = new ArrayList<>(list);
				WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
				random.setLargeFeatureSeed(c.seed, pos.x, pos.z);
				int total = 0;
				for (StructureSet.StructureSelectionEntry e : left) total += e.weight();
				while (!left.isEmpty()) {
					int roll = random.nextInt(total);
					int i = 0;
					for (StructureSet.StructureSelectionEntry e : left) {
						roll -= e.weight();
						if (roll < 0) break;
						i++;
					}
					StructureSet.StructureSelectionEntry e = left.get(i);
					if (g.creativeengineering$tryGenerate(e, c.manager, c.access, c.random, c.templates, c.seed, scratch, pos, section)) break;
					left.remove(i);
					total -= e.weight();
				}
			}
			return read(scratch, c.active, null);
		});
		CACHE.put(key, p);
		return p;
	}

	private static ProtoChunk scratch(Ctx c, ChunkPos pos) {
		return new ProtoChunk(pos, UpgradeData.EMPTY, c.heights, c.access.registryOrThrow(Registry.BIOME_REGISTRY), null);
	}

	/** The (single) valid start a scratch chunk got, with its rank (companionRank when given). */
	private static Predicted read(ProtoChunk scratch, Active a, Integer companionRank) {
		for (Map.Entry<Structure, StructureStart> e : scratch.getAllStarts().entrySet()) {
			if (!e.getValue().isValid()) continue;
			Integer r = companionRank != null ? companionRank : a.rank.get(e.getKey());
			return new Predicted(e.getKey(), r == null ? Integer.MIN_VALUE : r, boxes(e.getValue()));
		}
		return Predicted.NONE;
	}

	static List<BoundingBox> boxes(StructureStart start) {
		List<BoundingBox> out = new ArrayList<>();
		for (StructurePiece p : start.getPieces()) {
			if (p instanceof MetaStructure.Index) continue;
			out.add(p.getBoundingBox());
		}
		return out;
	}

	private static boolean overlaps(List<BoundingBox> a, List<BoundingBox> b) {
		for (BoundingBox x : a) for (BoundingBox y : b) if (x.intersects(y)) return true;
		return false;
	}

	/** Grid slots of a random-spread set whose start chunk lies in [x0, x1] x [z0, z1] and really is a start chunk. */
	private static List<ChunkPos> slots(Ctx c, StructurePlacement placement, int x0, int z0, int x1, int z1) {
		List<ChunkPos> out = new ArrayList<>();
		if (!(placement instanceof RandomSpreadStructurePlacement spread)) return out;
		int s = spread.spacing();
		for (int rx = Math.floorDiv(x0, s); rx <= Math.floorDiv(x1, s); rx++) {
			for (int rz = Math.floorDiv(z0, s); rz <= Math.floorDiv(z1, s); rz++) {
				ChunkPos p = spread.getPotentialStructureChunk(c.seed, rx * s, rz * s);
				if (p.x < x0 || p.x > x1 || p.z < z0 || p.z > z1) continue;
				if (placement.isStructureChunk(c.generator, c.random, c.seed, p.x, p.z)) out.add(p);
			}
		}
		return out;
	}

	// ── companions ───────────────────────────────────────────────────────────

	/** The chunks a companion-bearing meta started at `meta` sends its companions to (deterministic). */
	static List<ChunkPos> companionChunks(Companion rule, long seed, ChunkPos meta) {
		int salt = ("companions:" + rule.spec.structures()).hashCode();
		int n = rule.spec.min() + (int) (StructureFrequencies.roll(seed, meta.x, meta.z, salt) * (rule.spec.max() - rule.spec.min() + 1));
		double base = StructureFrequencies.roll(seed, meta.x, meta.z, salt + 1) * Math.PI * 2;
		List<ChunkPos> out = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			double angle = base + i * Math.PI * 2 / n;
			double d = rule.spec.distMin() + StructureFrequencies.roll(seed, meta.x, meta.z, salt + 2 + i) * (rule.spec.distMax() - rule.spec.distMin());
			ChunkPos p = new ChunkPos(meta.x + (int) Math.round(Math.cos(angle) * d), meta.z + (int) Math.round(Math.sin(angle) * d));
			if (!out.contains(p) && !p.equals(meta)) out.add(p);
		}
		return out;
	}

	/** Companion candidates in a deterministic weighted order for this chunk (vanilla-style weighted pick without replacement). */
	private static List<StructureSet.StructureSelectionEntry> companionOrder(Companion rule, long seed, ChunkPos pos) {
		List<StructureSet.StructureSelectionEntry> left = new ArrayList<>(rule.entries);
		List<StructureSet.StructureSelectionEntry> out = new ArrayList<>();
		WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
		random.setLargeFeatureWithSalt(seed, pos.x, pos.z, 0x5E77_1E5);
		int total = 0;
		for (StructureSet.StructureSelectionEntry e : left) total += e.weight();
		while (!left.isEmpty() && total > 0) {
			int roll = random.nextInt(total);
			int i = 0;
			for (StructureSet.StructureSelectionEntry e : left) {
				roll -= e.weight();
				if (roll < 0) break;
				i++;
			}
			StructureSet.StructureSelectionEntry e = left.remove(i);
			total -= e.weight();
			out.add(e);
		}
		return out;
	}

	/** Generates the companion for this chunk into `chunk` (real or scratch); true if one started. */
	private static boolean generateCompanion(Ctx c, Companion rule, ChunkAccess chunk, ChunkPos pos) {
		SectionPos section = SectionPos.bottomOf(chunk);
		Generator g = (Generator) c.generator;
		for (StructureSet.StructureSelectionEntry e : companionOrder(rule, c.seed, pos)) {
			StructureStart existing = c.manager.getStartForStructure(section, e.structure().value(), chunk);
			if (existing != null && existing.isValid()) return false;
			COMPANION.set(rule.spec.rank());
			try {
				if (g.creativeengineering$tryGenerate(e, c.manager, c.access, c.random, c.templates, c.seed, chunk, pos, section)) return true;
			} finally {
				COMPANION.remove();
			}
		}
		return false;
	}

	private static Predicted predictCompanion(Ctx c, Companion rule, ChunkPos pos) {
		Key key = new Key(c.generator, rule, pos.x, pos.z);
		Predicted p = CACHE.get(key);
		if (p != null) return p;
		p = isolated(() -> {
			ProtoChunk scratch = scratch(c, pos);
			generateCompanion(c, rule, scratch, pos);
			return read(scratch, c.active, rule.spec.rank());
		});
		CACHE.put(key, p);
		return p;
	}

	/** Metas of this rule predicted to start within `reach` chunks of [x0, x1] x [z0, z1]. */
	private static List<ChunkPos> metasNear(Ctx c, Companion rule, int x0, int z0, int x1, int z1, int reach) {
		List<ChunkPos> out = new ArrayList<>();
		for (ChunkPos p : slots(c, rule.set.placement(), x0 - reach, z0 - reach, x1 + reach, z1 + reach)) {
			if (predictSet(c, rule.set, p).structure() == rule.meta) out.add(p);
		}
		return out;
	}

	// ── entry points (mixin) ─────────────────────────────────────────────────

	/**
	 * tryGenerateStructure, the generated start (before it is stored): INVALID_START when a higher-ranked structure
	 * is predicted to overlap it.
	 */
	public static StructureStart filter(StructureStart start, StructureSet.StructureSelectionEntry entry, ChunkGenerator generator,
			StructureManager manager, RegistryAccess access, RandomState random, StructureTemplateManager templates, long seed,
			ChunkAccess chunk, ChunkPos pos) {
		if (!start.isValid() || !WorldgenConfig.placementPriorityEnabled() || DEPTH.get()[0] >= MAX_DEPTH) return start;
		Active a = active(access);
		if (a.setRank.isEmpty() && a.companions.isEmpty() && a.peaceSets.isEmpty()) return start;
		Structure s = entry.structure().value();
		Integer r = COMPANION.get() != null ? COMPANION.get() : a.rank.get(s);
		boolean hostile = COMPANION.get() == null && a.hostile.contains(s);
		if (r == null && !hostile) return start;
		if (COMPANION.get() == null && !StructureFrequencies.keepsAt(s, seed, pos.x, pos.z)) return start;   // the roll drops it anyway
		Ctx c = new Ctx(generator, manager, access, random, templates, seed, chunk, a);
		List<BoundingBox> mine = boxes(start);
		if (mine.isEmpty()) return start;
		BoundingBox all = BoundingBox.encapsulatingBoxes(mine).orElseThrow();
		if (r != null) {
			int x0 = (all.minX() >> 4) - REACH, z0 = (all.minZ() >> 4) - REACH, x1 = (all.maxX() >> 4) + REACH, z1 = (all.maxZ() >> 4) + REACH;
			for (Higher h : higher(c, s, pos, r, x0, z0, x1, z1)) {
				if (overlaps(h.predicted().boxes(), mine)) return drop(c, s, pos, h.predicted().structure(), h.at());
			}
		}
		if (hostile) {
			Higher h = peaceful(c, s, pos, mine, all);
			if (h != null) return dropPeace(c, s, pos, h);
		}
		return start;
	}

	/**
	 * The first peaceful structure predicted near a hostile start whose radius reaches one of its boxes (square, in the
	 * XZ plane, around the peaceful structure's piece boxes), or null.
	 */
	private static Higher peaceful(Ctx c, Structure s, ChunkPos pos, List<BoundingBox> mine, BoundingBox all) {
		Active a = c.active;
		int reach = a.maxRadius + REACH;
		int x0 = (all.minX() >> 4) - reach, z0 = (all.minZ() >> 4) - reach, x1 = (all.maxX() >> 4) + reach, z1 = (all.maxZ() >> 4) + reach;
		for (StructureSet set : setsOf(c.generator)) {
			if (!a.peaceSets.contains(set)) continue;
			for (ChunkPos p : slots(c, set.placement(), x0, z0, x1, z1)) {
				if (p.equals(pos) && set.structures().stream().anyMatch(e -> e.structure().value() == s)) continue;   // our own slot
				Predicted pred = predictSet(c, set, p);
				if (within(a, pred, mine)) return new Higher(pred, p);
			}
		}
		for (Companion rule : a.peaceCompanions) {
			for (ChunkPos m : metasNear(c, rule, x0, z0, x1, z1, rule.spec.distMax() + 1)) {
				for (ChunkPos p : companionChunks(rule, c.seed, m)) {
					if (p.x < x0 || p.x > x1 || p.z < z0 || p.z > z1 || p.equals(pos)) continue;
					Predicted pred = predictCompanion(c, rule, p);
					if (within(a, pred, mine)) return new Higher(pred, p);
				}
			}
		}
		return null;
	}

	/** Whether a box of `mine` comes within the predicted peaceful structure's radius of one of its boxes (XZ only). */
	private static boolean within(Active a, Predicted pred, List<BoundingBox> mine) {
		if (pred.structure() == null) return false;
		Integer n = a.radius.get(pred.structure());
		return n != null && withinRadius(pred.boxes(), n, mine);
	}

	/** Whether a box of `mine` is within `chunks` chunks (square, XZ) of a box of `peaceful`; touching counts. */
	static boolean withinRadius(List<BoundingBox> peaceful, int chunks, List<BoundingBox> mine) {
		int d = chunks * 16;
		for (BoundingBox b : peaceful) {
			for (BoundingBox m : mine) {
				if (m.maxX() >= b.minX() - d && m.minX() <= b.maxX() + d && m.maxZ() >= b.minZ() - d && m.minZ() <= b.maxZ() + d) return true;
			}
		}
		return false;
	}

	record Higher(Predicted predicted, ChunkPos at) {}

	/**
	 * Every structure ranked above r predicted to start in [x0, x1] x [z0, z1] (grid slots of the sets with a
	 * higher-ranked member, and companions of predicted metas), except s's own slot at pos.
	 */
	private static List<Higher> higher(Ctx c, Structure s, ChunkPos pos, int r, int x0, int z0, int x1, int z1) {
		List<Higher> out = new ArrayList<>();
		Active a = c.active;
		// a peaceful structure ignores hostile ones: any that would overlap it is inside its radius and drops (peaceful())
		boolean peaceful = a.radius.containsKey(s) && !a.hostile.isEmpty();
		for (StructureSet set : setsOf(c.generator)) {
			Integer sr = a.setRank.get(set);
			if (sr == null || sr <= r) continue;
			for (ChunkPos p : slots(c, set.placement(), x0, z0, x1, z1)) {
				if (p.equals(pos) && set.structures().stream().anyMatch(e -> e.structure().value() == s)) continue;   // our own slot
				Predicted pred = predictSet(c, set, p);
				if (pred.rank() > r && !(peaceful && a.hostile.contains(pred.structure()))) out.add(new Higher(pred, p));
			}
		}
		if (a.maxCompanionRank > r) {
			for (Companion rule : a.companions) {
				if (rule.spec.rank() <= r) continue;
				for (ChunkPos m : metasNear(c, rule, x0, z0, x1, z1, rule.spec.distMax() + 1)) {
					for (ChunkPos p : companionChunks(rule, c.seed, m)) {
						if (p.x < x0 || p.x > x1 || p.z < z0 || p.z > z1 || p.equals(pos)) continue;
						Predicted pred = predictCompanion(c, rule, p);
						if (pred.rank() > r && !(peaceful && a.hostile.contains(pred.structure()))) out.add(new Higher(pred, p));
					}
				}
			}
		}
		return out;
	}

	// ── shaping: jigsaw settlements grow around higher-ranked structures ──────

	/** The tryGenerateStructure call on this thread: which structure, where, and the generation context. */
	private record Current(Structure structure, ChunkPos pos, ChunkGenerator generator, StructureManager manager, RegistryAccess access,
			RandomState random, StructureTemplateManager templates, long seed, ChunkAccess chunk) {}

	private static final ThreadLocal<Current> CURRENT = new ThreadLocal<>();

	/** tryGenerateStructure HEAD: remember the call, for shape(). */
	public static void current(StructureSet.StructureSelectionEntry entry, ChunkGenerator generator, StructureManager manager,
			RegistryAccess access, RandomState random, StructureTemplateManager templates, long seed, ChunkAccess chunk, ChunkPos pos) {
		CURRENT.set(new Current(entry.structure().value(), pos, generator, manager, access, random, templates, seed, chunk));
	}

	/**
	 * JigsawPlacement's assembler (m_227210_) HEAD: the free space its pieces must fit in. For a structure of a shaped
	 * group (priority.shape: the settlements), every higher-ranked structure predicted nearby is cut out of it, so the
	 * jigsaw grows around them like around its own pieces: a street that reaches one ends or turns, a house that would
	 * overlap it is not chosen. Only the start piece can still overlap one; filter() then drops the whole start.
	 */
	public static VoxelShape shape(VoxelShape free, StructurePiece startPiece) {
		Current cur = CURRENT.get();
		if (cur == null || !WorldgenConfig.placementPriorityEnabled() || DEPTH.get()[0] >= MAX_DEPTH) return free;
		Active a = active(cur.access);
		if (!a.shaped.contains(cur.structure)) return free;
		// the assembler belongs to this call's own start (not a meta part elsewhere, not a stale call)
		BlockPos at = startPiece.getBoundingBox().getCenter();
		if (Math.abs((at.getX() >> 4) - cur.pos.x) > 1 || Math.abs((at.getZ() >> 4) - cur.pos.z) > 1) return free;
		Integer r = COMPANION.get() != null ? COMPANION.get() : a.rank.get(cur.structure);
		if (r == null) return free;
		Ctx c = new Ctx(cur.generator, cur.manager, cur.access, cur.random, cur.templates, cur.seed, cur.chunk, a);
		int x0 = cur.pos.x - 2 * REACH, z0 = cur.pos.z - 2 * REACH, x1 = cur.pos.x + 2 * REACH, z1 = cur.pos.z + 2 * REACH;
		VoxelShape blocked = Shapes.empty();
		int n = 0;
		for (Higher h : higher(c, cur.structure, cur.pos, r, x0, z0, x1, z1)) {
			for (BoundingBox b : h.predicted().boxes()) {
				blocked = Shapes.or(blocked, Shapes.create(AABB.of(b)));
				n++;
			}
		}
		if (n == 0) return free;
		SHAPED.incrementAndGet();
		return Shapes.join(free, blocked, BooleanOp.ONLY_FIRST);
	}

	static final java.util.concurrent.atomic.AtomicLong SHAPED = new java.util.concurrent.atomic.AtomicLong();


	private static StructureStart drop(Ctx c, Structure loser, ChunkPos at, Structure winner, ChunkPos winnerAt) {
		if (LOG.isDebugEnabled() || WorldgenConfig.placementPriorityLog()) {
			Registry<Structure> reg = c.access.registryOrThrow(Registry.STRUCTURE_REGISTRY);
			LOG.info("[placement-priority] dropped {} at chunk [{}, {}]: overlaps {} from chunk [{}, {}]",
					reg.getKey(loser), at.x, at.z, reg.getKey(winner), winnerAt.x, winnerAt.z);
		}
		DROPPED.incrementAndGet();
		return StructureStart.INVALID_START;
	}

	static final java.util.concurrent.atomic.AtomicLong DROPPED = new java.util.concurrent.atomic.AtomicLong();

	private static StructureStart dropPeace(Ctx c, Structure loser, ChunkPos at, Higher peaceful) {
		if (LOG.isDebugEnabled() || WorldgenConfig.placementPriorityLog()) {
			Registry<Structure> reg = c.access.registryOrThrow(Registry.STRUCTURE_REGISTRY);
			Structure p = peaceful.predicted().structure();
			LOG.info("[placement-priority] dropped {} at chunk [{}, {}]: within {} chunk(s) of peaceful {} from chunk [{}, {}]",
					reg.getKey(loser), at.x, at.z, c.active.radius.get(p), reg.getKey(p), peaceful.at().x, peaceful.at().z);
		}
		PEACE_DROPPED.incrementAndGet();
		return StructureStart.INVALID_START;
	}

	static final java.util.concurrent.atomic.AtomicLong PEACE_DROPPED = new java.util.concurrent.atomic.AtomicLong();

	/** createStructures, after every set: the companions the predicted metas around this chunk send here. */
	public static void companions(ChunkGenerator generator, RegistryAccess access, RandomState random, StructureManager manager,
			ChunkAccess chunk, StructureTemplateManager templates, long seed) {
		if (!WorldgenConfig.placementPriorityEnabled()) return;
		Active a = active(access);
		if (a.companions.isEmpty()) return;
		ChunkPos pos = chunk.getPos();
		Ctx c = new Ctx(generator, manager, access, random, templates, seed, chunk, a);
		for (Companion rule : a.companions) {
			if (!setsOf(generator).contains(rule.set)) continue;
			for (ChunkPos m : metasNear(c, rule, pos.x, pos.z, pos.x, pos.z, rule.spec.distMax() + 1)) {
				if (companionChunks(rule, seed, m).contains(pos) && generateCompanion(c, rule, chunk, pos)) {
					if (WorldgenConfig.placementPriorityLog()) LOG.info("[placement-priority] companion at chunk [{}, {}] of {} at chunk [{}, {}]",
							pos.x, pos.z, access.registryOrThrow(Registry.STRUCTURE_REGISTRY).getKey(rule.meta), m.x, m.z);
					return;
				}
			}
		}
	}
}
