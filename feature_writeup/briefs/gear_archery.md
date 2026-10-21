# gear / archery: implementation brief

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# gear`, Sub `archery` (rows 2 to 5 open, row 1 Complete).
Feature writeup: [archery.md](../archery.md) (test prefix `AR-`, last id AR-18).

## Scope and order

All four rows are KubeJS recipe edits in three files ([archery.js](../../kubejs/server_scripts/equipment/archery.js),
[bullets.js](../../kubejs/server_scripts/equipment/bullets.js), [blueprints.js](../../kubejs/server_scripts/engineering/blueprints.js)),
plus one NCH helper fix. One session, `/reload`, no restart.

1. **NCH `ieBlueprint` duplicate-input fix** (new, found in this scan; see `archery #3` Now). Without it the split-arrow
   and spectral-arrow blueprint recipes can never be crafted, whatever `archery #3` decides. It also fixes 6 recipes
   outside archery. Do it first.
2. `archery #2`: one word in bullets.js.
3. `archery #3`: a decision, then the split-arrow blueprint rewritten with a counted input.
4. `archery #4`: a decision, then the blueprint recipe in blueprints.js.
5. `archery #5`: cleanup (blazing shaft tag, bullets.js stub). The Supplementaries quiver item is moot (verified below).
   Optionally fold archery.md F4 in here (the 44 hand-written `e.add` tag lines → `global.tags.add`), since the same tag
   block is touched.

The NCH fix is helper work that `tooling` would normally own. **Propose a new TODO row** (`tooling/kubejs-helpers`, or
`recipes/engineering`): "ieBlueprint: duplicate inputs never match". It is built here because archery needs it.

---

## archery #2: Dragon scale bullets unreachable
Status **Bug** · Work: KubeJS · id `gear/archery #2`

### Goal
Make the dragon-scale armor-piercing cartridge recipe reachable.

### Now
- [bullets.js:18](../../kubejs/server_scripts/equipment/bullets.js):
  `R.ieBlueprint({ item: i.armor_piercing, count: 64 }, [Item.of(i.empty_casing, 64), Item.of(i.gunpowder, 32), i.dragon_scale], 'projectiles', e, 'armor_piercing_dragon_scale')`.
  Recipe id `creativeengineering:blueprint/projectiles/armor_piercing_dragon_scale` (`NCH.ieBlueprint`,
  [neoCraftHelper.js:1787](../../kubejs/startup_scripts/func/neoCraftHelper.js)).
- **Verified 2026-10-21 against `ImmersiveEngineering-1.19.2-9.2.4-170.jar`:** IE's blueprint categories are
  `bannerpatterns`, `bullet` (casull, armor piercing, silver, buckshot), `components`, `electrode`, `molds` and
  `specialBullet` (dragon's breath, flares, homing, potion, wolfpack, explosive). IE's own armor-piercing recipe
  (`immersiveengineering:blueprint/bullet_armorpiercing`) is `category: "bullet"`: 4 `empty_casing` + 2 `#forge:gunpowder`
  + 2 `#forge:nuggets/steel` + 2 `#forge:nuggets/constantan` → 4. Nothing makes a `projectiles` blueprint.
- The pack's bullet blueprint: [blueprints.js:30-35](../../kubejs/server_scripts/engineering/blueprints.js)
  (`{blueprint:"bullet"}`), and IE's own `immersiveengineering:crafting/blueprint_bullets` (`gcg / ddd / ppp`, not
  removed). `bullet` is in the duplication list (line 206).

### Build
1. bullets.js:18: `'projectiles'` → `'bullet'`. The id becomes
   `creativeengineering:blueprint/bullet/armor_piercing_dragon_scale`. Nothing references the old id (grep: only this
   line).
2. archery.md 2.4 and F1: fixed, with the date; AR-13 expected result: available on the bullet blueprint.

### Ids and data
`immersiveengineering:armor_piercing` (`i.armor_piercing`, item_dict:3809), `immersiveengineering:empty_casing`
(`i.empty_casing`:1643), `minecraft:gunpowder` (`i.gunpowder`:1393), `dragonloot:dragon_scale` (`i.dragon_scale`:6321).

### Decisions
- **`bullet` vs a new `projectiles` blueprint:** **recommend `bullet`.** It's IE's own category for this cartridge, the
  blueprint already exists, and a separate blueprint for one recipe adds nothing. The ratio checks out: 64 casings + 32
  gunpowder per scale is IE's 4 : 2 per 4 cartridges, with the scale replacing 32 steel + 32 constantan nuggets.

