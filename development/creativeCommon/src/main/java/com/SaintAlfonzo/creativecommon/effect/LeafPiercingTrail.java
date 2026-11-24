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
 * is inside one. Once it has passed through leaves it keeps trailing the last leaf's particles until it stops moving
 * (stuck in a block, or spent on a hit) or {@link #MAX_TRAIL_TICKS} after it first entered leaves.
 * Particles go out with {@code ServerLevel.sendParticles}, so every nearby client sees them.
 */
public final class LeafPiercingTrail {

	private static final String TAG = "fruitsdelight:leaf_piercing";
	/** Sample spacing along the flight segment, in blocks. */
	private static final double STEP = 0.25D;
	private static final int POOF_COUNT = 12;
	private static final int TRAIL_COUNT = 2;
	/** How long the trail lasts after the arrow first enters leaves: 5 seconds. */
	private static final int MAX_TRAIL_TICKS = 100;

	private LeafPiercingTrail() {}

	/** Per-arrow trail state, held by the mixin. */
	public static final class State {
		/** The leaf block the arrow was in at the end of its last tick, null for none. */
		@Nullable BlockPos lastLeaf;
		/** The last leaf block state passed through; null until the arrow first enters leaves. */
		@Nullable BlockState trail;
		int age;
		boolean done;
	}

	/** Called at the end of the arrow's tick. */
	public static void tick(AbstractArrow arrow, State s) {
		if (s.done || !(arrow.level instanceof ServerLevel level) || !arrow.getTags().contains(TAG)) return;
		Vec3 from = new Vec3(arrow.xo, arrow.yo, arrow.zo);
		Vec3 to = arrow.position();
		double length = from.distanceTo(to);
		if (length < 1.0E-3D) {
			// stopped: it reached its destination; a trail never restarts (a returning trident stays plain)
			if (s.trail != null) s.done = true;
			return;
		}
		if (s.trail != null && ++s.age > MAX_TRAIL_TICKS) {
			s.done = true;
			return;
		}
		int steps = Math.max(1, (int) Math.ceil(length / STEP));
		for (int i = 1; i <= steps; i++) {
			Vec3 at = from.lerp(to, (double) i / steps);
			BlockPos pos = new BlockPos(at);
			BlockState state = level.getBlockState(pos);
			if (!state.is(BlockTags.LEAVES)) {
				s.lastLeaf = null;
				if (s.trail != null && i % 2 == 0) {
					level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, s.trail), at.x, at.y, at.z,
							TRAIL_COUNT, 0.03D, 0.03D, 0.03D, 0.0D);
				}
				continue;
			}
			s.trail = state;
			BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, state);
			if (!pos.equals(s.lastLeaf)) {
				level.sendParticles(particle, at.x, at.y, at.z, POOF_COUNT, 0.2D, 0.2D, 0.2D, 0.05D);
				s.lastLeaf = pos;
			} else if (i % 2 == 0) {
				level.sendParticles(particle, at.x, at.y, at.z, TRAIL_COUNT, 0.03D, 0.03D, 0.03D, 0.0D);
			}
		}
	}
}
