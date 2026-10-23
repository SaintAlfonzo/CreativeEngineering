# recipes / wood: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# recipes`, Sub `wood`. Feature writeup:
[recipes_wood.md](../recipes_wood.md) (test prefix `RW-`, last id RW-10). Code: `kubejs/server_scripts/wood/`
(`bark.js`, `wood_types.js`, `sawdust.js`, `ash.js`); the IE sawmill side is [engineering.md](../engineering.md) 2.1
(211 JSONs in patcherMod `data/creativeengineering/recipes/sawmill/`).

Jars read for this brief (all in `scratchpad/jars`): Farmer's Delight 1.2.4, Quark Delight 1.0.0, Abnormals Delight
4.1.2, Architect's Palette 1.3.5, Create 0.5.1.i, Immersive Engineering 9.2.4-170, Quark Engineering, Upgrade Aquatic
5.1.0 (`RiverTreeFeature`, `RiverTreeGrower`, `UAFeatures` decompiled under `scratchpad/decomp/upgrade_aquatic/`).

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #1 | Twisted wood duplicate | Planned | tiny: there is no duplicate; tidy the code, plus one real duplicate found next to it |
| #2 | Leftover comments | Planned | tiny, plus one real gap (Quark Engineering stairs) and the Abnormals Delight bark recipes |
| #3 | Mulberry (Upgrade Aquatic river) wood -> oak | Built 2026-10-22 (recipes_wood.md 2.6) | medium: KubeJS recipes + startup list + one datapack JSON + a patcherMod event for saplings |

**Findings that change the rows:**

- **#1 has no duplicate to remove.** No mod in the pack ships a cutting-board recipe for the twisted log or wood
  (checked: FD, Quark Delight, Abnormals Delight, Architect's Palette, Create). So `bark.js` 61-62 add the only ones.
  IE has no twisted sawmill recipes either, so `sawmill: false` in `wood_types.js` 18 is right. The pack's own
  `creativeengineering:sawmill/*twisted*` covers the sawmill. There is a real duplicate one step over, though: IE's
  sawmill recipes for 7 vanilla stripped logs sit next to the pack's identical ones (#1 Build step 2).
- **#2's "4th line" is the stairs recipe.** Quark Engineering has 4 sawmill recipes per Quark species (`_door`, `_log`,
  `_slab`, `_stairs`), and the pack removes 3 of them. The comment hides a missing removal, so this is more than a
  comment fix.
