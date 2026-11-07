// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/location_handlers/AnchorPointsHandler.kt
// (AnchorPointsData and the top-level getBlockEntity) and common/registries/ModAttachments.kt (anchor_points, an overworld
// attachment). Translated to Java for Forge 1.19.2: the overworld attachment -> overworld SavedData "tempad_anchor_points"
// {<anchor uuid>: GlobalPos} (upstream codec: unboundedMap(UUID string, GlobalPos)). Always saved (like an attachment),
// since the marker blocks edit the map in place.
package earth.terrarium.tempad.common.location_handlers;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.common.block.timedoor_marker.AbstractMarkerBe;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class AnchorPointsData extends SavedData {
    public static final String NAME = "tempad_anchor_points";

    public final Map<UUID, GlobalPos> anchors = new LinkedHashMap<>();

    /** Upstream {@code anchorPoints} (ServerDataDelegate): null without a running server. */
    @Nullable
    public static AnchorPointsData get() {
        MinecraftServer server = Tempad.server();
        if (server == null) return null;
        return server.overworld().getDataStorage().computeIfAbsent(AnchorPointsData::load, AnchorPointsData::new, NAME);
    }

    /** Upstream {@code getPostions}: the anchors whose block entity is there and lets {@code accessor} in. */
    public Map<UUID, NamedGlobalVec3> getPositions(GameProfile accessor) {
        Map<UUID, NamedGlobalVec3> result = new LinkedHashMap<>();
        anchors.forEach((id, pos) -> {
            AbstractMarkerBe blockEntity = getBlockEntity(pos);
            if (blockEntity != null && blockEntity.canAccess(accessor)) result.put(id, blockEntity.getNamedGlobalVec3());
        });
        return result;
    }

    /** Upstream {@code plusAssign}. */
    public void add(AbstractMarkerBe block) {
        if (block.id == null) block.id = UUID.randomUUID();
        if (block.id != null && block.getLevel() != null) {
            anchors.put(block.id, GlobalPos.of(block.getLevel().dimension(), block.getBlockPos()));
        }
    }

    /** Upstream {@code minusAssign}. */
    public void remove(UUID locationId) {
        anchors.remove(locationId);
    }

    /** Upstream top-level {@code getBlockEntity(GlobalPos)}. */
    @Nullable
    public static AbstractMarkerBe getBlockEntity(GlobalPos pos) {
        MinecraftServer server = Tempad.server();
        ServerLevel level = server == null ? null : server.getLevel(pos.dimension());
        return level != null && level.getBlockEntity(pos.pos()) instanceof AbstractMarkerBe marker ? marker : null;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        anchors.forEach((id, pos) -> GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).result().ifPresent(it -> tag.put(id.toString(), it)));
        return tag;
    }

    public static AnchorPointsData load(CompoundTag tag) {
        AnchorPointsData data = new AnchorPointsData();
        for (String key : tag.getAllKeys()) {
            UUID id;
            try {
                id = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }
            GlobalPos.CODEC.parse(NbtOps.INSTANCE, tag.get(key)).result().ifPresent(pos -> data.anchors.put(id, pos));
        }
        return data;
    }
}
