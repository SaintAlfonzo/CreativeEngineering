# Brief: recipes / progression

Written 2026-10-21 for the open `recipes/progression` rows of [TODO.md](../todo/TODO.md). Reference writeup:
[recipe_progression.md](../recipe_progression.md) (test prefix **RP-**, last used RP-08; new tests below start at
RP-09, take the next free number when you add them). Layout: [README.md](README.md).

**Verified against jars** (downloaded and read 2026-10-21): Quark 3.4-418 (`building/crafting/furnaces/*`), Nether's
Delight 3.1 (blackstone furnaces), Farmer's Delight 1.2.4 and Create 0.5.1.i (cake recipes), Explorer's Compass 1.3.0
and Nature's Compass (recipes), Immersive Engineering 9.2.4 (sawmill recipes, `forge:dusts/wood`). Pack configs read:
`config/quark-common.toml` and `configs_edited/root_config/quark-common.toml` (`"Variant Furnaces" = true`, line 127).
Anything else is marked unverified.

## Scope and order

| Row | Change | Work | Group |
|---|---|---|---|
| progression #1 | Items with no recipe after removals | KubeJS | A: mostly resolved, two items left |
| progression #3 | `kubejs/recipes/` never loads | data (delete) | A |
| progression #2 | Recipe scan is slow | KubeJS | B |

Suggested order: **A** first (one `/reload`, mostly deletions and a JEI check), then **B** (a rewrite of one
diagnostic file; measure before and after). #1's two real gaps are owned elsewhere: the dispenser by
`recipes/engineering #1` ([recipes_engineering.md](recipes_engineering.md)) and the Explorer's Compass by
`recipes/magic #1`; this row closes when both are done.

---

## progression #1: Items with no recipe after removals

Status Planned · Work KubeJS · TODO `recipes/progression #1` (umbrella of `recipes/engineering #1`,
`recipes/cooking #1`, `recipes/cooking #2`, `recipes/magic #1`)

### Goal
Every item the pack's removals leave without a recipe gets one, or goes on the removed list.

### Now
Rechecked item by item:

