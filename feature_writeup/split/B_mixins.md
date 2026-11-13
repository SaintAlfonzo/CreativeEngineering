# Sweep B: mixins

Audit run 2026-11-01, read-only. Source: `development/patcherMod/src/main/resources/creativeengineering.mixins.json` (212 `mixins`, 77 `client`, 0 `server`; 289 total) and the classes in `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/` (flat, no sub-packages). Target words follow [README.md](README.md).

## Gating

- The json names `com.SaintAlfonzo.creativeengineering.perf.CEMixinPlugin` as plugin. Its `shouldApplyMixin` returns true for every mixin: **patcherMod has no mod-presence or config gate at the plugin level.** Its `postApply` only rewrites `IwServerLevelTickMixin` (Immersive Weathering `callTick` handler redirected to `perf.IwGrowthFast`), skipped by `-Dcreativeengineering.iwFastTick=false`.
- The shared `ce.shared.GatedMixinPlugin` (`development/shared/mixinplugin/`, used by CD, CB, moreMooblooms, incubationAdditions) gates by `needsMod` map (mixin simple name to mod ids) and by compartment package (`mixin.<modid>.` applied only when that mod is loaded, with an optional version pin). patcherMod does not use it; each new jar's mixins json gets its own subclass (plan rule 3.6), and the `gate` column below says what each mixin would need.
- Gates that do exist today: `@Pseudo` on 19 mixins (soft target, mixin skipped if the class is missing); all other mod-targeting mixins are hard targets (the mod jar must be present, `required: true`); `ArmorCapMixin` and `ServerAdvancementManagerMixin` check `CEConfig.COMMON_SPEC.isLoaded()` in code; `IwServerLevelTickMixin` has the system property above.
- Gate column words: `none` (vanilla target), `hard <modid>` (mod class target, no @Pseudo), `@Pseudo <modid>`, plus `config` where a config value switches the behaviour. Mod ids are inferred from the target package.

Columns: mixin class | side | target mod | gate | target jar | reason | config keys (`CEConfig.X.y`) | helper classes (non-mixin `creativeengineering.*`).

## Engineering (31: 27 common, 4 client)

| mixin class | side | target mod | gate | target jar | reason | config keys | helper classes |
|---|---|---|---|---|---|---|---|
| `IETagUtilsMixin` | common | immersiveengineering | hard immersiveengineering | Engineering | IE fix (TagUtils prefix bug), IE machine/fluid row | - | - |
| `IEConcreteSetTimeMixin` | common | immersiveengineering | hard immersiveengineering | Engineering | IE liquid concrete set time | - | - |
| `ImmersivePostsRegistrationMixin` | common | immersiveposts | hard immersiveposts | Engineering | Immersive Posts materials for pack metals (metal rows) | - | `compat.ImmersivePostsMaterials` |
| `ImmersivePostsValidItemMixin` | common | immersiveposts | hard immersiveposts | Engineering | Immersive Posts materials for pack metals (metal rows) | - | `compat.ImmersivePostsMaterials` |
| `TagLoaderImmersivePostsMixin` | common | minecraft | none | Engineering | Immersive Posts materials for pack metals (metal rows) | - | `compat.ImmersivePostsMaterials` |
| `MemoryStickItemMixin` | common | pneumaticcraft | hard pneumaticcraft | Engineering | fluid/ FluidBridge: XP fluid conversion (PneumaticCraft, Sophisticated) | - | `fluid.FluidBridge` |
| `PneumaticCraftUtilsMixin` | common | pneumaticcraft | hard pneumaticcraft | Engineering | fluid/ FluidBridge: XP fluid conversion (PneumaticCraft, Sophisticated) | - | `fluid.FluidBridge` |
| `XpHelperMixin` | common | sophisticatedcore | hard sophisticatedcore | Engineering | fluid/ FluidBridge: XP fluid conversion (PneumaticCraft, Sophisticated) | - | - |
| `XpPumpUpgradeWrapperMixin` | common | sophisticatedcore | hard sophisticatedcore | Engineering | fluid/ FluidBridge: XP fluid conversion (PneumaticCraft, Sophisticated) | - | `fluid.FluidBridge` |
| `XPFluidManagerMixin` | common | pneumaticcraft | hard pneumaticcraft | Engineering | fluid/ FluidBridge: XP fluid conversion (PneumaticCraft, Sophisticated) | - | `fluid.FluidBridge` |
| `SawmillRecipeMixin` | common | immersiveengineering | hard immersiveengineering | Engineering | IE sawmill (sawblade routing, recipe ext) | - | `sawmill.duck.SawmillRecipeExt` |
| `SawmillRecipeSerializerMixin` | common | immersiveengineering | hard immersiveengineering | Engineering | IE sawmill (sawblade routing, recipe ext) | - | `sawmill.duck.SawmillRecipeExt` |
| `SawmillProcessMixin` | common | immersiveengineering | hard immersiveengineering | Engineering | IE sawmill (sawblade routing, recipe ext) | - | `sawmill.SawbladeRouting`, `sawmill.duck.SawmillProcessExt` |
| `ProcessingRecipeMixin` | common | create | hard create | Engineering | Create processing recipe exclusive results (fan/) | - | `fan.ExclusiveResults`, `fan.duck.ProcessingRecipeExt` |
| `ProcessingRecipeSerializerMixin` | common | create | hard create | Engineering | Create processing recipe exclusive results (fan/) | - | `fan.ExclusiveResults`, `fan.duck.ProcessingRecipeExt` |
| `SequencedAssemblyRecipeMixin` | common | create | hard create | Engineering ? | Create sequenced assembly NBT passthrough; a Create recipe fix but it exists only for the Equipment gear progression (echo/felsic/dragon assembly) | - | - |
| `ExcavationDrillItemMixin` | common | create_dd | hard create_dd | Engineering | Dreams and Desires tool item mixin (not potion, not JEI) | - | - |
| `DeforesterSawItemMixin` | common | create_dd | hard create_dd | Engineering | Dreams and Desires tool item mixin (not potion, not JEI) | - | - |
| `OreExcavationBlockHistoryMixin` | common | oreexcavation | hard oreexcavation | Engineering | excavation/ vein undo (OreExcavation) | - | `excavation.VeinUndoAccess` |
| `OreExcavationCommandUndoMixin` | common | oreexcavation | hard oreexcavation | Engineering | excavation/ vein undo (OreExcavation) | - | `excavation.VeinUndo` |
| `OreExcavationHistoryMixin` | common | oreexcavation | hard oreexcavation | Engineering | excavation/ vein undo (OreExcavation) | - | `excavation.VeinUndoAccess` |
| `OreExcavationMiningAgentMixin` | common | oreexcavation | hard oreexcavation | Engineering | excavation/ vein undo (OreExcavation) | - | `excavation.VeinUndoAccess` |
| `OreExcavationSchedulerMixin` | common | oreexcavation | hard oreexcavation | Engineering | excavation/ vein undo (OreExcavation) | - | `excavation.VeinUndoAccess` |
| `BlockRunnerBlockStateMixin` | common | minecraft | none | Engineering | surface/ BlockRunner, camo and sinking | - | `surface.CamoSpeed`, `surface.StickyFacing` |
| `BlockRunnerSpeedConfigMixin` | common | blockrunner | hard blockrunner | Engineering | surface/ BlockRunner, camo and sinking | - | `surface.SurfaceSpeeds` |
| `FramedDoubleCamoOrderMixin` | common | framedblocks | hard framedblocks | Engineering | surface/ BlockRunner, camo and sinking | - | `surface.LastInsertedCamo` |
| `CopycatMaterialOrderMixin` | common | copycats | hard copycats | Engineering | surface/ BlockRunner, camo and sinking | - | `surface.LastInsertedMaterial` |
| `SkullBlockEntityMixin` | client | minecraft | none | Engineering | fan/ dragon head catalyst animation | - | `fan.CatalystUtils` |
| `JetpackItemTooltipMixin` | client | create_dd | hard create_dd | Engineering ? | Dreams and Desires jetpack tooltip null-level crash fix; Common if read as an engine fix | - | - |
| `EntitySinkRenderMixin` | client | minecraft | none | Engineering | surface/ BlockRunner, camo and sinking | - | `surface.SinkingRender` |
| `CameraSinkMixin` | client | minecraft | none | Engineering | surface/ BlockRunner, camo and sinking | - | `surface.SinkingRender` |

## Equipment (20: 20 common, 0 client)

