# Brief: food / drinks

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# food`, Sub `drinks` (rows 1 to 6, then 7 to 12); verified and
updated 2026-10-31. Writeups:
[food_catalog.md](../food_catalog.md) (`drink` table), [remainder_buffer.md](../remainder_buffer.md) (kettle leftovers,
RB-03), [crops.md](../crops.md) V5 / V18 (coffee), [placeable_food.md](../placeable_food.md) and the
[food_placeable brief](food_placeable.md) (Display Delight placing, `food/placeable #3`), [alcohol.md](../alcohol.md)
(milk / honey fluids, apple cider note B7.15). Tests use `RC-` ([recipes_cooking.md](../recipes_cooking.md));
[baking_sweets.md](baking_sweets.md) proposes RC-19 to RC-32, so these start at RC-33. The cooking plan these rows came
from is in git (`git show c82dd31^:feature_writeup/cooking.md`, "drink", "dairy", "fluid").

**Verified against jars** (scratchpad cache, Vineflower): Farmer's Respite (FR) 1.19-2.0, Farmer's Delight (FD) 1.2.4,
Delightful 3.4.1, Neapolitan 4.1.0, Abnormals Delight 4.1.2, Create 0.5.1.i, Create Central Kitchen (CCK) 1.3.11.c,
Rustic Delight 1.3.0, Ender's Delight 1.2.2, Fruits Delight 0.5.11, Alex's Mobs 1.21.1, Engineer's Delight (tmted)
R.1.3, Cooking for Blockheads (CfB) 13.3.4, KubeJS 1902.6.2-build.73. Facts that need the game are marked "in game".

## Scope and order

Rechecked 2026-10-31 (sweep): #4, #5, #6, #9 are Testing and listed under Built, needs play; #3 is down to the banana
bunch; #11 and #12 are new sections. Status per TODO.

| Row | Status | Size | Build with |
|---|---|---|---|
| `food/drinks #12` Spout filling for Kettle drinks fails to load | Bug | small, patcherMod (NCH) | first: every Kettle drink pair and the Kettle tank's Spout half wait on it |
| `food/drinks #3` Merges (banana bunch left) | In Progress | small | any time |
| `food/drinks #7` Kettle drinks: the rest of the move | In Progress | medium | after #12; with #8 |
| `food/drinks #8` Juice and tea rule | In Progress | medium | with #7 (one table) |
| `food/drinks #10` Azalea tea and tea audit | Planned | small | with #7 / #8 (the audit table) |
| `food/drinks #2` Tea cleanup | In Progress | medium | names and colours any time; mixing with #8 |
| `food/drinks #1` Lemonade | Planned | medium (3 fluids, 3 items) | after #12 (it needs bottle pairs) |
| `food/drinks #11` Cherry Cream Soda and fruit sodas | Planned | small to medium | after the decisions |

