// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/tva_device/impl/RudimentaryUpgradeHandler.kt. Translated to Java
// for Forge 1.19.2. (Its users, the timedoor projector item and block, arrive in phase 2.)
package earth.terrarium.tempad.api.tva_device.impl;

import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class RudimentaryUpgradeHandler implements UpgradeHandler {
    public static final RudimentaryUpgradeHandler INSTANCE = new RudimentaryUpgradeHandler();

    private RudimentaryUpgradeHandler() {
    }

    @Override
    public List<ResourceLocation> getInstalledUpgrades() {
        return List.of();
    }

    @Override
    public boolean contains(ResourceLocation upgrade) {
        return false;
    }

    @Override
    public void add(ResourceLocation upgrade) {
    }

    @Override
    public void remove(ResourceLocation upgrade) {
    }

    @Override
    public boolean willAccept(ResourceLocation upgrade) {
        return false;
    }

    @Override
    public boolean isRemovable(ResourceLocation upgrade) {
        return false;
    }
}
