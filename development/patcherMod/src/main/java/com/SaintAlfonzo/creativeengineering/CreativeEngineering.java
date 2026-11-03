package com.SaintAlfonzo.creativeengineering;

import com.SaintAlfonzo.creativeengineering.fluid.FluidBridge;

import com.SaintAlfonzo.creativeengineering.compat.IECompat;
import com.SaintAlfonzo.creativeengineering.items.CreativeEngineeringItems;
import com.SaintAlfonzo.creativeengineering.block.CreativeEngineeringBlocks;
import com.SaintAlfonzo.creativeengineering.shrapnel.CreativeEngineeringEntities;
import com.SaintAlfonzo.creativeengineering.shrapnel.TntShrapnelHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
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

@Mod(CreativeEngineering.MODID)
public class CreativeEngineering
{
    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------
    public static final String MODID = "creativeengineering";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    // -------------------------------------------------------------------------
    // Constructor — wires up event buses and registers items
    // -------------------------------------------------------------------------
    public CreativeEngineering() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        // Must be registered here, during construction - Forge loads every mod's COMMON/CLIENT
        // config immediately after every mod's constructor has run, so anything later (even
        // FMLCommonSetupEvent) is too late and the file is never read.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CEConfig.COMMON_SPEC, "creativeengineering-common.toml");

