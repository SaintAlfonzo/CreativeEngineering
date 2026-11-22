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

**Now:** Raspberry Pie's table sets all 1658 foods (Creative Delight's `solraspberrypie/diversity/creativedelight.json` since 2026-11-21, read by Raspberry Pie; Apple Pie's config list is
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

**Last step, the ceiling scale** (owner, 2026-11-21, plan R14, targets R15): each tier's values are multiplied by one
factor so that each column of the cumulative table in 1.1 peaks at its target: low only 20, mid and low 36.7, + high
53.3, + golden 60, + feasts 72, + gilded 84, + dragon 100. High meals and feasts reach about 3/4 of the ladder
(feasts better than golden); gilded and dragon food unlock the last quarter. The factors are fitted bottom-up (low only
first, then mid, high, golden, feasts, gilded, dragon), each fixing one column. Was (R14): 120 at the top, even steps
of 16.7.

**Tiers** (food tier by `q`, Nourishment counts as high): dragon (id contains `dragon`), gilded (enchanted golden),
feasts (`meal/feast` or the feast family), golden, high (q 5 or more, or Nourishment), mid (q 2 to 5, plus coated food
and candies), low (q under 2). Golden floor after scaling 3.49, gilded floor 4.57.

| Tier | Ceiling scale | Top food | Entries (cans in their source's tier, a family once) |
|---|---:|---:|---:|
| Dragon | 0.853 | 9.38 | 15 |
| Gilded | 0.674 | 6.74 | 45 |
| Feasts | 0.621 | 4.97 | 37 |
| Golden | 0.665 | 4.66 | 53 |
| High | 0.538 | 3.23 | 466 |
| Mid | 0.741 | 2.22 | 432 |
| Low | 1.212 | 1.21 | 373 |

**Medians by type** (diversity), was -> now: raw 0.26 -> 0.79, sweet 0.54 -> 1.62, cooked 0.72 ->
1.53, canned 0.90 -> 1.66, baked -> 1.73, salad 2.52 -> 2.12, handheld 3.03 ->
2.76, soup 3.61 -> 2.37, meal 4.33 -> 3.12, golden 3.41 -> 4.66 (R15 rescaled only golden, feasts, gilded and dragon).

### 1.1 Ceiling by rotation size, memory 32

Live config `endDecay = 32`: a food eaten k meals ago is worth `diversity x (1 - k/32)`, so a loop of N distinct foods
averages `mean diversity x (N - N(N-1)/64)`. Each cell takes the N best entries (a family counts once, drinks and
no-diet foods are out). The peak (best foods newest) is at most 5 above the average.

**The full table, no restrictions to low only.** Each column removes one more group, in the owner's order: dragon,
gilded, feasts, golden, high, mid. "Mid and low" is the table without high; "Low only" without mid. The ceiling row is
each column's best rotation, at the R15 targets. The last column is high meals and feasts with golden left out.

| Rotation | None | No dragon | No dragon, gilded | No D, G, feasts | No D, G, F, golden | Mid and low | Low only | High and feasts, no golden |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 1 | 9.4 | 6.7 | 5.0 | 4.7 | 3.2 | 2.2 | 1.2 | 5.0 |
| 2 | 18.5 | 13.3 | 9.8 | 9.2 | 6.4 | 4.4 | 2.4 | 9.8 |
| 3 | 27.3 | 19.6 | 14.4 | 13.5 | 9.4 | 6.5 | 3.5 | 14.4 |
| 4 | 34.6 | 24.6 | 18.9 | 17.7 | 12.3 | 8.5 | 4.6 | 18.9 |
| 5 | 41.2 | 29.5 | 23.2 | 20.9 | 15.1 | 10.4 | 5.7 | 23.2 |
| 6 | 46.9 | 34.0 | 27.3 | 23.9 | 17.9 | 12.3 | 6.7 | 27.3 |
| 8 | 57.3 | 42.5 | 35.2 | 29.5 | 23.0 | 15.8 | 8.6 | 35.2 |
| 10 | 66.7 | 49.8 | 42.4 | 34.5 | 27.8 | 19.1 | 10.4 | 42.4 |
| 12 | 74.1 | 56.2 | 48.5 | 39.0 | 32.1 | 22.1 | 12.0 | 48.2 |
| 14 | 80.5 | 61.9 | 54.1 | 43.1 | 36.0 | 24.8 | 13.5 | 53.1 |
| 16 | 85.8 | 67.0 | 58.8 | 46.8 | 39.6 | 27.2 | 14.8 | 57.1 |
| 18 | 90.2 | 71.5 | 62.6 | 50.0 | 42.7 | 29.4 | 16.0 | 60.5 |
| 20 | 93.7 | 75.4 | 65.5 | 52.8 | 45.4 | 31.3 | 17.0 | 63.5 |
| 22 | 96.2 | 78.4 | 67.8 | 55.1 | 47.7 | 32.9 | 17.9 | 65.9 |
| 24 | 98.1 | 80.7 | 69.7 | 57.0 | 49.7 | 34.2 | 18.6 | 67.7 |
| 26 | 99.3 | 82.4 | 71.1 | 58.5 | 51.2 | 35.2 | 19.2 | 68.8 |
| 28 | 99.9 | 83.5 | 71.8 | 59.5 | 52.3 | 36.0 | 19.6 | 69.2 |
| 30 | 99.9 | 84.0 | 72.0 | 60.0 | 53.0 | 36.5 | 19.9 | 69.0 |
| 32 | 99.2 | 83.9 | 71.6 | 59.9 | 53.3 | 36.7 | 20.0 | 68.4 |
| **Ceiling** (at N) | **100.0** (29) | **84.0** (31) | **72.0** (29) | **60.0** (31) | **53.3** (32) | **36.7** (32) | **20.0** (32) | **69.2** (28) |
| Entries | 1421 | 1406 | 1361 | 1324 | 1271 | 805 | 373 | 1308 |

**One tier only**, same order. Small tiers run out of foods: dragon has 15 entries, gilded 45.

| Rotation | Dragon | Gilded | Feasts | Golden | High | Mid | Low |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 1 | 9.4 | 6.7 | 5.0 | 4.7 | 3.2 | 2.2 | 1.2 |
| 2 | 18.5 | 13.3 | 9.8 | 9.2 | 6.4 | 4.4 | 2.4 |
| 3 | 27.3 | 19.6 | 14.4 | 13.5 | 9.4 | 6.5 | 3.5 |
| 4 | 34.6 | 24.6 | 18.9 | 17.7 | 12.3 | 8.5 | 4.6 |
| 5 | 41.2 | 29.5 | 23.2 | 20.9 | 15.1 | 10.4 | 5.7 |
| 6 | 46.9 | 34.0 | 27.3 | 23.9 | 17.9 | 12.3 | 6.7 |
| 8 | 56.5 | 42.5 | 35.2 | 29.5 | 23.0 | 15.8 | 8.6 |
| 10 | 64.6 | 49.8 | 42.4 | 34.5 | 27.8 | 19.1 | 10.4 |
| 12 | 69.1 | 55.8 | 48.2 | 39.0 | 32.1 | 22.1 | 12.0 |
| 14 | 70.8 | 61.0 | 53.1 | 43.1 | 36.0 | 24.8 | 13.5 |
| 16 | 70.3 | 65.6 | 57.1 | 46.8 | 39.6 | 27.2 | 14.8 |
| 18 | 70.3 | 69.7 | 60.5 | 50.0 | 42.7 | 29.4 | 16.0 |
| 20 | 70.3 | 73.1 | 63.5 | 52.8 | 45.4 | 31.3 | 17.0 |
| 22 | 70.3 | 76.0 | 65.9 | 55.1 | 47.7 | 32.9 | 17.9 |
| 24 | 70.3 | 78.3 | 67.7 | 57.0 | 49.7 | 34.2 | 18.6 |
| 26 | 70.3 | 80.0 | 68.8 | 58.5 | 51.2 | 35.2 | 19.2 |
| 28 | 70.3 | 81.0 | 69.1 | 59.5 | 52.3 | 36.0 | 19.6 |
| 30 | 70.3 | 80.9 | 68.6 | 59.8 | 53.0 | 36.5 | 19.9 |
| 32 | 70.3 | 80.1 | 67.7 | 59.3 | 53.3 | 36.7 | 20.0 |
| **Ceiling** (at N) | **70.8** (14) | **81.0** (29) | **69.1** (27) | **59.9** (29) | **53.3** (32) | **36.7** (32) | **20.0** (32) |
| Entries | 15 | 45 | 37 | 53 | 466 | 432 | 373 |

**Average set, one tier only:** N foods at the tier's mean value (a player eating the tier, not its best foods).

| Tier | Mean | N 4 | N 8 | N 16 | N 24 | N 32 | Ceiling (at N) |
|---|---:|---:|---:|---:|---:|---:|---:|
| Dragon | 6.00 | 22.9 | 42.7 | 70.3 | 70.3 | 70.3 | 70.3 (32) |
| Gilded | 4.51 | 17.2 | 32.1 | 55.2 | 69.3 | 74.4 | 74.4 (32) |
| Feasts | 3.85 | 14.7 | 27.4 | 47.2 | 59.2 | 63.5 | 63.5 (32) |
| Golden | 3.28 | 12.5 | 23.3 | 40.1 | 50.4 | 54.0 | 54.0 (32) |
| High | 2.48 | 9.4 | 17.6 | 30.3 | 38.1 | 40.8 | 40.8 (32) |
| Mid | 1.48 | 5.6 | 10.6 | 18.1 | 22.8 | 24.4 | 24.4 (32) |
| Low | 0.77 | 2.9 | 5.5 | 9.5 | 11.9 | 12.8 | 12.8 (32) |

Was (2026-11-19, window 32): None 91.9, golden excluded 85.1, high only 84.9, mid only 15.8, low only 17.6. The
enchanted golden apple (10) was the top food, and six cake and pie slices at a config value of 4 carried the low tier.

**Best foods by rank** (no restrictions): rotation N eats the N best entries, so rank N is the food rotation N adds.

| Rank | Food | Tier | Value | Rotation total |
|---:|---|---|---:|---:|
| 1 | roasted_dragon_steak | Dragon | 9.38 | 9.4 |
| 2 | fried_dragon_egg | Dragon | 9.38 | 18.5 |
| 3 | dragon_breath_and_chorus_soup | Dragon | 9.38 | 27.3 |
| 4 | liquid_dragon_egg | Dragon | 8.19 | 34.6 |
| 5 | steamed_dragon_egg | Dragon | 7.60 | 41.2 |
| 6 | dragon_leg_with_sauce | Dragon | 6.88 | 46.9 |
| 7 | enchanted_golden_honey_bottle | Gilded | 6.74 | 52.2 |
| 8 | enchanted_golden_cooked_porkchop | Gilded | 6.74 | 57.3 |
| 9 | enchanted_golden_cooked_beef | Gilded | 6.74 | 62.2 |
| 10 | dragon_meat_stew | Dragon | 6.56 | 66.7 |
| 11 | canned_steamed_dragon_egg | Dragon | 6.08 | 70.6 |
| 12 | smoked_dragon_leg | Dragon | 5.84 | 74.1 |
| 13 | roasted_dragon_meat | Dragon | 5.84 | 77.5 |
| 14 | enchanted_golden_cooked_salmon | Gilded | 5.62 | 80.5 |
| 15 | enchanted_golden_cooked_mutton | Gilded | 5.62 | 83.3 |
| 16 | canned_enchanted_golden_honey_bottle | Gilded | 5.40 | 85.8 |
| 17 | canned_enchanted_golden_cooked_porkchop | Gilded | 5.40 | 88.1 |
| 18 | canned_enchanted_golden_cooked_beef | Gilded | 5.40 | 90.2 |
| 19 | enchanted_golden_carrot | Gilded | 5.25 | 92.0 |
| 20 | canned_dragon_meat_stew | Dragon | 5.24 | 93.7 |
| 21 | enchanted_golden_cooked_chicken | Gilded | 5.00 | 95.1 |
| 22 | bowl_of_guardian_soup | Feasts | 4.97 | 96.2 |
| 23 | plate_of_stuffed_hoglin_snout | Feasts | 4.97 | 97.3 |
| 24 | bowl_of_pineapple_fried_rice | Feasts | 4.97 | 98.1 |
| 25 | roasted_mutton_rack | Feasts | 4.92 | 98.8 |
| 26 | stuffed_pumpkin | Feasts | 4.92 | 99.3 |
| 27 | shepherds_pie | Feasts | 4.92 | 99.7 |
| 28 | roast_chicken | Feasts | 4.92 | 99.9 |
| 29 | honey_glazed_ham | Feasts | 4.92 | 100.0 |
| 30 | stuffed_cantaloupe | Feasts | 4.92 | 99.9 |
| 31 | nachos_bowl | Feasts | 4.88 | 99.7 |
| 32 | enchanted_golden_pumpkin_pie | Gilded | 4.76 | 99.2 |

**Top 8 per tier** (the same at every rotation of 8 or more):

| Tier | Top 8 |
|---|---|
| Dragon | roasted_dragon_steak 9.38, fried_dragon_egg 9.38, dragon_breath_and_chorus_soup 9.38, liquid_dragon_egg 8.19, steamed_dragon_egg 7.60, dragon_leg_with_sauce 6.88, dragon_meat_stew 6.56, canned_steamed_dragon_egg 6.08 |
| Gilded | enchanted_golden_honey_bottle 6.74, enchanted_golden_cooked_porkchop 6.74, enchanted_golden_cooked_beef 6.74, enchanted_golden_cooked_salmon 5.62, enchanted_golden_cooked_mutton 5.62, canned_enchanted_golden_honey_bottle 5.40, canned_enchanted_golden_cooked_porkchop 5.40, canned_enchanted_golden_cooked_beef 5.40 |
| Feasts | bowl_of_guardian_soup 4.97, plate_of_stuffed_hoglin_snout 4.97, bowl_of_pineapple_fried_rice 4.97, roasted_mutton_rack 4.92, stuffed_pumpkin 4.92, shepherds_pie 4.92, roast_chicken 4.92, honey_glazed_ham 4.92 |
| Golden | golden_roll_slice 4.66, golden_roll 4.66, golden_nutritional_bar 4.66, golden_apple 4.57, canned_golden_nutritional_bar 3.73, canned_golden_apple 3.66, canned_golden_apples 3.66, golden_cooked_porkchop 3.52 |
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
