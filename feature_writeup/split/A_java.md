# Sweep A: Java classes

Audit of [creative_split.md](../creative_split.md) section 4, sweep A. Read-only; one row per non-mixin class. Paths are
relative to `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/`. Targets use the vocabulary of
[README.md](README.md). `CEConfig` and `CreativeEngineering.MODID` references are not counted as cross-target calls
(config sections move with their feature, rule 3); a reference to a `mixin.*Accessor` is counted against the target
of that mixin (sweep B). "Calls into other targets" names the referenced class and its target in brackets.

## A1: worldgen, compat, mobs, mooshroom, dimension, loot, forgivingvoid, trader, perf, changelog, util

Swept 2026-11-01 (agent A1). 173 classes, 28811 lines.

### worldgen/ (45 classes, 12844 lines)

Every class here is Worldgen unless noted. Internal references between worldgen classes are not listed.

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| BeeNests | 302 | Worldgen | bee nest biome modifier | none | `DeferredRegister<Codec<? extends BiomeModifier>>`; @SubscribeEvent ServerStoppedEvent |
| BiomeLayoutTuner | 756 | Worldgen | biome frequency tuning | none | none |
| DeadPlacementFilter | 66 | Worldgen | placement filter for dead structure sets | none | none |
| DimensionFilter | 47 | Worldgen | skip structures in wrong dimensions (perf.md 10.7, structure-specific) | none | @SubscribeEvent ServerStoppedEvent, TagsUpdatedEvent |
| DistantClip | 174 | Worldgen | Distant Horizons generation without the world bottom | none | none |
| donor/FarmersDonor | 86 | Worldgen | Farmer's Structures donor pack | `shared.BuiltInPacks` (A2 row; expected Common) | none |
| donor/FarmersTranslator | 448 | Worldgen | 1.21.1 to 1.19.2 worldgen data rewrite | none | none |
| donor/UpstreamJars | 16 | Worldgen | donor jar pins (read by build_deploy.py) | none | none |
| EarthFill | 1242 | Worldgen | earth fill system | none | @SubscribeEvent ServerStoppedEvent, TagsUpdatedEvent |
| FirstTickWater | 114 | Worldgen | no drops while generated water settles | none | none |
| GeneratedFallingBlocks | 48 | Worldgen | hold generated falling blocks ([worldgen.systems]) | none | none |
| GroundMatch | 370 | Worldgen | structure ground match | none | @SubscribeEvent ServerStoppedEvent |
| LocateCensus | 385 | Worldgen | /celocate census (locate analytics; dev-leaning but tied to locate) | none | none |
| LocateCommand | 403 | Worldgen | /celocate | `compat.AsyncLocatorBridge` (Worldgen) | commands `celocate` (batch, census, reset, stats, status, stop); @SubscribeEvent RegisterCommandsEvent, ServerTickEvent, ServerAboutToStartEvent, ServerStoppingEvent |
| LocateLoads | 111 | Worldgen | locate chunk-load accounting | none | none |
| LocatePrediction | 143 | Worldgen | locate prediction | none | none |
| LocateReflect | 100 | Worldgen | SRG handles for locate | none | none |
| LocateStats | 285 | Worldgen | locate stats | none | none |
| LootContainerSwaps | 128 | Worldgen | Lootr-compatible container swaps in templates | none | none (used by StructureTemplateLootSwapMixin) |
| MassPlacer | 729 | Worldgen ? | /ceplace mass structure placement; a test command, but built on PlacementPriority and TerrainAdaptation internals, so Devtools would depend on Worldgen | none | commands `ceplace` (by, preview, status, stop, structures, templates); @SubscribeEvent RegisterCommandsEvent, ServerTickEvent, ServerStoppedEvent |
| MetaFeaturePiece | 80 | Worldgen | meta structure piece | none | none |
| MetaSlots | 499 | Worldgen | meta structure slots | none | @SubscribeEvent ServerStoppedEvent |
| MetaStructure | 818 | Worldgen | meta structures | none | `DeferredRegister<StructureType<?>>`, `DeferredRegister<StructurePieceType>` |
| MulberrySapling | 37 | Worldgen | UA river sapling grows the datapack tree | none | @Mod.EventBusSubscriber(FORGE); @SubscribeEvent |
| OceanGeodes | 231 | Worldgen | geode depth under oceans | none | none (GeodeFeatureOceanDepthMixin) |
| PlacementInfo | 74 | Worldgen | placement record | none | none |
| PlacementPriority | 698 | Worldgen | structure placement priority | none | @SubscribeEvent ServerStoppedEvent |
| PoolTuner | 763 | Worldgen | jigsaw pool tuning | none | @SubscribeEvent ServerAboutToStartEvent |
| RingPositionCache | 227 | Worldgen | stronghold ring positions in world data (structure sets) | none | none |
| SeabedClearance | 103 | Worldgen | underground structures clear of the seabed | none | @SubscribeEvent ServerAboutToStartEvent, ServerStoppingEvent |
| SkyIslands | 129 | Worldgen | sky islands | none | @SubscribeEvent ServerStoppingEvent |
| SpawnConditions | 133 | Worldgen | fauna: spawn conditions | none | none |
| SpawnDump | 322 | Devtools ? | /dumpspawns backing class for `_lib/dump_spawns.js` (a dump, 2.1 Devtools); no Java callers, reached from KubeJS | none (reads vanilla registries only) | none |
| SpawnTuning | 309 | Worldgen | fauna: spawn biome modifier | `mooshroom.MooshroomFamily` (Worldgen ?) | `DeferredRegister<Codec<? extends BiomeModifier>>`; @SubscribeEvent ServerStoppedEvent |
| StructureBiomeFits | 239 | Worldgen | biome fits | none | @SubscribeEvent ServerAboutToStartEvent, ServerStoppedEvent |
| StructureBiomeRules | 249 | Worldgen | biome rules | none | @SubscribeEvent ServerAboutToStartEvent, ServerStoppedEvent |
| StructureFrequencies | 439 | Worldgen | structure frequencies | none | @SubscribeEvent ServerAboutToStartEvent, ServerStoppedEvent |
| StructureSpawnBoxes | 312 | Worldgen | structure mob spawn boxes | none | @SubscribeEvent ServerAboutToStartEvent, LivingSpawnEvent.SpecialSpawn, LivingDropsEvent |
| TemplateBlockSwaps | 160 | Worldgen | template block swaps | none | @SubscribeEvent ServerAboutToStartEvent |
| TemplateSpawnerFilter | 85 | Worldgen | strip spawners from templates | none | none |
| TemplateTrim | 186 | Worldgen | template trim | none | @SubscribeEvent ServerStoppedEvent, TagsUpdatedEvent |
| TerrainAdaptation | 504 | Worldgen | terrain adaptation | none | @SubscribeEvent ServerAboutToStartEvent, TagsUpdatedEvent, ServerStoppedEvent |
| TiedBiomes | 47 | Worldgen | biome tie handling for prediction | none | none |
| WorldgenConfig | 167 | Worldgen | worldgen config view | none | none |
| WorldgenFiles | 80 | Common ? | plain-text file access for KubeJS scripts; generic, and also used by `food.PlacedFoodSol` (CD), so a Worldgen home gives CD an edge into Worldgen | none | none |

