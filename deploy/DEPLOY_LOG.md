# Deploy log

Every version staged by `tools/build_deploy.py`, newest first. Each entry lists what changed since the
version before it and which files the other machines must delete. The full file list of a version is in
`history/<version>.json`.

<!-- deploy:1.1.01 -->
## 1.1.01 - 2026-10-22 21:46

- Minecraft 1.19.2, Forge 43.3.9
- Commit: a0aeb14 + 105 uncommitted file(s)
- Since: 1.0.9
- Deployed: server, client 2026-10-22 22:39
- Contents: 365 server+client mods, 45 client-only mods, 30 resource pack files, 16 shader pack files
- Payload: update from 1.0.9, config functional: 245 files, 11.2 MB (mods 8, kubejs 208, config 16, defaultconfigs 5, world_serverconfig 8)
- Notes: 1.1.01: big changes, changelog implementation
- Built with warnings:
  - 105 uncommitted change(s) in shipped folders (commit a0aeb14 + working tree)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 7: bullet is 484 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 13: bullet is 306 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 38: bullet is 404 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 58: bullet is 425 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 83: bullet is 437 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 104: bullet is 358 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 117: bullet is 497 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 131: bullet is 593 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 141: bullet is 453 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 146: bullet is 491 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 154: bullet is 386 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 172: bullet is 427 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 206: bullet is 325 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 220: bullet is 360 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 239: bullet is 458 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 245: bullet is 331 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 248: bullet is 484 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 253: bullet is 412 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 265: bullet is 316 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 271: bullet is 320 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 366: bullet is 362 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 393: bullet is 447 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 407: bullet is 726 characters (over 300: split it or use a sub-bullet)
  - changelog lint, kubejs/assets/creativeengineering/changelog/unreleased.md: line 424: bullet is 483 characters (over 300: split it or use a sub-bullet)

### Changes

Mods added (4):

- baby_mobs `babyMobs-1.0.0.jar` (both)
- Carry cats! 2.5+mc1.19.2-forge `Carry Cats 2.5 1.19.2.jar` (both)
- creative_labels `creativeLabels-1.0.0.jar` (both)
- veggiesdelight `backportedVeggies-1.9.2-ce-19.2-01.jar` (both)

Mods updated (4):

- brewinandchewin: `backportedBrewin-4.5.0-ce-19.2-02.jar` -> `backportedBrewin-4.5.0-ce-19.2-03.jar`
- creativeengineering: `creativeengineering-1.0.9.jar` -> `creativeengineering-1.1.01.jar`
- more_mooblooms: `moreMooblooms-1.0.0.jar` -> `moreMooblooms-1.1.0.jar`
- vinery: `backportedVinery-1.5.5-ce-19.2-02.jar` -> `backportedVinery-1.5.5-ce-19.2-04.jar`

Mods removed (12):

- AE2 QoL Recipes `ae2qolrecipes-forge-1.18-1.20.1-1.3.0.jar` (was both)
- Bottled Air 1.19.2-2.3-fabric+forge+neo `bottledair-1.19.2-2.3.jar` (was both)
- Comfortable Campfires 1.1.2+1.19.2 `comfortable-campfires-1.1.2+1.19.2.jar` (was both)
- Copper Drops 1.1 - 1.19-1.21+ `CopperDrops-1.1.jar` (was both)
- Create Waystone Recipes 1.19 / 1.20 `Create_Waystone_Recipes_1.19_v2.jar` (was both)
- Create: Copper Splashing `Create_Copper_Splashing_v2.jar` (was both)
- Create: Easy Stone Generators 1.0.0 `create_generators-1.0.0-forge-1.19.2.jar` (was both)
- Fast Leaf Decay `FastLeafDecay-30.jar` (was both)
- Furnace Recycle 1.19.2-2.2-fabric+forge+neo `furnacerecycle-1.19.2-2.2.jar` (was both)
- Glowing Illagers 1.0.0 `glowingraidillagers-1.19.2-1.0.0.jar` (was both)
- Nametag Crafting 1.0+mod `name-tag-crafting-recipe-mod-1.0 (3).jar` (was both)
- Starter Kit 1.19.2-7.0-fabric+forge `starterkit-1.19.2-7.0.jar` (was both)

<details><summary>kubejs/: 148 added, 60 changed, 1 removed</summary>

*added (148)*

- `assets/cookingforblockheads/models/block/fridge_large_door_lower.json`
- `assets/cookingforblockheads/models/block/fridge_large_door_lower_flipped.json`
- `assets/cookingforblockheads/models/block/fridge_large_door_upper.json`
- `assets/cookingforblockheads/models/block/fridge_large_door_upper_flipped.json`
- `assets/cookingforblockheads/models/block/fridge_large_lower.json`
- `assets/cookingforblockheads/models/block/fridge_large_upper.json`
- `assets/creativeengineering/brewinandchewin/coaster/` (56 files)
- `assets/creativeengineering/changelog/1.1.01.md`
- `assets/creativeengineering/jei_world/block_growth/generated.json`
- `assets/creativeengineering/jei_world/block_growth/manual.json`
- `assets/creativeengineering/jei_world/budding/generated.json`
- `assets/creativeengineering/jei_world/concrete_hardening/manual.json`
- `assets/creativeengineering/jei_world/dyeing/generated.json`
- `assets/creativeengineering/jei_world/dyeing/manual.json`
- `assets/creativeengineering/jei_world/grass_drops/generated.json`
- `assets/creativeengineering/jei_world/jar_interaction/generated.json`
- `assets/creativeengineering/jei_world/jar_interaction/manual.json`
- `assets/creativeengineering/jei_world/jar_storage/generated.json`
- `assets/creativeengineering/jei_world/jar_storage/manual.json`
- `assets/creativeengineering/jei_world/knife_drops/generated.json`
- `assets/creativeengineering/jei_world/lava_generation/generated.json`
- `assets/creativeengineering/jei_world/lava_generation/manual.json`
- `assets/creativeengineering/jei_world/lightning/generated.json`
- `assets/creativeengineering/jei_world/lightning/manual.json`
- `assets/creativeengineering/jei_world/mob_bottling/manual.json`
- `assets/creativeengineering/jei_world/mob_transformation/manual.json`
- `assets/creativeengineering/jei_world/salt/manual.json`
- `assets/creativeengineering/jei_world/soaping/generated.json`
- `assets/creativeengineering/jei_world/soaping/manual.json`
- `assets/creativeengineering/jei_world/stripping/generated.json`
- `assets/creativeengineering/jei_world/waxing/generated.json`
- `assets/creativeengineering/jei_world/waxing/manual.json`
- `assets/creativeengineering/textures/item/mortar_and_pestle.png`
- `client_scripts/name_fixes.js`
- `data/creativeengineering/moonlight/soft_fluids/` (29 files)
- `data/minecraft/loot_tables/blocks/copper_ore.json`
- `data/minecraft/loot_tables/blocks/deepslate_copper_ore.json`
- `data/rusticdelight/worldgen/configured_feature/wild_bell_peppers_feature.json`
- `data/rusticdelight/worldgen/configured_feature/wild_coffee_feature.json`
- `data/rusticdelight/worldgen/configured_feature/wild_cotton_feature.json`
- `data/salt/loot_tables/cauldron_evaporation/salt_full.json`
- `data/upgrade_aquatic/worldgen/configured_feature/river_tree.json`
- `data/upgrade_aquatic/worldgen/placed_feature/trees_river.json`
- `generation_scripts/food/mortar_catalog.json`
- `generation_scripts/food/placed_food_slices.json`
- `generation_scripts/names/name_fixes.json`
- `server_scripts/cooking/milk_jar.js`
- `server_scripts/cooking/mortar.js`
- `server_scripts/cooking/salt.js`
- `server_scripts/loot/09_crop_drops.js`
- `server_scripts/other/backpack_skins.js`
- `server_scripts/other/creative_labels.js`
- `server_scripts/other/feature_ports.js`
- `server_scripts/other/item_merges.js`
- `server_scripts/other/jeed_providers.js`
- `server_scripts/other/starter_kit.js`
- `server_scripts/tags/feature_port_tags.js`
- `server_scripts/tags/jartags.js`
- `server_scripts/wood/removed_woods.js`
- `startup_scripts/_lib/item_merges.js`
- `startup_scripts/_lib/removed_woods.js`
- `startup_scripts/cooking/alcohol_drinks.js`
- `startup_scripts/cooking/baking_ingredients.js`
- `startup_scripts/cooking/fluids.js`
- `startup_scripts/cooking/food_values.js`

*changed (60)*

- `assets/creativeengineering/lang/en_us.json`
- `assets/displaydelight/lang/en_us.json`
- `client_scripts/jei_order.js`
- `client_scripts/jeichanges.js`
- `client_scripts/namechanges.js`
- `client_scripts/tooltips.js`
- `generation_scripts/food/can_servings.json`
- `generation_scripts/food/can_types.json`
- `generation_scripts/food/food_types.json`
- `generation_scripts/food/placeable.json`
- `generation_scripts/stacks/stack_sizes.json`
- `generation_scripts/worldgen/flower_features.json`
- `generation_scripts/worldgen/pool_config.json`
- `generation_scripts/worldgen/pool_tags.json`
- `generation_scripts/worldgen/wild_crops.json`
- `generation_scripts/worldgen/wild_flowers.json`
- `generation_scripts/worldgen/climate/minecraft__overworld.caves.json.gz`
- `generation_scripts/worldgen/climate/minecraft__overworld.json.gz`
- `generation_scripts/worldgen/climate/minecraft__the_end.json.gz`
- `generation_scripts/worldgen/climate/minecraft__the_nether.json.gz`
- `server_scripts/_lib/dump_all.js`
- `server_scripts/_lib/dump_gear.js`
- `server_scripts/_lib/recipe_scan.js`
- `server_scripts/cooking/alcohol.js`
- `server_scripts/cooking/baking.js`
- `server_scripts/cooking/canning.js`
- `server_scripts/cooking/cooking.js`
- `server_scripts/cooking/golden.js`
- `server_scripts/economy/trades.js`
- `server_scripts/economy/wandering_trades.js`
- `server_scripts/engineering/resource_blocks.js`
- `server_scripts/engineering/sample_syringe.js`
- `server_scripts/equipment/archery.js`
- `server_scripts/equipment/bullets.js`
- `server_scripts/loot/07_rules.js`
- `server_scripts/loot/08_leaf_apples.js`
- `server_scripts/magic/tool_effects.js`
- `server_scripts/other/misc_mods.js`
- `server_scripts/tags/blocktags.js`
- `server_scripts/tags/cookingtags.js`
- `server_scripts/tags/equipmenttags.js`
- `server_scripts/tags/fluidtags.js`
- `server_scripts/tags/item_physics_tags.js`
- `server_scripts/tags/placeabletags.js`
- `server_scripts/worldgen/wild_flowers.js`
- `startup_scripts/_lib/block_dict.js`
- `startup_scripts/_lib/fluid_dict.js`
- `startup_scripts/_lib/item_dict.js`
- `startup_scripts/_lib/removed_items.js`
- `startup_scripts/changes/transition_items.js`
- `startup_scripts/compost/compost_values.js`
- `startup_scripts/cooking/cornmeal.js`
- `startup_scripts/func/armor_effects.js`
- `startup_scripts/func/item_physics.js`
- `startup_scripts/func/neoCraftHelper.js`
- `startup_scripts/func/sample_syringe.js`
- `startup_scripts/func/tool_effects.js`
- `startup_scripts/items/cans.js`
- `startup_scripts/items/misc_items.js`
- `startup_scripts/magic/magic_resistance.js`

*removed (1)*

- `startup_scripts/func/config.js`

</details>

<details><summary>config/: 3 added, 13 changed, 13 removed</summary>

*added (3)*

- `baby_mobs-common.toml`
- `creative_labels-common.toml`
- `shouldercats.json`

*changed (13)*

- `/` (10 files)
- `jei/ingredient-list-mod-sort-order.ini`
- `jei/recipe-category-sort-order.ini`
- `worldedit/worldedit.properties`

*removed (13)*

- `bottledair.json5`
- `comfortable_campfires.json`
- `starterkit.json5`
- `starterkit/descriptions/Archer.txt`
- `starterkit/descriptions/Default.txt`
- `starterkit/descriptions/Engineer.txt`
- `starterkit/descriptions/Lumberjack.txt`
- `starterkit/descriptions/Witch.txt`
- `starterkit/kits/Default.txt`
- `starterkit/kits/Engineer.txt`
- `starterkit/kits/inactive/Archer.txt`
- `starterkit/kits/inactive/Lumberjack.txt`
- `starterkit/kits/inactive/Witch.txt`

</details>

defaultconfigs/: 4 added, 1 changed, 0 removed:

*added (4)*

- `create-server.toml`
- `solapplepie-server.toml`
- `sophisticatedbackpacks-server.toml`
- `stardew_fishing-server.toml`

*changed (1)*

- `unlitcampfire-server.toml`

### Removals (from 1.0.9)

Delete on the server (30, also in removals_server.txt):

- `config/bottledair.json5`
- `config/comfortable_campfires.json`
- `config/starterkit.json5`
- `config/starterkit/descriptions/Archer.txt`
- `config/starterkit/descriptions/Default.txt`
- `config/starterkit/descriptions/Engineer.txt`
- `config/starterkit/descriptions/Lumberjack.txt`
- `config/starterkit/descriptions/Witch.txt`
- `config/starterkit/kits/Default.txt`
- `config/starterkit/kits/Engineer.txt`
- `config/starterkit/kits/inactive/Archer.txt`
- `config/starterkit/kits/inactive/Lumberjack.txt`
- `config/starterkit/kits/inactive/Witch.txt`
- `kubejs/startup_scripts/func/config.js`
- `mods/CopperDrops-1.1.jar`
- `mods/Create_Copper_Splashing_v2.jar`
- `mods/Create_Waystone_Recipes_1.19_v2.jar`
- `mods/FastLeafDecay-30.jar`
- `mods/ae2qolrecipes-forge-1.18-1.20.1-1.3.0.jar`
- `mods/backportedBrewin-4.5.0-ce-19.2-02.jar`
- `mods/backportedVinery-1.5.5-ce-19.2-02.jar`
- `mods/bottledair-1.19.2-2.3.jar`
- `mods/comfortable-campfires-1.1.2+1.19.2.jar`
- `mods/create_generators-1.0.0-forge-1.19.2.jar`
- `mods/creativeengineering-1.0.9.jar`
- `mods/furnacerecycle-1.19.2-2.2.jar`
- `mods/glowingraidillagers-1.19.2-1.0.0.jar`
- `mods/moreMooblooms-1.0.0.jar`
- `mods/name-tag-crafting-recipe-mod-1.0 (3).jar`
- `mods/starterkit-1.19.2-7.0.jar`

Delete on the client (30, also in removals_client.txt):

- `config/bottledair.json5`
- `config/comfortable_campfires.json`
- `config/starterkit.json5`
- `config/starterkit/descriptions/Archer.txt`
- `config/starterkit/descriptions/Default.txt`
- `config/starterkit/descriptions/Engineer.txt`
- `config/starterkit/descriptions/Lumberjack.txt`
- `config/starterkit/descriptions/Witch.txt`
- `config/starterkit/kits/Default.txt`
- `config/starterkit/kits/Engineer.txt`
- `config/starterkit/kits/inactive/Archer.txt`
- `config/starterkit/kits/inactive/Lumberjack.txt`
- `config/starterkit/kits/inactive/Witch.txt`
- `kubejs/startup_scripts/func/config.js`
- `mods/CopperDrops-1.1.jar`
- `mods/Create_Copper_Splashing_v2.jar`
- `mods/Create_Waystone_Recipes_1.19_v2.jar`
- `mods/FastLeafDecay-30.jar`
- `mods/ae2qolrecipes-forge-1.18-1.20.1-1.3.0.jar`
- `mods/backportedBrewin-4.5.0-ce-19.2-02.jar`
- `mods/backportedVinery-1.5.5-ce-19.2-02.jar`
- `mods/bottledair-1.19.2-2.3.jar`
- `mods/comfortable-campfires-1.1.2+1.19.2.jar`
- `mods/create_generators-1.0.0-forge-1.19.2.jar`
- `mods/creativeengineering-1.0.9.jar`
- `mods/furnacerecycle-1.19.2-2.2.jar`
- `mods/glowingraidillagers-1.19.2-1.0.0.jar`
- `mods/moreMooblooms-1.0.0.jar`
- `mods/name-tag-crafting-recipe-mod-1.0 (3).jar`
- `mods/starterkit-1.19.2-7.0.jar`

<!-- deploy:1.0.9 -->
## 1.0.9 - 2026-10-19 15:54

- Minecraft 1.19.2, Forge 43.3.9
- Commit: bb67d68 + 37 uncommitted file(s)
- Since: 1.0.8
- Deployed: server, client 2026-10-19 15:54
- Contents: 373 server+client mods, 45 client-only mods, 30 resource pack files, 16 shader pack files
- Payload: update from 1.0.8, config functional: 35 files, 5.5 MB (mods 1, kubejs 34)
- Built with warnings:
  - pack/manifest.json is out of date with mods/: run tools/gen_pack_manifest.py and commit pack/
  - 37 uncommitted change(s) in shipped folders (commit bb67d68 + working tree)

### Changes

Mods updated (1):

- creativeengineering: `creativeengineering-1.0.8.jar` -> `creativeengineering-1.0.9.jar`

Mods removed (1):

- Rain should extinguish campfires 1.0-1.19.2-forge `rsec-1.0-1.19.2-forge.jar` (was both)

<details><summary>kubejs/: 8 added, 26 changed, 0 removed</summary>

*added (8)*

- `data/create_dd/worldgen/configured_feature/striated_eroded_overworld.json`
- `data/creativeengineering/forge/biome_modifier/nether_worldgen/scarlet_mountains_crimson.json`
- `data/minecraft/worldgen/noise_settings/large_biomes.json`
- `data/minecraft/worldgen/noise_settings/overworld.json`
- `data/terralith/worldgen/configured_feature/enchanted/enchanted_oak.json`
- `data/terralith/worldgen/configured_feature/flower/blue/flowers.json`
- `data/terralith/worldgen/configured_feature/flower/blue/nylium_disk.json`
- `generation_scripts/worldgen/climate/minecraft__overworld.caves.json.gz`

*changed (26)*

- `client_scripts/potion_buckets.js`
- `data/campsite_structures/worldgen/structure/armorer_campsite.json`
- `data/campsite_structures/worldgen/structure/butcher_campsite.json`
- `data/campsite_structures/worldgen/structure/cartographer_campsite.json`
- `data/campsite_structures/worldgen/structure/cleric_campsite.json`
- `data/campsite_structures/worldgen/structure/farmer_campsite.json`
- `data/campsite_structures/worldgen/structure/fisher_campsite.json`
- `generation_scripts/worldgen/biome_config.json`
- `generation_scripts/worldgen/biome_fits.json`
- `generation_scripts/worldgen/flower_features.json`
- `generation_scripts/worldgen/nether_block_swaps.json`
- `generation_scripts/worldgen/structure_tags.json`
- `generation_scripts/worldgen/climate/minecraft__overworld.json.gz`
- `server_scripts/_lib/dump_all.js`
- `server_scripts/_lib/dump_climate.js`
- `server_scripts/cooking/cooking.js`
- `server_scripts/equipment/tipped_arrows.js`
- `server_scripts/metal/metal_base_materials.js`
- `server_scripts/metal/misc_metal_crafts.js`
- `server_scripts/tags/equipmenttags.js`
- `server_scripts/wood/sawdust.js`
- `server_scripts/worldgen/terrain_adaptation.js`
- `startup_scripts/_lib/item_dict.js`
- `startup_scripts/func/armor_effects.js`
- `startup_scripts/func/potion_area.js`
- `startup_scripts/items/air_brake.js`

</details>

### Removals (from 1.0.8)

Delete on the server (2, also in removals_server.txt):

- `mods/creativeengineering-1.0.8.jar`
- `mods/rsec-1.0-1.19.2-forge.jar`

Delete on the client (2, also in removals_client.txt):

- `mods/creativeengineering-1.0.8.jar`
- `mods/rsec-1.0-1.19.2-forge.jar`

<!-- deploy:1.0.8 -->
## 1.0.8 - 2026-10-19 10:06

