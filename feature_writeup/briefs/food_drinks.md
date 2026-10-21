# Brief: food / drinks

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# food`, Sub `drinks` (rows 1 to 6, all Planned). Writeups:
[food_catalog.md](../food_catalog.md) (`drink` table), [remainder_buffer.md](../remainder_buffer.md) (kettle leftovers,
RB-03), [crops.md](../crops.md) V5 / V18 (coffee), [placeable_food.md](../placeable_food.md) and the
[food_placeable brief](food_placeable.md) (Display Delight placing, `food/placeable #3`), [alcohol.md](../alcohol.md)
(milk / honey fluids, apple cider note B7.15). Tests use `RC-` ([recipes_cooking.md](../recipes_cooking.md));
[food_sweets.md](food_sweets.md) proposes RC-19 to RC-32, so these start at RC-33. The cooking plan these rows came
from is in git (`git show c82dd31^:feature_writeup/cooking.md`, "drink", "dairy", "fluid").

**Verified against jars** (scratchpad cache, Vineflower): Farmer's Respite (FR) 1.19-2.0, Farmer's Delight (FD) 1.2.4,
Delightful 3.4.1, Neapolitan 4.1.0, Abnormals Delight 4.1.2, Create 0.5.1.i, Create Central Kitchen (CCK) 1.3.11.c,
Rustic Delight 1.3.0, Ender's Delight 1.2.2, Fruits Delight 0.5.11, Alex's Mobs 1.21.1, Engineer's Delight (tmted)
R.1.3, Cooking for Blockheads (CfB) 13.3.4, KubeJS 1902.6.2-build.73. Facts that need the game are marked "in game".

## Scope and order