| mixin class | side | target mod | gate | target jar | reason | config keys | helper classes |
|---|---|---|---|---|---|---|---|
| `EntityStepstoolCollisionMixin` | common | minecraft | none | Equipment | step stool | - | `stepstool.Stepstool` |
| `ServerGamePacketListenerStepstoolMixin` | common | minecraft | none | Equipment | step stool | - | `stepstool.Stepstool` |
| `CopperPickaxeItemMixin` | common | everythingcopper | hard everythingcopper | Equipment | tool tier (CETiers) for Everything Copper pickaxe | - | `CETiers` |
| `BackpackSkinItemRendererMixin` | common | sophisticatedbackpacks | hard sophisticatedbackpacks | Equipment | backpack skins | - | `backpackskin.BackpackSkins` |
| `BackpackSkinBakedModelMixin` | common | sophisticatedbackpacks | hard sophisticatedbackpacks | Equipment | backpack skins | - | `backpackskin.BackpackSkinsClient` |
| `BackpackSkinBlockEntityMixin` | common | sophisticatedbackpacks | hard sophisticatedbackpacks | Equipment | backpack skins | - | `backpackskin.BackpackSkins`, `backpackskin.BackpackSkinsClient` |
| `BackpackSkinBlockShapeMixin` | common | sophisticatedbackpacks | hard sophisticatedbackpacks | Equipment | backpack skins | - | `backpackskin.BackpackSkins` |
| `BackpackSkinWornMixin` | common | sophisticatedbackpacks | hard sophisticatedbackpacks | Equipment | backpack skins | - | `backpackskin.BackpackSkins`, `backpackskin.BackpackSkinsClient` |
| `BackpackSkinEquipMixin` | common | sophisticatedbackpacks | hard sophisticatedbackpacks; config | Equipment | backpack skins | `BACKPACK_SKINS.allowEquip` | `backpackskin.BackpackSkins` |
| `EasyElytraTakeoffHelperMixin` | common | easyelytratakeoff | hard easyelytratakeoff | Equipment | elytra chestplates (Easy Elytra Takeoff) | - | `util.ElytraChestplates` |
| `EasyElytraTakeoffGroundCheckMixin` | common | easyelytratakeoff | hard easyelytratakeoff | Equipment | elytra chestplates (Easy Elytra Takeoff) | - | `util.ElytraChestplates` |
| `ArmorMaterialEnchantabilityMixin` | common | minecraft | none; config | Equipment | armor enchantability | `ARMOR_ENCHANT` | `util.ArmorEnchantabilityBridge` |
| `LivingEntityShieldMixin` | common | minecraft | none | Equipment | shields | - | `shield.ShieldBlocking` |
| `VanillaShieldDurabilityMixin` | common | minecraft | none | Equipment | shields | - | `shield.ShieldConfig`, `shield.ShieldTier` |
| `VanillaShieldRepairMixin` | common | minecraft | none | Equipment | shields | - | `shield.ShieldConfig`, `shield.ShieldTier` |
| `ShieldDisableCooldownMixin` | common | minecraft | none | Equipment | shields | - | `shield.ShieldConfig` |
| `MobShieldDisableMixin` | common | minecraft | none | Equipment | shields | - | `shield.ShieldConfig` |
| `ShieldDecorationRecipeMixin` | common | minecraft | none | Equipment | shields | - | `shield.TierShieldItem` |
| `BonemealCharmMixin` | common | minecraft | none; config | Equipment ? | bonemeal charm held item; generalised Vinery Winemaker trick, could be Magic | `BONEMEAL_CHARM.chance`, `BONEMEAL_CHARM.damageChance`, `BONEMEAL_CHARM.damagePerUse`, `BONEMEAL_CHARM.enabled` | `util.BonemealCharms` |
| `ArmorCapMixin` | common | minecraft | none; COMMON_SPEC.isLoaded in code; config | Equipment | dragon gear armor cap | `DRAGON_GEAR.armorCap` | - |

## Magic (Equipment) (3: 3 common, 0 client)

| mixin class | side | target mod | gate | target jar | reason | config keys | helper classes |
|---|---|---|---|---|---|---|---|
| `CycleEnchantmentMixin` | common | nock_enough_arrows | hard nock_enough_arrows | Magic (Equipment) | enchantment mixin (Nock Enough Arrows enchantments) | - | - |
| `StockpileEnchantmentMixin` | common | nock_enough_arrows | hard nock_enough_arrows | Magic (Equipment) | enchantment mixin (Nock Enough Arrows enchantments) | - | - |
| `RecoveryEnchantmentMixin` | common | nock_enough_arrows | hard nock_enough_arrows | Magic (Equipment) | enchantment mixin (Nock Enough Arrows enchantments) | - | - |

## Worldgen (56: 56 common, 0 client)

