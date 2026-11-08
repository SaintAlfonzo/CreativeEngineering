// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/UpdateTempadLockPacket.kt. Translated to Java for
// Forge 1.19.2 (SimpleChannel message).
package earth.terrarium.tempad.common.network.c2s;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.common.registries.ModComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record UpdateTempadLockPacket(boolean locked, ContextHolder<?> ctx) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(locked);
        ctx.encode(buf);
    }

    public static UpdateTempadLockPacket decode(FriendlyByteBuf buf) {
        return new UpdateTempadLockPacket(buf.readBoolean(), ContextHolder.decode(buf));
    }

    public void handle(ServerPlayer player) {
        ctx.getCtx(player).modify(it -> {
            GameProfile owner = ModComponents.getOwner(it);
            ModComponents.setLocked(it, locked && owner != null && player.getGameProfile().getId().equals(owner.getId()));
        });
    }
}
