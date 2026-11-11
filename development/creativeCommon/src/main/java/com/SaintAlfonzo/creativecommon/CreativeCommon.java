package com.SaintAlfonzo.creativecommon;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
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
    /** This jar's mod id since the Creative Split swap (2026-11-04, creative_split.md 6 Q1 and 7.13): patcherMod was
     *  {@code creativecommon} until then; that id is now Creative Engineering's (development/creativeEngineering). */
    public static final String MODID = "creativecommon";
    /** The namespace of every id, tag, lang key and channel this jar uses: patcherMod's, unchanged by the swap. */
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

        // Must be registered here, during construction - Forge loads every mod's COMMON/CLIENT
        // config immediately after every mod's constructor has run, so anything later (even
        // FMLCommonSetupEvent) is too late and the file is never read.
        // The tables came from patcherMod's creativeengineering-common.toml, which Creative Engineering keeps for its
        // [shrapnel], [surface] and [vein_undo]: the first boot copies the player's values over (then Engineering's
        // spec drops them from that file when Forge corrects it). The swap list toml is only renamed.
        com.SaintAlfonzo.creativecommon.swap.ConfigRename.carrySections(OLD_FILE, FILE, CommonConfig.SECTIONS);
        com.SaintAlfonzo.creativecommon.swap.ConfigRename.carryOver("creativeengineering-swaps.toml", MODID + "-swaps.toml");
        // [effects] and [magic_resistance] came from Creative Brewing's file (plan_cb_polish.md 2, 2026-11-06), and before
        // that from patcherMod's: the first boot copies the player's values in (sections this file already has are kept).
        // CB drops them from its own file when Forge corrects it.
        if (!com.SaintAlfonzo.creativecommon.swap.ConfigRename.carrySections("creativebrewing-common.toml", FILE, CommonConfig.EFFECT_SECTIONS))
            com.SaintAlfonzo.creativecommon.swap.ConfigRename.mergeSections("creativebrewing-common.toml", FILE, CommonConfig.EFFECT_SECTIONS);
        com.SaintAlfonzo.creativecommon.swap.ConfigRename.mergeSections(OLD_FILE, FILE, CommonConfig.EFFECT_SECTIONS);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.COMMON_SPEC, FILE);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CommonClientConfig.CLIENT_SPEC, "creativecommon-client.toml");

        // Every potion and mob effect the Creative mods own (plan_cb_polish.md 2, from Creative Brewing 2026-11-06):
        // the creativecommon: potions (Magic Resistance; Better Beacons potions are Creative Brewing's since 2026-11-07; the vanilla-effect ones are KubeJS,
        // kubejs_startup/creativecommon/effects/potions.js), Magic Resistance's damage cut (the MobEffect itself is
        // KubeJS, effects/magic_resistance.js) and Phantom Bane's rules.
        com.SaintAlfonzo.creativecommon.effect.CommonPotions.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.effect.MagicResistanceEffect.class);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.effect.PhantomBaneRules.class);

        // Sawblade and sawdust items, the shrapnel entity, the dragon breathing recipe type, the wooden bucket:
        // Creative Engineering (creativeengineering, 2026-11-04, creative_split.md 7.13 wave 8).
        // Dark Fire and Flint and Terrasteel: Creative Magic (creativemagic, 2026-11-04)
        // Elytra duplication, tier shields, backpack skins, stepstools, the air brake, Bottled Air, the copper tool tier
        // and the gear mixins: Creative Equipment (creativeequipment, 2026-11-04, creative_split.md 7.6 wave 7).
        // Oven, powdered sugar, Display Delight dishes: Creative Delight (2026-11-01, creative_delight.md 34).
        // Botanist workbench pot and flower box plant models moved to Creative Compat (2026-11-01).
        // Hands, face, feet Curios slots and the "Slot:" tooltip line: Creative Equipment (2026-11-04).
        // Spice of Life lunchboxes: Creative Delight (2026-11-01).
        // Trade sync: the server's villager and wandering trader lists go to dedicated-server clients, so JER's
        // villager pages show the pack's trades (trader/TradeSync; feature_writeup/trade_sync.md).
        com.SaintAlfonzo.creativecommon.trader.TradeSync.register();
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.trader.TradeSync.class);
        // Corn Delight cob pipe: Creative Delight (2026-11-01).
        // Swap lists (swap/, Common API): every jar's data/<modid>/swaps/*.json remaps ids it moved when a world loads.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.swap.SwapRemapper.class);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.swap.SwapReport.class);
        // Item merges in the lists ("merges", both items still registered): stacks in the world become the survivor
        // (swap/ItemMerges, triggers in creativecommon-swaps.toml; feature_writeup/item_merges.md 6).
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, com.SaintAlfonzo.creativecommon.swap.SwapConfig.SPEC, MODID + "-swaps.toml");
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.swap.ItemMerges.class);
        // Remainder buffer (cooking/): Creative Delight (2026-11-01); the WTHIT line reads compat.RemainderView.
        // Cooking for Blockheads corner storage and counter toppers moved to Creative Delight's blockheadsAdditions (2026-10-31).
        // Armor Trims' item lists cached once the registry locks; the trimmable list resets on tag reload (perf.md 10.2 #1).
        com.SaintAlfonzo.creativecommon.compat.ArmorTrimsItemCache.init();
        // Sacks as bundles moved to Creative Compat (2026-11-01).
        // Emu / duck / goose nests moved to their own mod: development/incubationAdditions.

        // Register this class for FORGE bus events (e.g. onServerStarted, attachCaps)
        MinecraftForge.EVENT_BUS.register(this);

        // Polymorph in the Sophisticated crafting upgrade, IE and Pretty Pipes tables moved to Creative Compat (2026-11-01).

        // Potions, effects, potion machines, the alcohol tooltip and copper poisoning moved to Creative Brewing
        // (2026-11-01, creative_brewing.md 4.2 phases 2 and 3).

        // TNT shrapnel, the IE tweaks, BlockRunner surfaces (speeds, camo tooltip, sinking): Creative Engineering
        // (2026-11-04). Its shrapnel burst reaches Compat's burn-explosion rule through util.BurnBursts (Common API).

        // Vine shearing moved to Creative Compat (2026-11-01).

        // Live whistling arrows, so mixin.NeaWhistleGoalSkipMixin skips NEA's per-mob arrow query when there are none.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.perf.WhistleArrowTracker.class);

        // Multi-serving cans, seasonings and placed-food Spice of Life: Creative Delight (2026-11-01).

        // Per-hoe Quark harvest area and its tooltip: Creative Delight (2026-11-01).

        // [time], seasons and crop seasons (/ccaltime): Creative Calendar (development/creativeCalendar), carved 2026-11-01.

        modEventBus.addListener(com.SaintAlfonzo.creativecommon.util.ModDisplayNames::onConfigReload);

        // Damage tags (Common API): data/<ns>/damage_tags/*.json reload listener (backs Creative Equipment's
        // shield.ShieldCategory and Creative Brewing's Magic Resistance) + the optional "seen sources" discovery log.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.damage.DamageTagLoader.class);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.damage.DamageTagLogger.class);

        // World generation and fauna (structures, locate, earth and water fill, terrain adaptation, meta structures,
        // sky islands, bee nests, mob spawns, mooshrooms, named mobs, leaf decay, the biome_tag loot condition, the
        // Farmer's Structures donor, Forgiving Void) moved to Creative Worldgen (2026-11-02, creative_split.md 7.6).
        // Quark monster boxes as vaults moved to Creative Compat (2026-11-01).

        // /cdevplace (MassPlacer) moved to Creative Devtools (feature_writeup/creative_devtools.md 2).

        // Python generators (/cdevgen) and the other dev tools moved to Creative Devtools
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

			// The sawmill classes pre-loaded here for SawmillProcessMixin: Creative Engineering (2026-11-04).

			// The shield block handler reaches the damage-tag classes only on the first block
			// (crash 2026-09-24: NoClassDefFoundError on DamageFlag); pre-load them too.
			forceLoad("com.SaintAlfonzo.creativecommon.damage.DamageFlag");
			forceLoad("com.SaintAlfonzo.creativecommon.damage.DamageTags");
			forceLoad("com.SaintAlfonzo.creativecommon.damage.DamageTagSnapshot");

			// The liquid XP bridge diagnostic, the dragon breathing fan type and the IE hammer blacklist: Creative
			// Engineering (2026-11-04).
			// Salt ticks and Corn Delight villager food: Creative Delight (2026-11-01).
			// Respite potted tea bush and the KubeJS can action: Creative Delight (2026-11-01).

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
    // The IE recycling diagnostics that ran here on ServerStartedEvent moved to Creative Devtools
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
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

            // Detail Armor Bar compat: Creative Equipment.
            // Lunchbox looks, cob pipe renderer and the oven screen: Creative Delight (2026-11-01).
            MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecommon.trader.client.TradeSyncClient.class);
        }

        // Stepstool key, tier shield item properties: Creative Equipment (2026-11-04).

        // Lunchbox colours (onItemColors): Creative Delight (2026-11-01).

        // Shrapnel renderer: Creative Engineering (2026-11-04).
    }
}
