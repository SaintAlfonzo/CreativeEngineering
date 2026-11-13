// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3
// (LICENSE.txt). Sends the server's wire types to a client (upstream: a synced 1.20 datapack registry; see WireRegistry).
package org.patryk3211.powergrid.network.packets;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.patryk3211.powergrid.electricity.wire.registry.WireItemEntry;
import org.patryk3211.powergrid.electricity.wire.registry.WireRegistry;
import org.patryk3211.powergrid.network.SimplePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class WireTypesSyncS2CPacket implements SimplePacket {
    private final Map<ResourceLocation, WireItemEntry> entries;

    public WireTypesSyncS2CPacket(Map<ResourceLocation, WireItemEntry> entries) {
        this.entries = entries;
    }

    public WireTypesSyncS2CPacket(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        entries = new HashMap<>(count);
        for(int i = 0; i < count; ++i) {
            var id = buf.readResourceLocation();
            var entry = buf.readWithCodec(WireItemEntry.CODEC);
            entries.put(id, entry);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entries.size());
        entries.forEach((id, entry) -> {
            buf.writeResourceLocation(id);
            buf.writeWithCodec(WireItemEntry.CODEC, entry);
        });
    }

    @Override
    public void handle(Supplier<NetworkManager.PacketContext> context) {
        context.get().queue(() -> WireRegistry.setClientEntries(entries));
    }
}