**Shared pieces (state 2026-10-31):**
- **NCH Kettle helper `frBrewing` exists** ([neoCraftHelper.js:1113](../../development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js#L1113);
  the shared helpers live in patcherMod's jar, so an edit needs a patcherMod rebuild and restart). It writes Creative
  Delight's tank format (farmers_respite.md K8): 1-2 ingredients, `opts.base` (the tank fluid one brew uses, 250 mB,
  default water, `'none'` for none), `container` (glass bottle), `time` 200, `exp` 0.35, one result. Drink tiers are the
  drink's fluid as base plus one ingredient.
- **`NCH.createBottle(drink, fluid, e, id, opts)`** (line 1883) writes the Spout / Item Drain pair the Kettle reads to pour
  and bottle (K10). Its filling half does not load today: `food/drinks #12`.
- **Startup fluid file** [fluids.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js)
  (Creative Delight, moved 2026-10-25): pack fluids are virtual (`noBucket().noBlock()`), a `drink(id, name, color)`
  shorthand at line 25 for Kettle drinks; ids in [fluid_dict.js](../../kubejs/startup_scripts/_lib/fluid_dict.js)
  ("Pack fluids", e.g. `f.chorus_juice` line 89, `f.milk_coffee` 102), `forge:fluid/<name>` tags in
  [fluidtags.js](../../kubejs/data/creativeengineering/kubejs/tags/fluidtags.js). JEI shows a bottle's effects on its fluid through the
  emptying recipe (patcherMod `compat/DrinkFluidEffects.java`), so a drink fluid needs a working `createBottle` pair.
- **Kettle recipes** live in Creative Delight's
  [farmersrespite/kettle.js](../../kubejs/data/creativedelight/kubejs/farmersrespite/kettle.js) (109 lines: section 1
  the mods' recipes rewritten on a base, 2 Rustic coffees and Fruits Delight teas, 3 the `createBottle` pairs at 98-107).
  New Kettle drinks go there (non-member file, ids `creativedelight:<type>/<suffix>`).
- **Drink items in KubeJS:** `.useAnimation('drink')`, `.containerItem(i.glass_bottle)`, `.maxStackSize(16)` like FR /
  FD drinks, `f.eaten(ev => ev.player.give(i.glass_bottle))` for the bottle back (callback form in
  [cans.js:80](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js#L80)).
- After new drinks: `/dumpfood` -> `tools/food_catalog.py` (type `drink`), `gen_stack_sizes.py`, a row in
  can_parity.md, and `food/placeable #3`'s tool picks them up.

Tests use `RC-` ([recipes_cooking.md](../recipes_cooking.md), last id RC-175). RC-33 to RC-42 below were proposed in
2026-10-21 and are not in the writeup; new sections use `RC-new<n>` placeholders.

---

## drinks #1: Lemonade
Status **Planned** (rechecked 2026-10-31: nothing built) · Work: KubeJS / data (+ textures) · id `food/drinks #1`

### Goal
A lemonade fluid with pink and blue variants; their effects come from what goes in.

### Now
- No lemonade. Fruits Delight has Lemon Juice: item `fruitsdelight:lemon_juice` (`i.lemon_juice`, item_dict.js:12449;
  1 / 0.2, Haste 10 s + Refreshing 10 s, bottle) and fluid `fruitsdelight:lemon_juice` (250 mB per bottle). Recipes
  (verified): shapeless `[water bottle (l2library:potion water), sugar, #forge:fruits/lemon x2]`; Create mixing
  `fruitsdelight:mixing/flowing_lemon_juice` `[sugar, 2 lemons, 250 mB water]` → 250 mB; emptying / filling.
- Canned Goods' "Oculemonade" (`canned_goods:oculemonade`, `i.oculemonade`) is an unrelated can drink (name only).
  Fruits Delight's `fruitsdelight:lemonade_cauldron` (`i.lemonade_cauldron`, lang "Lemonade Cauldron") is its cauldron
  of lemon juice; the name overlaps a pack lemonade.
- Effect ids: `fruitsdelight:refreshing`, `fruitsdelight:brightening` (blueberry), Neapolitan strawberries have no effect.

### Build
1. Fluids in `fluids.js` with the `drink(...)` shorthand: `creativeengineering:lemonade` (pale yellow), `pink_lemonade`,
   `blue_lemonade`; keys `f.lemonade` ... in fluid_dict.js "Pack fluids"; add `f.lemon_juice = 'fruitsdelight:lemon_juice'`
   (no key today).
2. Items `creativeengineering:lemonade`, `pink_lemonade`, `blue_lemonade` (drink item shape from Shared), food e.g.
   `f.hunger(2).saturation(0.3).alwaysEdible()` + effects (D2). Keys `i.lemonade` ... in a "Pack sweets and drinks"
   section of item_dict.js (shared with baking_sweets.md). Sprites: recolour Fruits Delight's lemon juice bottle.
3. Recipes in a Creative Delight tree (food code routes there, memory `creative-delight-routing`; e.g.
   `kubejs/data/creativedelight/kubejs/fruitsdelight/` for the lemon routes, Kettle ones in `farmersrespite/kettle.js`):
   - Create: `R.createMixing(Fluid.of(f.lemonade, 500), [Fluid.of(f.lemon_juice, 250), Fluid.of(f.water, 250), i.any_sugar], e, 'lemonade')`;
     pink: `[Fluid.of(f.lemonade, 500), i.t_fruits_strawberry]` → 500 pink; blue: `[Fluid.of(f.lemonade, 500), i.t_fruits_blueberry]` → 500 blue.
   - Spout / drain per variant: `R.createBottle(i.lemonade, f.lemonade, e, 'lemonade')` (after #12 is fixed).
   - Kettle (water base): `R.frBrewing([i.t_fruits_lemon, i.t_fruits_lemon], i.lemonade, e, 'lemonade')`; pink / blue as
     tiers on the lemonade in the tank: `R.frBrewing([i.t_fruits_strawberry], i.pink_lemonade, e, 'pink_lemonade',
     { base: f.lemonade })`. Sugar stays out of the Kettle (`drinks #7` rule, 2026-10-24).
4. Optional IE route (bottling machine) through `R.ieBottling`, as Creative Delight's farmersdelight/recipes.js:137 does for melon juice.

### Ids and data
Verified: lemon juice item / fluid / recipes above. New: 3 fluids, 3 items under `creativeengineering:`.

### Decisions
- **D1 Bucket.** Recommend `noBucket()` (drinks move in bottles and pipes, like CCK's); a bucket also needs a stack size
  decision (`dishes/ingredients #2` asks "bucket stack size 16?").
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
Status **In Progress** (fluid effect tooltip built 2026-10-22; rest open, rechecked 2026-10-31) · Work: KubeJS (+ textures) · id `food/drinks #2` · overlaps `food/placeable #3`

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
- **Since 2026-10-25 the Kettle is a tank** (farmers_respite.md 2.1, 5): long / strong tiers brew from the base tea's
  fluid plus milk or a honey bottle (kettle.js LONG / STRONG, lines 39-56), and spare bottles go to the output buffer
  (K13). The old "two bottles per upgrade" problem below is from the item design; RB-03 still checks the honey bottle's
  leftover.
- **Upgrade plating (2.0 design, superseded by the tank):** `KettleRecipe` (verified) uses the recipe's `container`, or the result's crafting remainder when
  that is empty, and every FR drink has a glass-bottle remainder. So a drink always waits to be plated; a recipe change
  can't remove that. The built remainder buffer (patcherMod `KettleRemainderMixin`, remainder_buffer.md 3.1) recycles
  one of the two returned bottles into the container slot, so the drink plates itself and the second bottle waits in
  the buffer. Nothing is thrown and the player supplies no bottle. Not yet seen in game (RB-03).
- Placing: none of the FR drinks is placeable; the [food_placeable brief](food_placeable.md) (`placeable #3`) covers
  the Display Delight blocks for all bottled drinks.
- Fluid tooltips: **built 2026-10-22** (patcherMod `compat/DrinkFluidEffects.java`: JEI fluid tooltips list the drink
  item's effects, item_merges.md 2.6, MG-20). Step 6 below is done.
- Names and colours: not done (no `long_*` / `strong_*` lang keys in namechanges.js, no `kubejs/assets/farmersrespite/`
  textures). CCK tea mixing untouched.

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
   (keys exist: `f.green_tea`, `f.black_tea` ... fluid_dict.js lines 71-72). Upgrades by fluid: `[Fluid.of(green_tea, 250), Fluid.of(f.milk, 250)]`
   → 250 long green tea (half the milk is lost, so it stays dearer than the kettle's 1 tea + 1 milk bottle); honey
   `Fluid.of(f.honey, 250)` for strong. CCK's filling recipes then bottle them.
4. **Upgrade plating.** No code. Run RB-03; if it passes, the remainder half of this row is done.
5. **Placeable:** build in `food/placeable #3`.
6. ~~**Fluid effect tooltip**~~ (done 2026-10-22, `DrinkFluidEffects`; text kept for reference): a patcherMod client hook. JEI draws fluid tooltips through its Forge fluid helper
   (`mezz.jei.forge.platform.FluidHelper#getTooltip`, unverified, decompile the JEI jar first). Append the effect lines
   of the item the fluid fills: build a fluid → item map from `create:filling` recipes whose input is a glass bottle (or
   bowl), then reuse vanilla `PotionUtils.addPotionTooltip` style lines from the item's `FoodProperties` effects.

### Ids and data
- Verified: FR recipe JSON, item names, models; CCK recipe ids and fluid list (lang `fluid.create_central_kitchen.*`);
  KettleRecipe container rule.
- Dict: `i.green_tea_leaves` exists (item_dict.js 13158). Check the other leaves keys before use.

### Decisions
- **D1 Merge or rename.** Recommend rename + recolour (step 1-2); no item merges. The tiers are a real mechanic
  (milk = longer, honey = stronger).
- **D2 Fluid tooltip.** Done (JEI). Was: recommend doing it only for JEI first (the place players look at fluids); Create's goggles and
  tank tooltips later if wanted.
- **D3 Mixing cost.** 3 leaves per bottle-equivalent (recommended) vs keeping 2 leaves with a heat requirement of
  superheated.

### Gotchas
- CCK's recipes live in a built-in datapack loaded with the mod; `R.remove` reaches them like any recipe (they are in the
  recipe manager by the time KubeJS runs), but check JEI.
- The Brewin' port's kombucha uses `create_central_kitchen:green_tea` (alcohol.md 4.5); keep that fluid.
- Added scope (scratchpad 2026-10-24): the azalea tea recipe and a full tea audit are `drinks #10`; do them with this
  row's merges so each tea is checked once.

### Tests
- RC-36 JEI: long and strong teas show their own names and colours.
- RC-37 mixer: 3 leaves + 250 mB water heated → 250 mB tea; tea + milk → long tea fluid; spout bottles both.
- RC-38 (= RB-03) kettle strong green tea: no item thrown, the drink plates itself, one bottle in the buffer.

---

## drinks #3: Merges
Status **In Progress** · Work: KubeJS · id `food/drinks #3`

### Goal
One item per drink or crop where two mods add the same thing; the last open part is the banana bunch.

### Now (2026-10-31)
- **Built and scratch-passed 2026-10-22** ([item_merges.md](../item_merges.md) 2.1 bananas, 2.2 milk bottles,
  2.3 chocolate cake, 2.6 apple ciders, 2.8 coffee; melon juice was already one item). Coffee confirmed in game
  2026-10-27. These parts need no brief; item_merges.md is the reference.
- **Banana bunch: not built.** `neapolitan:banana_bunch` (`i.banana_bunch`, item_dict.js 13298) opens on right click into
  1-3 bananas (`BananaBunchItem.handleOpening`, verified 2026-10-21; it also spawns a peel). No recipe uses it in
  `kubejs/` (grep `banana_bunch`, 2026-10-31). `i.neapolitan_banana` (12645) is the surviving banana; `i.banana` (10144)
  is still the purged Alex's Mobs id.

### Build
1. In [neapolitan/recipes.js](../../kubejs/data/creativedelight/kubejs/neapolitan/recipes.js) (Creative Delight): `R.shapeless(Item.of(i.neapolitan_banana, 2), i.banana_bunch, e, 'banana_bunch')` and
   `R.fdCutting(i.banana_bunch, i.any_knife, Item.of(i.neapolitan_banana, 3), e, 'banana_bunch')` (knife bonus).
2. item_merges.md 2.1: add the two recipes; changelog bullet under the bananas family.

### Decisions
- Counts: recommendation 2 by hand, 3 on the board (the right-click average is 2).

### Tests
- RC-41 (proposed 2026-10-21, keep): banana bunch crafts into 2 / cuts into 3 Neapolitan bananas; JEI shows both.

## drinks #7: Kettle with 6 ingredient slots; drinks move to the Kettle
Status **In Progress** · Work: patcherMod / KubeJS · id `food/drinks #7`

### State 2026-10-31 (read first; the tables below are the 2026-10-22 audit)
- The Kettle is now Creative Delight's **tank Kettle** (built 2026-10-25, [farmers_respite.md](../farmers_respite.md)
  2.1 K1-K13 and 5): milk and water are the `base` fluid, not ingredients, so most pot drinks fit in the 2 ingredient
  slots (farmers_respite.md line 136 "the Kettle has two slots"). The 6-slot patch is only needed for a drink with three
  or more solid inputs after milk / water / sugar leave the list; none in table 1 needs it. Recommendation: drop the
  6-slot part (owner to confirm; TODO wording then changes).
- **Done** (kettle.js): the 8 Rustic coffees (Coffee, Milk, Dark, Honey, Chocolate, Syrup, Pumpkin, Cherry Blossom, lines
  77-87; pot and grid recipes kept, owner 2026-10-25) and the 5 Fruits Delight teas (lines 92-96, 2x the pot per fruit,
  no sugar). Apple Cider and Hot Cocoa have Kettle recipes (lines 32-34).
- **Open:** Kettle recipes for Creamy Corn Drink (`i.creamy_corn_drink`), Ender Nectar (`i.ender_nectar`), Pickerelweed
  Juice (`i.pickerelweed_juice`; its pot recipe is the pack's, abnormals_delight/upgrade_aquatic.js:27), Cloud Latte
  (`i.cloud_latte`, bottled-cloud container), Mango Milkshake (`i.mango_milkshake`), Chorus Juice (`i.chorus_juice`) and
  the juices of table 2 (`i.hamimelon_juice`, `kiwi_juice`, `lemon_juice`, `orange_juice`, `pear_juice`,
  `prickly_pear_juice`); the pot removals for Apple Cider, Hot Cocoa and the moved drinks; a fluid + `createBottle` pair
  per new Kettle drink (K9 / K10), which waits on `food/drinks #12`.

### Build (2026-10-31)
1. Fix `food/drinks #12` first (no Kettle drink can be poured or bottled without its pair).
2. Per open drink, in kettle.js section 2: a fluid in fluids.js (`drink(...)`), a `createBottle` pair in section 3, and
   `kettle(<pot id to remove or null>, [inputs], item, id, base, opts)`: milk drinks on `f.milk` (corn for Creamy Corn
   Drink, ender eye for Ender Nectar, mango for Mango Milkshake, 2 cocoa for Cloud Latte with
   `{ container: i.bottled_cloud }`, item_dict.js 1403); water drinks on `f.water` (blue + purple pickerelweed, 1 chorus
   fruit, the juice fruits). Sugar stays pot-only.
3. Remove the pot recipe in the same call (the `removeId` argument) for every drink in "Move to the Kettle", except the
   Rustic coffees and the teas (both kept by decision).
4. Prickly Pear Juice: no live source; give it the Kettle recipe from cooked prickly pears (D below).
5. Update farmers_respite.md 5 and recipes_cooking.md; changelog bullet under a Kettle family.

### Goal
Drinks brew in the Kettle, not the Cooking Pot. Blocked by the Kettle's 2 ingredient slots (`KettleRecipe.matches`
reads slots 0-1 only, one item each, and ignores the container and water, so two recipes with the same pair clash).
Planned: 6 slots like the pot and oven (patcherMod), then the recipes with `NCH.frBrewing`. Until then every drink
below keeps its current recipes. Grid (manual) crafts stay either way.

### Decided (user 2026-10-22)
- **Move to the Kettle** (pot recipe removed when the Kettle one goes in): Coffee, Milk / Honey / Chocolate / Dark
  Coffee, Apple Cider, Hot Cocoa, Creamy Corn Drink, Ender Nectar, Pickerelweed Juice, Cloud Latte, Mango Milkshake,
  Hawberry / Lychee Cherry / Mango / Mangosteen / Peach Tea, Chorus Juice. Apple Cider and Hot Cocoa already have
  a Farmer's Respite Kettle recipe; only their pot recipe goes.
- **Stays in the pot:** Bellini Cocktail (alcoholic; not in the user's list).
- **Every drinkable juice gets a Kettle recipe more efficient than its manual one**, made in the pot or not (table 2).
- **Later (`drinks #8`):** one standard crafting rule for the juice and tea families (hand, Kettle, Create, IE).
  The Kettle recipes here can be rough until then, but must beat the grid.
- **Added 2026-10-24:** teas are made in both the Kettle and the pot. The Kettle recipe is 2x as efficient as the pot's
  (twice the output per input); the pot tea takes 2 fruit per tea (today 2 to 4 plus sugar, table 1); sugar, honey and
  honeycomb go in the pot recipe only, never the Kettle. Applies to the five Fruits Delight teas in table 1.

### 1. Pot drink audit (2026-10-22)
Live pot recipes from `kubejs/exported/recipes/all.csv` (dump of 2026-10-22 02:14), checked against the jars
(conditional wrappers unwrapped), filtered to the `drink` food type or the pot's drinks tab. Not drinks and left out:
the Fruits Delight jellies, custard and bayberry soup, Rustic Delight's cooking oil.

| Drink | Pot recipe | Ingredients | Kettle today | Plan |
|---|---|---|---|---|
| Coffee | `rusticdelight:cooking/coffee` | 4 roasted coffee beans | none | Kettle |
| Milk Coffee | `rusticdelight:cooking/milk_coffee` | milk, 3 roasted beans | none (grid: coffee + milk) | Kettle |
| Honey Coffee | `rusticdelight:cooking/honey_coffee` | milk, 3 roasted beans, honey bottle, sugar | none (grid) | Kettle |
| Chocolate Coffee | `rusticdelight:cooking/chocolate_coffee` | milk, 3 roasted beans, 2 cocoa beans | none (grid) | Kettle |
| Dark Coffee | `rusticdelight:cooking/dark_coffee` | 6 roasted beans | none | Kettle |
| Apple Cider | `farmersdelight:cooking/apple_cider` | 2 apples, sugar | yes (FR `brewing/apple_cider`) | drop pot |
| Hot Cocoa | `farmersdelight:cooking/hot_cocoa` | milk, sugar, 2 cocoa beans | yes (FR `brewing/hot_cocoa`, milk bottle container) | drop pot |
| Creamy Corn Drink | `corn_delight:cooking/creamy_corn_drink` | corn, milk, sugar | none | Kettle |
| Ender Nectar | `delightful:food/cooking/ender_nectar` | ender eye, milk, sugar | none | Kettle |
| Pickerelweed Juice | `abnormals_delight:pickerelweed_juice` | blue + purple pickerelweed, sugar | none | Kettle |
| Cloud Latte | `quarkdelight:cooking/cloud_latte` | milk, sugar, 2 cocoa beans (bottled cloud container) | none | Kettle |
| Bellini Cocktail | `farmersdelight:cooking/bellini_cocktail` | sugar, ice cubes, 2 peaches (alcoholic) | none | stays in pot |
| Mango Milkshake | `farmersdelight:cooking/mango_milkshake` | mango, milk bottle, sugar | none | Kettle |
| Hawberry Tea | `farmersdelight:cooking/hawberry_tea` | sugar, leaves, 4 hawberries | none | Kettle |
| Lychee Cherry Tea | `farmersdelight:cooking/lychee_cherry_tea` | sugar, spore blossom, 4 lychees | none | Kettle |
| Mango Tea | `farmersdelight:cooking/mango_tea` | sugar, leaves, 2 mangoes | none | Kettle |
| Mangosteen Tea | `farmersdelight:cooking/mangosteen_tea` | sugar, leaves, 2 mangosteens | none | Kettle |
| Peach Tea | `farmersdelight:cooking/peach_tea` | sugar, leaves, 2 peaches | none | Kettle |
| Chorus Juice (pack) | `creativeengineering:farmersdelightcooking/chorus_juice_manual_only` | 2 chorus fruit, sugar -> 2 | none | Kettle |

The Fruits Delight drinks use `farmersdelight:` recipe ids.

### Decisions (2026-10-31)
- 6 slots: recommendation drop (above).
- "2x as efficient" for juices: output per input (the kettle.js teas already read it that way); time no longer than
  the pot's (200 ticks today).
- Prickly pear: recommendation 2 cooked prickly pears on water -> 1 juice (Delightful's grid recipe that does not load
  took 4 + sugar).

### Tests
- RC-new1 (proposed, numbered when queued): JEI shows a Kettle recipe for each open drink and no pot recipe for the
  moved ones; brewing one with milk in the tank uses one unit.

### 2. Juices not made in the pot (2026-10-22)
Every `juice` item in `item_dict.js`, recipes read from the jars and checked live against `all.csv`.

| Juice | Made today (live) | Kettle today | Plan |
|---|---|---|---|
| Melon Juice | grid 4 slices + sugar + bottle | yes, FR 1 slice + sugar (already beats the grid) | keep |
| Strong Melon Juice | Kettle only (melon juice + honey) | yes | keep |
| Hamimelon Juice | grid 4 slices + bottle; Create mixing | none | Kettle, beat 4 slices |
| Kiwi Juice | grid 2 kiwis + bottle; Create mixing | none | Kettle, beat 2 kiwis |
| Lemon Juice | grid water bottle + 2 lemons + sugar; Create mixing with water | none | Kettle, beat 2 lemons |
| Orange Juice | grid water bottle + 2 oranges + sugar; Create mixing with water | none | Kettle, beat 2 oranges |
| Pear Juice | grid water bottle + 2 pears + sugar; Create mixing with water | none | Kettle, beat 2 pears |
| Prickly Pear Juice | none: Delightful's grid recipe (4 cooked prickly pears + sugar + bottle) does not load | none | Kettle; needs a source decision |
| Chorus Juice | grid 1 chorus + sugar + bottle (Ender's Delight); pot | none | Kettle (table 1) |

Not drinkable, left out: Vinery's apple juice and six grape juices (`GrapejuiceBottleItem`, wine ingredients that
return a wine bottle, not foods). `tmted:melon_juice` is merged into FD's at the fluid level (now in Creative Delight's farmersdelight/recipes.js).

---

## drinks #8: Standard crafting rule for juices and teas
Status **In Progress** (TODO; nothing of the rule built, 2026-10-31) / Work: KubeJS / id `food/drinks #8`

### Goal
One rule for every juice and tea (Veggies Delight juices included): inputs per bottle by hand, Kettle, Create mixing
and IE Mixer, each machine route more efficient than hand, as the can and cider families already are.

### Now
Juices are listed in `drinks #7` table 2; Create mixing for Hamimelon, Kiwi, Lemon, Orange and Pear exists; the IE Mixer
route is unverified. The Kettle half of the rule exists for the five Fruits Delight teas (kettle.js 92-96: leaves + 1 fruit
or 2 fruit, no sugar). Veggies Delight's juices have Create and Bottling Machine routes (veggiesdelight/recipes.js:24,
"the juice and tea rule may change" note). The cider family is the model
(Kettle beats the grid, `drinks #3`). Veggies' carrot, dandelion and turnip water juices are in `drinks #2`'s audit.

### Build
1. Write the rule once as a table in this section after the audit: fruit per bottle by hand, in the Kettle (2x the
   pot, 2026-10-24), in a Create mixer (heated), in an IE mixer; sugar / honey / honeycomb pot-only.
2. Apply it with `NCH.frBrewing` (Kettle), `NCH.createMixing` (line 1665), `NCH.ieBottling` (line 2258) and the IE mixer
   `NCH.ieMixer` (line 2231), and `R.shapeless` for the hand craft;
   no `e.custom`.

### Decisions
- "2x efficient" is read as twice the output per input compared with the pot recipe; the other reading is twice the
  speed. Recommend output per input, and a cooking time no longer than the pot's.
- Whether machines also beat hand for juices that have no pot recipe: recommend yes (the row says every juice).

### Tests
- RC-D1 (proposed) per family: the hand, Kettle, mixer routes of one juice and one tea give 1x / 2x / better output, JEI lists all.

---

## drinks #10: Azalea tea and tea audit
Status **Planned** (rechecked 2026-10-31: still no recipe, Ecologics not in `mods/`) / Work: KubeJS / id `food/drinks #10`

### Goal
Add an azalea tea recipe and audit every tea (Kettle and pot recipes, `drinks #7`, `drinks #8`).

### Now
- `delightful:azalea_tea` (`i.azalea_tea`) exists but has **no live recipe**: both Delightful recipes
  (`food/kettle/azalea_tea`: tea leaves + `ecologics:azalea_flower`, water, 2400 ticks; `integration/ecologics/azalea_tea`)
  are conditional on `ecologics:azalea_flower`, and Ecologics is not in `mods/` (checked).
- Candidate flowers in the pack: `i.azalea_flowers` (`twigs:azalea_flowers`, item_dict.js 10405),
  `i.immersive_weathering_azalea_flowers` (10663), `i.flowering_azalea` (2369). `i.azalea_tea` is line 11341. Which is the intended one is unverified.
- Food catalog: azalea tea 0 / 0, Nourishment 60 s and Caffeinated 10 s; no can (`can_parity.md` 607).

### Build
1. In kettle.js section 1 next to lavender tea (line 65):
   `kettle(null, [i.t_tea_leaves, i.azalea_flowers], i.azalea_tea, 'azalea_tea', f.water)` (`i.t_tea_leaves` line 1921),
   plus `drink(f.azalea_tea, 'Azalea Tea', <colour>)` in fluids.js and a `createBottle` pair (section 3), like lavender
   tea. Delightful's two conditional recipes never load; no removal needed.
2. Audit table in this section: every tea item (FR, Delightful, FD), its Kettle and pot recipe, its efficiency vs
   `drinks #8`, name, texture (`drinks #2`).

### Decisions
- **Flower.** Options: Twigs azalea flowers (craftable, renewable), Immersive Weathering's, the azalea bush block.
  Recommend `twigs:azalea_flowers`.

### Tests
- RC-D3 (proposed) JEI: azalea tea has a Kettle recipe; brewing gives the tea with its bottle plated (RB-03).

---

## drinks #11: Re-add Cherry Cream Soda and a fruit soda line
Status **Planned** · Work: KubeJS / data · id `food/drinks #11`

### Goal
Cherry Cream Soda comes back with a pack recipe; maybe a cheap fruit soda line (mostly bottles, some cans, dyed), worse
than the proper drinks.

### Now
- `abnormals_delight:cherry_cream_soda` (`i.cherry_cream_soda`, item_dict.js 13850) is registered but purged:
  [removed_items.js:338](../../kubejs/startup_scripts/_lib/removed_items.js#L338) ("compat items whose mod isn't
  installed", `ui/effects #6`, 2026-10-23). The item is FD's `DrinkableItem`, always edible, 15 s Slow Falling
  ([abnormals_delight.md](../abnormals_delight.md) 2.2; owner idea at line 180).
- **The removed recipe** (verified in `mods/abnormals_delight-1.19.2-4.1.2.jar`,
  `data/abnormals_delight/recipes/cherry_cream_soda.json`): `farmersdelight:cooking`, drinks tab, `#forge:milk`, sugar,
  2 `#forge:fruits/cherry`, `neapolitan:dried_vanilla_pods`; 200 ticks, 1.0 xp; conditions `environmental` and
  `neapolitan` loaded. Environmental is not installed, so it never loaded.
- `#forge:fruits/cherry` is **empty** in the pack (kubejs/exported/tags/item_tags.json, 2026-10-30). The pack's cherry
  is Vinery's `vinery:cherry` (`i.cherry`, 11209), tagged `forge:berries` / `c:berries` and the food-family tags only.
- Soda-like cans already exist: Canned Goods' Soda, Reef-Cola, Oculemonade, Mini Fizz (`i.soda`, `i.reef_cola`; kept as
  "drinks made in a can", canning.js:117).

### Build
1. Take `i.cherry_cream_soda` out of removed_items.js (and its comment line).
2. Tag: add `i.cherry` to `forge:fruits/cherry` through `global.tags` (Vinery is a Creative Brewing member; the tag call
   goes in the CB or CD tags file that holds Vinery fruit tags).
3. Recipe in [abnormals_delight/recipes.js](../../kubejs/data/creativedelight/kubejs/abnormals_delight/recipes.js) (Creative Delight):
   `R.fdCooking([i.milk, i.any_sweetener_drink, i.t_fruits_cherry, i.t_fruits_cherry, i.dried_vanilla_pods], i.cherry_cream_soda, e, 'cherry_cream_soda', 1.0, 200, i.glass_bottle)`
   (`fdCooking(input, output, e, idOverride, exp, time, container)` line 1074; `i.milk` = `#forge:milk` line 1825; add
   `i.t_fruits_cherry = '#forge:fruits/cherry'` next to the other `t_fruits_*` keys, ~line 1879).
   Or a Kettle tier on milk, per Decisions.
4. Fruit soda line (if wanted): one item per fruit is a lot of ids; recommendation a small set first (see Decisions),
   items in the CD Abnormals tree or a new CD `creativeengineering/sodas.js` startup file, recipe a cheap grid craft
   (water bottle + fruit + sugar + dye).
5. food_types.json / food catalog (`drink`), changelog bullet ("Cherry Cream Soda is back: ...").

### Ids and data
Verified: item id, removed recipe JSON, empty cherry tag, `i.dried_vanilla_pods` (12656), `i.cherry` (11209). New:
`i.t_fruits_cherry`, recipe id `creativedelight:farmersdelightcooking/cherry_cream_soda` (or the CD namespace NCH writes).

### Decisions
- Pot (the original) or Kettle: recommendation pot, as upstream, so the soda stays a cheap side drink.
- Fruit sodas: recommendation 3 to 4 bottled sodas (cherry, orange, lemon-lime, grape) and no new cans (the can family
  already has Soda and Reef-Cola); effects none or a 5 s Speed, saturation below the juices.
- Dyes: recommendation only as colour hints in the recipe (one dye each), not a dye-to-variant system.

### Gotchas
- `removedItems` strips tags and hides JEI entries; after un-removing, `/reload` is not enough (startup list): restart.
- Without the tag step the recipe loads but matches nothing (NCH warns "matches no items").

### Tests
- RC-new2 (proposed, numbered when queued): JEI shows Cherry Cream Soda with the pot recipe; cooking it with Vinery
  cherries works; drinking gives Slow Falling and the bottle back.

---

## drinks #12: Spout filling for Kettle drinks and the water cup fails to load
Status **Bug** · Work: patcherMod (NCH) · id `food/drinks #12` · blocks the Kettle tank's Spout half (farmers_respite.md K10)

### Goal
Every `NCH.createBottle` pair loads, so the Spout bottles and the Item Drain empties each Kettle drink and the water cup.

### Now
- Still failing in the user's `logs/latest.log` of 2026-10-31 17:17: 12 `Error parsing recipe
  creativedelight:createfilling/<id>` lines (`berry_matcha_latte`, the 7 Rustic coffees `cherry_blossom_coffee`,
  `chocolate_coffee`, `coffee`, `dark_coffee`, `honey_coffee`, `milk_coffee`, `pumpkin_coffee`, `syrup_coffee`,
  `lavender_tea`, `matcha_latte`, `water_cup`), each with the fluid ingredient written as `{"fluid":"minecraft:empty",
  "nbt":{},"amount":0}` and `Can't modify the empty stack`. `createemptying/*` raise nothing (whether they give fluid is
  unchecked). `logs/kubejs/server.log` of 17:20 has no such line (the errors are in latest.log only).
- Callers: kettle.js:107 (`BOTTLES.forEach(b => R.createBottle(...))`) and miners_delight/recipes.js:45 (water cup).
- [createBottle](../../development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js#L1883)
  builds `Fluid.of(fluid, mb)` inside NCH and passes `[container, fluidStack]` to `createFilling` (line 1828), which runs
  `toId`, `_validateAny` (442; calls `_ingredientLike` 189, `_isFluidLike` 436) on it and then
  `e.recipes.create.filling(output, input)`.
- Same symptom elsewhere: `creativedelight:createsequencedassembly/zucchini_quiche_sequenced_assembly`
  (veggiesdelight/recipes.js:65 passes a script-side `Fluid.of(f.milk, 250)` as a `fill` step through NCH's sequenced
  builder) also comes out `minecraft:empty`.
- Loads fine: NCH's own `e.recipes.create.filling(inc, [inc, Fluid.of(global.fluid.lava, 250)])` (lines 1511-1538,
  built and used without NCH validation) and the FD stuffed potato (farmersdelight/recipes.js:345,
  `e.recipes.create.filling(...)` called directly in the script). So the common factor of the failures is a
  FluidStackJS that passes through NCH's validation / id code before reaching the builder, not where `Fluid.of` is
  created. Not verified.

### Build
1. Confirm the cause in a scratch boot or the headless Rhino harness (memory `kubejs-headless-rhino-harness`): log
   `String(fluidStack)` and `fluidStack.amount` before and after `toId`, `_validateAny`, `_ingredientLike` in
   createFilling. Suspect `_ingredientLike`: a FluidStackJS may pass the `asIngredient` duck test (line 203) and go
   through `Ingredient.of(v)`, or a getter in `_resolveFluidAmount` may mutate it.
2. Fix in NCH, either way robust: write the filling as JSON like createEmptying's NBT branch does
   (`e.custom({ type: 'create:filling', ingredients: [_itemJson(container), _fluidJson(fluid with mb)], results: [...] })`),
   which needs no FluidStackJS at all; and make `_validateAny` / `_ingredientLike` return early for fluid-likes
   (`_isFluidLike` first) so other helpers are not hit. Apply the same to the sequenced-assembly `fill` step.
3. patcherMod rebuild and install (the helper is in its jar), restart; grep latest.log for `Error parsing recipe
   creativedelight:create` -> none.
4. Then run SQ-RSP-09; close the Spout half of K10 in farmers_respite.md.

### Ids and data
Recipe ids `creativedelight:createfilling/<drink id path>` and `createemptying/...`; fluids from fluid_dict.js
(`f.coffee` = `create_central_kitchen:coffee`, the pack drink fluids `creativeengineering:*`, `f.water`).

### Decisions
None (row says none). Recommendation for the fix shape: JSON via `_fluidJson` (step 2).

### Gotchas
- The error is a KubeJS recipe-parse warning, not an `Error occurred while handling event`; grep `Error parsing recipe`.
- Fixing NCH changes every helper that validates fluids; rerun the NCH harness tests (memory
  `patchermod-headless-smoke-test`) and check the potion sequenced recipes still load.

### Tests
- SQ-RSP-09 (in the scratch queue): Spout and Item Drain for Milk Coffee, Lavender Tea, Matcha Latte, Coffee; cup <-> water;
  no `Error parsing recipe creativedelight:createfilling/` line.
- RC-new3 (proposed): JEI shows a Spout recipe for each Kettle drink and the water cup; the zucchini quiche assembly
  shows its milk step.

---

## Built, needs play

- `food/drinks #4` Chorus juice: built 2026-10-22 ([recipes_cooking.md](../recipes_cooking.md) 2.2); scratch PASSED
  (SQ-DR-00, SQ-DR-01); open: RC-71, RC-72 in game.
- `food/drinks #5` Berry matcha latte: built 2026-10-22 (recipes_cooking.md 2.2; now a Kettle tier on the Matcha
  Latte in the tank, kettle.js:68); scratch PASSED (SQ-DR-02); open: RC-73 in game; its Spout pair is `food/drinks #12`.
- `food/drinks #6` Milk from a cow in a jar: built 2026-10-22 (CD `cookingforblockheads/milk_jar.js`, recipes_cooking.md
  2.2); scratch PASSED (SQ-DR-03); open: RC-74 in game.
- `food/drinks #9` Bayberry soup -> Bayberry Juice: built 2026-10-25, shipped 1.1.03 ([fruits_delight.md](../fruits_delight.md)
  "Bayberry Juice"); open: RC-D2, MJ-14 in game; its Kettle recipe waits for `drinks #7` / `#8`.

## Writeup corrections made

None.
