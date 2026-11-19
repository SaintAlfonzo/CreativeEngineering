# Botany Pots: slowed growth

Botany Pots (`BotanyPots-Forge-1.19.2-9.0.44.jar`, both sides) in this pack: crops grow 8 times slower, flowers 2
times slower, no bone meal in pots. Plan: [plan_farming_cooking_batch.md](plan_farming_cooking_batch.md) A15 / C5,
owner decisions Q15 to Q17 and Q24. TODO rows `largecontent/mod-additions #35`, `farming/trees #6`, `farming/crops #36`.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=ba133888da4a -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 0 open TODO rows (none), 0 Complete.
<!-- generated:status_index end open_work -->

| Part | Status | Files | Knobs |
|---|---|---|---|
| Growth x8 crops, x2 flowers, fertilizers removed | BUILT 2026-11-07, not tested | `kubejs/data/creativecompat/kubejs/other/botany_pots.js`, `global.NCH.scaleRecipeInt`, `i.t_wild_crops` (Creative Common) | `global.BOTANY_GROWTH_MULT` (8), `global.BOTANY_FLOWER_MULT` (2), top of the script |
| Lemon tree shows the lower half | BUILT 2026-11-07, not tested | `kubejs/data/fruitsdelight/recipes/botanypots/lemon.json` | none |
| Pot tooltip | BUILT 2026-11-07, not tested | `kubejs/client_scripts/tooltips.js` ([tooltips.md](tooltips.md) TT-43) | none |
| Better soils: Rich Soil and Rich Farmland x1.10, Fertile Soil x1.20, no yield; soil tooltips | BUILT 2026-11-14, Creative Common jar built not installed; SQ-SY-21, CQ-SY-22 | `botany_pots.js`, `NCH.setRecipeNumber` / `NCH.botanySoil` (Creative Common), `tooltips.js` (TT-44) | `global.BOTANY_SOIL_SPEED` |

Needs the Creative Common jar with `scaleRecipeInt` (built after 2026-11-07); with an older jar the script logs an
error and leaves the mod's times. Measure: `logs/latest.log` line `[botany_pots] growthTicks scaled on N pot crops`.

## 1. Overview

The mod has no config and no global speed. Every plant is a `botanypots:crop` recipe with its own `growthTicks`
(1200 for most vanilla ones), and every soil a `botanypots:soil` recipe with a `growthModifier` that multiplies the
pot's growth rate. The pack changes the crop recipes once, at recipe load, so crops added by other mods or later
jars are covered and JEI shows the new times.

## 2. Reference

- 173 pot crops in the pack (recipe dump 2026-11-05): 154 from Botany Pots' own data (vanilla, Farmer's Delight,
  Botania and other installed mods), 18 Fruits Delight trees and bushes, 1 Supplementaries flax. About 49 of them
  are flowers (32 Botania, 17 vanilla incl. wither rose); 8 wild crops sit in `#minecraft:flowers` too.
- One fertilizer recipe, `botanypots:minecraft/fertilizer/bonemeal` (bone meal adds 400 to 550 ticks).
- Example: wheat 1200 ticks -> 9600 (8 minutes on dirt); poppy 1200 -> 2400 (2 minutes). Rich soils still
  multiply on top.

## 3. What the pack changes

- **Growth:** `growthTicks` of every `botanypots:crop` recipe x8; x2 when the seed item (or a tag or list entry of
  the seed) is in `#minecraft:flowers` (small and tall flowers, Botania mystical and double flowers, wither rose,
  flowering azalea), unless it is also in `#farmersdelight:wild_crops` (wild tomatoes, wild coffee and the other
  wild crops drop produce, so they grow at crop speed). Everything else (crops, trees, mushrooms, coral, grass,
  vines) counts as a crop.
- **Soils (wave 2, [plan_crop_yields_wave2.md](plan_crop_yields_wave2.md) 8):** Rich Soil (mod x1.10) and Rich Soil
  Farmland (mod x1.25) set to x1.10, and Fertile Soil (`immersive_weathering:mulch_block`, no pot recipe in the mod)
  added as a pot soil (categories dirt, farmland) at x1.20. Plain farmland (x1.05) and dirt keep the mod's values. The
  modifier multiplies on top of the slowed base: wheat on Fertile Soil 9600 / 1.2 = 8000 ticks. Pots never get the
  soil yield extra (it only fires on world blocks), and the soils' tooltips say so.
