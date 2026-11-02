package com.SaintAlfonzo.creativecompat.compat;

import com.SaintAlfonzo.creativecompat.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * Mixin config plugin for creativecompat.mixins.json: the table form of the shared {@link GatedMixinPlugin}
 * (development/shared/mixinplugin). A mixin in {@link #NEEDS_MOD} applies when one of its mods is loaded; a mixin in
 * {@link #NEEDS_ALL} only when every one of its mods is (a target in one mod that calls code typed on another). Mixins
 * in neither table (vanilla targets whose feature does nothing without its mod: BlockItemOwnStackFillMixin,
 * StructureTemplateBotanistPotMixin, VineBlockShearMixin, CampfireRainMixin) always apply. The mixins moved from
 * patcherMod unchanged apart from their package (SRG names, remap = false, creativeengineering$ member prefixes).
 */
public class CompatMixinPlugin extends GatedMixinPlugin {

    private static final List<String> SUPPLEMENTARIES = List.of("supplementaries");
    private static final List<String> HANDCRAFTED = List.of("handcrafted");
    private static final List<String> QUARK = List.of("quark");
    private static final List<String> TRASHSLOT = List.of("trashslot");
    private static final List<String> DOMESTICATION = List.of("domesticationinnovation");
    private static final List<String> COZY = List.of("cozys_improved_cats");

    private static final Map<String, List<String>> NEEDS_MOD = Map.ofEntries(
            Map.entry("HandcraftedItemHoldingSaveMixin", HANDCRAFTED),
            Map.entry("HandcraftedNightstandStorageMixin", HANDCRAFTED),
            Map.entry("HandcraftedNightstandUseMixin", HANDCRAFTED),
            Map.entry("SupplementariesDyeBlocksMixin", SUPPLEMENTARIES),
            Map.entry("SupplementariesJarTileMixin", SUPPLEMENTARIES),
            Map.entry("SupplementariesJarItemTooltipMixin", SUPPLEMENTARIES),
            Map.entry("SupplementariesSackItemMixin", SUPPLEMENTARIES),
            Map.entry("SuppFlowerBoxPlantsMixin", SUPPLEMENTARIES),
            Map.entry("SupplementariesJarRendererMixin", SUPPLEMENTARIES),
            Map.entry("SupplementariesJarItemRendererMixin", SUPPLEMENTARIES),
            // Moonlight's ItemDisplayTile, used by the jar mixin
            Map.entry("ItemDisplayTileAccessor", List.of("moonlight")),
            Map.entry("ItemPhysicServerMixin", List.of("itemphysic")),
            Map.entry("BotanistWorkbenchPotMixin", List.of("beautify")),
            Map.entry("FramedFlowerPotUnframedMixin", List.of("framedblocks")),
            Map.entry("SiltPotSoilMixin", List.of("twigs")),
            Map.entry("QuarkSortingHandlerMixin", QUARK),
            Map.entry("MonsterBoxBlockEntityMixin", QUARK),
            Map.entry("LeafPileSlowMixin", List.of("immersive_weathering")),
            Map.entry("AnotherFurniturePlanterRenderMixin", List.of("another_furniture")),
            Map.entry("McaCombEyeColourMixin", List.of("mca")),
            Map.entry("BedspreadsBannerPatternMixin", List.of("bedspreads")),
            Map.entry("CustomizableElytraBannerPatternMixin", List.of("customizableelytra")),
            Map.entry("TrashSlotGuiHandlerMixin", TRASHSLOT),
            Map.entry("TrashSlotComponentMixin", TRASHSLOT),
            Map.entry("TrashSlotSaveStateAccessor", TRASHSLOT),
            Map.entry("DhCloudHeightMixin", List.of("distanthorizons")),
            // Feather on a Stick as a cat command (cats.CatFeather); the FishingHook ones check FeatherEntity
            Map.entry("DiFeatherEntityMixin", DOMESTICATION),
            Map.entry("FishingHookFeatherPullMixin", DOMESTICATION),
            // Cozy's Improved Cats (cats.CozyCats)
            Map.entry("CozyHomeGoalFeatherMixin", COZY),
            Map.entry("CozyMessageGenderMixin", COZY));

    private static final List<String> POLYMORPH_SOPHISTICATED = List.of("polymorph", "sophisticatedcore");

    /** Every listed mod must be loaded. */
    private static final Map<String, List<String>> NEEDS_ALL = Map.ofEntries(
            // Supplementaries' Breezy integration class, only used when Breezy is there too
            Map.entry("SupplementariesBreezyCompatMixin", List.of("supplementaries", "breezy")),
            // Sophisticated targets calling SophisticatedPolymorphCompat (typed on sophisticatedcore) for Polymorph
            Map.entry("SophisticatedCraftingPolymorphMixin", POLYMORPH_SOPHISTICATED),
            Map.entry("SophisticatedUpgradeOpenMixin", POLYMORPH_SOPHISTICATED),
            // Polymorph target calling SophisticatedPolymorphCompat (instanceof StorageContainerMenuBase)
            Map.entry("PolymorphRecipeSelectionPacketMixin", POLYMORPH_SOPHISTICATED),
            Map.entry("IECraftingTablePolymorphMixin", List.of("polymorph", "immersiveengineering")),
            // Skips DI's three-mode click on Cozy cats only; nothing to do without both
            Map.entry("CatCozyCommandMixin", List.of("domesticationinnovation", "cozys_improved_cats")),
            Map.entry("PrettyPipesCraftingTerminalPolymorphMixin", List.of("polymorph", "prettypipes")),
            // Create's breaker target, Quark's monster box
            Map.entry("CreateBreakerMonsterBoxMixin", List.of("create", "quark")));

    public CompatMixinPlugin() {
        super("[creativecompat]", NEEDS_MOD);
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        List<String> all = NEEDS_ALL.get(mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1));
        if (all != null) return all.stream().allMatch(GatedMixinPlugin::isLoaded);
        return super.shouldApplyMixin(targetClassName, mixinClassName);
    }
}
