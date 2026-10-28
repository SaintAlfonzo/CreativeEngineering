# Placeable food

Test prefix **PL-** · Last updated **2026-10-25** · Overall status **DEPLOYED with 1.0.6 (2026-10-14), coaster line with 1.0.8 (2026-10-19), not run in game; 2026-10-25 produce on the coaster and 9 Display Delight egg dishes BUILT, not installed** (catalog rerun on the 2026-10-24 live `/dumpplaceable`)
live `/dumpplaceable` export of 2026-10-18, rerun 2026-10-20; the tags and client script loaded
without errors in single player 2026-10-11)

## 0. At a glance

Every food that can be put down in the world gets one, two or three tooltip lines, for any mod's placement
mechanic: "Placeable" (with "(while sneaking)" when sneaking is required), "Placeable on plate", "Placeable on small plate"
and "Placeable on a coaster". The lines come from five item tags generated from a catalog.

| Part | Where | Status |
|---|---|---|
| `/dumpplaceable` (live Display Delight links + every food-looking block item) | [dump_placeable.js](../kubejs/server_scripts/_lib/dump_placeable.js), also in `/dumpall` | DEPLOYED with 1.0.6, run 2026-10-18 |
| Catalog + tag lists generator | [tools/placeable_catalog.py](../tools/placeable_catalog.py) → [placeable_catalog.md](placeable_catalog.md), [placeable.json](../kubejs/generation_scripts/food/placeable.json) | BUILT, run on the 2026-10-18 live dump |
| Tags `#creativeengineering:placeable`, `placeable/sneak`, `placeable_on_plate`, `placeable_on_small_plate`, `placeable_on_coaster` | [placeable_tags.js](../kubejs/data/creative_delight/kubejs/creativeengineering/placeable_tags.js) | DEPLOYED with 1.0.6 (coaster tag 2026-10-18, with 1.0.8) |
| Tooltip lines + removal of DD / Fruits Delight placement lines | [placeable_tooltip.js](../kubejs/assets/creative_delight/kubejs/_qol/placeable_tooltip.js) | DEPLOYED with 1.0.6 (coaster line with 1.0.8); moved into Creative Delight 2026-10-25 (QoL-2, switch `[qol] placeableTooltips`, not installed; creative_delight.md 22, SQ-CDQ-04) |

**Counts (live dump of 2026-10-24, catalog of 2026-10-25):** 507 items. 161 placeable (103 only while sneaking), 34 on a Food Plate, 13 on a Small Food Plate, 392 on a coaster (every raw, cooked, egg and dairy food). 9 patcherMod Display Delight egg dishes join "placeable" after the next dump.
**Open first:** the rules at the top of `tools/placeable_catalog.py` (`INCLUDE`, `EXCLUDE`, `SNEAK_ITEM_CLASSES`).
**Refresh:** `/dumpplaceable` (or `/dumpall`) in game → `python3 tools/placeable_catalog.py` (or `/cegen run placeable_catalog`) →
`/reload` for the tags (restart if the tool added item_dict keys).

## 1. Overview

There are three ways to put food down in this pack:

1. **Display Delight (DD)** has three mechanics:
   - Sneak + right click the top of a solid block places a display block of the food (soups, stews, meals, drinks).
   - Right click an empty **Food Plate** puts a handheld food on it. Sneaking puts down as much of the stack as the plate takes.
   - Right click an empty **Small Food Plate** puts a slice or roll on it.
   - Right click with an empty hand takes the food back.
2. **The food item is itself a block item.** This covers cakes, pies, feasts, pizza, popcorn and nachos boxes, Vinery wine
   and juice bottles, and Fruits Delight jams. The jams and the Mangosteen Cake use `FDBlockItem`, which places only
   while sneaking. Vinery bottles (backport 1.5.5, `DrinkBlockItem`) also place only while sneaking; in the air you drink them.
3. **Edible plants** (carrot, potato, sweet berries, glow berries, onion, corn kernels, blueberry, cranberry,
   mulberry) plant a crop. They are listed but get no line.
