// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/location_handlers/DefaultLocationHandler.kt. Translated to Java
// for Forge 1.19.2.
package earth.terrarium.tempad.common.location_handlers;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.DirectLocation;
import earth.terrarium.tempad.api.locations.LocationGetter;
import earth.terrarium.tempad.api.locations.LocationHandler;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class DefaultLocationHandler implements LocationHandler {
    public static final ResourceLocation ID = Tempad.id("default");

    public final GameProfile gameProfile;

    public DefaultLocationHandler(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    @Override
    public Map<UUID, NamedGlobalVec3> getLocations() {
        PlayerPointsData points = PlayerPointsData.get();
        return points == null ? new LinkedHashMap<>() : points.get(gameProfile.getId());
    }

    @Override
    public void remove(UUID locationId) {
        getLocations().remove(locationId);
    }

    /** Upstream {@code plusAssign}. */
    public void add(NamedGlobalVec3 location) {
        getLocations().put(UUID.randomUUID(), location);
    }

    @Nullable
    @Override
    public LocationGetter getSerializable(UUID locationId) {
        NamedGlobalVec3 location = get(locationId);
        return location == null ? null : new DirectLocation(location);
    }
}