### compat/ (loose classes: 56 classes, 5364 lines)

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| AeTooltipStacks | 38 | Common | AE2 tooltip stack for AppleSkin (2.4 tooltip mixins) | none | none |
| AppleSkinSeasonings | 54 | CD | AppleSkin values for seasoned food; only CD data | `food.Seasonings` (CD) | @SubscribeEvent FoodValuesEvent (manual registration when AppleSkin loaded) |
| ArmorOverflowRenderer | 117 | Common | armor bar extra rows (2.4 armor bar) | `mixin.ArmorBarRendererAccessor` (sweep B) | @Mod.EventBusSubscriber(FORGE, CLIENT); @SubscribeEvent |
| ArmorTrimsItemCache | 108 | Common | perf fix for Armor Trims item lists | none | addListener (recipe sync) |
| AsyncLocatorBridge | 50 | Worldgen | only class touching Async Locator; used by /celocate | none | none |
| AsyncMapTrades | 110 | Worldgen | map-trade locate through Async Locator (locate family, LC and Cloud Storage trades) | none | none (LCItemsForMapTradeMixin, CSSellingMapMixin) |
| AutomatedPotionMixingCategory | 56 | CB | JEI category for pack potion brews (IE Mixer layout); only CB data | none | none (PotionRecipeGeneratorsMixin) |
| BannerPatternAtlasCompat | 87 | Compat | bedspreads and customizableelytra atlas fix; third-party fix with no wider system | none | @Mod.EventBusSubscriber(MOD, CLIENT); @SubscribeEvent |
| BlockModelIcons | 135 | Common | WTHIT icon rule, WTHIT-free so devtools/WthitIconDump reads it | none | none |
| BotanistPot | 173 | Compat | Beautify botanist workbench as flower pot | none | `DeferredRegister<BlockEntityType<?>>` |
| BotanistPotBlockEntity | 93 | Compat | its block entity | none | none |
| BotanistPotClient | 201 | Compat | its model wrapper | none | @Mod.EventBusSubscriber(MOD, CLIENT); @SubscribeEvent x2 |
| CobPipeRenderer | 64 | CD | Corn Delight cob pipe curio render (food mod, CD routing) | none | none (Curios renderer register) |
| CornDelightCompat | 117 | CD | Corn Delight 1.2.11 behaviour for CD's KubeJS items | none | @SubscribeEvent PlayerInteractEvent.RightClickItem |
| CornDelightCurios | 30 | CD | Curios half of the above | none | none |
| CraftingMenuPolymorphClient | 42 | Compat | Polymorph widget on IE and Pretty Pipes crafting menus | none | none |
| CraftingMenuPolymorphCompat | 37 | Compat | Polymorph server half | none | @SubscribeEvent PlayerContainerEvent.Open |
| DetailArmorBarCompat | 342 | Common | Detail Armor Bar icons for modded armor (2.4 armor bar) | none | none (DetailArmorBarAPI register x2) |
| DisplayDelightDishBlocks | 63 | CD | Display Delight dish blocks (placeable food) | none | `DeferredRegister<Block>`; addListener |
| DisplayDelightDishes | 75 | CD | dish list and linking | none | none |
| DragonBreathingJEI | 176 | Engineering | JEI page for the dragon breathing fan recipe; only Engineering data | `fan.DragonBreathingRecipe`, `fan.DragonBreathingRecipeType` (Engineering) | @JeiPlugin |
| DrinkFluidEffects | 101 | Common | shared API (2.4) | none | @Mod.EventBusSubscriber(CLIENT); @SubscribeEvent (recipe sync) |
| DyeableCannedGolem (removed 2026-11-02: replaced by `CannedGolemDye` in the Canned Goods member, [cans.md](../cans.md) 9) | 38 | CD | Canned Goods golem dye (cans) | none | none (CannedGolemColorMixin) |
| FtbTooltipStacks | 50 | Common | FTB tooltip stack for AppleSkin (2.4) | `mixin.FtbTooltipListAccessor` (sweep B) | none |
| IECompat | 148 | Engineering | IE arc recycling of springs, hammer_no_rotate blacklist (serves CD cutting boards through a tag) | none | ServerAboutToStartEvent @SubscribeEvent; called from CreativeEngineering.commonSetup |
| ImmersivePostsMaterials | 60 | Engineering ? | Immersive Posts material cut (IE addon, block-state count); could be Compat or Common perf | none | none (three ImmersivePosts mixins) |
| JarMixing | 130 | Compat | Supplementaries mixed jars | none | none (SupplementariesJarTileMixin) |
| KubeJSCanAction | 26 | CD | KubeJS can ingredient action | `food.CanServings` (CD) | KubeJS custom ingredient action (registered from CreativeEngineering) |
| LightmansTooltipStacks | 43 | Common | LC tooltip stack for AppleSkin (2.4) | none | none |
| LingerificationCategory | 64 | CB | JEI category for lingering brews (fluidbrewing, CB) | `fluidbrewing.Lingerification` (CB, javadoc only) | none |
| MealSlotTooltip | 31 | Common | AppleSkin stack for FD cooking pot meal slot (third-party screens) | none | none |
| NightstandStorage | 143 | Compat | Handcrafted nightstand storage | none | @Mod.EventBusSubscriber(FORGE); @SubscribeEvent; addListener |
| NockRecipeBookSync | 38 | Equipment ? | Nock Enough Arrows RecipeBookType desync fix; Equipment owns the Nock mixins, but it is a plain fix (Compat alternative) | none | none |
| PlantBoxModels | 51 | Compat | flower box plant models (Supplementaries) | none | none |
| PlantBoxModelsClient | 57 | Compat | client half | none | none (model register x3) |
| PotionFluidTooltip | 32 | Common ? | shared JEI potion-fluid slot tooltip; users are RowPagesCategory (CB ?) and five IE potion JEI mixins (CB), so it could ride CB | none | none |
| QuarkSafeSort | 113 | Compat | Quark item-safe sort | none | none |
| RespitePottedTeaBush | 31 | CD ? | Farmer's Respite potted tea bush registration (CD routes Respite; it is decor, so Compat is the alternative) | none | none |
| SackBundle | 170 | Compat | Supplementaries sack as bundle | none | none |
| SackBundleScroll | 46 | Compat | scroll over sacks | none | @Mod.EventBusSubscriber(CLIENT); @SubscribeEvent |
| SophisticatedPolymorphClient | 98 | Compat | Polymorph widget on Sophisticated screens | none | none |
| SophisticatedPolymorphCompat | 46 | Compat | server half | none | @SubscribeEvent PlayerContainerEvent.Open |
| SpoutGunFarmland | 59 | Compat | Create Misc and Things spout gun soaks farmland (single tweak) | none | @Mod.EventBusSubscriber(FORGE); @SubscribeEvent |
| StateLines | 109 | Common | WTHIT state line rules (2.4) | `crops.CropGrowth.glob` (CD): a glob helper, should move to util | none |
| StringEntry | 98 | Common | Create config screen string widget; used by CEConfig and ShieldEffects | none | none (SubMenuConfigScreenMixin) |
| SuppFlowerBox | 24 | Compat | Supplementaries flower box calls | none | none |
| TrashSlotCompat | 222 | Compat | TrashSlot pack defaults | `mixin.TrashSlotSaveStateAccessor` (sweep B) | none |
| TrinketCaseRules | 29 | Compat | Botania trinket case rules | `mixin.ItemBackedInventoryAccessor` (sweep B) | none |
| TroughRetryAccess | 13 | CD | trough retry stamp (owner decision) | none | none |
| WthitBlockModelIcon | 139 | Common | WTHIT plugin, block model icon | none | IWailaClientPlugin (waila_plugins.json) |
| WthitCampfire | 126 | CD ? | WTHIT lines for campfires: Unlit Campfire's burn line plus CampfireRain (CD, campfire/) ticks | `campfire.CampfireRain`, `campfire.CampfireRainTicks` (CD) | IWailaCommonPlugin, IWailaClientPlugin |
| WthitCoaster | 130 | CB | WTHIT line for Brewin' coaster; only CB data | none | IWailaClientPlugin |
| WthitCropSeasons | 85 | Calendar | WTHIT line for crop seasons; only Calendar data | `seasons.CropSeasons`, `seasons.CropSeasonTooltip` (Calendar) | IWailaClientPlugin |
| WthitHive | 103 | Common | WTHIT hive lines (vanilla data) | none | IWailaClientPlugin |
| WthitHolderContents | 191 | Common | WTHIT Supplementaries holder contents | none | IWailaClientPlugin |
| WthitKitchenStation | 66 | Common | WTHIT Applied Cooking line (third-party data) | none | IWailaCommonPlugin, IWailaClientPlugin |
| WthitOperatorBlocks | 65 | Common | WTHIT operator block visibility | none | IWailaClientPlugin |
| WthitPotContents | 249 | Common | WTHIT pot contents across mods | none | IWailaClientPlugin |
| WthitRemainderBuffer | 116 | CD | WTHIT leftover slot line for pot, kettle and oven; only CD data | `cooking.RemainderBuffer`, `cooking.RemainderBufferHolder`, `oven.OvenBlockEntity` (CD) | IWailaCommonPlugin, IWailaClientPlugin |
| WthitSheared | 78 | Common | WTHIT sheared line (IW fertile state, VineShearing by state only) | none | IWailaClientPlugin |
| WthitSheetmetalTank | 42 | Common | WTHIT IE tank contents (third-party data) | none | IWailaCommonPlugin, IDataProvider |
| WthitSilkTouch | 69 | Common | WTHIT silk touch line | none | IWailaClientPlugin |
| WthitStateLines | 159 | Common | WTHIT state lines (2.4) | none | IWailaCommonPlugin, IWailaClientPlugin; addListener |