| Row | Size | Build with |
|---|---|---|
| `food/drinks #3` Merges (milk bottles first) | medium, several independent parts | first: #6 needs the milk bottle answer, `food/placeable #3` needs the merge list |
| `food/drinks #6` Milk from a cow in a jar | small | after the milk bottle merge |
| `food/drinks #5` Berry matcha with Farmer's Respite | small | adds the NCH kettle helper `frBrewing` (used by #1, #2, #4) |
| `food/drinks #2` Tea cleanup | medium | with `food/placeable #3` (placing), after #5 (helper) |
| `food/drinks #4` Chorus juice | small-medium | creates the shared startup fluid file (also `food/sweets #4`, #1); before `food/baked-goods #4` (chorus pie) |
| `food/drinks #1` Lemonade | medium (3 fluids, 3 items) | after #4 (fluid file), #5 (helper) |

**Shared pieces:**
- **NCH kettle helper** (none today; `farmersrespite:brewing` JSON verified from the FR jar). Add to
  [neoCraftHelper.js](../../kubejs/startup_scripts/func/neoCraftHelper.js) after `fdCutting` (line ~960):
  ```js
  // Farmer's Respite kettle (farmersrespite:brewing). inputs: 1-2 ingredients. opts: { container (default glass
  // bottle), time (2400), exp (0.35), needWater (true), tab ('drinks') }
  frBrewing(input, output, e, idOverride, opts) {
    opts = opts || {}
    var inputs = Array.isArray(input) ? input : [input]
    var idSuffix = idOverride ?? global.NCH.toId(output, input, 'frbrewing')
    var ctx = 'frBrewing ' + idSuffix
    if (inputs.length < 1 || inputs.length > 2) global.NCH._warn(ctx, 'kettle takes 1-2 ingredients')
    var json = { type: 'farmersrespite:brewing', recipe_book_tab: opts.tab ?? 'drinks',
      ingredients: inputs.map((v, n) => global.NCH._itemJson(v, { countMode: 'none', context: ctx + ' input[' + n + ']' })),
      result: global.NCH._itemJson(output, { allowTag: false, context: ctx + ' result' }),
      container: global.NCH._itemJson(opts.container ?? global.item.glass_bottle, { allowTag: false, countMode: 'none', context: ctx }),
      experience: opts.exp ?? 0.35, cookingtime: opts.time ?? 2400, needwater: opts.needWater ?? true }
    e.custom(json).id(`${global.NCH.modId}:farmersrespitebrewing/${idSuffix}`)
  },
  ```
- **Startup fluid file** `kubejs/startup_scripts/cooking/fluids.js` (no KubeJS-registered fluid exists in the pack;
  [fluid_dict.js](../../kubejs/startup_scripts/_lib/fluid_dict.js) only names other mods' fluids). KubeJS
  `FluidBuilder` (verified) has `thinTexture(color)`, `thickTexture`, `stillTexture`, `bucketColor`, `noBucket()`,
  `noBlock()`, `displayName`. Recommend `noBlock()` for every drink fluid (CCK's drink fluids are "virtual": no block,
  no bucket); bucket or not is D1 of #1. New ids go in fluid_dict.js under a "Pack fluids" heading and get a
  `forge:fluid/<name>` tag in [fluidtags.js](../../kubejs/server_scripts/tags/fluidtags.js) (list at lines 6-30).
- **Drink items in KubeJS:** `.useAnimation('drink')`, `.containerItem(i.glass_bottle)` (bottle back when crafted with;
  also what the FD pot / kettle treat as "served in"), `.maxStackSize(16)` like FR / FD drinks, and
  `f.eaten(ev => ev.player.give(i.glass_bottle))` for the bottle back after drinking (the callback form is in
  [cans.js:80](../../kubejs/startup_scripts/items/cans.js#L80); check `ev.player` in the harness).
- After new drinks: `/dumpfood` → `tools/food_catalog.py` (type `drink`), `gen_stack_sizes.py`, a `/` row in
  can_parity.md, and `food/placeable #3`'s tool picks them up.

Built, needs play: none (no drinks row is at Testing or Complete).

---

## drinks #1: Lemonade
Status **Planned** · Work: KubeJS / data (+ textures) · id `food/drinks #1`

### Goal
A lemonade fluid with pink and blue variants; their effects come from what goes in.

### Now
- No lemonade. Fruits Delight has Lemon Juice: item `fruitsdelight:lemon_juice` (`i.lemon_juice`, item_dict.js:12275;
  1 / 0.2, Haste 10 s + Refreshing 10 s, bottle) and fluid `fruitsdelight:lemon_juice` (250 mB per bottle). Recipes
  (verified): shapeless `[water bottle (l2library:potion water), sugar, #forge:fruits/lemon x2]`; Create mixing
  `fruitsdelight:mixing/flowing_lemon_juice` `[sugar, 2 lemons, 250 mB water]` → 250 mB; emptying / filling.
- Canned Goods' "Oculemonade" (`canned_goods:oculemonade`) is an unrelated can drink (name only).
- Effect ids: `fruitsdelight:refreshing`, `fruitsdelight:brightening` (blueberry), Neapolitan strawberries have no effect.

### Build
1. Fluids in `fluids.js`: `creativeengineering:lemonade` (pale yellow), `pink_lemonade`, `blue_lemonade`,
   `thinTexture(0x...)`, `noBlock()`; keys `f.lemonade` ...; `f.lemon_juice = 'fruitsdelight:lemon_juice'`.
2. Items `creativeengineering:lemonade`, `pink_lemonade`, `blue_lemonade` (drink item shape from Shared), food e.g.
   `f.hunger(2).saturation(0.3).alwaysEdible()` + effects (D2). Keys `i.lemonade` ... in a "Pack sweets and drinks"
   section of item_dict.js (shared with food_sweets.md). Sprites: recolour Fruits Delight's lemon juice bottle.
3. Recipes (`server_scripts/cooking/drinks.js`, new):
   - Create: `R.createMixing(Fluid.of(f.lemonade, 500), [Fluid.of(f.lemon_juice, 250), Fluid.of(f.water, 250), i.any_sugar], e, 'lemonade')`;
     pink: `[Fluid.of(f.lemonade, 500), i.t_fruits_strawberry]` → 500 pink; blue: `[Fluid.of(f.lemonade, 500), i.t_fruits_blueberry]` → 500 blue.
   - Spout / drain per variant: `R.createFilling(i.lemonade, [i.glass_bottle, Fluid.of(f.lemonade, 250)], e)`,
     `R.createEmptying([i.glass_bottle, Fluid.of(f.lemonade, 250)], i.lemonade, e)`.
   - By hand, in the kettle (it supplies the water): `R.frBrewing([i.t_fruits_lemon, i.any_sugar], i.lemonade, e, 'lemonade')`,
     and `R.frBrewing([i.lemonade, fruit], variant, e, ..., { needWater: false })` for pink / blue.
4. Optional IE route (bottling machine) through `R.ieBottling`, as cooking.js does for melon juice (line 53).

### Ids and data
Verified: lemon juice item / fluid / recipes above. New: 3 fluids, 3 items under `creativeengineering:`.

### Decisions
- **D1 Bucket.** Recommend `noBucket()` (drinks move in bottles and pipes, like CCK's); a bucket also needs a stack size
  decision (`food/ingredients #2` asks "bucket stack size 16?").
- **D2 Effects.** Recommend lemonade = lemon juice's effects at 3x duration (Haste + Refreshing 30 s); pink = lemonade +
  Regeneration I 5 s (strawberries carry no effect, so either borrow a mild one or give it more saturation instead);
  blue = lemonade + Night Vision + Brightening 20 s (blueberry's).
- **D3 Pink ingredient.** Strawberries (recommended; Neapolitan, common) or cranberries (Fruits Delight, but their
  effect is Shrinking).
- **D4 Lemon juice vs lemonade.** Keep both: lemon juice is the ingredient, lemonade the diluted, sweetened drink.

### Gotchas
- A shapeless recipe with two bottled inputs and two bottled outputs duplicates a bottle (the lemon juice hands back
  its bottle as a remainder). That's why the manual route is the kettle.
- The water-bottle ingredient of Fruits Delight's shapeless recipe is an `l2library:potion` ingredient type, not NCH's
  `i.water_bottle`; leave that recipe alone.

### Tests
- RC-33 kettle: 2 lemon + sugar → lemonade; lemonade + strawberry / blueberry → pink / blue.
- RC-34 mixer → lemonade fluid; spout fills a bottle, drain empties it, for all three.
- RC-35 drinking: drink animation, effects, bottle back.

---

## drinks #2: Tea cleanup
Status **Planned** · Work: KubeJS (+ patcherMod for the tooltip, textures) · id `food/drinks #2` · overlaps `food/placeable #3`

### Goal
Merge duplicate teas; Create mixing recipes dearer than the manual ones; recoloured, placeable teas (Display Delight);
a tooltip on tea fluids showing the drink's effect and duration. Upgraded teas (drink + milk / honey) no longer throw a
bottle or need plating by hand.

### Now
- **"Duplicates" are FR's own tiers.** Each FR drink has base, `long_` (+ `#forge:milk`) and `strong_` (+ honey bottle)
  versions (verified recipes `farmersrespite:brewing/<x>`, 2400 ticks). The long and strong items use the **same name**
  (FR lang: `item.farmersrespite.long_green_tea = Green Tea`) and the **same model and texture** as the base
  (`models/item/long_green_tea.json` → `farmersrespite:item/green_tea`, mug parent). Nothing tells them apart but the
  tooltip effect. That is the "duplicate" and "recolour" problem.
- Other teas: Delightful lavender and azalea tea (kettle), Fruits Delight hawberry / lychee cherry / mango / mangosteen /
  peach tea (shapeless and Fruits Delight fluids), Create builder's tea. Delightful's own green tea leaf is switched off
  (`green_tea_leaf = false`, delightful-common.toml:104), so FR's leaves are the only ones.
- **Create mixing already exists** for the base FR teas via CCK's built-in `datapacks/farmersrespite`:
  `create_central_kitchen:mixing/{green,yellow,black,dandelion,rose_hip,purulent}_tea`, `coffee`,
  `builders_tea_from_tea_leaves` (green tea checked: heated, 2 leaves + 250 mB water → 250 mB = one bottle, **the same
  cost as the kettle**; read the others before rewriting them),
  plus filling / emptying for every FR drink and fluids `create_central_kitchen:<drink>` (all registered: FR is loaded).
  No mixing for long / strong tiers. CCK also has `mixing/apple_cider`, `melon_juice`, `hot_cocoa` (FD datapack).
- **Upgrade plating:** `KettleRecipe` (verified) uses the recipe's `container`, or the result's crafting remainder when
  that is empty, and every FR drink has a glass-bottle remainder. So a drink always waits to be plated; a recipe change
  can't remove that. The built remainder buffer (patcherMod `KettleRemainderMixin`, remainder_buffer.md 3.1) recycles
  one of the two returned bottles into the container slot, so the drink plates itself and the second bottle waits in
  the buffer. Nothing is thrown and the player supplies no bottle. Not yet seen in game (RB-03).
- Placing: none of the FR drinks is placeable; the [food_placeable brief](food_placeable.md) (`placeable #3`) covers
  the Display Delight blocks for all bottled drinks.
- Fluid tooltips: [fluid_jei.js](../../kubejs/client_scripts/fluid_jei.js) only adds fluids to JEI's list. No fluid line
  shows effects.

### Build
1. **Names (cleanup instead of merging).** In [namechanges.js](../../kubejs/client_scripts/namechanges.js)
   `ClientEvents.lang('en_us')`: `e.add('item.farmersrespite.long_green_tea', 'Long Green Tea')`,
   `'Strong Green Tea'`, likewise for yellow, black, rose hip, dandelion, purulent, gambler's tea, coffee, apple cider,
   strong hot cocoa, strong melon juice (19 keys, listed in the FR lang file). Keep the item ids.
2. **Recolour.** Give long and strong their own sprites and models: `kubejs/assets/farmersrespite/models/item/long_<x>.json`
   → `farmersrespite:item/long_<x>` with a lighter (milk) / darker (honey) liquid, sprites in
   `kubejs/assets/farmersrespite/textures/item/`. Display Delight models from `placeable #3` then pick up the colour.
3. **Dearer mixing.** Remove CCK's 7 tea mixing recipes (`R.remove({ id: 'create_central_kitchen:mixing/green_tea' })` ...)
   and re-add through NCH at 3 leaves per 250 mB:
   `R.createMixing(Fluid.of('create_central_kitchen:green_tea', 250), [Item.of(i.green_tea_leaves, 3), Fluid.of(f.water, 250)], e, 'green_tea', 'heated')`
   (add `f.cck_green_tea` ... keys to fluid_dict.js). Upgrades by fluid: `[Fluid.of(green_tea, 250), Fluid.of(f.milk, 250)]`
   → 250 long green tea (half the milk is lost, so it stays dearer than the kettle's 1 tea + 1 milk bottle); honey
   `Fluid.of(f.honey, 250)` for strong. CCK's filling recipes then bottle them.
4. **Upgrade plating.** No code. Run RB-03; if it passes, the remainder half of this row is done.
5. **Placeable:** build in `food/placeable #3`.
6. **Fluid effect tooltip** (D2): a patcherMod client hook. JEI draws fluid tooltips through its Forge fluid helper
   (`mezz.jei.forge.platform.FluidHelper#getTooltip`, unverified, decompile the JEI jar first). Append the effect lines
   of the item the fluid fills: build a fluid → item map from `create:filling` recipes whose input is a glass bottle (or
   bowl), then reuse vanilla `PotionUtils.addPotionTooltip` style lines from the item's `FoodProperties` effects.

### Ids and data
- Verified: FR recipe JSON, item names, models; CCK recipe ids and fluid list (lang `fluid.create_central_kitchen.*`);
  KettleRecipe container rule.
- Dict: `i.green_tea_leaves` exists (baking.js:46 uses it). Check the other leaves keys before use.

### Decisions
- **D1 Merge or rename.** Recommend rename + recolour (step 1-2); no item merges. The tiers are a real mechanic
  (milk = longer, honey = stronger).
- **D2 Fluid tooltip.** Recommend doing it only for JEI first (the place players look at fluids); Create's goggles and
  tank tooltips later if wanted.
- **D3 Mixing cost.** 3 leaves per bottle-equivalent (recommended) vs keeping 2 leaves with a heat requirement of
  superheated.

### Gotchas
- CCK's recipes live in a built-in datapack loaded with the mod; `R.remove` reaches them like any recipe (they are in the
  recipe manager by the time KubeJS runs), but check JEI.
- The Brewin' port's kombucha uses `create_central_kitchen:green_tea` (alcohol.md 4.5); keep that fluid.

### Tests
- RC-36 JEI: long and strong teas show their own names and colours.
- RC-37 mixer: 3 leaves + 250 mB water heated → 250 mB tea; tea + milk → long tea fluid; spout bottles both.
- RC-38 (= RB-03) kettle strong green tea: no item thrown, the drink plates itself, one bottle in the buffer.

---

## drinks #3: Merges
Status **Planned** · Work: KubeJS · id `food/drinks #3`

### Goal
One item per drink or crop where two mods add the same thing: coffee, bananas (Alex's Mobs' goes), melon juice,
chocolate cake, milk bottles (Neapolitan's goes), apple ciders; banana bunches crafted or cut into bananas.

### Now and Build, per merge

**a. Milk bottles (do first).**
- `farmersdelight:milk_bottle` (`i.farmersdelight_milk_bottle`, item_dict.js:11546) and `neapolitan:milk_bottle` (no
  dict key). Tags (verified): FD's is in `#forge:milk/milk_bottle` (the pack's `i.t_milk_bottle`, item_dict.js:1908, used
  by muffins and tarts in cooking.js:366-369 and baking.js:36-58); Neapolitan's is only in `#forge:bottles/milk`. Both
  end up in `#forge:milk`. So **a Neapolitan milk bottle doesn't work in the muffin and tart recipes today.**
- Abnormals Delight overrides FD's `farmersdelight:milk_bottle` (bucket + bottles → FD bottles) and
  `milk_bucket_from_bottles` with a "Neapolitan not loaded" condition, so with Neapolitan in the pack the crafting route
  to FD milk bottles is probably off (which file wins the id depends on datapack order: check JEI in game).
  Neapolitan's `neapolitan:milk/milk_bucket_from_bottles` makes 3 Neapolitan bottles from a milk bucket. Create's spout
  has a filling recipe for each (`create:filling/compat/farmersdelight/milk_bottle`,
  `.../neapolitan/milk_bottle`, same input). Neapolitan's milk cauldron also hands out its own bottle (Java).
- Build: FD's bottle wins (it's the one the pack's tags, kettle containers, coasters and the Brewin' port use).
  Add `i.neapolitan_milk_bottle = 'neapolitan:milk_bottle'`; add it to `global.removedItems`; `R.remove` the ids
  `neapolitan:milk/milk_bucket_from_bottles` (3 Neapolitan bottles), `create:filling/compat/neapolitan/milk_bottle`,
  `delightful:food/kettle/matcha_latte_neapolitan`; re-add FD's two crafts through NCH
  (`R.shapeless(Item.of(i.farmersdelight_milk_bottle, 4), [i.milk_bucket, Item.of(i.glass_bottle, 4)], e, 'milk_bottle')`,
  and bucket + 4 bottles → milk bucket); keep `create:emptying/compat/neapolitan/milk_bottle` and add a 1:1 shapeless
  Neapolitan → FD bottle so old stacks convert. Set Neapolitan's "Milk Cauldron" off in neapolitan-common.toml
  (`[items.milk_bottles]`) or accept that cauldrons still hand out the old bottle (D1).

**b. Coffee.**
- Two plants and two drink families (verified): FR `farmersrespite:coffee_beans` (`i.farmersrespite_coffee_beans`,
  13125) brew FR Coffee (caffeinated, long / strong tiers) and go into coffee cake, blazing chili, CCK's coffee mixing;
  Rustic Delight `rusticdelight:coffee_beans` (`i.coffee_beans`, 12982) roast into `roasted_coffee_beans` and pot-cook
  into RD Coffee (Speed + Haste) and its milk / honey / chocolate / dark coffees, coffee-braised beef. FR coffee has no
  source in the world any more (crops.md V18); RD coffee grows wild in jungles.
- Build: RD beans win. `R.replaceInput({ input: i.farmersrespite_coffee_beans }, i.farmersrespite_coffee_beans, i.coffee_beans, e)`
  (reaches FR's kettle coffee, coffee cake, blazing chili, CCK `mixing/coffee`; also change the pack's own
  [baking.js:101](../../kubejs/server_scripts/cooking/baking.js#L101) coffee cake to `i.coffee_beans`). Hide FR beans,
  bean sack and coffee bush items (removed_items.js), and remove the recipes `farmersrespite:coffee_beans`,
  `coffee_beans_sack`, `cutting/coffee_berries`. Coffee berries and gambler's tea depend on D2. Drinks: keep both
  families (D2).

**c. Bananas.**
- `alexsmobs:banana` (`i.banana`, item_dict.js:10061; 4 / 0.3) and `neapolitan:banana` (`i.neapolitan_banana`, 12402;
  2 / 0.2, Agility). Both are in `#forge:fruits/banana` (verified). AM bananas no longer drop from leaves
  (`config/alexsmobs.toml bananasDropFromLeaves = false`, crops.md); their last source is the trader elephant chest
  loot (`alexsmobs:gameplay/trader_elephant_chest`, verified). Gorillas and capuchins eat `#alexsmobs:bananas`
  (= `#forge:crops/banana` + `#forge:fruits/banana`), which keeps working with Neapolitan's.
- Build: rename the dict key `i.banana` → `i.alexsmobs_banana` (also [compost_values.js:301](../../kubejs/startup_scripts/compost/compost_values.js#L301)
  and the two `"banana"` entries in food_types.json, via `tools/food_catalog.py`); add it to removed_items.js; swap the
  elephant chest entry to `neapolitan:banana` in the LootJS layer (`kubejs/server_scripts/loot/`, `loot` family).
- **Banana bunch:** `neapolitan:banana_bunch` (`i.banana_bunch`, 12932) opens on right click into 1-3 bananas
  (`BananaBunchItem.handleOpening`, verified; it also spawns a banana peel). Add
  `R.shapeless(Item.of(i.neapolitan_banana, 2), i.banana_bunch, e, 'banana_bunch')` and
  `R.fdCutting(i.banana_bunch, i.any_knife, Item.of(i.neapolitan_banana, 3), e, 'banana_bunch')` (knife bonus).

**d. Melon juice:** already merged at the fluid level since the first commit
([cooking.js:45-54](../../kubejs/server_scripts/cooking/cooking.js#L45-L54): tmted and CCK mixing removed, IE squeezing,
IE bottling and a Create mixing on `create_central_kitchen:melon_juice`; `tmted:melon_juice` is a dead fluid in
[fluidtags.js:102](../../kubejs/server_scripts/tags/fluidtags.js#L102)). There is only one melon juice item
(`farmersdelight:melon_juice`). Nothing left to build.

**e. Apple ciders.**
- One non-alcoholic item, `farmersdelight:apple_cider`, made two ways (FD pot 2 apples + sugar; FR kettle 1 apple +
  sugar + water), with FR's long / strong tiers on top. Fluids: `create_central_kitchen:apple_cider` (`f.apple_cider`,
  CCK `mixing/apple_cider`, filling, emptying) and the dead `tmted:apple_cider`. **Leftovers:** `tmted:mixer/apple_cider`
  (IE mixer: apple + sugar + 500 mB ethanol → dead fluid) and `tmted:bottling/apple_cider` (fluid tag
  `forge:apple_cider`) are not removed anywhere. Vinery's `vinery:apple_cider` is alcoholic (alcohol.md 11).
- Build: `R.remove` the two tmted ids; optional IE routes on the CCK fluid as for melon juice. Keep Vinery's as the
  alcoholic one (D3).

**f. Chocolate cake:** the same merge is in `food/baked-goods #5` with its decision (keep the Crafts & Additions
texture, both the spout and the baking recipe). Build it there.

### Ids and data
Verified: every recipe id and tag above. Dict keys to add: `i.neapolitan_milk_bottle`, `i.alexsmobs_banana` (rename).

### Decisions
- **D1 Neapolitan milk cauldron.** Recommend turning "Milk Cauldron" off (one bottle in the pack) unless cauldron milk
  storage is wanted; then accept the conversion recipe.
- **D2 Coffee drinks.** Recommend keeping both families (FR = Caffeinated, kettle tiers; RD = Speed + Haste, pot) and
  renaming RD's plain one "Brewed Coffee" or FR's "Kettle Coffee" in namechanges.js so the two "Coffee" items differ.
  Gambler's tea needs coffee berries: either keep FR berries as a cut product of RD beans or drop the tea.
- **D3 Apple cider.** Recommend FD's cider (+ FR tiers) as the only non-alcoholic one, Vinery's renamed "Hard Cider".

### Gotchas
- `removedItems` strips every tag from the item; check that no recipe needs `alexsmobs:banana` by id first (the
  `[jeiRemove]` log line lists them).
- `R.replaceInput` pack-wide also hits recipes that should keep FR beans (the FR bean sack itself): remove those first.

### Tests
- RC-39 milk: bucket + 4 bottles → 4 FD milk bottles; spout makes FD bottles only; a muffin accepts the converted bottle.
- RC-40 coffee: FR kettle coffee and coffee cake take RD beans; FR beans are hidden.
- RC-41 bananas: banana bunch crafts into 2 / cuts into 3 bananas; AM banana hidden; gorillas still take bananas.
- RC-42 cider: JEI shows no tmted cider recipe.

---

## drinks #4: Chorus juice
Status **Planned** · Work: KubeJS / data · id `food/drinks #4` · needed by `food/baked-goods #4` (chorus pie)

### Goal
Chorus juice gets a heated mixing recipe and a pot recipe, becomes a fluid, and is used in the ender pasta and the
chorus pie. Maybe a progression fluid or a diluter later.

### Now
- `endersdelight:chorus_juice` (`i.chorus_juice`, item_dict.js:1869): 0 / 0, Absorption 60 s, bottle. Only recipe
  (verified) `endersdelight:chorus_juice` shapeless `[chorus_fruit, sugar, glass_bottle]`.
- Uses: Ender's Delight pot `endersdelight:cooking/pearl_pasta` `[#endersdelight:enderman_loot, #forge:pasta,
  chorus_juice]`, shulker bowl; the pack's shapeless pearl pasta with 2 chorus juice
  ([cooking.js:418](../../kubejs/server_scripts/cooking/cooking.js#L418)). Chorus pie: Ender's Delight shaped (wheat,
  chorus fruit, sugar, pie crust) and the oven recipe ([baking.js:64-65](../../kubejs/server_scripts/cooking/baking.js#L64-L65), 3 chorus fruit).
- No chorus juice fluid: CCK's chorus fluids are for Ends Delight (not installed; `EDFluidEntries` not loaded).

### Build
1. Fluid `creativeengineering:chorus_juice` in `fluids.js` (purple, `thinTexture(0xB283C9)`, `noBlock()`), key
   `f.chorus_juice`.
2. Recipes in `server_scripts/cooking/drinks.js`:
   - `R.createMixing(Fluid.of(f.chorus_juice, 250), [i.chorus_fruit, i.any_sugar, Fluid.of(f.water, 250)], e, 'chorus_juice', 'heated')`
   - `R.fdCooking([i.chorus_fruit, i.chorus_fruit, i.any_sugar], Item.of(i.chorus_juice, 2), e, 'chorus_juice_manual_only', 0.35, 200, i.glass_bottle)`
     (`_manual_only` so Slice & Dice doesn't add a second mixer route next to the fluid one)
   - spout / drain: `R.createFilling(i.chorus_juice, [i.glass_bottle, Fluid.of(f.chorus_juice, 250)], e)`,
     `R.createEmptying([i.glass_bottle, Fluid.of(f.chorus_juice, 250)], i.chorus_juice, e)`
   - keep or drop the shapeless (D2).
3. Uses: pearl pasta already takes chorus juice (pot + the pack's shapeless). Chorus pie: a sequenced assembly with a
   chorus juice spouting step is `food/baked-goods #4`; it needs only the fluid from step 1.

### Ids and data
Verified: item, recipes and uses above. New: fluid `creativeengineering:chorus_juice`.

### Decisions
- **D1 Role.** Recommend a drink + cooking fluid only for now; "progression fluid / diluter for any tea" waits for a
  concrete use.
- **D2 Shapeless craft.** Recommend keeping it (an early route before the pot), at 1 fruit + sugar + bottle.

### Gotchas
- Chorus fruit block unpacking is missing (`food/ingredients #8`); a bulk chorus route for the mixer depends on it.

### Tests
- RC-43 heated mixer → chorus juice fluid; spout fills, drain empties; pot makes 2 bottles from 2 fruit + sugar.

---

## drinks #5: Berry matcha with Farmer's Respite
Status **Planned** · Work: KubeJS · id `food/drinks #5`

### Goal
Integrate Delightful's (berry) matcha lattes with Farmer's Respite's kettle.

### Now (verified, Delightful 3.4.1)
- With FR loaded, Delightful **turns off** its shapeless `food/matcha_latte` and `food/berry_matcha_latte(_neapolitan)`
  (`not mod_loaded farmersrespite`). What's left:
  - `delightful:food/kettle/matcha_latte`: kettle `[matcha, honey_bottle]`, water, **container `farmersdelight:milk_bottle`**
    (the milk is the "bottle"); `kettle/matcha_latte_neapolitan` the same with `neapolitan:milk_bottle` (goes with
    `drinks #3 a`).
  - `food/berry_matcha_latte_from_matcha_latte_neapolitan`: shapeless `[glass_bottle, matcha_latte, #forge:fruits/berries,
    #forge:ice_cubes]` (active because `#forge:ice_cubes` isn't empty). It eats an extra glass bottle: the latte hands
    its bottle back as a remainder and the output takes the bottle input too.
- So berry matcha latte has no kettle recipe. Values: matcha latte 6 / 0.5, berry 8 / 0.65 (both Haste II + Caffeinated
  60 s in this pack).
- Ids: `i.matcha` (item_dict.js:115), `i.matcha_latte` (11322), `i.berry_matcha_latte` (11261),
  `i.farmersdelight_milk_bottle` (11546); no `i.t_fruits_berries` key (`#forge:fruits/berries` is Delightful's tag).

### Build
1. NCH `frBrewing` (Shared).
2. `R.frBrewing([i.matcha_latte, i.t_fruits_berries], i.berry_matcha_latte, e, 'berry_matcha_latte', { needWater: false })`
   (a tier like FR's: drink + one ingredient; its two leftover bottles go through the remainder buffer as in
   `drinks #2`).
3. Replace the bottle-eating shapeless: `R.remove({ id: 'delightful:food/berry_matcha_latte_from_matcha_latte_neapolitan' })`,
   then `R.shapeless(i.berry_matcha_latte, [i.matcha_latte, i.t_fruits_berries, i.t_ice_cubes], e, 'berry_matcha_latte')`.
4. Create: CCK-style fluids aren't needed; skip unless lattes join the drink fluids later.

### Ids and data
Add `i.t_fruits_berries = '#forge:fruits/berries'`, `i.t_ice_cubes` (shared with food_sweets.md).

### Decisions
- **D1 Kettle or shapeless.** Recommend both (kettle for the FR route, a fixed shapeless as the cold "iced" route).

### Gotchas
- The kettle recipe's container defaults to the result's remainder (glass bottle); with `container` explicit in the
  helper this is the same.

### Tests
- RC-44 kettle: matcha latte + sweet berries → berry matcha latte, no bottle thrown.
- RC-45 grid: latte + berries + ice → berry latte, one bottle back, none lost.

---

## drinks #6: Milk from a cow in a jar
Status **Planned** · Work: KubeJS · id `food/drinks #6` · after `drinks #3 a` (which bottle)

### Goal
Right click a Cooking for Blockheads Cow in a Jar with a glass bottle to get a milk bottle.

### Now (verified, CfB 13.3.4)
- `cookingforblockheads:cow_jar` (`CowJarBlock extends MilkJarBlock`), `cookingforblockheads:milk_jar`. Block entity
  `MilkJarBlockEntity`: Balm `FluidTank` of 32000 mB milk (`getFluidTank()`), Cow in a Jar adds
  `cowJarMilkPerTick` = 1 mB per tick ([config](../../config/cookingforblockheads-common.toml), so 20 mB/s, a bottle every
  12.5 s). `MilkJarBlock.use` handles only a milk bucket (fill) and an empty bucket (take 1000 mB); anything else
  returns FAIL. Tank calls: `getAmount()`, `drain(Fluid, int, boolean simulate)`, milk fluid via Balm's
  `getMilkFluid()` (Forge milk, `minecraft:milk`). Exposed as a Balm fluid provider, so Create pipes can probably pull
  from it (in game).
- No CfB ids in item_dict. Neapolitan's "Milking with Glass Bottles" is off (neapolitan-common.toml), so bottles don't
  milk cows either.

### Build
1. Keys `i.cow_jar = 'cookingforblockheads:cow_jar'`, `i.milk_jar = 'cookingforblockheads:milk_jar'`.
2. New `kubejs/server_scripts/cooking/milk_jar.js`:
   ```js
   var $MilkFluid = Java.loadClass('net.minecraftforge.common.ForgeMod').MILK
   ;[global.item.cow_jar, global.item.milk_jar].forEach(id => BlockEvents.rightClicked(id, e => {
     if (!e.item.is(global.item.glass_bottle)) return
     var tank = e.block.entity.getFluidTank()
     if (tank.getAmount() < 250) return
     tank.drain($MilkFluid.get(), 250, false)
     e.item.shrink(1)                                  // creative: skip (e.player.isCreative())
     e.player.give(global.item.farmersdelight_milk_bottle)
     e.cancel()
   }))
   ```
3. Optional: the same for a milk bottle onto the jar (fill 250), mirroring the bucket behaviour.

### Ids and data
Verified: classes and methods above. Unverified: that `ForgeMod.MILK` is the fluid Balm returns on Forge (it should be,
Create switches Forge milk on); `e.block.entity` returning the CfB block entity class in Rhino.

### Decisions
- **D1 Milk jar too?** Recommend yes (same block entity, the player-filled jar).

### Gotchas
- `drain(..., false)`: the boolean is *simulate* (CfB passes false to drain for real). The tank's `setChanged` syncs the
  client.

### Tests
- RC-46 bottle on a full Cow in a Jar → FD milk bottle, tank −250 mB; empty jar does nothing; buckets still work.

## Writeup corrections made

None.
