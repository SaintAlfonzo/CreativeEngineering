package com.SaintAlfonzo.creativecommon.effect;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Leaf Piercing particles ({@code food-tuning #52}, raspberry_pie.md 12.13). Fruits Delight tags a projectile shot by a
 * holder of Leaf Piercing with {@code fruitsdelight:leaf_piercing} and lets tagged arrows pass through leaves
 * ({@code minecraft:leaves}). Each server tick of such an arrow ({@code mixin/LeafPiercingTrailMixin}) walks the stretch
 * it just flew: a poof of the leaf block's particles where it enters a leaf block, and a thin trail of them while it
 * is inside one. Particles go out with {@code ServerLevel.sendParticles}, so every nearby client sees them.
 */
public final class LeafPiercingTrail {

	private static final String TAG = "fruitsdelight:leaf_piercing";
	/** Sample spacing along the flight segment, in blocks. */
	private static final double STEP = 0.25D;
	private static final int POOF_COUNT = 12;
	private static final int TRAIL_COUNT = 2;

	private LeafPiercingTrail() {}

	/**
	 * Called at the end of the arrow's tick. {@code lastLeaf} is the leaf block the arrow was in at the end of its last
	 * tick (null for none); returns the one it is in now.
	 */
	@Nullable
	public static BlockPos tick(AbstractArrow arrow, @Nullable BlockPos lastLeaf) {
		if (!(arrow.level instanceof ServerLevel level) || !arrow.getTags().contains(TAG)) return null;
		Vec3 from = new Vec3(arrow.xo, arrow.yo, arrow.zo);
		Vec3 to = arrow.position();
		double length = from.distanceTo(to);
		if (length < 1.0E-3D) return lastLeaf;
		int steps = Math.max(1, (int) Math.ceil(length / STEP));
		BlockPos current = lastLeaf;
		for (int i = 1; i <= steps; i++) {
			Vec3 at = from.lerp(to, (double) i / steps);
			BlockPos pos = new BlockPos(at);
			BlockState state = level.getBlockState(pos);
			if (!state.is(BlockTags.LEAVES)) {
				current = null;
				continue;
			}
			BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, state);
			if (!pos.equals(current)) {
				level.sendParticles(particle, at.x, at.y, at.z, POOF_COUNT, 0.2D, 0.2D, 0.2D, 0.05D);
				current = pos;
			} else if (i % 2 == 0) {
				level.sendParticles(particle, at.x, at.y, at.z, TRAIL_COUNT, 0.03D, 0.03D, 0.03D, 0.0D);
			}
		}
		return current;
	}
}
