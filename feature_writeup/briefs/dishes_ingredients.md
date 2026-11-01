# Brief: dishes / ingredients

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# dishes`, Sub `ingredients`; checked and updated 2026-10-31 (open
rows 7 to 10, 15, 17, 18, 20 to 22, 25, 26). Layout per [README.md](README.md). Feature writeups:
[recipes_cooking.md](../recipes_cooking.md) (test prefix `RC-`; 2.1 flour, dough, cornmeal; 2.2 fluids and sugar; 2.3
prepared food), [food.md](../food.md) (`FO-`; 3.13 powdered sugar, 3.14 sweeteners, 3.15 batter grains),
[item_merges.md](../item_merges.md) (`MG-`), cans in [can_parity.md](../can_parity.md) (`CP-`), jars in
[mixed_jars.md](../mixed_jars.md) (`MJ-`), family rules in [food_families.md](../food_families.md) 3.1.

**Where the code is now (2026-10-31):** `kubejs/server_scripts/cooking/cooking.js` and `tags/cookingtags.js` are empty
stubs. Every food recipe lives in the Creative Delight server tree by the mod of the food it makes,
`kubejs/data/creative_delight/kubejs/<mod>/recipes.js` / `tags.js` (memory `creative-delight-routing`); pack-own food
items are registered in CD startup scripts (`development/creativeDelight/src/main/resources/kubejs_startup/<mod>/`, e.g.
`moredelight/toast.js` registers `creativeengineering:cheese_toast`), with the dict key in `_lib/item_dict.js`. Recipes
through `global.NCH` (`R`, [neoCraftHelper.js](../../development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/neoCraftHelper.js)),
tags through `global.tags` (`t`), ids from `global.item` (`i`) / `global.fluid` (`f`; the food dict is `fd`).

**Jars (2026-10-31):** Rustic Delight is the CD member `backportedRustic-1.7.0` (data read from
`rustic_upstream/rusticdelight-forge-1.20.1-1.7.0.jar`), Farmer's Delight and Delightful are CD repacks
(`repackedFarmers-1.19.2-1.2.4`, `repackedDelightful-3.4.1`); Veggies, Vintage and Fungi are in-tree ports in `mods/`
(`backportedVeggies-...-03`, `backportedVintage-...-09`, `backportedFungi-...-01`); More Delight 25.07.28, Neapolitan 4.1.0.

## Scope and order

