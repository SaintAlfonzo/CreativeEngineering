# Structure tuning: how often every structure and village house generates

Test prefix **ST-** · Last updated **2026-10-03** · Overall status **DEPLOYED, in-game test pending** (structure groups
replaced the per-set + multiplier model on 2026-09-27 evening; jar in `mods/`, needs a rejoin: ST-17 onward). Ground
matching (3.8), the CEConfig `[worldgen]` section with its debug density (3.9) and `/ceplace` (3.10) are DEPLOYED
2026-09-27 (jar md5 729953e6); the structure lines, live-set check and `/ceplace` ran on a scratch dedicated server, your client not yet: ST-27 to ST-42

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
| `/ceplace`: every structure or template of a filter in rows (flat-world testing) | patcherMod `worldgen/MassPlacer` | DEPLOYED 2026-09-27; filter/row/sign/alignment logic tested headless (30 checks); a real run on a scratch dedicated server placed 55 of 56 T&T structures after the watchdog fix. ST-39 to ST-42 |
| Data-only structure mods converted to `kubejs/data` (33 jars, 56 structures) | `tools/port_structure_jars.py` | Done 2026-09-27; scratch server boots, 56 of 56 placed by `/ceplace`, live sets match. Client untested. ST-43 |
| Biome rules: only water structures start in oceans and rivers | `biome_rules` in `structure_groups.json` + tag `water_ok`; patcherMod `worldgen/StructureBiomeRules` + `ChunkGeneratorStructureBiomeRulesMixin`, CEConfig `structureBiomeRules` | BUILT 2026-10-03, patcherMod 1.0.4 in `mods/` (md5 ab58494b); 6 Java checks, node/Rhino parity; a scratch dedicated server applied it (25 kept out). ST-44 to ST-46 |
| Improved Pillager Outpost as a 1-in-8 tower, not a replacement; IPO template names fixed; driftwood at 0.25x | `kubejs/data/{minecraft,ipo,upgrade_aquatic}` | BUILT 2026-10-03; the scratch server loaded the pools at 7:1 and created the world with the driftwood overrides. ST-47, ST-48 |
| Monobank banks 5x rarer in villages | `pool_tags.json` tags `monobank/vault_houses` x0.18 and `monobank/vault_streets` x0.16 | BUILT 2026-10-03; chance per pool computed from the 2026-10-03 pool report, not yet read back from a game load. ST-49 |
| Driftwood: in the water at the 0.25x rate, on land about a tenth of that | `kubejs/data/upgrade_aquatic` (5 placed features gated to water) + `kubejs/data/creativeengineering` (3 land placed features + 3 biome modifiers) | BUILT 2026-10-03; the 8 placement lists parse against the 1.19.2 codecs headless; not loaded in a game. ST-50 |
| Earth fill: cancel a structure whose foundation needs too much earth over water or air | patcherMod `worldgen/EarthFill` + `ChunkGeneratorEarthFillMixin`, CEConfig `[worldgen.earth_fill]`, tags in `blocktags.js` + `worldgen/earth_fill.js` | BUILT 2026-10-03 (patcherMod 1.0.4, not in `mods/`: the game was running); 21 headless checks + a vanilla terrain simulation; mixin slot checked against the SRG bytecode, never applied in a game. ST-51 to ST-55 |
| Water fill: cancel a water structure whose water would replace too much dry ground or air | `worldgen/EarthFill` (same hook), CEConfig `[worldgen.water_fill]`, tags in `worldgen/water_fill.js` | BUILT 2026-10-08 (patcherMod 1.0.5 build, not in `mods/`); 32 headless checks; defaults not calibrated. ST-56 to ST-60 |
| Terrain adaptation by tag (none / bury / beard_thin / beard_box / **soft**) and template trim (no square pads or air notches) | patcherMod `worldgen/TerrainAdaptation` + `TemplateTrim`, 2 new mixins + the ground-match mixin, CEConfig `[worldgen.terrain_adaptation]`, tags in `worldgen/terrain_adaptation.js` + `blocktags.js` | BUILT 2026-10-09 (patcherMod 1.0.5 build, not in `mods/`: the game was running); 50 headless checks (soft beard equals vanilla's at skirt 8, real templates trimmed as a python read predicts); mixin targets checked against the Forge-patched SRG bytecode, never applied in a game. ST-61 to ST-68 |

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
- Per village house: `current` weight / `enabled` in `pool_config.json`, or a `multiplier` in `pool_tags.json`.
- Lootr: block → vanilla container pairs in `loot_container_swaps.json`.
- `config/creativeengineering-common.toml` `[worldgen]` (also in Create's config screen), the layer above the JSON tables:
  - `[worldgen.systems]`: one on/off per system (structureGroups, structureFrequencies, poolTuning, lootContainerSwaps,
    biomeLayout, biomeMerges, deadPlacementFilter, groundMatch, structureBiomeRules, earthFill, waterFill,
    terrainAdaptation, templateTrim).
  - `[worldgen.debug] structureDensity`: every structure's rate times x (testing only).
  - `[worldgen.pools]`, `[worldgen.biome_solver]`: the solvers' constants.
  - `[worldgen.ground_match]` (+ `.sampling`), `[worldgen.earth_fill]` (limits, 3.13), `[worldgen.water_fill]` (3.14),
    `[worldgen.terrain_adaptation]` (soft beard `skirtBump`, 3.15), `[worldgen.mass_place]` (`/ceplace` layout).
- Ground matching: `[worldgen.ground_match]` (mode, baseLayers, nativeShare, minSamples, families, log, sampling); which
  blocks are base / plants / not-ground: the `creativeengineering:ground_match/*` block tags in `blocktags.js`; opt a
  structure out: `creativeengineering:ground_match/exempt` in `worldgen/ground_match.js`.
- Terrain around a structure (3.15): the structure tags `creativeengineering:terrain_adaptation/{none,bury,beard_thin,beard_box,soft}`
  and `creativeengineering:template_trim` in `worldgen/terrain_adaptation.js`; pad / plant blocks: the
  `creativeengineering:template_trim/*` block tags in `blocktags.js`; soft skirt width: `[worldgen.terrain_adaptation] skirtBump`.
- Inspecting structures: `/ceplace preview structures <filter>`, `/ceplace structures [by type|group|mod|none] <filter>`,
  `/ceplace templates <glob>` (3.10).

**Applying:** rejoin the world (worldgen is read at world load; `/reload` only refreshes the report). Only chunks
generated afterwards change.

**Measuring:** `python3 tools/gen_structure_groups.py --check` (expected rate of every structure), `[worldgen.debug]
structureDensity` to see many structures fast, `/ceplace` to see every structure of a mod side by side,
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
| Groups | 9 | 187 structures on 9 shared grids; the other 82 stay in standalone sets |
| Structure tags | 122 | 76 `mod/*`, 20 `type/*`, 12 `dungeon/*`, 9 `group/*`, 5 meta (`dungeons`, `pillager`, `nether`, `end`, `ruins`) |
| Live structures in no set | 142 | never generate on their own: 133 `terralith:zpointer/*` biome markers, the 7 ruined portals (removed on purpose), `nullscape:phantom_nest`, `terralith:underground_cabin` |
| Jigsaw pools | 609 | 3571 pieces, read live from the game, so code-injected houses are included |
| Pool tags | 15 | 4 tech-house mods at x0 + `tech` x0, 10 other house mods at x1 |

### 2.2 The groups

| Group | Members | Spacing | Separation (min gap) | Notes |
|---|---|---|---|---|
| `settlements` | 30: vanilla and T&T villages, Terralith fortified villages | 48 | 12 (208 blocks) | set by hand 2026-09-27 (was 24 / 10); The Missing Villages removed the same day |
| `pillager_settlements` | 29: vanilla outpost + every T&T tower/fort/camp outpost but the ship | 32 | 8 (144 blocks) | kept 4 chunks off `settlements` grid slots |
| `ruins` | 14: Philip's surface ruins, Terralith glacial hut | 8 | 4 (80 blocks) | underground ruins stay standalone |
| `surface_dungeons` | 8: YUNG's temples + witch huts, mansion, Terralith spire, T&T mimic, Philip's badlands dungeon | 16 | 10 (176 blocks) | vanilla temples YUNG's blocks stay out |
| `ocean_ruins` | 10: vanilla, Hopo's and Philip's ocean ruins, Philip's ocean fortress, shipwrecks, T&T wreckage | 16 | 2 (48 blocks) | |
| `ocean_dungeons` | 1: ocean monument | 32 | 6 (112 blocks) | the monuments' own salt; monument frequency 0.1 |
| `sky` | 3: Cloud Storage balloon + sky temple, AE2 meteorite | 72 | 16 (272 blocks) | |
| `boats` | 2: T&T ship village + pillager ship | 48 | 28 (464 blocks) | |
| `remote` | 37: T&T wandering trader camp + every single-building house mod in the Overworld | 16 | 8 (144 blocks) | 2 Farmer's Structures houses whose biomes come from absent mods stay out |

Every spacing and separation above was set by hand on 2026-09-27 (late evening); member weights and frequencies are
still the migration fit, so each group's rate changed by (old spacing / new spacing)^2: settlements x0.25, ocean ruins
x0.25, boats x0.73, remote x1.13, surface dungeons x1.27, ruins x1.9, sky x2.9, the other two unchanged.
Per-biome detail and holes: [structures_by_biome.md](structures_by_biome.md).

Standalone (73 structures): nether, End, underground (dungeons, ancient city, mineshafts, Terralith and Philip's
underground sets, buried treasure), strongholds, Hopo's fossils, way signs, ruined portals (all off),
and the YUNG's-blocked vanilla temples.

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
village doesn't change. Current pool tags remove 57 tech pieces: IE 25, Dynamic Village 20, PneumaticCraft 7,
Advanced Peripherals 5. Example: in `minecraft:village/desert/houses` a Dynamic Village house was 8.8% of each
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
| `minecraft:desert_pyramid` | YUNG's Better Desert Temples | `betterdeserttemples-forge-1_19.toml` "Disable Vanilla Pyramids" = true |
| `minecraft:jungle_pyramid` | YUNG's Better Jungle Temples | `betterjungletemples-forge-1_19_2.toml` "Disable Vanilla Jungle Temples" = true |
| `minecraft:swamp_hut` | YUNG's Better Witch Huts | `betterwitchhuts-forge-1_19.toml` "Disable Vanilla Witch Huts" = true |
| `minecraft:fortress` | YUNG's Better Nether Fortresses | `betterfortresses-forge-1_19.toml` "Disable Vanilla Nether Fortresses" = true |
| `minecraft:stronghold` | YUNG's Better Strongholds | always, no option |
| `betterdungeons:small_nether_dungeon` | YUNG's Better Dungeons | `betterdungeons-forge-1_19.toml` "Enable Small Nether Dungeons" = false |

The three blocked vanilla temples are kept out of `group/surface_dungeons` (they'd only occupy slots) and stay in their
own sets; the blocked stronghold and fortress are standalone. In `minecraft:nether_complexes`, a cell that picks the
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
| Philip's Ruins | `philips-ruins-common-configs.toml` | "Enable Configs" = false | its separation/density and per-type toggles are inactive; its rates are in the `ruins` group and standalone sets |
| Applied Energistics 2 | `ae2/common.json` | generateMeteorites = true | false would stop AE2 registering `ae2:meteorite` at all (see 4) |
| Supplementaries | `supplementaries-common.toml` (backed up) | `[building.sign_post.way_sign] enabled = true` | way-sign structures on; also tunable as `supplementaries:way_sign` |
| Cloud Storage | `cloud-storage.toml` | `balloon_stand_spawn_weight` = 2 | village-pool weight of its balloon stands (seen by PoolTuner); its 2 sky structures are the `sky` group |
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
| `systems.biomeMerges = false` | Structures keep their mods' own biome tags | `/reload` |
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

### 3.10 `/ceplace`: every structure or template of a mod, in rows (added 2026-09-27)

For looking at structures side by side, typically in a **superflat world with Generate Structures OFF** and cheats on.
Operator only.

- `/ceplace structures <filter>`: rows by type. `/ceplace structures by <type|group|mod|none> <filter>` picks another
  grouping. The filter takes terms like `structure_tags.json` values: an id, a glob (`towns_and_towers:*village*`),
  `@namespace`, `#type/tower` (a pack tag), `#minecraft:village` (a real structure tag), and `!term` to exclude.
  Tab-completion offers all of these.
- **Rows by type:** each structure goes in its **smallest** `type/*` tag, so outposts land in camp/fort/tower, not the
  `enemy_building` union. Structures with no type tag get a last row. For Towns & Towers (56 structures) that gives camp
  5, fort 9, ship 2, shipwreck 1, tower 15, village 23, and 1 untagged (`mimic_desert`).
- `/ceplace templates <glob>`: raw templates, one row per folder, jigsaw blocks turned into what they become. Towns &
  Towers keeps its 742 templates under the `kaisyn` namespace, for example `/ceplace templates kaisyn:village/*/houses/*`.
- `/ceplace preview structures|templates ...` lists the rows without placing. `/ceplace status` and `/ceplace stop`
  report on or cancel a run.
- **Layout:** rows start 3 blocks in front of you and run +X; each new row is placed further +Z, with a `rowGap` aisle
  between. A sign faces the aisle for each row and each structure (id, wrapped). Structures are generated exactly as
  `/place structure` does (any biome allowed, whole jigsaw layout), then moved onto the row cursor. `verticalAlign AUTO`
  drops sky structures and lifts underground ones to the ground (KEEP / GROUND also available); surface ones keep their
  height. Work runs in steps (lay out, then generate and build one chunk at a time) until `tickBudgetMs` (40) is used
  each tick, so a 90-chunk village spreads over many ticks; `maxItems` refuses bigger runs (1000). Chat shows progress; every placement
  and failure goes to `latest.log` (`[ceplace]`).
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

Rivers and oceans have no land version, so a river bank loses its driftwood. Edit the `chance` in a land file (or
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
2. where each stands in the world: piece position + rotation and mirror (the same transform the placement uses).
3. what the generator's own terrain holds there: `ChunkGenerator#getBaseColumn` for the column (read once per
   `sampleGrid` x `sampleGrid` cell, 2 by default, and cached per start), then the block at that Y: water, air, or
   solid. A block over water counts toward `maxOverWater`, over air toward `maxOverAir`; solid doesn't count.
4. Counting stops at the first limit crossed (unless `log = ALL`). A position in two pieces counts once.

Village streets and anything else with the `terrain_matching` projection are skipped: a gravity processor drops them onto
the ground column by column, so they never hang.

**Config** (`[worldgen.earth_fill]`; on/off `[worldgen.systems] earthFill`; applies to new chunks, read live):

| Key | Default | Meaning |
|---|---|---|
| `maxOverWater` | 24 | earth blocks allowed in water; -1 = no limit, 0 = none |
| `maxOverAir` | 256 | earth blocks allowed in air; -1 = no limit, 0 = none |
| `baseLayers` | 3 | template layers from the bottom that count as foundation |
| `blocks` | `["creativeengineering:earth_fill/earth"]` | block tags that count as earth |
| `overrides` | `[]` | per-structure limits: `"<id or #structure tag>=<water>,<air>"`, first match wins, e.g. `"philipsruins:ancient_ruins=0,16"` or `"minecraft:village_plains=-1,-1"` |
| `sampleGrid` | 2 | terrain read once per N x N cells (1 = every column) |
| `log` | `CANCELLED` | `CANCELLED` logs each cancelled start, `ALL` every start that has earth in its base (and counts it fully), `OFF` |

**Exempt structures:** the structure tag `creativeengineering:earth_fill/exempt` (`worldgen/earth_fill.js`), which lists
what floats or stands in the water on purpose: Cloud Storage, Sky Villages, the AE2 meteorite; every ship and raft;
ocean ruins and fortresses, wrecks, the monument, Hopo's fossils, the sushi stall, buried treasure. Add an id to exempt
it, or use an `overrides` line to give it its own numbers.

**Where the defaults come from.** `tools/earth_fill_sim.sh` generates real vanilla structures on vanilla overworld
terrain (the pack's own `EarthFill` counting, builtin registries, the real template and terrain code, 60 sites per
structure inside the structure's biomes, seed 4242) and prints how the counts are distributed and what share each limit
pair would cancel. Result, 2026-10-03:

| Structure | Earth in base (mean) | Over water p50 / max | Over air p50 / p90 / p99 | Cancelled at (24, 64) | at (48, 160) | at (96, 256) |
|---|---|---|---|---|---|---|
| village_plains | 239 | 0 / 0 | 58 / 120 / 200 | 45% | 5% | 0% |
| village_desert | 314 | 0 / 0 | 64 / 129 / 169 | 48% | 3% | 0% |
| village_savanna | 594 | 0 / 0 | 148 / 260 / 426 | 78% | 45% | 12% |
| village_snowy | 298 | 0 / 0 | 70 / 147 / 219 | 58% | 5% | 0% |
| village_taiga | 328 | 0 / 0 | 74 / 200 / 314 | 63% | 15% | 3% |
| pillager_outpost, igloo | 0 | none | none | 0% | 0% | 0% |
| mansion | 47 | 0 / 0 | 25 / 79 / 100 | 13% | 0% | 0% |

So a whole village hangs a median 60 to 150 earth blocks in air on ordinary terrain (its terrain adaptation fills
under it later), and none stands in water in its own biomes. 24 over water therefore only trips at shores, and 256 over
air only the extremes. **Small structures with a pad are a different case** (a 15 x 15 ruin has about 225 pad blocks in
its bottom layers; half over a cliff is 110), so tighten them with `overrides` once you have seen `log = ALL` numbers for
them. Modded structures were not simulated: the pack's registries need a game.

**Limits.**
- It judges the generator's noise terrain: no carvers, surface rules, features, lakes, or other structures' terrain
  adaptation (the Beardifier fill under villages is not estimated). Vanilla and modded jigsaw structures that rely on
  terrain adaptation therefore show their template earth as "in air" where the ground falls away, even though the
  generator will fill under them. That is why the air default is high.
- Only the template's own earth blocks are counted, not the terrain a structure's processors or beard add.
- `/place structure`, `/ceplace` and `/locate` don't go through `tryGenerateStructure`, so none of them is checked
  (a `/locate` can point at a slot the check later cancels: it loads the chunk and finds no start, as for a wrong biome).
- A cancelled slot stays empty, so structure rates drop a little on coasts and cliffs (the group grid is unchanged).
- Terrain-matching pieces and non-template pieces (legacy temples, mineshafts) count as no earth.
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

**Limits.** The same as earth fill (noise terrain only, no `/place`, `/ceplace` or `/locate`, an empty slot on a
cancel), plus:
- The seabed test reads the column's top non-air block, so a lake or river bed counts as seabed too, and a column whose
  surface the noise puts in an aquifer lake is "under water" even if a surface rule later makes it dry.
- At `sampleGrid` 2 a shoreline is resolved to 2 blocks; a ruin touching a beach may count a few blocks either way.
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

Tagged now (97 structures):
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
`/cegen run terrain_adaptation_scan`, about a minute fresh, seconds cached) follows every structure's start pool and
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

Batches 1 to 3 were tagged on 2026-10-09 (the user's choice); batch 4 is open. After tagging, the scan shows 97
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

## 4. Limits and decisions

- **Only structure_set documents are written, never the structure.** Frequency, `locate_offset` and on/off all live
  on the small set document (plus patcherMod's frequency table). Rewriting a structure (biomes, jigsaw pools, size)
  risks breaking jigsaw schemas, and vanilla structures have no json to start from. Where a structure spawns is changed
  through biome tags (`biome_merges.json`) instead.
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
- **Planned: meta structures** (a fort with towers, cloud clusters, a balloon with clouds): new structures of our own
  that join the groups through the same tags. Their json goes in `kubejs/data/creativeengineering/worldgen/structure/meta/`,
  the scanner lists them under `meta_structures`, the script accepts them as members with no original set. One
  structure start can only reach 8 chunks from its start chunk, so a fort with full villages around it needs separate
  satellite starts (see Claude's memory `structure-spacing-feasibility`).
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
- **`/ceplace` is not natural generation.** No frequency roll, no biome check, no terrain adaptation. Some structures turn
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
- patcherMod: [worldgen/StructureFrequencies.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/StructureFrequencies.java) + mixins [ChunkGeneratorStructureFrequencyMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ChunkGeneratorStructureFrequencyMixin.java), [StructureCheckFrequencyMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/StructureCheckFrequencyMixin.java); [worldgen/PoolTuner.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/PoolTuner.java), [LootContainerSwaps.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/LootContainerSwaps.java), [WorldgenFiles.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/WorldgenFiles.java) (report writer; KubeJS blocks java.io); mixins [StructureTemplateLootSwapMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/StructureTemplateLootSwapMixin.java), [KubeJSVirtualDataPackListMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/KubeJSVirtualDataPackListMixin.java), [ChunkGeneratorLocateMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ChunkGeneratorLocateMixin.java) + [DeadPlacementFilter.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/DeadPlacementFilter.java)
- Earth fill: [worldgen/EarthFill.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/EarthFill.java) (`count`, `pieceEarth`, `localEarth`, `pieceWater`, `localWater`, `predicted`, `topIsWater`, `limitsFor`, `waterLimitsFor`, `filter`) + mixin [ChunkGeneratorEarthFillMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ChunkGeneratorEarthFillMixin.java), knobs `CEConfig.EarthFill`, earth tag in [blocktags.js](../kubejs/server_scripts/tags/blocktags.js), exempt structures [worldgen/earth_fill.js](../kubejs/server_scripts/worldgen/earth_fill.js); water structures and ponds [worldgen/water_fill.js](../kubejs/server_scripts/worldgen/water_fill.js), knobs `CEConfig.WaterFill`; tests `EarthFillTest` and the calibration [tools/earth_fill_sim.sh](../tools/earth_fill_sim.sh) (`EarthFillSim`)
- Terrain adaptation candidates: [tools/terrain_adaptation_scan.py](../tools/terrain_adaptation_scan.py) → [terrain_adaptation_scan.md](terrain_adaptation_scan.md) + `kubejs/generation_scripts/worldgen/terrain_adaptation_scan.json` (cache `tools/.cache/terrain_scan.json`; `/cegen run terrain_adaptation_scan`)
- Terrain adaptation + template trim: [worldgen/TerrainAdaptation.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/TerrainAdaptation.java) (`Mode`, `rebuild`, `override`, `rigid`, `softBox`, `SoftBox`, `beard`, `field`, `addSoft`), [worldgen/TemplateTrim.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/TemplateTrim.java) (`kind`, `columns`, `trim`, `apply`), mixins [StructureTerrainAdaptationMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/StructureTerrainAdaptationMixin.java), [BeardifierSoftMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/BeardifierSoftMixin.java) and the trim call in [StructureTemplateGroundMatchMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/StructureTemplateGroundMatchMixin.java); knobs `CEConfig.TerrainAdaptation`; structure tags [worldgen/terrain_adaptation.js](../kubejs/server_scripts/worldgen/terrain_adaptation.js), block tags `template_trim/*` in [blocktags.js](../kubejs/server_scripts/tags/blocktags.js); test `TerrainAdaptationTest`
- Banks and driftwood: tags `monobank/vault_houses` / `monobank/vault_streets` in [pool_tags.json](../kubejs/generation_scripts/worldgen/pool_tags.json); [driftwood placed features](../kubejs/data/upgrade_aquatic/worldgen/placed_feature/) and [land driftwood](../kubejs/data/creativeengineering/worldgen/placed_feature/driftwood_land/) with their [biome modifiers](../kubejs/data/creativeengineering/forge/biome_modifier/)
- Dead explorer maps: [loot/06_dead_maps.js](../kubejs/server_scripts/loot/06_dead_maps.js)
- Worldgen config: `CEConfig.Worldgen` (+ `WorldgenSystems`, `WorldgenDebug`, `PoolTuning`, `BiomeSolver`, `GroundMatch`, `MassPlace`), read through [worldgen/WorldgenConfig.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/WorldgenConfig.java); density in `scalePlacement` in [structure_overrides.js](../kubejs/server_scripts/worldgen/structure_overrides.js)
- `/ceplace`: [worldgen/MassPlacer.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/MassPlacer.java) (`select`, `rowsFor`, `templateRows`, `signLines`, `verticalShift`, `placeStructure`, `placeTemplate`)
- Ground matching: [worldgen/GroundMatch.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/GroundMatch.java) (`sample`, `palette`, `rewrite`, `apply`), mixins [StructureTemplateGroundMatchMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/StructureTemplateGroundMatchMixin.java) + [StructureStartGroundMatchMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/StructureStartGroundMatchMixin.java), knobs `CEConfig.GroundMatch`, block tags at the end of [blocktags.js](../kubejs/server_scripts/tags/blocktags.js), exempt structures [worldgen/ground_match.js](../kubejs/server_scripts/worldgen/ground_match.js)
- Worldgen tool tests: [tools/test_worldgen_tools.sh](../tools/test_worldgen_tools.sh) ([tools/worldgen_test/](../tools/worldgen_test/): `GroundMatchTest`, `MassPlacerTest`; [tools/structure_density_check.js](../tools/structure_density_check.js)); the Java test classpath shared with `test_structure_groups.sh` is [tools/java_test_cp.sh](../tools/java_test_cp.sh)
- Dump commands: `/dumpstructuregen` [dump_structuregen.js](../kubejs/server_scripts/_lib/dump_structuregen.js) → `kubejs/exported/worldgen/structuregen.json`; `/dumpstructures` [structure_dump.js](../kubejs/server_scripts/_lib/structure_dump.js) (names only)
- Offline tools: [tools/structure_tuning_sim.js](../tools/structure_tuning_sim.js), [tools/test_structure_groups.sh](../tools/test_structure_groups.sh) (+ [tools/structure_groups_test/](../tools/structure_groups_test/)), [tools/check_structure_live.py](../tools/check_structure_live.py), [tools/audit_loot_containers.py](../tools/audit_loot_containers.py)
- Archive of the old model (config with `current`, tags with multipliers, old applier, old report): `kubejs/archive/worldgen/pre_groups_2026-09-27/`
- Converted structure mods: `kubejs/data/<modid>/`, made by [tools/port_structure_jars.py](../tools/port_structure_jars.py) (3.11)
- Old node generators (don't run, they would double-apply): `kubejs/archive/worldgen/`

## 6. Offline checks

- **`tools/test_structure_groups.sh`** (about 20 s): the node simulation; the same script in the pack's own Rhino, whose
  96 set files, frequency table and report must match node's byte for byte; the old-jar path (must apply nothing); and
  13 headless Java checks of `StructureFrequencies` (roll uniform and independent, a 0.25 member keeps 25% of its slots,
  the veto stops only the rest of its own set in its own chunk, `/locate`'s prediction matches the roll 2000/2000). All
  passed 2026-09-27. Run `./gradlew build` in `patcherMod` first.
- **Before rejoining after an edit:** `node tools/structure_tuning_sim.js [report.md] [out/]` prints the line the game will
  log (currently `9 groups, 96 structure_sets written (64 emptied into groups), 117 original sets; 109 structure
  frequencies below 1`), or the errors that would make it apply nothing.
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
- In game, `/cegen run worldgen` runs `gen_structure_config.py`, `build_biome_merges.py`, `gen_biome_config.py` and the
  merge analyzer; `/cegen run structure_live_check dumps` does `/dumpstructuregen` + `check_structure_live.py`.
- **Loot containers after mod changes:** `python3 tools/audit_loot_containers.py` rewrites `loot_container_audit.md`.
- **`tools/test_worldgen_tools.sh`** (about 30 s, after `./gradlew build`), all passed 2026-09-27 (earth fill added and passed 2026-10-03):
  1. 37 headless checks of `GroundMatch` against real block states and bound tags. They cover what counts as ground,
     the native/foreign decision (volcanic vs plains sample, the 35% threshold, too few columns), weighted picks, and a
     5x5 grass-pad ruin through all three modes: 50 base blocks swapped, poppy + tall grass dropped, exposed vs covered
     palettes, the roof garden and the next chunk left alone, plus `maxPlantHeight`.
  2. 30 checks of `/ceplace` and `WorldgenConfig`: sign wrapping, every filter term, rows on the real
     `structure_tags.json` (each Towns & Towers structure in exactly one row, always its smallest type tag), template
     folders, vertical alignment, and every config default equal to the old hard-coded value.
  3. `EarthFillTest` (32 checks since 2026-10-08): the counting core (limits -1 and 0, early stop, dedupe), the earth blocks of a real template
     through rotation, rigid / list / terrain-matching pool elements, a whole pool-element piece, override parsing; water fill:
     which blocks hold water (water, waterlogged, kelp, seagrass, all layers), a piece's water on a shore (sea / seabed / dry
     ground / air), the ground and air limits, seabed as solid for earth fill, and the column-top seabed test.
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
- `test_structure_groups.sh` step 2 also compares node and Rhino at density x4 and x0.3 and with groups off (identical).
  The sim takes `--density=X` and `--groups-off`.

## 7. Planned changes

Done 2026-10-03, all three (3.12, 3.13):
- Reduce bank frequency: Monobank vault pieces about 5x rarer in every village biome.
- Reduce driftwood on land: water driftwood keeps its 0.25x rate, land driftwood is about a tenth of that.
- Cancel structures that need too much earth over water or air: `worldgen/EarthFill`, limits in `[worldgen.earth_fill]`.

Done 2026-10-09 (3.15): terrain adaptation by structure tag, the soft beard, template trim.

Done 2026-10-09: the scan of every other structure for pads and saved air (3.15, `terrain_adaptation_scan.md`), and
its batches 1 to 3 tagged. Still open: batch 4 (the spire, the fish ponds, Philip's padded ruins, the villages: look in
game first); the modded-structure calibration of the earth limits (needs a game with `log = ALL`), and a Beardifier fill
estimate for terrain-adapted structures if the template count turns out to miss them.

## 8. In-game test suite

**Terrain adaptation and template trim** (2026-10-09, 3.15). Install the jar first (`./gradlew installToMods` in
`development/patcherMod` with the game closed), rejoin, and fly well away from explored land.
- [ ] **ST-61 Loads clean:** no mixin error for `StructureTerrainAdaptationMixin` or `BeardifierSoftMixin` (the game would not start); `latest.log` has `[terrain-adaptation] world load: 97 structures tagged, modes {NONE=6, SOFT=91}, 95 trimmed` and no `answers terrainAdaptation itself` or `... wins` warning; no KubeJS error for `worldgen/terrain_adaptation.js`; the toml has `[worldgen.terrain_adaptation] skirtBump = 0` and `systems.terrainAdaptation` / `systems.templateTrim`.
- [ ] **ST-62 Ballista:** `L illager_siege_weapons:ballista` (or another siege weapon) until one generates on a slope. No flat square around it and no notch in the hill; the ground under and around it is the natural slope; it stands on its own slabs (one end may hang a block or two over a steep slope: that is `none`).
- [ ] **ST-63 Soft house on a slope:** `L villager_butcher_house:butcher_house` (or the circus tent, a pillager tower) in hilly new terrain. The ground is level under the building and blends into the hill within about 4 blocks, following the walls with rounded corners; no square grass pad; the yard is natural ground.
- [ ] **ST-64 skirtBump:** set `skirtBump = 4`, rejoin, find the same kind of structure in new chunks: the blend is as wide as vanilla's (about 8 blocks) but still follows the building. Set it back to 0.
- [ ] **ST-65 Switches:** `[worldgen.systems] terrainAdaptation = false`, rejoin, new chunks: the tagged structures get their own beard again (the flat rectangle), with `templateTrim` still on (no pad, no air notch, but the rectangle is flattened). Then `templateTrim = false` too: pads and notches are back. Both back on.
- [ ] **ST-66 Watchtowers and plates:** `L watchtower_building:fresh_watchtower`: the tower stands complete on its base plate (the foundation piece is never trimmed), with the soft blend around it.
- [ ] **ST-67 Earth fill sees the trim:** with `[worldgen.earth_fill] log = "ALL"`, a trimmed structure's `[earth-fill]` line counts only earth under its built columns (the circus tent's base count drops by its 264 trimmed pad blocks). Put `log` back.
- [ ] **ST-68b Scan batches:** in new terrain, a Towns & Towers pillager outpost (`L towns_and_towers:pillager_outpost_grove`) and a vanilla one on a slope: level under the tower and pieces only, round 4-block blend, no grass slab or air notch beyond them; the outpost is otherwise complete (tower top, cages, tents). A hobbit house or botanist house: as ST-63. A Takes a Pillage bastille: its courtyards are still flat inside the walls, but outside the walls the ground blends back within about 4 blocks instead of a wide flat apron.
- [ ] **ST-68 Others unchanged:** a vanilla village, a YUNG's structure and an untagged modded house look as before (the same beard and pads).

**Water fill** (2026-10-08, 3.14)
- [ ] **ST-56 Loads clean:** the toml has `[worldgen.water_fill]` and `systems.waterFill`; no `[water-fill] overrides: ... skipped` warning; no KubeJS error for `worldgen/water_fill.js`; `/dumpstructuregen` still works.
- [ ] **ST-57 Log run (calibration):** set `[worldgen.water_fill] log = "ALL"`, rejoin, fly over 2,000+ blocks of new ocean and coast (or `[worldgen.debug] structureDensity = 4`). `latest.log` has `[water-fill] hopo:underwater_ruins at chunk [x, z]: N water blocks, G into dry ground (limit 32), A into air (limit 16) -> kept|CANCELLED` lines. Ruins in open ocean should show 0 or close to it; tell me the numbers and the limits get set from them. Put `log` back to `CANCELLED`.
- [ ] **ST-58 A cancel:** set `maxIntoGround = 0`, `maxIntoAir = 0` and `overrides = []`, rejoin, fly along a new coast: Hopo ruins near the shore log `CANCELLED` and are missing; the fish ponds and the swamp fisherman house cancel almost everywhere (their water is dug into the ground). Put the values back.
- [ ] **ST-59 Ponds:** with the default overrides, a fish pond on flat plains or a fisherman house in a swamp still generates (their ground count doesn't matter); one on a steep slope where the pond would spill logs `CANCELLED`.
- [ ] **ST-60 Switch and scope:** `[worldgen.systems] waterFill = false` → no `[water-fill]` lines. Back on: a structure not in `water_fill/structures` (a Better Jungle Temple, a village with farms) never logs a `[water-fill]` line, even at 0 and 0.

**Setup:**
- Creative world with cheats. Rejoin once after pulling these changes, then fly well away from explored land (or
  make a new world) so the chunks you check are new.
- `L <id>` = `/locate structure <id>`. A removed structure answers "Could not find a structure of type ... nearby".
- `Dump` = `/dumpstructuregen`, then run `python3 tools/check_structure_live.py` outside the game.
- Revert every temporary edit and rejoin before moving to the next test.

**Structure sets** (tests from before the groups; the retired ones used the old knobs)
- [ ] ~~**ST-01 Loads clean**~~ retired 2026-09-27, moved to ST-17.
- [ ] ~~**ST-02 Live values match**~~ retired 2026-09-27, moved to ST-18.
- [ ] **ST-03 Removal works:** `L minecraft:ruined_portal` → not found, answered within a second or two. In the Nether, `L minecraft:ruined_portal_nether` → not found.
- [ ] **ST-04 New houses load:** `L villager_tannery:tannery` and `L simply_houses:taiga_inn` → found. Teleport to the tannery → it has its villager, and urns stand where the original 1.20 build had decorated pots.
- [ ] **ST-05 AE2 meteorite in the sky group:** `L ae2:meteorite` → found, and a crater is there; meteorites are now as rare as Cloud Storage's structures (1 per 215 chunks).
- [ ] ~~**ST-06 Category knob**~~ retired 2026-09-27 (tags have no multipliers), moved to ST-21.
- [ ] ~~**ST-07 Per-set edit**~~ retired 2026-09-27, moved to ST-23.
- [ ] ~~**ST-08 One member off**~~ retired 2026-09-27, moved to ST-21.
- [ ] ~~**ST-09 An error applies nothing**~~ retired 2026-09-27, moved to ST-24.
- [ ] **ST-10 Blocked means blocked:** `L betterstrongholds:stronghold` → found; throw an eye of ender → it heads toward a YUNG's stronghold. The report lists `minecraft:stronghold` as blocked under Coverage.

**Structure groups** (2026-09-27)
- [ ] **ST-17 Loads clean:** rejoin, `logs/latest.log` has `[structure-tuning] 9 groups, 96 structure_sets written (64 emptied into groups), 117 original sets; 109 structure frequencies below 1` and `[structure-frequency] 109 structure(s) in 11 set(s) roll a frequency below 1`, no error line. `structure_tag_report.md` has no Warnings section and shows all 9 groups. *2026-09-27: both lines exactly so on a scratch dedicated-server boot of this instance (new world); your client rejoin still to do.*
- [x] **ST-18 Live sets match:** `Dump` → sections 1 and 2 empty, section 4 `0 mismatch(es)` over 96 sets. PASSED 2026-09-27 on the scratch dedicated server (`/dumpstructuregen` from its console, `check_structure_live.py`: 0 / 0 / 0 mismatches over 96 sets, with the hand-set group grids).
- [ ] **ST-19 Every group spawns:** one `L` per group → found: `minecraft:village_plains`, `towns_and_towers:pillager_outpost_forest`, `philipsruins:level_one_ruins`, `betterjungletemples:jungle_temple`, `hopo:underwater_ruins`, `minecraft:monument`, `cloudstorage:big_balloon`, `towns_and_towers:village_ocean`, `simply_houses:taiga_inn`. Teleport to two of them → the structure is really there (the `/locate` prediction and the frequency roll agree).
- [ ] **ST-20 The gap holds:** in fresh terrain, `L #minecraft:village` from 6 spots a few thousand blocks apart and teleport to each: note the chunk of each settlement's centre (F3). No two settlements of any mod (vanilla, T&T, fortified) start closer than 13 chunks. Same check for 3 ruins (5 chunks) and 3 surface dungeons (11 chunks).
- [ ] **ST-21 One member off, others unchanged:** in `structure_groups.json` set `groups.sky.structures.cloudstorage:sky_temple.enabled` to false, rejoin → the report shows it disabled and 1 of 2 members live; `L cloudstorage:sky_temple` → not found; `L cloudstorage:big_balloon` → found. Revert.
- [ ] **ST-22 A low frequency is rare, not gone:** `L minecraft:mansion` (surface_dungeons, frequency 0.12) → found, and a mansion stands there. `L minecraft:monument` (frequency 0.1) → found, and a monument stands there.
- [ ] **ST-23 Group spacing edit:** set `groups.boats.placement.spacing` to 60 (separation 21 stays), rejoin, `Dump` → `creativeengineering:group/boats` spacing 60 in `structuregen.json`, `check_structure_live.py` 0 mismatches. Put 41 back.
- [ ] **ST-24 An error applies nothing:** add `"frequncy": 0.5` to any member, rejoin → the log shows `[structure-tuning] ... unknown field "frequncy"` and "nothing applied", no `[structure-frequency]` line; the report lists the error; `L minecraft:ruined_portal` finds one again (mod defaults). Remove the typo and rejoin.
- [ ] **ST-25 Moving a structure:** move `terralith:glacial_hut` from `group/ruins` to `group/remote` in `structure_tags.json` (add `!terralith:glacial_hut` to ruins, `terralith:glacial_hut` to remote), run `python3 tools/gen_structure_groups.py` → it prints "removed ruins: terralith:glacial_hut" and a fitted remote entry; `--check` shows it at 1 per 22 like before; rejoin → loads clean, `L terralith:glacial_hut` → found. Revert both files (git) and rejoin.
- [ ] **ST-26 Way signs still avoid settlements:** `L supplementaries:way_sign` a few times → found, none inside a village (their exclusion zone now points at `creativeengineering:group/settlements`, 8 chunks).

**Village houses and containers**
- [x] **ST-11 Pool tuner runs:** `pool_tag_report.md` is rewritten at world load → "645 pools, 3669 pieces", "12 pool(s) retuned", no errors section. PASSED 2026-09-27 (report from the 12:00 world load).
- [ ] **ST-12 Tech houses gone:** `L minecraft:village_plains`, teleport, and walk 3 fresh villages → no Create: Dynamic Village, IE, PneumaticCraft or Advanced Peripherals houses. Pet shops, warehouses and chef houses still show up.
- [ ] **ST-13 Piece edit:** in `pool_config.json` → `minecraft:village/plains/houses` → `domesticationinnovation:plains_petshop` set `"enabled": false`, rejoin → `pool_tag_report.md` shows it **removed** in that pool. Revert.
- [ ] **ST-14 Container swap:** `/place template chefsdelight:plains_chef_house ~3 ~ ~3` → vanilla barrels stand where its oak cabinets and baskets were. Then find a chef house in a freshly generated village → those barrels are Lootr barrels (per-player loot).

**Structure searches**
- [ ] **ST-15 No hang on a removed set:** `/locate structure #minecraft:ruined_portal` → "Could not find" within a second or two (the emptied set has no members, so there's nothing to search; `[locate] skipping frequency-0 structure placements` only appears if some other set sits at frequency 0). Then `/loot give @s loot minecraft:chests/shipwreck_map` → a real buried-treasure map (live sets still searched).
- [ ] **ST-16 Monobank vaults drop no map:** `/reload`, log shows `[dead-maps] removed 5 explorer-map entries from 5 tables`. `/loot give @s loot monobank:monobank/village/plains` 30 times → emeralds/gold only, never a map. Then walk into a fresh plains village with a vault and open it → no freeze.

**Ground matching** (set `[worldgen.ground_match] log = true` first, in the toml or Create's config screen)
- [ ] **ST-27 Loads clean, grass stays on grass:** start the game → no mixin error for `StructureTemplateGroundMatchMixin` / `StructureStartGroundMatchMixin`; the toml has a `[worldgen.ground_match]` section. In a plains, `/place structure philipsruins:ancient_ruins` → the grass/dirt pad is still there, and no `[ground-match]` line is logged.
- [ ] **ST-28 Volcanic peaks:** `/locate biome terralith:volcanic_peaks`, teleport, `/place structure philipsruins:ancient_ruins`, then `/place structure philipsruins:ancient_towers` a bit away → no grass or dirt under either; the pad is the local basalt/blackstone mix. Flowers and grass tufts on the pad are gone. The log shows `[ground-match] ... creativeengineering:ground_match/grass N% of M ground columns` with N below 35.
- [ ] **ST-29 Natural generation:** with `log = true`, fly into fresh terrain across a desert or badlands and a mountain range → `[ground-match]` lines appear while chunks generate. Teleport to the chunk one names (`chunk [x, z]` × 16) → the structure there sits on the local ground.
- [ ] **ST-30 Modes:** set `mode = "PALETTE"`, `/place structure philipsruins:ancient_ruins` on volcanic peaks → a full pad made of the sampled blocks. `mode = "REMOVE"` → no pad at all; the walls stand on bare terrain. Put `TERRAIN` back.
- [ ] **ST-31 Exempt tag:** add `'philipsruins:ancient_ruins'` to `worldgen/ground_match.js`, `/reload`, and place it on volcanic peaks → the grass pad is back. Remove the entry and `/reload`.
- [ ] **ST-32 Plain templates untouched:** on volcanic peaks, `/place template philipsruins:ancient_ruin_1 ~2 ~ ~2` → grass pad as saved (only whole structures are matched), no `[ground-match]` line.

**Worldgen config** (edit `config/creativeengineering-common.toml` or Create's config screen; put every value back after each test)
- [ ] **ST-33 The section is there:** start the game → the toml has `[worldgen]` with `systems`, `debug`, `pools`, `biome_solver`, `ground_match` (+ `sampling`) and `mass_place`; Create's config screen shows them. A normal rejoin logs the usual `[structure-tuning] 9 groups, 97 structure_sets written ...` line (no DEBUG, no GROUPS OFF).
- [ ] **ST-34 Density up:** `structureDensity = 4`, rejoin → a WARN `DEBUG structure density x4 ... all exact` and `[structure-tuning] DEBUG density x4, 9 groups, 126 structure_sets written`. `structure_tag_report.md` shows every grid at about half its spacing. Fly into new terrain → clearly more structures of every kind.
- [ ] **ST-35 Density down:** `structureDensity = 0.25`, rejoin → same lines with x0.25; `L minecraft:village_plains` from a few spots → villages are much further apart.
- [ ] **ST-36 Groups off:** `systems.structureGroups = false`, rejoin → WARN `structure groups are OFF` and `GROUPS OFF, 0 groups, 0 structure_sets written`; `L philipsruins:ancient_ruins` still finds one (its mod's own set).
- [ ] **ST-37 Frequencies off:** `systems.structureFrequencies = false`, rejoin → `[structure-frequency] off in CEConfig [worldgen.systems]` instead of the `109 structure(s) ... roll a frequency below 1` line.
- [ ] **ST-38 Pool tuning off:** `systems.poolTuning = false`, rejoin → `pool_tag_report.md` lists the warning "pool tuning is off"; a fresh plains village can have Create: Dynamic Village houses again.

**`/ceplace`** (superflat world, Generate Structures OFF, cheats on)
- [ ] **ST-39 Preview:** `/ceplace preview structures @towns_and_towers` → `56 structures in 7 row(s)`: type/camp 5, type/fort 9, type/ship 2, type/shipwreck 1, type/tower 15, type/village 23, (no type/* tag) 1. `/ceplace preview templates kaisyn:village/*/houses/*` → one row per village style. *2026-09-27: the structures preview gave exactly this from a scratch dedicated server's console; templates preview and your client still to do.*
- [ ] **ST-40 Whole mod in rows:** `/ceplace structures @towns_and_towers` → progress in chat, then `/ceplace done: N of 56 placed in x ..`; a sign in front of each row and each structure; no two structures overlap; failures (if any) listed with a reason. Walk the rows: villages complete, outposts with their pillagers. *2026-09-27, scratch dedicated server on normal terrain: the first version was killed by the 60 s watchdog at 38/56 (a 90-chunk village generated in one tick); after the step/tick-budget rewrite, `done: 55 of 56 placed`, `village_ocean` refused dry land. Flat world and walking the rows still to do.*
- [ ] **ST-41 Templates:** `/ceplace templates kaisyn:village/meadow_swiss/houses/*` (or any folder from ST-39's list) → houses in a row, no jigsaw blocks left, signs with each template id.
- [ ] **ST-42 Status and stop:** start `/ceplace structures by group @philipsruins @hopo`, run `/ceplace status` (progress line naming the structure in progress and its chunks loaded/placed), then `/ceplace stop` → `stopped at n/N`, nothing more is placed.
- [ ] **ST-43 Converted structure mods in the client:** the game starts with no `NoSuchMethodException ... <init>()`. In a flat world, `/ceplace structures @mysticrift_pillagers_towers_aris @skyvillages @saloon @villager_forge @illagerwarship` → all placed. Look for supplementaries urns where the 1.20 decorated pots were, Vinery cherry wood on the cherry tower, and Sky Villages with no empty gaps. *2026-09-27: all 56 placed on a scratch dedicated server (0 failed); client not yet.*

**Water biomes, IPO, driftwood** (2026-10-03)
- [ ] **ST-44 Rule applies:** latest.log has `[structure-biome-rules] water: 12 biomes, ... kept out:` listing the 8 `mysticrift_pillagers_towers_aris:*` towers; `structure_tag_report.md` has a "Biome rules" section. *2026-10-03: both on a scratch dedicated server (25 kept out, 31 allowed that can start in water).*
- [ ] **ST-45 No towers at sea:** fly over 2,000+ blocks of new ocean → no Mysticrift tower stands in the water. Ships, rafts, wrecks and ocean ruins still show up. `L mysticrift_pillagers_towers_aris:oak_wood_pillager_tower` from far out at sea → the answer is on land.
- [ ] **ST-46 Switch:** set `[worldgen.systems] structureBiomeRules = false`, rejoin → the log says the rules are off; set it back.
- [ ] **ST-47 IPO mix:** `/place structure minecraft:pillager_outpost` about 16 times in a flat area → mostly vanilla watchtowers, about 2 tall IPO towers. No outpost is a bare plate without a tower, and no IPO tower is missing its top. *2026-10-03: pool weights 7:1 and the renamed IPO pieces confirmed in the scratch server's pool_config.json; not placed yet.*
- [ ] **ST-48 Driftwood:** new beach and ocean shoreline → driftwood still appears, but clearly sparser than before. *2026-10-03: the scratch server created a world with the 5 overrides, so the JSON parses.*

**Banks, driftwood, earth fill** (2026-10-03)
- [ ] **ST-49 Banks:** rejoin; `pool_tag_report.md` shows `monobank:village/houses/plains_vault` at 2.7 and `monobank:village/streets/plains_straight_fix_01` at 2.4 (final chance about 2.0% and 4.6%), `monobank/vault_houses x0.18` and `monobank/vault_streets x0.16` listed, no errors. Then `L minecraft:village_plains` 6 times and walk each: banks (a vault building with its door, or a straight street with a vault beside it) should be uncommon, roughly 1 village in 4 or 5 instead of most of them.
- [ ] **ST-50 Driftwood:** in new terrain, walk a beach, a swamp, a river and open ocean: driftwood floats in the shallows and on the sea, a log lying on dry sand or grass is now a rare find (beach about 1 per 160 chunks). A river bank has none. `latest.log` has no `upgrade_aquatic:driftwood_*` or `creativeengineering:driftwood_land/*` parse error.
- [ ] **ST-51 Loads clean:** the log has no mixin error for `ChunkGeneratorEarthFillMixin`; the toml has `[worldgen.earth_fill]` and `systems.earthFill`; `/reload` and `/dumpstructuregen` still work.
- [ ] **ST-52 Log run (calibration):** set `[worldgen.earth_fill] log = "ALL"`, rejoin, fly over 2,000+ blocks of fresh terrain (or `[worldgen.debug] structureDensity = 4`). `latest.log` has `[earth-fill] <id> at chunk [x, z]: N earth base blocks, W over water (limit 24), A over air (limit 256) -> kept|CANCELLED` lines. Collect them, then set the limits and any `overrides` from what you see. Put `log` back to `CANCELLED`.
- [ ] **ST-53 A cancel:** set `maxOverAir = 0` and `maxOverWater = 0`, rejoin, fly into new terrain: structures with any earth in their base now log `CANCELLED` and are missing on slopes and shores, and the slot stays empty. Put the limits back.
- [ ] **ST-54 Overrides and exempt:** set `overrides = ["minecraft:village_plains=-1,-1", "philipsruins:ancient_ruins=0,0"]`: plains villages never log a cancel, ruins cancel at any earth over water or air. A structure in `earth_fill/exempt` (a ship, a sky balloon) never logs, even at 0 and 0.
- [ ] **ST-55 Switch:** `[worldgen.systems] earthFill = false` → no `[earth-fill]` lines, nothing cancelled. A world with the switch on at the default limits still has villages (the simulated cancel rate at 24 / 256 is 0 to 12%).

## 9. History

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
- **2026-09-27:** `/cegen` in-game generator runner (patcherMod, `tools/generators.json`), BUILT, not in mods/ yet.
  Its scratch run picked up the new HopoBetterUnderwaterRuins set (`hopo:underwater_fossils`) and 2 undecided
  SimplyHouses biome-merge proposals (forest, taiga).
  - Monobank watchdog crash (15:32, dedicated server): a vault's ruined-portal explorer map searched a frequency-0 set
    for 60 s. Built `ChunkGeneratorLocateMixin` (searches skip frequency-0 placements) and `loot/06_dead_maps.js` (strips
    the map from the 5 vault tables). Jar built, not in `mods/` yet. ST-03 now depends on the jar; ST-15 and ST-16 added.
- **2026-09-27 (evening): structure groups** replace the per-set + multiplier model, on the user's request after a
  feasibility round (edge-distance rules with a predictor rejected: they needed higher base frequencies; meta structures
  pinned for later).
  - 9 groups as `group/*` tags, one shared grid each; per-member weight + frequency; standalone sets for the rest. The
    frequency (a missed roll empties the slot) is new patcherMod code: `StructureFrequencies` + 2 mixins, jar deployed
    (old jar kept in `development/patcherMod/build/rollback/`).
  - `tools/gen_structure_groups.py` migrated the old tuning with a per-biome fit (biome pools from every jar's tags +
    `biome_merges.json`, areas from the climate sample): every structure within x0.89 to x1.13. The old files are in
    `kubejs/archive/worldgen/pre_groups_2026-09-27/`. Tag multipliers and `current`/`enabled` are gone; the scanner now
    writes a read-only baseline.
  - The baseline had missed HopoBetterUnderwaterRuins (added 15:15): 4 new sets, and its `minecraft:shipwrecks` override
    (spacing 12, beaches and rivers). Hopo's 3 ruins joined `ocean_ruins` at their live rate; its fossils are standalone.
  - Four sets that excluded `minecraft:villages` (way signs, 3 Farmer's Structures houses) now exclude the settlements
    group with the radius scaled to keep the same share of slots excluded.
  - `tools/test_structure_groups.sh` (node + Rhino parity + old-jar refusal + 13 Java checks) passes;
    `check_structure_live.py` now takes its expectations from the simulation. ST-01, 02, 06 to 09 retired; ST-17 to ST-26 added.
  - User edits the same evening: `surface_dungeons` separation 7 → 10 (176-block gap); AE2 meteorites moved into `sky` at
    the clouds' rate (1 per 32 → 1 per 215 chunks; the old hand-set weight 0.2 ignored), sky refitted (spacing 123) and
    separation 15 (256-block gap). New `gen_structure_groups.py --refit GROUP`.
- **2026-09-27 (night): ground matching.** The user asked which volcanic-peaks structures use a grass/dirt base, to
  remove them there. A template scan found Philip's `ancient_ruins` and `ancient_towers`. The user then asked for a
  system instead: swap the base for the natural ground wherever it doesn't match. Built `worldgen/GroundMatch` + 2
  mixins, CEConfig `[ground_match]`, and 3 block tags plus an exempt structure tag in KubeJS. `/place structure`
  counts too, which makes testing easy. The 36-check headless test passes; the jar was built while the game was
  running, so it is not in `mods/` yet (old jar saved as `development/patcherMod/build/rollback/creativeengineering-1.0.1.pre-groundmatch.e58b6e57.jar`).
  The Java test classpath moved to `tools/java_test_cp.sh`, shared with `test_structure_groups.sh` (still 13/13).
  ST-27 to ST-32 added.
- **2026-09-27 (late night): CEConfig `[worldgen]`, debug density, `/ceplace`.** The user asked, for testing: as many
  worldgen parameters in the config as possible, a debug multiplier on every structure's frequency, and a way to
  generate structures en masse (all of Towns & Towers in rows by type, in a flat world).
  - `[worldgen]`: a switch per system (8), the debug density, the pool scale limit, the biome solver's constants, and
    ground matching moved in (`[ground_match]` → `[worldgen.ground_match]`, its on/off under `systems`, sampling knobs
    added). Every consumer reads it through the new `WorldgenConfig`, whose defaults are the old constants.
  - The density multiplier lives in the applier (`scalePlacement`): exact below 1 (frequency), above 1 via
    spacing/separation plus frequency, capped sets reported.
  - `/ceplace` (patcherMod `MassPlacer`): structures or templates in labelled rows, grouped by type/group/mod/folder.
  - Tests: `tools/test_worldgen_tools.sh` (37 + 30 Java checks + 244 density checks) replaces `test_ground_match.sh`.
    Rhino parity is extended to density and groups off. The sim got `--density` / `--groups-off` (and a fix: with
    nothing to write it crashed on the missing output folder). All pass.
  - Jar deployed (md5 c9740038); ground matching is in it too. The CreEngServer copy is not updated. ST-33 to ST-42 added.
- **2026-09-27 (night): The Missing Villages removed, structures-by-biome table, group edits confirmed live.**
  - The user found the mod's style clashing and asked for it gone for good: jar out of `mods/` (and `server_mods/`),
    its `instance.json` entry, `mod_sides.md` row, loot alias, tags (`mod/themissingvillages`, its line in `type/village`)
    and notes removed; `gen_structure_config.py` dropped its `minecraft:village` set (117 sets), biome merges lost its 5
    tags (45 left), the group sync removed its 15 settlements members, loot config regenerated (0 references). The pool
    tuner drops its pools at the next load (none were edited).
  - New report [structures_by_biome.md](structures_by_biome.md) (`tools/gen_structure_biome_table.py`, in `/cegen`
    reports): kinds per biome with rates, holes, per-kind lists, and professions per village style.
  - The user's hand-set grids for all 9 groups (written 21:56) were confirmed on a scratch dedicated-server boot of the
    instance: `[structure-tuning] 9 groups, 96 structure_sets written ... 109 structure frequencies below 1`, and
    `check_structure_live.py` 0 mismatches over 96 live sets (ST-18 passed). The client applies them on its next rejoin.
  - The same boot killed `/ceplace` via the 60 s watchdog (one 90-chunk village per tick). Rewritten as steps within a
    per-tick time budget (`[worldgen.mass_place] tickBudgetMs`, replaces `perTick`); the rerun placed 55 of 56. Jar
    redeployed (md5 729953e6). Found on the way: Make Bubbles Pop is client-only (row added to `mod_sides.md`), and
    MoreJS villager trades sometimes log "Sell item cannot be empty" (also in an earlier client log; not investigated).
- **2026-09-27 (late night): structure mods converted to data** (3.11). 33 new data-only structure jars (20 of them
  stopped the game at mod construction) moved into `kubejs/data` by the new `tools/port_structure_jars.py`, tagged and
  grouped. A scratch dedicated server booted cleanly; `check_structure_live.py` found 0 mismatches over 150 written sets,
  and `/ceplace` placed all 56 converted structures (0 failed). Revamped Shipwrecks' `minecraft:shipwrecks` override wins
  live. ST-43 added.
- **2026-10-02: refresh after the 1.0.4 mods.** Every worldgen generator re-run on the `/dumpall` exports.
  `gen_structure_config.py` found no change (179 sets): the only new structure mod, Improved Pillager Outpost
  (`ipo`, data only), replaces vanilla's `pillager_outpost` templates and its `features` pool and adds 6 `ipo:` pools,
  so the outpost set, its group membership and rate are unchanged. No `pool_tags.json` rule touches outpost pools;
  PoolTuner already lists the 6 `ipo:` pools at mod weights. `gen_structure_groups.py`: "already covers every
  structure". `/dumpclimate` re-sampled, so `biome_config.json` stats and `structures_by_biome.md` were regenerated.
  `build_biome_merges.py` still reports proposals 2 (beach -> Revamped Shipwrecks), 10 and 24 (forest/taiga ->
  Simply Houses) and 23 (swamp -> Villages & Pillages) as undecided; they predate this refresh and wait on you.
  Container audit: 3,874 containers; IPO, Hopo, Revamped/Unwrecked Ships, Blocky Siege and Villages & Pillages
  templates are now covered.
- **2026-10-03: water biomes, IPO, driftwood** (3.12). The user asked for: IPO towers as a small chance and not a
  replacement, only water structures in water biomes (Mysticrift towers were standing in the ocean), and driftwood
  at 0.25x. Built:
  - biome rules: `biome_rules` + `water_ok`, patcherMod `StructureBiomeRules` + mixin, a CEConfig switch, and the
    Python model and node/Rhino harnesses taught about the rule
  - IPO restored to a 1-in-8 pick, with the template-name bug fixed
  - driftwood overrides
  
  `tools/test_structure_groups.sh` passes (19 Java checks, node/Rhino parity including the staged rules). A scratch
  dedicated server booted with the new jar, applied the rule and loaded the pools at 7:1. patcherMod stays 1.0.4
  (rebuilt before release, installed to `mods/`, md5 ab58494b).
- **2026-10-03 (evening): banks, land driftwood, earth fill** (3.12, 3.13). The user asked for the three planned changes:
  - Monobank vaults 5x rarer in villages (two pool tags, weights computed from the pool report).
  - Driftwood outside water cut hard: the 5 water features are gated to water, 3 new land features at a tenth of the rate.
  - Earth fill: patcherMod `EarthFill` + `ChunkGeneratorEarthFillMixin`, `[worldgen.earth_fill]` (water limit, air limit, base layers,
    block tags, per-structure overrides, grid, log), earth tag in `blocktags.js`, exempt tag in `worldgen/earth_fill.js`.
    `EarthFillTest` (21 checks) joined `tools/test_worldgen_tools.sh`; `tools/earth_fill_sim.sh` simulates vanilla villages,
    outposts, igloos and mansions on vanilla terrain and gave the default limits (24 over water, 256 over air).
  - Not done: no game or scratch server was started (the game was running, 10 GB resident and swapping), so the
    mixin application, the KubeJS data and the pool report are checked offline only (ST-49 to ST-55). patcherMod stays 1.0.4,
    built in `development/patcherMod/build/libs/`, not installed to `mods/`.
- **2026-10-08: water fill** (3.14). The user asked for the earth-fill cancel to extend to water structures, with a cap
  on how many blocks or how much air their water replaces. Same hook and terrain as earth fill; the predicted terrain
  now tells a seabed (solid under a water column) from dry ground. Opt-in structure tag `water_fill/structures` (11
  structures, from a scan of every template's water) plus a `water_fill/ponds` tag whose default override only limits
  air. `EarthFillTest` 21 -> 32 checks, all pass. Not done: no game or scratch server (the game was running), defaults
  not calibrated (ST-57). The jar is in `development/patcherMod/build/libs/` (still 1.0.5), not installed to `mods/`.
- **2026-10-09: terrain adaptation by tag, the soft beard, template trim** (3.15). The user reported square flat pads
  around small structures (the Illager Siege Weapons ballista). The cause: vanilla's beard flattens the whole template
  box plus 8 blocks, and MCreator templates save their pads and air. The user picked the tag-driven system: mode tags
  (none / bury / beard_thin / beard_box / soft) via a `Structure#terrainAdaptation` mixin; a soft beard over the built
  footprint with a 4-block skirt and a configurable `skirtBump` (default 0); `template_trim` dropping air, pad and
  plants outside the built columns. YUNG's API replaces the Beardifier from its rigid list, so the soft pieces ride
  inside that list (a `BoundingBox` subclass with adjustment NONE) and the density is added inside vanilla's
  `compute`. The 6 siege weapons are in `none`, the other 53 ported terrain-adapted structures in `soft`, all 59
  trimmed. Earth fill now ignores pad that trim drops. `TerrainAdaptationTest` (50 checks) passes; the other worldgen
  tests are unchanged. Not done: no game or scratch server (the game was running, no free memory); the jar is in
  `development/patcherMod/build/libs/` (1.0.5), not installed to `mods/`. ST-61 to ST-68.
- **2026-10-09, later: terrain adaptation scan** (3.15). The user asked for a comprehensive scan of the other
  structures the tags could affect. `tools/terrain_adaptation_scan.py` measures every reachable rigid template of all
  383 structures (vanilla jigsaw ones via `pool_config.json`; ported templates in `kubejs/data` included) and sorts
  them: 29 strong and 17 mild candidates, 31 settlements to review, the rest compact, afloat, not on the surface,
  terrain-matching or code-built. Classification fixes on the way: village farm water no longer marks a village as a
  water structure (water must be 20% of a template's blocks), the earth_fill/exempt list marks ships, End detection
  matches `end` as a word part. Nothing tagged yet: four suggested batches in 3.15. Registered in `tools/generators.json`.
- **2026-10-09, later: scan batches 1 to 3 tagged** (3.15). User: "tag 1-3". `worldgen/terrain_adaptation.js` is now
  built from named groups (siegeWeapons, portedMods, padHouses, outposts, walledCompounds) so a group can be soft
  without trim: 12 houses on pads and 24 pillager outposts (vanilla + 23 T&T) went to `soft` + `template_trim`, the
  bastille and the pillager camp to `soft` alone. 97 tagged (`none` 6, `soft` 91, trimmed 95), checked by evaluating the
  script under node and by the scan. Batch 4 still open. ST-61 expects the new load line; new check ST-68b.
