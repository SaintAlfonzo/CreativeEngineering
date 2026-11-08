# Structure tuning: how often every structure and village house generates

Test prefix **ST-** · Last updated **2026-10-22** · Overall status **DEPLOYED, in-game test pending**: everything through
plan 7.4 went out INCOMPLETE with pack 1.0.6 (2026-10-14); fixes from the 8.0 scratch run with 1.0.7 (2026-10-18) and 1.0.8
(2026-10-19); earth fill on water, camp plinths and metas off coasts (7.5) with 1.0.9 (2026-10-19). The scratch run
8.0.1 to 8.0.10 finished 2026-10-19 ([testing/structures/00_plan.md](testing/structures/00_plan.md)); the client list
(8.0.11) is still to run. The 1.0.6 open bugs and switches are in [plan_1.0.6.md](plan_1.0.6.md) 2.1 and 2.2. Older parts: DEPLOYED, in-game test pending (structure groups
replaced the per-set + multiplier model on 2026-09-27 evening; jar in `mods/`, needs a rejoin: ST-17 onward). Ground
matching (3.8), the CEConfig `[worldgen]` section with its debug density (3.9) and `/cdevplace` (3.10) are DEPLOYED
2026-09-27 (jar md5 729953e6); the structure lines, live-set check and `/cdevplace` ran on a scratch dedicated server, your client not yet: ST-27 to ST-42

## 0. At a glance

Two systems cover every structure in the pack. **Structure groups** decide how often each whole structure
(village, temple, house mod, meteorite) spawns and how far apart structures of one kind keep: every structure of a
kind shares one grid, with its own weight and frequency on it. **Jigsaw pools** decide which pieces a structure is
built from, which is where village houses live. Two smaller parts act as structures are placed: one swaps other mods'
loot containers so Lootr can handle them, and **ground matching** swaps a grass/dirt base for the ground actually
there (a grassy ruin on volcanic peaks sits on basalt instead).

| Part | Layer | Status |
|---|---|---|
| Structure groups (`structure_groups.json` + `group/*` tags) | KubeJS `structure_overrides.js` (`ServerEvents.highPriorityData`) + patcherMod `StructureFrequencies` (2 mixins) | DEPLOYED 2026-09-27 (jar md5 e58b6e57): node sim, Rhino parity and a 13-check Java test pass (`tools/test_structure_groups.sh`). Needs a rejoin (ST-17 to ST-26) |
| Migration from the old model | `tools/gen_structure_groups.py --migrate` | Done 2026-09-27: every structure within x0.89 to x1.13 of its old rate (`structure_group_migration.md`) |
| KubeJS virtual-pack prefix fix (world creation broke without it) | patcherMod `KubeJSVirtualDataPackListMixin` | VERIFIED 2026-09-26 (world creation works, tuning applies) |
| Jigsaw pool tuning (`pool_config.json` + `pool_tags.json`) | patcherMod `worldgen/PoolTuner` | Runs in game: report rewritten 2026-09-27 12:00, 645 pools / 3669 pieces, 12 pools retuned. Latest report (2026-10-02): 1,367 pools / 6,932 pieces, 9 retuned, Improved Pillager Outpost's pools included. Villages themselves not checked yet |
| Lootr container swaps (`loot_container_swaps.json`) | patcherMod `LootContainerSwaps` + mixin | DEPLOYED; untested in game |
| Searches skip removed sets (explorer maps, `/locate`) | patcherMod `ChunkGeneratorLocateMixin` + KubeJS `loot/06_dead_maps.js` | DEPLOYED (in the 16:51 jar). Emptied sets now also have no members, so searches never see them (ST-15, ST-16) |
| Ground matching (grass/dirt bases on foreign ground) | patcherMod `worldgen/GroundMatch` + 2 mixins, CEConfig `[worldgen.ground_match]`, tags in `blocktags.js` | DEPLOYED 2026-09-27 (jar md5 729953e6), 37 headless checks pass (`tools/test_worldgen_tools.sh`). ST-27 to ST-32 |
| Worldgen config: a switch per system, debug structure density, solver constants | CEConfig `[worldgen]` via `worldgen/WorldgenConfig`; read by the KubeJS applier and every patcherMod worldgen class | DEPLOYED 2026-09-27. Density exact for every live set (244 node checks), node and Rhino identical at x4, x0.3 and groups off. ST-33 to ST-38 |
| `/cdevplace`: every structure or template of a filter in rows (flat-world testing) | patcherMod `worldgen/MassPlacer` | DEPLOYED 2026-09-27; filter/row/sign/alignment logic tested headless (30 checks); a real run on a scratch dedicated server placed 55 of 56 T&T structures after the watchdog fix. ST-39 to ST-42 |
| Data-only structure mods converted to `kubejs/data` (33 jars, 56 structures) | `tools/port_structure_jars.py` | Done 2026-09-27; scratch server boots, 56 of 56 placed by `/cdevplace`, live sets match. Client untested. ST-43 |
| Biome rules: only water structures start in oceans and rivers | `biome_rules` in `structure_groups.json` + tag `water_ok`; patcherMod `worldgen/StructureBiomeRules` + `ChunkGeneratorStructureBiomeRulesMixin`, CEConfig `structureBiomeRules` | DEPLOYED with 1.0.4 (2026-10-04); 6 Java checks, node/Rhino parity; a scratch dedicated server applied it (25 kept out). ST-44 to ST-46 |
| Improved Pillager Outpost as a 1-in-8 tower, not a replacement; IPO template names fixed; driftwood at 0.25x | `kubejs/data/{minecraft,ipo,upgrade_aquatic}` | DEPLOYED with 1.0.4 (2026-10-04); the scratch server loaded the pools at 7:1 and created the world with the driftwood overrides. ST-47, ST-48 |
| Monobank banks 5x rarer in villages | `pool_tags.json` tags `monobank/vault_houses` x0.18 and `monobank/vault_streets` x0.16 | DEPLOYED with 1.0.4 (2026-10-04); chance per pool computed from the 2026-10-03 pool report, not yet read back from a game load. ST-49 |
| Driftwood: in the water at the 0.25x rate, on land about a tenth of that | `kubejs/data/upgrade_aquatic` (5 placed features gated to water) + `kubejs/data/creativeengineering` (3 land placed features + 3 biome modifiers) | DEPLOYED with 1.0.4 (2026-10-04); the 8 placement lists parse against the 1.19.2 codecs headless. ST-50 |
| Earth fill: cancel a structure whose foundation needs too much earth over water or air | patcherMod `worldgen/EarthFill` + `ChunkGeneratorEarthFillMixin`, CEConfig `[worldgen.earth_fill]`, tags in `blocktags.js` + `worldgen/earth_fill.js` | DEPLOYED with 1.0.4 (2026-10-04); 21 headless checks + a vanilla terrain simulation. ST-51 to ST-55. Beard-aware counts, mound columns and size limits (7.3.1) BUILT 2026-10-13, DEPLOYED with 1.0.6, 48 headless checks + simulation; ST-158 to ST-162 |
| Water fill: cancel a water structure whose water would replace too much dry ground or air | `worldgen/EarthFill` (same hook), CEConfig `[worldgen.water_fill]`, tags in `worldgen/water_fill.js` | BUILT 2026-10-08, in patcherMod 1.0.5 (in `mods/`), DEPLOYED with pack 1.0.6 (2026-10-14; built for 1.0.5, which never shipped); the jar booted in single player 2026-10-11 without mixin or KubeJS errors; 32 headless checks; defaults not calibrated. ST-56 to ST-60 |
| Terrain adaptation by tag (none / bury / beard_thin / beard_box / **soft**) and template trim (no square pads or air notches) | patcherMod `worldgen/TerrainAdaptation` + `TemplateTrim`, 2 new mixins + the ground-match mixin, CEConfig `[worldgen.terrain_adaptation]`, tags in `worldgen/terrain_adaptation.js` + `blocktags.js` | BUILT 2026-10-09, in patcherMod 1.0.5 (in `mods/`), DEPLOYED with pack 1.0.6 (2026-10-14; built for 1.0.5, which never shipped); the jar booted in single player 2026-10-11 without mixin or KubeJS errors; 50 headless checks (soft beard equals vanilla's at skirt 8, real templates trimmed as a python read predicts); mixin targets checked against the Forge-patched SRG bytecode. ST-61 to ST-68 |
| Meta structures: a village with 1 to 3 peaceful towers, a driftwood flotilla at sea (3.16) | `meta_structures.json` + `meta/*` tags, staged by `structure_overrides.js`; patcherMod `worldgen/MetaStructure` (structure type `creativeengineering:meta`) + `MetaFeaturePiece`, CEConfig `metaStructures` / `[worldgen.meta_structures]` | BUILT 2026-10-11, in patcherMod 1.0.5 (in `mods/`), DEPLOYED with pack 1.0.6 (2026-10-14; built for 1.0.5, which never shipped); the jar booted in single player 2026-10-11 without mixin or KubeJS errors (`structure_overrides.js` loaded `MetaStructure` and `MetaSlots`); 26 headless checks (`MetaTest`), node/Rhino parity incl. the old-jar path; a scratch dedicated server generated both (towers and driftwood in place). ST-69 to ST-76 |
| The other 13 meta structures (ruin clusters, sunken ruins, wrecks, rafts, pillager towers/forts/camps with siege weapons, a pillager fleet), slot conversion so metas never change a structure's rate, `/locate` of structures inside metas (3.16) | the same files + patcherMod `worldgen/MetaSlots`, `ChunkGeneratorMetaLocateMixin`, `StructureCheckMetaMixin`, the conversion in `ChunkGeneratorStructureFrequencyMixin` | BUILT 2026-10-11, in patcherMod 1.0.5 (in `mods/`), DEPLOYED with pack 1.0.6 (2026-10-14; built for 1.0.5, which never shipped); the jar booted in single player 2026-10-11 without mixin or KubeJS errors; 35 headless checks (`MetaTest`), node/Rhino parity; scratch server: see 3.16. ST-84 to ST-92 |
| Biome fits: towers in every biome their wood belongs to, Terralith included (3.17) | `biome_fits.json` + `tower`/`tower/*` tags (renamed `land/*` 2026-10-12); staged by `structure_overrides.js`; patcherMod `worldgen/StructureBiomeFits` + `StructureBiomesMixin`, CEConfig `structureBiomeFits`; rates by `gen_structure_groups.py --fit-rates` | BUILT 2026-10-11, in patcherMod 1.0.5 (in `mods/`), DEPLOYED with pack 1.0.6 (2026-10-14; built for 1.0.5, which never shipped); the jar booted in single player 2026-10-11 without mixin or KubeJS errors; 25 headless checks (`FitTest`), node/Rhino parity; two scratch dedicated server runs applied all 13 and `/locate` found 12 towers, each in a fitted biome. ST-77 to ST-83 |
| Biome fits for every land structure: 161 structures on `land/*` biome-type and wood families, towers folded in, vanilla outpost by its dark oak, sky village over the oceans (3.17, plan 7.2) | `biome_fits.json` (29 fits) + `land/*` tags in `structure_tags.json`; `fit_keep` + capped report in `gen_structure_groups.py --fit-rates` | BUILT 2026-10-12, data only, DEPLOYED with 1.0.6 (the patcherMod 1.0.6 rebuild only renames comments); 30 headless checks (`FitTest`), node/Rhino parity, all 7 worldgen suites; a scratch dedicated server applied 29 fits to 163 structures. ST-93 to ST-101 |
| Way signs in `group/way_signs` at their current rate; every `settlements` member (metas, fortified villages included) a sign destination (3.21, plan 7.1) | `group/way_signs` tag + group entry; `structure_overrides.js` adds `group/settlements` to `#supplementaries:way_sign_destinations`; `RUNTIME_BIOME_TAGS` in `gen_structure_groups.py` | BUILT 2026-10-12, data only, DEPLOYED with 1.0.6; node sim clean. ST-127 to ST-130 |
| Standalone structures folded into groups: statues and cats -> `ruins`, It Takes a Pillage -> `pillager_settlements`, Nether ruins -> new `nether_ruins`, Hopo's fossils -> `ocean_ruins`, fresh watchtower -> `remote` (3.20, plan 7.1) | `group/*` tags in `structure_tags.json`, rates in `structure_groups.json` (sync) | BUILT 2026-10-12, data only, DEPLOYED with 1.0.6; node sim clean. ST-117 to ST-123 |
| Entrance dungeons: YUNG's zombie and spider dungeons in `group/entrance_dungeons`, land only, with a surface check so a crypt staircase or cave mouth never comes up in water (3.19, plan 7.1) | `group/entrance_dungeons` + `surface_check` tags, `biome_rules.water.surface_check`; patcherMod `StructureBiomeRules.surfaceCheck` via `ChunkGeneratorStructureBiomeRulesMixin` | BUILT 2026-10-12 in patcherMod 1.0.6, DEPLOYED with 1.0.6; builds, node sim clean; not run in game. ST-112 to ST-116 |
| Replacements (a member takes a share of another member's slots) and `group/witch_huts`: YUNG's hut, witch circle, witch village, and the vanilla swamp hut in 10% of YUNG's hut slots, swamps only (3.18, plan 7.1) | `replaces` + `share` in `structure_groups.json`, staged by `structure_overrides.js`; patcherMod `worldgen/MetaSlots` (conversion, biome narrowing, `/locate`), CEConfig `structureReplacements`; YUNG's "Disable Vanilla Witch Huts" = false | BUILT 2026-10-12 in patcherMod 1.0.6, DEPLOYED with 1.0.6; 7 new headless checks (`MetaTest` 55), node/Rhino parity incl. the replacement table and the old-jar path; not run in game. ST-102 to ST-111 |
| Fixes from the scratch run (3.23): pool order fixed per seed, code-added village pieces kept on reload, ground match over deep structures, driftwood on banks, cave fit for the 3 deep dungeons, stone bases in earth fill | patcherMod `PoolTuner`, `GroundMatch`, `EarthFill`, CEConfig `[worldgen.pools] canonicalOrder`; `biome_fits.json` + `structure_tags.json` (`land/any/deep`); `kubejs/data/upgrade_aquatic` driftwood; `earth_fill/stone_base` tags | DEPLOYED with 1.0.7 (2026-10-18, patcherMod 1.0.7 + kubejs data). Offline checks pass (PoolTunerTest new, 10 checks); ST-191 to ST-198 pending |
| Earth fill on water, camp plinths, metas off coasts (7.5): the footprint water share (`maxWaterSurplus` 0.5; 0 since 2026-10-30), sunk-start beard for the 6 Campsite camps (start height -4), `avoidCoasts` switch (off; on since 2026-10-30, scratch queue SQ-WG30-E01, E02) | patcherMod `worldgen/EarthFill`, `worldgen/TerrainAdaptation` (`terrain_adaptation/sunk_start`), `worldgen/MetaStructure` (`coast_river` biome tag, `meta/coast_ok`); CEConfig `[worldgen.earth_fill] maxWaterSurplus`, `[worldgen.meta_structures] avoidCoasts` | BUILT 2026-10-19 (patcherMod 1.0.9), scratch-tested (ST-199 to ST-205, testing/structures/12_earth_fill_camps.md); DEPLOYED with 1.0.9 (2026-10-19 15:54) |

**Open first:** [structure_groups.json](../kubejs/generation_scripts/worldgen/structure_groups.json) (every rate),
[structure_tags.json](../kubejs/generation_scripts/worldgen/structure_tags.json) (`group/*` = who shares a grid),
[pool_config.json](../kubejs/generation_scripts/worldgen/pool_config.json) / [pool_tags.json](../kubejs/generation_scripts/worldgen/pool_tags.json) (village houses),
the report the game rewrites on every load, [structure_tag_report.md](../kubejs/generation_scripts/worldgen/structure_tag_report.md),
and the offline estimate [structure_group_estimates.md](../kubejs/generation_scripts/worldgen/structure_group_estimates.md).

**Knobs:**
- Per group: `placement.spacing` (top density, one per spacing x spacing chunks) and `placement.separation` (minimum
  gap between any two members, in chunks).
- Per structure in a group: `weight` (wins shared slots), `frequency` (0..1, rarer; a missed roll leaves the slot empty),
  `enabled`.
- Per standalone set: `placement` overrides; per structure `frequency` / `enabled`.
- Membership: the `group/*` tags, then `python3 tools/gen_structure_groups.py`.
- Where a structure may spawn: a biome fit (3.17): its biome list in `biome_fits.json`, the structure in the fit's tag
  in `structure_tags.json`, then `python3 tools/gen_structure_groups.py --fit-rates`.
- Per village house: `current` weight / `enabled` in `pool_config.json`, or a `multiplier` in `pool_tags.json`.
- Lootr: block → vanilla container pairs in `loot_container_swaps.json`.
- `config/creativeengineering-common.toml` `[worldgen]` (also in Create's config screen), the layer above the JSON tables:
  - `[worldgen.systems]`: one on/off per system (structureGroups, structureFrequencies, poolTuning, lootContainerSwaps,
    biomeLayout, deadPlacementFilter, groundMatch, structureBiomeRules, structureBiomeFits, earthFill,
    waterFill, terrainAdaptation, templateTrim).
  - `[worldgen.debug] structureDensity`: every structure's rate times x (testing only).
  - `[worldgen.pools]`, `[worldgen.biome_solver]`: the solvers' constants.
  - `[worldgen.ground_match]` (+ `.sampling`), `[worldgen.earth_fill]` (limits, 3.13), `[worldgen.water_fill]` (3.14),
    `[worldgen.terrain_adaptation]` (soft beard `skirtBump`, 3.15), `[worldgen.mass_place]` (`/cdevplace` layout).
- Ground matching: `[worldgen.ground_match]` (mode, baseLayers, nativeShare, minSamples, families, log, sampling); which
  blocks are base / plants / not-ground: the `creativeengineering:ground_match/*` block tags in `blocktags.js`; opt a
  structure out: `creativeengineering:ground_match/exempt` in `worldgen/ground_match.js`.
- Terrain around a structure (3.15): the structure tags `creativeengineering:terrain_adaptation/{none,bury,beard_thin,beard_box,soft}`
  and `creativeengineering:template_trim` in `worldgen/terrain_adaptation.js`; pad / plant blocks: the
  `creativeengineering:template_trim/*` block tags in `blocktags.js`; soft skirt width: `[worldgen.terrain_adaptation] skirtBump`.
- Meta structures (3.16): definitions in `meta_structures.json` (`share` = the share of its anchors' slots a meta takes),
  who may take part in the `meta/*` tags of `structure_tags.json`; `[worldgen.systems] metaStructures`,
  `[worldgen.meta_structures] log`.
- Inspecting structures: `/cdevplace preview structures <filter>`, `/cdevplace structures [by type|group|mod|none] <filter>`,
  `/cdevplace templates <glob>` (3.10).

**Applying:** rejoin the world (worldgen is read at world load; `/reload` only refreshes the report). Only chunks
generated afterwards change.

**Measuring:** `python3 tools/gen_structure_groups.py --check` (expected rate of every structure), `[worldgen.debug]
structureDensity` to see many structures fast, `/cdevplace` to see every structure of a mod side by side,
`/locate structure <id>`, the report, and `/dumpstructuregen` + `python3 tools/check_structure_live.py` for the live sets.

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
| Structure sets in the baseline | 179 | 97 from mod jars, 15 from the vanilla table (vanilla builds them in Java), 66 from `kubejs/data` (MCreator and other data-only structure mods converted to data, see 3.11), 1 registered in Java by a mod (`ae2:meteorite`) |
| Structures in those sets | 269 | every one has a `mod/*` tag and a `type/*` or `dungeon/*` tag, except the 2 `takesapillage` ones (added before this batch, still untagged) |
| Groups | 10 | 187 structures on 9 shared grids, plus the 15 meta structures (2026-10-11; `flotsam` is the 10th group); the other 82 stay in standalone sets |
| Structure tags | 122 | 76 `mod/*`, 20 `type/*`, 12 `dungeon/*`, 9 `group/*`, 5 meta (`dungeons`, `pillager`, `nether`, `end`, `ruins`) |
| Live structures in no set | 142 | never generate on their own: 133 `terralith:zpointer/*` biome markers, the 7 ruined portals (removed on purpose), `nullscape:phantom_nest`, `terralith:underground_cabin` |
| Jigsaw pools | 609 | 3571 pieces, read live from the game, so code-injected houses are included |
| Pool tags | 15 | 4 tech-house mods at x0 + `tech` x0, 10 other house mods at x1 |

### 2.2 The groups

| Group | Members | Spacing | Separation (min gap) | Notes |
|---|---|---|---|---|
| `settlements` | 35: vanilla and T&T villages, Terralith fortified villages, C.C.C.'s red roof castle (7.4.4), their metas | 48 | 12 (208 blocks) | set by hand 2026-09-27 (was 24 / 10); The Missing Villages removed the same day |
| `pillager_settlements` | 47: vanilla outpost + every T&T tower/fort/camp outpost but the ship, Mysticrift towers, Takes A Pillage, C.C.C.'s pillager camp and fake trader camp (7.4.3), the pillager metas | 32 | 8 (144 blocks) | kept 4 chunks off `settlements` grid slots; siege weapons moved to `vehicles` 2026-10-14 |
| `ruins` | 31: Philip's surface ruins, Forest Ruins, Terralith glacial hut, statues and cats (3.20), C.C.C.'s ruined drum tower (7.4.4), the ruin metas | 16 | 8 (144 blocks) | underground ruins stay standalone |
| `surface_dungeons` | 7: YUNG's jungle temple, the vanilla jungle temple (a replacement, 10% of YUNG's slots, 3.18; the desert pair moved to `desert_temples` 2026-10-23, 3.24), mansion, Terralith spire, T&T mimic, Philip's badlands dungeon, C.C.C.'s undead castle (7.4.4) | 16 | 10 (176 blocks) | witch huts moved out 2026-10-12 |
| `way_signs` | 1: Supplementaries' way sign | 19 | 10 (176 blocks) | added 2026-10-12; the old set's placement, kept 8 chunks off `settlements` grid slots (3.21) |
| `nether_ruins` | 4: Twigs' bloodstone obelisk, Philip's Nether dungeon, sealed ghast statue, lava ruins | 14 | 7 (128 blocks) | added 2026-10-12 (7.1 decision 6, widened to all Nether ruins); grid from their crowding (3.20) |
| `entrance_dungeons` | 2: YUNG's zombie dungeon (crypt) + spider dungeon (spider cave) | 32 | 16 (272 blocks) | added 2026-10-12 (7.1 decisions 7, 8); land only, surface check (3.19) |
| `witch_huts` | 4: YUNG's witch hut + witch circle, Villages & Pillages witch village, vanilla swamp hut (a replacement, 3.18) | 16 | 8 (144 blocks) | added 2026-10-12 (7.1 decisions 2, 3); swamps only (`land/swamp`) |
| `desert_temples` | 2: YUNG's desert temple, the vanilla desert pyramid (a replacement, 10% of YUNG's slots) | 32 | 20 (336 blocks) | added 2026-10-23 (3.24): twice the surface dungeons' separation; temple frequency 0.285 keeps 1 per 63; rank 82, hostile |
| `vehicles` | 8: the 6 Illager Siege Weapons camps, C.C.C.'s pillager loot carriage (with the villager chase and golem prison van) and witch wagon | 52 | 16 (272 blocks) | added 2026-10-14 (7.4.5); hostile, rank 76, kept 4 chunks off `settlements` grid slots; `biome_frequency` harsh 1.0 / default 0.5 |
| `ocean_ruins` | 14: vanilla, Hopo's and Philip's ocean ruins, Philip's ocean fortress, shipwrecks, T&T wreckage, fossils (3.20), the ocean metas | 8 | 3 (64 blocks) | |
| `ocean_dungeons` | 1: ocean monument | 32 | 6 (112 blocks) | the monuments' own salt; monument frequency 0.1 |
| `sky` | 4: Cloud Storage balloon + sky temple, AE2 meteorite, sky village | 64 | 16 (272 blocks) | |
| `boats` | 12: T&T ship village + pillager ship, rafts, the other pillager ships, the boat metas | 48 | 28 (464 blocks) | |
| `flotsam` | 1: the driftwood flotilla (meta structure, 3.16) | 32 | 8 (144 blocks) | added 2026-10-11 |
| `remote` | 69: T&T wandering trader camp + every single-building house mod in the Overworld, the friendly camps (C.C.C.'s trader camp and Campsite Structures' 6 camps since 7.4), the remote metas | 24 | 12 (208 blocks) | 2 Farmer's Structures houses whose biomes come from absent mods stay out |

Every spacing and separation above was set by hand on 2026-09-27 (late evening); member weights and frequencies are
still the migration fit, so each group's rate changed by (old spacing / new spacing)^2: settlements x0.25, ocean ruins
x0.25, boats x0.73, remote x1.13, surface dungeons x1.27, ruins x1.9, sky x2.9, the other two unchanged.
Per-biome detail and holes: [structures_by_biome.md](structures_by_biome.md) (last regenerated 2026-10-12, before 7.4).
Member counts and grids refreshed 2026-10-14 from the world-load report (counts include each group's meta structures;
some grids were changed after the 09-27 hand pass, see the history).

Standalone (73 structures): nether, End, underground (dungeons, ancient city, mineshafts, Terralith and Philip's
underground sets, buried treasure), strongholds, Hopo's fossils, way signs, ruined portals (all off),
and the YUNG's-blocked vanilla desert and jungle temples. Since 2026-10-13/14 also Subterrestrial's 8 underground
cabins and C.C.C.'s piglin camp (Nether, 7.4.6); `eugene:castle` is registered but in no set (split in 7.4.4).

### 2.3 How a group places structures

- **Slots:** one per `spacing x spacing` chunk cell, at least `separation` chunks from the cell edge, so two members
  never start closer than `separation + 1` chunks (start to start: a village's edge can still reach its neighbour's
  when both are wide).
- **Who:** the members that fit the biome at the slot compete by `weight` (vanilla: a pick that can't generate there
  falls through to the next).
- **Frequency:** the winner then spawns with chance `frequency`. A miss leaves the slot **empty**: no fall-through, so
  lowering one member never makes another more common. Rolled by patcherMod from (seed, chunk, structure), the same
  roll `/locate` uses.
- **Rate:** where a member is alone in its biome it gets `frequency / spacing^2` structures per chunk^2 ("alone: 1 per N"
  in the report); where members share a biome they split the slots by weight. `structure_group_estimates.md` works
  this out per biome with the real biome areas.
- **Standalone sets** work the same way on their own grid (the report says "alone: 1 per N" too).

### 2.4 Pool weights

Weights are relative inside one pool. Vanilla houses are 2 to 4 each, IE's 1 each, Create: Dynamic Village's 20
each, Domestication Innovation's pet shop 17, Monobank's vault 15, Wares' warehouse 10, Advanced Peripherals' house
10, PneumaticCraft's mechanic house 8, Chef's Delight's houses 2 to 5, Cloud Storage's balloon stand 2. Lowering a
piece makes the rest of the pool (including its `minecraft:empty` lot) more likely; the number of houses in a
village doesn't change. Current pool tags remove 52 tech pieces: IE 25, Dynamic Village 20, PneumaticCraft 7
(Advanced Peripherals' 5 were removed the same way until its own config turned them off with 1.0.6). Example: in `minecraft:village/desert/houses` a Dynamic Village house was 8.8% of each
draw; now it's 0, and a vanilla desert house goes from 0.9% to 1.5%.

## 3. What the pack changes

### 3.1 Structure groups (KubeJS + patcherMod)

- `tools/gen_structure_config.py` builds the read-only baseline `structure_config.json` from the jars, `kubejs/data`,
  the vanilla table and the code-registered table (set definitions, members, biomes, `blocked` notes, and a
  `meta_structures` list for later). It drops sets whose mod is gone (prints `dropped <set>`).
- `structure_groups.json` holds every rate: groups (placement + per-member weight / frequency / enabled) and standalone
  sets (placement overrides + per-member frequency / enabled).
- At every world load, `kubejs/server_scripts/worldgen/structure_overrides.js` (in `ServerEvents.highPriorityData`):
  writes each group as `creativeengineering:group/<name>`; rewrites every original set a structure left (a set with no
  live member gets no structures and frequency 0); writes standalone sets that differ from their default; hands the
  frequency table to patcherMod (`StructureFrequencies.stage`); rewrites `structure_tag_report.md`.
- patcherMod `worldgen/StructureFrequencies` freezes that table at `ServerAboutToStartEvent` against the loaded
  registries (log: `[structure-frequency] N structure(s) in M set(s) roll a frequency below 1`).
  `ChunkGeneratorStructureFrequencyMixin` rolls a member right before its start is stored in
  `ChunkGenerator#tryGenerateStructure` (`m_223104_`) and vetoes the rest of that slot on a miss;
  `StructureCheckFrequencyMixin` answers `StructureCheck#canCreateStructure` (`m_226755_`, the `/locate` / map
  prediction) with the same roll.
- **Any config error applies nothing** (every structure uses its mod default), logged and listed in the report: unknown
  id or tag, a structure in two groups, a bad field or value, an invalid placement. An old patcherMod jar (no
  `StructureFrequencies`) also applies nothing, since a group without its frequencies would spawn every member at full
  rate. A stale standalone entry for a structure that moved into a group is only a warning.
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
- `mod/veggiesdelight` (the 5 depot houses Veggies Delight adds to vanilla villages; pool multiplier 0.5, [veggies_delight.md](veggies_delight.md) 4.12)
- `mod/villager_houses` groups the 14 one-building mods (each is a lone house with a villager in it)
- `type/house` now includes `#mod/villager_houses` and `simply_houses:*` (Simply Houses' buildings have no villagers;
  its "forest fort" is house-sized, 22×24×29, so it stays a house rather than `type/fort`, which also avoids a set split)
- `ae2:meteorite` is in `type/environmental`

These houses ran at x0.66 of their mods' rate under the old `type/house` multiplier; the migration kept that rate as
their frequencies in the `remote` group.

### 3.4 Structures other mods block in code (added 2026-09-27)

YUNG's mods stop some structures inside the generator, whatever their set says. The scanner reads the current
mod configs and writes a read-only `"blocked"` note on each affected structure entry. The report flags them in the
group and standalone tables and lists them under Coverage.

| Structure | Stopped by | Option |
|---|---|---|
| ~~`minecraft:desert_pyramid`~~ | ~~YUNG's Better Desert Temples~~ | "Disable Vanilla Pyramids" = **false** since 2026-10-12: 10% of YUNG's temple slots (3.18) |
| ~~`minecraft:jungle_pyramid`~~ | ~~YUNG's Better Jungle Temples~~ | "Disable Vanilla Jungle Temples" = **false** since 2026-10-12: 10% of YUNG's temple slots (3.18) |
| ~~`minecraft:swamp_hut`~~ | ~~YUNG's Better Witch Huts~~ | "Disable Vanilla Witch Huts" = **false** since 2026-10-12: the swamp hut spawns in a share of YUNG's hut slots (3.18) |
| `minecraft:fortress` | YUNG's Better Nether Fortresses | `betterfortresses-forge-1_19.toml` "Disable Vanilla Nether Fortresses" = true |
| `minecraft:stronghold` | YUNG's Better Strongholds | always, no option |
| `betterdungeons:small_nether_dungeon` | YUNG's Better Dungeons | `betterdungeons-forge-1_19.toml` "Enable Small Nether Dungeons" = false |

Until 2026-10-12 the three blocked vanilla temples were kept out of the groups in their own sets; now each is a 10%
replacement of its YUNG's version (3.18); the blocked stronghold and fortress are standalone. In `minecraft:nether_complexes`, a cell that picks the
blocked fortress falls through to the bastion. Tune the YUNG's replacements instead.

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
- Since 1.0.7 (DEPLOYED 2026-10-18, 3.23): every pool is first sorted into a stable order (`[worldgen.pools]
  canonicalOrder`), and pieces added in code get the world's processor lists, so they survive a chunk reload.

### 3.6 Lootr container swaps (patcherMod)

Lootr only converts vanilla chests, barrels, trapped chests and shulker boxes. `loot_container_swaps.json` turns a
listed block that a structure template places **with a loot table** into a vanilla container (facing kept), which
Lootr then converts: IE crates and Farmer's Delight cabinets/baskets → barrel, PneumaticCraft reinforced chest →
chest. Hook: `mixin/StructureTemplateLootSwapMixin` at the return of Forge's `StructureTemplate.processBlockInfos`,
which every template placement goes through. `loot_container_audit.md` (re-run 2026-10-02: 3,874 loot containers)
leaves only Nullscape's phantom-hive droppers, Monobank's bank blocks and Unwrecked Ships' trap dispensers (12, like
vanilla jungle-temple dispensers) unconverted, on purpose.

### 3.7 Existing configs that modify structure generation

**This pack's own** (all in `kubejs/generation_scripts/worldgen/`, applied at world load):

| File | Changes | Current state |
|---|---|---|
| `structure_groups.json` | every structure rate: 9 groups + 54 standalone sets | migrated from the old hand edits and 21 tag multipliers (2026-09-27) |
| `structure_tags.json` | `group/*` membership + classification | 9 groups; no multipliers |
| `structure_config.json` | read-only baseline | rebuilt by the scanner |
| `pool_config.json` / `pool_tags.json` | village house weights | no per-piece edits; tags remove the 4 tech-house mods |
| `loot_container_swaps.json` | loot containers in templates | 4 swap rules |
| ~~`biome_merges.json`~~ | retired 2026-10-11 (archived in `kubejs/archive/worldgen/biome_merges_2026-10-11/`, deleted 2026-10-22); **where** structures spawn is biome fits now (3.17, 7.2) | was 209 biome adds in 45 `has_structure/*` tags |
| `biome_config.json` | biome shares; structures follow their biomes | 9 overworld + 3 End biomes tuned |

**Mods' own configs** (values as of 2026-09-27; `configs_edited/` has backups of those marked "backed up"):

| Mod | File | Setting | Effect here |
|---|---|---|---|
| YUNG's Better Nether Fortresses | `betterfortresses-forge-1_19.toml` | "Disable Vanilla Nether Fortresses" = true | vanilla fortress stopped in code (3.4) |
| YUNG's Better Desert Temples / Jungle Temples | `betterdeserttemples-forge-1_19.toml`, `betterjungletemples-forge-1_19_2.toml` | "Disable Vanilla Pyramids" / "Disable Vanilla Jungle Temples" = **false** (2026-10-12, backed up, mirrored) | vanilla temples allowed as 10% replacements (3.18); YUNG's locate mixins only act while true |
| YUNG's Better Witch Huts | `betterwitchhuts-forge-1_19.toml` | "Disable Vanilla Witch Huts" = **false** (2026-10-12, backed up, mirrored to the server's `configs_edited/`) | the vanilla swamp hut is allowed; the pack decides its rate (3.18). YUNG's `LocateVanillaWitchHutCommandMixin` only blocks `/locate` while the option is true |
| YUNG's Better Strongholds | `betterstrongholds-forge-1_19.toml` | cobweb/torch/lantern rates | contents only; the vanilla stronghold is always stopped |
| YUNG's Better Dungeons | `betterdungeons-forge-1_19.toml` | "Enable Small Nether Dungeons" = false; small-dungeon chest/banner counts | small nether dungeons never generate (3.4); the rest is contents |
| YUNG's Extras | `YungsExtras/forge-1_19/wishing_wells.json` | wishing-well loot blocks | contents only; its ~60 features have no frequency option |
| Towns & Towers | `towns_and_towers/structure_rarity.json5` | villages 48/24, outposts 48/24 freq 0.2, others 32/16 | same as its jar sets; this framework's overrides win (live check, 0 mismatches). Leave at defaults |
| Towns & Towers | `towns_and_towers/structure_enable_or_disable.json5` | all true | nothing disabled |
| Philip's Ruins | `philips-ruins-common-configs.toml` | "Enable Configs" = false | its separation/density and per-type toggles are inactive; its rates are in the `ruins` group and standalone sets |
| Applied Energistics 2 | `ae2/common.json` | generateMeteorites = true | false would stop AE2 registering `ae2:meteorite` at all (see 4) |
| Supplementaries | `supplementaries-common.toml` (backed up) | `[building.sign_post.way_sign] enabled = true` | way-sign structures on; also tunable as `supplementaries:way_sign` |
| Cloud Storage | `cloud-storage.toml` | `balloon_stand_spawn_weight` = 2 | village-pool weight of its balloon stands (seen by PoolTuner); its 2 sky structures are the `sky` group |
| Chef's Delight | `chefsdelight-common.toml` | chef/cook house weights 2 to 5 per biome | village-pool weights |
| Domestication Innovation | `domestication-innovation.toml` (backed up) | `petstore_village_weight` = 17 | village-pool weight |
| Wares | `wares-common.toml` | GenerateWarehouses = true, WarehouseWeight = 10 | village-pool weight |
| Monobank | `monobank-common.toml` | GenerateVaults = true, VaultWeight = 15 | village-pool weight |
| Advanced Peripherals | `Advancedperipherals/world.toml` | enableVillagerStructures = false (since 1.0.6), villagerStructureWeight = 10 | no scientist houses at all; the pool tag `mod/advancedperipherals` x0 now matches nothing (`pool_tag_report.md` warning) |
| PneumaticCraft | `pneumaticcraft-common.toml` (backed up) | `addMechanicHouse` = 8 | village-pool weight (pool tag x0 removes it anyway) |
| Waystones | `waystones-common.toml` | spawnInVillages = true, forceSpawnInVillages = false | waystone pieces in village pools |
| Lootr | `lootr-common.toml` | decay/refresh_structures empty | converts structure loot containers per player |

**Data-pack overrides from mods** (not configs, but they change the baseline): Terralith replaces
`minecraft:villages` (spacing 26 instead of 34) and vanilla's monster room, deep monster room, fossil and desert-well
placed features; Nullscape replaces `minecraft:end_cities`; HopoBetterUnderwaterRuins replaces `minecraft:shipwrecks`
(spacing 12) and moves both shipwrecks onto beaches and rivers. The scanner reads these, so `original` already shows them.

### 3.8 Ground matching (patcherMod, added 2026-09-27)

Structures carry the ground they were built on. Philip's `ancient_ruins` (46 templates) and `ancient_towers` (9) sit on
a grass + dirt pad: about half of every template's bottom two layers is `grass_block`/`dirt`. Both spawn in
`terralith:volcanic_peaks`, through `forge:is_peak`/`c:mountain_peak`-style tags. The other structures that can spawn
there have no such pad, or are underground: the AE2 meteorite, Cloud Storage's sky pair, the vanilla outpost,
dungeons, strongholds, mineshafts, Lost Soul City, the bone dungeon. Philip's `ancient_dungeon` has a dirt floor, but
it sits at y -30.

Rather than dropping those structures from the biome (the first idea) or shipping re-grounded copies of every template
(static, one copy per structure per biome), the base is matched to the ground as it is placed:

1. `mixin/StructureStartGroundMatchMixin` notes which structure start is placing (`StructureStart#placeInChunk`, used
   by world generation and `/place structure`). `mixin/StructureTemplateGroundMatchMixin` hooks the return of Forge's
   `processBlockInfos`, the same hook as the Lootr swaps. Template placements outside a structure start (structure
   blocks, `/place template`) are left alone.
2. For the chunk being placed (the on/off switch is `[worldgen.systems] groundMatch`), `GroundMatch` samples the **natural ground**: the top solid block of each column
   (ocean-floor heightmap, skipping leaves/logs/cactus and other `ground_match/not_ground` blocks that neighbours'
   trees spill in) and the 3 blocks under it. The grid covers this chunk (every 2nd column) and its 8 neighbours (every
   4th), about 190 columns, and skips every column any structure piece covers, including the structure being placed.
3. Each **family** (a block tag; default `creativeengineering:ground_match/grass` = grass block, dirt, coarse dirt,
   podzol, rooted dirt, mycelium) is **native** when at least `nativeShare` (35%) of the sampled tops belong to it.
   Native: the template is placed untouched. With fewer than `minSamples` (16) natural columns, nothing changes.
4. Otherwise the family's blocks in the template's bottom `baseLayers` (3) layers are swapped, depending on `mode`:
   - `TERRAIN` (default): where natural ground is already at that spot, the block is skipped, so the real terrain
     stays. Anywhere else it gets a sampled block: a top block if nothing solid sits on it in the template, an
     under-block if something does.
   - `PALETTE`: every base block gets a sampled block.
   - `REMOVE`: base blocks are never placed.

   Picks are weighted by the sample's own mix (for example 3 basalt : 1 blackstone) and seeded from the position.
   Plants on a swapped block are dropped too (`ground_match/plants`: flowers, saplings, grass, ferns, both halves of
   tall ones).

Nothing is set per biome or per structure. A plains ruin keeps its grass. The same ruin gets basalt/blackstone on
volcanic peaks, sand/sandstone in a desert, and snow or stone on snowy slopes. To cover another kind of base (a sand
pad, say), add a family tag in `blocktags.js` and list it in `families`. With `log = true`, every changed template logs
its chunk, the family's share, and both palettes. The sampling grid, under-block depth, dig limit and plant height are
in `[worldgen.ground_match.sampling]`.

Since 1.0.7 (DEPLOYED 2026-10-18, 3.23): a column is skipped only when a structure piece reaches its sampled ground,
not whenever one covers it in XZ, so a mineshaft or an ancient city far below no longer empties the sample.

### 3.9 CEConfig `[worldgen]`: switches, debug density, constants (added 2026-09-27)

Every worldgen system reads its switch and constants through `worldgen/WorldgenConfig`. If the config isn't loaded
yet, it falls back to the defaults, which are the old hard-coded values. The KubeJS scripts call the same class, so the
toml is the one place for all of them. The per-structure, per-piece and per-biome tables stay in the JSON files.

| Key | What it does when changed | Applies |
|---|---|---|
| `systems.structureGroups = false` | The applier ignores `structure_groups.json`, the `group/*` tags and standalone edits: every set is the mod's own (tag errors become warnings) | rejoin |
| `systems.structureFrequencies = false` | No frequency roll: a group member spawns whenever it wins its slot | rejoin |
| `systems.poolTuning = false` | Every pool back to its first-seen weights (the report says why) | rejoin |
| `systems.lootContainerSwaps = false` | Modded loot containers stay modded (no Lootr) | new chunks |
| `systems.biomeLayout = false` | No biome override: vanilla/Terralith layout | new worlds |
| `systems.deadPlacementFilter = false` | Maps and `/locate` search frequency-0 sets again (can hang: test only) | live |
| `systems.groundMatch = false` | Structure bases stay as saved | new chunks |
| `debug.structureDensity` (0.01..100) | Every set's expected rate times x, grouped or not, groups on or off | rejoin |
| `pools.maxFractionScale` (10) | How far a pool may be scaled to make fractional weights whole | rejoin |
| `biome_solver.maxCandidates` (24), `rounds` (20) | Biome solver precision vs world-creation time (part of its cache key) | new worlds |

**Density math** (`scalePlacement` in `structure_overrides.js`): below 1 the set's `frequency` is multiplied, so it's
exact and grids, gaps and exclusion zones are unchanged. Above 1, spacing and separation shrink by sqrt(x), rounded
down, and the frequency absorbs the rounding, so it's exact until a grid hits its minimum (spacing = separation + 1).
Those sets are listed in the warning and in the log as capped. At x30, 6 sets cap: betterdungeons small dungeons, nether
fossils, strongholds (ring count limit 4095), Philip's nether structures, Terralith's underground sets. Concentric rings
get `count` times x. `legacy_type_1` (outposts' old rounding) switches to `default` whenever the frequency changes. The
report and summary line say `DEBUG density xN`, and every live set is written (126 files instead of 96).

### 3.10 `/cdevplace`: every structure or template of a mod, in rows (added 2026-09-27)

For looking at structures side by side, typically in a **superflat world with Generate Structures OFF** and cheats on.
Operator only.

A superflat world (single player) or `level-type=minecraft:flat` (server) stays flat since 2026-10-19:
`mixin/RegistryLoaderPresetOverworldMixin` keeps a world's own overworld when it isn't a multi-noise noise generator,
so the data packs' `dimension/overworld.json` (the biome layout's in the KubeJS virtual pack, Terralith's, Tectonic's)
no longer replace it. Before that every flat world came out as normal terrain (testing/structures/09_phase_9.md 3.2).

- `/cdevplace structures <filter>`: rows by type. `/cdevplace structures by <type|group|mod|none> <filter>` picks another
  grouping. The filter takes terms like `structure_tags.json` values: an id, a glob (`towns_and_towers:*village*`),
  `@namespace`, `#type/tower` (a pack tag), `#minecraft:village` (a real structure tag), and `!term` to exclude.
  Tab-completion offers all of these.
- **Rows by type:** each structure goes in its **smallest** `type/*` tag, so outposts land in camp/fort/tower, not the
  `enemy_building` union. Structures with no type tag get a last row. For Towns & Towers (56 structures) that gives camp
  5, fort 9, ship 2, shipwreck 1, tower 15, village 23, and 1 untagged (`mimic_desert`).
- `/cdevplace templates <glob>`: raw templates, one row per folder, jigsaw blocks turned into what they become. Towns &
  Towers keeps its 742 templates under the `kaisyn` namespace, for example `/cdevplace templates kaisyn:village/*/houses/*`.
  Templates are listed per data pack (`structures/**.nbt`) plus the world's `generated/` folder, not through vanilla
  `listTemplates()`, which throws on a bare `structures.nbt` path in this pack's merged listing (fixed 2026-10-19).
- `/cdevplace preview structures|templates ...` lists the rows without placing. `/cdevplace status` and `/cdevplace stop`
  report on or cancel a run.
- **Layout:** rows start 3 blocks in front of you and run +X; each new row is placed further +Z, with a `rowGap` aisle
  between. A sign faces the aisle for each row and each structure (id, wrapped). Structures are generated exactly as
  `/place structure` does (any biome allowed, whole jigsaw layout), then moved onto the row cursor. `verticalAlign AUTO`
  drops sky structures and lifts underground ones to the ground (KEEP / GROUND also available); surface ones keep their
  height. Work runs in steps (lay out, then generate and build one chunk at a time) until `tickBudgetMs` (40) is used
  each tick, so a 90-chunk village spreads over many ticks; `maxItems` refuses bigger runs (1000). Chat shows progress; every placement
  and failure goes to `latest.log` (`[cdevplace]`).
- Ground matching applies here too (the placing structure start is known), so a sand or snow superflat shows it
  happening at scale.

### 3.11 Structure mods converted to datapack data (added 2026-09-27, night)

Twenty of the 1.19.2-labelled MCreator structure jars added that night only have a `(FMLJavaModLoadingContext)`
constructor, which Forge 43.3.9 can't call, so the game didn't start (`NoSuchMethodException: <Mod>.<init>()`). They
carry only a datapack, so `tools/port_structure_jars.py` copies each one to `kubejs/data/<ns>/` and moves the jar to
`disabledmods/`. The 13 data-only jars that did load were converted too, so they no longer take up mod slots.
33 jars in all, 56 structures:

- **Remapped blocks** (templates saved in 1.20.1): `decorated_pot` becomes `supplementaries:urn` (with its sherds
  dropped), `chiseled_bookshelf` becomes `bookshelf`, and `cherry_*` becomes Vinery's `cherry_*` (the same wood set,
  properties kept). Items 1.19.2 doesn't have (pottery sherds, armor trims, brush, cherry boats) are dropped from
  chests, frames and hands. They would otherwise load as empty stacks anyway.
- **Mysticrift Pillager Towers** (jar named `1.19.2.jar`): its pools use templates re-saved in 1.21, a different item
  and entity format. The pools now point at the 1.20.1 saves of the same towers from the same jar (about 90% the same
  blocks, pillagers placed as entities).
- **Sky Villages 1.0.3**: 37 pool entries name compat templates (supplementaries, villagersplus, waystones, farmer's
  delight, graveyard) that the jar never shipped. They would load as empty pieces, so those entries were dropped.
- **Kept as jars:** Villages & Pillages (custom structure type, processors and mixins), Blocky Siege (a full siege
  mod with one structure), Revamped Shipwrecks and Unwrecked Ships Plus (lowcode datapack jars that load fine). The two
  jars with code are marked in `mod_sides.md`.
- **Revamped Shipwrecks** empties `minecraft:shipwrecks` and wins over Hopo's override of the same set (checked live).
  `type/shipwreck` now lists its two wrecks instead of the vanilla ones.
- **Tags:** a `mod/*` tag per namespace, plus `mod/villager_buildings` (16 one-building villager mods) and
  `mod/villager_camps` (4 friendly tents and camps). Group membership: buildings and camps go to `remote`; Mysticrift's
  towers and the siege-weapon camps to `pillager_settlements`; the ships and rafts to `boats`; Forest Ruins, the
  abandoned watchtower and the forgotten church to `ruins`; Sky Villages to `sky`; the witch village to
  `surface_dungeons`; the shipwrecks to `ocean_ruins`. Standalone: the statues (`type/decoration`), the fresh watchtower
  (`type/tower`) and the Nether ship.
- **Side effect:** the groups keep their grid, so new members take slots from the old ones. From the estimates: the
  old one-building houses in plains drop to about x0.45 of their earlier density, T&T outposts to x0.44 to x0.88, the
  T&T ship village and pillager ship to about x0.27, witch huts to x0.7, and farmer's structures / Simply Houses to
  about x0.6 to x0.8. To win that back, lower the affected groups' `spacing` in `structure_groups.json`.

### 3.12 Water biomes, IPO outposts, driftwood (added 2026-10-03)

**Biome rules.** Mods sometimes give a structure every overworld biome: Mysticrift's 8 pillager towers use
`#minecraft:is_overworld`, so they spawned in the open ocean. `biome_rules` in `structure_groups.json` holds rules of
the form `{biomes, allow}`. Only the structures in the `allow` tag can start in a rule's biomes. Every other structure
keeps its own biome list, minus those biomes.

- **`water`:** biomes `#minecraft:is_ocean`, `#minecraft:is_river`, `#c:river` (Terralith's warm river): 12 in this
  pack. `allow` is `#water_ok` in `structure_tags.json`:
  - in the water: ships, wrecks, underwater ruins, the monument, Hopo's underwater fossils, the ocean ruined portal, and
    Farmer's Structures' sushi stall. The stall is a stilted river building with fish inside, and its own mod spawns it
    in rivers only.
  - not in the water but fine there: sky structures and the AE2 meteorite, which sit above the sea, and everything
    underground (`type/underground`, `type/mineshaft`, `dungeon/underground`), which sits under the seabed. Remove
    those lines from the tag to keep them out of the sea too.
- **Kept out (live, scratch server 2026-10-03), 25 in all:**
  - the 8 Mysticrift towers
  - Terralith's spire (frozen rivers)
  - Supplementaries way signs (its set is registered in code, so it is not in `structure_config.json`)
  - vanilla `ruined_portal` (already removed)
  - vanilla `shipwreck` / `shipwreck_beached`: Revamped Shipwrecks empties their set, so nothing changes
  - Terralith's 12 `zpointer/*` dummies (in no set)

  The game logs the list as `[structure-biome-rules] water: ... kept out: ...`.
- **How:** `structure_overrides.js` resolves the tag and stages `{biomes, allow ids}` to patcherMod
  `StructureBiomeRules`. At ServerAboutToStart, that class resolves the biome tags against the loaded registry and
  covers every registered structure, including ones from mods that no config knows yet.
  `ChunkGeneratorStructureBiomeRulesMixin` narrows the valid-biome predicate that `tryGenerateStructure` hands to
  `Structure#generate`, which is the same check that keeps a structure inside its own biome list.
  - `/place structure` is not affected.
  - `/locate` predictions in 1.19.2 never look at biomes, so a search loads the chunk and finds no start, the same as
    for any other wrong-biome slot.
  - A rule error applies nothing. An older jar without the class logs a warning, and the rules simply don't apply.
  - Switch: CEConfig `[worldgen.systems] structureBiomeRules`.
- **Rates:** the water slots of a group grid stay empty for the kept-out members; the land rates don't change.
  `gen_structure_groups.py` and `structures_by_biome.md` apply the rule, so ocean rows now list only the ships under
  Pillager.

**Improved Pillager Outpost** (`ipo`, data only) overwrote `minecraft:pillager_outpost/base_plate` with a 19x1x19
plate. The plate's only jigsaw leads to `ipo:pillager_outpost/towers`, so every outpost was an IPO tower. Now:
- `kubejs/data/minecraft/structures/pillager_outpost/base_plate.nbt` is the vanilla plate again (from the 1.19.2 jar).
- IPO's plate is copied to `ipo:pillager_outpost/base_plate`.
- `minecraft:pillager_outpost/base_plates` (the start pool) picks the vanilla plate at weight 7 and the IPO plate at
  weight 1, so 1 outpost in 8 is an IPO tower. Retune it in `pool_config.json`, where both entries are listed.
- IPO's cages, tents and logs (`features` pool and templates) use the vanilla jigsaw layout, so vanilla outposts use
  them too. To keep vanilla outposts fully vanilla, copy the 6 vanilla feature templates and the vanilla
  `features` pool into `kubejs/data/minecraft`.
- **IPO v5 bug, fixed:** its pools ask for `tower/damaged_overgrown` and `tower/top/damaged_overgrown(_active)`, but
  the jar ships those templates as `overgrown_damaged`, so those picks placed an empty piece (a plate with no tower,
  or a tower with no top). `kubejs/data/ipo/worldgen/template_pool/pillager_outpost/*.json` are IPO's 5 pools with
  the names corrected. Delete them if IPO fixes this.

**Driftwood** (Upgrade Aquatic) is a placed feature, not a structure. Its 5 placed features are registered in Java,
so `kubejs/data/upgrade_aquatic/worldgen/placed_feature/driftwood_*.json` override them. Each file keeps the feature,
the square spread, the `WORLD_SURFACE_WG` heightmap and the biome filter, at 0.25x the mod's rate (2026-10-03 morning),
and since 2026-10-03 evening each one also has a `block_predicate_filter`: the block under the spot must be water, so
these five only ever place floating driftwood.

| Feature | Upgrade Aquatic | In the water now |
|---|---|---|
| ocean | rarity 1/32 per chunk | 1/128 |
| beach | rarity 1/4 per chunk | 1/16 |
| river | 2 per chunk | 1 in 2 chunks |
| swamp | 4 per chunk | 1 per chunk |
| extra (Atmospheric rainforest basins) | 5 per chunk | 1 or 2 (3:1), mean 1.25 |

Each attempt picks one random column, so a beach attempt that lands on sand no longer places anything: the
water version only fires where that column is water. Driftwood on dry ground (sand, grass, dirt) comes from three
new placed features, `kubejs/data/creativeengineering/worldgen/placed_feature/driftwood_land/{beach,swamp,extra}.json`,
added to the same biomes by `forge/biome_modifier/driftwood_land_*.json`. They run the same `upgrade_aquatic:driftwood`
feature with the opposite filter (`not` water below) at one tenth of the attempts:

| Land driftwood | Attempts |
|---|---|
| beach | 1 in 160 chunks (was 1 in 16, and 1 in 4 from the mod) |
| swamp | 1 in 10 chunks |
| extra | 1 in 8 chunks, 1 or 2 logs (3:1) |

Since 1.0.7 (data, DEPLOYED 2026-10-18, 3.23) the beach, river, swamp and extra versions filter for a bank next to water instead
of water below: Upgrade Aquatic can't float a log outside oceans, so the old filter never placed anything. Rivers and
oceans have no land version, so a river bank only gets the water-adjacent version. Edit the `chance` in a land file (or
delete the file and its biome modifier) to change it. Upgrade Aquatic's own feature decides what actually fits (it
needs sand, or dirt near water, or open water), so the real number is lower than the attempts.

**Banks** (Monobank's vault) are two kinds of village piece: a house, `monobank:village/houses/<biome>_vault` (weight
15), and a straight street with a vault attached, `monobank:village/streets/<biome>_straight_fix_01` (weight 15). With
the tech houses gone the vault was 10 to 12% of house picks and 21 to 29% of straight-street picks, so most banks came
from the streets. Two tags in `pool_tags.json` cut both about 5x; a pool also shrinks when a piece's weight drops, so the
multiplier is not 1/5:

| Tag | Multiplier | Weight | Chance per pick, before to after |
|---|---|---|---|
| `monobank/vault_houses` | 0.18 | 15 to 2.7 | plains 10.2% to 2.0%, desert 11.5% to 2.3%, savanna 10.5% to 2.1%, snowy 11.7% to 2.3%, taiga 10.9% to 2.2% (5.0x to 5.1x) |
| `monobank/vault_streets` | 0.16 | 15 to 2.4 | plains 23.1% to 4.6%, desert 29.4% to 6.3%, savanna 20.8% to 4.0%, snowy 23.8% to 4.8%, taiga 23.1% to 4.6% (4.7x to 5.2x) |

(Computed from `pool_tag_report.md`'s weights as of 2026-10-03; the next game load rewrites the report with the real
figures.) The vault's own `monobank:village/monobank/*` sub-pools (locked 12, unlocked 3, empty 1) are left alone:
scaling them would round their ratios. Set a tag's multiplier to 1 to restore the old rate.

### 3.13 Earth fill: cancel structures that need too much ground (added 2026-10-03)

Some structures carry their own ground: a grass and dirt pad under a ruin, a skirt of dirt under a house. On a shore,
a cliff or a lake that pad fills the water or hangs in the air. This check drops such a structure before it exists.

**Hook.** `mixin/ChunkGeneratorEarthFillMixin` runs where `ChunkGenerator#tryGenerateStructure` stores the finished
`StructureStart` (the local right after `Structure#generate`, slot 15 of the SRG bytecode). If the start fails the check
the mixin swaps in `StructureStart.INVALID_START`, so the pick fails the way a start in a wrong biome does: a slot with
one member stays empty, a shared slot tries the next pick. A start that the frequency roll is about to veto is skipped
first (the roll is deterministic), so nothing is computed for it.

**What is counted.** For each piece of the start (`PoolElementStructurePiece`, including list elements, and
`TemplateStructurePiece`; anything else is ignored):
1. the earth blocks of its template: the block tag `creativeengineering:earth_fill/earth` (`blocktags.js`: `#minecraft:dirt`,
   sand, gravel, clay, dirt path; extra tags go in the `blocks` config list) in the bottom `baseLayers` (3) template layers,
   first palette. Higher layers are ignored on purpose: above the ground everything is air, so a roof garden would count.
   Structures in the structure tag `earth_fill/stone_base` also count the block tag `earth_fill/stone_base` (stone and
   brick plates, since 1.0.7, DEPLOYED 2026-10-18; 3.23).
2. where each stands in the world: piece position + rotation and mirror (the same transform the placement uses).
3. what the generator's own terrain holds there: `ChunkGenerator#getBaseColumn` for the column (read once per
   `sampleGrid` x `sampleGrid` cell, 4 by default (2 until 2026-10-27), and cached per start and, with
   `[worldgen.systems] sharedTerrainCache`, across starts, predictions and retries: perf.md 10.8), then the block at
   that Y: water, air, or solid.
4. **the beard** (since 2026-10-13, 7.3.1): the structure's terrain adaptation fills air and water under its pieces
   with ground before the template is placed (vanilla's `beard_thin` / `beard_box`, the pack's soft beard, `bury`).
   So earth over water or air counts only where the ground under it lies more than the beard's reach below the
   piece's bottom layer: `beardReach` (6) for the beards, `buryReach` (10) for bury. For a sunk start (3.15) the reach
   and the mound test start at the bottom plus its sink, as the beard's ground level is raised by the sink (a camp sunk
   4 is filled only 2 under its real bottom; TODO `structures/earthfill #10`, fixed 2026-10-30; before, the full reach
   was assumed under the real bottom). A structure with adaptation `none` counts every block, as before. Over water counts toward `maxOverWater`, over air toward the size's air limit.
5. **mound columns**: every column of earth whose ground lies more than `maxMoundHeight` (8) below its piece's bottom
   counts once per start: there the beard builds a tall pillar or can't reach the piece (a ravine edge, a pad over a
   cliff, a house over deep water).
6. **the size**: the start's own earth base block count picks its limits, so a fish pond and a fortified town aren't
   held to one number (table below). A meta structure is checked part by part, each by its own size.
7. Counting stops at the first limit crossed (unless `log = ALL`). A position in two pieces counts once.

**Pruning** (since 2026-10-13): for a structure in `creativeengineering:earth_fill/prune` (`worldgen/earth_fill.js`:
`#creativeengineering:type/village`, every village), each jigsaw piece but the first is first judged alone, with the
same beard and mound rules. A piece with more than `pruneMaxHanging` (8) earth blocks over water or air past the beard,
or more than `pruneMaxMound` (4) mound columns, is dropped together with every piece attached below it: a house over
a river goes with its villagers, an animal pen with its animals and tree. The jigsaw tree comes from the pieces'
junctions (vanilla gives each child a first junction at its parent's jigsaw block, and the parent a matching one, the
next block over, with the opposite deltaY; pieces are listed parents first); the parent forgets its junction to the
dropped piece, so no beard bump is left at the street's end. The street leading there stays as a dead end. The first
piece (the town centre) is never dropped. Then the rest is judged as a whole as usual, by its own size, and can still
be cancelled. Its size is the pruned start's, so pruning can move a start down a size class and judge it by the
smaller limits (a large savanna village that lost its pet shop was cancelled as medium on 40 mound columns, Phase 7
3.3); that is accepted (user, 2026-10-19). The `type/*` tags of `structure_tags.json` are real structure tags since
2026-10-19 (`structure_overrides.js` adds them with the `meta/*` tags); before that `earth_fill/prune` was empty in game
and nothing was ever pruned (Phase 7 3.2). The log says `[earth-fill] <id> at chunk [x, z]: pruned K of N pieces; over the piece limits: <template>
(W water, A air, M mound), ...`. Meta structures (village_towers and the fortified metas are in the type tag) are
judged part by part and not pruned: a village part over its limits still cancels the meta, and the slot falls back to
the plain village, which is pruned.

Village streets and anything else with the `terrain_matching` projection are skipped: a gravity processor drops them onto
the ground column by column, so they never hang.

**Config** (`[worldgen.earth_fill]`; on/off `[worldgen.systems] earthFill`; applies to new chunks, read live):

| Key | Default | Meaning |
|---|---|---|
| `maxOverWater` | 24 | earth blocks in water the beard won't fill; -1 = no limit, 0 = none |
| `maxWaterSurplus` | 0 (0.5 before 2026-10-30) | the footprint water share (below): cancel when the footprint's water outnumbers its dry ground by more than this (0.5 = 50%, 0 = any water majority, the strictest value); -1 = off (added 2026-10-19, 7.5; set to 0 2026-10-30, TODO `structures/earthfill #2`) |
| `beardReach` | 6 | blocks under a piece the beard still fills (beard_thin, beard_box, soft) |
| `buryReach` | 10 | the same for bury |
| `maxMoundHeight` | 8 | ground deeper than this under a piece makes a mound column |
| `prune` | true | drop offending pieces of structures in `earth_fill/prune` before judging the whole start |
| `pruneMaxHanging` | 8 | earth blocks of one piece over water or air past the beard before it is dropped; -1 = never |
| `pruneMaxMound` | 4 | mound columns of one piece before it is dropped; -1 = never |
| `baseLayers` | 3 | template layers from the bottom that count as foundation |
| `blocks` | `["creativeengineering:earth_fill/earth"]` | block tags that count as earth |
| `overrides` | `[]` | per-structure limits: `"<id or #structure tag>=<water>,<air>[,<mound>]"`, first match wins, replaces the size's limits (mound left out = the size's), e.g. `"philipsruins:ancient_ruins=0,16"` or `"#creativeengineering:type/village=-1,-1,-1"` |
| `sampleGrid` | 4 | terrain read once per N x N cells (1 = every column); 2 until 2026-10-27 (perf.md 10.8) |
| `log` | `CANCELLED` | `CANCELLED` logs each cancelled start, `ALL` every start that has earth in its base (and counts it fully), `OFF` |

`[worldgen.earth_fill.sizes]`: `bounds = [150, 600, 2000]` (earth base blocks where medium, large and huge start), and
`[maxOverAir, maxMound]` per size (first guesses, to calibrate with ST-52):

| Size | Earth base blocks | maxOverAir (after beard) | maxMound columns |
|---|---|---|---|
| `small` | under 150 | 24 | 8 |
| `medium` | 150 to 599 | 64 | 24 |
| `large` | 600 to 1999 | 160 | 64 |
| `huge` | 2000 and more | 320 | 128 |

The old single `maxOverAir` (128 in the 1.0.6 jar default, 256 in 1.0.4 and the instance toml) is gone; Forge drops
it from the toml on the next start.

Log line: `[earth-fill] <id> at chunk [x, z] (<size>, <beard>): N earth, W over water (limit 24), A over air after
beard (limit L), M mound columns (limit K) -> kept|CANCELLED`.

**Exempt structures:** the structure tag `creativeengineering:earth_fill/exempt` (`worldgen/earth_fill.js`), which lists
what floats or stands in the water on purpose: Cloud Storage, Sky Villages, the AE2 meteorite; every ship and raft;
ocean ruins and fortresses, wrecks, the monument, Hopo's fossils, the sushi stall, buried treasure; and since
2026-10-13 the woodland mansion, which pours cobblestone under its footprint down to the ground itself. Add an id to
exempt it, or use an `overrides` line to give it its own numbers.

**Where the defaults come from.** `tools/earth_fill_sim.sh` generates real vanilla structures on vanilla overworld
terrain (the pack's own `EarthFill` counting, builtin registries, the real template and terrain code, 60 sites per
structure inside the structure's biomes, seed 4242).

*Beard reach* (`tools/earth_fill_sim.sh 4242 25 beard`, 125 village starts, 2026-10-13): every village start is bearded
with vanilla's own Beardifier three ways, and for each earth block over noise air the whole gap down to the terrain is
checked (noise density + beard > 0). Share filled by the gap under the piece's bottom:

| Gap (blocks) | 0-2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12+ |
|---|---|---|---|---|---|---|---|---|---|---|---|
| beard_thin | 100% | 98% | 92% | 90% | 81% | 35% | 26% | 6% | 13% | 0% | 0% |
| beard_box | 99-100% | 94% | 84% | 80% | 70% | 17% | 24% | 0% | 13% | 0% | 0% |
| bury | 100% | 100% | 100% | 100% | 100% | 100% | 96% | 91% | 100% | 27% | 0-20% |

Hence `beardReach` 6 and `buryReach` 10. The soft beard has beard_thin's vertical profile under its footprint, so it
shares `beardReach` (its `skirtBump` only widens the blend sideways).

*Counts* (`tools/earth_fill_sim.sh 4242 60`, 2026-10-13, the 1.0.6 code; "before" = every block over water or air):

| Structure | Earth (mean) | Over air before: p50 / p90 / p99 | After the beard: water p50 / p90 / p99 | air p50 / p90 / p99 | mound p50 / p90 / p99 | Sizes s/m/l/h | Cancelled (water / air / mound) |
|---|---|---|---|---|---|---|---|
| village_plains | 239 | 58 / 120 / 200 | 0 / 74 / 126 | 0 / 18 / 98 | 0 / 61 / 127 | 11/49/0/0 | 22% (18 / 2 / 18) |
| village_desert | 314 | 64 / 129 / 169 | 0 / 190 / 363 | 0 / 32 / 40 | 0 / 152 / 346 | 4/54/2/0 | 38% (35 / 0 / 30) |
| village_savanna | 594 | 148 / 260 / 426 | 0 / 124 / 320 | 0 / 41 / 99 | 1 / 93 / 355 | 1/28/31/0 | 40% (37 / 5 / 25) |
| village_snowy | 298 | 70 / 147 / 219 | 0 / 78 / 243 | 0 / 17 / 50 | 0 / 50 / 213 | 8/51/1/0 | 23% (20 / 0 / 18) |
| village_taiga | 328 | 74 / 200 / 314 | 0 / 109 / 417 | 0 / 34 / 103 | 0 / 66 / 462 | 5/52/3/0 | 23% (22 / 2 / 22) |
| pillager_outpost, igloo | 0 | none | none | none | none | all small | 0% |

After the beard, air almost never trips (0 to 5%). What still cancels a vanilla village is water: houses whose floor
sits in the top water layer of a river or the coast with more than 6 blocks of water under them, a pad over water the
beard can't fill (the same columns are mound columns). The 2026-10-03 run reported 0 over water for every village:
that was a fault of the simulation (the fluid tags weren't bound, and the check used `FluidTags.WATER` then), so the
1.0.4 check, which counted every water block at 24, cancelled about 45% of the villages that reach the water (the
same simulation, old rule).

*With pruning* (same run, the villages): the jigsaw tree linked every village piece to its parent (0 unlinked).

| Village | Starts pruned | Pieces dropped per pruned start | Whole start still cancelled (was) |
|---|---|---|---|
| plains | 30% | 3.7 | 7% (22%) |
| desert | 42% | 9.4 | 10% (38%) |
| savanna | 48% | 4.3 | 7% (40%) |
| snowy | 30% | 4.7 | 5% (23%) |
| taiga | 28% | 6.1 | 12% (23%) |

Dropped pieces seen in plains villages: a medium house (19 air past the beard) with its nitwit and unemployed
villager, an animal pen over 74 blocks of deep water, a pen with its sheep and tree, two extra meeting points over a
drop. The remaining cancels are starts whose town centre itself, or the sum of its marginal pieces, is over the limits.


**The footprint water share** (added 2026-10-19, 7.5). The beard fills water up to `beardReach` deep, so a camp or a
ruin set on a shallow lake or a lake island never counts toward `maxOverWater`: the lake is filled under it, and the
structure ends up on a little island. So after the limits, each earth piece's bounding box is read once more, every column
of it at the layer the start was set onto the surface with (the piece's bottom; for a sunk start, 3.15, its sink higher).
Water there (filled by the template's earth or the beard, or left standing where the template has no block) against dry
ground there (replaced or left alone); seabed and air count as neither, and so does a spot under more than `baseLayers`
blocks of predicted terrain (underground structures sit in aquifers or under the sea). When the water outnumbers the
ground by more than `maxWaterSurplus` (0.5: 60 water to 40 ground passes, 61 to 39 doesn't; the default since
2026-10-30 is 0: 50 to 50 passes, 51 to 49 doesn't), the start is cancelled.
Structures in `water_fill/structures` are not checked, and an override with every limit -1 skips it too. Under
`log = ALL` such a structure is still counted and its footprint logged, but never cancelled, neither by the share nor
by the size's mound limit when the override leaves the mound out (TODO `structures/earthfill #9`, fixed 2026-10-30,
`EarthFill.earthCancelled`; before, `ALL` could cancel it and so changed what a calibration run measured). A meta is
checked part by part, as for the limits. The log line gains `footprint <water> water / <ground> ground (surplus limit
0.5[, too wet])`. Inland scratch run (2,601 chunks, 972 starts checked): 14 cancelled by the share alone, among them
a sunflower-plains village on a lake (739 water / 0 ground), a windmill (255 / 0) and a drum tower (2,123 / 66) whose
spot is open water; kept starts with any footprint water were 5 of 926, all villages at a shore (113 water / 462 ground up to 470 / 425).

**Limits.**
- It judges the generator's noise terrain: no carvers, surface rules, features, lakes, or other structures' terrain
  adaptation. The beard is estimated by its reach, not computed per block.
- Only the template's own earth blocks are counted, not the terrain a structure's processors add.
- `/place structure`, `/cdevplace` and `/locate` don't go through `tryGenerateStructure`, so none of them is checked
  (a `/locate` can point at a slot the check later cancels: it loads the chunk and finds no start, as for a wrong biome).
- A cancelled slot stays empty, so structure rates drop a little on coasts and cliffs (the group grid is unchanged).
- Terrain-matching pieces and non-template pieces (legacy temples, mineshafts) count as no earth.
- Structures whose class answers its terrain adaptation itself (YUNG's) are judged by what it answers.
- For a structure in `creativeengineering:template_trim` (3.15) only the earth that is still placed counts: pad in a
  column with no built block above it is never placed, so it can't hang anywhere (added 2026-10-09).

### 3.14 Water fill: cancel water structures that would flood the land (added 2026-10-08)

The same idea as earth fill (3.13), turned round. Some structures carry their own water: Hopo's underwater ruins are
full of it up to 20 blocks high, the T&T ocean outpost stands on a 10,000-block water plate, the swamp fisherman house
sits on a pond. On a shore or on land that water replaces dry ground or stands in air and spills. This check drops such
a structure before it exists.

**Which structures.** Only the structure tag `creativeengineering:water_fill/structures`
([worldgen/water_fill.js](../kubejs/server_scripts/worldgen/water_fill.js)). It is opt-in, unlike earth fill: plenty of
land and underground structures carry water on purpose (Better Jungle Temples' water rooms have 600+ blocks, Better
Strongholds and the ancient city have fountains, village farms have water rows), and dug into the ground is exactly where
that water belongs. The list was made from a scan of every template in `mods/*.jar` (2026-10-08, 401 templates carry
water):

| In the tag | Water per template | Placed |
|---|---|---|
| `hopo:underwater_ruins`, `underwater_small_ruins`, `underwater_warm_small_ruins`, `underwater_fossils` | 30 to 7,700, from template y 1 up | on the ocean floor (`OCEAN_FLOOR_WG`) |
| `towns_and_towers:pillager_outpost_ocean` | 10,000 (plate, template y 0-5) | absolute y 58, so y 57-62: flush with the sea |
| `towns_and_towers:village_ocean` | 500 per escort ship (hull, y 0-1) | absolute y 59 |
| `farmers_structures:sushis` | 57 (waterlogged stilts) | rivers, absolute y 48 |
| ponds: `farmers_structures:aquacultures_beach`, `_carp`, `_common`, `old_swamp_fisherman_house:fisherman_house` | 170 to 270 | sunk into the ground (beach, plains, swamp) |

Left out on purpose: Terralith's spire (a land structure with a lake inside), the undergarden structures (another
dimension), the vanilla ocean ruins and shipwrecks and the other ships (their templates carry no water; they would count
nothing).

**What is counted.** Every water block of each template piece: anything whose fluid is water (water, waterlogged
blocks, kelp, seagrass, bubble columns), in **all** layers (water high up spills just as well), first palette, placed with
the piece's position and rotation. Each is compared with the same predicted terrain as earth fill (same `sampleGrid`,
same column cache, the same start), which now tells four kinds apart:
- water: fine.
- seabed: a solid block whose column is topped by water (a sea, lake or river bed). Fine: a ruin sunk a few blocks into
  the ocean floor is how it is built.
- dry ground: a solid block under a land column. Counts toward `maxIntoGround`.
- air: above the sea surface, or above the ground on land. Counts toward `maxIntoAir`.

Earth fill treats seabed as solid, so its results are unchanged. The two checks share one hook: earth first, then water,
and either can cancel.

**Config** (`[worldgen.water_fill]`; on/off `[worldgen.systems] waterFill`; applies to new chunks, read live):

| Key | Default | Meaning |
|---|---|---|
| `maxIntoGround` | 32 | water blocks allowed to replace dry ground; -1 = no limit, 0 = none |
| `maxIntoAir` | 16 | water blocks allowed in air; -1 = no limit, 0 = none |
| `overrides` | `["#creativeengineering:water_fill/ponds=-1,16"]` | per-structure limits: `"<id or #structure tag>=<ground>,<air>"`, first match wins |
| `log` | `CANCELLED` | `CANCELLED`, `ALL` (every checked start that carries water, counted fully), `OFF` |

The ponds tag (`creativeengineering:water_fill/ponds`, same file) holds the structures that dig their water into the
ground every time; the default override lifts their ground limit, so only a pond spilling into air cancels them. A log
line reads `[water-fill] <id> at chunk [x, z]: N water blocks, G into dry ground (limit 32), A into air (limit 16) -> kept|CANCELLED`.

**Where the defaults come from.** A guess, not a measurement: the vanilla simulation of 3.13 can't run modded
structures. The reasoning: a water structure in its own biome lands in water or seabed almost everywhere, so its counts
should be near 0, and a few dozen blocks into dry ground or air is already a visible spill. Calibrate with `log = ALL`
(ST-57).

**Limits.** The same as earth fill (noise terrain only, no `/place`, `/cdevplace` or `/locate`, an empty slot on a
cancel), plus:
- The seabed test reads the column's top non-air block, so a lake or river bed counts as seabed too, and a column whose
  surface the noise puts in an aquifer lake is "under water" even if a surface rule later makes it dry.
- At `sampleGrid` 4 a shoreline is resolved to 4 blocks; a ruin touching a beach may count a few blocks either way.
- Water a processor adds or removes at placement is not seen; only the template's own blocks count.

### 3.15 Terrain adaptation by tag, the soft beard and template trim (added 2026-10-09)

**The problem.** Some small and medium structures sit in a flat square of land with straight edges, cut into
otherwise smooth hills. The Illager Siege Weapons ballista is the clearest case. Three things cause it:

| Cause | What it does | Example |
|---|---|---|
| Vanilla's beard (`terrain_adaptation: beard_thin`) | Fills under and carves above the piece's whole **bounding box** at floor level, then fades out over about 8 blocks around that box | The ballista is a 7 x 13 box with no pad at all; 35 of its 91 columns are only air, yet the whole 7 x 13 plus 8 blocks around it is flattened |
| Pads saved in the template | A grass/dirt slab several blocks wider than the building, placed at one height | Circus tent: 264 pad-only columns; fletcher house 254; big horse stable 279 |
| Air saved in the template | Every air block in the template box replaces the terrain, cutting a rectangular notch into any slope | Circus tent 17,903 air blocks (6,072 of them outside the built columns), ballista 454 |

Nearly all MCreator structure mods (the 59 terrain-adapted ones ported to `kubejs/data`, 3.11) have all three: they
were built in a flat test world and saved as a whole box. Vanilla's beard skirt itself is round (straight-line
distance to the box); the square comes from the box being the whole template.

**Tags** ([worldgen/terrain_adaptation.js](../kubejs/server_scripts/worldgen/terrain_adaptation.js)):
- `creativeengineering:terrain_adaptation/<mode>` replaces a structure's own `terrain_adaptation`: `none`, `bury`,
  `beard_thin`, `beard_box` (vanilla's), or `soft` (below). Works for any structure, jar or `kubejs/data`, without
  touching its json. A structure in two mode tags takes the first of none, bury, beard_box, beard_thin, soft, and the
  load log says so.
- `creativeengineering:template_trim`: the template's air, pad and plants outside its built columns are not placed.

Tagged now (97 structures; batch 4 added 5 more on 2026-10-30, 3.30):
- `none` + `template_trim`: the 6 siege weapons (ballista, battering ram, reinforced ram, catapult, siege tower,
  trebuchet): small open frames on a few slabs, nothing to flatten for.
- `soft` + `template_trim`: the other 53 ported terrain-adapted structures (houses, stables, tents, churches, temples,
  towers, the forest ruins, statues, both watchtowers); scan batch 1, 12 houses on pads; scan batch 2, the vanilla and
  23 Towns & Towers pillager outposts (added 2026-10-09, below).
- `soft` alone: scan batch 3, Takes a Pillage's bastille and camp (walled yards stay flat).
- The ships, rafts and Sky Villages (`none` already) are untouched.

**The soft beard.** The same vertical profile as beard_thin (the same fill below the floor and carve above it, so the
structure still stands on ground on a slope), with two changes:
1. **Footprint, not box.** It covers the columns of the template that place a block: with `template_trim`, only the
   built ones (anything that isn't air, pad or a plant). The ballista's beard covers its 56 built columns, not the
   91-column box; a house's covers the house, not its yard.
2. **Short skirt.** The fade-out distance is 4 blocks plus `skirtBump` (vanilla's is 8), measured as straight-line
   distance to the nearest footprint column, so it follows the building's outline. Technically: vanilla's beard
   formula with the horizontal distance multiplied by `8 / skirt`. At `skirtBump = 4` (skirt 8) on a rectangular
   footprint it is exactly vanilla's beard_thin (test: worst difference 9e-6), so the bump runs from "tightest" (0) to
   "vanilla width, but around the building" (4) and up to 12 blocks (8), vanilla's hard limit.

Pieces that aren't templates (or whose template can't be read) use their bounding box as the footprint, still with
the short skirt.

**Template trim.** Each template placement of a trimmed structure is split into columns. A column with a built block
is placed whole (pad under the building, the air inside it, everything). Every other column drops its air, its pad
(block tag `creativeengineering:template_trim/pad`: `#minecraft:dirt`, sand, gravel, clay, snow block) and the plants on
it (`creativeengineering:template_trim/plants`: the ground-match plants, snow layers, moss carpet). `dirt_path` and
farmland are not pad: paths and crop plots count as built, as in ground matching. Structure blocks and jigsaws are
never dropped. A template with no built column at all is left alone: it is a foundation plate that another piece
stands on (the watchtowers' start piece). Trim runs before ground matching on the same hook, so ground matching only
sees what is still placed, and earth fill (3.13) only counts earth that is still placed.

Measured on the real templates (the test reads the ported `.nbt` files; an independent python read agrees):

| Template | Columns with blocks / built | Trim drops |
|---|---|---|
| ballista | 56 / 56 (no pad) | 210 air in 35 columns |
| circus tent | 841 / 577 | 6,072 air + 264 pad |
| forest ruin 3 | 238 / 170 | 519 air + 69 pad + 24 plants |

**Hooks** (patcherMod):
- `StructureTerrainAdaptationMixin`: `Structure#terrainAdaptation` (SRG `m_226620_`) answers from the tag table, so
  every vanilla use follows the tag (the Beardifier, and the 12-block reference margin of terrain-adapted starts). A
  soft structure answers `beard_thin`.
- `BeardifierSoftMixin`: in `Beardifier#forStructuresInChunk`'s per-start lambda (`m_223930_`) each `new Rigid` goes
  through `TerrainAdaptation.rigid`; a soft structure's piece gets adjustment NONE (vanilla adds nothing) and a
  `SoftBox` (a `BoundingBox` subclass carrying the footprint) as its box. In `Beardifier#compute` (`m_207386_`) the
  density (local slot 5) gets every `SoftBox`'s beard added right after vanilla's rigid loop. Keeping the soft pieces
  in vanilla's own list matters: YUNG's API replaces the Beardifier with a new one built from that list, so state kept
  on the Beardifier object would be lost.
- `StructureTemplateGroundMatchMixin` (processBlockInfos): `TemplateTrim.apply`, then ground matching.
- The tag table is built on world load (`ServerAboutToStartEvent`) and after every server tag load (`/reload`). Load
  log: `[terrain-adaptation] world load: 97 structures tagged, modes {NONE=6, SOFT=91}, 95 trimmed`.

**Config** (`[worldgen.terrain_adaptation]`; on/off `[worldgen.systems] terrainAdaptation` (mode tags, soft beard) and
`templateTrim`; new chunks only):

| Key | Default | Meaning |
|---|---|---|
| `skirtBump` | 0 | blocks added to the soft beard's minimum skirt of 4; 0 to 8. 4 = vanilla's width |

**Sunk starts** (added 2026-10-19, 7.5). Vanilla's beard (and the soft one) levels the ground to a piece's bottom
layer: right for a jigsaw start set onto the heightmap with start height 0, whose bottom layer replaces the surface
block. A start height below 0 sinks the template that many layers into the ground on purpose, and the beard then digs
the ground down to the sunk bottom around it. Campsite Structures' camps (7.4.1) stand on a 4-layer dirt dome under
their grass layer: with `beard_thin` the noise surface was dug down 4 to 5 blocks around each camp and the dome stood
out of a flat ring as a plinth (scratch 2026-10-19: camp grass at y 94, the ring at 90). The structure tag
`creativeengineering:terrain_adaptation/sunk_start` (`terrain_adaptation.js`) raises every piece's beard by the start's
sink (minus a constant start height below 0, for a start projected to a heightmap; patcherMod
`TerrainAdaptation#sink`, read from `JigsawStructure` by reflection), so the ground is levelled at the surface the start
was projected to. A tagged structure without such a start height is logged and ignored. Tagged: the 6 camps, whose
start height went from -5 (the old features' offset, a 1-block dip) to -4 (the grass layer replaces the surface
block). Opt-in, because other structures sink starts with their own beards on purpose and were not looked at in game:
Philip's ancient crypt, antiquus crypta and ocean fortress (-5, beard_box), ocean ruins (-3), ancient ruins, rare ruin
and end gate fortress (-1), T&T's nilotic village (-1). The load log line counts them (`6 sunk starts`).

**Limits.**
- New chunks only, rejoin after changing tags or switches. Already generated structures keep their terrain.
- YUNG's structures (`YungJigsawStructure`) answer `terrainAdaptation` themselves and have their own enhanced
  adaptation, so the mode tags do nothing for them (logged as "answers terrainAdaptation itself"). Template trim
  still works for them.
- `none` means the structure sits where its start lands (`project_start_to_heightmap` at its centre): on a steep
  slope a siege weapon's low end can float a block or two. Move it to `soft` if that bothers you.
- Trim is per template. In a multi-piece structure, a later piece's air no longer clears an earlier piece's blocks
  outside its own built columns (designers rarely rely on that).
- The soft footprint is read from the raw template (first palette); blocks that processors add or remove are not
  seen. Jigsaw junction beards (the small blend where two jigsaw pieces join) stay vanilla's.
- The ground inside the yard of a trimmed structure is natural, so a fence or a lamp post in the yard stands on
  whatever slope is there (its own column is built, so the soft beard levels a little around it).
- Only the ported MCreator structures are tagged. The scan below lists every other structure that could use the tags.

**Which other structures it could help (scan, 2026-10-09).** `python3 tools/terrain_adaptation_scan.py` (also
`/cdevgen run terrain_adaptation_scan`, about a minute fresh, seconds cached) follows every structure's start pool and
jigsaw links to the templates it can place and measures each rigid one the way `TemplateTrim` does (pad and plants
from the live tag export). Report: [terrain_adaptation_scan.md](terrain_adaptation_scan.md); data:
`kubejs/generation_scripts/worldgen/terrain_adaptation_scan.json`. Of 383 structures, before batches 1 to 3 were tagged:

| Verdict | Count | Who |
|---|---|---|
| Strong (30%+ of the box unbuilt, 12+ pad or air-only columns) | 29 | 16 Towns & Towers pillager outposts, the vanilla pillager outpost, Takes a Pillage's bastille and camp, Farmer's Structures cook houses and two fish ponds, the hobbit house, the sawmill, Simply Houses' forest fort, Terralith's glacial hut and spire, Beautify's desert botanist house |
| Mild (15%+, 6+ columns) | 17 | 7 more T&T outposts, 4 Beautify botanist houses, the Greek house, the swamp fisherman house, Philip's level-three and nether-themed ruins (801 and 512 pad columns), Philip's desert structures (bury), Simply Houses' taiga forge |
| Settlements (review) | 31 | the 5 vanilla villages, 24 T&T villages, Terralith's 2 fortified villages |
| Compact | 16 | most Simply Houses, the Better Witch Huts, 5 T&T outposts: built edge to edge, nothing to gain |
| Ships, rafts, sky | 23 | the earth_fill/exempt list: not suggested (trim at sea leaves sea water where the template had air) |
| Not surface | 194 | underground (141 of them Terralith's), buried, seabed, Nether, End, mostly-water templates |
| Terrain-matching only | 6 | Philip's field-stone, level-one, pumpkin, rare and badlands ruins: placed column by column on the ground, no beard |
| Code-built / not found | 8 | shipwrecks, Cloud Storage, the spider dungeon; 2 Philip's ruins and the bloodstone obelisk whose templates the pools name but no jar has |

Batches 1 to 3 were tagged on 2026-10-09 (the user's choice); batch 4 on 2026-10-30 (3.30: the ponds, the
fisherman house, the level-three ruins, the spire with the new air-only trim; villages and the Nether ruin left out). After tagging, the scan shows 97
tagged, 3 strong and 5 mild left (all batch 4 or too small to matter). The batches:
1. **Houses on pads**, the same case as the ported ones, `soft` + `template_trim`: hobbit house, sawmill, Simply Houses
   forest fort, Farmer's Structures cooks and cooks_moss, Terralith glacial hut, the 5 Beautify botanist houses, the
   Greek house.
2. **Pillager outposts**, `soft` + `template_trim`: the 23 strong and mild T&T outposts, and the vanilla outpost. For
   the vanilla one the gain is smaller than its numbers: 768 of its 2,020 air-only columns are the `base_plate`
   (16 x 16) and `feature_plate` (32 x 16), templates of pure air that trim leaves alone (nothing built) and the soft
   beard covers by their box; the rest is the overhang of the 8 Improved Pillager Outpost tower tops.
3. **Walled compounds**, `soft` alone (yards stay flat, the skirt gets short and round): Takes a Pillage bastille
   (9,423 pad columns: courtyards) and camp. Terralith's fortified villages belong here if the villages are done.
4. **Look first:** Terralith's spire (`none`, 5,555 air-only columns: trim would stop it carving the hills around
   it), the fish ponds and fisherman house (in `water_fill/ponds`; trim keeps the water columns), Philip's level-three
   and nether-themed ruins (grass pads that ground matching already reskins), and the villages (tagging one changes
   every house in it).

### 3.16 Meta structures: villages with towers, ruin clusters, pillager camps, fleets, flotillas (added 2026-10-11)

A meta structure is one structure of ours built from other structures and placed features: an **anchor** (a village,
a ruin, a pillager tower, a ship) generated at the start chunk exactly as it would be on its own, plus **parts** placed
around it (towers, small ruins, siege weapons, wrecks, driftwood logs). It is listed in a group like any other member,
but a meta with an anchor never takes a slot of its own: it **takes a share of the slots its anchors already won**.

**Rates stay put (slot conversion, patcherMod `MetaSlots`).** The group fills a slot as before (weighted pick, biome
fall-through, frequency roll). When the pick lands on an anchor of a meta and the anchor keeps its frequency roll, the
slot becomes that meta with chance `share` (a hash roll of seed, chunk and anchor), built on that very anchor. If the meta
can't be assembled (too few parts fit), the slot is the plain anchor. The meta's own entry in the group (weight 1) is
never picked directly. So no structure's rate changes, and no group gains or loses slots: ruins vs villages vs pillagers
vs ships stay exactly where `structure_groups.json` puts them; a meta only adds parts around an anchor that was going to
spawn anyway. Several metas may share an anchor (a pillager camp anchors three); their shares add up, cut at 100% in id
order. A meta with no anchor (the driftwood flotilla) is an ordinary member of its own group.

Until 2026-10-11 (evening) an anchored meta was a weighted member (village_towers weight 100). That kept rates only where
every member of the group is an anchor (true for villages, not for ruins, pillagers or ships), so it was replaced; the
village_towers share 0.33 matches its old "about a third where towers fit".

| Meta structure | Group | Anchor (share of its slots) | Parts | Est. 1 per (chunks) |
|---|---|---|---|---|
| `meta/village_towers` | `settlements` | any settlement village (`meta/villages`, 33%) | 1 to 3 peaceful towers 40 to 104 blocks out, 48 apart, margin 6 | 113 |
| `meta/driftwood_flotilla` | `flotsam` (own grid, 32/8) | none (weight 100) | 25%: a villager raft within 14; 8 to 16 driftwood logs within 26, 4 apart, over water 2+ deep (closer since 2026-10-12) | 32 |
| `meta/ruin_cluster` | `ruins` | a large ruin (`meta/large_ruins`, 50%) | 2 to 5 small ruins 18 to 56 out, 18 apart | 47 |
| `meta/ruin_scatter` | `ruins` | a small ruin (`meta/small_ruins`, 30%) | 1 to 3 more small ruins 14 to 44 out (2 to 4 in all) | 55 |
| `meta/sunken_ruin_cluster` | `ocean_ruins` | a large underwater ruin (50%) | 2 to 3 small underwater ruins 18 to 56 out | 38 |
| `meta/shipwreck_debris` | `ocean_ruins` | a sunken shipwreck (30%) | 1 to 3 floating wreckage 6 to 48 out, 22 apart (shallow oceans only); up to 6 driftwood logs 4 to 30 out (2026-10-12) | 32 |
| `meta/shipwreck_graveyard` | `ocean_ruins` | a sunken or beached shipwreck (12%) | 2 to 7 more shipwrecks 20 to 72 out, 24 apart (3 to 8 in all) | 46 |
| `meta/raft_convoy` | `boats` | a villager raft (30%) | 1 to 4 more rafts 10 to 44 out (2 to 5 in all, 2026-10-12) | 146 |
| `meta/pillager_fleet` | `boats` | a pillager ship (35%) | 1 to 2 more pillager ships 36 to 96 out, 40 apart; 2 to 5 floating wreckage / shipwrecks 16 to 100 out | 143 |
| `meta/pillager_tower_siege` | `pillager_settlements` | a standard pillager tower (35%) | 1 to 2 siege weapons or camps 24 to 64 out | 133 |
| `meta/pillager_great_tower_siege` | `pillager_settlements` | a Mysticrift tower (50%) | 2 to 4 siege weapons or camps 22 to 72 out | 432 |
| `meta/pillager_stronghold_siege` | `pillager_settlements` | a T&T fort or the mushroom tower (40%) | 2 to 3 siege weapons or camps 30 to 88 out | 141 |
| `meta/pillager_camp_siege` | `pillager_settlements` | a pillager camp (20%) | 1 to 2 more camps and 1 to 2 siege weapons | border only (see below) |
| `meta/pillager_camp_ruin` | `pillager_settlements` | a pillager camp (15%) | a large ruin; 0 to 1 more camp | border only |
| `meta/pillager_camp_ruins` | `pillager_settlements` | a pillager camp (15%) | 2 to 4 small ruins; 0 to 1 more camp | 100 |
| `meta/fortified_ruins` | `settlements` | a fortified town or fort (`meta/fortified_towns`, 25% since 2026-10-30, 20% before; 3.27) | 2 to 6 small ruins 44 to 108 out, 18 apart, margin 6 | not re-estimated (7.4) |
| `meta/fortified_camps` | `settlements` | a fortified town or fort (`meta/fortified_towns`, 25% since 2026-10-30, 20% before; 3.27) | 2 to 4 friendly camps 44 to 108 out, 24 apart, margin 6 | not re-estimated (7.4) |
| `meta/town_villages` | `settlements` | a fortified town or fort (40% since 2026-10-14) | none: 1 to 3 companion villages 64 to 96 out (3.22; 112 to 160 before 2026-10-19) | not re-estimated (7.4) |
| `meta/farmers_camp` | `remote` | a Farmer's Structure (30%) | 0 to 2 more Farmer's Structures and 1 to 2 friendly camps, 20 to 64 out | 159 |
| `meta/house_camp` | `remote` | a lone house (15%) | 1 friendly camp 16 to 48 out | 93 |
| `meta/waysign_camps` | `remote` | a friendly camp (25%) | 0 to 2 more friendly camps 16 to 56 out; 1 way sign 10 to 40 out | 146 |
| `meta/waysign_house_camp` | `remote` | a lone house (10%) | 1 friendly camp 16 to 48 out; 1 way sign 10 to 40 out | 113 |
| `meta/meteor_shower` | `sky` | an AE2 meteorite in a sand desert (`land/desert` biome tag as the meta's biomes, 50%; 3.31) | 1 to 3 more meteorites (overlap) 20 to 44 out, 20 apart | about 240 in deserts (the tool shows none: the fit biome tag is filled at load) |
| `meta/witch_coven` | `witch_huts` | YUNG's witch circle (40%; 3.31) | a witch hut 24 to 64 out; 1 to 2 witch wagons 20 to 64 out | 45 |
| `meta/village_siege_ring` | `settlements` | any village (4%; 3.31) | 2 to 6 siege weapons 48 to 104 out, 32 apart, all round | 291 |
| `meta/village_siege_camp` | `settlements` | any village (4%; 3.31) | 2 to 6 siege weapons, the first 56 to 104 out, the rest within 40 of it (`cluster`) | 291 |
| `meta/struck_monument` | `ocean_dungeons` | the ocean monument (25%; 3.31) | an AE2 ocean meteorite (overlap) 0 to 20 out | 202 |
| `meta/deadwood_<wood>` (acacia, birch, cherry, jungle, oak, spruce; 3.25) | `deadwood` (own grid, 16/5) | none (weight 100 each) | 3 to 6 Geophilic stumps within 14, 3 apart; 2 to 4 fallen logs 2 to 16 out, 6 apart | 16 (oak, birch: 20, they share forests) |
| `meta/cloud_cluster` | `sky` | a storage cloud (Cloud Storage sky temple, 30%; 3.31) | 1 to 3 more clouds 36 to 104 out, 40 apart, margin 8 | 222 |
| `meta/balloon_fleet` | `sky` | a hot air balloon (30%; 3.31) | 1 to 5 more balloons 20 to 96 out, 24 apart | 222 |
| `meta/cloud_balloons` | `sky` | a storage cloud (20%; 3.31) | 0 to 1 more cloud 36 to 96 out; 1 to 2 balloons 24 to 80 out | 272 |
| `meta/sky_village_islands` | `sky` | a sky village (50%; 3.31) | 2 to 5 floating islands (`creativeengineering:sky_island`) 24 to 112 out, 20 apart | about 256 (the tool shows none: it can't rate a part without a structure set) |
| `meta/witch_cart_caravan` | `vehicles` | a witch wagon (30%; 3.31) | 1 to 2 more wagons 16 to 44 out; a witch hut 36 to 80 out where one fits (swamps) | 152 |
| `meta/pillager_caravan` | `vehicles` | a loot carriage (35%; 3.31) | 1 to 3 more carriages 14 to 48 out; 50%: a siege weapon or pillager camp 30 to 72 out | 273 |
| `meta/trader_caravan` | `remote` | a trader camp (25%; 3.31) | 1 to 2 more trader camps 16 to 48 out; 1 way sign 10 to 40 out | 193 |
| `meta/meteorite_village` | `settlements` | any village (`meta/villages`, 5%; 3.31) | an AE2 meteorite (overlap) 0 to 16 from the village's middle | 272 |
| `meta/meteorite_desert_pyramid` | `desert_temples` | none: a replacement taking 8% of YUNG's desert temple slots (3.31) | the vanilla desert pyramid at the start chunk; an AE2 meteorite (overlap) 0 to 12 out | 212 |
| `meta/desert_temple_ruins` | `desert_temples` | YUNG's desert temple (35%; 3.31) | 2 to 4 small ruins 40 to 100 out (desert ones by their biome lists) | 101 |
| `meta/jungle_temple_ruins` | `surface_dungeons` | YUNG's jungle temple (35%; 3.31) | 2 to 4 small ruins 36 to 96 out (jungle-fitted ones) | 31 |
| `meta/underwater_meteorite` | `ocean_ruins` | a large underwater ruin (10%; 3.31) | 50%: 1 to 2 small underwater ruins; an AE2 ocean meteorite (overlap) 0 to 20 out | 55 |
| `meta/pillager_camp_meteorite` | `pillager_settlements` | a pillager camp (10%; 3.31) | an AE2 meteorite (overlap) 0 to 44 out: on the camp or beside it | 183 |

All are under `creativeengineering:`. "Est. 1 per" is `structure_group_estimates.md`: share x the anchors' own rate where
the anchor and every required part can stand in the same biome. It is an upper bound (a meta whose parts don't fit
is the plain anchor) and ignores parts reaching into a neighbouring biome.

**The camp metas are rare.** The anchors are the Towns & Towers camp outposts, which only spawn on beaches, snowy
beaches, sparse jungle and wooded badlands. Siege weapons list only plains, snowy plains, desert, savanna and taiga, and
no large ruin shares a biome with the camps, so `pillager_camp_siege` and `pillager_camp_ruin` only form where a camp's
biome borders theirs (a beach next to plains). Takes A Pillage's palisade camp (plains, savanna, meadow, Terralith
shrublands) is in `meta/pillager_camps` as a part, but it has its own structure set, so it can't be an anchor. Two ways
to widen them: put `takesapillage:pillager_camp` into `group/pillager_settlements` (the groups tool fits it to its
current rate), or give the siege weapons a biome fit (3.17). *2026-10-14 (7.4): both happened (TaP camp folded in 3.20,
siege weapons fitted to open land), and C.C.C.'s `eugene:pillager_camp` is a new anchor in plains, forest, taiga and
swamp, so the camp metas are no longer border-only; the estimates above were not redone (rates frozen until the audit).*

**Which structures take part: the `meta/*` tags** in `structure_tags.json`, each also a real structure tag
`creativeengineering:meta/<name>` (added by `structure_overrides.js` at every tag load). Sizes were read from the
templates (Philip's large ruins 13 to 26 blocks across, small ones 6 to 17):

| Tag | Members |
|---|---|
| `meta/villages` | `#type/village` minus the ocean fleet village, the wandering trader camp and our own metas |
| `meta/peaceful_towers` | desert and forest watchtowers, mini clock tower, fresh watchtower |
| `meta/flotilla_rafts` | the three villager rafts (flotilla middle, raft convoy) |
| `meta/large_ruins` | Philip's level-three ruins, rare ruin and ancient towers; the forgotten church |
| `meta/small_ruins` | Philip's level one and two (both pools), field stone (not the rock piles), pumpkin, ancient, desert and badlands ruins; Forest Ruins 1 to 7; Terralith's glacial hut |
| `meta/underwater_large_ruins` | Hopo's big ruins, Philip's ocean ruins and ocean fortress |
| `meta/underwater_small_ruins` | Hopo's small cold and warm ruins |
| `meta/shipwrecks`, `meta/sunken_shipwrecks` | Revamped Shipwrecks' sunken and beached wreck; the sunken one only |
| `meta/floating_wreckage` | the T&T wreckage (planks at sea level, shallow oceans) |
| `meta/fleet_wreckage` | floating wreckage + the sunken shipwreck |
| `meta/pillager_boats` | T&T pillager ship, Blocky Siege pillager ship, illager longship and warship |
| `meta/pillager_towers` | the vanilla outpost and the 14 T&T tower-form outposts (not the mushroom tower) |
| `meta/pillager_large_towers` | Mysticrift's 8 towers |
| `meta/pillager_strongholds` | the T&T forts (`#type/fort`) and the mushroom tower; the bastille has its own set, so it can't be an anchor |
| `meta/pillager_camps` | the 4 T&T camp outposts, Takes A Pillage's camp, C.C.C.'s pillager camp (7.4.3; not the fake trader camp) |
| `meta/siege_weapons` | the 6 illager siege-weapon camps (in `group/vehicles` since 2026-10-14; still parts here) |
| `meta/pillager_outworks` | siege weapons + pillager camps |
| `meta/fortified_towns` | Terralith's fortified village and fortified desert village, C.C.C.'s red roof castle (7.4.4) |
| `meta/friendly_camps` | the villager tents, snowy tent, archer camp, circus tent, T&T wandering trader camp, C.C.C.'s trader camp, Campsite Structures' 6 camps (7.4) |
| `meta/lone_houses` | every `remote` house except the peaceful towers, Farmer's Structures, temples and churches |
| `meta/farmers_structures` | Farmer's Structures' Overworld cooks, aquacultures and sushi stall |
| `meta/way_signs` | Supplementaries' way sign |

Add a structure to one of these tags and every meta naming the tag can use it; no other file changes. Anchors must be
members of the meta's own group (anything else in the tag is used as a part only). The metas are also in the
type tags (`type/ruin`, `type/underwater_ruin`, `type/shipwreck`, `type/ship`, `pillager`), which puts them in their
groups and lets the water ones start in water (`water_ok`).

**`/locate`** (patcherMod `ChunkGeneratorMetaLocateMixin`, `StructureCheckMetaMixin`):
- `/locate structure creativeengineering:meta/<name>` finds a meta. Its "could it be here" check only asks whether an
  anchor fits that chunk, keeps its frequency and converts to this meta; the chunk load that follows builds the real
  start, so a slot where the parts fell short (a plain anchor) is skipped and the search moves on. The answer is the
  meta's start chunk, where its anchor stands.
- `/locate structure <any structure>` also finds it inside metas: a search for a structure some meta can contain also
  searches those metas, and a hit reports the part's own position (the centre of its pieces), not the meta's start.
  Without this, a tower beside a village, a village with towers, or a ruin in a cluster couldn't be located at all
  (each is stored as part of the meta's start). Explorer maps, dolphins and Async Locator use the same search.

**Definitions** live in [meta_structures.json](../kubejs/generation_scripts/worldgen/meta_structures.json) (its
`_readme` lists every field; `share` is new). `structure_overrides.js` validates each one and adds it to the datapack as
`data/creativeengineering/worldgen/structure/meta/<name>.json`, but only when patcherMod has `MetaStructure` and
`MetaSlots` and `[worldgen.systems] metaStructures` is on. Otherwise every meta is left out of its group set with a
warning (an unknown structure type, or a set naming a missing structure, would fail the world load; a jar without
`MetaSlots` would let an anchored meta take group slots of its own). A definition with an error is left out on its
own. The applier also warns when no anchor of a meta is in its group, and when one anchor's metas share more than 100%.

**How one is built** (patcherMod [MetaStructure](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/MetaStructure.java),
structure type `creativeengineering:meta`):
1. **Anchor:** the structure whose slot this is (`MetaSlots`), generated at the start chunk exactly as alone. Without a
   slot behind it (`/place structure creativeengineering:meta/...`) the anchor is drawn by its weight in the group.
2. **Structure parts:** each copy tries up to 16 spots from the near end of the distance range outwards, at a random
   angle. A spot needs a structure from the tag whose own biome list, its biome rules (the water rule) and the meta's
   accept it, within 7 chunks of the start chunk, `margin` blocks clear (x/z) of every piece already placed, and
   `spacing` from the other copies. (Before 2026-10-11 evening a part didn't check its own biome rules.)
3. **Feature parts:** spots spread evenly over the distance ring, over water at least `water_depth` deep, clear of the
   structures placed before ([MetaFeaturePiece](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/MetaFeaturePiece.java)).
   The flotilla's placed feature is [meta/flotilla_driftwood](../kubejs/data/creativeengineering/worldgen/placed_feature/meta/flotilla_driftwood.json).
4. **Not enough:** a part that places fewer than its minimum count fails the meta, and the slot is the plain anchor.

**Parts stay themselves.** Everything is one structure start (each chunk only looks 8 chunks around itself for starts
that reach into it). Its last piece is an index of which pieces came from which structure. Template trim, the soft
beard, ground matching's exempt tag and `/locate` ask the index, so a fresh watchtower beside a village is trimmed and
soft-bearded as on its own, and a part with no beard (a raft, a siege weapon tagged `none`) gets none. A meta's own
`terrain_adaptation` must not be `none` when any part bears a beard (the Beardifier only looks at starts whose structure
has one); the land metas use `beard_thin`, the sea ones `none`. Earth fill sees a meta as a whole.

**Knobs:** the definitions (share, counts, distances, spacing, margin, chance, water depth), the `meta/*` tags,
`enabled` of each meta in its group, `groups.flotsam.placement`; `[worldgen.systems] metaStructures`;
`[worldgen.meta_structures] log = true` logs every assembled meta (anchor and part positions, or which part fell short).
`[worldgen.meta_structures] avoidCoasts` (added 2026-10-19 off by default, 7.5; on by default since 2026-10-30, TODO
`structures/metas #5`): no part may start in, or reach with
its box (centre or a corner) into, a biome of `creativeengineering:coast_river` (`biome_fits.json` biome_tags: the
beaches, stony shore, gravel beach and every river). A satellite there is tried elsewhere, an anchor there leaves the
slot to the plain anchor. The six water metas in `meta/coast_ok` (flotilla, raft convoy, pillager fleet, shipwreck
debris and graveyard, sunken ruin cluster) are not affected, nor are feature parts. The switch is read when a meta is
assembled but Forge did not reload it live on the scratch server: restart after changing it.

**Seen on a scratch server (2026-10-11, seed 8675309):** both registered and staged (`... 2 meta structure(s)`).
`/locate` found a taiga village with a mini clock tower 83 blocks out, and on the way assembled a Towns & Towers
snowy-slopes village with 3 fresh watchtowers and a Terralith fortified village with one; villages in savanna or snowy
plains where no tower fitted were dropped, as designed. Forced generation: the tower and village positions are inside
the meta's pieces, a point about 340 blocks away isn't, the clock tower stands with its armor stand and villager (MCA renamed
it). A flotilla 441 blocks from spawn: 12 of 12 driftwood spots, 34 driftwood log blocks in its box.

**Seen on a scratch server (2026-10-11 evening, seed 8675309, two fresh worlds):** `[structure-tuning] ... 15 meta
structure(s)` and `[meta-structures] 14 meta structure(s) take slots from 99 anchor structure(s)`. `/locate` found
every meta tried (all but the two border-only camp metas): ruin cluster 350 blocks out (an ancient tower with 4 pumpkin
and ancient ruins), ruin scatter, sunken ruin cluster, shipwreck debris (floating wreckage at y 62 over a sunken wreck),
a graveyard of 4 wrecks, a raft convoy, a pillager fleet (warship + T&T pillager ship + 4 wrecks), a T&T savanna tower
with a trebuchet and a ballista, a Mysticrift tower with 3 beach camps, a T&T jungle fort with 2 camps, a sparse-jungle
camp among ruins, villages with towers, the flotilla. Part searches answered with the part inside a meta:
`philipsruins:pumpkin_ruins` at the cluster's satellite, `philipsruins:ancient_towers` at its anchor,
`towns_and_towers:wreckage_ocean` at the debris meta's wreckage, `minecraft:village_savanna` and
`mini_clock_tower:mini_clock_tower` at a village_towers meta's village and tower. The first world showed earth fill
cancelling two sunken ruin clusters with the default limits (whole-meta check); with the per-part check the second
world kept them. No errors from our classes.

### 3.17 Biome fits: every structure in the biomes its look and wood belong to (added 2026-10-11, all structures 2026-10-12)

Built for the towers first (2026-10-11, below), then for every land structure whose mod lists only part of the land
(2026-10-12, plan 7.2): 161 structures in 78 types, each on `land/*` families. The tower fits (`tower/*`) were renamed
into the same families with identical lists. "The system" below is current; the tower tables and scratch-server
results further down are the 2026-10-11 history and still use the old `tower/*` names. **Which structure is on which
family, and every family's biomes: the two tables in [biome_fits.md](biome_fits.md).**


Earlier tuning found that tower structures don't spawn in Terralith biomes. Reading every tower mod's biome list showed
two different problems:

- **The peaceful tower mods list a few vanilla biomes and no Terralith biome**: Watchtower Building's two towers, the
  desert and forest watchtowers, the mini clock tower. That is also why `meta/village_towers` (3.16) only stood in 8
  vanilla biomes.
- **Mysticrift's 8 pillager towers list every overworld biome** (`#minecraft:is_overworld`), so they do spawn in
  Terralith, but in any wood: the oak tower in Mirage Isles (dark oak), the purpur End tower in the Overworld.

Towns & Towers' outposts, Philip's ancient towers and the vanilla outpost already reached Terralith through
`biome_merges.json` and were left alone (the merges were retired 2026-10-11; 7.2 gives them fits).

**What each tower is built from and where it spawned** (wood and stone counted over every template the structure can
place, `tools/terrain_adaptation_scan.py` helpers; surface biomes only):

| Structure | Built from (block count) | Own biome list | Terralith biomes |
|---|---|---|---|
| `mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` | oak 787, cobblestone 301 | `#minecraft:is_overworld` (123 land biomes) | 84 |
| `...:birch_pillager_tower` | birch 779, cobblestone 296 | same | 84 |
| `...:acacia_pillager_tower` | acacia 787, cobblestone 300 | same | 84 |
| `...:jungle_pillager_tower` | jungle 786, cobblestone 261 | same | 84 |
| `...:mangrove_pillager_tower` | mangrove 787, cobblestone 301 | same | 84 |
| `...:cherry_pillager_tower` | cherry 787 (Vinery's, remapped in 3.11), cobblestone 301 | same | 84 |
| `...:desert_pillager_tower` | sandstone 900, birch 169 | same | 84 |
| `...:end_pillager_tower` | purpur 689, end stone 441 | same | 84 |
| `watchtower_building:fresh_watchtower` | spruce 1943, oak 768, dark oak 516 | plains, forest, birch forest, flower forest, taiga | 0 |
| `watchtower_building:abandoned_watchtower` | spruce 2037, oak 768, dark oak 688 | same 5 | 0 |
| `forest_watchtower:forest_watchtower` | dark oak 148, stone bricks 111, spruce 98, cobblestone 91 | forest, flower forest | 0 |
| `mini_clock_tower:mini_clock_tower` | stone bricks 123, oak 121, spruce 54 | plains, sunflower plains, snowy plains | 0 |
| `desert_watchtower:watchtower` | sandstone 311, terracotta 20 | desert | 0 |

**Which wood grows where.** Terralith's trees were read from its own biome, placed-feature and configured-feature JSON
down to the log blocks; vanilla tree features (no JSON in 1.19.2) by name. Biomes with no tree, or only bushes, count
as open land. From that:

| Wood | Vanilla biomes | Terralith biomes |
|---|---|---|
| oak | forest, flower forest, swamp, windswept forest | brushland, shrubland (hot, cold, rocky too), blooming valley, fractured savanna, temperate highlands, shield (+ snowy), lush valley, Yosemite lowlands, ice marsh, windswept spires, haze mountain, alpha islands |
| birch | birch forest (+ old growth), forest, flower forest | birch taiga, white and granite cliffs, orchid swamp, blooming valley, temperate highlands, Yellowstone, shield (+ snowy), lush valley, Yosemite lowlands, cloud forest, sakura grove and valley |
| spruce | taiga, snowy taiga, both old-growth taigas, grove, windswept forest | Yellowstone, forested highlands, Siberian taiga and grove, wintry forest and lowlands, alpine grove, snowy maple forest, cloud forest, cold and rocky shrubland, shield (+ snowy), lush valley, Yosemite lowlands and cliffs, frozen cliffs, caldera |
| dark oak | dark forest | Siberian taiga and grove, temperate highlands, Mirage Isles, lavender forest and valley |
| acacia | savanna, savanna plateau, windswept savanna | ashen savanna, savanna slopes, savanna badlands, fractured savanna, arid highlands, hot shrubland, brushland, moonlight grove and valley |
| jungle | jungle, sparse jungle, bamboo jungle | rocky jungle, jungle mountains, tropical jungle, amethyst rainforest and canyon |
| mangrove | mangrove swamp | none |
| cherry | none | sakura grove and valley |
| sand (sandstone towers) | desert, beach, snowy beach | desert canyon, desert spires, desert oasis, sandstone valley, lush desert, ancient sands, red oasis |

**Open land without an obvious tree goes to a wood by climate** (user, 2026-10-11; temperatures from the biome files,
"cold" = below 0.15 or snowy, vanilla's snow line):

| Climate | Wood | Biomes |
|---|---|---|
| cold | spruce | snowy plains, snowy badlands, gravel desert, snowy beach |
| temperate | oak and spruce | plains, sunflower plains, meadow, windswept hills, windswept gravelly hills, steppe, mountain steppe, shield clearing, valley clearing, highlands, alpine highlands, blooming plateau, rocky mountains |
| warm and dry, mesas | acacia | badlands, eroded badlands, wooded badlands (also oak: its trees), white mesa, warped mesa, painted mountains, Bryce canyon (also spruce: its trees) |
| warm and wet | jungle | none left over: every warm wet biome here already has jungle trees |
| beaches | oak, spruce, sandstone | beach (all three), snowy beach (spruce, sandstone), gravel beach (oak, spruce: no sand) |

Overlaps are on purpose (fractured savanna is oak and acacia, forest oak and birch, shield oak, birch and spruce): more
variety. Left out of every tower list: oceans and rivers; caves; skylands (no structure goes there); peaks, ice spikes
and cliffs of bare rock (frozen, jagged and stony peaks, snowy slopes, glacial chasm, emerald and scarlet peaks,
volcanic biomes, basalt cliffs, stony spires, stony shore); mushroom fields.

**The system.** A *fit* is a named biome list in
[biome_fits.json](../kubejs/generation_scripts/worldgen/biome_fits.json) and the structure tag of the same name in
`structure_tags.json`:

- A structure in tag `land/oak` spawns in the `land/oak` biomes **plus every parent fit's** (`land`), and in nothing
  else: its mod's own list is ignored. It never gets a sibling's biomes (`land/spruce`).
- The `land` parent lists **no biome**, so a structure tagged only `land` would never spawn. A structure in several fit
  tags gets all their lists (the plains village: `land/plains` + `land/highlands`; the forest watchtower:
  `land/spruce` + `land/dark_forest`).
- A fit with `"inherit": false` skips its parents: `land/end` (End only).
- Structures in no fit tag keep their own list.

**Families** (2026-10-12; full biome lists in the families table of [biome_fits.md](biome_fits.md)):

- **Biome-type families** say what a biome looks like, for villages, houses, ruins and dungeons: `land/plains`,
  `land/highlands`, `land/forest`, `land/flower`, `land/taiga`, `land/snowy`, `land/icy`, `land/desert`,
  `land/badlands`, `land/savanna`, `land/warm_dry`, `land/oasis`, `land/swamp`, `land/beach`, `land/peaks`,
  `land/mushroom`.
- **Wood families** say where a wood belongs, for structures built of one wood: `land/oak`, `land/spruce`,
  `land/acacia`, `land/mangrove`, `land/cherry`, `land/sand` (sandstone), `land/end` (purpur). `land/birch`,
  `land/dark_forest` and `land/jungle` serve both uses.
- **`land/any`** = every land biome of every family (underground structures); **`land/sky`** = `land/any` + the
  oceans (the sky village).
- Every land biome is in at least one family; skylands are in none. 0% biomes (they exist but don't generate in this
  layout) are listed too, so a later biome-tuning change leaves no hole.

| Old tower fit | Now |
|---|---|
| `tower` | `land` (parent, empty) |
| `tower/oak`, `tower/spruce`, `tower/birch`, `tower/acacia`, `tower/jungle`, `tower/mangrove`, `tower/cherry` | `land/` + the same name |
| `tower/dark_oak` | `land/dark_forest` |
| `tower/desert` | `land/sand` (no `inherit: false` needed: the parent is empty) |
| `tower/end` | `land/end` |

- **Real tags:** every fit is also the biome tag and the structure tag `creativeengineering:<fit>`, added by
  `structure_overrides.js` on each tag load. `/locate biome #creativeengineering:land/oak` works. The fit is read
  through the biome tag, so another datapack adding a biome to it widens the fit.
- **How:** `structure_overrides.js` validates the fits (any error applies none, as for the biome rules) and stages
  `{fits, structures}` to patcherMod
  [StructureBiomeFits](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/StructureBiomeFits.java).
  At ServerAboutToStart (before the biome rules), each structure's fit tags are joined into one biome list, and
  [StructureBiomesMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/StructureBiomesMixin.java)
  makes `Structure#biomes()` return it.
  - That one method is what every consumer reads: generation's biome check, `/locate` and explorer maps, the
    StructureCheck, meta structure parts and the water rule.
  - The list compares biome values, not holder instances, the same as the biome rules.
- **Why not Forge's structure modifiers:** Forge 43 has them, and their builder has `setBiomes`, but nothing in 1.19.2
  (vanilla or Forge) reads the modified settings back; checked in the Forge-patched jar.
- **Water rule:** still applies after a fit (the fits list no water biome anyway).
- **Switch:** CEConfig `[worldgen.systems] structureBiomeFits`. Rejoin to apply; new chunks only.
- **Log:** `[structure-biome-fits] 11 fit(s), 13 structure(s) given fitted biome lists: <id> <old count> -> <new count>, ...`,
  plus any listed biome that doesn't exist.
- **Report:** `structure_tag_report.md` has a `## Biome fits` table.

**Rates.** A structure in a group takes slots wherever it fits, so new biomes would multiply it: the abandoned
watchtower would be about the only `ruins` member in many Terralith biomes and went to 21x before rescaling.
`python3 tools/gen_structure_groups.py --fit-rates` solves each fitted structure's **weight and frequency** so its
density per area of its surface biomes stays what it was over its mod's own list:

- The weight decides who else moves. A high weight with a low frequency wins slots in the new biomes and then vetoes
  them (empty): with frequency alone the beach-only houses (villager fish shop, Farmer's aquaculture beach) fell to
  x0.2. Weight 1 everywhere hands the old biomes' slots to the neighbours: the desert village temple doubled.
- So each weight from 1 to the base that can still reach the density at frequency <= 1 is tried, the frequency that
  hits the density is taken, and the weight kept is the one that moves the other members of its group least
  (sum of |log(new total / total before the fits)|). A few passes let towers that share biomes settle together.
- `fit_base` in the member's entry keeps its weight and frequency on its own list. Every run starts from it, so a rerun
  after editing a fit doesn't compound, and a structure that leaves every fit gets its base back. Rerun after any fit
  or fit-tag edit; a second run changes nothing.
- **Full biomes (2026-10-12).** A group grid caps how many structures a biome gets. When the fits put many members
  into the same biomes (plains now holds the vanilla, Terralith, T&T Tudor and T&T sunflower villages), some members
  can't reach their old density even at frequency 1. `--fit-rates` lists them each run ("N fitted structure(s)
  capped"); they share the biome's slots instead.
- **`fit_keep: true`** in a member's entry lets `--fit-rates` raise its weight above `fit_base` (the smallest weight
  that reaches the density, at most 64x) so it keeps its density when heavier neighbours arrive; the neighbours give
  up those slots. Set on the 12 group-member towers (8 Mysticrift, abandoned, desert and forest watchtowers, mini
  clock tower). Without it, the T&T outposts (weights 100 to 250) moved into the Mysticrift towers' biomes (weight 5)
  and cut the acacia, birch, cherry and jungle towers to x0.6 to x0.8. With it: birch 5 -> 8, jungle and acacia
  5 -> 10, cherry 5 -> 17, every tower within 3% of its rate before (cherry +7%: it was capped at 96% before).

Per million chunks, surface biomes only (`structure_group_estimates.md` has the per-biome rows):

| Structure | Biomes before | Before | Biomes after (Terralith share of the Overworld) | After | Weight, frequency (base) |
|---|---|---|---|---|---|
| fresh watchtower | 5 | 17.8 | 43 (12.5%) | 49.9 | standalone, 1.0 (1.0) |
| abandoned watchtower | 5 | 8.4 | 43 (12.5%) | 23.5 | 16, 0.074 (21, 1.0) |
| forest watchtower | 2 | 6.2 | 48 (13.1%) | 42.7 | 7, 0.999 (135, 1.0) |
| mini clock tower | 3 | 5.5 | 35 (11.7%) | 30.6 | 32, 0.190 (108, 1.0) |
| desert watchtower | 1 | 5.0 | 10 (0.7%) | 10.0 | 1, 0.912 (110, 0.253) |
| village with towers (meta) | 8 | 9.4 | 59 (18.4%) | 24.7 | own 100, 1; anchors unchanged |
| Mysticrift oak | 123 | 18.0 | 35 | 7.3 | 5, 0.472 (5, 0.943) |
| Mysticrift jungle | 123 | 17.7 | 8 | 1.1 | 5, 0.275 (5, 0.928) |
| Mysticrift acacia | 123 | 8.0 | 19 | 2.0 | 5, 0.082 (5, 0.421) |
| Mysticrift birch | 123 | 7.3 | 18 | 1.4 | 5, 0.345 (5, 0.380) |
| Mysticrift desert | 123 | 9.0 | 10 | 0.7 | 5, 0.881 (5, 0.469) |
| Mysticrift mangrove | 123 | 11.3 | 1 (mangrove swamp) | 0.1 | 1, 0.022 (5, 0.594) |
| Mysticrift cherry | 123 | 8.0 | 2 (sakura, 0.2% of the Overworld) | 0.04 | 5, 1.0 capped: 96% of its old crowding (5, 0.421) |
| Mysticrift End | 123 (Overworld) | 8.2 | 1 (End highlands; midlands aren't in this pack's End) | 11.0 in the End | 1, 0.016 (5, 0.430) |

The Mysticrift totals fall because each tower now stands only where its wood grows; where it stands it is as crowded
as before. Side effects (same estimate):

- Vanilla and T&T pillager outposts rise x1.2 to x1.4: they take the `pillager_settlements` slots where Mysticrift
  towers no longer fit.
- Villager fish shop and Farmer's aquaculture beach drop to about x0.65: towers now share the beaches.
- Plain villages drop by about the number of villages that now get towers (the meta is a village too). Terralith-biome
  villages now get towers.

**Open choices** (made a default here, easy to change in `biome_fits.json` / `structure_tags.json`):

- The End tower moves to the End (it was in every Overworld biome). Alternative: give `tower/end` no biomes and it
  never spawns.
- The forest watchtower and both Watchtower Building towers left oak/birch forests: they are mostly spruce (and
  dark oak); they keep plains and the other temperate open land through `tower/spruce`. Add them to `tower/oak` to
  keep forests.
- Mangrove and cherry towers are now rare overall: their woods grow in 0.6% and 0.2% of the Overworld.

**Seen on a scratch server (2026-10-11, seed 8675309, this jar):**

- The log listed all 13 structures: mini clock tower 3 -> 34 biomes, Mysticrift oak 151 -> 34, End tower 151 -> 2.
  The live tags count biomes that never generate in this layout too.
- The water rule now keeps out 18 structures, not 25: no Mysticrift tower lists a water biome any more.
- `/locate` found every tower tried, each in a biome its fits allow, four of them in biomes their mod never listed:

  | Structure | Found in | Allowed by |
  |---|---|---|
  | fresh watchtower | old-growth spruce taiga | `tower/spruce` |
  | mini clock tower | Terralith brushland | `tower/oak` |
  | forest watchtower | Terralith blooming plateau | `tower` |
  | desert watchtower | Terralith highlands | `tower` |
  | Mysticrift oak | windswept forest | `tower/oak` |
  | Mysticrift birch | plains | `tower` |
  | Mysticrift End | End highlands (in the End) | `tower/end` |

Second scratch world after the climate split (same seed, the lists above): the log gave mini clock tower 3 -> 36,
watchtowers 5 -> 44, forest watchtower 2 -> 49, desert towers -> 10, no missing-tag line for the empty `tower` fit;
`/locate` found the desert watchtower in the red oasis, Mysticrift's desert tower in desert canyon, the acacia tower on
a savanna plateau, the fresh watchtower in old-growth spruce taiga and the mini clock tower in Terralith brushland.

**Seen on a scratch server (2026-10-12, seed 8675309, all `land/*` fits, patcherMod 1.0.6 rebuilt):**

- Log (30 fits / 167 since the vanilla swamp hut and temples joined their YUNG's versions' fits, 3.18, and the
  ocean fortress got `ocean/deep`, section 7 Done): `[structure-tuning] ... 30 biome fit(s) for 167 structure(s)` and `[structure-biome-fits] 30 fit(s), 167
  structure(s)`, no unknown biome. Examples: vanilla outpost 22 -> 7 biome entries, plains village 7 -> 14, igloo
  5 -> 18, sky village 27 -> 122 (land + oceans; the water rule doesn't keep it out). Live counts include biomes that
  don't generate in this layout.
- `/locate` + a scratch `/biomeat` (surface biome at the answer) for one structure per family: all 23 answers in a
  biome their fits allow, 14 in biomes their mod never listed:

  | Structure | Found in | Family |
  |---|---|---|
  | T&T Tudor village | Terralith rocky mountains | `land/highlands` |
  | vanilla plains village | Terralith hot shrubland | `land/plains` |
  | villager flower house | Terralith lavender valley | `land/flower` |
  | T&T old-growth taiga village | Terralith Yellowstone | `land/taiga` |
  | vanilla snowy village | grove | `land/snowy` |
  | Terralith ice spire | frozen peaks | `land/icy` |
  | saloon | wooded badlands | `land/badlands` |
  | vanilla savanna village, Greek house | Terralith brushland | `land/savanna` |
  | YUNG's jungle temple | sparse jungle | `land/jungle` |
  | Philip's ancient towers | Terralith brushland | `land/savanna` |
  | vanilla pillager outpost | dark forest | `land/dark_forest` |
  | woodland mansion | Terralith temperate highlands | `land/dark_forest` |
  | T&T birch forest village | Terralith Yellowstone | `land/birch` |
  | villager forge | Terralith Yosemite lowlands | `land/taiga` |

  Also in their own (still fitted) biomes: T&T forest village (forest), vanilla desert village (desert), swamp
  fisherman house (swamp), fish shop (beach), YUNG's zombie dungeon (sparse jungle), sky village (over a deep
  lukewarm ocean), Mysticrift birch tower (Terralith shield), catapult (desert).
- `structure_tag_report.md` was taken from this load (it lists the `land/*` fits).

### 3.18 Replacements and the witch huts group (added 2026-10-12, plan 7.1)

**Replacements.** A group member can take a share of another member's slots instead of slots of its own: in
`structure_groups.json`, `"replaces": "<member id>", "share": 0.1`. It is the slot conversion of the meta structures
(3.16, `MetaSlots`) with a plain structure as the target:

1. The group fills a slot as before (weighted pick, biome fall-through, frequency roll). The replacement's own weight
   is never used: picked directly, it is skipped (keep it at 1).
2. When the pick lands on the replaced member and that member keeps its frequency roll, the slot goes to the
   replacement with chance `share` (a hash roll of seed, chunk and the replaced member; the same roll metas use, so a
   meta and a replacement of one structure split it in id order, cut at 100%).
3. The replacement may only start where the replaced member's biomes (after fits and biome rules) also allow it
   (`MetaSlots.narrowBiomes`, from `ChunkGeneratorStructureBiomeRulesMixin`). If it can't start there, the slot is
   the replaced member after all.

So the two together spawn exactly as often as the replaced member did alone; only the split changes. Unlike a meta's
anchor, the replaced member loses what the replacement takes (`gen_structure_groups.py` subtracts it in the estimates).

- **Rules** (`structure_overrides.js`, an error applies nothing): both in the same group; not itself; not a meta
  structure on either side (metas use `anchor` + `share`); not a replacement of a replacement. `enabled: false` turns
  one off.
- **Switch:** CEConfig `[worldgen.systems] structureReplacements` (default true). Off, or a patcherMod jar without
  `MetaSlots.stageReplacements`: the replacements are left out of their group sets with a warning, and the structures
  they replace keep every slot.
- **`/locate`** of a replacement predicts it from the replaced member's slots (conversion lands on it, the replaced
  member keeps its roll, the replacement's own point fits both biome lists). `/locate` of the replaced member may
  walk to a slot that turned out to be the replacement; the chunk load corrects it and the search moves on.
- **Log:** `[meta-structures] N replacement(s) take a share of another structure's slots` on the first generated chunk;
  `[structure-tuning] ... N replacement(s)` at load. The report notes each one in its group table.
- **Tools:** `--fit-rates` leaves replacements alone (their rate is the share); the estimates flag them.

**`group/witch_huts`** (7.1 decisions 2 and 3): YUNG's witch hut and witch circle, the Villages & Pillages witch
village, all three moved out of `surface_dungeons`, plus the vanilla swamp hut as a 10% replacement of YUNG's hut.
All four have the `land/swamp` fit only (swamp, mangrove swamp, Terralith orchid swamp, ice marsh). Grid spacing 16,
separation 8 (at least 144 blocks between witch huts); the sync fitted the three on that grid to the rates they had:

| Structure | Weight | Frequency | 1 per (in swamps) | Before (surface_dungeons) |
|---|---|---|---|---|
| `betterwitchhuts:witch_hut` | 150 | 0.935 | 25 | 24 |
| `minecraft:swamp_hut` | 1 (unused) | 1.0 | 75 (10% of YUNG's hut) | never (blocked) |
| `betterwitchhuts:witch_circle` | 61 | 0.9433 | 37 | 37 |
| `villagesandpillages:village_witch` | 100 | 0.9374 | 29 | 29 |

YUNG's hut + vanilla hut = 1 per 24, as before.

**Vanilla temples** (2026-10-12, decision 1): `minecraft:desert_pyramid` replaces `betterdeserttemples:desert_temple`
and `minecraft:jungle_pyramid` replaces `betterjungletemples:jungle_temple`, both at share 0.1 in
`surface_dungeons`, with their YUNG's version's fits (`land/desert` + `land/oasis`, `land/jungle`). YUNG's
"Disable Vanilla Pyramids" / "Disable Vanilla Jungle Temples" went to false; the old sets `minecraft:desert_pyramids`
and `minecraft:jungle_temples` are emptied. Desert: YUNG's 1 per 60 + vanilla 1 per 179 = 1 per 57 as before;
jungle: 1 per 19 + 1 per 58 = 1 per 18 as before. The vanilla swamp hut needed YUNG's "Disable Vanilla Witch Huts"
switched to false (3.4); its old set `minecraft:swamp_huts` is emptied. The two groups keep separate grids, so a
witch hut can now stand next to a surface dungeon. Side effect: Terralith's spire (the only `surface_dungeons` member
that shares a swamp, the ice marsh) went from 1 per 25 to 1 per 24.

### 3.19 Surface-entrance dungeons: land only, with a surface check (added 2026-10-12, plan 7.1)

YUNG's zombie dungeon (crypt, starts at y 50 with a staircase up to 20 blocks long) and spider dungeon (spider cave,
y 70) are the two Overworld dungeons with a way in from the surface. They left their own sets for
**`group/entrance_dungeons`** (spacing 32, separation 16: at least 272 blocks apart); the sync kept their rates
(zombie 1 per 48, spider 1 per 44, weights 92 / 109, frequencies 0.971 / 0.9754). YUNG's skeleton and small dungeons
stay standalone (closed rooms, decision 7).

**Land only.** Both were in `water_ok` through `#dungeon/underground`; `water_ok` now ends with
`!#group/entrance_dungeons`, so the water rule takes oceans and rivers out of their lists.

**Surface check.** The water rule also reads the biome at the start's own height, so a crypt at y 50 under a beach
or a riverbank passed it. `biome_rules.water` now has `"surface_check": "#surface_check"` (tag = the entrance
dungeons). For those structures `StructureBiomeRules.surfaceCheck` samples the noise surface above the generated
start's box (centre and 4 corners, `ChunkGenerator#getBaseHeight`, before any chunk exists): if the surface biome
is in the rule's biomes, or `WORLD_SURFACE_WG` is above `OCEAN_FLOOR_WG` (water or lava on top), the start is
dropped and the pick falls through like a wrong-biome start. Hook: a second `@ModifyVariable` on the stored start in
`ChunkGeneratorStructureBiomeRulesMixin` (slot 15, the same local earth fill swaps). Off with the biome rules
switch (`structureBiomeRules`). Debug log line per dropped start (`[structure-biome-rules] surface check dropped ...`).

Limits: the corners of a large box can sit over water while the entrance doesn't, so dungeons near coasts are a
little rarer than inland; `/locate` (which never checks biomes in 1.19.2) may walk to a dropped slot and move on;
the offline estimates can't see the ground and ignore the check.

### 3.20 Standalone structures folded into groups (added 2026-10-12, plan 7.1)

User, 2026-10-12: "fold the statues, cats, it takes a pillage, all nether ruins, underwater fossils, and fresh
watchtowers into the appropriate groups". Data only (tags + `gen_structure_groups.py` sync). The structures with a
lowered rate (decisions 4, 5) had their standalone frequency scaled to the target first, so the sync moved them over
at that rate. Their old sets are emptied.

| Structure | Group | Weight / frequency | 1 per (own biomes) | Before | Target |
|---|---|---|---|---|---|
| `old_illager_statue:old_illager_statue`, `oldvillagerstatue:old_villager_statue` | `ruins` | 26 / 1.0 | 96 each | 42, 40 | 96 (decision 4) |
| `small_cat_statues:cat_1..3` | `ruins` | 19 / 1.0 | 111 each (64 together) | 30 each | as decided |
| `takesapillage:pillager_camp` | `pillager_settlements` | 87 / 1.0 | 91 | 39 | 64 (decision 5) |
| `takesapillage:bastille` | `pillager_settlements` | 34 / 1.0 | 146 | 55 | 128 |
| `twigs:bloodstone_obelisk` | `nether_ruins` (new) | 194 / 1.0 | 22 | 20 | keep |
| `philipsruins:start_nether_dungeon`, `sealedruinedghaststatue1`, `nether_lava_ruins` | `nether_ruins` | 100 / 0.5993 | 36 each | 37 | keep |
| `hopo:underwater_fossils` | `ocean_ruins` | 215 / 1.0 | 20 | 18 | keep |
| `watchtower_building:fresh_watchtower` | `remote` (`fit_keep: true`) | 160 / 1.0 | 75 | 67 | keep |

- **Below target where the grid is full.** The pillager grid is already at capacity on open land, so It Takes a
  Pillage got as much as fits (camp 91 instead of 64, bastille 146 instead of 128) and the members it shares biomes
  with gave up about 10% (T&T outposts 82 -> 91, siege weapons 101 -> 111). The fossils (18 -> 20) and the fresh
  watchtower (67 -> 75) are capped the same way; `fit_keep` lets a later `--fit-rates` raise the watchtower like
  the other towers. Existing ruins moved 3-5% (the statues and cats take decoration-level slots), ocean ruins about
  5%, remote houses 1-4%.
- **`group/nether_ruins`** holds every Nether ruin: the obelisk (decision 6) plus Philip's three Nether ruins, which
  were one set (21/18). The sync sized the grid for the most crowded Nether biome: spacing 14, separation 7 (128
  blocks), so the obelisk's old 20/4 grid did not carry over. Philip's Overworld blackstone ruin
  (`start_nether_ruin`) stays in `ruins`; the lost soul city is underground and stays standalone.
- **Pillager metas:** the bastille joined `meta/pillager_strongholds` (an anchor of `meta/pillager_stronghold_siege`),
  and Takes A Pillage's camp, already in `meta/pillager_camps`, is now an anchor of the three camp metas, so those
  can form on open land (the "border only" note in 3.16 no longer holds for them).

### 3.21 Way signs: own group, every settlement a destination (added 2026-10-12, plan 7.1)

User, 2026-10-12: "put waysigns in their own group and tune them to be roughly equal to the current rate. ensure the
meta villages and settlements can be pointed to, as well as every other structure in the settlement group". This
replaces decision 10 (2 signs per village, a measured success share) and settles decision 11.

- **`group/way_signs`**: the way sign alone, on the old set's placement (spacing 19, separation 10, exclusion zone 8
  chunks around `group/settlements` grid slots) with a new salt, weight 1, frequency 1. Same rate as before:
  about 1 per 20 x 20 chunks of land. The old set `supplementaries:way_signs` is emptied.
- **Destinations:** the `ServerEvents.tags('worldgen/structure')` handler in `structure_overrides.js` adds every member
  of `group/settlements` to `#supplementaries:way_sign_destinations` (`replace: false`, so the mod's own entries stay).
  That is 31 structures today: vanilla and T&T villages, Terralith's two fortified villages, and
  `creativeengineering:meta/village_towers` (a village with towers is stored under the meta's id, so before this signs
  never pointed at one). It follows the group: a structure added to `settlements` becomes a destination too.
- **Rates tool:** the jar ships `supplementaries:has_way_signs` empty and Supplementaries fills it at run time with
  `#minecraft:is_overworld`; `gen_structure_groups.py` now reads it that way (`RUNTIME_BIOME_TAGS`), so the estimates
  show the sign's real rate instead of "never spawns".
- The sign's own rate never affects where it points: once placed, a sign searches up to 250 chunks for the 2 nearest
  destinations on their own grids, through `StructureCheck`, which already applies the group rolls and meta conversions.
  A sign needs flat dry ground between y 63 and 105, so not every grid slot gets one.

### 3.22 Placement priority and companion villages (added 2026-10-12)

**Before this (the existing overlap rate, estimated, not measured):** structures of different groups never checked
each other. Vanilla places them block by block by generation step and structure-registry order, so where two
overlap the later one overwrites the blocks its templates cover and the earlier one's leftovers and terrain stay
(half-ruins between houses, a pad under a camp). From the rates in `structure_group_estimates.md` and typical
footprints (a village's pieces cover roughly 30 to 80 chunks; ruins, lone houses and dungeons together start about
once per 150 to 250 chunks in village biomes), **about a third of villages have another group's structure starting
inside them, ruins most often**. To be measured on the scratch server (ST-145) before and after.

**Ranks** (`structure_groups.json` `priority`; every member of a group gets its rank, a `#tag` or id can override):

| Rank | Groups / structures |
|---:|---|
| 100 | `#priority/big_settlements`: Terralith's fortified towns, C.C.C.'s red roof castle and the metas built on them (`fortified_ruins`, `fortified_camps`, `town_villages`) |
| 95 | `settlements` (villages, `village_towers`) |
| 94 | companion villages (one below their group) |
| 90 | `pillager_settlements` |
| 80, 79, 78 | `surface_dungeons`, `witch_huts`, `entrance_dungeons` |
| 76 | `vehicles` (2026-10-14) |
| 70, 69 | `remote` (houses, camps, their metas), `way_signs` |
| 60 | `ruins`, `nether_ruins` |
| 50 | `sky` |
| 40, 35, 30, 25 | `ocean_dungeons`, `ocean_ruins`, `boats`, `flotsam` |

Standalone sets (underground dungeons, mineshafts, strongholds, Nether and End) take no part.

**How it works** (patcherMod [PlacementPriority](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/PlacementPriority.java),
`ChunkGeneratorPlacementPriorityMixin`):
1. When a ranked start is generated (after its biome rules, earth fill and surface check; only if it keeps its
   frequency roll), every set with a higher-ranked member is looked at: its grid slots whose start could reach this
   start's pieces (8 chunks, the vanilla reference radius) are **predicted**: the set's weighted pick is replayed on a
   scratch chunk with every pack mixin applied (frequency rolls, meta conversion, replacements, biome rules, earth
   fill, and their own priority checks), so the prediction is exactly what that chunk will generate.
2. If a predicted higher-ranked piece box overlaps one of this start's piece boxes **in 3D** (a dungeon deep under a
   village is fine), the start is dropped (`INVALID_START`) and the slot falls through to the set's next pick, as for
   a wrong biome. It is dropped before its terrain is shaped: no pad, no beard, no half-building.
3. Same rank: no check (each group's grid keeps its own members apart). Order between chunks doesn't matter (nothing
   reads a neighbour; everything is predicted from the seed).
4. Cost: each higher-ranked slot is generated about twice (once predicted, cached per generator, set and chunk; once
   for real). Predictions inside predictions stop at depth 3 (ranks strictly rise per level; real chains are 1 or 2
   deep).

**Shaping: villages grow around higher-ranked structures** (added 2026-10-12, later the same day). For the jigsaw
members of the groups in `priority.shape` (`settlements`: every village, also as a companion; the metas built on
villages) a higher-ranked structure is not a reason to drop the village but an obstacle: the higher-ranked piece boxes
predicted within 16 chunks are cut out of the free space vanilla's jigsaw assembler fits pieces into
(`JigsawPlacementPriorityMixin`, `JigsawPlacement` m_227210_ HEAD). The village grows as if they were its own pieces:
a street that reaches a fortified town ends or turns, a house that would overlap it isn't chosen, so the village stops
at the town's edge. Only when the village's start piece itself overlaps a higher-ranked structure is the whole
village dropped (step 2). Which call is assembling is remembered at `tryGenerateStructure` HEAD; the assembler of a
meta's part elsewhere, or a `/place`, is left alone. Vanilla jigsaw only: YUNG's jigsaw structures and non-jigsaw
structures still drop whole (they rank below settlements, so they are the ones that give way anyway). Today only
villages have anything above them (fortified towns and their metas), so shaping only acts near fortified towns.

**Companions: a fortified town with 1 to 3 villages** (`meta/town_villages`, the meta on hold since 2026-10-11). It
takes 40% of fortified town slots (with `fortified_ruins` and `fortified_camps` 20% each, 80% of fortified towns are
metas; D1 2026-10-14, was 30 / 25 / 25; `meta/villages` no longer includes fortified towns, so `village_towers` stays 33% of plain villages). The town is
the meta (no parts); its villages are **companions**: full starts in chunks of their own 4 to 6 chunks (64 to 96
blocks; 112 to 160 until 2026-10-19, when Phase 8 showed no companion ever reached its town at that range) from the town, spread evenly around it, so each keeps its whole reach (a meta part must stay within 7 chunks,
which is why the meta was on hold). A chunk makes its companion at the end of `createStructures` when a predicted
`town_villages` in range names it; the village is picked by weight among `meta/villages` members of `settlements` whose
biome fits there. Companions skip frequency rolls and meta conversion and rank 94: the town (100) and grid villages
(95) win, so a village that would run into the town doesn't start; pillagers, dungeons, houses and ruins lose to them
and predict them too.

**Peaceful radius (added 2026-10-12).** Ranks only stop overlaps, so a pillager tower could still stand right beside a
camp (seen in game at a `farmers_camp`: the tower was its own pillager-grid start, not a meta part). `priority.peaceful`
gives the friendly structures a radius in chunks, measured as a square in XZ from their piece boxes (not the start
chunk; height ignored), inside which no hostile structure starts, whatever the ranks:

| Peaceful | Radius (chunks) |
|---|---:|
| `#priority/big_settlements` (fortified towns and their metas) | 8 |
| `settlements` (villages, companion villages included) | 5 |
| `remote` (camps, lone houses, camp metas) | 3 |

Hostile: `pillager_settlements`, `surface_dungeons`, `witch_huts`, `entrance_dungeons` (65 structures). A hostile start
predicts the peaceful sets' slots (and companion villages) within radius + 8 chunks of its pieces and is dropped like a
wrong biome when any of its pieces is inside one's radius. Peaceful structures in turn skip hostile ones in their own
rank check and shaping (one that would overlap is inside the radius and drops anyway), so the two checks never predict
each other and a camp no longer loses to a pillager outpost that touches it. The hostile groups' rates fall by however
much land the radii cover; this is not in the offline estimates. By design a hostile big structure such as It Takes a
Pillage's bastille rarely stands near a town or village (ST-158: 3 of 4 kept bastilles were inside a peaceful radius
and dropped); a `/locate` of one started next to a village answers far away or times out (TODO
`structures/rates-and-groups #6`, 2026-10-30: no change).

**Knobs:** `priority` in `structure_groups.json` (`groups`, `structures`, `shape`, `peaceful` {`groups`, `structures`, `hostile`}), `companions` in `meta_structures.json` (`structures`, `count`,
`distance` in blocks), `[worldgen.systems] placementPriority`, `[worldgen.placement_priority] log = true` (one line per
dropped start and per companion). **Limits:** `/locate` predictions don't know about drops (a search may visit a
dropped slot, load it, find nothing and move on, as with earth fill); companion villages are not on any grid, so
`/locate`, explorer maps and way signs don't find them (`/locate creativeengineering:meta/town_villages` finds the
town); the offline estimates leave companions out and don't count drops.

### 3.23 Fixes from the scratch run (2026-10-18, DEPLOYED with 1.0.7)

Six of the open bugs from the 8.0 scratch run ([plan_1.0.6.md](plan_1.0.6.md) 2.1, [testing/structures/](testing/structures/00_plan.md)),
fixed in patcherMod 1.0.7 and the worldgen data, DEPLOYED with pack 1.0.7 (2026-10-18). All
of them act on new chunks only.

1. **Village layouts differed from one server start to the next (05_phase_5.md 3.3).** Cause found: the pools reach
   PoolTuner in an order that changes between JVM runs. `pool_tag_report.md`, rewritten at every world load, has 10
   versions in git since 2026-10-02, and they list `minecraft:village/desert/houses` in 9 different orders (plains too),
   with the same pieces: three were written within one minute on 2026-10-02 with the same 6,894 pieces, each in its own
   order. Vanilla's JSON order is none of them and no string hash order matches, so some mod rebuilds the pools
   through an identity-hashed collection. Jigsaw picks a piece by its index in the pool's list, so the same seed built different
   villages after each restart. The Phase 5 batch only showed it for village_desert and village_savanna because their
   box centre is what the meta's `/locate` returns; a plain village reports its chunk, so every jigsaw pool was likely
   affected. **Fix:** PoolTuner sorts every pool's pieces by key, then weight (a stable order), before the rest of its
   work, and rebuilds every pool whose order changed (logged as "put in canonical order"). Switch: CEConfig
   `[worldgen.pools] canonicalOrder` (default true). The order is no longer vanilla's, so a seed's villages differ
   from a vanilla game's (they already did) but repeat on every start.
   **Weight bug from the sort, fixed 2026-11-02 (`rates-and-groups #9`, plan_worldgen_edit.md B1).** The sort
   renumbered duplicate pieces (`#2`, `#3`) while `pool_config.json` kept `current` under the old keys, so 223
   duplicates in 27 pools played another piece's weight. Data reset to the originals (the 9 vinery / beautify edits
   kept); `PoolTuner.mergeCurrent` now lets an unedited entry follow the live weight when its stored `original`
   differs, and keeps an edited one with a warning in `pool_tag_report.md`.
2. **Cloud Storage and Domestication Innovation houses dropped on reload (05_phase_5.md 3.2).** Their pieces hold
   the builtin `ProcessorLists` holder, which the world's registry codec refuses: saving logs "Element ...
   minecraft:empty is not valid in current registry set" and writes the piece as `{}`. **Fix:** at ServerAboutToStart,
   before the tuning, PoolTuner gives every pool piece whose processor list isn't the world's own the world's holder
   of the same key (the field is found by type, so no SRG name). One log line counts the rebound pieces. PoolTunerTest
   reproduces the save failure with a piece built by `StructurePoolElement.single` and shows it saving after the rebind.
3. **Ground match gave up over deep structures (03_phase_3.md 4.1).** A sample column was skipped when any piece in
   the 3x3 chunks covered it in XZ, whatever its height, so an ancient city, Philip's underground ruins or a mineshaft
   hid most columns, and with fewer than 16 the base was left as is without a log line. **Fix:** the column's ground is
   found first, and the column is skipped only when a piece's box reaches that ground: from `subDepth` + 1 below the
   top block to 1 above it (a piece standing on the ground, or the top being its own blocks). With `log = true`, a
   template whose base was left alone for lack of samples now logs one line with the column count.
4. **Beach, river and swamp driftwood never placed (03_phase_3.md 4.2).** Outside `#is_ocean`, Upgrade Aquatic's
   feature only places on dirt (near water) or sand under its origin, so it can't float there, and our "water below"
   filter made the four overrides dead. **Fix:** `driftwood_beach`, `_river`, `_swamp` and `_extra` now filter for a
   bank: the block under the spot is not water, and water is beside it (4 sides) at that level or one lower. Same
   rates (0.25x the mod's). Only open-ocean driftwood floats, as before; `driftwood_land/*` still adds the rare logs
   further inland. 3.12 has the rates.
5. **Skeleton dungeon, bone dungeon and lost soul city never started (7.3.3).** Their start height (y -50 to -30)
   is in a cave biome (`terralith:cave/deep_caves` almost everywhere), and `land/any` lists surface biomes only.
   **Fix:** a new fit `land/any/deep` (inherits `land/any` and `land`) adds the 14 cave biomes (vanilla's dripstone
   caves, lush caves and deep dark, Terralith's 11 `cave/*`), and the three moved from the `land/any` tag to it. They
   are standalone sets, so no group rate changes (`gen_structure_groups.py --check` gives the same estimates). Their
   grouping and rates still wait on the cave-structure pass (Todo, "Underground").
6. **Earth fill was blind to stone bases (plan_1.0.6.md 2.1, 02_new_mods_tests.md 4.2).** Only dirt, sand, gravel,
   clay and path counted, so the drum tower's 49x45 polished andesite plate counted as "small, 10 earth" and could hang
   over a cliff. **Fix, opt-in:** structures in the new structure tag `creativeengineering:earth_fill/stone_base`
   (`worldgen/earth_fill.js`; for now only `eugene:ruined_drum_tower`) also count the block tag of the same name
   (`blocktags.js`: `#minecraft:base_stone_overworld`, cobblestone, stone bricks and their mossy/cracked forms, smooth
   stone, polished andesite/diorite/granite/deepslate, deepslate bricks) toward both their size and the limits. Not for
   every structure: most stone is the building, not a pad, and counting it everywhere would move every size and rate.
   The template cache now keeps one map per earth tag list, so the two lists don't clear each other.

Still open from 2.1: the undead castle vs earth fill (a decision), ST-135 (fortified metas vs the 120 s timeout; timeout 300 s and shares 0.25 since 2026-10-30, 3.27), the
ocean geode drop (reworked 2026-10-22 to the lowest-floor cap, ST-133 scratch part scratch server PASSED 2026-10-22, SQ-GE), the piglin camp (Nether, out of scope; the stone-base tag could take it later).

### 3.24 Desert temples: own group, double the gap (added 2026-10-23, TODO `structures/rates-and-groups #10`)

`group/desert_temples` (structure_tags.json) holds `betterdeserttemples:desert_temple` and `minecraft:desert_pyramid`
and is excluded from `group/surface_dungeons`. Placement 32 / 20: starts at least 21 chunks (336 blocks) apart, was
11 (176) in `surface_dungeons` (16 / 10, whose separation can't reach 20 below its spacing). The pyramid keeps its
`replaces` + `share` 0.1 entry (restored by hand after the sync re-added it as a plain meta member). YUNG's temple
frequency 0.285 on the new grid keeps the estimate at 1 per 63 (was 63). Priority rank 82 (above `surface_dungeons`
80, so a temple drops an overlapping mimic), and in `peaceful.hostile`. The T&T desert mimic gained the slots the
temples used to win (estimate 1 per 25 -> 21); its frequency went 0.739 -> 0.521 (1 per 24). Philip's badlands dungeon
did not move (1 per 26). The jungle temple pair stays in `surface_dungeons` (its distance is #2). Sync, `--check` and
`tools/test_structure_groups.sh` (28 checks) pass. New chunks only.

- [ ] **ST-225 Groups:** `structure_tag_report.md` lists `desert_temples` (32 / 20) with the pyramid as a replacement; no
  config error in latest.log.
- [ ] **ST-226 Distance:** census over a pregenerated desert square: no two desert temple / pyramid starts closer than
  21 chunks; about 1 per 63 x 63 chunks of desert.
- [ ] **ST-227 Priority:** a temple overlapping a desert mimic drops the mimic (`[placement-priority] dropped
  towns_and_towers:mimic_desert ... overlaps betterdeserttemples:desert_temple`), never the reverse.

### 3.25 Deadwood patches and doubled stumps (added 2026-10-25, TODO `farming/trees #2`)

Geophilic Reforged's scattered stumps (`tree_stumps/mc/*`) and fallen logs (`fallen_trees/mc/*`) for the six vanilla
woods now try twice as often: overrides in `kubejs/data/geophilic_reforged/worldgen/placed_feature/` put a `count 2`
in front of upstream's placement (stumps about 0.29 -> 0.58 tries per chunk, rarity 1/12; fallen logs 0.23 -> 0.47,
1/12, acacia and cherry 1/15). The BoP, Traverse and Windswept woods are not overridden (those mods are not in the pack).

New anchorless metas `creativeengineering:meta/deadwood_<wood>` (one per wood) in their own group `group/deadwood`
(spacing 16, separation 5, all weight 100, so oak and birch split the forests they share). Each is 3 to 6 stumps and
2 to 4 fallen logs of that wood (table in 3.16); its biomes are Geophilic's `#geophilic_reforged:wood_type/mc/<wood>`.
The parts are `creativeengineering:meta/deadwood_stump_<wood>` / `deadwood_fallen_log_<wood>` placed features:
upstream's ground filters (ocean-floor heightmap, dry land, oak-sapling survival) without count, rarity, square or
biome. A spot that fails a filter places nothing; the meta itself never fails. Also in `type/environmental`.
Estimate: 1 per 16 x 16 chunks of each wood's biomes. New chunks only. Sync, `--check`,
`tools/test_structure_groups.sh` and `tools/test_worldgen_tools.sh` pass.

Not changed: the sunk acacia log is Geophilic's bush (TODO `farming/trees #2` notes), still open.

- [ ] **ST-228 Load:** `structure_tag_report.md` lists `deadwood` (16 / 5) with the six metas; no
  `[meta-structures]` warning (`placed feature ... does not exist`) and no config error in latest.log.
- [ ] **ST-229 Locate:** `/locate structure creativeengineering:meta/deadwood_acacia` from a savanna (and `_spruce`
  from a taiga) finds one; at the spot: stumps and fallen acacia (spruce) logs within about 16 blocks, none in water.
- [ ] **ST-230 Wood:** each patch uses its biome's wood; none in biomes outside Geophilic's wood list (a desert, plains).
- [ ] **ST-231 Doubled:** census of standalone acacia stumps / fallen logs in a forceloaded savanna square, about twice
  the 1.1.03 count (rough: about 0.5 stumps and 0.4 fallen logs per chunk before filters).
- [ ] **ST-232 Quark spire teleport in the End [scratch]:** `/tp` an armor stand (or player) in the End to an
  unexplored spot (300016 100 300016): done in milliseconds, no "ticks behind", no region file at the target. Then
  an ender pearl onto Myalite next to a spire viaduct still warps along it (the chunks there are loaded).

### 3.26 Seabed clearance and Sky Structures' islands (added 2026-10-30, TODO `structures/terrain #5`, `#7`)

**Floating islands under the sea, stacked (`structures/terrain #5`).** Sky Structures 1.0.0 is not a structure mod:
its 9 islands (`grass_island`, the oak, spruce, birch, acacia, jungle, dark oak and bees variants, and the blacksmith
island, template `sky_grass_blacksmith`) are features added to every biome at `raw_generation`
(`forge:add_features`, `forge:any`), so the group, biome-fit and earth-fill systems never see them. Read from the jar
(`GrassIsland*Feature#place`): per chunk each rolls 150 in 1,000,000 (blacksmith 130), then places its template **1 or
2 times** at a random spot of the chunk at **`OCEAN_FLOOR_WG` + 16..79**. Over an ocean the floor is y 30 to 45, so the
island lands at y 46 to 124, often under the sea surface; the second placement puts a second island within 16 blocks of
the first (the two beehive island "twice on top of each other").
Fix: patcherMod `worldgen/SkyIslands` + `mixin/ConfiguredFeatureSkyIslandsMixin` answer `ConfiguredFeature#place` for
every feature registered under `sky_structures`: same roll, Overworld check, template, mirror, rotation and processor,
but one island per hit, and the base is the highest `WORLD_SURFACE_WG` (counts water) at the centre and corners of the
footprint, never below sea level. The band above it (16 + 0..63) is still the mod's; `structures/sky #3` changes it
there (`MIN_GAP`, `GAP_SPREAD`), not in structure data. `/place feature sky_structures:<island>` skips the roll.
Server stop logs `[sky-islands] placed N: {<template> over water|land=n}`.

**Underground structures opening the seabed (`structures/terrain #7`).** Terralith's cave structures pick an absolute
start height y -40..40, Subterrestrial's land cabins y 6..48 (jungle cabin 6..44, base / top / bottom pieces 5 high).
`water_ok` lets everything underground start under water biomes on the assumption that it is under the seabed, but
nothing checked the seabed's height. Under an ocean or a deep lake (floor y 30 to 50) the top of such a start reaches the
floor; its beard (`beard_box` carves the whole piece box, `beard_thin` up to about 11 blocks above the piece bottom)
turns the floor over it into air and the sea floods it (seen with `subterrestrial:jungle_cabin`).
Fix: patcherMod `worldgen/SeabedClearance` + `mixin/ChunkGeneratorSeabedClearanceMixin` (generated start of
`tryGenerateStructure`, slot 15): a structure in the structure tag `creativeengineering:seabed_clearance` is dropped
when, at the centre or a corner of its box widened by 4, the ground (`OCEAN_FLOOR_WG`) is no more than 8 blocks above the
box top and a fluid lies on it (`WORLD_SURFACE_WG` above `OCEAN_FLOOR_WG`). Deeper starts and starts under dry land are
kept, so rates only drop under water. Tag (`terrain_adaptation.js`, end): the 7 Terralith `underground/*` structures and
the 6 Subterrestrial land cabins. Left out: the ocean cabin (y 24..44 in ocean biomes only, built for the seabed; tagged
it would almost never start), the deepslate cabin (y -56..-6), Philip's y -40 ruins, buried treasure (its box sits at
y 90 until placed). Counted in `/cwglocate` census as `SURFACE_CHECK`; world load logs
`[seabed-clearance] world load: 13 structures tagged`, server stop `[seabed-clearance] dropped N starts: {...}`.
Lowering a start instead of dropping it (the row's first idea) needs moving an assembled jigsaw start and resetting its
cached box after earth fill has read it; not built.

- [x] **ST-233 Islands [scratch]:** SQ-WG30-D02 PASSED 2026-10-30: 13 `/place feature` islands (7 over deep ocean,
  6 on land), one island each, all 16+ above sea and ground; the `over water` count label also fires on land when a
  sample point has grass or flowers (`WORLD_SURFACE_WG` vs `OCEAN_FLOOR_WG`).
- [ ] **ST-234 Seabed [scratch]:** SQ-WG30-D03 FAILED 2026-10-30: 145 / 148 starts dropped per world, no seabed hole over
  any of 9 finds, but kept starts under the ocean still flood from cave or aquifer water below an intact seabed (jungle
  cabin [104, 84] on seed 8675309: box top 28, seabed 51, water around its floor; witch huts [-92, 0], [94, 93]).

### 3.27 Weight strip, test overrides, fortified metas vs `/locate` (added 2026-10-30, TODO `structures/rates-and-groups #3`, `#5`, `#6`)

Built 2026-10-30, offline checks only (node sim, `--check`, `test_structure_groups.sh` parity incl. the new
`testoverrides` variant); scratch run queued (SQ-WG30-C01 to C05).

**Weight strip (#3).** User (2026-10-19): strip every structure weight nobody chose; the 2026-09-27 migration fit and the
`--fit-rates` solves stood for each structure's old per-set rate, which the biome fits made meaningless.
- New member field `"tuned": "<what, when, who decided>"` (validator: `MEMBER_KEYS` in `structure_overrides.js`;
  `sync` carries it, with `note`, when a member moves group). Marked 2026-10-30 (35 members): the 6 Campsite camps, the
  whole `vehicles` group (witch wagon included), the two statues and three cats (`ruins`, 7.1 decision 4), It Takes a
  Pillage's camp and bastille (decision 5), the monument (frequency 0.1), the 13 `fit_keep` towers.
- New mode `python3 tools/gen_structure_groups.py --strip-weights [GROUP ...]`: every group member without `tuned`, not a
  meta, not a replacement -> weight 100 (`MEDIAN_WEIGHT`), `fit_base` dropped, `fit_keep: false`; frequencies kept (the
  brief's recommendation: they hold the migration's rarity; a later pass can reset them after the census). Standalone
  members lose their weight override (back to the mod's own: Philip's underground set all 1, Terralith's underground set
  11/2/13/5/5/4). Prints old -> new with the rate ratio per structure and writes the estimates against the file before.
- `--fit-rates` skips `fit_keep: false` members, so stripped weights stay stripped. It still solves the `tuned` members
  that carry `fit_base`.
- Run 2026-10-30 on every group: 204 members stripped, 173 weights changed. Biggest movers (rate x): T&T swamp outpost
  7.87 (weight 2), fake trader camp 7.59 (6), Philip's ocean fortress 6.46 (29), fish shop 5.07, gold washing facility
  4.76, T&T Swedish village 4.50; down: flower house 0.16 (781), country house 0.30 (447), Farmer's squids 0.30 (389),
  Philip's rare ruin 0.35 (254), Hopo's warm small ruins 0.36 (560). Tuned members move with their neighbours: the
  Mysticrift mangrove tower x0.13, desert tower x0.17, oak tower x0.43 (low hand weights against neighbours now at 100);
  the fortified metas x1.63 to x1.67 (their Terralith anchors went 46/108 -> 100). A dry `--fit-rates` on a copy would
  restore the towers (mangrove weight 5 -> 10, frequency 0.04 -> 0.86) but also re-solve the Campsite camps, vehicles
  and statues; not run (user decision, TODO `structures/rates-and-groups #2`). Full table: the session scratchpad
  `strip_weights_table.txt` (not kept in the repo); `structure_group_estimates.md` compares to mod defaults again.

**Test overrides (#5).** CEConfig `[worldgen.debug] testOverrides` (default false; `WorldgenConfig.testOverrides()`)
makes `structure_overrides.js` read `kubejs/generation_scripts/worldgen/structure_test_overrides.json` and change the
raw configs in memory before planning, so every normal check still runs. Keys: `"<id>"` / `"#<structure tag>"` ->
`frequency`, `weight`, `share` (metas with an anchor, replacements); `"groups"` -> `spacing`, `separation`. Any other
key (biomes, biome rules, peaceful, priority, earth fill...) refuses the whole file with the 7.4.8 rule in the
message; nothing from it applies and the world loads as without it. Active: one `TEST OVERRIDES ACTIVE: ...` warning
per load listing every change, `TEST OVERRIDES (n)` in the summary line, a `## Test overrides` report section. The
shipped file boosts every group member rarer than about 1 per 190 chunks after the strip (the 7 Mysticrift towers,
great tower siege, Philip's rare ruin, aquacultures beach, Simply Houses forest and taiga sets, flower house, desert
pyramid replacement share) and the fortified metas (anchors weight 400, camps / ruins share 0.45, town villages 0.1).
Sim: `node tools/structure_tuning_sim.js --test-overrides[=FILE]`; RhinoRun arg `testoverrides`.
`build_deploy.py` ships the file and warns when a staged toml has `testOverrides = true` or `structureDensity` != 1.

**Fortified metas (#6).** `[worldgen.locate] searchTimeoutSeconds` 120 -> 300 on 2026-10-30, back to 120 on 2026-11-02 (user, `locate #5`: SQ-WG30-C05 answered in 1 to 5 s) (CEConfig default and instance toml,
marked tuned in its comment; searches are non-blocking, a long one only holds a locator thread). Shares
`meta/fortified_ruins` and `meta/fortified_camps` 0.20 -> 0.25 each (`tuned` in `meta_structures.json`; D1 now 25 / 25
/ 40, 10% plain). With the strip: fortified ruins 1 per ~340 -> 182 chunks, camps ~337 -> 185 (estimates). A 300 s
search sees about 33 loads (9 s each, phase 2b); at a settlements grid of 48 the chance per in-biome slot is about
(48/182)^2 = 7%, so about 9 in 10 searches should answer. The bastille is unchanged (3.22: peaceful radius by design).

- [x] **ST-217 Strip report [scratch]:** SQ-WG30-C01 PASSED 2026-10-30 (stripped members 100, tuned ones kept).
- [ ] **ST-218 Census after the strip [scratch]:** 2026-11-01 (`structures/rates-and-groups #13`): cause of the biome
  false negatives not found from the code. Ruled out: the `seabed_clearance` drop (it reports reason `surface_check`, and
  a drop is a miss, never a false negative); the point (vanilla's jigsaw and mineshaft points are seeded from the seed
  and chunk, and generation's `Structure#isValidBiome` is the same lookup the prediction makes); the biome predicate
  (prediction and generation both use `StructureBiomeRules.filter` over the fitted `biomes()`); a non-exact biome search
  (1.19.2 `Climate.RTree` is an exact nearest search, so its answer is always one of the entries `TiedBiomes` walks);
  Citadel's `MultiNoiseBiomeSource` mixin (posts `EventReplaceBiome`, no listener in the pack); Tectonic's density
  functions (stateless records); ModernFix `faster_structure_location` (placement only). The 11 r1031 ones include
  `ancient_city` x2 (deep_dark only) and a mineshaft, which `TiedBiomes` should cover, so the prediction's point or
  biome differs from generation's in a way the code doesn't show. patcherMod now writes a diagnosis into
  `falseNegativeAt` for every biome false negative: the point found again after the load, its one-sample biome and
  every entry at the nearest distance (`+` passes, `-` fails), and the built start's first-piece centre. The next census
  (SQ-WG30-C02b) says which: a point away from the start piece, or a passing nearest biome.
  SQ-WG30-C02 PARTIAL 2026-10-30: `/cwglocate census 96 2144 -2592`
  at density 1 after the Chunky square (100,616 slot chunks, 2,749 starts, 316 s, 5 biome false negatives: old refinery
  x4, a mineshaft). Starts per group member: settlements 6 in 17 slots (Mediterranean 2, plains, taiga, jungle, old
  growth taiga 1 each); pillager settlements 24 in 37 (fake trader camp 4, savanna outpost 3, 2 each for camp ruin,
  tower siege, Mediterranean outpost, taiga outpost, 1 each for 9 more); remote 33 in 66 (shepherd farms 4, cod
  aquaculture 3, 2 each llama stable, fletcher house, forge, 1 each for 20 more); ruins 89 in 146 (ruin scatter 16,
  ancient towers 10, ruin cluster 8, ancient ruins 6, field stone rocks 6, 1 to 5 for 18 more). Too few per member to
  judge x0.7 to x1.4; needs density 4 or a larger radius.
- [x] **ST-221 Overrides off [offline + scratch]:** SQ-WG30-C03 (offline PASSED 2026-10-30: switch off with the file =
  no file, byte-identical sim output; scratch PASSED 2026-10-30, C01 boot has no TEST OVERRIDES line, C03 boot has one
  with 33 changes). **ST-222 Overrides on [scratch]:** SQ-WG30-C04 (offline PASSED 2026-10-30: node
  and Rhino identical with the switch on; a `biomes` / `peaceful` key refuses the file and stages nothing new; scratch
  PASSED 2026-10-30: one `TEST OVERRIDES REFUSED` line naming 7.4.8, report "None applied").
- [x] **ST-223 Fortified metas within 300 s [scratch]:** SQ-WG30-C05 PASSED 2026-10-30 (camps 1 to 4 s, ruins 5 s from
  0 0, no timeout); ST-135 rerun with the overrides: SQ-WG30-C04 PASSED (ruins 4 s, camps 8 s, 3 ruins 64 to 71 and 2
  camps 79 to 81 blocks from their village).

### 3.28 Earth fill: every surface piece, prune instead of cancel (added 2026-10-30, TODO `structures/earthfill #13`, `#14`, `#16`, `#3`, `#4`)

BUILT 2026-10-30 (patcherMod 1.1.04 rebuilt and in `mods/`; offline EarthFillTest 91 checks, MetaTest 76; not run in
game). Scratch r1031 (2026-10-31): SQ-EF-01, -04, -08 PASSED; SQ-EF-05 FAILED (ancient_crypt, ST-238); SQ-EF-02,
-03 client looks pending, SQ-EF-06 owner call (ST-240), SQ-EF-07 not run. Plan: [briefs/structures_earthfill.md](briefs/structures_earthfill.md) "Build plan (2026-10-30)". New chunks only.

**Every surface piece joins the footprint (#14, owner decision D1 "cover all structures").** Before, the footprint
water share (3.13) only saw pieces with earth blocks (`earth_fill/earth`), so plank, wool, sandstone and brick bases,
legacy pieces (vanilla desert and jungle pyramids, igloos) and custom pool elements were never judged over water: the
cause of #11 (desert structures in water) and #12 (camps and ruins floating). Now a piece with no earth joins as a
*span*: each column of its box (not already in an earth layer) counts by the predicted surface (the column's top)
when that lies from `baseLayers` below the piece's bottom to its top: water when the surface is water, ground when
solid; a piece buried under the surface or floating above it counts as neither. Only for structures in the
`surface_structures` generation step and pieces reaching above sea level less `baseLayers` (mineshafts, strongholds
and fortresses read no terrain); terrain-matching streets, empty and feature pool elements and meta index or feature
pieces never join. Switch `[worldgen.earth_fill] footprintAllPieces` (default true). Stilted structures that stand in
water on purpose go in the new structure tag `creativeengineering:earth_fill/wet_ok` (no footprint share, other
limits kept): `minecraft:swamp_hut`, `betterwitchhuts:witch_hut`, `mysticrift_pillagers_towers_aris:mangrove_pillager_tower`
(`worldgen/earth_fill.js`); the swamp fisherman house is a water-fill structure and skipped already. Note on #11:
raising `maxOverWater` (the row's first idea) would keep more flooded starts: earth fill only cancels, it never fills.

**Prune instead of cancel (#16, owner decisions D3, D4).**
- Wet village pieces: pruning (`earth_fill/prune`) also drops a piece whose own footprint is too wet (`maxWaterSurplus`)
  once it holds `pruneMinWetColumns` (16) water columns, with its jigsaw subtree, so a village at a lake loses its
  wet houses instead of going whole. The log reason reads `(footprint W water / G ground)`.
- Meta parts: with `[worldgen.meta_structures] keepShortParts` (default true) a village part of a meta is pruned as it
  would be alone, and a satellite over its limits is dropped from the meta (log `[earth-fill] <meta> ...: dropped K of
  N parts: <ids>`); the index piece is rebuilt (`MetaStructure.withParts`). The anchor over its limits, or no satellite
  left, still cancels the meta and the slot is the plain anchor.
- Short parts: a part that fits fewer times than its minimum (a satellite near a coast with `avoidCoasts`, on a cliff)
  keeps what fitted instead of failing the meta; the meta fails only when no satellite fitted at all
  (`no satellite fitted`). Off: the old rule (one short part fails the meta).

**Fixes (#13).** E1 an override with two numbers (`id=-1,-1`) counted as unlimited and skipped the size's mound limit
and the footprint; E2 under `log = ALL` an unlimited village was still pruned (the log level changed the world); E3
pruning judged a sunk piece at its unsunk bottom; E5 override ids without a namespace are `minecraft:` ones, an
unparsable id is skipped with a warning, unsorted `sizes.bounds` are sorted (one warning), an earth tag holding no block
warns once; E6 the water-block cache is cleared on a tag reload. Not a bug after all: E4 (an earth block below the
beard's reach counts when its spot is open; it is not filled).

**Sunk starts (#3).** `philipsruins:ancient_crypt` added to `terrain_adaptation/sunk_start` (start height -5, rigid
start piece; wrong, 2026-11-01: its only start element is terrain_matching, so the guard refuses it, see ST-238; owner call: dropped from `sunk_start`, TODO `structures/earthfill #17`). patcherMod now refuses the tag for a structure whose start pool has a terrain-matching element (warning
`... its start pool has a terrain_matching piece; ignored`): `antiquus_crypta`, `village_nilotic`. Field
`JigsawStructure.startPool` is SRG `f_227605_` (the brief's `f_227607_` is `maxDepth`). The seabed ruins
(`ocean_ruins`, `ocean_fortress`) wait for the in-game look (ST-238).

**Undead castle (#4).** Instance toml and `configs_edited`: `[worldgen.earth_fill] overrides =
["eugene:undead_castle=24,320,400"]` (mound 400 instead of the huge size's 128; water and air as before). Narrowing
its fits waits for the per-fit measurement (ST-240).

- [ ] **ST-235 Flooded pyramid gone [scratch]:** *r1031 (2026-10-31): client look pending (SQ-EF-03).* desert square with a lake or river, `log = "ALL"`: every
  `minecraft:desert_pyramid` / `betterdeserttemples:desert_temple` line shows a footprint; none of the kept ones stands
  in water; a desert structure on a lake logs `too wet` -> CANCELLED (ST-229 folded in). SQ-EF-03.
- [ ] **ST-236 Stilted huts kept [scratch]:** *PASS r1031 (2026-10-31, SQ-EF-04).* swamp and mangrove square: swamp huts, witch huts, mangrove towers log no
  footprint (`wet_ok`) and generate in water as before. SQ-EF-04.
- [ ] **ST-237 Earthless counts and cost [scratch]:** *r1031: counts read with SQ-EF-03 (look pending); the pregen cost half (SQ-EF-07) not run.* `grep -c` of `[earth-fill]` lines before and after; tent camps
  and ruins over lakes (#12) now cancelled; pregen time of the SQ-WGP square within 10% of 3:50. SQ-EF-03, SQ-EF-07.
- [ ] **ST-238 Crypt flush, nilotic refused [scratch]:** *FAIL r1031 (2026-10-31, SQ-EF-05 = SQ-TER-01): the load line warns `philipsruins:ancient_crypt` is in `sunk_start` but its start pool has a terrain_matching piece, ignored; the crypt is not sunk. Seabed look pending. 2026-11-01: the guard is right for the crypt and narrowing it changes nothing: the crypt's start pool (`ancient_crypt/crypt_start`, Philip's 10.8 jar, not overridden) has one element, `crypt_start`, and that element is itself `terrain_matching` (its json comment says rigid; the value is not). A terrain-matching start has no beard, ignores its start height (each column is placed on the heightmap), and its rigid rooms are placed from the surface at their jigsaw, so the -5 start height sinks nothing and a raised beard would bury the rooms 5 layers. The 3.28 note "rigid start piece" was wrong. Owner call 2026-11-01: dropped from `sunk_start` (`terrain_adaptation.js`); the crypt keeps its own terrain-matching placement. Rerun as SQ-TER-01b.* load line counts 7 sunk starts and warns for no tagged
  structure; `/place structure philipsruins:ancient_crypt` on a slope: ground ring at the entrance roof, no pit, rooms
  open; then look at an `ocean_ruins` and `ocean_fortress` on a seabed to decide their tag (brief #3). SQ-EF-05.
- [ ] **ST-239 Wet house pruned [scratch]:** *r1031: client look pending (SQ-EF-02).* a village at a lake logs `pruned K of N pieces` with a `footprint` reason
  and keeps its dry part. SQ-EF-02.
- [ ] **ST-240 Undead castle survives [scratch]:** *r1031 owner call (SQ-EF-06): no castle in the highlands square; across the world 1 of 3 kept, 2 cancelled over-air 708 / 562 against the 320 limit.* castles in two r 400 squares (plains/taiga and highlands): at least
  half kept, none with earth over more than 6 blocks of air; per-fit cancel table decides dropping `land/highlands`.
  SQ-EF-06.
- [ ] **ST-241 Meta parts dropped, not the meta [scratch]:** *PASS r1031 (2026-10-31, SQ-EF-08, after a rerun on a coast).* `avoidCoasts` on, `/place structure
  creativeengineering:meta/farmers_camp` (and `house_camp`, `ruin_scatter`) 40 blocks from a beach: the meta builds with
  fewer satellites (meta log `short:`) instead of `not placed`; at the spawn beach itself (anchor on the coast) still
  `no anchor fits`. Earth-fill `dropped K of N parts` lines carry the satellite ids; a saved meta reloads (rejoin, no
  error). SQ-EF-08.
- [ ] **ST-242 Override fixes [scratch]:** *PASS r1031 (2026-10-31, SQ-EF-01).* overrides `["minecraft:village_plains=-1,-1", "village_savanna=-1,-1,-1"]`,
  `log = "ALL"`: plains villages log the size's mound limit (not -1) and can cancel; savanna villages log limit -1, are
  never cancelled and never pruned (no `pruned` line). SQ-EF-01.

### 3.29 Earth fill phase 4: wet jigsaw pruning everywhere, meta parts pruned, robustness (added 2026-10-30, TODO `structures/earthfill #15`)

BUILT 2026-10-30 (patcherMod source, compiled; EarthFillTest 113 checks, MetaTest 76; not run in game). Scratch r1031
(2026-10-31): SQ-EF-09, -11 PASSED; SQ-EF-10 pending (needs a hand-made shore case).
Plan: [briefs/structures_earthfill.md](briefs/structures_earthfill.md) build plan phase 4 (E7 to E12). Owner ask:
metas pruned to raise their placement chance, and wet pieces of every jigsaw on water pruned. New chunks only.

**Wet pruning for every surface jigsaw.** Before, only `earth_fill/prune` (villages) was pruned (3.28). Now pruning has
three modes (`EarthFill.Prune`, picked by `pruneMode`): FULL for `earth_fill/prune` (piece limits `pruneMaxHanging`,
`pruneMaxMound` and the piece water share, as before); WET for every other start in the `surface_structures` step with
two or more pieces whose first is a jigsaw piece (camps, outposts, ruins, towers): only a piece whose own footprint is
too wet (`maxWaterSurplus`, at least `pruneMinWetColumns` 16 water columns) goes, with its jigsaw subtree; NONE for
the rest (single pieces, legacy pieces, underground, `water_fill/structures`, `earth_fill/wet_ok`, an unlimited
override, and the new structure tag `creativeengineering:earth_fill/no_prune`, empty today, for jigsaws whose pieces
only make sense together). The first piece is never dropped; the start is then judged whole as usual. Switch
`[worldgen.earth_fill] pruneWetJigsaws` (default true; `prune` off turns both off). Log: `pruned K of N pieces
(village|wet only); over the piece limits: ...`.

**Meta parts and anchors pruned (E9).** `filterParts` prunes every part as it would be alone, anchor included, and no
longer only under `keepShortParts`: a meta whose anchor (or satellite) reaches over a lake loses those pieces first
instead of being dropped or cancelled. `keepShortParts` still decides what happens to a part that is over its limits
after pruning (satellite dropped, anchor cancels the meta).

**Robustness.**
- E7: the whole-start water share needs `[worldgen.earth_fill] footprintMinWater` (default 9, 0 = any) water columns;
  1 water / 0 ground no longer cancels a cliff-edge start. A start under the floor logs `(surplus limit S, under 9 water)`.
- E8: a kept parent forgets its junctions to dropped children by identity (`forgetJunctions`), not by index, so custom
  placers with another junction order lose the right ones; for a dropped piece the tree could not link, a kept piece's
  junction pointing into its box (and into no kept box) goes too, so no beard blob is left at a dead street end.
- E10: earth-fill and water-fill lines logged during a PlacementPriority prediction carry `(predicted)`;
  `LocateStats.reject` already skipped predictions.
- E11: earth fill and placement priority run in one `ModifyVariable` (ChunkGeneratorEarthFillMixin), earth fill first;
  ChunkGeneratorPlacementPriorityMixin lost its slot-15 injector. BiomeRules and SeabedClearance keep their own.
- E12: the footprint counts a column once, at its lowest layer that is not buried (stacked pieces counted twice). Not
  changed (noted): template processors ignored by the earth count; `WorldgenConfig`'s silent fallback (on purpose, 3.28);
  the two-field volatile caches on reload; `SHARED` terrain caches freed only at server stop.

- [ ] **ST-297 Wet jigsaw piece pruned [scratch]:** *PASS r1031 (2026-10-31, SQ-EF-09).* `log = "ALL"`, a lake or river square: a non-village jigsaw (pillager
  outpost, a camp, a ruin) logs `pruned K of N pieces (wet only)` with a `footprint` reason and stands with its dry part;
  no village line says `wet only`. SQ-EF-09.
- [ ] **ST-298 Meta pruned before dropped [scratch]:** *r1031: pending, needs a hand-made shore case (SQ-EF-10).* meta log on; a meta with a jigsaw anchor or satellite at a lake
  logs a `pruned` line for that part id and builds; `dropped K of N parts` only for parts still over their limits. SQ-EF-10.
- [ ] **ST-299 Footprint floor and log tags [scratch]:** *PASS r1031 (2026-10-31, SQ-EF-11; the radius-96 census takes 1457 s on one locator thread).* `log = "ALL"`: no `too wet` line with fewer than 9 water;
  `under 9 water` lines end `-> kept`; `(predicted)` lines appear with placement priority on, and `/cwglocate` census
  counts EARTH_FILL and PRIORITY rejects as before (no mixin error at boot). SQ-EF-11.

- [ ] **ST-328 Earth fill on the placed blocks [scratch]:** under `log = "ALL"`, kept starts have no column more than
  `beardReach` over air or water, no flooded base, no foreign template earth (ground match), ponds and `wet_ok` stilts keep
  their water, the logged counts match the world, cancelled and pruned pieces are absent. SQ-EFB-00 to SQ-EFB-16
  (`/zq under` probe). TODO `structures/earthfill #18`.
  Scratch r1103w 2026-11-03 (first run, [judged](../perf/runs/r1103w/w/judged_w1.md)): FAIL SQ-EFB-02, -03, -04, -11,
  -21 (overhangs logged as 0 over air; a villages-size limit question), PASS -00, -01, -05, -09, -12, -17 to -20, -22;
  OPEN -06, -07, -14; batch faults -08, -10, -15; -13 client look; -16, -19 (W2), -22 (W3 half) not run.

### 3.30 Terrain batch 4, beard footprint, ocean geode floor (added 2026-10-30, TODO `structures/terrain #1`, `#2`, `#6`)

Status: BUILT 2026-10-30, patcherMod installed 2026-10-30 17:06; BROKEN: scratch r1031 (2026-10-31) SQ-TER-01 FAILED (ancient_crypt sunk start refused), SQ-TER-05 PASSED; SQ-TER-02 to -04, -06 not run. FIXED 2026-11-01: the crypt is off `sunk_start` (`structures/earthfill #17`), `/reload`; rerun SQ-TER-01b. New chunks only.

**Batch 4 (#1).** Scan re-run 2026-10-30 (`terrain_adaptation_scan.py --fresh`: nothing new since 10-26 apart from the
grown Sky Villages and the seabed-clearance labels). Tagged in `terrain_adaptation.js`:
- `soft` + `template_trim`: Farmer's Structures `aquacultures_common` and `aquacultures_carp`, the old swamp fisherman
  house (ponds: water is a built block for the trim, so the pond stays whole), Philip's `level_three_ruins` (its
  `meta/large_ruins` parts follow the anchor).
- `terrain_adaptation/sunk_start`: the carp pond (start -4, its grass rim at y 4 of a 4-layer dirt bowl), like the camps.
- New tag `creativeengineering:template_trim/air` (patcherMod `TemplateTrim#trimAir`): only air in columns with
  nothing solid (no built block, pad or plant) is dropped; pad, plants and snow stay, and the soft footprint and
  earth count stay the untrimmed ones. Terralith's spire gets it and keeps `none`: its 94x98 `layer1` air box cut a
  square notch into the hills, and the full trim would also drop 110 to 273 snow-only columns per upper layer (snow
  resting on the layer below, checked offline against the jar).
- Left out: `philipsruins:start_nether_ruin` (until the Nether move), the villages (one family at a time if wanted),
  `eugene:undead_castle` (strong in the scan; a walled compound, `soft` alone would fit, look first).
After tagging the scan shows 122 tagged, 1 strong (the undead castle) and 6 mild left.

**Beardifier fill estimate (#2).** `tools/earth_fill_sim.sh <seed> <n> footprint` (new `EarthFillSim` mode) takes the
columns with a solid block in the bottom `baseLayers` of every rigid piece and reads the gap under the piece's bottom
from the predicted terrain. Seed 4242, 60 sites each (mound depth 8):

| Structure | Mound columns earth fill counts p50/p90/max | Missed (no earth in the column) p50/p90/p99/max | Starts with missed | Newly over the mound limit |
|---|---|---|---|---|
| village_plains | 0/44/127 | 3/107/307/307 | 50% | 15% |
| village_desert | 0/146/336 | 1/113/378/378 | 50% | 18% |
| village_savanna | 0/97/361 | 0/43/166/166 | 42% | 2% |
| village_snowy | 0/60/214 | 3/141/513/513 | 53% | 13% |
| village_taiga | 0/92/464 | 1/110/519/519 | 50% | 8% |
| pillager_outpost | 0/0/0 | 0/138/273/273 | 32% | 23% |

Igloo and mansion have no beard. Far over "a few %", so step 2 is built behind a switch:
`[worldgen.earth_fill] beardFootprint` (default false). With it on, `EarthCheck.cancels` also collects each rigid
piece's solid bottom (`EarthFill.localBase`, trim-aware) and counts every column holding no earth whose ground lies more
than `maxMoundHeight` below the piece (`baseMound`, at the sunk level) toward the mound limit; a start with no earth is
judged as small. Log lines add `(N under built floors)`. Whole-start check only: pruning still judges earth, so a
village would lose the whole start rather than one house; calibrate with SQ-TER-05 before turning it on.

**Ocean geode floor (#6).** `[worldgen.ocean_geodes] floorPatch` = `OFF` / `CUT` / `SWAP` (default `SWAP`, user
2026-10-30: an option not to trim the geode, swapping the stone that reaches into the water instead). An ocean geode
that is not moved (the `stayShare` ones) and not `untouched` (prismarine geodes belong on the floor) notes the floor of
each column in its box before it places (`OceanGeodes#beforePlace`, same HEAD hook) and patches it after
(`afterPlace`, RETURN). Per column from the box top:
- `SWAP`: the geode keeps its shape; its outer and middle layer blocks from the old floor up become the column's old
  floor block, so it reads as a mound of sea floor; it stops at the inner layer (the opening into the crystals). A sand
  or gravel mound over the cavity holds like any generated sand (GeneratedFallingBlocks) and falls when a player
  disturbs it, as vanilla sand over a cave does.
- `CUT`: its blocks above the old floor become water (air over the sea level), and at or under the old floor the first
  outer or middle layer block becomes the floor block.
- Both: air under water becomes water (an opened cavity); an unusable noted floor block (such as an earlier geode's
  basalt) takes the box's most common floor, else gravel.
Headless: `OceanGeodesTest` (13 checks, both modes).

Tests:
- [ ] **ST-300 Ponds on a slope [scratch]:** `/place structure` the common and carp ponds and the fisherman house on a
  hillside: no square pad, no air notch, the water stays in the pond, the rim flush (carp: no dip or plinth).
- [ ] **ST-301 Spire in the hills [scratch]:** a placed spire leaves the hills round its base uncut; its snow is whole.
- [ ] **ST-302 Level-three ruins [scratch]:** two on a slope, no grass slab around them; a `meta/large_ruins` cluster
  with one as anchor looks the same.
- [ ] **ST-303 Load line [scratch]:** *FAIL r1031 (2026-10-31, SQ-TER-01): counts 109 tagged, 99 trimmed, 1 air-trimmed, 7 sunk starts, plus the ancient_crypt `ignored` warning (ST-238). 2026-11-01: crypt dropped from `sunk_start`; rerun SQ-TER-01b (7 sunk starts, no warning).* `[terrain-adaptation] world load:` trimmed +4, `1 air-trimmed`, sunk starts +1.
- [ ] **ST-304 Beard footprint calibration [scratch]:** *PASS r1031 (2026-10-31, SQ-TER-05, calibration table in the run doc); the look is in game.* `beardFootprint = true`, `log = "ALL"`: lines show
  `under built floors`; cancels per family against the sim table above.
- [ ] **ST-305 Geode floor [scratch]:** `SWAP` (default): ocean geodes that reach the floor are mounds of the floor's
  own block with the crystals showing only in the opening; `CUT`: flush with the floor, rim in the floor's block;
  prismarine geodes untouched in both.

### 3.33 Earth fill: water depth 2, sandstone and terracotta bases (added 2026-11-02, TODO `structures/earthfill #19`)

BUILT 2026-11-02 (patcherMod 1.1.07 build/libs 00:42, not in `mods/`; EarthFillTest +8 checks, not run yet). New chunks only.

**Water depth (user: "lower the water depth required for cancelled to 2, not 6").** Earth over water counted toward
`maxOverWater` only where the water ran deeper than the beard's reach (`beardReach` 6, `buryReach` 10), because the beard
fills shallower water. Now water has its own reach, `[worldgen.earth_fill] waterReach` (default 2, never past the beard's
reach): earth over water 3 or more deep counts. Air keeps `beardReach`. Pruning counts the same way (`pruneMaxHanging` 8
counts water past 2), so a village drops a house on a deep fill before the whole start is judged. Expect more cancels and
prunes at rivers, lakes and coasts (SQ-EFB-19 measures it against a `waterReach = 6` world).

**Desert and badlands bases.** Only `earth_fill/earth` blocks (dirt, sand, gravel, clay, path) are counted, so a structure on
a sandstone or terracotta floor showed little earth and the water and air limits saw little of it. `tools/earth_base_scan.py`
(bottom 3 layers of every rigid template): T&T badlands village 0 % earth (red sandstone 56 %, terracotta 14 %), wooded
badlands village 0 % (terracotta 37 %), badlands outpost 0 %, Philip's badlands dungeon 0 %, vanilla desert village 24 %
(sandstone 54 %), fortified desert village 43 %, T&T desert outpost 49 %, desert village temple 22 %, Philip's desert ruins 42 %.
New pair like `stone_base`: block tag `creativeengineering:earth_fill/sand_base` (`#forge:sandstone`, `#minecraft:terracotta`,
Philip's sand stone brick, red sand stone brick, sandstone tiles) counts for the structures in the structure tag of the same
name (`worldgen/earth_fill.js`, those 9). Their size class rises with the counted floor, so their air and mound limits grow
with it. Left out: Better Desert Temples and the desert mimic (buried / underground); the desert pyramid is a legacy piece,
judged by its footprint (3.28). Code: `EarthFill.withBases` (earth tags plus either base tag, cached per earth list).

- [ ] **ST-329 Water depth 2 [scratch]:** kept starts have no water deeper than 2 under their earth; more over-water cancels and
  prunes than with `waterReach = 6`, villages lost near rivers reported. SQ-EFB-17 to -19.
- [ ] **ST-330 Desert bases counted [scratch]:** desert and badlands structures in `sand_base` log their sandstone / terracotta
  floor as earth, houses over water or cliffs are pruned, dry-desert counts within 10 % of before. SQ-EFB-20 to -22.

### 3.31 Meta batch: sky clusters, caravans, meteorite strikes, temples with ruins (added 2026-10-30)

Thirteen metas (rows in the 3.16 table), all data except one new part field; five more the same evening (meteor
shower, witch coven, two sieged villages, struck monument) and a second part field, `cluster`. Shares were chosen here (the user asked for
the metas, not their rates); every anchor keeps its rate as in 3.16, except YUNG's desert temple, see the pyramid below.

**`overlap` (patcherMod `MetaStructure.Part`, new field, default false).** A part with `"overlap": true` skips the
margin check against the pieces placed before it (structures: `Layout.collides`; features: `Layout.covers`), so its
copies may stand on the anchor. Its box still counts for later parts, so an overlap part goes last. The meteorite metas
use it with `ae2:meteorite` (tag `meta/meteorites`): a start places its pieces in list order, the anchor's first, and
AE2's `MeteoritePlacer` clears every block above its crater curve up to y 255 inside the chunk being placed, so the
crater cuts through houses, roads, tents or the pyramid that were placed just before it in that chunk (crater about
20 to 28 blocks across for the largest meteorites; a lake, lava, ice, snow or obsidian crater by biome, as AE2 decides).
This is the "meteorite strike applied to any village" the user asked for: no separate mechanic was needed. Two AE2
details matter: its `findGenerationPoint` keeps only half the chunks (a seeded coin flip), so a part gets 24 tries; and
its piece box is 144 x 144 blocks around the meteorite's chunk (AE2's own reference reach), so a meteorite's chunk must be
within 3 chunks of the meta's start chunk to stay inside the 7-chunk reach, and the meteorite metas are in
`meta/coast_ok` (with `avoidCoasts` on, the box corners would nearly always touch a river). In an ocean biome AE2 puts
the meteorite on the floor with no crater: the sky stone sphere replaces the ruin blocks it overlaps
(`underwater_meteorite`).

**An anchorless meta as a replacement (`meta/meteorite_desert_pyramid`).** The vanilla pyramid is never picked by its
weight (it replaces YUNG's temple, 3.18), so no meta can anchor on it. The struck pyramid is a meta with no anchor whose
`structure_groups.json` entry has `"replaces": "betterdeserttemples:desert_temple", "share": 0.08`: MetaSlots already
converts any structure (`Conversion.target`), so in its slots it builds its parts centred on the start chunk, the pyramid
first (`distance [0, 0]`), then the meteorite. That first part is the meta's core (2026-11-01, `MetaStructure#core`,
`structures/metas #11`): a short core fails the meta even with `keepShortParts`, and the core is not a satellite. `structure_overrides.js` now allows this (only a meta with an anchor may
not replace, and no meta may be replaced). The user asked for it more common than the plain vanilla pyramid, and for a
lower vanilla rate: plain pyramid 10% -> 5% of YUNG's temple slots, struck pyramid 8%, so YUNG's own temples go from 90% to
87% of their slots (the only anchor whose rate changes in this batch; the 35% `desert_temple_ruins` share is of the
slots YUNG's temple keeps).

**Floating islands (`creativeengineering:sky_island`).** Sky Structures' islands are features (3.26), which a meta can't
place at a chosen spot (the feature rolls 150 in a million and picks its own spot in the chunk). So
`kubejs/data/creativeengineering/worldgen/structure/sky_island.json` is a one-piece jigsaw over the mod's 9 templates
(pool `creativeengineering:sky_island`: plain and the 6 tree islands weight 2, bees and blacksmith 1), 135 to 165 blocks
above `WORLD_SURFACE_WG`, like the sky village's +150. It is in the earth-fill exempt list. Until 2026-11-06 it had no
structure set and spawned only as a part; since then the Worldgen jar ships `structure_set/sky_island.json` (baseline
64 / 16) and the island is a member of group `sky` (weight 100, frequency 0.985, like the balloons and temples; about
0.22 of the group's slots, structure_group_estimates.md), so lone islands also generate (owner request after scratch
r1106spf). It is still a part of `meta/sky_village_islands`.

**Caravans (7.4.9, brief `briefs/structures_metas.md` #1).** Built as clusters (data only): parts land at random angles,
not in a line; the line layout of the brief is not built. The witch hut is tried for every witch caravan (count 0 to 1),
and stands only in swamps (its own fit), so in a dark forest the wagons travel alone.

**`cluster` (patcherMod `MetaStructure.Part`, second new field, default 0 = off).** For structure parts: the first copy
lands in the `distance` ring as usual; every later copy is tried `spacing` to `cluster` blocks from the first, not around
the anchor, so the copies stand as one group on one side (`village_siege_camp`: a siege camp outside the village rather
than a ring). The validator rejects it on feature parts and past the 112-block reach.

**Sieged villages need no peaceful-radius exemption.** Placement priority (3.22) compares whole starts: the siege weapons
are pieces of the village's own (peaceful, `settlements`) start, so nothing drops them; hostile starts nearby are kept
away from the whole meta as from the village. The siege weapons only stand in their own biomes (plains, snowy plains,
desert, savanna, taiga), so a forest village never becomes a sieged one (the slot stays the plain village).

**Spawn overrides inside a meta.** The game finds a structure's spawn overrides (guardians in a monument, witches and
cats in a witch hut) by the structure a start belongs to, so on its own a part inside a meta loses them. patcherMod's
spawn lookup (`StructureSpawnBoxes.mobsAt`, at the head of `ChunkGenerator#getMobsAt`) answers per part instead: every
part, the anchor too, keeps its spawn box (the meta's pool when the meta has a box) for the box's category and its own
`spawn_overrides` for every other category, each only on that part's own pieces (`"full"` = the box around the part's
pieces). This runs with `[worldgen.systems] spawnBoxes` off too (overrides only). Outside every part, vanilla's lookup
runs. Metas keep `"spawn_overrides": {}`: the meta's own would cover every piece of every part (2026-10-30: the copies
in `struck_monument`, `witch_coven` and `witch_cart_caravan` put guardians across the meteorite's 144-block piece box and
witches and cats on the wagons; removed). Before that date a boxed part also lost its own overrides of other categories
(the witch hut's cats).

**Monument rooms after a reload (fixed 2026-11-02, `structures/contents #8`).** Vanilla saves an ocean monument as
its building piece only; the rooms (and the three elder guardians, two in the wing rooms and one in the penthouse) are
rebuilt from the seed whenever a start is read back from disk (`OceanMonumentStructure.regeneratePiecesAfterLoad`),
but only for a start whose structure is the monument. A `meta/struck_monument` start saved before all its chunks were
decorated (a `/locate`, a server stop, chunks unloading mid-generation) came back with the outer walls only: no rooms,
no elder guardians. patcherMod `StructureStartMetaMonumentMixin` (`StructureStart.loadStaticStart` RETURN) calls
`MetaStructure.repairMonumentsAfterLoad`, which rebuilds a roomless monument part the vanilla way from the chunk it was
generated at and swaps it in when box and facing match (else one warning per start). Lone monuments were never
affected; guardian respawns (SQ-META31-09) were not either.

**Meteor shower biomes.** The meta's own `biomes` is the fit biome tag `#creativeengineering:land/desert` (sand deserts;
filled at load by `structure_overrides.js`), so a meteorite slot outside a desert stays a plain meteorite.

**Shares stacked on one anchor (id order):** storage cloud: `cloud_balloons` 20% + `cloud_cluster` 30%; pillager camps:
the three camp metas 50% + `pillager_camp_meteorite` 10%; large underwater ruins: `sunken_ruin_cluster` 50% +
`underwater_meteorite` 10%; villages: `meteorite_village` 5% + `village_siege_camp` 4% + `village_siege_ring` 4% + `village_towers` 33%; AE2 meteorite (deserts): `meteor_shower` 50%; C.C.C. trader camp: `trader_caravan`
25% + `waysign_camps` 25%; YUNG's desert temple: `desert_temple_ruins` 35% + struck pyramid 8% + plain pyramid 5%.

Known: `tools/test_structure_groups.sh` step 3 (old patcherMod jar, `--no-meta`) fails before reaching these: the
script loads `MetaSlots` at its top since an earlier change, so the simulated jar without it throws. Not from this batch.

Tests (rates in 3.16; `[worldgen.meta_structures] log = true` shows anchor and part positions):

- [ ] **ST-306 Loads [scratch]:** *PASS r1031 (2026-10-31, SQ-META31-01): 39 runtime / 46 writer metas, +18 since the last run (the "13 higher" is stale).* `[meta-structures] N meta structure(s) take slots from ...` is 13 higher than before
  (12 anchored + the struck pyramid), `4 replacement(s)`; no "share above 100%" warning; no `failed to generate as a
  part` error for `ae2:meteorite` or `creativeengineering:sky_island`.
- [ ] **ST-307 Sky clusters [scratch]:** *r1031: log half PASSED (SQ-META31-02); looks client check pending.* `/locate structure creativeengineering:meta/cloud_cluster`, `balloon_fleet`,
  `cloud_balloons`: 2 to 4 clouds, 2 to 6 balloons, 1 to 2 of each, none overlapping.
- [ ] **ST-308 Sky village islands [scratch]:** *r1031: log half PASSED (SQ-META31-02: 2 to 5 islands); 7 of 8 sky villages started at exactly y 213 got no island (`no satellite fitted`), 0 of 12 at other heights: look at the start height.* `/locate ... meta/sky_village_islands`: 2 to 5 islands around the
  village at about its height, none inside it; `/place structure creativeengineering:sky_island` places one island.
- [ ] **ST-309 Caravans [scratch]:** *r1031: log half PASSED (SQ-META31-03); the swamp hut half needs the biome logged, client check pending.* `/locate` each of `witch_cart_caravan` (a swamp one has a witch hut),
  `pillager_caravan`, `trader_caravan` (2 to 3 camps and a way sign).
- [ ] **ST-310 Struck village [scratch]:** *PASS r1031 (2026-10-31, log half, SQ-META31-04, -04b: two village types); crater look in game.* `/locate ... meta/meteorite_village`: a sky stone meteorite within 16 blocks
  of the village middle, the crater cut through the houses (no house floating over it), villagers alive around it.
  Try two village types (vanilla plains, a T&T one).
- [ ] **ST-311 Struck pyramid [scratch]:** *FAIL r1031 (2026-10-31, SQ-META31-04): a `meta/meteorite_desert_pyramid` placed with `0/1 desert_pyramid` (short) is a crater with no pyramid; with `keepShortParts` a replacement meta should fail when its first part is short. FIXED 2026-11-01 (patcherMod, `MetaStructure#core`): a meta without an anchor whose first part is a structure part at distance [0, 0] with chance 1 has it as its core; a short core fails the meta even with `keepShortParts` (log `not placed, core part ... placed 0 of at least 1`), and the core is not a satellite, so a pyramid without its meteorite also fails (`no satellite fitted`). Either way the slot is YUNG's plain temple. Built, not installed; retest SQ-META31-04b.* `/locate ... meta/meteorite_desert_pyramid` and `/locate structure
  minecraft:desert_pyramid` both answer; the struck one has a crater cutting the pyramid. The report line says 8% / 5%.
- [ ] **ST-312 Temples with ruins [scratch]:** *r1031: log half PASSED (SQ-META31-05: 2 to 3 ruins, 46 to 60 out); biome fit and spacing client check pending.* `/locate ... meta/desert_temple_ruins` (desert ruins only) and
  `meta/jungle_temple_ruins` (Philip's jungle-fitted ruins), 2 to 4 each, clear of the temple.
- [ ] **ST-313 Underwater strike [scratch]:** *PASS r1031 (log half, SQ-META31-04); look in game.* `/locate ... meta/underwater_meteorite`: a sky stone sphere on the seabed
  in or by the large ruin, no crater, no air pocket.
- [ ] **ST-314 Camp strike [scratch]:** *PASS r1031 (log half, SQ-META31-04); look in game.* a few `meta/pillager_camp_meteorite`: some craters through the camp, some
  beside it; pillagers spawned on the crater edge not stuck.
- [ ] **ST-316 Meteor shower [scratch]:** *r1031: log half PASSED (SQ-META31-06: 2 meteorites); look client check pending.* `/locate ... meta/meteor_shower`: 2 to 4 craters in a sand desert, 20+ apart.
- [ ] **ST-317 Witch coven [scratch]:** *r1031: inconclusive, the row's check was wrong (SQ-META31-07, fixed row in judged_w2.md, not rerun). Spec conflict: `spawn_boxes.json` boxes `eugene:witch_wagon` in `witch_huts_full`, and parts in a meta keep their box, so wagons spawn witches against this test.* `/locate ... meta/witch_coven`: circle, a hut, 1 to 2 wagons; witches respawn in
  the hut at night (kill them, wait), none on the wagons or the circle's open ground outside its pieces.
- [ ] **ST-318 Sieged villages [scratch]:** *PASS r1031 (2026-10-31, SQ-META31-08); "none inside the village" look in game.* `/locate` `meta/village_siege_ring` (weapons all round the village, none in
  it) and `meta/village_siege_camp` (weapons grouped on one side, within about 40 of each other).
- [ ] **ST-319 Struck monument [scratch]:** *r1031: guardians stay at the monument PASSED (SQ-META31-09); meteorite 8 blocks off the monument centre (SQ-META31-06), sphere look and elder guardians in game.* `/locate ... meta/struck_monument`: a sky stone sphere in or against the
  monument; elder guardians present; guardians respawn in the monument, not in the open water of the meteorite's area
  away from it.
- [ ] **ST-320 Part spawns in metas [scratch]:** a pillager siege meta (`meta/pillager_tower_siege`): pillagers respawn in
  the tower and outworks (box pool); a `meta/ruin_cluster` with a Philip's Ruins part: its zombies respawn on that ruin
  only. Same with `[worldgen.systems] spawnBoxes = false`: the parts' own overrides still apply.
- [ ] **ST-315 Meteorite compass [game]:** an AE2 meteorite compass points at a meta's meteorite (its piece is AE2's own,
  so `CompassService` records it).

### 3.32 Spawners and spawn boxes: pillager and witch structures (added 2026-10-30, TODO `structures/contents #3`)

Brief: [briefs/structures_contents.md](briefs/structures_contents.md) #3 (vanilla mechanics, the 2026-10-30 audit).
Owner decisions 2026-10-30: every pillager spawner out; every pillager and witch structure gets a box with a mix by
structure class; metas keep their parts' boxes; box evokers drop no totem; the Mysticrift End tower gets no box (its
pillagers come later with gear, `mobs/pillagers #2`).

**Spawner filter** (patcherMod `TemplateSpawnerFilter`, from `StructureTemplateManagerSwapMixin` after
`TemplateBlockSwaps`). A template, jar or `kubejs/data`, loses every spawner whose `SpawnData` entity or any
`SpawnPotentials` entry is in CEConfig `[worldgen.systems] removeSpawners` (default `["minecraft:pillager"]`; empty
keeps all): the block becomes air. A template that loses one also loses its spawners with no mob (the End tower's 2).
One `[spawner-filter] <template>: N spawner(s) ... removed` line per template as it loads (17 templates, 22 spawners).
New chunks only.

**Spawn boxes** (patcherMod `StructureSpawnBoxes`, `ChunkGeneratorSpawnBoxesMixin` at the head of
`ChunkGenerator#getMobsAt`; data `kubejs/generation_scripts/worldgen/spawn_boxes.json`, read at world load, switch
`[worldgen.systems] spawnBoxes`). A box gives structures (ids, `#tags`, `ns:*`, minus `exclude`) a named pool; inside,
the pool replaces the biome's monster list and the structure's own `spawn_overrides`, as the vanilla outpost's does.
Code-defined structures (vanilla outpost, swamp hut, mansion, Takes a Pillage) need no JSON copy. First box naming a
structure wins (a second one logs a warning). `requires_piece` boxes a start only when one of its pieces came from a
listed template. Old chunks get boxes at once. Metas: see 3.31 "Spawn overrides inside a meta"; a meta's box gives its
pool to the parts that have a box of their own, each on its own pieces.

All pools use group size 1-1, so the weights are the per-mob shares:

| Box | Structures | Pool | Type |
|---|---|---|---|
| `meta_large` | metas `pillager_great_tower_siege`, `pillager_stronghold_siege` | large: pillager 35, ravager 5, vindicator / evoker / illusioner / witch 15 each | parts' own |
| `meta_tower_siege` | meta `pillager_tower_siege` | large_tower: large without ravagers (pillager 36.8, others 15.8) | parts' own |
| `meta_camps` | metas `pillager_camp_siege`, `_camp_ruin`, `_camp_ruins`, `_camp_meteorite` | camp_meta: pillager 60, vindicator / witch / evoker 13.3 each | parts' own |
| `meta_fleet` | meta `pillager_fleet` | boat | parts' own |
| `meta_caravan` | meta `pillager_caravan` | caravan: pillager / illusioner / vindicator 33.3 each | parts' own |
| `strongholds` | `#meta/pillager_strongholds` (T&T forts, Bastille) minus the T&T mushroom fields outpost (no box: no mobs spawn in mushroom fields, owner 2026-10-30) | large | full |
| `mansion` | `minecraft:mansion` | large | piece |
| `large_towers` | `#meta/pillager_large_towers` minus the End tower (7 Mysticrift towers) | large_tower | full |
| `towers` | `#meta/pillager_towers` (vanilla outpost + 14 T&T towers) | lone_camp: pillager 75, vindicator 20, witch 5 | full |
| `camps` | `#meta/pillager_camps` (4 T&T camps, Takes a Pillage camp, C.C.C. camp) | lone_camp | full |
| `boats` | `#meta/pillager_boats` (T&T ocean outpost, Blocky Siege ship, longship, warship) | boat: pillager 50, vindicator / evoker / witch 16.7 each | piece |
| `unwrecked_pillager_ships` | `unwrecked_ships:ship`, `nether_ship`, only with the pillager layout or pillager sailors | boat | piece |
| `vehicles` | `#meta/siege_weapons`, `#meta/caravan_carriages` | vehicle: pillager 50, vindicator 50 | full |
| `witch_huts_full` | `minecraft:swamp_hut`, `eugene:witch_wagon` | witch | full |
| `witch_huts` | YUNG witch hut and circle, Villages & Pillages witch, Terralith underground witch hut | witch | piece |

Metas without a box (witch coven, witch cart caravan, the two sieged-village metas) let each part use its own: the
siege weapons around a sieged village spawn the vehicle mix, the village its biome's mobs.

**Box evokers drop no totem.** `no_drops` in the same file: a mob of a listed type that spawns naturally inside a box
is marked (`LivingSpawnEvent.SpecialSpawn`, persistent data `creativeengineering:box_spawn`) and its listed drops are
removed on death (`LivingDropsEvent`). Mansion evokers placed at generation and raid evokers keep their totem.

**Limits.** Only pillagers spawn by day (their spawn rule ignores sky light); vindicators, evokers, illusioners,
ravagers and witches need darkness, so a mixed box makes fewer mobs by day (a pick that fails the light check is a lost
attempt). Box mobs count toward the monster cap and despawn like any monster. 6% of box pillagers are captains (Bad
Omen). Takes a Pillage's own illagers (archer, legioner, skirmisher) are not in any pool. Structures already generated
keep their spawners.

**Box spawn height** (added 2026-11-02, TODO `structures/contents #6`; patcherMod `StructureSpawnBoxes.boxHeight`,
`NaturalSpawnerBoxHeightMixin` at the return of `NaturalSpawner#getRandomPosWithin`). Vanilla starts each spawn attempt
at a random height from the world bottom (y -64) to the column top, so over a 22-high Mysticrift tower about 1 attempt
in 8 lands in the tower's height range and fewer than 1 in 20 on one of its floors; the rest fall in the ground and
caves under it, where the biome's monsters spawn. In a column a box of that category covers (the piece boxes over the
column for `piece`, the box around all pieces for `full`, a meta's box parts as in 3.31), half the attempts now start
at a random height between the box's bottom and the column top. The other half, every unboxed column and every
category without a box keep vanilla's height. Caps (no budget of their own, owner 2026-11-02), light and placement
rules, the 24-block player distance and the box lookup are unchanged: the attempt only starts where the box mobs can
stand. Applies to every box, the vanilla outpost included (`towers`).

**Probe: `/cwgspawnbox [pos] [category]`** (added 2026-11-02, op level 2; default: the executor's block, `monster`).
Prints and logs one `[spawn-boxes]` line: the box that answers a natural spawn there (`box <id> of <structure>`, inside
a meta `box <meta box> (part box <part's box>) of <meta>, part <part>`, or `spawn_overrides` of a meta part) with its
pool as `type weight (min-max)`, or `no box (structures referenced here: ...) -> vanilla [...]` with the list vanilla
would use (a structure's own `spawn_overrides`, else the biome's). It asks the same lookup the spawner does, so a row can
check the box resolution without waiting on random spawns. Code: `StructureSpawnBoxes.lookup` / `probe`.

**Pre-scratch findings (2026-11-02, `mobs/pillagers #6`, `structures/contents #6`).** Static check of
`spawn_boxes.json` against the tags: every siege, camp, ship and witch structure resolves to the box in the table above,
no overlap (no `first wins` warning in the r1031 or r1101 logs; 67 structures boxed); inside `meta/pillager_tower_siege`
the anchor tower (`towers`), the camps (`camps`) and the weapons (`vehicles`) all take the meta's `large_tower` pool in
`inMeta`. Code reading finds no path that hands a part its own pool while the meta has a box. The r1101 SQ-SB-04 counts
(2 pillagers, 3 vindicators, no evoker by the anchor of the meta at chunk [-24, 2], whose parts were the T&T forest
tower at -375 91 44, a C.C.C. camp at -333 101 36 and a catapult at -375 72 20) are unlikely under `large_tower` (about
2% for 3 dark-spawned picks with no evoker, illusioner or witch) but come from one 240 s window with qa within 24 blocks
of the camp, and the SQ-SB-03 lone birch tower spawned no illager while zombies did. Not settled; the next run probes
first (`/cwgspawnbox` inside each part) and counts mobs only after that, with patrols off and the area cleared. Spawn
facts that thin box mobs: the spawner picks a height from the world bottom to the column top, so most attempts over a
tower land below it in caves (biome mobs); every box monster but the pillager needs block light 0, so lit tower
interiors only spawn pillagers; box mobs share the monster cap with cave mobs; nothing spawns within 24 blocks of a
player. Owner 2026-11-02: no cap change and no budget outside the cap;
make the attempts reach the box instead.

**Cause found (2026-11-02, `structures/contents #6`).** Not the box lookup and not the spawn rules: the birch tower
(`birch_pillager_tower_new`, 15 x 22 x 15, `full` box, base y 81 at [-2016, 6672]) has standing room on most of its levels
(ground yard 92 spots, floors at +4..+6, +10, the balcony at +14 with 95, the roof at +20/+21), dark except 2 lanterns
at +7 (pillagers need block light 8 or less there, the others 0); `PatrollingMonster.checkPatrollingMonsterSpawnRules`
(1.19.2 bytecode: block light > 8 refuses, then `checkAnyLightMonsterSpawnRules`) passes on every floor away from the
lanterns. What starves it is the attempt height: y uniform over -64..103 gives about 1 in 20 of the tower-column
attempts a floor level, and with the monster cap full of cave mobs only freed slots spawn anything, shared by about
289 chunks: a few hundred spawns in 300 s, of which the tower's 1-2 chunks win well under 1 on a floor. Template
pillagers (8 in the NBT) despawn at random beyond 32 blocks (qa stood 31-45 off). Fix: box spawn height (above).

**Tests** (scratch queue SQ-SB; r1031 2026-10-31 ran SB-01 to -03, SB-04 to -08 not run):
- **ST-320 Spawners gone:** *r1031 (SQ-SB-02): the log shows the spawners removed from the catapult, the Mysticrift oak and End towers and the Blocky Siege ship; the row's check was wrong, fixed row not rerun.* new world; `/cdevplace` a catapult, a Mysticrift oak tower, the Blocky Siege ship and the End
  tower -> no spawner where the templates had them; `[spawner-filter]` lines for each; `removeSpawners = []` brings
  them back in new chunks.
- **ST-321 Boxes load:** *PASS r1031 (2026-10-31, SQ-SB-01, -01b).* boot log `[spawn-boxes] 15 box(es)`, then `N structure(s) boxed` on the first spawn check, no
  `is in box ... and in box` warning.
- **ST-322 Large tower mix:** *FAIL r1031 (2026-10-31, SQ-SB-03): no illager at a lone birch tower by night or day while zombies and a creeper spawned. Cause: attempt heights from the world bottom (see Cause found); box spawn height built 2026-11-02, rerun SQ-PSM-03.* a Mysticrift tower at night on Normal -> pillagers, vindicators, evokers, illusioners,
  witches; no ravager, no zombie inside the box. By day only pillagers.
- **ST-323 Box inside a meta:** `/locate` a `meta/pillager_tower_siege`; by the anchor tower and by a siege weapon ->
  both spawn the large-tower mix (no ravagers); a lone T&T tower spawns the camp mix.
- **ST-324 Ruin inside a camp meta:** a `meta/pillager_camp_ruin` -> the camp spawns the camp-meta mix, the ruin its
  own Philip's Ruins mobs.
- **ST-325 Box evoker totem:** kill an evoker that spawned in a fort box -> no totem; a mansion evoker from generation
  -> totem.
- **ST-326 Unwrecked crew:** an Unwrecked ship with the pillager layout spawns the boat mix on deck; a villager-crew
  ship spawns biome mobs.
- **ST-327 Witch boxes:** a witch wagon and a Terralith underground witch hut at night -> witches only.

### 3.34 Quark spiral spires: no base over the void (added 2026-11-02, TODO `structures/contents #5`)

Quark's Spiral Spires (End outer islands past 1050 blocks, `[world.spiral_spires]` rarity 200, radius 15) are a
`MultiChunkFeatureGenerator`, not a structure: every chunk within the radius of a source rolls a column, walks down
from y 256 to the first end stone and builds a spike there (`SpiralSpireGenerator.makeSpike`, decompiled 3.4-418).
Quark only checks that the spike's own column is clear of other blocks; it never looks at the ground around it, so at
an island edge the base (a disc of radius 7 at the surface, plus up to 5 layers below where the centre is solid)
hangs over the void. Quark has no config switch for it.

patcherMod `QuarkSpiralSpireFootprintMixin` (`makeSpike` HEAD) cancels a spike when any block under its base disc
(i * i + j * j <= 49, one block below the surface) is air. The disc radius is 7 for every height and composition
Quark rolls, so the check draws no random and the other spikes of the cluster are unchanged; only spikes on solid
ground remain. No toggle. Test: ST-335.

- [ ] **ST-335 Spires on solid ground [scratch]:** End, fly the outer islands past 1050 blocks until a spire cluster
  (myalite crystal spirals on dusky myalite spikes); look under every spike from the side → each base sits on end
  stone all round, none hangs over the void; spikes still generate inland (a cluster has several).

## 4. Limits and decisions

- **Only structure_set documents are written, never the structure.** Frequency, `locate_offset` and on/off all live
  on the small set document (plus patcherMod's frequency table). Rewriting a structure (biomes, jigsaw pools, size)
  risks breaking jigsaw schemas, and vanilla structures have no json to start from. Where a structure spawns is changed
  through biome fits (3.17) instead (until 2026-10-11 also through biome-tag merges, `biome_merges.json`, now retired).
- **Groups guarantee a start-to-start gap, not an edge gap.** A village can reach up to 80 blocks from its start, so two
  large settlements 176 blocks apart can still nearly touch. Raise the group's separation (and spacing with it) for more.
- **Gaps between different groups** exist only where a group has an `exclusion_zone` (vanilla: one other set, at most 16
  chunks). It checks the other group's grid slots, not real structures and not biomes, so an empty or wrong-biome slot
  still repels. `pillager_settlements` uses 4 chunks against `settlements`; keep such radii small.
- **Frequency needs patcherMod.** Without `StructureFrequencies` the script applies nothing (see 3.1). The table is
  frozen per world load; `/reload` can't change it.
- **Why a fitted migration:** the old model tuned every set on its own grid (and split sets whose members had different
  multipliers), so structures of one kind piled up independently. Merging them onto one grid with plain weights would
  have changed many rates (up to x0.18 in the dry runs). The migration measured each structure's old density per biome
  and fitted weights and frequencies to keep it, and each group's grid is sized for its most crowded biome, so nothing is
  capped: every structure ended within x0.89 to x1.13. Lower `QUANTILE` in the tool to cap crowded biomes instead.
- **Moving structures:** `gen_structure_groups.py` fits a moved or new member to the rate the file gives it now (moved)
  or its mod default (new), holding every other member fixed. Members that share biomes with it lose a little share; the
  estimates report shows it.
- **Rejected (2026-09-27): distance rules with a structure predictor** (minimum edge distances between arbitrary kinds,
  clustering by thinning). It needed higher base frequencies to thin from; the user didn't want the working rates raised.
  Shared grids give consistent spacing with no new code in the generation path beyond the frequency roll.
- **Meta structures are one structure start** (3.16). Each chunk only looks 8 chunks around itself for starts that
  reach into it, so every part stays within 7 chunks of the start chunk (one chunk under, for the beard margin):
  satellites 40 to 104 blocks from a village fit, a fort with whole villages around it does not. Separate starts per
  satellite were rejected: every chunk would have to predict whether a meta anchor won a group slot nearby, and the
  weighted pick with biome fall-through can't be predicted cheaply (see Claude's memory `structure-spacing-feasibility`).
- **Meta parts check their own biome list.** The four peaceful towers come from MCreator mods that list vanilla biomes
  only, so village-with-towers stood in 8 vanilla biomes (about 11% of the Overworld) until the biome fits (3.17) gave
  them 20 to 42 biomes: now 52 biomes, 16% of the Overworld in Terralith ones. Rejected: letting towers use the
  village's biome check (a desert tower beside a snowy village).
- **Biome fits replace a structure's list, through `Structure#biomes()`, not its json.** The rule in the first bullet
  still holds for files: no structure json is rewritten. Fits are frozen per world load like the frequencies; a
  `/reload` that changes the biome tags doesn't move them. Forge 43's structure modifiers were the first idea, but
  nothing in 1.19.2 reads their modified biomes.
- **A meta anchor keeps the village's rate** (it rolls the anchor's frequency and picks by the anchors' group weights),
  so adding a meta never adds villages. `/locate` can't know the anchor before the start is built, so it only rolls
  the meta's own frequency (1), and it may name a slot that then holds another village; a search moves on to the next.
- **Earth fill sees a meta as a whole** (its own limits, the start chunk's terrain sample), not per part.
- **Meta definitions are staged, not files in `kubejs/data`,** so an older patcherMod jar (no `creativeengineering:meta`
  type) can't fail the world load: the applier leaves them out instead. The old plan had them in
  `kubejs/data/creativeengineering/worldgen/structure/meta/`.
- **Placed features aren't covered.** Monster-room dungeons (Terralith's json: 10 + 4 attempts per chunk), fossils
  (1 in 64 chunks), desert wells (1 in 1000), YUNG's Extras' ~60 desert/swamp features, Immersive Weathering's desert
  fossil, Supplementaries' road signs and Quark's monster boxes are features, not structures. Not built.
- **Blocked structures:** a structure another mod stops in code can't be brought back or tuned from here (3.4). The
  `blocked` notes come from the scanner, so re-run it after changing one of those YUNG's options.
- **AE2 meteorites and `generateMeteorites`:** with it false, AE2 doesn't register the set, and an override written
  from this config would add it back, and with the meteorite in `group/sky` a missing structure fails world load. To
  remove meteorites, set `enabled: false` under `groups.sky.structures.ae2:meteorite` and leave AE2's option alone.
- **Pool weights are relative.** A village still gets the same number of houses. Removing every piece leaves a lone
  `minecraft:empty`, so a start pool like that generates nothing: remove whole structures with `enabled: false` instead.
- **New chunks only, rejoin to apply.** `/reload` doesn't reload worldgen.
- **Removing a structure mod:** `python3 tools/gen_structure_config.py`, delete that mod's *explicit* ids from
  `structure_tags.json` (an unknown explicit id aborts all tuning; an `ns:*` glob only warns), then
  `python3 tools/gen_structure_groups.py`. Entries left in `structure_groups.json` for a structure that no longer exists
  are errors, so remove them too.
- **Removed sets and structure searches.** Vanilla 1.19.2's structure search (explorer maps, `/locate`, cartographer
  trades, dolphins) ignores a set's `frequency`, so searching a set at frequency 0 scanned every candidate chunk out to
  the radius (~10k generation checks) and hung the tick (Monobank vault map, `debug/crash-2026-09-27_15.32.34-server.txt`).
  Emptied sets now have no members at all, so searches never visit them; `ChunkGeneratorLocateMixin` still drops any
  frequency-0 placement, and `loot/06_dead_maps.js` strips explorer maps aimed at removed structures from the 5 Monobank
  vault tables. When disabling another structure, grep the jars for `exploration_map` and add any table that targets it.
- **Rejected:** tuning through each mod's own config (T&T, Philip's Ruins, YUNG's). Few mods have one, the formats
  differ, and vanilla has none. Mod options that *block* things are kept, and flagged.
- **Rejected:** loot-theme tags ("rich", "poor"). Categories are by what a structure is (house, tower, camp...).
- **Ground matching decides per chunk, not per structure.** A structure straddling a biome border can end up grassy on
  one side and matched on the other, just as the terrain under it changes. Chunks more than one away can't be read while
  a chunk generates, so a whole-structure decision isn't possible there.
- **"Base" means the bottom `baseLayers` of each template.** Terrain-matching pieces (village roads, dropped onto the
  heightmap column by column) don't have a fixed bottom, so their grass may or may not count. Family blocks higher up
  (roof gardens, planters) are never touched. `dirt_path` and farmland aren't in the family: they're part of the design.
- **The density multiplier only changes structure sets.** Village house weights, biome shares and exclusion-zone radii stay
  as they are. Above x1 a set's separation shrinks too (so the grid can get denser), which means group gaps are smaller
  while it's on. It's meant for testing ("do these spawn at all, and where"), not for a play world.
- **`/cdevplace` is not natural generation.** No frequency roll, no biome check, no terrain adaptation. Some structures turn
  a flat world down and are listed as failed: their own generator refuses the height or needs water or room (a
  mansion wants ground at y 60 or above, for example). Moving a generated structure sideways is exact only on flat
  ground; in a normal world it keeps the height of wherever the row cursor was.
- **Rejected for grass bases:** dropping structures from biomes (loses them where they'd fit with a matched base), and
  pre-built per-biome copies of templates (one copy per structure x biome, and it misses mixed or modded ground).

## 5. File map

- Configs: [kubejs/generation_scripts/worldgen/](../kubejs/generation_scripts/worldgen/), whose README has the full workflow
- Applier: [structure_overrides.js](../kubejs/server_scripts/worldgen/structure_overrides.js) (`resolveTags`, `validatePlacement`, `memberSettings`, `plan`, `buildReport`, the `highPriorityData` handler at the end)
- Groups tool: [tools/gen_structure_groups.py](../tools/gen_structure_groups.py) (`GROUP_SPECS` = the starting groups, `fit_group`, `sync`, `migrate`, `write_estimates`)
- Scanner: [tools/gen_structure_config.py](../tools/gen_structure_config.py) (`VANILLA_STRUCTURE_SETS`, `CODE_REGISTERED_SETS`, `CODE_BLOCKS`, `scan_meta_structures`)
- patcherMod: [worldgen/StructureFrequencies.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/StructureFrequencies.java) + mixins [ChunkGeneratorStructureFrequencyMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/ChunkGeneratorStructureFrequencyMixin.java), [StructureCheckFrequencyMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/StructureCheckFrequencyMixin.java); [worldgen/PoolTuner.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/PoolTuner.java), [LootContainerSwaps.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/LootContainerSwaps.java), [WorldgenFiles.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/WorldgenFiles.java) (report writer; KubeJS blocks java.io); mixins [StructureTemplateLootSwapMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/StructureTemplateLootSwapMixin.java), [KubeJSVirtualDataPackListMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/KubeJSVirtualDataPackListMixin.java), [ChunkGeneratorLocateMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/ChunkGeneratorLocateMixin.java) + [DeadPlacementFilter.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/DeadPlacementFilter.java)
- Earth fill: [worldgen/EarthFill.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/EarthFill.java) (`countEarth`, `beardOf`, `sizeOf`, `earthLimitsFor`, `count`, `pieceEarth`, `localEarth`, `pieceWater`, `localWater`, `predicted`, `topIsWater`, `waterLimitsFor`, `filter`) + mixin [ChunkGeneratorEarthFillMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/ChunkGeneratorEarthFillMixin.java), knobs `CEConfig.EarthFill`, earth tag in [blocktags.js](../kubejs/server_scripts/tags/blocktags.js), exempt structures [worldgen/earth_fill.js](../kubejs/server_scripts/worldgen/earth_fill.js); water structures and ponds [worldgen/water_fill.js](../kubejs/server_scripts/worldgen/water_fill.js), knobs `CEConfig.WaterFill`; tests `EarthFillTest` and the calibration [tools/earth_fill_sim.sh](../tools/earth_fill_sim.sh) (`EarthFillSim`; `beard` mode measures the beard reach)
- Terrain adaptation candidates: [tools/terrain_adaptation_scan.py](../tools/terrain_adaptation_scan.py) → [terrain_adaptation_scan.md](terrain_adaptation_scan.md) + `kubejs/generation_scripts/worldgen/terrain_adaptation_scan.json` (cache `tools/.cache/terrain_scan.json`; `/cdevgen run terrain_adaptation_scan`)
- Terrain adaptation + template trim: [worldgen/TerrainAdaptation.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/TerrainAdaptation.java) (`Mode`, `rebuild`, `override`, `rigid`, `softBox`, `SoftBox`, `beard`, `field`, `addSoft`), [worldgen/TemplateTrim.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/TemplateTrim.java) (`kind`, `columns`, `trim`, `apply`), mixins [StructureTerrainAdaptationMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/StructureTerrainAdaptationMixin.java), [BeardifierSoftMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/BeardifierSoftMixin.java) and the trim call in [StructureTemplateGroundMatchMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/StructureTemplateGroundMatchMixin.java); knobs `CEConfig.TerrainAdaptation`; structure tags [worldgen/terrain_adaptation.js](../kubejs/server_scripts/worldgen/terrain_adaptation.js), block tags `template_trim/*` in [blocktags.js](../kubejs/server_scripts/tags/blocktags.js); test `TerrainAdaptationTest`
- Banks and driftwood: tags `monobank/vault_houses` / `monobank/vault_streets` in [pool_tags.json](../kubejs/generation_scripts/worldgen/pool_tags.json); [driftwood placed features](../kubejs/data/upgrade_aquatic/worldgen/placed_feature/) and [land driftwood](../kubejs/data/creativeengineering/worldgen/placed_feature/driftwood_land/) with their [biome modifiers](../kubejs/data/creativeengineering/forge/biome_modifier/)
- Meta structures: [meta_structures.json](../kubejs/generation_scripts/worldgen/meta_structures.json); [worldgen/MetaStructure.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/MetaStructure.java) (`Part`, `strict`, `findGenerationPoint`, `placeStructures`, `placeFeatures`, `generate`, `Layout`, `Index`, `structureAt`, `structureOf`, `afterPlace`), [worldgen/MetaFeaturePiece.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/MetaFeaturePiece.java), the anchor roll in `StructureFrequencies#anchorChosen`, part lookups in `TemplateTrim#apply`, `GroundMatch#apply`, `TerrainAdaptation#rigid`; applier: `metaSupport`, `metaProblems`, the `## Meta structures` report section and the `ServerEvents.tags` handler in [structure_overrides.js](../kubejs/server_scripts/worldgen/structure_overrides.js); placed feature [meta/flotilla_driftwood](../kubejs/data/creativeengineering/worldgen/placed_feature/meta/flotilla_driftwood.json); test `MetaTest`
- Biome fits: [biome_fits.json](../kubejs/generation_scripts/worldgen/biome_fits.json) + the `tower`/`tower/*` tags in `structure_tags.json`; [worldgen/StructureBiomeFits.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/StructureBiomeFits.java) (`stage`, `activate`, `fitEntry`, `resolve`, `biomesFor`, `FitBiomes`) + mixin [StructureBiomesMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/StructureBiomesMixin.java); applier: `planBiomeFits`, `fitChain`, `fitStage`, the `## Biome fits` report section and both `ServerEvents.tags` handlers in [structure_overrides.js](../kubejs/server_scripts/worldgen/structure_overrides.js); estimates: `Pack.apply_biome_fits` and `fit_rates` (`--fit-rates`) in [tools/gen_structure_groups.py](../tools/gen_structure_groups.py); test `FitTest`
- Dead explorer maps: [loot/06_dead_maps.js](../kubejs/server_scripts/loot/06_dead_maps.js)
- Worldgen config: `CEConfig.Worldgen` (+ `WorldgenSystems`, `WorldgenDebug`, `PoolTuning`, `BiomeSolver`, `GroundMatch`, `MassPlace`), read through [worldgen/WorldgenConfig.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/WorldgenConfig.java); density in `scalePlacement` in [structure_overrides.js](../kubejs/server_scripts/worldgen/structure_overrides.js)
- `/cdevplace`: [worldgen/MassPlacer.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/MassPlacer.java) (`select`, `rowsFor`, `templateRows`, `signLines`, `verticalShift`, `placeStructure`, `placeTemplate`)
- Ground matching: [worldgen/GroundMatch.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/GroundMatch.java) (`sample`, `palette`, `rewrite`, `apply`), mixins [StructureTemplateGroundMatchMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/StructureTemplateGroundMatchMixin.java) + [StructureStartGroundMatchMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/StructureStartGroundMatchMixin.java), knobs `CEConfig.GroundMatch`, block tags at the end of [blocktags.js](../kubejs/server_scripts/tags/blocktags.js), exempt structures [worldgen/ground_match.js](../kubejs/server_scripts/worldgen/ground_match.js)
- Worldgen tool tests: [tools/test_worldgen_tools.sh](../tools/test_worldgen_tools.sh) ([tools/worldgen_test/](../tools/worldgen_test/): `GroundMatchTest`, `MassPlacerTest`; [tools/structure_density_check.js](../tools/structure_density_check.js)); the Java test classpath shared with `test_structure_groups.sh` is [tools/java_test_cp.sh](../tools/java_test_cp.sh)
- Dump commands: `/dumpstructuregen` [dump_structuregen.js](../kubejs/server_scripts/_lib/dump_structuregen.js) → `kubejs/exported/worldgen/structuregen.json`; `/dumpstructures` [structure_dump.js](../kubejs/server_scripts/_lib/structure_dump.js) (names only)
- Offline tools: [tools/structure_tuning_sim.js](../tools/structure_tuning_sim.js), [tools/test_structure_groups.sh](../tools/test_structure_groups.sh) (+ [tools/structure_groups_test/](../tools/structure_groups_test/)), [tools/check_structure_live.py](../tools/check_structure_live.py), [tools/audit_loot_containers.py](../tools/audit_loot_containers.py)
- Archive of the old model (config with `current`, tags with multipliers, old applier, old report): `kubejs/archive/worldgen/pre_groups_2026-09-27/`
- Converted structure mods: `kubejs/data/<modid>/`, made by [tools/port_structure_jars.py](../tools/port_structure_jars.py) (3.11)
- Old node generators (deleted 2026-10-22 from `kubejs/archive/worldgen/`; git history has them)

## 6. Offline checks

- **`tools/test_structure_groups.sh`** (about 20 s): the node simulation; the same script in the pack's own Rhino, whose
  96 set files, frequency table and report must match node's byte for byte; the old-jar path (must apply nothing); and
  13 headless Java checks of `StructureFrequencies` (roll uniform and independent, a 0.25 member keeps 25% of its slots,
  the veto stops only the rest of its own set in its own chunk, `/locate`'s prediction matches the roll 2000/2000). All
  passed 2026-09-27. Run `./gradlew build` in `patcherMod` first.
- **Before rejoining after an edit:** `node tools/structure_tuning_sim.js [report.md] [out/]` prints the line the game will
  log (currently `9 groups, 96 structure_sets written (64 emptied into groups), 117 original sets; 109 structure
  frequencies below 1`), or the errors that would make it apply nothing.
- **Which structures are still limited to a subset of biomes:** `python3 tools/structure_biome_coverage.py [--all]
  [--json OUT]` (every land structure: share of the land its biome list covers, Terralith count, group, fitted or not,
  and what it is built from; 7.2).
- **Expected rates:** `python3 tools/gen_structure_groups.py --check` rewrites `structure_group_estimates.md` (every
  structure: mod default vs now, per biome area). `--migration-report` rebuilds `structure_group_migration.md` (now vs the
  archived old model: 164 structures with a rate, x0.89 to x1.13, 135 within 5%).
- **After adding or removing a structure mod:** `python3 tools/gen_structure_config.py`, then
  `python3 tools/gen_structure_groups.py` (entries for new structures). New namespaces need a `mod/*` tag and a `type/*`
  entry, and a group tag if they belong in one; the sim's Coverage lines list gaps.
- **After a world load:** `/dumpstructuregen` in game, then `python3 tools/check_structure_live.py`. Sections 1 and 2
  should be empty and section 4 should say `0 mismatch(es)` over 96 sets (spacing, separation, count and members; the
  frequencies are confirmed by the `[structure-frequency]` log line instead). On the pre-groups dump it flags 76 sets,
  as it should.
- In game, `/cdevgen run worldgen` runs `gen_structure_config.py`, `gen_biome_config.py` and `gen_structure_groups.py`; `/cdevgen run structure_live_check dumps` does `/dumpstructuregen` + `check_structure_live.py`.
- **Loot containers after mod changes:** `python3 tools/audit_loot_containers.py` rewrites `loot_container_audit.md`.
- **`tools/test_worldgen_tools.sh`** (about 30 s, after `./gradlew build`), all passed 2026-09-27 (earth fill added and passed 2026-10-03):
  1. 37 headless checks of `GroundMatch` against real block states and bound tags. They cover what counts as ground,
     the native/foreign decision (volcanic vs plains sample, the 35% threshold, too few columns), weighted picks, and a
     5x5 grass-pad ruin through all three modes: 50 base blocks swapped, poppy + tall grass dropped, exposed vs covered
     palettes, the roof garden and the next chunk left alone, plus `maxPlantHeight`.
  2. 30 checks of `/cdevplace` and `WorldgenConfig`: sign wrapping, every filter term, rows on the real
     `structure_tags.json` (each Towns & Towers structure in exactly one row, always its smallest type tag), template
     folders, vertical alignment, and every config default equal to the old hard-coded value.
  3. `EarthFillTest`: the counting core (limits -1 and 0, early stop, dedupe), the earth blocks of a real template
     through rotation, rigid / list / terrain-matching pool elements, a whole pool-element piece, override parsing; water fill:
     which blocks hold water (water, waterlogged, kelp, seagrass, all layers), a piece's water on a shore (sea / seabed / dry
     ground / air), the ground and air limits, seabed as solid for earth fill, and the column-top seabed test. Since
     2026-10-13 (48 checks): earth after the beard on a slope and a river (no beard / beard / bury, upper layers judged
     by the gap under the piece), mound columns counted once, the size bounds, the size limits, 3-number overrides.
  4. `node tools/structure_density_check.js`: runs the simulation at x4, x0.25, x30 and x0.01 and checks every live set's
     rate against x1: 244 checks, exact except the reported capped sets. It also checks codec limits and `legacy_type_1`.
  5. `TerrainAdaptationTest` (50 checks, added 2026-10-09): block kinds; template columns and the trim on a pad-and-hut
     test template (50 air, 26 pad, 2 plants dropped; the hut and the grass under it kept; a pad-only plate untouched;
     structure blocks kept); earth fill's trimmed count; the real ballista, circus tent and forest ruin 3 templates
     against a python read; tag precedence; config defaults; distance fields (Euclidean, reach 12, rotation); the soft
     beard against vanilla's `getBeardContribution` (exact formula, and skirt 8 on a box = beard_thin around the box),
     skirt scaling and cutoffs, footprint vs box; `addSoft` adding only soft rigids and rewinding the iterator.
- **Terrain adaptation candidates:** `python3 tools/terrain_adaptation_scan.py [--fresh]` after adding or updating
  structure mods (or after a `/dumptags`, since pad and plants come from the tag export). Its template numbers agree
  with `TerrainAdaptationTest` (ballista 56 built / 35 air-only columns / 210 air; circus tent 264 pad / 6,072 air).
- **Meta structures** (2026-10-11): `tools/test_worldgen_tools.sh` step 6 runs `MetaTest` (26 checks): both real
  definitions parse with the game's codec, bad input is rejected (both or neither of structures/features, distance past
  the reach, count [3, 1], a bad tag, chance 1.5), the anchor roll equals the anchor's own roll 20000/20000 and a miss
  vetoes the slot, reach and per-piece clearance. `test_structure_groups.sh` compares the staged meta structure files and
  the `creativeengineering:meta/*` tags between node and Rhino, and checks that without MetaStructure no meta is staged
  or listed in a set. The node sim prints `meta structure file:` and `structure tag` lines (`--no-meta` = an old jar).
- `test_structure_groups.sh` step 2 also compares node and Rhino at density x4 and x0.3 and with groups off (identical).
  The sim takes `--density=X` and `--groups-off`.
- **Biome fits** (2026-10-11, `land/*` 2026-10-12): `tools/test_worldgen_tools.sh` step 7 runs the node sim and feeds its staged `fits.json`
  to `FitTest` (30 checks): the real fits parse in Java (163 structures, 29 fits, the oak tower's chain is
  `land/oak` + `land`, the End tower only `land/end`, `land` empty, the vanilla outpost on `land/dark_forest`, the plains
  village on two families, the sky village's list has the oceans, Philip's nether ruin unfitted, no structure gets a
  sibling fit), bad input stages nothing,
  fits join in order without duplicates, an empty fit still applies (never spawns), and the fitted list matches by biome
  value (another holder of a listed biome is in, an unbound holder is out). `test_structure_groups.sh` compares the
  staged fits and the fit biome tags between node and Rhino. The sim prints `biome fit` and `biome tag` lines.

- **Scratch-run fixes** (2026-10-18, 3.23): `tools/test_worldgen_tools.sh` step 10 runs `PoolTunerTest` (10 checks: the
  canonical order by key then weight, the same list from two input orders; a piece made in code fails to save under a
  copied registry set, is rebound to the copy's `minecraft:empty`, then saves; a second rebind changes nothing).
  `GroundMatchTest` +4 (the column exclusion by height: a deep piece, a piece standing on the ground, the 1-block XZ
  margin), `EarthFillTest` +2 (stone base counts with the stone tag; the plain list is unchanged after it), `FitTest`
  +1 (31 fits; the skeleton dungeon's chain `land/any/deep` + `land/any` + `land`). Every step passed 2026-10-18 against
  the mapped 1.19.2 Forge jar, with stubs for the main mod class and the Async Locator bridge (this container has no
  third-party jars). The 8 driftwood placement lists decode with the 1.19.2 `PlacementModifier` codec.

- `python3 tools/wood_mismatch_scan.py` -> `feature_writeup/wood_mismatch.md`: per live land structure, its woods,
  the share foreign to its biomes (wood fits as the native woods) and a verdict (unchanged / trim / woodless /
  restrict / swap), by family (2026-10-12).
- `python3 tools/nether_blocks_scan.py` -> `feature_writeup/nether_blocks_scan.md`: Nether blocks (3 tiers) in every
  Overworld structure's templates, per structure, per block and per template (2026-10-12).

## 7. Planned changes

Everything planned for structures, split into what is left and what is built. Status words as elsewhere: BUILT = in
the files / jar, not checked in game; DEPLOYED = shipped.

**Release state (2026-10-14):** everything under Done, including 7.1 to 7.4, ships in pack 1.0.6 marked INCOMPLETE:
built and in the payload, not fully tested. Fixes from the rest of the 8.0 run go into 1.0.7. Section numbers point at the reference.

### Todo

**Structure rates and groups**
- ~~**Rare structures hard to test:** loosen soft rules only (frequency, weight, share, spacing, separation, debug
  density), never correctness rules (fits, biome rules, peaceful radius, priority, earth/water fill); 7.4.8.~~ BUILT
  2026-10-30: `structure_test_overrides.json` + `[worldgen.debug] testOverrides` (3.27).
- **Nether, End and underground grouping pass** (pending from 7.1; still standalone):
  - Nether: vanilla fortress + bastion (`minecraft:nether_complexes`), YUNG's fortress, the vanilla Nether fossil,
    Hearths' towers and netherrack spiral, Philip's lost soul city, Farmer's Structures' two Nether cooks, the
    Unwrecked Ships Nether ship, YUNG's small Nether dungeon (off in its config).
  - End: vanilla end city, Nullscape's dragon skeleton, Philip's End ruins + end gate fortress, the 16 MES sets.
  - Underground: vanilla mineshafts, ancient city and strongholds, YUNG's stronghold, YUNG's skeleton and small
    dungeons, Philip's crypts, ancient dungeon and underground set, Terralith's underground set and frosted dungeon.
- **Cave-structure generation tuning** (marked 2026-10-13, see 7.3.3): deep structures need cave biomes in their
  fits. `betterdungeons:skeleton_dungeon`, `philipsruins:bone_dungeon` and `philipsruins:lost_soul_city` don't
  generate at all since 7.2 (`land/any` has no cave biomes); decide their biomes, group and rates with the
  underground pass above. Retest ST-115.
- **Sky village rate:** lower it if it is still too common once seen in game (it is in `sky` and spawns over land and
  sea, see Done); settlements over water could get a boost.
- **Fresh watchtower:** `fit_keep` is set; rerun `--fit-rates` to raise it back from 1 per 83 (`structure_group_estimates.md` of 2026-10-14; 75 at the 3.20 fold) to its old 1 per 67
  (the run re-solves every fitted structure, so check the diff).
- ~~**Pyramids** (scratchpad 2026-10-21): double their minimum separation.~~ BUILT 2026-10-23 for the desert pair (3.24);
  the jungle temple distance stays with TODO `structures/rates-and-groups #2`.
- **Lone waystones** (scratchpad 2026-10-21): too many, deserts worst; lower the rate, or drop the lone ones and add
  waystones to existing structures. TODO `structures/rates-and-groups #11`.

- ~~strip all structure weights unless an explicit 'increase or decrease' note or change was made. relative frequencies aren't static now~~
  BUILT 2026-10-30: `tuned` field + `--strip-weights`, weights stripped, frequencies kept (3.27).

**Structure contents**
- **Move to the Nether**: BUILT 2026-11-02, see Done (`structures/contents #1`).
- advanced periph structures are disabled by config, disable warning/remove structure directly 
- ~~swap river tree to oak log, remove river log/wood~~ (done 2026-10-22, [recipes_wood.md](recipes_wood.md) 2.6), swap driftwood craft to oak planks, remove driftwood/logs

**Biomes and terrain**
- Biome tuning: swap Overworld netherrack and plain crimson/warped ground for other blocks. (quark jasper, create crimsite)
  Scanned 2026-10-19 with every other Nether block left in Overworld generation: decision sheet in
  [nether_worldgen.md](nether_worldgen.md) section 2.
- Chunk generation: leaving out the bottom 64 to 96 blocks: investigated in [locate.md](locate.md) 3.7 (measure
  first; skip the band in Distant Horizons' own generation; a real deferred band is not recommended). The DH clip
  (option A, `worldgen/DistantClip`, `[worldgen.distant]`) is built and shipped since 1.0.6 and measured (LO-10);
  only LO-11 [game] is left.
- Terrain adaptation leftovers (3.15, `terrain_adaptation_scan.md`): batch 4 built 2026-10-30 (3.30; villages and
  the undead castle still open, look in game first); calibrate the earth-fill limits for modded structures (ran as
  ST-52 in Phase 7 at `log = ALL`, 2026-10-19: no change proposed except the drum tower / undead castle; the 1.0.9
  footprint share still needs its own run); the Beardifier fill estimate is measured and its count built behind `beardFootprint` (3.30), calibration open.

**Meta structures** (3.16)
- **Vehicle metas** (marked 2026-10-14, not planned, 7.4.9): witch cart caravan, pillager caravan, trader caravan; own `meta/*` tags so the existing pillager metas don't change.
- More raft variants (`meta/raft_convoy`) and wreckage variants (`meta/shipwreck_debris`): need new templates; only
  the 3 villager rafts and the T&T wreckage exist (the debris got driftwood logs meanwhile).

**Testing**
- One scratch dedicated server run in the order of 8.0 (every pending ST- and LO- test, easiest to hardest), then
  the client tests of 8.0.11.
- Locate misses, batch/census commands: [locate.md](locate.md) (BUILT 2026-10-12; no miss buffer, counters instead); its LO- tests are in the 8.0 run order.

**Tuning**
- Do after scratch server test
- set shipwreck freq to 40%
- increase minimum distance for jungle temples. if it makes sense to split every surface dungeon out by biome to have finer control, do this

### Done

**2026-11-02: Philip's four Nether-built structures moved to the Nether** (`structures/contents #1`, data only; brief
[structures_contents.md](briefs/structures_contents.md) #1; ST-294, ST-295). Biome fits `nether/soul` (soul sand valley:
lost soul dungeon, lost soul city), `nether/forest` (crimson and warped forests: rare ruin) and `nether/wastes` (Nether
wastes, basalt deltas: blackstone Nether ruin), all `inherit: false`, in `biome_fits.json` with same-named structure
tags. Out of `type/ruin`, `type/underground`, `dungeon/underground_modded`, `land/any/deep`, `land/jungle`,
`land/swamp` and `meta/large_ruins` (the ruin metas lose one anchor: level-three ruins, ancient towers and the forgotten
church remain); into `type/nether_constructed`. Lost soul dungeon, rare ruin and blackstone ruin joined
`group/nether_ruins` (out of `ruins` and Philip's `underground_structures` set); the lost soul city keeps its own set
(spacing 37, separation 30). Rates by hand after the sync (its fit to their Overworld rates gave weights 148 and 214):
dungeon and blackstone ruin weight 100, frequency 0.5993 like Philip's other Nether ruins (about 1 per 36 to 39
chunks in their biomes), rare ruin weight 6, frequency 0.3759 (1 per 181); the four old members went from 1 per 34 to
1 per 38 chunks (obelisk 28 to 30). Start heights in `kubejs/data/philipsruins/worldgen/structure/` (jar copies,
only the height changed): rare ruin and blackstone ruin `absolute -95` on `WORLD_SURFACE_WG` (y 33 above the lava
sea, Philip's own Nether convention), lost soul dungeon fixed y 12, lost soul city fixed y 5 (31 tall, to y 36; if
lava floods it, ST-294 says move it to y 33). Wither skeleton spawn overrides kept. `plan/move_to_nether` removed:
swap file unchanged (1407), `nether_swaps.md` lists no exempt structure. Existing worlds keep the Overworld copies
already generated.

- [ ] **ST-294 Soul builds in the Nether [scratch]:** new world; in the Nether `/locate structure` each of
  `philipsruins:lost_soul_city`, `lost_soul_dungeon`, `rare_ruin`, `start_nether_ruin` → found, in its fit's biomes
  (soul sand valley; crimson or warped forest; wastes or deltas), at y 5 / 12 / 33 / 33 (F3), not flooded by lava;
  in the Overworld `/locate` finds none of the four.
- [ ] **ST-295 Ruin metas without the rare ruin [scratch]:** Overworld `/locate structure
  creativeengineering:meta/ruin_cluster` → found, anchored on a level-three ruin, ancient towers or forgotten church.

**2026-10-14: 7.4, new structure mods + `vehicles` group + fortified-town shares 0.20 / 0.20 / 0.40** (patcherMod
1.0.6 rebuilt in `build/libs` 01:28, not in `mods/`; offline checks pass; ST-170 to ST-190 next, on a scratch
server). Camps. Castles. Carriages. 2.3.4 ported from `DecompiledMods/` (1.1.1 jar out), Campsite Structures turned
into real structures. Details and choices: [testing/structures/01_build_7_4.md](testing/structures/01_build_7_4.md).

**2026-10-13: 7.3, fixes before Phase 3** (patcherMod 1.0.6 in `build/libs`, not in `mods/`; builds, worldgen tests
pass (earth fill 48, locate 28); not run in game; ST-158 to ST-166):
- Earth fill (7.3.1, 3.13): air and water the beard fills don't count (`beardReach` 6, `buryReach` 10, both measured
  with vanilla's Beardifier); mound columns; limits by the start's size; the single `maxOverAir` is gone. The woodland
  mansion is exempt (its own foundation). The simulation now shows vanilla villages hanging a median 0 blocks over air
  after the beard; the remaining cancels are houses over water deeper than 6 (about 20 to 40% of villages, down from
  about 45% under the 1.0.4 rule).
- Pruning (asked after 7.3.1, same day): villages drop the pieces over water or a drop (with their villagers)
  instead of being cancelled whole; whole-village cancels in the simulation fall from 22-40% to 5-12% (3.13).
  Config `prune`, `pruneMaxHanging` 8, `pruneMaxMound` 4; tag `earth_fill/prune`. ST-167 to ST-169.
- `/locate` through metas (7.3.2): targets that can't spawn in the dimension don't bring metas in; a meta slot is
  only loaded when an asked-for structure could be in it (new counter `meta_target`); every search ends after
  `[worldgen.locate] searchTimeoutSeconds` (120); `/cwglocate stop` and the server stopping end running searches.

**2026-10-12: 7.1, standalone structures folded into groups** (patcherMod 1.0.6 in `build/libs`, not in `mods/`;
nothing run in game; ST-102 to ST-131):
- Replacements, a generic group-member option (`replaces` + `share`, `MetaSlots`, CEConfig
  `structureReplacements`): the vanilla swamp hut, desert pyramid and jungle temple each take 10% of their YUNG's
  version's slots, so totals are unchanged (3.18; decisions 1, 2). YUNG's three "Disable Vanilla" switches are false.
- `group/witch_huts` (16/8, swamps only): YUNG's hut and circle, the witch village, the vanilla swamp hut (3.18;
  decision 3).
- `group/entrance_dungeons` (32/16): YUNG's zombie and spider dungeons, land only, with a surface check on the water
  rule so an entrance never comes up in water (3.19; decisions 7, 8). YUNG's skeleton and small dungeons stay standalone.
- Statues and cats into `ruins` at decoration level; It Takes a Pillage into `pillager_settlements` (below its
  target: the grid is full on open land) and the bastille into `meta/pillager_strongholds`; new `group/nether_ruins`
  (14/7) with the obelisk and Philip's three Nether ruins; Hopo's fossils into `ocean_ruins`; the fresh watchtower
  into `remote` (3.20; decisions 4, 5, 6, 9). Fixes the 3.16 "camp metas border only" item.
- The large ocean-monument-like ruin is Philip's ocean fortress (`philipsruins:ocean_fortress`: pieces 21 high, main +
  side piece about 55 x 43, guardians and drowned): new biome fit `ocean/deep` (`#minecraft:is_deep_ocean`) puts it in
  deep, deep cold, deep frozen and deep lukewarm oceans only (it had all 9 oceans). Its rate per deep-ocean area is
  unchanged (1 per 73), so there are about half as many overall. As a `meta/sunken_ruin_cluster` anchor it now only
  forms in deep oceans too.
- The Farmer's Structures Aether and Undergarden houses removed: `enabled: false` in their standalone entries, so
  `farmers_structures:aethers_with_all` and `undergardens_sea_with_all` are written empty at frequency 0 and leave
  `/locate` searches. Their biomes come only from the Aether and the Undergarden, which the pack lacks, so they never
  spawned (decision 12). The two Nether cooks stay standalone.
- `group/way_signs` at the old rate; every `settlements` member (metas and Terralith fortified villages included) is
  a sign destination; the rates tool reads `has_way_signs` as the Overworld (3.21; replaces decision 10, settles 11).

**Nether blocks swapped in Overworld structures (2026-10-12; patcherMod 1.0.6 in `build/libs`; builds; not run in game;
ST-150 to ST-152):** rules in `kubejs/generation_scripts/worldgen/nether_swap_rules.json`, expanded by
`python3 tools/gen_nether_swaps.py` into 869 per-position swaps in 65 templates (`nether_block_swaps.json`, report
[nether_swaps.md](nether_swaps.md)), applied by `TemplateBlockSwaps` (now reads both swap files) when a template loads.
In order, first match wins:

| Structures | From | To |
|---|---|---|
| Philip's underground structures | soul sand, soul soil | basalt (204) |
| YUNG's crypt (zombie dungeon) | soul sand | coarse dirt (97) |
| YUNG's jungle temple, Terralith fortified desert village | netherrack (soul sand/soil: the basalt catch-all below; they have none) | magma block (42: the jungle temple 34, the desert village's armorsmith 8; vanilla's `infiniburn_overworld` tag already lists magma, so fire on it burns forever) |
| Terralith frosted dungeon | soul soil; soul fire | Immersive Weathering permafrost (32); removed (28) |
| Simply Houses taiga woodcutter | nether wart (block), netherrack | removed (3) |
| Terralith fortified village | soul campfire | campfire, same facing and lit state (6) |
| Simply Houses taiga house | crimson roots and fungus, warped fungus and roots, nether sprouts | removed (4) |
| Sky village | glowstone | Quark yellow corundum (8) |
| Simply Houses taiga forge | wart blocks | removed (6) |
| YUNG's stronghold | nether gold ore | gold ore (172) |
| T&T mushroom village | twisting vines | removed (15) |
| Mysticrift mangrove tower | every Nether block (ancient debris, glowstone, respawn anchor) | gold ore (14) |
| every Overworld structure | nether brick family (blocks, red, cracked, chiseled, slabs, stairs, walls, fence) | deepslate brick twin, fence to wall (155: YUNG's stronghold, the illager warship, the underground bunker) |
| every Overworld structure | netherrack left (the ruined portals, all off, keep theirs) | magma (none left) |
| every Overworld structure but the ancient city and Philip's sculk dungeon | soul sand / soil left | basalt (39: the underground bunker 30, Philip's ancient crypt 6 and level-two ruins 3) |
| every Overworld structure | shroomlight | Quark orange corundum (38) |
| every Overworld structure | soul campfire left | campfire (2) |
| every Overworld structure | potted crimson/warped fungus and roots | potted dead bush (4) |

Exempt from every rule: none since 2026-11-02 (the four `plan/move_to_nether` structures moved to the Nether, 7 Done). A template a Nether or End structure can also place is never
swapped (none was hit). The Unwrecked Ships **Nether ship** is disabled (`enabled: false`): it is the only crimson/warped
ship; Overworld ships never take those pieces (the pool walk overcounted them, their jigsaw names don't match, so the
nether_blocks_scan report lists crimson/warped for `unwrecked_ships:ship` wrongly). Read for "ancient crypt and lv2
ruins": they had no netherrack; their soul sand/soil goes to basalt (user, 2026-10-12, changed from magma). Fence-to-wall keeps the
fence's connections (low where the fence connected).

**Meat blocks swapped (2026-10-12; patcherMod 1.0.6 in `build/libs`, builds, not run in game; ST-149):**
patcherMod `TemplateBlockSwaps` + `StructureTemplateManagerSwapMixin` (`StructureTemplateManager#get` RETURN) patch
each marked template once when it loads, every palette, only where the marked block is still there (else a warning),
so every rotation and every structure using the template gets the fish block. Reads `meat_block_swaps.json` at
every world load; switch `[worldgen.systems] templateBlockSwaps`. The file format is generic (template, position,
from, to), so other per-position swaps can use it.

**Meat blocks found and marked (2026-10-12):** `tools/meat_block_scan.py` ->
[meat_blocks.md](meat_blocks.md) and `kubejs/generation_scripts/worldgen/meat_block_swaps.json`. Every red block
(red glazed terracotta, red terracotta, red wool, red concrete and powder, pink glazed terracotta, red mushroom block,
nether wart block, redstone block) in live Overworld structure templates, lone = no neighbour of the same block. The
pack has exactly 5 red glazed terracotta, all lone, and all 5 are marked to become `architects_palette:salmon_log`
standing upright (`axis=y`): the Towns & Towers butchers of the birch forest, Mediterranean and mushroom villages (4),
and one in the Sky Villages `farm_2`. 359 other lone red blocks (beds, wool and terracotta trim, redstone, wart blocks)
are listed for review; none is in a butcher- or food-named template, so none is marked.

**Placement priority and town + villages built (2026-10-12, 3.22; patcherMod 1.0.6 in `build/libs`; MetaTest 65,
both test scripts pass, node/Rhino parity incl. the priority table; not run in game; ST-140 to ST-146):** ranks per
group (settlements > pillagers > dungeons > houses/camps > ruins; monuments > ocean ruins > boats > flotsam; fortified
towns over villages), a lower-ranked start that would overlap a higher one is dropped before generating, decided from
predictions so chunk order doesn't matter. `meta/town_villages`: a fortified town with 1 to 3 companion villages
64 to 96 blocks out (112 to 160 before 2026-10-19); villages near a higher-ranked structure grow around it instead of being dropped (shaping). The
existing overlap rate is noted in 3.22 (estimated about a third of villages; not measured).

**Wood mismatch reviewed, no changes (2026-10-12):** `tools/wood_mismatch_scan.py` ->
[wood_mismatch.md](wood_mismatch.md) compared every live land structure's woods with the native woods of its biomes
(the wood fits): 71 unchanged, 20 trim only, 7 woodless, 35 restrict and 36 swap candidates. User: "im fine with all
of those remaining, no restricts or swaps, mark this as done". Covers the old "matching wood for other wooden
structures" item; towers keep their wood fits (3.17).

**Meta structures added and tweaked (2026-10-12, 3.16; data only; MetaTest parses all 21; ST-134 to ST-139):**
- New: `fortified_ruins` and `fortified_camps` (a Terralith fortified village, read as the "town/fort" of
  settlements, with 2 to 6 small ruins or 2 to 4 friendly camps), `farmers_camp` (1 to 3 Farmer's Structures + 1 to 2
  camps), `house_camp` (lone house + camp), `waysign_camps` (way sign + 1 to 3 camps), `waysign_house_camp` (way
  sign + camp + lone house). New tags `meta/fortified_towns`, `friendly_camps`, `lone_houses`, `farmers_structures`,
  `way_signs`. "Camps" are the friendly camps (no hostile mobs).
- Tweaked: driftwood flotilla packed closer (logs within 26, raft within 14); raft convoy 2 to 5 rafts; shipwreck
  debris 1 to 3 wreckage, wider apart, plus up to 6 driftwood logs close to the wreck. `pillager_tower_siege` already
  takes every standard-height tower (the vanilla outpost and the 14 T&T tower-form outposts; Mysticrift's tall towers
  have their own meta): no change.

**Overworld structures with Nether blocks found (2026-10-12):** `tools/nether_blocks_scan.py` walks every template an
Overworld structure can place (jigsaw pools included, pieces `pool_tags.json` removes skipped) and counts Nether blocks
in three tiers: 1 netherrack, soul sand/soil, nether wart; 2 crying obsidian, the blackstone family (gilded included),
basalt; 3 everything else Nether (nether bricks, crimson/warped, wart blocks, soul lanterns, magma...). Report:
[nether_blocks_scan.md](nether_blocks_scan.md). 64 live structures hold some; **tier 1 in 13**: Philip's lost soul city,
lost soul dungeon, underground set, sculk dungeon, ancient crypt and level-two pool ruins (soul sand/soil), the
vanilla ancient city (soul sand), YUNG's zombie dungeon (soul sand 97), YUNG's jungle temple (netherrack 34), Terralith's
frosted dungeon (soul soil) and fortified desert village (netherrack 8), Philip's blackstone Nether ruin, Simply Houses'
taiga woodcutter (1 netherrack). Biggest tier 2: Philip's Nether ruin (2287 blackstone), the animal barn (822
polished blackstone), Terralith's fortified village (480 basalt), Simply Houses' plains mansion and taiga forge
(basalt), T&T's Swedish outpost and the vanilla outpost (polished blackstone). The disabled ruined portals hold 1860
netherrack. Blind spots: processor-list swaps, code-built structures (YUNG's spider dungeon has no templates).

**Tech village houses removed (checked 2026-10-12; done earlier by the pool tuning, 3.5):** the `tech` tag in
`pool_tags.json` (multiplier 0) holds `#mod/immersiveengineering` (25 houses: engineer, electrician, gunsmith,
machinist, outfitter x 5 village types), `#mod/pneumaticcraft` (5 mechanic houses + the 2 custom streets that lead to
them), `#mod/dynamicvillage` and `#mod/advancedperipherals` (5 scientist houses). **Dynamic Village** is the
Create-only village mod: 20 Create houses (mechanical engineer, miner, hydraulic engineer, train mechanic x 5 village
types), injected in code. No other Create add-on in the pack adds village structures (every jar that depends on Create
checked for templates and pools). The last game report (`pool_tag_report.md`, 2026-10-11) shows all 57 pieces
**removed**. The mods' professions and workstations remain (villagers can still take those jobs).

**2026-10-12: geodes under oceans placed lower** (patcherMod 1.0.6 in `build/libs`, not in `mods/`; builds, not run
in game; ST-133): new `worldgen/OceanGeodes` + `GeodeFeatureOceanDepthMixin` on vanilla `GeodeFeature`, which
the amethyst geode and every Geode Plus geode use (Geode Plus' own `OceanGeodeFeature` is registered but no geode
uses it). A geode whose column's sea-floor biome is `#minecraft:is_ocean` is placed 10 blocks lower; 10% keep their
height (a hash roll of seed and position, not the feature's random). A geode that would end below the world bottom
keeps its height. **Geode Plus' prismarine geode is left alone** (it belongs on the sea floor): geodes whose inner
layer is on the `untouched` list (default `geode:budding_prismarine`) keep their height. CEConfig
`[worldgen.ocean_geodes] drop = 10, stayShare = 0.1, untouched = ["geode:budding_prismarine"]`, switch
`[worldgen.systems] oceanGeodes`. Rates and the land are unchanged; Terralith's cave/mountain geodes are other
feature types and untouched.

**2026-10-22: ocean geodes go under the deepest floor** (patcherMod 1.1.01, built 2026-10-22, in the `mods/` jar of
11:06 (md5 b5f60533) and the newer `build/libs` one; `server_mods/` and the 2026-10-22 scratch-run jar 37bb7049
still have `drop`; `mods/` holds md5 a4d48336 since the second 2026-10-22 scratch run; DEPLOYED with 1.1.01 (2026-10-22), scratch server PASSED 2026-10-22 (SQ-GE-00 to SQ-GE-04);
user decision, replaces the fixed 10-block drop that ST-133 showed does little over floors near y 0): a moved
geode's origin goes to at most `lowestFloorY - clearance - maxGenOffset` (the geode config's own placement-box
bound, 16 for vanilla amethyst), then down a further 0 to `spread` blocks (seed+position roll, so the moved
geodes don't share one layer). Geodes already that deep don't move. `stayShare` 0.1 still keeps its height.
Keys: `lowestFloorY` -8 (Terralith deep floors measured near y 0; Tectonic `deeper_oceans` is on), `clearance`
2, `spread` 8; `drop` removed. Defaults put moved amethyst origins at y -34 to -26. A geode whose box would reach
the world bottom keeps its height (not reached at the defaults). Moved geode tops (origin + `maxGenOffset`) end at
y -10 or lower (`lowestFloorY - clearance`). A config edit needs a restart and only changes new chunks. Scratch
queue SQ-GE (`tools/farm_worldgen_probe.js` `zfw geode`: places the amethyst and the prismarine geode at ocean and
land sites in a fresh normal world and reports where each landed).

**Fixed in the scratch run (2026-10-22, SQ-GE-03): prismarine geodes were moved too.** Geode Plus' prismarine geode
keeps its budding prismarine in the alternate inner layer (its inner layer is plain prismarine), so the `untouched`
check on the inner layer never matched. `OceanGeodes.untouched` now checks the alternate inner layer as well. Rerun:
23 of 24 ocean amethyst geodes moved, tops 10+ blocks under the floor; prismarine and the 32 land geodes kept their
height; with `oceanGeodes = false` 0 moved.

**2026-10-12: biome fits for every land structure** (3.17, plan 7.2): 29 fits, 161 structures (166 now with the
three vanilla replacements), towers folded in, vanilla outpost by its dark oak, sky village over land and sea.

**2026-10-11: meta structures** (3.16): 15 metas with slot conversion and `/locate` of parts: village + 1 to 3
peaceful towers, large ruin + small ruins (`ruin_cluster`), 2 to 4 small ruins (`ruin_scatter`), large underwater
ruin + small ones (`sunken_ruin_cluster`), shipwreck + floating wreckage (`shipwreck_debris`), shipwreck graveyard,
raft convoy, pillager fleet, pillager tower / large tower / stronghold + siege equipment, the three pillager camp
metas, the driftwood flotilla.

**2026-10-11: biome fits for towers by wood** (3.17).

**2026-10-09:** terrain adaptation by tag, the soft beard, template trim (3.15); the scan of every structure for pads
and saved air and its batches 1 to 3 tagged.

**2026-10-03** (3.12, 3.13): Monobank vault pieces about 5x rarer in villages; land driftwood about a tenth of the
water rate (water keeps 0.25x); earth fill cancels structures that need too much earth over water or air.

**2026-09-27 to 09-28:** structure groups (shared grids, weights, frequencies; 3.1); the sky village moved into
`sky` (3.11); structure mods converted to datapack data.

**2026-09-25:** every ruined portal removed (the set kept, 7 members off).

### 7.2 Plan: biome fits for every structure, towers folded in (2026-10-11, BUILT 2026-10-12)

Summary; the full plan, inventory, family lists and per-structure table are in [biome_fits.md](biome_fits.md). The
system is described in 3.17.

- Every land structure gets its biomes from `land/*` fit families instead of its mod's list: 29 fits (`land` + 27
  biome-type and wood families + `land/mushroom`), plus `ocean/deep` since 2026-10-12; 167 structures fitted. The old
  `tower/*` fits were renamed into `land/*`, every tower keeping its biome list.
- The vanilla pillager outpost goes by its dark oak; the sky village spawns over land and sea (`land/sky`);
  Philip's nether ruin is held on its own list (decision 4).
- `--fit-rates` keeps each member's density per area of its own biomes; `fit_keep: true` holds the 12 group-member
  towers at their old rates.
- Biome merges retired 2026-10-11; the fits restore the Terralith biomes the merges used to add.
- Known gap (2026-10-13, 8.0.2): `land/any` has no cave biomes, so the deep dungeons can't start (7.3.3).

### 7.3 Plan: fixes before Phase 3 (2026-10-13, BUILT 2026-10-13: 7.3.1 and 7.3.2)

Phase 2 of the scratch run (8.0.2) found three bugs. Two of them are fixed here before Phase 3, because Phases 5 and 6
measure rates and the bugs would skew them. The third (the deep dungeons) waits for a cave-structure tuning pass.

#### 7.3.1 Earth fill: limits by size, beard fill not counted

**Goal (user, 2026-10-13):** the cap stops a structure from generating where it would leave a huge mound of dirt
or a floating pad: places it just shouldn't be. It should not stop a structure whose ground the beard builds
normally.

**What goes wrong now** (8.0.2 finding (a)): `EarthFill` counts each earth block of the bottom 3 template layers
whose spot is air in `getBaseColumn`, the raw noise terrain. The beard (vanilla `beard_thin` / `beard_box`, and the
pack's soft beard from 3.15) fills most of that air with ground before the template is placed, but the count can't
see it. One cap of 256 then applies to every structure, so a fortified village (822 earth base blocks) trips at a
few percent overhang while a small ruin could hang a whole pad. Result: 49 of 49 fortified villages that anchored a
meta, nearly all Takes A Pillage camps and bastilles, and 41% of the T&T old-growth-taiga outposts were cancelled.

**Change 1: beard fill doesn't count toward the air cap.** Per piece, take its terrain adaptation (the structure's
own, or the pack's soft beard from `terrain_adaptation/*`) and decide for each earth block whether that air will
still be air after the beard:
- `beard_box`, `encapsulate`: the beard fills the box from below, so air under the piece never counts.
- `beard_thin`, soft beard: an air block counts only if the raw terrain under it is more than `beardReach` blocks
  below the piece's bottom. Below that depth the beard tapers out and the pad really hangs.
- `none` (and pieces with no beard): every air block counts, as now.
- `beardReach` comes from the Beardifier's falloff (vanilla `beard_thin` reaches about 6 blocks below a piece at
  full strength; check against `tools/earth_fill_sim.sh`), plus `skirtBump` for the soft beard. It's a CEConfig
  value, `[worldgen.earth_fill] beardReach`.

**Change 2: the mound check** (a second count, so the cap means what the goal says). A column where the raw terrain
lies more than `maxMoundHeight` below the piece's bottom is a column where the beard would build a tall dirt pillar,
or can't reach the piece at all. Count those columns per start (each column once, not per layer) as `mound`, with
its own limit. This is what stops a village on the edge of a ravine or a pad over a cliff.

**Change 3: limits by size family.** One cap can't fit both a fish pond and a fortified town. Each structure gets
a size family from its built footprint (the earth base block count the template scan already has):

| Family | Earth base blocks | maxOverAir (after beard) | maxMound columns |
|---|---|---|---|
| `small` | under 150 | 24 | 8 |
| `medium` | 150 to 600 | 64 | 24 |
| `large` | 600 to 2000 | 160 | 64 |
| `huge` | over 2000 | 320 | 128 |

The numbers are first guesses for the calibration run (ST-52, `log = ALL`, Phase 7). The families are tags
`earth_fill/size/<family>`, generated by a tool from the scan (`tools/gen_earth_fill_sizes.py`, in `/cdevgen`), so a
new mod's structures get one automatically. Each family's limits go in CEConfig (`[worldgen.earth_fill.sizes]`).
The existing `overrides` list still wins for single structures, and a type family (`#type/village=...`) can be
overridden too.

**Change 4: the default.** `maxOverAir` became 128 in the jar default (52b2c63) while the comment, 3.13 and the
instance toml say 256. With changes 1-3 the single default is only the fallback for structures without a size
tag: set it to the `medium` value and fix the comment.

**Log:** the `[earth-fill]` line gains the family and the counts after the beard, e.g. `... large: 822 earth, 41 over
air after beard (limit 160), 12 mound columns (limit 64) -> kept`.

**Tests (added to 8.0 Phase 1/2/7):** a fortified village, a bastille and a Takes A Pillage camp locate and stand
(the 8.0.2 cancels gone); a structure forced onto a cliff edge (`/place` doesn't hit the check: use `log = ALL` on
new chunks) still cancels; ST-135, ST-143 and ST-118 rerun; ST-52 calibrates the four families.

**Built 2026-10-13** (3.13 is the reference). Where it differs from the plan above:
- *Change 1* as planned, with the reach measured instead of guessed: `tools/earth_fill_sim.sh ... beard` beards real
  vanilla village starts with vanilla's own Beardifier and checks the whole gap under each earth block. beard_thin
  fills it 81% of the time at 6 blocks and 35% at 7, beard_box 70% / 17%, bury 100% to 10 and 27% at 11:
  `beardReach = 6`, `buryReach = 10`. beard_box fills from below with the same falloff as beard_thin (it carves inside
  its box), so it is not "never counts". The soft beard has beard_thin's vertical profile under its footprint, so it
  uses `beardReach` with no `skirtBump` added (skirtBump only widens the blend sideways).
- *Water too.* The simulation showed vanilla villages set house floors into the top water layer of rivers and coasts
  (all their "over water" blocks are at y 62), and the beard fills water as it fills air. So earth over water follows
  the same rule: it counts only past the beard's reach. The 2026-10-03 "0 over water" was a fault of the
  simulation (unbound fluid tags), see 3.13.
- *Change 2* as planned: `maxMoundHeight = 8` (where beard_thin's fill drops to a quarter); columns count once per
  start; the gap is measured through air and water.
- *Change 3, sizes:* the size is the start's own earth base block count, decided when the start is checked, not
  tags from a tool. The terrain scan has no earth counts (it measures columns), and a jigsaw structure's earth
  depends on which pieces it picked, so a per-structure tag could only be a guess; the count at generation is exact
  and covers new mods by itself. No `earth_fill/size/*` tags, no `gen_earth_fill_sizes.py`. Limits per size in
  `[worldgen.earth_fill.sizes]`; `overrides` (now `water,air[,mound]`) still win, `#tag` keys included.
- *Change 4:* every start has a size, so there is no fallback: `maxOverAir` is removed (the 128 / 256 mismatch goes
  with it).
- The woodland mansion is exempt: its afterPlace pours cobblestone under its footprint to the ground, and the small
  size cancelled half of the simulated mansions.
- Log line as planned, plus the size and the beard: `... (large, beard): 822 earth, 0 over water (limit 24), 41 over
  air after beard (limit 160), 12 mound columns (limit 64) -> kept`.

#### 7.3.2 `/locate` through metas: fail fast, prune, time out

**What goes wrong now** (8.0.2 finding (c)): for a target that can sit inside metas, `MetaSlots.beginLocate` adds
every such meta to the search, and `locateAt` loads each slot where the meta's prediction ("one of its anchors fits
here") says yes. Nothing asks whether the target itself could be there. A target that can't spawn in the dimension
(the End tower from the Overworld) never fails, and a rare anchor (bastille, desert tower) loads hundreds of slots
won by other anchors. Async Locator can't cancel a search, and its non-daemon threads hold up `stop`.

**Fix, in four parts:**
1. **Dimension check in `beginLocate`.** Drop every target whose biomes (after fits and biome rules) don't meet the
   level's `BiomeSource#possibleBiomes()`, then add only the metas that can contain a target that survived. If none
   survive, return the plain search, which vanilla already ends at once for structures that can't spawn there.
   ST-80: "Could not find" in about a second.
2. **A per-slot prediction for the target, before any load in `locateAt`.** For a meta slot:
   - If a target is an anchor of the meta: it must fit the slot (`LocatePrediction.fits(anchor, ...)` with its own
     biomes), keep its frequency (`StructureFrequencies.keepsAt`), and convert to this meta
     (`conversion(index, anchor, ...).target() == meta`). This is the same test `predictGenerationPoint` uses, but
     for the target only, not for "any anchor".
   - If a target is only a part: the meta's prediction must say yes, and the part's biomes must meet the biomes
     sampled at quart resolution over the area parts can reach (7 chunks around the slot, at the surface). That
     check is conservative, so the census still has 0 false negatives.
   - Count what this saves as a new `LocateStats` stage (`meta_target`).
3. **A search deadline.** `[worldgen.locate] searchTimeoutSeconds` (default 120). `LocateLoads` and `locateAt` check
   it before each load and end the search with "not found (timed out after N s)", logged with the `[locate]`
   counters. `/cwglocate stop` ends running `/locate` searches too, not just batches.
4. **Shutdown.** On `ServerStoppingEvent`, flag every running search to stop (the deadline check sees it), so
   `stop` no longer waits on Async Locator's threads. No change to Async Locator itself.

**Tests:** ST-80 first half fails fast; desert tower and bastille answer or time out within 120 s; ST-86/ST-87
still find parts and anchors; LO-03 census false negatives stay 0; `stop` during a long search exits within a few
seconds.

**Built 2026-10-13** as planned. Details:
- Part 1: `MetaSlots.beginLocate` takes the level. A target counts when some biome of
  `BiomeSource#possibleBiomes()` passes its biomes (fits applied) and biome rules. Targets that can't spawn stay in
  the search as asked (vanilla gives them no placement there) but bring no metas in.
- Part 2: only for a meta slot not generated yet (`CHUNK_LOAD_NEEDED`); a known start is loaded as before. The anchor
  test is `predictGenerationPoint`'s, for the target alone. For parts: every chunk within `MetaStructure.REACH` (7)
  of the slot, nearest first, its surface height (`WORLD_SURFACE_WG` at its centre), the biome of each of its 16
  quarts at that height and 16 above and below; the first biome a target part accepts ends the check with yes. Every
  part in `meta_structures.json` is a surface or sea structure. The census doesn't go through this path, so LO-03's
  false negatives can't change; the check can only make `/locate` skip a meta slot.
- Part 3: `LocateStats.expired()`: past `searchTimeoutSeconds` (0 = none), `/cwglocate stop` since the search began,
  the server stopping, or the search thread interrupted (Async Locator's cancel interrupts). Then every further
  candidate returns "nothing" at `getStructureGeneratingAt` HEAD (vanilla's ring walk runs out in milliseconds), and a
  load returns an empty chunk at once, or within a quarter second while it waits (the load now waits in 250 ms slices,
  not one 300 s `get`). A search cut short always logs its `[locate]` line, `... not found (timed out after 120 s)
  after N misses ...`; `/cwglocate stats` counts them (`cutShort`), and batch rows say `timed_out` or `stopped`.
- Part 4: `ServerStoppingEvent` flags every search (and resets on the next server start).

#### 7.3.3 Deep dungeons: marked for the cave-structure pass

`betterdungeons:skeleton_dungeon` (start y -50..-30), `philipsruins:bone_dungeon` and `philipsruins:lost_soul_city`
(y -40) can't start since the 7.2 fits: `land/any` lists surface biomes, and the biome at their start height is a
Terralith cave biome (`terralith:cave/deep_caves` everywhere sampled). Not fixed now: they go into the cave-structure
generation tuning (Todo, "Underground"), which decides their biomes (cave biomes, an `underground/*` fit family), their
grouping and their rates together. Until then they don't generate; ST-115 stays failed and is retested in that pass.
**2026-10-18:** they get a fit of their own, `land/any/deep` (cave biomes + `land/any`), so they generate again while the
grouping and rates still wait for that pass (3.23, ST-196).

### 7.4 Plan: new structure mods, the `vehicles` group, fortified-town meta shares (2026-10-14, BUILT 2026-10-14)

**Request (user, 2026-10-14):** migrate the newly added structure mods into structure tuning. Camps go into the
passive or hostile camps. The undead castle becomes a new surface dungeon and the red castle a new city/fort, with
the city/fort meta anchor share at 75% (settled the same day as 0.20 / 0.20 / 0.40, D1). Pillager carts, wagons, witch carts and the siege weapons already in the
pack form a new **vehicle** category (hostile only; existing metas left alone). Vehicles are rarer than camps, more
common than outposts, and more frequent in extreme/hostile biomes. Caravan metas are marked, not planned. This is
built and checked **before Phase 3** (8.0.3). Balance stays loose until the audit; **correctness comes first**: no
structure in a wrong biome, inside a peaceful radius, or past earth/water fill. Source for the castles, carts and
wagons (user, same day): **Camps. Castles. Carriages.**, the latest version **2.3.4**, decompiled at
[DecompiledMods/camps_castles_carriages-2.3.4](../DecompiledMods/camps_castles_carriages-2.3.4/).

**Order:** 7.4.0 to 7.4.8 in sequence, offline checks only (gradle build, `tools/test_worldgen_tools.sh`,
`tools/test_structure_groups.sh`, node sim), as in 7.1. Then the 7.4 block of tests in 8.0 (ST-170 to ST-190), then
Phase 3.

#### 7.4.0 Inventory (read from the files, 2026-10-14)

**Camps. Castles. Carriages. 2.3.4** (namespace `eugene`, a datapack wrapped as a mod: no classes, `mods.toml`
`lowcodefml`, `pack_format` 15 = 1.20.1). `mods/` has the older **1.1.1** jar (no castle pool, no red roof, drum
tower, prison van, lost llama). The plan ports **2.3.4** into `kubejs/data/eugene` and takes 1.1.1 out of `mods/`.

| Structure (mod id) | Pool: templates (weight) | Size (x, y, z) | Mobs and loot | Mod biomes / set (spacing/separation) | Goes to |
|---|---|---|---|---|---|
| `eugene:camp` | `trader_camp` (100), `trader_camp_fake` (10), `trader_camp_lost_llama` (15), `pillager_camp` (120) | 9x7x8 / 7x6x9 | trader + llamas, chest with a village map; the fake one turns into an illusioner (function); pillager camp: pillager + vindicator | 14 forest/taiga/plains/swamp/hills biomes; 38/22, 4 chunks off `minecraft:villages` | **split** (7.4.3): passive + hostile camps |
| `eugene:castle` | `plains_undead_castle` (3), `red_roof` (3, legacy element), `ruined_drum_tower` (3, legacy element) | 43x28x45 / 42x55x45 / 49x44x45 | undead: 3 skeleton spawners, 8 zombie-knight/skeleton-archer jigsaws, boss "Ser Marrow" jigsaw, 5 chests; red roof: 11 villager + 3 iron golem jigsaws, sheep/cows, 1 zombie; drum tower: ~200 suspicious gravel/sand (archaeology loot), bats | plains; 75/65, 4 chunks off villages | **split** (7.4.4): surface dungeon, city/fort, ruin |
| `eugene:pillager_loot_carriage` | `pillager_loot_carriage` (2), `pillager_chase_villger` (1, entities only: 5 pillagers chasing a villager), `pillager_golem_van` (1, prison van: iron golem, 4 pillagers, ravager) | 5x5x7 / 1x3x1 / 7x10x8 | hostile; 2 chests with a village map | plains; 120/70, 4 chunks off villages | `vehicles` |
| `eugene:witch_wagon` | `witch_wagon` | 13x11x7 | witch | dark forest; 60/25 | `vehicles` |
| `eugene:piglin_camp` | `piglin_camp` | 10x9x9 | 4 piglins, chest | crimson forest; 22/12; **fixed start y 33, no heightmap** | Nether, 7.4.6 |
| (vanilla override) `minecraft:pillager_outpost/feature_tent1`, `feature_tent2` | | 9x5x7 | | | kept, remapped (7.4.2) |

Functions (`#minecraft:tick` / `#minecraft:load`): the fake trader, the lost-llama quest, the Ser Marrow boss bar /
steed / minions, and the Nether "pig ranger" mini-boss (a hoglin rider summoned every 120 s near a random zombified
piglin 64 to 86 blocks from a player in the Nether, 20% chance; not tied to a structure).

**Campsite Structures 1.0.3** (`campsite_structures`, MCreator, has code: `CamperEntity`, items): 6 camps (armorer,
butcher, cartographer, cleric, farmer, fisher) placed as **features**, not structures: `forge:add_features` in
`forge:any` biomes at `surface_structures`, on `OCEAN_FLOOR_WG`. **Live now and outside every rule:** any biome
(Nether and End too), possible on a seabed, no group, fit, peaceful radius, earth/water fill or `/locate`.

**Illager Siege Weapons** (6 camps) are in `group/pillager_settlements` at ~1 per 111 chunks each. They are only meta
**parts** (`meta/siege_weapons`, `meta/pillager_outworks`); no meta anchors on them.

#### 7.4.1 Campsite Structures: features -> structures, passive camps

1. **Stop the features:** override all 6 `data/campsite_structures/forge/biome_modifier/*.json` in
   `kubejs/data/campsite_structures/` with `forge:none` (as for Bushier Flowers / wild crops). The jar stays (the
   camper entity and items are code), so the porter isn't used.
2. **Real structures:** per camp a `structure` (jigsaw, a one-piece pool on the jar's template
   `campsite_structures:<name>_campsite`), a `template_pool`, and an emptied `structure_set` for the scanner, written
   the way the ported MCreator camps are (`kubejs/data/villager_archer_camp/worldgen/*`).
3. **Passive** after reading each template's entities: `group/remote` + `meta/friendly_camps`, `type/camp`,
   `mod/campsite_structures`. Joining `meta/friendly_camps` makes them parts of the friendly-camp metas and anchors of
   `waysign_camps` (in `remote`, so valid); no meta file changes. A camp with hostile mobs goes to the hostile camps
   (7.4.3) instead.
4. **Fit:** open-land families as the villager tents; the fisher camp on `land/beach` + river-side land. Never
   `water_ok`. `--fit-rates` at the tents' per-area density.

#### 7.4.2 Port Camps. Castles. Carriages. 2.3.4 to `kubejs/data/eugene`

The data is 1.20.1 data on a 1.19.2 pack. The worldgen JSON format is the same, but some blocks, an item and two
commands aren't, so it can't just be dropped in as a jar.
1. **Porter:** `tools/port_structure_jars.py` fails on this jar (`mod_id` only matches `modId="..."`; this
   Modrinth-generated toml uses single quotes) and only reads jars. Extend it: accept both quote styles and an
   unpacked directory (`DecompiledMods/camps_castles_carriages-2.3.4`). Copy `data/eugene/**`, `data/minecraft/structures/**`,
   `assets/eugene/lang` -> `kubejs/assets`. Move the 1.1.1 jar out of `mods/` (`disabledmods/archive/`), so 1.1.1
   and 2.3.4 data never load together. Templates are DataVersion 3465 (and one 2975), under the 3837 cut-off, so
   they're portable.
2. **Blocks 1.19.2 doesn't have** (they'd load as air), added to `BLOCK_REMAP`:

   | Template | Block | -> |
   |---|---|---|
   | `ruined_drum_tower` | `suspicious_gravel` / `suspicious_sand` (~200, archaeology loot) | `gravel` / `sand` (D6: the loot is lost; option: a few buried chests with the archaeology table) |
   | `ruined_drum_tower`, `witch_wagon` | `decorated_pot` | `supplementaries:urn` (already in the table) |
   | `red_roof` | `chiseled_bookshelf` | `bookshelf` (already in the table) |
   | `red_roof` | `mangrove_hanging_sign` | `mangrove_wall_sign` (or air) |
   | `witch_wagon`, `feature_tent1/2` | `diagonalwindows:minecraft/purple_stained_glass_pane`, `diagonalfences:minecraft/dark_oak_fence` (mods not in the pack) | `minecraft:purple_stained_glass_pane`, `minecraft:dark_oak_fence` |

   Entities from mods not in the pack (`naturalist:firefly`, `wildlife:lark` in the drum tower) are stripped. The lone
   `minecraft:zombie` in `red_roof` is stripped too (a city holds no hostile mob, D5).
3. **Loot:** `snout_armor_trim_smithing_template` (1.20) in `piglin_camp_chest` -> drop the entry or swap it for a
   1.19.2 item. The `exploration_map` entries (destination `village`, carriage and trader chests) run through Async
   Locator's loot-function patch (it ships `ExplorationMapFunctionMixin`); test that opening a chest doesn't stall
   (ST-189). Run `tools/loot_breakdown.py` for the catalog.
4. **Functions:** `ride` and `damage` are 1.19.4 commands. `summon_pig_ranger` and `tick_ser_marrow` fail to parse, and
   `tick.mcfunction` calls `tick_ser_marrow`, so **the whole tick function fails** in 1.19.2 (no fake trader, lost
   llama or boss bar). Rewrite: mounts via `summon ... {Passengers:[...]}`; the steed's `damage` line dropped or
   replaced (`effect`). Check the port with a `/reload` on the scratch server (ST-170: no function parse errors).
   Performance note for perf.md: `tick` runs `execute as @e[type=wandering_trader] ... at @a` every tick; consider
   scheduling it every 10 ticks.
5. **Vanilla outpost tents:** the two `minecraft:pillager_outpost/feature_tent*` templates replace vanilla's; keep
   them (the mod's look), with the fence remap.
6. **Scans:** `tools/terrain_adaptation_scan.py`, `tools/nether_blocks_scan.py`, `tools/meat_block_scan.py` over the
   new templates. The drum tower's `red_concrete`/`emerald_block`/`lapis_block` and the undead castle's
   `coal_block` are decoration, nothing to swap unless a scan flags them.

#### 7.4.3 Camps: split `eugene:camp` into passive and hostile

One structure that is sometimes friendly and sometimes a pillager camp can't sit on either side of the peaceful
radius (a structure in both lists stays peaceful, so pillagers would camp next to villages). Split the pool:
- `eugene:camp` (kept id, so its loot/advancement references hold): pool = `trader_camp` (100) + `trader_camp_lost_llama`
  (15). **Passive:** `group/remote`, `meta/friendly_camps`.
- New `eugene:pillager_camp`: `pillager_camp`. **Hostile:** `group/pillager_settlements`, `meta/pillager_camps`, the `pillager` tag. Joining `meta/pillager_camps` makes it an anchor of the three camp
  metas (they gain an anchor in open land, which helps the "border only" camp metas of 3.16); their definitions stay as they are.
- New `eugene:fake_trader_camp`: `trader_camp_fake`. **Hostile** (an illusioner ambush; it must not sit inside a
  village's radius): `group/pillager_settlements` only, not in any meta tag (it would look like a friendly camp).
- Fit: the mod's 14 forest/taiga/plains/swamp/hills biomes as `land/*` families for all three. Weights inside the old
  pool (100/15 vs 120 vs 10) become starting rates: `sync` adds each at its share of the old 38/22 set.

#### 7.4.4 Castles: split `eugene:castle` three ways

`eugene:castle` is emptied (`enabled: false`); each template gets its own structure (one-piece pool, the mod's
jigsaw children kept: `ser_marrow`, `undead_castle_enemies`, `red_roof_villagers`, the vanilla `iron_golem` pool):
- **`eugene:undead_castle`** -> **surface dungeon:** `group/surface_dungeons` (hostile), `type/dungeon`. Fit: plains
  and other open land that suits a cobble/spruce ruin (`land/plains`, `land/taiga`, `land/highlands`). Terrain
  `beard_box`; no below-ground entrance (spawners sit inside), so no `surface_check`.
- **`eugene:red_roof_castle`** -> **city/fort:** `group/settlements`, `#priority/big_settlements` (rank 100, peaceful
  radius 8), `meta/fortified_towns` (anchor of `fortified_ruins`, `fortified_camps`, `town_villages`), a `type/*`
  fort tag but **not** `#type/village` (earth-fill pruning drops pieces of villages; the castle is one 42x55x45
  piece). Way-sign destination automatically. Fit: plains/meadow/forest edges. 55 blocks tall: check its
  `[earth-fill]` size class and that `town_villages` companions (64 to 96 blocks out since 2026-10-19) clear it.
- **`eugene:ruined_drum_tower`** -> **ruin:** `group/ruins`, `type/ruin`. Not in `meta/large_ruins` (at 49 across it
  is twice the size the ruin metas were measured for, and adding it would change existing metas). Neutral: neither
  peaceful nor hostile.
- **Fortified-town meta shares (D1, user 2026-10-14):** when a town/fort (a `meta/fortified_towns` anchor, red
  roof castle included) is placed, one roll picks **fortified_camps 0.20, fortified_ruins 0.20, town_villages 0.40**
  (was 0.25 / 0.25 / 0.30). Sum 0.80: under the 100% cap, so the id-order cut never trims any of them, and 20% of
  towns/forts stay plain. (An earlier 75% for town_villages alone was replaced the same day.)

#### 7.4.5 The `vehicles` group (hostile)

- **New `group/vehicles`** (`structure_tags.json` + `structure_groups.json`, own salt). Members:
  - the 6 Illager Siege Weapons camps (moved out of `group/pillager_settlements`);
  - `eugene:pillager_loot_carriage` (loot carriage, villager chase, golem prison van; pool weights kept);
  - `eugene:witch_wagon`.
- **Existing metas untouched.** Siege weapons are parts only, and a part may come from any group, so the 4 pillager
  metas keep working. The new vehicles don't join `meta/siege_weapons` / `meta/pillager_outworks` (that would change
  existing metas); the caravan metas (7.4.8) get their own tags. MetaTest + sim confirm the metas still assemble.
- **Correctness, the step that's easy to miss:** add `vehicles` to `priority.peaceful.hostile` (moving the siege
  weapons out of `pillager_settlements` would otherwise drop them from the hostile list), a `priority.groups` rank
  (76: under the dungeon groups, over `remote` 70), and the same 4-chunk `exclusion_zone` from `settlements` that
  `pillager_settlements` has (the mod's sets kept 4 chunks off villages). Not `water_ok`.
- **Fits:** carriage on open land (`land/plains`, `land/savanna`, `land/desert`, `land/badlands`, `land/snowy`
  plains); witch wagon `land/dark_forest` + `land/swamp`; siege weapons keep their open-land fit. Terrain
  `beard_thin` (mod default) for the carriage/wagon; the 1x3x1 chase piece has no blocks (earth fill sees no earth).
- **Rates** (soft; audit later), per biome from `structure_group_estimates.md`:
  - camps C = all `meta/friendly_camps` + `meta/pillager_camps` members combined; outposts O = the
    `pillager_settlements` towers and forts. Today camps ~1 per 47-135 chunks, outposts ~1 per 63-164; the 6 siege
    weapons are ~1 per 111 **each**, so as a category they come down.
  - Vehicles V (whole group): between O and C in ordinary biomes, about 2x that in harsh ones, never denser than C.
    Grid sized from the harsh rate; ordinary biomes get theirs by frequency. Equal member weights to start; the
    carriage and the witch wagon keep their old ratio to each other (mod sets 120 vs 60).
  - Acceptance: `--check` shows C > V > O in most biomes where all three exist.
- **Harsh biomes (D3):** biome tag `creativeengineering:harsh` from fit families: `land/badlands`, `land/desert`,
  `land/snowy` (+ frozen peaks), `land/swamp`, `land/dark_forest`, Terralith volcanic/ash biomes, jagged/stony peaks,
  wastelands.
- **Mechanism (D4, new code):** group-level `biome_frequency` {`"#creativeengineering:harsh": 1.0, "default": 0.5`}
  multiplies each member's `frequency` by the factor of the biome at the start (the sample the structure's own biome
  check uses). Rolled in `StructureFrequencies` and in the `/locate` roll (`StructureCheck#canCreateStructure`) by the
  same function, so `/locate` and generation can't disagree; JS validator key + staging, node/Rhino parity,
  `gen_structure_groups.py` estimates per biome. Rejected: a second set per biome (one structure in two sets breaks the
  one-set model and `/locate` prediction).

#### 7.4.6 Piglin camp (Nether)

Not part of the Overworld camp categories. Stays a **standalone** set at the mod rate (22/12), fit to
`minecraft:crimson_forest`, marked for the Nether grouping pass (Todo). **Correctness:** 2.3.4 starts it at a fixed
y 33 with no heightmap projection, just above the y 31 lava sea, so it can sit inside netherrack or hang over lava.
Before it goes live: make sure earth fill treats lava under the base like water (cancel over the lava sea); if it
doesn't, add that (`EarthFill` terrain kind) or give it a `surface_check`-style ground test. ST-186.
The pig ranger mini-boss (a function, not a structure) spawns anywhere in the Nether; keep it or drop
`loop_120s` from the port (D7).

#### 7.4.7 Shared steps for every new structure

1. `python3 tools/gen_structure_config.py` (rescan), `mod/eugene` + `mod/campsite_structures`, `type/*`, `group/*`
   tags; `python3 tools/gen_structure_groups.py` (sync) then `--fit-rates`. The mod's own sets end emptied (their
   `minecraft:villages` exclusions go with them; the groups' own exclusions replace them).
2. **FitTest count** is hardcoded (167 structures / 30 fits): raise it by every structure added to `land/*`.
3. `water_ok` for none of these; `surface_check` for none (no below-ground entrances found).
4. Earth fill: nothing to configure; every new structure's `[earth-fill]` line is read once with `log = ALL` (ST-176).
   Water fill: none opt in.
5. Peaceful: `eugene:camp`, Campsite camps (remote, 3), red roof castle (big settlements, 8). Hostile: undead castle
   (surface_dungeons), pillager camp + fake trader camp (pillager_settlements), every vehicle. Neutral: drum tower,
   piglin camp. The report's counts move by exactly these; record them in ST-170.
6. Lootr: the mod uses vanilla chests (nothing to swap); Campsite templates checked for modded containers.
7. Regenerate `structures_by_biome.md`, `structure_group_estimates.md`, `loot_catalog/`; update 2.2 (groups) and
   3.16 (shares).

#### 7.4.8 Testing rare structures: loosen soft rules only

When a rare structure is too slow to find (the 120 s `/locate` timeout, ST-158/164), loosen its **soft** rules for
the test run only: `frequency`, `weight`, `share`, group `spacing`/`separation`, or `[worldgen.debug]
structureDensity`. **Never** loosen correctness rules to make a test pass: biome fits, `biome_rules` (water,
`surface_check`), the peaceful radius, placement priority overlaps, earth fill and water fill limits. Note the
loosened values in the test's result line and put them back after.

Since 2026-10-30 the loosened values live in `kubejs/generation_scripts/worldgen/structure_test_overrides.json`, read
only with CEConfig `[worldgen.debug] testOverrides = true`; the applier refuses any non-soft key (3.27). A tester
turns the switch on in the scratch toml and nothing else.

#### 7.4.9 Future metas with vehicles (built 2026-10-30, 3.31)

- `meta/witch_cart_caravan`: witch wagons in a line, maybe beside a witch hut (swamps, dark forest).
- `meta/pillager_caravan`: loot carriages and prison vans with a siege weapon or a pillager camp.
- `meta/trader_caravan`: passive, trader camps along a road (way sign).
Each gets its own `meta/*` tags (not `meta/siege_weapons`), so the existing pillager metas don't change.

#### Decisions (defaults used unless you say otherwise)

| # | Question | Default |
|---|---|---|
| D1 | Fortified-town meta shares | DECIDED (user): camps 0.20 / ruins 0.20 / town_villages 0.40, 20% plain; raised 2026-10-30 (user, `/locate` within 300 s): camps 0.25 / ruins 0.25, 10% plain (3.27) |
| D2 | Fake trader camp: hostile or passive | hostile (`pillager_settlements`, no meta tag) |
| D3 | Which biomes count as harsh | the `harsh` list in 7.4.5 |
| D4 | Per-biome rate for vehicles | new `biome_frequency` group knob (code), harsh 1.0 / default 0.5 |
| D5 | Red roof castle's lone zombie | stripped from the template |
| D6 | Drum tower's archaeology blocks | plain gravel/sand, loot lost |
| D7 | Pig ranger mini-boss (Nether, not a structure) | keep, with the `ride` rewrite |
| D8 | Drum tower in `meta/large_ruins` | no (size, keeps metas unchanged) |

### 7.5 Plan: earth fill on water, camp plinths, metas off coasts (2026-10-19, BUILT + scratch-tested 2026-10-19, DEPLOYED with 1.0.9 2026-10-19)

User, 2026-10-19: "address the structure earth fill issues i outlined in my scratchpad. move the note to the structure
tuning and work on it there. water replacement cancel should also have a check if the number of water blocks converted
is a significant (configurable, go with 50% for now) portion higher than the solid blocks replaced or left alone by the
footprint. a certain mods tent structures are consistantly spawning with 3-4 block high footprint sticking out of the
ground with the tent on top. also include a disabled by default config switch to turn off metastructure generation in
any coast/river biome. test these changes as well as the quark spire tp on a scratch server".

The note, moved here from `todo/change_scratchpad.md`:

> - structure earth fill issues
>     - farmers camp
>         - some structures spawn on water while missing dirt blocks in footprint
>         - some structuers (tents) spawn with 4 high dirt column elevating structure
>             - single tents also spawn with oversized base
>     - camp ruins metastructure spawning w/ islands in water
>         - prevent metastructures from spawning on coast biomes?
>         - stricter %water vs %solid blocks replaced by structure beard/base?

**Findings.**
- Water: `maxOverWater` only counts water deeper than the beard's reach (6), so a structure on a shallow lake passes
  with "0 over water" and the beard turns the lake under it into an island. Scratch: a fisher campsite with its whole
  footprint on water (169 water / 0 ground) was kept by the old check.
- Tents: the plinths are Campsite Structures' camps (the farmers camp meta's friendly camps include them). Their
  templates are a dirt dome 4 layers deep under a grass layer; the port's start height -5 sinks it as the old features
  did (verified in the feature bytecode), but the beard flattens the ground to the template's bottom layer, so it dug
  the surface down around the dome (verified in the mapped 1.19.2 Beardifier and JigsawPlacement bytecode). The single
  camps (not in a meta) do the same, hence the "oversized base": the 13x13 dome under a small tent.
- The other tent mods (villager tents, snowy tent, circus tent) have a 1-layer base and start height 0, so they don't
  have this problem.

**Changes.**

| # | Change | Where |
|---|---|---|
| 1 | Footprint water share: water vs dry ground in each earth piece's box at its surface layer, cancel past `maxWaterSurplus` 0.5 (3.13). "Converted" = filled by earth or the beard, so water under the footprint counts whether or not the template has a block there; buried spots count as neither | `worldgen/EarthFill` (`footprint`, `Layer`, `Footprint`, `Terrain#top`), CEConfig `[worldgen.earth_fill] maxWaterSurplus` |
| 2 | Sunk starts: the beard levels at the projected surface for structures in `terrain_adaptation/sunk_start` (3.15); the 6 campsites tagged, start height -5 -> -4 | `worldgen/TerrainAdaptation` (`sink`, `sinkOf`, `Shape.sink`), `terrain_adaptation.js`, `kubejs/data/campsite_structures/worldgen/structure/*.json` |
| 3 | `avoidCoasts` (off by default): metas stay out of coast and river biomes, water metas exempt (3.16) | `worldgen/MetaStructure` (`generate`/`placeStructures` take the avoided biomes, `reaches`, `avoidsCoasts`), CEConfig `[worldgen.meta_structures] avoidCoasts`, `biome_fits.json` biome_tags `coast_river`, `structure_tags.json` `meta/coast_ok` |

Offline: `test_worldgen_tools.sh` all 10 sections pass (earth fill 64 checks, 9 new footprint and burial checks);
`test_structure_groups.sh` node/Rhino parity identical.

**Tests** (scratch dedicated server, seed 8675309, structureDensity 4, earth fill `log = ALL`, meta log on; log in
[testing/structures/12_earth_fill_camps.md](testing/structures/12_earth_fill_camps.md)):
- [x] **ST-199 Camp plinth gone [scratch]:** the armorer campsite at chunk [-7, -9]: old jar, its grass layer at y 94
  over a ring dug to y 90; new jar (start height -5) grass at 94 inside a ring at 95; start height -4: grass at 95,
  the ground around at 95. *PASSED 2026-10-19.*
- [x] **ST-200 Sunk starts load [scratch]:** `[terrain-adaptation] ... 6 sunk starts`, no "ignored" warning. *PASSED.*
- [x] **ST-201 Footprint share cancels on water [scratch]:** the fisher campsite at [-36, -7] (169 water / 0 ground,
  kept before) cancelled "too wet"; inland run 14 share-only cancels, a cancelled drum tower's spot probed: open water.
  *PASSED.*
- [x] **ST-202 No underground cancels [scratch]:** first build cancelled Philip's ancient dungeon (bury) and
  Subterrestrial's cabins (seabed and aquifer water deep underground); after the burial rule none in 972 checked
  starts. *PASSED (after the fix).*
- [x] **ST-203 avoidCoasts off = unchanged [scratch]:** `/place structure` of farmers_camp, house_camp, ruin_scatter at
  the spawn beach (-80 -16) builds all three (farmers camp anchored on a beach fish pond). *PASSED.*
- [x] **ST-204 avoidCoasts on [scratch]:** after a restart (not reloaded live), the same three at the beach: "not
  placed, no anchor fits"; at 2144 -2592 (nearest river 101 blocks) ruin_scatter, house_camp, farmers_camp,
  ruin_cluster and waysign_camps all build; `meta/coast_ok` holds the 6 water metas and a driftwood flotilla still
  generated. *PASSED.*
- [x] **ST-205 Quark spire teleport [scratch]:** an armor stand `tp`ed to 300016 100 300016 in the Overworld: 4 ms, no
  region file at the target. The same in the End (hook kept there): 38 s server stall, the target region generated.
  *PASSED (the mixin from plan_1.0.9.md).*

**Still open.**
- The End keeps Quark's hook: a `/tp` into unexplored End terrain still generates the target synchronously (38 s on
  the scratch server). Option: skip the hook when the target chunk isn't loaded, in every dimension.
- Philip's sunk-start structures and the T&T nilotic village (3.15): look in game before tagging them `sunk_start`.
- `maxWaterSurplus` is a first value: look at a few share-only cancels in game (`log = ALL`), villages included
  (a village on a lake goes as a whole; pruning only drops pieces over deep water).
- `avoidCoasts` near the coast also stops metas whose satellites can't find a spot clear of a beach or river (they
  fall back to the plain anchor). Switched on 2026-10-30 (user decision 2026-10-29), accepting that side effect.

### Planned from the scratchpad (2026-10-24)

Briefs: [briefs/structures_earthfill.md](briefs/structures_earthfill.md), [briefs/blocks_new.md](briefs/blocks_new.md). Nothing here is built.

- [TODO](todo/TODO.md) `structures/earthfill #11`: desert structures (pyramids) spawn in water. The row's `maxWaterSurplus` cancels a start when water
  outnumbers ground, so raising it keeps more flooded starts; the knob that fills water is `maxOverWater` (24), which already has
  per-structure and per-tag overrides (`<id or #tag>=water,air,mound`). Plan: one override on `group/desert_temples` and the other desert
  groups; extend the override with a surplus value only if the share is what cancels. Open: the value and the scope.
- [TODO](todo/TODO.md) `blocks/new #12`: rare ice hidden and configurable. The Rare Ice mod's block has no item, sits in `#minecraft:ice`, and
  already has `probabilityOfRareIce` in `config/rare-ice.properties`; its biome modifier can be overridden by a datapack. Open: what
  "hidden" means (recommended: rarer and cold biomes only, out of the JEI world tabs; keep it in the ice tag).

## 8. In-game test suite

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-DW-01, -02, -03; rerun pending (batch fault) SQ-LE-06.

### 8.0 Scratch-server run order, easiest to hardest (2026-10-12)

Every pending structure-generation test, ST- (below) and LO- ([locate.md](locate.md) 7), in the order to run them:
cheap log checks first, then single commands, config reboots, batch searches, pregenerated areas, calibration runs,
two-world comparisons, the flat-world `/cdevplace` run and the profiling run last. Each phase is one server setup; a
phase only starts when the one before it is clean. Tests that need eyes in a client follow at the end (8.0.11).
Tests marked **[scratch]** below are run by me on the scratch server; the rest of each phase works there too
through the log, reports, `/locate`, `/place` and scratch KubeJS probes (block and biome reads). ST-11 and ST-18
passed before; ST-77 is superseded by ST-93.

**8.0.0 Setup** (no tests):
- Scratch dedicated server as in the scratch-server recipe (Trash `CreEngServer` libraries, instance `kubejs/` and
  `config/` copied, mods symlinked minus client-only rows), but with
  `development/creativeCommon/build/libs/creativecommon-1.0.6.jar` instead of the older jar in `mods/`.
- `server.properties`: `level-seed=8675309`, `max-tick-time=-1`, a new `level-name` per world below.
- Toml: `[worldgen.meta_structures] log = true`, `[worldgen.placement_priority] log = true`; everything else at
  the defaults.
- `<world>/serverconfig/asynclocator-server.toml` written before boot with `asyncLocatorThreads = 4` (decision 3
  in locate.md).
- Back up `kubejs/exported/worldgen/structuregen.json` before the first `Dump`.

**8.0.1 Phase 1: boot and logs** (world A, one boot, no commands). One pass over `latest.log`, the reports and the
toml:
1. LO-16: boots, no mixin apply errors (and none for ST-27 and ST-61's mixins).
2. Load lines, newest counts win: ST-153, ST-140, ST-134, ST-127, ST-124, ST-117, ST-112, ST-102, ST-93, ST-84,
   ST-69, ST-17. The latest values (15 groups, 3 replacements, 22 metas of which 21 take slots, 30 fits / 181
   structures, 242 ranked, 104 peaceful / 70 hostile; since 7.4, 2026-10-14; before it 14 groups, 167, 228, 96 / 65) replace the counts the older tests name; for those, check that
   there are no errors and the report sections exist. A count that differs is compared with
   `structure_tag_report.md` before it counts as a failure.
3. Systems and sections: ST-33, ST-44, ST-51, ST-56, ST-133 (toml part), ST-49 (pool report), ST-149 and ST-150
   (template swap lines).
4. After the first chunks: ST-103, then `Dump` + `python3 tools/check_structure_live.py` (re-run of ST-18 on the
   current groups).
5. ST-161 (the earth-fill toml keys, 7.3).

*Phase 1 run 2026-10-13 (world `st_a`, boot 89 s): every Phase 1 test passed or passed its Phase 1 part (ST-17, 33,
27, 49, 51, 133, 149 have client/later parts left). Load line: `14 groups, 167 structure_sets written (133 emptied into
groups), 180 original sets; 110 structure frequencies below 1; 1 biome rule(s); 30 biome fit(s) for 167 structure(s);
22 meta structure(s); 3 replacement(s); 228 ranked structure(s), 1 companion rule(s)`. No mixin apply errors (any
mod), no warnings from our loggers, no KubeJS script errors. For later phases: (a) during `/locate` some starts are
assembled twice or 4 times (same chunk, same worker, about 2 s apart: `[meta-structures]` and `[earth-fill]` lines
doubled; boot-time starts log once). Look at this with LO-07/LO-08. (b) the 38.8 s plains-village `/locate` (10
misses) had the server 9.1 s behind once (LO-05). (c) `pool_tag_report.md` warns `advancedperipherals:*` matches no pool
piece. (d) 15 jars have no `mod_sides.md` row; the scratch server took them all except LavaClearView and jepp
(client-only).*

**8.0.2 Phase 2: single searches** (world A, console commands, a scratch KubeJS biome probe). Each answer is one
`/locate`; the biome checks read the biome at the answer:
1. Removed and blocked: ST-15, ST-03, ST-10 (the locate part), ST-16 (`/loot`).
2. One per group and member: ST-19, ST-04, ST-05, ST-22, ST-26.
3. Replacements and folds: ST-104, ST-105, ST-108, ST-125, ST-118, ST-119 (Nether), ST-131, ST-132.
4. Biome fits: ST-82, ST-100, ST-94, ST-95, ST-96, ST-97, ST-99, ST-78, ST-79, ST-80 (End), ST-81.
5. Water and entrances: ST-113, ST-115, ST-45 (the locate part).
6. Metas and parts: ST-85, ST-86, ST-87, ST-70 (locate + log line), ST-73 (locate), ST-135, ST-136, ST-120,
   ST-121, ST-143, ST-157 (first half).
7. Way signs: ST-128, ST-129.
8. Search counters, with the searches above as data: LO-07, LO-08, then LO-13 (the folds and log switches apply
   live, no reboot).
9. The 7.3 fixes (rerun after the 2026-10-13 jar): ST-163, ST-164, ST-158, ST-135, ST-143, ST-118, then ST-165 and
   ST-166 last (the second ends the boot).

*Phase 2 run 2026-10-13 (world `st_a`, same seed and jar, 4 locator threads, 4 boots; world spawn -80 76 is open
ocean, so many searches ran 1,000 to 9,000 blocks). About 150 `/locate`s, each answer's chunk probed for its starts and
biome (results inline). Passed: ST-03, 15, 19, 05, 22, 26, 104, 105, 108, 125, 119, 131, 132, 82, 100, 94, 96, 97, 78,
81, 113, 85, 86, 87, 121, 128, 129, LO-07, LO-13; scratch parts of ST-10, 16, 04, 95, 45, 70, 73, 157, LO-08. Found:*
- *(a) **Earth fill cancels big structures that have a beard.** Every fortified-village anchor of a meta assembled
  during the searches (49 of 49), every Takes A Pillage camp siege (3 of 3), 7 of 9 bastille sieges, 42 of 103 T&T
  old-growth-taiga outposts and 13 of 37 Farmer's Structures were cancelled, nearly all at `257+ over air (limit
  256)`. These structures use `beard_box` (Takes A Pillage) or `beard_thin` (Terralith, T&T, Farmer's, vanilla
  villages), which fills under them anyway; earth fill counts the earth over air before the beard and the limit is
  an absolute count, so a large base trips it at a few percent. Hence ST-135 and ST-143 fail, and ST-118's camp
  (7273 blocks, 178 misses) and bastille (none in 300 s) are far rarer than their 1 per 91 / 146. Earth fill shipped
  with 1.0.4. Also: the 1.0.6 default for `maxOverAir` is 128 (commit 52b2c63) while its comment and 3.13 say 256;
  the instance toml keeps 256.*
- *(b) **Deep structures can't start since the 7.2 fits.** `land/any` lists surface biomes, but
  `betterdungeons:skeleton_dungeon` (start y -50..-30), `philipsruins:bone_dungeon` and `philipsruins:lost_soul_city`
  (y -40) check the biome at their start, which is `terralith:cave/deep_caves` (8 of 8 columns probed at y -40):
  all three "Could not find" (ST-115).*
- *(c) **`/locate` of a structure inside metas never gives up.** `MetaSlots.beginLocate` adds every meta that can
  contain the target, and `locateAt` loads every slot where any of that meta's anchors fits: the End tower searched
  from the Overworld ran past 900 s instead of failing (ST-80), the desert tower and bastille crawled the same way.
  Async Locator can't cancel a search, so 4 such searches take every locator thread, and `stop` waits for them
  (its pool threads are non-daemon): the server had to be killed twice.*
- *(d) Phase 1 lead (a) explained: a start logged twice is the placement-priority prediction plus the real
  generation (3.22 step 4); 3 or 4 times (about 6% of `[earth-fill]` lines) are prediction cache misses.*
- *(e) Not ours: two `AnalyzerException ... inexistant local variable 19` traces per boot are another mod's
  `@ModifyVariable` on `NoiseChunk` (Mixin logs and works around it).*
- *(f) Scratch tooling: a KubeJS command that reads an ungenerated chunk deadlocks the server (Rhino's lock vs a
  worldgen worker firing a MoreJS enchant event for Sophisticated Backpacks); forceload first. The same holds for
  any pack script that reads new chunks from the server thread.*

*Phase 2 item 9 (7.3 rerun) run 2026-10-13 (world `st_b`, same seed, 1.0.6 jar of 21:59, one boot, about 17 min): passed ST-161, 163, 164, 143, 165, 166; partly ST-158 and ST-118 (earth fill keeps the big bearded structures now; the TaP camp answers); ST-135 still open. Earth fill is no longer what blocks them: the bastille is dropped by the peaceful radius (3 of 4 kept ones) and the fortified metas are rare enough that a 120 s search (about 9 s a load with 4 searches running) ends first. Their rates belong to the Phase 6 census. Also: with 4 slow searches running, the server thread took 31 s to reach a console `stop` (LO-05).*

*M2 run 2026-10-14 (st_c density 1, st_d density 4, 4 boots): passed ST-170 to 175, 177 to 185, 187, 189 (some with
[game] parts left); partly ST-176 and ST-188; ST-186 skipped (Nether); ST-190 is [game]. Bugs fixed on the way: 1.20
pools without `name` (world would not load), `minecraft:all_of` predicate, Ser Marrow thrown off an untamed steed.
Open for you: the undead castle rarely survives earth fill (4 of 5 cancelled on slopes); earth fill can't see stone
bases (drum tower); census reports every slot "known". (Way-sign metas: 0 of 11 on st_c, but st_d saved 7.)*

**Gate before Phase 3 (2026-10-14):** build plan 7.4 (new structure mods, `vehicles`, fortified-town meta shares) and
run its tests, ST-170 to ST-190 (block "New structure mods before Phase 3" below), on a new world first. Rare ones:
loosen soft rules only (7.4.8).

**8.0.3 Phase 3: placing and probing** (world A, `/place`, `/loot`, `/reload`, KubeJS block probes):
ST-14, ST-47, ST-92, ST-130, ST-28, ST-30, ST-32, ST-31, ST-12 (piece list of 3 village starts), ST-149 (block
part), ST-133 (geode count in a new ocean area), ST-48 and ST-50 (driftwood on land vs water, counted).

*Phase 3 run 2026-10-14 (world st_d at density 4, plus st_e for the off switches; 2 boots plus one for st_e):
passed ST-14, 47, 92, 130, 28, 30, 32, 31, 12, 149, 48 (some with [game] looks left); partly ST-133; failed ST-50.
Two bugs in older systems, not fixed: ground match gives up silently over deep structures (sampling skips every
column under any piece, whatever its height), and the beach/river/swamp driftwood water versions can never place
(the UA feature needs dirt/sand under its origin outside oceans). Tuning: a 10-block geode drop does little over deep
ocean floors near y 0.*

**8.0.4 Phase 4: config switches** (one reboot each, back to defaults after each; a new world where the test asks
for one). Log-only first, then the ones that need a search:
1. ST-46, ST-37, ST-38, ST-55, ST-60, ST-65, ST-75, ST-83, ST-24.
2. ST-36, ST-109, ST-21, ST-23 (+ `Dump`), ST-25, ST-13.
3. ST-35, ST-34 (density x0.25, then x4).
4. ST-76 only if an old jar is ever in `mods/` again (skip otherwise).

*Phase 4 run 2026-10-14 (density 1; worlds st_d, st_e and a new st_g; 10 boots, the log-only switches batched in
one): passed all but ST-65 (partly: the template-trim half needs a third world, left for [game]); ST-76 skipped (no
old jar). No bugs found. Counts that changed since the tests were written: 22 metas, 15 groups, 214 sets at x4,
boats 48/28.*

**8.0.5 Phase 5: batch searches** (fresh worlds, same seed, 4 locator threads):
1. World L1, folds on, first thing after boot: LO-02's batch (`/cwglocate batch all` from spawn).
2. World L2, `foldPlacement` and `foldBiomes` off: LO-01's batch; compare with L1 (same positions, more misses).
3. World L1 again: LO-06 (two batches at once), LO-15 (timeout and stop), LO-05 (tick times during a high-miss
   `/locate`, `nonBlockingLoads` on vs off).

*Phase 5 run 2026-10-14 (worlds st_l1 folds on, st_l2 folds off, st_l3 one locator thread; a second session
redid the ground-truth step after the first one's scratchpad was lost): LO-02, LO-06 (adapted: a second parallel batch
is refused by design), LO-08 and LO-15 passed; LO-01 partly (42 rows time out at the 120 s default); LO-05 FAILED: the
non-blocking load path still blocks the server thread (LocateLoads.java:82), so searches stall ticks for seconds per
load. Also found: Cloud Storage and Domestication Innovation village houses lose their pieces when a structure_starts
chunk reloads (static ProcessorLists holder), and vanilla desert/savanna village anchors of meta/village_towers get a
slightly different layout in each JVM run. Log: testing/structures/05_phase_5.md.*

**8.0.6 Phase 6: a pregenerated area** (world P: Chunky radius 96 chunks around spawn, then the checks):
1. LO-03 and LO-14 and LO-04: `/cwglocate census 96` (false negatives 0, miss reasons, member shares).
2. From the pregeneration's log: ST-141, ST-154, ST-114.
3. From the census and probes: ST-106, ST-107, ST-20, ST-72, ST-91, ST-98, ST-101, ST-74, ST-142 (blocks at 3
   logged drops), LO-12 (columns below y 48 in full chunks).
4. The census member shares feed the weight-stripping Todo (7).

*Phase 6 run 2026-10-14 (world st_p: Chunky square radius 96 chunks at 2144 -2592, 92% land instead of the 76% ocean
spawn, 37249 chunks in 64 min; the census ran on a separate fresh square at -1700 1500, 90% land, because a census over
generated chunks only counts "known"): ST-141, 154, 107, 20, 74, LO-12 passed; ST-142 and ST-114 passed their scratch
parts; LO-04 partly (shares written, comparison skipped as a rate); the rate tests (ST-72, 91, 98, 101, ST-106's ratio)
skipped. LO-03 and LO-14 FAILED on a census bug: slots run set by set, the first set that tries a chunk loads it and
every later set there counts as "known", so predictions, misses and reasons cover only that first set. Log:
testing/structures/06_phase_6.md.*

**8.0.7 Phase 7: calibration runs** (heavy logging, density x4, new worlds):
ST-52 (earth fill `log = ALL`, with ST-159, ST-160, ST-162, ST-167 and ST-168 from the same lines), ST-169, ST-57 (water fill `log = ALL`), ST-67, ST-53, ST-58, ST-54, ST-59.

*Phase 7 run 2026-10-19 (scratch server rebuilt from the Minecraft1192withForge libraries; patcherMod 1.0.7 jar because 1.0.8 crashes a dedicated server at mod loading (Serene Seasons clock mixin references the client `Minecraft` class), worldgen byte-identical; density 4; main square Chunky r 800 at -1400 -2592 in two worlds plus switch boots): ST-162, 159, 169, 67, 54, 58, 59 passed; ST-53 passed except its "slot stays empty" clause (group slots fall through to the next member); ST-160 partly (small villages log `small`; overrides pass); ST-52 and ST-57 data in the log. ST-167 FAILED as shipped: `earth_fill/prune` is empty because `type/village` is not a real structure tag, so pruning never runs; with a scratch-only tag fix ST-167 and ST-168 pass (village cancels 4 -> 2). Also found: pruning can drop a start a size class and tighten its limits; `/dumpstructuregen` broken since 10-14..10-18. Log: testing/structures/07_phase_7.md.*

**8.0.8 Phase 8: two-world comparisons** (same seed, two fresh worlds each, Chunky over the same square):
ST-144 (approached from opposite sides), ST-148 (shaping on vs off), ST-157 (second half: peaceful on vs off),
ST-145 (`placementPriority` on vs off, overlap count).

*Phase 8 run 2026-10-19 (seven fresh worlds, same seed, density 4, as shipped: pruning dead; areas r 400 at -1400 -2592, r 256 around a town at -3727 -3564, r 400 at 3450 -1100): ST-144, ST-157 (second half), ST-145 (rate skipped) and ST-148 passed. ST-148 needed the town companion distance pulled in to 64-96 blocks: at the real 112-160 no village reached a town in 8 towns. No bugs. Log: testing/structures/08_phase_8.md.*

**8.0.9 Phase 9: flat world** (superflat, structures off): ST-39, ST-41, ST-42, ST-40 (every Towns & Towers
structure placed; the longest step).

*Phase 9 run 2026-10-19: ST-42 passed, ST-40 passed its scratch part (55 of 56 placed on hills and on a flat world, signs, no overlaps, illagers; walking the rows is [game]), ST-39 partly and ST-41 FAILED: the `/cdevplace templates` mode throws because vanilla `listTemplates()` dies on a `structures.nbt` path a resource-manager mixin adds. Also: no flat world can be created while `[worldgen.systems] biomeLayout` is on (its overworld dimension override outranks presets and world datapacks; Terralith/Tectonic override too); recipe in the log. Log: testing/structures/09_phase_9.md.*

**8.0.10 Phase 10: profiling** (JFR on the scratch server): LO-10 (per-status chunk generation times of a Chunky
pregen radius 64, written into locate.md 3.7).

*Phase 10 run 2026-10-19 (instance toml at density 1, Chunky radius 64 chunks at 2144 -2592, 16,641 chunks in 24.5 min, JFR as server/LaunchServer.sh): LO-10 passed; per-status times are in locate.md 3.7 (noise 38.8%, features 24.4%, light 18.8%, structure starts 6.4% though rebuilt about 16 times per chunk). Pack-side hot spots: the earth-fill terrain prediction (about 4% of worldgen CPU) and twigs' obelisk check (3.1%). Log: testing/structures/10_phase_10.md. The scratch run (8.0.1 to 8.0.10) is complete.*

**8.0.11 After the scratch run: in a client** ([game]; the scratch run first narrows down where to look):
ST-27 (grass on grass), ST-29, ST-43, ST-62, ST-63, ST-64, ST-66, ST-68, ST-68 b, ST-71, ST-88, ST-89, ST-90,
ST-70 and ST-73 (looks), ST-45 (fly the ocean), ST-10 (eye of ender), ST-110, ST-111, ST-116, ST-122, ST-123,
ST-126, ST-137, ST-138, ST-139, ST-146, ST-147, ST-151, ST-152, ST-155, ST-156, LO-09, LO-11.


**Meta structures** (2026-10-11, 3.16). Install the jar first (`./gradlew installToMods` from the instance root with the
game closed), set `[worldgen.meta_structures] log = true` for the first run, rejoin, and use new chunks.
- [x] **ST-69 Loads clean:** the world loads; `latest.log` has `[structure-tuning] ... 1 biome rule(s); 2 meta structure(s)` and no `meta_structures.json` warning; the report (`structure_tag_report.md`) has a `## Meta structures` section with both marked built, in groups `settlements` and `flotsam`; the toml has `systems.metaStructures` and `[worldgen.meta_structures] log`. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED (counts now 22 metas): all 22 built, toml keys there, no meta_structures.json warning.*
- [ ] **ST-70 Village with towers:** `/locate structure creativeengineering:meta/village_towers`, go there. A normal village with 1 to 3 watchtowers or a clock tower just outside it (not touching houses or roads), each in a biome it belongs in. The log line `[meta-structures] ... anchor ... at x y z, <tower> at x y z` gives the positions. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): locate + log part PASSED: `meta/village_towers` answers from 4 positions (ST-81), log lines name the village and its tower(s), e.g. village_snowy at 5039 156 4803 with a fresh watchtower at 5065 124 4745. Looks are a client check.*
- [ ] **ST-71 Towers look as on their own:** the towers have no square pad or notch (trimmed and soft-bearded like a lone tower, ST-63), villagers inside; the village pieces have the usual village beard.
- [ ] **ST-72 No extra villages:** fly through plains/desert/forest for a while: villages are about as common as before; some of them have towers.
- [ ] **ST-73 Flotilla:** `/locate structure creativeengineering:meta/driftwood_flotilla`, go there by boat. 8 to 16 driftwood logs floating within about 36 blocks, none on land or in shallows; sometimes a villager raft in the middle. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): locate part PASSED: `meta/driftwood_flotilla` at 240 -464 (deep lukewarm ocean), start box 57 x 57. Boat visit is a client check.*
- [ ] **ST-74 Not near shore:** a flotilla never sits mostly on a beach (a spot near land generates fewer logs, or none: the meta fails below 8). *2026-10-14 scratch phase 6 (structure_tuning 8.0.6, seed 8675309, 1.0.6 jar of 12:44, density 1, world st_p: Chunky square radius 96 chunks at 2144 -2592, census at -1700 1500; log testing/structures/06_phase_6.md): PASSED on the scratch server: 7 flotillas in the square, 11 to 16 logs each; every log piece floats over water 6+ blocks deep, none on a beach (2 sit in a plains-biome water body).*
- [x] **ST-75 Switch:** `[worldgen.systems] metaStructures = false`, rejoin: the report marks both "not built (switched off ...)", the log line says `0 meta structure(s) (2 left out)`, and new chunks only get plain villages. Back on afterwards. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED: `0 meta structure(s) (22 left out)` and a WARN; the report marks the metas "not built: switched off"; no meta start in new chunks.*
- [ ] **ST-76 Old jar:** (only if an older patcherMod is ever in `mods/` again) the world still loads; the log warns `meta structures not built: patcherMod has no worldgen.MetaStructure`.

**More meta structures, slot conversion, /locate of parts** (2026-10-11 evening, 3.16). Install the jar first
(`./gradlew installToMods` from the instance root with the game closed), set `[worldgen.meta_structures] log = true`,
rejoin, new chunks.
- [x] **ST-84 Loads clean:** `latest.log` has `[structure-tuning] ... 15 meta structure(s)` with no `meta_structures.json` warning, and (on the first chunk generated) `[meta-structures] 14 meta structure(s) take slots from ... anchor structure(s)`. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED (counts now 22 / 21).*
- [x] **ST-85 Locate a meta:** `/locate structure creativeengineering:meta/ruin_cluster` (then `ruin_scatter`, `pillager_tower_siege`, `pillager_stronghold_siege`, `shipwreck_graveyard`, `raft_convoy`, `pillager_fleet`), go there: the anchor at the given spot with its parts around it, as the `[meta-structures]` log line lists them. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED: ruin_cluster (-704 608, brushland: anchor `philipsruins:ancient_towers` + 4 ancient ruins per the log line), ruin_scatter (0 1792, savanna), pillager_tower_siege (1136 -1312), pillager_stronghold_siege (-1792 2288; 2112 1072), shipwreck_graveyard (-384 -368, lukewarm ocean), raft_convoy (1552 1840, deep ocean), pillager_fleet (304 128, deep ocean, box 142 wide): each answer chunk holds that meta's start; the `[meta-structures]` lines list the parts.*
- [x] **ST-86 Locate a part:** `/locate structure illager_siege_weapons:catapult` or `watchtower_building:fresh_watchtower` from a place where the nearest one is part of a meta (the log shows it): the answer is the part's own position, not the meta's start, and the part is there. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED: `illager_siege_weapons:battering_ram` from 2101 1006 answers 2101 1006 (0 blocks), the part's own position inside a `meta/pillager_stronghold_siege` (log: ram at 2101 75 1006); likewise `mini_clock_tower` and `fresh_watchtower` answered off-grid part positions inside `meta/village_towers` (ST-78).*
- [x] **ST-87 Locate an anchor inside a meta:** `/locate structure philipsruins:level_three_ruins` near a ruin cluster finds the cluster's large ruin. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED: from -700 600 `philipsruins:ancient_towers` answers -709 603 (the ruin cluster's anchor, 9 blocks) and `philipsruins:ancient_ruins` -707 572 (one of its small ruins); this cluster's large ruin is ancient_towers, not level_three_ruins.*
- [ ] **ST-88 Ruins look right:** a ruin cluster's small ruins stand on the ground around the large one, none overlapping, each in a biome it belongs in (desert ruins in desert...).
- [ ] **ST-89 Siege camps:** a pillager tower with siege weapons: the weapons stand on open ground 24 to 64 blocks out, their illagers with them, no square pad (they are tagged `terrain_adaptation/none` + trim).
- [ ] **ST-90 At sea:** a pillager fleet: 2 or 3 ships apart from each other, wreckage floating and wrecks on the seabed around them; a raft convoy: 2 or 3 rafts close together.
- [ ] **ST-91 Rates unchanged:** fly a while through plains and forest: ruins, outposts and villages about as common as before; some of them now come in groups.
- [x] **ST-92 Place:** `/place structure creativeengineering:meta/ruin_scatter` in a forest builds a scatter of forest ruins (the anchor drawn by weight). *2026-10-14 scratch phase 3 (seed 8675309, 1.0.6 jar of 01:28, world st_d at structure density 4 with two Chunky squares; st_e for the off-switch comparisons; log feature_writeup/testing/structures/03_phase_3.md): PASSED: `/place` in a forest built a scatter (anchor forest ruin + 2 forest ruins); 5 generated scatters drew different weighted anchors (forest_ruins and Philip's ruins) with 2-3 parts.*

**Biome fits** (2026-10-11, 3.17). Install the jar first (`./gradlew installToMods` from the instance root with the game
closed), rejoin, use new chunks.
- [ ] **ST-77 Loads clean:** superseded by ST-93 (the `tower/*` fits are `land/*` since 2026-10-12; the counts are now 29 fits and 163 structures).
- [x] **ST-78 Terralith towers:** `/locate structure mini_clock_tower:mini_clock_tower` (and `watchtower_building:fresh_watchtower`) from a few places; some answers land in Terralith biomes (F3 shows the biome), all in biomes their fit lists. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED: mini clock towers at -1269 171 (brushland) and 4219 139 (beach), both parts of `meta/village_towers` villages; fresh watchtowers at -304 480 (beach) and -3609 4279 (`terralith:bryce_canyon`, inside a `meta/village_towers`). All in their fits, Terralith biomes among them.*
- [ ] **ST-79 Right wood:** find a few Mysticrift towers (`/locate structure mysticrift_pillagers_towers_aris:<wood>_pillager_tower`): each stands in a biome of its wood (open land by climate: spruce in the cold, oak or spruce temperate, acacia on mesas); no oak tower in Mirage Isles or a dark forest; sandstone towers only on sand (deserts, sand beaches). *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PARTLY: oak towers at -736 7680 (swamp) and 4384 -288, acacia at -3920 112 (brushland), birch at 2391 -3993 (forest, inside a `meta/pillager_great_tower_siege`), jungle at -2528 192 (sparse jungle): every probed one in a biome of its wood. **Desert tower: no answer in 900 s**, same cause as ST-80 (it is an anchor of `meta/pillager_large_towers`; the search goes through every meta slot).*
- [ ] **ST-80 End tower:** none in the Overworld (`/locate` there finds nothing); `/execute in minecraft:the_end run locate structure mysticrift_pillagers_towers_aris:end_pillager_tower` finds one on the outer islands. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): **FAILED (first half)**: `/locate` of `end_pillager_tower` in the Overworld gave no answer in 900 s instead of a quick "Could not find". The tower is an anchor of `meta/pillager_large_towers`, so `MetaSlots.beginLocate` adds that meta to the search and `locateAt` loads every Overworld meta slot (its prediction only asks whether any anchor fits) to look for an End tower that cannot be there; nothing checks that the asked-for structure's biomes exist in the dimension. The End half was not reached (all 4 locator threads were taken by such searches; the server had to be killed: AL's threads are non-daemon and `stop` waits for them).* *Second boot: End half PASSED: `/execute in minecraft:the_end run locate ...` found one at 1232 -368 (end highlands, outer islands) in 8 s.*
- [x] **ST-81 Villages get towers in Terralith:** `/locate structure creativeengineering:meta/village_towers` a few times; at least one is a Terralith-biome village (shield, yellowstone, brushland...). *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED: `meta/village_towers` from 3 positions: -1296 240 (brushland), 2000 1856 (`terralith:rocky_mountains`), -2208 1872 (brushland); all Terralith.*
- [x] **ST-82 Tags:** `/locate biome #creativeengineering:land/spruce` (was `tower/spruce` before 2026-10-12) finds a spruce biome. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED: `/locate biome #creativeengineering:land/spruce` answers `minecraft:beach` 78 blocks from spawn (beaches are in the spruce fit on purpose, see its note).*
- [x] **ST-83 Switch:** `[worldgen.systems] structureBiomeFits = false`, rejoin: the log says `off in CEConfig`, and the towers are back on their own lists (a mini clock tower only in plains). Back on afterwards. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED: `[structure-biome-fits] off in CEConfig`; mini clock towers located in plains and snowy plains, their own list.*

**Biome fits for every land structure** (2026-10-12, 3.17, plan 7.2). Data only: copy the `kubejs/` changes, rejoin,
use new chunks (a new world shows it best). F3 shows the biome at each answer.
- [x] **ST-93 Loads clean:** `latest.log` has `[structure-tuning] ... 30 biome fit(s) for 167 structure(s)` and `[structure-biome-fits] 30 fit(s), 167 structure(s) given fitted biome lists: ...` with no `no such biome` line; `structure_tag_report.md` lists the `land/*` fits. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED: 30 fits for 167 structures, no "no such biome".*
- [x] **ST-94 Villages in Terralith:** `/locate structure minecraft:village_snowy` (and `minecraft:village_taiga`, `towns_and_towers:exclusives/village_tudor`) from a few places: some answers in Terralith biomes (wintry forest, alpine grove, highlands, rocky mountains...), all in biomes of their families. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED: snowy villages at 5039 4803 (grove, from 2 positions, inside a `meta/village_towers`), taiga villages in `terralith:yellowstone` (twice) and `terralith:rocky_mountains`, Tudor at 1584 3072 (meadow); every answer in its family's fit.*
- [ ] **ST-95 Vanilla outpost by its wood:** `/locate structure minecraft:pillager_outpost` from a few places: every answer in dark forest, Siberian taiga, temperate highlands, lavender valley or Mirage Isles; none on plains or desert. Improved Pillager Outpost's base still shows up on about 1 in 8. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): locate part PASSED: 2 different vanilla outposts from 3 positions, both in dark forest (one's chunk centre falls in a river next to it, the tower's planks stand in dark forest); none on plains or desert. Searches are long: 8889 blocks / 390 s and 6708 / 297 s. The IPO share is ST-47 / a client check.*
- [x] **ST-96 Houses and ruins spread:** `/locate structure villager_forge:villager_forge`, `forest_ruins:ruin_1`, `saloon:saloon`: answers in Terralith biomes of their families (taiga/highlands, forest/plains, badlands). *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED: forge at 1616 2688 (plains = `land/plains`), ruin_1 at -1536 1136 (meadow; 2 box corners reach savanna plateau) and 2368 1616 (dark forest), saloon at -2224 -672 (wooded badlands) and -3424 4256 (`terralith:bryce_canyon`).*
- [x] **ST-97 Sky village over the sea:** `/locate structure skyvillages:skyvillage` from the middle of an ocean: the nearest one floats over water. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED: from 0 0 the nearest sky village is at 720 368 over deep ocean (808 blocks), from -2976 -2400 one at -3072 -2544 over deep ocean.*
- [ ] **ST-98 Towers unchanged:** Mysticrift towers about as common as before (the T&T outposts now share their biomes; `fit_keep` raised the tower weights to compensate).
- [ ] **ST-99 Held:** `philipsruins:start_nether_ruin` still only in its mod's 7 biomes. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): INCONCLUSIVE: `philipsruins:start_nether_ruin` from -368 396 gave no answer within 900 s (it is rare and only in 7 biomes; the search ran on until the restart). Retry from a position inside its biomes.* *Retry from 1616 2688 (plains, one of its biomes): no answer in 600 s either. It is weight 3 among ruins of weight ~200 (its 1 per 17 is alone in its biomes) and 12 of its starts were cancelled by earth fill during the searches. Recheck with the Phase 6 census.*
- [x] **ST-100 Tags:** `/locate biome #creativeengineering:land/badlands` finds a badlands biome; `#creativeengineering:land/sky` an ocean or land biome. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED: `land/badlands` -> `terralith:bryce_canyon` (1704 blocks), `land/sky` -> `minecraft:ocean` at spawn.*
- [ ] **ST-101 Crowding:** fly through plains and taiga: villages no denser than before (the group grid caps a biome), but the styles mix (vanilla, Terralith, T&T Tudor/Swedish in the same area).

**Replacements and `group/witch_huts`** (2026-10-12, 3.18, plan 7.1). Needs patcherMod 1.0.6 from 2026-10-12 or later
in `mods/` (`MetaSlots.stageReplacements`), the `kubejs/` changes and YUNG's witch hut config; new world. Marked
**[scratch]**: run on a scratch dedicated server at the end of plan 7.1 (log, report, `/locate`, a scratch KubeJS
biome/start check); **[game]**: needs eyes in a client.
- [x] **ST-102 Loads clean [scratch]:** `latest.log` has `[structure-tuning] ... 11 groups ... 1 replacement(s)` with no `replaces` error or `replacements not staged` warning; `structure_tag_report.md` has the `witch_huts` group (4 members, the swamp hut noted "replaces `betterwitchhuts:witch_hut`: takes 10%"), `surface_dungeons` without witch structures, and `minecraft:swamp_huts` under "Sets emptied into groups"; `creativeengineering-common.toml` gained `structureReplacements = true`. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED (now 14 groups / 3 replacements): witch_huts 4 members, swamp hut "replaces ... takes 10%", swamp_huts emptied, structureReplacements = true.*
- [x] **ST-103 Index [scratch]:** after the first chunks generate, the log has `[meta-structures] 1 replacement(s) take a share of another structure's slots` and no "never generates" warning for `minecraft:swamp_hut`. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED (now 3 replacements), no "never generates" warning.*
- [x] **ST-104 Vanilla hut locates [scratch]:** `/locate structure minecraft:swamp_hut` answers with a position (not YUNG's "Use /locate structure betterwitchhuts:witch_hut instead!"); the chunk there holds a `minecraft:swamp_hut` start and its biome is swamp, mangrove swamp, orchid swamp or ice marsh. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: `minecraft:swamp_hut` answers (no YUNG redirect); 4 searches from 4 positions found 3 different huts, each with a `minecraft:swamp_hut` start in mangrove swamp or swamp.*
- [x] **ST-105 The other three [scratch]:** `/locate structure betterwitchhuts:witch_hut`, `betterwitchhuts:witch_circle`, `villagesandpillages:village_witch` from 3 or more spawn-far positions: every answer in one of the four swamp biomes. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: witch hut, witch circle and witch village from 3000 3000, -3000 -3000 and 3000 -3000: 9 answers, every one in swamp or mangrove swamp at the chunk centre (one circle's chunk corner reads brushland, the centre and the box are mangrove swamp).*
- [ ] **ST-106 Split [scratch]:** in a pregenerated swamp-rich area (or 20+ `/locate`s from spread positions), vanilla huts are about 1 in 10 of all vanilla + YUNG's huts (anything from 1 in 6 to 1 in 20 is fine for a small sample), and no grid slot holds both. *2026-10-14 scratch phase 6 (structure_tuning 8.0.6, seed 8675309, 1.0.6 jar of 12:44, density 1, world st_p: Chunky square radius 96 chunks at 2144 -2592, census at -1700 1500; log testing/structures/06_phase_6.md): ratio SKIPPED (a rate, out of scope); "no grid slot holds both" holds (6 starts, no shared cell; 3 vanilla, 3 YUNG).*
- [x] **ST-107 Spacing [scratch]:** no two of the four witch structures start closer than 9 chunks (144 blocks); a witch hut may stand near a surface dungeon (separate grids). *2026-10-14 scratch phase 6 (structure_tuning 8.0.6, seed 8675309, 1.0.6 jar of 12:44, density 1, world st_p: Chunky square radius 96 chunks at 2144 -2592, census at -1700 1500; log testing/structures/06_phase_6.md): PASSED: 6 starts of group/witch_huts (3 vanilla, 3 YUNG), none closer than 9 chunks, no grid cell with two.*
- [x] **ST-108 Surface dungeons unaffected [scratch]:** `/locate structure betterdeserttemples:desert_temple`, `betterjungletemples:jungle_temple`, `minecraft:mansion` still answer as before; `/locate structure minecraft:desert_pyramid` still finds nothing (still blocked). *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: YUNG's desert temple (-4336 -432, red oasis), jungle temple (-1728 -432, sparse jungle) and the mansion answer. The last clause is out of date: `minecraft:desert_pyramid` now answers on purpose (a 10% replacement since 3.18, see ST-125).*
- [x] **ST-109 Switch [scratch]:** `[worldgen.systems] structureReplacements = false`, new world: the log warns `replacements not staged: switched off ...`, the report marks the swamp hut "replacement left out", `/locate structure minecraft:swamp_hut` finds nothing, YUNG's hut still locates. Back to true afterwards. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED (new world): WARN `replacements not staged: switched off ...`, report "replacement left out" for the swamp hut, `minecraft:swamp_hut` not found, YUNG's witch hut found 431 blocks away.*
- [ ] **ST-110 Vanilla hut looks right [game]:** visit the ST-104 hut: on stilts over the swamp, a witch and a black cat; YUNG's huts nearby unchanged.
- [ ] **ST-111 Spire [game, optional]:** Terralith's spire still shows up in ice marshes (1 per 24 instead of 25 there and everywhere else).

**Entrance dungeons** (2026-10-12, 3.19, plan 7.1). Same jar and new world as ST-102.
- [x] **ST-112 Loads clean [scratch]:** the log has `[structure-tuning] ... 12 groups` and `[structure-biome-rules] water: surface check for betterdungeons:spider_dungeon, betterdungeons:zombie_dungeon`, and the water rule's "kept out" list now includes both; the report shows `entrance_dungeons` (2 members) and the surface check line under the water rule. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED (now 14 groups): surface check line for spider + zombie dungeons; entrance_dungeons 2 members.*
- [x] **ST-113 Locate on land [scratch]:** `/locate structure betterdungeons:zombie_dungeon` and `betterdungeons:spider_dungeon` from 3+ positions, including the middle of an ocean: every answer has a land biome at the surface above it (scratch KubeJS biome check at WORLD_SURFACE). *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED: zombie and spider dungeons from 4 positions (-368 396, 48 -20 in deep ocean, 2400 1200, -2976 -2400): 6 different dungeons, every one with land above at WORLD_SURFACE (brushland, forest x2, windswept gravelly hills, savanna, highlands). One crypt's surface block is a lava pool (not water).*
- [ ] **ST-114 No wet entrance [scratch]:** with `log` at DEBUG for `creativeengineering/structure-biome-rules` (or by counting), some `surface check dropped a start` lines appear near coasts and rivers; at each found dungeon the WORLD_SURFACE block above the box centre is not water. *2026-10-14 scratch phase 6 (structure_tuning 8.0.6, seed 8675309, 1.0.6 jar of 12:44, density 1, world st_p: Chunky square radius 96 chunks at 2144 -2592, census at -1700 1500; log testing/structures/06_phase_6.md): block part PASSED: the 11 entrance dungeons in full chunks have grass, leaves or stone above the box centre, never water. The "surface check dropped" count was not taken (DEBUG log; the census reasons are broken, see LO-14).*
- [ ] **ST-115 Others untouched [scratch]:** `/locate structure betterdungeons:skeleton_dungeon` and `betterdungeons:small_dungeon` still answer, including under oceans. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): **FAILED for the skeleton dungeon**: `betterdungeons:small_dungeon` answers from 3 positions (one under cold ocean at -3168 -3040), but `betterdungeons:skeleton_dungeon` is "Could not find" from all 3 (17-23 s). Cause: its `land/any` fit (7.2) lists surface biomes only, while it starts at y -50..-30, where the biome is a Terralith cave biome (`terralith:cave/deep_caves` in all 8 columns probed at y -40), so the biome check never passes. Same for `philipsruins:bone_dungeon` and `philipsruins:lost_soul_city` (start y -40, `land/any`). Fix in the fit, then retest.*
- [ ] **ST-116 Staircase on dry land [game]:** visit two crypts and a spider cave: the staircase / cave mouth comes out on dry land.

**Folded standalone structures** (2026-10-12, 3.20, plan 7.1). Same world as ST-102.
- [x] **ST-117 Loads clean [scratch]:** the log has `[structure-tuning] 13 groups ...` with no errors; the report lists the statues and cats in `ruins`, both It Takes a Pillage structures in `pillager_settlements`, `nether_ruins` with 4 members, the fossils in `ocean_ruins`, the fresh watchtower in `remote`; their old sets under "Sets emptied into groups". *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED (now 14 groups): statues/cats in ruins, both Takes A Pillage in pillager_settlements, nether_ruins 4, fossils in ocean_ruins, fresh watchtower in remote, old sets emptied.*
- [ ] **ST-118 Locate each [scratch]:** `/locate structure` finds `old_illager_statue:old_illager_statue`, `small_cat_statues:cat_1`, `takesapillage:pillager_camp`, `takesapillage:bastille`, `hopo:underwater_fossils`, `watchtower_building:fresh_watchtower`, each in a biome of its fit. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): 5 of 6 PASSED, each start in a biome of its fit: statue at 608 2640 (savanna plateau), cat_1 at -192 -1728 (sparse jungle), fossils at -192 672 (ocean), fresh watchtower at -304 480 (beach = `land/spruce`), camp at 4464 -5040 (brushland = `land/savanna`), but only after 178 misses, 7273 blocks and 269 s. **Bastille: no answer in 300 s** (the search went on loading chunks past x -9200). Both are far rarer than the planned 1 per 91 / 146; only 8 + 7 earth-fill cancels and 2 peaceful drops were logged against them, so most misses are slots another member won. Look at the shares with the Phase 6 census (LO-14), then retest.* *2026-10-13 scratch phase 2b (world st_b, seed 8675309, 1.0.6 jar of 21:59, 4 locator threads, earth fill `log = ALL`): camp now PASSED (found at 2368 -2896, 117 blocks, kept by earth fill); bastille still no answer in 120 s (peaceful radius drops, see ST-158).*
- [x] **ST-119 Nether ruins [scratch]:** in the Nether (`/execute in minecraft:the_nether run locate structure ...`) `twigs:bloodstone_obelisk` and `philipsruins:nether_lava_ruins` answer, no two Nether ruins closer than 8 chunks. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: in the Nether `twigs:bloodstone_obelisk` at -368 -208 (crimson forest) and `philipsruins:nether_lava_ruins` at 80 16 (nether wastes) answer, 28 chunks apart.*
- [ ] **ST-120 Camp metas on open land [scratch]:** `/locate structure creativeengineering:meta/pillager_camp_siege` answers, some time on plains or savanna with a Takes A Pillage camp as the anchor (`[worldgen.meta_structures] log = true`). *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PARTLY: `meta/pillager_camp_siege` answers (-272 3072, sparse jungle, anchored on a T&T sparse-jungle outpost), but every camp siege anchored on a Takes A Pillage camp (3 assembled during searches) was **cancelled by earth fill** (257+ earth over air, limit 256). Blocked by the earth-fill finding (see the summary under 8.0.2).*
- [x] **ST-121 Bastille siege [scratch]:** `meta/pillager_stronghold_siege` can be anchored on a bastille (log line names `takesapillage:bastille`; may need several locates). *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED (barely): 9 `meta/pillager_stronghold_siege` starts were assembled on `takesapillage:bastille`; 7 of them were then cancelled by earth fill, 2 kept.*
- [ ] **ST-122 Statues rare [game]:** fly over plains and forest: statues and cats are an occasional find, ruins as common as before.
- [ ] **ST-123 Pillagers [game]:** pillager outposts in plains about as common as before (10% fewer at most), with the odd palisade camp or bastille among them.

**Vanilla temples as replacements** (2026-10-12, 3.18). Same world as ST-102.
- [x] **ST-124 Loads clean [scratch]:** the log has `... 3 replacement(s)` and `[meta-structures] 3 replacement(s) take a share ...`; the report notes both vanilla temples as replacements in `surface_dungeons`. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED: 3 replacements, both vanilla temples noted as 10% replacements in surface_dungeons.*
- [x] **ST-125 Locate [scratch]:** `/locate structure minecraft:desert_pyramid` and `minecraft:jungle_pyramid` answer (no YUNG's "Use /locate ... instead" error), each in a desert/oasis or jungle biome; YUNG's temples still locate. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: `minecraft:desert_pyramid` at -6912 -15808 (17290 blocks, 19 s, desert spires = `land/desert`) and `minecraft:jungle_pyramid` at -2784 784 (rocky jungle = `land/jungle`), no YUNG redirect, starts there; YUNG's temples still answer (ST-108).*
- [ ] **ST-126 Look [game]:** a vanilla desert pyramid and jungle temple generate whole (traps, chests); YUNG's temples nearby unchanged.

**Way signs** (2026-10-12, 3.21). Same world as ST-102.
- [x] **ST-127 Loads clean [scratch]:** the log has `[structure-tuning] 14 groups ...`; the report shows `way_signs` (1 member, exclusion zone on `group/settlements`) and `supplementaries:way_signs` under "Sets emptied into groups". *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED: 14 groups; way_signs 1 member, exclusion zone group/settlements r8; supplementaries:way_signs emptied.*
- [x] **ST-128 Destination tag [scratch]:** a scratch KubeJS check (or `/locate structure #supplementaries:way_sign_destinations`) shows the tag holds `creativeengineering:meta/village_towers` and `terralith:fortified_village`; the locate answers. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED: `#supplementaries:way_sign_destinations` holds 36 structures, including `creativeengineering:meta/village_towers`, `meta/town_villages`, both fortified metas and both Terralith fortified villages; `/locate structure #supplementaries:way_sign_destinations` answers `meta/village_towers` at -1296 240.*
- [x] **ST-129 Signs locate [scratch]:** `/locate structure supplementaries:way_sign` answers on land, roughly as close as on the old world (about 1 per 20 x 20 chunks). *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: same 4 searches as ST-26, answers 160-641 blocks away (about 1 per 20 x 20 chunks), all on land (brushland, old-growth pine taiga, mangrove swamp).*
- [x] **ST-130 Sign points at a towered village [scratch/game]:** `/place structure supplementaries:way_sign` near a `meta/village_towers` village (found by `/locate`): the sign's text names that village's direction and distance. *2026-10-14 scratch phase 3 (seed 8675309, 1.0.6 jar of 01:28, world st_d at structure density 4 with two Chunky squares; st_e for the off-switch comparisons; log feature_writeup/testing/structures/03_phase_3.md): PASSED: a placed way sign reads "200" and its yaw (176.8) points exactly at a `meta/village_towers` start 197 blocks away.*
- [x] **ST-131 Aether / Undergarden houses gone [scratch]:** the report lists `farmers_structures:aethers_with_all` and `undergardens_sea_with_all` as written with every member disabled; `/locate structure farmers_structures:aethers` fails quickly instead of searching. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: the report lists both sets as "Every member disabled: written with no structures"; `/locate` fails without a search (aethers 4.2 s, 12.9 s while 3 other searches ran; undergardens_sea 0.3 s).*
- [x] **ST-132 Ocean fortress deep only [scratch]:** `/locate structure philipsruins:ocean_fortress` from 3+ ocean positions: every answer in a deep ocean biome (deep, deep cold, deep frozen, deep lukewarm); the log line `[structure-biome-fits]` counts 30 fits. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: from 4 ocean positions: one fortress at 400 304 (deep ocean, start there) found 3 times, and one inside a `meta/sunken_ruin_cluster` at -4316 -5588 (deep lukewarm ocean). The last search took 268 s for 1233 blocks. The fits line counts 30 (phase 1).*
- [ ] **ST-133 Ocean geodes lower [scratch/game]:** `creativeengineering-common.toml` has `systems.oceanGeodes` and `[worldgen.ocean_geodes]`; in a new ocean area (spectator, or a scratch KubeJS count of `minecraft:budding_amethyst` / calcite tops per y in pregenerated ocean chunks), far fewer geode shells break the sea floor than on land-equivalent depths, and the geode tops sit about 10 blocks lower than with `oceanGeodes = false`; prismarine geodes (budding prismarine) sit where they did. No `GeodeFeature` mixin errors in the log. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): toml part passed, no GeodeFeature mixin errors; ocean count in phase 3.* *2026-10-14 scratch phase 3 (seed 8675309, 1.0.6 jar of 01:28, world st_d at structure density 4 with two Chunky squares; st_e for the off-switch comparisons; log feature_writeup/testing/structures/03_phase_3.md): PARTLY: same chunks with the system on and off: ocean amethyst geodes 7-15 lower (mostly 9-11) in 38 of 42 chunks, 4 kept their height, land geodes unchanged; Geode Plus geodes lowered too (ocean-land gap 9-17 on vs 0-4 off; Geode Plus is not reproducible per seed, so only distributions compare). But shells reaching the sea floor barely change (14 of 80 ocean geode chunks on vs 16 of 71 off): this seed's deep ocean floors sit near y 0, so a fixed 10-block drop still leaves them breaking through (tuning question, open).* *2026-10-22: reworked to the lowest-floor cap (the dated 2026-10-22 ocean geode note above). Expected: moved ocean amethyst geode tops at or below y -10, shells reaching the sea floor in ocean chunks only from the 1 in 10 that keep their height; land and prismarine geodes unchanged; the toml has `lowestFloorY`, `clearance`, `spread` and no `drop`.* *2026-10-22 second bulk run: scratch part scratch server PASSED 2026-10-22 (SQ-GE-00 to SQ-GE-04; SQ-GE-03 after the prismarine fix above): toml keys right, 23 of 24 ocean geodes moved with tops 10+ blocks under the floor, 32 land geodes kept their height, prismarine kept, system off 0 moved. Left: the look of a real ocean floor, in game.*

**New and tweaked meta structures** (2026-10-12, 3.16). `[worldgen.meta_structures] log = true`, new world.
- [x] **ST-134 Loads clean [scratch]:** `[structure-tuning] ... 22 meta structure(s)` with no `meta_structures.json` warning; `[meta-structures] 21 meta structure(s) take slots from ...` (22 / 21 since `town_villages`, 3.22). *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED: 22 meta structures; 21 take slots from 155 anchors.*
- [ ] **ST-135 Fortified metas [scratch]:** `/locate structure creativeengineering:meta/fortified_ruins` and `meta/fortified_camps` answer; the log line lists a fortified village with ruins / camps 44 to 108 blocks out. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): **FAILED**: `meta/fortified_ruins` and `meta/fortified_camps` gave no answer in 300 s. 29 were assembled during the searches (out to x/z +-19000), and earth fill cancelled every one of their fortified-village anchors (over-air limit). See the summary under 8.0.2.* *2026-10-13 scratch phase 2b (world st_b, seed 8675309, 1.0.6 jar of 21:59, 4 locator threads, earth fill `log = ALL`): NOT PASSED: `meta/fortified_camps` timed out at 120 s from 2448 -624 (11 loads); `meta/fortified_ruins` not retried. The earth fill blocker is gone (see ST-158); what is left is rarity plus the peaceful radius. Recheck with the Phase 6 census.*
- [ ] **ST-136 Remote metas [scratch]:** `/locate` answers for `meta/farmers_camp`, `meta/house_camp`, `meta/waysign_camps`, `meta/waysign_house_camp`; log lines list the parts. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): 3 of 4 PASSED: house_camp (2448 -624, plains), waysign_camps (1168 3072, plains), waysign_house_camp (2416 -2208, blooming plateau) answer with their starts. `meta/farmers_camp`: no answer in 300 s (37 assembled, 13 of their anchors cancelled by earth fill).*
- [ ] **ST-137 Way sign as a part [scratch/game]:** at a `waysign_camps` meta the sign post stands and its text points at real settlements.
- [ ] **ST-138 Tweaks [game]:** a driftwood flotilla is tighter (logs within about 26 blocks); a raft convoy can have 4 or 5 rafts; shipwreck debris has up to 3 wreckage pieces and driftwood logs near the wreck.
- [ ] **ST-139 Rates [game]:** fortified villages, houses and camps about as common as before (metas only add parts around structures that spawn anyway).

**Placement priority and companion villages** (2026-10-12, 3.22). New world; `[worldgen.placement_priority] log = true`.
- [x] **ST-140 Loads clean [scratch]:** `[structure-tuning] ... 228 ranked structure(s), 1 companion rule(s)`; on the first chunks `[placement-priority] 228 structure(s) ranked in ... set(s); 1 companion rule(s)`; the report has a `## Placement priority` section; the toml has `systems.placementPriority` and `[worldgen.placement_priority]`. No mixin errors. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED: 228 ranked, 1 companion rule, in 14 sets; report section and toml keys there.*
- [x] **ST-141 Drops happen [scratch]:** after pregenerating an area, the log has `[placement-priority] dropped <ruin/house> ... overlaps <village/...>` lines, losers always lower-ranked than winners. *2026-10-14 scratch phase 6 (structure_tuning 8.0.6, seed 8675309, 1.0.6 jar of 12:44, density 1, world st_p: Chunky square radius 96 chunks at 2144 -2592, census at -1700 1500; log testing/structures/06_phase_6.md): PASSED: 15 rank drop lines over 37249 chunks; the loser ranks below the winner in 15/15.*
- [ ] **ST-142 No mashups [scratch/game]:** at several logged drops, the winner stands whole and nothing of the loser is there (no blocks, no pad). *2026-10-14 scratch phase 6 (structure_tuning 8.0.6, seed 8675309, 1.0.6 jar of 12:44, density 1, world st_p: Chunky square radius 96 chunks at 2144 -2592, census at -1700 1500; log testing/structures/06_phase_6.md): scratch part PASSED: at all 22 distinct drops the loser's start is absent from its chunk and the winner's start is there; looking at them stays [game].*
- [x] **ST-143 Town with villages [scratch]:** `/locate structure creativeengineering:meta/town_villages`; the log has 1 to 3 `companion at chunk ... of creativeengineering:meta/town_villages` lines; each village is 7 to 10 chunks from the town and doesn't touch it. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): **FAILED / blocked**: `meta/town_villages` answered only once, at -2000 10336 (10073 blocks, desert; 20 other towns assembled, all cancelled by earth fill). After generating the 21 x 21 chunks around it there is no companion village (`/zstarts` lists the area's starts: the town, cabins, a dungeon, no village). Caveat: by then Forge had reset the scratch toml to the 1.0.6 defaults (`maxOverAir` 128, logs off; my edit raced its config watcher), and at 128 the companion predictor cancelled the town's own anchor, so no companions were sent. Retest after the earth-fill change. Also seen there: `philipsruins:badlands_start_dungeon` starts 6 chunks from the town (check against the peaceful radius, ST-156).* *2026-10-13 scratch phase 2b (world st_b, seed 8675309, 1.0.6 jar of 21:59, 4 locator threads, earth fill `log = ALL`): PASSED: `meta/town_villages` answered at 6384 -6000 (from 2448 -624, 95 s), its fortified-village anchor kept (8 over air). After forceloading the 21 x 21 chunks around it: 3 `companion at chunk ... of creativeengineering:meta/town_villages` lines, at [399, -382] (village_taiga, 7 chunks), [392, -371] (T&T village_birch_forest, 8.1) and [406, -371] (village_taiga, 8.1), all kept by earth fill; a pillager outpost/siege meta overlapping the first was dropped for it.*
- [x] **ST-144 Order independence [scratch]:** generate the same area twice in two fresh worlds with the same seed, approaching from opposite sides: the same structures in the same places (compare a `/dumpstructuregen`-style list or several `/locate` answers). *2026-10-19 scratch phase 8 (structure_tuning 8.0.8, seed 8675309, patcherMod 1.0.7 jar (worldgen = 1.0.8), density 4, worlds st_8a..st_8g; log testing/structures/08_phase_8.md): PASSED: Chunky square r 400 at -1400 -2592 in two fresh worlds, one approached from a west strip, one from an east strip (two JVM runs): 674 = 674 starts in full chunks, the same chunks, identical piece boxes.*
- [x] **ST-145 Overlap rate [scratch]:** with `placementPriority = false` count villages with another group's structure overlapping them in a pregenerated area (the rough estimate is about a third); with it on, none. *2026-10-19 scratch phase 8 (structure_tuning 8.0.8, seed 8675309, patcherMod 1.0.7 jar (worldgen = 1.0.8), density 4, worlds st_8a..st_8g; log testing/structures/08_phase_8.md): PASSED (the "about a third" estimate skipped as a rate): `placementPriority = false`: 2 of 7 settlements have a piece overlapping a ranked other-group structure (a savanna village x a field-stone ruin, a town x a ruin scatter); on: 0 of 30 over four areas. Off also means no companion villages.*
- [ ] **ST-146 Generation speed [game]:** flying fast over new land feels no slower than before (each higher-ranked slot is generated about twice).
- [ ] **ST-147 Villages stop at a town [scratch/game]:** at a `town_villages` meta (ST-143) and at grid villages near fortified towns: where a village reaches the town its streets end or turn and no house touches the town's pieces; the village is otherwise whole. The report's priority section lists `settlements` as shaped (34 structures).
- [x] **ST-148 Shaping off [scratch]:** with `priority.shape` emptied, the same seed gives villages that are dropped whole instead (log `dropped minecraft:village_...`), proving the shaping did the work. Put it back. *2026-10-19 scratch phase 8 (structure_tuning 8.0.8, seed 8675309, patcherMod 1.0.7 jar (worldgen = 1.0.8), density 4, worlds st_8a..st_8g; log testing/structures/08_phase_8.md): PASSED with the town companion distance loosened to 64-96 (at 112-160 no village reached a town in 8 towns, so on and off were identical): shape on, three companions end 0-1 blocks from their town with no overlapping piece; `priority.shape = []`, the same three slots log `dropped minecraft:village_taiga / village_old_growth_taiga / village_flower_forest ... overlaps creativeengineering:meta/town_villages`, and only one slot finds another village that fits.*
- [x] **ST-149 Meat blocks [scratch/game]:** the log has `[template-swaps] 5 block swap(s) in 4 template(s)` and no "no longer hold" warning; in a Towns & Towers birch forest, Mediterranean or mushroom village butcher (`/locate` the village, find the butcher) the hanging meat is an upright salmon log, not red glazed terracotta. `templateBlockSwaps = false` brings the terracotta back in new chunks. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): log part passed (combined line now 874 swaps / 69 templates, see ST-150), no "no longer hold"; block part in phase 3.* *2026-10-14 scratch phase 3 (seed 8675309, 1.0.6 jar of 01:28, world st_d at structure density 4 with two Chunky squares; st_e for the off-switch comparisons; log feature_writeup/testing/structures/03_phase_3.md): PASSED: all 5 marked spots are upright salmon logs (each template placed, plus 2 natural sky-village farms), no red glazed terracotta; with `templateBlockSwaps = false` (new world) every spot is red glazed terracotta again.*
- [x] **ST-150 Nether swaps load [scratch]:** the log has `[template-swaps] 874 block swap(s) in 69 template(s)` (meat + Nether) and no "no longer hold" warning; no `unwrecked_ships:nether_ship` in `/locate` in the Nether. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED: 874 swaps in 69 templates, no "no longer hold"; Nether /locate unwrecked_ships:nether_ship finds nothing.*
- [ ] **ST-151 Swapped blocks [game]:** a YUNG's jungle temple has magma (fire on it keeps burning) where netherrack was; a crypt's soul sand is coarse dirt; a YUNG's stronghold's statues use deepslate brick stairs/walls and its gold ore is normal; a Terralith frosted dungeon has permafrost and no soul fire; a fortified village's soul campfires are normal campfires; Philip's ancient crypt has basalt where soul sand was; a sky village has yellow corundum lights.
- [ ] **ST-152 Untouched [game]:** an ancient city keeps its soul sand and soul fire; Philip's lost soul dungeon, rare ruin and blackstone Nether ruin are unchanged; a Nether fortress and bastion are unchanged.

**Peaceful radius** (2026-10-12, 3.22). Install the 1.0.6 jar, new world or far new land; `[worldgen.placement_priority] log = true`.
- [x] **ST-153 Loads clean [scratch]:** on the first chunks `[placement-priority] 96 peaceful structure(s) in ... set(s) keep 65 hostile structure(s) away (radius up to 8 chunks)`; the report's priority section has a `### Peaceful radius` table (8 / 5 / 3) and `hostile: ... (65 structures)`; no KubeJS `priority.peaceful` error. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED: 96 peaceful in 2 sets keep 65 hostile away (radius up to 8); Peaceful radius table 8/5/3.*
- [x] **ST-154 Drops logged [scratch]:** after pregenerating, `dropped <outpost/temple/hut/dungeon> ... within N chunk(s) of peaceful <id> from chunk [...]` lines appear, N being 8, 5 or 3 by the peaceful structure. *2026-10-14 scratch phase 6 (structure_tuning 8.0.6, seed 8675309, 1.0.6 jar of 12:44, density 1, world st_p: Chunky square radius 96 chunks at 2144 -2592, census at -1700 1500; log testing/structures/06_phase_6.md): PASSED: 24 peaceful drop lines, every loser in a hostile group, N = the peaceful structure's radius (21 x 3 remote, 3 x 5 settlements; no big-settlement drop in the square).*
- [ ] **ST-155 Camps clear [game]:** `/locate` several `meta/farmers_camp`, `meta/house_camp` and T&T wandering trader camps: no pillager outpost, dungeon entrance, temple or witch hut within about 3 chunks (48 blocks) of any building of the camp.
- [ ] **ST-156 Villages and towns clear [game]:** around several villages no hostile structure within about 5 chunks (80 blocks) of the outermost house; around a fortified town (`/locate structure #creativeengineering:priority/big_settlements` or a Terralith fortified village) none within about 8 chunks (128 blocks).
- [x] **ST-157 Hostiles still spawn [scratch]:** `/locate structure #minecraft:pillager_outpost` (and a T&T outpost) still finds them out in the open; with `priority.peaceful` removed the same seed has hostile starts in the spots ST-154 logged. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): first half PASSED: `#minecraft:pillager_outpost` from 2400 1200 answers a T&T old-growth-taiga outpost at 2095 1055 (alpine highlands, inside a stronghold siege) and `towns_and_towers:pillager_outpost_forest` one at 2124 3655 (forest, inside a tower siege): out in the open.* *2026-10-19 scratch phase 8 (structure_tuning 8.0.8, seed 8675309, patcherMod 1.0.7 jar (worldgen = 1.0.8), density 4, worlds st_8a..st_8g; log testing/structures/08_phase_8.md): second half PASSED: with `priority.peaceful` removed, each of the 7 slots where the baseline world logged peaceful drops (2 in r 400 at -1400 -2592, 5 around a town at -3727 -3564) holds a hostile start (outposts, a stronghold siege, the undead castle, zombie dungeons).*

**Fixes before Phase 3** (2026-10-13, 7.3). The 1.0.6 jar from 2026-10-13 or later; new world or far new land.
- [ ] **ST-158 Big bearded structures stand [scratch]:** `/locate` a Terralith fortified village (or `meta/fortified_ruins`), a bastille and a Takes A Pillage camp: each answers, and the answer's chunk holds the start (no `CANCELLED ... over air` for them as in 8.0.2). Their `[earth-fill]` lines (`log = ALL`) name a size and `beard`, and air after the beard is far below the old 257+. *2026-10-13 scratch phase 2b (world st_b, seed 8675309, 1.0.6 jar of 21:59, 4 locator threads, earth fill `log = ALL`): PARTLY PASSED (earth fill part passed): no big bearded structure was cancelled for air any more. Bastille kept 4 times (20 over air after the beard, limit 320, `huge, beard`), TaP camp kept (0) and found at its chunk (2368 -2896, from 117 blocks), a fortified village kept as the anchor of `meta/town_villages` (8 over air). The 2 fortified-village cancels left are real: 681 mound columns (cliff) and 146 over water. Bastille: no answer within 120 s, from 2448 -624 or from 120 blocks of a kept one, because placement priority drops it (3 of 4 kept ones were within the 5-chunk peaceful radius of a village or house). Fortified village on its own: timed out at 120 s from 2448 -624 (14 loads).*
- [x] **ST-159 Cliffs and deep water still cancel [scratch]:** with `log = ALL`, pregenerate new land with cliffs and coasts: some starts log `CANCELLED` with mound columns or water past the beard over their limit; probe one or two: a piece over a drop or water deeper than about 6 blocks. *2026-10-19 scratch phase 7 (structure_tuning 8.0.7, seed 8675309, patcherMod 1.0.7 jar (worldgen identical to 1.0.8), density 4, worlds st_c / st_c2 / st_c3; log testing/structures/07_phase_7.md): PASSED: region-file probes of cancels: T&T beach village [-104, -157] over sea 21 deep (every column > 6), Farmer's Delight cooks [-79, -140] on a cliff (ground y 54-120 around it), Subterrestrial ocean cabins whose fixed height lies in open sea water (kept ones sit under shallow floors).*
- [ ] **ST-160 Sizes [scratch]:** among the `log = ALL` lines, small ruins log `small` with limits 24 / 8, villages `small`, `medium` or `large` by their size (small villages such as vanilla taiga log `small`), fortified villages `large` or `huge`; an `overrides` line with 2 numbers keeps the size's mound limit, one with 3 replaces it. *2026-10-19 scratch phase 7 (structure_tuning 8.0.7, seed 8675309, patcherMod 1.0.7 jar (worldgen identical to 1.0.8), density 4, worlds st_c / st_c2 / st_c3; log testing/structures/07_phase_7.md): PARTLY: ruins log `small` 24 / 8 and most villages `medium` / `large`, but small villages log `small` (vanilla taiga 128-135 earth, T&T beach 126, Mediterranean/Tudor 42-74); no fortified village in the square. Overrides PASSED: `philipsruins:ancient_dungeon=5,7` -> limits 5 / 7 / 8 (size's mound kept), `terralith:underground/witch_hut=5,7,3` -> 5 / 7 / 3.*
- [x] **ST-161 Old key dropped [scratch]:** after the first boot the toml has `beardReach`, `buryReach`, `maxMoundHeight` and `[worldgen.earth_fill.sizes]`, and no `maxOverAir`; no config error in the log. *2026-10-13 scratch phase 2b (world st_b, seed 8675309, 1.0.6 jar of 21:59, 4 locator threads, earth fill `log = ALL`): PASSED: after the first boot on the old toml, `maxOverAir` is gone, and `beardReach` 6, `buryReach` 10, `maxMoundHeight` 8, `prune`, `pruneMaxHanging` and `[worldgen.earth_fill.sizes]` were added (Forge's usual "corrected from null to its default" notices only); `log = "ALL"` was kept; no config error.*
- [x] **ST-162 Mansion exempt [scratch]:** with `log = ALL`, no `[earth-fill] minecraft:mansion` line ever. *2026-10-19 scratch phase 7 (structure_tuning 8.0.7, seed 8675309, patcherMod 1.0.7 jar (worldgen identical to 1.0.8), density 4, worlds st_c / st_c2 / st_c3; log testing/structures/07_phase_7.md): PASSED: no mansion in the square; `/locate` from a dark forest built 4 mansion starts (3 dropped by peaceful radius, 1 answered at 2560 -5504): no `[earth-fill] minecraft:mansion` line.*
- [x] **ST-167 Villages pruned, not cancelled [scratch]:** pregenerate new land with rivers and coasts (default log): `[earth-fill] <village> ... pruned K of N pieces; over the piece limits: ...` lines appear, and far fewer villages log `CANCELLED` than in a run with `prune = false`. Probe a pruned one: no house over deep water, the street it hung off ends there, no villager or animal stranded where the dropped piece was. *2026-10-19 scratch phase 7 (structure_tuning 8.0.7, seed 8675309, patcherMod 1.0.7 jar (worldgen identical to 1.0.8), density 4, worlds st_c / st_c2 / st_c3; log testing/structures/07_phase_7.md): FAILED as shipped: no `pruned` line ever: `#creativeengineering:earth_fill/prune` is empty at runtime because `#creativeengineering:type/village` is not a real tag (structure_overrides.js only adds meta/* tags). PASSED with a scratch-only fix (type/village added as a real tag): same square, village cancels 4 -> 2; pruned taiga village (5 of 124 pieces) and savanna village (8 of 171) stand, no junction points at a missing piece, no rigid piece hangs, villagers and golem inside kept pieces.* *2026-10-19 fix rerun (patcherMod 1.0.8 d60b5a99, `type/*` tags real; world st_norm, seed 8675309, density 4, square r 400 at 3450 -1100; testing/structures/11_fixes_1_0_8.md): PASSED as shipped: `#earth_fill/prune` and `#type/village` are filled; `village_savanna` [232, -38] pruned 1 of 132 and `village_taiga` [210, -70] pruned 1 of 112, both kept; no plain village cancelled in the square.*
- [x] **ST-168 Only villages are pruned [scratch]:** no `pruned` line names a structure outside `#creativeengineering:type/village` (or a meta); `/locate` of a pruned village still answers at its chunk. *2026-10-19 scratch phase 7 (structure_tuning 8.0.7, seed 8675309, patcherMod 1.0.7 jar (worldgen identical to 1.0.8), density 4, worlds st_c / st_c2 / st_c3; log testing/structures/07_phase_7.md): PASSED with the tag fix: 4 pruned lines, all villages; `/locate` of both pruned villages answers at their own chunk ([-81, -181], [-142, -130]).* *2026-10-19 fix rerun (same world): PASSED: both pruned lines are villages.*
- [x] **ST-169 Prune switch [scratch]:** `prune = false` (one reboot, new land): no `pruned` lines; the same seed's village near water logs `CANCELLED` instead. *2026-10-19 scratch phase 7 (structure_tuning 8.0.7, seed 8675309, patcherMod 1.0.7 jar (worldgen identical to 1.0.8), density 4, worlds st_c / st_c2 / st_c3; log testing/structures/07_phase_7.md): PASSED: `prune = false` (tag fix on): 0 pruned lines; the taiga village at [-81, -181] and the savanna village at [-142, -130] log CANCELLED with the same numbers as without pruning.*
- [x] **ST-163 End tower fails fast [scratch]:** from the Overworld, `/locate structure` the End tower of ST-80: "Could not find" within a few seconds (was 900+ s). *2026-10-13 scratch phase 2b (world st_b, seed 8675309, 1.0.6 jar of 21:59, 4 locator threads, earth fill `log = ALL`): PASSED: "Could not find" in 0 s (1 ms, 0 loads) from the Overworld (was 900+ s).*
- [x] **ST-164 Rare anchors answer or time out [scratch]:** `/locate` the desert tower and the bastille: each answers or ends within 120 s with a `[locate] ... not found (timed out after 120 s)` line; `/cwglocate stats` shows `meta_target` savings for them and `cutShort` for any that timed out. ST-86 and ST-87 still find a part and an anchor. *2026-10-13 scratch phase 2b (world st_b, seed 8675309, 1.0.6 jar of 21:59, 4 locator threads, earth fill `log = ALL`): PASSED: desert tower and bastille from 2448 -624 each ended at 120 s with `not found (timed out after 120 s)` (13 and 8 loads); `/cwglocate stats` shows `cutShort` 1 for both and `meta_target` savings of 47 and 23. Loads were slow (about 9 s each with 4 searches at once). The desert tower was kept 4 times by earth fill elsewhere during the searches (2 dropped by peaceful radius).*
- [x] **ST-165 Stop [scratch]:** start 4 slow searches (desert tower from 4 places), then `/cwglocate stop`: each logs `... (stopped)` within a second or two and the locator threads are free (a new `/locate` of a village answers at once). *2026-10-13 scratch phase 2b (world st_b, seed 8675309, 1.0.6 jar of 21:59, 4 locator threads, earth fill `log = ALL`): PASSED: 4 desert-tower searches, `/cwglocate stop` after 20 s: all 4 logged `not found (stopped)` in the same second; a `/locate` of `village_plains` sent 4 s later answered 4 s after that (2064 blocks).*
- [x] **ST-166 Server stop [scratch]:** with 4 slow searches running, `stop`: the server exits within a few seconds (no kill needed). *2026-10-13 scratch phase 2b (world st_b, seed 8675309, 1.0.6 jar of 21:59, 4 locator threads, earth fill `log = ALL`): PASSED: 4 bastille searches, `stop` after 20 s: the java process exited 40 s later with no kill. The searches logged `not found (server stopping)` within 2 s of "Stopping the server", and the save took 6 s; the other 31 s passed before the server thread got to the `stop` command at all (it was lagging behind under the 4 searches, see LO-05).*

**New structure mods before Phase 3** (2026-10-14, plan 7.4, BUILT 2026-10-14, offline checks passed). New world. Rare structures: loosen soft
rules only and note it (7.4.8). Correctness tests (biome, peaceful, fill) are never loosened.
*User constraints (2026-10-14, after the offline checks): Nether and End spawning are out of scope (ST-186 and the
Nether/End part of ST-171 skipped); structure-to-structure rates are out of scope (the split in ST-181, its `--check`
estimates, the harsh/ordinary ratio in ST-185 skipped). Every correctness part stays.*
- [x] **ST-170 Loads clean [scratch]:** no KubeJS `structure_overrides` / `priority` / `biome_frequency` error, no
  function parse error for `eugene:*` (`/reload` too), no "Skipping Entity" beyond the stripped ones; only the 2.3.4
  data is loaded (no 1.1.1 jar). The report lists `group/vehicles`; `hostile:` includes `vehicles` and moved by exactly
  the 7.4.7 list; the peaceful count by `eugene:camp`, 6 Campsite camps, red roof castle. Record the counts.
  *2026-10-14 offline (node sim, pre-check only): peaceful 96 -> 104 (+ red roof castle r8, `eugene:camp` and the 6
  Campsite camps r3), hostile 65 -> 70 (+ undead castle, pillager camp, fake trader camp, loot carriage, witch wagon;
  the siege weapons stay hostile through `vehicles`), no structure in both; drum tower and piglin camp neither.* *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED after 2 port fixes: 1.20 pools had no `name` (world failed to load; porter now adds it, new tools/check_worldgen_json.py) and predicate `minecraft:all_of` (now a JSON array). Load counts exact (15 groups, 30 fits for 181, 104 peaceful / 70 hostile incl. `vehicles`, biome factors staged); `/reload` clean for `eugene:*`; 1.1.1 jar not in mods.*
- [x] **ST-171 Campsite features gone [scratch]:** pregenerate new land, Nether and End: no Campsite camp outside a
  structure start; `/locate structure campsite_structures:<camp>` answers for each of the 6 in a fitted land biome. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED (Overworld; Nether/End skipped by constraint): 14 `campsite_structures:camper` in the two areas, every one inside a Campsite piece (standalone or a meta part), none loose; all 6 camps saved in fitted biomes.*
- [x] **ST-172 Camp split [scratch]:** `/locate` `eugene:camp`, `eugene:pillager_camp`, `eugene:fake_trader_camp`:
  each answers; 5 of each probed: `eugene:camp` only ever trader / lost-llama templates, `pillager_camp` only pillagers. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED: `eugene:camp` pieces only trader_camp / trader_camp_lost_llama, `pillager_camp` only pillager_camp, `fake_trader_camp` only trader_camp_fake; their mobs are there (trader + llamas; pillager + vindicator).*
- [x] **ST-173 Passive camps are clear [scratch]:** `dropped ... within 3 chunk(s) of peaceful` lines name
  `eugene:camp` and Campsite camps; no outpost, dungeon, witch hut, vehicle, pillager or fake trader camp within 3
  chunks of one. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED: drop lines with Campsite camps and `eugene:camp` as the peaceful side (witch hut, 2 dungeons, battering ram, outposts, pillager tower, great-tower siege meta, pillager and fake trader camps, r3); region check over 78 peaceful / 26 hostile starts: 0 within radius.*
- [x] **ST-174 Hostile camps keep away [scratch]:** no `eugene:pillager_camp` or `fake_trader_camp` within 5 chunks of
  a village, 3 of a house/camp, 8 of a fortified town or the red roof castle. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED: pillager camp 19 / fake trader camp 15 drops: r3 (houses, camps, way-sign metas), r5 (villages, village_towers), r8 (town_villages); 0 violations in the region check.*
- [x] **ST-175 Camps on land [scratch]:** 10 located camps of each kind (eugene and Campsite): none in an ocean/river
  biome, on a seabed or over water; the fisher camp on a beach or river bank. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED: 0 out-of-fit starts of 5530 (biome read at the jigsaw stub, the start piece centre); every kept camp 0 over water and 0 over air; fisher camp on a gravel beach, one at a shore cancelled (26 over water of 24).*
- [ ] **ST-176 Earth fill reads them [scratch]:** with earth fill `log = ALL`, each new structure logs `[earth-fill]`
  with a sensible size (castles `large`/`huge`); cancels only over real cliffs/water. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PARTLY: every new structure logs `[earth-fill]` with a sensible size (Campsite medium, eugene camps small, red roof castle large, undead castle huge). The undead castle was cancelled 4 times of 5 over real slopes (relief 30 and 12-15 blocks under two of them; mound columns 264-448 of 128). The drum tower reads `small` from its 10 earth base blocks: its 49x45 polished andesite base is invisible to earth fill (open issue).*
- [x] **ST-177 Castle split [scratch]:** `eugene:castle` never generates; `undead_castle`, `red_roof_castle`,
  `ruined_drum_tower` each `/locate` in a fitted biome; the undead castle has its knights/archers and Ser Marrow
  (boss bar shows within 32 blocks), the red roof castle its villagers and golems and no zombie. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED (boss bar is [game]): no `eugene:castle` start anywhere; drum towers in fit; red roof castles anchor metas; undead castle with knights, archers and Ser Marrow, red roof castle with 11 villagers, 2 golems, no zombie (st_c).*
- [x] **ST-178 Undead castle is a surface dungeon [scratch]:** it is in `group/surface_dungeons`; never within 5 chunks
  of a village, 3 of a house/camp, 8 of a fortified town. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED: in `group/surface_dungeons`; one dropped within 3 chunks of a watchtower; 0 violations.*
- [x] **ST-179 Red roof castle is a big settlement [scratch]:** no hostile start within 8 chunks of its pieces; nothing
  overlapping it (rank 100); a way-sign destination; never pruned by earth fill. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED: no hostile start within 8 chunks (23x23 chunks forceloaded around one); no overlap; `type/castle`, not pruned; way-sign destination (sim tag, live locate answers its meta); earth fill large/beard kept.*
- [x] **ST-180 Red roof castle anchors metas [scratch]:** `/locate structure creativeengineering:meta/town_villages`
  (and `fortified_camps`, `fortified_ruins`) can answer with a red roof castle as anchor (`[meta-structures] log`);
  companion villages clear the castle. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED: red roof castle anchors `town_villages` (5 seen), `fortified_camps` (st_c) and `fortified_ruins` ([50,-83], st_d); companion village 6 blocks off the castle piece, no overlap.*
- [x] **ST-181 Fortified-town shares [scratch]:** the applier log shows camps 0.20 / ruins 0.20 / town_villages
  0.40, no "shares over 100%" warning; `--check` estimates: camps and ruins x0.8, town_villages x1.33; with
  `[meta-structures] log`, placed towns/forts split about 1 : 1 : 2 : 1 (camps, ruins, villages, plain; soft). *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED (log part; rates skipped by constraint): report shares 0.20 / 0.20 / 0.40, no "over 100%" warning.*
- [x] **ST-182 Vehicles group [scratch]:** siege weapons no longer in `group/pillager_settlements`; carriage and witch
  wagon `/locate` in their fitted biomes only (wagon in dark forest/swamp); the carriage pool still gives all three
  variants. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED: no siege weapon in `group/pillager_settlements`; carriage and wagon answers in fit (savanna, desert, badlands; wagon mangrove swamp); `/place` x8: carriage 5, golem van 2, chase 1.*
- [x] **ST-183 Vehicles are hostile [scratch]:** `dropped <vehicle> ... within N chunk(s) of peaceful` lines; no vehicle
  within 5 chunks of a village, 3 of a house/camp, 8 of a fortified town; the 4-chunk settlements exclusion holds. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED: 14 vehicle drop lines (r3/r5/r8 and overlaps); the two surviving siege towers start 10 and 15 chunks from the nearest settlement (exclusion 4).*
- [x] **ST-184 Pillager metas unchanged [scratch]:** tower/great tower/stronghold/camp siege metas still assemble with
  siege-weapon parts (MetaTest + one `/locate` of tower siege); a camp meta can now anchor on `eugene:pillager_camp`. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED: tower / great tower / stronghold / camp siege metas assembled with 22 siege-weapon parts; 4 camp metas anchored on `eugene:pillager_camp` (st_c).*
- [x] **ST-185 Harsh biomes and locate agreement [scratch]:** `biome_frequency` staged (log); more vehicles in a harsh
  area than an ordinary one of equal size (soft, ratio about 2); 10 `/locate`s of a vehicle in ordinary biomes: every
  answer's chunk holds the start. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED (locate part; ratio skipped): biome factors staged; 10 searches from 10 points: 8 answers in 6 chunks, each chunk holds the start, all in fitted ordinary biomes; 2 timed out with misses only (Phase 6 lead).*
- [ ] **ST-186 Piglin camp grounded [scratch]:** 10 located piglin camps in crimson forest: none hanging over the lava
  sea or buried in netherrack (or cancelled with an `[earth-fill]` line saying so). *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): SKIPPED: Nether out of scope (user constraint 2026-10-14).*
- [x] **ST-187 Remapped blocks [scratch]:** KubeJS block probe on one witch wagon, red roof castle and drum tower: the
  purple panes, urns, bookshelves, signs, gravel/sand are there, no holes where 1.20 blocks were; vanilla outpost tents
  have their fences. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED: template palettes only vanilla + `supplementaries:urn`, vanilla outpost tents 2 fences each; drum tower in the world matches its template block for block (gravel 260, sand 39, bookshelf 29, urn 5); witch wagon (panes, urn) and red roof castle (168 bookshelves, lanterns on chains, no sign) in st_c.*
- [ ] **ST-188 Functions work [scratch]:** a fake trader within 6 blocks of a player turns into an illusioner; the
  lost-llama quest starts; Ser Marrow rides its steed (no `ride` error). *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PARTLY: Ser Marrow rides his steed after a fix (steed summoned with `Tame:1b`; an untamed skeleton horse throws its rider); the illusioner and the lost-llama quest need a player ([game]).*
- [x] **ST-189 Chest maps don't stall [scratch]:** open (or `/loot`) a carriage chest and a trader camp chest: the
  village map fills via Async Locator, no server stall in the log. *2026-10-14 scratch M2 (seed 8675309, 1.0.6 jar of 01:28, 4 locator threads, earth fill `log = ALL`; world st_c at density 1, st_d at density 4 with two Chunky areas of radius 800 around 0 0 and 2144 -2592; log feature_writeup/testing/structures/02_new_mods_tests.md): PASSED via `/loot spawn`: the village map search ran on an Async Locator thread in 86 ms, no stall; a real chest open needs a player.*
- [ ] **ST-190 Looks right [game]:** fly to one of each new structure: beard/trim fit the ground, no floating
  carts, Campsite's camper entity there, the castles' insides intact.

**Fixes from the scratch run** (2026-10-18, 3.23). In 1.0.7 (DEPLOYED 2026-10-18); new chunks only.

- [ ] **ST-191 Same villages after a restart [scratch]:** fresh world, `/cwglocate batch towns_and_towers` (and
  `/locate` of `meta/village_towers`), restart the server on the same world copy taken before the first batch, run it
  again -> every found position identical, including the desert and savanna village anchors. The log has `put in
  canonical order` with a count; `pool_tag_report.md` lists `minecraft:village/desert/houses` in key order on both
  starts. With `canonicalOrder = false` the old behaviour returns (orders differ between starts).
- [ ] **ST-192 Mod village houses survive a reload [scratch]:** fresh world, `/locate` villages far out so their
  structure_starts chunks are saved before placement, stop and restart, fly there -> no "Element ... not valid in
  current registry set" at save and no "Invalid pool element found" at load; Cloud Storage and Domestication
  Innovation houses are in the villages. The boot log has `pool piece(s) moved to the world's processor lists`.
- [ ] **ST-193 Ground match over a deep structure [scratch]:** with `[worldgen.ground_match] log = true`, a Philip's
  ruin or tower on volcanic peaks above a mineshaft or ancient city -> its grass pad is swapped (a log line with a
  palette); where a base is left for lack of columns, a `only N ground columns sampled` line says so.
- [ ] **ST-194 Ground match still skips structure ground [scratch]:** a ruin next to a village house -> the house's
  floor and paths are not in the sampled palette (the log's top/under histograms have no planks or path).
- [ ] **ST-195 Driftwood on banks [scratch]:** new terrain, walk a river, a swamp and a beach -> driftwood logs on
  sand or dirt right at the water's edge, about one river bank log every few chunks; none floating outside oceans;
  open ocean still has floating logs. `latest.log` has no `upgrade_aquatic:driftwood_*` parse error.
- [ ] **ST-196 Deep dungeons generate [scratch]:** `/locate structure betterdungeons:skeleton_dungeon`,
  `philipsruins:bone_dungeon` and `philipsruins:lost_soul_city` -> each found; `/place` or fly there: built at its
  depth. (ST-115 retest.)
- [ ] **ST-197 Drum tower on a cliff [scratch]:** with earth fill `log = ALL`, a few `eugene:ruined_drum_tower`
  starts -> each line shows it as large or huge with hundreds of base blocks (not "small, 10 earth"); one over a cliff
  or a lake is cancelled, one on flat ground kept.
- [ ] **ST-198 Other structures unchanged by the stone tag [scratch]:** with `log = ALL`, a stone-floored structure
  not in the tag (a YUNG's witch hut, a castle) -> its earth count and size are as before (stone not counted).

**Terrain adaptation and template trim** (2026-10-09, 3.15). Install the jar first (`./gradlew installToMods` in
`development/creativeCommon` with the game closed), rejoin, and fly well away from explored land.
- [x] **ST-61 Loads clean:** no mixin error for `StructureTerrainAdaptationMixin` or `BeardifierSoftMixin` (the game would not start); `latest.log` has `[terrain-adaptation] world load: 97 structures tagged, modes {NONE=6, SOFT=91}, 95 trimmed` and no `answers terrainAdaptation itself` or `... wins` warning; no KubeJS error for `worldgen/terrain_adaptation.js`; the toml has `[worldgen.terrain_adaptation] skirtBump = 0` and `systems.terrainAdaptation` / `systems.templateTrim`. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED: terrain-adaptation line exact (97, NONE=6 SOFT=91, 95 trimmed), no mixin/KubeJS errors, skirtBump = 0 and both systems keys.*
- [ ] **ST-62 Ballista:** `L illager_siege_weapons:ballista` (or another siege weapon) until one generates on a slope. No flat square around it and no notch in the hill; the ground under and around it is the natural slope; it stands on its own slabs (one end may hang a block or two over a steep slope: that is `none`).
- [ ] **ST-63 Soft house on a slope:** `L villager_butcher_house:butcher_house` (or the circus tent, a pillager tower) in hilly new terrain. The ground is level under the building and blends into the hill within about 4 blocks, following the walls with rounded corners; no square grass pad; the yard is natural ground.
- [ ] **ST-64 skirtBump:** set `skirtBump = 4`, rejoin, find the same kind of structure in new chunks: the blend is as wide as vanilla's (about 8 blocks) but still follows the building. Set it back to 0.
- [ ] **ST-65 Switches:** `[worldgen.systems] terrainAdaptation = false`, rejoin, new chunks: the tagged structures get their own beard again (the flat rectangle), with `templateTrim` still on (no pad, no air notch, but the rectangle is flattened). Then `templateTrim = false` too: pads and notches are back. Both back on. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PARTLY: terrain adaptation off (new area, same seed as a world with it on): the structures' own beard flattens the 8-block ring around them (550 vs 153 and 559 vs 363 ring columns at floor height, jungle outpost and fresh watchtower); ours keeps the slope. The `templateTrim = false` part was not run (a third world): [game].*
- [ ] **ST-66 Watchtowers and plates:** `L watchtower_building:fresh_watchtower`: the tower stands complete on its base plate (the foundation piece is never trimmed), with the soft blend around it.
- [x] **ST-67 Earth fill sees the trim:** with `[worldgen.earth_fill] log = "ALL"`, a trimmed structure's `[earth-fill]` line counts only earth under its built columns (the circus tent's base count drops by its 264 trimmed pad blocks). Put `log` back. *2026-10-19 scratch phase 7 (structure_tuning 8.0.7, seed 8675309, patcherMod 1.0.7 jar (worldgen identical to 1.0.8), density 4, worlds st_c / st_c2 / st_c3; log testing/structures/07_phase_7.md): PASSED: circus tent [-144, -228]: 784 earth with `templateTrim = false`, 520 with it on: exactly the 264 pad blocks (trim off also moves it from medium to large).*
- [ ] **ST-68b Scan batches:** in new terrain, a Towns & Towers pillager outpost (`L towns_and_towers:pillager_outpost_grove`) and a vanilla one on a slope: level under the tower and pieces only, round 4-block blend, no grass slab or air notch beyond them; the outpost is otherwise complete (tower top, cages, tents). A hobbit house or botanist house: as ST-63. A Takes a Pillage bastille: its courtyards are still flat inside the walls, but outside the walls the ground blends back within about 4 blocks instead of a wide flat apron.
- [ ] **ST-68 Others unchanged:** a vanilla village, a YUNG's structure and an untagged modded house look as before (the same beard and pads).

**Water fill** (2026-10-08, 3.14)
- [x] **ST-56 Loads clean:** the toml has `[worldgen.water_fill]` and `systems.waterFill`; no `[water-fill] overrides: ... skipped` warning; no KubeJS error for `worldgen/water_fill.js`; `/dumpstructuregen` still works. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED: toml section + key, no skipped warning, no KubeJS error, /dumpstructuregen works.*
- [x] **ST-57 Log run (calibration):** set `[worldgen.water_fill] log = "ALL"`, rejoin, fly over 2,000+ blocks of new ocean and coast (or `[worldgen.debug] structureDensity = 4`). `latest.log` has `[water-fill] hopo:underwater_ruins at chunk [x, z]: N water blocks, G into dry ground (limit 32), A into air (limit 16) -> kept|CANCELLED` lines. Ruins in open ocean should show 0 or close to it; tell me the numbers and the limits get set from them. Put `log` back to `CANCELLED`. *2026-10-19 scratch phase 7 (structure_tuning 8.0.7, seed 8675309, patcherMod 1.0.7 jar (worldgen identical to 1.0.8), density 4, worlds st_c / st_c2 / st_c3; log testing/structures/07_phase_7.md): DATA: Hopo ruins/fossils 97% at 0 into ground / 0 into air; outliers 111 and 115 into ground (fossil, warm ruin), 14 and 25 into air (large ruins); 32 / 16 cancel 2 of 164 in the main square. Ponds and the swamp house in the log 2.4.*
- [x] **ST-58 A cancel:** set `maxIntoGround = 0`, `maxIntoAir = 0` and `overrides = []`, rejoin, fly along a new coast: Hopo ruins near the shore log `CANCELLED` and are missing; the fish ponds and the swamp fisherman house cancel almost everywhere (their water is dug into the ground). Put the values back. *2026-10-19 scratch phase 7 (structure_tuning 8.0.7, seed 8675309, patcherMod 1.0.7 jar (worldgen identical to 1.0.8), density 4, worlds st_c / st_c2 / st_c3; log testing/structures/07_phase_7.md): PASSED: at 0 / 0 with `overrides = []` (coast square r 400 at -1400 -1000): 4 of 166 cancel (warm ruin 115 into ground, ruin and fossil 4-5 into air, the swamp fisherman house 268 into ground, which stood under the default override), all missing from their chunks; a fish-pond `/locate` cancelled every pond on 41 misses and found none.*
- [x] **ST-59 Ponds:** with the default overrides, a fish pond on flat plains or a fisherman house in a swamp still generates (their ground count doesn't matter); one on a steep slope where the pond would spill logs `CANCELLED`. *2026-10-19 scratch phase 7 (structure_tuning 8.0.7, seed 8675309, patcherMod 1.0.7 jar (worldgen identical to 1.0.8), density 4, worlds st_c / st_c2 / st_c3; log testing/structures/07_phase_7.md): PASSED: default overrides: fish pond kept at [-180, -211] (1 water block) and found by `/locate`; swamp fisherman house kept at [-192, 12] with 268 into dry ground and 0 into air and found by `/locate`; one on a slope ([-283, -70], 108 into air) and the carp ponds on slopes (26 and 67 into air) CANCELLED.*
- [x] **ST-60 Switch and scope:** `[worldgen.systems] waterFill = false` → no `[water-fill]` lines. Back on: a structure not in `water_fill/structures` (a Better Jungle Temple, a village with farms) never logs a `[water-fill]` line, even at 0 and 0. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED: off: no `[water-fill]` lines; on with `log = ALL`: lines only for tagged Hopo ruins/fossils, none for a Better Jungle Temple generated in the same area.*

**Setup:**
- Creative world with cheats. Rejoin once after pulling these changes, then fly well away from explored land (or
  make a new world) so the chunks you check are new.
- `L <id>` = `/locate structure <id>`. A removed structure answers "Could not find a structure of type ... nearby".
- `Dump` = `/dumpstructuregen`, then run `python3 tools/check_structure_live.py` outside the game.
- Revert every temporary edit and rejoin before moving to the next test.

**Structure sets** (tests from before the groups; the retired ones used the old knobs)
- [ ] ~~**ST-01 Loads clean**~~ retired 2026-09-27, moved to ST-17.
- [ ] ~~**ST-02 Live values match**~~ retired 2026-09-27, moved to ST-18.
- [x] **ST-03 Removal works:** `L minecraft:ruined_portal` → not found, answered within a second or two. In the Nether, `L minecraft:ruined_portal_nether` → not found. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: both answer "Could not find" in 0.5 s (Overworld and Nether).*
- [ ] **ST-04 New houses load:** `L villager_tannery:tannery` and `L simply_houses:taiga_inn` → found. Teleport to the tannery → it has its villager, and urns stand where the original 1.20 build had decorated pots. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): locate part PASSED: tannery at 2368 1232 (old-growth pine taiga) and taiga inn at 2768 1168 found, starts there. Villager and urns still to do in a client.*
- [x] **ST-05 AE2 meteorite in the sky group:** `L ae2:meteorite` → found, and a crater is there; meteorites are now as rare as Cloud Storage's structures (1 per 215 chunks). *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: meteorite at -1504 -688 (river), start box 143 wide at y 59; 1616 blocks from spawn vs a big balloon at 2537.*
- [ ] ~~**ST-06 Category knob**~~ retired 2026-09-27 (tags have no multipliers), moved to ST-21.
- [ ] ~~**ST-07 Per-set edit**~~ retired 2026-09-27, moved to ST-23.
- [ ] ~~**ST-08 One member off**~~ retired 2026-09-27, moved to ST-21.
- [ ] ~~**ST-09 An error applies nothing**~~ retired 2026-09-27, moved to ST-24.
- [ ] **ST-10 Blocked means blocked:** `L betterstrongholds:stronghold` → found; throw an eye of ender → it heads toward a YUNG's stronghold. The report lists `minecraft:stronghold` as blocked under Coverage. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): locate part PASSED: `betterstrongholds:stronghold` at 1728 -2192 (2900 blocks); `minecraft:stronghold` answers "Use /locate structure betterstrongholds:stronghold instead!"; the report lists it as blocked under Coverage. Eye of ender still to do in a client.*

**Structure groups** (2026-09-27)
- [ ] **ST-17 Loads clean:** rejoin, `logs/latest.log` has `[structure-tuning] 9 groups, 96 structure_sets written (64 emptied into groups), 117 original sets; 109 structure frequencies below 1` and `[structure-frequency] 109 structure(s) in 11 set(s) roll a frequency below 1`, no error line. `structure_tag_report.md` has no Warnings section and shows all 9 groups. *2026-09-27: both lines exactly so on a scratch dedicated-server boot of this instance (new world); your client rejoin still to do.* *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): log part passed (14 groups, 167 sets written, 133 emptied, 180 original; 110 frequencies below 1 in 14 sets), no Warnings section; client rejoin still to do.*
- [x] **ST-18 Live sets match:** `Dump` → sections 1 and 2 empty, section 4 `0 mismatch(es)` over 96 sets. PASSED 2026-09-27 on the scratch dedicated server (`/dumpstructuregen` from its console, `check_structure_live.py`: 0 / 0 / 0 mismatches over 96 sets, with the hand-set group grids). *Re-run 2026-10-13 scratch phase 1: 0 / 0 / 0 mismatches over 167 sets; 147 live structures in no set (the documented 142 + Farmer's aethers/undergardens_sea, the unwrecked nether ship, and the two vanilla shipwrecks Revamped Shipwrecks empties).*
- [x] **ST-19 Every group spawns:** one `L` per group → found: `minecraft:village_plains`, `towns_and_towers:pillager_outpost_forest`, `philipsruins:level_one_ruins`, `betterjungletemples:jungle_temple`, `hopo:underwater_ruins`, `minecraft:monument`, `cloudstorage:big_balloon`, `towns_and_towers:village_ocean`, `simply_houses:taiga_inn`. Teleport to two of them → the structure is really there (the `/locate` prediction and the frequency roll agree). *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: all 9 found, each answer chunk holds a start of that structure (the probe read the chunk's starts), except `level_one_ruins`, whose answer (-3283 1603, not chunk-aligned) is a part inside a `meta/pillager_camp_ruins` (that chunk references the meta). Searches from the ocean spawn are long: village_plains 3203 blocks / 38 s, outpost_forest 3732 / 66 s, village_ocean 6264 / 67 s.*
- [ ] **ST-20 The gap holds:** in fresh terrain, `L #minecraft:village` from 6 spots a few thousand blocks apart and teleport to each: note the chunk of each settlement's centre (F3). No two settlements of any mod (vanilla, T&T, fortified) start closer than 13 chunks. Same check for 3 ruins (5 chunks) and 3 surface dungeons (11 chunks). *2026-10-14 scratch phase 6 (structure_tuning 8.0.6, seed 8675309, 1.0.6 jar of 12:44, density 1, world st_p: Chunky square radius 96 chunks at 2144 -2592, census at -1700 1500; log testing/structures/06_phase_6.md): checked from region files instead of F3: settlements 7 starts, none closer than 13 chunks; ruins 105, none closer than 9 (the grid is 16/8 now, the 5 above is old); surface dungeons 10, none closer than 11; every one of the 30 random-spread sets with starts clean.*
- [x] **ST-21 One member off, others unchanged:** in `structure_groups.json` set `groups.sky.structures.cloudstorage:sky_temple.enabled` to false, rejoin → the report shows it disabled and 1 of 2 members live; `L cloudstorage:sky_temple` → not found; `L cloudstorage:big_balloon` → found. Revert. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED: report shows the sky temple disabled (3 of 4 sky members live now); sky temple not found, big balloon found.*
- [x] **ST-22 A low frequency is rare, not gone:** `L minecraft:mansion` (surface_dungeons, frequency 0.12) → found, and a mansion stands there. `L minecraft:monument` (frequency 0.1) → found, and a monument stands there. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: mansion at 816 -1504 (temperate highlands), monument at -2976 -2400 (deep ocean), each with its start in the answer chunk.*
- [x] **ST-23 Group spacing edit:** set `groups.boats.placement.spacing` to 60 (separation 21 stays), rejoin, `Dump` → `creativeengineering:group/boats` spacing 60 in `structuregen.json`, `check_structure_live.py` 0 mismatches. Put 41 back. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED: spacing 60 (current 48, separation 28) live in the dump; `check_structure_live.py` (run on the scratch copy) 0 mismatches over 178 sets.*
- [x] **ST-24 An error applies nothing:** add `"frequncy": 0.5` to any member, rejoin → the log shows `[structure-tuning] ... unknown field "frequncy"` and "nothing applied", no `[structure-frequency]` line; the report lists the error; `L minecraft:ruined_portal` finds one again (mod defaults). Remove the typo and rejoin. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED: `unknown field "frequncy"` + `nothing applied, every structure uses its mod/vanilla default` (ERROR), no `[structure-frequency]` line, the report lists it, a ruined portal locates.*
- [x] **ST-25 Moving a structure:** move `terralith:glacial_hut` from `group/ruins` to `group/remote` in `structure_tags.json` (add `!terralith:glacial_hut` to ruins, `terralith:glacial_hut` to remote), run `python3 tools/gen_structure_groups.py` → it prints "removed ruins: terralith:glacial_hut" and a fitted remote entry; `--check` shows it at 1 per 22 like before; rejoin → loads clean, `L terralith:glacial_hut` → found. Revert both files (git) and rejoin. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED (the `--check` rate part skipped): the generator printed "removed ruins: terralith:glacial_hut (now in group/remote)" and a fitted remote entry; loads clean; a glacial hut locates from snowy plains. Files restored from a backup (md5-checked), not git, since they carry uncommitted work.*
- [x] **ST-26 Way signs still avoid settlements:** `L supplementaries:way_sign` a few times → found, none inside a village (their exclusion zone now points at `creativeengineering:group/settlements`, 8 chunks). *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: 4 signs from 4 positions (160-641 blocks away), every one on land, no village start or reference in the sign's chunk.*

**Village houses and containers**
- [x] **ST-11 Pool tuner runs:** `pool_tag_report.md` is rewritten at world load → "645 pools, 3669 pieces", "12 pool(s) retuned", no errors section. PASSED 2026-09-27 (report from the 12:00 world load).
- [x] **ST-12 Tech houses gone:** `L minecraft:village_plains`, teleport, and walk 3 fresh villages → no Create: Dynamic Village, IE, PneumaticCraft or Advanced Peripherals houses. Pet shops, warehouses and chef houses still show up. *2026-10-14 scratch phase 3 (seed 8675309, 1.0.6 jar of 01:28, world st_d at structure density 4 with two Chunky squares; st_e for the off-switch comparisons; log feature_writeup/testing/structures/03_phase_3.md): PASSED (the walk is [game]): 0 tech-mod pieces in 42 saved village starts and in 146 generated villages (30 per vanilla type); pet shops in 83 of the 146, warehouses and chef houses present.*
- [x] **ST-13 Piece edit:** in `pool_config.json` → `minecraft:village/plains/houses` → `domesticationinnovation:plains_petshop` set `"enabled": false`, rejoin → `pool_tag_report.md` shows it **removed** in that pool. Revert. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED: pool_tag_report.md shows the plains pet shop **removed** in `minecraft:village/plains/houses` ("enabled: false").*
- [x] **ST-14 Container swap:** `/place template chefsdelight:plains_chef_house ~3 ~ ~3` → vanilla barrels stand where its oak cabinets and baskets were. Then find a chef house in a freshly generated village → those barrels are Lootr barrels (per-player loot). *2026-10-14 scratch phase 3 (seed 8675309, 1.0.6 jar of 01:28, world st_d at structure density 4 with two Chunky squares; st_e for the off-switch comparisons; log feature_writeup/testing/structures/03_phase_3.md): PASSED: `/place template` gave 5 Lootr barrels for the template's 1 basket + 4 cabinets with loot tables (3 decorative crates untouched); 4 chef/cook houses in generated villages hold exactly as many Lootr barrels as their templates have loot cabinets (5, 4, 5, 4), no cabinet left.*

**Structure searches**
- [x] **ST-15 No hang on a removed set:** `/locate structure #minecraft:ruined_portal` → "Could not find" within a second or two (the emptied set has no members, so there's nothing to search; `[locate] skipping frequency-0 structure placements` only appears if some other set sits at frequency 0). Then `/loot give @s loot minecraft:chests/shipwreck_map` → a real buried-treasure map (live sets still searched). *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): PASSED: `#minecraft:ruined_portal` "Could not find" in 0.5 s. `/loot` of `chests/shipwreck_map`: Async Locator logged "Location found - updating treasure map in chest" within 0.5 s (buried treasure 100 blocks from spawn, also found by `/locate`). The dropped or inserted map itself stays "menu.working": `/loot` hands out copies and AL fills in the original stack, which a real chest's slot holds. Test with a real chest, not `/loot`.*
- [ ] **ST-16 Monobank vaults drop no map:** `/reload`, log shows `[dead-maps] removed 5 explorer-map entries from 5 tables`. `/loot give @s loot monobank:monobank/village/plains` 30 times → emeralds/gold only, never a map. Then walk into a fresh plains village with a vault and open it → no freeze. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads; spawn -80 76 is open ocean): log + loot part PASSED: `/reload` logs `[dead-maps] removed 5 explorer-map entries from 5 tables`; 30 rolls of `monobank/village/plains` gave only gold/emerald items and blocks, no map. Vault visit still to do in a client.*

**Ground matching** (set `[worldgen.ground_match] log = true` first, in the toml or Create's config screen)
- [ ] **ST-27 Loads clean, grass stays on grass:** start the game → no mixin error for `StructureTemplateGroundMatchMixin` / `StructureStartGroundMatchMixin`; the toml has a `[worldgen.ground_match]` section. In a plains, `/place structure philipsruins:ancient_ruins` → the grass/dirt pad is still there, and no `[ground-match]` line is logged. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): no ground-match mixin errors, [worldgen.ground_match] there; the place check still to do.*
- [x] **ST-28 Volcanic peaks:** `/locate biome terralith:volcanic_peaks`, teleport, `/place structure philipsruins:ancient_ruins`, then `/place structure philipsruins:ancient_towers` a bit away → no grass or dirt under either; the pad is the local basalt/blackstone mix. Flowers and grass tufts on the pad are gone. The log shows `[ground-match] ... creativeengineering:ground_match/grass N% of M ground columns` with N below 35. *2026-10-14 scratch phase 3 (seed 8675309, 1.0.6 jar of 01:28, world st_d at structure density 4 with two Chunky squares; st_e for the off-switch comparisons; log feature_writeup/testing/structures/03_phase_3.md): PASSED: ruins and towers placed on volcanic peaks: grass share 0-34% (under 35), pads replaced with the local basalt/blackstone/smooth basalt, no grass, dirt or flowers left; `[ground-match]` lines as described. See the coverage bug under ST-30.*
- [ ] **ST-29 Natural generation:** with `log = true`, fly into fresh terrain across a desert or badlands and a mountain range → `[ground-match]` lines appear while chunks generate. Teleport to the chunk one names (`chunk [x, z]` × 16) → the structure there sits on the local ground.
- [x] **ST-30 Modes:** set `mode = "PALETTE"`, `/place structure philipsruins:ancient_ruins` on volcanic peaks → a full pad made of the sampled blocks. `mode = "REMOVE"` → no pad at all; the walls stand on bare terrain. Put `TERRAIN` back. *2026-10-14 scratch phase 3 (seed 8675309, 1.0.6 jar of 01:28, world st_d at structure density 4 with two Chunky squares; st_e for the off-switch comparisons; log feature_writeup/testing/structures/03_phase_3.md): PASSED after a retest: PALETTE kept no terrain (full sampled pad); REMOVE removed all 64 base blocks of the template, walls on bare basalt. The first two REMOVE tries (and a TERRAIN control) did nothing and logged nothing: sampling skips columns under ANY structure piece in XZ, and an ancient city at y -51 covered 95-99% of the sample points, so ground match gave up silently (bug, open).*
- [x] **ST-31 Exempt tag:** add `'philipsruins:ancient_ruins'` to `worldgen/ground_match.js`, `/reload`, and place it on volcanic peaks → the grass pad is back. Remove the entry and `/reload`. *2026-10-14 scratch phase 3 (seed 8675309, 1.0.6 jar of 01:28, world st_d at structure density 4 with two Chunky squares; st_e for the off-switch comparisons; log feature_writeup/testing/structures/03_phase_3.md): PASSED: with `philipsruins:ancient_ruins` in the exempt tag (scratch copy), no `[ground-match]` line and the grass pad kept; entry removed again.*
- [x] **ST-32 Plain templates untouched:** on volcanic peaks, `/place template philipsruins:ancient_ruin_1 ~2 ~ ~2` → grass pad as saved (only whole structures are matched), no `[ground-match]` line. *2026-10-14 scratch phase 3 (seed 8675309, 1.0.6 jar of 01:28, world st_d at structure density 4 with two Chunky squares; st_e for the off-switch comparisons; log feature_writeup/testing/structures/03_phase_3.md): PASSED: `/place template philipsruins:ancient_ruin_1` kept its grass 67 / dirt 52 as saved, no `[ground-match]` line.*

**Worldgen config** (edit `config/creativeengineering-common.toml` or Create's config screen; put every value back after each test)
- [ ] **ST-33 The section is there:** start the game → the toml has `[worldgen]` with `systems`, `debug`, `pools`, `biome_solver`, `ground_match` (+ `sampling`) and `mass_place`; Create's config screen shows them. A normal rejoin logs the usual `[structure-tuning] 9 groups, 97 structure_sets written ...` line (no DEBUG, no GROUPS OFF). *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): toml part passed (every listed section, plus locate/distant); Create config screen is a client check.*
- [x] **ST-34 Density up:** `structureDensity = 4`, rejoin → a WARN `DEBUG structure density x4 ... all exact` and `[structure-tuning] DEBUG density x4, 9 groups, 126 structure_sets written`. `structure_tag_report.md` shows every grid at about half its spacing. Fly into new terrain → clearly more structures of every kind. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED (flying is [game]): x4 lines; 3 subterrestrial sets stay below x4 at their densest grid (named in the WARN); every group grid halved. Counts now 15 groups / 214 sets.*
- [x] **ST-35 Density down:** `structureDensity = 0.25`, rejoin → same lines with x0.25; `L minecraft:village_plains` from a few spots → villages are much further apart. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED (the locate spread skipped: a rate): x0.25 lines with "all exact"; grids unchanged, frequency 0.25 on each (a lone plains village 1 per 96 chunks, was 48).*
- [x] **ST-36 Groups off:** `systems.structureGroups = false`, rejoin → WARN `structure groups are OFF` and `GROUPS OFF, 0 groups, 0 structure_sets written`; `L philipsruins:ancient_ruins` still finds one (its mod's own set). *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED: WARN `structure groups are OFF`, `GROUPS OFF, 0 groups, 1 structure_sets written` (the 1 is Revamped Shipwrecks' standalone set with every member disabled, written empty); ancient ruins locate on their own set.*
- [x] **ST-37 Frequencies off:** `systems.structureFrequencies = false`, rejoin → `[structure-frequency] off in CEConfig [worldgen.systems]` instead of the `109 structure(s) ... roll a frequency below 1` line. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED: `[structure-frequency] off in CEConfig [worldgen.systems]: every structure spawns whenever it wins its slot`, no below-1 roll line.*
- [x] **ST-38 Pool tuning off:** `systems.poolTuning = false`, rejoin → `pool_tag_report.md` lists the warning "pool tuning is off"; a fresh plains village can have Create: Dynamic Village houses again. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED: pool_tag_report.md warns "pool tuning is off"; 24 of 25 generated plains villages have Create: Dynamic Village houses (101 pieces) and some IE ones.*

**`/cdevplace`** (superflat world, Generate Structures OFF, cheats on)
- [ ] **ST-39 Preview:** `/cdevplace preview structures @towns_and_towers` → `56 structures in 7 row(s)`: type/camp 5, type/fort 9, type/ship 2, type/shipwreck 1, type/tower 15, type/village 23, (no type/* tag) 1. `/cdevplace preview templates kaisyn:village/*/houses/*` → one row per village style. *2026-09-27: the structures preview gave exactly this from a scratch dedicated server's console; templates preview and your client still to do.* *2026-10-19 scratch phase 9 (structure_tuning 8.0.9, seed 8675309, patcherMod 1.0.7 jar (= 1.0.8 MassPlacer); worlds st_9 (hills: the flat setting did not take) and st_9d (flat: biomeLayout off + a flat-overworld world datapack); log testing/structures/09_phase_9.md): structures part PASSED again (56 in 7 rows, counts exactly as written); templates part FAILED: `/cdevplace preview templates kaisyn:village/*/houses/*` throws "An unexpected error occurred" (vanilla listTemplates StringIndexOutOfBoundsException on a `structures.nbt` path at a namespace root; see ST-41).* *2026-10-19 fix rerun: templates part PASSED: `/cdevplace preview templates kaisyn:village/*/houses/*` lists 326 templates in 21 rows, one per house folder.*
- [ ] **ST-40 Whole mod in rows:** `/cdevplace structures @towns_and_towers` → progress in chat, then `/cdevplace done: N of 56 placed in x ..`; a sign in front of each row and each structure; no two structures overlap; failures (if any) listed with a reason. Walk the rows: villages complete, outposts with their pillagers. *2026-09-27, scratch dedicated server on normal terrain: the first version was killed by the 60 s watchdog at 38/56 (a 90-chunk village generated in one tick); after the step/tick-budget rewrite, `done: 55 of 56 placed`, `village_ocean` refused dry land. Flat world and walking the rows still to do.* *2026-10-19 scratch phase 9 (structure_tuning 8.0.9, seed 8675309, patcherMod 1.0.7 jar (= 1.0.8 MassPlacer); worlds st_9 (hills: the flat setting did not take) and st_9d (flat: biomeLayout off + a flat-overworld world datapack); log testing/structures/09_phase_9.md): scratch part PASSED: on hills and on a real flat world, `done: 55 of 56 placed`, `village_ocean` refused with its reason; a sign in front of each of the 7 rows and at each of the 55 structures; no two boxes overlap; all 29 outposts hold illagers, 22 of 24 villages villagers (the trader camp has traders; T&T `village_forest` is the forest_ruins set, abandoned by design). Walking the rows stays [game]. Note: no flat world is possible while `[worldgen.systems] biomeLayout` is on (its overworld dimension override outranks every preset and world datapack).*
- [ ] **ST-41 Templates:** `/cdevplace templates kaisyn:village/meadow_swiss/houses/*` (or any folder from ST-39's list) → houses in a row, no jigsaw blocks left, signs with each template id. *2026-10-19 scratch phase 9 (structure_tuning 8.0.9, seed 8675309, patcherMod 1.0.7 jar (= 1.0.8 MassPlacer); worlds st_9 (hills: the flat setting did not take) and st_9d (flat: biomeLayout off + a flat-overworld world datapack); log testing/structures/09_phase_9.md): FAILED: every `/cdevplace templates` (preview or place) throws: MassPlacer.templateIds calls StructureTemplateManager.listTemplates, which throws StringIndexOutOfBoundsException (begin 11, end 10, length 14) on a merged-listing path `structures.nbt`; no single pack lists it (a resource-manager mixin adds it). Fix: list templates per pack, keeping only `structures/` paths.* *2026-10-19 fix rerun (patcherMod 1.0.8 d60b5a99, world st_flat made with `level-type=minecraft:flat` and biomeLayout on): scratch part PASSED: `/cdevplace templates kaisyn:village/meadow_swiss/houses/*` placed 17 of 17 in 1 s on the flat surface (y -61), 0 failed; looking at the houses is [game].*
- [x] **ST-42 Status and stop:** start `/cdevplace structures by group @philipsruins @hopo`, run `/cdevplace status` (progress line naming the structure in progress and its chunks loaded/placed), then `/cdevplace stop` → `stopped at n/N`, nothing more is placed. *2026-10-19 scratch phase 9 (structure_tuning 8.0.9, seed 8675309, patcherMod 1.0.7 jar (= 1.0.8 MassPlacer); worlds st_9 (hills: the flat setting did not take) and st_9d (flat: biomeLayout off + a flat-overworld world datapack); log testing/structures/09_phase_9.md): PASSED: `/cdevplace structures by group @philipsruins @hopo` (33 in 5 rows); `/cdevplace status` -> "11/33 started, 10 placed, 0 failed, row 3/5, now philipsruins:ancient_towers (chunks loaded 2/2, placed 0/2)"; `/cdevplace stop` -> "stopped at 18/33", nothing placed after.*
- [ ] **ST-43 Converted structure mods in the client:** the game starts with no `NoSuchMethodException ... <init>()`. In a flat world, `/cdevplace structures @mysticrift_pillagers_towers_aris @skyvillages @saloon @villager_forge @illagerwarship` → all placed. Look for supplementaries urns where the 1.20 decorated pots were, Vinery cherry wood on the cherry tower, and Sky Villages with no empty gaps. *2026-09-27: all 56 placed on a scratch dedicated server (0 failed); client not yet.*

**Water biomes, IPO, driftwood** (2026-10-03)
- [x] **ST-44 Rule applies:** latest.log has `[structure-biome-rules] water: 12 biomes, ... kept out:` listing the 8 `mysticrift_pillagers_towers_aris:*` towers; `structure_tag_report.md` has a "Biome rules" section. *2026-10-03: both on a scratch dedicated server (25 kept out, 31 allowed that can start in water).* *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED (rule section + water line; the Mysticrift towers are no longer in the kept-out list because their biome fits already keep them on land: 151 -> 1..36 biomes).*
- [ ] **ST-45 No towers at sea:** fly over 2,000+ blocks of new ocean → no Mysticrift tower stands in the water. Ships, rafts, wrecks and ocean ruins still show up. `L mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` from far out at sea → the answer is on land. *2026-10-13 scratch phase 2 (world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): locate part PASSED: `oak_wood_pillager_tower` from 48 -20 (deep ocean) answers at 4384 -288 (gravel beach, the tower box on land) and from -2976 -2400 at 832 -8992 (`terralith:haze_mountain`). Flying the ocean is a client check.*
- [x] **ST-46 Switch:** set `[worldgen.systems] structureBiomeRules = false`, rejoin → the log says the rules are off; set it back. *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED: `[structure-biome-rules] off in CEConfig [worldgen.systems]: every structure uses its own biome list`.*
- [x] **ST-47 IPO mix:** `/place structure minecraft:pillager_outpost` about 16 times in a flat area → mostly vanilla watchtowers, about 2 tall IPO towers. No outpost is a bare plate without a tower, and no IPO tower is missing its top. *2026-10-03: pool weights 7:1 and the renamed IPO pieces confirmed in the scratch server's pool_config.json; not placed yet.* *2026-10-14 scratch phase 3 (seed 8675309, 1.0.6 jar of 01:28, world st_d at structure density 4 with two Chunky squares; st_e for the off-switch comparisons; log feature_writeup/testing/structures/03_phase_3.md): PASSED (look is [game]): 40 outposts generated with a probe (no placement): 34 vanilla with their watchtower, 6 IPO, each IPO with tower and top stacked; the placed IPO outpost is complete up to its top (y 156).*
- [x] **ST-48 Driftwood:** new beach and ocean shoreline → driftwood still appears, but clearly sparser than before. *2026-10-03: the scratch server created a world with the 5 overrides, so the JSON parses.* *2026-10-14 scratch phase 3 (seed 8675309, 1.0.6 jar of 01:28, world st_d at structure density 4 with two Chunky squares; st_e for the off-switch comparisons; log feature_writeup/testing/structures/03_phase_3.md): PASSED ("than before" not compared): outside meta structures, 14 logs float on open ocean and 4 lie on beach sand in about 20k chunks; at density 4 the flotilla metas hold most driftwood.*

**Banks, driftwood, earth fill** (2026-10-03)
- [ ] **ST-49 Banks:** rejoin; `pool_tag_report.md` shows `monobank:village/houses/plains_vault` at 2.7 and `monobank:village/streets/plains_straight_fix_01` at 2.4 (final chance about 2.0% and 4.6%), `monobank/vault_houses x0.18` and `monobank/vault_streets x0.16` listed, no errors. Then `L minecraft:village_plains` 6 times and walk each: banks (a vault building with its door, or a straight street with a vault beside it) should be uncommon, roughly 1 village in 4 or 5 instead of most of them. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): pool report: plains vault 15 -> 8 (2.0%), plains straight street 15 -> 5 (4.8%), both multipliers listed; weights are now whole numbers (8 and 5, not 2.7 and 2.4) with the same final chances. Village walk still to do.*
- [ ] **ST-50 Driftwood:** in new terrain, walk a beach, a swamp, a river and open ocean: driftwood floats in the shallows and on the sea, a log lying on dry sand or grass is now a rare find (beach about 1 per 160 chunks). A river bank has none. `latest.log` has no `upgrade_aquatic:driftwood_*` or `creativeengineering:driftwood_land/*` parse error. *2026-10-14 scratch phase 3 (seed 8675309, 1.0.6 jar of 01:28, world st_d at structure density 4 with two Chunky squares; st_e for the off-switch comparisons; log feature_writeup/testing/structures/03_phase_3.md): FAILED (shallows): no driftwood parse errors of ours, dry-land logs rare (4 in 1055 beach chunks), no river-bank logs, BUT no floating driftwood in 611 river chunks, 454 swamp chunks or any beach water: Upgrade Aquatic's DriftwoodFeature, outside oceans, only places with dirt or sand directly under its origin, and our water versions filter for water there, so they can never place (bug, open).*
- [ ] **ST-51 Loads clean:** the log has no mixin error for `ChunkGeneratorEarthFillMixin`; the toml has `[worldgen.earth_fill]` and `systems.earthFill`; `/reload` and `/dumpstructuregen` still work. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): mixin/toml part passed, /dumpstructuregen works; /reload not yet run.*
- [x] **ST-52 Log run (calibration):** set `[worldgen.earth_fill] log = "ALL"`, rejoin, fly over 2,000+ blocks of fresh terrain (or `[worldgen.debug] structureDensity = 4`). `latest.log` has `[earth-fill] <id> at chunk [x, z] (<size>, <beard>): N earth, W over water (limit 24), A over air after beard (limit L), M mound columns (limit K) -> kept|CANCELLED` lines (format since 7.3.1). Collect them per size, then set `[worldgen.earth_fill.sizes]`, `maxOverWater` and any `overrides` from what you see (the four sizes' numbers are first guesses). Put `log` back to `CANCELLED`. *2026-10-19 scratch phase 7 (structure_tuning 8.0.7, seed 8675309, patcherMod 1.0.7 jar (worldgen identical to 1.0.8), density 4, worlds st_c / st_c2 / st_c3; log testing/structures/07_phase_7.md): DATA: main square r 800 at -1400 -2592, default limits, prune working: small 1833 starts / 160 cancelled (141 underground, mostly Subterrestrial ocean cabins in open sea), medium 276 / 22, large 12 / 1, huge 1 / 1 (drum tower); surface-only 9% small, 12% medium. Per-size percentiles in the log 2.1; no limit change proposed.*
- [ ] **ST-53 A cancel:** set every `[worldgen.earth_fill.sizes]` list to `[0, 0]`, `maxOverWater = 0` and `beardReach = 0`, rejoin, fly into new terrain: structures with any earth over a gap in their base now log `CANCELLED` and are missing on slopes and shores, and the set's next member may take the slot (a one-structure set leaves it empty). Put the values back. *(Text corrected 2026-10-19 after the phase 7 run.)* *(Keys changed 2026-10-13, 7.3.1.)* *2026-10-19 scratch phase 7 (structure_tuning 8.0.7, seed 8675309, patcherMod 1.0.7 jar (worldgen identical to 1.0.8), density 4, worlds st_c / st_c2 / st_c3; log testing/structures/07_phase_7.md): PASSED except one clause: at zero, every start with any water/air/mound gap cancels (85 of 498 that stood at the defaults) and none without one; cancelled starts are missing from their chunks. "The slot stays empty" holds only for one-structure sets: in a group the set tries its next member (vanilla loop), e.g. jungle cabin -> deepslate cabin, taiga village -> T&T OGT village, judged by earth fill too.*
- [x] **ST-54 Overrides and exempt:** set `overrides = ["minecraft:village_plains=-1,-1,-1", "philipsruins:ancient_ruins=0,0,0"]`: plains villages never log a cancel, ruins cancel at any earth over water or air past the beard, or any mound column. A structure in `earth_fill/exempt` (a ship, a sky balloon, the mansion) never logs, even at 0. *2026-10-19 scratch phase 7 (structure_tuning 8.0.7, seed 8675309, patcherMod 1.0.7 jar (worldgen identical to 1.0.8), density 4, worlds st_c / st_c2 / st_c3; log testing/structures/07_phase_7.md): PASSED: `-1,-1,-1` tested on village_savanna (no plains in the square): 5 starts with gaps, none cancelled; `philipsruins:ancient_ruins=0,0,0` cancels at 3 and 7 air, keeps 0; no exempt id logged a line (Hopo ruins in the square logged only water-fill lines).*
- [x] **ST-55 Switch:** `[worldgen.systems] earthFill = false` → no `[earth-fill]` lines, nothing cancelled. A world with the switch on at the default limits still has villages (simulated with the 7.3.1 rules: 22 to 40% cancelled, nearly all over water deeper than 6; see 3.13). *2026-10-14 scratch phase 4 (seed 8675309, 1.0.6 jar of 01:28, density 1, world st_d unless noted; log feature_writeup/testing/structures/04_phase_4.md): PASSED: no `[earth-fill]` line over 18 new starts that earth fill normally logs; villages exist with it on (Phase 3).*

## 9. History

- **2026-11-02 late: worldgen edit plan steps 3 to 5 ([plan_worldgen_edit.md](plan_worldgen_edit.md)).** Meta part flag `punch` (3.16): an AE2 meteorite part gets crater type NONE plus a `MetaPunchPiece` shaft up through the pieces placed before; the struck desert pyramid uses it at distance [0, 4] (`metas #12`). Free-standing `ae2:meteorite` starts: 40 % surface, 30 % underground, 30 % 16 to 96 blocks up, moved ones without a crater (`AeMeteoriteHeightMixin`, `MeteoriteHeights`; metas keep the surface). Meta `meta/waystone_ruins` (share 0.2 on small ruins) with part `creativeengineering:waystone_ruin_part` (no structure set). Shares: camp ruin metas 0.25 [0.15], great tower siege 0.30 [0.50], meteor shower 0.33 [0.50]. Spawn box `end_tower`; probes: the lone birch tower and every siege part already resolve to the right box (`contents #6`, `pillagers #6`). This code moved to Creative Worldgen in the wave 5 carve the same evening. Tests SQ-WGE-09 to -15.
- **2026-10-19:** the 3.23 fixes went out in pack 1.0.7 (DEPLOYED 2026-10-18 19:43; [plan_1.0.7.md](plan_1.0.7.md) SF):
  status lines updated. Nothing in this area changed for 1.0.8. ST-191 to ST-198 still pending.
- **2026-10-18: six bugs from the scratch run fixed in source (3.23), for patcherMod 1.0.7, not built.** User: "tackle
  the structure and locate bugs in section 3" (TODO.md 3). Found the cause of the per-restart village layouts (pool
  order from the mods changes between JVM runs; 10 report versions in git show 9 desert orders) and fixed it with a
  canonical pool order; processor lists rebound for pieces added in code; ground match's column exclusion by height
  plus a skip log line; driftwood filters moved to water-adjacent banks; `land/any/deep` for the three deep dungeons;
  opt-in stone bases in earth fill. The locate fixes are in [locate.md](locate.md) 8. Offline checks pass;
  ST-191 to ST-198 added.
- **2026-10-14: shipped INCOMPLETE in pack 1.0.6.** User: "ship all the changes as 1.0.6. just mark the structure
  tuning as incomplete in this release". The 1.0.6 payload (update from 1.0.4) carries patcherMod 1.0.6 (12:44 rebuild of the
  01:28 source the Phase 3/4 scratch runs used) and all worldgen data through 7.4. Open bugs at staging: ground match over
  deep structures, beach/river/swamp driftwood, cave biomes missing from `land/any`, earth fill blind to stone bases,
  undead castle cancels, census all "known", ST-135; Phases 5 to 10 not run. See [plan_1.0.6.md](plan_1.0.6.md) 2.1.
- **2026-10-14: plan 7.4 built** (milestone M1 of the structure testing run, feature_writeup/testing/structures/).
  Campsite Structures' 6 features -> real `remote` structures (passive); Camps. Castles. Carriages. 2.3.4 ported to
  `kubejs/data/eugene` (porter: single quotes, unpacked dirs, 1.20 block remaps, absent-mod entities stripped,
  `ride`/`damage` rewritten), 1.1.1 moved to `disabledmods/archive/`; camp and castle pools split; new `vehicles`
  group with a `biome_frequency` knob (Java/JS/python, harsh 1.0 / default 0.5); fortified-town shares 0.20 / 0.20 /
  0.40. Offline checks all pass (test_structure_groups.sh, test_worldgen_tools.sh, FitTest 181). Loot catalog
  regenerated; 2.2, 3.16, 3.22 updated. structures_by_biome.md not regenerated (rates out of scope, user). Piglin camp:
  earth fill never sees netherrack/lava as earth, so it relies on the mod's own `beard_box` (no code change).
- **2026-10-14: plan 7.4 written, then redone on Camps. Castles. Carriages. 2.3.4** (user: "redo the plan with the
  mod camps, castles, carriages ... i decompiled the latest mod version in decompiled mods"). First draft guessed the
  castle/cart mods; redone from the decompiled 2.3.4: `eugene:camp` and `eugene:castle` each mix friendly and hostile
  templates, so they're split (camp -> passive camp + pillager camp + fake trader camp; castle -> undead castle /
  red roof castle / drum tower). 1.20 content to port: suspicious gravel/sand, decorated pot, chiseled bookshelf,
  hanging sign, Diagonal Fences/Windows blocks, a trim template in loot, `ride`/`damage` commands (they break the
  whole tick function on 1.19.2). Campsite Structures places its camps as features in every biome (live, outside all
  rules). Piglin camp: fixed y 33, may hang over the lava sea. Tests ST-170 to ST-190.
- **2026-10-13: plan 7.3 built (7.3.1, 7.3.2).** User: "implement the 7.3 changes for structure tuning". Earth fill
  counts what the beard can't fill (reach measured with vanilla's Beardifier: 6, bury 10), mound columns, limits by
  the start's own size (no size tags or tool: the count at generation is exact), `maxOverAir` removed, water under the
  beard treated like air (the 10-03 "0 over water" was unbound fluid tags in the simulation), mansion exempt.
  `/locate` through metas: dimension check, per-slot target prediction (`meta_target`), `searchTimeoutSeconds` 120,
  `/cwglocate stop` and server stop end searches. patcherMod 1.0.6 rebuilt in `build/libs`; worldgen tests pass;
  ST-158 to ST-166.
- **2026-10-13: earth-fill pruning.** User: "since villages are being canceled by houses over water, could just those
  houses of the offending jigsaw be canceled instead implement this change but dont scratch test it yet". Pieces of
  `earth_fill/prune` structures over `pruneMaxHanging` / `pruneMaxMound` are dropped with their jigsaw subtree (tree
  rebuilt from junctions); simulated whole-village cancels 22-40% -> 5-12%. EarthFillTest 53 checks. ST-167 to ST-169,
  not scratch-tested.
- **2026-10-19: scratch run Phases 7 to 10 (structure tuning testing finished).** User: "pick up structure tuning
  testing where you left off". New scratch server (Forge libraries from servers/Minecraft1192withForge), patcherMod
  1.0.7 jar because the staged 1.0.8 crashes a dedicated server (Serene Seasons clock mixin; plan_1.0.8.md blocker);
  worldgen identical. Phase 7 (calibration): earth-fill pruning never ran live (`earth_fill/prune` empty because
  `type/village` is not a real structure tag; ST-167 failed as shipped, passed with a scratch-only fix); pruning can
  drop a start a size class; `/dumpstructuregen` broken. Phase 8 (two-world comparisons): all passed (ST-148 with the
  town companion distance loosened). Phase 9 (`/cdevplace`): templates mode broken (vanilla `listTemplates` on a
  merged `structures.nbt` path); no flat world while `biomeLayout` is on. Phase 10: per-status worldgen times in
  locate.md 3.7. Logs: testing/structures/07_phase_7.md to 10_phase_10.md. Nothing in kubejs/, config/ or the
  patcherMod source was changed.

- **2026-10-19: earth fill on water, camp plinths, metas off coasts (7.5).** User: "address the structure earth fill
  issues i outlined in my scratchpad ...". The footprint water share in earth fill (`maxWaterSurplus` 0.5, buried spots
  ignored), sunk-start beard (`terrain_adaptation/sunk_start`, the 6 Campsite camps, start height -4), the
  `avoidCoasts` switch (off) with `coast_river` and `meta/coast_ok`. Scratch-tested with the Quark spire teleport
  mixin: ST-199 to ST-205 passed (testing/structures/12_earth_fill_camps.md). patcherMod 1.0.9 build, not deployed.
- 2026-10-19: status sweep: the 1.0.6 rows marked STAGED / `build/libs` went out with 1.0.6 (2026-10-14); 7.5 (earth fill on
  water, camps, coasts) DEPLOYED with 1.0.9 (15:54). Header rewritten.
- 2026-10-19: ST-53 and ST-160 test text corrected from the phase 7 run ([testing/structures/07_phase_7.md](testing/structures/07_phase_7.md) 5.5): the group's next member may take a cancelled start's slot; small villages log `small`.- 2026-10-22 (doc audit): ocean geode lowest-floor cap: jar state (in the `mods/` jar of 11:06, not in `server_mods/`
  or the scratch-run jar), ST-133 expectation corrected (moved tops at or below y -10, not -12), scratch queue SQ-GE
  and probe `tools/farm_worldgen_probe.js`.
- 2026-10-22 (second bulk run): SQ-GE-00 to SQ-GE-04 PASS on the scratch server after the prismarine fix
  (`OceanGeodes.untouched` reads the alternate inner layer); ST-133 scratch part recorded, the in-game look stays.
- 2026-10-23: desert temples split into `group/desert_temples`, 32 / 20 (3.24, `structures/rates-and-groups #10`);
  mimic frequency lowered to keep its rate. Tests ST-225 to ST-227. Not run.
- 2026-10-24: planned from the scratchpad: desert structures in water, rare ice (`structures/earthfill #11`, `blocks/new #12`). No code changed.
- 2026-10-25: deadwood metas and doubled Geophilic stumps / fallen logs (3.25, TODO `farming/trees #2`). Tests ST-228 to ST-231. Not run.
- 2026-10-27: worldgen performance work, details and measurements in [perf.md](perf.md) 10.6 to 10.9 and 11.2 (TODO `server #13, #14, #18`). Built: `DimensionFilter` (structure checks skipped in dimensions they do not belong to), the `kubejs/data` Tectonic cliffs override, the shared terrain cache for EarthFill and PlacementPriority, `sampleGrid` 2 -> 4, `FirstTickWater` (no item drops from generated water in a settling window after a chunk first ticks), and the stronghold ring position cache in the world's data. Options 1-4 together: pregen 5:26 -> 3:50, census 886 -> 424 s. Tests SQ-WGP-01 to -08, SQ-FTW-01 to -06, SQ-RING-01 to -03, CQ-WGP-01/-02, CQ-FTW-01. Option 5 (reuse predicted starts) will not be built. Open: Terralith underground census false negatives (`structures/testing #4`).

- 2026-10-28: planned from the scratchpad: more water-replace checks for lakes, tent camps and ruins floating in water, seen on a client scratch server (TODO `structures/earthfill #12`). No code changed.
- 2026-10-30: earth-fill fixes (3.13, TODO `structures/earthfill #9`, `#10`, bugs A and B of [briefs/structures_earthfill.md](briefs/structures_earthfill.md)). An override with every limit -1 is never cancelled under `log = ALL` (only measured), so a `log = ALL` run cancels exactly what `CANCELLED` does. A sunk start's beard reach and mound test start at its bottom plus the sink (`EarthCheck.cancels`), so earth over a gap the raised beard no longer reaches counts; expect slightly more Campsite cancels on slopes. Pruning (`pruned`) still uses the unsunk bottom; no pruned structure is sunk today. Offline: EarthFillTest 74 checks green. Tests SQ-WG30-A01 to -A03 (scratch_queue.md), with reruns of ST-54 at `log = ALL` and ST-199. Scratch 2026-10-30: A01 PASSED (34 village starts under an all -1 override, all kept); A02 PARTIAL (27 Campsite starts, 1 cancel, on water; ST-199's camp at [-7, -9] is no longer picked after the weight strip, a sampled sunk camp sits flush; no old-jar comparison); A03 PARTIAL (the -1 village kept, the limit-0 ruins never had earth over water or air, so their cancel was not exercised).

## Owner notes (scratchpad 2026-11-07)

- Waystones require a diamond or netherite pickaxe to break (owner). Row `structures/contents #10`.
