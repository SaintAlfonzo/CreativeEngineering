// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mobs;

import com.github.alexthe666.alexsmobs.entity.EntityCachalotWhale;
import com.github.alexthe666.alexsmobs.entity.EntityGiantSquid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

/**
 * Squid and Alex's Mobs cachalot whales (wave 1 ruling W17, creative-compat-publish #9; feature_writeup/
 * mob_block_tweaks.md 2.10). Vanilla squid and glow squid swim away from a cachalot whale within {@link #FLEE_START}
 * blocks until it is {@link #FLEE_STOP} off; orcas are not fled. The giant squid no longer flees whales (its
 * AIAvoidWhales goal never starts, {@code mixin/AmGiantSquidAvoidWhalesMixin}) and fights back instead: it targets a
 * whale that hurt it (Alex's Mobs' own retaliation goal ignores whales) or one within {@link #FIGHT_RANGE} blocks, and
 * its melee goal swims in and grabs. Goals are added when the mob joins a server level. Registered only with
 * Alex's Mobs loaded.
 */
public final class SquidsAndWhales {
	static final double FLEE_START = 16.0;
	static final double FLEE_STOP = 24.0;
	static final double FIGHT_RANGE = 12.0;

	private SquidsAndWhales() {
	}

	public static void onJoin(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide) return;
		if (event.getEntity() instanceof Squid squid) {
			squid.goalSelector.addGoal(1, new FleeWhaleGoal(squid));
		} else if (event.getEntity() instanceof EntityGiantSquid giant) {
			giant.targetSelector.addGoal(1, new HurtByTargetGoal(giant) {
				@Override
				public boolean canUse() {
					return giant.getLastHurtByMob() instanceof EntityCachalotWhale && super.canUse();
				}
			});
			giant.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(giant, EntityCachalotWhale.class, 10, false,
					false, whale -> whale.distanceTo(giant) <= FIGHT_RANGE));
		}
	}

	/** Vanilla's squid flee (Squid.SquidFleeGoal) aimed at the nearest cachalot whale instead of the last attacker. */
	static final class FleeWhaleGoal extends Goal {
		private final Squid squid;
		private EntityCachalotWhale whale;
		private int runDelay;
		private int fleeTicks;

		FleeWhaleGoal(Squid squid) {
			this.squid = squid;
		}

		@Override
		public boolean canUse() {
			if (!squid.isInWater() || runDelay-- > 0) return false;
			runDelay = 10 + squid.getRandom().nextInt(10);
			whale = nearest();
			return whale != null;
		}

		@Override
		public boolean canContinueToUse() {
			return whale != null && whale.isAlive() && squid.isInWater() && squid.distanceTo(whale) < FLEE_STOP;
		}

		@Override
		public void start() {
			fleeTicks = 0;
		}

		@Override
		public void stop() {
			whale = null;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			fleeTicks++;
			Vec3 away = squid.position().subtract(whale.position());
			if (away.lengthSqr() < 1.0E-4) away = new Vec3(squid.getRandom().nextDouble() - 0.5, 0.0, squid.getRandom().nextDouble() - 0.5);
			Vec3 dir = away.normalize();
			BlockPos ahead = new BlockPos(squid.getX() + dir.x * 2.0, squid.getY() + dir.y * 2.0, squid.getZ() + dir.z * 2.0);
			boolean water = squid.level.getFluidState(ahead).is(FluidTags.WATER);
			if (!water && !squid.level.getBlockState(ahead).isAir()) {
				// blocked ahead: slide along the wall level, as vanilla's flee does at the surface
				dir = new Vec3(dir.x, 0.0, dir.z);
				if (dir.lengthSqr() < 1.0E-4) return;
				dir = dir.normalize();
			} else if (!water) {
				dir = new Vec3(dir.x, 0.0, dir.z);
			}
			Vec3 move = dir.scale(3.0 / 20.0);
			squid.setMovementVector((float) move.x, (float) move.y, (float) move.z);
			if (fleeTicks % 10 == 5 && squid.level instanceof ServerLevel server) {
				server.sendParticles(ParticleTypes.BUBBLE, squid.getX(), squid.getY(), squid.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
			}
		}

		private EntityCachalotWhale nearest() {
			EntityCachalotWhale best = null;
			double bestDist = FLEE_START;
			for (EntityCachalotWhale w : squid.level.getEntitiesOfClass(EntityCachalotWhale.class,
					squid.getBoundingBox().inflate(FLEE_START), LivingEntity::isAlive)) {
				double d = squid.distanceTo(w);
				if (d < bestDist) {
					best = w;
					bestDist = d;
				}
			}
			return best;
		}
	}
}
