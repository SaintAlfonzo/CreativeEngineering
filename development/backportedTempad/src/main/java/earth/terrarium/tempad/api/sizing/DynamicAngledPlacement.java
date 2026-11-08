// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/sizing/DynamicAngledPlacement.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.api.sizing;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.common.config.CommonConfig;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import earth.terrarium.tempad.common.utils.BufCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;

public class DynamicAngledPlacement implements TimedoorPlacementSettings {
    public static final SizingType<DynamicAngledPlacement> TYPE = new SizingType<>(Tempad.id("default"),
            BufCodec.of((buf, v) -> { }, buf -> new DynamicAngledPlacement()), tag -> new DynamicAngledPlacement(), (v, tag) -> { });

    public final float width = 20 / 16f;
    public final float height = 36 / 16f;
    public final float depth = 6 / 16f;
    private final EntityDimensions dimensions = EntityDimensions.fixed(width, height);

    @Override
    public boolean showLineAnimation() {
        return true;
    }

    @Override
    public SizingType<?> getType() {
        return TYPE;
    }

    @Override
    public EntityDimensions getDimensions() {
        return dimensions;
    }

    @Override
    public float widthAtPercent(float percent) {
        if (percent < 0.5) return width * percent * 2;
        return width;
    }

    @Override
    public float heightAtPercent(float percent) {
        if (percent > 0.5) return Mth.lerp((percent - 0.5f) * 2, 0.2f, height);
        return 0.2f;
    }

    @Override
    public float depthAtPercent(float percent) {
        return depth;
    }

    @Override
    public void placeTimedoor(DoorType type, Vec3 anchor, float angle, TimedoorEntity timedoor) {
        Vec3 offset = NamedGlobalVec3.offsetLocation(anchor, angle, type == DoorType.ENTRY ? CommonConfig.TimeDoor.placementDistance() : 1);
        timedoor.setPos(offset.x, anchor.y, offset.z);
        timedoor.setYRot(angle + (type == DoorType.ENTRY ? 180 : 0));
    }

    @Override
    public boolean isInside(TimedoorEntity timedoor, Entity entity) {
        double dx = entity.getX() - timedoor.getX();
        double dz = entity.getZ() - timedoor.getZ();
        double hypotenuse = dx * dx + dz * dz;
        float alpha = (float) Mth.atan2(dz, dx);
        float theta = Mth.sin(alpha - timedoor.getYRot() * Mth.DEG_TO_RAD);
        float maxDistance = (depth / 2) + entity.getBbWidth() / 2f;
        return theta * theta * hypotenuse < maxDistance * maxDistance;
    }
}
