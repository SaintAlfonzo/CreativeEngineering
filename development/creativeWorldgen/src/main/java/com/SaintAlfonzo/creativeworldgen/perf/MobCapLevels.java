package com.SaintAlfonzo.creativeworldgen.perf;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Mob cap levels (perf.md section 17). Each dimension's level comes from its spawnable chunk count, set by
 * {@code MobCapLevelTickMixin} just before vanilla builds the tick's spawn state; {@code MobCapMixin} then reads
 * {@link #cap} for the global and per-player caps, and {@code MobCapSpawnAreaMixin} reads {@link #outsideRadius} and {@link #outsideBand}.
 * Level 0 is vanilla. Server thread only: dimensions tick one after another, so {@link #active} always belongs to
 * the dimension whose spawning is running.
 */
public final class MobCapLevels {

	private static final MobCategory[] CATEGORIES = MobCategory.values();

	/** Last chunk count and level per dimension, for the hysteresis and /ceperf mobcap. */
	public static final class State {
		public int chunks;
		public int level;
	}

	private static final Map<ServerLevel, State> STATES = new WeakHashMap<>();
	/** Caps of the dimension spawning now, by category ordinal; null = vanilla. */
	private static int[] active;

	private static List<? extends String> scaledList;
	private static boolean[] scaled = new boolean[CATEGORIES.length];
	private static List<? extends String> bandList;
	private static boolean[] banded = new boolean[CATEGORIES.length];

	private MobCapLevels() {}

	/** Called once per dimension tick with vanilla's spawnable chunk count. */
	public static void update(ServerLevel level, int chunks) {
		CWGConfig.Perf cfg = CWGConfig.PERF;
		State s = STATES.computeIfAbsent(level, l -> new State());
		s.chunks = chunks;
		if (!cfg.mobCapLevels.get()) {
			s.level = 0;
		} else if (cfg.mobCapForceLevel.get() >= 0) {
			s.level = cfg.mobCapForceLevel.get();
		} else {
			s.level = pickLevel(s.level, chunks, cfg.mobCapLevel1Chunks.get(), cfg.mobCapLevel2Chunks.get(),
					cfg.mobCapHysteresisChunks.get());
		}
		active = s.level == 0 ? null : caps(s.level == 1 ? cfg.mobCapLevel1Monster.get() : cfg.mobCapLevel2Monster.get());
	}

	/** Rises as soon as a threshold is passed, falls only once the count is the margin below it. */
	static int pickLevel(int previous, int chunks, int level1, int level2, int margin) {
		int up = chunks > level2 ? 2 : chunks > level1 ? 1 : 0;
		if (up >= previous) return up;
		return chunks > level2 - margin ? 2 : chunks > level1 - margin ? 1 : 0;
	}

	private static int[] caps(int monsterCap) {
		List<? extends String> list = CWGConfig.PERF.mobCapScaledCategories.get();
		if (list != scaledList) {
			scaled = flags(list);
			scaledList = list;
		}
		double factor = (double) monsterCap / MobCategory.MONSTER.getMaxInstancesPerChunk();
		int[] out = new int[CATEGORIES.length];
		for (MobCategory c : CATEGORIES) {
			int vanilla = c.getMaxInstancesPerChunk();
			out[c.ordinal()] = scaled[c.ordinal()] ? Math.max(1, (int) Math.round(vanilla * factor)) : vanilla;
		}
		return out;
	}

	private static boolean[] flags(List<? extends String> names) {
		boolean[] out = new boolean[CATEGORIES.length];
		for (MobCategory c : CATEGORIES) out[c.ordinal()] = names.contains(c.getName());
		return out;
	}

	/** The per-chunk cap vanilla multiplies by the chunk count (global) or compares per player (local). */
	public static int cap(MobCategory category) {
		int[] caps = active;
		return caps == null ? category.getMaxInstancesPerChunk() : caps[category.ordinal()];
	}

	/** True when the chunk is outside the level's spawn radius (in chunks) of every player, for a scaled category. */
	public static boolean outsideRadius(ServerLevel level, MobCategory category, ChunkPos chunk) {
		State s = STATES.get(level);
		if (s == null || s.level == 0 || !scaled[category.ordinal()]) return false;
		CWGConfig.Perf cfg = CWGConfig.PERF;
		int radius = s.level == 1 ? cfg.spawnChunksLevel1.get() : cfg.spawnChunksLevel2.get();
		if (radius <= 0) return false;
		for (ServerPlayer player : level.players()) {
			if (player.isSpectator()) continue;
			ChunkPos p = player.chunkPosition();
			if (Math.max(Math.abs(p.x - chunk.x), Math.abs(p.z - chunk.z)) <= radius) return false;
		}
		return true;
	}

	/** True when the spawn position is farther above or below the nearest player than the level's band. */
	public static boolean outsideBand(ServerLevel level, MobCategory category, BlockPos pos) {
		State s = STATES.get(level);
		if (s == null || s.level == 0) return false;
		CWGConfig.Perf cfg = CWGConfig.PERF;
		int band = s.level == 1 ? cfg.spawnBandLevel1.get() : cfg.spawnBandLevel2.get();
		if (band <= 0) return false;
		List<? extends String> list = cfg.spawnBandCategories.get();
		if (list != bandList) {
			banded = flags(list);
			bandList = list;
		}
		if (!banded[category.ordinal()]) return false;
		Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, -1.0, false);
		return player != null && Math.abs(pos.getY() - player.getY()) > band;
	}

	/** Snapshot for /ceperf mobcap. */
	public static Map<ServerLevel, State> states() {
		return STATES;
	}
}
