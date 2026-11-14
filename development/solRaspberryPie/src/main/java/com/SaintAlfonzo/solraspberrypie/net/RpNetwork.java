package com.SaintAlfonzo.solraspberrypie.net;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/** Raspberry Pie's channel (13.3): ladder, player state, messages and previews to the client; slot actions to the server. */
public final class RpNetwork {
    private static final String VERSION = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SolRaspberryPie.MOD_ID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, S2CLadder.class, S2CLadder::encode, S2CLadder::decode, S2CLadder::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, S2CState.class, S2CState::encode, S2CState::decode, S2CState::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, S2CMessage.class, S2CMessage::encode, S2CMessage::decode, S2CMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, S2CPreview.class, S2CPreview::encode, S2CPreview::decode, S2CPreview::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, C2SAction.class, C2SAction::encode, C2SAction::decode, C2SAction::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void toPlayer(ServerPlayer player, Object msg) {
        if (player.connection == null) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void toServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    private RpNetwork() {}
}
