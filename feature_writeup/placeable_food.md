# Placeable food

Test prefix **PL-** · Last updated **2026-10-11** · Overall status **STAGED for 1.0.6, not deployed** (catalog built from the
2026-10-03 `/dumpfood` export + the Display Delight jar; `/dumpplaceable` not run yet; the tags and client script loaded
without errors in single player 2026-10-11)

## 0. At a glance

Every food that can be put down in the world gets one, two or three tooltip lines, for any mod's placement
mechanic: "Placeable" (with "(while sneaking)" when sneaking is required), "Placeable on plate" and "Placeable on small plate".
The lines come from four item tags generated from a catalog.

| Part | Where | Status |
|---|---|---|
| `/dumpplaceable` (live Display Delight links + every food-looking block item) | [dump_placeable.js](../kubejs/server_scripts/_lib/dump_placeable.js), also in `/dumpall` | BUILT, not run |
| Catalog + tag lists generator | [tools/placeable_catalog.py](../tools/placeable_catalog.py) → [placeable_catalog.md](placeable_catalog.md), [placeable.json](../kubejs/generation_scripts/food/placeable.json) | BUILT, run on the fallback data |
| Tags `#creativeengineering:placeable`, `placeable/sneak`, `placeable_on_plate`, `placeable_on_small_plate` | [placeabletags.js](../kubejs/server_scripts/tags/placeabletags.js) | BUILT |
| Tooltip lines + removal of DD / Fruits Delight placement lines | [placeable_tooltip.js](../kubejs/client_scripts/placeable_tooltip.js) | BUILT |

**Counts (fallback data):** 164 items in 14 mods. 130 placeable (73 only while sneaking), 29 on a Food Plate, 11 on a Small Food Plate.
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
   while sneaking. Vinery bottles place on a plain right click against a block; in the air you drink them.
3. **Edible plants** (carrot, potato, sweet berries, glow berries, onion, corn kernels, blueberry, cranberry,
   mulberry) plant a crop. They are listed but get no line.

Data flow: `/dumpplaceable` → `kubejs/exported/food/placeable.json` → `tools/placeable_catalog.py` →
`generation_scripts/food/placeable.json` (item_dict keys) → `placeabletags.js` (tags) → `placeable_tooltip.js` (lines).

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
| Cake, Apple Pie, Pizza, Cherry Wine | Placeable |
| Apple Jam (Fruits Delight) | Placeable (while sneaking) |
| Carrot, Sweet Berries | none (planted) |

## 3. What the pack changes

- Adds the four tags and the tooltip lines (gray, the `col.norm` role in tooltips.js).
- Removes DD's four placement lines (`item.displaydelight.tooltip.displayable`, `...can_placed_on`,
  `block.displaydelight.food_plate`, `block.displaydelight.small_food_plate`) and Fruits Delight's
  `fruitsdelight.tooltip.place` from every item except line 0, so the Food Plate items keep their names. DD's config is unchanged.
- Gives two items their first item_dict keys: `i.golden_cake`, `i.enchanted_golden_cake`, plus the tag keys `i.t_placeable*`.
  They sit in the generated "Placeable food" block.

## 4. Limits and decisions

- **Placement only, not display holders.** Handcrafted plates/bowls/cups, Supplementaries item shelves and pedestals take
  *any* item, so tagging food for them says nothing. They are noted in the catalog and get no tag.
  Supplementaries cake stacking only applies to cakes, which are already tagged.
- **Plants are excluded.** A carrot "places" a crop, and "Placeable" on every seed-like food would be noise.
- **Excluded block items:** DD's own display-block items, Fantasy Furniture food decorations (not edible), Old Guns
  black powder cakes (not food), Immersive Weathering icicle and moss clump (ordinary blocks that happen to be edible).
- **Tag check at hover time** (`addAdvancedToAll` + `stack.hasTag`). A list fixed at script load would miss tags,
  which only arrive when you join a world. `ItemStack.is(TagKey)` is ambiguous in this Rhino build.
