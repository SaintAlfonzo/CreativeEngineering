package com.SaintAlfonzo.creativecommon.perf;

import com.SaintAlfonzo.creativecommon.perf.PerfJfrEvents.BlockEntityTypeTicks;
import com.SaintAlfonzo.creativecommon.perf.PerfJfrEvents.ChunkWait;
import com.SaintAlfonzo.creativecommon.perf.PerfJfrEvents.ChunkWaitStats;
import com.SaintAlfonzo.creativecommon.perf.PerfJfrEvents.EntityTypeTicks;
import com.SaintAlfonzo.creativecommon.perf.PerfJfrEvents.KubeJSHandler;
import com.SaintAlfonzo.creativecommon.perf.PerfJfrEvents.KubeJSHandlerStats;
import com.SaintAlfonzo.creativecommon.perf.PerfJfrEvents.LevelTick;
import com.SaintAlfonzo.creativecommon.perf.PerfJfrEvents.ServerTick;
import com.SaintAlfonzo.creativecommon.perf.PerfJfrEvents.SlowBlockEntityTick;
import com.SaintAlfonzo.creativecommon.perf.PerfJfrEvents.SlowEntityTick;
import jdk.jfr.Event;
import jdk.jfr.EventType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * State behind the perf mixins (ServerTickPerfMixin and friends, KubeJSHandlerTiming): which events a
 * running recording wants, the per-second totals, and the event writing. Everything except the
 * KubeJS wrapper runs on the server thread only, so the totals are plain fields.
 *
 * Switching: once a second (at the end of a server tick) the flags below are refreshed from JFR, so a
 * hook whose events no recording enables only reads one static boolean. A recording started later
 * with jcmd is picked up within a second.
 */
public final class PerfEvents {
	/** Fixed thresholds for the events that carry their time in a field (see PerfJfrEvents). */
	public static final long SLOW_ENTITY_TICK_NANOS = 2_000_000L;
	public static final long SLOW_BLOCK_ENTITY_TICK_NANOS = 2_000_000L;
	public static final long SLOW_CHUNK_WAIT_NANOS = 1_000_000L;

	public static volatile boolean serverTick, levelTick, chunkWaits, entityTicks, blockEntityTicks, kubejs;

	/** Hooks that have run at least once since start, for /ceperf status (a hook that never runs was not applied). */
	public static final int HOOK_SERVER_TICK = 1, HOOK_LEVEL_TICK = 2, HOOK_CHUNK_WAIT = 4, HOOK_ENTITY = 8,
			HOOK_BLOCK_ENTITY = 16, HOOK_KUBEJS = 32;
	private static volatile int hooksSeen;

	private static final EventType[] TYPES = new EventType[10];
	private static final Map<String, Class<? extends Event>> CLASSES = new LinkedHashMap<>();
	static {
		CLASSES.put("server tick", ServerTick.class);
		CLASSES.put("level tick", LevelTick.class);
		CLASSES.put("chunk waits", ChunkWait.class);
		CLASSES.put("chunk wait totals", ChunkWaitStats.class);
		CLASSES.put("slow entity ticks", SlowEntityTick.class);
		CLASSES.put("entity ticks by type", EntityTypeTicks.class);
		CLASSES.put("slow block entity ticks", SlowBlockEntityTick.class);
		CLASSES.put("block entity ticks by type", BlockEntityTypeTicks.class);
		CLASSES.put("KubeJS slow handlers", KubeJSHandler.class);
		CLASSES.put("KubeJS handler totals", KubeJSHandlerStats.class);
		int i = 0;
		for (Class<? extends Event> c : CLASSES.values()) {
			try {
				TYPES[i] = EventType.getEventType(c);
			} catch (Throwable t) {
				// JFR not available in this JVM: every flag stays false
			}
			i++;
		}
	}

	private static Thread serverThread;
	private static long lastFlush;

	private static final List<TickStats> ENTITY_STATS = new ArrayList<>();
	private static final List<TickStats> BLOCK_ENTITY_STATS = new ArrayList<>();
	private static final List<TickStats> HANDLER_STATS = new ArrayList<>();
	private static final Map<String, HandlerStats> HANDLERS = new ConcurrentHashMap<>();

