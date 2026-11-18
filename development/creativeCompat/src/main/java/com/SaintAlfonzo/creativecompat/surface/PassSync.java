package com.SaintAlfonzo.creativecompat.surface;

import java.util.Optional;
import java.util.function.Supplier;

import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Server to client sync of the pass-through values (plan 1.4). Tags already sync; {@code [surface.passthrough]} is a
 * common config, so a client would otherwise move by its own toml and rubber-band against the server. The server sends
 * its {@link PassValues} on login and whenever its toml is saved; the client uses them until it disconnects
 * ({@link PassSyncClient}). Channel {@code creativecompat:surface}, message 1 (format {@link PassValues#FORMAT};
 * a reader of another format switches the system off on the client); a side without the channel
 * is accepted (the client then uses its own values).
 */
public final class PassSync {

	private PassSync() {}

	private static final String PROTOCOL = "1";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(CreativeCompat.NS, "surface"),
			() -> PROTOCOL, NetworkRegistry.acceptMissingOr(PROTOCOL), NetworkRegistry.acceptMissingOr(PROTOCOL));

	/** Mod construction. */
	public static void register() {
		CHANNEL.registerMessage(1, Values.class, Values::encode, Values::decode, Values::handle,
				Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}

	/** The message: one {@link PassValues}. */
	public record Values(PassValues values) {
		void encode(FriendlyByteBuf buf) {
			values.write(buf);
		}

		static Values decode(FriendlyByteBuf buf) {
			return new Values(PassValues.read(buf));
		}

		void handle(Supplier<NetworkEvent.Context> ctx) {
			NetworkEvent.Context c = ctx.get();
			c.enqueueWork(() -> PassValues.setRemote(values));
			c.setPacketHandled(true);
		}
	}

	/** Forge bus: a player joined, send the server's values. */
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Values(PassValues.local()));
		}
	}

	/** The toml was saved: re-send to everyone (on the server thread). No-op without a running server. */
	static void sendToAllAfterSave() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null) return;
		server.execute(() -> CHANNEL.send(PacketDistributor.ALL.noArg(), new Values(PassValues.local())));
	}
}
