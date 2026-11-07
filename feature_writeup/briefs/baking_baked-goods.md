# baking / baked-goods: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# baking`, Sub `baked-goods` (rows 1 to 11); rows 12 to 37 added and every
section re-checked against the pack 2026-10-31. Feature writeups: [oven.md](../oven.md) (test prefix `OV-`, last OV-37:
the oven, bakeware and the baking overhaul, 2.6), [food_families.md](../food_families.md) (`FF-`, last FF-14: family rules,
4.6 baked families), [recipes_cooking.md](../recipes_cooking.md) (`RC-`, last RC-175: flour, dough, bread, sourdough),
[placeable_food.md](../placeable_food.md) (`PL-`, last PL-14), [mixed_jars.md](../mixed_jars.md) (`MJ-`). The "cooking plan"
the rows cite is the removed `feature_writeup/cooking.md`; its text is in git (`git show 2e8687f^:feature_writeup/cooking.md`,
lines 54-66 and 157-201). Test ids marked "proposed" are free on 2026-10-31 in their writeup; the 2026-10-21 proposals
(RC-19 to RC-39, RC-100 to RC-107, PL-10) were taken by other tests since and are renumbered below.

Where the code lives now (moved 2026-10-25, [creative_delight.md](../creative_delight.md)): server scripts in
`kubejs/data/creativedelight/kubejs/<mod>/` (oven tables in
[creativeengineering/baking.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/baking.js)), the family helper
`global.CD.food(e)` in [cd_food.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creativedelight/cd_food.js)
(Creative Delight jar: an edit needs `:creativeDelight:build`, install, restart), CD startup items under
`development/creativeDelight/src/main/resources/kubejs_startup/<mod>/`. Every cake, pie and cookie recipe of every mod is
swept by its family call (`F._sweep`, cd_food.js 135) and rebuilt on shared bases (oven.md 2.6).

Mod facts marked **(jar)** were read from the pack's jars; **(dump)** from `kubejs/exported/` (recipes 2026-10-30, tags and
items 2026-10-30); **(unverified)** means check in game.

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #37 | Zucchini quiche sequenced assembly fails to load | Bug | one-line fix in cd_food.js, CD rebuild |
| #1 | Flour, crust and cake base in every baked recipe | In Progress | small: 2 recipes left (crust press, dough block -> bases) |
| #2 | Muffins as a decent food source | In Progress | medium, KubeJS items + textures |
| #4 | Pie sequenced assembly and chorus pie | In Progress | medium, one cd_food.js helper + transition items |
| #5 | Cakes | In Progress | medium: rename, dead cake slices, slice placement, dragon egg cake |
| #6 | Cake paste and tarts | In Progress | medium, KubeJS + lang + textures |
| #7 | Sourdough chain | In Progress | large, KubeJS (`modifyResult` timestamps) |
| #8 | Yeast | In Progress | small KubeJS + an in-game test first |
| #9 | Sliced bread / toast in recipes | In Progress | superseded by #28; close or keep a bun item only |
| #10 | Bread block | In Progress | small, KubeJS |
| #12 | Durian and pineapple pie rework | In Progress | small-medium, decision first |
| #13 | Cherry pie | In Progress | small, CD startup item + table row + can |
| #14 | Unobtainable pie slices | In Progress | small, per slice add or hide |
| #17 | Dough unification | In Progress | small-medium, mostly decisions |
| #18 | Brownie and cupcake baking types | In Progress | medium; cupcake half dropped by #32 |
| #19 | Quiche baking type | In Progress | medium |
| #20 | Pizza food type | In Progress | medium |
| #21 | Lasagna baking type | In Progress | medium, needs #30 |
| #22 | Chorus cookie from the uncanny cookie | In Progress | small, one table row |
| #23 | Oatmeal cookies from oat dough | In Progress | small + one new item |
| #24 | No shapeless pie-to-crust recipes | In Progress | nothing to build (dump check 2026-10-30): close after JEI |
| #32 | Merge muffin and cupcake | In Progress | small: retexture left |
| #30 | Baking dish bakeware | Planned | small, CD startup item + tag + ware key |
| #34 | Every cake as a sequenced assembly | Planned | medium, one cd_food.js helper + transition items |
| #35 | Investigate brownie tray and adzuki bun family | Planned | investigation only |
| #36 | Dessert cookware and recipe treatments | Planned | medium, decision first |
| #3, #11, #15, #25-#29, #31, #33 | built rows | Testing | listed under Built, needs play |

Build order:

