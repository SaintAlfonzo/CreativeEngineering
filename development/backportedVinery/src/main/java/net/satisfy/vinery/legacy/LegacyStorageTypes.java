package net.satisfy.vinery.legacy;

import net.minecraft.resources.ResourceLocation;
import net.satisfy.vinery.core.Vinery;

/** Storage renderer ids of the D2 keepers that used doapi storage in 1.3.10 (their own layouts, not 1.5.5's racks). */
public final class LegacyStorageTypes {
    public static final ResourceLocation SHELF = Vinery.identifier("legacy_shelf");
    public static final ResourceLocation NINE_BOTTLE = Vinery.identifier("legacy_nine_bottle");
    public static final ResourceLocation FOUR_BOTTLE = Vinery.identifier("legacy_four_bottle");
    public static final ResourceLocation FLOWER_BOX = Vinery.identifier("legacy_flower_box");

    private LegacyStorageTypes() {
    }
}
