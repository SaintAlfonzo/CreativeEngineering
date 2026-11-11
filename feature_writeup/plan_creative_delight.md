# Creative Delight execution plan (2026-11-02)

Status: G steps 1 to 3 BUILT 2026-11-02 (see section H); fix plan I built 2026-11-02 afternoon (I progress), rerun queued; plan J and the remaining open rows built 2026-11-02 night (section K), not booted. **Deep scan 2026-11-02:** the installed CD jar (11-02 14:01) nests Veggies `-05`, Vintage `-13`, Fungi `-02`, Miner's `-05`, Display `-03` (later than the H and I progress lists); the 21:10 build (copper cup removal) is not installed. Rerun status (r1102b, judged): SQ-CDJ-12 (I8) and SQ-MDP-18 (I7) PASS, SQ-VTD8-02 (b) PASS; the other I reruns (SQ-CDJ-05 / SQ-DDP-01, SQ-MDP-13, -14, -16 / -09, SQ-LOOT-FD1, SQ-VTD8-01 rewrite) were not in the r1102b scope, and SQ-FGD-14 / SQ-FGDC sit in its postponed worldgen slice. Only owner decisions already on record are listed as work; open questions are listed in
section F and not resolved here. Row ids refer to [todo/TODO.md](todo/TODO.md); background is
[creative_delight.md](creative_delight.md) (sections 35 to 38). Every change that touches game data adds its live checks to
[testing/scratch_queue.md](testing/scratch_queue.md) (no scratch boot until the owner says so) and a bullet to
`kubejs/assets/creativecommon/changelog/unreleased.md` where a player can notice it. Hand-edited configs are copied to
`configs_edited/root_config/`. Never replace a jar while the game runs.

## 0. Standing rules for this plan (owner)

| # | Rule | Source |
|---|---|---|
| R1 | Display Delight becomes a Creative Delight member (mod id `displaydelight`, display name "Display Delight (Creative Delight)"). | Owner 2026-11-02 |
| R2 | Recipes written by Creative Delight use the recipe namespace `creativedelight`, for the new member and for every member row that still writes another namespace. | Owner 2026-11-02; framework Q24 |
| R3 | Creative Delight has its own config (`creativedelight-common.toml`) and handles intra-mod changes. A setting that can be isolated to one mod, or that belongs to a backport or rebuild, lives in the CD config, not in the pack's config or the port's own file. | Owner 2026-11-02 |
| R4 | A member needs framework #2 (install), #4 (dict) and #6 (move-diff harness) first. | creative_delight.md |
| R5 | Hard cut, no deprecation step; old ids load through the swap lists. | Q22 |

## A. Display Delight as a member (new, `members #35`)

Source: owner 2026-11-02; port facts in [display_delight.md](display_delight.md) (`backportedDisplay-1.7.0-ce-19.2-01`, 210 blocks, SQ-DDP / CQ-DDP).

