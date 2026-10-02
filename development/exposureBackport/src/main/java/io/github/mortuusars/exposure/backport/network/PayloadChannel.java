package io.github.mortuusars.exposure.backport.network;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Carries 1.21-style {@link CustomPacketPayload}s over one Forge {@link SimpleChannel}. On the wire a payload is its
 * type id followed by its {@link StreamCodec} encoding. Handlers run on the main thread, like NeoForge's default.
 * <p>
 * Each payload type is registered for the direction(s) it may travel, and a payload arriving the wrong way is
 * dropped: a client must not be able to make the server run a clientbound handler (client-only classes).
 * <p>
 * <b>Splitting.</b> NeoForge splits oversized payloads transparently, and Exposure relies on it (a captured image is
 * sent in one payload). 1.19.2 caps serverbound custom payloads at 32 KiB and clientbound ones at 1 MiB, so payloads
 * over the limit go as numbered {@link Part}s and are reassembled before decoding. Reassembly is bounded per
 * connection: {@link #MAX_ASSEMBLED_BYTES}, {@link #MAX_PENDING_TRANSFERS}, and stale transfers expire.
 */
public class PayloadChannel {
    private static final int SERVERBOUND_CHUNK = 32_000;
    private static final int CLIENTBOUND_CHUNK = 1_000_000;
    private static final int MAX_ASSEMBLED_BYTES = 16 * 1024 * 1024; // a 2048x2048 exposure is 4 MiB
    private static final int MAX_PENDING_TRANSFERS = 4;
    private static final long TRANSFER_TIMEOUT_MS = 60_000;

    private final SimpleChannel channel;
    private final Map<ResourceLocation, Entry<?>> entries = new HashMap<>();
    private final AtomicInteger nextTransferId = new AtomicInteger();
    /** sender (player UUID, or {@link #FROM_SERVER} on the client) -> transfers in progress */
    private final Map<UUID, Map<Integer, Assembly>> assemblies = new HashMap<>();
    private static final UUID FROM_SERVER = new UUID(0, 0);

    public PayloadChannel(ResourceLocation name, String protocolVersion) {
        this.channel = NetworkRegistry.newSimpleChannel(name, () -> protocolVersion, protocolVersion::equals, protocolVersion::equals);
        this.channel.registerMessage(0, Whole.class, Whole::write, this::decodeWhole, this::handleWhole);
        this.channel.registerMessage(1, Part.class, Part::write, Part::read, this::handlePart);
    }

    @FunctionalInterface
    public interface Handler<T extends CustomPacketPayload> {
        /** @param player the sender on the server, the local player on the client */
        void handle(T payload, PacketFlow flow, Player player);
    }

    private record Entry<T extends CustomPacketPayload>(StreamCodec<FriendlyByteBuf, T> codec, Handler<? super T> handler,
                                                         boolean clientbound, boolean serverbound) {
    }

    /** One payload. Outgoing: its encoded bytes (id + data). Incoming: the decoded payload. */
    private record Whole(@Nullable byte[] encoded, @Nullable CustomPacketPayload payload) {
        void write(FriendlyByteBuf buffer) {
            buffer.writeBytes(Objects.requireNonNull(encoded));
        }
    }

    private record Part(int transferId, int index, int count, byte[] data) {
        void write(FriendlyByteBuf buffer) {
            buffer.writeVarInt(transferId);
            buffer.writeVarInt(index);
            buffer.writeVarInt(count);
            buffer.writeByteArray(data);
        }

        static Part read(FriendlyByteBuf buffer) {
            int transferId = buffer.readVarInt();
            int index = buffer.readVarInt();
            int count = buffer.readVarInt();
            byte[] data = buffer.readByteArray(Math.max(SERVERBOUND_CHUNK, CLIENTBOUND_CHUNK));
            return new Part(transferId, index, count, data);
        }
    }

    private static final class Assembly {
        final byte[][] parts;
        final long startedAt = System.currentTimeMillis();
        int received;
        long bytes;

        Assembly(int count) {
            this.parts = new byte[count][];
        }
    }

    // ---- registration ----

    /**
     * Registers a payload with a handler that receives it as {@code P}: every payload this channel carries
     * implements {@code P} (e.g. Exposure's {@code Packet}).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <P extends CustomPacketPayload> void register(CustomPacketPayload.TypeAndCodec<?, ?> definition, Handler<P> handler,
                                                         boolean clientbound, boolean serverbound) {
        ResourceLocation id = definition.type().id();
        if (entries.containsKey(id)) {
            throw new IllegalStateException("Payload '" + id + "' is already registered");
        }
        entries.put(id, new Entry(definition.codec(), handler, clientbound, serverbound));
    }

    public void registerClientbound(CustomPacketPayload.TypeAndCodec<?, ?> definition) {
        register(definition, PayloadChannel::handleAsPacket, true, false);
    }

    public void registerServerbound(CustomPacketPayload.TypeAndCodec<?, ?> definition) {
        register(definition, PayloadChannel::handleAsPacket, false, true);
    }

    public void registerBidirectional(CustomPacketPayload.TypeAndCodec<?, ?> definition) {
        register(definition, PayloadChannel::handleAsPacket, true, true);
    }

    /** Default handler: the payload handles itself ({@code boolean handle(PacketFlow, Player)}, Exposure's packet shape). */
    private static void handleAsPacket(CustomPacketPayload payload, PacketFlow flow, Player player) {
        if (payload instanceof HandledPayload handled) {
            handled.handle(flow, player);
        } else {
            throw new IllegalStateException("Payload '" + payload.type().id() + "' has no handler");
        }
    }

    // ---- sending ----

    public void sendToServer(CustomPacketPayload payload) {
        send(payload, SERVERBOUND_CHUNK, channel::sendToServer);
    }

    public void sendToClient(CustomPacketPayload payload, ServerPlayer player) {
        send(payload, CLIENTBOUND_CHUNK, msg -> channel.send(PacketDistributor.PLAYER.with(() -> player), msg));
    }

    public void sendToClients(CustomPacketPayload payload, Predicate<ServerPlayer> filter) {
        MinecraftServer server = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(), "Cannot send clientbound payloads on the client");
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (filter.test(player)) {
                sendToClient(payload, player);
            }
        }
    }

    public void sendToAllClients(CustomPacketPayload payload) {
        send(payload, CLIENTBOUND_CHUNK, msg -> channel.send(PacketDistributor.ALL.noArg(), msg));
    }

    public void sendToPlayersNear(CustomPacketPayload payload, ServerLevel level, @Nullable ServerPlayer excluded,
                                  double x, double y, double z, double radius) {
        send(payload, CLIENTBOUND_CHUNK, msg -> channel.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(excluded, x, y, z, radius, level.dimension())), msg));
    }

    private void send(CustomPacketPayload payload, int chunkSize, Consumer<Object> sender) {
        byte[] encoded = encode(payload);
        if (encoded.length <= chunkSize) {
            sender.accept(new Whole(encoded, null));
            return;
        }
        int count = (encoded.length + chunkSize - 1) / chunkSize;
        int transferId = nextTransferId.incrementAndGet();
        for (int i = 0; i < count; i++) {
            int from = i * chunkSize;
            int to = Math.min(encoded.length, from + chunkSize);
            byte[] chunk = new byte[to - from];
            System.arraycopy(encoded, from, chunk, 0, chunk.length);
            sender.accept(new Part(transferId, i, count, chunk));
        }
    }

    // ---- wire ----

    @SuppressWarnings("unchecked")
    private byte[] encode(CustomPacketPayload payload) {
        ResourceLocation id = payload.type().id();
        Entry<CustomPacketPayload> entry = (Entry<CustomPacketPayload>) entries.get(id);
        if (entry == null) {
            throw new IllegalStateException("Payload '" + id + "' is not registered");
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeResourceLocation(id);
            entry.codec().encode(buffer, payload);
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return bytes;
        } finally {
            buffer.release();
        }
    }

    private CustomPacketPayload decodePayload(FriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        Entry<?> entry = entries.get(id);
        if (entry == null) {
            throw new IllegalStateException("Unknown payload '" + id + "'");
        }
        return entry.codec().decode(buffer);
    }

    private Whole decodeWhole(FriendlyByteBuf buffer) {
        return new Whole(null, decodePayload(buffer));
    }

    private void handleWhole(Whole whole, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);
        dispatch(Objects.requireNonNull(whole.payload()), context);
    }

    private void handlePart(Part part, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);
        ServerPlayer sender = context.getSender();
        UUID senderKey = sender != null ? sender.getUUID() : FROM_SERVER;
        byte[] complete = accept(senderKey, part);
        if (complete == null) {
            return;
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(complete));
        CustomPacketPayload payload;
        try {
            payload = decodePayload(buffer);
        } catch (Exception e) {
            Exposure.LOGGER.error("Cannot decode a split payload of {} bytes: {}", complete.length, e.toString());
            return;
        }
        dispatch(payload, context);
    }

    /** Stores a part; returns the whole payload's bytes once the last part arrived. */
    @Nullable
    private synchronized byte[] accept(UUID senderKey, Part part) {
        if (part.count() <= 0 || part.index() < 0 || part.index() >= part.count()) {
            return null;
        }
        Map<Integer, Assembly> pending = assemblies.computeIfAbsent(senderKey, k -> new LinkedHashMap<>());
        long now = System.currentTimeMillis();
        pending.values().removeIf(a -> now - a.startedAt > TRANSFER_TIMEOUT_MS);
        Assembly assembly = pending.get(part.transferId());
        if (assembly == null) {
            if (pending.size() >= MAX_PENDING_TRANSFERS) {
                Iterator<Integer> oldest = pending.keySet().iterator();
                oldest.next();
                oldest.remove();
            }
            assembly = new Assembly(part.count());
            pending.put(part.transferId(), assembly);
        }
        if (assembly.parts.length != part.count() || assembly.parts[part.index()] != null) {
            pending.remove(part.transferId());
            return null;
        }
        assembly.bytes += part.data().length;
        if (assembly.bytes > MAX_ASSEMBLED_BYTES) {
            Exposure.LOGGER.warn("Dropping a split payload over {} bytes", MAX_ASSEMBLED_BYTES);
            pending.remove(part.transferId());
            return null;
        }
        assembly.parts[part.index()] = part.data();
        if (++assembly.received < assembly.parts.length) {
            return null;
        }
        pending.remove(part.transferId());
        byte[] complete = new byte[(int) assembly.bytes];
        int offset = 0;
        for (byte[] chunk : assembly.parts) {
            System.arraycopy(chunk, 0, complete, offset, chunk.length);
            offset += chunk.length;
        }
        return complete;
    }

    /** Forget a player's unfinished transfers (they left). */
    public synchronized void clearTransfersOf(UUID playerId) {
        assemblies.remove(playerId);
    }

    @SuppressWarnings("unchecked")
    private void dispatch(CustomPacketPayload payload, NetworkEvent.Context context) {
        Entry<CustomPacketPayload> entry = (Entry<CustomPacketPayload>) entries.get(payload.type().id());
        boolean toClient = context.getDirection() == NetworkDirection.PLAY_TO_CLIENT || context.getDirection() == NetworkDirection.LOGIN_TO_CLIENT;
        if (entry == null || (toClient ? !entry.clientbound() : !entry.serverbound())) {
            return; // wrong direction: ignore
        }
        context.enqueueWork(() -> {
            if (toClient) {
                Player player = ClientAccess.player();
                if (player != null) {
                    entry.handler().handle(payload, PacketFlow.CLIENTBOUND, player);
                }
            } else {
                ServerPlayer sender = context.getSender();
                if (sender != null) {
                    entry.handler().handle(payload, PacketFlow.SERVERBOUND, sender);
                }
            }
        });
    }

    /** Payloads that handle themselves (upstream's {@code Packet} interfaces extend this). */
    public interface HandledPayload extends CustomPacketPayload {
        boolean handle(PacketFlow flow, Player player);
    }

    /** Loaded only when a clientbound payload arrives, so dedicated servers never resolve {@link Minecraft}. */
    private static class ClientAccess {
        static @Nullable Player player() {
            return Minecraft.getInstance().player;
        }
    }
}