4. **Brewin' and Chewin' coaster** (backport, [alcohol.md](alcohol.md) 8.3, 12): right click a coaster to put an item on
   it, up to 4. Since 2026-10-20 it takes only items with a coaster model (`#brewinandchewin:placeable_on_coaster`,
   port setting `[coaster] taggedItemsOnly`): the port's 96 (vanilla foods, Farmer's Delight meals and drinks, every
   Brewin drink) and the 56 Display Delight foods `tools/gen_coaster_models.py` gives one. "Placeable on a coaster"
   goes on exactly those; the Vinery and Farmer's Respite drinks lost it. A sneak right click with an item
   skips the coaster, so a Display Delight food then tries to put its own display block on top (AL-52).

Data flow: `/dumpplaceable` → `kubejs/exported/food/placeable.json` → `tools/placeable_catalog.py` →
`generation_scripts/food/placeable.json` (item_dict keys) → CD `creativeengineering/placeable_tags.js` (tags) → `placeable_tooltip.js` (lines).

## 2. Reference

**How DD decides what is placeable** (from the decompiled 1.2.0 jar):
- On world load, `DisplayEvents.onWorldLoad` walks DD's 191 registered blocks (`DisplayBlocks`, `PlatedBlocks`,
  `SmallPlatedBlocks`). It takes each block's first loot drop and links it in `BlockAssociations` only when that item is in
  the matching tag: `displaydelight:displayable`, `plate_displayable` or `small_plate_displayable`.
  The tag alone is not enough; a DD block must drop the food.
- Config `Disable-Vanila-Food-Placement` (false here) drops the three vanilla stews from the placed map.
- DD's own tooltip (config `Tooltips = true`) reads only the tags, so it would claim "Can be placed" for a tagged food with no block.
  The pack strips it and draws its own lines from the links.
- The jar also ships 25 loot tables for blocks it no longer registers (`hamburger`, `bowl`, `dog_food`, `plated_cake_slice`...).
  The tool reads the registered names from the class files so these don't count.

**Examples** (full list in [placeable_catalog.md](placeable_catalog.md)):

| Item | Lines |
|---|---|
| Beef Stew (FD) | Placeable (while sneaking) |
| Hamburger (FD) | Placeable on plate |
| Cake Slice (FD) | Placeable on small plate |
| Stuffed Potato (FD) | Placeable on plate, Placeable on small plate |
| Cake, Apple Pie, Pizza | Placeable |
| Cherry Wine (Vinery) | Placeable (while sneaking) |
| Apple Jam (Fruits Delight) | Placeable (while sneaking) |
| Carrot, Sweet Berries | none (planted) |

## 3. What the pack changes

- Adds the four tags and the tooltip lines (gray, the `col.norm` role in tooltips.js).
- Removes DD's four placement lines (`item.displaydelight.tooltip.displayable`, `...can_placed_on`,
  `block.displaydelight.food_plate`, `block.displaydelight.small_food_plate`) and Fruits Delight's
  `fruitsdelight.tooltip.place` from every item except line 0, so the Food Plate items keep their names. DD's config is unchanged.
- Gives two items their first item_dict keys: `i.golden_cake`, `i.enchanted_golden_cake`, plus the tag keys `i.t_placeable*`.
  They sit in the generated "Placeable food" block.
- **Coaster: only items with a coaster model** (owner 2026-10-25, [food_families.md](food_families.md) 4.4). Every food of
  the `raw/*`, `cooked/*`, `egg` and `dairy` food types had joined `#creativeengineering:placeable_on_coaster` and the ones
  without a coaster model lay flat on the tray; that step was cut from `tools/placeable_catalog.py` the same day, so the
  tag holds the Brewin' port's and the pack's coaster models only (152). Adding models for the produce stays open.
