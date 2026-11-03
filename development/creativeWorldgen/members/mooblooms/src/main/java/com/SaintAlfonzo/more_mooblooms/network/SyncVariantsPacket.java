package com.SaintAlfonzo.more_mooblooms.network;

import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomBreeding;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Server -> client: the whole variant table, as each variant's JSON text, and the breeding files (JEI). Sent from {@link OnDatapackSyncEvent},
 * which fires at login (before the recipe and tag packets, so JEI already sees every variant egg when it starts)
 * and after /reload. The client needs the table to draw flowers, name mooblooms and list the eggs.
 */
public record SyncVariantsPacket(Map<ResourceLocation, String> variants, Map<ResourceLocation, String> breeding) {

    private static final String PROTOCOL = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MoreMooblooms.ID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        CHANNEL.registerMessage(0, SyncVariantsPacket.class, SyncVariantsPacket::encode, SyncVariantsPacket::decode,
                SyncVariantsPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        SyncVariantsPacket packet = new SyncVariantsPacket(MoobloomVariants.serverJson(), MoobloomBreeding.serverJson());
        if (event.getPlayer() != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), packet);
        } else {
            CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        }
    }

    private void encode(FriendlyByteBuf buf) {
        writeMap(buf, variants);
        writeMap(buf, breeding);
    }

    private static SyncVariantsPacket decode(FriendlyByteBuf buf) {
        return new SyncVariantsPacket(readMap(buf), readMap(buf));
    }

    private static void writeMap(FriendlyByteBuf buf, Map<ResourceLocation, String> map) {
        buf.writeVarInt(map.size());
        map.forEach((id, json) -> {
            buf.writeResourceLocation(id);
            buf.writeUtf(json, 262144);
        });
    }

    private static Map<ResourceLocation, String> readMap(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        Map<ResourceLocation, String> map = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) map.put(buf.readResourceLocation(), buf.readUtf(262144));
        return map;
    }

    private void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            MoobloomVariants.acceptFromServer(variants);
            MoobloomBreeding.acceptFromServer(breeding);
        });
        ctx.get().setPacketHandled(true);
    }
}
