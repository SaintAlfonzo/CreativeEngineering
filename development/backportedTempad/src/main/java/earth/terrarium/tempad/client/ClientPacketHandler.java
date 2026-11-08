// Client side of the port's clientbound packets (ours, GPLv3). Only reached through DistExecutor on Dist.CLIENT.
package earth.terrarium.tempad.client;

import earth.terrarium.tempad.client.screen.ChronomarkScreen;
import earth.terrarium.tempad.client.screen.TimeTwisterScreen;
import earth.terrarium.tempad.client.screen.TimedoorMarkerScreen;
import earth.terrarium.tempad.common.network.s2c.OpenChronomark;
import earth.terrarium.tempad.common.network.s2c.OpenTimeTwister;
import earth.terrarium.tempad.common.network.s2c.OpenTimedoorMarker;
import earth.terrarium.tempad.common.network.s2c.RotatePlayerMomentumPacket;
import net.minecraft.client.Minecraft;

public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    /**
     * RotatePlayerMomentumPacket handler. Fix (TODO gameplay/tnt-and-portal #9 item 3): upstream zeroes the local
     * player's velocity ("TODO rotate player momentum"); this turns it by the packet's angle (radians, from
     * TimedoorEntity.momentumRotation), so the player keeps their speed and leaves along the exit facing.
     */
    public static void rotatePlayerMomentum(RotatePlayerMomentumPacket packet) {
        var player = Minecraft.getInstance().player;
        if (player != null) player.setDeltaMovement(player.getDeltaMovement().yRot(packet.angle()));
    }

    /** Upstream OpenTimeTwister handler. */
    public static void openTimeTwister(OpenTimeTwister packet) {
        Minecraft.getInstance().setScreen(new TimeTwisterScreen(packet.history(), packet.ctx()));
    }

    /** Upstream TempadClient.openTimedoorMarker. */
    public static void openTimedoorMarker(OpenTimedoorMarker packet) {
        Minecraft.getInstance().setScreen(new TimedoorMarkerScreen(packet.blockPos(), packet.name(), packet.color(), packet.canAccess(), packet.locked()));
    }

    /** Upstream TempadClient.openChronomark. */
    public static void openChronomark(OpenChronomark packet) {
        Minecraft.getInstance().setScreen(new ChronomarkScreen(packet.blockPos(), packet.name(), packet.color(), packet.accessOptions(), packet.access(),
                packet.locked(), packet.yOffset(), packet.angle()));
    }
}