- Minecraft 1.19.2, Forge 43.3.9
- Commit: 2e8687f + 4 uncommitted file(s)
- Since: 1.0.7
- Deployed: server, client 2026-10-19 10:20
- Contents: 374 server+client mods, 45 client-only mods, 30 resource pack files, 16 shader pack files
- Payload: update from 1.0.7, config functional: 41 files, 5.5 MB (mods 3, kubejs 28, config 8, defaultconfigs 2)
- Notes: Restaged 2026-10-19 (replaces the 01:16 staging): patcherMod 1.0.8 rebuilt with the dedicated-server boot fix (Serene Seasons clock mixin split into server + client), flat worlds stay flat (RegistryLoaderPresetOverworldMixin), /ceplace templates lists per pack, /dumpstructuregen reads the protected placement fields (PlacementInfo); type/* structure tags are real tags so earth-fill pruning runs on villages; town companion villages 64-96 blocks out
- Built with warnings:
  - 4 uncommitted change(s) in shipped folders (commit 2e8687f + working tree)

### Changes

Mods updated (3):

- brewinandchewin: `BrewinAndChewin-1.19-2.0.jar` -> `backportedBrewin-4.5.0-ce-19.2-02.jar`
- creativeengineering: `creativeengineering-1.0.7.jar` -> `creativeengineering-1.0.8.jar`
- vinery: `backportedVinery-1.5.5-ce-19.2-01.jar` -> `backportedVinery-1.5.5-ce-19.2-02.jar`

<details><summary>kubejs/: 6 added, 22 changed, 1 removed</summary>

*added (6)*

- `assets/creativeengineering/textures/item/baking_sheet.png`
- `assets/creativeengineering/textures/item/cake_pan.png`
- `assets/creativeengineering/textures/item/cornmeal.png`
- `assets/creativeengineering/textures/item/pie_tin.png`
- `server_scripts/cooking/alcohol.js`
- `startup_scripts/cooking/cornmeal.js`

*changed (22)*

- `assets/creativeengineering/lang/en_us.json`
- `client_scripts/placeable_tooltip.js`
- `client_scripts/tooltips.js`
- `generation_scripts/food/food_types.json`
- `generation_scripts/food/placeable.json`
- `generation_scripts/stacks/stack_sizes.json`
- `generation_scripts/worldgen/biome_config.json`
- `generation_scripts/worldgen/meta_structures.json`
- `server_scripts/_lib/dump_structuregen.js`
- `server_scripts/cooking/baking.js`
- `server_scripts/cooking/cooking.js`
- `server_scripts/tags/blocktags.js`
- `server_scripts/tags/cookingtags.js`
- `server_scripts/tags/metaltags.js`
- `server_scripts/tags/placeabletags.js`
- `server_scripts/worldgen/structure_overrides.js`
- `startup_scripts/_lib/block_dict.js`
- `startup_scripts/_lib/fluid_dict.js`
- `startup_scripts/_lib/item_dict.js`
- `startup_scripts/changes/transition_items.js`
- `startup_scripts/cooking/bakeware.js`
- `startup_scripts/func/neoCraftHelper.js`

*removed (1)*

- `assets/cornmeal.png`

</details>

config/: 0 added, 8 changed, 0 removed:

*changed (8)*

- `brewinandchewin-common.toml`
- `creativeengineering-common.toml`
- `rare-ice.properties`
- `vinery-common.toml`
- `ftbquests/quests/chapters/progression.snbt`
- `jei/ingredient-list-mod-sort-order.ini`
- `jei/recipe-category-sort-order.ini`
- `worldedit/worldedit.properties`

defaultconfigs/: 0 added, 2 changed, 0 removed:

*changed (2)*

- `seasonhud-server.toml`
- `sereneseasons-server.toml`

### Removals (from 1.0.7)

Delete on the server (5, also in removals_server.txt):

- `kubejs/assets/cornmeal.png`
- `mods/BrewinAndChewin-1.19-2.0.jar`
- `mods/backportedBrewin-4.5.0-ce-19.2-01.jar`
- `mods/backportedVinery-1.5.5-ce-19.2-01.jar`
- `mods/creativeengineering-1.0.7.jar`

Delete on the client (5, also in removals_client.txt):

- `kubejs/assets/cornmeal.png`
- `mods/BrewinAndChewin-1.19-2.0.jar`
- `mods/backportedBrewin-4.5.0-ce-19.2-01.jar`
- `mods/backportedVinery-1.5.5-ce-19.2-01.jar`
- `mods/creativeengineering-1.0.7.jar`

<!-- deploy:1.0.7 -->
## 1.0.7 - 2026-10-18 19:29

- Minecraft 1.19.2, Forge 43.3.9
- Commit: 908e831 + 3 uncommitted file(s)
- Since: 1.0.6
- Deployed: server, client 2026-10-18 19:43
- Contents: 375 server+client mods, 45 client-only mods, 30 resource pack files, 16 shader pack files
- Payload: update from 1.0.6, config functional: 51 files, 15.0 MB (mods 6, kubejs 34, config 7, resourcepacks 2, vinery_upstream 2)
- Notes: hotfix: backportedVinery grass slab bonemeal crash with Immersive Weathering (fertile property)
- Built with warnings:
  - pack/manifest.json is out of date with mods/: run tools/gen_pack_manifest.py and commit pack/
  - 3 uncommitted change(s) in shipped folders (commit 908e831 + working tree)

### Changes

Mods added (2):

- Create : The Salt 1.1.0 `create_salt 1.19.2-1.1.0.jar` (both)
- Storage Labels 1.19.2-1.10 `labels-1.19.2-1.10.jar` (both)

Mods updated (3):

- creativeengineering: `creativeengineering-1.0.6.jar` -> `creativeengineering-1.0.7.jar`
- exposure: `exposureBackport-1.9.19-ce-19.2-03.jar` -> `exposureBackport-1.9.19-ce-19.2-04.jar`
- vinery: `letsdo-vinery-forge-1.3.10.jar` -> `backportedVinery-1.5.5-ce-19.2-01.jar`

Mods removed (1):

- [Let's Do] API  1.1.0b-forge `doapi-1.1.0b.jar` (was both)

Mods whose side changed (14):

- Async Locator `async-locator-forge-1.19.2-1.3.0.jar`: server -> both
- Block Runner `BlockRunner-v4.2.2-1.19.2-Forge.jar`: server -> both
- Cherished Items `CherishedItems-Forge-1.19.2-1.0.2.jar`: server -> both
- Chunky (Forge/NeoForge) `Chunky-1.3.38.jar`: server -> both
- Create: Fast SchematicCannon `CreateFastSchematicCannon-2.6.1-0.5.1i-forge-1.19.2.jar`: server -> both
- Create: Liquid Fuel `createliquidfuel-2.1.1-1.19.2.jar`: server -> both
- Death Knell `DeathKnell-Forge-1.19.2-6.0.3.jar`: server -> both
- Glowing Illagers `glowingraidillagers-1.19.2-1.0.0.jar`: server -> both
- Ice Prevents Crop Growth `icepreventscropgrowth-1.19.2-3.2.jar`: server -> both
- Lightman's Discord Integration `lightmansdiscord-1.19.2-0.2.0.3-server.jar`: server -> both
- Loading Protection `loadingprotection-1.0.2-forge-1.19.2.jar`: server -> both
- Rain should extinguish campfires `rsec-1.0-1.19.2-forge.jar`: server -> both
- Simple Backups `SimpleBackups-1.19.1-2.1.12.jar`: server -> both
- Tax Free Levels `TaxFreeLevels-1.4.23-forge-1.20.1.jar`: server -> both

<details><summary>kubejs/: 10 added, 24 changed, 0 removed</summary>

*added (10)*

- `assets/cornmeal.png`
- `assets/prepared_flesh.png`
- `client_scripts/jei_order.js`
- `server_scripts/cooking/baking.js`
- `server_scripts/tags/jei_order_tags.js`
- `startup_scripts/_lib/effect_dict.js`
- `startup_scripts/_lib/multiblock_dict.js`
- `startup_scripts/cooking/bakeware.js`
- `startup_scripts/func/jei_order.js`
- `startup_scripts/jei/order.js`

*changed (24)*

- `assets/creativeengineering/lang/en_us.json`
- `assets/minecraft/texts/splashes.txt`
- `client_scripts/fluid_jei.js`
- `data/upgrade_aquatic/worldgen/placed_feature/driftwood_beach.json`
- `data/upgrade_aquatic/worldgen/placed_feature/driftwood_extra.json`
- `data/upgrade_aquatic/worldgen/placed_feature/driftwood_river.json`
- `data/upgrade_aquatic/worldgen/placed_feature/driftwood_swamp.json`
- `generation_scripts/worldgen/biome_fits.json`
- `generation_scripts/worldgen/pool_config.json`
- `generation_scripts/worldgen/structure_tags.json`
- `generation_scripts/worldgen/climate/minecraft__overworld.json.gz`
- `generation_scripts/worldgen/climate/minecraft__the_end.json.gz`
- `generation_scripts/worldgen/climate/minecraft__the_nether.json.gz`
- `server_scripts/_lib/villager_trades_dump.js`
- `server_scripts/cooking/cooking.js`
- `server_scripts/engineering/fabric.js`
- `server_scripts/other/misc_mods.js`
- `server_scripts/tags/blocktags.js`
- `server_scripts/tags/cookingtags.js`
- `server_scripts/worldgen/earth_fill.js`
- `startup_scripts/_lib/block_dict.js`
- `startup_scripts/_lib/item_dict.js`
- `startup_scripts/compost/compost_values.js`
- `startup_scripts/func/neoCraftHelper.js`

</details>

config/: 2 added, 5 changed, 0 removed:

*added (2)*

- `brewinandchewin-common.toml`
- `vinery-common.toml`

*changed (5)*

- `creativeengineering-common.toml`
- `rare-ice.properties`
- `jei/ingredient-list-mod-sort-order.ini`
- `jei/recipe-category-sort-order.ini`
- `worldedit/worldedit.properties`

resourcepacks/: 2 added, 0 changed, 0 removed:

*added (2)*

- `Fancy Crops v1.2.zip`
- `Fancy_Beds-v4.2.zip`

vinery_upstream/: 2 added, 0 changed, 0 removed:

*added (2)*

- `letsdo-vinery-fabric-1.5.5.jar`
- `letsdo-vinery-forge-1.3.10.jar`

### Removals (from 1.0.6)

Delete on the server (4, also in removals_server.txt):

- `mods/creativeengineering-1.0.6.jar`
- `mods/doapi-1.1.0b.jar`
- `mods/exposureBackport-1.9.19-ce-19.2-03.jar`
- `mods/letsdo-vinery-forge-1.3.10.jar`

Delete on the client (4, also in removals_client.txt):

- `mods/creativeengineering-1.0.6.jar`
- `mods/doapi-1.1.0b.jar`
- `mods/exposureBackport-1.9.19-ce-19.2-03.jar`
- `mods/letsdo-vinery-forge-1.3.10.jar`

<!-- deploy:1.0.6 -->
## 1.0.6 - 2026-10-14 12:52

- Minecraft 1.19.2, Forge 43.3.9
- Commit: 52b2c63 + 111 uncommitted file(s)
- Since: 1.0.5
- Deployed: server, client 2026-10-14 13:01
- Contents: 373 server+client mods, 45 client-only mods, 28 resource pack files, 16 shader pack files
- Payload: update from 1.0.4, config functional: 1255 files, 23.2 MB (mods 28, client_mods 4, kubejs 1205, config 16, defaultconfigs 1, resourcepacks 1)
- Notes: 1.0.6: everything since the 1.0.4 deploy, folding in the undistributed 1.0.5 (nests mod, stack sizes, cans, mooblooms, seasons/snow line, mob spawns, wild flowers/crops) plus 23 new mods (painting packs, Witch Mobility, Unlit Campfire...). STRUCTURE TUNING IS INCOMPLETE in this release: structure_tuning 7.1-7.4 (groups, replacements, earth fill 7.3, locate 7.3.2, block swaps, CCC 2.3.4 port, new structure mods) ships mid-test, scratch phases 1-3 + M2 run, phases 4-10 pending, known open bugs listed in plan_1.0.6.md.
- Built with warnings:
  - 111 uncommitted change(s) in shipped folders (commit 52b2c63 + working tree)

### Changes

<details><summary>Mods added (23)</summary>

- Atmospheric Paintings Pack for FORGE and FABRIC 1.19.2 `FORGE_PaintingPack_1.19.2.jar` (both)
- Bottled Air 1.19.2-2.3-fabric+forge+neo `bottledair-1.19.2-2.3.jar` (both)
- Campsite Structures 1.0.3 `campsite_structures-1.19.2-FORGE-1.0.3.jar` (both)
- Cycle Paintings 1.19.2-3.5-fabric+forge+neo `cyclepaintings-1.19.2-3.5.jar` (both)
- Dark Paintings 13.1.5 `DarkPaintings-Forge-1.19.2-13.1.5.jar` (both)
- Dimensional Paintings 1.4.1 `Dimensional-Paintings-1.19.2-1.4.1.jar` (both)
- Firefly Bush  [1.18+1.21++ Backport] `fireflybush-1.19.2.jar` (both)
- Fortune on Netherite `fortune_on_netherite_1.1.0_forge_1.19.2.jar` (both)
- Just Enough Painting Previews (JEPP) `jepp-1.19.2-1.1.0.jar` (client)
- Lava Clear View `LavaClearView-1.19.2-forge-5.0.0.jar` (client)
- MC style paintings 1.19.2 v2. `MC style paintings 1.19.2 v2.jar` (both)
- Neko's Enchanted Books 1.19-2.0.3 `NekosEnchantedBooks-1.19-2.0.3.jar` (client)
- Rain should extinguish campfires 1.0-1.19.2-forge `rsec-1.0-1.19.2-forge.jar` (server)
- REBIRTH: Paintings 5.0.0 |Minecon Live Update|1.19.2| `REBIRTH Paintings 5.0.0 Backport 1.19.2.jar` (both)
- RestApi 0.7-1.19.2-forge `restApi - 0.7 - 1.19.2 - Forge.jar` (both)
- Sticky It! `sticky_it-1.2.1-forge-1.19.2.jar` (both)
- Subterrestrial 2.0.0-forge `subterrestrial-forge-1.19.2-2.0.0.jar` (both)
- The Splash Milk 1.4.0 `splash_milk-1.19.2-1.4.0.jar` (both)
- Too Many Paintings! 26.08.16-1.19-forge `toomanypaintings-26.08.16-1.19-forge.jar` (both)
- Underground Bunkers `UndergroundBunkers-1.0.4-1.19.2-1.20.4-forge.jar` (both)
- Unlit Campfire `unlitcampfire-forge-1.19-1.7.1.0.jar` (both)
- Volcanic Caverns 1.2.2 `volcanic_caverns-1.19.2-1.2.2.jar` (both)
- Witch Mobility [Forge & Fabric] witchmobility-1.3.2-1.19.2-FORGE `witchmobility-1.3.2-1.19.2.jar` (both)

</details>

Mods updated (1):

- creativeengineering: `creativeengineering-1.0.5.jar` -> `creativeengineering-1.0.6.jar`

Mods whose side changed (1):

- Breedable Killer Rabbit `breedablekillerrabbit-1.19.2-3.5.jar`: server -> both

<details><summary>kubejs/: 1086 added, 26 changed, 13 removed</summary>

*added (1086)*

- `assets/canned_goods/textures/items/` (9 files)
- `assets/creativeengineering/textures/item/` (25 files)
- `assets/eugene/lang/en_us.json`
- `assets/eugene/lang/zh_cn.json`
- `data/campsite_structures/forge/biome_modifier/armorer_campsite_biome_modifier.json`
- `data/campsite_structures/forge/biome_modifier/butcher_campsite_biome_modifier.json`
- `data/campsite_structures/forge/biome_modifier/cartographer_campsite_biome_modifier.json`
- `data/campsite_structures/forge/biome_modifier/cleric_campsite_biome_modifier.json`
- `data/campsite_structures/forge/biome_modifier/farmer_campsite_biome_modifier.json`
- `data/campsite_structures/forge/biome_modifier/fisher_campsite_biome_modifier.json`
- `data/campsite_structures/worldgen/structure/armorer_campsite.json`
- `data/campsite_structures/worldgen/structure/butcher_campsite.json`
- `data/campsite_structures/worldgen/structure/cartographer_campsite.json`
- `data/campsite_structures/worldgen/structure/cleric_campsite.json`
- `data/campsite_structures/worldgen/structure/farmer_campsite.json`
- `data/campsite_structures/worldgen/structure/fisher_campsite.json`
- `data/campsite_structures/worldgen/structure_set/armorer_campsite.json`
- `data/campsite_structures/worldgen/structure_set/butcher_campsite.json`
- `data/campsite_structures/worldgen/structure_set/cartographer_campsite.json`
- `data/campsite_structures/worldgen/structure_set/cleric_campsite.json`
- `data/campsite_structures/worldgen/structure_set/farmer_campsite.json`
- `data/campsite_structures/worldgen/structure_set/fisher_campsite.json`
- `data/campsite_structures/worldgen/template_pool/armorer_campsite.json`
- `data/campsite_structures/worldgen/template_pool/butcher_campsite.json`
- `data/campsite_structures/worldgen/template_pool/cartographer_campsite.json`
- `data/campsite_structures/worldgen/template_pool/cleric_campsite.json`
- `data/campsite_structures/worldgen/template_pool/farmer_campsite.json`
- `data/campsite_structures/worldgen/template_pool/fisher_campsite.json`
- `data/creativeengineering/paintings/alexsmobs/dog_poker.json`
- `data/creativeengineering/paintings/alexsmobs/dog_poker.png`
- `data/creativeengineering/paintings/alexsmobs/nft.json`
- `data/creativeengineering/paintings/alexsmobs/nft.png`
- `data/creativeengineering/paintings/buzzier_bees/candle.json`
- `data/creativeengineering/paintings/buzzier_bees/candle.png`
- `data/creativeengineering/paintings/cccbridge/funny_redrouters.json`
- `data/creativeengineering/paintings/cccbridge/funny_redrouters.png`
- `data/creativeengineering/paintings/clayworks/baked.json`
- `data/creativeengineering/paintings/clayworks/baked.png`
- `data/creativeengineering/paintings/create_things_and_misc/create_2022.json`
- `data/creativeengineering/paintings/create_things_and_misc/create_2022.png`
- `data/creativeengineering/paintings/darkpaintings/` (60 files)
- `data/creativeengineering/paintings/handcrafted/` (56 files)
- `data/creativeengineering/paintings/minecraft/` (60 files)
- `data/creativeengineering/paintings/minecraft_style_paintings/` (24 files)
- `data/creativeengineering/paintings/neapolitan/cone.json`
- `data/creativeengineering/paintings/neapolitan/cone.png`
- `data/creativeengineering/paintings/neapolitan/dawn.json`
- `data/creativeengineering/paintings/neapolitan/dawn.png`
- `data/creativeengineering/paintings/neapolitan/scream.json`
- `data/creativeengineering/paintings/neapolitan/scream.png`
- `data/creativeengineering/paintings/paintings_pack1/` (98 files)
- `data/creativeengineering/paintings/rebirth_paintings/` (500 files)
- `data/creativeengineering/paintings/supplementaries/bombs.json`
- `data/creativeengineering/paintings/supplementaries/bombs.png`
- `data/creativeengineering/paintings/toomanypaintings/` (130 files)
- `data/creativeengineering/paintings/upgrade_aquatic/sightless.json`
- `data/creativeengineering/paintings/upgrade_aquatic/sightless.png`
- `data/eugene/functions/` (11 files)
- `data/eugene/loot_tables/chests/carriage_chest.json`
- `data/eugene/loot_tables/chests/piglin_camp_chest.json`
- `data/eugene/loot_tables/chests/trader_camp_chest.json`
- `data/eugene/loot_tables/chests/undead_castle_chest.json`
- `data/eugene/loot_tables/chests/watch_tower_chest.json`
- `data/eugene/loot_tables/entities/pig_ranger.json`
- `data/eugene/predicates/chance_1.json`
- `data/eugene/predicates/ranger_spawn.json`
- `data/eugene/structures/` (16 files)
- `data/eugene/tags/worldgen/biome/has_structure/camp.json`
- `data/eugene/tags/worldgen/biome/has_structure/castle.json`
- `data/eugene/tags/worldgen/biome/has_structure/piglin_camp.json`
- `data/eugene/tags/worldgen/biome/has_structure/pillager_loot_carriage.json`
- `data/eugene/tags/worldgen/biome/has_structure/witch_wagon.json`
- `data/eugene/worldgen/structure/` (10 files)
- `data/eugene/worldgen/structure_set/camp.json`
- `data/eugene/worldgen/structure_set/castle.json`
- `data/eugene/worldgen/structure_set/piglin_camp.json`
- `data/eugene/worldgen/structure_set/pillager_loot_carriage.json`
- `data/eugene/worldgen/structure_set/witch_wagon.json`
- `data/eugene/worldgen/template_pool/` (14 files)
- `data/minecraft/structures/pillager_outpost/feature_tent1.nbt`
- `data/minecraft/structures/pillager_outpost/feature_tent2.nbt`
- `data/minecraft/tags/functions/load.json`
- `data/minecraft/tags/functions/tick.json`
- `generation_scripts/worldgen/meat_block_swaps.json`
- `generation_scripts/worldgen/nether_block_swaps.json`
- `server_scripts/_lib/dump_paintings.js`

*changed (26)*

- `client_scripts/namechanges.js`
- `generation_scripts/food/can_servings.json`
- `generation_scripts/food/can_types.json`
- `generation_scripts/food/food_types.json`
- `generation_scripts/worldgen/biome_config.json`
- `generation_scripts/worldgen/biome_fits.json`
- `generation_scripts/worldgen/meta_structures.json`
- `generation_scripts/worldgen/pool_config.json`
- `generation_scripts/worldgen/season_types.json`
- `generation_scripts/worldgen/structure_config.json`
- `generation_scripts/worldgen/structure_groups.json`
- `generation_scripts/worldgen/structure_tags.json`
- `generation_scripts/worldgen/climate/minecraft__overworld.json.gz`
- `generation_scripts/worldgen/climate/minecraft__the_end.json.gz`
- `generation_scripts/worldgen/climate/minecraft__the_nether.json.gz`
- `server_scripts/_lib/dump_all.js`
- `server_scripts/cooking/canning.js`
- `server_scripts/tags/cantags.js`
- `server_scripts/tags/seasontags.js`
- `server_scripts/worldgen/earth_fill.js`
- `server_scripts/worldgen/mob_spawns.js`
- `server_scripts/worldgen/structure_overrides.js`
- `server_scripts/worldgen/wild_crops.js`
- `server_scripts/worldgen/wild_flowers.js`
- `startup_scripts/_lib/item_dict.js`
- `startup_scripts/items/cans.js`

*removed (13)*

- `assets/` (11 files)
- `generation_scripts/worldgen/biome_merges.json`
- `server_scripts/worldgen/biome_merges.js`

</details>

config/: 3 added, 11 changed, 0 removed:

*added (3)*

- `bottledair.json5`
- `cyclepaintings.json5`
- `dimpaintings-common.toml`

*changed (11)*

- `betterdeserttemples-forge-1_19.toml`
- `betterjungletemples-forge-1_19_2.toml`
- `betterwitchhuts-forge-1_19.toml`
- `create_central_kitchen-common.toml`
- `creativeengineering-common.toml`
- `prettyrain.json5`
- `rare-ice.properties`
- `Advancedperipherals/world.toml`
- `jei/ingredient-list-mod-sort-order.ini`
- `jei/recipe-category-sort-order.ini`
- `worldedit/worldedit.properties`

defaultconfigs/: 1 added, 0 changed, 0 removed:

*added (1)*

- `unlitcampfire-server.toml`

### Removals (from 1.0.4)

Delete on the server (5, also in removals_server.txt):

- `kubejs/generation_scripts/worldgen/biome_merges.json`
- `kubejs/server_scripts/worldgen/biome_merges.js`
- `kubejs/startup_scripts/changes/item_properties.js`
- `mods/creativeengineering-1.0.4.jar`
- `mods/exposureBackport-1.9.19-ce-19.2-02.jar`

Delete on the client (5, also in removals_client.txt):

- `kubejs/generation_scripts/worldgen/biome_merges.json`
- `kubejs/server_scripts/worldgen/biome_merges.js`
- `kubejs/startup_scripts/changes/item_properties.js`
- `mods/creativeengineering-1.0.4.jar`
- `mods/exposureBackport-1.9.19-ce-19.2-02.jar`

<!-- deploy:1.0.5 -->
## 1.0.5 - 2026-10-11 20:42

- Minecraft 1.19.2, Forge 43.3.9
- Commit: 52b2c63 + 25 uncommitted file(s)
- Since: 1.0.4
- Contents: 353 server+client mods, 42 client-only mods, 28 resource pack files, 16 shader pack files
- Payload: update from 1.0.4, config functional: 146 files, 13.7 MB (mods 8, client_mods 1, kubejs 126, config 10, resourcepacks 1)
- Notes: 1.0.5: everything since the 1.0.4 deploy (nests mod, stack sizes, per-type cans, worldgen: meta structures, biome fits, terrain adaptation, wild flowers/crops, bee nests, mob spawns, season types; More Mooblooms; Cool Rain, Pretty Rain, MEED, Breedable Killer Rabbit)
- Built with warnings:
  - 25 uncommitted change(s) in shipped folders (commit 52b2c63 + working tree)

### Changes

Mods added (6):

- Breedable Killer Rabbit 1.19.2-3.5-fabric+forge `breedablekillerrabbit-1.19.2-3.5.jar` (server)
- Cool Rain Reforged 1.0.2 `coolrain-1.19.2-Forge-1.0.2.jar` (both)
- incubation_additions `incubationAdditions-1.0.0.jar` (both)
- Moderately Enough Effect Descriptions (MEED) MEED-1.19.2-7.1.1 (Ars & Relics Reupdate) `MEED-1.19.2-7.1.1.jar` (both)
- more_mooblooms `moreMooblooms-1.0.0.jar` (both)
- Pretty Rain 1.19.2 `Pretty Rain-1.19.2-Forge-1.1.1.jar` (client)

Mods updated (2):

- creativeengineering: `creativeengineering-1.0.4.jar` -> `creativeengineering-1.0.5.jar`
- exposure: `exposureBackport-1.9.19-ce-19.2-02.jar` -> `exposureBackport-1.9.19-ce-19.2-03.jar`

Mods whose side changed (1):

- Just Enough Effect Descriptions (JEED) `jeed-1.19.2-2.1.4.jar`: client -> both

<details><summary>kubejs/: 104 added, 22 changed, 1 removed</summary>

*added (104)*

- `assets/` (11 files)
- `client_scripts/placeable_tooltip.js`
- `data/creativeengineering/forge/biome_modifier/azaleabloom_lush_caves.json`
- `data/creativeengineering/forge/biome_modifier/moobloom_cows.json`
- `data/creativeengineering/worldgen/placed_feature/meta/flotilla_driftwood.json`
- `data/fruitsdelight/worldgen/configured_feature/blueberry_bush.json`
- `data/fruitsdelight/worldgen/configured_feature/cranberry_bush.json`
- `data/fruitsdelight/worldgen/configured_feature/lemon_tree.json`
- `data/geophilic_reforged/worldgen/configured_feature/grass/deciduous_forest.json`
- `data/geophilic_reforged/worldgen/configured_feature/grass/plains.json`
- `data/minecraft/worldgen/configured_feature/patch_berry_bush.json`
- `data/minecraft/worldgen/configured_feature/patch_melon.json`
- `data/minecraft/worldgen/configured_feature/patch_pumpkin.json`
- `data/more_mooblooms/moobloom_spawns/creativeengineering.json`
- `data/neapolitan/worldgen/configured_feature/patch_adzuki_sprouts.json`
- `data/neapolitan/worldgen/configured_feature/patch_strawberry_bush.json`
- `data/terralith/worldgen/configured_feature/alpha/flowers.json`
- `data/terralith/worldgen/configured_feature/alpha/flowers_2.json`
- `data/terralith/worldgen/configured_feature/birch/fallen_tree_base.json`
- `data/terralith/worldgen/configured_feature/brushland/grass.json`
- `data/terralith/worldgen/configured_feature/brushland/grass_extra.json`
- `data/terralith/worldgen/configured_feature/flower/blue/orchid.json`
- `data/terralith/worldgen/configured_feature/flower/white/flowers.json`
- `data/terralith/worldgen/configured_feature/highlands/patch_pumpkin.json`
- `data/terralith/worldgen/configured_feature/highlands/alpine/grass.json`
- `data/terralith/worldgen/configured_feature/highlands/alpine/grass_extra.json`
- `data/terralith/worldgen/configured_feature/jungle/patch_melon.json`
- `data/terralith/worldgen/configured_feature/plains/patch_grass.json`
- `data/terralith/worldgen/configured_feature/plains/patch_tall_grass.json`
- `data/terralith/worldgen/configured_feature/skylands/patch_grass.json`
- `data/terralith/worldgen/configured_feature/skylands/winter/patch_grass.json`
- `data/terralith/worldgen/placed_feature/birch/fallen_tree.json`
- `data/terralith/worldgen/placed_feature/birch/trees_forest.json`
- `data/terralith/worldgen/placed_feature/birch/trees_forest_negative.json`
- `data/terralith/worldgen/placed_feature/canyon/red_sandstone/scattered_palms.json`
- `data/terralith/worldgen/placed_feature/canyon/sandstone/scattered_palms.json`
- `data/terralith/worldgen/placed_feature/cherry/birch.json`
- `data/terralith/worldgen/placed_feature/cherry/birch_sparse.json`
- `data/terralith/worldgen/placed_feature/coastal/trees_hawaii.json`
- `data/terralith/worldgen/placed_feature/dark/patch_mushroom.json`
- `data/terralith/worldgen/placed_feature/dark/trees.json`
- `data/terralith/worldgen/placed_feature/desert/lush/grass.json`
- `data/terralith/worldgen/placed_feature/enchanted/shrooms.json`
- `data/terralith/worldgen/placed_feature/enchanted/shrooms_brown.json`
- `data/terralith/worldgen/placed_feature/enchanted/small_shrooms.json`
- `data/terralith/worldgen/placed_feature/enchanted/small_shrooms_brown.json`
- `data/terralith/worldgen/placed_feature/enchanted/trees.json`
- `data/terralith/worldgen/placed_feature/flower/blue/amethysts.json`
- `data/terralith/worldgen/placed_feature/flower/blue/orange_tulips.json`
- `data/terralith/worldgen/placed_feature/flower/blue/trees.json`
- `data/terralith/worldgen/placed_feature/grove/alpine/trees_tiny.json`
- `data/terralith/worldgen/placed_feature/grove/mid/trees_mid.json`
- `data/terralith/worldgen/placed_feature/grove/small/trees_small.json`
- `data/terralith/worldgen/placed_feature/grove/tall/trees_tall.json`
- `data/terralith/worldgen/placed_feature/grove/tiny/trees_tiny.json`
- `data/terralith/worldgen/placed_feature/highlands/temperate/trees_neg.json`
- `data/terralith/worldgen/placed_feature/highlands/temperate/trees_pos.json`
- `data/terralith/worldgen/placed_feature/jungle/mega_tree.json`
- `data/terralith/worldgen/placed_feature/jungle/mega_tree_2.json`
- `data/terralith/worldgen/placed_feature/jungle/vanilla/trees_jungle.json`
- `data/terralith/worldgen/placed_feature/savanna/baobabs.json`
- `data/terralith/worldgen/placed_feature/shrubland/cold/cone_trees.json`
- `data/terralith/worldgen/placed_feature/skylands/trees.json`
- `data/terralith/worldgen/placed_feature/skylands/summer/trees.json`
- `data/terralith/worldgen/placed_feature/swamp/orchid/flowers.json`
- `data/vinery/worldgen/configured_feature/red_grape_bush.json`
- `data/vinery/worldgen/configured_feature/savanna_red_grape_bush.json`
- `data/vinery/worldgen/configured_feature/savanna_white_grape_bush.json`
- `data/vinery/worldgen/configured_feature/taiga_red_grape_bush.json`
- `data/vinery/worldgen/configured_feature/taiga_white_grape_bush.json`
- `data/vinery/worldgen/configured_feature/white_grape_bush.json`
- `generation_scripts/food/can_types.json`
- `generation_scripts/food/placeable.json`
- `generation_scripts/stacks/stack_sizes.json`
- `generation_scripts/worldgen/bee_nests.json`
- `generation_scripts/worldgen/biome_fits.json`
- `generation_scripts/worldgen/flower_features.json`
- `generation_scripts/worldgen/meta_structures.json`
- `generation_scripts/worldgen/mob_spawns.json`
- `generation_scripts/worldgen/season_types.json`
- `generation_scripts/worldgen/wild_flowers.json`
- `server_scripts/_lib/dump_bee_nests.js`
- `server_scripts/_lib/dump_crop_growth.js`
- `server_scripts/_lib/dump_placeable.js`
- `server_scripts/_lib/dump_spawns.js`
- `server_scripts/tags/entitytags.js`
- `server_scripts/tags/placeabletags.js`
- `server_scripts/tags/seasontags.js`
- `server_scripts/worldgen/bee_nests.js`
- `server_scripts/worldgen/mob_spawns.js`
- `server_scripts/worldgen/terrain_adaptation.js`
- `server_scripts/worldgen/water_fill.js`
- `server_scripts/worldgen/wild_flowers.js`
- `startup_scripts/changes/stack_sizes.js`

*changed (22)*

- `data/creativeengineering/worldgen/configured_feature/wild/hamimelon.json`
- `generation_scripts/food/can_servings.json`
- `generation_scripts/food/food_types.json`
- `generation_scripts/worldgen/biome_config.json`
- `generation_scripts/worldgen/pool_config.json`
- `generation_scripts/worldgen/structure_config.json`
- `generation_scripts/worldgen/structure_groups.json`
- `generation_scripts/worldgen/structure_tags.json`
- `generation_scripts/worldgen/wild_crops.json`
- `generation_scripts/worldgen/climate/minecraft__overworld.json.gz`
- `generation_scripts/worldgen/climate/minecraft__the_end.json.gz`
- `generation_scripts/worldgen/climate/minecraft__the_nether.json.gz`
- `server_scripts/_lib/dump_all.js`
- `server_scripts/cooking/canning.js`
- `server_scripts/other/misc_mods.js`
- `server_scripts/tags/blocktags.js`
- `server_scripts/worldgen/structure_overrides.js`
- `startup_scripts/_lib/block_dict.js`
- `startup_scripts/_lib/item_dict.js`
- `startup_scripts/compost/compost_values.js`
- `startup_scripts/func/sample_syringe.js`
- `startup_scripts/items/cans.js`

*removed (1)*

- `startup_scripts/changes/item_properties.js`

</details>

config/: 3 added, 7 changed, 0 removed, 1 no longer shipped:

*added (3)*

- `breedablekillerrabbit.json5`
- `more_mooblooms-common.toml`
- `prettyrain.json5`

*changed (7)*

- `create_central_kitchen-common.toml`
- `creativeengineering-common.toml`
- `quark-common.toml`
- `rare-ice.properties`
- `jei/ingredient-list-mod-sort-order.ini`
- `jei/recipe-category-sort-order.ini`
- `worldedit/worldedit.properties`

*no longer shipped (player settings, left in place) (1)*

- `DistantHorizons.toml`

resourcepacks/: 1 added, 0 changed, 0 removed:

*added (1)*

- `SquirmingSilverfish [1.13 - 26.3]   (v2).zip`

### Removals (from 1.0.4)

Delete on the server (3, also in removals_server.txt):

- `kubejs/startup_scripts/changes/item_properties.js`
- `mods/creativeengineering-1.0.4.jar`
- `mods/exposureBackport-1.9.19-ce-19.2-02.jar`

Delete on the client (3, also in removals_client.txt):

- `kubejs/startup_scripts/changes/item_properties.js`
- `mods/creativeengineering-1.0.4.jar`
- `mods/exposureBackport-1.9.19-ce-19.2-02.jar`

<!-- deploy:1.0.4 -->
## 1.0.4 - 2026-10-04 13:54

- Minecraft 1.19.2, Forge 43.3.9
- Commit: 334c76b + 1 uncommitted file(s)
- Since: 1.0.3
- Deployed: server, client 2026-10-04 13:54
- Contents: 347 server+client mods, 42 client-only mods, 27 resource pack files, 16 shader pack files
- Payload: update from 1.0.3, config functional: 398 files, 12.8 MB (mods 6, kubejs 377, config 15)
- Notes: 1.0.4: Exposure Extras camera bags (dyeable) + join-kick/refmap fixes, crop parity + hoe harvest areas, wild crop placement, canned golem dye, Display Delight icons, Wandering Trader/Winemaker rework, earth fill + IPO + driftwood + banks worldgen, perf throttles for Quark/Serene Seasons/Create Gravity/Fruits Delight/horse armor, Vinery barrel stacking, loot (Golden Food book removed, Lootr fixed containers), 4 new mods, Dimensional Gravity removed
- Built with warnings:
  - 1 uncommitted change(s) in shipped folders (commit 334c76b + working tree)

### Changes

Mods added (4):

- Blue Flame Burning 5 `blueflame-1.19.2-0.1.0.2.jar` (both)
- Fruits Delight `fruitsdelight-0.5.11.jar` (both)
- Improved Pillager Outpost 5 `ImprovedPillagerOutpost-v5.jar` (both)
- Just a Well 1.0.0 `well-1.19.2-1.0.0.jar` (both)

Mods updated (2):

- creativeengineering: `creativeengineering-1.0.3.jar` -> `creativeengineering-1.0.4.jar`
- exposure: `exposureBackport-1.9.19-ce-19.2-01.jar` -> `exposureBackport-1.9.19-ce-19.2-02.jar`

Mods removed (1):

- Dimensional Gravity `Dimensional Gravity 1.0.1 - 1.19.2.jar` (was both)

<details><summary>kubejs/: 301 added, 76 changed, 0 removed</summary>

*added (301)*

- `assets/dyable.png`
- `assets/leatheroutline.png`
- `assets/creativeengineering/textures/entity/canned_golem/` (15 files)
- `assets/displaydelight/models/block/df_cactus_soup.json`
- `assets/displaydelight/models/block/lm_pufferfish_broth.json`
- `assets/displaydelight/models/block/pumpkin_soup.json`
- `assets/displaydelight/models/item/` (89 files)
- `data/ancient_enchantment_temple/loot_tables/chests/fixed/55d6fe4433.json`
- `data/animal_barn/loot_tables/chests/fixed/346e177d20.json`
- `data/animal_barn/loot_tables/chests/fixed/3639e1f658.json`
- `data/animal_barn/loot_tables/chests/fixed/76f93e87de.json`
- `data/animal_barn/loot_tables/chests/fixed/c328cc64f4.json`
- `data/animal_barn/loot_tables/chests/fixed/df216b2369.json`
- `data/animal_barn/loot_tables/chests/fixed/ee30b37967.json`
- `data/antiquetradingship/loot_tables/chests/fixed/` (22 files)
- `data/bighorsestable/loot_tables/chests/fixed/01a2b6c5b7.json`
- `data/bighorsestable/loot_tables/chests/fixed/18ccb86b19.json`
- `data/bighorsestable/loot_tables/chests/fixed/1f8543df4e.json`
- `data/bighorsestable/loot_tables/chests/fixed/6b30f5527e.json`
- `data/bighorsestable/loot_tables/chests/fixed/c8ef58641f.json`
- `data/bighorsestable/loot_tables/chests/fixed/ed1a64f478.json`
- `data/creativeengineering/forge/biome_modifier/driftwood_land_beach.json`
- `data/creativeengineering/forge/biome_modifier/driftwood_land_extra.json`
- `data/creativeengineering/forge/biome_modifier/driftwood_land_swamp.json`
- `data/creativeengineering/worldgen/configured_feature/wild/beetroots.json`
- `data/creativeengineering/worldgen/configured_feature/wild/brown_mushroom_colonies.json`
- `data/creativeengineering/worldgen/configured_feature/wild/cabbages.json`
- `data/creativeengineering/worldgen/configured_feature/wild/hamimelon.json`
- `data/creativeengineering/worldgen/configured_feature/wild/red_mushroom_colonies.json`
- `data/creativeengineering/worldgen/configured_feature/wild/rubber_tree.json`
- `data/creativeengineering/worldgen/placed_feature/driftwood_land/beach.json`
- `data/creativeengineering/worldgen/placed_feature/driftwood_land/extra.json`
- `data/creativeengineering/worldgen/placed_feature/driftwood_land/swamp.json`
- `data/creativeengineering/worldgen/placed_feature/wild/` (11 files)
- `data/desert_village_temple/loot_tables/chests/fixed/1d30e06aa9.json`
- `data/desert_village_temple/loot_tables/chests/fixed/70abde37ea.json`
- `data/desert_village_temple/loot_tables/chests/fixed/799090db68.json`
- `data/desert_village_temple/loot_tables/chests/fixed/8ceac6a077.json`
- `data/desert_village_temple/loot_tables/chests/fixed/a415bd50d9.json`
- `data/desert_village_temple/loot_tables/chests/fixed/f3bdf01ab8.json`
- `data/desert_watchtower/loot_tables/chests/fixed/2ca75e3c4d.json`
- `data/forest_ruins/loot_tables/chests/fixed/62dc2f051e.json`
- `data/forest_ruins/loot_tables/chests/fixed/9952d57750.json`
- `data/forest_ruins/loot_tables/chests/fixed/d1eed4af72.json`
- `data/forest_watchtower/loot_tables/chests/fixed/85432f6caa.json`
- `data/forest_watchtower/loot_tables/chests/fixed/e7cdc49063.json`
- `data/forgotten_church/loot_tables/chests/fixed/46779e895a.json`
- `data/forgotten_church/loot_tables/chests/fixed/76668b036a.json`
- `data/gothic_church/loot_tables/chests/fixed/22960186d8.json`
- `data/gothic_church/loot_tables/chests/fixed/7ebc591a05.json`
- `data/illager_siege_weapons/loot_tables/chests/fixed/5757e9f7bf.json`
- `data/illager_viking_longship/loot_tables/chests/fixed/4c461f6c6a.json`
- `data/illagerwarship/loot_tables/chests/fixed/` (11 files)
- `data/ipo/structures/pillager_outpost/base_plate.nbt`
- `data/ipo/worldgen/template_pool/pillager_outpost/top_variants.json`
- `data/ipo/worldgen/template_pool/pillager_outpost/top_variants_damaged.json`
- `data/ipo/worldgen/template_pool/pillager_outpost/top_variants_overgrown.json`
- `data/ipo/worldgen/template_pool/pillager_outpost/top_variants_overgrown_damaged.json`
- `data/ipo/worldgen/template_pool/pillager_outpost/towers.json`
- `data/llama_stable/loot_tables/chests/fixed/bc4f7a2ef4.json`
- `data/minecraft/structures/pillager_outpost/base_plate.nbt`
- `data/minecraft/worldgen/template_pool/pillager_outpost/base_plates.json`
- `data/mini_clock_tower/loot_tables/chests/fixed/f922b639fd.json`
- `data/mysticrift_pillagers_towers_aris/loot_tables/chests/fixed/` (23 files)
- `data/oldvillagerstatue/loot_tables/chests/fixed/20693cf08a.json`
- `data/plains_villager_tents/loot_tables/chests/fixed/8f3056188d.json`
- `data/plains_villager_tents/loot_tables/chests/fixed/b49b524504.json`
- `data/saloon/loot_tables/chests/fixed/` (14 files)
- `data/small_viking_villager_house/loot_tables/chests/fixed/d6a8a7d110.json`
- `data/small_viking_villager_house/loot_tables/chests/fixed/e675137442.json`
- `data/small_villager_greenhouse/loot_tables/chests/fixed/120d23465a.json`
- `data/smallhorsestable/loot_tables/chests/fixed/6d452ae496.json`
- `data/smallhorsestable/loot_tables/chests/fixed/e91b103738.json`
- `data/snowy_tents/loot_tables/chests/fixed/0856ca91df.json`
- `data/snowy_tents/loot_tables/chests/fixed/7665f2ac77.json`
- `data/upgrade_aquatic/worldgen/placed_feature/driftwood_beach.json`
- `data/upgrade_aquatic/worldgen/placed_feature/driftwood_extra.json`
- `data/upgrade_aquatic/worldgen/placed_feature/driftwood_ocean.json`
- `data/upgrade_aquatic/worldgen/placed_feature/driftwood_river.json`
- `data/upgrade_aquatic/worldgen/placed_feature/driftwood_swamp.json`
- `data/villager_archer_camp/loot_tables/chests/fixed/8b21ddcb7f.json`
- `data/villager_butcher_house/loot_tables/chests/fixed/09fc8e99e5.json`
- `data/villager_butcher_house/loot_tables/chests/fixed/94920ed781.json`
- `data/villager_cartographer_house/loot_tables/chests/fixed/c610fb2dc6.json`
- `data/villager_circus_tent/loot_tables/chests/fixed/5afa0421bc.json`
- `data/villager_circus_tent/loot_tables/chests/fixed/87198d005e.json`
- `data/villager_cleric_house/loot_tables/chests/fixed/b50fc670ad.json`
- `data/villager_country_house/loot_tables/chests/fixed/0acd0b4993.json`
- `data/villager_country_house/loot_tables/chests/fixed/6ef82b7be1.json`
- `data/villager_country_house/loot_tables/chests/fixed/7fb77f72bf.json`
- `data/villager_country_house/loot_tables/chests/fixed/9572f885c2.json`
- `data/villager_farmer_house/loot_tables/chests/fixed/69db62953d.json`
- `data/villager_farmer_house/loot_tables/chests/fixed/6d9792742d.json`
- `data/villager_farmer_house/loot_tables/chests/fixed/752ac63222.json`
- `data/villager_fish_shop/loot_tables/chests/fixed/110911a70f.json`
- `data/villager_fish_shop/loot_tables/chests/fixed/161c4cd512.json`
- `data/villager_fishermans_hut/loot_tables/chests/fixed/018b921ab0.json`
- `data/villager_fishermans_hut/loot_tables/chests/fixed/47d7750151.json`
- `data/villager_fishermans_hut/loot_tables/chests/fixed/a15c906c2d.json`
- `data/villager_fishermans_hut/loot_tables/chests/fixed/a8f937c914.json`
- `data/villager_fletcher_house/loot_tables/chests/fixed/588ebff9b4.json`
- `data/villager_forge/loot_tables/chests/fixed/50834b4402.json`
- `data/villager_forge/loot_tables/chests/fixed/c240b7e40a.json`
- `data/villager_rafts/loot_tables/chests/fixed/07f6f1903c.json`
- `data/villager_rafts/loot_tables/chests/fixed/540848abac.json`
- `data/villager_rafts/loot_tables/chests/fixed/7b1027d4d0.json`
- `data/villager_rafts/loot_tables/chests/fixed/9cd6548b25.json`
- `data/villager_rafts/loot_tables/chests/fixed/9f0d58b7d7.json`
- `data/villager_rafts/loot_tables/chests/fixed/da5f8ab305.json`
- `data/villager_rafts/loot_tables/chests/fixed/e403a43512.json`
- `data/villager_tannery/loot_tables/chests/fixed/40d7ed31f9.json`
- `data/villager_windmill/loot_tables/chests/fixed/9c743ef286.json`
- `data/villager_windmill/loot_tables/chests/fixed/a03487dc71.json`
- `data/watchtower_building/loot_tables/chests/fixed/06161c8193.json`
- `data/watchtower_building/loot_tables/chests/fixed/7f048c032e.json`
- `generation_scripts/worldgen/wild_crops.json`
- `server_scripts/_lib/dump_stacks.js`
- `server_scripts/economy/wandering_trades.js`
- `server_scripts/loot/08_leaf_apples.js`
- `server_scripts/tags/tradetags.js`
- `server_scripts/worldgen/earth_fill.js`
- `server_scripts/worldgen/wild_crops.js`
- `startup_scripts/func/trade_helper.js`

*changed (76)*

- `data/ancient_enchantment_temple/structures/jungle_overgrown_temple.nbt`
- `data/animal_barn/structures/barn.nbt`
- `data/antiquetradingship/structures/shipo.nbt`
- `data/bighorsestable/structures/big_horsestable.nbt`
- `data/desert_village_temple/structures/villager_desert_temple.nbt`
- `data/desert_watchtower/structures/desert_watch_tower.nbt`
- `data/forest_ruins/structures/ruin_1.nbt`
- `data/forest_ruins/structures/ruin_4.nbt`
- `data/forest_ruins/structures/ruin_6.nbt`
- `data/forest_watchtower/structures/forest_watchtower.nbt`
- `data/forgotten_church/structures/church_ruin.nbt`
- `data/gothic_church/structures/church.nbt`
- `data/illager_siege_weapons/structures/siegetower.nbt`
- `data/illager_viking_longship/structures/longship_modded.nbt`
- `data/illagerwarship/structures/illager-warship.nbt`
- `data/llama_stable/structures/llama_stable.nbt`
- `data/mini_clock_tower/structures/mini_clock_tower.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/acacia_pillager_tower_new.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/birch_pillager_tower_new.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/cherry_pillager_tower_new.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/desert_pillager_tower_new.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/jungle_pillager_tower.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/mangrove_pillager_tower_new.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/oak_wood_pillager_tower_new.nbt`
- `data/oldvillagerstatue/structures/oldvillagerstatue.nbt`
- `data/plains_villager_tents/structures/villager_plains_tent.nbt`
- `data/saloon/structures/saloon.nbt`
- `data/small_viking_villager_house/structures/villagervikinghouse.nbt`
- `data/small_villager_greenhouse/structures/smallvillagergreenhouse.nbt`
- `data/smallhorsestable/structures/horsestable.nbt`
- `data/snowy_tents/structures/snowy-tent.nbt`
- `data/villager_archer_camp/structures/villagerarchercamp.nbt`
- `data/villager_butcher_house/structures/butcher.nbt`
- `data/villager_cartographer_house/structures/carto.nbt`
- `data/villager_circus_tent/structures/circus_tent.nbt`
- `data/villager_cleric_house/structures/villagerclerichouse.nbt`
- `data/villager_country_house/structures/country_house.nbt`
- `data/villager_farmer_house/structures/farmer_house.nbt`
- `data/villager_fish_shop/structures/smallfishinshop.nbt`
- `data/villager_fishermans_hut/structures/villagerfisherman.nbt`
- `data/villager_fletcher_house/structures/villager_archery_house.nbt`
- `data/villager_forge/structures/villager-forge.nbt`
- `data/villager_rafts/structures/raft1.nbt`
- `data/villager_rafts/structures/raft2.nbt`
- `data/villager_rafts/structures/raft3.nbt`
- `data/villager_tannery/structures/villager_tannery.nbt`
- `data/villager_windmill/structures/windmill.nbt`
- `data/watchtower_building/structures/a_wt_r3_4p.nbt`
- `data/watchtower_building/structures/ab_watchtower_big_tower.nbt`
- `data/watchtower_building/structures/ab_watchtower_small_tower.nbt`
- `data/watchtower_building/structures/fr_watchtower_big_tower.nbt`
- `data/watchtower_building/structures/fr_watchtower_small_tower.nbt`
- `generation_scripts/food/food_types.json`
- `generation_scripts/worldgen/biome_config.json`
- `generation_scripts/worldgen/pool_config.json`
- `generation_scripts/worldgen/pool_tags.json`
- `generation_scripts/worldgen/structure_groups.json`
- `generation_scripts/worldgen/structure_tags.json`
- `generation_scripts/worldgen/climate/minecraft__overworld.json.gz`
- `generation_scripts/worldgen/climate/minecraft__the_end.json.gz`
- `generation_scripts/worldgen/climate/minecraft__the_nether.json.gz`
- `server_scripts/_lib/dump_all.js`
- `server_scripts/_lib/dump_food.js`
- `server_scripts/_lib/villager_trades_dump.js`
- `server_scripts/engineering/dust.js`
- `server_scripts/equipment/camera_bag.js`
- `server_scripts/loot/07_rules.js`
- `server_scripts/loot/loot_config.json`
- `server_scripts/loot/loot_index.json`
- `server_scripts/magic/armor_effects.js`
- `server_scripts/tags/blocktags.js`
- `server_scripts/tags/equipmenttags.js`
- `server_scripts/worldgen/structure_overrides.js`
- `startup_scripts/_lib/item_dict.js`
- `startup_scripts/compost/compost_values.js`
- `startup_scripts/func/armor_effects.js`

</details>

config/: 4 added, 11 changed, 0 removed, 1 no longer shipped:

*added (4)*

- `exposure_extras-common.toml`
- `fruitsdelight-common.toml`
- `starterkit/descriptions/Engineer.txt`
- `starterkit/kits/Engineer.txt`

*changed (11)*

- `DistantHorizons.toml`
- `alexsmobs.toml`
- `create_central_kitchen-common.toml`
- `creativeengineering-common.toml`
- `everycomp-wood_types.toml`
- `quark-common.toml`
- `rare-ice.properties`
- `jei/ingredient-list-mod-sort-order.ini`
- `jei/recipe-category-sort-order.ini`
- `vinery/config.json5`
- `worldedit/worldedit.properties`

*no longer shipped (player settings, left in place) (1)*

- `trashslot-common.toml`

### Removals (from 1.0.3)

Delete on the server (3, also in removals_server.txt):

- `mods/Dimensional Gravity 1.0.1 - 1.19.2.jar`
- `mods/creativeengineering-1.0.3.jar`
- `mods/exposureBackport-1.9.19-ce-19.2-01.jar`

Delete on the client (3, also in removals_client.txt):

- `mods/Dimensional Gravity 1.0.1 - 1.19.2.jar`
- `mods/creativeengineering-1.0.3.jar`
- `mods/exposureBackport-1.9.19-ce-19.2-01.jar`

<!-- deploy:1.0.3 -->
## 1.0.3 - 2026-10-02 15:41

- Minecraft 1.19.2, Forge 43.3.9
- Commit: ffdda7e + 11 uncommitted file(s)
- Since: 1.0.2
- Deployed: server, client 2026-10-02 15:55
- Contents: 344 server+client mods, 42 client-only mods, 27 resource pack files, 16 shader pack files
- Payload: update from 1.0.1, config functional: 105 files, 10.4 MB (mods 10, kubejs 76, config 11, server 8)
- Notes: server catch-up from 1.0.1 (crash fixes, JFR launch scripts) + 1.0.3: exposure backport, camera bags, Vinery JEI, food mods, trash slot, join protection, LC + Cloud Storage map trades via Async Locator; config functional
- Built with warnings:
  - 11 uncommitted change(s) in shipped folders (commit ffdda7e + working tree)

### Changes

Mods added (4):

- Corn Delight[Forge] 1.0.3-1.19.2 `corn_delight-1.0.3-1.19.2.jar` (both)
- Farmer's Respite 1.19-2.0 `FarmersRespite-1.19-2.0.jar` (both)
- More Delight (Forge & Neoforge) 25.07.28-1.19-forge `moredelight-25.07.28-1.19-forge.jar` (both)
- Mysterious Mountain Lib 1.2.3-1.19.2 `mysterious_mountain_lib-1.2.3-1.19.2.jar` (both)

Mods updated (2):

- creativeengineering: `creativeengineering-1.0.2.jar` -> `creativeengineering-1.0.3.jar`
- exposure: `exposure-1.19.2-1.6.0-forge.jar` -> `exposureBackport-1.9.19-ce-19.2-01.jar`

<details><summary>kubejs/: 35 added, 16 changed, 235 removed</summary>

*added (35)*

- `assets/creativeengineering/blockstates/beef_culture_block.json`
- `assets/creativeengineering/blockstates/cod_culture_block.json`
- `assets/creativeengineering/blockstates/mutton_culture_block.json`
- `assets/creativeengineering/blockstates/pork_culture_block.json`
- `assets/creativeengineering/blockstates/poultry_culture_block.json`
- `assets/creativeengineering/blockstates/rabbit_culture_block.json`
- `assets/creativeengineering/blockstates/salmon_culture_block.json`
- `assets/creativeengineering/models/block/beef_culture_block.json`
- `assets/creativeengineering/models/block/cod_culture_block.json`
- `assets/creativeengineering/models/block/mutton_culture_block.json`
- `assets/creativeengineering/models/block/pork_culture_block.json`
- `assets/creativeengineering/models/block/poultry_culture_block.json`
- `assets/creativeengineering/models/block/rabbit_culture_block.json`
- `assets/creativeengineering/models/block/salmon_culture_block.json`
- `assets/creativeengineering/models/item/beef_culture_block.json`
- `assets/creativeengineering/models/item/cod_culture_block.json`
- `assets/creativeengineering/models/item/mutton_culture_block.json`
- `assets/creativeengineering/models/item/pork_culture_block.json`
- `assets/creativeengineering/models/item/poultry_culture_block.json`
- `assets/creativeengineering/models/item/rabbit_culture_block.json`
- `assets/creativeengineering/models/item/salmon_culture_block.json`
- `assets/creativeengineering/textures/block/culture_block/beef_culture_block.png`
- `assets/creativeengineering/textures/block/culture_block/cod_culture_block.png`
- `assets/creativeengineering/textures/block/culture_block/mutton_culture_block.png`
- `assets/creativeengineering/textures/block/culture_block/pork_culture_block.png`
- `assets/creativeengineering/textures/block/culture_block/poultry_culture_block.png`
- `assets/creativeengineering/textures/block/culture_block/rabbit_culture_block.png`
- `assets/creativeengineering/textures/block/culture_block/salmon_culture_block.png`
- `assets/displaydelight/lang/en_us.json`
- `generation_scripts/worldgen/climate/minecraft__overworld.json.gz`
- `generation_scripts/worldgen/climate/minecraft__the_end.json.gz`
- `generation_scripts/worldgen/climate/minecraft__the_nether.json.gz`
- `server_scripts/_lib/dump_crafting_audit.js`
- `server_scripts/_lib/dump_names.js`
- `server_scripts/equipment/camera_bag.js`

*changed (16)*

- `client_scripts/namechanges.js`
- `client_scripts/tooltips.js`
- `generation_scripts/worldgen/biome_config.json`
- `generation_scripts/worldgen/pool_config.json`
- `server_scripts/_lib/dump_all.js`
- `server_scripts/_lib/dump_climate.js`
- `server_scripts/cooking/canning.js`
- `server_scripts/cooking/cultivated_meat.js`
- `server_scripts/tags/blocktags.js`
- `server_scripts/tags/equipmenttags.js`
- `server_scripts/tags/storageblocktags.js`
- `server_scripts/worldgen/structure_overrides.js`
- `startup_scripts/_lib/item_dict.js`
- `startup_scripts/func/neoCraftHelper.js`
- `startup_scripts/items/cultures.js`
- `startup_scripts/magic/magic_resistance.js`

*removed (235)*

- `README.txt`
- `exported/worldgen/climate/minecraft__overworld.json`
- `exported/worldgen/climate/minecraft__the_end.json`
- `exported/worldgen/climate/minecraft__the_nether.json`
- `generation_scripts/sawmill.js`
- `generation_scripts/recipes/sawmill/` (211 files)
- `generation_scripts/worldgen/` (9 files)
- `recipes/food/bonemeal_broth.json`
- `recipes/recycle/sawmill_recycle_door.json`
- `recipes/recycle/sawmill_recycle_slab.json`
- `recipes/recycle/sawmill_recycle_stairs.json`
- `server_scripts/loot/README.md`
- `server_scripts/loot/loot_census.md`
- `startup_scripts/todo/create_ie_dict_additions.txt`
- `startup_scripts/todo/not_in_dict.txt`
- `startup_scripts/todo/recipes_to_remove.txt`
- `startup_scripts/todo/remove_list.txt`

</details>

<details><summary>config/: 3 added, 7 changed, 0 removed, 123 no longer shipped</summary>

*added (3)*

- `corn_delight-common.toml`
- `farmersrespite-common.toml`
- `mysterious_mountain_lib-common.toml`

*changed (7)*

- `creativeengineering-common.toml`
- `exposure-common.toml`
- `loadingprotection-common.toml`
- `rare-ice.properties`
- `jei/ingredient-list-mod-sort-order.ini`
- `jei/recipe-category-sort-order.ini`
- `worldedit/worldedit.properties`

*no longer shipped (player settings, left in place) (123)*

- `/` (81 files)
- `better_beacons/better_beacons-client.toml`
- `jei/jei-client.ini`
- `jei/jei-colors.ini`
- `jei/jei-debug.ini`
- `jei/jei-mod-id-format.ini`
- `jei/world/local/New_World/bookmarks.ini`
- `jei/world/local/Schematics/bookmarks.ini`
- `jei/world/local/Workspace/bookmarks.ini`
- `jei/world/local/WorldA/bookmarks.ini`
- `jei/world/server/Creative_Engineering_30/bookmarks.ini`
- `jei/world/server/Creative_Engineering_6768b59c/bookmarks.ini`
- `jei/world/server/Creative_Engineering_85adfa7b/bookmarks.ini`
- `jei/world/server/Jayda_and_Elijah_30/bookmarks.ini`
- `jei/world/server/Minecraft_Server_30/bookmarks.ini`
- `jei/world/server/Minecraft_Server_85adf6b1/bookmarks.ini`
- `jei/world/server/Minecraft_Server_85adfa7b/bookmarks.ini`
- `jei/world/server/Schematics_2_6768b657/bookmarks.ini`
- `jei/world/server/Schematics_2_85adfa7b/bookmarks.ini`
- `jei/world/server/Schematics_30/bookmarks.ini`
- `pneumaticcraft/PneumaticArmorHUDLayout.cfg`
- `presencefootsteps/userconfig.json`
- `spyglass-improvements/settings.json`
- `waila/blacklist.json5`
- `waila/blacklist.json5_2026.04.25.19.42.11`
- `waila/blacklist.json_2024.04.05.18.43.03`
- `waila/debug.json5`
- `waila/plugin_toggle.json5`
- `waila/waila.json5`
- `waila/waila_plugins.json5`
- `waila/extra/energy_blacklist.json5`
- `waila/extra/fluid_blacklist.json5`
- `waila/extra/item_blacklist.json5`
- `waila/extra/progress_blacklist.json5`
- `xaero/lib/client.cfg`
- `xaero/lib/profiles/default.cfg`
- `xaero/minimap/client.cfg`
- `xaero/minimap/default_radar_categories_client.json`
- `xaero/minimap/profiles/default.cfg`
- `xaero/minimap/profiles/entity_radar_categories/default.cfg.json`
- `xaero/minimap/profiles/info_display_config/default.cfg.txt`
- `xaero/world-map/client.cfg`
- `xaero/world-map/profiles/default.cfg`

</details>

server/: 1 added, 3 changed, 0 removed:

*added (1)*

- `perf/report.sh`

*changed (3)*

- `LaunchServer.bat`
- `LaunchServer.sh`
- `perf/README.md`

### Removals (server from 1.0.1, client from 1.0.2)

<details><summary>Delete on the server (240, also in removals_server.txt)</summary>

- `config/nohostilesaroundcampfire.json5`
- `kubejs/README.txt`
- `kubejs/exported/worldgen/climate/minecraft__overworld.json`
- `kubejs/exported/worldgen/climate/minecraft__the_end.json`
- `kubejs/exported/worldgen/climate/minecraft__the_nether.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_log-acacia_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_button.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_door.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-vertical_acacia_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_wood-acacia_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_log-ancient_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_button.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_door.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_planks_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_planks_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_planks_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ap_quark_ancient_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-vertical_ancient_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_wood-ancient_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_log-azalea_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-ap_quark_azalea_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_button.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_door.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_planks_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_planks_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_planks_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-vertical_azalea_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_wood-azalea_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_log-birch_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_button.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_door.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-vertical_birch_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_wood-birch_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_log-blossom_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-ap_quark_blossom_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_button.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_door.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_planks_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_planks_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_planks_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-vertical_blossom_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_wood-blossom_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_hyphae-crimson_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_button.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_door.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-vertical_crimson_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_stem-crimson_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_log-dark_oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_button.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_door.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-vertical_dark_oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_wood-dark_oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_log-jungle_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_button.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_door.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-vertical_jungle_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_wood-jungle_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_log-mangrove_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_button.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_door.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-vertical_mangrove_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_wood-mangrove_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_log-oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_button.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_door.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-vertical_oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_wood-oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_log-spruce_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_button.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_door.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-vertical_spruce_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_wood-spruce_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_acacia_log-acacia_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_acacia_wood-acacia_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_ancient_log-ancient_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_ancient_wood-ancient_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_azalea_log-azalea_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_azalea_wood-azalea_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_birch_log-birch_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_birch_wood-birch_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_blossom_log-blossom_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_blossom_wood-blossom_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_crimson_hyphae-crimson_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_crimson_stem-crimson_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_dark_oak_log-dark_oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_dark_oak_wood-dark_oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_jungle_log-jungle_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_jungle_wood-jungle_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_mangrove_log-mangrove_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_mangrove_wood-mangrove_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_oak_log-oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_oak_wood-oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_spruce_log-spruce_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_spruce_wood-spruce_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_twisted_log-twisted_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_twisted_wood-twisted_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_warped_hyphae-warped_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_warped_stem-warped_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_log-twisted_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-q_architects_palette_twisted_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-q_architects_palette_vertical_twisted_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_button.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_door.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_wood-twisted_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_hyphae-warped_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-vertical_warped_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_button.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_door.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_stem-warped_planks.json`
- `kubejs/generation_scripts/sawmill.js`
- `kubejs/generation_scripts/worldgen/README.md`
- `kubejs/generation_scripts/worldgen/biome_merge_proposals.json`
- `kubejs/generation_scripts/worldgen/biome_merge_proposals.md`
- `kubejs/generation_scripts/worldgen/biome_merge_suggestions.md`
- `kubejs/generation_scripts/worldgen/loot_container_audit.md`
- `kubejs/generation_scripts/worldgen/pool_tag_report.md`
- `kubejs/generation_scripts/worldgen/structure_group_estimates.md`
- `kubejs/generation_scripts/worldgen/structure_group_migration.md`
- `kubejs/generation_scripts/worldgen/structure_tag_report.md`
- `kubejs/recipes/food/bonemeal_broth.json`
- `kubejs/recipes/recycle/sawmill_recycle_door.json`
- `kubejs/recipes/recycle/sawmill_recycle_slab.json`
- `kubejs/recipes/recycle/sawmill_recycle_stairs.json`
- `kubejs/server_scripts/loot/README.md`
- `kubejs/server_scripts/loot/loot_census.md`
- `kubejs/startup_scripts/todo/create_ie_dict_additions.txt`
- `kubejs/startup_scripts/todo/not_in_dict.txt`
- `kubejs/startup_scripts/todo/recipes_to_remove.txt`
- `kubejs/startup_scripts/todo/remove_list.txt`
- `mods/creativeengineering-1.0.1.jar`
- `mods/exposure-1.19.2-1.6.0-forge.jar`
- `mods/nohostilesaroundcampfire-1.19.2-5.9.jar`
- `mods/snowundertrees-1.19.2-1.3.1.jar`

</details>

<details><summary>Delete on the client (237, also in removals_client.txt)</summary>

- `kubejs/README.txt`
- `kubejs/exported/worldgen/climate/minecraft__overworld.json`
- `kubejs/exported/worldgen/climate/minecraft__the_end.json`
- `kubejs/exported/worldgen/climate/minecraft__the_nether.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_log-acacia_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_button.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_door.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-acacia_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_planks-vertical_acacia_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/acacia_wood-acacia_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_log-ancient_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_button.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_door.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_planks_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_planks_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_planks_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ancient_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-ap_quark_ancient_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_planks-vertical_ancient_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/ancient_wood-ancient_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_log-azalea_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-ap_quark_azalea_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_button.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_door.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_planks_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_planks_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_planks_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-azalea_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_planks-vertical_azalea_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/azalea_wood-azalea_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_log-birch_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_button.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_door.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-birch_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_planks-vertical_birch_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/birch_wood-birch_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_log-blossom_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-ap_quark_blossom_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_button.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_door.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_planks_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_planks_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_planks_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-blossom_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_planks-vertical_blossom_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/blossom_wood-blossom_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_hyphae-crimson_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_button.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_door.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-crimson_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_planks-vertical_crimson_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/crimson_stem-crimson_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_log-dark_oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_button.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_door.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-dark_oak_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_planks-vertical_dark_oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/dark_oak_wood-dark_oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_log-jungle_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_button.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_door.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-jungle_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_planks-vertical_jungle_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/jungle_wood-jungle_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_log-mangrove_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_button.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_door.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-mangrove_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_planks-vertical_mangrove_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/mangrove_wood-mangrove_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_log-oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_button.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_door.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-oak_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_planks-vertical_oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/oak_wood-oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_log-spruce_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_button.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_door.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-spruce_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_planks-vertical_spruce_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/spruce_wood-spruce_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_acacia_log-acacia_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_acacia_wood-acacia_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_ancient_log-ancient_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_ancient_wood-ancient_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_azalea_log-azalea_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_azalea_wood-azalea_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_birch_log-birch_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_birch_wood-birch_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_blossom_log-blossom_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_blossom_wood-blossom_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_crimson_hyphae-crimson_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_crimson_stem-crimson_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_dark_oak_log-dark_oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_dark_oak_wood-dark_oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_jungle_log-jungle_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_jungle_wood-jungle_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_mangrove_log-mangrove_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_mangrove_wood-mangrove_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_oak_log-oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_oak_wood-oak_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_spruce_log-spruce_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_spruce_wood-spruce_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_twisted_log-twisted_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_twisted_wood-twisted_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_warped_hyphae-warped_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/stripped_warped_stem-warped_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_log-twisted_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-q_architects_palette_twisted_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-q_architects_palette_vertical_twisted_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_button.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_door.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_planks-twisted_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/twisted_wood-twisted_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_hyphae-warped_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-vertical_warped_planks.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_button.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_door.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_fence.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_fence_gate.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_ladder.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_pressure_plate.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_railing.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_sign.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_stairs.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_trapdoor.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_planks-warped_vertical_slab.json`
- `kubejs/generation_scripts/recipes/sawmill/warped_stem-warped_planks.json`
- `kubejs/generation_scripts/sawmill.js`
- `kubejs/generation_scripts/worldgen/README.md`
- `kubejs/generation_scripts/worldgen/biome_merge_proposals.json`
- `kubejs/generation_scripts/worldgen/biome_merge_proposals.md`
- `kubejs/generation_scripts/worldgen/biome_merge_suggestions.md`
- `kubejs/generation_scripts/worldgen/loot_container_audit.md`
- `kubejs/generation_scripts/worldgen/pool_tag_report.md`
- `kubejs/generation_scripts/worldgen/structure_group_estimates.md`
- `kubejs/generation_scripts/worldgen/structure_group_migration.md`
- `kubejs/generation_scripts/worldgen/structure_tag_report.md`
- `kubejs/recipes/food/bonemeal_broth.json`
- `kubejs/recipes/recycle/sawmill_recycle_door.json`
- `kubejs/recipes/recycle/sawmill_recycle_slab.json`
- `kubejs/recipes/recycle/sawmill_recycle_stairs.json`
- `kubejs/server_scripts/loot/README.md`
- `kubejs/server_scripts/loot/loot_census.md`
- `kubejs/startup_scripts/todo/create_ie_dict_additions.txt`
- `kubejs/startup_scripts/todo/not_in_dict.txt`
- `kubejs/startup_scripts/todo/recipes_to_remove.txt`
- `kubejs/startup_scripts/todo/remove_list.txt`
- `mods/creativeengineering-1.0.2.jar`
- `mods/exposure-1.19.2-1.6.0-forge.jar`

</details>

<!-- deploy:1.0.2 -->
## 1.0.2 - 2026-09-30 00:14

- Minecraft 1.19.2, Forge 43.3.9
- Commit: 81421be + 31 uncommitted file(s)
- Since: 1.0.1
- Deployed: client 2026-09-30
- Contents: 340 server+client mods, 42 client-only mods, 27 resource pack files, 16 shader pack files
- Notes: Crash fixes from external-server logs: thread-safe Level.random (Regrowth RNG crash), No Hostiles Around Campfire removed (spawn-scan hang), DH threads 6->2, Async Locator added, JEI now on server; loot-tuning + villager-trade script fixes; multi-serving cans; compost framework; Bushier Flowers growth/worldgen off (Flower Patch instead, 4 loose -> grown recipe); emu nest (Incubation); Untitled Duck Mod + Fuel Goes Here added
- Built with warnings:
  - 31 uncommitted change(s) in shipped folders (commit 81421be + working tree)

### Changes

Mods added (3):

- Async Locator 1.19.2-1.3.0 `async-locator-forge-1.19.2-1.3.0.jar` (server)
- Fuel Goes Here 1 `fuelgoeshere-1.19.2-0.1.0.0.jar` (both)
- Untitled Duck Mod (Forge) 0.6.1-forge-1.19.2 `untitledduckmod-0.6.1-1.19.2-forge.jar` (both)

Mods updated (1):

- creativeengineering: `creativeengineering-1.0.1.jar` -> `creativeengineering-1.0.2.jar`

Mods removed (2):

- No Hostiles Around Campfire 1.19.2-5.9-fabric+forge `nohostilesaroundcampfire-1.19.2-5.9.jar` (was both)
- Snow Under Trees v1.3.1 `snowundertrees-1.19.2-1.3.1.jar` (was both)

Mods whose side changed (1):

- Just Enough Items (JEI) `jei-1.19.2-forge-11.8.1.1034.jar`: client -> both

<details><summary>kubejs/: 18 added, 20 changed, 0 removed</summary>

*added (18)*

- `data/bushierflowers/forge/biome_modifier/add_grown_flower_default.json`
- `data/bushierflowers/forge/biome_modifier/add_grown_flower_flower_forest.json`
- `data/bushierflowers/forge/biome_modifier/add_grown_flower_meadow.json`
- `data/bushierflowers/forge/biome_modifier/add_grown_flower_plain.json`
- `data/bushierflowers/forge/biome_modifier/add_grown_flower_swamp.json`
- `generation_scripts/food/can_servings.json`
- `generation_scripts/food/food_types.json`
- `server_scripts/_lib/dump_compost.js`
- `server_scripts/_lib/dump_food.js`
- `server_scripts/cooking/canning.js`
- `server_scripts/other/flowers.js`
- `server_scripts/tags/cantags.js`
- `server_scripts/tags/foodtypetags.js`
- `startup_scripts/changes/item_properties.js`
- `startup_scripts/compost/compost_rules.js`
- `startup_scripts/compost/compost_values.js`
- `startup_scripts/func/compost.js`
- `startup_scripts/items/cans.js`

*changed (20)*

- `client_scripts/namechanges.js`
- `exported/worldgen/climate/minecraft__overworld.json`
- `exported/worldgen/climate/minecraft__the_end.json`
- `exported/worldgen/climate/minecraft__the_nether.json`
- `generation_scripts/worldgen/biome_config.json`
- `generation_scripts/worldgen/pool_config.json`
- `generation_scripts/worldgen/pool_tag_report.md`
- `generation_scripts/worldgen/structure_groups.json`
- `generation_scripts/worldgen/structure_tag_report.md`
- `server_scripts/_lib/dump_all.js`
- `server_scripts/cooking/cooking.js`
- `server_scripts/economy/trades.js`
- `server_scripts/loot/05_tuning.js`
- `server_scripts/other/misc_mods.js`
- `server_scripts/tags/blocktags.js`
- `server_scripts/tags/cookingtags.js`
- `startup_scripts/_lib/block_dict.js`
- `startup_scripts/_lib/item_dict.js`
- `startup_scripts/func/item_physics.js`
- `startup_scripts/func/neoCraftHelper.js`

</details>

config/: 0 added, 6 changed, 1 removed:

*changed (6)*

- `DistantHorizons.toml`
- `creativeengineering-common.toml`
- `rare-ice.properties`
- `jei/ingredient-list-mod-sort-order.ini`
- `jei/world/local/Workspace/bookmarks.ini`
- `worldedit/worldedit.properties`

*removed (1)*

- `nohostilesaroundcampfire.json5`

server/: 7 added, 0 changed, 0 removed:

*added (7)*

- `LaunchServer.bat`
- `LaunchServer.sh`
- `perf/README.md`
- `perf/ce-server.jfc`
- `perf/dump.bat`
- `perf/dump.ps1`
- `perf/dump.sh`

### Removals

Delete on the server (4, also in removals_server.txt):

- `config/nohostilesaroundcampfire.json5`
- `mods/creativeengineering-1.0.1.jar`
- `mods/nohostilesaroundcampfire-1.19.2-5.9.jar`
- `mods/snowundertrees-1.19.2-1.3.1.jar`

Delete on the client (4, also in removals_client.txt):

- `config/nohostilesaroundcampfire.json5`
- `mods/creativeengineering-1.0.1.jar`
- `mods/nohostilesaroundcampfire-1.19.2-5.9.jar`
- `mods/snowundertrees-1.19.2-1.3.1.jar`

<!-- deploy:1.0.1 -->
## 1.0.1 - 2026-09-28 17:50

- Minecraft 1.19.2, Forge 43.3.9
- Commit: c566438
- Since: 1.0.0
- Deployed: server 2026-09-28 18:00
- Contents: 338 server+client mods, 43 client-only mods, 27 resource pack files, 16 shader pack files

### Changes

<details><summary>Mods added (41)</summary>

- Abnormals Delight 4.1.2 `abnormals_delight-1.19.2-4.1.2.jar` (both)
- Alex's Abnormalities `alexsabnormalities-1.3.jar` (both)
- AstikorCarts [Horse Carts] 1.1.2 `astikorcarts-1.19.2-1.1.2.jar` (both)
- Berry Good 6.1.0 `berry_good-1.19.2-6.1.0.jar` (both)
- Blocky Siege `blocky_siege-6.1.91-1.19.2.jar` (both)
- Boat Item View (Forge/Neoforge) 1.19.x-0.0.4 `Boat-Item-View-Forge-1.19.x-0.0.4.jar` (client)
- Boatload 4.2.2 `boatload-1.19.2-4.2.2.jar` (both)
- BoatView360 v1.0.4-mc1.17-1.20.1-forge `boatview360-v1.0.4-mc1.17-1.20.1-forge.jar` (client)
- Breezy 1.0.8 `breezy-1.19.2-1.0.8.jar` (both)
- Bushier Flowers 0.0.2-1.19.2 `bushierflowers-0.0.2-1.19.2.jar` (both)
- Canned Goods :| 1.0.3 `CannedGoods-1.19.2-3.jar` (both)
- ColoredWater 1.19.2-1.3.7 `Colored Water-1.19.2-1.3.7.jar` (both)
- Create: Fast SchematicCannon 1.19.2-2.6.1-0.5.1i `CreateFastSchematicCannon-2.6.1-0.5.1i-forge-1.19.2.jar` (server)
- Floral Enchantment 1.19.2-1.4.3 `Floral Enchantment-1.19.2-1.4.3.jar` (both)
- Glow Ink Plus 1.2.0 `(Forge)GlowInkPlus-1.19.2-1.2.0.jar` (both)
- Glowing Illagers 1.0.0 `glowingraidillagers-1.19.2-1.0.0.jar` (server)
- Golden Foods! 1.19.2-2.0.0 `goldenfood-1.19.2-2.0.0.jar` (both)
- Greek Villager House `greek_villager_house-1.0.0-forge-1.19.2.jar` (both)
- Hobbit House `hobbit_house_-1.0.1-forge-1.19.2.jar` (both)
- Hopo Better Underwater Ruins 1.1.1 `HopoBetterUnderwaterRuins-[1.19-1.19.3]-1.1.1.jar` (both)
- Ice Boat Nerf - Reforged  [Forge 1.17 - 1.20.5] Ice Boat Nerf `iceboatnerf-1.1.1.jar` (both)
- Ice Prevents Crop Growth 1.19.2-3.2-fabric+forge+neo `icepreventscropgrowth-1.19.2-3.2.jar` (server)
- It Takes a Pillage 1.0.3 `takesapillage-1.0.3-1.19.2.jar` (both)
- Make Bubbles Pop [Client] [Fabric/Forge] 0.3.0-forge `make_bubbles_pop-0.3.0-forge-mc1.19-1.19.3.jar` (client)
- Neapolitan 4.1.0 `neapolitan-1.19.2-4.1.0.jar` (both)
- No Hostiles Around Campfire 1.19.2-5.9-fabric+forge `nohostilesaroundcampfire-1.19.2-5.9.jar` (both)
- Old fisherman swamp house `old_swamp_fisherman_house-1.0.2-forge-1.19.2.jar` (both)
- Rare Ice (Forge/Fabric) 0.5.1 `rare-ice-0.5.1.jar` (both)
- Re:Deco - Vanilla Furniture 1.5.0 `redeco-1.5.0-1.19.2.jar` (both)
- Revamped Shipwrecks (Mod) 1.1.0 `Revamped Shipwrecks 1.1.0.jar` (both)
- Sawmill House 1.3.0 `sawmillhouse-1.3.0-forge-1.19.2.jar` (both)
- Shulker Box Slot (Fabric/Forge/Quilt) 5.0.0-beta.2+1.19.2 `shulkerboxslot-forge-5.0.0-beta.2+1.19.2.jar` (both)
- Simply Houses [FORGE] SimplyHouses-1.1.3-1.19-forge `SimplyHouses-1.1.3-1.19-forge.jar` (both)
- Starter Kit 1.19.2-7.0-fabric+forge `starterkit-1.19.2-7.0.jar` (both)
- The Dirty Stuff (Cigarettes) `the_dirty_stuff-2.1-forge-1.19.2.jar` (both)
- The Salt 1.2.4.1 `salt-1.19.2-1.2.4.1.jar` (both)
- Tinted Campfires 1.19.2-1.2.9 `Tinted Campfires-1.19.2-1.2.9.jar` (both)
- Unwrecked Ships (Forge) Unwrecked Ships - 1.19-1.0.4 [Plus] `Unwrecked Ships Plus 1.19-1.0.4 Forge.jar` (both)
- Upgrade Aquatic 5.1.0 `upgrade_aquatic-1.19.2-5.1.0.jar` (both)
- Villages&Pillages forge-mc1.19.2-1.0.2 `villagesandpillages-forge-mc1.19.2-1.0.2.jar` (both)
- XP Tome v2.1.7 `xptome-1.19-2.1.7.jar` (both)

</details>

Mods updated (1):

- creativeengineering: `creativeengineering-1.0.0.jar` -> `creativeengineering-1.0.1.jar`

Mods removed (1):

- The Missing Villages 3.0.0-1.19.2 `themissingvillages-3.0.0-1.19.2-beta.jar` (was both)

<details><summary>kubejs/: 411 added, 43 changed, 0 removed</summary>

*added (411)*

- `assets/prettypipes/lang/en_us.json`
- `assets/watchtower_building/lang/en_us.json`
- `data/ancient_enchantment_temple/structures/jungle_overgrown_temple.nbt`
- `data/ancient_enchantment_temple/worldgen/structure/temple.json`
- `data/ancient_enchantment_temple/worldgen/structure_set/temple.json`
- `data/ancient_enchantment_temple/worldgen/template_pool/temple.json`
- `data/animal_barn/structures/barn.nbt`
- `data/animal_barn/worldgen/structure/barn.json`
- `data/animal_barn/worldgen/structure_set/barn.json`
- `data/animal_barn/worldgen/template_pool/barn.json`
- `data/antiquetradingship/structures/shipo.nbt`
- `data/antiquetradingship/worldgen/structure/antique_trading_ship.json`
- `data/antiquetradingship/worldgen/structure_set/antique_trading_ship.json`
- `data/antiquetradingship/worldgen/template_pool/antique_trading_ship.json`
- `data/bighorsestable/structures/big_horsestable.nbt`
- `data/bighorsestable/worldgen/structure/big_horse_stable.json`
- `data/bighorsestable/worldgen/structure_set/big_horse_stable.json`
- `data/bighorsestable/worldgen/template_pool/big_horse_stable.json`
- `data/creativeengineering/forge/biome_modifier/felsic_ore_crystal_peaks.json`
- `data/creativeengineering/forge/biome_modifier/felsic_ore_end_barrens.json`
- `data/creativeengineering/forge/biome_modifier/felsic_ore_end_highlands.json`
- `data/creativeengineering/forge/biome_modifier/felsic_ore_end_midlands.json`
- `data/creativeengineering/forge/biome_modifier/felsic_ore_shadowlands.json`
- `data/creativeengineering/forge/biome_modifier/felsic_ore_void_barrens.json`
- `data/creativeengineering/recipes/blueprint/` (12 files)
- `data/creativeengineering/recipes/sawmill/layered_obsidian_sawing.json`
- `data/creativeengineering/recipes/sawmill/rose_quartz_sawing.json`
- `data/creativeengineering/worldgen/configured_feature/felsic_ore.json`
- `data/creativeengineering/worldgen/placed_feature/felsic_ore_crystal_peaks.json`
- `data/creativeengineering/worldgen/placed_feature/felsic_ore_end_barrens.json`
- `data/creativeengineering/worldgen/placed_feature/felsic_ore_end_highlands.json`
- `data/creativeengineering/worldgen/placed_feature/felsic_ore_end_midlands.json`
- `data/creativeengineering/worldgen/placed_feature/felsic_ore_shadowlands.json`
- `data/creativeengineering/worldgen/placed_feature/felsic_ore_void_barrens.json`
- `data/desert_village_temple/structures/villager_desert_temple.nbt`
- `data/desert_village_temple/worldgen/structure/village_temple.json`
- `data/desert_village_temple/worldgen/structure_set/village_temple.json`
- `data/desert_village_temple/worldgen/template_pool/village_temple.json`
- `data/desert_watchtower/structures/desert_watch_tower.nbt`
- `data/desert_watchtower/worldgen/structure/watchtower.json`
- `data/desert_watchtower/worldgen/structure_set/watchtower.json`
- `data/desert_watchtower/worldgen/template_pool/watchtower.json`
- `data/felsic_gear/loot_tables/blocks/felsic_block.json`
- `data/forest_ruins/structures/ruin_1.nbt`
- `data/forest_ruins/structures/ruin_2.nbt`
- `data/forest_ruins/structures/ruin_3.nbt`
- `data/forest_ruins/structures/ruin_4.nbt`
- `data/forest_ruins/structures/ruin_5.nbt`
- `data/forest_ruins/structures/ruin_6.nbt`
- `data/forest_ruins/structures/ruin_7.nbt`
- `data/forest_ruins/worldgen/structure/ruin_1.json`
- `data/forest_ruins/worldgen/structure/ruin_2.json`
- `data/forest_ruins/worldgen/structure/ruin_3.json`
- `data/forest_ruins/worldgen/structure/ruin_4.json`
- `data/forest_ruins/worldgen/structure/ruin_5.json`
- `data/forest_ruins/worldgen/structure/ruin_6.json`
- `data/forest_ruins/worldgen/structure/ruin_7.json`
- `data/forest_ruins/worldgen/structure_set/ruin_1.json`
- `data/forest_ruins/worldgen/structure_set/ruin_2.json`
- `data/forest_ruins/worldgen/structure_set/ruin_3.json`
- `data/forest_ruins/worldgen/structure_set/ruin_4.json`
- `data/forest_ruins/worldgen/structure_set/ruin_5.json`
- `data/forest_ruins/worldgen/structure_set/ruin_6.json`
- `data/forest_ruins/worldgen/structure_set/ruin_7.json`
- `data/forest_ruins/worldgen/template_pool/ruin_1.json`
- `data/forest_ruins/worldgen/template_pool/ruin_2.json`
- `data/forest_ruins/worldgen/template_pool/ruin_3.json`
- `data/forest_ruins/worldgen/template_pool/ruin_4.json`
- `data/forest_ruins/worldgen/template_pool/ruin_5.json`
- `data/forest_ruins/worldgen/template_pool/ruin_6.json`
- `data/forest_ruins/worldgen/template_pool/ruin_7.json`
- `data/forest_watchtower/structures/forest_watchtower.nbt`
- `data/forest_watchtower/worldgen/structure/forest_watchtower.json`
- `data/forest_watchtower/worldgen/structure_set/forest_watchtower.json`
- `data/forest_watchtower/worldgen/template_pool/forest_watchtower.json`
- `data/forgotten_church/structures/church_ruin.nbt`
- `data/forgotten_church/worldgen/structure/forgotten_church.json`
- `data/forgotten_church/worldgen/structure_set/forgotten_church.json`
- `data/forgotten_church/worldgen/template_pool/forgotten_church.json`
- `data/gothic_church/structures/church.nbt`
- `data/gothic_church/worldgen/structure/church.json`
- `data/gothic_church/worldgen/structure_set/church.json`
- `data/gothic_church/worldgen/template_pool/church.json`
- `data/illager_siege_weapons/structures/ballista.nbt`
- `data/illager_siege_weapons/structures/betteringram.nbt`
- `data/illager_siege_weapons/structures/catapult.nbt`
- `data/illager_siege_weapons/structures/reforcedbatteringram.nbt`
- `data/illager_siege_weapons/structures/siegetower.nbt`
- `data/illager_siege_weapons/structures/trebuchet.nbt`
- `data/illager_siege_weapons/worldgen/structure/ballista.json`
- `data/illager_siege_weapons/worldgen/structure/battering_ram.json`
- `data/illager_siege_weapons/worldgen/structure/catapult.json`
- `data/illager_siege_weapons/worldgen/structure/reinforced_battering_ram.json`
- `data/illager_siege_weapons/worldgen/structure/siege_tower.json`
- `data/illager_siege_weapons/worldgen/structure/trebuchet.json`
- `data/illager_siege_weapons/worldgen/structure_set/ballista.json`
- `data/illager_siege_weapons/worldgen/structure_set/battering_ram.json`
- `data/illager_siege_weapons/worldgen/structure_set/catapult.json`
- `data/illager_siege_weapons/worldgen/structure_set/reinforced_battering_ram.json`
- `data/illager_siege_weapons/worldgen/structure_set/siege_tower.json`
- `data/illager_siege_weapons/worldgen/structure_set/trebuchet.json`
- `data/illager_siege_weapons/worldgen/template_pool/ballista.json`
- `data/illager_siege_weapons/worldgen/template_pool/battering_ram.json`
- `data/illager_siege_weapons/worldgen/template_pool/catapult.json`
- `data/illager_siege_weapons/worldgen/template_pool/reinforced_battering_ram.json`
- `data/illager_siege_weapons/worldgen/template_pool/siege_tower.json`
- `data/illager_siege_weapons/worldgen/template_pool/trebuchet.json`
- `data/illager_viking_longship/loot_tables/chests/longship.json`
- `data/illager_viking_longship/structures/longship_modded.nbt`
- `data/illager_viking_longship/worldgen/structure/illager_longship.json`
- `data/illager_viking_longship/worldgen/structure_set/illager_longship.json`
- `data/illager_viking_longship/worldgen/template_pool/illager_longship.json`
- `data/illagerwarship/structures/illager-warship.nbt`
- `data/illagerwarship/worldgen/structure/warship.json`
- `data/illagerwarship/worldgen/structure_set/warship.json`
- `data/illagerwarship/worldgen/template_pool/warship.json`
- `data/immersiveengineering/structures/multiblocks/arcfurnace.nbt`
- `data/immersiveengineering/structures/multiblocks/bucket_wheel.nbt`
- `data/immersivepetroleum/structures/multiblocks/cokerunit.nbt`
- `data/immersivepetroleum/structures/multiblocks/pumpjack.nbt`
- `data/lightmanscurrency/loot_tables/loot_addons/boss/tier1.json`
- `data/lightmanscurrency/loot_tables/loot_addons/boss/tier2.json`
- `data/lightmanscurrency/loot_tables/loot_addons/boss/tier3.json`
- `data/lightmanscurrency/loot_tables/loot_addons/entity/tier1.json`
- `data/lightmanscurrency/loot_tables/loot_addons/entity/tier2.json`
- `data/lightmanscurrency/loot_tables/loot_addons/entity/tier3.json`
- `data/lightmanscurrency/loot_tables/loot_addons/entity/tier4.json`
- `data/lightmanscurrency/loot_tables/loot_addons/entity/tier5.json`
- `data/lightmanscurrency/loot_tables/loot_addons/entity/tier6.json`
- `data/llama_stable/structures/llama_stable.nbt`
- `data/llama_stable/worldgen/structure/llama_stable.json`
- `data/llama_stable/worldgen/structure_set/llama_stable.json`
- `data/llama_stable/worldgen/template_pool/llama_stable.json`
- `data/mini_clock_tower/structures/mini_clock_tower.nbt`
- `data/mini_clock_tower/worldgen/structure/mini_clock_tower.json`
- `data/mini_clock_tower/worldgen/structure_set/mini_clock_tower.json`
- `data/mini_clock_tower/worldgen/template_pool/mini_clock_tower.json`
- `data/mysticrift_pillagers_towers_aris/structures/acacia_pillager_tower_new.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/birch_pillager_tower_new.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/cherry_pillager_tower_new.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/desert_pillager_tower_new.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/end_new_pillager_tower.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/jungle_pillager_tower.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/mangrove_pillager_tower_new.nbt`
- `data/mysticrift_pillagers_towers_aris/structures/oak_wood_pillager_tower_new.nbt`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure/acacia_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure/birch_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure/cherry_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure/desert_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure/end_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure/jungle_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure/mangrove_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure/oak_wood_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure_set/acacia_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure_set/birch_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure_set/cherry_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure_set/desert_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure_set/end_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure_set/jungle_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure_set/mangrove_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/structure_set/oak_wood_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/template_pool/acacia_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/template_pool/birch_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/template_pool/cherry_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/template_pool/desert_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/template_pool/end_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/template_pool/jungle_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/template_pool/mangrove_pillager_tower.json`
- `data/mysticrift_pillagers_towers_aris/worldgen/template_pool/oak_wood_pillager_tower.json`
- `data/old_illager_statue/structures/old_illager_statue.nbt`
- `data/old_illager_statue/worldgen/structure/old_illager_statue.json`
- `data/old_illager_statue/worldgen/structure_set/old_illager_statue.json`
- `data/old_illager_statue/worldgen/template_pool/old_illager_statue.json`
- `data/oldvillagerstatue/structures/oldvillagerstatue.nbt`
- `data/oldvillagerstatue/worldgen/structure/old_villager_statue.json`
- `data/oldvillagerstatue/worldgen/structure_set/old_villager_statue.json`
- `data/oldvillagerstatue/worldgen/template_pool/old_villager_statue.json`
- `data/plains_villager_tents/structures/villager_plains_tent.nbt`
- `data/plains_villager_tents/worldgen/structure/big_villager_tent.json`
- `data/plains_villager_tents/worldgen/structure_set/big_villager_tent.json`
- `data/plains_villager_tents/worldgen/template_pool/big_villager_tent.json`
- `data/saloon/structures/saloon.nbt`
- `data/saloon/worldgen/structure/saloon.json`
- `data/saloon/worldgen/structure_set/saloon.json`
- `data/saloon/worldgen/template_pool/saloon.json`
- `data/skyvillages/loot_tables/chests/` (11 files)
- `data/skyvillages/loot_tables/chests/supplementaries/sky_village_supplementaries_urn.json`
- `data/skyvillages/structures/` (20 files)
- `data/skyvillages/tags/worldgen/biome/has_structure/skyvillage.json`
- `data/skyvillages/worldgen/processor_list/house_processor.json`
- `data/skyvillages/worldgen/structure/skyvillage.json`
- `data/skyvillages/worldgen/structure_set/skyvillage.json`
- `data/skyvillages/worldgen/template_pool/skyvillage.json`
- `data/skyvillages/worldgen/template_pool/skyvillage_bridges.json`
- `data/skyvillages/worldgen/template_pool/skyvillage_deco.json`
- `data/skyvillages/worldgen/template_pool/skyvillage_houses.json`
- `data/skyvillages/worldgen/template_pool/skyvillage_terminator.json`
- `data/skyvillages/worldgen/template_pool/skyvillage_villager.json`
- `data/small_cat_statues/structures/cat1.nbt`
- `data/small_cat_statues/structures/cat2.nbt`
- `data/small_cat_statues/structures/cat3.nbt`
- `data/small_cat_statues/worldgen/structure/cat_1.json`
- `data/small_cat_statues/worldgen/structure/cat_2.json`
- `data/small_cat_statues/worldgen/structure/cat_3.json`
- `data/small_cat_statues/worldgen/structure_set/cat_1.json`
- `data/small_cat_statues/worldgen/structure_set/cat_2.json`
- `data/small_cat_statues/worldgen/structure_set/cat_3.json`
- `data/small_cat_statues/worldgen/template_pool/cat_1.json`
- `data/small_cat_statues/worldgen/template_pool/cat_2.json`
- `data/small_cat_statues/worldgen/template_pool/cat_3.json`
- `data/small_viking_villager_house/structures/villagervikinghouse.nbt`
- `data/small_viking_villager_house/worldgen/structure/small_viking_house.json`
- `data/small_viking_villager_house/worldgen/structure_set/small_viking_house.json`
- `data/small_viking_villager_house/worldgen/template_pool/small_viking_house.json`
- `data/small_villager_greenhouse/structures/smallvillagergreenhouse.nbt`
- `data/small_villager_greenhouse/worldgen/structure/small_villager_greenhouse.json`
- `data/small_villager_greenhouse/worldgen/structure_set/small_villager_greenhouse.json`
- `data/small_villager_greenhouse/worldgen/template_pool/small_villager_greenhouse.json`
- `data/smallhorsestable/structures/horsestable.nbt`
- `data/smallhorsestable/worldgen/structure/horse_stable.json`
- `data/smallhorsestable/worldgen/structure_set/horse_stable.json`
- `data/smallhorsestable/worldgen/template_pool/horse_stable.json`
- `data/snowy_tents/structures/snowy-tent.nbt`
- `data/snowy_tents/worldgen/structure/snowy_tent.json`
- `data/snowy_tents/worldgen/structure_set/snowy_tent.json`
- `data/snowy_tents/worldgen/template_pool/snowy_tent.json`
- `data/villager_archer_camp/structures/villagerarchercamp.nbt`
- `data/villager_archer_camp/worldgen/structure/villager_archer_camp.json`
- `data/villager_archer_camp/worldgen/structure_set/villager_archer_camp.json`
- `data/villager_archer_camp/worldgen/template_pool/villager_archer_camp.json`
- `data/villager_butcher_house/structures/butcher.nbt`
- `data/villager_butcher_house/worldgen/structure/butcher_house.json`
- `data/villager_butcher_house/worldgen/structure_set/butcher_house.json`
- `data/villager_butcher_house/worldgen/template_pool/butcher_house.json`
- `data/villager_cartographer_house/structures/carto.nbt`
- `data/villager_cartographer_house/worldgen/structure/villager_cartographer_house.json`
- `data/villager_cartographer_house/worldgen/structure_set/villager_cartographer_house.json`
- `data/villager_cartographer_house/worldgen/template_pool/villager_cartographer_house.json`
- `data/villager_circus_tent/structures/circus_tent.nbt`
- `data/villager_circus_tent/worldgen/structure/villager_circus_tent.json`
- `data/villager_circus_tent/worldgen/structure_set/villager_circus_tent.json`
- `data/villager_circus_tent/worldgen/template_pool/villager_circus_tent.json`
- `data/villager_cleric_house/structures/villagerclerichouse.nbt`
- `data/villager_cleric_house/worldgen/structure/villager_cleric_house.json`
- `data/villager_cleric_house/worldgen/structure_set/villager_cleric_house.json`
- `data/villager_cleric_house/worldgen/template_pool/villager_cleric_house.json`
- `data/villager_country_house/structures/country_house.nbt`
- `data/villager_country_house/worldgen/structure/country_house.json`
- `data/villager_country_house/worldgen/structure_set/country_house.json`
- `data/villager_country_house/worldgen/template_pool/country_house.json`
- `data/villager_farmer_house/structures/farmer_house.nbt`
- `data/villager_farmer_house/worldgen/structure/villager_farmer_house.json`
- `data/villager_farmer_house/worldgen/structure_set/villager_farmer_house.json`
- `data/villager_farmer_house/worldgen/template_pool/villager_farmer_house.json`
- `data/villager_fish_shop/structures/smallfishinshop.nbt`
- `data/villager_fish_shop/worldgen/structure/fish_shop.json`
- `data/villager_fish_shop/worldgen/structure_set/fish_shop.json`
- `data/villager_fish_shop/worldgen/template_pool/fish_shop.json`
- `data/villager_fishermans_hut/structures/villagerfisherman.nbt`
- `data/villager_fishermans_hut/worldgen/structure/villager_fishermans_hut.json`
- `data/villager_fishermans_hut/worldgen/structure_set/villager_fishermans_hut.json`
- `data/villager_fishermans_hut/worldgen/template_pool/villager_fishermans_hut.json`
- `data/villager_fletcher_house/structures/villager_archery_house.nbt`
- `data/villager_fletcher_house/worldgen/structure/villager_fletcher_house.json`
- `data/villager_fletcher_house/worldgen/structure_set/villager_fletcher_house.json`
- `data/villager_fletcher_house/worldgen/template_pool/villager_fletcher_house.json`
- `data/villager_flower_house/structures/villager_flower_house.nbt`
- `data/villager_flower_house/worldgen/structure/flower_house.json`
- `data/villager_flower_house/worldgen/structure_set/flower_house.json`
- `data/villager_flower_house/worldgen/template_pool/flower_house.json`
- `data/villager_forge/structures/villager-forge.nbt`
- `data/villager_forge/worldgen/structure/villager_forge.json`
- `data/villager_forge/worldgen/structure_set/villager_forge.json`
- `data/villager_forge/worldgen/template_pool/villager_forge.json`
- `data/villager_gold_washing_facility/loot_tables/chests/villager_gold_washing_facility_loot_table.json`
- `data/villager_gold_washing_facility/structures/villager_gold_washing_facility_lt.nbt`
- `data/villager_gold_washing_facility/worldgen/structure/villager_gold_washing_facility.json`
- `data/villager_gold_washing_facility/worldgen/structure_set/villager_gold_washing_facility.json`
- `data/villager_gold_washing_facility/worldgen/template_pool/villager_gold_washing_facility.json`
- `data/villager_rafts/structures/raft1.nbt`
- `data/villager_rafts/structures/raft2.nbt`
- `data/villager_rafts/structures/raft3.nbt`
- `data/villager_rafts/worldgen/structure/raft_1.json`
- `data/villager_rafts/worldgen/structure/raft_2.json`
- `data/villager_rafts/worldgen/structure/raft_3.json`
- `data/villager_rafts/worldgen/structure_set/raft_1.json`
- `data/villager_rafts/worldgen/structure_set/raft_2.json`
- `data/villager_rafts/worldgen/structure_set/raft_3.json`
- `data/villager_rafts/worldgen/template_pool/raft_1.json`
- `data/villager_rafts/worldgen/template_pool/raft_2.json`
- `data/villager_rafts/worldgen/template_pool/raft_3.json`
- `data/villager_tannery/structures/villager_tannery.nbt`
- `data/villager_tannery/worldgen/structure/tannery.json`
- `data/villager_tannery/worldgen/structure_set/tannery.json`
- `data/villager_tannery/worldgen/template_pool/tannery.json`
- `data/villager_windmill/structures/windmill.nbt`
- `data/villager_windmill/worldgen/structure/windmill.json`
- `data/villager_windmill/worldgen/structure_set/windmill.json`
- `data/villager_windmill/worldgen/template_pool/windmill.json`
- `data/watchtower_building/advancements/watchtower_found.json`
- `data/watchtower_building/loot_tables/chests/fresh_towerloot.json`
- `data/watchtower_building/loot_tables/chests/towerloot.json`
- `data/watchtower_building/structures/` (33 files)
- `data/watchtower_building/worldgen/structure/abandoned_watchtower.json`
- `data/watchtower_building/worldgen/structure/fresh_watchtower.json`
- `data/watchtower_building/worldgen/structure_set/abandoned_watchtower.json`
- `data/watchtower_building/worldgen/structure_set/fresh_watchtower.json`
- `data/watchtower_building/worldgen/template_pool/` (12 files)
- `exported/worldgen/climate/minecraft__overworld.json`
- `exported/worldgen/climate/minecraft__the_end.json`
- `exported/worldgen/climate/minecraft__the_nether.json`
- `generation_scripts/worldgen/loot_container_audit.md`
- `generation_scripts/worldgen/loot_container_swaps.json`
- `generation_scripts/worldgen/pool_config.json`
- `generation_scripts/worldgen/pool_tag_report.md`
- `generation_scripts/worldgen/pool_tags.json`
- `generation_scripts/worldgen/structure_group_estimates.md`
- `generation_scripts/worldgen/structure_group_migration.md`
- `generation_scripts/worldgen/structure_groups.json`
- `server_scripts/_lib/export_io.js`
- `server_scripts/_lib/java_guard.js`
- `server_scripts/loot/06_dead_maps.js`
- `server_scripts/loot/07_rules.js`
- `server_scripts/loot/95_tag_capture.js`
- `server_scripts/loot/loot_census.md`
- `server_scripts/tags/loottags.js`
- `server_scripts/worldgen/ground_match.js`
- `startup_scripts/changes/block_properties.js`

*changed (43)*

- `client_scripts/fluid_jei.js`
- `client_scripts/tooltips.js`
- `generation_scripts/worldgen/` (9 files)
- `server_scripts/_lib/` (13 files)
- `server_scripts/loot/` (9 files)
- `server_scripts/tags/blocktags.js`
- `server_scripts/worldgen/biome_merges.js`
- `server_scripts/worldgen/structure_overrides.js`
- `startup_scripts/_lib/block_dict.js`
- `startup_scripts/_lib/fluid_dict.js`
- `startup_scripts/_lib/item_dict.js`
- `startup_scripts/cooking/potions.js`
- `startup_scripts/func/item_physics.js`
- `startup_scripts/func/potion_area.js`
- `startup_scripts/func/tag_helper.js`

</details>

<details><summary>config/: 162 added, 28 changed, 0 removed</summary>

*added (162)*

- `/` (101 files)
- `better_beacons/better_beacons-client.toml`
- `chunky/tasks/minecraft/overworld.properties`
- `chunky/tasks/minecraft/the_end.properties`
- `com.yuushya/client_config.json`
- `doapi/recipebook.json5`
- `jei/blacklist.cfg`
- `jei/ingredient-list-type-sort-order.ini`
- `jei/jei-client.ini`
- `jei/jei-colors.ini`
- `jei/jei-debug.ini`
- `jei/jei-mod-id-format.ini`
- `jei/world/local/New_World/bookmarks.ini`
- `jei/world/local/Schematics/bookmarks.ini`
- `jei/world/local/Workspace/bookmarks.ini`
- `jei/world/local/WorldA/bookmarks.ini`
- `jei/world/server/Creative_Engineering_30/bookmarks.ini`
- `jei/world/server/Creative_Engineering_6768b59c/bookmarks.ini`
- `jei/world/server/Creative_Engineering_85adfa7b/bookmarks.ini`
- `jei/world/server/Jayda_and_Elijah_30/bookmarks.ini`
- `jei/world/server/Minecraft_Server_30/bookmarks.ini`
- `jei/world/server/Minecraft_Server_85adf6b1/bookmarks.ini`
- `jei/world/server/Minecraft_Server_85adfa7b/bookmarks.ini`
- `jei/world/server/Schematics_2_6768b657/bookmarks.ini`
- `jei/world/server/Schematics_2_85adfa7b/bookmarks.ini`
- `jei/world/server/Schematics_30/bookmarks.ini`
- `konkrete/locals/de_de.local`
- `konkrete/locals/en_us.local`
- `konkrete/locals/pl_pl.local`
- `konkrete/locals/pt_br.local`
- `pneumaticcraft/ArmorFeatureStatus.cfg`
- `presencefootsteps/userconfig.json`
- `simplehats_hatdl/simplehats_hatdl.zip`
- `simplehats_hatdl/uuids.json`
- `spyglass-improvements/settings.json`
- `starterkit/descriptions/Archer.txt`
- `starterkit/descriptions/Default.txt`
- `starterkit/descriptions/Lumberjack.txt`
- `starterkit/descriptions/Witch.txt`
- `starterkit/kits/Default.txt`
- `starterkit/kits/inactive/Archer.txt`
- `starterkit/kits/inactive/Lumberjack.txt`
- `starterkit/kits/inactive/Witch.txt`
- `waila/blacklist.json5_2026.04.25.19.42.11`
- `waila/blacklist.json_2024.04.05.18.43.03`
- `waila/waila.json5`
- `xaero/lib/client.cfg`
- `xaero/lib/common.cfg`
- `xaero/lib/profiles/default.cfg`
- `xaero/lib/server_profiles/default.cfg`
- `xaero/minimap/client.cfg`
- `xaero/minimap/common.cfg`
- `xaero/minimap/default_radar_categories_client.json`
- `xaero/minimap/default_radar_categories_server.json`
- `xaero/minimap/profiles/default.cfg`
- `xaero/minimap/profiles/entity_radar_categories/default.cfg.json`
- `xaero/minimap/profiles/info_display_config/default.cfg.txt`
- `xaero/minimap/server_profiles/default.cfg`
- `xaero/world-map/client.cfg`
- `xaero/world-map/common.cfg`
- `xaero/world-map/profiles/default.cfg`
- `xaero/world-map/server_profiles/default.cfg`

*changed (28)*

- `/` (15 files)
- `YungsExtras/forge-1_19/wishing_wells.json`
- `betterdeserttemples/forge-1_19/armorstands.json`
- `betterdeserttemples/forge-1_19/itemframes.json`
- `betterfortresses/forge-1_19/itemframes.json`
- `betterstrongholds/forge-1_19/armorstands.json`
- `betterstrongholds/forge-1_19/itemframes.json`
- `betterstrongholds/forge-1_19/ores.json`
- `betterstrongholds/forge-1_19/rareblocks.json`
- `jei/ingredient-list-mod-sort-order.ini`
- `jei/recipe-category-sort-order.ini`
- `waila/plugin_toggle.json5`
- `waila/waila_plugins.json5`
- `worldedit/worldedit.properties`

</details>

defaultconfigs/: 2 added, 1 changed, 0 removed:

*added (2)*

- `seasonhud-server.toml`
- `sereneseasons-server.toml`

*changed (1)*

- `brickhopper-server.toml`

### Removals

Delete on the server (2, also in removals_server.txt):

- `mods/creativeengineering-1.0.0.jar`
- `mods/themissingvillages-3.0.0-1.19.2-beta.jar`

Delete on the client (2, also in removals_client.txt):

- `mods/creativeengineering-1.0.0.jar`
- `mods/themissingvillages-3.0.0-1.19.2-beta.jar`

<!-- deploy:1.0.0 -->
## 1.0.0 - 2026-09-26 15:39

- Minecraft 1.19.2, Forge 43.3.9
- Commit: none (before the git repo)
- Since: nothing (first deployment: everything is new)
- Contents: 301 server+client mods, 40 client-only mods, 27 resource pack files, 16 shader pack files
- Notes: Deployed when the server started running (world winecraft created 2026-09-26 15:39). Recorded 2026-09-28: server files hashed from the CreEngServer folder as deployed; client = the same server files + client-only jars and packs from pack/manifest.json at 609cd21 (2026-09-27), the earliest record of the client side

### Changes

<details><summary>Mods added (341)</summary>

- Advanced Peripherals `AdvancedPeripherals-1.19.2-0.7.37r.jar` (both)
- AE2 QoL Recipes `ae2qolrecipes-forge-1.18-1.20.1-1.3.0.jar` (both)
- Alex's Delight `alexsdelight-1.4.1.jar` (both)
- Alex's Mobs 1.21.1 `alexsmobs-1.21.1.jar` (both)
- Allurement 3.2.1 `allurement-1.19.2-3.2.1.jar` (both)
- Another Furniture 1.19.2-2.1.4 `another_furniture-forge-1.19.2-2.1.4.jar` (both)
- AppleSkin forge-mc1.19-2.4.2 `appleskin-forge-mc1.19-2.4.2.jar` (both)
- Applied Botanics Addon 1.4.6-forge `Applied-Botanics-forge-1.4.6.jar` (both)
- Applied Cooking 2.0.3 `appliedcooking-2.0.3.jar` (both)
- Applied Energistics 2 AE2 12.9.12 [FORGE] `appliedenergistics2-forge-12.9.12.jar` (both)
- Architect's Palette 1.3.5 `Architects-Palette-1.19.2-1.3.5.jar` (both)
- Architectury API 6.6.92+forge `architectury-6.6.92-forge.jar` (both)
- Armor Trims Backport 1.3.3 `armortrims-1.19.2-1.3.3.jar` (both)
- AutoRegLib 1.8.2-55 `AutoRegLib-1.8.2-55.jar` (both)
- bad packets forge-0.2.3 `badpackets-forge-0.2.3.jar` (both)
- Balm `balm-forge-1.19.2-4.6.0.jar` (both)
- Beautify! 1.4.3 `beautify-1.4.3.jar` (both)
- Bedspreads 6.0.0+1.19.2 `bedspreads-forge-6.0.0+1.19.2.jar` (both)
- Better Days 1.19.2-3.3.4.3-FORGE `betterdays-1.19.2-3.3.4.3-FORGE.jar` (both)
- Better Third Person BetterThirdPerson - Forge - 1.19 - 1.9.0 `BetterThirdPerson-Forge-1.19-1.9.0.jar` (client)
- BetterF3 4.0.1 `BetterF3-4.0.1-Forge-1.19.2.jar` (client)
- Block Runner v4.2.2-1.19.2-Forge `BlockRunner-v4.2.2-1.19.2-Forge.jar` (server)
- Blueprint 6.2.0 `blueprint-1.19.2-6.2.0.jar` (both)
- Book Fishing 1.0.0 `Book Fishing 1.0.0 - 1.19.2.jar` (both)
- Bookshelf 16.3.20 `Bookshelf-Forge-1.19.2-16.3.20.jar` (both)
- Botania 1.19.2-440.2-forge `Botania-1.19.2-440.2-FORGE.jar` (both)
- Brewin' And Chewin' 1.19-2.0 `BrewinAndChewin-1.19-2.0.jar` (both)
- Brick Hopper `brickhopper-1.19-1.4.2.1.jar` (both)
- Buzzier Bees 5.0.1 `buzzier_bees-1.19.2-5.0.1.jar` (both)
- Caelus API 1.19.2-3.0.0.6 `caelus-forge-1.19.2-3.0.0.6.jar` (both)
- Cat Loaf [Forge] Cat Loaf 1.1.1 - 1.19.2 `catloaf-1.1.1-1.19.x-FORGE.jar` (both)
- CB Multipart 3.2.0.139 `CBMultipart-1.19.2-3.2.0.139-universal.jar` (both)
- CC: Tweaked `cc-tweaked-1.19.2-1.101.4.jar` (both)
- CC:C Bridge 1.5.1-forge `cccbridge-mc1.19.2-forge-v1.5.1.jar` (both)
- CERBON's Better Beacons [Forge | Fabric | NeoForge] `CerbonsBetterBeacons-Forge-1.19.2-1.0.2.jar` (both)
- Chalk 1.6.4 `chalk-1.19.2-1.6.4.jar` (both)
- Charm of Undying (Fabric/Forge/Quilt) 6.2.1+1.19.2 `charmofundying-forge-6.2.1+1.19.2.jar` (both)
- Chef's Delight [Forge] - Farmer's Delight Villagers 1.0.3-forge-1.19.2 `chefsdelight-1.0.3-forge-1.19.2.jar` (both)
- Cherished Items `CherishedItems-Forge-1.19.2-1.0.2.jar` (server)
- Cherished Worlds `cherishedworlds-forge-6.0.5+1.19.4.jar` (client)
- Chimes v2.0.1-1.19.2-Forge `Chimes-v2.0.1-1.19.2.jar` (both)
- Chunky (Forge/NeoForge) 1.3.38 `Chunky-1.3.38.jar` (server)
- Citadel 2.1.4-1.19 `citadel-2.1.4-1.19.jar` (both)
- Clayworks 2.1.0 `clayworks-1.19.2-2.1.0.jar` (both)
- Clear Void 1.3.0 `clearvoid-forge-1.3.0.jar` (client)
- Cloth Config API (Fabric/Forge/NeoForge) 8.3.134+forge `cloth-config-8.3.134-forge.jar` (both)
- Cloud  Storage 1.3.4 `cloudstorage-1.3.4-1.19.2.jar` (both)
- Clumps 9.0.0+14 `Clumps-forge-1.19.2-9.0.0+14.jar` (both)
- CodeChicken Lib 1.8.+ 4.3.2.490 `CodeChickenLib-1.19.2-4.3.2.490-universal.jar` (both)
- Collective 1.19.2-7.64-fabric+forge `collective-1.19.2-7.64.jar` (both)
- Comfortable Campfires 1.1.2+1.19.2 `comfortable-campfires-1.1.2+1.19.2.jar` (both)
- Comforts 6.0.7+1.19.2 `comforts-forge-6.0.7+1.19.2.jar` (both)
- Compressed Creativity 1.19.2-0.1.8 `compressedcreativity-1.19.2-0.1.8.jar` (both)
- Compressed Engineering `compressedengineering-1.3.5.jar` (both)
- ConnectedTexturesMod CTM - 1.19.2-1.1.6+8 `CTM-1.19.2-1.1.6+8.jar` (client)
- Connectible Chains [FORGE] Connectible Chains-forge-1.19.2-2.1.4 `Connectible Chains-forge-1.19.2-2.1.4.jar` (both)
- Construction Wand 1.19.2-2.10 `constructionwand-1.19.2-2.10.jar` (both)
- Control Engineering 0.2.0 `ControlEngineering-0.2.0.jar` (both)
- Controlling 10.0+7 `Controlling-forge-1.19.2-10.0+7.jar` (client)
- Cooking for Blockheads `cookingforblockheads-forge-1.19.2-13.3.4.jar` (both)
- Copper Drops 1.1 - 1.19-1.21+ `CopperDrops-1.1.jar` (both)
- Corpse [FORGE][1.19.2] Corpse 1.19.2-1.0.17 `corpse-forge-1.19.2-1.0.17.jar` (both)
- Corpse x Cosmetic Armor Reworked Compat 1.0.0 `cosmeticcorpsecompat-1.19.x-1.20.x-Forge-1.0.0.jar` (both)
- Corpse x Curios API Compat 1.20.4 `corpsecurioscompat-1.18.x-1.20.x-Forge-2.2.2.jar` (both)
- Cosmetic Armor Reworked `CosmeticArmorReworked-1.19.2-v1a.jar` (both)
- Create 1.19.2-0.5.1.i `create-1.19.2-0.5.1.i.jar` (both)
- Create : Gravity 1.2.0 `create_gravity-1.19.2-1.2.0-all.jar` (both)
- Create : Misc & Things 4.0A `create_misc_and_things_ 1.19.2_4.0A.jar` (both)
- Create Compact Exp `nocube's_create_compact_exp_1.0.4_forge_1.19.2.jar` (both)
- Create Compressed 2.0.0 `create_compressed-2.0.0-forge-1.19.2.jar` (both)
- Create Confectionery `create-confectionery1.19.2_v1.0.9.jar` (both)
- Create Crafts & Additions createaddition-forge-1.19.2-1.2.3 `createaddition-1.19.2-1.2.3.jar` (both)
- Create Deco `createdeco-1.3.3-1.19.2.jar` (both)
- Create Goggles (Create Plus) 0.5.5.g `creategoggles-1.19.2-0.5.5.g-[FORGE].jar` (both)
- Create Guardian Beam Defense 1.2.3b `Create-Guardian-Beam-Defense-1.2.3b.jar` (both)
- Create Mechanical Extruder 1.19.2-1.5.7.e-44 `create_mechanical_extruder-1.19.2-1.5.7.e-44.jar` (both)
- Create Questing 1.0.0 `Create_Questing-FORGE-1.19.2-1.0.0.jar` (client)
- Create Recycle Everything `create_recycle_1.0.2_forge_1.19.2.jar` (both)
- Create Slice & Dice 2.4.0 `sliceanddice-forge-2.4.0.jar` (both)
- Create Waystone Recipes 1.19 / 1.20 `Create_Waystone_Recipes_1.19_v2.jar` (both)
- Create: Applied Kinetics 1.4-1.19.2 `createappliedkinetics-1.4-1.19.2.jar` (both)
- Create: Bells & Whistles 0.4.4-1.19.2-forge `bellsandwhistles-v0.4.4-1.19.2.jar` (both)
- Create: Central Kitchen 1.3.11.c `create_central_kitchen-1.19.2-for-create-0.5.1.f-1.3.11.c.jar` (both)
- Create: Copper Splashing `Create_Copper_Splashing_v2.jar` (both)
- Create: Copycats+ 2.2.2+mc.1.19.2-forge `copycats-2.2.2+mc.1.19.2-forge.jar` (both)
- Create: Crystal Clear 1.19.2-0.2.a `create_crystal_clear-0.2.1-1.19.2.jar` (both)
- Create: Deep Dark 1.7.0 `create_deep_dark-1.7.0-forge-1.19.2.jar` (both)
- Create: Design n' Decor 0.4.0 `design_decor-0.4-1.19.2.jar` (both)
- Create: Dreams n' Desires 1.2cBetaMidDev `Create-DnDesire-1.19.2-1.2c.Beta Mid-Dev.jar` (both)
- Create: Dynamic Village 0.4-1.19.2 `dynamicvillage-v0.4-1.19.2.jar` (both)
- Create: Easy Stone Generators 1.0.0 `create_generators-1.0.0-forge-1.19.2.jar` (both)
- Create: Enchantment Industry 1.2.9.e `create_enchantment_industry-1.19.2-for-create-0.5.1.f-1.2.9.e.jar` (both)
- Create: Extended Cogwheels  2.1.1-1.19.2-0.5.1.f-forge `extendedgears-2.1.1-1.19.2-0.5.1.f-forge.jar` (both)
- Create: Factory 0.0.2-1.19.2 `create_factory-0.0.2-1.19.2.jar` (both)
- Create: Fishing Bobber Detector | Automatic Fishing with Create `bobberdetector-0.1.9-1.19.2.jar` (both)
- Create: Framed 1.4.5.1+1.19.2-forge `createframed-1.19.2-1.4.5.1.jar` (both)
- Create: Interiors 0.5.6+forge-mc1.19.2 `interiors-0.5.6+forge-mc1.19.2-build.105.jar` (both)
- Create: Liquid Fuel 2.1.1-1.19.2 `createliquidfuel-2.1.1-1.19.2.jar` (server)
- Create: Netherless (Forge) 1.3.0 `create_netherless-1.19.2-1.3.jar` (both)
- Create: Oxidized 0.1.1 `create_oxidized-1.19.2-0.1.1.jar` (both)
- Create: Power Loader 1.5.0-mc1.19.2 `create_power_loader-1.5.0-mc1.19.2.jar` (both)
- Create: Steam 'n' Rails 1.6.6+forge-mc1.19.2 `Steam_Rails-1.6.6+forge-mc1.19.2.jar` (both)
- Create: Tweaked Controllers 1.19.2-1.2.4 `create_tweaked_controllers-1.19.2-1.2.4.jar` (both)
- Create: Ultimate Factory 1.9.0 `create_ultimate_factory-1.9.0-forge-1.19.2.jar` (both)
- Create: Vintage Improvements 1.19.2-0.2.0.3 `vintageimprovements-1.19.2-0.2.0.3.jar` (both)
- CreativeCore 2.11.28 `CreativeCore_FORGE_v2.11.28_mc1.19.2.jar` (both)
- creativeengineering `creativeengineering-1.0.0.jar` (both)
- Curios API (Forge/NeoForge) `curios-forge-1.19.2-5.1.6.4.jar` (both)
- Customizable Elytra forge-1.7.4-1.19.0 `customizableelytra-1.19.0-1.7.4.jar` (both)
- Dave's Building Extended `dbExtended-1.19.2-6.0.1.jar` (both)
- Death Knell 6.0.3 `DeathKnell-Forge-1.19.2-6.0.3.jar` (server)
- Decorative Blocks `Decorative Blocks-forge-1.19.2-3.0.0.jar` (both)
- Delightful 3.4.1-1.19.2 `Delightful-1.19.2-3.4.1.jar` (both)
- Detail Armor Bar [Forge] 2.6.4+1.19.2-forge `DetailArmorBar-2.6.4+1.19.2-forge.jar` (client)
- Dimensional Gravity `Dimensional Gravity 1.0.1 - 1.19.2.jar` (both)
- Display Delight 1.2.0 `displaydelight-1.2.0.jar` (both)
- Distant Horizons: A Level of Detail mod 3.0.2-b-1.19.2 `DistantHorizons-3.0.2-b-1.19.2-fabric-forge.jar` (both)
- Do a Barrel Roll 2.6.2+1.19-forge `do-a-barrel-roll-2.6.2+1.19.2-forge.jar` (both)
- Domestication Innovation 1.6.1 `domesticationinnovation-1.6.1-1.19.2.jar` (both)
- Don't void Arrows 1.1.0 `don't void arrows V.1.1-1.19.2.jar` (both)
- Dramatic Doors 1.19.2-3.2.7 `DramaticDoors-QuiFabrge-1.19.2-3.2.7.jar` (both)
- Durability Tooltip 1.1.6a-forge-mc1.19 `durabilitytooltip-1.1.6a-forge-mc1.19.jar` (client)
- 'Dustrial Decor `DustrialDecor-1.3.3-1.19.2.jar` (both)
- Dynamic Villager Trades `dynamicvillagertrades-1.2.3-forge-1.19.2.jar` (both)
- Easy Elytra Takeoff 1.19.2-4.2-fabric+forge+neo `easyelytratakeoff-1.19.2-4.2.jar` (both)
- Easy Magic v4.3.3-1.19.2-Forge `EasyMagic-v4.3.3-1.19.2-Forge.jar` (both)
- Effective (Forge) `effective_fg-1.3.4.jar` (client)
- Effishiency 1.19.2-1.1.1 `effishiency-1.19.2-1.1.1.jar` (both)
- Elytra Bombing [Forge] `ElytraBombing-Forge-1.19.2-1.0.0.jar` (both)
- Elytra Slot 6.1.2+1.19.2 `elytraslot-forge-6.1.2+1.19.2.jar` (both)
- Embeddium (Rubidium) Extra 0.4.19+mc1.19.2-build.105 `rubidium-extra-0.4.19+mc1.19.2-build.105.jar` (client)
- Enchantment Descriptions `EnchantmentDescriptions-Forge-1.19.2-13.0.20.jar` (client)
- Ender Crop `endercrop-1.19.2-1.7.0-beta.jar` (both)
- Ender Storage 1.8.+ 2.10.1.181 `EnderStorage-1.19.2-2.10.1.181-universal.jar` (both)
- Ender's Delight 1.19-1.2.2 `endersdelight-1.19-1.2.2.jar` (both)
- Enderdragon Loot `dragonloot-1.1.15-1.19.2-forge.jar` (both)
- Engineer's Decor `engineersdecor-1.19.2-forge-1.3.29.jar` (both)
- Engineers Delight TMTED 1.19.2 R.1.3 `Engineers Delight 1.19.2 R.1.3.jar` (both)
- Entity Culling Fabric/Forge 1.6.1 `entityculling-forge-1.6.1-mc1.19.2.jar` (client)
- Etched [Forge 1.19.2] Etched 2.3.2 `etched-forge-2.3.2.jar` (both)
- Every Compat (Wood Good) 1.19.2-2.5.36 `everycomp-1.19.2-2.5.36.jar` (both)
- Everything is Copper `everythingcopper-1.19.2-2.3.4.jar` (both)
- Explorer's Compass 1.19.2-1.3.0-forge `ExplorersCompass-1.19.2-1.3.0-forge.jar` (both)
- Exposure 1.6.0 `exposure-1.19.2-1.6.0-forge.jar` (both)
- Fairy Lights fairylights-6.0.1-1.19.2 `fairylights-6.0.1-1.19.2.jar` (both)
- Fantasy's Furniture 6.7.0 `fantasyfurniture-1.19.2-6.7.0.jar` (both)
- Farmer's Delight 1.19.2-1.2.4 `FarmersDelight-1.19.2-1.2.4.jar` (both)
- Farmers Structures `FarmersStructures-1.0.0-1.19.2.jar` (both)
- Fast Leaf Decay `FastLeafDecay-30.jar` (both)
- FastFurnace `FastFurnace-1.19.2-7.0.0.jar` (both)
- FastWorkbench `FastWorkbench-1.19.2-7.1.4.jar` (both)
- Felsic Gear 1.0.0 `Felsic Gear 1.0.0  - 1.19.2.jar` (both)
- FerriteCore ((Neo)Forge) 5.0.3-forge `ferritecore-5.0.3-forge.jar` (both)
- Fish On The Line 1.19.2-3.2-fabric+forge+neo `fishontheline-1.19.2-3.2.jar` (both)
- Flower Patch `FlowerPatch-forge-1.19.2-2.0.3.jar` (both)
- Forgiving Void `forgivingvoid-forge-1.19-7.0.0.jar` (both)
- FramedBlocks 6.8.6 `FramedBlocks-6.8.6.jar` (both)
- Friendly Griefing 1.19-1.0.2 `FriendlyGriefing-1.19-1.0.2.jar` (both)
- FTB Library (NeoForge) `ftb-library-forge-1902.4.1-build.236.jar` (both)
- FTB Quests (NeoForge) `ftb-quests-forge-1902.5.10-build.497.jar` (both)
- FTB Teams (NeoForge) `ftb-teams-forge-1902.2.14-build.123.jar` (both)
- Fullblock Energistics forge-1.7.1-1.19.2 `fulleng-forge-1.7.1-1.19.2.jar` (both)
- Furnace Recycle 1.19.2-2.2-fabric+forge+neo `furnacerecycle-1.19.2-2.2.jar` (both)
- GeckoLib 3.1.40 `geckolib-forge-1.19-3.1.40.jar` (both)
- Geode+ [Forge 1.19.2] 1.1.8 `geodeforge-1.1.8-1.19.2.jar` (both)
- Geophilic Reforged – Biome Additions 1.2.0 `GeophilicReforged-v1.2.0.jar` (both)
- Golems Are Friends Not Fodder 3 `golemsarefriends-1.19.2-0.1.0.1.jar` (both)
- Handcrafted 2.0.6 `handcrafted-forge-1.19.2-2.0.6.jar` (both)
- Hearths v1.0.0-mc1.19u1.19.3 `hearths-v1.0.0-mc1.19u1.19.3.jar` (both)
- Heartstone 1.19.2-1.1.2 `heartstone-1.19.2-1.1.2.jar` (both)
- HT's TreeChop 0.19.1 `TreeChop-1.19.2-forge-0.19.1.jar` (both)
- Iceberg [Neo/Forge] 1.1.4 `Iceberg-1.19.2-forge-1.1.4.jar` (client)
- ImmediatelyFast 1.4.0+1.19.2-forge `ImmediatelyFast-Forge-1.4.0+1.19.2.jar` (client)
- Immersive Armors [Fabric/Forge] 1.5.6+1.19.2 `immersive_armors-1.5.6+1.19.2-forge.jar` (both)
- Immersive Engineering 1.19.2-9.2.4-170 `ImmersiveEngineering-1.19.2-9.2.4-170.jar` (both)
- Immersive Paintings [Fabric/Forge] 0.6.7+1.19.2 `immersive_paintings-0.6.7+1.19.2-forge.jar` (both)
- Immersive Petroleum `ImmersivePetroleum-1.19.2-4.2.0-29b.jar` (both)
- Immersive Posts 4.3.0-8 `ImmersivePosts-1.19.2-4.3.0-8.jar` (both)
- Immersive Weathering [FORGE] `immersive_weathering-1.19.2-1.2.10-forge.jar` (both)
- Incubation 3.0.1 `incubation-1.19.2-3.0.1.jar` (both)
- Iron Fishing Rods 1.0.0 `Iron Fishing Rods 1.0.0 - 1.19.2.jar` (both)
- Item Filters 1902.2.9-build.51 `item-filters-forge-1902.2.9-build.51.jar` (both)
- Item Highlighter [Neo/Forge] 1.1.4 `Highlighter-1.19.1-1.1.4.jar` (client)
- ItemPhysic Full 1.6.7 `ItemPhysic_FORGE_v1.6.7_mc1.19.2.jar` (both)
- Just Another Rotten Flesh to Leather Mod 1.4.3 `JRFTL [1.19.1]-1.4.3.jar` (both)
- Just Enough Beacons Reforged [Forge | Fabric | NeoForge] `JustEnoughBeacons-Forge-1.19+-1.1.2.jar` (client)
- Just Enough Breeding (JEBr) `justenoughbreeding-forge-1.19.2-1.4.0.jar` (client)
- Just Enough Effect Descriptions (JEED) 1.19.2-2.1.4 `jeed-1.19.2-2.1.4.jar` (client)
- Just Enough Immersive Multiblocks 1.19.2-0.0.3 `jeimultiblocks-1.19.2-0.0.3.jar` (client)
- Just Enough Items (JEI) 11.8.1.1034 `jei-1.19.2-forge-11.8.1.1034.jar` (client)
- Just Enough Professions (JEP) 2.0.2 `JustEnoughProfessions-forge-1.19.2-2.0.2.jar` (client)
- Just Enough Repair Materials (JERM) 1.19.2-1.2 `jerm-1.19.2-1.2.jar` (client)
- Just Enough Resources (JER) 1.2.3.243 `JustEnoughResources-1.19.2-1.2.3.243.jar` (client)
- Just Enough Sculk Sensor `jess-1.19.2-forge-1.0.0.jar` (client)
- Kiwi 🥝 (Neo/Forge) 8.3.6 `Kiwi-1.19.2-forge-8.3.6.jar` (both)
- Konkrete 1.8.0-1.19-1.19.2-forge `konkrete_forge_1.8.0_MC_1.19-1.19.2.jar` (client)
- Kotlin for Forge 3.12.0 `kotlinforforge-3.12.0-all.jar` (both)
- KubeJS 1902.6.2-build.73+forge `kubejs-forge-1902.6.2-build.73.jar` (both)
- KubeJS + CC: Tweaked 1902-1.0.2-beta `kjscc-1902-1.0.2-beta.jar` (both)
- KubeJS Botania `kubejs_botania-1.3.1-forge.jar` (both)
- KubeJS Create 1902.2.4-build.36+forge `kubejs-create-forge-1902.2.4-build.36.jar` (both)
- KubeJS CSV `kubejs_csv-1.18.2~1.20.1-1.0.0.jar` (both)
- KubeJS Delight 1.19.2-1.1.2 `kubejsdelight-1.19.2-1.1.2.jar` (both)
- KubeJS Immersive Engineering 1902.2.3-build.18 `kubejs-immersive-engineering-1902.2.3-build.18.jar` (both)
- Legendary Tooltips [Neo/Forge] `LegendaryTooltips-1.19.2-forge-1.4.0.jar` (client)
- [Let's Do] API  1.1.0b-forge `doapi-1.1.0b.jar` (both)
- [Let's Do] Vinery Vinery - 1.3.10 - FORGE - 1.19.2 `letsdo-vinery-forge-1.3.10.jar` (both)
- Lightman's Currency 1.19.2-2.2.1.3g `lightmanscurrency-1.19.2-2.2.1.3g.jar` (both)
- Lightman's Discord Integration 1.19.2-0.2.0.3 `lightmansdiscord-1.19.2-0.2.0.3-server.jar` (server)
- Little Contraptions [Forge] 1.19.2.2 `littlecontraptions-forge-1.19.2.2.jar` (both)
- Little Logistics 1.4.0 `littlelogistics-mc1.19.2-v1.4.0.jar` (both)
- Loading Protection `loadingprotection-1.0.2-forge-1.19.2.jar` (server)
- LootJS: KubeJS Addon 1.19.2-2.9.1+forge `lootjs-forge-1.19.2-2.9.1.jar` (both)
- Lootr (Forge & NeoForge) 0.4.28.75 `lootr-forge-1.19-0.4.29.76.jar` (both)
- Lucky's Wardrobe `wardrobe-1.0.3-1.19.2.jar` (both)
- MCA Reborn [Fabric/Forge] 7.5.20+1.19.2 `minecraft-comes-alive-7.5.20+1.19.2-universal.jar` (both)
- MemoryLeakFix v1.0.0 `memoryleakfix-forge-1.17+-1.0.0.jar` (both)
- MES - Moog's End Structures 1.3.1-1.19.2-forge `mes-1.3.1-1.19.2-forge.jar` (both)
- Metal Fences 1.0.1 `MetalFences-FORGE-1.19.2-1.0.1.jar` (both)
- MmmMmmMmmMmm (Target Dummy) MmmMmmMmmMmm-1.19.2-1.7.1 `dummmmmmy-1.19.2-1.7.1.jar` (both)
- Mod Name Tooltip `modnametooltip-1.20.1-1.20.0.jar` (client)
- Momentum Protection Enchantment [Forge] 1.19.2-0.0.1 `momentumprotection-1.19.2-0.0.1.jar` (both)
- Monobank 1.1.3 `monobank-1.19.2-1.1.3.jar` (both)
- Moonlight Lib 1.19.2-2.3.7 `moonlight-1.19.2-2.3.7-forge.jar` (both)
- More Immersive Wires 1.19-1.1.3 `more-immersive-wires-1.19.2-1.1.3.jar` (both)
- More Mob Variants 1.3.0 `moremobvariants-forge+1.19.2-1.3.0.1.jar` (both)
- MoreJS 1.19.2-0.3.0+forge `morejs-forge-1.19.2-0.3.0.jar` (both)
- Mosaic Masonry 1.1 `mosaic_masonry-1.1.jar` (both)
- Mouse Tweaks 1.19-2.23-forge `MouseTweaks-forge-mc1.19-2.23.jar` (client)
- Moyai 1.19.2-2.0.1 `moyai-1.19.2-2.0.1.jar` (both)
- Myrtrees `myrtrees-forge-1.2.0-build.32.jar` (both)
- Mystical Oak Tree `mysticaloaktree-1.19.2-1.10.jar` (both)
- Nametag Crafting 1.0+mod `name-tag-crafting-recipe-mod-1.0 (3).jar` (both)
- Nature's Compass 1.19.2-1.10.0-forge `NaturesCompass-1.19.2-1.10.0-forge.jar` (both)
- Nether's Delight 1.19-3.1 `NethersDelight-1.19-3.1.jar` (both)
- Nethered End Portal End Portal 1.19.2 ver1.2 `endportal-1.19.2-ver1.2.jar` (both)
- NetherPortalFix `netherportalfix-forge-1.19-10.0.1.jar` (both)
- Nock Enough Arrows 1.19.2 - 1.2.0 `nock_enough_arrows-1.19.2-1.2.0.jar` (both)
- Not Enough Animations 1.12.2 `notenoughanimations-forge-1.12.2-mc1.19.2.jar` (client)
- Not Enough Recipe Book [NERB] 0.3 `nerb-1.19.2-0.3-FORGE.jar` (both)
- Nullscape 1.2.2 `Nullscape_1.19.3_v1.2.2.jar` (both)
- Old Guns Mod `oldguns-1.19.2-3.7.3-47.jar` (both)
- Ore Excavation `OreExcavation-1.11.166.jar` (both)
- Panorama [1.19.2] [Forge] Panorama 1.3.0 `panorama-1.3.0 - 1.19.2.jar` (client)
- Patchouli 1.19.2-77-forge `Patchouli-1.19.2-77.jar` (both)
- Per-Dimension Game Rules `PerDimensionGameRules-1.19.2-forge-1.2.0.jar` (both)
- Philip's Ruins 1.19.2-10.8 `PhilipsRuins1.19.2-10.8.jar` (both)
- Pierce Arrow `piercearrow-1.19.2-2.0.0.jar` (both)
- Placebo `Placebo-1.19.2-7.4.1.jar` (both)
- PneumaticCraft: Repressurized pneumaticcraft-repressurized v1.19.2-4.3.15 `pneumaticcraft-repressurized-1.19.2-4.3.15-52.jar` (both)
- Pollen [Forge 1.19.2] Pollen 2.1.0 `pollen-forge-2.1.0.jar` (both)
- Polymorph (Fabric/Forge/Quilt) 0.46.6+1.19.2 `polymorph-forge-0.46.6+1.19.2.jar` (both)
- Polymorphic Energistics forge-0.1.1-1.19.2 `polyeng-forge-0.1.1-1.19.2.jar` (both)
- Ponder for KubeJS 1.19.2-1.2.0+forge `ponderjs-1.19.2-1.2.0.jar` (client)
- Portable Mobs 1.2.1 for Forge 1.19 `portablemobs-1.2.1-forge-mc1.19.jar` (both)
- Portable Wardrobes (Easy Armor Swap) `portablewardrobearmorswap-1.0.2-forge-1.19.2.jar` (both)
- Potion Bundles 1.6 `potionbundles-1.19.1-1.6.jar` (both)
- Presence Footsteps (Forge) 1.19.2-1.6.4.1 `PresenceFootsteps-1.19.2-1.6.4.1-forge.jar` (client)
- Pretty Pipes 1.13.6 `PrettyPipes-1.13.6.jar` (both)
- Pretty Pipes: Fluids `ppfluids-1.19.2-2.0.1.jar` (both)
- Prism [Neo/Forge] 1.0.2 `Prism-1.19.1-1.0.2.jar` (client)
- Project Red - Core 4.19.0-beta+33 `ProjectRed-1.19.2-4.19.0-beta+33-core.jar` (both)
- Project Red - Fabrication 4.19.0-beta+33 `ProjectRed-1.19.2-4.19.0-beta+33-fabrication.jar` (both)
- Project Red - Illumination 4.19.0-beta+33 `ProjectRed-1.19.2-4.19.0-beta+33-illumination.jar` (both)
- Project Red - Integration 4.19.0-beta+33 `ProjectRed-1.19.2-4.19.0-beta+33-integration.jar` (both)
- Project Red - Transmission 4.19.0-beta+33 `ProjectRed-1.19.2-4.19.0-beta+33-transmission.jar` (both)
- Puzzles Lib v4.4.3-1.19.2-Forge `PuzzlesLib-v4.4.3-1.19.2-Forge.jar` (both)
- Quark 1.19.2-3.4-418 `Quark-3.4-418.jar` (both)
- Quark Delight `quark_delight_1.0.0_forge_1.19.2.jar` (both)
- Quark Engineering `QuarkEngineering.jar` (both)
- Quark Oddities 1.18 `QuarkOddities-1.18.jar` (both)
- Quests Additions (Forge) `questsadditions-1.19.2-1.4.2.jar` (both)
- Redstone Gates `gates-1.8.0.jar` (both)
- Regrowth `Regrowth-1.19-41.1.31.1.jar` (both)
- Resourceful Lib 1.1.24 `resourcefullib-forge-1.19.2-1.1.24.jar` (both)
- Responsive Shields 2.3 `responsiveshields-2.3-mc1.18-19-20.x.jar` (both)
- Rhino 1902.2.3-build.284+forge `rhino-forge-1902.2.3-build.284.jar` (both)
- Rubidium 0.6.2c `rubidium-mc1.19.2-0.6.2c.jar` (client)
- Rustic Delight 1.3.0 `rusticdelight-forge-1.19.2-1.3.0.jar` (both)
- Scholar Unofficial Backport `scholar-1.19.2-1.0.0-forge.jar` (both)
- Sculk Transporting v1.1 `sculktransporting-1.19.2-1.1.jar` (both)
- SeasonHud 1.19.2-2.0.4 `seasonhud-forge-1.19.2-2.0.4.jar` (both)
- SebastrnLib 2.0.2 `sebastrnlib-2.0.2.jar` (both)
- Second Chance (Forge) Release 1.5.1 for Minecraft 1.19 `secondchanceforge-1.19-1.5.1.jar` (both)
- Security Craft v1.9.6.1 `[1.19.2] SecurityCraft v1.9.6.1.jar` (both)
- Separated Leaves 2.4.0-forge-1.19.2 `separatedleaves-2.4.0-forge-1.19.2.jar` (both)
- Serene Seasons 8.1.0.24 `SereneSeasons-1.19.2-8.1.0.24.jar` (both)
- Simple Backups 1.19.1-2.1.12 `SimpleBackups-1.19.1-2.1.12.jar` (server)
- Simple Hats 1.19.2-0.3.2 `simplehats-forge-1.19.2-0.3.2.jar` (both)
- Simple Netherite Horse Armor 1.19-1.0.4 `netherite_horse_armor-forge-1.19-1.0.4.jar` (both)
- Sit v1.3.3 `sit-1.19-1.3.3.jar` (both)
- Smaller Nether Portals 1.19.2-3.6-fabric+forge+neo `smallernetherportals-1.19.2-3.6.jar` (both)
- Snow Under Trees v1.3.1 `snowundertrees-1.19.2-1.3.1.jar` (both)
- Sooty Chimneys 1.2.1 `sootychimneys-1.19.2-1.2.1.jar` (both)
- Sophisticated Backpacks 1.19.2-3.20.2.1035 `sophisticatedbackpacks-1.19.2-3.20.2.1035.jar` (both)
- Sophisticated Core 1.19.2-0.6.4.730 `sophisticatedcore-1.19.2-0.6.4.730.jar` (both)
- Spice of Life: Apple Pie Edition `solapplepie-1.19.2-2.2.0.jar` (both)
- Spyglass Improvements: Zoom, Quick Access... [Fabric/NeoForge] 1.5.12+mc1.19+forge `spyglass_improvements-forge-1.5.12+mc1.19+forge.jar` (client)
- Stardew Fishing v1.3 (1.19.2) `stardew_fishing-1.19.2-1.3-all.jar` (both)
- Storage Delight (Forge & Neoforge) 25.12.09-1.19-forge `storagedelight-25.12.09-1.19-forge.jar` (both)
- Storage Drawers 11.4.1 `StorageDrawers-1.19.2-11.4.1.jar` (both)
- SuperMartijn642's Config Lib 1.1.8-forge-mc1.19 `supermartijn642configlib-1.1.8-forge-mc1.19.jar` (both)
- SuperMartijn642's Core Lib 1.1.21-forge-mc1.19.2 `supermartijn642corelib-1.1.21-forge-mc1.19.2.jar` (both)
- Supplementaries 1.19.2-2.4.20 `supplementaries-1.19.2-2.4.20.jar` (both)
- Supplementaries Squared `suppsquared-1.19.2-1.1.1.jar` (both)
- Survivality 1.2.4 `survivality-1.19.2-1.2.4-forge.jar` (both)
- SwingThroughGrass `swingthroughgrass-1.19.2-1.10.0.jar` (both)
- Tactical Fishing 1.2 `TacticalFishing-1.19.x2-v1.2.jar` (both)
- Tax Free Levels 1.4.23 `TaxFreeLevels-1.4.23-forge-1.20.1.jar` (server)
- Tectonic 2.3.5a `tectonic-forge-1.19-2.3.5a.jar` (both)
- Terralith 2.3.12 `Terralith_1.19.3_v2.3.12.jar` (both)
- The Dreamcatcher 1.0.0 `dreamcatcher mod.jar` (both)
- The Missing Villages 3.0.0-1.19.2 `themissingvillages-3.0.0-1.19.2-beta.jar` (both)
- Towns and Towers 1.10.0 `Towns-and-Towers-v.1.10-_FORGE-1.19.2_.jar` (both)
- Trade Cycling [FORGE][1.19.2] Trade Cycling 1.19.2-1.0.15 `trade-cycling-forge-1.19.2-1.0.15.jar` (both)
- Trash Cans 1.0.18-forge-mc1.19 `trashcans-1.0.18-forge-mc1.19.jar` (both)
- TrashSlot `trashslot-forge-1.19.2-12.1.0.jar` (both)
- Twigs 3.0.1 `twigs-forge-1.19.2-3.0.1.jar` (both)
- Via Romana: Infrastructure-Driven Fast Travel 1.1.3 `Via_Romana-1.19.2-1.1.3.jar` (both)
- Villagers Sell Animals [NoCube's Mods] `nocube's_villagers_sell_animals_1.2.1_forge_1.19.2.jar` (both)
- Vintage KubeJS 1.19.2-1.0.0rc-2 `vintage_kubejs-1.19.2-1.0.0rc-2.jar` (both)
- Wares 1.2.9 `wares-1.19.2-1.2.9.jar` (both)
- Waystones `waystones-forge-1.19.2-11.4.2.jar` (both)
- Waystones Teleport Pets 1.2 `WaystonesTeleportPets-1.19.2-1.19.4--1.2.jar` (both)
- WorldEdit 7.2.12 (Forge/Fabric for MC 1.19.2) `worldedit-mod-7.2.12.jar` (both)
- WTHIT Forge Edition forge-5.36.1 `wthit-1.19.2-forge-5.36.1.jar` (both)
- WTHIT Harvestability 2.1.0 `wthitharvestability-mc1.19.2-2.1.0.jar` (client)
- Xaero Zoomout 1.1.0 `XaeroZoomout-Forge-1.19-1.1.0.jar` (client)
- Xaero's Minimap forge-1.19.2-25.3.10 `xaerominimap-forge-1.19.2-25.3.10.jar` (client)
- Xaero's Minimap & World Map - Waystones Compatibility [Forge & NeoForge] 1.1 `xaeros_waystones_compatibility-1.1 - 1.19.2.jar` (both)
- Xaero's World Map forge-1.19.2-1.40.11 `xaeroworldmap-forge-1.19.2-1.40.11.jar` (client)
- YUNG's API (Forge/NeoForge) 1.19.2-Forge-3.8.10 `YungsApi-1.19.2-Forge-3.8.10.jar` (both)
- YUNG's Better Desert Temples (Forge/NeoForge) 1.19.2-Forge-2.2.2 `YungsBetterDesertTemples-1.19.2-Forge-2.2.2.jar` (both)
- YUNG's Better Dungeons (Forge/NeoForge) 1.19.2-Forge-3.2.2 `YungsBetterDungeons-1.19.2-Forge-3.2.2.jar` (both)
- YUNG's Better End Island (Forge/NeoForge) 1.19.2-Forge-1.0 `YungsBetterEndIsland-1.19.2-Forge-1.0.jar` (both)
- YUNG's Better Jungle Temples (Forge/NeoForge) 1.19.2-Forge-1.0.1 `YungsBetterJungleTemples-1.19.2-Forge-1.0.1.jar` (both)
- YUNG's Better Nether Fortresses (Forge/NeoForge) 1.19.2-Forge-1.0.6 `YungsBetterNetherFortresses-1.19.2-Forge-1.0.6.jar` (both)
- YUNG's Better Strongholds (Forge/NeoForge) 1.19.2-Forge-3.2.0 `YungsBetterStrongholds-1.19.2-Forge-3.2.0.jar` (both)
- YUNG's Better Witch Huts (Forge/NeoForge) 1.19.2-Forge-2.1.0 `YungsBetterWitchHuts-1.19.2-Forge-2.1.0.jar` (both)
- YUNG's Extras (Forge/NeoForge) 1.19.2-Forge-3.1.0 `YungsExtras-1.19.2-Forge-3.1.0.jar` (both)
- Yuushya Modelling 2.2.0 `yuushya-modelling-1.19.2-forge-2.2.0.jar` (both)

</details>

<details><summary>kubejs/: 687 added, 0 changed, 0 removed</summary>

*added (687)*

- `README.txt`
- `assets/create/textures/block/redstone_antenna.png`
- `assets/create/textures/block/redstone_antenna_powered.png`
- `assets/create/textures/block/redstone_bridge.png`
- `assets/create/textures/block/redstone_bridge_powered.png`
- `assets/create/textures/item/crushed_raw_aluminum.png`
- `assets/create_compressed/textures/block/crushed_aluminium_pile.png`
- `assets/creativeengineering/blockstates/` (39 files)
- `assets/creativeengineering/lang/en_us.json`
- `assets/creativeengineering/models/block/` (39 files)
- `assets/creativeengineering/models/item/` (76 files)
- `assets/creativeengineering/textures/block/dark_fire_0.png`
- `assets/creativeengineering/textures/block/dark_fire_0.png.mcmeta`
- `assets/creativeengineering/textures/block/dark_fire_1.png`
- `assets/creativeengineering/textures/block/dark_fire_1.png.mcmeta`
- `assets/creativeengineering/textures/block/layered_obsidian.png`
- `assets/creativeengineering/textures/block/prepared_flesh_block.png`
- `assets/creativeengineering/textures/block/coil_block/` (20 files)
- `assets/creativeengineering/textures/block/dust_pile/` (16 files)
- `assets/creativeengineering/textures/block/plate_block/` (11 files)
- `assets/creativeengineering/textures/entity/shield/` (14 files)
- `assets/creativeengineering/textures/item/` (65 files)
- `assets/creativeengineering/textures/mob_effect/magic_resistance.png`
- `assets/dragonloot/textures/item/dragon_scale.png`
- `assets/kubejs/lang/en_us.json`
- `assets/kubejs/textures/block/example_block.png`
- `assets/kubejs/textures/item/example_item.png`
- `assets/lightmanscurrency/textures/item/coin_copper.png`
- `assets/lightmanscurrency/textures/item/coin_diamond.png`
- `assets/lightmanscurrency/textures/item/coin_emerald.png`
- `assets/lightmanscurrency/textures/item/coin_gold.png`
- `assets/lightmanscurrency/textures/item/coin_iron.png`
- `assets/lightmanscurrency/textures/item/coin_netherite.png`
- `assets/minecraft/texts/minecraftsplashes.txt`
- `assets/minecraft/texts/splashes.txt`
- `assets/nock_enough_arrows/textures/item/redstone_torch_arrow.png`
- `assets/nock_enough_arrows/textures/item/soul_torch_arrow.png`
- `assets/pneumaticcraft/lang/en_us.json`
- `assets/pneumaticcraft/textures/block/compressed_iron_block.png`
- `assets/ponderjs_generated/lang/en_us.json`
- `assets/supplementaries/blockstates/speaker_block.json`
- `assets/supplementaries/models/block/speaker_block.json`
- `assets/supplementaries/models/custom/speaker.json`
- `assets/vintageimprovements/textures/item/aluminum_spring.png`
- `assets/vintageimprovements/textures/item/constantan_spring.png`
- `assets/vintageimprovements/textures/item/small_aluminum_spring.png`
- `assets/vintageimprovements/textures/item/small_constantan_spring.png`
- `client_scripts/armor_effects_tooltip.js`
- `client_scripts/fluid_jei.js`
- `client_scripts/jeichanges.js`
- `client_scripts/namechanges.js`
- `client_scripts/potion_buckets.js`
- `client_scripts/texture_tool.js`
- `client_scripts/tool_effects_tooltip.js`
- `client_scripts/tooltips.js`
- `config/client.properties`
- `config/common.properties`
- `config/dev.properties`
- `generation_scripts/sawmill.js`
- `generation_scripts/recipes/sawmill/` (211 files)
- `generation_scripts/worldgen/` (9 files)
- `generation_scripts/worldgen/biome_baselines/minecraft__overworld.json`
- `generation_scripts/worldgen/biome_baselines/minecraft__the_end.json`
- `generation_scripts/worldgen/biome_baselines/minecraft__the_nether.json`
- `recipes/food/bonemeal_broth.json`
- `recipes/recycle/sawmill_recycle_door.json`
- `recipes/recycle/sawmill_recycle_slab.json`
- `recipes/recycle/sawmill_recycle_stairs.json`
- `server_scripts/_lib/` (17 files)
- `server_scripts/cooking/chocolate.js`
- `server_scripts/cooking/cooking.js`
- `server_scripts/cooking/cultivated_meat.js`
- `server_scripts/cooking/golden.js`
- `server_scripts/economy/coin.js`
- `server_scripts/economy/trades.js`
- `server_scripts/engineering/` (24 files)
- `server_scripts/equipment/archery.js`
- `server_scripts/equipment/bullets.js`
- `server_scripts/equipment/dragon_autorepair.js`
- `server_scripts/equipment/end_gear.js`
- `server_scripts/equipment/shield.js`
- `server_scripts/equipment/tipped_arrows.js`
- `server_scripts/loot/` (10 files)
- `server_scripts/magic/` (9 files)
- `server_scripts/metal/metal_base_materials.js`
- `server_scripts/metal/metal_building.js`
- `server_scripts/metal/misc_metal_crafts.js`
- `server_scripts/other/dead_recipe_removal.js`
- `server_scripts/other/harvest.js`
- `server_scripts/other/misc_mods.js`
- `server_scripts/tags/` (10 files)
- `server_scripts/wood/ash.js`
- `server_scripts/wood/bark.js`
- `server_scripts/wood/sawdust.js`
- `server_scripts/wood/wood_types.js`
- `server_scripts/worldgen/biome_merges.js`
- `server_scripts/worldgen/biome_overrides.js`
- `server_scripts/worldgen/structure_overrides.js`
- `startup_scripts/_lib/block_dict.js`
- `startup_scripts/_lib/fluid_dict.js`
- `startup_scripts/_lib/item_dict.js`
- `startup_scripts/_lib/removed_items.js`
- `startup_scripts/changes/transition_items.js`
- `startup_scripts/cooking/potions.js`
- `startup_scripts/func/` (10 files)
- `startup_scripts/items/` (13 files)
- `startup_scripts/magic/magic_resistance.js`
- `startup_scripts/todo/create_ie_dict_additions.txt`
- `startup_scripts/todo/not_in_dict.txt`
- `startup_scripts/todo/recipes_to_remove.txt`
- `startup_scripts/todo/remove_list.txt`

</details>

<details><summary>config/: 305 added, 0 changed, 0 removed</summary>

*added (305)*

- `/` (140 files)
- `Advancedperipherals/general.toml`
- `Advancedperipherals/metaphysics.toml`
- `Advancedperipherals/peripherals.toml`
- `Advancedperipherals/world.toml`
- `YungsExtras/README.txt`
- `YungsExtras/forge-1_19/README.txt`
- `YungsExtras/forge-1_19/wishing_wells.json`
- `ae2/client.json`
- `ae2/common.json`
- `alexsmobs/` (88 files)
- `better_beacons/better_beacons-common.toml`
- `betterdeserttemples/README.txt`
- `betterdeserttemples/forge-1_19/README.txt`
- `betterdeserttemples/forge-1_19/armorstands.json`
- `betterdeserttemples/forge-1_19/itemframes.json`
- `betterfortresses/README.txt`
- `betterfortresses/forge-1_19/README.txt`
- `betterfortresses/forge-1_19/itemframes.json`
- `betterstrongholds/README.txt`
- `betterstrongholds/forge-1_19/README.txt`
- `betterstrongholds/forge-1_19/armorstands.json`
- `betterstrongholds/forge-1_19/itemframes.json`
- `betterstrongholds/forge-1_19/ores.json`
- `betterstrongholds/forge-1_19/rareblocks.json`
- `chunky/config.json`
- `ftbquests/quests/chapter_groups.snbt`
- `ftbquests/quests/data.snbt`
- `ftbquests/quests/chapters/` (10 files)
- `ftbquests/quests/reward_tables/` (12 files)
- `jei/ingredient-list-mod-sort-order.ini`
- `jei/recipe-category-sort-order.ini`
- `lightmanscurrency/MasterCoinList.json`
- `lightmanscurrency/PersistentTraders.json`
- `oreexcavation/block_groups.json`
- `oreexcavation/shapes.json`
- `oreexcavation/tool_overrides.json`
- `pneumaticcraft/MicromissileDefaults.cfg`
- `pneumaticcraft/PneumaticArmorHUDLayout.cfg`
- `pneumaticcraft/ProgrammingPuzzleBlacklist.cfg`
- `pneumaticcraft/thirdparty.cfg`
- `sereneseasons/fertility.toml`
- `sereneseasons/seasons.toml`
- `towns_and_towers/structure_enable_or_disable.json5`
- `towns_and_towers/structure_rarity.json5`
- `towns_and_towers/resources/t_and_t_waystones_patch_1.19.2.zip`
- `trades/lightmanscurrency/custom_banker_trades.json`
- `trades/lightmanscurrency/custom_cashier_trades.json`
- `trades/lightmanscurrency/custom_wandering_trader_trades.json`
- `vinery/config.json5`
- `waila/blacklist.json5`
- `waila/debug.json5`
- `waila/plugin_toggle.json5`
- `waila/waila_plugins.json5`
- `waila/extra/energy_blacklist.json5`
- `waila/extra/fluid_blacklist.json5`
- `waila/extra/item_blacklist.json5`
- `waila/extra/progress_blacklist.json5`
- `worldedit/worldedit.properties`

</details>

defaultconfigs/: 1 added, 0 changed, 0 removed:

*added (1)*

- `brickhopper-server.toml`

resourcepacks/: 27 added, 0 changed, 0 removed:

*added (27)*

- `/` (27 files)

shaderpacks/: 16 added, 0 changed, 0 removed:

*added (16)*

- `/` (14 files)
- `presets/BSL_v8.2.01-custom-high.txt`
- `presets/ComplementaryReimagined_r1.4-custom.txt`

### Removals

None.

