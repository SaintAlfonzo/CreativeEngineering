<!-- Layout of TODO.md: headings and prose with <?todo ...?> placeholders (tools/todo_rows.py). Rows are in rows/*.csv; tools/status_index.py renders TODO.md. -->
# TODO: every open change, by family

One `#` heading per category (Food Rules, Drinks and Drugs, Kitchen and Cooking, Baking and Sweets, Dishes and Ingredients, Farming, Worldgen, Structures, Mobs, Loot and Trade, Gear and Combat, Potions and Effects, Recipes and Materials, Mechanics, Items and Blocks, Interface and JEI, Tech, Alcohol, Mods and Ports, Creative Delight, Creative Mods, Tooling), each holding feature families named after the feature, as in the player changelog. One `#` heading per category, one `##` heading per feature family with one table, one row per significant feature change, bug fix or rebalance, sorted by status (the order below), then by old family and row number. **Complete** rows are not shown in the family tables: the [archive](#archive) at the bottom lists them. Detail stays in the linked writeups ([feature_writeup/README.md](../README.md) is the feature index); a row says enough to pick the work up and points at the rest. History of the layout and of what was folded in: [TODO_history.md](TODO_history.md).

**This file is generated** from [rows/](rows/) (one CSV per category) and [layout.md](layout.md) by `tools/status_index.py`. Edit rows with `python3 tools/todo.py` (`add`, `set`, `log`, `move`, `find`); edit headings and prose in layout.md ([agent note](../agent_notes/todo-layout.md)).

**Ids did not change in the regroup.** Every row keeps its old `family/sub #n` id (the family and subfamily names no longer exist as headings; they survive only as the Id), so writeups, briefs (`briefs/<family>_<sub>.md`), the scratch queue and dated History lines still resolve. Find a row by grepping its id.

**Implementation briefs (2026-10-21):** every open row (Bug, Planned, Pinned, In Progress) outside Tooling (which has no briefs) has a build plan in
[briefs/](../briefs/README.md), one file per subfamily, linked as `[brief]` in the row's Writeup cell.

**Scratch test queue:** [testing/scratch_queue.md](../testing/scratch_queue.md), rendered from `testing/rows/*.qb`; its coverage block is generated. A row's `Tests` cell (see [Test tiers](#test-tiers)) says which tiers are still open; its `queue` field names the queue rows that verify it.

## Status snapshot

Generated from the rows by `python3 tools/status_index.py`. The hand-written snapshot of 2026-11-03 is in [status/history.md](../status/history.md).

<?todo snapshot?>

## High priority

Open rows with `priority=high` (owner ruling D13), from every family:

<?todo high_priority?>

## How to read a row

| Column | Meaning |
|---|---|
| Id | The row's permanent id, `family/sub #n` (e.g. `cans/canning #3`), from the old technical family and subfamily. It no longer matches the headings: rows sit under the feature family they belong to. Ids never change; a new row takes the next free number of the old family/sub it would have had (or of the nearest one) |
| Change | The feature, fix or rebalance, in one line |
| Status | See below |
| Tests | The test tiers the row needs before it is **Complete**, and which have passed: see [Test tiers](#test-tiers) |
| Work | Where the work lands: **Creative Common** (the core jar; patcherMod before 2026-11-04), **new jar** (a new or other in-tree mod: the ports, More Mooblooms, Incubation Additions), **KubeJS**, **data** (json / datapack / generated), **config**, **tool** (`tools/*.py`), **mods/** (add or remove a jar), **docs** |
| Details | How it is (or would be) built, and what is left |
| Decisions | What has to be decided before or during the work; "none" when it can just be done |
| Writeup | The detailed writeup and section, plus its pending in-game tests |

Status, in table order within a subfamily (Bug first):

| Status | Meaning |
|---|---|
| **Bug** | A known bug that is not fixed yet |
| **Planned** | Not started, no progress |
| **Pinned** | Planned, held on purpose until something else happens (the row says what) |
| **In Progress** | Started, not complete |
| **Testing** | Implementation complete, at least one tier in its `Tests` cell still open: not built into the pack yet, built but not run, run only on a scratch server or in a GameTest, or live too briefly to count as played |
| **Complete** | Every tier in its `Tests` cell has passed (`+`). Features live since 1.0.4 or earlier that are part of everyday play, with nothing in their writeup saying they were never seen in game, count as `P+` |

### Test tiers

A feature is **Complete** only when every test tier it needs has passed. The `Tests` cell lists the tiers the row
needs, in this order, each marked `+` when passed and `~` when partly passed; a tier the row does not need is left out,
and `none` means nothing is tested in game (docs, decisions, investigations, tools that never touch game data).
Example: `S+ C P T` = scratch passed, client, play and tuning still open.

| Tier | Where | What it proves | Needed by | Passed when |
|---|---|---|---|---|
| **S** scratch | Throwaway dedicated server in the scratchpad, headless harness, GameTest, probe commands, fake players (`SQ-` entries) | The change loads clean and its server-side logic and data are right: recipes, tags, loot tables, worldgen census, commands, `latest.log` free of new errors | Every row that changes game data, scripts or server code | A PASS in a run log or the queue's Run log, or the writeup test ticked as scratch |
| **C** client | A client window: throwaway client or the owner's game (`CQ-` entries) | What only a client shows: textures, models, rendering, tooltips, JEI pages and order, GUIs, sounds, client config, client-server sync | Rows with anything visual or client-side | A recorded client check (CQ- PASS, visual test ticked, owner says it looks right) |
| **P** play | Normal survival play on the real pack (owner's game or the live server), after the change shipped in a deployed version | It behaves as designed in context: real chunks, real mobs, automation, multiplayer, no new errors in the player's `latest.log`, no side effects on neighbouring features | Every row a player can reach | Explicit evidence from play (a test ticked "in game", an owner note). Being deployed, or the owner having played, is not a pass |
| **T** tuning | Owner judgment after play | The numbers (rates, costs, food values, frequencies, durations, balance) are accepted or reset | Rows with tunable values | An owner statement |

Order of work: S before shipping (queue it in the scratch queue in the same session), C in the next client round, P
after a deploy, T last. A failure at any tier sends the row back to **In Progress** (or **Bug** once it shipped).

When a row is done: tick its tests in the writeup and `python3 tools/todo.py set ID status=Complete`; the [archive](#archive) picks it up (ids never change, so citations keep working; never renumber). New ideas go in the [scratchpad](scratchpad.md) first and
move here when they get a family.

## Families

- [Food Rules](#food-rules)
  - [Food Tuning](#food-tuning)
  - [Stack Sizes](#stack-sizes)
  - [Seasoning](#seasoning)
  - [Placeable Food](#placeable-food)
  - [Food Textures](#food-textures)
- [Drinks and Drugs](#drinks-and-drugs)
  - [Drinks](#drinks)
  - [Drugs](#drugs)
- [Kitchen and Cooking](#kitchen-and-cooking)
  - [Canning](#canning)
  - [Kitchen](#kitchen)
  - [Remainder Buffers](#remainder-buffers)
  - [Oven](#oven)
  - [Cooking Recipes](#cooking-recipes)
- [Baking and Sweets](#baking-and-sweets)
  - [Cakes and Pies](#cakes-and-pies)
  - [Breads and Dough](#breads-and-dough)
  - [Cookies and Muffins](#cookies-and-muffins)
  - [Baking Types and Bakeware](#baking-types-and-bakeware)
  - [Pancakes](#pancakes)
  - [Sweets](#sweets)
  - [Jams](#jams)
- [Dishes and Ingredients](#dishes-and-ingredients)
  - [Ingredients](#ingredients)
  - [Sweeteners and Syrups](#sweeteners-and-syrups)
  - [Dairy, Fats and Batter](#dairy-fats-and-batter)
  - [Produce and Potatoes](#produce-and-potatoes)
  - [Meals](#meals)
  - [Meal Rules and Feasts](#meal-rules-and-feasts)
  - [Rice, Egg and Sushi](#rice-egg-and-sushi)
  - [Soups, Salads and Sandwiches](#soups-salads-and-sandwiches)
  - [Pizza, Pasta and Pancakes](#pizza-pasta-and-pancakes)
  - [Skewers](#skewers)
  - [Dish Merges](#dish-merges)
  - [Ocean Foods](#ocean-foods)
  - [Pickles and Ferments](#pickles-and-ferments)
- [Farming](#farming)
  - [Crops](#crops)
  - [Watering and Soil](#watering-and-soil)
  - [Melons and Stems](#melons-and-stems)
  - [Fruit](#fruit)
  - [Wild Crops](#wild-crops)
  - [Trees](#trees)
  - [Composting](#composting)
  - [Bees](#bees)
  - [Incubation Nests](#incubation-nests)
  - [Wild Flowers](#wild-flowers)
- [Worldgen](#worldgen)
  - [Biomes](#biomes)
  - [Nether Blocks in Biomes](#nether-blocks-in-biomes)
  - [Locate](#locate)
  - [Seasons](#seasons)
  - [Time](#time)
  - [Sky Villages](#sky-villages)
- [Structures](#structures)
  - [Structure Rates and Groups](#structure-rates-and-groups)
  - [Meta Structures](#meta-structures)
  - [Earth Fill](#earth-fill)
  - [Terrain Adaptation](#terrain-adaptation)
  - [Structure Contents](#structure-contents)
  - [New Structures](#new-structures)
  - [Sky Structures](#sky-structures)
  - [Nether Block Swap](#nether-block-swap)
  - [Structure Testing](#structure-testing)
- [Mobs](#mobs)
  - [Mob Spawns](#mob-spawns)
  - [Pillagers](#pillagers)
  - [Mooblooms](#mooblooms)
  - [Mob and Block Tweaks](#mob-and-block-tweaks)
- [Loot and Trade](#loot-and-trade)
  - [Loot Tables](#loot-tables)
  - [LootJS Layer](#lootjs-layer)
  - [Lootr](#lootr)
  - [Wandering Trader](#wandering-trader)
- [Gear and Combat](#gear-and-combat)
  - [Archery](#archery)
  - [Camera Bags](#camera-bags)
  - [Echo Gear](#echo-gear)
  - [Shields](#shields)
  - [Gear Tuning](#gear-tuning)
- [Potions and Effects](#potions-and-effects)
  - [Potions](#potions)
  - [Potion Buckets](#potion-buckets)
  - [Effects](#effects)
  - [Enchanting](#enchanting)
- [Recipes and Materials](#recipes-and-materials)
  - [Recipe Progression](#recipe-progression)
  - [Metal and Materials](#metal-and-materials)
  - [Wood Recipes](#wood-recipes)
  - [Magic Recipes](#magic-recipes)
- [Mechanics](#mechanics)
  - [BlockRunner](#blockrunner)
  - [TNT and Portal](#tnt-and-portal)
  - [Misc Gameplay](#misc-gameplay)
  - [Item Physics](#item-physics)
- [Items and Blocks](#items-and-blocks)
  - [Jars](#jars)
  - [Storage and Item Loss](#storage-and-item-loss)
  - [Starter Kit](#starter-kit)
  - [New Blocks](#new-blocks)
  - [Decor and Framed Blocks](#decor-and-framed-blocks)
  - [Flower Pots](#flower-pots)
  - [Dyeing](#dyeing)
  - [Block and Gear Art](#block-and-gear-art)
- [Interface and JEI](#interface-and-jei)
  - [Interface](#interface)
  - [Names](#names)
  - [Tooltips](#tooltips)
  - [Effect Text](#effect-text)
  - [WTHIT](#wthit)
  - [JEI Coverage](#jei-coverage)
  - [JEI Tabs](#jei-tabs)
  - [JEI Ordering](#jei-ordering)
- [Tech](#tech)
  - [Sawmill and Components](#sawmill-and-components)
  - [Dreams and Desires](#dreams-and-desires)
  - [Engineering Recipes](#engineering-recipes)
- [Alcohol](#alcohol)
  - [Alcohol](#alcohol)
  - [Vinery Port](#vinery-port)
  - [Brewin Port](#brewin-port)
- [Mods and Ports](#mods-and-ports)
  - [Mod Integration](#mod-integration)
  - [Hearth and Harvest](#hearth-and-harvest)
  - [Hostile Delight](#hostile-delight)
  - [Mod Updates](#mod-updates)
  - [Karma](#karma)
  - [Creative Labels](#creative-labels)
  - [Autumnity Port](#autumnity-port)
  - [Baby Mobs Port](#baby-mobs-port)
  - [Caverns and Chasms Port](#caverns-and-chasms-port)
  - [Cultural Delights Port](#cultural-delights-port)
  - [Exposure Port](#exposure-port)
  - [Feature Porting](#feature-porting)
  - [Fungi Delight Port](#fungi-delight-port)
  - [Veggies Delight Port](#veggies-delight-port)
  - [Vintage Delight Port](#vintage-delight-port)
- [Creative Delight](#creative-delight)
  - [Creative Delight Framework](#creative-delight-framework)
  - [Creative Delight Members](#creative-delight-members)
  - [Rustic Delight Member](#rustic-delight-member)
  - [Creative Delight Moves](#creative-delight-moves)
  - [Creative Delight QoL Moves](#creative-delight-qol-moves)
  - [Creative Delight Swappers](#creative-delight-swappers)
- [Creative Mods](#creative-mods)
  - [Creative Brewing](#creative-brewing)
  - [Creative Calendar](#creative-calendar)
  - [Creative Split](#creative-split)
- [Tooling](#tooling)
  - [Performance and Server](#performance-and-server)
  - [Server Stability](#server-stability)
  - [Releases](#releases)
  - [Blocked on Dumps](#blocked-on-dumps)
  - [Conventions](#conventions)
  - [Dictionaries](#dictionaries)
  - [Generators and Tests](#generators-and-tests)
  - [patcherMod Cleanup](#patchermod-cleanup)
  - [Texture Tooling](#texture-tooling)
  - [Process](#process)
- [archive](#archive): every **Complete** row, by old family

---

# Food Rules

Food values and types, stack sizes, seasonings, placeable food and food textures.

## Food Tuning

<?todo rows Food Tuning?>

## Stack Sizes

<?todo rows Stack Sizes?>

## Seasoning

<?todo rows Seasoning?>

## Placeable Food

<?todo rows Placeable Food?>

## Food Textures

Texture changes the merge family needs (opened 2026-10-27; each row cites its `merges/` row, where the merge itself is tracked).

Texture and sprite art, pulled out of the other families on 2026-10-25. Each row keeps its brief and writeup links; the old id is in its Details. Rows that only partly concern art stay in their family: `food/drugs #2` (tobacco), `food/seasoning #7` (salt layer), `baking/baked-goods #27` (dough blocks), `dishes/meals #33` (gnocchi), `mobs/nests #3` (nest variety), `loot/lootr #2` (Lootr frames).

<?todo rows Food Textures?>

# Drinks and Drugs

Juices, teas, coffee, smoking.

## Drinks

<?todo rows Drinks?>

## Drugs

<?todo rows Drugs?>

# Kitchen and Cooking

Cans, kitchen blocks, remainder buffers, ovens and cooking recipes.

## Canning

<?todo rows Canning?>

## Kitchen

<?todo rows Kitchen?>

## Remainder Buffers

The remainder buffer on the pot, kettle and oven.

<?todo rows Remainder Buffers?>

## Oven

<?todo rows Oven?>

## Cooking Recipes

<?todo rows Cooking Recipes?>

# Baking and Sweets

Cakes, pies, bread, cookies, pancakes, sweets and jams.

## Cakes and Pies

<?todo rows Cakes and Pies?>

## Breads and Dough

Bread, dough and yeast.

<?todo rows Breads and Dough?>

## Cookies and Muffins

Cookies, muffins and cupcakes.

<?todo rows Cookies and Muffins?>

## Baking Types and Bakeware

Baking food types and bakeware.

<?todo rows Baking Types and Bakeware?>

## Pancakes

Pancake merges and renames.

<?todo rows Pancakes?>

## Sweets

<?todo rows Sweets?>

## Jams

<?todo rows Jams?>

# Dishes and Ingredients

Ingredients, meals, skewers, seafood, ferments and dish merges.

## Ingredients

<?todo rows Ingredients?>

## Sweeteners and Syrups

Sugar, honey, gelatin and glycerol.

<?todo rows Sweeteners and Syrups?>

## Dairy, Fats and Batter

Cornmeal, batter, cheese, fats, butter and doughs.

<?todo rows Dairy, Fats and Batter?>

## Produce and Potatoes

Produce fixes, potatoes, bananas and cabbage.

<?todo rows Produce and Potatoes?>

## Meals

<?todo rows Meals?>

## Meal Rules and Feasts

Rules that apply across meals: heatless shapeless recipes, sequenced assembly, feasts, ingredients audit.

<?todo rows Meal Rules and Feasts?>

## Rice, Egg and Sushi

Fried rice, egg dishes and sushi.

<?todo rows Rice, Egg and Sushi?>

## Soups, Salads and Sandwiches

Soups, salads and sandwiches.

<?todo rows Soups, Salads and Sandwiches?>

## Pizza, Pasta and Pancakes

Pizza, pasta and pancake dishes.

<?todo rows Pizza, Pasta and Pancakes?>

## Skewers

<?todo rows Skewers?>

### BBQ skewer overlap evaluation

Opened 2026-10-31 for `dishes/meals #46`. Every BBQ skewer that shares a dish or an ingredient with a pack item. All are ported as upstream and none is in the skewer family yet, so each verdict is provisional. Values are hunger / saturation points.

| BBQ skewer | Overlaps with | Same dish? | Verdict (provisional) | Decide with |
|---|---|---|---|---|
| Cooked Beef Skewer (8 / 11.2, Strength 50%) | FD Barbecue on a Stick (8 / 14.4) | No: beef, cabbage, grilled vs beef on a stick | Keep both | family fold, `dishes/meals #13` |
| Cooked Lamb Skewer (12 / 19.2, Regeneration 50%) | Veggies Turnip Mutton Skewer (7 / 9.8) | No: mutton only vs mutton and turnip | Keep both; rename to Mutton in lang (D5); value pass (D9) | family fold, D9 |
| Cooked Salmon Skewer (7 / 14) | Oceanic Seafood Skewer (11 / 15.4), FD Grilled Salmon | No: single fish vs mixed fish; fillet vs stick | Keep | family fold |
| Cooked Cod Skewer (7 / 14) | Oceanic Seafood Skewer, Grilled Cod | No | Keep | family fold |
| Cooked Pork Sausage Skewer (8 / 11.2, Resistance 50%) | End's Delight Ender Sausage (name only) | No: no pack pork sausage item | Keep | family fold |

No pack overlap for chicken, mushroom, potato, rabbit or vegetable skewers, the Burnt Skewer or the three combos' skewer inputs.

## Dish Merges

Item merges and splits: one item survives, the purged ids go through `global.itemMerges` ([item_merges.md](../item_merges.md) section 1). Opened 2026-10-27 from the scratchpad merge list and the open merge rows of other families (rows moved here say `(was old-id)` in Details; old ids were rewritten in the other docs). Plans: [item_merges.md](../item_merges.md) section 5 and [briefs/](../briefs/README.md) `merges_*.md`. Built merges stay where they are: salt `food/seasoning #6`, apple ciders / bananas / milk bottles / chocolate cake / coffee `food/drinks #3`, rocky dirts `recipes/materials #3`, permafrost `worldgen/biomes #5`, river wood `recipes/wood #3`.

<?todo rows Dish Merges?>

## Ocean Foods

<?todo rows Ocean Foods?>

## Pickles and Ferments

<?todo rows Pickles and Ferments?>

# Farming

Crops, wild crops, trees, composting, bees and incubation.

## Crops

<?todo rows Crops?>

## Watering and Soil

Watering cans, wet farmland, bone meal and soil quality.

<?todo rows Watering and Soil?>

## Melons and Stems

Melon and pumpkin stems, stages and extra stem fruit.

<?todo rows Melons and Stems?>

## Fruit

<?todo rows Fruit?>

## Wild Crops

<?todo rows Wild Crops?>

## Trees

<?todo rows Trees?>

## Composting

<?todo rows Composting?>

## Bees

<?todo rows Bees?>

## Incubation Nests

<?todo rows Incubation Nests?>

## Wild Flowers

<?todo rows Wild Flowers?>

# Worldgen

Biomes, locating, seasons, time and sky villages.

## Biomes

<?todo rows Biomes?>

## Nether Blocks in Biomes

<?todo rows Nether Blocks in Biomes?>

## Locate

<?todo rows Locate?>

## Seasons

<?todo rows Seasons?>

## Time

<?todo rows Time?>

## Sky Villages

<?todo rows Sky Villages?>

# Structures

Structure rates, metas, earth fill, terrain, contents and testing.

## Structure Rates and Groups

<?todo rows Structure Rates and Groups?>

## Meta Structures

<?todo rows Meta Structures?>

## Earth Fill

<?todo rows Earth Fill?>

## Terrain Adaptation

<?todo rows Terrain Adaptation?>

## Structure Contents

<?todo rows Structure Contents?>

## New Structures

<?todo rows New Structures?>

## Sky Structures

<?todo rows Sky Structures?>

## Nether Block Swap

<?todo rows Nether Block Swap?>

## Structure Testing

<?todo rows Structure Testing?>

# Mobs

Spawns, pillagers, mooblooms and mob and block tweaks.

## Mob Spawns

<?todo rows Mob Spawns?>

## Pillagers

<?todo rows Pillagers?>

## Mooblooms

<?todo rows Mooblooms?>

## Mob and Block Tweaks

<?todo rows Mob and Block Tweaks?>

# Loot and Trade

Loot tables, LootJS, Lootr and the wandering trader.

## Loot Tables

<?todo rows Loot Tables?>

## LootJS Layer

<?todo rows LootJS Layer?>

## Lootr

<?todo rows Lootr?>

## Wandering Trader

<?todo rows Wandering Trader?>

# Gear and Combat

Archery, camera bags, echo gear, shields and gear tuning.

## Archery

<?todo rows Archery?>

## Camera Bags

<?todo rows Camera Bags?>

## Echo Gear

<?todo rows Echo Gear?>

## Shields

<?todo rows Shields?>

## Gear Tuning

<?todo rows Gear Tuning?>

# Potions and Effects

Potions, potion buckets, effects and enchanting.

## Potions

<?todo rows Potions?>

## Potion Buckets

<?todo rows Potion Buckets?>

## Effects

<?todo rows Effects?>

## Enchanting

<?todo rows Enchanting?>

# Recipes and Materials

Recipe progression, metals, wood and magic recipes.

## Recipe Progression

<?todo rows Recipe Progression?>

## Metal and Materials

<?todo rows Metal and Materials?>

## Wood Recipes

<?todo rows Wood Recipes?>

## Magic Recipes

<?todo rows Magic Recipes?>

# Mechanics

BlockRunner, TNT and portals, item physics and other mechanics.

## BlockRunner

<?todo rows BlockRunner?>

## TNT and Portal

<?todo rows TNT and Portal?>

## Misc Gameplay

<?todo rows Misc Gameplay?>

## Item Physics

<?todo rows Item Physics?>

# Items and Blocks

Jars, storage, the starter kit, new, decor and flower pot blocks, dyeing and art.

## Jars

<?todo rows Jars?>

## Storage and Item Loss

<?todo rows Storage and Item Loss?>

## Starter Kit

<?todo rows Starter Kit?>

## New Blocks

<?todo rows New Blocks?>

## Decor and Framed Blocks

<?todo rows Decor and Framed Blocks?>

## Flower Pots

Flower pots, flower boxes, planters and hanging pots: tooltips, models and missing plants (scratchpad 2026-11-02). The pot audit and the Botanist Workbench stay in Decor and Framed Blocks (`blocks/framed-and-decor #5`, `#6`).

<?todo rows Flower Pots?>

## Dyeing

<?todo rows Dyeing?>

## Block and Gear Art

<?todo rows Block and Gear Art?>

# Interface and JEI

Interface, names, tooltips, effect text, WTHIT and JEI.

## Interface

<?todo rows Interface?>

## Names

<?todo rows Names?>

## Tooltips

<?todo rows Tooltips?>

## Effect Text

<?todo rows Effect Text?>

## WTHIT

<?todo rows WTHIT?>

## JEI Coverage

<?todo rows JEI Coverage?>

## JEI Tabs

The world-interaction and process tabs.

<?todo rows JEI Tabs?>

## JEI Ordering

<?todo rows JEI Ordering?>

# Tech

Create and Immersive Engineering content: the sawmill, components, Dreams and Desires, engineering recipes.

## Sawmill and Components

<?todo rows Sawmill and Components?>

## Dreams and Desires

<?todo rows Dreams and Desires?>

## Engineering Recipes

<?todo rows Engineering Recipes?>

# Alcohol

The Vinery and Brewin' ports and the pack's alcohol rules.

## Alcohol

Alcohol is the two ports plus the pack around them, grouped here from `largecontent` and the other families on
2026-10-19: `brewinport` and `vineryport` (the two jars) and `pack` (pack-side alcohol rows). The open calls are in the
decisions table under the main one. Rows that stay elsewhere because they cover more than alcohol: `pack/release #5`
(`/dumpall` refresh with the ports), `ui/names #2` (`vinery:trippy` lang), `jei/jei-coverage #2` (Vinery world
interactions). Both port jars (`-02`) are live since 1.0.8. First in-game results 2026-10-20 (local `-03`, [alcohol.md](../alcohol.md) 10.18:
bottle sizes, box looks, Stained Glass, wine box and flying fixes); AL-01 to AL-72 and VB-01 to VB-66 are still the first
job before any balance change.

<?todo rows Alcohol?>

### Decisions

Open calls, recommendation first, sorted Open, Partly decided, Pinned, Decided, Done. Decided and Done rows stay until
their tests pass. "Built with" means the jar already does the recommendation and the call only confirms it or asks
for a change.

| # | Decision | Status | Options (recommended first) | Blocks | Writeup |
|---|---|---|---|---|---|
| 12 | 1.3.10 reference tests (VN-, VJ-) | Open | A: retire them (the VB- suite covers the port); B: rewrite for the port. VJ- can no longer run: patcherMod's pages were deleted 2026-10-27 | alcohol/vineryport #8 | [vinery.md](../vinery.md), [vinery_jei.md](../vinery_jei.md) |
| 13 | Brewin' / Vinery effects in the brewing stand | Open | Scratchpad "ensure potion effects brewable" excludes them; confirm they stay unbrewable | none | `potions/potions` |
| 7 | Crate and bag merge | Partly decided: Vinery part 2026-10-19 (bags kept, crates removed: [alcohol.md](../alcohol.md) 10.10); other mods open, audited | [storage_audit.md](../storage_audit.md) 4: one block per contents, both ways via NCH; survivors suggested there. Then pick the texture and the surviving id per crop; Vinery is the biggest source | merges/materials #4 | scratchpad 2026-10-19, [alcohol.md](../alcohol.md) B7.12 |
| 2 | Wine effect reshuffle (B7.4): when to unpin | Pinned | A: after the VB- tests and the effects writeup refresh; B: now. When unpinned: two effects per wine, food-mod effects with vanilla / Vinery fallbacks, `tools/wine_effect_candidates.py` | alcohol/vineryport #3 | [alcohol.md](../alcohol.md) B7.4, VB-52 |
| 1 | Vinery wine effects (D4) | Decided (A, 2026-10-19) | A: 1.5.5 default list (one setting, retune later); B: seed with 1.3.10 effects where they still exist | decisions #3 | [alcohol.md](../alcohol.md) B5 D4 |
| 3 | Vinery balance numbers (D5) | Decided (A, 2026-10-19) | A: 1.5.5 barrel / press / growth defaults, pack ageing pace (16 days a year, 2 years a level, now months via `[time]`); mead's juice cost is moot (mead removed) | alcohol/vineryport #9 | [alcohol.md](../alcohol.md) B5 D5, [time.md](../time.md) |
| 4 | Grape stomping yield | Decided (A, 2026-10-19) | A: 12 grapes, 4 stomps, 4 bottles (scratchpad); B: keep 1.5.5's 12 stomps | alcohol/vineryport #9 | [alcohol.md](../alcohol.md) B7.14, VB-10 |
| 5 | Foundation, village houses, hanging sign, rotten cherry, bamboo racks (D7, D8, D9, D10, D12) | Decided (A, 2026-10-19) | A: bundled / skip / drop / keep at 0.15 / keep from `quark:bamboo_planks`; B for each in B5 | none | [alcohol.md](../alcohol.md) B5 |
| 9 | Scrambling on clients (4.3) and Dread Nog poison potion (4.4) | Decided (A, 2026-10-19) | A: keep the port's shape (client-side scramble settings; poison potion returning its bottle); B: 4.5.0's server-side scramble / ominous bottle | none | [alcohol.md](../alcohol.md) 4.3, 4.4 |
| 10 | Which drinks skip the rotten-food type | Decided (2026-10-20, your 11 list) | None skip: every alcoholic drink is half a rotten shank with no saturation; only non-alcoholic ciders (FD, Farmer's Respite) are left alone | `alcohol/pack #1` | [alcohol.md](../alcohol.md) 11 |
| 8 | Keg heat / cold tags (2.7) | Done, testing (your list, [alcohol.md](../alcohol.md) 10.13): furnaces, bonfires, braziers, running machines, fueled burners heat; snow, powder snow, IW thin ice / permafrost / cryosol cool; late the same day the remaining 2.7 "yes" rows (Botania blaze block; `#minecraft:ice`, Architects Palette packed ice); fridge and CFB oven not added. Tags live in `blocktags.js`; cold already works with the installed `-02`, the heat additions and running / fueled gates came with Brewin' `-03` (its keg reads `#brewinandchewin:heat_sources`; `-02` read FD's), shipped in 1.1.01 (deployed 2026-10-22) | none | alcohol/brewinport #2 | [alcohol.md](../alcohol.md) 2.7, 10.13; AL-73 to AL-77 |
| 11 | Hopper input on press and cask | Done, testing (2026-10-20): B, limited now; VN-05 / VN-08 were 1.3.10 tests and the port already refused junk, so the fix targets the jams the port still had ([alcohol.md](../alcohol.md) 10.19) | A: check VN-05 / VN-08 with the port first; B: limit now | alcohol/vineryport #7 | [vinery.md](../vinery.md) 4, VB-93, VB-94 |

## Vinery Port

The Backported Vinery jar and its follow-ups.

<?todo rows Vinery Port?>

## Brewin Port

The Backported Brewin' and Chewin' jar and its follow-ups.

<?todo rows Brewin Port?>

# Mods and Ports

Third-party mod integration and updates, other ports, Karma and Creative Labels.

## Mod Integration

<?todo rows Mod Integration?>

## Hearth and Harvest

Owner change list for the Hearth and Harvest member (Creative Delight, `development/creativeDelight/members/hearth`), from the scratchpad 2026-11-09. Item status and judgement calls: [hearth_and_harvest.md](../hearth_and_harvest.md) "Owner notes (scratchpad 2026-11-09)".

<?todo rows Hearth and Harvest?>

## Hostile Delight

Hostile Delight integration: the umbrella row and the owner's 2026-11-04 notes ([hostile_delight.md](../hostile_delight.md) 5.1).

<?todo rows Hostile Delight?>

## Mod Updates

Newer upstream versions of installed mods and new ports.

<?todo rows Mod Updates?>

## Karma

Karma example actions, moved verbatim from the scratchpad:
- Good: planting saplings (low limit); bonemeal to compost (low); trading with villagers (low); helping in raids
  (cooldown, high); killing pillagers and witches (cooldown, low); killing the Ender Dragon (exponential decrease,
  negative offset after x kills?); breeding animals (linear decrease to 0, low); taming pets (linear decrease to low,
  low); playing with pets (high karma, high limit); setting pet spawn points (linear decrease to low, cooldown, high);
  curing zombie villagers (slow linear decrease to low, high); tilling, planting, harvesting, planting flowers (high
  limit, slow linear increase, 5 min cooldown); food pantry server feature?; writing books, signing albums (linear
  decrease to low, high boost, high limit); taking photos (low boost, low limit); Via Romana path creation (linear
  increase to high, high limit).
- Bad: breaking claimed beds (small for villager-owned, large for player-owned, linear increase on repeats); being
  mean (no cooldown, high limit: talking rude to MCA villagers, punching villagers or baby animals, killing villagers,
  more for baby villagers and golems); pet respawning (high limit, exponential, no cooldown); killing pacifist or
  protected mobs (exponential, high limit, 1 min cooldown: bats, dolphins, axolotls, pandas, polar bears, elephants,
  whales, Alex's Mobs with no drops: review); killing animals (slow linear from low, 10 s cooldown, buffer of no
  effect); igniting TNT (constant low, no cooldown); breaking a village bell (exponential, no cooldown, high);
  breaking wild waystones (small linear if never activated, large exponential if another player activated it, and a
  chat announcement naming the destroyer); igniting a Nether portal (failing: one large decrease with a low limit,
  chat announcement the first time per player; succeeding: slow linear decrease, low limit, first-time announcement);
  attacking neutral mobs (slow linear, high cap: zombie pigmen, passive spiders, hitting or looking at endermen);
  stealing honey without a campfire (low exponential, low limit); refuelling a boat (slow linear, low limit); falling
  into the void (exponential, 5 min cooldown, high limit); looting another player's corpse (quick exponential, high
  limit); killing a named mob (one large decrease per mob; owner 2026-10-30; hook in patcherMod `mobs.NamedMobs.onDeath`,
  `mobs/tweaks #6`).

<?todo rows Karma?>

## Creative Labels

<?todo rows Creative Labels?>

## Autumnity Port

<?todo rows Autumnity Port?>

## Baby Mobs Port

<?todo rows Baby Mobs Port?>

## Caverns and Chasms Port

<?todo rows Caverns and Chasms Port?>

## Cultural Delights Port

<?todo rows Cultural Delights Port?>

## Exposure Port

<?todo rows Exposure Port?>

## Feature Porting

<?todo rows Feature Porting?>

## Fungi Delight Port

<?todo rows Fungi Delight Port?>

## Veggies Delight Port

<?todo rows Veggies Delight Port?>

## Vintage Delight Port

<?todo rows Vintage Delight Port?>

# Creative Delight

The Creative Delight jar and its members.

## Creative Delight Framework

The Creative Delight jar (`development/creativeDelight`) and everything that moves into it. Opened 2026-10-25 from
[creative_delight.md](../creative_delight.md); rows were taken from `ports/creativedelightport` and `food/types-and-tuning #14`.
Subfamilies are A to Z, which is also the work order: **framework** (loader, build, tests, dict, config, harness) first, then
**members** (each Delight mod's rows and Java moving in), **qol** (QoL-1 to QoL-6 leaving patcherMod and the pack folder),
**swappers** (later). A member row needs framework #2 (install), #4 (dict) and #6 (move-diff harness) first; a QoL row needs framework #5 (config key) and, for
Java, #8 (compartments). Owner rule: no ongoing backport or merge features beyond what these rows say.

<?todo rows Creative Delight Framework?>

## Creative Delight Members

<?todo rows Creative Delight Members?>

## Rustic Delight Member

Rustic Delight as a Creative Delight member.

<?todo rows Rustic Delight Member?>

## Creative Delight Moves

Moving mod rows and code into the Creative Delight trees.

<?todo rows Creative Delight Moves?>

## Creative Delight QoL Moves

<?todo rows Creative Delight QoL Moves?>

## Creative Delight Swappers

<?todo rows Creative Delight Swappers?>

# Creative Mods

Creative Brewing, Creative Calendar and the patcherMod split.

## Creative Brewing

Creative Brewing (built 2026-10-31, [creative_brewing.md](../creative_brewing.md)), the own mod that holds what Creative Delight does not take (owner, 2026-10-25: "mark all potion brewing, alcohol, vinery, and effect descriptions, changes, shuffles to be moved to a new mod, Creative Brewing"). Nothing moved yet.

<?todo rows Creative Brewing?>

## Creative Calendar

A future own mod, Creative Calendar, for the calendar, day and night cycle, seasons and weather / effects by biome; Creative Delight and Creative Brewing will both depend on it (owner, 2026-10-31). Reserved only: nothing moves until a plan is written.

<?todo rows Creative Calendar?>

## Creative Split

Split patcherMod into Creative Common (core) and high-level content jars (owner, 2026-11-01). Plan: [creative_split.md](../creative_split.md).

<?todo rows Creative Split?>

# Tooling

Performance, server and releases, KubeJS helpers and conventions, generators, dumps, texture tooling.

## Performance and Server

<?todo rows Performance and Server?>

## Server Stability

Crashes, corruption and mob cap bugs on the server.

<?todo rows Server Stability?>

## Releases

`pack/docs` has nothing open: rows 1 to 7 of the 2026-10-19 rework were done the same day (status sweep, old TODO
citations, README index, Immersive Paintings writeup, stale comments, ST-53 / ST-160 text, Exposure `-04`).

<?todo rows Releases?>

## Blocked on Dumps

| Row | Waits for | Queue |
|---|---|---|
| `pack/release #5` | the generator runs on the 2026-10-30 dump (`food_catalog.py`, `gen_stack_sizes.py`, `gen_can_types.py`, `effect_audit.py` with `EFFECT_OVERRIDES` emptied); `/cdevdumpnames`, `/cdevdumpicons`, `/dumpwinetextures` | CQ-DUMP-03, 04 |
| `ports/fungiport #2`, `ports/vintageport #2` | dumps done 2026-10-30 (`AWAIT_DUMP` empty since 2026-10-25): the stack, placeable, names, JEI, tier and WTHIT steps on them | CQ-DUMP-04, 06 |
| `food/stack-sizes #2` | dump done 2026-10-30: `gen_stack_sizes.py` / `stack_sizes.py` on it | CQ-DUMP-04 |
| `food/placeable #2` | dump done 2026-10-30: reading `food/placeable.json` | CQ-DUMP-06 |
| `cans/canning #3, #14`, `dishes/ocean #3`, `dishes/pickles-and-ferments #1`, `dishes/meals #32` | `/dumpfood` done 2026-10-30, then `/cdevgen` (glow, pickle and mhadjeb cans) | CQ-DUMP-04 |
| `alcohol/brewinport #2, #4` | dumps done 2026-10-30 (`tags/item_tags.json`): reading them | CQ-DUMP-06 |
| `ui/names #4`, `ui/wthit #2`, `textures/tools #1` | `/cdevdumpnames`, `/cdevdumpicons` (last 2026-10-20), `/dumpwinetextures` (never run) | CQ-DUMP-03 |
| `worldgen/biomes #8, #4` | End climate dump and `/locate biome` on a live End | CQ-DUMP-05 |
| `worldgen/biomes #10` | Visceral Heap in the Nether (Biomes O' Plenty, one biome) | Testing | S+ C P T | Worldgen / data | **r1115: SQ-VH-06, -06b PASS (all flesh); -06c PASS with a finding (border spill, `worldgen/biomes #16`); -11 RECORD.** BUILT and installed 2026-11-09 ([visceral_heap.md](../visceral_heap.md), tests `VH-`, scratch SQ-VH-01 to -12, CQ-VH-01 to -03). Never booted. After the first boot: `/dumpall`, `/cdevgen run all dumps`, `/dumpclimate`, the biome list readers, `integration-report` for the kept blocks. Targets 2026-11-09 (not yet solved): Heap 5, Wastes 30, Soul Sand 20, Crimson 15, Warped 15, Basalt 15 % | none | [plan_visceral_heap.md](../plan_visceral_heap.md) |
| `worldgen/biomes #16` | Visceral Heap features spill into a neighbouring biome | Planned | S C | Creative Worldgen / decision | r1115 SQ-VH-06c: Heap features (flesh, 2212 blocks) spill up to 12 blocks into a neighbouring warped forest; the surface rule is fine (flesh 0 in four other biomes, their vanilla blocks kept). | owner (design): is border spill acceptable, or keep Heap features off border chunks | [scratch_run_2026-11-15.md](../testing/scratch_run_2026-11-15.md) 8 Phase 1, [visceral_heap.md](../visceral_heap.md) 5 |
| `worldgen/biomes #11` | Visceral Heap flesh recipes (crush, blood, cultivation, climbable tendons) | Testing | S C P T | KubeJS / Engineering / Common | Owner 2026-11-10: plan [plan_flesh_recipes.md](../plan_flesh_recipes.md). BUILT 2026-11-10: recipes in `kubejs/data/creativeworldgen/kubejs/recipes/visceral_heap.js` (`requires: biomesoplenty`), climbable tag, dict keys and `NCH.createFluidTag` / `{ pools }` (Common rebuilt), Create result pools with JEI slots, 24-output cap and 32-slot crusher inventory (Engineering rebuilt). Both jars NOT installed (game open); needs a restart. SQ-FLR-01 to -06, CQ-FLR-01 to -03. Item physics for the Heap items deferred (plan section 6). Scratchpad 2026-11-13: the flesh crush, cultivation and blood notes are this row (executed; the plan changed two figures: oil cultivation gives 2 porous + 3 flesh, not 4 flesh; pus bubble and hair give slimeball and string, not organic waste) and the spongy / very sticky walk tiers are built (BR-57). | oil amount after testing | [visceral_heap.md](../visceral_heap.md) 6 |
| `worldgen/biomes #12` | Drop the duplicate tilled end stone | Planned | S P | KubeJS / dicts | Owner 2026-11-10: `item_dict.js` defines `i.tilled_end_stone` twice (line 131 `hearthandharvest:tilled_end_stone`, later `endercrop:tilled_end_stone`; the second wins). Drop the Hearth and Harvest one and use the Ender Crop version everywhere: remove the H&H block and its recipes and uses, swap or alias existing ids so worlds keep them, and delete the first dict line. Check what in the pack points at the H&H id first. | what to do with placed H&H blocks | scratchpad 2026-11-10 |
| `food/types-and-tuning #15, #6`, `baking/baked-goods #24`, `ui/effects #10` | reading the new food and recipe dumps | CQ-DUMP-06 |
| `creativedelight/members #44`, `#50`, `creativedelight/swappers #2`, `creativedelight/framework #6`, `dishes/meals #12`, `#46`, `food/types-and-tuning #3`, `tooling/generators-and-tests #11` | first boot of the 2026-11-03 jars, then `/dumpall` (`gen_loot_config.py`, merge recount, move-diff baseline), `/cdevgen food_families`, `/dumpfood` + `gen_can_types.py`, `/dumpcompost` | SQ-CDJ6, SQ-CDJ7 |

## Conventions

<?todo rows Conventions?>

## Dictionaries

<?todo rows Dictionaries?>

## Generators and Tests

<?todo rows Generators and Tests?>

## patcherMod Cleanup

<?todo rows patcherMod Cleanup?>

## Texture Tooling

<?todo rows Texture Tooling?>

---

## Process

From [process_review.md](../process_review.md) (2026-11-16). Plan: [plan_process_overhaul.md](../plan_process_overhaul.md).

<?todo rows Process?>

---

# archive

Rows that reached **Complete** (tested enough from real play to count as functional), moved out of the family tables on 2026-10-26. A row keeps its `family/sub #n` id, so writeups that cite it still resolve; sorted by family, subfamily, number. Open follow-ups stay in the writeup, not here. New completions are appended here at each sweep.

<?todo archive?>
