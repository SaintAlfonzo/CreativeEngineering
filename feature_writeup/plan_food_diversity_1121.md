# Plan: food diversity rework 2026-11-21

<!-- generated:status_index begin open_work sha1=19c26e770580 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 5 open TODO rows (Planned 5), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `food-tuning #32` | Planned | S C | Diversity table in Raspberry Pie: per-food values override Apple Pie, family members share one diet entry, no-diet tag; Apple Pie's default override list cleared |
| `food-tuning #33` | Planned | P | Diversity generator: tier tops dragon 12, gilded 10, feasts 8, golden 7, high 6, mid 3, low 1; cans 80% of input; vanilla meat fit for cooked meats; pot curve for baking, cooked crafts and filling dishes; small values for non-food inputs |
| `food-tuning #34` | Planned | S C | Pepper and covering families: medley color groups merge per form; chocolate coverings merge per base food; candies chocolate-filled and caramel-filled |
| `food-tuning #35` | Planned | S | Brewing, fermenting and wine outputs are drinks (0 diversity, Liquid Diet Log only); chocolate coins out of the diet |
| `food-tuning #36` | Planned | S P | Cooking Pot versions of the Create filling food recipes (coating as items, about 1.5x the spout) |
<!-- generated:status_index end open_work -->

Source: owner notes on [food_diversity_audit.md](food_diversity_audit.md) (chat 2026-11-21), rulings from two
AskUserQuestion rounds the same day. Builds the diversity layer the audit's levers 1 to 4 describe, owned by
Raspberry Pie (`solRaspberryPie`) instead of the KubeJS design in [food.md](food.md) 3.5. TODO rows: section 5;
status lives in the rows.

## 1. Intake

| # | Item (owner's words, shortened) | Class | Premise check |
|---|---|---|---|
| I1 | "dragon food and feasts to have a multiplier on their diversity to bring their top foods to the stairstep pattern the table expects, but with dragon food better than golden food" | open -> ruled R2 | Audit 1.1: dragon and feasts never reach the top 32 today |
| I2 | "add a multiplier to golden if needed at the same time" | open -> ruled R2 | Golden steaks 5.07, gilded 5.3-10 (audit 3.3) |
| I3 | "canned food should provide slightly less diversity than the input food" | open -> ruled R4 | Cans gain or lose up to 9 today (audit 3.4) |
| I4 | "baking should give on average the same boost to diversity that the cooking pot does" | open -> ruled R7 | Baking median +0.20, pot +2.36 (audit 3) |
| I5 | "brewing and fermenting things into drinks should destroy the diversity it has and turn it to a drink that only contributes to the drink log" | clear | `#solraspberrypie:drinks` already gives 0 diversity and Liquid Diet Log only (Drinks.java); the work is tagging every brewing, fermenting and wine output that is a drink |
| I6 | "cooked shaped/shapeless/assemble crafts that dont have a raw version should not get a diversity penalty and be about the same boost as cooking pot" | open -> ruled R7 | |
| I7 | "modded cooked meats should follow the vanilla meats diversity in terms of hunger/saturation ratio" | clear | Cooked bison 4.11, smoked dragon leg 4.61 against steak 2 (audit 3.1). Rule: fit the vanilla cooked meats' diversity to their hunger x (1 + modifier), apply to every cooked/meat and cooked/fish |
| I8 | "cooked dragon meat gets significant boost but not as high as dragon meat/egg in meals" | clear | Cooked dragon meat sits between the meat rule and the dragon meal tier |
| I9 | "mirror create filling recipes (ruby chocolate on apple/waffle, etc) in the cooking pot (slightly more expensive in cooking vs spout, adjust ratios) and base the diversity off the cooking pot value" | open -> ruled R3 | 95 food filling recipes (audit 3); list from `recipes.json` `create:filling` with a food input |
| I10 | "keep golden foods diversity lower and gilded higher, split golden and gilded in table, so the order is dragon, gilded, feasts, golden, high, mid, low" | clear | Gilded = enchanted golden foods (food.md 3.16). The audit table gets a gilded column |
| I11 | "fix the meals worth less than ingredients, by folding them into the family rules/providing small diversity values for non food items like ice, bonemeal, mushrooms, and blaze powder" | clear | Audit 3.5. Non-food inputs get a small input value in the generator only (they stay inedible) |
| I12 | "bell peppers diversity in all items should be tied to the medley combinations ... red-yellow-green ... pink-white-orange and black-purple-blue" | open -> ruled R5 | 9 colors x 6 forms (audit 3.6) |
| I13 | "the same is true for candy, where the base food is the family (3 chocolate + caramel); all covered marshmallows and berries count for the same food" | open -> ruled R6 | 16 candies, glazed berries and marshmallows (audit 3.6) |
| I14 | "lightmans chocolate coins shouldnt be obtainable" | premise: already true | `config/lightmanscurrency-common.lcconfig` [events]: `chocolate=false`, `chocolateDrops=false`, `advancementRewards=false`; no recipe makes them (recipes.json). Only coin-pile blocks drop them |
| I15 | "set their diversity to 0 and disable them from affecting diet" | clear | New tag `#solraspberrypie:no_diet`: like a drink without the Liquid Diet Log |
| I16 | (from the overrides question) Apple Pie's default `complexityUnparsed` list | open -> ruled R8 | 22 entries (audit 2.1) |