- **#3 can't be done in KubeJS alone.** The river tree's trunk comes from UA's code-registered configured feature.
  A datapack JSON fixes worldgen, but the sapling (`RiverTreeGrower`) reads the built-in holder and keeps growing
  river logs (#3 Now, "Sapling").

Build order: #1 and #2 together, in one `/reload` session (both only touch `bark.js` and `wood_types.js`). Then #3, which
needs a restart (startup list, datapack worldgen, patcherMod) and should ride the next patcherMod build.

Related rows: `farming/composting #2` ([brief](farming_composting.md)) removes the other leftover comment
(`sawdust.js` "Integrate nulch, move to compost file") and moves the soil recipes. Don't repeat it here.
`ports/cavesandchasmsport #4` wants "azalea wood -> oak as usual", and structure_tuning.md's todo wants
"swap river tree to oak log ... remove driftwood". #3 is the first wood removal actually built, so build its helper
to be reused (#3 Decisions). `tooling/conventions #4` (removed-items coverage) and `recipes/progression #2` (the
recipe scan cost, which grows with every id #3 adds to `global.removedItems`).

---

## wood #1: Twisted wood duplicate
Status Planned / Work KubeJS / TODO `recipes/wood #1` ([recipes_wood.md](../recipes_wood.md) F4)

### Goal
Settle F4: make sure twisted logs and wood have exactly one stripping recipe per machine and no stale duplicate, and
drop the misleading comment.

### Now
- [bark.js](../../kubejs/server_scripts/wood/bark.js) 23-28: `compatSpecies` lists twisted with `cutLog` / `cutWood`
  (Create ids) but no `fdLog` / `fdWood`. Lines 53-57 run the FD removal + `fdCutting` over `species` only (vanilla 9),
  and 64-73 do the Quark trio. Twisted gets its own two lines, 59-62:
  `R.fdCutting(i.log_twisted, R.AXE, [i.log_twisted_stripped, Item.of(i.bark_twisted).withChance(0.50)], e)` and the
  wood version (0.75). The comment says "no matching removal id in the original (kept as-is; note the original also had
  a bug here, see caveat below)". There is no caveat below.
- Generated ids: `creativeengineering:farmersdelightcutting/twisted_log_to_stripped_twisted_log_by_fdcutting` and
  `..._twisted_wood_to_stripped_twisted_wood_by_fdcutting` (`NCH.toId`,
  [neoCraftHelper.js](../../kubejs/startup_scripts/func/neoCraftHelper.js) 21-91; `fdCutting` 945-959).
- **Verified (jar):** cutting-board recipes for twisted exist in none of `farmersdelight` (18 vanilla log/wood +
  `netherwood`), `quarkdelight` (`cutting/axe/{blossom,ancient,azalea}_{log,wood,planks}`, `bamboo_block`,
  `hollow_logs`), `abnormals_delight` (Abnormals woods only) or `architects_palette`. Create has
  `create:cutting/compat/architects_palette/{twisted,stripped_twisted}_{log,wood}`. bark.js 33-34 removes the two
  unstripped ones; the stripped -> planks ones stay, as for every species.
- **Verified (jar):** IE 9.2.4 has no twisted sawmill recipe. Its sawmill folder covers the 9 vanilla species:
  `<x>_log`, `<x>_door`, `<x>_slab`, `<x>_stairs` and `stripped_<x>_log`, plus bookshelf and treated wood. So
  `wood_types.js` 18 `sawmill: false` correctly skips a removal that has nothing to remove. patcherMod ships
  `creativeengineering:sawmill/twisted_log-twisted_planks`, `stripped_twisted_{log,wood}-twisted_planks`,
  `twisted_wood-twisted_planks` and the 11 plank-shape recipes.
- **Real duplicate found:** IE's `immersiveengineering:sawmill/stripped_{oak,spruce,birch,dark_oak,jungle,acacia,mangrove}_log`
  (stripped log -> 6 planks, `#forge:dusts/wood`, 800 RF) are not removed. patcherMod ships the same conversion as
  `creativeengineering:sawmill/stripped_<x>_log-<x>_planks` (same input, output and energy; secondary
  `immersiveengineering:dust_wood`). bark.js 91-95 removes only `stripped_crimson_log` and `stripped_warped_log`
  (and the 9 unstripped `<x>_log`). With `SawbladeRouting` rule 2 (the first recipe with no `"blade"`) the machine
  output is the same either way, but JEI shows both.

### Build
1. **bark.js:** move twisted into the FD-cutting loop instead of the two special lines. Give the compat entry a flag,
   not an `fdLog` (there is nothing to remove):
   ```js
   { log: i.log_twisted, ..., cutLog: 'create:cutting/compat/architects_palette/twisted_log', cutWood: '...', reattach: true, fdAdd: true },
   ...
   var fdSpecies = species.concat(compatSpecies.filter(s => s.fdAdd))   // twisted: add only, no FD original exists
   R.bulk('remove', species.map(s => [{ id: s.fdLog }, e]))            // removals stay vanilla-only
   ```
   Or keep lines 61-62 and just replace the comment with "No mod ships a cutting-board recipe for twisted logs
   (checked 2026-10-21), so these are added, not replaced". Either way the generated ids stay the same, because
   `toId` derives them from input and output. Recommendation: the comment-only version. It's two lines, and the loop
   version saves nothing.
2. **bark.js 91-95:** add the 7 vanilla stripped logs to the removal list:
   ```js
   'stripped_oak_log', 'stripped_spruce_log', 'stripped_birch_log', 'stripped_dark_oak_log',
   'stripped_jungle_log', 'stripped_acacia_log', 'stripped_mangrove_log',
   'stripped_crimson_log', 'stripped_warped_log',
   ```
   and change the section comment to "Remove IE's sawmill log recipes; patcherMod data/creativeengineering/recipes/sawmill/
   replaces them (engineering.md 2.1)" (the current "add own in CreativeEngineering jar file").
3. Docs: recipes_wood.md F4 -> resolved (no original, comment fixed); add the stripped-log removal to 2.1's "IE
   sawmill" row and to engineering.md 2.1 if it lists IE's removed recipes.

### Ids and data
`i.log_twisted` / `i.wood_twisted` / `i.log_twisted_stripped` / `i.wood_twisted_stripped` / `i.bark_twisted`
(`immersive_weathering:architects_palette/twisted_bark`, item_dict 434-438). No new ids.

### Decisions
- **Remove IE's stripped-log recipes or not.** Recommendation: remove. Same output, one JEI entry, and it matches what
  bark.js already does for crimson and warped.

### Gotchas
- Don't add `fdLog` ids for twisted: `R.remove` on a missing id is silent, but it suggests a recipe that doesn't exist.
- IE's `<x>_slab` sawmill recipes stay on purpose. patcherMod has no vanilla planks -> slab recipe and relies on IE's
  as the default, no-blade route.

### Tests
- **RW-11 Twisted stripping, one each:** JEI uses of `architects_palette:twisted_log` -> exactly one cutting-board
  recipe (axe, stripped log + 50 % twisted bark) and one Create saw recipe; the IE sawmill shows the pack's
  `creativeengineering:sawmill/twisted_log-twisted_planks` only.
- **RW-12 Stripped-log sawmill, one each:** JEI uses of `minecraft:stripped_oak_log` on the sawmill -> one recipe
  (`creativeengineering:sawmill/stripped_oak_log-oak_planks`); the same for spruce.

---

## wood #2: Leftover comments
Status Planned / Work KubeJS / TODO `recipes/wood #2` ([recipes_wood.md](../recipes_wood.md) F5)

### Goal
Clear the F5 comments in `wood_types.js` and `bark.js`, and do the work they point at.

### Now
- [wood_types.js](../../kubejs/server_scripts/wood/wood_types.js) 46-52: removes
  `quarkengineering:sawmill/<name>/<name>_{slab,log,door}` for blossom, ancient and azalea. Line 51 is
  `// (whatever the 4th line is below this)`. **Verified (jar):** Quark Engineering has
  `data/quarkengineering/recipes/sawmill/<name>/<name>_stairs.json` for all three (input `quark:<name>_planks_stairs`,
  result `quark:<name>_planks`, 1600 RF, `quark:flag blossom_trees` etc.), plus `sawmill/bookshelves/<name>_bookshelf`.
  So the 4th line is `_stairs`, matching the IE vanilla `_stairs` removal at 43.
- [bark.js](../../kubejs/server_scripts/wood/bark.js) 97-98: `// ── Replace FD bark recipes with IW ──` then only
  `R.remove({ id: 'farmersdelight:paper_from_tree_bark' }, e)`. Is the replacement done? FD's 18 vanilla log/wood
  cutting recipes are replaced (53-57). Quark Delight's six log/wood recipes and its `hollow_logs` are replaced too
  (64-88). **Verified (jar)** that `farmersdelight:tree_bark` is still made by Abnormals Delight's
  `abnormals_delight:upgrade_aquatic/cutting/{driftwood,driftwood_log,river_log,river_wood}`. Those are the only
  Abnormals woods in the pack, the others being conditional on mods it doesn't run. FD's `cutting/netherwood` is
  conditional too. (`i.tree_bark`, item_dict 11591, is used by no pack recipe; it has a composter value, compost_values.js 484.)
- `sawdust.js` 83 ("Integrate nulch, move to compost file"): handled by `farming/composting #2`
  ([brief](farming_composting.md)). Leave it to that row.

### Build
1. **wood_types.js 51:** replace the comment with the stairs removal:
   ```js
   [{ id: 'quarkengineering:sawmill/' + s.name + '/' + s.name + '_stairs' }, e],
   ```
   and add a section comment: "the pack's sawmill JSONs (patcherMod) replace Quark Engineering's slab / log / door /
   stairs recipes; its bookshelf recipes stay".
2. **bark.js 97:** finish "Replace FD bark recipes with IW" for driftwood. Add driftwood to the FD-cutting set, with
   removal of Abnormals Delight's two driftwood recipes (river is handled by #3, which removes the whole river set):
   ```js
   // Abnormals Delight: Upgrade Aquatic driftwood (river wood goes with recipes/wood #3)
   R.bulk('remove', ['driftwood_log', 'driftwood'].map(n => [{ id: 'abnormals_delight:upgrade_aquatic/cutting/' + n }, e]))
   R.fdCutting(i.driftwood_log, R.AXE, [i.driftwood_log_stripped, Item.of(i.upgrade_aquatic_driftwood_bark).withChance(0.50)], e)
   R.fdCutting(i.driftwood, R.AXE, [i.driftwood_stripped, Item.of(i.upgrade_aquatic_driftwood_bark).withChance(0.75)], e)
   ```
   Also the Create saw: Create 0.5.1.i ships `create:cutting/compat/upgrade_aquatic/{driftwood_log,driftwood,
   stripped_driftwood_log,stripped_driftwood}` (verified). Remove the two unstripped ones and add `createSaw` with
   bark, as for the other species. The cleanest way is a
   `compatSpecies` entry with `fdLog: 'abnormals_delight:upgrade_aquatic/cutting/driftwood_log'` etc., run through the
   same loops as the Quark trio.
   Then change the comment to "FD's tree bark: its paper recipe removed; every cutting-board stripping recipe now gives
   Immersive Weathering's species bark (FD's, Quark Delight's and Abnormals Delight's replaced above)".
