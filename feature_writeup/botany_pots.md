# Botany Pots: slowed growth

Botany Pots (`BotanyPots-Forge-1.19.2-9.0.44.jar`, both sides) in this pack: crops grow 8 times slower, flowers 2
times slower, no bone meal in pots. Plan: [plan_farming_cooking_batch.md](plan_farming_cooking_batch.md) A15 / C5,
owner decisions Q15 to Q17 and Q24. TODO rows `largecontent/mod-additions #35`, `farming/trees #6`, `farming/crops #36`.

## 0. At a glance

| Part | Status | Files | Knobs |
|---|---|---|---|
| Growth x8 crops, x2 flowers, fertilizers removed | BUILT 2026-11-07, not tested | `kubejs/server_scripts/other/botany_pots.js`, `global.NCH.scaleRecipeInt`, `i.t_wild_crops` (Creative Common) | `global.BOTANY_GROWTH_MULT` (8), `global.BOTANY_FLOWER_MULT` (2), top of the script |
| Lemon tree shows the lower half | BUILT 2026-11-07, not tested | `kubejs/data/fruitsdelight/recipes/botanypots/lemon.json` | none |
| Pot tooltip | BUILT 2026-11-07, not tested | `kubejs/client_scripts/tooltips.js` ([tooltips.md](tooltips.md) TT-43) | none |

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
- **Soils:** unchanged, so their modifiers stack on the slower base.
- **Fertilizers:** every `botanypots:fertilizer` recipe removed (bone meal is the only one).
- **Lemon tree:** the two-block tree only fits as one block; the pot shows the lower half of each growth stage
  (`half: lower` pinned in every display phase). Drops and time otherwise as the mod (time x8 by the script).
- **Tooltip:** every botany pot item: "Useful for flowers, not great for crops".

## 4. Limits and decisions

- x0.2 (`mod-additions #35` first note) replaced by the owner's Q15 to Q17: crops x8, flowers x2 (not faster).
- Flowers by tag, not by category: pot categories are soil kinds (dirt, farmland), not plant kinds.
- Lemon: a stacked two-half model (option M) was rejected for now; lower half only (Q24).
- No yield changes (owner: no yield increases).

## 5. File map

- [botany_pots.js](../kubejs/server_scripts/other/botany_pots.js)
- [lemon.json](../kubejs/data/fruitsdelight/recipes/botanypots/lemon.json)
- `scaleRecipeInt` in [neoCraftHelper.js](../development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js)
- tooltip line in [tooltips.js](../kubejs/client_scripts/tooltips.js)

## 6. Offline checks

- `node --check kubejs/server_scripts/other/botany_pots.js`
- Pot crop count: `grep -c ',botanypots:crop,' kubejs/exported/recipes/all.csv` (after `/dumpall`).

## 7. In-game test suite

- [ ] **BP-01 Log:** start the game → `logs/latest.log` has `[botany_pots] growthTicks scaled on` with about 173 crops and
  about 49 flowers; no `scaleRecipeInt missing` error.
- [ ] **BP-08 Wild crop:** JEI on wild tomatoes in a pot → 9600 ticks (crop speed, not flower speed).
- [ ] **BP-02 Crop time:** JEI on wheat seeds in a pot → 9600 ticks (8:00) [1200]; a pot of wheat on dirt grows in about 8 minutes.
- [ ] **BP-03 Flower time:** JEI on a poppy in a pot → 2400 ticks [1200].
- [ ] **BP-04 Soil stacks:** the same crop on a faster soil (JEI soil page modifier above 1) grows faster than on dirt.
- [ ] **BP-05 No bone meal:** right-click a growing pot with bone meal → nothing happens, no bone meal used.
- [ ] **BP-06 Lemon:** plant lemon seeds in a pot → the lower half of the lemon tree at every stage, nothing floating
  or cut mid-leaf; time 9600 ticks.
- [ ] **BP-07 Tooltip:** TT-43.

## 8. History

- 2026-11-07: built (plan_farming_cooking_batch A15 / C5): growth pass, fertilizer removal, lemon display, tooltip,
  `NCH.scaleRecipeInt`.
