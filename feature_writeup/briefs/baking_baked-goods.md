# baking / baked-goods: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# baking`, Sub `baked-goods` (rows 1 to 11). Feature writeups:
[oven.md](../oven.md) (test prefix `OV-`, the oven and its 38 baking recipes), [recipes_cooking.md](../recipes_cooking.md)
(`RC-`, flour / dough / bread / sourdough / pie crust), [placeable_food.md](../placeable_food.md) (`PL-`),
[wthit_lines.md](../wthit_lines.md) 2.3 (`WL-`, jar contents). The "cooking plan" the rows cite is the removed
`feature_writeup/cooking.md`; its text is in git (`git show 2e8687f^:feature_writeup/cooking.md`, lines 54-66 and 157-201).

Mod facts marked **(jar)** were read from the pack's jars (versions from `pack/manifest.json`); **(unverified)** means
check in game. Jars read: FarmersDelight 1.2.4, Fruits Delight 0.5.11, Create 0.5.1.i, Create Central Kitchen 1.3.11.c
(CCK), Create Crafts & Additions 1.2.3 (CA), Create Factory 0.0.2, Neapolitan 4.1.0, Abnormals Delight 4.1.2, Delightful
3.4.1, Ender's Delight 1.2.2, Farmer's Respite 2.0, Quark 418, Upgrade Aquatic 5.1.0, untitledduckmod 0.6.1, Architect's
Palette 1.3.5, PneumaticCraft 4.3.15, More Delight 25.07.28, Supplementaries 2.4.20, Rustic Delight 1.3.0, Display Delight
1.2.0, KubeJS 1902.6.2-build.73.

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #1 | Flour, crust and cake base in every baked recipe | In Progress | medium, KubeJS (one decision blocks it, see below) |
| #3 | Cookie recipes (bent, heated compacting, efficient oven) | Done 2026-10-22 (superseded by the baking overhaul, scratch server PASSED) | small, KubeJS |
| #10 | Bread block (knife to crustless, 3 -> 6 slab) | Planned | small, KubeJS |
| #4 | Pie sequenced assembly, chorus pie through chorus juice | Planned | medium, KubeJS; the chorus half needs `food/drinks #4` |
| #5 | Cakes (dragon egg cake, rename, dead slices, placeable slices, chocolate cake merge) | Planned | large: KubeJS + assets, flight needs patcherMod |
| #6 | Cake paste in the pot, waffles, more tarts | Planned | medium, KubeJS + textures |
| #2 | Muffins as a real food (variants, paper, assembly) | Planned | medium, KubeJS + textures |
| #9 | Bread slices / toast / buns in sandwiches | Planned | medium, KubeJS (an audit, table below) |
| #8 | Yeast (new recipes, in-world fix) | Planned | small KubeJS + an in-game test first |
| #7 | Sourdough chain with timed waits | Planned | large, KubeJS (patcherMod only if the KubeJS route is rejected) |
| #11 | Mixed cookie jars, more jarable foods | Planned | tags: small KubeJS; mixed jars: patcherMod mixins |

No Testing / Complete rows in this subfamily.

**The blocking decision (RESOLVED 2026-10-22).** The user chose the baking overhaul ([oven.md](../oven.md) 2.6): every cake,
pie and cookie recipe is removed and rebuilt on shared bases, cookies are oven only, cakes and pie fillings keep a
hand-craft route, and the oven is the efficient one. It supersedes "originals stay" (oven.md 4). Rows 1, 3 and
`cooking/oven #6` are built (BUILT, not tested); the remaining open parts of row 1 are in TODO.

Build order:

1. **Batch A, one `/reload`:** #1 steps 1-2 (wheat -> flour, crusts), #3, #10, and #11's tag half. All in server scripts.
   Put the new code in a new `kubejs/server_scripts/cooking/baked_goods.js` (cooking.js is already 852 lines, half of it
   a commented-out copy) and leave `baking.js` for oven recipes.
2. **Batch B, restart:** #4 (non-chorus pies) and #1 step 3 (cake base everywhere, cake assembly). New incomplete items
   in `transition_items.js` need a restart.
3. **Batch C:** #5 slice removal, rename, chocolate cake merge, coaster models (with `food/drinks #3`, which also lists the
   chocolate cake merge). Restart (removed_items.js, assets).
4. **Batch D, restart + textures:** #2, #6 tarts, #5 dragon egg cake (new KubeJS items). The flight effect, if wanted,
   rides the next patcherMod release.
5. **Batch E:** #8, then #7 (the chain uses yeast).
6. **Later:** #11 mixed jars (patcherMod mixins), #9 (with `dishes/meals #2`, which decides burger bread).

Cross-family links: `cooking/oven #3` (muffins not cookable), `#5` (muffin oven yield 4), `#6` (baking audit: banana bread,
honey cake); `dishes/ingredients #1` (cornmeal as flour), `#2` (egg solution and frosting fluids), `#7` (batter);
`food/drinks #3` (chocolate cake merge), `#4` (chorus juice fluid); `dishes/meals #2` (burgers "bread or cornbread only"),
`#3` (sequenced meals), `#5` (feasts); `cans/canning #15` (canned cake), `food/types-and-tuning #1` (food values),
`#5` (placed pies and SoL); `food/placeable #2` (`/dumpplaceable`); `cooking/kitchen #10` (mortar and pestle);
`ui/wthit #3` (jar Contents line).

---

## baked-goods #1: Flour, crust and cake base in every baked recipe
Status In Progress / Work KubeJS / TODO `baking/baked-goods #1` (cooking plan sweet/baked, [oven.md](../oven.md) 2.6)

### Goal
Every crafting and pot recipe for a cookie, pie, cake or the strange eclair takes flour (or cornmeal) instead of wheat,
every pie sits on a pie crust, and every cake starts from a cake base. The oven half is done (1.0.8).

### Now
- Oven recipes: [baking.js](../../kubejs/server_scripts/cooking/baking.js) 41-117 already use `i.flour`
  (`#forge:flour/wheat`), `i.t_flour_or_cornmeal`, `i.pie_crust` and `i.cake_base` (oven.md 2.6).
- Pie crust from flour is built (1.0.8, [recipes_cooking.md](../recipes_cooking.md) 2.1):
  [cooking.js](../../kubejs/server_scripts/cooking/cooking.js) 347 `R.shaped(2 crust, ['FMF',' F '], F flour-or-cornmeal,
  M milk)` id `creativeengineering:shaped/pie_crust_flour`; 348-349 unheated compacting, 3 flour + 250 mB milk -> 2.
  FD's `farmersdelight:pie_crust` (`wMw / w`, wheat) and FD's `farmersdelight:integration/create/mixing/pie_crust_from_mixing`
  (3 `create:wheat_flour` + 250 mB milk) stay **(jar)**. An egg crust was asked first and dropped: the shapeless
  `3 flour + egg -> 6 dough` (cooking.js 92) matches the same grid.
- No crafting recipe has had wheat swapped yet. Live wheat recipes **(jar)**:
  - cookies: `minecraft:cookie` (Quark's copy, `#X#`), `quark:tweaks/crafting/utility/bent/cookie` (`X#`/`# `, Quark
    "Bent Recipes" = true in `config/quark-common.toml` 613), `farmersdelight:honey_cookie`, `farmersdelight:sweet_berry_cookie`,
    `farmersrespite:green_tea_cookie`, `abnormals_delight:upgrade_aquatic/mulberry_cookie`, `endersdelight:uncanny_cookies`,
    `fruitsdelight:lemon_cookie`, `cranberry_cookie`, `persimmon_cookie`, `bayberry_cookie`. CCK's compacting cookies
    (`create_central_kitchen:compacting/cookie`, `.../green_tea_cookie`, `.../mulberry_cookie`) already take flour.
  - pies: `farmersdelight:apple_pie`, `endersdelight:chorus_pie`, `delightful:food/salmonberry_pie`,
    `delightful:food/pumpkin_pie` (result `minecraft:pumpkin_pie`), `create_central_kitchen:crafting/mulberry_pie`
    (CCK built-in datapack `farmersdelight`, loaded with pie overhaul on: `enablePieOverhaul = true` in
    `config/create_central_kitchen-common.toml`).
  - eclair: `endersdelight:strange_eclair` (shapeless ender eye, 2 wheat, sugar).
- Crustless crafting pies **(jar)**: `upgrade_aquatic:mulberry_pie` (mulberry, sugar, egg; CCK's datapack carries a copy
  under the same id), `untitledduckmod:crafting_shaped/pumpkin_pie` (pumpkin, sugar, duck egg), and whatever
  `minecraft:pumpkin_pie` resolves to (vanilla, Delightful and CCK all ship a conditional file at that id; which one
  wins is load order, **(unverified)**: look it up in JEI).
