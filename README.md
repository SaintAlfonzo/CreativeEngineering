# CreativeEngineering

A **Minecraft 1.19.2 / Forge 43.3.9** modpack built around Create and Immersive Engineering, with a large
KubeJS layer and a family of in-tree Forge mods (the "Creative" jars). Current pack version: **1.2.01**.

This repo holds the pack's own source: scripts, data, configs and the custom mods. Third-party mod jars,
resource packs and shaders are not committed; [`pack/MODLIST.md`](pack/MODLIST.md) and
[`pack/manifest.json`](pack/manifest.json) list every one with version, download link and sha1.

## What is custom to this pack

### The Creative mods (`development/`)

The pack's own code is split into focused jars. Every one is GPLv3.

| Mod | What it does |
|---|---|
| **Creative Common** | The shared core: the helper libraries every other jar builds on (recipe and tag helpers, item/block/fluid dictionaries, config tooling, mixin gating), plus pack-wide tooltips, JEI ordering and WTHIT lines. |
| **Creative Engineering** | Machines and engineering content: Create's dragon-breathing fan processing, Immersive Engineering sawmill blade routing, precision sawblades, wooden buckets, TNT shrapnel, Ore Excavation undo rules, a liquid XP bridge, and BlockRunner walk speed by surface (speed tiers, sticky facing, camouflage, visual sinking). |
| **Creative Equipment** | The pack's gear: tiered shields with effects, step stool, backpack skins, air brake, bottled air, elytra duplication, a copper tool tier, armor effect frameworks, enchantability and armor cap tuning. |
| **Creative Magic** | Enchanting and magic tweaks: new enchantments (Cycle, Stockpile, Recovery), Dark Fire, Flint and Terrasteel, Botania trinket case rules, nether portal failure handling. |
| **Creative Worldgen** | World generation and fauna: structure tuning (spacing groups, meta structures, ground matching, earth and water fill, terrain adaptation), sky islands, bee nests by biome, biome layout tuning, spawn tuning, mooshroom variants, async structure locating, leaf decay tuning. Bundles More Mooblooms and Baby Mobs. |
| **Creative Delight** | Food. A bundle of Delight add-on ports and native builds (Farmer's, Ocean's, Nether's, End's, Rustic, Veggies, Vintage, Fungi, Miner's, Display, Hearth and Harvest, Roll, Large Meals, Barbecue, Canned Goods, Golden Food, Salt and more), unified under one food-tuning system with shared composting, cans, lunchboxes and placeable-food tooltips. |
| **Creative Brewing** | Brewing, alcohol and potions. Nests ports of Vinery and Brewin' and Chewin', and holds the pack's potion, effect and machine changes (automatic brewing, potion machines, effect refresh and HUD handling). |
| **Creative Calendar** | Calendar, day/night cycle, seasons and biome weather, which Delight crops and Brewing's wine clock rely on. |
| **Creative Compat** | Tweaks to third-party mods that have no larger system of their own: banner patterns, Polymorph pairings, Supplementaries jars and pipes, Quark safe sort, TrashSlot, ItemPhysic burn rules, cat feathers and Cozy cats, and more. Every other Creative jar requires it. |
| **Creative Labels** | Sticky notes and labels rebuilt for the pack. |
| **Creative Devtools** | Dev-only tools: `/cdevgen` generators, `/dumpall` and `/dump*` data exports, performance bots and the in-game structure placer. Not needed to play. |
| **Exposure Backport** | Exposure 1.9.19 with its Polaroid, Expanded and Catalog add-ons ported to 1.19.2. |

### Gameplay highlights

- **Food:** a single tuning system covers every food mod: consistent saturation and nourishment, comfort effects,
  placeable dishes, generated per-type cans, multi-serving cans, four-tier lunchboxes (copper through netherite, with
  skins, dye and soap, and contents kept when upgrading), a salt economy, and wild crops and flowers that generate by
  biome and season.
- **Engineering:** extra Create and Immersive Engineering processing chains, treated sticks and sawdust, TNT
  shrapnel, and surface-based movement speed.
- **Brewing and alcohol:** wine, beer and cocktails with intoxication, automated potion mixing, potion buckets and
  rebalanced effects with readable descriptions in JEI.
- **Exploration:** hundreds of tuned structures spread by group and biome, sky villages, meteorites, ocean geodes,
  reworked nether generation, biome-appropriate bee nests and mob spawns, and a wandering trader catalog.
- **Gear and combat:** tiered shields, armor and tool effects, custom damage types, dragon and echo gear, camera
  bags and backpack skins.
- **Quality of life:** JEI craftable-first ordering and world-interaction tabs, WTHIT lines for crop growth and run
  speed, tooltip clean-ups, toast filtering, stack size policy and item loss protections.
- **Performance:** a JFR/bot-player perf suite and a resource-pack bench keep the pack playable at its size.

### KubeJS layer (`kubejs/`)

- `startup_scripts/` for items and rules, `server_scripts/` for recipes and tags, `client_scripts/` for UI.
- Recipes and tags go through shared helpers (`global.NCH`, `global.tags`) rather than hand-written events, and every
  item id is defined once in a dictionary. Recipe values can be driven from config.
- `generation_scripts/` holds generated data; the generators live in `tools/` and run in game with `/cdevgen`.

### Tooling

- `tools/` contains the generators, audits, manifest builder (`gen_pack_manifest.py`), changelog linter and the
  release stager (`build_deploy.py`).
- `mod_sides.md` classifies every jar as client, server or both. `configs_edited/` backs up every hand-edited config.
- `feature_writeup/` has a design doc and test list for each feature; start at
  [`feature_writeup/README.md`](feature_writeup/README.md). Player-facing changes are tracked in
  `kubejs/assets/creativecommon/changelog/`.

## Repository layout

| Path | What it is |
|---|---|
| `kubejs/` | KubeJS scripts, data and assets |
| `development/` | Source of the Creative mods and Exposure Backport |
| `config/`, `defaultconfigs/` | Mod configs as used by the pack |
| `configs_edited/` | Backups of hand-edited configs (`root_config/`, `server_config/`) |
| `build-logic/`, `settings.gradle` | Shared Gradle build for the in-tree mods |
| `pack/` | Generated mod, resource pack and shader manifest |
| `tools/` | Python/JS helper scripts |
| `deploy/` | Staged releases and deploy log |
| `feature_writeup/` | Feature docs, test lists and TODOs |

## Rebuilding the pack

1. Create a Minecraft 1.19.2 + Forge 43.3.9 instance (Java 17).
2. Download every mod in `pack/MODLIST.md` into `mods/`, checking each file against the sha1 in `pack/manifest.json`.
3. Build the Creative mods (below) and put their jars in `mods/`.
   Some Creative mods translate another mod's original jar at startup instead of shipping its files (ports of mods
   whose licence does not allow a repack). Put each of these unmodified jars in the named folder of the game directory,
   never in `mods/`; the mod checks the sha1 and stops with the missing file's name and source if one is absent
   (list made by `python3 tools/donor_jars_md.py`):

