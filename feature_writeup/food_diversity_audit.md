# Food diversity audit (2026-11-20)

This audit finds foods whose Spice of Life diversity is out of line with their hunger and saturation, and processes
that add too much or too little diversity. It is investigation only: nothing was changed. The audit measured the
`/dumpfood` export of 2026-11-19 18:28 (`kubejs/exported/food/foods.json`, 1697 foods; `recipes.json`). Per-food
flags are in [food_diversity_audit.csv](food_diversity_audit.csv) (one row per flag, columns `flag, id, name, type,
nutrition, saturationModifier, diversity, formula, note`).

Removed items (`canned_goods:canned_food` at 9.92, `canned_goods:hot_hot_hotpot`) are left out. Both are still
registered with Canned Goods' broken modifiers (28 and 3).

## 1. How diversity is valued today

- **No pack tuning layer is live.** All 1675 foods outside Apple Pie's config list match its formula exactly:
  `q = hunger x (1 + modifier) / 2`, complexity `q / 5` below q 5, else `1 + 4 x log10(q - 4)`. The planned
  `sol_complexity.js` layer ([food.md](food.md) 3.5) is not built. Because complexity grows with hunger and modifier,
  every mismatch below comes from (a) the 22 config overrides, (b) the drinks tag, (c) the curve's shape, or
  (d) type and process design, not from per-food randomness.
- **Raspberry Pie:** drinks (`#solraspberrypie:drinks`, 157 items) give 0 diversity. Window 30 meals, penalty
  `1 - age/30`; a 30-food rotation averages `mean complexity x 15.5`. The ladder runs from 2 to 100.
- **Medians by type** (complexity): raw 0.26, sweet 0.54, cooked 0.72, canned 0.90, salad 2.52, handheld 3.03,
  golden 3.41, soup 3.61, meal 4.33.

**What 30-food rotations reach** (steady state, best 30 of each set):

| Diet | Diversity |
|---|---:|
| Best 30 foods in the pack | 91.7 |
| Golden Food items only (42 items) | 84.0 |
| Best 30 meals / soups / handhelds | 78.2 / 70.9 / 69.3 |
| Three dishes in color and number variants (section 3.6) | 49.5 |
| Best 30 raw | 34.9 |
| Vanilla 5 (steak, baked potato, bread, chicken, apple) | 4.8 |

The top 30 overall are 13 golden, 10 canned and 5 meals. Rung 100 is out of reach for every diet. Golden Food
alone reaches 84, against 91.7 for the best possible diet.

## 2. Food values

### 2.1 Config overrides out of line (16)

Apple Pie's default `complexityUnparsed` list still applies:

| Food | Hunger / mod | Config | Formula | Issue |
|---|---|---:|---:|---|
| Farmer's Delight cake, apple, chocolate and cheesecake slices; Farmer's Respite rose hip and coffee cake slices | 2-3 / 0.1-0.3 | 4 | 0.22-0.39 | 10 to 18x the formula. Other mods' cake slices score 0.11 |
| `farmersrespite:coffee` | 0 / 0 | 3 | 0 | **No food type and not in the drinks tag**, so a zero-hunger drink scores 3 |
| Steak, cooked porkchop | 8 / 0.8 | 2 | 3.02 | Lowered: top vanilla meats score below a one-step exotic meat (3.1) |
| Golden carrot | 6 / 0.6 | 2 | 0.96 | Raised |
| Golden apple / enchanted golden apple | 8 / 0.3, 16 / 0.3 | 4 / 10 | 1.32 / 4.22 | Enchanted golden apple is the single highest food (10) |
| Large Meals: hearty lunch, mushroom pot pie, pufferfish broth, sweet berry custard | 7-12 | 5-6 | 1.82-4.33 | Raised over every other meal |
| Blazing chili | 10 / 0.4 | 5 | 2.91 | Raised |

The five config teas are drinks, so they score 0 whatever the list says.

### 2.2 High hunger, zero diversity: drinks that fill (9)

Honey jar (10 hunger), berry matcha latte and bubble tea (8), fermented garlic honey (7), matcha latte, chorus milk
tea, soothing hot chocolate, honey bottle and honey coffee (6). A drink that fills like a meal adds nothing to the
diet. The open question is whether honey jar and garlic honey are drinks at all; they read as spreads or preserves.

### 2.3 Low hunger, high diversity: the saturation modifier

The formula weights the modifier as heavily as hunger, so foods with a high modifier score far above their hunger:

- Canned enchanted golden milk bucket and canned enchanted golden pumpkin pie: 2 hunger, modifier 8, **3.80**.
- Enchanted golden milk bucket and enchanted golden pumpkin pie: 8 hunger, modifier 8, **7.02** (second only to the
  enchanted golden apple).
- `oceanic_delight:ancient_fish_eggs`: raw, 7 hunger, modifier 3, **5.00**, the highest raw food by double.
- `nethersdelight:magma_gelatin` (1 / 6, 0.70) and `delightful:animal_fat` (1 / 2) are the small cases.

### 2.4 The curve's knee at q 5

The slope jumps about 9x at q 5 (0.2 per q below, 1.74 just above). Foods next to it differ a lot for a small change
in value: cooked chicken (6 / 0.6) scores 0.96, maple glazed bacon and the sweet roll (6 / 0.8) score 1.58. In the
pack, 68 foods sit just below the knee (q 4.5-5) and 42 just above (q 5-5.6). Any value change near 6 hunger moves
diversity sharply.

### 2.5 Type inversions

- **Raw above 1.5 (15):** dragon leg and raw dragon meat 2.52, elder guardian eye 2.59, elder guardian slab 2.45,
  wrapped cantaloupe 3.32, ancient fish eggs 5.00, and 9 Rustic Delight bell pepper rolls at 2.45. The rolls and the
  wrapped cantaloupe are prepared dishes, so their raw type looks wrong.
- **Dishes below 1 (35 meals, soups and salads):** mostly Ender's Delight at 4 hunger (chorus stew, endermite stew,
  amberveil stew 0.56-0.60; ender paella and pearl pasta 0.56), slabdish 0.48, caramel popcorn 0.80 (typed meal),
  kelp encrusted cod 0.80, sopa de macaco 0.70, maggot salad 0.96. They cost meal-grade effort and score like a
  cooked vegetable.

### 2.6 Untyped foods (107)

107 edible items have no food type (full list in the CSV under `untyped`). Two groups count as extra foods:

- **13 edible Create Central Kitchen intermediates** (`incomplete_hamburger`, `incomplete_*_pie`, ...): unfinished
  sequenced-assembly items, 0.24-0.80 each.
- **Ender's Delight `*_wood` bowls** (chorus stew, endermite stew, paella...): a second item for the same dish,
  counted as a separate food.

The rest look like a stale catalog: venison and duck dishes (Abnormals Delight, up to 4.87), the Delightful nut
butter and jelly line, goat. They need a `food_catalog.py` pass.

## 3. Processes

Each recipe with a food output was compared with its best food input (the median member for a tag). The median
change in diversity by step:

| Process | Recipes | Median change | Outputs below the best input (< -0.5) | Big gains (>= +2) |
|---|---:|---:|---:|---:|
| Cooking Pot (`farmersdelight:cooking`) | 323 | +2.36 | 10 | 167 |
| Shaped crafting | 75 | +1.52 | 1 | 34 |
| Create filling | 95 | +1.40 | 0 | 30 |
| Smoking / campfire / smelting | 109 / 106 / 102 | +0.46 to +0.48 | 0 | 12-15 each |
| Shapeless crafting (includes canning) | 877 | +0.44 | 87 | 172 |
| Baking (Creative Delight) | 42 | +0.20 | 1 | 1 |
| Create mixing | 625 | 0.00 | 6 | 182 |
| Cutting | 91 | -0.31 | 23 | 0 |
| Brewing, fermenting, wine | 17-64 | -0.22 to -0.26 | 1-7 | 0 |

Cooking Pot dishes earn their diversity. The outliers are below.

### 3.1 Too much: one cooking step on an exotic meat

Cooking a single raw meat outscores the vanilla steak (2): cooked bison **4.11** (from 0.60), cooked kangaroo 3.13,
cooked goose 3.41, cooked crab leg 3.02 (from 0.13), cooked pike 3.02, cooked mutton rack 3.80, smoked ham 3.80,
smoked dragon leg and roasted dragon meat 4.61. That is meal level from one furnace step, because the source mods give
these meats 8-10 hunger at modifier 0.8-1.0.

### 3.2 Too much: Create filling (one fluid dip)

