// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/sizing/TimedoorPlacementSettings.kt (SizingRegistry). Translated to
// Java for Forge 1.19.2.
package earth.terrarium.tempad.api.sizing;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SizingRegistry {
    private static final Map<ResourceLocation, SizingType<?>> SIZINGS = new LinkedHashMap<>();

    private SizingRegistry() {
    }

    public static Map<ResourceLocation, SizingType<?>> sizings() {
        return Collections.unmodifiableMap(SIZINGS);
    }

    public static <T extends TimedoorPlacementSettings> void register(SizingType<T> type) {
        SIZINGS.put(type.id(), type);
    }

    public static SizingType<?> get(ResourceLocation id) {
        SizingType<?> type = SIZINGS.get(id);
        if (type == null) throw new IllegalStateException("Unknown sizing type: " + id);
        return type;
    }

    @Nullable
    public static SizingType<?> getOrNull(ResourceLocation id) {
        return SIZINGS.get(id);
    }
}
