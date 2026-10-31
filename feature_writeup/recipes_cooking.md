# Cooking recipes: flour and dough, fluids, prepared meals, golden food, ruby chocolate, cultivated meat

Part of backlog row 20 (recipe progression), split per folder. This one covers `kubejs/server_scripts/cooking/`
`cooking.js`, `golden.js`, `chocolate.js` and `cultivated_meat.js`. The rest of the folder has writeups: `baking.js` in
[oven.md](oven.md), `canning.js` in [cans.md](cans.md) and [can_parity.md](can_parity.md); the fried egg and cooking oil
fixes in [food.md](food.md); the sample syringe in [engineering.md](engineering.md) 2.5.

## 0. At a glance

| Part | Status |
|---|---|
| One flour and one dough (Create's), from crushing, pressure chamber, explosions; dough from water or eggs; bread blocks | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3) |
| Cornmeal (new item): from corn and kernels on every crusher and PNC; required in tortillas and cornbread batter; a flour in pancakes, pies and cakes. Pie crust from 3 flour + milk by hand and compacting (×2 when built; the 2026-10-22 baking overhaul makes 3, [oven.md](oven.md)) | DEPLOYED (built 2026-10-19, shipped in 1.0.8; 2.1); recipe level scratch server PASSED 2026-10-22 (SQ-OF-04), RC-15 to RC-17 in game |
| Fluids: tomato sauce and melon juice by squeezer and bottler; sugar → `#forge:sugar` in every recipe | DEPLOYED |
| Prepared food: about 25 meals get shapeless, sequenced assembly or mixing routes; FD's shaped ones removed | DEPLOYED |
| Golden carrot / apple by heated mixing and arc furnace; enchanted golden apple by assembly | DEPLOYED |
| Ruby chocolate with dragon's breath | DEPLOYED |
| Cultivated meat: sample syringe → culture → ×3 by mixing → raw meat on the cutting board | DEPLOYED |
| Removals: vanilla smoker and cake, Quark crop crates, duplicate pie, egg, bread recipes | DEPLOYED; smoker made by Quark's deepslate / blackstone smoker recipes (F1), cake by the baking overhaul's recipes and slice reassembly (F2); both closed 2026-10-22 |
| Meals (TODO `dishes/meals` #1 to #3): heatless grid routes from cooked ingredients (rule in 2.3), all `_manual_only`; per-meal fixes (cooked eggs, tortilla wrap, dearer scrambled eggs, fruit tags, shaped burgers, one quiche); assemblies for 8 burgers, mutton wrap, fruit salad, shepherd's pie, horror lasagna, stuffed potato, crispy skewer, crawling sandwich, steak and potatoes | built, scratch server PASSED 2026-10-22 (SQ-OF-07, SQ-OF-08: ids, inputs, removed ids, one grid recipe per burger), DEPLOYED 1.1.01 (2.3); in game RC-55 to RC-64 |
| Meals (TODO `dishes/meals` #4, #7, #8): grid routes for 8 more pot meals plus dog food; squid ink pasta by two-fluid heated mixing; fish frying in the pot with cooking oil for any whole fish (Fish Tempura, Fries, Fish and Chips), PneumaticCraft's Thermopneumatic frying removed | built, scratch server PASSED 2026-10-22 (SQ-OF-09 to SQ-OF-11), DEPLOYED 1.1.01 (2.3); in game RC-66 to RC-69 |
| Drinks (TODO `food/drinks` #4 to #6): chorus juice fluid and its routes; berry matcha latte in the kettle and a grid recipe that keeps its bottle; glass bottles on CfB milk and cow jars | built, scratch server PASSED 2026-10-22 (SQ-DR-00 to SQ-DR-03: load, recipe ids, jar clicks), DEPLOYED 1.1.01 (2.2); in game RC-71 to RC-74 (JEI, kettle screen, sounds) |
| Bone broth routes and ice cubes (TODO `dishes/ingredients` #4, #5): grid routes for 6 more soups and stews (bone broth) and 7 mod pastas; ice hammered into 4 cubes, IE crusher 3; every ice food takes ice or cubes | built, scratch server PASSED 2026-10-22 (SQ-IN-01, SQ-IN-02), DEPLOYED 1.1.01 (2.3); in game RC-75, RC-76 |
| Cooking fluids (TODO `dishes/ingredients #2`): egg solution (1 egg + 250 mB milk -> 500 mB) with Create routes for dough, cornbread batter, pancakes, batter, scrambled eggs, vegetable omelet, potato salad, two custards, vanilla pudding and quiche; frosting fluid (mixer, compactor, cake spout) | built, scratch server PASSED 2026-10-22 (SQ-FL-01, SQ-FL-02), DEPLOYED 1.1.01 (2.2); in game RC-77, RC-78. Needs a restart (new fluids) |
| Golden food real gold only, smaller machine discount (TODO `dishes/meals #9`) | BUILT 2026-10-21, scratch probe PASSED 2026-10-22, DEPLOYED 1.1.01 (2.4); RC-70 |
| Feast Plating JEI tab (TODO `dishes/meals #5` part a): what a placed feast gives per right-click, the container it takes and its leftovers | BUILT 2026-10-21, DEPLOYED 1.1.01 (2.3); RC-65 |
| Food grinding by hand: every grinding recipe of the pack's own goes through NCH `foodGrinding` (crushing wheels + Millstone + a mortar and pestle craft with the pack's own mortar); other mods' food milling gets mortar crafts from a generated catalogue; hay bale on the Millstone | BUILT 2026-10-21, `mortar_probe.js` 33 checks pass, DEPLOYED 1.1.01 (2.1; the mortar and the catalogue are written up in [kitchen.md](kitchen.md) 2.6) |

- **Pending:** everything built up to 2026-10-22 (cornmeal millstone mirrors, `foodGrinding` mortar crafts, F8
  cornmeal fixes, the meals, fluids, bone broth and ice rows above) DEPLOYED with 1.1.01 (2026-10-22); later rows
  follow the changelogs (1.1.03 server and players, 1.1.04 client only, rest unreleased). No RC test has been ticked
  in game; RC-01 to RC-14 have never been run. Findings F1 to F7 (section 4); F1 and F2
  closed 2026-10-22 (removals kept).
- **Files to open:** [cooking.js](../kubejs/server_scripts/cooking/cooking.js) (lines 1-594; 597-1013 is the old
  `global.CE` copy, deleted 2026-10-22), [golden.js](../kubejs/server_scripts/cooking/golden.js),
  [cultivated_meat.js](../kubejs/data/creative_delight/kubejs/creativeengineering/cultivated_meat.js).
- **Knobs:** none. Server scripts (`/reload`); the meal assemblies' incomplete items and the new tag keys are
  startup (`transition_items.js`, `item_dict.js`): restart.

## 1. Overview

Farmer's Delight and its add-ons, Create Central Kitchen, Create Confectionery, tmted and PneumaticCraft each bring flour, dough, juices and meals, often the same item twice. The pack keeps one of each,
gives every Create and IE machine a route for the common foods (usually a better yield than by hand), and adds two
loops of its own: gilding food with gold in machines, and growing meat from mob samples.

## 2. Reference

### 2.1 Flour, dough, bread

- **One flour, one dough:** tmted's flour and FD's dough are replaced by Create's in every recipe's inputs and outputs
  (`i.in_flour`, `#forge:flour/wheat`, `#forge:dough/wheat`).
- **Flour:** IE crusher wheat → 2, hay bale → 18; crushing wheels and (since 2026-10-21) the Millstone hay bale → 13 +
  25 % 2; mortar and pestle hay bale → 13, wheat → 1 (Create's own wheat milling, through the mortar catalogue); PNC
  pressure chamber wheat → 3 at 1.5 bar; explosion crafting 1 per wheat (loss rate 50).
- **Dough:** flour + water bucket; Bottler 250 mB water per flour, 1000 mB per flour pile → dough block; 3 flour + egg → 6.
  FD's, tmted's and Create Central Kitchen's dough recipes and tmted's slime ball from dough are removed.
- **Cornmeal** (`creativeengineering:cornmeal`, KubeJS item `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cornmeal.js`, recolored-flour texture;
  tags `#forge:flour/corn` and `#creativeengineering:flour_or_cornmeal` = wheat flour or cornmeal). From cooking.md:

  | Machine | Corn | Corn crate (9) | Kernels | Kernel bag (9) |
  |---|---|---|---|---|
  | Create crushing wheels | 1 + 50 % 1 cornmeal (1.5), 25 % 1 kernel | 13 + 50 % 1 (13.5), 2 + 25 % 1 kernel | 33.3 % 1 | 3 |
  | IE crusher (3200 RF) | 2, 50 % 1 kernel (replaces Corn Delight's corn -> kernels + straw) | 18, 4 + 50 % 1 kernel | none (see below) | 4 + 50 % 1 (4.5) |
  | PNC pressure chamber (1.5 bar) | 3 (as wheat -> flour) | - | 4 kernels -> 3 (0.75) | - |
  | PNC explosion (loss 50) | 1 (as wheat -> flour) | - | - | - |

  The IE crusher takes one item and can't roll its main output, so a single kernel (0.5) has no recipe; the bag
  gives the ratio. cooking.md's PNC note "(0.2, 3)" was read as "same as flour", whose recipes are 3 at 1.5 bar and
  1 per item at loss rate 50. The millstone mirrors the four crushing-wheel recipes (corn, crate, kernels, bag; added
  2026-10-20 in c82dd31, not shipped; until then corn had no millstone recipe). Since 2026-10-21 all four are one
  `R.foodGrinding` call each, which also writes the mortar and pestle craft at the crushing wheels' and Millstone's
  exact cornmeal average: corn and wild corn 2 -> 3, kernels 3 -> 1 (byproducts machine-only; the corn ratio is set
  by hand since 2026-10-21, `dishes/ingredients #14`, where the helper would round down to 1). Crates and kernel bags
  have no mortar craft (machine-only since 2026-10-21). Wild corn grinds exactly as corn on the crushing wheels and
  Millstone (1 + 50 % 1 cornmeal, 25 % 1 kernel; 2026-10-21). Corn Delight's corn and wild corn
  milling are removed (wild corn now has the corn recipe above), and no corn recipe gives straw: the knife on corn (2 kernels), on wild corn (1) and the IE crusher on wild corn (1)
  keep their kernels without Corn Delight's straw.
  Corn Delight's raw tortilla (3 corn + water bucket) and cornbread batter (2 corn, milk, egg; mixing with 250 mB
  milk) take cornmeal in place of corn, same counts. The raw tortilla takes a water bucket or a water bottle (the
  bottle comes back through `potion_buckets.js`'s glass-bottle remainder), and washing cornmeal with a fan or
  the IE Bottler (250 mB water, as flour -> dough) gives 1 raw tortilla per cornmeal, in place of Corn Delight's
  washed corn (all 2026-10-21, `dishes/ingredients #14`). Muffins don't take cornmeal yet: noted for the muffin rebuild
  (`baking/baked-goods #2`).
- **Pie crust:** 3 flour or cornmeal in a bowl shape with milk (bucket or bottle) in the middle -> 2; unheated
  compacting 3 flour or cornmeal + 250 mB milk -> 2. FD's 3 wheat + milk -> 1 stays. (An egg in place of the milk was
  asked first; it was dropped because the shapeless 3 flour + egg -> 6 dough craft matches the same grid.)
- **Bread:** FD's furnace and smoker bread removed; dough block → smoke to bread block → saw or knife to 16 bread;
  stonecutter for slab, crustless block, nub (Architect's Palette's own recipes replaced).
- **Sourdough (PNC):** 8 dough + yeast culture bucket; mixer 500 mB yeast culture + 8 dough heated → 8; Bottler 50 mB per
  dough; smoke to sourdough bread, knife to 3 bread.
- **Pasta:** IE press with the wire mold, dough → 2 pasta.

### 2.2 Fluids and sugar

- **Tomato sauce:** IE squeezer 250 mB per tomato, Bottler into a bowl; 4 tomatoes + bowl shapeless. tmted's removed.
- **Melon juice:** squeezer 125 mB per slice, 1250 per melon; Bottler 250 mB per bottle; unheated mixing sugar + 3 slices →
  250 mB. tmted's and Create Central Kitchen's removed.
- **Chorus juice** (TODO `food/drinks #4`): the pack's first own fluid, `creativeengineering:chorus_juice`
  (`development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js`; no block, no bucket, like Central Kitchen's drink fluids; `f.chorus_juice`,
  `#forge:fluid/chorus_juice`). Ender's Delight's item: grid 1 fruit + sugar + bottle (theirs, kept); FD pot 2 fruit +
  sugar -> 2 bottles (`chorus_juice_manual_only`); heated mixer 1 fruit + sugar + 250 mB water -> 250 mB; spout,
  drain and IE Bottling Machine at 250 mB. JEI shows Absorption on the fluid through the drain recipe. Pearl pasta
  already takes the item; the chorus pie spouting step is `baking/baked-goods #4`. Decisions taken from the brief: a
  drink and cooking fluid only (no diluter role yet); the shapeless stays.
- **Egg solution** (TODO `dishes/ingredients #2`): `creativeengineering:egg_solution` (`fluids.js`, tank-only like chorus
  juice; `f.egg_solution`, `#forge:fluid/egg_solution`). Unheated mixer 1 egg + 250 mB milk -> 500 mB; 250 mB stands
  in for one egg and its milk, so the fluid doubles an egg. All uses are Create recipes in one cooking.js section
  ("Egg solution", after chorus juice): unheated mixer 3 flour + 250 -> 6 Create dough, 2 cornmeal + 250
  -> 3 cornbread batter, potato + onion + bowl + 250 -> Rustic potato salad; heated mixer 250 + sugar + flour /
  cornmeal / rice -> 5 pancakes, 2 wheat + 2 bowls + 250 -> 2 Rustic batter (row 7 moves batter to flour), bowl + 750
  -> scrambled eggs (the grid takes 3 eggs), cheese + onion + carrot + bowl + 500 -> vegetable omelet, glow berries /
  2 blueberries + sugar + bottle + 250 -> the two custards, dried vanilla pods + sugar + bowl + 250 -> vanilla
  pudding; quiche assembly with one 500 mB spout step in place of the milk spout and two egg deploys
  (`quiche_egg_solution_sequenced_assembly`, same incomplete quiche). Not in the oven (it takes no fluids): muffins,
  tarts, cake base stay on eggs; pie crust stays without a machine route (oven.md 2.6). Decisions (2026-10-22, the
  user): tank-only, 500 mB per egg.
- **Frosting fluid** (TODO `dishes/ingredients #2`): `creativeengineering:frosting` (tank-only, `#forge:fluid/frosting`),
  the Create route to oven.md 2.6's frosting item (baking.js): heated mixer sugar + 250 mB milk -> 500 mB (the grid's
  sugar + 2 milk -> 2); compacting 250 mB -> 1 frosting (fluid-only, like Create's chocolate bar); spout 750 mB on a
  baked cake base -> plain cake (`cake_frosting_spout`, beside the chocolate cake spout). Flavoured cakes stay on the
  item. Beetroot soup as an efficient beetroot was deferred to `ingredients #13` (the user, 2026-10-22).
- **Berry matcha latte** (TODO `food/drinks #5`): with Farmer's Respite loaded Delightful drops its grid lattes, so the
  berry latte had only `food/berry_matcha_latte_from_matcha_latte_neapolitan` (bottle + latte + berries + ice), which
  ate an extra bottle. Removed. New: kettle tier `farmersrespitebrewing/berry_matcha_latte` (latte +
  `#forge:fruits/berries`, no water, glass bottle container, like FR's long / strong teas; the latte's bottle goes
  through the remainder buffer) and grid `shapeless/berry_matcha_latte` (latte + berries + `#forge:ice_cubes`, the
  latte's bottle comes back). New NCH helper `frBrewing(input, output, e, id, opts)` for kettle recipes.
- **Milk from jars** (TODO `food/drinks #6`, [milk_jar.js](../kubejs/data/creative_delight/kubejs/cookingforblockheads/milk_jar.js)): a glass
  bottle on a CfB Milk Jar or Cow in a Jar takes 250 mB out as FD's milk bottle; a milk bottle puts 250 mB back
  (refused when it would overflow). CfB itself only handles buckets. The Cow in a Jar refills at 1 mB/tick (a bottle
  every 12.5 s). The item check uses `Ingredient.of(id).test(stack)`: the first version called `stack.is(id)`, which
  this Rhino build rejects as an ambiguous overload (memory `rhino-fieldandmethods-gotcha`), so the handler would
  have thrown on every click (fixed 2026-10-22 in the doc audit, before any run).
- **Sugar:** every recipe asking for sugar takes `#forge:sugar` (so glycerol-made sugar works); sugar cube from 9 sugar.

### 2.3 Prepared food

| Food | Pack routes |
|---|---|
| Bone broth | pot: bone or bone meal + glow berries / mushrooms / hanging roots / glow lichen → 2; shapeless 1 |
| Beef stew, baked cod stew, noodle soup, chicken soup, cooked rice | shapeless with bone broth; since 2026-10-21 baked cod stew takes a fried egg, baked potato and tomato sauce, chicken soup two cooked veggies (`#creativeengineering:soup_veggie_cooked`) in place of the raw veggie and carrot |
| Orange jam | Fruits Delight's 2-orange pot and heated mixing recipes kept; since 2026-10-23 also pot: 4 orange slices + sugar + lemon slice + bottle; heated mixing: 4 slices + lemon slice + sugar + 100 mB water → 125 mB jam (TODO `baking/sweets #6`) |
| Fruit beignet | removed item since 2026-10-23 (TODO `dishes/meals #19`): every recipe with it as output removed |
| Fried rice, mushroom rice, sinigang | shapeless with cooked rice; fried rice takes 2 fried eggs, mushroom rice a baked potato and a second brown mushroom, sinigang a second cooked fish, so each costs one more than the pot (2026-10-21) |
| Vegetable soup, vegetable noodles, ratatouille, corn soup | heatless shapeless from cooked ingredients (2026-10-21) |
| Chorus stew, endermite stew, crab pasta, pumpkin soup, pasta with meatballs, dumplings, cavern explorer stew, squid ink pasta | heatless shapeless from cooked ingredients, one more main ingredient than the pot (2 popped chorus fruit; 2 mite crusts; 2 cooked crab legs; bone broth + 2 pumpkin slices + cooked pork; 2 beef patties; cooked meat or brown mushroom, 1 dumpling against the pot's 2; bone broth + 2 glow shrooms; cooked fish + 2 ink sacs) (2026-10-21) |
| Acacia blossom soup, carrot soup, creamy onion soup, fig chicken stew, kangaroo stew, miso with bamboo sprouts | shapeless with bone broth, one more main ingredient than the pot (2 acacia blossoms; 3 carrots; 2 onions + a cooked veggie; cooked chicken + 3 figs; 2 cooked kangaroo; 2 dried kelp + cooked fish) (2026-10-22). Not given one: mushroom and rabbit stew, beetroot, bell pepper and adzuki soups, sopa de macaco (shapeless already), bayberry soup (sweet), blazing chili and strider moss stew (Nether) |
| Bell pepper, cheesy, creamy chicken, creamy ham, kangaroo, lobster pasta; duck noodles | heatless shapeless, no broth (bowl, or the tomato sauce's): 3 roasted peppers + a cooked veggie; flaxen wedge + tomato sauce + 2 cooked fish; milk + 2 cooked chicken cuts + onion; milk + smoked ham + 2 onions; 2 cooked kangaroo + tomato sauce; 2 cooked lobster tails + tomato sauce; 2 cooked duck (`#forge:cooked_duck`) + carrot + a cooked veggie (2026-10-22) |
| Ice cubes | Engineer's Hammer on ice on a cutting board, or hammer + ice in the grid -> 4; IE crusher 3; Create's own crushing 3 + 25 % 3; 9 -> 1 ice (Neapolitan). Neapolitan's pickaxe cutting recipe (named a missing item) removed. Every ice food (Neapolitan ice creams, melon / chocolate / kiwi / hamimelon popsicles, hamimelon shaved ice, bellini in pot and mixer) takes `#forge:ice_cubes`, which holds ice and cubes, so the CfB fridge's ice unit counts (2026-10-22) |
| Squid ink pasta ×2 | heated mixing: 250 mB ink + 250 mB tomato sauce, 2 pasta, any raw fish, 2 bowls |
| Dog food | shapeless: rotten flesh, bone meal, 2 cooked meat, cooked rice (brings the bowl) |
| Mutton wrap | shapeless or assembly on a tortilla (FD's bread recipe and CCK's bread assembly removed) |
| Scrambled eggs (Incubation) | 3 eggs + milk + bowl (was 2 eggs) |
| Vegetable omelet (Brewin') | pot takes 2 fried eggs (`R.replaceInput`); shapeless 2 omelettes + cheese wedge + onion + carrot + bowl |
| Bowl of exotic fruits (Quark Delight) | any `#creativeengineering:exotic_fruit` (Fruits Delight fruits, hami melon, melon) in the melon slot |
| Fruit salad | shapeless with `#creativeengineering:fruit_salad_slice` (melon, cantaloupe, hami melon, pineapple, lemon, orange slices) in the melon slots; assembly on a bowl ×2 |
| Burgers: hamburger, cheeseburger, deluxe cheeseburger, simple hamburger, hamburger with egg, loaded hamburger, bison burger, kangaroo burger; Vintage Delight's cheese burger and deluxe burger (2026-10-23: on its cheese slice, beef or vegetarian patty; the deluxe assembly starts on the patty) | shaped only (toppings top, patty middle, bread bottom), any `#forge:bread`, cheese wedges (Delightful's milk versions removed); one assembly each on bread (the egg burger on the patty). Each assembly starts with a different item: CCK's bacon, chicken and egg sandwiches already start on bread with bacon, chicken and a fried egg |
| Shepherd's pie | shapeless with a cheese wedge in place of milk; assembly on a bowl |
| Horror lasagna | shapeless (cooked) and assembly on a bowl ×2 (pasta first, then 250 mB tomato sauce) |
| Stuffed potato | FD's shapeless; assembly on a baked potato (cooked beef, 250 mB milk) |
| Crispy skewer, crawling sandwich, steak and potatoes | shaped only (were shapeless) plus an assembly each |
| Scarlet pierogi | Brewin's pot recipe with beetroot in place of nether wart (`R.replaceInput`); the pack's old pot copy and shapeless recipe were removed in 1.0.8 ([alcohol.md](alcohol.md) 8.6) |
| Pizza, ham and cheese sandwich, quiche (Brewin') | pizza with one dough; sandwich on slices or bread, plus assemblies; quiche: one shaped recipe and one assembly for bacon or mushroom (JEI cycles them), cabbage or a leaf (merged 2026-10-21; [alcohol.md](alcohol.md) 8.6) |
| Pancakes ×3 (Supplementaries) | shapeless sugar, egg, milk, flour, cornmeal or rice; heated mixing ×5 with 250 mB milk |
| Barbecue stick | shapeless or assembly with any cooked meat |
| Crab bars | shapeless ×2 (cooked), pot ×4 (raw), assembly ×10 |
| Grilled salmon, bacon and eggs | shapeless and assembly (FD's own shapeless recipe, kept under its FD id since 2026-10-23, F6) |
| Salmon roll, cod roll | shapeless ×2, assembly ×3 |
| Kelp roll, rice roll medley, roasted mutton chops | assembly |
| Fish stew, cabbage rolls, horror lasagna, pasta with mutton (and heated mixing ×4) | shapeless from cooked ingredients |
| Fish Tempura (PneumaticCraft salmon tempura, renamed) | raw: any whole fish (`#creativeengineering:food_type/raw/fish`) + batter (was salmon + sourdough); pot: cooking oil + raw tempura. PneumaticCraft's Thermopneumatic recipe and the pack's plant-oil mixing removed |
| Fries (PneumaticCraft chips, renamed) | pot: cooking oil + 2 `#forge:sliced_potato` (Rustic Delight slices, More Delight diced potatoes) → 2. Thermopneumatic chips removed |
| Fish and Chips (PneumaticCraft cod 'n' chips, renamed) | pot: cooking oil + any whole fish + 2 sliced potatoes, plated on paper; shapeless fries + cooked fish + paper (`_manual_only`). Tooltip: the chips are fries |
| Blueberry and cranberry muffins | pot recipes on `#forge:dough` ([oven.md](oven.md)) |
| Stuffed shulker, ender paella, pearl pasta (Ender's Delight) | shapeless |
| Vintage Delight (2026-10-23, [vintage_delight.md](vintage_delight.md) step 9) | stuffed burrito on a tortilla (ghost pepper or roasted bell pepper) + assembly; PB & J and cucumber salad `_manual_only`; heatless cheese pasta (pasta + 3 cheese + bowl), pad thai (2 cooked chicken cuts, roasted peanut) and pickle soup (bone broth, 2 pickles, baked potato); no furnace bread from oat dough; oatmeal cookie oven only. No route: oatmeal (no cooked oat), ghostly chili (Nether, as blazing chili) |

**Heatless meal rule** (comment at the top of the "Meals" section of `cooking.js`): a pot meal can also be crafted
shapeless from the cooked form of each raw ingredient (cooked meat, baked potato, tomato sauce, cooked rice, fried egg,
a cooked veggie; carrot, beetroot and mushrooms stay raw), soups and stews with bone broth. The grid costs one more
main ingredient than the pot. Every such recipe, and every shaped meal with an assembly, has an id ending
`_manual_only`, so Create's unheated basin and mechanical crafters skip it; the pack's older cooked shapeless meals (bone
broth, beef stew, noodle soup, sinigang, fish stew, cabbage rolls, horror lasagna, pasta with mutton, stuffed shulker,
ender paella, pearl pasta) were re-id'd the same way. Shepherd's pie already took cooked mutton chops
(`#forge:cooked_mutton`).

A sequenced assembly's first step is checked before plain spout and deployer recipes, so an assembly must not start
with a step that is also an ordinary recipe on its base (the horror lasagna does not start by spouting tomato sauce into
a bowl, which would take over the bowl-filling recipe).

**Feast Plating JEI tab** (patcherMod `compat/jei/FeastPlatingJEI`, `FeastPlatingCategory`). Plating a placed feast
is a block interaction, not a recipe, so JEI showed nothing. The tab has one page per feast: feast item, what to hold
(the container, one per serving and consumed; a knife, kept; or nothing), the servings with counts, and the leftovers.
- Every Farmer's Delight `FeastBlock` in any mod (FD, Corn Delight nachos, Fruits Delight pineapple rice, KubeJS
  Delight feasts) is read from the block: serving item per servings state (the rice roll medley gives three kinds),
  container = the serving's crafting remainder.
- Leftovers are the block's loot table at 0 servings (the last click breaks the plate), read from the table inside
  its mod jar and evaluated for block-state conditions only (no randomness; a datapack override would not show).
- Own block classes: Corn Delight popcorn box (empty hand, paper handed over with the last serving); Brewin' pizza
  (any hand, slices = the block's servings values, so the planned 8-slice port shows 8 without a change); Nether's
  Delight Stuffed Hoglin (two pages: knife twice for 2 hoglin ears, then 9 bowls for 1 snout, 4 ham, 4 roast; 2 bones
  left).
- Not on the tab: pies and Rustic Delight pancakes (eaten or cut in place; FD's cutting-board recipes show pie slices).

Fish on the cutting board give more: salmon and cod → 6 slices + bone meal, catfish → 6 slices.

### 2.4 Golden food (`golden.js`)

| Output | Grid | Heated mixing | Arc furnace |
|---|---|---|---|
| Golden carrot | carrot in 8 gold nuggets | carrot + 6 nuggets | carrot + 5 nuggets |
| Golden apple | vanilla's 8 ingots | apple + 6 gold ingots | apple + 5 ingots |
| Enchanted golden apple | | assembly on a golden apple, 4 loops of a gold block, 250 mB liquid XP and pressing | |

Since 2026-10-21 (TODO `dishes/meals #9`) the machine discount went from 3 and 5 (of 8) to 2 and 3; revisit with food
tuning (`food/types-and-tuning #1`, #3). Rose gold counts as gold in every golden food recipe (decided 2026-10-22: the
forge gold tags hold the pack's rose gold nugget and MCA's ingot and block); a one-day restriction to vanilla gold was
reverted.

### 2.5 Ruby chocolate and cultivated meat

- **Ruby chocolate** (Create Confectionery): heated mixing 250 mB milk + 250 mB dragon's breath + cocoa beans + sugar →
  250 mB (the mod's own recipe removed).
- **Cultivated meat:** sample syringe (iron rod, 4 glass panes, iron plate); a filled syringe + `#creativeengineering:culture_base`
  + slime ball → a culture (beef, pork, poultry, mutton, rabbit, cod, salmon); heated mixing culture + 250 mB diluted
  bone meal + 250 mB plant oil → 3; knife on the cutting board → 4 raw beef / pork / mutton, 3 chicken / rabbit, 8 cod or
  salmon slices. Culture blocks 9 ↔ 1. Since 2026-10-27 the cooking pot grows them too: culture + bone meal + Rustic
  Delight cooking oil → 3 (0.35 xp, 200 ticks; `creative_delight:farmersdelightcooking/<animal>_culture_cooking`, only
  with Rustic Delight on).
- **Creative Cultivation reservation (2026-10-27):** all cultivated-meat code (sample syringe, cultures, culture blocks,
  recipes, tags, lang, textures) is marked for a future own mod, Creative Cultivation, the way brewing and alcohol are
  marked for Creative Brewing ([creative_delight.md](creative_delight.md) 29.5). Items and blocks:
  CD `kubejs_startup/creativeengineering/cultures.js`; tags: `creativeengineering/cultivated_meat_tags.js`.

## 3. What the pack changes

All through `global.NCH` (`R.fdCooking`, `R.fdCutting`, `R.ieSqueezing`, `R.ieBottling`, `R.createMixing`,
`R.createSequencedAssembly` with `R.incomplete`, ...). Slice & Dice turns pot recipes into heated mixing after KubeJS runs
and skips ids ending in `_manual_only`, so recipes that must stay hand-only carry that suffix.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. The vanilla smoker recipe is removed; Quark's make the smoker.** Closed 2026-10-22 (TODO `recipes/cooking #1`):
  removal kept, comment names Quark's recipes (since the 2026-10-25 move: Creative Delight `minecraft/recipes.js` lines 12-14, was `cooking.js` 12); the 2026-10-22 recipe dump lists both Quark smokers
  and `nethersdelight:nether_brick_smoker`, no `minecraft:smoker`. `R.remove` in the same file removes it;
  the pack adds none. Rechecked 2026-10-21: Quark's `quark:building/crafting/furnaces/deepslate_smoker` and
  `blackstone_smoker` (4 logs around a Quark variant furnace, flag on) still make `minecraft:smoker` (Quark 3.4-418
  jar); the old `global.CE` copy (line 575, "minus blackstone") also removed the deepslate one, the live code doesn't. Smoking is how the pack makes bread blocks, sourdough bread and leather; the Quark recipes
  cover that before the Nether (Nether's Delight's nether brick smoker is the late alternative). Same pattern as the
  blast furnace ([recipes_engineering.md](recipes_engineering.md) F1). Check with RC-01;
  [briefs/recipes_cooking.md](briefs/recipes_cooking.md) #1.
- **F2. The vanilla cake recipe is removed as "redundant".** Closed 2026-10-22 (TODO `recipes/cooking #2`): removal kept.
  The baking overhaul ([oven.md](oven.md) 2.6, `baking.js`) now removes every recipe that makes `minecraft:cake`
  except `*_from_slices` and the pack's own, so the four recipes below are gone except the oven's. In the 2026-10-22 dump the
  cake comes from `creativeengineering:baking/cake`, `baking/cake_berry`, `shapeless/cake_manual_only`,
  `shapeless/cake_berry_manual_only` and `farmersdelight:cake_from_slices` (slice reassembly kept, user 2026-10-22).
  Earlier text: Fine if another mod's cake recipe makes
  `minecraft:cake` (Create's or Farmer's Delight's); not checked. RC-02. Jar check 2026-10-21: four recipes make it,
  `farmersdelight:cake_from_milk_bottle` and `neapolitan:cake` (vanilla's grid with milk tags),
  `create:crafting/curiosities/cake` and the oven's `creativeengineering:baking/cake`.
- **F3. Define-once:** `cooking.js` line 9 sets `i.catfish` (the local `i.catfish_slice` was dropped on 2026-10-20
  once the dict's entry was fixed to `alexsdelight:raw_catfish_slice`); `cultivated_meat.js` sets `i.culture_base` ([engineering.md](engineering.md) F8). Fix the
  dict entry and drop the local lines.
- **F4. Old code:** the commented-out `global.CE` copy in `cooking.js` was deleted 2026-10-22 ([TODO](todo/TODO.md) `tooling/conventions #3`).
- **F5. Golden carrots take rose gold nuggets** through `#forge:nuggets/gold` ([recipes_metal.md](recipes_metal.md) F5).
  FIXED 2026-10-21 locally in `golden.js` (vanilla gold only, also ingots and blocks); the metal tags are unchanged.
- **F6.** FIXED 2026-10-23 (TODO `recipes/cooking #3`): comments fixed, crab bar id now
  `creativeengineering:shapeless/crab_bars_manual_only`, the four FD re-adds deleted (FD's ids back), duplicate
  `i.catfish` line dropped. **Comments that don't match:** "Replace shaped recipe w/ shaped" (it's shapeless) on grilled salmon and bacon and
  eggs; "already shapeless?" on the rolls (yes: FD's grilled salmon, bacon and eggs, salmon roll and cod roll are
  all shapeless with the same ingredients, so the pack's remove + re-add only renames them); one crab bar id is `creativeengineering/crab_bars_shapeless_manual_only` with a
  slash in the suffix.
- **F7. Golden apples in machines** cost 5 ingots (mixing) or 3 (arc) against 8 in the grid; the arc furnace is the cheap
  route. Intended progression reward; worth a look with food tuning. 2026-10-21: now 6 (mixing) and 5 (arc), still a
  discount; final numbers with food tuning.

- **F8. Cornmeal fixes (scratchpad 2026-10-21, a priority). BUILT 2026-10-21** ([TODO](todo/TODO.md)
  `dishes/ingredients #14`, static audit in [briefs/dishes_ingredients.md](briefs/dishes_ingredients.md) #14). Done: the raw
  tortilla takes a water bottle as well as a bucket; washing cornmeal or the Bottler makes raw tortillas 1:1; the
  mortar and pestle grinds corn and wild corn 2 -> 3, the crushing wheels' and Millstone's average (the hand route the
  audit found missing; crates and bags are machine-only); wild corn grinds as corn on the crushing wheels and
  Millstone. Left: muffins without cornmeal (noted on `baking/baked-goods #2`, the muffin rebuild).

### Known limits

- **Machine routes skip effects:** a meal made by assembly or mixing is the same item, so nothing to keep; listed only
  because canning's servings don't apply to machine crafts ([cans.md](cans.md) F4).

### Rejected options

- **Keeping tmted's flour:** two flours split every bread recipe in JEI.

### Planned from the scratchpad (2026-10-24)

Rows added to [TODO](todo/TODO.md) on 2026-10-24; nothing is built. Plans are in the linked briefs
([dishes_meals](briefs/dishes_meals.md), [dishes_jams](briefs/dishes_jams.md), [baking_baked-goods](briefs/baking_baked-goods.md),
[food_drinks](briefs/food_drinks.md), [recipes_cooking](briefs/recipes_cooking.md)). The test list above is not extended
until a row is built.

**Drinks**
- [TODO](todo/TODO.md) `food/drinks #9`: Bayberry soup becomes a drink. Open: juice or tea (the notes say both).
- [TODO](todo/TODO.md) `food/drinks #10`: azalea tea recipe and an audit of every tea (Kettle and pot recipes, with `food/drinks #7`
  and `#8`).

**Jams**
- [TODO](todo/TODO.md) `dishes/jams #2`: jam in the pot from 4 fruit, 1 sugar and 1 lemon slice (a lemon gives 5 slices), output
  doubled to 2 jam.
- [TODO](todo/TODO.md) `dishes/jams #3`: recipes use jam, never bottled jam or jelly (mason-jarred jam is the same item in recipes);
  mulberry bread merges into bread with jam (open); jelly bread is renamed Bread with Jam (BUILT 2026-10-30, `namechanges.js`,
  unreleased). Vintage bottled jams removed 2026-10-29; PB and J takes `#forge:jams`.
- [TODO](todo/TODO.md) `dishes/jams #4`: grape and cherry jelly / jam (cherry pie is `baking/baked-goods #13`). BUILT 2026-10-29, unreleased: Green Grape,
  Red Grape and Cherry Jam Jars (Vintage port -09, not yet in `mods/`), pot 4 fruit + sugar + lemon slice -> 2.

**Meals**
- [TODO](todo/TODO.md) `dishes/meals #21`: audit meals that take only vanilla or one specific meat, veggie or fruit; keep where
  the dish is the specific thing (glazed salmon), otherwise open to any. Open: keep or open per meal.
- [TODO](todo/TODO.md) `dishes/meals #22`: overnight oats from 3 oats, 1 fruit slice, 1 berry fruit and 1 milk. The item already
  exists (Vintage Delight, Pickling Jar recipe only); this adds the new recipe. Open: grid, pot or Pickling Jar.
- [TODO](todo/TODO.md) `dishes/meals #23`: cooking oil + 3 eggs gives 3 fried eggs; the pot turns 4 fried eggs into 4 omelettes.
- [TODO](todo/TODO.md) `dishes/meals #24`: extra vegetable omelette recipe with tomato, beetroot, any bell pepper or ghost pepper
  swapped in. Open: which slot.
- [TODO](todo/TODO.md) `merges/dishes #8`: Brewin' pizza renamed Deluxe Pizza (tomato, peppers, mushrooms, cheese, cooked meat or
  fish). Open: whether Vintage's meat pizza merges into it.
- [TODO](todo/TODO.md) `dishes/meals #27`: Tropical Pizza (pineapple and bacon) with a funny tooltip. Open: tooltip text.
- [TODO](todo/TODO.md) `merges/dishes #5`: merge Brewin's pasta with cheese and Vintage's cheese pasta. Open: survivor.
- [TODO](todo/TODO.md) `merges/dishes #6`: merge the two Caesar salads (Veggies Delight "Cesar", Oceanic Delight "Caesar").
- [TODO](todo/TODO.md) `dishes/meals #31`: nut butter sandwich takes more jams and only square bread.
- [TODO](todo/TODO.md) `dishes/meals #32`: remove the mhadjeb (ends the "kept until dough unification" rule in
  [veggies_delight.md](veggies_delight.md) 5.2).
- [TODO](todo/TODO.md) `dishes/meals #36`: cacciatore accepts chicken cuts; a whole-versus-cut audit of the other meals (joins `#21`).
- [TODO](todo/TODO.md) `dishes/meals #37`: review Supplementaries pancake coverings, the JEI screen and the tooltip; jam coverings
  take the jam's colour.

**Baking**
- [TODO](todo/TODO.md) `baking/baked-goods #22`: chorus cookie made from an uncanny cookie (needs End's Delight integrated).
  Open: recipe shape.
- [TODO](todo/TODO.md) `baking/baked-goods #23`: oatmeal cookie from oat dough; new oatmeal raisin cookie (oat dough, 1 raw oat,
  1 grape) textured from the oatmeal cookie.
- [TODO](todo/TODO.md) `baking/baked-goods #24`: remove the shapeless pie-to-crust recipes. No such recipe was found in the
  dumps or jars; check in game first.
- [TODO](todo/TODO.md) `baking/baked-goods #25`: sponge cake renamed sponge pie (Oceanic Delight; its recipe already uses a pie crust).
- [TODO](todo/TODO.md) `baking/baked-goods #26`: honey cake flavour split between honey and honeycomb, 50 / 50.
- [TODO](todo/TODO.md) `baking/baked-goods #27`: a recoloured dough block and a dough texture for each dough (with `#17`).

**Other**
- [TODO](todo/TODO.md) `recipes/cooking #4`: a shulker shell crafts into 12 shulker bowls (was 8). BUILT 2026-10-28 (Creative Delight `endersdelight/recipes.js`), scratch PASS SQ-CM-06.

## 5. File map

- [server_scripts/cooking/](../kubejs/server_scripts/cooking/): `cooking.js`, `golden.js`, `chocolate.js`,
  `cultivated_meat.js` (this writeup); `baking.js` ([oven.md](oven.md)); `canning.js` ([cans.md](cans.md));
  `mortar.js` ([kitchen.md](kitchen.md)).
- `milk_jar.js` (2.2); pack fluids in `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js`.
- Scratch probe: [tools/food_drinks_probe/food_drinks_probe.js](../tools/food_drinks_probe/food_drinks_probe.js)
  (`/fdtest fluids|recipes|ice|milkjar|soupjar|sol|coffee|all`), scratch-only.
- NCH `foodGrinding` / `mortar` / `frBrewing` in [neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js).
- Tags: `tags/cookingtags.js` (food tags these recipes read; [food.md](food.md)).
- patcherMod `compat/jei/FeastPlatingJEI.java`, `FeastPlatingCategory.java` (Feast Plating tab; lang keys
  `creativeengineering.jei.feast_plating*`).

## 6. Offline checks

None.

## 7. In-game test suite

- [ ] **RC-01 Smoker (F1):** JEI `minecraft:smoker` → Quark's deepslate and blackstone smoker (furnace in a plus of 4
  logs) and Nether's Delight's nether brick smoker; no vanilla cobblestone recipe.
- [ ] **RC-02 Cake (F2):** JEI `minecraft:cake` → oven (cake base + 3 frosting, or + 2 frosting + red berry), the same
  two by hand from a baked cake base, and 7 cake slices → cake; no vanilla, FD milk-bottle, Neapolitan or Create grid cake.
- [ ] **RC-03 One flour:** JEI wheat flour → only Create's; tmted's flour absent from recipes.
- [ ] **RC-04 Flour machines:** IE crusher on a hay bale → 18 flour; crushing wheels → 13-15.
- [ ] **RC-05 Dough:** 3 flour + an egg → 6 dough; Bottler with water on a flour pile → dough block.
- [ ] **RC-06 Bread block:** smoke a dough block → bread block; saw → 16 bread.
- [ ] **RC-07 Sourdough:** heated mixer, 8 dough + 500 mB yeast culture → 8 sourdough.
- [ ] **RC-08 Tomato sauce:** squeezer on a tomato → 250 mB; Bottler into a bowl → tomato sauce.
- [ ] **RC-09 Sugar tag:** a recipe asking for sugar accepts another `#forge:sugar` item.
- [ ] **RC-10 Assembly meal:** bacon and eggs by sequenced assembly on a bowl → 1 after 2 loops.
- [ ] **RC-11 Golden carrot:** arc furnace, carrot + 3 gold nuggets → golden carrot.
- [ ] **RC-12 Enchanted golden apple:** assembly on a golden apple, 4 loops → enchanted golden apple.
- [ ] **RC-13 Ruby chocolate:** heated mixer with milk, dragon's breath, cocoa beans, sugar → 250 mB ruby chocolate.
- [ ] **RC-15 Cornmeal item** (tag: scratch server PASSED 2026-10-22, SQ-OF-04; name and texture: in game): shows as "Cornmeal" with the yellow flour texture; in `#forge:flour/corn`.
- [ ] **RC-16 Cornmeal machines** (knife, IE crusher, PNC recipes, Corn Delight ids gone: scratch server PASSED 2026-10-22, SQ-OF-04; crushing wheels, millstone, JEI: in game): crushing wheels on corn give 1-2 cornmeal and sometimes a kernel; the millstone
  gives the same (from the 2026-10-20 change; on 1.0.9 it may take no corn); a knife on corn gives 2 kernels and no straw; IE crusher on corn -> 2 cornmeal (+ kernel half the time), on a crate
  -> 18; PNC pressure chamber 4 kernels -> 3. JEI shows no Corn Delight corn -> kernels IE crusher recipe.
- [ ] **RC-17 Corn recipes** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-04, no raw tortilla or batter recipe takes corn; crafting and washing: in game): raw tortilla and cornbread batter need cornmeal (corn no longer works); splashing corn
  gives nothing, washing cornmeal gives a raw tortilla; pancakes accept cornmeal.
- [ ] **RC-18 Pie crust:** milk bucket (and a milk bottle) in a bowl of 3 flour -> 2 crusts, bucket returned; works with
  cornmeal; compacting 3 flour + 250 mB milk -> 2; 3 flour + egg still makes 6 dough.
- [ ] **RC-19 Grinding mirrors:** JEI cornmeal: crushing wheels, Millstone and a mortar and pestle craft for corn,
  crate, kernels (3 -> 1) and kernel bag; JEI wheat flour: the hay bale on the Millstone and on the mortar (-> 13).
- [ ] **RC-53 Tortilla water:** 3 cornmeal over a water bottle -> 3 raw tortillas and a glass bottle back; over a
  water bucket -> 3 and the bucket back. Bottler: cornmeal + 250 mB water -> 1 raw tortilla.
- [ ] **RC-54 Cornmeal mortar and washing:** 2 corn or 2 wild corn + mortar -> 3 cornmeal (1 corn: no recipe), mortar
  back with 1 damage; a corn crate or kernel bag + mortar: no recipe; cornmeal through a fan in water -> raw tortillas
  1:1; wild corn on crushing wheels and the Millstone -> 1-2 cornmeal, sometimes a kernel.
- [ ] **RC-55 Heatless meals** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-07; crafting and JEI: in game): JEI shows the shapeless routes for vegetable soup, vegetable noodles, ratatouille and
  corn soup; each crafts by hand; the same items in a basin under an unheated mixer do nothing.
- [ ] **RC-56 Cooked inputs** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-07; crafting and JEI: in game): fried rice, baked cod stew, chicken soup and mushroom rice need the cooked ingredients
  (fried egg, baked potato, tomato sauce, cooked veggie); the raw ones no longer craft them.
- [ ] **RC-57 Mutton wrap** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-07; crafting and JEI: in game): needs a tortilla in the grid and on the deployer belt; bread no longer works.
- [ ] **RC-58 Omelet and eggs** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-07; crafting and JEI: in game): vegetable omelet pot needs fried eggs; omelette + cheese + onion + carrot + bowl crafts
  one from 2 omelettes; scrambled eggs take 3 eggs.
- [ ] **RC-59 Fruit bowls** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-07; crafting and JEI: in game): exotic fruits accept a lemon in place of melon; fruit salad accepts a pineapple slice;
  fruit salad assembly on a bowl gives 2.
- [ ] **RC-60 Burgers** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-07; crafting and JEI: in game): each burger has only a shaped grid recipe; JEI shows no milk cheeseburger; each assembly on
  bread works (the egg burger on a beef patty); a mechanical crafter with a burger pattern does nothing; CCK's bacon,
  chicken and egg sandwich assemblies still work.
- [ ] **RC-61 Quiche** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-07; crafting and JEI: in game): one shaped recipe in JEI cycling bacon / mushroom; one assembly; mushrooms first works.
- [ ] **RC-62 Shepherd's pie** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-08; crafting and JEI: in game): 3 cooked mutton chops + a cheese wedge craft it; milk no longer works.
- [ ] **RC-63 Meal assemblies** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-08; crafting and JEI: in game): shepherd's pie, horror lasagna (2), stuffed potato, crispy skewer, crawling sandwich and
  steak and potatoes each run on a belt with deployers / spout; a bowl under a spout of tomato sauce still gives tomato
  sauce.
- [ ] **RC-64 Shaped only** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-08; crafting and JEI: in game): crispy skewer, crawling sandwich and steak and potatoes have only shaped grid recipes.
- [ ] **RC-65 Feast Plating tab:** JEI "Feast Plating" tab lists roast chicken, honey glazed ham, shepherd's pie, rice
  roll medley, stuffed pumpkin, nachos, popcorn box, pineapple fried rice, pizza and stuffed hoglin (2 pages), plus any
  KubeJS Delight feast. Roast chicken: 4 bowls -> 4 roast chicken, leftovers bowl + bone meal; popcorn box: empty
  hand, 4 caramel popcorn, paper "given"; pizza: any hand, 4 slices. Uses (U) on a bowl shows the tab; R on a roast
  chicken plate shows it. One plating in game matches the page. `latest.log` has `[jei] Feast Plating: N page(s)`.
- [ ] **RC-66 Expensive meals** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-09; crafting and JEI: in game): JEI shows a shapeless grid recipe from cooked ingredients for chorus stew, endermite
  stew, crab pasta, pumpkin soup, pasta with meatballs, dumplings (1), cavern explorer stew, squid ink pasta; none
  runs in an unheated basin. Craft one (pasta with meatballs: 2 beef patties + pasta + tomato sauce).
- [ ] **RC-67 Squid ink pasta mixing** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-09; crafting and JEI: in game): heated basin with 250 mB ink + 250 mB tomato sauce, 2 pasta, a raw cod, 2
  bowls -> 2 squid ink pasta.
- [ ] **RC-68 Dog food** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-11; crafting and JEI: in game): rotten flesh + bone meal + 2 cooked meat + cooked rice crafts dog food; not in an unheated basin.
- [ ] **RC-69 Frying** (recipe level: scratch server PASSED 2026-10-22, SQ-OF-10; crafting and JEI: in game): JEI shows no Thermopneumatic chips or tempura and no plant-oil tempura mixing; names read Fish
  Tempura, Raw Fish Tempura, Fries, Fish and Chips. Raw tempura from a pufferfish + batter. Pot: cooking oil + raw
  tempura -> Fish Tempura; cooking oil + 2 potato slices (Rustic) -> 2 Fries, also with diced potatoes; cooking oil +
  a raw cod + 2 slices with paper in the plate slot -> Fish and Chips; fries + cooked fish +
  paper in the grid -> Fish and Chips; its tooltip reads "The chips are fries. Don't tell the British."
- [ ] **RC-70 Gold food in machines:** golden carrot (grid, mixer with 6 nuggets, arc with 5) and golden apple (mixer 6,
  arc 5 ingots) accept rose gold nuggets / MCA rose gold ingots as well as gold; enchanted golden apple assembly accepts
  a rose gold block. (Scratch server 2026-10-22, `tools/golden_food_probe.js`: every gold input of these recipes
  accepts its rose gold twin, 16 of 16; the crafting itself in game.)
- [ ] **RC-71 Chorus juice fluid** (recipe ids: scratch server PASSED 2026-10-22, SQ-DR-01; client: JEI, the basin and spout runs): JEI shows the Chorus Juice fluid with an Absorption line. Heated basin: chorus
  fruit + sugar + 250 mB water -> 250 mB; spout fills a bottle, drain empties one, Bottling Machine fills one.
- [ ] **RC-72 Chorus juice pot** (id: scratch server PASSED 2026-10-22, SQ-DR-01; client: pot and JEI): Cooking Pot, 2 chorus fruit + sugar with bottles -> 2 Chorus Juice; not in a basin
  as a Slice & Dice recipe.
- [ ] **RC-73 Berry matcha latte** (ids, ice tag, bottle remainder: scratch server PASSED 2026-10-22, SQ-DR-02, exactly 2 berry latte recipes; client: JEI, kettle screen, the grid bottle): JEI has no recipe that takes a glass bottle plus a latte. Kettle: matcha latte +
  sweet berries (no water) with a glass bottle -> berry matcha latte, the leftover bottle goes to the leftover slot.
  Grid: latte + berries + ice -> berry latte and one glass bottle back.
- [ ] **RC-74 Milk from jars** (tank and inventory part: scratch server PASSED 2026-10-22, SQ-DR-03, both jars 1000 -> 750 mB and back; client: sound, a real player, creative): glass bottle on a Cow in a Jar with 250 mB or more -> Farmer's Delight milk bottle,
  bottle fill sound; on an empty Milk Jar nothing happens. Milk bottle on a Milk Jar adds 250 mB (refused when
  full). Buckets still fill and empty both jars. Creative keeps the bottle.
- [ ] **RC-75 Broth soups and pastas** (ids: scratch server PASSED 2026-10-22, SQ-IN-01, 13 ids; client: crafting, the basin): grid: bone broth + 2 cooked kangaroo + carrot + beetroot -> kangaroo stew; bone
  broth + milk + 3 carrots -> carrot soup; 2 cooked lobster tails + pasta + tomato sauce -> lobster
  pasta; 2 cooked duck + pasta + carrot + baked potato + bowl -> duck noodles. The kangaroo stew items in an unheated
  basin do nothing.
- [ ] **RC-76 Ice cubes** (ids and ice inputs: scratch server PASSED 2026-10-22, SQ-IN-02; client: JEI, the hammer on the board, the CfB book): JEI shows no pickaxe ice cutting. Hammer on ice on a cutting board -> 4 ice cubes (hammer
  loses 1); hammer + ice in the grid -> 4; IE crusher ice -> 3. Vanilla ice crafts a Neapolitan ice cream and a melon
  popsicle (Abnormals Delight's recipe) takes ice cubes; an ice cream crafts in the CfB crafting book with ice only from a fridge with the ice unit.
  9 cubes still make 1 ice.
- [ ] **RC-77 Egg solution** (fluid, tag and ids: scratch server PASSED 2026-10-22, SQ-FL-01; client: JEI, the machine runs): after a restart JEI shows the Egg Solution fluid. Unheated basin: egg + 250 mB milk ->
  500 mB. Unheated: 3 flour + 250 mB -> 6 dough; 2 cornmeal + 250 mB -> 3 cornbread batter. Heated: sugar + flour +
  250 mB -> 5 pancakes; bowl + 750 mB -> scrambled eggs. Quiche assembly in JEI has a second recipe with one egg
  solution spout step; run it on a pie crust -> quiche. No KubeJS errors for cooking.js in `logs/kubejs/server.log`.
- [ ] **RC-78 Frosting fluid** (fluid, tag and ids: scratch server PASSED 2026-10-22, SQ-FL-02; client: the machine runs, JEI): heated basin sugar + 250 mB milk -> 500 mB frosting; compactor over a basin with
  250 mB -> 1 Frosting item; spout 750 mB on a Baked Cake Base -> Cake. JEI lists the Frosting fluid next to the item.
- [ ] **RC-14 Cultivated beef:** cow sample syringe + culture base + slime ball → beef culture; heated mixer with diluted
  bone meal and plant oil → 3; knife → 4 raw beef.
- [ ] **RC-82 Culture in the cooking pot:** JEI beef culture → U: a cooking pot recipe beef culture + bone meal +
  cooking oil → 3 beef cultures (`creative_delight:farmersdelightcooking/beef_culture_cooking`); same for the other 6
  cultures. Cook one on a lit stove → 3 cultures.
- [ ] **RC-79 Orange jam from slices:** JEI orange jam → R: the 2-orange pot recipe and a pot recipe with 4 orange
  slices + sugar + lemon slice (bottle); a heated basin with 4 slices + lemon slice + sugar + 100 mB water gives 125 mB
  orange jam. Both 2-orange recipes still there.
- [ ] **RC-80 FD ids and crab bars:** JEI grilled salmon, bacon and eggs, salmon roll, cod roll → one shapeless recipe
  each under `farmersdelight:` (F3 + advanced tooltips show the id), plus the sequenced assembly; crab bars: shapeless
  `creativeengineering:shapeless/crab_bars_manual_only` (2), not offered by a Create mixer.
- [ ] **RC-81 Beignet gone, egg fried rice:** JEI search "beignet" → nothing; `/give` one → red REMOVED line. Fried
  rice shows as "Egg Fried Rice", placed on a block as "Placed Egg Fried Rice".

- [ ] **RC-170 Any bread (baking/baked-goods #28):** (recipe level: scratch server PASSED r1031 2026-10-31, SQ-BJ-02, -02b; JEI: in game) JEI `#forge:bread` -> U: the burgers, FD chicken / egg / bacon
  sandwiches, More Delight's slice sandwiches, the ham and cheese sandwich (hand and sequenced assembly) all accept a
  bread slice, toast, cornbread, garlic bread and plain bread in their bread slot. A toast-only recipe (cheese toast,
  caviar toast) still asks for toast.
- [ ] **RC-171 Bread does not copy:** (recipe level: scratch server PASSED r1031, SQ-BJ-02, -02b; Canned Bread has no recipe under its own id: client check) a knife on garlic bread or banana bread on the cutting board does nothing; bread
  slice cutting and toasting still take only their own input; Canned Bread still takes plain bread only; the
  Architect's Palette bread block still takes 9 plain bread.
- [ ] **RC-172 Jam bottles gone (dishes/jams #1, #3):** (recipe level: scratch server PASSED r1031, SQ-BJ-01, -01b; "jam bottle" also matches Upgrade Aquatic's mulberry jam bottle, not removed; JEI: in game) JEI search "jam bottle", "relish bottle", "sweet berries jam",
  "glow berries jam" -> nothing; `/give` one -> red REMOVED line; `logs/kubejs/server.log` has no recipe error from
  `vintagedelight/jams.js`. Pepper Jam Jar and Relish Jar remain, each with its pot recipe and no bottle craft.
- [ ] **RC-173 Grape and cherry jam (dishes/jams #4):** (recipe ids: scratch server PASSED r1031, SQ-BJ-01; cooking, placing, eating: in game) JEI Green Grape / Red Grape / Cherry Jam Jar -> R: Cooking Pot,
  4 white grapes (or red grapes, or cherries) + sugar + lemon slice, 2 empty mason jars -> 2 jars; a heated mixer copy
  (Slice & Dice). Cook one: 2 jars out, 2 empty jars used. Each places 1 to 4 per block (sneak-use), shows its green,
  purple or red contents, breaks into the jars it holds; eating one gives 7 hunger, Comfort 5 min, an empty jar back.
  A Supplementaries jar fills from it.
- [ ] **RC-174 Relish is an ingredient:** (tags: scratch server PASSED r1031, SQ-BJ-03) `/kubejs hand` on a Relish Jar: in `#creativeengineering:food_type/ingredient`,
  not in `#forge:jam_jars` or `#vintagedelight:savory_jam`; the Pickling Jar's jam -> vinegar recipe does not accept it.
- [ ] **RC-175 PB and J takes any jam:** grid bread + nut mash bottle + a Fruits Delight jam, or a grape / cherry jam
  jar -> PB and J; the jar comes back as an empty mason jar.

## 8. History

- By 2026-09-27 (initial git commit): the folder, first on `global.CE`, then on `global.NCH`; shipped with 1.0.3.
- 2026-10-02: fried egg restore and cooking oil `_manual_only` ([food.md](food.md)).
- 2026-10-18: muffins on `#forge:dough` ([oven.md](oven.md)).
- 2026-10-18: this writeup; findings F1 to F7; tests RC-01 to RC-14. No code changed.
- 2026-10-19: cornmeal item and recipes, pie crust from flour and milk, pancakes take cornmeal (2.1); RC-15 to RC-18.
- 2026-10-19: cornmeal, pie crust and pancakes STAGED in pack 1.0.8 ([plan_1.0.8.md](plan_1.0.8.md) OV); DEPLOYED
  with 1.0.8 (2026-10-19 10:20).
- 2026-10-20 (c82dd31): millstone mirrors of the four corn crushing recipes; not shipped.
- 2026-10-21: the pack's own food grinding (corn, crate, kernels, bag, hay bale) moved to NCH `foodGrinding`:
  crushing wheels + Millstone + a mortar and pestle craft; the hay bale gained its Millstone recipe; other mods' food
  milling got mortar crafts from a catalogue ([kitchen.md](kitchen.md) 2.6, TODO `cooking/kitchen #10`). RC-19. Not
  shipped.
- 2026-10-21: cornmeal fixes (`dishes/ingredients #14`, F8): raw tortilla takes a water bottle, washing cornmeal ->
  raw tortilla, Bottler cornmeal + 250 mB water -> raw tortilla, mortar corn 2 -> 3 (the Create machines' average),
  crates and bags off the mortar, wild corn grinds as corn on Create machines and the mortar; muffins left for
  `baked-goods #2`. RC-53, RC-54. Not shipped.
- 2026-10-21: meals (TODO `dishes/meals` #1 to #3, [brief](briefs/dishes_meals.md)): heatless meal rule and `_manual_only`
  ids, four new heatless meals, per-meal fixes, shaped burgers, merged quiche, 16 new assemblies (2.3); tags
  `soup_veggie_cooked`, `exotic_fruit`, `fruit_salad_slice`. RC-55 to RC-64. Not shipped.
- 2026-10-21: Feast Plating JEI tab (TODO `dishes/meals #5` part a; patcherMod 1.1.01, not deployed): every FD
  feast block plus popcorn box, pizza and stuffed hoglin, leftovers from the loot table (2.3). Loot evaluation
  checked headless (roast chicken -> bowl + bone meal, hoglin -> 2 bones). RC-65.
- 2026-10-21: meals #4, #7, #8, #9 ([brief](briefs/dishes_meals.md)): grid routes for 8 pot meals and dog food, squid
  ink pasta two-fluid mixing, pot frying for any fish (Fish Tempura, Fries, Fish and Chips; PneumaticCraft frying out;
  Rustic slices in `#forge:sliced_potato`), golden food on vanilla gold with a smaller machine discount (F5, F7).
  RC-66 to RC-70. Not shipped.
- 2026-10-22: drinks #4 to #6 ([brief](briefs/food_drinks.md)): chorus juice fluid (the pack's first, new
  `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js`) with pot, mixer, spout, drain and Bottling Machine routes; berry matcha latte
  in the kettle (new NCH `frBrewing`) and a grid recipe that no longer eats a bottle; glass bottles on CfB milk and
  cow jars (`milk_jar.js`). RC-71 to RC-74. Not shipped.
- 2026-10-22: ingredients #4, #5 ([brief](briefs/dishes_ingredients.md)): bone broth grid routes for 6 soups / stews
  and heatless routes for 7 mod pastas (new key `i.t_cooked_duck`); ice cubes by hammer (4) and IE crusher (3),
  Neapolitan's broken cutting recipe removed, every ice food on `#forge:ice_cubes`. RC-75, RC-76. Not shipped.
- 2026-10-22: F1 and F2 closed (TODO `recipes/cooking #1`, #2): both removals kept, smoker comment fixed, RC-01 and
  RC-02 given expected answers. No recipe change.
- 2026-10-22: ingredients #2 ([brief](briefs/dishes_ingredients.md)): egg solution and frosting fluids (`fluids.js`,
  tank-only); egg solution Create routes for 11 foods, frosting by mixer, compactor and cake spout (2.2). RC-77,
  RC-78. Not shipped.
- 2026-10-22 (doc audit): none of drinks #4 to #6, ingredients #2, #4, #5 has run yet (the bulk scratch run was
  before them). `milk_jar.js` item check fixed (`stack.is(id)` is ambiguous in this Rhino build, now
  `Ingredient.of(id).test(stack)`); the ice food list named `farmersdelight:melon_popsicle`, which doesn't load
  (Abnormals Delight's `abnormals_delight:neapolitan/melon_popsicle` replaces it), now that id. Scratch checks
  queued with `tools/food_drinks_probe/food_drinks_probe.js` (SQ-DR, SQ-IN, SQ-FL); RC-71 to RC-78 marked client or
  scratch per part.
- 2026-10-22: server-side checks of cornmeal (2.1, RC-15 to RC-17) and the meals of 2.3 (TODO `dishes/meals #1` to `#4`,
  `#7`, `#8`: ids, inputs, removed ids, one grid recipe per burger) queued as SQ-OF-04, SQ-OF-07 to SQ-OF-11
  ([testing/scratch_queue.md](testing/scratch_queue.md), probe `tools/older_food_probe.js`, `/ofprobe`). JEI, names and
  crafting by hand stay in game.
- 2026-10-22 (second bulk run): SQ-DR-00 to SQ-DR-03, SQ-FL-01, SQ-FL-02, SQ-IN-01, SQ-IN-02, SQ-OF-04 and SQ-OF-07 to
  SQ-OF-11 all PASS on the scratch server (results in [testing/scratch_queue.md](testing/scratch_queue.md)); the
  client and crafting halves of RC-15 to RC-17, RC-55 to RC-69 and RC-71 to RC-78 stay in game.
- 2026-10-23: F6 fixed (`recipes/cooking #3`); orange jam from slices (`baking/sweets #6`), fruit beignet removed
  (`dishes/meals #19`), fried rice renamed Egg Fried Rice (`dishes/meals #17`). Tests RC-79 to RC-81. Not run in game.
- 2026-10-24: planned scratchpad rows added to section 4 (drinks, jams, meals, baking, shulker bowl); no code changed.
- 2026-10-27: cultures and culture blocks registered by Creative Delight (`kubejs_startup/creativeengineering/cultures.js`,
  ids unchanged), their tags in `creativeengineering/cultivated_meat_tags.js`; cooking pot culture growth added (7 rows);
  all cultivated-meat code reserved for Creative Cultivation (creative_delight.md 29.5). Test RC-82. Not run in game.

- 2026-10-28: planned from the scratchpad: raise coleslaw nutrition (TODO `dishes/meals #42`), rename cucumber pasta to Veggie Pasta with more vegetables and a less minty colour (`dishes/meals #43`). Tropical Pizza stays `dishes/meals #27`. No code changed.

- 2026-10-29: any bread in any bread recipe (`baking/baked-goods #28`, CD `creativeengineering/bread_recipes.js`; ham and
  cheese sandwich in cooking.js). Jams: the jam and relish bottles and Vintage's sweet berry and glow berry jams removed,
  relish out of the jam tags, Green Grape, Red Grape and Cherry Jam Jars added (Vintage port `-09` + CD
  `vintagedelight/jams.js`), PB and J takes any jam. Tests RC-170 to RC-175. Not run in game.
- 2026-10-31: deep scan: F1 points at the smoker removal's current file (Creative Delight `minecraft/recipes.js`); `recipes/cooking #4` marked built (shell -> 12, SQ-CM-06 PASS). No code changed.
- 2026-10-31: scratchpad notes translated into TODO rows (`dishes/ingredients #25`). No code changed.
