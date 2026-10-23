# Brief: structures / metas

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# structures`, rows `structures/metas` #1 to #5 (open) and
#6 (Testing). Reference: [structure_tuning.md](../structure_tuning.md) 3.16 (meta structures: anchors, shares,
parts, `/locate`), 3.22 (priority, companions, peaceful radius), 7.4.5 (`vehicles`), 7.4.9 (caravans), 7.5
(`avoidCoasts`); the field list is the `_readme` of
[meta_structures.json](../../kubejs/generation_scripts/worldgen/meta_structures.json).

Verified against jars / files (2026-10-21): Towns & Towers 1.10 wreckage (structure `towns_and_towers:wreckage_ocean`,
pool `kaisyn:ships/wreckage_ocean/wreckage`, one template `kaisyn:ships/wreckage_ocean/wreckage_1`, 14x3x14: 22 spruce
log, 14 spruce slab, 5 dark oak planks, 3 black wool, 3 barrels); the three villager raft templates in
`kubejs/data/villager_rafts/structures/` read with the NBT reader of `tools/port_structure_jars.py` (raft1 5x9x7: 11
white wool, 4 white carpet, 2 white beds, a villager, 4 item frames; raft2 the same size, 11 wool, 6 carpet; raft3
7x14x11: 42 white wool, 8 carpet, 2 beds). Meta behaviour from `worldgen/MetaStructure.java` and
`structure_overrides.js` in the repo.

Proposed test ids: **ST-240 to ST-259** (renumber at build time; other worldgen briefs propose ST- ids too).

## Scope and order

| Step | Rows | Kind | Note |
|---|---|---|---|
| 1 | #5 `avoidCoasts` | config / decision | settle first: every new land meta inherits the answer |
| 2 | #3 driftwood rates | data | values only; settle with `structures/contents #2` (driftwood log cleanup) |
| 3 | #1 caravans | data (+ optional patcherMod "line" layout) | existing structures only, no templates |
| 4 | #2 raft and wreckage variants | templates + a small tool | needs the user's saved damaged rafts; the colour/wood variants can go first |
| 5 | #4 boat groups | data, or templates + data | data-only convoy first; small boats need templates |

Shared steps for every meta change (3.16, 7.4.7): definition in `meta_structures.json`; who takes part in `meta/*`
tags of `structure_tags.json`; the meta's id in its group's `group/*` tag and a weight-1 entry in
`structure_groups.json` (sync writes it with the meta note); `python3 tools/gen_structure_groups.py`, `--check`;
`node tools/structure_tuning_sim.js`; `tools/test_structure_groups.sh` (node/Rhino parity, staged meta files);
`tools/test_worldgen_tools.sh` step 6 (`MetaTest`) when Java changes. A meta takes a share of its anchors' slots, so
no structure's rate changes; anchors must be members of the meta's own group; shares of one anchor add up and are cut
at 1 in id order; distances at most 112 blocks (7 chunks).

---

## structures/metas #1: Vehicle metas (caravans)
Status Planned · Work data · TODO `structures/metas #1`

### Goal
Three caravans marked in 7.4.9: a witch cart caravan (witch wagons in a line, maybe by a witch hut; swamps, dark
forest), a pillager caravan (loot carriages and prison vans with a siege weapon or a pillager camp), a trader caravan
(passive: trader camps along a road, with a way sign). Each with its own `meta/*` tags so the existing pillager metas
don't change.

### Now
- `group/vehicles` (52/16, rank 76, hostile, `biome_frequency` harsh 1.0 / default 0.5): `eugene:pillager_loot_carriage`
  (100 / 1.0; its pool picks the loot carriage 2, the villager chase 1 (entities only, a 1x3x1 piece) or the golem
  prison van 1), `eugene:witch_wagon` (200 / 0.76, 13x11x7, fitted to `land/dark_forest` + `land/swamp`), the 6
  siege weapons. No meta anchors on any vehicle yet.
- Friendly camps in `remote` (rank 70, peaceful radius 3): `eugene:camp` (trader camp + lost-llama camp, 148 / 1.0),
  `towns_and_towers:exclusives/village_wandering_trader_camp`, both in `meta/friendly_camps` (structure_tags.json
  line 1830). `meta/waysign_camps` already takes 25 % of friendly camp slots (camps + a way sign).
