package com.SaintAlfonzo.creativecompat.mobs;

import com.SaintAlfonzo.creativecompat.CompatConfig;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;

/**
 * Quark monster boxes as vaults (CompatConfig [monster_box], feature_writeup/mob_block_tweaks.md section 2.9, TODO
 * {@code mobs/tweaks #5}). State lives on the block entity ({@code mixin/MonsterBoxBlockEntityMixin}, which saves it
 * in AutoRegLib's shared NBT, so it is both saved and sent to clients).
 * <ul>
 *   <li>{@link #ARMED}: Quark's own tick (flames, growl, 2 s countdown, {@code spawnMobs}). Its removal of the box is
 *       redirected to {@link #onTriggered}, which claims the wave and goes {@link #ACTIVE}.</li>
 *   <li>{@link #ACTIVE}: every {@code checkIntervalTicks} the wave is checked; a mob that is unloaded, dead or past
 *       {@code leashRange} is gone. All gone: one roll of {@code rewardTable}, one stack of it ejected, then
 *       {@link #COOLDOWN}.</li>
 *   <li>{@link #COOLDOWN}: nothing until {@code cooldownTicks} of game time have passed, then {@link #ARMED}.</li>
 * </ul>
 * Clients skip Quark's tick outside {@link #ARMED}, so a spent box shows no flames. Breaking: as Lootr's chests, only
 * while sneaking ({@link #onBreak}); Create's breakers skip the box ({@code mixin/CreateBreakerMonsterBoxMixin}).
 */
public final class MonsterBoxVault {

	private MonsterBoxVault() {}

	private static final Logger LOG = LogUtils.getLogger();

	public static final byte ARMED = 0;
	public static final byte ACTIVE = 1;
	public static final byte COOLDOWN = 2;

	/** Mob persistent-data key: the claiming box's {@link BlockPos#asLong()}. */
	public static final String CLAIM_KEY = "creativeengineering:monster_box";
	/** Quark's mark on every mob its box spawns ({@code MonsterBoxModule.TAG_MONSTER_BOX_SPAWNED}). */
	private static final String QUARK_SPAWNED = "quark:monster_box_spawned";
	/** Shared-NBT keys on the box. */
	private static final String TAG_PHASE = "creativeengineering:phase";
	private static final String TAG_UNTIL = "creativeengineering:until";
	private static final String TAG_WAVE = "creativeengineering:wave";

	/** Chat line when a box is mined without sneaking. */
	public static final String SNEAK_KEY = "chat.creativeengineering.monster_box.sneak";

	private static final ResourceLocation BOX_ID = new ResourceLocation("quark", "monster_box");
	private static Block box;

	/** The state the mixin adds to Quark's {@code MonsterBoxBlockEntity}. */
	public interface Box {
		byte creativeengineering$phase();
		void creativeengineering$setPhase(byte phase);
		long creativeengineering$until();
		void creativeengineering$setUntil(long until);
		List<UUID> creativeengineering$wave();
		void creativeengineering$setBreakProgress(int progress);
		/** Mark dirty and send the shared NBT to nearby clients. */
		void creativeengineering$changed();
	}

	private static boolean vaultMode() {
		return CompatConfig.MONSTER_BOX.vaultMode.get();
	}

	public static boolean isBox(BlockState state) {
		if (box == null) box = ForgeRegistries.BLOCKS.getValue(BOX_ID);
		return box != null && state.is(box);
	}

	/** A vault box that Create's breakers must leave alone. */
	public static boolean isProtected(BlockState state) {
		return isBox(state) && vaultMode();
	}

	/** Head of Quark's tick. True = skip Quark's tick this time. */
	public static boolean tick(Level level, BlockPos pos, Box box) {
		byte phase = box.creativeengineering$phase();
		if (level.isClientSide) return phase != ARMED;
		if (!vaultMode()) {
			if (phase != ARMED) reset(box);
			return false;
		}
		if (phase == ARMED) return false;
		// Quark's tick does nothing in Peaceful; a wave or cooldown waits too.
		if (level.getDifficulty() == Difficulty.PEACEFUL) return true;

		long now = level.getGameTime();
		if (phase == COOLDOWN) {
			if (now >= box.creativeengineering$until()) reset(box);
			return true;
		}
		// ACTIVE; boxes are spread over the interval
		if (Math.floorMod(now + pos.hashCode(), CompatConfig.MONSTER_BOX.checkIntervalTicks.get()) != 0) return true;
		if (waveGone((ServerLevel) level, pos, box.creativeengineering$wave())) {
			reward((ServerLevel) level, pos);
			box.creativeengineering$wave().clear();
			box.creativeengineering$setUntil(now + CompatConfig.MONSTER_BOX.cooldownTicks.get());
			box.creativeengineering$setPhase(COOLDOWN);
			box.creativeengineering$changed();
		}
		return true;
	}

