# Brief: food / meals

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# food`, Sub `meals` (rows 1 to 10). Layout per
[README.md](README.md). Feature writeup for this area: [recipes_cooking.md](../recipes_cooking.md) (2.3 Prepared food,
2.4 Golden food); the Brewin' recipes in [alcohol.md](../alcohol.md) 8.6. Every recipe below goes in
[cooking.js](../../kubejs/server_scripts/cooking/cooking.js) (section "Prepared", lines 172-342, or a new
`//##### Meals #####` section after it) or [golden.js](../../kubejs/server_scripts/cooking/golden.js), through
`global.NCH` (`R`), with ids from `global.item` (`i`) / `global.fluid` (`f`).

**Verified against jars** (scratchpad cache, 2026-10-21): Farmer's Delight 1.19.2-1.2.4, Quark Delight 1.0.0, Ender's
Delight 1.19-1.2.2, Alex's Mobs 1.21.1, Alex's Delight 1.4.1, PneumaticCraft 4.3.15, More Delight 25.07.28, Incubation
3.0.1, Delightful 3.4.1, Fruits Delight 0.5.11, Create Central Kitchen 1.3.11.c, Rustic Delight 1.3.0, Corn Delight
1.0.3, Golden Food 2.0.0, Create 0.5.1.i; the Brewin' port from `development/backportedBrewin/src/main/resources`.
Recipe ids, ingredient lists and tags quoted below are from those jars unless marked *unverified*. What another datapack
or KubeJS does to them at runtime is only knowable in game (`/dumprecipes`, `/dumptags item`, JEI).

## Scope and order

