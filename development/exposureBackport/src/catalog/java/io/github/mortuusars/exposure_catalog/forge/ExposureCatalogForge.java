package io.github.mortuusars.exposure_catalog.forge;

import io.github.mortuusars.exposure_catalog.ExposureCatalog;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

/** Forge entry point; upstream `io.github.mortuusars.exposure_catalog.neoforge.ExposureCatalogNeoForge`. */
@Mod(ExposureCatalog.ID)
public class ExposureCatalogForge {
    public ExposureCatalogForge() {
        ExposureCatalog.init();
        ExposureCatalog.LOGGER.info("Loaded {} {}", ExposureCatalog.ID,
                ModLoadingContext.get().getActiveContainer().getModInfo().getVersion());
    }
}
