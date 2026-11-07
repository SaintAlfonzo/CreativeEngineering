// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/context/SyncableContext.kt (ContextType). Translated to Java for
// Forge 1.19.2: ByteCodec -> BufCodec on FriendlyByteBuf.
package earth.terrarium.tempad.api.context;

import earth.terrarium.tempad.common.utils.BufCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record ContextType<T>(ResourceLocation id, BufCodec<T> codec) {
    public SyncableContext<T> getCtx(Player player, T context) {
        return ContextRegistry.get(this, player, context);
    }

    public ContextHolder<T> decode(FriendlyByteBuf buf) {
        return new ContextHolder<>(this, codec.decode(buf));
    }

    public static void write(FriendlyByteBuf buf, ContextType<?> type) {
        buf.writeResourceLocation(type.id());
    }

    public static ContextType<?> read(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        for (ContextType<?> type : ContextRegistry.registry().keySet()) {
            if (type.id().equals(id)) return type;
        }
        throw new IllegalStateException("Unknown Tempad context type " + id);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ContextType<?> other && other.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
