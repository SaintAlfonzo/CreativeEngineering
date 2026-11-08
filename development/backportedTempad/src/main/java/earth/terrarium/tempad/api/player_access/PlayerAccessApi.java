// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/player_access/PlayerAccess.kt (PlayerAccessApi). Translated to Java
// for Forge 1.19.2.
package earth.terrarium.tempad.api.player_access;

import earth.terrarium.tempad.Tempad;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PlayerAccessApi {
    public static final ResourceLocation NO_ACCESS = Tempad.id("private");
    private static final Map<ResourceLocation, PlayerAccess> VISIBILITY = new LinkedHashMap<>();

    private PlayerAccessApi() {
    }

    public static Map<ResourceLocation, PlayerAccess> visibility() {
        return Collections.unmodifiableMap(VISIBILITY);
    }

    public static List<ResourceLocation> ids() {
        List<ResourceLocation> ids = new ArrayList<>(VISIBILITY.keySet());
        Collections.sort(ids);
        return ids;
    }

    public static void register(ResourceLocation id, PlayerAccess visibility) {
        VISIBILITY.put(id, visibility);
    }

    @Nullable
    public static PlayerAccess get(ResourceLocation id) {
        return VISIBILITY.get(id);
    }
}
