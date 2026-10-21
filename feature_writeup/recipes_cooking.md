# Cooking recipes: flour and dough, fluids, prepared meals, golden food, ruby chocolate, cultivated meat

Part of backlog row 20 (recipe progression), split per folder. This one covers `kubejs/server_scripts/cooking/`
`cooking.js`, `golden.js`, `chocolate.js` and `cultivated_meat.js`. The rest of the folder has writeups: `baking.js` in
[oven.md](oven.md), `canning.js` in [cans.md](cans.md) and [can_parity.md](can_parity.md); the fried egg and cooking oil
fixes in [food.md](food.md); the sample syringe in [engineering.md](engineering.md) 2.5.

## 0. At a glance

| Part | Status |
|---|---|
| One flour and one dough (Create's), from crushing, pressure chamber, explosions; dough from water or eggs; bread blocks | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3) |
| Cornmeal (new item): from corn and kernels on every crusher and PNC; required in tortillas and cornbread batter; a flour in pancakes, pies and cakes. Pie crust from 3 flour + milk ×2 by hand and compacting | DEPLOYED (built 2026-10-19, shipped in 1.0.8; 2.1) |
| Fluids: tomato sauce and melon juice by squeezer and bottler; sugar → `#forge:sugar` in every recipe | DEPLOYED |
| Prepared food: about 25 meals get shapeless, sequenced assembly or mixing routes; FD's shaped ones removed | DEPLOYED |
| Golden carrot / apple by heated mixing and arc furnace; enchanted golden apple by assembly | DEPLOYED |
| Ruby chocolate with dragon's breath | DEPLOYED |
| Cultivated meat: sample syringe → culture → ×3 by mixing → raw meat on the cutting board | DEPLOYED |
| Removals: vanilla smoker and cake, Quark crop crates, duplicate pie, egg, bread recipes | DEPLOYED; smoker has no recipe (F1) |

- **Pending:** nothing built and unshipped. RC-01 to RC-14 have never been run. Findings F1 to F7 (section 4); F1 and F2
  need your call.
- **Files to open:** [cooking.js](../kubejs/server_scripts/cooking/cooking.js) (lines 1-433; 436-852 is the old
  `global.CE` copy, commented out), [golden.js](../kubejs/server_scripts/cooking/golden.js),
  [cultivated_meat.js](../kubejs/server_scripts/cooking/cultivated_meat.js).
- **Knobs:** none. Server scripts (`/reload`).

## 1. Overview

Farmer's Delight and its add-ons, Create Central Kitchen, Create Confectionery, tmted and PneumaticCraft each bring flour, dough, juices and meals, often the same item twice. The pack keeps one of each,
gives every Create and IE machine a route for the common foods (usually a better yield than by hand), and adds two
loops of its own: gilding food with gold in machines, and growing meat from mob samples.

## 2. Reference

### 2.1 Flour, dough, bread

- **One flour, one dough:** tmted's flour and FD's dough are replaced by Create's in every recipe's inputs and outputs
  (`i.in_flour`, `#forge:flour/wheat`, `#forge:dough/wheat`).
- **Flour:** IE crusher wheat → 2, hay bale → 18; crushing wheels hay bale → 13 + 25 % 2; PNC pressure chamber wheat → 3
  at 1.5 bar; explosion crafting 1 per wheat (loss rate 50).
- **Dough:** flour + water bucket; Bottler 250 mB water per flour, 1000 mB per flour pile → dough block; 3 flour + egg → 6.
  FD's, tmted's and Create Central Kitchen's dough recipes and tmted's slime ball from dough are removed.
- **Cornmeal** (`creativeengineering:cornmeal`, KubeJS item `startup_scripts/cooking/cornmeal.js`, recolored-flour texture;
  tags `#forge:flour/corn` and `#creativeengineering:flour_or_cornmeal` = wheat flour or cornmeal). From cooking.md:

  | Machine | Corn | Corn crate (9) | Kernels | Kernel bag (9) |
  |---|---|---|---|---|
  | Create crushing wheels | 1 + 50 % 1 cornmeal (1.5), 25 % 1 kernel | 13 + 50 % 1 (13.5), 2 + 25 % 1 kernel | 33.3 % 1 | 3 |
  | IE crusher (3200 RF) | 2, 50 % 1 kernel (replaces Corn Delight's corn -> kernels + straw) | 18, 4 + 50 % 1 kernel | none (see below) | 4 + 50 % 1 (4.5) |
  | PNC pressure chamber (1.5 bar) | 3 (as wheat -> flour) | - | 4 kernels -> 3 (0.75) | - |
  | PNC explosion (loss 50) | 1 (as wheat -> flour) | - | - | - |

  The IE crusher takes one item and can't roll its main output, so a single kernel (0.5) has no recipe; the bag
  gives the ratio. cooking.md's PNC note "(0.2, 3)" was read as "same as flour", whose recipes are 3 at 1.5 bar and
  1 per item at loss rate 50. Corn has no millstone recipe (Corn Delight's corn and wild corn milling removed), and
  no corn recipe gives straw: the knife on corn (2 kernels), on wild corn (1) and the IE crusher on wild corn (1)
  keep their kernels without Corn Delight's straw.
  Corn Delight's raw tortilla (3 corn + water bucket) and cornbread batter (2 corn, milk, egg; mixing with 250 mB
  milk) take cornmeal in place of corn, same counts; the splashing corn -> raw tortilla recipe is removed.
- **Pie crust:** 3 flour or cornmeal in a bowl shape with milk (bucket or bottle) in the middle -> 2; unheated
  compacting 3 flour or cornmeal + 250 mB milk -> 2. FD's 3 wheat + milk -> 1 stays. (An egg in place of the milk was
  asked first; it was dropped because the shapeless 3 flour + egg -> 6 dough craft matches the same grid.)
- **Bread:** FD's furnace and smoker bread removed; dough block → smoke to bread block → saw or knife to 16 bread;
  stonecutter for slab, crustless block, nub (Architect's Palette's own recipes replaced).
- **Sourdough (PNC):** 8 dough + yeast culture bucket; mixer 500 mB yeast culture + 8 dough heated → 8; Bottler 50 mB per
  dough; smoke to sourdough bread, knife to 2 bread.
- **Pasta:** IE press with the wire mold, dough → 2 pasta.

### 2.2 Fluids and sugar

- **Tomato sauce:** IE squeezer 250 mB per tomato, Bottler into a bowl; 4 tomatoes + bowl shapeless. tmted's removed.
- **Melon juice:** squeezer 125 mB per slice, 1250 per melon; Bottler 250 mB per bottle; unheated mixing sugar + 3 slices →
  250 mB. tmted's and Create Central Kitchen's removed.
- **Sugar:** every recipe asking for sugar takes `#forge:sugar` (so glycerol-made sugar works); sugar cube from 9 sugar.

### 2.3 Prepared food

| Food | Pack routes |
|---|---|
| Bone broth | pot: bone or bone meal + glow berries / mushrooms / hanging roots / glow lichen → 2; shapeless 1 |
| Beef stew, baked cod stew, noodle soup, chicken soup, cooked rice | shapeless with bone broth |
| Fried rice, mushroom rice, sinigang | shapeless with cooked rice |
| Scarlet pierogi | Brewin's pot recipe with beetroot in place of nether wart (`R.replaceInput`); the pack's old pot copy and shapeless recipe were removed in 1.0.8 ([alcohol.md](alcohol.md) 8.6) |
| Pizza, ham and cheese sandwich, quiche (Brewin') | pizza with one dough; sandwich on slices or bread, plus assemblies; quiche mushroom takes cabbage or a leaf, plus assemblies (in `cooking.js` since 2026-10-20; [alcohol.md](alcohol.md) 8.6) |
| Pancakes ×3 (Supplementaries) | shapeless sugar, egg, milk, flour, cornmeal or rice; heated mixing ×5 with 250 mB milk |
| Barbecue stick | shapeless or assembly with any cooked meat |
| Crab bars | shapeless ×2 (cooked), pot ×4 (raw), assembly ×10 |
| Grilled salmon, bacon and eggs | shapeless and assembly (FD's shaped removed) |
| Salmon roll, cod roll | shapeless ×2, assembly ×3 |
| Kelp roll, rice roll medley, roasted mutton chops | assembly |
| Fish stew, cabbage rolls, horror lasagna, pasta with mutton (and heated mixing ×4) | shapeless from cooked ingredients |
| Salmon tempura | heated mixing with 250 mB plant oil |
| Blueberry and cranberry muffins | pot recipes on `#forge:dough` ([oven.md](oven.md)) |
| Stuffed shulker, ender paella, pearl pasta (Ender's Delight) | shapeless |

Fish on the cutting board give more: salmon and cod → 6 slices + bone meal, catfish → 6 slices.

### 2.4 Golden food (`golden.js`)

| Output | Grid | Heated mixing | Arc furnace |
|---|---|---|---|
| Golden carrot | carrot in 8 gold nuggets (any `#forge:nuggets/gold`) | carrot + 5 nuggets | carrot + 3 nuggets |
| Golden apple | vanilla's 8 ingots | apple + 5 gold ingots | apple + 3 ingots |
| Enchanted golden apple | | assembly on a golden apple, 4 loops of a gold block, 250 mB liquid XP and pressing | |

### 2.5 Ruby chocolate and cultivated meat

- **Ruby chocolate** (Create Confectionery): heated mixing 250 mB milk + 250 mB dragon's breath + cocoa beans + sugar →
  250 mB (the mod's own recipe removed).
- **Cultivated meat:** sample syringe (iron rod, 4 glass panes, iron plate); a filled syringe + `#creativeengineering:culture_base`
  + slime ball → a culture (beef, pork, poultry, mutton, rabbit, cod, salmon); heated mixing culture + 250 mB diluted
  bone meal + 250 mB plant oil → 3; knife on the cutting board → 4 raw beef / pork / mutton, 3 chicken / rabbit, 8 cod or
  salmon slices. Culture blocks 9 ↔ 1.

## 3. What the pack changes

All through `global.NCH` (`R.fdCooking`, `R.fdCutting`, `R.ieSqueezing`, `R.ieBottling`, `R.createMixing`,
`R.createSequencedAssembly` with `R.incomplete`, ...). Slice & Dice turns pot recipes into heated mixing after KubeJS runs
and skips ids ending in `_manual_only`, so recipes that must stay hand-only carry that suffix.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. The vanilla smoker has no recipe. Your call.** `cooking.js` line 14 removes it ("Remove stone smoker recipe");
  nothing adds one. Smoking is how the pack makes bread blocks, sourdough bread and leather, so a player needs another
  smoker (Nether's Delight's nether brick smoker, a campfire for single items) or loot. Same pattern as the blast furnace
  ([recipes_engineering.md](recipes_engineering.md) F1). Check with RC-01.
- **F2. The vanilla cake recipe is removed as "redundant". Your call.** Fine if another mod's cake recipe makes
  `minecraft:cake` (Create's or Farmer's Delight's); not checked. RC-02.
- **F3. Define-once:** `cooking.js` sets `i.catfish` and `i.catfish_slice` (the dict's `catfish_slice` has the wrong
  namespace, [food.md](food.md)); `cultivated_meat.js` sets `i.culture_base` ([engineering.md](engineering.md) F8). Fix the
  dict entry and drop the local lines.
- **F4. Old code:** lines 436-852 of `cooking.js` are the `global.CE` version, commented out ([TODO](todo/TODO.md) `tooling/conventions #3`).
- **F5. Golden carrots take rose gold nuggets** through `#forge:nuggets/gold` ([recipes_metal.md](recipes_metal.md) F5).
- **F6. Comments that don't match:** "Replace shaped recipe w/ shaped" (it's shapeless) on grilled salmon and bacon and
  eggs; "already shapeless?" on the rolls; one crab bar id is `creativeengineering/crab_bars_shapeless_manual_only` with a
  slash in the suffix.
- **F7. Golden apples in machines** cost 5 ingots (mixing) or 3 (arc) against 8 in the grid; the arc furnace is the cheap
  route. Intended progression reward; worth a look with food tuning.

- **F8. Cornmeal fixes (scratchpad 2026-10-21, a priority).** "Cornmeal fixes" heads the priority list, but what is
  broken isn't written down: run RC-15 to RC-18 to find it. Also wanted: the raw tortilla takes a water bucket or a
  water bottle (bucket only today, 2.1) ([TODO](todo/TODO.md) `food/ingredients #14`).

### Known limits

- **Machine routes skip effects:** a meal made by assembly or mixing is the same item, so nothing to keep; listed only
  because canning's servings don't apply to machine crafts ([cans.md](cans.md) F4).

### Rejected options

- **Keeping tmted's flour:** two flours split every bread recipe in JEI.

## 5. File map

- [server_scripts/cooking/](../kubejs/server_scripts/cooking/): `cooking.js`, `golden.js`, `chocolate.js`,
  `cultivated_meat.js` (this writeup); `baking.js` ([oven.md](oven.md)); `canning.js` ([cans.md](cans.md)).
- Tags: `tags/cookingtags.js` (food tags these recipes read; [food.md](food.md)).

## 6. Offline checks

None.

## 7. In-game test suite

- [ ] **RC-01 Smoker (F1):** JEI `minecraft:smoker` → note any recipe.
- [ ] **RC-02 Cake (F2):** JEI `minecraft:cake` → note which recipes make it.
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
- [ ] **RC-15 Cornmeal item:** shows as "Cornmeal" with the yellow flour texture; in `#forge:flour/corn`.
- [ ] **RC-16 Cornmeal machines:** crushing wheels on corn give 1-2 cornmeal and sometimes a kernel; the millstone
  takes no corn; a knife on corn gives 2 kernels and no straw; IE crusher on corn -> 2 cornmeal (+ kernel half the time), on a crate
  -> 18; PNC pressure chamber 4 kernels -> 3. JEI shows no Corn Delight corn -> kernels IE crusher recipe.
- [ ] **RC-17 Corn recipes:** raw tortilla and cornbread batter need cornmeal (corn no longer works); splashing corn
  gives nothing; pancakes accept cornmeal.
- [ ] **RC-18 Pie crust:** milk bucket (and a milk bottle) in a bowl of 3 flour -> 2 crusts, bucket returned; works with
  cornmeal; compacting 3 flour + 250 mB milk -> 2; 3 flour + egg still makes 6 dough.
- [ ] **RC-14 Cultivated beef:** cow sample syringe + culture base + slime ball → beef culture; heated mixer with diluted
  bone meal and plant oil → 3; knife → 4 raw beef.

## 8. History

- By 2026-09-27 (initial git commit): the folder, first on `global.CE`, then on `global.NCH`; shipped with 1.0.3.
- 2026-10-02: fried egg restore and cooking oil `_manual_only` ([food.md](food.md)).
- 2026-10-18: muffins on `#forge:dough` ([oven.md](oven.md)).
- 2026-10-18: this writeup; findings F1 to F7; tests RC-01 to RC-14. No code changed.
- 2026-10-19: cornmeal item and recipes, pie crust from flour and milk, pancakes take cornmeal (2.1); RC-15 to RC-18.
- 2026-10-19: cornmeal, pie crust and pancakes STAGED in pack 1.0.8 ([plan_1.0.8.md](plan_1.0.8.md) OV); DEPLOYED
  with 1.0.8 (2026-10-19 10:20).
