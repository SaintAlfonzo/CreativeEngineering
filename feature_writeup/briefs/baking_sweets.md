# Brief: baking / sweets

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# baking`, Sub `sweets` (rows 1 to 6, all Planned). Writeups:
[food.md](../food.md) (types, 4.5 plans), [food_catalog.md](../food_catalog.md) (current values, `sweet` table),
[recipes_cooking.md](../recipes_cooking.md) (test prefix `RC-`, last used RC-18), [blockrunner.md](../blockrunner.md).
The hand-written cooking plan these rows came from is in git (`git show c82dd31^:feature_writeup/cooking.md`, tree
"sweet > candy / cold").

**Verified against jars** (scratchpad cache, decompiled with Vineflower): Delightful 3.4.1, Neapolitan 4.1.0,
Fruits Delight 0.5.11, Farmer's Delight 1.2.4, Abnormals Delight 4.1.2, More Delight 25.07.28, Create Central Kitchen
(CCK) 1.3.11.c, Quark 3.4-418, KubeJS 1902.6.2-build.73. Everything else is marked unverified or "in game".

## Scope and order

| Row | Size | Build with |
|---|---|---|
| `baking/sweets #6` Orange jam from 4 slices | tiny, 2 recipes | alone, first |
| `baking/sweets #5` Smoothie | tiny (if removed) | alone |
| `baking/sweets #4` Ice cream rework | large, in steps a-g | a, b, e first (no new items); c, d, f need the shared fluid file of `food/drinks #1/#4` |
| `baking/sweets #2` Rock candy variants | small-medium (new items) | builds the shared "pack sweets" table (below) |
| `baking/sweets #1` Gummies | medium (12-ish new items) | same table as #2 |
| `baking/sweets #3` Cold sweets variants | medium (new items) | same table; after `dishes/ingredients #5` (ice cubes) |

Order: #6 → #5 → #4 a/b/e → #2 → #1 → #3 → #4 c/d/f/g.

**Shared infrastructure for #1, #2, #3, #4f (build once, with whichever comes first):**
- Item ids in [item_dict.js](../../kubejs/startup_scripts/_lib/item_dict.js): a new hand-written section
  `// ########## Pack sweets and drinks ##########` in the Food block (after line 1878 "Tags"), keys
  `i.<fruit>_gummy`, `i.<colour>_rock_candy`, `i.<fruit>_popsicle` ... = `'creativeengineering:<same>'`. Missing fruit
  tag keys go in the Food tags block next to the existing nine (`i.t_fruits_blueberry` ... line ~1904):
  `i.t_fruits_<name> = '#forge:fruits/<name>'` (orange, peach, mango, kiwi, pear, lychee, pineapple, apple? see each row).
- Modded effect ids in [effect_dict.js](../../kubejs/startup_scripts/_lib/effect_dict.js) (vanilla only today), e.g.
  `ef.refreshing = 'fruitsdelight:refreshing'`, `ef.brightening = 'fruitsdelight:brightening'`,
  `ef.sweetening`, `ef.recovering`, `ef.heal_aura`, `ef.lozenge`, `ef.appetizing` (all `fruitsdelight:`), ids listed in
  [effects.md](../effects.md).
- One table + one registration loop: `kubejs/startup_scripts/cooking/sweets.js` holds
  `global.sweets = { gummy: [...], rock_candy: [...], ... }`, each entry `{ key, name, flavour (dict key), effects:
  [[effKey, ticks, amp]], texture }`, and registers the items in `StartupEvents.registry('item')`, the same shape as the
  generated-can loop in [cans.js:83-102](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js#L83-L102). A new
  `kubejs/server_scripts/cooking/sweets.js` reads the same table and writes the recipes through NCH. Per-fruit effects:
  take each fruit's Fruits Delight jello effect (the `sweet` table in food_catalog.md: lemon = haste + refreshing,
  orange = regeneration + recovering, mango = strength III + rage_aura, peach = heal_aura, pear = lozenge, ...).
- Container return on eating: KubeJS items don't hand back a stick or bottle when eaten (vanilla `finishUsingItem`).
  Use the food builder's `eaten` callback, already used in [cans.js:80](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js#L80):
  `f.eaten(ev => ev.player.give('minecraft:stick'))` (verify `ev.player` vs `ev.entity` in the Rhino harness; cans.js
  uses `ev.entity`). Also set `.containerItem(...)` so the item leaves its container when used as a crafting
  ingredient.
