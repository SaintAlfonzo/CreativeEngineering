# Recipe progression: the hub for the recipe scripts, removed items and dead recipes

Backlog row 20. The recipe scripts are written up per folder; this file gives the whole picture, the findings that cut
across folders, and the two things no folder owns: the removed-item list and recipes that don't (or no longer) load.

| Folder | Writeup | Tests |
|---|---|---|
| `server_scripts/engineering/` | [recipes_engineering.md](recipes_engineering.md) (machine gating, mechanisms, redstone, rails, resource stones, dusts) and [engineering.md](engineering.md) (components, coils, sawmill, syringe) | `RE-`, `EN-` |
| `server_scripts/metal/` | [recipes_metal.md](recipes_metal.md) | `RM-` |
| `server_scripts/wood/` | [recipes_wood.md](recipes_wood.md) | `RW-` |
| `server_scripts/cooking/` | [recipes_cooking.md](recipes_cooking.md), [oven.md](oven.md) (baking), [cans.md](cans.md) / [can_parity.md](can_parity.md) (canning) | `RC-`, `OV-`, `CN-`, `CP-` |
| `server_scripts/magic/` | [recipes_magic.md](recipes_magic.md), [armor_effects.md](armor_effects.md), [potions.md](potions.md), [archery.md](archery.md) | `RA-`, `AE-`, `PO-`, `AR-` |
| Removed items, dead recipes | this file | `RP-` |

Other recipe folders have their own writeups: `equipment/` ([gear_tuning.md](gear_tuning.md), [archery.md](archery.md),
[shields.md](shields.md), [camera_bags.md](camera_bags.md)), `loot/` ([loot.md](loot.md)), `worldgen/`. The live part of
`other/` (`misc_mods.js` lines 1-57, `flowers.js`, `harvest.js`) has no writeup of its own.

## 0. At a glance

| Part | Status |
|---|---|
| Five per-folder writeups (RE-, RM-, RW-, RC-, RA-) | written 2026-10-18 |
| Removed items: 309 ids in `global.removedItems`; hidden in JEI, red tooltip, all item tags stripped, recipes referencing them listed once per session | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3) |
| `dead_recipe_removal.js`: 21 Vintage Improvements recipes for metals the pack doesn't use (vanadium, shadow steel, refined radiance, ...) | DEPLOYED |
| Dead code: `kubejs/recipes/` (4 JSONs that never load), three commented-out `global.CE` blocks, `kubejs/archive/` | left in place (TODO 6) |

- **Pending:** nothing built and unshipped. RP-01 to RP-08 have never been run. Findings F1 to F6 (section 4); F1 needs
  your call (it collects the per-folder missing-recipe calls).
- **Files to open:** [_lib/removed_items.js](../kubejs/startup_scripts/_lib/removed_items.js),
  [_lib/recipe_scan.js](../kubejs/server_scripts/_lib/recipe_scan.js),
  [other/dead_recipe_removal.js](../kubejs/server_scripts/other/dead_recipe_removal.js).

## 1. Overview

### 1.1 The progression the recipes build

Read across the folders, the recipes set this order (each step names the writeup that holds the detail):

1. **Stone and wood:** 3-heat stone generators, bark and sawdust, Create andesite parts (recipes_engineering 2.5,
   recipes_wood).
2. **Early Create:** gold deployer hand, mechanical press on stiff springs and an iron block, saw and drill on IE parts
   (recipes_engineering 2.1).
3. **Ore processing:** crushing wheels 1×, IE crusher 1.33×, arc furnace 1.5× (recipes_metal 2.2).
4. **Nether:** alloy kiln from blackstone and nether bricks, cast iron, IE blast brick, sulfur and TNT (recipes_engineering
   2.1, 2.6).
5. **Steel:** IE blast furnace or the PNC pressure chamber; Engineer's Workbench needs steel, and with it every blueprint
   route (recipes_engineering 2.1).
6. **Mechanisms:** precision on brass, redstone mechanism (gold, electron tube, randomizer), vibration mechanism on an
   echo shard (recipes_engineering 2.2), which needs sculk, which grows on liquid XP (recipes_magic 2.1).
7. **Magic:** terrasteel for Nature's Compass and Dark Fire, a Botania eye for ender chests, XP machines on experience
   blocks (recipes_magic, [nether_portal.md](nether_portal.md)).

