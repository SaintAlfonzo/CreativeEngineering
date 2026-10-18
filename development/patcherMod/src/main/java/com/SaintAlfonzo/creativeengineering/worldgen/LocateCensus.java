package com.SaintAlfonzo.creativeengineering.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * /celocate census (feature_writeup/locate.md 3.6): every slot of every live structure set in a square, the
 * prediction for each member (what a search would see, stage by stage), then the real structure starts of that chunk
 * (generated to STRUCTURE_STARTS only, through LocateLoads, so no noise or features). Every set that tries a chunk is
 * predicted before that chunk loads, and the chunk loads once for all of them. While it runs, the generation reject
 * points report why a member didn't start in one of its slot chunks (LocateStats.reject).
 *
 * Per structure: slots, predicted maybe / no (by stage) / already known, built, misses (maybe but not built, with
 * reasons), false negatives (predicted no but built: must be 0; any other number is a prediction bug). Per set: each
 * member's measured share of the set's slots. Run it on fresh terrain: a chunk generated before the census only
 * counts as "known".
 *
 * The square is cut into regions; each region is one task on Async Locator's thread pool (LocateCommand).
 */
final class LocateCensus {

	/** One slot: a chunk a structure set's placement tries. */
	record Slot(ChunkPos pos, Holder<StructureSet> set) {}

	static final class Row {
		long slots, setSkipped, maybe, known, built, misses, falseNegatives;
		final long[] no = new long[LocateStats.Stage.values().length];
		final Map<String, Long> missReasons = new TreeMap<>();
		final Map<String, Long> takenBy = new TreeMap<>();
		final List<String> falseNegativeAt = new ArrayList<>();

		void add(Row o) {
			slots += o.slots; setSkipped += o.setSkipped; maybe += o.maybe; known += o.known; built += o.built; misses += o.misses;
			falseNegatives += o.falseNegatives;
			for (int i = 0; i < no.length; i++) no[i] += o.no[i];
			o.missReasons.forEach((k, v) -> missReasons.merge(k, v, Long::sum));
			o.takenBy.forEach((k, v) -> takenBy.merge(k, v, Long::sum));
			for (String at : o.falseNegativeAt) if (falseNegativeAt.size() < 20) falseNegativeAt.add(at);
		}
	}

	static final class SetRow {
		long slots, setSkipped;
		final Map<String, Long> built = new TreeMap<>();

		void add(SetRow o) {
			slots += o.slots; setSkipped += o.setSkipped;
			o.built.forEach((k, v) -> built.merge(k, v, Long::sum));
		}
	}

	final ServerLevel level;
	final ChunkPos center;
	final int radius;
	final long started = System.currentTimeMillis();
	final AtomicBoolean cancelled = new AtomicBoolean();
	final AtomicInteger regionsDone = new AtomicInteger(), slotsDone = new AtomicInteger();
	final Map<Long, List<Slot>> regions = new LinkedHashMap<>();
	int slotCount;
	final List<String> skippedPlacements = new ArrayList<>();
	private final Set<Long> targets = ConcurrentHashMap.newKeySet();
	private final Map<Long, Map<Structure, LocateStats.Reason>> reasons = new ConcurrentHashMap<>();
	private final Map<String, Row> rows = new HashMap<>();
	private final Map<String, SetRow> setRows = new HashMap<>();
	private final List<String> errors = new ArrayList<>();

	LocateCensus(ServerLevel level, ChunkPos center, int radius) {
		this.level = level;
		this.center = center;
		this.radius = radius;
	}

	// ── setup (server thread) ────────────────────────────────────────────────

	/** Every slot of every live set in the square, grouped into regions of {@code regionChunks}. */
	void enumerate(int regionChunks) {
		ChunkGenerator generator = level.getChunkSource().getGenerator();
		RandomState randomState = level.getChunkSource().randomState();
		long seed = level.getSeed();
		generator.ensureStructuresGenerated(randomState);
		int x0 = center.x - radius, z0 = center.z - radius, x1 = center.x + radius, z1 = center.z + radius;
		Registry<StructureSet> setRegistry = level.registryAccess().registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
		generator.possibleStructureSets().forEach(set -> {
			StructurePlacement placement = set.value().placement();
			if (!DeadPlacementFilter.filter(List.of(placement)).contains(placement) || set.value().structures().isEmpty()) return;
			List<ChunkPos> slots = new ArrayList<>();
			if (placement instanceof RandomSpreadStructurePlacement spread) {
				int spacing = spread.spacing();
				for (int cx = Math.floorDiv(x0, spacing); cx <= Math.floorDiv(x1, spacing); cx++) {
					for (int cz = Math.floorDiv(z0, spacing); cz <= Math.floorDiv(z1, spacing); cz++) {
						ChunkPos p = spread.getPotentialStructureChunk(seed, cx * spacing, cz * spacing);
						if (p.x >= x0 && p.x <= x1 && p.z >= z0 && p.z <= z1) slots.add(p);
					}
				}
			} else if (placement instanceof ConcentricRingsStructurePlacement rings) {
				List<ChunkPos> ring = generator.getRingPositionsFor(rings, randomState);
				if (ring != null) for (ChunkPos p : ring) if (p.x >= x0 && p.x <= x1 && p.z >= z0 && p.z <= z1) slots.add(p);
			} else {
				skippedPlacements.add(setRegistry.getKey(set.value()) + " (" + placement.getClass().getSimpleName() + ")");
				return;
			}
			for (ChunkPos p : slots) {
				long region = ChunkPos.asLong(Math.floorDiv(p.x, regionChunks), Math.floorDiv(p.z, regionChunks));
				regions.computeIfAbsent(region, k -> new ArrayList<>()).add(new Slot(p, set));
				targets.add(p.toLong());
				slotCount++;
			}
		});
	}