| Row | Change | Status | Build group |
|---|---|---|---|
| 1 | Cooked ingredients in heatless shapeless meals | Planned | A (the rule, plus a coverage list) |
| 2 | Per-meal recipe changes (12 meals) | Planned | A |
| 4 | Expensive cooked shapeless meals (9 meals) | Planned | A |
| 8 | Dog food | Planned | A |
| 3 | Sequenced assembly meals | In Progress | B |
| 5 | Feasts: JEI plating tab, assemblies, pizza 8 slices | Planned | B (assemblies), C (JEI tab, pizza: Java) |
| 7 | Fish recipes for any fish (tempura, fish and chips, fries, PNC deep frying out) | Planned | D |
| 6 | Glistering melon edible + gilding | Planned | E |
| 9 | Gold food cheap in machines | Planned | E (after `food/types-and-tuning #1`, #3) |
| 10 | Shrimp fried rice | Planned | F (tooltip + JEI note; Alex's Mobs already does the rest) |

Suggested order:

1. **Group A** (rows 1, 2, 4, 8): one pass over `cooking.js`. Do row 1's two shared pieces first: the new dict keys
   and tags (A0 below) and the `_manual_only` decision. Coordinate with `food/ingredients #4` (bone broth), which owns
   bone broth itself; this brief owns the meal recipes that consume it. Row 2's baked cod stew is also in row 4: build it
   once (row 2).
2. **Group B** (row 3, the assembly half of row 5): all new sequenced assemblies plus their `transition_items.js` rows.
   Shepherd's pie appears in both rows; build it once (row 3).
3. **Group D** (row 7): needs the `#rusticdelight:cooking_oil` key from A0; touches `food/ingredients #8` (diced
   potatoes merge) and `#10` (fish oil as cooking oil).
4. **Group E** (rows 6, 9): `golden.js` and `food_values.js`; row 9's numbers wait for food tuning.
5. **Group F** (row 10) any time: client scripts only.
6. **Group C** (row 5's JEI tab and pizza servings) last: Java (patcherMod, Brewin' port), a jar rebuild each.

**Shared A0: keys and tags to add first.** Key style in `item_dict.js`: plain keys for items, `t_<path>` for tags
(`i.t_cheese_wedge`, `i.t_crops_corn`, `i.t_fruits_*`, `i.t_cooking_oil_ingredients`); new tags made by the pack go
through `global.tags` in [cookingtags.js](../../kubejs/server_scripts/tags/cookingtags.js).

| Key (new) | Value | Used by |
|---|---|---|
| `i.t_bread` | `'#forge:bread'` (vanilla bread, `corn_delight:cornbread`, `pneumaticcraft:sourdough_bread`; not slices: those are `#forge:bread_slices`) | row 2 burgers. Note `i.bread` is `#forge:bread/wheat` |
| `i.t_tortilla` | `'#forge:tortilla'` (`corn_delight:tortilla`) | row 2 mutton wrap |
| `i.t_cooking_oil` | `'#rusticdelight:cooking_oil'` (`rusticdelight:cooking_oil`) | row 7 |
| `i.t_sliced_potatoes` | `'#forge:sliced_potatoes'` (today only `moredelight:diced_potatoes`) | row 7 |
| `i.t_soup_veggie_cooked` | `'#creativeengineering:soup_veggies_cooked'` (new tag, below) | rows 2, 4 |
| `i.t_exotic_fruit` | `'#creativeengineering:exotic_fruits'` (new tag, below) | row 2 exotic fruits |
| `i.t_fruit_salad_slice` | `'#creativeengineering:fruit_salad_slices'` (new tag, below) | row 2 fruit salad |

New tags (in `cookingtags.js`, all through `global.tags`):

```js
// cooked veggies that make sense in a soup or stew: the cooked/veggie food type minus snacks and rice
t.addMatching(e, i.t_soup_veggie_cooked, [i.t_food_cooked_veggie, i.t_food_cooked_veggie_sliced],
	{ except: [i.chips, i.popcorn, i.tortilla_chip, i.cooked_rice, i.dried_kelp] })
// Fruits Delight's 15 #forge:fruits members + its two melons + vanilla melon
t.add(e, i.t_exotic_fruit, [i.pear, i.hawberry, i.lychee, i.mango, i.persimmon, i.peach, i.orange, i.mangosteen,
	i.bayberry, i.kiwi, i.fig, i.blueberry, i.lemon, i.cranberry, i.pineapple, i.durian_flesh, i.hamimelon_slice, i.melon])
t.add(e, i.t_fruit_salad_slice, [i.melon, i.cantaloupe_slice, i.hamimelon_slice, i.pineapple_slice, i.lemon_slice,
	i.orange_slice])
```

`addMatching` with `except` expands the food-type tags to their members at tag time (tag_helper.js lines 26-48), so the
food-type tags must already be filled: [foodtypetags.js](../../kubejs/server_scripts/tags/foodtypetags.js) and
`cookingtags.js` both run in `ServerEvents.tags('item')`; if order bites, list the members by key instead (the
cooked/veggie list is in [food_catalog.md](../food_catalog.md) "cooked/veggie").

**Shared decision A1: `_manual_only` on the new shapeless meals.** Create runs every shapeless crafting recipe in an
unheated basin (`allowShapelessInMixer`, default true, verified in Create's `CRecipes`) and skips any recipe whose id
path ends in `_manual_only` (`AllRecipeTypes.CAN_BE_AUTOMATED`, verified; also used by mechanical crafters). Without
the suffix every "heatless cooked shapeless" meal becomes a no-heat mixer recipe, cheaper to automate than the pot
(which Slice & Dice turns into heated mixing). `food/ingredients #4` already says "no Create shapeless mixing".
**Recommend:** every new shapeless meal in rows 1, 2, 4 and 8 gets an id ending `_manual_only`
(`R.shapeless(out, inputs, e, 'chicken_soup_cooked_manual_only')` -> `creativeengineering:shapeless/chicken_soup_cooked_manual_only`),
and the 1.0.3-era bone-broth shapeless recipes (cooking.js lines 22-32, auto ids) are re-id'd the same way in the same
pass (`R.remove` nothing; just pass an `idOverride`). Same for the shaped burgers in row 2 (automation through their
sequenced assembly only).

---

## meals #1: Cooked ingredients in heatless shapeless meal recipes
Status: Planned / Work: KubeJS / id `food/meals #1`

### Goal
Every pot meal (soup, stew, pasta, rice dish) also has a heatless shapeless recipe that takes the cooked form of each
raw ingredient (baked potato for potato, cooked meat for raw, tomato sauce for tomato, cooked rice for rice, fried
egg for egg) plus bone broth for soups and stews. The pot stays the efficient route; the grid route costs a cooking
step per ingredient.

### Now
- [cooking.js](../../kubejs/server_scripts/cooking/cooking.js) lines 16-32: bone broth (pot x2 and shapeless x1) and
  eight bone-broth / cooked-rice shapeless meals: beef stew, baked cod stew, noodle soup, chicken soup, cooked rice
  (line 26), fried rice, mushroom rice, sinigang (lines 30-32). Ids are `toId` auto ids
  (`creativeengineering:shapeless/<inputs>_to_<output>_by_shapeless`).
- Also cooked-shapeless already: fish stew (252), cabbage rolls (255), horror lasagna (326), pasta with mutton (330),
  stuffed shulker, ender paella, pearl pasta (414-418), crab bars (241, `_manual_only`).
- Several of these still take raw ingredients (fried rice: raw eggs; baked cod stew: raw eggs, raw potato, tomato;
  mushroom rice: raw carrot / potato): fixed in row 2.

### Build
1. A0 keys and tags (top of this file), A1 decision.
2. Write the conversion rule as a comment block at the top of the new Meals section, so later meals follow it:
   raw meat -> the cooked tag (`i.cooked_beef`, `i.cooked_pork`, `i.cooked_chicken`, `i.cooked_mutton`,
   `i.cooked_fish`, `i.cooked_meat`); potato -> `i.baked_potato`; tomato -> `i.tomato_sauce` (bowl, consumed);
   rice -> `i.cooked_rice`; egg -> `i.cooked_eggs`; raw pasta stays `i.pasta`; generic `#forge:vegetables` ->
   `i.t_soup_veggie_cooked`; soups and stews add `i.bone_broth` (which brings the bowl, so no extra `i.bowl`).
   "Expensive" = at least one more main ingredient than the pot recipe, or the pot's output count halved.
3. Rows 2, 4 and 8 are the concrete recipes. Beyond them, coverage candidates (pot recipes with a soup / meal output
   and no shapeless route, from the jars above; decide which to add, see Decisions): FD `vegetable_soup`,
   `vegetable_noodles`, `ratatouille`, `stuffed_pumpkin_block`; Corn Delight `corn_soup`, `cornbread_stuffing`;
   Fruits Delight `fig_chicken_stew`, `lychee_chicken`, `orange_chicken`, `orange_marinated_pork`,
   `pineapple_marinated_pork`, `pineapple_fried_rice`; More Delight `carrot_soup`, `cooked_rice_with_{beef,porkchop,chicken_cuts}`,
   `diced_potatoes_with_*` (4), `mashed_potatoes`, `creamy_pasta_with_{chicken_cuts,ham}`; Quark Delight `miso_with_bamboo_sprouts`,
   `cooked_whole_crab`; Alex's Delight `kangaroo_stew`, `kangaroo_pasta`, `lobster_pasta`, `acacia_blossom_soup`;
   Rustic Delight `bell_pepper_soup`, `bell_pepper_pasta`, `coffee_braised_beef`, `stuffed_bell_pepper_*` (3). Not in
   the downloaded set (check with `/dumprecipes` and filter `type: farmersdelight:cooking`): Nether's Delight,
   Neapolitan, Brewin' (`cheesy_pasta`, `vegetable_omelet`), Delightful (`coconut_curry`, conditional recipes),
   Alex's Delight others.
4. Leave out: drinks, jellies, teas, custards (sweets / drinks rows), Rustic Delight `fried_*` (frying is the point),
   vanilla `rabbit_stew` / `mushroom_stew` / `beetroot_soup` (already shapeless in vanilla).

### Ids and data
- Pot recipe ids follow `<ns>:cooking/<name>`; Fruits Delight's are under `farmersdelight:cooking/<name>` (verified).
- `i.bone_broth` (`farmersdelight:bone_broth`), `i.cooked_rice`, `i.tomato_sauce`, `i.baked_potato` exist in the dict.
- Output keys for the coverage list: run `tools/food_catalog.py` output ([food_catalog.md](../food_catalog.md)) for ids;
  most already have keys in the generated block at the end of `item_dict.js` (grep the id).

### Decisions
- **Coverage:** only the meals rows 2, 4, 8 name, or the whole candidate list? Recommend rows 2/4/8 now, then the FD
  and Corn Delight candidates (vegetable soup, vegetable noodles, ratatouille, corn soup), and leave mod-specific dishes
  to their own rows.
- **A1 `_manual_only`:** recommend yes (see top).

### Gotchas
- Shapeless inputs max 9 slots; `Item.of(x, 2)` counts as 2 slots.
- `i.tomato_sauce` is a bowl item: a shapeless recipe that takes it plus `i.bowl` costs two bowls and returns one meal.
  Don't add `i.bowl` when tomato sauce or bone broth is already in the grid (bone broth is a bowl food; the bowl is
  consumed with it, not returned: vanilla remainder only applies to `craftRemainder` items).
- Bowl-returning crafts and the remainder buffer: [remainder_buffer.md](../remainder_buffer.md).

### Tests
- RC-30 Heatless route: for each new meal, JEI shows the shapeless recipe; crafting it by hand works; a basin under an
  unheated mixer with the same items does nothing (A1).

---

## meals #2: Per-meal recipe changes
Status: Planned / Work: KubeJS / id `food/meals #2`

### Goal
Twelve meal fixes from the cooking plan and scratchpad 2026-10-21. Per meal below: now -> change -> sketch.

### Now / Build (per meal)
1. **Fried rice + fried egg.** Pack shapeless cooking.js line 30 `[i.cooked_rice, i.eggs, i.carrot, i.onion]` (raw
   egg). Pot: `farmersdelight:cooking/fried_rice` (rice, `#forge:eggs`, carrot, onion) and
   `rusticdelight:cooking/fried_rice_from_cooking_oil` (rice, `#rusticdelight:fried_rice_ingredients` = eggs or
   cooking oil, carrot, onion). Change line 30's egg to `i.cooked_eggs`, id `fried_rice_cooked_manual_only`. Pots stay.
2. **Baked cod stew + fried egg, baked potato, tomato sauce.** Line 23 `[i.bone_broth, i.cooked_cod, i.eggs, i.potato,
   i.tomato]` -> `[i.bone_broth, i.cooked_cod, i.cooked_eggs, i.baked_potato, i.tomato_sauce]` (`_manual_only` id).
   This also covers the row 4 entry.
3. **Chicken soup any cooked veggie.** Line 25 `[i.bone_broth, i.cooked_chicken, i.vegetables, i.salad_ingredients,
   i.carrot]` -> `[i.bone_broth, i.cooked_chicken, i.t_soup_veggie_cooked, i.salad_ingredients, i.t_soup_veggie_cooked]`
   (the raw carrot goes too; salad stays raw: cabbage / lettuce aren't cooked).
4. **Mushroom rice + baked potato.** Line 31 `[i.cooked_rice, i.brown_mushroom, i.red_mushroom, [i.carrot, i.potato]]`
   -> `[i.cooked_rice, i.brown_mushroom, i.red_mushroom, i.baked_potato]`. (Decision: keep a carrot option? There is no
   cooked carrot item; recommend baked potato only, as written.)
5. **Mutton roll needs tortilla.** The item is `farmersdelight:mutton_wrap` (`i.mutton_wrap`, "Mutton Wrap"). Two
   recipes: `farmersdelight:mutton_wrap` (shapeless `#forge:bread`, `#forge:cooked_mutton`, salad, onion) and Create
   Central Kitchen's `create_central_kitchen:sequenced_assembly/mutton_wrap` (base `#forge:bread`; deploy cooked mutton,
   salad, onion; verified in CCK `FDRecipes`, shipped in its built-in `farmersdelight` datapack). Remove both; re-add
   `R.shapeless(i.mutton_wrap, [i.t_tortilla, i.cooked_mutton, i.salad_ingredients, i.onion], e, 'mutton_wrap')` and an
   assembly on `i.t_tortilla` (new transition row `['mutton_wrap']`, auto texture `farmersdelight:item/mutton_wrap`
   exists).
6. **Scrambled eggs dearer.** `incubation:scrambled_eggs` is already an unheated shapeless (`#forge:eggs` x2,
   `#forge:milk`, bowl; verified, no other recipe). So only "dearer" is left: remove it, re-add with 3 eggs + milk +
   bowl (or 2 eggs + milk + a cheese wedge). Decision below.
7. **Omelette with fried eggs + shapeless veggie omelette.** `moredelight:omelette` already cooks from
   `#forge:cooked_eggs` (smelting / smoking / campfire, verified): nothing to do there. The change is Brewin's
   `brewinandchewin:vegetable_omelet` (pot `brewinandchewin:cooking/vegetable_omelet`: cheese wedge, egg x2, onion,
   carrot, bowl): `R.replaceInput({ id: 'brewinandchewin:cooking/vegetable_omelet' }, i.eggs, i.cooked_eggs, e)` (the
   port's data stays 4.5.0, alcohol.md 8.6; `replaceInput` was used the same way for the pierogi, cooking.js line 174),
   plus `R.shapeless(i.vegetable_omelet, [i.omelette, i.t_cheese_wedge, i.onion, i.carrot, i.bowl], e,
   'vegetable_omelet_from_omelette_manual_only')`.
8. **Exotic fruits: any Fruits Delight fruit, shapeless.** `quarkdelight:bowl_of_exotic_fruits` is already shapeless
   (`quark:ancient_fruit`, `minecraft:melon_slice`, `minecraft:chorus_fruit`, bowl; conditional on Quark). Remove it;
   re-add with `i.t_exotic_fruit` in the melon slot.
9. **Fruit salad: sequenced + slice tag.** Id `farmersdelight:fruit_salad` ships twice (FD: apple, melon x2, berries
   x2, pumpkin slice, bowl; Delightful overrides the same id with apple, melon, pumpkin slice, `#forge:fruits`, berries
   x2, bowl). Which one loads depends on datapack order: check JEI. Remove the id; re-add shapeless
   `[i.apple, i.t_fruit_salad_slice, i.t_fruit_salad_slice, i.t_berries, i.t_berries, i.pumpkin_slice, i.bowl]`
   (`i.t_berries` = `#forge:berries`, exists) and an assembly on `i.bowl` deploying apple,
   slice, slice, berries, berries (transition row `['fruit_salad']`, auto texture exists).
10. **Burgers: cheese only, shaped, assembly the only automation, bread or cornbread only.** Recipes in the pack
    (verified): `farmersdelight:hamburger` (shapeless `#forge:bread`, `farmersdelight:beef_patty`, salad, tomato,
    onion) and CCK's `create_central_kitchen:sequenced_assembly/hamburger` (base `#forge:bread`, deploy patty, salad,
    tomato, onion); Delightful `delightful:food/cheeseburger` and `delightful:food/deluxe_cheeseburger`
    (`forge:conditional`; `#forge:cheese` = `brewinandchewin:flaxen_cheese_wedge` from Delightful's own tag), with
    `_from_milk` and `_from_bun` variants that only load when `#forge:cheese` is empty, `use_milk_tag = true`
    ([config/delightful-common.toml](../../config/delightful-common.toml) line 134: false) or a bun tag exists, so today
    the milk ones should not load (check JEI); More Delight `moredelight:simple_hamburger`, `hamburger_with_egg`,
    `loaded_hamburger` (shapeless, `#forge:bread`); Alex's Delight `alexsdelight:bison_burger`; and
    `alexsmobs:kangaroo_burger` (Alex's Mobs ships it shaped `B/K/B` with bread; Alex's Delight ships the same id as a
    shapeless; datapack order decides). For each: `R.remove` the id(s), add `R.shaped(..., e, '<name>_manual_only')`
    with `i.t_bread` as the bun (cheese burgers: `i.t_cheese_wedge`, never milk), and one `R.createSequencedAssembly`
    on `i.t_bread`. "Cheese only" = remove `delightful:food/cheeseburger_from_milk` and
    `delightful:food/deluxe_cheeseburger_from_milk` explicitly too (harmless if not loaded). Pattern rule (suggested):
    top row toppings, middle row patty (cheese either side), bottom row the bread, e.g. hamburger `['STO', ' P ', ' B ']`.
11. **Quiche recipes combined so they alternate in JEI.** Now: Brewin's `brewinandchewin:quiche_from_bacon` (shaped
    `blb/mcm/eCe`, cooked bacon) and the pack's `creativeengineering:shaped/quiche_from_mushroom` (cooking.js lines
    199-202), plus two assemblies `quiche_bacon_sequenced_assembly` / `quiche_mushroom_sequenced_assembly` (lines
    205-219). Merge into one shaped recipe whose `b` key is `[i.cooked_bacon, i.brown_mushroom]` and `l` key
    `[i.cabbage_leaf, i.cabbage]` (JEI cycles the options), id `quiche`; remove `brewinandchewin:quiche_from_bacon`.
    Merge the two assemblies into one whose first step deploys `Ingredient.of([i.cooked_bacon, i.brown_mushroom])`:
    this also removes the "Create only picks one of two recipes with the same first step" problem noted at line 203.
    Keep transition row `quiche_bacon` (rename to `quiche`) and drop `quiche_mushroom` from
    [transition_items.js](../../kubejs/startup_scripts/changes/transition_items.js) lines 45-46. Side effect: one bacon
    plus one mushroom in the two `b` slots also crafts; fine.
12. **Shepherd's pie takes cooked mutton chops.** Already true: FD's `farmersdelight:shepherds_pie_block` (shapeless:
    baked potato x2, `#forge:milk`, `#forge:cooked_mutton` x3, onion x2, bowl) and FD's `#forge:cooked_mutton` holds
    `minecraft:cooked_mutton` and `farmersdelight:cooked_mutton_chops` (both verified). Nothing in `kubejs/` removes the
    chops from the tag. Confirm in JEI; if it works, close this item. Row 3 rebuilds the recipe anyway (cheese for
    milk) and keeps `i.cooked_mutton`.

### Ids and data
- Missing keys to add: `i.t_bread`, `i.t_tortilla` (A0). All meal outputs have
  keys (`i.fried_rice`, `i.baked_cod_stew`, `i.chicken_soup`, `i.mushroom_rice`, `i.mutton_wrap`, `i.scrambled_eggs`,
  `i.omelette`, `i.vegetable_omelet`, `i.bowl_of_exotic_fruits`, `i.fruit_salad`, `i.hamburger`, `i.cheeseburger`,
  `i.deluxe_cheeseburger`, `i.simple_hamburger`, `i.hamburger_with_egg`, `i.loaded_hamburger`, `i.bison_burger`,
  `i.kangaroo_burger`, `i.quiche`, `i.shepherds_pie_block`; checked by value in `item_dict.js`).
- Patties: `i.beef_patty`, `i.bison_patty` (`alexsdelight:bison_patty`); kangaroo: `#alexsdelight:cooked_kangaroo`
  (no key: add `i.t_cooked_kangaroo`).

### Decisions
- Scrambled eggs cost: recommend 3 eggs + milk + bowl (one more egg).
- Fruit salad assembly yield: recommend 2 (assemblies in this file give a bonus over the grid, e.g. salmon roll 3 vs 2).
- Burgers: whether sourdough bread (in `#forge:bread`) counts as "bread". Recommend yes (it is bread); `i.t_bread`
  as is. Bun item: none exists; `food/baked-goods #9` may add one ("buns in burgers"), then swap the key.
- Quiche merge: recommend as above (one shaped, one assembly).

### Gotchas
- The burgers' new shaped ids must end `_manual_only` or mechanical crafters automate them (A1).
- `R.remove` of a CCK sequenced assembly id works like any other id (it is a normal datapack recipe).
- Delightful's conditional recipes are `forge:conditional` wrappers; the inner recipe keeps the outer id
  (`delightful:food/cheeseburger`).

### Tests
- RC-31 Fried rice / baked cod stew / chicken soup / mushroom rice take the cooked ingredients, not the raw ones.
- RC-32 Mutton wrap needs a tortilla in the grid and on the deployer belt; bread no longer works.
- RC-33 Vegetable omelet pot needs fried eggs; omelette + cheese + onion + carrot + bowl crafts one.
- RC-34 Exotic fruits accept a lemon (or any Fruits Delight fruit) in place of melon; fruit salad accepts a pineapple
  slice; fruit salad assembly on a bowl works.
- RC-35 Burgers: shaped only, no milk version in JEI, assembly on bread works; a mechanical crafter with the shaped
  pattern does nothing.
- RC-36 Quiche: one shaped recipe in JEI cycling bacon / mushroom; assembly with mushrooms first works.
- RC-37 Shepherd's pie with 3 cooked mutton chops crafts.

---

## meals #3: Sequenced assembly meals
Status: In Progress / Work: KubeJS / id `food/meals #3`

### Goal
Create sequenced assembly (deployer, spout) routes for the remaining hand-made meals: shepherd's pie (also shapeless
with cheese for milk), horror lasagna, stuffed potato (with spouting), crispy skewer, crawling sandwich, steak and
potatoes (the last three also become shaped).