- Apple to honeyed, chocolate or white chocolate apple: 0.52 -> 3.02; ruby chocolate apple 3.41.
- Waffle (0.56) to ruby, white, dark, plain or caramel chocolate waffle: 3.02-3.91.
- Sweet berries (0.22) to five glazed-berry variants: 2.45 each.
- A chocolate bar (0.78) to 16 chocolate candies: 2.79 each.

### 3.3 Too much: Golden Food

Eight gold nuggets plus a steak make a golden steak at **5.07** (+3.07). The enchanted versions (sequenced assembly)
score 5.3-7.0. 28 golden items score 3 or more, and 42 golden items reach 84 in rotation, almost the best possible
diet. The gold-dip step adds two new high-value foods for every vanilla food.

### 3.4 Both ways: canning

292 cans have a food source. The median change is +0.3, but the spread is wide (51 gain 1 or more, 42 lose 1 or
more):

- **Gains:** a can serving is source hunger x the section multiplier, and the log curve rewards bigger portions.
  Cooked beef (2) -> canned beef 5.07; cooked chicken 0.96 -> 3.99; cooked salmon and mutton 1.58 -> 4.33; canned
  soups at 18 hunger score 5.25 against about 3.6 for the soup. Ten of the pack's top 30 foods are cans.
- **Losses:** golden cans use family values, so canning a golden food throws its value away: enchanted golden apple
  10 -> 0.52, enchanted golden steaks 5.58 -> 0.90, golden steaks 5.07 -> 0.72.
- **Count:** each can is a second distinct food beside its source, so canning doubles the entries a player can
  rotate.

### 3.5 Too little: dishes worth less than an ingredient

- Guardian sandwich 0.96 from cooked guardian fillet 2.79; cave skewer 1.17 from baked spider leg 2.20; bell pepper
  soup 0.96 and cooked rice 0.84 from recipes that use bone broth (2.79).
- Multi-ingredient outputs that stay flat (3 or more food inputs, output no more than 0.3 above the best input; 202
  recipes, mostly cans): cantaloupe and salmonberry gummies 0.20, hamimelon shaved ice 0.12 (5 food inputs),
  cantaloupe popsicle 0.36, crawling sandwich 0.36 (4 food inputs), fish oil 0.

### 3.6 Too much by count: color and number variants

73 extra foods come from families that differ only by a color or number, with identical values:

| Family | Variants | Each |
|---|---:|---:|
| Rustic Delight stuffed bell pepper | 9 | 3.61 |
| Rustic Delight bell pepper roll | 9 | 2.45 |
| Create Confectionery chocolate candy (`_1`/`_2`/`_3`, 4 chocolates) | 16 | 2.79 |
| Rustic Delight bell pepper, slice, roasted, roasted slice | 9 each | 0.11-0.90 |
| Lightman's Currency chocolate coins | 6 | 0.22 |
| Create Confectionery bars, glazed berries, glazed marshmallows | 3 each | 0.78-2.45 |

Three dishes (stuffed peppers, rolls and candies: 34 items) rotate to **49.5**, rung 45. That is half the best
possible diet from three recipes, and above any raw or cooked diet.

## 4. Levers (for a decision, not decided)

Most findings are one design question: should diversity follow food value (today's formula), food type, or effort?

1. **Build the planned type layer** ([food.md](food.md) 3.5): complexity per food type, item overrides on top. That
   fixes 2.3-2.5, 3.1-3.3 and the cans in one place, and the curve's knee stops mattering.
2. **Clear Apple Pie's default config list** (2.1) or replace it with pack values; fix coffee's type.
3. **Count a can as its source** (or give cans the source's value): stops canning doubling entries and repairs the
   golden can losses.
4. **Alias variant families** to one diet entry (one food per family): stuffed peppers, rolls, candies, coins,
   Ender's `*_wood` bowls. Raspberry Pie already holds a per-item hook (the drinks tag); a `#solraspberrypie:same_food/<family>`
   tag would fit beside it.
5. **Drop edible intermediates** from the diet (Central Kitchen `incomplete_*`), or make them inedible.
6. **Review drinks that fill** (2.2): give the latte group a small value, or move honey jar and garlic honey out of
   drinks.
7. **Golden Food:** score golden versions at their source's value plus a fixed bonus, rather than through the
   modifier.

## 5. Reproducing

The analysis script is session scratch, not a pack tool. Rerun it on a fresh `/dumpall` export: the formula, the
drinks tag (`solraspberrypie:drinks`), the food types (`creativedelight:food_type/*` tags) and the recipe `in`/`out`
fields in `recipes.json` are all it reads.
