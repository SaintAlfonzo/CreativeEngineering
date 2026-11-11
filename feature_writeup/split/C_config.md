# Sweep C: config

Audit of `CEConfig.java` (`PM/CEConfig.java`, 3639 lines) for [creative_split.md](../creative_split.md) section 4.
Read-only, 2026-11-01. Targets use the vocabulary of [README.md](README.md).

Method: sections are the 50 top-level tables of `config/creativeengineering-common.toml` (one `public static final class`
each; `[worldgen]` is 17 classes, `[crop_drops]` 2). Readers: `grep CEConfig.<FIELD>` over patcherMod Java, Creative
Delight, Creative Brewing, `kubejs/`, the jar `kubejs_startup/` folders and `tools/`; indirect readers through
`WorldgenConfig`, `ShieldConfig`, `SurfaceSpeeds`, `MobCapLevels`, `EffectRules`, `TimeSettings`, `ModDisplayNames`
were followed one hop. Key counts are keys in the toml (394 define calls in the class). Edited values: a script
(`cfg_defaults.py`, scratchpad) compared every toml value with the literal default in the define call; keys with
computed defaults (enum maps, loops) were spot-checked by hand (see section 4).

Facts that frame every row:

- The spec is one `ForgeConfigSpec` (`COMMON_SPEC`) registered in `CreativeEngineering.java:79` as
  `creativeengineering-common.toml`. Nine files call `CEConfig.COMMON_SPEC` (`isLoaded()` guards and reload hooks):
  `CreativeEngineering`, `mixin/ArmorCapMixin`, `mixin/ServerAdvancementManagerMixin`, `surface/SurfaceSpeeds`,
  `time/TimeCommand`, `TimePresets`, `TimePush`, `TimeSettings`, `util/ModDisplayNames`. Each must point at its own
  jar's spec after the move.
- `config/creativeengineering-common.toml` and `configs_edited/root_config/creativeengineering-common.toml` are
  identical (2654 lines). The working tree has an uncommitted edit to both that only adds the new `reachPotionMb`
  keys at their defaults (`[crafting]`).
- CD and CB already depend on the class: `creativeDelight/build.gradle:23`, `creativeBrewing/build.gradle:17`
  ("patcherMod is a hard dependency"). `CDConfig.java:72,79` and `BAConfig.java:7` point players at
  `creativeengineering-common.toml [cooking]`, `[crop_growth]` and the already-moved `[kitchen]`.

## 1. Section table

`PM/` prefix dropped from reader paths; `CD:` = `development/creativeDelight/src/main/java/.../creativedelight/`,
`js:` = a KubeJS script. `readers' targets` is where sweeps A, B and F are expected to put those files.

