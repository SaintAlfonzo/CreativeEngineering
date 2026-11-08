// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/UpdateAnchorPacket.kt.
// Translated to Java for Forge 1.19.2 (SimpleChannel message; Color.BYTE_CODEC -> Color.write / read). The handler also
// skips unloaded positions (Level.isLoaded), so a packet cannot load chunks; upstream's checks follow unchanged.
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.common.block.timedoor_marker.TimedoorMarkerBE;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public record UpdateAnchorPacket(BlockPos blockPos, Color color, String name, boolean access, boolean locked) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        color.write(buf);
        buf.writeUtf(name);
        buf.writeBoolean(access);
        buf.writeBoolean(locked);
    }

    public static UpdateAnchorPacket decode(FriendlyByteBuf buf) {
        return new UpdateAnchorPacket(buf.readBlockPos(), Color.read(buf), buf.readUtf(), buf.readBoolean(), buf.readBoolean());
    }

    public void handle(ServerPlayer player) {
        if (!player.level.isLoaded(blockPos)) return;
        if (!(player.level.getBlockEntity(blockPos) instanceof TimedoorMarkerBE it)) return;
        boolean isOwner = it.owner != null && player.getGameProfile().getId().equals(it.owner.getId());
        if (!player.mayInteract(player.level, blockPos) || (it.locked && !isOwner)) return;
        it.setColor(color);
        it.setPosName(Component.literal(name));
        it.canAccess = access;
        it.locked = locked && isOwner;
        it.setChanged();
    }
}