| Needed by | Folder (in the game directory) | File | sha1 | Where to get it |
|---|---|---|---|---|
| creativebrewing | `vinery_upstream/` | `letsdo-vinery-fabric-1.5.5.jar` | `9d4c7646b91219a75a432b6dea1d7a7c41faaaec` | https://cdn.modrinth.com/data/1DWmBJVA/versions/PDi0T5xs/letsdo-vinery-fabric-1.5.5.jar (Modrinth, Let's Do Vinery 1.5.5 for Fabric 1.21.1) |
| creativebrewing | `vinery_upstream/` | `letsdo-vinery-forge-1.3.10.jar` | `fbbd4ae7c10a9d0aa13819e09b70167f3b4ff81c` | https://www.curseforge.com/minecraft/mc-mods/lets-do-vinery/files (Let's Do Vinery 1.3.10 for Forge 1.19.2) |
| creativecompat | `tempad_upstream/` | `tempad-1.21.1-3.0.4-all.jar` | `7a4d6837c6f7a4c9a8f4e7054952df25c148033e` | https://cdn.modrinth.com/data/gKNwt7xu/versions/T26aJH7E/tempad-1.21.1-3.0.4-all.jar (Modrinth, Tempad 3.0.4 for NeoForge 1.21.1) |
| creativedelight | `enders_upstream/` | `endersdelight-1.1.4.jar` | `d1541d70c7dbae71c2a7e1693f512f2166908802` | https://cdn.modrinth.com/data/2uEhdGKt/versions/Jaf9wxnO/endersdelight-1.1.4.jar (Modrinth, Ender's Delight 1.1.4 for Forge 1.20.1) |
| creativedelight | `goldenfood_upstream/` | `goldenfood-1.19.2-2.0.0.jar` | `4109e4d6d484892b8efc201b1a32b9361df930cd` | the Golden Food 2.0.0 release for Forge 1.19.2 by GGWPJason (CurseForge, file goldenfood-1.19.2-2.0.0.jar), or the pack's server files |
| creativedelight | `largemeals_upstream/` | `largemeals-1.20.1-1.3.0.jar` | `7662138788a75832a0c6bad6b13b006cac383496` | CurseForge, Large Meals 1.3.0 for Forge 1.20.1 (largemeals-1.20.1-1.3.0.jar) |
| creativedelight | `miners_upstream/` | `miners_delight-1.20.1-1.4.5-backport.jar` | `638438679800711939e7a8029ae3d5340f8c17bd` | https://cdn.modrinth.com/data/qMxbM4BQ/versions/p0INUam7/miners_delight-1.20.1-1.4.5-backport.jar (Modrinth, Miner's Delight 1.20.1-1.4.5-backport for Forge 1.20.1) |
| creativedelight | `miners_upstream/` | `miners_delight-1.19.2-1.1.2.jar` | `23701e0772676ff74c0290387c90fd9365987673` | https://mediafilez.forgecdn.net/files/4738/272/miners_delight-1.19.2-1.1.2.jar (CurseForge, Miner's Delight 1.19.2-1.1.2 for Forge 1.19.2, project 689630 file 4738272) |
| creativedelight | `oceanic_upstream/` | `oceanic_delight-1.0.1-forge-1.19.2.jar` | `91fd8842a5bf9b55c2d9fb39f721129bc6cae1b5` | https://cdn.modrinth.com/data/O1qboAbB/versions/9JkMCuAa/oceanic_delight-1.0.1-forge-1.19.2.jar (Modrinth, Oceanic Delight 1.0.1 for Forge 1.19.2) |
| creativedelight | `roll_upstream/` | `roll_delight-1.3.1-forge-1.20.1.jar` | `e23d48373f2a62c51533d15cd99eb24397f8d8d8` | CurseForge, Roll Delight 1.3.1 for Forge 1.20.1 (file "[1.20.1] Roll Delight-1.3.1-forge-.jar", renamed) |
| creativedelight | `rustic_upstream/` | `rusticdelight-forge-1.20.1-1.7.0.jar` | `bca9adf60b59951ae5049149185da64a6f7b6f8a` | https://cdn.modrinth.com/data/foa4fGIH/versions/z29Gd3ec/rusticdelight-forge-1.20.1-1.7.0.jar (Modrinth, Rustic Delight 1.7.0 for Forge 1.20.1) |
| creativeworldgen | `farmers_upstream/` | `FarmersStructures-1.0.6-1.21.1_neoforge.jar` | `94262398153d7ca5dc9ffb93e49acdbad85af223` | Farmer's Structures 1.0.6 for NeoForge 1.21.1 (CurseForge / Modrinth) |

