# Loot: every loot table in the pack, and the tools to tune chest loot

Test prefix **LT-** · Last updated **2026-10-02** · Overall status **DEPLOYED, in-game test pending** (the pre-roll
rewrite is confirmed by the 2026-09-27 12:00 log and 12:08 export; script rules, bans and packed food (3.8) pass the
offline sim but are untested in game)

## 0. At a glance

The pack has about 12,400 loot tables, but only 384 matter for chest loot. The rest are block drops (11,682)
and mob drops (242), which nothing here touches. Chest loot is edited by one framework with two stages, both
rebuilt by `/reload`:

1. **Pre-roll (KubeJS, `05_tuning.js`)**: the default for everything. Table JSON is rewritten before the game
   parses it, so odds stay exact and a removed entry's roll goes to the rest of its pool. Rules come from the
   catalog (`loot_config.json`: per-item `remove` / `frequency`, `@mod` / `/regex/` / `#tag` patterns, optional
   `tables` scope) and from calls in `07_rules.js` (section 3.8): bans with loud warnings, tag groups, boosts,
   packed-food splits, companions, added entries and pools, restored mod injections, vanilla reverts, aliases.
2. **Post-roll (LootJS)**: only what pre-roll can't reach. The same rules act on items a loot modifier or mod
   code adds (the catalog flags them `post_roll_only`; the log lists any that are tuned), and the `10`-`40`
   scripts hold LootJS-only edits: by structure, biome, dimension or player, stack caps, per-roll code.

A Python generator builds the catalog and index from the jars, structure templates and a `/dumploot` export;
`tools/loot_tuning_sim.js` runs the real runtime offline. Next to all this sit Lootr (per-player containers),
the Lootr container swaps and structure frequency (both in [structure_tuning.md](structure_tuning.md)), and
about 20 mod configs with their own loot knobs.

