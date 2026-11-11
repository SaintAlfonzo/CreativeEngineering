package com.SaintAlfonzo.creativedelight;

import com.mojang.logging.LogUtils;
import com.SaintAlfonzo.creativedelight.farm.VillageFarmCrops;
import com.SaintAlfonzo.creativedelight.treatedstick.TreatedSticks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.slf4j.Logger;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

/**
 * Creative Delight (feature_writeup/creative_delight.md): the Delight add-ons the pack owns, in one jar. Each member is
 * its own mod, nested under META-INF/jarjar/ with its own mod id and namespace; this mod id ({@code creativedelight})
 * registers only the placed treated-stick blocks ({@link TreatedSticks}). What CD adds is KubeJS scripts (startup scripts in {@code kubejs_startup/}, server and
 * client scripts under {@code data/} and {@code assets/creativedelight/kubejs/}) that call the pack's helpers, and
 * {@link CDConfig}.
 *
 * Since the Creative Split CD carve (2026-11-01, creative_delight.md 34) it also holds the kitchen and food Java that
 * was patcherMod's: the oven, lunchboxes, the remainder buffer, cans, seasonings, placed food, crops (stem fruit, crop
 * parity, dandelions, hoe area), salt ticks, troughs and the food mod compat (Corn Delight, Display Delight dishes,
 * Respite potted tea bush, AppleSkin; the Canned Goods golem dye moved into the repackedCannedGoods member). Their ids moved to this mod's namespace on
 * 2026-11-10 (plan_cd_namespace.md).
 */
@Mod(CreativeDelight.MOD_ID)
public class CreativeDelight {

	public static final String MOD_ID = "creativedelight";
	public static final Logger LOGGER = LogUtils.getLogger();

	public CreativeDelight() {
		// Mod id was creative_delight up to 2026-11-01 (creative_split.md 7.6): keep the player's values; ids remap through
		// data/creativedelight/swaps/creative_split.json.
		com.SaintAlfonzo.creativecommon.swap.ConfigRename.carryOver("creative_delight-common.toml", "creativedelight-common.toml");
		// [campfire] moved from creativecompat-common.toml (2026-11-04, CDConfig.COMPAT_SECTIONS): carried first, so Compat's
		// value wins; Compat drops the section from its own file when Forge corrects it.
		boolean campfireCarried = com.SaintAlfonzo.creativecommon.swap.ConfigRename.carrySections(
				CDConfig.COMPAT_FILE, CDConfig.FILE, CDConfig.COMPAT_SECTIONS)
				|| !com.SaintAlfonzo.creativecommon.swap.ConfigRename.mergeSections(
				CDConfig.COMPAT_FILE, CDConfig.FILE, CDConfig.COMPAT_SECTIONS).isEmpty();
		// The sections moved from creativeengineering-common.toml at the CD carve (CDConfig.MOVED_SECTIONS): the first boot
		// copies the player's values in (carry when this file is new, merge when it exists); patcherMod drops them from its own file when
		// Forge corrects it. Must run before Forge reads the COMMON configs.
		boolean carried = com.SaintAlfonzo.creativecommon.swap.ConfigRename.carrySections(
				CDConfig.OLD_FILE, CDConfig.FILE, CDConfig.MOVED_SECTIONS)
				|| !com.SaintAlfonzo.creativecommon.swap.ConfigRename.mergeSections(
				CDConfig.OLD_FILE, CDConfig.FILE, CDConfig.MOVED_SECTIONS).isEmpty();
		// A pack that skipped 1.2.00 still has [campfire] in patcherMod's file (Compat never carried it): take it from there.
		if (!campfireCarried) com.SaintAlfonzo.creativecommon.swap.ConfigRename.mergeSections(
				CDConfig.OLD_FILE, CDConfig.FILE, CDConfig.COMPAT_SECTIONS);
		// The Display and Vintage ports' own files, read once into [display] / [vintage] (plan_creative_delight.md A6).
		com.SaintAlfonzo.creativedelight.compat.MemberConfigMove.run();
		// M8: [members] blockheads_additions / incubation_additions became [compartments] switches; read raw below.
		com.SaintAlfonzo.creativedelight.compat.MemberConfigMove.carryCompartmentSwitches();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CDConfig.SPEC, CDConfig.FILE);
		// [salt] lives in this file, so its load events reach this bus, not Salt's: Salt rereads its food list here.
		FMLJavaModLoadingContext.get().getModEventBus().addListener((net.minecraftforge.fml.event.config.ModConfigEvent.Loading e) -> {
			if (e.getConfig().getSpec() == CDConfig.SPEC) com.SaintAlfonzo.creativedelight.compat.SaltConfigBridge.refresh();
		});
		FMLJavaModLoadingContext.get().getModEventBus().addListener((net.minecraftforge.fml.event.config.ModConfigEvent.Reloading e) -> {
			if (e.getConfig().getSpec() == CDConfig.SPEC) com.SaintAlfonzo.creativedelight.compat.SaltConfigBridge.refresh();
		});
		if (!carried) CDConfig.warnOldKeys();
		VillageFarmCrops.PROCESSORS.register(FMLJavaModLoadingContext.get().getModEventBus());
		VillageFarmCrops.LEGACY_PROCESSORS.register(FMLJavaModLoadingContext.get().getModEventBus());
		MinecraftForge.EVENT_BUS.addListener(VillageFarmCrops::onServerAboutToStart);
		TreatedSticks.register(FMLJavaModLoadingContext.get().getModEventBus());
		// Wooden bucket and its farm fluids (moved from Creative Equipment 2026-11-09; farm/bucket/)
		com.SaintAlfonzo.creativedelight.farm.bucket.WoodenBuckets.register(FMLJavaModLoadingContext.get().getModEventBus());
		registerKitchen(FMLJavaModLoadingContext.get().getModEventBus());
		registerCompartments(FMLJavaModLoadingContext.get().getModEventBus());

