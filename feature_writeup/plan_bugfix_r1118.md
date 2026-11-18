<!-- SPDX-License-Identifier: GPL-3.0-only -->
# Bug fixes from scratch run r1118 (2026-11-18)

<!-- generated:status_index begin open_work sha1=ab368c33b6a6 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 7 open TODO rows (Bug 6, Planned 1), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `conventions #38` | Bug | SQ-HH-1269 | Jar loot modifiers never run: Creative Loot GLM merge mixin matches nothing |
| `creative-delight-members #1` | Bug |  | Stale incubation_additions in the mod_names config |
| `crops #58` | Bug | SQ-SY-21 | Botany Pots growth times back at mod defaults |
| `generators-and-tests #19` | Bug | SQ-WCT-05 | cdevtest rdetail shows no ingredients for smithing and IE sawmill recipes |
| `recipes/metal #3` | Bug | S P | Rose gold in gold tags |
| `terrain-adaptation #1` | Bug | SQ-CBA-18 SQ-CBC-04 | Ported structure datapacks dropped from structure tags as absent mods |
| `performance-and-server #1` | Planned | S | Boot log noise cleanup: 91 failed third-party compat recipes for absent mods |
<!-- generated:status_index end open_work -->

Status: **planned, not built.** Source: [scratch_run_2026-11-18.md](testing/scratch_run_2026-11-18.md) sections 8 to
10 (findings F1 to F12) and the classification [testing/r1118/classes.csv](testing/r1118/classes.csv). Row-side work
(broken test rows, driver limits, old-save rows) is the sibling plan [plan_rowfix_r1118.md](plan_rowfix_r1118.md).
Status lives in the TODO rows; code facts below were read 2026-11-18, re-read before editing.

## 1. Intake

| # | Item (owner's words / run finding) | Class | Note |
|---|---|---|---|
| 1 | "build 2 plans, 1 for fixing the discovered bug fixes" | clear | this file |
| 2 | F10 Creative Loot GLM mixin matches nothing | premise, confirmed | `LootModifierManagerMixin.java:29` targets `m_5787_(Ljava/util/Map;...)V`; `creativeloot.mixins.json` has no `injectors.defaultRequire` |
| 3 | F1 ported structure datapacks dropped from tags | premise, confirmed | `tag_helper.js:84` drops an entry when `!global.has.mod(ns) && !_inRegistry`; the structure datapack namespaces are not mods and the tag event's registry ids do not list them |
| 4 | F12 Botany Pots growth back at defaults | premise, confirmed (cause open) | `creativecompat/kubejs/other/botany_pots.js:48-66` still runs and logs 175 changed; the edited JSON does not reach the loaded recipes |
| 5 | F7 macaroni and cheese takes only FD raw pasta | premise, confirmed | `kubejs/data/creativedelight/kubejs/hearthandharvest/recipes.js:64` uses `i.raw_pasta`; `i.pasta = '#forge:pasta'` (item_dict.js:2124) |
| 6 | F3 stale `incubation_additions` in `[mod_names]` | premise, confirmed | `config/creativecommon-common.toml:90` overrides list (and its `configs_edited` copy) |
| 7 | F9 orphan transitional `zucchini_quiche` | premise, confirmed -> ruled | `cd_transition_items.js:60`; owner: keep it (section 3) |
| 8 | F11 `rdetail` prints `ingredients=-` for smithing and IE sawmill | clear | Creative Devtools Java, dev-only |
| 9 | F8 rose gold block missing from the gold block tag | conflict -> ruled | judge-8 GAP-04 recorded "block tag gold-only by design", but the row's own title and `recipes/metal #3` want rose gold in the block tag; owner ruled add it (section 3) |
| 10 | F4 failed recipes 14 -> 91, all compat recipes for absent mods | open -> ruled | later cleanup row `performance-and-server #1` (low) |
| 11 | F2, F5, F6 checks | clear | queue rows, no code yet |
| 12 | 14 unjudged rows (steps 142 to 149) | clear | judge first: may add findings |

## 2. Phases

Short names: CC = `development/creativeCommon`; CL = `development/creativeLoot`; CD = `development/creativeDelight`;
CPT = `development/creativeCompat`; DT = `development/creativeDevtools`.

