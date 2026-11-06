# Sweep E: resources

Audit run 2026-11-01, read-only (only this file written). Covers `development/patcherMod/src/main/resources/`, the rest
of `development/patcherMod/`, `kubejs/data/` and `kubejs/assets/`. Targets use the vocabulary of
[README.md](README.md); `?` marks a doubtful row with its note. Rule 3.1 applies throughout: every id and every lang
key keeps its `creativeengineering:` name and `*.creativeengineering.*` key whichever jar ships the file; only the jar
that carries the file changes. Sizes are `du -sh` (4K blocks), counts are files.

`PMR` = `development/patcherMod/src/main/resources/`.

## 1. patcherMod resources (`PMR`, 500 files, 3.6M)

### 1.1 Root files and META-INF

| path | files | what it is | target jar | note |
|---|---|---|---|---|
| `PMR/META-INF/mods.toml` | 1 | mod id, licence, mixin config, soft deps (create, itemphysic, ...) | Common | each new jar gets its own mods.toml; the optional-dependency list is split by which jar's mixins need it |
| `PMR/creativeengineering.mixins.json` | 1 | 289 mixin entries | Common (split) | sweep B assigns the entries; each jar gets its own json (rule 3.6) |
| `PMR/pack.mcmeta` | 1 | pack format 9/10 | Common | every jar needs a copy |
| `PMR/credit.txt` | 1 | LGPL notice for CreateNumismatics textures | Common ? | stays with whichever jar ends up carrying the borrowed textures; no current resource is traced to it, check before dropping |
| `PMR/waila_plugins.json` | 1 | 15 WTHIT plugin entrypoints | Common (split by entry) | entries that only show one content jar's data move with it: `run_speed` (BlockRunner tiers, Engineering `surface/`), `crop_seasons` (Calendar), `campfire` (CD `campfire/`), `coaster` (CB), `sheetmetal_tank` (Engineering), `kitchen_station` (CD, Applied Cooking), `remainder_buffer` (Engineering ?, remainder buffers of machines); `block_model_icon`, `holder_contents`, `hive`, `state_lines`, `sheared`, `silk_touch`, `operator_blocks`, `pot_contents` stay Common |
| `PMR/creativeengineering/displaydelight_dishes.json` | 1 | Display Delight placed-block definitions (10 dishes) read by `compat/DisplayDelightDishes` | CD | drives the `placed_*` blockstates/models/loot below |

### 1.2 `assets/creativeengineering/` (173 files, 848K)

