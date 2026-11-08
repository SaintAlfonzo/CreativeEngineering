// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/utils/CodecUtils.kt and
// parts of Extensions.kt. Translated to Java for Forge 1.19.2: ByteCodecs -> FriendlyByteBuf, Codecs -> NBT helpers.
package earth.terrarium.tempad.common.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class CodecUtils {
    private CodecUtils() {
    }

    // ---- network (upstream ByteCodecs) ----

    public static void writeVec3(FriendlyByteBuf buf, Vec3 vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    public static Vec3 readVec3(FriendlyByteBuf buf) {
        return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void writeDimension(FriendlyByteBuf buf, ResourceKey<Level> dimension) {
        buf.writeResourceLocation(dimension.location());
    }

    public static ResourceKey<Level> readDimension(FriendlyByteBuf buf) {
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());
    }

    public static void writeGameProfile(FriendlyByteBuf buf, GameProfile profile) {
        buf.writeUUID(profile.getId());
        buf.writeUtf(profile.getName());
    }

    public static GameProfile readGameProfile(FriendlyByteBuf buf) {
        return new GameProfile(buf.readUUID(), buf.readUtf());
    }

    // ---- NBT (upstream Codecs) ----

    public static ListTag saveVec3(Vec3 vec) {
        ListTag list = new ListTag();
        list.add(DoubleTag.valueOf(vec.x));
        list.add(DoubleTag.valueOf(vec.y));
        list.add(DoubleTag.valueOf(vec.z));
        return list;
    }

    public static Vec3 loadVec3(Tag tag) {
        if (tag instanceof ListTag list && list.size() == 3) {
            return new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2));
        }
        return Vec3.ZERO;
    }

    public static ResourceKey<Level> loadDimension(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, location == null ? Level.OVERWORLD.location() : location);
    }

    /** Upstream GAME_PROFILE_CODEC: {id: int[4], name: string}. */
    public static CompoundTag saveGameProfile(GameProfile profile) {
        CompoundTag tag = new CompoundTag();
        tag.put("id", NbtUtils.createUUID(profile.getId()));
        tag.putString("name", profile.getName());
        return tag;
    }

    public static GameProfile loadGameProfile(CompoundTag tag) {
        UUID id = tag.contains("id") ? NbtUtils.loadUUID(tag.get("id")) : null;
        return new GameProfile(id, tag.getString("name"));
    }

    public static String saveComponent(Component component) {
        return Component.Serializer.toJson(component);
    }

    public static Component loadComponent(String json) {
        try {
            Component component = Component.Serializer.fromJson(json);
            return component == null ? Component.empty() : component;
        } catch (Exception e) {
            return Component.literal(json);
        }
    }

    public static Tag saveUUID(UUID id) {
        return NbtUtils.createUUID(id);
    }

    public static UUID loadUUID(Tag tag) {
        return NbtUtils.loadUUID(tag);
    }

    /** Upstream UUIDUtil.CODEC users get the int-array form; kept for symmetry with {@link UUIDUtil}. */
    public static UUID uuidOrNil(Tag tag) {
        try {
            return tag == null ? net.minecraft.Util.NIL_UUID : NbtUtils.loadUUID(tag);
        } catch (IllegalArgumentException e) {
            return net.minecraft.Util.NIL_UUID;
        }
    }
}
