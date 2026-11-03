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
 * Respite potted tea bush, AppleSkin; the Canned Goods golem dye moved into the repackedCannedGoods member). Their ids keep the {@code creativeengineering}
 * namespace ({@link #NS}).
 */
@Mod(CreativeDelight.MOD_ID)
public class CreativeDelight {

	public static final String MOD_ID = "creativedelight";
	/** The namespace of the ids the code moved from patcherMod registers and reads: patcherMod's, unchanged by the move. */
	public static final String NS = "creativeengineering";
	public static final Logger LOGGER = LogUtils.getLogger();

	public CreativeDelight() {
		// Mod id was creative_delight up to 2026-11-01 (creative_split.md 7.6): keep the player's values; ids remap through
		// data/creativedelight/swaps/creative_split.json.
		com.SaintAlfonzo.creativeengineering.swap.ConfigRename.carryOver("creative_delight-common.toml", "creativedelight-common.toml");
		// The sections moved from creativeengineering-common.toml at the CD carve (CDConfig.MOVED_SECTIONS): the first boot
		// copies the player's values in (carry when this file is new, merge when it exists); patcherMod drops them from its own file when
		// Forge corrects it. Must run before Forge reads the COMMON configs.
		boolean carried = com.SaintAlfonzo.creativeengineering.swap.ConfigRename.carrySections(
				CDConfig.OLD_FILE, CDConfig.FILE, CDConfig.MOVED_SECTIONS)
				|| !com.SaintAlfonzo.creativeengineering.swap.ConfigRename.mergeSections(
				CDConfig.OLD_FILE, CDConfig.FILE, CDConfig.MOVED_SECTIONS).isEmpty();
		// The Display and Vintage ports' own files, read once into [display] / [vintage] (plan_creative_delight.md A6).
		com.SaintAlfonzo.creativedelight.compat.MemberConfigMove.run();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CDConfig.SPEC, CDConfig.FILE);
		if (!carried) CDConfig.warnOldKeys();
		VillageFarmCrops.PROCESSORS.register(FMLJavaModLoadingContext.get().getModEventBus());
		VillageFarmCrops.LEGACY_PROCESSORS.register(FMLJavaModLoadingContext.get().getModEventBus());
		MinecraftForge.EVENT_BUS.addListener(VillageFarmCrops::onServerAboutToStart);
		TreatedSticks.register(FMLJavaModLoadingContext.get().getModEventBus());
		registerKitchen(FMLJavaModLoadingContext.get().getModEventBus());

		List<String> members = CDConfig.MEMBERS;
		List<String> missing = members.stream().filter(id -> !ModList.get().isLoaded(id)).toList();
		LOGGER.info("[creativedelight] members: {}{}", members, missing.isEmpty() ? "" : " (NOT loaded: " + missing + ")");
		members.forEach(CreativeDelight::warnIfNotNested);
	}

	/** The kitchen and food Java moved from patcherMod (creative_delight.md 34), registered as patcherMod did. */
	private static void registerKitchen(IEventBus modBus) {
		ModList mods = ModList.get();
		// Oven: FD cooking-pot style baking block + the creativeengineering:baking recipe type (oven/; oven.md).
		com.SaintAlfonzo.creativedelight.oven.OvenRegistry.register(modBus);
		// Powdered sugar block + bucket (food/PowderedSugarRegistry). Item and recipes are KubeJS.
		com.SaintAlfonzo.creativedelight.food.PowderedSugarRegistry.register(modBus);
		// Display Delight placed blocks for foods it has none for (compat/DisplayDelightDishes).
		com.SaintAlfonzo.creativedelight.compat.DisplayDelightDishes.register(modBus);
		// The oven's leftover slots show on WTHIT through Common's RemainderView (compat/WthitRemainderBuffer).
		com.SaintAlfonzo.creativeengineering.compat.RemainderView.addStation(
				com.SaintAlfonzo.creativedelight.oven.OvenBlockEntity.class);
		// Spice of Life lunchboxes: Netherite tier, upgrade/skin/dye/soap recipes, "Slots" tooltip (lunchboxes.md).
		if (mods.isLoaded("solapplepie")) {
			com.SaintAlfonzo.creativedelight.lunchbox.LunchboxItems.register(modBus);
			com.SaintAlfonzo.creativedelight.lunchbox.LunchboxRecipes.register(modBus);
			MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.lunchbox.LunchboxTooltip.class);
		}
		// Corn Delight 1.2.11 on 1.0.3: Cob Pipe right click to equip (corn_delight.md 2.3).
		if (mods.isLoaded("corn_delight")) {
			MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.compat.CornDelightCompat.class);
		}
		// Remainder buffer: pot/kettle/oven keep ingredient leftovers (remainder_buffer.md). Saved as an attached cap.
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.cooking.RemainderBufferStorage.class);
		// Cookware: sneak right-click picks up, sneak left-click toggles the handle (QoL-7, cooking/kitchen #27).
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.cooking.CookwareHandling.class);
		// Multi-serving cans ([canning]): max-servings cache cleared on tag reload + the "Servings" tooltip line.
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.food.CanServings.class);
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.food.CanServingsTooltip.class);
		// Pack seasonings (kubejs_startup/creativeengineering/seasonings.js): the craft, the bonus and the tooltip lines.
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
