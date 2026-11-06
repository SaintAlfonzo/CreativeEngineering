# Brief: food / types-and-tuning

Written 2026-10-21 from [TODO](../todo/TODO.md) `# food`, rows with Sub `types-and-tuning`; updated 2026-10-31 (brief
sweep: rows 13, 16 to 19 added; #3, #4, #6, #9 brought to the pack of 2026-10-31; Testing rows #2, #5, #10 cut to one
line). Reference writeups: [food.md](../food.md) (sections 2.3, 3.5, 3.8, 4.2, 4.3; test prefix `FO-`),
[food_families.md](../food_families.md) (`FF-`), [comfort_nourishment.md](../comfort_nourishment.md) (`CNS-`),
[barbeques_delight.md](../barbeques_delight.md) (`BBQ-`) and the generated [food_catalog.md](../food_catalog.md).
Mod facts marked **(jar)** were read from the decompiled jars on 2026-10-21 (Canned Goods 1.19.2-3, Golden Foods
2.0.0, Spice of Life Apple Pie 2.2.0, Farmer's Delight 1.2.4, KubeJS 1902.6.2-build.73, Delightful 3.4.1, Abnormals
Delight 4.1.2, Untitled Duck Mod 0.6.1). Values marked **(dump)** come from the 2026-10-30 `/dumpfood`
(`kubejs/exported/food/foods.json`, `recipes.json`) unless dated otherwise. Proposed test ids FO-33 to FO-42 below are
from 2026-10-21 and not in food.md yet; renumber when queued.

## Scope and order

Open rows: #1, #12, #16, #17, #18, #19 (Planned), #4 (Bug), #3, #6, #8, #9, #13 (In Progress). Testing: #2, #5, #10,
#11, #15 (listed at the end).

**State on 2026-10-31 that changes the rows:**

- Food code lives in Creative Delight (memory `creative-delight-routing`): cans.js, food_value_copies.js,
  comfort_nourishment.js and cultures.js are CD startup scripts
  (`development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/`), recipes are CD server scripts
  by mod (`kubejs/data/creativedelight/kubejs/<mod>/`). The pack's `startup_scripts/cooking/food_values.js` keeps
  only effect swaps; `alcohol_drinks.js` stays in the pack until Creative Brewing takes it.