| Phase | Row | Fix | Jar / reload | Tests | Changelog |
|---|---|---|---|---|---|
| B0 | (none yet) | Judge the 14 unjudged rows from `perf/runs/r1118/s142_res` .. `s149_res` per [r1118/BRIEF.md](testing/r1118/BRIEF.md): HH-319, -320, -328 (salt cave, corn maze), HR-05 (wild raspberries), IWF-03, META31-01, -09, CDW-01, -03, -03b, MIN-01, -03, -07, NSM-06. A real bug becomes a B row here; a row fault goes to plan_rowfix_r1118.md | none | — | — |
| B1 (high) | `conventions #38` | Read the real method set of `LootModifierManager` in the Forge 43.3.13 jar (`javap -p`) first; retarget the inject to the non-bridge `apply` (or inject into the bridge and cast), and add `"injectors": {"defaultRequire": 1}` to `creativeloot.mixins.json` so a future miss fails at load instead of silently | CL rebuild, restart | SQ-HH-1269 rerun; every GLM row (grep `glm_entries` in rows) | yes: jar loot drops restored (pitchfork straw, corn husk, Brewin', Ends, Rustic, Veggies, Vintage, Vinery, Exposure) |
| B2 | `terrain-adaptation #1` | Keep an entry whose namespace ships data in a loaded pack: add `global.has.datapack(ns)` (Common, from the server resource manager's namespaces, cached once per reload) and use it in `_ents` beside `global.has.mod`. Count kept, so `absent_mods.js` stops listing the 44 structure namespaces | CC rebuild, restart | SQ-CBA-18, SQ-CBC-04; new row: `creativeworldgen:terrain_adaptation/*` holds `forest_ruins:ruin_1` | yes: ported structures get terrain adaptation and template trim again |
| B3 | `crops #58` | Diagnose, then fix. Leads, in order: (a) the script moved into Compat's script pack 2026-11-17 (commit 91d2ffb5); check whether a later handler (Compat or Botany Pots itself) re-reads or re-adds `botanypots:crop` recipes after ours; (b) whether `NCH.forPack('creativecompat')` hands the helpers the same event; (c) whether KubeJS 1902 keeps `r.json` edits for recipes it did not mark changed (compare with the r1115 tree, where the same helpers worked). Fix in the helper if (c), so every `scaleRecipeInt` / `setRecipeNumber` user is covered | CC or CPT rebuild | SQ-SY-21; grep other `scaleRecipeInt` users and add one row each | yes: Botany Pots growth times restored (fixed) |
| B4 | `dishes/meals #38` (log) | `recipes.js:64` `i.raw_pasta` -> `i.pasta`; check the Slice and Dice copy is generated from it (else fix its line too) | `/reload` | SQ-CM-04 | yes: macaroni and cheese takes any pasta |
| B5 | `creative-delight-members #1` | F3: drop `incubation_additions=...` from the overrides in `config/creativecommon-common.toml` and `configs_edited/root_config/`; also from Common's built-in default list if it is there. F9 dropped: the transitional zucchini quiche stays (owner, 2026-11-18) | CC / CD rebuild if defaults change; restart | boot log has no `[mod_names]` WARN; SQ-CDF-07 still PASS | no (log only) |
| B6 | `generators-and-tests #19` | `rdetail`: read smithing `base` / `addition` and IE sawmill input; add the fluid amount to fluid ingredients (needed by SQ-RSP-09, row plan R5) | DT rebuild | SQ-WCT-05, SQ-RSP-09 | no (dev tool) |
| B7 | new rows | Checks: F2 Miner's Delight names on a client (client row); F5 Create "more inputs/outputs" for 9 recipes: one row that crafts each; F6 calendar fertile bonus rerun with 4x the sample; stale config comment "a full dragon set is 36" in Equipment's `armorCap` (the set is 40, see row plan R1) | none / CE Equipment text | new SQ rows | no |
| B8 | `recipes/metal #3` | F8: add the rose gold block to block `forge:storage_blocks/gold` (through `global.tags`). Then audit every recipe that reads `#forge:storage_blocks/gold`: one where gold is logically the only answer (gold block <-> ingots, any recipe whose output is gold itself) stays literal `minecraft:gold_block` (extend `goldOnly` in `kubejs/data/creativeengineering/kubejs/metal/rose_gold_interchange.js:13-33`); one using gold as a material takes the tag. F4 is not in this plan: `performance-and-server #1` | CE script `/reload`; tag needs a restart if set in startup | SQ-GAP-04 (restore its block-tag check, see row plan R1) | yes: rose gold block counts as gold in block recipes |

Order: B0 first (it may add rows), then B1 (high, one jar, unblocks every GLM row), B2 and B3 (both Common-side; one
Common rebuild if B3's fix is in the helper), B4 and B5 (scripts and config), B6, B7. One rerun of the touched rows at
the end, together with the row plan's rerun pass (plan_rowfix_r1118.md R7).

## 3. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| 1 | F8 rose gold block in the gold block tag | conflict (GAP-04 judge: gold-only by design) | "add the rose gold block, ensure logically gold only recipes still only gold, but gold as material can use both" | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 2 | F4 91 failed compat recipes for absent mods | open | "leave for now, mark as a later log noise cleanup task": row `performance-and-server #1` | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 3 | B2 approach: `global.has.datapack(ns)` from the resource manager | clear | agent default, unconfirmed | 2026-11-18 | this plan |
| 4 | B5 removing the transitional zucchini quiche item | clear | "keep zucchini quiche": not removed | 2026-11-18 | chat 2026-11-18 |

History: row 4 was an agent default (remove the item) until the owner ruled keep, 2026-11-18.

## 4. Phase notes

- B0 (2026-11-18): judged as [testing/r1118/j15.md](testing/r1118/j15.md): 5 PASS, 9 BATCH-FAULT, 0 real bugs. Row
  text for HR-05, HH-328, META31-09, META31-01 and NSM-06 handed to the coordinator for the row plan (rows are not
  edited by this plan); HH-319/-320 wait for multi-boot, CDW-01/-03/-03b and MIN-03 for reduced mod sets (row plan R4).
