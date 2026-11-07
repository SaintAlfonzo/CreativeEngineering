# Brief: structures / rates-and-groups

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# structures`, rows `structures/rates-and-groups` #1 to #8,
#10, #11 (open) and #9 (Testing). Reference for how the systems work: [structure_tuning.md](../structure_tuning.md)
(2.2 groups, 2.3 slots, 3.1 applier, 3.16 metas, 3.20 folds, 3.22 priority, 4 limits, 6 offline checks).

Facts checked against jars (2026-10-21): Waystones 11.4.2 worldgen (`WaystonePlacement`, `ModWorldGen`,
`SinglePoolElementMixin`, `WaystoneBlockEntityBase`), MES 1.3.1 `end_biomes` tag, YUNG's Better Nether Fortresses
1.0.6 `fortress_avoid` tag, Towns & Towers 1.10 wreckage (for the metas brief). Everything else is from the repo
(configs, `structure_groups.json`, `structure_config.json`, patcherMod source, the game-written reports).

Proposed test ids: **ST-210 to ST-239** for this brief (other worldgen briefs propose ST- ids too; renumber at build
time so they stay unique; the last id in use is ST-205).

## Scope and order

| Step | Rows | Kind | Why this order |
|---|---|---|---|
| 0 | **#9 bug** (found 2026-10-21, see "Built, needs play") | patcherMod + data | 27 pools have permuted weights since 1.0.7; small fix, affects every new chunk now |
| 1 | #8, #11 (lower the rate), #2a (shipwreck 40 %, fresh watchtower `--fit-rates`) | data / config | one-line changes, no design open |
| 2 | #5 test overrides | KubeJS + config | makes #6 and every later rate test cheap |
| 3 | #10 + #2b (jungle temple distance) | data | the same change: split `surface_dungeons` into temple groups |
| 4 | #4 ranking by importance | data (+ small KubeJS) | the new groups of steps 3 and 5 need ranks; design the table once |
| 5 | #1 Nether / End / underground grouping | data | with `structures/contents #1` (move soul dungeons to the Nether) and the cave pass (7.3.3) |
| 6 | #3 strip weights + #2c (sky village, settlements over water) | data + tool | one rate pass once every group exists; needs a census after |
| 7 | #6 fortified metas vs `/locate` | config | uses #5; rate side waits for #3 |
| any | #7 ocean geodes | patcherMod | independent |
| later | #11 (waystones inside structures) | data | optional second half of #11; uses the metas brief |

Every step: edit, then `python3 tools/gen_structure_groups.py` (sync) when tags changed, `--check` (rewrites
`structure_group_estimates.md`), `node tools/structure_tuning_sim.js` (the line the game will log),
`tools/test_structure_groups.sh` (node/Rhino parity) and, for Java, `tools/test_worldgen_tools.sh`. Rejoin to apply;
new chunks only.

---

## structures/rates-and-groups #1: Nether, End and underground grouping pass
Status Planned · Work data · TODO `structures/rates-and-groups #1`

### Goal
Put the standalone Nether, End and underground structures on shared grids (groups), like the Overworld surface,
so structures of one kind keep apart and their rates are set in one place; decide the deep dungeons' biomes and rates
(the cave-structure pass, [structure_tuning.md](../structure_tuning.md) 7.3.3, Todo "Cave-structure generation tuning").

### Now
- 54 standalone sets in `structure_groups.json` `standalone` (line 1826 on). Placements and members are in
  `structure_config.json` (`structure_sets.<id>.original`). Changed rates are in `structure_group_estimates.md`
  "Standalone sets" (line 404): Philip's underground set is at 15.6x its mod density (1 per 26), Terralith's
  underground set 1.5x to 15x (1 per 7 to 13), Philip's crypts 2x, the lost soul city 4.5x, from the old multiplier
  model's migration.
- Nether: `minecraft:nether_complexes` (27/4: fortress weight 2, blocked by YUNG's in code, falls through to the
  bastion weight 3), `betterfortresses:fortress` (30/20, `yungsapi:enhanced_random_spread` with
  `enhanced_exclusion_zone` 10 chunks from `#betterfortresses:fortress_avoid` = `minecraft:nether_complexes`,
  verified in the jar), `minecraft:nether_fossils` (2/1), `hearths:towers` (12/9 over 16/12),
  `hearths:netherrack_spirals` (6/4), `farmers_structures:cooks_nether_1/2_with_all`, `eugene:piglin_camp` (22/12,
  fixed y 33, 7.4.6), `unwrecked_ships:nether_ship` (off), `betterdungeons:small_nether_dungeons` (off in YUNG's
  config). `group/nether_ruins` (14/7, rank 60) already holds the obelisk and Philip's three Nether ruins.
- End: `minecraft:end_cities` (26/18 triangular), `nullscape:dragon_skeleton`, `philipsruins:end_ruins` (end ruins +
  end gate fortress), 16 `mes:*` sets. Every MES set is `mes:advanced_random_spread` with
  `min_distance_from_world_origin: 1000` (in `structure_config.json`), and MES's biome tag
  `#mes:has_structure/end_biomes` is `#minecraft:is_end` (verified in mes-1.3.1), which includes the central island.
- Underground: mineshafts (spacing 1, frequency 0.004), ancient city (19/6), strongholds (vanilla rings, blocked;
  `betterstrongholds:stronghold` with its own ring placement), YUNG's skeleton (46/23) and small (10/6) dungeons,
  Philip's `ancient_crypt`, `antiquus_crypta`, `ancient_dungeon` (7/2), `underground_structures` set (lost soul
  dungeon, bone dungeon, underground structures, sculk dungeon; 23/13) and `lost_soul_city` (37/30),
  `terralith:underground` (3/1, 6 members) and `underground_dungeon` (frosted dungeon, 9/6),
  `underground_bunkers:minor_structures` (25/20), the 8 `subterrestrial:*_cabin` sets (spacing 6 to 10),
  `minecraft:buried_treasures`.
- The skeleton dungeon, bone dungeon and lost soul city generate again through the `land/any/deep` fit (1.0.7,
  `biome_fits.json`); `land/any/deep` holds the 14 cave biomes.