| Item | Pack removal | Recipes that still make it | State |
|---|---|---|---|
| `minecraft:blast_furnace` | `process.js` line 11, `minecraft:blast_furnace` only | `quark:building/crafting/furnaces/deepslate_blast_furnace` and `blackstone_blast_furnace`: `III / IXI / ###`, 5 `minecraft:iron_ingot`, a `quark:deepslate_furnace` / `quark:blackstone_furnace`, 3 `minecraft:smooth_stone`; condition `quark:flag variant_furnaces` (on) **(jar, config)** | has a recipe, the one the comment ("require deepslate or blackstone recipe") asks for |
| `minecraft:smoker` | `cooking.js` line 13, `minecraft:smoker` only | `quark:building/crafting/furnaces/deepslate_smoker` and `blackstone_smoker`: 4 `#minecraft:logs` around the Quark furnace **(jar)** | has a recipe; the old `global.CE` copy (`cooking.js` line 575) also removed the deepslate one ("minus blackstone"), the live code doesn't |
| `minecraft:cake` | `cooking.js` line 357, `minecraft:cake` | `farmersdelight:cake_from_milk_bottle` (3 `#forge:milk`, sugar, egg, sugar, 3 wheat), `create:crafting/curiosities/cake` (milk bucket, 2 sugar, egg, `#forge:dough`) **(jars)**; `neapolitan:cake` (per the
recipes/cooking recheck, [recipes_cooking.md](../recipes_cooking.md) F2); the oven's `creativeengineering:baking/cake` (`baking.js` line 96, milk ×3 + cake base, cake pan) | has four recipes |
| `minecraft:dispenser` | `redstone.js` line 59 ("must be crafted from dropper") | none found in the scripts, `kubejs/data` or the jars at hand (~400 jars not scanned) | **gap**, built in `recipes/engineering #1` |
| `explorerscompass:explorerscompass` | `magic/progression.js` line 10 removes `explorerscompass:explorers_compass` (cobweb / cracked stone bricks / compass, **jar**) | none | **gap**, `recipes/magic #1` (loot only, or a late recipe next to Nature's Compass, which `progression.js` line 11 moves to terrasteel) |

- None of the five is in `global.removedItems`
  ([removed_items.js](../../kubejs/startup_scripts/_lib/removed_items.js)); with the three resolved, only the dispenser
  and the compass would belong there, and only if their row decides "no recipe".
- Nether's Delight's `nethersdelight:blackstone_blast_furnace` (`###/#b#/sss`, iron ingots, its blackstone furnace,
  polished blackstone, **jar**) is a separate working blast furnace block; it does not satisfy the Little Logistics
  recipes, which ask for `minecraft:blast_furnace` (`transport.js` lines 92-100).

### Build
1. JEI check first (RP-09 below): confirm the Quark blast furnace and smoker recipes and the cake recipes show.
   If they do, nothing to build for those three.
2. Dispenser: build it in `recipes/engineering #1` (sketch there).
3. Explorer's Compass (`i.explorers_compass`, dict line 636): `recipes/magic #1` decides. If "loot only", don't put it
   in `global.removedItems` (that hides it and strips its tags, wrong for an item players can find); add a tooltip
   saying where it comes from instead. Recommend a late recipe (see Decisions).
4. Fix the stale comments: `process.js` line 10 ("require deepslate or blackstone recipe") becomes "Quark's deepslate /
   blackstone blast furnace recipes stay (quark:building/crafting/furnaces/*)"; `cooking.js` line 12 likewise for the
   smoker.

### Ids and data
- Recipe ids above, all verified in jars. Quark furnace items: `i.deepslate_furnace` (dict line 3009),
  `i.blackstone_furnace` (2898). Nether's Delight furnaces are not in the dict (`nethersdelight:blackstone_furnace`,
  `nethersdelight:blackstone_blast_furnace`); add them only if a recipe needs them, as `i.nd_blackstone_furnace` /
  `i.nd_blackstone_blast_furnace` (no `nd_` keys exist yet; the dict uses short mod prefixes such as `ll_`, `q_`).

### Decisions
- **Keep Quark's blast furnace and smoker recipes as the pack recipes?** Recommend yes: they already require deepslate
  or blackstone. Optional polish: swap their iron ingots for `i.any_plate_iron` with
  `R.replaceInput({ id: 'quark:building/crafting/furnaces/deepslate_blast_furnace' }, i.ingot_iron, i.any_plate_iron, e)`
  (and the blackstone one), matching Little Logistics' plate rule. Owner: `recipes/engineering #1`.
- **Smoker: deepslate and blackstone, or blackstone only** (the old `global.CE` intent)? Owner `recipes/cooking #1`
  ([brief](recipes_cooking.md));
  recommend keeping both (the smoker gates bread blocks, sourdough and leather, early-game items).
- **Explorer's Compass:** recommend a late recipe mirroring Nature's Compass (compass + terrasteel + cracked stone
  bricks + cobweb), since structure finding is the reward for terrasteel too; owner `recipes/magic #1`.

### Gotchas
- `R.remove({ id })` removes exactly one recipe id; mod variants with other ids (Quark's here) survive. When a future
  row really wants "no recipe", remove by output (`R.remove({ output: i.smoker }, e)`), then add the pack's own.
- The Quark recipes depend on the `variant_furnaces` flag; turning Quark's "Variant Furnaces" off would silently bring
  the gap back.

### Tests
- **RP-09 Furnace variants:** JEI `minecraft:blast_furnace` and `minecraft:smoker` → the two Quark recipes each; craft a
  blast furnace from a deepslate furnace.
- **RP-10 Cake:** JEI `minecraft:cake` → FD milk bottle, Neapolitan, Create dough, oven baking.
- RP-05 stays as the umbrella check; RE-08 (dispenser) and RA-07 (compass) close the last two.

---

## progression #2: Recipe scan is slow

Status Planned · Work KubeJS · TODO `recipes/progression #2`

### Goal
The removed-item scan costs one pass over the recipes instead of one per removed item, and says how long it took.

### Now
- [_lib/recipe_scan.js](../../kubejs/server_scripts/_lib/recipe_scan.js) lines 5-45: `ServerEvents.recipes` at
  `// priority: -10000`, guarded by `global.hasScannedRemovedItemRecipes` (set in `removed_items.js` line 6, so once per
  game session, not per `/reload`). Lines 21-42: `global.removedItems.forEach` → `event.forEachRecipe({}, ...)` →
  `recipe.json.toString()` → `indexOf(itemId)`. That is 309 passes in 1.0.9 (312 in the working tree), each serialising
  every recipe again.
- Measured cost: the pasted log in `startup_scripts/todo/recipes_to_remove.txt` runs from 20:33:15 (first hit) to
  20:33:39 ("Scan complete. 178 total recipe references found across 297 removed items"), so at least 24 s of the
  first load, when the list was 297 long.
- `indexOf` matches substrings: an id that is a prefix of another id (a removed `mod:x` inside `mod:x_block`) counts as a
  hit. The count in the log is therefore an upper bound.
- Lines 50-54 (tag stripping in `ServerEvents.tags('item')`) are separate and cheap; keep them.
- `global.hasRemovedTags` (`removed_items.js` line 7) is set and never read (`tooling/conventions #4`).
- Related: `tooling/generators-and-tests #3` ("removed items keep their recipes; the scan only lists them", by design).

### Build
1. Rewrite lines 19-44 as one pass: build a lookup of removed ids, scan each recipe's JSON once for quoted
   `namespace:path` strings, and collect hits per id. Sketch:
   ```js
   var t0 = Date.now()
   var hits = {}
   global.removedItems.forEach(id => { hits[String(id)] = [] })
   var ID_RE = /"([a-z0-9_.-]+:[a-z0-9_.\/-]+)"/g
   event.forEachRecipe({}, recipe => {
     var s
     try { s = String(recipe.json) } catch (err) { return }
     var seen = {}, m
     ID_RE.lastIndex = 0
     while ((m = ID_RE.exec(s)) !== null) {
       var id = m[1]
       if (hits[id] && !seen[id]) { seen[id] = true; hits[id].push(getRecipeId(recipe)) }
     }
   })
   // then log exactly as now: '[jeiRemove] <id> -> N recipe(s):' + one line per id, and the summary line,
   // adding ' in ' + (Date.now() - t0) + ' ms'
   ```
2. Optionally also write the hits to `kubejs/exported/removed_refs.json` with `writeExportJson` from
   [_lib/export_io.js](../../kubejs/server_scripts/_lib/export_io.js) (it creates the folder; wrap the map in an object),
   so `startup_scripts/todo/recipes_to_remove.txt` can be regenerated instead of pasted.
3. Leave the once-per-session guard. Delete `global.hasRemovedTags` with `tooling/conventions #4` (not required here).
4. Update the writeup: 2.1 consumer row, F2 (fixed, with the measured times), RP-01.

### Ids and data
No recipe ids. Log tags `[jeiRemove]`, `[recipe_scan]` stay, so existing greps keep working.

### Decisions
- **Run on demand (a `/scanremoved` command) instead of on load?** Recommend no. A command would read the live
  `RecipeManager` (as `dump_recipes.js` does), where recipes are Java objects with no JSON to search; only
  `getIngredients()` / `getResultItem()` are generic, which misses secondaries, fluids-with-NBT and Create's extra
  results. The single pass should bring the cost down to well under a second, which removes the reason to move it.

### Gotchas
- Exact matching finds fewer hits than `indexOf` did (no prefix matches). Compare the new summary count with the old
  one once and note the difference in the writeup, so a lower number isn't read as a regression.
- Rhino: use a fresh `exec` loop with `lastIndex = 0` per recipe (a shared global regex keeps its position);
  `Date.now()` works.
- The handler only sees what `forEachRecipe` sees at priority -10000 inside the recipes event; that is unchanged by
  this row (see `dump_recipes.js` lines 7-16 for why the event is not a full snapshot). Don't move it.

### Tests
- **RP-11 Scan speed:** first world load → `[recipe_scan] Scan complete. N ... in T ms`; note N and T next to the old
  24 s; second `/reload` → "Skipping". Supersedes the timing part of RP-01.
- **RP-12 Scan hits:** `vintageimprovements:aluminum_sheet` is still listed with `vintageimprovements:pressing/aluminum_ingot`
  (until `recipes/metal #1` removes that recipe), proving exact matching still finds hits.

---

## progression #3: `kubejs/recipes/` never loads

Status Planned · Work data · TODO `recipes/progression #3`

### Goal
No dead recipe JSONs in the tree: each is ported to a load path or deleted.

### Now
- `kubejs/recipes/` is not a KubeJS load path (datapack files load from `kubejs/data/<ns>/recipes/`).
- `kubejs/recipes/food/bonemeal_broth.json`: FD cooking, bone meal + glow berries / mushrooms / hanging roots / glow
  lichen → bone broth. Superseded: [cooking.js](../../kubejs/server_scripts/cooking/cooking.js) lines 18-20 remove
  `farmersdelight:cooking/bone_broth` and add `R.fdCooking([[i.bone, i.bonemeal], [...]], Item.of(i.bone_broth, 2), ...)`
  plus a shapeless version, which already accept bone meal.
- `kubejs/recipes/recycle/sawmill_recycle_{slab,stairs,door}.json`: IE sawmill, `#minecraft:wooden_slabs` → 2 sticks,
  `#minecraft:wooden_stairs` → 3 sticks, `#minecraft:wooden_doors` → 4 sticks, each + `#forge:dusts/wood`.
- The pack's sawmill makes a door and a stair from **one** plank each (patcherMod
  `data/creativeengineering/recipes/sawmill/oak_planks-oak_door.json`, `oak_planks-oak_stairs.json`: 1 plank → 1 + wood
  dust), and the Create saw does the same (`wood/wood_types.js` lines 23-33). Ported as written, the recycle files would
  turn 1 plank into 4 sticks + 2 sawdust (door) or 3 sticks + 2 sawdust (stairs), against 2 sticks by crafting.
- The pack deliberately removed IE's own recycling (`immersiveengineering:sawmill/<wood>_door` and `_stairs` → 1
  plank, `wood_types.js` lines 40-44, verified in the IE jar), so recycling wood shapes is not part of the design today.
- Live sawmill JSONs from KubeJS sit in `kubejs/data/creativeengineering/recipes/sawmill/` (2 files: layered obsidian,
  rose quartz).

### Build
Recommended (delete):
1. Delete `kubejs/recipes/food/bonemeal_broth.json` and the three `kubejs/recipes/recycle/*.json`, then the empty
   `kubejs/recipes/` folder.
2. Writeup: 2.3 rows say "deleted 20xx-xx-xx", F3 fixed, RP-06 struck or reworded ("oak slab in the IE sawmill → IE's
   own planks → slab recipe only").

If the decision is "port" instead:
1. Write them as `kubejs/data/creativeengineering/recipes/sawmill/recycle_{door,stairs,slab}.json` (ids
   `creativeengineering:sawmill/recycle_*`) with the counts matched to the pack's sawmill cost: door → 2 sticks, stairs
   → 2 sticks, slab → 1 stick, each + `{"output":{"tag":"forge:dusts/wood"},"stripping":false}`. Keep `energy` 800.
2. Or wait for `engineering/sawmill-and-components #2` (an NCH helper writing raw sawmill JSON from
   `ServerEvents.highPriorityData`), which removes the reason these are hand-written files; `R.ieSawmill`
   ([neoCraftHelper.js](../../kubejs/startup_scripts/func/neoCraftHelper.js) lines 1813-1849) refuses secondaries today.

### Ids and data
`#minecraft:wooden_slabs`, `#minecraft:wooden_stairs`, `#minecraft:wooden_doors`, `#forge:dusts/wood` (IE ships the
forge tag, **jar**). No item dict keys are needed for a tag-only JSON; a helper version would use `i.stick` (dict
line 1395) and `i.sawdust` (`immersiveengineering:dust_wood`, line 1621; `i.saw_dust` on line 1622 is a duplicate key
for `tooling/conventions #2`).

### Decisions
- **Port or delete?** Recommend delete: the pack removed IE's wood-shape recycling on purpose, and the files as written
  multiply sticks. Port only if recycling looted doors and stairs becomes a goal, at the corrected counts.

### Gotchas
- A tag input such as `#minecraft:wooden_doors` also matches modded doors whose plank cost differs (Dramatic Doors'
  tall doors, Quark's); if porting, check JEI for an exploit with the tall doors.
- IE's sawmill takes the first matching recipe; no other sawmill recipe takes a slab, stair or door today (checked the
  211 patcherMod files and IE's own), so a port would not clash.

### Tests
- **RP-13 Dead folder:** `kubejs/recipes/` is gone; `/reload` logs nothing about it; JEI bone broth shows the two
  `cooking.js` recipes. (If ported: an oak door in the IE sawmill → 2 sticks + sawdust.)

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| progression #4 | Recipe tests | Complete | Status fits the "live since 1.0.4" rule, but every recipe writeup says its tests were never run, and this pass found live bugs in the area (`recipes/metal` brief: VI pressing makes removed sheets; Create Deco catwalks uncraftable). Consider Testing until the RP/RE/RM suites get a pass. |

## Writeup corrections made

- [recipe_progression.md](../recipe_progression.md) F1: added the 2026-10-21 recheck (blast furnace and smoker still
  made by Quark's variant-furnace recipes, cake by FD / Create / the oven; only dispenser and Explorer's Compass left).
- [recipe_progression.md](../recipe_progression.md) 2.1: "309 ids" → 309 in 1.0.9, 312 in the working tree (three
  Creative Labels donors, 2026-10-20, unshipped).
- [README.md](../README.md) recipe progression row: F1 list narrowed to the dispenser and Explorer's Compass.
- [recipes_cooking.md](../recipes_cooking.md) at-a-glance row and F1: smoker still made by Quark's deepslate /
  blackstone smoker (the cooking brief's agent added the F2 cake recheck; a duplicate sentence of mine there was taken
  out again).
- [recipes_engineering.md](../recipes_engineering.md) F1, 2.1 table and at-a-glance row: see the
  [recipes/engineering brief](recipes_engineering.md).
