// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/timedoor_marker/TimedoorMarkerBE.kt.
// Translated to Java for Forge 1.19.2: sendToClient -> ModNetworking.sendToPlayer; the attachments are AbstractMarkerBe fields.
package earth.terrarium.tempad.common.block.timedoor_marker;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.common.network.s2c.OpenTimedoorMarker;
import earth.terrarium.tempad.common.registries.ModBlocks;
import earth.terrarium.tempad.common.registries.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class TimedoorMarkerBE extends AbstractMarkerBe {
    public TimedoorMarkerBE(BlockPos pos, BlockState state) {
        super(ModBlocks.timedoorMarkerBE.get(), pos, state);
    }

    @Override
    public void openScreen(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(new OpenTimedoorMarker(getBlockPos(), getColor(), getPosName().getString(), canAccess, locked), serverPlayer);
        }
    }

    @Override
    public boolean canAccess(GameProfile player) {
        return owner != null && (canAccess || player.getId().equals(owner.getId()));
    }
}
