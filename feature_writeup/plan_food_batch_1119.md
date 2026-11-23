# Plan: food batch 2026-11-19 (cheese, pizza, flavor, seeds, fats)

Owner-selected rows (chat 2026-11-19) from the Dishes, Baking, Food Rules, Drinks and Farming categories of
[TODO.md](todo/TODO.md). Premises were checked against the scripts, jars and dumps on 2026-11-19 before the questions;
false premises are noted per row. Recipes go through `global.NCH`, tags through `global.tags`, ids through
`item_dict.js`; textures only as copies or owner-tool output with a `texture_sources/sources.csv` row. Every built row
gets scratch queue rows and a changelog bullet in the same session (CLAUDE.md "Done means").

## 0. At a glance

<!-- generated:status_index begin open_work sha1=e7a3d26efe11 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 22 open TODO rows (Planned 2, In Progress 4, Testing 16), 1 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `dairy-fats-and-batter #2` | Planned | S C P | Whale oil cauldron route |
| `pickles-and-ferments #17` | Planned | S C P | Pickled garlic item |
| `baking/baked-goods #20` | In Progress | S C P | Pizza food type |
| `dishes/meals #5` | In Progress | S C P T | Feasts |
| `dishes/meals #45` | In Progress | S C P | Broccoli soup: milk to cheese |
| `food/seasoning #3` | In Progress | S~ C P T | New seasonings |
| `baking/baked-goods #19` | Testing | S C P | Quiche baking type |
| `baking/baked-goods #21` | Testing | S C P | Lasagna baking type |
| `dishes/ingredients #10` | Testing | S C P T | Fats and oils: animal fat, blubber, cooking oil |
| `dishes/ingredients #33` | Testing | S C P | Rename cucumber dough to veggie dough |
| `dishes/ingredients #40` | Testing | S P | Edible seed family: sunflower, pumpkin, oat, mixed, corn seeds |
| `dishes/meals #27` | Testing | S C P | Pineapple pizza |
| `dishes/meals #44` | Testing | S C P | Unify the two stuffed pumpkin recipes |
| `dishes/meals #54` | Testing | S C P | Onion soup and creamy onion soup placeable |
| `dishes/meals #60` | Testing | S P | Cucumber salad |
| `food/drinks #14` | Testing | S P | Hot cocoa and apple cider review |
| `food/types-and-tuning #6` | Testing | S P | Vanilla egg -> `#forge:eggs` in recipes |
| `fruit #3` | Testing | S C | Fruit slice yields and reassembly |
| `seasoning #25` | Testing | S C P | Rename the seasoning mechanic to Flavor |
| `seasoning #26` | Testing | S C P | Flavor pairs: garlic + onion, salt + pepper, sugar + ghost pepper |
| `seasoning #27` | Testing | S C P | Sauce flavors: chorus sauce, glowing sauce, ardent sauce, relish |
| `seasoning #28` | Testing | S C P | Onion, garlic and ghost pepper powders, milled |
<!-- generated:status_index end open_work -->

<!-- generated:status_index begin open_work -->
<!-- generated:status_index end open_work -->