- Cakes still built from egg + sugar + wheat/dough **(jar)**: `farmersdelight:cake_from_milk_bottle`, `neapolitan:cake`,
  `create:crafting/curiosities/cake`, `untitledduckmod:crafting_shaped/duck_cake` and `goose_cake`, the six Neapolitan
  flavoured cakes (`neapolitan:adzuki/adzuki_cake` ...), CCK's seven `crafting/*_cake_from_dough` and eight compacting /
  mixing cakes, `farmersrespite:coffee_cake`, `farmersdelight:cooking/mangosteen_cake` (Fruits Delight pot, 2 wheat 2 sugar
  2 mangosteen). CA's three filling cakes already start from a baked cake base. `minecraft:cake` (vanilla) is removed
  (cooking.js 357).
- Cake base sources: `createaddition:compacting/cake_base` (egg, 2 sugar, `create:dough`), oven `baking/cake_base`
  (egg, 2 sugar, 2 flour-or-cornmeal); baked by `createaddition:smoking/cake_base_baked` or oven `baking/cake_base_baked`.

### Build
1. **Wheat -> flour** (new `cooking/baked_goods.js`, `ServerEvents.recipes`):
   ```js
   var COOKIE_WHEAT = ['minecraft:cookie', 'quark:tweaks/crafting/utility/bent/cookie', 'farmersdelight:honey_cookie',
     'farmersdelight:sweet_berry_cookie', 'farmersrespite:green_tea_cookie', 'abnormals_delight:upgrade_aquatic/mulberry_cookie',
     'endersdelight:uncanny_cookies', 'fruitsdelight:lemon_cookie', 'fruitsdelight:cranberry_cookie',
     'fruitsdelight:persimmon_cookie', 'fruitsdelight:bayberry_cookie', 'endersdelight:strange_eclair']
   COOKIE_WHEAT.forEach(id => R.replaceInput({ id: id }, i.wheat, i.flour, e))
   var PIE_WHEAT = ['farmersdelight:apple_pie', 'endersdelight:chorus_pie', 'delightful:food/salmonberry_pie',
     'delightful:food/pumpkin_pie', 'create_central_kitchen:crafting/mulberry_pie']
   PIE_WHEAT.forEach(id => R.replaceInput({ id: id }, i.wheat, i.t_flour_or_cornmeal, e))
   ```
   Cookies take wheat flour only and pies take cornmeal too, as in the oven (oven.md OV-19). If row 3 replaces the
   cookie recipes outright, drop the cookie ids here.
2. **A crust in every pie.** `R.remove` the three crustless recipes above and re-add them through `R.shapeless` with
   `i.pie_crust` added, ids `mulberry_pie_simple`, `pumpkin_pie_duck_egg`, `pumpkin_pie_simple` (the same names the oven
   uses). Crust "pressing dough" from the plan: `R.createPressing(i.pie_crust, i.any_dough, e, 'pie_crust_pressing')`
   (Create press, 1 dough -> 1 crust; 1 dough = 1/2 flour by the `3 flour + egg -> 6` craft, so the press is the
   cheapest crust, fine for automation). Check that `#forge:dough` doesn't already press into something else (CA's
   rolling uses dough? **(unverified)**; `create_factory:uncooked_waffle` presses `cake_paste`, not dough **(jar)**).
3. **Cake base everywhere.**
   - Remove the egg+sugar+wheat crafting cakes listed in Now (keep `createaddition:filling/*`), and add one shaped
     recipe per cake: plain cake `['MMM', ' B ']` (3 milk, raw cake base), flavoured `['MDM', ' B ', ' D ']` (2 milk,
     2 flavour, base), the same counts as the oven recipes (baking.js 96-105), ids `<cake>_cake_base`. The flavour list
     is `FLAVOURED_CAKES` in baking.js 97-102; move it to `global` (e.g. `global.bakedGoods.flavouredCakes` in a startup
     file) so the oven and crafting recipes share one list.
   - Replace CCK's compacting/mixing cakes with `R.createCompacting(cake, [i.cake_base, flavour, flavour,
     Fluid.of(f.milk, 1000)], e, '<cake>_compacting')`.
   - Sequenced assembly with spouting (the plan's "cake base recipe for all cakes, sequenced assembly w/ spouting"):
     input `i.cake_base_baked` (CA's model: the base is baked before it is filled), steps deploy flavour, deploy flavour,
     fill 500 mB milk, loops 1, through `R.createSequencedAssembly(..., ['<cake>'], e, '<cake>_sequenced_assembly', 1)`.
     One `transition_items.js` row per cake (auto texture works: `neapolitan:item/<cake>` etc.).
   - Mangosteen "cake" (pot): `[i.cake_base, mangosteen x2]` with bowl, as the oven version; or leave it if row 5 renames it
     to something that isn't a cake (recommended there).
   - "Slice a dough block into bases": `R.fdCutting(i.dough_block, i.any_knife, Item.of(i.cake_base, 3), e,
     'cake_base_from_dough_block')`. A dough block is 9 dough; a base is egg + 2 sugar + 1 dough, so 3 bases per block
     prices the missing egg and sugar at 2 dough each. See Decisions.

### Ids and data
`i.flour` `#forge:flour/wheat`, `i.t_flour_or_cornmeal` `#creativeengineering:flour_or_cornmeal`, `i.pie_crust`
`farmersdelight:pie_crust`, `i.cake_base` / `i.cake_base_baked` (`createaddition:*`), `i.dough_block`
`create_compressed:dough_block`, `i.any_dough` `#forge:dough`, `i.duck_egg` `untitledduckmod:duck_egg` (13280). All
recipe ids above **(jar)**; the winning `minecraft:pumpkin_pie` **(unverified)**.

### Decisions
- **Crafting routes stay?** See "The blocking decision" at the top. Recommendation: stay, priced above the oven.
- **Swap milk to egg in the crust** (TODO decision). The plan's reason was "allows using frosting" (frosting = milk +
  sugar, `dishes/ingredients #2`). An egg crust with 3 flour clashes with the dough craft. Recommendation: no swap now; if
  wanted, an extra egg crust with a shape the dough craft can't match, 4 flour + egg `['F F', 'FEF'] -> 3`. Revisit with
  `dishes/ingredients #2`.
- **Dough block -> how many cake bases.** Recommendation 3 (above). 9 would make bases free of egg and sugar.

### Gotchas
- `R.replaceInput` by recipe id only touches that recipe; conditional recipes (`forge:conditional`) keep their file id
  (`delightful:food/salmonberry_pie`), so the ids above are right.
- Fruits Delight's `lemon_cookie` is shaped `' C '/'ABA'` with a milk bottle; replacing wheat keeps the shape.
- Rustic Delight 1.3.0 ships `cherry_blossom_cookie` / `cherry_blossom_cheesecake` recipes, but `ModItems` registers
  no cherry blossom items in this 1.19.2 build **(jar)**, so those recipes don't load. Nothing to do.
- Slice & Dice turns pot recipes into heated mixing after KubeJS runs; a new pot recipe gets a mixer twin unless its id
  ends in `_manual_only` (cooking.js 388-389 note).

### Tests (RC-)
- RC-19 Wheat gone: JEI uses of `minecraft:wheat` show no cookie, pie or strange eclair recipe; each listed recipe shows
  Create wheat flour (pies: flour or cornmeal).
- RC-20 Crusts: the UA mulberry pie, duck-egg pumpkin pie and `minecraft:pumpkin_pie` crafts need a pie crust; a dough
  in a Create press gives a crust.
- RC-21 Cake base: every cake's crafting, compacting and assembly recipe takes a cake base; no cake recipe takes wheat,
  dough or flour directly; a dough block on a cutting board gives 3 bases.

---

## baked-goods #3: Cookie recipes
Status Done 2026-10-22 (superseded by the baking overhaul: cookies are oven only, cookie dough + 2 flavour -> 12, [oven.md](../oven.md) 2.6, OV-30) / Work KubeJS / TODO `baking/baked-goods #3` (cooking plan baked/cookies)

### Goal
Each cookie gets a 2x2 "bent" craft, a heated compacting recipe for automation, and a more efficient oven recipe.

### Now
- Crafting recipes as listed in #1 (3-wide `F X F` or Fruits Delight's `ABA`; FD/Ender's/Abnormals shapeless). Only the
  vanilla cookie has Quark's bent version.
- Compacting: CCK `compacting/cookie` (cocoa + 2 flour -> 8, **unheated**), `[dp farmersrespite] compacting/green_tea_cookie`,
  `[dp abnormals_delight] compacting/mulberry_cookie` **(jar)**.
- Oven: 10 cookie recipes, 2 flour + flavour -> 8, baking sheet (baking.js 43-52).
- Dead cookies: `abnormals_delight:cherry_cookie` and `maple_cookie` are registered but their only recipes are conditional
  on Environmental / Autumnity, which aren't in the pack **(jar + manifest)**. The pack still ships
  `creativeengineering:canned_cherry_cookie` / `canned_maple_cookie` (can_parity 2.x rows, `food_types.json` 83, 199),
  which can't be made.

### Build
1. One table in `baked_goods.js`: `[key, flavour]` per cookie (cookie/cocoa, honey_cookie/honey bottle,
   sweet_berry_cookie/sweet berries, green_tea_cookie/green tea leaves, bayberry, cranberry, persimmon (dried),
   lemon (+ milk bottle), mulberry, uncanny_cookies/mite crust), the same pairs as baking.js 43-52. Move that list to
   a shared global (startup) so baking.js reads it too.