1. **#37 first** (one line, CD rebuild): it unblocks every later `F.assemble` caller (#4, #19, #20, #34).
2. **One CD rebuild for the helpers:** `F.pieAssemble` (#4), `F.cakeAssemble` (#34), `ware.dish` (#30), cornmeal in
   `F.muffinBake` (#2), plus their transition items (`kubejs/startup_scripts/changes/transition_items.js`, restart).
3. **Startup items, one restart:** baking dish (#30), cherry pie and blueberry pie block items (#13, #14), new muffins
   (#2), oatmeal raisin cookie (#23), dragon egg cake (#5); hidden slices in `global.removedItems` (#5, #14).
4. **Server tables (`/reload`):** #1, #10, #22, #23, then #18 to #21 and #36 once their decisions are in.
5. **Later:** #8 then #7 (the chain uses yeast); #5 slice placement with `food/placeable`.

Cross-family links: `dishes/ingredients #2` (frosting fluid, built), `#14` (cornmeal as flour), `#24` (batter grains);
`food/drinks #4` (chorus juice fluid, built); `dishes/meals #2` (burgers, quiche), `#3` (sequenced meals), `#12` (pancake
type); `dishes/jams #4` (cherry jam); `cans/canning` (filling cans); `food/placeable` (slice placement); `cooking/kitchen
#10` (mortar); `merges/baking #1` (stackable pancakes); `ports/fungiport #3` (truffle pie item).

---

## baked-goods #37: Zucchini quiche sequenced assembly fails to load
Status Bug / Work KubeJS (Creative Delight jar) / TODO `baking/baked-goods #37` ([creative_delight.md](../creative_delight.md) 30, [food_families.md](../food_families.md) `quiche/whole`)

### Goal
The zucchini quiche assembly the 1.1.04 changelog announced loads: pie crust -> 3 roasted zucchini -> one cooked meat ->
250 mB milk -> Zucchini Quiche.

### Now
- Caller: [veggiesdelight/recipes.js](../../kubejs/data/creativedelight/kubejs/veggiesdelight/recipes.js) 61-66,
  `global.CD.food(e).assemble(i.pie_crust, [zucchini x3, [cooked_mutton, cooked_chicken, cooked_beef, cooked_pork,
  cooked_bacon], { fill: Fluid.of(f.milk, 250) }], i.zucchini_quiche)`. Transition row `['zucchini_quiche']` exists
  (`transition_items.js` 46).
- Cause: `F.assemble` in [cd_food.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creativedelight/cd_food.js)
  187-190 tests `s.press`, then `s.fill`; every JS array has `Array.prototype.fill`, so the meat array becomes a
  `create:filling` step with an empty fluid (`Can't modify the empty stack` in the owner's `logs/kubejs/server.log`,
  2026-10-29). Not fixed: cd_food.js last edited 2026-10-29 20:07, lines 188-189 unchanged on 2026-10-31.
- The quiche is the only `assemble` caller (grep 2026-10-31: no `burgerAssemble`, `wrapAssemble`, `goldenEnchant`
  callers either). Brewin's quiche assemblies (`brewinandchewin/recipes.js` 20-26, 65-73) are written inline and load
  (both ids in the 2026-10-30 recipe dump).

### Build
1. cd_food.js 187: put the array test first:
   ```js
   var seq = steps.map(s => {
     if (Array.isArray(s)) return e.recipes.create.deploying(inc, [inc, Ingredient.of(s)])
     if (s && s.press) return e.recipes.create.pressing(inc, inc)
     if (s && s.fill) return e.recipes.create.filling(inc, [inc, s.fill])
     return e.recipes.create.deploying(inc, [inc, Ingredient.of(s)])
   })
   ```
   Also update the header comment (cd_food.js 1-36) to say a step may be a list of alternatives.
2. `./gradlew :creativeDelight:build --offline`, `:creativeDelight:installToMods` with the game closed; restart.
3. Changelog: a "Fixed" bullet (the 1.1.04 changelog promised the assembly, it never loaded), e.g. under the quiche
   or baking family: "**Fixed:** The Zucchini Quiche sequenced assembly on a Pie Crust did not load".

### Gotchas
- Strings have no `fill`, so tag and item steps are safe; only arrays hit the branch.

### Tests
- FF-14 (exists): JEI shows the assembly; a Create line makes one quiche. Queue a boot grep for
  `zucchini_quiche_sequenced_assembly` errors (SQ-FFB-00 already greps CD script errors).

---

## baked-goods #1: Flour, crust and cake base in every baked recipe
Status In Progress / Work KubeJS / TODO `baking/baked-goods #1` (cooking plan sweet/baked, [oven.md](../oven.md) 2.6)

### Goal
Every baked recipe takes flour (or cornmeal), every pie sits on a pie crust, every cake starts from a cake base.

### Done (evidence 2026-10-31)
- Cookies, pies, cakes: the baking overhaul 2026-10-22 (oven.md 2.6, OV-25 to OV-31): bases, crust and frosting in
  [baking.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/baking.js) 94-118 (cake base raw 99-101, baked
  102-107; crust `3 flour/cornmeal + egg + milk` or `3 dough + milk` -> 3, 110-111; frosting 113). Every mod's recipe for
  a cake, pie, cookie, base or crust is swept (94-95 and each family call).
- Strange eclair wheat -> flour: [baked_goods.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/baked_goods.js)
  16-19 (with bread, adzuki bun, banana bread, scones).
- Frosting fluid and plain cake spout: baking.js 116-117 and 131 (baked base + 750 mB frosting; `dishes/ingredients #2`).
- Sequenced assembly with spouting for every cake: moved to row #34 (same work; see TODO discrepancies in the report).

### Still open
1. **Crust from a press:** `R.createPressing(i.pie_crust, i.any_dough, e, 'pie_crust_pressing')` in baking.js
   "Bases". No Create pressing recipe takes dough today (dump: only `create_factory:uncooked_waffle` presses cake paste;
   IE's metal press makes raw pasta from `#forge:dough/wheat`). 1 dough -> 1 crust is cheaper than the grid's 3 dough +
   milk -> 3 (the same rate, no milk): see Decisions.
2. **Dough block -> cake bases:** `R.fdCutting(i.dough_block, i.any_knife, Item.of(i.cake_base, 3), e,
   'cake_base_from_dough_block')`. Seven dough blocks exist since #27 (`i.dough_block` Create Compressed's, and
   `i.gingerdough_block` ... `i.sourdough_block`, item_dict 1853-1858); only the plain dough blocks should cut to bases
   (Create's, sweet potato, oat: the doughs in `#forge:dough`). A dough block on the board has no other knife recipe
   **(dump, unverified in JEI)**.

### Ids and data
`i.pie_crust` `farmersdelight:pie_crust`, `i.any_dough` `#forge:dough` (= `create:dough`, `veggiesdelight:sweet_potato_dough`,
`vintagedelight:oat_dough` **(dump)**), `i.cake_base` / `i.cake_base_baked` (`createaddition:*`), `i.dough_block` 202,
`i.sweet_potato_dough_block` 1855, `i.oat_dough_block` 1856.

### Decisions
- **Press yield.** Recommendation 1 dough -> 1 crust (the grid's rate without the milk; automation is meant to be cheaper).
- **Dough block -> how many bases.** Recommendation 3 (a base is 3 dough + egg + 2 sugar by hand; 3 per 9-dough block
  prices the missing egg and sugar at about 2 dough each).

### Tests (proposed)
- RC-176 Crust press: a dough under a Create press gives a pie crust; JEI lists it with the two grid crusts.
- RC-177 Dough block bases: Create's, sweet potato and oat dough blocks on a cutting board with a knife give 3 raw cake
  bases; the other dough blocks have no such recipe.

---

## baked-goods #2: Muffins as a decent food source
Status In Progress / Work KubeJS / TODO `baking/baked-goods #2` (cooking plan baked/muffins, [food_families.md](../food_families.md) 3.1)

### Goal
Muffins worth eating: more variants including two-fruit muffins with mixed, shorter effects (orange cranberry, lemon
poppy), "fruit, milk, egg, sugar, flour", served on paper, a sequenced assembly, and cornmeal accepted wherever flour is.

### Now
- `F.muffinBake(fruit, output)` ([cd_food.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creativedelight/cd_food.js)
  160-164): oven only, muffin tin, plated on paper, `[2 fruit, i.flour, i.t_milk_bottle, i.eggs, i.any_sweetener]` -> 4
  (`V.muffinsOven`, line 53). It removes the mods' Cooking Pot recipes (owner 2026-10-29, shipped in 1.1.05; Slice &
  Dice's mixer copies go with them). Takes **wheat flour only** (`i.flour`, line 163): the cornmeal ask is not built.
- Three muffins ([baking.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/baking.js) 143-145): Fruits
  Delight blueberry (5 / 0.4, night vision + brightening 20 s) and cranberry (5 / 0.4, shrinking 80 s), Veggies Delight's
  sweet potato cupcake, named Sweet Potato Muffin (row #32; its own sweet potato dough craft stays).
- No new muffin item, no muffin assembly. No poppy seed item; `i.poppy` is the flower.

### Build
1. Cornmeal: cd_food.js 163 `i.flour` -> `i.t_flour_or_cornmeal` (CD rebuild). Update OV-19 and the `cake/muffin` rule
   text in food_families.md ("wheat flour") when it lands.
2. New muffins as CD startup items (new `kubejs_startup/fruitsdelight/muffins.js`, the pack's own ids
   `creativeengineering:<name>_muffin`, routing per memory `creative-delight-routing`), each `.food(f => f.hunger(n)
   .saturation(m).effect(id, ticks, 0, 1))` and a texture `creativeengineering:item/<name>_muffin` recoloured from
   `fruitsdelight:item/blueberry_muffin`:
   - `orange_cranberry_muffin`: orange + cranberry; regeneration 10 s + shrinking 40 s.
   - `lemon_poppy_muffin`: lemon + poppy; haste 10 s + refreshing 10 s.
   - veggie muffins from row #32: carrot, zucchini, pumpkin (and the existing sweet potato).
   Dict keys next to `i.blueberry_muffin` (item_dict 12411); Fruits Delight effects as `ef.fd_<name>` in effect_dict.js.
3. Recipes: one `F.muffinBake` per single-fruit muffin; two-fruit muffins need a 2-flavour form: extend `muffinBake(fruit,
   output, second)` the way `cakeBake` takes `second` (cd_food.js 136-143).
4. Assembly: add to `muffinBake`: `F.assemble(i.paper, [flour-or-cornmeal, i.eggs, SWEET, fruit, fruit or second,
   { fill: Fluid.of(global.fluid.milk, 250) }], output)` -> yields 1 by the helper; for 4 per run pass the output as
   `Item.of(output, 4)` only if `incKey` resolves it (it reads the id, so it does). One transition row per muffin.
5. Food values: set every muffin together when `food/types-and-tuning #1` sets family values; proposed 6 / 0.6.

### Decisions
- **Variants** (TODO decision). Recommendation: the two combos, banana and strawberry singles, and the three veggie
  muffins from #32.
- Poppy: the flower (no new item). Recommendation: the flower.

### Gotchas
- Paper is the plating container (oven slot 8): automation must feed paper.
- KubeJS food effects take ticks (20 per second).

### Tests (proposed)
- OV-38 Muffin cornmeal: each muffin bakes with cornmeal in the flour slot.
- FF-15 New muffins: each new muffin bakes 4 on paper and assembles from paper; the two-fruit ones give both effects at
  the shorter length.

---

## baked-goods #4: Pie sequenced assembly and chorus pie
Status In Progress / Work KubeJS / TODO `baking/baked-goods #4` (cooking plan baked/pie, [food_families.md](../food_families.md) 3.1 `pie/whole` R6)

### Goal
Every obtainable pie can be made by Create sequenced assembly on a pie crust; the chorus pie's assembly spouts chorus
juice.

### Now
- No pie has an assembly: the family sweep (`pieBake` -> `F._sweep`, cd_food.js 135, 145-152) removed CCK's
  `sequenced_assembly/{apple,mulberry,pumpkin}_pie` and `sweet_berry_cheesecake` and FD's chocolate pie filling (dump
  2026-10-30: no pie assembly id; only quiche, shepherd's pie and horror lasagna assemblies). The family rule R5 "no other
  recipe" and the planned R6 "assembly for every pie" (food_families.md `pie/whole`) are this row.
- `PIES` ([baking.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/baking.js) 57-71): apple, chocolate,
  chorus, mulberry, pumpkin, rose hip, salmonberry, durian, pineapple, sweet berry cheesecake (Sweet Berry Pie), sweet
  potato, truffle, End's chorus fruit pie; oven: crust + 2 flavour + 2 sugar. Sponge Pie is a one-off (154-155).
- Chorus juice is a fluid now (`f.chorus_juice` `creativeengineering:chorus_juice`, fluid_dict 89; mixing, spout,
  drain and IE bottler in `endersdelight/recipes.js` 19-26; `food/drinks #4` built 2026-10-22).
- `F.assemble` (cd_food.js 183-193) builds an assembly from a step list; it needs #37's fix for list steps.

### Build
1. cd_food.js, new helper next to `pieBake`:
   ```js
   // crust -> flavour, sweet, loops 2 (= the oven's 2 flavour + 2 sugar)
   pieAssemble(flavour, output, steps) {
     F.assemble(i.pie_crust, steps || [flavour, SWEET], output, 2)
   },
   ```
   and call it from `pieBake` so every PIES row gets one (or from the PIES loop in baking.js, which keeps `pieBake` as is).
2. One `transition_items.js` row per pie key (`apple_pie`, `chocolate_pie`, ... `chorus_fruit_pie`); check each auto
   sprite (`<ns>:item/<path>`): FD pies and cheesecake have item sprites **(jar)**; the truffle pie uses FD's chocolate
   pie sprite (CD model), pass a texture override.
3. Chorus pie: `F.pieAssemble(i.chorus_fruit, i.chorus_pie, [i.chorus_fruit, { fill: Fluid.of(global.fluid.chorus_juice,
   250) }])` (loops 2: 2 fruit + 500 mB juice in place of the sugar; 250 mB juice = 1 chorus fruit + sugar).
4. CD rebuild, restart (new transition items).

### Ids and data
Pie keys as `PIES`; `i.pie_crust`, `f.chorus_juice`, `F.SWEET` (`i.any_sweetener`). Incomplete ids are
`kubejs:incomplete_<key>` (NCH `incomplete`, neoCraftHelper.js 1807). CCK's own `create_central_kitchen:incomplete_*`
items are unused and could be hidden.

### Decisions
- **Yield:** 1 pie per crust, 2 loops. Recommendation: as CCK had it.
- **Hand pies, tarts, Sponge Pie:** no assembly (oven-only rule). Recommendation: leave them out.

### Gotchas
- Two assemblies on one input whose first step is the same item shadow each other in Create: every pie starts with its
  own flavour, so never start one with the sweetener.

### Tests (proposed)
- FF-16 Pie assembly: every `PIES` pie assembles from a crust in a Create line (2 loops, flavour + sweetener); JEI lists
  one assembly per pie.
- FF-17 Chorus pie spout: the chorus pie assembly takes a chorus fruit and 250 mB chorus juice per loop.

---

## baked-goods #5: Cakes
Status In Progress / Work KubeJS / TODO `baking/baked-goods #5` (cooking plan baked/cake, scratchpad 2026-10-21)

### Goal
Four open parts: (a) a dragon egg cake, (b) rename the mangosteen "cake", (c) hide the unobtainable cake slices, (d) every
obtainable cake and pie slice placeable. (e) the chocolate cake merge is built (2026-10-22, item_merges.md 2.3, MG-09,
MG-10; CA's sprite, `neapolitan:chocolate_cake` merged by `kubejs/startup_scripts/_lib/item_merges.js`; baking.js 50 and
the spout 129).

### Now
- Cakes: `CAKES` ([baking.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/baking.js) 48-56) through
  `F.cakeBake` (cd_food.js 136-143): baked base + frosting + 2 flavour by hand, raw base in the oven (cake pan).
- (b) `fruitsdelight:mangosteen_cake` (`i.mangosteen_cake` 12461) is a sneak-placed bowl food, now a `CAKES` row (54);
  no rename in any lang file (grep 2026-10-31).
- (c) Cake slices with no cake in the pack: CCK `aloe_cake_slice`, `passionfruit_cake_slice`, `yucca_cake_slice`,
  `pumpkin_cake_slice`, `sweet_berry_cake_slice` (item_dict 5224-5246; CCK registers no block for them **(dump)**). Not in
  `global.removedItems` ([removed_items.js](../../kubejs/startup_scripts/_lib/removed_items.js); only CCK's
  `pumpkin_pie_slice` is, line 151). Pie slices are row #14.
- (d) Slices with a Display Delight plate: FD apple / chocolate pie, cake, sweet berry pie slices, Delightful pumpkin and
  salmonberry pie slices (food_families.md `pie/slice`, `cake/slice`). Every other slice has none.
- (a) Nothing exists. Dragon Loot 1.1.15 is in `mods/` (`i.dragon_scale` 6406); `f.dragon_breath`
  (`create_central_kitchen:dragon_breath`, fluid_dict 69). Own effects are registered like
  `kubejs/startup_scripts/magic/magic_resistance.js`.

### Build
1. (c) Add the five CCK cake slices to `global.removedItems` (restart): hidden in JEI, tags stripped. The `cake/slice`
   family table then drops them on the next `/cdevgen`.
2. (b) Rename in `kubejs/assets/creativedelight/kubejs/fruitsdelight/client.js` (new, the CD client pattern of
   `veggiesdelight/client.js`): `e.renameItem(i.mangosteen_cake, '<name>')`. If it stops being a cake, move it from
   `CAKES` to its own oven line (bowl as the plating container) and out of `cake/whole`.
3. (d) Placement: the cheap route is a coaster model per slice (format of
   `kubejs/assets/creativeengineering/brewinandchewin/coaster/farmersdelight__apple_pie_slice.json`, parent a Display
   Delight `plated_*` model with the pie's block textures), then rerun `tools/placeable_catalog.py`. The family rule asks
   for the Small Food Plate, which needs new placed blocks (CD or patcherMod, placeable_food.md 2): decide with
   `food/placeable`.
4. (a) Dragon egg cake: a CD startup item `creativeengineering:dragon_egg_cake` (`i.dragon_egg_cake`), a `CAKES`-style
   oven line `R.ceBaking([i.cake_base, i.frosting, i.dragon_breath, i.dragon_scale], ...)` with `F.ware.cake`, and a
   hand line on the baked base. A placeable cake block needs Java (a `CakeBlock` subclass in CD); flight needs a
   patcherMod `MobEffect` (sets `abilities.mayfly` while active, clears it on expiry unless creative or spectator).

### Decisions
- **Dragon egg cake effects** (TODO decision). Recommendation: no dragon egg in the recipe (one per world); regeneration
  II 30 s, resistance 120 s, slow falling 300 s; flight 60 s only if a patcherMod effect is wanted.
- **Mangosteen name.** Recommendation "Mangosteen Platter" (the sprite is fruit on a plate), out of the cake family.
- **Slice placement route.** Recommendation coaster now, Small Food Plate later with `food/placeable`.

### Gotchas
- `global.removedItems` is read at startup; recipes using a removed item are logged, not removed.
- Placed non-vanilla cakes don't count for Spice of Life (`food/types-and-tuning #5`).

### Tests (proposed)
- FF-18 Dead cake slices: the five CCK cake slices are absent from JEI.
- FF-19 Mangosteen: JEI and the tooltip show the new name; its recipe follows the decision.
- FF-20 Dragon egg cake: bakes in the cake pan, eats with the chosen effects, never takes the dragon egg.
- PL-15 Slices placed: each slice in (d) sits on a coaster with its pie's or cake's look.

---

## baked-goods #6: Cake paste and tarts
Status In Progress / Work KubeJS / TODO `baking/baked-goods #6` (cooking plan baked, [food_families.md](../food_families.md) 3.1)

### Goal
Cake paste can be made in the Cooking Pot; waffles get more recipes and uses; every tart becomes a pie (renamed and
retextured, owner 2026-10-21), with more variants.

### Now
- `create_factory:cake_paste` (`i.cake_paste` 5348): only Create Factory's heated mixing (sugar, `create:dough`, egg,
  250 mB milk) **(jar)**; nothing in the pack touches it (grep 2026-10-31). `uncooked_waffle` presses from paste
  (`create_factory:uncooked_waffle`, dump), `waffle` smokes, flavoured waffles spout 250 mB onto a waffle.
- Tarts: `F.tartBake` ([baking.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/baking.js) 149-150,
  cd_food.js 170-173): fruit + crust + egg + sugar + milk bottle -> 2 on the baking sheet; the Fruits Delight pot
  recipes stay. No rename yet (no lang entry). The filling cans are already Canned Fig / Lemon Pie Filling.
- food_families.md `pie/small` R4 "Planned: every tart becomes a pie".

### Build
1. Pot cake paste: `R.fdCooking([i.any_sweetener, i.any_dough, i.eggs, i.t_milk_bottle], i.cake_paste, e,
   'cake_paste_manual_only', 0.1, 200)` (the suffix keeps Slice & Dice from adding a mixer twin of Create Factory's);
   swap the mixer's egg and dough to `i.eggs` / `i.any_dough` with `R.replaceInput({ id: 'create_factory:cake_paste' },
   ...)`. File: a new `kubejs/data/creativedelight/kubejs/create_factory/recipes.js` (`// requires: create_factory`).
2. Waffles: oven `R.ceBaking([i.uncooked_waffle], Item.of(i.waffle, 1), e, 'waffle', F.ware.sheet)` and
   `R.ceBaking([i.cake_paste], ...)` from paste in one bake; keep the smoker (dessert/waffle rule: "Oven for the plain
   bake; toppings by Spout"). Same work as #36's waffle line: build once.
3. Tarts -> pies: rename in a CD client file (`fruitsdelight/client.js`): Lemon Tart -> Lemon Pie, Fig Tart -> Fig Pie;
   retexture in kubejs assets (a pie sprite recoloured from FD's apple pie); move both from `tartBake` to `PIES`-style
   rows only if they become whole placed pies (they are single items: see Decisions).
4. More variants: new pie items for blueberry, peach, orange (`#forge:fruits/*` exist in Fruits Delight **(jar)**) on
   the same route as the renamed tarts.

### Decisions
- **Tart -> pie shape.** The tarts are hand-held, 2 per bake. Recommendation: keep them as small pies (`pie/small`,
  baking sheet, 2 per bake) with the new names, the same treatment as #12's durian and pineapple pies.
- Which new fruits. Recommendation: blueberry, peach, orange.

### Tests (proposed)
- RC-178 Cake paste pot: sweetener, dough, egg and milk bottle in the Cooking Pot give cake paste; JEI shows no new mixer
  copy.
- OV-39 Oven waffle: uncooked waffle (or cake paste) + baking sheet -> waffle.
- FF-21 Tarts as pies: Lemon and Fig show their pie names and sprites; each new fruit pie bakes 2 per batch.

---

## baked-goods #7: Sourdough chain
Status In Progress / Work KubeJS / patcherMod / TODO `baking/baked-goods #7` (cooking plan baked/sourdough, [food_families.md](../food_families.md) 3.1)

### Goal
Starter -> growing -> fermenting -> sourdough with a timed wait between steps; the sprite grows through the chain;
milling or the mortar turns sourdough back into starter; a starter keeps its start date and a name and passes both to any
sourdough made from it. Sourdough counts as any dough in recipes (scratchpad 2026-10-22).

### Now
- [pneumaticcraft/recipes.js](../../kubejs/data/creativedelight/kubejs/pneumaticcraft/recipes.js) 25-46: PNC's
  `sourdough` replaced by a shapeless yeast bucket + 8 dough -> **1** (line 26; PNC's own gave 8 from 8 flour, so likely a
  bug); smoking -> sourdough bread (29); oven `sourdough_bread_1/3/6` (30-35, baking sheet, row #31); knife on sourdough
  bread -> 3 bread (38); heated mixing 500 mB yeast + 8 dough -> 8 (42); IE bottler 50 mB + dough -> 1 (46).
- Sourdough is **not** in `#forge:dough` (dump: Create, sweet potato, oat only). Owner rule 2026-10-28 (row #31): only
  doughs with no bread of their own bake into plain bread; putting sourdough in `#forge:dough` would make
  `bread_from_dough_1/3/6` turn it into plain bread.
- Tools: KubeJS `modifyResult` on crafting recipes (returns empty to block a craft); patcherMod's
  `CalendarDate.worldDay(Level)` (time.md 3.7) through `Java.loadClass`; the Brewin' keg (`R.bncFermenting`).

### Build (recommended route: KubeJS, lazy timestamps)
1. Items (CD startup, `kubejs_startup/pneumaticcraft/sourdough.js`): `creativeengineering:sourdough_starter`,
   `growing_sourdough_starter`, `fermenting_sourdough` (three sprites, stack size 1 so each carries NBT).
2. Recipes (server, pneumaticcraft/recipes.js): start = 2 flour + water bottle + yeast with `.modifyResult` writing
   `{ Started: worldDay }` (`i.water_bottle` item_dict 1305); feed = starter + flour + water -> growing starter, blocked
   until a day passed, copying `Started` and the custom name; ferment = growing starter + 8 dough -> fermenting sourdough
   (`keepIngredient` keeps the starter); bake-ready = fermenting sourdough -> 8 sourdough with lore "From <name>'s
   starter, day N". Client tooltip from the NBT.
3. Back to starter: `R.createMilling(i.sourdough_starter, i.sourdough, e, 'sourdough_starter_milling')`, and
   `R.mortar` / `foodGrinding` (NCH, `cooking/kitchen #10`, built).
4. "Sourdough as any dough": add sourdough to the dough slots by recipe (`R.replaceInput` of `i.any_dough` with
   `[i.any_dough, i.sourdough]` outside the bread bakes), not to the tag. See #17.
5. Fix line 26 to 8 sourdough, or remove it once the chain replaces it.

### Decisions
- **How the timed wait works** (TODO decision): (A, recommended) NBT timestamp + `modifyResult`, no Java, SUN clock;
  (B) Brewin' keg per stage (a real timer, but no name or date carries); (C) a CD item class that ticks in inventory.
- Yeast form: a yeast culture bottle (filled from the fluid) per starter. Recommendation yes (see #8).
- Wait: 1 world day per step.

### Gotchas
- `modifyResult` runs on the server; the client result slot can flicker. Test on a dedicated server.
- `getPlayer()` is null for autocrafters: treat null as "block" so the chain stays hand-made.

### Tests (proposed)
- RC-179 Starter: crafting stamps the day; feeding the same day gives nothing; a day later it works and keeps the name.
- RC-180 Sourdough from a starter: 8 sourdough named after the starter; the starter stays in the grid.
- RC-181 Sourdough hand craft: yeast bucket + 8 dough gives 8 (or the recipe is gone).

---

## baked-goods #8: Yeast
Status In Progress / Work KubeJS / TODO `baking/baked-goods #8` (cooking plan baked/yeast, [food_families.md](../food_families.md) 3.1)

### Goal
Yeast culture from mushroom colonies or blocks by crafting and heated mixing; fix the in-world yeast process; yeast feeds
the sourdough chain (#7).

### Now (unchanged 2026-10-31: no yeast recipe in the pack's scripts)
- PNC's fluid `pneumaticcraft:yeast_culture` (`f.yeast_culture`, tag `#forge:fluid/yeast_culture` in
  `server_scripts/tags/fluidtags.js` 43), `i.yeast_culture_bucket` (item_dict 207). Sources **(jar)**: thermo plant,
  `#forge:mushrooms` + 1000 mB water -> 250 mB; in world: sugar item entities in a culture source block spread it into
  an adjacent water source when the fluid ticks (exact `Items.SUGAR`). `in_world_yeast_crafting = true`
  (`config/pneumaticcraft-common.toml` 255).
- What "fix the in-world process" means is not written down. Candidates: ItemPhysic sinks sugar (item_physics.md 2) so
  it rests below the culture block; glycerol and other sweeteners are refused; the tick runs only when the water is placed.

### Build
1. In game first: culture bucket in a 1-deep pool, drop sugar in, place water next to it; note what fails.
2. Recipes (`kubejs/data/creativedelight/kubejs/pneumaticcraft/recipes.js`): `R.shapeless(i.yeast_culture_bucket,
   [i.water_bucket, [i.brown_mushroom_colony, i.red_mushroom_colony], i.any_sweetener], e, 'yeast_culture_bucket')` with
   `.consumeIngredient(i.water_bucket)` (no spare empty bucket); `R.createMixing(Fluid.of(f.yeast_culture, 500),
   [Fluid.of(f.water, 500), [colonies, mushroom blocks], i.any_sweetener], e, 'yeast_culture_mixing', 'heated')`.
3. If sugar sinking is the problem, float sugar in `server_scripts/tags/item_physics_tags.js`; if it is the tick timing,
   document it as PNC's design in recipes_cooking.md.

### Ids and data
`i.brown_mushroom_colony` 671, `i.red_mushroom_colony` 670 (Fungi Delight's inky cap and morel colonies 14549, 14559 could
join), `i.water_bucket` 201.

### Decisions
- Yield. Recommendation 1 colony -> 1 bucket by hand, 500 mB per batch in the mixer.

### Tests (proposed)
- RC-182 Yeast crafting: colony + water bucket + sugar gives one yeast culture bucket and no spare empty bucket.
- RC-183 In-world yeast: sugar in a culture pool and water placed next to it turns the water to culture.

---

## baked-goods #9: Sliced bread / toast in recipes
Status In Progress / Work KubeJS / TODO `baking/baked-goods #9` (cooking plan baked/sliced bread, scratchpad 2026-10-21)

Superseded by row #28 (owner 2026-10-29, built, shipped in 1.1.05): any bread works in any bread recipe, no burger, bun or
square-slice restrictions. [bread_recipes.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/bread_recipes.js)
swaps every vanilla-bread / bread-slice slot for `#forge:bread` (loaves, flavoured breads, slice, toast); toast-only slots
stay toast; RC-170, RC-171 ([recipes_cooking.md](../recipes_cooking.md)). The 2026-10-21 per-sandwich audit (slices vs
rolls vs buns) is dropped. What could remain: a burger bun item (oven, dough + egg -> 4 on the baking sheet, in
`#forge:bread`), only if the owner wants one for looks; it would not be required by any recipe. Recommendation: close the
row as superseded by #28.

---

## baked-goods #10: Bread block
Status In Progress / Work KubeJS / TODO `baking/baked-goods #10` (cooking plan misc, [food_families.md](../food_families.md) 3.1)

### Goal
The crustless bread block comes from cutting a bread block with a knife; the bread slab has the standard 3 -> 6 craft.

### Now
[architects_palette/recipes.js](../../kubejs/data/creativedelight/kubejs/architects_palette/recipes.js) 15-29: removes
Architect's Palette's `bread_block`, `bread_slab` and `stonecutting/bread_nub`; smokes Create Compressed's dough block into
a bread block (19); Create saw bread block -> 16 bread (22); **knife on a bread block -> 16 bread** (25); stonecutter bread
block -> slab (27), -> crustless block (28), slab -> nub (29).

### Build
1. Line 25: knife -> `i.crustless_bread_block` (FD's board takes the first matching recipe, so the two knife recipes can't
   coexist); bread stays on the saw (22).
2. Drop the stonecutter crustless recipe (28): the knife is the "cut". Keep slab and nub on the stonecutter.
3. `R.shaped(Item.of(i.bread_slab, 6), ['BBB'], { B: i.bread_block }, e, 'bread_slab')`.

### Gotchas
- Lines 22 and 25 output `Item.of(i.bread, 16)` with `i.bread` the tag `#forge:bread/wheat`: use `i.minecraft_bread`
  for outputs while touching them.
- The dough block smoking (19) predates the oven bread rule (#31: dough bakes in the oven); leave it unless the owner
  wants dough blocks in the oven too.

### Tests (proposed)
- RC-184 Bread block: a knife on a bread block on the cutting board gives a crustless bread block; 3 bread blocks make
  6 slabs; the saw still gives 16 bread.

---

## baked-goods #12: Durian and pineapple pie rework
Status In Progress / Work KubeJS / TODO `baking/baked-goods #12` ([oven.md](../oven.md) 2.6, [food_families.md](../food_families.md) 3.1)

### Goal
Fruits Delight's durian and pineapple "pies" (hand-held items, no placed block or slices) get a shape that fits: a real
4-slice pie, or a small pie with its own name, recipe and can.

### Now
- `fruitsdelight:durian_pie`, `pineapple_pie` (item_dict 12422, 12486): item sprites only, no block textures **(jar)**;
  5 hunger each. In `PIES` ([baking.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/baking.js) 65-66):
  crust + 2 durian flesh / pineapple slice + 2 sugar -> 1 in the pie tin (a whole pie's cost); Fruits Delight's pot
  recipes (2 per batch) are swept.
- Cans: `canned_durian_pie_filling`, `canned_pineapple_pie_filling` (`kubejs/generation_scripts/food/can_types.json`,
  section `pie`, 4 servings, 5 / 0.4, the pies' effects); made in the pot from 2 fruit + 2 sugar (`pieBake` can route) and
  baked with a crust back into 1 pie (`_from_filling`). food_families.md lists both in `pie/small`.

### Build (recommended: small pies)
1. Move both from `PIES` to their own lines: oven, baking sheet, crust + 2 fruit + 2 sugar -> 2 (`F.ware.sheet`), so a
   crust gives two hand pies like the tarts.
2. Can: keep the 4-serving filling can (pot 2 fruit + 2 sugar); crust + full can -> 4 small pies on the baking sheet.
3. Names: "Durian Hand Pie", "Pineapple Hand Pie" in a CD client file (`fruitsdelight/client.js`, shared with #5 and #6).
4. food_families.md: they stay in `pie/small`; update the R4 can note.

### Decisions
- **What each becomes** (TODO decision). Recommendation: small pies, 2 per bake, as above; a 4-slice placed pie needs a
  new block (CD Java), block textures and slice items for two fruits the pack hardly uses.
- **Yield and can.** Recommendation 2 per crust; a full can bakes 4.

### Tests (proposed)
- FF-22 Durian and pineapple: each bakes 2 on the baking sheet from crust + 2 fruit + 2 sugar; a full filling can + crust
  gives 4; the pie tin recipe is gone.

---

## baked-goods #13: Cherry pie
Status In Progress / Work patcherMod / KubeJS / TODO `baking/baked-goods #13` ([oven.md](../oven.md) 2.6, [food_families.md](../food_families.md) 3.1; decided 2026-10-29: add item, slice, block)

### Goal
A whole cherry pie that places Create Central Kitchen's `create_central_kitchen:cherry_pie` block (the whole pie is
Environmental's, not in the pack), on the pie family's recipe, cut into 4 slices, with a filling can. Folded scope:
cherry jelly / jam share the cherry route (jam: `dishes/jams #4`).

### Now
- CCK has the block (blockstate, models `block/cherry_pie*`, textures `block/cherry_pie_{top,inner,side,bottom}`), the
  slice `create_central_kitchen:cherry_pie_slice` (`i.cherry_pie_slice` 5225) and `incomplete_cherry_pie`; no whole item
  **(jar)**. Its own recipes (`crafting/cherry_pie`, `cutting/cherry_pie_slice`, `sequenced_assembly/cherry_pie`) sit in its
  built-in `farmersdelight` datapack, gated on Environmental.
- The truffle pie is the same case, built 2026-10-31: [truffle_pie_item.js](../../development/creativeDelight/src/main/resources/kubejs_startup/create_central_kitchen/truffle_pie_item.js)
  registers a `BlockItem` under CCK's id for CCK's block (`e.createCustom`), item model
  `development/creativeDelight/src/main/resources/assets/create_central_kitchen/models/item/truffle_pie.json`, name in
  `kubejs/assets/creativedelight/kubejs/create_central_kitchen/names.js`, `PIES` row with no can (baking.js 69).
- `i.cherry` = `vinery:cherry` (item_dict 11209). No `canned_cherry_pie_filling`.

### Build
1. Item: add `create_central_kitchen:cherry_pie` to truffle_pie_item.js (loop over both ids); dict key `i.cherry_pie`
   next to `i.truffle_pie` (item_dict 14574); item model in CD assets; texture in kubejs assets (decided 2026-10-29), e.g.
   `kubejs/assets/create_central_kitchen/textures/item/cherry_pie.png`; name "Cherry Pie" in names.js.
2. Recipe: `['cherry_pie', i.cherry, 'canned_cherry_pie_filling']` in `PIES` (crust + 2 cherries + 2 sugar, pie tin; the
   pot filling and `_from_filling` come with the row). Slices: `F.pieSlice(i.cherry_pie, i.cherry_pie_slice)` if CCK's
   cutting recipe stays gated (it is: Environmental) **(unverified: check JEI after the item exists)**.
3. Can: a `canned_cherry_pie_filling` row in the can generator (`cans/canning`), section `pie`, 4 servings.
4. Restart (startup item) and `/cdevgen` for the can.

### Decisions
- Resolved 2026-10-29: add item, slice and block.

### Gotchas
- The block's pick-block and drops may still point at Environmental's missing item; check that middle-click and breaking
  a full pie give the new item (the truffle pie needs the same check).

### Tests (proposed)
- FF-23 Cherry pie: bakes in the pie tin from crust + 2 cherries + 2 sugar; places CCK's block; a knife cuts 4 slices;
  the filling can bakes back into a pie.

---

## baked-goods #14: Unobtainable pie slices
Status In Progress / Work KubeJS / patcherMod / TODO `baking/baked-goods #14` ([oven.md](../oven.md) 2.6, [can_parity.md](../can_parity.md), [food_families.md](../food_families.md) 3.1)

### Goal
Every pie slice in the pack can be obtained, or is hidden.

### Now (2026-10-31)
- CCK cherry: row #13. CCK truffle: obtainable since 2026-10-31 (whole item, `ports/fungiport #3`; baking.js 69).
- Delightful: `blueberry_pie_slice`, `crimson_berry_pie_slice`, `gloomgourd_pie_slice`, `green_apple_pie_slice`,
  `nightshade_berry_pie_slice`, `source_berry_pie_slice` (item_dict 11347-11440). Their whole pies are BYG, The
  Undergarden and Ars Nouveau items (absent), but Delightful **registers the placed blocks**: `delightful:<x>_pie` appear
  in the block tag dump, and the upstream jar has their blockstates and block textures **(dump, jar)**. So each can get a
  whole item the truffle pie way. `config/delightful-common.toml` 65-73 enables all eight slice overhauls.
- Fruit sources in the pack: blueberry only (`i.t_fruits_blueberry`, Fruits Delight). No crimson berry, nightshade
  berry, green apple, gloomgourd or source berry item (their crates and sacks are dead too: `i.green_apple_crate`,
  `i.crimson_berry_sack`, `i.nightshade_berry_sack`).
- None has a can. CCK's `pumpkin_pie_slice` is already in `global.removedItems` (line 151).

### Build
1. Blueberry: a CD startup block item for `delightful:blueberry_pie` (new `kubejs_startup/delightful/pie_items.js`, the
   truffle_pie_item.js pattern), dict key, item model and texture, name; `['blueberry_pie', i.t_fruits_blueberry,
   'canned_blueberry_pie_filling']` in `PIES`; a filling can row.
2. The other five: add the slices to `global.removedItems`
   ([removed_items.js](../../kubejs/startup_scripts/_lib/removed_items.js)); optionally set their config switches to false
   (whether that unregisters the item is **(unverified)**). The dead crates and sacks can go with them.
3. Restart; `/cdevgen` (can parity, families).

### Decisions
- **Add or hide, per slice** (TODO decision). Recommendation: add blueberry (a fruit the pack has, a block that exists);
  hide crimson berry, nightshade berry, green apple, gloomgourd and source berry.

### Tests (proposed)
- FF-24 Pie slices: the blueberry pie bakes, places and cuts into 4 slices; the five hidden slices are absent from JEI.

---

## baked-goods #17: Dough unification
Status In Progress / Work KubeJS / TODO `baking/baked-goods #17` ([veggies_delight.md](../veggies_delight.md) 5.2, [food_families.md](../food_families.md) 3.1)

### Goal
One dough system across the mods: which doughs count as "any dough", which stay distinct, each with its own texture and
block.

### Now (2026-10-31)
- `#forge:dough` (`i.any_dough`) = `create:dough`, `veggiesdelight:sweet_potato_dough`, `vintagedelight:oat_dough` (oat
  added in `vintagedelight/tags.js` 12) **(dump)**; `#forge:dough/wheat` (`i.dough`) = `create:dough`. FD's `wheat_dough` is
  not in the pack's tag dump.
- Distinct doughs: `creativeengineering:cookie_dough` (dough + 2 sugar), Create Confectionery `gingerdough`, Vintage
  `cucumber_dough`, PNC `sourdough`, Farmer's Respite `nether_wart_sourdough`.
- Dough blocks and textures: built by #27 (2026-10-25; six blocks, oven.md 2.6, OV-35). Uncooked mhadjeb is removed
  (`dishes/meals #32`).
- Owner rule 2026-10-28 (#31): only doughs with no bread of their own bake into plain bread; the tag is exactly those.

### Build
1. Keep `#forge:dough` as the plain doughs (no change). Sourdough "as any dough" (#7): add it slot by slot with
   `R.replaceInput(..., i.any_dough, [i.any_dough, i.sourdough])`, skipping the bread bakes and dough blocks.
2. Item textures per dough: the open half of the folded scope; recoloured sprites in `kubejs/assets/<ns>/textures/item/`
   for doughs that share Create's look **(unverified which do: check the sprites)**.
3. Veggies Delight's sweet potato dough still smelts, smokes and campfires into bread (`veggiesdelight:bread_from_*`,
   row #31 note): sweep those with `F._sweep`-style removal so it bakes in the oven like every dough.

### Decisions
- **Which doughs stay distinct** (TODO decision). Recommendation: cookie dough, gingerdough, cucumber dough (it is pasta
  dough) and both sourdoughs stay distinct; Create, sweet potato and oat are "any dough".
- Sweet potato dough smelting routes: recommendation remove (oven only, as the owner ruled for oat dough).

### Tests (proposed)
- RC-185 Doughs: JEI `#forge:dough` lists Create, sweet potato and oat dough; sourdough fills a dough slot where the
  decision allows; no furnace, smoker or campfire recipe makes bread from sweet potato dough.

---

## baked-goods #18: Brownie and cupcake baking types
Status In Progress / Work KubeJS / data / TODO `baking/baked-goods #18` (scratchpad 2026-10-22, [food_families.md](../food_families.md) 3.1)

### Goal
A brownie baking type: brownies made in the oven as a tray that is cut into pieces. The cupcake half is dropped: row #32
merged cupcakes into muffins (decided 2026-10-29).

### Now
- Family `dessert/brownie` (food_families.md, rules: tray as its own block cut into brownies; "Oven: the tray (cake pan);
  Cutting Board: tray -> brownies"): Beetroot Brownie Tray `veggiesdelight:beetroot_brownie_tray` (14306, shapeless 3
  beetroot, 2 cocoa, 2 sugar, 2 eggs **(jar)**), Beetroot Brownie (14307, cut from the tray), Hostile Delight's Magma
  Brownie (blaze powder, carrot, magma cream, ardent sauce) and Slime Brownie (candied slime, melon slice, 2 refined
  gelatin), both shapeless singles **(jar)**. None has an oven route.

### Build
1. cd_food.js: `brownieBake(flavours, output)`: sweep, oven `[i.t_flour_or_cornmeal, i.eggs, SWEET, n(i.cocoa_beans, 2)]
   .concat(flavours)` within 6 slots, `ware.cake` (or `ware.dish` once #30 lands), 1 tray; hand route `_manual_only` on the
   same inputs if the family keeps a grid recipe.
2. Beetroot: `F.brownieBake([i.beetroot], i.beetroot_brownie_tray)` (one beetroot slot; the 3 beetroot of the original
   need `F.slot(i.beetroot, 3)` and 1 cocoa to fit 6 slots).
3. Hostile's brownies: singles, baking sheet, their own odd fillings (as `handPieBake` does for its pies).

### Decisions
- **Recipes, textures** (TODO decision). Recommendation: the oven tray for beetroot; the Hostile brownies as single
  bakes; no new textures (all three have sprites).

### Tests (proposed)
- FF-25 Brownies: the beetroot tray bakes in the oven and cuts into brownies; the two Hostile brownies bake on the
  baking sheet; their grid recipes follow the decision.

---

## baked-goods #19: Quiche baking type
Status In Progress / Work KubeJS / data / TODO `baking/baked-goods #19` (scratchpad 2026-10-22, [food_families.md](../food_families.md) 3.1)

### Goal
Quiche as a baking type: a pie variant for vegetables and eggs, with its own family rule and more variants.

### Now
- Family `quiche` exists (food_families.md 4.6, applied 2026-10-27): rule "shaped recipe on a pie crust + one sequenced
  assembly on a pie crust (as Brewin' quiche)"; slices cut on the board, Small Food Plate planned.
- Members: Brewin' `quiche` (shaped `quiche_manual_only` and two assemblies, `brewinandchewin/recipes.js` 20-26, 58-73);
  Veggies Delight `zucchini_quiche` (shaped VD recipes + the assembly that fails, #37). No oven route.

### Build
1. Fix #37.
2. Optional oven route per quiche: pie tin, crust + 2 eggs + milk + cheese + 2 filling (6 slots), through a new
   `F.quicheBake(fillings, output)` in cd_food.js beside `pieBake`.
3. New variants (decision): each a CD startup block item needs a placed block, so variants come from mods first.

### Decisions
- **Recipes, textures** (TODO decision). Recommendation: keep the family rule (grid + assembly), add the oven route for
  both quiches, no new quiche items until a mod brings one.

### Tests (proposed)
- FF-26 Quiche oven: both quiches bake in the pie tin from crust, eggs, milk, cheese and filling.

---

## baked-goods #20: Pizza food type
Status In Progress / Work KubeJS / data / TODO `baking/baked-goods #20` (scratchpad 2026-10-22, [food_families.md](../food_families.md) 3.1)

### Goal
Pizza as a food type with one recipe shape across the four pizzas.

### Now
- Family `pizza` exists (food_families.md): rule "one shaped recipe + one sequenced assembly (as Brewin' pizza)"; all
  four members fail it (no assembly). Slices: cutting board; Small Food Plate planned.
- Members: Brewin' `pizza` (Supreme Pizza, shaped on dough, `brewinandchewin/recipes.js` 33-37); Vintage `cheese_pizza`
  (3 cheese over dough + tomato sauce + dough), `meat_pizza`; Veggies `vegan_pizza` (bell pepper, vegetable, onion over
  dough + sauce + dough) **(jar)**. Ids item_dict 11858, 14328, 14412, 14451.

### Build
1. Per pizza: `F.assemble(i.any_dough, [i.tomato_sauce, toppings...], output)` (the pizza's own toppings as steps;
   cheese as `i.t_cheese_wedge`), plus a transition row per pizza.
2. Oven route (decision): `F.pizzaBake(toppings, output)` on the baking sheet: dough + sauce + up to 4 toppings.
3. Keep the shaped recipes (family rule).

### Decisions
- **Oven or sequenced assembly** (TODO decision). Recommendation: the assembly (the family rule) now; the oven route only
  if the owner wants pizza baked like the other baked families.
- Textures: none needed for existing pizzas.

### Tests (proposed)
- FF-27 Pizza: each pizza assembles on a dough in a Create line; the shaped recipes stay.

---

## baked-goods #21: Lasagna baking type
Status In Progress / Work KubeJS / data / TODO `baking/baked-goods #21` (scratchpad 2026-10-22, [food_families.md](../food_families.md) 3.1)

### Goal
Lasagna as a baking type, baked in the baking dish (#30).

### Now
- Family `lasagna` exists: `lasagna/whole` Veggies Delight `lasagna_pan` (14352; a feast block, `LasagnaPanBlock`;
  Cooking Pot: 2 dough, tomato sauce, milk, a raw meat, a vegetable, bowl container, 400 ticks **(jar)**; rule "Cooking
  Pot (Mixer mirrors it)"); `lasagna/serving` VD `lasagna_slice` (from the pan) and Brewin' `horror_lasagna` (a bowl dish:
  pot, grid `horror_lasagna_cooked_manual_only`, assembly `horror_lasagna_sequenced_assembly`).

### Build (after #30)
1. Oven: `R.ceBaking([n(i.pasta, 2), i.tomato_sauce, i.t_cheese_wedge, i.milk, meat], Item.of(i.lasagna_pan, 1), e,
   'lasagna_pan', F.ware.dish)` (6 slots; `i.pasta` is every raw pasta); sweep VD's pot recipe or keep it as the second
   route (decision).
2. The pan stays the placed block; servings are taken with a bowl as VD has it.

### Decisions
- **Dish vs pan** (TODO decision). Recommendation: the baking dish is the oven tool, the VD lasagna pan stays the placed
  block; dough becomes raw pasta (the IE press and the board make it from dough).
- Keep the Cooking Pot route? Recommendation: remove it (baked families are oven-made).

### Tests (proposed)
- FF-28 Lasagna: the lasagna pan bakes in the oven with a baking dish in the tool slot and serves slices with a bowl.

---

## baked-goods #22: Chorus cookie from the uncanny cookie
Status In Progress / Work KubeJS / resource pack / TODO `baking/baked-goods #22` ([recipes_cooking.md](../recipes_cooking.md), [food_families.md](../food_families.md) 3.1)

### Goal
The chorus cookie is made from an uncanny cookie.

### Now
- End's Delight is integrated (CD member): `i.chorus_cookie` `ends_delight:chorus_cookie` (item_dict 12324),
  `i.chorus_fruit_grain` (12327). The baking audit 2026-10-27 put it in `COOKIES`
  ([baking.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/baking.js) 81): cookie dough + 2 chorus fruit
  grain -> 12, End's own recipe swept. Not from the uncanny cookie (food_families.md 4.6 says so).
- Uncanny cookie `endersdelight:uncanny_cookies` (12049): cookie dough + 2 mite crust -> 12 (baking.js 76).

### Build
Take `chorus_cookie` out of `COOKIES` and add `F._sweep(i.chorus_cookie)` plus one recipe on the uncanny cookie: oven,
baking sheet, `[n(i.uncanny_cookies, 4), i.chorus_fruit_grain]` -> 4 (a dip, not a new batch), or a hand craft
`R.shapeless(Item.of(i.chorus_cookie, 8), [8 uncanny cookies, i.chorus_fruit_grain], e, 'chorus_cookie_manual_only')`.

### Decisions
- Recipe shape not stated. Recommendation: the hand craft, 8 uncanny cookies + 1 chorus fruit grain -> 8 (the dye
  pattern; cookies stay oven-made at the base).

### Tests (proposed)
- RC-186 Chorus cookie: 8 uncanny cookies + a chorus fruit grain craft 8 chorus cookies; the cookie dough recipe is gone.

---

## baked-goods #23: Oatmeal cookies from oat dough
Status In Progress / Work KubeJS / resource pack / TODO `baking/baked-goods #23` ([recipes_cooking.md](../recipes_cooking.md), [food_families.md](../food_families.md) 3.1)

### Goal
The oatmeal cookie uses oat dough. Oatmeal raisin cookie: oat dough + 1 raw oat + 1 grape, textured from the oatmeal
cookie.

### Now
- `['oatmeal_cookie', i.raw_oats]` in `COOKIES` ([baking.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/baking.js)
  78): cookie dough (any dough + 2 sugar; oat dough counts, it is in `#forge:dough`) + 2 raw oats -> 12. Unchanged
  2026-10-31.
- `i.oat_dough` (14463), `i.raw_oats` (14484), `i.oatmeal_cookie` (14466). No raisin item; grapes `i.red_grape` 11273,
  `i.white_grape` 11303 (Vinery). No oatmeal raisin cookie item.

### Build
1. Take `oatmeal_cookie` out of `COOKIES`; `F._sweep(i.oatmeal_cookie)`; oven `R.ceBaking([i.oat_dough, F.SWEET, F.SWEET,
   n(i.raw_oats, 2)], Item.of(i.oatmeal_cookie, 12), e, 'oatmeal_cookie', F.ware.sheet)`.
2. New CD startup item `creativeengineering:oatmeal_raisin_cookie` (`kubejs_startup/vintagedelight/`, texture recoloured
   from `vintagedelight:item/oatmeal_cookie`, food values copied from it), dict key, jar tag (`jar_tags.js`, every cookie
   is jarable), food type; oven `[i.oat_dough, F.SWEET, F.SWEET, i.raw_oats, [i.red_grape, i.white_grape]]` -> 12.

### Decisions
- Sugar with oat dough (oat dough has none). Recommendation: keep 2 sweetener so it matches cookie dough's cost.
- Grape: either. Recommendation: either grape (a list slot).

### Tests (proposed)
- RC-187 Oatmeal cookie bakes from oat dough, 2 sugar and 2 raw oats. RC-188 Oatmeal raisin cookie bakes from oat dough,
  2 sugar, a raw oat and a grape, and goes in a jar.

---

## baked-goods #24: No shapeless pie-to-crust recipes
Status In Progress / Work KubeJS / TODO `baking/baked-goods #24` ([recipes_cooking.md](../recipes_cooking.md))

### Goal
Remove the shapeless recipes that turn a pie into a crust.

### Now (checked 2026-10-31)
The 2026-10-30 crafting dump (`kubejs/exported/recipes/crafting_audit.json`) has exactly two recipes with a pie crust
result: `creativedelight:shapeless/pie_crust_flour_manual_only` and `pie_crust_dough_manual_only`; `all.csv` has no other
crust recipe of any type. baking.js 94-95 sweeps every crust-making recipe but the pack's. Nothing to remove.

### Build
None. Close the row after one JEI look (pie crust -> R: the two grid crafts only). SQ-INV-11 and CQ-DUMP-06 in the
scratch queue ask the same scan and can be marked answered.

### Tests
- RC-189 (proposed) JEI on pie crust: only the flour and the dough grid crafts.

---

## baked-goods #32: Merge muffin and cupcake
Status In Progress / Work KubeJS / lang / resource pack / TODO `baking/baked-goods #32` (decided 2026-10-29: merge into muffins)

### Done
- Sweet Potato Cupcake named Sweet Potato Muffin (`kubejs/assets/creativedelight/kubejs/veggiesdelight/client.js` 6-7,
  id `veggiesdelight:sweet_potato_cupcake` kept) and muffins out of the Cooking Pot (`F.muffinBake`, cd_food.js 160-164):
  both shipped in 1.1.05 (changelog 1.1.05.md 95-96). OV-36, OV-37 ([oven.md](../oven.md)).

### Left
1. Retexture: the sprite is still Veggies Delight's frosted cupcake
   (`development/backportedVeggies/src/main/resources/assets/veggiesdelight/textures/item/sweet_potato_cupcake.png`). Put
   a muffin sprite (paper case and dome of `fruitsdelight:item/blueberry_muffin`, an orange sweet potato top) in kubejs
   assets at `assets/veggiesdelight/textures/item/sweet_potato_cupcake.png` (resource pack override, no jar change).
   A player-visible change: changelog bullet.
2. Veggie muffins (carrot, zucchini, pumpkin): built with #2.
3. TODO #18: drop its cupcake half (the main session).

### Tests (proposed)
- FF-29 Sweet Potato Muffin sprite: the item shows the muffin sprite in hand, inventory and JEI.

---

## baked-goods #30: Baking dish bakeware
Status Planned / Work KubeJS / resource pack / TODO `baking/baked-goods #30` (scratchpad 2026-10-27, [oven.md](../oven.md) 2.5)

### Goal
A glass baking dish next to the baking sheet, muffin tin, pie tin and cake pan, for lasagna (#21) and other dish bakes
(brownies #18, puddings #36).

### Now
- Bakeware items: CD [bakeware.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/bakeware.js)
  13-18; dict keys item_dict 38-46 (`i.t_bakeware_<kind>`, `i.t_bakeware` the tool-slot filter); tags in CD
  [creativeengineering/tags.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/tags.js) 23-29; `ware` keys in
  cd_food.js 63-68; hammered chain in baking.js 176-179 (iron plate -> sheet -> muffin tin -> pie tin -> cake pan).

### Build
1. bakeware.js: `e.create(i.baking_dish).displayName('Baking Dish').texture('creativeengineering:item/baking_dish')`;
   item_dict `i.baking_dish = 'creativeengineering:baking_dish'`, `i.t_bakeware_baking_dish =
   '#creativeengineering:bakeware/baking_dish'`.
2. tags.js: `t.add(e, i.t_bakeware_baking_dish, i.baking_dish)` and the kind tag into `i.t_bakeware`.
3. cd_food.js `ware`: `dish: { exp: V.bake.exp, time: V.bake.time, tool: i.t_bakeware_baking_dish }`.
4. Recipe in baking.js "Oven and bakeware": `R.shaped(i.baking_dish, ['G G', 'GGG'], { G: i.glass }, e, 'baking_dish')`
   (`i.glass` item_dict 522); not in the iron hammering chain.
5. Texture `kubejs/assets/creativeengineering/textures/item/baking_dish.png` (or the CD assets, as the other bakeware).
6. oven.md 2.4 and 2.5 rows; CD rebuild (bakeware.js and cd_food.js are in the jar), restart. No `mod_sides.md` row
   (no new jar).

### Decisions
- **Craft recipe** (TODO decision). Recommendation: shaped 5 glass, the bowl shape; not hammered (glass can't be).

### Tests (proposed)
- OV-40 Baking dish: crafts from 5 glass, sits in the oven tool slot, is not used up by a dish bake.

---

## baked-goods #34: Every cake as a sequenced assembly
Status Planned / Work KubeJS / TODO `baking/baked-goods #34` (owner 2026-10-27, [food_families.md](../food_families.md) 4.6)

### Goal
Every cake can be assembled on a baked base (frosting, flavour steps); the two existing spouts get more steps.

### Now
- Cakes: `CAKES` (baking.js 48-56: adzuki, banana, chocolate, honey, mint, strawberry, vanilla, coffee, mangosteen,
  carrot) through `F.cakeBake` (cd_food.js 136-143), plain cake 123-126.
- Spouts (single fill): chocolate cake = baked base + 500 mB chocolate (129); plain cake = baked base + 750 mB frosting
  (131). No cake assembly (dump 2026-10-30).
- Row #1's open "sequenced assembly with spouting" is the same work.

### Build
1. cd_food.js: `cakeAssemble(flavour, output, second)`: `F.assemble(i.cake_base_baked, [{ fill:
   Fluid.of(global.fluid.frosting, 250) }, flavour, second || flavour], output)`; call it from `cakeBake`.
2. Replace the two spouts with assemblies: plain cake = frosting 250 mB x3 steps (or frosting + red berry); chocolate
   cake = 250 mB frosting, 500 mB chocolate.
3. Transition rows per cake (cakes are block items: pass `<ns>:item/<cake>` texture overrides where the auto sprite is
   wrong, **(unverified)** per cake).
4. Needs #37's fix only if a step is a list (the red berry choice is one).

### Decisions
- **Transition item per cake** (TODO decision). Recommendation: one per cake (each shows its own grey sprite), not the
  shared `i.incomplete`.

### Tests (proposed)
- FF-30 Cake assembly: each cake assembles on a baked base (frosting fill, two flavour deploys); plain and chocolate cake
  show their new steps.

---

## baked-goods #35: Investigate brownie tray and adzuki bun family
Status Planned / Work investigation / TODO `baking/baked-goods #35` ([food_families.md](../food_families.md) 4.6)

### Goal
Decide the family and recipe shape of the Beetroot Brownie Tray and the Adzuki Bun.

### Now
- Beetroot Brownie Tray (`veggiesdelight:beetroot_brownie_tray`): a placed block cut into 4 brownies, cake-like; family
  `dessert/brownie` (rule already: tray as own block, oven in the cake pan, board cut). Shapeless 3 beetroot, 2 cocoa,
  2 sugar, 2 eggs **(jar)**; no flour.
- Adzuki Bun (`neapolitan:adzuki_bun`): shapeless roasted adzuki beans + milk + wheat (now flour, baked_goods.js 17)
  **(jar)**; family `dessert/pastry` (was `bread/flat`); a can exists (`canned_adzuki_bun`). A filled sweet bun.

### Findings to decide on
- Tray: treat as a cake-pan (or baking dish) oven bake, as #18 builds. Recommendation: `dessert/brownie`, built by #18.
- Adzuki Bun: a filled bun is not bread under #33's rule (filled breads left `#forge:bread`). Recommendation: stay
  `dessert/pastry`; give it the pastry rule's oven bake (flour + roasted adzuki + milk, baking sheet, 2) with #36.
This row then closes into #18 and #36.

---

## baked-goods #36: Dessert cookware and recipe treatments
Status Planned / Work KubeJS / TODO `baking/baked-goods #36` ([food_families.md](../food_families.md) 4.6)

### Goal
Desserts (gingerbread, brownies, puddings, waffles, rolls, pastries) get their own cookware or recipe treatment instead
of cookie / cake / pie shapes.

### Now
- Family rules already written (food_families.md): `dessert/pudding` bowl puddings in the pot, fig pudding in the oven;
  `dessert/pastry`, `waffle`, `roll`, `other`: "Oven for the plain bake; toppings by Spout". Most members fail R3 today
  (grid, smoker or press recipes).
- Gingerbread moved to `dessert/other` 2026-10-27 (keeps its furnace / smoker recipes); the Gingerbread Man is a cookie
  (baking.js 87).

### Build (per family, one table each in baking.js)
1. Waffle: oven bake from uncooked waffle or cake paste (#6 step 2).
2. Pastry: adzuki bun, strawberry scones, strange eclair: oven, baking sheet, their flour recipes moved to the oven
   (`F._sweep` + `R.ceBaking`).
3. Sweet roll: `create:sweet_roll` oven bake from dough + sugar; the spout toppings stay.
4. Pudding: fig pudding in the baking dish (#30).
5. Brownies: #18.

### Decisions
- **Cookware items** (TODO decision). Recommendation: no new cookware beyond the baking dish (#30); waffles, pastries and
  rolls use the baking sheet.

### Tests (proposed)
- FF-31 Desserts: each family member listed above has its oven recipe on the stated bakeware; the spout toppings stay.

---

## Built, needs play

- `baked-goods #3` Cookie recipes: oven only, cookie dough + 2 flavour -> 12 ([oven.md](../oven.md) 2.6, OV-30); open S P.
- `baked-goods #11` Mixed cookie jars (patcherMod, every cookie jarable): [mixed_jars.md](../mixed_jars.md), MJ-01 to MJ-10;
  scratch passed (`/jartest`); open C P.
- `baked-goods #15` Maple cookie: cookie dough + 2 Rustic syrup -> 12 (baking.js 82), [food_families.md](../food_families.md)
  4.6, FF-11, SQ-FFB; open S P.
- `baked-goods #25` Sponge Pie rename and pie-tin bake (baking.js 154-155, CD `oceanic_delight/names.js`): food_families.md
  4.6, FF-13, SQ-FFB; open S C P.
- `baked-goods #26` Honey cake 1 honey bottle + 1 honeycomb (baking.js 51): food_families.md 4.6, FF-13; open S P.
- `baked-goods #27` Dough block for every dough (six blocks, 1.1.03): [oven.md](../oven.md) 2.6, OV-35, SQ-DB passed
  2026-10-25; open C P.
- `baked-goods #28` Any bread in any bread recipe (`bread_recipes.js`, shipped 1.1.05): [recipes_cooking.md](../recipes_cooking.md)
  RC-170, RC-171; recipe level passed 2026-10-31 (SQ-BJ-02, -02b); open P.
- `baked-goods #29` No smelting bread from oat dough (reversed 2026-10-28): oven.md 2.6, SQ-CM-06 passed; open P.
- `baked-goods #31` Bread baked from dough on a baking sheet (`bread_from_dough_1/3/6`, `sourdough_bread_1/3/6`): oven.md
  2.6, SQ-CM-06 passed; open P. Left over: Veggies' sweet potato dough smelting routes, carried in #17.
- `baked-goods #33` Filled breads out of `#forge:bread` (`bread_tags.js`, `tools/food_families.py`):
  [creative_delight.md](../creative_delight.md) 32; open S P.

## Writeup corrections made

- [recipes_cooking.md](../recipes_cooking.md) 8: last history line, added that the STAGED 1.0.8 items (cornmeal, pie
  crust, pancakes) went out with 1.0.8. (Its section 0 status and cooking.js line ranges were already corrected by
  another brief the same day.)
- [oven.md](../oven.md) 4: "a third cheaper for those 25" -> "those 9" (2.6.1 lists 9 trimmed recipes).

Found 2026-10-31, not made (writeups are outside this brief's sweep): oven.md 2.4 names `cookingtags.js` for the bakeware
tags, which live in CD `creativeengineering/tags.js` 23-29 (the CD bakeware.js header says the same stale path);
food_families.md `cake/muffin` R2 still lists a Cooking Pot muffin route (dropped 2026-10-29).
