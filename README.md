# CreativeEngineering 2

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
| **Creative Devtools** | Dev-only tools: `/cegen` generators, `/dumpall` and `/dump*` data exports, performance bots and the in-game structure placer. Not needed to play. |
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
- `generation_scripts/` holds generated data; the generators live in `tools/` and run in game with `/cegen`.

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
