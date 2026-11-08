// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/SaveSettingsPacket.kt. Translated to Java for Forge
// 1.19.2 (SimpleChannel message).
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.common.registries.ModComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record SaveSettingsPacket(ContextHolder<?> ctxData, ResourceLocation defaultApp, ResourceLocation defaultMacro) {
    public void encode(FriendlyByteBuf buf) {
        ctxData.encode(buf);
        buf.writeResourceLocation(defaultApp);
        buf.writeResourceLocation(defaultMacro);
    }

    public static SaveSettingsPacket decode(FriendlyByteBuf buf) {
        return new SaveSettingsPacket(ContextHolder.decode(buf), buf.readResourceLocation(), buf.readResourceLocation());
    }

    public void handle(ServerPlayer player) {
        SyncableContext<?> ctx = ctxData.getCtx(player);
        ItemStack tempad = ctx.getStack();
        ModComponents.setDefaultApp(tempad, defaultApp);
        ModComponents.setDefaultMacro(tempad, defaultMacro);
    }
}
