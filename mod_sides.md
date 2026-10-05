# Mod sides: client vs server

Every jar in `mods/` and whether the dedicated server needs it. `tools/dump_server_mods.py` reads the table below to build the server's mod folder, so keep it current when you add, remove or re-classify a mod.

Mods are matched by **mod id**, so updating a mod to a new version needs no edit here. The **Jar** column is informational, except for jars with no mod id, which are matched by file name.

| Side | Meaning | Goes on the server? |
|---|---|---|
| `both` | Adds content or logic both sides must agree on (blocks, items, entities, recipes, network packets) | Yes |
| `server` | Only does anything on the server; harmless on clients | Yes |
| `client` | Rendering, HUD, tooltips, menus, recipe viewers. The server never loads it | No |

**Current count:** 335 both, 13 server, 42 client (390 jars).

## Keeping this up to date

- **New mod:** add a row. Use `client` only if it adds no blocks, items or entities, and its page or `mods.toml` says it's client-side. If you're unsure, use `both`.
- **Removed mod:** delete its row. The dump script warns about rows whose mod is no longer installed.
- **Deploy a release:** `python3 tools/build_deploy.py --notes "..."` stages `deploy/latest/` (server jars in `mods/`, client-only jars in `client_mods/`, kubejs, config, packs, removal lists) and logs it in `deploy/DEPLOY_LOG.md`. It refuses to build while a jar has no row here.
- **Build the server mod folder:** `python3 tools/dump_server_mods.py` writes the server jars to `server_mods/` in the instance root. `--dry-run` lists what it would copy.

## Mods

