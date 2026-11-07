// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/OpenTempadPacket.kt. Translated to Java for Forge
// 1.19.2 (SimpleChannel message).
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.api.app.AppRegistry;
import earth.terrarium.tempad.api.app.TempadApp;
import earth.terrarium.tempad.api.context.ContextRegistry;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.common.registries.ModApps;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record OpenTempadPacket() {
    public void encode(FriendlyByteBuf buf) {
    }

    public static OpenTempadPacket decode(FriendlyByteBuf buf) {
        return new OpenTempadPacket();
    }

    public void handle(ServerPlayer player) {
        SyncableContext<?> ctx = ContextRegistry.locate(player, it -> it.is(ModItems.tempad.get()));
        if (ctx == null) return;
        TempadApp<?> app = AppRegistry.get(ModComponents.getDefaultApp(ctx.getStack()), ctx, false);
        if (app == null) app = AppRegistry.get(ModApps.TELEPORT, ctx, false);
        if (app != null) app.openMenu(player);
    }
}
