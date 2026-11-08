// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/CreateLocationPacket.kt. Translated to Java for
// Forge 1.19.2 (SimpleChannel message).
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.common.location_handlers.DefaultLocationHandler;
import earth.terrarium.tempad.common.registries.ModItems;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public record CreateLocationPacket(String name, Color color, ContextHolder<?> ctx) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        color.write(buf);
        ctx.encode(buf);
    }

    public static CreateLocationPacket decode(FriendlyByteBuf buf) {
        return new CreateLocationPacket(buf.readUtf(), Color.read(buf), ContextHolder.decode(buf));
    }

    public void handle(ServerPlayer player) {
        // Upstream: { ctx.stack.item === tempad } !in player.inventory (Inventory.contains(Predicate) whose test ignores the
        // slot), i.e. the context stack must be a Tempad.
        if (ctx.getCtx(player).getStack().getItem() != ModItems.tempad.get()) return;
        new DefaultLocationHandler(player.getGameProfile()).add(new NamedGlobalVec3(
                Component.literal(name), player.position(), player.level.dimension(), player.getYRot(), color));
    }
}
