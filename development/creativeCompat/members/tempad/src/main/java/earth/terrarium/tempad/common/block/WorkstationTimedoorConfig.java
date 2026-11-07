// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/WorkstationTimedoorConfig.kt. Translated to Java for
// Forge 1.19.2. Upstream is an unfinished stub that nothing constructs or registers: every method is Kotlin's TODO(),
// kept here as UnsupportedOperationException("Not yet implemented").
package earth.terrarium.tempad.common.block;

import earth.terrarium.tempad.api.sizing.DoorType;
import earth.terrarium.tempad.api.sizing.SizingType;
import earth.terrarium.tempad.api.sizing.TimedoorPlacementSettings;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;

public class WorkstationTimedoorConfig implements TimedoorPlacementSettings {
    private final EntityDimensions dimensions;
    private final boolean showLineAnimation;
    private final SizingType<?> type;

    public WorkstationTimedoorConfig(EntityDimensions dimensions, boolean showLineAnimation, SizingType<?> type) {
        this.dimensions = dimensions;
        this.showLineAnimation = showLineAnimation;
        this.type = type;
    }

    @Override
    public EntityDimensions getDimensions() {
        return dimensions;
    }

    @Override
    public boolean showLineAnimation() {
        return showLineAnimation;
    }

    @Override
    public SizingType<?> getType() {
        return type;
    }

    @Override
    public float widthAtPercent(float percent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public float heightAtPercent(float percent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public float depthAtPercent(float percent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void placeTimedoor(DoorType type, Vec3 anchor, float angle, TimedoorEntity timedoor) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isInside(TimedoorEntity timedoor, Entity entity) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
