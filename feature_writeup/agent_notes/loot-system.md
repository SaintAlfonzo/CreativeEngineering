# loot-system

> LootJS 2.9.1 scaffold (global.LOOT), pre-roll 05_tuning/07_rules, loot_catalog via loot_breakdown.py

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Merged 2026-10-19 from 3 memories (index compaction).

## loot-overhaul-lootjs

The pack-wide **chest loot overhaul** (cut unwanted items, redistribute, place in specific
tables, tune frequency, vanilla + ~320 mods) is built on **LootJS** — `mods/lootjs-forge-1.19.2-2.9.1.jar`,
already installed, hard-depends on KubeJS. That mod *is* "the kubejs loot integration"; no
"Loot Integration(s)" mod needed.

**Why LootJS, not Loot Integration(s) or datapack JSON:** the user wants surgical per-item /
per-table control at scale; Loot Integration(s) only bulk-swaps whole modded structure tables
(per-mod config surface, update risk). LootJS does all four goals from `/reload`-able scripts,
targets by exact id / `/regex/` / loot-type / tag, and runs *after* every mod's Forge GLMs so
it edits the merged result. Datapack JSON = ~340 tables, no "remove X everywhere", not reloadable.

**Writeup: `feature_writeup/loot.md`** (census, tools, LT- tests). Scripts 10-40 still hold only examples as of 2026-09-27.

**How to apply / where things are** (scaffold committed 2026-09-06, **content lists still empty**):
- `kubejs/server_scripts/loot/README.md` — full workflow + the LootJS **2.9.1 (2.x API)**
  cheatsheet + landscape facts. Read it first.
- **2026-09-25: global remove / frequency now live in [loot-system](loot-system.md)** (loot_config.json,
  pre-roll weight rewrite). `L.reweight` + `groups.junk` were deleted: LootJS 2.x is post-roll only,
  `replaceLoot`'s 3rd arg is `preserveCount`, so reweight never changed any frequency.
- `kubejs/server_scripts/loot/00_lib.js` — **all of `global.LOOT`**: `strict` flag, id
  validation (`_checkToken`/`_cleanTokens`), the `groups` / `tiers` lists, and the wrappers
  `chestType(event)` `tables(event,filter)` `entry(spec)` `addPool` `capCount` `remove`. Kept in ONE server script (not startup) — `global` IS shared startup↔server in
  this KubeJS, but `/reload` does NOT re-run startup scripts, so a startup helper is dead
  until a full relaunch. Server-side = every `/reload` rebuilds it.
- `10_removals.js` / `20_frequency.js` / `30_redistribute.js` / `40_targeted.js` — one
  commented example each, inert until filled. `99_debug.js` — `event.enableLogging()` toggle.
- **First real step:** run `/dumploot` (existing cmd, `kubejs/server_scripts/_lib/loot_dump.js`)
  in a test world → `kubejs/exported/loot_tables/<ns>.json` → build the lists from it. The export is
  tables as loaded (datapacks + pools mods add in code via LootTableLoadEvent) — NOT GLM output.

