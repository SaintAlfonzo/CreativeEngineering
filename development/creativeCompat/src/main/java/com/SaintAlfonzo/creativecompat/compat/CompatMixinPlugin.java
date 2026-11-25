package com.SaintAlfonzo.creativecompat.compat;

import com.SaintAlfonzo.creativecompat.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mixin config plugin for creativecompat.mixins.json: the table form of the shared {@link GatedMixinPlugin}
 * (development/shared/mixinplugin). A mixin in {@link #NEEDS_MOD} applies when one of its mods is loaded; a mixin in
 * {@link #NEEDS_ALL} only when every one of its mods is (a target in one mod that calls code typed on another). Mixins
 * in neither table change vanilla behavior with no mod to gate them: each has an enable key in the {@code [mixins]}
 * table of creativecompat-common.toml ({@link MixinSwitches}, default on, read here as text at mixin time). The mixins
 * use SRG names, remap = false and creativeengineering$ member prefixes.
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
	private static final List<String> IMM_PTL = List.of("imm_ptl_core");

	private static final Map<String, List<String>> NEEDS_MOD = Map.ofEntries(
			Map.entry("HandcraftedItemHoldingSaveMixin", HANDCRAFTED),
			Map.entry("HandcraftedNightstandStorageMixin", HANDCRAFTED),
			Map.entry("HandcraftedNightstandUseMixin", HANDCRAFTED),
			// Create: Dreams & Desires jetpack tooltip null-level guard
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
			// Nether / End crops grow only in their dimension (BotanyPotDimensionMixin, botany_pots.md 3)
			Map.entry("BotanyPotDimensionMixin", List.of("botanypots")),
			Map.entry("FramedFlowerPotUnframedMixin", List.of("framedblocks")),
			Map.entry("SiltPotSoilMixin", List.of("twigs")),
			// falling and stacking layers, bamboo leaf drops (surface/FallingLayers, BambooLeafDrops; G7.5, G7.9)
			Map.entry("TwigsBambooLeavesMixin", List.of("twigs")),
			Map.entry("BambooLeafDropMixin", List.of("twigs")),
			Map.entry("IeSawdustFallMixin", List.of("immersiveengineering")),
			Map.entry("SuppFodderFallMixin", SUPPLEMENTARIES),
			// surface layers stand on their visible top (surface/SurfaceLayers)
			Map.entry("IwLayerCollisionMixin", List.of("immersive_weathering")),
			Map.entry("SuppAshCollisionMixin", SUPPLEMENTARIES),
			Map.entry("IeSawdustCollisionMixin", List.of("immersiveengineering")),
			// layer rules: replacing, full fall damage, 9th layer to the full block (surface/SurfaceLayers)
			Map.entry("IwLayerRulesMixin", List.of("immersive_weathering")),
			Map.entry("IwSandLayerFallMixin", List.of("immersive_weathering")),
			Map.entry("IwLeafPileReplaceMixin", List.of("immersive_weathering")),
			Map.entry("SuppAshRulesMixin", SUPPLEMENTARIES),
			// MCA villagers avoid deep leaf piles (surface/LeafThorns, G7.14)
			Map.entry("McaLeafPilePathMixin", List.of("mca")),
			Map.entry("QuarkSortingHandlerMixin", QUARK),
			Map.entry("MonsterBoxBlockEntityMixin", QUARK),
			// old placed paintings move to the creativecompat painting ids (namespace_move.md D-PAINT)
			Map.entry("ImmersivePaintingMotiveAliasMixin", List.of("immersive_paintings")),
			Map.entry("AnotherFurniturePlanterRenderMixin", List.of("another_furniture")),
			Map.entry("McaCombEyeColourMixin", List.of("mca")),
			Map.entry("BedspreadsBannerPatternMixin", List.of("bedspreads")),
			Map.entry("CustomizableElytraBannerPatternMixin", List.of("customizableelytra")),
			Map.entry("TrashSlotGuiHandlerMixin", TRASHSLOT),
			Map.entry("TrashSlotComponentMixin", TRASHSLOT),
			Map.entry("TrashSlotSaveStateAccessor", TRASHSLOT),
			// Item Trash Can: 27-slot delete-on-close screen, sneak keeps the filter (compat/TrashCanScreen)
			Map.entry("TrashCansItemCanUseMixin", List.of("trashcans")),
			Map.entry("DhCloudHeightMixin", List.of("distanthorizons")),
			Map.entry("DhNoGenerationMixin", List.of("distanthorizons")),
			Map.entry("XaeroAtlasColourMixin", List.of("xaeroworldmap")),
			// Hats worn in the pack's Curios Face slot drawn like the head slot (curios_slots.md)
			Map.entry("SimpleHatsFaceSlotMixin", List.of("simplehats")),
			// Feather on a Stick as a cat command (cats.CatFeather); the FishingHook ones check FeatherEntity
			Map.entry("DiFeatherEntityMixin", DOMESTICATION),
			Map.entry("FishingHookFeatherPullMixin", DOMESTICATION),
			// Cozy's Improved Cats (cats.CozyCats)
			Map.entry("CozyMessageGenderMixin", COZY),
			// Alex's Mobs giant squid never flees cachalot whales (it fights them, mobs/SquidsAndWhales)
			Map.entry("AmGiantSquidAvoidWhalesMixin", List.of("alexsmobs")),
			// Alex's Mobs Fleet-Footed fix, from Raspberry Pie (W2-3); also needs its target's members (fleetFootedMatches)
			Map.entry("FleetFootedMixin", List.of("alexsmobs")),
			// Create: Gravity low gravity and the dedicated server start fix, from Creative Common (wave 2 carve, W2-4);
			// the ItemPhysic tie needs Create: Gravity too (NEEDS_ALL)
			Map.entry("CreateGravityServerStartMixin", List.of("create_gravity")),
			Map.entry("CreateGravityConfigAccessor", List.of("create_gravity")),
			Map.entry("LowGravityPullMixin", List.of("create_gravity")),
			Map.entry("LowGravitySlowPullMixin", List.of("create_gravity")),
			Map.entry("LowGravityArrowMixin", List.of("create_gravity")),
			Map.entry("LowGravityLlamaSpitMixin", List.of("create_gravity")),
			Map.entry("LowGravityBoatMixin", List.of("create_gravity")),
			Map.entry("LowGravityThrowableMixin", List.of("create_gravity")),
			// melting, from Creative Common: Rare Ice's own light melting. No MOD_RANGE row: the jar's mods.toml version
			// is ${file.jarVersion} with no Implementation-Version, and the redirect is require = 0, so any loaded Rare
			// Ice keeps it (SQ-MELT-09)
			Map.entry("RareIceLightMeltMixin", List.of("rare_ice")),
			// Snow! Real Magic fold-in (snow_real_magic.md 4, rulings R6, R16, R17): magma vs its snowfall and no SRM ice
			// melt, walk-through for its snowy plant, one pack grass spread tick
			Map.entry("SrmWorldTickMixin", List.of("snowrealmagic")),
			Map.entry("SrmSnowCollisionMixin", List.of("snowrealmagic")),
			Map.entry("SrmGrassSpreadMixin", List.of("snowrealmagic")),
			// part B (snow_real_magic.md 4c, R10 to R13, R18, R19): scoops, look off, salt guards, blizzards, ice chance,
			// drip sides
			Map.entry("SrmUseMixin", List.of("snowrealmagic")),
			Map.entry("SrmHooksMixin", List.of("snowrealmagic")),
			Map.entry("SrmFallingSnowMixin", List.of("snowrealmagic")),
			Map.entry("SrmGameruleDefaultsMixin", List.of("snowrealmagic")),
			Map.entry("SrmSnowPlaceLookMixin", List.of("snowrealmagic")),
			Map.entry("SrmLookOptionsMixin", List.of("snowrealmagic")),
			Map.entry("SrmLookSaveMixin", List.of("snowrealmagic")),
			Map.entry("SrmRenderMixin", List.of("snowrealmagic")),
			// Ore Excavation undo rules (excavation/)
			Map.entry("OreExcavationBlockHistoryMixin", ORE_EXCAVATION),
			Map.entry("OreExcavationCommandUndoMixin", ORE_EXCAVATION),
			Map.entry("OreExcavationHistoryMixin", ORE_EXCAVATION),
			Map.entry("OreExcavationMiningAgentMixin", ORE_EXCAVATION),
			Map.entry("OreExcavationSchedulerMixin", ORE_EXCAVATION),
			// BlockRunner surfaces (surface/)
			Map.entry("BlockRunnerBlockStateMixin", BLOCKRUNNER),
			Map.entry("BlockRunnerSpeedConfigMixin", BLOCKRUNNER),
			// pass-through field of view: BlockRunner's own FOV recompute steps aside for surface.PassFov
			Map.entry("BlockRunnerFovMixin", BLOCKRUNNER),
			// pass-through phase 2 (plan_passthrough.md 5.1, 5.3): pickarang and chakram snag, torch arrow web pickup
			Map.entry("QuarkPickarangPassMixin", QUARK),
			Map.entry("QuarkTorchArrowPickupMixin", QUARK),
			Map.entry("BotaniaChakramPassMixin", List.of("botania")),
			// Camouflage order on Framed Blocks and Copycats+ (surface/LastInserted*)
			Map.entry("FramedDoubleCamoOrderMixin", List.of("framedblocks")),
			Map.entry("CopycatMaterialOrderMixin", List.of("copycats")),
			// Portal changes (plan_portal_changes.md): gun portal despawn, opaque gun portals, mirror glass, mirror
			// limits and portal fog
			Map.entry("PortalGunHeartbeatMixin", List.of("portalgun")),
			Map.entry("PortalGunPortalInvoker", List.of("portalgun")),
			Map.entry("PortalGunRendererOpaqueMixin", List.of("portalgun")),
			Map.entry("IPMirrorGlassMixin", IMM_PTL),
			// Lift after any IP teleport only on floor exits (plan_portal_gun.md 3, plan_bugfix_1114.md 4.1)
			Map.entry("IPExitLiftMixin", IMM_PTL),
			Map.entry("IPPortalRenderingAccessor", IMM_PTL),
			Map.entry("IPPortalRenderLimitsMixin", IMM_PTL),
			Map.entry("IPPortalFogMixin", IMM_PTL),
			// Corpse decay, wither look and live equipment (corpse/, plan_corpses.md 3.1)
			Map.entry("CorpseDecayMixin", List.of("corpse")),
			Map.entry("CorpseRendererMixin", List.of("corpse")),
			// Corpse screen sections: cosmetic armor and curio slots, Additional Items without them (plan_corpses.md 3.6)
			Map.entry("CorpseInventoryContainerMixin", List.of("corpse")),
			Map.entry("CorpseContainerBaseMixin", List.of("corpse")),
			Map.entry("CorpseAdditionalContainerMixin", List.of("corpse")),
			Map.entry("CorpseGuisMixin", List.of("corpse")),
			Map.entry("CorpseScreenMixin", List.of("corpse")),
			// interface mixins (JEI, tooltips, GUI titles, toasts, effect icons)
			Map.entry("SubMenuConfigScreenMixin", List.of("create")),
			Map.entry("JeiRecipeGuiLogicMixin", List.of("jei")),
			Map.entry("JeiMissingSlotsErrorAccessor", List.of("jei")),
			Map.entry("JeiRecipeLayoutTooltipMixin", List.of("jei")),
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
	 * Tested version range of every gated mod: outside it the mod's mixins are skipped with one warning instead of
	 * crashing the boot (mixins with require = 1, shadows and accessors fail hard on a changed target). Lower bound =
	 * the tested jar, upper bound = its next minor version (publish_readiness.md PR-CP-05).
	 */
	private static final Map<String, String> MOD_RANGE = Map.ofEntries(
			Map.entry("jei", com.SaintAlfonzo.creativecompat.compat.jei.JeiGate.RANGE),
			Map.entry("jeed", "[1.19.2-2.1.4,1.19.2-2.2)"),
			Map.entry("create", "[0.5.1.i,0.5.2)"),
			Map.entry("lightmanscurrency", "[1.19.2-2.2.1.3g,1.19.2-2.3)"),
			Map.entry("patchouli", "[1.19.2-77,1.19.3)"),
			Map.entry("ftblibrary", "[1902.4.1-build.236,1902.5)"),
			Map.entry("ftbquests", "[1902.5.10-build.497,1902.6)"),
			Map.entry("prettypipes", "[1.13.6,1.14)"),
			Map.entry("wares", "[1.2.9,1.3)"),
			Map.entry("ae2", "[12.9.12,12.10)"),
			Map.entry("legendarytooltips", "[1.4.0,1.5)"),
			Map.entry("handcrafted", "[2.0.6,2.1)"),
			Map.entry("create_dd", "[1.2c.Beta,1.3)"),
			Map.entry("supplementaries", "[1.19.2-2.4.20,1.19.2-2.5)"),
			Map.entry("moonlight", "[1.19.2-2.3.7,1.19.2-2.4)"),
			Map.entry("itemphysic", "[1.6.7,1.7)"),
			Map.entry("beautify", "[1.4.3,1.5)"),
			Map.entry("botanypots", "[9.0.44,9.1)"),
			Map.entry("framedblocks", "[6.8.6,6.9)"),
			Map.entry("twigs", "[1.19.2-3.0.1,1.19.2-3.1)"),
			Map.entry("immersiveengineering", "[1.19.2-9.2.4-170,1.19.2-9.3)"),
			Map.entry("immersive_weathering", "[1.19.2-1.2.10,1.19.2-1.3)"),
			Map.entry("mca", "[7.5.20+1.19.2,7.6)"),
			Map.entry("quark", "[3.4-418,3.5)"),
			Map.entry("immersive_paintings", "[0.6.7+1.19.2,0.7)"),
			Map.entry("another_furniture", "[1.19.2-2.1.4,1.19.2-2.2)"),
			Map.entry("bedspreads", "[6.0.0+1.19.2,6.1)"),
			Map.entry("customizableelytra", "[1.19.0-1.7.4,1.19.0-1.8)"),
			Map.entry("trashslot", "[12.1.0,12.2)"),
			Map.entry("trashcans", "[1.0.18,1.1)"),
			Map.entry("distanthorizons", "[3.0.2-b,3.1)"),
			Map.entry("xaeroworldmap", "[1.40.11,1.41)"),
			Map.entry("simplehats", "[1.19.2-0.3.2,1.19.2-0.4)"),
			Map.entry("domesticationinnovation", "[1.6.1,1.7)"),
			Map.entry("cozys_improved_cats", "[2.1.0,2.2)"),
			Map.entry("alexsmobs", "[1.21.1,1.22)"),
			Map.entry("create_gravity", "[1.2.0,1.3)"),
			Map.entry("oreexcavation", "[1.11.166,1.12)"),
			Map.entry("blockrunner", "[4.2.2,4.3)"),
			Map.entry("botania", "[1.19.2-440.2-FORGE,1.19.2-441)"),
			Map.entry("copycats", "[2.2.2+mc.1.19.2-forge,2.3)"),
			Map.entry("portalgun", "[5.0.0,5.1)"),
			Map.entry("imm_ptl_core", "[2.3.7,2.4)"),
			Map.entry("corpse", "[1.19.2-1.0.17,1.19.2-1.1)"),
			Map.entry("curios", "[1.19.2-5.1.6.4,1.19.2-5.2)"),
			Map.entry("breezy", "[1.0.8,1.1)"),
			Map.entry("polymorph", "[0.46.6+1.19.2,0.47)"),
			Map.entry("sophisticatedcore", "[1.19.2-0.6.4.730,1.19.2-0.7)"),
			Map.entry("geckolib3", "[3.1.40,3.2)"),
			Map.entry("immediatelyfast", "[1.4.0+1.19.2,1.5)"),
			Map.entry("snowrealmagic", "[6.5.4,6.6)"));

	/**
	 * Fix mixins written against one exact jar: applied only at these versions, since a newer jar may carry the upstream
	 * fix the mixin would undo (publish_readiness.md PR-CP-06). The only {@code @Overwrite}: Breezy 1.0.8's wind data
	 * for Supplementaries 2.4.20.
	 */
	private static final Map<String, Map<String, String>> EXACT = Map.of(
			"SupplementariesBreezyCompatMixin", Map.of("supplementaries", "[1.19.2-2.4.20]", "breezy", "[1.0.8]"));

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
			// Cozy's home goal calls cats.CatFeather, which uses DI's FeatherEntity (publish_readiness.md PR-CP-07)
			Map.entry("CozyHomeGoalFeatherMixin", List.of("domesticationinnovation", "cozys_improved_cats")),
			Map.entry("PrettyPipesCraftingTerminalPolymorphMixin", List.of("polymorph", "prettypipes")),
			// Create's breaker target, Quark's monster box
			Map.entry("CreateBreakerMonsterBoxMixin", List.of("create", "quark")),
			// Immersive Weathering's azalea flowers place Twigs' azalea flowers block (item_merges.md 2.21)
			Map.entry("IwAzaleaFlowersPlaceMixin", List.of("immersive_weathering", "twigs")),
			// Transfer Items returns curios to their recorded slots (corpse/CorpseCurios, plan_corpses.md 3.6 item 4)
			Map.entry("CorpseTransferMessageMixin", List.of("corpse", "curios")),
			// Immersive Weathering's rain melt on Snow! Real Magic's snowy plant keeps the plant (snow_real_magic.md B5)
			Map.entry("IwSnowMeltSrmMixin", List.of("immersive_weathering", "snowrealmagic")),
			// GeckoLib item in the hotbar flushing ImmediatelyFast's HUD item batch (plan_portal_gun.md 2)
			Map.entry("GeckoItemHudBatchMixin", List.of("geckolib3", "immediatelyfast")),
			// Dropped items follow Create: Gravity's low gravity: ItemPhysic's pull, scaled by gravity.LowGravity (wave 2)
			Map.entry("LowGravityItemPhysicMixin", List.of("itemphysic", "create_gravity")));

	/** True when {@code modId} is loaded at a version in {@code spec}; a loaded jar at another version logs once. */
	private static boolean exactOn(String mixin, String modId, String spec) {
		net.minecraftforge.fml.loading.moddiscovery.ModFileInfo file =
				net.minecraftforge.fml.loading.LoadingModList.get().getModFileById(modId);
		if (file == null) return false;
		org.apache.maven.artifact.versioning.ArtifactVersion have = file.getMods().stream()
				.filter(m -> m.getModId().equals(modId)).findFirst().orElse(file.getMods().get(0)).getVersion();
		if (inRange(spec, have)) return true;
		LOGGER.warn("[creativecompat] {} {} is not {}: {} is off (check the fix against the new jar)", modId, have, spec, mixin);
		return false;
	}

	public CompatMixinPlugin() {
		super("[creativecompat]", NEEDS_MOD, MOD_RANGE);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CompatMixinPlugin.class);

	/** Mixins switched off in [mixins], read once from the config file. */
	private Set<String> switchedOff;

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		String name = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
		if (MixinSwitches.MIXINS.containsKey(name)) {
			if (switchedOff == null) {
				switchedOff = MixinSwitches.off(FMLPaths.CONFIGDIR.get().resolve("creativecompat-common.toml"));
				if (!switchedOff.isEmpty()) LOGGER.info("[creativecompat] mixins switched off in [mixins]: {}", switchedOff);
			}
			if (switchedOff.contains(name)) return false;
		}
		Map<String, String> exact = EXACT.get(name);
		if (exact != null && !exact.entrySet().stream().allMatch(e -> exactOn(name, e.getKey(), e.getValue()))) return false;
		List<String> all = NEEDS_ALL.get(name);
		if (all != null) return all.stream().allMatch(this::modOn);
		if (!super.shouldApplyMixin(targetClassName, mixinClassName)) return false;
		if (name.equals("FleetFootedMixin")) return fleetFootedMatches(targetClassName);
		if (name.equals("VanillaLightMeltMixin") && targetClassName.endsWith(".SnowLayerBlock") && modOn("snowrealmagic")) {
			// Snow! Real Magic replaces SnowLayerBlock's random tick (no light read left to redirect) and melts snow
			// layers itself; the ice target keeps the light melt.
			LOGGER.info("[creativecompat] Snow! Real Magic loaded: snow layers melt by its rules, not [melting]");
			return false;
		}
		return true;
	}

	private static final String LIVING_INT = "(Lnet/minecraft/world/entity/LivingEntity;I)V";

	/**
	 * Fleet-Footed fix (from Raspberry Pie, plan_rp_standalone.md P6): EffectFleetFooted (Alex's Mobs 1.21.1, the
	 * version tested) must have the int field {@code lastDuration} (shadowed; written by its isDurationEffectTick) and
	 * applyEffectTick(LivingEntity, int) (SRG {@code m_6742_}, the injection point). Anything else, including an
	 * unreadable class, skips the fix with one warning instead of failing the mixin config.
	 */
	private static boolean fleetFootedMatches(String targetClassName) {
		List<String> missing = new java.util.ArrayList<>();
		try {
			org.objectweb.asm.tree.ClassNode node = org.spongepowered.asm.service.MixinService.getService()
					.getBytecodeProvider().getClassNode(targetClassName);
			if (node.fields == null || node.fields.stream().noneMatch(f -> f.name.equals("lastDuration") && f.desc.equals("I"))) {
				missing.add("int field lastDuration");
			}
			if (node.methods == null || node.methods.stream().noneMatch(m -> (m.name.equals("m_6742_") || m.name.equals("applyEffectTick")) && m.desc.equals(LIVING_INT))) {
				missing.add("applyEffectTick(LivingEntity, int) (m_6742_)");
			}
		} catch (Throwable t) {
			missing.add("the class itself (" + t + ")");
		}
		if (missing.isEmpty()) return true;
		LOGGER.warn("[creativecompat] Alex's Mobs {} lacks {}: the Fleet-Footed fix is off (tested with Alex's Mobs 1.21.1)",
				targetClassName, String.join(", ", missing));
		return false;
	}
}
