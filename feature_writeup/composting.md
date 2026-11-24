# Composting: the pack's composter table, rules, values and exclusions

The 12 wooden composters are Creative Delight content since 2026-11-24 (moved from Creative Common in the wave 2 carve,
[plan_wave2_publish.md](plan_wave2_publish.md) W2-9; [creative_delight.md](creative_delight.md)).

Test prefix `CM-` (the backlog proposed `CP-`, which [can_parity.md](can_parity.md) already uses).

## 0. At a glance

<!-- generated:status_index begin open_work sha1=8cf060597fab -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 4 open TODO rows (Planned 1, Testing 3), 1 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `farming/composting #2` | Planned | S P T | Fertile Soul Soil (Nulch) recipe: review, then move into the compost file |
| `farming/composting #4` | Testing | S+ P T | Seed milling to bone meal |
| `farming/composting #5` | Testing | S P | Expand organic compost |
| `farming/composting #6` | Testing | S P | Merge the organic mash compost recipe |
<!-- generated:status_index end open_work -->

| Part | Status |
|---|---|
| `global.compost`: rebuilds the composter table (`ComposterBlock.COMPOSTABLES`) from nothing every time tags load | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3) |
| Rules: 10 categories by tag, id pattern, block class and edibility, first match wins | DEPLOYED |
| Values: 688 explicit per-item chances (generated once from the mods' own table, hand-edited since) | DEPLOYED (688, the 4 Brewin' port rows of 2026-10-19 ([alcohol.md](alcohol.md) 10.16) with 1.1.01) |
| Exclusions: never compostable (Canned Goods, golden food, incomplete items, Botania world seed and altered grass, ...) | DEPLOYED |
| `/dumpcompost` (before / after / why) and `tools/gen_compost_values.py` (`--add-new` after adding mods, `--check`) | in use |
| Compost and fertile soil crafting from mulch (in `sawdust.js`) | DEPLOYED; belongs to recipe progression (backlog row 20) |

- **Pending:** seed milling to bone meal (built 2026-10-30, shipped 1.1.05, TODO `farming/composting #4`, SQ-CP-01 scratch server PASSED r1031 2026-10-31). CM-01 to CM-08 have never been run. Findings F1 to F4 (section 4); none
  needs a decision.
- **Files to open:** [compost_rules.js](../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/compost_rules.js) (categories, exclusions),
  [compost_values.js](../kubejs/startup_scripts/compost/compost_values.js) (per-item chances),
  Creative Delight's `kubejs_startup/<mod>/compost.js` (the Farmer's, Fungi, Veggies and Vintage Delight rows, QoL-4
  since 2026-10-25, switch `[qol] compost`; `values(..., { when })`), Creative Common
  [func/compost.js](../development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/compost.js) (engine).
- **Knobs:** the two data files. After editing: `/kubejs reload startup_scripts`, then `/reload`.

## 1. Overview

Vanilla's composter reads one map, item -> chance of adding a layer (7 layers fill it, then bone meal comes out). Vanilla
registers its items in five levels (0.3, 0.5, 0.65, 0.85, 1.0); every mod registers its own items however it likes, or
not at all. In a pack with hundreds of food and plant mods the table was patchy: many plants missing, some odd items
compostable. Hoppers and every modded composter read the same map.

The pack takes the table over: it clears it and refills it from its own rules and values, in the same way on a
dedicated server and its clients.

## 2. Reference

### 2.1 Tiers

| Tier | Chance | Vanilla examples |
|---|---|---|
| `low` | 0.3 | seeds, saplings, leaves, grass |
| `medium` | 0.5 | tall grass, vines, cactus, melon slice |
| `high` | 0.65 | flowers, crops, mushrooms, lily pad |
| `very_high` | 0.85 | bread, baked potato, hay, mushroom blocks |
| `full` | 1.0 | cake, pumpkin pie |

### 2.2 Categories (first match wins)