| path | files | what it is | target jar | note |
|---|---|---|---|---|
| `assets/creativeengineering/state_lines.json` | 1 | WTHIT state-line rules (servings, slices, eggs, charges, pickles) | Common | plan 2.4 names it; the rows it describes are other mods' blocks |
| `assets/creativeengineering/blockstates/placed_*.json` | 10 | Display Delight placed dishes | CD | generated set, one per dish in `displaydelight_dishes.json` |
| `assets/creativeengineering/blockstates/oven.json`, `powdered_sugar_block.json` | 2 | oven, powdered sugar block | CD | `oven/`, `food/PowderedSugarRegistry` |
| `assets/creativeengineering/blockstates/dark_fire.json` | 1 | Dark Fire (nether portal ignition, nether_portal.md) | Worldgen ? | `block/DarkFireBlock` + `dimension/PortalFailureHandler`; the plan draft lists `block/` under Engineering, but this block only serves the portal rule; one owner needed for dark fire + Flint and Terrasteel |
| `assets/creativeengineering/models/block/placed_*.json`, `oven.json`, `oven_on.json`, `powdered_sugar_block.json` | 13 | as above | CD | |
| `assets/creativeengineering/models/block/dark_fire_0.json`, `dark_fire_1.json` | 2 | Dark Fire models | Worldgen ? | see blockstate row |
| `assets/creativeengineering/models/block/moss_block_tinted.json`, `moss_carpet_tinted.json` | 2 | tint-indexed moss copies swapped in at bake time (`block/MossTint`, CEConfig `[moss]`) | Compat | vanilla-block visual tweak with no wider system; `block/` package is otherwise Engineering, so MossTint must move out with these |
| `assets/creativeengineering/models/block/flower_box/` | 3 | glow berries / glow lichen models for Supplementaries flower box and Another Furniture planter (`compat/PlantBoxModels`) | Compat | |
| `assets/creativeengineering/models/item/*_shield.json` | 7 | copper, gold, diamond, netherite, echo, felsic, dragon shields | Equipment | `shield/` |
| `assets/creativeengineering/models/item/oven.json`, `powdered_sugar_bucket.json` | 2 | | CD | |
| `assets/creativeengineering/models/item/wooden_bucket.json`, `wooden_water_bucket.json` | 2 | wooden buckets (`bucket/WoodenBucketItems`) | Engineering | |
| `assets/creativeengineering/models/item/creosote_soaked_sawdust.json`, `precision_sawblade.json` | 2 | `item/CreativeEngineeringItems` | Engineering | |
| `assets/creativeengineering/models/item/flint_and_terrasteel.json` | 1 | Dark Fire igniter | Worldgen ? | same owner as dark fire (registered in `item/CreativeEngineeringItems`, Engineering in the draft) |
| `assets/creativeengineering/textures/block/dark_fire_*` (png + mcmeta) | 4 | | Worldgen ? | |
| `assets/creativeengineering/textures/block/moss_block_tinted.png` | 1 | | Compat | |
| `assets/creativeengineering/textures/block/powdered_sugar_block.png` | 1 | | CD | |
| `assets/creativeengineering/textures/item/{creosote_soaked_sawdust,precision_sawblade,wooden_bucket,wooden_water_bucket}.png` | 4 | | Engineering | |
| `assets/creativeengineering/textures/item/flint_and_terrasteel.png` | 1 | | Worldgen ? | |
| `assets/creativeengineering/textures/item/powdered_sugar_bucket.png` | 1 | | CD | |
| `assets/creativeengineering/textures/gui/*_tint.png` | 12 | Detail Armor Bar tint sheets per armor set (bone, dragon, echo, elementium, faraday, felsic, manasteel, manaweave, prismarine, slime, terrasteel, wither) | Common | armor bar is Interface (plan 2.4); the sheets cover third-party sets too, so they are not one content jar's data |
| `assets/creativeengineering/textures/entity/{botania,buzzier_bees,immersiveengineering,minecolonies}/` | 34 | banner-pattern textures re-stitched for Bedspreads (`compat/BannerPatternAtlasCompat`, `BedspreadsBannerPatternMixin`) | Compat | |
| `assets/creativeengineering/textures/entity/elytra_banner/`, `elytra_banner_low/` | 64 | the same patterns for Customizable Elytra (`CustomizableElytraBannerPatternMixin`) | Compat | |
| `assets/creativeengineering/lang/en_us.json` | 1 (220 keys) | mixed; split by key group below | Common (split) | keys keep their names (rule 3.1); each jar ships the keys of its own items, config and tooltips |

Lang key groups in `PMR/assets/creativeengineering/lang/en_us.json`:

| key group | keys | target jar | note |
|---|---|---|---|
| `item.creativeengineering.*_shield` | 7 | Equipment | |
| `effect.creativeengineering.compound.parts`, `effect.creativeengineering.part.*` | 5 | CB | `potion/CompoundEffects` |
| `creativeengineering.jei.automated_potion_mixing`, `.lingerification`, `.tipped_arrows`, `.potion_buckets` | 4 | CB | JEI pages of CB content |
| `creativeengineering.jei.seasoning`, `.canning`, `.can_uses`, `.feast_plating*`, `jei.creativeengineering.baking*` | 13 | CD | |
| `creativeengineering.jei.world.*` | 18 | Common | JEI world tabs (plan 2.4); `.world.salt`, `.world.campfire`, `.world.sample_syringe` show CD rows, `.world.mob_bottling`, `.world.mob_transformation` show Worldgen fauna rows; still one Common tab set |
| `create.recipe.fan_dragon_breathing*` | 2 | Engineering | `fan/` |
| `creativeengineering.surface.tier.*`, `creativeengineering.tooltip.walk_speed.*`, `creativeengineering.wthit.run_speed*` | 35 | Engineering | `surface/` BlockRunner tiers and camo speed |
| `creativeengineering.wthit.coaster*` | 3 | CB | |
| `creativeengineering.wthit.remainders.*` | 3 | Engineering ? | remainder buffer of machines; confirm with sweep A's owner of WthitRemainderBuffer |
| `creativeengineering.wthit.contents*`, `.hive_bees`, `.sheared*`, `.not_growing`, `.silk_touch.*`, `.state.*`, `.pot_contains`, `.rooted_dirt`, `.yes`, `.no`, `block.creativeengineering.rich_pot` | 22 | Common | WTHIT interface lines |
| `creativeengineering.wthit.campfire.rain*` | 4 | CD | `campfire/` |
| `creativeengineering.wthit.crop_season_here.*`, `creativeengineering.crop_season.*`, `tooltip.creativeengineering.calendar.*`, `desc.seasonhud.season.*` | 30 | Calendar | `seasons/`, `time/`, SeasonHUD mixin |
| `config.waila.plugin_creativeengineering*` | 15 | Common (split) | one key per plugin; follows the `waila_plugins.json` entry |
| `creativeengineering.changelog.*` | 5 | Common | changelog viewer |
| `creativeengineering.can.servings`, `.lunchbox.slots`, `.seasoning.*`, `.hoe_harvest.*`, `tooltip.creativeengineering.intoxicating_drink`, `block/item/container.creativeengineering.oven*`, `powdered_sugar_*`, `block.creativeengineering.placed_*` | 28 | CD | intoxicating_drink: CB takes `food/IntoxicatingDrinkTooltip` (creative_brewing.md phase 2), so that one key is CB |
| `item.creativeengineering.wooden_*bucket` | 2 | Engineering | |
| `creativeengineering.vein_undo.*` | 2 | Engineering | `excavation/VeinUndo` |
| `chat.creativeengineering.named_mob.title`, `.monster_box.sneak` | 2 | Worldgen | fauna (`mobs/`) |
| `creativeengineering.mca_eyes.*` | 12 | Compat | MCA comb eye colour mixin |
| `key.creativeengineering.stepstool` | 1 | Equipment | `stepstool/` |
| `generator.creativeengineering.flower_test` | 1 | Devtools | flower test world preset |
| `waila.h12y.tier.creativeengineering.copper` | 1 | ? | no `h12y` mod in `mods/` and no Java reference; stale key, candidate for deletion rather than a target |

