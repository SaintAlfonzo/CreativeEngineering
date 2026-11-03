# Can parity worksheet

Written 2026-10-13. Test prefix `CP-`. A hand-kept worksheet: one row per food in each canning family, so every food
that should have a can gets one, and every can gets a recipe, texture, value audit and effect check. Edit the cells
directly; nothing regenerates this file.

## 0. At a glance

| Part | Status |
|---|---|
| Raw veggie / fruit / meat cans (hand-written, [cans.js](../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js)) | 2026-10-13: removed mint, acorn, grapes, mite crust, raw ham/chevon/hoglin/kangaroo/moose/bison/bunfungus; White Strawberries got their own can; Canned Cactus is now Canned Cactus Flesh (and the item Cactus Chunk is renamed Cactus Flesh). Not run in game (CP-01 to CP-05) |
| Generated cans (cookies, cooked, bread, salad, soup, golden; [can_types.json](../kubejs/generation_scripts/food/can_types.json)) | DEPLOYED with 1.0.6, untested in game (see [food.md](food.md)) |
| Every remaining `x` row, `/` removals, alt inputs (2026-10-13) | BUILT, not run in game (CP-07 to CP-14): 17 raw fruit, shulker, 12 raw fish, 4 hazard, 2 special, 11 cookies, 10 cooked veggie, 3 cooked meat, 11 pastas, 9 pie fillings, 9 feasts, 2 drinks; 15 `/` cans removed; sliced alt inputs. 6 pie slices skipped (no source). Meal/pie/feast/drink/hazard values are placeholders ([3.1](#new-sections)) |
| Can crafts, family sprites, values (2026-10-21) | BUILT; values, recipes and removals scratch server PASSED 2026-10-22 (SQ-OF-00 to SQ-OF-03); not run in game (CP-15 to CP-21): the 20 buildable `Want x` rows without a can ([TODO](todo/TODO.md) `cans/canning #14`); the hand-written raw veggie / fruit / meat cans moved into gen_can_types.py (same items, recipes, values and servings; they now carry their food's effects, `#4`; Cactus Flesh follows the rule); Canned Cake 1 cake or 4 slices + sugar / honey (`#15`); Canned Goods' 13 cans that no recipe makes get their family's values, Canned Food and the Hot Hot Hotpot are removed; can-consuming recipes (filling + crust -> pie) are manual only and take full cans only (CP-22 to CP-25); every can without a sprite borrows its family's ([5.1](#sprites)). The 6 pie slices stay without a can (no source) |
| Hunger/saturation columns | FILLED 2026-10-21 by `tools/can_audit.py` (rerun after `gen_can_types.py`): Food / Craft sum / Canned h/s, In -> out and Out vs target per row, the full table in [4.3](#value-vs-goal). Values are what the files build; the game confirms them after a restart and `/dumpfood` |
| Priorities (scratchpad 2026-10-21) | can textures (48 of 235 built cans have their own sprite, [5.1](#sprites); [TODO](todo/TODO.md) `textures/food #1`) and can crafts (recipes for the rest of the `Want x` rows, then the `?` rows; `cans/canning #14`) |
| Potential cans (2026-10-20 food audit) | DECIDED 2026-10-20 (user): no `?` left (258 `x`, 493 `/`); 26 `x` rows have no can yet (20 to build, 6 pie slices with no source), `cans/canning #14`. Was: 263 rows at Want `?` waiting on a decision: new edible Brewin drinks (Beer, Vodka, Dread Nog), Gingerbread, two tarts, and every family pulled in from [3](#outside): [Hazard](#hazard) (4 built cans listed for the first time), [Egg and dairy](#egg-dairy), [Preserves](#preserves), [Desserts](#desserts), [Cake](#cake), [Sweets](#sweets), [Handheld](#handheld), [Ingredients](#ingredients) |
| Veggies Delight and Vintage Delight cans, vinegar additive (2026-10-23) | BUILT, needs a restart; not run in game (CP-27 to CP-31). Veggies: 24 new cans (5 raw veggie, 3 cooked veggie, 2 pastas, 3 breads, 4 salads, 5 soups, sweet potato pie filling with its oven and crust routes, plus Glistering Melon Slice from the golden loop). Vintage: 16 cans and 4 shared sources configured, skipped by `gen_can_types.py` (`AWAIT_DUMP`) until a `/dumpfood` with Vintage loaded. Vinegar additive recipe on every raw veggie, fruit, meat and fish can and every salad can (66 cans) |

**How to use it:** decide the `Want` cell first. Then fill the rest of the row as work lands.

| Cell | Meaning |
|---|---|
| `x` (yes) | done (or, for the h/s columns, the value is written in); in the `Want` column: canned |
| `/` (no) | wanted but not done yet; in the `Want` column: not canned |
| `n/a` | not canned (`Want` = no), so the column does not apply |
| `none` | Effects column only: the food has no effects, nothing to carry |
| `later` | h/s columns: not computed (no longer used: `tools/can_audit.py` fills every row with a can) |
| `-` | waiting on the `Want` decision |
| `?` | `Want` column only: undecided |
| `8 / 6+1` | Input column only: items in the basic recipe / items + 1 additive in the additive recipe (sliced forms share the count); `n/a` not canned, `-` no recipe yet |

**Columns:**

- **Want**: should this food have a can. Prefilled `x` where a can exists or the canning.js plan names it,
  `/` where removed or excluded by the plan (reason in the Item cell), `?` for everything else (none left since
  2026-10-20; `x*` = canned in the Cooking Pot).
- **Item**: name and id. Every food of the family's types in [food_types.json](../kubejs/generation_scripts/food/food_types.json)
  is listed, including the ones that will never be canned. Sliced forms are folded into their whole item's row.
- **Can**: the can's display name (can differ from the input: Cactus Flesh -> Canned Cactus Flesh, pie slices -> pie filling).
  Several items can share one can (bell pepper colours, bananas, hoglin plates).
- **Alt input (sliced)**: the sliced form, and whether the can's recipe takes it (`Raw Chicken Cuts: no`).
- **Impl**: the can item is registered (cans.js, the generated block, or a reused Canned Goods can).
- **Texture**: the can has its own sprite (a reused Canned Goods can, or a sprite from `textureswapper/canned/newcan`).
  `/` cans borrow their group's sprite (carrot, apple, raw beef, raw salmon) or Canned Goods' beetroot can. Where the
  sprites live: [5.1](#sprites).
- **Recipe**: a canning recipe takes this exact item.
- **Food audit**: the can's hunger/saturation is in the audit (`tools/can_audit.py`, [4.3](#value-vs-goal)).
- **Effects**: the food's effects reach the can. Every can copies them with scaled durations (the raw cans too
  since 2026-10-21), except where Canned Goods' own code applies them.
- **Food h/s**, **Craft sum h/s**, **Canned h/s**: hunger / saturation points (hunger x modifier x 2; AppleSkin shows
  half) of one plain food, of the basic recipe's ingredients, and of the whole can (servings x one serving).
- **In -> out**: the can as a percent of its basic recipe's ingredients, hunger / saturation (100% = equal; above 100%
  canning adds food). The additive recipe's percent is in [4.3](#value-vs-goal).
- **Out vs target**: the can as a percent of its family goal: servings x the section's meal (Baked Potato, Fruit Meal,
  Meat Meal, Fish Meal) or the source x the section multiplier. Off 100% means an own meal (shulker, maggots, pufferfish,
  White Strawberries) or hunger rounding.
- All five are written by `tools/can_audit.py`; edit the generator, not the cells.

## 1. Summary

2026-10-23: the 116 Veggies Delight and Vintage Delight rows added (64 + 54 foods, sliced forms folded); Vintage cans are configured and count as Want `x` but not Implemented until a `/dumpfood` with Vintage loaded. Recounted from the sheet rows 2026-10-21 after the can crafts build (Want, Impl, Texture, Recipe and Effects cells marked `x`; `x*` counts as `x`). Rows, not cans: shared cans count once per row (248 distinct cans have recipes: 227 pack cans and 21 reused Canned Goods cans; 13 more Canned Goods cans get food values only; Canned Food and the Hot Hot Hotpot are removed).

| Family | Items | Want x | Want ? | Want / | Implemented | Own texture | Recipe | Effects carried |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| [Raw veggie](#raw-veggie) | 28 | 18 | 0 | 10 | 19 | 12 | 19 | 0 |
| [Raw fruit](#raw-fruit) | 42 | 29 | 0 | 13 | 29 | 11 | 29 | 4 |
| [Raw meat](#raw-meat) | 23 | 6 | 0 | 17 | 6 | 3 | 6 | 2 |
| [Raw fish](#raw-fish) | 12 | 12 | 0 | 0 | 12 | 3 | 12 | 4 |
| [Cookies](#cookies) | 13 | 13 | 0 | 0 | 12 | 1 | 12 | 6 |
| [Cooked veggie and fruit](#cooked-veggie) | 32 | 29 | 0 | 3 | 23 | 2 | 23 | 8 |
| [Cooked meat](#cooked-meat) | 28 | 8 | 0 | 20 | 8 | 5 | 8 | 1 |
| [Cooked fish](#cooked-fish) | 14 | 11 | 0 | 3 | 8 | 2 | 8 | 0 |
| [Bread](#bread) | 15 | 11 | 0 | 4 | 11 | 1 | 11 | 3 |
| [Salad](#salad) | 16 | 13 | 0 | 3 | 12 | 0 | 12 | 10 |
| [Soup](#soup) | 39 | 35 | 0 | 4 | 32 | 2 | 32 | 27 |
| [Meals](#meal) | 82 | 19 | 0 | 63 | 17 | 0 | 17 | 16 |
| [Pie](#pie) | 21 | 19 | 0 | 2 | 13 | 0 | 13 | 12 |
| [Feasts](#feast) | 12 | 9 | 0 | 3 | 9 | 0 | 9 | 8 |
| [Drinks](#drink) | 69 | 2 | 0 | 67 | 2 | 1 | 2 | 0 |
| [Drinks: alcohol](#alcohol) | 44 | 0 | 0 | 44 | 0 | 0 | 0 | 0 |
| [Golden and enchanted golden](#golden) | 50 | 50 | 0 | 0 | 50 | 0 | 50 | 50 |
| [Special](#special) | 15 | 2 | 0 | 13 | 2 | 1 | 2 | 0 |
| [Hazard](#hazard) | 11 | 5 | 0 | 6 | 5 | 4 | 5 | 4 |
| [Egg and dairy](#egg-dairy) | 9 | 3 | 0 | 6 | 2 | 0 | 2 | 0 |
| [Preserves](#preserves) | 41 | 1 | 0 | 40 | 1 | 0 | 1 | 0 |
| [Desserts](#desserts) | 37 | 0 | 0 | 37 | 0 | 0 | 0 | 0 |
| [Cake](#cake) | 19 | 0 | 0 | 19 | 0 | 0 | 0 | 0 |
| [Sweets](#sweets) | 105 | 2 | 0 | 103 | 2 | 0 | 2 | 1 |
| [Handheld](#handheld) | 68 | 1 | 0 | 67 | 1 | 0 | 1 | 0 |
| [Ingredients](#ingredients) | 22 | 1 | 0 | 21 | 1 | 0 | 1 | 0 |
| **Total** | **867** | **299** | **0** | **568** | **277** | **48** | **277** | **156** |

## 2. Sheets

### 2.1 Raw veggie <a id="raw-veggie"></a>

Baked Potato meals (5, 6), 2 servings. Additive: salt or bone broth.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Beetroot `minecraft:beetroot` | Canned Beetroot | 8 / 6+1 | - | x | x | x | x | none | 1 / 1.2 | 8 / 9.6 | 10 / 12 | 125% / 125% | 100% / 100% |
| x | Bell Pepper `rusticdelight:bell_pepper_*` | Canned Bell Pepper | 8 / 6+1 | - | x | x | x | x | none | 1 / 0.2 | 8 / 1.6 | 10 / 12 | 125% / 750% | 100% / 100% |
| x | Cabbage `farmersdelight:cabbage` | Canned Cabbage | 5 / 3+1 | Cabbage Leaf: x | x | x | x | x | none | 2 / 1.6 | 10 / 8 | 10 / 12 | 100% / 150% | 100% / 100% |
| x | Cactus Flesh `delightful:cactus_flesh` | Canned Cactus Flesh | 4 / 3+1 | - | x | / | x | x | none | 3 / 1.8 | 12 / 7.2 | 10 / 12 | 83% / 167% | 100% / 100% |
| x | Carrot `minecraft:carrot` | Canned Carrot | 3 / 2+1 | - | x | x | x | x | none | 3 / 3.6 | 9 / 10.8 | 10 / 12 | 111% / 111% | 100% / 100% |
| x | Gongylidia `alexsmobs:gongylidia` | Canned Gongylidia | 2 / 1+1 | - | x | x | x | x | none | 3 / 7.2 | 6 / 14.4 | 10 / 12 | 167% / 83% | 100% / 100% |
| x | Onion `farmersdelight:onion` | Canned Onion | 5 / 3+1 | - | x | x | x | x | none | 2 / 1.6 | 10 / 8 | 10 / 12 | 100% / 150% | 100% / 100% |
| x | Potato `minecraft:potato` | Canned Potato | 8 / 6+1 | Potato Slices: x | x | x | x | x | none | 1 / 0.6 | 8 / 4.8 | 10 / 12 | 125% / 250% | 100% / 100% |
| x | Pumpkin Slice `farmersdelight:pumpkin_slice` | Canned Pumpkin | 4 / 3+1 | - | x | x | x | x | none | 3 / 1.8 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Tomato `farmersdelight:tomato` | Canned Tomato | 8 / 6+1 | - | x | x | x | x | none | 1 / 0.6 | 8 / 4.8 | 10 / 12 | 125% / 250% | 100% / 100% |
| x | Maize `corn_delight:corn` | Canned Corn | 6 / 4+1 | Corn Kernel: no (not canned) | x | / | x | x | none | 2 / 0.8 | 12 / 4.8 | 10 / 12 | 83% / 250% | 100% / 100% |
| x | Broccoli `veggiesdelight:broccoli` | Canned Broccoli | 5 / 3+1 | - | x | / | x | x | none | 2 / 1.6 | 10 / 8 | 10 / 12 | 100% / 150% | 100% / 100% |
| x | Cauliflower `veggiesdelight:cauliflower` | Canned Cauliflower | 2 / 1+1 | Cauliflower Floret: x | x | / | x | x | none | 4 / 3.2 | 8 / 6.4 | 10 / 12 | 125% / 188% | 100% / 100% |
| x | Cucumber `vintagedelight:cucumber` (configured; waits on /dumpfood with Vintage loaded) | Canned Cucumber | - | - | - | - | - | x | - | 2 / 0.8 | 12 / 4.8 | 10 / 12 | 83% / 250% | 100% / 100% |
| x | Ghost Pepper `vintagedelight:ghost_pepper` (configured; waits on /dumpfood with Vintage loaded) | Canned Ghost Pepper | - | - | - | - | - | x | - | 3 / 1.2 | 12 / 4.8 | 10 / 12 | 83% / 250% | 100% / 100% |
| x | Truffle `fungidelight:truffle` (fungi_delight.md step 8; waits on /dumpfood after the restart) | Canned Truffle | - | Truffle Slice: x | - | - | - | x | - | 6 / 7.2 | 6 / 7.2 | 10 / 12 | 167% / 167% | 100% / 100% |
| x | Sweet Potato `veggiesdelight:sweet_potato` | Canned Sweet Potato | 5 / 3+1 | - | x | / | x | x | none | 2 / 1.6 | 10 / 8 | 10 / 12 | 100% / 150% | 100% / 100% |
| x | Turnip `veggiesdelight:turnip` | Canned Turnip | 5 / 3+1 | - | x | / | x | x | none | 2 / 1.6 | 10 / 8 | 10 / 12 | 100% / 150% | 100% / 100% |
| x | Zucchini `veggiesdelight:zucchini` | Canned Zucchini | 3 / 2+1 | Zucchini Slice: x | x | / | x | x | none | 3 / 2.4 | 9 / 7.2 | 10 / 12 | 111% / 167% | 100% / 100% |
| / | Corn Kernel `corn_delight:corn_seeds` (not canned since 2026-10-21: a maize cuts into 4 kernels; can the maize) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Green Bell Pepper `rusticdelight:bell_pepper_green` | Canned Bell Pepper | n/a | - | x | x | x | / | none | later | later | later | n/a | n/a |
| / | Red Bell Pepper `rusticdelight:bell_pepper_red` | Canned Bell Pepper | n/a | - | x | x | x | / | none | later | later | later | n/a | n/a |
| / | Yellow Bell Pepper `rusticdelight:bell_pepper_yellow` | Canned Bell Pepper | n/a | - | x | x | x | / | none | later | later | later | n/a | n/a |
| / | Mint Leaves `neapolitan:mint_leaves` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Diced Potatoes `moredelight:diced_potatoes` | Canned Diced Potatoes | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Dandelion Leaf `veggiesdelight:dandelion_leaf` (seasoning) | Canned Dandelion Leaf | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Garlic Clove `veggiesdelight:garlic_clove` (seasoning) | Canned Garlic Clove | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Raw Oats `vintagedelight:raw_oats` (a grain, as corn kernels) | Canned Raw Oats | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Raw Vegetarian Patty `veggiesdelight:raw_vegetarian_patty` (patties are not canned) | Canned Raw Vegetarian Patty | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

### 2.2 Raw fruit <a id="raw-fruit"></a>

Fruit Meals (5, 3), 3 servings. Additive: sugar or honey.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Apple `minecraft:apple` | Canned Apple | 3 / 2+1 | - | x | x | x | x | none | 4 / 2.4 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Banana `neapolitan:banana` | Canned Banana | 7 / 5+1 | - | x | x | x | x | x | 2 / 0.8 | 14 / 5.6 | 15 / 9 | 107% / 161% | 100% / 100% |
| x | Cantaloupe Slice `delightful:cantaloupe_slice` | Canned Cantaloupe | 4 / 3+1 | - | x | x | x | x | none | 3 / 1.8 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Cherry `vinery:cherry` | Canned Cherries | 6 / 4+1 | - | x | x | x | x | none | 2 / 1.2 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Glow Berries `minecraft:glow_berries` | Canned Glow Berries | 8 / 6+1 | - | x | x | x | x | x | 2 / 0.4 | 16 / 3.2 | 15 / 9 | 94% / 281% | 100% / 100% |
| x | Melon Slice `minecraft:melon_slice` | Canned Melon | 6 / 4+1 | - | x | x | x | x | none | 2 / 1.2 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Mulberry `upgrade_aquatic:mulberry` | Canned Mulberries | 5 / 3+1 | - | x | x | x | x | none | 3 / 0.6 | 15 / 3 | 15 / 9 | 100% / 300% | 100% / 100% |
| x | Salmonberries `delightful:salmonberries` | Canned Salmonberries | 4 / 3+1 | - | x | x | x | x | x | 2 / 2.4 | 8 / 9.6 | 15 / 9 | 188% / 94% | 100% / 100% |
| x | Strawberries `neapolitan:strawberries` | Canned Strawberries | 6 / 4+1 | - | x | x | x | x | none | 3 / 0.3 | 18 / 1.8 | 15 / 9 | 83% / 500% | 100% / 100% |
| x | White Strawberries `neapolitan:white_strawberries` | Canned White Strawberries | 3 / 2+1 | - | x | - | x | x | none | 5 / 0.5 | 15 / 1.5 | 21 / 15 | 140% / 1000% | 140% / 167% |
| x | Sweet Berries `minecraft:sweet_berries` | Canned Sweet Berries | 8 / 6+1 | - | x | x | x | x | none | 2 / 0.4 | 16 / 3.2 | 15 / 9 | 94% / 281% | 100% / 100% |
| x | Bayberry `fruitsdelight:bayberry` | Canned Bayberries | 6 / 4+1 | - | x | - | x | x | none | 2 / 1.2 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Blueberry `fruitsdelight:blueberry` | Canned Blueberries | 6 / 4+1 | - | x | - | x | x | none | 2 / 1.2 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Coffee Berries `farmersrespite:coffee_berries` | Canned Coffee Berries | 5 / 3+1 | - | x | / | x | x | x | 2 / 1.6 | 10 / 8 | 15 / 9 | 150% / 112% | 100% / 100% |
| x | Cranberry `fruitsdelight:cranberry` | Canned Cranberries | 6 / 4+1 | - | x | x | x | x | none | 2 / 1.2 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Durian Flesh `fruitsdelight:durian_flesh` | Canned Durian | 3 / 2+1 | - | x | - | x | x | none | 4 / 2.4 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Fig `fruitsdelight:fig` | Canned Figs | 4 / 3+1 | - | x | - | x | x | none | 3 / 1.8 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Hamimelon Slice `fruitsdelight:hamimelon_slice` | Canned Hamimelon | 6 / 4+1 | - | x | - | x | x | none | 2 / 1.2 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Hawberry `fruitsdelight:hawberry` | Canned Hawberries | 6 / 4+1 | - | x | - | x | x | none | 2 / 1.2 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Kiwi `fruitsdelight:kiwi` | Canned Kiwi | 4 / 3+1 | - | x | - | x | x | none | 3 / 1.8 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Lemon `fruitsdelight:lemon` | Canned Lemon | 3 / 2+1 | Lemon Slice: x | x | - | x | x | none | 4 / 2.4 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Lychee `fruitsdelight:lychee` | Canned Lychee | 6 / 4+1 | - | x | - | x | x | none | 2 / 1.2 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Mango `fruitsdelight:mango` | Canned Mango | 4 / 3+1 | - | x | - | x | x | none | 3 / 1.8 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Mangosteen `fruitsdelight:mangosteen` | Canned Mangosteen | 4 / 3+1 | - | x | - | x | x | none | 3 / 1.8 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Orange `fruitsdelight:orange` | Canned Orange | 4 / 3+1 | Orange Slice: x | x | - | x | x | none | 3 / 1.8 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Peach `fruitsdelight:peach` | Canned Peach | 4 / 3+1 | - | x | - | x | x | none | 3 / 1.8 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Pear `fruitsdelight:pear` | Canned Pear | 4 / 3+1 | - | x | - | x | x | none | 3 / 1.8 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Persimmon `fruitsdelight:persimmon` | Canned Persimmon | 4 / 3+1 | - | x | - | x | x | none | 3 / 1.8 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Pineapple Slice `fruitsdelight:pineapple_slice` | Canned Pineapple | 6 / 4+1 | - | x | - | x | x | none | 2 / 1.2 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| / | Acorn `delightful:acorn` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Lemon Slice `fruitsdelight:lemon_slice` (alt input of Canned Lemon) | Canned Lemon Slice | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Orange Slice `fruitsdelight:orange_slice` (alt input of Canned Orange) | Canned Orange Slice | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Banana `alexsmobs:banana` (not canned, merged into the Neapolitan banana can) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Red Grape `vinery:red_grape` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Red Jungle Grapes `vinery:jungle_grapes_red` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Red Savanna Grapes `vinery:savanna_grapes_red` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Red Taiga Grapes `vinery:taiga_grapes_red` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | White Grape `vinery:white_grape` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | White Jungle Grapes `vinery:jungle_grapes_white` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | White Savanna Grapes `vinery:savanna_grapes_white` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | White Taiga Grapes `vinery:taiga_grapes_white` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Peanut `vintagedelight:peanut` (raw; the roasted peanut is canned) | Canned Peanut | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

- removing alex's mobs banana, merge w/ neapolitan (done 2026-10-13: the can only takes Neapolitan bananas)

### 2.3 Raw meat <a id="raw-meat"></a>

Meat Meals (5, 3), 4 servings. Additive: salt or bone broth.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Raw Beef `minecraft:beef` | Canned Raw Beef | 5 / 4+1 | Minced Beef: x | x | x | x | x | none | 3 / 1.8 | 15 / 9 | 20 / 12 | 133% / 133% | 100% / 100% |
| x | Raw Chicken `minecraft:chicken` | Canned Raw Chicken | 8 / 6+1 | Raw Chicken Cuts: x | x | / | x | x | x | 2 / 1.2 | 16 / 9.6 | 20 / 12 | 125% / 125% | 100% / 100% |
| x | Raw Mutton `minecraft:mutton` | Canned Raw Mutton | 8 / 6+1 | Raw Mutton Chops: x | x | x | x | x | none | 2 / 1.2 | 16 / 9.6 | 20 / 12 | 125% / 125% | 100% / 100% |
| x | Raw Porkchop `minecraft:porkchop` | Canned Raw Pork | 5 / 4+1 | Raw Bacon: x | x | x | x | x | none | 3 / 1.8 | 15 / 9 | 20 / 12 | 133% / 133% | 100% / 100% |
| x | Raw Rabbit `minecraft:rabbit` | Canned Raw Rabbit | 5 / 4+1 | - | x | / | x | x | none | 3 / 1.8 | 15 / 9 | 20 / 12 | 133% / 133% | 100% / 100% |
| x | Shulker Mollusk `endersdelight:shulker_mollusk` | Canned Shulker Flesh | 8 / 6+1 | Shulker Filet: x | x | / | x | x | x | 1 / 0.2 | 8 / 1.6 | 8 / 4 | 100% / 250% | 40% / 33% |
| / | Ground Strider `nethersdelight:ground_strider` | Canned Ground Strider | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Hoglin Ear `nethersdelight:hoglin_ear` | Canned Hoglin Ear | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Raw Duck `untitledduckmod:raw_duck` | Canned Raw Duck | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Raw Duck Fillet `abnormals_delight:duck_fillet` | Canned Raw Duck Fillet | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Raw Goose `untitledduckmod:raw_goose` | Canned Raw Goose | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Raw Mint Chops `neapolitan:mint_chops` | Canned Raw Mint Chops | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Raw Venison Chops `delightful:venison_chops` | Canned Raw Venison Chops | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Raw Venison Shanks `abnormals_delight:venison_shanks` | Canned Raw Venison Shanks | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Strider Slice `nethersdelight:strider_slice` | Canned Strider Slice | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Ham `farmersdelight:ham` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Hoglin Loin `nethersdelight:hoglin_loin` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Kangaroo Meat `alexsmobs:kangaroo_meat` (removed 2026-10-13) | n/a | n/a | Kangaroo Shank | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Mite Crust `endersdelight:mite_crust` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Raw Bison `alexsdelight:raw_bison` (removed 2026-10-13) | n/a | n/a | Bison Mince | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Raw Bunfungus `alexsdelight:raw_bunfungus` (removed 2026-10-13) | n/a | n/a | Raw Bunfungus Drumstick | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Raw Chevon `delightful:raw_goat` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Raw Moose Ribs `alexsmobs:moose_ribs` (removed 2026-10-13) | n/a | n/a | Loose Moose Rib | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.4 Raw fish <a id="raw-fish"></a>

Raw meat's goal: Meat Meals (5, 3), 4 servings, salt or bone broth. Generated (section raw_fish). Every sliced fish is an
alt input: 8 slices make a half full can (2 of 4 servings).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Blobfish `alexsmobs:blobfish` | Canned Blobfish | 4 / 3+1 | - | x | / | x | x | x | 3 / 2.4 | 12 / 9.6 | 16 / 8 | 133% / 83% | 100% / 100% |
| x | Flying Fish `alexsmobs:flying_fish` | Canned Flying Fish | 4 / 3+1 | - | x | / | x | x | none | 3 / 2.4 | 12 / 9.6 | 16 / 8 | 133% / 83% | 100% / 100% |
| x | Lobster Tail `alexsmobs:lobster_tail` | Canned Lobster Tail | 5 / 4+1 | - | x | / | x | x | none | 2 / 1.6 | 10 / 8 | 16 / 8 | 160% / 100% | 100% / 100% |
| x | Raw Calamari `rusticdelight:calamari` | Canned Raw Calamari | 7 / 6+1 | Raw Calamari Slice: x | x | / | x | x | none | 2 / 0.8 | 14 / 5.6 | 16 / 8 | 114% / 143% | 100% / 100% |
| x | Raw Catfish `alexsmobs:raw_catfish` | Canned Raw Catfish | 6 / 5+1 | Raw Catfish Slice: x | x | / | x | x | none | 2 / 1.2 | 12 / 7.2 | 16 / 8 | 133% / 111% | 100% / 100% |
| x | Raw Cod `minecraft:cod` | Canned Raw Cod | 8 / 6+1 | Raw Cod Slice: x | x | x | x | x | none | 2 / 0.4 | 16 / 3.2 | 16 / 8 | 100% / 250% | 100% / 100% |
| x | Raw Crab Leg `quark:crab_leg` | Canned Raw Crab Leg | 8 / 6+1 | - | x | / | x | x | none | 1 / 0.6 | 8 / 4.8 | 16 / 8 | 200% / 167% | 100% / 100% |
| x | Raw Lionfish `upgrade_aquatic:lionfish` | Canned Raw Lionfish | 6 / 5+1 | - | x | / | x | x | x | 2 / 1.2 | 12 / 7.2 | 16 / 8 | 133% / 111% | 100% / 100% |
| x | Raw Perch `upgrade_aquatic:perch` | Canned Raw Perch | 7 / 6+1 | Raw Perch Slice: x | x | / | x | x | x | 2 / 0.8 | 14 / 5.6 | 16 / 8 | 114% / 143% | 100% / 100% |
| x | Raw Pike `upgrade_aquatic:pike` | Canned Raw Pike | 4 / 3+1 | Raw Pike Slice: x | x | / | x | x | x | 3 / 1.8 | 12 / 7.2 | 16 / 8 | 133% / 111% | 100% / 100% |
| x | Raw Salmon `minecraft:salmon` | Canned Raw Salmon | 8 / 6+1 | Raw Salmon Slice: x | x | x | x | x | none | 2 / 0.4 | 16 / 3.2 | 16 / 8 | 100% / 250% | 100% / 100% |
| x | Tropical Fish `minecraft:tropical_fish` | Canned Tropical Fish | 8 / 6+1 | - | x | x | x | x | none | 1 / 0.2 | 8 / 1.6 | 16 / 8 | 200% / 500% | 100% / 100% |

### 2.5 Cookies <a id="cookies"></a>

Cookie x2 meals, 5 servings, fast. Additive: sugar.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Cookie `minecraft:cookie` | Canned Cookies | 8 / 6+1 | - | x | x | x | x | none | 2 / 1.2 | 16 / 9.6 | 20 / 12 | 125% / 125% | 100% / 100% |
| x | Bayberry Cookie `fruitsdelight:bayberry_cookie` | Canned Bayberry Cookies | 8 / 6+1 | - | x | - | x | x | x | 1 / 0.6 | 8 / 4.8 | 10 / 6 | 125% / 125% | 100% / 100% |
| x | Cherry Cookie `abnormals_delight:cherry_cookie` | Canned Cherry Cookies | 8 / 6+1 | - | x | - | x | x | none | 2 / 0.4 | 16 / 3.2 | 20 / 4 | 125% / 125% | 100% / 100% |
| x | Cranberry Cookie `fruitsdelight:cranberry_cookie` | Canned Cranberry Cookies | 8 / 6+1 | - | x | - | x | x | x | 1 / 0.6 | 8 / 4.8 | 10 / 6 | 125% / 125% | 100% / 100% |
| x | Green Tea Cookie `farmersrespite:green_tea_cookie` | Canned Green Tea Cookies | 8 / 6+1 | - | x | - | x | x | x | 2 / 0.4 | 16 / 3.2 | 20 / 4 | 125% / 125% | 100% / 100% |
| x | Honey Cookie `farmersdelight:honey_cookie` | Canned Honey Cookies | 8 / 6+1 | - | x | - | x | x | none | 2 / 0.4 | 16 / 3.2 | 20 / 4 | 125% / 125% | 100% / 100% |
| x | Lemon Cookie `fruitsdelight:lemon_cookie` | Canned Lemon Cookies | 8 / 6+1 | - | x | - | x | x | x | 1 / 0.6 | 8 / 4.8 | 10 / 6 | 125% / 125% | 100% / 100% |
| x | Maple Cookie `abnormals_delight:maple_cookie` | Canned Maple Cookies | 8 / 6+1 | - | x | - | x | x | none | 2 / 0.4 | 16 / 3.2 | 20 / 4 | 125% / 125% | 100% / 100% |
| x | Mulberry Cookie `abnormals_delight:mulberry_cookie` | Canned Mulberry Cookies | 8 / 6+1 | - | x | - | x | x | none | 2 / 0.4 | 16 / 3.2 | 20 / 4 | 125% / 125% | 100% / 100% |
| x | Persimmon Cookie `fruitsdelight:persimmon_cookie` | Canned Persimmon Cookies | 8 / 6+1 | - | x | - | x | x | x | 1 / 0.6 | 8 / 4.8 | 10 / 6 | 125% / 125% | 100% / 100% |
| x | Sweet Berry Cookie `farmersdelight:sweet_berry_cookie` | Canned Sweet Berry Cookies | 8 / 6+1 | - | x | - | x | x | none | 2 / 0.4 | 16 / 3.2 | 20 / 4 | 125% / 125% | 100% / 100% |
| x | Uncanny Cookie `endersdelight:uncanny_cookies` | Canned Uncanny Cookies | 8 / 6+1 | - | x | - | x | x | x | 5 / 6 | 40 / 48 | 50 / 60 | 125% / 125% | 100% / 100% |
| x | Oatmeal Cookie `vintagedelight:oatmeal_cookie` (configured; waits on /dumpfood with Vintage loaded) | Canned Oatmeal Cookies | - | - | - | - | - | x | - | 2 / 1.2 | 16 / 9.6 | 20 / 12 | 125% / 125% | 100% / 100% |

### 2.6 Cooked veggie and fruit <a id="cooked-veggie"></a>

Item x2 meals, 4 servings. Additive: salt.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Baked Potato `minecraft:baked_potato` | Canned Baked Potato | 6 / 5+1 | Baked Potato Slices: x | x | x | x | x | none | 5 / 6 | 30 / 36 | 40 / 48 | 133% / 133% | 100% / 100% |
| x | Cactus Steak `delightful:cactus_steak` | Canned Cactus Steak | 6 / 5+1 | - | x | / | x | x | none | 4 / 4 | 24 / 24 | 32 / 32 | 133% / 133% | 100% / 100% |
| x | Cooked Rice `farmersdelight:cooked_rice` | Canned Cooked Rice | 6 / 5+1 | - | x | / | x | x | x | 6 / 4.8 | 36 / 28.8 | 48 / 38.4 | 133% / 133% | 100% / 100% |
| x | Dried Kelp `minecraft:dried_kelp` | Canned Dried Kelp | 6 / 5+1 | - | x | x | x | x | none | 1 / 0.6 | 6 / 3.6 | 8 / 4.8 | 133% / 133% | 100% / 100% |
| x | Roasted Green Bell Pepper `rusticdelight:roasted_bell_pepper_green` | Canned Roasted Bell Pepper | 6 / 5+1 | - | x | / | x | x | none | 5 / 8 | 30 / 48 | 40 / 64 | 133% / 133% | 100% / 100% |
| x | Roasted Red Bell Pepper `rusticdelight:roasted_bell_pepper_red` | Canned Roasted Bell Pepper | 6 / 5+1 | - | x | / | x | x | none | 5 / 8 | 30 / 48 | 40 / 64 | 133% / 133% | 100% / 100% |
| x | Roasted Yellow Bell Pepper `rusticdelight:roasted_bell_pepper_yellow` | Canned Roasted Bell Pepper | 6 / 5+1 | - | x | / | x | x | none | 5 / 8 | 30 / 48 | 40 / 64 | 133% / 133% | 100% / 100% |
| x | Baked Durian `fruitsdelight:baked_durian` | Canned Baked Durian | 6 / 5+1 | - | x | - | x | x | x | 4 / 2.4 | 24 / 14.4 | 32 / 19.2 | 133% / 133% | 100% / 100% |
| x | Baked Pear `fruitsdelight:baked_pear` | Canned Baked Pear | 6 / 5+1 | - | x | - | x | x | x | 4 / 2.4 | 24 / 14.4 | 32 / 19.2 | 133% / 133% | 100% / 100% |
| x | Boiled Blue Pickerelweed `upgrade_aquatic:boiled_blue_pickerelweed` | Canned Boiled Blue Pickerelweed | 6 / 5+1 | - | x | - | x | x | none | 2 / 0 | 12 / 0 | 16 / 0 | 133% / - | 100% / - |
| x | Boiled Corn `corn_delight:boiled_corn` | Canned Boiled Corn | 6 / 5+1 | - | x | - | x | x | none | 6 / 2.4 | 36 / 14.4 | 48 / 19.2 | 133% / 133% | 100% / 100% |
| x | Boiled Purple Pickerelweed `upgrade_aquatic:boiled_purple_pickerelweed` | Canned Boiled Purple Pickerelweed | 6 / 5+1 | - | x | - | x | x | none | 3 / 0 | 18 / 0 | 24 / 0 | 133% / - | 100% / - |
| x | Chips `pneumaticcraft:chips` | Canned Chips | 6 / 5+1 | - | x | / | x | x | none | 5 / 6 | 30 / 36 | 40 / 48 | 133% / 133% | 100% / 100% |
| x | Dried Banana `neapolitan:dried_banana` | Canned Dried Banana | 6 / 5+1 | - | x | - | x | x | x | 4 / 2.4 | 24 / 14.4 | 32 / 19.2 | 133% / 133% | 100% / 100% |
| x | Dried Persimmon `fruitsdelight:dried_persimmon` | Canned Dried Persimmon | 6 / 5+1 | - | x | - | x | x | x | 4 / 2.4 | 24 / 14.4 | 32 / 19.2 | 133% / 133% | 100% / 100% |
| x | Grilled Corn `corn_delight:grilled_corn` | Canned Grilled Corn | 6 / 5+1 | - | x | / | x | x | none | 6 / 2.4 | 36 / 14.4 | 48 / 19.2 | 133% / 133% | 100% / 100% |
| x | Kimchi `brewinandchewin:kimchi` | Canned Kimchi | 6 / 5+1 | - | x | - | x | x | x | 2 / 2.4 | 12 / 14.4 | 16 / 19.2 | 133% / 133% | 100% / 100% |
| x | Pickled Pickles `brewinandchewin:pickled_pickles` | Canned Pickled Pickles | 6 / 5+1 | - | x | - | x | x | x | 4 / 2.4 | 24 / 14.4 | 32 / 19.2 | 133% / 133% | 100% / 100% |
| x | Roasted Adzuki Beans `neapolitan:roasted_adzuki_beans` | Canned Roasted Adzuki Beans | 6 / 5+1 | - | x | - | x | x | x | 3 / 2.4 | 18 / 14.4 | 24 / 19.2 | 133% / 133% | 100% / 100% |
| x | Tortilla Chip `corn_delight:tortilla_chip` | Canned Tortilla Chips | 6 / 5+1 | - | x | / | x | x | none | 1 / 0.2 | 6 / 1.2 | 8 / 1.6 | 133% / 133% | 100% / 100% |
| x | Baked Sweet Potato `veggiesdelight:baked_sweet_potato` | Canned Baked Sweet Potato | 6 / 5+1 | - | x | / | x | x | none | 4 / 4 | 24 / 24 | 32 / 32 | 133% / 133% | 100% / 100% |
| x | Cooked Inky Cap `fungidelight:cooked_cleaned_cap` (fungi_delight.md step 8; waits on /dumpfood after the restart) | Canned Cooked Inky Cap | - | - | - | - | - | x | - | 6 / 7.2 | 36 / 43.2 | 48 / 57.6 | 133% / 133% | 100% / 100% |
| x | Cooked Nether Wart `roll_delight:cooked_nether_wart` (Roll Delight port, roll_delight.md 4.1 D14; configured; waits on /dumpfood with the member loaded) | Canned Cooked Nether Wart | - | - | - | - | - | x | - | 6 / 3.6 | 36 / 21.6 | 48 / 28.8 | 133% / 133% | 100% / 100% |
| x | Kimchi `vintagedelight:kimchi` (fills Canned Kimchi; waits on /dumpfood with Vintage loaded) | Canned Kimchi | - | - | - | - | - | x | - | 4 / 2.4 | 12 / 14.4 | 16 / 19.2 | 133% / 133% | 100% / 100% |
| x | Pickle `vintagedelight:pickle` (fills Canned Pickled Pickles; waits on /dumpfood with Vintage loaded) | Canned Pickled Pickles | - | - | - | - | - | x | - | 6 / 3.6 | 24 / 14.4 | 32 / 19.2 | 133% / 133% | 100% / 100% |
| x | Pickled Beetroot `vintagedelight:pickled_beetroot` (configured; waits on /dumpfood with Vintage loaded) | Canned Pickled Beetroot | - | - | - | - | - | x | - | 5 / 4 | 30 / 24 | 40 / 32 | 133% / 133% | 100% / 100% |
| x | Pickled Onion `vintagedelight:pickled_onion` (configured; waits on /dumpfood with Vintage loaded) | Canned Pickled Onions | - | - | - | - | - | x | - | 6 / 2.4 | 36 / 14.4 | 48 / 19.2 | 133% / 133% | 100% / 100% |
| x | Pickled Pepper `vintagedelight:pickled_pepper` (configured; waits on /dumpfood with Vintage loaded) | Canned Pickled Peppers | - | - | - | - | - | x | - | 6 / 3.6 | 36 / 21.6 | 48 / 28.8 | 133% / 133% | 100% / 100% |
| x | Roasted Cauliflower Floret `veggiesdelight:roasted_cauliflower_floret` | Canned Roasted Cauliflower | 6 / 5+1 | - | x | / | x | x | none | 2 / 1.2 | 12 / 7.2 | 16 / 9.6 | 133% / 133% | 100% / 100% |
| x | Roasted Peanut `vintagedelight:roasted_peanut` (configured; waits on /dumpfood with Vintage loaded) | Canned Roasted Peanuts | - | - | - | - | - | x | - | 4 / 1.6 | 24 / 9.6 | 32 / 12.8 | 133% / 133% | 100% / 100% |
| x | Roasted Zucchini `veggiesdelight:roasted_zucchini` | Canned Roasted Zucchini | 6 / 5+1 | - | x | / | x | x | none | 4 / 3.2 | 24 / 19.2 | 32 / 25.6 | 133% / 133% | 100% / 100% |
| / | Popcorn `corn_delight:popcorn` | Canned Popcorn | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cooked Vegetarian Patty `veggiesdelight:cooked_vegetarian_patty` (patties are not canned cooked) | Canned Cooked Vegetarian Patty | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Roasted Garlic Clove `veggiesdelight:roasted_garlic_clove` (seasoning) | Canned Roasted Garlic Clove | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

### 2.7 Cooked meat <a id="cooked-meat"></a>

Item x2 meals, 3 servings. Additive: salt or bone broth.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| / | Beef Patty `farmersdelight:beef_patty` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Bison Patty `alexsdelight:bison_patty` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Bacon `farmersdelight:cooked_bacon` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Bison `alexsdelight:cooked_bison` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Bunfungus `alexsdelight:cooked_bunfungus` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Chevon `delightful:cooked_goat` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Cooked Chicken `minecraft:cooked_chicken` | Canned Cooked Chicken | 5 / 4+1 | Cooked Chicken Cuts: x | x | x | x | x | none | 6 / 7.2 | 30 / 36 | 36 / 43.2 | 120% / 120% | 100% / 100% |
| / | Cooked Kangaroo Meat `alexsmobs:cooked_kangaroo_meat` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Kangaroo Shank `alexsdelight:cooked_kangaroo_shank` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Loose Moose Rib `alexsdelight:cooked_loose_moose_rib` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Moose Ribs `alexsmobs:cooked_moose_ribs` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Cooked Mutton `minecraft:cooked_mutton` | Canned Cooked Mutten | 5 / 4+1 | Cooked Mutton Chops: x | x | x | x | x | none | 6 / 9.6 | 30 / 48 | 36 / 57.6 | 120% / 120% | 100% / 100% |
| x | Cooked Porkchop `minecraft:cooked_porkchop` | Canned Cooked Pork | 5 / 4+1 | - | x | x | x | x | none | 8 / 12.8 | 40 / 64 | 48 / 76.8 | 120% / 120% | 100% / 100% |
| x | Cooked Rabbit `minecraft:cooked_rabbit` | Canned Cooked Rabbit | 5 / 4+1 | - | x | x | x | x | none | 5 / 6 | 25 / 30 | 30 / 36 | 120% / 120% | 100% / 100% |
| / | Hoglin Sirloin `nethersdelight:hoglin_sirloin` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Smoked Ham `farmersdelight:smoked_ham` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Steak `minecraft:cooked_beef` | Canned Steak | 5 / 4+1 | - | x | x | x | x | none | 8 / 12.8 | 40 / 64 | 48 / 76.8 | 120% / 120% | 100% / 100% |
| / | Cooked Bunfungus Drumstick `alexsdelight:cooked_bunfungus_drumstick` | Canned Cooked Bunfungus Drumstick | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cooked Centipede Leg `alexsdelight:cooked_centipede_leg` | Canned Cooked Centipede Leg | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cooked Duck `untitledduckmod:cooked_duck` | Canned Cooked Duck | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cooked Duck Fillet `abnormals_delight:cooked_duck_fillet` | Canned Cooked Duck Fillet | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cooked Goose `untitledduckmod:cooked_goose` | Canned Cooked Goose | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cooked Mint Chops `neapolitan:cooked_mint_chops` | Canned Cooked Mint Chops | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cooked Venison Chops `delightful:cooked_venison_chops` | Canned Cooked Venison Chops | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cooked Venison Shanks `abnormals_delight:cooked_venison_shanks` | Canned Cooked Venison Shanks | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| x | Honey Glazed Porkchop `buzzier_bees:glazed_porkchop` | Canned Honey Glazed Porkchop | 5 / 4+1 | - | x | - | x | x | none | 9 / 14.4 | 45 / 72 | 54 / 86.4 | 120% / 120% | 100% / 100% |
| x | Jerky `brewinandchewin:jerky` | Canned Jerky | 5 / 4+1 | - | x | - | x | x | none | 3 / 4.2 | 15 / 21 | 18 / 25.2 | 120% / 120% | 100% / 100% |
| x | Maple Glazed Bacon `abnormals_delight:maple_glazed_bacon` | Canned Maple Glazed Bacon | 5 / 4+1 | - | x | - | x | x | x | 6 / 9.6 | 30 / 48 | 36 / 57.6 | 120% / 120% | 100% / 100% |

### 2.8 Cooked fish <a id="cooked-fish"></a>

Same section as cooked meat (cooked_meat in can_types.json).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Cooked Calamari `rusticdelight:cooked_calamari` | Canned Cooked Calamari | 5 / 4+1 | Cooked Calamari Slice: x | x | / | x | x | none | 5 / 6 | 25 / 30 | 30 / 36 | 120% / 120% | 100% / 100% |
| x | Cooked Catfish `alexsmobs:cooked_catfish` | Canned Cooked Catfish | 5 / 4+1 | - | x | / | x | x | none | 5 / 5 | 25 / 25 | 30 / 30 | 120% / 120% | 100% / 100% |
| x | Cooked Catfish Slice `alexsdelight:cooked_catfish_slice` | Canned Cooked Catfish | 5 / 4+1 | - | x | / | x | x | none | 3 / 1.8 | 25 / 25 | 30 / 30 | 120% / 120% | 100% / 100% |
| x | Cooked Cod `minecraft:cooked_cod` | Canned Cooked Cod | 5 / 4+1 | Cooked Cod Slice: x | x | x | x | x | none | 5 / 6 | 25 / 30 | 30 / 36 | 120% / 120% | 100% / 100% |
| x | Cooked Lionfish `upgrade_aquatic:cooked_lionfish` | Canned Cooked Lionfish | 5 / 4+1 | - | x | / | x | x | none | 6 / 8.4 | 30 / 42 | 36 / 50.4 | 120% / 120% | 100% / 100% |
| x | Cooked Perch `upgrade_aquatic:cooked_perch` | Canned Cooked Perch | 5 / 4+1 | Cooked Perch Slice: x | x | / | x | x | none | 5 / 7 | 25 / 35 | 30 / 42 | 120% / 120% | 100% / 100% |
| x | Cooked Pike `upgrade_aquatic:cooked_pike` | Canned Cooked Pike | 5 / 4+1 | Cooked Pike Slice: x | x | / | x | x | none | 8 / 12.8 | 40 / 64 | 48 / 76.8 | 120% / 120% | 100% / 100% |
| x | Cooked Salmon `minecraft:cooked_salmon` | Canned Cooked Salmon | 5 / 4+1 | Cooked Salmon Slice: x | x | x | x | x | none | 6 / 9.6 | 30 / 48 | 36 / 57.6 | 120% / 120% | 100% / 100% |
| x | Salted Cod `vintagedelight:salted_cod` (fills Canned Cooked Cod; waits on /dumpfood with Vintage loaded) | Canned Cooked Cod | - | - | - | - | - | - | - | - | - | - | - | - |
| x | Salted Salmon `vintagedelight:salted_salmon` (fills Canned Cooked Salmon; waits on /dumpfood with Vintage loaded) | Canned Cooked Salmon | - | - | - | - | - | - | - | - | - | - | - | - |
| x | Surströmming `vintagedelight:surstromming` (configured; waits on /dumpfood with Vintage loaded) | Canned Surströmming | - | - | - | - | - | x | - | 9 / 15.3 | 45 / 76.5 | 54 / 91.8 | 120% / 120% | 100% / 100% |
| / | Cooked Crab Leg `quark:cooked_crab_leg` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Lobster Tail `alexsmobs:cooked_lobster_tail` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Kippers `brewinandchewin:kippers` | Canned Kippers | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |

### 2.9 Bread <a id="bread"></a>

Bread x2 meals, 4 servings, no additive (honey bread also 5 bread + honey).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Adzuki Bun `neapolitan:adzuki_bun` | Canned Adzuki Bun | 6 | - | x | / | x | x | x | 5 / 4 | 30 / 24 | 40 / 32 | 133% / 133% | 100% / 100% |
| x | Banana Bread `neapolitan:banana_bread` | Canned Banana Bread | 6 | - | x | / | x | x | x | 5 / 6 | 30 / 36 | 40 / 48 | 133% / 133% | 100% / 100% |
| x | Bread `minecraft:bread` | Canned Bread | 6 | - | x | x | x | x | none | 5 / 6 | 30 / 36 | 40 / 48 | 133% / 133% | 100% / 100% |
| x | Cornbread `corn_delight:cornbread` | Canned Cornbread | 6 | - | x | / | x | x | none | 4 / 4 | 24 / 24 | 32 / 32 | 133% / 133% | 100% / 100% |
| x | Honey Bread `buzzier_bees:honey_bread` | Canned Honey Bread | 6 / 5+1 | - | x | / | x | x | none | 6 / 9.6 | 36 / 57.6 | 48 / 76.8 | 133% / 133% | 100% / 100% |
| x | Mulberry Bread `upgrade_aquatic:mulberry_bread` | Canned Mulberry Bread | 6 | - | x | / | x | x | none | 9 / 7.2 | 54 / 43.2 | 72 / 57.6 | 133% / 133% | 100% / 100% |
| x | Nether Wart Sourdough `farmersrespite:nether_wart_sourdough` | Canned Nether Wart Sourdough | 6 | - | x | / | x | x | x | 4 / 1.6 | 24 / 9.6 | 32 / 12.8 | 133% / 133% | 100% / 100% |
| x | Sourdough Bread `pneumaticcraft:sourdough_bread` | Canned Sourdough Bread | 6 | - | x | / | x | x | none | 7 / 14 | 42 / 84 | 56 / 112 | 133% / 133% | 100% / 100% |
| x | Cauliflower Bread `veggiesdelight:cauliflower_bread` | Canned Cauliflower Bread | 6 | - | x | / | x | x | none | 6 / 7.2 | 36 / 43.2 | 48 / 57.6 | 133% / 133% | 100% / 100% |
| x | Garlic Bread `veggiesdelight:garlic_bread` | Canned Garlic Bread | 6 | - | x | / | x | x | none | 5 / 4 | 30 / 24 | 40 / 32 | 133% / 133% | 100% / 100% |
| x | Mhadjeb `veggiesdelight:mhadjeb` | Canned Mhadjeb | 6 | - | x | / | x | x | none | 12 / 19.2 | 72 / 115.2 | 96 / 153.6 | 133% / 133% | 100% / 100% |
| / | Bread Slice `moredelight:bread_slice` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Toast `moredelight:toast` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Tortilla `corn_delight:tortilla` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Strawberry Scones `neapolitan:strawberry_scones` (plan: all bread but scones) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.10 Salad <a id="salad"></a>

Salad x1.25 meals, 8 servings, fast, effects 0.75x. Vinegar additive: 5 salads + 1 vinegar (2026-10-23).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Chicken Salad `moredelight:chicken_salad` | Canned Chicken Salad | 6 | - | x | / | x | x | x | 6 / 7.2 | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 107% / 100% |
| x | Bowl of Exotic Fruits `quarkdelight:bowl_of_exotic_fruits` | Canned Exotic Fruits | 6 | - | x | / | x | x | none | 11 / 11 | 66 / 66 | 112 / 110 | 170% / 167% | 102% / 100% |
| x | Field Salad `delightful:field_salad` | Canned Field Salad | 6 | - | x | / | x | x | x | 10 / 11 | 60 / 66 | 104 / 110 | 173% / 167% | 104% / 100% |
| x | Fruit Salad `farmersdelight:fruit_salad` | Canned Fruit Salad | 6 | - | x | / | x | x | x | 6 / 7.2 | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 107% / 100% |
| x | Lush Moss Salad `quarkdelight:lush_moss_salad` | Canned Lush Moss Salad | 6 | - | x | / | x | x | none | 8 / 8 | 48 / 48 | 80 / 80 | 167% / 167% | 100% / 100% |
| x | Mango Salad `fruitsdelight:mango_salad` | Canned Mango Salad | 6 | - | x | / | x | x | x | 8 / 9.6 | 48 / 57.6 | 80 / 96 | 167% / 167% | 100% / 100% |
| x | Mixed Salad `farmersdelight:mixed_salad` | Canned Mixed Salad | 6 | - | x | / | x | x | x | 6 / 7.2 | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 107% / 100% |
| x | Nether Salad `farmersdelight:nether_salad` | Canned Nether Salad | 6 | - | x | / | x | x | x | 5 / 4 | 30 / 24 | 48 / 40 | 160% / 167% | 96% / 100% |
| x | Broccoli Salad `veggiesdelight:broccoli_salad` | Canned Broccoli Salad | 6 / 5+1 | - | x | / | x | x | x | 6 / 7.2 | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 107% / 100% |
| x | Cesar Salad `veggiesdelight:cesar_salad` | Canned Cesar Salad | 6 / 5+1 | - | x | / | x | x | x | 10 / 14 | 60 / 84 | 104 / 140 | 173% / 167% | 104% / 100% |
| x | Coleslaw `veggiesdelight:coleslaw` | Canned Coleslaw | 6 / 5+1 | - | x | / | x | x | x | 8 / 9.6 | 48 / 57.6 | 80 / 96 | 167% / 167% | 100% / 100% |
| x | Cucumber Salad `vintagedelight:cucumber_salad` (configured; waits on /dumpfood with Vintage loaded) | Canned Cucumber Salad | - | - | - | - | - | x | - | 8 / 11.2 | 48 / 67.2 | 80 / 112 | 167% / 167% | 100% / 100% |
| x | Turnip Salad `veggiesdelight:turnip_salad` | Canned Turnip Salad | 6 / 5+1 | - | x | / | x | x | x | 6 / 7.2 | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 107% / 100% |
| / | Maggot Salad `alexsdelight:maggot_salad` (plan: all salads but maggot salad) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Potato Salad `moredelight:potato_salad` (plan: all salads but potato salad) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Potato Salad `rusticdelight:potato_salad` (plan: all salads but potato salad) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.11 Soup <a id="soup"></a>

Soup x1.25 meals, 8 servings, effects 0.75x. Additive: salt or bone broth (3 soups).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Acacia Blossom Soup `alexsdelight:acacia_blossom_soup` | Canned Acacia Blossom Soup | 6 / 3+1 | - | x | / | x | x | x | 12 / 19.2 | 72 / 115.2 | 120 / 192 | 167% / 167% | 100% / 100% |
| x | Adzuki Stew `neapolitan:adzuki_stew` | Canned Adzuki Stew | 6 / 3+1 | - | x | / | x | x | x | 8 / 9.6 | 48 / 57.6 | 80 / 96 | 167% / 167% | 100% / 100% |
| x | Baked Cod Stew `farmersdelight:baked_cod_stew` | Canned Baked Cod Stew | 6 / 3+1 | - | x | / | x | x | x | 14 / 21 | 84 / 126 | 144 / 210 | 171% / 167% | 103% / 100% |
| x | Bayberry Soup `fruitsdelight:bayberry_soup` | Canned Bayberry Soup | 6 / 3+1 | - | x | / | x | x | x | 1 / 0.4 | 6 / 2.4 | 8 / 4 | 133% / 167% | 80% / 100% |
| x | Beef Stew `farmersdelight:beef_stew` | Canned Beef Stew | 6 / 3+1 | - | x | / | x | x | x | 12 / 19.2 | 72 / 115.2 | 120 / 192 | 167% / 167% | 100% / 100% |
| x | Beetroot Soup `minecraft:beetroot_soup` | Canned Beetroot Soup | 6 / 3+1 | - | x | / | x | x | none | 6 / 7.2 | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 107% / 100% |
| x | Bell Pepper Soup `rusticdelight:bell_pepper_soup` | Canned Bell Pepper Soup | 6 / 3+1 | - | x | / | x | x | x | 6 / 7.2 | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 107% / 100% |
| x | Blazing Chili `farmersrespite:blazing_chili` | Canned Blazing Chili | 6 / 3+1 | - | x | / | x | x | x | 10 / 8 | 60 / 48 | 104 / 80.1 | 173% / 167% | 104% / 100% |
| x | Bone Broth `farmersdelight:bone_broth` | Canned Bone Broth | 6 / 3+1 | - | x | / | x | x | x | 8 / 11.2 | 48 / 67.2 | 80 / 112 | 167% / 167% | 100% / 100% |
| x | Carrot Soup `moredelight:carrot_soup` | Canned Carrot Soup | 6 / 3+1 | - | x | / | x | x | x | 12 / 19.2 | 72 / 115.2 | 120 / 192 | 167% / 167% | 100% / 100% |
| x | Chicken Soup `farmersdelight:chicken_soup` | Canned Chicken Soup | 6 / 3+1 | - | x | / | x | x | x | 14 / 21 | 84 / 126 | 144 / 210 | 171% / 167% | 103% / 100% |
| x | Chorus Stew `endersdelight:chorus_stew` | Canned Chorus Stew | 6 / 3+1 | - | x | / | x | x | x | 4 / 4 | 24 / 24 | 40 / 40 | 167% / 167% | 100% / 100% |
| x | Corn Soup `corn_delight:corn_soup` | Canned Corn Soup | 6 / 3+1 | - | x | / | x | x | x | 10 / 18 | 60 / 108 | 104 / 179.9 | 173% / 167% | 104% / 100% |
| x | Creamy Onion Soup `brewinandchewin:creamy_onion_soup` | Canned Creamy Onion Soup | 6 / 3+1 | - | x | / | x | x | x | 12 / 19.2 | 72 / 115.2 | 120 / 192 | 167% / 167% | 100% / 100% |
| x | Endermite Stew `endersdelight:endermite_stew` | Canned Endermite Stew | 6 / 3+1 | - | x | / | x | x | x | 4 / 3.2 | 24 / 19.2 | 40 / 32 | 167% / 167% | 100% / 100% |
| x | Fig Chicken Stew `fruitsdelight:fig_chicken_stew` | Canned Fig Chicken Stew | 6 / 3+1 | - | x | / | x | x | x | 12 / 19.2 | 72 / 115.2 | 120 / 192 | 167% / 167% | 100% / 100% |
| x | Fish Stew `farmersdelight:fish_stew` | Canned Fish Stew | 6 / 3+1 | - | x | / | x | x | x | 12 / 19.2 | 72 / 115.2 | 120 / 192 | 167% / 167% | 100% / 100% |
| x | Kangaroo Stew `alexsdelight:kangaroo_stew` | Canned Kangaroo Stew | 6 / 3+1 | - | x | / | x | x | x | 10 / 16 | 60 / 96 | 104 / 160 | 173% / 167% | 104% / 100% |
| x | Miso with Bamboo Sprouts `quarkdelight:miso_with_bamboo_sprouts` | Canned Miso with Bamboo Sprouts | 6 / 3+1 | - | x | / | x | x | none | 10 / 14 | 60 / 84 | 104 / 140 | 173% / 167% | 104% / 100% |
| x | Mushroom Stew `minecraft:mushroom_stew` | Canned Mushroom Stew | 6 / 3+1 | - | x | x | x | x | none | 6 / 7.2 | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 107% / 100% |
| x | Noodle Soup `farmersdelight:noodle_soup` | Canned Noodle Soup | 6 / 3+1 | - | x | / | x | x | x | 14 / 21 | 84 / 126 | 144 / 210 | 171% / 167% | 103% / 100% |
| x | Pumpkin Soup `farmersdelight:pumpkin_soup` | Canned Pumpkin Soup | 6 / 3+1 | - | x | / | x | x | x | 14 / 21 | 84 / 126 | 144 / 210 | 171% / 167% | 103% / 100% |
| x | Rabbit Stew `minecraft:rabbit_stew` | Canned Rabbit Stew | 6 / 3+1 | - | x | x | x | x | none | 10 / 12 | 60 / 72 | 104 / 120 | 173% / 167% | 104% / 100% |
| x | Sinigang `delightful:sinigang` | Canned Sinigang | 6 / 3+1 | - | x | / | x | x | x | 15 / 27 | 90 / 162 | 152 / 270 | 169% / 167% | 101% / 100% |
| x | Sopa De Macaco `alexsmobs:sopa_de_macaco` | Canned Sopa De Macaco | 6 / 3+1 | - | x | / | x | x | none | 5 / 4 | 30 / 24 | 48 / 40 | 160% / 167% | 96% / 100% |
| x | Strider Moss Stew `nethersdelight:strider_moss_stew` | Canned Strider Moss Stew | 6 / 3+1 | - | x | / | x | x | x | 8 / 9.6 | 48 / 57.6 | 80 / 96 | 167% / 167% | 100% / 100% |
| x | Vegetable Soup `farmersdelight:vegetable_soup` | Canned Vegetable Soup | 6 / 3+1 | - | x | / | x | x | x | 12 / 19.2 | 72 / 115.2 | 120 / 192 | 167% / 167% | 100% / 100% |
| x | Broccoli Soup `veggiesdelight:broccoli_soup` | Canned Broccoli Soup | 6 / 3+1 | - | x | / | x | x | x | 10 / 14 | 60 / 84 | 104 / 140 | 173% / 167% | 104% / 100% |
| x | Cacciatore `veggiesdelight:cacciatore` | Canned Cacciatore | 6 / 3+1 | - | x | / | x | x | x | 14 / 21 | 84 / 126 | 144 / 210 | 171% / 167% | 103% / 100% |
| x | Cauliflower Soup `veggiesdelight:cauliflower_soup` | Canned Cauliflower Soup | 6 / 3+1 | - | x | / | x | x | x | 10 / 14 | 60 / 84 | 104 / 140 | 173% / 167% | 104% / 100% |
| x | Garlic Chicken Stew `veggiesdelight:garlic_chicken_stew` | Canned Garlic Chicken Stew | 6 / 3+1 | - | x | / | x | x | x | 12 / 19.2 | 72 / 115.2 | 120 / 192 | 167% / 167% | 100% / 100% |
| x | Ghostly Chili `vintagedelight:ghostly_chili` (configured; waits on /dumpfood with Vintage loaded) | Canned Ghostly Chili | - | - | - | - | - | x | - | 12 / 19.2 | 72 / 115.2 | 120 / 192 | 167% / 167% | 100% / 100% |
| x | Oatmeal `vintagedelight:oatmeal` (configured; waits on /dumpfood with Vintage loaded) | Canned Oatmeal | - | - | - | - | - | x | - | 8 / 12 | 48 / 72 | 80 / 120 | 167% / 167% | 100% / 100% |
| x | Pickle Soup `vintagedelight:pickle_soup` (configured; waits on /dumpfood with Vintage loaded) | Canned Pickle Soup | - | - | - | - | - | x | - | 13 / 22.1 | 78 / 132.6 | 128 / 220.9 | 164% / 167% | 98% / 100% |
| x | Turnip Beef Stew `veggiesdelight:turnip_beef_stew` | Canned Turnip Beef Stew | 6 / 3+1 | - | x | / | x | x | x | 14 / 21 | 84 / 126 | 144 / 210 | 171% / 167% | 103% / 100% |
| / | Cavern Explorer Stew `quarkdelight:cavern_explorer_stew` (plan: all soups but cave explorer) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Chorus Stew `endersdelight:chorus_stew_wood` (wooden soup, removed) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Endermite Stew `endersdelight:endermite_stew_wood` (wooden soup, removed) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Suspicious Stew `minecraft:suspicious_stew` (effects live in NBT, a can would lose them) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.12 Meals <a id="meal"></a>

Placeholder goal: 2 pastas -> 4 meals of 0.75 pasta, effects 0.75x. Only the pastas are built (veg noodles -> Vegetable
Pasta, duck noodles -> Pasta with Duck).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Bell Pepper Pasta `rusticdelight:bell_pepper_pasta` | Canned Bell Pepper Pasta | 2 | - | x | / | x | x | x | 12 / 19.2 | 24 / 38.4 | 36 / 57.6 | 150% / 150% | 100% / 100% |
| x | Cheesy Pasta `brewinandchewin:cheesy_pasta` | Canned Cheesy Pasta | 2 | - | x | / | x | x | x | 14 / 21 | 28 / 42 | 44 / 63 | 157% / 150% | 105% / 100% |
| x | Crab Pasta `quarkdelight:crab_pasta` | Canned Crab Pasta | 2 | - | x | / | x | x | none | 11 / 13.2 | 22 / 26.4 | 32 / 39.6 | 145% / 150% | 97% / 100% |
| x | Kangaroo Pasta `alexsdelight:kangaroo_pasta` | Canned Kangaroo Pasta | 2 | - | x | / | x | x | x | 10 / 16 | 20 / 32 | 32 / 48 | 160% / 150% | 107% / 100% |
| x | Lobster Pasta `alexsdelight:lobster_pasta` | Canned Lobster Pasta | 2 | - | x | / | x | x | x | 10 / 16 | 20 / 32 | 32 / 48 | 160% / 150% | 107% / 100% |
| x | Duck Noodles `abnormals_delight:duck_noodles` | Canned Pasta with Duck | 2 | - | x | / | x | x | x | 12 / 19.2 | 24 / 38.4 | 36 / 57.6 | 150% / 150% | 100% / 100% |
| x | Pasta with Meatballs `farmersdelight:pasta_with_meatballs` | Canned Pasta with Meatballs | 2 | - | x | / | x | x | x | 12 / 19.2 | 24 / 38.4 | 36 / 57.6 | 150% / 150% | 100% / 100% |
| x | Pasta with Mutton Chop `farmersdelight:pasta_with_mutton_chop` | Canned Pasta with Mutton Chop | 2 | - | x | / | x | x | x | 12 / 19.2 | 24 / 38.4 | 36 / 57.6 | 150% / 150% | 100% / 100% |
| x | Pearl Pasta `endersdelight:pearl_pasta` | Canned Pearl Pasta | 2 | - | x | / | x | x | x | 4 / 3.2 | 8 / 6.4 | 12 / 9.6 | 150% / 150% | 100% / 100% |
| x | Squid Ink Pasta `farmersdelight:squid_ink_pasta` | Canned Squid Ink Pasta | 2 | - | x | / | x | x | x | 14 / 21 | 28 / 42 | 44 / 63 | 157% / 150% | 105% / 100% |
| x | Vegetable Noodles `farmersdelight:vegetable_noodles` | Canned Vegetable Pasta | 2 | - | x | / | x | x | x | 14 / 21 | 28 / 42 | 44 / 63 | 157% / 150% | 105% / 100% |
| x | Cornbread Stuffing `corn_delight:cornbread_stuffing` | Canned Cornbread Stuffing | 2 | - | x | / | x | x | x | 12 / 24 | 24 / 48 | 36 / 72 | 150% / 150% | 100% / 100% |
| x | Creamed Corn `corn_delight:creamed_corn` | Canned Creamed Corn | 2 | - | x | / | x | x | x | 7 / 7 | 14 / 14 | 20 / 21 | 143% / 150% | 95% / 100% |
| x | Creamy Pasta with Chicken Cuts `moredelight:creamy_pasta_with_chicken_cuts` | Canned Creamy Pasta with Chicken | 2 | - | x | / | x | x | x | 12 / 19.2 | 24 / 38.4 | 36 / 57.6 | 150% / 150% | 100% / 100% |
| x | Creamy Pasta with Ham `moredelight:creamy_pasta_with_ham` | Canned Creamy Pasta with Ham | 2 | - | x | / | x | x | x | 12 / 19.2 | 24 / 38.4 | 36 / 57.6 | 150% / 150% | 100% / 100% |
| x | Cheese Pasta `vintagedelight:cheese_pasta` (configured; waits on /dumpfood with Vintage loaded) | Canned Cheese Pasta | - | - | - | - | - | x | - | 9 / 13.5 | 18 / 27 | 28 / 40.5 | 156% / 150% | 104% / 100% |
| x | Pad Thai `vintagedelight:pad_thai` (configured; waits on /dumpfood with Vintage loaded) | Canned Pad Thai | - | - | - | - | - | x | - | 12 / 18 | 24 / 36 | 36 / 54 | 150% / 150% | 100% / 100% |
| x | Pasta With Broccoli `veggiesdelight:pasta_with_broccoli` | Canned Pasta with Broccoli | 2 | - | x | / | x | x | x | 12 / 19.2 | 24 / 38.4 | 36 / 57.6 | 150% / 150% | 100% / 100% |
| x | Potato Noodles `veggiesdelight:potato_noodles` | Canned Potato Noodles | 2 | - | x | / | x | x | x | 12 / 19.2 | 24 / 38.4 | 36 / 57.6 | 150% / 150% | 100% / 100% |
| / | Adzuki Curry `neapolitan:adzuki_curry` | Canned Adzuki Curry | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Bacon and Eggs `farmersdelight:bacon_and_eggs` | Canned Bacon and Eggs | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Black Cod `farmersrespite:black_cod` | Canned Black Cod | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Coconut Curry `delightful:coconut_curry` | Canned Coconut Curry | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Coffee-Braised Beef `rusticdelight:coffee_braised_beef` | Canned Coffee-Braised Beef | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cooked Rice with Beef `moredelight:cooked_rice_with_beef` | Canned Cooked Rice with Beef | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cooked Rice with Chicken Cuts `moredelight:cooked_rice_with_chicken_cuts` | Canned Cooked Rice with Chicken Cuts | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cooked Rice with Porkchop `moredelight:cooked_rice_with_porkchop` | Canned Cooked Rice with Porkchop | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cooked Whole Crab `quarkdelight:cooked_whole_crab` | Canned Cooked Whole Crab | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Diced Potatoes with Beef `moredelight:diced_potatoes_with_beef` | Canned Diced Potatoes with Beef | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Diced Potatoes with Chicken Cuts `moredelight:diced_potatoes_with_chicken_cuts` | Canned Diced Potatoes with Chicken Cuts | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Diced Potatoes with Egg and Tomato `moredelight:diced_potatoes_with_egg_and_tomato` | Canned Diced Potatoes with Egg and Tomato | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Diced Potatoes with Porkchop `moredelight:diced_potatoes_with_porkchop` | Canned Diced Potatoes with Porkchop | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Dune Platter `abnormals_delight:dune_platter` | Canned Dune Platter | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Ender Paella `endersdelight:ender_paella` | Canned Ender Paella | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Escargot `abnormals_delight:escargot` | Canned Escargot | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Fiery Fondue `brewinandchewin:fiery_fondue` | Canned Fiery Fondue | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Fried Calamari `rusticdelight:fried_calamari` | Canned Fried Calamari | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Fried Chicken `rusticdelight:fried_chicken` | Canned Fried Chicken | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Fried Mushrooms `rusticdelight:fried_mushrooms` | Canned Fried Mushrooms | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Fried Rice `farmersdelight:fried_rice` | Canned Fried Rice | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Grilled Salmon `farmersdelight:grilled_salmon` | Canned Grilled Salmon | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Grilled Strider `nethersdelight:grilled_strider` | Canned Grilled Strider | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Horror Lasagna `brewinandchewin:horror_lasagna` | Canned Horror Lasagna | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Lychee Chicken `fruitsdelight:lychee_chicken` | Canned Lychee Chicken | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mashed Potatoes `moredelight:mashed_potatoes` | Canned Mashed Potatoes | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Mushroom Rice `farmersdelight:mushroom_rice` | Canned Mushroom Rice | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Omelet `moredelight:omelette` | Canned Omelet | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Orange Chicken `fruitsdelight:orange_chicken` | Canned Orange Chicken | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Orange Marinated Pork `fruitsdelight:orange_marinated_pork` | Canned Orange Marinated Pork | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Passion Fruit Glazed Duck `abnormals_delight:passion_fruit_glazed_duck` | Canned Passion Fruit Glazed Duck | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Perch with Mushrooms `abnormals_delight:perch_with_mushrooms` | Canned Perch with Mushrooms | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Pike with Beetroot `abnormals_delight:pike_with_beetroot` | Canned Pike with Beetroot | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Pineapple Marinated Pork `fruitsdelight:pineapple_marinated_pork` | Canned Pineapple Marinated Pork | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Ratatouille `farmersdelight:ratatouille` | Canned Ratatouille | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Roasted Mutton Chops `farmersdelight:roasted_mutton_chops` | Canned Roasted Mutton Chops | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Scarlet Pierogi `brewinandchewin:scarlet_pierogi` (was `scarlet_pierogies` before the Brewin 4.5.0 port) | Canned Scarlet Pierogi | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Seared Venison `abnormals_delight:seared_venison` | Canned Seared Venison | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Shrimp-Fried Rice `alexsmobs:shrimp_fried_rice` | Canned Shrimp-Fried Rice | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Slabdish `abnormals_delight:slabdish` | Canned Slabdish | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Steak and Potatoes `farmersdelight:steak_and_potatoes` | Canned Steak and Potatoes | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Tea Curry `farmersrespite:tea_curry` | Canned Tea Curry | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Twisted Cereal `endersdelight:twisted_cereal` | Canned Twisted Cereal | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Vegetable Omelet `brewinandchewin:vegetable_omelet` | Canned Vegetable Omelet | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Venison with Bamboo Shoots `abnormals_delight:venison_with_bamboo_shoots` | Canned Venison with Bamboo Shoots | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Warped Moldy Meat `nethersdelight:warped_moldy_meat` | Canned Warped Moldy Meat | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Ender Paella `endersdelight:ender_paella_wood` (wooden variant, removed) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Pearl Pasta `endersdelight:pearl_pasta_wood` (plan: pearl pasta, not the wooden one) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Twisted Cereal `endersdelight:twisted_cereal_wood` (wooden variant, removed) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cauliflower Kuku `veggiesdelight:cauliflower_kuku` | Canned Cauliflower Kuku | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Dandelion Greens And Eggs `veggiesdelight:dandelion_and_eggs` | Canned Dandelion Greens And Eggs | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Fish And Chips `veggiesdelight:fish_and_chips` | Canned Fish And Chips | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Garlic Baked Cod `veggiesdelight:garlic_baked_cod` | Canned Garlic Baked Cod | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Garlic Rice With Cauliflower `veggiesdelight:garlic_rice_with_cauliflower` | Canned Garlic Rice With Cauliflower | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Lasagna Slice `veggiesdelight:lasagna_slice` | Canned Lasagna Slice | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mashed Potatoes `veggiesdelight:mashed_potatoes` | Canned Mashed Potatoes | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Overnight Oats `vintagedelight:overnight_oats` | Canned Overnight Oats | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Rice And Vegetables `veggiesdelight:rice_and_vegetables` | Canned Rice And Vegetables | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Roasted Vegetables `veggiesdelight:roasted_vegetables` | Canned Roasted Vegetables | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Shakshouka `veggiesdelight:shakshouka` | Canned Shakshouka | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Steak And Broccoli `veggiesdelight:steak_and_broccoli` | Canned Steak And Broccoli | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Stuffed Zucchinis `veggiesdelight:stuffed_zucchinis` | Canned Stuffed Zucchinis | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Turnip Cake `veggiesdelight:turnip_cake` | Canned Turnip Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

### 2.13 Pie <a id="pie"></a>

4 slices + can -> pie filling (4 servings, slice-sized meals); filling can + pie crust -> the whole pie. Fruitsdelight's
durian and pineapple pies are single items: 4 fill a can, no pie back (both marked for a rework, not really pies).
Sweet Berry Pie is Farmer's Delight's cheesecake, renamed. Source mods of the slices with no whole pie: TODO
`baking/baked-goods #14`.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Slice of Apple Pie `farmersdelight:apple_pie_slice` | Canned Apple Pie Filling | 4 | - | x | / | x | x | x | 3 / 1.8 | 12 / 7.2 | 12 / 7.2 | 100% / 100% | 100% / 100% |
| x | Slice of Blueberry Pie `delightful:blueberry_pie_slice` (no source in the pack: the whole pie is BYG `byg:blueberry_pie`, not installed) | Canned Blueberry Pie Filling | - | - | / | / | / | / | / | later | later | later | - | - |
| x | Slice of Cherry Pie `create_central_kitchen:cherry_pie_slice` (no source in the pack: the whole pie is Environmental `environmental:cherry_pie`, not installed) | Canned Cherry Pie Filling | - | - | / | / | / | / | / | later | later | later | - | - |
| x | Slice of Chocolate Pie `farmersdelight:chocolate_pie_slice` | Canned Chocolate Pie Filling | 4 | - | x | / | x | x | x | 3 / 1.8 | 12 / 7.2 | 12 / 7.2 | 100% / 100% | 100% / 100% |
| x | Chorus Pie Slice `endersdelight:chorus_pie_slice` | Canned Chorus Pie Filling | 4 | - | x | / | x | x | x | 3 / 1.8 | 12 / 7.2 | 12 / 7.2 | 100% / 100% | 100% / 100% |
| x | Slice of Crimson Berry Pie `delightful:crimson_berry_pie_slice` (no source in the pack: the whole pie is BYG `byg:crimson_berry_pie`, not installed) | Canned Crimson Berry Pie Filling | - | - | / | / | / | / | / | later | later | later | - | - |
| x | Durian Pie `fruitsdelight:durian_pie` (not really a pie, marked for rework: TODO `baking/baked-goods #12`) | Canned Durian Pie Filling | 4 | - | x | / | x | x | x | 5 / 4 | 20 / 16 | 20 / 16 | 100% / 100% | 100% / 100% |
| x | Slice of Green Apple Pie `delightful:green_apple_pie_slice` (no source in the pack: the whole pie is BYG `byg:green_apple_pie`, not installed) | Canned Green Apple Pie Filling | - | - | / | / | / | / | / | later | later | later | - | - |
| x | Slice of Mulberry Pie `create_central_kitchen:mulberry_pie_slice` | Canned Mulberry Pie Filling | 4 | - | x | / | x | x | x | 3 / 1.8 | 12 / 7.2 | 12 / 7.2 | 100% / 100% | 100% / 100% |
| x | Slice of Nightshade Berry Pie `delightful:nightshade_berry_pie_slice` (no source in the pack: the whole pie is BYG `byg:nightshade_berry_pie`, not installed) | Canned Nightshade Berry Pie Filling | - | - | / | / | / | / | / | later | later | later | - | - |
| x | Pineapple Pie `fruitsdelight:pineapple_pie` (not really a pie, marked for rework: TODO `baking/baked-goods #12`) | Canned Pineapple Pie Filling | 4 | - | x | / | x | x | x | 5 / 4 | 20 / 16 | 20 / 16 | 100% / 100% | 100% / 100% |
| x | Slice of Pumpkin Pie `delightful:pumpkin_pie_slice` | Canned Pumpkin Pie Filling | 4 | - | x | / | x | x | x | 3 / 1.8 | 12 / 7.2 | 12 / 7.2 | 100% / 100% | 100% / 100% |
| x | Slice of Pumpkin Pie `create_central_kitchen:pumpkin_pie_slice` | Canned Pumpkin Pie Filling | 4 | - | x | / | x | x | x | 3 / 1.8 | 12 / 7.2 | 12 / 7.2 | 100% / 100% | 100% / 100% |
| x | Rose Hip Pie Slice `farmersrespite:rose_hip_pie_slice` | Canned Rose Hip Pie Filling | 4 | - | x | / | x | x | x | 3 / 1.8 | 12 / 7.2 | 12 / 7.2 | 100% / 100% | 100% / 100% |
| x | Slice of Salmonberry Pie `delightful:salmonberry_pie_slice` | Canned Salmonberry Pie Filling | 4 | - | x | / | x | x | x | 3 / 1.8 | 12 / 7.2 | 12 / 7.2 | 100% / 100% | 100% / 100% |
| x | Slice of Sweet Berry Pie `farmersdelight:sweet_berry_cheesecake_slice` (the cheesecake, renamed; moved from 2.23 cakes 2026-10-22) | Canned Sweet Berry Pie Filling | 4 | - | x | / | x | x | x | 3 / 1.8 | 12 / 7.2 | 12 / 7.2 | 100% / 100% | 100% / 100% |
| x | Slice of Truffle Pie `create_central_kitchen:truffle_pie_slice` (no source in the pack: the whole pie is Environmental `environmental:truffle_pie`, not installed; the Fungi Delight truffle pie bake of 2026-10-23 never loads, TODO `ports/fungiport #3`) | Canned Truffle Pie Filling | - | - | / | / | / | / | / | later | later | later | - | - |
| x | Fig Tart `fruitsdelight:fig_tart` (to be renamed and retextured as a pie) | Canned Fig Pie Filling | 4 | - | x | / | x | x | x | 5 / 4 | 20 / 16 | 20 / 16 | 100% / 100% | 100% / 100% |
| x | Lemon Tart `fruitsdelight:lemon_tart` (to be renamed and retextured as a pie) | Canned Lemon Pie Filling | 4 | - | x | / | x | x | x | 5 / 4 | 20 / 16 | 20 / 16 | 100% / 100% | 100% / 100% |
| x | Sweet Potato Pie Slice `veggiesdelight:sweet_potato_pie_slice` (the can takes slices and pie crust back into the pie) | Canned Sweet Potato Pie Filling | 4 | - | x | / | x | x | none | 5 / 3 | 20 / 12 | 20 / 12 | 100% / 100% | 100% / 100% |
| / | Slice of Gloomgourd Pie `delightful:gloomgourd_pie_slice` (whole pie is The Undergarden `undergarden:gloomgourd_pie`, not installed) | Canned Gloomgourd Pie Filling | n/a | - | / | / | / | / | / | later | later | later | n/a | n/a |
| / | Slice of Source Berry Pie `delightful:source_berry_pie_slice` (whole pie is Ars Nouveau `ars_nouveau:source_berry_pie`, not installed) | Canned Source Berry Pie Filling | n/a | - | / | / | / | / | / | later | later | later | n/a | n/a |

- move all tarts to pies, renaame and retexture, recipe parity w/ other pies
- ensure all pies are valid (truffle, nightshade, crimson berry, source berry), identify mod that supplies missing
  - Checked 2026-10-13 against the item and recipe exports: Delightful registers blueberry, crimson berry, green apple,
    nightshade, gloomgourd and source berry slices but none of their whole pies, and Central Kitchen's cherry and
    truffle slices only have "incomplete" pies. Nothing in the pack makes any of these 8 slices. Delightful's whole
    pies come with its compat mods, Central Kitchen's with the mods that add cherry and truffle pies (not installed). The
    obtainable pies are apple, chocolate (FD), chorus (Ender's Delight), mulberry (Upgrade Aquatic -> Central Kitchen
    slice), pumpkin (vanilla -> Delightful slice), rose hip (Farmer's Respite), salmonberry (Delightful), durian and
    pineapple (Fruits Delight).
  - Rechecked 2026-10-22 in the jars (recipe conditions and class strings): Central Kitchen cherry and truffle from
    Environmental; Delightful blueberry, crimson berry, green apple, nightshade berry from BYG, gloomgourd from The
    Undergarden, source berry from Ars Nouveau. Sweet berry (Farmer's Delight's cheesecake, renamed Sweet Berry Pie)
    added. Durian and pineapple marked for a rework (not really pies). Open: TODO `baking/baked-goods #12`, `#14`.

### 2.14 Feasts <a id="feast"></a>

Placeholder goal: one placed feast block + can -> 6 servings worth the feast's serving (a placed feast gives 4). The
three stuffed hoglin plates share one Hoglin Feast can (block nethersdelight:stuffed_hoglin, valued as the ham plate).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Caramel Popcorn `corn_delight:caramel_popcorn` | Canned Caramel Popcorn | 1 (placed feast block) | - | x | / | x | x | none | 5 / 6 | 20 / 24 | 30 / 36 | 150% / 150% | 100% / 100% |
| x | Plate of Stuffed Hoglin `nethersdelight:plate_of_stuffed_hoglin_*` | Canned Hoglin Feast | 1 (placed feast block) | - | x | / | x | x | x | 10 / 15 | 110 / 165 | 60 / 90 | 55% / 55% | 100% / 100% |
| / | Plate of Stuffed Hoglin Ham `nethersdelight:plate_of_stuffed_hoglin_ham` (shares Canned Hoglin Feast) | Canned Hoglin Feast | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Plate of Stuffed Hoglin Roast `nethersdelight:plate_of_stuffed_hoglin_roast` (shares Canned Hoglin Feast) | Canned Hoglin Feast | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Plate of Stuffed Hoglin Snout `nethersdelight:plate_of_stuffed_hoglin_snout` (shares Canned Hoglin Feast) | Canned Hoglin Feast | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Plate of Honey Glazed Ham `farmersdelight:honey_glazed_ham` | Canned Honey Glazed Ham | 1 (placed feast block) | - | x | / | x | x | x | 14 / 21 | 56 / 84 | 84 / 126 | 150% / 150% | 100% / 100% |
| x | Nachos `corn_delight:nachos_bowl` | Canned Nachos | 1 (placed feast block) | - | x | / | x | x | x | 12 / 24 | 48 / 96 | 72 / 144 | 150% / 150% | 100% / 100% |
| x | Bowl of Pineapple Fried Rice `fruitsdelight:bowl_of_pineapple_fried_rice` | Canned Pineapple Fried Rice | 1 (placed feast block) | - | x | / | x | x | x | 14 / 22.4 | 56 / 89.6 | 84 / 134.4 | 150% / 150% | 100% / 100% |
| x | Plate of Roast Chicken `farmersdelight:roast_chicken` | Canned Roast Chicken | 1 (placed feast block) | - | x | / | x | x | x | 14 / 21 | 56 / 84 | 84 / 126 | 150% / 150% | 100% / 100% |
| x | Plate of Shepherd's Pie `farmersdelight:shepherds_pie` | Canned Shepherd's Pie | 1 (placed feast block) | - | x | / | x | x | x | 14 / 21 | 56 / 84 | 84 / 126 | 150% / 150% | 100% / 100% |
| x | Bowl of Stuffed Pumpkin `farmersdelight:stuffed_pumpkin` | Canned Stuffed Pumpkin | 1 (placed feast block) | - | x | / | x | x | x | 14 / 21 | 56 / 84 | 84 / 126 | 150% / 150% | 100% / 100% |
| x | Bowl Of Stuffed Shulker `endersdelight:stuffed_shulker_bowl` | Canned Stuffed Shulker | 1 (placed feast block) | - | x | - | x | x | x | 7 / 12.6 | 28 / 50.4 | 42 / 75.6 | 150% / 150% | 100% / 100% |

### 2.15 Drinks <a id="drink"></a>

Placeholder goal: 4 -> 4 servings. Honey reuses Canned Goods' can (its code clears poison); Canned Powdered Milk takes 4
milk buckets, 1 hunger, always edible, clears every effect.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Honey Bottle `minecraft:honey_bottle` | Canned Honey | 4 | - | x | x | x | x | none | 6 / 1.2 | 24 / 4.8 | 24 / 4.8 | 100% / 100% | 100% / 100% |
| x | Milk Bucket `minecraft:milk_bucket` | Canned Powdered Milk | 4 | - | x | / | x | x | none | n/a | n/a | 4 / 0.8 | n/a | n/a |
| / | Adzuki Milkshake `neapolitan:adzuki_milkshake` | Canned Adzuki Milkshake | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Apple Cider `farmersdelight:apple_cider` | Canned Apple Cider | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Apple Cider `farmersrespite:long_apple_cider` | Canned Apple Cider | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Apple Cider `farmersrespite:strong_apple_cider` | Canned Apple Cider | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Azalea Tea `delightful:azalea_tea` | Canned Azalea Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Banana Milkshake `neapolitan:banana_milkshake` | Canned Banana Milkshake | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Bellini Cocktail `fruitsdelight:bellini_cocktail` | Canned Bellini Cocktail | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Berry Matcha Latte `delightful:berry_matcha_latte` | Canned Berry Matcha Latte | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Black Tea `farmersrespite:black_tea` | Canned Black Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Black Tea `farmersrespite:long_black_tea` | Canned Black Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Black Tea `farmersrespite:strong_black_tea` | Canned Black Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Chocolate Coffee `rusticdelight:chocolate_coffee` | Canned Chocolate Coffee | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Chocolate Milkshake `neapolitan:chocolate_milkshake` | Canned Chocolate Milkshake | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Chorus Juice `endersdelight:chorus_juice` | Canned Chorus Juice | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cloud Latte `quarkdelight:cloud_latte` | Canned Cloud Latte | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Coffee `rusticdelight:coffee` | Canned Coffee | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Coffee `farmersrespite:coffee` | Canned Coffee | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Coffee `farmersrespite:long_coffee` | Canned Coffee | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Coffee `farmersrespite:strong_coffee` | Canned Coffee | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Creamy Corn Drink `corn_delight:creamy_corn_drink` | Canned Creamy Corn Drink | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Dandelion Tea `farmersrespite:dandelion_tea` | Canned Dandelion Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Dandelion Tea `farmersrespite:long_dandelion_tea` | Canned Dandelion Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Dark Coffee `rusticdelight:dark_coffee` | Canned Dark Coffee | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Ender Nectar `delightful:ender_nectar` | Canned Ender Nectar | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Green Tea `farmersrespite:green_tea` | Canned Green Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Green Tea `farmersrespite:long_green_tea` | Canned Green Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Green Tea `farmersrespite:strong_green_tea` | Canned Green Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Hamimelon Juice `fruitsdelight:hamimelon_juice` | Canned Hamimelon Juice | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Hawberry Tea `fruitsdelight:hawberry_tea` | Canned Hawberry Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Honey Coffee `rusticdelight:honey_coffee` | Canned Honey Coffee | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Hot Chocolate `create_confectionery:hot_chocolate_bottle` | Canned Hot Chocolate | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Kiwi Juice `fruitsdelight:kiwi_juice` | Canned Kiwi Juice | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Lavender Tea `delightful:lavender_tea` | Canned Lavender Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Lemon Juice `fruitsdelight:lemon_juice` | Canned Lemon Juice | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Lychee Cherry Tea `fruitsdelight:lychee_cherry_tea` | Canned Lychee Cherry Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mango Milkshake `fruitsdelight:mango_milkshake` | Canned Mango Milkshake | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mango Tea `fruitsdelight:mango_tea` | Canned Mango Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mangosteen Tea `fruitsdelight:mangosteen_tea` | Canned Mangosteen Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Matcha Latte `delightful:matcha_latte` | Canned Matcha Latte | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Milk Coffee `rusticdelight:milk_coffee` | Canned Milk Coffee | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mint Milkshake `neapolitan:mint_milkshake` | Canned Mint Milkshake | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Oculemonade `canned_goods:oculemonade` | Canned Oculemonade | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Orange Juice `fruitsdelight:orange_juice` | Canned Orange Juice | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Peach Tea `fruitsdelight:peach_tea` | Canned Peach Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Pear Juice `fruitsdelight:pear_juice` | Canned Pear Juice | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Prickly Pear Juice `delightful:prickly_pear_juice` | Canned Prickly Pear Juice | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Purulent Tea `farmersrespite:long_purulent_tea` | Canned Purulent Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Purulent Tea `farmersrespite:purulent_tea` | Canned Purulent Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Purulent Tea `farmersrespite:strong_purulent_tea` | Canned Purulent Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Reef-Cola `canned_goods:reef_cola` | Canned Reef-Cola | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Rose Hip Tea `farmersrespite:long_rose_hip_tea` | Canned Rose Hip Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Rose Hip Tea `farmersrespite:rose_hip_tea` | Canned Rose Hip Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Rose Hip Tea `farmersrespite:strong_rose_hip_tea` | Canned Rose Hip Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Soda `canned_goods:soda` | Canned Soda | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Soothing Hot Chocolate `create_confectionery:soothing_hot_chocolate` | Canned Soothing Hot Chocolate | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Strawberry Banana Smoothie `neapolitan:strawberry_banana_smoothie` | Canned Strawberry Banana Smoothie | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Strawberry Milkshake `neapolitan:strawberry_milkshake` | Canned Strawberry Milkshake | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Vanilla Milkshake `neapolitan:vanilla_milkshake` | Canned Vanilla Milkshake | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Yellow Tea `farmersrespite:long_yellow_tea` | Canned Yellow Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Yellow Tea `farmersrespite:strong_yellow_tea` | Canned Yellow Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Yellow Tea `farmersrespite:yellow_tea` | Canned Yellow Tea | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Carrot Juice `veggiesdelight:carrot_juice` | Canned Carrot Juice | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Dandelion Juice `veggiesdelight:dandelion_juice` | Canned Dandelion Juice | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Fermented Garlic Honey `veggiesdelight:fermented_garlic_honey` | Canned Fermented Garlic Honey | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Honey Jar `vintagedelight:honey_jar` | Canned Honey Jar | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Nut Milk Bottle `vintagedelight:nut_milk_bottle` | Canned Nut Milk Bottle | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Turnip Water `veggiesdelight:turnip_water` | Canned Turnip Water | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

### 2.16 Drinks: alcohol <a id="alcohol"></a>

Not in the canning.js plan; listed so the family is complete.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| / | A Bottle of 'Creepers Crush' `vinery:creepers_crush` | Canned A Bottle of 'Creepers Crush' | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | A Bottle of 'Mojang Noir' `vinery:bottle_mojang_noir` | Canned A Bottle of 'Mojang Noir' | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | A Bottle of 'Villagers Fright' `vinery:villagers_fright` | Canned A Bottle of 'Villagers Fright' | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Aegis Wine `vinery:aegis_wine` | Canned Aegis Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Apple Wine `vinery:apple_wine` | Canned Apple Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Bloody Mary `brewinandchewin:bloody_mary` | Canned Bloody Mary | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Bolvar Wine `vinery:bolvar_wine` | Canned Bolvar Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Bottle of Wodka `tmted:bottle_of_wodka` | Canned Wodka | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chenet Wine `vinery:chenet_wine` | Canned Chenet Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cherry Wine `vinery:cherry_wine` | Canned Cherry Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Chorus Wine `vinery:chorus_wine` | Canned Chorus Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cider `vinery:apple_cider` | Canned Cider | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Clark Wine `vinery:clark_wine` | Canned Clark Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cristel Wine `vinery:cristel_wine` | Canned Cristel Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Egg Grog `brewinandchewin:egg_grog` | Canned Egg Grog | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Eiswein `vinery:eiswein` | Canned Eiswein | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Glittering Grenadine `brewinandchewin:glittering_grenadine` | Canned Glittering Grenadine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Jellie Wine `vinery:jellie_wine` | Canned Jellie Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Jo's Special Mixture `vinery:jo_special_mixture` | Canned Jo's Special Mixture | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Kelp Cider `vinery:kelp_cider` | Canned Kelp Cider | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Kombucha `brewinandchewin:kombucha` | Canned Kombucha | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Magnetic Wine `vinery:magnetic_wine` | Canned Magnetic Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mead `brewinandchewin:mead` | Canned Mead | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mead `vinery:mead` (gone 2026-10-20: the Vinery port -04 dropped its duplicate; Brewin's Mead stays) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Mellohi Wine `vinery:mellohi_wine` | Canned Mellohi Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Miss Lilitus Wine `vinery:lilitu_wine` (was `king_danis_wine` before the Vinery 1.5.5 port -04) | Canned Miss Lilitus Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Noir Wine `vinery:noir_wine` | Canned Noir Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Pale Jane `brewinandchewin:pale_jane` | Canned Pale Jane | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Praetorian Wine `vinery:praetorian_wine` | Canned Praetorian Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Red Rum `brewinandchewin:red_rum` | Canned Red Rum | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Red Wine `vinery:red_wine` | Canned Red Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Rice Wine `brewinandchewin:rice_wine` | Canned Rice Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Saccharine Rum `brewinandchewin:saccharine_rum` | Canned Saccharine Rum | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Salty Folly `brewinandchewin:salty_folly` | Canned Salty Folly | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Solaris Wine `vinery:solaris_wine` | Canned Solaris Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Stal Wine `vinery:stal_wine` | Canned Stal Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Steel Toe Stout `brewinandchewin:steel_toe_stout` | Canned Steel Toe Stout | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Strad Wine `vinery:strad_wine` | Canned Strad Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Strongroot Ale `brewinandchewin:strongroot_ale` | Canned Strongroot Ale | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Sunkissed Wine `vinery:glowing_wine` | Canned Sunkissed Wine | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Withering Dross `brewinandchewin:withering_dross` | Canned Withering Dross | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Beer `brewinandchewin:beer` | Canned Beer | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Dread Nog `brewinandchewin:dread_nog` | Canned Dread Nog | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Vodka `brewinandchewin:vodka` | Canned Vodka | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

- remove wodka

### 2.17 Golden and enchanted golden <a id="golden"></a>

Golden x0.25 meals, 10 servings, effects 0.25x, 2 golden + honey bottle.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Enchanted Golden Apple `minecraft:enchanted_golden_apple` | Canned Enchanted Golden Apple | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Baked Potato `goldenfood:enchanted_golden_baked_potato` | Canned Enchanted Golden Baked Potato | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Beetroot `goldenfood:enchanted_golden_beetroot` | Canned Enchanted Golden Beetroot | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Beetroot Soup `goldenfood:enchanted_golden_beetroot_soup` | Canned Enchanted Golden Beetroot Soup | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Bread `goldenfood:enchanted_golden_bread` | Canned Enchanted Golden Bread | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Carrot `goldenfood:enchanted_golden_carrot` | Canned Enchanted Golden Carrot | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Chorus Fruit `goldenfood:enchanted_golden_chorus_fruit` | Canned Enchanted Golden Chorus Fruit | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Cooked Beef `goldenfood:enchanted_golden_cooked_beef` | Canned Enchanted Golden Cooked Beef | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Cooked Chicken `goldenfood:enchanted_golden_cooked_chicken` | Canned Enchanted Golden Cooked Chicken | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Cooked Cod `goldenfood:enchanted_golden_cooked_cod` | Canned Enchanted Golden Cooked Cod | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Cooked Mutton `goldenfood:enchanted_golden_cooked_mutton` | Canned Enchanted Golden Cooked Mutton | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Cooked Porkchop `goldenfood:enchanted_golden_cooked_porkchop` | Canned Enchanted Golden Cooked Porkchop | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Cooked Rabbit `goldenfood:enchanted_golden_cooked_rabbit` | Canned Enchanted Golden Cooked Rabbit | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Cooked Salmon `goldenfood:enchanted_golden_cooked_salmon` | Canned Enchanted Golden Cooked Salmon | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Cookie `goldenfood:enchanted_golden_cookie` | Canned Enchanted Golden Cookie | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Dried Kelp `goldenfood:enchanted_golden_dried_kelp` | Canned Enchanted Golden Dried Kelp | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Glow Berries `goldenfood:enchanted_golden_glow_berries` | Canned Enchanted Golden Glow Berries | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Honey Bottle `goldenfood:enchanted_golden_honey_bottle` | Canned Enchanted Golden Honey Bottle | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Melon Slice `goldenfood:enchanted_golden_melon_slice` | Canned Enchanted Golden Melon Slice | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Milk Bucket `goldenfood:enchanted_golden_milk_bucket` | Canned Enchanted Golden Milk Bucket | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Moss Clump `immersive_weathering:enchanted_golden_moss_clump` | Canned Enchanted Golden Moss Clump | None / 2+1 | - | x | / | x | x | x | 6 / 9.6 | 18 / 20.4 | 20 / 24 | 111% / 118% | 133% / 100% |
| x | Enchanted Golden Mushroom Stew `goldenfood:enchanted_golden_mushroom_stew` | Canned Enchanted Golden Mushroom Stew | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Pumpkin Pie `goldenfood:enchanted_golden_pumpkin_pie` | Canned Enchanted Golden Pumpkin Pie | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Rabbit Stew `goldenfood:enchanted_golden_rabbit_stew` | Canned Enchanted Golden Rabbit Stew | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Enchanted Golden Sweet Berries `goldenfood:enchanted_golden_sweet_berries` | Canned Enchanted Golden Sweet Berries | None / 2+1 | - | x | / | x | x | x | 8 / 128 | 22 / 257.2 | 20 / 320 | 91% / 124% | 100% / 100% |
| x | Golden Apple `minecraft:golden_apple` | Canned Golden Apple | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Baked Potato `goldenfood:golden_baked_potato` | Canned Golden Baked Potato | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Beetroot `goldenfood:golden_beetroot` | Canned Golden Beetroot | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Beetroot Soup `goldenfood:golden_beetroot_soup` | Canned Golden Beetroot Soup | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Bread `goldenfood:golden_bread` | Canned Golden Bread | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Carrot `minecraft:golden_carrot` | Canned Golden Carrot | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Chorus Fruit `goldenfood:golden_chorus_fruit` | Canned Golden Chorus Fruit | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Cooked Beef `goldenfood:golden_cooked_beef` | Canned Golden Cooked Beef | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Cooked Chicken `goldenfood:golden_cooked_chicken` | Canned Golden Cooked Chicken | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Cooked Cod `goldenfood:golden_cooked_cod` | Canned Golden Cooked Cod | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Cooked Mutton `goldenfood:golden_cooked_mutton` | Canned Golden Cooked Mutton | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Cooked Porkchop `goldenfood:golden_cooked_porkchop` | Canned Golden Cooked Porkchop | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Cooked Rabbit `goldenfood:golden_cooked_rabbit` | Canned Golden Cooked Rabbit | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Cooked Salmon `goldenfood:golden_cooked_salmon` | Canned Golden Cooked Salmon | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Cookie `goldenfood:golden_cookie` | Canned Golden Cookie | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Dried Kelp `goldenfood:golden_dried_kelp` | Canned Golden Dried Kelp | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Glow Berries `goldenfood:golden_glow_berries` | Canned Golden Glow Berries | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Honey Bottle `goldenfood:golden_honey_bottle` | Canned Golden Honey Bottle | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Melon Slice `goldenfood:golden_melon_slice` | Canned Golden Melon Slice | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Milk Bucket `goldenfood:golden_milk_bucket` | Canned Golden Milk Bucket | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Moss Clump `immersive_weathering:golden_moss_clump` | Canned Golden Moss Clump | None / 2+1 | - | x | / | x | x | x | 4 / 4.8 | 14 / 10.8 | 10 / 12 | 71% / 111% | 100% / 100% |
| x | Golden Mushroom Stew `goldenfood:golden_mushroom_stew` | Canned Golden Mushroom Stew | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Pumpkin Pie `goldenfood:golden_pumpkin_pie` | Canned Golden Pumpkin Pie | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Rabbit Stew `goldenfood:golden_rabbit_stew` | Canned Golden Rabbit Stew | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |
| x | Golden Sweet Berries `goldenfood:golden_sweet_berries` | Canned Golden Sweet Berries | None / 2+1 | - | x | / | x | x | x | 4 / 9.6 | 14 / 20.4 | 10 / 24 | 71% / 118% | 100% / 100% |

### 2.18 Special <a id="special"></a>

Fruit Meals like raw fruit (section special). Chorus reuses Canned Goods' can (its code teleports).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Ancient Fruit `quark:ancient_fruit` | Canned Ancient Fruit | 3 / 2+1 | - | x | / | x | x | none | 4 / 2.4 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| x | Chorus Fruit `minecraft:chorus_fruit` | Canned Chorus Fruit | 3 / 2+1 | - | x | x | x | x | none | 4 / 2.4 | 12 / 7.2 | 15 / 9 | 125% / 125% | 100% / 100% |
| / | Azalea Flowers `immersive_weathering:azalea_flowers` | Canned Azalea Flowers | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Biscuit of Totality `botania:mana_cookie` | Canned Biscuit of Totality | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cosmic Cod `alexsmobs:cosmic_cod` | Canned Cosmic Cod | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Dog Food `farmersdelight:dog_food` | Canned Dog Food | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Fish Oil `alexsmobs:fish_oil` | Canned Fish Oil | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Ice Cubes `neapolitan:ice_cubes` | Canned Ice Cubes | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Ice Sickle `immersive_weathering:ice_sickle` | Canned Ice Sickle | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Icicle `immersive_weathering:icicle` | Canned Icicle | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Magma Gelatin `nethersdelight:magma_gelatin` | Canned Magma Gelatin | n/a | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mana in a Bottle `botania:mana_bottle` | Canned Mana in a Bottle | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Moss Clump `immersive_weathering:moss_clump` | Canned Moss Clump | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Propelpearl `nethersdelight:propelpearl` | Canned Propelpearl | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Rainbow Jelly `alexsmobs:rainbow_jelly` | Canned Rainbow Jelly | n/a | - | - | - | - | - | none | - | - | - | n/a | n/a |


### 2.19 Hazard <a id="hazard"></a>

The four built cans are Canned Goods' own (section hazard in [can_types.json](../kubejs/generation_scripts/food/can_types.json)): Meat Meal (5, 3) x 4 servings, pufferfish the Fish Meal (4, 2), no additive; Canned Goods' code applies the bad effects per serving. Added as a family 2026-10-20 (it was in [3](#outside)); the rest are new potential cans.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Poisonous Potato `minecraft:poisonous_potato` | Canned Poisonous Potatos | 8 | - | x | x | x | x | x | 2 / 1.2 | 16 / 9.6 | 20 / 12 | 125% / 125% | 100% / 100% |
| x | Pufferfish `minecraft:pufferfish` | Canned Pufferfishs | 8 | - | x | x | x | x | x | 1 / 0.2 | 8 / 1.6 | 16 / 8 | 200% / 500% | 80% / 67% |
| x | Rotten Flesh `minecraft:rotten_flesh` | Canned Tough Jerky | 5 | - | x | x | x | x | x | 4 / 0.8 | 20 / 4 | 20 / 12 | 100% / 300% | 100% / 100% |
| x | Spider Eye `minecraft:spider_eye` | Canned Night Berries | 5 | - | x | x | x | x | x | 2 / 3.2 | 10 / 16 | 20 / 12 | 200% / 75% | 100% / 100% |
| / | Copper Nugget `everythingcopper:copper_nugget` | Canned Copper Nugget | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| x | Maggot `alexsmobs:maggot` | Canned Maggots | 7 | - | x | / | x | x | none | 1 / 0.4 | 7 / 2.8 | 8 / 4 | 114% / 143% | 40% / 33% |
| / | Pond Water `immersive_weathering:pond_water` | Canned Pond Water | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Rotten Apple `domesticationinnovation:rotten_apple` | Canned Rotten Apple | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Rotten Cherry `vinery:rotten_cherry` | Canned Rotten Cherry | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Sinister Carrot `domesticationinnovation:sinister_carrot` | Canned Sinister Carrot | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Soap `supplementaries:soap` | Canned Soap | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

### 2.20 Egg and dairy <a id="egg-dairy"></a>

Added 2026-10-20 from [3](#outside): cooked eggs and the Brewin cheese wedges. All undecided.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x* | Flaxen Cheese Wedge `brewinandchewin:flaxen_cheese_wedge` | Canned Flaxen Cheese | 4 (Cooking Pot) | - | x | / | x | x | none | 4 / 8 | 16 / 32 | 20 / 40 | 125% / 125% | 100% / 100% |
| x* | Scarlet Cheese Wedge `brewinandchewin:scarlet_cheese_wedge` | Canned Scarlet Cheese | 4 (Cooking Pot) | - | x | / | x | x | none | 4 / 8 | 16 / 32 | 20 / 40 | 125% / 125% | 100% / 100% |
| x | Cheese Slice `vintagedelight:cheese_slice` (configured; waits on /dumpfood with Vintage loaded) | Canned Cheese Slices | - | - | - | - | - | x | - | 4 / 6.4 | 16 / 25.6 | 20 / 32 | 125% / 125% | 100% / 100% |
| / | Boiled Emu Egg `alexsmobs:boiled_emu_egg` | Canned Boiled Emu Egg | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Fried Egg `farmersdelight:fried_egg` | Canned Fried Egg | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Fried Egg `incubation:fried_egg` | Canned Fried Egg | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Scrambled Eggs `incubation:scrambled_eggs` | Canned Scrambled Eggs | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Century Egg `vintagedelight:century_egg` | Canned Century Egg | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Pickled Egg `vintagedelight:pickled_egg` | Canned Pickled Egg | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

*needs pot cooking recipe

### 2.21 Preserves <a id="preserves"></a>

Added 2026-10-20 from the ingredient type: jams, jellies, nut butter, tomato sauce. All undecided. Most are bottles (the can would hand back the glass bottles, like Canned Honey).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Tomato Sauce `farmersdelight:tomato_sauce` | Canned Tomato Sauce | 4 | - | x | / | x | x | none | 4 / 3.2 | 16 / 12.8 | 16 / 12.8 | 100% / 100% | 100% / 100% |
| / | Apple Jam `fruitsdelight:apple_jelly` | Canned Apple Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Bayberry Jam `fruitsdelight:bayberry_jelly` | Canned Bayberry Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Blueberry Jam `fruitsdelight:blueberry_jelly` | Canned Blueberry Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Chorus Jam `fruitsdelight:chorus_jelly` | Canned Chorus Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cranberry Jam `fruitsdelight:cranberry_jelly` | Canned Cranberry Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Durian Jam `fruitsdelight:durian_jelly` | Canned Durian Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Fig Jam `fruitsdelight:fig_jelly` | Canned Fig Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Glow Jam Jar `delightful:glow_jelly_bottle` | Canned Glow Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Glowberry Jam `fruitsdelight:glowberry_jelly` | Canned Glowberry Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Hamimelon Jam `fruitsdelight:hamimelon_jelly` | Canned Hamimelon Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Hawberry Jam `fruitsdelight:hawberry_jelly` | Canned Hawberry Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Jam Jar `delightful:jelly_bottle` | Canned Jam | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Kiwi Jam `fruitsdelight:kiwi_jelly` | Canned Kiwi Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Lemon Jam `fruitsdelight:lemon_jelly` | Canned Lemon Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Lychee Jam `fruitsdelight:lychee_jelly` | Canned Lychee Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mango Jam `fruitsdelight:mango_jelly` | Canned Mango Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mangosteen Jam `fruitsdelight:mangosteen_jelly` | Canned Mangosteen Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Melon Jam `fruitsdelight:melon_jelly` | Canned Melon Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mulberry Jam Bottle `upgrade_aquatic:mulberry_jam_bottle` | Canned Mulberry Jam | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Nut Butter Bottle `delightful:nut_butter_bottle` | Canned Nut Butter | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Orange Jam `fruitsdelight:orange_jelly` | Canned Orange Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Peach Jam `fruitsdelight:peach_jelly` | Canned Peach Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Pear Jam `fruitsdelight:pear_jelly` | Canned Pear Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Persimmon Jam `fruitsdelight:persimmon_jelly` | Canned Persimmon Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Pineapple Jam `fruitsdelight:pineapple_jelly` | Canned Pineapple Jam | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Sweetberry Jam `fruitsdelight:sweetberry_jelly` | Canned Sweetberry Jam | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Apple Sauce Bottle `vintagedelight:apple_sauce_bottle` | Canned Apple Sauce Bottle | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Apple Sauce Jar `vintagedelight:apple_sauce_jar` | Canned Apple Sauce Jar | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Glow Berries Jam Bottle `vintagedelight:glow_berry_jam_bottle` | Canned Glow Berries Jam Bottle | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Glow Berries Jam Jar `vintagedelight:glow_berry_jam_jar` | Canned Glow Berries Jam Jar | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Nut Mash Bottle `vintagedelight:nut_mash_bottle` | Canned Nut Mash Bottle | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Nut Mash Jar `vintagedelight:nut_mash_jar` | Canned Nut Mash Jar | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Pepper Jam Bottle `vintagedelight:pepper_jam_bottle` | Canned Pepper Jam Bottle | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Pepper Jam Jar `vintagedelight:pepper_jam_jar` | Canned Pepper Jam Jar | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Relish Bottle `vintagedelight:relish_bottle` | Canned Relish Bottle | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Relish Jar `vintagedelight:relish_jar` | Canned Relish Jar | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Sweet Berries Jam Bottle `vintagedelight:sweet_berry_jam_bottle` | Canned Sweet Berries Jam Bottle | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Sweet Berries Jam Jar `vintagedelight:sweet_berry_jam_jar` | Canned Sweet Berries Jam Jar | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Vinegar Bottle `vintagedelight:vinegar_bottle` | Canned Vinegar Bottle | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Vinegar Jar `vintagedelight:vinegar_jar` | Canned Vinegar Jar | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

### 2.22 Desserts <a id="desserts"></a>

Added 2026-10-20 from the sweet type: jello, custard, pudding, ice cream, gelatine, glowgurt. All undecided.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| / | Adzuki Ice Cream `neapolitan:adzuki_ice_cream` | Canned Adzuki Ice Cream | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Apple Jello `fruitsdelight:apple_jello` | Canned Apple Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Banana Ice Cream `neapolitan:banana_ice_cream` | Canned Banana Ice Cream | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Bayberry Jello `fruitsdelight:bayberry_jello` | Canned Bayberry Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Blueberry Custard `fruitsdelight:blueberry_custard` | Canned Blueberry Custard | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Blueberry Jello `fruitsdelight:blueberry_jello` | Canned Blueberry Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Bucket of Sweet Gelatine `quarkdelight:bucket_of_sweet_gelatine` | Canned Sweet Gelatine | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Ice Cream `neapolitan:chocolate_ice_cream` | Canned Chocolate Ice Cream | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Chorus Jello `fruitsdelight:chorus_jello` | Canned Chorus Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cranberry Jello `fruitsdelight:cranberry_jello` | Canned Cranberry Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Durian Jello `fruitsdelight:durian_jello` | Canned Durian Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Fig Jello `fruitsdelight:fig_jello` | Canned Fig Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Fig Pudding Slice `fruitsdelight:fig_pudding_slice` | Canned Fig Pudding | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Glow Berry Custard `farmersdelight:glow_berry_custard` | Canned Glow Berry Custard | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Glowberry Jello `fruitsdelight:glowberry_jello` | Canned Glowberry Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Glowgurt `berry_good:glowgurt` | Canned Glowgurt | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Hamimelon Jello `fruitsdelight:hamimelon_jello` | Canned Hamimelon Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Hawberry Jello `fruitsdelight:hawberry_jello` | Canned Hawberry Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Kiwi Jello `fruitsdelight:kiwi_jello` | Canned Kiwi Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Lemon Jello `fruitsdelight:lemon_jello` | Canned Lemon Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Lychee Jello `fruitsdelight:lychee_jello` | Canned Lychee Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mango Jello `fruitsdelight:mango_jello` | Canned Mango Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mangosteen Jello `fruitsdelight:mangosteen_jello` | Canned Mangosteen Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Matcha Ice Cream `delightful:matcha_ice_cream` | Canned Matcha Ice Cream | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Melon Jello `fruitsdelight:melon_jello` | Canned Melon Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mint Ice Cream `neapolitan:mint_ice_cream` | Canned Mint Ice Cream | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Neapolitan Ice Cream `neapolitan:neapolitan_ice_cream` | Canned Neapolitan Ice Cream | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Orange Jello `fruitsdelight:orange_jello` | Canned Orange Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Peach Jello `fruitsdelight:peach_jello` | Canned Peach Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Pear Jello `fruitsdelight:pear_jello` | Canned Pear Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Persimmon Jello `fruitsdelight:persimmon_jello` | Canned Persimmon Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Pineapple Jello `fruitsdelight:pineapple_jello` | Canned Pineapple Jello | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Salmonberry Ice Cream `delightful:salmonberry_ice_cream` | Canned Salmonberry Ice Cream | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Strawberry Ice Cream `neapolitan:strawberry_ice_cream` | Canned Strawberry Ice Cream | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Sweetberry Jello `fruitsdelight:sweetberry_jello` | Canned Sweetberry Jello | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Vanilla Ice Cream `neapolitan:vanilla_ice_cream` | Canned Vanilla Ice Cream | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Vanilla Pudding `neapolitan:vanilla_pudding` | Canned Vanilla Pudding | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

### 2.23 Cake <a id="cake"></a>

Added 2026-10-20 from the sweet type: cake slices and the one whole edible cake item. All undecided. Canned Goods has its own `canned_goods:canned_cake` (no recipe here, [3.1](#new-sections)); slices could work like the pie fillings.

Wanted (scratchpad 2026-10-21, [TODO](todo/TODO.md) `cans/canning #15`): one canned cake with 4 servings (a quarter cake each, fast to eat), made from 1 cake + can, or 4 slices + an additive (sugar, honey) + can. The per-slice rows below stay `/`; which id (Canned Goods' `canned_cake` or a pack can) is still open. The slice audit (remove unobtainable slices) is `baking/baked-goods #5`.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| / | Mangosteen Cake `fruitsdelight:mangosteen_cake` | Canned Mangosteen Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Adzuki Cake `abnormals_delight:adzuki_cake_slice` | Canned Adzuki Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Aloe Cake `create_central_kitchen:aloe_cake_slice` | Canned Aloe Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Banana Cake `abnormals_delight:banana_cake_slice` | Canned Banana Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Cake `farmersdelight:cake_slice` | Canned Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Chocolate Cake `abnormals_delight:chocolate_cake_slice` | Canned Chocolate Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Chocolate Cake `create_central_kitchen:chocolate_cake_slice` | Canned Chocolate Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Coffee Cake `farmersrespite:coffee_cake_slice` | Canned Coffee Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Honey Cake `create_central_kitchen:honey_cake_slice` | Canned Honey Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Mint Cake `abnormals_delight:mint_cake_slice` | Canned Mint Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Passionfruit Cake `create_central_kitchen:passionfruit_cake_slice` | Canned Passionfruit Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Pumpkin Cake `create_central_kitchen:pumpkin_cake_slice` | Canned Pumpkin Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Strawberry Cake `abnormals_delight:strawberry_cake_slice` | Canned Strawberry Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Sweet Berry Cake `create_central_kitchen:sweet_berry_cake_slice` | Canned Sweet Berry Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Vanilla Cake `abnormals_delight:vanilla_cake_slice` | Canned Vanilla Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Yucca Cake `create_central_kitchen:yucca_cake_slice` | Canned Yucca Cake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Slice of Yucca Gateau `abnormals_delight:yucca_gateau_slice` | Canned Yucca Gateau | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Carrot Cake Slice `veggiesdelight:carrot_cake_slice` | Canned Carrot Cake Slice | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

### 2.24 Sweets <a id="sweets"></a>

The rest of the sweet type (candy, chocolate, waffles, rolls, muffins, marshmallows, toasts). Added 2026-10-20, all undecided.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Pear with Rock Sugar `fruitsdelight:pear_with_rock_sugar` | Canned Pears in Rock Sugar | 6 / 3+1 | - | x | / | x | x | x | 8 / 9.6 | 48 / 57.6 | 80 / 96 | 167% / 167% | 100% / 100% |
| x | Sweet Roll `create:sweet_roll` | Canned Sweet Rolls | 6 | - | x | / | x | x | none | 6 / 9.6 | 36 / 57.6 | 48 / 76.8 | 133% / 133% | 100% / 100% |
| / | Bar of Caramel `create_confectionery:bar_of_caramel` | Canned Bar of Caramel | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Bar of Chocolate `create:bar_of_chocolate` | Canned Bar of Chocolate | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Bar of Dark Chocolate `create_confectionery:bar_of_black_chocolate` | Canned Bar of Dark Chocolate | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Bar of Ruby Chocolate `create_confectionery:bar_of_ruby_chocolate` | Canned Bar of Ruby Chocolate | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Bar of White Chocolate `create_confectionery:bar_of_white_chocolate` | Canned Bar of White Chocolate | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Blueberry Muffin `fruitsdelight:blueberry_muffin` | Canned Blueberry Muffin | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Candy `supplementaries:candy` | Canned Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Candy Cane `create_confectionery:candy_cane` | Canned Candy Cane | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Caramel Apple `create_factory:caramel_apple` | Canned Caramel Apple | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Caramel Glazed Berries `create_confectionery:caramel_glazed_berries` | Canned Caramel Glazed Berries | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Caramel Roll `create_factory:caramel_roll` | Canned Caramel Roll | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Caramel Waffle `create_factory:caramel_waffle` | Canned Caramel Waffle | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Caramelized Marshmallows on a Stick `create_confectionery:caramelized_marshmellow_on_a_stick` | Canned Caramelized Marshmallows on a Stick | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Apple `create_factory:chocolate_apple` | Canned Chocolate Apple | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Bar `neapolitan:chocolate_bar` | Canned Chocolate Bar | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Chocolate Candy `create_confectionery:chocolate_candy_1` | Canned Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Candy `create_confectionery:chocolate_candy_2` | Canned Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Candy `create_confectionery:chocolate_candy_3` | Canned Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Candy `create_confectionery:chocolate_candy` | Canned Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Copper Coin `lightmanscurrency:coin_chocolate_copper` | Canned Chocolate Copper Coin | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Diamond Coin `lightmanscurrency:coin_chocolate_diamond` | Canned Chocolate Diamond Coin | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Emerald Coin `lightmanscurrency:coin_chocolate_emerald` | Canned Chocolate Emerald Coin | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Glazed Berries `create:chocolate_glazed_berries` | Canned Chocolate Glazed Berries | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Glazed Marshmallow `create_confectionery:chocolate_glazed_marshmallow` | Canned Chocolate Glazed Marshmallow | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Gold Coin `lightmanscurrency:coin_chocolate_gold` | Canned Chocolate Gold Coin | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Iron Coin `lightmanscurrency:coin_chocolate_iron` | Canned Chocolate Iron Coin | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Netherite Coin `lightmanscurrency:coin_chocolate_netherite` | Canned Chocolate Netherite Coin | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Night Berries `neapolitan:chocolate_spider_eye` | Canned Chocolate Night Berries | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Chocolate Popsicle `moredelight:chocolate_popsicle` | Canned Chocolate Popsicle | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Roll `create_factory:chocolate_roll` | Canned Chocolate Roll | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chocolate Strawberries `neapolitan:chocolate_strawberries` | Canned Chocolate Strawberries | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Chocolate Waffle `create_factory:chocolate_waffle` | Canned Chocolate Waffle | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cocoa Fudge `brewinandchewin:cocoa_fudge` | Canned Cocoa Fudge | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cotton Candy `cloudstorage:cotton_candy` | Canned Cotton Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cranberry Muffin `fruitsdelight:cranberry_muffin` | Canned Cranberry Muffin | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Dark Chocolate Apple `create_factory:dark_chocolate_apple` | Canned Dark Chocolate Apple | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Dark Chocolate Candy `create_confectionery:black_chocolate_candy_1` | Canned Dark Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Dark Chocolate Candy `create_confectionery:black_chocolate_candy_2` | Canned Dark Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Dark Chocolate Candy `create_confectionery:black_chocolate_candy_3` | Canned Dark Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Dark Chocolate Candy `create_confectionery:black_chocolate_candy` | Canned Dark Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Dark Chocolate Glazed Berries `create_confectionery:black_chocolate_glazed_berries` | Canned Dark Chocolate Glazed Berries | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Dark Chocolate Glazed Marshmallow `create_confectionery:black_chocolate_glazed_marshmallow` | Canned Dark Chocolate Glazed Marshmallow | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Dark Chocolate Roll `create_factory:dark_chocolate_roll` | Canned Dark Chocolate Roll | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Dark Chocolate Waffle `create_factory:dark_chocolate_waffle` | Canned Dark Chocolate Waffle | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Fruit Beignet `rusticdelight:fruit_beignet` | Canned Fruit Beignet | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Gingerbread `create_confectionery:gingerbread` | Canned Gingerbread | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Gingerbread Man `create_confectionery:gingerbread_man` | Canned Gingerbread Men | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Hamimelon Popsicle `fruitsdelight:hamimelon_popsicle` | Canned Hamimelon Popsicle | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Hamimelon Shaved Ice `fruitsdelight:hamimelon_shaved_ice` | Canned Hamimelon Shaved Ice | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Hawberry Sheet `fruitsdelight:hawberry_sheet` | Canned Hawberry Sheet | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Hawberry Stick `fruitsdelight:hawberry_stick` | Canned Hawberry Stick | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Honey Apple `buzzier_bees:honey_apple` | Canned Honey Apple | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Honey Candy `create_confectionery:honey_candy` | Canned Honey Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Honey Glazed Berries `create_factory:honey_glazed_berries` | Canned Honey Glazed Berries | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Honey Glazed Walnut `delightful:honey_glazed_walnut` | Canned Honey Glazed Walnut | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Honey Roll `create_factory:honey_roll` | Canned Honey Roll | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Honey Waffle `create_factory:honey_waffle` | Canned Honey Waffle | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Honeyed Apple `create:honeyed_apple` | Canned Honeyed Apple | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Kiwi Popsicle `fruitsdelight:kiwi_popsicle` | Canned Kiwi Popsicle | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Marshmallow `create_confectionery:marshmallow` | Canned Marshmallow | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Marshmallow on a Stick `delightful:marshmallow_stick` | Canned Marshmallow on a Stick | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Marshmallows on a Stick `create_confectionery:marshmallow_on_a_stick` | Canned Marshmallows on a Stick | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Melon Popsicle `farmersdelight:melon_popsicle` | Canned Melon Popsicle | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Mint Candies `neapolitan:mint_candies` | Canned Mint Candies | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Mint Chocolate `neapolitan:mint_chocolate` | Canned Mint Chocolate | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Roasted Marshmallow on a Stick `delightful:cooked_marshmallow_stick` | Canned Roasted Marshmallow on a Stick | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Rock Candy `delightful:rock_candy` | Canned Rock Candy | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Ruby Chocolate Apple `create_factory:ruby_chocolate_apple` | Canned Ruby Chocolate Apple | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Ruby Chocolate Candy `create_confectionery:ruby_chocolate_candy_1` | Canned Ruby Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Ruby Chocolate Candy `create_confectionery:ruby_chocolate_candy_2` | Canned Ruby Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Ruby Chocolate Candy `create_confectionery:ruby_chocolate_candy_3` | Canned Ruby Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Ruby Chocolate Candy `create_confectionery:ruby_chocolate_candy` | Canned Ruby Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Ruby Chocolate Glazed Berries `create_confectionery:ruby_chocolate_glazed_berries` | Canned Ruby Chocolate Glazed Berries | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Ruby Chocolate Glazed Marshmallow `create_confectionery:ruby_chocolate_glazed_marshmallow` | Canned Ruby Chocolate Glazed Marshmallow | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Ruby Chocolate Roll `create_factory:ruby_chocolate_roll` | Canned Ruby Chocolate Roll | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Ruby Chocolate Waffle `create_factory:ruby_chocolate_waffle` | Canned Ruby Chocolate Waffle | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | S'more `delightful:smore` | Canned S'more | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Salmonberry Gummy `delightful:salmonberry_gummy` | Canned Salmonberry Gummy | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Strange Eclair `endersdelight:strange_eclair` | Canned Strange Eclair | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Strawberry Bean Bonbons `neapolitan:strawberry_bean_bonbons` | Canned Strawberry Bean Bonbons | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Toast with Chocolate `moredelight:toast_with_chocolate` | Canned Toast with Chocolate | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Toast with Glow Berries `moredelight:toast_with_glow_berries` | Canned Toast with Glow Berries | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Toast with Honey `moredelight:toast_with_honey` | Canned Toast with Honey | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Toast with Sweet Berries `moredelight:toast_with_sweet_berries` | Canned Toast with Sweet Berries | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Vanilla Chocolate Fingers `neapolitan:vanilla_chocolate_fingers` | Canned Vanilla Chocolate Fingers | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Vanilla Fudge `neapolitan:vanilla_fudge` | Canned Vanilla Fudge | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Waffle `create_factory:waffle` | Canned Waffle | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | White Chocolate Apple `create_factory:white_chocolate_apple` | Canned White Chocolate Apple | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | White Chocolate Candy `create_confectionery:white_chocolate_candy_1` | Canned White Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | White Chocolate Candy `create_confectionery:white_chocolate_candy_2` | Canned White Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | White Chocolate Candy `create_confectionery:white_chocolate_candy_3` | Canned White Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | White Chocolate Candy `create_confectionery:white_chocolate_candy` | Canned White Chocolate Candy | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | White Chocolate Glazed Berries `create_confectionery:white_chocolate_glazed_berries` | Canned White Chocolate Glazed Berries | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | White Chocolate Glazed Marshmallow `create_confectionery:white_chocolate_glazed_marshmallow` | Canned White Chocolate Glazed Marshmallow | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | White Chocolate Roll `create_factory:white_chocolate_roll` | Canned White Chocolate Roll | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | White Chocolate Waffle `create_factory:white_chocolate_waffle` | Canned White Chocolate Waffle | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Beetroot Brownie `veggiesdelight:beetroot_brownie` | Canned Beetroot Brownie | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Chocolate Nut Granola Bar `vintagedelight:chocolate_nut_granola_bar` | Canned Chocolate Nut Granola Bar | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Deluxe Granola Bar `vintagedelight:deluxe_granola_bar` | Canned Deluxe Granola Bar | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Fruity Granola Bar `vintagedelight:fruity_granola_bar` | Canned Fruity Granola Bar | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Honey Roasted Peanut `vintagedelight:honey_roasted_peanut` (a sweet) | Canned Honey Roasted Peanut | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Sweet Potato Cupcake `veggiesdelight:sweet_potato_cupcake` | Canned Sweet Potato Cupcake | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Sweet Potato Pancakes `veggiesdelight:sweet_potato_pancakes` | Canned Sweet Potato Pancakes | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

### 2.25 Handheld <a id="handheld"></a>

The handheld type (sandwiches, burgers, rolls, skewers, stuffed peppers). Added 2026-10-20, all undecided.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Dumplings `farmersdelight:dumplings` | Canned Dumplings | 2 | - | x | / | x | x | none | 8 / 12.8 | 16 / 25.6 | 24 / 38.4 | 150% / 150% | 100% / 100% |
| / | Bacon Sandwich `farmersdelight:bacon_sandwich` | Canned Bacon Sandwich | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Barbecue on a Stick `farmersdelight:barbecue_stick` | Canned Barbecue on a Stick | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Bison Burger `alexsdelight:bison_burger` | Canned Bison Burger | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Bunfungus Sandwich `alexsdelight:bunfungus_sandwich` | Canned Bunfungus Sandwich | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cabbage Rolls `farmersdelight:cabbage_rolls` | Canned Cabbage Rolls | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Calamari Roll `rusticdelight:calamari_roll` | Canned Calamari Roll | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cheeseburger `delightful:cheeseburger` | Canned Cheeseburger | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Chicken Sandwich `farmersdelight:chicken_sandwich` | Canned Chicken Sandwich | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chicken Sandwich with Egg and Tomato `moredelight:chicken_sandwich_with_egg_and_tomato` | Canned Chicken Sandwich with Egg and Tomato | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chunk Nugget `delightful:chunk_nugget` | Canned Chunk Nugget | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Chunkwich `delightful:chunkwich` | Canned Chunkwich | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Classic Corn Dog `corn_delight:classic_corn_dog` | Canned Classic Corn Dog | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cod Roll `farmersdelight:cod_roll` | Canned Cod Roll | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cod n Chips `pneumaticcraft:cod_n_chips` | Canned Cod n Chips | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Corn Dog `corn_delight:corn_dog` | Canned Corn Dog | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Crab Bars `quarkdelight:crab_bars` | Canned Crab Bars | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Crab Rangoon `delightful:crab_rangoon` | Canned Crab Rangoon | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Crawling Sandwich `endersdelight:crawling_sandwich` | Canned Crawling Sandwich | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Crispy Skewer `endersdelight:crispy_skewer` | Canned Crispy Skewer | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Deluxe Cheeseburger `delightful:deluxe_cheeseburger` | Canned Deluxe Cheeseburger | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Egg Sandwich `farmersdelight:egg_sandwich` | Canned Egg Sandwich | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Egg with Bacon Sandwich `moredelight:egg_with_bacon_sandwich` | Canned Egg with Bacon Sandwich | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Gongylidia Bruschetta `alexsdelight:gongylidia_bruschetta` | Canned Gongylidia Bruschetta | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Ham and Cheese Sandwich `brewinandchewin:ham_and_cheese_sandwich` | Canned Ham and Cheese Sandwich | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Hamburger `farmersdelight:hamburger` | Canned Hamburger | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Hamburger with Egg `moredelight:hamburger_with_egg` | Canned Hamburger with Egg | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Hawberry Roll `fruitsdelight:hawberry_roll` | Canned Hawberry Roll | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Jelly Bread `fruitsdelight:jelly_bread` | Canned Jelly Bread | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Kangaroo Burger `alexsmobs:kangaroo_burger` | Canned Kangaroo Burger | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Kelp Roll `farmersdelight:kelp_roll` | Canned Kelp Roll | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Kelp Roll Slice `farmersdelight:kelp_roll_slice` | Canned Kelp Roll Slice | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Loaded Hamburger `moredelight:loaded_hamburger` | Canned Loaded Hamburger | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Mutton Wrap `farmersdelight:mutton_wrap` | Canned Mutton Wrap | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Nether Skewer `nethersdelight:nether_skewer` | Canned Nether Skewer | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Nut Butter and Jam Sandwich `delightful:nut_butter_and_jelly_sandwich` | Canned Nut Butter and Jam Sandwich | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Pizza Slice `brewinandchewin:pizza_slice` | Canned Pizza Slice | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Porkchop Sandwich `moredelight:porkchop_sandwich` | Canned Porkchop Sandwich | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Quiche Slice `brewinandchewin:quiche_slice` | Canned Quiche Slice | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Salmon Roll `farmersdelight:salmon_roll` | Canned Salmon Roll | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Salmon Tempura `pneumaticcraft:salmon_tempura` | Canned Salmon Tempura | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Simple Hamburger `moredelight:simple_hamburger` | Canned Simple Hamburger | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Spring Rolls `rusticdelight:spring_rolls` | Canned Spring Rolls | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Steak Sandwich `moredelight:steak_sandwich` | Canned Steak Sandwich | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Stuffed Green Bell Pepper `rusticdelight:stuffed_bell_pepper_green` | Canned Stuffed Green Bell Pepper | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Stuffed Potato `farmersdelight:stuffed_potato` | Canned Stuffed Potato | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Stuffed Red Bell Pepper `rusticdelight:stuffed_bell_pepper_red` | Canned Stuffed Red Bell Pepper | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Stuffed Yellow Bell Pepper `rusticdelight:stuffed_bell_pepper_yellow` | Canned Stuffed Yellow Bell Pepper | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Sweet Berry Meatballs `berry_good:sweet_berry_meatballs` | Canned Sweet Berry Meatballs | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Taco `corn_delight:taco` | Canned Taco | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Toast with Egg `moredelight:toast_with_egg` | Canned Toast with Egg | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Tomato Sandwich `moredelight:tomato_sandwich` | Canned Tomato Sandwich | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cheese Burger `vintagedelight:cheese_burger` | Canned Cheese Burger | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cheese Pizza Slice `vintagedelight:cheese_pizza_slice` | Canned Cheese Pizza Slice | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Chicken Fajitas Wrap `veggiesdelight:chicken_fajitas_wrap` | Canned Chicken Fajitas Wrap | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Garlic Stuffed Mushrooms `veggiesdelight:garlic_stuffed_mushrooms` | Canned Garlic Stuffed Mushrooms | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Meat Pizza Slice `vintagedelight:meat_pizza_slice` | Canned Meat Pizza Slice | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | PB & J `vintagedelight:pb_j` | Canned PB & J | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Stuffed Bell Pepper `veggiesdelight:stuffed_bellpepper` | Canned Stuffed Bell Pepper | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Stuffed Burrito `vintagedelight:stuffed_burrito` | Canned Stuffed Burrito | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Stuffed Zucchini Boat `veggiesdelight:stuffed_zucchini_boat` | Canned Stuffed Zucchini Boat | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | The Burger `vintagedelight:deluxe_burger` | Canned The Burger | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Turnip Mutton Skewer `veggiesdelight:turnip_mutton_skewer` | Canned Turnip Mutton Skewer | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Vegan Pizza Slice `veggiesdelight:vegan_pizza_slice` | Canned Vegan Pizza Slice | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Vegetables Wrap `veggiesdelight:vegetables_wrap` | Canned Vegetables Wrap | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Vegetarian Burger `veggiesdelight:vegetarian_burger` | Canned Vegetarian Burger | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Zucchini Quiche Slice `veggiesdelight:zucchini_quiche_slice` | Canned Zucchini Quiche Slice | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Zucchini Sandwich `veggiesdelight:zucchini_sandwich` | Canned Zucchini Sandwich | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

### 2.26 Ingredients <a id="ingredients"></a>

The rest of the ingredient type (batter, dough, oil, crust, raw pasta, coffee beans). Added 2026-10-20, all undecided.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s | In -> out | Out vs target |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Dough `create:dough` (not a food; the can is an ingredient can, not edible) | Canned Dough | 8 (Create dough, 8 servings) | - | x | / | x | x | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Animal Fat `delightful:animal_fat` | Canned Animal Fat | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Apple Mash `vinery:apple_mash` | Canned Apple Mash | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Batter `rusticdelight:batter` | Canned Batter | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Chopped Clover `delightful:chopped_clover` | Canned Chopped Clover | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cooking Oil `rusticdelight:cooking_oil` | Canned Cooking Oil | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cornbread Batter `corn_delight:cornbread_batter` | Canned Cornbread Batter | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Dried Vanilla Pods `neapolitan:dried_vanilla_pods` | Canned Dried Vanilla Pods | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Golden Coffee Beans `rusticdelight:golden_coffee_beans` | Canned Golden Coffee Beans | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Green Tea Leaves `delightful:green_tea_leaf` | Canned Green Tea Leaves | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Pie Crust `farmersdelight:pie_crust` | Canned Pie Crust | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Raw Pasta `farmersdelight:raw_pasta` | Canned Raw Pasta | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Raw Tortilla `corn_delight:tortilla_raw` | Canned Raw Tortilla | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Roasted Coffee Beans `rusticdelight:roasted_coffee_beans` | Canned Roasted Coffee Beans | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Sweet Berry Mince `berry_good:sweet_berry_mince` | Canned Sweet Berry Mince | - | - | - | - | - | - | none | - | - | - | n/a | n/a |
| / | Cheese Curds `vintagedelight:cheese_curds` | Canned Cheese Curds | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cucumber Dough `vintagedelight:cucumber_dough` | Canned Cucumber Dough | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Cucumber Noodles `vintagedelight:cucumber_noodles` | Canned Cucumber Noodles | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Oat Dough `vintagedelight:oat_dough` | Canned Oat Dough | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Potato Noodle `veggiesdelight:potato_noodle` | Canned Potato Noodle | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Sweet Potato Dough `veggiesdelight:sweet_potato_dough` | Canned Sweet Potato Dough | - | - | - | - | - | - | - | - | - | - | n/a | n/a |
| / | Uncooked Mhadjeb `veggiesdelight:uncooked_mhadjeb` | Canned Uncooked Mhadjeb | - | - | - | - | - | - | - | - | - | - | n/a | n/a |

## 3. Outside the families <a id="outside"></a>

Since 2026-10-20 every typed food is on a sheet (families 2.19 to 2.26 were pulled in from here). Only the `canned`
type (the cans themselves) and the excluded items stay off. A food added to the pack later goes into the family it fits.

### 3.1 New sections and placeholder values <a id="new-sections"></a>

Generated by [gen_can_types.py](../tools/gen_can_types.py) (SECTIONS). "Meal" sections size each source's recipe by
value: the basic recipe is worth about 0.8 of the can (at most 8 items), the additive one 0.7 (fruit) or 0.8 (meat,
fish) of that. Sliced forms share the whole item's slots and count; no recipe makes a partly full can (4.2).

| Section | Meal | Servings | Recipe | Additive | Status |
|---|---|---:|---|---|---|
| raw_fruit, special | Fruit Meal (5, 3) | 3 | sized per source | sugar or honey | same rule as the hand-written fruit cans |
| raw_meat | Meat Meal (5, 3) | 4 | sized per source | salt or bone broth | same rule as the hand-written meat cans |
| raw_fish | Fish Meal (4, 2) | 4 | sized per source | salt or bone broth | since 2026-10-20 (was the Meat Meal) |
| hazard | Meat Meal (5, 3) | 4 | sized per source | none | placeholder; Canned Goods' code adds the bad effects per serving |
| meal (pastas) | pasta x0.75, effects 0.75x | 4 | 2 pastas | none | placeholder |
| pie | slice x1 | 4 | 4 slices; filling + pie crust -> pie | none | placeholder |
| feast | serving x1 | 6 | 1 placed feast block | none | placeholder |
| drink | item x1 | 4 | 4 items | none | placeholder |

The hazard and low-value fish cans clamp at 8 items, so they come out generous (8 pufferfish -> 4 Meat Meals), the same
way the hand-written raw chicken can does.

Canned Goods' own cans that no recipe makes any more (their recipes are removed in canning.js). Since 2026-10-13 these
have recipes again: Chorus Fruit, Honey Bottles, Poisonous Potatos, Pufferfishs, Tough Jerky, Night Berries, Tropical Fish.
Canned Apples `canned_goods:canned_apples`, Canned Bread `canned_goods:canned_beetroot_soup`, Canned Beetroots `canned_goods:canned_beets`, Canned Cake `canned_goods:canned_cake`, Canned Pumpkin Pies `canned_goods:canned_carrots`, Canned Chorus Fruit `canned_goods:canned_chorus_fruits`, Canned Notch Apples `canned_goods:canned_enchanted_apples`, Canned Food `canned_goods:canned_food`, Canned Glow Berries `canned_goods:canned_glow_berries`, Canned Golden Apples `canned_goods:canned_golden_apples`, Canned Golden Carrots `canned_goods:canned_golden_carrot`, Canned Honey Bottles `canned_goods:canned_honey_bottles`, Canned Melon Slice `canned_goods:canned_melon_slice`, Canned Poisonous Potatos `canned_goods:canned_posionous_potatos`, Canned Potatos `canned_goods:canned_potatos`, Canned Pufferfishs `canned_goods:canned_pufferfishs`, Canned Golden Carrots `canned_goods:canned_pumpkin_pie`, Canned Tough Jerky `canned_goods:canned_rotten_flesh`, Canned Night Berries `canned_goods:canned_spider_eyes`, Canned Suspicious Stew `canned_goods:canned_suspicious_stew`, Canned Sweet Berries `canned_goods:canned_sweet_berries`, Canned Tropical Fish `canned_goods:canned_tropical_fish`, Hot Hot Hotpot `canned_goods:hot_hot_hotpot`.

## 4. Decisions

Settled 2026-10-20. "Decided" means the rule is fixed; none of it is built yet except where marked. Build order is in
[cans.md](cans.md) 4.

| Topic | Decision | Status |
|---|---|---|
| Pumpkin | Fruit: moved to canning.js's raw fruit section, 3 servings, 4 slices or 3 + sugar or honey (recipe id `canned_pumpkin_sugar`, was `canned_pumpkin_salt`). Slices only: the whole pumpkin is a block, not a food | BUILT 2026-10-20, not run in game |
| White strawberries | Same ratio as normal strawberries (the old 3 / 2 + sugar), with a much higher boost to the per-serving nutrition and saturation (raw item is 5 hunger, 0.05). Numbers set 2026-10-21 from the audit: own serving 7 hunger, 5 saturation points (Fruit Meal 5 / 3), 3 servings: in -> out 140% hunger (the family's usual is 125%, normal strawberries 83%) | BUILT 2026-10-21, not run in game |
| Bananas | Merged: Alex's Mobs bananas stay out of the can. Any behaviour or recipe the Alex's Mobs banana had (can, slices) is replicated for the Neapolitan banana | decided, not built |
| Raw meat "half can" | Was: a slice recipe that needed more than 8 items took 8 and made a can with fewer servings (8 Raw Chicken Cuts -> 2/4). Removed: no recipe makes a partly full can ([4.2](#slice-family-rule)) | superseded 2026-10-20 |
| Sliced and whole meats | Every can whose whole item and slice are both edible takes either, in the same slots at the whole item's count ([4.2](#slice-family-rule)). Slicing first is the 2x+ boost: one whole item cuts into 2 or more slices | BUILT 2026-10-20, not run in game |
| Partial cans as pie input | Not allowed. Filling + pie crust -> pie requires a full filling can; an opened can no longer makes a pie (NCH `cans { whole }`: the recipe's result checks `CanServings.isPartial`) | BUILT 2026-10-21, not run in game (CP-23) |
| Placeholder goals | Unchanged: meal, pie, feast, drink and hazard values stay placeholders until the food tuning pass ([3.1](#new-sections)) | open |
| Removed cans already shipped | No issue, no remap | closed |
| Crafting uses | For now the only crafting uses of a can are a manual shaped or shapeless craft (no machine recipes: Create mixing, mechanical crafting, other mods) | decided |
| Hand-written cans | Rebuild the old hand-written cans (raw veggie, fruit, meat) in the new mod, with their effects passed through (they carry none today, [cans.md](cans.md) F2) | decided, not built |
| Own mod | Canning is split into its own mod, Creative Canning, built the same way as the alcohol mod and Creative Labels | decided, not built |
| Golden cans | Hold 10 servings each at 0.25x nutrition (hunger and saturation) and 0.333x effect length. They stay susceptible to golden-food effect changes: when the golden foods' effects change, the cans follow. Later golden food effect changes are marked for follow-up | decided, not built |

### 4.1 Non-meat sliceable items <a id="sliceable-nonmeat"></a>

The either-input rule (slices or the whole item, same item count) covers every non-meat food whose whole item can be
eaten. Whole-to-slice yields are from the mods' cutting recipes (knife; Create cutting for Fruits Delight).

| Whole item | Slice item | Slices per whole | Can | Accepts |
|---|---|---:|---|---|
| Cabbage `farmersdelight:cabbage` | Cabbage Leaf | 2 | Canned Cabbage | both |
| Potato `minecraft:potato` | Potato Slices `rusticdelight:potato_slices` | 2 | Canned Potato | both |
| Baked Potato `minecraft:baked_potato` | Baked Potato Slices `rusticdelight:baked_potato_slices` | 2 | Canned Baked Potato | both |
| Lemon `fruitsdelight:lemon` | Lemon Slice | 4 | Canned Lemon | both |
| Orange `fruitsdelight:orange` | Orange Slice | 4 | Canned Orange | both |
| Pumpkin (block) | Pumpkin Slice `farmersdelight:pumpkin_slice` | 4 | Canned Pumpkin | slices only |
| Melon (block), Mini Melon | Melon Slice `minecraft:melon_slice` | 9, 6 | Canned Melon | slices only |
| Cantaloupe | Cantaloupe Slice `delightful:cantaloupe_slice` | 6 | Canned Cantaloupe | slices only |
| Hamimelon | Hamimelon Slice | 9 | Canned Hamimelon | slices only |
| Pineapple | Pineapple Slice | 8 | Canned Pineapple | slices only |

- **Slice-only by design (5 cans):** pumpkin, melon, cantaloupe, hamimelon and pineapple. The whole item cannot be eaten,
  so it is not a can input (no whole pumpkin, melon, mini melon, cantaloupe, hamimelon or pineapple recipe). Their
  counts are anchored on the slice and stay as they are today.
- **Cantaloupe food values:** BUILT 2026-10-20. The Cantaloupe Slice takes the Pumpkin Slice's whole FoodProperties
  (3 hunger, 0.3 modifier, no Resistance) via [food_values.js](../kubejs/startup_scripts/cooking/food_values.js). Its can
  is 4 slices, or 3 + sugar or honey, the same as Canned Pumpkin. Compost value (0.6) and other uses of the slice not
  rechecked.
- **Cabbage, potato, baked potato:** already shared one count (whole-anchored) between whole and slices.
- **Lemon and orange:** BUILT 2026-10-20: the whole fruit's count for both inputs (lemon 3, or 2 + sugar or honey;
  orange 4, or 3 + sugar or honey).
- **Not part of this rule:** pie, cake, pizza, quiche and kelp roll slices (portions of a prepared dish). Pie slices
  already make a filling can from 4 slices. Bread Slice has no can.
- **Not checked:** other mods' slice items not in the food catalog's `sliced` tags.

### 4.2 Sizing rule for every can with a sliced input <a id="slice-family-rule"></a>

BUILT 2026-10-20, not run in game. Applies to every can whose whole item and slice are both edible.

1. **The whole can definition comes from the whole food.** Values, servings and the count are the whole item's: the
   basic recipe's items are worth 0.8 of the can in hunger + saturation points, capped at 8 (the grid less the can);
   the additive recipe takes 0.7 (fruit) or 0.8 (meat, fish) of that count.
2. **The slice takes the whole item's slots at the same count.** Slicing first is therefore a 2x+ boost: one whole item
   cuts into 2 or more slices.
3. **No recipe makes a partly full can.**
4. Slice-only cans (pumpkin, melon, cantaloupe, hamimelon, pineapple; 4.1) have no edible whole item, so they are sized
   on the slice.

| Can | Inputs (shared slots) | Basic | + additive |
|---|---|---:|---:|
| Canned Raw Beef | Raw Beef, Minced Beef (`#forge:raw_beef`) | 5 | 4 + salt or broth |
| Canned Raw Pork | Porkchop, Raw Bacon (`#forge:raw_pork`) | 5 | 4 + salt or broth |
| Canned Raw Chicken | Raw Chicken, Raw Chicken Cuts (`#forge:raw_chicken`) | 8 | 6 + salt or broth |
| Canned Raw Mutton | Raw Mutton, Raw Mutton Chops (`#forge:raw_mutton`) | 8 | 6 + salt or broth |
| Canned Shulker Flesh | Shulker Mollusk, Shulker Filet | 8 | 6 + salt or broth |
| Canned Raw Cod, Salmon | the fish, its slice | 8 | 6 + salt or broth |
| Canned Raw Calamari, Perch | the fish, its slice | 7 | 6 + salt or broth |
| Canned Raw Catfish | Catfish, Raw Catfish Slice | 6 | 5 + salt or broth |
| Canned Raw Pike | Pike, Pike Slice | 4 | 3 + salt or broth |
| Canned Lemon | Lemon, Lemon Slice | 3 | 2 + sugar or honey |
| Canned Orange | Orange, Orange Slice | 4 | 3 + sugar or honey |
| Canned Cabbage | `#forge:crops/cabbage` | 5 | 3 + salt or broth |
| Canned Potato | `#forge:vegetables/potato` | 8 | 6 + salt or broth |
| Canned Baked Potato | Baked Potato, Baked Potato Slices | 6 | 5 + salt |
| Cooked chicken, mutton, cod, salmon, calamari, catfish, perch, pike cans | the cooked item, its cooked slice | 5 | 4 + salt or broth |

Removed recipe ids: `canned_raw_beef_minced(_broth)`, `canned_raw_pork_bacon(_broth)`, `canned_raw_chicken_cuts`,
`canned_raw_mutton_chops`, and the generated `_<slice>` / `_<slice>_sugar` / `_<slice>_broth` variants (now part of the
basic and additive recipes).

Fish cans use the Fish Meal (4 + 2) since 2026-10-20: 4 servings = a can worth 24, goal 19.2 (Meat Meal cans stay at
32, goal 25.6). Other fish counts after the change: blobfish 4 / 3, flying fish 4 / 3, lobster tail 5 / 4, lionfish
6 / 5 (with salt or broth).

The 8 foods that were capped at 8 before the fish change. "In" is the hunger + saturation points of the basic recipe,
"out" the can's total (servings x meal):

| Can | Whole item (value) | Before: count, in / out, goal share | Now: count, in / out, goal share | Still capped |
|---|---|---|---|---|
| Shulker (meat) | Shulker Mollusk (1.2) | 8, 9.6 / 32, 38% | 8, 9.6 / 12, 100% (own meal (2, 1) x 4, still meat) | no (exactly 8) |
| Pufferfish (hazard) | Pufferfish (1.2) | 8, 9.6 / 32, 38% | 8, 9.6 / 24, 50% (Fish Meal) | yes |
| Tropical Fish | Tropical Fish (1.2) | 8, 9.6 / 32, 38% | 8, 9.6 / 24, 50% | yes |
| Raw Crab | Raw Crab (1.6) | 8, 12.8 / 32, 50% | 8, 12.8 / 24, 67% | yes |
| Raw Cod | Cod (2.4) | 8, 19.2 / 32, 75% | 8, 19.2 / 24, 100% | no (exactly 8) |
| Raw Salmon | Salmon (2.4) | 8, 19.2 / 32, 75% | 8, 19.2 / 24, 100% | no (exactly 8) |
| Raw Calamari | Calamari (2.8) | 8, 22.4 / 32, 88% | 7, 19.6 / 24, 102% | no |
| Raw Perch | Perch (2.8) | 8, 22.4 / 32, 88% | 7, 19.6 / 24, 102% | no |

Exactly 8 on the Meat Meal: Raw Chicken, Raw Mutton (3.2 each). Open: Pufferfish, Tropical Fish (50%) and Raw Crab
(67%) still fall short at 8. Every can's breakdown: [4.3](#value-vs-goal).

### 4.3 Hunger and saturation audit, every can <a id="value-vs-goal"></a>

<!-- BEGIN CAN AUDIT (tools/can_audit.py, do not edit) -->

Generated by `tools/can_audit.py` from can_types.json (what is built) and the food export. Totals are hunger / saturation points for the whole can; percents are per component, 100% = equal. **In -> out** = output / input (above 100% canning adds food), **out vs target** = output / the family goal (servings x the section meal, or the source x the section multiplier). The additive column uses the cheapest additive (salt and sugar count 0). Feasts and cake: the block's servings x the serving. `n/a`: no recipe (values-only Canned Goods cans), not a food (Canned Dough), or no value source (Powdered Milk).

| Section | Cans | In -> out (basic) | In -> out (additive) | Out vs target |
|---|---:|---|---|---|
| raw_veggie | 20 | h 83 to 167%, s 83 to 750% | h 111 to 333%, s 167 to 1000% | h 100%, s 100% |
| raw_fruit | 34 | h 83 to 188%, s 94 to 1000% | h 125 to 250%, s 125 to 1499% | h 100 to 140%, s 100 to 167% |
| raw_meat | 6 | h 100 to 133%, s 125 to 250% | h 133 to 167%, s 167 to 333% | h 40 to 100%, s 33 to 100% |
| raw_fish | 12 | h 100 to 200%, s 83 to 500% | h 133 to 267%, s 111 to 667% | h 100%, s 100% |
| hazard | 5 | h 100 to 200%, s 75 to 500% | n/a | h 40 to 100%, s 33 to 100% |
| special | 2 | h 125%, s 125% | h 188%, s 188% | h 100%, s 100% |
| cookies | 13 | h 125%, s 125% | h 167%, s 167% | h 100%, s 100% |
| cooked_veggie | 26 | h 133%, s 133% | h 160%, s 160% | h 100%, s 100% |
| cooked_meat | 16 | h 120%, s 120% | h 150%, s 150% | h 100%, s 100% |
| soup | 38 | h 133 to 178%, s 167% | h 267 to 356%, s 333 to 334% | h 80 to 107%, s 100% |
| bread | 12 | h 133%, s 133% | h 155%, s 246% | h 100%, s 100% |
| meal | 21 | h 143 to 160%, s 150% | n/a | h 95 to 107%, s 100% |
| pie | 14 | h 100%, s 100% | n/a | h 100%, s 100% |
| feast | 9 | h 55 to 150%, s 55 to 150% | n/a | h 100%, s 100% |
| drink | 2 | h 100%, s 100% | n/a | h 100%, s 100% |
| dairy | 3 | h 125%, s 125% | n/a | h 100%, s 100% |
| preserve | 2 | h 100%, s 100% | n/a | h 100%, s 100% |
| dessert | 1 | h 167%, s 167% | h 333%, s 333% | h 100%, s 100% |
| cake | 1 | h 114%, s 101% | h 200%, s 176% | h 114%, s 101% |
| golden | 55 | h 71 to 111%, s 111 to 124% | n/a | h 100 to 133%, s 100% |
| salad | 14 | h 160 to 178%, s 167% | h 185 to 206%, s 196 to 199% | h 96 to 107%, s 100% |

**raw_veggie**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_beetroot | 8 beetroot | 8 / 9.6 | 10 / 12 | 125% / 125% | 6 / 7.2 | 167% / 167% | 10 / 12 | 100% / 100% |
| canned_cabbage | 5 any_cabbage | 10 / 8 | 10 / 12 | 100% / 150% | 6 / 4.8 | 167% / 250% | 10 / 12 | 100% / 100% |
| canned_carrot | 3 carrot | 9 / 10.8 | 10 / 12 | 111% / 111% | 6 / 7.2 | 167% / 167% | 10 / 12 | 100% / 100% |
| canned_onion | 5 onion | 10 / 8 | 10 / 12 | 100% / 150% | 6 / 4.8 | 167% / 250% | 10 / 12 | 100% / 100% |
| canned_potato | 8 any_potato | 8 / 4.8 | 10 / 12 | 125% / 250% | 6 / 3.6 | 167% / 333% | 10 / 12 | 100% / 100% |
| canned_tomato | 8 tomato | 8 / 4.8 | 10 / 12 | 125% / 250% | 6 / 3.6 | 167% / 333% | 10 / 12 | 100% / 100% |
| canned_bell_pepper | 8 any_bell_pepper | 8 / 1.6 | 10 / 12 | 125% / 750% | 6 / 1.2 | 167% / 1000% | 10 / 12 | 100% / 100% |
| canned_cactus_flesh | 4 cactus_flesh | 12 / 7.2 | 10 / 12 | 83% / 167% | 9 / 5.4 | 111% / 222% | 10 / 12 | 100% / 100% |
| canned_gongylidia | 2 gongylidia | 6 / 14.4 | 10 / 12 | 167% / 83% | 3 / 7.2 | 333% / 167% | 10 / 12 | 100% / 100% |
| canned_corn | 6 corn | 12 / 4.8 | 10 / 12 | 83% / 250% | 8 / 3.2 | 125% / 375% | 10 / 12 | 100% / 100% |
| canned_broccoli | 5 broccoli | 10 / 8 | 10 / 12 | 100% / 150% | 6 / 4.8 | 167% / 250% | 10 / 12 | 100% / 100% |
| canned_cauliflower | 2 cauliflower/cauliflower_floret | 8 / 6.4 | 10 / 12 | 125% / 188% | 4 / 3.2 | 250% / 375% | 10 / 12 | 100% / 100% |
| canned_sweet_potato | 5 sweet_potato | 10 / 8 | 10 / 12 | 100% / 150% | 6 / 4.8 | 167% / 250% | 10 / 12 | 100% / 100% |
| canned_turnip | 5 turnip | 10 / 8 | 10 / 12 | 100% / 150% | 6 / 4.8 | 167% / 250% | 10 / 12 | 100% / 100% |
| canned_zucchini | 3 zucchini/zucchini_slice | 9 / 7.2 | 10 / 12 | 111% / 167% | 6 / 4.8 | 167% / 250% | 10 / 12 | 100% / 100% |
| canned_cucumber | 6 cucumber | 12 / 4.8 | 10 / 12 | 83% / 250% | 8 / 3.2 | 125% / 375% | 10 / 12 | 100% / 100% |
| canned_ghost_pepper | 4 ghost_pepper | 12 / 4.8 | 10 / 12 | 83% / 250% | 9 / 3.6 | 111% / 333% | 10 / 12 | 100% / 100% |
| canned_truffle | 1 truffle/truffle_slice | 6 / 7.2 | 10 / 12 | 167% / 167% | 6 / 7.2 | 167% / 167% | 10 / 12 | 100% / 100% |
| canned_beets | no recipe | n/a | 10 / 12 | n/a | - | - | 10 / 12 | 100% / 100% |
| canned_potatos | no recipe | n/a | 10 / 12 | n/a | - | - | 10 / 12 | 100% / 100% |

**raw_fruit**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_apple | 3 apple | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |
| canned_banana | 7 neapolitan_banana | 14 / 5.6 | 15 / 9 | 107% / 161% | 10 / 4 | 150% / 225% | 15 / 9 | 100% / 100% |
| canned_cantaloupe | 4 cantaloupe_slice | 12 / 7.2 | 15 / 9 | 125% / 125% | 9 / 5.4 | 167% / 167% | 15 / 9 | 100% / 100% |
| canned_cherry | 6 cherry | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |
| canned_glow_berry | 8 glow_berries | 16 / 3.2 | 15 / 9 | 94% / 281% | 12 / 2.4 | 125% / 375% | 15 / 9 | 100% / 100% |
| canned_melon | 6 melon | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |
| canned_mulberry | 5 mulberry | 15 / 3 | 15 / 9 | 100% / 300% | 9 / 1.8 | 167% / 500% | 15 / 9 | 100% / 100% |
| canned_pumpkin | 4 pumpkin_slice | 12 / 7.2 | 15 / 9 | 125% / 125% | 9 / 5.4 | 167% / 167% | 15 / 9 | 100% / 100% |
| canned_salmonberry | 4 salmonberries | 8 / 9.6 | 15 / 9 | 188% / 94% | 6 / 7.2 | 250% / 125% | 15 / 9 | 100% / 100% |
| canned_strawberry | 6 strawberries | 18 / 1.8 | 15 / 9 | 83% / 500% | 12 / 1.2 | 125% / 750% | 15 / 9 | 100% / 100% |
| canned_white_strawberry | 3 white_strawberries | 15 / 1.5 | 21 / 15 | 140% / 1000% | 10 / 1 | 210% / 1499% | 15 / 9 | 140% / 167% |
| canned_sweet_berry | 8 sweet_berries | 16 / 3.2 | 15 / 9 | 94% / 281% | 12 / 2.4 | 125% / 375% | 15 / 9 | 100% / 100% |
| canned_bayberry | 6 bayberry | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |
| canned_blueberry | 6 blueberry | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |
| canned_cranberry | 6 cranberry | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |
| canned_durian | 3 durian_flesh | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |
| canned_fig | 4 fig | 12 / 7.2 | 15 / 9 | 125% / 125% | 9 / 5.4 | 167% / 167% | 15 / 9 | 100% / 100% |
| canned_hamimelon | 6 hamimelon_slice | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |
| canned_hawberry | 6 hawberry | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |
| canned_kiwi | 4 kiwi | 12 / 7.2 | 15 / 9 | 125% / 125% | 9 / 5.4 | 167% / 167% | 15 / 9 | 100% / 100% |
| canned_lemon | 3 lemon/lemon_slice | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |
| canned_lychee | 6 lychee | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |
| canned_mango | 4 mango | 12 / 7.2 | 15 / 9 | 125% / 125% | 9 / 5.4 | 167% / 167% | 15 / 9 | 100% / 100% |
| canned_mangosteen | 4 mangosteen | 12 / 7.2 | 15 / 9 | 125% / 125% | 9 / 5.4 | 167% / 167% | 15 / 9 | 100% / 100% |
| canned_orange | 4 orange/orange_slice | 12 / 7.2 | 15 / 9 | 125% / 125% | 9 / 5.4 | 167% / 167% | 15 / 9 | 100% / 100% |
| canned_peach | 4 peach | 12 / 7.2 | 15 / 9 | 125% / 125% | 9 / 5.4 | 167% / 167% | 15 / 9 | 100% / 100% |
| canned_pear | 4 pear | 12 / 7.2 | 15 / 9 | 125% / 125% | 9 / 5.4 | 167% / 167% | 15 / 9 | 100% / 100% |
| canned_persimmon | 4 persimmon | 12 / 7.2 | 15 / 9 | 125% / 125% | 9 / 5.4 | 167% / 167% | 15 / 9 | 100% / 100% |
| canned_pineapple | 6 pineapple_slice | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |
| canned_coffee_berries | 5 coffee_berries | 10 / 8 | 15 / 9 | 150% / 112% | 6 / 4.8 | 250% / 187% | 15 / 9 | 100% / 100% |
| canned_apples | no recipe | n/a | 15 / 9 | n/a | - | - | 15 / 9 | 100% / 100% |
| canned_glow_berries | no recipe | n/a | 15 / 9 | n/a | - | - | 15 / 9 | 100% / 100% |
| canned_melon_slice | no recipe | n/a | 15 / 9 | n/a | - | - | 15 / 9 | 100% / 100% |
| canned_sweet_berries | no recipe | n/a | 15 / 9 | n/a | - | - | 15 / 9 | 100% / 100% |

**raw_meat**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_raw_beef | 5 raw_beef | 15 / 9 | 20 / 12 | 133% / 133% | 12 / 7.2 | 167% / 167% | 20 / 12 | 100% / 100% |
| canned_raw_chicken | 8 raw_chicken | 16 / 9.6 | 20 / 12 | 125% / 125% | 12 / 7.2 | 167% / 167% | 20 / 12 | 100% / 100% |
| canned_raw_mutton | 8 raw_mutton | 16 / 9.6 | 20 / 12 | 125% / 125% | 12 / 7.2 | 167% / 167% | 20 / 12 | 100% / 100% |
| canned_raw_pork | 5 raw_pork | 15 / 9 | 20 / 12 | 133% / 133% | 12 / 7.2 | 167% / 167% | 20 / 12 | 100% / 100% |
| canned_raw_rabbit | 5 rabbit | 15 / 9 | 20 / 12 | 133% / 133% | 12 / 7.2 | 167% / 167% | 20 / 12 | 100% / 100% |
| canned_shulker | 8 shulker_mollusk/shulker_filet | 8 / 1.6 | 8 / 4 | 100% / 250% | 6 / 1.2 | 133% / 333% | 20 / 12 | 40% / 33% |

**raw_fish**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_blobfish | 4 blobfish | 12 / 9.6 | 16 / 8 | 133% / 83% | 9 / 7.2 | 178% / 111% | 16 / 8 | 100% / 100% |
| canned_flying_fish | 4 flying_fish | 12 / 9.6 | 16 / 8 | 133% / 83% | 9 / 7.2 | 178% / 111% | 16 / 8 | 100% / 100% |
| canned_raw_lobster | 5 lobster_tail | 10 / 8 | 16 / 8 | 160% / 100% | 8 / 6.4 | 200% / 125% | 16 / 8 | 100% / 100% |
| canned_raw_calamari | 7 calamari/calamari_slice | 14 / 5.6 | 16 / 8 | 114% / 143% | 12 / 4.8 | 133% / 167% | 16 / 8 | 100% / 100% |
| canned_raw_catfish | 6 catfish/raw_catfish_slice | 12 / 7.2 | 16 / 8 | 133% / 111% | 10 / 6 | 160% / 133% | 16 / 8 | 100% / 100% |
| canned_raw_cod | 8 cod/cod_slice | 16 / 3.2 | 16 / 8 | 100% / 250% | 12 / 2.4 | 133% / 333% | 16 / 8 | 100% / 100% |
| canned_raw_crab | 8 raw_crab | 8 / 4.8 | 16 / 8 | 200% / 167% | 6 / 3.6 | 267% / 222% | 16 / 8 | 100% / 100% |
| canned_raw_lionfish | 6 lionfish | 12 / 7.2 | 16 / 8 | 133% / 111% | 10 / 6 | 160% / 133% | 16 / 8 | 100% / 100% |
| canned_raw_perch | 7 perch/perch_slice | 14 / 5.6 | 16 / 8 | 114% / 143% | 12 / 4.8 | 133% / 167% | 16 / 8 | 100% / 100% |
| canned_raw_pike | 4 pike/pike_slice | 12 / 7.2 | 16 / 8 | 133% / 111% | 9 / 5.4 | 178% / 148% | 16 / 8 | 100% / 100% |
| canned_raw_salmon | 8 salmon/salmon_slice | 16 / 3.2 | 16 / 8 | 100% / 250% | 12 / 2.4 | 133% / 333% | 16 / 8 | 100% / 100% |
| canned_tropical_fish | 8 tropical_fish | 8 / 1.6 | 16 / 8 | 200% / 500% | 6 / 1.2 | 267% / 667% | 16 / 8 | 100% / 100% |

**hazard**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_pufferfishs | 8 pufferfish | 8 / 1.6 | 16 / 8 | 200% / 500% | - | - | 20 / 12 | 80% / 67% |
| canned_posionous_potatos | 8 poisonous_potato | 16 / 9.6 | 20 / 12 | 125% / 125% | - | - | 20 / 12 | 100% / 100% |
| canned_spider_eyes | 5 spider_eye | 10 / 16 | 20 / 12 | 200% / 75% | - | - | 20 / 12 | 100% / 100% |
| canned_rotten_flesh | 5 rotten_flesh | 20 / 4 | 20 / 12 | 100% / 300% | - | - | 20 / 12 | 100% / 100% |
| canned_maggots | 7 maggot | 7 / 2.8 | 8 / 4 | 114% / 143% | - | - | 20 / 12 | 40% / 33% |

**special**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_chorus_fruits | 3 chorus_fruit | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |
| canned_ancient_fruit | 3 ancient_fruit | 12 / 7.2 | 15 / 9 | 125% / 125% | 8 / 4.8 | 188% / 188% | 15 / 9 | 100% / 100% |

**cookies**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_cookies | 8 cookie | 16 / 9.6 | 20 / 12 | 125% / 125% | 12 / 7.2 | 167% / 167% | 20 / 12 | 100% / 100% |
| canned_bayberry_cookie | 8 bayberry_cookie | 8 / 4.8 | 10 / 6 | 125% / 125% | 6 / 3.6 | 167% / 167% | 10 / 6 | 100% / 100% |
| canned_cherry_cookie | 8 cherry_cookie | 16 / 3.2 | 20 / 4 | 125% / 125% | 12 / 2.4 | 167% / 167% | 20 / 4 | 100% / 100% |
| canned_cranberry_cookie | 8 cranberry_cookie | 8 / 4.8 | 10 / 6 | 125% / 125% | 6 / 3.6 | 167% / 167% | 10 / 6 | 100% / 100% |
| canned_green_tea_cookie | 8 green_tea_cookie | 16 / 3.2 | 20 / 4 | 125% / 125% | 12 / 2.4 | 167% / 167% | 20 / 4 | 100% / 100% |
| canned_honey_cookie | 8 honey_cookie | 16 / 3.2 | 20 / 4 | 125% / 125% | 12 / 2.4 | 167% / 167% | 20 / 4 | 100% / 100% |
| canned_lemon_cookie | 8 lemon_cookie | 8 / 4.8 | 10 / 6 | 125% / 125% | 6 / 3.6 | 167% / 167% | 10 / 6 | 100% / 100% |
| canned_maple_cookie | 8 maple_cookie | 16 / 3.2 | 20 / 4 | 125% / 125% | 12 / 2.4 | 167% / 167% | 20 / 4 | 100% / 100% |
| canned_mulberry_cookie | 8 mulberry_cookie | 16 / 3.2 | 20 / 4 | 125% / 125% | 12 / 2.4 | 167% / 167% | 20 / 4 | 100% / 100% |
| canned_persimmon_cookie | 8 persimmon_cookie | 8 / 4.8 | 10 / 6 | 125% / 125% | 6 / 3.6 | 167% / 167% | 10 / 6 | 100% / 100% |
| canned_sweet_berry_cookie | 8 sweet_berry_cookie | 16 / 3.2 | 20 / 4 | 125% / 125% | 12 / 2.4 | 167% / 167% | 20 / 4 | 100% / 100% |
| canned_uncanny_cookie | 8 uncanny_cookies | 40 / 48 | 50 / 60 | 125% / 125% | 30 / 36 | 167% / 167% | 50 / 60 | 100% / 100% |
| canned_oatmeal_cookie | 8 oatmeal_cookie | 16 / 9.6 | 20 / 12 | 125% / 125% | 12 / 7.2 | 167% / 167% | 20 / 12 | 100% / 100% |

**cooked_veggie**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_baked_potato | 6 baked_potato/baked_potato_slices | 30 / 36 | 40 / 48 | 133% / 133% | 25 / 30 | 160% / 160% | 40 / 48 | 100% / 100% |
| canned_cooked_cactus_steak | 6 cactus_steak | 24 / 24 | 32 / 32 | 133% / 133% | 20 / 20 | 160% / 160% | 32 / 32 | 100% / 100% |
| canned_cooked_rice | 6 cooked_rice | 36 / 28.8 | 48 / 38.4 | 133% / 133% | 30 / 24 | 160% / 160% | 48 / 38.4 | 100% / 100% |
| canned_dried_kelp | 6 dried_kelp | 6 / 3.6 | 8 / 4.8 | 133% / 133% | 5 / 3 | 160% / 160% | 8 / 4.8 | 100% / 100% |
| canned_roasted_bell_pepper | 6 roasted_bell_pepper_green/roasted_bell_pepper_red/roasted_bell_pepper_yellow | 30 / 48 | 40 / 64 | 133% / 133% | 25 / 40 | 160% / 160% | 40 / 64 | 100% / 100% |
| canned_baked_durian | 6 baked_durian | 24 / 14.4 | 32 / 19.2 | 133% / 133% | 20 / 12 | 160% / 160% | 32 / 19.2 | 100% / 100% |
| canned_baked_pear | 6 baked_pear | 24 / 14.4 | 32 / 19.2 | 133% / 133% | 20 / 12 | 160% / 160% | 32 / 19.2 | 100% / 100% |
| canned_boiled_blue_pickerelweed | 6 boiled_blue_pickerelweed | 12 / 0 | 16 / 0 | 133% / - | 10 / 0 | 160% / - | 16 / 0 | 100% / - |
| canned_boiled_purple_pickerelweed | 6 boiled_purple_pickerelweed | 18 / 0 | 24 / 0 | 133% / - | 15 / 0 | 160% / - | 24 / 0 | 100% / - |
| canned_boiled_corn | 6 boiled_corn | 36 / 14.4 | 48 / 19.2 | 133% / 133% | 30 / 12 | 160% / 160% | 48 / 19.2 | 100% / 100% |
| canned_dried_banana | 6 dried_banana | 24 / 14.4 | 32 / 19.2 | 133% / 133% | 20 / 12 | 160% / 160% | 32 / 19.2 | 100% / 100% |
| canned_dried_persimmon | 6 dried_persimmon | 24 / 14.4 | 32 / 19.2 | 133% / 133% | 20 / 12 | 160% / 160% | 32 / 19.2 | 100% / 100% |
| canned_kimchi | 6 kimchi/vintagedelight_kimchi | 12 / 14.4 | 16 / 19.2 | 133% / 133% | 10 / 12 | 160% / 160% | 16 / 19.2 | 100% / 100% |
| canned_pickled_pickles | 6 pickled_pickles/pickle | 24 / 14.4 | 32 / 19.2 | 133% / 133% | 20 / 12 | 160% / 160% | 32 / 19.2 | 100% / 100% |
| canned_roasted_adzuki_beans | 6 roasted_adzuki_beans | 18 / 14.4 | 24 / 19.2 | 133% / 133% | 15 / 12 | 160% / 160% | 24 / 19.2 | 100% / 100% |
| canned_chips | 6 chips | 30 / 36 | 40 / 48 | 133% / 133% | 25 / 30 | 160% / 160% | 40 / 48 | 100% / 100% |
| canned_grilled_corn | 6 grilled_corn | 36 / 14.4 | 48 / 19.2 | 133% / 133% | 30 / 12 | 160% / 160% | 48 / 19.2 | 100% / 100% |
| canned_tortilla_chip | 6 tortilla_chip | 6 / 1.2 | 8 / 1.6 | 133% / 133% | 5 / 1 | 160% / 160% | 8 / 1.6 | 100% / 100% |
| canned_baked_sweet_potato | 6 baked_sweet_potato | 24 / 24 | 32 / 32 | 133% / 133% | 20 / 20 | 160% / 160% | 32 / 32 | 100% / 100% |
| canned_roasted_zucchini | 6 roasted_zucchini | 24 / 19.2 | 32 / 25.6 | 133% / 133% | 20 / 16 | 160% / 160% | 32 / 25.6 | 100% / 100% |
| canned_roasted_cauliflower | 6 roasted_cauliflower_floret | 12 / 7.2 | 16 / 9.6 | 133% / 133% | 10 / 6 | 160% / 160% | 16 / 9.6 | 100% / 100% |
| canned_pickled_beetroot | 6 pickled_beetroot | 30 / 24 | 40 / 32 | 133% / 133% | 25 / 20 | 160% / 160% | 40 / 32 | 100% / 100% |
| canned_pickled_onion | 6 pickled_onion | 36 / 14.4 | 48 / 19.2 | 133% / 133% | 30 / 12 | 160% / 160% | 48 / 19.2 | 100% / 100% |
| canned_pickled_pepper | 6 pickled_pepper | 36 / 21.6 | 48 / 28.8 | 133% / 133% | 30 / 18 | 160% / 160% | 48 / 28.8 | 100% / 100% |
| canned_roasted_peanut | 6 roasted_peanut | 24 / 9.6 | 32 / 12.8 | 133% / 133% | 20 / 8 | 160% / 160% | 32 / 12.8 | 100% / 100% |
| canned_cooked_inky_cap | 6 cooked_cleaned_cap | 36 / 43.2 | 48 / 57.6 | 133% / 133% | 30 / 36 | 160% / 160% | 48 / 57.6 | 100% / 100% |

**cooked_meat**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_beef | 5 beef_cooked | 40 / 64 | 48 / 76.8 | 120% / 120% | 32 / 51.2 | 150% / 150% | 48 / 76.8 | 100% / 100% |
| canned_pork | 5 porkchop_cooked | 40 / 64 | 48 / 76.8 | 120% / 120% | 32 / 51.2 | 150% / 150% | 48 / 76.8 | 100% / 100% |
| canned_chicken | 5 chicken_cooked/chicken_cuts_cooked | 30 / 36 | 36 / 43.2 | 120% / 120% | 24 / 28.8 | 150% / 150% | 36 / 43.2 | 100% / 100% |
| canned_cooked_mutten | 5 mutton_cooked/cooked_mutton_chops | 30 / 48 | 36 / 57.6 | 120% / 120% | 24 / 38.4 | 150% / 150% | 36 / 57.6 | 100% / 100% |
| canned_cooked_rabbit | 5 rabbit_cooked | 25 / 30 | 30 / 36 | 120% / 120% | 20 / 24 | 150% / 150% | 30 / 36 | 100% / 100% |
| canned_glazed_porkchop | 5 glazed_porkchop | 45 / 72 | 54 / 86.4 | 120% / 120% | 36 / 57.6 | 150% / 150% | 54 / 86.4 | 100% / 100% |
| canned_jerky | 5 jerky | 15 / 21 | 18 / 25.2 | 120% / 120% | 12 / 16.8 | 150% / 150% | 18 / 25.2 | 100% / 100% |
| canned_maple_glazed_bacon | 5 maple_glazed_bacon | 30 / 48 | 36 / 57.6 | 120% / 120% | 24 / 38.4 | 150% / 150% | 36 / 57.6 | 100% / 100% |
| canned_fish | 5 cod_cooked/cod_slice_cooked | 25 / 30 | 30 / 36 | 120% / 120% | 20 / 24 | 150% / 150% | 30 / 36 | 100% / 100% |
| canned_cooked_salmon | 5 salmon_cooked/salmon_slice_cooked | 30 / 48 | 36 / 57.6 | 120% / 120% | 24 / 38.4 | 150% / 150% | 36 / 57.6 | 100% / 100% |
| canned_cooked_calamari | 5 cooked_calamari/cooked_calamari_slice | 25 / 30 | 30 / 36 | 120% / 120% | 20 / 24 | 150% / 150% | 30 / 36 | 100% / 100% |
| canned_cooked_catfish | 5 catfish_cooked_mob/catfish_slice_cooked | 25 / 25 | 30 / 30 | 120% / 120% | 20 / 20 | 150% / 150% | 30 / 30 | 100% / 100% |
| canned_cooked_perch | 5 cooked_perch/cooked_perch_slice | 25 / 35 | 30 / 42 | 120% / 120% | 20 / 28 | 150% / 150% | 30 / 42 | 100% / 100% |
| canned_cooked_pike | 5 cooked_pike/cooked_pike_slice | 40 / 64 | 48 / 76.8 | 120% / 120% | 32 / 51.2 | 150% / 150% | 48 / 76.8 | 100% / 100% |
| canned_cooked_lionfish | 5 cooked_lionfish | 30 / 42 | 36 / 50.4 | 120% / 120% | 24 / 33.6 | 150% / 150% | 36 / 50.4 | 100% / 100% |
| canned_surstromming | 5 surstromming | 45 / 76.5 | 54 / 91.8 | 120% / 120% | 36 / 61.2 | 150% / 150% | 54 / 91.8 | 100% / 100% |

**soup**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_rabbit_stew | 6 rabbit_stew | 60 / 72 | 104 / 120 | 173% / 167% | 30 / 36 | 347% / 333% | 100 / 120 | 104% / 100% |
| canned_mushrooms | 6 mushroom_stew | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 18 / 21.6 | 356% / 333% | 60 / 72 | 107% / 100% |
| canned_beetroot_soup | no recipe | n/a | 64 / 71.9 | n/a | - | - | 60 / 72 | 107% / 100% |
| canned_suspicious_stew | no recipe | n/a | 64 / 71.9 | n/a | - | - | 60 / 72 | 107% / 100% |
| canned_acacia_blossom_soup | 6 acacia_blossom_soup | 72 / 115.2 | 120 / 192 | 167% / 167% | 36 / 57.6 | 333% / 333% | 120 / 192 | 100% / 100% |
| canned_adzuki_stew | 6 adzuki_stew | 48 / 57.6 | 80 / 96 | 167% / 167% | 24 / 28.8 | 333% / 333% | 80 / 96 | 100% / 100% |
| canned_baked_cod_stew | 6 baked_cod_stew | 84 / 126 | 144 / 210 | 171% / 167% | 42 / 63 | 343% / 333% | 140 / 210 | 103% / 100% |
| canned_bayberry_soup | 6 bayberry_soup | 6 / 2.4 | 8 / 4 | 133% / 167% | 3 / 1.2 | 267% / 333% | 10 / 4 | 80% / 100% |
| canned_beef_stew | 6 beef_stew | 72 / 115.2 | 120 / 192 | 167% / 167% | 36 / 57.6 | 333% / 333% | 120 / 192 | 100% / 100% |
| canned_ce_beetroot_soup | 6 beetroot_soup | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 18 / 21.6 | 356% / 333% | 60 / 72 | 107% / 100% |
| canned_bell_pepper_soup | 6 bell_pepper_soup | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 18 / 21.6 | 356% / 333% | 60 / 72 | 107% / 100% |
| canned_blazing_chili | 6 blazing_chili | 60 / 48 | 104 / 80.1 | 173% / 167% | 30 / 24 | 347% / 334% | 100 / 80 | 104% / 100% |
| canned_bone_broth | 6 bone_broth | 48 / 67.2 | 80 / 112 | 167% / 167% | 24 / 33.6 | 333% / 333% | 80 / 112 | 100% / 100% |
| canned_broccoli_soup | 6 broccoli_soup | 60 / 84 | 104 / 140 | 173% / 167% | 30 / 42 | 347% / 333% | 100 / 140 | 104% / 100% |
| canned_cacciatore | 6 cacciatore | 84 / 126 | 144 / 210 | 171% / 167% | 42 / 63 | 343% / 333% | 140 / 210 | 103% / 100% |
| canned_carrot_soup | 6 carrot_soup | 72 / 115.2 | 120 / 192 | 167% / 167% | 36 / 57.6 | 333% / 333% | 120 / 192 | 100% / 100% |
| canned_cauliflower_soup | 6 cauliflower_soup | 60 / 84 | 104 / 140 | 173% / 167% | 30 / 42 | 347% / 333% | 100 / 140 | 104% / 100% |
| canned_chicken_soup | 6 chicken_soup | 84 / 126 | 144 / 210 | 171% / 167% | 42 / 63 | 343% / 333% | 140 / 210 | 103% / 100% |
| canned_chorus_stew | 6 chorus_stew | 24 / 24 | 40 / 40 | 167% / 167% | 12 / 12 | 333% / 333% | 40 / 40 | 100% / 100% |
| canned_corn_soup | 6 corn_soup | 60 / 108 | 104 / 179.9 | 173% / 167% | 30 / 54 | 347% / 333% | 100 / 180 | 104% / 100% |
| canned_creamy_morel_soup | 6 creamy_morel_soup | 60 / 84 | 104 / 140 | 173% / 167% | 30 / 42 | 347% / 333% | 100 / 140 | 104% / 100% |
| canned_creamy_onion_soup | 6 creamy_onion_soup | 72 / 115.2 | 120 / 192 | 167% / 167% | 36 / 57.6 | 333% / 333% | 120 / 192 | 100% / 100% |
| canned_endermite_stew | 6 endermite_stew | 24 / 19.2 | 40 / 32 | 167% / 167% | 12 / 9.6 | 333% / 333% | 40 / 32 | 100% / 100% |
| canned_fig_chicken_stew | 6 fig_chicken_stew | 72 / 115.2 | 120 / 192 | 167% / 167% | 36 / 57.6 | 333% / 333% | 120 / 192 | 100% / 100% |
| canned_fish_stew | 6 fish_stew | 72 / 115.2 | 120 / 192 | 167% / 167% | 36 / 57.6 | 333% / 333% | 120 / 192 | 100% / 100% |
| canned_garlic_chicken_stew | 6 garlic_chicken_stew | 72 / 115.2 | 120 / 192 | 167% / 167% | 36 / 57.6 | 333% / 333% | 120 / 192 | 100% / 100% |
| canned_ghostly_chili | 6 ghostly_chili | 72 / 115.2 | 120 / 192 | 167% / 167% | 36 / 57.6 | 333% / 333% | 120 / 192 | 100% / 100% |
| canned_kangaroo_stew | 6 kangaroo_stew | 60 / 96 | 104 / 160 | 173% / 167% | 30 / 48 | 347% / 333% | 100 / 160 | 104% / 100% |
| canned_miso_with_bamboo_sprouts | 6 miso_with_bamboo_sprouts | 60 / 84 | 104 / 140 | 173% / 167% | 30 / 42 | 347% / 333% | 100 / 140 | 104% / 100% |
| canned_noodle_soup | 6 noodle_soup | 84 / 126 | 144 / 210 | 171% / 167% | 42 / 63 | 343% / 333% | 140 / 210 | 103% / 100% |
| canned_oatmeal | 6 oatmeal | 48 / 72 | 80 / 120 | 167% / 167% | 24 / 36 | 333% / 333% | 80 / 120 | 100% / 100% |
| canned_pickle_soup | 6 pickle_soup | 78 / 132.6 | 128 / 220.9 | 164% / 167% | 39 / 66.3 | 328% / 333% | 130 / 221 | 98% / 100% |
| canned_pumpkin_soup | 6 pumpkin_soup | 84 / 126 | 144 / 210 | 171% / 167% | 42 / 63 | 343% / 333% | 140 / 210 | 103% / 100% |
| canned_sinigang | 6 sinigang | 90 / 162 | 152 / 270 | 169% / 167% | 45 / 81 | 338% / 333% | 150 / 270 | 101% / 100% |
| canned_sopa_de_macaco | 6 sopa_de_macaco | 30 / 24 | 48 / 40 | 160% / 167% | 15 / 12 | 320% / 334% | 50 / 40 | 96% / 100% |
| canned_strider_moss_stew | 6 strider_moss_stew | 48 / 57.6 | 80 / 96 | 167% / 167% | 24 / 28.8 | 333% / 333% | 80 / 96 | 100% / 100% |
| canned_turnip_beef_stew | 6 turnip_beef_stew | 84 / 126 | 144 / 210 | 171% / 167% | 42 / 63 | 343% / 333% | 140 / 210 | 103% / 100% |
| canned_vegetable_soup | 6 vegetable_soup | 72 / 115.2 | 120 / 192 | 167% / 167% | 36 / 57.6 | 333% / 333% | 120 / 192 | 100% / 100% |

**bread**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_bread | 6 minecraft_bread | 30 / 36 | 40 / 48 | 133% / 133% | - | - | 40 / 48 | 100% / 100% |
| canned_sweet_roll | 6 sweet_roll | 36 / 57.6 | 48 / 76.8 | 133% / 133% | - | - | 48 / 76.8 | 100% / 100% |
| canned_adzuki_bun | 6 adzuki_bun | 30 / 24 | 40 / 32 | 133% / 133% | - | - | 40 / 32 | 100% / 100% |
| canned_banana_bread | 6 banana_bread | 30 / 36 | 40 / 48 | 133% / 133% | - | - | 40 / 48 | 100% / 100% |
| canned_cauliflower_bread | 6 cauliflower_bread | 36 / 43.2 | 48 / 57.6 | 133% / 133% | - | - | 48 / 57.6 | 100% / 100% |
| canned_cornbread | 6 cornbread | 24 / 24 | 32 / 32 | 133% / 133% | - | - | 32 / 32 | 100% / 100% |
| canned_garlic_bread | 6 garlic_bread | 30 / 24 | 40 / 32 | 133% / 133% | - | - | 40 / 32 | 100% / 100% |
| canned_honey_bread | 6 honey_bread | 36 / 57.6 | 48 / 76.8 | 133% / 133% | 31 / 31.2 | 155% / 246% | 48 / 76.8 | 100% / 100% |
| canned_mhadjeb | 6 mhadjeb | 72 / 115.2 | 96 / 153.6 | 133% / 133% | - | - | 96 / 153.6 | 100% / 100% |
| canned_mulberry_bread | 6 mulberry_bread | 54 / 43.2 | 72 / 57.6 | 133% / 133% | - | - | 72 / 57.6 | 100% / 100% |
| canned_nether_wart_sourdough | 6 nether_wart_sourdough | 24 / 9.6 | 32 / 12.8 | 133% / 133% | - | - | 32 / 12.8 | 100% / 100% |
| canned_sourdough_bread | 6 sourdough_bread | 42 / 84 | 56 / 112 | 133% / 133% | - | - | 56 / 112 | 100% / 100% |

**meal**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_bell_pepper_pasta | 2 bell_pepper_pasta | 24 / 38.4 | 36 / 57.6 | 150% / 150% | - | - | 36 / 57.6 | 100% / 100% |
| canned_cheesy_pasta | 2 cheesy_pasta | 28 / 42 | 44 / 63 | 157% / 150% | - | - | 42 / 63 | 105% / 100% |
| canned_crab_pasta | 2 crab_pasta | 22 / 26.4 | 32 / 39.6 | 145% / 150% | - | - | 33 / 39.6 | 97% / 100% |
| canned_kangaroo_pasta | 2 kangaroo_pasta | 20 / 32 | 32 / 48 | 160% / 150% | - | - | 30 / 48 | 107% / 100% |
| canned_lobster_pasta | 2 lobster_pasta | 20 / 32 | 32 / 48 | 160% / 150% | - | - | 30 / 48 | 107% / 100% |
| canned_duck_pasta | 2 duck_noodles | 24 / 38.4 | 36 / 57.6 | 150% / 150% | - | - | 36 / 57.6 | 100% / 100% |
| canned_pasta_with_meatballs | 2 pasta_with_meatballs | 24 / 38.4 | 36 / 57.6 | 150% / 150% | - | - | 36 / 57.6 | 100% / 100% |
| canned_pasta_with_mutton | 2 pasta_with_mutton | 24 / 38.4 | 36 / 57.6 | 150% / 150% | - | - | 36 / 57.6 | 100% / 100% |
| canned_pearl_pasta | 2 pearl_pasta | 8 / 6.4 | 12 / 9.6 | 150% / 150% | - | - | 12 / 9.6 | 100% / 100% |
| canned_squid_ink_pasta | 2 squid_ink_pasta | 28 / 42 | 44 / 63 | 157% / 150% | - | - | 42 / 63 | 105% / 100% |
| canned_vegetable_pasta | 2 vegetable_noodles | 28 / 42 | 44 / 63 | 157% / 150% | - | - | 42 / 63 | 105% / 100% |
| canned_creamy_chicken_pasta | 2 creamy_pasta_with_chicken_cuts | 24 / 38.4 | 36 / 57.6 | 150% / 150% | - | - | 36 / 57.6 | 100% / 100% |
| canned_creamy_ham_pasta | 2 creamy_pasta_with_ham | 24 / 38.4 | 36 / 57.6 | 150% / 150% | - | - | 36 / 57.6 | 100% / 100% |
| canned_cornbread_stuffing | 2 cornbread_stuffing | 24 / 48 | 36 / 72 | 150% / 150% | - | - | 36 / 72 | 100% / 100% |
| canned_creamed_corn | 2 creamed_corn | 14 / 14 | 20 / 21 | 143% / 150% | - | - | 21 / 21 | 95% / 100% |
| canned_dumplings | 2 dumplings | 16 / 25.6 | 24 / 38.4 | 150% / 150% | - | - | 24 / 38.4 | 100% / 100% |
| canned_pasta_with_broccoli | 2 pasta_with_broccoli | 24 / 38.4 | 36 / 57.6 | 150% / 150% | - | - | 36 / 57.6 | 100% / 100% |
| canned_potato_noodles | 2 potato_noodles | 24 / 38.4 | 36 / 57.6 | 150% / 150% | - | - | 36 / 57.6 | 100% / 100% |
| canned_cheese_pasta | 2 cheese_pasta | 18 / 27 | 28 / 40.5 | 156% / 150% | - | - | 27 / 40.5 | 104% / 100% |
| canned_pad_thai | 2 pad_thai | 24 / 36 | 36 / 54 | 150% / 150% | - | - | 36 / 54 | 100% / 100% |
| canned_truffle_pasta | 2 truffle_pasta | 24 / 38.4 | 36 / 57.6 | 150% / 150% | - | - | 36 / 57.6 | 100% / 100% |

**pie**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_apple_pie_filling | 4 apple_pie_slice | 12 / 7.2 | 12 / 7.2 | 100% / 100% | - | - | 12 / 7.2 | 100% / 100% |
| canned_chocolate_pie_filling | 4 chocolate_pie_slice | 12 / 7.2 | 12 / 7.2 | 100% / 100% | - | - | 12 / 7.2 | 100% / 100% |
| canned_chorus_pie_filling | 4 chorus_pie_slice | 12 / 7.2 | 12 / 7.2 | 100% / 100% | - | - | 12 / 7.2 | 100% / 100% |
| canned_mulberry_pie_filling | 4 mulberry_pie_slice | 12 / 7.2 | 12 / 7.2 | 100% / 100% | - | - | 12 / 7.2 | 100% / 100% |
| canned_pumpkin_pie_filling | 4 delightful_pumpkin_pie_slice/pumpkin_pie_slice | 12 / 7.2 | 12 / 7.2 | 100% / 100% | - | - | 12 / 7.2 | 100% / 100% |
| canned_rose_hip_pie_filling | 4 rose_hip_pie_slice | 12 / 7.2 | 12 / 7.2 | 100% / 100% | - | - | 12 / 7.2 | 100% / 100% |
| canned_salmonberry_pie_filling | 4 salmonberry_pie_slice | 12 / 7.2 | 12 / 7.2 | 100% / 100% | - | - | 12 / 7.2 | 100% / 100% |
| canned_sweet_berry_pie_filling | 4 sweet_berry_cheesecake_slice | 12 / 7.2 | 12 / 7.2 | 100% / 100% | - | - | 12 / 7.2 | 100% / 100% |
| canned_durian_pie_filling | 4 durian_pie | 20 / 16 | 20 / 16 | 100% / 100% | - | - | 20 / 16 | 100% / 100% |
| canned_pineapple_pie_filling | 4 pineapple_pie | 20 / 16 | 20 / 16 | 100% / 100% | - | - | 20 / 16 | 100% / 100% |
| canned_sweet_potato_pie_filling | 4 sweet_potato_pie_slice | 20 / 12 | 20 / 12 | 100% / 100% | - | - | 20 / 12 | 100% / 100% |
| canned_fig_pie_filling | 4 fig_tart | 20 / 16 | 20 / 16 | 100% / 100% | - | - | 20 / 16 | 100% / 100% |
| canned_lemon_pie_filling | 4 lemon_tart | 20 / 16 | 20 / 16 | 100% / 100% | - | - | 20 / 16 | 100% / 100% |
| canned_carrots | no recipe | n/a | 12 / 7.2 | n/a | - | - | 12 / 7.2 | 100% / 100% |

**feast**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_caramel_popcorn | 1 popcorn_box | 20 / 24 | 30 / 36 | 150% / 150% | - | - | 30 / 36 | 100% / 100% |
| canned_hoglin_feast | 1 stuffed_hoglin | 110 / 165 | 60 / 90 | 55% / 55% | - | - | 60 / 90 | 100% / 100% |
| canned_honey_glazed_ham | 1 honey_glazed_ham_block | 56 / 84 | 84 / 126 | 150% / 150% | - | - | 84 / 126 | 100% / 100% |
| canned_nachos | 1 nachos_block | 48 / 96 | 72 / 144 | 150% / 150% | - | - | 72 / 144 | 100% / 100% |
| canned_pineapple_fried_rice | 1 pineapple_fried_rice | 56 / 89.6 | 84 / 134.4 | 150% / 150% | - | - | 84 / 134.4 | 100% / 100% |
| canned_roast_chicken | 1 roast_chicken_block | 56 / 84 | 84 / 126 | 150% / 150% | - | - | 84 / 126 | 100% / 100% |
| canned_shepherds_pie | 1 shepherds_pie_block | 56 / 84 | 84 / 126 | 150% / 150% | - | - | 84 / 126 | 100% / 100% |
| canned_stuffed_pumpkin | 1 stuffed_pumpkin_block | 56 / 84 | 84 / 126 | 150% / 150% | - | - | 84 / 126 | 100% / 100% |
| canned_stuffed_shulker | 1 stuffed_shulker | 28 / 50.4 | 42 / 75.6 | 150% / 150% | - | - | 42 / 75.6 | 100% / 100% |

**drink**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_honey_bottles | 4 honey_bottle | 24 / 4.8 | 24 / 4.8 | 100% / 100% | - | - | 24 / 4.8 | 100% / 100% |
| canned_powdered_milk | 4 minecraft_milk_bucket | n/a | 4 / 0.8 | n/a | - | - | n/a | n/a |

**dairy**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_flaxen_cheese | 4 flaxen_cheese_wedge | 16 / 32 | 20 / 40 | 125% / 125% | - | - | 20 / 40 | 100% / 100% |
| canned_scarlet_cheese | 4 scarlet_cheese_wedge | 16 / 32 | 20 / 40 | 125% / 125% | - | - | 20 / 40 | 100% / 100% |
| canned_cheese_slice | 4 cheese_slice | 16 / 25.6 | 20 / 32 | 125% / 125% | - | - | 20 / 32 | 100% / 100% |

**preserve**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_tomato_sauce | 4 tomato_sauce | 16 / 12.8 | 16 / 12.8 | 100% / 100% | - | - | 16 / 12.8 | 100% / 100% |
| canned_dough | 8 in_dough | n/a | n/a | n/a | - | - | n/a | n/a |

**dessert**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_pear_with_rock_sugar | 6 pear_with_rock_sugar | 48 / 57.6 | 80 / 96 | 167% / 167% | 24 / 28.8 | 333% / 333% | 80 / 96 | 100% / 100% |

**cake**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_cake | 1 cake | 14 / 2.8 | 16 / 2.8 | 114% / 101% | 8 / 1.6 | 200% / 176% | 14 / 2.8 | 114% / 101% |

**golden**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_pumpkin_pie | no recipe | n/a | 10 / 24 | n/a | - | - | 10 / 24 | 100% / 100% |
| canned_golden_carrot | no recipe | n/a | 10 / 24 | n/a | - | - | 10 / 24 | 100% / 100% |
| canned_golden_apples | no recipe | n/a | 10 / 24 | n/a | - | - | 10 / 24 | 100% / 100% |
| canned_enchanted_apples | no recipe | n/a | 20 / 320 | n/a | - | - | 20 / 320 | 100% / 100% |
| canned_glistering_melon_slice | 2 glistering_melon_slice + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_apple | 2 golden_apple + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_baked_potato | 2 golden_baked_potato + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_beetroot | 2 golden_beetroot + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_beetroot_soup | 2 golden_beetroot_soup + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_bread | 2 golden_bread + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_ce_golden_carrot | 2 golden_carrot + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_chorus_fruit | 2 golden_chorus_fruit + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_cooked_beef | 2 golden_cooked_beef + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_cooked_chicken | 2 golden_cooked_chicken + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_cooked_cod | 2 golden_cooked_cod + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_cooked_mutton | 2 golden_cooked_mutton + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_cooked_porkchop | 2 golden_cooked_porkchop + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_cooked_rabbit | 2 golden_cooked_rabbit + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_cooked_salmon | 2 golden_cooked_salmon + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_cookie | 2 golden_cookie + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_dried_kelp | 2 golden_dried_kelp + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_glow_berries | 2 golden_glow_berries + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_honey_bottle | 2 golden_honey_bottle + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_melon_slice | 2 golden_melon_slice + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_milk_bucket | 2 golden_milk_bucket + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_moss_clump | 2 golden_moss_clump + 1 honey_bottle | 14 / 10.8 | 10 / 12 | 71% / 111% | - | - | 10 / 12 | 100% / 100% |
| canned_golden_mushroom_stew | 2 golden_mushroom_stew + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_pumpkin_pie | 2 golden_pumpkin_pie + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_rabbit_stew | 2 golden_rabbit_stew + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_golden_sweet_berries | 2 golden_sweet_berries + 1 honey_bottle | 14 / 20.4 | 10 / 24 | 71% / 118% | - | - | 10 / 24 | 100% / 100% |
| canned_enchanted_golden_apple | 2 enchanted_golden_apple + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_baked_potato | 2 enchanted_golden_baked_potato + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_beetroot | 2 enchanted_golden_beetroot + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_beetroot_soup | 2 enchanted_golden_beetroot_soup + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_bread | 2 enchanted_golden_bread + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_carrot | 2 enchanted_golden_carrot + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_chorus_fruit | 2 enchanted_golden_chorus_fruit + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_cooked_beef | 2 enchanted_golden_cooked_beef + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_cooked_chicken | 2 enchanted_golden_cooked_chicken + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_cooked_cod | 2 enchanted_golden_cooked_cod + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_cooked_mutton | 2 enchanted_golden_cooked_mutton + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_cooked_porkchop | 2 enchanted_golden_cooked_porkchop + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_cooked_rabbit | 2 enchanted_golden_cooked_rabbit + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_cooked_salmon | 2 enchanted_golden_cooked_salmon + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_cookie | 2 enchanted_golden_cookie + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_dried_kelp | 2 enchanted_golden_dried_kelp + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_glow_berries | 2 enchanted_golden_glow_berries + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_honey_bottle | 2 enchanted_golden_honey_bottle + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_melon_slice | 2 enchanted_golden_melon_slice + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_milk_bucket | 2 enchanted_golden_milk_bucket + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_moss_clump | 2 enchanted_golden_moss_clump + 1 honey_bottle | 18 / 20.4 | 20 / 24 | 111% / 118% | - | - | 15 / 24 | 133% / 100% |
| canned_enchanted_golden_mushroom_stew | 2 enchanted_golden_mushroom_stew + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_pumpkin_pie | 2 enchanted_golden_pumpkin_pie + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_rabbit_stew | 2 enchanted_golden_rabbit_stew + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |
| canned_enchanted_golden_sweet_berries | 2 enchanted_golden_sweet_berries + 1 honey_bottle | 22 / 257.2 | 20 / 320 | 91% / 124% | - | - | 20 / 320 | 100% / 100% |

**salad**

| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |
|---|---|---|---|---|---|---|---|---|
| canned_bowl_of_exotic_fruits | 6 bowl_of_exotic_fruits | 66 / 66 | 112 / 110 | 170% / 167% | 56 / 55.4 | 200% / 199% | 110 / 110 | 102% / 100% |
| canned_broccoli_salad | 6 broccoli_salad | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 31 / 36.4 | 206% / 198% | 60 / 72 | 107% / 100% |
| canned_cesar_salad | 6 cesar_salad | 60 / 84 | 104 / 140 | 173% / 167% | 51 / 70.4 | 204% / 199% | 100 / 140 | 104% / 100% |
| canned_chicken_salad | 6 chicken_salad | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 31 / 36.4 | 206% / 198% | 60 / 72 | 107% / 100% |
| canned_coleslaw | 6 coleslaw | 48 / 57.6 | 80 / 96 | 167% / 167% | 41 / 48.4 | 195% / 198% | 80 / 96 | 100% / 100% |
| canned_cucumber_salad | 6 cucumber_salad | 48 / 67.2 | 80 / 112 | 167% / 167% | 41 / 56.4 | 195% / 199% | 80 / 112 | 100% / 100% |
| canned_field_salad | 6 field_salad | 60 / 66 | 104 / 110 | 173% / 167% | 51 / 55.4 | 204% / 199% | 100 / 110 | 104% / 100% |
| canned_fruit_salad | 6 fruit_salad | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 31 / 36.4 | 206% / 198% | 60 / 72 | 107% / 100% |
| canned_inky_caps_salad | 6 inky_caps_salad | 48 / 72 | 80 / 120 | 167% / 167% | 41 / 60.4 | 195% / 199% | 80 / 120 | 100% / 100% |
| canned_lush_moss_salad | 6 lush_moss_salad | 48 / 48 | 80 / 80 | 167% / 167% | 41 / 40.4 | 195% / 198% | 80 / 80 | 100% / 100% |
| canned_mango_salad | 6 mango_salad | 48 / 57.6 | 80 / 96 | 167% / 167% | 41 / 48.4 | 195% / 198% | 80 / 96 | 100% / 100% |
| canned_mixed_salad | 6 mixed_salad | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 31 / 36.4 | 206% / 198% | 60 / 72 | 107% / 100% |
| canned_nether_salad | 6 nether_salad | 30 / 24 | 48 / 40 | 160% / 167% | 26 / 20.4 | 185% / 196% | 50 / 40 | 96% / 100% |
| canned_turnip_salad | 6 turnip_salad | 36 / 43.2 | 64 / 71.9 | 178% / 167% | 31 / 36.4 | 206% / 198% | 60 / 72 | 107% / 100% |

<!-- END CAN AUDIT -->

## 5. File map

- [development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js](../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js): hand-written cans, generated can registration, Canned Goods food overrides
- [kubejs/data/creativedelight/kubejs/creativeengineering/canning.js](../kubejs/data/creativedelight/kubejs/creativeengineering/canning.js): recipes, section goals, plans for pie/feast/meal/drink/special
- [tools/gen_can_types.py](../tools/gen_can_types.py) -> [can_types.json](../kubejs/generation_scripts/food/can_types.json): generated cans
- [can_servings.json](../kubejs/generation_scripts/food/can_servings.json): servings per can
- [food_types.json](../kubejs/generation_scripts/food/food_types.json): food families, `canned` map (can -> sources)
- [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js): "canning" section
- [kubejs/client_scripts/namechanges.js](../kubejs/client_scripts/namechanges.js): Cactus Flesh rename
- [food_catalog.md](food_catalog.md): every food's current values (source for the h/s columns)

### 5.1 Can sprites <a id="sprites"></a>

Drawn in `textureswapper/canned/newcan` (label layers in `newlabel`, combined by `combine_tex.py --batch`), copied
into the pack by hand:

| Sprite | Goes to | File |
|---|---|---|
| apple1, banana1, beet1, bell_pepper1, cabbage1, cantalope1, carrot1, cherries1, glow_berries1, gongylidia1, melon1, mulberries1, onion1, potato1, pumpkin1, salmonberries1, strawberries1, sweet_berries1, tomato1 | the matching hand-written `creativeengineering:canned_*` can | `kubejs/assets/creativeengineering/textures/item/<id>.png`; cans.js leaves out `.texture()` for these |
| cooked_cod1, cooked_salmon1, mushroom1 | Canned Goods cans reused for Cooked Cod (`canned_fish`), Cooked Salmon, Mushroom Stew (`canned_mushrooms`) | `kubejs/assets/canned_goods/textures/items/<texture>.png` (overrides the mod's sprite) |
| chorus_fruit1, tropical_fish1, pufferfish1, posionous_potato1, night_berries1, tough_leather1 | Canned Goods' own cans with no recipe here (Chorus Fruit, Tropical Fish, Pufferfishs, Poisonous Potatos, Night Berries = `canned_spider_eyes`, Tough Jerky = `canned_rotten_flesh`) | same, `kubejs/assets/canned_goods/textures/items/` |
| cranberries1, raw_cod1, raw_salmon1, raw_beef, raw_mutton, raw_pork | `canned_cranberry`, `canned_raw_cod`, `canned_raw_salmon`, `canned_raw_beef`, `canned_raw_mutton`, `canned_raw_pork` | `kubejs/assets/creativeengineering/textures/item/<id>.png` |

Still to draw (scratchpad 2026-10-21, a priority): a sprite for every built can that borrows one ([TODO](todo/TODO.md) `textures/food #1`).

**Family sprites (2026-10-21):** a can without its own borrows its family's, set per section in `GROUP_SPRITE`
(gen_can_types.py): raw veggie -> carrot, raw fruit and special -> apple, salad -> cabbage, raw meat -> raw beef, raw
fish -> raw salmon (CE sprites); hazard -> tough jerky, cookies -> cookies, cooked veggie -> baked potato, cooked
meat -> steak, bread -> bread, soup -> rabbit stew, pie -> pumpkin pie, drink -> honey bottles, golden -> golden apples,
enchanted golden -> notch apples, dessert and cake -> cake, meal / feast / dairy / preserve -> Canned Food (Canned
Goods sprites). The generator checks for `<id>.png` and leaves `texture` null when the can has its own. No can points
at Canned Goods' beetroot can any more (it was the fallback until 2026-10-21).

## 6. Offline checks

- After a restart: `/dumpfood`, then `python3 tools/food_catalog.py --check`. Removed cans should be gone from the
  export and from the `canned` map; the new `canned_white_strawberry` and `canned_cactus_flesh` should be present.
- `logs/latest.log`: no `[canning] ... skipped` and no `[can servings] not item ids in item_dict` lines.
- `python3 tools/can_audit.py --check` prints the per-section ranges of [4.3](#value-vs-goal); without `--check` it
  rewrites the sheet's h/s and percent cells and 4.3. Run it after every `gen_can_types.py` run.
- 2026-10-13 dry run (node, stubbed NCH): canning.js makes 381 recipes, every input is an item_dict id, no duplicate
  recipe ids.

## 7. In-game tests

- [ ] **CP-01 removed cans gone:** JEI search "Canned Mint", "Canned Acorns", "Canned Grapes", "Canned Mite Crust",
  "Canned Raw Ham" (and chevon, hoglin, kangaroo, moose, bison, bunfungus) -> no results.
- [ ] **CP-02 white strawberry can:** JEI on White Strawberries -> Canned White Strawberries (3 berries, or 2 + sugar/honey);
  plain Strawberries -> Canned Strawberries only.
- [ ] **CP-03 cactus flesh:** the Delightful cactus piece reads "Cactus Flesh"; it cans into "Canned Cactus Flesh"
  (4, or 2 + salt/bone broth).
- [ ] **CP-04 servings:** eat one Canned White Strawberries and one Canned Cactus Flesh -> bar shows 2/3 and 1/2.
- [ ] ~~CP-05 cooked cuts still canned~~ superseded: their cooked cans were removed too (CP-07).
- [ ] **CP-06 can sprites:** JEI search "Canned" -> the 19 raw veggie/fruit cans, Canned Fish, Canned Cooked Salmon,
  Canned Mushrooms show their newcan sprites (no purple/black missing texture); Canned Cactus Flesh, White Strawberries,
  the raw meat cans and the generated cans still show the beetroot can. (Superseded by CP-08 for the group sprites.)
- [ ] **CP-07 removed cans:** JEI search "Canned Beef Patty", "Canned Cooked Bacon", "Canned Smoked Ham", "Canned Hoglin
  Sirloin", "Canned Cooked Kangaroo", "Canned Cooked Moose", "Canned Crab Leg", "Canned Toast", "Canned Tortilla",
  "Canned Bread Slice" -> no results. Alex's Mobs Banana has no canning recipe.
- [ ] **CP-08 sprites:** Canned Raw Beef / Mutton / Pork / Cod / Salmon and Canned Cranberries show their own sprites;
  Canned Raw Chicken and Raw Rabbit show the raw beef can, Canned Cactus Flesh the carrot can, Canned White
  Strawberries and the new fruit cans the apple can, Canned Raw Pike (etc.) the raw salmon can. No missing textures.
- [ ] **CP-09 sliced inputs (2026-10-20)** (values and recipes: scratch server PASSED 2026-10-22, SQ-OF-01, SQ-OF-02, no recipe makes a partly full can; JEI, eating: in game): 5 Minced Beef, 5 Raw Beef, or a mix -> a full Canned Raw Beef; 8 Raw Chicken
  Cuts -> a full Canned Raw Chicken (no bar); 3 Lemon Slices -> Canned Lemon; 8 Cod Slices -> a full Canned Raw Cod.
  JEI shows no recipe making a partly full can. 4 Cantaloupe Slices -> Canned Cantaloupe; a Cantaloupe Slice eats as
  3 hunger with no Resistance. Pumpkin: 3 slices + sugar -> Canned Pumpkin (3 servings).
- [ ] **CP-10 hazard cans:** craft Canned Pufferfishs (8 pufferfish) and eat a serving -> hunger, poison and nausea from
  Canned Goods; one Canned Tough Jerky serving -> hunger. Effects apply once per serving, not twice.
- [ ] **CP-11 pie filling:** 4 Apple Pie Slices -> Canned Apple Pie Filling; filling + Pie Crust -> Apple Pie. 4 Durian
  Pies -> Canned Durian Pie Filling (no pie recipe).
- [ ] **CP-12 feast:** a placed-feast item (Roast Chicken block item) + can -> Canned Roast Chicken (6 servings);
  Stuffed Hoglin -> Canned Hoglin Feast.
- [ ] **CP-13 drinks:** 4 Honey Bottles -> Canned Honey Bottles, 4 glass bottles back; 4 Milk Buckets -> Canned Powdered
  Milk, 4 buckets back; eating a serving with any effect active clears it.
- [ ] **CP-14 chorus:** Canned Chorus Fruit (3 chorus fruit) teleports on each serving; Canned Ancient Fruit uses the
  apple sprite.
- [ ] **CP-15 corn** (values and recipes: scratch server PASSED 2026-10-22, SQ-OF-02, SQ-OF-04; crafting: in game): 6 maize, or 4 + salt -> Canned Corn (2 servings); corn kernels make no can. A knife cuts 1 maize
  into 4 kernels.
- [ ] **CP-16 cheese** (values and recipes: scratch server PASSED 2026-10-22, SQ-OF-01; the pot run: in game): 4 Flaxen Cheese Wedges in a heated Cooking Pot with an empty can in the container slot -> Canned
  Flaxen Cheese (4 servings); same for Scarlet. No crafting-grid recipe.
- [ ] **CP-17 bowls** (values and recipes: scratch server PASSED 2026-10-22, SQ-OF-01; crafting: in game): 2 Creamed Corn + can -> can and 2 bowls back; 6 Pears with Rock Sugar -> 6 bowls back.
- [ ] **CP-18 coffee** (values and recipes: scratch server PASSED 2026-10-22, SQ-OF-02, wither 40 t at 0.8, caffeinated 200 t; eating: in game): a Canned Coffee Berries serving gives Caffeinated 10 s and, 80 % of the time, Wither for 2 s.
- [ ] **CP-19 new cans** (values and recipes: scratch server PASSED 2026-10-22, SQ-OF-00, SQ-OF-01, `[cans] registered 228 generated cans`, no `skipped` line; sprites, JEI: in game): every can of the 2026-10-21 build shows its family sprite (no purple/black) and its JEI
  recipe; startup log `[cans] registered 228 generated cans`; server log has no `[canning] ... skipped` line.
- [ ] **CP-20 migrated raw cans** (values and recipes: scratch server PASSED 2026-10-22, SQ-OF-01, SQ-OF-02; crafting, eating: in game): 8 beetroot -> Canned Beetroot, 6 + salt the same; 3 apples / 2 + sugar; 5 raw beef /
  4 + bone broth; 4 / 3 + salt Cactus Flesh (the rule; was 4 / 2 + salt). Values unchanged (5 hunger; 2 / 3 / 4 servings). A Canned Banana /
  Glow Berries / Salmonberries serving gives Agility 15 s / Glowing 15 s / Water Breathing 5 s; Canned Raw Chicken
  sometimes Hunger.
- [ ] **CP-21 Canned Goods values** (values and recipes: scratch server PASSED 2026-10-22, SQ-OF-01, SQ-OF-02, no can above modifier 10; AppleSkin: in game): AppleSkin on Canned Cake shows 4 hunger and about 0.7 saturation (was 28 / 112;
  saturation as shown = hunger x modifier x 2); Canned Apples 5 hunger, 3.0 saturation (3 servings); Canned Suspicious Stew 8 hunger (8 servings, Canned Goods' own
  effects still apply). No Canned Goods can shows more saturation than its family's formula gives. Canned Cake: 1 cake
  + can, or 4 slices + sugar -> 4 fast servings.
- [ ] **CP-22 removed** (values and recipes: scratch server PASSED 2026-10-22, SQ-OF-03, after the trade fix in 8, 0 of 240 level 2 butcher offers; JEI: in game): Canned Food and the Hot Hot Hotpot are hidden in JEI; a butcher (any level) never offers Canned
  Food; the butcher's other trades are still there.
- [ ] **CP-23 opened filling** (CQ-CNP-01): an opened Canned Apple Pie Filling (2 / 4) + Pie Crust gives no result; a full one gives
  an Apple Pie.
- [ ] **CP-24 manual only** (server half: SQ-CNP-01): JEI's Create "Shapeless Mixing" / "Automated Crafting" categories list no `*_pie_manual_only`
  recipe; filling + crust in a basin under a mixer, in a mechanical crafter, or in an IE Assembler makes nothing. In a
  crafting terminal with the player's inventory (AE2) the recipe works. Canning recipes (empty can + food) still run in
  the mixer.
- [ ] **CP-25 dough:** 8 Create dough + can -> Canned Dough (8 servings, not edible, servings bar after a craft uses one).
- [ ] **CP-26 sweet berry pie** (recipes and can values: scratch server PASSED 2026-10-22; names: in game): 4 Slice of Sweet Berry Pie + can -> Canned Sweet Berry Pie Filling (4 servings, 3 hunger,
  Speed 30 s); full can + pie crust -> Sweet Berry Pie by hand and in the oven (OV-32).
- [ ] **CP-27 Veggies raw cans:** 5 Broccoli + can -> Canned Broccoli; 2 Cauliflower or 2 Cauliflower Florets + can ->
  Canned Cauliflower; 3 Zucchini or Zucchini Slices -> Canned Zucchini; Sweet Potato and Turnip as broccoli. Each also
  has a `_salt` and a `_vinegar` recipe: 3 + additive (cauliflower 1 + additive, zucchini 2 + additive).
- [ ] **CP-28 Veggies cooked, bread, salads, soups, pastas:** 6 Baked Sweet Potato / Roasted Zucchini / Roasted
  Cauliflower Florets + can; Cauliflower Bread, Garlic Bread, Mhadjeb (6); the 4 salads (6, or 5 + vinegar); the 5 soups
  (6, or 3 + broth); Pasta with Broccoli and Potato Noodles (2). Values as can_parity 4.3.
- [ ] **CP-29 sweet potato pie filling:** 4 Sweet Potato Pie Slices + can -> Canned Sweet Potato Pie Filling; 2 Baked
  Sweet Potato + 2 sugar + can in the Cooking Pot -> the filling; full filling + Pie Crust -> Sweet Potato Pie by hand
  and in the oven.
- [ ] **CP-30 vinegar additive:** JEI on Vinegar Bottle shows a `_vinegar` can recipe for Canned Carrot, Canned Apple,
  Canned Raw Beef, Canned Raw Cod and Canned Mixed Salad. Since 2026-11-02 the Vinegar Bottle is merged into the Vinegar Jar: the recipes take the jar (scratch SQ-VNG-01, -02, deep scan 2026-11-02).
- [ ] **CP-31 Vintage cans (after `/dumpfood` with Vintage loaded, `gen_can_types.py`, restart):** the 16 Vintage cans
  exist; Vintage Kimchi fills Canned Kimchi, Pickle fills Canned Pickled Pickles, Salted Cod and Salted Salmon fill
  Canned Fish and Canned Cooked Salmon.

## 8. History

- 2026-10-25: Vintage Delight and Fungi Delight are in the 2026-10-24 dump, so `gen_can_types.py` built their 20
  configured cans (`AWAIT_DUMP` emptied; the item_dict keys and `canned` type entries exist, the items after a restart).
  Fungi rows added: Truffle (Truffle Slice as alt input) in 2.1, Cooked Inky Cap in 2.6 (fungi_delight.md step 8).
  Hostile, End's, Miner's, Ocean's and Oceanic Delight foods are held out of the type loops (`UNDECIDED_MODS`) until
  their cans are decided. `can_audit.py` rerun.
- 2026-10-23: Veggies Delight and Vintage Delight rows (116) and cans. Veggies' 24 cans generated from the 10-22 dump
  (the catalog was rerun first; it was still from 10-20). Vintage has never been in a dump, so its 16 cans (cucumber,
  ghost pepper, 3 pickles, roasted peanuts, Surströmming, cheese slices, oatmeal cookies, cheese pasta, pad thai,
  cucumber salad, pickle soup, ghostly chili, oatmeal) and its 4 extra sources of existing cans (kimchi, pickle, salted
  cod and salmon) are configured and skipped through `AWAIT_DUMP` in `gen_can_types.py`; the run lists them as
  waiting. The vinegar additive, configured since the Vintage port, ran for the first time (66 cans gained a
  `_vinegar` recipe). Not canned by precedent: garlic, dandelion leaf, raw oats, patties, eggs, jams and sauces,
  handhelds, drinks, sweets other than cookies, meals other than pastas.

- 2026-10-22 (second bulk run): SQ-OF-00 to SQ-OF-03 PASS on the scratch server (`[cans] registered 228`). The
  trade half of SQ-OF-03 failed first: butchers still sold Canned Food (24 of 240 level 2 offers). MoreJS 0.3.0 reads a
  level range `[1, 5]` as level 1 only (both ends from the first entry), so the `trades.js` Canned Food filter (and the
  cleric, chef and engineer removals) never reached levels 2 to 5; every range is a real
  `IntRange(1, 5)` now, 0 offers left. CP-09, CP-15 to CP-22 keep their sprite, JEI and eating halves.
- 2026-10-22: server-side checks queued as SQ-OF-00 to SQ-OF-03 ([testing/scratch_queue.md](testing/scratch_queue.md),
  `tools/older_food_probe.js`, `/ofprobe cans|canextra|canremoved`): every can of can_types.json against its food values,
  effects, servings and recipe ids; the TODO rows' named values (raw-can effects, coffee, pumpkin, cactus, Canned Cake);
  no partly full can, Canned Food or Hot Hot Hotpot recipe, no Canned Food butcher trade. Covers the server half of CP-09,
  CP-15 to CP-22; sprites, JEI and eating stay in game. CP-19 says 227 registered; can_types.json now has 228 non-existing cans (the probe prints the expected count).
- 2026-10-21 (audit): `tools/can_audit.py` fills Food / Craft sum / Canned h/s, Food audit and two new columns
  (In -> out, Out vs target) for every row with a can, and generates 4.3 for all 261 cans. Findings: golden cans lose
  hunger (71 to 111%: the required honey bottle counts as input), feasts run 55% (Stuffed Hoglin, 11 plates -> 6) to
  150% (4-serving feasts -> 6), additive soups about 3x, normal strawberries 83% hunger. White Strawberries got their
  numbers: own serving 7 / 5, 140% hunger (TODO `cans/canning #6`).
- 2026-10-21 (later): Canned Food (also its butcher trade) and the Hot Hot Hotpot removed (removed_items.js, trades.js);
  Cactus Flesh follows the sizing rule (4 / 3+1); a maize cuts into 4 kernels and kernels are not canned (row `/`);
  coffee berries keep their Wither at 2 s; Fig and Lemon Tart cans are pie fillings (`canned_fig_pie_filling`,
  `canned_lemon_pie_filling`); Canned Dough holds 8 Create dough, 8 servings, not edible (its uncan recipe dropped until
  dough crafts take it). No recipe makes a partial can (the `servings` output path is gone). NCH `cans` recipes are
  manual only: `_manual_only` ids (Create skips them) and no result on a grid without a player (IE Assembler, AE2
  assemblers); `{ whole }` cans must be full (filling + crust -> pie). Summary recounted.
- 2026-10-21: can crafts built: Canned Corn (kernels share Maize's slots), Coffee Berries (no Wither), Chips, Grilled
  Corn, Tortilla Chips, Cornbread Stuffing, Creamed Corn, both Creamy Pastas, Dumplings, Fig and Lemon Tart Filling,
  Maggots (own 2 / 1 meal), Flaxen and Scarlet Cheese (Cooking Pot, can in the container slot), Tomato Sauce, Dough
  (values of FD wheat dough; one serving crafts back into one dough), Pears in Rock Sugar, Sweet Rolls; new sections
  dairy, preserve, dessert, cake. The 26 hand-written raw cans are generated now (checked against the old recipes,
  values, servings, names, sprites: identical; Cactus Flesh keeps 4 / 2+1 through its own `adv`), and Banana, Glow
  Berries, Salmonberries and Raw Chicken now carry their effects. Canned Goods cans no recipe makes (apples, beets,
  potatos, glow berries, melon slice, sweet berries, "pumpkin pies" `canned_carrots`, the two golden carrot cans,
  golden and notch apples, `canned_beetroot_soup`, suspicious stew, Canned Food, Hot Hot Hotpot) take their family's
  values instead of the mod's saturation points. Family sprites replace the beetroot-can fallback. Summary recounted.
- 2026-10-13: written. Same day: removed 11 raw cans, split White Strawberries into its own can, renamed Canned Cactus
  to Canned Cactus Flesh (id `canned_cactus_flesh`) and the item Cactus Chunk to Cactus Flesh.
- 2026-10-13: newcan sprites deployed (5.1): 19 hand-written cans, 3 reused and 6 unused Canned Goods cans. Texture
  column checked against the sprites: added the bell pepper colour rows and the Alex's Mobs banana (they share a
  textured can) and the drawn-but-unbuilt raw cod, raw salmon, tropical fish, chorus fruit. Not checked in game (CP-06).
- 2026-10-13: every remaining `x` row built through gen_can_types.py (raw fruit, shulker, raw fish, hazard, special,
  cookies, cooked veggie/meat, pastas, pie, feast, drink), the 15 implemented `/` cans removed (cooked patties, bacon,
  bison, ham, chevon, hoglin, kangaroo, moose, bunfungus, lobster, crab; bread slice, toast, tortilla), Alex's Mobs
  banana dropped from the banana can, sliced alt inputs for raw meat and fish (partial cans where 8 is too few), group
  sprites, raw beef/mutton/pork/cod/salmon and cranberry sprites deployed. Six pie slices with no source skipped.
  Placeholder values listed in 3.1. Not run in game (CP-07 to CP-14).
- 2026-10-20: 4.2 family rule applied to every can with a sliced input (counts table, not built).
- 2026-10-20: Input column (basic / +additive counts) added to every family table.
- 2026-10-20: pufferfish takes the Fish Meal; shulker its own (2, 1) x 4 meal (full at 8 mollusks, still meat);
  4.3 value-vs-goal breakdown for every can.
- 2026-10-20: raw fish cans moved to a Fish Meal (4, 2): can worth 24; cod and salmon now reach the goal at 8,
  calamari and perch drop to 7. Shulker, pufferfish (Meat Meal), tropical fish, crab still capped.
- 2026-10-20: 4.2 built: every sliced-input can is defined by the whole food and the slice shares its slots and count;
  partial-can recipes removed; pumpkin moved to fruit (3 servings, 4 / 3 + sugar); cantaloupe slice copies the pumpkin
  slice's food values (food_values.js). Not run in game (CP-09).
- 2026-10-20: section 4 settled into decisions (pumpkin fruit, strawberry ratio, banana merge, sliced and whole meat
  inputs, no partial pie cans, golden cans 10 servings at 0.25x / 0.333x, own mod Creative Canning). No code changed.
- 2026-10-14: the 2026-10-13 can changes (removals, White Strawberries can, Cactus Flesh rename, generated rows, new
  sprites) ship in pack **1.0.6** (STAGED 2026-10-14, [plan_1.0.6.md](plan_1.0.6.md)); still not run in game (CP-01 to CP-05).
- 2026-10-20 (food audit, fresh `/dumpfood` from a scratch server: 1030 foods): renamed ids fixed (Scarlet Pierogi,
  Miss Lilitus Wine `vinery:lilitu_wine`), Vinery's duplicate Mead marked gone. New `?` rows: Beer, Vodka, Dread Nog
  (edible since the Brewin port), Gingerbread and Gingerbread Man (sweets), Fig and Lemon Tart (pie). New families from
  section 3: Hazard (the 4 built Canned Goods hazard cans plus 7 `?`), Egg and dairy (6), Preserves (27 jams, nut butter,
  tomato sauce), Desserts (37 jello, custard, pudding, ice cream), Cake (18). Summary recounted from the rows (it still
  showed the 10-13 `?` counts). Still outside: 96 sweets, 52 handhelds, 15 ingredients.
- 2026-10-20: the rest of section 3 pulled in, all `?` (for bulk decisions): Sweets (96), Handheld (52),
  Ingredients (15). Every typed food is now on a sheet. Summary recounted.
