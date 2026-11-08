// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/UpdateChronomarkPacket.kt.
// Translated to Java for Forge 1.19.2 (SimpleChannel message; Color.BYTE_CODEC -> Color.write / read). The handler also
// skips unloaded positions (Level.isLoaded), so a packet cannot load chunks; upstream's checks follow unchanged.
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.common.block.timedoor_marker.ChronomarkBE;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public record UpdateChronomarkPacket(BlockPos blockPos, Color color, String name, ResourceLocation access, boolean locked, float yOffset,
                                     int angle) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        color.write(buf);
        buf.writeUtf(name);
        buf.writeResourceLocation(access);
        buf.writeBoolean(locked);
        buf.writeFloat(yOffset);
        buf.writeInt(angle);
    }

    public static UpdateChronomarkPacket decode(FriendlyByteBuf buf) {
        return new UpdateChronomarkPacket(buf.readBlockPos(), Color.read(buf), buf.readUtf(), buf.readResourceLocation(), buf.readBoolean(),
                buf.readFloat(), buf.readInt());
    }

    public void handle(ServerPlayer player) {
        if (!player.level.isLoaded(blockPos)) return;
        if (!(player.level.getBlockEntity(blockPos) instanceof ChronomarkBE it)) return;
        boolean isOwner = it.owner != null && player.getGameProfile().getId().equals(it.owner.getId());
        if (!player.mayInteract(player.level, blockPos) || (it.locked && !isOwner)) return;
        it.setColor(color);
        it.setPosName(Component.literal(name));
        it.accessId = access;
        it.locked = locked && isOwner;
        it.yOffset = Mth.clamp(yOffset, -5f, 5f);
        it.angle = angle;
        it.setChanged();
    }
}