### 1.3 `assets/minecraft/` (6 files, 988K)

| path | files | what it is | target jar | note |
|---|---|---|---|---|
| `assets/minecraft/textures/gui/title/background/panorama_0..5.png` | 6 | title-screen panorama override | Common ? | pack branding, no Java; could equally be a `kubejs/assets` resource (pack) instead of any jar. Largest single asset block (972K) |

### 1.4 `data/` (284 files, 1.6M)

| path | files | what it is | target jar | note |
|---|---|---|---|---|
| `data/creativeengineering/recipes/sawmill/` | 211 | IE sawmill recipes for every wood set, generated by `generation_scripts/sawmill.js` and copied into the jar (build_deploy.py 277) | Engineering | generator output root must move with it |
| `data/creativeengineering/recipes/lingerification_list.json` | 1 | `fluidbrewing/Lingerification` | CB | |
| `data/creativeengineering/damage_tags/` | 32 | pack damage-type tags (`damage/DamageFlag`, shield categories) | Equipment | `shield/` and `damage/` read them; CEConfig and `CreativeEngineering.java` register the folder |
| `data/creativeengineering/dimension/` | 11 | sky and hub dimensions (autumnsky, springsky, summersky, wintersky, hub, shield, desert, flowergrove, flowerhills, flowerridge, yellowstone) | Worldgen | referenced only from data and docs (weather_by_biome.md 505, spawns_by_biome.md); no Java constant |
| `data/creativeengineering/dimension_type/hub_type.json` | 1 | | Worldgen | |
| `data/creativeengineering/worldgen/noise_settings/` | 3 | y32plus, y40plus, y208void (328K) | Worldgen | used by the dimensions above |
| `data/creativeengineering/worldgen/world_preset/flower_test.json` + `data/minecraft/tags/worldgen/world_preset/normal.json` | 2 | flower test preset listed in the world-type menu | Devtools | test world (flower_pots.md); the `minecraft` tag file only exists to show the preset |
| `data/creativeengineering/worldgen/density_function/` | 0 | empty folder | - | delete |
| `data/creativeengineering/loot_tables/blocks/` | 12 | oven, powdered sugar block, 10 placed dishes | CD | |
| `data/creativeengineering/tags/blocks/fan_processing_catalysts/dragon_breathing.json` | 1 | `fan/` | Engineering | |
| `data/creativeengineering/tags/items/physics/` | 4 | ItemPhysic fireproof/lavaproof/floats/sinks | Compat | `itemphysic/` |
| `data/forge/tags/items/sawblades.json` | 1 | sawblade tag (precision sawblade) | Engineering | |
| `data/displaydelight/tags/items/displayable.json` | 1 | Display Delight displayable foods | CD | |
| `data/tmted/recipes/crusher/` | 2 | Engineer's Delight crusher flour recipes (wheat, bale) | CD ? | food recipe for an IE addon; CD by the food routing rule, Engineering if the owner reads "IE machine recipe" |
| `data/tmted/recipes/crushing/`, `data/minecraft/tags/blocks/mineable/` | 0 | empty folders | - | delete |

