package com.SaintAlfonzo.creativedelight.farm.shippingbin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * The balloon that carries a load sold under open sky away (an effect, no items): it inflates in the bin for
 * {@value #INFLATE_TICKS} ticks, rises {@value #RISE} blocks a tick with a slow drift (its direction changes every 6000
 * game ticks, the same for every balloon at a time) for {@value #HEIGHT} blocks, then puffs smoke and is gone. A hit pops
 * it: a falling crate drops and breaks on landing, with no loot (upstream bug B2 kept: the emeralds are already paid).
 */
public class ShippingBalloonEntity extends Entity {
	public static final int INFLATE_TICKS = 20;
	private static final double RISE = 0.12D;
	private static final double DRIFT = 0.02D;
	private static final double HEIGHT = 172.0D;

	private double startY = Double.NaN;

	public ShippingBalloonEntity(EntityType<? extends ShippingBalloonEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
	}

	@Override
	public void tick() {
		super.tick();
		if (Double.isNaN(startY)) startY = getY();
		if (tickCount > INFLATE_TICKS) {
			float angle = (level.getGameTime() / 6000L % 360L) * 73.0F * Mth.DEG_TO_RAD;
			Vec3 motion = new Vec3(Mth.cos(angle) * DRIFT, RISE, Mth.sin(angle) * DRIFT);
			setDeltaMovement(motion);
			move(MoverType.SELF, motion);
		}
		if (!level.isClientSide && getY() - startY > HEIGHT) {
			((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 2.0D, getZ(), 20, 0.6D, 0.6D, 0.6D, 0.02D);
			discard();
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (isInvulnerableTo(source) || isRemoved()) return false;
		if (!level.isClientSide) {
			level.playSound(null, getX(), getY() + 2.0D, getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 1.0F, 1.2F);
			((ServerLevel) level).sendParticles(ParticleTypes.POOF, getX(), getY() + 2.5D, getZ(), 20, 0.7D, 0.6D, 0.7D, 0.05D);
			FallingShippingCrateEntity crate = ShippingBin.FALLING_SHIPPING_CRATE.get().create(level);
			if (crate != null) {
				crate.moveTo(getX(), getY(), getZ(), getYRot(), 0.0F);
				level.addFreshEntity(crate);
			}
			discard();
		}
		return true;
	}

	@Override
	public boolean isPickable() {
		return !isRemoved();
	}

	@Override
	public boolean isAttackable() {
		return true;
	}

	@Override
	public boolean isNoGravity() {
		return true;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		return distance < 256.0D * 256.0D;
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		startY = tag.contains("StartY") ? tag.getDouble("StartY") : Double.NaN;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (!Double.isNaN(startY)) tag.putDouble("StartY", startY);
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
