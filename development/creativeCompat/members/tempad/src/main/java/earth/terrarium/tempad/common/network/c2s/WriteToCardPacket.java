// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/WriteToCardPacket.kt. Translated to Java for
// Forge 1.19.2 (SimpleChannel message). Sent by the Teleport screen when a location is clicked onto a carried location
// card: the carried card (one of the stack) is exchanged for a card written with that location.
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.locations.LocationGetter;
import earth.terrarium.tempad.api.locations.LocationHandler;
import earth.terrarium.tempad.api.locations.TempadLocations;
import earth.terrarium.tempad.common.menu.CarriedMenuCtx;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record WriteToCardPacket(ResourceLocation providerId, UUID locationId, ContextHolder<?> ctx) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(providerId);
        buf.writeUUID(locationId);
        ctx.encode(buf);
    }

    public static WriteToCardPacket decode(FriendlyByteBuf buf) {
        return new WriteToCardPacket(buf.readResourceLocation(), buf.readUUID(), ContextHolder.decode(buf));
    }

    public void handle(ServerPlayer player) {
        if (TempadLocations.get(providerId) == null) return;
        SyncableContext<?> context = ctx.getCtx(player);
        CarriedMenuCtx menuCtx = new CarriedMenuCtx(player);

        if (menuCtx.getStack().getItem() != ModItems.locationCard.get()) return;
        ItemStack card = new ItemStack(ModItems.locationCard.get());
        LocationHandler handler = TempadLocations.get(player, context, providerId);
        LocationGetter target = handler == null ? null : handler.getSerializable(locationId);
        if (target != null) ModComponents.setPortalTarget(card, target);
        menuCtx.exchange(card);
    }
}