| Row | Change | Status | Group |
|---|---|---|---|
| 26 | More Delight potato recipes with an empty sliced-potato slot | **Bug** | A: one tag line, do first |
| 25 | Cabbage cutting gives 4 leaves | Planned | B (replaces the cabbage clause of row 8) |
| 18 | Cutting banana bunch | In Progress | B |
| 8 | Produce fixes (melon, chorus crate machine, raw pasta box) | In Progress | B |
| 20 | Sweet potato slices | Testing (built 2026-11-01) | C (new item, restart) |
| 21 | Potato and sweet potato compatibility | In Progress | C (after 20) |
| 22 | Honeycomb as sugar, honey and spread | In Progress | D |
| 7 | Batter uses | In Progress | D |
| 15 | Butter | In Progress | E (new item; decision first) |
| 17 | Tofu from mushrooms | Planned | E (new item) |
| 10 | Fats and oils | In Progress | F (loot + recipes; decisions D1-D3) |
| 9 | Cheese | In Progress | G (Creative Brewing jar for the Brewin' half) |
| 1-6, 11, 13, 14, 16, 23, 24 | see Built, needs play | Testing | - |
| 12 | Backport Veggie / Vintage / Fungi | Closed 2026-10-30 (duplicate of the port rows) | section removed |

Rows that add shapeless crafts meant to stay by hand give the id a `_manual_only` suffix (Create skips those in the
mixer, `AllRecipeTypes.CAN_BE_AUTOMATED`). Slice & Dice copies every Farmer's Delight cutting and pot recipe
(`sliceanddice:cutting/...`), so a removed upstream cutting recipe also drops its copy.

---

## ingredients #26: More Delight potato recipes with an empty sliced-potato slot
Status **Bug** / Work: KubeJS / id `dishes/ingredients #26` ([scratch_run_2026-10-31.md](../testing/scratch_run_2026-10-31.md) 8)

### Goal
The six More Delight potato dishes show and accept Rustic Delight potato slices again.

### Now (verified 2026-10-31)
- More Delight's recipes read **`#forge:sliced_potatoes`** (plural): `moredelight:cooking/mashed_potatoes`
  (`[#forge:milk, #forge:sliced_potatoes x2]`), `potato_salad`, and the four `cooking/diced_potatoes_with_*` (jar
  `moredelight-25.07.28`). Its own tag file `data/forge/tags/items/sliced_potatoes.json` holds only `moredelight:diced_potatoes`.
- The diced potato merge (item_merges.md 2.4, shipped 1.1.01) purges `moredelight:diced_potatoes`
  (CD startup `moredelight/item_merges.js`), so the removed-item pass strips it from that tag, and adds Rustic's slices to
  the **singular** `#forge:sliced_potato` (`i.t_sliced_potato`,
  [moredelight/item_merges.js:15](../../kubejs/data/creative_delight/kubejs/moredelight/item_merges.js), also
  [rusticdelight/tags.js:21](../../kubejs/data/creative_delight/kubejs/rusticdelight/tags.js)).
- Dump (`kubejs/exported/tags/item_tags.json` 26445-26448): `forge:sliced_potato` = `[rusticdelight:potato_slices]`,
  `forge:sliced_potatoes` = `[]`. item_merges.md 2.4 says More Delight reads `#forge:sliced_potato`: wrong name.

### Build
1. Dict: `i.t_sliced_potatoes = '#forge:sliced_potatoes'` next to `i.t_sliced_potato` (item_dict.js 1955), with a comment
   that More Delight reads the plural.
2. [moredelight/item_merges.js](../../kubejs/data/creative_delight/kubejs/moredelight/item_merges.js) tag event:
   `global.tags.add(e, global.item.t_sliced_potatoes, global.item.potato_slices)` (keep the singular line: fries and
   cod and chips in `pneumaticcraft/recipes.js:60-61` read it). Optional: make one tag include the other.
3. Fix item_merges.md 2.4's tag name (writeup owner) and add a changelog bullet ("Fixed: ... took no potato slices").

### Gotchas
- MG-12 ("diced potatoes with beef using potato slices: cooks") is listed as passed at recipe level (item_merges.md 357);
  the dump shows the slot empty, so that pass checked the recipe id, not the slot. Rerun it.
- The `diced_potatoes_with_egg_and_tomato_cooked_manual_only` grid route
  ([moredelight/recipes.js:18](../../kubejs/data/creative_delight/kubejs/moredelight/recipes.js)) uses baked potatoes and is unaffected.

### Tests
- MG-12 rerun: JEI shows Potato Slices in all six recipes' slot; a pot cooks mashed potatoes from milk + 2 slices.
- MG-new1 (proposed, numbered when queued): tag dump, `#forge:sliced_potatoes` holds `rusticdelight:potato_slices`.

---

## ingredients #25: Cabbage cutting gives 4 leaves
Status **Planned** / Work: KubeJS / id `dishes/ingredients #25`

### Goal
Cutting a cabbage gives 4 leaves (scratchpad 2026-10-31). Supersedes row 8's "3 leaves with a chance of a 4th".

### Now
`farmersdelight:cutting/cabbage` gives 2 `farmersdelight:cabbage_leaf` (`i.cabbage_leaf`, item_dict 11585; repackedFarmers
1.2.4, verified); Slice & Dice copies it. `farmersdelight:cabbage_from_leaves` is shapeless 2 leaves -> 1 cabbage
(verified). `i.cabbage` is `#forge:crops/cabbage` (1928).

### Build
In CD [farmersdelight/recipes.js](../../kubejs/data/creative_delight/kubejs/farmersdelight/recipes.js):
1. `R.remove({ id: 'farmersdelight:cutting/cabbage' }, e)`, then
   `R.fdCutting(i.cabbage, i.any_knife, Item.of(i.cabbage_leaf, 4), e, 'cabbage_leaves')`.
2. `R.remove({ id: 'farmersdelight:cabbage_from_leaves' }, e)` and `R.shapeless(i.cabbage, [Item.of(i.cabbage_leaf, 4)], e, 'cabbage_from_leaves')`
   (or drop the reverse craft). Without this, 1 cabbage -> 4 leaves -> 2 cabbages duplicates.
3. Changelog bullet under the Farmer's Delight family: "Cutting a Cabbage gives 4 Cabbage Leaves (was 2)".

### Decisions
- **Reverse craft:** recommend 4 leaves -> 1 cabbage (keeps leaves storable without a dupe).

### Tests
- RC-new1 (proposed, numbered when queued): knife on cabbage -> 4 leaves (board and Slice & Dice); 4 leaves -> 1 cabbage;
  2 leaves craft nothing.

---

## ingredients #18: Cutting banana bunch
Status **In Progress** / Work: KubeJS / id `dishes/ingredients #18`

### Goal
A cutting-board recipe that cuts a banana bunch into bananas ([food_families.md](../food_families.md) 3.1, fruit/slice row).

### Now (verified in `neapolitan-1.19.2-4.1.0.jar`)
`neapolitan:banana_bunch` (`i.banana_bunch`, item_dict 13298) has no recipe except 9 -> `banana_bundle`. Using it
(`BananaBunchItem.handleOpening`) opens it into `1 + random(3)` = 1 to 3 Neapolitan bananas (`i.neapolitan_banana`,
average 2); chimps open bunches too. No cutting recipe exists in the dump.

### Build
In CD `neapolitan/recipes.js` (`// requires: neapolitan`):
`R.fdCutting(i.banana_bunch, i.any_knife, [Item.of(i.neapolitan_banana, 2), Item.of(i.neapolitan_banana).withChance(0.5)], e, 'banana_bunch')`
(multi-output with chance works through KubeJS Delight, as in farmersdelight/recipes.js). Slice & Dice copies it.

### Decisions
- **Count:** recommend 2 + 50 % of a third (average 2.5, a little better than opening by hand). Alternative: 3 flat.

### Tests
- RC-new1 (proposed, numbered when queued): knife on a banana bunch -> 2 or 3 bananas; JEI shows the chance.

---

## ingredients #8: Produce fixes
Status **In Progress** / Work: KubeJS / id `dishes/ingredients #8`

### Goal
The small produce changes from the cooking plan and the 2026-10-21 scratchpad.

### Done (struck from Build)
- Diced potatoes merged into Rustic Delight potato slices (BUILT 2026-10-22, shipped 1.1.01, item_merges.md 2.4). Its
  tag gap is row 26.
- Corn milled to cornmeal before cornbread: row 1.
- Cabbage leaves: moved to row 25 (4 leaves, 2026-10-31); TODO still lists "3 leaves with a chance of a 4th" here.

### Now and Build (open sub-items)
- **Melon pressing.** Now: [farmersdelight/recipes.js:126-140](../../kubejs/data/creative_delight/kubejs/farmersdelight/recipes.js):
  IE squeezer slice 125 mB / melon block 1250 mB, IE bottler, Create mixing 3 slices + drink sweetener -> 250 mB; Farmer's
  Respite kettle melon juice ([farmersrespite/kettle.js:35](../../kubejs/data/creative_delight/kubejs/farmersrespite/kettle.js)).
  "Inefficient pressing recipe": add an early, sugar-free Create route, e.g.
  `R.createCompacting(Fluid.of(f.melon_juice, 250), Item.of(i.melon, 5), e, 'melon_juice_compacting')` in the same block
  (5 slices, worse than the squeezer's 2 and the mixer's 3 + sugar). Decision D1.
- **Chorus crate unpacking.** Hand unpack exists (`endersdelight:chorus_fruit_from_crate`, 9 fruit); LootJS packs the crate
  ([07_rules.js:76](../../kubejs/server_scripts/loot/07_rules.js)). Missing: a machine route; no other crate has one either.
  If wanted: `R.viVibrating([Item.of(i.chorus_fruit, 9)], [i.chorus_crate], e, 'chorus_crate_unpack')` (NCH 1952) or
  `R.ieMetalPress`. Decision D2.
- **Raw pasta boxed.** No pasta storage block exists (`block_dict.js` has none). Add a block
  `creativeengineering:raw_pasta_box` in a CD startup script (`farmersdelight/`), keys `b.raw_pasta_box` / `i.raw_pasta_box`,
  9 `i.raw_pasta` (11645) <-> 1, `t.storageBlock(e, 'raw_pasta', ...)` as endersdelight/tags.js:9 does for the chorus crate,
  and the crate block tag (blocktags.js 292). Texture needed. Decision D3.

### Decisions
- **D1 Melon "pressing":** recommend the compacting route above.
- **D2 Chorus crate machine:** recommend the vibrating table only if other crates get one too; otherwise drop it.
- **D3 Pasta box:** recommend yes, with Create Compressed's dough block as the texture base.

### Tests
- RC-new1 (proposed, numbered when queued): compactor 5 melon slices -> 250 mB melon juice; 9 raw pasta <-> 1 box.

---

## ingredients #20: Sweet potato slices
Status **Testing** / Work: KubeJS (+ new item) / id `dishes/ingredients #20` ([veggies_delight.md](../veggies_delight.md))

### Goal
A cut sweet potato gives 3 sweet potato slices.

### Built 2026-11-01 (veggies_delight.md 5.6)
- `creativeengineering:sliced_sweet_potato` "Sliced Sweet Potato" (owner's name, not `sweet_potato_slices`); the owner chose
  Rustic's potato slices sprite with no new art, so the model reads `rusticdelight:item/potato_slices` in place (nothing copied).
- Board and slicer 3; the potato went to 3 slices [2] at the same time. No baked form. Tags, compost, food type, family,
  dict and the Canned Sweet Potato source as in 5.6. Tests VD-44 to VD-46 (replace RC-new1, RC-new2). CD built, not installed.

### Now (checked 2026-10-31)
- No sweet potato slice item (Veggies port and `item_dict.js`: `sweet_potato` 14284, `baked_sweet_potato` 14295, dough, pie,
  cupcake). The potato analogue is Rustic's `potato_slices` (`i.potato_slices` 12743; knife on a potato -> 2) and
  `baked_potato_slices` (12720).
- Fries and cod and chips read `#forge:sliced_potato` (`i.t_sliced_potato`; [pneumaticcraft/recipes.js:60-61](../../kubejs/data/creative_delight/kubejs/pneumaticcraft/recipes.js));
  More Delight's dishes read `#forge:sliced_potatoes` (row 26).

### Build
1. Item `creativeengineering:sweet_potato_slices` (and `baked_sweet_potato_slices` if the potato's baked form is mirrored) in
   CD startup [veggiesdelight/veggies_items.js](../../development/creativeDelight/src/main/resources/kubejs_startup/veggiesdelight/veggies_items.js)
   (`StartupEvents.registry('item', ...)`, toast.js pattern), dict keys in item_dict.js, food values like `potato_slices`,
   texture recoloured from Rustic's slices (Rustic is ARR: draw our own, do not copy).
2. CD `veggiesdelight/recipes.js`: `R.fdCutting(i.sweet_potato, i.any_knife, Item.of(i.sweet_potato_slices, 3), e, 'sweet_potato_slices')`;
   baked form by `R.smelting` / smoking / campfire as for the potato slices.
3. CD `veggiesdelight/tags.js`: `t.add(e, [i.t_sliced_potato, i.t_sliced_potatoes], i.sweet_potato_slices)` (row 26's key),
   so fries and More Delight's dishes take it.
4. `food_types.json` (ingredient or raw/veggie cut), compost row, `/cegen` for cans, changelog bullet.

### Decisions
- Count 3 (the row's number) against 2 for the potato: recommend as asked.

### Tests
- RC-new1, RC-new2 (proposed, numbered when queued): knife a sweet potato -> 3 slices; slices make fries and mashed potatoes.

---

## ingredients #21: Potato and sweet potato compatibility
Status **In Progress** / Work: KubeJS / id `dishes/ingredients #21` ([veggies_delight.md](../veggies_delight.md))

### Goal
Recipes that take a potato also take a sweet potato, and the same for the cooked and sliced forms.

### Now (checked 2026-10-31)
- `i.potato` is `#forge:crops/potato` (item_dict 1899); the sweet potato is in `#forge:crops/sweet_potato`,
  `#forge:vegetables`, `#forge:crops` (tag dump), not in the potato tags. `i.baked_sweet_potato` is Veggies'.
- Potato users: `#forge:sliced_potato` (fries, cod and chips), `#forge:sliced_potatoes` (More Delight, row 26),
  `#forge:vegetables/potato` (Veggies' fish and chips, Rustic potato salad), item ids in Create / Oceanic recipes; Veggies'
  mashed potatoes already mix both ([veggiesdelight/cooking.js:59](../../kubejs/data/creative_delight/kubejs/veggiesdelight/cooking.js)).

### Build
1. Audit with `/dumprecipes` (the `exported/recipes` dump has ids only, no inputs): recipes naming `minecraft:potato`,
   `#forge:crops/potato`, `#forge:vegetables/potato`, `minecraft:baked_potato`, the two sliced tags.
2. Prefer tag edits through `global.tags` in CD `veggiesdelight/tags.js`: sweet potato into `#forge:vegetables/potato`,
   slices (row 20) into both sliced tags; item-only recipes get `R.replaceInput` with `[potato, sweet potato]`.

### Decisions
Which recipes (the row's decision). Adding the sweet potato to `#forge:crops/potato` also changes compost, trades, mob
food and planting: recommend only the recipe-facing vegetable tags, and replace inputs by id for the rest.

### Gotchas
Dishes named for the potato (mashed potatoes, hash browns) need a look at output names.

### Tests
- RC-new1 (proposed, numbered when queued): a sweet potato works in each audited recipe; compost and trades unchanged.

---

## ingredients #22: Honeycomb as sugar, honey and spread
Status **In Progress** / Work: KubeJS / id `dishes/ingredients #22` ([food.md](../food.md) 3.14)

### Goal
Honeycomb counts as sugar in heated mixing (2 sugar per honeycomb), as honey in some recipes, and spreads on toast.

### Now (checked 2026-10-31)
- Done by row 3 (shipped 1.1.03): honeycomb is in `#creativeengineering:sweeteners` (`i.any_sweetener`, item_dict 1870;
  [creativeengineering/tags.js:14](../../kubejs/data/creative_delight/kubejs/creativeengineering/tags.js)) and the brewing tier,
  so every cooking sugar slot takes it 1 for 1; drinks do not.
- Done: honey cake takes a honey bottle and a honeycomb ([creativeengineering/baking.js:51](../../kubejs/data/creative_delight/kubejs/creativeengineering/baking.js),
  `baking/baked-goods #26`).
- Open: the 2-per-comb rate, other honey uses, the toast spread. More Delight `toast_with_honey` is toast + honey bottle
  (fast-eating toast list in CD startup `moredelight/toast.js`); no honeycomb toast recipe.

### Build
1. Spread: CD `moredelight/recipes.js`, `R.shapeless(i.toast_with_honey, [i.toast, i.honeycomb], e, 'toast_with_honey_comb_manual_only')`
   (keep the bottle recipe).
2. 2 per comb: since the tag already makes 1 comb = 1 sugar in every slot, add one conversion instead of twin recipes:
   `R.createMixing(Item.of(i.sugar, 2), [i.honeycomb], e, 'sugar_from_honeycomb', 'heated')` in CD `minecraft/recipes.js`.
3. Honey: list the recipes taking `i.honey_bottle` as a flavour (pies via the `SWEET` slot, cookies); add the comb where it
   reads as a comb dish, as honey cake does.

### Decisions
- **2 per comb:** recommend the heated-mixer conversion (one recipe, covers everything downstream).
- **Honey uses:** recommend the toast and the pie `SWEET` slot only.

### Tests
- RC-new1 (proposed, numbered when queued): heated mixer 1 honeycomb -> 2 sugar; honeycomb on toast -> Toast with Honey.

---

## ingredients #7: Batter uses
Status **In Progress** / Work: KubeJS / id `dishes/ingredients #7`

### Goal
More uses for batter; batter from wheat flour or cornmeal; more fried foods (fried mushrooms?); a batter route into the
cake base (scratchpad 2026-10-22).

### Now (checked 2026-10-31)
- `rusticdelight:batter` (`i.batter`, item_dict 12721). Made in the pot (`rusticdelight:cooking/batter`) and its Slice & Dice
  copy, grain slots wheat flour, cornmeal or rice (row 24, [creativeengineering/baked_goods.js:21-27](../../kubejs/data/creative_delight/kubejs/creativeengineering/baked_goods.js),
  shipped 1.1.05); heated mix with egg solution ([rusticdelight/recipes.js:46-47](../../kubejs/data/creative_delight/kubejs/rusticdelight/recipes.js)).
  So "batter made from wheat flour or cornmeal" is **done**.
- Users (upstream Rustic 1.7.0 data): the seven pancakes (grid and pot), fried calamari, fried chicken, fried fish; the pack's
  salmon tempura ([pneumaticcraft/recipes.js:57](../../kubejs/data/creative_delight/kubejs/pneumaticcraft/recipes.js)).
- Not using batter: Supplementaries pancakes (sweetener + egg + milk + grain, [supplementaries/recipes.js:28-33](../../kubejs/data/creative_delight/kubejs/supplementaries/recipes.js)),
  muffins (oven, flour + milk + egg + sweetener, CD startup `creative_delight/cd_food.js:160-164`), the cake base (flour or
  dough, [creativeengineering/baking.js:99-105](../../kubejs/data/creative_delight/kubejs/creativeengineering/baking.js)).

### Build
1. Cake base: in baking.js add `R.shapeless(i.cake_base, [Item.of(i.batter, 2), i.eggs, i.any_sweetener, i.any_sweetener], e, 'cake_base_batter_manual_only')`
   and an oven `R.ceBaking([n(i.batter, 2), i.eggs, n(i.any_sweetener, 2)], i.cake_base_baked, e, 'cake_base_baked_batter', CAKE)`.
2. Supplementaries pancakes: a batter + sweetener shapeless (`_manual_only`) beside the flour one.
3. Fried foods: fried mushrooms would be a new item (decision); otherwise batter into existing fried foods (corn dogs,
   onion rings if present) by `R.replaceInput`.
4. Not for pies: the crust is the shell.

### Decisions
- **Which recipes:** recommend cake base and Supplementaries pancakes first; muffins stay on the oven family rule
  (owner 2026-10-29: muffins bake in the oven only, from flour).
- **Fried mushrooms:** recommend no new item unless the owner wants one.

### Tests
- RC-new1 (proposed, numbered when queued): 2 batter + egg + 2 sugar -> cake base (grid) and baked cake base (oven).

---

## ingredients #15: Butter
Status **In Progress** / Work: KubeJS (+ new item) / id `dishes/ingredients #15`

### Goal
Add butter (churned from milk or cream?) and give it uses in baking and cooking (scratchpad 2026-10-22).

### Now
No butter item in the pack (dict: only `create_confectionery:cocoa_butter`, `hostiledelight:magicalbutter`). No cream
item or churn block. Milk: `i.milk` = `#forge:milk` (1825), `i.t_milk_bottle` (1896), fluid `f.milk`. Fats today:
`i.animal_fat`, `i.cooking_oil` (row 10).

### Build
1. Item `creativeengineering:butter` (ingredient, small food value) in a CD startup script
   (`creativeengineering/baking_ingredients.js`), dict key `i.butter`, own texture.
2. Source (decision D1): Create mixing, unheated, `R.createMixing(Item.of(i.butter, 2), [Fluid.of(f.milk, 1000)], e, 'butter', 'n/a')`;
   a hand route needs a churn, which does not exist: recommend machine-only, plus a slow pot route (milk bucket -> 2,
   bucket back) if an early route is wanted.
3. Uses (decision D2): pie crust and cookie dough as an alternative fat; toast with butter; mashed potatoes; fried egg in
   place of cooking oil (`food.md` 3.7). Each by an extra slot option, not a replacement.
4. `food_types.json`, compost, cans (`/cegen`), changelog.

### Decisions
- **D1 New item and source:** recommend a new item from the Create mixer (milk), not from the Brewin' keg.
- **D2 Uses:** recommend pie crust, toast and mashed potatoes first.

### Tests
- RC-new1 (proposed, numbered when queued): mixer 1000 mB milk -> 2 butter; butter works in the pie crust recipe.

---

## ingredients #17: Tofu from mushrooms
Status **Planned** / Work: KubeJS (+ new item) / id `dishes/ingredients #17`

### Goal
Tofu made from mushrooms as a protein option; texture a recolour of the animal fat sprite (scratchpad 2026-10-22).

### Now
No tofu or soy item in the pack (dict). Mushrooms: `i.mushrooms` = `#forge:mushrooms` (1906). The animal fat sprite is
`assets/delightful/textures/item/animal_fat.png` in `repackedDelightful-3.4.1` (Delightful is MIT: notice beside the
recoloured copy). Vegetarian protein today: Veggies' vegetarian patty in burgers (vintagedelight/recipes.js 112-121).

### Build
1. Item `creativeengineering:tofu` (food, low hunger, like a raw cut) in a CD startup script (`creativeengineering/`),
   dict key `i.tofu`; texture `kubejs/assets/creativeengineering/textures/item/tofu.png` (recoloured fat) + MIT notice.
2. Recipe (decision D1): pot `R.fdCooking([i.mushrooms, i.mushrooms, i.mushrooms], Item.of(i.tofu, 2), e, 'tofu', 0.35, 200)`
   plus Create compacting 3 mushrooms -> 2 tofu.
3. Uses: a cooked tofu (smoking / campfire), and tofu as a meat alternative in a few dishes (stir fry, salads, the burger
   patty slot) by slot arrays.
4. `food_types.json` (protein / ingredient), compost, `/cegen`, changelog.

### Decisions
- **D1 Recipe:** recommend the pot and the compactor, no grid route.
- **D2 Where it substitutes meat:** recommend only dishes that already accept the vegetarian patty.

### Tests
- RC-new1 (proposed, numbered when queued): pot 3 mushrooms -> 2 tofu; tofu accepted in a burger.

---

## ingredients #10: Fats and oils
Status **In Progress** / Work: KubeJS / data / id `dishes/ingredients #10`

### Goal
Animal fat only from cows, pigs and "nether cows"; knives give more meat and fat (tooltip on knives); whales drop blubber
-> whale oil that works like animal fat; tallow crafted from animal fat (no tallow drops); animal oil removed (animal fat
cooks into cooking oil, which takes animal oil's crafts); more seeds into cooking oil, bottled seed oil; fish oil counts as
cooking oil, more fish into fish oil.

### Now (checked 2026-10-31; nothing of this row is built)
- `delightful:animal_fat` (`i.animal_fat`): Delightful loot modifier `data/delightful/loot_modifiers/animal_fat.json`
  (repackedDelightful 3.4.1, a CD member), knife kills of `#delightful:fatty_animals`: about 27 vanilla mobs (cat, wolf, cod,
  salmon, cow, dolphin, donkey, fox, glow squid, squid, goat, hoglin, horse, llama, mooshroom, mule, ocelot, panda, pig,
  piglin, piglin brute, polar bear, pufferfish, rabbit, sheep, tropical fish, turtle) plus optional modded ones. The squid
  drop was seen in SQ-CDB-04 (2026-10-25).
- `delightful:animal_oil_bottle` (`i.animal_oil_bottle`): pot 2 fat -> 1 (`delightful:cooking/animal_oil_bottle`); its torch is
  removed ([wood/ash.js:23-24](../../kubejs/server_scripts/wood/ash.js), torch from `i.t_animal_fat`). Delightful crafts 2 fat -> 1 tallow
  (`delightful:integration/immersive_weathering/tallow_from_animal_fat`).
- `immersive_weathering:tallow` (`i.tallow`): IW loot injects `immersive_weathering:injects/pig_tallow`, `hoglin_tallow`.
- `#forge:animal_fat` = fat, tallow, animal oil ([misctags.js:61](../../kubejs/server_scripts/tags/misctags.js)); soap uses them
  ([misc_mods.js:19-21](../../kubejs/server_scripts/other/misc_mods.js)).
- `rusticdelight:cooking_oil` (`i.cooking_oil`): pot 6 `#rusticdelight:cooking_oil_ingredients` -> 2 and heated mixing 3 -> 1
  ([rusticdelight/recipes.js:82-87](../../kubejs/data/creative_delight/kubejs/rusticdelight/recipes.js)); the tag gets pumpkin
  seeds in [minecraft/tags.js:13](../../kubejs/data/creative_delight/kubejs/minecraft/tags.js). `alexsmobs:fish_oil`
  (`i.fish_oil`). Fluids `f.plantoil`, `f.seed_oil`, `f.vegetable_oil`. No blubber or whale oil; the whale is
  `alexsmobs:cachalot_whale`.

### Build
1. Fat sources: CD `delightful/tags.js`, an `ServerEvents.tags('entity_type', ...)` block (none exists in the CD tree yet):
   clear `delightful:fatty_animals` and set cow, mooshroom, pig, hoglin (D1) through `global.tags`. Tallow drops off: remove
   the two IW injects with LootJS (`server_scripts/loot/10_removals.js` style, or a CD `immersive_weathering/loot.js`);
   keep Delightful's 2 fat -> tallow craft.
2. Knives give more: a LootJS rule in CD `delightful/loot.js` adding +1 raw meat / fat when the killer holds
   `i.any_knife`; tooltip line on `#forge:tools/knives` in `client_scripts/tooltips.js`.
3. Animal oil out: `global.removedItems.push(i.animal_oil_bottle)` (CD startup `delightful/`),
   `R.remove({ id: 'delightful:cooking/animal_oil_bottle' }, e)`, pot 2 fat -> 1 cooking oil
   (`R.fdCooking([i.animal_fat, i.animal_fat], i.cooking_oil, e, 'cooking_oil_from_fat', 0.35, 200, i.glass_bottle)`),
   `R.replaceInput({}, i.animal_oil_bottle, i.cooking_oil, e)`; drop it from `#forge:animal_fat` and the soap lists.
4. Fish oil as cooking oil: `t.add(e, i.t_cooking_oil, i.fish_oil)` (`#rusticdelight:cooking_oil`, read by Rustic's fried
   recipes and pneumaticcraft/recipes.js:60); more fish -> fish oil: pot 3 `i.t_food_raw_fish` -> 1, Alex's Mobs' hand craft stays.
5. Seeds: more seeds in `#rusticdelight:cooking_oil_ingredients` (melon, sunflower, hemp by id); bottled seed oil =
   `R.ieBottling(i.cooking_oil, <plant oil tag>, i.glass_bottle, e, 'cooking_oil_bottling', 250)` (check the tag in game).
6. Whales: new item `creativeengineering:blubber` (`i.blubber`) dropped by `alexsmobs:cachalot_whale` (LootJS), cooked
   in the pot into cooking oil (D2).

### Decisions
- **D1 "Nether cows":** no such mob; read as hoglins (already fat-tagged, IW's tallow source). Confirm.
- **D2 Blubber and whale oil vs animal fat:** recommend blubber cooks into cooking oil (one oil), no whale oil item.
- **D3 Bottled seed oil:** recommend the Bottler route into cooking oil, no new item.

### Tests
- RC-new1 (proposed, numbered when queued): knife kill on a cow drops fat, on a sheep or squid none; no tallow from pigs;
  2 fat in the pot -> cooking oil; no animal oil in JEI.

---

## ingredients #9: Cheese
Status **In Progress** / Work: KubeJS / Creative Brewing jar / id `dishes/ingredients #9`

### Goal
More cheese types (red mushroom, warped fungus), new colours, longer ageing, more per slice; cheese making to the
Vintage Delight method; a stackable cheese ripening shelf; 9 wedges craft back to the cheese block, for each cheese
(scratchpad 2026-10-23).

### Now (checked 2026-10-31)
- Brewin' cheese is now in Creative Brewing ([creativeBrewing](../../development/creativeBrewing/members/brewin), jar
  `creative_brewing-1.0.0`; build via `:creativeBrewing:`): flaxen and scarlet, keg fermenting milk -> cheese
  (`recipes/fermenting/flaxen_cheese_from_milk.json`), pouring -> unripe wheel; `UnripeCheeseWheelBlock` ripens with a
  10 % chance per random tick (`AGE` 0-1, lines 22-66); `CheeseWheelBlock` `SERVINGS` 0-3 (4 wedges), knife cutting -> 4,
  and 4 wedges -> wheel shaped (`flaxen_cheese_wheel_from_wedges.json`, scarlet too).
- Vintage Delight is installed (port `-09`): Cheese Mold, curds (milk bucket + salt + vinegar in the pot -> 4), next random
  tick -> `vintagedelight:cheese_wheel`, knife -> 4 `cheese_slice`, and `cheese_wheel_from_slices`.
- Fungi Delight 1.3 has no cheese (checked 2026-10-23). `#forge:cheese` (`i.any_cheese`) = flaxen wedge + cheese slice;
  `#brewinandchewin:foods/cheese_wedge` (`i.t_cheese_wedge`).
- So "wedges craft back to the block" exists for every cheese at 4 per wheel; "9 wedges" only fits with more servings.

### Build
1. Longer ageing: in the Brewin' member, lower the 0.1F chance in `UnripeCheeseWheelBlock.randomTick` (or a config knob).
   More per slice: `SERVINGS` 0-5 or 0-8, the cutting recipe -> 6 or 9, and the wedges -> wheel craft to match.
   Creative Brewing build.
2. New cheeses (red mushroom, warped fungus): copy the flaxen set in the Brewin' member (block, item, unripe block, fluid,
   fermenting / pouring / cutting recipes, models, textures, lang, loot); add to `#brewinandchewin:foods/cheese_wedge`.
3. Vintage method: decide whether the Brewin' keg route stays beside the Cheese Mold (`merges/dishes` owns cheese merges).
4. Ripening shelf: a new block in the Brewin' member holding several unripe wheels, ticking them; stack sizes in
   [food_stack-sizes.md](food_stack-sizes.md).

### Decisions
- **Which cheeses:** recommend red mushroom and warped fungus (mirrors of flaxen / scarlet).
- **Wedges per wheel:** recommend 9 only if "more per slice" is taken; otherwise the existing 4 -> wheel crafts close that clause.

### Tests
- RC-new1 (proposed, numbered when queued): an unripe wheel ripens later than before; cutting gives the new count; the
  wedges craft back to the wheel.

---

## Built, needs play

As built, with the writeup that holds the facts. Open tiers from each TODO Tests cell (`+` = scratch passed).

| Row | What | Writeup | Open tiers / tests |
|---|---|---|---|
| `ingredients #1` | Cornmeal: milling and crushing ratios, tortilla and cornbread batter, millstone routes, knife 1 maize -> 4 kernels (1.0.8, 1.1.01) | [recipes_cooking.md](../recipes_cooking.md) 2.1 | C P: RC-15 to RC-18 |
| `ingredients #2` | Egg solution and frosting fluids, tank-only (1.1.01) | [recipes_cooking.md](../recipes_cooking.md) 2.2 | C P: RC-77, RC-78 |
| `ingredients #3` | Four sweetener tiers in every sugar slot (1.1.03) | [food.md](../food.md) 3.14 | C P: RC-25 |
| `ingredients #4` | Bone broth grid routes and 7 heatless pastas, `_manual_only` (1.1.01) | [recipes_cooking.md](../recipes_cooking.md) 2.3 | C P: RC-75 |
| `ingredients #5` | Ice cubes: hammer -> 4, IE crusher -> 3, ice foods on `#forge:ice_cubes` (1.1.01) | [recipes_cooking.md](../recipes_cooking.md) 2.3 | C P: RC-76 |
| `ingredients #6` | Vinegar: Vintage's bottle as the `_vinegar` can additive on 66 cans (1.1.02) | [can_parity.md](../can_parity.md) CP-30, [vintage_delight.md](../vintage_delight.md) | S C P: CP-30 |
| `ingredients #11` | Botania bowl of water hidden (a Garden of Glass item, no recipe uses it): in `removedItems` (CD startup `botania/botania_items.js`), 1.1.03 | none yet. Built 2026-10-25; fold into [recipes_cooking.md](../recipes_cooking.md) 2.2 (water sources) | C: JEI half (SQ-CDB-13 PASS by `/zq`) |
| `ingredients #13` | Every soup and stew as a jar soft fluid, generated (`tools/gen_soup_soft_fluids.py`), 1.1.01 and 1.1.03 | [mixed_jars.md](../mixed_jars.md) | C P: MJ-11, MJ-12, MJ-14; WTHIT line is `ui/wthit #3` |
| `ingredients #14` | Cornmeal fixes: bottle or bucket for raw tortilla, washing / Bottler, mortar 2 -> 3, wild corn grinds (1.1.01) | [recipes_cooking.md](../recipes_cooking.md) 2.1 / F8 | C P: RC-53, RC-54 (and RC-15 to RC-19) |
| `ingredients #16` | Powdered sugar item, block and bucket, sugar seasoning (1.1.03) | [food.md](../food.md) 3.13 | S~ C P: SQ-PS-03, -04; FO-50 to FO-53 |
| `ingredients #23` | Powdered sugar rebalance (vacuum 4 -> bucket, bucket <-> 8, mortar 3 -> 4, crushing 1 + 50 %, no fishing); shipped 1.1.05 | [food.md](../food.md) 3.13 | C P T: SQ-PS-05, FO-50 to FO-52 |
| `ingredients #24` | Batter grains: wheat flour, cornmeal or rice; shipped 1.1.05 | [food.md](../food.md) 3.15 | C P: FO-58 (SQ-PS-05 PASS r1031 per food.md 3.15) |
