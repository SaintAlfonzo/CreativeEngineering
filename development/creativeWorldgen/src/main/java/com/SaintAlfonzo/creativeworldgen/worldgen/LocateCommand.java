package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativecommon.util.WorldgenFiles;
import com.SaintAlfonzo.creativeworldgen.compat.AsyncLocatorBridge;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * /cwglocate (feature_writeup/locate.md 3.4 to 3.6), op level 2. Everything runs on Async Locator's thread pool, so its
 * asyncLocatorThreads setting is the parallelism; the server thread only starts work, prints progress and writes the
 * reports.
 *
 *  - batch [filter] [radius]: one search per live structure (filter: all, a namespace, an id, an id ending in *, or
 *    #tag) from the source's position; per structure found or not, distance, candidates, loads, misses, predictions
 *    saved by stage, time. kubejs/exported/worldgen/locate_batch.json.
 *  - census radius [x z]: every slot of every live set in the square (LocateCensus).
 *    kubejs/exported/worldgen/locate_census.json.
 *  - stats [reset]: totals of every search since boot (LocateStats). kubejs/exported/worldgen/locate_stats.json.
 *  - status, stop (stop also ends every /locate and map search running now; structure_tuning.md 7.3.2).
 */
public final class LocateCommand {

	private static final Logger LOG = LogManager.getLogger("creativeworldgen/locate");
	static final String BATCH_OUT = "kubejs/exported/worldgen/locate_batch.json";
	static final String CENSUS_OUT = "kubejs/exported/worldgen/locate_census.json";
	static final String STATS_OUT = "kubejs/exported/worldgen/locate_stats.json";
	private static final long PROGRESS_MS = 10_000;

	private static final class Item {
		final String id;
		final HolderSet<Structure> set;
		final LocateStats.Search[] search;
		AsyncLocatorBridge.Handle handle;
		volatile boolean done;
		volatile Pair<BlockPos, Holder<Structure>> result;
		volatile String status = "pending";

		Item(String id, HolderSet<Structure> set) {
			this.id = id;
			this.set = set;
			this.search = LocateStats.capture(set);
		}
	}

	private static final class Batch {
		final ServerLevel level;
		final CommandSourceStack source;
		final BlockPos origin;
		final int radius;
		final List<Item> items = new ArrayList<>();
		final long started = System.currentTimeMillis();
		long lastProgress = started;
		boolean stopped;

		Batch(ServerLevel level, CommandSourceStack source, BlockPos origin, int radius) {
			this.level = level; this.source = source; this.origin = origin; this.radius = radius;
		}
	}

	private static Batch batch;                     // server thread only
	private static LocateCensus censusJob;          // server thread only (also published to LocateStats.census)
	private static CommandSourceStack censusSource;
	private static long censusLastProgress;

	private LocateCommand() {}

	// ── commands ─────────────────────────────────────────────────────────────

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("cwglocate").requires(s -> s.hasPermission(2))
				.then(Commands.literal("batch")
						.executes(ctx -> batch(ctx, "all", WorldgenConfig.locateBatchRadius()))
						.then(Commands.argument("filter", StringArgumentType.string())
								.executes(ctx -> batch(ctx, StringArgumentType.getString(ctx, "filter"), WorldgenConfig.locateBatchRadius()))
								.then(Commands.argument("radius", IntegerArgumentType.integer(1, 1000))
										.executes(ctx -> batch(ctx, StringArgumentType.getString(ctx, "filter"), IntegerArgumentType.getInteger(ctx, "radius"))))))
				.then(Commands.literal("census")
						.then(Commands.argument("radius", IntegerArgumentType.integer(1, 2048))
								.executes(ctx -> census(ctx, IntegerArgumentType.getInteger(ctx, "radius"), null))
								.then(Commands.argument("x", IntegerArgumentType.integer())
										.then(Commands.argument("z", IntegerArgumentType.integer())
												.executes(ctx -> census(ctx, IntegerArgumentType.getInteger(ctx, "radius"),
														new BlockPos(IntegerArgumentType.getInteger(ctx, "x"), 0, IntegerArgumentType.getInteger(ctx, "z"))))))))
				.then(Commands.literal("stats")
						.executes(ctx -> stats(ctx.getSource(), false))
						.then(Commands.literal("reset").executes(ctx -> stats(ctx.getSource(), true))))
				.then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
				.then(Commands.literal("stop").executes(ctx -> stop(ctx.getSource())));
		event.getDispatcher().register(root);
	}

	private static int batch(CommandContext<CommandSourceStack> ctx, String filter, int radius) {
		CommandSourceStack source = ctx.getSource();
		if (!AsyncLocatorBridge.available()) return fail(source, "Async Locator is not installed; /cwglocate needs its thread pool.");
		if (batch != null) return fail(source, "A batch is already running (/cwglocate status, /cwglocate stop).");
		ServerLevel level = source.getLevel();
		List<Holder<Structure>> targets = targets(level, filter);
		if (targets.isEmpty()) return fail(source, "No live structure matches '" + filter + "' in " + level.dimension().location() + ".");
		Batch b = new Batch(level, source, new BlockPos(source.getPosition()), radius);
		for (Holder<Structure> h : targets) {
			String id = h.unwrapKey().map(k -> k.location().toString()).orElse("?");
			b.items.add(new Item(id, HolderSet.direct(h)));
		}
		batch = b;
		for (Item item : b.items) {
			try {
				item.handle = AsyncLocatorBridge.locate(level, item.set, b.origin, radius, false);
				item.handle.future().whenComplete((r, e) -> {
					item.result = r;
					LocateStats.Search s = item.search[0];
					String ended = s == null ? null : s.ended;
					// a queued search that /cwglocate stop cancels completes with a CancellationException (05_phase_5.md 3.4)
					Throwable cause = e instanceof CompletionException && e.getCause() != null ? e.getCause() : e;
					item.status = cause instanceof CancellationException ? "stopped" : e != null ? "error: " + e : r != null ? "found"
							: ended == null ? "not_found" : ended.startsWith("timed out") ? "timed_out" : "stopped";
					item.done = true;
				});
			} catch (RuntimeException e) {
				item.status = "error: " + e;
				item.done = true;
			}
		}
		source.sendSuccess(Component.literal("[cwglocate] batch: " + b.items.size() + " structures from " + b.origin.getX() + " " + b.origin.getZ()
				+ ", radius " + radius + " chunks, " + threads() + " locator thread(s). Report: " + BATCH_OUT), true);
		return b.items.size();
	}

	private static int census(CommandContext<CommandSourceStack> ctx, int radius, BlockPos centerBlock) {
		CommandSourceStack source = ctx.getSource();
		if (!AsyncLocatorBridge.available()) return fail(source, "Async Locator is not installed; /cwglocate needs its thread pool.");
		if (censusJob != null) return fail(source, "A census is already running (/cwglocate status, /cwglocate stop).");
		ServerLevel level = source.getLevel();
		BlockPos at = centerBlock != null ? centerBlock : new BlockPos(source.getPosition());
		LocateCensus c = new LocateCensus(level, new ChunkPos(at), radius);
		c.enumerate(WorldgenConfig.locateCensusRegionChunks());
		if (c.slotCount == 0) return fail(source, "No structure slots in that square.");
		censusJob = c;
		censusSource = source;
		censusLastProgress = System.currentTimeMillis();
		LocateStats.census = c;
		try {
			for (List<LocateCensus.Slot> region : c.regions.values()) AsyncLocatorBridge.submit(() -> c.runRegion(region));
		} catch (ReflectiveOperationException | RuntimeException e) {
			c.cancelled.set(true);
			finishCensus();
			return fail(source, "Could not use Async Locator's thread pool: " + e);
		}
		source.sendSuccess(Component.literal("[cwglocate] census: " + c.slotCount + " slot chunks in " + c.regions.size() + " regions around chunk "
				+ c.center.x + " " + c.center.z + " (radius " + radius + "), " + threads() + " locator thread(s). Report: " + CENSUS_OUT
				+ (c.skippedPlacements.isEmpty() ? "" : "; skipped " + c.skippedPlacements.size() + " sets with custom placement types")), true);
		return c.slotCount;
	}

	private static int stats(CommandSourceStack source, boolean reset) {
		if (reset) {
			LocateStats.resetTotals();
			source.sendSuccess(Component.literal("[cwglocate] search totals cleared."), true);
			return 1;
		}
		List<Map<String, Object>> rows = LocateStats.totals();
		write(STATS_OUT, Map.of("searches", rows));
		source.sendSuccess(Component.literal("[cwglocate] " + rows.size() + " structures searched since boot (" + STATS_OUT + "). Most misses:"), false);
		for (Map<String, Object> r : rows.subList(0, Math.min(10, rows.size()))) {
			source.sendSuccess(Component.literal("  " + r.get("structure") + ": " + r.get("misses") + " misses, " + r.get("hits") + "/" + r.get("searches")
					+ " found, " + r.get("ms") + " ms"), false);
		}
		return rows.size();
	}

	private static int status(CommandSourceStack source) {
		if (batch == null && censusJob == null) {
			source.sendSuccess(Component.literal("[cwglocate] nothing running."), false);
			return 0;
		}
		if (batch != null) source.sendSuccess(Component.literal(batchProgress(batch)), false);
		if (censusJob != null) source.sendSuccess(Component.literal(censusProgress(censusJob)), false);
		return 1;
	}

	private static int stop(CommandSourceStack source) {
		if (batch != null) {
			batch.stopped = true;
			for (Item item : batch.items) if (!item.done && item.handle != null) item.handle.cancel().run();
		}
		if (censusJob != null) censusJob.cancelled.set(true);
		LocateStats.stopAll();
		source.sendSuccess(Component.literal("[cwglocate] stopping every running search (batch, census, /locate, maps); the reports are written with what finished."), true);
		return 1;
	}

	// ── progress and reports (server thread) ─────────────────────────────────

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		long now = System.currentTimeMillis();
		if (batch != null) tickBatch(batch, now);
		if (censusJob != null) {
			if (censusJob.regionsDone.get() >= censusJob.regions.size()) finishCensus();
			else if (now - censusLastProgress >= PROGRESS_MS) {
				censusLastProgress = now;
				censusSource.sendSuccess(Component.literal(censusProgress(censusJob)), false);
			}
		}
	}

	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		LocateStats.resume();
	}

	/** Every running search ends at its next candidate or load (LocateStats#expired), so stop doesn't wait on Async Locator's threads. */
	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		LocateStats.shutdown();
		if (batch != null) for (Item item : batch.items) if (!item.done && item.handle != null) item.handle.cancel().run();
		if (censusJob != null) censusJob.cancelled.set(true);
		batch = null;
		censusJob = null;
		LocateStats.census = null;
	}

	private static void tickBatch(Batch b, long now) {
		long timeoutNanos = WorldgenConfig.locateBatchTimeoutSeconds() * 1_000_000_000L;
		int done = 0;
		for (Item item : b.items) {
			if (!item.done) {
				LocateStats.Search s = item.search[0];
				if (s != null && System.nanoTime() - s.started > timeoutNanos && item.handle != null) {
					item.handle.cancel().run();
					item.status = "timed_out";
					item.done = true;
				} else if (b.stopped && item.handle == null) {
					item.done = true;
				}
			}
			if (item.done) done++;
		}
		if (done == b.items.size() || (b.stopped && b.items.stream().allMatch(i -> i.done || i.handle == null || i.handle.future().isDone()))) {
			finishBatch(b);
		} else if (now - b.lastProgress >= PROGRESS_MS) {
			b.lastProgress = now;
			b.source.sendSuccess(Component.literal(batchProgress(b)), false);
		}
	}

	private static void finishBatch(Batch b) {
		batch = null;
		List<Map<String, Object>> rows = new ArrayList<>();
		int found = 0;
		long misses = 0;
		for (Item item : b.items) {
			LocateStats.release(item.set);
			LocateStats.Search s = item.search[0];
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("structure", item.id);
			String status = item.done ? item.status : "stopped";
			m.put("status", status);
			if ("found".equals(status)) found++;
			Pair<BlockPos, Holder<Structure>> r = item.result;
			if (r != null) {
				m.put("pos", List.of(r.getFirst().getX(), r.getFirst().getZ()));
				m.put("foundAs", r.getSecond().unwrapKey().map(k -> k.location().toString()).orElse("?"));
				m.put("distance", (int) Math.round(Math.hypot(r.getFirst().getX() - b.origin.getX(), r.getFirst().getZ() - b.origin.getZ())));
			}
			if (s != null) {
				m.put("candidates", s.candidates);
				m.put("loads", s.loads);
				m.put("misses", s.misses);
				misses += s.misses;
				Map<String, Long> saved = new LinkedHashMap<>();
				for (LocateStats.Stage st : LocateStats.Stage.values()) saved.put(st.name().toLowerCase(Locale.ROOT), s.saved[st.ordinal()]);
				m.put("savedByStage", saved);
				m.put("ms", s.millis);
				m.put("thread", s.thread);
			}
			rows.add(m);
		}
		rows.sort((x, y) -> Integer.compare((Integer) y.getOrDefault("misses", 0), (Integer) x.getOrDefault("misses", 0)));
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("dimension", b.level.dimension().location().toString());
		root.put("origin", List.of(b.origin.getX(), b.origin.getZ()));
		root.put("radiusChunks", b.radius);
		root.put("locatorThreads", threads());
		root.put("seconds", (System.currentTimeMillis() - b.started) / 1000);
		root.put("structures", rows);
		write(BATCH_OUT, root);
		String msg = "[cwglocate] batch done: " + found + "/" + b.items.size() + " found, " + misses + " misses in total, "
				+ (System.currentTimeMillis() - b.started) / 1000 + " s. " + BATCH_OUT;
		b.source.sendSuccess(Component.literal(msg), true);
		LOG.info(msg);
	}

	private static void finishCensus() {
		LocateCensus c = censusJob;
		censusJob = null;
		LocateStats.census = null;
		if (c == null) return;
		write(CENSUS_OUT, c.report(threads()));
		long[] t = c.totals();
		String msg = "[cwglocate] census done" + (c.cancelled.get() ? " (stopped)" : "") + ": " + c.slotsDone.get() + "/" + c.slotCount + " slot chunks, "
				+ t[0] + " starts, " + t[1] + " misses, " + t[2] + " false negatives" + (t[2] == 0 ? "" : " (prediction bug: see falseNegativeAt)")
				+ ", " + (System.currentTimeMillis() - c.started) / 1000 + " s. " + CENSUS_OUT;
		if (censusSource != null) censusSource.sendSuccess(Component.literal(msg), true);
		LOG.info(msg);
	}

	private static String batchProgress(Batch b) {
		long done = b.items.stream().filter(i -> i.done).count();
		return "[cwglocate] batch: " + done + "/" + b.items.size() + " searches done, " + (System.currentTimeMillis() - b.started) / 1000 + " s";
	}

	private static String censusProgress(LocateCensus c) {
		return "[cwglocate] census: " + c.slotsDone.get() + "/" + c.slotCount + " slot chunks, " + c.regionsDone.get() + "/" + c.regions.size()
				+ " regions, " + (System.currentTimeMillis() - c.started) / 1000 + " s";
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	/** Live structures (at least one live placement, or converted from anchors) matching the filter. */
	private static List<Holder<Structure>> targets(ServerLevel level, String filter) {
		Registry<Structure> reg = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		var generator = level.getChunkSource().getGenerator();
		var randomState = level.getChunkSource().randomState();
		generator.ensureStructuresGenerated(randomState);
		Set<Holder<Structure>> candidates = new LinkedHashSet<>();
		if (filter.startsWith("#")) {
			reg.getTag(TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(filter.substring(1)))).ifPresent(tag -> tag.forEach(candidates::add));
		} else {
			for (Map.Entry<ResourceKey<Structure>, Structure> e : reg.entrySet()) {
				String id = e.getKey().location().toString();
				boolean match = filter.equals("all") || id.equals(filter) || e.getKey().location().getNamespace().equals(filter)
						|| (filter.endsWith("*") && id.startsWith(filter.substring(0, filter.length() - 1)));
				if (match) reg.getHolder(e.getKey()).ifPresent(candidates::add);
			}
		}
		List<Holder<Structure>> live = new ArrayList<>();
		for (Holder<Structure> h : candidates) {
			boolean placed = !LocateReflect.placementsFor(generator, h, randomState).isEmpty();
			if (placed || !MetaSlots.anchorsOf(level.registryAccess(), h.value()).isEmpty()) live.add(h);
		}
		live.sort((a, b) -> a.unwrapKey().map(k -> k.location().toString()).orElse("").compareTo(b.unwrapKey().map(k -> k.location().toString()).orElse("")));
		return live;
	}

	private static int threads() {
		try {
			ExecutorService e = AsyncLocatorBridge.executor();
			return e instanceof ThreadPoolExecutor t ? t.getMaximumPoolSize() : -1;
		} catch (ReflectiveOperationException | RuntimeException e) {
			return -1;
		}
	}

	private static void write(String rel, Object json) {
		try {
			WorldgenFiles.writeText(rel, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(json) + "\n");
		} catch (Exception e) {
			LOG.error("[cwglocate] could not write {}", rel, e);
		}
	}

	private static int fail(CommandSourceStack source, String message) {
		source.sendFailure(Component.literal("[cwglocate] " + message));
		return 0;
	}
}