| mixin class | side | target mod | gate | target jar | reason | config keys | helper classes |
|---|---|---|---|---|---|---|---|
| `LCItemsForMapTradeMixin` | common | lightmanscurrency | hard lightmanscurrency | Worldgen | locate: async map trades (map-trade locate hang) | - | `compat.AsyncMapTrades` |
| `CSSellingMapMixin` | common | cloudstorage | hard cloudstorage | Worldgen | locate: async map trades (map-trade locate hang) | - | `compat.AsyncMapTrades` |
| `ForgivingVoidMixin` | common | forgivingvoid | hard forgivingvoid | Worldgen | Forgiving Void origin pull (forgivingvoid/) | - | `forgivingvoid.ForgivingVoidBridge` |
| `StructureTemplateLootSwapMixin` | common | minecraft | none | Worldgen | structure template processing (loot swap, ground match) | - | `worldgen.LootContainerSwaps` |
| `StructureTemplateGroundMatchMixin` | common | minecraft | none | Worldgen | structure template processing (loot swap, ground match) | - | `worldgen.GroundMatch`, `worldgen.TemplateTrim` |
| `StructureStartGroundMatchMixin` | common | minecraft | none | Worldgen | structure template processing (loot swap, ground match) | - | `worldgen.GroundMatch` |
| `ChunkGeneratorLocateMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.DeadPlacementFilter` |
| `ChunkGeneratorStructureFrequencyMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.DimensionFilter`, `worldgen.LocateStats`, `worldgen.MetaSlots`, `worldgen.StructureFrequencies` |
| `StructureCheckFrequencyMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.LocatePrediction` |
| `StructureCheckMetaMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.MetaSlots` |
| `ChunkGeneratorMetaLocateMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.LocateLoads`, `worldgen.LocateStats`, `worldgen.MetaSlots` |
| `ChunkGeneratorStructureBiomeRulesMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.LocateStats`, `worldgen.MetaSlots`, `worldgen.StructureBiomeRules` |
| `StructureBiomesMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.StructureBiomeFits` |
| `ChunkGeneratorEarthFillMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.EarthFill`, `worldgen.LocateStats`, `worldgen.PlacementPriority` |
| `LevelChunkFirstTickWaterMixin` | common | minecraft | none | Worldgen | earth and water fill (FirstTickWater, GeneratedFallingBlocks) | - | `worldgen.FirstTickWater`, `worldgen.GeneratedFallingBlocks` |
| `FlowingFluidSettleMixin` | common | minecraft | none | Worldgen | earth and water fill (FirstTickWater, GeneratedFallingBlocks) | - | `worldgen.FirstTickWater` |
| `BlockPopResourceSettleMixin` | common | minecraft | none | Worldgen | earth and water fill (FirstTickWater, GeneratedFallingBlocks) | - | `worldgen.FirstTickWater` |
| `FallingBlockHoldMixin` | common | minecraft | none | Worldgen | earth and water fill (FirstTickWater, GeneratedFallingBlocks) | - | `worldgen.GeneratedFallingBlocks` |
| `ProtoChunkTicksFallingMixin` | common | minecraft | none | Worldgen | earth and water fill (FirstTickWater, GeneratedFallingBlocks) | - | `worldgen.GeneratedFallingBlocks` |
| `ChunkGeneratorBeeNestsMixin` | common | minecraft | none | Worldgen | bee nests | - | `worldgen.BeeNests` |
| `TreeFeatureBeeNestsMixin` | common | minecraft | none | Worldgen | bee nests | - | `worldgen.BeeNests` |
| `BeehiveDecoratorBeeNestsMixin` | common | minecraft | none | Worldgen | bee nests | - | `worldgen.BeeNests` |
| `GeodeFeatureOceanDepthMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.OceanGeodes` |
| `ChunkGeneratorPlacementPriorityMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.PlacementPriority` |
| `ChunkGeneratorRingCacheMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.RingPositionCache` |
| `JigsawPlacementPriorityMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.PlacementPriority` |
| `StructureTemplateManagerSwapMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.TemplateBlockSwaps`, `worldgen.TemplateSpawnerFilter` |
| `ChunkGeneratorSpawnBoxesMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.StructureSpawnBoxes` |
| `StructureTerrainAdaptationMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.TerrainAdaptation` |
| `BeardifierSoftMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.TerrainAdaptation` |
| `BushierFlowersDisableMixin` | common | bushierflowers | hard bushierflowers | Worldgen | wild flowers: Bushier Flowers disabled | - | - |
| `QuarkPermafrostStyleMixin` | common | quark | hard quark | Worldgen | Quark permafrost underground style (worldgen config) | - | `worldgen.WorldgenConfig` |
| `MonsterBoxBlockEntityMixin` | common | quark | hard quark | Worldgen | mobs/ MonsterBoxVault (structure monster boxes) | - | `mobs.MonsterBoxVault` |
| `CreateBreakerMonsterBoxMixin` | common | create | hard create | Worldgen | mobs/ MonsterBoxVault (structure monster boxes) | - | `mobs.MonsterBoxVault` |
| `BubbleColumnImmunityMixin` | common | minecraft | none | Worldgen ? | bubble column immunity by entity tag for big ocean mobs (fauna); the tag lives in server_scripts/tags | - | - |
| `SpawnPlacementsConditionsMixin` | common | minecraft | none | Worldgen | mob spawn conditions and spawn ground (fauna) | - | `worldgen.SpawnConditions` |
| `AnimalWalkTargetMixin` | common | minecraft | none | Worldgen | mob spawn conditions and spawn ground (fauna) | - | - |
| `MushroomCowWalkTargetMixin` | common | minecraft | none | Worldgen ? | mooshroom walk target = spawn ground; plan 2.2 leaves mooshroom/ to the audit (Worldgen fauna or CD with More Mooblooms) | - | - |
| `McaVillagerPathGatesMixin` | common | mca | hard mca | Worldgen | MCA villager pathing (fauna) | - | - |
| `WalkNodeEvaluatorVillagerGateMixin` | common | minecraft | none | Worldgen | MCA villager pathing (fauna) | - | - |
| `McaVillagerPathLaddersMixin` | common | mca | hard mca | Worldgen | MCA villager pathing (fauna) | - | - |
| `GroundPathNavigationClimbMixin` | common | minecraft | none | Worldgen | MCA villager pathing (fauna) | - | - |
| `MoveControlVillagerLadderMixin` | common | minecraft | none | Worldgen | MCA villager pathing (fauna) | - | - |
| `StructureCheckThreadSafeMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.WorldgenConfig` |
| `NoiseBasedChunkGeneratorDistantClipMixin` | common | minecraft | none | Worldgen | DistantClip / sky islands | - | `worldgen.DistantClip` |
| `NoiseChunkDistantClipMixin` | common | minecraft | none | Worldgen | DistantClip / sky islands | - | `worldgen.DistantClip` |
| `ConfiguredFeatureDistantClipMixin` | common | minecraft | none | Worldgen | DistantClip / sky islands | - | `worldgen.DistantClip` |
| `ConfiguredFeatureSkyIslandsMixin` | common | minecraft | none | Worldgen | DistantClip / sky islands | - | `worldgen.SkyIslands` |
| `ChunkGeneratorSeabedClearanceMixin` | common | minecraft | none | Worldgen | chunk generator / structure / locate mixin | - | `worldgen.LocateStats`, `worldgen.SeabedClearance` |
| `RegistryLoaderPresetOverworldMixin` | common | minecraft | none | Worldgen | flat and preset worlds keep their overworld | - | - |
| `LeavesDecayRangeMixin` | common | minecraft | none | Worldgen | leaf decay range | - | `block.LeafDecayRange` |
| `RaidGlowingMixin` | common | minecraft | none; config | Worldgen | raid glowing (fauna) | `RAIDS.glowingAmplifier`, `RAIDS.glowingRaiders` | - |
| `MobBeeIdleMixin` | common | minecraft | none | Worldgen | bee idle (owner: fauna) | - | - |
| `MobCapLevelTickMixin` | common | minecraft | none | Worldgen | mob caps and spawn areas (plan 2.1 Worldgen) | - | `perf.MobCapLevels` |
| `MobCapMixin` | common | minecraft | none | Worldgen | mob caps and spawn areas (plan 2.1 Worldgen) | - | `perf.MobCapLevels` |
| `MobCapSpawnAreaMixin` | common | minecraft | none | Worldgen | mob caps and spawn areas (plan 2.1 Worldgen) | - | `perf.MobCapLevels` |

## Compat (41: 31 common, 10 client)

