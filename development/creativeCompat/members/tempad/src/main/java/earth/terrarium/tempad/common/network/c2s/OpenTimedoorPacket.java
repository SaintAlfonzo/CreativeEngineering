// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/OpenTimedoorPacket.kt. Translated to Java for Forge
// 1.19.2 (SimpleChannel message).
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.locations.LocationHandler;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.api.locations.TempadLocations;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record OpenTimedoorPacket(ResourceLocation providerId, UUID locationId, ContextHolder<?> ctx) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(providerId);
        buf.writeUUID(locationId);
        ctx.encode(buf);
    }

    public static OpenTimedoorPacket decode(FriendlyByteBuf buf) {
        return new OpenTimedoorPacket(buf.readResourceLocation(), buf.readUUID(), ContextHolder.decode(buf));
    }

    public void handle(ServerPlayer player) {
        if (TempadLocations.get(providerId) == null) return;
        SyncableContext<?> context = ctx.getCtx(player);
        LocationHandler handler = TempadLocations.get(player, context, providerId);
        NamedGlobalVec3 location = handler == null ? null : handler.get(locationId);
        if (location != null) {
            Component message = TimedoorEntity.openTimedoor(player, context, providerId, locationId, location);
            if (message != null) player.displayClientMessage(message, true);
        }
    }
}
