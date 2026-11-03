# Large Meals: overlap and integration report

Investigation written 2026-10-30 for TODO `largecontent/mod-additions #12` (the Large Meals part). **BUILT 2026-11-01** (build notes in
section 4.2): member `backportedLargeMeals 1.3.0-ce-19.2-01` nested in the Creative Delight jar; installed in `mods/` (CD jar 11-02 14:01), scratch run r1102b (8), not played (deep scan 2026-11-02).
**Owner decisions D1-D9 answered 2026-11-01** (section 5); the sections below are updated to match.
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
| Decisions | 5 | D1-D9 answered 2026-11-01 |
| Build | 4.2 | BUILT 2026-11-01 (member + CD scripts + Display Delight `lm_`); in `mods/` since the CD jar of 11-02 14:01, not played; `/dumpall`, can generator, compost and `/cegen` wait on the game |
| Tests | 8 | LGM-01..20 in game: none run. Scratch r1102b (2026-11-02): SQ-LGM-01 to -09 PASS; SQ-LGM-10 FAIL (Chicken Curry recipe ids clash with Ender's, 8); CQ-LGM-11 waits for the client round |

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
| Chicken Curry | *overlaps w/ Ender's Delight Chicken Curry* (`endersdelight:chicken_curry`, 7 / 0.9, Nourishment 5 min, End spices, Shulker Bowl) | Same display name, different dish | D2: Large Meals keeps "Chicken Curry"; Ender's becomes "End Chicken Curry" |
| Raw / Cooked Rack of Mutton | *overlaps w/ vanilla mutton and FD Mutton Chops* (rack cuts to 4 chops) | A second mutton drop from sheep | keep; knife drop through the Farmer's Delight loot modifier route (D3) |
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
- **Feast Plating JEI tab (owner: fold in):** it reads every FD `FeastBlock`, so the three feasts appear once they are
  `FeastBlock`s; LGM-11 checks it, and a feast that does not show gets a `FeastPlatingCategory` entry.
- **Effects:** FD Comfort and Nourishment, vanilla Regeneration; no new effect, nothing for JEED.
- **Loot:** two knife-drop modifiers on sheep, the same mechanic as the other knife drops (D3). They list in the "Knife and
  Machete Drops" JEI tab ([jei_world_tabs.md](jei_world_tabs.md)) with no extra work; [loot.md](loot.md) is chest loot only
  and is not involved.
- **Eggs (D9):** `#forge:eggs` holds 12 items (vanilla, turtle, crocodile, emu, terrapin, 5 Quark parrot, duck, goose). Owner
  2026-11-01: every Large Meals egg input uses the tag; the dragon eggs stay out of it (only used in dragon egg crafts), so
  the tag needs no additions. Tomato Egg Soup, Omurice and Sweet Berry Custard take any of the 12; the broader migration
  stays in [food.md](food.md) 3.8.
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

FD `add_item` global loot modifiers on `minecraft:sheep` (kept as shipped, D3), killer holding `#farmersdelight:tools/knives`, chance 0.5 + 0.1 per Looting
level: raw rack, or cooked rack when the sheep is on fire.

### 3.4 Data that does nothing in the pack

5 Diet tags; 15 advancements (lang has "Sheep Butcher"); `vi_vn` lang (kept, harmless).

### 3.5 Upstream bugs

| # | Bug | Evidence | Fix in port |
|---|---|---|---|
| B1 | "Plate of Mushroom Pot Pie" and "Plate of Roasted Rack of Mutton" are served into a bowl (FD feast takes the serving's remainder, bowl); "Bowl of Omurice" is right | `en_us.json`, `ModItems` `bowlFoodItem` | name by the container (D6) |
| B2 | Cooked Rack of Mutton has only a smoker recipe (no furnace, no campfire) | `recipes/cooked_mutton_rack.json` | the pack's meat cooking route (step 8) |
| B3 | Roasted Rack of Mutton feast is a grid craft while the other two feasts are pot recipes with a bowl container | `recipes/roasted_mutton_rack_block.json` | pot recipe added plus a dearer cooked-rack variant; the grid craft stays as the `_manual_only` heatless route (D7) |
| B4 | Pasta with Mushroom Sauce takes literal `minecraft:brown_mushroom` x2, Hearty Lunch literal `farmersdelight:bacon` x2 | recipe JSONs | tag inputs (step 4) |
| B5 | Diet tags and advancements for systems the pack does not run | 3.4 | not shipped |
| B6 | Egg inputs are `#forge:eggs`, which lacks the dragon eggs and accepts turtle and parrot eggs | pack tag dump | keep the tag in all three recipes (D9) and complete it with the missing eggs (step 4a) |

## 4. Fold-in, in build order

Code goes into a Creative Delight member (memory `creative-delight-routing`, Rustic precedent
[rustic_delight.md](rustic_delight.md) section 5): `development/creativeDelight/members/largemeals` (Gradle
`:backportedLargeMeals`, `bundled_in=creativeDelight`, mod id `largemeals`), donor `largemeals_upstream/<jar>` read as
hidden built-in packs (`UpstreamJars` pin). KubeJS rows in `kubejs/data/creativedelight/kubejs/largemeals/` (server)
and `development/creativeDelight/src/main/resources/kubejs_startup/largemeals/` (startup).

0. **Port (Java):** items and the three feasts on FD's `FeastBlock` / `ConsumableItem` (MIT base classes), our own
   food-value table, feast shapes; GLMs as FD `add_item` data (ours) or per D3. Donor translation: drop recipe
   `category`, the 1.20 creative-tab list (`CREATIVE_TAB_ITEMS`) becomes an FD tab entry.
1. **Dumps and generators:** `/dumpall`, then `/cegen` (memory `dumpall-refresh-workflow`).
2. **Ids:** all 18 items and 3 blocks in `item_dict.js` / block dict.
3. **Tags** via `global.tags`: food types; bowls meals in the soup / stew / meal types, desserts in `dessert/pudding`.
4a. **Egg tag:** no change; re-scan the item dump after `/dumpall` for any raw egg added since 2026-10-24 and add it if found.
   Dragon eggs stay out.
4. **Recipe inputs:** literal mushrooms to `#forge:mushrooms/brown` (or the pack's brown-mushroom key), bacon to
   `#forge:raw_pork` / bacon key, eggs per B6. All recipes rewritten through `global.NCH` (the donor's JSONs are not loaded).
5. **Mob loot:** the two sheep drops stay Farmer's Delight `add_item` loot modifiers in our data, like the other knife
   drops (Ender's, Ocean's). Add the JEI info line and item tooltip "Obtained by killing a Sheep with a Knife" per the
   Ender's knife-drop pattern ([ends_delight.md](ends_delight.md)).
6. **Item merges:** none (D2 is a rename, not a merge).
7. **Potions:** none.
8. **Recipes by pack rules** ([recipes_cooking.md](recipes_cooking.md)): pot meals kept and mirrored by the Mixer;
   heatless rule: one `_manual_only` shapeless per pot meal from cooked ingredients, soups with bone broth, one more main
   ingredient than the pot; feasts get a sequenced assembly on a bowl (shepherd's pie precedent); cooked rack by the
   pack's raw-meat route (furnace, smoker, campfire; B2); cutting recipes kept ([oven.md](oven.md): none bake).
   **Roasted Rack feast (D7):** two pot recipes, bowl container, 250 ticks like the other feasts: (a) the base recipe, raw
   rack plus the grid craft's cooked rice, carrots and baked potatoes (Q3, accepted); (b) a dearer variant that takes the **cooked**
   rack and adds extra ingredients (Q2, accepted: +1 baked potato, +1 carrot), same output. The upstream grid craft stays as the `_manual_only` heatless route.
   Inputs on tags (brown mushroom, bacon, eggs) throughout (B4, B6).
9. **Effects:** none new; JEED text unchanged.
10. **Food types and values:** `food_types.json`; values stay as upstream (D4; Chicken Curry 12 / 0.8). **Spice of Life
    (owner: register correctly):** the four `largemeals:` complexity entries name food items (`sweet_berry_custard`,
    `pufferfish_broth`, `mushroom_pot_pie` = the serving item, `hearty_lunch`), so they go live. Check: all 18 ids have Food
    properties before SoL's `ComplexityParser` runs (no `Item is not food: largemeals:` in the log); the feast block items are
    not food and are not listed; the servings count when eaten from the hand and when served from the placed block
    (`PlacedFoodSol`, [food.md](food.md) 4.2 finding 5: confirm it covers any `FeastBlock`, else add the three); the custard's
    always-edible flag survives the pack's food-value pass.
11. **Food families:** `python3 tools/food_families.py --check`.
12. **Stack sizes** ([stack_sizes.md](stack_sizes.md)): bowl meals 16 as FD; Sweet Berry Custard by the bottle rule
    (custard 32 in the drink family note); feasts 1.
13. **Cans and jars** ([cans.md](cans.md)): soups by the generated per-type can rule (parity 2.11); feasts wait for
    `cans/canning #17`; soups into the Supplementaries jar list.
14. **Compost:** CD `compost.js` defaults for meals (racks as meat, not compostable).
15. **Crops, seasons, worldgen:** none.
16. **Names** ([names.md](names.md)): Large Meals keeps "Chicken Curry"; Ender's `endersdelight:chicken_curry` is renamed
    "End Chicken Curry" by a lang override (id unchanged, so saves and recipes are unaffected). The Large Meals item needs its
    own `item_dict` key, not `chicken_curry` (already Ender's). Serving names become "Bowl of Mushroom Pot Pie" and "Bowl of
    Roasted Rack of Mutton" (B1, D6). Where the rename goes: `item.endersdelight.chicken_curry` in
    `kubejs/assets/creativedelight/kubejs/creativeengineering/names.js` (line 41), and `block.displaydelight.erd_chicken_curry`
    ("Placed Chicken Curry") in `kubejs/assets/displaydelight/lang/en_us.json` becomes "Placed End Chicken Curry"; the new
    `lm_` curry block is "Placed Chicken Curry". Also fix the names in `ends_delight.md`, `names.md`, `extra_delight.md`, `display_delight.md`
    and the 1.1.03 changelog mention stays (deployed, never edited). The rename is a player-visible change: changelog bullet.
17. **Mob and block audit:** feasts: mineable tag as FD feasts, BlockRunner tier, Item Physic, WTHIT servings line
    (FD feasts are covered by the existing crop/feast plugin).
18. **Tooltips and JEI** ([tooltips.md](tooltips.md), [jei_coverage.md](jei_coverage.md)): the rack drop as a JEI info
    page or loot entry; Feast Plating tab automatic.
19. **Placeables (D5, owner: add):** Display Delight: drop `lm_` from `EXCLUDED_PREFIXES`, rerun `port_resources.py`, rebuild
    `backportedDisplay` (13 blocks).
20. **Trades and chests:** none upstream; none added.
21. **Release:** `mod_sides.md` row, changelog family "Large Meals", TODO row, plan row; remove the 4 dead SoL warnings
    from [food.md](food.md) 4.2 finding 4.

Ordering: 0 before all; 1 before 2-4; 2 before 3, 4, 8, 10, 19; 19 needs the Large Meals jar on the Display Delight
build classpath only for ids (it resolves by name at runtime), so it can go in the same release.

### 4.2 Build notes (2026-11-01)

- **Java** (`development/creativeDelight/members/largemeals`, Gradle `:backportedLargeMeals`, package
  `com.SaintAlfonzo.creativedelight.largemeals`, mod id `largemeals`): `MealItems` (18 ids, upstream food values, D4; bowl foods
  through Farmer's Delight `bowlFoodItem` + `ConsumableItem`, custard with a glass bottle back, stack 16), `MealBlocks` (3
  feasts), `MealFeastBlock` (FeastBlock, 4 servings, leftovers on, shape per feast), `MealTab`. No config, no mixins.
- **Translator** (`upstream/LargeMealsTranslator`): upstream assets as the client pack (lang patched: "Bowl of Mushroom Pot Pie",
  "Bowl of Roasted Rack of Mutton" (D6/B1), tab name, advancement lines dropped); from upstream data only the 3 block loot tables
  and the 2 sheep `add_item` loot modifiers with `global_loot_modifiers.json` (D3), unchanged. Blockstates and models need no
  change (the feasts keep FD's `facing` / `servings`). Recipes, advancements and Diet tags are not loaded.
- **Donor:** `largemeals_upstream/largemeals-1.20.1-1.3.0.jar` (bytes unchanged, sha1 `76621387...`);
  `build_deploy.py` ships the folder and checks the build guard (`UPSTREAM_DONORS`).
- **Scripts** (CD): `kubejs/data/creativedelight/kubejs/largemeals/recipes.js` (all recipes via NCH: 10 pot dishes, 3 feast pot
  recipes plus the cooked-rack variant, cooked rack furnace / smoker / campfire, 2 cuttings, 13 `_manual_only` heatless
  shapeless, 3 assemblies on a bowl), `tags.js` (`forge:mushrooms/brown` = the vanilla brown mushroom; feasts knife-mineable and
  BlockRunner confectionery), client `largemeals/client.js` (rack tooltips) and `jei_info.js` (rack pages); dicts
  (`item_dict.js` keys, `lm_chicken_curry` for the clash, `block_dict.js`), `transition_items.js` (3 feast assembly items),
  `food_types.json` (soups, meals, `lm_chicken_curry`, three feast servings in `meal/feast`, custard and pudding in `sweet`,
  racks raw / cooked meat). Display Delight: `lm_` removed from `EXCLUDED_PREFIXES`, `port_resources.py` rerun (13 blocks, 0
  missing models), "Placed ..." names in `kubejs/assets/displaydelight/lang/en_us.json`. Names: `item.endersdelight.chicken_curry`
  and `block.displaydelight.erd_chicken_curry` now read End Chicken Curry (D2).
- **Deviation from Q2 / Q3 (owner decided 2026-11-02):** the Cooking Pot has 6 ingredient slots, so the accepted recipes (8 and
  10 ingredients) cannot exist. Final: base = raw rack, 2 cooked rice, 2 carrot, 1 baked potato (6); dearer = cooked rack, 1
  cooked rice, 2 carrot, 2 baked potato (6; a carrot was added to each, so the dearer one gave up a rice to stay at 6). The
  upstream grid craft is unchanged as the heatless route.
- **Not done here (waits on the game or owner):** `/dumpall`, then `python3 tools/gen_can_types.py` (soups and meals get generated
  cans; feasts wait for `cans/canning #17`), `python3 tools/gen_compost_values.py --add-new` after `/dumpcompost` (racks are not
  compostable), then `/cegen` (stack sizes, placeables, food families: `python3 tools/food_families.py --check`). Item Physic and
  WTHIT need no rows (FD feasts are covered). Spice of Life: the four `largemeals:` entries in `solapplepie-server.toml` need no
  change; confirm in LGM-16. Chopstick foods: the Large Meals dishes are not on the Roll Delight list (not asked).
- **Installing:** `:creativeDelight:installToMods` and the rebuilt `:backportedDisplay:installToMods`, only with the game closed
  (it was running during this build), plus `largemeals_upstream/` next to `mods/` on client and server.

## 5. Decisions (answered by the owner 2026-11-01)

| # | Question | Answer |
|---|---|---|
| D1 | Port Large Meals? | Yes, as Creative Delight member `largemeals` |
| D2 | Two "Chicken Curry" items | Large Meals keeps **Chicken Curry**; Ender's Delight's becomes **End Chicken Curry** (lang override, id unchanged) |
| D3 | Rack drop from sheep | Same mob-drop mechanic as the other knife drops (Farmer's Delight `add_item` loot modifier; lists in the Knife and Machete Drops JEI tab). Not a loot.md entry (chest loot only) |
| D4 | Values | Keep upstream; Chicken Curry stays 12 / 0.8 |
| D5 | Display Delight `lm_` blocks | Add the 13, same release |
| D6 | "Plate of ..." names | Rename to "Bowl of Mushroom Pot Pie" and "Bowl of Roasted Rack of Mutton" |
| D7 | Roasted Rack feast | Add a pot recipe and a more expensive cooked-rack version (step 8); the grid craft stays as the heatless route |
| D8 | Spice of Life | Foods register correctly: the four entries go live, all 18 ids food-checked (step 10) |
| D9 | Eggs | `#forge:eggs` in every Large Meals recipe; the tag already holds every raw egg except the dragon eggs, which stay out (step 4a) |
| - | Feast Plating JEI tab | Fold the three feasts in (LGM-11) |
| - | Upstream bugs | Fix B1-B6 (section 3.5) |

Q1 answered 2026-11-01 (dragon eggs stay out of the tag); Q2 and Q3 accepted with their defaults: the cooked-rack pot recipe adds
1 extra baked potato and 1 extra carrot over the base recipe, and the base recipe is the upstream grid craft's ingredients (3 cooked
rice, 2 carrot, 2 baked potato, bowl) with the raw rack.

## 6. File map (planned)

| Path | What |
|---|---|
| `largemeals_upstream/largemeals-1.20.1-1.3.0.jar` | donor, unmodified, sha1-pinned |
| `development/creativeDelight/members/largemeals/` | Java (items, feasts, food values), translator, NOTICE |
| `development/creativeDelight/src/main/resources/kubejs_startup/largemeals/` | dict rows, food types, compost |
| `kubejs/data/creativedelight/kubejs/largemeals/recipes.js` | NCH recipes, heatless crafts, assemblies |
| `development/backportedDisplay/` `port_resources.py` | `lm_` prefix removed |
| `defaultconfigs/solapplepie-server.toml` | unchanged (entries become live) |
| `feature_writeup/large_meals.md` | this file |

## 7. Offline checks (once built)

- `./gradlew :backportedLargeMeals:build --offline`, `:creativeDelight:build`; build guard finds no ARR path in our jar.
- `python3 tools/food_families.py --check`; `python3 tools/changelog_lint.py`.
- Display Delight: `port_resources.py` reports 13 `lm_` blocks added, 0 missing models.

## 8. In-game test suite (drafted)

Scratch r1102b (2026-11-02, `perf/runs/r1102b/s3/judged.md`): SQ-LGM-01 to -09 PASS (01, 03, 04b, 08 proven from the
log after batch faults); covers the server half of LGM-01 to LGM-10, LGM-18. **SQ-LGM-10 FAIL:** 12 of 13 heatless
crafts; Large Meals Chicken Curry has no recipe at all: `creativedelight:farmersdelightcooking/chicken_curry` and
`shapeless/chicken_curry_cooked_manual_only` are emitted by both CD `largemeals/recipes.js` and
`endersdelight/additions.js`, KubeJS keeps the last (`Duplicate recipe for id`). Needs a distinct id (for example
`lm_chicken_curry`); LGM-12 and LGM-13 wait on it.

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
| LGM-12 | Hover both curries | "Chicken Curry" (Large Meals) and "End Chicken Curry" (Ender's); Ender's recipes and old saves unaffected |
| LGM-13 | Heatless crafts in JEI | one `_manual_only` per pot meal, absent from the mechanical crafter |
| LGM-14 | Display Delight: place a Rice Pudding | placed block shows, picks back up |
| LGM-15 | Eat Hearty Lunch | Regeneration 5 s and Nourishment 3 min; Spice of Life counts complexity 6 |
| LGM-16 | Eat the four complexity foods and the other 14 | no `Item is not food` warning; SoL food list shows each once; the three feast servings count from the placed block and from the hand |
| LGM-17 | Roasted Rack feast: both pot recipes | raw-rack and cooked-rack recipes each give the feast; the grid craft appears as heatless only |
| LGM-18 | Egg recipes with a duck, goose, parrot and turtle egg; a dragon egg | the four accepted by Tomato Egg Soup, Omurice, Sweet Berry Custard; the dragon egg rejected |
| LGM-19 | Sheep kill with a knife | rack drop listed in the Knife and Machete Drops JEI tab |
| LGM-20 | Display Delight: all 13 `lm_` blocks | each places and picks back up |

## 9. History

- 2026-11-02 (deep scan): CD jar with the member in `mods/` since 11-02 14:01; scratch r1102b: SQ-LGM-01 to -09 PASS, SQ-LGM-10 FAIL (curry recipe id clash with Ender's).
- 2026-11-02: Roasted Rack pot recipes: a carrot added to each, both at 6 ingredients (dearer one loses a rice); deviation accepted.
- 2026-11-01: BUILT (4.2): member, scripts, Display Delight `lm_`, names, changelog family, scratch SQ-LGM-01..10; Roasted Rack pot recipes shrunk to the 6-slot limit.
- 2026-11-01: owner decisions D1-D9 answered and folded into sections 2-5 and 8 (LGM-16..20 added); nothing built.
- 2026-11-01: dragon eggs kept out of `#forge:eggs`; Q2 and Q3 accepted.
- 2026-10-30: investigation written (jar inventory, decompiled code, FD 1.19.2 API and pot container checked with
  `javap`, overlap against the 10-24 item dump), nothing built.
