# Structure tuning: how often every structure and village house generates

Test prefix **ST-** · Last updated **2026-09-27** · Overall status **DEPLOYED, in-game test pending** (the parts that were
already live have been checked against the game's own dumps; today's additions need a rejoin)

## 0. At a glance

Two systems cover every structure in the pack. **Structure sets** decide how often a whole structure
(village, temple, house mod, meteorite) gets a chance to spawn: one config, category tags with multipliers,
applied at world load. **Jigsaw pools** decide which pieces a structure is built from, which is where
village houses live: another config, the same tag syntax, applied by patcherMod. A third, smaller part swaps
other mods' loot containers so Lootr can handle them.

| Part | Layer | Status |
|---|---|---|
| Structure-set tuning (`structure_config.json` + `structure_tags.json`) | KubeJS `structure_overrides.js` (`ServerEvents.highPriorityData`) | Runs in game: log 2026-09-27 12:00, 76 sets overridden, 6 splits. All 66 written sets match the live registry (`tools/check_structure_live.py` on the 11:54 dump, 0 mismatches) |
| Today's coverage pass: `ae2:meteorite`, 24 new house structures tagged, `blocked` notes | scanner + tags + report | BUILT 2026-09-27: node sim gives 93 overrides, 6 splits, no coverage gaps. Needs a rejoin (ST-01, ST-02) |
| KubeJS virtual-pack prefix fix (world creation broke without it) | patcherMod `KubeJSVirtualDataPackListMixin` | VERIFIED 2026-09-26 (world creation works, tuning applies) |
| Jigsaw pool tuning (`pool_config.json` + `pool_tags.json`) | patcherMod `worldgen/PoolTuner` | Runs in game: report rewritten 2026-09-27 12:00, 645 pools / 3669 pieces, 12 pools retuned. Villages themselves not checked yet |
| Lootr container swaps (`loot_container_swaps.json`) | patcherMod `LootContainerSwaps` + mixin | DEPLOYED; untested in game |

**Open first:** [structure_config.json](../kubejs/generation_scripts/worldgen/structure_config.json) (per set),
[structure_tags.json](../kubejs/generation_scripts/worldgen/structure_tags.json) (categories),
[pool_config.json](../kubejs/generation_scripts/worldgen/pool_config.json) / [pool_tags.json](../kubejs/generation_scripts/worldgen/pool_tags.json) (village houses),
and the two reports the game rewrites on every load:
[structure_tag_report.md](../kubejs/generation_scripts/worldgen/structure_tag_report.md),
[pool_tag_report.md](../kubejs/generation_scripts/worldgen/pool_tag_report.md).

**Knobs:**
- Per structure set: `current` (spacing, separation, frequency, count, locate_offset) and each structure's `enabled`.
- Per category: `multiplier` on any tag in `structure_tags.json` (0 = removed, 0.5 = half as many, 2 = twice).
- Per village house: `current` weight / `enabled` in `pool_config.json`, or a `multiplier` in `pool_tags.json`.
- Lootr: block → vanilla container pairs in `loot_container_swaps.json`.

**Applying:** rejoin the world (worldgen is read at world load; `/reload` only refreshes the structure
report). Only chunks generated afterwards change.

**Measuring:** `/locate structure <id>` (a removed structure answers "Could not find"), the reports above,
and `/dumpstructuregen` + `python3 tools/check_structure_live.py` for the live values.

---

## 1. Overview: how 1.19.2 places structures

- **Structure set:** every structure belongs to a *structure_set*, which owns the placement. Almost all use
  `random_spread`: the world is cut into square cells of `spacing` chunks, each cell gets one attempt at a
  random spot at least `separation` chunks from the cell edge, and `frequency` (0 to 1) is an extra chance
  roll on top. Strongholds use `concentric_rings` (`count` attempts on rings around spawn). A set with
  several structures picks one per cell by `weight`, and the pick must also pass its own biome check.
