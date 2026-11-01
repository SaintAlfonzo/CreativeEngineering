# gear / archery: implementation brief

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# gear`, Sub `archery`; updated 2026-10-31 (brief sweep).
Feature writeup: [archery.md](../archery.md) (test prefix `AR-`, last id AR-18).

## Scope and order

| Order | Row | Status | Work |
|---|---|---|---|
| 1 | `tooling/conventions #7` NCH `ieBlueprint` repeated inputs | Bug | patcherMod jar helper (`neoCraftHelper.js`), rebuild + restart |
| - | `gear/archery #2`, `#5` | Testing | built 2026-10-22, see Built, needs play |
| - | `gear/archery #1`, `#3`, `#4` | Complete | see Built, needs play |

The only open build work is the NCH fix. It lives in this file because it was found writing the archery brief and it
blocks two archery blueprint recipes (spectral, split arrow); it fixes 7 recipes outside archery too.

---

## tooling/conventions #7: `NCH.ieBlueprint`: repeated inputs never match
Status **Bug** · Work: KubeJS (helper in the patcherMod jar) · id `tooling/conventions #7`

### Goal
A blueprint recipe that lists the same ingredient more than once must be craftable in the Engineer's Workbench. Fix it
once in the helper, not at the call sites.

### Now
- Helper: [neoCraftHelper.js:2277-2300](../../development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/neoCraftHelper.js)
  `ieBlueprint(outputs, inputs, category, e, idOverride)`. It maps each input through `_nbtItemJson(..., { countMode:
  'base_ingredient' })` (line 541-600; plain inputs fall through to `_itemJson`, line 479) and emits one JSON input per
  array entry. No merge step. Unchanged since the 2026-10-21 scan (checked 2026-10-31; last touched in commit ffa47cf).
- Why it fails (IE 9.2.4-170 `BlueprintCraftingRecipe.getMaxCrafted`, decompiled 2026-10-21): the workbench pools its
  stacks by item, then each ingredient in order takes `pool / count` crafts' worth from every matching pool. Four
  separate `arrow` x1 ingredients: the first takes every arrow, the second finds none, the recipe returns 0. A tag input
  still works if the player puts in two different matching items; one kind fails.
- Call sites with a repeated input (scanned 2026-10-31, 75 `ieBlueprint(` calls in `kubejs/server_scripts`):

  | File:line | Recipe | Repeated input |
  |---|---|---|
  | [archery.js:65](../../kubejs/server_scripts/equipment/archery.js) | spectral arrow | `i.glowstone` x2 |
  | archery.js:275, 276 | split arrow (electrum, brass) | `i.arrow` x4 |
  | [redstone.js:53](../../kubejs/server_scripts/engineering/redstone.js) | dropper | `i.anystone` x4 |
  | redstone.js:60 | observer | `i.anystone` x4 |
  | redstone.js:65 | piston | `i.planks` x2, `i.anystone` x2 |
  | redstone.js:229 | thunder detector | `i.mundane_potion` x2 |
  | [music.js:12, 18](../../kubejs/server_scripts/engineering/music.js) | note block, jukebox | `i.planks` x3 |

  9 recipes. `i.planks` = `#minecraft:planks`, `i.anystone` = `#forge:stone` (tags), `i.mundane_potion` is a raw
  `forge:nbt` ingredient object (item_dict.js:1306), `i.glowstone` / `i.arrow` plain items.
- Not seen in game: AR-08 and AR-11 never run; no scratch queue entry.

### Build
1. In `ieBlueprint`, after the `inputs.map(...)`, merge entries whose ingredient part is identical and add their counts:
   ```js
   var merged = [], byKey = {}
   jsonInputs.forEach(j => {
     var ing = j.base_ingredient !== undefined ? j.base_ingredient : j
     var cnt = j.base_ingredient !== undefined ? (j.count || 1) : 1
     var key = JSON.stringify(ing)
     if (key in byKey) merged[byKey[key]].count += cnt
     else { byKey[key] = merged.length; merged.push({ base_ingredient: ing, count: cnt }) }
   })
   json.inputs = merged.map(m => m.count > 1 ? m : m.base_ingredient)
   ```
   Merge on the emitted JSON (after `_nbtItemJson`), never by rebuilding an `Item.of(tag, n)`: that collapses a tag to
   its first item (memory `kubejs-api-notes`). The id is computed before the merge (`idOverride` / `toId`), so no
   recipe id changes.
