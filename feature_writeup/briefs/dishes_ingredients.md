# Brief: dishes / ingredients

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# dishes`, Sub `ingredients` (open rows 2 to 14). Layout per
[README.md](README.md). Feature writeup for this area: [recipes_cooking.md](../recipes_cooking.md) (2.1 flour, dough,
cornmeal; 2.2 fluids and sugar; 2.3 prepared food); cans in [cans.md](../cans.md) / [can_parity.md](../can_parity.md);
food types and salt in [food.md](../food.md). Recipes go in
[cooking.js](../../kubejs/server_scripts/cooking/cooking.js) through `global.NCH` (`R`), tags in
[cookingtags.js](../../kubejs/server_scripts/tags/cookingtags.js) through `global.tags` (`t`), ids from `global.item`
(`i`) / `global.fluid` (`f`).

**cooking.js map (2026-10-21 tree, 852 lines):** live handler lines 1-433, old `global.CE` copy commented out 436-853.
Smoker removal 13; bone broth 15-27; cooked rice 28-32; fluids 34-54; sugar 56-60; flour / dough 62-92;
**cornmeal 94-142**; bread block 143-152; sourdough 153-165; pasta 167-170; prepared 172-342 (pancakes 221-226);
sweets 344-369 (pie crust 346-349, muffins 359-369); fried egg 375-386; cooking oil 387-395; fish 397-405; ender
407-418; crates 420-431.

**Verified against jars** (scratchpad cache, 2026-10-21): Corn Delight 1.0.3, Create 0.5.1.i, Create Central Kitchen
1.3.11.c, Farmer's Delight 1.2.4, Supplementaries 2.4.20, Moonlight 2.3.7, Neapolitan 4.1.0, Rustic Delight 1.3.0,
More Delight 25.07.28, Ender's Delight 1.2.2, Delightful 3.4.1, Botania 440.2, Cooking for Blockheads 13.3.4,
PneumaticCraft 4.3.15, KubeJS 1902.6.2-build.73; the Brewin' port from `development/backportedBrewin`. Anything
marked *unverified* or *in game* needs the running pack (`/dumprecipes`, `/dumptags item`, JEI, `logs/kubejs/server.log`).

## Scope and order

