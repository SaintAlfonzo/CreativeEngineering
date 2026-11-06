# Brief: baking / sweets

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# baking`, Sub `sweets` (rows 1 to 6); checked again 2026-10-31
(rows 1 to 4 In Progress, 5 and 6 Testing). Writeups:
[food_families.md](../food_families.md) 3.1 (family rules: `candy/piece`, `frozen/ice_cream`, `frozen/popsicle`,
`dessert/pudding`), [food.md](../food.md) (types, 4.5 plans), [food_catalog.md](../food_catalog.md) (current values,
`sweet` table), [delightful.md](../delightful.md) (the 3.8.1 repack: new gummies, popsicle, milkshakes),
[recipes_cooking.md](../recipes_cooking.md) (test prefix `RC-`; new tests are placeholders `RC-newN`, proposed, numbered when
queued), [blockrunner.md](../blockrunner.md).
The hand-written cooking plan these rows came from is in git (`git show c82dd31^:feature_writeup/cooking.md`, tree
"sweet > candy / cold").

**Verified against jars** (scratchpad cache, decompiled with Vineflower): Delightful 3.4.1 (since 2026-10-26 the
Creative Delight member `repackedDelightful`, carrying the 3.8.1 items and values, delightful.md 3), Neapolitan 4.1.0,
Fruits Delight 0.5.11, Farmer's Delight 1.2.4, Abnormals Delight 4.1.2, More Delight 25.07.28, Create Central Kitchen
(CCK) 1.3.11.c, Quark 3.4-418, KubeJS 1902.6.2-build.73. Everything else is marked unverified or "in game".

**Where the code goes (since 2026-10-25, memory `creative-delight-routing`):** all food KubeJS lives in the Creative
Delight trees, one folder per mod; pack-native sweets go in `creativeengineering/`: startup
`development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/sweets.js` (needs a CD rebuild +
restart), server `kubejs/data/creativedelight/kubejs/creativeengineering/sweets.js` inside `global.CD.recipes(() => ...)`
(recipe ids `creativedelight:<type>/<suffix>`). Edits to an upstream mod's own recipes go in that mod's folder
(`delightful/`, `neapolitan/`, `fruitsdelight/`). Item ids keep the `creativeengineering:` namespace, as
`creativeengineering:corn_popsicle` (CD `corn_delight/corn_items.js:16`).

## Scope and order

| Row | Size | Build with |
|---|---|---|
| `baking/sweets #6` Orange jam from 4 slices | BUILT 2026-10-23, shipped 1.1.03 (Testing) | done |
| `baking/sweets #5` Smoothie | REMOVED 2026-10-29 (Testing): `global.removedItems` (removed_items.js) + its recipes removed in CD `neapolitan/recipes.js`; test RC-164 | done; the parity expansion is open, folded into #4 d/f |
| `baking/sweets #4` Ice cream rework | large, in steps a-g | a DONE (1.1.01, `dishes/ingredients #5`); b, e next (no new items); c, d, f use the CD fluid file (exists since 2026-10-25) |
| `baking/sweets #2` Rock candy variants | small-medium (new items) | builds the shared "pack sweets" table (below) |
| `baking/sweets #1` Gummies | medium (10-ish new items) | same table as #2 |
| `baking/sweets #3` Cold sweets variants | medium (new items) | same table; `dishes/ingredients #5` (ice cubes) is built (Testing) |

Order: #4 b/e → #2 → #1 → #3 → #4 c/d/f/g.

**Shared infrastructure for #1, #2, #3, #4f (build once, with whichever comes first):**
- Item ids in [item_dict.js](../../kubejs/startup_scripts/_lib/item_dict.js): a new hand-written block
  `// Pack sweets (TODO baking/sweets #1-#4). Items: CD creativeengineering/sweets.js` in the `########## Tags ##########`
  part next to the other CD-registered pack items (Corn Delight line 1828, glowing calamari line 1833), keys
  `i.<fruit>_gummy`, `i.<crystal>_rock_candy`, `i.<fruit>_popsicle` ... = `'creativeengineering:<same>'`. Fruit tag keys
  that exist: blueberry, cranberry, lemon, fig, mangosteen, hawberry, lychee, mango, peach, bayberry, banana, strawberry,
  pear, persimmon, salmonberries, berries (item_dict.js:1877-1893), cantaloupe (2032). Missing and needed by the D1
  picks: `i.t_fruits_orange`, `i.t_fruits_kiwi`, `i.t_fruits_pineapple` (`'#forge:fruits/<name>'`, next to line 1891).
- Modded effect ids in [effect_dict.js](../../kubejs/startup_scripts/_lib/effect_dict.js): it now has some modded ids
  (`ef.caffeinated`, `ef.agility`, `ef.climbing` ... lines 51-55) but no `fruitsdelight:` one. Add
  `ef.refreshing = 'fruitsdelight:refreshing'`, `ef.brightening`, `ef.sweetening`, `ef.recovering`, `ef.heal_aura`,
  `ef.lozenge`, `ef.appetizing` (all `fruitsdelight:`), ids listed in [effects.md](../effects.md). Effect text and
  changes are reserved for Creative Brewing, but the dict stays shared (creative_brewing.md 4), so adding keys is fine.
