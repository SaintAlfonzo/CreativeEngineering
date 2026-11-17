package com.SaintAlfonzo.creativedevtools.test;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Time verbs of {@code /cdevtest} (plan_scratch_testing_expansion.md TX-21; 1.19.2 has no {@code /tick}):
 * <ul>
 *   <li>{@code tickwarp N}: the next N server ticks run back to back. Every tick is a full one (levels, entities,
 *       block and random ticks near players, Forge tick events); only the wait between ticks goes, by pulling
 *       MinecraftServer.nextTickTime back to now at the end of each tick. {@code tickwarp stop|status}.</li>
 *   <li>{@code randomtick X1 Y1 Z1 X2 Y2 Z2 N}: every randomly ticking block (and fluid) in the box gets N random ticks,
 *       no player needed. N random ticks per block = N*4096/randomTickSpeed game ticks of exposure.</li>
 *   <li>{@code blocktick X Y Z [fluid]}: runs the block's (or fluid's) scheduled tick now.</li>
 *   <li>{@code watch X Y Z TICKS EVERY [what=state|inv|tank|be]}: a line every EVERY ticks for TICKS ticks.</li>
 * </ul>
 */
public final class TestTicks {

	/** randomtick refuses more than this many block visits (box volume times N). */
	static final long MAX_VISITS = 50_000_000L;

	private static int warpLeft, warpTotal;
	private static long warpStartMs, warpStartTick;
	private static TestCtx warpCtx;
	private static Field nextTickTime;
	private static final List<Watch> WATCHES = new ArrayList<>();

	private TestTicks() {
	}

	static void register() {
		TestCommand.add("tickwarp", TestTicks::tickwarp);
		TestCommand.add("randomtick", TestTicks::randomtick, "nofluids");
		TestCommand.add("blocktick", TestTicks::blocktick, "fluid");
		TestCommand.add("watch", TestTicks::watch);
	}

	// ---- tick warp

	private static void tickwarp(TestCtx c) {
		String w = c.args.word("ticks|stop|status");
		c.args.end();
		c.args.checkOpts();
		if (w.equals("status")) {
			c.emit(c.line().kv("running", warpLeft > 0).kv("left", warpLeft).kv("total", warpTotal));
			return;
		}
		if (w.equals("stop")) {
			int left = warpLeft;
			if (left > 0) finishWarp(c.server, "stopped");
			c.emit(c.line().kv("stopped", left > 0).kv("left", left));
			return;
		}
		int n;
		try {
			n = Integer.parseInt(w);
		} catch (NumberFormatException e) {
			throw new TestArgs.Bad("ticks not an integer: " + w);
		}
		if (n < 1 || n > 1_728_000) throw new TestArgs.Bad("ticks out of range 1..1728000 (one day of game time is 24000)");
		if (warpLeft > 0) throw new TestArgs.Bad("a warp is running (" + warpLeft + " left): tickwarp stop first");
		field();
		warpLeft = n;
		warpTotal = n;
		warpStartMs = Util.getMillis();
		warpStartTick = c.server.getTickCount();
		warpCtx = c;
		c.emit(c.line().kv("started", n).kv("tick", warpStartTick));
	}

	private static Field field() {
		if (nextTickTime == null) {
			// MinecraftServer.nextTickTime (protected long); the SRG name is remapped in a dev run
			nextTickTime = ObfuscationReflectionHelper.findField(MinecraftServer.class, "f_129726_");
		}
		return nextTickTime;
	}

