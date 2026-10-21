# Can parity worksheet

Written 2026-10-13. Test prefix `CP-`. A hand-kept worksheet: one row per food in each canning family, so every food
that should have a can gets one, and every can gets a recipe, texture, value audit and effect check. Edit the cells
directly; nothing regenerates this file.

## 0. At a glance

| Part | Status |
|---|---|
| Raw veggie / fruit / meat cans (hand-written, [cans.js](../kubejs/startup_scripts/items/cans.js)) | 2026-10-13: removed mint, acorn, grapes, mite crust, raw ham/chevon/hoglin/kangaroo/moose/bison/bunfungus; White Strawberries got their own can; Canned Cactus is now Canned Cactus Flesh (and the item Cactus Chunk is renamed Cactus Flesh). Not run in game (CP-01 to CP-05) |
| Generated cans (cookies, cooked, bread, salad, soup, golden; [can_types.json](../kubejs/generation_scripts/food/can_types.json)) | DEPLOYED with 1.0.6, untested in game (see [food.md](food.md)) |
| Every remaining `x` row, `/` removals, alt inputs (2026-10-13) | BUILT, not run in game (CP-07 to CP-14): 17 raw fruit, shulker, 12 raw fish, 4 hazard, 2 special, 11 cookies, 10 cooked veggie, 3 cooked meat, 11 pastas, 9 pie fillings, 9 feasts, 2 drinks; 15 `/` cans removed; sliced alt inputs. 6 pie slices skipped (no source). Meal/pie/feast/drink/hazard values are placeholders ([3.1](#new-sections)) |
| Hunger/saturation columns | postponed until the food values are settled ([food.md](food.md) tuning) |
| Potential cans (2026-10-20 food audit) | 263 rows at Want `?` waiting on a decision: new edible Brewin drinks (Beer, Vodka, Dread Nog), Gingerbread, two tarts, and every family pulled in from [3](#outside): [Hazard](#hazard) (4 built cans listed for the first time), [Egg and dairy](#egg-dairy), [Preserves](#preserves), [Desserts](#desserts), [Cake](#cake), [Sweets](#sweets), [Handheld](#handheld), [Ingredients](#ingredients) |

**How to use it:** decide the `Want` cell first. Then fill the rest of the row as work lands.

| Cell | Meaning |
|---|---|
| `yes` | done (or, for the h/s columns, the value is written in) |
| `no` | wanted but not done yet |
| `n/a` | not canned (`Want` = no), so the column does not apply |
| `none` | Effects column only: the food has no effects, nothing to carry |
| `later` | h/s columns: postponed until hunger values are settled |
| `-` | waiting on the `Want` decision |
| `?` | `Want` column only: undecided |
| `8 / 6+1` | Input column only: items in the basic recipe / items + 1 additive in the additive recipe (sliced forms share the count); `n/a` not canned, `-` no recipe yet |

**Columns:**

- **Want**: should this food have a can. Prefilled `yes` where a can exists or the canning.js plan names it,
  `no` where removed or excluded by the plan (reason in the Item cell), `?` for everything else.
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
- **Food audit**: the can's hunger/saturation has been checked against the parity rules. None are, yet.
- **Effects**: the food's effects reach the can. Generated cans copy them with scaled durations; the hand-written
  raw cans do not.
- **Food h/s**, **Craft sum h/s**, **Canned h/s**: the plain food's hunger/saturation, the summed hunger/saturation of
  the least efficient recipe's inputs, and one can's total. Postponed.

## 1. Summary

Recounted from the sheet rows 2026-10-20 (Want, Impl, Texture, Recipe and Effects cells marked `x`).

| Family | Items | Want x | Want ? | Want / | Implemented | Own texture | Recipe | Effects carried |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| [Raw veggie](#raw-veggie) | 17 | 12 | 0 | 5 | 13 | 12 | 13 | 0 |
| [Raw fruit](#raw-fruit) | 41 | 29 | 0 | 12 | 28 | 11 | 28 | 0 |
| [Raw meat](#raw-meat) | 23 | 6 | 0 | 17 | 6 | 3 | 6 | 1 |
| [Raw fish](#raw-fish) | 12 | 12 | 0 | 0 | 12 | 3 | 12 | 4 |
| [Cookies](#cookies) | 12 | 12 | 0 | 0 | 12 | 1 | 12 | 6 |
| [Cooked veggie and fruit](#cooked-veggie) | 21 | 20 | 0 | 1 | 17 | 2 | 17 | 8 |
| [Cooked meat](#cooked-meat) | 28 | 8 | 0 | 20 | 8 | 5 | 8 | 1 |
| [Cooked fish](#cooked-fish) | 11 | 8 | 0 | 3 | 8 | 2 | 8 | 0 |
| [Bread](#bread) | 12 | 8 | 0 | 4 | 8 | 1 | 8 | 3 |
| [Salad](#salad) | 11 | 8 | 0 | 3 | 8 | 0 | 8 | 6 |
| [Soup](#soup) | 31 | 27 | 0 | 4 | 27 | 2 | 27 | 22 |
| [Meals](#meal) | 64 | 15 | 0 | 49 | 11 | 0 | 11 | 10 |
| [Pie](#pie) | 20 | 16 | 2 | 2 | 10 | 0 | 10 | 10 |
| [Feasts](#feast) | 12 | 9 | 0 | 3 | 9 | 0 | 9 | 8 |
| [Drinks](#drink) | 63 | 2 | 0 | 61 | 2 | 1 | 2 | 0 |
| [Drinks: alcohol](#alcohol) | 44 | 0 | 3 | 41 | 0 | 0 | 0 | 0 |
| [Golden and enchanted golden](#golden) | 50 | 50 | 0 | 0 | 50 | 0 | 50 | 50 |
| [Special](#special) | 15 | 2 | 0 | 13 | 2 | 1 | 2 | 0 |
| [Hazard](#hazard) | 11 | 4 | 7 | 0 | 4 | 4 | 4 | 4 |
| [Egg and dairy](#egg-dairy) | 6 | 0 | 6 | 0 | 0 | 0 | 0 | 0 |
| [Preserves](#preserves) | 27 | 0 | 27 | 0 | 0 | 0 | 0 | 0 |
| [Desserts](#desserts) | 37 | 0 | 37 | 0 | 0 | 0 | 0 | 0 |
| [Cake](#cake) | 18 | 0 | 18 | 0 | 0 | 0 | 0 | 0 |
| [Sweets](#sweets) | 98 | 0 | 96 | 2 | 0 | 0 | 0 | 0 |
| [Handheld](#handheld) | 52 | 0 | 52 | 0 | 0 | 0 | 0 | 0 |
| [Ingredients](#ingredients) | 15 | 0 | 15 | 0 | 0 | 0 | 0 | 0 |
| **Total** | **751** | **248** | **263** | **240** | **235** | **48** | **235** | **133** |

## 2. Sheets

### 2.1 Raw veggie <a id="raw-veggie"></a>

Baked Potato meals (5, 6), 2 servings. Additive: salt or bone broth.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Beetroot `minecraft:beetroot` | Canned Beetroot | 8 / 6+1 | - | x | x | x | / | none | later | later | later |
| x | Bell Pepper `rusticdelight:bell_pepper_*` | Canned Bell Pepper | 8 / 6+1 | - | x | x | x | / | none | later | later | later |
| x | Cabbage `farmersdelight:cabbage` | Canned Cabbage | 5 / 3+1 | Cabbage Leaf: x | x | x | x | / | none | later | later | later |
| x | Cactus Flesh `delightful:cactus_flesh` | Canned Cactus Flesh | 4 / 2+1 | - | x | / | x | / | none | later | later | later |
| x | Carrot `minecraft:carrot` | Canned Carrot | 3 / 2+1 | - | x | x | x | / | none | later | later | later |
| x | Gongylidia `alexsmobs:gongylidia` | Canned Gongylidia | 2 / 1+1 | - | x | x | x | / | none | later | later | later |
| x | Onion `farmersdelight:onion` | Canned Onion | 5 / 3+1 | - | x | x | x | / | none | later | later | later |
| x | Potato `minecraft:potato` | Canned Potato | 8 / 6+1 | Potato Slices: x | x | x | x | / | none | later | later | later |
| x | Pumpkin Slice `farmersdelight:pumpkin_slice` | Canned Pumpkin | 4 / 3+1 | - | x | x | x | / | none | later | later | later |
| x | Tomato `farmersdelight:tomato` | Canned Tomato | 8 / 6+1 | - | x | x | x | / | none | later | later | later |
| x | Corn Kernel `corn_delight:corn_seeds` | Canned Corn Kernel | - | - | - | - | - | - | none | - | - | - |
| x | Maize `corn_delight:corn` | Canned Corn | - | - | - | - | - | - | none | - | - | - |
| / | Green Bell Pepper `rusticdelight:bell_pepper_green` | Canned Bell Pepper | n/a | - | x | x | x | / | none | later | later | later |
| / | Red Bell Pepper `rusticdelight:bell_pepper_red` | Canned Bell Pepper | n/a | - | x | x | x | / | none | later | later | later |
| / | Yellow Bell Pepper `rusticdelight:bell_pepper_yellow` | Canned Bell Pepper | n/a | - | x | x | x | / | none | later | later | later |
| / | Mint Leaves `neapolitan:mint_leaves` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Diced Potatoes `moredelight:diced_potatoes` | Canned Diced Potatoes | n/a | - | - | - | - | - | none | - | - | - |

### 2.2 Raw fruit <a id="raw-fruit"></a>

Fruit Meals (5, 3), 3 servings. Additive: sugar or honey.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Apple `minecraft:apple` | Canned Apple | 3 / 2+1 | - | x | x | x | / | none | later | later | later |
| x | Banana `neapolitan:banana` | Canned Banana | 7 / 5+1 | - | x | x | x | / | / | later | later | later |
| x | Cantaloupe Slice `delightful:cantaloupe_slice` | Canned Cantaloupe | 4 / 3+1 | - | x | x | x | / | / | later | later | later |
| x | Cherry `vinery:cherry` | Canned Cherries | 6 / 4+1 | - | x | x | x | / | none | later | later | later |
| x | Glow Berries `minecraft:glow_berries` | Canned Glow Berries | 8 / 6+1 | - | x | x | x | / | / | later | later | later |
| x | Melon Slice `minecraft:melon_slice` | Canned Melon | 6 / 4+1 | - | x | x | x | / | none | later | later | later |
| x | Mulberry `upgrade_aquatic:mulberry` | Canned Mulberries | 5 / 3+1 | - | x | x | x | / | none | later | later | later |
| x | Salmonberries `delightful:salmonberries` | Canned Salmonberries | 4 / 3+1 | - | x | x | x | / | / | later | later | later |
| x | Strawberries `neapolitan:strawberries` | Canned Strawberries | 6 / 4+1 | - | x | x | x | / | none | later | later | later |
| x | White Strawberries `neapolitan:white_strawberries` | Canned White Strawberries | 3 / 2+1 | - | x | - | x | / | none | later | later | later |
| x | Sweet Berries `minecraft:sweet_berries` | Canned Sweet Berries | 8 / 6+1 | - | x | x | x | / | none | later | later | later |
| x | Bayberry `fruitsdelight:bayberry` | Canned Bayberries | 6 / 4+1 | - | x | - | x | / | none | later | later | later |
| x | Blueberry `fruitsdelight:blueberry` | Canned Blueberries | 6 / 4+1 | - | x | - | x | / | none | later | later | later |
| x | Coffee Berries `farmersrespite:coffee_berries` | Canned Coffee Berries | - | - | - | - | - | - | - | - | - | - |
| x | Cranberry `fruitsdelight:cranberry` | Canned Cranberries | 6 / 4+1 | - | x | x | x | / | none | later | later | later |
| x | Durian Flesh `fruitsdelight:durian_flesh` | Canned Durian | 3 / 2+1 | - | x | - | x | / | none | later | later | later |
| x | Fig `fruitsdelight:fig` | Canned Figs | 4 / 3+1 | - | x | - | x | / | none | later | later | later |
| x | Hamimelon Slice `fruitsdelight:hamimelon_slice` | Canned Hamimelon | 6 / 4+1 | - | x | - | x | / | none | later | later | later |
| x | Hawberry `fruitsdelight:hawberry` | Canned Hawberries | 6 / 4+1 | - | x | - | x | / | none | later | later | later |
| x | Kiwi `fruitsdelight:kiwi` | Canned Kiwi | 4 / 3+1 | - | x | - | x | / | none | later | later | later |
| x | Lemon `fruitsdelight:lemon` | Canned Lemon | 3 / 2+1 | Lemon Slice: x | x | - | x | / | none | later | later | later |
| x | Lychee `fruitsdelight:lychee` | Canned Lychee | 6 / 4+1 | - | x | - | x | / | none | later | later | later |
| x | Mango `fruitsdelight:mango` | Canned Mango | 4 / 3+1 | - | x | - | x | / | none | later | later | later |
| x | Mangosteen `fruitsdelight:mangosteen` | Canned Mangosteen | 4 / 3+1 | - | x | - | x | / | none | later | later | later |
| x | Orange `fruitsdelight:orange` | Canned Orange | 4 / 3+1 | Orange Slice: x | x | - | x | / | none | later | later | later |
| x | Peach `fruitsdelight:peach` | Canned Peach | 4 / 3+1 | - | x | - | x | / | none | later | later | later |
| x | Pear `fruitsdelight:pear` | Canned Pear | 4 / 3+1 | - | x | - | x | / | none | later | later | later |
| x | Persimmon `fruitsdelight:persimmon` | Canned Persimmon | 4 / 3+1 | - | x | - | x | / | none | later | later | later |
| x | Pineapple Slice `fruitsdelight:pineapple_slice` | Canned Pineapple | 6 / 4+1 | - | x | - | x | / | none | later | later | later |
| / | Acorn `delightful:acorn` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Lemon Slice `fruitsdelight:lemon_slice` (alt input of Canned Lemon) | Canned Lemon Slice | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Orange Slice `fruitsdelight:orange_slice` (alt input of Canned Orange) | Canned Orange Slice | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Banana `alexsmobs:banana` (not canned, merged into the Neapolitan banana can) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Red Grape `vinery:red_grape` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Red Jungle Grapes `vinery:jungle_grapes_red` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Red Savanna Grapes `vinery:savanna_grapes_red` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Red Taiga Grapes `vinery:taiga_grapes_red` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | White Grape `vinery:white_grape` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | White Jungle Grapes `vinery:jungle_grapes_white` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | White Savanna Grapes `vinery:savanna_grapes_white` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | White Taiga Grapes `vinery:taiga_grapes_white` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

- removing alex's mobs banana, merge w/ neapolitan (done 2026-10-13: the can only takes Neapolitan bananas)

### 2.3 Raw meat <a id="raw-meat"></a>

Meat Meals (5, 3), 4 servings. Additive: salt or bone broth.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Raw Beef `minecraft:beef` | Canned Raw Beef | 5 / 4+1 | Minced Beef: x | x | x | x | / | none | later | later | later |
| x | Raw Chicken `minecraft:chicken` | Canned Raw Chicken | 8 / 6+1 | Raw Chicken Cuts: x | x | / | x | / | / | later | later | later |
| x | Raw Mutton `minecraft:mutton` | Canned Raw Mutton | 8 / 6+1 | Raw Mutton Chops: x | x | x | x | / | none | later | later | later |
| x | Raw Porkchop `minecraft:porkchop` | Canned Raw Pork | 5 / 4+1 | Raw Bacon: x | x | x | x | / | none | later | later | later |
| x | Raw Rabbit `minecraft:rabbit` | Canned Raw Rabbit | 5 / 4+1 | - | x | / | x | / | none | later | later | later |
| x | Shulker Mollusk `endersdelight:shulker_mollusk` | Canned Shulker Flesh | 8 / 6+1 | Shulker Filet: x | x | / | x | / | x | later | later | later |
| / | Ground Strider `nethersdelight:ground_strider` | Canned Ground Strider | n/a | - | - | - | - | - | - | - | - | - |
| / | Hoglin Ear `nethersdelight:hoglin_ear` | Canned Hoglin Ear | n/a | - | - | - | - | - | none | - | - | - |
| / | Raw Duck `untitledduckmod:raw_duck` | Canned Raw Duck | n/a | - | - | - | - | - | none | - | - | - |
| / | Raw Duck Fillet `abnormals_delight:duck_fillet` | Canned Raw Duck Fillet | n/a | - | - | - | - | - | - | - | - | - |
| / | Raw Goose `untitledduckmod:raw_goose` | Canned Raw Goose | n/a | - | - | - | - | - | none | - | - | - |
| / | Raw Mint Chops `neapolitan:mint_chops` | Canned Raw Mint Chops | n/a | - | - | - | - | - | - | - | - | - |
| / | Raw Venison Chops `delightful:venison_chops` | Canned Raw Venison Chops | n/a | - | - | - | - | - | none | - | - | - |
| / | Raw Venison Shanks `abnormals_delight:venison_shanks` | Canned Raw Venison Shanks | n/a | - | - | - | - | - | none | - | - | - |
| / | Strider Slice `nethersdelight:strider_slice` | Canned Strider Slice | n/a | - | - | - | - | - | - | - | - | - |
| / | Ham `farmersdelight:ham` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Hoglin Loin `nethersdelight:hoglin_loin` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Kangaroo Meat `alexsmobs:kangaroo_meat` (removed 2026-10-13) | n/a | n/a | Kangaroo Shank | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Mite Crust `endersdelight:mite_crust` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Raw Bison `alexsdelight:raw_bison` (removed 2026-10-13) | n/a | n/a | Bison Mince | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Raw Bunfungus `alexsdelight:raw_bunfungus` (removed 2026-10-13) | n/a | n/a | Raw Bunfungus Drumstick | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Raw Chevon `delightful:raw_goat` (removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Raw Moose Ribs `alexsmobs:moose_ribs` (removed 2026-10-13) | n/a | n/a | Loose Moose Rib | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.4 Raw fish <a id="raw-fish"></a>

Raw meat's goal: Meat Meals (5, 3), 4 servings, salt or bone broth. Generated (section raw_fish). Every sliced fish is an
alt input: 8 slices make a half full can (2 of 4 servings).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Blobfish `alexsmobs:blobfish` | Canned Blobfish | 4 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Flying Fish `alexsmobs:flying_fish` | Canned Flying Fish | 4 / 3+1 | - | x | / | x | / | none | later | later | later |
| x | Lobster Tail `alexsmobs:lobster_tail` | Canned Lobster Tail | 5 / 4+1 | - | x | / | x | / | none | later | later | later |
| x | Raw Calamari `rusticdelight:calamari` | Canned Raw Calamari | 7 / 6+1 | Raw Calamari Slice: x | x | / | x | / | none | later | later | later |
| x | Raw Catfish `alexsmobs:raw_catfish` | Canned Raw Catfish | 6 / 5+1 | Raw Catfish Slice: x | x | / | x | / | none | later | later | later |
| x | Raw Cod `minecraft:cod` | Canned Raw Cod | 8 / 6+1 | Raw Cod Slice: x | x | x | x | / | none | later | later | later |
| x | Raw Crab Leg `quark:crab_leg` | Canned Raw Crab Leg | 8 / 6+1 | - | x | / | x | / | none | later | later | later |
| x | Raw Lionfish `upgrade_aquatic:lionfish` | Canned Raw Lionfish | 6 / 5+1 | - | x | / | x | / | x | later | later | later |
| x | Raw Perch `upgrade_aquatic:perch` | Canned Raw Perch | 7 / 6+1 | Raw Perch Slice: x | x | / | x | / | x | later | later | later |
| x | Raw Pike `upgrade_aquatic:pike` | Canned Raw Pike | 4 / 3+1 | Raw Pike Slice: x | x | / | x | / | x | later | later | later |
| x | Raw Salmon `minecraft:salmon` | Canned Raw Salmon | 8 / 6+1 | Raw Salmon Slice: x | x | x | x | / | none | later | later | later |
| x | Tropical Fish `minecraft:tropical_fish` | Canned Tropical Fish | 8 / 6+1 | - | x | x | x | / | none | later | later | later |

### 2.5 Cookies <a id="cookies"></a>

Cookie x2 meals, 5 servings, fast. Additive: sugar.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Cookie `minecraft:cookie` | Canned Cookies | 8 / 6+1 | - | x | x | x | / | none | later | later | later |
| x | Bayberry Cookie `fruitsdelight:bayberry_cookie` | Canned Bayberry Cookies | 8 / 6+1 | - | x | - | x | / | x | later | later | later |
| x | Cherry Cookie `abnormals_delight:cherry_cookie` | Canned Cherry Cookies | 8 / 6+1 | - | x | - | x | / | none | later | later | later |
| x | Cranberry Cookie `fruitsdelight:cranberry_cookie` | Canned Cranberry Cookies | 8 / 6+1 | - | x | - | x | / | x | later | later | later |
| x | Green Tea Cookie `farmersrespite:green_tea_cookie` | Canned Green Tea Cookies | 8 / 6+1 | - | x | - | x | / | x | later | later | later |
| x | Honey Cookie `farmersdelight:honey_cookie` | Canned Honey Cookies | 8 / 6+1 | - | x | - | x | / | none | later | later | later |
| x | Lemon Cookie `fruitsdelight:lemon_cookie` | Canned Lemon Cookies | 8 / 6+1 | - | x | - | x | / | x | later | later | later |
| x | Maple Cookie `abnormals_delight:maple_cookie` | Canned Maple Cookies | 8 / 6+1 | - | x | - | x | / | none | later | later | later |
| x | Mulberry Cookie `abnormals_delight:mulberry_cookie` | Canned Mulberry Cookies | 8 / 6+1 | - | x | - | x | / | none | later | later | later |
| x | Persimmon Cookie `fruitsdelight:persimmon_cookie` | Canned Persimmon Cookies | 8 / 6+1 | - | x | - | x | / | x | later | later | later |
| x | Sweet Berry Cookie `farmersdelight:sweet_berry_cookie` | Canned Sweet Berry Cookies | 8 / 6+1 | - | x | - | x | / | none | later | later | later |
| x | Uncanny Cookie `endersdelight:uncanny_cookies` | Canned Uncanny Cookies | 8 / 6+1 | - | x | - | x | / | x | later | later | later |

### 2.6 Cooked veggie and fruit <a id="cooked-veggie"></a>

Item x2 meals, 4 servings. Additive: salt.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Baked Potato `minecraft:baked_potato` | Canned Baked Potato | 6 / 5+1 | Baked Potato Slices: x | x | x | x | / | none | later | later | later |
| x | Cactus Steak `delightful:cactus_steak` | Canned Cactus Steak | 6 / 5+1 | - | x | / | x | / | none | later | later | later |
| x | Cooked Rice `farmersdelight:cooked_rice` | Canned Cooked Rice | 6 / 5+1 | - | x | / | x | / | x | later | later | later |
| x | Dried Kelp `minecraft:dried_kelp` | Canned Dried Kelp | 6 / 5+1 | - | x | x | x | / | none | later | later | later |
| x | Roasted Green Bell Pepper `rusticdelight:roasted_bell_pepper_green` | Canned Roasted Bell Pepper | 6 / 5+1 | - | x | / | x | / | none | later | later | later |
| x | Roasted Red Bell Pepper `rusticdelight:roasted_bell_pepper_red` | Canned Roasted Bell Pepper | 6 / 5+1 | - | x | / | x | / | none | later | later | later |
| x | Roasted Yellow Bell Pepper `rusticdelight:roasted_bell_pepper_yellow` | Canned Roasted Bell Pepper | 6 / 5+1 | - | x | / | x | / | none | later | later | later |
| x | Baked Durian `fruitsdelight:baked_durian` | Canned Baked Durian | 6 / 5+1 | - | x | - | x | / | x | later | later | later |
| x | Baked Pear `fruitsdelight:baked_pear` | Canned Baked Pear | 6 / 5+1 | - | x | - | x | / | x | later | later | later |
| x | Boiled Blue Pickerelweed `upgrade_aquatic:boiled_blue_pickerelweed` | Canned Boiled Blue Pickerelweed | 6 / 5+1 | - | x | - | x | / | none | later | later | later |
| x | Boiled Corn `corn_delight:boiled_corn` | Canned Boiled Corn | 6 / 5+1 | - | x | - | x | / | none | later | later | later |
| x | Boiled Purple Pickerelweed `upgrade_aquatic:boiled_purple_pickerelweed` | Canned Boiled Purple Pickerelweed | 6 / 5+1 | - | x | - | x | / | none | later | later | later |
| x | Chips `pneumaticcraft:chips` | Canned Chips | - | - | - | - | - | - | none | - | - | - |
| x | Dried Banana `neapolitan:dried_banana` | Canned Dried Banana | 6 / 5+1 | - | x | - | x | / | x | later | later | later |
| x | Dried Persimmon `fruitsdelight:dried_persimmon` | Canned Dried Persimmon | 6 / 5+1 | - | x | - | x | / | x | later | later | later |
| x | Grilled Corn `corn_delight:grilled_corn` | Canned Grilled Corn | - | - | - | - | - | - | none | - | - | - |
| x | Kimchi `brewinandchewin:kimchi` | Canned Kimchi | 6 / 5+1 | - | x | - | x | / | x | later | later | later |
| x | Pickled Pickles `brewinandchewin:pickled_pickles` | Canned Pickled Pickles | 6 / 5+1 | - | x | - | x | / | x | later | later | later |
| / | Popcorn `corn_delight:popcorn` | Canned Popcorn | n/a | - | - | - | - | - | none | - | - | - |
| x | Roasted Adzuki Beans `neapolitan:roasted_adzuki_beans` | Canned Roasted Adzuki Beans | 6 / 5+1 | - | x | - | x | / | x | later | later | later |
| x | Tortilla Chip `corn_delight:tortilla_chip` | Canned Tortilla Chip | - | - | - | - | - | - | none | - | - | - |

### 2.7 Cooked meat <a id="cooked-meat"></a>

Item x2 meals, 3 servings. Additive: salt or bone broth.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| / | Beef Patty `farmersdelight:beef_patty` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Bison Patty `alexsdelight:bison_patty` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Bacon `farmersdelight:cooked_bacon` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Bison `alexsdelight:cooked_bison` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Bunfungus `alexsdelight:cooked_bunfungus` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Chevon `delightful:cooked_goat` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Cooked Chicken `minecraft:cooked_chicken` | Canned Cooked Chicken | 5 / 4+1 | Cooked Chicken Cuts: x | x | x | x | / | none | later | later | later |
| / | Cooked Kangaroo Meat `alexsmobs:cooked_kangaroo_meat` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Kangaroo Shank `alexsdelight:cooked_kangaroo_shank` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Loose Moose Rib `alexsdelight:cooked_loose_moose_rib` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Moose Ribs `alexsmobs:cooked_moose_ribs` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Cooked Mutton `minecraft:cooked_mutton` | Canned Cooked Mutten | 5 / 4+1 | Cooked Mutton Chops: x | x | x | x | / | none | later | later | later |
| x | Cooked Porkchop `minecraft:cooked_porkchop` | Canned Cooked Pork | 5 / 4+1 | - | x | x | x | / | none | later | later | later |
| x | Cooked Rabbit `minecraft:cooked_rabbit` | Canned Cooked Rabbit | 5 / 4+1 | - | x | x | x | / | none | later | later | later |
| / | Hoglin Sirloin `nethersdelight:hoglin_sirloin` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Smoked Ham `farmersdelight:smoked_ham` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Steak `minecraft:cooked_beef` | Canned Steak | 5 / 4+1 | - | x | x | x | / | none | later | later | later |
| / | Cooked Bunfungus Drumstick `alexsdelight:cooked_bunfungus_drumstick` | Canned Cooked Bunfungus Drumstick | n/a | - | - | - | - | - | none | - | - | - |
| / | Cooked Centipede Leg `alexsdelight:cooked_centipede_leg` | Canned Cooked Centipede Leg | n/a | - | - | - | - | - | none | - | - | - |
| / | Cooked Duck `untitledduckmod:cooked_duck` | Canned Cooked Duck | n/a | - | - | - | - | - | none | - | - | - |
| / | Cooked Duck Fillet `abnormals_delight:cooked_duck_fillet` | Canned Cooked Duck Fillet | n/a | - | - | - | - | - | none | - | - | - |
| / | Cooked Goose `untitledduckmod:cooked_goose` | Canned Cooked Goose | n/a | - | - | - | - | - | none | - | - | - |
| / | Cooked Mint Chops `neapolitan:cooked_mint_chops` | Canned Cooked Mint Chops | n/a | - | - | - | - | - | - | - | - | - |
| / | Cooked Venison Chops `delightful:cooked_venison_chops` | Canned Cooked Venison Chops | n/a | - | - | - | - | - | none | - | - | - |
| / | Cooked Venison Shanks `abnormals_delight:cooked_venison_shanks` | Canned Cooked Venison Shanks | n/a | - | - | - | - | - | none | - | - | - |
| x | Honey Glazed Porkchop `buzzier_bees:glazed_porkchop` | Canned Honey Glazed Porkchop | 5 / 4+1 | - | x | - | x | / | none | later | later | later |
| x | Jerky `brewinandchewin:jerky` | Canned Jerky | 5 / 4+1 | - | x | - | x | / | none | later | later | later |
| x | Maple Glazed Bacon `abnormals_delight:maple_glazed_bacon` | Canned Maple Glazed Bacon | 5 / 4+1 | - | x | - | x | / | x | later | later | later |

### 2.8 Cooked fish <a id="cooked-fish"></a>

Same section as cooked meat (cooked_meat in can_types.json).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Cooked Calamari `rusticdelight:cooked_calamari` | Canned Cooked Calamari | 5 / 4+1 | Cooked Calamari Slice: x | x | / | x | / | none | later | later | later |
| x | Cooked Catfish `alexsmobs:cooked_catfish` | Canned Cooked Catfish | 5 / 4+1 | - | x | / | x | / | none | later | later | later |
| x | Cooked Catfish Slice `alexsdelight:cooked_catfish_slice` | Canned Cooked Catfish | 5 / 4+1 | - | x | / | x | / | none | later | later | later |
| x | Cooked Cod `minecraft:cooked_cod` | Canned Cooked Cod | 5 / 4+1 | Cooked Cod Slice: x | x | x | x | / | none | later | later | later |
| x | Cooked Lionfish `upgrade_aquatic:cooked_lionfish` | Canned Cooked Lionfish | 5 / 4+1 | - | x | / | x | / | none | later | later | later |
| x | Cooked Perch `upgrade_aquatic:cooked_perch` | Canned Cooked Perch | 5 / 4+1 | Cooked Perch Slice: x | x | / | x | / | none | later | later | later |
| x | Cooked Pike `upgrade_aquatic:cooked_pike` | Canned Cooked Pike | 5 / 4+1 | Cooked Pike Slice: x | x | / | x | / | none | later | later | later |
| x | Cooked Salmon `minecraft:cooked_salmon` | Canned Cooked Salmon | 5 / 4+1 | Cooked Salmon Slice: x | x | x | x | / | none | later | later | later |
| / | Cooked Crab Leg `quark:cooked_crab_leg` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Lobster Tail `alexsmobs:cooked_lobster_tail` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Kippers `brewinandchewin:kippers` | Canned Kippers | n/a | - | - | - | - | - | - | - | - | - |

### 2.9 Bread <a id="bread"></a>

Bread x2 meals, 4 servings, / additive (honey bread also 5 bread + honey).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Adzuki Bun `neapolitan:adzuki_bun` | Canned Adzuki Bun | 6 | - | x | / | x | / | x | later | later | later |
| x | Banana Bread `neapolitan:banana_bread` | Canned Banana Bread | 6 | - | x | / | x | / | x | later | later | later |
| x | Bread `minecraft:bread` | Canned Bread | 6 | - | x | x | x | / | none | later | later | later |
| / | Bread Slice `moredelight:bread_slice` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Cornbread `corn_delight:cornbread` | Canned Cornbread | 6 | - | x | / | x | / | none | later | later | later |
| x | Honey Bread `buzzier_bees:honey_bread` | Canned Honey Bread | 6 / 5+1 | - | x | / | x | / | none | later | later | later |
| x | Mulberry Bread `upgrade_aquatic:mulberry_bread` | Canned Mulberry Bread | 6 | - | x | / | x | / | none | later | later | later |
| x | Nether Wart Sourdough `farmersrespite:nether_wart_sourdough` | Canned Nether Wart Sourdough | 6 | - | x | / | x | / | x | later | later | later |
| x | Sourdough Bread `pneumaticcraft:sourdough_bread` | Canned Sourdough Bread | 6 | - | x | / | x | / | none | later | later | later |
| / | Toast `moredelight:toast` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Tortilla `corn_delight:tortilla` (not canned, removed 2026-10-13) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Strawberry Scones `neapolitan:strawberry_scones` (plan: all bread but scones) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.10 Salad <a id="salad"></a>

Salad x1.25 meals, 8 servings, fast, effects 0.75x. Vinegar additive later.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Chicken Salad `moredelight:chicken_salad` | Canned Chicken Salad | 6 | - | x | / | x | / | x | later | later | later |
| x | Bowl of Exotic Fruits `quarkdelight:bowl_of_exotic_fruits` | Canned Exotic Fruits | 6 | - | x | / | x | / | none | later | later | later |
| x | Field Salad `delightful:field_salad` | Canned Field Salad | 6 | - | x | / | x | / | x | later | later | later |
| x | Fruit Salad `farmersdelight:fruit_salad` | Canned Fruit Salad | 6 | - | x | / | x | / | x | later | later | later |
| x | Lush Moss Salad `quarkdelight:lush_moss_salad` | Canned Lush Moss Salad | 6 | - | x | / | x | / | none | later | later | later |
| x | Mango Salad `fruitsdelight:mango_salad` | Canned Mango Salad | 6 | - | x | / | x | / | x | later | later | later |
| x | Mixed Salad `farmersdelight:mixed_salad` | Canned Mixed Salad | 6 | - | x | / | x | / | x | later | later | later |
| x | Nether Salad `farmersdelight:nether_salad` | Canned Nether Salad | 6 | - | x | / | x | / | x | later | later | later |
| / | Maggot Salad `alexsdelight:maggot_salad` (plan: all salads but maggot salad) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Potato Salad `moredelight:potato_salad` (plan: all salads but potato salad) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Potato Salad `rusticdelight:potato_salad` (plan: all salads but potato salad) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.11 Soup <a id="soup"></a>

Soup x1.25 meals, 8 servings, effects 0.75x. Additive: salt or bone broth (3 soups).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Acacia Blossom Soup `alexsdelight:acacia_blossom_soup` | Canned Acacia Blossom Soup | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Adzuki Stew `neapolitan:adzuki_stew` | Canned Adzuki Stew | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Baked Cod Stew `farmersdelight:baked_cod_stew` | Canned Baked Cod Stew | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Bayberry Soup `fruitsdelight:bayberry_soup` | Canned Bayberry Soup | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Beef Stew `farmersdelight:beef_stew` | Canned Beef Stew | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Beetroot Soup `minecraft:beetroot_soup` | Canned Beetroot Soup | 6 / 3+1 | - | x | / | x | / | none | later | later | later |
| x | Bell Pepper Soup `rusticdelight:bell_pepper_soup` | Canned Bell Pepper Soup | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Blazing Chili `farmersrespite:blazing_chili` | Canned Blazing Chili | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Bone Broth `farmersdelight:bone_broth` | Canned Bone Broth | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Carrot Soup `moredelight:carrot_soup` | Canned Carrot Soup | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Chicken Soup `farmersdelight:chicken_soup` | Canned Chicken Soup | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Chorus Stew `endersdelight:chorus_stew` | Canned Chorus Stew | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Corn Soup `corn_delight:corn_soup` | Canned Corn Soup | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Creamy Onion Soup `brewinandchewin:creamy_onion_soup` | Canned Creamy Onion Soup | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Endermite Stew `endersdelight:endermite_stew` | Canned Endermite Stew | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Fig Chicken Stew `fruitsdelight:fig_chicken_stew` | Canned Fig Chicken Stew | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Fish Stew `farmersdelight:fish_stew` | Canned Fish Stew | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Kangaroo Stew `alexsdelight:kangaroo_stew` | Canned Kangaroo Stew | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Miso with Bamboo Sprouts `quarkdelight:miso_with_bamboo_sprouts` | Canned Miso with Bamboo Sprouts | 6 / 3+1 | - | x | / | x | / | none | later | later | later |
| x | Mushroom Stew `minecraft:mushroom_stew` | Canned Mushroom Stew | 6 / 3+1 | - | x | x | x | / | none | later | later | later |
| x | Noodle Soup `farmersdelight:noodle_soup` | Canned Noodle Soup | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Pumpkin Soup `farmersdelight:pumpkin_soup` | Canned Pumpkin Soup | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Rabbit Stew `minecraft:rabbit_stew` | Canned Rabbit Stew | 6 / 3+1 | - | x | x | x | / | none | later | later | later |
| x | Sinigang `delightful:sinigang` | Canned Sinigang | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Sopa De Macaco `alexsmobs:sopa_de_macaco` | Canned Sopa De Macaco | 6 / 3+1 | - | x | / | x | / | none | later | later | later |
| x | Strider Moss Stew `nethersdelight:strider_moss_stew` | Canned Strider Moss Stew | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| x | Vegetable Soup `farmersdelight:vegetable_soup` | Canned Vegetable Soup | 6 / 3+1 | - | x | / | x | / | x | later | later | later |
| / | Cavern Explorer Stew `quarkdelight:cavern_explorer_stew` (plan: all soups but cave explorer) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Chorus Stew `endersdelight:chorus_stew_wood` (wooden soup, removed) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Endermite Stew `endersdelight:endermite_stew_wood` (wooden soup, removed) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Suspicious Stew `minecraft:suspicious_stew` (effects live in NBT, a can would lose them) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.12 Meals <a id="meal"></a>

Placeholder goal: 2 pastas -> 4 meals of 0.75 pasta, effects 0.75x. Only the pastas are built (veg noodles -> Vegetable
Pasta, duck noodles -> Pasta with Duck).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Bell Pepper Pasta `rusticdelight:bell_pepper_pasta` | Canned Bell Pepper Pasta | 2 | - | x | / | x | / | x | later | later | later |
| x | Cheesy Pasta `brewinandchewin:cheesy_pasta` | Canned Cheesy Pasta | 2 | - | x | / | x | / | x | later | later | later |
| x | Crab Pasta `quarkdelight:crab_pasta` | Canned Crab Pasta | 2 | - | x | / | x | / | none | later | later | later |
| x | Kangaroo Pasta `alexsdelight:kangaroo_pasta` | Canned Kangaroo Pasta | 2 | - | x | / | x | / | x | later | later | later |
| x | Lobster Pasta `alexsdelight:lobster_pasta` | Canned Lobster Pasta | 2 | - | x | / | x | / | x | later | later | later |
| x | Duck Noodles `abnormals_delight:duck_noodles` | Canned Pasta with Duck | 2 | - | x | / | x | / | x | later | later | later |
| x | Pasta with Meatballs `farmersdelight:pasta_with_meatballs` | Canned Pasta with Meatballs | 2 | - | x | / | x | / | x | later | later | later |
| x | Pasta with Mutton Chop `farmersdelight:pasta_with_mutton_chop` | Canned Pasta with Mutton Chop | 2 | - | x | / | x | / | x | later | later | later |
| x | Pearl Pasta `endersdelight:pearl_pasta` | Canned Pearl Pasta | 2 | - | x | / | x | / | x | later | later | later |
| x | Squid Ink Pasta `farmersdelight:squid_ink_pasta` | Canned Squid Ink Pasta | 2 | - | x | / | x | / | x | later | later | later |
| x | Vegetable Noodles `farmersdelight:vegetable_noodles` | Canned Vegetable Pasta | 2 | - | x | / | x | / | x | later | later | later |
| / | Adzuki Curry `neapolitan:adzuki_curry` | Canned Adzuki Curry | n/a | - | - | - | - | - | - | - | - | - |
| / | Bacon and Eggs `farmersdelight:bacon_and_eggs` | Canned Bacon and Eggs | n/a | - | - | - | - | - | - | - | - | - |
| / | Black Cod `farmersrespite:black_cod` | Canned Black Cod | n/a | - | - | - | - | - | - | - | - | - |
| / | Coconut Curry `delightful:coconut_curry` | Canned Coconut Curry | n/a | - | - | - | - | - | - | - | - | - |
| / | Coffee-Braised Beef `rusticdelight:coffee_braised_beef` | Canned Coffee-Braised Beef | n/a | - | - | - | - | - | - | - | - | - |
| / | Cooked Rice with Beef `moredelight:cooked_rice_with_beef` | Canned Cooked Rice with Beef | n/a | - | - | - | - | - | - | - | - | - |
| / | Cooked Rice with Chicken Cuts `moredelight:cooked_rice_with_chicken_cuts` | Canned Cooked Rice with Chicken Cuts | n/a | - | - | - | - | - | - | - | - | - |
| / | Cooked Rice with Porkchop `moredelight:cooked_rice_with_porkchop` | Canned Cooked Rice with Porkchop | n/a | - | - | - | - | - | - | - | - | - |
| / | Cooked Whole Crab `quarkdelight:cooked_whole_crab` | Canned Cooked Whole Crab | n/a | - | - | - | - | - | none | - | - | - |
| x | Cornbread Stuffing `corn_delight:cornbread_stuffing` | Canned Cornbread Stuffing | - | - | - | - | - | - | - | - | - | - |
| x | Creamed Corn `corn_delight:creamed_corn` | Canned Creamed Corn | - | - | - | - | - | - | - | - | - | - |
| x | Creamy Pasta with Chicken Cuts `moredelight:creamy_pasta_with_chicken_cuts` | Canned Creamy Pasta with Chicken Cuts | - | - | - | - | - | - | - | - | - | - |
| x | Creamy Pasta with Ham `moredelight:creamy_pasta_with_ham` | Canned Creamy Pasta with Ham | - | - | - | - | - | - | - | - | - | - |
| / | Diced Potatoes with Beef `moredelight:diced_potatoes_with_beef` | Canned Diced Potatoes with Beef | n/a | - | - | - | - | - | none | - | - | - |
| / | Diced Potatoes with Chicken Cuts `moredelight:diced_potatoes_with_chicken_cuts` | Canned Diced Potatoes with Chicken Cuts | n/a | - | - | - | - | - | none | - | - | - |
| / | Diced Potatoes with Egg and Tomato `moredelight:diced_potatoes_with_egg_and_tomato` | Canned Diced Potatoes with Egg and Tomato | n/a | - | - | - | - | - | none | - | - | - |
| / | Diced Potatoes with Porkchop `moredelight:diced_potatoes_with_porkchop` | Canned Diced Potatoes with Porkchop | n/a | - | - | - | - | - | none | - | - | - |
| / | Dune Platter `abnormals_delight:dune_platter` | Canned Dune Platter | n/a | - | - | - | - | - | - | - | - | - |
| / | Ender Paella `endersdelight:ender_paella` | Canned Ender Paella | n/a | - | - | - | - | - | - | - | - | - |
| / | Escargot `abnormals_delight:escargot` | Canned Escargot | n/a | - | - | - | - | - | none | - | - | - |
| / | Fiery Fondue `brewinandchewin:fiery_fondue` | Canned Fiery Fondue | n/a | - | - | - | - | - | - | - | - | - |
| / | Fried Calamari `rusticdelight:fried_calamari` | Canned Fried Calamari | n/a | - | - | - | - | - | - | - | - | - |
| / | Fried Chicken `rusticdelight:fried_chicken` | Canned Fried Chicken | n/a | - | - | - | - | - | - | - | - | - |
| / | Fried Mushrooms `rusticdelight:fried_mushrooms` | Canned Fried Mushrooms | n/a | - | - | - | - | - | - | - | - | - |
| / | Fried Rice `farmersdelight:fried_rice` | Canned Fried Rice | n/a | - | - | - | - | - | - | - | - | - |
| / | Grilled Salmon `farmersdelight:grilled_salmon` | Canned Grilled Salmon | n/a | - | - | - | - | - | - | - | - | - |
| / | Grilled Strider `nethersdelight:grilled_strider` | Canned Grilled Strider | n/a | - | - | - | - | - | - | - | - | - |
| / | Horror Lasagna `brewinandchewin:horror_lasagna` | Canned Horror Lasagna | n/a | - | - | - | - | - | - | - | - | - |
| / | Lychee Chicken `fruitsdelight:lychee_chicken` | Canned Lychee Chicken | n/a | - | - | - | - | - | - | - | - | - |
| / | Mashed Potatoes `moredelight:mashed_potatoes` | Canned Mashed Potatoes | n/a | - | - | - | - | - | none | - | - | - |
| / | Mushroom Rice `farmersdelight:mushroom_rice` | Canned Mushroom Rice | n/a | - | - | - | - | - | - | - | - | - |
| / | Omelet `moredelight:omelette` | Canned Omelet | n/a | - | - | - | - | - | none | - | - | - |
| / | Orange Chicken `fruitsdelight:orange_chicken` | Canned Orange Chicken | n/a | - | - | - | - | - | - | - | - | - |
| / | Orange Marinated Pork `fruitsdelight:orange_marinated_pork` | Canned Orange Marinated Pork | n/a | - | - | - | - | - | - | - | - | - |
| / | Passion Fruit Glazed Duck `abnormals_delight:passion_fruit_glazed_duck` | Canned Passion Fruit Glazed Duck | n/a | - | - | - | - | - | - | - | - | - |
| / | Perch with Mushrooms `abnormals_delight:perch_with_mushrooms` | Canned Perch with Mushrooms | n/a | - | - | - | - | - | - | - | - | - |
| / | Pike with Beetroot `abnormals_delight:pike_with_beetroot` | Canned Pike with Beetroot | n/a | - | - | - | - | - | - | - | - | - |
| / | Pineapple Marinated Pork `fruitsdelight:pineapple_marinated_pork` | Canned Pineapple Marinated Pork | n/a | - | - | - | - | - | - | - | - | - |
| / | Ratatouille `farmersdelight:ratatouille` | Canned Ratatouille | n/a | - | - | - | - | - | - | - | - | - |
| / | Roasted Mutton Chops `farmersdelight:roasted_mutton_chops` | Canned Roasted Mutton Chops | n/a | - | - | - | - | - | - | - | - | - |
| / | Scarlet Pierogi `brewinandchewin:scarlet_pierogi` (was `scarlet_pierogies` before the Brewin 4.5.0 port) | Canned Scarlet Pierogi | n/a | - | - | - | - | - | - | - | - | - |
| / | Seared Venison `abnormals_delight:seared_venison` | Canned Seared Venison | n/a | - | - | - | - | - | - | - | - | - |
| / | Shrimp-Fried Rice `alexsmobs:shrimp_fried_rice` | Canned Shrimp-Fried Rice | n/a | - | - | - | - | - | none | - | - | - |
| / | Slabdish `abnormals_delight:slabdish` | Canned Slabdish | n/a | - | - | - | - | - | - | - | - | - |
| / | Steak and Potatoes `farmersdelight:steak_and_potatoes` | Canned Steak and Potatoes | n/a | - | - | - | - | - | - | - | - | - |
| / | Tea Curry `farmersrespite:tea_curry` | Canned Tea Curry | n/a | - | - | - | - | - | - | - | - | - |
| / | Twisted Cereal `endersdelight:twisted_cereal` | Canned Twisted Cereal | n/a | - | - | - | - | - | - | - | - | - |
| / | Vegetable Omelet `brewinandchewin:vegetable_omelet` | Canned Vegetable Omelet | n/a | - | - | - | - | - | - | - | - | - |
| / | Venison with Bamboo Shoots `abnormals_delight:venison_with_bamboo_shoots` | Canned Venison with Bamboo Shoots | n/a | - | - | - | - | - | - | - | - | - |
| / | Warped Moldy Meat `nethersdelight:warped_moldy_meat` | Canned Warped Moldy Meat | n/a | - | - | - | - | - | - | - | - | - |
| / | Ender Paella `endersdelight:ender_paella_wood` (wooden variant, removed) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Pearl Pasta `endersdelight:pearl_pasta_wood` (plan: pearl pasta, not the wooden one) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Twisted Cereal `endersdelight:twisted_cereal_wood` (wooden variant, removed) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.13 Pie <a id="pie"></a>

4 slices + can -> pie filling (4 servings, slice-sized meals); filling can + pie crust -> the whole pie. Fruitsdelight's
durian and pineapple pies are single items: 4 fill a can, no pie back.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Slice of Apple Pie `farmersdelight:apple_pie_slice` | Canned Apple Pie Filling | 4 | - | x | / | x | / | x | later | later | later |
| x | Slice of Blueberry Pie `delightful:blueberry_pie_slice` (no source in the pack: no whole pie, nothing drops it) | Canned Blueberry Pie Filling | - | - | / | / | / | / | / | later | later | later |
| x | Slice of Cherry Pie `create_central_kitchen:cherry_pie_slice` (no source in the pack: no whole pie, nothing drops it) | Canned Cherry Pie Filling | - | - | / | / | / | / | / | later | later | later |
| x | Slice of Chocolate Pie `farmersdelight:chocolate_pie_slice` | Canned Chocolate Pie Filling | 4 | - | x | / | x | / | x | later | later | later |
| x | Chorus Pie Slice `endersdelight:chorus_pie_slice` | Canned Chorus Pie Filling | 4 | - | x | / | x | / | x | later | later | later |
| x | Slice of Crimson Berry Pie `delightful:crimson_berry_pie_slice` (no source in the pack: no whole pie, nothing drops it) | Canned Crimson Berry Pie Filling | - | - | / | / | / | / | / | later | later | later |
| x | Durian Pie `fruitsdelight:durian_pie` | Canned Durian Pie Filling | 4 | - | x | / | x | / | x | later | later | later |
| / | Slice of Gloomgourd Pie `delightful:gloomgourd_pie_slice` | Canned Gloomgourd Pie Filling | n/a | - | / | / | / | / | / | later | later | later |
| x | Slice of Green Apple Pie `delightful:green_apple_pie_slice` (no source in the pack: no whole pie, nothing drops it) | Canned Green Apple Pie Filling | - | - | / | / | / | / | / | later | later | later |
| x | Slice of Mulberry Pie `create_central_kitchen:mulberry_pie_slice` | Canned Mulberry Pie Filling | 4 | - | x | / | x | / | x | later | later | later |
| x | Slice of Nightshade Berry Pie `delightful:nightshade_berry_pie_slice` (no source in the pack: no whole pie, nothing drops it) | Canned Nightshade Berry Pie Filling | - | - | / | / | / | / | / | later | later | later |
| x | Pineapple Pie `fruitsdelight:pineapple_pie` | Canned Pineapple Pie Filling | 4 | - | x | / | x | / | x | later | later | later |
| x | Slice of Pumpkin Pie `delightful:pumpkin_pie_slice` | Canned Pumpkin Pie Filling | 4 | - | x | / | x | / | x | later | later | later |
| x | Slice of Pumpkin Pie `create_central_kitchen:pumpkin_pie_slice` | Canned Pumpkin Pie Filling | 4 | - | x | / | x | / | x | later | later | later |
| x | Rose Hip Pie Slice `farmersrespite:rose_hip_pie_slice` | Canned Rose Hip Pie Filling | 4 | - | x | / | x | / | x | later | later | later |
| x | Slice of Salmonberry Pie `delightful:salmonberry_pie_slice` | Canned Salmonberry Pie Filling | 4 | - | x | / | x | / | x | later | later | later |
| / | Slice of Source Berry Pie `delightful:source_berry_pie_slice` | Canned Source Berry Pie Filling | n/a | - | / | / | / | / | / | later | later | later |
| x | Slice of Truffle Pie `create_central_kitchen:truffle_pie_slice` (no source in the pack: no whole pie, nothing drops it) | Canned Truffle Pie Filling | - | - | / | / | / | / | / | later | later | later |
| x | Fig Tart `fruitsdelight:fig_tart` | Canned Fig Tart Filling | - | - | - | - | - | - | - | - | - | - |
| X | Lemon Tart `fruitsdelight:lemon_tart` | Canned Lemon Tart Filling | - | - | - | - | - | - | - | - | - | - |

- move all tarts to pies, renaame and retexture, recipe parity w/ other pies
- ensure all pies are valid (truffle, nightshade, crimson berry, source berry), identify mod that supplies missing
  - Checked 2026-10-13 against the item and recipe exports: Delightful registers blueberry, crimson berry, green apple,
    nightshade, gloomgourd and source berry slices but none of their whole pies, and Central Kitchen's cherry and
    truffle slices only have "incomplete" pies. Nothing in the pack makes any of these 8 slices. Delightful's whole
    pies come with its compat mods, Central Kitchen's with the mods that add cherry and truffle pies (not installed). The
    obtainable pies are apple, chocolate (FD), chorus (Ender's Delight), mulberry (Upgrade Aquatic -> Central Kitchen
    slice), pumpkin (vanilla -> Delightful slice), rose hip (Farmer's Respite), salmonberry (Delightful), durian and
    pineapple (Fruits Delight).

### 2.14 Feasts <a id="feast"></a>

Placeholder goal: one placed feast block + can -> 6 servings worth the feast's serving (a placed feast gives 4). The
three stuffed hoglin plates share one Hoglin Feast can (block nethersdelight:stuffed_hoglin, valued as the ham plate).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Caramel Popcorn `corn_delight:caramel_popcorn` | Canned Caramel Popcorn | 1 (placed feast block) | - | x | / | x | / | none | later | later | later |
| x | Plate of Stuffed Hoglin `nethersdelight:plate_of_stuffed_hoglin_*` | Canned Hoglin Feast | 1 (placed feast block) | - | x | / | x | / | x | later | later | later |
| / | Plate of Stuffed Hoglin Ham `nethersdelight:plate_of_stuffed_hoglin_ham` (shares Canned Hoglin Feast) | Canned Hoglin Feast | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Plate of Stuffed Hoglin Roast `nethersdelight:plate_of_stuffed_hoglin_roast` (shares Canned Hoglin Feast) | Canned Hoglin Feast | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Plate of Stuffed Hoglin Snout `nethersdelight:plate_of_stuffed_hoglin_snout` (shares Canned Hoglin Feast) | Canned Hoglin Feast | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Plate of Honey Glazed Ham `farmersdelight:honey_glazed_ham` | Canned Honey Glazed Ham | 1 (placed feast block) | - | x | / | x | / | x | later | later | later |
| x | Nachos `corn_delight:nachos_bowl` | Canned Nachos | 1 (placed feast block) | - | x | / | x | / | x | later | later | later |
| x | Bowl of Pineapple Fried Rice `fruitsdelight:bowl_of_pineapple_fried_rice` | Canned Pineapple Fried Rice | 1 (placed feast block) | - | x | / | x | / | x | later | later | later |
| x | Plate of Roast Chicken `farmersdelight:roast_chicken` | Canned Roast Chicken | 1 (placed feast block) | - | x | / | x | / | x | later | later | later |
| x | Plate of Shepherd's Pie `farmersdelight:shepherds_pie` | Canned Shepherd's Pie | 1 (placed feast block) | - | x | / | x | / | x | later | later | later |
| x | Bowl of Stuffed Pumpkin `farmersdelight:stuffed_pumpkin` | Canned Stuffed Pumpkin | 1 (placed feast block) | - | x | / | x | / | x | later | later | later |
| x | Bowl Of Stuffed Shulker `endersdelight:stuffed_shulker_bowl` | Canned Stuffed Shulker | 1 (placed feast block) | - | x | - | x | / | x | later | later | later |

### 2.15 Drinks <a id="drink"></a>

Placeholder goal: 4 -> 4 servings. Honey reuses Canned Goods' can (its code clears poison); Canned Powdered Milk takes 4
milk buckets, 1 hunger, always edible, clears every effect.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Honey Bottle `minecraft:honey_bottle` | Canned Honey | 4 | - | x | x | x | / | none | later | later | later |
| x | Milk Bucket `minecraft:milk_bucket` | Canned Powdered Milk | 4 | - | x | / | x | / | none | later | later | later |
| / | Adzuki Milkshake `neapolitan:adzuki_milkshake` | Canned Adzuki Milkshake | n/a | - | - | - | - | - | - | - | - | - |
| / | Apple Cider `farmersdelight:apple_cider` | Canned Apple Cider | n/a | - | - | - | - | - | - | - | - | - |
| / | Apple Cider `farmersrespite:long_apple_cider` | Canned Apple Cider | n/a | - | - | - | - | - | - | - | - | - |
| / | Apple Cider `farmersrespite:strong_apple_cider` | Canned Apple Cider | n/a | - | - | - | - | - | - | - | - | - |
| / | Azalea Tea `delightful:azalea_tea` | Canned Azalea Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Banana Milkshake `neapolitan:banana_milkshake` | Canned Banana Milkshake | n/a | - | - | - | - | - | - | - | - | - |
| / | Bellini Cocktail `fruitsdelight:bellini_cocktail` | Canned Bellini Cocktail | n/a | - | - | - | - | - | - | - | - | - |
| / | Berry Matcha Latte `delightful:berry_matcha_latte` | Canned Berry Matcha Latte | n/a | - | - | - | - | - | - | - | - | - |
| / | Black Tea `farmersrespite:black_tea` | Canned Black Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Black Tea `farmersrespite:long_black_tea` | Canned Black Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Black Tea `farmersrespite:strong_black_tea` | Canned Black Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Chocolate Coffee `rusticdelight:chocolate_coffee` | Canned Chocolate Coffee | n/a | - | - | - | - | - | - | - | - | - |
| / | Chocolate Milkshake `neapolitan:chocolate_milkshake` | Canned Chocolate Milkshake | n/a | - | - | - | - | - | - | - | - | - |
| / | Chorus Juice `endersdelight:chorus_juice` | Canned Chorus Juice | n/a | - | - | - | - | - | - | - | - | - |
| / | Cloud Latte `quarkdelight:cloud_latte` | Canned Cloud Latte | n/a | - | - | - | - | - | none | - | - | - |
| / | Coffee `rusticdelight:coffee` | Canned Coffee | n/a | - | - | - | - | - | - | - | - | - |
| / | Coffee `farmersrespite:coffee` | Canned Coffee | n/a | - | - | - | - | - | - | - | - | - |
| / | Coffee `farmersrespite:long_coffee` | Canned Coffee | n/a | - | - | - | - | - | - | - | - | - |
| / | Coffee `farmersrespite:strong_coffee` | Canned Coffee | n/a | - | - | - | - | - | - | - | - | - |
| / | Creamy Corn Drink `corn_delight:creamy_corn_drink` | Canned Creamy Corn Drink | n/a | - | - | - | - | - | - | - | - | - |
| / | Dandelion Tea `farmersrespite:dandelion_tea` | Canned Dandelion Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Dandelion Tea `farmersrespite:long_dandelion_tea` | Canned Dandelion Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Dark Coffee `rusticdelight:dark_coffee` | Canned Dark Coffee | n/a | - | - | - | - | - | - | - | - | - |
| / | Ender Nectar `delightful:ender_nectar` | Canned Ender Nectar | n/a | - | - | - | - | - | - | - | - | - |
| / | Green Tea `farmersrespite:green_tea` | Canned Green Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Green Tea `farmersrespite:long_green_tea` | Canned Green Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Green Tea `farmersrespite:strong_green_tea` | Canned Green Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Hamimelon Juice `fruitsdelight:hamimelon_juice` | Canned Hamimelon Juice | n/a | - | - | - | - | - | - | - | - | - |
| / | Hawberry Tea `fruitsdelight:hawberry_tea` | Canned Hawberry Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Honey Coffee `rusticdelight:honey_coffee` | Canned Honey Coffee | n/a | - | - | - | - | - | - | - | - | - |
| / | Hot Chocolate `create_confectionery:hot_chocolate_bottle` | Canned Hot Chocolate | n/a | - | - | - | - | - | none | - | - | - |
| / | Kiwi Juice `fruitsdelight:kiwi_juice` | Canned Kiwi Juice | n/a | - | - | - | - | - | - | - | - | - |
| / | Lavender Tea `delightful:lavender_tea` | Canned Lavender Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Lemon Juice `fruitsdelight:lemon_juice` | Canned Lemon Juice | n/a | - | - | - | - | - | - | - | - | - |
| / | Lychee Cherry Tea `fruitsdelight:lychee_cherry_tea` | Canned Lychee Cherry Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Mango Milkshake `fruitsdelight:mango_milkshake` | Canned Mango Milkshake | n/a | - | - | - | - | - | - | - | - | - |
| / | Mango Tea `fruitsdelight:mango_tea` | Canned Mango Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Mangosteen Tea `fruitsdelight:mangosteen_tea` | Canned Mangosteen Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Matcha Latte `delightful:matcha_latte` | Canned Matcha Latte | n/a | - | - | - | - | - | - | - | - | - |
| / | Milk Coffee `rusticdelight:milk_coffee` | Canned Milk Coffee | n/a | - | - | - | - | - | - | - | - | - |
| / | Mint Milkshake `neapolitan:mint_milkshake` | Canned Mint Milkshake | n/a | - | - | - | - | - | - | - | - | - |
| / | Oculemonade `canned_goods:oculemonade` | Canned Oculemonade | n/a | - | - | - | - | - | - | - | - | - |
| / | Orange Juice `fruitsdelight:orange_juice` | Canned Orange Juice | n/a | - | - | - | - | - | - | - | - | - |
| / | Peach Tea `fruitsdelight:peach_tea` | Canned Peach Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Pear Juice `fruitsdelight:pear_juice` | Canned Pear Juice | n/a | - | - | - | - | - | - | - | - | - |
| / | Prickly Pear Juice `delightful:prickly_pear_juice` | Canned Prickly Pear Juice | n/a | - | - | - | - | - | - | - | - | - |
| / | Purulent Tea `farmersrespite:long_purulent_tea` | Canned Purulent Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Purulent Tea `farmersrespite:purulent_tea` | Canned Purulent Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Purulent Tea `farmersrespite:strong_purulent_tea` | Canned Purulent Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Reef-Cola `canned_goods:reef_cola` | Canned Reef-Cola | n/a | - | - | - | - | - | - | - | - | - |
| / | Rose Hip Tea `farmersrespite:long_rose_hip_tea` | Canned Rose Hip Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Rose Hip Tea `farmersrespite:rose_hip_tea` | Canned Rose Hip Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Rose Hip Tea `farmersrespite:strong_rose_hip_tea` | Canned Rose Hip Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Soda `canned_goods:soda` | Canned Soda | n/a | - | - | - | - | - | - | - | - | - |
| / | Soothing Hot Chocolate `create_confectionery:soothing_hot_chocolate` | Canned Soothing Hot Chocolate | n/a | - | - | - | - | - | none | - | - | - |
| / | Strawberry Banana Smoothie `neapolitan:strawberry_banana_smoothie` | Canned Strawberry Banana Smoothie | n/a | - | - | - | - | - | - | - | - | - |
| / | Strawberry Milkshake `neapolitan:strawberry_milkshake` | Canned Strawberry Milkshake | n/a | - | - | - | - | - | none | - | - | - |
| / | Vanilla Milkshake `neapolitan:vanilla_milkshake` | Canned Vanilla Milkshake | n/a | - | - | - | - | - | - | - | - | - |
| / | Yellow Tea `farmersrespite:long_yellow_tea` | Canned Yellow Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Yellow Tea `farmersrespite:strong_yellow_tea` | Canned Yellow Tea | n/a | - | - | - | - | - | - | - | - | - |
| / | Yellow Tea `farmersrespite:yellow_tea` | Canned Yellow Tea | n/a | - | - | - | - | - | - | - | - | - |

### 2.16 Drinks: alcohol <a id="alcohol"></a>

Not in the canning.js plan; listed so the family is complete.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| / | A Bottle of 'Creepers Crush' `vinery:creepers_crush` | Canned A Bottle of 'Creepers Crush' | n/a | - | - | - | - | - | - | - | - | - |
| / | A Bottle of 'Mojang Noir' `vinery:bottle_mojang_noir` | Canned A Bottle of 'Mojang Noir' | n/a | - | - | - | - | - | - | - | - | - |
| / | A Bottle of 'Villagers Fright' `vinery:villagers_fright` | Canned A Bottle of 'Villagers Fright' | n/a | - | - | - | - | - | - | - | - | - |
| / | Aegis Wine `vinery:aegis_wine` | Canned Aegis Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Apple Wine `vinery:apple_wine` | Canned Apple Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Bloody Mary `brewinandchewin:bloody_mary` | Canned Bloody Mary | n/a | - | - | - | - | - | - | - | - | - |
| / | Bolvar Wine `vinery:bolvar_wine` | Canned Bolvar Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Bottle of Wodka `tmted:bottle_of_wodka` | Canned Wodka | n/a | - | - | - | - | - | none | - | - | - |
| / | Chenet Wine `vinery:chenet_wine` | Canned Chenet Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Cherry Wine `vinery:cherry_wine` | Canned Cherry Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Chorus Wine `vinery:chorus_wine` | Canned Chorus Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Cider `vinery:apple_cider` | Canned Cider | n/a | - | - | - | - | - | - | - | - | - |
| / | Clark Wine `vinery:clark_wine` | Canned Clark Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Cristel Wine `vinery:cristel_wine` | Canned Cristel Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Egg Grog `brewinandchewin:egg_grog` | Canned Egg Grog | n/a | - | - | - | - | - | - | - | - | - |
| / | Eiswein `vinery:eiswein` | Canned Eiswein | n/a | - | - | - | - | - | - | - | - | - |
| / | Glittering Grenadine `brewinandchewin:glittering_grenadine` | Canned Glittering Grenadine | n/a | - | - | - | - | - | - | - | - | - |
| / | Jellie Wine `vinery:jellie_wine` | Canned Jellie Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Jo's Special Mixture `vinery:jo_special_mixture` | Canned Jo's Special Mixture | n/a | - | - | - | - | - | - | - | - | - |
| / | Kelp Cider `vinery:kelp_cider` | Canned Kelp Cider | n/a | - | - | - | - | - | - | - | - | - |
| / | Kombucha `brewinandchewin:kombucha` | Canned Kombucha | n/a | - | - | - | - | - | - | - | - | - |
| / | Magnetic Wine `vinery:magnetic_wine` | Canned Magnetic Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Mead `brewinandchewin:mead` | Canned Mead | n/a | - | - | - | - | - | - | - | - | - |
| / | Mead `vinery:mead` (gone 2026-10-20: the Vinery port -04 dropped its duplicate; Brewin's Mead stays) | n/a | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Mellohi Wine `vinery:mellohi_wine` | Canned Mellohi Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Miss Lilitus Wine `vinery:lilitu_wine` (was `king_danis_wine` before the Vinery 1.5.5 port -04) | Canned Miss Lilitus Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Noir Wine `vinery:noir_wine` | Canned Noir Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Pale Jane `brewinandchewin:pale_jane` | Canned Pale Jane | n/a | - | - | - | - | - | - | - | - | - |
| / | Praetorian Wine `vinery:praetorian_wine` | Canned Praetorian Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Red Rum `brewinandchewin:red_rum` | Canned Red Rum | n/a | - | - | - | - | - | - | - | - | - |
| / | Red Wine `vinery:red_wine` | Canned Red Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Rice Wine `brewinandchewin:rice_wine` | Canned Rice Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Saccharine Rum `brewinandchewin:saccharine_rum` | Canned Saccharine Rum | n/a | - | - | - | - | - | - | - | - | - |
| / | Salty Folly `brewinandchewin:salty_folly` | Canned Salty Folly | n/a | - | - | - | - | - | - | - | - | - |
| / | Solaris Wine `vinery:solaris_wine` | Canned Solaris Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Stal Wine `vinery:stal_wine` | Canned Stal Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Steel Toe Stout `brewinandchewin:steel_toe_stout` | Canned Steel Toe Stout | n/a | - | - | - | - | - | - | - | - | - |
| / | Strad Wine `vinery:strad_wine` | Canned Strad Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Strongroot Ale `brewinandchewin:strongroot_ale` | Canned Strongroot Ale | n/a | - | - | - | - | - | - | - | - | - |
| / | Sunkissed Wine `vinery:glowing_wine` | Canned Sunkissed Wine | n/a | - | - | - | - | - | - | - | - | - |
| / | Withering Dross `brewinandchewin:withering_dross` | Canned Withering Dross | n/a | - | - | - | - | - | - | - | - | - |
| / | Beer `brewinandchewin:beer` | Canned Beer | - | - | - | - | - | - | - | - | - | - |
| / | Dread Nog `brewinandchewin:dread_nog` | Canned Dread Nog | - | - | - | - | - | - | - | - | - | - |
| / | Vodka `brewinandchewin:vodka` | Canned Vodka | - | - | - | - | - | - | - | - | - | - |

- remove wodka

### 2.17 Golden and enchanted golden <a id="golden"></a>

Golden x0.25 meals, 10 servings, effects 0.25x, 2 golden + honey bottle.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Enchanted Golden Apple `minecraft:enchanted_golden_apple` | Canned Enchanted Golden Apple | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Baked Potato `goldenfood:enchanted_golden_baked_potato` | Canned Enchanted Golden Baked Potato | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Beetroot `goldenfood:enchanted_golden_beetroot` | Canned Enchanted Golden Beetroot | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Beetroot Soup `goldenfood:enchanted_golden_beetroot_soup` | Canned Enchanted Golden Beetroot Soup | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Bread `goldenfood:enchanted_golden_bread` | Canned Enchanted Golden Bread | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Carrot `goldenfood:enchanted_golden_carrot` | Canned Enchanted Golden Carrot | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Chorus Fruit `goldenfood:enchanted_golden_chorus_fruit` | Canned Enchanted Golden Chorus Fruit | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Beef `goldenfood:enchanted_golden_cooked_beef` | Canned Enchanted Golden Cooked Beef | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Chicken `goldenfood:enchanted_golden_cooked_chicken` | Canned Enchanted Golden Cooked Chicken | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Cod `goldenfood:enchanted_golden_cooked_cod` | Canned Enchanted Golden Cooked Cod | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Mutton `goldenfood:enchanted_golden_cooked_mutton` | Canned Enchanted Golden Cooked Mutton | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Porkchop `goldenfood:enchanted_golden_cooked_porkchop` | Canned Enchanted Golden Cooked Porkchop | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Rabbit `goldenfood:enchanted_golden_cooked_rabbit` | Canned Enchanted Golden Cooked Rabbit | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Salmon `goldenfood:enchanted_golden_cooked_salmon` | Canned Enchanted Golden Cooked Salmon | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cookie `goldenfood:enchanted_golden_cookie` | Canned Enchanted Golden Cookie | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Dried Kelp `goldenfood:enchanted_golden_dried_kelp` | Canned Enchanted Golden Dried Kelp | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Glow Berries `goldenfood:enchanted_golden_glow_berries` | Canned Enchanted Golden Glow Berries | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Honey Bottle `goldenfood:enchanted_golden_honey_bottle` | Canned Enchanted Golden Honey Bottle | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Melon Slice `goldenfood:enchanted_golden_melon_slice` | Canned Enchanted Golden Melon Slice | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Milk Bucket `goldenfood:enchanted_golden_milk_bucket` | Canned Enchanted Golden Milk Bucket | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Moss Clump `immersive_weathering:enchanted_golden_moss_clump` | Canned Enchanted Golden Moss Clump | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Mushroom Stew `goldenfood:enchanted_golden_mushroom_stew` | Canned Enchanted Golden Mushroom Stew | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Pumpkin Pie `goldenfood:enchanted_golden_pumpkin_pie` | Canned Enchanted Golden Pumpkin Pie | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Rabbit Stew `goldenfood:enchanted_golden_rabbit_stew` | Canned Enchanted Golden Rabbit Stew | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Sweet Berries `goldenfood:enchanted_golden_sweet_berries` | Canned Enchanted Golden Sweet Berries | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Apple `minecraft:golden_apple` | Canned Golden Apple | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Baked Potato `goldenfood:golden_baked_potato` | Canned Golden Baked Potato | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Beetroot `goldenfood:golden_beetroot` | Canned Golden Beetroot | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Beetroot Soup `goldenfood:golden_beetroot_soup` | Canned Golden Beetroot Soup | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Bread `goldenfood:golden_bread` | Canned Golden Bread | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Carrot `minecraft:golden_carrot` | Canned Golden Carrot | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Chorus Fruit `goldenfood:golden_chorus_fruit` | Canned Golden Chorus Fruit | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Beef `goldenfood:golden_cooked_beef` | Canned Golden Cooked Beef | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Chicken `goldenfood:golden_cooked_chicken` | Canned Golden Cooked Chicken | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Cod `goldenfood:golden_cooked_cod` | Canned Golden Cooked Cod | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Mutton `goldenfood:golden_cooked_mutton` | Canned Golden Cooked Mutton | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Porkchop `goldenfood:golden_cooked_porkchop` | Canned Golden Cooked Porkchop | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Rabbit `goldenfood:golden_cooked_rabbit` | Canned Golden Cooked Rabbit | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Salmon `goldenfood:golden_cooked_salmon` | Canned Golden Cooked Salmon | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Cookie `goldenfood:golden_cookie` | Canned Golden Cookie | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Dried Kelp `goldenfood:golden_dried_kelp` | Canned Golden Dried Kelp | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Glow Berries `goldenfood:golden_glow_berries` | Canned Golden Glow Berries | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Honey Bottle `goldenfood:golden_honey_bottle` | Canned Golden Honey Bottle | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Melon Slice `goldenfood:golden_melon_slice` | Canned Golden Melon Slice | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Milk Bucket `goldenfood:golden_milk_bucket` | Canned Golden Milk Bucket | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Moss Clump `immersive_weathering:golden_moss_clump` | Canned Golden Moss Clump | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Mushroom Stew `goldenfood:golden_mushroom_stew` | Canned Golden Mushroom Stew | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Pumpkin Pie `goldenfood:golden_pumpkin_pie` | Canned Golden Pumpkin Pie | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Rabbit Stew `goldenfood:golden_rabbit_stew` | Canned Golden Rabbit Stew | None / 2+1 | - | x | / | x | / | x | later | later | later |
| x | Golden Sweet Berries `goldenfood:golden_sweet_berries` | Canned Golden Sweet Berries | None / 2+1 | - | x | / | x | / | x | later | later | later |

### 2.18 Special <a id="special"></a>

Fruit Meals like raw fruit (section special). Chorus reuses Canned Goods' can (its code teleports).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Ancient Fruit `quark:ancient_fruit` | Canned Ancient Fruit | 3 / 2+1 | - | x | / | x | / | none | later | later | later |
| x | Chorus Fruit `minecraft:chorus_fruit` | Canned Chorus Fruit | 3 / 2+1 | - | x | x | x | / | none | later | later | later |
| / | Azalea Flowers `immersive_weathering:azalea_flowers` | Canned Azalea Flowers | n/a | - | - | - | - | - | none | - | - | - |
| / | Biscuit of Totality `botania:mana_cookie` | Canned Biscuit of Totality | n/a | - | - | - | - | - | - | - | - | - |
| / | Cosmic Cod `alexsmobs:cosmic_cod` | Canned Cosmic Cod | n/a | - | - | - | - | - | - | - | - | - |
| / | Dog Food `farmersdelight:dog_food` | Canned Dog Food | n/a | - | - | - | - | - | none | - | - | - |
| / | Fish Oil `alexsmobs:fish_oil` | Canned Fish Oil | n/a | - | - | - | - | - | none | - | - | - |
| / | Ice Cubes `neapolitan:ice_cubes` | Canned Ice Cubes | n/a | - | - | - | - | - | none | - | - | - |
| / | Ice Sickle `immersive_weathering:ice_sickle` | Canned Ice Sickle | n/a | - | - | - | - | - | - | - | - | - |
| / | Icicle `immersive_weathering:icicle` | Canned Icicle | n/a | - | - | - | - | - | - | - | - | - |
| / | Magma Gelatin `nethersdelight:magma_gelatin` | Canned Magma Gelatin | n/a | - | - | - | - | - | - | - | - | - |
| / | Mana in a Bottle `botania:mana_bottle` | Canned Mana in a Bottle | n/a | - | - | - | - | - | none | - | - | - |
| / | Moss Clump `immersive_weathering:moss_clump` | Canned Moss Clump | n/a | - | - | - | - | - | none | - | - | - |
| / | Propelpearl `nethersdelight:propelpearl` | Canned Propelpearl | n/a | - | - | - | - | - | none | - | - | - |
| / | Rainbow Jelly `alexsmobs:rainbow_jelly` | Canned Rainbow Jelly | n/a | - | - | - | - | - | none | - | - | - |


### 2.19 Hazard <a id="hazard"></a>

The four built cans are Canned Goods' own (section hazard in [can_types.json](../kubejs/generation_scripts/food/can_types.json)): Meat Meal (5, 3) x 4 servings, pufferfish the Fish Meal (4, 2), no additive; Canned Goods' code applies the bad effects per serving. Added as a family 2026-10-20 (it was in [3](#outside)); the rest are new potential cans.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Poisonous Potato `minecraft:poisonous_potato` | Canned Poisonous Potatos | 8 | - | x | x | x | / | x | later | later | later |
| x | Pufferfish `minecraft:pufferfish` | Canned Pufferfishs | 8 | - | x | x | x | / | x | later | later | later |
| x | Rotten Flesh `minecraft:rotten_flesh` | Canned Tough Jerky | 5 | - | x | x | x | / | x | later | later | later |
| x | Spider Eye `minecraft:spider_eye` | Canned Night Berries | 5 | - | x | x | x | / | x | later | later | later |
| / | Copper Nugget `everythingcopper:copper_nugget` | Canned Copper Nugget | - | - | - | - | - | - | - | - | - | - |
| x | Maggot `alexsmobs:maggot` | Canned Maggot | - | - | - | - | - | - | none | - | - | - |
| / | Pond Water `immersive_weathering:pond_water` | Canned Pond Water | - | - | - | - | - | - | - | - | - | - |
| / | Rotten Apple `domesticationinnovation:rotten_apple` | Canned Rotten Apple | - | - | - | - | - | - | - | - | - | - |
| / | Rotten Cherry `vinery:rotten_cherry` | Canned Rotten Cherry | - | - | - | - | - | - | - | - | - | - |
| / | Sinister Carrot `domesticationinnovation:sinister_carrot` | Canned Sinister Carrot | - | - | - | - | - | - | - | - | - | - |
| / | Soap `supplementaries:soap` | Canned Soap | - | - | - | - | - | - | - | - | - | - |

### 2.20 Egg and dairy <a id="egg-dairy"></a>

Added 2026-10-20 from [3](#outside): cooked eggs and the Brewin cheese wedges. All undecided.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| / | Boiled Emu Egg `alexsmobs:boiled_emu_egg` | Canned Boiled Emu Egg | - | - | - | - | - | - | none | - | - | - |
| / | Fried Egg `farmersdelight:fried_egg` | Canned Fried Egg | - | - | - | - | - | - | none | - | - | - |
| / | Fried Egg `incubation:fried_egg` | Canned Fried Egg | - | - | - | - | - | - | none | - | - | - |
| / | Scrambled Eggs `incubation:scrambled_eggs` | Canned Scrambled Eggs | - | - | - | - | - | - | none | - | - | - |
| x* | Flaxen Cheese Wedge `brewinandchewin:flaxen_cheese_wedge` | Canned Flaxen Cheese Wedge | - | - | - | - | - | - | none | - | - | - |
| x* | Scarlet Cheese Wedge `brewinandchewin:scarlet_cheese_wedge` | Canned Scarlet Cheese Wedge | - | - | - | - | - | - | none | - | - | - |

*needs pot cooking recipe

### 2.21 Preserves <a id="preserves"></a>

Added 2026-10-20 from the ingredient type: jams, jellies, nut butter, tomato sauce. All undecided. Most are bottles (the can would hand back the glass bottles, like Canned Honey).

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| / | Apple Jam `fruitsdelight:apple_jelly` | Canned Apple Jam | - | - | - | - | - | - | - | - | - | - |
| / | Bayberry Jam `fruitsdelight:bayberry_jelly` | Canned Bayberry Jam | - | - | - | - | - | - | - | - | - | - |
| / | Blueberry Jam `fruitsdelight:blueberry_jelly` | Canned Blueberry Jam | - | - | - | - | - | - | - | - | - | - |
| / | Chorus Jam `fruitsdelight:chorus_jelly` | Canned Chorus Jam | - | - | - | - | - | - | - | - | - | - |
| / | Cranberry Jam `fruitsdelight:cranberry_jelly` | Canned Cranberry Jam | - | - | - | - | - | - | - | - | - | - |
| / | Durian Jam `fruitsdelight:durian_jelly` | Canned Durian Jam | - | - | - | - | - | - | - | - | - | - |
| / | Fig Jam `fruitsdelight:fig_jelly` | Canned Fig Jam | - | - | - | - | - | - | - | - | - | - |
| / | Glow Jam Jar `delightful:glow_jelly_bottle` | Canned Glow Jam | - | - | - | - | - | - | - | - | - | - |
| / | Glowberry Jam `fruitsdelight:glowberry_jelly` | Canned Glowberry Jam | - | - | - | - | - | - | - | - | - | - |
| / | Hamimelon Jam `fruitsdelight:hamimelon_jelly` | Canned Hamimelon Jam | - | - | - | - | - | - | - | - | - | - |
| / | Hawberry Jam `fruitsdelight:hawberry_jelly` | Canned Hawberry Jam | - | - | - | - | - | - | - | - | - | - |
| / | Jam Jar `delightful:jelly_bottle` | Canned Jam | - | - | - | - | - | - | none | - | - | - |
| / | Kiwi Jam `fruitsdelight:kiwi_jelly` | Canned Kiwi Jam | - | - | - | - | - | - | - | - | - | - |
| / | Lemon Jam `fruitsdelight:lemon_jelly` | Canned Lemon Jam | - | - | - | - | - | - | - | - | - | - |
| / | Lychee Jam `fruitsdelight:lychee_jelly` | Canned Lychee Jam | - | - | - | - | - | - | - | - | - | - |
| / | Mango Jam `fruitsdelight:mango_jelly` | Canned Mango Jam | - | - | - | - | - | - | - | - | - | - |
| / | Mangosteen Jam `fruitsdelight:mangosteen_jelly` | Canned Mangosteen Jam | - | - | - | - | - | - | - | - | - | - |
| / | Melon Jam `fruitsdelight:melon_jelly` | Canned Melon Jam | - | - | - | - | - | - | - | - | - | - |
| / | Mulberry Jam Bottle `upgrade_aquatic:mulberry_jam_bottle` | Canned Mulberry Jam | - | - | - | - | - | - | none | - | - | - |
| / | Nut Butter Bottle `delightful:nut_butter_bottle` | Canned Nut Butter | - | - | - | - | - | - | none | - | - | - |
| / | Orange Jam `fruitsdelight:orange_jelly` | Canned Orange Jam | - | - | - | - | - | - | - | - | - | - |
| / | Peach Jam `fruitsdelight:peach_jelly` | Canned Peach Jam | - | - | - | - | - | - | - | - | - | - |
| / | Pear Jam `fruitsdelight:pear_jelly` | Canned Pear Jam | - | - | - | - | - | - | - | - | - | - |
| / | Persimmon Jam `fruitsdelight:persimmon_jelly` | Canned Persimmon Jam | - | - | - | - | - | - | - | - | - | - |
| / | Pineapple Jam `fruitsdelight:pineapple_jelly` | Canned Pineapple Jam | - | - | - | - | - | - | - | - | - | - |
| / | Sweetberry Jam `fruitsdelight:sweetberry_jelly` | Canned Sweetberry Jam | - | - | - | - | - | - | none | - | - | - |
| x | Tomato Sauce `farmersdelight:tomato_sauce` | Canned Tomato Sauce | - | - | - | - | - | - | none | - | - | - |

### 2.22 Desserts <a id="desserts"></a>

Added 2026-10-20 from the sweet type: jello, custard, pudding, ice cream, gelatine, glowgurt. All undecided.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| / | Adzuki Ice Cream `neapolitan:adzuki_ice_cream` | Canned Adzuki Ice Cream | - | - | - | - | - | - | - | - | - | - |
| / | Apple Jello `fruitsdelight:apple_jello` | Canned Apple Jello | - | - | - | - | - | - | - | - | - | - |
| / | Banana Ice Cream `neapolitan:banana_ice_cream` | Canned Banana Ice Cream | - | - | - | - | - | - | - | - | - | - |
| / | Bayberry Jello `fruitsdelight:bayberry_jello` | Canned Bayberry Jello | - | - | - | - | - | - | - | - | - | - |
| / | Blueberry Custard `fruitsdelight:blueberry_custard` | Canned Blueberry Custard | - | - | - | - | - | - | - | - | - | - |
| / | Blueberry Jello `fruitsdelight:blueberry_jello` | Canned Blueberry Jello | - | - | - | - | - | - | - | - | - | - |
| / | Bucket of Sweet Gelatine `quarkdelight:bucket_of_sweet_gelatine` | Canned Sweet Gelatine | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Ice Cream `neapolitan:chocolate_ice_cream` | Canned Chocolate Ice Cream | - | - | - | - | - | - | - | - | - | - |
| / | Chorus Jello `fruitsdelight:chorus_jello` | Canned Chorus Jello | - | - | - | - | - | - | - | - | - | - |
| / | Cranberry Jello `fruitsdelight:cranberry_jello` | Canned Cranberry Jello | - | - | - | - | - | - | - | - | - | - |
| / | Durian Jello `fruitsdelight:durian_jello` | Canned Durian Jello | - | - | - | - | - | - | - | - | - | - |
| / | Fig Jello `fruitsdelight:fig_jello` | Canned Fig Jello | - | - | - | - | - | - | - | - | - | - |
| / | Fig Pudding Slice `fruitsdelight:fig_pudding_slice` | Canned Fig Pudding | - | - | - | - | - | - | - | - | - | - |
| / | Glow Berry Custard `farmersdelight:glow_berry_custard` | Canned Glow Berry Custard | - | - | - | - | - | - | - | - | - | - |
| / | Glowberry Jello `fruitsdelight:glowberry_jello` | Canned Glowberry Jello | - | - | - | - | - | - | - | - | - | - |
| / | Glowgurt `berry_good:glowgurt` | Canned Glowgurt | - | - | - | - | - | - | - | - | - | - |
| / | Hamimelon Jello `fruitsdelight:hamimelon_jello` | Canned Hamimelon Jello | - | - | - | - | - | - | - | - | - | - |
| / | Hawberry Jello `fruitsdelight:hawberry_jello` | Canned Hawberry Jello | - | - | - | - | - | - | - | - | - | - |
| / | Kiwi Jello `fruitsdelight:kiwi_jello` | Canned Kiwi Jello | - | - | - | - | - | - | - | - | - | - |
| / | Lemon Jello `fruitsdelight:lemon_jello` | Canned Lemon Jello | - | - | - | - | - | - | - | - | - | - |
| / | Lychee Jello `fruitsdelight:lychee_jello` | Canned Lychee Jello | - | - | - | - | - | - | - | - | - | - |
| / | Mango Jello `fruitsdelight:mango_jello` | Canned Mango Jello | - | - | - | - | - | - | - | - | - | - |
| / | Mangosteen Jello `fruitsdelight:mangosteen_jello` | Canned Mangosteen Jello | - | - | - | - | - | - | - | - | - | - |
| / | Matcha Ice Cream `delightful:matcha_ice_cream` | Canned Matcha Ice Cream | - | - | - | - | - | - | - | - | - | - |
| / | Melon Jello `fruitsdelight:melon_jello` | Canned Melon Jello | - | - | - | - | - | - | - | - | - | - |
| / | Mint Ice Cream `neapolitan:mint_ice_cream` | Canned Mint Ice Cream | - | - | - | - | - | - | - | - | - | - |
| / | Neapolitan Ice Cream `neapolitan:neapolitan_ice_cream` | Canned Neapolitan Ice Cream | - | - | - | - | - | - | - | - | - | - |
| / | Orange Jello `fruitsdelight:orange_jello` | Canned Orange Jello | - | - | - | - | - | - | - | - | - | - |
| / | Peach Jello `fruitsdelight:peach_jello` | Canned Peach Jello | - | - | - | - | - | - | - | - | - | - |
| / | Pear Jello `fruitsdelight:pear_jello` | Canned Pear Jello | - | - | - | - | - | - | - | - | - | - |
| / | Persimmon Jello `fruitsdelight:persimmon_jello` | Canned Persimmon Jello | - | - | - | - | - | - | - | - | - | - |
| / | Pineapple Jello `fruitsdelight:pineapple_jello` | Canned Pineapple Jello | - | - | - | - | - | - | - | - | - | - |
| / | Salmonberry Ice Cream `delightful:salmonberry_ice_cream` | Canned Salmonberry Ice Cream | - | - | - | - | - | - | - | - | - | - |
| / | Strawberry Ice Cream `neapolitan:strawberry_ice_cream` | Canned Strawberry Ice Cream | - | - | - | - | - | - | none | - | - | - |
| / | Sweetberry Jello `fruitsdelight:sweetberry_jello` | Canned Sweetberry Jello | - | - | - | - | - | - | none | - | - | - |
| / | Vanilla Ice Cream `neapolitan:vanilla_ice_cream` | Canned Vanilla Ice Cream | - | - | - | - | - | - | - | - | - | - |
| / | Vanilla Pudding `neapolitan:vanilla_pudding` | Canned Vanilla Pudding | - | - | - | - | - | - | - | - | - | - |

### 2.23 Cake <a id="cake"></a>

Added 2026-10-20 from the sweet type: cake slices and the one whole edible cake item. All undecided. Canned Goods has its own `canned_goods:canned_cake` (no recipe here, [3.1](#new-sections)); slices could work like the pie fillings.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| / | Mangosteen Cake `fruitsdelight:mangosteen_cake` | Canned Mangosteen Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Adzuki Cake `abnormals_delight:adzuki_cake_slice` | Canned Adzuki Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Aloe Cake `create_central_kitchen:aloe_cake_slice` | Canned Aloe Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Banana Cake `abnormals_delight:banana_cake_slice` | Canned Banana Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Cake `farmersdelight:cake_slice` | Canned Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Chocolate Cake `abnormals_delight:chocolate_cake_slice` | Canned Chocolate Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Chocolate Cake `create_central_kitchen:chocolate_cake_slice` | Canned Chocolate Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Coffee Cake `farmersrespite:coffee_cake_slice` | Canned Coffee Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Honey Cake `create_central_kitchen:honey_cake_slice` | Canned Honey Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Mint Cake `abnormals_delight:mint_cake_slice` | Canned Mint Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Passionfruit Cake `create_central_kitchen:passionfruit_cake_slice` | Canned Passionfruit Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Pumpkin Cake `create_central_kitchen:pumpkin_cake_slice` | Canned Pumpkin Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Strawberry Cake `abnormals_delight:strawberry_cake_slice` | Canned Strawberry Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Sweet Berry Cake `create_central_kitchen:sweet_berry_cake_slice` | Canned Sweet Berry Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Sweet Berry Cheesecake `farmersdelight:sweet_berry_cheesecake_slice` | Canned Sweet Berry Cheesecake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Vanilla Cake `abnormals_delight:vanilla_cake_slice` | Canned Vanilla Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Yucca Cake `create_central_kitchen:yucca_cake_slice` | Canned Yucca Cake | - | - | - | - | - | - | - | - | - | - |
| / | Slice of Yucca Gateau `abnormals_delight:yucca_gateau_slice` | Canned Yucca Gateau | - | - | - | - | - | - | - | - | - | - |

### 2.24 Sweets <a id="sweets"></a>

The rest of the sweet type (candy, chocolate, waffles, rolls, muffins, marshmallows, toasts). Added 2026-10-20, all undecided.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| / | Bar of Caramel `create_confectionery:bar_of_caramel` | Canned Bar of Caramel | - | - | - | - | - | - | none | - | - | - |
| / | Bar of Chocolate `create:bar_of_chocolate` | Canned Bar of Chocolate | - | - | - | - | - | - | none | - | - | - |
| / | Bar of Dark Chocolate `create_confectionery:bar_of_black_chocolate` | Canned Bar of Dark Chocolate | - | - | - | - | - | - | none | - | - | - |
| / | Bar of Ruby Chocolate `create_confectionery:bar_of_ruby_chocolate` | Canned Bar of Ruby Chocolate | - | - | - | - | - | - | none | - | - | - |
| / | Bar of White Chocolate `create_confectionery:bar_of_white_chocolate` | Canned Bar of White Chocolate | - | - | - | - | - | - | none | - | - | - |
| / | Blueberry Muffin `fruitsdelight:blueberry_muffin` | Canned Blueberry Muffin | - | - | - | - | - | - | - | - | - | - |
| / | Candy `supplementaries:candy` | Canned Candy | - | - | - | - | - | - | none | - | - | - |
| / | Candy Cane `create_confectionery:candy_cane` | Canned Candy Cane | - | - | - | - | - | - | none | - | - | - |
| / | Caramel Apple `create_factory:caramel_apple` | Canned Caramel Apple | - | - | - | - | - | - | none | - | - | - |
| / | Caramel Glazed Berries `create_confectionery:caramel_glazed_berries` | Canned Caramel Glazed Berries | - | - | - | - | - | - | none | - | - | - |
| / | Caramel Roll `create_factory:caramel_roll` | Canned Caramel Roll | - | - | - | - | - | - | none | - | - | - |
| / | Caramel Waffle `create_factory:caramel_waffle` | Canned Caramel Waffle | - | - | - | - | - | - | none | - | - | - |
| / | Caramelized Marshmallows on a Stick `create_confectionery:caramelized_marshmellow_on_a_stick` | Canned Caramelized Marshmallows on a Stick | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Apple `create_factory:chocolate_apple` | Canned Chocolate Apple | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Bar `neapolitan:chocolate_bar` | Canned Chocolate Bar | - | - | - | - | - | - | - | - | - | - |
| / | Chocolate Candy `create_confectionery:chocolate_candy_1` | Canned Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Candy `create_confectionery:chocolate_candy_2` | Canned Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Candy `create_confectionery:chocolate_candy_3` | Canned Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Candy `create_confectionery:chocolate_candy` | Canned Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Copper Coin `lightmanscurrency:coin_chocolate_copper` | Canned Chocolate Copper Coin | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Diamond Coin `lightmanscurrency:coin_chocolate_diamond` | Canned Chocolate Diamond Coin | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Emerald Coin `lightmanscurrency:coin_chocolate_emerald` | Canned Chocolate Emerald Coin | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Glazed Berries `create:chocolate_glazed_berries` | Canned Chocolate Glazed Berries | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Glazed Marshmallow `create_confectionery:chocolate_glazed_marshmallow` | Canned Chocolate Glazed Marshmallow | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Gold Coin `lightmanscurrency:coin_chocolate_gold` | Canned Chocolate Gold Coin | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Iron Coin `lightmanscurrency:coin_chocolate_iron` | Canned Chocolate Iron Coin | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Netherite Coin `lightmanscurrency:coin_chocolate_netherite` | Canned Chocolate Netherite Coin | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Night Berries `neapolitan:chocolate_spider_eye` | Canned Chocolate Night Berries | - | - | - | - | - | - | - | - | - | - |
| / | Chocolate Popsicle `moredelight:chocolate_popsicle` | Canned Chocolate Popsicle | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Roll `create_factory:chocolate_roll` | Canned Chocolate Roll | - | - | - | - | - | - | none | - | - | - |
| / | Chocolate Strawberries `neapolitan:chocolate_strawberries` | Canned Chocolate Strawberries | - | - | - | - | - | - | - | - | - | - |
| / | Chocolate Waffle `create_factory:chocolate_waffle` | Canned Chocolate Waffle | - | - | - | - | - | - | none | - | - | - |
| / | Cocoa Fudge `brewinandchewin:cocoa_fudge` | Canned Cocoa Fudge | - | - | - | - | - | - | - | - | - | - |
| / | Cotton Candy `cloudstorage:cotton_candy` | Canned Cotton Candy | - | - | - | - | - | - | none | - | - | - |
| / | Cranberry Muffin `fruitsdelight:cranberry_muffin` | Canned Cranberry Muffin | - | - | - | - | - | - | - | - | - | - |
| / | Dark Chocolate Apple `create_factory:dark_chocolate_apple` | Canned Dark Chocolate Apple | - | - | - | - | - | - | none | - | - | - |
| / | Dark Chocolate Candy `create_confectionery:black_chocolate_candy_1` | Canned Dark Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | Dark Chocolate Candy `create_confectionery:black_chocolate_candy_2` | Canned Dark Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | Dark Chocolate Candy `create_confectionery:black_chocolate_candy_3` | Canned Dark Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | Dark Chocolate Candy `create_confectionery:black_chocolate_candy` | Canned Dark Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | Dark Chocolate Glazed Berries `create_confectionery:black_chocolate_glazed_berries` | Canned Dark Chocolate Glazed Berries | - | - | - | - | - | - | none | - | - | - |
| / | Dark Chocolate Glazed Marshmallow `create_confectionery:black_chocolate_glazed_marshmallow` | Canned Dark Chocolate Glazed Marshmallow | - | - | - | - | - | - | none | - | - | - |
| / | Dark Chocolate Roll `create_factory:dark_chocolate_roll` | Canned Dark Chocolate Roll | - | - | - | - | - | - | none | - | - | - |
| / | Dark Chocolate Waffle `create_factory:dark_chocolate_waffle` | Canned Dark Chocolate Waffle | - | - | - | - | - | - | none | - | - | - |
| / | Fruit Beignet `rusticdelight:fruit_beignet` | Canned Fruit Beignet | - | - | - | - | - | - | - | - | - | - |
| / | Gingerbread `create_confectionery:gingerbread` | Canned Gingerbread | - | - | - | - | - | - | none | - | - | - |
| / | Gingerbread Man `create_confectionery:gingerbread_man` | Canned Gingerbread Men | - | - | - | - | - | - | none | - | - | - |
| / | Hamimelon Popsicle `fruitsdelight:hamimelon_popsicle` | Canned Hamimelon Popsicle | - | - | - | - | - | - | - | - | - | - |
| / | Hamimelon Shaved Ice `fruitsdelight:hamimelon_shaved_ice` | Canned Hamimelon Shaved Ice | - | - | - | - | - | - | - | - | - | - |
| / | Hawberry Sheet `fruitsdelight:hawberry_sheet` | Canned Hawberry Sheet | - | - | - | - | - | - | - | - | - | - |
| / | Hawberry Stick `fruitsdelight:hawberry_stick` | Canned Hawberry Stick | - | - | - | - | - | - | - | - | - | - |
| / | Honey Apple `buzzier_bees:honey_apple` | Canned Honey Apple | - | - | - | - | - | - | none | - | - | - |
| / | Honey Candy `create_confectionery:honey_candy` | Canned Honey Candy | - | - | - | - | - | - | none | - | - | - |
| / | Honey Glazed Berries `create_factory:honey_glazed_berries` | Canned Honey Glazed Berries | - | - | - | - | - | - | none | - | - | - |
| / | Honey Glazed Walnut `delightful:honey_glazed_walnut` | Canned Honey Glazed Walnut | - | - | - | - | - | - | none | - | - | - |
| / | Honey Roll `create_factory:honey_roll` | Canned Honey Roll | - | - | - | - | - | - | none | - | - | - |
| / | Honey Waffle `create_factory:honey_waffle` | Canned Honey Waffle | - | - | - | - | - | - | none | - | - | - |
| / | Honeyed Apple `create:honeyed_apple` | Canned Honeyed Apple | - | - | - | - | - | - | none | - | - | - |
| / | Kiwi Popsicle `fruitsdelight:kiwi_popsicle` | Canned Kiwi Popsicle | - | - | - | - | - | - | - | - | - | - |
| / | Marshmallow `create_confectionery:marshmallow` | Canned Marshmallow | - | - | - | - | - | - | none | - | - | - |
| / | Marshmallow on a Stick `delightful:marshmallow_stick` | Canned Marshmallow on a Stick | - | - | - | - | - | - | none | - | - | - |
| / | Marshmallows on a Stick `create_confectionery:marshmallow_on_a_stick` | Canned Marshmallows on a Stick | - | - | - | - | - | - | none | - | - | - |
| / | Melon Popsicle `farmersdelight:melon_popsicle` | Canned Melon Popsicle | - | - | - | - | - | - | none | - | - | - |
| / | Mint Candies `neapolitan:mint_candies` | Canned Mint Candies | - | - | - | - | - | - | - | - | - | - |
| / | Mint Chocolate `neapolitan:mint_chocolate` | Canned Mint Chocolate | - | - | - | - | - | - | - | - | - | - |
| x | Pear with Rock Sugar `fruitsdelight:pear_with_rock_sugar` | Canned Pear with Rock Sugar | - | - | - | - | - | - | - | - | - | - |
| / | Roasted Marshmallow on a Stick `delightful:cooked_marshmallow_stick` | Canned Roasted Marshmallow on a Stick | - | - | - | - | - | - | none | - | - | - |
| / | Rock Candy `delightful:rock_candy` | Canned Rock Candy | - | - | - | - | - | - | - | - | - | - |
| / | Ruby Chocolate Apple `create_factory:ruby_chocolate_apple` | Canned Ruby Chocolate Apple | - | - | - | - | - | - | none | - | - | - |
| / | Ruby Chocolate Candy `create_confectionery:ruby_chocolate_candy_1` | Canned Ruby Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | Ruby Chocolate Candy `create_confectionery:ruby_chocolate_candy_2` | Canned Ruby Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | Ruby Chocolate Candy `create_confectionery:ruby_chocolate_candy_3` | Canned Ruby Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | Ruby Chocolate Candy `create_confectionery:ruby_chocolate_candy` | Canned Ruby Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | Ruby Chocolate Glazed Berries `create_confectionery:ruby_chocolate_glazed_berries` | Canned Ruby Chocolate Glazed Berries | - | - | - | - | - | - | none | - | - | - |
| / | Ruby Chocolate Glazed Marshmallow `create_confectionery:ruby_chocolate_glazed_marshmallow` | Canned Ruby Chocolate Glazed Marshmallow | - | - | - | - | - | - | none | - | - | - |
| / | Ruby Chocolate Roll `create_factory:ruby_chocolate_roll` | Canned Ruby Chocolate Roll | - | - | - | - | - | - | none | - | - | - |
| / | Ruby Chocolate Waffle `create_factory:ruby_chocolate_waffle` | Canned Ruby Chocolate Waffle | - | - | - | - | - | - | none | - | - | - |
| / | S'more `delightful:smore` | Canned S'more | - | - | - | - | - | - | - | - | - | - |
| / | Salmonberry Gummy `delightful:salmonberry_gummy` | Canned Salmonberry Gummy | - | - | - | - | - | - | - | - | - | - |
| / | Strange Eclair `endersdelight:strange_eclair` | Canned Strange Eclair | - | - | - | - | - | - | - | - | - | - |
| / | Strawberry Bean Bonbons `neapolitan:strawberry_bean_bonbons` | Canned Strawberry Bean Bonbons | - | - | - | - | - | - | - | - | - | - |
| x | Sweet Roll `create:sweet_roll` | Canned Sweet Roll | - | - | - | - | - | - | none | - | - | - |
| / | Toast with Chocolate `moredelight:toast_with_chocolate` | Canned Toast with Chocolate | - | - | - | - | - | - | none | - | - | - |
| / | Toast with Glow Berries `moredelight:toast_with_glow_berries` | Canned Toast with Glow Berries | - | - | - | - | - | - | none | - | - | - |
| / | Toast with Honey `moredelight:toast_with_honey` | Canned Toast with Honey | - | - | - | - | - | - | none | - | - | - |
| / | Toast with Sweet Berries `moredelight:toast_with_sweet_berries` | Canned Toast with Sweet Berries | - | - | - | - | - | - | none | - | - | - |
| / | Vanilla Chocolate Fingers `neapolitan:vanilla_chocolate_fingers` | Canned Vanilla Chocolate Fingers | - | - | - | - | - | - | - | - | - | - |
| / | Vanilla Fudge `neapolitan:vanilla_fudge` | Canned Vanilla Fudge | - | - | - | - | - | - | - | - | - | - |
| / | Waffle `create_factory:waffle` | Canned Waffle | - | - | - | - | - | - | none | - | - | - |
| / | White Chocolate Apple `create_factory:white_chocolate_apple` | Canned White Chocolate Apple | - | - | - | - | - | - | none | - | - | - |
| / | White Chocolate Candy `create_confectionery:white_chocolate_candy_1` | Canned White Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | White Chocolate Candy `create_confectionery:white_chocolate_candy_2` | Canned White Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | White Chocolate Candy `create_confectionery:white_chocolate_candy_3` | Canned White Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | White Chocolate Candy `create_confectionery:white_chocolate_candy` | Canned White Chocolate Candy | - | - | - | - | - | - | none | - | - | - |
| / | White Chocolate Glazed Berries `create_confectionery:white_chocolate_glazed_berries` | Canned White Chocolate Glazed Berries | - | - | - | - | - | - | none | - | - | - |
| / | White Chocolate Glazed Marshmallow `create_confectionery:white_chocolate_glazed_marshmallow` | Canned White Chocolate Glazed Marshmallow | - | - | - | - | - | - | none | - | - | - |
| / | White Chocolate Roll `create_factory:white_chocolate_roll` | Canned White Chocolate Roll | - | - | - | - | - | - | none | - | - | - |
| / | White Chocolate Waffle `create_factory:white_chocolate_waffle` | Canned White Chocolate Waffle | - | - | - | - | - | - | none | - | - | - |

### 2.25 Handheld <a id="handheld"></a>

The handheld type (sandwiches, burgers, rolls, skewers, stuffed peppers). Added 2026-10-20, all undecided.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| / | Bacon Sandwich `farmersdelight:bacon_sandwich` | Canned Bacon Sandwich | - | - | - | - | - | - | none | - | - | - |
| / | Barbecue on a Stick `farmersdelight:barbecue_stick` | Canned Barbecue on a Stick | - | - | - | - | - | - | none | - | - | - |
| / | Bison Burger `alexsdelight:bison_burger` | Canned Bison Burger | - | - | - | - | - | - | none | - | - | - |
| / | Bunfungus Sandwich `alexsdelight:bunfungus_sandwich` | Canned Bunfungus Sandwich | - | - | - | - | - | - | none | - | - | - |
| / | Cabbage Rolls `farmersdelight:cabbage_rolls` | Canned Cabbage Rolls | - | - | - | - | - | - | none | - | - | - |
| / | Calamari Roll `rusticdelight:calamari_roll` | Canned Calamari Roll | - | - | - | - | - | - | none | - | - | - |
| / | Cheeseburger `delightful:cheeseburger` | Canned Cheeseburger | - | - | - | - | - | - | - | - | - | - |
| / | Chicken Sandwich `farmersdelight:chicken_sandwich` | Canned Chicken Sandwich | - | - | - | - | - | - | none | - | - | - |
| / | Chicken Sandwich with Egg and Tomato `moredelight:chicken_sandwich_with_egg_and_tomato` | Canned Chicken Sandwich with Egg and Tomato | - | - | - | - | - | - | none | - | - | - |
| / | Chunk Nugget `delightful:chunk_nugget` | Canned Chunk Nugget | - | - | - | - | - | - | none | - | - | - |
| / | Chunkwich `delightful:chunkwich` | Canned Chunkwich | - | - | - | - | - | - | - | - | - | - |
| / | Classic Corn Dog `corn_delight:classic_corn_dog` | Canned Classic Corn Dog | - | - | - | - | - | - | none | - | - | - |
| / | Cod Roll `farmersdelight:cod_roll` | Canned Cod Roll | - | - | - | - | - | - | none | - | - | - |
| / | Cod n Chips `pneumaticcraft:cod_n_chips` | Canned Cod n Chips | - | - | - | - | - | - | none | - | - | - |
| / | Corn Dog `corn_delight:corn_dog` | Canned Corn Dog | - | - | - | - | - | - | none | - | - | - |
| / | Crab Bars `quarkdelight:crab_bars` | Canned Crab Bars | - | - | - | - | - | - | none | - | - | - |
| / | Crab Rangoon `delightful:crab_rangoon` | Canned Crab Rangoon | - | - | - | - | - | - | none | - | - | - |
| / | Crawling Sandwich `endersdelight:crawling_sandwich` | Canned Crawling Sandwich | - | - | - | - | - | - | - | - | - | - |
| / | Crispy Skewer `endersdelight:crispy_skewer` | Canned Crispy Skewer | - | - | - | - | - | - | none | - | - | - |
| / | Deluxe Cheeseburger `delightful:deluxe_cheeseburger` | Canned Deluxe Cheeseburger | - | - | - | - | - | - | - | - | - | - |
| x | Dumplings `farmersdelight:dumplings` | Canned Dumplings | - | - | - | - | - | - | none | - | - | - |
| / | Egg Sandwich `farmersdelight:egg_sandwich` | Canned Egg Sandwich | - | - | - | - | - | - | none | - | - | - |
| / | Egg with Bacon Sandwich `moredelight:egg_with_bacon_sandwich` | Canned Egg with Bacon Sandwich | - | - | - | - | - | - | none | - | - | - |
| / | Gongylidia Bruschetta `alexsdelight:gongylidia_bruschetta` | Canned Gongylidia Bruschetta | - | - | - | - | - | - | none | - | - | - |
| / | Ham and Cheese Sandwich `brewinandchewin:ham_and_cheese_sandwich` | Canned Ham and Cheese Sandwich | - | - | - | - | - | - | none | - | - | - |
| / | Hamburger `farmersdelight:hamburger` | Canned Hamburger | - | - | - | - | - | - | none | - | - | - |
| / | Hamburger with Egg `moredelight:hamburger_with_egg` | Canned Hamburger with Egg | - | - | - | - | - | - | none | - | - | - |
| / | Hawberry Roll `fruitsdelight:hawberry_roll` | Canned Hawberry Roll | - | - | - | - | - | - | - | - | - | - |
| / | Jelly Bread `fruitsdelight:jelly_bread` | Canned Jelly Bread | - | - | - | - | - | - | none | - | - | - |
| / | Kangaroo Burger `alexsmobs:kangaroo_burger` | Canned Kangaroo Burger | - | - | - | - | - | - | none | - | - | - |
| / | Kelp Roll `farmersdelight:kelp_roll` | Canned Kelp Roll | - | - | - | - | - | - | none | - | - | - |
| / | Kelp Roll Slice `farmersdelight:kelp_roll_slice` | Canned Kelp Roll Slice | - | - | - | - | - | - | none | - | - | - |
| / | Loaded Hamburger `moredelight:loaded_hamburger` | Canned Loaded Hamburger | - | - | - | - | - | - | none | - | - | - |
| / | Mutton Wrap `farmersdelight:mutton_wrap` | Canned Mutton Wrap | - | - | - | - | - | - | none | - | - | - |
| / | Nether Skewer `nethersdelight:nether_skewer` | Canned Nether Skewer | - | - | - | - | - | - | none | - | - | - |
| / | Nut Butter and Jam Sandwich `delightful:nut_butter_and_jelly_sandwich` | Canned Nut Butter and Jam Sandwich | - | - | - | - | - | - | - | - | - | - |
| / | Pizza Slice `brewinandchewin:pizza_slice` | Canned Pizza Slice | - | - | - | - | - | - | none | - | - | - |
| / | Porkchop Sandwich `moredelight:porkchop_sandwich` | Canned Porkchop Sandwich | - | - | - | - | - | - | none | - | - | - |
| / | Quiche Slice `brewinandchewin:quiche_slice` | Canned Quiche Slice | - | - | - | - | - | - | none | - | - | - |
| / | Salmon Roll `farmersdelight:salmon_roll` | Canned Salmon Roll | - | - | - | - | - | - | none | - | - | - |
| / | Salmon Tempura `pneumaticcraft:salmon_tempura` | Canned Salmon Tempura | - | - | - | - | - | - | none | - | - | - |
| / | Simple Hamburger `moredelight:simple_hamburger` | Canned Simple Hamburger | - | - | - | - | - | - | none | - | - | - |
| / | Spring Rolls `rusticdelight:spring_rolls` | Canned Spring Rolls | - | - | - | - | - | - | none | - | - | - |
| / | Steak Sandwich `moredelight:steak_sandwich` | Canned Steak Sandwich | - | - | - | - | - | - | none | - | - | - |
| / | Stuffed Green Bell Pepper `rusticdelight:stuffed_bell_pepper_green` | Canned Stuffed Green Bell Pepper | - | - | - | - | - | - | none | - | - | - |
| / | Stuffed Potato `farmersdelight:stuffed_potato` | Canned Stuffed Potato | - | - | - | - | - | - | none | - | - | - |
| / | Stuffed Red Bell Pepper `rusticdelight:stuffed_bell_pepper_red` | Canned Stuffed Red Bell Pepper | - | - | - | - | - | - | none | - | - | - |
| / | Stuffed Yellow Bell Pepper `rusticdelight:stuffed_bell_pepper_yellow` | Canned Stuffed Yellow Bell Pepper | - | - | - | - | - | - | none | - | - | - |
| / | Sweet Berry Meatballs `berry_good:sweet_berry_meatballs` | Canned Sweet Berry Meatballs | - | - | - | - | - | - | none | - | - | - |
| / | Taco `corn_delight:taco` | Canned Taco | - | - | - | - | - | - | - | - | - | - |
| / | Toast with Egg `moredelight:toast_with_egg` | Canned Toast with Egg | - | - | - | - | - | - | none | - | - | - |
| / | Tomato Sandwich `moredelight:tomato_sandwich` | Canned Tomato Sandwich | - | - | - | - | - | - | none | - | - | - |

### 2.26 Ingredients <a id="ingredients"></a>

The rest of the ingredient type (batter, dough, oil, crust, raw pasta, coffee beans). Added 2026-10-20, all undecided.

| Want | Item | Can | Input (basic / +additive) | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| / | Animal Fat `delightful:animal_fat` | Canned Animal Fat | - | - | - | - | - | - | none | - | - | - |
| / | Apple Mash `vinery:apple_mash` | Canned Apple Mash | - | - | - | - | - | - | none | - | - | - |
| / | Batter `rusticdelight:batter` | Canned Batter | - | - | - | - | - | - | - | - | - | - |
| / | Chopped Clover `delightful:chopped_clover` | Canned Chopped Clover | - | - | - | - | - | - | none | - | - | - |
| / | Cooking Oil `rusticdelight:cooking_oil` | Canned Cooking Oil | - | - | - | - | - | - | - | - | - | - |
| / | Cornbread Batter `corn_delight:cornbread_batter` | Canned Cornbread Batter | - | - | - | - | - | - | none | - | - | - |
| / | Dried Vanilla Pods `neapolitan:dried_vanilla_pods` | Canned Dried Vanilla Pods | - | - | - | - | - | - | - | - | - | - |
| / | Golden Coffee Beans `rusticdelight:golden_coffee_beans` | Canned Golden Coffee Beans | - | - | - | - | - | - | none | - | - | - |
| / | Green Tea Leaves `delightful:green_tea_leaf` | Canned Green Tea Leaves | - | - | - | - | - | - | none | - | - | - |
| / | Pie Crust `farmersdelight:pie_crust` | Canned Pie Crust | - | - | - | - | - | - | none | - | - | - |
| / | Raw Pasta `farmersdelight:raw_pasta` | Canned Raw Pasta | - | - | - | - | - | - | - | - | - | - |
| / | Raw Tortilla `corn_delight:tortilla_raw` | Canned Raw Tortilla | - | - | - | - | - | - | none | - | - | - |
| / | Roasted Coffee Beans `rusticdelight:roasted_coffee_beans` | Canned Roasted Coffee Beans | - | - | - | - | - | - | none | - | - | - |
| / | Sweet Berry Mince `berry_good:sweet_berry_mince` | Canned Sweet Berry Mince | - | - | - | - | - | - | none | - | - | - |
| x | Dough `create:dough` | Canned Dough | - | - | - | - | - | - | - | - | - | - |

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

Canned Goods' own cans that / recipe makes any more (their recipes are removed in canning.js). Since 2026-10-13 these
have recipes again: Chorus Fruit, Honey Bottles, Poisonous Potatos, Pufferfishs, Tough Jerky, Night Berries, Tropical Fish.
Canned Apples `canned_goods:canned_apples`, Canned Bread `canned_goods:canned_beetroot_soup`, Canned Beetroots `canned_goods:canned_beets`, Canned Cake `canned_goods:canned_cake`, Canned Pumpkin Pies `canned_goods:canned_carrots`, Canned Chorus Fruit `canned_goods:canned_chorus_fruits`, Canned Notch Apples `canned_goods:canned_enchanted_apples`, Canned Food `canned_goods:canned_food`, Canned Glow Berries `canned_goods:canned_glow_berries`, Canned Golden Apples `canned_goods:canned_golden_apples`, Canned Golden Carrots `canned_goods:canned_golden_carrot`, Canned Honey Bottles `canned_goods:canned_honey_bottles`, Canned Melon Slice `canned_goods:canned_melon_slice`, Canned Poisonous Potatos `canned_goods:canned_posionous_potatos`, Canned Potatos `canned_goods:canned_potatos`, Canned Pufferfishs `canned_goods:canned_pufferfishs`, Canned Golden Carrots `canned_goods:canned_pumpkin_pie`, Canned Tough Jerky `canned_goods:canned_rotten_flesh`, Canned Night Berries `canned_goods:canned_spider_eyes`, Canned Suspicious Stew `canned_goods:canned_suspicious_stew`, Canned Sweet Berries `canned_goods:canned_sweet_berries`, Canned Tropical Fish `canned_goods:canned_tropical_fish`, Hot Hot Hotpot `canned_goods:hot_hot_hotpot`.

## 4. Decisions

Settled 2026-10-20. "Decided" means the rule is fixed; none of it is built yet except where marked. Build order is in
[cans.md](cans.md) 4.

| Topic | Decision | Status |
|---|---|---|
| Pumpkin | Fruit: moved to canning.js's raw fruit section, 3 servings, 4 slices or 3 + sugar or honey (recipe id `canned_pumpkin_sugar`, was `canned_pumpkin_salt`). Slices only: the whole pumpkin is a block, not a food | BUILT 2026-10-20, not run in game |
| White strawberries | Same ratio as normal strawberries (the old 3 / 2 + sugar), with a much higher boost to the per-serving nutrition and saturation (raw item is 5 hunger, 0.05). Set the numbers when the h/s columns are filled | decided, numbers later |
| Bananas | Merged: Alex's Mobs bananas stay out of the can. Any behaviour or recipe the Alex's Mobs banana had (can, slices) is replicated for the Neapolitan banana | decided, not built |
| Raw meat "half can" | Was: a slice recipe that needed more than 8 items took 8 and made a can with fewer servings (8 Raw Chicken Cuts -> 2/4). Removed: no recipe makes a partly full can ([4.2](#slice-family-rule)) | superseded 2026-10-20 |
| Sliced and whole meats | Every can whose whole item and slice are both edible takes either, in the same slots at the whole item's count ([4.2](#slice-family-rule)). Slicing first is the 2x+ boost: one whole item cuts into 2 or more slices | BUILT 2026-10-20, not run in game |
| Partial cans as pie input | Not allowed. Filling + pie crust -> pie requires a full filling can; an opened can no longer makes a pie. Needs a recipe-side NBT check (recipe ingredients ignore NBT today), so the pie recipe moves to a custom check or a `CanServings`-aware ingredient | decided, not built |
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

### 4.3 Value vs goal, every can <a id="value-vs-goal"></a>

Computed 2026-10-20 from can_types.json, canning.js and food_catalog.md (cantaloupe slice at its new 3 / 1.8). Value
= hunger + saturation points. **In** = the basic recipe (count x the first input's value); **can** = servings x meal;
**goal** = 0.8 x can (the meal-section target); **share** = in / goal. Meal sections (hand-written raw, raw_fruit,
raw_meat, raw_fish, hazard, special) are sized to hit 100% (8 cap aside); mult sections (cookies onward) use a fixed
count, so their share is set by the section multiplier and is the same for every can in it. Additive recipes are not
listed (they use fewer items for the same can).

| Section | Cans | Share range | Note |
|---|---:|---|---|
| hand veggie | 9 | 55% to 116% | Baked Potato meal (5 + 6) x 2 |
| hand fruit | 12 | 86% to 103% | Fruit Meal (5 + 3) x 3 |
| hand meat | 5 | 94% to 100% | Meat Meal (5 + 3) x 4 |
| raw_fruit | 17 | 100% to 100% | Fruit Meal x 3 |
| raw_meat | 1 | 100% to 100% | shulker: own meal (2 + 1) x 4 |
| raw_fish | 12 | 50% to 113% | Fish Meal (4 + 2) x 4 |
| hazard | 4 | 50% to 102% | pufferfish Fish Meal, rest Meat Meal |
| special | 2 | 100% to 100% | Fruit Meal x 3 |
| cookies | 12 | 100% to 100% | item x2, 8 -> 5 servings |
| cooked_veggie | 15 | 94% to 94% | item x2, 6 -> 4 |
| cooked_meat | 15 | 104% to 104% | item x2, 5 -> 3 |
| soup | 27 | 73% to 87% | item x1.25, 6 -> 8 |
| bread | 8 | 94% to 94% | item x2, 6 -> 4 |
| meal | 11 | 81% to 84% | pasta x0.75, 2 -> 4 (placeholder) |
| pie | 9 | 125% to 125% | slice x1, 4 -> 4 (placeholder) |
| feast | 9 | n/a | 1 placed feast -> 6 (input is a block, no value) |
| drink | 2 | 125% to 125% | item x1, 4 -> 4 (placeholder) |
| salad | 8 | 73% to 77% | item x1.25, 6 -> 8 |
| golden | 50 | 89% to 100% | item x0.25, 2 -> 10 |

**hand veggie**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_beetroot | Beetroot (2.2) | 8 | 2 x (5 + 6) | 22 | 17.6 | 17.6 | 100% |
| canned_cabbage | Cabbage (3.6) | 5 | 2 x (5 + 6) | 22 | 18 | 17.6 | 102% |
| canned_carrot | Carrot (6.6) | 3 | 2 x (5 + 6) | 22 | 19.8 | 17.6 | 112% |
| canned_onion | Onion (3.6) | 5 | 2 x (5 + 6) | 22 | 18 | 17.6 | 102% |
| canned_potato | Potato (1.6) | 8 | 2 x (5 + 6) | 22 | 12.8 | 17.6 | 73% |
| canned_tomato | Tomato (1.6) | 8 | 2 x (5 + 6) | 22 | 12.8 | 17.6 | 73% |
| canned_bell_pepper | Green Bell Pepper (1.2) | 8 | 2 x (5 + 6) | 22 | 9.6 | 17.6 | 55% |
| canned_cactus_flesh | Cactus Chunk (4.8) | 4 | 2 x (5 + 6) | 22 | 19.2 | 17.6 | 109% |
| canned_gongylidia | Gongylidia (10.2) | 2 | 2 x (5 + 6) | 22 | 20.4 | 17.6 | 116% |

**hand fruit**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_apple | Apple (6.4) | 3 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_banana | Banana (2.8) | 7 | 3 x (5 + 3) | 24 | 19.6 | 19.2 | 102% |
| canned_cantaloupe | Cantaloupe Slice (4.8) | 4 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_cherry | Cherry (3.2) | 6 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_glow_berry | Glow Berries (2.4) | 8 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_melon | Melon Slice (3.2) | 6 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_mulberry | Mulberry (3.6) | 5 | 3 x (5 + 3) | 24 | 18 | 19.2 | 94% |
| canned_pumpkin | Pumpkin Slice (4.8) | 4 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_salmonberry | Salmonberries (4.4) | 4 | 3 x (5 + 3) | 24 | 17.6 | 19.2 | 92% |
| canned_strawberry | Strawberries (3.3) | 6 | 3 x (5 + 3) | 24 | 19.8 | 19.2 | 103% |
| canned_white_strawberry | White Strawberries (5.5) | 3 | 3 x (5 + 3) | 24 | 16.5 | 19.2 | 86% |
| canned_sweet_berry | Sweet Berries (2.4) | 8 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |

**hand meat**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_raw_beef | Raw Beef (4.8) | 5 | 4 x (5 + 3) | 32 | 24 | 25.6 | 94% |
| canned_raw_chicken | Raw Chicken (3.2) | 8 | 4 x (5 + 3) | 32 | 25.6 | 25.6 | 100% |
| canned_raw_mutton | Raw Mutton (3.2) | 8 | 4 x (5 + 3) | 32 | 25.6 | 25.6 | 100% |
| canned_raw_pork | Raw Porkchop (4.8) | 5 | 4 x (5 + 3) | 32 | 24 | 25.6 | 94% |
| canned_raw_rabbit | Raw Rabbit (4.8) | 5 | 4 x (5 + 3) | 32 | 24 | 25.6 | 94% |

**raw_fruit**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_bayberry | Bayberry (3.2) | 6 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_blueberry | Blueberry (3.2) | 6 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_cranberry | Cranberry (3.2) | 6 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_durian | Durian Flesh (6.4) | 3 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_fig | Fig (4.8) | 4 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_hamimelon | Hamimelon Slice (3.2) | 6 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_hawberry | Hawberry (3.2) | 6 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_kiwi | Kiwi (4.8) | 4 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_lemon | Lemon (6.4) | 3 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_lychee | Lychee (3.2) | 6 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_mango | Mango (4.8) | 4 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_mangosteen | Mangosteen (4.8) | 4 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_orange | Orange (4.8) | 4 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_peach | Peach (4.8) | 4 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_pear | Pear (4.8) | 4 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_persimmon | Persimmon (4.8) | 4 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_pineapple | Pineapple Slice (3.2) | 6 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |

**raw_meat**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_shulker | Shulker Mollusk (1.2) | 8 | 4 x (2 + 1) | 12 | 9.6 | 9.6 | 100% |

**raw_fish**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_blobfish | Blobfish (5.4) | 4 | 4 x (4 + 2) | 24 | 21.6 | 19.2 | 113% |
| canned_flying_fish | Flying Fish (5.4) | 4 | 4 x (4 + 2) | 24 | 21.6 | 19.2 | 113% |
| canned_raw_lobster | Lobster Tail (3.6) | 5 | 4 x (4 + 2) | 24 | 18 | 19.2 | 94% |
| canned_raw_calamari | Raw Calamari (2.8) | 7 | 4 x (4 + 2) | 24 | 19.6 | 19.2 | 102% |
| canned_raw_catfish | Raw Catfish (3.2) | 6 | 4 x (4 + 2) | 24 | 19.2 | 19.2 | 100% |
| canned_raw_cod | Raw Cod (2.4) | 8 | 4 x (4 + 2) | 24 | 19.2 | 19.2 | 100% |
| canned_raw_crab | Raw Crab Leg (1.6) | 8 | 4 x (4 + 2) | 24 | 12.8 | 19.2 | 67% |
| canned_raw_lionfish | Raw Lionfish (3.2) | 6 | 4 x (4 + 2) | 24 | 19.2 | 19.2 | 100% |
| canned_raw_perch | Raw Perch (2.8) | 7 | 4 x (4 + 2) | 24 | 19.6 | 19.2 | 102% |
| canned_raw_pike | Raw Pike (4.8) | 4 | 4 x (4 + 2) | 24 | 19.2 | 19.2 | 100% |
| canned_raw_salmon | Raw Salmon (2.4) | 8 | 4 x (4 + 2) | 24 | 19.2 | 19.2 | 100% |
| canned_tropical_fish | Tropical Fish (1.2) | 8 | 4 x (4 + 2) | 24 | 9.6 | 19.2 | 50% |

**hazard**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_pufferfishs | Pufferfish (1.2) | 8 | 4 x (4 + 2) | 24 | 9.6 | 19.2 | 50% |
| canned_posionous_potatos | Poisonous Potato (3.2) | 8 | 4 x (5 + 3) | 32 | 25.6 | 25.6 | 100% |
| canned_spider_eyes | Spider Eye (5.2) | 5 | 4 x (5 + 3) | 32 | 26 | 25.6 | 102% |
| canned_rotten_flesh | Rotten Flesh (4.8) | 5 | 4 x (5 + 3) | 32 | 24 | 25.6 | 94% |

**special**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_chorus_fruits | Chorus Fruit (6.4) | 3 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |
| canned_ancient_fruit | Ancient Fruit (6.4) | 3 | 3 x (5 + 3) | 24 | 19.2 | 19.2 | 100% |

**cookies**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_cookies | Cookie (3.2) | 8 | 5 x (4 + 2.4) | 32 | 25.6 | 25.6 | 100% |
| canned_bayberry_cookie | Bayberry Cookie (1.6) | 8 | 5 x (2 + 1.2) | 16 | 12.8 | 12.8 | 100% |
| canned_cherry_cookie | Cherry Cookie (2.4) | 8 | 5 x (4 + 0.8) | 24 | 19.2 | 19.2 | 100% |
| canned_cranberry_cookie | Cranberry Cookie (1.6) | 8 | 5 x (2 + 1.2) | 16 | 12.8 | 12.8 | 100% |
| canned_green_tea_cookie | Green Tea Cookie (2.4) | 8 | 5 x (4 + 0.8) | 24 | 19.2 | 19.2 | 100% |
| canned_honey_cookie | Honey Cookie (2.4) | 8 | 5 x (4 + 0.8) | 24 | 19.2 | 19.2 | 100% |
| canned_lemon_cookie | Lemon Cookie (1.6) | 8 | 5 x (2 + 1.2) | 16 | 12.8 | 12.8 | 100% |
| canned_maple_cookie | Maple Cookie (2.4) | 8 | 5 x (4 + 0.8) | 24 | 19.2 | 19.2 | 100% |
| canned_mulberry_cookie | Mulberry Cookie (2.4) | 8 | 5 x (4 + 0.8) | 24 | 19.2 | 19.2 | 100% |
| canned_persimmon_cookie | Persimmon Cookie (1.6) | 8 | 5 x (2 + 1.2) | 16 | 12.8 | 12.8 | 100% |
| canned_sweet_berry_cookie | Sweet Berry Cookie (2.4) | 8 | 5 x (4 + 0.8) | 24 | 19.2 | 19.2 | 100% |
| canned_uncanny_cookie | Uncanny Cookie (11) | 8 | 5 x (10 + 12) | 110 | 88 | 88 | 100% |

**cooked_veggie**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_baked_potato | Baked Potato (11) | 6 | 4 x (10 + 12) | 88 | 66 | 70.4 | 94% |
| canned_cooked_cactus_steak | Cactus Steak (8) | 6 | 4 x (8 + 8) | 64 | 48 | 51.2 | 94% |
| canned_cooked_rice | Cooked Rice (10.8) | 6 | 4 x (12 + 9.6) | 86.4 | 64.8 | 69.1 | 94% |
| canned_dried_kelp | Dried Kelp (1.6) | 6 | 4 x (2 + 1.2) | 12.8 | 9.6 | 10.2 | 94% |
| canned_roasted_bell_pepper | Roasted Green Bell Pepper (13) | 6 | 4 x (10 + 16) | 104 | 78 | 83.2 | 94% |
| canned_baked_durian | Baked Durian (6.4) | 6 | 4 x (8 + 4.8) | 51.2 | 38.4 | 41 | 94% |
| canned_baked_pear | Baked Pear (6.4) | 6 | 4 x (8 + 4.8) | 51.2 | 38.4 | 41 | 94% |
| canned_boiled_blue_pickerelweed | Boiled Blue Pickerelweed (2) | 6 | 4 x (4 + 0) | 16 | 12 | 12.8 | 94% |
| canned_boiled_purple_pickerelweed | Boiled Purple Pickerelweed (3) | 6 | 4 x (6 + 0) | 24 | 18 | 19.2 | 94% |
| canned_boiled_corn | Boiled Corn (8.4) | 6 | 4 x (12 + 4.8) | 67.2 | 50.4 | 53.8 | 94% |
| canned_dried_banana | Dried Banana (6.4) | 6 | 4 x (8 + 4.8) | 51.2 | 38.4 | 41 | 94% |
| canned_dried_persimmon | Dried Persimmon (6.4) | 6 | 4 x (8 + 4.8) | 51.2 | 38.4 | 41 | 94% |
| canned_kimchi | Kimchi (4.4) | 6 | 4 x (4 + 4.8) | 35.2 | 26.4 | 28.2 | 94% |
| canned_pickled_pickles | Pickled Pickles (6.4) | 6 | 4 x (8 + 4.8) | 51.2 | 38.4 | 41 | 94% |
| canned_roasted_adzuki_beans | Roasted Adzuki Beans (5.4) | 6 | 4 x (6 + 4.8) | 43.2 | 32.4 | 34.6 | 94% |

**cooked_meat**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_beef | Steak (20.8) | 5 | 3 x (16 + 25.6) | 124.8 | 104 | 99.8 | 104% |
| canned_pork | Cooked Porkchop (20.8) | 5 | 3 x (16 + 25.6) | 124.8 | 104 | 99.8 | 104% |
| canned_chicken | Cooked Chicken (13.2) | 5 | 3 x (12 + 14.4) | 79.2 | 66 | 63.4 | 104% |
| canned_cooked_mutten | Cooked Mutton (15.6) | 5 | 3 x (12 + 19.2) | 93.6 | 78 | 74.9 | 104% |
| canned_cooked_rabbit | Cooked Rabbit (11) | 5 | 3 x (10 + 12) | 66 | 55 | 52.8 | 104% |
| canned_glazed_porkchop | Honey Glazed Porkchop (23.4) | 5 | 3 x (18 + 28.8) | 140.4 | 117 | 112.3 | 104% |
| canned_jerky | Jerky (7.2) | 5 | 3 x (6 + 8.4) | 43.2 | 36 | 34.6 | 104% |
| canned_maple_glazed_bacon | Maple Glazed Bacon (15.6) | 5 | 3 x (12 + 19.2) | 93.6 | 78 | 74.9 | 104% |
| canned_fish | Cooked Cod (11) | 5 | 3 x (10 + 12) | 66 | 55 | 52.8 | 104% |
| canned_cooked_salmon | Cooked Salmon (15.6) | 5 | 3 x (12 + 19.2) | 93.6 | 78 | 74.9 | 104% |
| canned_cooked_calamari | Cooked Calamari (11) | 5 | 3 x (10 + 12) | 66 | 55 | 52.8 | 104% |
| canned_cooked_catfish | Cooked Catfish (10) | 5 | 3 x (10 + 10) | 60 | 50 | 48 | 104% |
| canned_cooked_perch | Cooked Perch (12) | 5 | 3 x (10 + 14) | 72 | 60 | 57.6 | 104% |
| canned_cooked_pike | Cooked Pike (20.8) | 5 | 3 x (16 + 25.6) | 124.8 | 104 | 99.8 | 104% |
| canned_cooked_lionfish | Cooked Lionfish (14.4) | 5 | 3 x (12 + 16.8) | 86.4 | 72 | 69.1 | 104% |

**soup**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_rabbit_stew | Rabbit Stew (22) | 6 | 8 x (13 + 15) | 224 | 132 | 179.2 | 74% |
| canned_mushrooms | Mushroom Stew (13.2) | 6 | 8 x (8 + 9) | 135.9 | 79.2 | 108.7 | 73% |
| canned_acacia_blossom_soup | Acacia Blossom Soup (31.2) | 6 | 8 x (15 + 24) | 312 | 187.2 | 249.6 | 75% |
| canned_adzuki_stew | Adzuki Stew (17.6) | 6 | 8 x (10 + 12) | 176 | 105.6 | 140.8 | 75% |
| canned_baked_cod_stew | Baked Cod Stew (35) | 6 | 8 x (18 + 26.2) | 353.9 | 210 | 283.1 | 74% |
| canned_bayberry_soup | Bayberry Soup (1.4) | 6 | 8 x (1 + 0.5) | 12 | 8.4 | 9.6 | 87% |
| canned_beef_stew | Beef Stew (31.2) | 6 | 8 x (15 + 24) | 312 | 187.2 | 249.6 | 75% |
| canned_ce_beetroot_soup | Beetroot Soup (13.2) | 6 | 8 x (8 + 9) | 135.9 | 79.2 | 108.7 | 73% |
| canned_bell_pepper_soup | Bell Pepper Soup (13.2) | 6 | 8 x (8 + 9) | 135.9 | 79.2 | 108.7 | 73% |
| canned_blazing_chili | Blazing Chili (18) | 6 | 8 x (13 + 10) | 184.1 | 108 | 147.3 | 73% |
| canned_bone_broth | Bone Broth (19.2) | 6 | 8 x (10 + 14) | 192 | 115.2 | 153.6 | 75% |
| canned_carrot_soup | Carrot Soup (31.2) | 6 | 8 x (15 + 24) | 312 | 187.2 | 249.6 | 75% |
| canned_chicken_soup | Chicken Soup (35) | 6 | 8 x (18 + 26.2) | 353.9 | 210 | 283.1 | 74% |
| canned_chorus_stew | Chorus Stew (8) | 6 | 8 x (5 + 5) | 80 | 48 | 64 | 75% |
| canned_corn_soup | Corn Soup (28) | 6 | 8 x (13 + 22.5) | 283.9 | 168 | 227.1 | 74% |
| canned_creamy_onion_soup | Creamy Onion Soup (30) | 6 | 8 x (15 + 22.5) | 300 | 180 | 240 | 75% |
| canned_endermite_stew | Endermite Stew (7.2) | 6 | 8 x (5 + 4) | 72 | 43.2 | 57.6 | 75% |
| canned_fig_chicken_stew | Fig Chicken Stew (31.2) | 6 | 8 x (15 + 24) | 312 | 187.2 | 249.6 | 75% |
| canned_fish_stew | Fish Stew (31.2) | 6 | 8 x (15 + 24) | 312 | 187.2 | 249.6 | 75% |
| canned_kangaroo_stew | Kangaroo Stew (26) | 6 | 8 x (13 + 20) | 263.9 | 156 | 211.1 | 74% |
| canned_miso_with_bamboo_sprouts | Miso with Bamboo Sprouts (24) | 6 | 8 x (13 + 17.5) | 244 | 144 | 195.2 | 74% |
| canned_noodle_soup | Noodle Soup (35) | 6 | 8 x (18 + 26.2) | 353.9 | 210 | 283.1 | 74% |
| canned_pumpkin_soup | Pumpkin Soup (35) | 6 | 8 x (18 + 26.2) | 353.9 | 210 | 283.1 | 74% |
| canned_sinigang | Sinigang (42) | 6 | 8 x (19 + 33.7) | 421.9 | 252 | 337.5 | 75% |
| canned_sopa_de_macaco | Sopa De Macaco (9) | 6 | 8 x (6 + 5) | 88 | 54 | 70.4 | 77% |
| canned_strider_moss_stew | Strider Moss Stew (17.6) | 6 | 8 x (10 + 12) | 176 | 105.6 | 140.8 | 75% |
| canned_vegetable_soup | Vegetable Soup (31.2) | 6 | 8 x (15 + 24) | 312 | 187.2 | 249.6 | 75% |

**bread**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_bread | Bread (11) | 6 | 4 x (10 + 12) | 88 | 66 | 70.4 | 94% |
| canned_adzuki_bun | Adzuki Bun (9) | 6 | 4 x (10 + 8) | 72 | 54 | 57.6 | 94% |
| canned_banana_bread | Banana Bread (11) | 6 | 4 x (10 + 12) | 88 | 66 | 70.4 | 94% |
| canned_cornbread | Cornbread (8) | 6 | 4 x (8 + 8) | 64 | 48 | 51.2 | 94% |
| canned_honey_bread | Honey Bread (15.6) | 6 | 4 x (12 + 19.2) | 124.8 | 93.6 | 99.8 | 94% |
| canned_mulberry_bread | Mulberry Bread (16.2) | 6 | 4 x (18 + 14.4) | 129.6 | 97.2 | 103.7 | 94% |
| canned_nether_wart_sourdough | Nether Wart Sourdough (5.6) | 6 | 4 x (8 + 3.2) | 44.8 | 33.6 | 35.8 | 94% |
| canned_sourdough_bread | Sourdough Bread (21) | 6 | 4 x (14 + 28) | 168 | 126 | 134.4 | 94% |

**meal**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_bell_pepper_pasta | Bell Pepper Pasta (31.2) | 2 | 4 x (9 + 14.4) | 93.6 | 62.4 | 74.9 | 83% |
| canned_cheesy_pasta | Cheesy Pasta (31.2) | 2 | 4 x (9 + 14.4) | 93.6 | 62.4 | 74.9 | 83% |
| canned_crab_pasta | Crab Pasta (24.2) | 2 | 4 x (8 + 9.9) | 71.6 | 48.4 | 57.3 | 84% |
| canned_kangaroo_pasta | Kangaroo Pasta (26) | 2 | 4 x (8 + 12) | 80 | 52 | 64 | 81% |
| canned_lobster_pasta | Lobster Pasta (26) | 2 | 4 x (8 + 12) | 80 | 52 | 64 | 81% |
| canned_duck_pasta | Duck Noodles (31.2) | 2 | 4 x (9 + 14.4) | 93.6 | 62.4 | 74.9 | 83% |
| canned_pasta_with_meatballs | Pasta with Meatballs (31.2) | 2 | 4 x (9 + 14.4) | 93.6 | 62.4 | 74.9 | 83% |
| canned_pasta_with_mutton | Pasta with Mutton Chop (31.2) | 2 | 4 x (9 + 14.4) | 93.6 | 62.4 | 74.9 | 83% |
| canned_pearl_pasta | Pearl Pasta (7.2) | 2 | 4 x (3 + 2.4) | 21.6 | 14.4 | 17.3 | 83% |
| canned_squid_ink_pasta | Squid Ink Pasta (35) | 2 | 4 x (11 + 15.8) | 107 | 70 | 85.6 | 82% |
| canned_vegetable_pasta | Vegetable Noodles (35) | 2 | 4 x (11 + 15.8) | 107 | 70 | 85.6 | 82% |

**pie**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_apple_pie_filling | Slice of Apple Pie (4.8) | 4 | 4 x (3 + 1.8) | 19.2 | 19.2 | 15.4 | 125% |
| canned_chocolate_pie_filling | Slice of Chocolate Pie (4.8) | 4 | 4 x (3 + 1.8) | 19.2 | 19.2 | 15.4 | 125% |
| canned_chorus_pie_filling | Chorus Pie Slice (4.8) | 4 | 4 x (3 + 1.8) | 19.2 | 19.2 | 15.4 | 125% |
| canned_mulberry_pie_filling | Slice of Mulberry Pie (4.8) | 4 | 4 x (3 + 1.8) | 19.2 | 19.2 | 15.4 | 125% |
| canned_pumpkin_pie_filling | Slice of Pumpkin Pie (4.8) | 4 | 4 x (3 + 1.8) | 19.2 | 19.2 | 15.4 | 125% |
| canned_rose_hip_pie_filling | Rose Hip Pie Slice (4.8) | 4 | 4 x (3 + 1.8) | 19.2 | 19.2 | 15.4 | 125% |
| canned_salmonberry_pie_filling | Slice of Salmonberry Pie (4.8) | 4 | 4 x (3 + 1.8) | 19.2 | 19.2 | 15.4 | 125% |
| canned_durian_pie_filling | Durian Pie (9) | 4 | 4 x (5 + 4) | 36 | 36 | 28.8 | 125% |
| canned_pineapple_pie_filling | Pineapple Pie (9) | 4 | 4 x (5 + 4) | 36 | 36 | 28.8 | 125% |

**feast**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_caramel_popcorn | popcorn_box (-) | 1 | 6 x (5 + 6) | 66 | - | 52.8 | - |
| canned_hoglin_feast | stuffed_hoglin (-) | 1 | 6 x (10 + 15) | 150 | - | 120 | - |
| canned_honey_glazed_ham | honey_glazed_ham_block (-) | 1 | 6 x (14 + 21) | 210 | - | 168 | - |
| canned_nachos | nachos_block (-) | 1 | 6 x (12 + 24) | 216 | - | 172.8 | - |
| canned_pineapple_fried_rice | pineapple_fried_rice (-) | 1 | 6 x (14 + 22.4) | 218.4 | - | 174.7 | - |
| canned_roast_chicken | roast_chicken_block (-) | 1 | 6 x (14 + 21) | 210 | - | 168 | - |
| canned_shepherds_pie | shepherds_pie_block (-) | 1 | 6 x (14 + 21) | 210 | - | 168 | - |
| canned_stuffed_pumpkin | stuffed_pumpkin_block (-) | 1 | 6 x (14 + 21) | 210 | - | 168 | - |
| canned_stuffed_shulker | stuffed_shulker (-) | 1 | 6 x (7 + 12.6) | 117.6 | - | 94.1 | - |

**drink**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_honey_bottles | Honey Bottle (7.2) | 4 | 4 x (6 + 1.2) | 28.8 | 28.8 | 23 | 125% |
| canned_powdered_milk | minecraft_milk_bucket (-) | 4 | 4 x (1 + 0.2) | 4.8 | - | 3.8 | - |

**salad**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_bowl_of_exotic_fruits | Bowl of Exotic Fruits (22) | 6 | 8 x (14 + 13.8) | 222 | 132 | 177.6 | 74% |
| canned_chicken_salad | Chicken Salad (13.2) | 6 | 8 x (8 + 9) | 135.9 | 79.2 | 108.7 | 73% |
| canned_field_salad | Field Salad (21) | 6 | 8 x (13 + 13.8) | 214 | 126 | 171.2 | 74% |
| canned_fruit_salad | Fruit Salad (13.2) | 6 | 8 x (8 + 9) | 135.9 | 79.2 | 108.7 | 73% |
| canned_lush_moss_salad | Lush Moss Salad (16) | 6 | 8 x (10 + 10) | 160 | 96 | 128 | 75% |
| canned_mango_salad | Mango Salad (17.6) | 6 | 8 x (10 + 12) | 176 | 105.6 | 140.8 | 75% |
| canned_mixed_salad | Mixed Salad (13.2) | 6 | 8 x (8 + 9) | 135.9 | 79.2 | 108.7 | 73% |
| canned_nether_salad | Nether Salad (9) | 6 | 8 x (6 + 5) | 88 | 54 | 70.4 | 77% |

**golden**

| Can | Input (value) | Count | Servings x meal | Can | In | Goal | Share |
|---|---|---:|---|---:|---:|---:|---:|
| canned_golden_apple | Golden Apple (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_baked_potato | item.goldenfood.golden_baked_potato (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_beetroot | item.goldenfood.golden_beetroot (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_beetroot_soup | item.goldenfood.golden_beetroot_soup (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_bread | item.goldenfood.golden_bread (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_ce_golden_carrot | Golden Carrot (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_chorus_fruit | item.goldenfood.golden_chorus_fruit (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_cooked_beef | item.goldenfood.golden_cooked_beef (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_cooked_chicken | item.goldenfood.golden_cooked_chicken (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_cooked_cod | item.goldenfood.golden_cooked_cod (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_cooked_mutton | item.goldenfood.golden_cooked_mutton (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_cooked_porkchop | item.goldenfood.golden_cooked_porkchop (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_cooked_rabbit | item.goldenfood.golden_cooked_rabbit (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_cooked_salmon | item.goldenfood.golden_cooked_salmon (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_cookie | item.goldenfood.golden_cookie (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_dried_kelp | item.goldenfood.golden_dried_kelp (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_glow_berries | item.goldenfood.golden_glow_berries (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_honey_bottle | item.goldenfood.golden_honey_bottle (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_melon_slice | item.goldenfood.golden_melon_slice (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_milk_bucket | item.goldenfood.golden_milk_bucket (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_moss_clump | Golden Moss Clump (8.8) | 2 | 10 x (1 + 1.2) | 22 | 17.6 | 17.6 | 100% |
| canned_golden_mushroom_stew | item.goldenfood.golden_mushroom_stew (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_pumpkin_pie | item.goldenfood.golden_pumpkin_pie (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_rabbit_stew | item.goldenfood.golden_rabbit_stew (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_golden_sweet_berries | item.goldenfood.golden_sweet_berries (13.6) | 2 | 10 x (1 + 2.4) | 34 | 27.2 | 27.2 | 100% |
| canned_enchanted_golden_apple | Enchanted Golden Apple (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_baked_potato | item.goldenfood.enchanted_golden_baked_potato (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_beetroot | item.goldenfood.enchanted_golden_beetroot (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_beetroot_soup | item.goldenfood.enchanted_golden_beetroot_soup (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_bread | item.goldenfood.enchanted_golden_bread (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_carrot | item.goldenfood.enchanted_golden_carrot (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_chorus_fruit | item.goldenfood.enchanted_golden_chorus_fruit (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_cooked_beef | item.goldenfood.enchanted_golden_cooked_beef (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_cooked_chicken | item.goldenfood.enchanted_golden_cooked_chicken (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_cooked_cod | item.goldenfood.enchanted_golden_cooked_cod (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_cooked_mutton | item.goldenfood.enchanted_golden_cooked_mutton (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_cooked_porkchop | item.goldenfood.enchanted_golden_cooked_porkchop (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_cooked_rabbit | item.goldenfood.enchanted_golden_cooked_rabbit (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_cooked_salmon | item.goldenfood.enchanted_golden_cooked_salmon (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_cookie | item.goldenfood.enchanted_golden_cookie (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_dried_kelp | item.goldenfood.enchanted_golden_dried_kelp (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_glow_berries | item.goldenfood.enchanted_golden_glow_berries (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_honey_bottle | item.goldenfood.enchanted_golden_honey_bottle (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_melon_slice | item.goldenfood.enchanted_golden_melon_slice (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_milk_bucket | item.goldenfood.enchanted_golden_milk_bucket (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_moss_clump | Enchanted Golden Moss Clump (15.6) | 2 | 10 x (2 + 2.4) | 44 | 31.2 | 35.2 | 89% |
| canned_enchanted_golden_mushroom_stew | item.goldenfood.enchanted_golden_mushroom_stew (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_pumpkin_pie | item.goldenfood.enchanted_golden_pumpkin_pie (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_rabbit_stew | item.goldenfood.enchanted_golden_rabbit_stew (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |
| canned_enchanted_golden_sweet_berries | item.goldenfood.enchanted_golden_sweet_berries (136) | 2 | 10 x (2 + 32) | 340 | 272 | 272 | 100% |

**Golden can numbers:** today a golden can serves the golden food's full value for 10 servings ([cans.md](cans.md) F3);
the decision cuts that to a quarter of the nutrition and a third of the effect time, so ten servings are worth about 2.5
golden foods of nutrition and 3.3 of effect time.

**Follow-ups to mark:** golden food effect changes (see [effects.md](effects.md)) must be rechecked against the golden
cans; the Creative Canning rebuild needs its own tests and a name audit.

## 5. File map

- [kubejs/startup_scripts/items/cans.js](../kubejs/startup_scripts/items/cans.js): hand-written cans, generated can registration, Canned Goods food overrides
- [kubejs/server_scripts/cooking/canning.js](../kubejs/server_scripts/cooking/canning.js): recipes, section goals, plans for pie/feast/meal/drink/special
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

**Group sprites:** a can without its own borrows its group's: raw veggie -> carrot, raw fruit and special -> apple, raw
meat -> raw beef, raw fish -> raw salmon (cans.js for the hand-written cans, `GROUP_SPRITE` in gen_can_types.py for the
rest). The generator checks for `<id>.png` and leaves `texture` null when the can has its own. Other sections still use
Canned Goods' beetroot can. Never override `canned_goods:items/canned_beets`: those cans point at it.

## 6. Offline checks

- After a restart: `/dumpfood`, then `python3 tools/food_catalog.py --check`. Removed cans should be gone from the
  export and from the `canned` map; the new `canned_white_strawberry` and `canned_cactus_flesh` should be present.
- `logs/latest.log`: / `[canning] ... skipped` and / `[can servings] not item ids in item_dict` lines.
- 2026-10-13 dry run (node, stubbed NCH): canning.js makes 381 recipes, every input is an item_dict id, no duplicate
  recipe ids.

## 7. In-game tests

- [ ] **CP-01 removed cans gone:** JEI search "Canned Mint", "Canned Acorns", "Canned Grapes", "Canned Mite Crust",
  "Canned Raw Ham" (and chevon, hoglin, kangaroo, moose, bison, bunfungus) -> / results.
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
- [ ] **CP-09 sliced inputs (2026-10-20):** 5 Minced Beef, 5 Raw Beef, or a mix -> a full Canned Raw Beef; 8 Raw Chicken
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

## 8. History

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
