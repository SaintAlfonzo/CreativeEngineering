# Brief: food / seasoning

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# food`, Sub `seasoning` (rows 1 to 4, all Planned). Layout
per [README.md](README.md). Related writeups: [food.md](../food.md) (1 "Salt", 3.5 "Salt (optional)"),
[recipes_cooking.md](../recipes_cooking.md); sister brief [dishes_ingredients.md](dishes_ingredients.md) (bone broth #4,
ice cubes #5, Vintage Delight #12).

**Verified against jars** (scratchpad cache, 2026-10-21): The Salt 1.2.4.1 (`salt`, decompiled: `SaltingRecipe`,
`Salting`, `SaltJeiPlugin`, `mixin/FoodDataMixin`), Create: The Salt 1.1.0 (`create__the_salt`), Corn Delight 1.0.3,
Create 0.5.1.i. The pack has no KubeJS salt code yet (no `salt` / `can_be_salted` in kubejs/), and
[config/salt-common.toml](../../config/salt-common.toml) is at the mod defaults.

## Scope and order

| Row | Change | Group |
|---|---|---|
| 1 | Salt system expansion | A |
| 4 | Salt on more foods (part of row 1's tag audit) | A |
| 2 | Seasoning crafts in their own JEI category | B (patcherMod) |
| 3 | New seasonings (pepper, dill, cheese, cream and onion, ice cubes; popcorn variants) | C (needs B's mechanism) |

Order: rows 4 + 1 together (one tag pass, one recipe pass, one config pass); row 2 next, built so that row 3's
seasonings plug into the same category; row 3 last, after its decision. Row 1's capped nutrition depends on
`food/types-and-tuning #1` (the per-type value table); the rest of row 1 doesn't.

## How salt works today (shared Now)
- **Salting** is one special crafting recipe, `salt:salting` (type `salt:salting`, ingredient `#forge:salts`). It
  matches any one item in `#salt:can_be_salted` that is not already salted plus one salt, and sets NBT `{Salted:1b}`
  (`SaltingRecipe.matches/assemble`). `FoodDataMixin` adds the bonus when eaten; `Salting.getAdditionalFoodValue` reads
  per-item values from `SaltingFoodValues` in salt-common.toml, else the flat `SaltingNutrition = 2`,
  `SaltingSaturationModifier = 0.1`.
- **`#salt:can_be_salted`** (the jar's tag, 348 lines): vanilla cooked meats, bread, soups, rotten flesh, `#forge:bread`,
  `#forge:cooked_*`, cabbage, tomato, onion, salad ingredients, and about 40 Farmer's Delight foods (including
  `farmersdelight:bone_broth`, `cooked_rice`, `tomato_sauce`: intermediates), plus many ids of mods not in the pack
  (croptopia, culturaldelights, butchercraft ...; harmless). food.md 1 counts 117 resolved foods.
- **Create: The Salt** adds about 70 `create:mixing` recipes, food + `#forge:salts` -> the same food with
  `{Salted:1b}` (e.g. `create__the_salt:beefstew`, `cooked_beef_salted`, `bonebroth`), plus salt production:
  heated mixing 200 mB water -> 20 % salt (`salt_create_recipe`), crushing / milling raw rock salt -> 1 salt and the raw
  block -> 9.
- **Salt sources:** rock salt ore and salt clusters drop `salt:raw_rock_salt` (fortune); 1 raw -> 1 salt shapeless
  (`salt:salt_from_raw_rock_salt`); cauldron evaporation; butcher trades. Dict: only `i.salt = '#forge:salt'`
  (item_dict.js 12556).
- **JEI:** Salt shows salting as a crafting-category extension (`SaltJeiPlugin.registerVanillaCategoryExtensions` ->
  `SaltingShapelessExtension`), plus its own evaporation and crystal-growing categories.

---

## seasoning #1: Salt system expansion
Status **Planned** · Work: KubeJS / config / tool · id `food/seasoning #1`

### Goal
Salt improves nutrition, capped; the saltable tag covers every valid food; more salt recipes; Create salting works
pack-wide; intermediates (bone broth) can't be salted; salt from clusters / raw rock salt by cutting board, milling
and crushing with equal efficiency and more than one output with a chance; merge with Vintage Delight later.

Added 2026-10-24 (future, not this pass): one JEI category per seasoning type instead of the single Seasoning tab
(`SeasoningJEI`, [food.md](../food.md) 3.11); the pack seasoning pages (3.12) would split by seasoning. Needs the
category list first (`seasoning #3`); no work before it.

### Build
1. **Dict keys** (item_dict.js, next to `i.salt`): `i.salt_item = 'salt:salt'`, `i.raw_rock_salt = 'salt:raw_rock_salt'`,
   `i.raw_rock_salt_block = 'salt:raw_rock_salt_block'`, `i.salt_cluster = 'salt:salt_cluster'`,
   `i.t_can_be_salted = '#salt:can_be_salted'`, `i.t_salts = '#forge:salts'`.
2. **Tag audit** (cookingtags.js, item event; row 4 is part of this):
   ```js
   t.add(e, i.t_can_be_salted, [i.t_food_meal, i.t_food_soup, i.t_food_cooked_meat, i.t_food_cooked_meat_sliced,
       i.t_food_cooked_fish, i.t_food_cooked_fish_sliced, i.t_food_cooked_veggie, i.t_food_egg, i.t_food_handheld,
       i.t_food_salad, i.t_food_baked])
   t.remove(e, i.t_can_be_salted, [i.bone_broth, i.cooked_rice, i.tomato_sauce])   // intermediates
   ```
   The food-type tags are built in foodtypetags.js; nested tag references resolve live. A nested tag can't be
   filtered, so if an intermediate sits inside one of those types, remove it from the type or use
   `t.addMatching(..., { except })`, which expands (tag_helper.js 35-55). Out: sweets, drinks, golden, canned, hazard,
   raw food (decision D2).
3. **Capped nutrition** (needs `food/types-and-tuning #1`): a tool (`tools/gen_salt_values.py`, or a mode of
   `tools/food_catalog.py`) reads the `/dumpfood` export and food_types.json and writes `SaltingFoodValues` into
   config/salt-common.toml: per item `min(2, max(1, round(hunger * 0.25)))` and saturation +0.05 / +0.1 by type (food.md
   3.5's suggestion: +1 / +0.05 raw, +2 / +0.1 meals). salt-common.toml is a common config in `config/`, read at
   load: a restart applies it. Keep `minecraft:rotten_flesh,1,0.05`.
4. **Create salting pack-wide** (cooking.js or a new `kubejs/data/creative_delight/kubejs/creativeengineering/seasoning.js`):
   ```js
   R.remove({ mod: 'create__the_salt', type: 'create:mixing', not: { id: 'create__the_salt:salt_create_recipe' } }, e)
   Ingredient.of(i.t_can_be_salted).itemIds.forEach(id => {
       R.createMixing(Item.of(id, '{Salted:1b}'), [id, i.t_salts], e, 'salting/' + String(id).replace(':', '_'))
   })
   ```
   `R.remove` passes a filter object through to `e.remove`. Tags are bound in the recipe event, so `itemIds` is the
   live tag. Gotcha: the input matches salted food too (plain item ingredients ignore NBT), so a salted stack in a basin
   wastes salt; acceptable, or use `{ type: 'forge:nbt', item: id, nbt: '{}' }` only if tested (NBT ingredients have
   a history of matching nothing in this pack, item_dict.js comments and cans_canning.md).
5. **Salt production, equal efficiency, several outputs with chance** (raw rock salt -> 1 salt + 25 % 1 by every route):
   ```js
   R.remove({ id: 'salt:salt_from_raw_rock_salt' }, e)       // hand craft: keep or make it 1 flat (decision D3)
   R.remove({ id: 'create__the_salt:salt_create_crushing' }, e)
   R.remove({ id: 'create__the_salt:salt_create_milling' }, e)
   var SALT = [i.salt_item, Item.of(i.salt_item).withChance(0.25)]
   R.createCrushing(SALT, i.raw_rock_salt, e, 'raw_rock_salt_salt')
   R.createMilling(SALT, i.raw_rock_salt, e, 'raw_rock_salt_salt')
   R.ieCrushing(i.salt_item, i.raw_rock_salt, e, 'raw_rock_salt_salt', [{ chance: 0.25, output: i.salt_item }])
   R.hammering(i.salt_item, i.raw_rock_salt, e, 'raw_rock_salt')   // cutting board + grid, IE hammer
   ```
   Clusters (silk touch only gives the block): `salt_cluster` -> 3 salt + 50 % 1 on the same routes; buds smaller.
   Check `R.hammering` with a single output (FD cutting allows only result lists; NCH passes a string, as other calls do).
6. **Vintage Delight merge:** waits on `dishes/ingredients #12`.

### Ids and data
Recipe types `salt:salting` (special crafting), `create:mixing` with NBT result, `create:crushing`, `create:milling`,
`immersiveengineering:crusher`, `farmersdelight:cutting`. Config `config/salt-common.toml [Salting] SaltingFoodValues`.

### Built 2026-10-21: raw rock salt 4x (`cooking/salt.js`)
Raw rock salt (salt crystals) -> 4 salt on every route: hand craft (`creativeengineering:shapeless/raw_rock_salt_salt`),
Create crushing and milling (`raw_rock_salt_salt`), mortar craft (catalog `count` 4). Replaces the three mod recipes
(`salt:salt_from_raw_rock_salt`, `create__the_salt:salt_create_crushing` / `_milling`). Unchanged: salt block 9 <-> 1,
raw block -> 9 raw, Create water mixing (20 %), cauldron evaporation. Supersedes the 1 salt + 25 % plan in step 5 and D3.
Water mixing is now 60 % (was 20 %); cauldron evaporation (full cauldron) bonus salt is 50 % (was 25 %), a loot table override in `kubejs/data/salt/`. Rerun `/dumpfood` and the mortar catalog tool to regenerate the catalog from the new recipes.
Test **SE-05:** 1 raw rock salt in the grid, crushing wheels, millstone and with the mortar each give 4 salt; the salt
block still unpacks to 9.

### Decisions
- **D1 The cap:** recommend +25 % of the food's hunger rounded, 1 to 2, so a 1-hunger raw food no longer triples.
- **D2 What is saltable:** recommend savoury cooked food (meals, soups, cooked meat / fish / veg, eggs, handhelds,
  salads, bread); not raw food, sweets, drinks, intermediates.
- **D3 Hand route for salt:** recommend keeping 1 raw -> 1 salt by hand (no bonus chance) so machines are the better route.

### Gotchas
- Salting copies the stack's other NBT; multi-serving cans are out (canned type excluded).
- Create Salt's water -> 20 % salt mixing stays (it is the only renewable route besides evaporation).

### Tests (prefix `SE-`, new; or food.md's `FO-`)
- **SE-01 Saltable:** a cooked dish from Rustic Delight (calamari roll) salts in the grid; bone broth doesn't.
- **SE-02 Create salting:** a basin with cooked beef and salt gives salted cooked beef; same for a pack-added food.
- **SE-03 Salt routes:** raw rock salt in crushing wheels, millstone, IE crusher, on a cutting board with a hammer: 1
  salt, sometimes 2.
- **SE-04 Values:** a salted 1-hunger food shows +1 in the tooltip, a meal +2.

---

## seasoning #4: Salt on more foods
Status **Planned** · Work: KubeJS · id `food/seasoning #4`

### Goal
Calamari roll, crab bar, "crab ragout", every meat dish; audit the saltable tag (part of row 1).

### Now
Not in `#salt:can_be_salted` (jar, checked): `rusticdelight:calamari_roll` (the tag names `culturaldelights:calamari_roll`,
not installed), `quarkdelight:crab_bars` (`i.crab_bars`), any Quark Delight / Delightful / Alex's Delight dish. "Crab
ragout" is no item in the dict; the closest is `delightful:crab_rangoon` (`i.crab_rangoon`, item_dict.js 11283).

### Build
Row 1 step 2 covers them by type (`meal`, `handheld`, cooked meat / fish). Check each named item is in one of those
types in food_types.json; if not, `t.add(e, i.t_can_be_salted, [i.calamari_roll, i.crab_bars, i.crab_rangoon])`.

### Decisions
- **Crab ragout:** recommend reading it as crab rangoon; confirm.

### Tests
- SE-01 covers it.

---

## seasoning #2: Seasoning crafts in their own JEI category
Status **Planned** · Work: patcherMod · id `food/seasoning #2`

### Goal
Shapeless seasoning crafts (food + salt, and row 3's seasonings) shown in their own JEI category instead of crafting.

### Now
Salt's salting is a crafting-category extension (`SaltJeiPlugin.registerVanillaCategoryExtensions`, verified), so it
sits in the Crafting tab as one "any saltable food + salt" page. patcherMod already has JEI plugins to copy:
[compat/jei/SmithingTemplateJEI.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/jei/SmithingTemplateJEI.java),
[oven/jei/OvenJeiPlugin.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/oven/jei/OvenJeiPlugin.java),
[compat/DragonBreathingJEI.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/DragonBreathingJEI.java).

### Build
1. New `compat/jei/SeasoningJEI.java` (`@JeiPlugin`): `RecipeType.create("creativeengineering", "seasoning", SeasoningDisplay.class)`,
   a category drawn like a 2-input shapeless page (food + seasoning -> seasoned food, tooltip with the bonus from
   `Salting.getAdditionalFoodValue`), catalyst: crafting table (and the Create mixer for the row 1 mixing recipes).
2. Recipes: one display per seasoning, listing every item of `#salt:can_be_salted` as a cycling slot (one page), or
   one page per food (hundreds; recommend the cycling page plus "uses of X" lookups working per food via focus).
3. Remove Salt's crafting extension: a mixin cancelling `SaltJeiPlugin.registerVanillaCategoryExtensions`
   (`@Inject(method = "registerVanillaCategoryExtensions", at = @At("HEAD"), cancellable = true, remap = false)`),
   gated by a CEConfig flag; or hide the `salt:salting` recipe in `onRuntimeAvailable` via
   `IRecipeManager.hideRecipes(RecipeTypes.CRAFTING, ...)` (the extension wraps the recipe object; hiding is simpler).
4. Pack seasonings (row 3): give their recipe ids a common prefix (`creativeengineering:shapeless/season/...`) and have
   the plugin move every crafting recipe with that prefix into the category (hide in crafting, add as displays).
5. The Create salting mixes (row 1 step 4) stay in the mixer category; decision whether to also hide them there.

### Decisions
- **Mixer salting in JEI:** recommend hiding the generated `salting/*` mixing recipes from the mixing category and
  listing the mixer as a catalyst on the seasoning page (hundreds of near-identical mixing pages otherwise).

### Built 2026-10-21
`compat/jei/SeasoningJEI` + `SeasoningCategory`, CEConfig `[jei] seasoningCategory`; details in [food.md](../food.md) 3.11.
Step 3 done by hiding (no mixin). Step 4: prefix `creativeengineering:shapeless/season/`, 2 or 3 ingredients. Step 5 /
the mixer decision: Create: The Salt's mixes are hidden from Mixing and shown as one mixer page; row 1's generated
`salting/*` mixes will need the same treatment (they will be folded in automatically if their result carries `Salted`).

### Tests
- **FO-30 / FO-31** in food.md 7 (replace the SE-05 JEI line; SE-05 is the raw rock salt test above).

---

## seasoning #3: New seasonings
Status **Planned** · Work: KubeJS / patcherMod · id `food/seasoning #3`

### Goal
Pepper, dill (sea pickle?), cheese, cream and onion, ice cubes for cold drinks; distinct placeable popcorn variants
(regular popcorn placeable first).

### Now
No pepper (only Rustic Delight bell peppers), dill or spice item in the dict. Salt's mechanism is salt-only
(`Salting.isSalted` reads the `Salted` key; `FoodDataMixin` adds only salt's value). Popcorn: `corn_delight:popcorn`
(smelted kernels), `caramel_popcorn` (popcorn + sugar, shapeless), `popcorn_box` (placeable, generation_scripts/food/placeable.json
109); the popcorn item itself isn't placeable.

### Build
1. **Mechanism (patcherMod, `food/` package):** NBT list `Seasonings:["creativeengineering:pepper", ...]` on a food;
   a custom crafting recipe type `creativeengineering:seasoning` (copy `SaltingRecipe`: one food from a tag
   `#creativeengineering:seasonable/<seasoning>` + one seasoning item, not already carrying it); the bonus applied on
   eat (a `LivingEntityUseItemEvent.Finish` handler or a FoodData mixin like Salt's), values per seasoning in CEConfig
   (`[seasoning]`: nutrition, saturation); tooltip line per seasoning. Row 2's category shows these.
2. **Items (KubeJS startup):** `creativeengineering:ground_pepper` (from? decision: bell pepper seeds dried, or a new
   crop), `creativeengineering:dill` (from sea pickles on the cutting board, per the row's hint), cheese = any
   `#brewinandchewin:foods/cheese_wedge`, cream and onion = a powder from milk + onion (heated mixing). Dict keys
   `i.ground_pepper`, `i.dill`, `i.cream_onion_seasoning`.
3. **Ice cubes for cold drinks:** the same recipe type with `i.t_ice_cubes` (dishes_ingredients.md #5) on
   `#creativeengineering:food_type/drink` minus hot ones; or simply chilled variants (decision).
4. **Popcorn variants:** regular popcorn placeable first (`food/placeable`, see [food_placeable.md](food_placeable.md));
   then salted / cheese / caramel popcorn boxes as distinct block items (KubeJS blocks or Display Delight style), made
   from a popcorn box + seasoning.

### Decisions
- **Nutrition vs saturation per seasoning (the row's decision):** recommend salt = nutrition (as now), pepper =
  saturation, cheese = both small, cream and onion = saturation, ice = no food value (a short Fire Resistance or a
  thirst-style effect instead).
- **NBT seasonings vs new items:** recommend NBT (like salt) for dishes, new items only for popcorn variants (they need
  their own block models).

### Gotchas
- NBT on food breaks stacking with unseasoned food and makes recipe inputs ambiguous (plain ingredients still accept
  it); same as salt today.
- Food-tuning values (`food/types-and-tuning #1`) must be applied before seasoning bonuses are tuned.

### Built 2026-10-23: the framework (step 1) and a popcorn test set
Step 1 built as planned except: definitions live in KubeJS startup (`global.seasoning` in
`development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/seasonings.js`, passed to `food/Seasonings.define`), not CEConfig; the recipe carries its food
ingredient and seasoning items (no per-seasoning `seasonable` tag); bonus by a `LivingEntityUseItemEvent.Finish`
handler; saturation is absolute points; `heal` and `combines` (which seasonings may share a food) added. Test set:
cheese (+2 saturation), onion (+2 nutrition), garlic (Speed I 10 s) on popcorn; onion and garlic combine. Details and
tests: [food.md](../food.md) 3.12, FO-45 to FO-48. Steps 2 to 4 not started.

### Built 2026-10-26: foods by family, popcorn only
Each seasoning's foods are `#<ns>:seasonable/<path>`: its `families` (food_families.py paths, subfamilies included),
then `add`, then `remove` ([food.md](../food.md) 3.12). The craft refuses a food outside the tag; crafts are generated
per seasoning and per combining pair. Cheese, onion, garlic: popcorn only (`add`); sugar: no foods (waffle test dropped).
Tests FO-53, FO-56.

### Tests
- **SE-06 Pepper:** cooked beef + ground pepper -> peppered cooked beef with the bonus line; eating it gives the extra
  saturation.

---

## seasoning #6: Merge salts; all salt is Vintage pink
Status **Planned** / Work: KubeJS / data / id `food/seasoning #6`

### Goal
One salt in the whole pack: the salt items of the salt mods merge into Vintage Delight's pink salt, and textures,
recipes and tags use it.

### Now
- Salt sources: The Salt (`salt:salt` = `i.salt_item`, `raw_rock_salt`, `salt:salt_block`, `salt:salt_lamp`, buds,
  clusters, ores; tags `forge:salt`, `forge:salts`, `forge:dusts/salt`), Create: The Salt (recipes only, no item), Vintage
  Delight (`vintagedelight:salt_dust` = `i.salt_dust`, `salt_block`, `salt_bucket`, 17 lamps, bricks, a layer block
  `vintagedelight:salt`). `salt_dust.png` is the pink one (checked: pink crystal icon).
- Pack code writes `i.salt_item` as an output: `kubejs/data/creative_delight/kubejs/creativeengineering/salt.js` lines 21-27; `i.salt` is the tag
  `#forge:salt`. Recipes that take the tag already accept either salt.
- The merge framework: `startup_scripts/_lib/item_merges.js` (`[purged, survivor]`), recipes and loot swapped by
  `server_scripts/other/item_merges.js`, design in [item_merges.md](../item_merges.md).

### Build
1. Dict: add `i.vintagedelight_salt_block` etc. only where missing; point `i.salt_item` at the survivor (or drop it).
2. `item_merges.js`: `[i.salt_item, i.salt_dust]` and the block / lamp pairs; The Salt's world gen (ore, clusters, buds)
   keeps dropping `salt:raw_rock_salt`, so merge that to the survivor's input route (raw rock salt -> salt dust, 4x,
   `salt.js`) rather than purging it.
3. Retexture: if the survivor is Vintage's, nothing; if The Salt's, a pink recolour of `salt:item/salt` in a resource pack.
4. `salt-common.toml` needs no change: `SaltingRecipe` takes `#forge:salts`; check Salt's evaporation / `SaltJeiPlugin`
   output still names `salt:salt` (unverified; the JEI page in 3.11 shows it).

### Decisions
- **Which salt wins.** Options: (a) Vintage's `salt_dust` survives (the row's wording); (b) The Salt's `salt:salt`
  survives, recoloured pink. Recommend (a), but keep `salt:raw_rock_salt` and Salt's ore/cluster worldgen producing it,
  because The Salt's block and lamp ids are referenced by its own worldgen (unverified).
- The Salt's blocks and lamps vs Vintage's: merge only the items first; decide blocks with `vintage_delight.md` 3.1.

### Gotchas
- Tests SE-03 and the canning `_salt` recipes (`canning.js`) name the salt tag; re-run `/cegen` after any dict change.

### Tests
- FO-50 One salt in JEI: search Salt shows one item (pink); raw rock salt crushing gives it; salting still works.

---

## seasoning #7: Salt layers: place from inside, retexture
Status **Planned** / Work: patcherMod / resource pack / id `food/seasoning #7`

### Goal
Place salt layers while standing in them (no push out); retexture the salt layer as a palette swap of another layer block.

### Now
- Vintage Delight's layer is `SaltLayerBlock extends SnowLayerBlock` (`development/backportedVintage/.../block/custom/
  SaltLayerBlock.java`); `SaltDustItem.useOn` writes layers 1 to 8 with `setBlock` and no entity check. Snow's collision
  shape is one layer shorter than its height, so a thicker layer shoves a player standing in it upward.
- Texture: `assets/vintagedelight/textures/block/` (the layer model uses `salt_height2` ... `salt_height14`).

### Build
1. Fix in the port (it is the port's block, so this is `new jar`, not patcherMod): override `getCollisionShape` in
   `SaltLayerBlock` to return empty (or a thin shape) when an entity is inside, or skip the entity push in
   `SaltDustItem`. Rebuild with `:backportedVintage:build`, then `installToMods`.
2. Retexture: a palette swap of a snow-layer-like block texture (which one: decision) in the port's resources; the
   `_height` models stay.

### Decisions
- Which layer block is the palette source: options snow, sand layers (IW), Create's; recommend snow (same model).
- Collision: empty always (walk through salt) vs only while standing in it; recommend only while an entity overlaps.

### Built 2026-10-26 (item icon only)
The salt layer item (`vintagedelight:salt`) shows its one-layer block model, as snow's and ash's items do (was salt dust's
flat icon); port `-07`, `tools/port_resources.py`. Collision and retexture still open. Test VTD-77.

### Tests
- VTD-54 Stand in a salt layer, right-click salt dust at your feet: the layer thickens, you are not pushed up.

---

## seasoning #8: Show seasoning on AppleSkin
Status **Planned** / Work: patcherMod / id `food/seasoning #8`

### Goal
A seasoned food's seasoning shows in the AppleSkin tooltip.

### Now
- Pack seasonings (3.12) are already added to AppleSkin's food values: `compat/AppleSkinSeasonings.java` (`FoodValuesEvent`,
  nutrition and saturation only; FO-49). Salt's own `Salted` bonus is not shown (the file's comment: "Salt's `Salted` is
  its own mechanism and untouched"; Salt's mixin applies it on eating).
- So the gap is Salt's `Salted` (and heal / effect seasonings, which AppleSkin cannot show as values).

### Build
1. In `AppleSkinSeasonings.onFoodValues`, if the stack has `Salted:1b`, add `Salting.getAdditionalFoodValue(stack)`
   (Salt's API, as `SeasoningJEI` compiles against `libs/salt-...jar`). Check it is not counted twice if a merge changes
   `FoodDataMixin`.
2. If the intended "seasoning" is only the pack ones, the row is already built (FO-49); confirm in game first.

### Decisions
- Show the seasoning's name line too? AppleSkin has no line API; the pack's own tooltip line already exists
  (`food/SeasoningTooltip`). Recommend values only.

### Built 2026-10-26
Salt's own `AppleSkinHandler` already shows `Salted` (it was not missing: the brief's "Now" was wrong). The real fault
was order: the pack handler and Salt's ran in no fixed order on the same event. The pack handler is now `LOWEST`, so it
adds on top of Salt's values ([food.md](../food.md) 3.12). Values only, no name line (D: recommended).

### Tests
- FO-54 (food.md 7; replaces the FO-51 proposed here, the number went to powdered sugar).

---

## seasoning #9: Seasoned icon for every new seasoning
Status **Planned** / Work: resource pack / id `food/seasoning #9`

### Goal
Each new seasoning (`seasoning #3`) gets its own seasoned icon.

### Now
- Salt draws a layered overlay on salted foods (`salt:item/salted_overlay`, `LayeredBakedModel`); the pack seasonings
  have none: only a tooltip line (`SeasoningTooltip`) and lang (`seasoning_lang.js`). "Seasoned icon" is read as this
  overlay (unverified: the note does not say).

### Build
1. One 16x16 overlay per seasoning (garlic, onion, cheese so far) in `kubejs/assets/creativeengineering/textures/item/`.
2. A client model hook that layers the overlay for each id in `Seasonings:[...]` (model override on the NBT, or a
   `LayeredBakedModel`-style baker in patcherMod). Salt's baked model is the pattern to copy.

### Decisions
- One combined icon per combination vs layers per seasoning; recommend layers (garlic + onion = both overlays).

### Built 2026-10-26
patcherMod `food/SeasonedItemModel` + `mixin/SeasonedItemModelMixin` (layers, as recommended); overlays in Salt's shape,
the seasoning's colours (`textureswapper/seasoning/make_seasoning.py`); Salt's own overlay switched off and drawn by the pack
([food.md](../food.md) 3.12). Several overlays on one food overlap (one shape).

### Tests
- FO-55 (food.md 7; replaces the FO-52 proposed here).

---

## Built, needs play
- `seasoning #2` (Testing): patcherMod jar to install, FO-30, FO-31.

## Proposed TODO corrections (for the TODO owner)
- `seasoning #4` Change: "crab ragout" is no item in the pack; probably Delightful's crab rangoon. Writeup: this brief.
- `seasoning #1` Details: add "bone broth, cooked rice and tomato sauce are in `salt:can_be_salted` today; Create: The
  Salt mixes about 70 FD foods only".

## Writeup corrections made
None (food.md's salt description matches the jar and config).
