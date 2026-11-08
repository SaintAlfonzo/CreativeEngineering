// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/s2c/OpenChronomark.kt. Translated to
// Java for Forge 1.19.2 (SimpleChannel message). The client side runs in client/ClientPacketHandler.openChronomark
// (upstream TempadClient.openChronomark: opens ChronomarkScreen).
package earth.terrarium.tempad.common.network.s2c;

import earth.terrarium.tempad.client.ClientPacketHandler;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;
import java.util.List;

public record OpenChronomark(BlockPos blockPos, Color color, String name, List<ResourceLocation> accessOptions, ResourceLocation access,
                             boolean locked, float yOffset, int angle) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        color.write(buf);
        buf.writeUtf(name);
        buf.writeCollection(accessOptions, FriendlyByteBuf::writeResourceLocation);
        buf.writeResourceLocation(access);
        buf.writeBoolean(locked);
        buf.writeFloat(yOffset);
        buf.writeInt(angle);
    }

    public static OpenChronomark decode(FriendlyByteBuf buf) {
        return new OpenChronomark(buf.readBlockPos(), Color.read(buf), buf.readUtf(), buf.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation),
                buf.readResourceLocation(), buf.readBoolean(), buf.readFloat(), buf.readInt());
    }

    public void handleClient() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.openChronomark(this));
    }
}