### Now
- Done: quiche (bacon, mushroom; cooking.js lines 203-219, 1.0.8). Pattern to copy: the barbecue stick (lines 228-237)
  and grilled salmon (lines 257-268).
- Upstream recipes (verified): `farmersdelight:shepherds_pie_block` shapeless (above, row 2 item 12);
  `brewinandchewin:cooking/horror_lasagna` pot (scarlet wedge, beetroot, tomato sauce, pasta, cheese wedge; bowl) and
  the pack's shapeless (cooking.js line 326: scarlet wedge x2, beetroot soup, tomato sauce x2, pasta,
  `#brewinandchewin:cooked_horror_lasagna_meats` x2, bowl; that tag is filled by the pack, cookingtags.js lines 34-37);
  `farmersdelight:stuffed_potato` shapeless (baked potato, `#forge:cooked_beef`, `#forge:milk`);
  `endersdelight:crispy_skewer` shapeless (mite crust, popped chorus fruit, chorus fruit, stick);
  `endersdelight:crawling_sandwich` shapeless (`#endersdelight:shulker_loot`, mite crust, chorus fruit, bread);
  `farmersdelight:steak_and_potatoes` shapeless (baked potato, cooked beef, bowl, onion, cooked rice).

### Build
1. Transition rows in [transition_items.js](../../kubejs/startup_scripts/changes/transition_items.js) "cooking" block
   (lines 32-46): `['shepherds_pie']`, `['horror_lasagna']`, `['stuffed_potato']`, `['crispy_skewer']`,
   `['crawling_sandwich']`, `['steak_and_potatoes']`. Auto textures exist for all
   (`farmersdelight:item/shepherds_pie`, `brewinandchewin:item/horror_lasagna`, `farmersdelight:item/stuffed_potato`,
   `endersdelight:item/crispy_skewer`, `endersdelight:item/crawling_sandwich`, `farmersdelight:item/steak_and_potatoes`;
   verified in the jars / port). Startup change: needs a restart.