3. Docs: recipes_wood.md F5 -> resolved; 2.1 species list gains driftwood (cutting board and saw); 2.4 the Quark
   Engineering stairs.

### Ids and data
- New dict keys (none of these exist in item_dict.js yet; only `i.upgrade_aquatic_driftwood_bark` does, 12924): in the
  wood block next to the twisted / Quark entries (item_dict 434-460, `i.log_<species>` style):
  `i.log_driftwood = 'upgrade_aquatic:driftwood_log'`, `i.wood_driftwood = 'upgrade_aquatic:driftwood'`,
  `i.log_driftwood_stripped = 'upgrade_aquatic:stripped_driftwood_log'`,
  `i.wood_driftwood_stripped = 'upgrade_aquatic:stripped_driftwood'`, `i.bark_driftwood =
  'immersive_weathering:upgrade_aquatic/driftwood_bark'` (alias of the existing key). Ids verified against UA's lang
  (`driftwood_log`, `driftwood`, `stripped_driftwood_log`, `stripped_driftwood`).
- **Unverified:** whether IW generates `driftwood_bark` from its own data (it's in the dict, generated from a dump, so
  it exists in game).

### Decisions
- **Add driftwood bark now or leave AD's tree bark.** Recommendation: add it. It's the last stripping recipe that
  still gives FD's generic bark, and the rejected option in recipes_wood.md 4 says one bark per species.
