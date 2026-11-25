// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.farm.rabbittrap;

import com.SaintAlfonzo.creativedelight.CDConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The trap's state (farming_for_blockheads.md 2.3, 2.3.2, 2.3.3). While a player is within 64 blocks it lures, every 10
 * ticks, the nearest catchable animal within 8 blocks (2 up or down) and catches the first one that touches it. Left
 * alone it rolls every 1200 ticks, and once when its chunk loads again (queued, {@link PassiveCatches}), with the chance
 * {@code min(0.95, ticks alone / rabbitTrapFullChanceTicks)}: the loot table {@code gameplay/rabbit_trap} first (the
 * fox note), else a fresh animal from the biome table (10 % baby). One catch until a player releases it.
 */
public class RabbitTrapBlockEntity extends BlockEntity {
	public static final int MAX_USES = 8;
	static final int LURE_INTERVAL = 10;
	static final int ROLL_INTERVAL = 1200;
	static final double PLAYER_RANGE = 64.0D;
	static final double LURE_RANGE = 8.0D;
	static final double LURE_HEIGHT = 2.0D;
	static final double MAX_CHANCE = 0.95D;
	static final float BABY_CHANCE = 0.1F;

	@Nullable
	private CompoundTag caughtEntity;
	private final List<ItemStack> caughtItems = new ArrayList<>();
	private boolean passiveCatch;
	private int damage;
	private long setupTime = Long.MIN_VALUE;
	private long caughtTime = Long.MIN_VALUE;
	private long aloneSince = -1L;
	private long nextRoll = -1L;
	private boolean firstTick = true;
	// client copy of whether an animal (not only items) is inside, for the wiggle
	private boolean clientHasAnimal;

	public RabbitTrapBlockEntity(BlockPos pos, BlockState state) {
		super(RabbitTrap.RABBIT_TRAP_BLOCK_ENTITY.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, RabbitTrapBlockEntity trap) {
		trap.tick((ServerLevel) level);
	}

	private void tick(ServerLevel level) {
		long now = level.getGameTime();
		if (aloneSince < 0) aloneSince = now;
		if (firstTick) {
			firstTick = false;
			nextRoll = now + ROLL_INTERVAL + level.random.nextInt(ROLL_INTERVAL);
			// the chunk loaded again: one roll for the time away, in random order with its neighbors (D12)
			if (!hasCatch() && now - aloneSince >= ROLL_INTERVAL) PassiveCatches.enqueue(level, worldPosition);
		}
		if ((now + worldPosition.hashCode()) % LURE_INTERVAL != 0) return;
		Vec3 center = Vec3.atBottomCenterOf(worldPosition);
		if (level.hasNearbyAlivePlayer(center.x, center.y, center.z, PLAYER_RANGE)) {
			aloneSince = now;
			if (!hasCatch()) lure(level);
		} else if (now >= nextRoll) {
			nextRoll = now + ROLL_INTERVAL;
			rollPassive(level, now - aloneSince);
		}
	}

	// ---------------------------------------------------------------- lured catches

	private void lure(ServerLevel level) {
		AABB trapBox = new AABB(worldPosition);
		List<Animal> animals = level.getEntitiesOfClass(Animal.class,
				trapBox.inflate(LURE_RANGE, LURE_HEIGHT, LURE_RANGE), RabbitTrapBlockEntity::canCatch);
		if (animals.isEmpty()) return;
		AABB touch = trapBox.inflate(0.25D);
		for (Animal animal : animals) {
			if (animal.getBoundingBox().intersects(touch)) {
				capture(level, animal);
				return;
			}
		}
		Vec3 center = Vec3.atBottomCenterOf(worldPosition);
		animals.stream().min(Comparator.comparingDouble(a -> a.distanceToSqr(center)))
				.ifPresent(a -> a.getNavigation().moveTo(center.x, center.y, center.z, 1.0D));
	}

	/** Catchable: in the tag, not owned (ruling D10), not riding or ridden. */
	public static boolean canCatch(Animal animal) {
		if (!animal.isAlive() || !animal.getType().is(RabbitTrap.CAPTURABLE)) return false;
		if (animal.isPassenger() || animal.isVehicle()) return false;
		if (animal instanceof TamableAnimal tamable && tamable.isTame()) return false;
		return !(animal instanceof OwnableEntity owned) || owned.getOwnerUUID() == null;
	}

	private void capture(ServerLevel level, Animal animal) {
		if (animal.isLeashed()) animal.dropLeash(true, true);
		CompoundTag tag = new CompoundTag();
		if (!animal.saveAsPassenger(tag)) return;
		Vec3 at = animal.position();
		animal.discard();
		caughtEntity = tag;
		passiveCatch = false;
		level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.3D, at.z, 8, 0.2D, 0.2D, 0.2D, 0.02D);
		trigger(level);
	}