- `meta/way_signs` (line 1872), `meta/pillager_camps` (1901), `meta/siege_weapons` (1912); `group/witch_huts` members
  for a hut part.
- Parts are placed at a random angle per copy (`MetaStructure.placeStructures`, line 321: `angle =
  random.nextDouble() * 2 PI`, tries from the near end of `distance` outwards). There is no "in a line" layout.

### Build
1. Tags (`structure_tags.json`, each with a note): `meta/caravan_wagons` = `eugene:witch_wagon`;
   `meta/caravan_carriages` = `eugene:pillager_loot_carriage`; `meta/trader_camps` = `eugene:camp`,
   `towns_and_towers:exclusives/village_wandering_trader_camp`. Add `creativeengineering:meta/witch_cart_caravan`
   and `.../pillager_caravan` to `group/vehicles`'s values, `.../trader_caravan` to `group/remote`'s.
2. Definitions in `meta_structures.json` (after `waysign_house_camp`, line 604; `type`, `biomes:
   "#minecraft:is_overworld"`, `step: surface_structures`, `terrain_adaptation: beard_thin`, `spawn_overrides: {}`):

   | Meta | Anchor (share) | Parts |
   |---|---|---|
   | `witch_cart_caravan` | `#creativeengineering:meta/caravan_wagons` (0.30) | 1 to 2 more wagons, distance [16, 44], spacing 16, margin 3; chance 0.3: 1 witch hut (`#creativeengineering:group/witch_huts` members, or a new `meta/witch_huts` tag), distance [36, 80], margin 6 |
   | `pillager_caravan` | `#creativeengineering:meta/caravan_carriages` (0.35) | 1 to 3 more carriages, [14, 48], spacing 14, margin 3; chance 0.5: 1 of `#creativeengineering:meta/pillager_outworks` (siege weapons + camps), [30, 72], margin 6 |
   | `trader_caravan` | `#creativeengineering:meta/trader_camps` (0.25) | 1 to 2 more trader camps, [16, 48], spacing 16, margin 4; 1 way sign (`#creativeengineering:meta/way_signs`), [10, 40] |

   Shares on `eugene:camp`: `waysign_camps` 0.25 + `trader_caravan` 0.25 = 0.5, under the cap.