	// ── generation threads (LocateStats.reject / attempted) ──────────────────

	void note(ChunkPos pos, Structure structure, LocateStats.Reason reason, boolean overwrite) {
		long key = pos.toLong();
		if (!targets.contains(key)) return;
		Map<Structure, LocateStats.Reason> m = reasons.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
		if (overwrite) m.put(structure, reason);
		else m.putIfAbsent(structure, reason);
	}

	// ── one region (an Async Locator thread) ─────────────────────────────────

	void runRegion(List<Slot> slots) {
		Map<String, Row> local = new HashMap<>();
		Map<String, SetRow> localSets = new HashMap<>();
		ChunkGenerator generator = level.getChunkSource().getGenerator();
		RandomState randomState = level.getChunkSource().randomState();
		long seed = level.getSeed();
		Registry<Structure> structures = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		Registry<StructureSet> sets = level.registryAccess().registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
		// every set that tries a chunk is predicted before that chunk loads, so the first set's load doesn't turn the
		// later sets' slots into "known" (06_phase_6.md 3.1)
		Map<Long, List<Slot>> byChunk = new LinkedHashMap<>();
		for (Slot slot : slots) byChunk.computeIfAbsent(slot.pos().toLong(), k -> new ArrayList<>()).add(slot);
		try {
			for (List<Slot> chunkSlots : byChunk.values()) {
				if (cancelled.get()) return;
				chunk(chunkSlots, local, localSets, generator, randomState, seed, structures, sets);
				slotsDone.addAndGet(chunkSlots.size());
			}
		} catch (RuntimeException e) {
			synchronized (this) {
				if (errors.size() < 20) errors.add(e.toString());
			}
		} finally {
			synchronized (this) {
				local.forEach((k, v) -> rows.computeIfAbsent(k, x -> new Row()).add(v));
				localSets.forEach((k, v) -> setRows.computeIfAbsent(k, x -> new SetRow()).add(v));
			}
			regionsDone.incrementAndGet();
		}
	}

	/** One set's slot at a chunk: whether the set tries it, and what a search would predict for each member. */
	private record Predicted(Slot slot, boolean setTries, List<Structure> members, Map<Structure, String> prediction) {}

	/** Every slot at one chunk: predict them all, load the chunk once, then score them all. */
	private void chunk(List<Slot> chunkSlots, Map<String, Row> local, Map<String, SetRow> localSets, ChunkGenerator generator,
			RandomState randomState, long seed, Registry<Structure> structures, Registry<StructureSet> sets) {
		ChunkPos pos = chunkSlots.get(0).pos();
		List<Predicted> predicted = new ArrayList<>();
		for (Slot slot : chunkSlots) predicted.add(predict(slot, generator, randomState, seed));

		// what generation did
		ChunkAccess chunk = LocateLoads.structureStarts(level, pos.x, pos.z);
		Map<Structure, StructureStart> starts = chunk.getAllStarts();
		Map<Structure, LocateStats.Reason> why = reasons.getOrDefault(pos.toLong(), Map.of());
		for (Predicted p : predicted) score(p, starts, why, local, localSets, structures, sets);
	}

	/** What a search would see, before the chunk exists. */
	private Predicted predict(Slot slot, ChunkGenerator generator, RandomState randomState, long seed) {
		ChunkPos pos = slot.pos();
		StructureSet set = slot.set().value();
		boolean setTries = set.placement().isStructureChunk(generator, randomState, seed, pos.x, pos.z);
		List<Structure> members = new ArrayList<>();
		Map<Structure, String> prediction = new HashMap<>();
		for (StructureSet.StructureSelectionEntry e : set.structures()) {
			Structure s = e.structure().value();
			if (prediction.containsKey(s)) continue;
			members.add(s);
			LocateStats.resetPrediction();
			StructureCheckResult r = level.structureManager().checkStructurePresence(pos, s, false);
			if (r == StructureCheckResult.CHUNK_LOAD_NEEDED) prediction.put(s, "maybe");
			else if (r == StructureCheckResult.START_NOT_PRESENT && LocateStats.wasPredicted()) {
				LocateStats.Stage st = LocateStats.lastNo();
				prediction.put(s, "no:" + (st == null ? LocateStats.Stage.POINT : st).name());
			} else prediction.put(s, "known");
		}
		return new Predicted(slot, setTries, members, prediction);
	}

