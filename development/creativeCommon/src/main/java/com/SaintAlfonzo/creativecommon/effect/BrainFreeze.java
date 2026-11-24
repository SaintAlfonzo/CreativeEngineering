// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.effect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.SaintAlfonzo.creativecommon.CommonConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Brain freeze (TODO sweets #9, feature_writeup/smoothies.md section 3). A player who finishes eating a cold food
 * ({@code #creativecommon:brain_freeze}; Creative Delight fills the tag) rolls through {@link BrainFreezeRoll} with the
 * {@code [brain_freeze]} values of creativecommon-common.toml. A hit gives creativecommon:brain_freeze (the MobEffect is
 * KubeJS, {@code kubejs_startup/creativecommon/effects/brain_freeze.js}) and a blue action-bar line. While the effect
 * lasts, any living entity's frozen ticks are held just above full, so the vanilla powdered snow overlay draws and the
 * vanilla freeze damage (1 every 2 s, none with leather armor or for freeze-immune mobs) and slowdown apply; when it
 * ends the frost thaws the vanilla way. Fire Resistance does not stop it (vanilla freeze damage ignores it).
 * The counts live on the server only, per player, and are dropped at logout.
 */
public final class BrainFreeze {

	public static final ResourceLocation ID = new ResourceLocation("creativecommon", "brain_freeze");
	public static final TagKey<Item> COLD_FOODS = ItemTags.create(new ResourceLocation("creativecommon", "brain_freeze"));
	public static final String MESSAGE = "message.creativecommon.brain_freeze";

	private static final Map<UUID, BrainFreezeRoll> ROLLS = new HashMap<>();
	private static MobEffect effect;

	private BrainFreeze() {}

	private static MobEffect effect() {
		if (effect == null) effect = ForgeRegistries.MOB_EFFECTS.getValue(ID);
		return effect;
	}

	@SubscribeEvent
	public static void onFinish(LivingEntityUseItemEvent.Finish event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) return;
		CommonConfig.BrainFreezeConfig cfg = CommonConfig.BRAIN_FREEZE;
		if (!cfg.enabled.get()) return;
		ItemStack stack = event.getItem();
		if (stack.isEmpty() || !stack.is(COLD_FOODS)) return;
		BrainFreezeRoll roll = ROLLS.computeIfAbsent(player.getUUID(), u -> new BrainFreezeRoll());
		int seconds = roll.eat(player.level.getGameTime(), cfg.windowSeconds.get() * 20L, cfg.chanceStep.get(),
				cfg.lengths.get(), player.getRandom()::nextDouble);
		if (seconds > 0) freeze(player, seconds);
	}

	/** Freezes the player for the given seconds and sends the blue action-bar line. */
	public static void freeze(ServerPlayer player, int seconds) {
		MobEffect e = effect();
		if (e != null) {
			player.addEffect(new MobEffectInstance(e, seconds * 20, 0, false, false, true));
		} else {
			// effect not registered (KubeJS missing): frozen ticks alone, thawing 2 per tick
			player.setTicksFrozen(Math.max(player.getTicksFrozen(), player.getTicksRequiredToFreeze() + seconds * 40));
		}
		player.displayClientMessage(Component.translatable(MESSAGE).withStyle(ChatFormatting.BLUE), true);
	}

	@SubscribeEvent
	public static void onLivingTick(LivingEvent.LivingTickEvent event) {
		LivingEntity entity = event.getEntity();
		if (entity.level.isClientSide) return;
		MobEffect e = effect();
		if (e == null || !entity.hasEffect(e)) return;
		// LivingTickEvent fires before aiStep, which thaws 2 ticks when the entity is not in powdered snow: + 2 keeps it
		// at the full-freeze mark for the freeze damage check that follows
		int full = entity.getTicksRequiredToFreeze() + 2;
		if (entity.getTicksFrozen() < full) entity.setTicksFrozen(full);
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		ROLLS.remove(event.getEntity().getUUID());
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		ROLLS.clear();
	}
}