2. Recipes (sketch; `inc = R.incomplete(key)`, one loop unless noted):

   | Meal | Grid change | Assembly: base, steps -> output |
   |---|---|---|
   | Shepherd's pie (`i.shepherds_pie_block`) | remove `farmersdelight:shepherds_pie_block`; shapeless baked potato x2, `i.t_cheese_wedge`, `i.cooked_mutton` x3, onion x2, bowl | `i.bowl`; deploy cooked mutton x3, onion, onion, baked potato, baked potato, cheese wedge -> 1 |
   | Horror lasagna (`i.horror_lasagna`) | keep line 326 | `i.bowl`; spout `Fluid.of(f.tomato_sauce, 250)`, deploy pasta, scarlet wedge, lasagna meat, lasagna meat, scarlet wedge -> 2 (the efficient route the plan asked for) |
   | Stuffed potato (`i.stuffed_potato`) | keep | `i.baked_potato`; deploy `i.cooked_beef`, spout `Fluid.of(f.milk, 250)` -> 1 |
   | Crispy skewer (`i.crispy_skewer`) | remove `endersdelight:crispy_skewer`; shaped (pattern: Decisions) | `i.stick`; deploy chorus fruit, popped chorus, mite crust -> 1 |
   | Crawling sandwich (`i.crawling_sandwich`) | remove `endersdelight:crawling_sandwich`; shaped `['SMC', ' B ']` | `i.minecraft_bread` (or `i.t_bread`); deploy shulker loot, mite crust, chorus fruit -> 1 |
   | Steak and potatoes (`i.steak_and_potatoes`) | remove `farmersdelight:steak_and_potatoes`; shaped `['ROP', ' B ', ' W ']` (rice, onion, potato, beef, bowl) | `i.bowl`; deploy cooked rice, baked potato, cooked beef, onion -> 1 |

   ```js
   const incShep = R.incomplete('shepherds_pie')
   R.createSequencedAssembly(i.shepherds_pie_block, i.bowl, [
   	e.recipes.create.deploying(incShep, [incShep, i.cooked_mutton]),
   	// ... as in the table
   	e.recipes.create.deploying(incShep, [incShep, i.t_cheese_wedge])
   ], ['shepherds_pie'], e, 'shepherds_pie_sequenced_assembly', 1)
   ```
3. Shepherd's pie is also in row 5's feast list: built here, row 5 points to it.
4. Update [recipes_cooking.md](../recipes_cooking.md) 2.3 table when built.

### Ids and data
- Keys exist: `i.shepherds_pie_block`, `i.horror_lasagna`, `i.stuffed_potato`, `i.crispy_skewer`,
  `i.crawling_sandwich`, `i.steak_and_potatoes`, `i.mite_crust`, `i.popped_chorus_fruit`, `i.chorus_fruit`,
  `i.shulker_loot`, `i.scarlet_cheese_wedge`, `i.cooked_horror_lasagna_meat`, `i.t_cheese_wedge`; fluids
  `f.tomato_sauce` (`create_central_kitchen:tomato_sauce`), `f.milk`.

