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
3. Cooked meat and fish heated from a raw source: the vanilla meats' fit on `q`.
4. Cooking Pot dishes: the pack curve, never below the best food input + 0.1.
5. Baking: the best ingredient + the pot's median boost, scaled by the batch's input value, capped one tier above
   the food's own (a food lifted past its tier's top moves up a tier).
6. Create filling, and shaped, shapeless or assembled crafts that are no raw food's cooked version: the pot rule,
   1.69 x the inputs' value per output item (crafts only when that is higher than the curve).
7. Everything else: the pack curve, `q` 0-2 -> 0-1, 2-5 -> 1-3, 5-12 -> 3-6, 12-30 -> 6-9 (no knee).

Then the tiers: dragon, gilded, feast and golden get multipliers that lift each tier's third-best food to its top
(golden and gilded on their source food); golden is floored at the average high-tier meal and gilded at the average
feast; low, mid and high are capped at their tops; coated food and candies are mid tier, capped at its top and never
below their best food input. Families share one diet entry and their best member's value.

**Last step, the ceiling scale** (owner, 2026-11-21, plan R14): each tier's values are multiplied by one factor so that
the cumulative table in 1.1 peaks at 120 with nothing excluded and the columns below step evenly down to low only
(120, 103.3, 86.7, 70, 53.3, 36.7, 20). The factors are fitted bottom-up (low only first, then mid, high, golden,
feasts, gilded, dragon), each fixing one column.

**Tiers** (food tier by `q`, Nourishment counts as high): dragon (id contains `dragon`), gilded (enchanted golden),
feasts (`meal/feast` or the feast family), golden, high (q 5 or more, or Nourishment), mid (q 2 to 5, plus coated food
and candies), low (q under 2). Golden floor after scaling 4.08, gilded floor 5.68.