| Row | Change | Status | Group |
|---|---|---|---|
| 14 | Cornmeal fixes (priority) | Planned | A |
| 11 | Botania bowl of water use | Planned | A (water sources, shares the tortilla / dough recipes) |
| 8 | Produce fixes (melon, diced potatoes, chorus crate, cabbage, boxed pasta) | Planned | B |
| 3 | Sugar and glycerol | Planned | B |
| 4 | Bone broth | Testing | C (with [dishes_meals.md](dishes_meals.md) A1 and `food/seasoning #1`) |
| 5 | Ice cubes | Testing | D (with `baking/sweets #3`, `#4`, `cooking/kitchen #6`) |
| 2 | New fluids (egg solution, frosting, beetroot soup) | Testing | E |
| 7 | Batter uses | Planned | E (egg solution makes batter) |
| 10 | Fats and oils | Planned | F |
| 6 | Vinegar | Planned | G (needs a source: row 12 or a pack item) |
| 13 | Every soup and stew as a jar fluid | Testing | H (data only) |
| 9 | Cheese | Planned | I (new jar: the Brewin' port) |
| 12 | Backport Veggie / Vintage / Fungi Delight | Planned | J (decision first) |

Suggested order: **A** first (the priority, all KubeJS, one `/reload`); **B** and **C** next (small KubeJS passes in
the same file); **D**; **E** (the first KubeJS-registered fluids in the pack: a startup script and a restart); **F**;
**H** any time (data only); **G**, **I**, **J** wait on decisions. Rows 2, 4, 5 and 7 all add Create mixing recipes:
give every new shapeless craft an id ending `_manual_only` (Create skips those in the mixer,
`AllRecipeTypes.CAN_BE_AUTOMATED`, verified in create-1.19.2-0.5.1.i) where the grid route is meant to stay by hand.

---

## ingredients #14: Cornmeal fixes (priority)
Status **Testing** · Work: KubeJS · id `dishes/ingredients #14` (follows `dishes/ingredients #1`, Testing)

**Built 2026-10-21:** step 1 (bottle) as written; step 2 washing and Bottler; step 3 moved to the muffin rebuild
(`baked-goods #2`); step 4 replaced by exact mortar ratios on the existing `foodGrinding` calls (corn 2 -> 3, the
Create machines' average; crates and bags off the mortar); step 5 as a full mirror of corn (`foodGrinding` on wild
corn: 1 + 50 % 1 cornmeal, 25 % 1 kernel, mortar 2 -> 3); step 6 (milk tag) not done; the
line 97 comment is fixed. Tests are RC-53 and RC-54 (this brief's RC-19 to RC-22 numbers were already taken).

### Goal
The 2026-10-21 priority list heads with "cornmeal fixes" but nothing says what is broken; the one concrete ask is "raw
tortilla takes a water bucket or a water bottle". This section is a static audit of every recipe that touches corn,
kernels, cornmeal, tortillas and cornbread, so the fixes can be built without first finding them in play.

### Now
- **Item:** [cornmeal.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cornmeal.js) line 7
  `e.create(global.item.cornmeal).displayName('Cornmeal')`; `i.cornmeal = 'creativeengineering:cornmeal'`
  ([item_dict.js](../../kubejs/startup_scripts/_lib/item_dict.js) 1885, priority 1000 so it loads first). No model
  file: KubeJS generates `creativeengineering:item/cornmeal`, which is where the sprite is
  (`kubejs/assets/creativeengineering/textures/item/cornmeal.png`, 16x16 RGBA). The name: KubeJS merges the files in
  `kubejs/assets/<ns>/lang/` into its generated lang (verified, `GeneratedClientResourcePack`, build 73), so the
  existing `kubejs/assets/creativeengineering/lang/en_us.json` does not shadow "Cornmeal".
- **Tags:** [cookingtags.js](../../kubejs/server_scripts/tags/cookingtags.js) 13-14: `#forge:flour/corn` = cornmeal;
  `#creativeengineering:flour_or_cornmeal` = `#forge:flour/wheat` + `#forge:flour/corn`. Not in `#forge:flour` (on
  purpose: no mod recipe takes cornmeal).
- **Recipes**, cooking.js 94-142 (ids `creativeengineering:<type>/<id>`):

  | Lines | Recipe | Id |
  |---|---|---|
  | 99-103, 123 | removes Corn Delight's millstone corn / wild corn, knife corn / wild corn, IE crusher wild corn / corn | (ids verified in the jar) |
  | 104-106 | knife corn -> 2 kernels; knife wild corn -> 1 kernel; IE crusher wild corn -> 1 kernel | `farmersdelightcutting/corn_kernels`, `farmersdelightcutting/wild_corn_kernels`, `iecrushing/wild_corn_kernels` |
  | 108-113 | crushing wheels: corn, crate, kernels, kernel bag | `createcrushing/corn_cornmeal`, `..._corn_crate_cornmeal`, `..._corn_kernels_cornmeal`, `..._corn_kernel_bag_cornmeal` |
  | 115-120 | **millstone mirrors of the four (added 2026-10-20 in c82dd31, after the 1.0.9 snapshot)** | `createmilling/<same>` |
  | 124-129 | IE crusher corn, crate, kernel bag | `iecrushing/corn_cornmeal` etc. |
  | 131-133 | PNC pressure chamber corn -> 3, 4 kernels -> 3; explosion corn -> 1 at loss 50 | `pcpressurechamber/corn_cornmeal`, `.../corn_kernels_cornmeal`, `pcexplosioncrafting/corn_cornmeal` |
  | 135-137 | removes `corn_delight:tortilla_raw` and the splashing corn -> raw tortilla; shaped `CCC / W` cornmeal + water bucket -> 3 | `shaped/tortilla_raw` |
  | 138-142 | removes Corn Delight's batter craft and mixing; shapeless 2 cornmeal + milk + egg -> 3; mixing 2 cornmeal + 250 mB `minecraft:milk` + egg -> 3 | `shapeless/cornbread_batter`, `createmixing/cornbread_batter` |
  | 224, 226 | pancakes take `[flour_or_cornmeal, rice]` (shapeless x3, heated mixing x5) | `shapeless/forge_sugar_to_pancake_by_shapeless` (auto id), `createmixing/...` |
  | 347-349 | pie crust from 3 flour or cornmeal + milk (shaped `FMF / F`, compacting) | `shaped/pie_crust_flour`, `createcompacting/pie_crust_flour` |
  | baking.js 63-76, 108 | oven pies and the oven cake base take `flour_or_cornmeal` | `baking/<pie>`, `baking/cake_base` |
- **Corn Delight data still live** (jar): corn -> 1 kernel shapeless (`corn_delight:corn_seeds`), crate / bag 9 <-> 1,
  smelting / smoking / campfire raw tortilla -> tortilla and batter -> cornbread, kernels -> popcorn, knife tortilla ->
  chips, pot recipes on cornbread / batter (stuffing, stuffed pumpkin, corn dogs, nachos), IE cloche, fermenter
  (120 mB ethanol), squeezer (80 mB plant oil per kernel). CCK adds corn soup / creamy corn drink fluids (mixing on
  `#forge:crops/corn`). None of these conflict with the cornmeal recipes.
- **Checked and fine:** every removed id exists in Corn Delight 1.0.3; `#forge:crops/corn` = corn only,
  `#forge:seeds/corn` = kernels only, `#forge:grain/corn` = corn; the IE crusher JSON NCH writes has the same shape as
  Corn Delight's own crusher recipes; the PNC pressure-chamber and explosion JSON match PNC's wheat-flour recipes;
  the millstone crate recipe has 4 outputs, Create's milling maximum; no grid collision between tortilla, batter,
  pancakes, pie crust and the 3 flour + egg dough; no exploit loop (best is PNC corn -> 3; knife 2 kernels -> 1.5 by
  PNC, 0.67 by crushing). Every `i.` key used in cooking.js lines 1-433 resolves to a dict entry.

### Findings (what is actually wrong or missing)
- **C1. Millstone: the writeup and RC-16 say the opposite of the code.** Until 2026-10-20 the section said "No millstone
  route for corn"; c82dd31 (2026-10-20 23:52, after the 1.0.9 deploy of 2026-10-19) added the four `createmilling`
  mirrors and rewrote the comment. The deployed `cooking.js` (sha1 `81b2eeb...` in
  [deploy/history/1.0.9.json](../../deploy/history/1.0.9.json)) matches no commit, so whether 1.0.9 has the mirrors
  can only be seen in game (JEI: uses of corn, Millstone tab). This is probably one cornmeal fix already made. The
  comment at line 97 ("The Millstone mirrors every Create crushing recipe") overstates it: only these four. Writeup 2.1
  and RC-16 are corrected (see the end of this file). Note crushing wheels fall back to milling recipes anyway, so the
  mirrors only matter for the millstone.
- **C2. Raw tortilla takes only a water bucket** (line 137; the row's ask).
- **C3. No automated raw tortilla.** Corn Delight's splashing corn -> raw tortilla is removed (136) and nothing replaced
  it, while cornbread batter kept a mixing route. Flour has the matching routes cornmeal lacks: Create's
  `create:splashing/wheat_flour` (`#forge:flour/wheat` -> dough, verified) and the Bottler (line 78, 250 mB water per flour).
- **C4. Muffins don't take cornmeal.** The cooking plan's cornmeal list had pancakes, pies and cakes, *and muffins*;
  the oven muffins (baking.js 36, 38) use `i.flour`, the pot muffins (cooking.js 366-369) `i.any_dough`. oven.md OV-19
  says "cookies and muffins don't" take cornmeal, so the build chose this; row 1 still lists muffins "to check".
- **C5. No hand route to cornmeal.** Every corn food past grilled / boiled corn and popcorn now needs a millstone,
  crushing wheels, IE crusher or PNC chamber: raw tortilla (so taco, tortilla chips, nachos), batter (cornbread,
  corn dogs, stuffing, stuffed pumpkin from cornbread). Before 1.0.8 these were hand crafts from corn. Wheat flour is the
  same (no hand route), so this is consistent, but it is the most likely "broken" report from play.
- **C6. Wild corn has no Create route.** Corn Delight's wild corn milling (corn seeds + straw) is removed and nothing
  replaces it; only the knife and IE crusher give its kernel.
- **C7. Milk fluid narrowed.** The batter mixing takes `Fluid.of(f.milk, 250)` (`minecraft:milk` only); Corn Delight's
  took `fluidTag forge:milk`. Harmless unless another milk fluid exists in game (none in `fluid_dict.js`).
- **C8. One bad id drops everything after it.** `global.NCH.strict` is true, so any NCH call whose item or tag
  resolves empty throws and KubeJS abandons the rest of the `ServerEvents.recipes` handler. If something in lines 1-93
  throws, Corn Delight's corn recipes stay and cornmeal has no recipes at all; if the 2026-10-20 additions in 172-219
  (pizza, sandwich, quiche on the Brewin' port's `#brewinandchewin:foods/cheese_wedge` and `pizza_topping`, both
  present in the port's data) throw, pancakes and pie crust lose cornmeal. Static check passes; the log check is in Tests.

### Build
1. **Tortilla with a bottle (C2).** cooking.js 137:
   ```js
   R.shaped(Item.of(i.tortilla_raw, 3), ['CCC', ' W '], { C: i.cornmeal, W: [i.water_bucket, i.water_bottle] }, e, 'tortilla_raw')
   ```
   `i.water_bottle` is the dict's `forge:nbt` ingredient (`minecraft:potion` + `{Potion:"minecraft:water"}`,
   item_dict.js 1362). The bottle comes back: [potion_buckets.js](../../kubejs/startup_scripts/items/potion_buckets.js)
   76-87 gives `minecraft:potion` a glass-bottle crafting remainder. A raw-JSON ingredient object in an NCH grid recipe
   is the same pattern potion_bottling.js uses (`forge:partial_nbt` in `R.shapeless`). Same output for bucket and
   bottle (recommended, see D1). Add the Botania water bowl here too if row 11 goes ahead.
2. **Automation (C3).** After line 137:
   ```js
   R.createSplashing(i.tortilla_raw, i.cornmeal, e, 'cornmeal_tortilla_raw')          // NCH line 1117, 1:1 = the grid ratio
   R.ieBottling(i.tortilla_raw, f.t_water, i.cornmeal, e, 'cornmeal_tortilla_raw', 250) // as flour -> dough, line 78
   ```
   Check `createSplashing` with a string output once (no pack script calls it yet).
3. **Muffins (C4), decision D2.** If yes: baking.js 36 and 38 `i.flour` -> `i.t_flour_or_cornmeal`; leave the pot
   muffins on dough. Update oven.md 2.6 / OV-19 and recipes_cooking.md 2.1 in the same change.
4. **Hand route (C5), decision D3.** If wanted, one lossy grid craft per corn, kept out of the mixer:
   `R.shapeless(i.cornmeal, [i.t_crops_corn, i.t_crops_corn, i.any_knife], e, 'cornmeal_by_hand_manual_only', [i.any_knife, 1])`
   (2 corn -> 1 cornmeal, the knife damaged by the `damage` argument). Or `R.hammering(i.cornmeal, Item.of(i.corn, 2), e, 'cornmeal')`
   if the IE hammer tier is early enough (it is not early game).
5. **Wild corn (C6).** `R.createMilling([i.corn_seeds, Item.of(i.corn_seeds).withChance(0.25)], i.wild_corn, e, 'wild_corn_kernels')`.
6. **Comment and milk (C1, C7).** Line 97: "The Millstone mirrors the four crushing recipes below". Line 141: use the
   fluid tag only if a second milk fluid shows up in game (`/dumpfluid`); otherwise leave it.
7. Update recipes_cooking.md 2.1 / section 7, then row 1 and row 14 in TODO.md together.

### Ids and data
- Items (dict, verified in jars): `i.cornmeal`, `i.corn` / `i.t_crops_corn`, `i.corn_seeds` / `i.t_seeds_corn`,
  `i.wild_corn`, `i.corn_crate`, `i.corn_kernel_bag`, `i.tortilla_raw`, `i.tortilla`, `i.cornbread_batter`,
  `i.cornbread`, `i.water_bucket`, `i.water_bottle`, `i.water_bowl` (`botania:water_bowl`), `i.any_knife`.
  Fluid `f.t_water` (`#forge:fluid/water`).
- Recipe types: `minecraft:crafting_shaped`, `create:splashing` (`R.createSplashing`), `immersiveengineering:bottling_machine`
  (`R.ieBottling`), `create:milling` (`R.createMilling`), `creativeengineering:baking` (`R.ceBaking`).

### Decisions
- **D1 Bottle yield.** A bottle is a third of a bucket. Recommend the same 3 tortillas either way (the bucket is
  returned, so it was never a cost); a stricter split would need two recipes.
- **D2 Cornmeal in muffins.** Recommend yes for the oven muffins only (cornmeal muffins are a real thing, and the plan
  listed them); no for cookies.
- **D3 A hand route.** Recommend no extra route if the millstone mirrors are live (the millstone is andesite-age Create);
  otherwise the lossy 2 corn + knife craft.
- **D4 PNC explosion ratio.** The plan's "(0.2, 3)" was read as "same as flour" (1 per corn at 50 % loss); if 0.2
  meant a yield, change line 132's loss rate. Recommend leaving it.

### Gotchas
- Strict NCH (C8): test the full handler, not just the new lines.
- `R.shaped` derives no id from an array key; keep the explicit `'tortilla_raw'` id so the recipe replaces in place.
- A water bottle from Create's spout carries the same `{Potion:"minecraft:water"}` as one from a source block; any
  extra NBT would fail the strict `forge:nbt` match (then switch to `forge:partial_nbt`, potion_bottling.js style).

### Tests (prefix `RC-`, recipes_cooking.md section 7)
- **RC-19 Cooking handler completes:** after `/reload`, `logs/kubejs/server.log` has "The cooking event has finished!"
  and no `[NCH]` error from cooking.js.
- **RC-20 Tortilla water:** 3 cornmeal over a water bottle -> 3 raw tortillas and a glass bottle back; with a bucket ->
  3 and the bucket back.
- **RC-21 Tortilla automation:** a fan through water turns cornmeal into raw tortillas 1:1; the Bottler fills cornmeal with 250 mB water.
- **RC-22 Millstone:** corn in a millstone -> 1-2 cornmeal, sometimes a kernel; wild corn -> a kernel (if step 5 is built).
- RC-15 to RC-18 as written (RC-16 corrected for the millstone).

---

## ingredients #11: Botania bowl of water use
Status **Testing** · Work: KubeJS · id `dishes/ingredients #11`

### Goal
Give `botania:water_bowl` a use.

### Now
No recipe in Botania 440.2 uses it (verified: no `data/**/recipes` file names it). `WaterBowlItem` is a plain item; Botania's
Forge `CapabilityUtil$WaterBowlFluidHandler` makes it a 1000 mB water container that empties to a bowl, so Create's
item drain and spout and any fluid-handler tank already treat it as water. Dict: `i.water_bowl` (item_dict.js 6284).

### Build
1. Use it as a grid water source wherever the pack already takes a water bucket for food: dough
   (cooking.js 76, `[i.flour, i.water_bucket]`), raw tortilla (#14 step 1). Change the water key to
   `[i.water_bucket, i.water_bottle, i.water_bowl]`. It can't be a tag (the bottle needs an NBT ingredient), so if
   three or more recipes share it, add one dict entry holding the array (e.g. `i.any_water_container`, next to
   `i.water_bottle` at item_dict.js 1362) rather than repeating the literal.
2. The bowl must come back. In a startup script (potion_buckets.js pattern, `ItemEvents.modification`):
   `event.modify(i.water_bowl, it => it.setCraftingRemainder(Item.of(i.bowl).getItem()))`. Check first in game
   whether Botania already sets a remainder (unverified; the class sets none, the registration might).

### Decisions
- **Whether.** Recommend yes as a third water container (cheap, no new recipes).

### Tests
- **RC-23 Water bowl:** flour + water bowl -> dough, bowl returned.

---

## ingredients #8: Produce fixes
Status **In Progress** · Work: KubeJS · id `dishes/ingredients #8`

### Goal
Six small produce changes from the cooking plan and the 2026-10-21 scratchpad.

### Now and Build (one sub-item each)
- **Melon pressing.** Now: IE squeezer slice 125 mB / melon 1250 mB, mixing sugar + 3 slices -> 250 mB (cooking.js
  51-53), FD's hand craft 4 slices + sugar + bottle -> melon juice (jar). "Inefficient pressing recipe": add an
  early, sugar-free automation, e.g. `R.createCompacting(Fluid.of(f.melon_juice, 250), Item.of(i.melon, 5), e, 'melon_juice_compacting')`
  (5 slices, worse than the squeezer's 2 and the mixer's 3 + sugar). Decision D1.
- **Diced potatoes -> Rustic Delight potato slices.** `moredelight:diced_potatoes` (knife on `#forge:vegetables/potato`
  -> 1, `moredelight:cutting/diced_potatoes`; More Delight also ships a Casualness Delight compat cutting) feeds
  More Delight's diced-potato dishes, mashed potatoes and potato salad. Rustic Delight's `rusticdelight:potato_slices`
  comes from `rusticdelight:cutting/potato_slices`. Merge: `R.replaceInput({ mod: 'moredelight' }, i.diced_potatoes, i.potato_slices, e)`,
  `R.remove({ id: 'moredelight:cutting/diced_potatoes' }, e)`, add `i.diced_potatoes` to `global.removedItems`
  (removed_items.js) so JEI hides it. Check both cutting yields first (unverified: slices per potato) and keep the
  higher one.
- **Chorus crate unpacking.** The hand unpack exists (`endersdelight:chorus_fruit_from_crate`, crate -> 9 chorus fruit,
  verified). What is missing is a machine route; other crates have none either. If wanted:
  `R.viVibrating([Item.of(i.chorus_fruit, 9)], [i.chorus_crate], e, 'chorus_crate_unpack')` (Vintage Improvements
  vibrating table, NCH 1521) or IE metal press. Decision D2.
- **Corn milled to cornmeal before cornbread:** done by row 1 (cooking.js 138-142). Strike it from the row.
- **Cabbage leaves.** FD's `farmersdelight:cutting/cabbage` gives 2 leaves (verified). Replace:
  `R.remove({ id: 'farmersdelight:cutting/cabbage' }, e)` then
  `R.fdCutting(i.cabbage, i.any_knife, [Item.of(i.cabbage_leaf, 3), Item.of(i.cabbage_leaf).withChance(0.5)], e, 'cabbage_leaves')`
  (multi-output with chance is already used in bark.js 61). Decision: the 4th leaf's chance (recommend 50 %).
- **Raw pasta boxed.** No pasta storage block exists. Add a KubeJS block `creativeengineering:raw_pasta_box` (block_dict
  key `b.raw_pasta_box`, item key `i.raw_pasta_box`), 9 `i.raw_pasta` <-> 1 shaped / shapeless, tag it with the other
  crates in storageblocktags.js and blocktags.js 277. Texture needed. Decision D3.

### Decisions
- **D1 Melon "pressing":** recommend the compacting route above.
- **D2 Chorus crate machine:** recommend the vibrating table only if other crates get one too; otherwise drop it.
- **D3 Pasta box:** recommend yes, with Create Compressed's dough block as the texture base.

### Tests
- **RC-24 Produce:** knife on cabbage -> 3-4 leaves; More Delight dishes accept potato slices; diced potatoes hidden.

---

## ingredients #3: Sugar and glycerol
Status **Testing** · Work: KubeJS · id `dishes/ingredients #3`

### Goal
Glycerol in most sugar recipes, but not in sugar-only items (sugar bag, sugar block) or drinks; more uses for sugar cubes.

### Now
Already pack-wide and too wide: cookingtags.js 7 puts `pneumaticcraft:glycerol` in `#forge:sugar`, and cooking.js 59
`R.replaceInput({ input: i.sugar }, i.sugar, i.any_sugar, e)` swaps vanilla sugar for the tag in every recipe loaded
before it (drinks included). The sugar cube (line 60, 9 sugar -> 1) is written after the swap, so it stays vanilla
sugar. No sugar bag or sugar block exists in the dict (only `quark:sugar_cane_block`).

### Build
1. Restrict the swap: `R.replaceInput({ input: i.sugar, not: { output: i.t_food_drink } }, i.sugar, i.any_sugar, e)`
   (KubeJS 6 recipe filters take `not`; `i.t_food_drink` = `#creativeengineering:food_type/drink`, which includes
   `drink/alcohol`). `R.replaceInput` passes the filter straight through, so no helper change.
2. Drinks made by later scripts (alcohol.js, the Brewin' port's keg recipes) are not touched by line 59; leave them.
3. Sugar cube uses: one cube = 9 sugar, so make it a bulk sugar in mixing: e.g. the melon juice mixing
   `[i.sugar_cube, Item.of(i.melon, 27)] -> 2250 mB`; and in row 2's frosting. Add to `#forge:sugar`? No (a cube would
   count as one sugar).

### Decisions
- **Restrictions:** drinks out (row). Recommend also leaving candy whose recipe is sugar plus a colour (rock candy,
  `baking/sweets #2`) on vanilla sugar if that row wants "real" sugar; otherwise only drinks.

### Gotchas
- `not: { output: tag }` needs the food-type tags bound when recipes load (they are: foodtypetags.js runs in the tag
  event). Check in JEI that a tea still asks for sugar only.

### Tests
- **RC-25 Glycerol:** a cake accepts glycerol; a tea (Farmer's Respite) does not.

---

## ingredients #4: Bone broth
Status **Testing** · Work: KubeJS · id `dishes/ingredients #4`

**Built 2026-10-22** ([recipes_cooking.md](../recipes_cooking.md) 2.3): tests are RC-75 (this brief's RC-26 number was already taken).

### Goal
Bone broth lets soups, stews and pastas be crafted shapeless instead of in the pot; no Create shapeless mixing; every
valid soup / stew / pasta covered; cooked variants in heatless shapeless.

### Now
cooking.js 18-26: pot bone broth x2 (bone or bone meal + glow berries / hanging roots / glow lichen / mushrooms), shapeless
x1, and shapeless beef stew, baked cod stew, noodle soup, chicken soup, cooked rice on bone broth. Those ids carry no
`_manual_only` (auto ids like `creativeengineering:shapeless/bone_broth_to_beef_stew_by_shapeless`), so Create's mixer
runs them unheated in a basin: the "no Create shapeless mixing" rule is broken today. Bone broth is in
`salt:can_be_salted` (Salt 1.2.4.1 tag, verified) and Create: The Salt salts it by mixing
(`create__the_salt:bonebroth`): an intermediate that can be salted (`food/seasoning #1`).

### Build
1. Rename the five ids with `_manual_only` (`'beef_stew_bone_broth_manual_only'` ...). The pot (and Slice & Dice's heated
   mixing copy) stays the automated route.
2. Coverage list: `#creativeengineering:food_type/soup` (31 items, food_types.json) plus the pastas in `meal`
   (bell pepper pasta, cheesy pasta, crab pasta, creamy pastas, kangaroo pasta, lobster pasta, pasta with meatballs /
   mutton chop, pearl pasta, squid ink pasta, vegetable noodles, duck noodles). For each one whose pot recipe takes
   water-like filler (a second vegetable, a bowl of water), write the shapeless form: pot ingredients with cooked
   variants (baked potato for potato, `i.tomato_sauce` for tomato, cooked meat for raw) + `i.bone_broth`, bowl
   included in the broth. The meal recipes themselves belong to [dishes_meals.md](dishes_meals.md) rows 1, 2, 4 (their A1
   `_manual_only` rule is this row's rule); this row owns bone broth and the soup / stew list.
3. Excluded from bone broth: bone broth itself, suspicious stew, sweet soups (bayberry soup?), drinks.
4. Salt exclusion: see `food/seasoning #1` step 1.

### Decisions
- **Which soups get a broth route:** recommend every soup in `food_type/soup` with a meat, fish or vegetable base;
  skip sweet and Nether / End stews that have no water-like ingredient.

### Tests
- **RC-26 Broth:** beef stew from broth + cooked beef + carrot + baked potato in the grid; the same five items in a basin
  do nothing.

---

## ingredients #5: Ice cubes
Status **Testing** · Work: KubeJS · id `dishes/ingredients #5`

**Built 2026-10-22** ([recipes_cooking.md](../recipes_cooking.md) 2.3): tests are RC-76 (this brief's RC-27 number was already taken).

### Goal
Hammer ice into cubes on the cutting board (machine crushing less efficient); cubes usable as ice; every ice food takes
ice or ice cubes; the fridge's ice module provides both.

### Now
- Item `neapolitan:ice_cubes` (`i.ice_cubes`, item_dict.js 12414), edible (0 / 0). Sources (verified, Neapolitan 4.1.0):
  a loot modifier on `minecraft:blocks/ice` (pickaxe, no silk touch: 0-2 cubes, fortune); 9 cubes -> 1 ice
  (`neapolitan:ice`). **Neapolitan's `neapolitan:ice_cutting` (ice on the cutting board with a pickaxe -> 4) names
  `neapolitan:ice_cube` (singular, no such item)**, so it fails to load: there is no cutting-board route in game.
- Ice foods in Neapolitan take the *item* `neapolitan:ice_cubes` (7 ice creams); `#forge:ice_cubes` holds the cubes,
  and cookingtags.js 26 adds `minecraft:ice` to it, but no recipe reads the tag yet.
- Cooking for Blockheads fridge with the ice unit lends `minecraft:snowball` and `minecraft:ice` to recipes
  (`FridgeBlockEntity$1.applyIceUnit`, verified by decompiling 13.3.4), not ice cubes.

### Build
1. `R.remove({ id: 'neapolitan:ice_cutting' }, e)` (harmless if it never loaded) and
   `R.hammering(Item.of(i.ice_cubes, 4), i.ice, e, 'ice_cubes')` (NCH 965: IE hammer on the cutting board + grid craft).
   Hammer the board tag rule: cutting boards are already in `#creativeengineering:hammer_no_rotate`.
2. Machines, less efficient: `R.createCrushing([Item.of(i.ice_cubes, 3)], i.ice, e, 'ice_cubes')`,
   `R.ieCrushing(Item.of(i.ice_cubes, 3), i.ice, e, 'ice_cubes')`; packed ice -> 9 ice only (vanilla-style), not cubes.
   Check in JEI that no Create / Vintage Improvements ice crushing exists already.
3. Every ice food takes ice or cubes: `R.replaceInput({}, i.ice_cubes, i.t_ice_cubes, e)` with a new dict key
   `i.t_ice_cubes = '#forge:ice_cubes'` (key style `t_<tag path>`). The tag already holds vanilla ice (cookingtags.js 26),
   so the fridge's ice unit then satisfies every ice recipe with no mixin. Do not swap the 9 cubes -> ice recipe
   (`{ not: { id: 'neapolitan:ice' } }`), or 9 ice make 1 ice.
4. Recipes asking for `minecraft:ice` itself (shaved ice, popsicles, CCK ice cream mixing: find them with JEI "uses of
   ice") get `[i.ice, i.t_ice_cubes]` per recipe, coordinated with `baking/sweets #3` and `#4`.

### Decisions
- **One ice = how many cubes:** recommend 4 by hammer (Neapolitan's intent), 3 by machine, 9 back to one ice.
- **Fridge giving cubes directly:** recommend the tag route (step 3); a mixin on `FridgeBlockEntity$1.applyIceUnit`
  only if a recipe must have the cube item itself.

### Tests
- **RC-27 Ice:** hammer on ice on a cutting board -> 4 cubes; an ice cream crafts with an ice block from a fridge with the
  ice unit.

---

## ingredients #2: New fluids
Status **Testing** · Work: KubeJS / data · id `dishes/ingredients #2`

**Built 2026-10-22** ([recipes_cooking.md](../recipes_cooking.md) 2.2, RC-77, RC-78; the RC-28 below was never
used). Both fluids tank-only (no bucket, no block, like chorus juice); egg solution 500 mB per egg; frosting became
the Create route to the frosting item the baking overhaul added after this brief (mixer, compactor, cake spout).
Dough 3 flour + 250 mB -> 6 (not 8: 250 mB is already half an egg). Pie crust and the oven recipes got no egg
solution route. Beetroot soup moved to row 13.

### Goal
Egg solution (egg + milk: omelette, scrambled eggs, cornbread batter, efficient dough, potato salad, quiche, pudding,
batter); frosting (milk + sugar: cakes, pies, muffins, custard, tart); beetroot soup as an efficient beetroot; maybe
buckets stacking to 16.

### Now
No KubeJS fluid is registered anywhere (no `StartupEvents.registry('fluid')`); `fluid_dict.js` lists mod fluids only.
CCK has corn soup, tomato sauce, ice creams, teas, but no egg, frosting or beetroot soup fluid (lang, verified).
Moonlight has a beetroot soup *soft fluid* (jars only, `data/moonlight/moonlight/soft_fluids/beetroot_soup.json`), not a
Forge fluid. Farmer's Delight's cooking pot can't take fluids, so these fluids only work in Create (mixing, filling,
sequenced assembly), IE (Bottler, mixer) and buckets in the grid.

### Build
1. New startup script `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js`:
   ```js
   StartupEvents.registry('fluid', e => {
       e.create(global.fluid.egg_solution).thinTexture(0xF3D97A).bucketColor(0xF3D97A).displayName('Egg Solution')
       e.create(global.fluid.frosting).thinTexture(0xFFF4F0).bucketColor(0xFFF4F0).displayName('Frosting')
   })
   ```
   Dict: `f.egg_solution = 'creativeengineering:egg_solution'`, `f.frosting = 'creativeengineering:frosting'` in a
   new `//Creative Engineering (KubeJS)` block of fluid_dict.js; buckets `i.egg_solution_bucket`,
   `i.frosting_bucket` (`creativeengineering:<fluid>_bucket`, KubeJS's default bucket id; check with `/kubejs hand`).
2. Make them: `R.createMixing(Fluid.of(f.egg_solution, 250), [i.eggs, Fluid.of(f.milk, 250)], e, 'egg_solution', 'n/a')`
   (1 egg + 250 mB milk -> 250 mB; recommend 500 mB for the efficiency the plan wants);
   `R.createMixing(Fluid.of(f.frosting, 250), [Item.of(i.sugar, 2), Fluid.of(f.milk, 250)], e, 'frosting', 'heated')`.
3. Uses, through Create: dough `R.createMixing(Item.of(i.in_dough, 8), [Item.of(i.flour, 3), Fluid.of(f.egg_solution, 250)], e, 'dough_egg_solution', 'n/a')`
   (3 flour + 250 mB egg solution -> 8, beating 3 flour + egg -> 6; `i.in_dough` = `create:dough`, item_dict.js 1806;
   `i.dough` is the `#forge:dough/wheat` tag and can't be an output); cornbread batter mixing (cooking.js 141) gains an egg-solution twin
   (2 cornmeal + 250 mB egg solution -> 3); batter (row 7); omelette / scrambled eggs / quiche as filling steps in
   their sequenced assemblies (dishes_meals.md row 3). Frosting: a filling step in cake / pie / muffin assemblies
   (`baking/baked-goods #1` mentions frosting in the crust).
4. Beetroot soup: no fluid; give `minecraft:beetroot_soup` an "efficient beetroot" role directly (a recipe that takes
   3 beetroot also takes 1 beetroot soup, returning the bowl), or wait for row 13's jar fluid.
5. Bucket stacks: [food_stack-sizes.md](food_stack-sizes.md) row 1's policy gives filled buckets 8 today; decide there.

### Decisions
- **Which fluids:** recommend egg solution and frosting now; beetroot soup as an item swap, not a fluid.
- **Bucket stack size:** leave to `food/stack-sizes #1`.

### Gotchas
- The first KubeJS fluids in the pack: needs a restart and a client update (assets are generated); check the flowing
  texture renders and the bucket fills from a Create tank.
- NCH `_checkFluid` validates against the Forge registry, so the dict keys must match the registered ids exactly.

### Tests
- **RC-28 Egg solution:** mixer egg + milk -> egg solution; dough from flour + egg solution.

---

## ingredients #7: Batter uses
Status **In Progress** · Work: KubeJS · id `dishes/ingredients #7`

### Goal
More uses for batter; batter replaces dough, flour or pie crust in some baking.

### Now
`rusticdelight:batter` (`i.batter`, item_dict.js 12455). Made only in the pot: milk + egg + 2 wheat -> 2, bowl
(`rusticdelight:cooking/batter`, verified). Used by four shaped pancakes (cherry blossom, chocolate, honey, vegetable),
fried chicken and fried calamari (pot), plus Rustic Delight's other fried foods. The pancakes are a second pancake line
next to Supplementaries' (cooking.js 224).

### Build
1. Make batter the flour way: `R.remove({ id: 'rusticdelight:cooking/batter' }, e)`, pot milk + egg + 2 flour -> 2
   (`R.fdCooking([i.milk, i.eggs, i.flour, i.flour], Item.of(i.batter, 2), e, 'batter', 0.35, 100, i.bowl)`); with row 2,
   mixing 2 flour + 250 mB egg solution -> 2.
2. Uses: Supplementaries pancakes from batter + sugar (shapeless `_manual_only`), the oven muffins with batter in place
   of flour + egg + milk (baking.js 36-38: `[n(fruit,2), i.batter, i.any_sugar]`, MUFFIN), tempura / fish and chips
   (dishes_meals.md row 7), corn dogs taking batter or cornbread batter.
3. Not for pies: the crust is the pie's shell; batter replaces flour + egg + milk only.

### Decisions
- **Which recipes:** recommend muffins and Supplementaries pancakes first; cakes stay on the cake base.

### Tests
- **RC-29 Batter:** oven muffin from 2 berries, batter, sugar.

---

## ingredients #10: Fats and oils
Status **In Progress** · Work: KubeJS / data · id `dishes/ingredients #10`

### Goal
Animal fat only from cows, pigs and "nether cows"; knives give more meat and fat (tooltip on knives); whales drop
blubber -> whale oil that works like animal fat; tallow crafted from animal fat (no tallow drops); animal oil removed
(animal fat cooks into cooking oil, which takes animal oil's crafts); more seeds into cooking oil, bottled seed oil;
fish oil counts as cooking oil, more fish into fish oil.

### Now (verified in jars)
- `delightful:animal_fat` (`i.animal_fat`): loot modifier `delightful:animal_fat`, 60 % (+2 x looting), 1-3, only when
  killed with `#forge:tools/scavenging` (knives) from `#delightful:fatty_animals` (cat, wolf, cod, salmon, cow,
  dolphin, horse, pig, hoglin, sheep, ... about 30 vanilla mobs plus many optional ones).
- `delightful:animal_oil_bottle` (`i.animal_oil_bottle`): pot, 2 fat -> 1 (`delightful:cooking/animal_oil_bottle`);
  Delightful's torch from it is removed (wood/ash.js 30). Delightful also crafts 2 fat -> 1 tallow
  (`delightful:integration/immersive_weathering/tallow_from_animal_fat`).
- `immersive_weathering:tallow` (`i.tallow`): loot injects `immersive_weathering:injects/pig_tallow` and
  `hoglin_tallow`.
- `#forge:animal_fat` = fat, tallow, animal oil (misctags.js 45); used by soap (misc_mods.js 19-21) and the pack torch
  (ash.js 31; note ash.js 7 defines `i.any_animal_fat` locally: move it to the dict, CLAUDE.md define-once).
- `rusticdelight:cooking_oil` (`i.cooking_oil`): pot 6 seeds -> 2 (`_manual_only`), heated mixing 3 seeds -> 1
  (cooking.js 390-395); `#rusticdelight:cooking_oil_ingredients` = cotton + pumpkin seeds (cookingtags.js 32).
  `alexsmobs:fish_oil` (`i.fish_oil`). Fluids `f.plantoil`, `f.seed_oil`, `f.vegetable_oil`. No blubber or whale oil item
  exists; the whale is `alexsmobs:cachalot_whale`.

### Build
1. Fat sources: in entitytags.js replace `delightful:fatty_animals` with cows, pigs, mooshrooms, hoglins (and the "nether
   cow", decision D1): `t.removeAll`-style clear then `t.add(e, 'delightful:fatty_animals', [...])` (entity_type event).
   Tallow drops off: remove the two IW injects with LootJS in `server_scripts/loot/10_removals.js`
   (`immersive_weathering:injects/pig_tallow`, `hoglin_tallow`), keep Delightful's 2 fat -> tallow craft.
2. Knives give more: a LootJS rule (loot/40_targeted.js style) adding +1 raw meat / fat when the killer holds
   `#forge:tools/knives`; tooltip line on `#forge:tools/knives` in client_scripts/tooltips.js.
3. Animal oil out: add `i.animal_oil_bottle` to `global.removedItems`; `R.remove({ id: 'delightful:cooking/animal_oil_bottle' }, e)`;
   pot 2 fat -> 1 cooking oil (`R.fdCooking([i.animal_fat, i.animal_fat], i.cooking_oil, e, 'cooking_oil_from_fat', 0.35, 200, i.glass_bottle)`);
   `R.replaceInput({}, i.animal_oil_bottle, i.cooking_oil, e)` and drop it from `#forge:animal_fat`.
4. Fish oil as cooking oil: `t.add(e, 'rusticdelight:cooking_oil', i.fish_oil)` (the tag Rustic Delight's fried
   recipes read, `#rusticdelight:cooking_oil`; add the dict key `i.t_cooking_oil = '#rusticdelight:cooking_oil'`);
   more fish -> fish oil: pot 3 `#forge:raw_fishes` -> 1 fish oil, the Alex's Mobs hand craft stays.
5. Seeds: add more seeds to `#rusticdelight:cooking_oil_ingredients` (melon, sunflower, hemp seeds by id); "bottled seed
   oil" = IE Bottler `f.plantoil` 250 mB into a glass bottle -> cooking oil (`R.ieBottling(i.cooking_oil, '#forge:plantoil', i.glass_bottle, e, 'cooking_oil_bottling', 250)`;
   check the plant-oil tag name in game).
6. Whales: new KubeJS item `creativeengineering:blubber` (`i.blubber`) dropped by `alexsmobs:cachalot_whale` (LootJS),
   cooked in the pot into cooking oil (or a whale oil item, D2).

### Decisions
- **D1 "Nether cows":** no such mob in the dicts; read as hoglins (already fat-tagged, IW's tallow source). Confirm.
- **D2 Blubber and whale oil vs animal fat:** recommend blubber cooks into cooking oil (one oil), no separate whale oil.
- **D3 Bottled seed oil:** recommend the Bottler route into cooking oil, no new item.

### Tests
- **RC-30 Fat:** knife kill on a cow drops fat, on a sheep none; no tallow from pigs; 2 fat in the pot -> cooking oil.

---

## ingredients #6: Vinegar
Status **Testing** · Work: KubeJS · id `dishes/ingredients #6`

### Goal
A vinegar canning additive for fruit, veg, meat and fish (and salads).

### Now
No vinegar item in the pack (dict, verified). Supplementaries ships a vinegar soft fluid for `simplefarming:vinegar`
(not installed). The can generator already has the hook: [gen_can_types.py](../../tools/gen_can_types.py) section
`extra` lists (line 61; `SALT = ['salt', 'bone_broth']` line 66; `salad` line 79 "vinegar additive comes later"),
and canning.js comments 124-133, 453, 460 plan it.

### Build
1. Source (decision D1): a KubeJS item `creativeengineering:vinegar` (`i.vinegar`, bottle, glass-bottle remainder via
   `.containerItem('minecraft:glass_bottle')`) made by fermenting apple cider in the Brewin' port keg
   (`R.bncFermenting`, NCH 1041) or IE fermenter; or row 12's Veggie Delight vinegar.
2. Additive: in gen_can_types.py add `VINEGAR = ['vinegar']` and `('_vinegar', VINEGAR, n, 1)` to the raw veggie,
   raw fruit, raw meat, raw fish and salad sections; run the tool; canning.js reads the json (no script change).
3. Soft fluid for jars: `kubejs/data/creativeengineering/moonlight/soft_fluids/vinegar.json` (row 13's format).

### Decisions
- **D1 Source mod:** recommend the pack item from the Brewin' keg (no new mod).

### Tests
- **CP-? (cans.md prefix) Vinegar can:** 5 salads + vinegar + empty can -> a can.

---

## ingredients #13: Every soup and stew as a jar fluid
Status **Testing** (built 2026-10-22, [mixed_jars.md](../mixed_jars.md) section 9; colour: each its own) · Work: data · id `dishes/ingredients #13`

### Goal
Supplementaries jar soft fluids for every soup, all one soup colour; WTHIT shows them (`ui/wthit #3`).

### Now (verified)
Soft fluids are data: `data/<namespace>/moonlight/soft_fluids/<name>.json`, read from any namespace (Moonlight 2.3.7
ships its own under `moonlight`, Supplementaries 2.4.20 under `supplementaries`). Already covered: Moonlight beetroot
soup, mushroom stew, rabbit stew, suspicious stew; Supplementaries beef stew, baked cod stew, chicken soup, fish stew,
pumpkin soup, vegetable soup, sopa de macaco. Missing, from `food_type/soup`: `alexsdelight:acacia_blossom_soup`,
`alexsdelight:kangaroo_stew`, `neapolitan:adzuki_stew`, `fruitsdelight:bayberry_soup`, `fruitsdelight:fig_chicken_stew`,
`rusticdelight:bell_pepper_soup`, `farmersrespite:blazing_chili`, `moredelight:carrot_soup`,
`quarkdelight:cavern_explorer_stew`, `quarkdelight:miso_with_bamboo_sprouts`, `endersdelight:chorus_stew` and
`endermite_stew` (+ `_wood` bowl variants), `corn_delight:corn_soup`, `brewinandchewin:creamy_onion_soup`,
`farmersdelight:noodle_soup`, `nethersdelight:strider_moss_stew`, `delightful:sinigang`. Golden Food's soups: check the
`golden` type list. Bone broth: decision.

### Build
One json per soup in `kubejs/data/creativeengineering/moonlight/soft_fluids/`, Supplementaries' pumpkin soup as the
template:
```json
{ "color": "#c8803a", "containers": [{ "filled": ["corn_delight:corn_soup"], "empty": "minecraft:bowl", "capacity": 2 }],
  "flowing_texture": "moonlight:soft_fluids/potion_flow", "still_texture": "moonlight:soft_fluids/soup_liquid",
  "food": { "divider": 2, "item": "corn_delight:corn_soup" }, "from_mod": "corn_delight",
  "translation_key": "item.corn_delight.corn_soup" }
```
For "one colour", also override the existing ones by the same path (`data/supplementaries/moonlight/soft_fluids/beef_stew.json`
etc. in kubejs/data) with only the colour changed. `_wood` variants: `"empty"` is Ender's Delight's wooden bowl id.
A small generator in tools/ (reading food_types.json and the dict) would keep it in step with new soups.

### Decisions
- **The colour:** recommend one warm broth colour for savoury soups, keep sweet / Nether / End ones distinct (or
  everything one colour, as the row says).

### Tests
- **WT-? / JAR-01:** a bowl of corn soup goes into a jar and comes back out; WTHIT shows "Contents: Corn Soup (1 / 12)".

---

## ingredients #9: Cheese
Status **In Progress** · Work: new jar (Brewin' port) / KubeJS · id `dishes/ingredients #9`

### Goal
More cheese types (red mushroom, warped fungus, Fungi Delight), new colours, longer ageing, more per slice; Vintage
Delight's cheese method when added; a stackable cheese ripening shelf.

### Now
The Brewin' port (`development/backportedBrewin`) has flaxen and scarlet cheese: keg fermenting milk -> cheese fluid
(`recipes/fermenting/flaxen_cheese_from_milk.json`), pouring -> unripe wheel, `UnripeCheeseWheelBlock` ripens by
random tick (`AGE` 0-1, lines 22-66), `CheeseWheelBlock` has `SERVINGS` 0-3 (4 wedges; knife cutting -> 4 wedges,
`recipes/cutting/flaxen_cheese_wheel.json`). JEI aging category in `integration/jei`.

### Build
1. Longer ageing: in `UnripeCheeseWheelBlock.randomTick` ripen only on a chance (a CEConfig-style knob in the port,
   or a fixed 1 in N). More per slice: `SERVINGS` 0-5 and the cutting recipe -> 6 (port data and block).
2. New cheeses (red mushroom, warped fungus): copy the flaxen set in the port: block + item registration, unripe
   block, fluid, fermenting / pouring / cutting recipes, models, textures, lang, loot; add to
   `#brewinandchewin:foods/cheese_wedge`. That is a port release (alcohol family owns the jar; see
   [alcohol.md](../alcohol.md)).
3. Ripening shelf: a new block (port or patcherMod) holding several unripe wheels, ticking them; stack size of wheels
   is in [food_stack-sizes.md](food_stack-sizes.md).

### Decisions
- **Which cheeses:** recommend red mushroom and warped fungus (mirrors of flaxen / scarlet); Fungi Delight's waits on
  row 12.

---

## ingredients #12: Backport Veggie Delight, Vintage Delight, Fungi Delight
Status **Planned** · Work: new jar · id `dishes/ingredients #12`

### Goal
Sources for vinegar (row 6), the cheese method (row 9) and salt production (`food/seasoning #1`).

### Now
None of the three is in pack/MODLIST.md. Whether 1.19.2 builds exist is unverified (they are 1.20-era mods). The
porting rules and tiers are in [feature_porting.md](../feature_porting.md) 0 (a mod that registers saved things is a
full port, like the Brewin' and Vinery ports).

### Build
1. Check CurseForge / Modrinth for 1.19.2 Forge files; if one exists, add the jar (pack/MODLIST.md, manifest).
2. Otherwise decide per feature: vinegar and salt production are small enough to do in KubeJS (rows 6, seasoning #1);
   only a full content port justifies a new in-tree jar.

### Decisions
- **Which, and as ports:** recommend no ports; build vinegar and the salt routes in KubeJS, and revisit Vintage Delight
  only if its cheese method is wanted as is.

---

## ingredients #20: Sweet potato slices
Status: In Progress / Work: KubeJS (+ new item) / TODO `dishes/ingredients #20` ([veggies_delight.md](../veggies_delight.md))

### Goal
A cut sweet potato gives 3 sweet potato slices.

### Now
- No sweet potato slice item exists (Veggies Delight jar and `item_dict.js`: only `sweet_potato`, `baked_sweet_potato`,
  dough, pie, cupcake, pancakes). The potato analogue is Rustic Delight `potato_slices` (`i.potato_slices`, a knife
  cut of `minecraft:potato` gives 2, verified in the jar) and `baked_potato_slices`; tag `#forge:sliced_potato`
  (`i.t_sliced_potato`, set in [cookingtags.js](../../kubejs/server_scripts/tags/cookingtags.js) line 38 and
  [item_merges.js](../../kubejs/server_scripts/other/item_merges.js) line 85) is what fries and fish and chips read.
- VD is in-tree (`development/backportedVeggies`), so a new item can be registered there (Java) or as a KubeJS
  startup item.

### Build
1. Item `sweet_potato_slices` (food, recoloured Rustic `potato_slices` texture; also a baked form if the potato has one).
2. `R.fdCutting(i.sweet_potato, i.any_knife, Item.of(i.sweet_potato_slices, 3), e, 'sweet_potato_slices')`.
3. `t.add(e, i.t_sliced_potato, i.sweet_potato_slices)` through `global.tags`, so fries (`dishes/meals #34`) take it.
4. Dict key, `food_types.json` entry (ingredient), changelog line.

### Decisions
Slices from a potato give 2 and from a sweet potato 3 (the row's number): recommend as asked. Item registered in
Java (VD port) or KubeJS: recommend KubeJS startup, no port edit (`dishes/ingredients #12`).

### Tests
RC-110 Knife a sweet potato: 3 slices. RC-111 Slices count in the fries recipe.

---

## ingredients #21: Potato and sweet potato compatibility
Status: In Progress / Work: KubeJS / TODO `dishes/ingredients #21` ([veggies_delight.md](../veggies_delight.md))

### Goal
Recipes that take a potato also take a sweet potato, and the same for the cooked and sliced forms.

### Now
- `i.potato` is `#forge:crops/potato` (item_dict 1934); VD tags the sweet potato under its own `#forge:crops/sweet_potato`
  and `#forge:vegetables/sweet_potato` (jar), so it is not in the potato tags. Baked potato is `i.baked_potato`
  (vanilla); `i.baked_sweet_potato` is VD's.
- Potato users already read tags: `#forge:sliced_potato` (fries, fish and chips, More Delight potato salad),
  `#forge:vegetables/potato` (VD fish and chips, Rustic potato salad, `potato_noodle`), vanilla `minecraft:potato`
  items in Create / Oceanic recipes.

### Build
1. Audit with `/dumprecipes`: recipes whose inputs name `minecraft:potato`, `#forge:crops/potato`,
   `#forge:vegetables/potato`, `minecraft:baked_potato`, `#forge:sliced_potato`.
2. Prefer tag edits (`global.tags`): add sweet potato to the potato crop and vegetable tags, baked sweet potato to a
   baked-potato tag, sweet potato slices (`#20`) to `#forge:sliced_potato`. Item-only recipes get `R.replaceInput`.

### Decisions
Which recipes (the row's decision). Adding the sweet potato to `#forge:crops/potato` also changes everything else that
reads the tag (compost, trades, mob food, planting tags): recommend tagging only the recipe-facing vegetable tags and
replacing inputs by id for the rest.

### Gotchas
Dishes named for the potato (mashed potatoes, VD's version already has a sweet potato mix) need a look at output names.

### Tests
RC-112 A sweet potato works in each audited recipe; RC-113 compost and trades unchanged.

---

## ingredients #22: Honeycomb as sugar, honey and spread
Status: In Progress / Work: KubeJS / TODO `dishes/ingredients #22` ([food.md](../food.md))

### Goal
Honeycomb counts as sugar in heated mixing (2 sugar per honeycomb), as honey in some recipes, and spreads on toast.

### Now
- `i.any_sugar` is `#forge:sugar` (item_dict 1914); `i.honeycomb` = `minecraft:honeycomb` (557). Sugar steps in
  Create heated mixing are written with `R.createMixing(..., 'heated')` (cooking.js line 803 orange jam, baking.js
  frosting); the pot copies are turned into mixer twins by Slice & Dice unless the id ends `_manual_only`.
- Toast is More Delight `moredelight:toast` (bread slice smoked); `toast_with_honey` is toast + honey bottle + glass
  bottle (shapeless, jar). Honeycomb has no toast recipe.

### Build
1. Spread: `R.shapeless(i.toast_with_honey, [i.toast, i.honeycomb], e, 'toast_with_honey_comb_manual_only')` (remove
   the honey bottle recipe or keep both).
2. Sugar: a tag cannot give "2 per comb", so add a second mixer recipe per sugar-using heated mix with honeycomb at
   half the count, by a helper loop in the file that owns the mix; do not add honeycomb to `#forge:sugar` (it would
   make 1 comb = 1 sugar everywhere, including crafting and cans).
3. Honey: list recipes that take `i.honey_bottle` as a flavour (cakes, cookies, pies via `SWEET` in baking.js);
   honeycomb joins where that reads as a comb dish.

### Decisions
Which recipes (the row's decision): recommend heated mixing only, the toast, honey cake (`baking/baked-goods #26`),
and the `SWEET` pie slot; not every honey use.

### Tests
RC-114 Heated mix with honeycomb gives the sugar recipe's result; honeycomb on toast gives honey toast.

---

## Built, needs play

| Row | Change | Note |
|---|---|---|
| 1 | Cornmeal (Testing) | DEPLOYED with 1.0.8; millstone mirrors added 2026-10-20 (c82dd31), not shipped. "Cornmeal as flour in ... muffins still to check": muffins don't take it (by design per oven.md OV-19; `#14` C4). Its "Corn milled to cornmeal" twin in row 8 is done. |

## Proposed TODO corrections (for the TODO owner)
- `ingredients #14` Details: replace "what is broken isn't written down: run RC-15 to RC-18" with the static findings
  (millstone mirrors added 2026-10-20, unshipped; tortilla bucket only; no automated tortilla; no wild corn Create
  route; muffins without cornmeal); Writeup: add this brief.
- `ingredients #1` Details: "muffins still to check" -> "muffins don't take cornmeal (OV-19); millstone mirrors
  2026-10-20, not shipped".
- `ingredients #8` Details: chorus crate hand unpacking exists (`endersdelight:chorus_fruit_from_crate`); only a machine
  route is missing. The cornmeal clause is done (row 1).
- `ingredients #5` Details: add "Neapolitan's own cutting-board ice -> cubes recipe names a missing item and never loads".
- `ingredients #4` Details: add "the five existing broth crafts lack `_manual_only`, so Create mixes them today".

## Writeup corrections made
- [recipes_cooking.md](../recipes_cooking.md): "Pending" line (nothing unshipped -> the millstone mirrors); 2.1 "Corn
  has no millstone recipe" -> the millstone mirrors (2026-10-20, unshipped) and no Create route for wild corn; F1 line
  14 -> 13; F3 (the local `i.catfish_slice` is gone since 2026-10-20); F8 points to this brief with the findings;
  RC-16 millstone expectation; History: a 2026-10-20 entry. (The At a glance status, file line ranges and F4 had
  already been corrected by another agent.)