	private static long[] waitStarts = new long[16];
	private static int waitDepth;
	private static long chunkRequests, chunkWaited, chunkLongest;

	private PerfEvents() {}

	/** Per-second totals of one entity type, block entity type or KubeJS handler. */
	public static class TickStats {
		public final String key;
		long count, nanos, longest;
		boolean listed;

		public TickStats(String key) {
			this.key = key;
		}

		void add(List<TickStats> list, long dt) {
			if (!listed) {
				listed = true;
				list.add(this);
			}
			count++;
			nanos += dt;
			if (dt > longest) longest = dt;
		}

		void reset() {
			count = nanos = longest = 0;
		}
	}

	public static final class HandlerStats extends TickStats {
		public final String event, source;
		public final int line;

		HandlerStats(String event, String source, int line) {
			super(event + " " + source + ":" + line);
			this.event = event;
			this.source = source;
			this.line = line;
		}
	}

	public static void seen(int hook) {
		if ((hooksSeen & hook) == 0) hooksSeen |= hook;
	}

	public static boolean hookSeen(int hook) {
		return (hooksSeen & hook) != 0;
	}

	/** Event label -> enabled in a running recording, for /ceperf status. */
	public static Map<String, Boolean> enabledEvents() {
		Map<String, Boolean> out = new LinkedHashMap<>();
		int i = 0;
		for (String label : CLASSES.keySet()) out.put(label, on(TYPES[i++]));
		return out;
	}

	public static int handlerCount() {
		return HANDLERS.size();
	}

	private static boolean on(EventType... types) {
		for (EventType t : types) if (t != null && t.isEnabled()) return true;
		return false;
	}

	private static void refresh() {
		serverTick = on(TYPES[0]);
		levelTick = on(TYPES[1]);
		chunkWaits = on(TYPES[2], TYPES[3]);
		entityTicks = on(TYPES[4], TYPES[5]);
		blockEntityTicks = on(TYPES[6], TYPES[7]);
		kubejs = on(TYPES[8], TYPES[9]);
	}

	// ── server and level ticks ──

	public static ServerTick beginServerTick() {
		seen(HOOK_SERVER_TICK);
		if (!serverTick) return null;
		ServerTick e = new ServerTick();
		e.begin();
		return e;
	}

	public static void endServerTick(MinecraftServer server, ServerTick e) {
		if (e != null) {
			e.end();
			if (e.shouldCommit()) {
				e.tick = server.getTickCount();
				e.players = server.getPlayerCount();
				e.commit();
			}
		}
		// a wait cut short by an exception never popped its start; none spans the end of a tick
		waitDepth = 0;
		long now = System.nanoTime();
		if (serverThread == null || now - lastFlush >= 1_000_000_000L) {
			serverThread = Thread.currentThread();
			lastFlush = now;
			flush();
			refresh();
		}
	}

	public static LevelTick beginLevelTick() {
		seen(HOOK_LEVEL_TICK);
		if (!levelTick) return null;
		LevelTick e = new LevelTick();
		e.begin();
		return e;
	}

	public static void endLevelTick(ServerLevel level, LevelTick e) {
		if (e == null) return;
		e.end();
		if (e.shouldCommit()) {
			e.dimension = dimension(level);
			e.loadedChunks = level.getChunkSource().getLoadedChunksCount();
			e.players = level.players().size();
			e.commit();
		}
	}

	private static String dimension(Level level) {
		return level.dimension().location().toString();
	}

	// ── chunk waits (ServerChunkCache.getChunk around managedBlock) ──

	public static void chunkWaitStart() {
		seen(HOOK_CHUNK_WAIT);
		if (!chunkWaits) return;
		if (waitDepth == waitStarts.length) waitStarts = Arrays.copyOf(waitStarts, waitDepth * 2);
		waitStarts[waitDepth++] = System.nanoTime();
	}