| Category | Chance | Matches |
|---|---|---|
| seeds | low | seed tags, `*seed(s)` ids, crop/stem blocks; not food; not seed bags, seed pouches, seed oil |
| saplings | low | sapling tag, `*_sapling` / `*propagule`, sapling blocks |
| leaves | low | leaves tag, `*_leaves` / leaf piles / leaf carpets |
| flowers | full | flower and petal tags, Botania floating flowers, petals, Floral Enchantments bushes / vines / baskets, flower blocks |
| mushrooms | high | mushroom tag, mushroom and fungus blocks |
| doughs | very_high | `#forge:dough` and ids ending in `dough` (Create, oat, cucumber, sweet potato, sourdough, cookie, ginger); owner 2026-11-01 |
| crops | high | crops, vegetables, fruit, berries, grain, nuts, tea leaves, raw veggie and fruit food types, crop/cocoa/berry/vine/wart blocks |
| baked | very_high | baked and cooked plant food types, `*bread*` / `*cookie*` (not molasses) |
| cakes_pies | full | `*pie*` / `*cake*`, cake blocks (not blaze/echo/slime/powder cakes, hats, molasses, plated food) |
| plant_matter | medium | straw, bark, sawdust, mulch, hay, thatch, packed plant blocks, mushroom blocks, moss (not hats, creosote, hay nests, reinforced) |
| plants | medium | grass, ferns, vines, kelp, seagrass, lichen, roots, moss, any bush/vine/plant block, anything Forge calls plantable |

Meat, fish, dairy, eggs, soups and meals never match a rule.

### 2.3 Values and exclusions

- **Values** (688 items): explicit chances that beat the rules. They started as every item the mods had made
  compostable, at the mods' own chance, grouped by the category the rules would give them; hand-edited since (Botania's
  seeds at full, for example).
- **Exclusions** (beat everything): `botania:world_seed`, Botania's altered grass blocks, muddy mangrove roots and
  petrified blocks, all of `canned_goods:`, golden food (`goldenfood:`, `*golden_*` except golden seeds,
  `#food_type/golden`), `*incomplete_*` items, `endercrop:`.

### 2.4 `/dumpcompost`

