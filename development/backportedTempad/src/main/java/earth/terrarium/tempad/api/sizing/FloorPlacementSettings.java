// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/sizing/FloorPlacementSettings.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.api.sizing;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import earth.terrarium.tempad.common.utils.BufCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;

public class FloorPlacementSettings implements TimedoorPlacementSettings {
    // Fix (TODO gameplay/tnt-and-portal #9 item 1): upstream's MapCodec writes "x" and "z" from yOffset; each key is
    // written from its own axis here.
    public static final SizingType<FloorPlacementSettings> TYPE = new SizingType<>(Tempad.id("floor"),
            BufCodec.of((buf, v) -> {
                buf.writeFloat(v.xOffset);
                buf.writeFloat(v.yOffset);
                buf.writeFloat(v.zOffset);
            }, buf -> new FloorPlacementSettings(buf.readFloat(), buf.readFloat(), buf.readFloat())),
            tag -> new FloorPlacementSettings(tag.getFloat("x"), tag.getFloat("y"), tag.getFloat("z")),
            (v, tag) -> {
                tag.putFloat("x", v.xOffset);
                tag.putFloat("y", v.yOffset);
                tag.putFloat("z", v.zOffset);
            });

    public static final float WIDTH = 20 / 16f;
    public static final float HEIGHT = 4 / 16f;
    public static final float DEPTH = 20 / 16f;

    public final float xOffset;
    public final float yOffset;
    public final float zOffset;
    private final EntityDimensions dimensions = EntityDimensions.fixed(WIDTH, HEIGHT);

    public FloorPlacementSettings(float xOffset, float yOffset, float zOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    public FloorPlacementSettings() {
        this(0.0f, 0.0f, 0.0f);
    }

    @Override
    public EntityDimensions getDimensions() {
        return dimensions;
    }

    @Override
    public boolean showLineAnimation() {
        return false;
    }

    @Override
    public SizingType<?> getType() {
        return TYPE;
    }

    @Override
    public float widthAtPercent(float percent) {
        if (percent < 0.5) return WIDTH * percent * 2;
        return WIDTH;
    }

    @Override
    public float heightAtPercent(float percent) {
        return HEIGHT;
    }

    @Override
    public float depthAtPercent(float percent) {
        if (percent > 0.5) return Mth.lerp((percent - 0.5f) * 2, 0.2f, DEPTH);
        return 0.2f;
    }

    @Override
    public void placeTimedoor(DoorType type, Vec3 anchor, float angle, TimedoorEntity timedoor) {
        switch (type) {
            case ENTRY -> timedoor.setPos(anchor.x + xOffset, anchor.y + yOffset, anchor.z + zOffset);
            case EXIT -> timedoor.setPos(anchor.x, anchor.y + 2.4f, anchor.z);
        }
    }

    @Override
    public boolean isInside(TimedoorEntity timedoor, Entity entity) {
        return true;
    }
}