3. `python3 tools/gen_structure_groups.py`: adds the three metas with weight 1 and the meta note.
4. Optional, the look "in a line" (patcherMod): a part field `layout` = `"ring"` (default, today) | `"line"`: one
   bearing per part (rolled once from the meta's random), copy i at distance `min + i * step` along it, alternating
   sides of the anchor (`step` = `spacing`, or `(max - min) / count`), tries jitter the distance by +-spacing/2.
   Files: `MetaStructure.Part` record + codec (line 176: `strict(Codec.STRING, "layout", "ring")`, validate the
   value), `placeStructures` (line 321); JS `META_PART_KEYS` (`structure_overrides.js` line 101) and the part
   validator (~line 707); the `parts` text in `meta_structures.json` `_readme`; MetaTest checks (a line part's
   copies are collinear within a tolerance; a bad layout value is rejected).
5. Writeup: 3.16 table rows + tag rows; 7.4.9 -> built.

### Ids and data
`eugene:witch_wagon`, `eugene:pillager_loot_carriage`, `eugene:camp` (the ported C.C.C. 2.3.4 data in
`kubejs/data/eugene`), `towns_and_towers:exclusives/village_wandering_trader_camp`, `supplementaries:way_sign`,
`betterwitchhuts:witch_hut`, `betterwitchhuts:witch_circle`, `villagesandpillages:village_witch`; group and tag ids
above. All in the repo.

### Decisions
- **Which caravans** (the row's decision). Recommend all three; they are data only and change no rate.
- **Line layout:** build it (the caravans read as caravans) or not (a loose cluster, data only). Recommend data first,
  the line layout if the cluster looks wrong in game.
- **Witch hut in the caravan:** a hut is a hostile surface dungeon of its own; as a part it is built inside our meta
  start (rank 76) and only in swamps (its own fit). Recommend chance 0.3.

### Gotchas
- An anchor that isn't in the meta's own group only acts as a part: the trader caravan must sit in `remote`, the
  other two in `vehicles` (hostile: the peaceful radius keeps them away from villages and camps automatically, since
  `priority.peaceful.hostile` lists the group).
- `biome_frequency` of `vehicles` rolls the anchor's frequency, so caravans are twice as likely per area in harsh
  biomes, like the vehicles.
- The villager chase piece has no blocks; a caravan anchored on it still places carriages around it (fine), but earth
  fill sees no earth for that piece.
- Parts check their own biome lists: carriages are on open land only (`land/plains`, savanna, desert, badlands, snowy
  plains), so a pillager caravan never forms in forests.

### Tests
- ST-240 Loads: `[meta-structures] N meta structure(s) take slots from M anchor structure(s)` counts 3 more; the
  report lists the three; no "share above 100 %" warning.
- ST-241 `/locate structure creativeengineering:meta/witch_cart_caravan` (and the other two) answer; with
  `[worldgen.meta_structures] log = true` the log lists anchor and part positions within the distances.
- ST-242 Peaceful: no pillager caravan part inside a village's 5-chunk radius over a pregenerated plains square.
- ST-243 (line layout) the copies of a line part lie within 4 blocks of one line through the anchor.

---

## structures/metas #2: More raft and wreckage variants
Status Planned · Work data / templates · TODO `structures/metas #2`

### Goal
Only the 3 villager rafts and the one T&T wreckage template exist. Save damaged raft variants and load them into the
pack; random wool colour per raft; more floating debris with a random wood type; peaceful raft variants.

### Now
- Rafts: structures `villager_rafts:raft_1..3` (ported data, `kubejs/data/villager_rafts/worldgen/`), one-piece
  pools `villager_rafts:raft_N` on templates `villager_rafts:raft1..3`, in `group/boats` (83 / 1.0 each) and
  `meta/flotilla_rafts` (= `villager_rafts:*`), used by `driftwood_flotilla` (25 %: one raft) and `raft_convoy`
  (2 to 5 rafts). White wool sails, carpets and beds in all three.
- Wreckage: `towns_and_towers:wreckage_ocean` (`ocean_ruins`, 108 / 0.3243; biomes
  `#towns_and_towers:has_structure/shallow_oceans_wreckage`: cold, plain, lukewarm and warm ocean; start y 62),
  pool `kaisyn:ships/wreckage_ocean/wreckage` with one `legacy_single_pool_element`, no processors. In
  `meta/floating_wreckage` and `meta/fleet_wreckage` (debris, pillager fleet).
- Tooling: `tools/port_structure_jars.py` has a byte-exact NBT reader/writer (`nbt_read`, `nbt_write`, `Tag`) and
  palette remaps (`BLOCK_REMAP`, `convert_template`); `/ceplace templates <glob>` shows templates in rows, including
  `<world>/generated/<ns>/structures/` (11_fixes_1_0_8.md).

### Build
1. **Colour and wood variants (no code, can go first).** New `tools/gen_template_variants.py` + spec
   `kubejs/generation_scripts/worldgen/template_variants.json`:
   `{"villager_rafts:raft1": {"swap": {"minecraft:white_wool": "minecraft:{c}_wool", "minecraft:white_carpet":
   "minecraft:{c}_carpet", "minecraft:white_bed": "minecraft:{c}_bed"}, "values": {"c": [16 dye colours]}, "pool":
   "villager_rafts:raft_1"}}`. The tool reads the template (NBT functions imported from `port_structure_jars.py`),
   rewrites the palette names (properties kept), writes `kubejs/data/<ns>/structures/<name>_<value>.nbt`, and
   rewrites the pool JSON with one element per variant (weights: original 1, each variant 1, or by a `weights` map).
   Same spec for the wreckage: `kaisyn:ships/wreckage_ocean/wreckage_1` with `spruce_log`/`spruce_slab` (and the
   dark oak planks) -> `{w}_log`/`{w}_slab`/`{w}_planks` over oak, birch, spruce, dark_oak, jungle, acacia,
   mangrove; the pool override goes to `kubejs/data/kaisyn/worldgen/template_pool/ships/wreckage_ocean/wreckage.json`
   (kubejs data overrides the jar's file, as `kubejs/data/upgrade_aquatic` does).
   Pool variants keep the structure ids, so no tag, group, rate or meta changes.
2. **Damaged rafts (user templates).** In a creative single-player world of the pack: `/place template
   villager_rafts:raft1 ~ ~ ~` on water, damage it, then a structure block in SAVE mode named
   `villager_rafts:raft1_damaged_a` (include entities: on if the villager / item frames stay). The file lands in
   `saves/<world>/generated/villager_rafts/structures/`. Copy it to `kubejs/data/villager_rafts/structures/`, add it
   to the variant spec (so it gets colour variants too) and to its pool. Do the same for new peaceful raft designs
   (a fishing raft, a trader raft): new elements of the same pools, or a new `villager_rafts:raft_4` structure (the
   `villager_rafts:*` glob puts it in `meta/flotilla_rafts`; it needs a `group/boats` entry: sync gives a new
   structure weight 100 / 1.0, set it to 83 like the others).
3. **More floating debris:** new templates (floating crates, a broken mast, barrels and planks), saved the same way
   under `kaisyn` or `creativeengineering`, added as elements of the wreckage pool; wood variants through the spec.
4. Scans after new templates (7.4.2 step 6): `python3 tools/terrain_adaptation_scan.py`,
   `tools/nether_blocks_scan.py`, `tools/meat_block_scan.py` (red beds and red wool will show up as lone red blocks in
   the meat scan: expected, not meat), `tools/audit_loot_containers.py` (chests in new templates).
5. Writeup: 3.16 tag table (raft and wreckage variants), 3.11 (ported data), and the TODO note "only the 3 rafts".

### Ids and data
Templates `villager_rafts:raft1..3`, `kaisyn:ships/wreckage_ocean/wreckage_1` (verified), pools
`villager_rafts:raft_1..3`, `kaisyn:ships/wreckage_ocean/wreckage`; vanilla `minecraft:<colour>_wool / _carpet /
_bed`, `minecraft:<wood>_log / _slab / _planks` (all exist in 1.19.2; mangrove included, no cherry or bamboo).

### Decisions
- **Template source** (the row's decision): the user builds the damaged and peaceful variants; the colour and wood
  variants are generated. Recommend generating first.
- **Offline variants vs a processor:** a patcherMod structure processor that picks one palette per piece (seeded by
  the piece origin) would cover any template without files, but needs a registered `StructureProcessorType` and
  testing. Recommend offline variants (small files, deterministic, visible in `/ceplace templates`).
- **Colour weights:** all 16 equal, or mostly white/brown/grey. Recommend white 4, each other colour 1.

### Gotchas
- Chests opened before saving bake their loot into the template; save from a fresh `/place template` without opening
  them, or check `LootTable` tags in the saved NBT (the raft chests use `villager_rafts:chests/fixed/*`).
- Beds in 1.19.2 store colour in the block id (no block entity data), so the palette swap is enough.
- `legacy_single_pool_element` is kept for the wreckage elements (T&T's choice); new elements can be the same type.
- Variant elements share their structure's piece-count limit (size 1: a one-piece structure), nothing else changes.

### Tests
- ST-244 Pools load: `pool_tag_report.md` lists `villager_rafts:raft_1` with its variants and
  `kaisyn:ships/wreckage_ocean/wreckage` with the wood variants; no "missing template" error in the log.
- ST-245 `/ceplace templates villager_rafts:*` and `kaisyn:ships/wreckage_ocean/*` show every variant intact.
- ST-246 A raft convoy found by `/locate` shows rafts of different colours; a shipwreck debris meta shows wreckage of
  a non-spruce wood in some runs.

---

## structures/metas #3: Driftwood rates
Status Planned · Work data · TODO `structures/metas #3`

### Goal
Old note (scratchpad 2026-10-19): lower the individual driftwood frequency, raise the flotilla meta's.

### Now
- Individual driftwood is Upgrade Aquatic's placed feature `upgrade_aquatic:driftwood`, overridden in
  `kubejs/data/upgrade_aquatic/worldgen/placed_feature/`: `driftwood_ocean` rarity 1/128 (water below),
  `driftwood_beach` 1/16, `driftwood_river` 1 in 2, `driftwood_swamp` count 1, `driftwood_extra` 1 or 2 (3:1) (the
  last four on a bank since 1.0.7), plus the land versions in
  `kubejs/data/creativeengineering/worldgen/placed_feature/driftwood_land/` (beach 1/160, swamp 1/10, extra 1/8)
  with biome modifiers in `kubejs/data/creativeengineering/forge/biome_modifier/driftwood_land_*.json`
  ([structure_tuning.md](../structure_tuning.md) 3.12 tables).
- Flotilla: `meta/driftwood_flotilla`, no anchor, alone in `group/flotsam` (32/8, weight 100, frequency 1): about 1
  per 32 x 32 chunks in oceans; 8 to 16 logs within 26 blocks, 25 % with a raft. Its log feature is
  `creativeengineering:meta/flotilla_driftwood`.

### Build
1. Individual (proposed values, see Decisions): ocean 128 -> 512, beach 16 -> 32, river 2 -> 4, swamp `count 1` ->
   `rarity_filter` 2, extra -> `rarity_filter` 2 (drop the weighted count). Land versions unchanged (already a tenth).
2. Flotilla: `groups.flotsam.placement` spacing 32 -> 24 (x1.78 flotillas, 1 per 24), separation 8 stays.
3. `python3 tools/check_worldgen_json.py` if it covers placed features (it checks the worldgen JSON shapes), then the
   1.19.2 codec decode the 3.23 note mentions (`tools/test_worldgen_tools.sh` decodes the 8 driftwood lists).
4. Update 3.12 tables and `__comment` lines in each file.

### Ids and data
`upgrade_aquatic:driftwood` (feature), the 5 overridden placed features, the 3 land ones, the flotsam group. The
feature itself still decides what fits (UA's own checks); unverified against the UA jar here, the 3.12 notes come
from an earlier jar read.

### Decisions
- **Values** (the row's decision): recommend the numbers above (open-ocean singles x0.25, shore singles x0.5,
  flotillas x1.78).
- **Order with `structures/contents #2`** (driftwood crafts to oak planks, remove driftwood logs): if the driftwood
  log goes, the feature (and the flotilla) still place UA's driftwood blocks unless the feature is swapped. Settle
  contents #2 first or in the same session.

### Gotchas
- Rarity is per chunk and per feature; driftwood in rivers and swamps sits on banks, so "lower" there is visible on
  every river walk.
- The flotilla is the only `flotsam` member, so its rate is exactly spacing^-2 x frequency.

### Tests
- ST-247 `--check` flotilla 1 per 24; a pregenerated ocean square (same seed) has about 1.8x the flotillas and about a
  quarter of the lone floating logs of a run with the old values.

---

## structures/metas #4: Travelling boats in groups
Status Planned · Work data · TODO `structures/metas #4`

### Goal
A boat-group meta: boats travelling together (scratchpad 2026-10-19 "travellings boats, in groups"; the "peaceful raft
variants" of the same note are in #2).

### Now
- `group/boats` (48/28, rank 30): T&T ship village and pillager ship, `antiquetradingship:antique_trading_ship` (97),
  `unwrecked_ships:ship` (73), Blocky Siege pillager ship, illager longship and warship, the 3 rafts, the
  `raft_convoy` and `pillager_fleet` metas. Peaceful ships at sea have no meta; `raft_convoy` (rafts) and
  `pillager_fleet` (hostile ships) are the patterns.
- `water_ok` (structure_tags.json line 1044) lists `#type/ship`; `type/ship` (line 561) holds every ship mod.

### Build
1. Data-only first: tag `meta/merchant_ships` = `antiquetradingship:antique_trading_ship`, `unwrecked_ships:ship`;
   meta `creativeengineering:meta/merchant_convoy` (in `group/boats`, `type/ship`, `meta/coast_ok`), anchor
   `#creativeengineering:meta/merchant_ships` share 0.3, parts: 1 to 2 more merchant ships [40, 100] spacing 40
   margin 8; chance 0.5: 1 to 2 rafts [20, 60] spacing 16. `terrain_adaptation: none`, biomes
   `#minecraft:is_overworld` (parts check their own lists).
2. With templates (the row's "template source"): small sailboats or rowboats built in game (a few blocks of hull +
   mast + a villager; vanilla boat entities with a villager passenger may not survive a template: test one first),
   saved as `creativeengineering:boat/<name>` templates, one jigsaw structure `creativeengineering:small_boat` (pool of
   the designs, start height y 62 like the T&T wreckage, `terrain_adaptation: none`), tags `type/ship`, `group/boats`,
   `meta/small_boats`, an `ocean/*` fit if it should avoid frozen oceans; a meta `boat_flotilla` anchored on it
   (share 0.5, 2 to 5 more boats [12, 48] spacing 12). Its group entry starts at weight 100 / 1.0 from sync (no mod
   default): set it by hand (about the rafts' 83).
3. Same shared steps as above; `--check`.

### Ids and data
`antiquetradingship:antique_trading_ship`, `unwrecked_ships:ship`, `villager_rafts:*`; new ids above. Template
contents: user-built.

### Decisions
- **Template source** (the row's decision): data-only convoy now (recommended), small boats when templates exist.
- **Peaceful at sea:** nothing keeps pillager ships away from merchant convoys (the peaceful radius is land-only by
  group). Recommend leaving it (both rank 30, separate slots of one grid).

### Gotchas
- Big ships reach far; with parts at up to 100 blocks the meta's box nears the 7-chunk reach; keep `distance` max at
  or under 100 and `margin` 8.
- Unwrecked Ships' `ship` pool once held Nether pieces; only `unwrecked_ships:nether_ship` was crimson/warped (off).

### Tests
- ST-248 `/locate structure creativeengineering:meta/merchant_convoy` answers; the log shows 2 to 3 ships spaced 40+
  apart and no piece on land.

---

## structures/metas #5: `avoidCoasts` side effect
Status Planned · Work config · TODO `structures/metas #5`

### Goal
Decide what to do with `[worldgen.meta_structures] avoidCoasts` (off): with it on, land metas whose satellites can't
avoid a nearby beach or river fail (three failed at -104 -150, 32 blocks from a gravel beach;
[testing/structures/12_earth_fill_camps.md](../testing/structures/12_earth_fill_camps.md) 4).

### Now
- `MetaStructure` (line 85 doc, `generate` / `placeStructures` take an `avoid` predicate, `reaches`, `avoidsCoasts`
  line 420): with the switch on, no part may start in, or reach with its box centre or a corner into, a
  `creativeengineering:coast_river` biome (beaches, stony shore, gravel beach, every river); an anchor there leaves
  the slot to the plain anchor; the 6 water metas in `meta/coast_ok` and feature parts are exempt. A failed meta is
  the plain anchor, so no anchor's rate changes; only fewer metas near coasts.
- CEConfig line ~2296 `avoidCoasts` default false; needs a restart (not reloaded live).
- The original complaint (camp ruins meta making islands in water, 7.5) is handled by earth fill's footprint water
  share (`maxWaterSurplus` 0.5, ST-201), which is on.

### Build
1. Recommended: keep the switch off; close the row after one look in game at coastal metas (are there still islands
   or camps in water? if not, done). Config only, nothing to build.
2. If the user wants it on but softer, a code option: `avoidCoasts = "OFF" | "START" | "FULL"` where START only
   checks each part's start point (not the box corners), which lets satellites stand next to a beach. Files:
   `MetaStructure.generate` / `reaches`, CEConfig enum, `WorldgenConfig` getter; MetaTest case.

### Decisions
- Keep off (recommended), soften (START), or on (FULL, today's code).

### Tests
- ST-249 (if softened) at -104 -150 (seed 8675309) the three land metas assemble with START and fail with FULL.

---

## Built, needs play (Testing / Complete)

| Row | Change | Status | Note |
|---|---|---|---|
| #6 | Companion reach: town companion villages 64 to 96 blocks out (1.0.8) | Testing | Matches `meta_structures.json` (`town_villages` `companions.distance` [64, 96]); scratch run saw 7 companions at 58 to 93 blocks ([testing/structures/11_fixes_1_0_8.md](../testing/structures/11_fixes_1_0_8.md)). Nothing stale found |

## Writeup corrections made

- [testing/structures/12_earth_fill_camps.md](../testing/structures/12_earth_fill_camps.md) 3 (Deploy state): "Not
  deployed" -> DEPLOYED with pack 1.0.9 (2026-10-19 15:54, plan_1.0.9.md EF).