### 1.5 `kubejs_startup/creativeengineering/func/` (3 files, 152K)

| path | files | what it is | target jar | note |
|---|---|---|---|---|
| `kubejs_startup/creativeengineering/func/neoCraftHelper.js` | 1 (2518 lines) | `global.NCH` | Common | rule 3.4 |
| `kubejs_startup/creativeengineering/func/tag_helper.js` | 1 (269) | `global.tags` | Common | |
| `kubejs_startup/creativeengineering/func/compost.js` | 1 (395) | compost helper | Common | plan 2.4 keeps it shared although every caller is food |

## 2. `development/patcherMod/` outside `src/main` (git-ignored unless noted)

| path | files | what it is | target | note |
|---|---|---|---|---|
| `src/generated/resources/` | 0 | empty | - | |
| `src/main/java/` | 629 | sweep A, B, C, D | - | |
| `build/` | 1490 (43M) | Gradle output | - | ignored |
| `libs/` | 78 (226M) | compile-only dependency jars | Common (build) | ignored; each new jar's `build.gradle` lists only the libs it compiles against |
| `jar_archive/` | 34 (72M) | displaced jars | - | ignored |
| `run/`, `run-data/` | 0 | empty run dirs | - | ignored |
| `working/` | 3 (40K) | `FluidMemoryEssence.java`, `MemoryStickItem.java`, `xpfluidbridge/`, `xpfluidbridge.zip`: parked, uncompiled code | ? | tracked scratch; not in any jar; owner to keep or delete |
| `gradle/` | 2 | wrapper | - | |
| `build.gradle`, `gradle.properties`, `gradlew*` | 5 | build | Common | template for the new jars' build files |
| `changelog.txt` (635 lines), `CREDITS.txt`, `NOTICE-commonmark.txt`, `LICENSE.txt` | 4 | dev changelog, credits, commonmark notice (changelog viewer), GPLv3 | Common | NOTICE-commonmark follows the changelog viewer; LICENSE is copied to every jar |

## 3. `kubejs/data/` (1998 files, 15M)

### 3.1 Structure datapacks (49 namespaces, 629 files, 7.3M)

| namespace | files | what it is | target or pack |
|---|---|---|---|
| `ancient_enchantment_temple`, `animal_barn`, `antiquetradingship`, `bighorsestable`, `desert_village_temple`, `desert_watchtower`, `forest_ruins`, `forest_watchtower`, `forgotten_church`, `gothic_church`, `illager_siege_weapons`, `illager_viking_longship`, `illagerwarship`, `llama_stable`, `mini_clock_tower`, `mysticrift_pillagers_towers_aris`, `old_illager_statue`, `oldvillagerstatue`, `plains_villager_tents`, `saloon`, `skyvillages`, `small_cat_statues`, `smallhorsestable`, `small_viking_villager_house`, `small_villager_greenhouse`, `snowy_tents`, `villager_archer_camp`, `villager_butcher_house`, `villager_cartographer_house`, `villager_circus_tent`, `villager_cleric_house`, `villager_country_house`, `villager_farmer_house`, `villager_fishermans_hut`, `villager_fish_shop`, `villager_fletcher_house`, `villager_flower_house`, `villager_forge`, `villager_gold_washing_facility`, `villager_rafts`, `villager_tannery`, `villager_windmill` | 5 to 69 each (474 total) | structure nbt, template pools, structure/structure_set json, loot tables | Worldgen |
| `campsite_structures` | 24 | Earth-fill camps: template pools plus `forge/biome_modifier` per profession | Worldgen |
| `eugene` | 69 | Eugene's structures with mcfunctions (`functions/`, `predicates/`, `tags/worldgen/biome/has_structure`) | Worldgen |
| `watchtower_building` | 54 | structures, loot, and `advancements/watchtower_found.json` | Worldgen (advancement file: delete, pack drops advancements) |
| `ipo` | 6 | Illager Pillager Outpost rework: base plate nbt + template pools | Worldgen |
| `minecraft` | 13 | pillager outpost base plates and tents, `tags/functions/{tick,load}` (Eugene), copper ore loot, `noise_settings/{overworld,large_biomes}`, `patch_{melon,pumpkin,berry_bush}` | Worldgen |
| `immersiveengineering`, `immersivepetroleum` | 2 + 2 | multiblock template nbt (arcfurnace, bucket wheel, pumpjack, coker) | Engineering ? (multiblock shape overrides for IE machines, not structures; Worldgen only if they are placed-structure donors) |

