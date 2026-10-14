# M1: plan 7.4 built (offline only), 2026-10-14

Milestone 1 of the structure testing run ([00_plan.md](00_plan.md)). The plan itself is
[structure_tuning.md 7.4](../../structure_tuning.md); the live notes are
[01_build_7_4.scratch.md](01_build_7_4.scratch.md). Nothing went on a scratch server in this milestone; nothing was
installed into `mods/`, deployed or committed.

## 1. Outcome

All of 7.4.1 to 7.4.7 is built and every offline check passes. patcherMod 1.0.6 was rebuilt in `build/libs`
(2026-10-14 01:28) for the `biome_frequency` code; it is not in `mods/`. Next: M2, tests ST-170 to ST-190 on a scratch
server.

**User constraints added during the milestone (2026-10-14):**
1. Nether and End spawning are out of scope from now on (piglin camp, pig ranger, ST-186 and the Nether/End part of
   ST-171 are skipped).
2. Structure-to-structure rates are out of scope from now on: no more rate tuning, estimates or rate tables; the rate
   parts of tests (ST-181's split and `--check`, ST-185's harsh/ordinary ratio) are skipped. Correctness only.
3. Sleep 30 minutes once the 7.4 changes are complete (an hour instead if the token limit gets close, and then skip
   the 30 minutes).

## 2. What was built

| Step | What | Files |
|---|---|---|
| 7.4.1 | Campsite Structures' 6 camp features stopped (`forge:none`) and rebuilt as real jigsaw structures; all 6 templates hold only the passive camper, so all are passive (`remote`, `meta/friendly_camps`) | `kubejs/data/campsite_structures/` |
| 7.4.2 | Camps. Castles. Carriages. 2.3.4 ported to `kubejs/data/eugene` (64 files); the 1.1.1 jar moved to `disabledmods/archive/` | `tools/port_structure_jars.py`, `kubejs/data/eugene/`, `kubejs/data/minecraft/structures/pillager_outpost/` |
| 7.4.3 | `eugene:camp` split: trader camp + lost llama (passive), new `eugene:pillager_camp` (hostile, `meta/pillager_camps` anchor), new `eugene:fake_trader_camp` (hostile, no meta tag) | eugene worldgen, `structure_tags.json` |
| 7.4.4 | `eugene:castle` split: `undead_castle` (surface dungeon), `red_roof_castle` (big settlement, `meta/fortified_towns`), `ruined_drum_tower` (ruin); fortified-town shares 0.20 / 0.20 / 0.40 | eugene worldgen, `meta_structures.json` |
| 7.4.5 | New `group/vehicles` (6 siege weapons, loot carriage, witch wagon): hostile, rank 76, 4-chunk settlements exclusion, 52 / 16 grid; new group knob `biome_frequency` (harsh 1.0 / default 0.5) in Java (`StructureFrequencies`, `/locate` prediction), JS (validator, staging, report), the node sim / Rhino parity and the python estimator | patcherMod, `structure_overrides.js`, `structure_groups.json`, `biome_fits.json` (`harsh` tag), tools |
| 7.4.6 | Piglin camp: no code change (see 4.3) | none |
| 7.4.7 | Rescan, tags, sync, fits; FitTest count 167 -> 181; loot catalog regenerated; 2.2, 3.16, 3.22, 7.4 and 8.0 of structure_tuning.md updated | as listed |

**Port details (7.4.2).** Remaps: diagonal fence and diagonal pane -> vanilla, chiseled bookshelf -> bookshelf,
decorated pot -> Supplementaries urn, mangrove hanging sign -> a hanging lantern (it hangs under a chain where a wall
sign would have no wall; the plan offered a wall sign or air), suspicious gravel/sand -> plain gravel/sand. Stripped:
the red roof castle's zombie (D5), 6 `naturalist:firefly` and 1 `wildlife:lark`. The `ruined_equipment` axe was dropped
from the carriage's item frame. Loot: the archaeology table is not copied (1.19.2 has no such loot type), the snout
trim entry is dropped. Functions: `summon_pig_ranger` mounts via `Passengers` (and a `DeathLootTable` typo in it is
fixed); Ser Marrow's steed is summoned carrying a fresh skeleton that copies the old one's attributes, gear and name
(`mount_ser_marrow`); the steed's `damage` line became `effect give ... wither`. Porter fixes on the way: `minecraft:`
templates (vanilla overrides) were skipped as unreferenced, and vanilla pool entries were pruned when a jar ships
`data/minecraft`.

