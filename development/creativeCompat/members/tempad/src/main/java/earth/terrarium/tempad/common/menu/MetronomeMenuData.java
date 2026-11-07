// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/menu/MetronomeMenu.kt (MetronomeMenuData).
// Translated to Java for Forge 1.19.2: the ObjectByteCodec (UUID, GLOBAL_POS, BOOLEAN) -> encode / decode on the open-screen
// buffer (FriendlyByteBuf UUID, GlobalPos, boolean, same order).
package earth.terrarium.tempad.common.menu;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record MetronomeMenuData(UUID uuid, GlobalPos pos, boolean locked) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeGlobalPos(pos);
        buf.writeBoolean(locked);
    }

    public static MetronomeMenuData decode(FriendlyByteBuf buf) {
        return new MetronomeMenuData(buf.readUUID(), buf.readGlobalPos(), buf.readBoolean());
    }
}
