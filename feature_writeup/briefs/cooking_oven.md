# Brief: cooking / oven

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# cooking`, Sub `oven`; swept 2026-10-31. Reference writeups:
[oven.md](../oven.md) (block, `creativeengineering:baking`, OV- tests),
[food_families.md](../food_families.md) 3.1 and 4.6 (family rules), [remainder_buffer.md](../remainder_buffer.md),
[creative_delight.md](../creative_delight.md) 30 (`global.CD.food`).
Code: [baking.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/baking.js) (181 lines: cake / pie /
cookie / muffin / tart / hand-pie tables, oven and bakeware recipes),
[baked_goods.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/baked_goods.js) (wheat -> flour swap),
the family calls in [cd_food.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creative_delight/cd_food.js)
(Creative Delight jar), `global.NCH.ceBaking` (line 1161), `fdCooking` (1074), `hammering` (1142) in
[neoCraftHelper.js](../../development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/neoCraftHelper.js)
(patcherMod jar), patcherMod `oven/` (`OvenBlockEntity`, `BakingRecipe`, `jei/OvenJeiPlugin`).

## Scope and order

Open rows: #2, #6, #7 (In Progress). Testing: #1, #4, #5 (table at the end). #3 is archived (superseded 2026-10-29:
muffins are oven only, `baking/baked-goods #32`).

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `oven #6` baking audit, remainder | KubeJS (CD tree) | Audit and wheat swap done 2026-10-27; left: oven routes for the breads, buns, scones, eclair, brownies, fig pudding, waffles, rolls, pizzas, lasagna |
| 2 | `oven #7` pot recipes that suit the oven, plus smoking | KubeJS (CD tree; `cd_food.js` needs a CD rebuild) | Shares the baking dish (`baking/baked-goods #30`) with lasagna (`#21`) |
| 3 | `oven #2` oven pattern | KubeJS, one line | Owner picks the final pattern; no offline clash data in the dumps |

Related rows elsewhere: `baking/baked-goods #30` (baking dish), `#21` (lasagna), `#37` (zucchini quiche assembly bug,
`assemble` array steps; creative_delight.md 30), `#31` (bread from dough on the sheet, Testing).

---

## oven #6: Baking audit
Status In Progress / Work KubeJS / TODO `cooking/oven #6`

### Goal
Every baked good has an oven recipe on flour (not wheat), and no wheat crafting route is left.

