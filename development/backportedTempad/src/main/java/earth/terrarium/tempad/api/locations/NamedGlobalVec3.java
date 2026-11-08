// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/locations/NamedGlobalVec3.kt. Translated to Java for Forge 1.19.2:
// MapCodec -> NBT {name (component JSON), pos [x, y, z], dimension, angle, color}, ByteCodec -> FriendlyByteBuf.
package earth.terrarium.tempad.api.locations;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.utils.CodecUtils;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record NamedGlobalVec3(Component name, Vec3 pos, ResourceKey<Level> dimension, float angle, Color color) implements TooltipComponent {
    public static final NamedGlobalVec3 NOWHERE = new NamedGlobalVec3(Component.empty(), Vec3.ZERO, Level.OVERWORLD, 0f, Color.DEFAULT);

    public Vec3 offsetLocation() {
        return offsetLocation(pos, angle + 180);
    }

    public int x() {
        return (int) pos.x;
    }

    public int y() {
        return (int) pos.y;
    }

    public int z() {
        return (int) pos.z;
    }

    public MutableComponent dimensionText() {
        return Component.translatable(Util.makeDescriptionId("dimension", dimension.location()));
    }

    public static Vec3 offsetLocation(Vec3 pos, float angle) {
        return offsetLocation(pos, angle, 1);
    }

    public static Vec3 offsetLocation(Vec3 pos, float angle, int distance) {
        float angleInRadians = (angle + 90) * Mth.DEG_TO_RAD;
        return pos.add(Mth.cos(angleInRadians) * (double) distance, 0.0, Mth.sin(angleInRadians) * (double) distance);
    }

    /** Upstream {@code Player.namedGlobalVec3}. */
    public static NamedGlobalVec3 of(Player player) {
        return new NamedGlobalVec3(Component.translatable(player.getGameProfile().getName()), player.position(),
                player.level.dimension(), player.getYRot(), Tempad.ORANGE);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", CodecUtils.saveComponent(name));
        tag.put("pos", CodecUtils.saveVec3(pos));
        tag.putString("dimension", dimension.location().toString());
        tag.putFloat("angle", angle);
        tag.put("color", color.save());
        return tag;
    }

    public static NamedGlobalVec3 load(CompoundTag tag) {
        return new NamedGlobalVec3(CodecUtils.loadComponent(tag.getString("name")), CodecUtils.loadVec3(tag.get("pos")),
                CodecUtils.loadDimension(tag.getString("dimension")), tag.getFloat("angle"), Color.load(tag.get("color")));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeComponent(name);
        CodecUtils.writeVec3(buf, pos);
        CodecUtils.writeDimension(buf, dimension);
        buf.writeFloat(angle);
        color.write(buf);
    }

    public static NamedGlobalVec3 read(FriendlyByteBuf buf) {
        return new NamedGlobalVec3(buf.readComponent(), CodecUtils.readVec3(buf), CodecUtils.readDimension(buf), buf.readFloat(), Color.read(buf));
    }
}
