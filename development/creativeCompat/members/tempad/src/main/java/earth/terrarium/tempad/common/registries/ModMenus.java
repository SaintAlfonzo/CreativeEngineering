// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModMenus.kt. Translated to Java for Forge 1.19.2:
// MenuContentHelper.create -> IForgeMenuType.create, the app content read from the open-screen buffer.
// teleport, settings, new_location, timeline, wallet, portal_setup, guide, metronome (upstream order).
package earth.terrarium.tempad.common.registries;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.apps.BasicAppContent;
import earth.terrarium.tempad.common.apps.NewLocationAppData;
import earth.terrarium.tempad.common.apps.SettingsData;
import earth.terrarium.tempad.common.apps.TeleportData;
import earth.terrarium.tempad.common.apps.TimelineData;
import earth.terrarium.tempad.common.menu.AbstractTempadMenu;
import earth.terrarium.tempad.common.menu.MetronomeMenu;
import earth.terrarium.tempad.common.menu.MetronomeMenuData;
import earth.terrarium.tempad.common.menu.WalletMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import earth.terrarium.tempad.common.apps.PortalSetupData;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Tempad.MOD_ID);

    public static class TeleportMenu extends AbstractTempadMenu<TeleportData> {
        public TeleportMenu(int id, Inventory inv, TeleportData data) {
            super(id, inv, teleport.get(), data);
        }
    }

    public static final RegistryObject<MenuType<TeleportMenu>> teleport = REGISTRY.register("teleport",
            () -> IForgeMenuType.create((id, inv, buf) -> new TeleportMenu(id, inv, TeleportData.decode(buf))));

    public static class SettingsMenu extends AbstractTempadMenu<SettingsData> {
        public SettingsMenu(int id, Inventory inv, SettingsData data) {
            super(id, inv, settings.get(), data);
        }
    }

    public static final RegistryObject<MenuType<SettingsMenu>> settings = REGISTRY.register("settings",
            () -> IForgeMenuType.create((id, inv, buf) -> new SettingsMenu(id, inv, SettingsData.decode(buf))));

    public static class NewLocationMenu extends AbstractTempadMenu<NewLocationAppData> {
        public NewLocationMenu(int id, Inventory inv, NewLocationAppData data) {
            super(id, inv, newLocation.get(), data);
        }
    }

    public static final RegistryObject<MenuType<NewLocationMenu>> newLocation = REGISTRY.register("new_location",
            () -> IForgeMenuType.create((id, inv, buf) -> new NewLocationMenu(id, inv, NewLocationAppData.decode(buf))));

    public static class TimelineMenu extends AbstractTempadMenu<TimelineData> {
        public TimelineMenu(int id, Inventory inv, TimelineData data) {
            super(id, inv, timeline.get(), data);
        }
    }

    public static final RegistryObject<MenuType<TimelineMenu>> timeline = REGISTRY.register("timeline",
            () -> IForgeMenuType.create((id, inv, buf) -> new TimelineMenu(id, inv, TimelineData.decode(buf))));

    /** Upstream {@code MenuType(::WalletMenu, FeatureFlags.VANILLA_SET)}: no open data. */
    public static final RegistryObject<MenuType<WalletMenu>> wallet = REGISTRY.register("wallet",
            () -> IForgeMenuType.create((id, inv, buf) -> new WalletMenu(id, inv)));

    public static class PortalSetupMenu extends AbstractTempadMenu<PortalSetupData> {
        public PortalSetupMenu(int id, Inventory inv, PortalSetupData data) {
            super(id, inv, portalSetup.get(), data);
        }
    }

    public static final RegistryObject<MenuType<PortalSetupMenu>> portalSetup = REGISTRY.register("portal_setup",
            () -> IForgeMenuType.create((id, inv, buf) -> new PortalSetupMenu(id, inv, PortalSetupData.decode(buf))));

    public static class KnowledgeMenu extends AbstractTempadMenu<BasicAppContent> {
        public KnowledgeMenu(int id, Inventory inv, BasicAppContent data) {
            super(id, inv, guide.get(), data);
        }
    }

    public static final RegistryObject<MenuType<KnowledgeMenu>> guide = REGISTRY.register("guide",
            () -> IForgeMenuType.create((id, inv, buf) -> new KnowledgeMenu(id, inv, BasicAppContent.decode(buf))));

    /** Upstream {@code MenuContentHelper.create(::MetronomeMenu, ...)}: Optional content (absent without an owner). */
    public static final RegistryObject<MenuType<MetronomeMenu>> metronome = REGISTRY.register("metronome",
            () -> IForgeMenuType.create((id, inv, buf) -> new MetronomeMenu(id, inv, buf.readOptional(MetronomeMenuData::decode))));

    private ModMenus() {
    }
}