        CreativeEngineeringItems.register(modEventBus);
				CreativeEngineeringBlocks.register(modEventBus);
				CreativeEngineeringEntities.register(modEventBus);
				com.SaintAlfonzo.creativeengineering.fan.DragonBreathingRecipeType.register(modEventBus);
				com.SaintAlfonzo.creativeengineering.gear.ElytraDuplicationSerializers.register(modEventBus);
        // Tier shields (copper..dragon, iron = the vanilla shield): the items here, the block-check
        // widening in mixin/LivingEntityShieldMixin, everything tunable in CEConfig.SHIELDS.
        com.SaintAlfonzo.creativeengineering.shield.ShieldItems.register(modEventBus);
        // Wooden bucket: water-only early bucket (bucket/WoodenBucketItems).
        com.SaintAlfonzo.creativeengineering.bucket.WoodenBucketItems.register(modEventBus);
        // Oven, powdered sugar, Display Delight dishes: Creative Delight (2026-11-01, creative_delight.md 34).
        // Botanist workbench pot and flower box plant models moved to Creative Compat (2026-11-01).
        // Backpack skins: smithing + clearing recipes, Curios equip block (backpackskin/; the looks are mixin/BackpackSkin*).
        com.SaintAlfonzo.creativeengineering.backpackskin.BackpackSkinSerializers.register(modEventBus);
        if (net.minecraftforge.fml.ModList.get().isLoaded("curios")) {
            MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.backpackskin.BackpackSkinCurios.class);
            // Hands, face, feet Curios slots by IMC (compat/CuriosSlots; feature_writeup/curios_slots.md).
            modEventBus.addListener(com.SaintAlfonzo.creativeengineering.compat.CuriosSlots::enqueue);
        }
        // Spice of Life lunchboxes: Creative Delight (2026-11-01).
        // Stepstools: stand a block higher on the stool in the Curios feet slot (stepstool/; the client half is
        // stepstool/client, the collision mixin.EntityStepstoolCollisionMixin; feature_writeup/stepstool.md).
        com.SaintAlfonzo.creativeengineering.stepstool.Stepstool.register();
        if (net.minecraftforge.fml.ModList.get().isLoaded("curios")) {
            MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.stepstool.Stepstool.class);
        }
        // Corn Delight cob pipe: Creative Delight (2026-11-01).
        // Swap lists (swap/, Common API): every jar's data/<modid>/swaps/*.json remaps ids it moved when a world loads.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.swap.SwapRemapper.class);
        // Item merges in the lists ("merges", both items still registered): stacks in the world become the survivor
        // (swap/ItemMerges, triggers in creativeengineering-swaps.toml; feature_writeup/item_merges.md 6).
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, com.SaintAlfonzo.creativeengineering.swap.SwapConfig.SPEC, "creativeengineering-swaps.toml");
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.swap.ItemMerges.class);
        // Remainder buffer (cooking/): Creative Delight (2026-11-01); the WTHIT line reads compat.RemainderView.
        // Cooking for Blockheads corner storage and counter toppers moved to Creative Delight's blockheadsAdditions (2026-10-31).
        // Armor Trims' item lists cached once the registry locks; the trimmable list resets on tag reload (perf.md 10.2 #1).
        com.SaintAlfonzo.creativeengineering.compat.ArmorTrimsItemCache.init();
        // Sacks as bundles moved to Creative Compat (2026-11-01).
        // Emu / duck / goose nests moved to their own mod: development/incubationAdditions.

        // Register this class for FORGE bus events (e.g. onServerStarted, attachCaps)
        MinecraftForge.EVENT_BUS.register(this);

        // Register IECompat for FORGE bus events (e.g. onServerAboutToStart)
        MinecraftForge.EVENT_BUS.register(IECompat.class);

        // Polymorph in the Sophisticated crafting upgrade, IE and Pretty Pipes tables moved to Creative Compat (2026-11-01).

        // Potions, effects, potion machines, the alcohol tooltip and copper poisoning moved to Creative Brewing
        // (2026-11-01, creative_brewing.md 4.2 phases 2 and 3).

        // Elytra air brake velocity effect (the item itself is registered in KubeJS -
        // startup_scripts/items/air_brake.js) - see CEConfig.AirBrake for the tuning knobs.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.gear.AirBrakeHandler.class);

        // TNT shrapnel on primed-TNT-entity detonation (the dropped-TNT-item side lives in
        // Creative Compat's itemphysic.ItemBurnTransform instead - see CEConfig.Shrapnel for the tuning knobs).
        MinecraftForge.EVENT_BUS.register(TntShrapnelHandler.class);
        // The same burst for Creative Compat's burn-explosion rule (itemphysic.ItemBurnTransform), through util.BurnBursts.
        com.SaintAlfonzo.creativeengineering.util.BurnBursts.register(com.SaintAlfonzo.creativeengineering.shrapnel.ShrapnelSpawner::burst);

        // Vine shearing moved to Creative Compat (2026-11-01).

        // Live whistling arrows, so mixin.NeaWhistleGoalSkipMixin skips NEA's per-mob arrow query when there are none.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.perf.WhistleArrowTracker.class);

        // Bottled Air port (item.BottledAir, CEConfig [bottled_air]): glass bottle right-clicked in water refills air.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.item.BottledAir.class);

        // Tier shields: per-category damage absorption, scaled wear and on-block potion effects
        // (ShieldBlockEvent listener - see CEConfig.Shields).
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.shield.ShieldBlockHandler.class);

        // Iron tier: folds the player's real vanilla shield into the tier system (ShieldTier.IRON,
        // no new item). Blocking/reduction/wear/effects are handled by the same listener above via
        // ShieldTier#of; this one just gives that item the same tooltip lines, since it has no
        // TierShieldItem#appendHoverText of its own to show them from.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.shield.IronShieldTooltip.class);

        // Multi-serving cans, seasonings and placed-food Spice of Life: Creative Delight (2026-11-01).

        // Framed / copycat blocks: say on BlockRunner's speed tooltip line whether they walk like
        // their camo (surface.CamoSpeed, CEConfig.Surface.Camouflage).
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.surface.CamoSpeedTooltip.class);

        // Per-hoe Quark harvest area and its tooltip: Creative Delight (2026-11-01).

        // BlockRunner tier speeds come from CEConfig [surface.speed] (mixin/BlockRunnerSpeedConfigMixin):
        // re-apply them when the toml is saved, and log an error if the mixin never ran.
        // [time], seasons and crop seasons (/cetime): Creative Calendar (development/creativeCalendar), carved 2026-11-01.

        modEventBus.addListener(com.SaintAlfonzo.creativeengineering.surface.SurfaceSpeeds::onConfigReload);
        modEventBus.addListener(com.SaintAlfonzo.creativeengineering.util.ModDisplayNames::onConfigReload);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.surface.SurfaceSpeeds.class);

        // Damage tags: data/<ns>/damage_tags/*.json reload listener (backs shield.ShieldCategory and
        // any future armour/resistance logic) + the optional "seen sources" discovery log.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.damage.DamageTagLoader.class);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.damage.DamageTagLogger.class);

        // World generation and fauna (structures, locate, earth and water fill, terrain adaptation, meta structures,
        // sky islands, bee nests, mob spawns, mooshrooms, named mobs, leaf decay, the biome_tag loot condition, the
        // Farmer's Structures donor, Forgiving Void) moved to Creative Worldgen (2026-11-02, creative_split.md 7.6).
        // Quark monster boxes as vaults moved to Creative Compat (2026-11-01).

        // /ceplace (MassPlacer) moved to Creative Devtools (feature_writeup/creative_devtools.md 2).

        // Python generators (/cegen) and the other dev tools moved to Creative Devtools
        // (feature_writeup/creative_devtools.md).

        // Fire an early presence check so the log clearly shows, at startup,
        // whether the target fluid was actually found - rather than only
        // discovering a missing-dependency problem the first time a player
        // right-clicks a Memory Stick.
        //FluidBridge.logStartupDiagnostic();
        TierSortingRegistry.registerTier(
					  CETiers.COPPER,
					  new ResourceLocation("creativeengineering", "copper"),
					  List.of(Tiers.STONE),   // this tier is after stone
					  List.of(Tiers.IRON)     // and before iron
				);
    }

    // -------------------------------------------------------------------------
    // commonSetup — runs during mod loading, calls IECompat.register()
    // -------------------------------------------------------------------------
	private void commonSetup(final FMLCommonSetupEvent event)
	{
			LOGGER.info("CommonSetup starting");

			IECompat.register();

			// [mod_names]: mod display names in the pack's style, before JEI/WTHIT cache them
			com.SaintAlfonzo.creativeengineering.util.ModDisplayNames.apply();

			// SawmillProcessMixin applies lazily on the first sawmill tick (minutes into
			// a session). ModLauncher's Mixin bytecode provider cannot locate classes from
			// this jar that have never been loaded when it does a late transform, so the
			// helper + duck classes the mixin references must be defined up front here or
			// the mixin apply throws ClassMetadataNotFoundException and crashes the game.
			forceLoad("com.SaintAlfonzo.creativeengineering.sawmill.SawbladeRouting");
			forceLoad("com.SaintAlfonzo.creativeengineering.sawmill.duck.SawmillProcessExt");
			forceLoad("com.SaintAlfonzo.creativeengineering.sawmill.duck.SawmillRecipeExt");

			// The shield block handler reaches the damage-tag classes only on the first block
			// (crash 2026-09-24: NoClassDefFoundError on DamageFlag); pre-load them too.
			forceLoad("com.SaintAlfonzo.creativeengineering.damage.DamageFlag");
			forceLoad("com.SaintAlfonzo.creativeengineering.damage.DamageTags");
			forceLoad("com.SaintAlfonzo.creativeengineering.damage.DamageTagSnapshot");

			event.enqueueWork(FluidBridge::logStartupDiagnostic);
			event.enqueueWork(com.SaintAlfonzo.creativeengineering.fan.DragonBreathingFanProcessingType::register);
			event.enqueueWork(com.SaintAlfonzo.creativeengineering.compat.NockRecipeBookSync::register);
			// Salt ticks and Corn Delight villager food: Creative Delight (2026-11-01).
			if (net.minecraftforge.fml.ModList.get().isLoaded("immersiveengineering")) {
				event.enqueueWork(IECompat::registerHammerNoRotate);
			}
			// Respite potted tea bush and the KubeJS can action: Creative Delight (2026-11-01).

			LOGGER.info("CreativeEngineering setup complete");
	}

	/** Define a class now (with linking) so it exists before any late Mixin transform needs it. */
	private void forceLoad(String className) {
		try {
			Class.forName(className, true, CreativeEngineering.class.getClassLoader());
		} catch (Throwable t) {
			LOGGER.error("Failed to pre-load {} - sawmill blade routing may crash on use", className, t);
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

            com.SaintAlfonzo.creativeengineering.compat.DetailArmorBarCompat.register();
            com.SaintAlfonzo.creativeengineering.shield.client.TierShieldClient.setup(event);
            // Lunchbox looks, cob pipe renderer and the oven screen: Creative Delight (2026-11-01).
            if (net.minecraftforge.fml.ModList.get().isLoaded("curios")) {
                MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.stepstool.client.StepstoolClient.class);
                // "Slot:" line on every Curios item, also where the item's mod hides it (ui/tooltips #8).
                MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.compat.CuriosSlotTooltip.class);
            }
        }

        @SubscribeEvent
        public static void onRegisterKeys(net.minecraftforge.client.event.RegisterKeyMappingsEvent event)
        {
            com.SaintAlfonzo.creativeengineering.stepstool.client.StepstoolClient.onRegisterKeys(event);
        }

        // Lunchbox colours (onItemColors): Creative Delight (2026-11-01).

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerEntityRenderer(CreativeEngineeringEntities.SHRAPNEL.get(),
                    context -> new ThrownItemRenderer<>(context, 1.0F, false));
        }
    }
}
