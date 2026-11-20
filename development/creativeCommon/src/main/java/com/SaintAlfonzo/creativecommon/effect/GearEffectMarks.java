// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.effect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import com.SaintAlfonzo.creativecommon.client.PermanentEffects;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Server -> client marks for gear-held effects (plan_diet_books_r2.md B2), so the inventory effect list can put them in
 * its permanent column (client.PermanentEffects).
 * <p>
 * The gear scripts (kubejs/data/creativecommon/kubejs/effects/armor_effects.js, tool_effects.js) call
 * {@link #mark(Entity, MobEffect)} for every effect they apply to a player in that tick. At the end of each server
 * tick the marks of the tick are compared with the set last sent to that player; a packet goes out only when it
 * changed. A player with no marks in a tick has the empty set, so taking gear off clears it. Login and logout reset
 * the sent set, so a rejoining player gets a fresh one.
 */
public final class GearEffectMarks {

	private static final String PROTOCOL = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation("creativecommon", "gear_effects"),
			() -> PROTOCOL, NetworkRegistry.acceptMissingOr(PROTOCOL), NetworkRegistry.acceptMissingOr(PROTOCOL));

	/** Server: marks collected during the current tick, and the last set sent, per player. */
	private static final Map<UUID, Set<ResourceLocation>> CURRENT = new HashMap<>();
	private static final Map<UUID, Set<ResourceLocation>> SENT = new HashMap<>();

	private GearEffectMarks() {
	}

	public static void register() {
		CHANNEL.messageBuilder(Marks.class, 0, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(Marks::encode).decoder(Marks::decode).consumerMainThread(Marks::handle).add();
	}

	/**
	 * Script API: {@code effect} is held by gear on {@code entity} this tick. Ignores anything but a server player and
	 * unregistered effects. Server thread only.
	 */
	public static void mark(Entity entity, MobEffect effect) {
		if (!(entity instanceof ServerPlayer player) || effect == null) {
			return;
		}
		ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
		if (id != null) {
			CURRENT.computeIfAbsent(player.getUUID(), k -> new HashSet<>()).add(id);
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}
		for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
			UUID uuid = player.getUUID();
			Set<ResourceLocation> now = CURRENT.getOrDefault(uuid, Set.of());
			Set<ResourceLocation> last = SENT.getOrDefault(uuid, Set.of());
			if (now.equals(last)) {
				continue;
			}
			SENT.put(uuid, Set.copyOf(now));
			if (player.connection != null && CHANNEL.isRemotePresent(player.connection.connection)) {
				CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Marks(Set.copyOf(now)));
			}
		}
		CURRENT.clear();
	}

	@SubscribeEvent
	public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		SENT.remove(event.getEntity().getUUID());
	}

	@SubscribeEvent
	public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		SENT.remove(event.getEntity().getUUID());
		CURRENT.remove(event.getEntity().getUUID());
	}

	/** The full set of effect ids the player's gear holds. */
	public record Marks(Set<ResourceLocation> ids) {

		static void encode(Marks m, FriendlyByteBuf buf) {
			buf.writeVarInt(m.ids.size());
			for (ResourceLocation id : m.ids) {
				buf.writeResourceLocation(id);
			}
		}

		static Marks decode(FriendlyByteBuf buf) {
			int n = buf.readVarInt();
			Set<ResourceLocation> ids = new LinkedHashSet<>();
			for (int k = 0; k < n; k++) {
				ids.add(buf.readResourceLocation());
			}
			return new Marks(ids);
		}

		static void handle(Marks m, Supplier<NetworkEvent.Context> ctx) {
			Set<MobEffect> effects = new HashSet<>();
			for (ResourceLocation id : m.ids) {
				MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
				if (effect != null) {
					effects.add(effect);
				}
			}
			PermanentEffects.setGear(effects);
			ctx.get().setPacketHandled(true);
		}
	}
}