	private static void finishWarp(MinecraftServer server, String how) {
		long ms = Math.max(1, Util.getMillis() - warpStartMs);
		int ran = (int) (server.getTickCount() - warpStartTick);
		TestCtx c = warpCtx;
		warpLeft = 0;
		warpCtx = null;
		if (c != null) {
			c.emit(c.line().kv("end", how).kv("ticks", ran).kv("asked", warpTotal).kv("ms", ms)
					.kv("tps", String.format(java.util.Locale.ROOT, "%.1f", ran * 1000.0 / ms)));
		}
	}

	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
		if (server == null) return;
		if (!WATCHES.isEmpty()) tickWatches(server);
		if (warpLeft <= 0) return;
		// the tick the command ran in is not one of the n warped ticks (it ran at normal speed): count from the next
		if (server.getTickCount() > warpStartTick && --warpLeft <= 0) {
			finishWarp(server, "done");
			return;
		}
		try {
			// runServer adds 50 ms to nextTickTime before each tick and waits for it after: from now, no wait
			field().setLong(server, Util.getMillis());
		} catch (IllegalAccessException e) {
			TestCtx.LOGGER.error("[cdevtest] tickwarp cannot set nextTickTime, stopping", e);
			finishWarp(server, "failed");
		}
	}

	// ---- random and scheduled ticks

	private static void randomtick(TestCtx c) {
		ServerLevel level = c.level();
		int[] a = c.args.xyz("from"), b = c.args.xyz("to");
		int n = c.args.integer("n");
		c.args.end();
		c.args.checkOpts("dim", "nofluids");
		if (n < 1) throw new TestArgs.Bad("n must be at least 1");
		long vol = TestArgs.volume(a, b);
		if (vol > TestWorld.MAX_BOX) throw new TestArgs.Bad("box over " + TestWorld.MAX_BOX + " blocks");
		if (vol * n > MAX_VISITS) throw new TestArgs.Bad("box volume x n over " + MAX_VISITS + ": split the call");
		boolean fluids = !c.args.flag("nofluids");
		Map<Long, BlockState> start = new HashMap<>();
		TestWorld.scan(level, a, b, (p, st) -> start.put(p.asLong(), st));
		long t0 = Util.getMillis();
		long[] ticked = {0, 0};
		int skipped = 0;
		for (int round = 0; round < n; round++) {
			skipped = TestWorld.scan(level, a, b, (p, st) -> {
				BlockPos at = p.immutable();
				if (st.isRandomlyTicking()) {
					st.randomTick(level, at, level.random);
					ticked[0]++;
				}
				if (fluids) {
					FluidState fs = level.getFluidState(at);
					if (fs.isRandomlyTicking()) {
						fs.randomTick(level, at, level.random);
						ticked[1]++;
					}
				}
			});
		}
		int[] changed = {0};
		TestWorld.scan(level, a, b, (p, st) -> {
			BlockState was = start.get(p.asLong());
			if (was != null && was != st) changed[0]++;
		});
		int speed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
		c.emit(c.line().kv("rounds", n).kv("block_ticks", ticked[0]).kv("fluid_ticks", ticked[1]).kv("changed", changed[0])
				.kv("equiv_game_ticks", speed > 0 ? (long) n * 4096L / speed : -1).kv("ms", Util.getMillis() - t0)
				.kv("skipped_columns", skipped));
	}

	private static void blocktick(TestCtx c) {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		c.args.end();
		c.args.checkOpts("dim", "fluid");
		TestCtx.loaded(level, pos);
		String before = TestWorld.stateStr(level.getBlockState(pos));
		if (c.args.flag("fluid")) {
			FluidState fs = level.getFluidState(pos);
			if (!fs.isEmpty()) fs.tick(level, pos);
		} else {
			level.getBlockState(pos).tick(level, pos, level.random);
		}
		c.emit(c.line().kv("before", before).kv("after", TestWorld.stateStr(level.getBlockState(pos))));
	}

	// ---- watch

	private record Watch(TestCtx c, ServerLevel level, BlockPos pos, Direction side, String what, int every, long start, long end) {
	}

	private static void watch(TestCtx c) {
		ServerLevel level = c.level();
		BlockPos pos = c.pos("pos");
		int ticks = c.args.integer("ticks"), every = c.args.integer("every");
		c.args.end();
		c.args.checkOpts("dim", "what", "face");
		TestCtx.loaded(level, pos);
		if (ticks < 1 || ticks > 72_000 || every < 1) throw new TestArgs.Bad("ticks 1..72000, every 1 or more");
		String what = c.args.opt("what", "state");
		if (!List.of("state", "inv", "tank", "be").contains(what)) throw new TestArgs.Bad("what must be state, inv, tank or be");
		String f = c.args.opt("face", "null");
		long now = c.server.getTickCount();
		WATCHES.add(new Watch(c, level, pos, f.equals("null") ? null : TestCtx.face(f), what, every, now, now + ticks));
		c.emit(c.line().kv("watching", what).kv("ticks", ticks).kv("every", every));
		sample(WATCHES.get(WATCHES.size() - 1), 0L);
	}

	private static void tickWatches(MinecraftServer server) {
		long now = server.getTickCount();
		for (Iterator<Watch> it = WATCHES.iterator(); it.hasNext(); ) {
			Watch w = it.next();
			long t = now - w.start();
			// t=0 was sampled when the watch was added (same tick)
			if ((t > 0 && t % w.every() == 0) || now >= w.end()) sample(w, t);
			if (now >= w.end()) {
				it.remove();
				w.c().emit(w.c().line().kv("end", "done").kv("t", t));
			}
		}
	}

	private static void sample(Watch w, long t) {
		TestCtx c = w.c();
		try {
			if (!w.level().hasChunkAt(w.pos())) {
				c.emit(c.line().kv("t", t).kv("state", "unloaded"));
				return;
			}
			switch (w.what()) {
				case "state" -> c.emit(c.line().kv("t", t).kv("state", TestWorld.stateStr(w.level().getBlockState(w.pos()))));
				case "inv" -> TestWorld.invLines(c, w.level(), w.pos(), w.side(), t);
				case "tank" -> TestWorld.tankLines(c, w.level(), w.pos(), w.side(), t);
				default -> {
					var be = w.level().getBlockEntity(w.pos());
					c.emit(c.line().kv("t", t).kv("nbt", be == null ? "none" : be.saveWithFullMetadata().toString()));
				}
			}
		} catch (VirtualMachineError e) {
			throw e;
		} catch (Throwable e) {
			c.emit(c.line().kv("t", t).kv("error", e.getClass().getSimpleName() + ": " + e.getMessage()));
		}
	}
}