- One table + one registration loop: CD startup `creativeengineering/sweets.js` holds
  `global.sweets = { gummy: [...], rock_candy: [...], ... }`, each entry `{ key, name, flavour (dict key), effects:
  [[effKey, ticks, amp]], texture }`, and registers the items in `StartupEvents.registry('item')`, the same shape as the
  generated-can loop in [cans.js:40-66](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js#L40-L66).
  CD server `creativeengineering/sweets.js` reads the same table and writes the recipes through NCH. Per-fruit effects:
  take each fruit's Fruits Delight jello effect (the `sweet` table in food_catalog.md: lemon = haste + refreshing,
  orange = regeneration + recovering, mango = strength III + rage_aura, peach = heal_aura, pear = lozenge, ...).
- Container return on eating: KubeJS items don't hand back a stick or bottle when eaten (vanilla `finishUsingItem`).
  Use the food builder's `eaten` callback as the Corn Popsicle does
  ([corn_items.js:16-22](../../development/creativeDelight/src/main/resources/kubejs_startup/corn_delight/corn_items.js#L16-L22),
  live since 1.1.03): `f.eaten(ev => { var p = ev.player; if (p && !p.isCreative()) p.give('minecraft:stick') })`
  (cans.js:37 uses `ev.entity`; both work). Also set `.containerItem(...)` so the item leaves its container when used as
  a crafting ingredient.
- After any new food: `/dumpfood` → `python3 tools/food_catalog.py` (types them into `food_types.json`; new ids go
  under `sweet`), `tools/gen_stack_sizes.py` (ice creams and custards sit in the 32 group of
  `generation_scripts/stacks/stack_sizes.json`), the family in `generation_scripts/food/food_families.json`
  (food_families.md 3.1), compost values (Delightful gives its gummy 0.65:
  [delightful/compost_values.js:60](../../development/creativeDelight/src/main/resources/kubejs_startup/delightful/compost_values.js#L60);
  pack items go in CD `creativeengineering/` compost per compost_rules.js), the Supplementaries jar tag for candies
  ([jar_tags.js:42](../../kubejs/data/creativedelight/kubejs/creativeengineering/jar_tags.js#L42)), and a `/`
  row per item in [can_parity.md](../can_parity.md) (`cans/canning #2`). Sprites go in
  `kubejs/assets/creativeengineering/textures/item/<id>.png` (as `corn_popsicle.png`; recolours of the mod sprite
  named in each row).

## Built, needs play

- `baking/sweets #5` Smoothie removed (2026-10-29; [section below](#sweets-5-smoothie)); tiers S P open; RC-164,
  SQ-CM-24 queued. The parity expansion stays open under #4 d/f.
- `baking/sweets #6` Orange jam from 4 orange slices (built 2026-10-23, shipped 1.1.03;
  [recipes_cooking.md](../recipes_cooking.md) RC-79); tiers S P open.

---

## sweets #1: Gummies
Status **In Progress** · Work: KubeJS (+ textures) · id `baking/sweets #1`

### Goal
A gummy for most fruits. Gelling agent: kelp or slime ball. Three routes: a cheap manual craft, a more efficient
Cooking Pot recipe, and a bulk Create recipe.

### Now
- Three Delightful gummies now: `delightful:salmonberry_gummy` (`i.salmonberry_gummy`, item_dict.js:11429), and since
  the 3.8.1 repack (2026-10-25, delightful.md 2.1) `delightful:cantaloupe_gummy` and `delightful:matcha_gummy`
  (item_dict.js:2012, 2020). `GummyItem`: eats in 14 ticks; `Nutrition.SALMONBERRY_GUMMY` = 2 hunger, 0.0 modifier,
  always edible, Water Breathing 400 ticks (verified; 3.8.1 makes it 45 s, effect changes not applied, delightful.md 2.4).
  Hostile Delight adds `hostiledelight:glowgummy` (not a fruit gummy, leave it).
- Recipes (verified): `delightful:gummy/salmonberries` FD pot `[#forge:fruits/salmonberries, minecraft:sugar,
  minecraft:honey_bottle, minecraft:kelp]` → 1, 200 ticks, 1.0 xp, no container; `delightful:mixing/salmonberry_gummy`
  Create heated mixing `[salmonberries, sugar, honey_block, kelp]` → 9. The cantaloupe and matcha gummies copy that
  shape in CD [delightful/recipes.js:39-46](../../kubejs/data/creativedelight/kubejs/delightful/recipes.js#L39-L46)
  (`GUMMIES` loop: pot `[fruit, i.any_sweetener, i.honey_bottle, i.kelp]` → 1, heated mixing with a honey block → 9).
  The pack's sugar swap ([CD minecraft/recipes.js:42-47](../../kubejs/data/creativedelight/kubejs/minecraft/recipes.js#L42-L47))
  turns `minecraft:sugar` into `i.any_sweetener` (`#creativeengineering:sweeteners`) in most recipes.
- Slice & Dice copies every pot recipe to heated mixing after KubeJS runs, unless the id ends in `_manual_only`
  ([food.md](../food.md) 3.7).

### Build
1. Pick the fruits (D1) and add `i.<fruit>_gummy` keys plus any missing `i.t_fruits_*` keys (see Shared).
2. Tag `#creativeengineering:gelling_agents` = kelp, slime ball (D3 for dried kelp) in CD
   [creativeengineering/tags.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/tags.js) via
   `global.tags` `t.add(e, i.t_gelling_agents, [i.kelp, i.slime_ball])`, dict key
   `i.t_gelling_agents = '#creativeengineering:gelling_agents'` (no such key or tag yet).
3. Register the items from the sweets table:
   ```js
   e.create(i[g.key]).displayName(g.name).texture('creativeengineering:item/' + g.key)
     .useDuration(() => 14)                          // GummyItem speed
     .food(f => { f.hunger(2).saturation(0.1).alwaysEdible()
       g.effects.forEach(x => f.effect(global.effect[x[0]], x[1], x[2], 1)) })
   ```
4. Recipes per gummy in CD server `creativeengineering/sweets.js` (fruit = dict key of the fruit or its tag):
   - manual: `R.shapeless(Item.of(gummy, 2), [fruit, i.any_sweetener, i.t_gelling_agents], e, key)`
   - pot: `R.fdCooking([fruit, fruit, i.any_sweetener, i.honey_bottle, i.t_gelling_agents], Item.of(gummy, 6), e, key + '_manual_only', 0.35, 200)`
   - Create: `R.createMixing(Item.of(gummy, 12), [fruit, fruit, i.any_sweetener, i.honey_block, i.t_gelling_agents], e, key, 'heated')`
   (the `_manual_only` id stops a weaker Slice & Dice copy next to the hand-written one).
5. Bring the three Delightful gummies onto the same scheme: in CD `delightful/recipes.js` replace the `GUMMIES` loop
   (lines 39-46) with table entries marked `existing: true` (recipes only, no registration), and remove Delightful's own
   `delightful:gummy/salmonberries` and `delightful:mixing/salmonberry_gummy` there (`R.remove({ id: ... }, e)`).
6. Sprites: recolour `assets/delightful/textures/item/salmonberry_gummy.png` per fruit.
7. Follow-ups from Shared (food types, stack sizes, compost, can parity).

### Ids and data
- Verified: the ids and recipes above; Delightful's `forge:fruits/salmonberries` = `delightful:salmonberries`.
- Fruit tags present (Fruits Delight jar, verified): persimmon, fig, mango, cranberry, hawberry, mangosteen, pear,
  orange, peach, lychee, kiwi, lemon, pineapple, bayberry, blueberry. Neapolitan: `forge:fruits/strawberry`, `banana`.
  Vanilla fruits (apple, sweet berries, glow berries, melon slice) use their dict item keys.
- New: `creativeengineering:<fruit>_gummy`. Cantaloupe (Delightful) is already done, so drop melon from D1 or keep
  watermelon apart (D1).

### Decisions
- **D1 Fruits.** Recommend 11 new: apple, sweet berry, glow berry, melon (watermelon slice; cantaloupe exists),
  strawberry, blueberry, lemon, orange, mango, peach, pineapple, kiwi; minus one if melon reads too close to cantaloupe.
  Skip grapes (Vinery wine chain), coffee berries, durian, acorn.
- **D2 Values.** Delightful's is 2 / 0.0. Recommend 2 / 0.1 and always-edible (a snack); effect = the fruit's jello
  effect at half the jello duration. Revisit with the tuning system (`food/types-and-tuning #1`).
- **D3 Gelling agents.** Kelp and slime ball as asked; recommend dried kelp too. Quark Delight's gelatine bucket is
  another option, not recommended (too dear for a gummy).
- **D4 Yields.** Proposed 2 / 6 / 12 above, against Delightful's 1 / 9 (pot / heated mixer, now also used for the two
  3.8.1 gummies). Recommend moving all three Delightful gummies to the pack yields too (step 5), so every gummy reads the
  same in JEI.

### Gotchas
- The pot has 6 slots: the pot recipe above uses 5.
- `forge:fruits/strawberry` (Neapolitan) vs `forge:fruits/strawberries` (Delightful's berry tag lists the plural, which
  nothing fills here). Use the singular.
- Build the recipe loop with `forEach`. A `for (var ...)` loop with closures in Rhino captures the last value.

### Tests
(proposed, numbered when queued; were RC-19 to RC-21 in the 2026-10-21 brief, since used by other docs)
- RC-new1 each gummy: the shapeless, pot and heated mixing recipes show in JEI and give 2 / 6 / 12.
- RC-new2 eating: a gummy eats fast, applies its effect, and AppleSkin shows 2 / 0.1.
- RC-new3 the salmonberry, cantaloupe and matcha gummies take slime balls (the new recipes) and Delightful's own
  recipes and the old `GUMMIES` loop recipes are gone.

---

## sweets #2: Rock candy variants
Status **In Progress** · Work: KubeJS (+ textures) · id `baking/sweets #2`

### Goal
More rock candies from other crystals. **Decided 2026-10-29 (TODO):** the rose quartz one stays; add quartz, amethyst and
echo shard variants; identify other crystals usable for rock candy (D1 below lists them).

### Now
- `delightful:rock_candy` (`i.rock_candy`, item_dict.js:11426). `ConsumableItem`: hands back a stick; fast, Speed
  120 ticks (verified `Nutrition.ROCK_CANDY`); hunger / saturation 4 / 0.5 since the 3.8.1 values
  ([delightful_foods.js:16](../../development/creativeDelight/src/main/resources/kubejs_startup/delightful/delightful_foods.js#L16),
  was 4 / 0.2). It is in the Supplementaries jar tag (CD `creativeengineering/jar_tags.js:42`).
- Recipe `delightful:cooking/rock_candy` (verified, not edited by the pack): FD pot `[#forge:gems/rose_quartz x2,
  #forge:sugar]`, container `minecraft:stick`, 200 ticks, 0.35 xp, loads only while that tag isn't empty.
  `forge:gems/rose_quartz` = `create:rose_quartz` (live dump `kubejs/exported/tags/item_tags.json`).
- Crystals in the pack (live `forge:gems/*`, same dump): quartz `minecraft:quartz`, amethyst `minecraft:amethyst_shard`,
  prismarine `minecraft:prismarine_crystals`, certus quartz (`ae2:certus_quartz_crystal`, charged too), fluix
  `ae2:fluix_crystal`, ruby / sapphire / peridot (`projectred_core:`), plus diamond, emerald, lapis, Botania's
  dragonstone and mana diamond, Vintage Improvements' sulfur. Echo shard `minecraft:echo_shard` has no gem tag. Quark
  corundum clusters (9 colours, `i.<c>_corundum_cluster`, `i.any_corundum = '#quark:corundum'` item_dict.js:156) are
  the other "crystal" option the original row named.
- Dict keys: `i.quartz` 1365, `i.amethyst_shard` 1356, `i.echo_shard` 1364, `i.prismarine_crystals` 1361,
  `i.rose_quartz` 1487, `i.t_gems_certus_quartz` 2034. No `i.t_gems_quartz` / `i.t_gems_amethyst` keys yet.
- Echo shards are craftable (sculk + amethyst, [crystal.js:40](../../kubejs/data/creativeengineering/kubejs/engineering/crystal.js#L40))
  and a cleric trade ([trades.js:112](../../kubejs/server_scripts/economy/trades.js#L112)), so an echo candy is
  reachable, if dear.

### Build
1. Keys `i.<crystal>_rock_candy = 'creativeengineering:<crystal>_rock_candy'` (quartz, amethyst, echo, plus the D1
   extras) in the pack sweets block; tag keys `i.t_gems_quartz = '#forge:gems/quartz'`,
   `i.t_gems_amethyst = '#forge:gems/amethyst'` (tags take other mods' copies).
2. Table entries; register as food like Delightful's: `f.hunger(4).saturation(0.5).fastToEat()` (the live rock candy
   values), the effect (D2), the stick back (Shared), `.containerItem(i.stick)`.
3. Recipes in CD server `creativeengineering/sweets.js`:
   `R.fdCooking([gem, gem, i.any_sugar], Item.of(candy, 1), e, '<crystal>_rock_candy', 0.35, 200, i.stick)`, gem = the
   tag key (echo: `i.echo_shard`, 1 shard → 2 candies, D3). Slice & Dice adds the heated mixing copy by itself (fine
   here: same yield). Use `i.any_sugar` (plain sugar), not `i.any_sweetener`: honey is not rock candy.
4. Sprites: recolour `assets/delightful/textures/item/rock_candy.png` (white for quartz, purple for amethyst, dark teal
   for echo).
5. Shared follow-ups: jar tag (jar_tags.js:42), food types, families (`candy/piece`), compost, can parity.

### Ids and data
Verified: everything under Now (dump of 2026-10-30). New: `creativeengineering:<crystal>_rock_candy`.

### Decisions
- **D1 Other crystals.** Recommend adding prismarine (sea candy, its own colour) and one Quark corundum family
  candy taking `#quark:corundum` clusters only if a colour set is wanted later. Skip diamond, emerald, lapis (money
  and enchanting), AE2 crystals and fluix (machine progression), Project Red gems (tech), Botania gems, sulfur.
- **D2 Effects.** Recommend Speed 6 s on all of them, like the original, plus one small flavour per crystal: quartz
  Haste 6 s, amethyst Night Vision 10 s, echo Slow Falling 6 s, prismarine Water Breathing 10 s. Owner to pick;
  "Speed only" is the safe default.
- **D3 Echo cost.** One echo shard is worth more than 2 quartz: recommend 1 shard + sugar → 2 candies.

### Gotchas
- `delightful:cooking/rock_candy` is gated on `forge:gems/rose_quartz` being filled; the new recipes use their own tags,
  so they load regardless.
- Quartz and amethyst are also Create / IE inputs: keep the candy at 2 gems per candy so it is never a cheap sink.

### Tests
(proposed, numbered when queued; were RC-22 / RC-23 in the 2026-10-21 brief, since used by other docs)
- RC-new1 every crystal: the pot recipe (2 gems + sugar + stick; echo 1 shard → 2) makes the candy; eating hands back a stick.
- RC-new2 the rose quartz candy still has its recipe and eats at 4 / 0.5.

---

## sweets #3: Cold sweets variants
Status **In Progress** · Work: KubeJS (+ textures) · id `baking/sweets #3` · `dishes/ingredients #5` (ice cubes) is built (Testing)

### Goal
More custard, shaved ice and popsicle flavours.

### Now (recipes verified)
| Kind | Existing | Recipe | Leaves |
|---|---|---|---|
| Custard | `farmersdelight:glow_berry_custard` | FD pot `[glow_berries, #forge:milk, #forge:eggs, sugar]`, no container field | glass bottle |
| Custard | `fruitsdelight:blueberry_custard` | FD pot (`farmersdelight:cooking/blueberry_custard` in the Fruits Delight jar) `[blueberry x2, #forge:milk/milk_bottle, #forge:eggs, sugar]`, container bottle | glass bottle |
| Shaved ice | `fruitsdelight:hamimelon_shaved_ice` | shapeless `[glass_bottle, hamimelon_slice x2, #forge:milk/milk_bottle, sugar, minecraft:ice]` | glass bottle |
| Popsicle | `farmersdelight:melon_popsicle` | shaped `" mm","imm","-i "` m melon slice, i ice, - stick. Abnormals Delight replaces FD's with a Neapolitan-ice-cube version when Neapolitan is loaded (`abnormals_delight:neapolitan/melon_popsicle`); FD's own is off | nothing |
| Popsicle | `fruitsdelight:kiwi_popsicle` | same shape with `minecraft:ice` | stick |
| Popsicle | `delightful:cantaloupe_popsicle` (3.8.1 repack) | CD `delightful/recipes.js:24-25`: ` cc / icc / si ` or `im / si` (whole cantaloupe), `i.t_ice_cubes`, `i.any_stick` | stick |
| Popsicle | `moredelight:chocolate_popsicle` | same shape, cocoa beans | nothing |
| Popsicle | `creativeengineering:corn_popsicle` (CD `corn_delight/corn_items.js:16`) | CD `corn_delight/recipes.js:66` | stick |
| Popsicle | `ends_delight:chorus_fruit_popsicle` | End's Delight's own | unverified |

The Fruits Delight hamimelon popsicle is merged into the cantaloupe one (owner 2026-10-28, CD
`fruitsdelight/item_merges.js:15-16`; its recipe removed in the server `fruitsdelight/item_merges.js:17`).
Values: food_catalog.md `sweet` table. **Ice is unified (1.1.01, `dishes/ingredients #5`):** `#forge:ice_cubes` holds
`minecraft:ice` and Neapolitan's cubes (CD `minecraft/tags.js:10`), dict key `i.t_ice_cubes` (item_dict.js:1894), and CD
[neapolitan/recipes.js:36-44](../../kubejs/data/creativedelight/kubejs/neapolitan/recipes.js#L36-L44) swaps
`neapolitan:ice_cubes` for the tag pack-wide and `minecraft:ice` for it in the melon / chocolate / kiwi popsicles, the
hamimelon shaved ice and the bellini.

### Build
1. ~~Add `i.t_ice_cubes`~~ done (item_dict.js:1894, `dishes/ingredients #5`).
2. Table entries per kind (D1). Item shape per kind:
   - custard / shaved ice: drink-like (`.useAnimation('drink')`, bottle back as in Shared,
     `.containerItem(i.glass_bottle)`); custards stack 32 (stack_sizes.json `sizes/32` holds `glow_berry_custard`).
   - popsicle: fast food, gives back a stick like the Corn Popsicle (Shared).
3. Recipes, mirroring the originals:
   - custard: `R.fdCooking([fruit, fruit, i.t_milk_bottle, i.eggs, i.any_sugar], custard, e, key, 0.1, 200, i.glass_bottle)`
   - shaved ice: `R.shapeless(ice, [i.glass_bottle, fruit, fruit, i.t_milk_bottle, i.any_sugar, i.t_ice_cubes], e, key)`
   - popsicle: `R.shaped(pop, [' FF', 'IFF', 'SI '], { F: fruit, I: i.t_ice_cubes, S: i.any_stick }, e, key)`
   (all in CD server `creativeengineering/sweets.js`; the new recipes use the tag directly, so no swap is needed).
4. ~~Swap `minecraft:ice` for `#forge:ice_cubes` in the existing ice recipes~~ done 1.1.01 (neapolitan/recipes.js:44).
5. Sprites: recolour the custard, shaved ice and popsicle sprites of FD / Fruits Delight / Delightful.

### Ids and data
Verified: the table above (`i.glow_berry_custard` 11608, `i.blueberry_custard` 12408, `i.cantaloupe_popsicle` 2009,
`i.hamimelon_shaved_ice` 12434; `i.hamimelon_popsicle` 12433 is merged away). New: `creativeengineering:<fruit>_custard`,
`<fruit>_shaved_ice`, `<fruit>_popsicle`.

### Decisions
- **D1 Which.** Recommend custard + sweet berry, strawberry, mango; shaved ice + melon, lemon, peach; popsicle + sweet
  berry, strawberry, orange, lemon, pineapple. Each takes its fruit's jello effect at the duration of the kind it copies.
  Melon and cantaloupe popsicles exist; a melon shaved ice is still new.
- ~~D2 Ice input~~ decided by `dishes/ingredients #5`: `#forge:ice_cubes` (`i.t_ice_cubes`).

### Gotchas
- The melon popsicle's live recipe is Abnormals Delight's ice-cube copy, not FD's. A `R.replaceInput` keyed on FD's id
  finds nothing.
- Food Plate placement is the frozen family's rule for popsicles (food_families.md 3.1, `frozen/popsicle`): add the new
  ones to the placeable tooltip tags (CD `creativeengineering/placeable_tags.js`) if Food Plate takes them.

### Tests
(proposed, numbered when queued; were RC-24 in the 2026-10-21 brief, since used by other docs)
- RC-new1 each new cold sweet: recipe in JEI takes ice or ice cubes, eaten hands back the bottle / stick, effect applies.

---

## sweets #4: Ice cream rework
Status **In Progress** · Work: KubeJS / data (+ textures) · id `baking/sweets #4`

### Goal
One recipe shape for every ice cream, a fluid per flavour with mixing recipes, bowl spouting, an ice cream block per
flavour that is sticky and sinks you in (BlockRunner), more flavours. The matcha and salmonberry ice creams stop
being gross. All ice creams match each other (scratchpad 2026-10-21: "ice cream parity"). Fungi Delight's Truffle Ice
Cream joins as a flavour (TODO row, its grid recipe is a placeholder).

### Now
- Ice creams (all bowl foods, stack 32 in the pack's `stack_sizes.json` `sizes/32`): Neapolitan vanilla, chocolate,
  strawberry, banana, mint, adzuki (6 / 0.4 each, one flavour effect each) and the mixed
  `neapolitan:neapolitan_ice_cream` (12 / 0.4); Delightful `matcha_ice_cream` (Slowness III 5 s, Vitality II 60 s,
  Caffeinated 60 s) and `salmonberry_ice_cream` (Slowness III 5 s, Water Breathing 60 s), both at **6 / 0.3** since the
  3.8.1 values ([delightful_foods.js:14-15](../../development/creativeDelight/src/main/resources/kubejs_startup/delightful/delightful_foods.js#L14-L15);
  was 6 / 0.5 and 6 / 0.4). That script keeps the 3.4.1 effects (3.8.1 drops the Slowness, delightful.md 2.4, "effects
  wait for the effects review"), so the **Slowness III** is still live and is the "gross" part; Neapolitan's
  `IceCreamItem` adds 200 freeze ticks instead (verified).
- Fungi Delight's `fungidelight:truffle_ice_cream` (7 / 0.5, Haste 10 s, bowl, stack 16, fungi_delight.md) has only the
  placeholder grid recipe `truffle_ice_cream_manual_only` (slice, snowball, sweetener, milk, bowl;
  [CD fungidelight/cooking.js:22](../../kubejs/data/creativedelight/kubejs/fungidelight/cooking.js#L22)); it joins as a
  flavour (TODO).
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
- No fluid and no spouting for matcha or salmonberry. **Milkshakes exist since the 3.8.1 repack (2026-10-25):**
  `delightful:matcha_milkshake`, `salmonberry_milkshake` (`ShakeItem`), grid `[3 bottles, ice cream, milk]` → 3
  ([CD delightful/recipes.js:62-63](../../kubejs/data/creativedelight/kubejs/delightful/recipes.js#L62-L63)), plus the
  Neapolitan milk-cauldron interaction in the repack (delightful.md 3, DF-07). No milkshake fluid for them.
- Create: Dreams and Desires also registers milkshake fluids (`create_dd:chocolate|glowberry|pumpkin|strawberry|vanilla_milkshake`,
  live `kubejs/exported/fluids.json`); its mixing recipes are already handled in CD `minecraft/recipes.js:39` (sugar
  swap list). Two vanilla / chocolate / strawberry milkshake fluids exist side by side (CCK and D&D).
- Blocks: `neapolitan:<f>_ice_cream_block` (6) and `delightful:matcha_ice_cream_block`, `salmonberry_ice_cream_block`;
  plain `Block`, snow material, hardness 0.2, snow sound; recipe 8 snow blocks around 1 ice cream → 8 (verified).
  No mixed-Neapolitan block.
- BlockRunner: `delightful:*_ice_cream_block` is in the `confectionery` tier (0.85,
  [blocktags.js:380-382](../../kubejs/server_scripts/tags/blocktags.js#L380-L382)). The Neapolitan blocks are not named in
  any tier (they have tags, so `tools/blockrunner_untagged_blocks.json` doesn't list them either): check their speed with
  the WTHIT Run Speed line. `very_sticky` (0.45: honey, slime, banana slug slime, Fruits Delight jelly and jello blocks,
  blocktags.js:416-419) is part of `#creativeengineering:sinking` (blocktags.js:447), which gives the visual sink-in.

### Build
a. ~~**Unify the recipes.**~~ DONE 1.1.01 (`dishes/ingredients #5`, Testing): `i.t_ice_cubes` exists and CD
   [neapolitan/recipes.js:39](../../kubejs/data/creativedelight/kubejs/neapolitan/recipes.js#L39) swaps
   `neapolitan:ice_cubes` for it in every recipe but `neapolitan:ice`, so the Neapolitan shapeless and the CCK mixing
   recipes take ice cubes or an ice block. Sugar slots take `i.any_sweetener` (CD minecraft/recipes.js:47). Left: the
   truffle ice cream onto the same shape (step f).
b. **Not gross, parity values.** Startup, `ItemEvents.modification`:
   ```js
   var $Effects = Java.loadClass('net.minecraft.world.effect.MobEffects')
   ;[i.matcha_ice_cream, i.salmonberry_ice_cream].forEach(id =>
     e.modify(id, item => item.setFoodProperties(f => { f.removeEffect($Effects.MOVEMENT_SLOWDOWN); f.saturation(0.4) })))
   ```
   (`FoodBuilder.removeEffect(MobEffect)` and the consumer form of `setFoodProperties` verified in the KubeJS jar; the
   consumer form runs in cans.js:72.) Simpler now: CD
   [delightful_foods.js:39-41](../../development/creativeDelight/src/main/resources/kubejs_startup/delightful/delightful_foods.js#L39-L41)
   already rebuilds both foods and copies the old effects; skip `MOVEMENT_SLOWDOWN` in that copy loop for the two ice
   creams (and set 0.4 in `global.delightfulFoodValues` for parity, D1). Server: `ItemEvents.foodEaten(i.matcha_ice_cream,
   e => e.entity.setTicksFrozen(e.entity.getTicksFrozen() + 200))` (and salmonberry) for Neapolitan's brain freeze, in CD
   server `delightful/`. Check first with `potions/effects` whether the effects review already plans the 3.8.1 effect
   set (delightful.md 2.4).
c. **Fluids for matcha and salmonberry.** In the shared startup fluid file `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js`
   (live since 2026-10-25 with 7 virtual fluids: chorus juice, the Veggies juices, egg solution, ...; ids
   `creativeengineering:<name>` in fluid_dict.js "Pack fluids", lines 89-118; same `.noBucket().noBlock()` shape):
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
d. **Milkshake fluids for them** (D3): the items and the shapeless recipe exist (Delightful 3.8.1 repack, Now). Left:
   fluids `creativeengineering:matcha_milkshake`, `salmonberry_milkshake` and the CCK-style mixing (500 ice cream + 250
   milk → 750) / filling (bottle + 250) / emptying. Same for the truffle flavour if it gets a milkshake.
e. **Blocks sticky and sinking.** blocktags.js: drop `'delightful:*_ice_cream_block'` from `confectionery` (line 382)
   and add `'neapolitan:*_ice_cream_block', 'delightful:*_ice_cream_block'` to the `very_sticky` list (line 416). That
   puts them in `#creativeengineering:sinking` too. Optional honey-like jump: `block.setJumpFactor(0.5)` in
   [block_properties.js](../../kubejs/startup_scripts/changes/block_properties.js) (jump is not a BlockRunner speed, so it
   doesn't stack). Optional block → items back: `R.shapeless(Item.of(iceCream, 8), [block, Item.of(i.bowl, 8)])`,
   as Fruits Delight does for jelly blocks.
f. **Truffle and more flavours** (D4). Truffle first: in CD server `fungidelight/cooking.js:22` replace the placeholder
   with the unified shape `R.shapeless(i.truffle_ice_cream, [i.bowl, i.truffle_slice, i.milk, i.t_ice_cubes,
   i.any_sweetener], e, 'truffle_ice_cream')`, then its fluid / mixing / filling / emptying as in step c (no block: Fungi
   Delight has none; add one only with the new flavours). Per new flavour: bowl item (KubeJS, stack 32, `f.eaten` gives the bowl back,
   `.containerItem(i.bowl)`), block (`StartupEvents.registry('block')`, `.material('snow').hardness(0.2).soundType('snow')`,
   block tags as in step e), fluid (step c, using the block texture), the unified shapeless recipe, mixing, filling,
   emptying, 8-snow block recipe, sprites (item and block).
g. **Bowl spouting:** done by CCK for the Neapolitan six; steps c and f add it for the rest.

### Ids and data
- Verified: everything under Now. Dict keys exist for all 9 ice creams, the 2 Delightful blocks and both Delightful
  milkshakes (`i.matcha_ice_cream` 11403, `i.salmonberry_ice_cream` 11430, `i.matcha_ice_cream_block` 11404,
  `i.salmonberry_ice_cream_block` 11431, `i.ice_cubes` 12657, `i.truffle_ice_cream`). The CCK fluids are live
  (`create_central_kitchen:<f>_ice_cream`, `<f>_milkshake`, dump 2026-10-30). Neapolitan blocks and the CCK fluids have
  no dict keys: add them (`i.vanilla_ice_cream_block`, `f.vanilla_ice_cream = 'create_central_kitchen:vanilla_ice_cream'` ...).
- New: `creativeengineering:matcha_ice_cream`, `salmonberry_ice_cream`, `truffle_ice_cream` (fluids), the two
  milkshake fluids, plus D4 items, blocks, fluids.

### Decisions
- **D1 Parity values.** Recommend every single-flavour ice cream at 6 / 0.4 + 200 freeze ticks + its own effect; the
  Neapolitan mixed one stays 12 / 0.4. Matcha keeps Vitality and Caffeinated and loses the Slowness. This overrides the
  3.8.1 value 6 / 0.3 now set for matcha and salmonberry. Truffle (7 / 0.5, Haste) comes down to 6 / 0.4 + Haste.
- **D2 Saturation instead?** The scratchpad asks "saturation?". The values above already match Neapolitan's; with the
  Slowness gone, no saturation bump is needed.
- **D3 Milkshakes for the Delightful flavours (and new ones).** The items came with the repack; recommend the fluids
  too: parity means every ice cream has a milkshake, fluid, spout recipe and block. A smoothie type (the removed
  `baking/sweets #5`) can join here as one more drink per fruit pair, if wanted.
- **D6 Two milkshake fluid sets (CCK and D&D).** Out of scope here; note it for `merges` (recommend CCK's as the pack
  set, since it covers all six Neapolitan flavours).
- **D4 New flavours.** Recommend 3 to start: sweet berry, lemon, mango (distinct colours, existing fruit effects).
- **D5 BlockRunner tier.** `very_sticky` (recommended) or a new "ice cream" tier, which would mean a new `[surface.speed]`
  entry and patcherMod `SurfaceTier` change.

### Gotchas
- `setFoodProperties` has two one-argument overloads. cans.js shows the consumer form working; still check the log for
  `[food values]`-style errors after the restart.
- CCK's ice cream fluids are 500 mB per bowl and milkshakes 250 mB per bottle. Keep the new ones the same, or spouts and
  drains won't round-trip.
- Ice creams stack to 32 in the pack (stack_sizes.json `sizes/32`). New ones go in the same group (`food/stack-sizes #1`).
- `delightful_foods.js` rebuilds the food from scratch: any effect change for matcha / salmonberry belongs in that
  script, not a second `ItemEvents.modification`, or the order of the two decides the result.

### Tests
(proposed, numbered when queued; were RC-25 to RC-29 in the 2026-10-21 brief. The old RC-25 check, ice in every ice
cream, is covered by `dishes/ingredients #5`'s RC-76 in [recipes_cooking.md](../recipes_cooking.md).)
- RC-new1 matcha / salmonberry ice cream: no Slowness, freeze overlay as with Neapolitan's, AppleSkin 6 / 0.4.
- RC-new2 matcha, salmonberry and truffle: mixer makes the fluid, spout fills a bowl, drain empties it; milkshake fluids
  fill and empty bottles.
- RC-new3 walking on any ice cream block: slow like honey (WTHIT Run Speed 0.45), visual sink.
- RC-new4 each new flavour: item, block, fluid, milkshake round trip.
- RC-new5 truffle ice cream: crafts in the unified shape (bowl, truffle slice, milk, ice cubes, sweetener); the
  placeholder `truffle_ice_cream_manual_only` is gone.

---

## sweets #5: Smoothie
Status **Testing** (removed 2026-10-29) · Work: KubeJS · id `baking/sweets #5`

Built 2026-10-29 (owner: remove); fold into [recipes_cooking.md](../recipes_cooking.md) (define RC-164 there, it is cited
only by this brief, TODO and SQ-CM-24) and the parity note into food_families.md 3.1 `frozen`.
- `i.strawberry_banana_smoothie` (`neapolitan:strawberry_banana_smoothie`, item_dict.js:12668) is in `global.removedItems`
  ([removed_items.js:385](../../kubejs/startup_scripts/_lib/removed_items.js#L385): hidden in JEI, tags stripped), and CD
  [neapolitan/recipes.js:16](../../kubejs/data/creativedelight/kubejs/neapolitan/recipes.js#L16) removes every recipe
  with it as output. Player bullet "Removed: Strawberry Banana Smoothie." is in the 1.1.05 changelog.
- Leftovers that still name it (harmless, clean up with the next touch): the Supplementaries soft fluid
  `kubejs/data/supplementaries/moonlight/soft_fluids/strawberry_banana_smoothie.json` (jar fill for a hidden item),
  [food_values.js:18](../../kubejs/startup_scripts/cooking/food_values.js#L18) (Agility tooltip list),
  `food_dict.js:1168`, rows in food_catalog.md, food_families.md, can_parity.md (regenerated by the next
  `/dumpfood` → `food_catalog.py`; move it to `excluded` in food_types.json then).
- Open: the "parity expansion" (smoothies as a type) is not a separate build; it joins `baking/sweets #4` D3.
- Tests: RC-164 (JEI has no smoothie; no recipe makes it), SQ-CM-24 queued. Tiers S P open.

---

## sweets #6: Orange jam from 4 orange slices
Status **Testing** · Work: KubeJS · id `baking/sweets #6`

Built 2026-10-23, shipped 1.1.03; the writeup covers it: [recipes_cooking.md](../recipes_cooking.md) RC-79 and its
2026-10-23 log line. As built (owner: oranges or slices): Fruits Delight's 2-orange pot and heated mixing recipes stay;
CD [fruitsdelight/recipes.js:39-46](../../kubejs/data/creativedelight/kubejs/fruitsdelight/recipes.js#L39-L46) adds pot
`orange_jam_from_slices` (4 slices, `i.any_sweetener`, lemon slice, bottle) and the heated mixing (4 slices, lemon slice,
sweetener, 100 mB water → 125 mB `f.orange_jam`, fluid_dict.js:118). The test is RC-79, not RC-31 / RC-32 (those ids
belong to other briefs). Tiers S P open.

## Writeup corrections made

None. (`remainder_buffer.md`, linked from `food/drinks #2`, ends with a superseded draft that another agent has
already marked.)
