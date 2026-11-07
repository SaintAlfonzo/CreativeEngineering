// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModItems.kt. Translated to Java for Forge 1.19.2:
// ResourcefulRegistry -> DeferredRegister; the creative tab -> a 1.19.2 CreativeModeTab (items join it through their
// properties, so the tab lists them in registration order, as upstream's BuildCreativeModeTabContentsEvent loop).
// Items are registered in upstream's order (TP-D7), every upstream item since phase 4.
package earth.terrarium.tempad.common.registries;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.config.CommonConfig;
import earth.terrarium.tempad.common.config.CommonConfigCache;
import earth.terrarium.tempad.common.items.CapacitorItem;
import earth.terrarium.tempad.common.items.ChronometerItem;
import earth.terrarium.tempad.common.items.CreativeChronometerItem;
import earth.terrarium.tempad.common.items.HandbookItem;
import earth.terrarium.tempad.common.items.LocationBroadcasterItem;
import earth.terrarium.tempad.common.items.LocationCardItem;
import earth.terrarium.tempad.common.items.MetronomeItem;
import earth.terrarium.tempad.common.items.RudimentaryTempadItem;
import earth.terrarium.tempad.common.items.ScreeningDeviceItem;
import earth.terrarium.tempad.common.items.SpatialAnchorItem;
import earth.terrarium.tempad.common.items.TempadItem;
import earth.terrarium.tempad.common.items.TimeTwisterItem;
import earth.terrarium.tempad.common.items.WorkstationItem;
import earth.terrarium.tempad.common.items.WalletItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, Tempad.MOD_ID);

    /** Upstream tab {@code tempad:main}, title {@code category.tempad}, icon a Tempad with the time twister attached. */
    public static final CreativeModeTab TAB = new CreativeModeTab(Tempad.MOD_ID + ".main") {
        @Override
        public ItemStack makeIcon() {
            ItemStack stack = new ItemStack(tempad.get());
            ModComponents.setTwisterEquipped(stack, true);
            return stack;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("category.tempad");
        }
    };

    private static Item.Properties props() {
        return new Item.Properties().tab(TAB);
    }

    // Base
    public static final RegistryObject<LocationCardItem> locationCard = REGISTRY.register("location_card", () -> new LocationCardItem(props()));
    public static final RegistryObject<WalletItem> cardWallet = REGISTRY.register("card_wallet", () -> new WalletItem(props()));
    public static final RegistryObject<Item> timeSteel = REGISTRY.register("time_steel", () -> new Item(props()));
    public static final RegistryObject<HandbookItem> knowledgeProjector = REGISTRY.register("knowledge_projector", () -> new HandbookItem(props()));
    /**
     * Upstream: {@code requiredFeatures(Tempad.flag)}. 1.19.2 has no feature flags, so the item always exists; it is shown
     * in the creative tab only while {@code require_location_upgrade} is on (its recipes are conditioned on the same
     * setting by the translator).
     */
    public static final RegistryObject<Item> newLocationUpgrade = REGISTRY.register("new_location_upgrade", () -> new Item(props()) {
        @Override
        public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
            if (CommonConfigCache.requireLocationUpgrade()) super.fillItemCategory(tab, items);
        }
    });
    public static final ResourceLocation newLocationKey = Tempad.id("new_location");
    public static final RegistryObject<Item> playerTeleportUpgrade = REGISTRY.register("player_teleport_upgrade", () -> new Item(props()));
    public static final ResourceLocation playerKey = Tempad.id("player_teleport");
    public static final RegistryObject<Item> guideUpgrade = REGISTRY.register("knowledge_repository_upgrade", () -> new Item(props()));
    public static final ResourceLocation guideKey = Tempad.id("knowledge_repository");

    // Rudi Tier
    public static final RegistryObject<RudimentaryTempadItem> timedoorProjector = REGISTRY.register("timedoor_projector",
            () -> new RudimentaryTempadItem(props()));
    public static final RegistryObject<SpatialAnchorItem> timedoorMarker = REGISTRY.register("timedoor_marker",
            () -> new SpatialAnchorItem(ModBlocks.timedoorMarker.get(), props()));
    public static final RegistryObject<CapacitorItem> chrononCell = REGISTRY.register("chronon_cell", () -> new CapacitorItem(props()));
    public static final RegistryObject<ChronometerItem> chrononGenerator = REGISTRY.register("chronon_generator",
            () -> new ChronometerItem(props(), CommonConfig.ChrononGenerator::generationRate, CommonConfig.ChrononGenerator::generationAmount));
    public static final RegistryObject<LocationBroadcasterItem> locationBroadcaster = REGISTRY.register("location_broadcaster",
            () -> new LocationBroadcasterItem(props()));

    // TVA Tier
    public static final RegistryObject<TempadItem> tempad = REGISTRY.register("tempad", () -> new TempadItem(props()));
    public static final RegistryObject<SpatialAnchorItem> chronomark = REGISTRY.register("chronomark",
            () -> new SpatialAnchorItem(ModBlocks.chronomark.get(), props()));
    public static final RegistryObject<CapacitorItem> chrononBattery = REGISTRY.register("chronon_battery", () -> new CapacitorItem(props()));
    public static final RegistryObject<ChronometerItem> chronometer = REGISTRY.register("chronometer",
            () -> new ChronometerItem(props(), CommonConfig.Chronometer::generationRate, CommonConfig.Chronometer::generationAmount));
    public static final RegistryObject<TimeTwisterItem> timeTwister = REGISTRY.register("time_twister", () -> new TimeTwisterItem(props()));

    public static final RegistryObject<WorkstationItem> workstation = REGISTRY.register("workstation", () -> new WorkstationItem(props()));
    public static final RegistryObject<MetronomeItem> metronome = REGISTRY.register("metronome", () -> new MetronomeItem(props()));

    public static final RegistryObject<ScreeningDeviceItem> screeningDevice = REGISTRY.register("screening_device",
            () -> new ScreeningDeviceItem(props()));

    // Creative
    public static final RegistryObject<CreativeChronometerItem> creativeChronometer = REGISTRY.register("creative_chronometer",
            () -> new CreativeChronometerItem(props()));

    private ModItems() {
    }
}