### compat/guititle/ (2 classes, 523 lines)

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| TitlePlaqueLayout | 197 | Common | GUI plaque layout (2.4) | none | none |
| TitlePlaques | 326 | Common | GUI plaque stretch (2.4) | none | @Mod.EventBusSubscriber(MOD, CLIENT); @SubscribeEvent |

### compat/jei/ (36 classes, 6495 lines)

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| ArmorTrimsSmithing | 160 | Common | Armor Trims half of SmithingTemplateJEI (third-party data) | none | none |
| CanCraftingCategory | 88 | CD | can crafting page | none | none |
| CanPagesJEI | 182 | CD | Canning and Using Cans pages; only CD data | none | @JeiPlugin |
| CraftableFirstRecipeLayoutList | 155 | Common | craftable-first backport (2.4) | `mixin.JeiMissingSlotsErrorAccessor` (sweep B) | none |
| cycling/CraftingProbe | 117 | Common | special recipe probe | none | none |
| cycling/CyclingCraftingDisplay | 115 | Common | cycling crafting display | none | none |
| cycling/CyclingCraftingExtension | 56 | Common | JEI crafting extension | none | none |
| cycling/CyclingCraftingJEI | 74 | Common | JEI coverage of special recipes, AE2 facade hide | none | @JeiPlugin |
| cycling/CyclingCraftingPages | 1030 | Common | pages for third-party special recipes | none | none |
| FeastPlatingCategory | 465 | CD | feast plating page (FD feasts, Brewin' pizza) | none | none |
| FeastPlatingJEI | 51 | CD | its plugin | none | @JeiPlugin |
| FluidTankScale | 28 | Common | JEI fluid tank scale ([jei]) | none | none (JeiFluidTankScaleMixin) |
| JeiArrow | 22 | Common | shared JEI arrow | none | none |
| JeiIngredientChanges | 81 | Common | ingredient change tracking for the order report | none | none |
| PackIngredientOrder | 544 | Common | JEI ordering (2.4) | `mixin.JeiIngredientFilterAccessor` (sweep B) | none |
| PackOrderReport | 247 | Common | JEI order report (dev output, but part of the ordering code; Devtools alternative) | none | none |
| PotionPagesJEI | 266 | CB ? | per-potion Tipped Arrows and Potion Buckets pages; potion content is CB's, tipped arrows are vanilla | none | @JeiPlugin |
| RecipeRows | 218 | CB ? | rows for PotionPagesJEI (Create, IE recipes) | none | none |
| RowPagesCategory | 146 | CB ? | page type used only by PotionPagesJEI | `compat.PotionFluidTooltip` (Common ?) | none |
| SeasoningCategory | 119 | CD | seasoning page | none | none |
| SeasoningJEI | 295 | CD | seasoning plugin; only CD data | `food.SeasoningRecipe`, `food.Seasonings` (CD) | @JeiPlugin |
| SmithingTemplateJEI | 95 | Common | Armor Trims smithing in JEI (third-party data) | none | @JeiPlugin |
| world/BurnRows | 178 | Common ? | Burning world tab; reads `itemphysic` rules (Compat), so Common would call Compat: move the row provider into Compat through WorldRows.provider | `itemphysic.ItemBurnTransform`, `itemphysic.ItemPhysicSplit` (Compat) | none |
| world/CampfireRows | 153 | Common ? | Campfire Rules tab; reads CampfireRain (CD), same fix as BurnRows | `campfire.CampfireRain` (CD) | none |
| world/PotionRows | 139 | Common | per-potion row expansion (vanilla potion registry) | none | none |
| world/WorldInteractionCategory | 231 | Common | world tab category (2.4) | none | none |
| world/WorldInteractionJEI | 155 | Common | world tabs plugin (2.4) | `compat.jei.CanPagesJEI` (CD): check what it uses | @JeiPlugin |
| world/WorldRow | 257 | Common | parsed world row | none | none |
| world/WorldRows | 73 | Common | runtime rows API (KubeJS and Java providers): the hook other jars use | none | none |
| world/WorldSlotRenderer | 209 | Common | world tab slot renderer | none | none |
| world/WorldTab | 54 | Common | world tab list | none | none |

### compat/kubejs/ (1 class, 98 lines)

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| JarStartupScripts | 98 | Common | jar startup script loader (2.4) | `mixin.KubeJSScriptManagerInvoker` (sweep B, Common) | none |

### mobs/ (2 classes, 430 lines)

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| MonsterBoxVault | 236 | Worldgen ? | Quark monster boxes as vaults ([monster_box]); mob tweak (fauna) but Quark-specific (Compat alternative) | none | @SubscribeEvent BlockEvent.BreakEvent (MonsterBoxBlockEntityMixin) |
| NamedMobs | 194 | Worldgen | named mobs (fauna) | none | @SubscribeEvent LivingSpawnEvent.SpecialSpawn, EntityJoinLevelEvent, LivingDeathEvent |

### mooshroom/ (2 classes, 115 lines)

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| MooshroomFamily | 104 | Worldgen ? | natural brown mooshroom spawns (fauna, calls SpawnTuning) plus breeding of Fungi Delight cows (husbandry, CD with More Mooblooms); the plan's open question. If CD, SpawnTuning.brownChance must become a Common API or the spawn half splits off | `worldgen.SpawnTuning.brownChance` (Worldgen) | @SubscribeEvent LivingSpawnEvent.SpecialSpawn (MushroomCowBreedingMixin, AnimalCanMateMooshroomMixin) |
| MushroomTypeSetter | 11 | Worldgen ? | duck interface for MushroomCowBreedingMixin; follows MooshroomFamily | none | none |

### dimension/ (1 class, 181 lines)

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| PortalFailureHandler | 181 | Worldgen | nether portal ignition handling (2.1 dimension/) | `block.CreativeEngineeringBlocks.DARK_FIRE` (Engineering, block/): needs a tag or Common lookup | @Mod.EventBusSubscriber(FORGE); @SubscribeEvent |

### loot/ (1 class, 77 lines)

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| BiomeTagCondition | 77 | Worldgen | biome_tag loot condition (2.1 loot/) | none | `DeferredRegister<LootItemConditionType>` |

### forgivingvoid/ (1 class, 64 lines)

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| ForgivingVoidBridge | 64 | Worldgen | Forgiving Void config bridge (2.1) | none | none (ForgivingVoidMixin) |

### trader/ (4 classes, 309 lines)

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| TagItemListing | 53 | Common | listing type used by TradeListings | none | none |
| TradeListings | 108 | Common | shared API (2.4), Java side of global.trades | none | none |
| WanderingTraderTweaks | 78 | Worldgen ? | wandering trader spawn rate and trade counts; a mob spawn tweak (fauna), Compat alternative | `trader.TradeListings` (Common) | none (WanderingTraderSpawnerMixin, WanderingTraderTradesMixin) |
| WinemakerTrades | 70 | CB | Vinery Wandering Winemaker listings (called from KubeJS) | none | none |

### perf/ (10 classes, 1171 lines)

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| CEMixinPlugin | 116 | Common | mixin config plugin (IW ASM rewrite); each jar gets its own plugin, this one stays with Common's json | none | IMixinConfigPlugin |
| IwGrowthFast | 125 | Common | IW growth fast path (perf) | none | none |
| IwGrowthHolder | 12 | Common | duck for IwBlockGrowthHolderMixin | none | none |
| KubeJSHandlerTiming | 45 | Common | KubeJS handler timing (perf) | none | none (KubeJSEventHandlerContainerMixin) |
| MobCapLevels | 129 | Worldgen ? | mob cap levels: perf.md 17, but 2.1 lists "mob caps and spawn areas" under Worldgen fauna; PerfCommand (Devtools) reads it | none | none (MobCapLevelTickMixin, MobCapMixin, MobCapSpawnAreaMixin) |
| PerfCommand | 153 | Devtools | /ceperf (2.1) | `perf.PerfEvents`, `perf.MobCapLevels` (Common, Worldgen ?) | commands `ceperf` (status, dump, mobcap); @Mod.EventBusSubscriber(FORGE); @SubscribeEvent RegisterCommandsEvent |
| PerfEvents | 343 | Common ? | state behind the perf mixins (Common); 2.1 names "JFR events" for Devtools, but seven Common perf mixins call this class, so it must stay under them | none | none |
| PerfJfrEvents | 202 | Common ? | the JFR event classes; same note as PerfEvents (two Common mixins reference it) | none | none |
| PerfStatsHolder | 6 | Common | duck for EntityType and BlockEntityType perf stats | none | none |
| WhistleArrowTracker | 40 | Common ? | perf: live whistling arrows for NeaWhistleGoalSkipMixin; perf is Common, but the Nock mixins are Equipment's | none | @SubscribeEvent EntityJoinLevelEvent, EntityLeaveLevelEvent (registered from CreativeEngineering) |

### changelog/ (5 classes, 928 lines)

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| ChangelogButtons | 214 | Common | changelog viewer (2.4) | none | @Mod.EventBusSubscriber(CLIENT); @SubscribeEvent x2; addListener |
| ChangelogMarkdown | 175 | Common | markdown to blocks (shaded commonmark) | none | none |
| ChangelogPanel | 207 | Common | viewer panel | none | none |
| ChangelogScreen | 146 | Common | viewer screen | none | none |
| ChangelogStore | 186 | Common | version files and read marker | none | @Mod.EventBusSubscriber(MOD, CLIENT); @SubscribeEvent |

### util/ (5 classes, 212 lines)

| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| ArmorEnchantabilityBridge | 31 | Common ? | util is Common by rule, but this is the armor enchantability feature's config bridge (Equipment, 2.1) and nothing else reads it | none | none (ArmorMaterialEnchantabilityMixin) |
| BlockStateIdOverflow | 38 | Common | engine fix arithmetic (SectionUpdateEncodeFixMixin) | none | none |
| BonemealCharms | 25 | Common ? | tag holder for the bone-meal charm effect (Equipment or CB gear effects); util rule says Common | none | none (BonemealCharmMixin) |
| ElytraChestplates | 38 | Common ? | tag holder for gliding chestplates (Equipment); util rule says Common | none | none (EasyElytraTakeoff mixins) |
| ModDisplayNames | 80 | Common | mod display name overrides (interface) | none | none |

### A1 summary

173 classes, 28811 lines.

| target | classes | lines |
|---|---|---|
| Worldgen | 54 | 13676 |
| Common | 68 | 9429 |
| CD | 18 | 1953 |
| Compat | 19 | 1821 |
| CB | 7 | 950 |
| Engineering | 3 | 384 |
| Calendar | 1 | 85 |
| Equipment | 1 | 38 |
| Devtools | 2 | 475 |

Doubtful rows (24, target marked `?`):

- `MassPlacer` (Worldgen ?): /ceplace mass structure placement; a test command, but built on PlacementPriority and TerrainAdaptation internals, so Devtools would depend on Worldgen
- `SpawnDump` (Devtools ?): /dumpspawns backing class for `_lib/dump_spawns.js` (a dump, 2.1 Devtools); no Java callers, reached from KubeJS
- `WorldgenFiles` (Common ?): plain-text file access for KubeJS scripts; generic, and also used by `food.PlacedFoodSol` (CD), so a Worldgen home gives CD an edge into Worldgen
- `ImmersivePostsMaterials` (Engineering ?): Immersive Posts material cut (IE addon, block-state count); could be Compat or Common perf
- `NockRecipeBookSync` (Equipment ?): Nock Enough Arrows RecipeBookType desync fix; Equipment owns the Nock mixins, but it is a plain fix (Compat alternative)
- `PotionFluidTooltip` (Common ?): shared JEI potion-fluid slot tooltip; users are RowPagesCategory (CB ?) and five IE potion JEI mixins (CB), so it could ride CB
- `RespitePottedTeaBush` (CD ?): Farmer's Respite potted tea bush registration (CD routes Respite; it is decor, so Compat is the alternative)
- `WthitCampfire` (CD ?): WTHIT lines for campfires: Unlit Campfire's burn line plus CampfireRain (CD, campfire/) ticks
- `PotionPagesJEI` (CB ?): per-potion Tipped Arrows and Potion Buckets pages; potion content is CB's, tipped arrows are vanilla
- `RecipeRows` (CB ?): rows for PotionPagesJEI (Create, IE recipes)
- `RowPagesCategory` (CB ?): page type used only by PotionPagesJEI
- `world/BurnRows` (Common ?): Burning world tab; reads `itemphysic` rules (Compat), so Common would call Compat: move the row provider into Compat through WorldRows.provider
- `world/CampfireRows` (Common ?): Campfire Rules tab; reads CampfireRain (CD), same fix as BurnRows
- `MonsterBoxVault` (Worldgen ?): Quark monster boxes as vaults ([monster_box]); mob tweak (fauna) but Quark-specific (Compat alternative)
- `MooshroomFamily` (Worldgen ?): natural brown mooshroom spawns (fauna, calls SpawnTuning) plus breeding of Fungi Delight cows (husbandry, CD with More Mooblooms); the plan's open question. If CD, SpawnTuning.brownChance must become a Common API or the spawn half splits off
- `MushroomTypeSetter` (Worldgen ?): duck interface for MushroomCowBreedingMixin; follows MooshroomFamily
- `WanderingTraderTweaks` (Worldgen ?): wandering trader spawn rate and trade counts; a mob spawn tweak (fauna), Compat alternative
- `MobCapLevels` (Worldgen ?): mob cap levels: perf.md 17, but 2.1 lists "mob caps and spawn areas" under Worldgen fauna; PerfCommand (Devtools) reads it
- `PerfEvents` (Common ?): state behind the perf mixins (Common); 2.1 names "JFR events" for Devtools, but seven Common perf mixins call this class, so it must stay under them
- `PerfJfrEvents` (Common ?): the JFR event classes; same note as PerfEvents (two Common mixins reference it)
- `WhistleArrowTracker` (Common ?): perf: live whistling arrows for NeaWhistleGoalSkipMixin; perf is Common, but the Nock mixins are Equipment's
- `ArmorEnchantabilityBridge` (Common ?): util is Common by rule, but this is the armor enchantability feature's config bridge (Equipment, 2.1) and nothing else reads it
- `BonemealCharms` (Common ?): tag holder for the bone-meal charm effect (Equipment or CB gear effects); util rule says Common
- `ElytraChestplates` (Common ?): tag holder for gliding chestplates (Equipment); util rule says Common

Cross-target calls (class, its target, what it reaches; accessor references to mixins are left to sweep B):

- `donor/FarmersDonor` (Worldgen) -> `shared.BuiltInPacks` (A2 row; expected Common)
- `LocateCommand` (Worldgen) -> `compat.AsyncLocatorBridge` (Worldgen)
- `SpawnDump` (Devtools ?) -> none (reads vanilla registries only)
- `SpawnTuning` (Worldgen) -> `mooshroom.MooshroomFamily` (Worldgen ?)
- `AppleSkinSeasonings` (CD) -> `food.Seasonings` (CD)
- `DragonBreathingJEI` (Engineering) -> `fan.DragonBreathingRecipe`, `fan.DragonBreathingRecipeType` (Engineering)
- `KubeJSCanAction` (CD) -> `food.CanServings` (CD)
- `StateLines` (Common) -> `crops.CropGrowth.glob` (CD): a glob helper, should move to util
- `WthitCampfire` (CD ?) -> `campfire.CampfireRain`, `campfire.CampfireRainTicks` (CD)
- `WthitCropSeasons` (Calendar) -> `seasons.CropSeasons`, `seasons.CropSeasonTooltip` (Calendar)
- `WthitRemainderBuffer` (CD) -> `cooking.RemainderBuffer`, `cooking.RemainderBufferHolder`, `oven.OvenBlockEntity` (CD)
- `RowPagesCategory` (CB ?) -> `compat.PotionFluidTooltip` (Common ?)
- `SeasoningJEI` (CD) -> `food.SeasoningRecipe`, `food.Seasonings` (CD)
- `world/BurnRows` (Common ?) -> `itemphysic.ItemBurnTransform`, `itemphysic.ItemPhysicSplit` (Compat)
- `world/CampfireRows` (Common ?) -> `campfire.CampfireRain` (CD)
- `world/WorldInteractionJEI` (Common) -> `compat.jei.CanPagesJEI` (CD): check what it uses
- `MooshroomFamily` (Worldgen ?) -> `worldgen.SpawnTuning.brownChance` (Worldgen)
- `PortalFailureHandler` (Worldgen) -> `block.CreativeEngineeringBlocks.DARK_FIRE` (Engineering, block/): needs a tag or Common lookup
- `WanderingTraderTweaks` (Worldgen ?) -> `trader.TradeListings` (Common)
- `PerfCommand` (Devtools) -> `perf.PerfEvents`, `perf.MobCapLevels` (Common, Worldgen ?)

Notes for the merge pass:

- Common -> content edges to fix: `StateLines` -> `crops.CropGrowth.glob` (CD; a string glob helper, move to util), `world/BurnRows` -> `itemphysic` (Compat), `world/CampfireRows` -> `campfire.CampfireRain` (CD), `world/WorldInteractionJEI` -> `CanPagesJEI.doubleIcon` (CD; an icon helper, move to JeiArrow or a Common helper). The two row classes can register through `WorldRows.provider` from their own jar.
- Worldgen -> Engineering edge: `dimension/PortalFailureHandler` reads `block.CreativeEngineeringBlocks.DARK_FIRE`; replace with a block tag or registry lookup by id.
- Devtools -> Common and Worldgen: `PerfCommand` reads `PerfEvents` and `MobCapLevels`; allowed (dependencies point down) once `MobCapLevels` has a home.
- Common perf mixins (sweep B) depend on `PerfEvents` and `PerfJfrEvents`, so those two cannot go to Devtools with `PerfCommand` as 2.1 words it.
- `WorldgenFiles` is read by `food.PlacedFoodSol` (A2, CD) and by KubeJS worldgen scripts; a Worldgen home gives CD an edge into Worldgen, hence `Common ?`.
- `MooshroomFamily` is the plan's open question (2.2): the spawn half needs `SpawnTuning.brownChance` (Worldgen), the breeding half is husbandry (CD). A CD home needs the chance exposed from Common or the class split.


## A2: the remaining packages and root classes

Sweep A2, 2026-11-01. Covers every non-mixin class in `PM` outside the packages of A1: backpackskin, block, bucket,
campfire, cooking, crops, damage, devtools, excavation, fan, fluid, fluidbrewing, food, gear, item, itemphysic,
lunchbox, oven, potion, sawmill, seasons, shield, shrapnel, stepstool, surface, time, plus `CEConfig`, `CETiers` and
`CreativeEngineering`. 167 classes, 21492 lines. Method: imports of other `creativeengineering` packages, grep for
`DeferredRegister`, `@SubscribeEvent`, `@Mod.EventBusSubscriber`, `@JeiPlugin`, `Commands.literal`, `ModList.isLoaded`,
and the class header comment; full reads only where those did not settle the row.

Conventions: references to `CEConfig` and `CreativeEngineering.MODID` are not counted as cross-target calls (every jar
reads its own config sections, sweep C). Same-package references are not listed. "sweep D" = the registration
inventory for how a `register(IEventBus)` method is wired from `CreativeEngineering`.

### backpackskin (Equipment)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| backpackskin/BackpackSkinClearRecipe | 48 | Equipment | skin removal recipe for Sophisticated Backpacks | none | none (serializer in BackpackSkinSerializers) |
| backpackskin/BackpackSkinColors | 43 | Equipment | client colour handler for skinned backpacks | none | `@Mod.EventBusSubscriber(MOD, CLIENT)`, `@SubscribeEvent` RegisterColorHandlersEvent (LOWEST) |
| backpackskin/BackpackSkinCurios | 20 | Equipment | Curios equip hook for skinned backpacks | none | `@SubscribeEvent` CurioEquipEvent (instance registered by CreativeEngineering, sweep D) |
| backpackskin/BackpackSkinRecipe | 85 | Equipment | smithing recipe backpack + skin block | none | none |
| backpackskin/BackpackSkinsClient | 72 | Equipment | client model lookup of the skin block | none | none |
| backpackskin/BackpackSkinSerializers | 28 | Equipment | registers the two skin recipe serializers | none | `DeferredRegister<RecipeSerializer>` (creativeengineering), `register(IEventBus)` |
| backpackskin/BackpackSkins | 84 | Equipment | skin tag on the stack, shared API of the package | none | none |

### block (Engineering, leaf decay to Worldgen)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| block/CreativeEngineeringBlocks | 28 | Engineering | registers the pack's own blocks (Dark Fire) on CreativeEngineering.BLOCKS | none (uses CreativeEngineering.BLOCKS, Common until sweep D moves it) | `register(IEventBus)` over CreativeEngineering's `DeferredRegister<Block>` |
| block/DarkFireBlock | 115 | Engineering | Dark Fire block (portal igniter for FlintAndTerraSteel) | none | none |
| block/FastLeafDecayPort | 47 | Worldgen | Fast Leaf Decay port; plan 2.1 puts the leaf decay mixins in Worldgen, the helpers follow | none | `@SubscribeEvent` (BlockEvent.NeighborNotify) |
| block/LeafDecayPiles | 36 | Worldgen | IW leaf pile on decay, same leaf decay system | none | none; `ModList.isLoaded("immersive_weathering")` |
| block/LeafDecayRange | 172 | Worldgen | leaf life range, helper of LeavesDecayRangeMixin (Worldgen) | none | none |
| block/MossTint | 99 | Engineering? | biome-tinted moss models; client-only visual block tweak; Worldgen (biome colour) is the other candidate | none | `@Mod.EventBusSubscriber(MOD, CLIENT)`, 4 `@SubscribeEvent` (model bake, colour handlers) |
| block/SaltBlockTicks | 120 | Engineering? | one-salt merge (item_merges.md 2.9): Vintage Delight salt blocks tick like The Salt's; CD if salt counts as food | none | none; `ModList.isLoaded("salt")`, reflection init |
| block/VineShearingClient | 134 | Engineering? | client model of a sheared modded vine; Compat (Quark extension) or Worldgen (plants) are candidates | none | `@Mod.EventBusSubscriber(MOD, CLIENT)`, `@SubscribeEvent` model bake |
| block/VineShearing | 118 | Engineering? | shears stop vines growing (VineBlockShearMixin helper); same doubt as the client half | none | `@SubscribeEvent` (PlayerInteractEvent) |

### bucket (Engineering)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| bucket/WoodenBucketItem | 106 | Engineering | water-only early bucket item | none | none |
| bucket/WoodenBucketItems | 33 | Engineering | registers wooden_bucket and wooden_water_bucket | none | `DeferredRegister<Item>` (creativeengineering), `register(IEventBus)` |

### campfire (CD by draft; both rows doubtful)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| campfire/CampfireComfort | 58 | CD? | Comfortable Campfires port: FD Comfort near a lit campfire; grants an effect, so CB if the gear-effect rule extends to it; not food | none | none (driven from CampfireRainMixin) |
| campfire/CampfireRain | 56 | CD? | rain douses campfires after N ticks; not food at all: Engineering (block rule) or Calendar (weather) candidates | none | none (driven from CampfireRainMixin) |
| campfire/CampfireRainTicks | 12 | CD? | duck interface for CampfireRain's tick count; goes wherever CampfireRain goes | none | none |

### cooking (CD)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| cooking/client/RemainderSlotFrames | 27 | CD | draws remainder slot frames on station screens | none | none |
| cooking/RemainderBufferHolder | 10 | CD | duck interface for mixin-given stations | none | none |
| cooking/RemainderBuffer | 201 | CD | leftover-container slots on FD-style stations; read by compat/WthitRemainderBuffer (Common interface row showing CD data: should move to CD) | none | none |
| cooking/RemainderBufferStorage | 58 | CD | capability save of the buffer | none | `@SubscribeEvent` AttachCapabilitiesEvent (BlockEntity) |
| cooking/RemainderOutputHandler | 58 | CD | automation handler with remainder slots | none | none |
| cooking/RemainderSlot | 32 | CD | take-only menu slot | none | none |

### crops (CD)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| crops/CropGrowth | 140 | CD | growth rules behind the WTHIT Growth line for CD crops; called from compat/StateLines (Common): that WTHIT row only shows CD data and should follow it | none | none |
| crops/CropParity | 55 | CD | non-CropBlock crops (tobacco) follow farmland rules | none | none |
| crops/DandelionMaturing | 36 | CD | dandelion to Veggies Delight mature dandelion on soil ticks | none | none |
| crops/HoeHarvestArea | 73 | CD | Quark hoe harvest area per hoe tier (Compat is the alternative; kept with crops) | none | none |
| crops/HoeHarvestTooltip | 31 | CD | "Harvest area" tooltip line | none | `@SubscribeEvent` ItemTooltipEvent |
| crops/StemExtraFruit | 189 | CD | extra fruit on melon/pumpkin/hamimelon stems | none | none |

### damage (Equipment by draft; whole package doubtful)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| damage/DamageFlag | 103 | Equipment? | DamageSource flags as built-in damage tags; read by shield (Equipment) and CreativeEngineering | none | none |
| damage/DamageTagLoader | 109 | Equipment? | loads data/<ns>/damage_tags/*.json | none | `@SubscribeEvent` AddReloadListenerEvent, `event.addListener(new DamageTagLoader())` |
| damage/DamageTagLogger | 101 | Equipment? | debug logger of seen damage sources ([damage_tags] logSeenSources) | none | `@SubscribeEvent(LOWEST, receiveCanceled)` LivingHurt/Attack |
| damage/DamageTags | 60 | Equipment? | damage tag registry; read by shield/* (Equipment) AND potion/MagicResistanceEffect (CB): a content-to-content edge, so the package is a Common API candidate under rule 2 | none | none |
| damage/DamageTagSnapshot | 191 | Equipment? | merged generation of the tags | none | none |

### devtools (Devtools)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| devtools/BlockStateSyncDebug | 197 | Devtools | ghost-block debug switch ([debug.block_state_sync]) | none | `@Mod.EventBusSubscriber(FORGE)` + nested `(FORGE, CLIENT)`, 2 `@SubscribeEvent` |
| devtools/DisplayNameDump | 319 | Devtools | /cedumpnames client name dump | none | `@Mod.EventBusSubscriber(CLIENT)`, 2 `@SubscribeEvent`, command `cedumpnames` |
| devtools/GeneratorCommand | 363 | Devtools | /cegen command | none | `@Mod.EventBusSubscriber(FORGE)`, 2 `@SubscribeEvent`, command `cegen` (list, status, cancel, run, dumps) |
| devtools/GeneratorRunner | 351 | Devtools | runs tools/*.py as child processes | none | none |
| devtools/LevelRandomProbe | 55 | Devtools | debug stand-in for a Level's random ([debug.level_random]) | none | none |
| devtools/WorldJeiDump | 74 | Devtools | /dumpworldjei | none | `@Mod.EventBusSubscriber(FORGE)`, `@SubscribeEvent`, command `dumpworldjei` |
| devtools/WorldJeiMobBuckets | 74 | Devtools | dump part: bucketable mobs | none | none |
| devtools/WorldJeiToolPairs | 356 | Devtools | dump part: tool pairs | none | none |
| devtools/WthitIconDump | 165 | Devtools | /cedumpicons client icon audit | compat.BlockModelIcons (Common) | `@Mod.EventBusSubscriber(CLIENT)`, 2 `@SubscribeEvent`, command `cedumpicons` |

### excavation (Engineering)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| excavation/VeinUndoAccess | 66 | Engineering | reflective access to Ore Excavation internals | none | none |
| excavation/VeinUndo | 67 | Engineering | rules on /undo_excavation (OreExcavationCommandUndoMixin helper) | none | none |

### fan (Engineering)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| fan/CatalystUtils | 63 | Engineering | direction helpers for the dragon-head catalyst | none | none |
| fan/DragonBreathingFanProcessingType | 136 | Engineering | Create fan processing type; its JEI page part (compat/DragonBreathingJEI) is CB per creative_brewing.md 4.2, no code edge here | none | none (registered via Create's FanProcessingTypeRegistry in CreativeEngineering, sweep D) |
| fan/DragonBreathingRecipe | 92 | Engineering | the processing recipe | none | none |
| fan/DragonBreathingRecipeType | 72 | Engineering | registers serializer and recipe type | none | `DeferredRegister<RecipeSerializer>`, `DeferredRegister<RecipeType>` (creativeengineering), `register(IEventBus)` |
| fan/duck/ProcessingRecipeExt | 15 | Engineering | duck on Create's ProcessingRecipe | none | none |
| fan/ExclusiveResults | 48 | Engineering | exclusive chance outputs for Create processing recipes | none | none |

### fluid (Engineering)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| fluid/FluidBridge | 93 | Engineering | liquid-XP fluid lookup (Create Enchantment Industry) for the XP mixins (XPFluidManager, XpPumpUpgradeWrapper, MemoryStickItem, PneumaticCraftUtils); not the FluidTagInput API of plan 2.4 | none | none (a commented-out diagnostic call in CreativeEngineering) |

### fluidbrewing (CB)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| fluidbrewing/DragonBreathBrewing | 112 | CB | fluid-driven potion conversion | none | none |
| fluidbrewing/Lingerification | 125 | CB | IE Refinery lingering conversion (IE potion machine) | none | none |
| fluidbrewing/PotionMixDedup | 103 | CB | dedups Alex's Mobs potion mixes | none | none |

### food (CD, one row CB)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| food/CanServings | 210 | CD | multi-serving cans; read by ItemStackCanServingsMixin, compat/KubeJSCanAction, oven/BakingRecipe, CreativeEngineering | none | `@SubscribeEvent` (LivingEntityUseItemEvent.Finish) |
| food/CanServingsTooltip | 23 | CD | servings tooltip line | none | `@SubscribeEvent` ItemTooltipEvent |
| food/IntoxicatingDrinkTooltip | 81 | CB | alcohol: replaces Quark's saturation word on Intoxication drinks; named in creative_brewing.md 4.2 phase 2 | none | `@Mod.EventBusSubscriber(CLIENT)`, `@SubscribeEvent(LOWEST)` ItemTooltipEvent |
| food/PlacedFoodSol | 130 | CD | Spice of Life for placed pies and cakes | worldgen.WorldgenFiles (Worldgen; a plain file helper, better a Common util) | `@SubscribeEvent` (PlayerInteractEvent.RightClickBlock); `ModList.isLoaded` solapplepie, farmersdelight |
| food/PowderedSugarBlock | 39 | CD | powdered sugar block behaviour | none | none |
| food/PowderedSugarRegistry | 44 | CD | registers powdered_sugar_block and its bucket | none | `DeferredRegister<Block>`, `DeferredRegister<Item>` (creativeengineering), `register(IEventBus)` |
| food/SeasonedItemModel | 166 | CD | seasoning overlay item models (client) | none | `@Mod.EventBusSubscriber(MOD, CLIENT)`, 2 `@SubscribeEvent` (model bake, additional models) |
| food/SeasoningRecipe | 174 | CD | shapeless food + seasoning recipe | none | `DeferredRegister<RecipeSerializer>` (creativeengineering), `register(IEventBus)` |
| food/Seasonings | 223 | CD | seasoning NBT and effect application | none | `@SubscribeEvent` (LivingEntityUseItemEvent.Finish) |
| food/SeasoningTooltip | 85 | CD | one tooltip line per seasoning | none | `@SubscribeEvent` ItemTooltipEvent |

### gear (Equipment)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| gear/AirBrakeHandler | 154 | Equipment | air brake item behaviour | none | `@SubscribeEvent` (item use) |
| gear/ElytraDuplicationRecipe | 117 | Equipment | elytra duplication crafting recipe | none | none |
| gear/ElytraDuplicationSerializers | 59 | Equipment | registers elytra_duplication serializer | none | `DeferredRegister<RecipeSerializer>` (creativeengineering), `register(IEventBus)` |

### item (Engineering)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| item/BottledAir | 69 | Engineering? | Bottled Air port (glass bottle refills air under water); a player-item port with no machine, Equipment is the alternative | none | `@SubscribeEvent` (PlayerInteractEvent / LivingEntityUseItemEvent) |
| item/CreativeEngineeringItems | 40 | Engineering | registers the component items (precision sawblade, creosote sawdust, flint and terrasteel). Note: file sits in `item/` but declares `package ...items` | none | `DeferredRegister<Item>` (creativeengineering), `register(IEventBus)` |
| item/CreosoteSoakedSawdustItem | 20 | Engineering | fuel item | none | none |
| item/FlintAndTerraSteelItem | 56 | Engineering | places Dark Fire; header says TEMPORARY testing behaviour | block.CreativeEngineeringBlocks (Engineering) | none |
| item/PrecisionSawbladeItem | 25 | Engineering | sawblade component for the sawmill routing | none | none |

### itemphysic (Compat)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| itemphysic/AllurementItemCurses | 61 | Compat | re-applies Allurement curses to dropped items | none | none; `ModList.isLoaded("allurement")` |
| itemphysic/BurnBlastDamage | 25 | Compat | damage source of burn explosions | none | none |
| itemphysic/ItemBurnTransform | 225 | Compat | fire/lava item transforms (explode, spawn shrapnel) | shrapnel.ShrapnelSpawner (Engineering): Compat-to-Engineering edge, needs a soft check or a Common hook | none |
| itemphysic/ItemPhysicSplit | 218 | Compat | burning split and per-item float registry (ItemPhysic tweaks) | none | none |

### lunchbox (CD)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| lunchbox/client/LunchboxClient | 52 | CD | item properties for lunchbox model overrides | none | none (called from CreativeEngineering client setup, sweep D) |
| lunchbox/LunchboxDyeRecipe | 96 | CD | dye recipe | none | none |
| lunchbox/Lunchboxes | 217 | CD | tiers, skins, dye, contents API | none | none |
| lunchbox/LunchboxItems | 42 | CD | Netherite Lunchbox. Registers under the `solapplepie` namespace, not creativeengineering (rule 1 note) | none | `DeferredRegister<Item>` (solapplepie), `register(IEventBus)` |
| lunchbox/LunchboxRecipes | 34 | CD | registers the lunchbox recipe serializers | none | `DeferredRegister<RecipeSerializer>` (creativeengineering), `register(IEventBus)` |
| lunchbox/LunchboxShapedRecipe | 71 | CD | shaped tier upgrade | none | none |
| lunchbox/LunchboxSkinRecipe | 62 | CD | smithing skin recipe | none | none |
| lunchbox/LunchboxSoapRecipe | 70 | CD | Supplementaries soap clears dye/skin | none | none |
| lunchbox/LunchboxTooltip | 24 | CD | tooltip line | none | `@SubscribeEvent` ItemTooltipEvent |
| lunchbox/LunchboxUpgradeRecipe | 72 | CD | smithing tier upgrade keeping contents | none | none |

### oven (CD)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| oven/BakingRecipe | 266 | CD | creativeengineering:baking recipe | food.CanServings (CD) | none (serializer in OvenRegistry) |
| oven/client/OvenScreen | 86 | CD | oven GUI | cooking.client.RemainderSlotFrames (CD) | none |
| oven/jei/BakingRecipeCategory | 147 | CD | JEI category; shows only CD data (interface test) | none | none |
| oven/jei/OvenJeiPlugin | 60 | CD | JEI plugin for baking; shows only CD data | none | `@JeiPlugin` |
| oven/OvenBlockEntity | 536 | CD | the oven | cooking.RemainderBuffer, cooking.RemainderOutputHandler (CD) | none |
| oven/OvenBlock | 173 | CD | oven block | none | none |
| oven/OvenHeat | 61 | CD | heat-source tag rule | none | none |
| oven/OvenItemHandler | 76 | CD | automation face | none | none |
| oven/OvenMenu | 175 | CD | menu | cooking.RemainderSlot (CD) | none |
| oven/OvenRegistry | 76 | CD | everything the oven registers | none | `DeferredRegister` x5: Block, Item, BlockEntityType, MenuType, RecipeSerializer (creativeengineering), `register(IEventBus)` |
| oven/OvenResultSlot | 57 | CD | result slot | none | none |

### potion (CB)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| potion/CEPotions | 99 | CB | potions for effects KubeJS cannot build | none | `DeferredRegister<Potion>` (creativeengineering), `register(IEventBus)`; `ModList.isLoaded(row.needsMod())` |
| potion/CompoundEffects | 135 | CB | compound effects | none | none |
| potion/CompoundEffectTooltip | 97 | CB | compound effect parts in item tooltips | none | `@Mod.EventBusSubscriber(CLIENT)`, `@SubscribeEvent(LOW)` ItemTooltipEvent |
| potion/CopperPoisoningHandler | 115 | CB | copper poisoning from oxidized gear (gear effect, brewing 4.2) | none | `@SubscribeEvent` (LivingEvent tick) |
| potion/EffectCategories | 61 | CB | [effects] categoryOverrides; read by MobEffectCategory mixin (CB) | none | none |
| potion/EffectRules | 147 | CB | [effects] remap/removed/forceHudIcon; read by 4 effect mixins (CB) | none | none |
| potion/MagicResistanceEffect | 122 | CB | the magic_resistance effect logic | damage.DamageTags (Equipment?, see damage), shield.EffectTickContext (Common) | 2 `@SubscribeEvent` (LivingHurt, LivingAttack) |
| potion/PhantomBaneRules | 67 | CB | phantom prevention rule | none | 3 `@SubscribeEvent` |
| potion/PotionBucketDispenser | 101 | CB | dispenser behaviour of the KubeJS potion buckets | none | none (dispenser registration in CreativeEngineering, sweep D) |

### sawmill (Engineering)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| sawmill/duck/SawmillProcessExt | 25 | Engineering | duck on IE's sawmill process | none | none |
| sawmill/duck/SawmillRecipeExt | 16 | Engineering | duck on IE's SawmillRecipe | none | none |
| sawmill/SawbladeRouting | 72 | Engineering | blade-aware IE sawmill recipe selection | none | none |

### seasons (Calendar)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| seasons/CropSeasons | 364 | Calendar | crop growing seasons by crop type (creative_calendar.md 2); read by compat/WthitCropSeasons (Common interface row showing Calendar data: should follow to Calendar) | none | `@Mod.EventBusSubscriber`, 3 `@SubscribeEvent` (LOW crop growth, tags updated, server start) |
| seasons/CropSeasonTooltip | 106 | Calendar | "Fertile: Spring" tooltip and WTHIT summary | none | `@Mod.EventBusSubscriber(CLIENT)`, `@SubscribeEvent` ItemTooltipEvent |
| seasons/SeasonTypes | 504 | Calendar | season types by biome tag over Serene Seasons; read by 9 Season*/SeasonHud*/PrettyRain mixins | none | `@Mod.EventBusSubscriber`, `@SubscribeEvent` |

### shield (Equipment; EffectTickContext Common)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| shield/client/TierShieldClient | 31 | Equipment | client item properties for tier shields | none | none (called from client setup, sweep D) |
| shield/client/TierShieldMaterials | 60 | Equipment | per-tier shield base materials | none | `@Mod.EventBusSubscriber(MOD, CLIENT)`, `@SubscribeEvent` (texture stitch / model) |
| shield/client/TierShieldRenderer | 90 | Equipment | BEWLR for tier shields | none | `@OnlyIn(CLIENT)` |
| shield/EffectTickContext | 49 | Common | which effect is ticking on which entity; set by MobEffectTickContextMixin, read by shield and potion (CB): the shared API of plan 2.4 | none | none |
| shield/ElementalKind | 59 | Equipment | elemental sub-kinds by damage tag | damage.DamageTags (Equipment?) | none |
| shield/IronShieldTooltip | 27 | Equipment | tooltip on the vanilla shield as the iron rung | none | `@SubscribeEvent` ItemTooltipEvent |
| shield/ShieldBlockHandler | 142 | Equipment | after-block wear and effects | none | 2 `@SubscribeEvent` (ShieldBlockEvent, LivingHurt) |
| shield/ShieldBlocking | 93 | Equipment | can this shield block this hit | damage.DamageFlag, damage.DamageTags (Equipment?) | none |
| shield/ShieldCategory | 86 | Equipment | hit classification by damage tag | damage.DamageTags (Equipment?) | none |
| shield/ShieldConfig | 213 | Equipment | null-safe reads of CEConfig.SHIELDS | none | none |
| shield/ShieldEffects | 133 | Equipment | on-block potion effects parsed from the tier config; not in the brewing 4.2 gear-effect list, stays with shields | none | none |
| shield/ShieldGroup | 83 | Equipment | physical/elemental/magic groups | damage.DamageTags (Equipment?) | none |
| shield/ShieldItems | 42 | Equipment | registers <tier>_shield items | none | `DeferredRegister<Item>` (creativeengineering), `register(IEventBus)` |
| shield/ShieldTickGate | 69 | Equipment | repeating environmental damage gate | damage.DamageFlag, damage.DamageTags (Equipment?) | none |
| shield/ShieldTier | 252 | Equipment | the tier ladder | none | none |
| shield/ShieldTooltip | 137 | Equipment | shared tooltip lines | none | none (called from TierShieldItem, IronShieldTooltip) |
| shield/TierShieldItem | 112 | Equipment | the seven tier shield items | none | none |

### shrapnel (Engineering)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| shrapnel/CreativeEngineeringEntities | 32 | Engineering | registers the shrapnel entity type | none | `DeferredRegister<EntityType>` (creativeengineering), `register(IEventBus)` |
| shrapnel/ShrapnelEntity | 119 | Engineering | the projectile | none | none |
| shrapnel/ShrapnelSpawner | 58 | Engineering | spawns a burst; called by itemphysic/ItemBurnTransform (Compat) | none | none |
| shrapnel/TntShrapnelHandler | 37 | Engineering | TNT explosion hook | none | `@SubscribeEvent` ExplosionEvent (registered on the Forge bus by CreativeEngineering) |

### stepstool (Equipment)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| stepstool/client/StepstoolClient | 136 | Equipment | step keys, input, stool render | none | 3 `@SubscribeEvent` (key input, render, client tick) |
| stepstool/StepstoolCurios | 20 | Equipment | Curios slot hook for stepstools | none | none (plain helper) |
| stepstool/Stepstool | 350 | Equipment | stepstool mechanics (Curios feet slot) | none | 8 `@SubscribeEvent` (player tick, hurt, jump, dismount, logout, ...) |

### surface (Engineering)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| surface/CamoSpeed | 117 | Engineering | framed/copycat blocks walk like their camo (BlockRunner) | none | none; `ModList.isLoaded` framedblocks, copycats |
| surface/CamoSpeedTooltip | 38 | Engineering | rewrites BlockRunner's speed tooltip on camo blocks | none | `@SubscribeEvent(LOW)` ItemTooltipEvent |
| surface/CopycatsPlusCamo | 48 | Engineering | Copycats+ camo lookup | none | none |
| surface/FramedCamo | 40 | Engineering | FramedBlocks camo lookup | none | none |
| surface/LastInsertedCamo | 10 | Engineering | duck on FramedDoubleBlockEntity | none | none |
| surface/LastInsertedMaterial | 11 | Engineering | duck on Copycats+ MaterialItemStorage | none | none |
| surface/RunSpeed | 234 | Engineering | the real walk speed a block gives (for WthitRunSpeed) | none (CEConfig.SpeedFormat only) | none |
| surface/SinkingRender | 78 | Engineering | visual soul-sand sinking (client) | none | none (called from client setup, sweep D) |
| surface/StickyFacing | 65 | Engineering | blockstate-aware sticky tier | none | none |
| surface/StuckInBlock | 150 | Engineering | blocks that slow from inside | none | none |
| surface/SurfaceSpeeds | 85 | Engineering | [surface.speed] drives BlockRunner's tiers | none | `@SubscribeEvent` ModConfigEvent (COMMON_SPEC check); `ModList.isLoaded("blockrunner")` |
| surface/SurfaceTier | 64 | Engineering | BlockRunner surface tiers | none | none |
| surface/WthitRunSpeed | 89 | Engineering | WTHIT "Run Speed" line; shows only Engineering data (interface test), listed in waila_plugins.json (sweep E) | none | WTHIT plugin (IWailaPlugin); `MinecraftForge.EVENT_BUS.addListener(TagsUpdatedEvent)` |

### time (Calendar)
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| time/CalendarDate | 40 | Calendar | calendar date on the SUN clock | none | none |
| time/SeasonClock | 152 | Calendar | seasons following the sun | none | 3 `@SubscribeEvent` (server tick / level events); `ModList.isLoaded("sereneseasons")` |
| time/TimeClient | 98 | Calendar | Serene Seasons calendar tooltip takes over the Vinery calendar's job | seasons.SeasonTypes (Calendar) | `@Mod.EventBusSubscriber(CLIENT)`, 2 `@SubscribeEvent` |
| time/TimeCommand | 138 | Calendar | /cetime | none | `@SubscribeEvent` RegisterCommandsEvent, command `cetime` (push, set day/season/wine, date) |
| time/TimeDate | 253 | Calendar | /cetime date subcommand | none | builds `Commands.literal("date")` for TimeCommand |
| time/TimePause | 19 | Calendar | [time] pauseWhenEmpty | none | none |
| time/TimePresets | 139 | Calendar | [time] preset sets | none (CEConfig.Time.Preset) | none |
| time/TimePush | 107 | Calendar | pushes [time] into Serene Seasons and SeasonHUD configs | none | `@SubscribeEvent` (ModConfigEvent); `ModList.isLoaded` sereneseasons, seasonhud |
| time/TimeSettings | 130 | Calendar | derived [time] values; read by Xaero, BetterDays, SereneSeasons clock and SeasonHud mixins | none | none |
| time/WineTime | 56 | Calendar | read-only [time] view for the Vinery port (CB); CB depends on Calendar, so this edge points down | none | none |

### root classes
| class | lines | target | reason | calls into other targets | registrations and event subscribers |
|---|---|---|---|---|---|
| CEConfig | 3639 | Common | the config framework; its ~60 sections move with their features per sweep C | none | none (COMMON_SPEC registered by CreativeEngineering) |
| CETiers | 24 | Equipment | copper tool tier and needs_copper_tool tag; read by mixin/CopperPickaxeItemMixin | none | none |
| CreativeEngineering | 576 | Common | the mod entry; sweep D splits its contents by registration | fluid.FluidBridge (Engineering, commented out), compat.IECompat (A1), items.CreativeEngineeringItems, block.CreativeEngineeringBlocks, shrapnel.CreativeEngineeringEntities, shrapnel.TntShrapnelHandler (Engineering), plus the many `register(...)` calls listed in sweep D | `@Mod`, `DeferredRegister<Block>` BLOCKS, 5 `@SubscribeEvent`, nested `@Mod.EventBusSubscriber(MOD, CLIENT)`, `MinecraftForge.EVENT_BUS.register(IECompat.class)` |

### A2 summary

| target | classes | lines |
|---|---|---|
| CD | 45 | 4583 |
| Engineering | 42 | 3003 |
| Equipment | 35 | 3433 |
| Calendar | 13 | 2106 |
| CB | 13 | 1365 |
| Devtools | 9 | 1954 |
| Compat | 4 | 529 |
| Worldgen | 3 | 255 |
| Common | 3 | 4264 |
| total | 167 | 21492 |

Rows that moved from the 2.1 to 2.4 draft: `block/FastLeafDecayPort`, `LeafDecayPiles`, `LeafDecayRange` to Worldgen (with
the leaf decay mixins); `food/IntoxicatingDrinkTooltip` to CB (brewing 4.2); `shield/EffectTickContext` to Common
(plan 2.4); `CETiers` to Equipment.

Doubtful rows (`?`):
- `damage/*` (5 classes, 564 lines, Equipment?): `DamageTags` is read by shield (Equipment) and by `potion/MagicResistanceEffect`
  (CB). Either the whole package becomes a Common API, or CB soft-depends on Equipment. Recommendation: Common.
- `campfire/CampfireComfort`, `CampfireRain`, `CampfireRainTicks` (CD?): neither is food. Comfort grants an effect
  (CB if the gear-effect rule covers it); Rain is a weather rule on a block (Engineering or Calendar).
- `block/MossTint` (Engineering?): client-only biome tint of moss; Worldgen is the alternative.
- `block/SaltBlockTicks` (Engineering?): the one-salt item merge; CD if salt is treated as food.
- `block/VineShearing`, `VineShearingClient` (Engineering?): Quark Shear Vines extended to modded vines; Compat or Worldgen.
- `item/BottledAir` (Engineering?): player-item port with no machine; Equipment is the alternative.

Cross-target calls (class to a different target):
- `potion/MagicResistanceEffect` (CB) -> `damage.DamageTags` (Equipment?) and `shield.EffectTickContext` (Common). The only
  class in this sweep that calls into two other targets.
- `itemphysic/ItemBurnTransform` (Compat) -> `shrapnel.ShrapnelSpawner` (Engineering).
- `food/PlacedFoodSol` (CD) -> `worldgen.WorldgenFiles` (Worldgen; a file helper that belongs in Common `util/`).
- `devtools/WthitIconDump` (Devtools) -> `compat.BlockModelIcons` (Common): points down, fine.
- `time/WineTime` (Calendar) is read by CB's Vinery port: points down, fine.
- `CreativeEngineering` (Common) -> Engineering registrars (`items.CreativeEngineeringItems`, `block.CreativeEngineeringBlocks`,
  `shrapnel.*`, `fluid.FluidBridge` commented out) and `compat.IECompat`: resolved by sweep D.
- Common interface classes that read this sweep's content (reverse edges, for the interface test): `compat/StateLines` ->
  `crops.CropGrowth` (CD), `compat/WthitRemainderBuffer` -> `cooking.RemainderBuffer` (CD), `compat/WthitCropSeasons` ->
  `seasons.CropSeasons` (Calendar), `compat/KubeJSCanAction` -> `food.CanServings` (CD). Those compat rows show one content
  jar's data and should follow it (A1 decides).
- Mixin readers (sweep B): `fluid.FluidBridge` (4 XP mixins), `time.TimeSettings` (4), `seasons.SeasonTypes` (9),
  `potion.EffectRules` (4), `shield.EffectTickContext` (1), `CETiers` (1), `food.CanServings` (1).

Other notes: `lunchbox/LunchboxItems` registers under the `solapplepie` namespace (rule 1 says ids stay, so this keeps
registering there). `item/CreativeEngineeringItems.java` sits in `item/` but declares `package ...items`.
`item/FlintAndTerraSteelItem` is headed TEMPORARY testing behaviour. The `oven/jei` plugin and `surface/WthitRunSpeed`
are interface rows that show only their jar's data and stay with CD and Engineering respectively.