**Key gotchas (also in README):**
- Filter on loot **type** (`LootJS.LootType.CHEST`), not path — ~250 modded container tables
  live outside `.../chests/` (Terralith, The Missing Villages, MES, Towns & Towers, Philip's
  Ruins uses singular `chest/`, YUNG's Better Dungeons nests `<dungeon>/chests/`).
- LootJS runs after Forge GLMs — sees/edits Farmers Delight (edit `farmersdelight:chests/fd_*`
  for its content), armortrims, everythingcopper, simplehats, Lightman's `coins_in_chests`,
  Botania `botania:loot_tables/inject/*`.
- **Lootr** fills each container per-player at open time (not chunk gen) — a `/reload` reaches
  chests a player hasn't opened yet; test in fresh chunks. Lootr edits still apply because it
  reads the final merged table.
- `LootEntry.withChance(int)` is a misnamed alias of `withWeight` — use `.when(c => c.randomChance(f))`.

**Out of scope for LootJS — mod-config knobs:** structure spawn frequency (YUNG's / Philip's
Ruins `"Enable Configs"=true` / Towns & Towers / Wares `.toml`), Lightman's coin tiers
(`lightmanscurrency-common.lcconfig [loot]`), Domestication Innovation book injects, Allurement
horse-armor list, BetterDungeons chest count. Listed in README.

Approved plan: `/home/[REDACTED]/.claude/plans/id-like-to-implement-purring-lamport.md` (opaque name;
this memory is the durable pointer). Related helper pattern: memory `item-dict-mechanics`, and the
`global.NCH` recipe helper in `neoCraftHelper.js` that `global.LOOT` mirrors.

## chest-loot-tuning

**Full reference, census, findings and LT- tests: `feature_writeup/loot.md`** (2026-09-27). This memory keeps gotchas.

Built 2026-09-25. Pack-wide "remove item X from every chest" and "X shows up f× as often" knobs.
**Pre-roll runs in game** (2026-09-27 12:00 log: 3 rules -> 7 tables, 36 entries removed; the 12:08
/dumploot export shows the rewritten weights). The Rhino pre-roll risk points all worked. Post-roll
(`ItemFilter.custom`, `stack.id`) is still untested. The 2026-09-25 node harness wasn't saved.

**2026-09-27 findings (not fixed, user to decide):** index stale (SimplyHouses + the_dirty_stuff added);
the scope rule misses 39 container tables (untyped outside chests/, IE engineers_house declared
minecraft:empty); entries injected INTO existing pools (Quark, Buzzier Bees, NHA) aren't cataloged;
goldenfood overrides 16 vanilla village chests. `tools/loot_census.py` (reuses the generator's loaders
+ audit_loot_containers.scan) reports all of this.

**Verified in bytecode 2026-09-27:** (1) Forge fires LootTableLoadEvent ONLY when the winning copy's
sourcePackId is "Default" (vanilla). Any jar or kubejs/data override of a vanilla table silently kills
every mod's code injection into it. KubeJS genericLootTables edits do NOT (it redirects the map
forEach; the vanilla lambda still reads the real pack). (2) LootJS LootType comes from the param set the
CALLER passes to LootContext.Builder.create; containers, Lootr and `/loot give @s loot <table>` all
pass CHEST, whatever the table's declared type. `/loot give` is the quick test path.

**Files:** `tools/gen_loot_config.py` → `kubejs/server_scripts/loot/loot_config.json` (editable:
per item `remove`/`frequency` + read-only `chests`/`where` %; `patterns` `@modid`//regex/ with
`except`; `settings.enabled/slack/log_tables`) + `loot_index.json` (generated: per item `edit`
tables + `reach` set or `"*"`, table types). Runtime `loot/05_tuning.js` (IIFE, `global.lootTuning`).

**Why pre-roll via KubeJS, not LootJS:** LootJS 2.x is a GLM — every action is post-roll, it can
never change a weight (the old `L.reweight` helper was a no-op and was deleted). KubeJS's
`ServerEvents.genericLootTables` → `event.modify(id, b => ...)` exposes the raw JSON (`b.pools` is
the live Gson array) before parsing and re-runs on /reload. Gotchas: `modify` forces `b.type` to
`minecraft:generic` (restore from index); on a nonexistent id it creates an empty table (throw
inside the callback to abort); the event's `lootMap` is private and reflection is class-filtered, so
table ids must come from the offline index.

**Semantics:** tuned entry's per-roll probability becomes exactly f × old; slack `redistribute`
(default, others in the pool keep ratios, chests stay full) or `empty`. Pools never dropped (mods
index pools by name/position). Pools with composites / expand:true tags fall back to drop-entry /
random_chance condition. Post-roll LootJS strips removed items everywhere and thins reduced ones
only where the item's `reach` doesn't cover the rolled table (no double reduction).

**Generator facts:** raw JSON from vanilla -extra jar + mods + kubejs/data (no chest table is
shipped by two mod jars, but goldenfood overrides vanilla's village chests); /dumploot export used ONLY for pools mods add in code (export pools past raw pool
count — so a post-tuning export is fine); GLMs parsed from active global_loot_modifiers entries:
`lootTable`/`loot_table` field = sub-table edge (FD, oldguns), `item`/`addition` = injected item;
armortrims `chance` is integer percent via nextIntBetweenInclusive(0,100)<=chance. Lightman
coin tables are code-built (`type minecraft:empty`, config-driven entries) — out of scope.
Initial config seeds `patterns: {"@pneumaticcraft": {remove: true}}` (migrated from 10_removals.js).