	// ---------------------------------------------------------------- unattended catches

	/** The queued roll for the time the chunk was away (PassiveCatches). */
	void rollTimeAway(ServerLevel level) {
		rollPassive(level, level.getGameTime() - Math.max(0L, aloneSince));
	}

	private void rollPassive(ServerLevel level, long ticksAlone) {
		if (hasCatch() || ticksAlone <= 0) return;
		double chance = Math.min(MAX_CHANCE, ticksAlone / (double) CDConfig.RABBIT_TRAP.fullChanceTicks());
		if (level.random.nextDouble() >= chance) return;
		if (PassiveCatches.blocked(level, worldPosition)) return;
		LootTable table = level.getServer().getLootTables().get(RabbitTrap.LOOT_TABLE);
		List<ItemStack> items = table.getRandomItems(new LootContext.Builder(level)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
				.withRandom(level.random).create(LootContextParamSets.CHEST));
		items.removeIf(ItemStack::isEmpty);
		if (!items.isEmpty()) {
			caughtItems.addAll(items);
		} else {
			CompoundTag animal = spawnCatch(level);
			if (animal == null) return;
			caughtEntity = animal;
		}
		passiveCatch = true;
		trigger(level);
	}

	/** A fresh animal of the biome's table, saved and never added to the world (it walks out on release). */
	@Nullable
	private CompoundTag spawnCatch(ServerLevel level) {
		EntityType<?> type = RabbitTrapCatches.INSTANCE.roll(level, worldPosition, level.random);
		if (type == null) return null;
		Entity entity = type.create(level);
		if (!(entity instanceof Animal animal)) {
			if (entity != null) entity.discard();
			return null;
		}
		Vec3 c = Vec3.atBottomCenterOf(worldPosition);
		animal.moveTo(c.x, c.y, c.z, level.random.nextFloat() * 360.0F, 0.0F);
		animal.finalizeSpawn(level, level.getCurrentDifficultyAt(worldPosition), MobSpawnType.NATURAL, null, null);
		if (level.random.nextFloat() < BABY_CHANCE) animal.setBaby(true);
		CompoundTag tag = new CompoundTag();
		boolean saved = animal.saveAsPassenger(tag);
		animal.discard();
		return saved ? tag : null;
	}

	private void trigger(ServerLevel level) {
		caughtTime = level.getGameTime();
		BlockState state = getBlockState();
		if (state.hasProperty(RabbitTrapBlock.TRIGGERED) && !state.getValue(RabbitTrapBlock.TRIGGERED))
			level.setBlock(worldPosition, state.setValue(RabbitTrapBlock.TRIGGERED, true), 3);
		level.playSound(null, worldPosition, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 0.8F);
		Vec3 c = Vec3.atCenterOf(worldPosition);
		level.sendParticles(ParticleTypes.POOF, c.x, c.y, c.z, 6, 0.3D, 0.2D, 0.3D, 0.01D);
		sync();
	}

	// ---------------------------------------------------------------- release