### Gotchas
- The 64-casing input is one `IngredientWithSize` of 64 (`Item.of(..., 64)` → `base_ingredient` + `count`), which IE
  matches across workbench slots. Fine.
- JEI lists blueprint recipes per category: check that it shows under the bullet blueprint.

### Tests
**AR-13** rewritten: Engineer's Workbench with the bullet blueprint, 64 casings + 32 gunpowder + a dragon scale → 64
armor-piercing cartridges; JEI shows the recipe under the bullet blueprint.

---

## archery #3: Split arrow cost
Status **Planned** · Work: KubeJS · id `gear/archery #3` · Depends on the NCH fix (Scope step 1)

### Goal
Settle grid 6 arrows vs blueprint 4 arrows (both + 1 electrum or brass wire → 2 split arrows).

### Now
- [archery.js:273](../../kubejs/server_scripts/equipment/archery.js): shaped `a a / awa / a a`, `a` = `i.arrow`, `w` =
  `[i.wire_electrum, i.wire_brass]` → 2. (No `idOverride`; the id is derived by `NCH.toId`.)
- archery.js:275-276: `R.ieBlueprint({ item: i.split_arrow, count: 2 }, [i.arrow, i.arrow, i.arrow, i.arrow, i.wire_electrum], 'archery', e, 'split_arrow_electrum')`
  and the same with `i.wire_brass`.
- **The blueprint version can't be crafted (new finding, verified against the IE jar).**
  `BlueprintCraftingRecipe.getMaxCrafted` (called by the workbench through `BlueprintInventory` → `matchesRecipe`)
  works like this:
  1. It pools the workbench's stacks by item.
  2. Then, for each ingredient in order, it takes `pool / ingredient.count` crafts' worth from **every** matching pool,
     greedily.
  3. If any ingredient finds nothing left, it returns 0.

  With four separate `i.arrow` ingredients of size 1, the first one takes every arrow and the second finds none, so the
  recipe never matches. The same goes for every `ieBlueprint` call that repeats an input. Scanned 2026-10-21:

  | File:line | Repeated input |
  |---|---|
  | archery.js:65 (spectral arrow) | `i.glowstone` x2 |
  | archery.js:275, 276 (split arrow) | `i.arrow` x4 |
  | engineering/redstone.js:55, 64 | `i.anystone` x4 |
  | engineering/redstone.js:69 | `i.planks` x2, `i.anystone` x2 |
  | engineering/redstone.js:229 | `i.mundane_potion` x2 |
  | engineering/music.js:12, 18 | `i.planks` x3 |

  A tag input (`i.planks`, `i.anystone`) still works when the player puts in two **different** matching items. With a
  single kind it fails. Not yet seen in game (AR-08 and AR-11 have never been run).
- What a split arrow does (NEA 1.2.0 `SplitArrow`, decompiled): on any hit it spawns **8** plain arrows in a ring (every
  45°) and discards itself. The spawned arrows have no owner, so they can't be picked up.

### Build
1. **NCH fix** ([neoCraftHelper.js:1787-1810](../../kubejs/startup_scripts/func/neoCraftHelper.js) `ieBlueprint`): after
   `inputs.map(...)`, merge entries whose ingredient part is identical and add up their counts:
   ```js
   var merged = [], byKey = {}
   jsonInputs.forEach(j => {
     var ing = j.base_ingredient !== undefined ? j.base_ingredient : j
     var cnt = j.base_ingredient !== undefined ? (j.count || 1) : 1
     var key = JSON.stringify(ing)
     if (key in byKey) { merged[byKey[key]].count += cnt }
     else { byKey[key] = merged.length; merged.push({ base_ingredient: ing, count: cnt }) }
   })
   json.inputs = merged.map(m => m.count > 1 ? m : m.base_ingredient)
   ```
   Log one line per merge at debug level if NCH has one; otherwise silent. This fixes all nine recipes above with no edits
   to their call sites. Recipe ids don't change (the id comes from `idOverride` / `toId` before the merge).
2. Optionally make the archery call explicit: `[Item.of(i.arrow, 4), i.wire_electrum]`.
3. Apply the cost decision (below) to line 273 and/or 275-276.
4. archery.md 2.1 Split Arrow row, F2 (decided), AR-08.