### 3.2 Other worldgen and vegetation namespaces

| namespace | files | what it is | target or pack |
|---|---|---|---|
| `terralith` | 52 | configured/placed feature overrides (trees, grass, skylands) | Worldgen |
| `geophilic_reforged` | 14 | grass and fallen tree features | Worldgen |
| `tectonic` | 1 | density function override | Worldgen |
| `create_dd` | 1 | striated eroded overworld feature | Worldgen |
| `upgrade_aquatic` | 7 | driftwood placed features, river trees | Worldgen |
| `bushierflowers` | 5 | grown flower biome modifiers | Worldgen |
| `fireflybushport` | 1 | firefly bush biome modifier | Worldgen |
| `immersive_weathering` | 11 | Upgrade Aquatic coral block growths | Worldgen ? (IW soil/plant growth is a worldgen-side system in memory `iw-soil-wild-plants`; Compat if read as an IW tweak) |
| `fruitsdelight`, `vinery`, `rusticdelight`, `neapolitan` | 4 + 6 + 3 + 2 | wild crop configured features and biome modifiers | CD (wild crops system lives in Creative Delight since 10-25; creative_delight.md 1100 to 1108) |
| `creativedelight/worldgen/` | 29 | wild crop configured/placed features | CD |
| `more_mooblooms/moobloom_spawns/creativeengineering.json` | 1 | moobloom spawn rules | CD (moreMooblooms member) |
| `felsic_gear/loot_tables/blocks/felsic_block.json` | 1 | felsic block drop | Equipment |
| `lightmanscurrency/loot_tables/loot_addons/` | 9 | coin drops per entity/boss tier | Worldgen (`loot/`) |
| `oceanic_delight/loot_tables/gameplay/` | 2 | fishing net on kelp/sea grass | CD (creative_delight.md 154) |
| `salt/loot_tables/cauldron_evaporation/` | 3 | salt from cauldron evaporation | CD ? (salt is a food ingredient system; Compat if read as a Salt-mod tweak) |
| `moonlight/moonlight/soft_fluids/`, `supplementaries/moonlight/soft_fluids/`, `vintagedelight/moonlight/soft_fluids/` | 2 + 7 + 2 | Supplementaries jar/goblet soft fluids (milk, lava, milkshakes, relish) | CD |
| `vintagedelight/worldgen/configured_feature/patch_salt.json` | 1 | wild salt patch | CD (with the wild crops) |

### 3.3 `kubejs/data/creativeengineering/` (1095 files, 5.2M)

| path | files | what it is | target or pack |
|---|---|---|---|
| `paintings/<ns>/` | 950 (4.5M) | Immersive Paintings datapack: every painting of 14 painting mods as PNG + JSON (immersive_paintings.md) | Compat |
| `moonlight/soft_fluids/` | 66 | soups, stews and cups as Supplementaries soft fluids | CD |
| `worldgen/configured_feature/wild/`, `worldgen/placed_feature/wild/` | 5 + 23 | pack wild crops | CD (same system as `creativedelight/worldgen`) |
| `worldgen/configured_feature/felsic_ore.json`, `worldgen/placed_feature/felsic_ore_*.json` | 7 | felsic ore in End biomes | Worldgen (Equipment owns the gear, Worldgen the ore placement) |
| `worldgen/placed_feature/meta/` | 13 | meta-structure placed features | Worldgen |
| `worldgen/placed_feature/driftwood_land/` | 3 | | Worldgen |
| `worldgen/structure/sky_island.json`, `worldgen/template_pool/sky_island.json` | 2 | sky islands | Worldgen |
| `forge/biome_modifier/felsic_ore_*`, `driftwood_land_*`, `azaleabloom_lush_caves`, `nether_worldgen/scarlet_mountains_crimson` | 11 | | Worldgen |
| `forge/biome_modifier/moobloom_cows.json` | 1 | moobloom cow spawns | CD (moreMooblooms) |
| `forge/biome_modifier/vintage_wild_ghost_peppers.json` | 1 | wild crop | CD |
| `loot_tables/gameplay/fishing_net/` | 3 | coral, ocean treasure, junk | CD (creative_delight.md 154) |
| `loot_tables/misc/monster_box_reward.json` | 1 | `mobs/MonsterBoxVault` reward | Worldgen (fauna) |
| `recipes/sawmill/` | 5 | hand-written sawmill recipes (recycle slab/stairs/door, layered obsidian, rose quartz) | Engineering |
| ~~`curios/slots/{face,feet,hands}.json`, `curios/entities/player_hands_face.json`~~ (deleted 2026-11-01: never loaded on Curios 5.1.x; slots now IMC in patcherMod `compat/CuriosSlots`, moves with the Equipment Java) | 4 | Curios slot definitions | Equipment ? (`backpackskin/BackpackSkinCurios` is Equipment; `compat/CornDelightCurios` cob pipe is CD; one jar must own the slot files, Equipment suggested) |
| `creativedelight/kubejs/`, `creativedelight/cup_conversion/` | 108 + 1 | CD server scripts and cup conversion data | CD (sweep F for the scripts) |
| `creativebrewing/kubejs/_core/cb_server.js` | 1 | CB server core | CB (sweep F) |

