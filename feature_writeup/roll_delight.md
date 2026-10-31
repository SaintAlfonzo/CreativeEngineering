# Roll Delight: overlap and integration report

Investigation written 2026-10-30 for TODO `largecontent/mod-additions #12` (the Roll Delight part); **nothing built**.
Jar: `devmods/toBackport/[1.20.1] Roll Delight-1.3.1-forge-.jar` (file 1.3.1, `mods.toml` version 1.3.0), unpacked to
`DecompiledMods/roll_delight-1.3.1/`, code in `DecompiledMods/roll_delight-1.3.1-src/` (31 classes, MCreator). Forge
1.20.1 only, so a **port is needed**. Licence **All Rights Reserved**: the Vinery / Miner's / Rustic donor-jar route (jar
kept unmodified in `roll_upstream/`, our code written from behaviour). "Roll" in this file is Roll Delight. Test
prefix `ROLL-`.

**Scope rule:** integration adds no recipe and no mechanic that is not in Roll Delight or defined by a pack rule
([recipes_cooking.md](recipes_cooking.md), [food.md](food.md), [food_families.md](food_families.md)), unless the owner
asks for it by name. Upstream bugs are fixed (3.5).

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Inventory: 14 items + 3 blocks (17 registry ids), no effects, no potions, 17 recipes, 3 block loot tables, 1 tag, no worldgen, no advancements | 1, 3 | read from jar and code |
| Upstream bugs | 3.5 | B1-B9; B2 (1.20-only ingredients) blocks 2 items on 1.19.2 |
| Overlap | 2 | Rice Roll = FD Kelp Roll; Amazing Salmon Roll vs FD Salmon Roll; medleys vs FD Rice Roll Medley; Tobiko vs Oceanic Delight Fish Egg Roll |
| Fold-in | 4 | 19 steps planned |
| Decisions | 5 | D1-D10 |
| Tests | 8 | ROLL-01..14 drafted, none run |

- **Jar:** 178 files; bundles `mixinextras-forge-0.5.0` (jarjar), unused by the code.
- **Licence:** ARR (`mods.toml`). No fork; donor jar for art and models, Java written from behaviour.
- **Dependencies:** declares only Minecraft 1.20.1. Needs Farmer's Delight at runtime (recipes use `farmersdelight:cooking`,
  `cutting`, `cooked_rice`, `salmon_slice`, `brown_mushroom_colony`; chopsticks code names 6 FD ids) but does not declare it.
- **Pack references:** none. No script, tag, dict or Display Delight entry names `roll_delight`. The dumps in
  `kubejs/exported/` (items 2026-10-24, names 10-20) cannot contain it; `/dumpall` is step 0 of the fold-in.

## 1. Overview

A small sushi add-on: five rolls that cut into three slices, two standalone rolls, two eight-serving "medley" blocks
that hand out rolls, a pot-cooked nether wart, and chopsticks that eat the off-hand food for a potion effect.

| Fact | Value |
|---|---|
| Mod id | `roll_delight` (package `net.mcreator.rolldelight`) |
| Items | 14 (11 foods, chopsticks, 2 medley block items) + wrapped chopsticks block item |
| Blocks | 3: `legendary_roll_medley`, `mythical_roll_medley` (`blockstate` 0-8, cake-like, wool sound), `wrapped_chopsticks` (`blockstate` 0-3, bamboo sound) |
| Effects / potions | none registered; chopsticks apply vanilla effects (3.3) |
| Recipes | 17: 11 shaped/shapeless crafting, 5 FD cutting, 1 FD cooking |
| Loot | 3 block tables (own blocks); no mob loot, no GLM |
| Tags | 1: `data/minecraft/tags/item/enchantable/durability.json` (1.21 path, dead on 1.20 and 1.19.2) |
| Advancements | none (the pack drops all advancements anyway) |
| Lang | `en_us` only |
| Tooltips | 1 (chopsticks) |
| Config | none |
| Compost | `ancient_roll`, `ancient_roll_slice` at 100 % (`RollDelightModCompostableItems`) |
| Events | chopsticks on FD Rice Roll Medley (`RightClickBlock`); shears break of frogspawn drops frogspawn (`BreakEvent`, class `TestProcedure` but live) |

