# creativedelight / qol briefs

Written 2026-10-25, checked 2026-10-31 against [TODO](../todo/TODO.md) `# creativedelight`, Sub `qol`. Every QoL feature gets
a config switch (framework #5), default on, and keeps its marked `[QoL-n]` writeup. Feature writeup:
[creative_delight.md](../creative_delight.md) 8 (the QoL list) and 22 (as built; test prefix `CDQ-`, scratch `SQ-CDQ-`).

## Scope and order

| Row | Change | Status | Note |
|---|---|---|---|
| 3 | Food types and the Quark saturation word | Pinned | The only open row; waits on `food/types-and-tuning #13` |
| 1, 2, 4, 5, 6 | Remainder buffer, placeable tooltips, compost rows, heatless routes, WTHIT line / JEI pages | Testing | Built 2026-10-25, live since 1.1.03; see Built, needs play |

---

## qol #3: QoL-3 Food types and saturation word
Status **Pinned** / Work: KubeJS / id `creativedelight/qol #3`. Rules still in development
([food_families.md](../food_families.md), `food/types-and-tuning #13`, In Progress).

### Now (checked 2026-10-31)
- The food dict exists (framework #4, built 2026-10-25, In Progress): `kubejs/startup_scripts/_lib/food_dict.js` (1542 keys)
  and `food_grammar.js`; pack scripts alias it `fd` (`f` is the fluid dict there).
- The food type tags already live in the CD tree: [food_type_tags.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/food_type_tags.js)
  (moved from `server_scripts/tags/foodtypetags.js` 2026-10-25, rows unchanged). It reads
  [food_types.json](../../kubejs/generation_scripts/food/food_types.json), keyed by **item_dict** keys, not food dict keys;
  removed items lose the tags again in `_core/removed_item_tags.js`. No `qol.foodTypes` switch yet
  (`CDConfig.java:41`: "QoL-3 (foodTypes) gets its key when it moves").
- Saturation word: Quark's own word; the pack's only override is patcherMod
  `food/IntoxicatingDrinkTooltip.java` ("Intoxicating Drink", `CEConfig` 1497).

### Build (when unpinned)
1. Re-key `food_types.json` from item_dict keys to food dict keys (`tools/food_catalog.py` writes it; teach it the dict).
2. Add `qol.foodTypes` to `CDConfig` and gate `food_type_tags.js` with `global.CD.qol('foodTypes')`.
3. Decide whether `IntoxicatingDrinkTooltip` moves to a CD compartment (it reads the drink food type).
Startup tables, if any, load through the jar startup loader (creative_delight.md 10.5).

### Decisions
- **Move the saturation word override:** recommend leave it in patcherMod (it is alcohol, which Creative Brewing will own).

### Tests
CDQ-new1 (proposed, numbered when queued): `/dumptags` food_type counts unchanged after the re-key; switch off strips them.

---

## Built, needs play

As built: [creative_delight.md](../creative_delight.md) 22; tests in 22.4, scratch SQ-CDQ-00 (boot, PASS 2026-10-25) to -08.

| Row | What | Open tiers | Tests |
|---|---|---|---|
| `qol #1` | Remainder buffer: pot and kettle mixins in CD `mixin/farmersdelight/`, `mixin/farmersrespite/`; buffer API and oven stay in patcherMod | S C P | CDQ-01 to -03, SQ-CDQ-01 to -03 |
| `qol #2` | Placeable-food tooltip lines: `kubejs/assets/creativedelight/kubejs/_qol/placeable_tooltip.js`, switch `qol.placeableTooltips` | C | CDQ-04, SQ-CDQ-04 |
| `qol #4` | Compost rows for member foods (101 rows in `kubejs_startup/<mod>/compost.js`), switch `qol.compost` | S | CDQ-05, SQ-CDQ-05 |
| `qol #5` | Heatless pot routes and grid-meal moves into `<mod>/cooking.js`, switch `qol.manualOnlyRoutes` | S | CDQ-06, SQ-CDQ-06 |
| `qol #6` | WTHIT crop line (`compat/wthit/WthitCropGrowth`) and Fungi JEI pages, switches `qol.wthitCropLine`, `qol.jeiInfoPages` | C | CDQ-07, -08, SQ-CDQ-07, -08 |
