package com.SaintAlfonzo.creativedelight.farm.shippingbin;

import com.SaintAlfonzo.creativedelight.CDConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The courier who fetches a load sold under a roof (rulings D5 to D5c, D9). A villager-shaped mob with no AI goals: this
 * class walks it through its phases. Invulnerable, never despawns on its own, never interacts.
 * <ol>
 * <li>{@link #PHASE_IN}: walks to the bin from where it was placed (a Courier Spawn Point or a point
 * {@code courierWalkDistance} away, {@link CourierDispatch}). Past its walk timer it teleports to the bin.</li>
 * <li>{@link #PHASE_PICKUP}: stands at the bin for a second, then lifts the crate (the crate rides on it).</li>
 * <li>{@link #PHASE_OUT}: walks back to its spawn point (D5c) or out to the configured distance; past the timer it stops
 * where it is. {@code at_bin} mode skips this.</li>
 * <li>{@link #PHASE_DRINK}: drinks an invisibility potion (the wandering trader's drink sound), turns invisible and is gone
 * with the crate.</li>
 * </ol>
 * The timer is game time, also kept on the bin (ShippingBinBlockEntity), so a courier frozen in an unloaded chunk still
 * times out. Paths longer than its search reach are walked in legs: each finished partial path is followed by a new one.
 */
public class CourierEntity extends PathfinderMob {
	public static final int PHASE_IN = 0, PHASE_PICKUP = 1, PHASE_OUT = 2, PHASE_DRINK = 3;
	private static final double SPEED = 0.7D;
	private static final int PICKUP_TICKS = 20, DRINK_TICKS = 32, VANISH_TICKS = 12;

	private BlockPos bin = BlockPos.ZERO;
	@Nullable
	private BlockPos origin; // the Courier Spawn Point it came from
	@Nullable
	private BlockPos exit;
	private int phase = PHASE_IN;
	private long deadline;
	private int phaseTicks;
	private boolean atBinMode;

	public CourierEntity(EntityType<? extends CourierEntity> type, Level level) {
		super(type, level);
		setInvulnerable(true);
		setPersistenceRequired();
		// The search reach of a path is the follow range (vanilla); spawn points count up to the search radius (D5a, D9).
		AttributeInstance range = getAttribute(Attributes.FOLLOW_RANGE);
		if (range != null) range.setBaseValue(CDConfig.SHIPPING_BIN.courierSpawnSearchRadius() + 16.0D);
		getNavigation().setMaxVisitedNodesMultiplier(4.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.MOVEMENT_SPEED, 0.5D)
				.add(Attributes.FOLLOW_RANGE, 112.0D);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		// Plans a path the moment it is placed, before it ever stood on the ground (vanilla waits for onGround).
		GroundPathNavigation navigation = new GroundPathNavigation(this, level) {
			@Override
			protected boolean canUpdatePath() {
				return true;
			}
		};
		navigation.setCanOpenDoors(true);
		navigation.setCanPassDoors(true);
		return navigation;
	}

	void setup(BlockPos bin, @Nullable BlockPos origin, int phase, long deadline, boolean atBinMode) {
		this.bin = bin.immutable();
		this.origin = origin == null ? null : origin.immutable();
		this.phase = phase;
		this.deadline = deadline;
		this.atBinMode = atBinMode;
		this.phaseTicks = 0;
	}

	public BlockPos bin() {
		return bin;
	}

	public int phase() {
		return phase;
	}

	/** A path from where it stands to the bin, null when the bin cannot be reached. */
	@Nullable
	Path pathToBin() {
		// accuracy 2: the ground next to the bin (the navigation aims at the air above a solid target block)
		Path path = getNavigation().createPath(bin, 2);
		return path != null && path.canReach() ? path : null;
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide) return;
		ServerLevel server = (ServerLevel) level;
		long now = server.getGameTime();
		phaseTicks++;
		// The bin's chunk is loaded: leave when the bin is gone or sent someone else in our place.
		if (phase < PHASE_DRINK && server.isLoaded(bin)) {
			if (!(server.getBlockEntity(bin) instanceof ShippingBinBlockEntity binEntity)) {
				startDrinking();
			} else if (!getUUID().equals(binEntity.courierId())) {
				discardWithCrate();
				return;
			}
		}
		switch (phase) {
			case PHASE_IN -> {
				if (nearBin()) {
					enter(PHASE_PICKUP, deadline);
				} else if (now >= deadline) {
					teleportToBin(server);
					enter(PHASE_PICKUP, deadline);
				} else {
					walkTo(bin, 2);
				}
			}
			case PHASE_PICKUP -> {
				getNavigation().stop();
				getLookControl().setLookAt(Vec3.atCenterOf(bin));
				if (phaseTicks >= PICKUP_TICKS) {
					takeCrate(server);
					if (atBinMode) {
						startDrinking();
					} else {
						exit = CourierDispatch.exitFor(server, this, origin);
						if (exit == null) startDrinking();
						else enter(PHASE_OUT, now + CDConfig.SHIPPING_BIN.courierWalkTimeoutTicks());
					}
				}
			}
			case PHASE_OUT -> {
				if (exit == null || now >= deadline || distanceToSqr(Vec3.atBottomCenterOf(exit)) < 2.25D) {
					startDrinking();
				} else {
					walkTo(exit, 1);
				}
			}
			default -> drinkTick(server);
		}
	}

	private boolean nearBin() {
		Vec3 center = Vec3.atBottomCenterOf(bin);
		double dx = getX() - center.x, dz = getZ() - center.z;
		return dx * dx + dz * dz < 4.0D && Math.abs(getY() - bin.getY()) < 2.5D;
	}

	private void walkTo(BlockPos target, int accuracy) {
		PathNavigation navigation = getNavigation();
		if (navigation.isDone() && (phaseTicks % 10 == 0)) {
			// A leg: a new (possibly partial) path from here; a courier that gets nowhere is left to the walk timer.
			navigation.moveTo(navigation.createPath(target, accuracy), SPEED);
		}
	}

	private void enter(int newPhase, long newDeadline) {
		phase = newPhase;
		deadline = newDeadline;
		phaseTicks = 0;
		getNavigation().stop();
		if (level.getBlockEntity(bin) instanceof ShippingBinBlockEntity binEntity && getUUID().equals(binEntity.courierId())) {
			binEntity.noteCourier(newPhase, newDeadline);
		}
	}

	private void teleportToBin(ServerLevel server) {
		Vec3 from = position();
		Vec3 to = CourierDispatch.besideBin(server, bin);
		teleportEffects(server, from);
		teleportTo(to.x, to.y, to.z);
		teleportEffects(server, to);
	}

	static void teleportEffects(ServerLevel server, Vec3 at) {
		server.playSound(null, at.x, at.y, at.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1.0F, 1.0F);
		server.sendParticles(ParticleTypes.PORTAL, at.x, at.y + 1.0D, at.z, 32, 0.3D, 0.8D, 0.3D, 0.2D);
	}

	private void takeCrate(ServerLevel server) {
		if (!getPassengers().isEmpty()) return;
		if (server.getBlockEntity(bin) instanceof ShippingBinBlockEntity binEntity && binEntity.crateId() != null
				&& server.getEntity(binEntity.crateId()) instanceof ShippingCrateEntity crate) {
			crate.startRiding(this, true);
			binEntity.clearCrate();
		}
	}

	private void startDrinking() {
		if (phase == PHASE_DRINK) return;
		enter(PHASE_DRINK, deadline);
		setItemSlot(EquipmentSlot.MAINHAND, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY));
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.WANDERING_TRADER_DRINK_POTION, getSoundSource(), 1.0F, 1.0F);
	}

	private void drinkTick(ServerLevel server) {
		getNavigation().stop();
		if (phaseTicks == DRINK_TICKS) {
			setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
			addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, false, true));
			for (Entity passenger : getPassengers()) passenger.setInvisible(true);
			server.playSound(null, getX(), getY(), getZ(), SoundEvents.WANDERING_TRADER_DISAPPEARED, getSoundSource(), 1.0F, 1.0F);
		} else if (phaseTicks >= DRINK_TICKS + VANISH_TICKS) {
			server.sendParticles(ParticleTypes.POOF, getX(), getY() + 1.0D, getZ(), 12, 0.3D, 0.6D, 0.3D, 0.02D);
			if (server.isLoaded(bin) && server.getBlockEntity(bin) instanceof ShippingBinBlockEntity binEntity
					&& getUUID().equals(binEntity.courierId())) {
				binEntity.courierGone();
			}
			discardWithCrate();
		}
	}

	private void discardWithCrate() {
		for (Entity passenger : getPassengers()) passenger.discard();
		discard();
	}

	@Override
	public double getPassengersRidingOffset() {
		return getBbHeight() + 0.05D; // the crate rides on its head
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return source == DamageSource.OUT_OF_WORLD && super.hurt(source, amount);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		return InteractionResult.PASS;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return false;
	}

	@Override
	protected boolean shouldDropLoot() {
		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.put("Bin", NbtUtils.writeBlockPos(bin));
		if (origin != null) tag.put("Origin", NbtUtils.writeBlockPos(origin));
		if (exit != null) tag.put("Exit", NbtUtils.writeBlockPos(exit));
		tag.putInt("Phase", phase);
		tag.putLong("Deadline", deadline);
		tag.putInt("PhaseTicks", phaseTicks);
		tag.putBoolean("AtBin", atBinMode);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		bin = NbtUtils.readBlockPos(tag.getCompound("Bin"));
		origin = tag.contains("Origin") ? NbtUtils.readBlockPos(tag.getCompound("Origin")) : null;
		exit = tag.contains("Exit") ? NbtUtils.readBlockPos(tag.getCompound("Exit")) : null;
		phase = tag.getInt("Phase");
		deadline = tag.getLong("Deadline");
		phaseTicks = tag.getInt("PhaseTicks");
		atBinMode = tag.getBoolean("AtBin");
	}
}