2. Bent craft per cookie: `R.shaped(Item.of(cookie, 8), ['XF', 'F '], { X: flavour, F: i.flour }, e, key + '_bent')`.
   Lemon cookie has 4 ingredients: `['XF', 'FM']`. Remove `quark:tweaks/crafting/utility/bent/cookie` (it would become a
   duplicate after #1's swap) or leave the vanilla one to Quark and skip it in the table.
3. Heated compacting: `R.createCompacting(Item.of(cookie, 8), [i.flour, i.flour, flavour], e, key + '_compacting',
   'heated')`; remove the three CCK unheated ones (`R.remove({ id: 'create_central_kitchen:compacting/cookie' })`, and the
   two `[dp]` ids, same id form).
4. Efficient oven: change the 10 `ceBaking` results in baking.js 43-52 from 8 to 12 (see Decisions).
5. Dead cookies: give the cherry cookie a recipe from `vinery:cherry` (`i.cherry`, item_dict 11126; check whether it is
   in `#forge:fruits/cherry`) and add it to the table; put `maple_cookie` (no syrup in the pack) in
   `global.removedItems` and tell `cans/canning` its can is dead.

### Ids and data
Cookie ids: item_dict 2147 (`i.cookie`), 11533, 11586, 12823, 12229, 12241, 12306, 12272, 12123, 11960; Abnormals
`i.cherry_cookie` 12110, `i.maple_cookie` 12120. Flavours as baking.js. Recipe ids `creativeengineering:shaped/<key>_bent`,
`creativeengineering:createcompacting/<key>_compacting`.

### Decisions
- **Yields.** Recommendation: grid 8 (as today), heated compacting 8, oven 12. The oven is the "efficient baking recipe".
- **Keep the 3-wide crafts?** Recommendation: keep (after #1's flour swap); the bent one is the convenience.

### Gotchas
- A 2x2 shaped recipe also matches in the 3x3 grid; `['XF','F ']` vs the 3-wide `FXF` don't collide.
- `#forge:flour/wheat` vs cornmeal: cookies stay wheat flour only (OV-19).

### Tests (RC-)
- RC-22 Bent cookies: each cookie crafts in the 2x2 inventory grid from flour + flavour.
- RC-23 Cookie compacting: a heated basin under a press makes 8 of each cookie; an unheated one makes none.
- OV-20 Oven cookies: each oven cookie recipe gives 12.

---

## baked-goods #10: Bread block
Status Planned / Work KubeJS / TODO `baking/baked-goods #10` (cooking plan misc)

### Goal
The crustless bread block comes from cutting a bread block with a knife, and the bread slab has the standard 3 -> 6 craft.

### Now
[cooking.js](../../kubejs/server_scripts/cooking/cooking.js) 143-152: removes Architect's Palette's `bread_block`
(9 hay -> 9 bread blocks), `bread_slab` (3 -> 6) and `stonecutting/bread_nub`; dough block smoked to a bread block;
saw / knife bread block -> 16 bread; stonecutter bread block -> slab, -> crustless block; slab -> nub. AP's bread slab
model has crusty sides and crumb top/bottom **(jar)**.

### Build
1. `R.fdCutting(i.bread_block, i.any_knife, i.crustless_bread_block, e, 'crustless_bread_block')`. The knife recipe on the
   same input already gives 16 bread (line 149): FD's cutting board picks the first matching recipe, so two knife
   recipes on one input can't coexist. Move "bread block -> 16 bread" to the saw only (line 148 stays) and the
   crustless block to the knife. The cut could also give a crust by-product (e.g. 4 bread, as the trimmed crust), but no
   crumb item exists; recommend the crustless block alone.
2. Keep the stonecutter crustless recipe (line 151) or drop it (recommend drop: the knife is the "cut").
3. `R.shaped(Item.of(i.bread_slab, 6), ['BBB'], { B: i.bread_block }, e, 'bread_slab')` (AP's own shape, which line 145
   removed). Keep the stonecutter slab.

### Ids and data
`i.bread_block` 1809, `i.crustless_bread_block` 156, `i.bread_slab` 155, `i.bread_nub` 157 (item_dict).

### Decisions
- Knife on a bread block: crustless block or 16 bread? Recommendation: crustless (row 10), bread via saw and by
  smoking dough directly.

### Gotchas
- Lines 148-149 output `Item.of(i.bread, 16)` where `i.bread` is the tag `#forge:bread/wheat`; whether KubeJS 6 resolves
  a tag to its first item as a result is untested (RC-06). Use `i.minecraft_bread` (item_dict 2076) for outputs while
  touching these lines; the same applies to the sourdough knife recipe (line 161).

### Tests (RC-)
- RC-24 Bread block: knife on a bread block on the cutting board -> crustless bread block; 3 bread blocks -> 6 slabs;
  the saw still gives 16 bread.

---

## baked-goods #4: Pie sequenced assembly and chorus pie
Status Planned / Work KubeJS / TODO `baking/baked-goods #4` (cooking plan baked/pie)

### Goal
Every obtainable pie can be made by Create sequenced assembly; the chorus pie's assembly spouts chorus juice as a fluid.

### Now
- Existing SA **(jar)**: CCK `[dp farmersdelight]` `create_central_kitchen:sequenced_assembly/apple_pie` (crust ->
  deploy apple, deploy `#forge:flour/wheat`, deploy sugar, loops 2, transitional `create_central_kitchen:incomplete_apple_pie`),
  `.../mulberry_pie`, `.../pumpkin_pie` (pumpkin slices), `.../sweet_berry_cheesecake` (2 berries + 250 mB milk, loops 2).
  The pack's own: quiche (cooking.js 198-219, meals #3). FD fills a crust with 500 mB chocolate for the chocolate pie
  (`farmersdelight:integration/create/filling/chocolate_pie`).
- No SA for: `endersdelight:chorus_pie`, `farmersrespite:rose_hip_pie`, `delightful:salmonberry_pie`,
  `fruitsdelight:durian_pie`, `pineapple_pie`, the two tarts. Obtainable pies: can_parity 2.13 note (apple, chocolate,
  chorus, mulberry, pumpkin, rose hip, salmonberry, durian, pineapple, sweet berry). 2026-10-22: the cheesecake is
  renamed Sweet Berry Pie (filling can added); durian and pineapple are marked for a rework (not really pies,
  TODO `baking/baked-goods #12`); source mods of the slices with no whole pie in TODO `#14`.
- Chorus juice is only an item (`endersdelight:chorus_juice`, item_dict 1869; chorus fruit + sugar + bottle). No chorus
  fluid in the pack or in any jar read. The pack registers no fluids of its own yet (`fluid_dict.js` names mod fluids only).

### Build
1. Table of new pie assemblies, each `R.createSequencedAssembly(pie, i.pie_crust, steps, ['<pie>'], e,
   '<pie>_sequenced_assembly', 2)` with a `transition_items.js` row per pie (auto texture verified for chorus_pie,
   rose_hip_pie, salmonberry_pie, durian_pie, pineapple_pie, lemon_tart **(jar)**). Steps follow CCK's pattern
   (filling, flour, sugar per loop; 2 loops), with the oven's ingredients:
   | Pie | Steps (x2 loops) |
   |---|---|
   | salmonberry | deploy `i.t_fruits_salmonberries`, deploy flour-or-cornmeal, deploy sugar |
   | rose hip | deploy rose hips, deploy honey bottle (or fill 125 mB honey), fill 250 mB milk |
   | durian | deploy durian flesh, deploy egg, deploy sugar (loops 1: the pot makes 2 from 2 flesh) |
   | pineapple | deploy pineapple slice, deploy egg, deploy sugar (loops 1) |
   | chocolate | deploy `#forge:bars/chocolate`, fill 250 mB milk, deploy sugar; keep FD's 500 mB chocolate filling |
   | tarts (optional) | lemon / fig, egg, sugar, fill 250 mB milk, loops 1, result 2 |
   Write the steps inline with `e.recipes.create.deploying(inc, [inc, x])` / `.filling(inc, [inc, Fluid.of(...)])`
   as cooking.js 210-218 does; NCH has no per-step helper and doesn't need one.
2. Override CCK's apple and mulberry SAs to use `i.t_flour_or_cornmeal` (they take wheat flour only) only if cornmeal
   pies matter; otherwise leave them.
3. **Chorus pie** (after `food/drinks #4` decides the fluid):
   - startup: `StartupEvents.registry('fluid', e => e.create('creativeengineering:chorus_juice').thinTexture(0xa985a9)
     .bucketColor(0xa985a9).displayName('Chorus Juice'))` (new file `startup_scripts/cooking/fluids.js`); add
     `f.chorus_juice = 'creativeengineering:chorus_juice'` to `fluid_dict.js`, and `f.t_chorus_juice =
     '#forge:fluid/chorus_juice'` if a tag is wanted (dict style of `f.t_honey`).
   - server: `R.createFilling(i.chorus_juice, [i.glass_bottle, Fluid.of(f.chorus_juice, 250)], e)`,
     `R.createEmptying([i.glass_bottle, Fluid.of(f.chorus_juice, 250)], i.chorus_juice, e)`, heated mixing chorus fruit
     + sugar + 250 mB water -> 250 mB juice.
   - SA: crust -> deploy chorus fruit, deploy flour-or-cornmeal, fill 250 mB chorus juice, loops 2.

### Ids and data
Pie ids item_dict 11492, 11511, 11946, 12825, 11349, 12248, 12312, 13014, 2596; `i.pie_crust` 11556;
`i.rose_hips` 12827, `i.durian_flesh` 12858, `i.pineapple_slice` 12901. CCK's incomplete items
(`i.incomplete_apple_pie` ... item_dict 5140-5150) can be reused for apple / mulberry / pumpkin / cheesecake.

### Decisions
- **Chorus juice fluid** (TODO decision): a pack fluid (recommended) vs. deploying the juice bottle (no fluid; the bottle
  comes back as an extra output only through a remainder, which SA deploying doesn't give). Shared with `food/drinks #4`
  and `dishes/meals #4` (pearl pasta uses the bottle, cooking.js 418).
- Yield: SA pies 1 per crust like CCK. Recommendation: same as CCK.

### Gotchas
- Two SAs on the same input whose first step is the same deploy item can shadow each other (cooking.js 203 note): every
  pie here starts with a different fruit, so fine; don't start two with "deploy sugar".
- A new KubeJS fluid needs a restart and its bucket gets a generated item `creativeengineering:chorus_juice_bucket`;
  add it to item_dict if referenced.

### Tests (OV- is the oven's; use RC-)
- RC-25 Pie assembly: each pie in the table assembles from a crust in a Create line; JEI lists one SA per pie.
- RC-26 Chorus juice: spout fills a bottle with chorus juice, a drain empties it; the chorus pie SA takes 250 mB per loop.

---

## baked-goods #5: Cakes
Status Planned / Work KubeJS / TODO `baking/baked-goods #5` (cooking plan baked/cake, scratchpad 2026-10-21)

### Goal
Five parts: (a) dragon egg cake, (b) rename the mangosteen "cake", (c) remove unobtainable cake and pie slices,
(d) every obtainable slice placeable, (e) merge the two chocolate cakes, keeping CA's texture and both the spout and the
baking recipe.

### Now
- (b) `fruitsdelight:mangosteen_cake` is a bowl food placed only while sneaking (`FDBlockItem`, placeable_food.md 1),
  made in the pot with a bowl; its sprite is mangosteen flesh on a plate. Name key `block.fruitsdelight.mangosteen_cake`
  **(jar)**. 9 / 0.6, comfort 60 s, sliding 120 s (food_catalog.md 452).
- (c) Slices with no source in the pack **(jar + manifest)**:
  - CCK: `aloe_cake_slice`, `passionfruit_cake_slice`, `yucca_cake_slice` (cut from Peculiars cakes), `pumpkin_cake_slice`,
    `sweet_berry_cake_slice` (Seasonals), `cherry_pie_slice`, `truffle_pie_slice` (Environmental), plus the transitional
    `incomplete_cherry_pie`, `incomplete_truffle_pie`. None of Peculiars, Seasonals, Environmental, Atmospheric is in
    `pack/manifest.json`.
  - CCK `pumpkin_pie_slice`: its cutting recipes are removed by the pack (cooking.js 351-353), so it is dead too;
    Delightful's `pumpkin_pie_slice` is the live one.
  - Delightful: `blueberry_pie_slice`, `crimson_berry_pie_slice`, `gloomgourd_pie_slice`, `green_apple_pie_slice`,
    `nightshade_berry_pie_slice`, `source_berry_pie_slice` (whole pies come from BYG, Undergarden, Ars Nouveau, none in the
    pack). `config/delightful-common.toml` 65-73 enables all eight slice overhauls.
  - can_parity.md 2.13 (the note under the pie table) found the same 8 pie slices on 2026-10-13.
- (d) Slices with a Display Delight small-plate model: FD apple / chocolate pie, cake, cheesecake slices, Delightful
  pumpkin and salmonberry pie slices. Those also have coaster models (`kubejs/assets/creativeengineering/brewinandchewin/
  coaster/*.json`, pointing at DD's `small_plated_*` / `plated_*` models). No placement for: `endersdelight:chorus_pie_slice`,
  `farmersrespite:rose_hip_pie_slice`, `farmersrespite:coffee_cake_slice`, `create_central_kitchen:mulberry_pie_slice`,
  `chocolate_cake_slice`, `honey_cake_slice`, Abnormals' six Neapolitan cake slices, `fruitsdelight:fig_pudding_slice`,
  Brewin's quiche slice.
- (e) Two chocolate cakes: `createaddition:chocolate_cake` (CA `CACakeBlock`, 7 bites; made only by
  `createaddition:filling/chocolate_cake`, baked base + 500 mB `create:chocolate`; CCK cuts it into 7
  `create_central_kitchen:chocolate_cake_slice`, 3 / 0.3, speed 20 s + sugar rush 10 s) and `neapolitan:chocolate_cake`
  (crafted, CCK compacted / dough variants, oven `baking/neapolitan_chocolate_cake`; Abnormals cuts it into 7
  `abnormals_delight:chocolate_cake_slice`, 1 / 0.1, speed 20 s). `createaddition:honey_cake` has no oven recipe
  (`cooking/oven #6`).
- (a) Nothing exists. The dragon egg is unique per world unless Enderdragon Loot drops more (**(unverified)**, its
  config has no egg setting). No flight effect in the pack; `startup_scripts/magic/magic_resistance.js` shows how the
  pack registers its own `MobEffect` (KubeJS registers, patcherMod does the behaviour).

### Build
1. (c) Add the 7 dead CCK slices, CCK `pumpkin_pie_slice`, the 2 CCK incompletes and the 6 Delightful slices to
   `global.removedItems` ([removed_items.js](../../kubejs/startup_scripts/_lib/removed_items.js)): that hides them in JEI
   (`client_scripts/jeichanges.js`), strips their tags (`recipe_scan.js` 51-55), and labels stray stacks. Optionally also set
   the six Delightful slice switches to false in `config/delightful-common.toml` (whether that unregisters the item is
   **(unverified)**; if it does, old stacks vanish, which is fine for items no one can have). Tell `cans/canning`: the
   can_parity rows for these slices stay `/`.
2. (b) Rename in `kubejs/client_scripts/namechanges.js`: `e.add('block.fruitsdelight.mangosteen_cake', 'Mangosteen
   Platter')` (the item uses the block key). Decide the name (below). If it is no longer a cake, take it out of #1's
   cake base list and give it back its pot recipe shape.
3. (e) Chocolate cake merge, keeping `createaddition:chocolate_cake` (the CA texture comes with the id):
   - `R.replaceOutput({ output: i.neapolitan_chocolate_cake }, i.neapolitan_chocolate_cake, i.chocolate_cake, e)`, then
     check the oven recipe `baking/neapolitan_chocolate_cake` (baking.js 99) now outputs CA's; rename its id
     `chocolate_cake`.
   - `R.replaceInput({ input: i.neapolitan_chocolate_cake }, ...)` for anything that eats Neapolitan's (golden or canned
     recipes: grep `neapolitan_chocolate_cake` in `generation_scripts/food/` and canning.js).
   - Add `i.neapolitan_chocolate_cake` and `i.abnormals_delight_chocolate_cake_slice` to `global.removedItems`.
     Abnormals' knife recipe `abnormals_delight:neapolitan/cutting/chocolate_cake` goes with it (remove it explicitly).
   - Keep CA's spout recipe; it already exists. The surviving slice is CCK's (3 / 0.3), better than Abnormals' (1 / 0.1).
   - Same row in `food/drinks #3`; build once and tick both.
4. (d) Placeable slices via the Brewin' port's coaster (the cheap route, no Java): for each slice without a model, a model
   `kubejs/assets/creativeengineering/models/block/coaster/<slice>.json` with `"parent":
   "displaydelight:block/plated_apple_pie_slice"` and its `top_texture` / `inner_texture` / `side_texture` pointed at the
   whole pie's or cake's own block textures, plus a coaster file `.../brewinandchewin/coaster/<ns>__<slice>.json`
   `{ "item": "<id>", "models": "creativeengineering:block/coaster/<slice>" }` (format as the existing
   `farmersdelight__apple_pie_slice.json`). Then rerun `tools/placeable_catalog.py` so they join
   `#creativeengineering:placeable_on_coaster`. The Display Delight small plate is the nicer route but DD links only its
   own registered blocks (`DisplayEvents.onWorldLoad`, placeable_food.md 2): new plated blocks need patcherMod blocks
   plus a mixin into that method. Leave it for later.
5. (a) Dragon egg cake: a KubeJS item first (`StartupEvents.registry('item')`, `creativeengineering:dragon_egg_cake`,
   `.food(f => f.hunger(14).saturation(1.2).alwaysEdible().effect(...))`, texture in
   `kubejs/assets/creativeengineering/textures/item/`), dict key `i.dragon_egg_cake`. Recipe: oven
   `R.ceBaking([i.cake_base, i.dragon_breath, i.dragon_scale, n(i.milk, 2)], i.dragon_egg_cake, e, 'dragon_egg_cake', CAKE)`
   and an SA with a 250 mB `f.dragon_breath` fill (the pack already mixes that fluid, chocolate.js 14). A placeable cake
   block needs patcherMod (a `CakeBlock` subclass; KubeJS 6 has no cake block type). Flight: a patcherMod `MobEffect`
   that sets `abilities.mayfly` while active and clears it (unless creative/spectator) on expiry, synced with
   `onUpdateAbilities()`; registered like magic resistance.

### Ids and data
Slices: item_dict 4274, 5134-5157, 11264-11357, 12108-12132. `i.chocolate_cake` 5052 (CA), `i.neapolitan_chocolate_cake`
12948, `i.chocolate_cake_slice` 5136, `i.abnormals_delight_chocolate_cake_slice` 12111, `i.mangosteen_cake` 12287,
`i.dragon_egg` 2239, `i.dragon_breath` 2238 (item), `f.dragon_breath` (`create_central_kitchen:dragon_breath`),
`i.dragon_scale` 6321 (`dragonloot:dragon_scale`). New: `i.dragon_egg_cake`; effect key (if flight) `ef.flight =
'creativeengineering:flight'` in effect_dict.js.

### Decisions
- **Dragon egg cake effects** (TODO decision) and **does it eat the egg?** Recommendation: don't consume the dragon egg
  (one per world); build it from dragon's breath + dragon scale + cake base, keep the name for the look. Effects:
  regeneration II 30 s, resistance 120 s, slow falling 300 s; flight (if wanted) 60 s, as a patcherMod effect.
- **Mangosteen name.** Recommendation "Mangosteen Platter" (the sprite is fruit on a plate); "Mangosteen Pudding" if it
  should stay a dessert.
- **Chocolate cake id.** Recommendation CA's (texture, better slice, existing spout recipe); the row already says CA's
  texture.
- **Slice placement route.** Recommendation coaster now, DD small plate later.

### Gotchas
- Removing Neapolitan's chocolate cake also removes it from CCK's compacting recipes (cooking side) and the oven
  table; the shared flavour list (row 1 step 3) must drop it or point at CA's.
- `global.removedItems` is read at startup (restart), and `recipe_scan.js` only logs recipes using removed items: remove
  those recipes explicitly.
- `food/types-and-tuning #5`: placed non-vanilla cakes don't count for Spice of Life; a merged CA cake inherits that.

### Tests (RC- / PL-)
- RC-27 Dead slices: the 16 slices and 2 incompletes don't show in JEI; `latest.log` has no recipe error naming them.
- RC-28 Chocolate cake: one chocolate cake in JEI (CA's sprite) with the spout, oven, crafting and compacting recipes;
  a knife gives 7 CCK slices.
- PL-10 Slices on coasters: each slice in (d) sits on a coaster with its pie's look.
- RC-29 Dragon egg cake: bakes in the oven with a cake pan, eats with the chosen effects, never takes the dragon egg.

---

## baked-goods #6: Cake paste and tarts
Status Planned / Work KubeJS / TODO `baking/baked-goods #6` (cooking plan baked)

### Goal
Cake paste can be made in the FD cooking pot; waffles get more recipes and uses; more tart variants exist.

### Now
- `create_factory:cake_paste` **(jar)**: only heated mixing (sugar, `create:dough`, `minecraft:egg`, 250 mB milk).
  `create_factory:uncooked_waffle`: Create press on cake paste. `waffle`: smoking. Flavoured waffles: spout 250 mB of
  chocolate / honey / caramel / black / ruby / white chocolate onto a waffle. Waffle 4 / 0.4; flavoured 6-11 hunger
  (food_catalog.md 381-544).
- Tarts: `fruitsdelight:lemon_tart`, `fig_tart` (FD pot: fruit, crust, egg, sugar, milk bottle -> 2) and their oven
  copies (baking.js 56-59).
- can_parity.md (the note after the pie table, line ~486) holds an older user note pulling the other way: "move all
  tarts to pies, rename and retexture, recipe parity w/ other pies".

### Build
1. Pot cake paste: `R.fdCooking([i.any_sugar, i.any_dough, i.eggs, i.t_milk_bottle], i.cake_paste, e,
   'cake_paste_manual_only', 0.1, 200)` (the `_manual_only` suffix keeps Slice & Dice from making a heated-mixing twin of
   create_factory's). Also swap create_factory's mixing to tags: `R.replaceInput({ id: 'create_factory:cake_paste' },
   i.minecraft_egg, i.eggs, e)`, `i.sugar -> i.any_sugar` (already done pack-wide by cooking.js 58), `create:dough ->
   i.any_dough`.
2. Waffles: oven `R.ceBaking([i.uncooked_waffle], Item.of(i.waffle, 1), e, 'waffle', SHEET)` (baking sheet), and a
   one-step oven route from paste, `R.ceBaking([i.cake_paste], i.waffle, e, 'waffle_from_paste', SHEET)`. A hand route
   without a press: `R.shapeless(i.uncooked_waffle, [i.cake_paste, i.baking_sheet], e, 'uncooked_waffle')` with the sheet
   kept through KubeJS's keep action (`recipe.keepIngredient(i.baking_sheet)`; `NCH.shapeless` returns the recipe, and
   `KeepAction` is in the KubeJS 1902.6.2 jar). Flavoured waffles by hand: waffle + honey bottle / chocolate bar /
   caramel bar, shapeless (the honey bottle's remainder is the glass bottle).
3. More cake paste uses: cake paste as a cake base substitute (`[i.cake_base, i.cake_paste]` alternatives in #1's cake
   recipes) and in muffins (#2).
4. Tarts: new KubeJS food items (`creativeengineering:<fruit>_tart`) for 3-4 fruits with tags in the pack
   (`#forge:fruits/blueberry`, `peach`, `orange`, `strawberry`), each with a pot recipe mirroring the lemon tart, an oven
   recipe on the baking sheet, and a sprite. Food values/effects copied from the fruit's jello effects at tart length
   (lemon tart: 5 / 0.4, haste + refreshing 20 s).

### Ids and data
`i.cake_paste` 5258, `i.uncooked_waffle` 5274, `i.waffle` 5275, flavoured waffles 5261-5278; tarts `i.lemon_tart` 12277,
`i.fig_tart` 12253. Fruit tags verified in Fruits Delight: persimmon, fig, mango, cranberry, hawberry, mangosteen,
pear, orange, peach, lychee, kiwi, lemon, pineapple, bayberry, blueberry **(jar)**.

### Decisions
- **Tarts: more variants, or fold tarts into pies** (can_parity note)? Recommendation: more variants as tarts (cheap,
  baking sheet), since tarts plate 2 per bake and pies are placed blocks; drop the can_parity note when decided.
- Which tart fruits. Recommendation: blueberry, peach, orange (no tart or pie uses them yet).

### Gotchas
- Every new item needs a texture and a restart, and a food type: rerun `tools/food_catalog.py` after a `/dumpfood` so it
  lands in `generation_scripts/food/food_types.json`.
- The oven has 6 ingredient slots; a waffle recipe with paste + flavour is fine.

### Tests (RC- / OV-)
- RC-30 Cake paste pot: sugar, dough, egg, milk bottle in the cooking pot -> cake paste; no extra mixer recipe in JEI.
- OV-21 Oven waffle: uncooked waffle + baking sheet -> waffle.
- RC-31 Tarts: each new tart cooks in the pot and bakes in the oven, 2 per batch.

---

## baked-goods #2: Muffins as a decent food source
Status Planned / Work KubeJS / TODO `baking/baked-goods #2` (cooking plan baked/muffins)

### Goal
Muffins worth eating: more variants including two-fruit muffins with mixed, shorter effects (orange cranberry, lemon
poppy), "fruit, milk, egg, sugar, flour", served on paper, and a sequenced assembly. **Cornmeal too (asked
2026-10-21 with `dishes/ingredients #14`):** every rebuilt muffin recipe takes `i.t_flour_or_cornmeal` where it takes
flour (today the oven muffins use `i.flour`, baking.js 36 and 38, and oven.md OV-19 says they take no cornmeal; update
OV-19 when this lands). Cookies stay flour-only.

### Now
- Two muffins, both Fruits Delight: `blueberry_muffin` (5 / 0.4, night vision + brightening 20 s), `cranberry_muffin`
  (5 / 0.4, shrinking 80 s) (food_catalog.md 375, 411).
- Pot: cooking.js 364-369, `#forge:dough`, 2 per batch, **no paper** (FD pot container = none). Oven: baking.js 36-39,
  flour, muffin tin, plated on paper, 2 per bake. `cooking/oven #5` wants 4 per oven bake; `cooking/oven #3` says muffins may
  not cook in the pot.
- Fruits Delight effects (mob_effect textures in the jar): `brightening`, `shrinking`, `refreshing`, `recovering`,
  `digesting`, `sliding`, `astringent`, `leaf_piercing`, ... **(jar)**. effect_dict.js has vanilla effects only.
- No poppy seed item in the pack; `i.poppy` is the flower (item_dict 2583).

### Build
1. Pot muffins plated on paper: add `i.paper` as the `container` arg of the two `fdCooking` calls (cooking.js 366-369),
   matching the oven.
2. New muffins as KubeJS items (new `startup_scripts/cooking/muffins.js`), each `.food(f => f.hunger(n).saturation(m)
   .effect(id, ticks, amp, 1))`:
   - `creativeengineering:orange_cranberry_muffin`: orange + cranberry; regeneration 10 s + shrinking 40 s (half each).
   - `creativeengineering:lemon_poppy_muffin`: lemon + poppy (flower); haste 10 s + refreshing 10 s.
   - single-fruit ones if wanted: banana (`#forge:fruits/banana`), strawberry, peach.
   Dict keys `i.orange_cranberry_muffin` etc. near the Fruits Delight muffins (item_dict 12237); effect keys
   `ef.fd_refreshing = 'fruitsdelight:refreshing'` style in effect_dict.js (add a "Fruits Delight" block).
3. Recipes per muffin, one table: pot `[fruitA, fruitB, i.flour, i.t_milk_bottle, i.eggs, i.any_sugar]` with paper,
   oven (baking.js MUFFIN opts), and an SA: input `i.paper` -> deploy flour, deploy egg, deploy sugar, deploy fruit,
   fill 250 mB milk, loops 1 -> 2 muffins.
4. "Decent food source": set hunger / saturation for all muffins together, from `food/types-and-tuning #1` (food.md 3.5).
   Until that exists, extend `startup_scripts/cooking/food_values.js` with explicit values (it only copies whole
   FoodProperties today) or set them in the new items' builders. Proposed: 6 / 0.6 for every muffin.

### Ids and data
`i.blueberry_muffin` 12237, `i.cranberry_muffin` 12244, `i.paper` 1359, `i.t_fruits_*` 1898-1906 (add
`i.t_fruits_orange = '#forge:fruits/orange'`, `i.t_fruits_lemon` exists), `i.poppy` 2583.

### Decisions
- **Variants** (TODO decision). Recommendation: the two combos plus banana and strawberry singles (the pack's
  Neapolitan fruits).
- Poppy: the flower (no new item) vs. a `poppy_seeds` item from cutting a poppy. Recommendation: the flower.

### Gotchas
- Fixing `cooking/oven #3` first: if pot muffins don't cook, new pot muffins won't either. Run OV-13.
- Paper as the FD pot container makes the pot wait for paper in its bowl slot; automation must feed paper.
- KubeJS food builder effects take ticks (20 per second).

### Tests (RC- / OV-)
- RC-32 Muffins on paper: the pot asks for paper; each new muffin cooks in the pot, bakes in the oven and assembles.
- RC-33 Muffin effects: orange cranberry gives both effects at the shorter length.

---

## baked-goods #9: Sliced bread / toast in recipes
Status Planned / Work KubeJS / TODO `baking/baked-goods #9` (cooking plan baked/sliced bread, scratchpad 2026-10-21)

### Goal
Every sandwich takes the bread its sprite shows: square slices -> bread slices (or toast), long rolls -> whole bread,
round burgers -> buns, stuffing -> whole bread only; ambiguous ones take both.

### Now
- Bread slice and toast: More Delight `moredelight:bread_slice` (knife on `#forge:bread/wheat` -> 4), `toast` (smoke or
  campfire a slice). Tag `#forge:bread_slices` = `#forge:bread_slices/wheat` = slice **and toast** **(jar)**.
- Already done: Brewin's ham and cheese sandwich takes `[bread_slice, bread]` per side (cooking.js 180-196).
  More Delight already overrides `farmersdelight:bacon_sandwich` to 2 `#forge:bread_slices` **(jar)**.
- No bun item in the pack. `#forge:bread` is FD's `#forge:bread/wheat` = `minecraft:bread` **(jar)**.
- Audit (sprites checked from the jars, 2026-10-21):

  | Food (recipe id) | Sprite | Takes now | Proposed |
  |---|---|---|---|
  | `farmersdelight:bacon_sandwich` (More Delight's) | square slices | 2 slices | keep |
  | `moredelight:egg_with_bacon_sandwich`, `tomato_sandwich` | square slices | 2 slices | keep |
  | `delightful:food/nut_butter_and_jelly_sandwich` | square slices | bread (or SAR slices, absent) | 2 slices |
  | `alexsdelight:bunfungus_sandwich` | square slices | bread | 2 slices |
  | `alexsdelight:gongylidia_bruschetta` | toasted slices | bread | toast (2 -> 2) |
  | `farmersdelight:chicken_sandwich`, `egg_sandwich`, `moredelight:chicken_sandwich_with_egg_and_tomato`, `porkchop_sandwich`, `steak_sandwich`, `endersdelight:crawling_sandwich`, `alexsmobs:kangaroo_burger` (both recipes) | long roll | bread | bread (keep) |
  | `farmersdelight:hamburger`, `moredelight:simple_hamburger`, `hamburger_with_egg`, `loaded_hamburger`, `delightful:cheeseburger`, `deluxe_cheeseburger`, `alexsdelight:bison_burger` | round bun | bread | bun |
  | `farmersdelight:mutton_wrap` | tortilla wrap | bread | tortilla (`dishes/meals #2` "mutton roll needs tortilla") |
  | `fruitsdelight:jelly_bread`, `buzzier_bees:honey_bread`, `upgrade_aquatic:mulberry_bread`, Create `sweet_roll`, create_factory rolls | whole loaf | bread | keep; slices/toast already have More Delight's `toast_with_*` |
  | `farmersdelight:roast_chicken_block`, `corn_delight:cornbread_stuffing` (stuffing) | - | bread | whole bread only (keep) |

  CCK's sandwich SAs (`create_central_kitchen:sequenced_assembly/{bacon,chicken,egg}_sandwich`, `hamburger`,
  `mutton_wrap`) start from `#forge:bread` **(jar)**.

### Build
1. Slices: `R.replaceInput({ id }, '#forge:bread', [slice x2]...)` doesn't work for "1 bread -> 2 slices"; re-add the
   recipe instead: `R.remove` + `R.shapeless(out, [i.t_bread_slices, filling..., i.t_bread_slices], e, id)` with
   `i.t_bread_slices = '#forge:bread_slices'` (new dict key). Slices include toast via the tag; for "slice only" use
   `i.bread_slice`.
2. Buns: new KubeJS item `creativeengineering:burger_bun` (dict `i.burger_bun`, tag `#forge:bread/bun` or
   `#creativeengineering:burger_buns`), oven `R.ceBaking([i.any_dough, i.eggs], Item.of(i.burger_bun, 4), e,
   'burger_bun', SHEET)`, smoking 1 dough -> 1 bun, knife on bread -> 2 buns. Burgers then take 1 bun (or bread,
   if "ambiguous take both"). Also redo CCK's hamburger SA with a bun input.
3. Toast for the bruschetta: shapeless with `i.toast` x2 for the bread.
4. Mutton wrap: leave to `dishes/meals #2`.

### Ids and data
`i.bread_slice` 12364, `i.toast` 12388, `i.minecraft_bread` 2076, `i.bread` `#forge:bread/wheat`; the
[meals brief](dishes_meals.md) proposes `i.t_bread = '#forge:bread'` (bread, cornbread, sourdough bread) for whole-bread
inputs, use the same key here; sandwich ids from
item_dict (moredelight block from 12364, FD block from 11492). Recipe ids in the table **(jar)**.

### Decisions
- **Buns** (row says buns; `dishes/meals #2` says burgers "bread or cornbread only"). Recommendation: a bun item, with
  cornbread accepted as a bun stand-in, decided together with meals #2.
- Cost: 1 bread = 4 slices; a 2-slice sandwich is half a bread, cheaper than today's 1 bread. Recommendation: accept
  (it is the point of slicing).

### Gotchas
- `#forge:bread_slices` contains toast; a recipe that should take untoasted slices only must use the item.
- More Delight's bacon sandwich override shares FD's id; touch it by id, not by output.

### Tests (RC-)
- RC-34 Sandwich bread: every row of the table takes the proposed bread in JEI; burgers take a bun.

---

## baked-goods #8: Yeast
Status Planned / Work KubeJS / TODO `baking/baked-goods #8` (cooking plan baked/yeast)

### Goal
Yeast culture from mushroom colonies or mushroom blocks by crafting and heated mixing; fix the in-world yeast process;
yeast feeds the sourdough chain (#7).

### Now
- Yeast is PneumaticCraft's fluid `pneumaticcraft:yeast_culture` (`f.yeast_culture`, `f.t_yeast_culture`
  `#forge:fluid/yeast_culture`) with `i.yeast_culture_bucket`. Sources **(jar)**: Thermopneumatic Processing Plant,
  `#forge:mushrooms` + 1000 mB water -> 250 mB at 30-60 °C (`pneumaticcraft:thermo_plant/yeast_culture`); in-world: a
  yeast culture source block with **`minecraft:sugar`** item entities in its block space spreads into an adjacent water
  source when its fluid tick runs (`FluidYeastCulture$Source.tick`: exact `Items.SUGAR`, neighbour must be
  `Blocks.WATER` source). `in_world_yeast_crafting = true` (`config/pneumaticcraft-common.toml` 255).
- What "fix the in-world yeast process" means isn't written down. Candidates: ItemPhysic makes sugar **sink**
  (item_physics.md 2, "heavy powders ... sugar") so it can rest below the culture block's box when the culture sits on
  a slab or the pool is deeper; glycerol (`#forge:sugar`, cookingtags.js 7) is not accepted; the tick only runs when the
  water is placed (PNC's own book says so).

### Build
1. In game first: pour a culture bucket in a 1-deep pool, drop sugar into it, place water next to it; repeat with the
   culture on top of another culture block. Note what fails.
2. KubeJS recipes (in `baked_goods.js` or a new `cooking/yeast.js`):
   - `R.shapeless(i.yeast_culture_bucket, [i.water_bucket, [i.brown_mushroom_colony, i.red_mushroom_colony], i.any_sugar],
     e, 'yeast_culture_bucket')`.
   - `R.createMixing(Fluid.of(f.yeast_culture, 500), [Fluid.of(f.water, 500), [i.brown_mushroom_block,
     i.red_mushroom_block, i.brown_mushroom_colony, i.red_mushroom_colony], i.any_sugar], e, 'yeast_culture_mixing', 'heated')`.
3. If step 1 shows the sugar-sinking problem, add sugar to `#creativeengineering:physics/floats` in
   `server_scripts/tags/item_physics_tags.js` (and out of the sinks rule) or accept and document; if the tick timing is
   the problem, it is PNC's design (document it in recipes_cooking.md).

### Ids and data
`i.brown_mushroom_colony` / `i.red_mushroom_colony` (item_dict 612-613, duplicated at 633-634 and 740-741),
`i.brown_mushroom_block` 2090, `i.red_mushroom_block` 2632, `i.mushrooms` `#forge:mushrooms`, `i.water_bucket` 152.

### Decisions
- Yield: Recommendation 1 colony -> 1 bucket by hand, 500 mB per block in the mixer.

### Gotchas
- A water bucket in a crafting grid leaves its empty bucket behind (vanilla crafting remainder), so water bucket ->
  yeast culture bucket would also return an empty bucket: a bucket dupe. Chain `.consumeIngredient(i.water_bucket)` on
  the `R.shapeless` result (KubeJS `ConsumeAction`, in the 1902.6.2 jar) so the water bucket is used up without a
  remainder.

### Tests (RC-)
- RC-35 Yeast crafting: a colony + water bucket + sugar gives one yeast culture bucket and no spare empty bucket.
- RC-36 In-world yeast: sugar in a culture pool + water placed next to it -> the water turns to culture.

---

## baked-goods #7: Sourdough chain
Status Planned / Work KubeJS / patcherMod / TODO `baking/baked-goods #7` (cooking plan baked/sourdough)

### Goal
Starter -> growing -> fermenting -> sourdough, with a timed wait between steps; the sprite grows through the chain; milling
or the mortar turns sourdough back into starter; a starter keeps its start date and a name, and passes both to any
sourdough made from it.

### Now
- cooking.js 153-165: `pneumaticcraft:sourdough` removed and re-added as `R.shapeless(Item.of(i.sourdough, **1**),
  [i.yeast_culture_bucket, Item.of(i.dough, 8)], e)`; heated mixing 500 mB yeast + 8 dough -> 8; IE bottler 50 mB per
  dough -> 1; smoking -> `pneumaticcraft:sourdough_bread` (7 / 1.0); knife -> 2 bread. PNC's own recipe was 8 flour +
  1000 mB yeast -> **8** **(jar)**, so the pack's hand craft gives 1 where PNC gave 8 (likely a bug; see Gotchas).
- Farmer's Respite's `nether_wart_sourdough` is unrelated (mushrooms + wart, no yeast).
- Tools the chain can use:
  - KubeJS `modifyResult` on crafting recipes **(jar)**: `RecipeJS.modifyResult(callback)`, called from
    `KubeJSCraftingRecipe.kjs$assemble` with a `ModifyRecipeCraftingGrid` (`get(i)`, `find(ingredient)`, `findAll()`,
    `getPlayer()`) and the result stack; returning an empty stack means no output, which blocks the craft.
    `R.shaped` / `R.shapeless` return the recipe, so `.modifyResult(...)` chains.
  - patcherMod's calendar: `com.SaintAlfonzo.creativeengineering.time.CalendarDate.worldDay(Level)` (sunrises on the
    pack's SUN clock, time.md 3.7) is callable from KubeJS with `Java.loadClass`.
  - The Brewin' keg (port, alcohol.md 2.2): `brewinandchewin:fermenting` with item results, `fermenting_time` and
    `temperature`, through `R.bncFermenting`.

### Build (recommended route: KubeJS, lazy timestamps)
1. Items (startup, new `startup_scripts/cooking/sourdough.js`): `creativeengineering:sourdough_starter`,
   `growing_sourdough_starter`, `fermenting_sourdough` (three sprites growing in size; stack size 1 so each carries its
   own NBT). Dict keys under the sourdough lines (item_dict 1812).
2. Recipes (server):
   - start: `R.shapeless(i.sourdough_starter, [i.flour, i.flour, i.water_bottle, yeast], e, 'sourdough_starter')`
     with `.modifyResult((grid, out) => { var p = grid.player; if (!p) return Item.empty; out.nbt = { Started:
     CalendarDate.worldDay(p.level) }; return out })`. `i.water_bottle` exists (item_dict 1362, an NBT ingredient).
     The name is the vanilla custom name (rename the starter on an anvil); copy it with `out.setHoverName(...)` in the
     later steps. Yeast from #8 (see Decisions on its form).
   - feed: starter + flour + water -> growing starter, blocked (`return Item.empty`) until `worldDay - Started >= 1`;
     copy `Started` and the stack's custom name.
   - ferment: growing starter + 8 dough -> fermenting sourdough (keeps NBT), blocked until 1 more day.
   - bake-ready: fermenting sourdough -> `Item.of(i.sourdough, 8)` with lore "From <name>'s starter, day N". To keep a
     starter alive, the ferment step (not this one) keeps the growing starter in the grid with
     `recipe.keepIngredient(i.growing_sourdough_starter)` (KubeJS keep action; a crafting remainder can't carry NBT).
   - client tooltip (`client_scripts`): "Started day N, ready in M days" from the NBT and the client's level.
3. Back to starter: `R.createMilling(i.sourdough_starter, i.sourdough, e, 'sourdough_starter_milling')` (NBT can't carry
   through a machine) and the mortar route when `cooking/kitchen #10` adds one.
4. Fix the existing shapeless to 8 (Gotchas) or remove it once the chain replaces it.

### Ids and data
`i.sourdough` `pneumaticcraft:sourdough` 1812, `i.sourdough_bread` 1813, `i.yeast_culture_bucket` 158, `f.yeast_culture`.
New: three starter items; NBT key `Started` (long, world day on the SUN clock) plus the vanilla custom name. `i.water_bottle`
(item_dict 1362) and `i.flour` exist.

### Decisions
- **How the timed wait works** (TODO decision):
  - (A, recommended) NBT timestamp + `modifyResult` check, as above. No Java; the wait is real time on the SUN clock
    (sleeping skips it forward); the starter can sit in a chest. JEI shows the recipe normally.
  - (B) Brewin' keg fermenting recipes per stage (`R.bncFermenting(i.growing_sourdough_starter, [...], f.water, e, ...,
    { time: 24000 })`): a real timer and temperature, but the keg makes a fresh stack, so no name or date carries.
  - (C) patcherMod item class with `inventoryTick` that advances the stage by itself: sprites change on their own, but
    needs Java and only ticks in a player's inventory.
- **Yeast form**: a yeast culture *bottle* (new item, filled from the fluid with `R.createFilling` / IE bottler) as the
  starter's yeast. Recommendation yes; a bucket per starter is wasteful.
- Wait lengths: Recommendation 1 world day per step.

### Gotchas
- `modifyResult` runs on the server; the client predicts the unmodified result, so the result slot can flicker or show
  a stack that the server then refuses. Test on a dedicated server.
- `getPlayer()` can be null (Create mechanical crafter, autocrafters): treat null as "block" for the timed steps, so the
  chain stays hand-made.
- cooking.js 156 `Item.of(i.dough, 8)` wraps a **tag** (`#forge:dough/wheat`) in `Item.of`; whether KubeJS turns that
  into 8 dough slots is untested (RC-07 only covers the mixer). Check the JEI page; use 8 copies of `i.dough` in the
  input array otherwise.

### Tests (RC-)
- RC-37 Starter: crafting a starter stamps the day; feeding it the same day gives no output; a day later it does and
  keeps the name.
- RC-38 Sourdough from a starter: 8 sourdough named after the starter; the starter stays in the grid.
- RC-39 Sourdough hand craft: yeast bucket + 8 dough gives 8 sourdough (or the recipe is gone, if the chain replaces it).

---

## baked-goods #11: Mixed cookie jars
Status BUILT 2026-10-21, see [mixed_jars.md](../mixed_jars.md) (families, slot-per-item design, tests MJ-01 to MJ-10; BG-J1 became MJ-05) / Work patcherMod / TODO `baking/baked-goods #11` (cooking plan cooked/soup, [wthit_lines.md](../wthit_lines.md) 2.3, scratchpad 2026-10-21)

### Goal
(a) Every cookie and the listed sweets/fruits fit in a Supplementaries jar; (b) one jar can hold several kinds.

### Now
- Supplementaries 2.4.20 **(jar)**: `JarBlockTile` (extends `ItemDisplayTile`, one display stack) accepts an item when
  `jar_cookies` is on (`config/supplementaries-common.toml` 109), the fluid tank and mob container are empty, the jar
  isn't full and the item is in `#supplementaries:cookies`; then `return isEmpty() || item == getDisplayedItem().getItem()`
  (`canPlaceItem`, SRG `m_7013_`; same check in `canPlaceItemThroughFace`, `m_7155_`). `addItem` grows the one stack.
  `JarBlockTileRenderer.render` draws `getDisplayedItem()` once per count, stacked. Capacity = `[functional.jar]` capacity
  (12). The tag is read only by the jar and the creative tab.
- `#supplementaries:cookies` already holds `minecraft:cookie`, FD honey / sweet berry cookies, `farmersrespite:green_tea_cookie`,
  `abnormals_delight:mulberry_cookie`, `botania:mana_cookie`, `create:honeyed_apple`, `create:chocolate_glazed_berries`,
  `create:chocolate_bar`, `supplementaries:candy` (plus absent mods) **(jar)**.
- WTHIT line: patcherMod `compat/WthitHolderContents.java` 100-101 reads `jar.getDisplayedItem()` ("Cookie (5 / 12)").

### Done 2026-10-21
- Cookies only, in `server_scripts/tags/cookingtags.js` (end) with the dict key `i.t_jar_cookies` (item_dict, next to
  `i.candy`): cherry, maple, bayberry, cranberry, lemon, persimmon, golden, enchanted golden cookie, uncanny cookies,
  and `create_confectionery:gingerbread_man` (not in the list below; it is a cookie). Cans and
  `create_confectionery:gingerbread` (a slab piece) left out. Cherry and maple have no recipe yet (#3) but tagging is harmless.

### Mixing: what has to change (re-checked against the decompiled jar 2026-10-21)
The jar's inventory is Moonlight `ItemDisplayTile.stacks`, a private `NonNullList` sized 1 by the 3-arg constructor;
`getMaxStackSize()` (`m_6893_`) returns the jar capacity, so all 12 cookies are one stack in slot 0. Code that reads
only slot 0: `canPlaceItem` / `canPlaceItemThroughFace` (same-item test), `addItem` (grows slot 0), `extractItem`
(splits slot 0), `isFull` (slot 0 count), `handleInteraction`'s eat branch (off here: `drink_from_jar = false`),
`JarBlockTileRenderer.render`, `JarItemRenderer` (reads `Items[0]` only), `JarBlock#getAnalogOutputSignal`
(`AbstractContainerMenu.getRedstoneSignalFromContainer`, which scales by slot count), and patcherMod
`WthitHolderContents` 100-103. `JarItem`'s tooltip already loops over up to 27 stored stacks, so it shows a mix as is.
Nothing else in Supplementaries touches `JarBlockTile`.

A simpler alternative to the `CE_Layers` list below: give the jar `capacity` slots of one item each, in insertion
order. `@Inject` at RETURN of the `JarBlockTile` constructor calls `setItems` (`m_6520_`, public) with
`NonNullList.withSize(capacity, EMPTY)`; Moonlight's load rebuilds the list at the current size and
`ContainerHelper` saves the `Slot` byte, so old single-slot jars load into slot 0 (a 5-cookie stack in slot 0 then
needs splitting on load, or the code just treats slot counts generally). That keeps the data in the vanilla `Items`
tag, so the client sync, the jar item's `BlockEntityTag` and the tooltip work with no extra tag. The mixins are then:
`canPlaceItem` / `canPlaceItemThroughFace` (any cookie while total < capacity), `addItem` (first empty slot),
`extractItem` (last filled slot), `isFull` (total count), `getMaxStackSize` -> 1 (fixes the comparator; check that
nothing else depends on it being capacity, the WTHIT line reads it as the capacity and must switch to the
config value), and the two renderers loop over the slots. Hoppers then see 12 slots, which is fine.

### Build
1. (a) Tags, KubeJS only (`server_scripts/tags/cookingtags.js`):
   `t.add(e, 'supplementaries:cookies', [i.lemon_cookie, i.cranberry_cookie, i.bayberry_cookie, i.persimmon_cookie,
   i.uncanny_cookies, i.golden_cookie, i.enchanted_golden_cookie, i.cherry_cookie (if #3 revives it), i.apple,
   i.golden_apple, i.enchanted_golden_apple, i.sweet_berries, i.golden_sweet_berries, i.enchanted_golden_sweet_berries,
   i.glow_berries, i.golden_glow_berries, i.caramel_apple, i.chocolate_glazed_berries (already in), the
   create_confectionery glazed berries, i.hyper_experience_bottle])`. Use a dict key for the tag
   (`i.t_jar_cookies = '#supplementaries:cookies'`). "Candy apples" = `create_factory:caramel_apple`, `create:honeyed_apple`;
   "Hyperexperience" = `create_enchantment_industry:hyper_experience_bottle` (item_dict 5249).
2. (b) Mixed jars, patcherMod (client + common mixins in `creativeengineering.mixins.json`):
   - `JarBlockTile`: `@Inject` at HEAD of `m_7013_` and `m_7155_` returning true for any `#supplementaries:cookies` item
     when not full (drop the same-item test); `@Inject`/`@Overwrite`-style on `addItem` to append the item id to a
     `CE_Layers` list tag (saved in `m_183515_`, loaded in `m_142466_`) and keep the display stack as the first item
     with the total count; `extractItem` pops the top layer and returns that item.
   - `JarBlockTileRenderer.render`: in the per-layer loop, render `CE_Layers[i]` instead of the display stack
     (`@Redirect` of the `ItemRenderer.renderStatic` call, or `@ModifyVariable` on the stack local).
   - `WthitHolderContents.contents`: list the kinds ("Contents: 3 Cookie, 2 Lemon Cookie (5 / 12)").
   - The jar item (picked up jar) keeps `BlockEntityTag`, so the list travels; `JarItem`'s tooltip shows only the display
     stack unless mixed too.

### Ids and data
Cookie ids as #3; `i.golden_cookie` 12352, `i.enchanted_golden_cookie` 12330, `i.golden_sweet_berries` / `golden_glow_berries`
(Golden Food block, item_dict 12315-12360), `i.caramel_apple` 5259, `i.honeyed_apple` 181. Mixin targets: class
`net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile`, methods `m_7013_`, `m_7155_`, `addItem`,
`extractItem`, `m_183515_`, `m_142466_`; `net.mehvahdjukaar.supplementaries.client.renderers.tiles.JarBlockTileRenderer#render`
**(jar, decompiled)**.

### Decisions
- **Whether** (TODO decision) to build mixed jars. Recommendation: do (a) now (tags only); (b) only if mixed jars are
  wanted for decor, since it is three mixins on another mod's tile and renderer. A cheaper stand-in for (b): an
  "Assorted Cookies" KubeJS item (crafted from 4 different cookies -> 4) in the cookies tag, with a mixed sprite.

### Gotchas
- Apples and berries in the "cookies" tag also become jar-able for hoppers / dispensers (Supplementaries' jar
  dispenser behaviour), which is intended.
- The renderer draws every layer as a flat cookie-sized item; apples will look like stacked apples. Check the look.
- `ItemDisplayTile` syncs its inventory to the client; the `CE_Layers` tag must go through the same save so the client
  renderer sees it (it does if written in `saveAdditional`, which `getUpdateTag` uses **(unverified)**).

### Tests (WL- / PL-)
- BG-J1 Jar cookies (built 2026-10-21): each of cherry, maple, bayberry, cranberry, lemon, persimmon, golden, enchanted
  golden cookie, uncanny cookies and gingerbread man goes into an empty jar and stacks to 12. After a restart (new dict key),
  `/kubejs hand` on one shows `#supplementaries:cookies`.
- BG-J2 Jar foods (if the step-1 apples/berries are added): each goes into an empty jar and stacks to 12.
- BG-J3 Mixed jar (if built): two cookie kinds in one jar render in order, the WTHIT line lists both, breaking and
  replacing the jar keeps them.

---

## Built, needs play

None: no baked-goods row is at Testing or Complete.

## Writeup corrections made

- [recipes_cooking.md](../recipes_cooking.md) 8: last history line, added that the STAGED 1.0.8 items (cornmeal, pie
  crust, pancakes) went out with 1.0.8. (Its section 0 status and cooking.js line ranges were already corrected by
  another brief the same day.)
- [oven.md](../oven.md) 4: "a third cheaper for those 25" -> "those 9" (2.6.1 lists 9 trimmed recipes).
