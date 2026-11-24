// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.perf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.slf4j.Logger;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Idle random ticks (feature_writeup/random_tick_audit.md, plan_farming_cooking_batch.md F2): block states that can no
 * longer change on a random tick (grown colonies, waxed lanterns, Floral Enchantment bushes) report
 * {@code isRandomlyTicking() == false}, so a chunk section holding only such states is skipped by the random-tick loop.
 * Driven by {@code [perf] idleRandomTicks} / {@code idleRandomTickRules} ({@link IdleTickRules}); applied by
 * {@code mixin/IdleRandomTickMixin}, which caches the answer per state in one byte.
 *
 * <p>The answer must not change while a state sits in a loaded chunk (a section counts its ticking states when they are
 * set and when they are removed), so the rules are read once, at the first lookup after the config has loaded, and a
 * config change applies after a restart. Before that, states keep the block's own answer and are asked again later.
 *
 * <p>{@code +wake_above}: a stopped state whose upper neighbor changes to a new non-air block (not another ruled block)
 * gets up to {@link #WAKE_TRIES} scheduled ticks, roughly one random-tick interval apart, each running the block's own
 * random tick, until the block above changes (a vine climbed) or the tries run out. Kept in memory only.
 */
public final class IdleRandomTicks {
	private IdleRandomTicks() {}

	private static final Logger LOGGER = LogUtils.getLogger();

	public static final byte UNKNOWN = 0;
	public static final byte STOP = 1;
	public static final byte KEEP = 2;

	static final int WAKE_TRIES = 12;

	/** Block -> its rules; null until built, empty when the feature is off. */
	private static volatile Map<Block, List<IdleTickRules.Rule>> byBlock;

	/** Per level: packed pos -> pending wake. Server thread only. */
	private static final Map<ServerLevel, Long2ObjectOpenHashMap<Wake>> WAKES = new WeakHashMap<>();

	private static final class Wake {
		final Block above;
		int left;

		Wake(Block above, int left) {
			this.above = above;
			this.left = left;
		}
	}

	/** {@link #STOP}, {@link #KEEP}, or {@link #UNKNOWN} when the config or registry is not ready (ask again later). */
	public static byte classify(BlockState state) {
		Map<Block, List<IdleTickRules.Rule>> rules = rules();
		if (rules == null) return UNKNOWN;
		List<IdleTickRules.Rule> list = rules.get(state.getBlock());
		if (list == null) return KEEP;
		View view = new View(state);
		for (IdleTickRules.Rule r : list) {
			if (IdleTickRules.matches(r, view)) return STOP;
		}
		return KEEP;
	}

	private static Map<Block, List<IdleTickRules.Rule>> rules() {
		Map<Block, List<IdleTickRules.Rule>> m = byBlock;
		if (m != null) return m;
		synchronized (IdleRandomTicks.class) {
			if (byBlock != null) return byBlock;
			if (!CommonConfig.COMMON_SPEC.isLoaded()) return null;
			boolean on;
			List<? extends String> lines;
			try {
				on = CommonConfig.PERF.idleRandomTicks.get();
				lines = CommonConfig.PERF.idleRandomTickRules.get();
			} catch (IllegalStateException notLoaded) {
				return null;
			}
			Map<Block, List<IdleTickRules.Rule>> built = new IdentityHashMap<>();
			if (on) {
				build(lines, built);
				build(IdleTickRules.modRules(), built);
			}
			byBlock = built;
			return built;
		}
	}

	private static void build(List<? extends String> lines, Map<Block, List<IdleTickRules.Rule>> out) {
		int absent = 0;
		Map<String, Integer> perMod = new HashMap<>();
		for (String line : lines) {
			if (line == null || line.isBlank()) continue;
			IdleTickRules.Rule rule;
			try {
				rule = IdleTickRules.parse(line);
			} catch (IllegalArgumentException e) {
				LOGGER.warn("[perf] idleRandomTickRules: skipped '{}': {}", line, e.getMessage());
				continue;
			}
			ResourceLocation id = ResourceLocation.tryParse(rule.blockId());
			Block block = id == null || !ForgeRegistries.BLOCKS.containsKey(id) ? null : ForgeRegistries.BLOCKS.getValue(id);
			if (block == null) {
				absent++;
				continue;
			}
			String problem = IdleTickRules.check(rule, new View(block.defaultBlockState()));
			if (problem != null) {
				LOGGER.warn("[perf] idleRandomTickRules: skipped '{}': {}", line, problem);
				continue;
			}
			out.computeIfAbsent(block, b -> new ArrayList<>()).add(rule);
			perMod.merge(id.getNamespace(), 1, Integer::sum);
		}
		LOGGER.info("[perf] idle random ticks: {} rules on {} blocks {} ({} for blocks not in this pack)",
				perMod.values().stream().mapToInt(Integer::intValue).sum(), out.size(), perMod, absent);
	}

	/** True when the state is stopped by a {@code +wake_above} rule. */
	private static boolean wakes(BlockState state) {
		Map<Block, List<IdleTickRules.Rule>> rules = byBlock;
		if (rules == null) return false;
		List<IdleTickRules.Rule> list = rules.get(state.getBlock());
		if (list == null) return false;
		View view = new View(state);
		for (IdleTickRules.Rule r : list) {
			if (r.wakeAbove() && IdleTickRules.matches(r, view)) return true;
		}
		return false;
	}

	/** Mixin hook: BlockStateBase.updateShape from above on a stopped state. */
	public static void onAboveChanged(BlockState state, ServerLevel level, BlockPos pos, BlockState above) {
		if (!wakes(state)) return;
		Long2ObjectOpenHashMap<Wake> map = WAKES.computeIfAbsent(level, l -> new Long2ObjectOpenHashMap<>());
		Map<Block, List<IdleTickRules.Rule>> rules = byBlock;
		if (above.isAir() || rules == null || rules.containsKey(above.getBlock())) {
			// cleared, or the vine itself grew into the space: nothing new to climb
			map.remove(pos.asLong());
			return;
		}
		map.put(pos.asLong(), new Wake(above.getBlock(), WAKE_TRIES));
		schedule(level, pos, state.getBlock());
	}

	/** Mixin hook: BlockStateBase.tick (scheduled tick) on a stopped state. Runs a pending wake; the block's own tick
	 * still runs after it. */
	public static void onScheduledTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		Long2ObjectOpenHashMap<Wake> map = WAKES.get(level);
		if (map == null) return;
		long key = pos.asLong();
		Wake w = map.get(key);
		if (w == null) return;
		if (level.getBlockState(pos.above()).getBlock() != w.above) {
			map.remove(key);
			return;
		}
		state.getBlock().randomTick(state, level, pos, random);
		BlockState now = level.getBlockState(pos);
		if (--w.left > 0 && now.getBlock() == state.getBlock()
				&& level.getBlockState(pos.above()).getBlock() == w.above) {
			schedule(level, pos, state.getBlock());
		} else {
			map.remove(key);
		}
	}

	/** About one random-tick interval for one block (4096 / randomTickSpeed), jittered; none when random ticks are off. */
	private static void schedule(ServerLevel level, BlockPos pos, Block block) {
		int speed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
		if (speed <= 0) return;
		int mean = Math.max(20, 4096 / speed);
		level.scheduleTick(pos, block, mean / 2 + level.random.nextInt(mean));
	}

	/** {@link IdleTickRules.StateView} over a real block state. */
	private record View(BlockState state) implements IdleTickRules.StateView {
		@Override
		public String value(String prop) {
			Property<?> p = state.getBlock().getStateDefinition().getProperty(prop);
			return p == null ? null : name(p, state);
		}

		@Override
		public String bound(String prop, boolean max) {
			if (!(state.getBlock().getStateDefinition().getProperty(prop) instanceof IntegerProperty ip)) return null;
			int best = max ? Integer.MIN_VALUE : Integer.MAX_VALUE;
			for (Integer v : ip.getPossibleValues()) best = max ? Math.max(best, v) : Math.min(best, v);
			return Integer.toString(best);
		}

		private static <T extends Comparable<T>> String name(Property<T> p, BlockState s) {
			return p.getName(s.getValue(p));
		}
	}
}
