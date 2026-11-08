// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/sizing/TimedoorPlacementSettings.kt. Translated to Java for Forge
// 1.19.2: the dispatch codecs -> write/read (network: type id then the type's fields) and save/load (NBT: "type" plus
// the type's fields, as upstream's dispatch MapCodec). The Kotlin member extension TimedoorEntity.isInside(entity)
// becomes isInside(timedoor, entity).
package earth.terrarium.tempad.api.sizing;

import earth.terrarium.tempad.common.entity.TimedoorEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface TimedoorPlacementSettings {
    EntityDimensions getDimensions();

    boolean showLineAnimation();

    SizingType<?> getType();

    float widthAtPercent(float percent);

    float heightAtPercent(float percent);

    float depthAtPercent(float percent);

    void placeTimedoor(DoorType type, Vec3 anchor, float angle, TimedoorEntity timedoor);

    boolean isInside(TimedoorEntity timedoor, Entity entity);

    @SuppressWarnings("unchecked")
    static void write(FriendlyByteBuf buf, TimedoorPlacementSettings settings) {
        SizingType<TimedoorPlacementSettings> type = (SizingType<TimedoorPlacementSettings>) settings.getType();
        buf.writeResourceLocation(type.id());
        type.byteCodec().encode(settings, buf);
    }

    static TimedoorPlacementSettings read(FriendlyByteBuf buf) {
        return SizingRegistry.get(buf.readResourceLocation()).byteCodec().decode(buf);
    }

    @SuppressWarnings("unchecked")
    static CompoundTag save(TimedoorPlacementSettings settings) {
        SizingType<TimedoorPlacementSettings> type = (SizingType<TimedoorPlacementSettings>) settings.getType();
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type.id().toString());
        type.saver().accept(settings, tag);
        return tag;
    }

    @Nullable
    static TimedoorPlacementSettings load(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("type"));
        SizingType<?> type = id == null ? null : SizingRegistry.getOrNull(id);
        return type == null ? null : type.loader().apply(tag);
    }
}