- **Display Delight blocks for foods DD has none for** (patcherMod `compat/DisplayDelightDishes`, list
  `creativeengineering/displaydelight_dishes.json` in the jar): 9 egg dishes (Vegetable Omelet, Shulker Omelette, Inky
  Cap Scrambled Eggs, Suspicious Bacon And Eggs, Diced Potatoes with Egg and Tomato, Omelet, Cauliflower Kuku, Dandelion
  Greens And Eggs, Shakshouka). Each is a DD `FoodBlock` named `creativeengineering:placed_<dish>`, linked into DD's
  item <-> block map on server world load the way DD links its own, with the food in `#displaydelight:displayable`
  (jar data). Bowl dishes are DD's bowl with a food surface cut from the item sprite; plated ones the sprite flat on
  DD's food plate. `tools/gen_dd_dishes.py` writes the blockstates, models, loot tables, tag and "Placed ..." names
  from the list (output in the patcherMod jar: rebuild after a run). `/dumpplaceable` reads DD's live map, so they show
  in the catalog after the next dump.

## 4. Limits and decisions

- **Placement only, not display holders.** Handcrafted plates/bowls/cups, Supplementaries item shelves and pedestals take
  *any* item, so tagging food for them says nothing. They are noted in the catalog and get no tag.
  Supplementaries cake stacking only applies to cakes, which are already tagged.
- **Plants are excluded.** A carrot "places" a crop, and "Placeable" on every seed-like food would be noise.
- **Excluded block items:** DD's own display-block items, Fantasy Furniture food decorations (not edible), Old Guns
  black powder cakes (not food), Immersive Weathering icicle and moss clump (ordinary blocks that happen to be edible).
- **Tag check at hover time** (`addAdvancedToAll` + `stack.hasTag`). A list fixed at script load would miss tags,
  which only arrive when you join a world. `ItemStack.is(TagKey)` is ambiguous in this Rhino build.
- **Fallback data** (only when there is no `/dumpplaceable` export; the catalog has used the live dump since 2026-10-18): DD links come from the jar's loot tables and the 2026-10-03 live tags in
  `/dumpfood`, and food blocks from dump_food's class regex. That regex misses non-edible food blocks whose class names
  lack Cake/Pie/Feast/..., e.g. a cheese wheel. The dump's wide net lists those under "Review" in the catalog.
  Real ones go into `INCLUDE`.
- Rejected: editing DD's config `Tooltips = false`. Stripping the lines by key keeps config untouched and also covers Fruits Delight.

## 5. File map

- [kubejs/server_scripts/_lib/dump_placeable.js](../kubejs/server_scripts/_lib/dump_placeable.js): `/dumpplaceable`
- [tools/placeable_catalog.py](../tools/placeable_catalog.py): rules, catalog, tag lists, item_dict keys (registered in [tools/generators.json](../tools/generators.json) as `placeable_catalog`)
- [kubejs/generation_scripts/food/placeable.json](../kubejs/generation_scripts/food/placeable.json): generated tag lists
- [kubejs/data/creative_delight/kubejs/creativeengineering/placeable_tags.js](../kubejs/data/creative_delight/kubejs/creativeengineering/placeable_tags.js) (Creative Delight since 2026-10-27): tags
- [kubejs/assets/creative_delight/kubejs/_qol/placeable_tooltip.js](../kubejs/assets/creative_delight/kubejs/_qol/placeable_tooltip.js): lines and removals
- [placeable_catalog.md](placeable_catalog.md): generated catalog
- Tooltip record: [tooltips.md](tooltips.md)

## 6. Offline checks

- `python3 tools/placeable_catalog.py --check`: exits 1 if `placeable.json` is out of date or an item lacks a key.
- `node --check` on the three scripts.
- In the catalog, "Display Delight gaps" should list nothing unexpected. A DD block for an installed but untagged
  food means adding the tag would make that food placeable.

## 7. In-game test suite

Restart the game (new item_dict keys, new client script). Hover items in JEI or the inventory.

