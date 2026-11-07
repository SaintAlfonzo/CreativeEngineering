// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/DeleteLocationPacket.kt. Translated to Java for
// Forge 1.19.2 (SimpleChannel message).
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.locations.LocationHandler;
import earth.terrarium.tempad.api.locations.TempadLocations;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record DeleteLocationPacket(ContextHolder<?> ctx, ResourceLocation providerId, UUID locationId) {
    public void encode(FriendlyByteBuf buf) {
        ctx.encode(buf);
        buf.writeResourceLocation(providerId);
        buf.writeUUID(locationId);
    }

    public static DeleteLocationPacket decode(FriendlyByteBuf buf) {
        return new DeleteLocationPacket(ContextHolder.decode(buf), buf.readResourceLocation(), buf.readUUID());
    }

    public void handle(ServerPlayer player) {
        SyncableContext<?> context = ctx.getCtx(player);
        if (context.getStack().getItem() != ModItems.tempad.get()) return;
        LocationHandler handler = TempadLocations.get(player, context, providerId);
        if (handler != null) handler.remove(locationId);
    }
}
