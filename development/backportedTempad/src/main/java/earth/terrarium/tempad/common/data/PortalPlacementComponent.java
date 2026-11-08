// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/data/PortalPlacementComponent.kt. Translated to Java for
// Forge 1.19.2: Codec -> NBT with the upstream field names.
package earth.terrarium.tempad.common.data;

import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public record PortalPlacementComponent(float leftRight, float upDown, float forwardBack, int angle, boolean isUpright) {
    public static final PortalPlacementComponent DEFAULT = new PortalPlacementComponent(-2.5f, 0f, 0f, 0, true);

    public Vector3f calcOffset(Direction dir) {
        float x = switch (dir) {
            case NORTH -> leftRight;
            case SOUTH -> -leftRight;
            case WEST -> -forwardBack;
            case EAST -> forwardBack;
            default -> throw new IllegalStateException("Unsupported direction " + dir);
        };
        float z = switch (dir) {
            case NORTH -> -forwardBack;
            case SOUTH -> forwardBack;
            case WEST -> -leftRight;
            case EAST -> leftRight;
            default -> throw new IllegalStateException("Unsupported direction " + dir);
        };
        return new Vector3f(x, upDown, z);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("leftRight", leftRight);
        tag.putFloat("upDown", upDown);
        tag.putFloat("forwardBack", forwardBack);
        tag.putInt("angle", angle);
        tag.putBoolean("isUpright", isUpright);
        return tag;
    }

    public static PortalPlacementComponent load(CompoundTag tag) {
        return new PortalPlacementComponent(tag.getFloat("leftRight"), tag.getFloat("upDown"), tag.getFloat("forwardBack"),
                tag.getInt("angle"), tag.getBoolean("isUpright"));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(leftRight);
        buf.writeFloat(upDown);
        buf.writeFloat(forwardBack);
        buf.writeInt(angle);
        buf.writeBoolean(isUpright);
    }

    public static PortalPlacementComponent read(FriendlyByteBuf buf) {
        return new PortalPlacementComponent(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readBoolean());
    }
}
