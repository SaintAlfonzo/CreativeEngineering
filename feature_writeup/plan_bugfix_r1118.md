<!-- SPDX-License-Identifier: GPL-3.0-only -->
# Bug fixes from scratch run r1118 (2026-11-18)

<!-- generated:status_index begin open_work sha1=dee915d6a6f2 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 7 open TODO rows (Planned 1, Testing 6), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `performance-and-server #1` | Planned | S | Boot log noise cleanup: 91 failed third-party compat recipes for absent mods |
| `conventions #38` | Testing | SQ-HH-1269 | Jar loot modifiers never run: Creative Loot GLM merge mixin matches nothing |
| `creative-delight-members #1` | Testing |  | Stale incubation_additions in the mod_names config |
| `crops #58` | Testing | SQ-SY-21 | Botany Pots growth times back at mod defaults |
| `generators-and-tests #19` | Testing | SQ-WCT-05 | cdevtest rdetail shows no ingredients for smithing and IE sawmill recipes |
| `recipes/metal #3` | Testing | S P | Rose gold in gold tags |
| `terrain-adaptation #1` | Testing | SQ-CBA-18 SQ-CBC-04 | Ported structure datapacks dropped from structure tags as absent mods |
<!-- generated:status_index end open_work -->

Status: **built, waiting on reruns** (2026-11-18). B0 to B8 are done (section 4); every fix waits on its rows in the
R7 rerun of [plan_rowfix_r1118.md](plan_rowfix_r1118.md). `performance-and-server #1` (F4 log noise) is a later
cleanup, not in this plan's scope. Source: [scratch_run_2026-11-18.md](testing/scratch_run_2026-11-18.md) sections 8 to
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
- B1 (2026-11-18): `javap -p` on forge-1.19.2-43.3.13-universal.jar: `LootModifierManager` has `apply(Map, ResourceManager,
  ProfilerFiller)` (Forge's own name, kept in production) and the bridge `m_5787_(Object, ...)`; the old target
  `m_5787_(Map, ...)` matched neither. Inject retargeted to `apply(Map, ...)`, `injectors.defaultRequire 1` added.
  Changelog: none. The miss came in with 89a25077 (2026-11-17), after 1.2.02 shipped, so no released version lost the
  jar loot drops; the plan's "yes" assumed a player-visible regression.
- B2 (2026-11-18): `global.has.datapack(ns)` asks Common's `DataNamespaces`, filled at the head of
  `ReloadableServerResources.loadResources` (m_206861_; KubeJS has already added its packs to the manager there).
  Narrowed against the plan: the tag helper keeps a datapack namespace only in worldgen tags (`event.getType()`
  contains `worldgen/`). Item and block tags keep the mod rule, because jars ship optional compat data under absent
  mods' namespaces and a kept unknown item or block would fail the whole tag.
- B3 (2026-11-18): cause found at lead (c). KubeJS 1902.6.2 `RecipeJS.createRecipe` returns the original `Recipe` when
  `hasChanged()` is false and the original was already parsed; `JsonRecipeJS.hasInput` parses it (`getOriginalRecipe`)
  for every `{ input }` filter, such as `replaceItems`' item-merge pass. So a bare `r.json` edit is dropped once any such
  filter has touched the recipe. Fixed in the helper: `setCookTime`, `scaleRecipeInt` and `setRecipeNumber` set
  `r.changed = true`. Users: botany_pots.js (scale, set) and fruitsdelight/recipes.js (jelly cook time 400). Changelog:
  none. 1.2.02 had working pot times; the regression and the unreleased soil bullet sit in the same cycle, and the
  working list already describes the fixed state.
- B4 (2026-11-18): `i.raw_pasta` -> `i.pasta`; Slice and Dice builds its copy from the cooking recipe after KubeJS, so it
  follows. CD rebuilt. Changelog bullet under Hearth and Harvest.
- B5 (2026-11-18): the entry was only in the toml overrides (and its configs_edited copy), not in Common's default list.
- B6 (2026-11-18): `rdetail` prints smithing `base:` / `addition:` (UpgradeRecipe fields by SRG name) and a public
  `input` Ingredient (IE sawmill) when `getIngredients()` is empty; a `part=fluids` line with amounts; a `part=numbers`
  line (growth ticks, growth modifier, cook time, processing duration) so B3 checks can run without a pot; `cdevtest tag`
  takes kind `structure` (for the B2 row).
- B7 (2026-11-18): rows handed off (F2 client names, F5 the 9 over-limit Create recipes, F6 CS-03 at 4x). The
  `armorCap` comment says a full dragon set is 40 (Dragon Loot config); Equipment rebuilt; the toml comment edited in
  `config/` and `configs_edited/`.
- B8 (2026-11-18): rose gold block added to block `forge:storage_blocks/gold` (`tags/storageblocktags.js`). Audit of every
  recipe that reads the gold block (jar recipe JSONs scanned for `#forge:storage_blocks/gold` and literal
  `minecraft:gold_block`, plus pack scripts). `rose_gold_interchange.js` swaps a jar recipe's literal gold block for
  the tag unless the output is in `goldOnly`; script-added recipes are not reached by that swap.

| Recipe | Reads | Ruling | Result |
|---|---|---|---|
| vanilla gold ingot from gold block; Create Recycle `create:crushing/gold/scrap_gold_block` (-> 9 gold ingots) | literal | gold-only (makes gold) | stays literal (`goldOnly` ingot) |
| pack: gold bars laser cutting, gold block -> crushed gold block, gold block -> gold dust pile (`metal_building.js`, `metal_base_materials.js`) | literal, script-added | gold-only (makes gold forms) | stays literal |
| pack: gilded blackstone deploying (`misc_metal_crafts.js`) | literal, script-added | gold-only (gilded blackstone drops gold nuggets: rose gold -> gold) | stays literal |
| Create Deco `gold_sheet_metal_from_gold_block_stonecutting`; Design Decor `gold_cyllinder`, `gold_boiler_large` stonecutting | literal -> was swapped to the tag | gold-only (gold reshaped, output is gold) | `goldOnly` gains `i.gold_sheet_metal`, `i.gold_boiler`, `i.gold_boiler_large`: literal again |
| pack: Golden Watering Can smithing (`hearthandharvest/recipes.js`) | literal, script-added | material | `i.any_block_gold` (tag) |
| Handcrafted golden thin / medium / thick / wide pot; Dustrial Decor large gold chain; Design Decor capitalism block; MCA whistle; Yuushya shimmering pearl | literal -> swapped to the tag | material | tag (unchanged) |
| CC: Tweaked advanced turtle upgrade; PneumaticCraft weighted gun ammo; Sophisticated Backpacks stack upgrade tier 2 | tag | material | tag (unchanged) |
| Quark raw gold block / bricks smelting and blasting; Fortune on Netherite gold block smelting | output only | n/a | unchanged |

  Changelog: a Rose Gold family (the earlier golden-items bullet moved into it) with the watering can and the decor
  cuts.