	private void score(Predicted p, Map<Structure, StructureStart> starts, Map<Structure, LocateStats.Reason> why, Map<String, Row> local,
			Map<String, SetRow> localSets, Registry<Structure> structures, Registry<StructureSet> sets) {
		ChunkPos pos = p.slot().pos();
		boolean setTries = p.setTries();
		String setId = String.valueOf(sets.getKey(p.slot().set().value()));
		SetRow setRow = localSets.computeIfAbsent(setId, k -> new SetRow());
		setRow.slots++;
		if (!setTries) setRow.setSkipped++;
		String winner = null;
		for (Structure s : p.members()) {
			StructureStart st = starts.get(s);
			if (st != null && st.isValid()) winner = idOf(structures, s);
		}
		for (Structure s : p.members()) {
			String id = idOf(structures, s);
			Row row = local.computeIfAbsent(id, k -> new Row());
			row.slots++;
			if (!setTries) row.setSkipped++;
			StructureStart st = starts.get(s);
			boolean built = st != null && st.isValid();
			if (built) {
				row.built++;
				setRow.built.merge(id, 1L, Long::sum);
			}
			String pr = p.prediction().get(s);
			if (pr.equals("known")) {
				row.known++;
			} else if (pr.equals("maybe")) {
				row.maybe++;
				if (!built) {
					row.misses++;
					LocateStats.Reason reason = why.get(s);
					if (reason == LocateStats.Reason.BUILT) reason = null;   // built and then replaced: shouldn't happen
					String key = reason != null ? reason.name().toLowerCase(Locale.ROOT)
							: winner != null ? "slot_taken" : !setTries ? "set_skipped" : "not_tried";
					row.missReasons.merge(key, 1L, Long::sum);
					if (reason == null && winner != null) row.takenBy.merge(winner, 1L, Long::sum);
				}
			} else {
				row.no[LocateStats.Stage.valueOf(pr.substring(3)).ordinal()]++;
				if (built) {
					row.falseNegatives++;
					if (row.falseNegativeAt.size() < 20) row.falseNegativeAt.add(pos.x + "," + pos.z + " (" + pr.substring(3).toLowerCase(Locale.ROOT) + ")");
				}
			}
		}
	}

	private static String idOf(Registry<Structure> structures, Structure s) {
		ResourceLocation id = structures.getKey(s);
		return id != null ? id.toString() : String.valueOf(s);
	}

	// ── report (server thread) ───────────────────────────────────────────────

	synchronized Map<String, Object> report(int threads) {
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("dimension", level.dimension().location().toString());
		root.put("centerChunk", List.of(center.x, center.z));
		root.put("radiusChunks", radius);
		root.put("locatorThreads", threads);
		root.put("regions", regions.size());
		root.put("slotChunks", slotCount);
		root.put("seconds", (System.currentTimeMillis() - started) / 1000);
		root.put("cancelled", cancelled.get());
		long fn = rows.values().stream().mapToLong(r -> r.falseNegatives).sum();
		root.put("falseNegatives", fn);
		root.put("skippedPlacements", skippedPlacements);
		root.put("errors", errors);
		List<Map<String, Object>> list = new ArrayList<>();
		rows.entrySet().stream().sorted((a, b) -> Long.compare(b.getValue().misses, a.getValue().misses)).forEach(e -> {
			Row r = e.getValue();
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("structure", e.getKey());
			m.put("slots", r.slots);
			m.put("setSkipped", r.setSkipped);
			m.put("predictedMaybe", r.maybe);
			Map<String, Long> no = new LinkedHashMap<>();
			for (LocateStats.Stage st : LocateStats.Stage.values()) no.put(st.name().toLowerCase(Locale.ROOT), r.no[st.ordinal()]);
			m.put("predictedNo", no);
			m.put("known", r.known);
			m.put("built", r.built);
			m.put("misses", r.misses);
			m.put("missReasons", r.missReasons);
			m.put("takenBy", r.takenBy);
			m.put("falseNegatives", r.falseNegatives);
			if (!r.falseNegativeAt.isEmpty()) m.put("falseNegativeAt", r.falseNegativeAt);
			list.add(m);
		});
		root.put("structures", list);
		List<Map<String, Object>> setList = new ArrayList<>();
		new TreeMap<>(setRows).forEach((id, r) -> {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("set", id);
			m.put("slots", r.slots);
			m.put("setSkipped", r.setSkipped);
			m.put("built", r.built);
			Map<String, Double> share = new LinkedHashMap<>();
			r.built.forEach((k, v) -> share.put(k, r.slots == 0 ? 0.0 : Math.round(10000.0 * v / r.slots) / 10000.0));
			m.put("shareOfSlots", share);
			setList.add(m);
		});
		root.put("sets", setList);
		return root;
	}

	synchronized long[] totals() {
		long built = 0, misses = 0, fn = 0;
		for (Row r : rows.values()) {
			built += r.built;
			misses += r.misses;
			fn += r.falseNegatives;
		}
		return new long[]{built, misses, fn};
	}
}