- After any new food: `/dumpfood` → `python3 tools/food_catalog.py` (types them into `food_types.json`; new ids go
  under `sweet`), `tools/gen_stack_sizes.py`, compost values
  ([compost_values.js](../../kubejs/startup_scripts/compost/compost_values.js), Delightful gives gummies 0.65), and a `/`
  row per item in [can_parity.md](../can_parity.md) (`cans/canning #2`). Sprites go in
  `kubejs/assets/creativeengineering/textures/item/<id>.png` (recolours of the mod sprite named in each row).

Built, needs play: none (no sweets row is at Testing or Complete).

---

## sweets #1: Gummies
Status **Planned** · Work: KubeJS (+ textures) · id `baking/sweets #1`

### Goal
A gummy for most fruits. Gelling agent: kelp or slime ball. Three routes: a cheap manual craft, a more efficient
Cooking Pot recipe, and a bulk Create recipe.

### Now
- One gummy: `delightful:salmonberry_gummy` (`i.salmonberry_gummy`, item_dict.js:11346). `GummyItem`: eats in 14 ticks;
  `Nutrition.SALMONBERRY_GUMMY` = 2 hunger, 0.0 modifier, always edible, Water Breathing 400 ticks (verified).
- Recipes (verified): `delightful:gummy/salmonberries` FD pot `[#forge:fruits/salmonberries, minecraft:sugar,
  minecraft:honey_bottle, minecraft:kelp]` → 1, 200 ticks, 1.0 xp, no container; `delightful:mixing/salmonberry_gummy`
  Create heated mixing `[salmonberries, sugar, honey_block, kelp]` → 9. The pack's sugar swap
  ([cooking.js:58](../../kubejs/server_scripts/cooking/cooking.js#L58)) already turns `minecraft:sugar` into `#forge:sugar`.
- Slice & Dice copies every pot recipe to heated mixing after KubeJS runs, unless the id ends in `_manual_only`
  ([food.md](../food.md) 3.7).

### Build
1. Pick the fruits (D1) and add `i.<fruit>_gummy` keys plus any missing `i.t_fruits_*` keys (see Shared).
2. Tag `#creativeengineering:gelling_agents` = kelp, slime ball (D3 for dried kelp) in
   [cookingtags.js](../../kubejs/server_scripts/tags/cookingtags.js) via `t.add(e, i.t_gelling_agents, [i.kelp, i.slime_ball])`,
   dict key `i.t_gelling_agents = '#creativeengineering:gelling_agents'`.
3. Register the items from the sweets table:
   ```js
   e.create(i[g.key]).displayName(g.name).texture('creativeengineering:item/' + g.key)
     .useDuration(() => 14)                          // GummyItem speed
     .food(f => { f.hunger(2).saturation(0.1).alwaysEdible()
       g.effects.forEach(x => f.effect(global.effect[x[0]], x[1], x[2], 1)) })
   ```
4. Recipes per gummy in `server_scripts/cooking/sweets.js` (fruit = dict key of the fruit or its tag):
   - manual: `R.shapeless(Item.of(gummy, 2), [fruit, i.any_sugar, i.t_gelling_agents], e, key)`
   - pot: `R.fdCooking([fruit, fruit, i.any_sugar, i.honey_bottle, i.t_gelling_agents], Item.of(gummy, 6), e, key + '_manual_only', 0.35, 200)`
   - Create: `R.createMixing(Item.of(gummy, 12), [fruit, fruit, i.any_sugar, i.honey_block, i.t_gelling_agents], e, key, 'heated')`
   (the `_manual_only` id stops a weaker Slice & Dice copy next to the hand-written one).
5. Bring the salmonberry gummy onto the same scheme: `R.remove({ id: 'delightful:gummy/salmonberries' })` and
   `'delightful:mixing/salmonberry_gummy'`, then a table entry with `existing: true` (recipes only, no registration).
6. Sprites: recolour `assets/delightful/textures/item/salmonberry_gummy.png` per fruit.
7. Follow-ups from Shared (food types, stack sizes, compost, can parity).

### Ids and data
- Verified: the ids and recipes above; Delightful's `forge:fruits/salmonberries` = `delightful:salmonberries`.
- Fruit tags present (Fruits Delight jar, verified): persimmon, fig, mango, cranberry, hawberry, mangosteen, pear,
  orange, peach, lychee, kiwi, lemon, pineapple, bayberry, blueberry. Neapolitan: `forge:fruits/strawberry`, `banana`.
  Vanilla fruits (apple, sweet berries, glow berries, melon slice) use their dict item keys.
- New: `creativeengineering:<fruit>_gummy`.

### Decisions
- **D1 Fruits.** Recommend 12: apple, sweet berry, glow berry, melon, strawberry, blueberry, lemon, orange, mango,
  peach, pineapple, kiwi. Skip grapes (Vinery wine chain), coffee berries, durian, acorn.
- **D2 Values.** Delightful's is 2 / 0.0. Recommend 2 / 0.1 and always-edible (a snack); effect = the fruit's jello
  effect at half the jello duration. Revisit with the tuning system (`food/types-and-tuning #1`).
- **D3 Gelling agents.** Kelp and slime ball as asked; recommend dried kelp too. Quark Delight's gelatine bucket is
  another option, not recommended (too dear for a gummy).
- **D4 Yields.** Proposed 2 / 6 / 12 above. Check them against the Create heated recipe of Delightful (9 per batch).

### Gotchas
- The pot has 6 slots: the pot recipe above uses 5.
- `forge:fruits/strawberry` (Neapolitan) vs `forge:fruits/strawberries` (Delightful's berry tag lists the plural, which
  nothing fills here). Use the singular.
- Build the recipe loop with `forEach`. A `for (var ...)` loop with closures in Rhino captures the last value.

### Tests
- RC-19 each gummy: the shapeless, pot and heated mixing recipes show in JEI and give 2 / 6 / 12.
- RC-20 eating: a gummy eats fast, applies its effect, and AppleSkin shows 2 / 0.1.
- RC-21 the salmonberry gummy takes slime balls (the new recipe) and Delightful's own recipes are gone.

---

## sweets #2: Rock candy variants
Status **Planned** · Work: KubeJS (+ textures) · id `baking/sweets #2`

### Goal
4 or 5 more rock candies made from Quark corundum. The rose quartz one stays.

### Now
- `delightful:rock_candy` (`i.rock_candy`, item_dict.js:11343). `ConsumableItem`: hands back a stick; 4 / 0.2, fast,
  Speed 120 ticks (verified `Nutrition.ROCK_CANDY`).
- Recipe `delightful:cooking/rock_candy` (verified): FD pot `[#forge:gems/rose_quartz x2, #forge:sugar]`, container
  `minecraft:stick`, 200 ticks, 0.35 xp, loads only while that tag isn't empty. Delightful fills
  `forge:gems/rose_quartz` with `create:rose_quartz` (+ optional BOP shard).
- Corundum (Quark, module on in `quark-common.toml`): 9 colours (red, orange, yellow, green, blue, indigo, violet,
  white, black), each with a block `quark:<c>_corundum`, a `_cluster` and a `_pane`. Dict keys exist
  (`i.<c>_corundum_cluster`, item_dict.js ~2880-3400; `i.any_corundum = '#quark:corundum'`, line 108). Corundum is
  also used by [dragon_breathing.js:21](../../kubejs/server_scripts/magic/dragon_breathing.js#L21) (myalite).

### Build
1. Keys `i.<c>_rock_candy = 'creativeengineering:<c>_rock_candy'` for the chosen colours (D1).
2. Table entries; register as food like Delightful's: `f.hunger(4).saturation(0.2).fastToEat()`, the effect (D2),
   `f.eaten(ev => ev.player.give(i.stick))`, `.containerItem(i.stick)`.
3. Recipes: `R.fdCooking([cluster, cluster, i.any_sugar], Item.of(candy, 1), e, '<c>_rock_candy', 0.35, 200, i.stick)`.
   Slice & Dice adds the heated mixing copy by itself (fine here: same yield).
4. Sprites: recolour `assets/delightful/textures/item/rock_candy.png` to each corundum colour.

### Ids and data
Verified: everything under Now. New: `creativeengineering:<colour>_rock_candy`.

### Decisions
- **D1 Colours.** Recommend 5 that look different from the pink rose-quartz candy: yellow, green, blue, violet, white.
- **D2 Effects.** Recommend Speed 6 s on all of them, like the original (variants for looks). The alternative is one
  6 s vanilla effect per colour (yellow haste, green jump boost, blue water breathing, violet night vision, white slow
  falling).
- **D3 Input.** Clusters (they read as "crystals"), not the blocks.

### Gotchas
- Clusters only come from corundum crystal growth in caves. Check that the source is steady enough for a snack, or
  allow the corundum block as a second input (cut on the stonecutter into clusters?).

### Tests
- RC-22 every colour: the pot recipe (2 clusters + sugar + stick) makes the candy; eating hands back a stick.
- RC-23 the rose quartz candy still has its recipe.

---

## sweets #3: Cold sweets variants
Status **Planned** · Work: KubeJS (+ textures) · id `baking/sweets #3` · after `dishes/ingredients #5` (ice cubes)

### Goal
More custard, shaved ice and popsicle flavours.

### Now (recipes verified)
| Kind | Existing | Recipe | Leaves |
|---|---|---|---|
| Custard | `farmersdelight:glow_berry_custard` | FD pot `[glow_berries, #forge:milk, #forge:eggs, sugar]`, no container field | glass bottle |
| Custard | `fruitsdelight:blueberry_custard` | FD pot (`farmersdelight:cooking/blueberry_custard` in the Fruits Delight jar) `[blueberry x2, #forge:milk/milk_bottle, #forge:eggs, sugar]`, container bottle | glass bottle |
| Shaved ice | `fruitsdelight:hamimelon_shaved_ice` | shapeless `[glass_bottle, hamimelon_slice x2, #forge:milk/milk_bottle, sugar, minecraft:ice]` | glass bottle |
| Popsicle | `farmersdelight:melon_popsicle` | shaped `" mm","imm","-i "` m melon slice, i ice, - stick. Abnormals Delight replaces FD's with a Neapolitan-ice-cube version when Neapolitan is loaded (`abnormals_delight:neapolitan/melon_popsicle`); FD's own is off | nothing |
| Popsicle | `fruitsdelight:hamimelon_popsicle`, `kiwi_popsicle` | same shape with `minecraft:ice` | stick |
| Popsicle | `moredelight:chocolate_popsicle` | same shape, cocoa beans | nothing |

Values: food_catalog.md `sweet` table. The pack already puts `minecraft:ice` into `#forge:ice_cubes`
([cookingtags.js:26](../../kubejs/server_scripts/tags/cookingtags.js#L26)); no dict key for that tag yet.

### Build
1. Add `i.t_ice_cubes = '#forge:ice_cubes'` (or take it from `dishes/ingredients #5`, which owns ice).
2. Table entries per kind (D1). Item shape per kind:
   - custard / shaved ice: drink-like (`.useAnimation('drink')`, `f.eaten(ev => ev.player.give(i.glass_bottle))`,
     `.containerItem(i.glass_bottle)`), max stack per `food/stack-sizes #1` (bottled drinks 32? see that row).
   - popsicle: fast food, gives back a stick like the Fruits Delight ones.
3. Recipes, mirroring the originals:
   - custard: `R.fdCooking([fruit, fruit, i.t_milk_bottle, i.eggs, i.any_sugar], custard, e, key, 0.1, 200, i.glass_bottle)`
   - shaved ice: `R.shapeless(ice, [i.glass_bottle, fruit, fruit, i.t_milk_bottle, i.any_sugar, i.t_ice_cubes], e, key)`
   - popsicle: `R.shaped(pop, [' FF', 'IFF', 'SI '], { F: fruit, I: i.t_ice_cubes, S: i.stick }, e, key)`
4. Optionally swap `minecraft:ice` for `#forge:ice_cubes` in the 4 existing ice recipes
   (`R.replaceInput({ id: ... }, i.ice, i.t_ice_cubes, e)`); `dishes/ingredients #5` decides the ice rule pack-wide.
5. Sprites: recolour the custard, shaved ice and popsicle sprites of FD / Fruits Delight.

### Ids and data
Verified: the table above (`i.glow_berry_custard` 11526, `i.blueberry_custard` 12234, `i.hamimelon_popsicle` 12259,
`i.hamimelon_shaved_ice` 12260). New: `creativeengineering:<fruit>_custard`, `<fruit>_shaved_ice`, `<fruit>_popsicle`.

### Decisions
- **D1 Which.** Recommend custard + sweet berry, strawberry, mango; shaved ice + melon, lemon, peach; popsicle + sweet
  berry, strawberry, orange, lemon, pineapple. Each takes its fruit's jello effect at the duration of the kind it copies.
- **D2 Ice input.** `#forge:ice_cubes` (contains ice blocks today) or the ice block. Follow `dishes/ingredients #5`.

### Gotchas
- The melon popsicle's live recipe is Abnormals Delight's ice-cube copy, not FD's. A `R.replaceInput` keyed on FD's id
  finds nothing.

### Tests
- RC-24 each new cold sweet: recipe in JEI, eaten hands back the bottle / stick, effect applies.

---

## sweets #4: Ice cream rework
Status **Planned** · Work: KubeJS / data (+ textures) · id `baking/sweets #4`

### Goal
One recipe shape for every ice cream, a fluid per flavour with mixing recipes, bowl spouting, an ice cream block per
flavour that is sticky and sinks you in (BlockRunner), more flavours. The matcha and salmonberry ice creams stop
being gross. All ice creams match each other (scratchpad 2026-10-21: "ice cream parity").

### Now
- Ice creams (all bowl foods, stack 1): Neapolitan vanilla, chocolate, strawberry, banana, mint, adzuki (6 / 0.4 each,
  one flavour effect each) and the mixed `neapolitan:neapolitan_ice_cream` (12 / 0.4); Delightful
  `matcha_ice_cream` (6 / 0.5: Slowness III 5 s, Vitality II 60 s, Caffeinated 60 s) and `salmonberry_ice_cream` (6 / 0.4:
  Slowness III 5 s, Water Breathing 60 s). Verified `Nutrition.MATCHA_ICE_CREAM` / `SALMONBERRY_ICE_CREAM`. The
  **Slowness III** is the "gross" part; Neapolitan's `IceCreamItem` adds 200 freeze ticks instead (verified).
- Shapeless recipes (verified): Neapolitan `[bowl, flavour, #forge:milk, neapolitan:ice_cubes (item), minecraft:sugar]`,
  flavour = vanilla pods, chocolate bar, `#forge:fruits/strawberry`, `#forge:fruits/banana`, mint leaves, roasted adzuki
  beans; Delightful `[bowl, matcha | #forge:fruits/salmonberries, #forge:milk, #forge:ice_cubes (tag), #forge:sugar]`
  (its `_no_neapolitan` copies with `minecraft:ice` load only while `#forge:ice_cubes` is empty, so they're off).
- **Fluids already exist for the 6 Neapolitan flavours**, from CCK (verified: `NeapolitanFluidEntries`, registered
  because Neapolitan is loaded; recipes in CCK's built-in `datapacks/neapolitan`):
  `create_central_kitchen:<flavour>_ice_cream` (500 mB per bowl) and `<flavour>_milkshake` (250 mB per bottle).
  Recipes: `create_central_kitchen:mixing/<f>_ice_cream` (unheated: ice cubes item, sugar, flavour, 250 mB
  `#forge:milk` → 500 mB), `mixing/chocolate_ice_cream_from_fluid_chocolate`, `filling/<f>_ice_cream` (bowl + 500 mB →
  ice cream = bowl spouting), `emptying/<f>_ice_cream`, `mixing/<f>_milkshake` and `_from_ice_cream`, filling /
  emptying of milkshakes, two sequenced assemblies for the Neapolitan ice cream. None of these is in the repo, so none
  is listed in a writeup; the live fluid list needs `/dumpfluids`.
- No fluid, no spouting and no milkshake for matcha or salmonberry.
- Blocks: `neapolitan:<f>_ice_cream_block` (6) and `delightful:matcha_ice_cream_block`, `salmonberry_ice_cream_block`;
  plain `Block`, snow material, hardness 0.2, snow sound; recipe 8 snow blocks around 1 ice cream → 8 (verified).
  No mixed-Neapolitan block.
- BlockRunner: `delightful:*_ice_cream_block` is in the `confectionery` tier (0.85,
  [blocktags.js:367](../../kubejs/server_scripts/tags/blocktags.js#L367)). The Neapolitan blocks are not named in any
  tier (they have tags, so `tools/blockrunner_untagged_blocks.json` doesn't list them either): check their speed with
  the WTHIT Run Speed line. `very_sticky` (0.45: honey, slime, jelly blocks, blocktags.js:399-402) is part of
  `#creativeengineering:sinking` (blocktags.js:430), which gives the visual sink-in.

### Build
a. **Unify the recipes.** Add `i.t_ice_cubes` (see #3). `R.replaceInput({ input: i.ice_cubes }, i.ice_cubes,
   i.t_ice_cubes, e)` pack-wide (`i.ice_cubes` = `neapolitan:ice_cubes`, item_dict.js:12414) so the Neapolitan
   shapeless and the CCK mixing recipes take any ice cube or ice block. Sugar is already `#forge:sugar` (cooking.js:58).
   Coordinate with `dishes/ingredients #5`, which owns the ice rule.
b. **Not gross, parity values.** Startup, `ItemEvents.modification`:
   ```js
   var $Effects = Java.loadClass('net.minecraft.world.effect.MobEffects')
   ;[i.matcha_ice_cream, i.salmonberry_ice_cream].forEach(id =>
     e.modify(id, item => item.setFoodProperties(f => { f.removeEffect($Effects.MOVEMENT_SLOWDOWN); f.saturation(0.4) })))
   ```
   (`FoodBuilder.removeEffect(MobEffect)` and the consumer form of `setFoodProperties` verified in the KubeJS jar; the
   consumer form already runs in cans.js:111.) Server: `ItemEvents.foodEaten(i.matcha_ice_cream, e => e.entity.setTicksFrozen(e.entity.getTicksFrozen() + 200))`
   (and salmonberry) for Neapolitan's brain freeze. Where this lives: as `items` entries of
   `food_tuning.json` if `food/types-and-tuning #1` is built first; otherwise a `global.foodEdits` map next to
   `global.foodValueCopies` in [food_values.js](../../kubejs/startup_scripts/cooking/food_values.js).
c. **Fluids for matcha and salmonberry.** In the shared startup fluid file `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js`
   (also used by `food/drinks #1` and `#4`; no KubeJS fluid exists in the pack yet):
   ```js
   StartupEvents.registry('fluid', e => {
     e.create(global.fluid.matcha_ice_cream).displayName('Matcha Ice Cream')
       .stillTexture('delightful:block/matcha_ice_cream_block').flowingTexture('delightful:block/matcha_ice_cream_block')
       .noBlock().noBucket()        // like CCK's virtual fluids (FluidBuilder methods verified)
   })
   ```
   fluid_dict keys `f.matcha_ice_cream = 'creativeengineering:matcha_ice_cream'` etc. Recipes copying CCK's:
   `R.createMixing(Fluid.of(f.matcha_ice_cream, 500), [i.t_ice_cubes, i.any_sugar, i.matcha, Fluid.of(f.milk, 250)], e, 'matcha_ice_cream')`,
   `R.createFilling(i.matcha_ice_cream, [i.bowl, Fluid.of(f.matcha_ice_cream, 500)], e, 'matcha_ice_cream')`,
   `R.createEmptying([i.bowl, Fluid.of(f.matcha_ice_cream, 500)], i.matcha_ice_cream, e, 'matcha_ice_cream')`.
d. **Milkshakes for them** (D3): new drink items `creativeengineering:matcha_milkshake`, `salmonberry_milkshake`, fluids,
   and the CCK-style mixing (500 ice cream + 250 milk → 750) / filling (bottle + 250). Shapeless like Neapolitan's
   `[3 bottles, ice cream, #forge:milk]` → 3.
e. **Blocks sticky and sinking.** blocktags.js: drop `'delightful:*_ice_cream_block'` from `confectionery` (line 367)
   and add `'neapolitan:*_ice_cream_block', 'delightful:*_ice_cream_block'` to the `very_sticky` list (line 399). That
   puts them in `#creativeengineering:sinking` too. Optional honey-like jump: `block.setJumpFactor(0.5)` in
   [block_properties.js](../../kubejs/startup_scripts/changes/block_properties.js) (jump is not a BlockRunner speed, so it
   doesn't stack). Optional block → items back: `R.shapeless(Item.of(iceCream, 8), [block, Item.of(i.bowl, 8)])`,
   as Fruits Delight does for jelly blocks.
f. **More flavours** (D4). Per flavour: bowl item (KubeJS, `.maxStackSize(1)`, `f.eaten` gives the bowl back,
   `.containerItem(i.bowl)`), block (`StartupEvents.registry('block')`, `.material('snow').hardness(0.2).soundType('snow')`,
   block tags as in step e), fluid (step c, using the block texture), the unified shapeless recipe, mixing, filling,
   emptying, 8-snow block recipe, sprites (item and block).
g. **Bowl spouting:** done by CCK for the Neapolitan six; steps c and f add it for the rest.

### Ids and data
- Verified: everything under Now. Dict keys exist for all 8 ice creams and the 2 Delightful blocks
  (`i.matcha_ice_cream` 11320, `i.salmonberry_ice_cream` 11347, `i.matcha_ice_cream_block` 11321,
  `i.salmonberry_ice_cream_block` 11348, `i.ice_cubes` 12414). Neapolitan blocks and the CCK fluids have no dict keys:
  add them (`i.vanilla_ice_cream_block`, `f.vanilla_ice_cream = 'create_central_kitchen:vanilla_ice_cream'` ...).
- New: `creativeengineering:matcha_ice_cream`, `salmonberry_ice_cream` (fluids), plus D3 / D4 items, blocks, fluids.

### Decisions
- **D1 Parity values.** Recommend every single-flavour ice cream at 6 / 0.4 + 200 freeze ticks + its own effect; the
  Neapolitan mixed one stays 12 / 0.4. Matcha keeps Vitality and Caffeinated and loses the Slowness.
- **D2 Saturation instead?** The scratchpad asks "saturation?". The values above already match Neapolitan's; with the
  Slowness gone, no saturation bump is needed.
- **D3 Milkshakes for the Delightful flavours (and new ones).** Recommend yes: parity means every ice cream has a
  milkshake, fluid, spout recipe and block.
- **D4 New flavours.** Recommend 3 to start: sweet berry, lemon, mango (distinct colours, existing fruit effects).
- **D5 BlockRunner tier.** `very_sticky` (recommended) or a new "ice cream" tier, which would mean a new `[surface.speed]`
  entry and patcherMod `SurfaceTier` change.

### Gotchas
- `setFoodProperties` has two one-argument overloads. cans.js shows the consumer form working; still check the log for
  `[food values]`-style errors after the restart.
- CCK's ice cream fluids are 500 mB per bowl and milkshakes 250 mB per bottle. Keep the new ones the same, or spouts and
  drains won't round-trip.
- Neapolitan ice creams stack to 1. New ones should match (`food/stack-sizes #1`).

### Tests
- RC-25 every ice cream takes an ice block or ice cubes in the crafting grid and in the mixer.
- RC-26 matcha / salmonberry ice cream: no Slowness, freeze overlay as with Neapolitan's, AppleSkin 6 / 0.4.
- RC-27 matcha and salmonberry: mixer makes the fluid, spout fills a bowl, drain empties it.
- RC-28 walking on any ice cream block: slow like honey (WTHIT Run Speed 0.45), visual sink.
- RC-29 each new flavour: item, block, fluid, milkshake round trip.

---

## sweets #5: Smoothie
Status **Planned** · Work: KubeJS · id `baking/sweets #5`

### Goal
There is one smoothie against many milkshakes. Remove it; smoothies come back only as a type in a later sweets parity
expansion.

### Now
- `neapolitan:strawberry_banana_smoothie` (`i.strawberry_banana_smoothie`, item_dict.js:12424): 3 / 0.05,
  Agility 30 s, drink, leaves a bottle. Typed `drink` in
  [food_types.json](../../kubejs/generation_scripts/food/food_types.json) (not `sweet`), listed in
  `generation_scripts/stacks/stack_sizes.json:754`, a `n/a` row in can_parity.md.
- Only recipe (verified): `neapolitan:mixed/strawberry_banana_smoothie` shapeless `[#forge:fruits/banana,
  #forge:fruits/strawberry x2, glass_bottle]`. No recipe in the cached jars uses it. No CCK fluid (CCK's pomegranate
  smoothie is for an Abnormals mod that isn't installed).

### Build (if removed, D1)
1. `R.remove({ id: 'neapolitan:mixed/strawberry_banana_smoothie' }, e)` in cooking.js "Sweets".
2. Add `i.strawberry_banana_smoothie` to `global.removedItems` in
   [removed_items.js](../../kubejs/startup_scripts/_lib/removed_items.js): hides it in JEI, strips its tags, logs any
   recipe still naming it.
3. Rerun `tools/food_catalog.py` after the next `/dumpfood` (the item stays registered, so move it to `excluded` in
   food_types.json with a reason), and `gen_stack_sizes.py`.

### Ids and data
Verified as above.

### Decisions
- **D1 Remove or expand.** Recommend remove now. An expansion (one smoothie per fruit pair, fluid + spout like the
  milkshakes) belongs to the parity work of #4 d/f and can bring the type back with variants.

### Gotchas
- Removed items still exist in old inventories and loot. Neapolitan has no loot entry for it (not checked in every
  structure mod).

### Tests
- RC-30 JEI has no smoothie; `[jeiRemove]` log line lists no recipe for it.

---

## sweets #6: Orange jam from 4 orange slices
Status **Planned** · Work: KubeJS · id `baking/sweets #6`

### Goal
Orange jam is made from 4 orange slices instead of 2 whole oranges.

### Now (verified, Fruits Delight 0.5.11)
- `fruitsdelight:orange_jelly` ("Orange Jam", `i.orange_jelly`, item_dict.js:12295; type `ingredient`).
- Pot: `farmersdelight:cooking/orange_jelly` (shipped in the Fruits Delight jar) `[#forge:fruits/orange x2, minecraft:sugar,
  fruitsdelight:lemon_slice]`, container glass bottle, 200 ticks, 0.1 xp. Slice & Dice makes its heated mixing copy.
- Fluid route: `fruitsdelight:mixing/flowing_orange_jam` heated `[lemon_slice, sugar, #forge:fruits/orange x2, 100 mB
  water]` → 125 mB `fruitsdelight:orange_jam`; `filling/orange_jelly` (bottle + 125 mB), `compacting/orange_jelly_block`
  (1000 mB), `orange_jelly` (block + 8 bottles → 8).
- Fruits Delight's cauldron jam (`enableCauldronRecipe = true` in fruitsdelight-common.toml) is Java; it isn't changed.
- One orange cuts into 4 slices (`fruitsdelight:orange_cutting` knife, `fruitsdelight:cutting/orange_slice` Create saw).
  `forge:fruits/orange` holds only `fruitsdelight:orange`.

### Build
1. cooking.js "Sweets": `R.remove({ id: 'farmersdelight:cooking/orange_jelly' }, e)` and
   `R.remove({ id: 'fruitsdelight:mixing/flowing_orange_jam' }, e)`.
2. `R.fdCooking([i.orange_slice, i.orange_slice, i.orange_slice, i.orange_slice, i.any_sugar, i.lemon_slice],
   i.orange_jelly, e, 'orange_jam', 0.1, 200, i.glass_bottle)` (6 slots, the pot's maximum; Slice & Dice copies it).
3. Add `f.orange_jam = 'fruitsdelight:orange_jam'` to fluid_dict.js, then
   `R.createMixing(Fluid.of(f.orange_jam, 125), [Item.of(i.orange_slice, 4), i.lemon_slice, i.any_sugar, Fluid.of(f.water, 100)], e, 'orange_jam', 'heated')`.

### Ids and data
Verified: `i.orange_slice` 12298, `i.lemon_slice` 12276, `i.orange` 12887, `i.orange_jelly` 12295.

### Decisions
- **D1 Cost.** 4 slices is one orange, half of today's 2 oranges. Recommend accepting the cheaper jam (it's how the
  row reads); the alternative is 8 slices (same cost, uses the pot's slots badly).

### Gotchas
- The pot recipe's id is in the `farmersdelight` namespace even though Fruits Delight ships it.
- Slice & Dice's copy follows the new pot recipe on its own (food.md 3.7), so the mixer gets both an item route and
  the fluid route.

### Tests
- RC-31 pot: 4 orange slices + sugar + lemon slice + bottle → orange jam; the 2-orange recipe is gone in JEI.
- RC-32 heated mixer: 4 slices + lemon slice + sugar + 100 mB water → 125 mB orange jam.

## Writeup corrections made

None. (`remainder_buffer.md`, linked from `food/drinks #2`, ends with a superseded draft that another agent has
already marked.)