### Decisions
- Crispy skewer shaped pattern: recommend a diagonal skewer, stick bottom-left, mite crust and chorus fruit at the top:
  `[' MC', ' P ', 'S  ']` (M mite crust, C chorus fruit, P popped chorus fruit, S stick). Check JEI for clashes.
- Horror lasagna assembly yield 2: recommend yes (it is the "efficient" route); check against food tuning.

### Gotchas
- Two assemblies on the same base with the same first step: Create only ever picks one (cooking.js line 203). Bowl-based
  assemblies here start with different items (cooked mutton, tomato sauce spout, cooked rice), and the existing bowl
  ones start with onion (grilled salmon), cooked bacon, tomato (roasted mutton chops), cod roll (rice medley): no clash.
  Keep it that way when adding feasts (row 5).
- A spout step uses `e.recipes.create.filling(inc, [inc, Fluid.of(...)])` (as the quiche, line 213).
- `createSequencedAssembly` throws without an explicit idOverride (NCH line 1340).

### Tests
- RC-38 Each assembly runs on a belt with deployers / spout and gives the listed output; JEI shows each.
- RC-39 Shepherd's pie by hand needs a cheese wedge, milk no longer works; crispy skewer, crawling sandwich, steak and
  potatoes have only shaped grid recipes.

---

## meals #4: Expensive cooked shapeless meals
Status: Planned / Work: KubeJS / id `food/meals #4`

### Goal
Grid routes (heatless, from cooked ingredients, costlier than the pot) for chorus stew, endermite stew, crab pasta,
baked cod stew (row 2), pumpkin soup, pasta with meatballs, dumplings, cavern explorer stew; squid ink pasta as a
two-fluid mix (ink + tomato sauce) or an expensive shapeless.

### Now (pot recipes, verified)
| Meal | Pot inputs (container) |
|---|---|
| `endersdelight:chorus_stew` | chorus fruit, chorus flower (`endersdelight:shulker_bowl`) |
| `endersdelight:endermite_stew` | `#endersdelight:enderman_loot` (ender pearl, ender shard), mite crust (shulker bowl) |
| `quarkdelight:crab_pasta` | `quark:crab_leg`, `farmersdelight:raw_pasta`, `#forge:vegetables` (bowl) |
| `farmersdelight:pumpkin_soup` | pumpkin slice, salad, `#forge:raw_pork`, `#forge:milk` |
| `farmersdelight:pasta_with_meatballs` | minced beef, `#forge:pasta`, tomato sauce |
| `farmersdelight:dumplings` x2 | `#forge:dough`, cabbage, onion, raw chicken / pork / beef or brown mushroom |
| `quarkdelight:cavern_explorer_stew` | `quark:glow_shroom`, glow ink sac, `#forge:vegetables` (bowl) |
| `farmersdelight:squid_ink_pasta` | `#forge:raw_fishes`, `#forge:pasta`, tomato, ink sac |

### Build
Shapeless, each with an `_manual_only` id (A1). Suggested inputs (one more main ingredient than the pot):

```js
R.shapeless(i.chorus_stew, [i.popped_chorus_fruit, i.popped_chorus_fruit, i.chorus_flower, i.shulker_bowl], e, 'chorus_stew_cooked_manual_only')
R.shapeless(i.endermite_stew, [i.enderman, i.mite_crust, i.mite_crust, i.shulker_bowl], e, 'endermite_stew_cooked_manual_only')
R.shapeless(i.crab_pasta, [i.cooked_crab, i.cooked_crab, i.pasta, i.t_soup_veggie_cooked, i.bowl], e, 'crab_pasta_cooked_manual_only')
R.shapeless(i.pumpkin_soup, [i.bone_broth, i.pumpkin_slice, i.pumpkin_slice, i.salad_ingredients, i.cooked_pork, i.milk], e, 'pumpkin_soup_cooked_manual_only')
R.shapeless(i.pasta_with_meatballs, [i.beef_patty, i.beef_patty, i.pasta, i.tomato_sauce], e, 'pasta_with_meatballs_cooked_manual_only')
R.shapeless(Item.of(i.dumplings, 1), [i.any_dough, i.cabbage, i.onion, [i.cooked_meat, i.brown_mushroom]], e, 'dumplings_cooked_manual_only')
R.shapeless(i.cavern_explorer_stew, [i.bone_broth, i.glow_shroom, i.glow_shroom, i.glow_ink, i.t_soup_veggie_cooked], e, 'cavern_explorer_stew_cooked_manual_only')
R.shapeless(i.squid_ink_pasta, [i.cooked_fish, i.pasta, i.tomato_sauce, i.ink_sac, i.ink_sac], e, 'squid_ink_pasta_cooked_manual_only')
// two-fluid heated mix: Create's basin takes two input fluids
R.createMixing(Item.of(i.squid_ink_pasta, 2), [Fluid.of(f.ink, 250), Fluid.of(f.tomato_sauce, 250),
	i.pasta, i.pasta, i.raw_fish, Item.of(i.bowl, 2)], e, 'squid_ink_pasta_mixing', 'heated')
```

- Pasta with meatballs uses `i.beef_patty` (cooked minced beef) in place of minced beef; tomato sauce brings its bowl.
- Dumplings: the pot gives 2, the grid 1 ("expensive" by yield).
- Chorus / endermite stews come in a shulker bowl (Ender's Delight's container); the `_wood` variants
  (`chorus_stew_wood`, `endermite_stew_wood`) have no recipe here (the pack removed `twisted_cereal_wood`, cooking.js
  line 410); leave them.

### Ids and data
- Keys exist (checked): `i.chorus_stew`, `i.endermite_stew`, `i.crab_pasta`, `i.pumpkin_soup`,
  `i.pasta_with_meatballs`, `i.dumplings`, `i.cavern_explorer_stew`, `i.squid_ink_pasta`, `i.popped_chorus_fruit`,
  `i.chorus_flower`, `i.shulker_bowl`, `i.enderman` (`#endersdelight:enderman_loot`), `i.mite_crust`, `i.cooked_crab`
  (`quark:cooked_crab_leg`), `i.pumpkin_slice`, `i.beef_patty`, `i.glow_shroom`, `i.glow_ink`, `i.ink_sac`,
  `i.any_dough`, `i.cooked_meat`. Fluids `f.ink` (`create_enchantment_industry:ink`), `f.tomato_sauce`.
- Ink fluid source: Create Enchantment Industry turns ink sacs into ink (its own recipes; *unverified*, check JEI).

### Decisions
- Squid ink pasta: both the mix and the shapeless (recommend both; the mix is the efficient route, 2 per batch).
- Cheaper / dearer counts are first guesses; revisit with `food/types-and-tuning #1` (soup values, row 8 of that
  sub).

### Gotchas
- `i.enderman` is a tag key with a misleading name (`#endersdelight:enderman_loot`).
- Quark Delight recipes are conditional on Quark; Quark is in the pack, so the outputs exist.

### Tests
- RC-40 Each of the 8 meals has a shapeless grid recipe from cooked ingredients, not automatable in an unheated basin.
- RC-41 Heated mixer with 250 mB ink + 250 mB tomato sauce, 2 pasta, a raw fish, 2 bowls -> 2 squid ink pasta.

---

