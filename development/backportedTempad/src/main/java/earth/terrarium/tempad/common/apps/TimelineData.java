// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/apps/TimelineApp.kt (TimelineData).
// Translated to Java for Forge 1.19.2: the ObjectByteCodec (map of DATE_BYTE_CODEC (epoch ms long) to
// HistoricalLocation.BYTE_CODEC, BOOLEAN, ContextHolder) -> encode / decode in the same order; the map keeps its order.
package earth.terrarium.tempad.common.apps;

import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.common.data.HistoricalLocation;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class TimelineData extends AppContent<TimelineData> {
    public final Map<Date, HistoricalLocation> history;

    public TimelineData(Map<Date, HistoricalLocation> history, boolean isStationary, ContextHolder<?> ctx) {
        super(ctx, isStationary);
        this.history = history;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        writeHistory(buf, history);
        buf.writeBoolean(isStationary);
        ctx.encode(buf);
    }

    public static TimelineData decode(FriendlyByteBuf buf) {
        Map<Date, HistoricalLocation> history = readHistory(buf);
        boolean isStationary = buf.readBoolean();
        return new TimelineData(history, isStationary, ContextHolder.decode(buf));
    }

    /** {@code ByteCodec.mapOf(DATE_BYTE_CODEC, HistoricalLocation.BYTE_CODEC)}; shared with OpenTimeTwister. */
    public static void writeHistory(FriendlyByteBuf buf, Map<Date, HistoricalLocation> history) {
        buf.writeMap(history, (b, date) -> b.writeLong(date.getTime()), (b, location) -> location.write(b));
    }

    public static Map<Date, HistoricalLocation> readHistory(FriendlyByteBuf buf) {
        return buf.readMap(LinkedHashMap::new, b -> new Date(b.readLong()), HistoricalLocation::read);
    }
}