- **Also the other modded woods with IW bark** (Vinery cherry, Myrtrees rubberwood, the four Create D&D woods,
  Botania livingwood / dreamwood; their barks are in item_dict 10582-12922). Their logs have no cutting-board recipe
  at all today (none ships one, as far as the cached jars show). Recommendation: out of scope; note it as a new
  scratchpad idea if wanted.

### Gotchas
- Removing a QE stairs recipe that is behind a `quark:flag` condition is fine: if the flag is off the recipe never
  loads and `remove` is a no-op.
- Keep bark.js's species tables the single source. Don't add a third special case like lines 59-62.

### Tests
- **RW-13 Quark stairs on the sawmill:** a blossom planks stairs in the sawmill (plain blade) -> passes through
  unchanged or follows a pack recipe; JEI shows no `quarkengineering:sawmill/blossom/blossom_stairs`.
- **RW-14 Driftwood bark:** a driftwood log on the cutting board with an axe -> stripped driftwood log + driftwood
  bark (IW) about half the time, never FD tree bark.

---

## wood #3: Mulberry wood -> oak
Status Built 2026-10-22, needs play (RW-15 to RW-18) / Work KubeJS / data (+ patcherMod, see below) / TODO `recipes/wood #3` (scratchpad 2026-10-21)

**As built (2026-10-22), where it differs from the plan below:** every river item is an item merge into its oak twin
(`global.removedWoods` joins `global.itemMerges`), so recipe inputs, loot (old placed logs drop oak, step 6) and
inventories come from the merge system; no conversion recipes and no loot-table JSONs. All 92 ids have a twin (dict
section "removed woods"). Recipes making them: one OR-filter `R.remove`. The hedge recipe is removed and re-added
with an oak log. Sapling: the Forge event route (`SaplingGrowTreeEvent.setFeature` exists in 43.3.9), class
`MulberrySapling`, switch `[worldgen.systems] mulberrySapling`. The kept blocks are renamed Mulberry (user request).
Driftwood not included.

