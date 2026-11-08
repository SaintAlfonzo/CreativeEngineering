// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/locations/TempadLocations.kt (LocationHandler). Translated to Java
// for Forge 1.19.2.
package earth.terrarium.tempad.api.locations;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public interface LocationHandler {
    Map<UUID, NamedGlobalVec3> getLocations();

    /** Upstream {@code minusAssign}. */
    void remove(UUID locationId);

    @Nullable
    default NamedGlobalVec3 get(UUID locationId) {
        return getLocations().get(locationId);
    }

    @Nullable
    LocationGetter getSerializable(UUID locationId);
}