2. Leave the call sites as they are (the merge covers them). Optional: write `{ id: i.arrow, count: 4 }` in archery.js
   275-276 for readability (the `{ id, count }` form, as in blueprints.js and transport.js:76).
3. Rebuild and install patcherMod, full restart (the helper is in the jar's `kubejs_startup/`).
4. archery.md 5 (file map line for `NCH.ieBlueprint`) and the AR tests below; pack_hygiene.md if it lists helper bugs.

### Ids and data
No new ids. IE `immersiveengineering:blueprint` recipe type, `IngredientWithSize` = `{ base_ingredient, count }`
(verified against the IE jar 2026-10-21).

### Decisions
- **Merge in the helper or reject repeats with an error:** recommend merge. Writing the same input twice is the natural
  way to read a recipe, and an error would break 9 recipes until each is rewritten.

### Gotchas
- Two different tags that overlap (e.g. `#minecraft:planks` and a specific plank) are not merged and can still starve
  each other in IE's greedy matcher. None in the pack today; note it in the helper comment.
- `JSON.stringify` key order: entries built by the same helper path give the same order, so identical inputs match.
- No changelog bullet needed for the helper itself, but the 9 recipes become craftable: one bullet under the catch-all
  ("Engineer's Workbench recipes that list an input twice can now be crafted (spectral and split arrows, dropper,
  observer, piston, thunder detector, note block, jukebox)").

### Tests
- **AR-new1** (proposed, numbered when queued) Duplicate blueprint inputs: workbench, archery blueprint, 2 glowstone
  dust + 1 arrow -> 2 spectral arrows; 4 arrows + electrum wire -> 2 split arrows (fails before the fix).
- **AR-new2** Redstone blueprint: 4 of one stone kind + quartz + redstone -> observer; 3 oak planks + amethyst shard +
  redstone -> note block.
- Scratch: `/dumprecipes`, check the 9 recipe JSONs have one counted input per repeated ingredient (SQ id when queued).

---

## Built, needs play

| Row | Change | Status | Notes |
|---|---|---|---|
| `gear/archery #2` | Dragon scale bullets unreachable | Testing | Category `bullet` (bullets.js), 2026-10-22, deployed 1.1.01; [archery.md](../archery.md) F1. Scratch SQ-GM-04 PASSED; open: AR-13 in game (P) |
| `gear/archery #5` | Archery leftovers | Testing | Blazing shaft tag in the explosive arrow recipes, bullets.js cleaned, F8 moot; 2026-10-22, deployed 1.1.01; [archery.md](../archery.md) F5, F7, F8. Scratch SQ-GM-04 PASSED; open: AR-07 in game (P) |
| `gear/archery #3` | Split arrow cost | Complete | Decided 2026-10-29: keep grid 6 / blueprint 4 ([archery.md](../archery.md) F2). The blueprint version needs `tooling/conventions #7` |
| `gear/archery #4` | Archery blueprint needs an Ethereal Arrow | Complete | Decided 2026-10-22: Ethereal Arrow kept ([archery.md](../archery.md) F3) |
| `gear/archery #1` | Archery and bullets | Complete | AR-01 to AR-18 unticked; the spectral and split arrow blueprints wait on `tooling/conventions #7` |

## Writeup corrections made

- [archery.md](../archery.md) 4 F1: IE's blueprint categories verified against the IE 9.2.4-170 jar (the text said "as
  far as I know; IE jar not in the repo").
- [archery.md](../archery.md) 4 F8: Supplementaries 2.4.20 has no quiver crafting recipe (verified); F8 marked moot,
  AR-15 is only a JEI confirmation.
- [archery.md](../archery.md) 5: `NCH.ieBlueprint` is at ~line 1787, not ~1607; the arrow tooltip lines are
  tooltips.js 592-618 and 673 (were 582-608 and 672); `NockRecipeBookSync` is registered at `CreativeEngineering.java`
  line 291 (was 266).