## 3. Offline checks

| Check | Result | Evidence |
|---|---|---|
| gradle build | PASSED | `creativeengineering-1.0.6.jar` 01:28; no source change since |
| `tools/test_structure_groups.sh` | PASSED | node sim: 15 groups, 178 sets, 30 fits for 181 structures, 22 metas, 242 ranked; Rhino output identical to node (also at density x4, x0.3, groups off; biome frequency tables identical); old-jar cases; FreqTest 28/28 incl. the new biome-factor checks (prediction == generation 6000/6000) |
| `tools/test_worldgen_tools.sh` | PASSED | GroundMatch 37, MassPlacer 30, EarthFill 53, density 264, TerrainAdaptation 50, MetaTest 70, FitTest 30, Locate 28 |
| Template scans | PASSED | terrain adaptation, Nether blocks (tier 1 in 15 live structures, was 13), meat blocks |
| Peaceful / hostile lists (node sim, before vs after) | PASSED | peaceful 96 -> 104: + red roof castle (8), `eugene:camp` and the 6 Campsite camps (3). Hostile 65 -> 70: + undead castle, pillager camp, fake trader camp, loot carriage, witch wagon. None in both; drum tower and piglin camp neither. Exactly the 7.4.7.5 list |
| New structures' biomes (staged fits) | PASSED | no ocean, river, cave, Nether or End biome in any new Overworld structure's list; fisher camp on 3 beach biomes; witch wagon dark forest + swamp (11 biomes) |
| Loot catalog | PASSED | 405 tables in 50 namespaces (was 386 in 48: `eugene`, `subterrestrial`); no new dead table reference; no snout trim |

## 4. Findings and choices to review

1. **Subterrestrial** (8 underground cabin sets, jar added 2026-10-13) showed up in the rescan. It is only classified
   (`mod/subterrestrial`, `type/underground`, which keeps the ocean cabin's ocean biomes) and stays standalone at the
   mod's rates. Not part of 7.4.
2. **`--fit-rates` drift.** A full `--fit-rates` also re-fits about 75 existing structures (40 by more than 5%, e.g. the
   T&T desert outpost weight 33 -> 159); the same happens on the pre-7.4 configs, so it is old drift, not 7.4. Only the
   new members' fitted values were kept; every existing entry was restored. Left for the audit.
3. **Piglin camp (7.4.6).** Earth fill only counts dirt, sand, gravel, clay and path as earth, and lava counts as solid,
   so it never sees the camp's netherrack/nylium floor; "treat lava like water" would change nothing. What keeps the
   camp off the lava is the mod's own `beard_box` terrain adaptation (fill below, carve the box). Now out of scope (Nether).
4. **Vehicles vs camps vs outposts.** "Camps > vehicles > outposts" can't hold in ordinary biomes, where outposts are as
   dense as camps or denser (plains: both about 1 per 39 chunks). Vehicles were set rarer than camps everywhere but
   Terralith's lavender forest. Now out of scope (rates); the values stand until the audit.
5. **Campsite cleric camp** chest rolls `minecraft:entities/witch` (the mod's template); harmless, noted for the loot
   audit.
6. **Fake trader camp and the tick function** perf note: the mod's `tick` runs `execute as @e[type=wandering_trader]`
   every tick (not changed; for perf.md).

## 5. Values changed and restored

No config value was changed for testing. The pre-7.4 configs (structure tags, groups, meta, fits, config, estimates,
tag report) are backed up in the build session's scratchpad (`m1/backup/`).

## 6. Not done (and why)

- `structures_by_biome.md` not regenerated: it is a per-biome rate table (constraint 2).
- 3.16's "Est. 1 per" for the three fortified metas marked "not re-estimated" instead of new numbers (constraint 2).
