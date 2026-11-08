// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/macro/MacroRegistry.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.api.macro;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class MacroRegistry {
    private static final Map<ResourceLocation, TempadMacro> MACROS = new LinkedHashMap<>();

    private MacroRegistry() {
    }

    public static void register(ResourceLocation id, TempadMacro macro) {
        MACROS.put(id, macro);
    }

    @Nullable
    public static TempadMacro get(ResourceLocation id) {
        return MACROS.get(id);
    }

    public static Set<ResourceLocation> getIds() {
        return Collections.unmodifiableSet(MACROS.keySet());
    }

    public static Map<ResourceLocation, TempadMacro> all() {
        return Collections.unmodifiableMap(MACROS);
    }
}
