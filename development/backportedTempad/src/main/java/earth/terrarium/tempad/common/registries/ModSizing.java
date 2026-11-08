// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModSizing.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.registries;

import earth.terrarium.tempad.api.sizing.DynamicAngledPlacement;
import earth.terrarium.tempad.api.sizing.FloorPlacementSettings;
import earth.terrarium.tempad.api.sizing.SizingRegistry;
import earth.terrarium.tempad.api.sizing.VerticalPlacementSettings;

public final class ModSizing {
    private ModSizing() {
    }

    public static void init() {
        SizingRegistry.register(DynamicAngledPlacement.TYPE);
        SizingRegistry.register(FloorPlacementSettings.TYPE);
        SizingRegistry.register(VerticalPlacementSettings.TYPE);
    }
}
