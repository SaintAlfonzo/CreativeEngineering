# Large Meals: overlap and integration report

Investigation written 2026-10-30 for TODO `largecontent/mod-additions #12` (the Large Meals part); **nothing built**.
Jar: `devmods/toBackport/largemeals-1.20.1-1.3.0.jar`, unpacked to `DecompiledMods/largemeals-1.3.0/`, code in
`DecompiledMods/largemeals-1.3.0-src/` (7 classes). Forge 1.20.1 only, so a **port is needed**. Licence **All Rights
Reserved**: donor-jar route (jar kept unmodified in `largemeals_upstream/`, our code written from behaviour). The code
is thin: every class extends Farmer's Delight (MIT) `FeastBlock` / `ConsumableItem`, and FD 1.19.2-1.2.4 has the same
constructors and `ModItems.foodItem / bowlFoodItem / basicItem` helpers (checked with `javap`). "Large Meals" is not
abbreviated in this file. Test prefix `LGM-`.

**Scope rule:** integration adds no recipe and no mechanic that is not in Large Meals or defined by a pack rule
([recipes_cooking.md](recipes_cooking.md), [food.md](food.md), [food_families.md](food_families.md)), unless the owner
asks for it by name. Upstream bugs are fixed (3.5).

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Inventory: 18 items + 3 feast blocks, no effects of its own (FD Comfort / Nourishment, vanilla Regeneration), 16 recipes, 2 GLMs, 3 block loot tables, 5 Diet tags, 15 advancements | 1, 3 | read from jar and code |
| Upstream bugs | 3.5 | B1-B6, none blocking |
| Overlap | 2 | Chicken Curry name clash with Ender's Delight; custard and pudding families; mutton rack vs FD mutton chops |
| Already wired in the pack | 2.2 | 13 Display Delight `lm_` blocks (excluded prefix), 4 Spice of Life complexity entries (dead today) |
| Fold-in | 4 | 21 steps planned |
| Decisions | 5 | D1-D9 |
| Tests | 8 | LGM-01..15 drafted, none run |

- **Jar:** 156 files, no jarjar. `mods.toml` authors FoundWiz, jamb.
- **Dependencies:** Forge 47+, Minecraft 1.20.1, Farmer's Delight 1.2.4+ (mandatory). Every recipe tag it uses exists
  in the pack (`forge:pasta`, `forge:raw_chicken`, `forge:raw_fishes/cod`, `forge:milk`, `forge:eggs`, `forge:crops/*`,
  `farmersdelight:tools/knives`; checked in `kubejs/exported/tags/item_tags.json`).
- **Pack references:** Display Delight 1.7.0 already carries 13 `lm_` display blocks, left out of the port by
  `EXCLUDED_PREFIXES` ([display_delight.md](display_delight.md) section 4); `defaultconfigs/solapplepie-server.toml`
  lists 4 `largemeals:` complexity entries that warn `Item is not food` today ([food.md](food.md) 4.2 finding 4);
  [names.md](names.md) has the `lm_omurice` placed name. Nothing else; dumps predate any jar, `/dumpall` is step 1.

## 1. Overview

A Farmer's Delight addon of bowl meals: eight pot meals and soups, two desserts, three four-serving feast blocks
(omurice, mushroom pot pie, roasted rack of mutton), and a mutton rack that sheep drop when killed with a knife.

| Fact | Value |
|---|---|
| Mod id | `largemeals` (package `net.foundwiz.largemeals`) |
| Items | 18: 2 mutton racks, 8 bowl meals, 3 feast block items (stack 1), 3 feast servings, 2 desserts |
| Blocks | 3 `FeastBlock`s (cake properties, `servings` 0-4, leftovers on) |
| Effects / potions | none registered |
| Recipes | 16: 12 FD cooking, 2 FD cutting, 1 smoking, 1 shapeless crafting |
| Loot | 3 block tables (feast drops itself at 4 servings, else a bowl); 2 GLMs on sheep (FD `add_item`) |
| Tags | 5 under `data/diet/` (Diet mod, not in the pack) |
| Advancements | 15 (1 "Sheep Butcher", 14 recipe unlocks); the pack drops all advancements |
| Lang | `en_us`, `vi_vn` |
| Tooltips | FD effect tooltips through `ConsumableItem(..., true)` |
| Config | none |

## 2. Overlap (first)