- **Fallback data** (until `/dumpplaceable` runs): DD links come from the jar's loot tables and the 2026-10-03 live tags in
  `/dumpfood`, and food blocks from dump_food's class regex. That regex misses non-edible food blocks whose class names
  lack Cake/Pie/Feast/..., e.g. a cheese wheel. The dump's wide net lists those under "Review" in the catalog.
  Real ones go into `INCLUDE`.
- Rejected: editing DD's config `Tooltips = false`. Stripping the lines by key keeps config untouched and also covers Fruits Delight.

## 5. File map

- [kubejs/server_scripts/_lib/dump_placeable.js](../kubejs/server_scripts/_lib/dump_placeable.js): `/dumpplaceable`
- [tools/placeable_catalog.py](../tools/placeable_catalog.py): rules, catalog, tag lists, item_dict keys (registered in [tools/generators.json](../tools/generators.json) as `placeable_catalog`)
- [kubejs/generation_scripts/food/placeable.json](../kubejs/generation_scripts/food/placeable.json): generated tag lists
- [kubejs/server_scripts/tags/placeabletags.js](../kubejs/server_scripts/tags/placeabletags.js): tags
- [kubejs/client_scripts/placeable_tooltip.js](../kubejs/client_scripts/placeable_tooltip.js): lines and removals
- [placeable_catalog.md](placeable_catalog.md): generated catalog
- Tooltip record: [tooltips.md](tooltips.md)

## 6. Offline checks

- `python3 tools/placeable_catalog.py --check`: exits 1 if `placeable.json` is out of date or an item lacks a key.
- `node --check` on the three scripts.
- In the catalog, "Display Delight gaps" should list nothing unexpected. A DD block for an installed but untagged
  food means adding the tag would make that food placeable.

## 7. In-game test suite

Restart the game (new item_dict keys, new client script). Hover items in JEI or the inventory.

- [ ] **PL-01 Load:** `logs/latest.log` has `[placeable] placeable=130, placeable/sneak=73, placeable_on_plate=29, placeable_on_small_plate=11` and no `[placeable]` warning. No `Error occurred while handling event 'item.tooltip'`.
- [ ] **PL-02 DD placed:** hover Beef Stew. → "Placeable (while sneaking)" in gray, and no "Can be placed while sneaking" line. Sneak + right click a block top places it.
- [ ] **PL-03 DD plate:** hover Hamburger. → "Placeable on plate" only; no "Can be plated on:" / "Food Plate" lines. Right click an empty Food Plate plates it.
- [ ] **PL-04 DD small plate:** hover Cake Slice. → "Placeable on small plate". Right click an empty Small Food Plate plates it.
- [ ] **PL-05 Both plates:** hover Stuffed Potato. → "Placeable on plate" and "Placeable on small plate".
- [ ] **PL-06 Own block:** hover Cake, Apple Pie, Pizza and Cherry Wine. → "Placeable" each, no "(while sneaking)".
- [ ] **PL-07 Jam:** hover Apple Jam. → "Placeable (while sneaking)", and Fruits Delight's "Shift right click to place down" is gone.
- [ ] **PL-08 Not tagged:** hover Carrot, Sweet Berries, a Handcrafted plate and the Food Plate item. → No placeable line; the Food Plate still shows its name.
- [ ] **PL-09 Dump:** run `/dumpplaceable`. → Chat reports about 50 + 29 + 11 = 90 Display Delight links. Then `python3 tools/placeable_catalog.py` prints `[dump]` and the catalog's source line says "/dumpplaceable (live)". Review the "Review" section.

## 8. History

- **2026-10-05:** Built. Catalog from the fallback data (164 items). DD's leftover loot tables found and ignored.
  DD and Fruits Delight placement lines replaced by the pack's lines. PL-01 to PL-09 added.
- **2026-10-11:** ships in pack 1.0.5 (staged, not deployed): `placeable_tooltip.js`, `placeabletags.js`, `placeable.json`
  and `dump_placeable.js` are in the payload. Loaded without KubeJS errors in single player. PL-01 to PL-09 still pending.
- 2026-10-14: pack 1.0.5 was never deployed; everything above that was staged for it ships in pack **1.0.6** (STAGED
  2026-10-14, update from 1.0.4, [plan_1.0.6.md](plan_1.0.6.md)), unchanged.