**2026-09-27 late: framework finished** (writeup 3.2-3.8, 4.1 KubeJS-vs-LootJS, LT-14..27; sim-tested only). `05_tuning.js` (`// priority: 100`) = `global.lootTuning` pre-roll API: frequency (f>1 boosts,
`tables` scope), remove, ban (LOUD: switch check / foreign table / post-roll), pack (SPLIT of the loose entry, user's
call: low roll loose, high roll crates, highest most crates; nothing else in pool moves; cap 0.9), companion, add
(main pool = most item entries), addPool, restoreInjection (only where index.overridden says vanilla was replaced),
useVanilla, alias. Per-table order: alias, vanilla, pools, add, pack/companion, tuning. Removed packed/added items
switch themselves off. Writes kubejs/exported/loot_runtime_additions.json so the generator's /dumploot diff ignores
the rules' own pools/items. Rules live in `07_rules.js`; tags via `95_tag_capture.js` (`// priority: -1000`) +
tags/loottags.js (chalk, golden_food).

**User preference:** pre-roll removals/edits first; LootJS only when unachievable (GLM-added / code-injected items,
structure/biome/player scoping). The 10-40 scripts now only hold LootJS-only examples.

**goldenfood (finding 4):** its 16 village overrides only add a gf_enchantment book but kill Forge injections;
bytecode audit of all LootTableLoadEvent jars: only Chalk, Botania, CC:Tweaked inject there (Dustrial off). Fixed by
useVanilla + restoreInjection, not jar edits (pack manifest re-downloads jars). Golden food x2 + book companion +
1% in village/camp/outpost. Generator now: template-placed tables seed scope + are roots (376 tables), injected
entries -> catalog `post_roll_only`, stale exports skipped, template scan cached in tools/.cache/ (gitignored).
Offline: `node tools/loot_tuning_sim.js` ("4 rules -> rewrote 204 tables", all checks pass).

Related: [loot-system](loot-system.md) (the LootJS scaffold this sits beside), [structure-gen-framework](structure-gen-framework.md)
(same generator → editable JSON → apply pattern).

## loot-catalog

Built 2026-10-03 at the user's request ("detailed breakdown of the loottables... eventual goal is to make my own loot tables to replace every naturally spawning container's table").

**Files:** `tools/loot_breakdown.py` (generator, reuses gen_loot_config loaders + loot_census.chest_scope) and `tools/loot_breakdown_scan.py` (template scan, cached in tools/.cache/template_scan.json, ~35 s cold). Output `feature_writeup/loot_catalog/`: README.md (index, dead refs), handplaced.md (A chest-like, B functional, C already converted to chests/fixed/*), by_structure.md, tables/<ns>.md (383 tables, 47 namespaces; vanilla in minecraft.md).

**Key facts found:**
- 963 containers hold fixed items with no table (435 dispensers in jungle temples etc.); 164 chest-like. 151 more were converted by lootr_fixed_containers.py (only kubejs/data ports).
- 11 dead table ids: 17 containers roll a table that doesn't exist (always empty): kaisyn forest_ruins x4, simply_houses taiga_inn (luna_structures: id) x4, terralith desert/giant_bee_hive, minecraft village_leatherworker (vanilla is village_tannery).
- 29 tables are unused as installed (Wares and Old Guns are flagged non-container instead) (all of MES, 3 skyvillages, 7 terralith, library_md...).
- Structure resolution: jigsaw walk limited to the structure's `size` (without it a house with a street jigsaw reaches the whole village). Philip's Ruins ships // comments in JSON (loads_lenient). Vanilla start pools hardcoded.
- Tables are AS SHIPPED, before the pack's pre-roll rules; code-added pools/entries come from the /dumploot export (weights approximate).

**How to apply:** re-run `python3 tools/loot_breakdown.py` after mod updates or a fresh /dumploot. Next step is the user writing/refining their own tables; loot.md section 7 TODO (tree-like tag structure, island folding) is where that goes. Not registered in tools/generators.json. Related: [loot-system](loot-system.md), [loot-system](loot-system.md), memory `feature-writeup-pattern`.
