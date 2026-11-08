// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/RedirectAppPacket.kt. Translated to Java for Forge
// 1.19.2 (SimpleChannel message).
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.api.app.AppRegistry;
import earth.terrarium.tempad.api.app.TempadApp;
import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record RedirectAppPacket(AppRegistry.AppHolder appHolder) {
    public RedirectAppPacket(ResourceLocation appId, ContextHolder<?> data, boolean isStationary) {
        this(new AppRegistry.AppHolder(appId, data, isStationary));
    }

    public void encode(FriendlyByteBuf buf) {
        appHolder.write(buf);
    }

    public static RedirectAppPacket decode(FriendlyByteBuf buf) {
        return new RedirectAppPacket(AppRegistry.AppHolder.read(buf));
    }

    public void handle(ServerPlayer player) {
        TempadApp<?> app = appHolder.getApp(player);
        ItemStack stack = appHolder.getCtx(player).getStack();
        if (!stack.is(ModItems.tempad.get()) || (app != null && !app.isEnabled(player))) return;
        if (app != null) app.openMenu(player);
    }
}