		List<String> members = CDConfig.MEMBERS;
		List<String> missing = members.stream().filter(id -> !ModList.get().isLoaded(id)).toList();
		LOGGER.info("[creativedelight] members: {}{}", members, missing.isEmpty() ? "" : " (NOT loaded: " + missing + ")");
		members.forEach(CreativeDelight::warnIfNotNested);
	}

	/**
	 * The optional compartments folded in at M8 (plan_cb_publish.md 3b): each entry class is named only here, behind its
	 * guard, so without Cooking for Blockheads, Balm or Incubation the JVM never loads a class that names their types.
	 */
	private static void registerCompartments(IEventBus modBus) {
		if (com.SaintAlfonzo.creativedelight.compat.Compartments.on(com.SaintAlfonzo.creativedelight.compat.Compartments.CFB)) {
			com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.CfbCompartment.init(modBus);
		}
		if (com.SaintAlfonzo.creativedelight.compat.Compartments.on(com.SaintAlfonzo.creativedelight.compat.Compartments.INCUBATION)) {
			com.SaintAlfonzo.creativedelight.compat.incubation.IncubationCompartment.init(modBus);
		}
		modBus.addListener(com.SaintAlfonzo.creativedelight.compat.CompartmentPacks::add);
		LOGGER.info("[creativedelight] compartments: cookingforblockheads {}, incubation {}",
				com.SaintAlfonzo.creativedelight.compat.Compartments.on(com.SaintAlfonzo.creativedelight.compat.Compartments.CFB) ? "on" : "off",
				com.SaintAlfonzo.creativedelight.compat.Compartments.on(com.SaintAlfonzo.creativedelight.compat.Compartments.INCUBATION) ? "on" : "off");
	}

	/** The kitchen and food Java moved from patcherMod (creative_delight.md 34), registered as patcherMod did. */
	private static void registerKitchen(IEventBus modBus) {
		ModList mods = ModList.get();
		// Oven: FD cooking-pot style baking block + the creativedelight:baking recipe type (oven/; oven.md).
		com.SaintAlfonzo.creativedelight.oven.OvenRegistry.register(modBus);
		// creativedelight:biome_tag loot condition for the fishing net tables (from Creative Worldgen, 2026-11-06)
		com.SaintAlfonzo.creativedelight.loot.BiomeTagCondition.register(modBus);
		// Powdered sugar block + bucket (food/PowderedSugarRegistry). Item and recipes are KubeJS.
		com.SaintAlfonzo.creativedelight.food.PowderedSugarRegistry.register(modBus);
		// Display Delight placed blocks for foods it has none for (compat/DisplayDelightDishes).
		com.SaintAlfonzo.creativedelight.compat.DisplayDelightDishes.register(modBus);
		// The oven's leftover slots show on WTHIT through Common's RemainderView (compat/WthitRemainderBuffer).
		com.SaintAlfonzo.creativecompat.compat.RemainderView.addStation(
				com.SaintAlfonzo.creativedelight.oven.OvenBlockEntity.class);
		// Campfires: rain puts them out, Comfort nearby (campfire/, mixin.minecraft.CampfireRainMixin; moved from Compat
		// 2026-11-04). patcherMod's WTHIT line reads the switch and ticks through Common's CampfireRainInfo holder.
		com.SaintAlfonzo.creativecommon.campfire.CampfireRainInfo.set(
				() -> CDConfig.SPEC.isLoaded() && CDConfig.CAMPFIRE.rainDouses.get(),
				() -> CDConfig.SPEC.isLoaded() ? CDConfig.CAMPFIRE.rainOutTicks.get() : 160);
		if (mods.isLoaded("jei")) {
			// JEI World Interactions: Campfire Rules rows (WorldRows is JEI-free; JEI loads its plugin later)
			com.SaintAlfonzo.creativecompat.compat.jei.world.WorldRows.provider(
					com.SaintAlfonzo.creativecompat.compat.jei.world.WorldTab.CAMPFIRE,
					com.SaintAlfonzo.creativedelight.compat.jei.CampfireRows::rows);
		}
		// Spice of Life lunchboxes: Netherite tier, upgrade/skin/dye/soap recipes, "Slots" tooltip (lunchboxes.md).
		if (mods.isLoaded("solapplepie")) {
			com.SaintAlfonzo.creativedelight.lunchbox.LunchboxItems.register(modBus);
			com.SaintAlfonzo.creativedelight.lunchbox.LunchboxRecipes.register(modBus);
			MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.lunchbox.LunchboxTooltip.class);
		}
		// Caffeinated adds a hidden Phantom Bane (from Creative Common's PhantomBaneRules, 2026-11-07).
		if (mods.isLoaded("farmersrespite")) {
			MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.compat.farmersrespite.CaffeinatedPhantomBane.class);
		}
		// Corn Delight 1.2.11 on 1.0.3: Cob Pipe right click to equip (corn_delight.md 2.3).
		if (mods.isLoaded("corn_delight")) {
			MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.compat.CornDelightCompat.class);
		}
		// Remainder buffer: pot/kettle/oven keep ingredient leftovers (remainder_buffer.md). Saved as an attached cap.
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.cooking.RemainderBufferStorage.class);
		// Cookware: sneak right-click picks up, sneak left-click toggles the handle (QoL-7, cooking/kitchen #27).
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.cooking.CookwareHandling.class);
		// Cookware keeps its contents in the item on a player break or pickup, glint and "Holds:" tooltip (QoL-8, cooking/kitchen #40).
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.cooking.CookwareContents.class);
		// Multi-serving cans ([canning]): max-servings cache cleared on tag reload + the "Servings" tooltip line.
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.food.CanServings.class);
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.food.CanServingsTooltip.class);
		// Pack seasonings (kubejs_startup/creativedelight/seasonings.js): the craft, the bonus and the tooltip lines.
		com.SaintAlfonzo.creativedelight.food.SeasoningRecipe.register(modBus);
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.food.Seasonings.class);
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.food.SeasoningTooltip.class);
		if (mods.isLoaded("appleskin")) {
			MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.compat.AppleSkinSeasonings.class);
		}
		// Bites from placed pies and cakes count for Spice of Life (the bite is mixin.minecraft.ServerPlayerGameModePlacedFoodMixin).
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.food.PlacedFoodSol.class);
		// Per-hoe Quark harvest area: the "Harvest area: 3x3" tooltip (the area is mixin.quark.QuarkHoeArea*).
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.crops.HoeHarvestTooltip.class);
		// Water bucket / bottle soaks farmland soils (farming/crops #17)
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.crops.WaterOnSoil.class);
		modBus.addListener(CreativeDelight::commonSetup);
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
		ModList mods = ModList.get();
		// Vintage's salt blocks melt ice and dissolve like The Salt's (item_merges.md 2.9).
		event.enqueueWork(com.SaintAlfonzo.creativedelight.salt.SaltBlockTicks::init);
		// Corn is villager food (corn_delight.md 2.6).
		if (mods.isLoaded("corn_delight")) {
			event.enqueueWork(com.SaintAlfonzo.creativedelight.compat.CornDelightCompat::addVillagerFood);
		}
		// Farmer's Respite 2.0 registers potted_wild_tea_bush but never adds it to the flower pot (farmers_respite.md 2.2).
		if (mods.isLoaded("farmersrespite")) {
			event.enqueueWork(com.SaintAlfonzo.creativedelight.compat.RespitePottedTeaBush::register);
		}
		// Can-serving crafting action for KubeJS recipes (NCH re-applies it on every server script load; this covers
		// clients of a dedicated server).
		if (mods.isLoaded("kubejs")) {
			event.enqueueWork(com.SaintAlfonzo.creativedelight.compat.KubeJSCanAction::register);
		}
	}

	/** Client half of the kitchen Java (MOD bus, client only). */
	@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static final class ClientModEvents {

		private ClientModEvents() {}

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			ModList mods = ModList.get();
			if (mods.isLoaded("solapplepie")) {
				com.SaintAlfonzo.creativedelight.lunchbox.client.LunchboxClient.setup(event);
			}
			if (mods.isLoaded("corn_delight") && mods.isLoaded("curios")) {
				com.SaintAlfonzo.creativedelight.compat.CobPipeRenderer.register();
			}
			event.enqueueWork(() -> net.minecraft.client.gui.screens.MenuScreens.register(
					com.SaintAlfonzo.creativedelight.oven.OvenRegistry.OVEN_MENU.get(),
					com.SaintAlfonzo.creativedelight.oven.client.OvenScreen::new));
		}

		/** Supplementaries faucet moved toward the block it drains (cooking/kitchen #26); after the mods' own wrappers. */
		@SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
		public static void onModelsBaked(net.minecraftforge.client.event.ModelEvent.BakingCompleted event) {
			if (ModList.get().isLoaded("supplementaries")) {
				com.SaintAlfonzo.creativedelight.compat.supplementaries.client.FaucetFitModel.wrap(event);
			}
		}

		@SubscribeEvent
		public static void onItemColors(RegisterColorHandlersEvent.Item event) {
			if (ModList.get().isLoaded("solapplepie")) {
				com.SaintAlfonzo.creativedelight.lunchbox.client.LunchboxClient.onItemColors(event);
			}
		}
	}

	/**
	 * A jar in mods/ always beats a nested one with the same mod id, whatever the versions (creative_delight.md 14.1
	 * point 4, SQ-CDJ-02). A nested member is loaded from the root of its own jij: file system; a top-level jar from
	 * the default one, so a member whose mod file lives there is a stale upstream jar left beside CD.
	 */
	private static void warnIfNotNested(String id) {
		IModFileInfo info = ModList.get().getModFileById(id);
		if (info == null) return;
		Path path = info.getFile().getFilePath();
		if (path.getFileSystem() != FileSystems.getDefault()) return;
		LOGGER.error("[creativedelight] member {} is loaded from {} (version {}), not from the Creative Delight jar: "
				+ "a jar in mods/ overrides the nested copy. Remove that jar.", id, path.getFileName(),
				info.versionString());
	}
}
