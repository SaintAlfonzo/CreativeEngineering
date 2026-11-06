package com.SaintAlfonzo.creativeworldgen.worldgen;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FallingBlock;

/**
 * Falling blocks that generation leaves floating stay in place until a real block update next to them
 * ({@code [worldgen.systems] holdGeneratedFallingBlocks}, feature_writeup/perf.md 13).
 *
 * <p>Generation never makes sand or gravel fall itself; they fall when the chunk first ticks, for three reasons:
 * {@code LevelChunk.postProcessGeneration} re-runs {@code updateShape} on every marked position (blocks above
 * carvers and lakes), generated water flows for the first time ({@link FirstTickWater}) and updates its neighbours,
 * and ticks a feature scheduled during generation fire. In the outer ring of loaded chunks (block ticks run, entities
 * do not) the falling entity never lands and is saved as is: 193 of 339 entities around spawn in the overall run.
 *
 * <p>{@code FallingBlock.updateShape} is where a neighbour change schedules the fall. While post-processing or a
 * settling fluid tick runs it returns the state without scheduling ({@code FallingBlockHoldMixin}), and generation's
 * own scheduled ticks for falling blocks are dropped ({@code ProtoChunkTicksFallingMixin}). Any later update next to
 * the block (a player digging, water outside the window, a piston) calls {@code updateShape} again and it falls as in
 * vanilla. Only blocks that extend {@link FallingBlock}; a mod's own falling block class keeps its behaviour.
 * Server thread only.
 */
public final class GeneratedFallingBlocks {

	private GeneratedFallingBlocks() {}

	/** True inside {@code LevelChunk.postProcessGeneration}. */
	private static boolean postProcessing;

	public static void beginPostProcess() {
		postProcessing = true;
	}

	public static void endPostProcess() {
		postProcessing = false;
	}

	/** {@code FallingBlock.updateShape} HEAD: true to skip scheduling the fall. */
	public static boolean hold(LevelAccessor level) {
		return (postProcessing || FirstTickWater.settlingTick()) && !level.isClientSide()
				&& WorldgenConfig.holdGeneratedFallingBlocksEnabled();
	}

	/** {@code ProtoChunkTicks.schedule} HEAD: true to drop a tick scheduled for a falling block during generation. */
	public static boolean dropGenerationTick(Object type) {
		return type instanceof FallingBlock && WorldgenConfig.holdGeneratedFallingBlocksEnabled();
	}
}
