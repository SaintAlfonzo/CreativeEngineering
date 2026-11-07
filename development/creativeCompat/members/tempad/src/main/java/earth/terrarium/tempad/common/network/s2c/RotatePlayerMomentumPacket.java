// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/s2c/RotatePlayerMomentumPacket.kt. Translated to Java for
// Forge 1.19.2 (SimpleChannel message). The client side runs in client/ClientPacketHandler.
package earth.terrarium.tempad.common.network.s2c;

import earth.terrarium.tempad.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** {@code angle}: radians for Vec3.yRot (TimedoorEntity.momentumRotation). */
public record RotatePlayerMomentumPacket(float angle) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(angle);
    }

    public static RotatePlayerMomentumPacket decode(FriendlyByteBuf buf) {
        return new RotatePlayerMomentumPacket(buf.readFloat());
    }

    public void handleClient() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.rotatePlayerMomentum(this));
    }
}