### Ids and data
`nock_enough_arrows:split_arrow` (`i.split_arrow`:797), `minecraft:arrow` (`i.arrow`), `immersiveengineering:wire_electrum`
(`i.wire_electrum`:1267), `creativeengineering:brass_wire` (`i.wire_brass`:1183).

### Decisions
- **6 or 4:** archery.md's own rule is that the crafting grid stays deliberately worse than the fletching table and the
  workbench ("Known limits": vanilla grid recipes remain, deliberately worse). Grid 6 / blueprint 4 follows that rule.
  **Recommend keeping 6 (grid) and 4 (blueprint), documenting it as intended, and closing F2.** If the user wants one
  number, use 4 in both. Each split arrow already returns 8 arrow hits, so the split arrow stays cheap either way.

### Gotchas
- Shaped recipes key the wire as an array, so both wires work in the grid. The blueprint needs one recipe per wire
  (`IngredientWithSize` takes one ingredient). Keep both lines, or make a `#forge:wires/split_arrow` tag through
  `global.tags` and use one line.

### Tests
- **AR-08** rewritten: grid 6 arrows + wire → 2 split arrows; workbench (archery blueprint) 4 arrows + wire → 2, with
  exactly 4 arrows in the workbench.
- **AR-19 Duplicate blueprint inputs:** workbench, archery blueprint, 2 glowstone dust + 1 arrow → 2 spectral arrows
  (fails before the NCH fix).

---

## archery #4: Archery blueprint needs an Ethereal Arrow
Status **Planned** · Work: KubeJS · id `gear/archery #4`

### Goal
Decide whether the `archery` blueprint may depend on a trader-only arrow.

### Now
- [blueprints.js:9-21](../../kubejs/server_scripts/engineering/blueprints.js): both recipes (`blueprint_archery`: `abc /
  ddd / ppp` with 3 blue dye + 3 paper; `blueprint_archery_schematic`: `abc / _s_`) use `a` echoing, `b` **ethereal**,
  `c` reinforced arrow.
- Ethereal Arrow (`nock_enough_arrows:ethereal_arrow`, `i.ethereal_arrow`:780) has no recipe. NEA 1.2.0 ships no loot
  table for it (jar checked). The wandering trader sells **8 for 6 emeralds**
  ([wandering_trader_catalog.md:97](../wandering_trader_catalog.md)).
- Blueprints copy: any blueprint + empty schematic → 2 (blueprints.js:202-233). So one ethereal arrow (one trader roll
  gives 8) unlocks the blueprint for good. The trader only gates the **first** archery blueprint per world (or team).

### Build (if the user swaps it)
Replace `b: i.ethereal_arrow` in both recipes with `b: i.seeker_arrow` (eye of ender + light shaft, a fletching-table
recipe, archery.js ~188). Teleportation (ender pearl + quick shaft) works too. Update archery.md 2.3 and F3, AR-10.

### Decisions
- **Keep or swap:** **recommend swap to the seeker arrow.** Echoing (echo shard, light shaft) and reinforced (obsidian or
  scale, steel rod) already show off the shaft classes. A seeker (light shaft, eye of ender) is a mid-game goal, while
  waiting on a random trader roll blocks all bulk arrow production for a server with no other route. If the user likes
  the trader dependency, keep it and add a one-line tooltip on the archery blueprint saying the Ethereal Arrow comes from
  the wandering trader.

### Gotchas
The blueprint item is IE's `immersiveengineering:blueprint` with NBT. `R.shaped(Item.of(i.blueprint, '{blueprint:"archery"}'), ...)`
already handles it; keep the same recipe ids (`blueprint_archery`, `blueprint_archery_schematic`) so JEI bookmarks survive.

### Tests
AR-10 updated to the chosen arrow.

---

## archery #5: Archery leftovers
Status **Planned** · Work: KubeJS · id `gear/archery #5`

### Goal
Close archery.md F5 (unused blazing shaft tag), F7 (bullets.js stub) and F8 (Supplementaries quiver recipe).

### Now
- **F5:** `#forge:arrow_shaft/blazing` = blaze rod ([equipmenttags.js](../../kubejs/server_scripts/tags/equipmenttags.js)
  "Blazing" block, with the blaze rod also in `#forge:arrow_shaft`); dict key `i.blazing_arrow_shaft` (item_dict:765).
  The explosive arrow names the item instead: archery.js:196 `R.fletching(i.tnt, i.blaze_rod, ...)` and :198
  `R.ieBlueprint(..., [i.tnt, i.blaze_rod, i.any_fletching], ...)`.