## 2. Owner rulings

| # | Item | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| R1 | Where values and families live | open | Raspberry Pie table: `tools/food_diversity.py` computes every food's value from the dumps and rules into a table shipped in solRaspberryPie; Raspberry Pie overrides Apple Pie's complexity and maps family members to one diet entry. Tooltips, Diet Log and Food Dictionary read the same value | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R2 | Top food per tier (I1, I2, I10) | open | Dragon 12, gilded 10, feasts 8, golden 7, high 6, mid 3, low 1. Tier multipliers are fitted so each tier's best food lands there | 2026-11-21 | AskUserQuestion 2026-11-21 (owner typed values) |
| R3 | Pot mirror of filling recipes (I9) | open | Coating as items at about 1.5x the spout's fluid, batched to whole items (example 2 apples + 3 chocolate bars -> 2 chocolate apples) | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R4 | Cans (I3) | open | A can scores 80% of its input food (golden and gilded cans included). Cans stay a separate diet entry | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R5 | Pepper families (I12) | open | Within a medley group the colors merge and the forms stay separate: red, yellow and green stuffed peppers are one food; orange, white and pink roasted slices are one food; same for slices, raw, roasted. Rolls follow the same rule (agent default, unconfirmed) | 2026-11-21 | AskUserQuestion 2026-11-21 (owner text) |
| R6 | Covering families (I13) | open | Milk, dark, ruby and white chocolate are one family (like a pepper group); honey, caramel and any other non-chocolate covering are separate foods. Reading applied (agent default, unconfirmed): per base food, all chocolate coverings = 1 entry (apples, waffles, rolls, glazed berries, glazed marshmallows); candies: the 12 chocolate-filled = 1 entry, the 4 caramel-filled = 1 entry | 2026-11-21 | AskUserQuestion 2026-11-21 (owner text) |
| R7 | "Same boost as the cooking pot" (I4, I6, I9) | open | Pot rule, same curve: fit the pot's output against the sum of its inputs and apply it to baking, cooked crafts without a raw version and the mirrored filling dishes | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R8 | Apple Pie default overrides (I16) | open | Clear the list; the table sets every food (slices fall to the low tier, steak follows the meat rule, coffee becomes a drink) | 2026-11-21 | AskUserQuestion 2026-11-21 |
| R9 | Small values for non-food inputs (I11) | clear | Generator-only input values (ice, bone meal, mushrooms, blaze powder and similar about 0.1 to 0.25); a dish never scores below its best food input | 2026-11-21 | agent default, unconfirmed |

## 3. Design

**Table.** `tools/food_diversity.py` reads `kubejs/exported/food/foods.json`, `recipes.json` and the food-type tags
and writes `development/solRaspberryPie/src/main/resources/data/solraspberrypie/diversity/values.json` (item ->
value) and `families.json` (family id -> members). Its rules config is
`kubejs/generation_scripts/food/food_diversity.json` (tier tops, multipliers, can share, non-food input values,
families). A `--report` prints the tier tops and the audit's rotation table with golden and gilded split, so the
stairstep (R2) is checked on every run. Listed in `tools/generators.json`.

**Rule order** (each food takes the first rule that fits):
1. `#solraspberrypie:no_diet` (chocolate coins): 0, not counted.
2. Drinks: 0 (existing tag; I5 adds the brewing, fermenting and wine outputs).
3. Cans: 0.8 x the input food's value (R4).
4. Cooked meat and fish with a raw source: vanilla meat fit on hunger x (1 + modifier) (I7); cooked dragon meat x
   its dragon multiplier, capped under the dragon meal tier (I8).
5. Recipe-derived (pot, baking, cooked crafts without a raw version, mirrored filling dishes): the pot curve fitted
   on output value against the sum of input values (R7), non-food inputs at their small value (R9); never below the
   best food input.
6. Everything else: Apple Pie's formula.
Then tier multipliers (dragon, gilded, feast, golden) are applied, fitted so the best food of each tier meets R2.

**Families.** A family's members share one diet entry: eating any member counts as eating the family, and the
diversity of the entry is the member's own value. Raspberry Pie keys Apple Pie's food memory by family id through
a mixin on the food list (the same place the drinks skip lives, FoodListMixin).

**Pot mirrors** (R3): NCH cooking recipes in Creative Delight's server scripts (the Create Factory, Confectionery,
Neapolitan and Buzzier Bees food fillings), coating as items at about 1.5x the spout amount, batched.

## 4. Phases

| Phase | Work | Owner of the code |
|---|---|---|
| P1 | Raspberry Pie: table loader (datapack JSON, reloadable), complexity override, families, `no_diet` tag; Apple Pie config list cleared (R8) | solRaspberryPie |
| P2 | Generator `food_diversity.py` + rules config + `--report`; audit table with the gilded column | tools |
| P3 | Drinks tag: brewing, fermenting, wine outputs (I5); chocolate coins in `no_diet` (I15) | KubeJS tags |
| P4 | Pot mirrors of the filling recipes (I9, R3) | Creative Delight scripts |
| P5 | Docs (raspberry_pie.md, food_diversity_audit.md follow-up), changelog, queue rows (prefix RPI) | docs |

## 5. TODO rows

`food-tuning #32` (P1), `#33` (P2), `#34` (families, P1 + P2), `#35` (P3), `#36` (P4).
