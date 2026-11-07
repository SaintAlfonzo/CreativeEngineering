// Port glue (ours, GPLv3): the 1.21 teleport calls Tempad 3.0.4 uses, on Forge 1.19.2.
package earth.terrarium.tempad.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.EnumSet;
import java.util.function.Function;

public final class Teleports {
    private Teleports() {
    }

    /**
     * 1.21 {@code entity.changeDimension(DimensionTransition(level, pos, velocity, yRot, xRot, false, DO_NOTHING))}: a
     * teleporter that puts the entity at {@code pos} with that velocity and rotation, no portal sound, no portal search.
     */
    public static Entity changeDimension(Entity entity, ServerLevel target, Vec3 pos, Vec3 velocity, float yRot, float xRot) {
        return entity.changeDimension(target, new ITeleporter() {
            @Override
            public PortalInfo getPortalInfo(Entity e, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                return new PortalInfo(pos, velocity, yRot, xRot);
            }

            @Override
            public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                return false;
            }
        });
    }

    /**
     * 1.21 {@code entity.teleportTo(level, x, y, z, RelativeMovement.ALL, yRot, xRot)} inside one level: the position and
     * rotation end up absolute, but a player's client keeps its own velocity (all axes sent as relative).
     */
    public static void teleportSameLevel(Entity entity, double x, double y, double z, float yRot, float xRot) {
        if (entity instanceof ServerPlayer player) {
            ChunkPos chunk = new ChunkPos(new BlockPos(x, y, z));
            player.getLevel().getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunk, 1, player.getId());
            player.stopRiding();
            if (player.isSleeping()) player.stopSleepInBed(true, true);
            player.connection.teleport(x, y, z, yRot, xRot, EnumSet.allOf(ClientboundPlayerPositionPacket.RelativeArgument.class));
            player.setYHeadRot(yRot);
        } else {
            entity.moveTo(x, y, z, yRot, xRot);
            entity.setYHeadRot(yRot);
        }
    }
}
