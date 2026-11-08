package com.SaintAlfonzo.creativedelight.compat.supplementaries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Supplementaries faucet fits the containers around it (TODO cooking/kitchen #26, kitchen.md 2.14). Client only in
 * use (FaucetFitModel, mixin FaucetStreamFitMixin); plain math on block shapes, no Supplementaries types.
 * <ul>
 * <li>Horizontal: the faucet (model and stream) moves toward the block it drains, by how far that block's shape
 * stands back from the shared face (a Supplementaries jar 3 px, a kettle, a pot), at most {@link #MAX_SHIFT}. The move
 * is then held so the 4 px spout stays over the shape of the block below, when that block is not empty.</li>
 * <li>Vertical: the stream ends on the top of the block below (its shape's top) instead of a fixed 5 px above that
 * block's floor, so it meets a low container (a carpet, a pot rim) or stops at a full block's top. Nothing below (an
 * empty shape): upstream length.</li>
 * </ul>
 */
public final class FaucetFit {

	/** The furthest the faucet moves toward its source, in blocks (6 px). */
	public static final double MAX_SHIFT = 6.0 / 16.0;
	/** Half the width of the spout and the stream, in blocks. */
	private static final double HALF_SPOUT = 2.0 / 16.0;
	/** Upstream's stream: 16 segments of 1/16 block, the first one's top 5/16 above the faucet block's floor. */
	public static final int UPSTREAM_SEGMENTS = 16;
	private static final double FIRST_TOP = 5.0 / 16.0;

	private FaucetFit() {}

	private static Direction facing(BlockState faucet) {
		return faucet.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
				? faucet.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
	}

	/** The faucet's shift {x, z} in blocks, toward its source. */
	public static double[] shift(BlockGetter level, BlockPos pos, BlockState faucet) {
		Direction facing = facing(faucet);
		Direction toSource = facing.getOpposite();
		BlockPos sourcePos = pos.relative(toSource);
		VoxelShape source = level.getBlockState(sourcePos).getShape(level, sourcePos);
		if (source.isEmpty()) return new double[] {0, 0};
		Direction.Axis axis = facing.getAxis();
		// how far the source's face toward the faucet stands back from the block edge
		double inset = facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE
				? source.min(axis) : 1.0 - source.max(axis);
		double amount = Math.max(0.0, Math.min(MAX_SHIFT, inset));
		if (amount <= 0.0) return new double[] {0, 0};
		double s = amount * toSource.getAxisDirection().getStep();
		// keep the spout over the block below, if there is one
		BlockPos belowPos = pos.below();
		VoxelShape below = level.getBlockState(belowPos).getShape(level, belowPos);
		if (!below.isEmpty()) {
			double lo = below.min(axis) - (0.5 - HALF_SPOUT);
			double hi = below.max(axis) - (0.5 + HALF_SPOUT);
			if (lo <= hi) s = Math.max(lo, Math.min(hi, s));
			if (Math.signum(s) != toSource.getAxisDirection().getStep()) s = 0;
		}
		return axis == Direction.Axis.X ? new double[] {s, 0} : new double[] {0, s};
	}

	/** Segments of the stream (1/16 block each) so it ends on the top of the block below. */
	public static int segments(BlockGetter level, BlockPos pos) {
		BlockPos belowPos = pos.below();
		VoxelShape below = level.getBlockState(belowPos).getShape(level, belowPos);
		if (below.isEmpty()) return UPSTREAM_SEGMENTS;
		double top = below.max(Direction.Axis.Y) - 1.0; // relative to the faucet block's floor
		int n = (int) Math.round((FIRST_TOP - top) * 16.0);
		return Math.max(1, Math.min(32, n));
	}
}