No `kubejs/data/creativeengineering/kubejs/` folder exists yet; the `creativeengineering` namespace holds only data.
Pack server scripts live in `kubejs/server_scripts/` (sweep F).

## 4. `kubejs/assets/` (882 files, 7.2M)

### 4.1 `kubejs/assets/creativeengineering/` (664 files, 5.9M)

| path | files | what it is | target or pack |
|---|---|---|---|
| `jei_world/` | 25 (3.1M) | JEI world-tab illustrations (dyeing 2.3M, soaping, waxing, ...) | Common (JEI world tabs are Interface) |
| `changelog/*.md` | 16 | player changelog per version + `unreleased.md` | Common (changelog viewer) |
| `brewinandchewin/coaster/` | 206 (844K) | per-food coaster models for Brewin's coaster, generated by `gen_coaster_models.py` | CB (WthitCoaster and the generator's `outRoot` move to CB in creative_brewing.md phase 1 and 2; the models depict CD foods) |
| `lang/en_us.json` | 1 (33 keys) | effect renames for 17 mods (`effect.<mod>.*`), 2 `curios.identifier.*` | CB (effects); `curios.identifier.*` Equipment |
| `textures/mob_effect/magic_resistance.png` | 1 | Magic Resistance effect icon | CB |
| `textures/slot/empty_face_slot.png` | 1 | Curios face slot icon | Equipment (with the curios slots) |
| `textures/entity/shield/` | 14 | shield base textures per tier | Equipment |
| `textures/entity/canned_golem/` | 15 | dyed canned golem skins (`compat/DyeableCannedGolem`) | CD (cans) |
| `textures/block/coil_block/`, `plate_block/`, `dust_pile/`, `layered_obsidian.png` | 20 + 11 + 16 + 1 | coils, plates, dust piles, layered obsidian | Engineering |
| `textures/block/culture_block/`, `dough_block/` | 7 + 6 | cultures, dough blocks | CD |
| `textures/block/dark_fire_*` | 4 | duplicate of the PMR dark fire textures | Worldgen ? (same owner as dark fire; one copy should go) |
| `textures/block/prepared_flesh_block.png`, `weathered_cardboard_box.png` | 2 | Just Rotten Flesh to Leather block, Wares cardboard | CD ? (prepared flesh is a food-chain item), Compat (cardboard) |
| `textures/item/` | 115 (468K) | mixed, split by item below | split |
| `models/item/`, `models/block/`, `blockstates/` | 97 + 53 + 53 | mixed, split by item below | split |

Item-level split of `textures/item`, `models/item`, `models/block`, `blockstates` (names are `creativeengineering:` ids):

| item or block group | approx files (tex/model/blockstate) | target or pack |
|---|---|---|
| `component_*` (13), `wirecoil_*` (9), `*_wire` (7), `*_grit` (3), `zinc_plate`, `spool`, `transmitter`, `amethyst_dust`, `amethyst_lens`, `rose_gold_nugget`, `rose_gold_rod`, `wood_pellets`, `coil_*` (10 model + 10 blockstate), `*_plate_block` (11 x 3), `*_dust_pile` (16 x 3), `layered_obsidian` | about 190 | Engineering |
| `canned_*` (24), `culture_*` (7) and `*_culture_block` (8 x 3), `*dough_block` (6 x 3), `cookie_dough`, `cornmeal`, `corncob`, `corn_popsicle`, `cheese_toast`, `glowing_calamari*` (3), `pickled_sea_pickle`, `baking_sheet`, `cake_pan`, `pie_tin`, `mortar_and_pestle`, `powdered_sugar`, `frosting`, `seasoned` + `seasoned/` (10), `cob_pipe*` (3), `sample_syringe`, `prepared_flesh_block`, `*_art_notice.txt` (3) | about 110 | CD |
| `potion_bucket`, `splash_potion_bucket`, `lingering_potion_bucket` (+ `_liquid` layers) | 9 | CB |
| `air_brake`, `arrow_fletching`, `echo_{axe,hoe,pickaxe,shovel}` (+ mcmeta), `felsic_scrap` | 11 | Equipment |
| `weathered_cardboard_box` | 3 | Compat |

