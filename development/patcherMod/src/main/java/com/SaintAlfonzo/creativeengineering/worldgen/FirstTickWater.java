package com.SaintAlfonzo.creativeengineering.worldgen;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * No item drops while a chunk's generated water settles (feature_writeup/perf.md 10.2 #5).
 *
 * <p>Vanilla runs a chunk's generation leftovers only when it first becomes ticking: {@code ChunkMap.prepareTickingChunk}
 * calls {@code LevelChunk.postProcessGeneration}, which ticks every aquifer / carver water mark, and the fluid ticks that
 * springs scheduled during generation fire on the first ticks. That water then flows into caves for the first time and
 * replaces moss carpet, grass, azalea, cave vines, torches and the like with drops, and its block updates make gravel
 * and sand fall. Pregenerated chunks hold none of this until a player comes near.
 *
 * <p>Here a chunk that ticks for the first time opens a window ({@code [worldgen.first_tick_water] windowTicks}); water
 * ticking in that chunk, and anywhere that water spreads to, is "settling" until the window ends. The water flows as in
 * vanilla, but no item drops while a settling fluid tick runs ({@code BlockPopResourceSettleMixin}): the blocks it
 * replaces, and the plants that lose their support in the same tick, vanish without items. Nothing runs during
 * generation, so world creation and pregen cost nothing extra. Server thread only.
 *
 * <p>The window also opens when only {@code holdGeneratedFallingBlocks} is on: {@link GeneratedFallingBlocks} reads
 * {@link #settlingTick} to keep sand and gravel next to settling water in place. Drops are skipped only with
 * {@code firstTickWater} on.
 */
public final class FirstTickWater {

	private FirstTickWater() {}

	private static final long PURGE_EVERY = 200L;

	private static final class State {
		final Long2LongOpenHashMap chunks = new Long2LongOpenHashMap();
		final Long2LongOpenHashMap positions = new Long2LongOpenHashMap();
		long nextPurge;
	}

	private static final Map<Level, State> STATES = new WeakHashMap<>();
	/** True while a settling fluid tick runs; read by the popResource hook. */
	private static boolean ticking;

	/** {@code LevelChunk.postProcessGeneration} HEAD: a chunk with generation leftovers starts ticking. */
	public static void onFirstTicking(LevelChunk chunk) {
		if (!(chunk.getLevel() instanceof ServerLevel level)) return;
		if (!WorldgenConfig.firstTickWaterEnabled() && !WorldgenConfig.holdGeneratedFallingBlocksEnabled()) return;
		long window = WorldgenConfig.firstTickWaterWindowTicks();
		if (window <= 0L) return;
		if (chunk.getInhabitedTime() != 0L && !hasMarks(chunk)) return;
		State s = STATES.computeIfAbsent(level, l -> new State());
		long now = level.getGameTime();
		s.chunks.put(chunk.getPos().toLong(), now + window);
		purge(s, now);
	}

	private static boolean hasMarks(LevelChunk chunk) {
		for (ShortList list : chunk.getPostProcessing()) {
			if (list != null && !list.isEmpty()) return true;
		}
		return false;
	}

	/** {@code FlowingFluid.tick} HEAD. */
	public static void beginTick(Level level, BlockPos pos) {
		ticking = level instanceof ServerLevel && isSettling(level, pos);
	}

	/** {@code FlowingFluid.tick} RETURN. */
	public static void endTick() {
		ticking = false;
	}

	/** {@code FlowingFluid.spreadTo} HEAD: the target settles too, so the flood keeps the rule across chunk edges. */
	public static void onSpread(BlockGetter level, BlockPos target) {
		if (!ticking || !(level instanceof ServerLevel sl)) return;
		State s = STATES.get(sl);
		if (s != null) s.positions.put(target.asLong(), sl.getGameTime() + WorldgenConfig.firstTickWaterWindowTicks());
	}

	/** {@code Block.popResource} HEAD: true while a settling fluid tick runs, so the drop is skipped. */
	public static boolean suppressDrops() {
		return ticking && WorldgenConfig.firstTickWaterEnabled();
	}

	/** True while a settling fluid tick runs; {@link GeneratedFallingBlocks} holds falling blocks it disturbs. */
	public static boolean settlingTick() {
		return ticking;
	}

	private static boolean isSettling(Level level, BlockPos pos) {
		State s = STATES.get(level);
		if (s == null || (s.chunks.isEmpty() && s.positions.isEmpty())) return false;
		long now = level.getGameTime();
		long until = s.chunks.getOrDefault(ChunkPos.asLong(pos), Long.MIN_VALUE);
		if (until >= now) return true;
		until = s.positions.getOrDefault(pos.asLong(), Long.MIN_VALUE);
		return until >= now;
	}

	private static void purge(State s, long now) {
		if (now < s.nextPurge) return;
		s.nextPurge = now + PURGE_EVERY;
		s.chunks.long2LongEntrySet().removeIf(e -> e.getLongValue() < now);
		s.positions.long2LongEntrySet().removeIf((Long2LongMap.Entry e) -> e.getLongValue() < now);
	}
}