- **F7:** bullets.js has an empty "Removals" header (lines 10-12) and one recipe (`archery #2`). The `Hello! ... started`
  log lines are the pack-wide pattern (archery.js and blueprints.js have them too), not a stub symptom.
- **F8, verified 2026-10-21: there is no Supplementaries quiver recipe to remove.** `supplementaries-1.19.2-2.4.20.jar`
  has no crafting recipe that outputs `supplementaries:quiver`. The quiver is loot-only there (`inject/dungeon_quiver`,
  `inject/mansion_quiver`, quiver skeletons). The only quiver-named recipe is `supplementaries:quiver_fd`, a Farmer's
  Delight cutting recipe (saddle + shears → 2 leather) gated on the quiver flag. The pack's quiver recipe (archery.js:290,
  `TCT / TCT / _T_`) is the only one. Supplementaries can also add recipes through its runtime dynamic pack, which the jar
  listing doesn't show, so AR-15 still confirms in JEI.

### Build
1. **F5:** use the tag. archery.js:196 and :198 `i.blaze_rod` → `i.blazing_arrow_shaft` (`#forge:arrow_shaft/blazing`).
   The recipe accepts the same items today; the tag then lets other "blazing" shafts join (Create's blaze-related items,
   none chosen yet).
2. **F7:** delete the empty "Removals" block in bullets.js. Keep the file (bullets get their own home once more
   cartridge recipes come).
3. **F8:** nothing to build. Close it in archery.md as "no such recipe in 2.4.20".
4. Optional, same session (archery.md F4): rewrite the shaft block in equipmenttags.js as one `global.tags.add` call
   per tag, e.g.
   `global.tags.add(e, 'forge:arrow_shaft/quick', [i.rod_silver, i.rod_electrum, i.rod_gold, i.rod_rose_gold])`,
   with `#forge:arrow_shaft` built from the class tags.
   `global.tags.add(e, 'forge:arrow_shaft', ['#forge:arrow_shaft/metal', '#forge:arrow_shaft/wooden', '#forge:arrow_shaft/blazing'])`
   uses nested references, which `add` passes through to `event.add` as-is. Membership doesn't change. Check with
   `/kubejs dump_tags` or JEI on `#forge:arrow_shaft` before and after.

### Ids and data
`#forge:arrow_shaft/blazing`, `minecraft:blaze_rod` (`i.blaze_rod`:1396), `supplementaries:quiver` (`i.quiver`:1524),
`nock_enough_arrows:quiver` (`i.quiver_out`:4256, removed item).

### Decisions
None; the row says none.

### Gotchas
- `R.fletching` derives its recipe id from the result and the arrowhead (`toId(result, arrowheadName, "fletching")`), so
  changing the shaft keeps the id. The blueprint uses `idOverride` `explosive_arrow`, also unchanged.
- If the F4 rewrite uses nested `#tag` entries, `#forge:arrow_shaft` must not include itself.

### Tests
- **AR-07** unchanged (TNT + blaze rod + fletching → 4) and must still pass.
- **AR-15** rewritten: JEI on `supplementaries:quiver` → exactly one recipe (the pack's copper one).

---

## Built, needs play

| Row | Change | Status | Notes |
|---|---|---|---|
| archery #1 | Archery and bullets | Complete | Code matches archery.md, except two things it doesn't know: the spectral and split arrow blueprint recipes can't match (above), and F8 is moot. AR-01 to AR-18 unticked |

## Writeup corrections made

- [archery.md](../archery.md) 4 F1: IE's blueprint categories verified against the IE 9.2.4-170 jar (the text said "as
  far as I know; IE jar not in the repo").
- [archery.md](../archery.md) 4 F8: Supplementaries 2.4.20 has no quiver crafting recipe (verified); F8 marked moot,
  AR-15 is only a JEI confirmation.
- [archery.md](../archery.md) 5: `NCH.ieBlueprint` is at ~line 1787, not ~1607; the arrow tooltip lines are
  tooltips.js 592-618 and 673 (were 582-608 and 672); `NockRecipeBookSync` is registered at `CreativeEngineering.java`
  line 291 (was 266).
