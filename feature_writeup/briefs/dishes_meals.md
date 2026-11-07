# Brief: dishes / meals

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# dishes`, Sub `meals`; re-checked against the pack 2026-10-31
(brief sweep), which also brought in the rows moved to other families whose plan lives here (`merges/dishes #5`, #6, #8,
#10, `textures/food #2`) and the Barbeque's Delight rows (#46 to #50). Layout per [README.md](README.md). Feature writeups:
[recipes_cooking.md](../recipes_cooking.md) (2.3 Prepared food, 2.4 Golden food), [food.md](../food.md) (2.1 food types),
[food_families.md](../food_families.md) (3.1 family rules), [barbeques_delight.md](../barbeques_delight.md) (rows #46 to #50),
[item_merges.md](../item_merges.md) (merges). Test prefix RC- (recipes_cooking.md, next free RC-176); new tests below are
placeholders `RC-newN` / `FO-newN` / `MG-newN` / `BBQ-newN`, numbered when queued.

**Where the code is now (2026-10-31).** `kubejs/server_scripts/cooking/cooking.js` is an empty stub: every food recipe
moved to the Creative Delight server tree by the mod of the food it makes (2026-10-25; the Brewin' dishes 2026-10-31),
`kubejs/data/creativedelight/kubejs/<mod>/recipes.js` (or `cooking.js`). The ones this brief touches:
[farmersdelight/recipes.js](../../kubejs/data/creativedelight/kubejs/farmersdelight/recipes.js) (Meals section from line
258, heatless meal rule comment line 259), [brewinandchewin/recipes.js](../../kubejs/data/creativedelight/kubejs/brewinandchewin/recipes.js)
(pizza, quiche, omelet, cheesy pasta), [veggiesdelight/cooking.js](../../kubejs/data/creativedelight/kubejs/veggiesdelight/cooking.js),
[vintagedelight/cooking.js](../../kubejs/data/creativedelight/kubejs/vintagedelight/cooking.js),
[creativeengineering/recipes.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/recipes.js) (burgers, line 54),
[creativeengineering/baking.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/baking.js) (oven). Food values:
CD startup `development/creativeDelight/src/main/resources/kubejs_startup/<mod>/<mod>_foods.js` (pattern:
`alexsmobs/alexsmobs_foods.js`). Food tooltips and names: `kubejs/assets/creativedelight/kubejs/<mod>/client.js`
(the pack's `tooltips.js` Food section moved there 2026-10-27). The Brewin' port is a Creative Brewing member,
`development/creativeBrewing/members/brewin` (was `development/backportedBrewin`); build through `:creativeBrewing:`.
Farmer's Delight 1.2.4, Ocean's, Oceanic, End's, Delightful and Rustic 1.7.0 are nested in `mods/creativedelight-1.0.0.jar`
(`META-INF/jarjar/`), not in `mods/` on their own.

**Shared rules, built 2026-10-21 (rows 1-4, 7-9):** the heatless meal rule (a pot meal is also a shapeless craft from
cooked forms, one more main, id ending `_manual_only` so Create's unheated basin and mechanical crafters skip it) and the
tag keys `i.t_bread`, `i.t_tortilla`, `i.t_cooking_oil`, `i.t_sliced_potato`, `i.t_soup_veggie_cooked`, `i.t_exotic_fruit`,
`i.t_fruit_salad_slice` (item_dict.js lines 1949-2037). Every new shapeless meal below follows the rule; the writeup is
[recipes_cooking.md](../recipes_cooking.md) 2.3.

## Scope and order

| Row | Change | Status | Group |
|---|---|---|---|
| `dishes/meals #5` | Feasts: assemblies, pizza 8 slices | In Progress (JEI tab shipped 1.1.01) | C |
| `dishes/meals #6` | Glistering melon: gilding routes | In Progress (edible shipped 1.1.01) | A |
| `dishes/meals #10` | Shrimp fried rice: tooltip, JEI note | In Progress (pot recipe and values built 2026-10-29, scratch PASSED r1031) | A |
| `dishes/meals #12` | Pancake food type | In Progress | B |
| `dishes/meals #13` | Skewer food type | In Progress | B |
| `dishes/meals #14` | Patty food type (or tag) | In Progress | B |
| `dishes/meals #15` | Wrap food type (salad type exists) | In Progress | B |
| `dishes/meals #18` | Cheese in nachos | In Progress | A |
| `dishes/meals #20` | Feasts in ovens | In Progress | C |
| `dishes/meals #21` | Meal-specific ingredients audit | In Progress | D |
| `dishes/meals #23` | Fried egg and omelette batches | In Progress | A |
| `dishes/meals #24` | Extra veggie omelette recipe | In Progress | A |
| `dishes/meals #27` | Tropical Pizza | In Progress | E (Java) |
| `dishes/meals #29` | Fermented veg in uncooked crafts | In Progress | D |
| `dishes/meals #31` | Nut butter sandwich | In Progress | F (after `merges/dishes #7`) |
| `dishes/meals #33` | Creamy Gnocchi: textures, potato dough | In Progress (names and recipes shipped 1.1.03) | E |
| `dishes/meals #34` | Fries from any potato slice | In Progress | F (after `dishes/ingredients #20`) |
| `dishes/meals #35` | Steak and broccoli sides | In Progress | A |
| `dishes/meals #37` | Pancake coverings review | In Progress | E (after `dishes/jams #1`) |
| `dishes/meals #40` | New sushi roll medleys | Planned | E (Java) |
| `dishes/meals #44` | One stuffed pumpkin recipe | Planned | A |
| `dishes/meals #45` | Broccoli soup: milk to cheese | Planned | A |
| `dishes/meals #46` | Skewer fold into the Barbeque's Delight system | Planned | G (after the port and `food/seasoning #11`) |
| `dishes/meals #47` | Kebab Wrap on a tortilla | Planned | G (after the port) |
| `dishes/meals #48` | Kebab Sandwich and Bibimbap by pack rules | Planned | G (after the port) |
| `dishes/meals #49` | Decide: smelt or smoke raw skewers | Planned | G (decision only) |
| `dishes/meals #50` | Raw versions of the 8 pack skewers | Planned | G (after the port) |
| `merges/dishes #5` | Cheesy pastas: bone broth in the heatless craft | In Progress (rename built 2026-10-30) | A |
| `merges/dishes #6` | Merge Caesar salads | Planned | A |
| `merges/dishes #8` | Supreme Pizza shape, quiche family, nutrients | In Progress (rename and recipe shipped 1.1.03) | A (with #5's pizza cost) |
| `merges/dishes #10` | Loaded Burger: drop More Delight's, any cheese, burger audit | In Progress (rename built 2026-10-30) | A, then D |
| `textures/food #2` | Retexture cauliflower soup | Planned | E (art) |
| `merges/dishes #4` | Merge potato salad | Planned | owned by [merges_dishes.md](merges_dishes.md); old section kept below |

Order: **A** (recipe-only KubeJS in the CD trees, no dependencies; one pass) -> **B** (one pass over
`kubejs/generation_scripts/food/food_types.json`, then `/dumpfood` and `python3 tools/food_catalog.py`) -> **D** (audits,
after A so they see the new recipes) -> **C** (feast assemblies and oven; the pizza servings are a Creative Brewing jar
change) -> **E** (new blocks and art) -> **F** when their blockers land -> **G** after `largecontent/mod-additions #37`
(the Barbeque's Delight port, nothing built yet: no `barbequesdelight` jar, member or dict key on 2026-10-31).

---

## meals #5: Feasts
Status: In Progress (JEI "Feast Plating" tab shipped 1.1.01, RC-65; assemblies and pizza servings open) / Work: KubeJS + Creative Brewing jar (pizza) / id `dishes/meals #5`

### Goal
(a) JEI plating tab: **done** (patcherMod `compat/jei/FeastPlatingJEI.java`, `FeastPlatingCategory.java`; every FD
feast block, popcorn box, pizza, stuffed hoglin, leftovers from the loot table). (b) Sequenced assembly for roast chicken,
honey glazed ham, pizza, popcorn (popcorn box also shaped); shepherd's pie is done (row 3). (c) Pizza: 8 slices to match
its texture, dearer dough and cheese.

### Now
- Plating works in game: FD `FeastBlock.takeServing` gives the serving when the held stack matches the serving's
  crafting remainder (a bowl), 4 servings, then leftovers.
- Feast recipes (FD 1.2.4, all shapeless): `farmersdelight:roast_chicken_block` (onion, eggs, bread, carrot x2, cooked
  chicken, baked potato x2, bowl; its bread is `i.t_bread` since `farmersdelight/recipes.js` line 16),
  `farmersdelight:honey_glazed_ham_block` (sweet berries x4, honey bottle, smoked ham, cooked rice x2, bowl). Popcorn:
  `corn_delight:popcorn_box` shapeless (caramel popcorn x4, paper). Shepherd's pie assembly: `farmersdelight/recipes.js`
  lines 331-340 (base `i.bowl`). No assembly for the other four exists (grep 2026-10-31).
- Pizza: Supreme Pizza (`i.pizza`, `brewinandchewin:pizza`), `brewinandchewin/recipes.js` line 36-37:
  `['bfo', 'mtc', ' d ']` (bell pepper, cheese wedge, onion, mushrooms, tomato sauce, cooked meat, 1 dough), id
  `creativedelight:shaped/pizza`. The port's `PizzaBlock`
  (`development/creativeBrewing/members/brewin/src/main/java/umpaz/brewinandchewin/common/block/PizzaBlock.java` line 29)
  still has `SERVINGS` 0-3 (4 slices); `BnCSlicingModifier` (`forge/loot/BnCSlicingModifier.java`) drops the rest on a knife
  break. Slice values `BnCFoods.PIZZA_SLICE` 5 / 1.0 (line 143).

### Build
1. **Assemblies (b)** in `farmersdelight/recipes.js` Meals (roast chicken, ham) and `corn_delight/recipes.js` (popcorn),
   `brewinandchewin/recipes.js` (pizza), all `R.createSequencedAssembly` with `R.incomplete(<key>)`; transition rows
   `['roast_chicken']` texture `farmersdelight:item/roast_chicken_block`, `['honey_glazed_ham']`
   `farmersdelight:item/honey_glazed_ham_block`, `['pizza']` (sprite `brewinandchewin:item/pizza`),
   `['popcorn_box', 'corn_delight:item/caramel_popcorn']`:
   - Roast chicken: base `i.bowl`; deploy cooked chicken, baked potato x2, carrot x2, `i.t_bread`, onion, egg.
   - Honey glazed ham: base `i.bowl`; deploy smoked ham, cooked rice x2, spout `Fluid.of(f.honey, 250)`, sweet berries x4.
   - Pizza: base `i.dough`; spout `Fluid.of(f.tomato_sauce, 250)`, deploy the Supreme toppings in the grid's order.
   - Popcorn box: base `i.paper`; deploy caramel popcorn, 4 loops; grid becomes shaped: remove `corn_delight:popcorn_box`,
     `R.shaped(i.popcorn_box, ['C C', 'CPC'], { C: i.caramel_popcorn, P: i.paper }, e, 'popcorn_box')`.
   First steps must differ from every other bowl / dough / paper assembly (Create picks one by first step): cooked
   chicken and smoked ham on a bowl are free; check `/dumprecipes` `create:sequenced_assembly` for `i.dough` bases.
2. **Pizza 8 slices (c)** (Creative Brewing member `brewin`): `SERVINGS` 0-7, 8 `SHAPES`, models `pizza_slice1..7` and
   blockstate entries, `takeServing` unchanged, `BnCSlicingModifier` drops `servings + 1`. Pack side in
   `brewinandchewin/recipes.js`: remove `brewinandchewin:cutting/pizza` and `brewinandchewin:pizza_from_slices`, then
   `R.fdCutting(i.pizza, i.any_knife, Item.of(i.pizza_slice, 8), e, 'pizza')` and
   `R.shaped(i.pizza, ['SSS', 'S S', 'SSS'], { S: i.pizza_slice }, e, 'pizza_from_slices')`. Slice values roughly halve
   (CD startup `brewinandchewin/brewin_foods.js`, new). The dearer grid is `merges/dishes #8`'s shape. Note the change
   in [creative_brewing.md](../creative_brewing.md) (the Brewin' port notes).

### Ids and data
Keys exist: `i.roast_chicken_block`, `i.honey_glazed_ham_block`, `i.pizza`, `i.pizza_slice`, `i.popcorn_box`,
`i.caramel_popcorn`, `i.smoked_ham`, `i.paper`, `i.dough`, `i.any_knife`, `i.bowl`. Placed feasts for BlockRunner:
`kubejs/server_scripts/tags/blocktags.js` lines 384-390.

### Decisions
- Pizza: 8 servings in the port (recommend; matches the texture) vs 4 servings that each drop 2 slices.
- Vintage's cheese and meat pizzas (`backportedVintage` `PizzaBlock`) follow the same 8 later, or stay 4: recommend the
  same change in the same session so all pizzas agree.

### Gotchas
- A feast assembly outputs the block item; fine in Create.
- The pizza change is a Creative Brewing jar rebuild and install (client and server); never while the game runs.

### Tests
- RC-43 Each feast assembly runs and gives the block; popcorn box is shaped only.
- RC-44 Pizza: 8 slices by hand and by knife; the placed pizza shows 8 stages; 8 slices craft back to a pizza.

---

## meals #6: Glistering melon edible
Status: In Progress (edible shipped 1.1.01, FO-29, server part scratch PASSED 2026-10-22 SQ-OF-12; gilding left) / Work: KubeJS / id `dishes/meals #6`

### Goal
Heated "gilding" routes for `minecraft:glistering_melon_slice`, like the golden carrot.

### Now
- Edible: CD startup `minecraft/food_values.js` line 8 (`glistering_melon_slice: { from: 'golden_melon_slice', effects: false }`:
  4 / 1.2, always edible, no effects), type `golden` ([food.md](../food.md) 3.10).
- Gilding pattern: golden carrot heated mixing with 6 nuggets
  ([create/golden.js](../../kubejs/data/creativedelight/kubejs/create/golden.js) line 19), arc furnace with 5 nuggets
  ([immersiveengineering/golden.js](../../kubejs/data/creativedelight/kubejs/immersiveengineering/golden.js) line 16), from
  row 9's numbers (machines 6 and 5 of the grid's 8). Vanilla's grid (melon slice in 8 nuggets) already fits the rule.
- No glistering melon recipe in either file (grep 2026-10-31).

### Build
1. `create/golden.js`, after the golden carrot:
   `R.createMixing(i.glistering_melon_slice, [i.melon, Ingredient.of([i.any_nugget_gold]).withCount(6)], e, 'glistering_melon_gilding', 'heated')`.
2. `immersiveengineering/golden.js`: `R.ieArcFurnace(i.glistering_melon_slice, i.melon, e, 'glistering_melon_arc_gilding', [], [nugget x5])`
   (list `i.any_nugget_gold` five times: NCH's `ieArcFurnace` drops counts).

### Ids and data
`i.glistering_melon_slice` (item_dict line 2378), `i.melon` (`minecraft:melon_slice`), `i.any_nugget_gold` (`#forge:nuggets/gold`, holds rose gold).

### Decisions
None open (numbers follow row 9; `food/types-and-tuning #3` may move them for every golden food at once).

### Gotchas
Glistering melon stays a brewing ingredient; the machine routes make brewing cheaper too (accepted with the golden carrot).

### Tests
- RC-45 Heated mixer melon slice + 6 gold nuggets -> 1; arc furnace melon slice + 5 nuggets -> 1; brewing still works.

---

## meals #10: Shrimp fried rice
Status: In Progress (pot recipe and values built 2026-10-29, unreleased; scratch PASSED r1031 2026-10-31: SQ-CM-21, SQ-CM-23) / Work: KubeJS / id `dishes/meals #10`

### Goal
`alexsmobs:shrimp_fried_rice` with the tooltip "You're telling me a shrimp fried this rice?", a JEI note for the mantis
shrimp route, and a balance check of that route.

### Now
- Built: pot Fried Rice + 2 `#forge:shrimps` -> 1, 1.0 xp, 200 ticks, no container
  ([alexsmobs/recipes.js](../../kubejs/data/creativedelight/kubejs/alexsmobs/recipes.js) line 16, id
  `creativedelight:farmersdelightcooking/shrimp_fried_rice`, only with the Oceanic member on); values 20 / 0.6, Comfort
  240 s (CD startup `alexsmobs/alexsmobs_foods.js`). RC-163.
- Alex's Mobs' own route (`MantisShrimpAIFryRice`, verified 2026-10-21): a tamed, standing mantis shrimp holding
  `#alexsmobs:shrimp_rice_fryables` (= `#forge:crops/rice`) punches a furnace-type block and after 200 ticks swaps the
  whole held stack for the same count of shrimp fried rice. So 1 raw rice -> one 20 / 24 meal.
- No tooltip or JEI page for it (grep 2026-10-31). Food tooltips live in `kubejs/assets/creativedelight/kubejs/<mod>/client.js`;
  there is no `alexsmobs/` folder there yet. JEI pages: `JEIEvents.information`, `e.addItem(id, [Text.of(...)])`, gated
  on `global.CD.qol('jeiInfoPages')` (pattern `endersdelight/client.js` lines 18-26).

### Build
1. New `kubejs/assets/creativedelight/kubejs/alexsmobs/client.js` (`// requires: alexsmobs`): `ItemEvents.tooltip`
   adding "You're telling me a shrimp fried this rice?" (gray italic, flavour) to `i.shrimp_fried_rice`; JEI page "Hand
   rice to a tamed Mantis Shrimp next to a furnace: it fries the whole stack." on the same item.
2. Balance (decision below), then the changelog bullet under the existing shrimp fried rice bullet's family.

### Ids and data
`i.shrimp_fried_rice`, `i.fried_rice`, `i.t_shrimps`; tag `#alexsmobs:shrimp_rice_fryables`.

### Decisions
- Mantis route gives a full meal per raw rice (the pot needs Fried Rice + 2 shrimp). Recommend: keep it (tame-and-cook
  reward, slow: one shrimp, 200 ticks per stack); if it proves too cheap, a LootJS-free fix is to point
  `#alexsmobs:shrimp_rice_fryables` at `i.fried_rice` instead of raw rice (tag edit through `global.tags`), so the shrimp
  only adds the shrimp step.

### Gotchas
The shrimp converts the whole held stack at once.

### Tests
- RC-49 Tame a mantis shrimp, give it rice, stand it by a furnace: the stack turns into shrimp fried rice; the tooltip
  line and the JEI page show.

---

## meals #11: Merge potato salad
Status: Planned / Work: KubeJS / TODO `merges/dishes #4` (scratchpad 2026-10-22, repeated 2026-10-24)

Moved 2026-10-31: the row's brief is [merges_dishes.md](merges_dishes.md); this old section is kept until that one has
the facts. 2026-10-31: Oceanic's `oceanic_delight:potato_salad` now has a dict key (Oceanic is a CD member).

### Goal
Merge the potato salad items into one, through `global.itemMerges` and the helpers.

### Now
Three exist (jars): Rustic Delight `rusticdelight:potato_salad` (`i.potato_salad`; shapeless eggs, milk, onion, potato,
bowl), More Delight `moredelight:potato_salad` (`i.moredelight_potato_salad`; pot: eggs, `#forge:sliced_potato`,
onion) and Oceanic Delight `oceanic_delight:potato_salad` (cabbage, sea pickle, potato, bowl; not in `item_dict` yet).
The diced potato merge ([item_merges.js](../../kubejs/startup_scripts/_lib/item_merges.js)) already unified the
potato slice ingredient. Listed in the pinned marked-merge notes only for Mashed Potatoes, not potato salad.

### Decisions
Survivor: recommend Rustic's (it has the pack's cooking-fluid egg route and a can parity row; check
`can_parity.md`). Fold the other two recipes in as alternatives (sliced potatoes; sea pickle variant), not as items.

---

## meals #12: Pancake food type
Status: In Progress / Work: KubeJS / data / id `dishes/meals #12` ([food_families.md](../food_families.md) 3.1 Dessert / Pastry rows)

### Goal
A pancake food type, sweet potato pancakes placeable, and a decision on which pancakes survive (the TODO row folds in the
Autumnity leftover "remove or integrate pancakes").

### Now
- Food types: [food_types.json](../../kubejs/generation_scripts/food/food_types.json) `types`, one type per food, `/` nests
  (`sweet` holds 220 foods); `kubejs/data/creativedelight/kubejs/creativeengineering/food_type_tags.js` turns each into
  `#creativeengineering:food_type/<type>` (nested types also in their parents). No pancake type; the family is
  `dessert/pastry` in food_families.md.
- Pancakes in the pack (2026-10-31):
  - Supplementaries `supplementaries:pancake` (`i.pancakes`, renamed Stackable Pancake, `namechanges.js` line 300,
    `merges/baking #1`): a placeable stack block; recipes in
    [supplementaries/recipes.js](../../kubejs/data/creativedelight/kubejs/supplementaries/recipes.js) lines 17-32 (grid 3,
    mixer 5, egg-solution mixer 5). Not in `food_types.json` (block item, eaten placed).
  - Veggies Delight `veggiesdelight:sweet_potato_pancakes` (`i.sweet_potato_pancakes`, type `sweet`): pot sweet potato
    dough + honey bottle + milk, bowl (`veggiesdelight:cooking/sweet_potato_pancakes`). Not in any placeable tag.
  - Rustic Delight 1.7.0 port (CD member `rustic`): 7 pancake stack blocks (`pancakes`, `honey_`, `chocolate_`,
    `cherry_blossom_`, `vegetable_`, `pumpkin_`, `coffee_pancakes`) and 7 serving items (`pancake`, `honey_pancake`...),
    `PancakeStackBlock` ([rustic_delight.md](../rustic_delight.md) 5). **None has an item_dict key** and none is in
    `food_types.json` (the 2026-10-30 dumps list the recipes, e.g. `rusticdelight:cherry_blossom_pancakes`).
  - Autumnity is not in the pack (no jar, no port, no dict ids): its pancakes need nothing here.
- Related rows: `creativedelight/members #15` (Rustic pancake rework, evaluate against Supplementaries), `merges/baking #1`
  (stackable pancakes recolour), `dishes/meals #37` (coverings).

### Build
1. Dict keys for the 14 Rustic pancake ids in the Rustic block of `item_dict.js` (or CD startup `rusticdelight/`), then
   `/dumpall` and `python3 tools/food_catalog.py` so they get types.
2. New type `sweet/pancake` in `food_types.json`: move `sweet_potato_pancakes` and the 7 Rustic servings into it (the
   stacks are block items, in the type only if they are edible as items). Run `/cdevgen run food_catalog` to check.
3. Sweet potato pancakes placeable: add the key to the Food Plate list through `tools/placeable_catalog.py` rules (the
   generated `kubejs/generation_scripts/food/placeable.json`, read by `creativeengineering/placeable_tags.js`), not by hand.
   Alternative: make it a Rustic-style stack (Java), not recommended now.
4. Changelog bullet (food types are not player-visible unless a recipe or tooltip reads them; the placeable line is).

### Ids and data
`i.pancakes`, `i.pancake_disc`, `i.sweet_potato_pancakes` exist; Rustic pancakes (`rusticdelight:*pancake*`) need keys.
Tag `#creativeengineering:food_type/sweet/pancake` (new).

### Decisions
- Which pancakes survive: recommend all three lines stay (Supplementaries = the stackable topping block, Rustic = the
  flavoured stacks, Veggies = a bowl dish); settle overlaps in `creativedelight/members #15`, not here.
- Type name `sweet/pancake` (recommend, nests under sweet for tuning) vs a top-level `pancake`.

### Gotchas
A Rustic stack block takes single pancakes back (sneak-use, up to 12); placeable tags must not double-claim it.

### Tests
- FO-new1 `/dumpfood` lists the pancake type with sweet potato pancakes and the Rustic servings; `#creativeengineering:food_type/sweet/pancake` holds them.
- FO-new2 Sweet potato pancakes can be placed on a Food Plate and show "Placeable on plate".

---

## meals #13: Skewer food type
Status: In Progress / Work: KubeJS / data / id `dishes/meals #13` ([food_families.md](../food_families.md) 3.1 Skewer row)

### Goal
A skewer food type covering the pack's skewers, with family recipes (shaped plus one sequenced assembly, stick kept).

### Now
- Skewers sit in `handheld` in `food_types.json` (line 22): `bbq_stick` (FD Barbecue on a Stick), `end_barbecue_stick`
  (End's), `improvised_barbecue_stick` (Miner's), `nether_skewer` (Nether's), `seafood_skewer` (Oceanic),
  `turnip_mutton_skewer` (Veggies), `caveskewer` (Hostile), `crispy_skewer` (Ender's), plus `tentacle_on_a_stick`,
  `baked_tentacle_on_a_stick` (Ocean's) and `tentacles_on_a_stick` (Miner's). The `skewer` family has 11 members, 6 of
  them on the rule (food_families.md 3.2 line 291); Crispy Skewer is the rule's example (shaped `_manual_only` plus one
  assembly, recipes_cooking.md 2.1).
- Barbeque's Delight (10 raw, 10 cooked, burnt) is not ported yet; owner O1 (barbeques_delight.md 0.1): its skewers do
  **not** join the type or family until `dishes/meals #46`.

### Build
1. New type `handheld/skewer` in `food_types.json`: move the 11 keys above (hawberry stick and marshmallow sticks stay
   `sweet`). `/cdevgen run food_catalog`.
2. Recipes by the family rule, per skewer not yet on it (food_families.md 3.4 "Skewer" table lists which): shaped
   `R.shaped(..., e, '<key>_manual_only')` with the stick, one `R.createSequencedAssembly` on `i.stick` (first step must
   be unique per skewer: deploy the main meat), in the CD tree of the skewer's mod.
3. Saltable: add the type to the seasonable set (`food/seasoning` rules, seasonings.md) only if the owner wants skewers
   seasonable before the Barbeque's fold; recommend waiting for `food/seasoning #11`.

### Ids and data
Keys above exist (item_dict lines 1792, 12039, 12340, 12547, 12687, 12698, 12716, 14321, 14608, 14626, 14685).

### Decisions
- Which foods: recommend the 11 above; Barbeque's skewers join in `#46`.
- Recipes now or with `#46`: recommend the type now (data only) and the family recipes in `#46`'s pass, since the fold
  adds raw versions and grill recipes to the same 8 items.

### Tests
- FO-new3 `#creativeengineering:food_type/handheld/skewer` holds the 11 skewers and they are still in `handheld`.

---

## meals #14: Patty food type
Status: In Progress / Work: KubeJS / data / id `dishes/meals #14` ([food_families.md](../food_families.md) 3.1)

### Goal
One patty group so recipes take any patty ("every patty type is always interchangeable", [item_merges.md](../item_merges.md) 5.1).

### Now
- Patties (food_types.json): cooked `beef_patty` (FD, `cooked/meat`), `bison_patty` (Alex's Delight, `cooked/meat`),
  `cooked_vegetarian_patty` (Veggies, `cooked/veggie`), `vegan_patty` (Miner's, `cooked/veggie/sliced`); raw
  `raw_vegetarian_patty` (`raw/veggie`), `guardianpatty` (Hostile, `raw/fish/sliced`).
- Recipes name patties one by one: the burger table in `creativeengineering/recipes.js` lines 70-85 (`i.beef_patty`,
  `i.bison_patty`), Vintage's burgers `vintagedelight/recipes.js` line 115 (`vtdPatty = [i.beef_patty, i.cooked_vegetarian_patty]`),
  the stuffed burrito (`vintagedelight/cooking.js` line 23, `i.beef_patty`), pasta with meatballs
  (`farmersdelight/recipes.js` line 278, 2 `i.beef_patty`).
- A food type cannot hold them: food types are by food group (meat, veggie, fish), one type per food.

### Build
1. Tag `#creativeengineering:patty/cooked` (key `i.t_patty_cooked`) via `global.tags` in
   `creativeengineering/tags.js`: beef, bison, cooked vegetarian, vegan patty (and the cooked guardian patty if Hostile has
   one: check the jar). Optional `#creativeengineering:patty/raw`.
2. Replace the single-patty slots with `i.t_patty_cooked` in the recipes above, keeping the named burgers that are the
   dish (bison burger keeps bison, the vegetarian burger keeps the vegetarian patty: decision below).
3. Family: the patty members stay in their food groups; add a "patty" note row in food_families.md only if the owner
   wants a subfamily (`meat/cooked_cut` already says "patty piece").

### Ids and data
`i.beef_patty` (line 586), `i.bison_patty` (585), `i.cooked_vegetarian_patty` (14305), `i.vegan_patty` (14599),
`i.raw_vegetarian_patty` (14304), `i.guardianpatty` (12575).

### Decisions
- Food type vs tag: recommend the tag (the row says "food type", but a type would pull meat and veggie patties out of
  their groups and break tuning); answer before building.
- Which recipes open up: recommend every recipe with a generic "patty" (hamburger, cheeseburger, deluxe, simple, egg,
  loaded, burrito, meatballs); bison, kangaroo and vegetarian burgers keep their own.

### Tests
- RC-new1 A hamburger and a cheeseburger craft with a vegan patty and a bison patty; the bison burger still needs bison.

---

## meals #15: Salad and wrap food types
Status: In Progress / Work: KubeJS / data / id `dishes/meals #15` ([food_families.md](../food_families.md) 3.1 Salad, Wrap rows)

### Goal
Salad type; field salad takes more ingredients; wrap type, which requires a tortilla.

### Now
- **Salad type exists**: `salad` in `food_types.json` (25 foods, since 1.0.2, [food.md](../food.md) 2.1). The field salad
  part is built (`dishes/meals #39`, Testing). What is left is the wrap half.
- Wraps (food_types `handheld`): `mutton_wrap` (FD; on `i.t_tortilla` since 1.1.01, `farmersdelight/recipes.js` lines
  294-303), `chicken_fajitas_wrap` and `vegetables_wrap` (Veggies; on a tortilla, `veggiesdelight/cooking.js` lines
  75-94), `stuffed_burrito` (Vintage; tortilla, `vintagedelight/cooking.js` lines 22-31), `taco` (Corn Delight,
  `corn_delight:taco` shapeless, *unverified* inputs), `vegan_wrap`, `insect_wrap` (Miner's; `miners_delight:vegan_wrap`,
  `insect_wrap`, `alt_insect_wrap`, inputs *unverified*: the port converts the donor jar in memory, so read them with
  `/dumprecipes` or JEI).
- The wrap family (`sandwich/wrap`, 6 members) says "A tortilla in place of bread" (planned) and "Shaped crafting + one
  sequenced assembly" (decision).

### Build
1. New type `handheld/wrap` in `food_types.json`: the 7 keys above (`taco` too, if the owner counts it as a wrap).
2. Tortilla rule: re-make the Miner's wraps and the taco on `i.t_tortilla` where they use bread or a leaf (CD
   `miners_delight/recipes.js`, `corn_delight/recipes.js`), each `_manual_only` plus one assembly on the tortilla with a
   unique first step (mutton wrap: cooked mutton; fajitas: chicken; vegetables wrap: greens; burrito: pepper).
3. The family's "shaped" decision is open for all sandwiches; the wraps built so far are shapeless `_manual_only`.

### Ids and data
`i.mutton_wrap`, `i.chicken_fajitas_wrap`, `i.vegetables_wrap`, `i.vegan_wrap`, `i.insect_wrap`, `i.taco`,
`i.stuffed_burrito`, `i.t_tortilla` (`#forge:tortilla` = `corn_delight:tortilla` only).

### Decisions
- Which foods are wraps: recommend the 6 tortilla or leaf wraps plus the taco; cabbage-wrapped elder guardian stays a meal.
- Shapeless (as built) or shaped: recommend shapeless stays for wraps (the assembly is the automation) until the
  sandwich family decision is answered.

### Tests
- FO-new4 `#creativeengineering:food_type/handheld/wrap` holds the wraps.
- RC-new2 Every wrap needs a tortilla (JEI uses of bread show no wrap).

---

## meals #18: Cheese in nachos
Status: In Progress / Work: KubeJS / id `dishes/meals #18` ([food_families.md](../food_families.md) 3.1 Snack row)

### Goal
Nachos take cheese.

### Now
`corn_delight:cooking/nachos_block` (Corn Delight 1.0.3 jar, verified): pot 2 tortilla chips, any cooked meat
(chicken, pork, beef or mutton tag), `#forge:milk`, tomato sauce, bowl -> `corn_delight:nachos_block` (a feast block;
serving `nachos_bowl`). No pack change to it (grep 2026-10-31); Slice and Dice copies it to a heated mixer.

### Build
In [corn_delight/recipes.js](../../kubejs/data/creativedelight/kubejs/corn_delight/recipes.js):
`R.replaceInput({ id: 'corn_delight:cooking/nachos_block' }, i.milk, i.any_cheese, e)` (Slice and Dice rebuilds its copy
from the edited pot recipe). If a heatless route is wanted: `R.shapeless(i.nachos_block, [Item.of(i.tortilla_chip, 3),
i.cooked_meat, i.any_cheese, i.tomato_sauce, i.bowl], e, 'nachos_block_cooked_manual_only')` (one more main, rule).

### Ids and data
`i.nachos_block`, `i.nachos_bowl`, `i.tortilla_chip`, `i.any_cheese` (`#forge:cheese`: flaxen wedge, cheese slice),
`i.milk`, `i.tomato_sauce` exist.

### Decisions
- Replace milk (recommend) or add a cheese alternative; the row's "recipe shape" decision: recommend the pot only, no
  heatless grid (nachos are a feast block, and feasts get their grid in `#20`).
- `#forge:cheese` vs `i.t_cheese_wedge`: recommend `#forge:cheese` (takes the cheese slice).

### Tests
- RC-new3 The nachos pot recipe takes a cheese wedge or slice, not milk.

---

## meals #20: Feasts in ovens
Status: In Progress (nothing built, grep 2026-10-31) / Work: KubeJS / id `dishes/meals #20`

### Goal
Feasts cooked in the oven, starting with the hoglin feast; then every feast in the pot and the oven. Related `#5`.

### Now
- Feast recipes: FD's roast chicken, honey glazed ham, shepherd's pie are shapeless grid crafts (`#5` Now);
  `farmersdelight:stuffed_pumpkin_block` and `corn_delight:cooking/nachos_block` are pot recipes;
  `nethersdelight:raw_stuffed_hoglin` is shaped and `nethersdelight:cooking/stuffed_hoglin` cooks it in the pot
  (`i.raw_stuffed_hoglin` -> `i.stuffed_hoglin`). Rustic's medleys and Rice Roll Royale are shapeless (Rustic member).
- Oven helper (patcherMod `neoCraftHelper.js` line 1161): `R.ceBaking(input, output, e, idOverride, { exp, time, container, tool })`,
  1-6 slots, tool = bakeware kept in the tool slot. Bakeware keys: `i.t_bakeware_baking_sheet`, `_cake_pan`, `_pie_tin`,
  `_muffin_tin` (item_dict lines 43-46); no roasting pan.
- Feast list for a loop: the feast blocks of `blocktags.js` lines 384-390 and the `feast/whole` family (food_families.md 3.1).

### Build
1. Hoglin first, in CD `nethersdelight/recipes.js` (new): `R.ceBaking([i.raw_stuffed_hoglin], i.stuffed_hoglin, e,
   'stuffed_hoglin', { exp: 1.0, time: 400, tool: i.t_bakeware_baking_sheet })`.
2. Then one `FEASTS` table per mod file (roast chicken, honey glazed ham, shepherd's pie in `farmersdelight/recipes.js`):
   `[key, inputs, container]`, each giving a pot recipe (`R.fdCooking(inputs, i[key], e, key, xp, time, container)`) and
   an oven recipe (`R.ceBaking`, same inputs, baking sheet). The shapeless grids stay (feasts are crafted cold today);
   re-id them `_manual_only` only if Create mixing of them is unwanted.
3. Oven inputs over 6 slots are trimmed by `_bakingSlots` (logged): list filler first.

### Decisions
- Which feasts: recommend every feast with a cooked main (roast chicken, ham, shepherd's pie, stuffed hoglin, stuffed
  pumpkin); not the cold ones (rice roll medley, Rice Roll Royale, pepper medleys, popcorn box).
- Tool: recommend the baking sheet (no new bakeware); a roasting pan is a new item for later.
- Raw inputs (the pot rule) vs cooked inputs: recommend raw in the pot and oven, cooked stays the grid.

### Tests
- RC-new4 Raw stuffed hoglin bakes into a stuffed hoglin in the oven with a baking sheet.
- RC-new5 Each listed feast has a pot and an oven recipe in JEI.

---

## meals #21: Meal-specific ingredients audit
Status: In Progress (audit not started) / Work: KubeJS / id `dishes/meals #21` ([recipes_cooking.md](../recipes_cooking.md))

### Goal
Audit every meal that accepts only vanilla or one specific meat, veggie or fruit. Keep it where the dish is the specific
thing (glazed salmon keeps cooked salmon); otherwise accept any. Pattern: `dishes/meals #7` (any fish).

### Now
Tag aliases to open slots with: `i.cooked_meat` (`#forge:cooked_meat`), `i.cooked_fish` (`#forge:cooked_fishes`),
`i.t_food_raw_fish`, `i.t_food_cooked_meat`, `i.t_soup_veggie_cooked`, `i.vegetables`, `i.salad_ingredients`,
`i.t_exotic_fruit`, `i.t_fruit_salad_slice`, `i.t_berries`, `i.mushrooms` (item_dict 1874-2037, 12788-12794). The
dumps of 2026-10-30 (`kubejs/exported/recipes/all.csv`, `crafting_audit.json`) are the input. Count of affected meals
not known yet.

### Build
1. Script the list: from `crafting_audit.json`, every `farmersdelight:cooking` and shapeless meal (food type `meal`,
   `soup`, `salad`) with a single-item ingredient whose item is in a raw/cooked meat, fish, veggie or fruit food type;
   write it into this section as `keep` / `open up`.
2. Per opened recipe: `R.replaceInput({ id: ... }, i.x, i.tag, e)` in the CD tree of the recipe's mod; heatless twins
   (`*_cooked_manual_only`) the same.

### Decisions
Per meal, keep or open up. Recommend keeping named dishes (glazed salmon, steak and potatoes, chicken soup) and opening
generic stews, pastas and stir-fries. Overlaps `#29` (pickled swaps) and `#35`.

### Tests
RC-120 For each opened meal a second meat or veggie works; each kept meal takes only the named one.

---

## meals #22: Overnight oats
Status: Testing (shipped 1.1.04 client; SQ-CM-06 PASSED 2026-10-28) / Work: KubeJS / TODO `dishes/meals #22` ([recipes_cooking.md](../recipes_cooking.md))

Built 2026-10-28 as the grid option, not the Pickling Jar: shapeless 3 raw oats, `#creativeengineering:fruit_salad_slice`,
`#forge:berries`, `#forge:milk`, bowl (CD `vintagedelight/recipes.js`); fold into [vintage_delight.md](../vintage_delight.md) (recipes). The plan below is history.

### Goal
Overnight oats from 3 oats, 1 fruit slice, 1 berry fruit and 1 milk.

### Now
Not a new item: `vintagedelight:overnight_oats` (`i.overnight_oats`) exists, with one recipe, a Pickling Jar
`vintagedelight:fermenting` (1200 ticks, raw oats, 2 berries, pumpkin slice, melon slice, apple, bowl; verified in the
backported jar, no milk). It has no can (`can_parity.md` row `/`).

### Build
`R.shapeless(i.overnight_oats, [Item.of(i.raw_oats, 3), i.t_fruit_salad_slice, i.t_berries, i.milk, i.bowl], e,
'overnight_oats_manual_only')` or a pot recipe with `R.fdCooking`; remove the fermenting recipe with
`R.remove({ id: 'vintagedelight:fermenting/overnight_oats_from_fermenting' }, e)` if it is replaced.

### Decisions
Grid, pot or Pickling Jar (`R.vdPickling`, the "overnight" fit, no heat); bowl missing from the row. Recommend
`R.vdPickling` with the row's inputs plus a bowl container and remove the six-ingredient recipe.

### Tests
RC-121 Overnight oats craft from the row's inputs; the old fermenting recipe is gone.

---

## meals #23: Fried egg and omelette batches
Status: In Progress (nothing built, grep 2026-10-31) / Work: KubeJS / id `dishes/meals #23` ([recipes_cooking.md](../recipes_cooking.md))

### Goal
Cooking oil plus 3 eggs makes 3 fried eggs; a cooking pot recipe turns 4 fried eggs into 4 omelettes.

### Now
Fried egg `farmersdelight:fried_egg` (`i.farmersdelight_fried_egg`; `#forge:cooked_eggs` = `i.cooked_eggs` holds only it):
smelting / smoking / campfire in [farmersdelight/recipes.js](../../kubejs/data/creativedelight/kubejs/farmersdelight/recipes.js)
lines 367-368 and the egg-to-fried-egg ids; Rustic's pot `rusticdelight:fried_egg_from_cooking_oil` (oil + 1 egg -> 1;
Slice and Dice copy `sliceanddice:cooking/rusticdelight/fried_egg_from_cooking_oil`). Omelette `moredelight:omelette`
(`i.omelette`): `moredelight:omelette_smelting` / `_smoking` / `_campfire_cooking` from a cooked egg, 1 : 1. Oil
`i.t_cooking_oil` (`#rusticdelight:cooking_oil`).

### Build
In CD `rusticdelight/recipes.js` (the oil route) and `moredelight/recipes.js` (omelette):
`R.remove({ id: 'rusticdelight:fried_egg_from_cooking_oil' }, e)`;
`R.fdCooking([i.t_cooking_oil, i.eggs, i.eggs, i.eggs], Item.of(i.farmersdelight_fried_egg, 3), e, 'fried_egg_batch', 0.35, 100)`;
`R.fdCooking([i.cooked_eggs, i.cooked_eggs, i.cooked_eggs, i.cooked_eggs], Item.of(i.omelette, 4), e, 'omelette_batch', 0.35, 100)`.
Slice and Dice copies pot recipes into heated mixers (fine: both are heated).

### Decisions
Remove the single-egg pot recipe: recommend yes (the batch replaces it; the furnace keeps 1 : 1).

### Gotchas
Does the cooking oil bottle come back from the pot? Not checked for the fries either (recipes_cooking.md 2.3 note).

### Tests
RC-122 Oil + 3 eggs -> 3 fried eggs. RC-123 4 fried eggs -> 4 omelettes in the pot.

---

## meals #24: Extra veggie omelette recipe
Status: In Progress (nothing built) / Work: KubeJS / id `dishes/meals #24` ([recipes_cooking.md](../recipes_cooking.md))

### Goal
A veggie omelette recipe where one ingredient is swapped for tomato, beetroot, any bell pepper or ghost pepper.

### Now
`brewinandchewin:vegetable_omelet` (`i.vegetable_omelet`), all in
[brewinandchewin/recipes.js](../../kubejs/data/creativedelight/kubejs/brewinandchewin/recipes.js): the pot
`brewinandchewin:cooking/vegetable_omelet` (cheese wedge, carrot, onion, eggs made `#forge:cooked_eggs` by
`R.replaceInput`, line 86, bowl), the grid fold of 2 omelettes + cheese + onion + carrot + bowl (line 87), and the heated
egg-solution mixing (line 17). Bell pepper `i.any_bell_pepper` (`#forge:crops/bell_pepper`, 9 Rustic colours), ghost
pepper `i.ghost_pepper`, `i.tomato`, `i.beetroot` (crop tags).

### Build
A shared slot `var omeletVeg = [i.carrot, i.tomato, i.beetroot, i.any_bell_pepper, i.ghost_pepper]` in place of the
carrot in all three recipes: the pot through `R.replaceInput({ id: 'brewinandchewin:cooking/vegetable_omelet' }, i.carrot,
omeletVeg, e)`, the fold and the mixing by editing their lines. One recipe each, JEI cycles the options.

### Decisions
Which slot: recommend the carrot slot (as above), widened rather than a second recipe. Confirm with the owner (the note
is terse).

### Tests
RC-124 Each swap works in the pot, the grid fold and the mixer and gives the same omelet.

---

## textures/food #2: Retexture cauliflower soup
Status: Planned / Work: resource pack / id `textures/food #2` (was `dishes/meals #25`; [veggies_delight.md](../veggies_delight.md))

### Goal
New texture for Veggies Delight's cauliflower soup.

### Now
Texture `development/backportedVeggies/src/main/resources/assets/veggiesdelight/textures/item/cauliflower_soup.png`
(in-tree port, `mods/backportedVeggies-1.9.2-ce-19.2-03.jar`). No `kubejs/assets/veggiesdelight/` folder yet; the
precedent for a port sprite override is `kubejs/assets/vintagedelight/textures/item/cucumber_noodles.png` (`#43`). No
Display Delight placed model for it (grep `backportedDisplay` 2026-10-31); the can uses the generated can sprite.

### Build
Draw it (palette swap tools in `textureswapper/`); ship as `kubejs/assets/veggiesdelight/textures/item/cauliflower_soup.png`
(resource reload, no jar). Changelog bullet (visible).

### Decisions
Override in `kubejs/assets` (recommend) or replace the port PNG (rebuild `backportedVeggies`).

### Tests
RC-125 The soup shows the new texture in the inventory and JEI.

---

## merges/dishes #8: Pizza and quiche
Status: In Progress (Supreme Pizza rename and recipe shipped 1.1.03) / Work: KubeJS / id `merges/dishes #8` (was `dishes/meals #26`); related `baking/baked-goods #19`, `#20`, `dishes/meals #5`

### Goal
Owner 2026-10-27 ([item_merges.md](../item_merges.md) 5.1): no overlap to merge. Brewin's pizza is Supreme Pizza, its
craft rebuilt with dough, sauce, dough on the bottom row; the quiche food family exists; best nutrients.

### Now
- Done: name (`namechanges.js` lines 65-66, block and slice), recipe `brewinandchewin/recipes.js` lines 33-37 (moved from
  `cooking.js` 2026-10-31): `['bfo', 'mtc', ' d ']`, one dough, sauce in the middle. Vintage's `cheese_pizza`,
  `meat_pizza` and Veggies' `vegan_pizza` stay the simple ones.
- Quiche family: `quiche` and `quiche/slice` exist in food_families.md 3.1 (rows 297-299; Brewin's quiche and Vintage's
  zucchini quiche). `baking/baked-goods #19` (quiche baking type) is its own row.
- Values: Supreme slice 5 / 1.0 (`BnCFoods.PIZZA_SLICE`), meat pizza slice 5 / 0.75, cheese 4 / 0.75 (Vintage
  `ModFoods.java` lines 216-226), vegan 5 / 0.5 (Veggies `ModFoods.java` line 100).

### Build
1. Shape: `R.shaped(i.pizza, ['bfo', 'mcf', 'dtd'], { b: i.any_bell_pepper, f: i.t_cheese_wedge, o: i.onion,
   m: i.mushrooms, c: i.cooked_meat, t: i.tomato_sauce, d: i.dough }, e, 'pizza')` (2 cheese, 2 dough: also `#5`'s
   "dearer dough and cheese"). Same id, so the old recipe is replaced.
2. Nutrients: Supreme slice above every other slice: e.g. 6 / 1.0 in a new CD startup `brewinandchewin/brewin_foods.js`
   (`setFoodProperties`), halved again if `#5`'s 8 slices land.

### Decisions
Slice values (recommend 6 / 1.0 at 4 slices; 3 / 1.0 at 8). Whether Vintage's two pizzas get the same bottom-row shape:
recommend yes for one pizza rule (`baking/baked-goods #20`).

### Tests
RC-126 Supreme Pizza crafts only from the new shape; its slice shows the new values.

---

## meals #27: Pineapple pizza
Status: In Progress (nothing built, grep "tropical" 2026-10-31) / Work: KubeJS / lang / new jar / id `dishes/meals #27` ([recipes_cooking.md](../recipes_cooking.md))

### Goal
Pineapple pizza with bacon, named **Tropical Pizza**, tooltip **"Misnomer, invented in Canada"** (owner 2026-10-30).

### Now
No such item. Pineapple slice `i.pineapple_slice` (Fruits Delight), bacon `i.cooked_bacon`. Pizzas are blocks: Vintage
`PizzaBlock` (`development/backportedVintage/src/main/java/net/ribs/vintagedelight/block/ModBlocks.java` lines 147-148,
items `item/ModItems.java` lines 104-105, slice foods `ModFoods.java` lines 216-226), Brewin's in the Creative Brewing
member. A new pizza needs a block, a block item, a slice item, models and a cutting recipe.

### Build
1. Register `tropical_pizza` + `tropical_pizza_slice` in `backportedVintage` (copy `meat_pizza`: block, item, slice food,
   blockstate, models, recoloured textures with pineapple and bacon on top), cutting board and Slice and Dice data as the
   meat pizza's. Build `:backportedVintage:`, install.
2. Dict keys `i.tropical_pizza`, `i.tropical_pizza_slice`; recipe in CD `vintagedelight/recipes.js` with the pizza rule
   (`merges/dishes #8` shape): `['pcp', 'bfb', 'dtd']` pineapple slice, cheese, cooked bacon, dough, tomato sauce.
3. Name and tooltip: `kubejs/assets/creativedelight/kubejs/vintagedelight/client.js` (new): lang "Tropical Pizza",
   "Tropical Pizza Slice"; tooltip "Misnomer, invented in Canada" on both.
4. Food families row, placeable (own block), cans none (pizzas not canned yet, `cans/canning #17`).

### Decisions
Owner-decided name and tooltip. Namespace: Vintage port (recommend: same class and loot as its pizzas) vs a CD member.

### Tests
RC-127 Tropical Pizza crafts, places, slices (knife and Slice and Dice); name and tooltip show.

---

## merges/dishes #5: Merge cheesy pastas
Status: In Progress (rename built 2026-10-30, unreleased) / Work: KubeJS / id `merges/dishes #5` (was `dishes/meals #28`)

### Goal
Owner 2026-10-27: rename Brewin's to Cheesy Fish Pasta (done) and add bone broth to the cheesy pasta heatless craft. With
the rename the two dishes are distinct (fish pasta vs cheese pasta): no item merge is left.

### Now
- Brewin `brewinandchewin:cheesy_pasta` (`i.cheesy_pasta`) renamed Cheesy Fish Pasta with its can and placed block
  (`namechanges.js` lines 296-298). Pot upstream; heatless `brewinandchewin/recipes.js` line 83: flaxen wedge, pasta,
  tomato sauce, 2 cooked fish (comment line 82: "Pastas take no broth").
- Vintage `vintagedelight:cheese_pasta` (`i.cheese_pasta`, "Cheese Pasta"): pot `vintagedelight:cooking/cheese_pasta`;
  heatless `vintagedelight/cooking.js` line 34: pasta, 3 `#forge:cheese`, bowl.

### Build
Add `i.bone_broth` to the heatless craft the owner meant (decision), in place of the bowl where the dish is a soup-style
pasta: e.g. `R.shapeless(i.cheese_pasta, [i.pasta, i.any_cheese, i.any_cheese, i.any_cheese, i.bone_broth], e,
'cheese_pasta_cooked_manual_only')` (bone broth brings its bowl). Update the "Pastas take no broth" comment.

### Decisions
- Which heatless craft takes bone broth: recommend Vintage's Cheese Pasta (the plain cheese one; the fish pasta has
  tomato sauce as its liquid). Confirm.
- Close the merge: recommend renaming the row "Cheesy pastas: names and bone broth" (no `global.addItemMerges` pair).

### Tests
RC-128 Cheesy Fish Pasta and Cheese Pasta both in JEI under those names; the chosen heatless craft takes bone broth.

---

## meals #29: Fermented veg in uncooked crafts
Status: In Progress (nothing built) / Work: KubeJS / id `dishes/meals #29` ([vintage_delight.md](../vintage_delight.md)); related `dishes/pickles-and-ferments #2`

### Goal
Fermented or pickled garlic, onion and the like accepted in place of the raw one in some uncooked crafts: burgers and
the salads the note lists as "maggot potato chicken broccoli cucumber salad" (confirm). Not the field salad. More pickled
options in the cucumber salad.

### Now
- Pickled items: `i.pickled_onion`, `i.pickled_pepper`, `i.pickled_beetroot`, `i.pickle` (Vintage), `i.kimchi`
  (Brewin'); tag `i.t_pickled_veg` (`#creativeengineering:pickled_veg`, pickle soup). Garlic: only
  `i.fermented_garlic_honey` (Veggies), no pickled garlic.
- Targets: burgers `creativeengineering/recipes.js` lines 70-85 and `vintagedelight/recipes.js` lines 117-121 (onion
  slots); salads: maggot salad (`alexsdelight:maggot_salad_recipe`, `_2`), potato salad (`rusticdelight:potato_salad`,
  `moredelight:cooking/potato_salad`, `oceanic_delight:potato_salad`; the merge is `merges/dishes #4`), chicken salad
  (`moredelight:chicken_salad`), broccoli salad (`veggiesdelight/cooking.js` line 37: broccoli, tomato, onion, bowl),
  cucumber salad (`vintagedelight/cooking.js` line 22: salad ingredients, pickled beetroot, pickled onion, cucumber, bowl).

### Build
1. Key `i.onion_or_pickled = [i.onion, i.pickled_onion]` style lists in each file (or a tag
   `#creativeengineering:onion_or_pickled` via `global.tags` if more than two files use it).
2. Burgers: swap `O: i.onion` for the list in both burger tables (shaped and assembly steps).
3. Salads: `R.replaceInput` on the upstream ids (onion -> list); broccoli salad line 37 edited in place.
4. Cucumber salad: widen its two pickled slots to `i.t_pickled_veg`.

### Decisions
The dish list (confirm before building); garlic needs a pickled garlic item first (`pickles-and-ferments`), so garlic
slots stay raw for now.

### Tests
RC-129 Each listed dish takes the pickled onion and still the raw one; the cucumber salad takes any pickled veg.

---

## merges/dishes #6: Merge Caesar salads
Status: Planned / Work: KubeJS / lang / id `merges/dishes #6` (was `dishes/meals #30`)

### Goal
One Caesar Salad: correct spelling, Veggies Delight's texture, lettuce + tomato + cheese + bread (owner 2026-10-27).

### Now
- `veggiesdelight:cesar_salad` (`i.cesar_salad`, shown "Caesar Salad" via `name_rules.json` line 4; can
  `canned_cesar_salad`): upstream and compat recipes removed and replaced by
  `creativedelight:shapeless/cesar_salad_manual_only` ([veggiesdelight/cooking.js](../../kubejs/data/creativedelight/kubejs/veggiesdelight/cooking.js)
  line 38: cabbage, `i.t_bread`, garlic clove or any cheese, tomato, bowl).
- `oceanic_delight:caesar_salad` (`i.caesar_salad`, item_dict line 14656; Oceanic is a CD member since 2026-10, so the
  merge is unblocked): `oceanic_delight:caesar_salad` shapeless (cooked chicken, cabbage, milk, bread, bowl), bread made
  `i.t_bread` at `oceansdelight/recipes.js` line 14.

### Build
1. CD startup `oceanic_delight/item_merges.js` (new; or the Veggies one): `global.addItemMerges('<group>', [[i.caesar_salad, i.cesar_salad]])`
   (group per item_merges.md 1.1).
2. Recipe: lettuce = `i.salad_ingredients` (cabbage, cabbage leaf, chopped clover, dandelion leaf). Rewrite line 38 as
   `[i.salad_ingredients, i.tomato, i.any_cheese, i.t_bread, i.bowl]`; keep Oceanic's chicken version as an alternative
   (`R.replaceInput` milk -> `i.any_cheese`) or remove it (decision).
3. Lang: the display name is already right; no texture change (Veggies' survives).

### Decisions
Keep Oceanic's chicken recipe as a second route (recommend yes, with cheese for milk) or drop it.

### Tests
RC-130 One Caesar Salad in JEI; Oceanic's id turns into it in an inventory; both recipes give it.

---

## meals #31: Nut butter sandwich
Status: In Progress / Work: KubeJS / id `dishes/meals #31`; blocked on `merges/dishes #7` (nut butter / nut mash)

### Goal
Nut butter sandwich: more jam options, and only square bread.

### Now
- More jam options: **done** (`dishes/jams #3`): Vintage's `pb_j` is `creativedelight:shapeless/pb_j_manual_only`,
  `[i.t_bread, i.nut_mash_bottle, i.t_forge_jams]` ([vintagedelight/cooking.js](../../kubejs/data/creativedelight/kubejs/vintagedelight/cooking.js)
  line 21; `#forge:jams` 22 items).
- Bread slot is `i.t_bread` (`#forge:bread`, whole loaves). Square bread = `#forge:bread_slices` (`moredelight:bread_slice`,
  `moredelight:toast`).
- Delightful's `delightful:food/nut_butter_and_jelly_sandwich` still ships (a second sandwich for the same dish).
- `merges/dishes #7` (owner 2026-10-27) says "Nut butter sandwich: Vintage's texture, Delightful's name; **any bread** +
  any jam + a nut mash jar", which conflicts with this row's "only square bread".

### Build
After `merges/dishes #7`: one sandwich item (its merge pair), recipe `[2 x bread slot, i.nut_mash_bottle, i.t_forge_jams]`
shaped plus one assembly by the square sandwich rule (food_families `sandwich/square`), bread slot per the decision.

### Decisions
Square slices only (this row) vs any bread (`merges/dishes #7`): needs the owner. Recommend 2 `#forge:bread_slices` (the
sprite shows slices; `baking/baked-goods #9` plans the same for square sandwiches).

### Tests
RC-131 The sandwich takes each jam and only the chosen bread.

---

## meals #32: Remove mhadjeb
Status: Testing / Work: KubeJS / TODO `dishes/meals #32` ([recipes_cooking.md](../recipes_cooking.md)); also `baking/baked-goods #17`

Built 2026-10-25 (shipped 1.1.03; CD `veggiesdelight/veggies_items.js`, `recipes.js`); fold into [veggies_delight.md](../veggies_delight.md) 5.2.
Still open: `creativeengineering:canned_mhadjeb` is registered with `creativedelight:shapeless/canned_mhadjeb` (exports
2026-10-30): run `/dumpfood`, then `/cdevgen` (can generator), SQ-CDB-12.

### Goal
Remove the mhadjeb.

### Now
`veggiesdelight:mhadjeb` and `uncooked_mhadjeb` (`i.mhadjeb`, `i.uncooked_mhadjeb`): uncooked is shapeless dough + onion +
bell pepper + tomato sauce (plus a Vintage ghost pepper / cheese variant), cooked by smelting / smoking / campfire.
`canned_mhadjeb` is generated (`can_types.json`, `can_servings.json`, 4 servings). `veggies_delight.md` 5.2 kept it
"until doughs are unified", which this row now ends.

### Build
Add both keys to `global.removedItems` ([removed_items.js](../../kubejs/startup_scripts/_lib/removed_items.js)) (hides
in JEI, strips tags; `/recipescan` lists leftovers); drop the `canned_mhadjeb` row in
`tools/gen_can_types.py` and run `/cdevgen`; `food_types.json` follows from the generator.

### Decisions
None. Compost value and loot entries naming it need a grep.

### Tests
RC-132 No mhadjeb in JEI or the creative tab; no `canned_mhadjeb`.

---

## meals #33: Potato noodles become creamy gnocchi
Status: In Progress (names and recipes shipped 1.1.03, scratch SQ-CDB-10 PASS 2026-10-25; textures and potato dough open) / Work: KubeJS / lang / art / id `dishes/meals #33` ([veggies_delight.md](../veggies_delight.md))

### Goal
Creamy Gnocchi (milk, gnocchi, onion or garlic, either can be pickled); cutting potato dough and sweet potato dough gives gnocchi.

### Now
- Built: names Gnocchi, Creamy Gnocchi, Canned Creamy Gnocchi (`namechanges.js` lines 263-267); pot
  `R.fdCooking([i.milk, i.potato_noodle, i.potato_noodle, [i.onion, i.t_crops_garlic, i.pickled_onion]], i.potato_noodles, ...)`
  replacing `veggiesdelight:cooking/potato_noodles` ([veggiesdelight/recipes.js](../../kubejs/data/creativedelight/kubejs/veggiesdelight/recipes.js)
  lines 57-58); knife `gnocchi_from_sweet_potato_dough` (line 56, 2 gnocchi); gnocchi in `#forge:pasta`
  (`veggiesdelight/tags.js` line 12, `#38`). Vintage's cheese variant (`veggiesdelight:compat/vintagedelight/cooking/potato_noodles`) stays.
- Open: both sprites still show the noodle art; there is no potato dough item (only `i.sweet_potato_dough`; grep 2026-10-31).
  RC-133.

### Build
1. Two sprites: `kubejs/assets/veggiesdelight/textures/item/potato_noodle.png` (gnocchi pieces) and `potato_noodles.png`
   (bowl), same folder as `textures/food #2`; the can follows the generated can art.
2. Potato dough: only with `baking/baked-goods #17` (dough unification); then
   `R.fdCutting(i.potato_dough, i.any_knife, Item.of(i.potato_noodle, 2), e, 'gnocchi_from_potato_dough')`.

### Decisions
Potato dough now (new startup item) or with the dough unification: recommend with the unification.

### Tests
RC-133 Creamy Gnocchi name, recipe and new sprites; knifing sweet potato dough gives gnocchi.

---

## meals #34: Fries from any potato slice
Status: In Progress (waits for `dishes/ingredients #20`) / Work: KubeJS / id `dishes/meals #34` ([veggies_delight.md](../veggies_delight.md))

### Goal
Fries from any potato slice, sweet potato slices too.

### Now
Fries (`i.chips`, PneumaticCraft chips renamed) and fish and chips already read `i.t_sliced_potato`
([pneumaticcraft/recipes.js](../../kubejs/data/creativedelight/kubejs/pneumaticcraft/recipes.js) lines 60-64).
`#forge:sliced_potato` holds only `rusticdelight:potato_slices` (`rusticdelight/tags.js` line 21; More Delight's diced
potatoes merged into it). No sweet potato slice item yet (`dishes/ingredients #20`).

### Build
After `ingredients #20`: `t.add(e, i.t_sliced_potato, i.sweet_potato_slices)` in CD `veggiesdelight/tags.js`. Nothing else.

### Tests
RC-134 Fries from sweet potato slices; fish and chips accepts them.

---

## meals #35: Steak and broccoli sides
Status: In Progress (nothing built) / Work: KubeJS / id `dishes/meals #35` ([veggies_delight.md](../veggies_delight.md))

### Goal
Steak and broccoli accepts rice, mashed potatoes, any baked potato plus a bowl, and other baked veggies plus a bowl.

### Now
`veggiesdelight:steak_and_broccoli`: upstream shapeless (cooked beef, broccoli, cooked rice, bowl) removed
([veggiesdelight/cooking.js](../../kubejs/data/creativedelight/kubejs/veggiesdelight/cooking.js) line 23) and replaced by
`creativedelight:shapeless/steak_and_broccoli_cooked_manual_only` (line 41: cooked beef, broccoli, cooked rice; the rice
brings the bowl). Mashed: `i.mashed_potatoes` (More Delight, Mashed Potatoes) and `i.veggiesdelight_mashed_potatoes`
(Mashed Sweet Potatoes after `merges/dishes #2`); both bowl items. Baked sides: `i.t_soup_veggie_cooked` holds baked
potato, baked sweet potato, roasted zucchini and the other cooked veggies.

### Build
Two recipes in place of line 41: `[i.cooked_beef, i.broccoli, [i.cooked_rice, i.mashed_potatoes, i.veggiesdelight_mashed_potatoes]]`
(the side brings its bowl) and `[i.cooked_beef, i.broccoli, i.t_soup_veggie_cooked, i.bowl]`
(`steak_and_broccoli_baked_cooked_manual_only`).

### Decisions
None beyond the row (bowl only for baked sides).

### Tests
RC-135 Each side crafts the meal; baked sides need the bowl.

---

## meals #36: Cacciatore takes chicken cuts
Status: Testing / Work: KubeJS / TODO `dishes/meals #36` ([recipes_cooking.md](../recipes_cooking.md))

Built 2026-10-25 (nothing to build: `i.cooked_chicken` is `#forge:cooked_chicken`, which holds the cooked cuts; grid fix 4
makes the heatless recipe cycle them; SQ-CDB-11 PASS); fold into [recipes_cooking.md](../recipes_cooking.md) 2.3. The heatless
route is now in CD `veggiesdelight/cooking.js`, not `cooking.js`; the line numbers below are stale.

### Goal
Allow chicken cuts in the cacciatore; mark another whole versus cut audit.

### Now
The pot recipes (`veggiesdelight:cooking/cacciatore` and the Vintage compat file) read `#forge:raw_chicken`, and
Farmer's Delight's tag is chicken + `farmersdelight:chicken_cuts` (verified), so the pot route already takes cuts.
The heatless route cooking.js line 502 takes `Item.of(i.cooked_chicken, 2)`: whole cooked chicken only;
`i.chicken_cuts_cooked` (`farmersdelight:cooked_chicken_cuts`) is in `forge:cooked_meat` (cookingtags.js line 49).

### Build
Widen the heatless slot to `[i.cooked_chicken, Item.of(i.chicken_cuts_cooked, N)]` (cuts per whole chicken: FD cuts
2 per chicken, so 4 cuts for the 2 chickens: *unverified*). The audit is part of `meals #21`: other recipes with a
whole-meat slot that should take cuts (garlic chicken stew line 499, corn soup line 443, pad thai line 597).

### Decisions
Cut count equivalence (recommend 2 cuts = 1 whole).

### Tests
RC-136 Heatless cacciatore with cooked cuts.

---

## meals #37: Pancake coverings review
Status: In Progress (review not started) / Work: KubeJS / patcherMod / id `dishes/meals #37` ([recipes_cooking.md](../recipes_cooking.md)); after `dishes/jams #1`

### Goal
Review the Supplementaries pancake covering options, the JEI screen and the tooltip; recolour the jam covering to each
jam's colour.

### Now
- Supplementaries 2.4.20 jar: covering textures `textures/blocks/pancake_top_` / `pancake_side_` `syrup`, `honey`,
  `honey1`, `jam`, `chocolate` (one jam texture for every jam); item tag `supplementaries:pancake_syrup`.
- Pack touches: `supplementaries/recipes.js` (pancake recipes, lines 17-32), `namechanges.js` line 300 (Stackable Pancake),
  `jeed_providers.js` line 94 (speed). No JEI screen for coverings in the pack (none found); Rustic's pancake stacks carry
  their own toppings (`#12`).
- `dishes/jams #1` (one jam item and block, recoloured per jam) is In Progress.

### Build
1. Audit in game: which items apply which covering (Supplementaries code: `PancakeBlock` topping by item / tag), what JEI
   and the tooltip say; write the list here.
2. Jam colour: per-jam tint of `pancake_top_jam` / `pancake_side_jam` by a patcherMod `BlockColor` on
   `supplementaries:pancake` keyed on the stored topping item (the block entity holds it: check), colours from
   `dishes/jams #1`. Or one texture per jam if jams stay a handful (resource pack).
3. A JEI information page on `i.pancakes` listing the coverings (`JEIEvents.information`, CD `supplementaries/client.js`).

### Decisions
Tint (one mixin or colour handler, any number of jams) vs per-jam textures: recommend the tint, after `jams #1` settles
the jam set.

### Tests
RC-137 Each jam covering shows its colour; JEI page and tooltip match the real options.

---

## meals #39: Field salad revamp
Status: Testing (built 2026-10-29, unreleased; SQ-CM-20 PASSED r1031 2026-10-31) / Work: KubeJS / TODO `dishes/meals #39` ([delightful.md](../delightful.md))

Built 2026-10-29; fold into [delightful.md](../delightful.md) (recipes) or [creative_delight.md](../creative_delight.md) section 31.

### Built
Delightful's `delightful:food/field_salad` (2 `#forge:salad_ingredients`, cactus steak, carrot, salmonberries, acorn,
bowl) is replaced by two shapeless recipes in Creative Delight `delightful/recipes.js`:
- `creativedelight:shapeless/field_salad_manual_only`: 2 salad ingredients + the four slot tags + bowl.
- `creativedelight:shapeless/field_salad_flowers_manual_only`: 2 `#minecraft:flowers` in place of the salad ingredients.
Slot tags (`delightful/tags.js`, keys in item_dict): berry slot `#forge:berries` (9 berries);
`#creativeengineering:field_salad/root` = carrot, onion, beetroot, corn, turnip (crop tags where they exist);
`#creativeengineering:field_salad/nut` = `#forge:nuts/acorn`, peanut;
`#creativeengineering:field_salad/green` = cactus steak, broccoli, cauliflower, cauliflower floret, zucchini, zucchini
slice, cucumber, green bell pepper. Sliced forms in the pack: only the zucchini slice and cauliflower floret (no onion,
carrot, beetroot, corn, turnip, berry or nut slices exist); the cabbage leaf is already a salad ingredient. Food
values unchanged (10 / 0.6). `#minecraft:flowers` includes Botania's functional and mystical flowers.

### Tests
RC-160 Both field salad recipes in JEI; craft one with each slot alternative (a sweet berry, a turnip, a peanut,
a zucchini slice; then 2 dandelions in place of the leaves); the upstream recipe is gone.

---

## meals #40: New sushi roll medleys
Status: Planned / Work: new jar / KubeJS / data / id `dishes/meals #40`; related `creativedelight/members #14`, `#26`

### Goal
New roll medleys (feast platters like FD's Rice Roll Medley): hostile rolls, shrimp and sea pickle rolls, egg rolls, and
the bell pepper rolls.

### Now
- FD `farmersdelight:rice_roll_medley_block` (`i.rice_medley`): a `FeastBlock` serving salmon rolls, cod rolls and kelp
  roll slices; shapeless grid plus the pack's assembly (`farmersdelight/recipes.js` lines 228-235: bowl, cod roll, salmon
  roll, kelp roll slice -> 1).
- Rustic port (CD member `rustic`, built 2026-10-25): `RiceRollRoyaleBlock` (`members/rustic/.../content/RiceRollRoyaleBlock.java`,
  `extends FeastBlock`, 8 servings from a fixed `ROLLS` list: cherry blossom, calamari, yellow, red, green pepper rolls, 3 kelp
  roll slices), registered `rusticdelight:rice_roll_royale` (`RDBlocks.java` line 66), and **the bell pepper rolls
  already exist**: `bell_pepper_roll_<colour>` for all 9 colours (`RDItems.java` line 111). None of these has an item_dict key.
- Roll items for the new medleys (dict keys exist): Hostile `i.ghastroll`, `i.guardianroll`; Ocean's `i.elder_guardian_roll`;
  Oceanic `i.shrimp_roll`, `i.sea_pickle_roll` / `_slice`, `i.egg_roll`, `i.fish_egg_roll` / `_slice`.

### Build
1. Generalise the Royale block: a `RollMedleyBlock(List<Supplier<Item>> rolls)` in the Rustic member (or the CD
   `farmers` member), servings = list size, shape and leftovers as the Royale. Register 4 blocks + block items:
   `hostile_roll_medley` (ghast, guardian, elder guardian rolls), `ocean_roll_medley` (shrimp and sea pickle rolls and
   slices), `egg_roll_medley` (egg and fish egg rolls and slices), `bell_pepper_roll_medley` (the 9 colours or the 3 common
   ones). Models: recolours of `rice_roll_medley_block` stages; loot tables (full drops itself, as the Royale).
2. Dict keys for the 4 medleys and the Rustic rolls; recipes in the CD tree of the namespace: shapeless rolls + bowl
   (as FD's medley), plus one assembly each on `i.bowl` with a unique first roll.
3. JEI plating: the `FeastPlatingCategory` (patcherMod) picks up any `FeastBlock` subclass from the registry: check it
   lists the new ones. Food families `feast/whole`, placeable own block, compost, names.

### Ids and data
Namespace decision below; servings verified to exist only as listed above (the Hostile and Oceanic roll recipes and values
not read for this brief).

### Decisions
- Namespace: `rusticdelight:` (the Royale's home; one class) vs `creativedelight:` vs each roll's own mod (needs a CD
  additions member per ARR mod). Recommend `rusticdelight:` for the pepper medley and a `creativedelight:` namespace for
  the three cross-mod ones, since they mix mods.
- Bell pepper medley: 9 colours or 3: recommend the 3 common (green, yellow, red), like the Royale.

### Gotchas
Create picks a sequenced assembly by its first step: every bowl-based medley needs a different first roll (rice medley
starts with a cod roll).

### Tests
- RC-new6 Each medley crafts (grid and assembly), places, and serves its rolls in order with a bowl; JEI plating lists it.

---

## meals #43: Veggie Noodles
Status: Testing (built 2026-10-29, unreleased; SQ-CM-22 PASSED r1031 2026-10-31) / Work: KubeJS / resource pack / lang / TODO `dishes/meals #43` ([vintage_delight.md](../vintage_delight.md))

Built 2026-10-29; fold into [vintage_delight.md](../vintage_delight.md) (recipes, textures).

### Built
- The row's "cucumber pasta" is `vintagedelight:cucumber_noodles` (Cucumber Noodles; cut from `cucumber_dough`, in
  `#forge:pasta`). Renamed Veggie Noodles (`kubejs/client_scripts/namechanges.js`).
- Its dough recipe `vintagedelight:cucumber_dough` (cucumber + `#forge:dough` -> 2) is replaced by
  `creativedelight:shapeless/cucumber_dough`: one of `#creativeengineering:veggie_noodle_veg` (cucumber, broccoli,
  zucchini, zucchini slice, raw cactus steak = cactus flesh, dandelion leaf) + `#forge:dough` -> 2 (CD
  `vintagedelight/recipes.js`, `tags.js`). The knife and Slice and Dice cuts are unchanged.
- Sprite: `kubejs/assets/vintagedelight/textures/item/cucumber_noodles.png`, the 4 shades remapped by brightness onto
  the zucchini item's 4 lightest shades (shape and shading kept).
- Open: the dough is still named Cucumber Dough, with its own mint sprite, and the Cucumber Dough Block likewise.

### Tests
RC-162 Veggie Noodles name and olive-green sprite; the dough from each of the six vegetables; the noodles still cut
from it and still work as `#forge:pasta`.

---

## meals #44: Unify the two stuffed pumpkin recipes
Status: Planned / Work: KubeJS / id `dishes/meals #44`

### Goal
Owner decision 2026-10-30: one new recipe: tomato sauce + any raw potato + any bread + any berries + any mushroom + any veggie.

### Now
Two pot recipes make `farmersdelight:stuffed_pumpkin_block` (`i.stuffed_pumpkin_block`), both with a pumpkin container,
verified in the jars 2026-10-31:
- `farmersdelight:cooking/stuffed_pumpkin_block` (FD 1.2.4, nested in CD): `#forge:crops/rice`, `#forge:crops/onion`,
  brown mushroom, potato, `#forge:berries`, `#forge:vegetables`; 2.0 xp, 400 ticks.
- `corn_delight:cooking/stuffed_pumpkin_from_cornbread`: cornbread, `#forge:vegetables`, tomato sauce, brown mushroom
  colony, sweet berries; 0.5 xp, 400 ticks.
Slice and Dice copies both. No pack change to either (grep 2026-10-31); `can_stuffed_pumpkin` exists.

### Build
In CD `farmersdelight/recipes.js` (Meals):
```js
R.remove({ id: 'farmersdelight:cooking/stuffed_pumpkin_block' }, e)
R.remove({ id: 'corn_delight:cooking/stuffed_pumpkin_from_cornbread' }, e)   // Corn Delight is always loaded in the pack
R.fdCooking([i.tomato_sauce, i.any_potato, i.t_bread, i.t_berries, i.mushrooms, i.vegetables],
	i.stuffed_pumpkin_block, e, 'stuffed_pumpkin_block', 2.0, 400, i.pumpkin)
```
`i.any_potato` = `#forge:vegetables/potato` (potato, potato slices, sweet potato). Slice and Dice rebuilds its copy.

### Ids and data
`i.tomato_sauce`, `i.any_potato`, `i.t_bread` (loaves incl. cornbread), `i.t_berries`, `i.mushrooms` (`#forge:mushrooms`,
27 incl. Botania's), `i.vegetables`, `i.pumpkin` exist.

### Decisions
`#forge:mushrooms` includes the 16 Botania mushrooms: recommend a narrower edible set if that reads wrong (`i.brown_mushroom`,
red, and the Fungi Delight ones). Rice and onion drop out (the owner's list): confirm.

### Tests
- RC-new7 Only one stuffed pumpkin recipe in JEI (pot and its mixer copy), with the six slots; it takes a sweet potato and cornbread.

---

## meals #45: Broccoli soup: milk to cheese
Status: Planned / Work: KubeJS / id `dishes/meals #45`

### Goal
Broccoli soup takes cheese, not milk.

### Now
- Pot `veggiesdelight:cooking/broccoli_soup`: 2 `#forge:crops/broccoli` + `#forge:milk`, bowl. **A cheese version already
  exists**: `veggiesdelight:compat/vintagedelight/cooking/broccoli_soup` (2 broccoli + `#forge:cheese`, bowl; Vintage is
  loaded), both in the backportedVeggies jar and the 2026-10-30 recipe dump.
- Heatless `creativedelight:shapeless/broccoli_soup_cooked_manual_only`
  ([veggiesdelight/cooking.js](../../kubejs/data/creativedelight/kubejs/veggiesdelight/cooking.js) line 51): bone broth,
  3 broccoli, milk.

### Build
1. `R.remove({ id: 'veggiesdelight:cooking/broccoli_soup' }, e)` (the cheese pot stays).
2. Line 51: `i.milk` -> `i.any_cheese`.

### Decisions
Replace (recommend: the cheese pot already covers the dish) vs keep both. Cauliflower soup (line 52, milk) is the same
shape: change it too only if asked.

### Tests
- RC-new8 Broccoli soup: one pot recipe (cheese) and the heatless craft with cheese; no milk route.

---

## merges/dishes #10: Burger audit and loaded burger
Status: In Progress (rename built 2026-10-30, unreleased) / Work: KubeJS / data / id `merges/dishes #10`

### Goal
Owner 2026-10-27: drop More Delight's loaded burger in favour of Vintage's "The Burger", renamed Loaded Burger, which
accepts any cheese; mark every burger for an ingredient-variant audit and balancing.

### Now
- Rename done: `vintagedelight:deluxe_burger` (`i.deluxe_burger`, upstream name "The Burger") -> Loaded Burger
  (`namechanges.js` line 299).
- Its recipe ([vintagedelight/recipes.js](../../kubejs/data/creativedelight/kubejs/vintagedelight/recipes.js) lines
  108-128): shaped `['STO', 'APE', 'CB ']` with `C: i.cheese_slice` only, patty `[i.beef_patty, i.cooked_vegetarian_patty]`,
  plus an assembly on the patty.
- More Delight `moredelight:loaded_hamburger` (`i.loaded_hamburger`): pack shaped `_manual_only` + assembly in
  [creativeengineering/recipes.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/recipes.js) line 82-83.
- Burgers (14): hamburger, cheeseburger, deluxe cheeseburger, simple hamburger, hamburger with egg, loaded hamburger, bison,
  kangaroo (`creativeengineering/recipes.js` lines 70-87), Vintage cheese and Loaded (`vintagedelight/recipes.js`),
  vegetarian burger (Veggies), vegan and cave hamburger (Miner's), guardian burger (Hostile).

### Build
1. Drop: `global.addItemMerges(<group>, [[i.loaded_hamburger, i.deluxe_burger]])` in CD startup `moredelight/item_merges.js`;
   delete the `loaded_hamburger` row from the burger table (line 82-83) so no recipe makes the purged item.
2. Any cheese: `C: i.any_cheese` and the first assembly step `i.any_cheese` (keeps the slice; adds the flaxen wedge). Its
   first step then equals Vintage's cheese burger's (`i.cheese_slice`, a subset): give the Loaded Burger a different first
   step (it already starts on the patty, not the bread: fine), and check the cheese burger stays unique.
3. Audit: a table here of every burger's slots (patty, cheese, greens, bread) and values; open slots per `#14` (patty tag)
   and `#29` (pickled onion); balance in `food/types-and-tuning`.

### Decisions
Merge (inventory swap, the item disappears) vs only removing its recipe: recommend the merge (old worlds carry it).

### Tests
- MG-new1 A More Delight loaded burger in an inventory becomes a Loaded Burger; JEI hides it.
- RC-new9 The Loaded Burger crafts with a flaxen wedge and with a cheese slice.

---

## meals #46: Skewer fold: the pack's skewers join the Barbeque's Delight system
Status: Planned / Work: KubeJS / data / id `dishes/meals #46`; after `largecontent/mod-additions #37` (the port), `food/seasoning #11`, `#13`

### Goal
[barbeques_delight.md](../barbeques_delight.md) 4.1 (revised 2026-10-31): the BBQ system stays; the 8 pack skewers get raw
versions (`#50`) that the Grill cooks into the existing items; raw skewers worse food, chicken "nasty"; seasonings on raw
skewers copy to the cooked one, sauces refused; cooked skewers craftable from cooked ingredients (shaped plus one
assembly) without `grilled`; family membership, skewer type (`#13`), saltable, stack sizes, plate.

### Now
Nothing of Barbeque's Delight is in the pack (no jar, no `development/backportedBarbeques`, no dict keys, 2026-10-31).
The 8 pack skewers and their keys: `#13` Now. The overlap evaluation table (5 rows, keep both) is in TODO.

### Build (after the port)
1. Skewer type and family: add the 10 BBQ cooked skewers and the burnt skewer (`hazard` overlay) to `handheld/skewer`
   (`#13`) and the `skewer` family (`tools/food_families.py`), and the raw ones to a raw type (decision).
2. Raw items and grill recipes: `#50`.
3. Cooked-ingredient crafts for the 10 BBQ lines and the 8 pack lines: shaped `_manual_only` + one assembly on
   `i.stick`, cooked meat in place of raw (barbeques_delight.md 4.3 #8 default), in CD `barbequesdelight/recipes.js`
   and each skewer's mod file. They carry no `grilled` (O21), so they do not stack with grilled ones (O30, no code).
4. Seasoning rules ride on `food/seasoning #11` / `#13`: raw skewers in the seasonable tags (seasonings yes, sauces no);
   the grill copies seasonings over (port Java).
5. Saltable tag, stack sizes (skewer 64), Food Plate review (`food/placeable #5`), values (`food/types-and-tuning #19`).

### Decisions
barbeques_delight.md 4.3 decisions 8 (ingredients per line, default cooked meat for raw) and 10 (combos carry `grilled`).

### Tests
BBQ-20 to BBQ-24 (planned in barbeques_delight.md 4.3 #14): names, raw skewer food and poison, `grilled` saturation,
crafted skewer without `grilled`, sauce refused on a raw skewer.

---

## meals #47: Kebab Wrap on a tortilla
Status: Planned / Work: KubeJS / data / id `dishes/meals #47`; after `largecontent/mod-additions #37`

### Goal
Owner O3: the Kebab Wrap ships on bread (upstream combine: bread, 1 cooked skewer, cabbage); switch it to `forge:tortilla`
to match the wrap rule (`#15`, the mutton wrap), keeping the skewer-effect NBT; family `sandwich/wrap`.

### Now
Not ported yet. Upstream recipe type `barbequesdelight:combine` (shapeless with NBT, stores the skewers in `content`).

### Build (after the port)
1. Edit the port's combine JSON for `kebab_wrap` (bread -> `{"tag": "forge:tortilla"}`), or re-emit it from KubeJS through a
   new `R.bbqCombine` emitter in `neoCraftHelper.js` (barbeques_delight.md 4 step 7 already plans `bbqSkewering` /
   `bbqGrilling`; add `bbqCombine` there) with `i.t_tortilla`.
2. Type `handheld/wrap` (`#15`), family `sandwich/wrap`, values check.

### Decisions
Port data edit (simplest) vs a KubeJS emitter (keeps "recipes through the helper"): recommend the emitter, since the port
will need it for `#48` too.

### Tests
- BBQ-new1 Kebab Wrap combines from a tortilla, a cooked skewer and cabbage; not from bread; the skewer's effects carry.

---

## meals #48: Integrate Kebab Sandwich and Bibimbap
Status: Planned / Work: KubeJS / data / id `dishes/meals #48`; after `largecontent/mod-additions #37`

### Goal
Owner O15: in the integration stage, the Kebab Sandwich gets the sandwich rule (shaped plus one sequenced assembly) and
the Bibimbap the meal rule (takes and returns a bowl, Cooking Pot plus a heatless `_manual_only` craft); food families
`sandwich`, `meal`; values; plates.

### Now
Not ported. Upstream: Kebab Sandwich combine = bread, 2 cooked skewers, cabbage, onion (14 / 19.6); Bibimbap combine =
cooked rice, onion, carrot, 3 cooked skewers (16 / 22.4, Nourishment 2:00). Both carry the skewers' effects through the
`content` NBT (barbeques_delight.md 3.2).

### Build (after the port)
1. The NBT is the obstacle: shaped grids, the Cooking Pot and Create assemblies do not copy input NBT into the output.
   Either keep the combine type and add a shaped-pattern variant of it in the port (`ShapedCombineRecipe`), or accept that
   the pot and assembly versions carry no skewer effects.
2. Sandwich: shaped (bread top and bottom, the 2 skewers, cabbage and onion between; pattern per the decision), plus one assembly on `i.t_bread` with a unique first step.
3. Bibimbap: pot `R.fdCooking([cooked rice, onion, carrot, 3 skewers], bibimbap, e, ..., i.bowl)` and a heatless shapeless
   `_manual_only` from cooked parts (one more main); the bowl returns on eating (port `craftRemainder` / food container).
4. Families, values (`food/types-and-tuning #19`), Food Plate tag.

### Decisions
Recipe shapes (the row's decision) and whether the pot / assembly versions may lose the skewer effects: recommend keeping
the combine (effects) as the hand route and letting the machine routes give the plain item.

### Tests
- BBQ-new2 Kebab Sandwich: shaped and assembly recipes; Bibimbap: pot and heatless craft, bowl in and back.

---

## meals #49: Decide: smelt or smoke raw skewers
Status: Planned (decision only) / Work: KubeJS / id `dishes/meals #49` ([barbeques_delight.md](../barbeques_delight.md) 4.3 decision 6, O22)

### Goal
Decide whether a raw skewer can be smelted, smoked or campfire-cooked into a cooked skewer **without** `grilled`. Until
decided only the Grill cooks raw skewers.

### Now
Nothing ported. The pack's cooking heat rule for cooked meat is Smoker + Furnace + Campfire (food_families.md 3.1 Cooked
meat row); O19 originally said "never cooked in furnaces", O22 reopened it.

### Options
- **No** (the Grill keeps its reason to exist; matches O19).
- **Yes, worse:** smoker and campfire only (`R.smoking`, `R.campfire`), result without `grilled` (so 2 less saturation,
  O25), longer time than the grill.
- **Yes, all three** by the heat rule.

### Recommendation
Smoker and campfire only, no furnace, result without `grilled`: one helper loop over the 18 raw skewers in
`barbequesdelight/recipes.js` once `#50` exists. Answer before `#46` is built.

### Tests
- BBQ-new3 (if yes) A raw skewer smoked gives the cooked skewer with no Grilled line; a furnace refuses it.

---

## meals #50: Raw versions of the pack skewers
Status: Planned / Work: new jar / KubeJS / data / id `dishes/meals #50`; after `largecontent/mod-additions #37`

### Goal
Owner O24 (2026-10-31): the 8 pack skewers (Barbecue on a Stick, End Barbecue Stick, Improvised Barbecue Stick, Nether
Skewer, Seafood Skewer, Turnip Mutton Skewer, Cave Skewer, Crispy Skewer) each get a raw item; the Grill cooks it into
the existing item.

### Now
The 8 cooked items and keys: `#13` Now. Nothing raw exists. barbeques_delight.md 4.3 decision 2 (namespace, recipe,
grill time, stack, texture) and 7 (names) hold the defaults.

### Build (after the port)
1. Items: 8 raw skewers, by default in a CD member under each original mod's namespace (decision 2 default), e.g.
   `farmersdelight:raw_barbecue_stick`; recoloured textures (raw tones of the cooked sprite); stack 64; food values worse
   than cooked (`food/types-and-tuning #19`), chicken ones "nasty" (O27).
2. Recipes: one `barbequesdelight:skewering` (basin) recipe per raw skewer from the cooked recipe's raw ingredients, and one
   `barbequesdelight:grilling` recipe raw -> existing cooked item, grill time from the closest BBQ line (3.1 table), both
   through the planned `R.bbqSkewering` / `R.bbqGrilling` emitters.
3. Names (decision 7 default): "Raw " prefix; Crispy Skewer -> "Uncooked Crispy Skewer". Lang in the CD client file.
4. JEI: they appear in the Skewering and Grilling categories by recipe; dict keys, food types (raw type per `#46`).

### Decisions
barbeques_delight.md 4.3 #2 (namespace, recipes, grill time, textures) and #7 (names); defaults as above.

### Tests
- BBQ-new4 Each of the 8 raw skewers is made in the basin and grills into the existing cooked item; names read Raw.

---

## Built, needs play

Rows at Testing; the build facts are in their TODO rows and writeups. Their old build sections were cut 2026-10-31 where
the writeup covers them (rows 1-4, 7-9 in [recipes_cooking.md](../recipes_cooking.md) 2.3 / 2.4, row 42 in
[veggies_delight.md](../veggies_delight.md) history 2026-10-29); the others keep a short section above.

- `dishes/meals #1` Heatless shapeless meals from cooked ingredients, `_manual_only` (shipped 1.1.01) - [recipes_cooking.md](../recipes_cooking.md) 2.3 - open C P (RC-55; SQ-OF-07 PASSED).
- `dishes/meals #2` Per-meal recipe changes, burgers shaped + one assembly (shipped 1.1.01) - [recipes_cooking.md](../recipes_cooking.md) 2.3 - open C P (RC-56 to RC-62).
- `dishes/meals #3` Sequenced assembly meals (shipped 1.1.01) - [recipes_cooking.md](../recipes_cooking.md) 2.3 - open C P (RC-63, RC-64).
- `dishes/meals #4` Expensive cooked shapeless meals, squid ink pasta mixing (shipped 1.1.01) - [recipes_cooking.md](../recipes_cooking.md) 2.3 - open C P (RC-66, RC-67).
- `dishes/meals #7` Fish recipes for any fish, pot frying (shipped 1.1.01) - [recipes_cooking.md](../recipes_cooking.md) 2.3 - open C P (RC-69). Not checked: whether the cooking oil bottle comes back from the pot.
- `dishes/meals #8` Dog food (shipped 1.1.01; any raw meat 1.1.03) - [recipes_cooking.md](../recipes_cooking.md) 2.3 - open C P (RC-68).
- `dishes/meals #9` Gold food cheap in machines, every gold tag (shipped 1.1.01) - [recipes_cooking.md](../recipes_cooking.md) 2.4 - open C P T (RC-70; final numbers with `food/types-and-tuning #1`, #3). IE arc additives are listed 5 times (NCH's `ieArcFurnace` drops counts).
- `dishes/meals #17` Fried rice renamed Egg Fried Rice (shipped 1.1.03) - `namechanges.js` - open C P.
- `dishes/meals #19` Beignet removed; Rustic port no longer registers it (2026-10-29) - [rustic_delight.md](../rustic_delight.md) 5.7 - open C (SQ-CM-30 PASSED r1031 2026-10-31).
- `dishes/meals #22` Overnight oats shapeless (shipped 1.1.04 client) - section above - open C P (SQ-CM-06 PASSED).
- `dishes/meals #32` Mhadjeb removed (shipped 1.1.03) - section above - open S~ C P (RC-132; `canned_mhadjeb` waits for the can regeneration, SQ-CDB-12).
- `dishes/meals #36` Cacciatore takes chicken cuts (nothing to build; grid fix 2026-10-25) - section above - open C P (RC-136; SQ-CDB-11 PASSED).
- `dishes/meals #38` Gnocchi in `#forge:pasta`, every pasta recipe on the tag (shipped 1.1.04 client) - [veggies_delight.md](../veggies_delight.md) - open C P (SQ-CM-03, -04 PASSED).
- `dishes/meals #39` Field salad: flowers and slot tags (built 2026-10-29) - section above - open C P (RC-160; SQ-CM-20 PASSED r1031).
- `dishes/meals #41` Second stuffed pepper recipe from a roasted pepper (shipped 1.1.04 client) - [rustic_delight.md](../rustic_delight.md) 5.2 - open S C P (SQ-RD-33 not run).
- `dishes/meals #42` Coleslaw 12 / 0.6 (built 2026-10-29) - [veggies_delight.md](../veggies_delight.md) history 2026-10-29 - open P T (RC-161; SQ-CM-21 PASSED r1031; rerun `comfort_nourishment` after `/dumpfood`).
- `dishes/meals #43` Veggie Noodles: name, sprite, six-vegetable dough (built 2026-10-29) - section above - open C P (RC-162; SQ-CM-22 PASSED r1031).

Recipe level of RC-160 to RC-164 (field salad, coleslaw and shrimp fried rice values, cucumber dough, shrimp fried rice,
no smoothie): scratch server PASSED r1031 (2026-10-31, SQ-CM-20 to -24); JEI, tooltips and crafting stay in game.

Test ids: RC-42 to RC-49 and RC-120 to RC-137 in the sections above are this brief's proposals (only RC-132, RC-133,
RC-136 are in the writeup or the scratch queue); new sections use `RC-newN` placeholders. Renumber from RC-176 when
folding into [recipes_cooking.md](../recipes_cooking.md).

## Writeup corrections made

- [recipes_cooking.md](../recipes_cooking.md) 0 "At a glance": the cornmeal / pie crust row said "BUILT 2026-10-19,
  not booted"; it shipped in 1.0.8 (history line, plan_1.0.8.md), now DEPLOYED.
- [recipes_cooking.md](../recipes_cooking.md) 0 "Files to open" and F4: `cooking.js` live code is lines 1-433, the old
  `global.CE` copy 436-852 (was 1-338 / 340-764).
- [recipes_cooking.md](../recipes_cooking.md) 2.3: Scarlet pierogi row described the pack's removed shapeless recipe;
  now the pot recipe with beetroot for nether wart. Added a row for pizza, ham and cheese sandwich and quiche (in
  `cooking.js` since 2026-10-20).
- [alcohol.md](../alcohol.md) 8.6 Scarlet Pierogi row: the "delete the pack's two pierogi recipes, cooking.js lines
  124-127" step is done (removed with 1.0.8).