## meals #5: Feasts
Status: Planned / Work: KubeJS (+ patcherMod for a JEI category, Brewin' port for pizza servings) / id `food/meals #5`

### Goal
(a) Show "use a bowl on a placed feast to plate it" in JEI. (b) Sequenced assembly for roast chicken, shepherd's pie,
honey glazed ham, pizza, popcorn (popcorn box also becomes shaped). (c) Pizza: 8 slices per pizza to match its texture,
dearer dough and cheese.

### Now
- Plating already works in game: FD `FeastBlock.takeServing` (decompiled, FD 1.2.4) gives `servingItem` when the held
  stack matches the serving's crafting remainder (a bowl for FD's plates), 4 servings, then leftovers.
- Feast recipes (verified, all shapeless in FD 1.2.4): `farmersdelight:roast_chicken_block` (onion, eggs, bread,
  carrot x2, cooked chicken, baked potato x2, bowl), `farmersdelight:shepherds_pie_block` (row 3),
  `farmersdelight:honey_glazed_ham_block` (sweet berries x4, honey bottle, smoked ham, cooked rice x2, bowl).
  Popcorn: `corn_delight:popcorn_box` shapeless (caramel popcorn x4, paper).
- Pizza: pack shaped `creativeengineering:shaped/pizza` (cooking.js lines 176-178: `' f '/'ptp'/' d '`, 1 cheese
  wedge, 2 toppings, tomato sauce, 1 dough). The Brewin' port's `PizzaBlock`
  (`development/backportedBrewin/src/main/java/umpaz/brewinandchewin/common/block/PizzaBlock.java` line 29) has
  `SERVINGS` 0-3 (4 slices), 4 shapes, models `pizza`, `pizza_slice1..3`; `brewinandchewin:cutting/pizza` gives 4
  slices, `pizza_from_slices` is `pp/pp`; `BnCSlicingModifier` drops the remaining servings on a knife break.

### Build
1. **JEI plating (a).** Two options:
   - Quick (KubeJS client): a JEI information page on each feast block item (`JEIEvents.information`, KubeJS 6;
     *unverified* event name / method `addItem(ids, lines)` in 1902.6.2: test once) saying "Place, then use a bowl on
     it: 4 servings of <serving>".
   - Proper (patcherMod): a `FeastPlatingCategory` registered from a new `@JeiPlugin` next to
     `compat/DragonBreathingJEI.java`: iterate `ForgeRegistries.BLOCKS`, keep `instanceof FeastBlock`
     (`vectorwing.farmersdelight.common.block.FeastBlock`, public fields `servingItem`, method
     `getServingItem(BlockState)`, `getMaxServings()` = 4), one recipe per block: inputs block item + the serving's
     `getCraftingRemainingItem()`, output serving x4. Catalyst: none. Pizza and quiche (Brewin' `PizzaBlock`, not a
     `FeastBlock`) and Corn Delight's popcorn box / nachos (check their block classes) need their own entries.
   Recommend the JEI category (the row asks for a tab); the info page is a stopgap.
2. **Assemblies (b)** (transition rows `['roast_chicken']` texture `farmersdelight:item/roast_chicken_block`,
   `['honey_glazed_ham']` texture `farmersdelight:item/honey_glazed_ham_block`, `['pizza']` (auto
   `brewinandchewin:item/pizza` exists), `['popcorn_box', 'corn_delight:item/caramel_popcorn']` (the box has no flat
   item sprite); shepherd's pie is row 3):
   - Roast chicken: base `i.bowl`; deploy cooked chicken, baked potato, baked potato, carrot, carrot, bread, onion,
     egg -> `i.roast_chicken_block`.
   - Honey glazed ham: base `i.bowl`; deploy smoked ham, cooked rice, cooked rice, spout `Fluid.of(f.honey, 250)`,
     deploy sweet berries x4 (or 2 loops of 2) -> `i.honey_glazed_ham_block`.
   - Pizza: base `i.dough`; spout `Fluid.of(f.tomato_sauce, 250)`, deploy cheese wedge, topping, topping, cheese wedge
     -> `i.pizza`.
   - Popcorn box: base `i.paper`; deploy caramel popcorn, 4 loops -> `i.popcorn_box`; and the grid recipe becomes
     shaped: remove `corn_delight:popcorn_box`, `R.shaped(i.popcorn_box, ['C C', 'CPC'], { C: i.caramel_popcorn,
     P: i.paper }, e, 'popcorn_box')`.
   Bowl-based first steps: cooked chicken (roast chicken), smoked ham (ham) - unique against the list in row 3
   Gotchas.
3. **Pizza 8 slices (c)** (Brewin' port, new jar `-04` or next): `SERVINGS` 0-7, 8 `SHAPES`, models
   `pizza_slice1..7` and blockstate entries (the top texture already shows 8 slices, per the plan), `takeServing`
   unchanged (1 slice per click), `BnCSlicingModifier` drops `servings + 1`. Pack side: remove
   `brewinandchewin:cutting/pizza`, `R.fdCutting(i.pizza, i.any_knife, Item.of(i.pizza_slice, 8), e, 'pizza')`; remove
   `brewinandchewin:pizza_from_slices`, `R.shaped(i.pizza, ['SSS', 'S S', 'SSS'], { S: i.pizza_slice }, e,
   'pizza_from_slices')`; dearer pizza: e.g. `['fff', 'ptp', 'ddd']` (3 cheese, 3 dough). Slice food (5 / 1.0 in
   `BnCFoods.PIZZA_SLICE`) should halve-ish: a port change or a tuning-config value (`food/types-and-tuning #1`).
   Note in alcohol.md 3.2 (port deviations from 4.5.0).

### Ids and data
- Keys exist: `i.roast_chicken_block`, `i.honey_glazed_ham_block`, `i.shepherds_pie_block`, `i.pizza`,
  `i.popcorn_box`, `i.caramel_popcorn`, `i.smoked_ham`, `i.paper`, `i.dough`, `i.t_pizza_topping`, `i.pizza_slice`,
  `i.any_knife`.
- Feast block registry: FD feasts are `FeastBlock`; the pack's placeable feasts also sit in a blocktags.js list (line
  370-375) for BlockRunner.

### Decisions
- JEI route: patcherMod category (recommend) vs KubeJS info page.
- Pizza: 8 servings in the port (recommend; matches the texture) vs 4 servings that each drop 2 slices (no model work,
  but the texture still shows quarters going).
- Pizza cost: recommend `['fff', 'ptp', 'ddd']` for 8 slices.

### Gotchas
- A feast assembly output is the block item; Create's sequenced assembly handles block items fine.
- The port is a separate jar (`development/backportedBrewin`): build + replace in `mods/`, client and server.
- Bowl stacking issues with plated servings: `food/kitchen #5`.

### Tests
- RC-42 JEI shows the plating tab (or info page) for every feast, with the bowl and 4 servings.
- RC-43 Each feast assembly runs and gives the block; popcorn box is shaped only.
- RC-44 Pizza: 8 slices by hand and by knife; the placed pizza shows 8 stages; 8 slices craft back to a pizza.

---

## meals #6: Glistering melon edible
Status: Planned / Work: KubeJS / id `food/meals #6`

### Goal
`minecraft:glistering_melon_slice` becomes food, and gets heated "gilding" routes like the golden carrot.

### Now
- Vanilla: not edible; crafted from melon slice + 8 gold nuggets; a brewing ingredient.
- Golden Food's `goldenfood:golden_melon_slice` (shaped: melon slice ringed by 8 gold ingots, recipe id
  `minecraft:golden_melon_slice` in that jar, verified) is the edible golden melon: 4 / 1.2, regeneration II 5 s,
  absorption 120 s ([food_catalog.md](../food_catalog.md) "golden").