### Goal
Remove Upgrade Aquatic's river wood set (the "mulberry" tree is UA's river tree: river leaves with mulberry vines
hanging from them). River trees keep generating by rivers, but with oak logs. River leaves, the river sapling and the
mulberry vines stay. Every recipe that made or used river wood points at oak.

### Now
- **Verified (jar, UA 5.1.0):** the river wood set is `river_log`, `river_wood`, `stripped_river_log`,
  `stripped_river_wood`, `river_planks`, `river_stairs`, `river_slab`, `river_fence`, `river_fence_gate`,
  `river_pressure_plate`, `river_button`, `river_door`, `river_trapdoor`, `river_boards`, `vertical_river_planks`,
  `river_vertical_slab`, `river_bookshelf`, `river_ladder`, `river_sign` (+ `river_wall_sign`, block only),
  `river_chest`, `river_trapped_chest`, `river_post`, `stripped_river_post`, `river_beehive`, `river_boat`,
  `river_chest_boat`, `river_furnace_boat`, `large_river_boat`. Kept: `river_leaves`, `river_sapling`
  (+ `potted_river_sapling`), `river_leaf_pile`, `river_leaf_carpet`, `river_hedge`, `mulberry_vine`. Item tag
  `upgrade_aquatic:river_logs` = the 4 logs/woods. `river_hedge` = river leaves over `#upgrade_aquatic:river_logs`
  (x2, `blueprint:quark_flag hedges`).
- **Other mods' river items (verified in jars):** Create `create:cutting/compat/upgrade_aquatic/{river_log,river_wood,
  stripped_river_log,stripped_river_wood}`; Abnormals Delight `abnormals_delight:river_cabinet` (+ recipe
  `upgrade_aquatic/river_cabinet`) and `upgrade_aquatic/cutting/river_{log,wood,door,sign,trapdoor}`; Immersive
  Weathering `immersive_weathering:upgrade_aquatic/river_bark` (`i.upgrade_aquatic_river_bark`, item_dict 12925) and
  `.../river_leaf_pile` (kept). **Not enumerable offline:** Every Compat (`config/everycomp-wood_types.toml` 71-73
  enables `upgrade_aquatic` `driftwood` and `river`: hollow logs, Quark-style posts, etc.), Supplementaries / Supp
  Squared (sign posts, hanging signs, item shelves, e.g. `supplementaries:upgrade_aquatic/hanging_sign_driftwood` in
  stack_sizes.json 1031), Moonlight-generated sets in other mods. Get the full list in game with
  `/dumpitemsbymod everycomp,supplementaries,suppsquared,immersive_weathering,abnormals_delight,upgrade_aquatic`
  ([dump_items.js](../../kubejs/server_scripts/_lib/dump_items.js), writes `kubejs/exported/items_by_mod/`), then
  filter ids containing `river_` and drop the kept ones.
- Pack references today: only kept items. `i.river_sapling` (wandering_trades.js 28, compost_values.js 94),
  `i.river_leaves` / `i.river_leaf_carpet` (compost_values.js 185-186), `i.upgrade_aquatic_river_leaf_pile` (152),
  and `i.upgrade_aquatic_river_bark` (compost_values.js 508). The bark goes. Mulberry foods (`i.mulberry` etc.)
  don't depend on the wood.
- **Worldgen, verified (decompiled `UAFeatures`):** placed feature `upgrade_aquatic:trees_river`
  (`count_extra(1, 0.5, 2)`, vegetation placements) -> configured feature `upgrade_aquatic:river_tree`, type
  `upgrade_aquatic:river_tree` (`RiverTreeFeature`, codec = vanilla `TreeConfiguration.CODEC`). Its config is
  `RIVER_TREE_CONFIG`: trunk `simple(river_log)`, `StraightTrunkPlacer(3, 1, 1)`, foliage `simple(river_leaves)`,
  `BlobFoliagePlacer(0, 0, 0)`, `TwoLayersFeatureSize(0, 0, 0)`, `ignoreVines()`. Biome modifier
  `data/upgrade_aquatic/forge/biome_modifier/add_feature/river_tree.json` (`#minecraft:is_river`, vegetal_decoration).
  `RiverTreeFeature` places the trunk through `config.trunkPlacer` / `trunkProvider` and the leaves through
  `TreeUtil.placeLeafAt(..., config)`, and hangs `mulberry_vine` (age 4) under leaves 1 time in 6.
  `MulberryVineBlock.canSurvive` needs `#minecraft:leaves` or `#minecraft:logs` above, so oak logs are fine.
