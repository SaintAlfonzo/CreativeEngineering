// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/sizing/VerticalPlacementSettings.kt. Translated to Java for Forge
// 1.19.2.
package earth.terrarium.tempad.api.sizing;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import earth.terrarium.tempad.common.utils.BufCodec;
import net.minecraft.world.phys.Vec3;

public class VerticalPlacementSettings extends DynamicAngledPlacement {
    // Fix (TODO gameplay/tnt-and-portal #9 item 1): each key is written from its own axis (see FloorPlacementSettings).
    public static final SizingType<VerticalPlacementSettings> TYPE = new SizingType<>(Tempad.id("vertical"),
            BufCodec.of((buf, v) -> {
                buf.writeFloat(v.xOffset);
                buf.writeFloat(v.yOffset);
                buf.writeFloat(v.zOffset);
            }, buf -> new VerticalPlacementSettings(buf.readFloat(), buf.readFloat(), buf.readFloat())),
            tag -> new VerticalPlacementSettings(tag.getFloat("x"), tag.getFloat("y"), tag.getFloat("z")),
            (v, tag) -> {
                tag.putFloat("x", v.xOffset);
                tag.putFloat("y", v.yOffset);
                tag.putFloat("z", v.zOffset);
            });

    public final float xOffset;
    public final float yOffset;
    public final float zOffset;

    public VerticalPlacementSettings(float xOffset, float yOffset, float zOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    @Override
    public SizingType<?> getType() {
        return TYPE;
    }

    @Override
    public void placeTimedoor(DoorType type, Vec3 anchor, float angle, TimedoorEntity timedoor) {
        if (type == DoorType.ENTRY) {
            timedoor.setPos(anchor.x + xOffset, anchor.y + yOffset, anchor.z + zOffset);
        } else {
            Vec3 offset = NamedGlobalVec3.offsetLocation(anchor, angle, 1);
            timedoor.setPos(offset.x, anchor.y, offset.z);
        }
        timedoor.setYRot(angle);
    }
}