### 2.1 Items

| Large Meals item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Chicken Curry | *overlaps w/ Ender's Delight Chicken Curry* (`endersdelight:chicken_curry`, 7 / 0.9, Nourishment 5 min, End spices, Shulker Bowl) | Same display name, different dish | D2: rename one |
| Raw / Cooked Rack of Mutton | *overlaps w/ vanilla mutton and FD Mutton Chops* (rack cuts to 4 chops) | A second mutton drop from sheep | keep; loot through the pack system (D3) |
| Roasted Rack of Mutton (feast) | *overlaps w/ FD Roasted Mutton Chops* (meal, 14) | Name only; one is a feast | keep |
| Sweet Berry Custard | *overlaps w/ FD Glow Berry Custard, Fruits Delight Blueberry Custard* | Same shape (milk, egg, sugar, berry, bottle); a family member, not a duplicate | keep |
| Rice Pudding | *overlaps w/ Neapolitan Vanilla Pudding, Fruits Delight Fig Pudding* (`dessert/pudding` family) | family member | keep |
| Pasta with Mushroom Sauce | *overlaps w/ More Delight creamy pastas, Fungi's Delight Truffle Pasta, Brewin' Cheesy Pasta* | milk-based pasta family | keep |
| Cod Deluxe | *overlaps w/ FD Baked Cod Stew* (cod + tomato) | different side ingredients (rice, beetroot vs potato, egg) | keep |
| Pufferfish Broth | *overlaps w/ Oceanic Stuffed Pufferfish, Ocean's Fugu Roll* | pufferfish as a cooked dish; broth gives no poison | keep |
| Red Soup | vanilla Beetroot Soup | adds tomato and onion | keep |
| Omurice (feast) | More Delight Omelette, Brewin' Vegetable Omelet | different dish (rice under egg, feast) | keep |
| Mushroom Pot Pie (feast) | FD Shepherd's Pie (feast on pie crust) | different filling | keep |
| Potato Soup, Tomato Egg Soup, Hearty Lunch | none found | | keep |

Dump searched: `kubejs/exported/items_by_mod/` (2026-10-24) for soup, curry, pudding, custard, pie, pasta, mutton,
omelet, pufferfish, cod; Creative Delight members' lang files for ids added after that date.

### 2.2 Systems

- **Display Delight:** 13 `lm_` blocks in 1.7.0 (placed Omurice, Chicken Curry, Rice Pudding, Roasted Mutton Rack and
  the other bowl dishes) are excluded only because the mod was missing; removing `lm_` from `EXCLUDED_PREFIXES` in
  `port_resources.py` and rerunning brings them in (`BlockAssociations` already maps `lm_` -> `largemeals`).