- **Sapling, verified:** `RiverTreeGrower.getConfiguredFeature` (SRG `m_213888_`) returns
  `UAConfiguredFeatures.RIVER_TREE.getHolder().get()`, the **built-in** registry holder. In 1.19.2 a datapack override
  of `upgrade_aquatic:river_tree` changes worldgen (the dynamic registry copy) but not this holder, so saplings would
  still grow river logs.
- Precedent for a datapack configured-feature override: `kubejs/data/minecraft/worldgen/configured_feature/patch_berry_bush.json`
  and `kubejs/data/vinery/worldgen/configured_feature/*.json` (shipped).
- No wood has been removed this way before. The "as with the other removed woods" in the row has no precedent in the
  code (no `log`/`planks` entries in [removed_items.js](../../kubejs/startup_scripts/_lib/removed_items.js), no wood
  `replaceInput` beyond Nature's Compass). The merge pattern to copy is the flour one: `R.replaceOutput` +
  `R.replaceInput` + `global.removedItems` ([cooking.js](../../kubejs/server_scripts/cooking/cooking.js) 76-77,
  removed_items.js 138).

### Build
1. **Dict** ([item_dict.js](../../kubejs/startup_scripts/_lib/item_dict.js), next to 13023-13025): add the set under
   one key per id plus a list key, so the startup list, the server script and the client hide share one definition:
   ```js
   // Upgrade Aquatic river wood: removed (recipes/wood #3), its recipes point at oak. Leaves, sapling, hedge, vines stay.
   i.river_log = 'upgrade_aquatic:river_log';  i.river_wood = 'upgrade_aquatic:river_wood'
   i.river_log_stripped = 'upgrade_aquatic:stripped_river_log';  i.river_wood_stripped = 'upgrade_aquatic:stripped_river_wood'
   i.river_planks = 'upgrade_aquatic:river_planks';  ...  i.river_cabinet = 'abnormals_delight:river_cabinet'
   i.t_river_logs = '#upgrade_aquatic:river_logs'
   global.removedWoods = { river: [i.river_log, i.river_wood, ..., i.upgrade_aquatic_river_bark, i.river_cabinet /* + the everycomp/supp ids from the dump */] }
   ```
   Put `global.removedWoods` in removed_items.js instead if you'd rather keep item_dict.js to plain keys
   (recommended: removed_items.js, next to `global.removedItems`, with `global.removedItems.push(...global.removedWoods.river)`
   at the end).
2. **Startup list:** append the river ids to `global.removedItems` (removed_items.js). That hides them in JEI
   (jeichanges.js 5), flags the tooltip, and strips their item tags (recipe_scan.js 50-54), which also empties
   `#upgrade_aquatic:river_logs` and takes river planks out of `#minecraft:planks`.
3. **Server, new file `kubejs/server_scripts/wood/removed_woods.js`:**
   ```js
   ServerEvents.recipes(e => {
   	const R = global.NCH, i = global.item
   	// recipes that only make removed river items
   	R.bulk('remove', global.removedWoods.river.map(id => [{ output: id }, e]))
   	// a kept item built on a river log: the hedge takes an oak log (x2 as before)
   	R.replaceInput({ id: 'upgrade_aquatic:river_hedge' }, i.t_river_logs, i.log_oak, e)
   	// anything else still asking for a river item takes the oak one (planks, logs, slabs ...)
   	var OAK = { river_planks: i.planks_oak, river_log: i.log_oak, river_wood: i.wood_oak, river_slab: i.slab_oak,
   	            river_stairs: i.stairs_oak, river_fence: i.fence_oak /* ... one row per shape with an oak twin */ }
   	Object.keys(OAK).forEach(k => R.replaceInput({ input: i[k] }, i[k], OAK[k], e))
   	// old inventories: 1:1 back to oak (logs, wood, planks), ids <x>_to_oak
   	R.bulk('shapeless', [[Item.of(i.log_oak), [i.river_log], e, 'river_log_to_oak'], ...])
   })
   ```
   Removing by output first, then `replaceInput`, means the river recipes are gone before the input swap and nothing
   gets rewritten into a second oak recipe. Write it as a function, `removeWood(e, ids, oakMap)`, so driftwood and
   the C&C azalea can reuse it. Put it in `startup_scripts/func/` next to NCH (`global.NCH.removeWood`, or a small
   `global.woods` helper; extend NCH rather than writing raw `e.remove` calls). The `R.merge(old, kept, e)` helper
   proposed in [recipes_materials.md](recipes_materials.md) is the per-item half of it; build them together. The conversion recipes keep
   pre-existing river items usable. Leave them out if the "REMOVED ITEM" tooltip and JEI hiding are enough.
4. **Worldgen override:** `kubejs/data/upgrade_aquatic/worldgen/configured_feature/river_tree.json`, the same tree with
   an oak trunk (vanilla `TreeConfiguration` JSON, values from `RIVER_TREE_CONFIG`):
   ```json
   { "type": "upgrade_aquatic:river_tree",
     "config": {
       "trunk_provider": { "type": "minecraft:simple_state_provider", "state": { "Name": "minecraft:oak_log", "Properties": { "axis": "y" } } },
       "trunk_placer": { "type": "minecraft:straight_trunk_placer", "base_height": 3, "height_rand_a": 1, "height_rand_b": 1 },
       "foliage_provider": { "type": "minecraft:simple_state_provider", "state": { "Name": "upgrade_aquatic:river_leaves" } },
       "foliage_placer": { "type": "minecraft:blob_foliage_placer", "radius": 0, "offset": 0, "height": 0 },
       "minimum_size": { "type": "minecraft:two_layers_feature_size", "limit": 0, "lower_size": 0, "upper_size": 0 },
       "dirt_provider": { "type": "minecraft:simple_state_provider", "state": { "Name": "minecraft:dirt" } },
       "decorators": [], "ignore_vines": true, "force_dirt": false } }
   ```
   (River leaves' default state is used. If the log shows the wrong leaf distance, add the state's `Properties` from
   F3.)
5. **Saplings (patcherMod):** make the river sapling read the dynamic registry. Two ways:
   - Forge event (preferred if the API is there; **unverified for Forge 43.3.9**: check that
     `net.minecraftforge.event.level.SaplingGrowTreeEvent` has `getFeature()` / `setFeature(Holder)`): in a
     `@SubscribeEvent` handler, if `event.getFeature()` is the `upgrade_aquatic:river_tree` key, replace it with
     `level.registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY).getHolderOrThrow(key)`. Then the
     datapack JSON drives both worldgen and saplings.
   - Mixin `RiverTreeGrowerMixin` on `com.teamabnormals.upgrade_aquatic.common.block.grower.RiverTreeGrower`, method
     `m_213888_(RandomSource, boolean)` (`remap = false`, SRG name as the project's other vanilla-method mixins),
     `@Inject(at = @At("RETURN"), cancellable = true)` returning a lazily built `Holder.direct(new ConfiguredFeature<>(
     UAFeatures.RIVER_TREE.get(), <RIVER_TREE_CONFIG with BlockStateProvider.simple(Blocks.OAK_LOG)>))`. Needs the UA
     jar in `development/patcherMod/libs/` (copied from `mods/`). The mixin has no level to read the registry from,
     so it duplicates the tree shape in Java.
   Gate either behind a CEConfig switch, e.g. `[worldgen.systems] riverTreeOak = true`, so it can be turned off
   without a build. Add it to `creativeengineering.mixins.json` (mixin route only).
6. **Loot for already-placed logs:** chunks generated since 1.0.x hold river logs. Override
   `kubejs/data/upgrade_aquatic/loot_tables/blocks/river_log.json` (and `river_wood`, `stripped_*`, `river_planks`) to
   drop the oak twin, so chopping an old tree gives oak. Recommended: yes for the 4 logs/woods (players meet them),
   not for crafted shapes (the conversion recipes cover inventories).
7. **Every Compat:** leave `config/everycomp-wood_types.toml` `[types.wood_type.upgrade_aquatic] river = true` as is
   and hide its river blocks through the list (step 1). Setting it false unregisters blocks that may be placed in
   worlds (Forge missing-registry prompt, the blocks turn to air). Keep `[types.leaves_type.upgrade_aquatic] river`.
8. **compost_values.js 508:** drop the `i.upgrade_aquatic_river_bark` row (or leave it; a removed item composting is
   harmless).
9. Docs: a new 2.6 "Removed woods" in [recipes_wood.md](../recipes_wood.md) (river set, oak map, worldgen JSON,
   sapling fix, config switch), the RW tests below, the structure_tuning.md todo line ("swap river tree to oak log,
   remove river log/wood") marked done for the river half.

### Ids and data
- UA ids above (verified, jar lang + recipes + loot tables). Oak twins from the dict: `i.log_oak`, `i.wood_oak`,
  `i.log_oak_stripped`, `i.wood_oak_stripped`, `i.planks_oak`, `i.slab_oak`, `i.vslab_oak`, `i.vplanks_oak`,
  `i.stairs_oak`, `i.fence_oak`, `i.fencegate_oak`, `i.door_oak`, `i.trapdoor_oak`, `i.ladder_oak`, `i.button_oak`,
  `i.pressureplate_oak`, `i.sign_oak` (wood_types.js 9 uses them). No oak twin in the dict for boards, post, beehive,
  bookshelf, chests, boats under the `<shape>_oak` names; the dict has them under other keys: `i.oak_post`
  (`quark:oak_post`), `i.bookshelf`, `i.chest`, `i.oak_boat`. Boards, beehive, trapped chest, ladder-less shapes just
  go (recipes removed).
- Configured feature `upgrade_aquatic:river_tree`, placed `upgrade_aquatic:trees_river` (verified).

### Decisions
- **Driftwood too?** structure_tuning.md's todo pairs it ("remove driftwood/logs"). Recommendation: not in this row.
  The pack places driftwood logs in its own worldgen (`creativeengineering:meta/flotilla_driftwood`,
  `driftwood_land/*`, `kubejs/data/upgrade_aquatic/worldgen/placed_feature/driftwood_*`), so removing it means
  editing those features as well. File it as its own row; it reuses the helper from step 3.
- **Sapling: Forge event or mixin.** Recommendation: the event if Forge 43.3.9 has `setFeature` (one JSON drives
  both); otherwise the mixin.
- **Conversion recipes for old items.** Recommendation: logs, wood and planks only (1:1 shapeless to oak). Shapes and
  boats can stay as hidden leftovers.

### Gotchas
- **Order inside the server script:** remove by output before `replaceInput`, or the input swap turns UA's own
  river-planks recipes into duplicate oak recipes.
- `global.removedItems` strips tags at `ServerEvents.tags('item')`. Any recipe that names `#upgrade_aquatic:river_logs`
  (the hedge, Create's compat cutting, AD's cabinet) goes dead silently. The explicit hedge `replaceInput` keeps the
  one kept item craftable.
- `recipes/progression #2`: each id added to `global.removedItems` is another full recipe pass in recipe_scan.js on
  the first load. About 30 more ids is noticeable; do that row first or together.
- A worldgen JSON change needs a restart and only affects new chunks.
- Leaves next to oak logs decay by the pack's leaf-decay rules ([mob_block_tweaks.md](../mob_block_tweaks.md) 2.5).
  River leaves are in `#minecraft:leaves`, so nothing changes.

### Tests
- **RW-15 River set gone:** JEI search `@upgrade_aquatic river` -> only leaves, sapling, leaf pile, leaf carpet, hedge;
  a river hedge recipe takes an oak log.
- **RW-16 River tree:** `/place feature upgrade_aquatic:river_tree` on grass by a river -> oak trunk, river leaves,
  some mulberry vines; new chunks along a river show the same trees.
- **RW-17 River sapling:** bone-meal a river sapling -> an oak-trunk river tree (the patcherMod fix).
- **RW-18 Old logs:** break a river log placed before the change (or `/setblock` one) -> drops an oak log; a river
  planks item in the grid -> oak planks.

---

## Built, needs play

- `wood #3` (2026-10-22): built; `MulberrySapling` is in the `mods/` jar (11:06); scratch SQ-MB queued; RW-15 to RW-20 in [recipes_wood.md](../recipes_wood.md) 7.

## Writeup corrections made

- [recipes_wood.md](../recipes_wood.md) 2.4: "Quark Engineering's sawmill recipes for the Quark three are removed" ->
  only the slab, log and door ones; the stairs ones are not (F5).
- [recipes_wood.md](../recipes_wood.md) F4: added the 2026-10-21 jar check (no mod ships a twisted cutting-board
  recipe and IE has no twisted sawmill recipe, so there is no duplicate) and the IE stripped-log duplicate.
- [recipes_wood.md](../recipes_wood.md) F5: added that the "4th line" is the Quark Engineering stairs recipe.
