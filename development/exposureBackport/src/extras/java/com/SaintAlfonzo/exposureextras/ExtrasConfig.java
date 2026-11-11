package com.SaintAlfonzo.exposureextras;

import net.minecraftforge.common.ForgeConfigSpec;

/** {@code config/exposureextras-common.toml}. */
public final class ExtrasConfig {

    public static final ForgeConfigSpec SPEC;
    public static final CameraBag CAMERA_BAG;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CAMERA_BAG = new CameraBag(builder);
        SPEC = builder.build();
    }

    private ExtrasConfig() {}

    /**
     * Camera bags ({@code camerabag/} package): slots per tier. Read each time a bag is opened, so a
     * change applies from the next opening; the server's value wins (it is sent with the menu).
     */
    public static final class CameraBag {
        public final ForgeConfigSpec.IntValue leatherSlots;
        public final ForgeConfigSpec.IntValue ironSlots;
        public final ForgeConfigSpec.IntValue netheriteSlots;

        private CameraBag(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Camera bags hold anything tagged #exposureextras:camera_equipment, in any slot, at the",
                    "item's normal stack size. Slots per tier (1-27). Lowering a value moves the items in the",
                    "removed slots into the player's inventory (or drops them) the next time that bag is opened."
            ).push("camera_bag");

            leatherSlots = b.defineInRange("leatherSlots", 5, 1, 27);
            ironSlots = b.defineInRange("ironSlots", 9, 1, 27);
            netheriteSlots = b.defineInRange("netheriteSlots", 14, 1, 27);

            b.pop(); // camera_bag
        }
    }
}