- Groups are always plain `minecraft:random_spread` (`structure_overrides.js` line 885); `placement` takes one
  `exclusion_zone`.

### Build
1. Tags in `structure_tags.json` (each with a `note`), then sync. Proposed groups (see Decisions for the open ones):

   | New group | Members | Starting grid | Rank (#4) |
   |---|---|---|---|
   | `nether_strongholds` | `betterfortresses:fortress`, `minecraft:bastion_remnant` (and the blocked `minecraft:fortress` stays out: sync skips `pack.blocked`) | sync sizes it; bastion + fortress today ~1 per 27 and 1 per 30 | 90 |
   | `nether_ruins` (existing) | + Hearths' crimson and warped towers, Farmer's Structures' two Nether cooks, `eugene:piglin_camp`, and (with `structures/contents #1`) the lost soul city, lost soul dungeon, rare ruin and blackstone ruin | keep 14/7, re-sync | 60 |
   | `end_cities` | `minecraft:end_city`, `philipsruins:end_gate_fortress`, MES phantom citadel, enderkeep courtyard, starlight voyager, ender spire | sync | 85 |
   | `end_ruins` | `philipsruins:end_ruins`, `nullscape:dragon_skeleton`, the other 12 MES structures | sync | 60 |
   | `underground_dungeons` | YUNG's skeleton and small dungeons, Philip's crypts, ancient dungeon, bone, sculk and lost soul dungeons (until moved), Terralith's frosted dungeon, the underground bunker | sync | 55 |
   | `underground_cabins` | Terralith's underground set, Philip's `underground_structures`, Subterrestrial's 8 cabins | sync | 50 |

   Stay standalone: mineshafts, strongholds (both ring placements), buried treasure, ancient city (deep dark only,
   its own grid is fine), Nether fossils and netherrack spirals (decoration-level density; a group with spacing 2
   would starve every other member), the switched-off sets.
2. `structure_groups.json`: sync adds each group from its tag (`group_grid` sizes the grid from the most crowded
   biome, the members keep their current rates). Then set `placement` by hand where the decision says so, and add
   each new group to `priority.groups`. Hostile ones (`nether_strongholds`, `underground_dungeons`?) into
   `priority.peaceful.hostile` only if a peaceful structure can be near them: none in the Nether or End today, so
   leave them out.
3. **MES on the main island:** a group loses MES's `min_distance_from_world_origin`. Give the End members a fit
   instead: a new `end/outer` fit in `biome_fits.json` (`inherit: false`) with the outer End biomes (vanilla
   highlands, midlands, small end islands, barrens, and Nullscape's outer biomes; read the live list from
   `/dumpstructuregen` or `biome_config.json`), and the 16 MES structures + Philip's End structures in the
   `end/outer` structure tag. Without it MES structures can start on the dragon island.
4. **YUNG's fortress exclusion:** in one group with the bastion the shared grid replaces the 10-chunk
   `enhanced_exclusion_zone` (members never start closer than separation + 1). Keep separation >= 10.
5. Deep dungeons: decide their fits with the cave pass. `land/any/deep` (surface biomes + cave biomes) works for
   start heights in caves; leave it unless a structure should be cave-biome-only (lush caves cabins, deep dark).
6. Re-tag `type/*` / `dungeon/*` only if a new group needs a tag that doesn't exist; prefer the existing
   `dungeon/underground_*`, `type/underground`, `nether`, `end` tags as group tag values
   (`"values": ["#dungeon/underground", "!#dungeon/underground_vanilla", ...]`).
7. FitTest's hardcoded counts (`tools/worldgen_test/FitTest`, 7.4.7 step 2) go up if `end/outer` adds structures.
8. Regenerate `structures_by_biome.md` (`python3 tools/gen_structure_biome_table.py`), update 2.2 (groups table) and
   the standalone paragraph under it.

### Ids and data
- Set and structure ids: all above, from `structure_config.json` (verified in repo). MES biome tag verified in
  `mes-1.3.1-1.19.2-forge.jar`; YUNG's `fortress_avoid` verified in `YungsBetterNetherFortresses-1.19.2-Forge-1.0.6.jar`.
- No item / block ids involved.

### Decisions
- **Which End structures count as "cities" vs "ruins"** (recommendation in the table: the big walled ones in
  `end_cities`). MES's nature pieces (enderskog, enderbloom grove, mythic garden, placid prairie) could be a third
  `end_nature` group if they crowd ruins out; start with two groups and look at `--check`.
- **Underground rates:** restore mod defaults (Philip's underground set 15.6x, Terralith's up to 15x today) or keep.
  Recommend: target 1x to 2x mod default, the migration's multipliers were never a decision.
- **Piglin camp:** into `nether_ruins` (neutral) now, or wait for the lava check of 7.4.6. Recommend wait; it stays
  standalone until earth fill handles lava (row `structures/earthfill` family).
- **Ranks underground vs surface:** priority is 3D, so a dungeon under a village never conflicts; ranks only order
  underground structures among themselves. Recommend dungeons above cabins.

### Gotchas
- Groups are plain random spread: MES's origin distance, YUNG's enhanced exclusion and the vanilla triangular spread
  of end cities are lost (end cities keep their density by `--check`, not their spread shape).
- A structure in two groups is a config error that makes the applier apply nothing (3.1). `nether_ruins`'s tag must
  not overlap the new groups' tags.
- Dragon skeleton biome is `nullscape:shadowlands` only; alone in its biomes it gets every slot of `end_ruins` there
  at its frequency: check its rate.
- Subterrestrial's cabins have very dense mod grids (6 to 10); folding them into one grid with Terralith's set cuts
  them hard unless the grid is small. Look at `--check` before accepting.
- `/locate` of a member whose set changed works the same (group sets are normal sets); explorer maps aiming at the
  old set ids (none known for these) would go dead: grep the jars for `exploration_map` destinations (4, "Removed
  sets and structure searches").
- New chunks only: the server world's explored Nether/End keep today's layout.

### Tests
- ST-210 Groups load: the `[structure-tuning]` line counts the new groups; `structure_tag_report.md` lists them with
  members; no "structure in two groups" error.
- ST-211 MES off the main island: in a new world `/locate structure mes:monolith` from 0 0 in the End answers more
  than 1000 blocks out; a Chunky pregen of radius 1000 blocks around 0 0 in the End has no MES start
  (`/dumpstructuregen` or a structure-start census).
- ST-212 Fortress and bastion keep apart: `/cwglocate census` (locate.md) over a Nether square: no fortress start
  within 10 chunks of a bastion.
- ST-213 Deep dungeons (retest of ST-115): skeleton dungeon, bone dungeon, lost soul city `/locate` answers in cave
  biomes.
- ST-214 Underground density: a pregenerated 64x64-chunk square has underground cabins and dungeons within x0.5 to
  x2 of `--check`'s estimate.

---

## structures/rates-and-groups #2: Tuning pass after the scratch run
Status Planned · Work data · TODO `structures/rates-and-groups #2`

### Goal
Four rate changes listed after the scratch run: shipwrecks to 40 %, a bigger minimum distance between jungle temples,
the sky village (lower it if common; maybe more settlements over water), and the fresh watchtower back to 1 per 67.

### Now
- Shipwrecks are Revamped Shipwrecks' two jigsaw wrecks (it empties `minecraft:shipwrecks`): in `ocean_ruins`
  `revamped_shipwrecks:shipwreck` weight 258 / frequency 1.0 and `shipwreck_beached` 129 / 0.4444
  (`structure_groups.json` lines 1026, 1030). Estimates: 1 per 17 and 1 per 12 (1.91x and 4.0x mod default).
  Both anchor `meta/shipwreck_graveyard`; the sunken one also anchors `meta/shipwreck_debris` and `pillager_fleet`
  wreckage parts.
- Jungle temple: `betterjungletemples:jungle_temple` in `surface_dungeons` (16/10), weight 1 (fit_base 186),
  frequency 0.7481, estimate **1 per 19** in its biomes (1.51x mod default). Members never start closer than 11
  chunks (176 blocks).
- Sky village: `skyvillages:skyvillage` in `sky` (64/16), weight 299 / frequency 1.0, fitted to land and the oceans
  (`land/sky` fit), estimate 1 per 91. Shares the grid with the AE2 meteorite and Cloud Storage's balloon and sky
  temple (100 / 0.985 each).
- Fresh watchtower: `remote`, weight 160 / 1.0, `fit_keep: true`, no `fit_base`; estimate 1 per 83 (2026-10-14
  estimates).

### Build
1. Shipwrecks: set `frequency` of both wrecks to 0.4 x today: shipwreck 1.0 -> 0.4, beached 0.4444 -> 0.178 (see
   Decisions). A missed roll leaves the slot empty, so no other ocean ruin gains.
2. Jungle temples: done by #10's split (a `jungle_temples` group with a bigger separation). Do them together.
3. Fresh watchtower: `python3 tools/gen_structure_groups.py --fit-rates`, then `git diff
   kubejs/generation_scripts/worldgen/structure_groups.json`. The run re-solves every fitted structure; if more than
   the watchtower moves, either accept (the others moved because 7.4 added neighbours) or revert all but the
   watchtower's entry. Read its new "1 per" in `structure_group_estimates.md`.
4. Sky village (after an in-game look, see Decisions): to lower it, its frequency (1.0 -> 0.5 halves it). For "more
   over water": the existing group-level `biome_frequency` would scale the meteorite and balloons too, so either
   - move the sky village into its own group `sky_villages` (tag + sync; give it the `sky` rank 50), then
     `"biome_frequency": {"#minecraft:is_ocean": 1.0, "default": 0.5}` on it (data only), or
   - make `biome_frequency` per member (patcherMod `StructureFrequencies` stages factors per set today:
     `stageBiomeFactors`, `Member.factors`; the JS `planBiomeFrequency` (`structure_overrides.js` ~633) and the
     estimator would need a member-level key). More work for the same result.
   Recommend the own group.

### Ids and data
`revamped_shipwrecks:shipwreck`, `revamped_shipwrecks:shipwreck_beached`, `betterjungletemples:jungle_temple`,
`minecraft:jungle_pyramid` (its 10 % replacement), `skyvillages:skyvillage`, `watchtower_building:fresh_watchtower`;
biome tag `#minecraft:is_ocean`. All in the repo's configs.

### Decisions
- **Shipwreck "40 %":** 40 % of today's rate for both wrecks (recommended; keeps the beached:sunken ratio) or both at
  frequency 0.4 (beached goes slightly down, sunken to 40 %).
- **Sky village:** look first (the row says "if still common"); at 1 per 91 it is rarer than the mod default (70).
  Recommend no rate change unless the look says otherwise; the over-water boost only with the own group.
- **Watchtower:** accept the whole `--fit-rates` diff or only the watchtower. Recommend whole diff if every change
  is under 10 %.

### Gotchas
- Shipwreck metas are shares of anchor slots, so graveyards and debris fall to 40 % too (intended or not: say so in
  the writeup).
- `--fit-rates` writes `fit_base` for structures that lack one; review those new fields in the diff.

### Tests
- ST-215 Shipwreck rate: `--check` shows both wrecks at 0.4x the 2026-10-21 estimate; a scratch census of a
  pregenerated ocean square counts about 40 % of the wrecks of a run with the old values (same seed).
- ST-216 Watchtower: `/locate structure watchtower_building:fresh_watchtower` answers; `--check` 1 per 67 +-5.

---

## structures/rates-and-groups #3: Strip unexplained structure weights
Status Testing (built 2026-10-30; SQ-WG30-C01 PASS, C02 PARTIAL) (structure_tuning.md 3.27; `fit_keep: false` marks stripped members) · Work data · TODO `structures/rates-and-groups #3`

### Goal
User note (2026-10-19): "strip all structure weights unless an explicit 'increase or decrease' note or change was
made. relative frequencies aren't static now". The group weights and frequencies are still the 2026-09-27 migration
fit (made to keep each structure at its old per-set rate) plus `--fit-rates` solves; since the biome fits moved every
structure's biomes, those numbers no longer stand for anything a person chose.

### Now
- Group members carry `weight`, `frequency`, sometimes `fit_base`, `fit_keep`, `note`, `replaces` + `share`.
  Weights range 1 to 560 (`ocean_ruins`: Hopo's warm small ruins 560, ocean fortress 29). 33 metas and 3
  replacements are weight 1 by design (never picked by weight). Only 7 members have a hand note: the 6 Campsite
  camps and the witch wagon. Hand-set without a note: the whole `vehicles` group (7.4.5), the statues and cats
  (`ruins` 26 and 19, decision 4 of 7.1), It Takes a Pillage's camp and bastille (decision 5), the monument's
  frequency 0.1 (`ocean_dungeons`), `fit_keep` towers.
- Tools: `gen_structure_groups.py` `sync` (fits new members), `--fit-rates` (re-solves fitted ones), `--refit GROUP`
  (new grid, every member refitted to its current rate), `MEDIAN_WEIGHT = 100` (line 75).

### Build
1. Add a member field `"tuned": "<what and when, who decided>"` (validator: `structure_overrides.js` member keys;
   the tool must carry it through `sync` / `--fit-rates`). Mark the deliberate ones listed above (and anything the
   user names). Metas and replacements are skipped by rule (weight 1 by design).
2. New tool mode `python3 tools/gen_structure_groups.py --strip-weights [GROUP ...]`: for every member without
   `tuned`, not a meta, not a replacement: `weight` -> `MEDIAN_WEIGHT` (100), drop `fit_base`, keep or reset
   `frequency` (Decision). Print a table old -> new and the estimate ratio per structure; write
   `structure_group_estimates.md` against today's file (`write_estimates(..., baseline=<old doc>)`), so the diff in
   rate per structure is visible before rejoining.
3. Group grids stay (spacing/separation are hand-set, 2.2). `--fit-rates` must not re-fit stripped members later:
   either mark them `fit_keep: false` + no `fit_base`, or teach `fit_rates` to skip members without `fit_base`.
4. Run the census (`/cwglocate census`, locate.md) on a scratch world afterwards; set `tuned` on whatever the user then
   adjusts by hand.

### Ids and data
No ids beyond `structure_groups.json`. Code: `tools/gen_structure_groups.py` (`fit_rates` line 1268, `sync` 865,
`main` 1412), `kubejs/server_scripts/worldgen/structure_overrides.js` (member validation in the groups loop ~885).

### Decisions
- **Weights only, or weights and frequencies?** Weights decide the share where members meet; frequency is "rarer
  everywhere" and holds most of the migration's rarity (mansion 0.119, desert temple 0.25). Recommend strip
  weights to 100 and keep frequencies in this pass; a second pass can reset frequencies to 1 group by group after
  the census.
- **Which entries count as explicit** (the list in Now, plus anything else the user names). Ask before running.

### Gotchas
- Stripping moves real rates by large factors in shared biomes (an ocean ruin at 560 vs 29 today becomes 1:1). Show
  the per-structure ratio table to the user first.
- Do it after #1 and #10 so new groups are stripped the same way, and before the final `--check` of #2.

### Tests
- ST-217 Strip report: the tool prints every changed member; `tuned` members, metas and replacements unchanged
  (diff of `structure_groups.json`); node sim clean.
- ST-218 Census after the strip: per group, members sharing a biome start in about equal numbers (x0.7 to x1.4)
  where their frequencies are equal.

---

## structures/rates-and-groups #4: Placement priority by importance
Status Planned · Work patcherMod / data · TODO `structures/rates-and-groups #4`

### Goal
User (2026-10-19): "placement order doesnt work how i thought, amend placement order for importance and hard to place
structures instead of structure 'age'". Today a group's rank decides who wins an overlap; the user wants the
structures that matter more or are harder to place (big, rare, picky about ground) to win, whatever group they are in.
The source memory `placement-priority` is not in the repo; confirm the reading with the user (see Decisions).

### Now
- Ranks: `structure_groups.json` `priority` (line 2453): `groups` (settlements 95 ... flotsam 25) and `structures`
  (`#priority/big_settlements` 100). JS `planPriority` (`structure_overrides.js` line 533): every member of a ranked
  group gets the group's rank, then `priority.structures` entries (ids or `#tag`) override, **only for group
  members** (`if (owner[id])`, line 553); standalone structures can't be ranked.
- Java `PlacementPriority` (`worldgen/PlacementPriority.java`): ranks are per structure already (`Active.rank`), a
  set's rank is its highest member's (`setRank`); `filter` (line 497) drops a start whose boxes overlap a predicted
  higher-ranked start (`higher`, line 578: only sets whose rank > r are predicted). Equal ranks never check each
  other. Companions rank group - 1.
- So "by group" is a data choice, not a code limit: per-structure ranks work today for group members.

### Build
1. Ranking table (data). Propose tiers to the user, then encode them with `priority/<tier>` structure tags in
   `structure_tags.json` and `priority.structures` entries (`"#priority/landmarks": 97`). Starting proposal, ordered
   by size x rarity x placement difficulty (earth-fill sensitive, narrow biomes):

   | Rank | Structures |
   |---:|---|
   | 100 | `#priority/big_settlements` (unchanged) |
   | 97 | `#priority/landmarks`: woodland mansion, YUNG's desert and jungle temples (+ their vanilla replacements), C.C.C.'s undead castle, Mysticrift's great towers, T&T forts, Philip's ocean fortress (at sea) |
   | 95 | villages (settlements) |
   | 90 | other pillager outposts and towers |
   | 85 | Terralith spire, Philip's badlands dungeon, T&T mimic, witch village |
   | 80 to 25 | the rest as today by group |

   Back it with numbers: add `--priority-report` to `gen_structure_groups.py` that prints per ranked structure its
   rank, "1 per N" (from the estimates), footprint (largest piece box; `terrain_adaptation_scan.json` has template
   sizes per structure) and whether it is earth-fill sensitive (`[earth-fill]` size class), sorted by a score
   `rarity x footprint`. The user picks the tiers from it.
2. Allow standalone structures in `priority.structures` (needed after #1 only if a standalone one should rank):
   in `planPriority` accept ids that are in `structure_config.json` sets even without `owner[id]`. Java needs no
   change (`setRank` covers any set).
3. Ties across groups: equal ranks never check each other; give every pair of groups that can meet in one biome
   different ranks (the split temple groups of #10 must not share 80 with `surface_dungeons`).
4. Report: `buildReport` "## Placement priority" (line 1105) already lists overrides; add the per-structure table.

### Ids and data
`priority` block of `structure_groups.json`; tags `priority/*` in `structure_tags.json` (`priority/big_settlements`
at line 1878 is the pattern). No mod data.

### Decisions
- **Reading of "age":** most likely "the order a structure happens to generate in" (vanilla's last-writer-wins, or
  the group order). Ask the user which structures they saw losing; recommend the tier table above as the default.
- **Ranks within a group** (a fort vs a camp in `pillager_settlements`): a group's grid keeps members apart, so ranks
  inside a group only matter near grid cell edges with big members. Recommend not to split ranks inside a group
  except for `#priority/landmarks`.

### Gotchas
- Cost: a start predicts every set with a higher-ranked member; ranking a few members of many groups high makes more
  sets "higher" for everyone below and adds predictions (each predicted slot is generated twice;
  [testing/structures/10_phase_10.md](../testing/structures/10_phase_10.md) 5 lists the hot spots, row
  `structures/earthfill #7`). Keep high ranks to a short list; check `/cwglocate` timing (LO- tests) after.
- Prediction depth is capped at 3 (`MAX_DEPTH`); chains stay short only while ranks rise per level.
- Peaceful radius is separate from rank (a hostile landmark still drops near a village).

### Tests
- ST-219 Ranks staged: the report's priority table lists the tiers; `[placement-priority] N structure(s) ranked`.
- ST-220 Landmark wins: `/place structure minecraft:mansion` beside a predicted pillager tower slot is not the
  check (no prediction on `/place`); instead pregenerate a dark-forest square and read `[placement-priority]
  dropped` lines (`log = true`): no mansion is dropped for a lower tier; a drop line names a higher-ranked winner.

---

## structures/rates-and-groups #5: Rare structures hard to test
Status Complete (built 2026-10-30; SQ-WG30-C03, C04 PASSED 2026-10-30) (structure_tuning.md 3.27; the file ships with boosts, the switch off) · Work config · TODO `structures/rates-and-groups #5`

### Goal
Make rare structures testable by loosening soft rules only (frequency, weight, share, spacing, separation, debug
density), never correctness rules (fits, biome rules, surface check, peaceful radius, priority, earth/water fill);
rule in [structure_tuning.md](../structure_tuning.md) 7.4.8. Today that means hand-editing the JSON on a scratch
copy and remembering to put it back.

### Now
- One soft knob in config: `[worldgen.debug] structureDensity` (CEConfig `WorldgenDebug`, line ~1794), every set
  scaled (`scalePlacement`, `structure_overrides.js` line 340; warning line 985).
- No per-structure test knob; meta `share` lives in `meta_structures.json`.

### Build
1. New optional file `kubejs/generation_scripts/worldgen/structure_test_overrides.json` (shipped empty: `{}` plus a
   `_readme`), read by `structure_overrides.js` only when a new CEConfig switch `[worldgen.debug] testOverrides`
   (default false) is on. Entries:
   `{"<id or #tag>": {"frequency": x, "weight": n, "share": s}, "groups": {"<group>": {"spacing": n, "separation": n}}}`.
2. Applier: after building `groupPlans` and `metaPlans`, apply the overrides; refuse any other key with an error
   that names the 7.4.8 rule (so `biomes`, `biome_rules`, `peaceful`, `priority`, earth fill keys can never sneak
   in). Log one loud warning per world load (`TEST OVERRIDES ACTIVE: ...`) and add a report section.
3. CEConfig + `WorldgenConfig.testOverrides()` getter (patcherMod), `defineInRange`-style comment that it is for
   scratch servers.
4. Writeup: 7.4.8 points at the file; each test result line names the overrides it used.

### Ids and data
None. Files: `kubejs/server_scripts/worldgen/structure_overrides.js`, `CEConfig.java` (`WorldgenDebug`),
`worldgen/WorldgenConfig.java`.

### Decisions
- File vs config list: recommend the JSON file (tags and groups are easier there; the toml list form is clumsy).

### Gotchas
- The node sim (`tools/structure_tuning_sim.js`) and Rhino must read the file the same way (parity test).
- Ship the switch off and the file empty; `build_deploy.py` should warn if the switch is on in the staged config
  (one line in its preflight).

### Tests
- ST-221 Overrides off: file with entries, switch off -> identical sim output.
- ST-222 Overrides on: `meta/fortified_ruins` share 1.0 -> the report shows it, the warning logs once; a key like
  `biomes` is refused with the rule message and nothing is applied.

---

## structures/rates-and-groups #6: Fortified metas vs the locate timeout
Status Testing (built 2026-10-30; SQ-WG30-C04, C05 PASSED 2026-10-30; `locate #5` sets the timeout back to 120) (structure_tuning.md 3.27; timeout 300 and shares 0.25, user) · Work data / config · TODO `structures/rates-and-groups #6`

### Goal
ST-135: `/locate` of `meta/fortified_ruins` / `meta/fortified_camps` times out at 120 s; the bastille is dropped by
the peaceful radius ([plan_1.0.6.md](../plan_1.0.6.md) 2.1, [structure_tuning.md](../structure_tuning.md) 8.0.2 note).

### Now
- Shares (D1, 7.4.4): `fortified_camps` 0.20, `fortified_ruins` 0.20, `town_villages` 0.40 of
  `meta/fortified_towns` slots (Terralith's two fortified villages 1 per 109 / 150, red roof castle).
- `[worldgen.locate] searchTimeoutSeconds = 120` (config line 1580; CEConfig line 2232, range 0 to 3600),
  `loadTimeoutSeconds = 300`, `batchTimeoutSeconds = 120`. Phase 2b: 9 s per load with 4 searches, so a 120 s search
  sees about 13 slots.
- The bastille (`takesapillage:bastille`, `pillager_settlements`) is hostile; dropping it near a fortified town is the
  peaceful radius doing its job.

### Build
1. Play side: raise `searchTimeoutSeconds` to 300 (searches are non-blocking since 1.0.7, locate.md 3; a long search
   only keeps a locator thread busy). Config edit in `config/creativeengineering-common.toml` (and note it in
   locate.md's knob list).
2. Test side: use #5 (`structure_test_overrides.json`: `meta/fortified_ruins` and `fortified_camps` share 1.0, or the
   fortified villages' frequency up) for ST-135; never relax earth fill or the radius.
3. Rate side: leave the shares (a user decision, D1); revisit with #3's census.
4. Bastille: no change; note in 3.22 that it rarely stands near towns by design.

### Decisions
- Timeout 300 vs 600. Recommend 300 (a search of 30+ loads already means the structure is genuinely far).

### Tests
- ST-135 rerun with the #5 overrides: both metas answer; the log lists a fortified village with 2 to 6 ruins / 2 to
  4 camps 44 to 108 blocks out.
- ST-223 Without overrides, `/locate structure creativeengineering:meta/fortified_camps` answers within 300 s or
  reports the timeout cleanly (no stall, LO-05 style).

---

## structures/rates-and-groups #7: Ocean geodes drop does little
Status Testing (built 2026-10-22) · Work patcherMod · TODO `structures/rates-and-groups #7`

**Built 2026-10-22 (user decision), not as planned below:** no FLOOR/FIXED mode and no cancel. A moved geode's
origin goes to `lowestFloorY - clearance - maxGenOffset` (-8, 2, the config's own placement bound; amethyst 16), then
0 to `spread` 8 lower by a seed+position roll; geodes already that deep stay; `stayShare` 0.1 kept; `drop` removed.
In the `mods/` jar of 11:06 (md5 b5f60533), not scratch-tested; ST-133 rerun, scratch queue SQ-GE
(`tools/farm_worldgen_probe.js` `zfw geode`). Writeup: [structure_tuning.md](../structure_tuning.md) 2026-10-22 note.
The plan below is kept for reference.

### Goal
Geodes under oceans should stop breaking through the sea floor. The fixed 10-block drop does nothing where deep
floors sit near y 0 (ST-133 partly, [testing/structures/03_phase_3.md](../testing/structures/03_phase_3.md) 4).

### Now
- `worldgen/OceanGeodes.placeLowered` via `GeodeFeatureOceanDepthMixin` (GeodeFeature `m_142674_` HEAD): a geode
  whose sea-floor biome (`OCEAN_FLOOR_WG` column) is `#minecraft:is_ocean` places `drop` blocks lower unless the
  `stayShare` roll keeps it; skips if it would go below the world bottom.
- CEConfig `OceanGeodes` (line 2140): `drop` 10 (0 to 64), `stayShare` 0.1, `untouched` `["geode:budding_prismarine"]`.
- Phase 3 numbers: shells reaching the floor 14 of 80 ocean geode chunks with the system on vs 16 of 71 off.

### Build
1. Floor-relative mode in `OceanGeodes.placeLowered`: compute the geode's reach above its origin from its config
   (`GeodeConfiguration`: outer wall distance max + point offset max + a margin; read the exact formula from the
   decompiled 1.19.2 `GeodeFeature#place` before coding: unverified here), then
   `targetY = min(origin.y - drop, floor - clearance - reach)`.
2. If `targetY` is under `minBuildHeight + reach + 1`: cancel the geode (return false) when a new
   `cancelIfNoRoom` is true (default true), else keep today's behaviour.
3. CEConfig `[worldgen.ocean_geodes]`: `mode = "FLOOR" | "FIXED"` (default FLOOR), `clearance = 3`,
   `cancelIfNoRoom = true`; `WorldgenConfig` getters.
4. Headless check in `tools/worldgen_test/` (a fake level column: floor y 2, origin y 20 -> target below or
   cancelled).

### Ids and data
`minecraft:budding_amethyst`, `geode:budding_prismarine` (Geode Plus, untouched list). patcherMod files:
`worldgen/OceanGeodes.java`, `mixin/GeodeFeatureOceanDepthMixin.java`, `CEConfig.java` (`OceanGeodes`),
`worldgen/WorldgenConfig.java`.

### Decisions
- Retune (floor-relative) or drop the system. Recommend floor-relative with cancel: amethyst stays on land and in
  shallow seas, and deep-sea floors stop showing shells. Cancelling some ocean geodes lowers amethyst under oceans
  only.

### Gotchas
- Geode Plus geodes are not reproducible per seed (03_phase_3.md), compare distributions, not chunks.
- The decision is per geode origin column; a geode near a shore may still reach a shallower neighbour column.

### Tests
- ST-133 rerun: shells reaching the floor in ocean chunks drop to under a quarter of the off-switch count; land
  geodes unchanged; prismarine geodes unchanged.

---

## structures/rates-and-groups #8: Advanced Peripherals structures
Status Planned · Work data · TODO `structures/rates-and-groups #8`

### Goal
Advanced Peripherals' scientist houses are off by its config; the pool tag that removed them now warns
`"advancedperipherals:*" matches no pool piece` on every load. Remove the warning or the structures outright.

### Now
- `config/Advancedperipherals/world.toml`: `enableVillagerStructures = false` (since 1.0.6, commit 15c43a0).
- `kubejs/generation_scripts/worldgen/pool_tags.json` lines 25 to 34: `mod/advancedperipherals` (x0, values
  `advancedperipherals:*`) and `tech` lists `#mod/advancedperipherals`.
- `pool_tag_report.md` line 11: the warning. `pool_config.json` has no `advancedperipherals` pieces.

### Build
1. Delete the `mod/advancedperipherals` entry and the `#mod/advancedperipherals` value from `tech` in
   `pool_tags.json`. The config switch stays the single off switch.
2. Note in [structure_tuning.md](../structure_tuning.md) 3.7 (already says the config is off) that the tag is gone.
3. Rejoin: the report has no warning.

### Decisions
- Remove the tag (recommended) or keep it as a guard in case the config is turned back on. If the user wants the
  guard: keep the tag and make PoolTuner downgrade "matches no pool piece" to an info line for tags with
  `"optional": true` (small `PoolTuner` change); not worth it for one mod.

### Tests
- ST-224 `pool_tag_report.md` after a load: no Warnings section; no scientist house in 5 fresh villages (ST-12 still
  passes).

---

## structures/rates-and-groups #10: Pyramid separation
Status Testing (built 2026-10-23, deployed with 1.1.03; SQ-LE-06 not run) · Work data · TODO `structures/rates-and-groups #10`

### Goal
Double the desert pyramids' minimum separation (scratchpad 2026-10-21). Together with #2's jungle temple distance.

### Now
- Desert temples: `betterdeserttemples:desert_temple` (weight 40, frequency 0.2496, 1 per 60) and
  `minecraft:desert_pyramid` (replacement, 10 % of YUNG's slots) in `surface_dungeons` (spacing 16, separation 10:
  starts at least 11 chunks = 176 blocks apart). Jungle: YUNG's temple + `minecraft:jungle_pyramid` (10 %).
- Separation must stay below spacing, so doubling it (20) inside `surface_dungeons` (16) is impossible without
  changing every surface dungeon.
- `group/surface_dungeons` tag (`structure_tags.json` line 944): `#dungeon/surface`, `!#type/sky`,
  `!#group/witch_huts` (the witch huts were split out the same way on 2026-10-12, 3.18).

### Build
1. Tags: `group/desert_temples` = `betterdeserttemples:desert_temple`, `minecraft:desert_pyramid`;
   `group/jungle_temples` = `betterjungletemples:jungle_temple`, `minecraft:jungle_pyramid`; add
   `!#group/desert_temples`, `!#group/jungle_temples` to `group/surface_dungeons`. A replacement must be in the same
   group as the member it replaces (`replaces` + `share` move with them).
2. `python3 tools/gen_structure_groups.py`: it removes them from `surface_dungeons`, creates the two groups, fits
   each to its current rate. Then set placements by hand: `desert_temples` spacing 32, separation 20 (doubled);
   `jungle_temples` spacing 32, separation 20 (see Decisions). Re-run `--check`; the frequency that keeps today's
   rate on the bigger grid is (spacing / today's "1 per N")^2: desert 32/60 -> 0.28 (fits); jungle 32/19 -> above 1,
   so the jungle temple is capped at 1 per 32.
3. Restore the replacements by hand: sync deletes a member's entry from the group it left and, because the pack
   reads the vanilla pyramids as "converted" (`pack.converted`, `gen_structure_groups.py` line 402), re-adds them in
   the new group as weight 1 with the meta note but **without** `replaces` / `share`. Put back
   `"replaces": "betterdeserttemples:desert_temple", "share": 0.1` (and the jungle pair) and the replacement note
   (old entries: `structure_groups.json` lines 915 to 930).
4. `priority.groups`: `desert_temples` 82, `jungle_temples` 81 (not 80: equal ranks don't check each other, and the
   mimic and badlands dungeon share their biomes); `priority.peaceful.hostile` += both groups.
5. Check `--check` for the members left in `surface_dungeons`: the T&T mimic and badlands dungeon gain the slots the
   temples used to win in deserts. Lower their frequency if they jump (or let #3 handle it).

### Ids and data
Ids above (repo). New group set ids `creativeengineering:group/desert_temples`, `.../jungle_temples`.

### Decisions
- **Jungle temple distance:** separation 20 like the pyramids (recommended) or 16. With 20 its rate falls from 1 per
  19 to at most 1 per 32 (mod default 1 per 24).
- **Desert temple rate:** keep 1 per 60 (frequency 0.28 on the new grid; recommended) or let it rise to the grid's
  1 per 32.

### Gotchas
- Earlier dated notes (3.18, 3.4) describe the temples in `surface_dungeons`; update 2.2 and 3.18.
- The vanilla pyramid's "10 % of YUNG's slots" holds per group, so it follows.

### Tests
- ST-225 Groups: report lists `desert_temples` (32/20) and `jungle_temples` (32/20) with their replacements; no
  config error.
- ST-226 Distance: a census over a pregenerated desert square: no two desert temple / pyramid starts closer than 21
  chunks; `--check` desert temple 1 per 60 +-5.
- ST-227 Priority: a predicted temple drops an overlapping mimic (`[placement-priority] dropped
  towns_and_towers:mimic_desert ... overlaps betterdeserttemples:desert_temple`), never the reverse.

---

## structures/rates-and-groups #11: Lone waystones
Status Planned · Work data · TODO `structures/rates-and-groups #11`

### Goal
Too many lone waystones, deserts worst. Lower the rate, or remove the lone ones and add waystones to existing
structures.

### Now (verified in waystones-forge-1.19.2-11.4.2)
- Lone waystones are **placed features**, not structures: 4 placed features (`waystones:waystone`,
  `mossy_waystone`, `sandy_waystone`) added in Java to every biome of the allowed dimensions at
  `VEGETAL_DECORATION`; desert (`waystones:is_desert` = `minecraft:desert` only) gets the sandy one, jungles,
  swamps and mushroom fields the mossy one, everything else the plain one (`ModWorldGen`). Structure groups and the
  rates tool don't see them.
- Placement (`WaystonePlacement.isWaystoneChunk`): `frequency` = `[worldGen] frequency` in
  `config/waystones-common.toml` (line 109, **25**). A chunk qualifies when `(chunkX + offX) % 25 == 0` and the same
  for z, with offsets drawn from a `java.util.Random` seeded by `seed * chunkX*13 * chunkZ*13`, i.e. a new offset per
  chunk: not a grid. Simulated for seed 8675309 (scratchpad script): about **1 per 684 chunks** (1 per 26 x 26), but
  clustered: median nearest neighbour 7.8 chunks, 10 % within 2.8 chunks. At 50: 1 per 2,500 chunks, median 13.4.
- The rate is the same in every biome; deserts look worst because the sandy stone stands on open flat ground (and
  the row x = 0 / z = 0 chunks share one seed, a small artefact).
- `dimensionAllowList = ["minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"]`, `spawnInVillages = true`
  (one waystone piece, weight 1, injected into the 5 vanilla village house pools), `forceSpawnInVillages = false`.
- A waystone block placed by any template initializes itself (named, registered as generated) on first access
  (`WaystoneBlockEntityBase.getWaystone`), so waystones inside our own templates work. Village waystone pieces are
  any single pool element whose name contains `/waystone`; such a piece is skipped within 100 blocks of one already
  generated in this server run (`SinglePoolElementMixin`).

### Build
1. Lower: `config/waystones-common.toml` `[worldGen] frequency` 25 -> 50 (a quarter of the lone stones). Config only;
   Waystones reads it when the placement runs, but restart anyway. Remove `minecraft:the_end` and
   `minecraft:the_nether` from `dimensionAllowList` if the user wants none there (Decision).
2. Or move into structures (later, with the metas brief): `frequency = 0` (no lone stones), then:
   - a tiny structure `creativeengineering:waystone_shrine` (jigsaw, one-piece pool on the mod's own
     `waystones:village/common/waystone` / `village/desert/waystone` templates; pool element names keep `/waystone`
     so the 100-block guard applies), in no group (`enabled` only as a part);
   - a `meta/waystone_sites` tag with it, and an optional part (`chance` 0.3 to 0.5) in metas that are travel points:
     `waysign_camps`, `waysign_house_camp`, `farmers_camp`, `fortified_camps`, `town_villages` (companions excluded).
     Data only (`meta_structures.json` parts).
3. Writeup: a short "Waystones" row in [structure_tuning.md](../structure_tuning.md) 3.7 (existing configs) with the
   measured density.

### Ids and data
`waystones:waystone`, `waystones:mossy_waystone`, `waystones:sandy_waystone` (blocks and features),
templates `waystones:village/common/waystone`, `waystones:village/desert/waystone`, biome tags
`waystones:is_desert`, `is_swamp`, `is_mushroom`. Verified in the jar.

### Decisions
- **Lower vs move:** recommend lower now (frequency 50) and decide the move after the user has seen the new density;
  the move takes lone stones out of the wild entirely, which also removes their exploration value.
- **Nether and End lone stones:** keep (default) or drop from the allow list.

### Gotchas
- The `frequency` key is "approximate chunk distance", but the placement is not a grid: clusters stay at any value.
  A pack-side fix (a proper grid) would need a mixin on `WaystonePlacement.isWaystoneChunk`; not recommended for a
  decoration feature.
- Existing chunks keep their stones.

### Tests
- ST-228 Density: a pregenerated 100 x 100-chunk square (scratch, same seed) has about a quarter of the lone stones
  at 50 as at 25 (count `waystones:*waystone` blocks with a scratch KubeJS probe or a block census).
- ST-229 (move variant) a `waysign_camps` meta with its waystone part: the stone is named and usable after first
  right-click; `frequency = 0` places no lone stones.

---

## Built, needs play (Testing / Complete)

| Row | Change | Status | Note |
|---|---|---|---|
| #9 | Pool tuning switches: `[worldgen.pools] canonicalOrder` (on since 1.0.7) | Testing | **Bug found 2026-10-21**, see below |

**#9 bug: canonical order permuted the weights of duplicate pool pieces (since 1.0.7).** PoolTuner keys pool pieces
by `elementKey` and numbers duplicates `#2`, `#3` in list order (`PoolTuner.java` lines 219 to 235); `current` values
are matched by that key (lines 249 to 266). When `canonicalOrder` first sorted the pools (by key, then weight), the
`#n` numbering of same-key pieces changed, `original` was refreshed from the live pool, but `current` stayed with the
old key, so in **27 pools 223 pieces now generate with another piece's weight**. In
`kubejs/generation_scripts/worldgen/pool_config.json` (commit 908e831 on): `betterfortresses:bridge` pillars
(10/15/20 now play as 20/10/15), every `subterrestrial:cabin/*` pool (weight-5 and weight-100 variants swapped),
`nullscape:dragon_skeleton` and `_boneyard`, `unwrecked_ships:ship/sails` and `pirate_sails`,
`villagesandpillages:village_witch/road_and_house`. 27 of the 39 "Retuned pools" in `pool_tag_report.md` are these.
The only deliberate edits in the file are the 9 vinery / beautify village pieces.
Fix:
1. Data: for every piece where `current != original` and the multiset of `current` over the same-key siblings
   equals the multiset of `original` (a pure permutation), set `current = original` (a 10-line python pass; the 9
   deliberate edits are not permutations and stay).
2. Code (`PoolTuner` merge): when an old entry's `original` differs from the live piece's original and the old
   `current == old original` (never edited), take the live original; when it was edited, keep it and add a warning
   "piece order changed under an edited duplicate: check". Add a `PoolTunerTest` case (two runs, reordered
   duplicates, unedited -> follows; edited -> warns).
3. Test: after a load, `pool_tag_report.md` "Retuned pools" lists only the vinery/beautify/tech pools; ST-191 style
   check that a seed's villages still repeat across restarts.

## Writeup corrections made

- [structure_tuning.md](../structure_tuning.md) 3.7: Advanced Peripherals row said `enableVillagerStructures = true`;
  the config is false since 1.0.6. Row rewritten.
- [structure_tuning.md](../structure_tuning.md) 2.4: "57 tech pieces ... Advanced Peripherals 5" -> 52, AP off by its
  config (matches `pool_tag_report.md`: `tech x0 (52 pieces)`).
- [structure_tuning.md](../structure_tuning.md) 3.23 intro: "None is in `mods/` or a deploy yet" contradicted its own
  heading (DEPLOYED with 1.0.7); now says shipped in patcherMod 1.0.7 / pack 1.0.7.
- [structure_tuning.md](../structure_tuning.md) 7 Todo, fresh watchtower: "from 1 per 75" -> 1 per 83 (the 2026-10-14
  estimates; 75 at the 3.20 fold).