### 4.2 Other `kubejs/assets` namespaces

| namespace | files | what it is | target or pack |
|---|---|---|---|
| `beautify` | 51 | Beautify hanging pot and flower pot textures (flower_pots.md) | Compat |
| `supplementaries` | 3 | speaker block model override | Compat |
| `lightmanscurrency` | 6 | coin textures | Compat |
| `wares` | 5 | cardboard box and package textures | Compat |
| `prettypipes/lang` | 1 | Pretty Pipes name tweaks | Compat (tooltip mixin is Common; lang text is the Compat tweak) |
| `create` | 6 | redstone bridge/antenna (4), `crushed_raw_aluminum` (Engineering), `dough` (CD) | Engineering (split: `dough.png` CD) |
| `create_compressed` | 2 | `crushed_aluminium_pile` (Engineering), `dough_block` (CD) | split |
| `vintageimprovements` | 4 | aluminum and constantan springs | Engineering |
| `myrtrees` | 2 | rubberwood log blockstates | Engineering ? (wood/IE rubber chain; Compat if only visual) |
| `pneumaticcraft` | 2 | lang + compressed iron block texture | Engineering |
| `canned_goods`, `create_confectionery`, `brewinandchewin` (kimchi), `endersdelight`, `rusticdelight`, `jrftl`, `vintagedelight`, `solapplepie` (lunchboxes), `salt`, `displaydelight/lang`, `farmersrespite/lang` | 9 + 1 + 1 + 13 + 6 + 1 + 17 + 25 + 19 + 1 + 1 | food item/crop textures, lunchbox models, salt blocks, Delight lang | CD (`salt` CD ? as in 3.2) |
| `creativedelight` | 22 | CD client scripts (17 mod folders) + treated stick textures | CD (scripts: sweep F) |
| `creativebrewing/kubejs/_core/cb_client.js` | 1 | CB client core | CB (sweep F) |
| `dragonloot`, `felsic_gear`, `nock_enough_arrows` | 1 + 1 + 2 | dragon scale, felsic block, torch arrows | Equipment |
| `eugene/lang` (en_us, zh_cn), `watchtower_building/lang` | 3 | structure mod names | Worldgen |
| `minecraft/texts/{splashes,minecraftsplashes}.txt` | 2 | splash texts | Common ? (branding like the panorama; pack if the owner prefers resources outside jars) |
| `kubejs/` | 3 | KubeJS example block/item textures and lang | pack (KubeJS scaffold; delete candidate) |
| `ponderjs_generated/lang` | 1 | PonderJS generated lang | pack |
| `kubejs/assets/*.png` (root) | 6 | texture sources (`cherry_pie`, `dyable`, `frosting`, `outline`, `leatheroutline`, `prepared_flesh`) not under any namespace, so never loaded | pack (move to `textureswapper/` or delete) |

`textureswapper/` at repo root (7.2M) holds the source PNGs for the shield and canned golem textures and more; it is a
tooling folder, not a resource, and is outside this sweep's scope beyond this note.

## 5. Summary

### 5.1 Files and size per target (resources only; scripts counted by sweep F)

