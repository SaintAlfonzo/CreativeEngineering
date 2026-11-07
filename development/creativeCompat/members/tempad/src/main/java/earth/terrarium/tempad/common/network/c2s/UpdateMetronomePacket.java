// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/UpdateMetronomePacket.kt.
// Translated to Java for Forge 1.19.2 (SimpleChannel message). The handler also skips unloaded positions (Level.isLoaded), so a
// packet cannot load chunks; upstream's checks follow unchanged.
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.common.block.MetronomeBe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record UpdateMetronomePacket(BlockPos blockPos, boolean locked) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeBoolean(locked);
    }

    public static UpdateMetronomePacket decode(FriendlyByteBuf buf) {
        return new UpdateMetronomePacket(buf.readBlockPos(), buf.readBoolean());
    }

    public void handle(ServerPlayer player) {
        if (!player.level.isLoaded(blockPos)) return;
        if (!(player.level.getBlockEntity(blockPos) instanceof MetronomeBe it)) return;
        boolean isOwner = it.owner != null && player.getGameProfile().getId().equals(it.owner.getId());
        if (!player.mayInteract(player.level, blockPos) || (it.locked && !isOwner)) return;
        it.locked = locked && isOwner;
        it.setChanged();
    }
}