Writes `kubejs/exported/compost/compost.json`: `original` (the table as vanilla and mods registered it, captured on
the first rebuild), `now` (each item's chance, whether a value or a rule put it there, and which category), and
`excluded` (items a value, rule or the original table would compost but an exclusion blocked). Part of `/dumpall`.

## 3. What the pack changes

- **Engine** (`global.compost`, `func/compost.js`, priority 900, after the dictionaries and before the two data files):
  `tiers()`, `category(name, chance, { tags, ids, blocks, items, edible, except })`, `values(category, rows)`,
  `exclude(entries)`, `apply(cause)`, `report()`.
- **When it runs:** a Forge `TagsUpdatedEvent` listener (registered from the startup script) rebuilds the table: at server
  start, on `/reload`, and on each client when it receives the server's tags. So a dedicated server and its clients
  build the same table from the same scripts, and categories that read tags see the live tags.
- **Rebuild order:** clear -> rules -> values (beat rules) -> exclusions (beat both). The log has `[compost] <cause>: N
  compostable (...)` and warnings for values naming items that don't exist or an unknown tier.
- **Seed milling (2026-10-30, TODO `farming/composting #4`):** `server_scripts/wood/compost.js` mills any seed in
  `#creativedelight:millable_seeds` into bone meal at 15 % (Create millstone and crushing wheels; the tag is built in
  `tags/wood_tags.js` from `#forge:seeds` and `#c:seeds` minus corn kernels, which mill into cornmeal, and rice). The
  composter needs about 21 seeds per bone meal; the mill about 7. Test CP-01 (scratch queue SQ-CP-01): PASSED r1031 (2026-10-31). Shipped 1.1.05 (Gameplay bullet).
- **Generator:** since 2026-10-25 (creative_delight.md 29) the rows are 35 generated `<mod>/compost_values.js` files in
  Creative Delight's startup tree (`development/creativeDelight/src/main/resources/kubejs_startup/`), and
  `tools/gen_compost_values.py` regenerates them exactly from a `/dumpcompost` (its `value` rows); hand rows go in a
  `<mod>/compost.js` there, which the generator skips and which win by load order. `--add-new` adds the new mods' own
  compostables at their original chance; `--check` writes nothing and exits 1 on any difference. Ids the item dict lacks get keys in a generated block of
  `item_dict.js`.
- **Organic Compost slots (2026-11-20, TODO `farming/composting #5`, `#6`):** the rot slot (`#forge:mulch/waste`,
  Creative Delight `creativedelight/compost_tags.js`) also takes rotten tomato, rotten cherry and Vintage Delight's
  organic mash (the jam -> vinegar leftover), so Vintage Delight's own two mash compost recipes are removed
  (`vintagedelight/recipes.js`; its tree fertilizer stays). The dirt slot tag `#creativedelight:compost_dirt`
  (`i.t_compost_dirt`) = dirt, coarse dirt, rooted dirt, podzol, moss block, mud, Immersive Weathering humus and Twigs
  silt (owner R7: no grass block or mycelium, silk touch only; no rich soil, an output tier); Creative Engineering's
  `wood/sawdust.js` takes it in the dirt slot of its two crafting and two Create mixing Organic Compost recipes (G3, 2026-11-21). Organic mash composts at 0.5 (plant matter; a hand row in
  CD `vintagedelight/compost.js`; it was not compostable). Tests SQ-CP-02, SQ-CP-03, SQ-CP-04.

- **Wood composters (2026-11-21, TODO `farming/composting #3`, [brief](briefs/farming_composting.md) "composting #3"):**
  Creative Delight registers `creativedelight:<wood>_composter` for spruce, birch, dark oak, jungle, acacia, mangrove,
  twisted, ancient, blossom, azalea, crimson and warped (`farm/composter/WoodComposters`); oak stays `minecraft:composter`.
  Each is a `ComposterBlock` subclass, so the shared table above, hoppers, the comparator, the level-7 -> 8 tick and
  bone meal are vanilla's; WTHIT's level line registers on the `ComposterBlock` class and covers them. Fire 5 / 20 and
  fuel 300 ticks like vanilla, none for crimson and warped; map color from the wood's planks. Loot: itself (explosion
  decay) plus 1 bone meal at level 8. Model: vanilla's composter parent with `creativedelight:block/<wood>_composter_{side,top,bottom}`
  (palette swaps, plan R8), vanilla fill models. Recipes (Delight's server script `creativedelight/composters.js`): that wood's 7
  slabs in the vanilla shape. Vanilla's `minecraft:composter` recipe (`#minecraft:wooden_slabs`, mixed species too) stays,
  so one species' slabs match both and Polymorph offers the choice, the wood composter listed first (Polymorph sorts by
  translation key; owner 2026-11-21). Each wood composter crafts back into the vanilla one, shapeless 1 to 1. Tags
  `#creativedelight:composters` (block and item, vanilla's and the 12; in the Compat item physics floats list when Delight
  is loaded), the 12 in `#minecraft:mineable/axe`. Dict keys `i.composter_<wood>`, `i.t_composters` (kept; values follow
  the new namespace). Saves with the old `creativecommon:` ids are swapped by Delight's `swaps/common_composters.json`.
  Texture credit (palettes from Quark and Architect's Palette planks): Delight's NOTICE (publish_readiness.md PR-CC-12, ruling W1).

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. Rules can catch odd items.** Id patterns and block classes are broad (anything plantable, anything named
  `*_grass`, `*bark*`, `*cake*`). The exclusions and `except` lists cover the cases found so far; after adding mods,
  check `/dumpcompost`'s `now` for surprises (CM-06).
- **F2. A two-step reload.** Edits need `/kubejs reload startup_scripts` and then `/reload`; `/reload` alone keeps the
  old rules.
- **F3. `gen_compost_values.py` isn't in `/cdevgen`.** It's a manual `--add-new` after adding mods (output is now deterministic, so it could join; was: the file is hand-edited,
  so a blind regeneration would be wrong). Fine as is; noted for the tooling list ([pack_tooling.md](pack_tooling.md) F5).
- **F4. The nulch recipe** sits in `sawdust.js` (line 83) with a note that it belongs with compost ([TODO](todo/TODO.md) `farming/composting #2`).

- **Planned (scratchpad 2026-10-21):** milling seeds gives a small chance of bone meal, worse than the composter
  (32 seeds to 1 bone meal) ([TODO](todo/TODO.md) `farming/composting #4`).

### Known limits

- **Wood composters are not job sites:** the farmer's POI (`PoiTypes.FARMER`) holds only `Blocks.COMPOSTER` states
  and `WorkAtComposter` checks `Blocks.COMPOSTER`, so a villager does not take a wood composter as a farmer job site or
  compost into one. Kept as in the brief's recommendation (2026-11-21); it would take mixins on the POI states and on
  `WorkAtComposter`. Other mods that test `Blocks.COMPOSTER` by identity (not the class) skip them too.
- **Mixed slabs:** a grid of two species' slabs makes the vanilla composter.
- **Composter only:** the rules decide the composter chance, not any other mod's composting machine with its own recipe
  list.
- **Excluded means never:** an excluded item can't be made compostable by a value row.

### Rejected options

- **Adding to the mods' table:** leaves every odd mod registration in place; a full rebuild is the only way to remove
  them.

## 5. File map

- [startup_scripts/func/compost.js](../kubejs/startup_scripts/func/compost.js), [development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/compost_rules.js](../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/compost_rules.js), [startup_scripts/compost/compost_values.js](../kubejs/startup_scripts/compost/compost_values.js).
- [server_scripts/_lib/dump_compost.js](../kubejs/server_scripts/_lib/dump_compost.js) (`/dumpcompost`).
- [tools/gen_compost_values.py](../tools/gen_compost_values.py).
- Related recipes: [wood/sawdust.js](../kubejs/data/creativeengineering/kubejs/wood/sawdust.js) (compost, fertile soil, mulch).
- Wood composters: [farm/composter/](../development/creativeDelight/src/main/java/com/SaintAlfonzo/creativedelight/farm/composter/WoodComposters.java) (Creative Delight), [creativedelight/composters.js](../kubejs/data/creativedelight/kubejs/creativedelight/composters.js) (recipes, tags), [swaps/common_composters.json](../development/creativeDelight/src/main/resources/data/creativedelight/swaps/common_composters.json) (old ids).

## 6. Offline checks

`python3 tools/gen_compost_values.py --add-new --dry-run` against a fresh `/dumpcompost` lists any mod compostables that
have no row yet.

## 7. In-game test suite

Use a composter and watch its level; or read `/dumpcompost`.

- [ ] **CM-01 Rebuild log:** `/reload` -> `[compost] ...: N compostable` in the log; no "do not exist" warnings.
- [ ] **CM-02 Seeds and flowers:** wheat seeds raise the level about 3 times in 10; any flower (vanilla or Botania) every
  time.
- [ ] **CM-03 Modded crop:** a Farmer's Delight tomato -> about 65 %.
- [ ] **CM-04 Baked:** bread -> about 85 %; a pie -> always.
- [ ] **CM-05 Excluded:** a Canned Goods can, a golden carrot and a Botania world seed -> refused.
- [ ] **CM-06 Dump:** `/dumpcompost` -> `kubejs/exported/compost/compost.json` with `original`, `now`, `excluded`; skim
  `now` for odd items (F1).
- [ ] **CM-07 Hopper:** a hopper feeding seeds into a composter -> accepted at the same chance.
- [ ] **CM-08 Client agrees:** on the dedicated server, right-click a composter with a modded crop -> the client shows
  the fill (no desync), same as the server.
- [ ] **CM-09 Compost dirt slot:** JEI on Organic Compost -> the dirt slot of the two crafting and two mixing recipes
  cycles dirt, coarse dirt, rooted dirt, podzol, moss block, mud, humus and silt; podzol crafts one (SQ-CP-04).
- [ ] **CM-10 Wood composter recipe:** 7 spruce slabs in the composter shape -> Spruce Composter; 7 oak or bamboo slabs
  -> Composter; 7 spruce slabs -> Polymorph button lists Spruce Composter first, then Composter, and either can be
  picked; mixed spruce and birch slabs -> Composter; a Spruce Composter alone in the grid -> Composter. Queue SQ-CM-37
  (recipes), CQ-CM-37 (Polymorph).
- [ ] **CM-11 Same table:** fill a Warped Composter with wheat seeds and a pie -> same chances as CM-02 / CM-04; at level 7
  it turns ready after a moment; right-click -> 1 bone meal. Queue SQ-CM-38.
- [ ] **CM-12 Hopper and comparator:** hopper on top feeds seeds in; hopper below pulls the bone meal out at level 8;
  comparator output rises with the level. Queue SQ-CM-39.
- [ ] **CM-13 Drops:** break a Birch Composter at level 8 with an axe -> the composter and 1 bone meal; at level 3 -> only the
  composter. Queue SQ-CM-40.
- [ ] **CM-14 Look and WTHIT:** each of the 12 shows its wood, the fill and the ready texture; WTHIT shows the level line. Queue CQ-CM-42.
- [ ] **CM-15 Fire and fuel:** a lit Acacia Composter burns away, a Crimson Composter does not; an Acacia Composter
  smelts 1.5 items in a furnace, a Crimson Composter is not fuel. Queue SQ-CM-41.
- [ ] **CM-16 Moved to Creative Delight:** an old save's Common composters load as Delight's (placed, chest, inventory); the
  recipes, tags and floats list use the new namespace; Common alone registers none. Queue SQ-CM-42, SQ-CM-43.

## 8. History

- By 2026-09-27 (initial git commit): engine, rules, exclusions, `compost_values.js` generated from the first
  `/dumpcompost`, the dump and the generator; shipped with 1.0.3.
- 2026-10-02: values merged for the 1.0.4 mods ([plan_1.0.4.md](plan_1.0.4.md) DR).
- 2026-10-18: this writeup; findings F1 to F4; tests CM-01 to CM-08. No code changed.
- 2026-10-19: the Brewin' 4.5.0 port's four chances (kimchi, pickled pickles, quiche, quiche slice) added by hand as
  rows ([alcohol.md](alcohol.md) 10.16); 688 values. Not deployed yet.
- 2026-10-31: scratchpad notes translated into TODO rows (`farming/composting #5`). No code changed.
- 2026-11-01: `doughs` category (0.85, before crops) in CD `compost_rules.js`: every dough composts, Create's included
  (owner, raised by the Vintage dump step `ports/vintageport #2`); SQ-VDF-10. Needs the CD jar.
- 2026-11-20: Organic Compost rot slot (+ rotten tomato, rotten cherry, organic mash), the `compost_dirt` tag, Vintage
  Delight's mash compost recipes removed, organic mash at 0.5 (section 3; plan_owner_rows_1120.md G2b). Needs the CD
  jar; the dirt slot swap is G3.
- 2026-11-21: Organic Compost dirt slot swapped to `#creativedelight:compost_dirt` in Creative Engineering's `wood/sawdust.js`
  (plan_owner_rows_1120.md G3). Needs the Creative Engineering jar. SQ-CP-04.
- 2026-11-21: wood composters built (plan_wood_bucket_batch_1121.md P2 item 1): Creative Common blocks, assets, loot,
  recipes and tags (section 3); CM-10 to CM-15. Needs the Creative Common jar and the texture PNGs.
- 2026-11-24: wave 2 carve (TODO `creative-common-publish #18`, ruling W2-9): the 12 composters moved to Creative Delight
  with Delight's namespace (blocks, items, assets, loot, lang, recipes, tags, dict values); old ids swapped by
  `swaps/common_composters.json`; texture credit in Delight's NOTICE. CM-16, SQ-CM-42, SQ-CM-43.