| # | Step | Detail |
|---|---|---|
| A1 | Land the port's open work | display_delight.md 5 (silk-touch default, creative tab) are kept-as-is notes; SQ-DDP and CQ-DDP are still queued. Run them, or carry them into SQ-CDJ-05, before freezing |
| A2 | Move the module | `development/backportedDisplay` -> `development/creativeDelight/members/display/` with the `ce.forge-repack` shape of `members/ends/`; `bundled_in=creativeDelight`, `mod_name=Display Delight (Creative Delight)` (was " (Backport)"), version bump; root `settings.gradle` and the member map in `creativeDelight/build.gradle` get the row; build and install through `:creativeDelight:` only |
| A3 | Jar bookkeeping | glob into CD `replaces_jars`; drop the `IN_TREE_MODS` row; one `build_deploy.py` row; `mod_sides.md` row merged (both sides); `installToMods` moves the old jar to `jar_archive/displaced/` |
| A4 | Names | grep `namechanges.js`, `jeed_providers`, tooltip scripts and `CounterToppers` / `FeastPlatingCategory` for the "(Backport)" name; the lang override with Display's placed-food names stays |
| A5 | Namespace | Display registry namespace `displaydelight` and block states unchanged (old worlds load). Pack rows that write Display recipes move to the CD trees under namespace `creativedelight` (R2). Port-generated recipes (`md_` links to the Miner's port) are checked for the same rule |
| A6 | Config | Display's settings (silk-touch food drop, plate hide / restore, take-all, particles, support legs) get keys in `creativedelight-common.toml` under `[members.displaydelight]` or `[display]`; the old port config is read once by `ConfigRename.mergeSections` and not kept (R3) |
| A7 | Member switch | `displaydelight = true` in CD `[members]`; off skips the CD script rows for Display, as for the other members |
| A8 | Tests | SQ-CDJ-05 gains Display (all ids load once; an old world with placed plates loads); SQ-DDP rerun against the nested jar; CQ-DDP client checks |
| A9 | Docs | creative_delight.md 38.4 (written), 38.2 table row changed, README row, display_delight.md header, `members #1` list now five ports |

## B. Namespace and config work (R2, R3)

| # | Row | Change |
|---|---|---|
| B1 | `framework #5` | `creativedelight-common.toml` is the single config for CD. Add a per-member section for settings that touch one mod (today: `[members]` switches only). Settings of a backport or rebuild member move in when the member is bundled (`members #1`, A6, `members #33`) |
| B2 | `framework #5` | Pack-config keys that only one CD member uses are listed (grep `creativeengineering-common.toml` `[cooking]`, `[crop_growth]`, `[dandelion_maturing]`, `[crop_drops]`, `[stem_fruit]`, `[canning]`) and each is sorted as: CD-owned (move) or shared (stay). The list is the deliverable; moves follow `ConfigRename.mergeSections` with the old file read once |
| B3 | Q24 | Recipe namespace for CD calls: `creativedelight:` on every NCH call from a CD script. Open detail (per call or scoped) is in section F |
| B4 | `framework #6` | The move-diff harness treats `creativedelight:` as the allowed namespace change and fails on anything else lost |

## C. Decided, ready to build

| # | Row | Work | Notes |
|---|---|---|---|
| C1 | `members #33` | Ender's Delight 1.2.2 as the donor-route member `portedEnders` (mod id `endersdelight`). Step 1: class and registry inventory of the installed 1.2.2 jar (items, blocks, block entities, recipes, loot, tags, lang, mixins) against pack scripts, dict rows, `end.js`, shield and damage tags and the `enders_additions` member | creative_delight.md 38.1; owner 2026-11-02. Same method as Oceanic (section 36). Steps 2 to 5: member code, fold 1.1.4 content in, standalone jar leaves `mods/` (`replaces_jars`, `UPSTREAM_DONORS`), SQ-DNR rows (missing and wrong jar, old world with End plants) |
| C2 | `members #9` | Fold `enders_additions` (1.1.4 donor) into C1; one display name "Ender's Delight (Creative Delight)" | Done together with C1 |
| C3 | `members #10` | Cutover release: one `build_deploy.py` row, `mod_sides.md` rows merged, `installToMods` displaces old jars, changelog bullet for the jar swap and the "(Creative Delight)" names, writeups. Ships in the next deployment, unless a fix has to go first; Creative Split is 1.2.00 | Never while the game runs. Needs `members #1` and A |
| C4 | `members #30` R3 | Dandelion juice / tea follow the drink rules ([creative_delight_drinks.md](creative_delight_drinks.md) 3, 4, 6, 7) with `merges/drinks #1` and `food/drinks #15` | R7 (pancakes) is not in this item |
| C5 | `members #18` | Rustic fried foods and calamari: no work. Re-check only if `merges/dishes #11` (tempura) or `members #24` (calamari swap) changes the set | |
| C6 | `members #15` | Pancake art from Supplementaries is approved and built; nothing further until F2 | |
| C7 | `food/drinks #15` | Build the drink rules (the CD drinks writeup is answered in full) | Rows that move into CD trees use namespace `creativedelight` (R2) |

## D. Built, waiting for install or tests

No decision is open on these; each needs a jar copied to `mods/` or a test run (scratch queue, no boot until the owner says so).

| # | Row | Waiting for |
|---|---|---|
| D1 | `members #31` | Miner's port `-02` (`build/libs/` only): install; copy `miners_upstream/` 1.1.2 jar to the server by hand; SQ-MD checks |
| D2 | `members #32` | Vintage port `backportedVintage-0.1.6-ce-19.2-10` install; SQ for Mixed Seeds |
| D3 | `members #11` | Coffee merge: CD and CB jars built, not installed; SQ-RDX-01 and following |
| D4 | `members #23`, `#24`, `#28`, `#29` | Open client / server checks SQ-CM-32, -33, -34, -35, CQ-UI-02 |
| D5 | `members #14`-`#17`, `#19`-`#22`, `#25`, `#26` | Rustic: SQ-RD, SQ-RDX-01 to -14 |
| D6 | `members #7`, `#9`, `#6` | End's SQ-EN-05, -08 and the earth-fill half of -07; Oceanic donor checks SQ-DNR |
| D7 | `members #2`-`#5`, `#12`, `#13` | Moved rows: SQ-CDM queued runs |
| D8 | `qol #1`-`#6`, `framework #5`, `#8` | SQ-CDQ-03 (boot without Farmer's Respite) and -04, per-key switch checks (SQ-CDQ); headless check that CD boots with each optional mod absent |
| D9 | `framework #1` | SQ-CDT-07, SQ-CDJ-03, -04, SQ-FN-02, -03; SQ-CDJ-02 rewritten, not rerun |
| D10 | `framework #3`, `#7`, `#10`, `#11`, `#12` | Offline passes recorded; in-game confirmation after the next install |
| D11 | `swappers #1` | First swap lists in Testing; SQ for the id remap on a fresh old-id save |

## E. Partly built, to finish

| # | Row | Left |
|---|---|---|
| E1 | `framework #4` | `tool_dict.js`, `misc_dict.js`; the Veggies pilot and recipe diff (needs E2) |
| E2 | `framework #6` | The dump half of the move-diff harness: registry names, recipe ids and counts, tags, loot table ids |
| E3 | `members #6` | Guardian food (Elder Guardian Eye vs Ocean's and Hostile Delight lines); waits on `mod-additions #5`, see F |
| E4 | `members #25` | Final art for the Glowing Calamari Crate (placeholder in) |
| E5 | `swappers #3` | World half and triggers in patcherMod (`onWorldLoad`, `onPlayerDataLoad`, `onContainerOpen`, `onCommand`, `onceWhenUpdated`) |
| E6 | `swappers #2` | Script half (recipes, loot, tags) feeding `global.itemMerges`; follows `swappers #1` |
| E7 | `members #1` | Freeze and bundle the ports: Veggies, Vintage, Fungi, Miner's, plus Display (A). Land each port's open work first: Fungi SQ-FGD, Miner's SQ-MDP / SQ-MD-01, `vintageport #18` and `#4`. Then per port: `bundle_members`, `bundled_in=creativeDelight`, display name, version bump, `replaces_jars`, drop `IN_TREE_MODS` row, grep old names. SQ-CDJ-05 |
| E8 | `members #34` | The survey is done (38.2); the per-mod calls are F1 |

## F. Open decisions, not made here

| # | Question | Row |
|---|---|---|
| F1 | Which of the other cooking mods become members, donors or stay separate (Hostile, Chefs, Engineers, KubeJSDelight; Fruits, Respite, Corn, More, Abnormals, Alex's, Quark, Storage). Per-mod licence check first | `members #34` |
| F2 | Stackable Pancake as family base; blocks the pancake family rule and Veggies R7 | `merges/baking #2` |
| F3 | Guardian food lines | `members #6`, `mod-additions #5` |
| F4 | Final crate art | `members #25` |
| F5 | NCH namespace option shape (per call or scoped); confirm the QoL key list in 8.1 | Q24, Q21. Broken into F5a (namespace shape: built as scoped `NCH.withNamespace`, recommend closing) and F5b (QoL key list: `foodTypes` listed in 8.1 is not built because `qol #3` is held; the other six keys match `CDConfig`; recommend editing 8.1 to the built list and re-adding `foodTypes` with `qol #3`) |
| F6 | Donor download step in the build; food types and the saturation word move (after food families settle) | `framework #9`, `qol #3` |
| F7 | Whether the Display-specific pack keys in the pack config (placeable tooltips, counter plates) move to the CD config in the same step as A6 or later | B2. Moot: the pack config has no Display keys (grep of `creativeengineering-common.toml` and `CEConfig`), `placeableTooltips` is already `[qol]` in the CD config and A6 moved the port's own file. Recommend closing |

## G. Order

1. A1 and the D-list installs that need no decision (D1 to D3), so the freeze has nothing pending.
2. E2 (dump half of the harness), then E1; both gate member moves.
3. A2 to A9 together with E7 (one bundle pass over five ports), then C3 (cutover release).
4. C1 and C2 in their own session (inventory first), before or after the cutover as the release allows.
5. C4 and C7 with the drinks build; B1 to B4 alongside every move.
6. E5, E6 (swappers) after E7.

## H. Progress (2026-11-02, implementation run)

Built and installed: `mods/creativedelight-1.0.0.jar` (20 members, now with Veggies `-05`, Vintage `-11`, Fungi `-02`,
Miner's `-03`, Display `-02`) and `mods/creativebrewing-1.0.0.jar`. Old port jars displaced to
`development/creativeDelight/jar_archive/displaced/`. Nothing booted, nothing committed, nothing staged.

| # | Status | Result |
|---|---|---|
| A1 | Parked | SQ-DDP / CQ-DDP carried into the scratch queue |
| A2 | Done | `members/display`; plan premise wrong: source port, so `ce.forge-mod` (like `members/rustic`), not `ce.forge-repack`. Version `1.7.0-ce-19.2-02` |
| A3 | Done | With C3 |
| A4 | Done | No "(Backport)" name in scripts; `pack.mcmeta` of the five ports and Miner's donor-missing message now "(Creative Delight)" |
| A5 | Done | Premise wrong: no pack or CD script writes a Display recipe; `md_` entries are blockstates and loot only. Registry namespace unchanged |
| A6 | Done | CD `[display]` with nine positive switches; port reads them through `development/shared/cdconfig` (`CdSettings`). Old `displaydelight-common.toml` read once by `compat/MemberConfigMove` ("Disable..." keys inverted), renamed `.moved`. Not `ConfigRename.mergeSections` (patcherMod untouched) |
| A7 | Done | Switch generated from `bundle_members`; no CD script rows to gate |
| A8 | Partial | DDP-13 added in display_delight.md; runs queued in scratch_queue.md section SQ-CDJ5 (boots 0 to 5 and a client block; new rows SQ-CDJ-09 to -11, CQ-CDJ-01, SQ-DDP-06, -07, SQ-VTD8-01, SQ-MDP-17, SQ-CDFW-10, -11; older rows marked RERUN) |
| A9 | Done | display_delight.md, creative_delight.md 38.4, README, TODO |
| B1 | Done | `[display]` and `[vintage]` per-member sections; old `vintagedelight-common.toml` read once, renamed `.moved` |
| B2 | Already done | All six sections moved at the CD carve (`CDConfig.MOVED_SECTIONS`) |
| B3 | Already done | All CD recipe scripts go through `global.CD.recipes` (`NCH.withNamespace('creativedelight')`) |
| B4 | Done | `dump-diff` allows a lost id when `creativedelight:<same path>` exists with the same type |
| C1 | Partial | Step 1 inventory in ends_delight.md 5.7 (24 items, 4 blocks, 21 recipes, no mixins, no overlap with 1.1.4 additions). Member not started |
| C2 | Open | Not started |
| C3 | Bookkeeping done | `bundle_members`, `replaces_jars`, `build_deploy.py` (IN_TREE_MODS rows dropped; CD extra-source check; `data/creativedelight/loot_tables` classed dev), `mod_sides.md` merged, changelog bullet. Tools find nested jars via `pack_paths.mod_jars`. Not staged |
| C4, C7 | Open | Drinks build not started |
| D1, D2 | Already done | Were in mods/; now superseded by the nested members |
| D3 | Done | CD and CB installed |
| E1 | Done | `tool_dict.js` (108 keys), `misc_dict.js` (227), `tools/kitchen_dicts.py`; Veggies pilot reads 119 refs as `fd.<key>`, recipe calls byte-identical (CDT-09 offline) |
| E2 | Done | `cd_move_diff.py dump-snapshot` / `dump-diff`; baseline taken on stale dumps (refresh with `/dumpall` + `/cedumpnames` before the next move) |
| E5, E6 | Open | Not started (E5 is patcherMod Java; wait for the other patcherMod work to settle) |
| E7 | Done | Five ports bundled; Veggies, Vintage, Fungi, Miner's folders stay under `development/` |

Manual, not done: server `mods/` gets the new CD and CB jars and loses the five `backported*` jars; server `miners_upstream/`
needs `miners_delight-1.19.2-1.1.2.jar` and `miners_delight-1.20.1-1.4.5-backport.jar`; instance `server_mods/` still holds
old `backportedDisplay -01`, `Fungi -01`, `Miners -01`, `Veggies -03`, `Vintage -07`.

Owner calls raised: F5 close as "scoped" (built and used everywhere); F7 moot (no Display keys in the pack config); confirm
the defaults for tool / misc dict category names and the `item_` catch-all, Display's CD key names with inverted
"Disable..." keys, and the four port folders staying under `development/`.

Scratch r1102 (2026-11-02, [scratch_run_2026-11-02.md](testing/scratch_run_2026-11-02.md) 8): nesting, config moves (DDP-06, VTD4-04),
donor stop messages and the Veggies pilot PASSED; 9 FAIL rows from 8 defects, TODO `members #36` to `#42`. Client block open.

## I. Fix and rerun plan after scratch r1102 (owner decisions 2026-11-02)

Source: [scratch_run_2026-11-02.md](testing/scratch_run_2026-11-02.md) section 8. Each item: fix, then the rows to rerun
(add them to the SQ-CDJ5 queue section; the batches are in `tools/scratch_query/batches/r1102/`, already corrected for the
batch faults the judge found). No scratch boot until the owner says so. Rebuild and install through `:creativeDelight:`
only, never while the game runs. Player changelog bullet for each player-visible fix.

| # | TODO row | Decision | Fix | Rerun |
|---|---|---|---|---|
| I1 | `members #36` | Drop the Display baked tentacle plate | Nothing to restore. Remove `od_plated_baked_tentacle_on_a_stick` from the old-id handling: add it to the swap list as "removed, no replacement" so an old world loads without the Unidentified mapping error (check the swap-list format in `swappers #1`; a removal entry, not a conversion). Drop the `oceansdelight:baked_tentacle_on_a_stick` line from `plate_displayable.json` if still there | SQ-CDJ-05, SQ-DDP-01 (pass text: no missing-registry line) |
| I2 | `members #37` | Fix the Pickling Jar | See I2 detail below | SQ-VTD8-01 (rewrite), SQ-VTD4-04, SQ-VTD-* jar rows |
| I3 | `members #38` | Cave Hamburger with a calamari recipe | In CD `kubejs/data/creativedelight/kubejs/miners_delight/recipes.js`: the tentacle removal block (`R.remove({input: ...})`) also removed `cave_hamburger`. Add the recipe back through `global.NCH` with `rusticdelight:calamari` (via `global.item` key) in the slot that took baked tentacles, keeping the brown mushroom option as upstream had it | SQ-MDP-13 (count 11 becomes 12), SQ-MDP-06-style `zq makes` check |
| I4 | `members #39` | Stop the Copper Pot cooking when the leftover slot is full | `CopperPotBlockEntity.canCook`: check `leftoversFit()` before returning true when the meal slot is empty, so `stallWhenFull` holds in both cases. File: `development/backportedMiners/.../copper_pot/CopperPotBlockEntity.java` (around line 327) | SQ-MDP-14 (the "slot 7 full" pot must not cook and must keep its milk bucket) |
| I5 | `members #40` | Add the cave carrot crate loot rule | Make the rule apply: `kubejs/server_scripts/loot/loot_index.json` has no miners_delight tables because Miner's data comes from the donor jar. Have `tools/gen_loot_config.py` read the donor jar (`miners_upstream/`) or the `/dumploot` tables, regenerate the index, then confirm `L.pack` in CD `miners_delight/loot.js` rewrites the table | SQ-MDP-16 (300 rolls, crates appear), SQ-MDP-09 |
| I6 | `members #42` | Truffle dirt and rooted dirt patches in forests | Worldgen features: rooted dirt patches and `fungidelight:truffle_dirt` generate in forest biomes (dark forest, taiga/cold forest, birch). Order the modifiers so the truffle ore runs inside the rooted dirt blob (`wild/truffle_dirt_ore`, `wild_crops.json`, or merge the two rules). r1102 scan: rooted dirt present everywhere, truffle dirt 0 in all four squares. Worldgen registry change: fresh world needed | SQ-FGD-14 on a fresh group B world (radius 300 is enough for DF/TA/BI/SW; mushroom fields needs a located square, not centred radius), SQ-FGD-13 |
| I7 | `members #41` (spider half) | Patch the spider burning-kill bug | Upstream `scavenging_spider` lacks `is_on_fire:false` that `scavenging_spider_leg` has: add it in `MinersTranslator.PATCH_DATA`. Cheese blast drop closed 2026-11-02: tables keep `survives_explosion`, VTD-54 reworded | SQ-MDP-08c |
| I8 | (folders) | Fold the four port folders into the member setup | Move `development/backportedVeggies`, `backportedVintage`, `backportedFungi`, `backportedMiners` to `development/creativeDelight/members/{veggies,vintage,fungi,miners}/` as was done for Display (`ce.forge-mod` shape of `members/rustic`, `bundled_in=creativeDelight`). Update root `settings.gradle` and the member map in `creativeDelight/build.gradle`, `tools/` that name the old paths (`build_deploy.py`, `port_resources.py`, `check_donor.py`, `check_vintage*`, `prep_srv.sh` drop list names stay), CLAUDE.md layout line (ports are members), memory notes naming `development/backported*`. Run `:creativeDelight:build --offline` and the ports' resource guards; clean-build after deleting overlay files (memory repack-stale-resources). Do this first so the fixes below land in the final paths | SQ-CDJ-09 (members line, item counts), SQ-CDJ-05 |
| I9 | decisions | Dictionary category names, Display config key names | Confirmed as built: tool and misc dict category names and the `item_` catch-all (`tool_dict.js`, `misc_dict.js`), Display `[display]` keys with inverted "Disable..." keys. Record in creative_delight.md 13.4 / 38.4 and display_delight.md; nothing to change | none |

### I progress (2026-11-02 afternoon, implementation run)

All of I1 to I8 built; `mods/creativedelight-1.0.0.jar` installed 13:06 (Veggies `-05`, Vintage `-12`, Fungi `-02`, Miner's
`-04`, Display `-03`). patcherMod `creativeengineering-1.1.07` rebuilt 13:04 (swap-list `removed` key) and left in
`build/libs` (it also carries the not-yet-installed SQ-EFB work). Nothing booted, nothing committed. Queue: "SQ-CDJ5 fix
rerun" in scratch_queue.md. Changelog bullets added for I1, I2, I3, I6, I7 and the FD loot side effect of I5; I4 and I5 fix
unreleased bullets (Copper Pot leftover slot, Cave Carrot Crates), so no new bullet.

| # | Status | Result |
|---|---|---|
| I1 | Built | Swap lists gained `"removed": [ids]` (patcherMod `SwapLists.isRemoved`, `SwapRemapper` calls Forge `ignore()`); CD `data/creativedelight/swaps/removed.json`; Display `plate_displayable.json` entry dropped. Takes effect only with the new patcherMod |
| I2 | Built | `FermentingRecipe.assignSlots` (one slot per ingredient entry, distinct slots first, backtracking; 2026-11-04 Vintage `-18`: strict, filled slots = entries, no shared slot) drives match, consume and `remaindersFit`; thrown leftovers queue for CD `[vintage.pickling_jar] overflowEjectDelayTicks` (default 20, saved in the block entity, dropped when the jar breaks); `CdSettings.number` added. The duplicate bottle has no code path left (one leftover per consumed item); offline assignment harness passes (split 4, stacked, 2+2, greedy trap, too few, stray slot) |
| I3 | Built | `creativedelight:shapeless/cave_hamburger` |
| I4 | Built | Empty meal slot returns `leftoversFit()` |
| I5 | Built | `gen_loot_config.py` reads nested bundle jars and the Miner's 1.4.5 donor; regenerated index adds `miners_delight:chests/md_abandoned_mineshaft` and 15 `farmersdelight:chests/fd_*` tables (lost since FD was nested), so FD chest crates apply again |
| I6 | Built | One `rooted_dirt` rule with both features, plus birch; `crops_by_biome.md` regenerated |
| I7 | Built | `MinersTranslator.PATCH_DATA` `notOnFire` on `scavenging_spider.json` |
| I8 | Built | Folders moved to `members/{veggies,vintage,fungi,miners}`; `settings.gradle`, `build_deploy.py` (extra-src list, Miner's donor package path), the ports' `tools/*.py` (`INSTANCE` four levels up), comments in `item_dict.js`, `gen_crop_worldgen_table.py`, `display_delight_jar.py`, `gen_display_delight_models.py`; clean build passed |
| I9 | Recorded | As built (no change) |

### I2 detail: Pickling Jar (`FermentingJarBlockEntity`, since 1.1.01)

Two defects, both in the consume and output path. Fix together, test together.

1. **Ingredients taken from the wrong slots.** `consumeIngredient(Ingredient)` takes one item from the first slot that matches,
   per ingredient entry. `FermentingRecipe.matches` needs one ingredient per non-empty slot. Fermented garlic (4 garlic cloves
   plus a honey bottle): with the cloves split over slots the jar drained the first slot instead of one from each of four
   slots, and 2 from 2 slots instead of 1 from each of 4 (as the owner observed); the next batch then no longer matches and the jar stalls
   after one batch (SQ-VTD8-01: 1 of 3 batches). Fix: consume exactly the slot each ingredient matched in `matches`
   (record the slot assignment once, use it for both match and consume), one item per slot.
2. **Duplicate glass bottle from honey-bottle overflow.** The honey bottle's remainder (glass bottle) is output twice when the
   bottle buffer overflows (`remainderBuffer` true and the buffer full; with `false` it ejects). Fix: one remainder per
   consumed honey bottle, whichever path stores or ejects it. **Add a delay on the overflow ejection** (the bottle is thrown
   out after a short delay, not on the same tick as the craft); suggested 10 to 20 ticks, a config key in CD `[vintage.pickling_jar]`
   (`overflowEjectDelayTicks`, default 20) read through `CdSettings`. Confirm the duplicate with `data get` before fixing; the
   r1102 batches did not isolate it (SQ-VTD4-04 showed 1 bottle in slot 9 and 1 in the chest, with the buffer on).

Test rows (rewrite SQ-VTD8-01 and add SQ-VTD8-02): (a) 3 batches of 4 garlic cloves in 4 separate slots plus a honey bottle
each: all three batches finish, 3 fermented garlic honey, slots consumed 1 per slot per batch; (b) cloves stacked in one slot
and in two slots: the recipe still matches and consumes one per ingredient entry; (c) 3 honey bottles with `remainderBuffer`
true and the buffer full: exactly 3 glass bottles in total, the overflow ones ejected after the delay (count item entities and
slot 9 at +0 and at +delay); with `remainderBuffer` false: 3 bottles ejected, none duplicated.

### Order for the next session

1. I8 (folder move) with a clean build and SQ-CDJ-09 style boot check.
2. I1, I3, I5, I7 (script and data, `/reload` level), then I2 and I4 (Java), I6 (worldgen, needs restart and a fresh world).
3. Changelog bullets for I1, I2, I3, I4, I5, I6, I7; writeups: display_delight.md (I1), vintage_delight.md (I2), miners_delight.md (I3, I4, I5, I7), fungi writeup (I6); README and TODO rows.
4. Queue: add the rerun rows and the new SQ-VTD8-02 to SQ-CDJ5, refresh the stale row texts listed in the r1102 run doc, rerun the four batch-fault rows (SQ-DDP-07, SQ-MDP-06, -10, SQ-MXS-03) with the fixed batches.
5. Run on request only: Boot 1 equivalent on the new jar (b1_main plus b1_long), b5 for I6, b2 for the Display/Vintage switches; the client block (CQ-CDJ-01, CQ-DDP-01, -02, CQ-MND-01) when the owner releases it.

Decided 2026-11-02: cheese wheel and pizza blast drops stay as built (survives_explosion); SQ-VTD5-01 pass text and VTD-54
reworded. The Fungi census (mushroom-fields half of SQ-FGD-12, SQ-FGD-15) is queued as SQ-FGD-12m and SQ-FGD-15c in SQ-FGDC.
Still open: F5 and F7 (see section F: both recommended closed).

## J. More Creative Delight plans (owner decisions 2026-11-02, after the Ender's donor review)

Plans only; nothing built, no jar touched. Rules R2 (recipe namespace `creativedelight`), R3 (CD config) and R5 apply. Every member keeps
its old ids and mod id. Rows: `members #46` to `#50`. Same cutover as `members #10`.

### J1. Ender's content into End's Delight (`members #46`)

| # | Step |
|---|---|
| J1.1 | Ender's Delight 1.2.2 stays `endersdelight-1.19-1.2.2.jar` in `mods/` (ARR, no donor member, `members #33` dropped). |
| J1.2 | Move `members/enders` (`enders_additions`: 14 registrations, End House, shulker bowl dish, translator) into the End's member (`members/ends`). The `endersdelight:` ids register always, whether or not the external Ender's mod is present. Old worlds without Ender's may lose them; accepted by the owner. |
| J1.3 | The upstream reader (`upstream/UpstreamJars`, `EndersTranslator`, bottom packs) moves with it and keeps reading `enders_upstream/`; a future Ender's version is also taken from that folder, not from `mods/`. |
| J1.4 | CD script rows that name Ender's 1.2.2 content keep the `// requires: endersdelight` header; rows for the moved 1.1.4 items need no header. Display name stays "End's Delight (Creative Delight)". |
| J1.5 | Docs: ends_delight.md 5.3, `mod_sides.md` (drop the `enders_additions` mention), SQ-DNR-E and SQ-EN rows rerun against the merged member; no player-visible change, so no changelog bullet. |

### J2. Salt and Create: The Salt repacked (`members #47`, `#48`)

Shape of `members/oceans` (`ce.forge-repack`, `bundled_in=creativeDelight`, "Salt (Creative Delight)" and "Create: The Salt (Creative Delight)"); MIT notices kept beside the code. Globs go into CD `replaces_jars`; `mod_sides.md` rows merged into the CD row; `installToMods` displaces the old jars. Salt keeps its access transformer.

References to `salt` and `create__the_salt` found 2026-11-02 (these must keep working after the move):

| Where | Reference | Action |
|---|---|---|
| CD Java | `salt/SaltBlockTicks`, mixins `SaltBlockRandomTickMixin`, `SaltLayerRandomTickMixin`, `SaltRockNoDissolveMixin` (hook `ISaltBlock`; `salt:raw_rock_salt_block` exempt) | Keep working first; once Salt is a member, replace the three mixins by direct edits |
| CD scripts | `creativedelight/salt.js` (removes `salt:salt_from_raw_rock_salt` and three `create__the_salt:` recipes by id, rebuilds 4 salt per raw rock salt), `seasoning_tags.js`, `vintagedelight/recipes.js`, startup `vintagedelight/item_merges.js` | Recipe ids unchanged; rows under `creativedelight:` (R2) |
| Vintage member | its own `salt`, `salt_block`, `evaporator`, `SaltBlock`, `SaltLayerBlock` (not Salt's `ISaltBlock`); salt merge (food/seasoning #6) makes Vintage's salt dust the one salt | No change; both stay members of one jar, so the "only if Vintage is off" fallback in `salt.js` stays |
| Pack KubeJS | `item_dict.js` (`t_can_be_salted` = `#salt:can_be_salted`), `server_scripts/tags/blocktags.js` (`salt:salt_block`, `salt:raw_rock_salt_block`), `assets/creativecommon/jei_world/salt/manual.json` | Stay; ids unchanged |
| patcherMod | `WorldTab.SALT` (JEI world tab names `salt:salt_block`, `salt:salt`), `CEConfig` mod-name override `create__the_salt` and the seasoning JEI text | Stay; mod-name override changes to the "(Creative Delight)" name |
| Config | `config/salt-common.toml` (salting nutrition, saturation, food values) | Move into `creativedelight-common.toml` `[salt]` with `ConfigRename.mergeSections` (R3), old file read once and renamed; copy to `configs_edited/` |
| Creative Brewing, Calendar, Compat | none found (an earlier grep hit was `basalt`) | none |
| Recipe ids and tag | `salt:salting` recipe type, `#salt:can_be_salted`, `salt:` loot and worldgen | Kept by id; a move-diff harness run (framework #6) must show them unchanged |

### J3. Canned Goods repacked (`members #49`)

`members/cannedgoods`, AFL 3.0: notice file kept, every changed file marked as modified, display name "Canned Goods (Creative Delight)", mod id `canned_goods`. CD already holds `CannedGolemColorMixin`, `CannedGolemRendererMixin`, `CanPagesJEI` and the generated per-type cans; after the move the mixins become direct code. Same jar bookkeeping as J2.

### J4. Golden Food as a donor jar (`members #50`)

Member `portedGolden` (mod id `goldenfood`, ids and block states unchanged): our own code for the 17 upstream classes (golden and enchanted cakes, enchanted foods, enchantment, anvil update event, creative tab); art, data and lang read from the unmodified, sha1-pinned `goldenfood_upstream/goldenfood-1.19.2-2.0.0.jar`, which goes to server and clients like `miners_upstream/`; `UPSTREAM_DONORS` and `check_donor.py` gain the row; the standalone jar leaves `mods/`. Pack code already in CD: `goldenfood/placeable.js`, `food_types.js`; pack files naming it: loot tags, trade tags, block tags, `05_tuning.js`, `07_rules.js`. Step 1 is a class and registry inventory, as for Oceanic.

### J5. Order

1. J2 and J3 (plain repacks, the Ocean's pattern), then J1 (member map change), then J4 (needs the donor tooling and an inventory).
2. Each adds its checks to `testing/scratch_queue.md`; no scratch boot until the owner says so.
3. All ride the `members #10` cutover.


## K. Progress (2026-11-02 night, implementation run: J1 to J4 and the remaining open rows)

Owner asked to execute every open Creative Delight change, Golden Food included. Built by seven parallel agents, merged
and bundled by the main session. Installed 23:16 to 23:20 with this instance's game closed: `mods/creativedelight-1.0.0.jar`
(23 members as written; the jar reinstalled 2026-11-03 00:08 nests 22, Ender's additions being inside End's), `mods/creativeengineering-1.1.08.jar`, `mods/creativeworldgen-1.0.0.jar` (first install of Creative
Worldgen, wave 5, see creative_split.md history). Displaced: `salt-1.19.2-1.2.4.1.jar`, `create_salt 1.19.2-1.1.0.jar`,
`CannedGoods-1.19.2-3.jar`, `goldenfood-1.19.2-2.0.0.jar`, patcherMod 1.1.07, `moreMooblooms-1.1.0.jar`. Nothing booted,
nothing committed. Queue: SQ-CDJ6 in testing/scratch_queue.md.

| # | Row | Status | Result |
|---|---|---|---|
| J1 | `members #46` | Built | `enders_additions` is overlay code of `members/ends` (repackedEnds `-03`, two `[[mods]]`, Ender's dependency optional); `members/enders` deleted (backup in `moved_code_backup/2026-11-02-j1-enders/`); settings.gradle, bundle_members, `build_deploy.py` donor path changed together. ends_delight.md 5.9 |
| J2 | `members #47`, `#48` | Built | `repackedSalt`, `repackedCreateSalt` (Create optional). Rock salt no-dissolve moved from a mixin into Salt's `Dissolving` (`replaced_classes`); the two vanilla-block mixins stay. **Config not moved** (Salt's `Configuration` feeds about 20 classes; R3 left open). Mod-name overrides for `create__the_salt` and `canned_goods` now carry "(Creative Delight)" (CEConfig default, live config, configs_edited). salt.md |
| J3 | `members #49` | Built | `repackedCannedGoods`; golem dye as direct code in two replaced classes plus GPL3 `CannedGolemDye`; CD mixins removed. cans.md 9 |
| J4 | `members #50` | Built | `portedGolden`, own code, donor `goldenfood_upstream/` (sha1-pinned, `UPSTREAM_DONORS`, `check_donor.py` 0 problems on the nested jar). Owner questions Q1, Q2 in golden_food.md 7 |
| E5 | `swappers #3` | Built | Live-item merges in Common `swap/ItemMerges`, `/ceswap items`, triggers in `config/creativeengineering-swaps.toml` (owner: confirm defaults and file) |
| E6 | `swappers #2` | Built | CD merge pairs moved into `data/creativedelight/swaps/merges_<mod>.json`, fed by CD `swap_merges.js` |
| - | `members #51` | Built | Miner's loot modifiers replaced by pack loot rules (new Worldgen helpers `L.drop`, `L.dropShare`); haste half (`potions/effects #17`) not done, Pinned |
| - | `members #30` R7, `dishes/meals #12` | Built | Pancake food type `sweet/pancake`, family `dessert/pancake` without a base, Sweet Potato Pancakes placeable |

Not done in this run, and why: `framework #4` (37 foods) and `#6` (baseline), `members #43`, `#44` need fresh dumps from the
game (`/dumpall`, `/dumpfood`, `/cedumpnames`); `members #34` (other cooking mods), `#6` (guardian food), `#25` (crate art),
F5, F7 wait on owner decisions; `qol #3`, `framework #9` are Pinned; `members #10` is the release.

Follow-ups: after the first boot, `/dumpall`, then `python3 tools/gen_loot_config.py` (SQ-GLD-09); `tools/creative_delight_test/move_diff_snapshot.json` lists the
deleted merge scripts (retake the baseline with `framework #6`); `tools/loot_tuning_sim.js` still points at the old loot
paths; `split/A_java.md`, `split/B_mixins.md` and section 34.1 of creative_delight.md still name the removed golem and rock
salt mixins; `gen_dd_dishes.py` full runs rewrite 9 older dish models onto the newer bowl (reverted; review). Server by
hand: copy `goldenfood_upstream/` beside mods/, remove the four displaced jars, install the three jars.

Deploy tooling fixed in the same run: `build_deploy.py` `CATEGORIES` lacked `oceanic_upstream` and `enders_upstream`
(KeyError on any deploy build) and now carries them plus `goldenfood_upstream`, with server and client copy steps;
`gen_structure_config.py` reads the End House from `enders_upstream/` (it had dropped the set since the member began
reading the donor on 11-01); `structure_config.json` regenerated, identical apart from that source line. The dry run's
only remaining preflight error is three FTB jars with no `mod_sides.md` row (not from this run).

Later the same night (owner): Golden Food Q2, the enchantment, its anvil route and the village books removed; 23 enchanted
golden foods by sequenced assembly (CD `goldenfood/golden.js`, incomplete items in `transition_items.js`, restart needed);
CD jar reinstalled. Q1: quirk kept, candle integration with every cake is TODO `baking/baked-goods #38`. Swapper triggers
kept as built (owner). After the first boot also `/cegen food_families`.

## L. Progress (2026-11-03, implementation run: decided open rows of the members)

Owner asked to execute every decided open item of the direct Creative Delight members. Scope: open TODO rows (Bug,
Planned, In Progress) whose work lands in one of the 22 members and whose decision is on record; mods with only a CD
script folder (Fruits, Respite, Hostile and others) were left out. Built by four parallel agents (Barbeque's; Vintage;
Oceanic, Fungi, Delightful, Veggies, Rustic; Golden, Salt, Incubation). `:creativeDelight:build` and `:creativeBrewing:build`
passed; both jars installed 2026-11-03 00:08 with the game closed. Nothing booted, nothing committed. Queue: SQ-CDJ7.
Owner decision 2026-11-03: gilded foods take 8 loops of 1 gold ingot + 125 mB (replaces 4 gold-block loops), apple included.

Built (Testing): `food/seasoning #18`, `#11`, `#13`, `#7`, `#4`, `dishes/meals #49`, `ports/vintageport #7`, `jei-coverage #15`,
`merges/dishes #4`, `#7`, `#9`, `textures/merges #2`, `#3`, `#4`, `farming/crops #27`, `farming/wild-crops #6`, `dishes/ocean #8`,
`ui/names #13`, `#14`, `ui/effects #12`, `food/types-and-tuning #22`, `#23`, `creativedelight/members #43` (rule, not a gap).
Already built: `ui/wthit #13`. Partly: `dishes/meals #46` (assembly, pack skewers #50 and pairs open), `food/types-and-tuning #3`
(cakes, other golden foods, can rerun), `merges/dishes #8` (quiche built; lettuce-tag reading to confirm), `mobs/nests #3`
(textures), `ports/vintageport #1` (SQ-VP-06 fix only). Not built: `food/types-and-tuning #17` (food set, form, art),
`mobs/nests #2` (art), `creativedelight/members #44` (needs `/dumpcompost`).

Left out on purpose: `members #10` (release), `#45` (scratch reruns), `#6`, `#25`, `#34` (owner decisions), `ports/vintageport #2`
(SQ-VTD7-02 owner call), `cooking/kitchen #23` (art for an ARR mod's blocks), `creativesplit/cdfinish #11` (split row).
Watch: a full `gen_can_types.py` run picks up pending can changes from this run (four unrelated can scripts were reset to HEAD
by the Vintage agent); `food_families.py` reports 94 unsorted; item_merges.md 2.17 to 2.20 and the merge count (SWP-01) need
one recount after boot.