| Side | Mod | Mod id | Jar | Notes |
|---|---|---|---|---|
| both | Dustrial Decor | `dustrial_decor` | `DustrialDecor-1.3.3-1.19.2.jar` |  |
| both | Advanced Peripherals | `advancedperipherals` | `AdvancedPeripherals-1.19.2-0.7.37r.jar` |  |
| both | AE2 QoL Recipes | `ae2qolrecipes` | `ae2qolrecipes-forge-1.18-1.20.1-1.3.0.jar` |  |
| both | Alex's Delight | `alexsdelight` | `alexsdelight-1.4.1.jar` |  |
| both | Alex's Mobs | `alexsmobs` | `alexsmobs-1.21.1.jar` | Required by: Alex's Delight |
| both | Allurement | `allurement` | `allurement-1.19.2-3.2.1.jar` |  |
| both | Another Furniture | `another_furniture` | `another_furniture-forge-1.19.2-2.1.4.jar` |  |
| both | AppleSkin | `appleskin` | `appleskin-forge-mc1.19-2.4.2.jar` | Server syncs saturation/exhaustion to players |
| both | Applied Botanics | `appbot` | `Applied-Botanics-forge-1.4.6.jar` |  |
| both | Applied Cooking | `appliedcooking` | `appliedcooking-2.0.3.jar` |  |
| both | Applied Energistics 2 | `ae2` | `appliedenergistics2-forge-12.9.12.jar` | Required by: Applied Botanics, Create Applied Kinetics, Fullblock Energistics, Polymorphic Energistics |
| both | Architect's Palette | `architects_palette` | `Architects-Palette-1.19.2-1.3.5.jar` |  |
| both | Architectury | `architectury` | `architectury-6.6.92-forge.jar` | Required by: Create Goggles, FTB Library, FTB Quests, FTB Teams, Item Filters, Just Enough Sculk Sensor (+9 more) |
| both | Armor Trims Backport | `armor_trims` | `armortrims-1.19.2-1.3.3.jar` |  |
| both | AutoRegLib | `autoreglib` | `AutoRegLib-1.8.2-55.jar` | Required by: Quark |
| both | Bad Packets | `badpackets` | `badpackets-forge-0.2.3.jar` | Networking library for WTHIT; Required by: wthit |
| both | Balm | `balm` | `balm-forge-1.19.2-4.6.0.jar` | Required by: CookingForBlockheads, Forgiving Void, NetherPortalFix, TrashSlot, Waystones |
| both | Beautify | `beautify` | `beautify-1.4.3.jar` |  |
| both | Bedspreads | `bedspreads` | `bedspreads-forge-6.0.0+1.19.2.jar` |  |
| both | BetterDays | `betterdays` | `betterdays-1.19.2-3.3.4.3-FORGE.jar` |  |
| both | Blueprint | `blueprint` | `blueprint-1.19.2-6.2.0.jar` | Required by: Allurement, Buzzier Bees, Clayworks, Incubation |
| both | Bobber Detector | `bobberdetector` | `bobberdetector-0.1.9-1.19.2.jar` |  |
| both | Book Fishing | `book_fishing` | `Book Fishing 1.0.0 - 1.19.2.jar` |  |
| both | Bookshelf | `bookshelf` | `Bookshelf-Forge-1.19.2-16.3.20.jar` |  |
| both | Botania | `botania` | `Botania-1.19.2-440.2-FORGE.jar` | Required by: Applied Botanics, KubeJS Botania |
| both | Brewin' and Chewin' | `brewinandchewin` | `BrewinAndChewin-1.19-2.0.jar` |  |
| both | Brick Hopper | `brickhopper` | `brickhopper-1.19-1.4.2.1.jar` |  |
| both | Buzzier Bees | `buzzier_bees` | `buzzier_bees-1.19.2-5.0.1.jar` |  |
| both | Caelus API | `caelus` | `caelus-forge-1.19.2-3.0.0.6.jar` | Required by: Elytra Slot |
| both | Cat Loaf | `catloaf` | `catloaf-1.1.1-1.19.x-FORGE.jar` |  |
| both | CBMultipart | `cb_multipart` | `CBMultipart-1.19.2-3.2.0.139-universal.jar` | Required by: ProjectRed Core, ProjectRed Fabrication, ProjectRed Illumination, ProjectRed Integration, ProjectRed Transmission, cb_microblock (+1 more) |
| both | CC: Tweaked | `computercraft` | `cc-tweaked-1.19.2-1.101.4.jar` | Required by: Advanced Peripherals, CC:C Bridge, KubeJS + CC: Tweaked |
| both | CC:C Bridge | `cccbridge` | `cccbridge-mc1.19.2-forge-v1.5.1.jar` |  |
| both | CERBON's Better Beacons | `better_beacons` | `CerbonsBetterBeacons-Forge-1.19.2-1.0.2.jar` |  |
| both | Chalk | `chalk` | `chalk-1.19.2-1.6.4.jar` |  |
| both | Charm of Undying | `charmofundying` | `charmofundying-forge-6.2.1+1.19.2.jar` |  |
| both | Chefs Delight | `chefsdelight` | `chefsdelight-1.0.3-forge-1.19.2.jar` |  |
| both | Chimes | `chimes` | `Chimes-v2.0.1-1.19.2.jar` |  |
| both | Citadel | `citadel` | `citadel-2.1.4-1.19.jar` | Required by: Alex's Delight, Alex's Mobs, Cloud Storage, Domestication Innovation |
| both | Clayworks | `clayworks` | `clayworks-1.19.2-2.1.0.jar` |  |
| both | Cloth Config v8 API | `cloth_config` | `cloth-config-8.3.134-forge.jar` | Required by: BetterF3, Tax Free Levels |
| both | Cloud Storage | `cloudstorage` | `cloudstorage-1.3.4-1.19.2.jar` |  |
| both | Clumps | `clumps` | `Clumps-forge-1.19.2-9.0.0+14.jar` |  |
| both | CodeChicken Lib | `codechickenlib` | `CodeChickenLib-1.19.2-4.3.2.490-universal.jar` | Required by: CBMultipart, EnderStorage, ProjectRed Core, ProjectRed Fabrication, ProjectRed Illumination, ProjectRed Integration (+3 more) |
| both | Collective | `collective` | `collective-1.19.2-7.64.jar` |  |
| both | Comfortable Campfires | `comfortable_campfires` | `comfortable-campfires-1.1.2+1.19.2.jar` |  |
| both | Comforts | `comforts` | `comforts-forge-6.0.7+1.19.2.jar` |  |
| both | Compressed Creativity | `compressedcreativity` | `compressedcreativity-1.19.2-0.1.8.jar` |  |
| both | Compressed Engineering | `compressedengineering` | `compressedengineering-1.3.5.jar` |  |
| both | Connectible Chains | `connectiblechains` | `Connectible Chains-forge-1.19.2-2.1.4.jar` |  |
| both | Construction Wand | `constructionwand` | `constructionwand-1.19.2-2.10.jar` |  |
| both | Control Engineering | `controlengineering` | `ControlEngineering-0.2.0.jar` |  |
| both | CookingForBlockheads | `cookingforblockheads` | `cookingforblockheads-forge-1.19.2-13.3.4.jar` |  |
| both | Copper Drops | `copperdrops` | `CopperDrops-1.1.jar` |  |
| both | Copper Splashing Recipes | `create_copper_splashing` | `Create_Copper_Splashing_v2.jar` |  |
| both | Corpse | `corpse` | `corpse-forge-1.19.2-1.0.17.jar` | Required by: corpsecurioscompat |
| both | corpsecurioscompat | `corpsecurioscompat` | `corpsecurioscompat-1.18.x-1.20.x-Forge-2.2.2.jar` |  |
| both | Cosmetic Armor x Corpse Compat | `cosmeticcorpsecompat` | `cosmeticcorpsecompat-1.19.x-1.20.x-Forge-1.0.0.jar` |  |
| both | CosmeticArmorReworked | `cosmeticarmorreworked` | `CosmeticArmorReworked-1.19.2-v1a.jar` |  |
| both | Create | `create` | `create-1.19.2-0.5.1.i.jar` | Required by: Bobber Detector, CC:C Bridge, Compressed Creativity, Create Applied Kinetics, Create Confectionery, Create Deco (+21 more) |
| both | Create Applied Kinetics | `createappliedkinetics` | `createappliedkinetics-1.4-1.19.2.jar` |  |
| both | Create Compact Exp (by NoCube) | `nocubescreateexp` | `nocube's_create_compact_exp_1.0.4_forge_1.19.2.jar` |  |
| both | Create Compressed | `create_compressed` | `create_compressed-2.0.0-forge-1.19.2.jar` |  |
| both | Create Confectionery | `create_confectionery` | `create-confectionery1.19.2_v1.0.9.jar` |  |
| both | Create Crafts & Additions | `createaddition` | `createaddition-1.19.2-1.2.3.jar` |  |
| both | Create Deco | `createdeco` | `createdeco-1.3.3-1.19.2.jar` |  |
| both | Create Enchantment Industry | `create_enchantment_industry` | `create_enchantment_industry-1.19.2-for-create-0.5.1.f-1.2.9.e.jar` |  |
| both | Create Goggles | `creategoggles` | `creategoggles-1.19.2-0.5.5.g-[FORGE].jar` |  |
| both | Create Mechanical Extruder | `create_mechanical_extruder` | `create_mechanical_extruder-1.19.2-1.5.7.e-44.jar` |  |
| both | Create Recycling | `create_crush_everything` | `create_recycle_1.0.2_forge_1.19.2.jar` |  |
| both | Create Slice & Dice | `sliceanddice` | `sliceanddice-forge-2.4.0.jar` |  |
| both | Create Waystone Recipes | `create_waystone_recipes` | `Create_Waystone_Recipes_1.19_v2.jar` |  |
| both | Create: Bells & Whistles | `bellsandwhistles` | `bellsandwhistles-v0.4.4-1.19.2.jar` |  |
| both | Create: Central Kitchen | `create_central_kitchen` | `create_central_kitchen-1.19.2-for-create-0.5.1.f-1.3.11.c.jar` |  |
| both | Create: Copycats+ | `copycats` | `copycats-2.2.2+mc.1.19.2-forge.jar` |  |
| both | Create: Crystal Clear | `create_crystal_clear` | `create_crystal_clear-0.2.1-1.19.2.jar` |  |
| both | Create: Deep Dark | `create_deep_dark` | `create_deep_dark-1.7.0-forge-1.19.2.jar` |  |
| both | Create: Design n' Decor | `design_decor` | `design_decor-0.4-1.19.2.jar` |  |
| both | Create: Dreams & Desires | `create_dd` | `Create-DnDesire-1.19.2-1.2c.Beta Mid-Dev.jar` |  |
| both | Create: Dynamic Village | `dynamicvillage` | `dynamicvillage-v0.4-1.19.2.jar` |  |
| both | Create: Factory | `create_factory` | `create_factory-0.0.2-1.19.2.jar` |  |
| both | Create: Framed | `createframed` | `createframed-1.19.2-1.4.5.1.jar` |  |
| both | Create: Generators | `create_generators` | `create_generators-1.0.0-forge-1.19.2.jar` |  |
| both | Create: Gravity | `create_gravity` | `create_gravity-1.19.2-1.2.0-all.jar` |  |
| both | Create: Interiors | `interiors` | `interiors-0.5.6+forge-mc1.19.2-build.105.jar` |  |
| both | Create: Netherless | `create_netherless` | `create_netherless-1.19.2-1.3.jar` |  |
| both | Create: Oxidized | `create_oxidized` | `create_oxidized-1.19.2-0.1.1.jar` |  |
| both | Create: Power Loader | `create_power_loader` | `create_power_loader-1.5.0-mc1.19.2.jar` |  |
| both | Create: Steam 'n' Rails | `railways` | `Steam_Rails-1.6.6+forge-mc1.19.2.jar` |  |
| both | create: things and misc | `create_things_and_misc` | `create_misc_and_things_ 1.19.2_4.0A.jar` |  |
| both | Create: Tweaked Controllers | `create_tweaked_controllers` | `create_tweaked_controllers-1.19.2-1.2.4.jar` |  |
| both | Create: Ultimate Factory | `create_ultimate_factory` | `create_ultimate_factory-1.9.0-forge-1.19.2.jar` |  |
| both | Create: Vintage Improvements | `vintageimprovements` | `vintageimprovements-1.19.2-0.2.0.3.jar` |  |
| both | Creative Engineering | `creativeengineering` | `creativeengineering-1.0.4.jar` | This pack's own mod (development/patcherMod); client-only parts are guarded |
| both | CreativeCore | `creativecore` | `CreativeCore_FORGE_v2.11.28_mc1.19.2.jar` | Required by: ItemPhysic |
| both | Curios API | `curios` | `curios-forge-1.19.2-5.1.6.4.jar` | Required by: Botania, Charm of Undying, Elytra Slot, corpsecurioscompat |
| both | Customizable Elytra | `customizableelytra` | `customizableelytra-1.19.0-1.7.4.jar` |  |
| both | Dave's Building Extended | `davebuildingmod` | `dbExtended-1.19.2-6.0.1.jar` |  |
| both | Decorative Blocks | `decorative_blocks` | `Decorative Blocks-forge-1.19.2-3.0.0.jar` |  |
| both | Delightful | `delightful` | `Delightful-1.19.2-3.4.1.jar` |  |
| both | Display Delight | `displaydelight` | `displaydelight-1.2.0.jar` |  |
| both | Distant Horizons | `distanthorizons` | `DistantHorizons-3.0.2-b-1.19.2-fabric-forge.jar` | Optional on server: sends distant terrain to players |
| both | Do A Barrel Roll | `do_a_barrel_roll` | `do-a-barrel-roll-2.6.2+1.19.2-forge.jar` | Optional on server: enforces its settings |
| both | Domestication Innovation | `domesticationinnovation` | `domesticationinnovation-1.6.1-1.19.2.jar` |  |
| both | Don't void Arrows | `dont_void_arrows` | `don't void arrows V.1.1-1.19.2.jar` |  |
| both | DragonLoot | `dragonloot` | `dragonloot-1.1.15-1.19.2-forge.jar` |  |
| both | Dramatic Doors | `dramaticdoors` | `DramaticDoors-QuiFabrge-1.19.2-3.2.7.jar` |  |
| both | Dreamcatcher | `dreamcatcher` | `dreamcatcher mod.jar` |  |
| both | Dynamic Villager Trades | `dynamicvillagertrades` | `dynamicvillagertrades-1.2.3-forge-1.19.2.jar` |  |
| both | Easy Elytra Takeoff | `easyelytratakeoff` | `easyelytratakeoff-1.19.2-4.2.jar` |  |
| both | Easy Magic | `easymagic` | `EasyMagic-v4.3.3-1.19.2-Forge.jar` |  |
| both | Effishiency | `effishiency` | `effishiency-1.19.2-1.1.1.jar` |  |
| both | Elytra Bombing | `ebb` | `ElytraBombing-Forge-1.19.2-1.0.0.jar` |  |
| both | Elytra Slot | `elytraslot` | `elytraslot-forge-6.1.2+1.19.2.jar` |  |
| both | End Portal | `endportal` | `endportal-1.19.2-ver1.2.jar` |  |
| both | Ender Crop | `endercrop` | `endercrop-1.19.2-1.7.0-beta.jar` |  |
| both | Ender's Delight | `endersdelight` | `endersdelight-1.19-1.2.2.jar` |  |
| both | EnderStorage | `enderstorage` | `EnderStorage-1.19.2-2.10.1.181-universal.jar` |  |
| both | Engineer's Decor | `engineersdecor` | `engineersdecor-1.19.2-forge-1.3.29.jar` |  |
| both | Engineers Delight | `tmted` | `Engineers Delight 1.19.2 R.1.3.jar` |  |
| both | Etched | `etched` | `etched-forge-2.3.2.jar` |  |
| both | Every Compat | `everycomp` | `everycomp-1.19.2-2.5.36.jar` |  |
| both | Everything is Copper | `everythingcopper` | `everythingcopper-1.19.2-2.3.4.jar` |  |
| both | Explorer's Compass | `explorerscompass` | `ExplorersCompass-1.19.2-1.3.0-forge.jar` |  |
| both | Exposure (Backport & Extras) | `exposure` | `exposureBackport-1.9.19-ce-19.2-02.jar` | This pack's port of Exposure 1.9.19 (development/exposureBackport); one jar with mod ids exposure, exposure_polaroid, exposure_catalog, exposure_expanded, plus exposure_extras (camera bags). Replaced exposure-1.19.2-1.6.0-forge.jar |
| both | Extended Cogwheels | `extendedgears` | `extendedgears-2.1.1-1.19.2-0.5.1.f-forge.jar` |  |
| both | Fairy Lights | `fairylights` | `fairylights-6.0.1-1.19.2.jar` |  |
| both | Fantasy's Furniture | `fantasyfurniture` | `fantasyfurniture-1.19.2-6.7.0.jar` |  |
| both | Farmer's Delight | `farmersdelight` | `FarmersDelight-1.19.2-1.2.4.jar` | Required by: Alex's Delight, Brewin' and Chewin', Chefs Delight, Delightful, FarmersStructures, Nether's Delight (+1 more) |
| both | FarmersStructures | `farmers_structures` | `FarmersStructures-1.0.0-1.19.2.jar` |  |
| both | Fast Workbench | `fastbench` | `FastWorkbench-1.19.2-7.1.4.jar` |  |
| both | FastFurnace | `fastfurnace` | `FastFurnace-1.19.2-7.0.0.jar` |  |
| both | FastLeafDecay | `fastleafdecay` | `FastLeafDecay-30.jar` |  |
| both | Felsic Gear | `felsic_gear` | `Felsic Gear 1.0.0  - 1.19.2.jar` |  |
| both | Ferrite Core | `ferritecore` | `ferritecore-5.0.3-forge.jar` |  |
| both | Fish On The Line | `fishontheline` | `fishontheline-1.19.2-3.2.jar` |  |
| both | Flower Patch | `flowerpatch` | `FlowerPatch-forge-1.19.2-2.0.3.jar` | Added 2026-09-26 |
| both | Forgiving Void | `forgivingvoid` | `forgivingvoid-forge-1.19-7.0.0.jar` |  |
| both | FramedBlocks | `framedblocks` | `FramedBlocks-6.8.6.jar` |  |
| both | Friendly Griefing | `friendlygriefing` | `FriendlyGriefing-1.19-1.0.2.jar` |  |
| both | FTB Library | `ftblibrary` | `ftb-library-forge-1902.4.1-build.236.jar` | Required by: Create Questing, FTB Quests, FTB Teams |
| both | FTB Quests | `ftbquests` | `ftb-quests-forge-1902.5.10-build.497.jar` | Required by: Create Questing, Quests Additions |
| both | FTB Teams | `ftbteams` | `ftb-teams-forge-1902.2.14-build.123.jar` | Required by: FTB Quests |
| both | Fullblock Energistics | `fulleng` | `fulleng-forge-1.7.1-1.19.2.jar` |  |
| both | Furnace Recycle | `furnacerecycle` | `furnacerecycle-1.19.2-2.2.jar` |  |
| both | Gates | `gates` | `gates-1.8.0.jar` |  |
| both | GeckoLib | `geckolib3` | `geckolib-forge-1.19-3.1.40.jar` |  |
| both | Geode+ | `geode` | `geodeforge-1.1.8-1.19.2.jar` |  |
| both | Geophilic Reforged | `geophilic_reforged` | `GeophilicReforged-v1.2.0.jar` |  |
| both | Golems Are Friends Not Fodder | `golemsarefriends` | `golemsarefriends-1.19.2-0.1.0.1.jar` |  |
| both | Guardian Beam Defense | `creategbd` | `Create-Guardian-Beam-Defense-1.2.3b.jar` |  |
| both | Handcrafted | `handcrafted` | `handcrafted-forge-1.19.2-2.0.6.jar` |  |
| both | Hearths | `hearths` | `hearths-v1.0.0-mc1.19u1.19.3.jar` |  |
| both | Heartstone | `heartstone` | `heartstone-1.19.2-1.1.2.jar` |  |
| both | HT's TreeChop | `treechop` | `TreeChop-1.19.2-forge-0.19.1.jar` |  |
| both | Immersive Armors | `immersive_armors` | `immersive_armors-1.5.6+1.19.2-forge.jar` |  |
| both | Immersive Engineering | `immersiveengineering` | `ImmersiveEngineering-1.19.2-9.2.4-170.jar` | Required by: Compressed Engineering, Control Engineering, Immersive Petroleum, Just Enough Immersive Multiblocks, KubeJS Immersive Engineering, More Immersive Wires (+1 more) |
| both | Immersive Paintings | `immersive_paintings` | `immersive_paintings-0.6.7+1.19.2-forge.jar` |  |
| both | Immersive Petroleum | `immersivepetroleum` | `ImmersivePetroleum-1.19.2-4.2.0-29b.jar` |  |
| both | Immersive Posts | `immersiveposts` | `ImmersivePosts-1.19.2-4.3.0-8.jar` |  |
| both | Immersive Weathering | `immersive_weathering` | `immersive_weathering-1.19.2-1.2.10-forge.jar` |  |
| both | Incubation | `incubation` | `incubation-1.19.2-3.0.1.jar` |  |
| both | Iron Fishing Rods | `iron_fishing_rods` | `Iron Fishing Rods 1.0.0 - 1.19.2.jar` |  |
| both | Item Filters | `itemfilters` | `item-filters-forge-1902.2.9-build.51.jar` | Required by: FTB Quests |
| both | ItemPhysic | `itemphysic` | `ItemPhysic_FORGE_v1.6.7_mc1.19.2.jar` |  |
| both | JRFTL | `jrftl` | `JRFTL [1.19.1]-1.4.3.jar` |  |
| both | Kiwi | `kiwi` | `Kiwi-1.19.2-forge-8.3.6.jar` | Required by: Per-Dimension Game Rules |
| both | kotlinforforge | - | `kotlinforforge-3.12.0-all.jar` | Datapack/library jar with no Forge mod id; matched by file name |
| both | KubeJS | `kubejs` | `kubejs-forge-1902.6.2-build.73.jar` | Required by: KubeJS + CC: Tweaked, KubeJS Botania, KubeJS CSV, KubeJS Create, KubeJS Immersive Engineering, LootJS (+2 more) |
| both | KubeJS + CC: Tweaked | `kjscc` | `kjscc-1902-1.0.2-beta.jar` |  |
| both | KubeJS Botania | `kubejs_botania` | `kubejs_botania-1.3.1-forge.jar` |  |
| both | KubeJS Create | `kubejs_create` | `kubejs-create-forge-1902.2.4-build.36.jar` |  |
| both | KubeJS CSV | `kubejs_csv` | `kubejs_csv-1.18.2~1.20.1-1.0.0.jar` |  |
| both | KubeJS Immersive Engineering | `kubejs_immersive_engineering` | `kubejs-immersive-engineering-1902.2.3-build.18.jar` |  |
| both | KubeJSDelight | `kubejsdelight` | `kubejsdelight-1.19.2-1.1.2.jar` |  |
| both | Lets Do Api | `doapi` | `doapi-1.1.0b.jar` | Required by: Vinery |
| both | Lightman's Currency | `lightmanscurrency` | `lightmanscurrency-1.19.2-2.2.1.3g.jar` |  |
| both | Little Contraptions | `littlecontraptions` | `littlecontraptions-forge-1.19.2.2.jar` |  |
| both | Little Logistics | `littlelogistics` | `littlelogistics-mc1.19.2-v1.4.0.jar` | Required by: Little Contraptions |
| both | LootJS | `lootjs` | `lootjs-forge-1.19.2-2.9.1.jar` |  |
| both | Lootr | `lootr` | `lootr-forge-1.19-0.4.29.76.jar` |  |
| both | Memory Leak Fix | `memoryleakfix` | `memoryleakfix-forge-1.17+-1.0.0.jar` |  |
| both | Metal Fences | `metalfences` | `MetalFences-FORGE-1.19.2-1.0.1.jar` |  |
| both | Minecraft Comes Alive | `mca` | `minecraft-comes-alive-7.5.20+1.19.2-universal.jar` |  |
| both | MmmMmmMmmmmm | `dummmmmmy` | `dummmmmmy-1.19.2-1.7.1.jar` |  |
| both | Momentum Protection | `momentumprotection` | `momentumprotection-1.19.2-0.0.1.jar` |  |
| both | Monobank | `monobank` | `monobank-1.19.2-1.1.3.jar` |  |
| both | Moog's End Structures | `mes` | `mes-1.3.1-1.19.2-forge.jar` |  |
| both | Moonlight Library | `moonlight` | `moonlight-1.19.2-2.3.7-forge.jar` | Required by: Every Compat, Heartstone, Immersive Weathering, MmmMmmMmmmmm, Moyai, Mystical Oak Tree (+2 more) |
| both | More Immersive Wires | `more_immersive_wires` | `more-immersive-wires-1.19.2-1.1.3.jar` |  |
| both | More Mob Variants | `moremobvariants` | `moremobvariants-forge+1.19.2-1.3.0.1.jar` |  |
| both | MoreJS | `morejs` | `morejs-forge-1.19.2-0.3.0.jar` |  |
| both | Mosaic Masonry | `mosaic_masonry` | `mosaic_masonry-1.1.jar` |  |
| both | Moyai | `moyai` | `moyai-1.19.2-2.0.1.jar` |  |
| both | Myrtrees | `myrtrees` | `myrtrees-forge-1.2.0-build.32.jar` |  |
| both | Mystical Oak Tree | `mysticaloaktree` | `mysticaloaktree-1.19.2-1.10.jar` |  |
| both | Name Tag Crafting Recipe | `mr_name_tagcraftingrecipemod` | `name-tag-crafting-recipe-mod-1.0 (3).jar` |  |
| both | Nature's Compass | `naturescompass` | `NaturesCompass-1.19.2-1.10.0-forge.jar` |  |
| both | Nether's Delight | `nethersdelight` | `NethersDelight-1.19-3.1.jar` |  |
| both | Netherite Horse Armor | `netherite_horse_armor` | `netherite_horse_armor-forge-1.19-1.0.4.jar` |  |
| both | NetherPortalFix | `netherportalfix` | `netherportalfix-forge-1.19-10.0.1.jar` |  |
| both | Nock Enough Arrows | `nock_enough_arrows` | `nock_enough_arrows-1.19.2-1.2.0.jar` |  |
| both | Not Enough Recipe Book | `nerb` | `nerb-1.19.2-0.3-FORGE.jar` |  |
| both | Nullscape | `nullscape` | `Nullscape_1.19.3_v1.2.2.jar` |  |
| both | Old Guns Mod | `oldguns` | `oldguns-1.19.2-3.7.3-47.jar` |  |
| both | OreExcavation | `oreexcavation` | `OreExcavation-1.11.166.jar` |  |
| both | Patchouli | `patchouli` | `Patchouli-1.19.2-77.jar` | Required by: Botania, Via Romana |
| both | Per-Dimension Game Rules | `pdgamerules` | `PerDimensionGameRules-1.19.2-forge-1.2.0.jar` |  |
| both | Philip's Ruins | `philipsruins` | `PhilipsRuins1.19.2-10.8.jar` |  |
| both | Pierce Arrow | `piercearrow` | `piercearrow-1.19.2-2.0.0.jar` |  |
| both | Placebo | `placebo` | `Placebo-1.19.2-7.4.1.jar` | Required by: Fast Workbench, FastFurnace |
| both | PneumaticCraft: Repressurized | `pneumaticcraft` | `pneumaticcraft-repressurized-1.19.2-4.3.15-52.jar` | Required by: Compressed Creativity |
| both | Pollen | `pollen` | `pollen-forge-2.1.0.jar` | Required by: Etched |
| both | Polymorph | `polymorph` | `polymorph-forge-0.46.6+1.19.2.jar` | Required by: Polymorphic Energistics |
| both | Polymorphic Energistics | `polyeng` | `polyeng-forge-0.1.1-1.19.2.jar` |  |
| both | Portable Mobs | `portablemobs` | `portablemobs-1.2.1-forge-mc1.19.jar` |  |
| both | PortableWardrobe | `portablewardrobe` | `portablewardrobearmorswap-1.0.2-forge-1.19.2.jar` |  |
| both | Potion Bundles | `potionbundles` | `potionbundles-1.19.1-1.6.jar` |  |
| both | Pretty Pipes | `prettypipes` | `PrettyPipes-1.13.6.jar` | Required by: Pretty Pipes: Fluids |
| both | Pretty Pipes: Fluids | `ppfluids` | `ppfluids-1.19.2-2.0.1.jar` |  |
| both | ProjectRed Core | `projectred_core` | `ProjectRed-1.19.2-4.19.0-beta+33-core.jar` | Required by: ProjectRed Fabrication, ProjectRed Illumination, ProjectRed Integration, ProjectRed Transmission |
| both | ProjectRed Fabrication | `projectred_fabrication` | `ProjectRed-1.19.2-4.19.0-beta+33-fabrication.jar` |  |
| both | ProjectRed Illumination | `projectred_illumination` | `ProjectRed-1.19.2-4.19.0-beta+33-illumination.jar` |  |
| both | ProjectRed Integration | `projectred_integration` | `ProjectRed-1.19.2-4.19.0-beta+33-integration.jar` | Required by: ProjectRed Fabrication |
| both | ProjectRed Transmission | `projectred_transmission` | `ProjectRed-1.19.2-4.19.0-beta+33-transmission.jar` | Required by: ProjectRed Fabrication |
| both | Puzzles Lib | `puzzleslib` | `PuzzlesLib-v4.4.3-1.19.2-Forge.jar` | Required by: Block Runner, Easy Magic |
| both | Quark | `quark` | `Quark-3.4-418.jar` | Required by: Quark Engineering |
| both | Quark Delight | `quarkdelight` | `quark_delight_1.0.0_forge_1.19.2.jar` |  |
| both | Quark Engineering | `quarkengineering` | `QuarkEngineering.jar` |  |
| both | Quark Oddities | `quarkoddities` | `QuarkOddities-1.18.jar` |  |
| both | Quests Additions | `questsadditions` | `questsadditions-1.19.2-1.4.2.jar` |  |
| both | Regrowth | `regrowth` | `Regrowth-1.19-41.1.31.1.jar` |  |
| both | Resourceful Lib | `resourcefullib` | `resourcefullib-forge-1.19.2-1.1.24.jar` | Required by: Handcrafted |
| both | Responsive Shields | `responsiveshields` | `responsiveshields-2.3-mc1.18-19-20.x.jar` |  |
| both | Rhino | `rhino` | `rhino-forge-1902.2.3-build.284.jar` | Required by: KubeJS |
| both | Rustic Delight | `rusticdelight` | `rusticdelight-forge-1.19.2-1.3.0.jar` |  |
| both | Scholar | `scholar` | `scholar-1.19.2-1.0.0-forge.jar` |  |
| both | Sculk Transporting | `sculktransporting` | `sculktransporting-1.19.2-1.1.jar` |  |
| both | SeasonHud | `seasonhud` | `seasonhud-forge-1.19.2-2.0.4.jar` | Registers a SERVER config the server syncs to players - must be on the server |
| both | Sebastrn Lib | `sebastrnlib` | `sebastrnlib-2.0.2.jar` |  |
| both | Second Chance Forge | `secondchanceforge` | `secondchanceforge-1.19-1.5.1.jar` |  |
| both | SecurityCraft | `securitycraft` | `[1.19.2] SecurityCraft v1.9.6.1.jar` |  |
| both | Separated Leaves | `separatedleaves` | `separatedleaves-2.4.0-forge-1.19.2.jar` |  |
| both | Serene Seasons | `sereneseasons` | `SereneSeasons-1.19.2-8.1.0.24.jar` |  |
| both | SimpleHats | `simplehats` | `simplehats-forge-1.19.2-0.3.2.jar` |  |
| both | Sit | `sit` | `sit-1.19-1.3.3.jar` |  |
| both | Smaller Nether Portals | `smallernetherportals` | `smallernetherportals-1.19.2-3.6.jar` |  |
| both | Sooty Chimneys | `sootychimneys` | `sootychimneys-1.19.2-1.2.1.jar` |  |
| both | Sophisticated Backpacks | `sophisticatedbackpacks` | `sophisticatedbackpacks-1.19.2-3.20.2.1035.jar` |  |
| both | Sophisticated Core | `sophisticatedcore` | `sophisticatedcore-1.19.2-0.6.4.730.jar` | Required by: Sophisticated Backpacks |
| both | Spice of Life: Apple Pie Edition | `solapplepie` | `solapplepie-1.19.2-2.2.0.jar` |  |
| both | Stardew Fishing | `stardew_fishing` | `stardew_fishing-1.19.2-1.3-all.jar` |  |
| both | Storage Delight | `storagedelight` | `storagedelight-25.12.09-1.19-forge.jar` |  |
| both | Storage Drawers | `storagedrawers` | `StorageDrawers-1.19.2-11.4.1.jar` |  |
| both | SuperMartijn642's Config Library | `supermartijn642configlib` | `supermartijn642configlib-1.1.8-forge-mc1.19.jar` | Required by: Durability Tooltip, Trash Cans |
| both | SuperMartijn642's Core Lib | `supermartijn642corelib` | `supermartijn642corelib-1.1.21-forge-mc1.19.2.jar` | Required by: Portable Mobs, Trash Cans |
| both | Supplementaries | `supplementaries` | `supplementaries-1.19.2-2.4.20.jar` | Required by: Supplementaries Squared |
| both | Supplementaries Squared | `suppsquared` | `suppsquared-1.19.2-1.1.1.jar` |  |
| both | Survivality | `survivality` | `survivality-1.19.2-1.2.4-forge.jar` |  |
| both | SwingThroughGrass | `swingthroughgrass` | `swingthroughgrass-1.19.2-1.10.0.jar` |  |
| both | Tactical Fishing | `tactical_fishing` | `TacticalFishing-1.19.x2-v1.2.jar` |  |
| both | Tectonic | `tectonic` | `tectonic-forge-1.19-2.3.5a.jar` |  |
| both | Terralith | `terralith` | `Terralith_1.19.3_v2.3.12.jar` |  |
| both | Towns and Towers | `t_and_t` | `Towns-and-Towers-v.1.10-_FORGE-1.19.2_.jar` |  |
| both | Trade Cycling | `trade_cycling` | `trade-cycling-forge-1.19.2-1.0.15.jar` |  |
| both | Trash Cans | `trashcans` | `trashcans-1.0.18-forge-mc1.19.jar` |  |
| both | TrashSlot | `trashslot` | `trashslot-forge-1.19.2-12.1.0.jar` |  |
| both | Twigs | `twigs` | `twigs-forge-1.19.2-3.0.1.jar` |  |
| both | Via Romana | `via_romana` | `Via_Romana-1.19.2-1.1.3.jar` |  |
| both | Villagers Sell Animals (by NoCube) | `villagersellanimals` | `nocube's_villagers_sell_animals_1.2.1_forge_1.19.2.jar` |  |
| both | Vinery | `vinery` | `letsdo-vinery-forge-1.3.10.jar` |  |
| both | Vintage KubeJS | `vintage_kubejs` | `vintage_kubejs-1.19.2-1.0.0rc-2.jar` |  |
| both | Wardrobe | `wardrobe` | `wardrobe-1.0.3-1.19.2.jar` |  |
| both | Wares | `wares` | `wares-1.19.2-1.2.9.jar` |  |
| both | Waystones | `waystones` | `waystones-forge-1.19.2-11.4.2.jar` | Required by: Waystones Teleport Pets, Waystones2Waypoints2 |
| both | Waystones Teleport Pets | `w2pets` | `WaystonesTeleportPets-1.19.2-1.19.4--1.2.jar` |  |
| both | Waystones2Waypoints2 | `w2w2` | `xaeros_waystones_compatibility-1.1 - 1.19.2.jar` | Xaero's waypoints for waystones. Server sends the waystone-activated packet on a strict `w2w2:main` channel, so a client that has it can't join a server that doesn't; client code is DistExecutor-guarded |
| both | WorldEdit | `worldedit` | `worldedit-mod-7.2.12.jar` |  |
| both | wthit | `wthit` | `wthit-1.19.2-forge-5.36.1.jar` | Server supplies the looking-at data |
| both | YUNG's API | `yungsapi` | `YungsApi-1.19.2-Forge-3.8.10.jar` | Required by: YUNG's Better Desert Temples, YUNG's Better Dungeons, YUNG's Better End Island, YUNG's Better Jungle Temples, YUNG's Better Nether Fortresses, YUNG's Better Strongholds (+2 more) |
| both | YUNG's Better Desert Temples | `betterdeserttemples` | `YungsBetterDesertTemples-1.19.2-Forge-2.2.2.jar` |  |
| both | YUNG's Better Dungeons | `betterdungeons` | `YungsBetterDungeons-1.19.2-Forge-3.2.2.jar` |  |
| both | YUNG's Better End Island | `betterendisland` | `YungsBetterEndIsland-1.19.2-Forge-1.0.jar` |  |
| both | YUNG's Better Jungle Temples | `betterjungletemples` | `YungsBetterJungleTemples-1.19.2-Forge-1.0.1.jar` |  |
| both | YUNG's Better Nether Fortresses | `betterfortresses` | `YungsBetterNetherFortresses-1.19.2-Forge-1.0.6.jar` |  |
| both | YUNG's Better Strongholds | `betterstrongholds` | `YungsBetterStrongholds-1.19.2-Forge-3.2.0.jar` |  |
| both | YUNG's Better Witch Huts | `betterwitchhuts` | `YungsBetterWitchHuts-1.19.2-Forge-2.1.0.jar` |  |
| both | YUNG's Extras | `yungsextras` | `YungsExtras-1.19.2-Forge-3.1.0.jar` |  |
| both | Yuushya Modelling | `yuushya_modelling` | `yuushya-modelling-1.19.2-forge-2.2.0.jar` |  |
| server | Async Locator | `asynclocator` | `async-locator-forge-1.19.2-1.3.0.jar` | Off-thread /locate, explorer maps, dolphins, eyes of ender (IGNORE_SERVER_VERSION) |
| server | Block Runner | `blockrunner` | `BlockRunner-v4.2.2-1.19.2-Forge.jar` | Server-side walk-speed logic |
| server | CherishedItems | `cherisheditems` | `CherishedItems-Forge-1.19.2-1.0.2.jar` | Item despawn rules |
| server | Chunky | `chunky` | `Chunky-1.3.38.jar` | Chunk pre-generation |
| server | Create Liquid Fuel | `createliquidfuel` | `createliquidfuel-2.1.1-1.19.2.jar` | Server-side fuel recipes |
| server | DeathKnell | `deathknell` | `DeathKnell-Forge-1.19.2-6.0.3.jar` | Death messages |
| server | Lightmans Discord Integration | `lightmansdiscord` | `lightmansdiscord-1.19.2-0.2.0.3-server.jar` | Discord bot (needs a token) |
| server | Loading Protection | `loadingprotection` | `loadingprotection-1.0.2-forge-1.19.2.jar` | Join invulnerability |
| server | Simple Backups | `simplebackups` | `SimpleBackups-1.19.1-2.1.12.jar` | World backups |
| server | Tax Free Levels | `taxfreelevels` | `TaxFreeLevels-1.4.23-forge-1.20.1.jar` | Server-side enchanting cost |
| client | Better Third Person | `betterthirdperson` | `BetterThirdPerson-Forge-1.19-1.9.0.jar` | Camera |
| client | BetterF3 | `betterf3` | `BetterF3-4.0.1-Forge-1.19.2.jar` | F3 screen |
| client | Cherished Worlds | `cherishedworlds` | `cherishedworlds-forge-6.0.5+1.19.4.jar` | World-list favourites |
| client | Clear Void | `clearvoid` | `clearvoid-forge-1.3.0.jar` | Removes void fog/skybox band |
| client | ConnectedTexturesMod | `ctm` | `CTM-1.19.2-1.1.6+8.jar` | Connected textures (resource-pack rendering) |
| client | Controlling | `controlling` | `Controlling-forge-1.19.2-10.0+7.jar` | Keybind menu search |
| client | Create Questing | `create_questing` | `Create_Questing-FORGE-1.19.2-1.0.0.jar` | Create-styled FTB Quests GUI |
| client | Detail Armor Bar | `detailab` | `DetailArmorBar-2.6.4+1.19.2-forge.jar` | Armor bar HUD (creativeengineering compat code is client-guarded) |
| client | Durability Tooltip | `durabilitytooltip` | `durabilitytooltip-1.1.6a-forge-mc1.19.jar` | Tooltips |
| client | Effective (Forge) | `effective_fg` | `effective_fg-1.3.4.jar` | Visual/ambient effects |
| client | EnchantmentDescriptions | `enchdesc` | `EnchantmentDescriptions-Forge-1.19.2-13.0.20.jar` | Enchantment tooltips |
| client | EntityCulling | `entityculling` | `entityculling-forge-1.6.1-mc1.19.2.jar` | Rendering optimisation |
| client | Highlighter | `highlighter` | `Highlighter-1.19.1-1.1.4.jar` | New-item highlight |
| client | Iceberg | `iceberg` | `Iceberg-1.19.2-forge-1.1.4.jar` | Library for Legendary Tooltips / Highlighter |
| client | ImmediatelyFast | `immediatelyfast` | `ImmediatelyFast-Forge-1.4.0+1.19.2.jar` | Rendering optimisation |
| client | Just Enough Breeding | `justenoughbreeding` | `justenoughbreeding-forge-1.19.2-1.4.0.jar` | JEI add-on |
| client | Just Enough Effect Descriptions | `jeed` | `jeed-1.19.2-2.1.4.jar` | JEI add-on |
| client | Just Enough Immersive Multiblocks | `jeimultiblocks` | `jeimultiblocks-1.19.2-0.0.3.jar` | JEI add-on |
| both | Just Enough Items | `jei` | `jei-1.19.2-forge-11.8.1.1034.jar` | Recipe viewer; required on the server |
| client | Just Enough Professions (JEP) | `justenoughprofessions` | `JustEnoughProfessions-forge-1.19.2-2.0.2.jar` | JEI add-on |
| client | Just Enough Repair Materials | `jerm` | `jerm-1.19.2-1.2.jar` | JEI add-on |
| client | Just Enough Resources | `jeresources` | `JustEnoughResources-1.19.2-1.2.3.243.jar` | JEI add-on |
| client | Just Enough Sculk Sensor | `justenoughsculksensor` | `jess-1.19.2-forge-1.0.0.jar` | JEI add-on |
| client | JustEnoughBeacons | `just_enough_beacons` | `JustEnoughBeacons-Forge-1.19+-1.1.2.jar` | JEI add-on |
| client | Konkrete | `konkrete` | `konkrete_forge_1.8.0_MC_1.19-1.19.2.jar` | Menu library; no server mod needs it |
| client | Legendary Tooltips | `legendarytooltips` | `LegendaryTooltips-1.19.2-forge-1.4.0.jar` | Tooltips |
| client | Make Bubbles Pop | `make_bubbles_pop` | `make_bubbles_pop-0.3.0-forge-mc1.19-1.19.3.jar` | Bubble particles; loads net.minecraft.client on a dedicated server (FML crash, found 2026-09-27) |
| client | Mod Name Tooltip | `modnametooltip` | `modnametooltip-1.20.1-1.20.0.jar` | Tooltips |
| client | Mouse Tweaks | `mousetweaks` | `MouseTweaks-forge-mc1.19-2.23.jar` | Inventory mouse handling |
| client | NotEnoughAnimations | `notenoughanimations` | `notenoughanimations-forge-1.12.2-mc1.19.2.jar` | Player animations |
| client | Panorama | `panorama` | `panorama-1.3.0 - 1.19.2.jar` | Title-screen panoramas (also skip the mods/panorama folder) |
| client | PonderJS | `ponderjs` | `ponderjs-1.19.2-1.2.0.jar` | KubeJS Ponder scenes (client) |
| client | Presence Footsteps (Forge) | `presencefootsteps` | `PresenceFootsteps-1.19.2-1.6.4.1-forge.jar` | Footstep sounds |
| client | Prism | `prism` | `Prism-1.19.1-1.0.2.jar` | Library for Legendary Tooltips |
| client | Rubidium | `rubidium` | `rubidium-mc1.19.2-0.6.2c.jar` | Renderer (Sodium port) |
| client | Rubidium Extra | `rubidium_extra` | `rubidium-extra-0.4.19+mc1.19.2-build.105.jar` | Rubidium add-on |
| client | Spyglass Improvements | `spyglass_improvements` | `spyglass_improvements-forge-1.5.12+mc1.19+forge.jar` | Spyglass zoom |
| client | WTHIT Harvestability | `wthitharvestability` | `wthitharvestability-mc1.19.2-2.1.0.jar` | WTHIT add-on (client display only) |
| client | Xaero's Minimap | `xaerominimap` | `xaerominimap-forge-1.19.2-25.3.10.jar` | Minimap (optional on server; not used there) |
| client | Xaero's World Map | `xaeroworldmap` | `xaeroworldmap-forge-1.19.2-1.40.11.jar` | World map (optional on server; not used there) |
| client | XaeroZoomout | `xaerozoomout` | `XaeroZoomout-Forge-1.19-1.1.0.jar` | Map zoom |
| both | Blocky Siege | `blocky_siege` | `blocky_siege-6.1.91-1.19.2.jar` | **Structure mod with more than structures:** siege weapons (GeckoLib entities), blocks, items, recipes. Its one structure (`blocky_siege:pillager_ship`) is in group/boats. Requires GeckoLib |
| both | Villages & Pillages | `villagesandpillages` | `villagesandpillages-forge-mc1.19.2-1.0.2.jar` | **Structure mod with more than structures:** its witch village uses a custom structure type, 6 structure processors and 3 mixins (fewer trees in the village), so it can't become a datapack. Requires YUNG's API |
| both | Revamped Shipwrecks | `revamped_shipwrecks` | `Revamped Shipwrecks 1.1.0.jar` | Structures only (lowcode datapack jar, loads fine). Empties `minecraft:shipwrecks` and replaces the vanilla wrecks with its own |
| both | Unwrecked Ships Plus | `unwrecked_ships_plus` | `Unwrecked Ships Plus 1.19-1.0.4 Forge.jar` | Structures only (lowcode datapack jar, loads fine) |
| both | Redeco | `redeco` | `redeco-1.5.0-1.19.2.jar` | Decoration blocks; no structures |
| both | Glow Ink Plus | `glow_ink_plus` | `(Forge)GlowInkPlus-1.19.2-1.2.0.jar` | Glowing wool/carpet blocks |
| both | Abnormals Delight | `abnormals_delight` | `abnormals_delight-1.19.2-4.1.2.jar` | Blocks/items; requires Blueprint, Farmer's Delight |
| both | Alex's Abnormalities | `alexsabnormalities` | `alexsabnormalities-1.3.jar` | Alex's Mobs x Abnormals compat blocks/items |
| both | AstikorCarts | `astikorcarts` | `astikorcarts-1.19.2-1.1.2.jar` | Cart entities |
| both | Berry Good | `berry_good` | `berry_good-1.19.2-6.1.0.jar` | Blocks/items |
| client | Boat Item View | `boatiview` | `Boat-Item-View-Forge-1.19.x-0.0.4.jar` | Held-item rendering in boats |
| both | Boatload | `boatload` | `boatload-1.19.2-4.2.2.jar` | Boat entities |
| client | BoatView360 | `boatview360` | `boatview360-v1.0.4-mc1.17-1.20.1-forge.jar` | Boat camera |
| both | Breezy | `breezy` | `breezy-1.19.2-1.0.8.jar` | Hot air balloon entities |
| both | Canned Goods | `canned_goods` | `CannedGoods-1.19.2-3.jar` | Blocks/items |
| both | Colored Water | `colored_water` | `Colored Water-1.19.2-1.3.7.jar` | Blocks/items |
| server | Create: Fast Schematic Cannon | `createfastschematiccannon` | `CreateFastSchematicCannon-2.6.1-0.5.1i-forge-1.19.2.jar` | Schematicannon block-entity speed mixins |
| both | Floral Enchantment | `floralench` | `Floral Enchantment-1.19.2-1.4.3.jar` | Blocks/items |
| server | GlowingRaidIllagers | `glowingraidillagers` | `glowingraidillagers-1.19.2-1.0.0.jar` | Raid mixin applies vanilla Glowing |
| both | Golden Food | `goldenfood` | `goldenfood-1.19.2-2.0.0.jar` | Items |
| both | Greek Villager House | `greek_villager_house` | `greek_villager_house-1.0.0-forge-1.19.2.jar` | Structures only (data-only jar, loads fine; not ported) |
| both | hobbit house | `hobbit_house_` | `hobbit_house_-1.0.1-forge-1.19.2.jar` | Structures only (data-only jar, loads fine; not ported) |
| both | Hopo Better Underwater Ruins | `hopour` | `HopoBetterUnderwaterRuins-[1.19-1.19.3]-1.1.1.jar` | Structures only |
| both | Ice Boat Nerf | `iceboatnerf` | `iceboatnerf-1.1.1.jar` | Boat mixin; boat physics run on the driving client |
| server | Ice Prevents Crop Growth | `icepreventscropgrowth` | `icepreventscropgrowth-1.19.2-3.2.jar` | Crop-growth event |
| both | Neapolitan | `neapolitan` | `neapolitan-1.19.2-4.1.0.jar` | Blocks/items/entities |
| both | old swamp fisherman house | `old_swamp_fisherman_house` | `old_swamp_fisherman_house-1.0.2-forge-1.19.2.jar` | Structures only (data-only jar, loads fine; not ported) |
| both | Rare Ice | `rare_ice` | `rare-ice-0.5.1.jar` | Blocks |
| both | Salt | `salt` | `salt-1.19.2-1.2.4.1.jar` | Blocks/items |
| both | SawmillHouse | `sawmillhouse` | `sawmillhouse-1.3.0-forge-1.19.2.jar` | Structures only (data-only jar, loads fine; not ported) |
| both | Shulker Box Slot | `shulkerboxslot` | `shulkerboxslot-forge-5.0.0-beta.2+1.19.2.jar` | Accessory slot |
| both | Simply Houses | `simply_houses` | `SimplyHouses-1.1.3-1.19-forge.jar` | Structures only (data-only jar, loads fine; not ported) |
| both | Starter Kit | `starterkit` | `starterkit-1.19.2-7.0.jar` | Kit choice screen |
| both | It Takes A Pillage | `takesapillage` | `takesapillage-1.0.3-1.19.2.jar` | Entities + structures |
| both | The Dirty Stuff | `the_dirty_stuff` | `the_dirty_stuff-2.1-forge-1.19.2.jar` | Blocks/items |
| both | Tinted Campfires | `tintedcampfires` | `Tinted Campfires-1.19.2-1.2.9.jar` | Blocks |
| both | Upgrade Aquatic | `upgrade_aquatic` | `upgrade_aquatic-1.19.2-5.1.0.jar` | Blocks/items/entities |
| both | XP Tome | `xpbook` | `xptome-1.19-2.1.7.jar` | Items |
| both | Bushier Flowers | `bushierflowers` | `bushierflowers-0.0.2-1.19.2.jar` | Flower bush blocks |
| both | Fuel Goes Here | `fuelgoeshere` | `fuelgoeshere-1.19.2-0.1.0.0.jar` | Furnace shift-click puts fuel in the fuel slot; ships recipes/tags. Added 2026-09-30 |
| both | Untitled Duck Mod | `untitledduckmod` | `untitledduckmod-0.6.1-1.19.2-forge.jar` | Duck/goose entities + items. Needs GeckoLib, Architectury. Added 2026-09-30 |
| both | Farmer's Respite | `farmersrespite` | `FarmersRespite-1.19-2.0.jar` | Tea/coffee crops, kettle, drinks. Needs Farmer's Delight. Added 2026-10-01 |
| both | Corn Delight | `corn_delight` | `corn_delight-1.0.3-1.19.2.jar` | Corn crop + foods. Needs Farmer's Delight, Mysterious Mountain Lib. Added 2026-10-01 |
| both | More Delight | `moredelight` | `moredelight-25.07.28-1.19-forge.jar` | Extra Farmer's Delight foods/tools. Added 2026-10-01 |
| both | Mysterious Mountain Lib | `mysterious_mountain_lib` | `mysterious_mountain_lib-1.2.3-1.19.2.jar` | Library. Required by: Corn Delight. Added 2026-10-01 |
| both | Blue Flame Burning | `blueflame` | `blueflame-1.19.2-0.1.0.2.jar` | Soul-fire burning state synced to clients (blue flame overlay). Added 2026-10-02 |
| both | Fruits Delight | `fruitsdelight` | `fruitsdelight-0.5.11.jar` | Fruit trees/bushes, jams, jellies, foods; biome modifiers. Needs Farmer's Delight. Added 2026-10-02 |
| both | ImprovedPillagerOutpost | `ipo` | `ImprovedPillagerOutpost-v5.jar` | Structures only (data-only jar: replaces vanilla pillager_outpost template pools) Added 2026-10-02 |
| both | Well Mod | `well` | `well-1.19.2-1.0.0.jar` | Well blocks (16 dyed) that pump water. Added 2026-10-02 |