- Food-value copying exists: [food_values.js](../../kubejs/startup_scripts/cooking/food_values.js) lines 6-8
  (`global.foodValueCopies`, target key <- source key, `ItemEvents.modification`, restart to apply).
- Gilding pattern: golden.js lines 10-14 (golden carrot: heated mixing with 5 nuggets, arc furnace with 3).

### Build
1. `food_values.js`: add `glistering_melon_slice: 'golden_carrot'` (same nugget tier as the golden carrot; the
   golden melon slice is ingot tier). `setFoodProperties` on a non-food item makes it edible (`Item.isEdible` reads the
   food properties; *unverified in game*, test RC-45).
2. golden.js, after the golden carrot:
   ```js
   R.createMixing(i.glistering_melon_slice, [i.melon, Ingredient.of([i.any_nugget_gold]).withCount(5)], e, 'glistering_melon_gilding', 'heated')
   R.ieArcFurnace(i.glistering_melon_slice, i.melon, e, 'glistering_melon_arc_gilding', [], [i.any_nugget_gold, i.any_nugget_gold, i.any_nugget_gold])
   ```
   (Row 9 may swap `i.any_nugget_gold` for a gold-only nugget.)
3. After a `/dumpfood` export, rerun `python3 tools/food_catalog.py` so the slice lands in
   `kubejs/generation_scripts/food/food_types.json` type `golden` (gives the type tag, canning parity and SoL value).

### Ids and data
- `i.glistering_melon_slice`, `i.melon`, `i.golden_carrot`, `i.any_nugget_gold` exist.

### Decisions
- Food values: copy the golden carrot (recommend) or the golden melon slice. Either follows whatever row
  `food/types-and-tuning #3` (golden food values, D2) sets later.
- Keep Golden Food's golden melon slice (ingot tier) alongside: recommend yes, the two tiers mirror carrot / apple.

### Gotchas
- `ItemEvents.modification` is startup: restart, not `/reload`.
- Glistering melon stays a brewing ingredient; eating it uses one.

### Tests
- RC-45 Glistering melon can be eaten (golden carrot values / effects); heated mixer melon + 5 gold nuggets -> 1; arc
  furnace melon + 3 nuggets -> 1; brewing still works.

---

## meals #7: Fish recipes for any fish
Status: Planned / Work: KubeJS / id `food/meals #7`

### Goal
Frying with cooking oil in the pot for any fish; Salmon Tempura becomes Fish Tempura; Fish and Chips from raw potato
slices + any raw fish, plated with paper, with a joke tooltip about the "chips"; PneumaticCraft's chips renamed (fries)
and made from diced potato + oil; every PneumaticCraft deep-frying recipe removed.

### Now (verified)
- PNC deep frying = two Thermopneumatic Processing Plant recipes: `pneumaticcraft:thermo_plant/chips`
  (`#forge:crops/potato` + 100 mB `#forge:plantoil` at 423 K -> 4 `pneumaticcraft:chips`) and
  `pneumaticcraft:thermo_plant/salmon_tempura` (raw salmon tempura + 100 mB plant oil -> salmon tempura).
- PNC crafting: `pneumaticcraft:raw_salmon_tempura` (salmon + sourdough; the pack swaps salmon for
  `#forge:raw_fishes/salmon`, cooking.js lines 272-276), `pneumaticcraft:cod_n_chips` (chips, cooked cod, paper).
- The pack's own deep frying: cooking.js line 271, heated mixing 250 mB `f.plantoil` + raw salmon tempura -> salmon
  tempura (auto id).
- Pot frying precedent: Rustic Delight `rusticdelight:cooking/fried_calamari` (`#rusticdelight:cooking_oil`,
  `rusticdelight:batter`, `#forge:raw_fishes/calamari`, tomato; bowl) and `fried_chicken`.
- Potato slices: `rusticdelight:potato_slices` (knife: potato -> 2) and `moredelight:diced_potatoes` (knife: potato ->
  1; the only member of `#forge:sliced_potatoes`). `food/ingredients #8` merges diced potatoes into Rustic Delight's
  slices.

### Build
1. Remove: `pneumaticcraft:thermo_plant/chips`, `pneumaticcraft:thermo_plant/salmon_tempura`, the pack's line-271
   mixing, `pneumaticcraft:cod_n_chips` (replaced below).
2. Fish tempura: keep the item `pneumaticcraft:salmon_tempura`, rename in
   [namechanges.js](../../kubejs/client_scripts/namechanges.js) (`e.renameItem(i.salmon_tempura, 'Fish Tempura')`,
   `e.renameItem(i.raw_salmon_tempura, 'Raw Fish Tempura')`). Raw tempura: replace the line 272-276 replaceInput with
   `R.remove({ id: 'pneumaticcraft:raw_salmon_tempura' })` + `R.shapeless(i.raw_salmon_tempura, [i.t_food_raw_fish,
   i.batter], e, 'raw_fish_tempura')` (batter in place of sourdough; decision). Frying:
   `R.fdCooking([i.t_cooking_oil, i.raw_salmon_tempura], i.salmon_tempura, e, 'fish_tempura', 0.35, 200)`. Slice &
   Dice gives it a heated mixing copy automatically (cooking.js line 388 note).