## 2. Overlap (first)

### 2.1 Items

| Roll item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Rice Roll (3 cooked rice + 3 dried kelp) | *overlaps w/ FD Kelp Roll* (rice, kelp, carrot; 12 hunger, cuts to 3 slices) | Same dish, same cut-into-3 shape; only Tobiko uses its slice | D3: do not register; Tobiko takes FD Kelp Roll Slice |
| Rice Roll Slice | *overlaps w/ FD Kelp Roll Slice* (6) | as above; also mis-named (B1) | merged with the roll |
| Amazing Salmon Roll / Slice | *overlaps w/ FD Salmon Roll* (7) and Rustic Rice Roll Royale | Same fish, rice and kelp; differs only in being a whole roll that is cut | D4: keep as the big maki; name and values by pack rule |
| Mushroom Roll / Slice | none (first vegetarian maki) | cabbage + brown mushroom colony in a roll | keep |
| Tobiko Roll | *overlaps w/ Oceanic Delight Fish Egg Roll / Slice* and Miner's Weird Caviar (fish roe) | Same idea (roe roll); Tobiko uses frog spawn, Oceanic uses fish eggs | keep; D9 decides the frogspawn source |
| Golden Roll / Slice | *overlaps w/ Golden Food* (golden carrot, golden cooked cod / mutton) | Made from 2 iron + 3 gold ingots + golden carrot; a gold food outside the pack's golden-food rules | D7 |
| Ancient Roll / Slice | none (pitcher plant, torchflower) | Both ingredients are 1.20-only (B2) | D2 |
| Nether Roll | none (raw porkchop + cooked nether wart) | | keep |
| Cooked Nether Wart | Farmer's Respite Nether Wart Sourdough uses wart, no cooked form exists | bowl food, stack 16 | keep |
| Legendary / Mythical Roll Medley | *overlaps w/ FD Rice Roll Medley* (feast block, 8 servings, three roll kinds) | Same block idea; Roll's medleys hand out items with an empty hand and no bowl, FD's needs a bowl | D8 |
| Chopsticks, Wrapped Chopsticks | none in the pack (no chopsticks item in any mod dump) | new tool and decoration | D5 |

### 2.2 Systems

- **Sushi family:** the pack's `roll` family ([food_families.md](food_families.md), `tools/food_families.py`) holds FD
  salmon/cod/kelp rolls, Rustic bell pepper, calamari and cherry blossom rolls, Ocean's fugu and elder guardian rolls,
  Hostile's guardian roll, Oceanic's egg, fish egg, sea pickle and shrimp rolls. Family rules: plated on Food Plate (whole)
  and Small Food Plate (slice), crafted from wrapper + filling, never canned.
- **Roll recipe rule** ([recipes_cooking.md](recipes_cooking.md) 2.3): FD salmon and cod roll have shapeless x2 and a
  sequenced assembly x3; kelp roll and rice roll medley have an assembly. Roll's rolls are crafting-only.
- **Feast Plating JEI tab** reads FD `FeastBlock`s; Roll's medleys are not FeastBlocks, so they would not show.
- **Effects:** no new effect. Chopsticks give vanilla effects at level II-III (3.3), including Conduit Power, which no
  pack food gives today.
- **Loot, worldgen, crops, seasons, machines:** none.
- **Same batch** (`mod-additions #12`): Large Meals ([large_meals.md](large_meals.md)) shares no item; Mineral Delight
  ([mineral_delight.md](archive/mineral_delight.md)) has mineral foods next to the Golden Roll's ingots. **Carnivore**
  ([carnivore.md](archive/carnivore.md) D9) adds `carnivore:frogspawn` (proposed name "Frog Roe") from frogs, cut on the board
  into 2 placeable `minecraft:frogspawn`: a second survival source of the item Tobiko needs, and the same roe idea as
  Tobiko. If both ports go ahead, one frogspawn source is enough (D9).