## 1. Owner rulings (intake 2026-11-19)

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| R1 | `dishes/meals #27` Tropical Pizza "needs to be added, use cheese pizza texture, pineapple slices and cooked bacon" | clear | New pizza block like Vintage's cheese pizza, textures copied from it; recipe top row pineapple slice, cooked bacon, pineapple slice, then 3 cheese, then dough, tomato sauce, dough | 2026-11-19 | chat + AskUserQuestion 2026-11-19 |
| R2 | `dishes/meals #5` "4 servings x 2 (add recraft from 8 slices too)" | open | All 5 pizzas (cheese, meat, vegan, Supreme, Tropical): 4 bites, each bite cut gives 2 slices; knife cut and loot give 8; 8 slices in a ring recraft one; the 2x2 recrafts are removed | 2026-11-19 | AskUserQuestion 2026-11-19 |
| R3 | `food/seasoning #3` "+2 saturation for now" | open | The mechanic is renamed **Flavor** everywhere (player text, docs, code, NBT, config). Two kinds: **Seasoning** (a dust) and **Sauce**. Every flavor except Grilled gives +2 saturation (replaces its nutrition/heal value). Grilled adds configurable flavor slots (default 2). Pairs garlic + onion, salt + pepper, sugar + ghost pepper give 5 saturation together (an extra 0.5 per flavor) when both are on an eligible food | 2026-11-19 | AskUserQuestion 2026-11-19 (owner free text) |
| R4 | Flavor rename: old seasoned foods | open | Dropped: no read of the old `Seasonings` NBT key; pre-rename seasoned foods become plain food | 2026-11-19 | AskUserQuestion 2026-11-19 |
| R5 | Sauces from existing items | open | New sauce flavors: End's Delight chorus sauce, Hostile Delight glowing sauce and ardent sauce, Vintage relish. The three Barbeque's Delight sauces are sauces. Tomato sauce and apple sauce are not | 2026-11-19 | AskUserQuestion 2026-11-19 |
| R6 | Seasoning items | open | Current items stay seasonings for now (whole onion, garlic). New milled powders: onion powder, garlic powder, ghost pepper powder (separate from Barbeque's chili powder). Textures: palette swap of the Barbeque's Delight chili powder sprite, palette from each crop's sprite, owner confirms the palettes | 2026-11-19 | AskUserQuestion 2026-11-19 |
| R7 | Pepper powder source | open | Stays trade-only (butcher) for now; sources belong to `food/seasoning #12` | 2026-11-19 | AskUserQuestion 2026-11-19 |
| R8 | `baking/baked-goods #19` "double the main filling" | open | Brewin' quiches (meat, fungus, greens) take 2 filling; zucchini quiche stays at 2 cooked zucchini (counts as doubled; the oven recipe has 6 slots) | 2026-11-19 | AskUserQuestion 2026-11-19 |
| R9 | `baking/baked-goods #21` "skip retexture for now" | open | This batch: renames (Lasagna Pan -> Lasagna, Horror Lasagna -> Crimson Lasagna) and cheese rules only (Lasagna: milk -> any cheese except Scarlet; Crimson: Scarlet cheese). Recipes stay in the cooking pot; the oven move waits for the baking dish (`baking/baked-goods #30`) | 2026-11-19 | AskUserQuestion 2026-11-19 |
| R10 | `dishes/meals #54` "does removed onion soup have a placed model? if not, remove this item" | premise | Plain onion soup never existed on 1.19.2 (FD 1.2.4 has none); Display Delight registers an orphan `displaydelight:onion_soup` block whose source food is missing. Remove the orphan block; creamy onion soup is already placeable (`displaydelight:bnc_creamy_onion_soup`), so the row closes after a check | 2026-11-19 | AskUserQuestion 2026-11-19 |
| R11 | `food/drinks #14` warm drinks family | premise | No warm drinks family existed. New family `drink/warm`, tea-like values (1 hunger, 0.2, always drinkable, stack 32, bottle back), own effects kept; members hot cocoa, strong hot cocoa (Respite), Farmer's Delight apple cider (leaves `drink/soda`) | 2026-11-19 | AskUserQuestion 2026-11-19 |
| R12 | `dishes/ingredients #10` scope | open | All four parts this batch: blubber drop and cut to fat; whale oil item and fluid; animal fat cooks into cooking oil; seed oil and fish oil. Blubber drops from the Alex's Mobs cachalot whale only | 2026-11-19 | AskUserQuestion 2026-11-19 |
| R13 | `dishes/meals #45` other milk-as-cheese recipes | open | Also convert: Farmer's Delight sweet berry cheesecake, More Delight creamy pastas (chicken cuts, ham, and the Veggies compat copies), Brewin' fiery fondue pot. Mashed potatoes keep milk. "Adjust output food values to compensate" for the dearer input | 2026-11-19 | AskUserQuestion 2026-11-19 |
| R14 | `dishes/pickles-and-ferments #14` pickled garlic | open | A new pickled garlic item, later, on its own row; zucchini and cucumber "pickle into pickles, not a new item" (already true, check only) | 2026-11-19 | chat + AskUserQuestion 2026-11-19 |
| R15 | `dishes/ingredients #40` seed values | open | 0.5 hunger = 1 nutrition point. Sunflower seeds and corn kernels: 1 nutrition, 0.5 saturation points (modifier 0.25). Pumpkin, oat, mixed seeds: 1 nutrition, 0 saturation. Usable in recipes like trail mix that need a seed for an edible food (an edible-seed tag) | 2026-11-19 | chat + AskUserQuestion 2026-11-19 |
| R16 | `food/types-and-tuning #6` eggs | premise | `#forge:eggs` already holds turtle and parrot eggs. Switch Create echo cake base and Create Factory cake paste to any egg. Fried egg stays vanilla egg (turtle has its own); dread nog keeps the turtle egg | 2026-11-19 | AskUserQuestion 2026-11-19 |
| R17 | `dishes/ingredients #33` "veggie list for veggie pasta is good, ensure green bell pepper slice is usable" | clear | Keep `#creativedelight:veggie_noodle_veg`, add `rusticdelight:bell_pepper_slice_green`; rename Cucumber Dough (and its block) to Veggie Dough. Recolor texture not in this batch | 2026-11-19 | chat 2026-11-19 |
| R18 | `dishes/meals #5` "eating a pizza from the block gives 2 slices as items" | clear | Right-click on a placed pizza without a knife takes a bite off and gives 2 slice items (no direct eating), for all 5 pizzas | 2026-11-19 | chat 2026-11-19 |
| R19 | `dishes/ingredients #10` "any raw fish and any standard seed can be processed into oils" | clear | Any raw fish -> fish oil; any standard seed (`#forge:seeds`) -> seed oil (cooking oil) | 2026-11-19 | chat 2026-11-19 |
| R20 | A2 Grilled | open | Accepted as written: Grilled's flavor slots replace its +2 saturation | 2026-11-19 | AskUserQuestion 2026-11-19 (via the orchestrator) |
| R21 | A3 "compensate" | open | Accepted as written: re-derive the outputs' values upward from the new ingredients | 2026-11-19 | AskUserQuestion 2026-11-19 (via the orchestrator) |
| R22 | A4 blubber cut | open | Accepted as written: a knife cuts a blubber into 2 animal fat | 2026-11-19 | AskUserQuestion 2026-11-19 (via the orchestrator) |
| R23 | A5 Tropical Pizza id | open | Accepted as written: the id is in the Vintage member's namespace (`vintagedelight:`) | 2026-11-19 | AskUserQuestion 2026-11-19 (via the orchestrator) |
| R24 | Wave E powder textures | open | Run `textureswapper/palette_swap.py` on the Barbeque's Delight chili powder sprite with each crop's own sprite palette (onion, garlic, ghost pepper); no further confirm | 2026-11-19 | AskUserQuestion 2026-11-19 (via the orchestrator) |
| R25 | `baking/baked-goods #20` pizza value | open | A whole pizza is worth 1.5 x its old value; slices, diversity and dependent values are updated to match (owner, 2026-11-22) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| R26 | Flavor leftovers and sugar pair (`food/seasoning #3`) | open | Build now: sugar foods for the sugar + ghost pepper pair (the agent picks desserts and lists them for review); seasoning leftovers (dill, cream and onion, ice cubes, placeable popcorn variants); feast sequenced assemblies (roast chicken, shepherd's pie, honey glazed ham, pizza, popcorn; `dishes/meals #5`); cheesecakes (Rustic's three) take a cheese wedge or a milk bottle or bucket; Sweet Berry Pie unchanged (owner, 2026-11-22) | 2026-11-22 | AskUserQuestion 2026-11-22 |
| R27 | R25 pizza slices, food wave report | clear | Pizza slices at 0.75x each (whole pizza 1.5x a 4-slice one) confirmed (owner, 2026-11-22) | 2026-11-22 | coordinator relay 2026-11-22 |
| R28 | R26 sugar + ghost pepper foods | open | Build the agent's list (waffles, sweet rolls, Beetroot Brownie) with every pancake, the Vegetable Pancake included. Cookies, scones and misc baked goods not yet: the owner gives exemptions from the full item list first (owner, 2026-11-22) | 2026-11-22 | coordinator relay 2026-11-22 |
| R29 | `food/seasoning #3` cream and onion | clear | Build it, with any cheese or milk as the cream (owner, 2026-11-22); dill stays skipped. Reversed: Cream and Onion is not a popcorn flavor; the flavor, its craft, the two-ingredient flavor craft support and its popcorn box are removed (owner, 2026-11-23) | 2026-11-23 | coordinator relay 2026-11-22, 2026-11-23 |
| R30 | `food/seasoning #3` placeable popcorn | open | Build the placeable popcorn variants on the existing placeable popcorn textures and models (copies, no edits) (owner, 2026-11-22) | 2026-11-22 | coordinator relay 2026-11-22 |
| R31 | R28 follow-up: sugar on bakes | open | Every listed cookie (29), scone (9) and misc baked good (9) takes sugar as a flavor, not ghost pepper (owner, 2026-11-23) | 2026-11-23 | coordinator relay 2026-11-23 |

**Still open**: R12's cachalot blubber drop is not ruled on 2026-11-22; it stays as built until the owner says otherwise.

**Agent readings, unconfirmed**: none left (A1 became R18; A2 to A5 were accepted as written, R20 to R23).

## 2. Premise corrections found 2026-11-19

- `#45`: Veggies Delight broccoli soup and pasta with broccoli already have a Vintage-compat recipe with `#forge:cheese`
  loaded beside the milk one; the milk versions (pot, the pack grid `veggiesdelight/cooking.js:52`, `:63`, Slice & Dice
  mirrors) are removed rather than converted.
- `#20`: the Supreme Pizza rename is already in `kubejs/client_scripts/namechanges.js:17-18`; left: the stale
  `food_families.md:282` line ("merge into Deluxe Pizza").
- `#19`: both quiches are already oven-only; `food_families.md:285` and `briefs/baking_baked-goods.md:679-700` are stale.
- `#14`: zucchini and cucumber already ferment into `vintagedelight:pickle` (`vintagedelight/recipes.js:41-43`).
- `#6`: turtle and parrot eggs are already in `#forge:eggs`; quiches, coffee cake, fig pudding already take any egg.
- `#54`: no plain onion soup item ever existed; creamy onion soup is already placeable.
- `#14` drinks: no warm drinks family existed.
- `fruit #3`: no 3-slice cantaloupe grid exists (the 3 is whole mini cantaloupe -> 3 slices); the large cantaloupe is
  `fruitsdelight:hamimelon`, the mini one `delightful:cantaloupe`; the 8-slice pineapple cut is the Create saw recipe.
- Briefs `dishes_meals.md` #27 and #8 sections cite stale paths and lines (`backportedVintage`, old shape).

## 3. Waves

### Wave A: server scripts only (`/reload`)

| Order | Row | Build |
|---|---|---|
| A1 | `dishes/meals #45` | Remove the milk broccoli soup and pasta with broccoli (pot, pack grid, Slice & Dice). Milk -> `#forge:cheese` in Veggies Lasagna (any cheese except Scarlet, which `#forge:cheese` already excludes), Corn Delight nachos, FD stuffed potato (the shapeless and the sequenced assembly's 250 mB milk fill -> a cheese deploy), Fungi truffle pasta (pot + grid `fungidelight/cooking.js:35`), plus R13's cheesecake, creamy pastas and fiery fondue pot; S&D mirrors follow. Output values per A3 |
| A2 | `dishes/meals #44` | Remove `farmersdelight:cooking/stuffed_pumpkin_block` and `corn_delight:cooking/stuffed_pumpkin_from_cornbread`; one pot recipe (pumpkin container): tomato sauce, raw potato (`#forge:crops/potato` + sweet potato), `#forge:bread`, `#forge:berries`, `#forge:mushrooms`, `#forge:vegetables` |
| A3 | `dishes/meals #60` | Cucumber salad (`creativedelight:shapeless/cucumber_salad_manual_only`, `vintagedelight/cooking.js:24`): the pickled beetroot and pickled onion slots each also take `vintagedelight:pickle`; raw cucumber kept |
| A4 | `dishes/pickles-and-ferments #14` | Check only (R14); new row for pickled garlic |
| A5 | `food/types-and-tuning #6` | `R.replaceInput` egg -> `i.eggs` on `create_deep_dark:compacting_echo_cake_base` and `create_factory:cake_paste` |
| A6 | `fruit #3` | Cuts: melon 8 (FD cutting), pumpkin 8 (+ knife break loot `farmersdelight/loot.js:17`), large cantaloupe (hamimelon) 8 (knife and saw), mini cantaloupe 4 (`delightful:cutting/cantaloupe`; `delightful/loot.js:12` already 4), lemon 5 + seeds (pack recipes `fruitsdelight/recipes.js:14-23`, knife and saw), orange 5 (knife and saw), pineapple keeps 6 + saplings, remove the 8-slice saw cut (saw gets the 6 + saplings). Whole from slices, all shapeless: melon 9, pumpkin 9, hamimelon 9, mini cantaloupe 4, lemon 6, orange 6, pineapple 9; the shaped 3x3 and 2x2 versions removed |
| A7 | `dishes/ingredients #33` | `veggie_noodle_veg` tag (`vintagedelight/tags.js:14`) adds `rusticdelight:bell_pepper_slice_green` |
| A8 | `baking/baked-goods #19` | Brewin' quiche oven recipes (`brewinandchewin/recipes.js:56-65`): filling slot doubled; zucchini unchanged. Fix `food_families.md:285` and the brief |
| A9 | `baking/baked-goods #21` | Veggies `cooking/lasagna_pan`: milk -> `#forge:cheese`. Crimson Lasagna recipes (mod pot, pack grid `brewinandchewin/recipes.js:69`, assembly `:86-95`): the generic cheese wedge slot -> Scarlet only |
| A10 | `baking/baked-goods #20` | Doc fix only (`food_families.md:282`); then the row's remaining open parts (textures, oven or assembly) stay |

### Wave B: lang and startup (`/reload` for client lang, restart for startup)

| Order | Row | Build |
|---|---|---|
| B1 | `dishes/ingredients #33` | Lang: Cucumber Dough -> Veggie Dough, Cucumber Dough Block -> Veggie Dough Block |
| B2 | `baking/baked-goods #21` | Lang: Lasagna Pan -> Lasagna (+ slice), Horror Lasagna -> Crimson Lasagna (and Display Delight's placed name) |
| B3 | `dishes/ingredients #40` | Startup food properties for pumpkin, oat, mixed seeds (1 / 0); sunflower and corn set to 1 nutrition, 0.25 modifier. New tag `#creativedelight:edible_seeds` (the five); trail mix (`hearthandharvest/recipes.js:76`) takes it instead of `#forge:seeds`; scan other "seed for an edible food" recipes and switch them |
| B4 | `food/drinks #14` | `tools/food_families.py`: new `drink/warm` family and members; rerun the generator; drinks.js values |

### Wave C: Creative Delight Java and data (one CD rebuild, install, restart)

| Order | Row | Build |
|---|---|---|
| C1 | `dishes/meals #5` | Every pizza block's cut gives 2 slices (Vintage and Veggies `PizzaBlock`, Brewin' `PizzaBlock` in the Brewing member: Creative Brewing rebuild too); loot tables 8/6/4/2 by bites; cutting recipes 8; 8-slice ring recraft per pizza, 2x2 removed; using the block without a knife gives 2 slice items (R18) |
| C2 | `dishes/meals #27` | Tropical Pizza block, item and slice in the Vintage member (copy of the cheese pizza registration and models; textures copied from `cheese_pizza_*`, rows in `sources.csv`); recipe per R1 in the Vintage mod-JSON style; name, tooltip "Misnomer, invented in Canada"; 8 slices per C1; item_dict keys; food values from the cheese pizza plus toppings |
| C3 | `dishes/meals #54` | Unregister `displaydelight:onion_soup` (`DisplayFoods.java:32`, `display_blocks.json:21`, `displayable.json:133`); check creamy onion soup places |

### Wave D: fats (`dishes/ingredients #10`; new items, a Common helper; ask A4 first)

1. Blubber item (texture copy of `delightful:animal_fat`), dropped by the cachalot whale (global loot modifier; its loot table is empty), knife cut into animal fat, counts as fat in soap (`creativecompat/kubejs/other/misc_mods.js:20-24`).
2. Whale oil item and fluid (texture copy of `rusticdelight:cooking_oil`): from blubber by Create mixing and a cauldron route (new NCH helper; Creative Common rebuild); whale oil fluid joins `#forge:lubricant`; soap.
3. Animal fat cooks into cooking oil; cooking oil takes over `delightful:animal_oil_bottle`'s crafts.
4. Seed oil: any standard seed (`#forge:seeds`) pressed into cooking oil; fish oil (`alexsmobs:fish_oil`) counts as
   cooking oil, and any raw fish cooks down to it (manual craft stays, less efficient) (R19).
5. Config toggles in `creativedelight-common.toml` for each part (the "all configurable" ruling).

### Wave E: Flavor (`food/seasoning #3` and its new rows; CD Java, rebuild, restart)

1. Rename Seasoning -> Flavor everywhere: Java `food/Seasonings`, NBT key, config keys, `seasonings.js`, `NCH.season`,
   tags `seasonable/*`, lang, JEI titles, docs. No old-NBT read (R4).
2. Kinds: `kind: 'seasoning' | 'sauce'`; sauces: the three Barbeque's sauces plus R5's four.
3. Values: every flavor +2 saturation except Grilled (A2); Grilled adds `grilledFlavorSlots` (default 2).
4. Pairs: garlic + onion, salt (the Salt mod's Salted tag) + pepper, sugar + ghost pepper: 5 saturation together.
5. Powders (R6): onion, garlic, ghost pepper powder items, milled (Create millstone and the pack mill routes); textures
   by `palette_swap.py` from chili powder after the owner confirms the palettes; `sources.csv` rows.

## 4. Order and checks

A -> B -> C -> D -> E. Each wave: `python3 tools/check_all.py --fast --changed`, scratch rows in the feature's `.qb`
file, changelog bullets in `unreleased.md`, row `log` entries, `status_index.py`. Waves C, D and E need jar installs
through `installToMods` (never under a running game without the dialog).

## 5. Build notes

Choices made while building, where the plan or a ruling left a gap (newest wave last).

- Wave A, 2026-11-19 (food agent of the orchestrated session):
  - A1: the pack's heatless grid routes for broccoli soup and pasta with broccoli were converted to cheese rather than
    removed: removing them would leave the two pot meals without the heatless route the meal rule requires. The milk
    pot recipes are removed as planned (the mod's Vintage compat cheese versions stay).
  - A1 / R13 premise: Farmer's Delight's sweet berry cheesecake is the pack's **Sweet Berry Pie** (pie family: crust + 3
    berries + 2 sweet, no milk), so there was no milk to convert; left unchanged. Owner question: should the pie (and
    Rustic's three cheesecakes) take a cheese?
  - A3: "re-derive" used the soup rule draft of `food/types-and-tuning #8` (output hunger about 1.5x the ingredients',
    milk 2, cheese 4): +3 hunger for a bowl dish, +1 per serving for a feast (nachos, lasagna, fiery fondue); the
    saturation modifier kept. Rows in CD startup `creativedelight/cheese_values.js` (applied by
    `food_value_copies.js`); can values copied from the catalog go stale until `/dumpfood` + `gen_can_types.py`.
  - A6: Delightful's 1 -> 3 hand split of the mini cantaloupe stays (the knife gives 4); the mini melon is untouched.
    The lemon section moved from `fruitsdelight/recipes.js` into `creativedelight/fruit_cuts.js`.
  - A4: zucchini and cucumber pickle into `vintagedelight:pickle` (checked); pickled garlic is `pickles-and-ferments #17`.

- Wave B, 2026-11-19:
  - B3: no other food recipe took `#forge:seeds` (scan of every recipe jar): only trail mix and Delightful's stuffed
    cantaloupe moved to `#creativedelight:edible_seeds`. Seed values in CD startup `creativedelight/edible_seeds.js`.
    The new dict key `t_edible_seeds` needed a Creative Common rebuild (installed in wave B).
  - B4: Respite's long and strong apple cider stay out of `drink/warm` (the ruling names FD's cider only). Hot cocoa
    and strong hot cocoa were not foods; they become edible at the tea values, own effects kept.
  - B2: the Lasagna slice keeps its name (Lasagna Slice).
- Wave C, 2026-11-19:
  - C1: the slice rule is one pack script, `creativedelight/pizza.js` (cut 8, ring of 8, the mods' 4-cuts and 2x2 out),
    not per-member JSON. Veggies' vegan pizza had no block loot (knife break through FD's pastry slicing modifier, 4 -
    bites); it now has a block loot table like Vintage's (8/6/4/2 on any break) and its GLM entry is gone. Brewin's
    Supreme Pizza keeps knife-only break loot (its slicing modifier, now 2 a serving left) and gives 2 slices a
    serving by hand. Slice food values are unchanged (a pizza now feeds twice as much); owner question.
  - C1: a hand use of a pizza no longer eats, so Apple Pie and the Food Dictionary skip it: new block tag
    `#creativedelight:serves_slices` read by `PlacedFoodSol.afterUse` (CD Java).
  - C2: Tropical Pizza art: exact copies of the cheese pizza's three PNGs (`tropical_pizza_top`, item, slice), row in
    `texture_sources/sources.csv`. Its recipe JSON carries a `fruitsdelight` mod-loaded condition (pineapple slice).
    Slice values = the meat pizza slice's (5 / 0.75, Nourishment 30 s): cheese pizza slice + 1 for the toppings.
  - C3: the orphan block's assets (blockstate, models, texture, three lang keys) went with the registration.
- Wave D, 2026-11-19:
  - D2 premise gap: there is no cauldron recipe mechanic to hang an NCH helper on (the only cauldron boiling is Hearth
    and Harvest's sap block, Java with its own block state and art). The manual whale oil route is a Cooking Pot recipe
    (2 blubber + bottle -> 1, as Rustic's cooking oil); the cauldron route moved to `dairy-fats-and-batter #2`.
  - Blubber and Whale Oil read Delightful's animal fat and Rustic's cooking oil sprites in place (no copied files); the
    whale oil fluid uses Crafts & Additions' seed oil textures untinted. No color was picked.
  - Cachalot drop count 3-6 (not ruled); D3 merges Delightful's animal oil bottle into Cooking Oil (the survivor takes
    over its crafts, R12); D4 "pressed" became the cooking oil ingredient tag (any seed in Rustic's pot and mixer
    recipes) plus a Spout from Crafts & Additions' seed oil fluid; fish oil joins the cooking oil tag and the item
    slots that took Cooking Oil take the tag.
  - D5 switches: CDConfig `[fats]` (blubber, whaleOil, animalFatOil, seedOil, fishOil), default on; no hand-edited
    config (Forge adds the section on load). The soap list in Compat's `other/misc_mods.js` reads them; the Compat
    jar was not rebuilt (the pack folder copy loads; the corpses agent has uncommitted Compat Java).
- Wave E, 2026-11-19:
  - E1: renamed in code, NBT, recipe type and ids, tags, lang keys, config key and player text (list in
    [seasonings.md](seasonings.md) section 7). Not renamed: the overlay asset path `item/seasoned/` and
    `textureswapper/seasoning/` (asset and tool paths, no player text), Barbeque's own seasoning items (they are
    seasonings, a flavor kind), the TODO family `food/seasoning`, and Compat's `[jei] seasoningCategory` switch (Compat
    config, another session's files; its comment still says "Seasoning page"). Old config `[food] maxSeasonings` is
    replaced by `maxFlavors` (Forge resets it to the default 2; the pack value was the default).
  - E2: `kind` is data only for now (no rule reads it); the four new sauces go on grilled skewers like Barbeque's
    sauces (no foods were ruled; a flavor with no foods gets no craft).
  - E3: "+2 saturation, replaces its nutrition/heal" read as: nutrition 0, heal 0, saturation 2 for every flavor; effects
    (garlic's Speed) and the Barbeque kinds stay. Grilled: no saturation, `addsSlots` gives `[food]
    grilledFlavorSlots` (default 2) past `maxFlavors`.
  - E4: a pair's extra is 1 saturation point on eating (shown in AppleSkin and as a tooltip line); salt is Salt's
    `Salted` tag. Sugar has no foods yet, so sugar + ghost pepper cannot happen until sugar gets foods.
  - E5: the powders are flavor items for onion and garlic (the whole crops still work) and the item of a new Ghost
    Pepper flavor (popcorn, combines with sugar). 2 powder from one crop by `R.foodGrinding` (crushing wheels,
    millstone, mortar). Art by `palette_swap.py --invert` per R24; the crops' green stems came into the palettes
    (previews in the hand-over), owner to judge.
