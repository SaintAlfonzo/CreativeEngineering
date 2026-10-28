package com.SaintAlfonzo.creativeengineering.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Counters for structure searches (feature_writeup/locate.md 3.4). One {@link Search} per top-level
 * ChunkGenerator#findNearestMapStructure call on a thread (the meta-expanded inner search counts into it):
 * candidates looked at, chunks loaded to STRUCTURE_STARTS, misses (a load that found no start) and the
 * predictions that said "no" by stage. A search with at least {@code logMissesAtLeast} misses logs one line;
 * every search adds to per-structure totals (/celocate stats). /celocate batch captures each of its searches by
 * the identity of the HolderSet it passed in.
 *
 * Also the search deadline (feature_writeup/structure_tuning.md 7.3.2): a search running past [worldgen.locate]
 * searchTimeoutSeconds, one /celocate stop ended, or any search while the server stops, is {@link #expired}: the
 * candidate checks and chunk loads then return "nothing here" at once, so the search finishes as not found and
 * frees its thread (Async Locator can't cancel a search itself).
 *
 * Also the census hooks: while a census runs, the generation-time reject points report why a structure didn't
 * start in one of the census's slot chunks ({@link #reject}).
 *
 * Cost: a ThreadLocal read and a few increments per candidate; nothing per tick. Generation threads pay one
 * volatile read per reject point.
 */
public final class LocateStats {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/locate");

	/** Which part of the prediction said "no" (locate.md 3.1). */
	public enum Stage { PLACEMENT, BIOME, POINT, FREQUENCY, META, META_TARGET }

	/** Why a structure didn't start in a census slot (recorded at generation time). */
	public enum Reason { BUILT, NO_START, SURFACE_CHECK, EARTH_FILL, PRIORITY, FREQUENCY, VETO, META_ONLY, CONVERTED, DIMENSION }

	public static final class Search {
		public final String label;
		public final BlockPos origin;
		public final String thread;
		final long started = System.nanoTime();
		public int candidates, loads, misses;
		public final long[] saved = new long[Stage.values().length];
		public long millis;
		public BlockPos found;
		public String foundId;
		int callLoads;
		int depth = 1;
		final long stopsAtStart = STOPS.get();
		/** Why the search was cut short ("timed out after 120 s", "stopped"), or null. */
		public volatile String ended;

		Search(String label, BlockPos origin) {
			this.label = label;
			this.origin = origin;
			this.thread = Thread.currentThread().getName();
		}
	}

	static final class Totals {
		final LongAdder searches = new LongAdder(), hits = new LongAdder(), candidates = new LongAdder(), loads = new LongAdder(),
				misses = new LongAdder(), missesBeforeHit = new LongAdder(), millis = new LongAdder(), cutShort = new LongAdder();
		final LongAdder[] saved = new LongAdder[Stage.values().length];
		Totals() { for (int i = 0; i < saved.length; i++) saved[i] = new LongAdder(); }
	}

	private static final ThreadLocal<Search> CURRENT = new ThreadLocal<>();
	private static final ThreadLocal<Stage> LAST_NO = new ThreadLocal<>();
	private static final ThreadLocal<boolean[]> PREDICTED = ThreadLocal.withInitial(() -> new boolean[1]);
	private static final Map<String, Totals> TOTALS = new ConcurrentHashMap<>();
	private static final Map<HolderSet<Structure>, Search[]> CAPTURE = Collections.synchronizedMap(new IdentityHashMap<>());
	/** Bumped by /celocate stop: every search that started before is stopped. */
	private static final java.util.concurrent.atomic.AtomicLong STOPS = new java.util.concurrent.atomic.AtomicLong();
	/** The server is stopping: every search and search load ends at once. */
	private static volatile boolean shuttingDown;

	private LocateStats() {}

	// ── search lifecycle (ChunkGeneratorMetaLocateMixin) ─────────────────────

	/** findNearestMapStructure HEAD. The meta-expanded inner search (MetaSlots.locating()) counts into the outer one. */
	public static void begin(HolderSet<Structure> structures, BlockPos origin) {
		Search s = CURRENT.get();
		if (s != null && MetaSlots.locating()) {
			s.depth++;
			return;
		}
		s = new Search(label(structures), origin);   // a stale record (a search that threw) is simply replaced
		CURRENT.set(s);
		Search[] slot = CAPTURE.get(structures);
		if (slot != null) slot[0] = s;
	}

	/** findNearestMapStructure RETURN (and the end of the meta-expanded path). */
	public static void end(Pair<BlockPos, Holder<Structure>> result) {
		Search s = CURRENT.get();
		if (s == null || --s.depth > 0) return;
		CURRENT.remove();
		s.millis = (System.nanoTime() - s.started) / 1_000_000L;
		if (result != null) {
			s.found = result.getFirst();
			s.foundId = result.getSecond().unwrapKey().map(k -> k.location().toString()).orElse("?");
		}
		Totals t = TOTALS.computeIfAbsent(s.label, k -> new Totals());
		t.searches.increment();
		if (result != null) {
			t.hits.increment();
			t.missesBeforeHit.add(s.misses);
		}
		t.candidates.add(s.candidates);
		t.loads.add(s.loads);
		t.misses.add(s.misses);
		t.millis.add(s.millis);
		if (s.ended != null) t.cutShort.increment();
		for (int i = 0; i < s.saved.length; i++) t.saved[i].add(s.saved[i]);
		int at = WorldgenConfig.locateLogMissesAtLeast();
		if (s.ended != null || (at >= 0 && s.misses >= at)) {
			String outcome = result == null ? (s.ended != null ? "not found (" + s.ended + ")" : "no hit")
					: "hit at (" + s.found.getX() + ", " + s.found.getZ() + ")" + (s.ended != null ? " (cut short: " + s.ended + ")" : "");
			LOG.info("[locate] {} from ({}, {}): {} after {} misses, {} loads, {} candidates, {} ms [{}]", s.label,
					s.origin.getX(), s.origin.getZ(), outcome, s.misses, s.loads, s.candidates, s.millis, s.thread);
		}
	}

	// ── deadline and stop ────────────────────────────────────────────────────

	/**
	 * The search on this thread must end now: past searchTimeoutSeconds, stopped by /celocate stop, the server
	 * stopping, or its thread interrupted (Async Locator's cancel). Outside a search only the last two count (a
	 * census load). Marks the search, so its log line and the batch report say why.
	 */
	public static boolean expired() {
		Search s = CURRENT.get();
		if (s != null && s.ended != null) return true;
		String why = null;
		if (shuttingDown) why = "server stopping";
		else if (Thread.currentThread().isInterrupted()) why = "cancelled";
		else if (s != null && STOPS.get() != s.stopsAtStart) why = "stopped";
		else if (s != null) {
			int limit = WorldgenConfig.locateSearchTimeoutSeconds();
			if (limit > 0 && System.nanoTime() - s.started > limit * 1_000_000_000L) why = "timed out after " + limit + " s";
		}
		if (why != null && s != null) s.ended = why;
		return why != null;
	}

	/** /celocate stop: every search running now ends at its next candidate or load. */
	public static void stopAll() { STOPS.incrementAndGet(); }

	/** ServerStoppingEvent: every running search ends, and no new one gets anywhere, until the next server start. */
	public static void shutdown() {
		shuttingDown = true;
		stopAll();
	}

	/** ServerAboutToStartEvent (a singleplayer world opened after another one stopped). */
	public static void resume() { shuttingDown = false; }

	/** getStructureGeneratingAt HEAD: one candidate chunk. */
	public static void beginCall() {
		Search s = CURRENT.get();
		if (s == null) return;
		s.candidates++;
		s.callLoads = 0;
	}

	/** getStructureGeneratingAt RETURN: every load in this call that didn't give the hit is a miss. */
	public static void endCall(Pair<BlockPos, Holder<Structure>> result) {
		Search s = CURRENT.get();
		if (s == null) return;
		s.misses += result != null && s.callLoads > 0 ? s.callLoads - 1 : s.callLoads;
		s.callLoads = 0;
	}

	/** A chunk loaded to STRUCTURE_STARTS by a search (LocateLoads). */
	public static void load() {
		Search s = CURRENT.get();
		if (s == null) return;
		s.loads++;
		s.callLoads++;
	}

	// ── prediction (LocatePrediction) ────────────────────────────────────────

	/** StructureCheck#canCreateStructure ran on this thread (the census tells fresh predictions from cached ones). */
	public static void predicted() {
		PREDICTED.get()[0] = true;
	}

	public static void predictedNo(Stage stage) {
		LAST_NO.set(stage);
		Search s = CURRENT.get();
		if (s != null) s.saved[stage.ordinal()]++;
	}

	/** Census: clear before asking StructureCheck, read after. */
	static void resetPrediction() {
		PREDICTED.get()[0] = false;
		LAST_NO.remove();
	}

	static boolean wasPredicted() { return PREDICTED.get()[0]; }

	static Stage lastNo() { return LAST_NO.get(); }

	// ── batch capture ────────────────────────────────────────────────────────

	/** The batch's own HolderSet: the search started with it fills slot[0]. */
	static Search[] capture(HolderSet<Structure> structures) {
		Search[] slot = new Search[1];
		CAPTURE.put(structures, slot);
		return slot;
	}

	static void release(HolderSet<Structure> structures) {
		CAPTURE.remove(structures);
	}

	// ── totals (/celocate stats) ─────────────────────────────────────────────

	static List<Map<String, Object>> totals() {
		List<Map<String, Object>> rows = new ArrayList<>();
		for (Map.Entry<String, Totals> e : TOTALS.entrySet()) {
			Totals t = e.getValue();
			Map<String, Object> row = new java.util.LinkedHashMap<>();
			row.put("structure", e.getKey());
			row.put("searches", t.searches.sum());
			row.put("hits", t.hits.sum());
			row.put("misses", t.misses.sum());
			row.put("missesBeforeHit", t.missesBeforeHit.sum());
			row.put("loads", t.loads.sum());
			row.put("candidates", t.candidates.sum());
			row.put("ms", t.millis.sum());
			row.put("cutShort", t.cutShort.sum());
			Map<String, Long> saved = new java.util.LinkedHashMap<>();
			for (Stage st : Stage.values()) saved.put(st.name().toLowerCase(java.util.Locale.ROOT), t.saved[st.ordinal()].sum());
			row.put("savedByStage", saved);
			rows.add(row);
		}
		rows.sort((a, b) -> Long.compare((Long) b.get("misses"), (Long) a.get("misses")));
		return rows;
	}

	static void resetTotals() { TOTALS.clear(); }

	static String label(HolderSet<Structure> structures) {
		return structures.unwrap().map(tag -> "#" + tag.location(), list -> list.size() == 1
				? list.get(0).unwrapKey().map(k -> k.location().toString()).orElse("?")
				: list.size() + " structures");
	}

	// ── census reasons (generation threads) ──────────────────────────────────

	/** The census running now, or null. Written on the server thread, read by generation threads. */
	static volatile LocateCensus census;

	/**
	 * A generation-time reject point says why {@code structure} doesn't start at {@code pos}. Only recorded while a
	 * census runs, for its slot chunks, and never for PlacementPriority's predictions (scratch chunks).
	 */
	public static void reject(Structure structure, ChunkPos pos, Reason reason) {
		LocateCensus c = census;
		if (c == null || PlacementPriority.predicting()) return;
		c.note(pos, structure, reason, false);
	}

	/** tryGenerateStructure RETURN: built, or "no start" when no reject point gave a reason. */
	public static void attempted(Structure structure, ChunkPos pos, boolean built) {
		LocateCensus c = census;
		if (c == null || PlacementPriority.predicting()) return;
		c.note(pos, structure, built ? Reason.BUILT : Reason.NO_START, built);
	}
}