### Now
- **Audit done 2026-10-27** ([oven.md](../oven.md) 4, last bullet): 156 baked foods from the 2026-10-24 dump; cookies,
  cakes, pies, muffins, tarts, hand pies on their family calls in `baking.js` (each call sweeps the mod's own recipes).
- **Honey cake: done.** `baking.js` line 51, `['honey_cake', i.honey_bottle, i.honeycomb]` in the CAKES table (cake pan,
  raw base). The row's "Known: honey cake (add a baking recipe)" is stale.
- **Wheat swap: done.** `baked_goods.js` 16-19 turns wheat into flour in bread, Quark's bent bread, Glow Bread, Adzuki
  Bun, Banana Bread, Strawberry Scones, Strange Eclair; sweet potato dough and Rustic batter 25-27. Left on wheat, not
  baked: Brewin' vodka, sweet gelatine bucket, fried shrimp, turnip cake, nutritional bar.
- **Bread from dough in the oven since 2026-10-28** (`baking/baked-goods #31`, Testing): 1 / 3 / 6 dough -> bread on the
  baking sheet. Cauliflower bread: `veggiesdelight/recipes.js` 45.
- **Still without an oven recipe** (oven.md 4): Banana, Glow, Garlic, Honey and Mulberry Bread, Nether Wart Sourdough,
  Cornbread and tortilla (furnace trio), Adzuki Bun, Strawberry Scones, Strange Eclair, the brownies, Fig Pudding,
  Create Factory waffles and rolls, pizzas, lasagna. Banana bread is a hand craft on flour now (`neapolitan:banana/banana_bread`).

### Build
1. **Loaf breads** (banana, glow, garlic, honey, mulberry, nether wart sourdough): one `R.ceBaking([...the hand
   recipe's inputs, flour as is], Item.of(<bread>, 1), e, '<bread>', { tool: F.ware.sheet })` each, in `baking.js`'s
   empty "Breads (baking sheet)" block (line 167), or a new family call `F.breadBake(inputs, bread)` in `cd_food.js` if
   the owner wants the hand craft removed (decision). Keep the hand craft (dearer route, decision).
2. **Buns, scones, eclair** (`dessert/pastry`): `R.ceBaking` on the baking sheet from the hand recipe's inputs.
3. **Brownies** (`dessert/brownie` rule: oven bakes the tray in the cake pan, board cuts it): an oven recipe for the
   tray block where one exists; check the brownie ids in `kubejs/exported/food/recipes.json` first.
4. **Fig pudding** (`dessert/pudding`: oven, cut on the board), **waffles, rolls** (Create Factory; dessert "Oven for
   the plain bake"): oven recipes on the sheet; waffles may need an iron (out of scope: sheet for now).
5. **Pizzas and lasagna**: leave to `baking/baked-goods` (pizza rule is shaped + sequenced assembly; lasagna waits for
   the baking dish `#30` / `#21`). Note them as "not oven by rule" in oven.md 4.
6. Cornbread and tortilla: the furnace trio is their family rule (`bread` "Smoker + Furnace + Campfire: the dough");
   add an oven mirror only if the owner wants one.
7. Update oven.md 4's "Still without an oven recipe" list and 2.6's tables.

### Ids and data
Breads from the dict (`i.banana_bread`, `i.garlic_bread`, ... check each key in `item_dict.js`), `i.flour`,
`i.any_dough`, `F.ware.sheet` / `.cake`, `F.SWEET`, recipe type `creativeengineering:baking` (ids
`creative_delight:baking/<suffix>` inside `global.CD.recipes`).

### Decisions
- Hand craft for breads that gain an oven recipe: **keep it** (dearer, by hand); the oven is the efficient route, as
  for cakes (oven.md 4).
- Pizzas and lasagna: **not this row** (their own rules and rows).
- Cornbread / tortilla: **stay on the furnace trio** (family rule).

### Gotchas
- With `ovenIngredientCounts` off each counted ingredient is a slot; keep recipes within 6 (oven.md 2.6.1).
- A family call sweeps the output's other recipes; a plain `R.ceBaking` does not. Use plain calls unless the
  hand route is meant to go.
- Changing a bread's recipe changes its can parity "craft sum" (can_parity.md).

### Tests
- OV-new1 (proposed, numbered when queued): each listed bread, bun, scone, eclair, pudding, waffle and roll has a Baking page in JEI with its bakeware.
- OV-new2 (proposed, numbered when queued): no recipe in JEI takes plain wheat for a baked good (the five non-baked leftovers excepted).
- Scratch: a probe listing the new recipe ids loaded, and an `exported/food/recipes.json` check after `/dumpfood`.

---

## oven #7: Cooking pot recipes that suit the oven
Status In Progress / Work KubeJS / TODO `cooking/oven #7` (related to #6)

### Goal
Move or mirror the Cooking Pot recipes that are really bakes (casseroles, stuffed and roasted dishes, baked stews) to
the oven, add tools or dishes where needed, and let the oven smoke (decided 2026-10-29: smoking is allowed in the oven).

### Now
- The oven runs only `creativeengineering:baking` (`OvenBlockEntity.getMatchingRecipe`, lines 139-166:
  `getRecipeFor(OvenRegistry.BAKING_TYPE ...)`); JEI catalyst only for the baking category (`jei/OvenJeiPlugin.java` 45).
- Food dump 2026-10-30 (`kubejs/exported/food/recipes.json`): 338 `farmersdelight:cooking`, 135 `minecraft:smoking`,
  84 `creativeengineering:baking`. Pot recipes with bake-like outputs (from the dump, by name):
  - Stuffed: FD stuffed pumpkin (and Corn Delight's from cornbread), Rustic stuffed bell peppers (9 colours, and CD's
    `_from_roasted` copies), Veggies stuffed bell peppers block, stuffed zucchinis, garlic stuffed mushrooms,
    Delightful stuffed cantaloupe, Fungi stuffed morels, Ender's stuffed shulker, Nether's stuffed hoglin, Miner's
    stuffed squid, Oceanic stuffed pufferfish.
  - Baked or roasted: FD baked cod stew, Veggies roasted vegetables, Miner's fake meatloaf, Corn Delight cornbread
    stuffing, Veggies turnip cake.
  - Layered: Brewin' horror lasagna, Veggies lasagna pan (wait for the baking dish, `baking/baked-goods #21`, `#30`).
  - Tarts: FD fig / lemon tart keep their pot recipe by rule (`tartBake` "tarts keep theirs", cd_food.js header).
  - Not bakes (stay in the pot): pancakes, mashed / diced potatoes, potato salad, gnocchi, pierogi, pie fillings.
- Family helpers in `cd_food.js` (lines 19-25, 136-170): `cakeBake`, `pieBake`, `cookieBake`, `muffinBake`, `tartBake`,
  `handPieBake`; oven parts `F.slot`, `F.ware.sheet / pie / cake / muffin`, `F.SWEET`. No casserole or smoking family yet.

### Build
1. **Smoking in the oven, KubeJS route** (recommended): in a new CD script
   `kubejs/data/creative_delight/kubejs/creativeengineering/oven_smoking.js`, walk every loaded `minecraft:smoking`
   recipe with a food result (`e.forEachRecipe({ type: 'minecraft:smoking' }, r => ...)`, skip non-food results) and
   write `R.ceBaking([input], result, e, 'smoking/<path>', { time: <its cookingtime>, exp: <its xp> })` with no tool
   (a recipe without `tool` ignores the tool slot, oven.md 2.2). Inside `global.CD.recipes`, ids
   `creative_delight:baking/smoking/<path>`. JEI then lists them on the Baking page.
2. **Bake family** in `cd_food.js`: `F.dishBake(inputs, output, ware)` = the pot recipe's ingredients as an oven recipe
   on `ware`, plus removal of that output's `farmersdelight:cooking` recipes (the family is authoritative, as
   `muffinBake`). Container: keep the pot recipe's container (bowl) through `ceBaking`'s `container`.
3. Rows: stuffed dishes and baked stews from the list above in their mod folders (`farmersdelight/`, `rusticdelight/`,
   `veggiesdelight/`, ...; memory `creative-delight-routing`), on the baking sheet (stuffed) or the baking dish once
   `#30` exists (stews, casseroles, lasagna). Owner picks the list (decision).
4. food_families.md 3.1: the stations column for the families that change (stuffed, stew), and oven.md 2.6 a
   "From the pot" table. Changelog bullet (Oven family): which dishes moved, "the oven also smokes food".

### Ids and data
`minecraft:smoking` (135 recipes in the 2026-10-30 dump), `farmersdelight:cooking`, `creativeengineering:baking`; ids of
the dishes above from the dump (verified by name, not by dict key; check each key in `item_dict.js`).

### Decisions
- Smoking route: **KubeJS mirror** (no Java, JEI pages for free, `/reload`) over a Java fallback in
  `OvenBlockEntity.getMatchingRecipe` (needs a BakingRecipe wrapper, a JEI catalyst for the smoking category and a
  patcherMod rebuild). The CfB oven already smokes through `CfbOvenSmokingMixin` (oven.md 2.8).
- Move or mirror pot dishes: **move** (family call removes the pot recipe) for stuffed dishes and baked stews;
  **keep** pot-only for soups, mashes, pancakes.
- Bakeware for stuffed dishes: **baking sheet**; stews and casseroles: **baking dish** (`#30`).

### Gotchas
- The smoking mirror must skip non-food smoking recipes (mod sponges, dyes) and the oat dough / sourdough cases the pack
  removed (`baking/baked-goods #29`: no smoker bread from oat dough).
- A single-input oven recipe matches a lone item in any slot; with 6 slots a stack still bakes one at a time.
- Family sweeps skip `creativeengineering` and `creative_delight` recipes (cd_food.js header), so CD's own
  `_from_roasted` stuffed pepper copies need an explicit removal.

### Tests
- OV-new3 (proposed, numbered when queued): a raw porkchop alone in the oven (no bakeware) becomes a cooked porkchop at smoker speed; JEI's
  Baking page lists the smoking mirrors.
- OV-new4 (proposed, numbered when queued): each moved dish bakes in the oven with its bakeware and has no Cooking Pot recipe in JEI.
- Scratch: recipe-id probe for the mirror count (equals the food-result smoking count) and the removed pot ids.

---

## oven #2: Oven crafting pattern (bakeware is hammered)
Status In Progress / Work KubeJS / TODO `cooking/oven #2`

### Goal
A final crafting pattern for the oven that clashes with no other recipe.

### Now
- Oven: `baking.js` line 172, `R.shaped(i.oven, ['PPP', 'B B', 'BBB'], { P: i.any_plate_iron, B: i.bricks }, e,
  'oven')`, id `creative_delight:shaped/oven` (in the 2026-10-30 dump; it moved into the CD tree 2026-10-25, so not
  `creativeengineering:shaped/oven` any more). Placeholder (oven.md 2.1, 4).
- Bakeware: hammered chain `R.hammering` lines 176-179 (plate -> baking sheet -> muffin tin -> pie tin -> cake pan),
  oven.md 2.5. Done.
- No clash data offline: `kubejs/exported/recipes/crafting_audit.json` keeps only ingredient counts, `all.csv` only ids.

### Build
1. Clash check in game: JEI "uses" of an iron plate and of bricks, looking for 3x3 brick patterns (FD's stove is
   `III / B B / BCB`, close but different). Or extend `/dumpall`'s crafting audit to write shaped patterns (a
   `server_scripts/_lib` dump command) and compare offline.
2. Final pattern (decision), then change line 172 only (same suffix, so the id stays).
3. oven.md 2.1 "Crafting" row and 4 ("placeholder") updated; changelog bullet if the pattern changes.

### Ids and data
`creativeengineering:oven` (`i.oven`, item_dict.js 37), `#forge:plates/iron` (`i.any_plate_iron`, 1714), `minecraft:bricks`
(`i.bricks`, 2171), `minecraft:furnace` (`i.furnace`), FD stove `farmersdelight:stove`.

### Decisions
- Pattern: **`PPP / BFB / BBB` with F = furnace** (reads as an oven, no clash with the stove's campfire centre), or keep
  the current one if the clash check is clean. Owner's call.

### Gotchas
- `i.any_plate_iron` is a tag: test every member (IE, Create plates).

### Tests
OV-03 (oven.md 7) with the final pattern.

---

## Open side item (no row)

Brewin's `heating_cask` (`i.heating_cask`) is still not in `#creativeengineering:oven_heat_sources`
(`kubejs/server_scripts/tags/blocktags.js` line 27; checked 2026-10-31, the cask appears only in the walk-speed list at
294). The oven's "non-stove" rule suggests leaving it out. A one-line decision in oven.md 4; no TODO row.

## Built, needs play

| Row | Change | Open tiers | Tests | Writeup |
|---|---|---|---|---|
| oven #1 | Oven block, baking type, JEI, bakeware, baking recipes, counts option, hammered bakeware, hammer no-rotate | C P | OV-01 to OV-19 (OV-13 dropped) | [oven.md](../oven.md) 0, 7 |
| oven #4 | Tooltips on the Fantasy Furniture and CfB ovens; CfB oven smokes | C P | OV-23 (OV-24 passed, SQ-OF-06) | [oven.md](../oven.md) 2.8 |
| oven #5 | Muffin yield 4 from baking (pot route gone in the 2026-10-31 CD jar) | P | OV-20, OV-36 | [oven.md](../oven.md) 3.8, 7 |

## Writeup corrections made

- [oven.md](../oven.md) 0: "(placeholder pattern, 3.6)" -> "2.1" (the pattern is in 2.1; 3.6 is about FD changes). (2026-10-21)
- [oven.md](../oven.md) 4: "Crafting patterns for the oven and bakeware are placeholders" -> only the oven's is (bakeware
  is hammered, 2.5), linking this brief. (2026-10-21)
- [oven.md](../oven.md) 4: the heating cask "the pack's Brewin 2.0 jar doesn't have that block" -> the port has it;
  adding it is open. (2026-10-21)
