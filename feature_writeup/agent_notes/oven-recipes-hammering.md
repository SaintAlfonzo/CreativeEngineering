# oven-recipes-hammering

> Oven baking recipes mirror every muffin/cookie/cake/pie/tart recipe with ingredient counts; bakeware hammered; IE hammer rotates blocks before their use()

Moved from agent memory 2026-11-16 (last edited there 2026-10-19). Repo status docs win over any status line here.

2026-10-18: `kubejs/server_scripts/cooking/baking.js` holds 37 `creativeengineering:baking` recipes (started as 46 mirrors of every live
muffin, cookie, tart, pie and cake recipe, pot + crafting), originals kept. Then the cooking plan's baked changes on top: flour for wheat/dough, crust in
every pie, Create C&A cake base in every cake (cakes merged one per cake), no shepherd's pie (user). Cake base is
also made in the oven (egg, 2 sugar, 2 flour; 2026-10-19).
Cornmeal item `creativeengineering:cornmeal` (cooking.md spec, recipes_cooking.md 2.1): pies, cake base, pancakes and
the new pie crust (3 flour + milk -> 2) take `#creativeengineering:flour_or_cornmeal`. User rejected egg in pie crust
because shapeless 3 flour + egg -> 6 dough matches the same grid; check shapeless overlaps before adding shaped crafts. Tools by kind: baking sheet (cookies,
tarts), pie tin (pies), cake pan (cakes), muffin tin (muffins, paper plating). Dummy recipes removed.
Details: feature_writeup/oven.md 2.5-2.6 (memory `feature-writeup-pattern`).

- `BakingRecipe` ingredients take an optional `"count"` (one slot gives up n), but only with CEConfig
  `[cooking] ovenIngredientCounts` on; the user wants it **off by default**. Off: `NCH.ceBaking` expands counts to
  one slot per item and, over 6 slots, drops from the largest repeated group (ties: first listed, so scripts list
  filler first). 9 recipes are trimmed 9->6; each case + savings is in oven.md 2.6.1 (user asked for that list).
  Scripts always write the full original cost (`Item.of(id, n)` / `{ id: '#tag', count: n }`).
- Bakeware chain via `R.hammering` (FD cutting with IE hammer + shapeless hammer craft):
  plate -> baking_sheet -> muffin_tin -> pie_tin -> cake_pan (reordered 10-19). Ids kept pie_tin/muffin_tin although the user said "pan".
- **Gotcha:** the IE Engineer's Hammer's `onItemUseFirst` rotates any block without an IE BE (RotationUtil.rotateBlock)
  before the block's `use()`, so it turned the FD cutting board instead of cutting. Fixed by
  `IECompat.registerHammerNoRotate` (IE rotation blacklist, block tag `#creativeengineering:hammer_no_rotate`).
  Any future "use the IE hammer on block X" recipe needs X in that tag.

**Why:** costs and the hammer fix aren't visible from the recipe list alone.
**How to apply:** new baked goods go in baking.js with the matching bakeware at full cost; if one gets trimmed, add it to oven.md 2.6.1.
Related: [patchermod-coremod](patchermod-coremod.md), memory `centralize-recipes-tags-items`.
