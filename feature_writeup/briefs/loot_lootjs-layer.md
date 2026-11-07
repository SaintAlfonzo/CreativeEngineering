# Brief: loot / lootjs-layer

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# loot`, Sub `lootjs-layer`. Reference writeups:
[loot.md](../loot.md) (3.4 LootJS scripts, 3.5 tools, 4.2 gap 8, 6 offline checks),
[kubejs/server_scripts/loot/README.md](../../kubejs/server_scripts/loot/README.md) (API + LootJS cheatsheet),
[exposure.md](../exposure.md) 4. Code: [00_lib.js](../../kubejs/server_scripts/loot/00_lib.js),
[05_tuning.js](../../kubejs/server_scripts/loot/05_tuning.js), [07_rules.js](../../kubejs/server_scripts/loot/07_rules.js),
`10_removals.js` to `40_targeted.js`, `99_debug.js`; tools `gen_loot_config.py`, `loot_census.py`,
`audit_loot_containers.py`, `loot_breakdown.py`, `loot_tuning_sim.js`; `tools/generators.json` (`/cdevgen`).

## Scope and order

Open rows: #1 (In Progress), #2, #3 (Planned). No Testing / Complete rows in this sub.

**State on 2026-10-21 that changes the rows** (checked against the repo and the jars):

- **#2 is half done.** `dump_loot_tables.sh` was deleted from git in commit `d0d572c` (2026-09-28, "First Push");
  `loot_table_dump/` was never tracked (the root `.gitignore` ignores everything not allow-listed). What is left: the
  stale allow-list line `!/dump_loot_tables.sh` at [.gitignore](../../.gitignore) line 16, and whatever local copies
  sit in the user's instance folder. loot.md 0 / 4.2 / 5 corrected.
- **#3's reason is stale, the row itself is not.** The census was regenerated 2026-10-02 and lists the Exposure port
  (`exposure` 6 tables, `exposure_expanded` 21, source `exposureBackport-1.9.19-ce-19.2-02`), not Exposure 1.6.0.
  But the census, `loot_index.json` and `loot_config.json` (all committed 2026-10-03, `d993d01`) predate the mods added
  in 1.0.4 to 1.0.7. Comparing the census with the 2026-10-14 catalog ([loot_catalog/README.md](../loot_catalog/README.md)):
  `subterrestrial` (13 container tables), `underground_bunkers` (3) and `eugene` (5, the camps_castles_carriages
  port in `kubejs/data`) are in the catalog and **absent from the census and from `loot_index.json`** (0 hits for
  each namespace). So the pre-roll rules (chalk x0.5, golden food x2, packed food, PneumaticCraft / coin bans
  pre-roll) do not rewrite those 21 tables; bans still catch them post-roll (LootJS strip + banner).
- **#1 has less to do than the row suggests.** `L.add(tables, item, { conditions })` in 05_tuning.js (lines 481-507,
  `itemEntry` 777-784) already writes raw loot conditions onto an added entry, so place-scoped additions
  (`minecraft:location_check` on biome, structure or y) can be pre-roll with exact odds. LootJS is only needed for
  stack caps, per-player logic and rolls the index can't see.

**Build groups and order:**

