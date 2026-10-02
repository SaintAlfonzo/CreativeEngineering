package io.github.mortuusars.exposure_catalog;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

/**
 * Loader-independent entry point, ported from upstream Exposure Catalog 1.1.2 `io.github.mortuusars.exposure_catalog.ExposureCatalog`.
 * Forge glue (the {@code @Mod} class, event wiring) lives in {@code forge/}, upstream's {@code neoforge/}.
 */
public class ExposureCatalog {
    public static final String ID = "exposure_catalog";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(ID, path);
    }
}