### 1.2 Removing an item

KubeJS can't unregister another mod's item. The pack removes items in four layers: recipes producing them are removed
by hand in the feature scripts; the id goes in `global.removedItems`; at startup and tag load the list hides it in JEI,
gives it a red "REMOVED ITEM: DO NOT USE" tooltip and strips every item tag from it (so it can't satisfy a tag
ingredient); and `recipe_scan.js` lists any recipe still mentioning it.

## 2. Reference

### 2.1 Removed items (`_lib/removed_items.js`, priority -10000)

309 ids, grouped by origin: Gates logic and wireless parts; every duplicate sheet, rod, wire and spring from Create
Crafts & Additions, Create Deco and Vintage Improvements for metals the pack standardises on IE parts; parts for metals
the pack doesn't use (tin, osmium, signalum, slimesteel, shadow steel, vanadium, ...); Dave's Building Mod steel ingot;
Nock Enough Arrows' quiver and torch arrow; Quark's dragon scale; IE's potion bucket (no source left,
[potion_buckets.md](potion_buckets.md)).

| Consumer | What it does |
|---|---|
| `client_scripts/jeichanges.js` | `e.hide(id)` in JEI; red bold tooltip |
| `server_scripts/_lib/recipe_scan.js` (tags event) | `removeAllTagsFrom(id)` on item tags |
| `server_scripts/_lib/recipe_scan.js` (recipes event, once per session) | for each id, walks every recipe's JSON and logs `[jeiRemove] <id> -> N recipe(s)` and the ids |

### 2.2 Worklists (`startup_scripts/todo/`)

| File | What |
|---|---|
| `recipes_to_remove.txt` | a pasted scan log (undated): 83 removed items still referenced, 178 references across 297 items (the list has grown to 309 since) |
| `remove_list.txt` | ids still to add; one left (`vintageimprovements:rose_gold_rod`, TODO 6) |
| `create_ie_dict_additions.txt`, `not_in_dict.txt` | dictionary candidates, not recipe work (TODO 5) |

### 2.3 Dead recipes

| What | Where | State |
|---|---|---|
| Vintage Improvements recipes for unused metals (vanadium storage, plate, rolling, pressing; shadow steel, netherite, refined radiance, andesite, cast iron and uranium coiling; sulfur trioxide pressurizing) | [dead_recipe_removal.js](../kubejs/server_scripts/other/dead_recipe_removal.js) | removed (21 ids) |
| `kubejs/recipes/food/bonemeal_broth.json` | not a load path | dead; superseded by `cooking.js`'s bone broth pot recipe |
| `kubejs/recipes/recycle/sawmill_recycle_{slab,stairs,door}.json` (IE sawmill: slabs → 2 sticks + sawdust, ...) | not a load path | dead; never ported (F3) |
| Old `global.CE` blocks | `misc_mods.js` 60-310, `resource_blocks.js` 257-487, `cooking.js` 340-764 | commented out |
| `kubejs/archive/` | 9 old scripts and `worldgen/` | not loaded |

## 3. What the pack changes

Nothing beyond the files above; every removal and replacement is in the folder scripts.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. Five vanilla or mod items have no recipe after the pack's removals. Your call, one by one.** Blast furnace
  ([recipes_engineering.md](recipes_engineering.md) F1; Little Logistics' locomotive and tug need it), dispenser (same,
  F2), smoker ([recipes_cooking.md](recipes_cooking.md) F1), Explorer's Compass ([recipes_magic.md](recipes_magic.md)
  F1), and possibly the cake (recipes_cooking F2). None is in `removed_items.js` (TODO 6 "Bookkeeping"), so they show in
  JEI with no recipe. Either give each a pack recipe, or add it to the removed list.
