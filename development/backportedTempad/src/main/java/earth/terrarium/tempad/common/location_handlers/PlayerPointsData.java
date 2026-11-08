// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/location_handlers/PlayerHandler.kt (PlayerPointsData) and
// common/registries/ModAttachments.kt (player_points, an overworld attachment). Translated to Java for Forge 1.19.2:
// the overworld attachment -> overworld SavedData "tempad_player_points" {<player uuid>: {<location uuid>: location}}.
// Always saved (like an attachment), since the handlers edit the maps in place.
package earth.terrarium.tempad.common.location_handlers;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerPointsData extends SavedData {
    public static final String NAME = "tempad_player_points";

    public final Map<UUID, Map<UUID, NamedGlobalVec3>> data = new LinkedHashMap<>();

    public Map<UUID, NamedGlobalVec3> get(UUID playerId) {
        return data.computeIfAbsent(playerId, id -> new LinkedHashMap<>());
    }

    /** Upstream {@code playerPoints} (ServerDataDelegate): null without a running server. */
    @Nullable
    public static PlayerPointsData get() {
        var server = Tempad.server();
        if (server == null) return null;
        return server.overworld().getDataStorage().computeIfAbsent(PlayerPointsData::load, PlayerPointsData::new, NAME);
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        data.forEach((player, locations) -> {
            CompoundTag playerTag = new CompoundTag();
            locations.forEach((id, location) -> playerTag.put(id.toString(), location.save()));
            tag.put(player.toString(), playerTag);
        });
        return tag;
    }

    public static PlayerPointsData load(CompoundTag tag) {
        PlayerPointsData points = new PlayerPointsData();
        for (String player : tag.getAllKeys()) {
            UUID playerId;
            try {
                playerId = UUID.fromString(player);
            } catch (IllegalArgumentException e) {
                continue;
            }
            CompoundTag playerTag = tag.getCompound(player);
            Map<UUID, NamedGlobalVec3> locations = points.get(playerId);
            for (String id : playerTag.getAllKeys()) {
                try {
                    locations.put(UUID.fromString(id), NamedGlobalVec3.load(playerTag.getCompound(id)));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return points;
    }
}
