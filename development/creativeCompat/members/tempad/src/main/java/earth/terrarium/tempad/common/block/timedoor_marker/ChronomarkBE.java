// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/timedoor_marker/ChronomarkBE.kt.
// Translated to Java for Forge 1.19.2: sendToClient -> ModNetworking.sendToPlayer; the attachments are AbstractMarkerBe fields.
package earth.terrarium.tempad.common.block.timedoor_marker;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.api.player_access.PlayerAccess;
import earth.terrarium.tempad.api.player_access.PlayerAccessApi;
import earth.terrarium.tempad.common.network.s2c.OpenChronomark;
import earth.terrarium.tempad.common.registries.ModBlocks;
import earth.terrarium.tempad.common.registries.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ChronomarkBE extends AbstractMarkerBe {
    public ChronomarkBE(BlockPos pos, BlockState state) {
        super(ModBlocks.chronomarkBE.get(), pos, state);
    }

    @Override
    public Vec3 getLandingPosition() {
        Vec3 starting = bottomCenter().add(0.0, yOffset, 0.0);
        double xOffset = Math.cos(Math.toRadians(getLandingAngle() + 90.0));
        double zOffset = Math.sin(Math.toRadians(getLandingAngle() + 90.0));
        return starting.add(xOffset, 0.0, zOffset);
    }

    @Override
    public float getLandingAngle() {
        return (float) angle;
    }

    @Override
    public void openScreen(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(new OpenChronomark(getBlockPos(), getColor(), getPosName().getString(), PlayerAccessApi.ids(), accessId,
                    locked, yOffset, angle), serverPlayer);
        }
    }

    @Override
    public boolean canAccess(GameProfile player) {
        if (owner != null) {
            PlayerAccess access = PlayerAccessApi.get(accessId);
            if (access != null) return access.canAccess(level, owner, player);
        }
        return owner != null && player.getId().equals(owner.getId());
    }
}
