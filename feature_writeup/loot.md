# Loot: every loot table in the pack, and the tools to tune chest loot

Test prefix **LT-** · Last updated **2026-09-27** · Overall status **DEPLOYED, in-game test pending** (the pre-roll
rewrite is confirmed by the 2026-09-27 12:00 log and 12:08 export; everything else is untested in game)

## 0. At a glance

The pack has about 11,000 loot tables, but only about 370 matter for chest loot. The rest are block drops
(10,286) and mob drops (225), which nothing here touches. Chest loot is tuned in three layers, all reloaded by
`/reload`:

1. **The catalog** (`loot_config.json`): every item that can show up in a chest, with `remove` / `frequency`
   knobs, plus `@mod` and `/regex/` patterns. A Python generator builds it from the jars.
2. **The tuning runtime** (`05_tuning.js`): rewrites table weights before the roll (exact odds). A LootJS pass
   after the roll catches whatever the JSON pass can't see.
3. **Targeted LootJS scripts** (`10_removals.js` to `40_targeted.js`): per-table removals, additions, stack caps.
   The helpers are in place; the scripts hold only commented examples.

Next to those sit Lootr (per-player containers), the Lootr container swaps and structure frequency (both in
[structure_tuning.md](structure_tuning.md)), and about 20 mod configs with their own loot knobs.

| Part | Layer | Status |
|---|---|---|
| Catalog + index (`loot_config.json`, `loot_index.json`) | `tools/gen_loot_config.py` | BUILT 2026-09-26. **Stale:** 30 container tables added since (SimplyHouses 29, the_dirty_stuff 1) aren't indexed; see finding 1 |
| Pre-roll weight rewrite | KubeJS `ServerEvents.genericLootTables` in `05_tuning.js` | Runs in game: log 2026-09-27 12:00:42, `3 rules -> rewrote 7 tables (36 entries removed, 7 pools reweighted, 0 via conditions)`, which matches an offline replay exactly; the 12:08 `/dumploot` shows the halved chalk and emptied PNC tables (LT-01, LT-06 PASSED) |
| Post-roll backstop + `/loottuning` | LootJS `LootType.CHEST` modifier in `05_tuning.js` | DEPLOYED; nothing logged or tested yet |
| Targeted scripts + `global.LOOT` | LootJS, `00_lib.js`, `10`-`40`, `99_debug.js` | DEPLOYED as inert scaffolding (examples only) |
| `/dumploot` | `_lib/loot_dump.js` | Works; last run 2026-09-27 12:08 (10,865 tables) |
| Census (this writeup's numbers) | `tools/loot_census.py` -> `loot_census.md` | BUILT 2026-09-27 |
| Lootr container swaps | patcherMod | See [structure_tuning.md](structure_tuning.md) 3.6 / ST-14 |

**Findings to act on** (details in 4.2; nothing below has been changed yet):

1. **Stale index.** SimplyHouses (added 10:33 today) and the_dirty_stuff aren't in `loot_index.json`, so
   pre-roll rules can't touch their 30 tables and their items aren't in the catalog. Re-run the generator, ideally
   after fix 2.
2. **39 container tables sit outside the tuning scope** (129 of the 2,320 template containers). They are
   untyped and not under a `chests/` folder, or declared something other than `minecraft:chest`: Terralith
   village houses, The Missing Villages' jungle_sparse / swamp_mangrove sets, Monobank vaults, IE's engineer's
   house crates (declared `minecraft:empty`, 50 containers), Nullscape phantom nests. Post-roll rules still reach
   them, with empty-slot semantics; the catalog doesn't list them, and 24 of their items appear nowhere else.
3. **Items injected into existing pools aren't in the catalog:** Quark (ancient tome, blank rune, ancient sapling,
   Endermosh disc), Buzzier Bees' four-leaf clover, Netherite Horse Armor. Lightman's coins come from a GLM, so
   they're missing too. Rules still work on them post-roll (`@quark`, or an item key you add by hand).
