// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/config/ConfigCache.kt
// (ConfigSync). Translated to Java for Forge 1.19.2: one message with every synced value (upstream sends a map of
// per-entry packets), plus requireLocationUpgrade (the feature flag replacement).
package earth.terrarium.tempad.common.network.s2c;

import earth.terrarium.tempad.common.config.CommonConfigCache;
import net.minecraft.network.FriendlyByteBuf;

public record ConfigSyncPacket(int chrononCellCapacity, int batteryCapacity, int chrononGeneratorCapacity, int chronometerCapacity,
                               int chronomarkMaxOffset, int metronomeCapacity, boolean metronomeScaleGeneration,
                               int rudimentaryTempadCapacity, int tempadCapacity, int tempadMaxOffset, int timeTwisterCapacity,
                               boolean requireLocationUpgrade) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(chrononCellCapacity);
        buf.writeInt(batteryCapacity);
        buf.writeInt(chrononGeneratorCapacity);
        buf.writeInt(chronometerCapacity);
        buf.writeInt(chronomarkMaxOffset);
        buf.writeInt(metronomeCapacity);
        buf.writeBoolean(metronomeScaleGeneration);
        buf.writeInt(rudimentaryTempadCapacity);
        buf.writeInt(tempadCapacity);
        buf.writeInt(tempadMaxOffset);
        buf.writeInt(timeTwisterCapacity);
        buf.writeBoolean(requireLocationUpgrade);
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf buf) {
        return new ConfigSyncPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean());
    }

    public void handleClient() {
        CommonConfigCache.apply(this);
    }
}