	/** Lets the catch out in front of the trap (right-click or break). */
	public void releaseCatch(ServerLevel level) {
		if (!hasCatch()) return;
		Direction facing = getBlockState().hasProperty(RabbitTrapBlock.FACING)
				? getBlockState().getValue(RabbitTrapBlock.FACING) : Direction.NORTH;
		BlockPos front = worldPosition.relative(facing);
		if (!level.getBlockState(front).getCollisionShape(level, front).isEmpty()) front = worldPosition;
		Vec3 at = Vec3.atBottomCenterOf(front);
		if (caughtEntity != null) {
			float yaw = facing.toYRot();
			Entity entity = EntityType.loadEntityRecursive(caughtEntity, level, e -> {
				e.moveTo(at.x, at.y, at.z, yaw, 0.0F);
				return e;
			});
			if (entity != null && !level.tryAddFreshEntityWithPassengers(entity)) {
				// the old id is still known to the level (a copy left behind): release it as a new animal
				entity.setUUID(Mth.createInsecureUUID(level.random));
				level.tryAddFreshEntityWithPassengers(entity);
			}
			if (entity instanceof Mob mob) mob.setPersistenceRequired();
		}
		for (ItemStack stack : caughtItems) {
			ItemEntity item = new ItemEntity(level, at.x, at.y + 0.3D, at.z, stack.copy());
			item.setDefaultPickUpDelay();
			level.addFreshEntity(item);
		}
		level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.3D, at.z, 6, 0.2D, 0.2D, 0.2D, 0.02D);
		clearCatch();
	}

	private void clearCatch() {
		caughtEntity = null;
		caughtItems.clear();
		passiveCatch = false;
		setChanged();
	}

	// ---------------------------------------------------------------- state

	public boolean hasCatch() {
		return caughtEntity != null || !caughtItems.isEmpty();
	}

	/** Holds a catch made while left alone (counts for rabbitTrapPassiveSpacing, D12). */
	public boolean holdsPassiveCatch() {
		return passiveCatch && hasCatch();
	}

	/** Client: an animal (not only the fox's note) is inside. */
	public boolean hasAnimal() {
		return level != null && level.isClientSide ? clientHasAnimal : caughtEntity != null;
	}

	/** The caught animal's type id, or null (WTHIT, tests). */
	@Nullable
	public String caughtTypeId() {
		return caughtEntity != null && caughtEntity.contains("id", Tag.TAG_STRING) ? caughtEntity.getString("id") : null;
	}

	public int damage() {
		return damage;
	}

	public long setupTime() {
		return setupTime;
	}

	public long caughtTime() {
		return caughtTime;
	}

	/** Placement: the item's used uses and the set-up animation. */
	void placed(Level level, int damageFromItem) {
		damage = Mth.clamp(damageFromItem, 0, MAX_USES - 1);
		setupTime = level.getGameTime();
		aloneSince = level.getGameTime();
		sync();
	}

	private void sync() {
		setChanged();
		if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		caughtEntity = tag.contains("CaughtEntity", Tag.TAG_COMPOUND) ? tag.getCompound("CaughtEntity") : null;
		caughtItems.clear();
		ListTag items = tag.getList("CaughtItems", Tag.TAG_COMPOUND);
		for (int i = 0; i < items.size(); i++) {
			ItemStack stack = ItemStack.of(items.getCompound(i));
			if (!stack.isEmpty()) caughtItems.add(stack);
		}
		passiveCatch = tag.getBoolean("PassiveCatch");
		damage = tag.getInt(RabbitTrapItem.DAMAGE_TAG);
		aloneSince = tag.contains("AloneSince") ? tag.getLong("AloneSince") : -1L;
		setupTime = tag.contains("SetupTime") ? tag.getLong("SetupTime") : Long.MIN_VALUE;
		caughtTime = tag.contains("CaughtTime") ? tag.getLong("CaughtTime") : Long.MIN_VALUE;
		clientHasAnimal = tag.getBoolean("HasAnimal");
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		if (caughtEntity != null) tag.put("CaughtEntity", caughtEntity.copy());
		if (!caughtItems.isEmpty()) {
			ListTag items = new ListTag();
			for (ItemStack stack : caughtItems) items.add(stack.save(new CompoundTag()));
			tag.put("CaughtItems", items);
		}
		if (passiveCatch) tag.putBoolean("PassiveCatch", true);
		// only when used, so a dropped unused trap (copy_nbt) still stacks with new ones
		if (damage > 0) tag.putInt(RabbitTrapItem.DAMAGE_TAG, damage);
		tag.putLong("AloneSince", aloneSince);
		if (setupTime != Long.MIN_VALUE) tag.putLong("SetupTime", setupTime);
		if (caughtTime != Long.MIN_VALUE) tag.putLong("CaughtTime", caughtTime);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		if (setupTime != Long.MIN_VALUE) tag.putLong("SetupTime", setupTime);
		if (caughtTime != Long.MIN_VALUE) tag.putLong("CaughtTime", caughtTime);
		tag.putBoolean("HasAnimal", caughtEntity != null);
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