4. **`goldenfood` replaces all 16 vanilla village chest tables, which silently drops every code injection into
   them.** Forge only fires `LootTableLoadEvent` for tables whose winning copy comes from the vanilla pack, so
   Botania's and Chalk's village pools are gone (the 09-26 index had code pools on 10 village tables; today's
   export has none). GLM additions (Farmer's Delight, Everything Copper) still apply.
5. **12 structure containers name loot tables that don't exist** and always generate empty (Terralith desert
   fisherman/leatherworker/shepherd, Towns & Towers forest-ruins set, and others; list in 2.8).
6. **Lightman's coins are spawned, then stripped.** `[loot.chests] enabled = true` in its config, while a
   `loot_config.json` regex removes the coins post-roll. `enabled = false` would say the same thing directly. The 9
   `kubejs/data` overrides of its entity/boss coin tables are inert while `[loot.entities] enabled = false`.
7. **Generator root rule:** after the re-run, 4 vanilla village tables that SimplyHouses borrows would stop
   counting as containers in the catalog's `where` view (display only; the runtime is unaffected).
8. Housekeeping: 7 stale `/dumploot` files from removed mods; the pre-generator `dump_loot_tables.sh` +
   `loot_table_dump/` (40 MB, 2026-08-25) are superseded; the node harness used to test the runtime on 09-25
   wasn't saved.

**Open first:** [loot_config.json](../kubejs/server_scripts/loot/loot_config.json) (the knobs),
[loot_census.md](../kubejs/server_scripts/loot/loot_census.md) (the full per-mod tables),
[loot/README.md](../kubejs/server_scripts/loot/README.md) (LootJS cheatsheet).

**Knobs:**
- Per item: `"remove": true` or `"frequency": 0..1` under `items` in `loot_config.json`.
- Per mod or id pattern: `patterns` -> `"@modid"` or `"/regex/"`, each with an optional `except` list.
- Globally: `settings.enabled`, `settings.slack` (`redistribute` | `empty`), `settings.log_tables`.
- Per table (add, cap, targeted remove): LootJS in `10`-`40` via `global.LOOT`.
- Whether a chest exists at all: structure frequency, house pool weights, mod configs ([structure_tuning.md](structure_tuning.md)).

**Applying:** `/reload`. Both stages rebuild. Test with `/loot give @s loot <table>`, which runs the same path as
a chest (tables, code injections, GLMs, LootJS). In the world, only containers nobody has opened yet change (Lootr rolls
per player on first open).

**Measuring:** `/loottuning`, the `[loot-tuning]` line in `logs/kubejs/server.log`, `/dumploot` (the export shows
the rewritten weights), `python3 tools/loot_census.py`.

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

## 2. Reference: the loot landscape (census 2026-09-27)

All numbers come from `python3 tools/loot_census.py` (sources: vanilla jar, `mods/*.jar`, `kubejs/data`, the
12:08 `/dumploot` export, and a scan of every structure template). The full per-namespace tables are in
[loot_census.md](../kubejs/server_scripts/loot/loot_census.md).

### 2.1 Totals

