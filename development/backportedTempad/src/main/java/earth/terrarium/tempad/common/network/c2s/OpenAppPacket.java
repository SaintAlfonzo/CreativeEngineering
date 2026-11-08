// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/OpenAppPacket.kt. Translated to Java for Forge
// 1.19.2 (SimpleChannel message).
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.api.app.AppRegistry;
import earth.terrarium.tempad.api.app.TempadApp;
import earth.terrarium.tempad.api.context.ContextRegistry;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record OpenAppPacket(ResourceLocation id) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
    }

    public static OpenAppPacket decode(FriendlyByteBuf buf) {
        return new OpenAppPacket(buf.readResourceLocation());
    }

    public void handle(ServerPlayer player) {
        SyncableContext<?> ctx = ContextRegistry.locate(player, it -> it.is(ModItems.tempad.get()));
        if (ctx == null) return;
        TempadApp<?> app = AppRegistry.get(id, ctx, false);
        if (app != null) app.openMenu(player);
    }
}
