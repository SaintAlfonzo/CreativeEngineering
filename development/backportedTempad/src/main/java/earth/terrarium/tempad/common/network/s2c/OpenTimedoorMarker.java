// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/s2c/OpenTimedoorMarker.kt. Translated
// to Java for Forge 1.19.2 (SimpleChannel message). The client side runs in client/ClientPacketHandler.openTimedoorMarker
// (upstream TempadClient.openTimedoorMarker: opens TimedoorMarkerScreen).
package earth.terrarium.tempad.common.network.s2c;

import earth.terrarium.tempad.client.ClientPacketHandler;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public record OpenTimedoorMarker(BlockPos blockPos, Color color, String name, boolean canAccess, boolean locked) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        color.write(buf);
        buf.writeUtf(name);
        buf.writeBoolean(canAccess);
        buf.writeBoolean(locked);
    }

    public static OpenTimedoorMarker decode(FriendlyByteBuf buf) {
        return new OpenTimedoorMarker(buf.readBlockPos(), Color.read(buf), buf.readUtf(), buf.readBoolean(), buf.readBoolean());
    }

    public void handleClient() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.openTimedoorMarker(this));
    }
}
