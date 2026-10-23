# Brief: cooking / oven

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# cooking`, Sub `oven`. Reference writeups: [oven.md](../oven.md)
(block, `creativeengineering:baking`, 38 recipes, OV- tests), [remainder_buffer.md](../remainder_buffer.md),
[tooltips.md](../tooltips.md). Code: [baking.js](../../kubejs/server_scripts/cooking/baking.js),
[cooking.js](../../kubejs/server_scripts/cooking/cooking.js) (muffin pot recipes, lines 359-369),
`global.NCH.ceBaking` / `fdCooking` in [neoCraftHelper.js](../../kubejs/startup_scripts/func/neoCraftHelper.js)
(`fdCooking` line 926, `ceBaking` line 984), [tooltips.js](../../kubejs/client_scripts/tooltips.js), patcherMod `oven/`.
Oven bugs in the shared leftover slots are in [cooking_kitchen.md](cooking_kitchen.md) (#14).

## Scope and order

Open rows: #3 (Bug), #4, #5, #6 (Planned), #2 (In Progress). #1 is Testing (table at the end).

**Findings that change the rows:**

- **#3 is most likely not a recipe bug.** The pack's muffin pot recipes are fine on paper, and FD's matching
  (verified, `CookingPotRecipe.matches`, FD 1.2.4) counts one item per *slot*: the recipe lists two blueberries, so it
  needs two separate slots with one berry each, six filled slots in all. A stack of berries in one slot never
  matches. Confirm in game with JEI's [+] (OV-13) before changing anything.
- **#4: the Fantasy Furniture ovens are not decorative.** All seven `fantasyfurniture:*/oven` blocks (6.7.0) have a
  block entity and a menu (input, fuel, output) and cook **smoking** recipes (`RecipeType.SMOKING`, verified from the
  decompiled `OvenBlockEntity`). The decorative ones are Farmhouse Decorations' three antique ovens (no block entity).
  The Cooking for Blockheads oven cooks **smelting** recipes with food results only (verified) and, with the pack's
  config, takes no fuel but cooking oil (see [cooking_kitchen.md](cooking_kitchen.md) #8).
- **#2: only the oven's own pattern is a placeholder.** The bakeware is hammered since 2026-10-18 (oven.md 2.5).
- **#6 and `baking/baked-goods #1` share one decision:** whether "no leftover crafting route" removes hand crafting
  for baked goods. [baking_baked-goods.md](baking_baked-goods.md) recommends reading it as "no *wheat* route" and asking
  the user first. This brief follows that.

**Build groups and order:**

1. **#3 in-game check** (no code), then **#5** (two numbers in baking.js). One `/reload`.
2. **#6 baking audit**: banana bread + honey cake first (known), then the listed audit. With `baking/baked-goods #1`.
3. **#4 tooltips** (client script, `F3+T`) and the CfB oven smoking mixin (patcherMod, restart). Pair the mixin with
   kitchen #8's fuel fix.
4. **#2**: pick the final oven pattern after a clash check; with the next recipe pass.

---

## oven #3: Muffins may not be cookable
Status Bug / Work KubeJS / TODO `cooking/oven #3`

### Goal
Blueberry and cranberry muffins cook in the FD pot (and bake in the oven), or the reason they don't is known.

### Now
- Fruits Delight 0.5.11 ships the pot recipes as `farmersdelight:cooking/blueberry_muffin` / `cranberry_muffin`
  (file `data/farmersdelight/recipes/cooking/*.json` in the fruitsdelight jar, verified): `#forge:fruits/blueberry` x2
  (two ingredient entries), `#forge:dough/wheat`, `#forge:milk/milk_bottle`, `#forge:eggs`, `minecraft:sugar` -> 2
  muffins. `#forge:fruits/blueberry` = `fruitsdelight:blueberry` only (verified).
- `cooking.js` lines 364-369 remove those ids and add `creativeengineering:farmersdelightcooking/blueberry_muffin` /
  `cranberry_muffin` through `R.fdCooking`: the same, but with `i.any_dough` (`#forge:dough`) and `i.any_sugar`
  (`#forge:sugar`). Since 1.0.8.
- Create 0.5.1.i puts `create:dough` in both dough tags (oven.md 3.8), so the dough was never the cause.
- FD `CookingPotRecipe.matches` (decompiled): counts non-empty slots 0-5, requires that count == the number of
  ingredients (6), then `RecipeMatcher` pairs one stack per ingredient. **Two berries stacked in one slot = 5
  inputs = no match.** This is the most likely "not cookable".
- The oven version (`baking.js` lines 36-39, `MUFFIN` settings line 32) has the same 2-berries-in-2-slots shape with
  `ovenIngredientCounts` off. It needs the muffin tin in the tool slot, and the result waits for paper (one per
  muffin). Both are easy to mistake for "doesn't cook".

### Build
1. In game (OV-13): open JEI on `fruitsdelight:blueberry_muffin`, find the Cooking Pot recipe
   (`creativeengineering:farmersdelightcooking/blueberry_muffin`), and press [+] next to a pot holding the
   ingredients. If it cooks, the earlier failure was the slot rule: close the row (Bug -> Complete), no code.
2. If [+] can't fill it or it still won't cook: hold each ingredient and run `/kubejs hand` to list its tags.
   Check the six against the recipe above (JEI shows the recipe id with advanced tooltips). Then also look for a second
   pot recipe with the same inputs that wins first (`getRecipeFor` returns the first match).
3. Optional polish if players keep hitting the slot rule: a line on the pot ("one ingredient per slot") in
   `tooltips.js`. Not recommended: it is FD's standard behaviour and JEI shows it.

### Ids and data
`fruitsdelight:blueberry_muffin` (`i.blueberry_muffin`), `fruitsdelight:cranberry_muffin` (`i.cranberry_muffin`),
`fruitsdelight:blueberry` (`i.blueberry`), tags `i.t_fruits_blueberry`, `i.t_fruits_cranberry`, `i.any_dough`,
`i.t_milk_bottle` (`#forge:milk/milk_bottle`, bottles only), `i.eggs`, `i.any_sugar`. All verified against the jar
or item_dict.js.

### Decisions
None unless step 2 finds a real cause.

### Gotchas
- The `farmersdelight:` namespace on Fruits Delight's recipe ids is real (the file sits under `data/farmersdelight`),
  so the `R.remove` lines do hit them.

### Tests
OV-13 as written in oven.md 7, plus: "two berries in one slot does not cook; split into two slots and it does".

---

## oven #5: Muffin yield 4 from baking
Status Planned / Work KubeJS / TODO `cooking/oven #5`

### Goal
An oven muffin bake gives 4 muffins (the pot keeps 2), so the oven is the efficient route.

### Now
`baking.js` lines 36-39: `R.ceBaking([n(i.t_fruits_blueberry, 2), i.flour, i.t_milk_bottle, i.eggs, i.any_sugar],
Item.of(i.blueberry_muffin, 2), e, 'blueberry_muffin', MUFFIN)`, same for cranberry. `MUFFIN` (line 32) = muffin tin
tool, paper plating, 200 ticks, 0.1 xp. The recipe id `creativeengineering:baking/blueberry_muffin` stays the same.

### Build
1. `Item.of(i.blueberry_muffin, 2)` -> `4`, `Item.of(i.cranberry_muffin, 2)` -> `4`. Update the comment above them
   (line 35: "2 muffins" mirror note -> "4 in the oven, 2 in the pot").
2. oven.md 2.6 "Muffins (... 2 per bake)" -> 4; OV-07 says "one paper per muffin", which now means 4 paper a bake.
3. `/reload`.

### Ids and data
As #3. Paper `i.paper` (`minecraft:paper`).

### Decisions
- Cost of 4 paper per bake: fine (paper is cheap), **keep paper plating per muffin**.
- Also raise ingredients (e.g. 3 berries)? **No**: the row asks for a better route.

### Gotchas
- The waiting result slot (7) takes up to 64, so `hasRoomForResult` (OvenBlockEntity line 189) needs no change.
- Food values: `food/types-and-tuning #1` may retune muffins; `baking/baked-goods #2` (muffin variants) should copy
  the 4 yield for new oven muffins.

### Tests
**OV-20 Muffin yield**: one oven bake of blueberry muffins gives 4 (with 4 paper); the pot still gives 2.

---

## oven #6: Baking audit
Status Planned / Work KubeJS / TODO `cooking/oven #6`

### Goal
Every baked good has an oven recipe that uses flour (not wheat), and no wheat crafting route is left.

### Now
- Known gaps (scratchpad 2026-10-21, checked against the jars):
  - `neapolitan:banana_bread` (`i.banana_bread`): only `neapolitan:banana/banana_bread`, shapeless wheat +
    `#forge:fruits/banana` + sugar. No oven recipe.
  - `createaddition:honey_cake` (`i.honey_cake`): only `createaddition:filling/honey_cake` (Create filling:
    `createaddition:cake_base_baked` + 500 mB `#forge:honey`). No oven recipe.
- Candidates for the full audit: the `baked` food type (12 items in `kubejs/generation_scripts/food/food_types.json`:
  adzuki_bun, banana_bread, bread_slice, cornbread, honey_bread, minecraft_bread, mulberry_bread,
  nether_wart_sourdough, sourdough_bread, strawberry_scones, toast, tortilla) plus the baked members of `sweet`
  (muffins, cookies, tarts, pies, cakes, gingerbread, fruit beignet, waffles...). Tag
  `#creativeengineering:food_type/baked` (`kubejs/server_scripts/tags/foodtypetags.js`).
- The oven covers muffins, cookies, tarts, pies, cakes (38 recipes, oven.md 2.6); no breads yet.

### Build
1. Known two, in `baking.js`:
   - `R.remove({ id: 'neapolitan:banana/banana_bread' }, e)` (only if the "no wheat route" decision says so; else
     swap wheat for flour with `R.replaceInput` or a new `R.shapeless` with `i.flour`).
   - `R.ceBaking([i.flour, i.t_fruits_banana, i.any_sugar], Item.of(i.banana_bread, 1), e, 'banana_bread', SHEET)`.
     Check `i.t_fruits_banana` is in item_dict.js (the banana_cake recipe uses it, line 98).
   - `R.ceBaking([n(i.honey_bottle, 2), i.cake_base_baked], Item.of(i.honey_cake, 1), e, 'honey_cake', CAKE)`. 500 mB
     is two 250 mB bottles. The two glass bottles go to the oven's leftover slots. Or use the raw `i.cake_base`
     to match the other oven cakes (they all start from the raw base).
2. Audit: for each item in the `baked` type and the baked members of `sweet`, list its recipes (`/dumpall` crafting
   audit, or JEI) and mark: oven recipe yes / no, wheat in a crafting route yes / no. Put the table in oven.md 2.6
   ("Audit 2026-10-xx"). Then add the missing `R.ceBaking` lines grouped by bakeware.
3. Breads need a bakeware: none of the four fits a loaf.

### Ids and data
`neapolitan:banana_bread`, `createaddition:honey_cake`, `createaddition:cake_base` / `cake_base_baked` (`i.cake_base`,
`i.cake_base_baked`), `minecraft:honey_bottle` (`i.honey_bottle`), `#forge:flour/wheat` (`i.flour`). Recipe type
`creativeengineering:baking` via `NCH.ceBaking`.

### Decisions
- "No leftover crafting route" (shared with `baking/baked-goods #1`): **read as "no wheat route"**, keep a dearer
  flour hand recipe, ask the user before removing hand crafting.
- Bakeware for breads: **baking sheet for now**; a loaf pan (hammered from the cake pan, `R.hammering`) if the user
  wants one more step.
- Honey cake from the raw or the baked base: **raw base**, like every other oven cake.

### Gotchas
- `ovenIngredientCounts` off: a counted ingredient becomes n slots. Keep each new recipe within 6 slots or check
  `ceBaking`'s drop log (oven.md 2.6.1).
- Changing a crafting recipe changes the can parity sheet's "craft sum" for canned breads (`canned_banana_bread`
  etc.): note it in [can_parity.md](../can_parity.md).

### Tests
**OV-21 Banana bread / honey cake bake** with their bakeware. **OV-22 Audit**: each baked item in the audit table has
a Baking page in JEI, and no recipe in JEI takes plain wheat for it.

---

## oven #4: Oven tooltips for other ovens
Status Planned / Work KubeJS / TODO `cooking/oven #4`

### Goal
Each oven-like block says what it does: works like a smoker, works like a furnace (and smoker), or decorative.

### Now
- Fantasy Furniture 6.7.0: `fantasyfurniture:royal/oven`, `nordic/oven`, `venthyr/oven`, `necrolord/oven`,
  `bone/skeleton/oven`, `bone/wither/oven`, `dunmer/oven` ("Dunmer Cooking Spit"). Their `OvenBlockEntity` runs
  `RecipeType.SMOKING` with normal fuel (`ForgeHooks.getBurnTime`). **Working smokers**, verified.
- Farmhouse Decorations: `farmhouse_decorations:antique_oven`, `kitchen_antique_oven`, `kitchen_antique_oven_decorated`.
  Block classes only, no block entity: **decorative**.
- Cooking for Blockheads 13.3.4 `cookingforblockheads:oven`: `getSmeltingResult` uses CfB's registry, else
  `RecipeType.SMELTING`, and keeps only food results (or CfB's non-food list). No smoking. Fuel is cooking oil only
  with the pack config (kitchen #8).
- None of these has an item_dict key. `tooltips.js` has `addTooltips(e, [id, [text, col.x]])` and
  `addTooltipPattern(e, 'ns:*glob', ...)` (lines 15-53). Roles: `col.norm` gray, `col.uncm` green (line 122 on).

### Build
1. item_dict.js: add keys in the mods' blocks (file style `i.<name> = 'ns:id'`): `i.cfb_oven =
   'cookingforblockheads:oven'`, `i.antique_oven`, `i.kitchen_antique_oven`, `i.kitchen_antique_oven_decorated`.
   For Fantasy Furniture use the pattern helper (seven ids, one glob).
2. `tooltips.js`, kitchen section (next to the heating cask lines, ~496-504):
   - `addTooltipPattern(e, 'fantasyfurniture:*oven', ['Works like a smoker', col.norm])`
   - `addTooltips(e, [i.antique_oven, ['Decorative', col.norm]], [i.kitchen_antique_oven, ...], [i.kitchen_antique_oven_decorated, ...])`
   - `addTooltips(e, [i.cfb_oven, ['Cooks food like a furnace and a smoker', col.norm], ['Burns cooking oil', col.uncm]])`
     (the second line per kitchen #8's fuel decision).
   - The pack's own oven: optional `[i.oven, ['Bakes with bakeware; needs fire or magma below', col.norm]]`.
3. CfB oven smoking (patcherMod): new mixin `mixin/CfbOvenSmokingMixin` on
   `net.blay09.mods.cookingforblockheads.tile.OvenBlockEntity`, `@Inject(method = "getSmeltingResult", at =
   @At("RETURN"), cancellable = true, remap = false)`: if the result is empty, query `RecipeType.SMOKING` with the
   `@Shadow` field `singleSlotRecipeWrapper` (decompiled name; it is set to the stack just before) and return its
   result if it is food. The insert filter (`OvenBlockEntity$1`) calls the same method, so smoker-only food is
   accepted too. Register in `creativeengineering.mixins.json` (no guard for a missing CfB; the pack always has it).

### Ids and data
Ids above (jar-verified). The CfB method and field names are from the decompiled 13.3.4 jar; the vanilla calls in it
are SRG (`m_44015_` getRecipeFor), which the mixin uses as normal Mojang calls.

### Decisions
- Fantasy Furniture text: **"Works like a smoker"**, not "Decorative" (the row's word; the jar says they work).
- CfB oven: **add the smoking fallback** (row: "make sure it both smelts and smokes").

### Gotchas
- Glob `fantasyfurniture:*oven` also matches any future id ending in `oven`; the console log line prints the count
  (expect 7).
- The CfB smoking fallback makes smoker-only foods cookable there, not furnace-only non-food items (CfB filters to
  food).

### Tests
**OV-23 Tooltips** on all three kinds. **OV-24 CfB oven smokes**: a smoker-only food (find one with JEI, e.g. Corn
Delight `popcorn_from_smoking`'s input if it has no smelting twin) cooks in the CfB oven.

---

## oven #2: Oven and bakeware crafting patterns
Status In Progress / Work KubeJS / TODO `cooking/oven #2`

### Goal
A final crafting pattern for the oven that clashes with no other recipe.

### Now
- Oven: `baking.js` line 121, `R.shaped(i.oven, ['PPP', 'B B', 'BBB'], { P: i.any_plate_iron, B: i.bricks }, e,
  'oven')`, id `creativeengineering:shaped/oven`. Placeholder (oven.md 2.1, 4).
- Bakeware: not shaped any more. Hammered chain `R.hammering` lines 125-128 (plate -> baking sheet -> muffin tin ->
  pie tin -> cake pan), oven.md 2.5. The row's "bakeware patterns" part is done.
- No clash tool in `tools/`.

### Build
1. Clash check: in game, place the pattern with each `#forge:plates/iron` member and plain bricks in a crafting
   table; only the oven may come out. Also look in JEI under "uses" of iron plate for shaped 3x3 brick patterns.
   (FD's stove is `III / B B / BCB` with ingots and a campfire; close, but different.)
2. Final pattern (decision), then change line 121 only (same id, so advancements and JEI stay).
3. oven.md 2.1 "Crafting" row and 4 ("placeholder") updated.

### Ids and data
`creativeengineering:oven` (`i.oven`), `#forge:plates/iron` (`i.any_plate_iron`), `minecraft:bricks` (`i.bricks`),
FD stove `farmersdelight:stove`.

### Decisions
- Pattern: **`PPP / BFB / BBB` with F = `minecraft:furnace`** (reads as an oven, no clash with the stove's campfire),
  or keep the current one if the clash check is clean. User's call.

### Gotchas
- `i.any_plate_iron` is a tag: test every member (IE, Create plates).

### Tests
**OV-03** (oven.md 7) with the final pattern.

---

## Open side item (no row)

Brewin's `heating_cask` (`i.heating_cask`, in the pack since the 4.5.0 port) is not in
`#creativeengineering:oven_heat_sources` (`kubejs/server_scripts/tags/blocktags.js` lines 24-26). It heats a pot like
a stove (tooltips.js), so the oven's "non-stove" rule suggests leaving it out. Worth a one-line decision in oven.md 4;
no TODO row exists.

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| oven #1 | Oven block, baking type, JEI, bakeware, baking recipes, counts option, hammered bakeware, hammer no-rotate | Testing | The row says "37 baking recipes"; baking.js has 38 (31 `R.ceBaking` lines + 7 from `FLAVOURED_CAKES`), as oven.md says. Leftover-slot sync bug: kitchen #14. OV-01 to OV-19 not run |

## Writeup corrections made

- [oven.md](../oven.md) 0: "(placeholder pattern, 3.6)" -> "2.1" (the pattern is in 2.1; 3.6 is about FD changes).
- [oven.md](../oven.md) 4: "Crafting patterns for the oven and bakeware are placeholders" -> only the oven's is (bakeware
  is hammered, 2.5), linking this brief.
- [oven.md](../oven.md) 4: the heating cask "the pack's Brewin 2.0 jar doesn't have that block" -> the port has it;
  adding it is open.