| mixin class | side | target mod | gate | target jar | reason | config keys | helper classes |
|---|---|---|---|---|---|---|---|
| `HandcraftedItemHoldingSaveMixin` | common | handcrafted | @Pseudo handcrafted | Compat | Handcrafted nightstand storage tweak | - | `compat.NightstandStorage` |
| `HandcraftedNightstandStorageMixin` | common | handcrafted | @Pseudo handcrafted | Compat | Handcrafted nightstand storage tweak | - | `compat.NightstandStorage`, `compat.NightstandStorage.NightstandContainer` |
| `HandcraftedNightstandUseMixin` | common | handcrafted | @Pseudo handcrafted | Compat | Handcrafted nightstand storage tweak | - | `compat.NightstandStorage` |
| `SupplementariesBreezyCompatMixin` | common | supplementaries | hard supplementaries | Compat | Supplementaries fix (Breezy wind API) | - | - |
| `SupplementariesDyeBlocksMixin` | common | supplementaries | hard supplementaries | Compat | Supplementaries dye-blocks block-entity fix | - | - |
| `ItemDisplayTileAccessor` | common | moonlight | hard moonlight | Compat | Supplementaries jar mixing (JarMixing); tooltip shows only this feature | - | - |
| `SupplementariesJarTileMixin` | common | supplementaries | hard supplementaries | Compat | Supplementaries jar mixing (JarMixing); tooltip shows only this feature | - | `compat.JarMixing` |
| `SupplementariesJarItemTooltipMixin` | common | supplementaries | hard supplementaries | Compat | Supplementaries jar mixing (JarMixing); tooltip shows only this feature | - | `compat.JarMixing` |
| `SupplementariesSackItemMixin` | common | supplementaries | hard supplementaries | Compat | Supplementaries sack bundle (storage tweak) | - | `compat.SackBundle` |
| `SophisticatedCraftingPolymorphMixin` | common | sophisticatedcore | hard sophisticatedcore | Compat | Polymorph compat for Sophisticated / IE table / Pretty Pipes terminal | - | `compat.SophisticatedPolymorphCompat` |
| `SophisticatedUpgradeOpenMixin` | common | sophisticatedcore | hard sophisticatedcore | Compat | Polymorph compat for Sophisticated / IE table / Pretty Pipes terminal | - | `compat.SophisticatedPolymorphCompat` |
| `PolymorphRecipeSelectionPacketMixin` | common | polymorph | hard polymorph | Compat | Polymorph compat for Sophisticated / IE table / Pretty Pipes terminal | - | `compat.SophisticatedPolymorphCompat` |
| `IECraftingTablePolymorphMixin` | common | immersiveengineering | hard immersiveengineering | Compat | Polymorph compat for Sophisticated / IE table / Pretty Pipes terminal | - | `compat.CraftingMenuPolymorphCompat` |
| `PrettyPipesCraftingTerminalPolymorphMixin` | common | prettypipes | hard prettypipes | Compat | Polymorph compat for Sophisticated / IE table / Pretty Pipes terminal | - | `compat.CraftingMenuPolymorphCompat` |
| `ItemPhysicServerMixin` | common | itemphysic | hard itemphysic | Compat | ItemPhysic fire/lava split, burn transform, Allurement curses (itemphysic/) | - | `itemphysic.AllurementItemCurses`, `itemphysic.ItemBurnTransform`, `itemphysic.ItemPhysicSplit` |
| `BlockItemOwnStackFillMixin` | common | minecraft | none | Compat | Berry Good: berries back in creative tabs and JEI | - | - |
| `StructureTemplateBotanistPotMixin` | common | minecraft | none | Compat | plant pots and planters compat (BotanistPot, PlantBoxModels) | - | `compat.BotanistPot` |
| `BotanistWorkbenchPotMixin` | common | beautify | @Pseudo beautify | Compat | plant pots and planters compat (BotanistPot, PlantBoxModels) | - | `compat.BotanistPot`, `compat.BotanistPotBlockEntity` |
| `SuppFlowerBoxPlantsMixin` | common | supplementaries | hard supplementaries | Compat | plant pots and planters compat (BotanistPot, PlantBoxModels) | - | `compat.PlantBoxModels` |
| `FramedFlowerPotUnframedMixin` | common | framedblocks | hard framedblocks | Compat | plant pots and planters compat (BotanistPot, PlantBoxModels) | - | - |
| `SiltPotSoilMixin` | common | twigs | @Pseudo twigs | Compat | plant pots and planters compat (BotanistPot, PlantBoxModels) | - | - |
| `QuarkSortingHandlerMixin` | common | quark | hard quark | Compat | Quark sort item-loss fix | - | `compat.QuarkSafeSort` |
| `VineBlockShearMixin` | common | minecraft | none | Compat ? | sheared modded vines (block/VineShearing); a cross-mod block tweak, Sweep A decides block/ | - | `block.VineShearing` |
| `SaltBlockRandomTickMixin` | common | minecraft | none | Compat ? | Vintage salt family random ticks and Salt mod dissolving (block/SaltBlockTicks); third-party block tweak | - | `block.SaltBlockTicks` |
| `SaltLayerRandomTickMixin` | common | minecraft | none | Compat ? | Vintage salt family random ticks and Salt mod dissolving (block/SaltBlockTicks); third-party block tweak | - | `block.SaltBlockTicks` |
| `SaltRockNoDissolveMixin` (removed 2026-11-02: direct code in the Salt member's `Dissolving`, [salt.md](../salt.md)) | common | salt | hard salt | Compat ? | Vintage salt family random ticks and Salt mod dissolving (block/SaltBlockTicks); third-party block tweak | - | `block.SaltBlockTicks` |
| `LeafPileSlowMixin` | common | immersive_weathering | hard immersive_weathering; config | Compat | Immersive Weathering leaf pile slow | `LEAF_PILES.slowFromLayers` | - |
| `TrinketCaseInventoryMixin` | common | botania | hard botania | Compat | Botania trinket case rules (storage tweak) | - | `compat.TrinketCaseRules` |
| `TrinketCaseSlotMixin` | common | botania | hard botania | Compat | Botania trinket case rules (storage tweak) | - | `compat.TrinketCaseRules` |
| `TrinketCaseMenuMixin` | common | botania | hard botania | Compat | Botania trinket case rules (storage tweak) | - | `compat.TrinketCaseRules` |
| `ItemBackedInventoryAccessor` | common | botania | hard botania | Compat | Botania trinket case rules (storage tweak) | - | - |
| `AnotherFurniturePlanterRenderMixin` | client | another_furniture | @Pseudo another_furniture | Compat | plant pots and planters compat (BotanistPot, PlantBoxModels) | - | `compat.PlantBoxModelsClient` |
| `McaCombEyeColourMixin` | client | mca | hard mca | Compat | MCA comb eye colour | - | - |
| `SupplementariesJarRendererMixin` | client | supplementaries | hard supplementaries | Compat | Supplementaries jar mixing (JarMixing); tooltip shows only this feature | - | - |
| `SupplementariesJarItemRendererMixin` | client | supplementaries | hard supplementaries | Compat | Supplementaries jar mixing (JarMixing); tooltip shows only this feature | - | - |
| `BedspreadsBannerPatternMixin` | client | bedspreads | hard bedspreads | Compat | banner pattern atlas compat fix | - | `compat.BannerPatternAtlasCompat` |
| `CustomizableElytraBannerPatternMixin` | client | customizableelytra | hard customizableelytra | Compat | banner pattern atlas compat fix | - | `compat.BannerPatternAtlasCompat` |
| `TrashSlotGuiHandlerMixin` | client | trashslot | hard trashslot | Compat | TrashSlot pack defaults | - | `compat.TrashSlotCompat` |
| `TrashSlotComponentMixin` | client | trashslot | hard trashslot | Compat | TrashSlot pack defaults | - | - |
| `TrashSlotSaveStateAccessor` | client | trashslot | hard trashslot | Compat | TrashSlot pack defaults | - | - |
| `DhCloudHeightMixin` | client | distanthorizons | @Pseudo distanthorizons | Compat | Distant Horizons cloud height follows vanilla | - | - |

## Calendar (18: 10 common, 8 client)

| mixin class | side | target mod | gate | target jar | reason | config keys | helper classes |
|---|---|---|---|---|---|---|---|
| `CropSeasonGrowHookMixin` | common | immersiveengineering, thedirtystuff, nethersdelight | @Pseudo immersiveengineering, thedirtystuff, nethersdelight | Calendar | crop seasons grow hook (seasons/CropSeasons) | - | - |
| `SereneSeasonsMeltThrottleMixin` | common | sereneseasons | hard sereneseasons; config | Calendar | Serene Seasons / Better Days / time pause (time/, seasons/) | `PERF.seasonsMeltIntervalTicks` | - |
| `SereneSeasonsClockMixin` | common | sereneseasons | hard sereneseasons | Calendar | Serene Seasons / Better Days / time pause (time/, seasons/) | - | `time.SeasonClock` |
| `SereneSeasonsTimeSkipMixin` | common | sereneseasons | hard sereneseasons | Calendar | Serene Seasons / Better Days / time pause (time/, seasons/) | - | `time.SeasonClock` |
| `BetterDaysConfigMixin` | common | betterdays | @Pseudo betterdays | Calendar | Serene Seasons / Better Days / time pause (time/, seasons/) | - | `time.TimeSettings` |
| `BetterDaysTimePauseMixin` | common | betterdays | @Pseudo betterdays | Calendar | Serene Seasons / Better Days / time pause (time/, seasons/) | - | `time.TimePause` |
| `ServerLevelTimePauseMixin` | common | minecraft | none | Calendar | Serene Seasons / Better Days / time pause (time/, seasons/) | - | `time.TimePause` |
| `SeasonBiomeConfigMixin` | common | sereneseasons | hard sereneseasons | Calendar | Serene Seasons / Better Days / time pause (time/, seasons/) | - | `seasons.SeasonTypes` |
| `SeasonHooksTypesMixin` | common | sereneseasons | hard sereneseasons | Calendar | Serene Seasons / Better Days / time pause (time/, seasons/) | - | `seasons.SeasonTypes` |
| `SeasonFertilityMixin` | common | sereneseasons | hard sereneseasons | Calendar | Serene Seasons / Better Days / time pause (time/, seasons/) | - | `seasons.SeasonTypes` |
| `SereneSeasonsClientClockMixin` | client | sereneseasons | hard sereneseasons | Calendar | Serene Seasons / Better Days / time pause (time/, seasons/) | - | `time.TimeSettings` |
| `XaeroTimeInfoMixin` | client | xaerominimap | @Pseudo xaerominimap | Calendar | SeasonHUD / Xaero time / weather by biome | - | `time.TimeSettings` |
| `SeasonHooksClientMixin` | client | sereneseasons | hard sereneseasons | Calendar | Serene Seasons / Better Days / time pause (time/, seasons/) | - | `seasons.SeasonTypes` |
| `SeasonRainColumnMixin` | client | minecraft | none | Calendar | Serene Seasons / Better Days / time pause (time/, seasons/) | - | `seasons.SeasonTypes` |
| `SeasonColorUtilMixin` | client | sereneseasons | hard sereneseasons | Calendar | Serene Seasons / Better Days / time pause (time/, seasons/) | - | `seasons.SeasonTypes` |
| `SeasonHudHelperMixin` | client | seasonhud | hard seasonhud | Calendar | SeasonHUD / Xaero time / weather by biome | - | `seasons.SeasonTypes` |
| `SeasonHudCurrentSeasonMixin` | client | seasonhud | hard seasonhud; config | Calendar | SeasonHUD / Xaero time / weather by biome | `TIME.hudTimeOfDay` | `seasons.SeasonTypes`, `time.TimeSettings` |
| `PrettyRainSeasonMixin` | client | particlerain | @Pseudo particlerain | Calendar | SeasonHUD / Xaero time / weather by biome | - | `seasons.SeasonTypes` |

## CD (24: 22 common, 2 client)

| mixin class | side | target mod | gate | target jar | reason | config keys | helper classes |
|---|---|---|---|---|---|---|---|
| `ServerPlayerGameModePlacedFoodMixin` | common | minecraft | none | CD | placed food (Spice of Life) hook | - | `food.PlacedFoodSol` |
| `AlexsMobsSeagullStealMixin` | common | alexsmobs | hard alexsmobs | CD | lunchbox protection: seagulls never steal Spice of Life containers | - | - |
| `AlexsMobsSeagullItemMixin` | common | alexsmobs | hard alexsmobs | CD | lunchbox protection: seagulls never steal Spice of Life containers | - | - |
| `CampfireRainMixin` | common | minecraft | none | CD | campfire rain and comfort (campfire/) | - | `campfire.CampfireComfort`, `campfire.CampfireRain`, `campfire.CampfireRainTicks` |
| `MobCobPipeSlotMixin` | common | minecraft | none | CD | Corn Delight cob pipe head slot (food mod tree) | - | `compat.CornDelightCompat` |
| `ItemStackCanServingsMixin` | common | minecraft | none | CD | can servings (food/) | - | `food.CanServings` |
| `TobaccoPlacementMixin` | common | thedirtystuff | hard thedirtystuff | CD | crops/ (CropParity, StemExtraFruit, DandelionMaturing, HoeHarvestArea) | - | `crops.CropParity` |
| `FarmBlockUnderCropsMixin` | common | minecraft | none | CD | crops/ (CropParity, StemExtraFruit, DandelionMaturing, HoeHarvestArea) | - | `crops.CropParity` |
| `AttachedStemExtraFruitMixin` | common | minecraft | none | CD | crops/ (CropParity, StemExtraFruit, DandelionMaturing, HoeHarvestArea) | - | `crops.StemExtraFruit` |
| `MulchBlockCropGrowthMixin` | common | immersive_weathering | hard immersive_weathering | CD | crops/ (CropParity, StemExtraFruit, DandelionMaturing, HoeHarvestArea) | - | `crops.CropParity` |
| `DandelionFertileSoilMixin` | common | immersive_weathering | hard immersive_weathering | CD | crops/ (CropParity, StemExtraFruit, DandelionMaturing, HoeHarvestArea) | - | `crops.DandelionMaturing` |
| `DandelionRichFarmlandMixin` | common | farmersdelight | hard farmersdelight | CD | crops/ (CropParity, StemExtraFruit, DandelionMaturing, HoeHarvestArea) | - | `crops.DandelionMaturing` |
| `DandelionRichSoilMixin` | common | farmersdelight | hard farmersdelight | CD | crops/ (CropParity, StemExtraFruit, DandelionMaturing, HoeHarvestArea) | - | `crops.DandelionMaturing` |
| `QuarkHoeHarvestMixin` | common | quark | hard quark | CD | crops/ (CropParity, StemExtraFruit, DandelionMaturing, HoeHarvestArea) | - | `crops.CropParity` |
| `QuarkHoeAreaBreakMixin` | common | quark | hard quark | CD | crops/ (CropParity, StemExtraFruit, DandelionMaturing, HoeHarvestArea) | - | `crops.HoeHarvestArea` |
| `QuarkHoeAreaHarvestMixin` | common | quark | hard quark | CD | crops/ (CropParity, StemExtraFruit, DandelionMaturing, HoeHarvestArea) | - | `crops.HoeHarvestArea` |
| `QuarkOwnPickMixin` | common | quark | hard quark | CD | crops/ (CropParity, StemExtraFruit, DandelionMaturing, HoeHarvestArea) | - | `crops.CropParity` |
| `CannedGolemColorMixin` (removed 2026-11-02: direct code in the Canned Goods member, [cans.md](../cans.md) 9) | common | cannedgoods | hard cannedgoods | CD | canned golem dye (cans) | - | `compat.DyeableCannedGolem` |
| `TemptGoalTroughRetryMixin` | common | minecraft | none | CD | troughs (owner: trough code to CD) | - | `compat.TroughRetryAccess` |
| `FeedingTroughModuleMixin` | common | quark | hard quark; config | CD | troughs (owner: trough code to CD) | `PERF.troughMissCooldownTicks` | `compat.TroughRetryAccess` |
| `MushroomCowBreedingMixin` | common | minecraft | none | CD ? | mooshroom breeding (husbandry, pairs with More Mooblooms member); plan 2.2 leaves mooshroom/ to the audit | - | `mooshroom.MooshroomFamily`, `mooshroom.MushroomTypeSetter` |
| `AnimalCanMateMooshroomMixin` | common | minecraft | none | CD ? | mooshroom breeding (husbandry, pairs with More Mooblooms member); plan 2.2 leaves mooshroom/ to the audit | - | `mooshroom.MooshroomFamily` |
| `CannedGolemRendererMixin` (removed 2026-11-02: direct code in the Canned Goods member, [cans.md](../cans.md) 9) | client | cannedgoods | hard cannedgoods | CD | canned golem dye (cans) | - | `compat.DyeableCannedGolem` |
| `SeasonedItemModelMixin` | client | minecraft | none | CD | seasoned item model (food/) | - | `food.SeasonedItemModel` |

## CB (25: 14 common, 11 client)

| mixin class | side | target mod | gate | target jar | reason | config keys | helper classes |
|---|---|---|---|---|---|---|---|
| `CopperPoisoningEffectMixin` | common | everythingcopper | @Pseudo everythingcopper | CB | effect mixin (CB move plan phase 2) | - | `potion.CopperPoisoningHandler` |
| `LivingEntityEffectRemapMixin` | common | minecraft | none | CB | effect mixin (CB move plan phase 2) | - | `potion.EffectRules` |
| `LivingEntityUseItemIconMixin` | common | minecraft | none | CB | effect mixin (CB move plan phase 2) | - | `potion.EffectRules` |
| `CreateTeaPipeIconMixin` | common | create | @Pseudo create | CB | effect mixin (CB move plan phase 2) | - | - |
| `FruitsDelightEffectFuncMixin` | common | fruitsdelight | @Pseudo fruitsdelight | CB | effect mixin (CB move plan phase 2) | - | `potion.EffectRules` |
| `CaffeinatedEffectMixin` | common | farmersrespite | @Pseudo farmersrespite | CB | effect mixin (CB move plan phase 2) | - | - |
| `MechanicalMixerBlockEntityMixin` | common | create | hard create | CB | potion machine mixin (CB move plan phase 2) | - | `fluidbrewing.DragonBreathBrewing` |
| `PotionFluidHandlerMixin` | common | create | hard create | CB | potion machine mixin (CB move plan phase 2) | - | - |
| `PotionHelperMixin` | common | immersiveengineering | hard immersiveengineering | CB | potion machine mixin (CB move plan phase 2) | - | `fluidbrewing.PotionMixDedup` |
| `PotionMixingRecipesMixin` | common | create | hard create | CB | potion machine mixin (CB move plan phase 2) | - | `fluidbrewing.PotionMixDedup` |
| `PotionRecipeGeneratorsMixin` | common | immersiveengineering | hard immersiveengineering | CB | potion machine mixin (CB move plan phase 2) | - | - |
| `EchoArmorEffectProcedureMixin` | common | create_deep_dark | hard create_deep_dark | CB | gear effect, CB move plan phase 2 (echo set bonus and its tooltip) | - | - |
| `EchoArmorTooltipMixin` | common | create_deep_dark | hard create_deep_dark | CB | gear effect, CB move plan phase 2 (echo set bonus and its tooltip) | - | - |
| `MobEffectCategoryMixin` | common | minecraft | none | CB | effect mixin (CB move plan phase 2) | - | `potion.EffectCategories` |
| `GuiEffectIconMixin` | client | minecraft | none | CB | effect mixin (CB move plan phase 2) | - | `potion.EffectRules` |
| `JeedEffectRendererMixin` | client | jeed | @Pseudo jeed | CB | effect mixin (CB move plan phase 2) | - | `potion.CompoundEffects` |
| `JeedEffectInfoMixin` | client | jeed | @Pseudo jeed | CB | effect mixin (CB move plan phase 2) | - | `potion.CompoundEffects` |
| `ItemDrainCategoryMixin` | client | create | hard create | CB | JEI potion mixin (CB move plan phase 2: Spout, ItemDrain, JEIHelper, IE categories with PotionFluidTooltip) | - | - |
| `SpoutCategoryMixin` | client | create | hard create | CB | JEI potion mixin (CB move plan phase 2: Spout, ItemDrain, JEIHelper, IE categories with PotionFluidTooltip) | - | - |
| `MixerRecipeCategoryMixin` | client | immersiveengineering | hard immersiveengineering | CB | JEI potion mixin (CB move plan phase 2: Spout, ItemDrain, JEIHelper, IE categories with PotionFluidTooltip) | - | `compat.PotionFluidTooltip` |
| `RefineryRecipeCategoryMixin` | client | immersiveengineering | hard immersiveengineering | CB | JEI potion mixin (CB move plan phase 2: Spout, ItemDrain, JEIHelper, IE categories with PotionFluidTooltip) | - | `compat.PotionFluidTooltip` |
| `BottlingMachineRecipeCategoryMixin` | client | immersiveengineering | hard immersiveengineering | CB | JEI potion mixin (CB move plan phase 2: Spout, ItemDrain, JEIHelper, IE categories with PotionFluidTooltip) | - | `compat.PotionFluidTooltip` |
| `SqueezerRecipeCategoryMixin` | client | immersiveengineering | hard immersiveengineering | CB | JEI potion mixin (CB move plan phase 2: Spout, ItemDrain, JEIHelper, IE categories with PotionFluidTooltip) | - | `compat.PotionFluidTooltip` |
| `FermenterRecipeCategoryMixin` | client | immersiveengineering | hard immersiveengineering | CB | JEI potion mixin (CB move plan phase 2: Spout, ItemDrain, JEIHelper, IE categories with PotionFluidTooltip) | - | `compat.PotionFluidTooltip` |
| `JEIHelperMixin` | client | immersiveengineering | hard immersiveengineering | CB | JEI potion mixin (CB move plan phase 2: Spout, ItemDrain, JEIHelper, IE categories with PotionFluidTooltip) | - | `compat.AutomatedPotionMixingCategory`, `compat.LingerificationCategory` |

## Common (71: 29 common, 42 client)

| mixin class | side | target mod | gate | target jar | reason | config keys | helper classes |
|---|---|---|---|---|---|---|---|
| `IwBlockGrowthHandlerMixin` | common | immersive_weathering | hard immersive_weathering | Common | perf: Immersive Weathering random-tick fast path (Q3) | - | `perf.IwGrowthFast` |
| `IwBlockGrowthHolderMixin` | common | minecraft | none | Common | perf: Immersive Weathering random-tick fast path (Q3) | - | `perf.IwGrowthHolder` |
| `IwServerLevelTickMixin` | common | minecraft | none; sysprop creativeengineering.iwFastTick (plugin postApply) | Common | perf: Immersive Weathering random-tick fast path (Q3) | - | - |
| `ArmorTrimsLargeItemListsMixin` | common | armor_trims | hard armor_trims | Common ? | perf cache of Armor Trims item lists; Compat if the owner counts it as a third-party tweak rather than perf | - | `compat.ArmorTrimsItemCache` |
| `FluidTagInputMixin` | common | immersiveengineering | hard immersiveengineering | Common ? | FluidTagInput is listed as a shared API in plan 2.4; the mixin is an IE fluid-input tweak shared by Bottling Machine, Mixer and Refinery (potion inputs), so CB or Engineering are the alternatives | - | - |
| `MobEffectTickContextMixin` | common | minecraft | none | Common | EffectTickContext shared API (plan 2.4) | - | `shield.EffectTickContext` |
| `KubeJSVirtualDataPackListMixin` | common | kubejs | hard kubejs | Common | KubeJS mixin (core) | - | - |
| `HolderReferenceUnboundMixin` | common | minecraft | none | Common | engine fix: unbound Holder.Reference.is no longer throws (perf.md 13) | - | - |
| `LevelThreadSafeRandomMixin` | common | minecraft | none | Common | thread-safe random (Q3) | - | - |
| `WorldEditSkipStateIdInitMixin` | common | worldedit | hard worldedit | Common | engine fix: lazy WorldEdit block state ids (startup time) | - | - |
| `WorldEditBlockStateIdAccessMixin` | common | worldedit | hard worldedit | Common | engine fix: lazy WorldEdit block state ids (startup time) | - | - |
| `QuarkSpiralSpiresTeleportMixin` | common | quark | hard quark | Common ? | perf: Quark viaduct hook forced sync chunk gen on teleport; Compat if read as a Quark tweak | - | - |
| `WanderingTraderSpawnerMixin` | common | minecraft | none | Common | trader system stays in patcherMod (creative_brewing.md 4.2: TradeListings stays; CB calls it) | - | `trader.WanderingTraderTweaks` |
| `WanderingTraderTradesMixin` | common | minecraft | none | Common | trader system stays in patcherMod (creative_brewing.md 4.2: TradeListings stays; CB calls it) | - | `trader.WanderingTraderTweaks` |
| `CreateGravityDimensionMixin` | common | create_gravity | hard create_gravity; config | Common ? | perf gate for Create: Gravity per dimension; Compat if the PERF.gravity* keys move with it | `PERF.gravityDimensions`, `PERF.gravityLimitDimensions` | - |
| `ServerAdvancementManagerMixin` | common | minecraft | none; COMMON_SPEC.isLoaded in code; config | Common | advancement drop (plan 2.4) | `ADVANCEMENTS.enabled` | - |
| `SectionUpdateEncodeFixMixin` | common | minecraft | none | Common | section updates / null-state packets (engine fix) | - | `util.BlockStateIdOverflow` |
| `KubeJSScriptManagerInvoker` | common | kubejs | hard kubejs | Common | KubeJS mixin (core) | - | - |
| `KubeJSStartupJarScriptsMixin` | common | kubejs | hard kubejs | Common | KubeJS mixin (core) | - | `compat.kubejs.JarStartupScripts` |
| `ServerTickPerfMixin` | common | minecraft | none | Common | perf/ tick timing and stats | - | `perf.PerfEvents`, `perf.PerfJfrEvents` |
| `ServerLevelTickPerfMixin` | common | minecraft | none | Common | perf/ tick timing and stats | - | `perf.PerfEvents`, `perf.PerfJfrEvents` |
| `ServerChunkCacheWaitPerfMixin` | common | minecraft | none | Common | perf/ tick timing and stats | - | `perf.PerfEvents` |
| `LevelEntityTickPerfMixin` | common | minecraft | none | Common | perf/ tick timing and stats | - | `perf.PerfEvents` |
| `BlockEntityTickPerfMixin` | common | minecraft | none | Common | perf/ tick timing and stats | - | `perf.PerfEvents` |
| `EntityTypePerfStatsMixin` | common | minecraft | none | Common | perf/ tick timing and stats | - | `perf.PerfEvents`, `perf.PerfStatsHolder` |
| `BlockEntityTypePerfStatsMixin` | common | minecraft | none | Common | perf/ tick timing and stats | - | `perf.PerfEvents`, `perf.PerfStatsHolder` |
| `NeaWhistleGoalSkipMixin` | common | nock_enough_arrows | @Pseudo nock_enough_arrows | Common ? | perf: skips Nock Enough Arrows whistle tempt goal (perf/WhistleArrowTracker); Equipment if read as an arrows row | - | `perf.WhistleArrowTracker` |
| `KubeJSEventHandlerListenMixin` | common | kubejs | hard kubejs | Common | perf: KubeJS handler timing | - | `perf.KubeJSHandlerTiming` |
| `KubeJSEventHandlerContainerMixin` | common | kubejs | hard kubejs | Common | perf: KubeJS handler timing | - | `perf.KubeJSHandlerTiming` |
| `KubeJSClientResourceScriptsMixin` | client | kubejs | hard kubejs | Common | KubeJS mixin (core) | - | - |
| `JeiRecipeGuiLogicMixin` | client | jei | hard jei; config | Common | JEI tweak (compat/jei) | `JEI.craftableFirst` | `compat.jei.CraftableFirstRecipeLayoutList` |
| `JeiMissingSlotsErrorAccessor` | client | jei | hard jei | Common | JEI tweak (compat/jei) | - | - |
| `JeiRecipeLayoutTooltipMixin` | client | jei | hard jei | Common | JEI tweak (compat/jei) | - | - |
| `JeiFluidHelperTooltipMixin` | client | jei | hard jei | Common | JEI fluid tooltip via DrinkFluidEffects (shared API, plan 2.4) | - | `compat.DrinkFluidEffects` |
| `JeiFluidTankScaleMixin` | client | jei | hard jei; config | Common | JEI tweak (compat/jei) | `JEI.fluidAmountScale` | `compat.jei.FluidTankScale` |
| `JeiRecipesGuiTooltipMixin` | client | jei | hard jei | Common | JEI tweak (compat/jei) | - | - |
| `JeiIngredientFilterAccessor` | client | jei | hard jei | Common | JEI tweak (compat/jei) | - | - |
| `JeiIngredientFilterMixin` | client | jei | hard jei | Common | JEI tweak (compat/jei) | - | `compat.jei.PackIngredientOrder` |
| `JeiStarterMixin` | client | jei | hard jei | Common | JEI tweak (compat/jei) | - | `compat.jei.PackIngredientOrder` |
| `JeiPluginCallerMixin` | client | jei | hard jei | Common | JEI tweak (compat/jei) | - | `compat.jei.JeiIngredientChanges` |
| `JeiIngredientManagerMixin` | client | jei | hard jei | Common | JEI tweak (compat/jei) | - | `compat.jei.JeiIngredientChanges` |
| `LCEasyScreenHelperMixin` | client | lightmanscurrency | hard lightmanscurrency | Common | tooltip mixin (Lightman's Currency) | - | `compat.LightmansTooltipStacks` |
| `LCEasyGuiGraphicsMixin` | client | lightmanscurrency | hard lightmanscurrency | Common | tooltip mixin (Lightman's Currency) | - | `compat.LightmansTooltipStacks` |
| `SubMenuConfigScreenMixin` | client | create | hard create | Common | config framework: string widget in Create's config screen | - | `compat.StringEntry` |
| `ArmorBarRendererAccessor` | client | detailab | hard detailab | Common | armor bar (Interface) | - | - |
| `ArmorBarRendererOverflowTickMixin` | client | detailab | hard detailab | Common | armor bar (Interface) | - | - |
| `CreativeMenuDisableMixin` | client | minecraft | none | Common | perf: creative search cache freeze | - | - |
| `SyncGlDebugOutputMixin` | client | minecraft | none | Common | GL debug sync (Q3) | - | - |
| `SectionUpdateNullStateMixin` | client | minecraft | none | Common | section updates / null-state packets (engine fix) | - | `devtools.BlockStateSyncDebug`, `util.BlockStateIdOverflow` |
| `BlockUpdateNullStateMixin` | client | minecraft | none | Common | section updates / null-state packets (engine fix) | - | `devtools.BlockStateSyncDebug` |
| `IcebergTitleBreakMixin` | client | iceberg | hard iceberg | Common | Legendary Tooltips / Iceberg fix (Interface) | - | - |
| `ContainerTitlePlaqueMixin` | client | minecraft | none | Common | GUI title plaque (Interface) | - | `compat.guititle.TitlePlaques` |
| `ToastFilterMixin` | client | minecraft | none; config | Common | toast filter (Interface) | `TOASTS.advancement`, `TOASTS.tutorial` | - |
| `RenderSystemShaderTextureMixin` | client | minecraft | none | Common | GUI title plaque (Interface) | - | `compat.guititle.TitlePlaques` |
| `CreativePickBlockSyncMixin` | client | minecraft | none | Common | engine fix: creative pick-block desync | - | - |
| `ChatOpenArmMixin` | client | minecraft | none | Common | engine fix: chat key typed into chat (MC-122477), [fixes_compat.md](../fixes_compat.md) 2.33 | - | `util.ChatOpenKeyGuard` |
| `ChatOpenCharDropMixin` | client | minecraft | none | Common | engine fix: chat key typed into chat (MC-122477), [fixes_compat.md](../fixes_compat.md) 2.33 | - | `util.ChatOpenKeyGuard` |
| `CckCookingGuideMealTooltipMixin` | client | createcentralkitchen | hard createcentralkitchen | Common | tooltip mixin (stack for AppleSkin): Create Central Kitchen guides | - | `compat.MealSlotTooltip` |
| `CckBrewingGuideMealTooltipMixin` | client | createcentralkitchen | hard createcentralkitchen | Common | tooltip mixin (stack for AppleSkin): Create Central Kitchen guides | - | `compat.MealSlotTooltip` |
| `PrettyPipesTerminalTooltipMixin` | client | prettypipes | hard prettypipes | Common | tooltip mixin (stack for AppleSkin): Pretty Pipes terminal | - | - |
| `FtbTooltipListAccessor` | client | ftblibrary | hard ftblibrary | Common | tooltip mixin (FTB Library / Quests) | - | - |
| `FtbGuiHelperMixin` | client | ftblibrary | hard ftblibrary | Common | tooltip mixin (FTB Library / Quests) | - | `compat.FtbTooltipStacks` |
| `FtbItemTaskTooltipMixin` | client | ftbquests | hard ftbquests | Common | tooltip mixin (FTB Library / Quests) | - | `compat.FtbTooltipStacks` |
| `FtbQuestDescriptionTooltipMixin` | client | ftbquests | hard ftbquests | Common | tooltip mixin (FTB Library / Quests) | - | `compat.FtbTooltipStacks` |
| `FtbScreenWrapperTooltipMixin` | client | ftblibrary | hard ftblibrary | Common | tooltip mixin (FTB Library / Quests) | - | `compat.FtbTooltipStacks` |
| `FtbMenuScreenWrapperTooltipMixin` | client | ftblibrary | hard ftblibrary | Common | tooltip mixin (FTB Library / Quests) | - | `compat.FtbTooltipStacks` |
| `WaresAgreementTooltipMixin` | client | wares | hard wares | Common | tooltip mixin (Wares) | - | - |
| `PatchouliBookTooltipMixin` | client | patchouli | hard patchouli | Common | tooltip mixin (Patchouli) | - | - |
| `AeBaseScreenTooltipMixin` | client | ae2 | hard ae2 | Common | tooltip mixin (AE2) | - | `compat.AeTooltipStacks` |
| `AeTableRendererTooltipMixin` | client | ae2 | hard ae2 | Common | tooltip mixin (AE2) | - | `compat.AeTooltipStacks` |
| `AePatternEncodingTooltipMixin` | client | ae2 | hard ae2 | Common | tooltip mixin (AE2) | - | `compat.AeTooltipStacks` |
| `AeGuideItemTooltipMixin` | client | ae2 | hard ae2 | Common | tooltip mixin (AE2) | - | - |
| `FallingBlockWaterSplashMixin` | client | minecraft | none | Common | engine fix: falling blocks splash in water | - | - |

## Summary

### Count per target and side

| target | common | client | server | total |
|---|---|---|---|---|
| Engineering | 27 | 4 | 0 | 31 |
| Equipment | 20 | 0 | 0 | 20 |
| Magic (Equipment) | 3 | 0 | 0 | 3 |
| Worldgen | 56 | 0 | 0 | 56 |
| Compat | 31 | 10 | 0 | 41 |
| Calendar | 10 | 8 | 0 | 18 |
| CD | 22 | 2 | 0 | 24 |
| CB | 14 | 11 | 0 | 25 |
| Common | 29 | 42 | 0 | 71 |
| **all** | 212 | 77 | 0 | 289 |

Devtools: 0 mixins. The block-state probe (`devtools.BlockStateSyncDebug`) is reached from two Common packet mixins (see edges), not from a mixin of its own. `pack`, `Computers (Engineering)`: 0 mixins (no AE2 or ComputerCraft mixin is more than a tooltip; the AE2 tooltip mixins are Common).

### Doubtful rows (16)

| mixin | target | note |
|---|---|---|
| `ArmorTrimsLargeItemListsMixin` | Common ? | perf cache of Armor Trims item lists; Compat if the owner counts it as a third-party tweak rather than perf |
| `FluidTagInputMixin` | Common ? | FluidTagInput is listed as a shared API in plan 2.4; the mixin is an IE fluid-input tweak shared by Bottling Machine, Mixer and Refinery (potion inputs), so CB or Engineering are the alternatives |
| `SequencedAssemblyRecipeMixin` | Engineering ? | Create sequenced assembly NBT passthrough; a Create recipe fix but it exists only for the Equipment gear progression (echo/felsic/dragon assembly) |
| `BonemealCharmMixin` | Equipment ? | bonemeal charm held item; generalised Vinery Winemaker trick, could be Magic |
| `QuarkSpiralSpiresTeleportMixin` | Common ? | perf: Quark viaduct hook forced sync chunk gen on teleport; Compat if read as a Quark tweak |
| `CreateGravityDimensionMixin` | Common ? | perf gate for Create: Gravity per dimension; Compat if the PERF.gravity* keys move with it |
| `BubbleColumnImmunityMixin` | Worldgen ? | bubble column immunity by entity tag for big ocean mobs (fauna); the tag lives in server_scripts/tags |
| `MushroomCowWalkTargetMixin` | Worldgen ? | mooshroom walk target = spawn ground; plan 2.2 leaves mooshroom/ to the audit (Worldgen fauna or CD with More Mooblooms) |
| `MushroomCowBreedingMixin` | CD ? | mooshroom breeding (husbandry, pairs with More Mooblooms member); plan 2.2 leaves mooshroom/ to the audit |
| `AnimalCanMateMooshroomMixin` | CD ? | mooshroom breeding (husbandry, pairs with More Mooblooms member); plan 2.2 leaves mooshroom/ to the audit |
| `VineBlockShearMixin` | Compat ? | sheared modded vines (block/VineShearing); a cross-mod block tweak, Sweep A decides block/ |
| `SaltBlockRandomTickMixin` | Compat ? | Vintage salt family random ticks and Salt mod dissolving (block/SaltBlockTicks); third-party block tweak |
| `SaltLayerRandomTickMixin` | Compat ? | Vintage salt family random ticks and Salt mod dissolving (block/SaltBlockTicks); third-party block tweak |
| `SaltRockNoDissolveMixin` (removed 2026-11-02: direct code in the Salt member's `Dissolving`, [salt.md](../salt.md)) | Compat ? | Vintage salt family random ticks and Salt mod dissolving (block/SaltBlockTicks); third-party block tweak |
| `NeaWhistleGoalSkipMixin` | Common ? | perf: skips Nock Enough Arrows whistle tempt goal (perf/WhistleArrowTracker); Equipment if read as an arrows row |
| `JetpackItemTooltipMixin` | Engineering ? | Dreams and Desires jetpack tooltip null-level crash fix; Common if read as an engine fix |

### Cross-jar edges (4: mixin whose helper class or config key belongs to another target)

Helper classes that resolve to Common (shared APIs, `CEConfig`, perf) are not listed; every content jar depends on Common by rule 3.2.

| mixin | mixin target | helper or key -> its target | fix (rule 3.2) |
|---|---|---|---|
| `FeedingTroughModuleMixin` | CD | CEConfig.PERF.troughMissCooldownTicks -> Common | PERF.* key moves to the mixin's jar config (Sweep C flags it) or stays Common |
| `SereneSeasonsMeltThrottleMixin` | Calendar | CEConfig.PERF.seasonsMeltIntervalTicks -> Common | PERF.* key moves to the mixin's jar config (Sweep C flags it) or stays Common |
| `SectionUpdateNullStateMixin` | Common | devtools.BlockStateSyncDebug -> Devtools | probe hook must become a Common API (e.g. a listener Devtools registers) or the probe moves to Common |
| `BlockUpdateNullStateMixin` | Common | devtools.BlockStateSyncDebug -> Devtools | probe hook must become a Common API (e.g. a listener Devtools registers) or the probe moves to Common |

### Package-mismatch edges (52: helper lives in a package whose plan-2 nominal jar differs from the mixin target)

These are not dependency edges once Sweep A moves the helper with its feature; they are listed so the Java sweep assigns the helper to the same jar as its mixin (or the merge pass moves the mixin instead). Nominal package targets used: compat/ Compat, perf/ Common, block/ Engineering, mobs/ Worldgen, shield/ Equipment, food/ CD, fluid/ Engineering, trader/ Common, devtools/ Devtools, potion/ CB.

| mixin | mixin target | helper | helper package nominal jar |
|---|---|---|---|
| `ImmersivePostsRegistrationMixin` | Engineering | `compat.ImmersivePostsMaterials` | Compat |
| `ImmersivePostsValidItemMixin` | Engineering | `compat.ImmersivePostsMaterials` | Compat |
| `TagLoaderImmersivePostsMixin` | Engineering | `compat.ImmersivePostsMaterials` | Compat |
| `LCItemsForMapTradeMixin` | Worldgen | `compat.AsyncMapTrades` | Compat |
| `CSSellingMapMixin` | Worldgen | `compat.AsyncMapTrades` | Compat |
| `ArmorTrimsLargeItemListsMixin` | Common ? | `compat.ArmorTrimsItemCache` | Compat |
| `MobCobPipeSlotMixin` | CD | `compat.CornDelightCompat` | Compat |
| `MobEffectTickContextMixin` | Common | `shield.EffectTickContext` | Equipment |
| `CannedGolemColorMixin` (removed 2026-11-02: direct code in the Canned Goods member, [cans.md](../cans.md) 9) | CD | `compat.DyeableCannedGolem` | Compat |
| `TemptGoalTroughRetryMixin` | CD | `compat.TroughRetryAccess` | Compat |
| `FeedingTroughModuleMixin` | CD | `compat.TroughRetryAccess` | Compat |
| `LeavesDecayRangeMixin` | Worldgen | `block.LeafDecayRange` | Engineering |
| `VineBlockShearMixin` | Compat ? | `block.VineShearing` | Engineering |
| `SaltBlockRandomTickMixin` | Compat ? | `block.SaltBlockTicks` | Engineering |
| `SaltLayerRandomTickMixin` | Compat ? | `block.SaltBlockTicks` | Engineering |
| `SaltRockNoDissolveMixin` (removed 2026-11-02: direct code in the Salt member's `Dissolving`, [salt.md](../salt.md)) | Compat ? | `block.SaltBlockTicks` | Engineering |
| `KubeJSStartupJarScriptsMixin` | Common | `compat.kubejs.JarStartupScripts` | Compat |
| `MobCapLevelTickMixin` | Worldgen | `perf.MobCapLevels` | Common |
| `MobCapMixin` | Worldgen | `perf.MobCapLevels` | Common |
| `MobCapSpawnAreaMixin` | Worldgen | `perf.MobCapLevels` | Common |
| `MixerRecipeCategoryMixin` | CB | `compat.PotionFluidTooltip` | Compat |
| `RefineryRecipeCategoryMixin` | CB | `compat.PotionFluidTooltip` | Compat |
| `BottlingMachineRecipeCategoryMixin` | CB | `compat.PotionFluidTooltip` | Compat |
| `SqueezerRecipeCategoryMixin` | CB | `compat.PotionFluidTooltip` | Compat |
| `FermenterRecipeCategoryMixin` | CB | `compat.PotionFluidTooltip` | Compat |
| `JEIHelperMixin` | CB | `compat.AutomatedPotionMixingCategory` | Compat |
| `JEIHelperMixin` | CB | `compat.LingerificationCategory` | Compat |
| `JeiRecipeGuiLogicMixin` | Common | `compat.jei.CraftableFirstRecipeLayoutList` | Compat |
| `JeiFluidHelperTooltipMixin` | Common | `compat.DrinkFluidEffects` | Compat |
| `JeiFluidTankScaleMixin` | Common | `compat.jei.FluidTankScale` | Compat |
| `JeiIngredientFilterMixin` | Common | `compat.jei.PackIngredientOrder` | Compat |
| `JeiStarterMixin` | Common | `compat.jei.PackIngredientOrder` | Compat |
| `JeiPluginCallerMixin` | Common | `compat.jei.JeiIngredientChanges` | Compat |
| `JeiIngredientManagerMixin` | Common | `compat.jei.JeiIngredientChanges` | Compat |
| `LCEasyScreenHelperMixin` | Common | `compat.LightmansTooltipStacks` | Compat |
| `LCEasyGuiGraphicsMixin` | Common | `compat.LightmansTooltipStacks` | Compat |
| `SubMenuConfigScreenMixin` | Common | `compat.StringEntry` | Compat |
| `CannedGolemRendererMixin` (removed 2026-11-02: direct code in the Canned Goods member, [cans.md](../cans.md) 9) | CD | `compat.DyeableCannedGolem` | Compat |
| `SectionUpdateNullStateMixin` | Common | `devtools.BlockStateSyncDebug` | Devtools |
| `BlockUpdateNullStateMixin` | Common | `devtools.BlockStateSyncDebug` | Devtools |
| `ContainerTitlePlaqueMixin` | Common | `compat.guititle.TitlePlaques` | Compat |
| `RenderSystemShaderTextureMixin` | Common | `compat.guititle.TitlePlaques` | Compat |
| `CckCookingGuideMealTooltipMixin` | Common | `compat.MealSlotTooltip` | Compat |
| `CckBrewingGuideMealTooltipMixin` | Common | `compat.MealSlotTooltip` | Compat |
| `FtbGuiHelperMixin` | Common | `compat.FtbTooltipStacks` | Compat |
| `FtbItemTaskTooltipMixin` | Common | `compat.FtbTooltipStacks` | Compat |
| `FtbQuestDescriptionTooltipMixin` | Common | `compat.FtbTooltipStacks` | Compat |
| `FtbScreenWrapperTooltipMixin` | Common | `compat.FtbTooltipStacks` | Compat |
| `FtbMenuScreenWrapperTooltipMixin` | Common | `compat.FtbTooltipStacks` | Compat |
| `AeBaseScreenTooltipMixin` | Common | `compat.AeTooltipStacks` | Compat |
| `AeTableRendererTooltipMixin` | Common | `compat.AeTooltipStacks` | Compat |
| `AePatternEncodingTooltipMixin` | Common | `compat.AeTooltipStacks` | Compat |

### Notes for the merge pass

- Reservation marks: the three enchantment mixins are the only `Magic (Equipment)` rows; no mixin is `Computers (Engineering)`.
- Plan 2.4 lists `FluidTagInput` as a shared API; `FluidTagInputMixin` is marked `Common ?` on that basis, but it is really an IE recipe-input tweak for potion inputs (CB) and should be decided together with Sweep A's `compat/FluidTagInput` row.
- All 289 mixins are `remap = false` (SRG) apart from the vanilla-target ones using default remap, so each moves unchanged except for its package (rule 3.6). Every jar with a hard mod target needs that mod in its `mods.toml` dependencies or a `needsMod` entry in its GatedMixinPlugin subclass.
- `perf.CEMixinPlugin` itself is Common; its `IwServerLevelTickMixin` rewrite stays with the IW perf mixins in Common.