## 3. New content

### 3.1 Foods

Values: hunger / saturation modifier (saturation points = hunger x modifier x 2). Stack 64 unless noted.

| Item | Hunger / mod | From | Slices | Note |
|---|---|---|---|---|
| Rice Roll | 10 / 0.3 | 3 cooked rice + 3 dried kelp | 3 x 5 / 0.3 | *overlaps w/ FD Kelp Roll* |
| Amazing Salmon Roll | 15 / 0.6 | shaped: 4 salmon slice, 2 cooked rice, 3 dried kelp | 3 x 9 / 0.6 | *overlaps w/ FD Salmon Roll* |
| Mushroom Roll | 14 / 0.6 | shaped: cabbage, 2 cooked rice, brown mushroom colony, 3 dried kelp | 3 x 8 / 0.6 | |
| Ancient Roll | 16 / 0.6 | shaped: pitcher plant, 2 cooked rice, torchflower, 3 dried kelp | 3 x 10 / 0.6 | B2 |
| Golden Roll | 12 / 1.2 | shaped: 2 iron ingot, golden carrot, 3 gold ingot | 3 x 6 / 1.0 | *overlaps w/ Golden Food* |
| Nether Roll | 7 / 0.7 | shapeless: 2 porkchop + cooked nether wart -> 2 | none | |
| Tobiko Roll | 7 / 0.6 | shapeless: frogspawn + 3 rice roll slice -> 3 | none | *overlaps w/ Oceanic Fish Egg Roll* |
| Cooked Nether Wart | 6 / 0.3, stack 16, bowl | Cooking Pot: nether wart (100 ticks), bowl from the item's remainder | none | |

Slicing gives more food than the whole roll: three slices are worth 1.5x (Rice, Golden) to 1.9x (Ancient) the
whole roll's hunger. FD's kelp roll does the same at 1.5x (12 -> 3 x 6), so the ratio itself has precedent; the
absolute values do not (an Amazing Salmon Roll Slice, 9, beats an FD Salmon Roll, 7). D6.

### 3.2 Blocks

| Block | Recipe | Servings | Behaviour |
|---|---|---|---|
| Legendary Roll Medley (stack 1) | 3 mushroom roll slice, 3 amazing salmon roll slice, 2 tobiko roll, bowl | 8: right-click with anything but chopsticks hands out 3 mushroom slices, 3 salmon slices, 2 tobiko, then the bowl | breaking part-eaten drops what is left plus the bowl |
| Mythical Roll Medley (stack 1) | 3 ancient roll slice, 3 golden roll slice, 2 nether roll, bowl | 8, same pattern | needs Ancient Roll (B2) |
| Wrapped Chopsticks (stack 16) | chopsticks, paper, red dye, yellow dye | holds up to 4 chopsticks; empty hand takes one out, wrapped chopsticks in hand adds one | decoration |

A medley returns exactly what went in (no food gained); it is storage and display.

### 3.3 Chopsticks

Durability 8, 32-tick use, bow animation, only while hunger < 20. On finishing, if the off-hand holds one of the items
below, one is removed from the inventory, hunger and saturation are **added directly** (not through `FoodData.eat`),
and an effect is applied. Durability is spent first, for any off-hand item.

| Off-hand | Hunger | Saturation | Effect |
|---|---|---|---|
| FD Salmon Roll | +7 | +9 | Water Breathing II, 10 s |
| FD Cod Roll | +7 | +9 | Dolphin's Grace II, 10 s |
| FD Salmon Slice | +1 | +1 | Water Breathing II, 5 s |
| FD Cod Slice | +1 | +1 | Dolphin's Grace II, 5 s |
| FD Kelp Roll Slice | +6 | +6 | Speed II, 10 s |
| FD Cooked Rice | +6 | +5 | Slow Falling III, 5 s; gives a bowl |
| Ancient Roll Slice | +10 | +12 | Absorption II, 10 s |
| Golden Roll Slice | +6 | +12 | Night Vision, 10 s |
| Nether Roll | +7 | +9 | Nausea II, 10 s |
| Mushroom Roll Slice | +8 | +9 | Regeneration II, 10 s |
| Amazing Salmon Roll Slice | +9 | +11 | Conduit Power II, 10 s |
| Tobiko Roll | +7 | +9 | Jump Boost III, 10 s |
| Cooked Nether Wart | +6 | +3 | Fire Resistance, 5 s; gives a bowl |
| Raw Porkchop | +3 | +1 | Nausea III, 5 s |

