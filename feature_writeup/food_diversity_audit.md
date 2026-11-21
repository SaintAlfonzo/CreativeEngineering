# Food diversity audit (2026-11-20, values updated 2026-11-21)

This audit measured each food's Spice of Life diversity against its hunger and saturation, and the processes that add
too much or too little. The owner's rulings on it (chat 2026-11-21) are built as Raspberry Pie's diversity table:
[plan_food_diversity_1121.md](plan_food_diversity_1121.md) (rulings, design, open items). **The values in this file are
the table's (generator `tools/food_diversity.py`, 2026-11-21) unless marked "was"**; "was" is the 2026-11-19 state
(Apple Pie's formula plus its config list). Source: the `/dumpfood` export of 2026-11-19 18:28
(`kubejs/exported/food/foods.json`, 1697 foods; `recipes.json`). The 2026-11-19 per-food flags stay in
[food_diversity_audit.csv](food_diversity_audit.csv) (columns `flag, id, name, type, nutrition, saturationModifier,
diversity, formula, note`) as the record of what was found.

Removed items (`canned_goods:canned_food`, `canned_goods:hot_hot_hotpot`) are left out.

## 1. How diversity is valued

**Was:** no pack layer. Every food outside Apple Pie's 22-entry config list followed `q = hunger x (1 + modifier) /
2`, complexity `q / 5` below q 5, else `1 + 4 x log10(q - 4)`, with a 9x jump in slope at q 5.

**Now:** Raspberry Pie's table sets all 1658 foods (`/solraspberrypie/diversity.json`; Apple Pie's config list is
empty). Each food takes the first rule that fits:

1. Drinks (`#solraspberrypie:drinks`) and no-diet foods (`#solraspberrypie:no_diet`, the chocolate coins): 0, outside
   the diet.
2. Cans: 80% of the food inside.
3. Cooked meat and fish heated from a raw source: the vanilla meats' fit, 0.569 x q.
4. Cooking Pot dishes: the pack curve, never below the best food input + 0.1.
5. Baking: the best ingredient + the pot's median boost (2.97), scaled by the batch's input value, capped one tier above
   the food's own.
6. Create filling, and shaped, shapeless or assembled crafts that are no raw food's cooked version: the pot rule,
   1.69 x the inputs' value per output item (crafts only when that is higher than the curve).
7. Everything else: the pack curve, `q` 0-2 -> 0-1, 2-5 -> 1-3, 5-12 -> 3-6, 12-30 -> 6-9 (no knee).

Then the tiers: dragon, gilded, feast and golden get multipliers that lift each tier's third-best food to its top
(golden and gilded on their source food). Golden is floored at the average high-tier meal (5.25) and gilded at the
average feast (6.78). Low, mid and high are capped at their tops. Coated food and candies are capped at the mid top
(3), never below their best food input. Families share one diet entry and their best member's value.

**Tiers** (food tier by `q`, Nourishment counts as high): dragon (id contains `dragon`), gilded (enchanted golden),
feasts (`meal/feast` or the feast family), golden, high (q 5 or more, or Nourishment), mid (q 2 to 5), low (q under 2).

| Tier | Top | Foods (cans counted in their source's tier, a family once) |
|---|---:|---:|
| Dragon | 11 | 15 |
| Gilded | 10 | 45 |
| Feasts | 8 | 37 |
| Golden | 7 (floor 5.25) | 53 |
| High | 6 | 483 |
| Mid | 3 | 413 |
| Low | 1 | 375 |

**Medians by type** (diversity), was -> now: raw 0.26 -> 0.65, sweet 0.54 -> 2.23, cooked 0.72 -> 2.07, canned 0.90 ->
2.40, baked -> 2.82, salad 2.52 -> 3.60, handheld 3.03 -> 5.12, golden 3.41 -> 6.78, soup 3.61 -> 4.39, meal 4.33 -> 5.49.

### 1.1 Ceiling by rotation size, memory 32

Live config `endDecay = 32`: a food eaten k meals ago is worth `diversity x (1 - k/32)`, so a loop of N distinct foods
averages `mean diversity x (N - N(N-1)/64)`. Each cell takes the N best entries (a family counts once, drinks and
no-diet foods are out). The peak (best foods newest) is at most 5 above the average.

**Cumulative** (each column removes one more group, in the owner's order dragon, gilded, feasts, golden). Each step
lowers the ceiling at every rotation size. Foods left: 1423, 1408, 1363, 1326, 1271.

| Rotation | None | No dragon | No dragon, gilded | No D, G, feasts | No D, G, F, golden |
|---:|---:|---:|---:|---:|---:|
| 1 | 11.0 | 10.0 | 8.0 | 7.0 | 6.0 |
| 2 | 21.7 | 19.7 | 15.8 | 13.8 | 11.8 |
| 3 | 32.0 | 29.1 | 23.2 | 20.3 | 17.4 |
| 4 | 41.0 | 36.5 | 30.4 | 26.6 | 22.9 |
| 5 | 49.7 | 43.8 | 37.4 | 31.8 | 28.1 |
| 6 | 58.1 | 50.4 | 44.0 | 36.8 | 33.2 |
| 8 | 72.6 | 62.9 | 56.7 | 46.2 | 42.8 |
| 10 | 84.4 | 74.5 | 68.2 | 54.9 | 51.6 |
| 12 | 94.6 | 85.0 | 77.7 | 62.8 | 59.6 |
| 14 | 103.8 | 94.4 | 86.1 | 70.0 | 66.9 |
| 16 | 112.0 | 102.8 | 93.4 | 76.5 | 73.5 |
| 18 | 119.1 | 110.2 | 99.3 | 82.2 | 79.3 |
| 20 | 125.2 | 116.2 | 104.1 | 87.1 | 84.4 |
| 22 | 130.3 | 120.7 | 108.0 | 91.3 | 88.7 |
| 24 | 134.3 | 124.2 | 111.1 | 94.7 | 92.2 |
| 26 | 137.2 | 126.7 | 113.3 | 97.4 | 95.1 |
| 28 | 138.6 | 128.3 | 114.6 | 99.4 | 97.1 |
| 30 | 139.0 | 128.8 | 115.0 | 100.6 | 98.4 |
| 32 | 138.3 | 128.5 | 114.6 | 101.0 | 99.0 |

**One tier only**, in the owner's order. At one food each column is its tier top. Small tiers run out of foods:
dragon (15) stops rising at 14 and falls under gilded from 12 foods; golden (53) falls under high from 12.

| Rotation | Dragon | Gilded | Feasts | Golden | High | Mid | Low |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 1 | 11.0 | 10.0 | 8.0 | 7.0 | 6.0 | 3.0 | 1.0 |
| 2 | 21.7 | 19.7 | 15.8 | 13.8 | 11.8 | 5.9 | 2.0 |
| 3 | 32.0 | 29.1 | 23.2 | 20.3 | 17.4 | 8.7 | 2.9 |
| 4 | 40.6 | 36.5 | 30.4 | 26.6 | 22.9 | 11.4 | 3.8 |
| 5 | 48.3 | 43.8 | 37.4 | 31.4 | 28.1 | 14.1 | 4.7 |
| 6 | 54.9 | 50.4 | 44.0 | 35.9 | 33.2 | 16.6 | 5.5 |
| 8 | 66.3 | 62.9 | 56.7 | 44.3 | 42.8 | 21.4 | 7.1 |
| 10 | 75.7 | 73.8 | 68.2 | 51.8 | 51.6 | 25.8 | 8.6 |
| 12 | 81.0 | 82.8 | 77.7 | 58.6 | 59.6 | 29.8 | 9.9 |
| 14 | 83.0 | 90.5 | 85.6 | 64.8 | 66.9 | 33.5 | 11.2 |
| 16 | 82.4 | 97.3 | 92.0 | 70.3 | 73.5 | 36.8 | 12.2 |
| 18 | 82.4 | 103.3 | 97.5 | 75.1 | 79.3 | 39.7 | 13.2 |
| 20 | 82.4 | 108.4 | 102.3 | 79.3 | 84.4 | 42.2 | 14.1 |
| 22 | 82.4 | 112.7 | 106.2 | 82.8 | 88.7 | 44.3 | 14.8 |
| 24 | 82.4 | 116.2 | 109.1 | 85.7 | 92.2 | 46.1 | 15.4 |
| 26 | 82.4 | 118.7 | 110.8 | 87.9 | 95.1 | 47.5 | 15.8 |
| 28 | 82.4 | 120.0 | 111.3 | 89.5 | 97.1 | 48.6 | 16.2 |
| 30 | 82.4 | 119.9 | 110.6 | 89.9 | 98.4 | 49.2 | 16.4 |
| 32 | 82.4 | 118.7 | 109.1 | 89.1 | 99.0 | 49.5 | 16.5 |

Was (2026-11-19, window 32): None 91.9, golden excluded 85.1, high only 84.9, mid only 15.8, low only 17.6. The
enchanted golden apple (10) was the top food, and six cake and pie slices at a config value of 4 carried the low tier.

**Top 8 per tier** (the same at every rotation of 8 or more):

| Tier | Top 8 |
|---|---|
| Dragon | roasted_dragon_steak 11.00, fried_dragon_egg 11.00, dragon_breath_and_chorus_soup 11.00, liquid_dragon_egg 9.61, steamed_dragon_egg 8.92, dragon_leg_with_sauce 8.07, dragon_meat_stew 7.69, canned_steamed_dragon_egg 7.13 |
| Gilded | enchanted_golden_honey_bottle 10.00, enchanted_golden_cooked_porkchop 10.00, enchanted_golden_cooked_beef 10.00, enchanted_golden_cooked_salmon 8.33, enchanted_golden_cooked_mutton 8.33, canned_enchanted_golden_honey_bottle 8.00, canned_enchanted_golden_cooked_porkchop 8.00, canned_enchanted_golden_cooked_beef 8.00 |
| Feasts | bowl_of_guardian_soup 8.00, plate_of_stuffed_hoglin_snout 8.00, bowl_of_pineapple_fried_rice 8.00, roasted_mutton_rack 7.92, stuffed_pumpkin 7.92, shepherds_pie 7.92, roast_chicken 7.92, honey_glazed_ham 7.92 |
| Golden | golden_roll_slice 7.00, golden_roll 7.00, golden_nutritional_bar 7.00, golden_apple 6.87, canned_golden_nutritional_bar 5.60, canned_golden_apple 5.50, canned_golden_apples 5.50, golden_cooked_porkchop 5.29 |
| High | stuffed_burrito 6.00, pickle_soup 6.00, pb_j 6.00, overnight_oats 6.00, deluxe_burger 6.00, cucumber_salad 6.00, cheese_burger 6.00, vegetarian_burger 6.00 |
| Mid | oatmeal_cookie 3.00, coffee_cookie 3.00, vanilla_ice_cream 3.00, vanilla_chocolate_fingers 3.00, strawberry_ice_cream 3.00, strawberry_bean_bonbons 3.00, mint_ice_cream 3.00, mint_chocolate 3.00 |
| Low | strawberry_jello 1.00, salmonberry_jello 1.00, red_grape_jello 1.00, raspberry_jello 1.00, pepper_jello 1.00, mulberry_jello 1.00, green_grape_jello 1.00, cherry_jello 1.00 |

## 2. Food values (2026-11-19 findings, then now)

### 2.1 Apple Pie's config overrides

Was: 16 out of line (slices at 4, steak and porkchop lowered to 2, golden carrot raised to 2, golden apple 4,
enchanted golden apple 10, Large Meals 5 to 6, blazing chili 5, coffee 3 as a zero-hunger non-drink).
**Now:** the list is empty. Cake slice 0.55; steak and porkchop 4.10; golden carrot 5.25 (golden floor); golden apple
6.87; enchanted golden apple 6.78 (gilded floor; its source is an apple); Hearty Lunch 5.49; mushroom pot pie 7.19
(feast); blazing chili 3.86; Farmer's Respite coffee is a drink.

### 2.2 Drinks that fill (honey jar, lattes, bubble tea, garlic honey)

Unchanged: still drinks at 0. Open: whether honey jar and fermented garlic honey are drinks at all.

### 2.3 High saturation modifier, low hunger

Was: enchanted golden milk bucket and pumpkin pie 7.02, their cans 3.80, ancient fish eggs 5.00. **Now:** gilded
values follow the source food (milk bucket and pumpkin pie 7.05, cans 5.64). Ancient fish eggs are capped at the high
top (6): still the highest raw food. Open.

### 2.4 The curve's knee at q 5

**Fixed:** the pack curve has no knee (slope 0.5 per q below q 2, 0.67 up to q 5, 0.43 up to q 12). Cooked chicken 2.73 against maple
glazed bacon 3.46.

### 2.5 Type inversions

- Raw above 1.5: dragon leg and raw dragon meat 3.60 (curve, below the cooked meat's 6.84); the bell pepper rolls 3.56
  (still typed raw, one entry per color group now).
- Dishes below 1: the Ender's Delight 4-hunger stews are now 1.67 (curve 4 hunger, mid tier); caramel popcorn 3.06
  (feast).

### 2.6 Untyped foods (107)

Unchanged; they take the pack curve. They still need a `food_catalog.py` pass.

## 3. Processes

Median change of the output against its best food input (the median member for a tag), now:

| Process | Recipes | Median change | Outputs below the best input (< -0.5) | Big gains (>= +2) |
|---|---:|---:|---:|---:|
| Cooking Pot | 275 | +3.07 | 3 | 191 |
| Shaped crafting | 74 | +2.98 | 1 | 51 |
| Baking (Creative Delight) | 39 | +2.73 | 1 | 31 |
| Create filling | 95 | +1.20 | 0 | 35 |
| Smoking | 109 | +1.35 | 2 | 36 |
| Campfire | 106 | +1.34 | 0 | 34 |
| Smelting | 102 | +1.34 | 1 | 33 |
| Shapeless crafting (includes canning, slicing) | 839 | -0.28 | 312 | 146 |
| Create mixing | 597 | +0.00 | 5 | 211 |
| Cutting | 95 | -0.67 | 51 | 0 |
| Vintage Delight fermenting (pickles) | 16 | +1.05 | 1 | 2 |

Was: Cooking Pot +2.36, shaped +1.52, filling +1.40, heat +0.46 to +0.48, shapeless +0.44, baking +0.20, mixing 0,
cutting -0.31, brewing and fermenting -0.22 to -0.26. Brewing, wine and keg recipes are missing from the table now:
their outputs are all drinks (0, Liquid Diet Log only). Heat steps went up because the meat fit raises vanilla meats
(steak 2 -> 4.10); shapeless went down because cans are 80% of their source.

### 3.1 One cooking step on an exotic meat

Was: cooked bison 4.11, smoked dragon leg 4.61 against steak 2. **Now:** every cooked meat and fish follows 0.569 x q,
the vanilla fit (beef and porkchop 4.10, salmon 3.07, chicken 2.73). Cooked dragon meat (roasted, smoked leg) 6.84,
between the high top and the lowest dragon dish (7.69).

### 3.2 Create filling

Was: chocolate apples 3.02 to 3.41, waffles 3.02 to 3.91, glazed berries 2.45, candies 2.79. **Now:** coated food and
candies are capped at the mid top, 3 (never below their food input: glazed porkchop 4.20 over cooked porkchop 4.10).
Each coating also has a Cooking Pot recipe (2 bases + 3 coating items -> 2).

### 3.3 Golden Food

Was: golden steak 5.07, gilded 5.3 to 10. **Now:** golden = source x 1.29, at least 5.25, at most 7; gilded = source x
2.71, at least 6.78, at most 10.

### 3.4 Canning

Was: median +0.3, 51 cans gaining 1 or more, golden cans losing up to 9.5. **Now:** every can is 80% of its source
(canned beef 3.28, canned golden cooked beef 4.24, canned enchanted golden apple 5.42). A can is still its own diet
entry.

### 3.5 Dishes worth less than an ingredient

Was: guardian sandwich 0.96 from a 2.79 fillet, bell pepper soup 0.96, 202 flat multi-input recipes. **Now:** a Cooking
Pot dish never scores below its best food input + 0.1, and non-food inputs count in the pot rule (ice, mushrooms,
blaze powder 0.25; bone meal, sugar 0.15; others 0.1; containers and gold 0). Bell pepper soup 2.87, hamimelon
shaved ice 1.00 (low cap). Pot recipes below the best input: 3 (was 10).

### 3.6 Color and number variants

**Fixed by families** (25): bell peppers per form and color group (standard red, yellow, green; pale pink, white,
orange; dark black, purple, blue), so 18 pepper entries instead of 54, and each form counts on its own; chocolate
coatings per base food (apples, waffles, rolls, glazed berries, glazed marshmallows); chocolate-filled candies (12)
and caramel candies (4). Honey and caramel coatings stay separate foods. The chocolate coins are out of the diet.

## 4. Levers: done and open

- Done: the type and process layer (lever 1, as Raspberry Pie's table), Apple Pie's config list cleared (2), cans at
  80% of the source (3, as a separate entry), variant families (4), golden on the source value (7).
- Open: Central Kitchen `incomplete_*` intermediates are still edible foods (5); drinks that fill (6); the Diet Log
  ladder (rungs 2 to 100) against a 138 ceiling (plan O1).

## 5. Reproducing

`/dumpfood`, then `python3 tools/food_diversity.py --report` (tier tops, fits, rotation table). The 2026-11-19 analysis
script was session scratch; the tables in section 1.1 come from the generator's table.