- **Comfort and Nourishment have an owner:** CD `comfort_nourishment.js` (priority -100, runs after every other food
  rebuild) sets them from `generation_scripts/food/comfort_nourishment.json` (row #15). Any value writer here must
  leave those two effects alone.
- **Food families exist** (row #13): 33 families / 76 subfamilies, `generation_scripts/food/food_families.json`, tags
  `#creativeengineering:food_family/<path>` (CD seasoning_tags.js). Types (31, food_types.json, 1429 keys) still drive
  cans, stack sizes and the catalog.
- Fresh dumps: `/dumpall` 2026-10-30. `can_types.json` is from 2026-10-27 (cans don't follow #15 yet).

**Build groups and order:**

1. **Quick fixes, no export needed:** #4 leftovers (largemeals lines) -> #3 golden values -> #6 egg migration -> #9
   culture food values.
2. **Export round:** `/dumpfood`, `gen_can_types.py` (picks up #3, #9 and #15), `food_catalog.py`.
3. **#1 tuning system** (A report -> B startup apply -> C SoL map), then #8 soups as the first content pass, then
   #19 skewers (after the BBQ port), #18 meal effects, then the rest by family (#13).
4. **#12 SoL pace and benefits** on top of #1's complexities (same toml as #4).
5. Independent: #16 (one item), #17 (crates), #13 (decisions, then rule passes).

Rows of other subfamilies that depend on this: `cans/canning #1`, `#3` (can values follow #1), `cans/canning #4`
(effect passthrough, D6), `dishes/meals #9` (gold food cost, after #3), `food/seasoning #1` (salt cap needs #1),
`dishes/meals #13`, `#46`, `#50` (skewers, #19).

---

## types-and-tuning #4: Spice of Life config
Status **Bug** / Work config / `food/types-and-tuning #4`

### Goal
One pack-level Spice of Life server config that every new world gets, with the broken entries fixed.

### Done
- 2026-10-22: [defaultconfigs/solapplepie-server.toml](../../defaultconfigs/solapplepie-server.toml) =
  `configs_edited/server_config/solapplepie-server.toml` (identical, `cmp` 2026-10-31), shipped with 1.1.01; user kept
  `minFoodsToActivate = 10` (line 40), `resetOnDeath = true` (57), `queueSize = 256` (72); new-world values PASSED on
  the scratch server (SQ-CF-01).
- 2026-10-27: the 11th benefit `attribute,generic.speed,2` (not a real attribute, dropped silently **(jar)**) is now
  `attribute,generic.movement_speed,0.01` (additive on base 0.1, +10 %) in both copies.

### Now (left)
- `complexityUnparsed` (line 120) still has the 4 `largemeals:*` entries (`sweet_berry_custard,5`,
  `pufferfish_broth,5`, `mushroom_pot_pie,5`, `hearty_lunch,6`); the mod is not installed, `ComplexityParser` warns
  `Item is not food` at load.
- Existing worlds keep their own `serverconfig/` copy: `saves/Workspace`, `WorldA` to `WorldG` still have
  `generic.speed,2` (checked 2026-10-31); `saves/New World` does not.
- Tier bonuses (`benefitsUnparsed`) still WIP, belongs to #12.

### Build
1. Drop the 4 `largemeals` lines from `complexityUnparsed` in both copies (defaultconfigs and configs_edited).
2. Existing worlds: copy the file into each world's `serverconfig/` by hand (world closed), at least the dedicated
   server's world; note it in the deploy notes. defaultconfigs seeds only worlds without a copy.

### Decisions
- Copy into existing worlds: recommend yes for the server world and Workspace; the test worlds don't matter.

### Gotchas
- A server config is synced to clients on login, so clients need nothing.
- The benefit list must have exactly one entry per threshold or the extra entries are ignored.

### Tests
- FO-32 (food.md 7): new single-player world, `serverconfig/solapplepie-server.toml` equals the pack file; after
  step 1 no `Item is not food: largemeals` lines in the log.

---

## types-and-tuning #3: Golden food changes vanilla values
Status **In Progress** / Work data / `food/types-and-tuning #3` (decision D2)

### Goal
Golden Foods! stops changing vanilla values: enchanted foods 4 / 1.2 like the vanilla notch apple, golden carrot back
to vanilla.

### Now (2026-10-30 dump; nothing in the pack writes these values yet)
- Golden Foods replaces three vanilla items through a `minecraft` DeferredRegister **(jar, `GFItems`)**.
  `minecraft:enchanted_golden_apple` is **8 / 8.0** (128 saturation points), Regen II 20 s, Resistance I 300 s, Fire
  Res I 300 s, Absorption IV 120 s; vanilla is 4 / 1.2. `minecraft:golden_carrot` is **4 / 1.2 + Regen II 5 s,
  Absorption I 120 s** (vanilla 6 / 1.2, no effects). `minecraft:golden_apple` 4 / 1.2 as vanilla.
- The 23 `goldenfood:enchanted_golden_*` foods are 8 / 8.0 with the notch effects (dump: Enchanted Golden Cookie the
  same); the 23 golden ones 4 / 1.2 with the golden apple effects.
- Since 2026-10-27 (#15) every golden food also has Nourishment 120 s, every enchanted one 240 s, set by CD
  comfort_nourishment.js (rule 4); keep it.
- Stack sizes: golden and enchanted foods 16 (`gen_stack_sizes.py` golden rule).
- SoL: enchanted golden apple 10 and golden carrot 2 from the toml list (#4); the 23 enchanted foods by formula.
- Generated cans copy the source: `creativeengineering:canned_enchanted_golden_apple` is 2 / 8.0 per serving
  (32 points) in the dump.
- Files: the pack's `startup_scripts/cooking/food_values.js` holds only effect swaps now; whole-food value copies are
  CD `kubejs_startup/creativeengineering/food_value_copies.js` with rows in `minecraft/food_values.js`. Recipes for
  golden foods: CD `minecraft/golden.js`, `create/golden.js`, `immersiveengineering/golden.js`.

### Build
1. New CD startup script `development/creativeDelight/src/main/resources/kubejs_startup/goldenfood/golden_values.js`
   (Creative Delight tree by mod; when #1 lands, its entries move into food_tuning.json `items`):
   ```js
   ItemEvents.modification(e => {
     var i = global.item
     var types = JSON.parse(String(JsonIO.readString('kubejs/generation_scripts/food/food_types.json'))).types
     types['golden/enchanted'].forEach(key => {
       if (key === 'enchanted_golden_moss_clump') return
       e.modify(i[key], item => item.setFoodProperties(f => { f.hunger(4); f.saturation(1.2) }))
     })
     e.modify(i.golden_carrot, item => item.setFoodProperties(f => {
       f.hunger(6); f.saturation(1.2); f.alwaysEdible(false)
       f.removeEffect(regen); f.removeEffect(absorption)   // MobEffect objects via ForgeRegistries.MOB_EFFECTS
     }))
   })
   ```
   The consumer form starts from the item's own properties and keeps the effects; comfort_nourishment.js (priority
   -100) runs later and adds Nourishment back, so do not strip it here. `removeEffect` takes a `MobEffect`; or build
   a fresh `FoodProperties$Builder` as `startup_scripts/cooking/alcohol_drinks.js` does.
2. SoL: leave the toml values until #1C.
3. Export round, then `gen_can_types.py`: the enchanted cans become 1 / 1.2 per serving, the golden carrot can follows.
4. food.md 4.2 finding 3 -> fixed; can_parity.md 2.17 note.

### Decisions
- D2 enchanted foods: recommend 4 / 1.2 (vanilla) and keep the notch effects.
- Golden carrot: recommend fully vanilla (6 / 1.2, no effects, not always edible): it costs 8 nuggets, a ninth of a
  golden apple, so golden-apple effects on it are cheap healing. Alternative: 6 / 1.2 and keep the effects.
- Stack size: leave the policy's 16.

### Gotchas
- `minecraft:golden_carrot` and `minecraft:enchanted_golden_apple` are Golden Foods' objects; `e.modify` on the
  vanilla id reaches them.
- The golden cans' effects follow the source; recheck them after this row.

### Tests
- FO-34 (proposed): AppleSkin on an Enchanted Golden Apple and an Enchanted Golden Cookie: 4 hunger, saturation 9.6;
  Golden Carrot 6 hunger, no Regeneration or Absorption (Nourishment 120 s stays), not edible at full hunger.

---

## types-and-tuning #6: Vanilla egg -> `#forge:eggs` in recipes
Status **In Progress** / Work KubeJS / `food/types-and-tuning #6`

### Goal
Every food recipe that takes the vanilla egg takes any egg (cooking plan: "any egg to fried egg").

### Now (rescan of the 2026-10-30 recipe dump, `kubejs/exported/food/recipes.json`, 2026-10-31)
`i.eggs` = `#forge:eggs` (item_dict.js 1939), `i.minecraft_egg` = `minecraft:egg` (2332). 75 recipes already take
`#forge:eggs`. Done since 2026-10-21: `fruitsdelight:fig_pudding` (CD `fruitsdelight/recipes.js` line 22
replaceInput); `farmersrespite:coffee_cake` no longer takes an egg (the pack's oven and `_manual_only` routes build it
from a cake base); Brewin's two quiche recipes are removed (CD `brewinandchewin/recipes.js` lines 57-58).

Recipes that still take `minecraft:egg` as an item (dump, IE recipes and 7 unreadable ones not covered):

| Recipe id | Type | Where | Change |
|---|---|---|---|
| `creativedelight:smelting/egg_to_fried_egg_by_smelting`, `smoking/...`, `campfirecooking/...` | furnace / smoker / campfire | CD [farmersdelight/recipes.js](../../kubejs/data/creativedelight/kubejs/farmersdelight/recipes.js) lines 367-369, input `i.minecraft_egg` | edit to `i.eggs` |
| `creativeengineering:shaped/quiche_manual_only` | crafting shaped | CD [brewinandchewin/recipes.js](../../kubejs/data/creativedelight/kubejs/brewinandchewin/recipes.js) line 62, key `e: i.minecraft_egg` (its assembly twin already deploys `i.eggs`, lines 69-70) | edit to `i.eggs` |
| `rusticdelight:fried_egg_from_cooking_oil` (+ `sliceanddice:cooking/rusticdelight/...` copy) | `farmersdelight:cooking` | Rustic Delight jar | replaceInput (the Slice & Dice copy follows) |
| `create_factory:cake_paste` | `create:mixing` | Create: Factory jar | replaceInput |
| `create_deep_dark:compacting_echo_cake_base` | `create:compacting` | Create Deep Dark jar | replaceInput |
| `hostiledelight:gpudrecp` (Ghast Pudding) | crafting shapeless | Hostile Delight jar (new in the list) | replaceInput |
| `vintagedelight:fermenting/pickled_egg_from_fermenting` | `vintagedelight:fermenting` | Vintage port (new in the list) | decision |
| `incubation:chicken_egg_crate` | crafting shaped | Incubation | **no** (storage block) |
| `mca:potion_of_feminity`, `potion_of_masculinity` | crafting shaped | MCA | no (not food) |

### Build
1. CD farmersdelight/recipes.js 367-369 and brewinandchewin/recipes.js 62: `i.minecraft_egg` -> `i.eggs`.
   **Edit the source, don't replaceInput:** `NCH.replaceInput` (neoCraftHelper.js 635) walks only data-loaded
   recipes unless `includeAdded`, and these are script-added.
2. Mod recipes, in the CD tree of each mod (`kubejs/data/creativedelight/kubejs/<mod>/recipes.js`, as fruitsdelight
   does): `R.replaceInput({ id: '<id>' }, i.minecraft_egg, i.eggs, e)` for the Rustic, Create Factory, Create Deep
   Dark and Hostile Delight ids. If several share a file, `R.bulk('replaceInput', [...])` (neoCraftHelper.js 760).
3. Pickled egg: per the decision, a replaceInput in CD `vintagedelight/recipes.js` (the port is ours, so a data edit
   in the port is the other option).
4. Next `/dumpfood`: rerun the scan (`'minecraft:egg' in json.dumps(r['in'])` over `recipes.json`). food.md 3.8 ->
   built, with this table.

### Ids and data
Ids from the 2026-10-30 recipe dump. Recipe types: smelting, smoking, campfire, shaped and shapeless crafting,
`farmersdelight:cooking`, `create:mixing`, `create:compacting`, `vintagedelight:fermenting`.

### Decisions
- Turtle and parrot eggs in `#forge:eggs`: recommend keep them. If not wanted: `global.tags.remove` in a tag script,
  which also changes the egg stack rule.
- Pickled egg: recommend any egg (a pickled duck egg is real food); Vintage's fermenting type must accept a tag
  ingredient (check the port's serializer reads `tag`).

### Gotchas
- `replaceInput` needs KubeJS to understand the recipe type: crafting and Create types are native,
  `farmersdelight:cooking` comes from KubeJS Delight. If JEI still shows the vanilla egg, remove and re-add through
  `R.fdCooking` / `R.createMixing` / `R.createCompacting` (`_manual_only` for a pot recipe whose Slice & Dice copy
  must not double, food.md 3.7).
- The fried egg is the main `#forge:cooked_eggs` source; this widens it to every egg.

### Tests
- FO-35 (proposed): JEI uses of Duck Egg and Emu Egg show Fried Egg (furnace, smoker, campfire, cooking-oil pot), the
  quiche, cake paste (mixer), echo cake base (compacting), ghast pudding; Egg still shows the chicken egg crate, Duck
  Egg doesn't.

---

## types-and-tuning #9: Cultivated meat cultures have no nutrition
Status **In Progress** / Work KubeJS / `food/types-and-tuning #9`

### Goal
The 7 cultures are (poor) food; the Cooking Pot grows them less efficiently than the mixer.

### Done
- Recipes ([cultivated_meat.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/cultivated_meat.js), CD,
  85 lines): syringe; syringe + culture base + slime -> 1 culture (29-35); heated mixing 1 culture + 250 mB diluted
  bonemeal + 250 mB plant oil -> 3 (37-43); cutting board -> raw meat / fish slices (57 on); blocks 9 <-> 1.
- 2026-10-27: Cooking Pot route, culture + bone meal + cooking oil -> **3** (lines 45-54, `<animal>_culture_cooking`,
  0.35 xp, 200 ticks); pack_hygiene.md PH-34, scratch PH-25. Cultures moved into CD and reserved for a future Creative
  Cultivation mod (creative_delight.md 29.5).

### Now (left)
- Items: CD [cultures.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cultures.js)
  lines 20-29, plain `e.create(i.culture_*)` items, no `.food()` (header line 11: "Plain items, no .food()").
- Not in foods.json (not edible), not typed in food_types.json, not in can_parity.md.
- The pot gives the same 1 -> 3 as the mixer, so the row's "inefficient cultivation with the pot" is not met.

### Build
1. cultures.js: `.food(f => f.hunger(1).saturation(0.1).meat().effect('minecraft:hunger', 600, 0, 0.8))` on the five
   meat cultures, the same without `.meat()` on cod / salmon. Update the header comment.
2. Pot efficiency (decision): in cultivated_meat.js lines 48-54 change the output to `Item.of(i.culture_*, 2)`, or keep
   3 and lengthen the time to 400 ticks.
3. Export round: `food_catalog.py` types the cultures (`hazard` per the decision: add the keys to food_types.json
   `hazard`), `tools/food_families.py` gives them a family or leaves them in 3.3.
4. can_parity.md: add the 7 as `/` rows (no can).

### Decisions
- Values: recommend 1 / 0.1 + 80 % Hunger 30 s, type `hazard` (so tuning doesn't band it as raw meat).
- Pot vs mixer: recommend 2 out of the pot (the mixer's 3 needs two fluids and heat, the pot only items).

### Gotchas
- Startup change: restart. SoL complexity by formula is tiny (0.02), fine.
- comfort_nourishment.js strips Nourishment from every food not in its lists; the cultures have none, no conflict.

### Tests
- **RC-new1** (recipes_cooking.md prefix, proposed; the 2026-10-21 "RC-19" is taken by grinding mirrors): eat a
  Beef Culture -> 1 hunger, usually Hunger; a heated Cooking Pot with a culture + bone meal + cooking oil -> 2 cultures; the mixer still gives 3.

---

## types-and-tuning #1: Food tuning system
Status **Planned** / Work KubeJS / data / tool / `food/types-and-tuning #1` (D1 to D6)

### Goal
Every food's hunger, saturation, flags, effects and Spice of Life value follow its type (31 types,
food_types.json), with named exceptions; cans follow their source. Since 2026-10-23 the families of row #13 are the
finer grouping: `types` in food_tuning.json may also be keyed by family (`family:soup/stew`), a family entry beating a
type entry.

### Now
- Design: food.md 3.5 (one config, startup apply, server SoL map, starting bands per type, canned parity rule).
  Decisions D1 to D6 (4.3) are open.
- Pieces that exist: types and tags (CD `kubejs/data/creativedelight/kubejs/creativeengineering/food_type_tags.js`;
  families and their tags, row #13), `tools/food_catalog.py` (classifier, `--check`, catalog,
  `kubejs/exported/food/catalog.json` per food with values, effects, code effects, SoL now and by formula), three
  value writers already at startup: CD cans.js (generated cans + the Canned Goods overrides, `ceCanFood` sets flags
  both ways since 2026-10-21), CD food_value_copies.js (whole-food copies, 1 row: glistering melon slice), the pack's
  alcohol_drinks.js (drinks: 1 / 0, Intoxication, Nausea), and CD comfort_nourishment.js (priority -100: owns Comfort
  and Nourishment on every food, row #15).
- Canned parity has moved: it is no longer the 3.5 rule but `tools/gen_can_types.py` SECTIONS (per-section
  multipliers and servings, can_parity.md 3.1, 4.2), owned by `cans/canning #1` / `#3`. What #1 still owes cans:
  tuned source values (they flow in on the next `gen_can_types.py` run) and the SoL value per can (D5).
- SoL internals **(jar)**: `ConfigHandler.complexityMap` is a public static `Map<FoodInstance, Double>`, replaced at
  `ServerStartingEvent` from the config; `FoodList.getComplexity(FoodInstance)` reads the map first, then the formula
  `q = (h + h*mod)/2`, `q/5` below 5, else `1 + 4 log10(q - 4)` (times `defaultContribution`). Clients get the map
  on login (`ConfigHandler.syncConfig(player)`, `ConfigMessage`). The map accepts non-food items (the parser refuses
  them, the map doesn't), which #5 uses.

### Build
**A. Config and report (tool, no game):**
1. `kubejs/generation_scripts/food/food_tuning.json`:
   ```json
   { "_readme": ["..."],
     "types": { "soup": { "hunger": [6, 14], "modifier": [0.6, 0.8], "sol": 1.5,
                          "effects": [{ "effect": "farmersdelight:comfort", "secondsPerHunger": 15 }] }, ... },
     "items": { "carrot_soup": { "hunger": 10 }, "enchanted_golden_apple": { "hunger": 4, "modifier": 1.2, "sol": 2 },
                "cantaloupe_slice": { "copy": "pumpkin_slice" }, "beer": { "exempt": true } },
     "rules": { "maxModifier": 1.2, "alwaysEdibleTypes": ["golden", "golden/enchanted", "drink", "sweet"] } }
   ```
   Keys are item_dict keys (never ids). `types` inherit down the `/` tree (a `raw/meat/sliced` entry overrides
   `raw/meat`). `drink/alcohol` is `exempt` as a type (alcohol_drinks.js owns it). Fold food_values.js's one copy and
   #3's golden values into `items`, then delete those files' entries.
2. `tools/food_catalog.py --report`: per type, every food outside its band (value, band, distance) and the hard-rule
   breaks (modifier > 1.2 outside golden, always-edible outside the listed types); `--report --seed` writes the
   accepted ones into `items` as `exempt` (D1 review-and-exempt). Add SoL columns: complexity now, by formula after
   tuning, and the per-type value.
3. Add a diet simulator to the report (the table in food.md 2.3: k foods of mean complexity c, steady state
   `c (k - k(k-1)/(2*endDecay))`, tiers reached against the toml thresholds) so #12's ladder can be checked offline.

**B. Apply values (startup):**
4. CD `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/food_tuning.js` on
   `ItemEvents.modification`. Read food_tuning.json and
   food_types.json with `JsonIO.readString` (as cans.js does). For each typed key: resolve the type chain, merge
   type defaults <- item overrides, skip `exempt`, and write with a fresh vanilla builder so flags are explicit
   (alcohol_drinks.js lines 61-82 is the pattern; the consumer form would keep old flags, see "State"):
   ```js
   var B = Java.loadClass('net.minecraft.world.food.FoodProperties$Builder')
   e.modify(id, item => {
     var old = item.getFoodProperties(); if (!old) return
     var b = new B().nutrition(v.hunger).saturationMod(v.modifier)
     if (v.meat) b.meat(); if (v.always) b.alwaysEat(); if (v.fast) b.fast()
     keepOrReplaceEffects(old, v, b)   // copy old pairs unless v.effects replaces them
     item.setFoodProperties(b.build())
   })
   ```
   Only clamp when D1 says snap; with review-and-exempt the script applies `items` values and the hard rules only,
   and the bands stay a report. Log `[food tuning] N foods changed, M exempt, K skipped`.
5. Order with the other writers: cans.js re-values the Canned Goods cans and must stay the only writer for
   `canned` (skip the type here). alcohol (exempt). food_value_copies.js rows fold into `items`.
   comfort_nourishment.js runs last (priority -100) and owns Comfort / Nourishment: the tuning never writes those two
   effects (keep them from `old` when rebuilding). All are startup `ItemEvents.modification`
   handlers, so whichever registers last wins on a shared item: keep the sets disjoint instead of relying on order.
6. tools/build_deploy.py KUBEJS_CLASSES (the `generation_scripts/food/` rows, lines 265-273): add
   `('generation_scripts/food/food_tuning.json', 'runtime')`, or the deploy preflight fails.

**C. Spice of Life values (server):**
7. CD `kubejs/data/creativedelight/kubejs/solapplepie/sol_complexity.js` (server):
   ```js
   var SOL = { CH: Java.loadClass('com.tarinoita.solsweetpotato.ConfigHandler'),
               FI: Java.loadClass('com.tarinoita.solsweetpotato.tracking.FoodInstance') }
   function ceFillSol(server) {
     var map = SOL.CH.complexityMap   // already holds the toml list; ours overrides per item
     each typed key -> map.put(new SOL.FI(Item.of(id).item), value)
     server.players.forEach(p => SOL.CH.syncConfig(p))
   }
   ServerEvents.loaded(e => ceFillSol(e.server))       // after SoL's ServerStartingEvent rebuild
   if (Utils.server) ceFillSol(Utils.server)            // /reload re-runs scripts; refill
   ```
   The dump already loads these classes (`kubejs/server_scripts/_lib/dump_food.js` lines 64-66), so the class filter allows them. Remove the
   per-item complexity lines from the toml (#4) once the script owns them, or the two disagree on restart order
   only for a moment (the script runs last).
8. Cans (D5): each can's value = its source's per-type SoL value (from food_types.json `canned`), so a can no longer
   beats its source; D5 stays "a can counts as a separate food" (no way to merge in the mod).

**D. Content passes:** #8 soups first (small, shows the loop works), then the rest by type, each as a reviewed
`items` diff. Workflow: `/dumpfood` -> `food_catalog.py --report` -> edit food_tuning.json -> restart (values) or
`/reload` (SoL only) -> `/dumpfood` -> compare -> `gen_can_types.py` (cans follow) -> restart.

### Ids and data
Effects: `farmersdelight:comfort`, `farmersdelight:nourishment`, `minecraft:hunger`. food_tuning.json names them by
effect_dict key (`global.effect`, `startup_scripts/_lib/effect_dict.js`): `ef.hunger` exists (line 38); the
Farmer's Delight ones are not in the dict yet (checked 2026-10-31): add `ef.comfort = 'farmersdelight:comfort'` and
`ef.nourishment = 'farmersdelight:nourishment'` under a new "Farmer's Delight" heading. Type tag keys
`i.t_food_*` (item_dict, generated block). SoL classes and fields verified in the jar.

### Decisions (recommendations)
- D1: review-and-exempt (as food.md says). Hard rules applied, bands reported.
- D2: yes (row #3).
- D3: take per-type complexities; benefits trimmed and the ladder rescaled under #12 (one decision with #12).
- D4: moot for golden cans once row #2 hides the Canned Goods golden cans; keep the hazard cans' code effects
  (pufferfish, spider eye, poisonous potato, rotten flesh: that is their point).
- D5: accept; value cans at the source's SoL.
- D6: 30% Hunger 30 s on `raw/meat` and `raw/meat/sliced` (Farmer's Delight's own raw-meat rule); not on raw fish
  (sushi and the fish cans use it raw). Pairs with `cans/canning #4` (raw can effect passthrough).

### Gotchas
- A startup change needs a restart and then a new `/dumpfood` before the generator: two restarts per value round.
- Spice of Life reads food properties live for the formula, so SoL values left to the formula move with the tuning.
- FD's vanilla-soup effects come from `config/farmersdelight-common.toml` (event-applied), not food properties: the
  tuning must not add a second Comfort to `minecraft:*_stew` / `*_soup`.
- New `item_dict` keys need a restart before the tag and tuning scripts see them.

### Tests
- FO-37 log: `[food tuning] ... changed` with no warnings; FO-38 AppleSkin on one food per type matches
  food_tuning.json; FO-39 `/dumpfood` + `food_catalog.py --report` shows no hard-rule breaks; FO-40 SoL tooltip on a
  meal shows the per-type value (client got the synced map).

---

## types-and-tuning #8: Soup and stew values from their ingredients
Status **In Progress** (nothing built; checked 2026-10-31) / Work data / `food/types-and-tuning #8` (part of #1)

### Goal
Each soup's hunger and saturation follow what goes into it; outliers marked first, then changed.

### Now
31 foods in `soup` (2026-10-20 dump); the families split them finer: `soup/soup` 36, `soup/stew` 30 members
(food_families.json, 2026-10-31). Main recipes from the jars (table from 2026-10-21), with known ingredient hunger (catalog values; tags and
non-food items count 0):

| Soup | Now h / mod | Pot recipe ingredients | Known hunger |
|---|---|---|---:|
| Beef Stew (FD) | 12 / 0.8 | `#forge:raw_beef`, carrot, potato | 7 |
| Chicken Soup (FD) | 14 / 0.75 | raw chicken, carrot, `#forge:salad_ingredients`, `#forge:vegetables` | ~9 |
| Vegetable Soup (FD) | 12 / 0.8 | carrot, potato, beetroot, salad ingredient | ~7 |
| Fish Stew (FD) | 12 / 0.8 | raw fish, tomato sauce (4), onion | ~8 |
| Pumpkin Soup (FD) | 14 / 0.75 | 3 pumpkin slices, salad ingredient, raw pork, milk | ~11 |
| Noodle Soup (FD) | 14 / 0.75 | pasta, cooked egg, dried kelp, raw pork | ~10 |
| Baked Cod Stew (FD) | 14 / 0.75 | cod, potato, egg, tomato (pack: + bone broth, shapeless) | ~6 |
| Rabbit Stew (vanilla, FD pot) | 10 / 0.6 | baked potato, rabbit, carrot, mushroom | 11 |
| Mushroom / Beetroot Soup | 6 / 0.6 | 2 mushrooms / 3 beetroot | 0 / 3 |
| Carrot Soup (More Delight) | 12 / 0.8 | milk, 2 carrots | 6 |
| Acacia Blossom Soup (Alex's Delight) | 12 / 0.8 | 2 onions, 2 raw pasta, acacia blossom | 4 |
| Corn Soup (Corn Delight) | 10 / 0.9 | corn, salad ingredient, any raw meat or mushroom, milk | ~7 |
| Sinigang (Delightful) | 15 / 0.9 | raw fish, tomato sauce, onion, raw pork, ginger, citrus | ~13 |
| Chorus / Endermite Stew (End's Delight, 2 bowls each) | 4 / 0.5, 4 / 0.4 | 4 chorus fruit + flower / enderman loot + mite crust | 4 / 1 |
| Bayberry Soup (Fruits Delight) | 1 / 0.2, drink, bottle | 4 bayberries | ~8 |
| Sopa de Macaco (Alex's Mobs) | 5 / 0.4 | banana peel, mushroom, bone | 0 |

The rest (adzuki stew, bell pepper soup, blazing chili, bone broth, cavern explorer stew, creamy onion soup, fig chicken
stew, kangaroo stew, miso, strider moss stew, suspicious stew) need tag resolution: do them with the tool.

### Build
1. food_catalog.py `--report soups`: for every soup recipe in `recipes.json`, sum the ingredients' hunger and
   saturation points, resolving a tag to the median of its members from `kubejs/exported/tags/item_tags.json`, and
   giving non-food ingredients a virtual value (mushroom 1, milk 2, bone 0) from a small table in food_tuning.json
   (`"ingredientValues"`). Print soup value, ingredient sum, ratio; flag ratio outside 1.2-1.8.
2. Proposed rule (to confirm with the report): hunger = round(1.5 x ingredient hunger), clamped to the soup band
   6-14; modifier from the band by tier. Today's ratios sit near 1.5 for FD's core soups (beef 12/7, fish 12/8);
   outliers high: carrot soup, acacia blossom soup, baked cod stew; low: rabbit stew, bayberry soup.
3. Write the accepted values into food_tuning.json `items`; mark deliberate ones `exempt` (End's Delight stews are
   drink-animation snacks; bayberry soup is a bottle drink: move it to `drink` in food_types.json instead).

### Decisions
- Rule multiplier: recommend 1.5, with the band clamp.
- Bayberry Soup: recommend re-type as `drink` (bottle, drink animation) and leave its values.
- Pack recipe changes (bone broth in shapeless soups, `dishes/ingredients #4`) change the sums: run the report after
  those land too.

### Gotchas
- Effects are not part of this row: Comfort / Nourishment on soups come from comfort_nourishment.js (row #15),
  whose input-hunger rule reads the same ingredients; rerun `gen_comfort_nourishment.py` after soup values change.
- Two recipes for one soup (pot vs shapeless): use the pot recipe (the "full" one).
- Soup cans follow on the next `gen_can_types.py` run (section `soup`, mult 1.25).

### Tests
- FO-41: `food_catalog.py --report soups` prints no flagged soup after the change; AppleSkin on Carrot Soup shows the
  new value.

---

## types-and-tuning #12: Spice of Life much slower
Status **Planned** / Work config / KubeJS / `food/types-and-tuning #12`

### Goal
Spice of Life takes about twice as long; a shorter chain (about 12 tiers) that ends at triple max health (60, i.e.
+40 health); low-level effects spread along it instead of Strength III / Haste II at the top.

### Now
The pack toml of #4 (2026-10-31): 17 thresholds `2 ... 100`, the mod's benefits (the 11th fixed to movement speed),
`queueSize = 256`, `endDecay = 32`, `startDecay = 0`, `minFoodsToActivate = 10`, `resetOnDeath = true` (the last
three kept by the user 2026-10-22). food.md 3.5's ladder (1 to 26) was sized for the per-type complexities with queue 32.

### Build
1. In the #4 toml: keep `queueSize = 256` (user), set `endDecay = 64` (twice as many distinct meals needed), keep
   `startDecay = 0`; the decay window, not the queue, sets the pace while the queue is longer than `endDecay`. With food.md 3.5's per-type values (mean complexity about 1.5 for a varied kitchen diet, 2.5 for
   meals), steady-state diversity is `c (k - k(k-1)/128)`: FD kitchen 12 foods about 17, chef 32 foods about 40,
   64 distinct foods about 53.
2. Thresholds (12): `2, 4, 6, 8, 11, 14, 18, 22, 27, 32, 38, 45`. That gives: steak only 0, vanilla 5 foods 1,
   FD kitchen 6, chef 11, 64 foods 12, versus 11 / 17 for the kitchen / chef on the 3.5 ladder: about half the pace.
3. Benefits (12, +40 max health in total, low-level effects; `effect,<id>,0` is level I):
   ```
   attribute,generic.max_health,2
   attribute,generic.max_health,2;effect,haste,0
   attribute,generic.max_health,4
   attribute,generic.max_health,4;effect,luck,0
   attribute,generic.max_health,4
   attribute,generic.max_health,4;effect,speed,0
   attribute,generic.max_health,4
   attribute,generic.max_health,4;effect,strength,0
   attribute,generic.max_health,4;attribute,generic.armor_toughness,1
   attribute,generic.max_health,4;attribute,generic.knockback_resistance,0.25
   attribute,generic.max_health,2;effect,resistance,0
   attribute,generic.max_health,2;effect,regeneration,0
   ```
4. Check it offline with #1's diet simulator before shipping; then copy into existing worlds' serverconfig.

### Decisions
- Chain length and the effect list (with D3): recommend the 12 above. Permanent Regeneration I at the top is strong
  (benefit effects are kept on constantly); alternative: drop it and give the last tier +4 health instead.
- `resetOnDeath`: the user kept true on 2026-10-22; with +40 health on the line a death reset is harsh, so ask again
  when the ladder ships (recommend false).

### Gotchas
- Raising `queueSize` keeps old food lists (they only grow); lowering it later trims them on the next eat
  (`FoodList.addFood` drops entries aged past the size) **(jar)**.
- Benefits are recomputed at login (`BenefitsHandler.onPlayerLogin`) and on every eat **(jar)**: after a config
  change, rejoin.
- Max-health benefits rescale current health (`AttributeBenefit.applyTo`, unless First Aid is loaded) **(jar)**.

### Tests
- FO-42 (proposed): fresh player, eat 10 then 16 different FD meals: no tier before the 10th food
  (`minFoodsToActivate`), 2 or 3 tiers after; the food book lists the 12 benefits.

---

## types-and-tuning #13: Food families: one set of cooking rules per family
Status **In Progress** / Work tool / docs (then KubeJS per rule) / `food/types-and-tuning #13`

### Goal
Every food sits in a family, and each family has one set of rules (cannable, placeable, recipe shape by station,
values); the members are brought in line.

### Done
- 2026-10-23: [tools/food_families.py](../../tools/food_families.py) (`FAMILIES`, `classify()`, `RULES`), writeup
  [food_families.md](../food_families.md) 3.1 to 3.4 (generated), findings F1 to F9 (4.3).
- 2026-10-25: produce families decided and applied (4.4: fruit, veggie, meat, fish, egg, dairy): recipes, coaster tag,
  Display Delight dish blocks, cans; recipe helpers (4.5). FF-01 to FF-10; SQ-FF-01 (rule columns read full after
  `/dumpall`) **PASSED 2026-10-31** in r1031.
- 2026-10-26: `food_families.json` and `#creativeengineering:food_family/<path>` tags (CD seasoning_tags.js), used by
  the seasonings.
- 2026-10-27: baked families restructured and applied (4.6): pizza, quiche, lasagna, dessert; cookie, cake, pie,
  muffin, quiche members. FF-11 to FF-14.
- Counts (2026-10-27 tree): 1416 items, 33 families, 76 subfamilies, 22 without a family, 64 skipped.

### Now (left)
- Decisions open (4.2): **D1** every Decision entry of 3.1 for the families not yet decided (soup, meal, sandwich,
  roll, skewer, stuffed, salad, drink, alcohol, preserve, snack, candy, frozen, golden, feast, can ...); **D2** the
  family list (`meal/plate` catch-all, drinks by kind, sweet and savory pies together); **D3** sandwich bread shapes
  (Chunkwich, Zucchini Sandwich, Kangaroo Burger); **D4** the foods without a family (3.3, e.g. Crab Bars).
- Findings still open (4.3): F1 heatless `_manual_only` crafts missing for 14 pot soups and 26 pot meals; F2 stews
  with no pot recipe; F3 cakes outside Oven / Crafting; F5 burgers with shapeless recipes; F6 Sweet Potato Pie can
  route; F7 6 pie slices with no source; F8 can worksheet gaps; F9 pie / feast assemblies, drink shapes, tea and cake
  slice placement.
- Rule application for the undecided families: not started.

### Build
1. Owner pass over 4.2 D1 to D4 (the cost of each proposal is in 3.2's compliance counts).
2. Per decided family, one pass in the CD tree of each member's mod, with the family helpers of 4.5 (`global.CD.food`
   callers, `global.NCH` recipes, `global.tags` for placement tags); then `/dumpall` and
   `python3 tools/food_families.py` (or `/cegen run food_families`) until 3.2 reads full.
3. Suggested order (small to large): soup (F1, F2: the heatless crafts and pot recipes), sandwich and roll (D3, F5),
   skewer (with `dishes/meals #13`, #46 and row #19), drinks (F9, `food/drinks #8`), the rest.
4. Values per family land through #1 (`types` keyed by family), not in this row.

### Decisions
- Recommend D2 as drafted (keep `meal/plate` as the catch-all until a split earns its keep), D3 as the audit read the
  sprites, D4 keep the 22 out except Crab Bars -> `snack` (also unblocks `food/seasoning #4`).

### Gotchas
- 3.2 and 3.4 are generated from the dumps: a rule pass only shows after `/dumpall`.
- Family tags move when the tool reruns; the seasonable tags follow on the next restart.

### Tests
- FF-01 to FF-14 (food_families.md 7, in game); per family pass, **FF-new1** (proposed): the family's 3.2 row reads
  full after `/dumpall`.

---

## types-and-tuning #16: Spice of Life carrot for the food dictionary only
Status **Planned** / Work KubeJS / data / `food/types-and-tuning #16`

### Goal
Add a "Spice of Life carrot" item only so the food dictionary lists it: no recipe, loot or other way to obtain it
(decided 2026-10-29: add it).

### Now
- The installed Spice of Life (`solapplepie-1.19.2-2.2.0`) registers only `food_book`, `lunchbox`,
  `golden_lunchbox`, `lunchbag` (jar item models, 2026-10-31); no carrot item in it or in the display-name dump. The
  "carrot" is presumably the mascot of Spice of Life: Carrot Edition, the mod Apple Pie is forked from. **Unverified:**
  which "food dictionary": SoL's Food Book lists only foods the player has eaten, so an unobtainable item never shows
  there; the starter kit's planned food dictionary (`items/starter-kit #1`) or JEI are the other readings.
- Pack items for the food system are CD startup items under `creativeengineering:` (e.g. powdered sugar,
  `kubejs_startup/creativeengineering/baking_ingredients.js`).

### Build
1. Item `creativeengineering:spice_of_life_carrot` (dict key `i.spice_of_life_carrot`, item_dict.js next to the
   solapplepie block at 12753) in a CD startup script (`kubejs_startup/solapplepie/items.js`), `.food()` with vanilla
   carrot values so it is a food, texture a recolour of the carrot (textureswapper).
2. No recipe, no loot, no trade. Keep it out of `global.removedItems` (that would hide it).
3. food_types.json: `excluded` (reason "display only"), so tuning, cans and stack rules skip it; food_families.py skip
   list likewise.

### Decisions
- **Which dictionary** (premise check): recommend asking the owner; if it is JEI or the starter-kit book, step 1 is
  enough; if it is SoL's Food Book, the item cannot appear without being eaten, so the row is moot.

### Tests
- **FO-new5** (proposed): JEI search "Spice of Life" shows the carrot with no recipe; `/give` it and eat it: carrot
  values.

---

## types-and-tuning #17: Expand crated golden food
Status **Planned** / Work KubeJS / data / `food/types-and-tuning #17`

### Goal
Storage crates for more golden foods, like Quark's golden apple and golden carrot crates.

### Now
- Only `quark:golden_apple_crate`, `quark:golden_carrot_crate` (`i.golden_apple_crate`, `i.golden_carrot_crate`,
  item_dict.js 3156-3157), 9 <-> 1; left out of chest loot on purpose ([loot.md](../loot.md), crate split).
- Golden foods: 23 `goldenfood:golden_*` and 23 `goldenfood:enchanted_golden_*` (food_types.json `golden`,
  `golden/enchanted`), plus the pack's golden items (glistering melon slice).
- No pack-made crate block exists; pack blocks are KubeJS startup registries (pack `startup_scripts/items/*_blocks.js`,
  CD `creativeengineering/cultures.js` culture blocks as the 9-item storage pattern, hand-authored blockstate and
  models).

### Build
1. Pick the foods (decision). For each, a block `creativeengineering:golden_<food>_crate` in a new CD startup script
   `kubejs_startup/goldenfood/golden_crates.js` (dict keys `i.golden_<food>_crate`), wood material, axe tag, a
   texture from Quark's golden crate with the food's sprite (textureswapper).
2. Recipes in CD `kubejs/data/creativedelight/kubejs/goldenfood/crates.js`: `R.shaped` 9 -> crate and `R.shapeless`
   crate -> 9 (`global.NCH`), plus the storage-block tags with `global.tags`.
3. Stack 64; no compost, no loot.

### Decisions
- Which foods: recommend the raw-produce golden foods only (`golden_beetroot`, `golden_sweet_berries`,
  `golden_glow_berries`, `golden_melon_slice`, `golden_chorus_fruit`, `golden_dried_kelp`; food_types.json `golden`),
  not cooked dishes or the enchanted tier (enchanted foods stack to 16 and are rare).

### Tests
- **FO-new6** (proposed): 9 Golden Beetroots -> Golden Beetroot Crate -> 9 back; the crate places, breaks with an axe and
  drops itself.

---

## types-and-tuning #18: Other effects on high-value meals
Status **Planned** / Work KubeJS / `food/types-and-tuning #18`

### Goal
High-value meals get effects beyond Nourishment (today the only effect on them, and only on bowls).

### Now
- Row #15 (built 2026-10-27, [comfort_nourishment.md](../comfort_nourishment.md) 9.0, 10): bowls, mason jars,
  shulker bowls and copper cups get Nourishment (4+ inputs, input hunger 8+, output 10+) or Comfort; bottles lose
  every effect; every other food loses Nourishment; golden foods Nourishment 120 / 240 s. Generator
  `tools/gen_comfort_nourishment.py`, output `generation_scripts/food/comfort_nourishment.json`, applier CD
  `comfort_nourishment.js` (priority -100).
- Meals' other effects today are their mods' own (e.g. FD's), left as they are by the applier.

### Build
1. Add a third pass to `gen_comfort_nourishment.py` (or a sibling `gen_meal_effects.py` writing
   `meal_effects.json`): for foods the rule gives Nourishment (high-value), one extra effect chosen by family from a
   table in the tool, e.g. `meal/pasta` Haste I, `fish/*` dishes Water Breathing, `meal/plate` roasts Strength I,
   spicy dishes (`i.t_hot_spices` = `#delightful:hot_spices` inputs) Fire Resistance, `feast/serving` Regeneration I; 60 to 120 s.
2. Applier: extend comfort_nourishment.js to add the `extra` list (or a second CD script after it, priority -101).
3. Keep the mod's own effects; add only where the food has none besides Comfort / Nourishment.
4. `gen_can_types.py` picks the effects up for cans (it already reads comfort_nourishment.json for the source).

### Decisions
- **Which effects, which meals** (TODO): recommend one mild effect per family as in step 1, short, level I, only on
  foods that already pass the Nourishment test.
- Interaction with BBQ sauces (`food/seasoning #11` transforms): sauces rewrite these effects too; fine.

### Gotchas
- SoL benefits also grant effects (#12): avoid giving meals the same effects the ladder hands out permanently.

### Tests
- **CNS-new1** (proposed): AppleSkin / tooltip on a high-value pasta shows Nourishment plus Haste; a low-value bowl
  shows only Comfort; `[comfort_nourishment]` log counts include the extra effects.

---

## types-and-tuning #19: Barbeque's Delight skewers: food and saturation investigation
Status **Planned** / Work KubeJS / data / `food/types-and-tuning #19`

### Goal
Tune the 10 BBQ skewers, their raw versions and the pack's skewers together: hunger, saturation, the `grilled` bonus,
raw-skewer downsides (worse food, poison chance; "nasty" hazard downside for chicken only).

### Now
- BBQ is not installed; the port (`largecontent/mod-additions #37`) is not built. Upstream values
  (barbeques_delight.md 3.1, saturation in points): beef 8 / 11.2, chicken 7 / 9.8, cod and salmon 7 / 14, lamb
  12 / 19.2, mushroom 6 / 4.8, pork sausage 8 / 11.2, potato 6 / 7.2, rabbit 10 / 16, vegetable 5 / 5; effects 50 %
  Strength, Regeneration, Resistance, Nourishment, 100 % Jump Boost. Raw skewers are not edible upstream.
- Pack skewer family `skewer` (food_families.json, 11 members, 2026-10-30 dump, hunger / modifier): Classic Corn Dog
  10 / 0.9, Corn Dog 8 / 0.9, Barbecue on a Stick 8 / 0.9, Seafood Skewer 11 / 0.7, Nether Skewer 7 / 0.5, Turnip
  Mutton Skewer 7 / 0.7, Cave Skewer 6 / 0.7, Baked Tentacle on a Stick 6 / 0.6, Tentacles on a Stick 4 / 0.2,
  Tentacle on a Stick 3 / 0.3, Crispy Skewer 2 / 0.2.
- `grilled` = +2 saturation points (`food/seasoning #13`). The skewer food type is `dishes/meals #13` (In Progress).

### Build
1. After the port: `/dumpfood`, then a skewer table in `food_catalog.py --report` (or #1's report filtered to the
   skewer family): hunger, saturation points, effects, ingredient hunger sum, per skewer.
2. Proposed rule: cooked skewer hunger = round(1.2 x ingredient hunger), saturation from the meat / fish family
   modifier; lamb and rabbit brought down to the beef line; raw skewer = half hunger, modifier 0.3; raw chicken
   skewer 30 % Hunger 30 s plus 10 % Poison 5 s (the hazard downside); other raw meat 30 % Hunger only.
3. Values through #1's food_tuning.json (`types` key `family:skewer`, `items` per skewer) or, before #1, a CD startup
   script in the port's tree (`kubejs_startup/barbequesdelight/skewer_values.js`).

### Decisions
- Target values (TODO): recommend step 2, checked against the report; a grilled skewer (+2) should edge out the same
  skewer crafted from cooked ingredients, not beat a bowl meal.

### Tests
- **FO-new7** (proposed): AppleSkin on the lamb and beef skewers shows the tuned values; eating a raw chicken skewer
  sometimes gives Poison, a raw beef skewer only Hunger. BBQ-01 to BBQ-21 (barbeques_delight.md 8) cover the port.

---

## Built, needs play

| Row | What | Writeup | Open |
|---|---|---|---|
| `food/types-and-tuning #2` | Canned Goods food cans take their family's values (`gen_can_types.py`), `ceCanFood` flags both ways, Canned Food and Hot Hot Hotpot removed (fixed 2026-10-21) | [food.md](../food.md) 4.2 findings 1-2 | S+ C P; SQ-OF-01 to -03 passed 2026-10-22; Oculemonade / golden-can code effects left as is (can_parity CP-21, CP-22) |
| `food/types-and-tuning #5` | Placed pie bites count as their slice, cakes via `placed_food_slices.json` (patcherMod `PlacedFoodSol`, 2026-10-22) | [food.md](../food.md) 4.2 finding 5 | S+ P; SQ-SOL-01 passed (FO-44); FO-43 (the bite) in game |
| `food/types-and-tuning #10` | Venison, chevon and passion fruit glazed duck removed; Abnormals Delight duck line wired to Untitled Duck Mod (2026-10-25, shipped 1.1.03) | [food_families.md](../food_families.md) 4.4 | S P; SQ-FF-01 PASSED 2026-10-31; FF-03, FF-05 in game; open: Maple Glazed Bacon has no source (remove with its can, or a recipe) |
| `food/types-and-tuning #11` | Food tests FO-02 to FO-28: type tags, canned recipes, servings, fried eggs, egg stacks | [food.md](../food.md) 7 | S C P (all shipped 1.0.2 to 1.0.6; FO-02 counts from 2026-10-02) |
| `food/types-and-tuning #15` | Comfort vs Nourishment split (R5, built 2026-10-27, CD jar in the 1.1.04 client payload) | [comfort_nourishment.md](../comfort_nourishment.md) 10 | S C P T; CNS-01 to CNS-08, SQ-CNS not run; left: `gen_can_types.py` on the 2026-10-30 dump (can_types.json is from 2026-10-27), the B.2 hand check |
| `food/types-and-tuning #15, #6` (Blocked on Dumps, CQ-DUMP-06) | Read the 2026-10-30 food and recipe dumps | this brief #6 (egg rescan done 2026-10-31 offline) | Comfort foods per type count and the rest of CQ-DUMP-06 |

## Writeup corrections made (2026-10-21)

- [food.md](../food.md) header: status "BUILT, restart pending" -> DEPLOYED (1.0.2 on), FO-02 to FO-28 not run, link
  to this brief; last updated 2026-10-21.
- food.md 0 at-a-glance: type tags, canning.js, multi-serving cans, raw canning and fried eggs rows "BUILT ... needs
  a restart" -> DEPLOYED.
- food.md 3.2 and 3.7 headings "(built, restart pending)" -> "(DEPLOYED with 1.0.2)".
- food.md 3.5: the "ambiguous overload, prove it first" risk replaced by: both forms are in use and confirmed by the
  10-20 dump; the consumer form keeps old flags.
- food.md 3.8: `#forge:eggs` 10 -> 12 items (duck, goose), with a pointer to this brief's rescan (Brewin' port ids).
- food.md 3.9: the egg rule covers 12 eggs, not 10.
- food.md 4.2 finding 4: Farmer's Respite is installed now, its entries apply; only `largemeals` is dead; unknown
  attributes are dropped silently.
- food.md FO-02: note that the expected counts are now about 1016 foods and 252 canned sources.
