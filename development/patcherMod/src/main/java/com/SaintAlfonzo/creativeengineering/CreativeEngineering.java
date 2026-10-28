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

// IE API imports - only used in onServerStarted for diagnostics
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.utils.TagUtils;

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
        modEventBus.addListener(com.SaintAlfonzo.creativeengineering.worldgen.donor.FarmersDonor::onAddPackFinders);

        // Must be registered here, during construction - Forge loads every mod's COMMON/CLIENT
        // config immediately after every mod's constructor has run, so anything later (even
        // FMLCommonSetupEvent) is too late and the file is never read.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CEConfig.COMMON_SPEC, "creativeengineering-common.toml");

        CreativeEngineeringItems.register(modEventBus);
				CreativeEngineeringBlocks.register(modEventBus);
				CreativeEngineeringEntities.register(modEventBus);
				com.SaintAlfonzo.creativeengineering.fan.DragonBreathingRecipeType.register(modEventBus);
				com.SaintAlfonzo.creativeengineering.gear.ElytraDuplicationSerializers.register(modEventBus);
				com.SaintAlfonzo.creativeengineering.potion.CEPotions.register(modEventBus);
				// creativeengineering:biome_tag loot condition (loot/BiomeTagCondition; the fishing net tables use it).
				com.SaintAlfonzo.creativeengineering.loot.BiomeTagCondition.register(modEventBus);
        // Tier shields (copper..dragon, iron = the vanilla shield): the items here, the block-check
        // widening in mixin/LivingEntityShieldMixin, everything tunable in CEConfig.SHIELDS.
        com.SaintAlfonzo.creativeengineering.shield.ShieldItems.register(modEventBus);
        // Meta structures: the creativeengineering:meta structure type and its two piece types
        // (worldgen/MetaStructure; the structures themselves are kubejs meta_structures.json).
        com.SaintAlfonzo.creativeengineering.worldgen.MetaStructure.register(modEventBus);
        // Oven: FD cooking-pot style baking block + the creativeengineering:baking recipe type
        // (oven/ package; feature_writeup/oven.md). Recipes and tags are KubeJS.
        com.SaintAlfonzo.creativeengineering.oven.OvenRegistry.register(modEventBus);
        // Powdered sugar block + bucket (food/PowderedSugarRegistry; TODO dishes/ingredients #16). Item and recipes are KubeJS.
        com.SaintAlfonzo.creativeengineering.food.PowderedSugarRegistry.register(modEventBus);
        // Display Delight placed blocks for foods it has none for (compat/DisplayDelightDishes; food_families.md egg/dish).
        com.SaintAlfonzo.creativeengineering.compat.DisplayDelightDishes.register(modEventBus);
        // Backpack skins: smithing + clearing recipes, Curios equip block (backpackskin/; the looks are mixin/BackpackSkin*).
        com.SaintAlfonzo.creativeengineering.backpackskin.BackpackSkinSerializers.register(modEventBus);
        if (net.minecraftforge.fml.ModList.get().isLoaded("curios")) {
            MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.backpackskin.BackpackSkinCurios.class);
        }
        // Corn Delight 1.2.11 on 1.0.3: Cob Pipe right click to equip (compat/CornDelightCompat; corn_delight.md 2.3)
        if (net.minecraftforge.fml.ModList.get().isLoaded("corn_delight")) {
            MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.compat.CornDelightCompat.class);
        }
        // Remainder buffer: pot/kettle/oven keep ingredient leftovers in two slots instead of throwing them
        // (cooking/ package; the pot and kettle mixins are Creative Delight QoL-1; remainder_buffer.md). Saved as an attached cap.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.cooking.RemainderBufferStorage.class);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.compat.CfbCornerStorage.class);
        // Cutting board, plates, coaster on CfB counters: the per-state topper cache resets on tag reload.
        com.SaintAlfonzo.creativeengineering.compat.CounterToppers.init();
        // Armor Trims' item lists cached once the registry locks; the trimmable list resets on tag reload (perf.md 10.2 #1).
        com.SaintAlfonzo.creativeengineering.compat.ArmorTrimsItemCache.init();
        // Sacks work like bundles in inventory screens (compat.SackBundle, mixin.SupplementariesSackItemMixin):
        // the scroll-to-rotate packet. The scroll listener is compat.SackBundleScroll (client).
        com.SaintAlfonzo.creativeengineering.compat.SackBundle.register();
        // Emu / duck / goose nests moved to their own mod: development/incubationAdditions.

        // Register this class for FORGE bus events (e.g. onServerStarted, attachCaps)
        MinecraftForge.EVENT_BUS.register(this);

        // Register IECompat for FORGE bus events (e.g. onServerAboutToStart)
        MinecraftForge.EVENT_BUS.register(IECompat.class);

        // Polymorph recipe selection in the Sophisticated crafting upgrade, the IE Engineer's
        // Crafting Table and the Pretty Pipes Crafting Terminal (server half)
        if (net.minecraftforge.fml.ModList.get().isLoaded("polymorph")) {
            MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.compat.SophisticatedPolymorphCompat.class);
            MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.compat.CraftingMenuPolymorphCompat.class);
        }

        // Damage-reduction half of creativeengineering:magic_resistance (the MobEffect
        // itself is registered in KubeJS - startup_scripts/magic/magic_resistance.js)
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.potion.MagicResistanceEffect.class);

        // Elytra air brake velocity effect (the item itself is registered in KubeJS -
        // startup_scripts/items/air_brake.js) - see CEConfig.AirBrake for the tuning knobs.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.gear.AirBrakeHandler.class);

        // TNT shrapnel on primed-TNT-entity detonation (the dropped-TNT-item side lives in
        // itemphysic.ItemBurnTransform instead - see CEConfig.Shrapnel for the tuning knobs).
        MinecraftForge.EVENT_BUS.register(TntShrapnelHandler.class);

        // Leaf decay (CEConfig [leaf_decay]): Fast Leaf Decay's air-neighbour trigger, ported in; the range and
        // timing rules are mixin.LeavesDecayRangeMixin + block.LeafDecayRange.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.block.FastLeafDecayPort.class);

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

        // Multi-serving cans (food.CanServings, CEConfig [canning]): max-servings cache cleared on tag
        // reload + the "Servings: x / y" tooltip line. Eating and the bar are mixin.ItemStackCanServingsMixin.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.food.CanServings.class);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.food.CanServingsTooltip.class);

        // Pack seasonings (food.Seasonings, defined by KubeJS development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/seasonings.js): the
        // creativeengineering:seasoning craft, the bonus on eating and the per-seasoning tooltip lines.
        com.SaintAlfonzo.creativeengineering.food.SeasoningRecipe.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.food.Seasonings.class);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.food.SeasoningTooltip.class);
        if (net.minecraftforge.fml.ModList.get().isLoaded("appleskin")) {
            MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.compat.AppleSkinSeasonings.class);
        }

        // Bites from placed pies and cakes count for Spice of Life (food.PlacedFoodSol; the bite itself is
        // mixin.ServerPlayerGameModePlacedFoodMixin). This loads its cake -> slice map at server start.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.food.PlacedFoodSol.class);

        // Framed / copycat blocks: say on BlockRunner's speed tooltip line whether they walk like
        // their camo (surface.CamoSpeed, CEConfig.Surface.Camouflage).
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.surface.CamoSpeedTooltip.class);

        // Per-hoe Quark harvest area (crops.HoeHarvestArea, #creativeengineering:hoe_harvest/NxN tags): the
        // "Harvest area: 3x3" tooltip. The area itself is mixin.QuarkHoeAreaBreakMixin / QuarkHoeAreaHarvestMixin.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.crops.HoeHarvestTooltip.class);

        // BlockRunner tier speeds come from CEConfig [surface.speed] (mixin/BlockRunnerSpeedConfigMixin):
        // re-apply them when the toml is saved, and log an error if the mixin never ran.
        // [time] master (feature_writeup/time.md): pushes into Serene Seasons / SeasonHUD at server start and
        // on every toml save (the Vinery port reads time/WineTime itself), the sun-driven season counter, and /cetime. Calendar tooltip: time/TimeClient (client).
        modEventBus.addListener(com.SaintAlfonzo.creativeengineering.time.TimePush::onConfigReload);
        modEventBus.addListener(com.SaintAlfonzo.creativeengineering.time.TimePresets::onConfigLoading);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.time.TimePush.class);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.time.SeasonClock.class);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.time.TimeCommand.class);

        modEventBus.addListener(com.SaintAlfonzo.creativeengineering.surface.SurfaceSpeeds::onConfigReload);
        modEventBus.addListener(com.SaintAlfonzo.creativeengineering.util.ModDisplayNames::onConfigReload);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.surface.SurfaceSpeeds.class);

        // Damage tags: data/<ns>/damage_tags/*.json reload listener (backs shield.ShieldCategory and
        // any future armour/resistance logic) + the optional "seen sources" discovery log.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.damage.DamageTagLoader.class);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.damage.DamageTagLogger.class);

        // Jigsaw pool tuning (village houses etc.: pool_config.json / pool_tags.json) + the loot
        // container swaps that let Lootr convert modded structure chests. ServerAboutToStartEvent,
        // LOWEST priority, so every mod has injected its village pieces first.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.PoolTuner.class);

        // Structure groups: per-structure frequency inside a structure_set, staged by
        // kubejs/server_scripts/worldgen/structure_overrides.js and frozen at ServerAboutToStartEvent.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.StructureFrequencies.class);
        // Meta structures with an anchor take over slots their anchors won (worldgen/MetaSlots); its index is per world.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.MetaSlots.class);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.PlacementPriority.class);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.TemplateBlockSwaps.class);

        // Mob spawn tuning: biome modifier type creativeengineering:tune_spawns (mob_spawns.json, written by
        // kubejs/server_scripts/worldgen/mob_spawns.js); its change record feeds /dumpspawns and is cleared on stop.
        com.SaintAlfonzo.creativeengineering.worldgen.SpawnTuning.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.SpawnTuning.class);
        // Brown mooshrooms on natural spawns (mob_spawns.json brown_chance); the family's breeding is in mixins.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.mooshroom.MooshroomFamily.class);

        // Bee nest tuning: biome modifier type creativeengineering:bee_nests (bee_nests.json, written by
        // kubejs/server_scripts/worldgen/bee_nests.js) + the tree/nest/chunk counters behind /dumpbeenests.
        com.SaintAlfonzo.creativeengineering.worldgen.BeeNests.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.BeeNests.class);

        // Structure biome rules (biome_rules in structure_groups.json): only allowed structures start in
        // the water biomes. Staged by structure_overrides.js, frozen at ServerAboutToStartEvent.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.StructureBiomeRules.class);

        // Biome fits (biome_fits.json + the tower/* structure tags): a structure's own biome list replaced by the
        // fit's biome tags (mixin/StructureBiomesMixin). Staged by structure_overrides.js, frozen at
        // ServerAboutToStartEvent (HIGHEST, before the biome rules read the lists).
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.StructureBiomeFits.class);

        // Structure bases that don't match the ground (grass pads on volcanic peaks...): swapped for the
        // sampled local ground as templates place (mixin/StructureTemplateGroundMatchMixin). Registered for
        // its per-world sample cache reset; knobs in CEConfig [worldgen.ground_match].
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.GroundMatch.class);

        // Earth fill: cancels a structure start whose template foundations would sit over too much water or
        // air, and a water structure whose water would replace too much dry ground or air
        // (mixin/ChunkGeneratorEarthFillMixin). Registered to clear its template caches; knobs in
        // CEConfig [worldgen.earth_fill] and [worldgen.water_fill].
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.EarthFill.class);

        // Structures no biome of a dimension can hold are not tried there (ChunkGeneratorStructureFrequencyMixin);
        // registered to clear its cache. On/off: [worldgen.systems] dimensionFilter.
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.DimensionFilter.class);

        // Terrain adaptation by structure tag (none / bury / beard_thin / beard_box / the soft beard) and template trim
        // (air and bare pad around the built columns not placed): mixin/StructureTerrainAdaptationMixin,
        // BeardifierSoftMixin and StructureTemplateGroundMatchMixin. Registered for the tag table and the template
        // caches; knobs in CEConfig [worldgen.terrain_adaptation].
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.TerrainAdaptation.class);
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.TemplateTrim.class);

        // /ceplace: every structure or template matching a filter, laid out in rows (flat-world testing);
        // knobs in CEConfig [worldgen.mass_place].
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.MassPlacer.class);

        // /celocate batch | census | stats: structure-search measurements on Async Locator's thread pool;
        // the search prediction folds, miss logging and non-blocking loads are mixins. CEConfig [worldgen.locate].
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeengineering.worldgen.LocateCommand.class);

        // Python generators (tools/generators.json): /cegen registers itself on the FORGE bus; these two
        // run them at startup when CEConfig [debug.generators] runAtStartup is on, finishing before any
        // datapack loads.
        modEventBus.addListener(com.SaintAlfonzo.creativeengineering.devtools.GeneratorCommand::onCommonSetup);
        modEventBus.addListener(com.SaintAlfonzo.creativeengineering.devtools.GeneratorCommand::onLoadComplete);

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

			// ItemPhysicServerMixin applies when ItemPhysic first loads its server helper; pre-load
			// the split registry it references so a late transform can resolve it.
			forceLoad("com.SaintAlfonzo.creativeengineering.itemphysic.ItemPhysicSplit");

			// The shield block handler reaches the damage-tag classes only on the first block
			// (crash 2026-09-24: NoClassDefFoundError on DamageFlag); pre-load them too.
			forceLoad("com.SaintAlfonzo.creativeengineering.damage.DamageFlag");
			forceLoad("com.SaintAlfonzo.creativeengineering.damage.DamageTags");
			forceLoad("com.SaintAlfonzo.creativeengineering.damage.DamageTagSnapshot");

			// ForgivingVoidMixin applies lazily on the first void-fall bounce; pre-load the
			// config bridge it references so a late transform can resolve it.
			forceLoad("com.SaintAlfonzo.creativeengineering.forgivingvoid.ForgivingVoidBridge");
			LOGGER.info("[forgiving-void] {}", com.SaintAlfonzo.creativeengineering.forgivingvoid.ForgivingVoidBridge.summary());

			event.enqueueWork(FluidBridge::logStartupDiagnostic);
			event.enqueueWork(com.SaintAlfonzo.creativeengineering.fluidbrewing.DragonBreathBrewing::build);
			event.enqueueWork(com.SaintAlfonzo.creativeengineering.fluidbrewing.Lingerification::registerListGenerator);
			event.enqueueWork(com.SaintAlfonzo.creativeengineering.fan.DragonBreathingFanProcessingType::register);
			event.enqueueWork(com.SaintAlfonzo.creativeengineering.potion.PotionBucketDispenser::register);
			event.enqueueWork(com.SaintAlfonzo.creativeengineering.compat.NockRecipeBookSync::register);
			// Vintage's salt blocks melt ice and dissolve like The Salt's (item_merges.md 2.9)
			event.enqueueWork(com.SaintAlfonzo.creativeengineering.block.SaltBlockTicks::init);
			// Corn is villager food (corn_delight.md 2.6)
			if (net.minecraftforge.fml.ModList.get().isLoaded("corn_delight")) {
				event.enqueueWork(com.SaintAlfonzo.creativeengineering.compat.CornDelightCompat::addVillagerFood);
			}
			if (net.minecraftforge.fml.ModList.get().isLoaded("immersiveengineering")) {
				event.enqueueWork(IECompat::registerHammerNoRotate);
			}
			// Farmer's Respite 2.0 registers potted_wild_tea_bush but never adds it to the flower pot
			// (feature_writeup/farmers_respite.md 2.2)
			if (net.minecraftforge.fml.ModList.get().isLoaded("farmersrespite")) {
				event.enqueueWork(com.SaintAlfonzo.creativeengineering.compat.RespitePottedTeaBush::register);
			}
			// Can-serving crafting action for KubeJS recipes (NCH re-registers it on every server
			// script load; this covers clients of a dedicated server).
			if (net.minecraftforge.fml.ModList.get().isLoaded("kubejs")) {
				event.enqueueWork(com.SaintAlfonzo.creativeengineering.compat.KubeJSCanAction::register);
			}

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
    // onServerStarted — FORGE bus, runs after world load
    // Used ONLY for diagnostics. The actual prefixToIngotMap override
    // lives in IECompat.onServerAboutToStart, which runs BEFORE the
    // arc recycling profiler bakes its cache.
    // -------------------------------------------------------------------------
    @SubscribeEvent
    public void onServerStarted(net.minecraftforge.event.server.ServerStartedEvent event)
    {
        net.minecraft.core.RegistryAccess registryAccess = event.getServer().registryAccess();
        var itemRegistry = registryAccess.registryOrThrow(Registry.ITEM_REGISTRY);

        // --- 1. Dump the current prefixToIngotMap so we can confirm our values stuck ---
        LOGGER.info("=== prefixToIngotMap at server start ===");
        IEApi.prefixToIngotMap.forEach((k, v) ->
                LOGGER.info("  {} -> {}", k, java.util.Arrays.toString(v)));

        // --- 2. Check that forge:ingots/<metal> tags resolve to real items ---
        LOGGER.info("=== forge:ingots tag resolution ===");
        String[] ingotsToTest = {"iron", "copper", "gold", "zinc", "steel",
                                 "lead", "uranium", "constantan", "nickel", "aluminum"};
        for (String metal : ingotsToTest) {
            var ingotTag = TagKey.create(Registry.ITEM_REGISTRY,
                    new ResourceLocation("forge", "ingots/" + metal));
            var items = TagUtils.elementStream(itemRegistry, ingotTag).toList();
            LOGGER.info("  forge:ingots/{} contains: {}", metal, items);
        }

        // --- 3. Test IETags.getIngot + getPreferredTagStack for each metal ---
        // This is the exact internal call breakStackIntoPreciseIngots makes.
        // If a metal returns air/empty here, that's why it recycles to nothing.
        LOGGER.info("=== Testing getPreferredTagStack for each metal ===");
        for (String metal : ingotsToTest) {
            try {
                // IETags.getIngot returns a ResourceLocation, not a TagKey
                ResourceLocation ingotRL = IETags.getIngot(metal);
                LOGGER.info("  IETags.getIngot({}) = {}", metal, ingotRL);

                // createItemWrapper needs a TagKey, so wrap the ResourceLocation
               // CORRECT - pass the ResourceLocation directly
								var wrapper = TagUtils.createItemWrapper(ingotRL);

                var preferred = IEApi.getPreferredTagStack(registryAccess, wrapper);
                LOGGER.info("  getPreferredTagStack({}) = {}", metal, preferred);
            } catch (Exception e) {
                LOGGER.error("  Error testing getPreferredTagStack for {}: {}", metal, e.getMessage());
            }
        }

        // --- 4. Run breakStackIntoPreciseIngots on specific items ---
        // Tells us the final recycling output IE would calculate for each item.
        LOGGER.info("=== breakStackIntoPreciseIngots per item ===");
        String[][] testItems = {
            {"immersiveengineering", "stick_iron",       "IE Iron Rod"},
            {"immersiveposts",       "stick_copper",     "IP Copper Rod"},
            {"immersiveposts",       "stick_lead",       "IP Lead Rod"},
            {"immersiveposts",       "stick_uranium",    "IP Uranium Rod"},
            {"immersiveposts",       "stick_constantan", "IP Constantan Rod"},
            {"immersiveposts",       "stick_nickel",     "IP Nickel Rod"},
            {"immersiveposts",       "stick_gold",       "IP Gold Rod"},
            {"immersiveengineering", "stick_aluminum",   "IE Aluminum Rod"},
            {"creativeengineering",  "iron_wire",        "CE Iron Wire"},
            {"creativeengineering",  "gold_wire",        "CE Gold Wire"},
            {"creativeengineering",  "zinc_wire",        "CE Zinc Wire"},
            {"immersiveengineering", "wire_copper",      "IE Copper Wire"},
            {"minecraft",            "iron_ingot",       "Vanilla Iron Ingot"},
            {"immersiveengineering", "ingot_iron",       "IE Iron Ingot"},
            {"vintageimprovements", "zinc_rod", "VI Zinc Rod"},
        };

        for (String[] entry : testItems) {
            String namespace = entry[0];
            String path      = entry[1];
            String label     = entry[2];

            LOGGER.info("  --- {} ({}) ---", label, namespace + ":" + path);

            var item = ForgeRegistries.ITEMS
                    .getValue(new ResourceLocation(namespace, path));
            if (item == null || item == net.minecraft.world.item.Items.AIR) {
                LOGGER.warn("    Item not found in registry, skipping");
                continue;
            }

            ItemStack stack = new ItemStack(item);
            LOGGER.info("    Stack: {}", stack);
            stack.getTags().forEach(tag -> LOGGER.info("    Tag: {}", tag.location()));

            var result = blusunrize.immersiveengineering.api.ApiUtils
                    .breakStackIntoPreciseIngots(registryAccess, stack);
            if (result != null) {
                LOGGER.info("    breakStackIntoPreciseIngots: {} x {}",
                        result.getFirst(), result.getSecond());
            } else {
                LOGGER.info("    breakStackIntoPreciseIngots: null (item not recognized)");
            }
        }
        
        var zincSpringTag = TagKey.create(Registry.ITEM_REGISTRY,
								new ResourceLocation("forge", "springs/zinc"));
				var zincSmallSpringTag = TagKey.create(Registry.ITEM_REGISTRY,
								new ResourceLocation("forge", "small_springs/zinc"));
				LOGGER.info("forge:springs/zinc contains: {}", TagUtils.elementStream(itemRegistry, zincSpringTag).toList());
				LOGGER.info("forge:small_springs/zinc contains: {}", TagUtils.elementStream(itemRegistry, zincSmallSpringTag).toList());
    }

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
            if (net.minecraftforge.fml.ModList.get().isLoaded("trashslot")) {
                com.SaintAlfonzo.creativeengineering.compat.TrashSlotCompat.register();
            }
            if (net.minecraftforge.fml.ModList.get().isLoaded("polymorph")) {
                com.SaintAlfonzo.creativeengineering.compat.SophisticatedPolymorphClient.register();
                com.SaintAlfonzo.creativeengineering.compat.CraftingMenuPolymorphClient.register();
            }
            com.SaintAlfonzo.creativeengineering.shield.client.TierShieldClient.setup(event);
            if (net.minecraftforge.fml.ModList.get().isLoaded("corn_delight") && net.minecraftforge.fml.ModList.get().isLoaded("curios")) {
                com.SaintAlfonzo.creativeengineering.compat.CobPipeRenderer.register();
            }
            event.enqueueWork(() -> net.minecraft.client.gui.screens.MenuScreens.register(
                    com.SaintAlfonzo.creativeengineering.oven.OvenRegistry.OVEN_MENU.get(),
                    com.SaintAlfonzo.creativeengineering.oven.client.OvenScreen::new));
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerEntityRenderer(CreativeEngineeringEntities.SHRAPNEL.get(),
                    context -> new ThrownItemRenderer<>(context, 1.0F, false));
        }
    }
}