| Part | Layer | Status |
|---|---|---|
| Catalog + index (`loot_config.json`, `loot_index.json`) | `tools/gen_loot_config.py` | BUILT, regenerated 2026-10-26: `loot_index.json` 553 tables, 649 items, now including the 21 container tables of Subterrestrial (13), Eugene's (5) and Underground Bunkers (3) that the 2026-10-02 run missed |
| Pre-roll framework | KubeJS `ServerEvents.genericLootTables` in `05_tuning.js`, rules in `07_rules.js` | The 2026-09-27 12:00 version ran in game (LT-01, LT-06 PASSED). The full framework is DEPLOYED 2026-09-27 evening. 2026-10-02: the offline sim and a scratch server both log `4 rules -> rewrote 184 tables` (fewer than 204 because The Missing Villages is gone); after the book removal 2026-10-03 the sim says `rewrote 128 tables`; in game LT-14 to LT-27 |
| Post-roll pass + `/loottuning` | LootJS `LootType.CHEST` modifier in `05_tuning.js` | DEPLOYED; nothing logged or tested yet |
| PneumaticCraft / Lightman chest loot off at the source | `pneumaticcraft-common.toml`, `lightmanscurrency-common.lcconfig` | DEPLOYED 2026-09-27, guarded by bans |
| LootJS-only scripts + `global.LOOT` | `00_lib.js`, `10`-`40`, `99_debug.js` | DEPLOYED as scaffolding (commented examples of LootJS-only edits) |
| `/dumploot` | `_lib/loot_dump.js` | Works; last run 2026-11-05 22:44 via `/dumpall` (12,931 live tables). Two tables can't be serialised and are skipped with a warning: `farmersrespite:blocks/kettle` (custom copy function with no serializer), `myrtrees:blocks/wooden_bucket` (block-state condition on a missing property). The Brewin' port's keg serialises since the port |
| Census (this writeup's numbers) | `tools/loot_census.py` -> `loot_census.md` | BUILT; re-run 2026-10-26 (TODO `loot/lootjs-layer #3`, done): 12,342 distinct tables, 472 chest tables; the section 2 numbers below are still the 2026-10-02 run. It lists 1 "unindexed" table, `botania:inject/village_chest`: the pool the pack's own `restoreInjection` puts back, which the generator ignores on purpose |
| Lootr container swaps | patcherMod | See [structure_tuning.md](structure_tuning.md) 3.6 / ST-14 |
| Loot catalog (every container table entry by entry, structures, hand-placed containers) | `tools/loot_breakdown.py` -> [loot_catalog/](loot_catalog/README.md) | BUILT 2026-10-03: 383 tables in 47 namespaces, 963 hand-placed containers; read-only reference for writing replacement tables |

**Findings** (details in 4.2). Fixed 2026-09-27: 1 stale index, 2 scope missed 39 container tables, 3 injected
entries not cataloged, 4 goldenfood dropped village injections, 5 always-empty containers, 6 Lightman coins,
7 root rule. Open: 8 housekeeping (legacy `loot_table_dump/`, 40 MB, local only, superseded; not deleted; the
script left the repo in `d0d572c`).

**Open first:** [07_rules.js](../kubejs/server_scripts/loot/07_rules.js) (the rules),
[loot_config.json](../kubejs/server_scripts/loot/loot_config.json) (per-item knobs),
[loot_census.md](../kubejs/server_scripts/loot/loot_census.md) (per-mod tables),
[loot/README.md](../kubejs/server_scripts/loot/README.md) (API + LootJS cheatsheet).

**Knobs:**
- Per item: `"remove": true` or `"frequency": 0..100` under `items` in `loot_config.json` (above 1 boosts).
- Per mod, id pattern or tag: `patterns` -> `"@modid"`, `"/regex/"` or `"#ns:tag"`, with optional `except` and `tables`.
- Script rules in `07_rules.js` (3.8): chalk `L.frequency(i.t_loot_chalk, 0.5)`; golden food
  `L.frequency(i.t_loot_golden_food, 2)` (the Golden Food book is in no loot table since 2026-10-03);
  packed food `L.packRate(0.1)` and `L.packBoost(/village/, 3)`.
- Globally: `settings.enabled`, `settings.slack` (`redistribute` | `empty`), `settings.log_tables`.
- LootJS-only edits (structure / biome / player scoped, stack caps): `10`-`40` via `global.LOOT`.
- Whether a chest exists at all: structure frequency, house pool weights, mod configs ([structure_tuning.md](structure_tuning.md)).

**Applying:** `/reload`. Both stages rebuild. Test with `/loot give @s loot <table>`, which runs the same path as
a chest (tables, code injections, GLMs, LootJS). In the world, only containers nobody has opened yet change (Lootr rolls
per player on first open).

**Measuring:** `/loottuning`, the `[loot-tuning]` lines in `logs/kubejs/server.log`, `/dumploot` (the export shows
the rewritten tables), `python3 tools/loot_census.py`, `python3 tools/loot_breakdown.py` (per-entry catalog, see [loot_catalog/](loot_catalog/README.md)), `node tools/loot_tuning_sim.js [table ids]` (offline).
Any `!!!` banner in the log is a ban alarm (3.8).

---

## 1. Overview: how chest loot works in 1.19.2 with mods

- **A loot table** is JSON at `data/<ns>/loot_tables/<path>.json`: pools, each with `rolls` and weighted
  `entries` (`item`, `tag`, `loot_table` = roll another table, `empty`, or composites), plus conditions and
  functions. The declared `type` (`minecraft:chest`, `block`, `entity`...) only drives validation.
- **Containers get a table id, not items.** A structure template stores `LootTable` on the container, or code / a
  processor sets it at placement (vanilla mineshafts, YUNG's). The table is rolled when the container is first
  opened. With Lootr, each player gets their own roll at their first open.
- **Where the JSON comes from, lowest priority first:** the vanilla jar, then every mod jar, then `kubejs/data`.
  The last copy of an id wins. Some tables exist only in code (every KubeJS-registered block's drop, Every
  Compat's), and some JSON tables are filled from a config at roll time (Lightman's coin tiers).
- **Four ways mods add chest loot:**
  1. Ship their own tables. Most structure mods do.
  2. Replace another mod's or vanilla's table outright (`goldenfood` replaces the 16 vanilla village chests).
  3. `LootTableLoadEvent`, fired when a table is parsed. Mods append whole pools (Supplementaries, CC:Tweaked,
     Sophisticated Backpacks, Botania, Chalk, Immersive Weathering) or slip entries into an existing pool (Quark,
     Buzzier Bees, Netherite Horse Armor). **Forge fires this only for tables whose winning copy is vanilla's**:
     `LootTables.apply` passes `custom = !sourcePackId.equals("Default")` and `ForgeHooks.loadLootTable` skips
     the event when `custom` is true (bytecode-checked, Forge 43.3.9). That's why finding 4 happens.
  4. Global loot modifiers (GLMs) edit the rolled item list afterwards: Farmer's Delight (rolls its
     `fd_*` sub-tables), Armor Trims, Everything Copper, Old Guns, PneumaticCraft, Simple Hats, Lightman's coins,
     End Portal, Geode+, the_dirty_stuff. LootJS 2.x is itself a GLM.
- **Order of one roll:** JSON (already rewritten pre-roll by the pack) -> pools added/edited at load -> roll ->
  GLMs, including LootJS.
- **LootJS's `LootType` comes from the caller, not the table.** Its `LootContextBuilderMixin` maps the param set
  passed to `LootContext.Builder.create(...)` to a type. Containers, Lootr and `/loot ... loot <table>` all pass
  the chest set, so any table a container rolls is `LootType.CHEST`, whatever its declared type or path.

## 2. Reference: the loot landscape (census 2026-09-27, totals and 2.3 refreshed 2026-10-02)

All numbers come from `python3 tools/loot_census.py` (sources: vanilla jar, `mods/*.jar`, `kubejs/data`, the
12:08 `/dumploot` export, and a scan of every structure template). The full per-namespace tables are in
[loot_census.md](../kubejs/server_scripts/loot/loot_census.md).

### 2.1 Totals

| | count |
|---|---:|
| Distinct loot tables (after overrides) | 12,420 |
| Namespaces with a table / sources shipping them | 160 / 153 (vanilla + jars + `kubejs/data`) |
| Container tables (rolled by a container) | 290 |
| Chest sub-tables (pulled into a container table) | 94 |
| Block drops / mob drops / gameplay (fishing, gifts, bartering) | 11,682 / 242 / 43 |
| Config-driven (`minecraft:empty`, filled in code) / other | 10 / 59 |
| Live in game (`/dumploot`) / live-only (code-built, no JSON) / shipped but not live | 12,326 / 222 / 316 |
| Overridden by a higher-priority copy | 51 (16 of them vanilla village chests) |
| Active global loot modifiers | 150 |
| Structure-template containers with a loot table | 3,874 (`loot_container_audit.md`) |
| Items in the chest catalog | 659 |

"Shipped but not live" is almost all block drops: Steam 'n' Rails' compat tracks (168) and Display Delight
(109) name blocks from mods that aren't installed, so the game logs `Couldn't parse loot table` and skips them.
The only chest one is the_dirty_stuff's tobacco chest; that mod didn't load in the 12:00 session that ran the dump.

### 2.2 Where the files are

| Source | Path | What's there |
|---|---|---|
| Vanilla | `~/.local/share/atlauncher/libraries/net/minecraft/client/1.19.2-*/client-*-extra.jar` -> `data/minecraft/loot_tables/` | 1,026 tables: 869 blocks, 95 entities, 43 chests, 19 gameplay |
| Mod jars | `mods/<jar>` -> `data/<ns>/loot_tables/` | everything else; a jar may ship other namespaces (goldenfood ships `minecraft:chests/village/*`) |
| Pack datapack | `kubejs/data/<ns>/loot_tables/` | 10 files: Lightman's 9 entity/boss coin tiers (overrides), `felsic_gear:blocks/felsic_block` (new, an ore-style drop) |
| Code only | none | 180 live-only: everycomp 87, creativeengineering 39 (KubeJS blocks), suppsquared 21, supplementaries 13, immersive_weathering 12, tobacconistmod 6 |
| patcherMod | `development/creativeCommon/src/main/resources/data/creativeengineering/loot_tables/` | empty folder |

Folder layout is not a reliable signal. About half the container tables aren't under `chests/`: Terralith
`village/<type>/`, The Missing Villages `village/<biome>/`, Towns & Towers `village/` + `outpost/`, Wares
`agreement/` + `package/`, Philip's Ruins singular `chest/`, MES at the top level, YUNG's Better Dungeons
`<dungeon>/chests/`.

### 2.3 Chest loot by mod

"Containers" are tables a container rolls; "sub" are tables only pulled into others. "Template containers"
counts containers in structure templates that name one of the mod's tables. A table can show 0 when code or a
processor assigns it (MES, YUNG's) or when another mod's structure places it.

| Mod (namespace) | Containers | Sub | Folder(s) | Template containers, by who places them | Notes |
|---|---:|---:|---|---|---|
| Vanilla (`minecraft`) | 43 | | `chests/`, `chests/village/` | Revamped Shipwrecks 848, Towns & Towers 463, vanilla 128, Quark 120, +18 mods (Improved Pillager Outpost among them) | 16 village tables replaced by goldenfood; 27 get code pools |
| Terralith | 39 | 3 | `village/fortified/`, `village/desert/`, `spire/`, `ruin/`, `underground/` | 143 (own) | 10 outside scope; its templates name 4 missing ids |
| Wares | 35 | 1 | `agreement/`, `package/`, `chests/village/` | 5 | agreements and packages are item contents rolled in code |
| SimplyHouses | 29 | | `chests/<house>/` | 90 | not indexed yet; borrows 4 vanilla village tables |
| Philip's Ruins | 17 | | `chest/` | 485 | |
| Moog's End Structures (`mes`) | 16 | | top level | 0 (assigned in code); also places `end_city_treasure` 19x | |
| Towns & Towers (`kaisyn`) | 12 | | `village/`, `outpost/` | 219 (own) + 463 of vanilla's | 1 outside scope; its templates name 6 missing ids |
| YUNG's Better Desert Temples / Strongholds / Fortresses / Dungeons / Jungle Temples / Witch Huts / Extras | 10 / 10 / 9 / 8 / 2 / 1 / 2 | | `chests/` (Dungeons: `<dungeon>/chests/`) | 33 / 87 / 81 / 42 / 10 / 5 / 3 | also have item frames, armor stands, wishing-well blocks (2.9) |
| Beautify | 5 | | `chests/botanist/` | 5 | |
| Monobank | 5 | | `monobank/village/` | 10 | all 5 outside scope |
| PneumaticCraft | 4 | | `custom/`, `chests/` | 5 | mechanic house + 3 dungeon tiers its GLM rolls; all PNC loot removed |
| Farmer's Delight | 1 | 13 | `chests/` | 5 (Farmer's Structures) | 13 `fd_*` are GLM sub-tables of vanilla chests |
| Dustrial Decor | 2 | | `chests/`, `entities/` | 0 | |
| Chef's Delight | 1 | | `chests/` | 46 | |
| IE (`immersiveengineering`) | 1 | | `chests/` | 50 | `engineers_house` declared `minecraft:empty`, outside scope |
| Nullscape | 1 | | top level | 13 | `phantom_nest`, in droppers Lootr can't convert |
| Cloud Storage, Domestication Innovation | 1 / 1 | | `chests/` | 0 | untyped; rolled by code (balloon, pet shop), so their items get `reach: "*"` |
| Lootr, Simple Hats, Twigs | 1 / 1 / 1 | | | 0 | |
| Supplementaries | | 19 | `inject/` | | code pools into vanilla chests |
| Sophisticated Backpacks | | 8 | `inject/chests/` | | code pools |
| Botania | | 6 | `inject/` | | code pools (+ black lotus entries) |
| Old Guns | | 6 | top level | | GLM sub-tables (shipwrecks, dungeon, weaponsmith) |
| Chalk, Immersive Weathering, CC:Tweaked, the_dirty_stuff | | 3 / 3 / 1 / 1 | | | code pools / GLM |
| Sky Villages | 12 | | `chests/`, `chests/supplementaries/` | 0 (assigned in code) | 1 untyped. Added since the 09-27 census |
| It Takes A Pillage | 5 | | `chests/bastille/` | 17 (own) | 1 untyped |
| Villages & Pillages | 3 | | `village_witch/house/` | 21 (own) | |
| Watchtower Building / Illager Viking Longship / Villager Gold Washing Facility / Rare Ice | 2 / 1 / 1 / 1 | | `chests/` | 0 | `rare-ice` untyped |
| Unwrecked Ships | 1 | | `dispensers/` | 4 | a trap dispenser table; dispensers stay unconverted on purpose |
| Exposure Expanded (1.0.4) | 1 | 20 | `chests/` | 0 | film and filter sub-tables pulled into vanilla chests by GLM |
| Exposure (1.0.4) | | 5 | `chests/` | | photograph/film sub-tables (GLM) |
| Corn Delight (1.0.4) | | 2 | top level | | `shipwreck_supply`, `village_house` (GLM sub-tables; corn and seeds) |

### 2.4 Vanilla tables other mods borrow

Vanilla tables are placed by far more modded structures than vanilla ones, so a vanilla table edit reaches
Towns & Towers, Quark, Better Dungeons and others too. The top of the list (template containers):

| Table | Containers | Placed by |
|---|---:|---|
| `minecraft:chests/simple_dungeon` | 173 | Quark 118, Better Dungeons 48, Towns & Towers 4, Terralith 2, Philip's Ruins 1 |
| `minecraft:chests/pillager_outpost` | 92 | Towns & Towers 90, vanilla 2 |
| `minecraft:chests/village/village_fisher` | 69 | Towns & Towers 64, Farmer's Structures 4, vanilla 1 |
| `minecraft:chests/village/village_plains_house` | 52 | Towns & Towers 36, Farmer's Structures 6, vanilla 6, Monobank 4 |
| `minecraft:chests/end_city_treasure` | 43 | MES 43 |
| `minecraft:chests/village/village_butcher` | 37 | Towns & Towers 22, Farmer's Structures 14, vanilla 1 |
| `minecraft:chests/village/village_taiga_house` | 35 | Towns & Towers 21, vanilla 10, Monobank 4 |

SimplyHouses also borrows the fisher, butcher, fletcher and weaponsmith tables through `loot_table` entries
inside its own tables. The census has the full 30-row list.

### 2.5 Who injects what into chest loot

| Channel | Mods | Tables | Visible to the catalog? |
|---|---|---|---|
| Code pools (`LootTableLoadEvent`, whole pools) | Supplementaries 10, CC:Tweaked 9, Sophisticated Backpacks 8, Botania 6, Chalk 4, Immersive Weathering 3, Alex's Mobs 2, Domestication Innovation 1 | 19 vanilla chest tables | yes (the generator diffs `/dumploot` pool counts) |
| Code entries into existing pools | Quark (tome 5 tables, rune 4, sapling, Endermosh disc), Buzzier Bees (clover 2), Netherite Horse Armor (bastion treasure) | 9 vanilla chest tables | yes since finding 3's fix (`post_roll_only`) |
| GLM rolling a sub-table | Farmer's Delight (14 vanilla chests -> `fd_*`), Old Guns (4 vanilla chests -> 6 tables), the_dirty_stuff (plains house -> tobacco chest) | | yes |
| GLM adding one item | Armor Trims templates (21 GLMs, percent chance; 4 more target 1.20 trail-ruins tables that don't exist), Everything Copper (8 items), End Portal (purpur eye), Geode+ (goat horn) | | yes, with the GLM's chance |
| GLM in code (opaque) | PneumaticCraft `dungeon_loot` (16 tables), Simple Hats (12), Lightman's `coins_in_chests` (config lists) | | no; post-roll rules only |

### 2.6 Overrides (same id shipped twice)

| Overridden | By | Effect |
|---|---|---|
| 16 `minecraft:chests/village/*` | goldenfood | its copies win, and code injections into them stop; undone by `useVanilla` + `restoreInjection` (finding 4, 3.8) |
| 17 vanilla glass block drops | Quark | glass drops itself |
| `minecraft:blocks/copper_ore`, `deepslate_copper_ore` | CopperDrops | |
| `minecraft:blocks/amethyst_block` | Nullscape | |
| `minecraft:entities/hoglin`, `strider`, `gameplay/piglin_bartering` | Nether's Delight | |
| `alexsmobs:entities/bison`, `bunfungus` | Alex's Mobs over Alex's Delight | jar-vs-jar: the census assumes file-name order, so check in game if it matters |
| 9 `lightmanscurrency:loot_addons/{entity,boss}/tier*` | `kubejs/data` | explicit coin items instead of the configured tier item; inert while its entity drops are off |

No chest table is shipped by two mod jars, so jar-vs-jar order never matters for chest loot.

### 2.7 Whose items show up in chests (catalog)

541 items from 24 mods: vanilla 382, PneumaticCraft 30 (all removed), Farmer's Delight 18, Armor Trims 12,
Philip's Ruins 12, Old Guns 10, Sophisticated Backpacks 9, Chalk 8 (halved), Cloud Storage 8, Everything Copper 8,
Supplementaries 8, and 13 more with 1 to 6 each. 214 items show up in only one table. Not in the catalog: the
items of finding 3, Lightman's coins, and the 24 items only the out-of-scope tables hold (all of IE's
engineer's-house loot, for example).

### 2.8 Containers that always generate empty

| Missing table | Containers | Structure |
|---|---:|---|
| `terralith:village/desert/fisherman`, `leatherworker`, `shepherd` | 2 / 1 / 1 | Terralith desert village houses |
| `terralith:underground/giant_bee_hive` | 2 | Terralith giant bee hives |
| `kaisyn:village/village_birch_forest_house` | 1 | Towns & Towers birch forest house 6 |
| `kaisyn:village/village_forest_ruins/forest_ruins_{butcher,library,temple}` | 3 | Towns & Towers forest ruins |
| `minecraft:chests/village/village_leatherworker` | 1 | Towns & Towers Iberian leatherworker (vanilla has no such table) |
| `wythers:chests/village/forest_ruins_big_house` | 1 | Towns & Towers forest ruins (a Wythers leftover) |
| `luna_structures:chests/taiga_inn/taiga_inn_barrels` | 1 | SimplyHouses taiga inn |
| `themissingvillages:village/swamp_mangrove/cartographer` | 1 | The Missing Villages mangrove cartographer |

All 12 are filled since 2026-09-27 by `L.alias(...)` in `07_rules.js` (3.8), one roll of the nearest sibling table.
The generator lists them as `dead_placed` in the index report.

### 2.9 Loot that isn't a loot table

YUNG's structures also place items without loot tables: Better Fortresses / Strongholds / Desert Temples
`itemframes.json` and `armorstands.json` (chance per item), Better Strongholds `ores.json` / `rareblocks.json`,
YUNG's Extras `wishing_wells.json` (gold ore 0.5, emerald ore 0.05, gold block 0.01), all in
`config/<mod>/forge-1_19/`. The 14 converted villager-house datapacks in `kubejs/data` hold fixed chest contents
(some sherds that load empty in 1.19.2), not loot tables. None of this is visible to the loot tools.

## 3. What the pack has: the tools

### 3.1 `loot_config.json`: the knobs

Every catalog item has a `remove` and a `frequency` knob plus two read-only fields: `chests` (how many container
tables can give it) and `where` (the chance per table, in %, that one chest holds at least one, before tuning, no
Luck). `patterns` hold bulk rules. An item whose own entry is edited ignores patterns. Otherwise the first
matching pattern in file order wins.

```jsonc
"items": {
  "minecraft:bread":       { "remove": true, ... },          // never in any chest
  "minecraft:golden_apple": { "frequency": 0.25, ... },      // a quarter as often per roll, everywhere
  "quark:ancient_tome":    { "remove": true }                // not in the catalog: add it by hand, works post-roll
},
"patterns": {
  "@somemod": { "remove": true },                           // every item of a mod
  "#forge:seeds": { "frequency": 0.5 },                      // tag members, read from the same reload
  "/_sword$/": { "frequency": 0.5, "except": ["minecraft:diamond_sword"] }
},
"settings": { "enabled": true, "slack": "redistribute", "log_tables": false }
```

`frequency` above 1 boosts (pre-roll only, up to 100). A pattern can take `"tables"` (an id, `@mod` or `/regex/`, or
a list) to act only in matching tables. Rules the runtime rejects, with a warning: negative or non-number values.
`#tag` patterns work since 2026-09-27 (3.8).

**Live today:** no JSON patterns and no item edits. The three rules that used to sit here moved to `07_rules.js` on
2026-09-27: `@pneumaticcraft` and the Lightman coin regex became bans, `@chalk` became the `#creativeengineering:loot/chalk`
tag rule (same 0.5). They rewrite the same tables as before: 4 PneumaticCraft tables lose all 36 entries, and the 3
Chalk sub-tables are reweighted. PneumaticCraft's mechanic house is 100% PNC items, so its chest now rolls nothing.
That's moot while pool tuning keeps the house out of villages ([structure_tuning.md](structure_tuning.md) ST-12).

### 3.2 `05_tuning.js`: the runtime

**Pre-roll** (`ServerEvents.genericLootTables`, raw JSON before parsing). The runtime plans which tables need
work (from the index plus the rules), then rewrites each one in this order:

1. `alias()`: a table no source ships is created (from a sibling table or given pools).
2. `useVanilla()`: a jar's override of a vanilla table is replaced by vanilla's pools (stored in the index).
3. `addPool()` / `restoreInjection()`: whole pools are appended. A restore happens only where the vanilla table is
   overridden, and a pool whose `name` already exists is skipped.
4. `add()`: an item entry is added to the table's main pool (the one with the most item entries) at a set share.
5. `pack()` / `companion()`: packed food is split out of its loose entry; companions go beside their anchors.
6. Tuning: every entry with a rule gets a new integer weight. Its chance per roll becomes exactly `f x` the old one
   (`f` = 0 drops the entry, below 1 cuts, above 1 boosts).

Where the difference goes:

- `redistribute` (default): to or from the pool's untuned entries, keeping their ratios. A cut makes the others a
  bit more likely, and a boost takes its extra share from them. Chests stay as full as before.
- `empty`: a cut's lost share goes to an added `minecraft:empty` entry, so every other item keeps its exact odds.
  Boosts always take from the other entries.

Example: a pool of bread 10, apple 5, wheat 5. Bread at `frequency 0.5` goes from 50% to 25% of rolls:
`redistribute` gives bread 10, apple 15, wheat 15; `empty` gives bread 5, apple 5, wheat 5, empty 5. Bread at
`frequency 1.5` goes to 75%: apple and wheat drop to 12.5% each. If boosts ask for more than the whole pool, the
other entries keep 1% of their odds and the log warns. A boost on an entry that is its pool's only item (a
guaranteed golden apple) changes nothing.
Weights are scaled by the smallest multiple of the old total that keeps every share within 1%; `quality`
scales with the weight.

Pools are never dropped (mods look pools up by name or position). A pool left with no real entry becomes one
`empty` entry. Pools with composite entries or `expand: true` tags (4 Terralith spire pools, 6 SimplyHouses taiga
pools) can't be reweighted exactly: a tuned item there is dropped (remove), gets a `random_chance` condition (cut),
or is left alone (boosts, packing). `modify` forces the table type to `minecraft:generic`, so the runtime puts back
the type from the index (vanilla's for a reverted table, `minecraft:chest` for an alias).

**Post-roll** (LootJS on every `LootType.CHEST` roll): only what pre-roll can't reach. A removed item's stacks are
stripped (the slot stays empty); a reduced item is kept with probability `f`; a boost can't apply. Nothing is
done twice: the index's `reach` list says which rolled tables pre-roll already covers for an item, and the runtime
extends it for everything the rules add (aliases, restored pools, packed and added items). At each reload the log
lists tuned items that are post-roll only (`post_roll_only` in the catalog); today there are none.

**Every reload writes** `kubejs/exported/loot_runtime_additions.json`: per table, the pools and items the rules
added. The generator reads it so the next `/dumploot` diff doesn't take them for mod injections.

**Tools:** `/loottuning` (op level 2) prints active/inactive, slack, rules and bans, pre-roll counts, packed and
added counts, post-roll counts since the reload, tag rules, and any alarms. `settings.log_tables` logs one line
per rewritten table. Everything is in `global.lootTuning`. The script is an IIFE because all server scripts share
one scope.

### 3.3 `tools/gen_loot_config.py`: the generator

Rebuilds `loot_config.json` + `loot_index.json` from the vanilla -extra jar, `mods/*.jar`, `kubejs/data`, active
GLMs, structure templates, the `/dumploot` export and `/dumptags` (members of `expand` tags). About 3 s; the
template scan (about 45 s) is cached in `tools/.cache/template_loot.json` until the jar list changes.

- **Scope:** tables declared `minecraft:chest`, untyped / generic tables under a `chests/` or `chest/` folder, and
  every table a structure template's container names (`audit_loot_containers.scan`), plus everything those reach
  through `loot_table` entries, code pools and sub-table GLMs. **Roots** are tables nothing pulls in, or that a
  template places. Untyped roots no template places (Cloud Storage balloon, Domestication Innovation pet shop) are
  rolled by mod code, so their items get `reach: "*"`.
- **From the export:** pools past the JSON pool count (minus the ones the rules append) are code pools; item
  entries in a pool that the JSON pool doesn't have (minus the rules' own) are code-injected entries (Quark tomes
  and runes, Buzzier Bees' clover, Netherite Horse Armor...). Their weights are brought back to the JSON's scale by
  the median ratio of shared entries, and they count in the catalog's `where`. Export files much older than the
  newest (removed mods) are skipped.
- **Catalog:** every item with a chance per table; items no JSON entry holds get `"post_roll_only": true`.
- **Index:** per item, `edit` (tables with a direct JSON entry) and `reach`; per table, its declared type;
  `overridden` (tables a jar or datapack replaced, with vanilla's own copy for `useVanilla`). The `report` lists
  roots, templates' dead references (`dead_placed`), code-pool tables, injected entries, GLM sub-tables and items,
  opaque GLMs and missing references.
- **Safe re-runs:** keeps `remove` / `frequency` / any key you added, keeps edited items that vanished as
  `"missing": true`, never touches `settings` / `patterns`. Run it after mod changes, then `/reload`.
- The generator and census don't read `07_rules.js`; the catalog's `where` is the view before any rule.

### 3.4 LootJS-only scripts and `global.LOOT`

`00_lib.js` builds `global.LOOT` on every reload:

- **Targeting:** `chestType(event)` (every container roll), `tables(event, filter)` (an id, `/regex/`, or a list).
- **Entries:** `entry({item, weight, count, chance})`, `addPool(builder, specs)`.
- **Edits:** `capCount(builder, filter, max)`, `remove(builder, group)`.
- **Validation:** `strict: true` makes an unknown id or empty tag throw; `false` warns instead.
- **Reusable lists:** `groups` / `tiers`, all empty.

Each script's header sends table-based work to the pre-roll API first; what stays here is what only LootJS can do.

| Script | For | Contents today |
|---|---|---|
| `06_dead_maps.js` | pre-roll: drop explorer-map entries aimed at removed structures from listed tables | 5 Monobank vault tables, `minecraft:ruined_portal` (see structure_tuning.md section 4) |
| `10_removals.js` | removals scoped by structure / biome / dimension / player (post-roll, empty slot) | 2 commented examples |
| `20_frequency.js` | stack-size caps; bonuses gated by place or chance | 2 commented examples |
| `30_redistribute.js` | additions scoped by structure / biome (on top of the rolled loot) | 2 commented examples |
| `40_targeted.js` | per-roll code (`.apply(ctx => ...)`); `disableLootModification` | 2 commented examples |
| `99_debug.js` | `event.enableLogging()` -> `[LootJS]` lines in `logs/latest.log` | commented out |

LootJS gotcha: `LootEntry.withChance(int)` is a misnamed alias of `withWeight`; use `.when(c => c.randomChance(f))`.

### 3.5 Commands and reports

| Tool | Output | Use |
|---|---|---|
| `/dumploot` (`/dumploot all` also samples villager trades) | `kubejs/exported/loot_tables/<ns>.json` | the live tables, including code pools and the pre-roll rewrite; the generator needs it for code pools. Never deletes files of removed mods |
| `/loottuning` | chat | what the last reload did |
| `python3 tools/loot_census.py` (~30 s) | `kubejs/server_scripts/loot/loot_census.md` | the census behind section 2; flags index drift, out-of-scope tables, dead references |
| `python3 tools/audit_loot_containers.py` | `kubejs/generation_scripts/worldgen/loot_container_audit.md` | which containers Lootr converts, swaps or misses |
| Just Enough Resources | JEI "Dungeon" tab | shows chest tables from the server's loot manager: pre-roll edits yes, GLMs / post-roll no |

### 3.6 Lootr and whether a chest exists

- **Lootr** (`config/lootr-common.toml`): every structure chest, barrel, trapped chest and shulker box (plus Quark
  chests, `convert_quark = true`) becomes per-player. `randomise_seed = true`. `refresh_*` / `decay_*` lists
  (tables, mod ids, dimensions, structures) can refill or remove containers over time; all empty today.
  `loot_table_blacklist` / `loot_modid_blacklist` keep tables as normal shared chests.
  `report_unresolved_tables = true` logs ids Lootr can't resolve (useful with finding 5).
- **Container swaps and structure / house frequency:** [structure_tuning.md](structure_tuning.md). Removing a
  structure or a village house is the only way to remove its chests.

### 3.7 Mod configs with their own loot knobs

| Mod | File | Knob (current value) |
|---|---|---|
| Lightman's Currency | `config/lightmanscurrency-common.lcconfig` `[loot]` | tier items T1-T6; `[loot.chests] enabled=false` (2026-09-27) + per-tier table lists; `[loot.entities] enabled=false`. Guarded by a ban (3.8) |
| PneumaticCraft | `pneumaticcraft-common.toml` | `enable_dungeon_loot = false` (2026-09-27). Guarded by a ban (3.8) |
| Sophisticated Backpacks | `sophisticatedbackpacks-common.toml` | `chestLootEnabled = true` |
| Farmer's Delight | `farmersdelight-common.toml` | `generateFDChestLoot = true` |
| Alex's Mobs | `alexsmobs.toml` | `addLootToChests = true` |
| Domestication Innovation | `domestication-innovation.toml` `[loot]` | per-enchantment book chances (0.15 to 0.65), `pet_curse_enchantments_loot_only` |
| Allurement | `allurement-common.toml` | enchanted horse armor "Generates in loot tables", unenchanted table list |
| Quark | `quark-common.toml` | Ancient Tomes `Loot Tables` (table,weight list), Endermosh disc `Add To End City Loot` / weight 5, Ancient Wood `Ancient City Loot Weight` 8, Monster Box `Enable Extra Loot Table` (false since 2026-10-30: the vault box's one reward replaces the per-mob roll, [mob_block_tweaks.md](mob_block_tweaks.md) 2.9), `Replace Worldgen Chests` |
| Netherite Horse Armor | `netherite_horse_armor-common.toml` | `weight = 8` in bastion treasure (0 disables) |
| Immersive Armors | `immersive_armors.json` | `lootChance = 0.025` |
| Dustrial Decor | `dustrial_decor-common.toml` | `loot_table_modification = false` |
| YUNG's Better Dungeons | `betterdungeons-forge-1_19.toml` | small dungeon min/max chest count 1-2 |
| YUNG's (item frames, armor stands, wishing wells) | `config/<mod>/forge-1_19/*.json` | see 2.9 |
| Monobank | `monobank-common.toml` | `StealingFromContainersIsACrime` (loot-table containers near protected structures) |

Remember the [configs_edited rule](../configs_edited) when changing any of these: copy the edited file there
too.

### 3.8 Script rules (`07_rules.js`)

`05_tuning.js` (priority 100) exposes `global.lootTuning`; `07_rules.js` (priority 0) calls it. Item targets are the
same as `patterns` (`'ns:id'`, `'@mod'`, `'/regex/'`, `'#ns:tag'`, a JS regex, a list), written as `global.item`
keys; table filters take ids, `@mod`, regexes or lists. Precedence: bans, then an item's own `loot_config.json`
entry, then JSON patterns in file order, then script rules in call order. The full API is in
[loot/README.md](../kubejs/server_scripts/loot/README.md).

**Bans.** `L.ban(target, { name, why, modLootOn, ownTables })` removes the item like `remove`, and assumes the mod's
own loot switch already keeps it out. It raises a loud warning (a `!!!` banner at ERROR level in
`logs/kubejs/server.log`, red chat to online ops, and the list again to any op who logs in before the next reload) in
three cases:

| When | Check | Means |
|---|---|---|
| every reload | `modLootOn()` reads the mod's live config object (`ConfigHelper.common().general.enableDungeonLoot`, `LCConfig.COMMON.enableChestLoot` / `enableEntityDrops`) | the config was reset or edited back on |
| pre-roll | a banned entry in a table outside `ownTables` (default for `'@mod'`: that mod's own tables) | another mod or datapack ships the item |
| post-roll | a chest roll produced a banned stack (it's stripped) | a GLM or code pool added it after the pre-roll pass |

Live bans: `@pneumaticcraft` (switch: `enable_dungeon_loot = false`) and Lightman's coins, coin piles and coin blocks
including the chocolate ones (switches: `[loot.chests]` and `[loot.entities] enabled=false`; `ownTables`
`@lightmanscurrency`). Pre-roll still strips the 36 entries of PneumaticCraft's own 4 tables quietly; that's expected.

**Tag rules.** Tags aren't bound while loot tables load, but the reload's tag event runs earlier (its prepare phase;
loot tables apply after the barrier). `95_tag_capture.js` (priority -1000, so its `ServerEvents.tags('item')` handler
is last) copies the members of every tag a rule names. It also reads during KubeJS's pre pass, which forces the real
re-run (see `tag_helper.js`). If the capture didn't run, the runtime falls back to the registry's bound tags (last
reload's; empty on a fresh start) and warns. Two meta tags in `tags/loottags.js`:

- `#creativeengineering:loot/chalk` (`i.t_loot_chalk`) = `#chalk:chalks` (nested, so new colours follow) + the chalk
  box. `L.frequency(i.t_loot_chalk, 0.5)`: 8 of its 17 members show up in chest loot.
- `#creativeengineering:loot/golden_food` (`i.t_loot_golden_food`) = golden apple, golden carrot and Golden Food's
  23 plain `golden_*` foods (the enchanted ones and the notch apple are left out as the rare tier). Only the golden
  apple and carrot are in chest loot today.

**Packed food: a split.** `L.pack(packed, loose, { rate, max })` splits each pool entry of the loose item. The entry
keeps its chance per roll, and no other entry in the pool changes. Within it, think of the roll as a number from 0 to
1: below `1 - rate` it gives the loose food as before; above, crates, and the higher the roll the more crates
(1 up to `max`, each step half as likely as the last). `max` comes from the loose entry's own stack size (about a
quarter of its largest stack, 1 to 3: a 1-7 potato stack gets up to 2 crates). Each crate band is its own entry,
copying the loose entry's conditions.

- `L.packRate(0.1)`: a tenth of each loose entry becomes crates. `L.packBoost(/village/, 3)` triples that in every
  table whose id contains `village` (vanilla, FD `fd_village_*`, Terralith, The Missing Villages, Towns & Towers,
  Monobank, Wares packages), so villages give crates for 30% of those rolls. Rates are capped at 0.9, so a loose
  entry never vanishes. `L.packExclude(/^wares:agreement\//)`: Wares trade agreements list items a trader asks for.
- Example, vanilla taiga house (village): potato 10 of 60 weight becomes potato 7, one crate 2, two crates 1 (by
  share); sweet berries 5 become berries 3.5, one sack 1, two sacks 0.5. Everything else keeps its odds.
- Pairs (from the jars' 3x3 recipes; Quark's own carrot/potato/beetroot/apple crates are removed items, so FD's and
  Vinery's are used). Sim on 2026-09-27 (evening):

| Packed | Loose | Pools (village-boosted) |
|---|---|---:|
| `vinery:apple_crate` | apple | 30 (16) |
| `farmersdelight:potato_crate` | potato | 27 (16) |
| `farmersdelight:carrot_crate` | carrot | 11 (4) |
| `quark:berry_sack` | sweet berries | 7 (5) |
| `quark:glowberry_sack` | glow berries | 6 (1) |
| `quark:cocoa_beans_sack` | cocoa beans | 5 (4) |
| `farmersdelight:beetroot_crate` / `onion_crate` / `rice_bag` | beetroot / onion / rice | 4 (2) each |
| `endersdelight:chorus_crate` | chorus fruit | 1 (0) |
| cabbage, tomato, cherry, red / white grape, ancient fruit crates | | 0: no chest holds the loose item today |

  99 loose entries split into 146 crate entries. Skipped: 6 SimplyHouses taiga pools with composite entries and
  `farmersdelight:chests/fd_pillager_outpost`, which already has an onion crate. Left out on purpose: golden apple /
  golden carrot crates, egg crates, hay bales, nether wart sacks, Rustic Delight's pepper crates (no dict keys, never
  in chest loot).

**Golden Food (finding 4).** goldenfood ships copies of all 16 vanilla village chest tables. The only change is one
`enchanted_book` entry (weight 1) carrying its `goldenfood:gf_enchantment`, which an anvil uses to turn golden apples
and carrots into enchanted ones. Because the copies replace vanilla's files, Forge fires no `LootTableLoadEvent` for
them. Every mod that injects pools in code lost its village loot. A bytecode audit of every jar with an event
handler found three such mods: Chalk (cartographer, mason, plains house, savanna house), Botania (temple,
toolsmith, weaponsmith; rolls 0-1) and CC:Tweaked (cartographer; its `treasure_disk` table is empty until a
datapack fills it). Immersive Armors, Allurement, Farmer's Delight, Everything Copper and the other loot modifiers
aren't affected. Dustrial Decor's handler is switched off (`loot_table_modification = false`). The rules:

1. `L.useVanilla(/^minecraft:chests\/village\//, { winner: /goldenfood/ })`: the 16 tables get vanilla's pools back.
2. `L.restoreInjection(...)` x3: the Chalk, Botania and CC pools, exactly as their code builds them, only where the
   vanilla table is overridden (if goldenfood is removed, Forge injects them itself and these skip).
3. `L.frequency(i.t_loot_golden_food, 2)`: golden food is twice as likely per roll wherever it's an entry (58 entries;
   simple dungeon golden apple 11.6% -> 22% of pool 0). Pools where it's the only item (the igloo's guaranteed
   golden apple) can't go higher.

The Golden Food enchantment book is in no loot table (removed 2026-10-03): the vanilla revert in rule 1 drops
goldenfood's own copy, and the companion and village/camp `L.add` rules that used to place it are gone. Its anvil
recipe (book + golden apple/carrot) is untouched, so the book needs another source if it should exist in survival.

**Always-empty containers (finding 5).** `L.alias(missing, source)` creates the 12 tables structure containers name
but no mod ships, as one roll of a sibling: Towns & Towers birch-forest house and forest ruins (plains house,
butcher, Terralith fortified library, temple), the Wythers leftover (plains house), SimplyHouses' taiga inn
barrels (SimplyHouses' own copy under `luna_structures:`), the Iberian leatherworker and Terralith desert
leatherworker (tannery), Terralith desert fisherman (fisher) and shepherd (shepherd), The Missing Villages mangrove
cartographer (its swamp cartography). Terralith's giant bee hive has no sibling, so it gets its own pool:
honeycomb 1-3, honey bottle, glass bottle, 2-4 rolls. An alias never touches a table that exists.

**Offline check:** `node tools/loot_tuning_sim.js [table ids]` runs the real `05` + `07` + tag scripts against the raw
jar tables and the `/dumptags` export and prints the `[loot-tuning]` line, the per-pair table above, golden food,
reverts, restores, aliases and bans. It fails if a banned item survives, a split's crate share is off
by more than 2% (or loose + crates don't add up to the old loose share in pools nothing else touches), a reverted
table still has goldenfood's book, a restored pool or alias is missing, or a tag rule has no members. It reads the
two mod configs from disk, so a switched-on config shows up as an alarm there too.

## 4. Limits and decisions

### 4.1 Decisions

- **KubeJS pre-roll (`ServerEvents.genericLootTables`) is the default.**
  - *Strengths:* it edits the table JSON before it's parsed, so it can express anything a loot table can: exact
    weights, removals that hand their roll to the rest of the pool, boosts, new entries, new pools, whole tables
    (aliases, vanilla reverts). The result is an ordinary table, so JEI/JER, `/dumploot` and every mod that reads
    tables see it. It costs nothing per roll. It re-runs on `/reload`. It doesn't block Forge's
    `LootTableLoadEvent` (its mixin redirects the `forEach` in `LootTables.apply`; vanilla's lambda still reads the
    real source pack).
  - *Weaknesses:* it only sees JSON. It can't see what loot modifiers add after the roll (Lightman, PneumaticCraft,
    Armor Trims, Simple Hats), pools or entries mods add in code later in the same load (Quark's tomes), or tables
    built entirely in code. It can't list tables (the map is private and reflection is class-filtered), so
    targets come from the offline index, which must be regenerated after mod changes. `modify` forces the type
    to `minecraft:generic` and creates a table for an unknown id. Tags aren't bound yet (hence
    `95_tag_capture.js`). It knows nothing about the chest's position, structure, biome or player, and `JSON.stringify` is
    Rhino's Gson-backed one.
- **LootJS 2.9.1 post-roll only where pre-roll can't reach.**
  - *Strengths:* it runs after every loot modifier and code pool, on the final rolled list, so it's the only way to
    touch what those add. It knows the context: loot type by caller (every container roll is `CHEST`, whatever the
    table), structure, biome, dimension, player, luck, the rolled stacks themselves. Tags are bound. It also has
    per-roll code (`apply`), stack-size edits and conditions (`anyStructure`, `randomChance`).
  - *Weaknesses:* it never sees a weight. A removal leaves an empty slot, a "reduction" is random thinning, a boost
    is impossible, and an addition sits on top of the roll instead of competing in the pool. Its edits are
    invisible to JEI/JER and `/dumploot` (only rolling shows them). It runs on every chest roll. It's the 2.x API:
    `withChance` is a misnamed `withWeight`, and `replaceLoot`'s third argument is `preserveCount` (the deleted
    `L.reweight` helper relied on it and never changed a weight).
  - The Loot Integrations mod was rejected: it only swaps whole modded structure tables, per mod. Hand-written datapack
    JSON per table was rejected too: about 370 files, no "remove X everywhere", and a vanilla-table override
    silently kills mods' injections (finding 4).
- **Boosting is allowed, pre-roll.** `frequency > 1` (up to 100) takes the extra share from the pool's other
  entries. The earlier "no boosting" rule came from the catalog view; with the pre-roll runtime doing exact
  shares, a boost is as exact as a cut. It can't apply post-roll or inside composite pools.
- **`#tag` rules via a capture, not the registry.** Tags aren't bound while loot tables load, but the same reload's
  tag event has already run, so `95_tag_capture.js` copies the members there. Rejected: resolving tags offline in
  the generator from `/dumptags` (stale until the next dump); post-roll-only tag rules (empty-slot semantics).
- **Packed food is a split of the loose entry (user, 2026-09-27).** A low roll within the entry gives loose food, a
  high one crates, the highest the most crates; no other entry changes. This replaced the first version (an extra
  crate entry beside the loose one, which diluted the rest of the pool). A rate can't pass 0.9, so the village boost
  tops out there.
- **goldenfood: revert and restore rather than edit the jar.** Deleting its tables from the jar would bring
  back every injection automatically, but the pack manifest rebuilds mods from their download source, so a
  modified jar wouldn't survive a rebuild. A patcherMod mixin that makes Forge fire the event anyway was the other
  option. It would also restore injectors nobody knows about, but the bytecode audit found only three, so data
  rules cover it without a Java change.
- **Bans live in script, next to the config check.** The mod's own switch is the real removal; the ban is the
  alarm. JSON patterns can't hold a config check, so the old `@pneumaticcraft` / coin patterns moved to `07_rules.js`.
  The Python generator and census don't read `07_rules.js`, so the census's "tuned" column doesn't show script rules.
- **The index comes from files, not the game.** Table ids come from the offline index (see the KubeJS weaknesses).
  Hence "re-run the generator after mod changes". What the rules add is written back
  (`loot_runtime_additions.json`) so the next generation isn't fooled by it.

### 4.2 Known gaps and their fixes

| # | Gap | Status |
|---|---|---|
| 1 | Index predated SimplyHouses / the_dirty_stuff | FIXED 2026-09-27 (regenerated). Re-run the generator after every mod change |
| 2 | Scope missed 39 container tables (129 containers: Terralith houses, Missing Villages sets, Monobank vaults, IE engineer's house, Nullscape nests) | FIXED 2026-09-27: every table a template names seeds the scope (376 tables, census agrees) |
| 3 | Entries injected into existing pools weren't cataloged | FIXED 2026-09-27: diffed per pool against `/dumploot`, in `where`, flagged `post_roll_only` (10 tables: Quark, Buzzier Bees, NHA...) |
| 4 | goldenfood blocked village injections | FIXED 2026-09-27: vanilla pools back, Chalk/Botania/CC restored, its book later removed from all tables (3.8) |
| 5 | 12 containers named missing tables | FIXED 2026-09-27: aliases (3.8) |
| 6 | Lightman coins spawned then stripped | FIXED 2026-09-27: `[loot.chests] enabled=false`, regex kept as a ban |
| 7 | Root rule dropped borrowed vanilla tables | FIXED 2026-09-27: root = no parent, or placed by a template |
| 8 | Stale exports, legacy dump | Stale exports: the generator skips them (FIXED). `dump_loot_tables.sh` was deleted from the repo in commit `d0d572c` (2026-09-28; `.gitignore` still allow-lists it). Open: the local `loot_table_dump/` folder (40 MB, gitignored, never in the repo) and any local copy of the script; delete when you're happy (it's your call) |

### 4.3 Other limits

- A post-roll removal leaves an empty slot. Only the pre-roll path refills from the pool.
- Pre-roll exactness assumes the JSON pool is the whole pool. If a mod later slips an entry into the same pool
  (Quark's tome into `simple_dungeon`), the tuned share is slightly off.
- A table-scoped rule (`tables`) is matched against the table that holds the entry. For an entry in a sub-table,
  include the sub-table in the filter (e.g. `/village/` already matches FD's `fd_village_*`).
- A tuned item that is only reachable from a `reach: "*"` table is never thinned post-roll.
- Mob drops, block drops, fishing and bartering are out of scope, even for the same item.
- Packing, companions and boosts only act where the item is a direct JSON entry in an indexed table.
- The catalog's `where` is the view before any rule: it still shows goldenfood's copies and no crates.
- Worlds: a `/reload` reaches only containers nobody has opened. Lootr containers roll per player at first open.

## 5. File map

- Config and data: [loot_config.json](../kubejs/server_scripts/loot/loot_config.json),
  [loot_index.json](../kubejs/server_scripts/loot/loot_index.json) (generated),
  [loot_census.md](../kubejs/server_scripts/loot/loot_census.md) (generated),
  [README.md](../kubejs/server_scripts/loot/README.md)
- Runtime: [05_tuning.js](../kubejs/server_scripts/loot/05_tuning.js), [07_rules.js](../kubejs/server_scripts/loot/07_rules.js),
  [95_tag_capture.js](../kubejs/server_scripts/loot/95_tag_capture.js), [loottags.js](../kubejs/server_scripts/tags/loottags.js),
  [00_lib.js](../kubejs/server_scripts/loot/00_lib.js),
  [06_dead_maps.js](../kubejs/server_scripts/loot/06_dead_maps.js),
  [10_removals.js](../kubejs/server_scripts/loot/10_removals.js), [20_frequency.js](../kubejs/server_scripts/loot/20_frequency.js),
  [30_redistribute.js](../kubejs/server_scripts/loot/30_redistribute.js), [40_targeted.js](../kubejs/server_scripts/loot/40_targeted.js),
  [99_debug.js](../kubejs/server_scripts/loot/99_debug.js), [_lib/loot_dump.js](../kubejs/server_scripts/_lib/loot_dump.js) (`/dumploot`)
- Tools: [gen_loot_config.py](../tools/gen_loot_config.py), [loot_census.py](../tools/loot_census.py),
  [loot_breakdown.py](../tools/loot_breakdown.py) + [loot_breakdown_scan.py](../tools/loot_breakdown_scan.py) (output: [loot_catalog/](loot_catalog/README.md)),
  [loot_tuning_sim.js](../tools/loot_tuning_sim.js),
  [audit_loot_containers.py](../tools/audit_loot_containers.py)
- Pack datapack overrides: [kubejs/data/lightmanscurrency/loot_tables/](../kubejs/data/lightmanscurrency/loot_tables/),
  [felsic_block.json](../kubejs/data/felsic_gear/loot_tables/blocks/felsic_block.json)
- Neighbours: [loot_container_swaps.json](../kubejs/generation_scripts/worldgen/loot_container_swaps.json),
  [loot_container_audit.md](../kubejs/generation_scripts/worldgen/loot_container_audit.md),
  [lootr-common.toml](../config/lootr-common.toml)
- Mod configs: [pneumaticcraft-common.toml](../config/pneumaticcraft-common.toml),
  [lightmanscurrency-common.lcconfig](../config/lightmanscurrency-common.lcconfig) (copies in `configs_edited/root_config/`)
- Exports: `kubejs/exported/loot_tables/` (8 stale files the generator skips: colony4cc, distantfriends,
  domum_ornamentum, minecolonies, multipiston, refinedcooking, refinedstorage, tobacconistmod),
  `kubejs/exported/loot_runtime_additions.json` (written every reload), `tools/.cache/template_loot.json` (scan cache)
- Legacy, superseded: `dump_loot_tables.sh` (deleted from the repo in `d0d572c`; old copy: `git show 609cd21:dump_loot_tables.sh`), `loot_table_dump/` (local only, gitignored)

## 6. Offline checks

- In game, `/cdevgen run loot dumps` runs `/dumploot` + `/dumptags item`, then `gen_loot_config.py`, the audit and the
  census, and says in chat which files changed (patcherMod `/cdevgen`, list in `tools/generators.json`). `/cdevgen run
  loot_config` alone is about 2 s. Full output lands in `logs/cdevgen/<id>.log`.
- `python3 tools/gen_loot_config.py` prints scope / GLM / item counts. On 2026-09-27 (evening): `scope: 376 tables
  (309 roots, 237 placed by templates, 2 code-rolled orphans, 19 with code-added pools, 10 with code-injected
  entries)`, 615 items (55 post-roll only), 12 dead template references.
- `python3 tools/loot_census.py`: totals, index drift ("chest tables missing from loot_index.json"),
  out-of-scope tables, dead references. It should agree with section 2 until the next mod change.
- `node tools/loot_tuning_sim.js [table ids]` (about 5 s): runs the real runtime + rules offline (3.8). On
  2026-09-27 (evening) it gives `4 rules -> rewrote 204 tables (36 entries removed, 59 pools reweighted, 0 via
  conditions, 58 boosted entries; packed 99 loose entries into 146 crate entries; 51 companions, 114 items + 0 pools
  added, 8 injections restored, 16 reverted to vanilla, 12 aliases)` and "all checks passed". Earlier the same day a
  scratch variant that flipped the PNC switch on and planted a coin and a PNC item in `simple_dungeon` raised all
  three expected banners. After any `/dumploot`, compare a tuned
  table in the export with its jar copy to see the rewritten weights (LT-06, LT-16).
- `grep "\[loot-tuning\]" logs/kubejs/server.log` after any launch or `/reload`.

## 7. TODO

  - [x] remove 'golden food enchantment' book from loot tables (2026-10-03: companion + village `L.add` rules deleted from `07_rules.js`; sim checks it; in-game LT-23)
  - Comprehensive loot table system, remapping all loot tables (using existing tables when possible). tree-like tag structure for tables (container/village/leatherworker/mediocre_loot_table, container/village/leatherworker/tresure_loot_table, container/ocean/shipwreck/tresurea, etc), item merging for many-item-to-many-table looting 
  - Cover missed lootr chests, fixed loot chests (bible verse written book + golden carrot). Inventory of every hand-placed container: [loot_catalog/handplaced.md](loot_catalog/handplaced.md) (regenerated 2026-10-14; the verse chest is `philipsruins:ancient_tower_8` (6, 2, 5) and holds a writable book, not a written one)
  - Find island loot tables (only present in 1 or 2 structures), fold into other loot tables (list done 2026-10-03: [loot_islands.md](loot_islands.md), 172 tables; nothing folded yet)
  - lower chalk frequencies
  - ensure coins are removed from loot tables
  - remove pneumaticcraft loot
  - incraese food crate/sack frequency, boost further in farming/cooking structures
  - add cans to loot pool, appear in villager chests, camps, ships, boosted in farming/cooking structures
  - mca comb, thread, bouquets (boosted in flower houses), rings (rare, in homes or blacksmith) to villager houses
  - override Quark's Lootr chests so every one gets the resource pack skin (scratchpad 2026-10-21, TODO `loot/lootr #2`)
  - Autumnity port: maple banner patterns in a new autumn loot table (scratchpad 2026-10-21, TODO `ports/autumnityport #5`)
  
### Loot table changes

- Basic loot table
  - Mob loot (Rotten flesh, bones, string. rarer: spider eye, gunpowder. rarest: ender pearl)
  - Crafting material (Sticks, leather, paper, feathers, clay balls, wool, coal, charcoal)
  - Minable (Copper ingots, copper nugs, iron nugs. rarer: iron ingot, gold nugs, lapis, redstone)
  - Emeralds (emeralds, rarely: emerald blocks)
  - Tools (copper tools, copper shield, boats, flint and steel, bundle, leather/copper horse armor, fishing rod (new, damaged, damaged + low enchantment) arrows, bow/crossbow (new, damaged, damaged + low enchantment). rarer: quiver, shears, bucket, compass, clock, iron tools, spyglass, iron/gold shield, iron/gold horse armor)
  - food (wheat, bread, potatoes, apples, carrots, beetroots, sugar, empty bowl, bonemeal. rarer: vanilla soup/stews, vanilla cooked food, crate/sacks of existing food in loot, vanilla cake cookies and pie, cans of vanilla food, )
  - building (lantern, chain, glass, painting, ladder, armor stand, flower pot (any), copper bars)
  - rare random (any dye, bricks, books, amethyst shard, candle, target, saddle, empty map, book and quill, vanilla banner pattern, any vanilla flower, lv1 enchanted book)

- Blacksmith/armorer loot table
  - tools (copper tools, copper armor, copper horse armor, copper anvil, bucket, shears, copper shield. rarer: iron tools, iron armor, iron/gold shield, gold tools+armor)

## 8. In-game test suite

Use `/loot give @s loot <table>` to roll a table on demand. It runs the whole chain (code pools, GLMs, LootJS).
Revert every temporary edit and `/reload` afterwards.

- [x] **LT-01 Pre-roll runs:** launch or `/reload` -> `logs/kubejs/server.log` has
  `[loot-tuning] 3 rules -> rewrote 7 tables (36 entries removed, 7 pools reweighted, 0 via conditions)`.
  PASSED 2026-09-27 (12:00:42 log).
- [ ] **LT-02 Status command:** `/loottuning` -> "active, slack=redistribute, 4 rules (2 bans)", a pre-roll line
  with 204 tables rewritten and 58 boosted, "packed: 16 pairs, rate 0.1, 1 boosts -> 99 loose entries split into 146
  crate entries, 6 skipped", "added: 0 companions, 0 items, 0 pools, 8 restored injections, 16 tables reverted to
  vanilla, 12 aliases", a post-roll line ending "0 tuned items are post-roll only", a bans line, the two tag rules
  (chalk 17, golden food 25), and no ALARM lines.
- [ ] **LT-03 PNC removed pre-roll:** `/loot give @s loot pneumaticcraft:chests/mechanic_house` 5x -> nothing at
  all (the table was 100% PNC).
- [ ] **LT-04 PNC GLM path:** `/loot give @s loot minecraft:chests/simple_dungeon` 20x -> no PneumaticCraft items;
  vanilla dungeon loot, Quark tomes and runes and Botania items still show up.
- [ ] **LT-05 Lightman coins gone at the source:** during LT-04, no coins at all (simple_dungeon is in Lightman's T3
  list), no red `[loot]` chat line, and `/loottuning` "bans: ... 0 stacks stripped post-roll" (
  `[loot.chests] enabled=false` the coins never roll; a nonzero count means the switch isn't working).
- [x] **LT-06 Chalk halved:** `/dumploot`, then compare the chalk tables in `kubejs/exported/loot_tables/chalk.json`
  with the jar -> chalk's share of each pool is halved. PASSED 2026-09-27 (12:08 export): `village_chalk_loot` empty
  70 -> 175 with chalk weights kept (33% -> 17%), `dungeon_chalk_loot` chalk weights halved with empty 60 -> 75,
  `desert_pyramid_chalk_loot` empty 70 -> 181; the 4 PNC tables are a single `minecraft:empty` entry. The only
  untuned entry in these pools is their own `empty`, so `redistribute` fed it.
- [ ] **LT-07 Item remove:** add `"remove": true` to `minecraft:bread`, `/reload` -> log shows 5 rules and more
  tables; `/loot give @s loot minecraft:chests/village/village_plains_house` 20x -> never bread, chests not emptier.
- [ ] **LT-08 Pattern with except on injected items:** add `"@quark": {"remove": true, "except": ["quark:ancient_tome"]}`,
  `/reload` -> 30 rolls of `minecraft:chests/simple_dungeon` show ancient tomes but never blank runes.
- [ ] **LT-09 Slack modes:** set `minecraft:bread` to `"frequency": 0.5`, `/reload`, `/dumploot` -> in
  `minecraft.json`, `chests/village/village_plains_house` has no new `minecraft:empty` entry and the other items'
  weights grew. Then `"slack": "empty"`, `/reload`, `/dumploot` -> `/loottuning` says slack=empty, the table gained a
  `minecraft:empty` entry, and the other items' shares are unchanged.
- [ ] **LT-10 Kill switch:** `"enabled": false`, `/reload` -> `/loottuning` says inactive; LT-03 gives PNC items again,
  and village chests are goldenfood's copies again (no crates, no restored Chalk pool).
- [ ] **LT-11 LootJS-only script:** uncomment the village rotten flesh example in `10_removals.js` and
  `enableLogging()` in `99_debug.js`, `/reload`, open never-opened chests inside a village -> no rotten flesh;
  `logs/latest.log` has `[LootJS]` lines naming `CE/loot/removals/village_flesh`. (`/loot give` has no position, so
  structure-scoped rules need a real chest.)
- [ ] **LT-12 Lootr timing:** with LT-07's rule active, open a never-opened village chest in fresh chunks -> no bread.
  A chest you opened before the `/reload` keeps its old contents.
- [ ] **LT-13 Formerly out-of-scope table, now pre-roll:** add `"immersiveengineering:coal_coke": {"remove": true}`,
  `/reload` -> 20 rolls of `immersiveengineering:chests/engineers_house` never give coal coke and never leave a gap;
  `/dumploot` shows no coal coke entry in that table (it's in scope since finding 2's fix).

- [ ] **LT-14 Framework loads:** launch or `/reload` -> `logs/kubejs/server.log` has
  `[loot-tuning] 4 rules -> rewrote 204 tables (36 entries removed, 59 pools reweighted, 0 via conditions, 58 boosted
  entries; packed 99 loose entries into 146 crate entries; 0 companions, 0 items + 0 pools added, 8 injections
  restored, 16 reverted to vanilla, 11 aliases)` (2026-10-03 sim: 128 tables, 94 entries into 137 crate entries; what `node tools/loot_tuning_sim.js` prints), no `!!!` banner, no
  "tag rules resolved from the registry" warning, and `kubejs/exported/loot_runtime_additions.json` is fresh.
- [ ] **LT-15 Chalk tag:** `/kubejs hand` on a chalk box and on red chalk -> both list `creativeengineering:loot/chalk`.
  `/dumploot`, then `chalk.json` -> `village_chalk_loot` etc. show the halved chalk weights, same as LT-06.
- [ ] **LT-16 Packed food split, in the export:** `/dumploot`, then in `minecraft.json` ->
  `chests/village/village_plains_house` has potato 353, potato crate 101, potato crate x2 51, apple 353, apple crate
  152 (crates are 30% of each loose item's old share; bread, emerald and the rest keep their odds);
  `chests/pillager_outpost` pool 1 reads wheat 14, potato 9, potato crate 1, carrot 9, carrot crate 1.
- [ ] **LT-17 Packed food in chests:** `/loot give @s loot minecraft:chests/village/village_plains_house` 20x -> about
  3 crate results per 7 loose-potato stacks (some of them 2 crates), and potatoes overall about as common as
  before counting crates. `/loot give @s loot minecraft:chests/pillager_outpost` 20x -> about one crate per 9 potato /
  carrot stacks (rate 0.1).
- [ ] **LT-18 Config switch alarm:** set `enable_dungeon_loot = true` in `config/pneumaticcraft-common.toml`, wait a
  few seconds (Forge reloads the file), `/reload` -> red `[loot] BANNED loot is switched ON again: PneumaticCraft` in
  chat and a `!!!` banner in `logs/kubejs/server.log`; relog -> the op login notice repeats it. Revert, `/reload`,
  no message.
- [ ] **LT-19 Post-roll alarm:** `/loot give @s loot lightmanscurrency:loot_addons/chest/tier1` -> nothing given, red
  `[loot] BANNED item lightmanscurrency:coin_copper rolled in chest loot lightmanscurrency:loot_addons/chest/tier1`
  (that table is the coin source, so here the alarm is expected; it proves the post-roll path).
- [ ] **LT-20 Tuning a packed item:** add `"farmersdelight:potato_crate": {"remove": true}` to `loot_config.json`
  items, `/reload` -> LT-14's line shows 5 rules; `/dumploot` -> no potato crates anywhere, and the village plains
  house potato entry is back to its full weight (a removed packed item switches its split off). Revert.
- [ ] **LT-21 goldenfood reverted, injections back:** `/dumploot`, then in `minecraft.json` -> none of the 16
  `chests/village/*` tables has goldenfood's `{Enchantments:[{id:"goldenfood:gf_enchantment"...` book (no `gf_enchantment` anywhere);
  `village_plains_house` has a second pool rolling `chalk:chests/village_chalk_loot`, `village_temple` one rolling
  `botania:inject/village_chest`, `village_cartographer` both Chalk's and the `computercraft_treasure` pool.
  `/loot give @s loot minecraft:chests/village/village_mason` 20x -> some chalk.
- [ ] **LT-22 Golden food doubled:** `/dumploot` -> in `chests/simple_dungeon` pool 0, golden apple is about 22% of the
  weight (was 11.6%); `/loot give @s loot minecraft:chests/simple_dungeon` 20x -> noticeably more golden apples.
  The igloo still gives exactly one golden apple.
- [ ] **LT-23 Golden Food book gone:** `/dumploot` -> `grep gf_enchantment kubejs/exported/loot_tables/*.json` finds
  nothing; 20 rolls of `minecraft:chests/simple_dungeon` and a few village tables never give the book.
  `node tools/loot_tuning_sim.js` prints "the Golden Food book is in 0 tables".
- [ ] **LT-25 Aliases fill empty containers:** `/loot give @s loot terralith:village/desert/fisherman` -> fisher loot;
  `/loot give @s loot terralith:underground/giant_bee_hive` -> honeycomb / honey. In a fresh Terralith desert village,
  the fisherman house chest isn't empty.
- [ ] **LT-26 Table-scoped boost:** in `07_rules.js` add `L.frequency(i.emerald, 3, { tables: /village/ })`,
  `/reload`, `/dumploot` -> emerald's share triples in village tables and is unchanged in non-village tables that
  have it (e.g. `chests/jungle_temple`). Revert.
- [ ] **LT-27 Post-roll-only listing:** add `"quark:ancient_tome": {"remove": true}` to `loot_config.json`, `/reload`
  -> the log has "1 tuned item(s) can only be tuned after the roll ...: quark:ancient_tome" and `/loottuning` says
  "1 tuned items are post-roll only"; 20 rolls of `minecraft:chests/stronghold_library` never give a tome. Revert.

## 9. History

- **2026-08-25:** `dump_loot_tables.sh` extracted every jar's loot tables into `loot_table_dump/` (superseded).
- **2026-09-06:** LootJS scaffold: `global.LOOT` (`00_lib.js`), concern scripts `10`-`40`, `99_debug.js`, README
  with the LootJS 2.9.1 cheatsheet. Lists left empty; `/dumploot` already existed.
- **2026-09-25:** config-driven tuning: `gen_loot_config.py`, `loot_config.json`, `loot_index.json`,
  `05_tuning.js`, `/loottuning`. Pre-roll rewrite plus post-roll backstop. `L.reweight` and `groups.junk` deleted
  (no-ops). `@pneumaticcraft` migrated from `10_removals.js`. Tested in a node harness on 303 tables, 0 failures.
- **2026-09-26:** catalog regenerated; the config gained the Lightman coin regex and `@chalk` at 0.5.
- **2026-09-27:** first in-game evidence (12:00:42 log, 7 tables rewritten; the 12:08 export shows the new
  weights; LT-01 and LT-06 PASSED). Full examination:
  `tools/loot_census.py` added, this writeup written. Found the 8 items in 4.2. Confirmed in bytecode that LootJS
  types rolls by caller, that Forge skips `LootTableLoadEvent` for overridden vanilla tables, and that KubeJS's
  pre-roll edit doesn't trigger that.
- **2026-09-27:** `/cdevgen` (patcherMod `devtools/GeneratorRunner`) runs the loot generators from in game or at
  startup (`[debug.generators] runAtStartup`). BUILT, not in mods/ yet. Its first scratch run showed `loot_config.json`
  and `loot_index.json` are behind SimplyHouses.
- **2026-09-27:** `06_dead_maps.js` strips Monobank's ruined-portal explorer map from its 5 vault tables (ruined
  portals are removed, and the map search hung the dedicated server). Tested in node on the real tables; test ST-16 in
  structure_tuning.md.
- **2026-09-27 (evening):** script rules. `05_tuning.js` got a `global.lootTuning` API (`frequency`, `remove`, `ban`,
  `pack`, `packRate`, `packBoost`, `packExclude`), `#tag` targets (captured from the reload's tag event by
  `95_tag_capture.js`), loud ban alarms and the packed-food pre-roll pass. `07_rules.js` holds the rules: bans on
  PneumaticCraft and Lightman's coins (their JSON patterns moved here), the `#creativeengineering:loot/chalk` meta tag
  at 0.5 (replaces `@chalk`), 16 packed-food pairs at rate 0.1 with villages x3. PneumaticCraft `enable_dungeon_loot`
  and Lightman `[loot.chests] enabled` set to false (finding 6). Catalog regenerated (finding 1; 337 tables, 584 items;
  HopoBetterUnderwaterRuins is new). `tools/loot_tuning_sim.js` added; it caught a packed-weight indexing bug before
  deploy. Tests LT-14 to LT-20.
- **2026-09-27 (late):** framework finished; pre-roll is the default for everything a table can express.
  `05_tuning.js` gained table-scoped rules (`tables`), boosts (`frequency > 1`), `add`, `companion`, `addPool`,
  `restoreInjection`, `useVanilla` and `alias`. It also lists tuned post-roll-only items and writes
  `loot_runtime_additions.json`. Packed food became a split of the loose entry (user's call: low roll loose, high
  roll crates, highest roll most crates; nothing else in the pool moves). Generator: template-placed tables seed the
  scope and count as roots (findings 2, 7), code-injected entries are diffed into the catalog as `post_roll_only`
  (finding 3), overridden tables carry vanilla's copy, stale exports are skipped, and the template scan is cached.
  Finding 4: a bytecode audit of every jar with a `LootTableLoadEvent` handler found Chalk, Botania and CC:Tweaked
  as the only village injectors lost to goldenfood. The 16 tables went back to vanilla with those 3 pools restored;
  golden food is x2 (`#creativeengineering:loot/golden_food`), and the Golden Food book sits beside it and at 1% in
  114 village/camp/outpost tables. Finding 5: 12 aliases. The 10-40 LootJS scripts are rescoped to LootJS-only edits.
  Census seeds its scope the same way (0 tables outside). The sim caught two problems before deploy: the book landing
  in the outpost's crossbow pool (`add` now picks the main pool) and a companion diluting the igloo's guaranteed
  golden apple (anchor-only pools skipped). Section 4.1 now spells out KubeJS vs LootJS strengths and weaknesses.
  Tests LT-21 to LT-27; LT-02/10/11/13/14/16/17/20 updated.
- **2026-10-02** Refresh after the mods added since 09-27 (Exposure backport + Expanded, Corn Delight, Sky Villages, It
  Takes A Pillage, Villages & Pillages, Unwrecked Ships, watchtowers, Improved Pillager Outpost and others; The
  Missing Villages removed). `/dumploot` re-run via `/dumpall`, `gen_loot_config.py` regenerated (383 tables, 659
  items; new: corn, Exposure films/filters/photographs, Upgrade Aquatic fish, Neapolitan treats), census and container
  audit re-run (3,874 template containers; the one new unconverted kind is Unwrecked Ships' trap dispensers). Sim and
  scratch server agree: `4 rules -> rewrote 184 tables`. Section 2.1 totals and the 2.3 table refreshed; 2.4 to 2.9
  still read as of 09-27 (the census has the current lists).
- **2026-10-03:** TODO 1: the Golden Food enchantment book is in no loot table (companion and village/camp `L.add` rules
  removed from `07_rules.js`; `tools/loot_tuning_sim.js` now fails if it shows up anywhere). Sim: `rewrote 128 tables`,
  all checks passed. TODO 4 list: [loot_islands.md](loot_islands.md), analysis only.
- **2026-10-03:** loot catalog: `tools/loot_breakdown.py` writes [loot_catalog/](loot_catalog/README.md): every container table
  with rolls, entries (id, count, weight, chance, functions), structures and templates; hand-placed containers
  (no table) with positions and fixed contents; a by-structure index; dead references (11 missing tables, 17 containers
  that always open empty). Reference only, nothing applied.

## Owner ideas (scratchpad 2026-10-26)

- Seed, crate and raw food loot tables by biome: `loot/tables #14` (see also the Pale and Dark seed rows, `loot/tables #13`).
