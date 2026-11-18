package com.SaintAlfonzo.creativecompat.surface;

import com.SaintAlfonzo.creativecompat.CompatConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;

/**
 * Grown bamboo drops Twigs bamboo leaves (plan_post_scratch_1115.md G7.9; {@code mixin/BambooLeafDropMixin}).
 *
 * <p>Source: bamboo blocks with leaves (small or large). Their 4 side neighbors are "pseudo leaf" spots that drop like
 * an Immersive Weathering leaf block drops leaf piles: on each random tick of the leafy block every free side (air)
 * rolls {@code bambooLeafChance} (IW's leaf pile chance, 0.0005). A side holding any other block does not drop. A
 * dropped leaf lands on the ground below the spot (the first block that is not air or replaceable, at most 12 blocks
 * down) as one layer of Twigs bamboo leaves, or one more layer on a pile there, up to {@code bambooLeafMaxLayers}
 * (5 to 8 are the full bamboo leaves). Vanilla only random-ticks bamboo that can still grow; the mixin adds the
 * leafy states (2 or 3 blocks at the top of each stalk), and only while the chance is above 0.
 */
public final class BambooLeafDrops {

	private BambooLeafDrops() {}

	/** How far below the side spot a leaf may land. */
	static final int REACH = 12;

	/** A leafy bamboo state that should random-tick for the drops. Read at chunk load (section tick counts). */
	public static boolean ticks(BlockState state) {
		if (!state.hasProperty(BambooBlock.LEAVES) || state.getValue(BambooBlock.LEAVES) == BambooLeaves.NONE) return false;
		return CompatConfig.SPEC.isLoaded() && CompatConfig.SURFACE.passthrough.bambooLeafChance.get() > 0.0;
	}

	public static void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!ticks(state)) return;
		Block twigs = FallingLayers.twigsLeaves();
		if (twigs == null) return;
		CompatConfig.Surface.Passthrough c = CompatConfig.SURFACE.passthrough;
		double chance = c.bambooLeafChance.get();
		int cap = c.bambooLeafMaxLayers.get();
		for (Direction d : Direction.Plane.HORIZONTAL) {
			if (random.nextDouble() >= chance) continue;
			BlockPos side = pos.relative(d);
			if (!level.isLoaded(side) || !level.getBlockState(side).isAir()) continue;
			land(level, side, twigs, cap);
		}
	}

	/** Drops one leaf from {@code from} straight down onto the ground below. */
	static void land(ServerLevel level, BlockPos from, Block twigs, int cap) {
		BlockPos.MutableBlockPos p = from.mutable();
		int minY = level.getMinBuildHeight();
		for (int i = 0; i < REACH; i++) {
			BlockPos below = p.below();
			if (below.getY() < minY) return;
			BlockState under = level.getBlockState(below);
			if (FallingLayers.isBambooLeaves(under)) {
				p.move(Direction.DOWN);
				break;
			}
			if (!FallingBlock.isFree(under) || !under.getFluidState().isEmpty()) break;
			p.move(Direction.DOWN);
		}
		BlockPos at = p.immutable();
		BlockState there = level.getBlockState(at);
		if (FallingLayers.isBambooLeaves(there)) {
			int n = FallingLayers.layers(there);
			if (n >= cap) return;
			BlockState to = FallingLayers.withLayers(there, n + 1);
			if (to != null) level.setBlock(at, to, Block.UPDATE_ALL);
			return;
		}
		if (!(there.isAir() || there.getMaterial().isReplaceable()) || !level.getFluidState(at).isEmpty()) return;
		BlockState one = FallingLayers.withLayers(twigs.defaultBlockState(), 1);
		if (one != null && one.canSurvive(level, at)) level.setBlock(at, one, Block.UPDATE_ALL);
	}
}
