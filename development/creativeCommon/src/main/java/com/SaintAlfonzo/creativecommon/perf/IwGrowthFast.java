package com.SaintAlfonzo.creativecommon.perf;

import com.google.common.base.Suppliers;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.data.block_growths.BlockGrowthHandler;
import com.ordana.immersive_weathering.data.block_growths.IConditionalGrowingBlock;
import com.ordana.immersive_weathering.data.block_growths.TickSource;
import com.ordana.immersive_weathering.data.block_growths.growths.IBlockGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Fast path for Immersive Weathering's block growths (perf.md 15, candidates A to C). IW's random-tick hook ran
 * {@code BlockGrowthHandler.tickBlock} for every random-tick position, and every call read its config, allocated a
 * memoized biome supplier and did three map lookups before finding that the block has no growth (nearly always).
 * <ul>
 *   <li>A: the {@code block_growths} switch is read once per game tick, and the supplier is only built when a
 *       growth will run.</li>
 *   <li>B: each block carries its growth sets per tick source ({@link IwGrowthHolder}, filled from IW's own maps
 *       after every rebuild), so a block without growths returns after one field read.</li>
 *   <li>C: IW's {@code ServerLevelMixin.callTick} handler is rewritten (see {@link CommonMixinPlugin}) to call
 *       {@link #blockTick}, which re-reads the block state from the section only when the block random-ticked
 *       (nothing else can change it between vanilla's read and the hook).</li>
 * </ul>
 * Same growths, same order, same results as IW. {@code -Dcreativecommon.iwFastTick=false} turns all of it off
 * (IW's own code runs) for A/B measurements.
 */
public final class IwGrowthFast {

	public static final boolean ENABLED = !"false".equals(System.getProperty("creativecommon.iwFastTick"));

	private static final TickSource[] SOURCES = TickSource.values();
	/** Universal growths (no owner list) per tick source ordinal, null when none. IW's own sets. */
	private static volatile Set<IBlockGrowth>[] universal = newTable();
	/** Blocks whose holder field was set by the last rebuild, to clear on the next one. */
	private static List<Block> owners = new ArrayList<>();

	private static long cfgTime = Long.MIN_VALUE;
	private static boolean cfgOn;

	private IwGrowthFast() {}

	@SuppressWarnings("unchecked")
	private static Set<IBlockGrowth>[] newTable() {
		return (Set<IBlockGrowth>[]) new Set[SOURCES.length];
	}

	/** After IW's {@code rebuild}: copies its maps into the per-block fields and the universal table. */
	public static synchronized void rebuilt(Map<TickSource, Map<Block, Set<IBlockGrowth>>> forBlock,
			Map<TickSource, Set<IBlockGrowth>> universalGrowths) {
		for (Block b : owners) ((IwGrowthHolder) b).ce$setIwGrowths(null);
		List<Block> now = new ArrayList<>();
		for (Map.Entry<TickSource, Map<Block, Set<IBlockGrowth>>> e : forBlock.entrySet()) {
			int s = e.getKey().ordinal();
			for (Map.Entry<Block, Set<IBlockGrowth>> be : e.getValue().entrySet()) {
				IwGrowthHolder h = (IwGrowthHolder) be.getKey();
				Set<IBlockGrowth>[] t = h.ce$iwGrowths();
				if (t == null) {
					t = newTable();
					h.ce$setIwGrowths(t);
					now.add(be.getKey());
				}
				t[s] = be.getValue();
			}
		}
		owners = now;
		Set<IBlockGrowth>[] u = newTable();
		for (Map.Entry<TickSource, Set<IBlockGrowth>> e : universalGrowths.entrySet()) u[e.getKey().ordinal()] = e.getValue();
		universal = u;
	}

	/** IW's {@code block_growths} switch, read again once per game tick (IW read it on every call). */
	private static boolean growthsOn(ServerLevel level) {
		long t = level.getGameTime();
		if (t != cfgTime) {
			cfgOn = CommonConfigs.BLOCK_GROWTHS.get();
			cfgTime = t;
		}
		return cfgOn;
	}

	/** Replaces {@code BlockGrowthHandler.tickBlock} (via {@code IwBlockGrowthHandlerMixin}). */
	public static void tickBlock(TickSource source, BlockState state, ServerLevel level, BlockPos pos) {
		Block block = state.getBlock();
		Set<IBlockGrowth>[] own = ((IwGrowthHolder) block).ce$iwGrowths();
		int s = source.ordinal();
		Set<IBlockGrowth> mine = own == null ? null : own[s];
		Set<IBlockGrowth> all = universal[s];
		if (mine == null && all == null) return;
		if (!growthsOn(level)) return;
		if (block instanceof IConditionalGrowingBlock cb && !cb.canGrow(state)) return;

		Supplier<Holder<Biome>> biome = Suppliers.memoize(() -> level.getBiome(pos));
		if (all != null) {
			for (IBlockGrowth config : all) config.tryGrowing(pos, state, level, biome);
		}
		if (mine != null) {
			for (IBlockGrowth config : mine) config.tryGrowing(pos, state, level, biome);
		}
	}

	/**
	 * Body of IW's rewritten {@code callTick} handler, called from vanilla's random-tick loop just before
	 * {@code getFluidState}. {@code before} is the state vanilla read; only its random tick can have changed the
	 * block since, so IW's re-read from the section is skipped for blocks that do not random-tick.
	 */
	public static void blockTick(ServerLevel level, LevelChunkSection section, BlockPos pos, BlockState before,
			int minX, int minY, int minZ) {
		BlockState state = before.isRandomlyTicking()
				? section.getBlockState(pos.getX() - minX, pos.getY() - minY, pos.getZ() - minZ)
				: before;
		BlockGrowthHandler.tickBlock(TickSource.BLOCK_TICK, state, level, pos);
	}
}