| target | PMR files / size | kubejs/data files / size | kubejs/assets files / size | total approx |
|---|---|---|---|---|
| Common | 27 (lang, state_lines, gui tints, panorama, waila, helpers, META-INF, mixins json) / 1.3M | 0 | 50 (jei_world, changelog, splashes, lang bits) / 3.5M | 77 / 4.8M |
| Engineering | 224 (211 sawmill, tags, buckets, sawdust, sawblade) / 932K | 5 (+4 IE multiblocks ?) / 48K | about 210 (components, coils, plates, dust, wires + create, vintageimprovements, pneumaticcraft, myrtrees) / 1.4M | 440 / 2.4M |
| Equipment | 39 (damage_tags, shield models) / 160K | 5 (felsic loot, curios) / 32K | about 35 (shields, echo tools, air brake, slot, dragonloot, felsic, arrows) / 190K | 80 / 380K |
| Worldgen | 24 (dimensions, noise, dark fire ?) / 448K | 629 structures + 92 (terralith, geophilic, tectonic, UA, bushierflowers, firefly, IW ?, LC loot, CE worldgen non-wild, sky island, monster box) / 8.4M | 7 (dark fire dup ?, eugene and watchtower lang) / 60K | 750 / 8.9M |
| Compat | 101 (banner textures 98, flower box, moss, physics tags) / 512K | 950 paintings / 4.5M | about 70 (beautify, supplementaries, LC, wares, prettypipes, cardboard) / 320K | 1120 / 5.3M |
| Calendar | 0 (30 lang keys only) | 0 | 0 | lang keys only |
| Devtools | 2 (flower_test preset + minecraft tag) / 16K | 0 | 0 | 2 / 16K |
| CD | 42 (placed dishes, oven, powdered sugar, displaydelight, tmted ?) / 244K | 29 + 14 + 1 + 2 + 3 + 11 + 66 + 28 + 2 + 3 + 109 CD scripts / about 1.6M | about 200 (cans, cultures, dough, seasoned, golem skins, lunchboxes, salt, Delight textures, CD client scripts) / 1.4M | 510 / 3.2M |
| CB | 1 (lingerification) / 4K | 1 (cb_server.js) / 16K | 240 (coasters 206, effects lang, magic resistance, potion buckets) / 870K | 242 / 890K |
| pack / delete | 3 empty folders, `h12y` key | 1 advancement | 10 (kubejs scaffold, ponderjs lang, 6 root pngs) | 14 |

Measured totals: PMR 500 files, 3.6M; `kubejs/data` 1998 files, 15M; `kubejs/assets` 882 files, 7.2M.

### 5.2 Doubtful rows (`?`)

| row | question |
|---|---|
| Dark Fire + Flint and Terrasteel (PMR blockstate, 2 models, 4 textures, item model and texture; duplicate textures in `kubejs/assets`) | nether portal rule is `dimension/` (Worldgen), items are registered in `item/` and `block/` (Engineering in the draft). One owner. |
| `waila_plugins.json` `remainder_buffer` and `creativeengineering.wthit.remainders.*` | Engineering if the remainder buffer is a machine feature; confirm with sweep A. |
| `credit.txt` | which textures it covers; no current file is traced to CreateNumismatics. |
| `assets/minecraft` panorama and `minecraft/texts` splashes | Common or pack resource. |
| `waila.h12y.tier.creativeengineering.copper` | stale key, no mod; delete. |
| `data/tmted/recipes/crusher` | CD (food) or Engineering (IE addon machine). |
| `kubejs/data/{immersiveengineering,immersivepetroleum}/structures/multiblocks` | Engineering (multiblock shapes) or Worldgen. |
| `kubejs/data/immersive_weathering/block_growths` | Worldgen (growth system) or Compat (IW tweak). |
| `salt` loot and textures (`kubejs/data/salt`, `kubejs/assets/salt`) | CD or Compat. |
| `curios` slot files and `textures/slot` | Equipment (backpack skins) or CD (cob pipe); one jar must register the slots. |
| `myrtrees` blockstates | Engineering (rubber chain) or Compat. |
| `prepared_flesh_block` texture and `jrftl` | CD (food chain) or Compat. |
| `development/patcherMod/working/` | parked code, no jar; keep or delete. |

### 5.3 Folders that mix targets and must be split by file

- `PMR/assets/creativeengineering/lang/en_us.json` (220 keys, 22 groups across Common, Engineering, Equipment, Worldgen, Compat, Calendar, Devtools, CD, CB).
- `PMR/waila_plugins.json` and its `config.waila.plugin_creativeengineering.*` keys (15 entries, 7 leave Common).
- `PMR/assets/creativeengineering/blockstates`, `models/block`, `models/item`, `textures/block`, `textures/item` (CD, Engineering, Equipment, Compat, Worldgen ? side by side).
- `PMR/data/creativeengineering/recipes` (sawmill Engineering, lingerification CB), `data/creativeengineering/tags` (Engineering, Compat), `data/creativeengineering/worldgen` (Worldgen, Devtools).
- `kubejs/assets/creativeengineering/textures/item`, `models/item`, `models/block`, `blockstates`, `textures/block` (Engineering, CD, CB, Equipment, Compat, Worldgen ?).
- `kubejs/assets/creativeengineering/lang/en_us.json` (CB effects, Equipment curios).
- `kubejs/assets/create`, `kubejs/assets/create_compressed` (Engineering + CD dough).
- `kubejs/data/creativeengineering/worldgen` and `forge/biome_modifier` (Worldgen + CD wild crops and mooblooms), `loot_tables` (CD fishing nets + Worldgen monster box).
- `creativeengineering.mixins.json` and `META-INF/mods.toml` (per-jar copies, split by sweep B's assignment).
