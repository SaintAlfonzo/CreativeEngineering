# Sweep D: registration hub

Audit run 2026-11-01, read-only, for [creative_split.md](../creative_split.md) section 4. Targets use the words of
[README.md](README.md). `PM` = `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/`; line numbers
are those of `PM/CreativeEngineering.java` (576 lines) on 2026-11-01. `?` = doubtful, note on the row.
**Mixed** in the note column = the block wires classes of two or more targets and must be split by row.

## D.1 `CreativeEngineering.java` block by block

### D.1.1 Constructor (lines 71 to 335)

| lines | what it does | target jar | the class it wires (its target) | note |
|---|---|---|---|---|
| 62-66 | `MODID = "creativeengineering"`; `BLOCKS` DeferredRegister (Block, ns `creativeengineering`); only `block/CreativeEngineeringBlocks` writes to it | Worldgen | `block/CreativeEngineeringBlocks` (Worldgen: `dark_fire` is the portal block of `dimension/`) | The holder sits in the main class; moves to the jar that owns `dark_fire` |
| 72-73 | mod bus: `addListener(this::commonSetup)` | Common | `commonSetup` (see D.1.2, **Mixed**) | Every jar keeps its own `commonSetup` |
| 74 | mod bus: `FarmersDonor::onAddPackFinders` (Farmer's Structures donor jar read as a pack) | Worldgen | `worldgen/donor/FarmersDonor` (Worldgen) | `AddPackFindersEvent` must stay in the constructor |
| 79 | `registerConfig(COMMON, CEConfig.COMMON_SPEC, "creativeengineering-common.toml")` | Common | `CEConfig` (**Mixed**: sweep C splits it per jar) | Each jar registers its own `<modid>-common.toml` in its constructor (rule 3.3; the comment at 76-78 says why it cannot be later) |
| 81 | `CreativeEngineeringItems.register` | Engineering | `items/CreativeEngineeringItems` (**Mixed**: sawblade and sawdust Engineering, `flint_and_terrasteel` Magic (Equipment)) | Package is `items` (import line 6), the folder is `item/`; see D.2 |
| 82 | `CreativeEngineeringBlocks.register` -> `CreativeEngineering.BLOCKS.register(bus)` | Worldgen | `block/CreativeEngineeringBlocks` (Worldgen) | |
| 83 | `CreativeEngineeringEntities.register` (shrapnel entity) | Engineering | `shrapnel/CreativeEngineeringEntities` (Engineering) | Renderer registered in D.1.4 |
| 84 | `DragonBreathingRecipeType.register` (Create fan recipe type + serializer) | Engineering? | `fan/DragonBreathingRecipeType` (Engineering) | Fan processing turns potions lingering; its list generator (`fluidbrewing/Lingerification`, line 377) is CB. Row for the owner: fan type Engineering with a soft edge to CB, or all CB |
| 85 | `ElytraDuplicationSerializers.register` | Equipment | `gear/ElytraDuplicationSerializers` (Equipment) | |
| 86 | `CEPotions.register` | CB | `potion/CEPotions` (CB) | creative_brewing.md 4.2 phase row |
| 88 | `BiomeTagCondition.register` (loot condition `biome_tag`) | Worldgen | `loot/BiomeTagCondition` (Worldgen) | |
| 91 | `ShieldItems.register` | Equipment | `shield/ShieldItems` (Equipment) | `shield/EffectTickContext` stays Common (2.4) |
| 93 | `WoodenBucketItems.register` | Engineering | `bucket/WoodenBucketItems` (Engineering) | |
| 96 | `MetaStructure.register` (structure type + 2 piece types) | Worldgen | `worldgen/MetaStructure` (Worldgen) | |
| 99 | `OvenRegistry.register` (6 registries) | CD | `oven/OvenRegistry` (CD) | Screen in D.1.4, JEI in D.4 |
| 101 | `PowderedSugarRegistry.register` | CD | `food/PowderedSugarRegistry` (CD) | |
| 103 | `DisplayDelightDishes.register` (dish blocks from `displaydelight_dishes.json`) | CD | `compat/DisplayDelightDishes`, `DisplayDelightDishBlocks` (CD) | Reads a jar resource (sweep E) |
| 105 | `BotanistPot.register` (block entity `botanist_pot`) | Compat? | `compat/BotanistPot` (Compat?) | Beautify flower-pot tweak; not in the 2.1 Compat list, nearest fit |
| 107-109 | client only: mod bus `PlantBoxModelsClient::onRegisterAdditional` | Compat | `compat/PlantBoxModelsClient` (Compat) | Supplementaries models in Another Furniture boxes |
| 111 | `BackpackSkinSerializers.register` | Equipment | `backpackskin/BackpackSkinSerializers` (Equipment) | |
| 112-114 | if `curios`: FORGE bus `BackpackSkinCurios` | Equipment | `backpackskin/BackpackSkinCurios` (Equipment) | soft dep `curios` |
| 117-121 | if `solapplepie`: `LunchboxItems.register`, `LunchboxRecipes.register`, FORGE bus `LunchboxTooltip` | CD | `lunchbox/*` (CD) | `LunchboxItems` registers in namespace `solapplepie`, not ours (D.2); soft dep `solapplepie` |
| 124-127 | `Stepstool.register()` (network channel, D.1.5); if `curios`: FORGE bus `Stepstool` | Equipment | `stepstool/Stepstool` (Equipment) | soft dep `curios` |
| 129-131 | if `corn_delight`: FORGE bus `CornDelightCompat` | CD | `compat/CornDelightCompat` (CD) | Food mod compat follows the CD routing rule |
| 134 | FORGE bus `RemainderBufferStorage` (attached capability) | CD | `cooking/RemainderBufferStorage` (CD) | |
| 137 | static init `ArmorTrimsItemCache.init()` (adds a `TagsUpdatedEvent` listener itself) | Common | `compat/ArmorTrimsItemCache` (Common: perf) | |
| 140 | static init `SackBundle.register()` (network channel, D.1.5) | Compat | `compat/SackBundle` (Compat) | |
| 144 | FORGE bus `this` (`onServerStarted`, D.1.3) | Engineering | main class method | |
| 147 | FORGE bus `IECompat` (`onServerAboutToStart`, LOWEST: IE prefixToIngotMap override) | Engineering | `compat/IECompat` (Engineering) | |
| 151-154 | if `polymorph`: FORGE bus `SophisticatedPolymorphCompat`, `CraftingMenuPolymorphCompat` | Compat | `compat/*Polymorph*` (Compat) | Client halves in D.1.4; the IE Engineer's table part is an IE edge but stays with the Polymorph row |
| 158 | FORGE bus `MagicResistanceEffect` | CB | `potion/MagicResistanceEffect` (CB) | Effect itself is KubeJS `startup_scripts/magic` (Magic (Equipment) per 2.3; effects stay CB) |
| 162 | FORGE bus `CopperPoisoningHandler` | CB | `potion/CopperPoisoningHandler` (CB) | Copper gear roll: gear effect, CB per creative_brewing.md 4.2 |
| 165 | FORGE bus `PhantomBaneRules` | CB | `potion/PhantomBaneRules` (CB) | |
| 169 | FORGE bus `AirBrakeHandler` | Equipment | `gear/AirBrakeHandler` (Equipment) | |
| 173 | FORGE bus `TntShrapnelHandler` | Engineering | `shrapnel/TntShrapnelHandler` (Engineering) | Item-side half is `itemphysic/ItemBurnTransform` (Compat): cross-target pair |
| 177 | FORGE bus `FastLeafDecayPort` | Worldgen | `block/FastLeafDecayPort` (Worldgen: leaf decay is a 2.1 Worldgen row) | |
| 179 | FORGE bus `VineShearing` | Worldgen? | `block/VineShearing` (Worldgen?) | Plant growth tweak with Quark and IW parts; could be Compat |
| 182 | FORGE bus `WhistleArrowTracker` | Common | `perf/WhistleArrowTracker` (Common: perf) | Serves a Nock Enough Arrows mixin (archery, Equipment); perf row by Q3 |
| 185 | FORGE bus `BottledAir` | Equipment? | `item/BottledAir` (Equipment?) | Feature port, glass-bottle use; nearest fit is player gear |
| 189 | FORGE bus `ShieldBlockHandler` | Equipment | `shield/ShieldBlockHandler` (Equipment) | |
| 195 | FORGE bus `IronShieldTooltip` | Equipment | `shield/IronShieldTooltip` (Equipment) | Interface row showing one jar's data: stays with Equipment |
| 199-200 | FORGE bus `CanServings`, `CanServingsTooltip` | CD | `food/CanServings*` (CD) | |
| 204-209 | `SeasoningRecipe.register`; FORGE bus `Seasonings`, `SeasoningTooltip`; if `appleskin`: `AppleSkinSeasonings` | CD | `food/Seasoning*`, `compat/AppleSkinSeasonings` (CD) | soft dep `appleskin` |
| 213 | FORGE bus `PlacedFoodSol` | CD | `food/PlacedFoodSol` (CD) | |
| 217 | FORGE bus `CamoSpeedTooltip` | Engineering | `surface/CamoSpeedTooltip` (Engineering) | |
| 221 | FORGE bus `HoeHarvestTooltip` | CD | `crops/HoeHarvestTooltip` (CD) | |
| 227-231 | mod bus `TimePush::onConfigReload`, `TimePresets::onConfigLoading`; FORGE bus `TimePush`, `SeasonClock`, `TimeCommand` (`/cetime`) | Calendar | `time/*` (Calendar) | |
| 233, 235 | mod bus `SurfaceSpeeds::onConfigReload`; FORGE bus `SurfaceSpeeds` | Engineering | `surface/SurfaceSpeeds` (Engineering) | Block 233-235 is **Mixed** with line 234 |
| 234 | mod bus `ModDisplayNames::onConfigReload` | Common | `util/ModDisplayNames` (Common) | |
| 239-240 | FORGE bus `DamageTagLoader` (reload listener for `damage_tags/*.json`), `DamageTagLogger` | Equipment | `damage/*` (Equipment) | Backs `shield/ShieldCategory` |
| 245-255 | FORGE bus `PoolTuner`, `StructureFrequencies`, `MetaSlots`, `PlacementPriority`, `TemplateBlockSwaps`, `StructureSpawnBoxes` | Worldgen | `worldgen/*` (Worldgen) | |
| 259-260 | `SpawnTuning.register` (biome modifier `tune_spawns`); FORGE bus `SpawnTuning` | Worldgen | `worldgen/SpawnTuning` (Worldgen: fauna) | |
| 262 | FORGE bus `MooshroomFamily` | Worldgen? | `mooshroom/MooshroomFamily` (Worldgen? or CD with More Mooblooms, plan 2.2 open) | Brown chance on natural spawns reads `mob_spawns.json` (Worldgen data) |
| 264, 266 | FORGE bus `NamedMobs`, `MonsterBoxVault` | Worldgen | `mobs/*` (Worldgen: fauna) | `MonsterBoxVault` is a Quark tweak; fauna decision keeps it with `mobs/` |
| 270-271 | `BeeNests.register` (biome modifier `bee_nests`); FORGE bus `BeeNests` | Worldgen | `worldgen/BeeNests` (Worldgen) | |
| 275-312 | FORGE bus `StructureBiomeRules`, `StructureBiomeFits`, `GroundMatch`, `EarthFill`, `DimensionFilter`, `SeabedClearance`, `SkyIslands`, `TerrainAdaptation`, `TemplateTrim`, `MassPlacer` (`/ceplace`) | Worldgen | `worldgen/*` (Worldgen) | `/ceplace` is a test tool but reads Worldgen internals: stays Worldgen |
| 316 | FORGE bus `LocateCommand` (`/celocate batch, census, stats`) | Worldgen | `worldgen/LocateCommand` (Worldgen) | Measurement command on Worldgen internals |
| 321-322 | mod bus `GeneratorCommand::onCommonSetup`, `::onLoadComplete` (run `tools/generators.json` at startup) | Devtools | `devtools/GeneratorCommand` (Devtools) | `/cegen` itself is an `@EventBusSubscriber` (D.3) |
| 328 | commented-out `FluidBridge.logStartupDiagnostic()` | - | - | Dead line |
| 329-334 | `TierSortingRegistry.registerTier(CETiers.COPPER, "creativeengineering:copper", after STONE, before IRON)` | Equipment | `CETiers` (Equipment, root package) | Tier id keeps the namespace |

### D.1.2 `commonSetup` (lines 341 to 402), **Mixed**

| lines | what it does | target jar | the class it wires (its target) | note |
|---|---|---|---|---|
| 345 | `IECompat.register()` (IE prefix map setup) | Engineering | `compat/IECompat` (Engineering) | |
| 348 | `ModDisplayNames.apply()` before JEI/WTHIT cache names | Common | `util/ModDisplayNames` (Common) | |
| 355-357 | `forceLoad` sawmill `SawbladeRouting`, `duck/SawmillProcessExt`, `duck/SawmillRecipeExt` (late mixin apply) | Engineering | `sawmill/*` (Engineering) | The forceLoad helper (405-411) must be copied into every jar that has late-applying mixins |
| 361 | `forceLoad` `itemphysic/ItemPhysicSplit` | Compat | `itemphysic/ItemPhysicSplit` (Compat) | |
| 365-367 | `forceLoad` `damage/DamageFlag`, `DamageTags`, `DamageTagSnapshot` | Equipment | `damage/*` (Equipment) | |
| 371-372 | `forceLoad` + log `forgivingvoid/ForgivingVoidBridge` | Worldgen | `forgivingvoid/ForgivingVoidBridge` (Worldgen) | |
| 374 | `enqueueWork(FluidBridge::logStartupDiagnostic)` | Engineering | `fluid/FluidBridge` (Engineering) | Diagnostic only |
| 375 | `enqueueWork(PlantBoxModels::commonSetup)` | Compat | `compat/PlantBoxModels` (Compat) | |
| 376-377 | `enqueueWork(DragonBreathBrewing::build)`, `Lingerification::registerListGenerator` | CB | `fluidbrewing/*` (CB) | |
| 378 | `enqueueWork(DragonBreathingFanProcessingType::register)` (Create `FanProcessingTypeRegistry`, direct registry call) | Engineering? | `fan/DragonBreathingFanProcessingType` (Engineering?) | Same doubt as line 84 |
| 379 | `enqueueWork(PotionBucketDispenser::register)` | CB | `potion/PotionBucketDispenser` (CB) | |
| 380 | `enqueueWork(NockRecipeBookSync::register)` | Equipment? | `compat/NockRecipeBookSync` (Equipment?: archery, Nock Enough Arrows) | Engine-side desync fix; could be Common (engine fix) |
| 382 | `enqueueWork(SaltBlockTicks::init)` | Compat? | `block/SaltBlockTicks` (Compat?: Vintage Delight salt acts like The Salt's) | item_merges.md row; CD if the owner counts salt as food |
| 384-386 | if `corn_delight`: `CornDelightCompat::addVillagerFood` | CD | `compat/CornDelightCompat` (CD) | |
| 387-389 | if `immersiveengineering`: `IECompat::registerHammerNoRotate` | Engineering | `compat/IECompat` (Engineering) | |
| 392-394 | if `farmersrespite`: `RespitePottedTeaBush::register` | CD? | `compat/RespitePottedTeaBush` (CD?: Farmer's Respite is a food mod; the fix is a flower-pot entry) | Compat if the owner keys on the fix, CD if on the mod |
| 397-399 | if `kubejs`: `KubeJSCanAction::register` | CD | `compat/KubeJSCanAction` (CD: can servings crafting action) | NCH (Common) re-registers it on script load: a Common-to-CD edge for sweep G |

### D.1.3 `onServerStarted` (lines 420 to 518)

| lines | what it does | target jar | the class it wires (its target) | note |
|---|---|---|---|---|
| 420-518 | FORGE bus `ServerStartedEvent`: logs IE `prefixToIngotMap`, `forge:ingots/*` resolution, `getPreferredTagStack`, `breakStackIntoPreciseIngots` for 15 items, `forge:springs/zinc` | Engineering | main class, IE API imports (lines 52-54) | Diagnostics only; candidate for Devtools or deletion. Hard import of the IE API in the main class: the Engineering jar's main class needs `immersiveengineering` loaded or this moves behind a `ModList` check |

### D.1.4 `ClientModEvents` (lines 523 to 575), `@Mod.EventBusSubscriber(MOD, CLIENT)`, **Mixed**

| lines | what it does | target jar | the class it wires (its target) | note |
|---|---|---|---|---|
| 527-530 | `FMLClientSetupEvent`: two log lines | Common | - | |
| 532 | `DetailArmorBarCompat.register()` | Common | `compat/DetailArmorBarCompat` (Common: armor bar) | |
| 533-535 | if `trashslot`: `TrashSlotCompat.register()` | Compat | `compat/TrashSlotCompat` (Compat) | |
| 536-539 | if `polymorph`: `SophisticatedPolymorphClient`, `CraftingMenuPolymorphClient` | Compat | `compat/*PolymorphClient` (Compat) | |
| 540 | `TierShieldClient.setup(event)` | Equipment | `shield/client/TierShieldClient` (Equipment) | |
| 541-543 | if `solapplepie`: `LunchboxClient.setup(event)` | CD | `lunchbox/client/LunchboxClient` (CD) | |
| 544-546 | if `curios`: FORGE bus `StepstoolClient` | Equipment | `stepstool/client/StepstoolClient` (Equipment) | |
| 547-549 | if `corn_delight` and `curios`: `CobPipeRenderer.register()` (Curios renderer registry) | CD | `compat/CobPipeRenderer` (CD) | |
| 550-552 | `MenuScreens.register(OVEN_MENU, OvenScreen::new)` | CD | `oven/client/OvenScreen` (CD) | |
| 555-559 | `RegisterKeyMappingsEvent` -> `StepstoolClient.onRegisterKeys` | Equipment | `stepstool/client/StepstoolClient` (Equipment) | |
| 561-567 | `RegisterColorHandlersEvent.Item` -> if `solapplepie`: `LunchboxClient.onItemColors` | CD | `lunchbox/client/LunchboxClient` (CD) | |
| 569-574 | `EntityRenderersEvent.RegisterRenderers`: `SHRAPNEL` -> `ThrownItemRenderer` | Engineering | `shrapnel/CreativeEngineeringEntities` (Engineering) | |

### D.1.5 Network channels (created in static init of the classes the constructor touches)

| channel | class | target jar | note |
|---|---|---|---|
| `creativeengineering:sack_bundle` (`SackBundle.CHANNEL`, scroll-to-rotate packet) | `compat/SackBundle` (`register()` at line 140), listener `compat/SackBundleScroll` (client) | Compat | Channel name keeps the namespace (rule 3.1); versioned by the channel's own protocol string |
| `creativeengineering:stepstool` (`Stepstool.CHANNEL`) | `stepstool/Stepstool` (`register()` at line 124) | Equipment | Same |
| no own channel | `mixin/ExcavationDrillItemMixin`, `mixin/DeforesterSawItemMixin` redirect PneumaticCraft's `SimpleChannel#sendToServer` on the server | Engineering | Sweep B rows; listed here because they reference `SimpleChannel` |

No other `NetworkRegistry` use in patcherMod. KubeJS startup scripts are loaded from the jar by
`compat/kubejs/JarStartupScripts` + `mixin/KubeJSStartupJarScriptsMixin` (Common), not by the main class.

## D.2 `DeferredRegister` holders (21 classes, 22 files incl. the main class)

All but one register in namespace `creativeengineering` (`CreativeEngineering.MODID`). Rule 3.1: every jar that takes
a holder creates `DeferredRegister.create(<registry>, "creativeengineering")` and calls `.register(modEventBus)` in its
own constructor. Forge allows several mods to register into one namespace through DeferredRegister (the namespace is a
string, not the active mod container), so the only per-jar requirement is a shared constant for the namespace
(proposal: `CreativeCommon.CE_NAMESPACE`), and that no two jars register the same id.

| class | registry | ids registered (count and examples) | target jar |
|---|---|---|---|
| `CreativeEngineering` (field `BLOCKS`) + `block/CreativeEngineeringBlocks` | BLOCKS | 1: `dark_fire` | Worldgen (dimension portal block) |
| `items/CreativeEngineeringItems` (folder `item/`) | ITEMS | 3: `precision_sawblade`, `creosote_soaked_sawdust`, `flint_and_terrasteel` | **Mixed**: first two Engineering, `flint_and_terrasteel` Magic (Equipment). Split the holder |
| `shrapnel/CreativeEngineeringEntities` | ENTITY_TYPES | 1: `shrapnel` | Engineering |
| `fan/DragonBreathingRecipeType` | RECIPE_SERIALIZERS, RECIPE_TYPES | 1 + 1: `dragon_breathing` | Engineering? (see D.1.1 line 84) |
| `gear/ElytraDuplicationSerializers` | RECIPE_SERIALIZERS | 1: `elytra_duplication` | Equipment |
| `potion/CEPotions` | POTIONS | 3 rows: `long_reach`, `phantom_bane` (Better Beacons fallbacks), `magic_resistance` | CB |
| `loot/BiomeTagCondition` | LOOT_ITEM_REGISTRY (loot condition types) | 1: `biome_tag` | Worldgen |
| `shield/ShieldItems` | ITEMS | 7: `<tier>_shield` for copper, gold, diamond, netherite, echo, felsic, dragon (iron = vanilla shield) | Equipment |
| `bucket/WoodenBucketItems` | ITEMS | 2: `wooden_bucket`, `wooden_water_bucket` | Engineering |
| `worldgen/MetaStructure` | STRUCTURE_TYPE, STRUCTURE_PIECE | 1 + 2: `meta`, `meta_index`, `meta_feature` | Worldgen |
| `oven/OvenRegistry` | BLOCKS, ITEMS, BLOCK_ENTITY_TYPES, MENU_TYPES, RECIPE_SERIALIZERS, RECIPE_TYPES | 6: `oven` x4, `baking` x2 | CD |
| `food/PowderedSugarRegistry` | BLOCKS, ITEMS | 2: `powdered_sugar_block`, `powdered_sugar_bucket` | CD |
| `compat/DisplayDelightDishBlocks` (via `DisplayDelightDishes`) | BLOCKS | n from `creativeengineering/displaydelight_dishes.json` (one `FoodBlock` per dish; 0 without Display Delight) | CD |
| `compat/BotanistPot` | BLOCK_ENTITY_TYPES | 1: `botanist_pot` | Compat? |
| `backpackskin/BackpackSkinSerializers` | RECIPE_SERIALIZERS | 2: `backpack_skin`, `backpack_skin_clear` | Equipment |
| `lunchbox/LunchboxItems` | ITEMS, **namespace `solapplepie`** | 1: `solapplepie:netherite_lunchbox` | CD (the one holder outside our namespace; keeps `solapplepie`) |
| `lunchbox/LunchboxRecipes` | RECIPE_SERIALIZERS | 5: `lunchbox_shaped`, `lunchbox_upgrade`, `lunchbox_skin`, `lunchbox_dye`, `lunchbox_soap` | CD |
| `food/SeasoningRecipe` | RECIPE_SERIALIZERS | 1: `seasoning` | CD |
| `worldgen/BeeNests` | BIOME_MODIFIER_SERIALIZERS | 1: `bee_nests` | Worldgen |
| `worldgen/SpawnTuning` | BIOME_MODIFIER_SERIALIZERS | 1: `tune_spawns` | Worldgen |

Not DeferredRegister but registry writes: `fan/DragonBreathingFanProcessingType` (Create `FanProcessingTypeRegistry`,
Engineering?), `compat/CobPipeRenderer` (Curios renderer registry, CD), `CETiers` via `TierSortingRegistry` (Equipment),
`MenuScreens.register` for the oven (CD). KubeJS registers the rest of the pack's items (`startup_scripts/items`, sweep F).

Per-jar DeferredRegister sets that follow: Engineering (ITEMS, ENTITY_TYPES, RECIPE_SERIALIZERS, RECIPE_TYPES),
Equipment (ITEMS, RECIPE_SERIALIZERS; plus ITEMS for `flint_and_terrasteel` as Magic row), Worldgen (BLOCKS,
LOOT_ITEM, STRUCTURE_TYPE, STRUCTURE_PIECE, BIOME_MODIFIER_SERIALIZERS), CD (BLOCKS, ITEMS, BLOCK_ENTITY_TYPES,
MENU_TYPES, RECIPE_SERIALIZERS, RECIPE_TYPES, plus the `solapplepie` ITEMS holder), CB (POTIONS), Compat
(BLOCK_ENTITY_TYPES). Common, Calendar and Devtools register nothing.

## D.3 Event subscribers

### D.3.1 `@Mod.EventBusSubscriber` classes (31 annotations, 30 classes)

Every annotation names `modid = creativeengineering`; a moved class changes the modid to its new jar (the annotation
is scanned per mod container, so a class in jar X annotated with modid Y never fires).

| class | bus and side | target jar |
|---|---|---|
| `CreativeEngineering.ClientModEvents` | MOD, client | **Mixed** (D.1.4) |
| `potion/CompoundEffectTooltip` | FORGE, client | CB |
| `backpackskin/BackpackSkinColors` | MOD, client | Equipment |
| `compat/ArmorOverflowRenderer` | FORGE, client | Common (armor bar) |
| `compat/BannerPatternAtlasCompat` | MOD, client | Common (atlas fix for bedspreads and customizable elytra) |
| `compat/SpoutGunFarmland` | FORGE, both | Compat (Create: Misc and Things spout gun) |
| `block/MossTint` | MOD, client | Worldgen? (biome-tinted moss, `[moss]`; could be Compat) |
| `compat/SackBundleScroll` | FORGE, client | Compat |
| `compat/NightstandStorage` | FORGE, both | Compat (Handcrafted storage) |
| `block/VineShearingClient` | MOD, client | Worldgen? (with `VineShearing`) |
| `changelog/ChangelogStore` (inner class) | MOD, client | Common |
| `compat/DrinkFluidEffects` | FORGE, client | Common (shared API per 2.4; the data it shows is CB's) |
| `compat/BotanistPotClient` | MOD, client | Compat? |
| `worldgen/MulberrySapling` | FORGE, both | Worldgen |
| `seasons/CropSeasonTooltip` | FORGE, client | Calendar |
| `devtools/WthitIconDump` | FORGE, client | Devtools |
| `shield/client/TierShieldMaterials` | MOD, client | Equipment |
| `devtools/DisplayNameDump` | FORGE, client | Devtools |
| `devtools/WorldJeiDump` (`/dumpworldjei`) | FORGE, both | Devtools |
| `time/TimeClient` | FORGE, client | Calendar |
| `dimension/PortalFailureHandler` | FORGE, both | Worldgen |
| `devtools/GeneratorCommand` (`/cegen`) | FORGE, both | Devtools |
| `food/SeasonedItemModel` | MOD, client | CD |
| `devtools/BlockStateSyncDebug` (two annotations: FORGE both, FORGE client) | FORGE, both + client | Devtools |
| `food/IntoxicatingDrinkTooltip` | FORGE, client | CB (alcohol tooltip; creative_brewing.md 4.2) |
| `seasons/SeasonTypes` | FORGE, both | Calendar |
| `perf/PerfCommand` (`/ceperf status, mobcap, dump`) | FORGE, both | Devtools (plan 2.1 names PerfCommand) |
| `compat/guititle/TitlePlaques` | MOD, client | Common |
| `changelog/ChangelogButtons` | FORGE, client | Common |
| `seasons/CropSeasons` | FORGE, both | Calendar |

### D.3.2 Classes with `@SubscribeEvent` registered by hand (all from `CreativeEngineering.java`)

Cross-check: every class with `@SubscribeEvent` is either annotated above or registered in the main class; none is
orphaned. The hand-registered ones, with bus and side (side = where the handler is meaningful; all are registered on
both sides unless the constructor gates them):

| class | bus and side | target jar |
|---|---|---|
| `CreativeEngineering` (`onServerStarted`) | FORGE, server | Engineering (diagnostics) |
| `compat/IECompat` | FORGE, server | Engineering |
| `backpackskin/BackpackSkinCurios` | FORGE, both (if curios) | Equipment |
| `lunchbox/LunchboxTooltip` | FORGE, client (if solapplepie) | CD |
| `stepstool/Stepstool`, `stepstool/client/StepstoolClient` | FORGE, both / client (if curios) | Equipment |
| `compat/CornDelightCompat` | FORGE, both (if corn_delight) | CD |
| `cooking/RemainderBufferStorage` | FORGE, both | CD |
| `compat/SophisticatedPolymorphCompat`, `compat/CraftingMenuPolymorphCompat` | FORGE, server (if polymorph) | Compat |
| `potion/MagicResistanceEffect`, `potion/CopperPoisoningHandler`, `potion/PhantomBaneRules` | FORGE, server | CB |
| `gear/AirBrakeHandler` | FORGE, both | Equipment |
| `shrapnel/TntShrapnelHandler` | FORGE, server | Engineering |
| `block/FastLeafDecayPort` | FORGE, server | Worldgen |
| `block/VineShearing` | FORGE, server | Worldgen? |
| `perf/WhistleArrowTracker` | FORGE, server | Common |
| `item/BottledAir` | FORGE, both | Equipment? |
| `shield/ShieldBlockHandler`, `shield/IronShieldTooltip` | FORGE, server / client | Equipment |
| `food/CanServings`, `food/CanServingsTooltip`, `food/Seasonings`, `food/SeasoningTooltip`, `food/PlacedFoodSol`, `compat/AppleSkinSeasonings` | FORGE, both / client | CD |
| `surface/CamoSpeedTooltip`, `surface/SurfaceSpeeds` | FORGE (+ MOD config reload), client / both | Engineering |
| `crops/HoeHarvestTooltip` | FORGE, client | CD |
| `time/TimePush`, `time/TimePresets`, `time/SeasonClock`, `time/TimeCommand` | FORGE + MOD config, both | Calendar |
| `util/ModDisplayNames` | MOD config reload, both | Common |
| `damage/DamageTagLoader`, `damage/DamageTagLogger` | FORGE, server | Equipment |
| `worldgen/PoolTuner`, `StructureFrequencies`, `MetaSlots`, `PlacementPriority`, `TemplateBlockSwaps`, `StructureSpawnBoxes`, `SpawnTuning`, `BeeNests`, `StructureBiomeRules`, `StructureBiomeFits`, `GroundMatch`, `EarthFill`, `DimensionFilter`, `SeabedClearance`, `SkyIslands`, `TerrainAdaptation`, `TemplateTrim`, `MassPlacer`, `LocateCommand` | FORGE, server | Worldgen |
| `mooshroom/MooshroomFamily` | FORGE, server | Worldgen? |
| `mobs/NamedMobs`, `mobs/MonsterBoxVault` | FORGE, server | Worldgen |
| `worldgen/donor/FarmersDonor` (method ref) | MOD, both | Worldgen |
| `devtools/GeneratorCommand` (method refs, plus its own annotation) | MOD, both | Devtools |

Listeners added outside the main class (`EVENT_BUS.addListener` in static init): `surface/WthitRunSpeed` and
`compat/WthitStateLines` (TagsUpdated reset), `compat/DisplayDelightDishBlocks::onLevelLoad`, `compat/ArmorTrimsItemCache`.
They move with their class.

Commands (all on `RegisterCommandsEvent`): `/cetime` Calendar, `/ceplace` and `/celocate` Worldgen, `/cegen`,
`/dumpworldjei`, `/ceperf` Devtools. The `/dumpall` family is KubeJS (`server_scripts/_lib`, sweep F).

## D.4 JEI and WTHIT entry points

JEI finds `@JeiPlugin` classes by annotation scan of every mod jar, so a plugin class moves with no wiring; each jar
just needs at least one `@JeiPlugin` class of its own and a plugin id in its own words (ids below keep
`creativeengineering:` and may stay). WTHIT reads `waila_plugins.json` from the root of every jar: each jar ships its own
`waila_plugins.json` listing only its entry classes (keys keep the `creativeengineering:` prefix).

| entry point | what it registers | target jar | note on the split |
|---|---|---|---|
| `compat/jei/world/WorldInteractionJEI` (`creativeengineering:world_interactions`) | world tabs (categories, recipes, catalysts) | Common | Interface; shows rows from many jars through `WorldTab` data (sweep A lists the tab sources) |
| `compat/jei/cycling/CyclingCraftingJEI` (`cycling_crafting`) | vanilla category extensions + cycling pages for special recipes | Common | Interface; the pages for lunchbox/backpack skin recipes read those jars' serializers: soft edges, or each content jar contributes its page kind |
| `compat/jei/SmithingTemplateJEI` (`smithing_template`) | smithing recipes shown Armor Trims style | Common | Interface fix for a third-party mod |
| `compat/jei/CanPagesJEI` (`can_pages`) | can categories, catalysts, transfer handlers, recipes | CD | Shows only CD data |
| `compat/jei/SeasoningJEI` (`seasoning`) | seasoning category | CD | |
| `compat/jei/FeastPlatingJEI` (`feast_plating`) | feast plating category | CD | |
| `compat/jei/PotionPagesJEI` (`potion_pages`) | potion page categories | CB | |
| `compat/DragonBreathingJEI` (`jei_plugin`) | dragon breathing fan category | Engineering? (with the fan type) | Generic id `jei_plugin`; rename when it moves |
| `oven/jei/OvenJeiPlugin` (`oven`) | baking category, catalysts, GUI and transfer handlers | CD | |
| WTHIT `run_speed` -> `surface/WthitRunSpeed` (client, requires `blockrunner`) | BlockRunner speed line | Engineering | own `waila_plugins.json` in the Engineering jar |
| WTHIT `block_model_icon` -> `compat/WthitBlockModelIcon` (client) | model icon fallback | Common | |
| WTHIT `coaster` -> `compat/WthitCoaster` (client, requires `brewinandchewin`) | coaster contents | CB? | Brewin' drinks; Brewin' dishes stay CD, so the owner picks |
| WTHIT `holder_contents` -> `compat/WthitHolderContents` (client, requires `supplementaries`) | jar/goblet/cage contents | Common | Generic interface line (Compat if keyed on the mod) |
| WTHIT `hive` -> `compat/WthitHive` (common + client) | "Bees: x / y" | Common? | Generic hive line; Worldgen if seen as bee nest data |
| WTHIT `state_lines` -> `compat/WthitStateLines` (client) | lines from `state_lines.json` | Common | 2.4 names it |
| WTHIT `campfire` -> `compat/WthitCampfire` (common + client) | campfire cooking lines | CD | `campfire/` is CD |
| WTHIT `sheetmetal_tank` -> `compat/WthitSheetmetalTank` (common, requires `immersiveengineering`) | IE tank contents | Engineering | |
| WTHIT `kitchen_station` -> `compat/WthitKitchenStation` (common + client, requires `appliedcooking`, `ae2`) | ME Kitchen Station status | CD? | Applied Cooking is a food mod; AE2 side makes it a Computers (Engineering) candidate |
| WTHIT `remainder_buffer` -> `compat/WthitRemainderBuffer` (common + client) | remainder slots | CD | |
| WTHIT `sheared` -> `compat/WthitSheared` (client) | sheared grass line | Worldgen? | Pairs with `VineShearing` |
| WTHIT `silk_touch` -> `compat/WthitSilkTouch` (client) | silk touch requirement | Common | Tag `creativeengineering:wthit/needs_silk_touch` |
| WTHIT `crop_seasons` -> `compat/WthitCropSeasons` (client, requires `sereneseasons`) | crop season line | Calendar | |
| WTHIT `operator_blocks` -> `compat/WthitOperatorBlocks` (client) | hide technical blocks | Common | |
| WTHIT `pot_contents` -> `compat/WthitPotContents` (client, requires `framedblocks`, `supplementaries`) | "Contains: Poppy" | Common | Generic pots line (Compat if keyed on mods) |

`compat/Wthit*` holds 14 classes plus `surface/WthitRunSpeed` = the 15 entries. They implement
`IWailaClientPlugin` / `IWailaCommonPlugin` (no class is named `IWailaPlugin`).

## D.5 `META-INF/mods.toml`

| entry | value today | what each new jar needs |
|---|---|---|
| `modLoader` / `loaderVersion` | `javafml`, `${loader_version_range}` | same, from `build-logic` (`ce.forge-mod`) |
| `license` | GNU GPL3 | same |
| `[[mixins]] config` | `creativeengineering.mixins.json` | one `<modid>.mixins.json` per jar that has mixins; `mixin_configs=none` in `gradle.properties` for a jar without (Devtools, maybe Calendar) |
| `[[mods]]` | `creativeengineering`, `${mod_version}`, "Creative Engineering" | one block per jar, id without underscore |
| dep `forge` mandatory, `minecraft` 1.19.2 mandatory | | same |
| dep `kubejs` mandatory `[1902.6.0,)` AFTER | | Common keeps it (jar startup loader). Content jars: mandatory on `kubejs` if their startup scripts need it, and **mandatory `creativecommon` AFTER** (rule 3.2) |
| dep `create` optional AFTER | | Engineering (fan, sawmill), Common (perf mixins) |
| dep `itemphysic` optional AFTER | | Compat |
| dep `create_deep_dark` optional AFTER | | Equipment (echo gear) |
| dep `easyelytratakeoff` optional AFTER | | Equipment (air brake) |
| dep `detailab` optional AFTER, CLIENT | | Common (armor bar) |
| not declared, gated by `ModList` in code | `curios`, `solapplepie`, `corn_delight`, `polymorph`, `appleskin`, `immersiveengineering`, `farmersrespite`, `trashslot`, plus every mixin target | Each jar declares its optional deps or keeps the `ModList` checks; Calendar: `sereneseasons`, `seasonhud`; CD and CB add `creativecalendar` AFTER |

The main class imports the IE API at top level (lines 52-54, used only in `onServerStarted`), so the jar fails to load
without IE on the classpath at class-verify time only if that method is reached; the Engineering jar should declare
`immersiveengineering` or gate the method.

## D.6 Mixin config, plugin and coremod

| item | what it is | target jar |
|---|---|---|
| `creativeengineering.mixins.json` | 213 common + 78 client mixin entries, `server: []`, package `com.SaintAlfonzo.creativeengineering.mixin`, no refmap (SRG names, `remap = false`; `disable_target_validator=true`) | **Mixed**: sweep B assigns each entry; each jar gets `<modid>.mixins.json` with its package |
| `"plugin": perf/CEMixinPlugin` | `IMixinConfigPlugin`: `shouldApplyMixin` gates, and `postApply` rewrites Immersive Weathering's `ServerLevelMixin$callTick` to call `perf/IwGrowthFast.blockTick` (switch `-Dcreativeengineering.iwFastTick=false`) | Common (perf, Q3) |
| Jar manifest `MixinConfigs` | written by `build-logic/src/main/groovy/ce.forge-mod.gradle` from `mixin_configs` (default `<mod_id>.mixins.json`) | per jar, automatic |
| Coremod JS | **none**: no `META-INF/coremods.json`, no `Mixins.addConfiguration`. "Coremod" in the plan means the mixin jar (memory `patchermod-coremod`) | - |
| `GatedMixinPlugin` | `development/shared/mixinplugin/ce/shared/GatedMixinPlugin.java`, pulled in by `shared_sources=mixinplugin` (CD, CB, Labels, More Mooblooms, Incubation). patcherMod has `shared_sources=packs` only and uses `CEMixinPlugin` instead | Every new jar: `shared_sources=packs,mixinplugin`, a `<X>MixinPlugin extends GatedMixinPlugin` named in its mixins json. Common keeps `CEMixinPlugin` (or merges its IW rewrite into a gated subclass) |

## D.7 Summary per target jar

Blocks of `CreativeEngineering.java` each jar takes, and what its main class must contain.

| jar | registration blocks it takes (lines) | main class needs |
|---|---|---|
| **Common** | 72-73 (own `commonSetup`), 79 (own toml), 137, 182, 234, 348, 527-532; D.3.1 Common rows; JEI world tabs, cycling, smithing template; WTHIT block_model_icon, holder_contents, hive?, state_lines, silk_touch, operator_blocks, pot_contents | `registerConfig(creativecommon-common.toml)`; `MinecraftForge.EVENT_BUS.register(WhistleArrowTracker)`; mod bus `ModDisplayNames::onConfigReload`; `ArmorTrimsItemCache.init()`; `commonSetup` with `ModDisplayNames.apply()`; client `DetailArmorBarCompat.register()`; `CEMixinPlugin` in `creativecommon.mixins.json`; `waila_plugins.json` with its 7 entries; no DeferredRegister; the `CE_NAMESPACE` constant for the others |
| **Engineering** | 81 (two of three items), 83, 84?, 93, 144 + 420-518, 147, 173, 217, 233/235, 345, 355-357, 374, 378?, 387-389, 569-574; JEI dragon breathing?; WTHIT run_speed, sheetmetal_tank | DeferredRegisters ITEMS, ENTITY_TYPES, RECIPE_SERIALIZERS, RECIPE_TYPES in ns `creativeengineering`; FORGE bus `IECompat`, `TntShrapnelHandler`, `CamoSpeedTooltip`, `SurfaceSpeeds`; mod bus `SurfaceSpeeds::onConfigReload`; `commonSetup` with `IECompat.register()`, three sawmill `forceLoad`s, `FluidBridge` diagnostic, fan type registration, hammer no-rotate; client `RegisterRenderers` for shrapnel; own toml and mixins json; `immersiveengineering`, `create`, `pneumaticcraft` as optional deps |
| **Equipment** (+ Magic rows) | 81 (`flint_and_terrasteel`), 85, 91, 111-114, 124-127, 169, 185?, 189, 195, 239-240, 329-334, 365-367, 380?, 540, 544-546, 555-559; D.3.1 `BackpackSkinColors`, `TierShieldMaterials` | DeferredRegisters ITEMS (shields, flint and terrasteel), RECIPE_SERIALIZERS (elytra duplication, backpack skins); `TierSortingRegistry.registerTier(COPPER)`; FORGE bus `ShieldBlockHandler`, `IronShieldTooltip`, `AirBrakeHandler`, `DamageTagLoader`, `DamageTagLogger`, `BottledAir`?, and if `curios` `BackpackSkinCurios`, `Stepstool`; `Stepstool.register()` (channel `creativeengineering:stepstool`); `commonSetup` with three damage `forceLoad`s and `NockRecipeBookSync`?; client `TierShieldClient.setup`, `StepstoolClient` (FORGE bus + key mappings); own toml, mixins json; optional deps `curios`, `create_deep_dark`, `easyelytratakeoff`, `nock_enough_arrows` |
| **Worldgen** (+ fauna) | 62-66 + 82 (`dark_fire`), 74, 88, 96, 177, 179?, 245-255, 259-316, 262?, 264, 266, 371-372; D.3.1 `MulberrySapling`, `PortalFailureHandler`, `MossTint`?, `VineShearingClient`?; WTHIT sheared? | DeferredRegisters BLOCKS, LOOT_ITEM, STRUCTURE_TYPE, STRUCTURE_PIECE, BIOME_MODIFIER_SERIALIZERS (x2); mod bus `FarmersDonor::onAddPackFinders` (constructor); FORGE bus the 19 `worldgen/*` classes plus `FastLeafDecayPort`, `NamedMobs`, `MonsterBoxVault`, `MooshroomFamily`?, `VineShearing`?; `commonSetup` with the `ForgivingVoidBridge` forceLoad and log; commands `/ceplace`, `/celocate`; own toml (the `[worldgen.*]` tree), mixins json |
| **Compat** | 105 (BotanistPot?), 107-109, 140, 151-154, 361, 375, 382?, 533-539; D.3.1 `SpoutGunFarmland`, `SackBundleScroll`, `NightstandStorage`, `BotanistPotClient`? | DeferredRegister BLOCK_ENTITY_TYPES (`botanist_pot`); `SackBundle.register()` (channel `creativeengineering:sack_bundle`); FORGE bus the two Polymorph compats if `polymorph`; client-only mod bus `PlantBoxModelsClient::onRegisterAdditional` in the constructor; `commonSetup` with `ItemPhysicSplit` forceLoad, `PlantBoxModels::commonSetup`, `SaltBlockTicks.init`?; client `TrashSlotCompat`, Polymorph clients; own toml, mixins json; optional deps `itemphysic`, `polymorph`, `trashslot`, `supplementaries`, `handcrafted`, `beautify` |
| **Calendar** | 227-231; D.3.1 `CropSeasonTooltip`, `TimeClient`, `SeasonTypes`, `CropSeasons`; WTHIT crop_seasons | No DeferredRegister; mod bus `TimePush::onConfigReload`, `TimePresets::onConfigLoading`; FORGE bus `TimePush`, `SeasonClock`, `TimeCommand` (`/cetime`); own toml (`[time]`, seasons); mixins json for the Serene Seasons, SeasonHUD, Better Days, Xaero's mixins; optional deps `sereneseasons`, `seasonhud` |
| **Devtools** | 321-322; D.3.1 `WthitIconDump`, `DisplayNameDump`, `WorldJeiDump`, `GeneratorCommand`, `BlockStateSyncDebug`, `PerfCommand` | No DeferredRegister; mod bus `GeneratorCommand::onCommonSetup`, `::onLoadComplete`; everything else is `@EventBusSubscriber` (modid changed to `creativedevtools`); own toml (`[debug.*]`); `mixin_configs=none` unless sweep B assigns probe mixins; left out of player payloads (Q6) |
| **CD** | 99, 101, 103, 117-121, 129-131, 134, 199-209, 213, 221, 384-386, 392-394?, 397-399, 541-543, 547-552, 561-567; D.3.1 `SeasonedItemModel`; JEI can_pages, seasoning, feast_plating, oven; WTHIT campfire, remainder_buffer, kitchen_station? | Into `CreativeDelight`'s existing constructor: DeferredRegisters BLOCKS, ITEMS, BLOCK_ENTITY_TYPES, MENU_TYPES, RECIPE_SERIALIZERS, RECIPE_TYPES in ns `creativeengineering` plus the `solapplepie` ITEMS holder; FORGE bus `RemainderBufferStorage`, `CanServings`, `CanServingsTooltip`, `Seasonings`, `SeasoningTooltip`, `PlacedFoodSol`, `HoeHarvestTooltip`, and gated `LunchboxTooltip`, `CornDelightCompat`, `AppleSkinSeasonings`; `commonSetup` adds `CornDelightCompat::addVillagerFood`, `RespitePottedTeaBush`?, `KubeJSCanAction`; client `LunchboxClient.setup` and `onItemColors`, `CobPipeRenderer`, oven `MenuScreens.register`; `waila_plugins.json` entries (CD has none today); optional deps `solapplepie`, `corn_delight`, `appleskin`, `displaydelight`, `farmersrespite` |
| **CB** | 86, 158, 162, 165, 376-377, 379; D.3.1 `CompoundEffectTooltip`, `IntoxicatingDrinkTooltip`; JEI potion_pages; WTHIT coaster? | Into `CreativeBrewing`'s constructor: DeferredRegister POTIONS; FORGE bus `MagicResistanceEffect`, `CopperPoisoningHandler`, `PhantomBaneRules`; `commonSetup` adds `DragonBreathBrewing::build`, `Lingerification::registerListGenerator`, `PotionBucketDispenser::register`; `waila_plugins.json` for the coaster if taken |

### Blocks that wire two or more targets (must be split by row)

- Constructor as a whole (71-335): 9 targets.
- `commonSetup` (341-402): Engineering, Common, Compat, Equipment, Worldgen, CB, CD.
- `ClientModEvents` (523-575): Common, Compat, Equipment, CD, Engineering.
- Line 79 `registerConfig` with `CEConfig.COMMON_SPEC`: every target (sweep C).
- Line 81 `CreativeEngineeringItems`: Engineering + Magic (Equipment) in one DeferredRegister holder.
- Lines 233-235: Engineering (`SurfaceSpeeds`) interleaved with Common (`ModDisplayNames`).
- Line 84 / 378 / `DragonBreathingJEI`: Engineering fan type whose output is CB's lingering potions.
- Line 173 `TntShrapnelHandler` (Engineering) pairs with `itemphysic/ItemBurnTransform` (Compat).
- Lines 397-399 `KubeJSCanAction` (CD) is re-registered by NCH (Common).
- `creativeengineering.mixins.json` and `CEMixinPlugin`: one config for 291 mixins of every target.
- `waila_plugins.json`: 15 entries of 6 targets in one file.
- `mods.toml` optional deps: `create` serves both Engineering and Common perf mixins.

Doubtful rows for the owner (`?` above): fan dragon breathing (Engineering or CB), `BotanistPot` (Compat), `VineShearing`
+ `WthitSheared` + `MossTint` (Worldgen or Compat), `BottledAir` (Equipment), `MooshroomFamily` (Worldgen or CD),
`NockRecipeBookSync` (Equipment or Common), `SaltBlockTicks` (Compat or CD), `RespitePottedTeaBush` (CD or Compat),
`WthitCoaster` (CB or CD), `WthitHive` (Common or Worldgen), `WthitKitchenStation` (CD or Computers (Engineering)),
`onServerStarted` IE diagnostics (Engineering or Devtools or delete).
