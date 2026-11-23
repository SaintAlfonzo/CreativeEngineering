package com.SaintAlfonzo.creativecommon;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.apache.logging.log4j.LogManager;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraft.world.item.Tiers;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod(CreativeCommon.MODID)
public class CreativeCommon
{
	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------
	/** This jar's mod id (creative_split.md 6 Q1 and 7.13); Creative Engineering (development/creativeEngineering) is
	 *  {@code creativeengineering}. */
	public static final String MODID = "creativecommon";
	/** The namespace of every id, tag, lang key and channel this jar uses: Creative Common's, unchanged by the swap. */
	public static final String NS = "creativecommon";
	/** The file every pre-swap table lived in, now Creative Engineering's own. */
	private static final String OLD_FILE = "creativeengineering-common.toml";
	private static final String FILE = MODID + "-common.toml";
	private static final Logger LOGGER = LogUtils.getLogger();

	// -------------------------------------------------------------------------
	// Constructor — wires up event buses and registers items
	// -------------------------------------------------------------------------
	public CreativeCommon() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::commonSetup);
		// [creativecommon] builds: the stamp of every own jar (plan_process_overhaul.md P1.2)
		com.SaintAlfonzo.creativecommon.util.BuildStamps.log();

		// Must be registered here, during construction - Forge loads every mod's COMMON/CLIENT
		// config immediately after every mod's constructor has run, so anything later (even
		// FMLCommonSetupEvent) is too late and the file is never read.
		// The tables came from Creative Common's creativeengineering-common.toml, which Creative Engineering keeps for its
		// [shrapnel], [surface] and [vein_undo]: the first boot copies the player's values over (then Engineering's
		// spec drops them from that file when Forge corrects it). The swap list toml is only renamed.
		com.SaintAlfonzo.creativecommon.swap.ConfigRename.carrySections(OLD_FILE, FILE, CommonConfig.SECTIONS);
		com.SaintAlfonzo.creativecommon.swap.ConfigRename.carryOver("creativeengineering-swaps.toml", MODID + "-swaps.toml");
		// [effects] and [magic_resistance] are carried over from Creative Brewing's file (plan_cb_polish.md 2) and
		// patcherMod's old file: the first boot copies the player's values in (sections this file already has are kept).
		// CB drops them from its own file when Forge corrects it.
		if (!com.SaintAlfonzo.creativecommon.swap.ConfigRename.carrySections("creativebrewing-common.toml", FILE, CommonConfig.EFFECT_SECTIONS))
			com.SaintAlfonzo.creativecommon.swap.ConfigRename.mergeSections("creativebrewing-common.toml", FILE, CommonConfig.EFFECT_SECTIONS);
		com.SaintAlfonzo.creativecommon.swap.ConfigRename.mergeSections(OLD_FILE, FILE, CommonConfig.EFFECT_SECTIONS);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.COMMON_SPEC, FILE);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CommonClientConfig.CLIENT_SPEC, "creativecommon-client.toml");

		// Every potion and mob effect the Creative mods own (plan_cb_polish.md 2, from Creative Brewing): the
		// creativecommon: potions (Magic Resistance; Better Beacons potions are Creative Brewing's; the vanilla-effect
		// ones are KubeJS, kubejs_startup/creativecommon/effects/potions.js), Magic Resistance's damage cut (the
		// MobEffect itself is KubeJS, effects/magic_resistance.js) and Phantom Bane's rules.
		com.SaintAlfonzo.creativecommon.effect.CommonPotions.register(modEventBus);
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.effect.MagicResistanceEffect.class);
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.effect.PhantomBaneRules.class);

		// Sawblade and sawdust items, the shrapnel entity, the dragon breathing recipe type, the wooden bucket:
		// Creative Engineering (creativeengineering, creative_split.md 7.13 wave 8). Dark Fire and Flint and
		// Terrasteel: Creative Magic (creativemagic) Elytra duplication, tier shields, backpack skins, stepstools, the
		// air brake, Bottled Air, the copper tool tier and the gear mixins: Creative Equipment (creativeequipment,
		// creative_split.md 7.6 wave 7). Oven, powdered sugar, Display Delight dishes: Creative Delight
		// (creative_delight.md 34). Hands, face, feet Curios slots and the "Slot:" tooltip line: Creative Equipment.
		// Spice of Life lunchboxes: Creative Delight. Trade sync: the server's villager and wandering trader lists go
		// to dedicated-server clients, so JER's villager pages show the pack's trades (trader/TradeSync;
		// feature_writeup/trade_sync.md).
		com.SaintAlfonzo.creativecommon.trader.TradeSync.register();
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.trader.TradeSync.class);
		// Gear-held effects marked for the client (armor and tool scripts -> effect/GearEffectMarks), so the inventory
		// effect list puts them in its permanent column (plan_diet_books_r2.md B2, mixin.EffectColumnsMixin).
		com.SaintAlfonzo.creativecommon.effect.GearEffectMarks.register();
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.effect.GearEffectMarks.class);
		// Corn Delight cob pipe: Creative Delight. Swap lists (swap/, Common API): every jar's
		// data/<modid>/swaps/*.json remaps ids it moved when a world loads.
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.swap.SwapRemapper.class);
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.swap.SwapReport.class);
		// Item merges in the lists ("merges", both items still registered): stacks in the world become the survivor
		// (swap/ItemMerges, triggers in creativecommon-swaps.toml; feature_writeup/item_merges.md 6).
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, com.SaintAlfonzo.creativecommon.swap.SwapConfig.SPEC, MODID + "-swaps.toml");
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.swap.ItemMerges.class);
		// Remainder buffer (cooking/): Creative Delight; the WTHIT line reads compat.RemainderView. Armor Trims' item
		// lists cached once the registry locks; the trimmable list resets on tag reload (perf.md 10.2 #1).
		com.SaintAlfonzo.creativecommon.compat.ArmorTrimsItemCache.init();
		// Emu / duck / goose nests live in their own mod: development/incubationAdditions.

		// Register this class for FORGE bus events (e.g. onServerStarted, attachCaps)
		MinecraftForge.EVENT_BUS.register(this);



		// TNT shrapnel, the IE tweaks, BlockRunner surfaces (speeds, camo tooltip, sinking): Creative Engineering.
		// Its shrapnel burst reaches Compat's burn-explosion rule through util.BurnBursts (Common API).


		// Live whistling arrows, so mixin.NeaWhistleGoalSkipMixin skips NEA's per-mob arrow query when there are none.
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.perf.WhistleArrowTracker.class);

		// Multi-serving cans, seasonings and placed-food Spice of Life: Creative Delight.

		// Per-hoe Quark harvest area and its tooltip: Creative Delight.

		// [time], seasons and crop seasons (/ccaltime): Creative Calendar (development/creativeCalendar).

		modEventBus.addListener(com.SaintAlfonzo.creativecommon.util.ModDisplayNames::onConfigReload);
		// Own config watcher (plan_post_scratch_1115.md G11): our mods' config files reload on any save, debounced 1 s
		modEventBus.addListener(com.SaintAlfonzo.creativecommon.config.OwnConfigWatcher::onLoadComplete);

		// Damage tags (Common API): data/<ns>/damage_tags/*.json reload listener (backs Creative Equipment's
		// shield.ShieldCategory and Creative Brewing's Magic Resistance) + the optional "seen sources" discovery log.
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.damage.DamageTagLoader.class);
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.damage.DamageTagLogger.class);
		// Melting: salt, fire and lava melt ice and snow (melting/, feature_writeup/melting.md)
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.melting.Melting.class);
		modEventBus.addListener(com.SaintAlfonzo.creativecommon.melting.Melting::onConfig);
		// Wood composters, one per species (composter/, farming/composting #3, feature_writeup/composting.md)
		com.SaintAlfonzo.creativecommon.composter.WoodComposters.register(modEventBus);
		// Glass drops color shards (loot/GlassShardsModifier, gameplay/misc #22, feature_writeup/glass_shards.md)
		com.SaintAlfonzo.creativecommon.loot.GlassShardsModifier.register(modEventBus);
		// Sneak right-click picks up a sack with its contents (storage/SneakPickup, items/storage-and-item-loss #12)
		MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.storage.SneakPickup.class);

		// World generation and fauna (structures, locate, earth and water fill, terrain adaptation, meta structures,
		// sky islands, bee nests, mob spawns, mooshrooms, named mobs, leaf decay, the biome_tag loot condition, the
		// Farmer's Structures donor, Forgiving Void) live in Creative Worldgen (creative_split.md 7.6).

		// /cdevplace (MassPlacer) lives in Creative Devtools (feature_writeup/creative_devtools.md 2).

		// Python generators (/cdevgen) and the other dev tools live in Creative Devtools
		// (feature_writeup/creative_devtools.md).

	}

	// -------------------------------------------------------------------------
	// commonSetup — runs during mod loading, calls IECompat.register()
	// -------------------------------------------------------------------------
	private void commonSetup(final FMLCommonSetupEvent event)
	{
			LOGGER.info("CommonSetup starting");

			// [mod_names]: mod display names in the pack's style, before JEI/WTHIT cache them
			com.SaintAlfonzo.creativecommon.util.ModDisplayNames.apply();
			// Before any chunk loads: sections count their random tickers on load
			event.enqueueWork(com.SaintAlfonzo.creativecommon.melting.Melting::initRandomTicks);

			// The sawmill classes pre-loaded here for SawmillProcessMixin: Creative Engineering.

			// The shield block handler reaches the damage-tag classes only on the first block (crash:
			// NoClassDefFoundError on DamageFlag); pre-load them too.
			forceLoad("com.SaintAlfonzo.creativecommon.damage.DamageFlag");
			forceLoad("com.SaintAlfonzo.creativecommon.damage.DamageTags");
			forceLoad("com.SaintAlfonzo.creativecommon.damage.DamageTagSnapshot");

			// The liquid XP bridge diagnostic, the dragon breathing fan type and the IE hammer blacklist: Creative
			// Engineering. Salt ticks and Corn Delight villager food: Creative Delight. Respite potted tea bush
			// and the KubeJS can action: Creative Delight.

			LOGGER.info("CreativeCommon setup complete");
	}

	/** Define a class now (with linking) so it exists before any late Mixin transform needs it. */
	private void forceLoad(String className) {
		try {
			Class.forName(className, true, CreativeCommon.class.getClassLoader());
		} catch (Throwable t) {
			LOGGER.error("Failed to pre-load {}", className, t);
		}
	}

	// -------------------------------------------------------------------------
	// The IE recycling diagnostics (on ServerStartedEvent) live in Creative Devtools
	// (ie/IeRecyclingDiagnostics, feature_writeup/creative_devtools.md 2). The prefixToIngotMap override itself is
	// IECompat.onServerAboutToStart.
	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------
	// ClientModEvents — MOD bus, CLIENT side only
	// -------------------------------------------------------------------------
	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents
	{
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event)
		{
			// Detail Armor Bar compat: Creative Equipment. Lunchbox looks, cob pipe renderer and
			// the oven screen: Creative Delight.
			MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.trader.client.TradeSyncClient.class);
			MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.client.PermanentEffectsEvents.class);
		}

		// Stepstool key, tier shield item properties: Creative Equipment.

		// Lunchbox colors (onItemColors): Creative Delight.

		// Shrapnel renderer: Creative Engineering.
	}
}
