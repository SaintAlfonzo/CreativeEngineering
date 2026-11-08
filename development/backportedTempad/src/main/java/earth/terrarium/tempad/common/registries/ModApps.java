// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModApps.kt. Translated to Java for Forge 1.19.2.
// Registers the apps in upstream's order.
package earth.terrarium.tempad.common.registries;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.app.AppRegistry;
import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import earth.terrarium.tempad.common.apps.KnowledgeRepositoryApp;
import earth.terrarium.tempad.common.apps.NewLocationApp;
import earth.terrarium.tempad.common.apps.PortalSetupApp;
import earth.terrarium.tempad.common.apps.SettingsApp;
import earth.terrarium.tempad.common.apps.TeleportApp;
import earth.terrarium.tempad.common.apps.TimelineApp;
import earth.terrarium.tempad.common.config.CommonConfig;
import earth.terrarium.tempad.common.config.CommonConfigCache;
import net.minecraft.resources.ResourceLocation;

public final class ModApps {
    public static final ResourceLocation TELEPORT = Tempad.id("teleport");
    public static final ResourceLocation PORTAL_SETUP = Tempad.id("portal_setup");
    public static final ResourceLocation NEW_LOCATION = Tempad.id("new_location");
    public static final ResourceLocation TIMELINE = Tempad.id("timeline");
    public static final ResourceLocation SETTINGS = Tempad.id("settings");
    public static final ResourceLocation GUIDE = ModItems.guideKey;

    private ModApps() {
    }

    public static void init() {
        AppRegistry.register(TELEPORT, (ctx, isStationary) -> !isStationary ? new TeleportApp(ctx) : null);
        AppRegistry.register(PORTAL_SETUP, (ctx, isStationary) -> isStationary ? new PortalSetupApp(ctx) : null);
        AppRegistry.register(NEW_LOCATION, (ctx, isStationary) -> {
            // Upstream: Tempad.flag !in level.enabledFeatures() || upgrades contains new_location
            UpgradeHandler upgrades = UpgradeHandler.of(ctx.getStack());
            if (CommonConfig.allowLocationSaving() && !isStationary
                    && (!CommonConfigCache.requireLocationUpgrade() || (upgrades != null && upgrades.contains(ModItems.newLocationKey)))) {
                return new NewLocationApp(ctx);
            }
            return null;
        });
        AppRegistry.register(TIMELINE, (ctx, isStationary) -> !ModComponents.isTwisterEquipped(ctx.getStack()) ? null : new TimelineApp(ctx, isStationary));
        AppRegistry.register(SETTINGS, SettingsApp::new);
        AppRegistry.register(GUIDE, (ctx, isStationary) -> {
            UpgradeHandler upgrades = UpgradeHandler.of(ctx.getStack());
            return upgrades != null && upgrades.contains(ModItems.guideKey) ? new KnowledgeRepositoryApp(ctx, isStationary) : null;
        });
    }
}
