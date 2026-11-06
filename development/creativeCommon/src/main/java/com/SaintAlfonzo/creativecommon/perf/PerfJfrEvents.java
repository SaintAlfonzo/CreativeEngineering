package com.SaintAlfonzo.creativecommon.perf;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import jdk.jfr.Threshold;
import jdk.jfr.Timespan;

/**
 * The pack's own JFR events (feature_writeup/perf.md section 3.6). They only cost anything while a
 * recording enables them: the server's always-on recording does (server/perf/ce-server.jfc), a client
 * without a recording only pays a static boolean check per hook. The defaults below are what a recording
 * gets when its settings file does not name the event.
 *
 * Two kinds of timing:
 *  - ServerTick, LevelTick and KubeJSHandler use JFR's own duration (begin/end), so the jfc threshold applies.
 *  - The Slow* events and ChunkWait are measured by the hooks and carry the time in a field; they are only
 *    created past the fixed thresholds in {@link PerfEvents}, so their JFR duration is 0.
 * The *Stats / *Ticks events are per-second totals, written by the server thread at the end of a tick.
 */
public final class PerfJfrEvents {
	private PerfJfrEvents() {}

	@Name("creativecommon.ServerTick")
	@Label("Server Tick")
	@Category({"Creative Engineering", "Performance"})
	@Description("One whole server tick (MinecraftServer.tickServer), not averaged.")
	@StackTrace(false)
	public static class ServerTick extends Event {
		@Label("Tick")
		public int tick;
		@Label("Players")
		public int players;
	}

	@Name("creativecommon.LevelTick")
	@Label("Level Tick")
	@Category({"Creative Engineering", "Performance"})
	@Description("One dimension's part of a server tick (ServerLevel.tick).")
	@StackTrace(false)
	public static class LevelTick extends Event {
		@Label("Dimension")
		public String dimension;
		@Label("Loaded Chunks")
		public int loadedChunks;
		@Label("Players")
		public int players;
	}

	@Name("creativecommon.ChunkWait")
	@Label("Chunk Wait")
	@Category({"Creative Engineering", "Performance"})
	@Description("The server thread stopped in getChunk until a chunk was ready (it runs queued main-thread tasks meanwhile). "
			+ "The stack names who asked for the chunk.")
	public static class ChunkWait extends Event {
		@Label("Dimension")
		public String dimension;
		@Label("Chunk X")
		public int chunkX;
		@Label("Chunk Z")
		public int chunkZ;
		@Label("Status")
		public String status;
		@Label("Wait Time")
		@Timespan(Timespan.NANOSECONDS)
		public long waitTime;
	}

	@Name("creativecommon.ChunkWaitStats")
	@Label("Chunk Wait Totals")
	@Category({"Creative Engineering", "Performance"})
	@Description("Per second: server-thread chunk requests that missed the cache, and the time spent waiting for them.")
	@StackTrace(false)
	public static class ChunkWaitStats extends Event {
		@Label("Requests")
		public long requests;
		@Label("Waited")
		@Timespan(Timespan.NANOSECONDS)
		public long waited;
		@Label("Longest")
		@Timespan(Timespan.NANOSECONDS)
		public long longest;
	}

	@Name("creativecommon.KubeJSHandler")
	@Label("KubeJS Handler")
	@Category({"Creative Engineering", "Performance"})
	@Description("One slow call of one KubeJS event handler, named by its script and line.")
	@StackTrace(false)
	@Threshold("5 ms")
	public static class KubeJSHandler extends Event {
		@Label("Event")
		public String event;
		@Label("Script")
		public String source;
		@Label("Line")
		public int line;
	}

	@Name("creativecommon.KubeJSHandlerStats")
	@Label("KubeJS Handler Totals")
	@Category({"Creative Engineering", "Performance"})
	@Description("Per second and per handler: calls and time on the server thread.")
	@StackTrace(false)
	public static class KubeJSHandlerStats extends Event {
		@Label("Event")
		public String event;
		@Label("Script")
		public String source;
		@Label("Line")
		public int line;
		@Label("Calls")
		public long calls;
		@Label("Time")
		@Timespan(Timespan.NANOSECONDS)
		public long time;
		@Label("Longest")
		@Timespan(Timespan.NANOSECONDS)
		public long longest;
	}

	@Name("creativecommon.SlowEntityTick")
	@Label("Slow Entity Tick")
	@Category({"Creative Engineering", "Performance"})
	@Description("One entity tick over the fixed threshold, with its type and position.")
	@StackTrace(false)
	public static class SlowEntityTick extends Event {
		@Label("Dimension")
		public String dimension;
		@Label("Entity Type")
		public String entityType;
		@Label("X")
		public int x;
		@Label("Y")
		public int y;
		@Label("Z")
		public int z;
		@Label("Tick Time")
		@Timespan(Timespan.NANOSECONDS)
		public long tickTime;
	}

	@Name("creativecommon.EntityTypeTicks")
	@Label("Entity Ticks by Type")
	@Category({"Creative Engineering", "Performance"})
	@Description("Per second and per entity type (the registered type, not the class that declares tick()): ticks and time.")
	@StackTrace(false)
	public static class EntityTypeTicks extends Event {
		@Label("Entity Type")
		public String entityType;
		@Label("Ticks")
		public long ticks;
		@Label("Time")
		@Timespan(Timespan.NANOSECONDS)
		public long time;
		@Label("Longest")
		@Timespan(Timespan.NANOSECONDS)
		public long longest;
	}

	@Name("creativecommon.SlowBlockEntityTick")
	@Label("Slow Block Entity Tick")
	@Category({"Creative Engineering", "Performance"})
	@Description("One block entity tick over the fixed threshold, with its type and position.")
	@StackTrace(false)
	public static class SlowBlockEntityTick extends Event {
		@Label("Dimension")
		public String dimension;
		@Label("Block Entity Type")
		public String blockEntityType;
		@Label("X")
		public int x;
		@Label("Y")
		public int y;
		@Label("Z")
		public int z;
		@Label("Tick Time")
		@Timespan(Timespan.NANOSECONDS)
		public long tickTime;
	}

	@Name("creativecommon.BlockEntityTypeTicks")
	@Label("Block Entity Ticks by Type")
	@Category({"Creative Engineering", "Performance"})
	@Description("Per second and per block entity type: ticks and time.")
	@StackTrace(false)
	public static class BlockEntityTypeTicks extends Event {
		@Label("Block Entity Type")
		public String blockEntityType;
		@Label("Ticks")
		public long ticks;
		@Label("Time")
		@Timespan(Timespan.NANOSECONDS)
		public long time;
		@Label("Longest")
		@Timespan(Timespan.NANOSECONDS)
		public long longest;
	}
}