| Tier | Ceiling scale | Top food | Entries (cans in their source's tier, a family once) |
|---|---:|---:|---:|
| Dragon | 0.993 | 10.93 | 15 |
| Gilded | 0.838 | 8.38 | 45 |
| Feasts | 0.752 | 6.01 | 37 |
| Golden | 0.777 | 5.44 | 53 |
| High | 0.539 | 3.23 | 466 |
| Mid | 0.741 | 2.22 | 432 |
| Low | 1.212 | 1.21 | 373 |

**Medians by type** (diversity), was -> now: raw 0.26 -> 0.79, sweet 0.54 -> 1.62, cooked 0.72 ->
1.53, canned 0.90 -> 1.66, baked -> 1.73, salad 2.52 -> 2.12, handheld 3.03 ->
2.76, soup 3.61 -> 2.37, meal 4.33 -> 3.12, golden 3.41 -> 5.44.

### 1.1 Ceiling by rotation size, memory 32

Live config `endDecay = 32`: a food eaten k meals ago is worth `diversity x (1 - k/32)`, so a loop of N distinct foods
averages `mean diversity x (N - N(N-1)/64)`. Each cell takes the N best entries (a family counts once, drinks and
no-diet foods are out). The peak (best foods newest) is at most 5 above the average.

**The full table, no restrictions to low only.** Each column removes one more group, in the owner's order: dragon,
gilded, feasts, golden, high, mid. "Mid and low" is the table without high; "Low only" without mid. The ceiling row is
each column's best rotation; they step by 16.7.

| Rotation | None | No dragon | No dragon, gilded | No D, G, feasts | No D, G, F, golden | Mid and low | Low only |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 1 | 10.9 | 8.4 | 6.0 | 5.4 | 3.2 | 2.2 | 1.2 |
| 2 | 21.5 | 16.5 | 11.8 | 10.7 | 6.4 | 4.4 | 2.4 |
| 3 | 31.8 | 24.3 | 17.5 | 15.8 | 9.4 | 6.5 | 3.5 |
| 4 | 40.3 | 30.6 | 22.9 | 20.7 | 12.3 | 8.5 | 4.6 |
| 5 | 48.0 | 36.6 | 28.1 | 24.4 | 15.2 | 10.4 | 5.7 |
| 6 | 54.9 | 42.2 | 33.1 | 27.9 | 17.9 | 12.3 | 6.7 |
| 8 | 68.0 | 52.7 | 42.6 | 34.5 | 23.0 | 15.8 | 8.6 |
| 10 | 79.0 | 61.8 | 51.3 | 40.3 | 27.8 | 19.1 | 10.4 |
| 12 | 87.8 | 69.5 | 58.4 | 45.6 | 32.1 | 22.1 | 12.0 |
| 14 | 95.5 | 76.4 | 64.9 | 50.4 | 36.1 | 24.8 | 13.5 |
| 16 | 102.0 | 82.5 | 70.6 | 54.6 | 39.6 | 27.2 | 14.8 |
| 18 | 107.7 | 87.9 | 75.2 | 58.4 | 42.7 | 29.4 | 16.0 |
| 20 | 112.1 | 92.5 | 78.7 | 61.7 | 45.5 | 31.2 | 17.0 |
| 22 | 115.2 | 96.4 | 81.6 | 64.4 | 47.8 | 32.8 | 17.9 |
| 24 | 117.6 | 99.1 | 83.9 | 66.6 | 49.7 | 34.2 | 18.6 |
| 26 | 119.1 | 101.2 | 85.6 | 68.4 | 51.2 | 35.2 | 19.2 |
| 28 | 119.9 | 102.6 | 86.6 | 69.6 | 52.3 | 36.0 | 19.6 |
| 30 | 119.9 | 103.3 | 86.6 | 69.9 | 53.0 | 36.5 | 19.9 |
| 32 | 119.2 | 103.2 | 86.1 | 69.2 | 53.3 | 36.7 | 20.0 |
| **Ceiling** (at N) | **120.0** (29) | **103.3** (31) | **86.7** (29) | **70.0** (29) | **53.3** (32) | **36.7** (32) | **20.0** (32) |
| Entries | 1421 | 1406 | 1361 | 1324 | 1271 | 805 | 373 |

**One tier only**, same order. Small tiers run out of foods: dragon has 15 entries, gilded 45.

| Rotation | Dragon | Gilded | Feasts | Golden | High | Mid | Low |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 1 | 10.9 | 8.4 | 6.0 | 5.4 | 3.2 | 2.2 | 1.2 |
| 2 | 21.5 | 16.5 | 11.8 | 10.7 | 6.4 | 4.4 | 2.4 |
| 3 | 31.8 | 24.3 | 17.5 | 15.8 | 9.4 | 6.5 | 3.5 |
| 4 | 40.3 | 30.6 | 22.9 | 20.7 | 12.3 | 8.5 | 4.6 |
| 5 | 48.0 | 36.6 | 28.1 | 24.4 | 15.2 | 10.4 | 5.7 |
| 6 | 54.6 | 42.2 | 33.1 | 27.9 | 17.9 | 12.3 | 6.7 |
| 8 | 65.8 | 52.7 | 42.6 | 34.5 | 23.0 | 15.8 | 8.6 |
| 10 | 75.2 | 61.8 | 51.3 | 40.3 | 27.8 | 19.1 | 10.4 |
| 12 | 80.5 | 69.3 | 58.4 | 45.6 | 32.1 | 22.1 | 12.0 |
| 14 | 82.5 | 75.8 | 64.3 | 50.4 | 36.1 | 24.8 | 13.5 |
| 16 | 81.8 | 81.5 | 69.1 | 54.6 | 39.6 | 27.2 | 14.8 |
| 18 | 81.8 | 86.5 | 73.3 | 58.4 | 42.7 | 29.4 | 16.0 |
| 20 | 81.8 | 90.8 | 76.9 | 61.7 | 45.5 | 31.2 | 17.0 |
| 22 | 81.8 | 94.4 | 79.9 | 64.4 | 47.8 | 32.8 | 17.9 |
| 24 | 81.8 | 97.3 | 82.0 | 66.6 | 49.7 | 34.2 | 18.6 |
| 26 | 81.8 | 99.4 | 83.3 | 68.4 | 51.2 | 35.2 | 19.2 |
| 28 | 81.8 | 100.5 | 83.7 | 69.6 | 52.3 | 36.0 | 19.6 |
| 30 | 81.8 | 100.4 | 83.1 | 69.9 | 53.0 | 36.5 | 19.9 |
| 32 | 81.8 | 99.4 | 82.0 | 69.2 | 53.3 | 36.7 | 20.0 |
| **Ceiling** (at N) | **82.5** (14) | **100.6** (29) | **83.7** (27) | **70.0** (29) | **53.3** (32) | **36.7** (32) | **20.0** (32) |
| Entries | 15 | 45 | 37 | 53 | 466 | 432 | 373 |

Was (2026-11-19, window 32): None 91.9, golden excluded 85.1, high only 84.9, mid only 15.8, low only 17.6. The
enchanted golden apple (10) was the top food, and six cake and pie slices at a config value of 4 carried the low tier.

**Top 8 per tier** (the same at every rotation of 8 or more):

| Tier | Top 8 |
|---|---|
| Dragon | roasted_dragon_steak 10.93, fried_dragon_egg 10.93, dragon_breath_and_chorus_soup 10.93, liquid_dragon_egg 9.54, steamed_dragon_egg 8.86, dragon_leg_with_sauce 8.02, dragon_meat_stew 7.63, canned_steamed_dragon_egg 7.08 |
| Gilded | enchanted_golden_honey_bottle 8.38, enchanted_golden_cooked_porkchop 8.38, enchanted_golden_cooked_beef 8.38, enchanted_golden_cooked_salmon 6.98, enchanted_golden_cooked_mutton 6.98, canned_enchanted_golden_honey_bottle 6.70, canned_enchanted_golden_cooked_porkchop 6.70, canned_enchanted_golden_cooked_beef 6.70 |
| Feasts | bowl_of_guardian_soup 6.01, plate_of_stuffed_hoglin_snout 6.01, bowl_of_pineapple_fried_rice 6.01, roasted_mutton_rack 5.96, stuffed_pumpkin 5.96, shepherds_pie 5.96, roast_chicken 5.96, honey_glazed_ham 5.96 |
| Golden | golden_roll_slice 5.44, golden_roll 5.44, golden_nutritional_bar 5.44, golden_apple 5.34, canned_golden_nutritional_bar 4.35, canned_golden_apple 4.27, canned_golden_apples 4.27, golden_cooked_porkchop 4.12 |
| High | stuffed_burrito 3.23, pickle_soup 3.23, pb_j 3.23, overnight_oats 3.23, deluxe_burger 3.23, cucumber_salad 3.23, cheese_burger 3.23, vegetarian_burger 3.23 |
| Mid | oatmeal_cookie 2.22, coffee_cookie 2.22, vanilla_ice_cream 2.22, vanilla_chocolate_fingers 2.22, strawberry_ice_cream 2.22, strawberry_bean_bonbons 2.22, mint_ice_cream 2.22, mint_chocolate 2.22 |
| Low | strawberry_jello 1.21, salmonberry_jello 1.21, red_grape_jello 1.21, raspberry_jello 1.21, pepper_jello 1.21, mulberry_jello 1.21, green_grape_jello 1.21, cherry_jello 1.21 |

## 2. Food values (2026-11-19 findings, then now)

### 2.1 Apple Pie's config overrides

Was: 16 out of line (slices at 4, steak and porkchop lowered to 2, golden carrot raised to 2, golden apple 4,
enchanted golden apple 10, Large Meals 5 to 6, blazing chili 5, coffee 3 as a zero-hunger non-drink).
**Now:** the list is empty. Cake slice 0.67; steak and porkchop 2.21;
golden carrot 4.08 (golden floor); golden apple 5.34; enchanted golden apple
5.68 (gilded floor; its source is an apple); Hearty Lunch 2.96;
mushroom pot pie 5.41 (feast); blazing chili 2.08; Farmer's
Respite coffee is a drink.

### 2.2 Drinks that fill (honey jar, lattes, bubble tea, garlic honey)

Unchanged: still drinks at 0. Open: whether honey jar and fermented garlic honey are drinks at all.

### 2.3 High saturation modifier, low hunger

Was: enchanted golden milk bucket and pumpkin pie 7.02, their cans 3.80, ancient fish eggs 5.00. **Now:** gilded
values follow the source food (milk bucket and pumpkin pie 5.91, cans
4.73). Ancient fish eggs sit at the high top
(3.23): still the highest raw food. Open.

### 2.4 The curve's knee at q 5

**Fixed:** the pack curve has no knee (slope 0.5 per q below q 2, 0.67 up to q 5, 0.43 up to q 12, before the scale).
Cooked chicken 2.02 against maple glazed bacon 1.87.

### 2.5 Type inversions

- Raw above 1.5 (was): dragon leg and raw dragon meat now 3.58 (below the cooked meat's
  6.80); the bell pepper rolls 1.92 (still typed raw,
  one entry per color group now).
- Dishes below 1 (was): the Ender's Delight 4-hunger stews are now 1.23; caramel popcorn
  2.30 (feast).

### 2.6 Untyped foods (107)

Unchanged; they take the pack curve. They still need a `food_catalog.py` pass.

## 3. Processes

Median change of the output against its best food input (the median member for a tag), now (after the ceiling
scale, so gains are smaller than before it):

| Process | Recipes | Median change | Outputs below the best input (< -0.5) | Gains of 1 or more |
|---|---:|---:|---:|---:|
| Cooking Pot | 275 | +1.35 | 0 | 169 |
| Shaped crafting | 74 | +1.50 | 1 | 60 |
| Baking (Creative Delight) | 39 | +1.20 | 0 | 29 |
| Create filling | 95 | +0.89 | 0 | 47 |
| Smoking | 109 | +0.77 | 2 | 34 |
| Campfire | 106 | +0.76 | 0 | 32 |
| Smelting | 102 | +0.75 | 1 | 31 |
| Shapeless crafting (includes canning, slicing) | 839 | -0.21 | 183 | 138 |
| Create mixing | 597 | +0.00 | 2 | 193 |
| Cutting | 95 | -0.41 | 41 | 0 |
| Vintage Delight fermenting (pickles) | 16 | +0.60 | 1 | 2 |

Was: Cooking Pot +2.36, shaped +1.52, filling +1.40, heat +0.46 to +0.48, shapeless +0.44, baking +0.20, mixing 0,
cutting -0.31, brewing and fermenting -0.22 to -0.26. Brewing, wine and keg recipes are missing from the table now:
their outputs are all drinks (0, Liquid Diet Log only).

### 3.1 One cooking step on an exotic meat

Was: cooked bison 4.11, smoked dragon leg 4.61 against steak 2. **Now:** every cooked meat and fish follows the vanilla
fit, 0.306 x q after the scale (beef and porkchop 2.21, salmon 1.65,
chicken 2.02). Cooked dragon meat (roasted, smoked leg) 6.80,
between the high top and the lowest dragon dish (7.63).

### 3.2 Create filling

Was: chocolate apples 3.02 to 3.41, waffles 3.02 to 3.91, glazed berries 2.45, candies 2.79. **Now:** coated food and
candies are mid tier (chocolate apple 2.22, caramel apple 2.22,
candies 2.22), never below their food input (glazed porkchop
2.26 over cooked porkchop 2.21). Each coating also has a Cooking
Pot recipe (2 bases + 3 coating items -> 2).

### 3.3 Golden Food

Was: golden steak 5.07, gilded 5.3 to 10. **Now:** golden = source x a fitted multiplier, floored at the average high
meal (4.08); gilded likewise, floored at the average feast (5.68). Golden cooked beef
4.12, gilded cooked beef 8.38.

### 3.4 Canning

Was: median +0.3, 51 cans gaining 1 or more, golden cans losing up to 9.5. **Now:** every can is 80% of its source
(canned beef 1.76, canned golden cooked beef 3.29,
canned enchanted golden apple 4.54). A can is still its own diet entry.

### 3.5 Dishes worth less than an ingredient

Was: guardian sandwich 0.96 from a 2.79 fillet, bell pepper soup 0.96, 202 flat multi-input recipes. **Now:** a Cooking
Pot dish never scores below its best food input, and non-food inputs count in the pot rule (ice, mushrooms, blaze
powder 0.25; bone meal, sugar 0.15; others 0.1; containers and gold 0; before the scale). Bell pepper soup
2.12, hamimelon shaved ice 1.21.

### 3.6 Color and number variants

**Fixed by families** (25): bell peppers per form and color group (standard red, yellow, green; pale pink, white,
orange; dark black, purple, blue), so 18 pepper entries instead of 54, and each form counts on its own; chocolate
coatings per base food (apples, waffles, rolls, glazed berries, glazed marshmallows); chocolate-filled candies (12)
and caramel candies (4). Honey and caramel coatings stay separate foods. The chocolate coins are out of the diet.

## 4. Levers: done and open

- Done: the type and process layer (lever 1, as Raspberry Pie's table), Apple Pie's config list cleared (2), cans at
  80% of the source (3, as a separate entry), variant families (4), golden on the source value (7), the ceiling
  scale (120 at the top, even steps to low only).
- Open: Central Kitchen `incomplete_*` intermediates are still edible foods (5); drinks that fill (6); the Diet Log
  ladder (rungs 2 to 100) against a 120 ceiling (plan O1).

## 5. Reproducing

`/dumpfood`, then `python3 tools/food_diversity.py --report` (tier tops, fits, scales, the full cumulative table).