- **Spice of Life:** the 4 complexity entries (Sweet Berry Custard 5, Pufferfish Broth 5, Mushroom Pot Pie 5, Hearty
  Lunch 6) start to apply once the items exist ([food.md](food.md) 4.2, [briefs/food_types-and-tuning.md](briefs/food_types-and-tuning.md) row #4).
- **Feast Plating JEI tab** reads every FD `FeastBlock`, so the three feasts appear without work.
- **Effects:** FD Comfort and Nourishment, vanilla Regeneration; no new effect, nothing for JEED.
- **Loot:** two GLMs on sheep ([loot.md](loot.md) has no sheep rows today).
- **Eggs:** `#forge:eggs` in the pack holds turtle, crocodile, emu, terrapin and Quark parrot eggs; the tag migration is
  open in [food.md](food.md) 3.8. Tomato Egg Soup, Omurice and Sweet Berry Custard use it.
- **Cans and jars:** soups and stews have per-type cans (can parity 2.11) and Supplementaries jar fluids (TODO
  `dishes/ingredients #13`); feasts are cannable under TODO `cans/canning #17`.
- **Diet:** not installed; its 5 tags are dead.
- **Same batch** (`mod-additions #12`): no shared item with Roll Delight ([roll_delight.md](roll_delight.md)) or
  Mineral Delight ([mineral_delight.md](archive/mineral_delight.md)).

## 3. New content

### 3.1 Foods

Hunger / saturation modifier. Bowl meals return a bowl, stack 16 (FD `bowlFoodItem`). Pot time 200 ticks unless noted.

| Item | Hunger / mod | Effect | Cooking Pot (or other) | Note |
|---|---|---|---|---|
| Raw Rack of Mutton | 5 / 0.3 (meat) | | sheep drop (3.3); cuts to 4 FD Mutton Chops | stack 64 |
| Cooked Rack of Mutton | 10 / 0.8 (meat) | | smoker only, 200 ticks; cuts to 4 Cooked Mutton Chops | B2 |
| Pufferfish Broth | 8 / 0.7 | Comfort 1 min | pufferfish, potato, beetroot, onion, milk | |
| Potato Soup | 10 / 0.6 | Comfort 3 min | 2 potato, onion, milk | |
| Red Soup | 10 / 0.6 | Comfort 3 min | tomato, beetroot, onion | |
| Tomato Egg Soup | 10 / 0.6 | Comfort 3 min | tomato, egg, onion | `#forge:eggs` |
| Cod Deluxe | 12 / 0.8 | Nourishment 3 min | cod, beetroot, rice, tomato | |
| Hearty Lunch | 12 / 0.8 | Regeneration 5 s, Nourishment 3 min | 2 bacon, rice, cabbage, carrot, onion | |
| Chicken Curry | 12 / 0.8 | Nourishment 3 min | raw chicken, onion, milk, tomato sauce | D2 |
| Pasta with Mushroom Sauce | 12 / 0.8 | Nourishment 3 min | 2 brown mushroom, milk, onion, pasta | |
| Sweet Berry Custard | 7 / 0.6, always edible, stack 16, glass bottle | Regeneration 5 s | sweet berries, milk, egg, sugar | |
| Rice Pudding | 7 / 0.6 | Regeneration 5 s | rice, milk, sugar | |

The pot recipes name no container. FD 1.2.4's `CookingPotBlockEntity.getContainer()` falls back to the meal's crafting
remainder (bowl or bottle), so they behave on 1.19.2 as on 1.20.1 (bytecode checked).

### 3.2 Feasts

| Feast (block item stack 1) | Made by | Serving (bowl each) | Serving value |
|---|---|---|---|
| Omurice | Cooking Pot, bowl container, 250 ticks: rice, raw chicken, egg, milk, carrot, cabbage | Bowl of Omurice | 12 / 0.8, Nourishment 5 min |
| Mushroom Pot Pie | Cooking Pot, bowl container, 250 ticks: brown mushroom colony, raw chicken, carrot, cabbage, milk, pie crust | Plate of Mushroom Pot Pie | 12 / 0.8, Comfort 5 min |
| Roasted Rack of Mutton | shapeless crafting: 3 cooked rice, 2 carrot, cooked rack, 2 baked potato, bowl | Plate of Roasted Rack of Mutton | 14 / 0.75, Nourishment 5 min |

Four servings each (FD `FeastBlock`), leftovers on. Loot: the block at 4 servings, otherwise a bowl.

### 3.3 Loot

FD `add_item` GLMs on `minecraft:sheep`, killer holding `#farmersdelight:tools/knives`, chance 0.5 + 0.1 per Looting
level: raw rack, or cooked rack when the sheep is on fire.

### 3.4 Data that does nothing in the pack

5 Diet tags; 15 advancements (lang has "Sheep Butcher"); `vi_vn` lang (kept, harmless).

### 3.5 Upstream bugs

| # | Bug | Evidence | Fix in port |
|---|---|---|---|
| B1 | "Plate of Mushroom Pot Pie" and "Plate of Roasted Rack of Mutton" are served into a bowl (FD feast takes the serving's remainder, bowl); "Bowl of Omurice" is right | `en_us.json`, `ModItems` `bowlFoodItem` | name by the container (D6) |
| B2 | Cooked Rack of Mutton has only a smoker recipe (no furnace, no campfire) | `recipes/cooked_mutton_rack.json` | the pack's meat cooking route (step 8) |
| B3 | Roasted Rack of Mutton feast is a grid craft while the other two feasts are pot recipes with a bowl container | `recipes/roasted_mutton_rack_block.json` | not a bug as such; the pack's feast rule (assembly on a bowl) covers it, D7 |
| B4 | Pasta with Mushroom Sauce takes literal `minecraft:brown_mushroom` x2, Hearty Lunch literal `farmersdelight:bacon` x2 | recipe JSONs | tag inputs (step 4) |
| B5 | Diet tags and advancements for systems the pack does not run | 3.4 | not shipped |
| B6 | `#forge:eggs` accepts turtle and parrot eggs in three recipes | pack tag dump | pack egg tag once [food.md](food.md) 3.8 lands |

## 4. Fold-in, in build order

Code goes into a Creative Delight member (memory `creative-delight-routing`, Rustic precedent
[rustic_delight.md](rustic_delight.md) section 5): `development/creativeDelight/members/largemeals` (Gradle
`:backportedLargeMeals`, `bundled_in=creativeDelight`, mod id `largemeals`), donor `largemeals_upstream/<jar>` read as
hidden built-in packs (`UpstreamJars` pin). KubeJS rows in `kubejs/data/creative_delight/kubejs/largemeals/` (server)
and `development/creativeDelight/src/main/resources/kubejs_startup/largemeals/` (startup).

0. **Port (Java):** items and the three feasts on FD's `FeastBlock` / `ConsumableItem` (MIT base classes), our own
   food-value table, feast shapes; GLMs as FD `add_item` data (ours) or per D3. Donor translation: drop recipe
   `category`, the 1.20 creative-tab list (`CREATIVE_TAB_ITEMS`) becomes an FD tab entry.
1. **Dumps and generators:** `/dumpall`, then `/cegen` (memory `dumpall-refresh-workflow`).
2. **Ids:** all 18 items and 3 blocks in `item_dict.js` / block dict.
3. **Tags** via `global.tags`: food types; bowls meals in the soup / stew / meal types, desserts in `dessert/pudding`.
4. **Recipe inputs:** literal mushrooms to `#forge:mushrooms/brown` (or the pack's brown-mushroom key), bacon to
   `#forge:raw_pork` / bacon key, eggs per B6. All recipes rewritten through `global.NCH` (the donor's JSONs are not loaded).
5. **Mob loot:** rack drops through [loot.md](loot.md) (pack runtime loot additions) per D3.
6. **Item merges:** none (D2 is a rename, not a merge).
7. **Potions:** none.
8. **Recipes by pack rules** ([recipes_cooking.md](recipes_cooking.md)): pot meals kept and mirrored by the Mixer;
   heatless rule: one `_manual_only` shapeless per pot meal from cooked ingredients, soups with bone broth, one more main
   ingredient than the pot; feasts get a sequenced assembly on a bowl (shepherd's pie precedent); cooked rack by the
   pack's raw-meat route (furnace, smoker, campfire); cutting recipes kept ([oven.md](oven.md): none bake).
9. **Effects:** none new; JEED text unchanged.
10. **Food types and values:** `food_types.json`; values checked against FD meals of the same ingredient count
    ([food.md](food.md)); the 4 Spice of Life complexity entries kept or retuned (D8).
11. **Food families:** `python3 tools/food_families.py --check`.
12. **Stack sizes** ([stack_sizes.md](stack_sizes.md)): bowl meals 16 as FD; Sweet Berry Custard by the bottle rule
    (custard 32 in the drink family note); feasts 1.
13. **Cans and jars** ([cans.md](cans.md)): soups by the generated per-type can rule (parity 2.11); feasts wait for
    `cans/canning #17`; soups into the Supplementaries jar list.
14. **Compost:** CD `compost.js` defaults for meals (racks as meat, not compostable).
15. **Crops, seasons, worldgen:** none.
16. **Names** ([names.md](names.md)): D2 curry rename, B1 serving names.
17. **Mob and block audit:** feasts: mineable tag as FD feasts, BlockRunner tier, Item Physic, WTHIT servings line
    (FD feasts are covered by the existing crop/feast plugin).
18. **Tooltips and JEI** ([tooltips.md](tooltips.md), [jei_coverage.md](jei_coverage.md)): the rack drop as a JEI info
    page or loot entry; Feast Plating tab automatic.
19. **Placeables:** Display Delight: drop `lm_` from `EXCLUDED_PREFIXES`, rerun `port_resources.py`, rebuild
    `backportedDisplay` (13 blocks) per D5.
20. **Trades and chests:** none upstream; none added.
21. **Release:** `mod_sides.md` row, changelog family "Large Meals", TODO row, plan row; remove the 4 dead SoL warnings
    from [food.md](food.md) 4.2 finding 4.

Ordering: 0 before all; 1 before 2-4; 2 before 3, 4, 8, 10, 19; 19 needs the Large Meals jar on the Display Delight
build classpath only for ids (it resolves by name at runtime), so it can go in the same release.

## 5. Decisions for the owner

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | Port Large Meals? | Yes, as CD member `largemeals`: small, FD-shaped, pack hooks already waiting | port |
| D2 | Two "Chicken Curry" items | Rename Large Meals' to "Tomato Chicken Curry" (Ender's is deployed and keeps its name) | rename Large Meals' |
| D3 | Rack drop from sheep (50 % with a knife) | Keep, as pack loot data rather than a GLM so loot.md lists it | keep as GLM |
| D4 | Values (Chicken Curry 12 vs Ender's 7, soups 10) | Keep upstream; they sit inside FD's range | keep |
| D5 | Turn on the 13 Display Delight `lm_` blocks | Yes, same release | yes |
| D6 | "Plate of ..." served in a bowl | Rename to "Bowl of ..." (container wins) | rename |
| D7 | Roasted Rack feast: grid craft kept? | Keep the grid craft as `_manual_only`, add the assembly on a bowl (pack feast rule) | as recommended |
| D8 | Spice of Life complexity entries (5, 5, 5, 6) | Keep | keep |
| D9 | Eggs: wait for the `#forge:eggs` migration or use `minecraft:egg` now | Wait; use the tag | tag |

Blocking a build: D1, D2.

## 6. File map (planned)

| Path | What |
|---|---|
| `largemeals_upstream/largemeals-1.20.1-1.3.0.jar` | donor, unmodified, sha1-pinned |
| `development/creativeDelight/members/largemeals/` | Java (items, feasts, food values), translator, NOTICE |
| `development/creativeDelight/src/main/resources/kubejs_startup/largemeals/` | dict rows, food types, compost |
| `kubejs/data/creative_delight/kubejs/largemeals/recipes.js` | NCH recipes, heatless crafts, assemblies |
| `development/backportedDisplay/` `port_resources.py` | `lm_` prefix removed |
| `defaultconfigs/solapplepie-server.toml` | unchanged (entries become live) |
| `feature_writeup/large_meals.md` | this file |

## 7. Offline checks (once built)

- `./gradlew :backportedLargeMeals:build --offline`, `:creativeDelight:build`; build guard finds no ARR path in our jar.
- `python3 tools/food_families.py --check`; `python3 tools/changelog_lint.py`.
- Display Delight: `port_resources.py` reports 13 `lm_` blocks added, 0 missing models.

## 8. In-game test suite (drafted)

| ID | Test | Pass |
|---|---|---|
| LGM-01 | Boot with the CD jar | no `Failed to parse` naming `largemeals`; no `Item is not food: largemeals` from Spice of Life |
| LGM-02 | JEI search "@largemeals" | 18 items, each with a recipe or a loot / info source |
| LGM-03 | Cook Potato Soup in the pot with a bowl | soup out; without a bowl the pot holds the meal |
| LGM-04 | Cook Sweet Berry Custard | needs a glass bottle; stacks to the D-rule size |
| LGM-05 | Kill 20 sheep with a knife | about half drop a raw rack; with a non-knife none |
| LGM-06 | Kill a burning sheep with a knife | cooked rack |
| LGM-07 | Cut raw and cooked rack | 4 mutton chops / 4 cooked mutton chops |
| LGM-08 | Cook raw rack in furnace and on campfire | cooked rack (B2) |
| LGM-09 | Place Omurice, serve 4 with bowls | 4 Bowl of Omurice, then leftovers bowl |
| LGM-10 | Break a full and a part-eaten feast | full: the feast item; part-eaten: a bowl |
| LGM-11 | Feast Plating JEI tab | three Large Meals pages |
| LGM-12 | Hover both curries | two different names (D2) |
| LGM-13 | Heatless crafts in JEI | one `_manual_only` per pot meal, absent from the mechanical crafter |
| LGM-14 | Display Delight: place a Rice Pudding | placed block shows, picks back up |
| LGM-15 | Eat Hearty Lunch | Regeneration 5 s and Nourishment 3 min; Spice of Life counts complexity 6 |

## 9. History

- 2026-10-30: investigation written (jar inventory, decompiled code, FD 1.19.2 API and pot container checked with
  `javap`, overlap against the 10-24 item dump), nothing built.
