// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/context/SyncableContext.kt (ContextHolder). Translated to Java for
// Forge 1.19.2.
package earth.terrarium.tempad.api.context;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public record ContextHolder<T>(ContextType<T> type, T data) {
    public SyncableContext<T> getCtx(Player player) {
        return type.getCtx(player, data);
    }

    public void encode(FriendlyByteBuf buf) {
        ContextType.write(buf, type);
        type.codec().encode(data, buf);
    }

    public static ContextHolder<?> decode(FriendlyByteBuf buf) {
        return ContextType.read(buf).decode(buf);
    }
}