	public static void chunkWaitEnd(Level level, int x, int z, ChunkStatus status) {
		if (waitDepth == 0) return;
		long dt = System.nanoTime() - waitStarts[--waitDepth];
		chunkRequests++;
		chunkWaited += dt;
		if (dt > chunkLongest) chunkLongest = dt;
		if (dt >= SLOW_CHUNK_WAIT_NANOS) {
			ChunkWait e = new ChunkWait();
			if (e.isEnabled()) {
				e.dimension = level == null ? "?" : dimension(level);
				e.chunkX = x;
				e.chunkZ = z;
				e.status = status == null ? "?" : status.getName();
				e.waitTime = dt;
				e.commit();
			}
		}
	}

	// ── entity and block entity ticks ──

	public static void entityTicked(Level level, Entity entity, long dt) {
		((PerfStatsHolder) entity.getType()).ce$perfStats().add(ENTITY_STATS, dt);
		if (dt >= SLOW_ENTITY_TICK_NANOS) {
			SlowEntityTick e = new SlowEntityTick();
			if (e.isEnabled()) {
				BlockPos p = entity.blockPosition();
				e.dimension = dimension(level);
				e.entityType = ((PerfStatsHolder) entity.getType()).ce$perfStats().key;
				e.x = p.getX();
				e.y = p.getY();
				e.z = p.getZ();
				e.tickTime = dt;
				e.commit();
			}
		}
	}

	public static void blockEntityTicked(Level level, BlockEntity be, long dt) {
		((PerfStatsHolder) be.getType()).ce$perfStats().add(BLOCK_ENTITY_STATS, dt);
		if (dt >= SLOW_BLOCK_ENTITY_TICK_NANOS) {
			SlowBlockEntityTick e = new SlowBlockEntityTick();
			if (e.isEnabled()) {
				BlockPos p = be.getBlockPos();
				e.dimension = dimension(level);
				e.blockEntityType = ((PerfStatsHolder) be.getType()).ce$perfStats().key;
				e.x = p.getX();
				e.y = p.getY();
				e.z = p.getZ();
				e.tickTime = dt;
				e.commit();
			}
		}
	}

	// ── KubeJS handlers (KubeJSHandlerTiming) ──

	/** One totals entry per event + script + line, so a /reload reuses it instead of adding another. */
	public static HandlerStats handlerStats(String event, String source, int line) {
		seen(HOOK_KUBEJS);
		return HANDLERS.computeIfAbsent(event + "|" + source + "|" + line, k -> new HandlerStats(event, source, line));
	}

	/** Totals cover the server thread only (client and startup scripts would race the flush). */
	public static void handlerRan(HandlerStats s, long dt) {
		if (Thread.currentThread() == serverThread) s.add(HANDLER_STATS, dt);
	}

	public static void slowHandler(KubeJSHandler e, HandlerStats s) {
		e.event = s.event;
		e.source = s.source;
		e.line = s.line;
		e.commit();
	}

	// ── per-second totals ──

	private static void flush() {
		if (chunkRequests > 0) {
			ChunkWaitStats e = new ChunkWaitStats();
			e.requests = chunkRequests;
			e.waited = chunkWaited;
			e.longest = chunkLongest;
			e.commit();
			chunkRequests = chunkWaited = chunkLongest = 0;
		}
		for (TickStats s : ENTITY_STATS) {
			if (s.count == 0) continue;
			EntityTypeTicks e = new EntityTypeTicks();
			e.entityType = s.key;
			e.ticks = s.count;
			e.time = s.nanos;
			e.longest = s.longest;
			e.commit();
			s.reset();
		}
		for (TickStats s : BLOCK_ENTITY_STATS) {
			if (s.count == 0) continue;
			BlockEntityTypeTicks e = new BlockEntityTypeTicks();
			e.blockEntityType = s.key;
			e.ticks = s.count;
			e.time = s.nanos;
			e.longest = s.longest;
			e.commit();
			s.reset();
		}
		for (TickStats t : HANDLER_STATS) {
			if (t.count == 0) continue;
			HandlerStats s = (HandlerStats) t;
			KubeJSHandlerStats e = new KubeJSHandlerStats();
			e.event = s.event;
			e.source = s.source;
			e.line = s.line;
			e.calls = s.count;
			e.time = s.nanos;
			e.longest = s.longest;
			e.commit();
			s.reset();
		}
	}
}
