// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModNetworking.kt. Translated to Java for Forge 1.19.2:
// Resourceful's Network (CustomPacketPayload) -> one SimpleChannel "tempad:main". Phase 1 registers the packets of the
// Tempad item, its three apps, the keybinds and the timedoor, plus the config sync; the packets of phases 2 and 3 are
// appended after these, each phase in upstream's relative order (ids are indexes).
package earth.terrarium.tempad.common.registries;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.network.c2s.BackTrackLocation;
import earth.terrarium.tempad.common.network.c2s.CreateLocationPacket;
import earth.terrarium.tempad.common.network.c2s.DeleteLocationPacket;
import earth.terrarium.tempad.common.network.c2s.OpenAppPacket;
import earth.terrarium.tempad.common.network.c2s.OpenTempadPacket;
import earth.terrarium.tempad.common.network.c2s.OpenTimedoorPacket;
import earth.terrarium.tempad.common.network.c2s.RedirectAppPacket;
import earth.terrarium.tempad.common.network.c2s.SaveSettingsPacket;
import earth.terrarium.tempad.common.network.c2s.SetFavoritePacket;
import earth.terrarium.tempad.common.network.c2s.UpdateTempadLockPacket;
import earth.terrarium.tempad.common.network.c2s.UseMacroPacket;
import earth.terrarium.tempad.common.network.c2s.UpdateAnchorPacket;
import earth.terrarium.tempad.common.network.c2s.UpdateChronomarkPacket;
import earth.terrarium.tempad.common.network.c2s.UpdateMetronomePacket;
import earth.terrarium.tempad.common.network.c2s.SyncPortalSettingsPacket;
import earth.terrarium.tempad.common.network.c2s.WriteToCardPacket;
import earth.terrarium.tempad.common.network.s2c.ConfigSyncPacket;
import earth.terrarium.tempad.common.network.s2c.OpenChronomark;
import earth.terrarium.tempad.common.network.s2c.OpenTimeTwister;
import earth.terrarium.tempad.common.network.s2c.OpenTimedoorMarker;
import earth.terrarium.tempad.common.network.s2c.RotatePlayerMomentumPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModNetworking {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(Tempad.id("main"), () -> PROTOCOL,
            PROTOCOL::equals, PROTOCOL::equals);

    private static int nextId = 0;

    private ModNetworking() {
    }

    public static void init() {
        // c2s
        serverbound(CreateLocationPacket.class, CreateLocationPacket::encode, CreateLocationPacket::decode, CreateLocationPacket::handle);
        serverbound(DeleteLocationPacket.class, DeleteLocationPacket::encode, DeleteLocationPacket::decode, DeleteLocationPacket::handle);
        serverbound(RedirectAppPacket.class, RedirectAppPacket::encode, RedirectAppPacket::decode, RedirectAppPacket::handle);
        serverbound(OpenAppPacket.class, OpenAppPacket::encode, OpenAppPacket::decode, OpenAppPacket::handle);
        serverbound(OpenTempadPacket.class, OpenTempadPacket::encode, OpenTempadPacket::decode, OpenTempadPacket::handle);
        serverbound(OpenTimedoorPacket.class, OpenTimedoorPacket::encode, OpenTimedoorPacket::decode, OpenTimedoorPacket::handle);
        serverbound(SaveSettingsPacket.class, SaveSettingsPacket::encode, SaveSettingsPacket::decode, SaveSettingsPacket::handle);
        serverbound(SetFavoritePacket.class, SetFavoritePacket::encode, SetFavoritePacket::decode, SetFavoritePacket::handle);
        serverbound(UpdateTempadLockPacket.class, UpdateTempadLockPacket::encode, UpdateTempadLockPacket::decode, UpdateTempadLockPacket::handle);
        serverbound(UseMacroPacket.class, UseMacroPacket::encode, UseMacroPacket::decode, UseMacroPacket::handle);
        // s2c
        clientbound(RotatePlayerMomentumPacket.class, RotatePlayerMomentumPacket::encode, RotatePlayerMomentumPacket::decode, RotatePlayerMomentumPacket::handleClient);
        clientbound(ConfigSyncPacket.class, ConfigSyncPacket::encode, ConfigSyncPacket::decode, ConfigSyncPacket::handleClient);
        // phase 2 (appended: ids are registration indexes)
        serverbound(WriteToCardPacket.class, WriteToCardPacket::encode, WriteToCardPacket::decode, WriteToCardPacket::handle);
        serverbound(SyncPortalSettingsPacket.class, SyncPortalSettingsPacket::encode, SyncPortalSettingsPacket::decode, SyncPortalSettingsPacket::handle);
        // phase 3 (appended in upstream's relative order)
        serverbound(BackTrackLocation.class, BackTrackLocation::encode, BackTrackLocation::decode, BackTrackLocation::handle);
        serverbound(UpdateAnchorPacket.class, UpdateAnchorPacket::encode, UpdateAnchorPacket::decode, UpdateAnchorPacket::handle);
        serverbound(UpdateChronomarkPacket.class, UpdateChronomarkPacket::encode, UpdateChronomarkPacket::decode, UpdateChronomarkPacket::handle);
        serverbound(UpdateMetronomePacket.class, UpdateMetronomePacket::encode, UpdateMetronomePacket::decode, UpdateMetronomePacket::handle);
        clientbound(OpenTimeTwister.class, OpenTimeTwister::encode, OpenTimeTwister::decode, OpenTimeTwister::handleClient);
        clientbound(OpenTimedoorMarker.class, OpenTimedoorMarker::encode, OpenTimedoorMarker::decode, OpenTimedoorMarker::handleClient);
        clientbound(OpenChronomark.class, OpenChronomark::encode, OpenChronomark::decode, OpenChronomark::handleClient);
    }

    private static <T> void serverbound(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder,
                                        BiConsumer<T, ServerPlayer> handler) {
        CHANNEL.registerMessage(nextId++, type, encoder, decoder, (T message, Supplier<NetworkEvent.Context> ctx) -> {
            NetworkEvent.Context context = ctx.get();
            ServerPlayer player = context.getSender();
            if (player != null) context.enqueueWork(() -> handler.accept(message, player));
            context.setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private static <T> void clientbound(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder,
                                        Consumer<T> handler) {
        CHANNEL.registerMessage(nextId++, type, encoder, decoder, (T message, Supplier<NetworkEvent.Context> ctx) -> {
            NetworkEvent.Context context = ctx.get();
            context.enqueueWork(() -> handler.accept(message));
            context.setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    /** Upstream {@code sendToServer()}. */
    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    /** Upstream {@code sendToClient(player)}. */
    public static void sendToPlayer(Object message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToAll(Object message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }
}