3. Fries: rename `pneumaticcraft:chips` to "Fries" (`e.renameItem(i.chips, 'Fries')`); recipe
   `R.fdCooking([i.t_cooking_oil, i.potato_slices, i.potato_slices], Item.of(i.chips, 2), e, 'fries', 0.35, 200)`
   (switch `i.potato_slices` to `i.t_sliced_potatoes` once `food/ingredients #8` puts Rustic's slices in the tag).
4. Fish and chips: rename `pneumaticcraft:cod_n_chips` "Fish and Chips"; pot recipe
   `R.fdCooking([i.t_cooking_oil, i.t_food_raw_fish, i.potato_slices, i.potato_slices], i.cod_n_chips, e,
   'fish_and_chips', 0.35, 200, i.paper)` (container paper = "plated with paper"); plus the cooked shapeless
   `R.shapeless(i.cod_n_chips, [i.chips, i.cooked_fish, i.paper], e, 'fish_and_chips_cooked_manual_only')`.
   Tooltip in [tooltips.js](../../kubejs/client_scripts/tooltips.js) Food section (lines 484-494):
   `[i.cod_n_chips, ["The chips are fries. Don't tell the British.", col.uncm]]` (wording is yours).
5. "Any fish": `i.t_food_raw_fish` (`#creativeengineering:food_type/raw/fish`, 13 whole fish incl. blobfish, flying
   fish, catfish, lionfish, tropical fish) vs `i.raw_fish` (`#forge:raw_fishes`: cod, salmon, tropical fish and their
   slices, calamari, koi / glowfish entries; cross-mod). Decision below.
6. [recipes_cooking.md](../recipes_cooking.md) 2.3: replace the "Salmon tempura" row.

### Ids and data
- Keys exist: `i.salmon_tempura`, `i.raw_salmon_tempura`, `i.chips`, `i.cod_n_chips`, `i.batter`, `i.cooking_oil`,
  `i.potato_slices`, `i.paper`, `i.t_food_raw_fish`, `i.raw_fish`, `i.cooked_fish`. New: `i.t_cooking_oil`,
  `i.t_sliced_potatoes` (A0).
- `#rusticdelight:cooking_oil` = `rusticdelight:cooking_oil` (+ an optional Frycook's canola oil, not installed);
  `food/ingredients #10` plans fish oil as cooking oil: it would join this tag.

### Decisions
- Fish set: recommend `i.t_food_raw_fish` (whole fish only; the pack's type tag, avoids slices and calamari), and leave
  the poisonous ones in (it is fried).
- Raw tempura binder: batter (recommend; Rustic's frying recipes use it) or keep sourdough.
- Names: "Fish Tempura", "Fries", "Fish and Chips" (TODO says "names" is open).

### Gotchas
- A pot recipe with a `container` puts the output in the plate slot only when the container is there: paper must be
  supplied (as the oven's `container`).
- `R.remove` of the thermo plant ids: PNC's recipe ids are `pneumaticcraft:thermo_plant/<name>` (verified file paths).
- Slice & Dice skips ids ending `_manual_only`; don't suffix the pot recipes unless the mixer copy is unwanted.

### Tests
- RC-46 No Thermopneumatic chips or tempura in JEI; pot: cooking oil + 2 potato slices -> 2 Fries; cooking oil + raw
  tempura -> Fish Tempura; cooking oil + any raw fish + 2 slices with paper -> Fish and Chips; tooltip shows.

---

## meals #8: Dog food
Status: Planned / Work: KubeJS / id `food/meals #8`

### Goal
An expensive cooked shapeless recipe for `farmersdelight:dog_food`.

### Now
- Only `farmersdelight:cooking/dog_food` (rotten flesh, bone meal, `#farmersdelight:wolf_prey` = raw chicken / raw
  mutton / rabbit, `#forge:crops/rice`; verified). Bowl comes from the pot. Food type `special`.

### Build
`R.shapeless(i.dog_food, [i.rotten_flesh, i.bonemeal, i.cooked_meat, i.cooked_meat, i.cooked_rice], e,
'dog_food_cooked_manual_only')` (cooked rice brings the bowl; two cooked meats = "expensive"). Optional
`i.t_wolf_prey_cooked` tag (cooked chicken / mutton / rabbit) to mirror the pot's set; recommend `i.cooked_meat`.

### Ids and data
`i.dog_food`, `i.rotten_flesh`, `i.bonemeal`, `i.cooked_meat` (`#forge:cooked_meat`, filled by cookingtags.js lines
40-57), `i.cooked_rice`.

### Decisions
None beyond the meat set (recommend `i.cooked_meat`).

### Gotchas
Cooked rice is a bowl item; no separate bowl.

### Tests
- RC-47 Dog food crafts by hand from the cooked set; not in an unheated basin.

---

## meals #9: Gold food cheap in machines
Status: Planned / Work: KubeJS / id `food/meals #9`

### Goal
Revisit the machine gilding costs once food tuning settles, and stop rose gold counting as gold for golden carrots.

### Now
- [golden.js](../../kubejs/server_scripts/cooking/golden.js): golden carrot grid 8 `i.any_nugget_gold` (line 10-11),
  heated mixing carrot + 5 (line 13), arc furnace carrot + 3 (line 14); golden apple heated mixing apple + 5
  `i.any_ingot_gold` (line 17), arc + 3 (line 18), vanilla grid 8; enchanted golden apple assembly 4 loops of
  `i.any_block_gold` + 250 mB XP + pressing (lines 21-26).
- `i.any_nugget_gold` = `#forge:nuggets/gold`, which the pack extends with `creativeengineering:rose_gold_nugget`
  ([metaltags.js](../../kubejs/server_scripts/tags/metaltags.js) line 40); `#forge:storage_blocks/gold` also gets the
  rose gold block (line 42), so the enchanted golden apple takes rose gold blocks too.
- [recipes_cooking.md](../recipes_cooking.md) F5, F7; [recipes_metal.md](../recipes_metal.md) F5 and TODO
  `recipes/metal #3` (rose gold in gold tags, decision "keep in the block tag?").

### Build
1. Rose gold: if `recipes/metal #3` takes rose gold out of `forge:nuggets/gold`, nothing to do here. Otherwise swap the
   golden food recipes to a gold-only ingredient: `i.nugget_gold` (`minecraft:gold_nugget`) or a new tag
   `creativeengineering:nuggets/pure_gold` (`i.t_nugget_pure_gold`, `#forge:nuggets/gold` minus rose gold via
   `t.addMatching(..., { except: i.nugget_rose_gold })`). Same for `i.any_block_gold` in the enchanted golden apple.
2. Costs: after `food/types-and-tuning #1` / #3 set golden food values, adjust golden.js counts. A first proposal:
   mixing 6 ingots, arc 5 (the arc furnace is the cheap route today at 3).

### Ids and data
`i.nugget_gold`, `i.nugget_rose_gold`, `i.any_nugget_gold`, `i.any_ingot_gold`, `i.any_block_gold`.

### Decisions
- Rose gold: fix in the metal tags (`recipes/metal #3`) or locally here. Recommend locally (a pure-gold tag), since rose
  gold rods in `forge:rods/gold` may be intended.
- Machine discount size: wait for tuning (recommend keeping a discount, smaller than now).

### Gotchas
Row 6's glistering melon gilding should use whatever nugget ingredient this row picks.

### Tests
- RC-48 Golden carrot (grid, mixer, arc) refuses rose gold nuggets; enchanted golden apple refuses rose gold blocks.

---

## meals #10: Shrimp fried rice
Status: Planned / Work: KubeJS / data / id `food/meals #10`

### Goal
`alexsmobs:shrimp_fried_rice` only from the mantis shrimp, with the tooltip "You're telling me a shrimp fried this
rice?".

### Now (verified in Alex's Mobs 1.21.1)
- No crafting recipe makes it (no recipe JSON in Alex's Mobs or the other jars checked).
- Alex's Mobs already has the behaviour: `MantisShrimpAIFryRice` (decompiled). A tamed, not sitting mantis shrimp
  holding items in `#alexsmobs:shrimp_rice_fryables` (= `#forge:crops/rice`) walks to a furnace-type block (or a block in
  a vanilla block tag, likely `#minecraft:campfires`; SRG `BlockTags.f_13087_`, *unverified*) with air above, punches
  it (lighting a furnace) and after 200 ticks swaps its whole held stack for the same count of shrimp fried rice.
- The mantis shrimp has no entity loot table in the jar (drops nothing); tamed by 10-30 tropical fish.
- Food type `meal` (`food_types.json`), dict key `i.shrimp_fried_rice`; can parity row is `/` (no can).

### Build
1. Nothing to remove (confirm with `/dumprecipes`: no recipe outputs `alexsmobs:shrimp_fried_rice`).
2. Tooltip in tooltips.js Food section: `[i.shrimp_fried_rice, ["You're telling me a shrimp fried this rice?",
   col.uncm], ['Hand rice to a tamed Mantis Shrimp next to a furnace', col.norm]]`.
3. Optional JEI info page with the same how-to (see row 5's `JEIEvents.information` note).

### Ids and data
`i.shrimp_fried_rice`, `i.spawn_egg_mantis_shrimp`; tag `#alexsmobs:shrimp_rice_fryables`.

### Decisions
- "Only drops from mantis shrimp": recommend the existing frying behaviour (no loot change). The alternative, a LootJS
  drop on death, would make it a kill reward instead of a tame-and-cook one.

### Gotchas
The shrimp converts the whole held stack at once; fine as a bulk route.

### Tests
- RC-49 Tame a mantis shrimp, give it a stack of rice (sneak-use), stand it next to a furnace: the stack turns into
  shrimp fried rice; tooltip shows both lines.

---

## Built, needs play

None: no `meals` row is at Testing or Complete.

Test ids: [recipes_cooking.md](../recipes_cooking.md) ends at RC-18; this brief proposes RC-30 to RC-49 to stay clear of
other briefs that may claim RC-19 onward (renumber when folding into the writeup).

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