4. Copy `kubejs/`, `config/` and `defaultconfigs/` into the instance.
5. Download the resource packs into `resourcepacks/` and enable them in the order listed in `pack/MODLIST.md`.
6. For a dedicated server, run `python3 tools/dump_server_mods.py` to copy only the `both`/`server` mods into
   `server_mods/`.

## Building the in-tree mods

The instance root is one Gradle build with a subproject per mod; shared setup is the `ce.forge-mod` convention plugin
in `build-logic/`.

```
./gradlew build                            # every mod
./gradlew :creativeCommon:build            # one mod
./gradlew :creativeCommon:installToMods    # build, then swap the jar into mods/ (old one -> jar_archive/displaced/)
```

Creative Delight and Creative Brewing nest their members; build and install them through `:creativeDelight:` and
`:creativeBrewing:`, not a member's own task. `development/<mod>/libs/` is gitignored and holds compile-time copies of
mod jars from `mods/`, so populate it before building. Never replace a jar while the game is running.

## After changing mods

Re-run `python3 tools/gen_pack_manifest.py`, update `mod_sides.md` for any new mod, and commit `pack/`.

## Licence

The pack's own work is licensed under [GPLv3](LICENSE). Bundled and ported third-party code keeps its original
licence; see the `CREDITS.txt` and `NOTICE` files in each mod folder.