| | count |
|---|---:|
| Distinct loot tables (after overrides) | 11,010 |
| Namespaces with a table / sources shipping them | 133 / 132 (vanilla + 130 jars + `kubejs/data`) |
| Container tables (rolled by a container) | 305 |
| Chest sub-tables (pulled into a container table) | 64 |
| Block drops / mob drops / gameplay (fishing, gifts, bartering) | 10,286 / 225 / 43 |
| Config-driven (`minecraft:empty`, filled in code) / other | 10 / 77 |
| Live in game (`/dumploot`) / live-only (code-built, no JSON) / shipped but not live | 10,865 / 180 / 325 |
| Overridden by a higher-priority copy | 50 (16 of them vanilla village chests) |
| Active global loot modifiers | 122 (55 target chest tables by id, plus Lightman's coins by config) |
| Structure-template containers with a loot table | 2,320 |
| Items in the chest catalog | 541 (382 vanilla; `minecraft:emerald` is in the most tables, 101) |

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
| patcherMod | `patcherMod/src/main/resources/data/creativeengineering/loot_tables/` | empty folder |

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
| Vanilla (`minecraft`) | 43 | | `chests/`, `chests/village/` | Towns & Towers 463, vanilla 128, Quark 120, Better Dungeons 48, +12 mods | 16 village tables replaced by goldenfood; 19 get code pools |
| The Missing Villages | 47 | | `village/<biome>/` | 59 (own) | 21 outside tuning scope (untyped) |
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
| Code entries into existing pools | Quark (tome 5 tables, rune 4, sapling, Endermosh disc), Buzzier Bees (clover 2), Netherite Horse Armor (bastion treasure) | 9 vanilla chest tables | **no** (finding 3) |
| GLM rolling a sub-table | Farmer's Delight (14 vanilla chests -> `fd_*`), Old Guns (4 vanilla chests -> 6 tables), the_dirty_stuff (plains house -> tobacco chest) | | yes |
| GLM adding one item | Armor Trims templates (21 GLMs, percent chance; 4 more target 1.20 trail-ruins tables that don't exist), Everything Copper (8 items), End Portal (purpur eye), Geode+ (goat horn) | | yes, with the GLM's chance |
| GLM in code (opaque) | PneumaticCraft `dungeon_loot` (16 tables), Simple Hats (12), Lightman's `coins_in_chests` (config lists) | | no; post-roll rules only |

### 2.6 Overrides (same id shipped twice)

| Overridden | By | Effect |
|---|---|---|
| 16 `minecraft:chests/village/*` | goldenfood | its copies win, and code injections into them stop (finding 4) |
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

A JSON at `kubejs/data/<ns>/loot_tables/<path>.json` would fill them. It could hold a single `loot_table`
entry pointing at the nearest real table, for example the fortified fisherman for the desert one.

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
  "@pneumaticcraft": { "remove": true },                     // live
  "/^lightmanscurrency:coin(pile|block)?_(chocolate_)?(copper|iron|gold|emerald|diamond|netherite)$/": { "remove": true },  // live
  "@chalk": { "frequency": 0.5 },                            // live
  "/_sword$/": { "frequency": 0.5, "except": ["minecraft:diamond_sword"] }
},
"settings": { "enabled": true, "slack": "redistribute", "log_tables": false }
```

Rules the runtime rejects, with a warning: `frequency` above 1 (clamped to "no rule"; boosting belongs in
`30_redistribute.js`), negative values, and `#tag` patterns (tags aren't bound yet when loot tables load).

**Live today:** the three patterns above and no item edits. In game that rewrote 7 tables: 4 PneumaticCraft
tables lost all 36 entries, and the 3 Chalk sub-tables were reweighted. The Lightman rule has no pre-roll
target (coins come from a GLM) and acts post-roll only. PneumaticCraft's mechanic house is 100% PNC items, so
its chest now rolls nothing. That's moot while pool tuning keeps the house out of villages
([structure_tuning.md](structure_tuning.md) ST-12).

### 3.2 `05_tuning.js`: the runtime

**Pre-roll** (`ServerEvents.genericLootTables`, raw JSON before parsing). For each table `loot_index.json` lists
as holding a tuned item, every pool with a tuned entry gets new integer weights. The tuned entry's chance per
roll becomes exactly `f x` the old one. The lost share goes:

- `redistribute` (default): to the pool's untuned entries, keeping their ratios, so chests stay as full as before.
- `empty`: to an added `minecraft:empty` entry, so every other item keeps its exact odds.

Example: a pool of bread 10, apple 5, wheat 5 with bread at `frequency 0.5`. Bread goes from 50% to 25% of rolls.
`redistribute` gives bread 10, apple 15, wheat 15 (25% / 37.5% / 37.5%). `empty` gives bread 5, apple 5,
wheat 5, empty 5. Weights are scaled by the smallest multiple of the old total that keeps every share within 1%.
`quality` scales with the weight.

Pools are never dropped (mods look pools up by name or position). A pool left with no real entry becomes one
`empty` entry. Pools with composite entries or `expand: true` tags (4 Terralith spire pools) can't be reweighted
exactly. There a tuned item is dropped (remove) or gets a `random_chance` condition (frequency). `modify` forces
the table type to `minecraft:generic`, so the runtime puts back the type recorded in the index.

**Post-roll** (LootJS on every `LootType.CHEST` roll): a removed item's stacks are stripped, and the slot stays
empty. A reduced item is kept with probability `f`, unless the pre-roll pass already governs that item in the
rolled table (the index's `reach` list), so nothing is reduced twice. Items with `reach: "*"` (27, the ones
Cloud Storage balloons and pet shops can give) are never thinned post-roll.

**Tools:** `/loottuning` (op level 2) prints active/inactive, slack, the rule count, the pre-roll counts, and the
post-roll counts since the last reload. `settings.log_tables` logs one line per rewritten table. Everything is in
`global.lootTuning`. The script is an IIFE because all server scripts share one scope.

### 3.3 `tools/gen_loot_config.py`: the generator

Rebuilds `loot_config.json` + `loot_index.json` from the vanilla -extra jar, `mods/*.jar`, `kubejs/data`, active
GLMs, the `/dumploot` export (only to find pools added in code) and `/dumptags` (members of `expand` tags).

- **Scope:** tables declared `minecraft:chest`, plus untyped / generic tables under a `chests/` or `chest/`
  folder, plus everything those reach through `loot_table` entries, code pools and sub-table GLMs. It has 3 gaps:
  finding 2 (tables that match neither rule), finding 3 (entries injected into existing pools) and finding 7
  (a borrowed table stops counting as a root).
- **Index:** per item, `edit` (tables with a direct JSON entry, which pre-roll rewrites) and `reach` (every table
  whose rolls those edits already cover); per table, its declared type. The `report` section lists roots, code-rolled
  orphans, code-pool tables, GLM sub-tables, GLM items, opaque GLMs and missing references.
- **Safe re-runs:** keeps `remove` / `frequency` / any key you added, keeps edited items that vanished as
  `"missing": true`, never touches `settings` / `patterns`. Run it after mod changes, then `/reload`.

### 3.4 LootJS targeted scripts and `global.LOOT`

`00_lib.js` builds `global.LOOT` on every reload:

- **Targeting:** `chestType(event)` (every container roll), `tables(event, filter)` (an id, `/regex/`, or a list).
- **Entries:** `entry({item, weight, count, chance})`, `addPool(builder, specs)`.
- **Edits:** `capCount(builder, filter, max)`, `remove(builder, group)`.
- **Validation:** `strict: true` makes an unknown id or empty tag throw; `false` warns instead.
- **Reusable lists:** `groups` / `tiers`, all empty.

| Script | For | Contents today |
|---|---|---|
| `10_removals.js` | take an item out of specific tables only (post-roll, empty slot) | 2 commented examples |
| `20_frequency.js` | stack-size caps; chance that an added pool fires | 2 commented examples |
| `30_redistribute.js` | put items in more places (`anyStructure`, `anyBiome`, `anyDimension` scoping) | 3 commented examples |
| `40_targeted.js` | signature edits to one table; `disableLootModification` for hand-written overrides | 3 commented examples |
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
| Lightman's Currency | `config/lightmanscurrency-common.lcconfig` `[loot]` | tier items T1-T6; `[loot.chests] enabled = true` + per-tier table lists; `[loot.entities] enabled = false` |
| PneumaticCraft | `pneumaticcraft-common.toml` | `enable_dungeon_loot = true` (its GLM now rolls emptied tables) |
| Sophisticated Backpacks | `sophisticatedbackpacks-common.toml` | `chestLootEnabled = true` |
| Farmer's Delight | `farmersdelight-common.toml` | `generateFDChestLoot = true` |
| Alex's Mobs | `alexsmobs.toml` | `addLootToChests = true` |
| Domestication Innovation | `domestication-innovation.toml` `[loot]` | per-enchantment book chances (0.15 to 0.65), `pet_curse_enchantments_loot_only` |
| Allurement | `allurement-common.toml` | enchanted horse armor "Generates in loot tables", unenchanted table list |
| Quark | `quark-common.toml` | Ancient Tomes `Loot Tables` (table,weight list), Endermosh disc `Add To End City Loot` / weight 5, Ancient Wood `Ancient City Loot Weight` 8, Monster Box `Enable Extra Loot Table`, `Replace Worldgen Chests` |
| Netherite Horse Armor | `netherite_horse_armor-common.toml` | `weight = 8` in bastion treasure (0 disables) |
| Immersive Armors | `immersive_armors.json` | `lootChance = 0.025` |
| Dustrial Decor | `dustrial_decor-common.toml` | `loot_table_modification = false` |
| YUNG's Better Dungeons | `betterdungeons-forge-1_19.toml` | small dungeon min/max chest count 1-2 |
| YUNG's (item frames, armor stands, wishing wells) | `config/<mod>/forge-1_19/*.json` | see 2.9 |
| Monobank | `monobank-common.toml` | `StealingFromContainersIsACrime` (loot-table containers near protected structures) |

Remember the [configs_edited rule](../configs_edited) when changing any of these: copy the edited file there
too.

## 4. Limits and decisions

### 4.1 Decisions

- **Pre-roll rewrite via KubeJS, not LootJS.** LootJS 2.x only ever sees rolled stacks and can't change a weight.
  The old `L.reweight` helper was a no-op (`replaceLoot`'s third argument is `preserveCount`) and was deleted
  on 2026-09-25. `genericLootTables` exposes the raw JSON and re-runs on `/reload`.
- **LootJS for the rest.** LootJS runs after every GLM, so it can edit Farmer's Delight, Armor Trims and the
  other injected loot. The Loot Integrations mod was rejected: it only swaps whole modded structure tables, per mod.
  A datapack JSON per table was rejected too: about 340 files, no "remove X everywhere", and no reload.
- **No boosting in the catalog.** `frequency > 1` would need to steal share from other items; additions go in
  `30_redistribute.js`, where they're explicit.
- **No `#tag` patterns.** Tags aren't bound while loot tables load. Use `/regex/` or `@mod`.
- **The index comes from files, not the game.** The event's table map is private and reflection is class-filtered,
  so table ids come from the offline index. Hence "re-run the generator after mod changes".
- **KubeJS's pre-roll edit does not block injections.** Its mixin redirects the `forEach` in `LootTables.apply`,
  and vanilla's own lambda still reads the real source pack. Only file-level overrides (a jar or `kubejs/data`
  replacing a vanilla table) block `LootTableLoadEvent`.

### 4.2 Known gaps and their fixes (none applied yet)

| # | Gap | Effect | Proposed fix |
|---|---|---|---|
| 1 | Index predates SimplyHouses / the_dirty_stuff | pre-roll can't touch 30 tables; items missing from the catalog | re-run `gen_loot_config.py` (after fix 2) |
| 2 | Scope misses 39 container tables (129 containers) | no exact reweighting there, not in the catalog | seed the generator's scope with every table a template container names (reuse `audit_loot_containers.scan`, as the census does) |
| 3 | Entries injected into existing pools unseen | Quark / Buzzier Bees / NHA items missing from the catalog | have the generator diff each pool's entries against `/dumploot`, like it already does for pool counts, and list those items as post-roll-only |
| 4 | goldenfood blocks village injections | no Botania / Chalk loot in vanilla village chests | pick one: accept it; re-add the lost loot with LootJS in `30_redistribute.js`; or drop goldenfood's overrides (it's all-or-nothing per table) |
| 5 | 12 containers name missing tables | always empty | small `kubejs/data` tables pointing at a sibling table |
| 6 | Lightman coins spawned then stripped | works, but two systems fight | set `[loot.chests] enabled = false` and keep or drop the regex |
| 7 | Root rule drops borrowed vanilla tables | `where` view only | root = no parent, or placed by a template |
| 8 | Stale exports, legacy dump, no saved harness | clutter; runtime changes can't be re-checked offline | skip stale files by mtime in the generator; archive `dump_loot_tables.sh` / `loot_table_dump/`; save a `tools/loot_tuning_sim.js` |

