// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/data/TravelHistoryAttachment.kt (HistoricalLocation). Translated
// to Java for Forge 1.19.2: Codec -> NBT {marker?, dimension, pos: [x, y, z]}.
package earth.terrarium.tempad.common.data;

import earth.terrarium.tempad.common.utils.CodecUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record HistoricalLocation(@Nullable ResourceLocation marker, ResourceKey<Level> dimension, Vec3 pos) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (marker != null) tag.putString("marker", marker.toString());
        tag.putString("dimension", dimension.location().toString());
        tag.put("pos", CodecUtils.saveVec3(pos));
        return tag;
    }

    public static HistoricalLocation load(CompoundTag tag) {
        ResourceLocation marker = tag.contains("marker") ? ResourceLocation.tryParse(tag.getString("marker")) : null;
        return new HistoricalLocation(marker, CodecUtils.loadDimension(tag.getString("dimension")), CodecUtils.loadVec3(tag.get("pos")));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeOptional(java.util.Optional.ofNullable(marker), FriendlyByteBuf::writeResourceLocation);
        CodecUtils.writeDimension(buf, dimension);
        CodecUtils.writeVec3(buf, pos);
    }

    public static HistoricalLocation read(FriendlyByteBuf buf) {
        ResourceLocation marker = buf.readOptional(FriendlyByteBuf::readResourceLocation).orElse(null);
        return new HistoricalLocation(marker, CodecUtils.readDimension(buf), CodecUtils.readVec3(buf));
    }
}