	private static void reset(Box box) {
		box.creativeengineering$wave().clear();
		box.creativeengineering$setBreakProgress(0);
		box.creativeengineering$setPhase(ARMED);
		box.creativeengineering$changed();
	}

	/**
	 * Quark's tick is about to remove the box after {@code spawnMobs}. True = keep it (the redirect skips the
	 * removal). Clients always keep it: the server's removal or phase reaches them as a block update.
	 */
	public static boolean onTriggered(Level level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof Box box)) return false;
		if (level.isClientSide) {
			box.creativeengineering$setBreakProgress(0);
			return true;
		}
		if (!vaultMode()) return false;

		List<UUID> wave = box.creativeengineering$wave();
		wave.clear();
		long claim = pos.asLong();
		// Quark pushes each mob a little from the box centre; they are in the level already.
		for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(4),
				m -> m.getPersistentData().getBoolean(QUARK_SPAWNED) && !m.getPersistentData().contains(CLAIM_KEY))) {
			mob.getPersistentData().putLong(CLAIM_KEY, claim);
			wave.add(mob.getUUID());
		}
		box.creativeengineering$setBreakProgress(0);
		box.creativeengineering$setPhase(ACTIVE);
		box.creativeengineering$changed();
		return true;
	}

	private static boolean waveGone(ServerLevel level, BlockPos pos, List<UUID> wave) {
		Vec3 centre = Vec3.atCenterOf(pos);
		double leash = CompatConfig.MONSTER_BOX.leashRange.get();
		for (UUID id : wave) {
			Entity mob = level.getEntity(id);
			if (mob != null && mob.isAlive() && mob.distanceToSqr(centre) <= leash * leash) return false;
		}
		return true;
	}

	/** One roll of the reward table; one stack of the result pops out of the box top. */
	private static void reward(ServerLevel level, BlockPos pos) {
		ResourceLocation id = ResourceLocation.tryParse(CompatConfig.MONSTER_BOX.rewardTable.get());
		LootTable table = id == null ? LootTable.EMPTY : level.getServer().getLootTables().get(id);
		if (table == LootTable.EMPTY) {
			LOG.warn("[CE] monster box reward table {} is missing or empty", CompatConfig.MONSTER_BOX.rewardTable.get());
			return;
		}
		Vec3 centre = Vec3.atCenterOf(pos);
		LootContext.Builder ctx = new LootContext.Builder(level)
				.withParameter(LootContextParams.ORIGIN, centre)
				.withRandom(level.random);
		Player near = level.getNearestPlayer(centre.x, centre.y, centre.z, 16, false);
		if (near != null) ctx.withLuck(near.getLuck()).withParameter(LootContextParams.THIS_ENTITY, near);
		List<ItemStack> rolled = table.getRandomItems(ctx.create(LootContextParamSets.CHEST));
		rolled.removeIf(ItemStack::isEmpty);
		if (rolled.isEmpty()) return;

		ItemStack stack = rolled.get(level.random.nextInt(rolled.size()));
		if (CompatConfig.MONSTER_BOX.rewardSingleCount.get()) stack.setCount(1);
		ItemEntity item = new ItemEntity(level, centre.x, pos.getY() + 1.05, centre.z, stack);
		item.setDeltaMovement(level.random.nextGaussian() * 0.05, 0.25, level.random.nextGaussian() * 0.05);
		item.setPickUpDelay(10);
		level.addFreshEntity(item);
		level.playSound(null, pos, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.8F, 0.8F);
		level.sendParticles(ParticleTypes.POOF, centre.x, pos.getY() + 1.0, centre.z, 8, 0.3, 0.1, 0.3, 0.02);
	}

	public static void write(Box box, CompoundTag tag) {
		tag.putByte(TAG_PHASE, box.creativeengineering$phase());
		tag.putLong(TAG_UNTIL, box.creativeengineering$until());
		ListTag wave = new ListTag();
		for (UUID id : box.creativeengineering$wave()) wave.add(NbtUtils.createUUID(id));
		tag.put(TAG_WAVE, wave);
	}

	public static void read(Box box, CompoundTag tag) {
		box.creativeengineering$setPhase(tag.getByte(TAG_PHASE));
		box.creativeengineering$setUntil(tag.getLong(TAG_UNTIL));
		List<UUID> wave = box.creativeengineering$wave();
		wave.clear();
		for (Tag t : tag.getList(TAG_WAVE, Tag.TAG_INT_ARRAY)) wave.add(NbtUtils.loadUUID(t));
	}

	/** Lootr's rule: the box only breaks while sneaking; otherwise a chat warning and no break. */
	@SubscribeEvent
	public static void onBreak(BlockEvent.BreakEvent event) {
		if (!isBox(event.getState()) || !vaultMode()) return;
		Player player = event.getPlayer();
		if (player == null || player.isShiftKeyDown()) return;
		event.setCanceled(true);
		if (!(player instanceof FakePlayer))
			player.displayClientMessage(Component.translatable(SNEAK_KEY).withStyle(ChatFormatting.AQUA), false);
	}
}