| section | lines in CEConfig | keys | target jar | reader files | readers' targets | flag |
|---|---|---|---|---|---|---|
| `backpackSkins` (BackpackSkins) | 144-158 | 1 | Equipment | backpackskin/BackpackSkinCurios, mixin/BackpackSkinEquipMixin | Equipment | |
| `forgiving_void` (ForgivingVoid, 3 override subtables) | 159-225 | 15 | Worldgen | forgivingvoid/ForgivingVoidBridge | Worldgen | |
| `dragon_gear` (DragonGear, `.autorepair`) | 226-253 | 4 | Equipment | mixin/ArmorCapMixin; js: server_scripts/equipment/dragon_autorepair.js (`autorepairEnabled`, `autorepairIntervalTicks`, `autorepairAmountPerInterval`) | Equipment | |
| `stepstool` (Stepstool) | 254-282 | 5 | Equipment | stepstool/Stepstool, stepstool/client/StepstoolClient | Equipment | |
| `air_brake` (AirBrake) | 283-375 | 11 | Equipment | gear/AirBrakeHandler, CreativeEngineering (registration); js: startup_scripts/items/air_brake.js (`useDurationTicks`) | Equipment | |
| `crafting` (Crafting, 10 `felsic_*` subtables via FelsicPiece) | 376-446 | 50 | Equipment | no Java reader; js: server_scripts/equipment/end_gear.js via `NCH.cfg(..., 'CRAFTING.FELSIC_<PIECE>')` | Equipment | |
| `magic_resistance` (Magic) | 447-477 | 2 | CB | potion/MagicResistanceEffect | CB | |
| `copper_poisoning` (CopperPoisoning) | 478-519 | 8 | CB | potion/CopperPoisoningHandler, CreativeEngineering | CB | |
| `botania` (Botania) | 520-536 | 1 | Compat? | compat/TrinketCaseRules | Compat | |
| `armor_enchantability` (ArmorEnchant) | 537-567 | 1 | Equipment | mixin/ArmorMaterialEnchantabilityMixin, util/ArmorEnchantabilityBridge | Equipment (the util/ bridge is Equipment content, not Common util) | |
| `shrapnel` (Shrapnel) | 568-626 | 8 | Engineering | shrapnel/ShrapnelEntity, shrapnel/ShrapnelSpawner, CreativeEngineering | Engineering | |
| `bonemeal_charm` (BonemealCharm) | 627-659 | 4 | Equipment | mixin/BonemealCharmMixin | Equipment | |
| `crop_growth` (CropGrowth) | 660-693 | 2 | CD | CD:compat/wthit/WthitCropGrowth (`showHarvestable`, `fallback`) | CD | |
| `crop_drops` (CropDrops, `.bell_peppers`; SoilYield `.soil_yield`, `.soil_yield.crops`) | 694-800 | 42 | CD | no Java reader; js: kubejs/data/creativedelight/kubejs/creativedelight/soil_yield.js (`Java.loadClass(...).CROP_DROPS.SOIL_YIELD` and `NCH.cfg(..., 'CROP_DROPS.SOIL_YIELD')`), kubejs/data/creativedelight/kubejs/rusticdelight/crop_drops.js (`NCH.cfg(..., 'CROP_DROPS')`) | CD | |
| `stem_fruit` (StemFruit) | 801-835 | 5 | CD | crops/StemExtraFruit | CD | |
| `leaf_decay` (LeafDecay) | 836-902 | 7 | Worldgen | block/FastLeafDecayPort, block/LeafDecayPiles, block/LeafDecayRange | Worldgen (leaf decay is a Worldgen row although in `block/`) | |
| `leaf_piles` (LeafPiles) | 903-927 | 1 | Compat | mixin/LeafPileSlowMixin; surface/StuckInBlock (Run Speed line) | Compat; Engineering | SPLIT (one key read from two jars; see 2) |
| `dandelion_maturing` (DandelionMaturing) | 928-949 | 2 | CD? | crops/DandelionMaturing | CD (`crops/` routes to CD; dandelions are a wild flower, Worldgen is the alternative) | |
| `moss` (Moss) | 950-975 | 2 | Worldgen? | block/MossTint | Worldgen? (biome-tinted vanilla block, client only; Common is the alternative) | |
| `vine_shearing` (VineShearing) | 976-1004 | 3 | Worldgen? | block/VineShearing, block/VineShearingClient | Worldgen? (plant behaviour; the sheared state's WTHIT line is `[wthit] sheared`) | |
| `campfire` (Campfire) | 1005-1049 | 7 | CD | campfire/CampfireComfort, campfire/CampfireRain, compat/WthitCampfire, compat/jei/world/CampfireRows | CD (the WTHIT and JEI rows show only campfire data) | |
| `raids` (Raids) | 1050-1066 | 2 | Worldgen | mixin/RaidGlowingMixin | Worldgen | |
| `monster_box` (MonsterBox) | 1067-1098 | 6 | Worldgen? | mobs/MonsterBoxVault | Worldgen (`mobs/`); Compat if the owner treats it as a Quark tweak | |
| `bottled_air` (BottledAir) | 1099-1129 | 5 | Equipment? | item/BottledAir | Equipment? (a worn/used item port, not an Engineering component) | |
| `perf` (Perf) | 1130-1246 | 18 | Common | perf/MobCapLevels (+ mixin/MobCapMixin, MobCapLevelTickMixin, MobCapSpawnAreaMixin, perf/PerfCommand via it); mixin/FeedingTroughModuleMixin; mixin/SereneSeasonsMeltThrottleMixin; mixin/CreateGravityDimensionMixin; js: server_scripts/magic/armor_effects.js (`mountEffectIntervalTicks`) | Worldgen (13 mob cap and spawn keys), CD (trough), Calendar (seasons melt), Common or Engineering (gravity), CB (mount effect interval) | SPLIT MOVE |
| `cooking` (Cooking) | 1247-1292 | 4 | CD | cooking/RemainderBuffer, oven/BakingRecipe, CD:qol/RemainderBufferQol; js: jar `kubejs_startup/creativeengineering/func/neoCraftHelper.js` (`NCH.cfg({ovenIngredientCounts}, 'COOKING')`) | CD; NCH is Common (reads a CD key, see 3) | |
| `coaster` (Coaster) | 1293-1315 | 1 | CB | compat/WthitCoaster | CB (Brewin' port lives in CB; the line shows only coaster data) | |
| `wthit` (Wthit, `.state_lines`) | 1316-1493 | 33 | Common | compat/BlockModelIcons, WthitBlockModelIcon, WthitStateLines, WthitSilkTouch, WthitOperatorBlocks, WthitHolderContents, WthitHive, WthitSheared, WthitCampfire, WthitCropSeasons, WthitKitchenStation, WthitPotContents, WthitRemainderBuffer, WthitSheetmetalTank | Common (icons, state lines, silk touch, operator blocks, holders, hive); CD (campfire, kitchen station, pot, remainder buffer); Calendar (crop seasons); Engineering (sheetmetal tank); Worldgen? (sheared) | SPLIT |
| `alcohol` (Alcohol) | 1494-1513 | 1 | CB | food/IntoxicatingDrinkTooltip | CB | |
| `canning` (Canning) | 1514-1552 | 5 | CD | food/CanServings | CD | |
| `seasons` (Seasons) | 1553-1591 | 3 | Calendar | seasons/SeasonTypes | Calendar | |
| `crop_seasons` (CropSeasons) | 1592-1635 | 6 | Calendar | seasons/CropSeasons, seasons/CropSeasonTooltip | Calendar | |
| `time` (Time, `.day`, `.seasons`, `.wine`, `.wine.boosts`) | 1636-1728 | 17 | Calendar | time/TimeSettings, TimeCommand, TimeDate, TimePause, TimePresets, TimePush, WineTime; mixin/SeasonHudCurrentSeasonMixin (+ Xaero, BetterDays, SereneSeasons clock mixins via TimeSettings) | Calendar | |
| `surface` (Surface; nested Sticky, Camouflage, Sinking, Wthit) | 1729-1873 | 35 | Engineering | surface/SurfaceSpeeds, CamoSpeed, SinkingRender, StickyFacing, WthitRunSpeed, RunSpeed; mixin/BlockRunnerSpeedConfigMixin; CreativeEngineering | Engineering (`surface.wthit` shows only surface data) | |
| `armor_overflow` (ArmorOverflow) | 1874-1907 | 3 | Common | compat/ArmorOverflowRenderer | Common (armor bar is Interface) | |
| `jei` (Jei) | 1908-1987 | 8 | Common | compat/jei/CraftableFirstRecipeLayoutList, PackIngredientOrder, PackOrderReport, FluidTankScale, mixin/JeiRecipeGuiLogicMixin, mixin/JeiFluidTankScaleMixin; compat/jei/CanPagesJEI; compat/jei/SeasoningJEI; compat/jei/PotionPagesJEI | Common (4 keys); CD (`canCategories`, `seasoningCategory`); CB (`potionBucketCategory`, `tippedArrowCategory`) | SPLIT |
| `gui_title` (GuiTitle) | 1988-2014 | 2 | Common | compat/guititle/TitlePlaques | Common | |
| `toasts` (Toasts) | 2015-2037 | 2 | Common | mixin/ToastFilterMixin | Common | |
| `changelog` (Changelog) | 2038-2070 | 4 | Common | changelog/ChangelogButtons | Common | |
| `shields` (Shields, `.facing`, 8 tier tables each with `.reduction`, `.durabilityCost`, `.elemental_breakdown`; TierCfg) | 2071-2260 | 143 | Equipment | shield/ShieldConfig (+ ShieldTier, ShieldGroup, ElementalKind, TierShieldItem, ShieldBlocking, ShieldBlockHandler, ShieldTooltip), mixin/VanillaShieldDurabilityMixin, VanillaShieldRepairMixin, MobShieldDisableMixin, ShieldDisableCooldownMixin, CreativeEngineering | Equipment (`shield/EffectTickContext` stays Common per 2.4 but reads no config) | |
| `damage_tags` (DamageTagConfig) | 2261-2306 | 1 | Equipment | damage/DamageTagLogger | Equipment | |
| `worldgen` (Worldgen + WorldgenSystems `.systems`, WorldgenDebug `.debug`, PoolTuning `.pools`, BiomeSolver `.biome_solver`, GroundMatch `.ground_match`/`.sampling`, EarthFill `.earth_fill`, EarthFillSizes `.sizes`, WaterFill `.water_fill`, TerrainAdaptation, OceanGeodes, PlacementPriorityCfg, LocateCfg `.locate`, FirstTickWaterCfg, DistantCfg `.distant`, MetaStructures, MassPlace) | 2307-3143 | 110 | Worldgen | worldgen/WorldgenConfig (sole direct reader; 30 `worldgen/` classes read through it), mixin/StructureCheckThreadSafeMixin, mixin/QuarkPermafrostStyleMixin (`systems.permafrostMerge`) | Worldgen (the permafrost mixin is a Quark tweak inside the worldgen system; stays Worldgen) | |
| `vein_undo` (VeinUndo) | 3144-3166 | 2 | Engineering | excavation/VeinUndo | Engineering | |
| `trash_slot` (TrashSlot) | 3167-3247 | 5 | Compat | compat/TrashSlotCompat | Compat | |
| `wandering_trader` (WanderingTrader) | 3248-3284 | 4 | Worldgen? | trader/WanderingTraderTweaks (+ WanderingTraderSpawnerMixin, WanderingTraderTradesMixin) | Worldgen? (spawn rules are fauna; `trader/TradeListings` stays Common per 2.4, so `trader/` splits) | |
| `named_mobs` (NamedMobs) | 3285-3321 | 6 | Worldgen | mobs/NamedMobs | Worldgen | |
| `advancements` (Advancements) | 3322-3335 | 1 | Common | mixin/ServerAdvancementManagerMixin | Common | |
| `mod_names` (ModNames) | 3336-3472 | 1 | Common | util/ModDisplayNames (compat/CornDelightCompat and CreativeEngineering through it) | Common | |
| `effects` (Effects) | 3473-3537 | 5 | CB | potion/EffectCategories, potion/EffectRules (+ mixin/LivingEntityEffectRemapMixin, GuiEffectIconMixin, LivingEntityUseItemIconMixin, FruitsDelightEffectFuncMixin) | CB | |
| `debug` (Debug, `.generators`, `.block_state_sync`, `.names`, `.wthit`, `.recipe_scan`, `.level_random`) | 3538-3639 | 11 | Devtools | devtools/GeneratorCommand, BlockStateSyncDebug, DisplayNameDump, WthitIconDump, LevelRandomProbe; js: server_scripts/_lib/recipe_scan.js (`NCH.cfg({recipeScanAtLoad}, 'DEBUG')`) | Devtools | |

Section counts by target: Common 9 (`perf`, `wthit`, `jei` only at section level, see 2), Equipment 10, Worldgen 9
(4 with `?`), CD 7, CB 5, Engineering 3, Calendar 3, Compat 3, Devtools 1. Total 50.

## 2. Sections whose keys split between jars

### 2.1 `perf` (SPLIT, MOVE)

Section-level target is Common (Q3: perf is Common), but 17 of 18 keys are read by code the plan puts elsewhere.
Only the two gravity keys are arguably Common.

| keys | reader | target |
|---|---|---|
| `mobCapLevels`, `mobCapForceLevel`, `mobCapLevel1Chunks`, `mobCapLevel2Chunks`, `mobCapHysteresisChunks`, `mobCapLevel1Monster`, `mobCapLevel2Monster`, `mobCapScaledCategories`, `spawnChunksLevel1`, `spawnChunksLevel2`, `spawnBandLevel1`, `spawnBandLevel2`, `spawnBandCategories` (13) | perf/MobCapLevels, mixin/MobCapMixin, MobCapLevelTickMixin, MobCapSpawnAreaMixin, perf/PerfCommand | Worldgen (2.1: "mob caps and spawn areas" are fauna). `PerfCommand` reads the levels for its report: Devtools reading a Worldgen value, needs an API or a soft read |
| `troughMissCooldownTicks` | mixin/FeedingTroughModuleMixin | CD (troughs are CD, 2.2) |
| `seasonsMeltIntervalTicks` | mixin/SereneSeasonsMeltThrottleMixin | Calendar (Serene Seasons is Calendar's mod) or Common as a perf fix; Calendar fits "config keys move with their feature" |
| `gravityDimensions`, `gravityLimitDimensions` | mixin/CreateGravityDimensionMixin | Common? (a perf fix on Create; Engineering if the owner wants every Create mixin there) |
| `mountEffectIntervalTicks` | js: server_scripts/magic/armor_effects.js (gear effects) | CB (creative_brewing.md 4.2: gear effects) |

Recommendation: dissolve `[perf]`; each key goes to a `[perf]` table in its feature jar's toml, keeping its name and
comment. The two gravity keys stay in Common's `[perf]`.

### 2.2 `wthit` (SPLIT)

Section target Common (Interface). Per-row test "shows only one content jar's data":

| keys | reader | target |
|---|---|---|
| `iconBlockModel`, `iconForgePick`, `iconExclude`, `iconItems`, `iconSubstitutes`, `stateLine`, `stateLines`, `state_lines.*` (7), `silkTouch`, `silkTouchShowFalse`, `operatorBlocks`, `operatorBlockLevel`, `operatorBlockList`, `holderContents`, `holderPotionEffects`, `holderSingleName`, `hiveBees`, `hiveBeesOnTop`, `hiveHoneyMax` | BlockModelIcons, WthitBlockModelIcon, WthitStateLines, WthitSilkTouch, WthitOperatorBlocks, WthitHolderContents, WthitHive | Common (generic lines; the hive line is vanilla beehive data, not the bee-nest system). The `state_lines.*` switches (servings, slices, eggs, hatch, level, charges, pickles) label food and nest blocks but read `state_lines.json` generically: Common |
| `campfireBurn`, `campfireRain` | WthitCampfire (also reads `[campfire]`) | CD |
| `kitchenStation`, `potContents`, `remainderBuffer` | WthitKitchenStation, WthitPotContents, WthitRemainderBuffer | CD |
| `cropSeasons`, `cropOutOfSeason` | WthitCropSeasons | Calendar |
| `sheetmetalTank` | WthitSheetmetalTank | Engineering (IE tank contents) |
| `sheared` | WthitSheared | follows `vine_shearing` (Worldgen?) |

### 2.3 `jei` (SPLIT)

| keys | reader | target |
|---|---|---|
| `craftableFirst`, `packOrder`, `orderReport`, `fluidAmountScale` | CraftableFirstRecipeLayoutList, JeiRecipeGuiLogicMixin, PackIngredientOrder, PackOrderReport, FluidTankScale, JeiFluidTankScaleMixin | Common |
| `canCategories`, `seasoningCategory` | CanPagesJEI, SeasoningJEI | CD |
| `potionBucketCategory`, `tippedArrowCategory` | PotionPagesJEI | CB |

### 2.4 Single-key cross reads (no key split, but a cross-jar edge)

| section.key | owner | other reader | note |
|---|---|---|---|
| `leaf_piles.slowFromLayers` | Compat (LeafPileSlowMixin) | surface/StuckInBlock (Engineering, Run Speed line) | either the Run Speed line stops showing the pile slow, or the value goes through a Common API |
| `cooking.ovenIngredientCounts` | CD | jar `neoCraftHelper.js` `NCH.cfg(..., 'COOKING')` (Common) | NCH's oven recipe writer reads a CD key; after the move NCH.cfg must resolve `COOKING` on CD's config class (section 3) |
| `worldgen.systems.permafrostMerge` | Worldgen | mixin/QuarkPermafrostStyleMixin | Quark tweak but part of the worldgen system; stays Worldgen with the key |
| `perf.mobCap*` | Worldgen | perf/PerfCommand (Devtools) | report only |

## 3. Config read from KubeJS scripts

All script reads go through `Java.loadClass('com.SaintAlfonzo.creativeengineering.CEConfig')`, directly or through
`global.NCH.cfg(defaults, 'PATH')` (`neoCraftHelper.js:711-760`), which hard-codes that one class name and walks the
dotted path over its static fields. Every moved section breaks these unless `NCH.cfg` learns the new classes
(a class per jar, or a registry the jars fill at load) or the scripts load the new class themselves.

| script | section.key | how | script's target |
|---|---|---|---|
| `kubejs/startup_scripts/items/air_brake.js` | `air_brake.useDurationTicks` | `Java.loadClass` (startup; falls back to 72000) | Equipment |
| `kubejs/server_scripts/equipment/dragon_autorepair.js` | `dragon_gear.autorepairEnabled`, `autorepairIntervalTicks`, `autorepairAmountPerInterval` | `Java.loadClass` (falls back to off) | Equipment |
| `kubejs/server_scripts/equipment/end_gear.js` | `crafting.felsic_<piece>.*` (10 pieces) | `NCH.cfg` | Equipment |
| `kubejs/server_scripts/magic/armor_effects.js` | `perf.mountEffectIntervalTicks` | `Java.loadClass` | CB (gear effects) |
| `kubejs/data/creativedelight/kubejs/creativedelight/soil_yield.js` | `crop_drops.soil_yield.*`, `.crops.*` | `Java.loadClass(...).CROP_DROPS.SOIL_YIELD` and `NCH.cfg` | CD (already a CD script) |
| `kubejs/data/creativedelight/kubejs/rusticdelight/crop_drops.js` | `crop_drops.bell_peppers.*` | `NCH.cfg` | CD |
| `kubejs/server_scripts/_lib/recipe_scan.js` | `debug.recipe_scan.recipeScanAtLoad` | `NCH.cfg` | Devtools |
| jar `kubejs_startup/creativeengineering/func/neoCraftHelper.js` | `cooking.ovenIngredientCounts` | `NCH.cfg` inside NCH | Common reading CD |

Mentions only (log text or comments, no read): `server_scripts/worldgen/{structure_overrides,mob_spawns,wild_flowers,
bee_nests,mushroom_spawns}.js` (name `[worldgen.systems]` keys in their off messages), `startup_scripts/func/item_physics.js`
(`CEConfig.Shrapnel` comment), `startup_scripts/items/stepstools.js`, `func/trade_helper.js`, `jei/order.js`,
`cooking/potions.js`, `cooking/food_values.js`, `_lib/item_dict.js`, `server_scripts/tags/*.js`,
`economy/wandering_trades.js`. `tools/` has test harness copies and probes that import or reference the class
(`tools/remainder_buffer_test`, `tools/shield_test`, `tools/worldgen_test`, `tools/*probe*.js`); they are Devtools
and follow their feature.

## 4. Edited values (must move with the section)

`config/` and `configs_edited/root_config/` copies are identical. Compared against the define defaults:

| section | edited keys | note |
|---|---|---|
| `shields` | `reduction.physical` in copper 20 [default 40], gold 30 [50], iron 40 [60], diamond 50 [70], netherite 60 [80], echo 70 [90], felsic 80 [95]; dragon 100 = default | defaults are `ShieldTier` enum columns. `durability`, `repairItem`, copper `elemental_breakdown` match; `unblockableEffectTicks` matches `UNBLOCKABLE_EFFECT_TICKS_DEFAULT`. The remaining per-tier keys (`durabilityCost`, effects, `knockbackThreshold`, `blocks*Ticks`, magic/elemental reduction) come from tier maps and were not checked one by one |
| `damage_tags` | `logSeenSources = true` [false] | debug logging left on in the play config |
| `mod_names` | `overrides` list: 113 entries vs 117 defaults; the file lacks the "(Alfonzo)" suffixes of `baby_mobs`, `brewinandchewin`, `creative_labels`, `creativeengineering`, `exposure` and adds `exposure_catalog`, `exposure_expanded` | the toml list is older than the Java default; decide which wins before the move |

Every other section matches its defaults: 46 sections checked fully by script; `crafting` (felsic defaults equal the
toml values 250/500 mB, loops 1, blows 3/5/8), `surface.speed` (26 tiers equal `SurfaceTier`), `crop_drops.soil_yield.crops`
(all false, "all off by default"), `forgiving_void.*.enabled`, `named_mobs` lists, `perf` lists, `trash_slot` lists
(6 defaults, 6 in file), `wthit` lists and `state_lines.*` (all true), `worldgen` `log`/`overrides`/`untouched`/`removeSpawners`
checked by hand and equal. So edited values exist only in `shields` (Equipment), `damage_tags` (Equipment) and
`mod_names` (Common).

## 5. What stays in `creativeengineering-common.toml`

The file name belongs to mod id `creativeengineering`, which the plan gives to the Engineering jar (Q1 decides whether
Common or Engineering keeps the id). Two readings:

- If Common keeps the file: `armor_overflow`, `gui_title`, `toasts`, `changelog`, `advancements`, `mod_names`, the
  Common halves of `wthit` (19 keys plus `state_lines`), `jei` (4 keys) and `perf` (2 gravity keys). 9 sections,
  about 50 keys of 394.
- If Engineering keeps the file: `shrapnel`, `surface`, `vein_undo`, `wthit.sheetmetalTank`. 3 sections, 46 keys;
  everything above moves to `creativecommon-common.toml`.

Either way the file loses more than 85 percent of its keys. Players' edited values (section 4) all leave the file
(`shields`, `damage_tags` to Equipment; `mod_names` to Common's file in the second reading).

## 6. Observations for the execution plans

- `NCH.cfg` is the single choke point for script-side config: six scripts and NCH itself depend on the hard-coded
  class name. Give NCH a per-jar registration (`global.NCH.configClass('CRAFTING', <class>)` or the jar's
  `global.<X>.config`) before the first carve.
- `COMMON_SPEC.isLoaded()` guards in nine files become per-jar spec guards.
- `perf` is the only section whose target differs from its readers (MOVE); the other two SPLIT sections (`wthit`,
  `jei`) keep most keys in Common.
- Sections already moved out of this file: `[kitchen]` to Blockheads' `BAConfig` (2026-10-31), the CB sections per
  creative_brewing.md 4.2 (`magic_resistance`, `copper_poisoning`, `alcohol`, `effects`, `coaster` are still here,
  so phase 1 of that plan has not run).
- `?` rows for the owner: `botania` (Compat or the Magic reservation), `monster_box` (Worldgen `mobs/` or Compat
  Quark), `bottled_air` (Equipment or Engineering `item/`), `moss`, `vine_shearing`, `dandelion_maturing`
  (Worldgen plants or CD `crops/`), `wandering_trader` (Worldgen fauna; `trader/TradeListings` stays Common).
