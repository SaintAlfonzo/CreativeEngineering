// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/s2c/OpenTimeTwister.kt. Translated
// to Java for Forge 1.19.2 (SimpleChannel message; the history map uses TimelineData's codec). The client side runs in
// client/ClientPacketHandler.openTimeTwister (upstream: setScreen(TimeTwisterScreen(history, ctx))).
package earth.terrarium.tempad.common.network.s2c;

import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.client.ClientPacketHandler;
import earth.terrarium.tempad.common.apps.TimelineData;
import earth.terrarium.tempad.common.data.HistoricalLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.Date;
import java.util.Map;

public record OpenTimeTwister(Map<Date, HistoricalLocation> history, ContextHolder<?> ctx) {
    public void encode(FriendlyByteBuf buf) {
        TimelineData.writeHistory(buf, history);
        ctx.encode(buf);
    }

    public static OpenTimeTwister decode(FriendlyByteBuf buf) {
        return new OpenTimeTwister(TimelineData.readHistory(buf), ContextHolder.decode(buf));
    }

    public void handleClient() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.openTimeTwister(this));
    }
}
