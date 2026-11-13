// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3
// (LICENSE.txt). Sends the server's component_items table to a client on join and after /reload (upstream: a synced
// 1.20 datapack registry; see ComponentRegistry).
package org.patryk3211.powergrid.network.packets;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.patryk3211.powergrid.circuits.components.ComponentItemEntry;
import org.patryk3211.powergrid.circuits.components.ComponentRegistry;
import org.patryk3211.powergrid.network.SimplePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ComponentItemsSyncS2CPacket implements SimplePacket {
    private final Map<ResourceLocation, ComponentItemEntry> entries;

    public ComponentItemsSyncS2CPacket(Map<ResourceLocation, ComponentItemEntry> entries) {
        this.entries = entries;
    }

    public ComponentItemsSyncS2CPacket(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        entries = new HashMap<>(count);
        for(int i = 0; i < count; ++i) {
            var id = buf.readResourceLocation();
            var entry = buf.readWithCodec(ComponentRegistry.ITEM_CODEC);
            entries.put(id, entry);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entries.size());
        entries.forEach((id, entry) -> {
            buf.writeResourceLocation(id);
            buf.writeWithCodec(ComponentRegistry.ITEM_CODEC, entry);
        });
    }

    @Override
    public void handle(Supplier<NetworkManager.PacketContext> context) {
        context.get().queue(() -> {
            ComponentRegistry.setClientItems(entries);
            // Port: one line per sync, so a client log shows the table arrived (CQ-PWG-22)
            org.patryk3211.powergrid.PowerGrid.LOGGER.info("Received {} component items from the server", entries.size());
        });
    }
}