- **F2. The scan is slow by construction.** `recipe_scan.js` walks every recipe once per removed item (309 passes,
  serialising each recipe's JSON each time) on the first recipe load of a session. Not measured; one pass that checks
  each recipe against a set would cost a 309th. It only logs, so it could also run on demand (a `/dump` command) instead
  ([pack_tooling.md](pack_tooling.md) F10).
- **F3. The sawmill recycle recipes never loaded.** `kubejs/recipes/recycle/` (slabs, stairs, doors back to sticks) was
  meant for IE's sawmill. They have sawdust secondaries, which `R.ieSawmill` can't do (the KubeJS IE schema bug), so
  porting means moving them to `kubejs/data/creativeengineering/recipes/sawmill/`, where the other sawmill JSONs load;
  or delete them (TODO 6).
- **F4. `global.hasRemovedTags` is set and never read.**
- **F5. Define-once across the recipe folders:** local redefinitions of dict keys in `end.js`, `dust.js`, `redstone.js`,
  `cooking.js`, `cultivated_meat.js`, `ash.js`; duplicate keys inside `item_dict.js` (`mulch_brown` ×4, `xp_block` ×3,
  `any_sandpaper` ×3, ...). Each writeup lists its own; a one-off duplicate-key scan of `item_dict.js` would find the
  rest.
- **F6. Raw recipe code outside NCH:** `mechanisms.js` (vibrating step), `ash.js` (soot scraping), `storage.js` (Ender
  Storage), plus the IE sawmill JSONs in `kubejs/data` that can't go through KubeJS ([engineering.md](engineering.md) F3).

### Known limits

- **A removed item still exists:** it can come from loot, old worlds or `/give`; the tooltip is the only warning.
- **Tag stripping is item tags only;** block tags of removed blocks stay.

### Rejected options

- **Removing items from the registry:** not possible from KubeJS without a mod; hiding plus tag stripping is enough for
  a pack that ships new worlds.

## 5. File map

- [startup_scripts/_lib/removed_items.js](../kubejs/startup_scripts/_lib/removed_items.js),
  [server_scripts/_lib/recipe_scan.js](../kubejs/server_scripts/_lib/recipe_scan.js),
  [client_scripts/jeichanges.js](../kubejs/client_scripts/jeichanges.js).
- [server_scripts/other/dead_recipe_removal.js](../kubejs/server_scripts/other/dead_recipe_removal.js),
  [server_scripts/other/misc_mods.js](../kubejs/server_scripts/other/misc_mods.js).
- [startup_scripts/todo/](../kubejs/startup_scripts/todo/) worklists; `kubejs/recipes/`; `kubejs/archive/`.

## 6. Offline checks

None. The scan runs in game; its output is in `logs/kubejs/server.log` under `[jeiRemove]`.

## 7. In-game test suite

The per-folder suites carry the recipe tests: RE-01 to RE-20, RM-01 to RM-14, RW-01 to RW-10, RC-01 to RC-14, RA-01 to
RA-08.

- [ ] **RP-01 Scan:** first `/reload` of a session → `[recipe_scan] Scan complete. N total recipe references found
  across 309 removed items`; second `/reload` → "Skipping". Note N and the time the reload takes (F2).
- [ ] **RP-02 Hidden:** JEI search for `vanadium` → nothing; `/give` a Vintage Improvements vanadium ingot → red "REMOVED
  ITEM" tooltip.
- [ ] **RP-03 Tags stripped:** `/kubejs hand` on a removed sheet (e.g. a Create Crafts & Additions zinc sheet) → no
  tags; it doesn't go into a recipe asking for `#forge:plates/zinc`.
- [ ] **RP-04 Dead removal:** JEI vanadium and shadow steel coiling → no recipes.
- [ ] **RP-05 Missing recipes (F1):** JEI blast furnace, dispenser, smoker, Explorer's Compass, cake → note which have a
  recipe (same checks as RE-03, RE-08, RC-01, RC-02, RA-07).
- [ ] **RP-06 Recycle JSONs (F3):** an IE sawmill with oak slabs → note the output (expect no recipe, or IE's own).
- [ ] **RP-07 Progression walk:** in a fresh survival world, check each step in 1.1 is reachable in order (no step needs
  an item from a later one).
- [ ] **RP-08 Steel before blueprints:** try to craft an Engineer's Workbench before any steel → impossible; after one
  pressure chamber or blast furnace run → possible.

## 8. History

- By 2026-09-27 (initial git commit): removed-item list, scan and tag stripping, `dead_recipe_removal.js`; shipped with
  1.0.3. `kubejs/recipes/` and the `global.CE` blocks were already dead.
- 2026-10-18: the five per-folder writeups and this hub; findings F1 to F6; tests RP-01 to RP-08. No code changed.
