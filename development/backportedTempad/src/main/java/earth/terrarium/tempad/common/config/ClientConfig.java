// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/java/earth/terrarium/tempad/common/config/ClientConfig.java.
// Translated for Forge 1.19.2 (TP-D4): Resourceful Config -> ForgeConfigSpec, file tempad-client.toml, same key and
// default. Registered as a CLIENT config (loaded only on the physical client).
package earth.terrarium.tempad.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.ConfigValue<String> SORTING_MODE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        SORTING_MODE = b.comment("The sorting mode for the Tempad screen. Options: Dimension, Alphabetical, Type")
                .define("sorting_mode", "Dimension");
        SPEC = b.build();
    }

    private ClientConfig() {
    }

    public static String sortingMode() {
        return SORTING_MODE.get();
    }

    public static void setSortingMode(String mode) {
        SORTING_MODE.set(mode);
        SORTING_MODE.save();
    }
}