### 4.3 Other limits

- A post-roll removal leaves an empty slot. Only the pre-roll path refills from the pool.
- Pre-roll exactness assumes the JSON pool is the whole pool. If a mod later slips an entry into the same pool
  (Quark's tome into `simple_dungeon`), the tuned share is slightly off.
- A tuned item that is only reachable from a `reach: "*"` table is never thinned post-roll.
- Mob drops, block drops, fishing and bartering are out of scope, even for the same item.
- Worlds: a `/reload` reaches only containers nobody has opened. Lootr containers roll per player at first open.

## 5. File map

- Config and data: [loot_config.json](../kubejs/server_scripts/loot/loot_config.json),
  [loot_index.json](../kubejs/server_scripts/loot/loot_index.json) (generated),
  [loot_census.md](../kubejs/server_scripts/loot/loot_census.md) (generated),
  [README.md](../kubejs/server_scripts/loot/README.md)
- Runtime: [05_tuning.js](../kubejs/server_scripts/loot/05_tuning.js), [00_lib.js](../kubejs/server_scripts/loot/00_lib.js),
  [10_removals.js](../kubejs/server_scripts/loot/10_removals.js), [20_frequency.js](../kubejs/server_scripts/loot/20_frequency.js),
  [30_redistribute.js](../kubejs/server_scripts/loot/30_redistribute.js), [40_targeted.js](../kubejs/server_scripts/loot/40_targeted.js),
  [99_debug.js](../kubejs/server_scripts/loot/99_debug.js), [_lib/loot_dump.js](../kubejs/server_scripts/_lib/loot_dump.js) (`/dumploot`)
- Tools: [gen_loot_config.py](../tools/gen_loot_config.py), [loot_census.py](../tools/loot_census.py),
  [audit_loot_containers.py](../tools/audit_loot_containers.py)
- Pack datapack overrides: [kubejs/data/lightmanscurrency/loot_tables/](../kubejs/data/lightmanscurrency/loot_tables/),
  [felsic_block.json](../kubejs/data/felsic_gear/loot_tables/blocks/felsic_block.json)
- Neighbours: [loot_container_swaps.json](../kubejs/generation_scripts/worldgen/loot_container_swaps.json),
  [loot_container_audit.md](../kubejs/generation_scripts/worldgen/loot_container_audit.md),
  [lootr-common.toml](../config/lootr-common.toml)
- Exports: `kubejs/exported/loot_tables/` (7 stale files: colony4cc, distantfriends, domum_ornamentum,
  minecolonies, multipiston, refinedcooking, refinedstorage)
- Legacy, superseded: [dump_loot_tables.sh](../dump_loot_tables.sh), `loot_table_dump/`

## 6. Offline checks

- `python3 tools/gen_loot_config.py` prints scope / GLM / item counts. Today's files report 301 tables and 236
  roots. A re-run would add SimplyHouses and the_dirty_stuff.
- `python3 tools/loot_census.py`: totals, index drift ("chest tables missing from loot_index.json"),
  out-of-scope tables, dead references. It should agree with section 2 until the next mod change.
- Replaying the three live rules against `loot_index.json` gives 7 tables, 36 PneumaticCraft entries removed and
  7 pools reweighted, the same as the 12:00 log line. After any `/dumploot`, compare a tuned table in the export
  with its jar copy to see the rewritten weights (LT-06). The node harness that tested all 303 tables on 2026-09-25
  wasn't kept, so a `tools/loot_tuning_sim.js` (like `structure_tuning_sim.js`) is the missing piece for checking
  runtime changes offline.
- `grep "\[loot-tuning\]" logs/kubejs/server.log` after any launch or `/reload`.

## 7. In-game test suite

Use `/loot give @s loot <table>` to roll a table on demand. It runs the whole chain (code pools, GLMs, LootJS).
Revert every temporary edit and `/reload` afterwards.

- [x] **LT-01 Pre-roll runs:** launch or `/reload` -> `logs/kubejs/server.log` has
  `[loot-tuning] 3 rules -> rewrote 7 tables (36 entries removed, 7 pools reweighted, 0 via conditions)`.
  PASSED 2026-09-27 (12:00:42 log).
- [ ] **LT-02 Status command:** `/loottuning` -> "active, slack=redistribute, 3 rules", "pre-roll: 7 tables
  rewritten, 36 entries removed, 7 pools reweighted, 0 via conditions", and a post-roll line.
- [ ] **LT-03 PNC removed pre-roll:** `/loot give @s loot pneumaticcraft:chests/mechanic_house` 5x -> nothing at
  all (the table was 100% PNC).
- [ ] **LT-04 PNC GLM path:** `/loot give @s loot minecraft:chests/simple_dungeon` 20x -> no PneumaticCraft items;
  vanilla dungeon loot, Quark tomes and runes and Botania items still show up.
- [ ] **LT-05 Lightman coins stripped post-roll:** during LT-04, no coins at all (simple_dungeon is in Lightman's T3
  list). `/loottuning` post-roll "stacks removed" is above 0.
- [x] **LT-06 Chalk halved:** `/dumploot`, then compare the chalk tables in `kubejs/exported/loot_tables/chalk.json`
  with the jar -> chalk's share of each pool is halved. PASSED 2026-09-27 (12:08 export): `village_chalk_loot` empty
  70 -> 175 with chalk weights kept (33% -> 17%), `dungeon_chalk_loot` chalk weights halved with empty 60 -> 75,
  `desert_pyramid_chalk_loot` empty 70 -> 181; the 4 PNC tables are a single `minecraft:empty` entry. The only
  untuned entry in these pools is their own `empty`, so `redistribute` fed it.
- [ ] **LT-07 Item remove:** add `"remove": true` to `minecraft:bread`, `/reload` -> log shows 4 rules and more
  tables; `/loot give @s loot minecraft:chests/village/village_plains_house` 20x -> never bread, chests not emptier.
- [ ] **LT-08 Pattern with except on injected items:** add `"@quark": {"remove": true, "except": ["quark:ancient_tome"]}`,
  `/reload` -> 30 rolls of `minecraft:chests/simple_dungeon` show ancient tomes but never blank runes.
- [ ] **LT-09 Slack modes:** set `minecraft:bread` to `"frequency": 0.5`, `/reload`, `/dumploot` -> in
  `minecraft.json`, `chests/village/village_plains_house` has no new `minecraft:empty` entry and the other items'
  weights grew. Then `"slack": "empty"`, `/reload`, `/dumploot` -> `/loottuning` says slack=empty, the table gained a
  `minecraft:empty` entry, and the other items' shares are unchanged.
- [ ] **LT-10 Kill switch:** `"enabled": false`, `/reload` -> `/loottuning` says inactive; LT-03 gives PNC items again.
- [ ] **LT-11 Targeted script:** uncomment the simple_dungeon rotten flesh example in `10_removals.js` and
  `enableLogging()` in `99_debug.js`, `/reload` -> 20 rolls without rotten flesh; `logs/latest.log` has `[LootJS]`
  lines naming `CE/loot/removals/simple_dungeon_flesh`.
- [ ] **LT-12 Lootr timing:** with LT-07's rule active, open a never-opened village chest in fresh chunks -> no bread.
  A chest you opened before the `/reload` keeps its old contents.
- [ ] **LT-13 Out-of-scope table still tuned post-roll:** add `"immersiveengineering:coal_coke": {"remove": true}`,
  `/reload` -> 20 rolls of `immersiveengineering:chests/engineers_house` never give coal coke. After fix 2 this
  becomes a pre-roll edit.

## 8. History

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