1. **#3 first** (on the user's machine, needs `mods/`): regenerate index, catalog, census, audit and loot catalog in
   one pass. Every `loot/tables` row (#1 tree, #3 chalk, #4 crates, #5 cans, #12 azalea) and `loot/lootr #1` read
   these files, so stale inputs would mislead them.
2. **#1** (decision + small cleanup + the in-game check of the post-roll pass) right after, because `tables #3/#4/#5/#12`
   need to know whether "underground / lush caves" goes pre-roll or LootJS.
3. **#2** whenever the user says so (deletion is the user's call): one `.gitignore` line + a local folder.

---

## lootjs-layer #3: Loot census stale
Status Planned / Work tool / `loot/lootjs-layer #3`

### Goal
Census, tuning index and catalog describe the current mod list again, so the pre-roll rules reach every container
table and the loot writeups' numbers are true.

### Now
- [loot_census.md](../../kubejs/server_scripts/loot/loot_census.md) header: "Generated ... on 2026-10-02"; 12,420
  tables, 384 in chest scope, 290 roots. `loot_index.json` / `loot_config.json`: generated 2026-10-02, committed
  2026-10-03 (`d993d01`), 383 tables, 659 items.
- [loot_catalog/](../loot_catalog/README.md) (from `tools/loot_breakdown.py`, 2026-10-14): 405 container tables in
  50 namespaces. It already has the new namespaces; it is the closest thing to current.
- [loot_container_audit.md](../../kubejs/generation_scripts/worldgen/loot_container_audit.md): 2026-10-04 (`334c76b`),
  so it also predates Subterrestrial / Underground Bunkers / Campsite Structures / Volcanic Caverns / the `eugene` port.
- Mods with structures or loot added since 2026-10-02 (from [DEPLOY_LOG.md](../../deploy/DEPLOY_LOG.md)): 1.0.4
  Fruits Delight, Improved Pillager Outpost, Just a Well; 1.0.6 Campsite Structures, Subterrestrial, Underground
  Bunkers, Volcanic Caverns (and the `eugene` datapack port); 1.0.7 Create: The Salt; exposureBackport `-02` -> `-04`.
- `tools/generators.json` has `loot_config` (needs `/dumploot` + `/dumptags item`), `loot_container_audit` and
  `loot_census` (group `loot`), so `/cdevgen run loot dumps` in game runs all three. `loot_breakdown.py` is **not**
  registered there.
- All these tools read `mods/*.jar` from the instance folder (`INSTANCE = tools/..`); the repo clone has no jars, so
  they can't run in a cloud session.

### Build
1. In game (dev instance, a world with no players needed): `/cdevgen run loot dumps`. It runs `/dumploot`,
   `/dumptags item`, `gen_loot_config.py`, `audit_loot_containers.py` and `loot_census.py`, and reports changed files
   (log `logs/cdevgen/<id>.log`).
2. `python3 tools/loot_breakdown.py` (catalog). Optional: add it to `tools/generators.json` as
   `{"id": "loot_breakdown", "script": "tools/loot_breakdown.py", "groups": ["reports", "loot", "slow"], "dumps": ["dumploot"], "outputs": ["feature_writeup/loot_catalog/README.md"], "apply": "report only"}`
   so step 1 covers it next time.
3. `node tools/loot_tuning_sim.js` -> "all checks passed"; note the new `rewrote N tables` count.
4. `/reload` (or restart) and read the `[loot-tuning]` line in `logs/kubejs/server.log`.
5. Writeups: loot.md 0 (Catalog + index row, Census row), 2.1 totals, 2.3 per-mod table, 6 counts; exposure.md 4
   ("Stale loot census" bullet: drop it once regenerated); loot_catalog is regenerated by step 2.
6. Commit the generated files (`loot_config.json` keeps hand edits; the generator never touches `settings` /
   `patterns`).

### Ids and data
- New container tables expected in scope (from the catalog, verified there): `subterrestrial:*` 13,
  `underground_bunkers:*` 3, `eugene:chests/{carriage_chest,piglin_camp_chest,trader_camp_chest,undead_castle_chest,watch_tower_chest}`.
- Census "stale export files" will grow if any namespace vanished; the dump never deletes files.

### Decisions
- Regenerate catalog (`loot_breakdown.py`) in the same pass? **Yes**: the `tables` rows plan off it.

### Gotchas
- Run after the eugene fixed-container conversion (`loot/lootr #1` step 1) if that lands first, or re-run: it adds
  `eugene:chests/fixed/*` tables.
- Three tables can't be serialised by `/dumploot` (keg, kettle, wooden bucket block tables): expected warnings.

### Tests
- **LT-28 index current:** after the regeneration, `python3 tools/loot_census.py` reports 0 "chest tables missing
  from loot_index.json" and 0 "container tables outside the tuning scope"; `grep -c '"subterrestrial:' loot_index.json` > 0.
- **LT-29 new tables tuned:** `/loot give @s loot subterrestrial:<a chest table with chalk>` x20 -> chalk at about
  half its jar rate (compare with `node tools/loot_tuning_sim.js subterrestrial:<id>`).

---

## lootjs-layer #1: LootJS-only scripts
Status In Progress / Work KubeJS / `loot/lootjs-layer #1`

### Goal
Decide what the LootJS layer is for, fill only what has a user, and prove the post-roll half works in game.

### Now
- [00_lib.js](../../kubejs/server_scripts/loot/00_lib.js) lines 20-139: `global.LOOT` with `chestType`, `tables`,
  `entry`, `addPool`, `capCount`, `remove`, `strict: true`. `groups` (lines 29-34: `foodJunk`, `redundantTools`,
  `sherds`, `musicDiscs`) and `tiers` (41-46: `common` .. `treasure`) are empty arrays. Nothing outside `10`-`40` reads
  them (grep).
- `10_removals.js`, `20_frequency.js`, `30_redistribute.js`, `40_targeted.js`: two commented examples each, with
  literal ids. `99_debug.js`: `event.enableLogging()` commented.
- The post-roll pass that matters is in [05_tuning.js](../../kubejs/server_scripts/loot/05_tuning.js) line 1304
  (`LootJS.modifiers`, bans + `post_roll_only` items). loot.md 0: "DEPLOYED; nothing logged or tested yet".
- `todo/kubejs_todo.txt` 15-22 and `todo/mod_todo.txt` 276 still say `10_removals.js` has a live PneumaticCraft rule;
  it doesn't (the ban moved to 07_rules.js line 29). Those are old scan notes, not writeups.
- Verified in `lootjs-forge-1.19.2-2.9.1.jar`: the builder conditions are `anyBiome`, `anyStructure`,
  `anyDimension`, `lightLevel`, `randomChance`, `matchLoot`, `customCondition(json)`, `playerPredicate`,
  `blockEntityPredicate`, `not/or/and` (no y-level condition; use `customCondition` with `minecraft:location_check`
  or `.apply(ctx => ctx.getBlockPos().y ...)`). The loot type comes from the param set the caller builds
  (`LootContextBuilderMixin`, `PSETS_TO_TYPE`).
- Verified in `lootr-forge-1.19-0.4.29.76.jar`: Lootr's chest, barrel and shulker fills use
  `LootContextParamSets.CHEST` with `ORIGIN` = the container and `THIS_ENTITY` = the opener
  (`LootrChestBlockEntity` ~line 278), so `LootType.CHEST`, biome / structure conditions and player predicates all
  see Lootr rolls.

### Build
1. **Decision first (D1).** Recommended split, to be written into loot.md 3.4 and the 10-40 headers:
   - Item additions or boosts scoped by **biome, structure or depth** -> pre-roll `L.add(..., { conditions })` in
     07_rules.js. Example for `tables #12`:
     ```js
     L.add(/./, i.azalea, { share: 0.02, conditions: [
       { condition: 'minecraft:location_check', predicate: { biome: 'minecraft:lush_caves' } } ] })
     ```
     and for "underground" (`tables #4/#5`): `predicate: { position: { y: { max: 40 } } }` (a failed condition drops
     the entry from the draw; the rest of the pool keeps its odds).
   - LootJS (10-40) only for: stack caps (`capCount`), per-player gates, items a GLM adds (not in any table JSON).
2. **Trim 00_lib.js:** delete `groups` (pack-wide removals live in `loot_config.json` / 07_rules.js, as its own
   comment says) or keep it with a one-line "unused until a 10-40 rule needs it". Keep `tiers` only if D1 keeps a
   LootJS addition; fill entries with `global.item` keys (`{ item: i.x, weight, count, chance }`), never literal ids
   (CLAUDE.md).
3. **Switch the examples to dict keys** in 10-40 (`i.rotten_flesh` style) so a copy-paste follows the convention.
4. **Prove the post-roll pass** (the part that is live): see LT-30 / LT-31. Uncomment `99_debug.js` only for the test.
5. Row closes as Complete (scaffolding by design) once D1 is written up and LT-30/31 pass; place-scoped content is
   then built inside the `tables` rows.

### Ids and data
- Biome ids: `minecraft:lush_caves`, `minecraft:dripstone_caves`, `minecraft:deep_dark` (vanilla, verified names).
- Dict keys: check `global.item` has `azalea`, `flowering_azalea` (used in wandering_trades.js, so yes); leaf
  carpet / leaf layer ids for `tables #12` belong to that row.

### Decisions
- **D1 pre-roll conditions vs LootJS for place-scoped loot:** recommend pre-roll (exact, visible to
  `loot_tuning_sim.js`, one code path). Caveat: only tables in `loot_index.json` scope get the entry, so #3 first.
- **"Underground" definition:** y bound (`position.y.max`, catches every buried chest) vs a table list
  (mineshaft, dungeons, strongholds, ancient city, `subterrestrial:*`, `underground_bunkers:*`, `betterdungeons:*`).
  Recommend the table list for big boosts (designer intent, no surprises in tall structures) and the y bound only for
  a small generic bonus.

### Gotchas
- `LootEntry.withChance(int)` is an alias of `withWeight` (README); use `.when(c => c.randomChance(f))`.
- A post-roll removal leaves an empty slot; never use it for something pre-roll can do.
- Lootr rolls on first open per player: test in fresh chunks or with `/loot give`.
- `location_check` in a pre-roll entry is evaluated against `ORIGIN`; `/loot give @s loot <table>` uses the command
  position, so stand in the biome / depth to test.

### Tests
- **LT-30 post-roll ban:** with `99_debug.js` logging on, `/loot give @s loot pneumaticcraft:chests/<table>` (or any
  table the census lists with a PNC injection) -> no PneumaticCraft item; `[loot-tuning]` / `[LootJS]` lines show the
  strip.
- **LT-31 Lootr context:** open a fresh Lootr chest with logging on -> `[LootJS]` line shows `LootType CHEST` and a
  position (proves biome / structure conditions will work on Lootr rolls).

---

## lootjs-layer #2: Legacy loot dump
Status In Progress (script and dump deleted; `.gitignore` line left) / Work none / `loot/lootjs-layer #2`

### Goal
No trace of the 2026-08-25 dump script or its 40 MB output.

### Now
- Script: deleted in `d0d572c` (recoverable with `git show 609cd21:dump_loot_tables.sh`).
- [.gitignore](../../.gitignore) line 16 `!/dump_loot_tables.sh` still allow-lists it.
- `loot_table_dump/`: never in git; lives (if at all) in the instance folder on the user's machine.
- Superseded by `/dumploot` (`_lib/loot_dump.js`) and `tools/loot_breakdown.py`.

### Build
1. Remove line 16 from `.gitignore`.
2. User deletes `<instance>/loot_table_dump/` and `<instance>/dump_loot_tables.sh` locally (`rm -r` on their machine).
3. loot.md: 4.2 row 8 -> FIXED; drop the "Legacy, superseded" bullet in 5; finding 8 in 0 -> fixed.

### Decisions
- Delete: the row's own decision is "delete"; it is the user's call (loot.md 4.2). Recommend yes.

### Tests
None (housekeeping).

---

## Writeup corrections made

- [loot.md](../loot.md) 0 Findings: legacy dump line now says the script left the repo in `d0d572c` and only the local
  `loot_table_dump/` is open.
- [loot.md](../loot.md) 0 Census row: added that census / index / config are stale since the 1.0.4-1.0.7 mods
  (no Subterrestrial, Underground Bunkers, `eugene` tables).
- [loot.md](../loot.md) 4.2 row 8: the script was deleted in `d0d572c`; `.gitignore` still allow-lists it.
- [loot.md](../loot.md) 5: broken link `../dump_loot_tables.sh` replaced with the commit to recover it from.
- [loot.md](../loot.md) 7: handplaced inventory date 2026-10-03 -> regenerated 2026-10-14; the verse chest's
  template and that it holds a writable book.
- [exposure.md](../exposure.md) 4 "Stale loot census": the census already lists the port's tables (2026-10-02), not
  1.6.0's; it is stale for the mods added since.
