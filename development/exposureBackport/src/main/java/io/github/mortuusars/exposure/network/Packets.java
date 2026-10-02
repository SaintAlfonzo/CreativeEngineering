package io.github.mortuusars.exposure.network;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.PayloadChannel;
import io.github.mortuusars.exposure.network.packet.C2SPackets;
import io.github.mortuusars.exposure.network.packet.CommonPackets;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.network.packet.S2CPackets;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Upstream {@code Packets} ({@code @ExpectPlatform}) with the NeoForge {@code PacketsImpl} folded in. Every packet
 * goes over one Forge channel ({@link PayloadChannel}); the definitions lists are upstream's.
 */
public class Packets {
    private static final PayloadChannel CHANNEL = new PayloadChannel(Exposure.resource("main"), "1");

    /** Called from the mod constructor. */
    public static void register() {
        S2CPackets.getDefinitions().forEach(CHANNEL::registerClientbound);
        C2SPackets.getDefinitions().forEach(CHANNEL::registerServerbound);
        CommonPackets.getDefinitions().forEach(CHANNEL::registerBidirectional);
    }

    /** Drops a player's unfinished split payloads (forge.event.ForgeCommonEvents, on logout). */
    public static void onPlayerLeft(java.util.UUID playerId) {
        CHANNEL.clearTransfersOf(playerId);
    }

    public static void sendToServer(Packet packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToClient(Packet packet, ServerPlayer player) {
        CHANNEL.sendToClient(packet, player);
    }

    public static void sendToClients(Packet packet, Predicate<ServerPlayer> filter) {
        CHANNEL.sendToClients(packet, filter);
    }

    public static void sendToAllClients(Packet packet) {
        CHANNEL.sendToAllClients(packet);
    }

    public static void sendToPlayersNear(Packet packet, ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius) {
        CHANNEL.sendToPlayersNear(packet, level, excluded, x, y, z, radius);
    }

    public static void sendToOtherClients(@NotNull ServerPlayer except, Packet packet) {
        except.server.getPlayerList().getPlayers().forEach(player -> {
            if (!player.equals(except)) {
                sendToClient(packet, player);
            }
        });
    }

    public static void sendToPlayersNear(Packet packet, ServerLevel level, @Nullable ServerPlayer excluded, Entity entity, double radius) {
        sendToPlayersNear(packet, level, excluded, entity.getX(), entity.getY(), entity.getZ(), radius);
    }
}
