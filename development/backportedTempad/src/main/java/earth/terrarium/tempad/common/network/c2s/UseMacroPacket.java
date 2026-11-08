// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/UseMacroPacket.kt. Translated to Java for Forge
// 1.19.2 (SimpleChannel message).
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.api.context.ContextRegistry;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.macro.MacroRegistry;
import earth.terrarium.tempad.api.macro.TempadMacro;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record UseMacroPacket() {
    public void encode(FriendlyByteBuf buf) {
    }

    public static UseMacroPacket decode(FriendlyByteBuf buf) {
        return new UseMacroPacket();
    }

    public void handle(ServerPlayer player) {
        SyncableContext<?> ctx = ContextRegistry.locate(player, it -> it.is(ModItems.tempad.get()));
        if (ctx == null) return;
        if (player.getCooldowns().isOnCooldown(ctx.getStack().getItem())) return;
        TempadMacro macro = MacroRegistry.get(ModComponents.getDefaultMacro(ctx.getStack()));
        if (macro != null) macro.run(player, ctx);
    }
}