- **Fertilizers:** every `botanypots:fertilizer` recipe removed (bone meal is the only one).
- **Lemon tree:** the two-block tree only fits as one block; the pot shows the lower half of each growth stage
  (`half: lower` pinned in every display phase). Drops and time otherwise as the mod (time x8 by the script).
- **Tooltip:** every botany pot item: "Useful for flowers, not great for crops".

## 4. Limits and decisions

- x0.2 (`mod-additions #35` first note) replaced by the owner's Q15 to Q17: crops x8, flowers x2 (not faster).
- Flowers by tag, not by category: pot categories are soil kinds (dirt, farmland), not plant kinds.
- Lemon: a stacked two-half model (option M) was rejected for now; lower half only (Q24).
- No yield changes (owner: no yield increases).
- `NCH.scaleRecipeInt` / `setRecipeNumber` / `setCookTime` edit the recipe JSON in place and set `r.changed`: KubeJS
  1902.6 builds an unchanged recipe from its original `Recipe` once anything parsed it (every `{ input }` /
  `{ output }` filter does, such as the item-merge pass), so the bare JSON edit was dropped (r1118 F12, `crops #58`).

## 5. File map

- [botany_pots.js](../kubejs/data/creativecompat/kubejs/other/botany_pots.js)
- [lemon.json](../kubejs/data/fruitsdelight/recipes/botanypots/lemon.json)
- `scaleRecipeInt` in [neoCraftHelper.js](../development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js)
- tooltip line in [tooltips.js](../kubejs/client_scripts/tooltips.js)

## 6. Offline checks

- `node --check kubejs/data/creativecompat/kubejs/other/botany_pots.js`
- Pot crop count: `grep -c ',botanypots:crop,' kubejs/exported/recipes/all.csv` (after `/dumpall`).

## 7. In-game test suite

- [ ] **BOT-01 Log:** start the game -> `logs/latest.log` has `[botany_pots] growthTicks scaled on` with about 173 crops and
  about 49 flowers; no `scaleRecipeInt missing` error.
- [ ] **BOT-08 Wild crop:** JEI on wild tomatoes in a pot -> 9600 ticks (crop speed, not flower speed).
- [ ] **BOT-02 Crop time:** JEI on wheat seeds in a pot -> 9600 ticks (8:00) [1200]; a pot of wheat on dirt grows in about 8 minutes.
- [ ] **BOT-03 Flower time:** JEI on a poppy in a pot -> 2400 ticks [1200].
- [ ] **BOT-04 Soil stacks:** the same crop on a faster soil (JEI soil page modifier above 1) grows faster than on dirt.
- [ ] **BOT-05 No bone meal:** right-click a growing pot with bone meal -> nothing happens, no bone meal used.
- [ ] **BOT-06 Lemon:** plant lemon seeds in a pot -> the lower half of the lemon tree at every stage, nothing floating
  or cut mid-leaf; time 9600 ticks.
- [ ] **BOT-07 Tooltip:** TT-43.
- [ ] **BOT-09 Better soils:** SQ-SY-21: log `3 soils set (Rich x1.1, Fertile x1.2)`; JEI soil page: Rich Soil and Rich
  Soil Farmland x1.10, Fertile Soil x1.20; a wheat pot on Fertile Soil takes about 8000 ticks. Tooltip TT-44.

## 8. History

- 2026-11-14: better soils speed (Rich x1.10, Fertile Soil x1.20 as a new pot soil), soil tooltips, `NCH.setRecipeNumber`
  and `NCH.botanySoil` (crop yields wave 2).
- 2026-11-07: built (plan_farming_cooking_batch A15 / C5): growth pass, fertilizer removal, lemon display, tooltip,
  `NCH.scaleRecipeInt`.
- 2026-11-17: `botany_pots.js` moved into Creative Compat's script pack (plan_rules_fixes.md V-26); the Fertile Soil pot soil recipe id is now `creativecompat:`; the `global.BOTANY_*` values the tooltip reads are unchanged (SQ-RFX-02).
- 2026-11-18: growth and soil values reached no loaded recipe (r1118 F12, back at the mod's times); the three NCH JSON
  helpers now mark the recipe changed (plan_bugfix_r1118.md B3).