- [ ] **PL-01 Load:** `logs/latest.log` has `[placeable] placeable=161, placeable/sneak=103, placeable_on_plate=34, placeable_on_small_plate=13, placeable_on_coaster=392` (2026-10-24 dump; the 9 egg dishes add to `placeable` after the next dump) and no `[placeable]` warning. No `Error occurred while handling event 'item.tooltip'`.
- [ ] **PL-02 DD placed:** hover Beef Stew. → "Placeable (while sneaking)" in gray, and no "Can be placed while sneaking" line. Sneak + right click a block top places it.
- [ ] **PL-03 DD plate:** hover Hamburger. → "Placeable on plate" only; no "Can be plated on:" / "Food Plate" lines. Right click an empty Food Plate plates it.
- [ ] **PL-04 DD small plate:** hover Cake Slice. → "Placeable on small plate". Right click an empty Small Food Plate plates it.
- [ ] **PL-05 Both plates:** hover Stuffed Potato. → "Placeable on plate" and "Placeable on small plate".
- [ ] **PL-06 Own block:** hover Cake, Apple Pie and Pizza. → "Placeable" each, no "(while sneaking)". Cherry Wine → "Placeable (while sneaking)"; it places only on a sneak right click.
- [ ] **PL-07 Jam:** hover Apple Jam. → "Placeable (while sneaking)", and Fruits Delight's "Shift right click to place down" is gone.
- [ ] **PL-08 Not tagged:** hover a Handcrafted plate and the Food Plate item. → No placeable line; the Food Plate still shows its name. Carrot and Sweet Berries show only "Placeable on a coaster" (2026-10-25).
- [ ] **PL-09 Dump:** run `/dumpplaceable`. → Chat reports about 50 + 29 + 11 = 90 Display Delight links. Then `python3 tools/placeable_catalog.py` prints `[dump]` and the catalog's source line says "/dumpplaceable (live)". Review the "Review" section.

## 8. History

- **2026-10-25:** every raw, cooked, egg and dairy food on the coaster (`add_coaster_produce()`), and 9 Display Delight
  egg-dish blocks in patcherMod (`compat/DisplayDelightDishes`, `tools/gen_dd_dishes.py`), for the produce family rule
  ([food_families.md](food_families.md) 4.4). Rerun on the 2026-10-24 dump: 507 items, 392 on a coaster. Tests FF-06 to
  FF-08 in food_families.md; PL-08 updated.

- **2026-10-05:** Built. Catalog from the fallback data (164 items). DD's leftover loot tables found and ignored.
  DD and Fruits Delight placement lines replaced by the pack's lines. PL-01 to PL-09 added.
- **2026-10-11:** ships in pack 1.0.5 (staged, not deployed): `placeable_tooltip.js`, `placeabletags.js`, `placeable.json`
  and `dump_placeable.js` are in the payload. Loaded without KubeJS errors in single player. PL-01 to PL-09 still pending.
- 2026-10-14: pack 1.0.5 was never deployed; everything above that was staged for it ships in pack **1.0.6** (STAGED
  2026-10-14, update from 1.0.4, [plan_1.0.6.md](plan_1.0.6.md)), unchanged.
- **2026-10-18:** fifth tag `#creativeengineering:placeable_on_coaster` ("Placeable on a coaster") for the Brewin'
  backport's coaster: `add_coaster()` in the tool reads the port's coaster model files and adds the Vinery and Farmer's
  Respite drinks. Rerun on today's live `/dumpplaceable`: 269 items, 160 on a coaster. Test AL-52 in alcohol.md.
- 2026-10-19: coaster tag and line STAGED in pack 1.0.8 ([plan_1.0.8.md](plan_1.0.8.md) PL).
- 2026-10-19: status sweep: pack 1.0.6 was DEPLOYED 2026-10-14 and 1.0.8 (coaster line and tag) 2026-10-19 10:20. PL-01 to PL-09 pending.
- **2026-10-20:** coaster tag = modelled items only (alcohol.md 12): `add_coaster()` reads the port's coaster files and
  the pack's generated Display Delight ones; `COASTER_DRINK_MODS` removed. Rerun on the last dump: 152 on a coaster
  (60 Vinery / Farmer's Respite drinks out, 56 DD foods in). Tests AL-91 to AL-93.
- **2026-10-27:** the tag script moved into Creative Delight as `kubejs/data/creative_delight/kubejs/creativeengineering/placeable_tags.js`
  (rows unchanged; creative_delight.md 29.6). Item keys `pasta_with_mutton_chop` and `barbecue_stick` in `placeable.json`
  are now `pasta_with_mutton` and `bbq_stick` (one dict key per id).

## Owner ideas (scratchpad 2026-10-26)

- Dough blocks for every dough (six exist, `baking/baked-goods #27`): `food/placeable #4`.