- **Jigsaw structures** (villages, T&T, YUNG's, most modded buildings) are assembled from *template pools*.
  Each pool is a weighted list of pieces; a village street connector draws a house from
  `minecraft:village/plains/houses` with chance `weight / pool total`. Mods add their houses to the vanilla
  pools in code, so they never appear as structures of their own.
- **Placed features are not structures.** Vanilla monster-room dungeons, fossils and desert wells, YUNG's
  Extras' desert and swamp decorations and Supplementaries' road signs are features in a different registry.
  Neither system here touches them (see 4).
- **Load timing:** structure sets and pools are worldgen data, read when a world loads. Existing chunks never
  change.

## 2. Reference

### 2.1 What exists (2026-09-27)

| | Count | Where from |
|---|---|---|
| Structure sets in the config | 114 | 87 from mod jars, 16 from the vanilla table (vanilla builds them in Java), 10 from `kubejs/data` (MCreator house mods converted to data), 1 registered in Java by a mod (`ae2:meteorite`) |
| Structures in those sets | 218 | every one has a `mod/*` tag and a `type/*` or `dungeon/*` tag |
| Structure tags | 74 | 37 `mod/*`, 20 `type/*`, 12 `dungeon/*`, 5 meta (`dungeons`, `pillager`, `nether`, `end`, `ruins`) |
| Live structures in no set | 140 | never generate on their own: 133 `terralith:zpointer/*` biome markers, 5 Missing Villages base ids (their sized variants are in sets), `nullscape:phantom_nest`, `terralith:underground_cabin` |
| Jigsaw pools | 645 | 3669 pieces, read live from the game, so code-injected houses are included |
| Pool tags | 15 | 4 tech-house mods at x0 + `tech` x0, 10 other house mods at x1 |

### 2.2 Structure tag families

| Family | Tags | Examples |
|---|---|---|
| `mod/*` | one per namespace, plus `mod/yungs` (6 YUNG's mods) and `mod/villager_houses` (14 one-building house mods) | `mod/philipsruins` x2, `mod/cloudstorage` x0.1 |
| `type/*` | village, house, camp, fort, tower, ship, enemy_building, ruin, ruined_portal, underwater_ruin, shipwreck, mineshaft, underground, environmental, decoration, sky, nether/end environmental and constructed | `type/ruined_portal` x0 (all ruined portals removed), `type/camp` x2.5 |
| `dungeon/*` | surface / underground × vanilla / better (YUNG's) / modded, plus unions | `dungeon/surface_better` x1.33 |
| meta | `dungeons`, `pillager`, `nether`, `end`, `ruins` | `pillager` x1.33 |

Tags multiply: a structure's final multiplier is the product of every tag it's in (each tag counted once).
The report prints each chain, e.g. `minecraft:pillager_outpost`: x2.9364 from `type/tower`, `type/enemy_building`,
`pillager` and more.

### 2.3 How a multiplier becomes placement values

| Placement | What changes | Example from the current report |
|---|---|---|
| `random_spread` grid | spacing → floor(spacing / √m), separation scales with it, `frequency` absorbs the rounding so the result is exact | `villager_tannery:tannery` x0.66: spacing 45 → 55, separation 30 → 37, frequency 1 → 0.986 |
| spacing already 1 | only `frequency` | `minecraft:buried_treasures` x1.5: frequency 0.01 → 0.015 |
| `legacy_type_1` (outposts) | switches to `default` when frequency changes (legacy rounds to 1/n) | `minecraft:pillager_outposts` x2.94: spacing 32 → 18, frequency 0.2 → 0.186 |
| `concentric_rings` | `count` | `minecraft:strongholds` x1.5: 128 → 192 (but see the YUNG's block in 3.5) |
| x0 | the whole set gets `frequency: 0`, or the member is dropped from a shared set | `minecraft:ruined_portals` frequency 1 → 0 |
| mixed multipliers in one set | the set splits: extra groups move to `creativeengineering:split/<ns>/<path>_<n>` with a new salt | 6 sets split (T&T towns/towers/other, Terralith underground, two Philip's Ruins sets) |

### 2.4 Pool weights

Weights are relative inside one pool. Vanilla houses are 2 to 4 each, IE's 1 each, Create: Dynamic Village's 20
each, Domestication Innovation's pet shop 17, Monobank's vault 15, Wares' warehouse 10, Advanced Peripherals' house
10, PneumaticCraft's mechanic house 8, Chef's Delight's houses 2 to 5, Cloud Storage's balloon stand 2. Lowering a
piece makes the rest of the pool (including its `minecraft:empty` lot) more likely; the number of houses in a
village doesn't change. Current pool tags remove 57 tech pieces: IE 25, Dynamic Village 20, PneumaticCraft 7,
Advanced Peripherals 5. Example: in `minecraft:village/desert/houses` a Dynamic Village house was 8.8% of each
draw; now it's 0, and a vanilla desert house goes from 0.9% to 1.5%.

## 3. What the pack changes

### 3.1 Structure-set tuning (KubeJS)

- `tools/gen_structure_config.py` builds `structure_config.json` from the jars (`worldgen/structure_set` and
  `structure` jsons), `kubejs/data`, the vanilla table and the code-registered table. Re-running it is safe: it
  keeps `enabled` and every field you changed in `current` (3-way merge), and drops sets whose mod is gone
  (it prints `dropped <set>`).
- At every world load, `kubejs/server_scripts/worldgen/structure_overrides.js` (in `ServerEvents.highPriorityData`)
  combines `current`, `enabled` and the tag multipliers, and adds a structure_set json for every set that differs
  from its default to KubeJS's top-priority virtual datapack (above Terralith, Nullscape and `kubejs/data`). Sets
  left at their default get nothing. It then rewrites `structure_tag_report.md`.
- **Any config error applies nothing** (every structure uses its mod default) and is logged as an error and
  listed in the report: unknown id or tag, a bad multiplier, an invalid placement. An unknown *explicit* id
  aborts; a glob that matches nothing only warns.
- It only ever writes structure_set documents, never the structure itself (see 4).

### 3.2 Code-registered sets (added 2026-09-27)

AE2 registers `ae2:meteorite` in Java (`appeng.init.worldgen.InitStructures`: spacing 32, separation 8, salt
124895654, linear), so the jar scan never saw it and it was the only live set the config lacked. The scanner now
has a `CODE_REGISTERED_SETS` table (bytecode-checked values, plus the class that proves the mod is installed).
`tools/check_structure_live.py` section 1 lists any other set in the live registry the config doesn't have.

### 3.3 Coverage of the new mods (added 2026-09-27)

The five house mods added on 2026-09-27 (Greek Villager House, Hobbit House, Old Swamp Fisherman House, Sawmill
House, Simply Houses) and the 10 MCreator houses converted to `kubejs/data` bring 17 sets and 24 structures. They
were already in the config but had no tags, so category knobs skipped them. Now:
- a `mod/<namespace>` tag for each of the 15 house namespaces (Simply Houses included), plus `mod/ae2`
- `mod/villager_houses` groups the 14 one-building mods (each is a lone house with a villager in it)
- `type/house` now includes `#mod/villager_houses` and `simply_houses:*` (Simply Houses' buildings have no villagers;
  its "forest fort" is house-sized, 22×24×29, so it stays a house rather than `type/fort`, which also avoids a set split)
- `ae2:meteorite` is in `type/environmental`

**Side effect:** `type/house` is x0.66, so these 17 sets now spawn at 0.66 of their mods' rate (e.g. hobbit house
spacing 45 → 55). To keep them at their own rate, set `mod/villager_houses` and `mod/simply_houses` to 1.5 (x0.99), or
change `type/house`.

### 3.4 Structures other mods block in code (added 2026-09-27)

YUNG's mods stop some structures inside the generator, whatever their set says. The scanner reads the current
mod configs and writes a read-only `"blocked"` note on each affected structure entry. The report flags them on
their multiplier line and lists them under Coverage.

| Structure | Stopped by | Option |
|---|---|---|
| `minecraft:desert_pyramid` | YUNG's Better Desert Temples | `betterdeserttemples-forge-1_19.toml` "Disable Vanilla Pyramids" = true |
| `minecraft:jungle_pyramid` | YUNG's Better Jungle Temples | `betterjungletemples-forge-1_19_2.toml` "Disable Vanilla Jungle Temples" = true |
| `minecraft:swamp_hut` | YUNG's Better Witch Huts | `betterwitchhuts-forge-1_19.toml` "Disable Vanilla Witch Huts" = true |
| `minecraft:fortress` | YUNG's Better Nether Fortresses | `betterfortresses-forge-1_19.toml` "Disable Vanilla Nether Fortresses" = true |
| `minecraft:stronghold` | YUNG's Better Strongholds | always, no option |
| `betterdungeons:small_nether_dungeon` | YUNG's Better Dungeons | `betterdungeons-forge-1_19.toml` "Enable Small Nether Dungeons" = false |

What this means for the current tags: `dungeon/surface_vanilla` x0.66 only changes the woodland mansion (its
other three members are blocked), and `dungeon/underground_vanilla` x1.5 only changes the ancient city (the
stronghold count 128 → 192 is written but nothing generates). In `minecraft:nether_complexes`, a cell that picks the
blocked fortress falls through to the bastion. Tune the YUNG's replacements (`dungeon/surface_better`,
`mod/betterstrongholds`, ...) instead.

### 3.5 Jigsaw pool tuning (patcherMod)

- `worldgen/PoolTuner` runs on `ServerAboutToStartEvent` at LOWEST priority, after every mod has injected its
  houses. It reads every live pool, merges `pool_config.json` (game-written: new pools/pieces appear with
  `current = original`, your edits stay, edited pieces whose mod is gone stay with `"missing": true`), resolves
  `pool_tags.json` (same syntax as structure tags, plus an optional `"pools"` glob filter), rebuilds changed pools
  and writes `pool_tag_report.md`, including a list of every piece another mod injected into each pool.
- Fractional weights: the pool is scaled up (at most x10) and gcd-reduced to keep the ratios.
- Singleplayer: vanilla pools are the same Java objects across world loads and some mods append again every
  load. PoolTuner keeps the first weights it saw per pool, so edits never compound and re-added duplicates are dropped.
- Any error applies no pool tuning at all.

### 3.6 Lootr container swaps (patcherMod)

Lootr only converts vanilla chests, barrels, trapped chests and shulker boxes. `loot_container_swaps.json` turns a
listed block that a structure template places **with a loot table** into a vanilla container (facing kept), which
Lootr then converts: IE crates and Farmer's Delight cabinets/baskets → barrel, PneumaticCraft reinforced chest →
chest. Hook: `mixin/StructureTemplateLootSwapMixin` at the return of Forge's `StructureTemplate.processBlockInfos`,
which every template placement goes through. `loot_container_audit.md` (re-run 2026-09-27: 2320 loot containers)
leaves only Nullscape's phantom-hive droppers and Monobank's bank blocks unconverted, on purpose.

### 3.7 Existing configs that modify structure generation

**This pack's own** (all in `kubejs/generation_scripts/worldgen/`, applied at world load):

| File | Changes | Current state |
|---|---|---|
| `structure_config.json` | per-set placement, per-structure on/off | hand edits: `cloudstorage:big_balloon` and `sky_temple` (spacing/separation/frequency), `minecraft:ocean_monuments` (frequency) |
| `structure_tags.json` | category multipliers | 21 active, listed at the top of `structure_tag_report.md` |
| `pool_config.json` / `pool_tags.json` | village house weights | no per-piece edits; tags remove the 4 tech-house mods |
| `loot_container_swaps.json` | loot containers in templates | 4 swap rules |
| `biome_merges.json` | **where** structures can spawn: adds biomes to `has_structure/*` tags | 228 biome adds (Terralith biomes for vanilla and modded structures) |
| `biome_config.json` | biome shares; structures follow their biomes | 9 overworld + 3 End biomes tuned |

**Mods' own configs** (values as of 2026-09-27; `configs_edited/` has backups of those marked "backed up"):

| Mod | File | Setting | Effect here |
|---|---|---|---|
| YUNG's Better Desert Temples / Jungle Temples / Witch Huts / Nether Fortresses | `better*-forge-1_19*.toml` | "Disable Vanilla X" = true | vanilla versions stopped in code (3.4) |
| YUNG's Better Strongholds | `betterstrongholds-forge-1_19.toml` | cobweb/torch/lantern rates | contents only; the vanilla stronghold is always stopped |
| YUNG's Better Dungeons | `betterdungeons-forge-1_19.toml` | "Enable Small Nether Dungeons" = false; small-dungeon chest/banner counts | small nether dungeons never generate (3.4); the rest is contents |
| YUNG's Extras | `YungsExtras/forge-1_19/wishing_wells.json` | wishing-well loot blocks | contents only; its ~60 features have no frequency option |
| Towns & Towers | `towns_and_towers/structure_rarity.json5` | villages 48/24, outposts 48/24 freq 0.2, others 32/16 | same as its jar sets; this framework's overrides win (live check, 0 mismatches). Leave at defaults |
| Towns & Towers | `towns_and_towers/structure_enable_or_disable.json5` | all true | nothing disabled |
| Philip's Ruins | `philips-ruins-common-configs.toml` | "Enable Configs" = false | its separation/density and per-type toggles are inactive; frequency comes from its sets (`mod/philipsruins` x2) |
| Applied Energistics 2 | `ae2/common.json` | generateMeteorites = true | false would stop AE2 registering `ae2:meteorite` at all (see 4) |
| Supplementaries | `supplementaries-common.toml` (backed up) | `[building.sign_post.way_sign] enabled = true` | way-sign structures on; also tunable as `supplementaries:way_sign` |
| Cloud Storage | `cloud-storage.toml` | `balloon_stand_spawn_weight` = 2 | village-pool weight of its balloon stands (seen by PoolTuner); `mod/cloudstorage` x0.1 is for its 2 sky structures |
| Chef's Delight | `chefsdelight-common.toml` | chef/cook house weights 2 to 5 per biome | village-pool weights |
| Domestication Innovation | `domestication-innovation.toml` (backed up) | `petstore_village_weight` = 17 | village-pool weight |
| Wares | `wares-common.toml` | GenerateWarehouses = true, WarehouseWeight = 10 | village-pool weight |
| Monobank | `monobank-common.toml` | GenerateVaults = true, VaultWeight = 15 | village-pool weight |
| Advanced Peripherals | `Advancedperipherals/world.toml` | enableVillagerStructures = true, villagerStructureWeight = 10 | village-pool weight (pool tag x0 removes it anyway) |
| PneumaticCraft | `pneumaticcraft-common.toml` (backed up) | `addMechanicHouse` = 8 | village-pool weight (pool tag x0 removes it anyway) |
| Waystones | `waystones-common.toml` | spawnInVillages = true, forceSpawnInVillages = false | waystone pieces in village pools |
| Lootr | `lootr-common.toml` | decay/refresh_structures empty | converts structure loot containers per player |

**Data-pack overrides from mods** (not configs, but they change the baseline): Terralith replaces
`minecraft:villages` (spacing 26 instead of 34) and vanilla's monster room, deep monster room, fossil and desert-well
placed features; Nullscape replaces `minecraft:end_cities`. The scanner reads these, so `original` already shows them.

## 4. Limits and decisions

- **Only structure_set documents are written, never the structure.** Frequency, `locate_offset` and on/off all live
  on the small set document. Rewriting a structure (biomes, jigsaw pools, size) risks breaking jigsaw schemas, and
  vanilla structures have no json to start from. Where a structure spawns is changed through biome tags
  (`biome_merges.json`) instead.
- **Placed features aren't covered.** Monster-room dungeons (Terralith's json: 10 + 4 attempts per chunk), fossils
  (1 in 64 chunks), desert wells (1 in 1000), YUNG's Extras' ~60 desert/swamp features, Immersive Weathering's desert
  fossil, Supplementaries' road signs and Quark's monster boxes are features, not structures. Tuning them would need a
  separate placed-feature system (an extra `count`/`rarity_filter` modifier per feature). Not built.
- **Blocked structures:** a structure another mod stops in code can't be brought back or tuned from here (3.4). The
  `blocked` notes come from the scanner, so re-run it after changing one of those YUNG's options.
- **AE2 meteorites and `generateMeteorites`:** with it false, AE2 doesn't register the set, and an override written
  from this config would add it back. To remove meteorites, use `enabled: false` here (frequency 0) and leave AE2's
  option alone.
- **Shared-set splits are only exact when the members' biomes don't overlap.** Split-off members stop competing for
  the same cells and end up somewhat more common where biomes overlap. The report lists every split, and exclusion
  zones pointing at the original set don't see the split-off parts.
- **Pool weights are relative.** A village still gets the same number of houses. Removing every piece leaves a lone
  `minecraft:empty`, so a start pool like that generates nothing: remove whole structures in `structure_config.json` instead.
- **New chunks only, rejoin to apply.** `/reload` doesn't reload worldgen.
- **Removing a structure mod:** re-run `python3 tools/gen_structure_config.py`, then delete that mod's *explicit* ids
  from `structure_tags.json` (an unknown explicit id aborts all tuning; its `ns:*` glob only warns). Leaving the old
  set in the config fails world creation with "Unbound values in registry".
- **Rejected:** tuning through each mod's own config (T&T, Philip's Ruins, YUNG's). Few mods have one, the formats
  differ, and vanilla has none. This framework covers everything the same way. Mod options that *block* things are
  kept, and flagged.
- **Rejected:** loot-theme tags ("rich", "poor"). Categories are by what a structure is (house, tower, camp...).

## 5. File map

- Configs: [kubejs/generation_scripts/worldgen/](../kubejs/generation_scripts/worldgen/), whose README has the full workflow
- Structure sets applier: [structure_overrides.js](../kubejs/server_scripts/worldgen/structure_overrides.js) (`resolveTags` L120, `scalePlacement` L186, `planSet` L259, `plan` L448, event L510)
- Scanner: [tools/gen_structure_config.py](../tools/gen_structure_config.py) (`VANILLA_STRUCTURE_SETS`, `CODE_REGISTERED_SETS`, `CODE_BLOCKS`)
- patcherMod: [worldgen/PoolTuner.java](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/PoolTuner.java), [LootContainerSwaps.java](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/LootContainerSwaps.java), [WorldgenFiles.java](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/WorldgenFiles.java) (report writer; KubeJS blocks java.io); mixins [StructureTemplateLootSwapMixin](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/StructureTemplateLootSwapMixin.java), [KubeJSVirtualDataPackListMixin](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/KubeJSVirtualDataPackListMixin.java)
- Dump commands: `/dumpstructuregen` [dump_structuregen.js](../kubejs/server_scripts/_lib/dump_structuregen.js) → `kubejs/exported/worldgen/structuregen.json`; `/dumpstructures` [structure_dump.js](../kubejs/server_scripts/_lib/structure_dump.js) (names only)
- Offline tools: [tools/structure_tuning_sim.js](../tools/structure_tuning_sim.js), [tools/check_structure_live.py](../tools/check_structure_live.py), [tools/audit_loot_containers.py](../tools/audit_loot_containers.py)
- Converted house mods: `kubejs/data/<modid>/` (see Claude's memory `mcreator-house-mods-datapack`)
- Old node generators (don't run, they would double-apply): `kubejs/archive/worldgen/`

## 6. Offline checks

- **After adding or removing a structure mod:** `python3 tools/gen_structure_config.py`. It prints the edits it kept and
  any `dropped` sets. Then `node tools/structure_tuning_sim.js`: the Coverage lines should read "none" for missing
  `type/*` and `mod/*` tags. New namespaces need a `mod/*` tag and a `type/*` entry.
- **Before rejoining after an edit:** `node tools/structure_tuning_sim.js [report.md] [out/]` runs the real
  `structure_overrides.js` in node and prints the summary line the game will log (currently `93 structure_sets
  overridden (6 split sets), 114 sets total`), or the errors that would make it apply nothing. On 2026-09-27 it
  reproduced the game's own report line for line on every set that was already written.
- **After a world load:** `/dumpstructuregen` in game, then `python3 tools/check_structure_live.py`. Sections 1 (live
  sets the config lacks) and 2 (config sets that aren't live) should be empty, and section 4 should say `0 mismatch(es)`.
  Frequency and salt can't be compared (the dump can't read them).
- **Loot containers after mod changes:** `python3 tools/audit_loot_containers.py` rewrites `loot_container_audit.md`.
- **Node, not Rhino:** the sim catches config, tag and math mistakes. Rhino-only quirks need the headless Rhino harness
  (Claude's memory `kubejs-headless-rhino-harness`). PoolTuner has a 32-check headless Java test (memory `jigsaw-pool-tuning`).

## 7. In-game test suite

**Setup:**
- Creative world with cheats. Rejoin once after pulling these changes, then fly well away from explored land (or
  make a new world) so the chunks you check are new.
- `L <id>` = `/locate structure <id>`. A removed structure answers "Could not find a structure of type ... nearby".
- `Dump` = `/dumpstructuregen`, then run `python3 tools/check_structure_live.py` outside the game.
- Revert every temporary edit and rejoin before moving to the next test.

**Structure sets**
- [ ] **ST-01 Loads clean:** rejoin, search `logs/latest.log` for `[structure-tuning]` → `93 structure_sets overridden (6 split sets), 114 sets total` and no error line. `structure_tag_report.md` Coverage: "none", "none", and the 6 blocked structures from 3.4.
- [ ] **ST-02 Live values match:** `Dump` → sections 1 and 2 empty, section 4 `0 mismatch(es)` over 83 sets. Partly PASSED 2026-09-27 on the pre-change dump (66 sets, 0 mismatches; section 1 then listed `ae2:meteorite`, fixed since).
- [ ] **ST-03 Removal works:** `L minecraft:ruined_portal` → not found. In the Nether, `L minecraft:ruined_portal_nether` → not found.
- [ ] **ST-04 New houses load:** `L villager_tannery:tannery` and `L simply_houses:taiga_inn` → found. Teleport to the tannery → it has its villager, and urns stand where the original 1.20 build had decorated pots.
- [ ] **ST-05 AE2 meteorite untouched:** `L ae2:meteorite` → found (no override is written for it at x1).
- [ ] **ST-06 Category knob:** set `mod/villager_houses` to 0, rejoin → the report shows the 14 house sets at frequency 0; `L hobbit_house_:hobbit_house` → not found. Set it back to 1.
- [ ] **ST-07 Per-set edit:** in `structure_config.json` set `ae2:meteorite` → `current.spacing` 16, rejoin, `Dump` → `ae2:meteorite` spacing 16 in `structuregen.json` and still `0 mismatch(es)`. Put 32 back.
- [ ] **ST-08 One member off:** in `simply_houses:forest_structures` set `forest_chapel` → `"enabled": false`, rejoin → the report writes the set with 3 members; `L simply_houses:forest_chapel` → not found, `L simply_houses:forest_house` → found. Revert.
- [ ] **ST-09 An error applies nothing:** add `"simply_houses:typo"` to `mod/simply_houses`, rejoin → the log shows a `[structure-tuning]` error and "nothing applied"; the report lists the error; `L minecraft:ruined_portal` finds one again. Remove the typo and rejoin.
- [ ] **ST-10 Blocked means blocked:** `L betterstrongholds:stronghold` → found; throw an eye of ender → it heads toward a YUNG's stronghold. The report flags `minecraft:stronghold` as blocked.

**Village houses and containers**
- [x] **ST-11 Pool tuner runs:** `pool_tag_report.md` is rewritten at world load → "645 pools, 3669 pieces", "12 pool(s) retuned", no errors section. PASSED 2026-09-27 (report from the 12:00 world load).
- [ ] **ST-12 Tech houses gone:** `L minecraft:village_plains`, teleport, and walk 3 fresh villages → no Create: Dynamic Village, IE, PneumaticCraft or Advanced Peripherals houses. Pet shops, warehouses and chef houses still show up.
- [ ] **ST-13 Piece edit:** in `pool_config.json` → `minecraft:village/plains/houses` → `domesticationinnovation:plains_petshop` set `"enabled": false`, rejoin → `pool_tag_report.md` shows it **removed** in that pool. Revert.
- [ ] **ST-14 Container swap:** `/place template chefsdelight:plains_chef_house ~3 ~ ~3` → vanilla barrels stand where its oak cabinets and baskets were. Then find a chef house in a freshly generated village → those barrels are Lootr barrels (per-player loot).

## 8. History

- **2026-09-25:** framework built: jar scanner + vanilla table, `structure_config.json`, node generator writing
  `kubejs/data` overrides, `/dumpstructuregen`. Ruined-portal set fixed (one set, 7 members) and all ruined portals removed.
  Vanilla table ids corrected against the live registry. Biome merges applied (separate feature).
- **2026-09-26:** `structure_tags.json` (mod / type / dungeon families) with exact multiplier math and set splitting;
  moved to runtime (`structure_overrides.js` in `highPriorityData`, node scripts archived). First in-game run broke world
  creation (KubeJS virtual-pack prefix bug) → fixed by `KubeJSVirtualDataPackListMixin`, confirmed in game. MineColonies
  removed → scanner drops stale sets. PoolTuner and Lootr container swaps built (32-check headless test).
- **2026-09-27:**
  - 10 MCreator house mods converted to `kubejs/data`; the scanner reads `kubejs/data` (113 sets).
  - Coverage pass after the new mods: `ae2:meteorite` added through a new code-registered-set table (114 sets); 16 new
    `mod/<namespace>` tags (15 house namespaces + `ae2`) and the group tag `mod/villager_houses`; the 24 new house structures joined `type/house` (x0.66, see 3.3); `ae2:meteorite`
    in `type/environmental`. No coverage gaps left.
  - Scanner marks structures another mod blocks in code (`blocked`), and the report shows them.
  - New tools: `check_structure_live.py` (0 mismatches on the pre-change live dump) and `structure_tuning_sim.js`.
    `loot_container_audit.md` re-run (2320 containers).
  - This writeup.
