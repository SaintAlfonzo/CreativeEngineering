# Structure tuning: how often every structure and village house generates

Test prefix **ST-** · Last updated **2026-09-27** · Overall status **DEPLOYED, in-game test pending** (structure groups
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
| Jigsaw pool tuning (`pool_config.json` + `pool_tags.json`) | patcherMod `worldgen/PoolTuner` | Runs in game: report rewritten 2026-09-27 12:00, 645 pools / 3669 pieces, 12 pools retuned. Villages themselves not checked yet |
| Lootr container swaps (`loot_container_swaps.json`) | patcherMod `LootContainerSwaps` + mixin | DEPLOYED; untested in game |
| Searches skip removed sets (explorer maps, `/locate`) | patcherMod `ChunkGeneratorLocateMixin` + KubeJS `loot/06_dead_maps.js` | DEPLOYED (in the 16:51 jar). Emptied sets now also have no members, so searches never see them (ST-15, ST-16) |
| Ground matching (grass/dirt bases on foreign ground) | patcherMod `worldgen/GroundMatch` + 2 mixins, CEConfig `[worldgen.ground_match]`, tags in `blocktags.js` | DEPLOYED 2026-09-27 (jar md5 729953e6), 37 headless checks pass (`tools/test_worldgen_tools.sh`). ST-27 to ST-32 |
| Worldgen config: a switch per system, debug structure density, solver constants | CEConfig `[worldgen]` via `worldgen/WorldgenConfig`; read by the KubeJS applier and every patcherMod worldgen class | DEPLOYED 2026-09-27. Density exact for every live set (244 node checks), node and Rhino identical at x4, x0.3 and groups off. ST-33 to ST-38 |
| `/ceplace`: every structure or template of a filter in rows (flat-world testing) | patcherMod `worldgen/MassPlacer` | DEPLOYED 2026-09-27; filter/row/sign/alignment logic tested headless (30 checks); a real run on a scratch dedicated server placed 55 of 56 T&T structures after the watchdog fix. ST-39 to ST-42 |
| Data-only structure mods converted to `kubejs/data` (33 jars, 56 structures) | `tools/port_structure_jars.py` | Done 2026-09-27; scratch server boots, 56 of 56 placed by `/ceplace`, live sets match. Client untested. ST-43 |

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
    biomeLayout, biomeMerges, deadPlacementFilter, groundMatch).
  - `[worldgen.debug] structureDensity`: every structure's rate times x (testing only).
  - `[worldgen.pools]`, `[worldgen.biome_solver]`: the solvers' constants.
  - `[worldgen.ground_match]` (+ `.sampling`), `[worldgen.mass_place]` (`/ceplace` layout).
- Ground matching: `[worldgen.ground_match]` (mode, baseLayers, nativeShare, minSamples, families, log, sampling); which
  blocks are base / plants / not-ground: the `creativeengineering:ground_match/*` block tags in `blocktags.js`; opt a
  structure out: `creativeengineering:ground_match/exempt` in `worldgen/ground_match.js`.
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
which every template placement goes through. `loot_container_audit.md` (re-run 2026-09-27: 2320 loot containers)
leaves only Nullscape's phantom-hive droppers and Monobank's bank blocks unconverted, on purpose.

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
- patcherMod: [worldgen/StructureFrequencies.java](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/StructureFrequencies.java) + mixins [ChunkGeneratorStructureFrequencyMixin](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ChunkGeneratorStructureFrequencyMixin.java), [StructureCheckFrequencyMixin](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/StructureCheckFrequencyMixin.java); [worldgen/PoolTuner.java](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/PoolTuner.java), [LootContainerSwaps.java](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/LootContainerSwaps.java), [WorldgenFiles.java](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/WorldgenFiles.java) (report writer; KubeJS blocks java.io); mixins [StructureTemplateLootSwapMixin](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/StructureTemplateLootSwapMixin.java), [KubeJSVirtualDataPackListMixin](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/KubeJSVirtualDataPackListMixin.java), [ChunkGeneratorLocateMixin](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ChunkGeneratorLocateMixin.java) + [DeadPlacementFilter.java](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/DeadPlacementFilter.java)
- Dead explorer maps: [loot/06_dead_maps.js](../kubejs/server_scripts/loot/06_dead_maps.js)
- Worldgen config: `CEConfig.Worldgen` (+ `WorldgenSystems`, `WorldgenDebug`, `PoolTuning`, `BiomeSolver`, `GroundMatch`, `MassPlace`), read through [worldgen/WorldgenConfig.java](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/WorldgenConfig.java); density in `scalePlacement` in [structure_overrides.js](../kubejs/server_scripts/worldgen/structure_overrides.js)
- `/ceplace`: [worldgen/MassPlacer.java](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/MassPlacer.java) (`select`, `rowsFor`, `templateRows`, `signLines`, `verticalShift`, `placeStructure`, `placeTemplate`)
- Ground matching: [worldgen/GroundMatch.java](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/GroundMatch.java) (`sample`, `palette`, `rewrite`, `apply`), mixins [StructureTemplateGroundMatchMixin](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/StructureTemplateGroundMatchMixin.java) + [StructureStartGroundMatchMixin](../patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/StructureStartGroundMatchMixin.java), knobs `CEConfig.GroundMatch`, block tags at the end of [blocktags.js](../kubejs/server_scripts/tags/blocktags.js), exempt structures [worldgen/ground_match.js](../kubejs/server_scripts/worldgen/ground_match.js)
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
- **`tools/test_worldgen_tools.sh`** (about 30 s, after `./gradlew build`), all passed 2026-09-27:
  1. 37 headless checks of `GroundMatch` against real block states and bound tags. They cover what counts as ground,
     the native/foreign decision (volcanic vs plains sample, the 35% threshold, too few columns), weighted picks, and a
     5x5 grass-pad ruin through all three modes: 50 base blocks swapped, poppy + tall grass dropped, exposed vs covered
     palettes, the roof garden and the next chunk left alone, plus `maxPlantHeight`.
  2. 30 checks of `/ceplace` and `WorldgenConfig`: sign wrapping, every filter term, rows on the real
     `structure_tags.json` (each Towns & Towers structure in exactly one row, always its smallest type tag), template
     folders, vertical alignment, and every config default equal to the old hard-coded value.
  3. `node tools/structure_density_check.js`: runs the simulation at x4, x0.25, x30 and x0.01 and checks every live set's
     rate against x1: 244 checks, exact except the reported capped sets. It also checks codec limits and `legacy_type_1`.
- `test_structure_groups.sh` step 2 also compares node and Rhino at density x4 and x0.3 and with groups off (identical).
  The sim takes `--density=X` and `--groups-off`.

## 7. In-game test suite

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
    (old jar kept in `patcherMod/build/rollback/`).
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
  running, so it is not in `mods/` yet (old jar saved as `patcherMod/build/rollback/creativeengineering-1.0.1.pre-groundmatch.e58b6e57.jar`).
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
