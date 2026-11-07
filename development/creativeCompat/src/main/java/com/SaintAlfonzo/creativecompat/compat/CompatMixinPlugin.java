package com.SaintAlfonzo.creativecompat.compat;

import com.SaintAlfonzo.creativecompat.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * Mixin config plugin for creativecompat.mixins.json: the table form of the shared {@link GatedMixinPlugin}
 * (development/shared/mixinplugin). A mixin in {@link #NEEDS_MOD} applies when one of its mods is loaded; a mixin in
 * {@link #NEEDS_ALL} only when every one of its mods is (a target in one mod that calls code typed on another). Mixins
 * in neither table (vanilla targets whose feature does nothing without its mod: BlockItemOwnStackFillMixin,
 * StructureTemplateBotanistPotMixin, VineBlockShearMixin; the visual sinking pair CameraSinkMixin and
 * EntitySinkRenderMixin) always apply. The mixins moved from
 * patcherMod unchanged apart from their package (SRG names, remap = false, creativeengineering$ member prefixes).
 */
public class CompatMixinPlugin extends GatedMixinPlugin {

    private static final List<String> SUPPLEMENTARIES = List.of("supplementaries");
    private static final List<String> HANDCRAFTED = List.of("handcrafted");
    private static final List<String> QUARK = List.of("quark");
    private static final List<String> TRASHSLOT = List.of("trashslot");
    private static final List<String> DOMESTICATION = List.of("domesticationinnovation");
    private static final List<String> COZY = List.of("cozys_improved_cats");
    private static final List<String> ORE_EXCAVATION = List.of("oreexcavation");
    private static final List<String> BLOCKRUNNER = List.of("blockrunner");

    private static final Map<String, List<String>> NEEDS_MOD = Map.ofEntries(
            Map.entry("HandcraftedItemHoldingSaveMixin", HANDCRAFTED),
            Map.entry("HandcraftedNightstandStorageMixin", HANDCRAFTED),
            Map.entry("HandcraftedNightstandUseMixin", HANDCRAFTED),
            // Create: Dreams & Desires jetpack tooltip null-level guard (moved from Creative Common 2026-11-04)
            Map.entry("JetpackItemTooltipMixin", List.of("create_dd")),
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
            Map.entry("XaeroAtlasColourMixin", List.of("xaeroworldmap")),
            // Hats worn in the pack's Curios Face slot drawn like the head slot (curios_slots.md)
            Map.entry("SimpleHatsFaceSlotMixin", List.of("simplehats")),
            // Feather on a Stick as a cat command (cats.CatFeather); the FishingHook ones check FeatherEntity
            Map.entry("DiFeatherEntityMixin", DOMESTICATION),
            Map.entry("FishingHookFeatherPullMixin", DOMESTICATION),
            // Cozy's Improved Cats (cats.CozyCats)
            Map.entry("CozyHomeGoalFeatherMixin", COZY),
            Map.entry("CozyMessageGenderMixin", COZY),
            // Alex's Mobs giant squid flee radius from cachalot whales (CompatConfig.GiantSquid)
            Map.entry("AmGiantSquidAvoidWhalesMixin", List.of("alexsmobs")),
            // From Creative Engineering 2026-11-06: Ore Excavation undo rules (excavation/)
            Map.entry("OreExcavationBlockHistoryMixin", ORE_EXCAVATION),
            Map.entry("OreExcavationCommandUndoMixin", ORE_EXCAVATION),
            Map.entry("OreExcavationHistoryMixin", ORE_EXCAVATION),
            Map.entry("OreExcavationMiningAgentMixin", ORE_EXCAVATION),
            Map.entry("OreExcavationSchedulerMixin", ORE_EXCAVATION),
            // BlockRunner surfaces (surface/)
            Map.entry("BlockRunnerBlockStateMixin", BLOCKRUNNER),
            Map.entry("BlockRunnerSpeedConfigMixin", BLOCKRUNNER),
            // Camouflage order on Framed Blocks and Copycats+ (surface/LastInserted*)
            Map.entry("FramedDoubleCamoOrderMixin", List.of("framedblocks")),
            Map.entry("CopycatMaterialOrderMixin", List.of("copycats")),
            // Portal Gun: lift after a teleport only on floor exits (portalgun/PortalGunExitLift, plan_portal_gun.md 3)
            Map.entry("PortalGunExitLiftMixin", List.of("portalgun")),
            // interface mixins from Creative Common (JEI, tooltips, GUI titles, toasts, effect icons; 2026-11-07)
            Map.entry("SubMenuConfigScreenMixin", List.of("create")),
            Map.entry("JeiRecipeGuiLogicMixin", List.of("jei")),
            Map.entry("JeiMissingSlotsErrorAccessor", List.of("jei")),
            Map.entry("JeiRecipeLayoutTooltipMixin", List.of("jei")),
            Map.entry("JeiFluidHelperTooltipMixin", List.of("jei")),
            Map.entry("JeiFluidTankScaleMixin", List.of("jei")),
            Map.entry("JeiRecipesGuiTooltipMixin", List.of("jei")),
            Map.entry("JeiIngredientFilterAccessor", List.of("jei")),
            Map.entry("JeiIngredientFilterMixin", List.of("jei")),
            Map.entry("JeiStarterMixin", List.of("jei")),
            Map.entry("JeiPluginCallerMixin", List.of("jei")),
            Map.entry("JeiIngredientManagerMixin", List.of("jei")),
            Map.entry("LCEasyScreenHelperMixin", List.of("lightmanscurrency")),
            Map.entry("LCEasyGuiGraphicsMixin", List.of("lightmanscurrency")),
            Map.entry("IcebergTitleBreakMixin", List.of("legendarytooltips")),
            Map.entry("PrettyPipesTerminalTooltipMixin", List.of("prettypipes")),
            Map.entry("FtbTooltipListAccessor", List.of("ftblibrary")),
            Map.entry("FtbGuiHelperMixin", List.of("ftblibrary")),
            Map.entry("FtbScreenWrapperTooltipMixin", List.of("ftblibrary")),
            Map.entry("FtbMenuScreenWrapperTooltipMixin", List.of("ftblibrary")),
            Map.entry("FtbItemTaskTooltipMixin", List.of("ftbquests")),
            Map.entry("FtbQuestDescriptionTooltipMixin", List.of("ftbquests")),
            Map.entry("WaresAgreementTooltipMixin", List.of("wares")),
            Map.entry("PatchouliBookTooltipMixin", List.of("patchouli")),
            Map.entry("AeBaseScreenTooltipMixin", List.of("ae2")),
            Map.entry("AeTableRendererTooltipMixin", List.of("ae2")),
            Map.entry("AePatternEncodingTooltipMixin", List.of("ae2")),
            Map.entry("AeGuideItemTooltipMixin", List.of("ae2")),
            Map.entry("JeedEffectInfoMixin", List.of("jeed")),
            Map.entry("JeedEffectRendererMixin", List.of("jeed")));

    private static final List<String> POLYMORPH_SOPHISTICATED = List.of("polymorph", "sophisticatedcore");

    /**
     * Tested version range of the interface mods (moved with their mixins from Creative Common's CommonMixinPlugin,
     * 2026-11-07): outside it the mod's mixins are skipped with one warning instead of crashing the boot.
     */
    private static final Map<String, String> MOD_RANGE = Map.ofEntries(
            Map.entry("jei", "[11.8.1.1034,11.9)"),
            Map.entry("jeed", "[1.19.2-2.1.4,1.19.2-2.2)"),
            Map.entry("create", "[0.5.1.i,0.5.2)"),
            Map.entry("lightmanscurrency", "[1.19.2-2.2.1.3g,1.19.2-2.3)"),
            Map.entry("patchouli", "[1.19.2-77,1.19.3)"),
            Map.entry("ftblibrary", "[1902.4.1-build.236,1902.5)"),
            Map.entry("ftbquests", "[1902.5.10-build.497,1902.6)"),
            Map.entry("prettypipes", "[1.13.6,1.14)"),
            Map.entry("wares", "[1.2.9,1.3)"),
            Map.entry("ae2", "[12.9.12,12.10)"),
            Map.entry("legendarytooltips", "[1.4.0,1.5)"));

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
            Map.entry("CreateBreakerMonsterBoxMixin", List.of("create", "quark")),
            // GeckoLib item in the hotbar flushing ImmediatelyFast's HUD item batch (plan_portal_gun.md 2)
            Map.entry("GeckoItemHudBatchMixin", List.of("geckolib3", "immediatelyfast")));

    public CompatMixinPlugin() {
        super("[creativecompat]", NEEDS_MOD, MOD_RANGE);
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        List<String> all = NEEDS_ALL.get(mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1));
        if (all != null) return all.stream().allMatch(GatedMixinPlugin::isLoaded);
        return super.shouldApplyMixin(targetClassName, mixinClassName);
    }
}