Chopsticks on an FD Rice Roll Medley (event): eats one serving in place with the same direct add and the row's effect.

### 3.4 Other mechanics

- Breaking frogspawn with shears in main hand drops a frogspawn item (the only survival source for Tobiko's input).
- Compost: Ancient Roll and slice at 100 %.

### 3.5 Upstream bugs

Not seen in `logs/latest.log` (the jar is not installed); found in data and code.

| # | Bug | Evidence | Fix in port |
|---|---|---|---|
| B1 | Rice Roll Slice is named "Rice Roll" | `en_us.json` key `item.roll_delight.rice_roll_slice` | correct name (moot if D3 merges it) |
| B2 | Ancient Roll needs `minecraft:pitcher_plant` and `minecraft:torchflower`, which do not exist on 1.19.2; the Ancient Roll, its slice and the Mythical Medley become uncraftable | `ancient_roll_recipe.json`; no pack item dump has either id | D2 |
| B3 | Chopsticks bypass `FoodData.eat`: hunger is not capped (19 + 10 = 29) and saturation is not capped at hunger, so repeated use stacks saturation without limit | `ChopsticksPlayerFinishesUsingItemProcedure` uses `setFoodLevel(get + n)` / `setSaturation(get + n)` | go through `FoodData.eat` (caps both) |
| B4 | The chopsticks path does not count as eating: no Spice of Life diversity, no Diet, no `LivingEntityUseItemEvent.Finish` for the food, no AppleSkin preview | same | eat the stack through the normal path (`ItemStack.finishUsingItem` on the off-hand food) |
| B5 | Chopsticks lose durability with any non-empty off-hand item, even one with no branch (whole rolls, Rice Roll and slice, any block) | durability `hurt` happens before the item checks | spend durability only when something is eaten |
| B6 | The tooltip promises "a roll, slice, or rice"; whole Roll rolls and Rice Roll / slice have no branch | tooltip vs branch list | follows D5 |
| B7 | Empty off-hand logs an empty INFO line on every use | `RollDelightMod.LOGGER.info("")` | drop |
| B8 | Enchantability tag sits at the 1.21 path `tags/item/` | file path | drop the tag; give chopsticks `isEnchantable` via the item if D5 wants Unbreaking |
| B9 | Medley `use` returns `SUCCESS` for every right-click, so a held block cannot be placed against a medley | `MythicalRollMedleyBlock.use` | return `PASS` when nothing was taken |

## 4. Fold-in, in build order

Code goes into a Creative Delight member (memory `creative-delight-routing`, Rustic precedent in
[rustic_delight.md](rustic_delight.md) section 5): `development/creativeDelight/members/roll` (Gradle `:backportedRoll`,
`bundled_in=creativeDelight`, mod id `roll_delight`), donor `roll_upstream/<jar>` read as hidden built-in packs
(`UpstreamJars` pin, `DonorJars`). KubeJS rows in `kubejs/data/creative_delight/kubejs/roll_delight/` (server) and
`development/creativeDelight/src/main/resources/kubejs_startup/roll_delight/` (startup).

0. **Port (Java):** register the ids kept by D2/D3; foods as plain items with the values D6 fixes; Cooked Nether Wart
   as a bowl food (stack 16); chopsticks and wrapped chopsticks written from 3.2/3.3 with B3-B9 fixed; medleys per D8.
   Donor translation: 1.20 data to 1.19.2 (the recipe `category` fields are ignored on 1.19.2; drop the 1.21 tag).
1. **Dumps and generators:** after install, `/dumpall`, then `/cegen` (memory `dumpall-refresh-workflow`).
2. **Ids:** every kept id in `item_dict.js` (and the block dict for the three blocks) before any script uses them.
3. **Tags** via `global.tags`: slices and rolls into the `roll` food type; `#forge:cooked_rice`-style inputs where the
   pack has one; Cooked Nether Wart into the vegetable / side food type.
4. **Upstream recipe removals and tag inputs:** `minecraft:porkchop` -> `#forge:raw_pork` (pack tag also holds bacon:
   check that is wanted), `forge:crops/nether_wart` and cabbage are tags already, FD ids (`salmon_slice`, `cooked_rice`,
   `brown_mushroom_colony`) through their dict keys. Rewrite every kept recipe through `global.NCH` under the CD
   `roll_delight` folder (the donor's recipe JSONs are not loaded).
5. **Mob loot:** none.
6. **Item merges:** Rice Roll and slice into FD Kelp Roll / slice (D3), Tobiko recipe re-pointed. Never deployed, so
   no conversion. Record in [item_merges.md](item_merges.md).
7. **Potions:** none.
8. **Recipes by pack rules** ([recipes_cooking.md](recipes_cooking.md)): rolls follow the FD roll rule (shapeless kept,
   one sequenced assembly per roll on cooked rice, as salmon/kelp roll); medleys get an assembly on a bowl like the rice
   roll medley; cutting recipes kept; Cooked Nether Wart's pot recipe mirrored by the Mixer by the pack's existing
   mirror. Golden Roll per D7 ([recipes_cooking.md](recipes_cooking.md) 2.4).
9. **Effects:** no new effect; the chopsticks table goes into JEED text for the chopsticks item (tooltip, step 15).
10. **Food types and values:** add to `food_types.json` (`roll`), values per D6 ([food.md](food.md)).
11. **Food families:** `python3 tools/food_families.py --check` after the types are in.
12. **Stack sizes** ([stack_sizes.md](stack_sizes.md)): rolls and slices by the handheld rule; medleys 1 (as upstream).
13. **Cans and jars:** none (handhelds are never canned, `roll` family).
14. **Compost:** CD `compost.js` values by the pack's compost framework (drop the 100 % entries).
15. **Names and tooltips:** B1; chopsticks tooltip lists what they work on; [names.md](names.md) rule for "Amazing"
    (D4). JEI info page for the shears-on-frogspawn drop ([jei_coverage.md](jei_coverage.md)).
16. **Mob and block audit:** medleys and wrapped chopsticks: mineable tag (axe or none, cake-like), BlockRunner tier,
    Item Physic, WTHIT servings line.
17. **Placeables:** Food Plate / Small Food Plate per the `roll` family; Display Delight has no Roll Delight blocks.
18. **Worldgen, trades, chests:** none.
19. **Release:** `mod_sides.md` row (both sides), player changelog bullets under a "Roll Delight" family, TODO row,
    plan row.

Ordering: 0 before everything; 1 before 2-4 (ids must exist in the dump); 2 before 3, 4, 8, 10; 6 before 8; 10 before 11.

## 5. Decisions for the owner

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | Port Roll Delight at all? | Yes, as CD member `roll` (small, fills the maki gap; chopsticks are new) | port |
| D2 | Ancient Roll needs 1.20-only pitcher plant and torchflower | Leave out Ancient Roll, its slice and the Mythical Medley until a sniffer-plant backport exists (any substitute is a new recipe, which the scope rule needs you to name) | leave out the three ids |
| D3 | Rice Roll duplicates FD Kelp Roll | Do not register Rice Roll / slice; Tobiko takes 3 FD Kelp Roll Slices | merge |
| D4 | "Amazing Salmon Roll" next to FD "Salmon Roll" | Keep the item; rename to "Salmon Maki Roll" / "Salmon Maki" so the two read apart | keep upstream name |
| D5 | Chopsticks | Keep, fixed (B3-B9): eat the off-hand food normally, then add the table's effect; table keys go to the pack ids (FD rolls, Roll slices) | keep, fixed, upstream effect levels |
| D6 | Food values (slices beat FD rolls) | Scale to FD: whole roll as an FD meal of its ingredient count, slices = whole x 1.5 / 3 (FD kelp roll ratio) | keep upstream values |
| D7 | Golden Roll eats 3 gold + 2 iron ingots | Route through the golden-food rule (golden carrot + gold nuggets, as `golden.js`), drop the iron | keep upstream recipe |
| D8 | Medleys hand out servings with no bowl, unlike FD feasts | Rebuild as FD `FeastBlock`s (bowl per serving, leftovers bowl) so the Feast Plating tab and WTHIT see them | keep upstream behaviour (B9 fixed) |
| D9 | Frogspawn from shears (Carnivore's Frog Roe cut on the board is a second source) | Keep the shears drop and add a JEI info page; if Carnivore is ported too, keep both (different routes: placed block vs frog kill) and let Tobiko take vanilla frogspawn only | keep |
| D10 | Effects at level II-III through chopsticks (Conduit Power II, Jump III, Regeneration II) | Keep the effects, lower to level I to match pack food effects | keep upstream levels |

Blocking a build: D1, D2, D3, D5.

## 6. File map (planned)

| Path | What |
|---|---|
| `roll_upstream/[1.20.1] Roll Delight-1.3.1-forge-.jar` | donor, unmodified, sha1-pinned |
| `development/creativeDelight/members/roll/` | Java (items, blocks, chopsticks, events), translator, NOTICE |
| `development/creativeDelight/src/main/resources/kubejs_startup/roll_delight/` | dict rows, food types, compost |
| `kubejs/data/creative_delight/kubejs/roll_delight/recipes.js` | NCH recipes, assemblies, removals |
| `kubejs/startup_scripts/` `item_dict.js` | ids |
| `feature_writeup/roll_delight.md` | this file |

## 7. Offline checks (once built)

- `./gradlew :backportedRoll:build --offline` and `:creativeDelight:build`; build guard finds no ARR path in our jar.
- Headless harness: chopsticks finish with hunger 19 + Ancient-equivalent row ends at hunger 20 and saturation <= 20 (B3).
- `python3 tools/food_families.py --check`; `python3 tools/changelog_lint.py`.

## 8. In-game test suite (drafted)

| ID | Test | Pass |
|---|---|---|
| ROLL-01 | Boot with the CD jar | no `Failed to parse` naming `roll_delight` in `latest.log` |
| ROLL-02 | JEI search "roll" | every kept Roll item has a recipe; no Rice Roll (D3), no Ancient Roll (D2) |
| ROLL-03 | Cut an Amazing Salmon Roll and a Mushroom Roll on the board | 3 slices each, values per D6 |
| ROLL-04 | Craft Tobiko from frogspawn + 3 kelp roll slices | 3 Tobiko Rolls |
| ROLL-05 | Break frogspawn with shears, then with a hand | shears drop frogspawn; hand drops nothing |
| ROLL-06 | Cook nether wart in the Cooking Pot with a bowl | Cooked Nether Wart; no bowl, no output |
| ROLL-07 | Chopsticks, Mushroom Roll Slice in off-hand, hunger 19 | slice gone, hunger 20 (not 27), saturation <= 20, effect applied |
| ROLL-08 | Chopsticks with a dirt block in off-hand | no durability lost (B5) |
| ROLL-09 | Chopsticks eat 10 slices in a row from hunger 10 | saturation never exceeds hunger; Spice of Life counts the food |
| ROLL-10 | Place a Legendary Medley, take all servings | 3 + 3 + 2 items, then the bowl; block gone |
| ROLL-11 | Break a half-eaten medley | remaining items + bowl drop |
| ROLL-12 | Wrapped Chopsticks: add 3, take 4 | stacks to 4 visually; 4 chopsticks back, block gone |
| ROLL-13 | Golden Roll recipe in JEI | matches D7 |
| ROLL-14 | Chopsticks on an FD Rice Roll Medley | one serving eaten, same caps as ROLL-07 |

## 9. History

- 2026-10-30: investigation written (jar inventory, decompiled code, overlap against the 10-24 item dump), nothing built.
