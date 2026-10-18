# Can parity worksheet

Written 2026-10-13. Test prefix `CP-`. A hand-kept worksheet: one row per food in each canning family, so every food
that should have a can gets one, and every can gets a recipe, texture, value audit and effect check. Edit the cells
directly; nothing regenerates this file.

## 0. At a glance

| Part | Status |
|---|---|
| Raw veggie / fruit / meat cans (hand-written, [cans.js](../kubejs/startup_scripts/items/cans.js)) | 2026-10-13: removed mint, acorn, grapes, mite crust, raw ham/chevon/hoglin/kangaroo/moose/bison/bunfungus; White Strawberries got their own can; Canned Cactus is now Canned Cactus Flesh (and the item Cactus Chunk is renamed Cactus Flesh). Not run in game (CP-01 to CP-05) |
| Generated cans (cookies, cooked, bread, salad, soup, golden; [can_types.json](../kubejs/generation_scripts/food/can_types.json)) | STAGED for 1.0.6, untested in game (see [food.md](food.md)) |
| Every remaining `x` row, `/` removals, alt inputs (2026-10-13) | BUILT, not run in game (CP-07 to CP-14): 17 raw fruit, shulker, 12 raw fish, 4 hazard, 2 special, 11 cookies, 10 cooked veggie, 3 cooked meat, 11 pastas, 9 pie fillings, 9 feasts, 2 drinks; 15 `/` cans removed; sliced alt inputs. 6 pie slices skipped (no source). Meal/pie/feast/drink/hazard values are placeholders ([3.1](#new-sections)) |
| Hunger/saturation columns | postponed until the food values are settled ([food.md](food.md) tuning) |

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

Counts at the time of writing. Update by hand when a family changes a lot.

| Family | Items | Want x | Want ? | Want / | Implemented | Own texture | Recipe | Effects carried |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| [Raw veggie](#raw-veggie) | 17 | 10 | 2 | 5 | 13 | 12 | 13 | 0 |
| [Raw fruit](#raw-fruit) | 41 | 28 | 1 | 12 | 28 | 11 | 28 | 0 |
| [Raw meat](#raw-meat) | 23 | 6 | 0 | 17 | 6 | 3 | 6 | 1 |
| [Raw fish](#raw-fish) | 12 | 12 | 0 | 0 | 12 | 3 | 12 | 4 |
| [Cookies](#cookies) | 12 | 12 | 0 | 0 | 12 | 1 | 12 | 6 |
| [Cooked veggie and fruit](#cooked-veggie) | 21 | 17 | 4 | 0 | 17 | 2 | 17 | 8 |
| [Cooked meat](#cooked-meat) | 28 | 8 | 0 | 20 | 8 | 5 | 8 | 1 |
| [Cooked fish](#cooked-fish) | 11 | 8 | 0 | 3 | 8 | 2 | 8 | 0 |
| [Bread](#bread) | 12 | 8 | 0 | 4 | 8 | 1 | 8 | 3 |
| [Salad](#salad) | 11 | 8 | 0 | 3 | 8 | 0 | 8 | 6 |
| [Soup](#soup) | 31 | 27 | 0 | 4 | 27 | 2 | 27 | 22 |
| [Meals](#meal) | 64 | 11 | 49 | 4 | 11 | 0 | 11 | 10 |
| [Pie](#pie) | 18 | 16 | 0 | 2 | 10 | 0 | 10 | 10 |
| [Feasts](#feast) | 12 | 9 | 0 | 3 | 9 | 0 | 9 | 8 |
| [Drinks](#drink) | 63 | 2 | 61 | 0 | 2 | 1 | 2 | 0 |
| [Drinks: alcohol](#alcohol) | 41 | 0 | 41 | 0 | 0 | 0 | 0 | 0 |
| [Golden and enchanted golden](#golden) | 50 | 50 | 0 | 0 | 50 | 0 | 50 | 50 |
| [Special](#special) | 15 | 2 | 13 | 0 | 2 | 1 | 2 | 0 |
| **Total** | **482** | **234** | **171** | **77** | **231** | **44** | **231** | **129** |

## 2. Sheets

### 2.1 Raw veggie <a id="raw-veggie"></a>

Baked Potato meals (5, 6), 2 servings. Additive: salt or bone broth.

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Beetroot `minecraft:beetroot` | Canned Beetroot | - | x | x | x | / | none | later | later | later |
| x | Bell Pepper `rusticdelight:bell_pepper_*` | Canned Bell Pepper | - | x | x | x | / | none | later | later | later |
| x | Cabbage `farmersdelight:cabbage` | Canned Cabbage | Cabbage Leaf: x | x | x | x | / | none | later | later | later |
| x | Cactus Flesh `delightful:cactus_flesh` | Canned Cactus Flesh | - | x | / | x | / | none | later | later | later |
| x | Carrot `minecraft:carrot` | Canned Carrot | - | x | x | x | / | none | later | later | later |
| x | Gongylidia `alexsmobs:gongylidia` | Canned Gongylidia | - | x | x | x | / | none | later | later | later |
| x | Onion `farmersdelight:onion` | Canned Onion | - | x | x | x | / | none | later | later | later |
| x | Potato `minecraft:potato` | Canned Potato | Potato Slices: x | x | x | x | / | none | later | later | later |
| x | Pumpkin Slice `farmersdelight:pumpkin_slice` | Canned Pumpkin | - | x | x | x | / | none | later | later | later |
| x | Tomato `farmersdelight:tomato` | Canned Tomato | - | x | x | x | / | none | later | later | later |
| xF | Corn Kernel `corn_delight:corn_seeds` | Canned Corn Kernel | - | - | - | - | - | none | - | - | - |
| x | Maize `corn_delight:corn` | Canned Corn | - | - | - | - | - | none | - | - | - |
| / | Green Bell Pepper `rusticdelight:bell_pepper_green` | Canned Bell Pepper | - | x | x | x | / | none | later | later | later |
| / | Red Bell Pepper `rusticdelight:bell_pepper_red` | Canned Bell Pepper | - | x | x | x | / | none | later | later | later |
| / | Yellow Bell Pepper `rusticdelight:bell_pepper_yellow` | Canned Bell Pepper | - | x | x | x | / | none | later | later | later |
| / | Mint Leaves `neapolitan:mint_leaves` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Diced Potatoes `moredelight:diced_potatoes` | Canned Diced Potatoes | - | - | - | - | - | none | - | - | - |

### 2.2 Raw fruit <a id="raw-fruit"></a>

Fruit Meals (5, 3), 3 servings. Additive: sugar or honey.

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Apple `minecraft:apple` | Canned Apple | - | x | x | x | / | none | later | later | later |
| x | Banana `neapolitan:banana` | Canned Banana | - | x | x | x | / | / | later | later | later |
| x | Cantaloupe Slice `delightful:cantaloupe_slice` | Canned Cantaloupe | - | x | x | x | / | / | later | later | later |
| x | Cherry `vinery:cherry` | Canned Cherries | - | x | x | x | / | none | later | later | later |
| x | Glow Berries `minecraft:glow_berries` | Canned Glow Berries | - | x | x | x | / | / | later | later | later |
| x | Melon Slice `minecraft:melon_slice` | Canned Melon | - | x | x | x | / | none | later | later | later |
| x | Mulberry `upgrade_aquatic:mulberry` | Canned Mulberries | - | x | x | x | / | none | later | later | later |
| x | Salmonberries `delightful:salmonberries` | Canned Salmonberries | - | x | x | x | / | / | later | later | later |
| x | Strawberries `neapolitan:strawberries` | Canned Strawberries | - | x | x | x | / | none | later | later | later |
| x | White Strawberries `neapolitan:white_strawberries` | Canned White Strawberries | - | x | / | x | / | none | later | later | later |
| x | Sweet Berries `minecraft:sweet_berries` | Canned Sweet Berries | - | x | x | x | / | none | later | later | later |
| x | Bayberry `fruitsdelight:bayberry` | Canned Bayberries | - | x | - | x | / | none | later | later | later |
| x | Blueberry `fruitsdelight:blueberry` | Canned Blueberries | - | x | - | x | / | none | later | later | later |
| x | Coffee Berries `farmersrespite:coffee_berries` | Canned Coffee Berries | - | - | - | - | - | - | - | - | - |
| x | Cranberry `fruitsdelight:cranberry` | Canned Cranberries | - | x | x | x | / | none | later | later | later |
| x | Durian Flesh `fruitsdelight:durian_flesh` | Canned Durian | - | x | - | x | / | none | later | later | later |
| x | Fig `fruitsdelight:fig` | Canned Figs | - | x | - | x | / | none | later | later | later |
| x | Hamimelon Slice `fruitsdelight:hamimelon_slice` | Canned Hamimelon | - | x | - | x | / | none | later | later | later |
| x | Hawberry `fruitsdelight:hawberry` | Canned Hawberries | - | x | - | x | / | none | later | later | later |
| x | Kiwi `fruitsdelight:kiwi` | Canned Kiwi | - | x | - | x | / | none | later | later | later |
| x | Lemon `fruitsdelight:lemon` | Canned Lemon | Lemon Slice: x | x | - | x | / | none | later | later | later |
| x | Lychee `fruitsdelight:lychee` | Canned Lychee | - | x | - | x | / | none | later | later | later |
| x | Mango `fruitsdelight:mango` | Canned Mango | - | x | - | x | / | none | later | later | later |
| x | Mangosteen `fruitsdelight:mangosteen` | Canned Mangosteen | - | x | - | x | / | none | later | later | later |
| x | Orange `fruitsdelight:orange` | Canned Orange | Orange Slice: x | x | - | x | / | none | later | later | later |
| x | Peach `fruitsdelight:peach` | Canned Peach | - | x | - | x | / | none | later | later | later |
| x | Pear `fruitsdelight:pear` | Canned Pear | - | x | - | x | / | none | later | later | later |
| x | Persimmon `fruitsdelight:persimmon` | Canned Persimmon | - | x | - | x | / | none | later | later | later |
| x | Pineapple Slice `fruitsdelight:pineapple_slice` | Canned Pineapple | - | x | - | x | / | none | later | later | later |
| / | Acorn `delightful:acorn` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Lemon Slice `fruitsdelight:lemon_slice` (alt input of Canned Lemon) | Canned Lemon Slice | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Orange Slice `fruitsdelight:orange_slice` (alt input of Canned Orange) | Canned Orange Slice | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Banana `alexsmobs:banana` (not canned, merged into the Neapolitan banana can) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Red Grape `vinery:red_grape` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Red Jungle Grapes `vinery:jungle_grapes_red` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Red Savanna Grapes `vinery:savanna_grapes_red` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Red Taiga Grapes `vinery:taiga_grapes_red` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | White Grape `vinery:white_grape` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | White Jungle Grapes `vinery:jungle_grapes_white` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | White Savanna Grapes `vinery:savanna_grapes_white` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | White Taiga Grapes `vinery:taiga_grapes_white` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

- removing alex's mobs banana, merge w/ neapolitan (done 2026-10-13: the can only takes Neapolitan bananas)

### 2.3 Raw meat <a id="raw-meat"></a>

Meat Meals (5, 3), 4 servings. Additive: salt or bone broth.

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Raw Beef `minecraft:beef` | Canned Raw Beef | Minced Beef: x | x | x | x | / | none | later | later | later |
| x | Raw Chicken `minecraft:chicken` | Canned Raw Chicken | Raw Chicken Cuts: x (half can) | x | / | x | / | / | later | later | later |
| x | Raw Mutton `minecraft:mutton` | Canned Raw Mutton | Raw Mutton Chops: x (half can) | x | x | x | / | none | later | later | later |
| x | Raw Porkchop `minecraft:porkchop` | Canned Raw Pork | Raw Bacon: x | x | x | x | / | none | later | later | later |
| x | Raw Rabbit `minecraft:rabbit` | Canned Raw Rabbit | - | x | / | x | / | none | later | later | later |
| x | Shulker Mollusk `endersdelight:shulker_mollusk` | Canned Shulker Flesh | Shulker Filet: x | x | / | x | / | x | later | later | later |
| / | Ground Strider `nethersdelight:ground_strider` | Canned Ground Strider | - | - | - | - | - | - | - | - | - |
| / | Hoglin Ear `nethersdelight:hoglin_ear` | Canned Hoglin Ear | - | - | - | - | - | none | - | - | - |
| / | Raw Duck `untitledduckmod:raw_duck` | Canned Raw Duck | - | - | - | - | - | none | - | - | - |
| / | Raw Duck Fillet `abnormals_delight:duck_fillet` | Canned Raw Duck Fillet | - | - | - | - | - | - | - | - | - |
| / | Raw Goose `untitledduckmod:raw_goose` | Canned Raw Goose | - | - | - | - | - | none | - | - | - |
| / | Raw Mint Chops `neapolitan:mint_chops` | Canned Raw Mint Chops | - | - | - | - | - | - | - | - | - |
| / | Raw Venison Chops `delightful:venison_chops` | Canned Raw Venison Chops | - | - | - | - | - | none | - | - | - |
| / | Raw Venison Shanks `abnormals_delight:venison_shanks` | Canned Raw Venison Shanks | - | - | - | - | - | none | - | - | - |
| / | Strider Slice `nethersdelight:strider_slice` | Canned Strider Slice | - | - | - | - | - | - | - | - | - |
| / | Ham `farmersdelight:ham` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Hoglin Loin `nethersdelight:hoglin_loin` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Kangaroo Meat `alexsmobs:kangaroo_meat` (removed 2026-10-13) | n/a | Kangaroo Shank | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Mite Crust `endersdelight:mite_crust` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Raw Bison `alexsdelight:raw_bison` (removed 2026-10-13) | n/a | Bison Mince | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Raw Bunfungus `alexsdelight:raw_bunfungus` (removed 2026-10-13) | n/a | Raw Bunfungus Drumstick | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Raw Chevon `delightful:raw_goat` (removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Raw Moose Ribs `alexsmobs:moose_ribs` (removed 2026-10-13) | n/a | Loose Moose Rib | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.4 Raw fish <a id="raw-fish"></a>

Raw meat's goal: Meat Meals (5, 3), 4 servings, salt or bone broth. Generated (section raw_fish). Every sliced fish is an
alt input: 8 slices make a half full can (2 of 4 servings).

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Blobfish `alexsmobs:blobfish` | Canned Blobfish | - | x | / | x | / | x | later | later | later |
| x | Flying Fish `alexsmobs:flying_fish` | Canned Flying Fish | - | x | / | x | / | none | later | later | later |
| x | Lobster Tail `alexsmobs:lobster_tail` | Canned Lobster Tail | - | x | / | x | / | none | later | later | later |
| x | Raw Calamari `rusticdelight:calamari` | Canned Raw Calamari | Raw Calamari Slice: x (half can) | x | / | x | / | none | later | later | later |
| x | Raw Catfish `alexsmobs:raw_catfish` | Canned Raw Catfish | Raw Catfish Slice: x (half can) | x | / | x | / | none | later | later | later |
| x | Raw Cod `minecraft:cod` | Canned Raw Cod | Raw Cod Slice: x (half can) | x | x | x | / | none | later | later | later |
| x | Raw Crab Leg `quark:crab_leg` | Canned Raw Crab Leg | - | x | / | x | / | none | later | later | later |
| x | Raw Lionfish `upgrade_aquatic:lionfish` | Canned Raw Lionfish | - | x | / | x | / | x | later | later | later |
| x | Raw Perch `upgrade_aquatic:perch` | Canned Raw Perch | Raw Perch Slice: x (half can) | x | / | x | / | x | later | later | later |
| x | Raw Pike `upgrade_aquatic:pike` | Canned Raw Pike | Raw Pike Slice: x (half can) | x | / | x | / | x | later | later | later |
| x | Raw Salmon `minecraft:salmon` | Canned Raw Salmon | Raw Salmon Slice: x (half can) | x | x | x | / | none | later | later | later |
| x | Tropical Fish `minecraft:tropical_fish` | Canned Tropical Fish | - | x | x | x | / | none | later | later | later |

### 2.5 Cookies <a id="cookies"></a>

Cookie x2 meals, 5 servings, fast. Additive: sugar.

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Cookie `minecraft:cookie` | Canned Cookies | - | x | x | x | / | none | later | later | later |
| x | Bayberry Cookie `fruitsdelight:bayberry_cookie` | Canned Bayberry Cookies | - | x | - | x | / | x | later | later | later |
| x | Cherry Cookie `abnormals_delight:cherry_cookie` | Canned Cherry Cookies | - | x | - | x | / | none | later | later | later |
| x | Cranberry Cookie `fruitsdelight:cranberry_cookie` | Canned Cranberry Cookies | - | x | - | x | / | x | later | later | later |
| x | Green Tea Cookie `farmersrespite:green_tea_cookie` | Canned Green Tea Cookies | - | x | - | x | / | x | later | later | later |
| x | Honey Cookie `farmersdelight:honey_cookie` | Canned Honey Cookies | - | x | - | x | / | none | later | later | later |
| x | Lemon Cookie `fruitsdelight:lemon_cookie` | Canned Lemon Cookies | - | x | - | x | / | x | later | later | later |
| x | Maple Cookie `abnormals_delight:maple_cookie` | Canned Maple Cookies | - | x | - | x | / | none | later | later | later |
| x | Mulberry Cookie `abnormals_delight:mulberry_cookie` | Canned Mulberry Cookies | - | x | - | x | / | none | later | later | later |
| x | Persimmon Cookie `fruitsdelight:persimmon_cookie` | Canned Persimmon Cookies | - | x | - | x | / | x | later | later | later |
| x | Sweet Berry Cookie `farmersdelight:sweet_berry_cookie` | Canned Sweet Berry Cookies | - | x | - | x | / | none | later | later | later |
| x | Uncanny Cookie `endersdelight:uncanny_cookies` | Canned Uncanny Cookies | - | x | - | x | / | x | later | later | later |

### 2.6 Cooked veggie and fruit <a id="cooked-veggie"></a>

Item x2 meals, 4 servings. Additive: salt.

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Baked Potato `minecraft:baked_potato` | Canned Baked Potato | Baked Potato Slices: x | x | x | x | / | none | later | later | later |
| x | Cactus Steak `delightful:cactus_steak` | Canned Cactus Steak | - | x | / | x | / | none | later | later | later |
| x | Cooked Rice `farmersdelight:cooked_rice` | Canned Cooked Rice | - | x | / | x | / | x | later | later | later |
| x | Dried Kelp `minecraft:dried_kelp` | Canned Dried Kelp | - | x | x | x | / | none | later | later | later |
| x | Roasted Green Bell Pepper `rusticdelight:roasted_bell_pepper_green` | Canned Roasted Bell Pepper | - | x | / | x | / | none | later | later | later |
| x | Roasted Red Bell Pepper `rusticdelight:roasted_bell_pepper_red` | Canned Roasted Bell Pepper | - | x | / | x | / | none | later | later | later |
| x | Roasted Yellow Bell Pepper `rusticdelight:roasted_bell_pepper_yellow` | Canned Roasted Bell Pepper | - | x | / | x | / | none | later | later | later |
| x | Baked Durian `fruitsdelight:baked_durian` | Canned Baked Durian | - | x | - | x | / | x | later | later | later |
| x | Baked Pear `fruitsdelight:baked_pear` | Canned Baked Pear | - | x | - | x | / | x | later | later | later |
| x | Boiled Blue Pickerelweed `upgrade_aquatic:boiled_blue_pickerelweed` | Canned Boiled Blue Pickerelweed | - | x | - | x | / | none | later | later | later |
| x | Boiled Corn `corn_delight:boiled_corn` | Canned Boiled Corn | - | x | - | x | / | none | later | later | later |
| x | Boiled Purple Pickerelweed `upgrade_aquatic:boiled_purple_pickerelweed` | Canned Boiled Purple Pickerelweed | - | x | - | x | / | none | later | later | later |
| x | Chips `pneumaticcraft:chips` | Canned Chips | - | - | - | - | - | none | - | - | - |
| x | Dried Banana `neapolitan:dried_banana` | Canned Dried Banana | - | x | - | x | / | x | later | later | later |
| x | Dried Persimmon `fruitsdelight:dried_persimmon` | Canned Dried Persimmon | - | x | - | x | / | x | later | later | later |
| x | Grilled Corn `corn_delight:grilled_corn` | Canned Grilled Corn | - | - | - | - | - | none | - | - | - |
| x | Kimchi `brewinandchewin:kimchi` | Canned Kimchi | - | x | - | x | / | x | later | later | later |
| x | Pickled Pickles `brewinandchewin:pickled_pickles` | Canned Pickled Pickles | - | x | - | x | / | x | later | later | later |
| / | Popcorn `corn_delight:popcorn` | Canned Popcorn | - | - | - | - | - | none | - | - | - |
| x | Roasted Adzuki Beans `neapolitan:roasted_adzuki_beans` | Canned Roasted Adzuki Beans | - | x | - | x | / | x | later | later | later |
| x | Tortilla Chip `corn_delight:tortilla_chip` | Canned Tortilla Chip | - | - | - | - | - | none | - | - | - |

### 2.7 Cooked meat <a id="cooked-meat"></a>

Item x2 meals, 3 servings. Additive: salt or bone broth.

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| / | Beef Patty `farmersdelight:beef_patty` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Bison Patty `alexsdelight:bison_patty` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Bacon `farmersdelight:cooked_bacon` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Bison `alexsdelight:cooked_bison` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Bunfungus `alexsdelight:cooked_bunfungus` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Chevon `delightful:cooked_goat` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Cooked Chicken `minecraft:cooked_chicken` | Canned Cooked Chicken | Cooked Chicken Cuts: x | x | x | x | / | none | later | later | later |
| / | Cooked Kangaroo Meat `alexsmobs:cooked_kangaroo_meat` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Kangaroo Shank `alexsdelight:cooked_kangaroo_shank` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Loose Moose Rib `alexsdelight:cooked_loose_moose_rib` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Moose Ribs `alexsmobs:cooked_moose_ribs` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Cooked Mutton `minecraft:cooked_mutton` | Canned Cooked Mutten | Cooked Mutton Chops: x | x | x | x | / | none | later | later | later |
| x | Cooked Porkchop `minecraft:cooked_porkchop` | Canned Cooked Pork | - | x | x | x | / | none | later | later | later |
| x | Cooked Rabbit `minecraft:cooked_rabbit` | Canned Cooked Rabbit | - | x | x | x | / | none | later | later | later |
| / | Hoglin Sirloin `nethersdelight:hoglin_sirloin` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Smoked Ham `farmersdelight:smoked_ham` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Steak `minecraft:cooked_beef` | Canned Steak | - | x | x | x | / | none | later | later | later |
| / | Cooked Bunfungus Drumstick `alexsdelight:cooked_bunfungus_drumstick` | Canned Cooked Bunfungus Drumstick | - | - | - | - | - | none | - | - | - |
| / | Cooked Centipede Leg `alexsdelight:cooked_centipede_leg` | Canned Cooked Centipede Leg | - | - | - | - | - | none | - | - | - |
| / | Cooked Duck `untitledduckmod:cooked_duck` | Canned Cooked Duck | - | - | - | - | - | none | - | - | - |
| / | Cooked Duck Fillet `abnormals_delight:cooked_duck_fillet` | Canned Cooked Duck Fillet | - | - | - | - | - | none | - | - | - |
| / | Cooked Goose `untitledduckmod:cooked_goose` | Canned Cooked Goose | - | - | - | - | - | none | - | - | - |
| / | Cooked Mint Chops `neapolitan:cooked_mint_chops` | Canned Cooked Mint Chops | - | - | - | - | - | - | - | - | - |
| / | Cooked Venison Chops `delightful:cooked_venison_chops` | Canned Cooked Venison Chops | - | - | - | - | - | none | - | - | - |
| / | Cooked Venison Shanks `abnormals_delight:cooked_venison_shanks` | Canned Cooked Venison Shanks | - | - | - | - | - | none | - | - | - |
| x | Honey Glazed Porkchop `buzzier_bees:glazed_porkchop` | Canned Honey Glazed Porkchop | - | x | - | x | / | none | later | later | later |
| x | Jerky `brewinandchewin:jerky` | Canned Jerky | - | x | - | x | / | none | later | later | later |
| x | Maple Glazed Bacon `abnormals_delight:maple_glazed_bacon` | Canned Maple Glazed Bacon | - | x | - | x | / | x | later | later | later |

### 2.8 Cooked fish <a id="cooked-fish"></a>

Same section as cooked meat (cooked_meat in can_types.json).

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Cooked Calamari `rusticdelight:cooked_calamari` | Canned Cooked Calamari | Cooked Calamari Slice: x | x | / | x | / | none | later | later | later |
| x | Cooked Catfish `alexsmobs:cooked_catfish` | Canned Cooked Catfish | - | x | / | x | / | none | later | later | later |
| x | Cooked Catfish Slice `alexsdelight:cooked_catfish_slice` | Canned Cooked Catfish | - | x | / | x | / | none | later | later | later |
| x | Cooked Cod `minecraft:cooked_cod` | Canned Cooked Cod | Cooked Cod Slice: x | x | x | x | / | none | later | later | later |
| x | Cooked Lionfish `upgrade_aquatic:cooked_lionfish` | Canned Cooked Lionfish | - | x | / | x | / | none | later | later | later |
| x | Cooked Perch `upgrade_aquatic:cooked_perch` | Canned Cooked Perch | Cooked Perch Slice: x | x | / | x | / | none | later | later | later |
| x | Cooked Pike `upgrade_aquatic:cooked_pike` | Canned Cooked Pike | Cooked Pike Slice: x | x | / | x | / | none | later | later | later |
| x | Cooked Salmon `minecraft:cooked_salmon` | Canned Cooked Salmon | Cooked Salmon Slice: x | x | x | x | / | none | later | later | later |
| / | Cooked Crab Leg `quark:cooked_crab_leg` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Cooked Lobster Tail `alexsmobs:cooked_lobster_tail` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Kippers `brewinandchewin:kippers` | Canned Kippers | - | - | - | - | - | - | - | - | - |

### 2.9 Bread <a id="bread"></a>

Bread x2 meals, 4 servings, / additive (honey bread also 5 bread + honey).

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Adzuki Bun `neapolitan:adzuki_bun` | Canned Adzuki Bun | - | x | / | x | / | x | later | later | later |
| x | Banana Bread `neapolitan:banana_bread` | Canned Banana Bread | - | x | / | x | / | x | later | later | later |
| x | Bread `minecraft:bread` | Canned Bread | - | x | x | x | / | none | later | later | later |
| / | Bread Slice `moredelight:bread_slice` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Cornbread `corn_delight:cornbread` | Canned Cornbread | - | x | / | x | / | none | later | later | later |
| x | Honey Bread `buzzier_bees:honey_bread` | Canned Honey Bread | - | x | / | x | / | none | later | later | later |
| x | Mulberry Bread `upgrade_aquatic:mulberry_bread` | Canned Mulberry Bread | - | x | / | x | / | none | later | later | later |
| x | Nether Wart Sourdough `farmersrespite:nether_wart_sourdough` | Canned Nether Wart Sourdough | - | x | / | x | / | x | later | later | later |
| x | Sourdough Bread `pneumaticcraft:sourdough_bread` | Canned Sourdough Bread | - | x | / | x | / | none | later | later | later |
| / | Toast `moredelight:toast` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Tortilla `corn_delight:tortilla` (not canned, removed 2026-10-13) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Strawberry Scones `neapolitan:strawberry_scones` (plan: all bread but scones) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.10 Salad <a id="salad"></a>

Salad x1.25 meals, 8 servings, fast, effects 0.75x. Vinegar additive later.

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Chicken Salad `moredelight:chicken_salad` | Canned Chicken Salad | - | x | / | x | / | x | later | later | later |
| x | Bowl of Exotic Fruits `quarkdelight:bowl_of_exotic_fruits` | Canned Exotic Fruits | - | x | / | x | / | none | later | later | later |
| x | Field Salad `delightful:field_salad` | Canned Field Salad | - | x | / | x | / | x | later | later | later |
| x | Fruit Salad `farmersdelight:fruit_salad` | Canned Fruit Salad | - | x | / | x | / | x | later | later | later |
| x | Lush Moss Salad `quarkdelight:lush_moss_salad` | Canned Lush Moss Salad | - | x | / | x | / | none | later | later | later |
| x | Mango Salad `fruitsdelight:mango_salad` | Canned Mango Salad | - | x | / | x | / | x | later | later | later |
| x | Mixed Salad `farmersdelight:mixed_salad` | Canned Mixed Salad | - | x | / | x | / | x | later | later | later |
| x | Nether Salad `farmersdelight:nether_salad` | Canned Nether Salad | - | x | / | x | / | x | later | later | later |
| / | Maggot Salad `alexsdelight:maggot_salad` (plan: all salads but maggot salad) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Potato Salad `moredelight:potato_salad` (plan: all salads but potato salad) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Potato Salad `rusticdelight:potato_salad` (plan: all salads but potato salad) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.11 Soup <a id="soup"></a>

Soup x1.25 meals, 8 servings, effects 0.75x. Additive: salt or bone broth (3 soups).

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Acacia Blossom Soup `alexsdelight:acacia_blossom_soup` | Canned Acacia Blossom Soup | - | x | / | x | / | x | later | later | later |
| x | Adzuki Stew `neapolitan:adzuki_stew` | Canned Adzuki Stew | - | x | / | x | / | x | later | later | later |
| x | Baked Cod Stew `farmersdelight:baked_cod_stew` | Canned Baked Cod Stew | - | x | / | x | / | x | later | later | later |
| x | Bayberry Soup `fruitsdelight:bayberry_soup` | Canned Bayberry Soup | - | x | / | x | / | x | later | later | later |
| x | Beef Stew `farmersdelight:beef_stew` | Canned Beef Stew | - | x | / | x | / | x | later | later | later |
| x | Beetroot Soup `minecraft:beetroot_soup` | Canned Beetroot Soup | - | x | / | x | / | none | later | later | later |
| x | Bell Pepper Soup `rusticdelight:bell_pepper_soup` | Canned Bell Pepper Soup | - | x | / | x | / | x | later | later | later |
| x | Blazing Chili `farmersrespite:blazing_chili` | Canned Blazing Chili | - | x | / | x | / | x | later | later | later |
| x | Bone Broth `farmersdelight:bone_broth` | Canned Bone Broth | - | x | / | x | / | x | later | later | later |
| x | Carrot Soup `moredelight:carrot_soup` | Canned Carrot Soup | - | x | / | x | / | x | later | later | later |
| x | Chicken Soup `farmersdelight:chicken_soup` | Canned Chicken Soup | - | x | / | x | / | x | later | later | later |
| x | Chorus Stew `endersdelight:chorus_stew` | Canned Chorus Stew | - | x | / | x | / | x | later | later | later |
| x | Corn Soup `corn_delight:corn_soup` | Canned Corn Soup | - | x | / | x | / | x | later | later | later |
| x | Creamy Onion Soup `brewinandchewin:creamy_onion_soup` | Canned Creamy Onion Soup | - | x | / | x | / | x | later | later | later |
| x | Endermite Stew `endersdelight:endermite_stew` | Canned Endermite Stew | - | x | / | x | / | x | later | later | later |
| x | Fig Chicken Stew `fruitsdelight:fig_chicken_stew` | Canned Fig Chicken Stew | - | x | / | x | / | x | later | later | later |
| x | Fish Stew `farmersdelight:fish_stew` | Canned Fish Stew | - | x | / | x | / | x | later | later | later |
| x | Kangaroo Stew `alexsdelight:kangaroo_stew` | Canned Kangaroo Stew | - | x | / | x | / | x | later | later | later |
| x | Miso with Bamboo Sprouts `quarkdelight:miso_with_bamboo_sprouts` | Canned Miso with Bamboo Sprouts | - | x | / | x | / | none | later | later | later |
| x | Mushroom Stew `minecraft:mushroom_stew` | Canned Mushroom Stew | - | x | x | x | / | none | later | later | later |
| x | Noodle Soup `farmersdelight:noodle_soup` | Canned Noodle Soup | - | x | / | x | / | x | later | later | later |
| x | Pumpkin Soup `farmersdelight:pumpkin_soup` | Canned Pumpkin Soup | - | x | / | x | / | x | later | later | later |
| x | Rabbit Stew `minecraft:rabbit_stew` | Canned Rabbit Stew | - | x | x | x | / | none | later | later | later |
| x | Sinigang `delightful:sinigang` | Canned Sinigang | - | x | / | x | / | x | later | later | later |
| x | Sopa De Macaco `alexsmobs:sopa_de_macaco` | Canned Sopa De Macaco | - | x | / | x | / | none | later | later | later |
| x | Strider Moss Stew `nethersdelight:strider_moss_stew` | Canned Strider Moss Stew | - | x | / | x | / | x | later | later | later |
| x | Vegetable Soup `farmersdelight:vegetable_soup` | Canned Vegetable Soup | - | x | / | x | / | x | later | later | later |
| / | Cavern Explorer Stew `quarkdelight:cavern_explorer_stew` (plan: all soups but cave explorer) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Chorus Stew `endersdelight:chorus_stew_wood` (wooden soup, removed) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Endermite Stew `endersdelight:endermite_stew_wood` (wooden soup, removed) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Suspicious Stew `minecraft:suspicious_stew` (effects live in NBT, a can would lose them) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.12 Meals <a id="meal"></a>

Placeholder goal: 2 pastas -> 4 meals of 0.75 pasta, effects 0.75x. Only the pastas are built (veg noodles -> Vegetable
Pasta, duck noodles -> Pasta with Duck).

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Bell Pepper Pasta `rusticdelight:bell_pepper_pasta` | Canned Bell Pepper Pasta | - | x | / | x | / | x | later | later | later |
| x | Cheesy Pasta `brewinandchewin:cheesy_pasta` | Canned Cheesy Pasta | - | x | / | x | / | x | later | later | later |
| x | Crab Pasta `quarkdelight:crab_pasta` | Canned Crab Pasta | - | x | / | x | / | none | later | later | later |
| x | Kangaroo Pasta `alexsdelight:kangaroo_pasta` | Canned Kangaroo Pasta | - | x | / | x | / | x | later | later | later |
| x | Lobster Pasta `alexsdelight:lobster_pasta` | Canned Lobster Pasta | - | x | / | x | / | x | later | later | later |
| x | Duck Noodles `abnormals_delight:duck_noodles` | Canned Pasta with Duck | - | x | / | x | / | x | later | later | later |
| x | Pasta with Meatballs `farmersdelight:pasta_with_meatballs` | Canned Pasta with Meatballs | - | x | / | x | / | x | later | later | later |
| x | Pasta with Mutton Chop `farmersdelight:pasta_with_mutton_chop` | Canned Pasta with Mutton Chop | - | x | / | x | / | x | later | later | later |
| x | Pearl Pasta `endersdelight:pearl_pasta` | Canned Pearl Pasta | - | x | / | x | / | x | later | later | later |
| x | Squid Ink Pasta `farmersdelight:squid_ink_pasta` | Canned Squid Ink Pasta | - | x | / | x | / | x | later | later | later |
| x | Vegetable Noodles `farmersdelight:vegetable_noodles` | Canned Vegetable Pasta | - | x | / | x | / | x | later | later | later |
| / | Adzuki Curry `neapolitan:adzuki_curry` | Canned Adzuki Curry | - | - | - | - | - | - | - | - | - |
| / | Bacon and Eggs `farmersdelight:bacon_and_eggs` | Canned Bacon and Eggs | - | - | - | - | - | - | - | - | - |
| / | Black Cod `farmersrespite:black_cod` | Canned Black Cod | - | - | - | - | - | - | - | - | - |
| / | Coconut Curry `delightful:coconut_curry` | Canned Coconut Curry | - | - | - | - | - | - | - | - | - |
| / | Coffee-Braised Beef `rusticdelight:coffee_braised_beef` | Canned Coffee-Braised Beef | - | - | - | - | - | - | - | - | - |
| / | Cooked Rice with Beef `moredelight:cooked_rice_with_beef` | Canned Cooked Rice with Beef | - | - | - | - | - | - | - | - | - |
| / | Cooked Rice with Chicken Cuts `moredelight:cooked_rice_with_chicken_cuts` | Canned Cooked Rice with Chicken Cuts | - | - | - | - | - | - | - | - | - |
| / | Cooked Rice with Porkchop `moredelight:cooked_rice_with_porkchop` | Canned Cooked Rice with Porkchop | - | - | - | - | - | - | - | - | - |
| / | Cooked Whole Crab `quarkdelight:cooked_whole_crab` | Canned Cooked Whole Crab | - | - | - | - | - | none | - | - | - |
| x | Cornbread Stuffing `corn_delight:cornbread_stuffing` | Canned Cornbread Stuffing | - | - | - | - | - | - | - | - | - |
| x | Creamed Corn `corn_delight:creamed_corn` | Canned Creamed Corn | - | - | - | - | - | - | - | - | - |
| x | Creamy Pasta with Chicken Cuts `moredelight:creamy_pasta_with_chicken_cuts` | Canned Creamy Pasta with Chicken Cuts | - | - | - | - | - | - | - | - | - |
| x | Creamy Pasta with Ham `moredelight:creamy_pasta_with_ham` | Canned Creamy Pasta with Ham | - | - | - | - | - | - | - | - | - |
| / | Diced Potatoes with Beef `moredelight:diced_potatoes_with_beef` | Canned Diced Potatoes with Beef | - | - | - | - | - | none | - | - | - |
| / | Diced Potatoes with Chicken Cuts `moredelight:diced_potatoes_with_chicken_cuts` | Canned Diced Potatoes with Chicken Cuts | - | - | - | - | - | none | - | - | - |
| / | Diced Potatoes with Egg and Tomato `moredelight:diced_potatoes_with_egg_and_tomato` | Canned Diced Potatoes with Egg and Tomato | - | - | - | - | - | none | - | - | - |
| / | Diced Potatoes with Porkchop `moredelight:diced_potatoes_with_porkchop` | Canned Diced Potatoes with Porkchop | - | - | - | - | - | none | - | - | - |
| / | Dune Platter `abnormals_delight:dune_platter` | Canned Dune Platter | - | - | - | - | - | - | - | - | - |
| / | Ender Paella `endersdelight:ender_paella` | Canned Ender Paella | - | - | - | - | - | - | - | - | - |
| / | Escargot `abnormals_delight:escargot` | Canned Escargot | - | - | - | - | - | none | - | - | - |
| / | Fiery Fondue `brewinandchewin:fiery_fondue` | Canned Fiery Fondue | - | - | - | - | - | - | - | - | - |
| / | Fried Calamari `rusticdelight:fried_calamari` | Canned Fried Calamari | - | - | - | - | - | - | - | - | - |
| / | Fried Chicken `rusticdelight:fried_chicken` | Canned Fried Chicken | - | - | - | - | - | - | - | - | - |
| / | Fried Mushrooms `rusticdelight:fried_mushrooms` | Canned Fried Mushrooms | - | - | - | - | - | - | - | - | - |
| / | Fried Rice `farmersdelight:fried_rice` | Canned Fried Rice | - | - | - | - | - | - | - | - | - |
| / | Grilled Salmon `farmersdelight:grilled_salmon` | Canned Grilled Salmon | - | - | - | - | - | - | - | - | - |
| / | Grilled Strider `nethersdelight:grilled_strider` | Canned Grilled Strider | - | - | - | - | - | - | - | - | - |
| / | Horror Lasagna `brewinandchewin:horror_lasagna` | Canned Horror Lasagna | - | - | - | - | - | - | - | - | - |
| / | Lychee Chicken `fruitsdelight:lychee_chicken` | Canned Lychee Chicken | - | - | - | - | - | - | - | - | - |
| / | Mashed Potatoes `moredelight:mashed_potatoes` | Canned Mashed Potatoes | - | - | - | - | - | none | - | - | - |
| / | Mushroom Rice `farmersdelight:mushroom_rice` | Canned Mushroom Rice | - | - | - | - | - | - | - | - | - |
| / | Omelet `moredelight:omelette` | Canned Omelet | - | - | - | - | - | none | - | - | - |
| / | Orange Chicken `fruitsdelight:orange_chicken` | Canned Orange Chicken | - | - | - | - | - | - | - | - | - |
| / | Orange Marinated Pork `fruitsdelight:orange_marinated_pork` | Canned Orange Marinated Pork | - | - | - | - | - | - | - | - | - |
| / | Passion Fruit Glazed Duck `abnormals_delight:passion_fruit_glazed_duck` | Canned Passion Fruit Glazed Duck | - | - | - | - | - | - | - | - | - |
| / | Perch with Mushrooms `abnormals_delight:perch_with_mushrooms` | Canned Perch with Mushrooms | - | - | - | - | - | - | - | - | - |
| / | Pike with Beetroot `abnormals_delight:pike_with_beetroot` | Canned Pike with Beetroot | - | - | - | - | - | - | - | - | - |
| / | Pineapple Marinated Pork `fruitsdelight:pineapple_marinated_pork` | Canned Pineapple Marinated Pork | - | - | - | - | - | - | - | - | - |
| / | Ratatouille `farmersdelight:ratatouille` | Canned Ratatouille | - | - | - | - | - | - | - | - | - |
| / | Roasted Mutton Chops `farmersdelight:roasted_mutton_chops` | Canned Roasted Mutton Chops | - | - | - | - | - | - | - | - | - |
| / | Scarlet Pierogies `brewinandchewin:scarlet_pierogies` | Canned Scarlet Pierogies | - | - | - | - | - | - | - | - | - |
| / | Seared Venison `abnormals_delight:seared_venison` | Canned Seared Venison | - | - | - | - | - | - | - | - | - |
| / | Shrimp-Fried Rice `alexsmobs:shrimp_fried_rice` | Canned Shrimp-Fried Rice | - | - | - | - | - | none | - | - | - |
| / | Slabdish `abnormals_delight:slabdish` | Canned Slabdish | - | - | - | - | - | - | - | - | - |
| / | Steak and Potatoes `farmersdelight:steak_and_potatoes` | Canned Steak and Potatoes | - | - | - | - | - | - | - | - | - |
| / | Tea Curry `farmersrespite:tea_curry` | Canned Tea Curry | - | - | - | - | - | - | - | - | - |
| / | Twisted Cereal `endersdelight:twisted_cereal` | Canned Twisted Cereal | - | - | - | - | - | - | - | - | - |
| / | Vegetable Omelet `brewinandchewin:vegetable_omelet` | Canned Vegetable Omelet | - | - | - | - | - | - | - | - | - |
| / | Venison with Bamboo Shoots `abnormals_delight:venison_with_bamboo_shoots` | Canned Venison with Bamboo Shoots | - | - | - | - | - | - | - | - | - |
| / | Warped Moldy Meat `nethersdelight:warped_moldy_meat` | Canned Warped Moldy Meat | - | - | - | - | - | - | - | - | - |
| / | Ender Paella `endersdelight:ender_paella_wood` (wooden variant, removed) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Pearl Pasta `endersdelight:pearl_pasta_wood` (plan: pearl pasta, not the wooden one) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Twisted Cereal `endersdelight:twisted_cereal_wood` (wooden variant, removed) | n/a | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

### 2.13 Pie <a id="pie"></a>

4 slices + can -> pie filling (4 servings, slice-sized meals); filling can + pie crust -> the whole pie. Fruitsdelight's
durian and pineapple pies are single items: 4 fill a can, no pie back.

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Slice of Apple Pie `farmersdelight:apple_pie_slice` | Canned Apple Pie Filling | - | x | / | x | / | x | later | later | later |
| x | Slice of Blueberry Pie `delightful:blueberry_pie_slice` (no source in the pack: no whole pie, nothing drops it) | Canned Blueberry Pie Filling | - | / | / | / | / | / | later | later | later |
| x | Slice of Cherry Pie `create_central_kitchen:cherry_pie_slice` (no source in the pack: no whole pie, nothing drops it) | Canned Cherry Pie Filling | - | / | / | / | / | / | later | later | later |
| x | Slice of Chocolate Pie `farmersdelight:chocolate_pie_slice` | Canned Chocolate Pie Filling | - | x | / | x | / | x | later | later | later |
| x | Chorus Pie Slice `endersdelight:chorus_pie_slice` | Canned Chorus Pie Filling | - | x | / | x | / | x | later | later | later |
| x | Slice of Crimson Berry Pie `delightful:crimson_berry_pie_slice` (no source in the pack: no whole pie, nothing drops it) | Canned Crimson Berry Pie Filling | - | / | / | / | / | / | later | later | later |
| x | Durian Pie `fruitsdelight:durian_pie` | Canned Durian Pie Filling | - | x | / | x | / | x | later | later | later |
| / | Slice of Gloomgourd Pie `delightful:gloomgourd_pie_slice` | Canned Gloomgourd Pie Filling | - | / | / | / | / | / | later | later | later |
| x | Slice of Green Apple Pie `delightful:green_apple_pie_slice` (no source in the pack: no whole pie, nothing drops it) | Canned Green Apple Pie Filling | - | / | / | / | / | / | later | later | later |
| x | Slice of Mulberry Pie `create_central_kitchen:mulberry_pie_slice` | Canned Mulberry Pie Filling | - | x | / | x | / | x | later | later | later |
| x | Slice of Nightshade Berry Pie `delightful:nightshade_berry_pie_slice` (no source in the pack: no whole pie, nothing drops it) | Canned Nightshade Berry Pie Filling | - | / | / | / | / | / | later | later | later |
| x | Pineapple Pie `fruitsdelight:pineapple_pie` | Canned Pineapple Pie Filling | - | x | / | x | / | x | later | later | later |
| x | Slice of Pumpkin Pie `delightful:pumpkin_pie_slice` | Canned Pumpkin Pie Filling | - | x | / | x | / | x | later | later | later |
| x | Slice of Pumpkin Pie `create_central_kitchen:pumpkin_pie_slice` | Canned Pumpkin Pie Filling | - | x | / | x | / | x | later | later | later |
| x | Rose Hip Pie Slice `farmersrespite:rose_hip_pie_slice` | Canned Rose Hip Pie Filling | - | x | / | x | / | x | later | later | later |
| x | Slice of Salmonberry Pie `delightful:salmonberry_pie_slice` | Canned Salmonberry Pie Filling | - | x | / | x | / | x | later | later | later |
| / | Slice of Source Berry Pie `delightful:source_berry_pie_slice` | Canned Source Berry Pie Filling | - | / | / | / | / | / | later | later | later |
| x | Slice of Truffle Pie `create_central_kitchen:truffle_pie_slice` (no source in the pack: no whole pie, nothing drops it) | Canned Truffle Pie Filling | - | / | / | / | / | / | later | later | later |

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

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Caramel Popcorn `corn_delight:caramel_popcorn` | Canned Caramel Popcorn | - | x | / | x | / | none | later | later | later |
| x | Plate of Stuffed Hoglin `nethersdelight:plate_of_stuffed_hoglin_*` | Canned Hoglin Feast | - | x | / | x | / | x | later | later | later |
| / | Plate of Stuffed Hoglin Ham `nethersdelight:plate_of_stuffed_hoglin_ham` (shares Canned Hoglin Feast) | Canned Hoglin Feast | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Plate of Stuffed Hoglin Roast `nethersdelight:plate_of_stuffed_hoglin_roast` (shares Canned Hoglin Feast) | Canned Hoglin Feast | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| / | Plate of Stuffed Hoglin Snout `nethersdelight:plate_of_stuffed_hoglin_snout` (shares Canned Hoglin Feast) | Canned Hoglin Feast | - | n/a | n/a | n/a | n/a | n/a | n/a | n/a | n/a |
| x | Plate of Honey Glazed Ham `farmersdelight:honey_glazed_ham` | Canned Honey Glazed Ham | - | x | / | x | / | x | later | later | later |
| x | Nachos `corn_delight:nachos_bowl` | Canned Nachos | - | x | / | x | / | x | later | later | later |
| x | Bowl of Pineapple Fried Rice `fruitsdelight:bowl_of_pineapple_fried_rice` | Canned Pineapple Fried Rice | - | x | / | x | / | x | later | later | later |
| x | Plate of Roast Chicken `farmersdelight:roast_chicken` | Canned Roast Chicken | - | x | / | x | / | x | later | later | later |
| x | Plate of Shepherd's Pie `farmersdelight:shepherds_pie` | Canned Shepherd's Pie | - | x | / | x | / | x | later | later | later |
| x | Bowl of Stuffed Pumpkin `farmersdelight:stuffed_pumpkin` | Canned Stuffed Pumpkin | - | x | / | x | / | x | later | later | later |
| x | Bowl Of Stuffed Shulker `endersdelight:stuffed_shulker_bowl` | Canned Stuffed Shulker | - | x | - | x | / | x | later | later | later |

### 2.15 Drinks <a id="drink"></a>

Placeholder goal: 4 -> 4 servings. Honey reuses Canned Goods' can (its code clears poison); Canned Powdered Milk takes 4
milk buckets, 1 hunger, always edible, clears every effect.

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Honey Bottle `minecraft:honey_bottle` | Canned Honey | - | x | x | x | / | none | later | later | later |
| x | Milk Bucket `minecraft:milk_bucket` | Canned Powdered Milk | - | x | / | x | / | none | later | later | later |
| / | Adzuki Milkshake `neapolitan:adzuki_milkshake` | Canned Adzuki Milkshake | - | - | - | - | - | - | - | - | - |
| / | Apple Cider `farmersdelight:apple_cider` | Canned Apple Cider | - | - | - | - | - | - | - | - | - |
| / | Apple Cider `farmersrespite:long_apple_cider` | Canned Apple Cider | - | - | - | - | - | - | - | - | - |
| / | Apple Cider `farmersrespite:strong_apple_cider` | Canned Apple Cider | - | - | - | - | - | - | - | - | - |
| / | Azalea Tea `delightful:azalea_tea` | Canned Azalea Tea | - | - | - | - | - | - | - | - | - |
| / | Banana Milkshake `neapolitan:banana_milkshake` | Canned Banana Milkshake | - | - | - | - | - | - | - | - | - |
| / | Bellini Cocktail `fruitsdelight:bellini_cocktail` | Canned Bellini Cocktail | - | - | - | - | - | - | - | - | - |
| / | Berry Matcha Latte `delightful:berry_matcha_latte` | Canned Berry Matcha Latte | - | - | - | - | - | - | - | - | - |
| / | Black Tea `farmersrespite:black_tea` | Canned Black Tea | - | - | - | - | - | - | - | - | - |
| / | Black Tea `farmersrespite:long_black_tea` | Canned Black Tea | - | - | - | - | - | - | - | - | - |
| / | Black Tea `farmersrespite:strong_black_tea` | Canned Black Tea | - | - | - | - | - | - | - | - | - |
| / | Chocolate Coffee `rusticdelight:chocolate_coffee` | Canned Chocolate Coffee | - | - | - | - | - | - | - | - | - |
| / | Chocolate Milkshake `neapolitan:chocolate_milkshake` | Canned Chocolate Milkshake | - | - | - | - | - | - | - | - | - |
| / | Chorus Juice `endersdelight:chorus_juice` | Canned Chorus Juice | - | - | - | - | - | - | - | - | - |
| / | Cloud Latte `quarkdelight:cloud_latte` | Canned Cloud Latte | - | - | - | - | - | none | - | - | - |
| / | Coffee `rusticdelight:coffee` | Canned Coffee | - | - | - | - | - | - | - | - | - |
| / | Coffee `farmersrespite:coffee` | Canned Coffee | - | - | - | - | - | - | - | - | - |
| / | Coffee `farmersrespite:long_coffee` | Canned Coffee | - | - | - | - | - | - | - | - | - |
| / | Coffee `farmersrespite:strong_coffee` | Canned Coffee | - | - | - | - | - | - | - | - | - |
| / | Creamy Corn Drink `corn_delight:creamy_corn_drink` | Canned Creamy Corn Drink | - | - | - | - | - | - | - | - | - |
| / | Dandelion Tea `farmersrespite:dandelion_tea` | Canned Dandelion Tea | - | - | - | - | - | - | - | - | - |
| / | Dandelion Tea `farmersrespite:long_dandelion_tea` | Canned Dandelion Tea | - | - | - | - | - | - | - | - | - |
| / | Dark Coffee `rusticdelight:dark_coffee` | Canned Dark Coffee | - | - | - | - | - | - | - | - | - |
| / | Ender Nectar `delightful:ender_nectar` | Canned Ender Nectar | - | - | - | - | - | - | - | - | - |
| / | Green Tea `farmersrespite:green_tea` | Canned Green Tea | - | - | - | - | - | - | - | - | - |
| / | Green Tea `farmersrespite:long_green_tea` | Canned Green Tea | - | - | - | - | - | - | - | - | - |
| / | Green Tea `farmersrespite:strong_green_tea` | Canned Green Tea | - | - | - | - | - | - | - | - | - |
| / | Hamimelon Juice `fruitsdelight:hamimelon_juice` | Canned Hamimelon Juice | - | - | - | - | - | - | - | - | - |
| / | Hawberry Tea `fruitsdelight:hawberry_tea` | Canned Hawberry Tea | - | - | - | - | - | - | - | - | - |
| / | Honey Coffee `rusticdelight:honey_coffee` | Canned Honey Coffee | - | - | - | - | - | - | - | - | - |
| / | Hot Chocolate `create_confectionery:hot_chocolate_bottle` | Canned Hot Chocolate | - | - | - | - | - | none | - | - | - |
| / | Kiwi Juice `fruitsdelight:kiwi_juice` | Canned Kiwi Juice | - | - | - | - | - | - | - | - | - |
| / | Lavender Tea `delightful:lavender_tea` | Canned Lavender Tea | - | - | - | - | - | - | - | - | - |
| / | Lemon Juice `fruitsdelight:lemon_juice` | Canned Lemon Juice | - | - | - | - | - | - | - | - | - |
| / | Lychee Cherry Tea `fruitsdelight:lychee_cherry_tea` | Canned Lychee Cherry Tea | - | - | - | - | - | - | - | - | - |
| / | Mango Milkshake `fruitsdelight:mango_milkshake` | Canned Mango Milkshake | - | - | - | - | - | - | - | - | - |
| / | Mango Tea `fruitsdelight:mango_tea` | Canned Mango Tea | - | - | - | - | - | - | - | - | - |
| / | Mangosteen Tea `fruitsdelight:mangosteen_tea` | Canned Mangosteen Tea | - | - | - | - | - | - | - | - | - |
| / | Matcha Latte `delightful:matcha_latte` | Canned Matcha Latte | - | - | - | - | - | - | - | - | - |
| / | Milk Coffee `rusticdelight:milk_coffee` | Canned Milk Coffee | - | - | - | - | - | - | - | - | - |
| / | Mint Milkshake `neapolitan:mint_milkshake` | Canned Mint Milkshake | - | - | - | - | - | - | - | - | - |
| / | Oculemonade `canned_goods:oculemonade` | Canned Oculemonade | - | - | - | - | - | - | - | - | - |
| / | Orange Juice `fruitsdelight:orange_juice` | Canned Orange Juice | - | - | - | - | - | - | - | - | - |
| / | Peach Tea `fruitsdelight:peach_tea` | Canned Peach Tea | - | - | - | - | - | - | - | - | - |
| / | Pear Juice `fruitsdelight:pear_juice` | Canned Pear Juice | - | - | - | - | - | - | - | - | - |
| / | Prickly Pear Juice `delightful:prickly_pear_juice` | Canned Prickly Pear Juice | - | - | - | - | - | - | - | - | - |
| / | Purulent Tea `farmersrespite:long_purulent_tea` | Canned Purulent Tea | - | - | - | - | - | - | - | - | - |
| / | Purulent Tea `farmersrespite:purulent_tea` | Canned Purulent Tea | - | - | - | - | - | - | - | - | - |
| / | Purulent Tea `farmersrespite:strong_purulent_tea` | Canned Purulent Tea | - | - | - | - | - | - | - | - | - |
| / | Reef-Cola `canned_goods:reef_cola` | Canned Reef-Cola | - | - | - | - | - | - | - | - | - |
| / | Rose Hip Tea `farmersrespite:long_rose_hip_tea` | Canned Rose Hip Tea | - | - | - | - | - | - | - | - | - |
| / | Rose Hip Tea `farmersrespite:rose_hip_tea` | Canned Rose Hip Tea | - | - | - | - | - | - | - | - | - |
| / | Rose Hip Tea `farmersrespite:strong_rose_hip_tea` | Canned Rose Hip Tea | - | - | - | - | - | - | - | - | - |
| / | Soda `canned_goods:soda` | Canned Soda | - | - | - | - | - | - | - | - | - |
| / | Soothing Hot Chocolate `create_confectionery:soothing_hot_chocolate` | Canned Soothing Hot Chocolate | - | - | - | - | - | none | - | - | - |
| / | Strawberry Banana Smoothie `neapolitan:strawberry_banana_smoothie` | Canned Strawberry Banana Smoothie | - | - | - | - | - | - | - | - | - |
| / | Strawberry Milkshake `neapolitan:strawberry_milkshake` | Canned Strawberry Milkshake | - | - | - | - | - | none | - | - | - |
| / | Vanilla Milkshake `neapolitan:vanilla_milkshake` | Canned Vanilla Milkshake | - | - | - | - | - | - | - | - | - |
| / | Yellow Tea `farmersrespite:long_yellow_tea` | Canned Yellow Tea | - | - | - | - | - | - | - | - | - |
| / | Yellow Tea `farmersrespite:strong_yellow_tea` | Canned Yellow Tea | - | - | - | - | - | - | - | - | - |
| / | Yellow Tea `farmersrespite:yellow_tea` | Canned Yellow Tea | - | - | - | - | - | - | - | - | - |

### 2.16 Drinks: alcohol <a id="alcohol"></a>

Not in the canning.js plan; listed so the family is complete.

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| / | A Bottle of 'Creepers Crush' `vinery:creepers_crush` | Canned A Bottle of 'Creepers Crush' | - | - | - | - | - | - | - | - | - |
| / | A Bottle of 'Mojang Noir' `vinery:bottle_mojang_noir` | Canned A Bottle of 'Mojang Noir' | - | - | - | - | - | - | - | - | - |
| / | A Bottle of 'Villagers Fright' `vinery:villagers_fright` | Canned A Bottle of 'Villagers Fright' | - | - | - | - | - | - | - | - | - |
| / | Aegis Wine `vinery:aegis_wine` | Canned Aegis Wine | - | - | - | - | - | - | - | - | - |
| / | Apple Wine `vinery:apple_wine` | Canned Apple Wine | - | - | - | - | - | - | - | - | - |
| / | Bloody Mary `brewinandchewin:bloody_mary` | Canned Bloody Mary | - | - | - | - | - | - | - | - | - |
| / | Bolvar Wine `vinery:bolvar_wine` | Canned Bolvar Wine | - | - | - | - | - | - | - | - | - |
| / | Bottle of Wodka `tmted:bottle_of_wodka` | Canned Wodka | - | - | - | - | - | none | - | - | - |
| / | Chenet Wine `vinery:chenet_wine` | Canned Chenet Wine | - | - | - | - | - | - | - | - | - |
| / | Cherry Wine `vinery:cherry_wine` | Canned Cherry Wine | - | - | - | - | - | - | - | - | - |
| / | Chorus Wine `vinery:chorus_wine` | Canned Chorus Wine | - | - | - | - | - | - | - | - | - |
| / | Cider `vinery:apple_cider` | Canned Cider | - | - | - | - | - | - | - | - | - |
| / | Clark Wine `vinery:clark_wine` | Canned Clark Wine | - | - | - | - | - | - | - | - | - |
| / | Cristel Wine `vinery:cristel_wine` | Canned Cristel Wine | - | - | - | - | - | - | - | - | - |
| / | Egg Grog `brewinandchewin:egg_grog` | Canned Egg Grog | - | - | - | - | - | - | - | - | - |
| / | Eiswein `vinery:eiswein` | Canned Eiswein | - | - | - | - | - | - | - | - | - |
| / | Glittering Grenadine `brewinandchewin:glittering_grenadine` | Canned Glittering Grenadine | - | - | - | - | - | - | - | - | - |
| / | Jellie Wine `vinery:jellie_wine` | Canned Jellie Wine | - | - | - | - | - | - | - | - | - |
| / | Jo's Special Mixture `vinery:jo_special_mixture` | Canned Jo's Special Mixture | - | - | - | - | - | - | - | - | - |
| / | Kelp Cider `vinery:kelp_cider` | Canned Kelp Cider | - | - | - | - | - | - | - | - | - |
| / | Kombucha `brewinandchewin:kombucha` | Canned Kombucha | - | - | - | - | - | - | - | - | - |
| / | Magnetic Wine `vinery:magnetic_wine` | Canned Magnetic Wine | - | - | - | - | - | - | - | - | - |
| / | Mead `brewinandchewin:mead` | Canned Mead | - | - | - | - | - | - | - | - | - |
| / | Mead `vinery:mead` | Canned Mead | - | - | - | - | - | - | - | - | - |
| / | Mellohi Wine `vinery:mellohi_wine` | Canned Mellohi Wine | - | - | - | - | - | - | - | - | - |
| / | Miss Lilitus Wine `vinery:king_danis_wine` | Canned Miss Lilitus Wine | - | - | - | - | - | - | - | - | - |
| / | Noir Wine `vinery:noir_wine` | Canned Noir Wine | - | - | - | - | - | - | - | - | - |
| / | Pale Jane `brewinandchewin:pale_jane` | Canned Pale Jane | - | - | - | - | - | - | - | - | - |
| / | Praetorian Wine `vinery:praetorian_wine` | Canned Praetorian Wine | - | - | - | - | - | - | - | - | - |
| / | Red Rum `brewinandchewin:red_rum` | Canned Red Rum | - | - | - | - | - | - | - | - | - |
| / | Red Wine `vinery:red_wine` | Canned Red Wine | - | - | - | - | - | - | - | - | - |
| / | Rice Wine `brewinandchewin:rice_wine` | Canned Rice Wine | - | - | - | - | - | - | - | - | - |
| / | Saccharine Rum `brewinandchewin:saccharine_rum` | Canned Saccharine Rum | - | - | - | - | - | - | - | - | - |
| / | Salty Folly `brewinandchewin:salty_folly` | Canned Salty Folly | - | - | - | - | - | - | - | - | - |
| / | Solaris Wine `vinery:solaris_wine` | Canned Solaris Wine | - | - | - | - | - | - | - | - | - |
| / | Stal Wine `vinery:stal_wine` | Canned Stal Wine | - | - | - | - | - | - | - | - | - |
| / | Steel Toe Stout `brewinandchewin:steel_toe_stout` | Canned Steel Toe Stout | - | - | - | - | - | - | - | - | - |
| / | Strad Wine `vinery:strad_wine` | Canned Strad Wine | - | - | - | - | - | - | - | - | - |
| / | Strongroot Ale `brewinandchewin:strongroot_ale` | Canned Strongroot Ale | - | - | - | - | - | - | - | - | - |
| / | Sunkissed Wine `vinery:glowing_wine` | Canned Sunkissed Wine | - | - | - | - | - | - | - | - | - |
| / | Withering Dross `brewinandchewin:withering_dross` | Canned Withering Dross | - | - | - | - | - | - | - | - | - |

### 2.17 Golden and enchanted golden <a id="golden"></a>

Golden x0.25 meals, 10 servings, effects 0.25x, 2 golden + honey bottle.

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Enchanted Golden Apple `minecraft:enchanted_golden_apple` | Canned Enchanted Golden Apple | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Baked Potato `goldenfood:enchanted_golden_baked_potato` | Canned Enchanted Golden Baked Potato | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Beetroot `goldenfood:enchanted_golden_beetroot` | Canned Enchanted Golden Beetroot | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Beetroot Soup `goldenfood:enchanted_golden_beetroot_soup` | Canned Enchanted Golden Beetroot Soup | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Bread `goldenfood:enchanted_golden_bread` | Canned Enchanted Golden Bread | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Carrot `goldenfood:enchanted_golden_carrot` | Canned Enchanted Golden Carrot | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Chorus Fruit `goldenfood:enchanted_golden_chorus_fruit` | Canned Enchanted Golden Chorus Fruit | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Beef `goldenfood:enchanted_golden_cooked_beef` | Canned Enchanted Golden Cooked Beef | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Chicken `goldenfood:enchanted_golden_cooked_chicken` | Canned Enchanted Golden Cooked Chicken | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Cod `goldenfood:enchanted_golden_cooked_cod` | Canned Enchanted Golden Cooked Cod | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Mutton `goldenfood:enchanted_golden_cooked_mutton` | Canned Enchanted Golden Cooked Mutton | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Porkchop `goldenfood:enchanted_golden_cooked_porkchop` | Canned Enchanted Golden Cooked Porkchop | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Rabbit `goldenfood:enchanted_golden_cooked_rabbit` | Canned Enchanted Golden Cooked Rabbit | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cooked Salmon `goldenfood:enchanted_golden_cooked_salmon` | Canned Enchanted Golden Cooked Salmon | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Cookie `goldenfood:enchanted_golden_cookie` | Canned Enchanted Golden Cookie | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Dried Kelp `goldenfood:enchanted_golden_dried_kelp` | Canned Enchanted Golden Dried Kelp | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Glow Berries `goldenfood:enchanted_golden_glow_berries` | Canned Enchanted Golden Glow Berries | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Honey Bottle `goldenfood:enchanted_golden_honey_bottle` | Canned Enchanted Golden Honey Bottle | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Melon Slice `goldenfood:enchanted_golden_melon_slice` | Canned Enchanted Golden Melon Slice | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Milk Bucket `goldenfood:enchanted_golden_milk_bucket` | Canned Enchanted Golden Milk Bucket | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Moss Clump `immersive_weathering:enchanted_golden_moss_clump` | Canned Enchanted Golden Moss Clump | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Mushroom Stew `goldenfood:enchanted_golden_mushroom_stew` | Canned Enchanted Golden Mushroom Stew | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Pumpkin Pie `goldenfood:enchanted_golden_pumpkin_pie` | Canned Enchanted Golden Pumpkin Pie | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Rabbit Stew `goldenfood:enchanted_golden_rabbit_stew` | Canned Enchanted Golden Rabbit Stew | - | x | / | x | / | x | later | later | later |
| x | Enchanted Golden Sweet Berries `goldenfood:enchanted_golden_sweet_berries` | Canned Enchanted Golden Sweet Berries | - | x | / | x | / | x | later | later | later |
| x | Golden Apple `minecraft:golden_apple` | Canned Golden Apple | - | x | / | x | / | x | later | later | later |
| x | Golden Baked Potato `goldenfood:golden_baked_potato` | Canned Golden Baked Potato | - | x | / | x | / | x | later | later | later |
| x | Golden Beetroot `goldenfood:golden_beetroot` | Canned Golden Beetroot | - | x | / | x | / | x | later | later | later |
| x | Golden Beetroot Soup `goldenfood:golden_beetroot_soup` | Canned Golden Beetroot Soup | - | x | / | x | / | x | later | later | later |
| x | Golden Bread `goldenfood:golden_bread` | Canned Golden Bread | - | x | / | x | / | x | later | later | later |
| x | Golden Carrot `minecraft:golden_carrot` | Canned Golden Carrot | - | x | / | x | / | x | later | later | later |
| x | Golden Chorus Fruit `goldenfood:golden_chorus_fruit` | Canned Golden Chorus Fruit | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Beef `goldenfood:golden_cooked_beef` | Canned Golden Cooked Beef | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Chicken `goldenfood:golden_cooked_chicken` | Canned Golden Cooked Chicken | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Cod `goldenfood:golden_cooked_cod` | Canned Golden Cooked Cod | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Mutton `goldenfood:golden_cooked_mutton` | Canned Golden Cooked Mutton | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Porkchop `goldenfood:golden_cooked_porkchop` | Canned Golden Cooked Porkchop | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Rabbit `goldenfood:golden_cooked_rabbit` | Canned Golden Cooked Rabbit | - | x | / | x | / | x | later | later | later |
| x | Golden Cooked Salmon `goldenfood:golden_cooked_salmon` | Canned Golden Cooked Salmon | - | x | / | x | / | x | later | later | later |
| x | Golden Cookie `goldenfood:golden_cookie` | Canned Golden Cookie | - | x | / | x | / | x | later | later | later |
| x | Golden Dried Kelp `goldenfood:golden_dried_kelp` | Canned Golden Dried Kelp | - | x | / | x | / | x | later | later | later |
| x | Golden Glow Berries `goldenfood:golden_glow_berries` | Canned Golden Glow Berries | - | x | / | x | / | x | later | later | later |
| x | Golden Honey Bottle `goldenfood:golden_honey_bottle` | Canned Golden Honey Bottle | - | x | / | x | / | x | later | later | later |
| x | Golden Melon Slice `goldenfood:golden_melon_slice` | Canned Golden Melon Slice | - | x | / | x | / | x | later | later | later |
| x | Golden Milk Bucket `goldenfood:golden_milk_bucket` | Canned Golden Milk Bucket | - | x | / | x | / | x | later | later | later |
| x | Golden Moss Clump `immersive_weathering:golden_moss_clump` | Canned Golden Moss Clump | - | x | / | x | / | x | later | later | later |
| x | Golden Mushroom Stew `goldenfood:golden_mushroom_stew` | Canned Golden Mushroom Stew | - | x | / | x | / | x | later | later | later |
| x | Golden Pumpkin Pie `goldenfood:golden_pumpkin_pie` | Canned Golden Pumpkin Pie | - | x | / | x | / | x | later | later | later |
| x | Golden Rabbit Stew `goldenfood:golden_rabbit_stew` | Canned Golden Rabbit Stew | - | x | / | x | / | x | later | later | later |
| x | Golden Sweet Berries `goldenfood:golden_sweet_berries` | Canned Golden Sweet Berries | - | x | / | x | / | x | later | later | later |

### 2.18 Special <a id="special"></a>

Fruit Meals like raw fruit (section special). Chorus reuses Canned Goods' can (its code teleports).

| Want | Item | Can | Alt input (sliced) | Impl | Texture | Recipe | Food audit | Effects | Food h/s | Craft sum h/s | Canned h/s |
|---|---|---|---|---|---|---|---|---|---|---|---|
| x | Ancient Fruit `quark:ancient_fruit` | Canned Ancient Fruit | - | x | / | x | / | none | later | later | later |
| x | Chorus Fruit `minecraft:chorus_fruit` | Canned Chorus Fruit | - | x | x | x | / | none | later | later | later |
| / | Azalea Flowers `immersive_weathering:azalea_flowers` | Canned Azalea Flowers | - | - | - | - | - | none | - | - | - |
| / | Biscuit of Totality `botania:mana_cookie` | Canned Biscuit of Totality | - | - | - | - | - | - | - | - | - |
| / | Cosmic Cod `alexsmobs:cosmic_cod` | Canned Cosmic Cod | - | - | - | - | - | - | - | - | - |
| / | Dog Food `farmersdelight:dog_food` | Canned Dog Food | - | - | - | - | - | none | - | - | - |
| / | Fish Oil `alexsmobs:fish_oil` | Canned Fish Oil | - | - | - | - | - | none | - | - | - |
| / | Ice Cubes `neapolitan:ice_cubes` | Canned Ice Cubes | - | - | - | - | - | none | - | - | - |
| / | Ice Sickle `immersive_weathering:ice_sickle` | Canned Ice Sickle | - | - | - | - | - | - | - | - | - |
| / | Icicle `immersive_weathering:icicle` | Canned Icicle | - | - | - | - | - | - | - | - | - |
| / | Magma Gelatin `nethersdelight:magma_gelatin` | Canned Magma Gelatin | - | - | - | - | - | - | - | - | - |
| / | Mana in a Bottle `botania:mana_bottle` | Canned Mana in a Bottle | - | - | - | - | - | none | - | - | - |
| / | Moss Clump `immersive_weathering:moss_clump` | Canned Moss Clump | - | - | - | - | - | none | - | - | - |
| / | Propelpearl `nethersdelight:propelpearl` | Canned Propelpearl | - | - | - | - | - | none | - | - | - |
| / | Rainbow Jelly `alexsmobs:rainbow_jelly` | Canned Rainbow Jelly | - | - | - | - | - | none | - | - | - |


## 3. Outside the families

Food types / canning family covers. Pull an item in by adding a row to the family it fits.

| Type | Items | Examples |
|---|---:|---|
| egg | 4 | Boiled Emu Egg, Fried Egg, Fried Egg, Scrambled Eggs |
| dairy | 2 | Flaxen Cheese Wedge, Scarlet Cheese Wedge |
| sweet | 155 | Slice of Chocolate Cake, Slice of Adzuki Cake, Adzuki Ice Cream, Slice of Aloe Cake, Apple Jello, Slice of Banana Cake, Banana Ice Cream, Bar of Dark Chocolate ... |
| handheld | 52 | Bacon Sandwich, Barbecue on a Stick, Bison Burger, Bunfungus Sandwich, Cabbage Rolls, Calamari Roll, Cheeseburger, Chicken Sandwich ... |
| ingredient | 42 | Animal Fat, Apple Jam, Apple Mash, Batter, Bayberry Jam, Blueberry Jam, Chopped Clover, Chorus Jam ... |
| hazard | 11 | Maggot, Copper Nugget, Poisonous Potato, Pond Water, Pufferfish, Rotten Apple, Rotten Cherry, Rotten Flesh ... |

### 3.1 New sections and placeholder values <a id="new-sections"></a>

Generated by [gen_can_types.py](../tools/gen_can_types.py) (SECTIONS). "Meal" sections size each source's recipe by
value: the basic recipe is worth about 0.8 of the can (at most 8 items), the additive one 0.7 (fruit) or 0.8 (meat,
fish) of that. An alt input that would need more than 8 takes 8 and makes a partly full can (`CanServings` NBT).

| Section | Meal | Servings | Recipe | Additive | Status |
|---|---|---:|---|---|---|
| raw_fruit, special | Fruit Meal (5, 3) | 3 | sized per source | sugar or honey | same rule as the hand-written fruit cans |
| raw_meat, raw_fish | Meat Meal (5, 3) | 4 | sized per source | salt or bone broth | same rule as the hand-written meat cans |
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

## 4. Open decisions

- **Pumpkin: veggie or fruit.** The recipes sit in canning.js's raw veggie section (4 slices, 2 servings via
  can_servings.json) but take sugar or honey as the additive (recipe id `canned_pumpkin_salt`), and cans.js gives it
  Fruit Meal values. One side should move. Slices only either way: the whole pumpkin is a block, not a food.
- **White strawberries** (5 hunger, 0.05) now have their own can at the old 3 / 2 + sugar ratio. Recheck the ratio
  when the h/s columns are filled.
- **Raw meat slices.** Done 2026-10-13: minced beef and raw bacon fill a can (8, or 6 + salt); chicken cuts, mutton chops
  and every raw fish slice make a half full can from 8 (the fair count would not fit the grid). Check the half cans
  feel right in game (CP-09).
- **Bananas.** Done 2026-10-13: Alex's Mobs bananas no longer can; the can takes Neapolitan bananas only.
- **Placeholder goals.** Meal, pie, feast, drink and hazard values ([3.1](#new-sections)) were picked to get the cans in;
  set the real goals in canning.js and SECTIONS, then rerun the generator.
- **Partial can as pie input.** Filling + pie crust -> pie accepts an opened filling can too (recipe ingredients
  ignore NBT), so a can with 1 serving left still makes a whole pie.
- **Shulker Filet** is valued the same as the mollusk in the food catalog (1, 0.2), so 8 filets fill a can like 8
  mollusks. If a mollusk cuts into 2 filets, the filet recipe is a cheaper path.
- **Removed cans already shipped.** The removed cans and `canned_cactus` were in 1.0.4. Anyone holding one loses it
  when 1.0.5+ loads, unless the old ids get a remap (missing-mappings, as Incubation Additions does for its nests).

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
- [ ] **CP-09 alt inputs:** 8 Minced Beef -> a full Canned Raw Beef; 8 Raw Chicken Cuts -> a Canned Raw Chicken with the
  bar at 2/4; 6 Lemon Slices -> Canned Lemon; 8 Cod Slices -> Canned Raw Cod at 2/4. The half can stacks only with other
  half cans.
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
- 2026-10-14: the 2026-10-13 can changes (removals, White Strawberries can, Cactus Flesh rename, generated rows, new
  sprites) ship in pack **1.0.6** (STAGED 2026-10-14, [plan_1.0.6.md](plan_1.0.6.md)); still not run in game (CP-01 to CP-05).
