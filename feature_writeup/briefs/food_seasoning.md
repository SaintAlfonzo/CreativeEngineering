# Brief: food / seasoning

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# food`, Sub `seasoning`; updated 2026-10-31 (brief sweep:
rows 5, 10 to 13 added, every section checked against the pack, Testing rows cut to one line). Layout per
[README.md](README.md). Writeups: [food.md](../food.md) (3.11 Seasoning JEI page, 3.12 pack seasonings framework,
3.13 powdered sugar; test prefix `FO-`, last id FO-59), [seasonings.md](../seasonings.md) (catalog of every
seasoning, the cap, BBQ pairs), [barbeques_delight.md](../barbeques_delight.md) (3.3, 3.8, 4.1, 4.3: the BBQ fold),
[item_merges.md](../item_merges.md) 2.9 (salt merge, `MG-`), [vintage_delight.md](../vintage_delight.md) (`VTD-`).
Sister brief [dishes_ingredients.md](dishes_ingredients.md) (bone broth #4, ice cubes #5, powdered sugar #16).

**Verified against jars** (2026-10-21, rechecked 2026-10-31 in `mods/`): The Salt `salt-1.19.2-1.2.4.1`, Create: The
Salt `create_salt 1.19.2-1.1.0`, Corn Delight 1.0.3, backportedVintage `0.1.6-ce-19.2-09`, AppleSkin 2.4.2,
patcherMod `creativeengineering-1.1.04`. Barbeque's Delight is **not** installed and its port
(`development/backportedBarbeques`, `largecontent/mod-additions #37`) does not exist yet.

## Scope and order

| Row | Status | Change | Group |
|---|---|---|---|
| 10 | In Progress | Salt swap complete; fermenting | Z: only in-game checks left (the SQ-SALT-01 retest passed as SQ-CM-10) |
| 4 | In Progress | Salt on more foods | A (one tag pass with row 1) |
| 1 | In Progress | Salt system expansion | A: tag audit, Create salting, capped values |
| 7 | In Progress | Salt layers: place from inside | B (port jar, collision only) |
| 5 | In Progress | Sugar as a seasoning | C (pack framework, data only) |
| 3 | In Progress | New seasonings | C (pack framework, data + items) |
| 11 | Planned | BBQ seasonings into the pack system | D (patcherMod framework kinds; after the BBQ port) |
| 13 | Planned | `grilled` seasoning type | D (after 11 and the skewer fold) |
| 12 | Planned | Seasoning sources and villager trades | E (temporary source with the port; full pass after 11) |
| 2, 6, 8, 9 | Testing | JEI page, salt merge, AppleSkin, icons | listed at the end |

Order: A (rows 4 + 1 together: one tag pass, one recipe pass; the capped values wait on
`food/types-and-tuning #1`), B any time (port rebuild), C as content passes on the built framework, then D after the
BBQ port lands, in the order of barbeques_delight.md 4.1 (seasoning system first, then skewers and `grilled`). E's
temporary source ships with the port.

## How salt works today (shared Now, checked 2026-10-31)
- **Salting** is one special crafting recipe, `salt:salting` (type `salt:salting`, ingredient `#forge:salts`): any one
  item in `#salt:can_be_salted` not yet salted plus one salt gives NBT `{Salted:1b}`. Salt's `FoodDataMixin` adds the
  bonus when eaten; values from [config/salt-common.toml](../../config/salt-common.toml) `[Salting]`, still the mod
  defaults: `SaltingNutrition = 2`, `SaltingSaturationModifier = 0.1`, `SaltingFoodValues = ["minecraft:rotten_flesh,1,0.05"]`.
  It is separate from the pack framework (Salt's `Salted` never counts toward pack combine rules or the planned cap).
- **One salt** since the merge (`seasoning #6`): The Salt's `salt:salt` merges into Vintage Delight's pink
  `vintagedelight:salt_dust` (`i.salt_dust`, item_dict.js 14496; merge row in CD
  `kubejs_startup/vintagedelight/item_merges.js` line 11). `#forge:salts`, `#forge:salt` and `#forge:dusts/salt` hold
  only salt dust (SQ-SALT-02 PASS). Dict keys: `i.salt` = `#forge:salt`, `i.salt_item` = `salt:salt`,
  `i.raw_rock_salt`, `i.salt_salt_block`, `i.salt_lamp` (item_dict.js 12825-12829). No key yet for
  `#salt:can_be_salted` or `#forge:salts`.
- **`#salt:can_be_salted`**: 148 items in the 2026-10-30 tag dump (`kubejs/exported/tags/item_tags.json`), still the
  jar's list; no pack script adds to or removes from it (the only mention is a comment in
  [seasoning_tags.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/seasoning_tags.js) line 13).
  Intermediates still in it: `farmersdelight:bone_broth`, `cooked_rice`, `tomato_sauce`.
- **Create: The Salt**: 67 `create__the_salt:*` `create:mixing` recipes in the 2026-10-30 recipe dump (food + salt ->
  the same food `{Salted:1b}`, FD foods and vanilla only). Its salt production recipes are removed.
- **Salt routes** ([salt.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/salt.js), CD): raw rock salt
  -> 4 salt by hand, crushing, milling (and the mortar via `mortar_catalog.json`); heated basin 200 mB water -> 60 %
  salt (`water_salt`); cauldron evaporation bonus 50 % (`kubejs/data/salt/loot_tables/cauldron_evaporation/`); Large
  Salt Lamp from 7 Salt Blocks. Salt's own butcher trades are on (`ButcherSaltTradesEnabled = true`); the trade dump
  still names `salt:salt`, which the merge's inventory swap turns into salt dust.
- **Pack framework** (food.md 3.12): `global.seasoning` in CD
  [seasonings.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/seasonings.js)
  (5 seasonings: cheese, onion, garlic, sugar, void_pepper), patcherMod `food/Seasonings.java` (223 lines; bonus
  kinds `nutrition`, `saturation`, `heal`, `effects`; `combines`), `food/SeasoningRecipe`, crafts by `NCH.season`
  (neoCraftHelper.js 1400-1412) from [seasoning.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/seasoning.js),
  foods by `#<ns>:seasonable/<path>` from `families` / `add` / `remove`. Today every seasoning with foods is on popcorn
  only; sugar has none.

---

## seasoning #1: Salt system expansion
Status **In Progress** · Work: KubeJS / config / tool · id `food/seasoning #1`

### Goal
Salt improves nutrition, capped; the saltable tag covers every valid food; Create salting works pack-wide;
intermediates (bone broth, cooked rice, tomato sauce) can't be salted; salt from crystals by every route with equal
efficiency. Later (scratchpad 2026-10-24): one JEI category per seasoning type instead of the one Seasoning tab.

### Done
- 2026-10-21: raw rock salt -> 4 salt on every route (supersedes the 1 + 25 % plan); water mixing 60 % (was 20 %);
  cauldron bonus 50 % (was 25 %). Test SE-05 (below).
- 2026-10-26: Vintage merge (row 6). Dict keys `i.salt_item`, `i.raw_rock_salt` exist.

### Build
1. **Dict keys** (item_dict.js, next to `i.salt`, line 12825): `i.t_can_be_salted = '#salt:can_be_salted'`,
   `i.t_salts = '#forge:salts'`.
2. **Tag audit, family-driven** (rows 1 and 4 together). Salt keeps its own tag, but fill it the way the pack
   seasonings are filled: add a `saltable` definition (`families`, `add`, `remove`) next to `global.seasoning` in
   seasonings.js (not passed to `Seasonings.define`), and a block in seasoning_tags.js that resolves it with the same
   `members()` helper and writes it with `global.tags`:
   ```js
   // seasonings.js
   global.saltable = { families: ['meat/cooked', 'meat/cooked_cut', 'fish/cooked', 'fish/cooked_cut',
       'veggie/cooked', 'veggie/cooked_cut', 'egg/cooked', 'egg/dish', 'soup', 'meal', 'roll', 'sandwich', 'stuffed',
       'salad', 'skewer', 'snack'], add: [], remove: [i.bone_broth, i.cooked_rice, i.tomato_sauce] }
   // seasoning_tags.js, after the seasonable loop
   t.add(e, i.t_can_be_salted, idsOf(global.saltable))   // idsOf: the families -> members() expansion the loop uses
   t.remove(e, i.t_can_be_salted, global.saltable.remove)
   ```
   Family names checked in `kubejs/generation_scripts/food/food_families.json` (2026-10-31). The jar's list stays
   underneath (its ids of absent mods are harmless); `remove` works on it because it is a plain list.
   `bone_broth` is in `soup/soup` and `cooked_rice` in `meal/rice`, so they must be removed after the family add.
   Out: raw food, sweets, drinks, golden, canned, hazard (D2).
3. **Capped nutrition** (needs `food/types-and-tuning #1`): a tool mode (`tools/food_catalog.py --salt` or
   `tools/gen_salt_values.py`, registered in `tools/generators.json`) reads the `/dumpfood` export and writes
   `SaltingFoodValues` into config/salt-common.toml: per item `min(2, max(1, round(hunger * 0.25)))` hunger and
   +0.05 / +0.1 modifier by family. Common config, read at load: restart. Hand-edited config: copy to
   `configs_edited/root_config/salt-common.toml` (a copy already exists there). Keep `minecraft:rotten_flesh,1,0.05`.
4. **Create salting pack-wide** (salt.js, CD): one unheated mixing per saltable food not already covered by
   Create: The Salt:
   ```js
   var covered = {}   // outputs of the 67 create__the_salt mixes
   Ingredient.of(i.t_can_be_salted).itemIds.forEach(id => {
       if (covered[id]) return
       R.createMixing(Item.of(id, '{Salted:1b}'), [id, i.t_salts], e, 'salting/' + String(id).replace(':', '_'))
   })
   ```
   Tags are bound in the recipe event. The plain item input also matches salted food (a salted stack wastes salt);
   accept it rather than an NBT ingredient (NBT ingredients have matched nothing in this pack before). The Seasoning
   JEI page folds in any mixing whose result carries `Salted` (food.md 3.11), so no JEI work.
5. **Per-seasoning JEI categories** (future, scratchpad 2026-10-24): `SeasoningJEI` registers one `RecipeType` per
   seasoning (salt plus each `Seasonings.ids()`) instead of one. Wait for the seasoning list to settle (rows 3, 11).

### Decisions
- **D1 The cap:** recommend +25 % of the food's hunger rounded, 1 to 2, so a 1-hunger food no longer triples.
- **D2 What is saltable:** recommend the savoury cooked families in step 2; not raw food, sweets, drinks,
  intermediates. Snack is in for popcorn; check the rest of `snack` in food_families.md 3.4 first.
- **D3** (hand route): settled by the 4x build (every route gives 4).

### Gotchas
- Salting copies the stack's other NBT; canned food is out (not in any listed family).
- A family-driven tag moves when food_families.json is regenerated: rerun `/cegen run food_families` after `/dumpall`.

### Tests (SE- ids from 2026-10-21, proposed; numbered as FO- when queued)
- **SE-01 Saltable:** Calamari Roll and Crab Rangoon salt in the grid; Bone Broth, Cooked Rice, Tomato Sauce don't.
- **SE-02 Create salting:** a basin with a pack-added cooked food and salt gives the salted food.
- **SE-04 Values:** a salted 1-hunger food shows +1 in AppleSkin, a meal +2.
- **SE-05 Salt routes (built 2026-10-21):** 1 raw rock salt in the grid, crushing wheels, millstone and the mortar
  each give 4 salt; the salt block still unpacks to 9.

---

## seasoning #4: Salt on more foods
Status **In Progress** · Work: KubeJS · id `food/seasoning #4`

### Goal
Calamari roll, crab bar, every meat dish in the saltable tag (part of row 1's audit).

### Now (2026-10-30 dumps)
Not in `#salt:can_be_salted`: `rusticdelight:calamari_roll` (`i.calamari_roll`, item_dict.js 12728; family `roll`),
`delightful:crab_rangoon` (`i.crab_rangoon`, 11366; family `roll`), `quarkdelight:crab_bars` (`i.crab_bars`, 1768;
**no family**, food_families.md 3.3 line 345 proposes `snack`). "Crab ragout" is no item; read as crab rangoon.

### Build
Row 1 step 2 covers the first two through `roll`. Crab bars: give it a family in `tools/food_families.py` (`snack`)
or put it in `global.saltable.add` (`i.crab_bars`).

### Tests
- SE-01 covers it (add Crab Bars to its list).

---

## seasoning #7: Salt layers: place from inside
Status **In Progress** · Work: new jar (backportedVintage) · id `food/seasoning #7`

### Goal
Place salt layers while standing in them, without being pushed out. (The retexture half is built.)

### Done
- 2026-10-26: the layer item shows its one-layer block model (port `-07`, VTD-77); the layer uses the pink Salt Block
  texture (`make_salt.py`, item_merges.md 2.9, MG-35), in the 1.1.04 client payload.

### Now
- [SaltLayerBlock.java](../../development/backportedVintage/src/main/java/net/ribs/vintagedelight/block/custom/SaltLayerBlock.java)
  (30 lines) extends `SnowLayerBlock` and overrides nothing about shape: the collision shape is one layer shorter than
  the block, so adding a layer at the player's feet shoves the player up.
- [SaltDustItem.java](../../development/backportedVintage/src/main/java/net/ribs/vintagedelight/item/custom/SaltDustItem.java)
  `useOn` (lines 21-50) grows a layer with `setBlock` (line 30) or places one above the clicked block (line 43) with
  no entity check. Port now `0.1.6-ce-19.2-09`.

### Build
1. In `SaltLayerBlock`, override `getCollisionShape(state, level, pos, ctx)`: when `ctx` is an
   `EntityCollisionContext` whose entity's feet are inside the block (`entity.getY() < pos.getY() + height`),
   return the shape of the layer count the entity already stands on (or `Shapes.empty()`), else `super`.
2. Rebuild with `./gradlew :backportedVintage:build --offline`, then `:backportedVintage:installToMods` (game closed).
   Bump the port build to `-10` in gradle.properties.

### Decisions
- Collision while inside: recommend "only while an entity overlaps" (walking on top of thick salt stays solid).

### Tests
- **VTD-new1** (proposed): stand in a 2-layer salt patch, right-click salt dust at your feet up to 8 layers: the layer
  thickens, you are not pushed up; walking onto a full layer from outside still steps up. (The old brief's "VTD-54" is
  a different test, block drops.)

---

## seasoning #10: Check the salt swap is complete; check fermenting
Status **In Progress** · Work: KubeJS · id `food/seasoning #10`

### Goal
No recipe, loot or catch still takes or gives a pre-merge salt item, and Vintage fermenting works with the merged salt.

### Now
- SQ-SALT-01 (2026-10-28) found two leftovers; fixed 2026-10-28 in CD
  [vintagedelight/recipes.js](../../kubejs/data/creative_delight/kubejs/vintagedelight/recipes.js) (froglight
  fermenting on The Salt's block; Tactical Fishing salt bucket and powdered sugar bucket catches removed).
- The retest ran as **SQ-CM-10 in r1031 and PASSED 2026-10-31** (scratch_run_2026-10-31.md section 8 PASS list; the
  row checks the three froglight recipes on `salt:salt_block` and both fishing recipes absent).
- Fermenting: Vintage pickling on `#forge:salt` PASSED 2026-10-25 (SQ-VTD4-02).
- Pack scripts: the only pre-merge salt id left is the CD fallback in salt.js line 13 (`i.salt_item` when the Vintage
  member is off), by design.
- Not run: the salting craft and cauldron evaporation halves of SQ-SALT-02 / -03 (client block CQ-SALT, r1031 deferred);
  MG-32, MG-33.

### Build
Nothing left to build. Run the client checks, then move the row to Testing or Complete.

### Tests
- MG-32 (salt a food with salt dust), MG-33 (cauldron evaporation drops salt dust), the CQ-SALT client rows.

---

## seasoning #5: Sugar as a seasoning
Status **In Progress** · Work: KubeJS · id `food/seasoning #5`

### Goal
Sugar (or powdered sugar) seasons foods through the pack framework.

### Now
- Defined in seasonings.js: `sugar: { item: [i.sugar, i.powdered_sugar], nutrition: 1, families: [], add: [] }`
  (+1 nutrition placeholder). No foods since 2026-10-26 (the Create Factory waffle test was dropped; popcorn + sugar
  stays Corn Delight's caramel popcorn), so no tag and no craft (`hasFoods` in seasoning.js). FO-53 checks that.
- `i.powdered_sugar` = `creativeengineering:powdered_sugar` (item_dict.js 1850; food.md 3.13). The overlay exists
  (`kubejs/assets/creativeengineering/textures/item/seasoned/sugar.png`).

### Build
1. Pick the foods (decision) and set `families` in the sugar entry, e.g. `['fruit/whole', 'fruit/slice',
   'bread/slice', 'dessert/waffle', 'dessert/pastry']`; `remove` anything already sweet that should not take it.
2. Set the real bonus (decision); add `combines` if sugar may share a food with another seasoning (none today).
3. Restart (startup definition), `/reload` is not enough. Update food.md 3.12 test-set paragraph and FO-53.

### Decisions
- **Which foods:** recommend fruit (whole and slices), toast / bread slices, waffles and pastries; not drinks (the
  drinks rows own sweetening) and not popcorn (caramel popcorn exists).
- **Nutrition vs saturation:** recommend saturation +1 (sugar is quick energy, little filling), placeholder until
  `food/types-and-tuning #1` sets per-family values.

### Tests
- **FO-new1** (proposed): an apple + sugar gives a Sugar line "+1 saturation"; popcorn + sugar still gives caramel
  popcorn only; replaces FO-53 when foods are set.

---

## seasoning #3: New seasonings
Status **In Progress** · Work: KubeJS / patcherMod · id `food/seasoning #3`

### Goal
Pepper, dill (sea pickle?), cheese, cream and onion, ice cubes for cold drinks; distinct placeable popcorn variants
(regular popcorn placeable first); real values.

### Done
- Framework BUILT 2026-10-23 (food.md 3.12), foods by family 2026-10-26; shipped 1.1.03. Test set on popcorn:
  cheese, onion, garlic (2026-10-23), void pepper (2026-10-26, Ender's Delight Additions only, FO-57). Scratch:
  SQ-SN-00, -01 and SQ-SNF-01 to -03 PASS.
- **Pepper** is partly covered: Void Pepper (`i.crushed_voidpepper`, item_dict.js 12056) is a seasoning, and
  `#creativeengineering:spices` (`i.t_spices`, 12079) holds `voidpepper` and `crushed_voidpepper` (CD
  `endersdelight/tags.js` line 15). BBQ's Pepper Powder comes with the port (row 11).

### Now (left)
- No dill, cream-and-onion powder or ice seasoning item. `i.ice_cubes` = `neapolitan:ice_cubes`,
  `i.t_ice_cubes` = `#forge:ice_cubes` (item_dict.js 12657, 1894).
- Popcorn (`corn_delight:popcorn`) is not placeable (2026-10-30 placeable dump: no `placeable` tag); Corn Delight's
  `popcorn_box` is.
- Every seasoning is on popcorn only: no real families yet, so "real values" also waits for foods.

### Build
1. **Foods first:** give cheese, onion, garlic, void pepper real `families` (decision), one seasoning per pass; rerun
   the seasonable tags (restart).
2. **Dill:** item `creativeengineering:dill` in CD `kubejs_startup/creativeengineering/` (startup registry, dict key
   `i.dill`), source by decision; seasoning entry with `families: ['fish/cooked', 'fish/cooked_cut', 'preserve/pickle']`.
3. **Cream and onion:** powder item `creativeengineering:cream_onion_powder` (`i.cream_onion_powder`), heated mixing
   milk + onion via `R.createMixing`, plus a `_manual_only` shapeless; seasoning on `snack` (crisps, popcorn).
4. **Ice for cold drinks:** a seasoning `ice` with `item: i.t_ice_cubes` on `drink/juice`, `drink/soda`,
   `drink/tea` minus hot ones (`remove`); bonus decision. Needs drinks to be seasonable (they are foods with
   `useAnim` DRINK; check `Seasonings.onEaten` fires for them, `LivingEntityUseItemEvent.Finish` does).
5. **Popcorn variants:** regular popcorn placeable first (`food/placeable`, [food_placeable.md](food_placeable.md));
   then salted / cheese / caramel popcorn boxes as their own block items (CD, Display Delight style).
6. Each new seasoning: lang name (`seasoning_lang.js` reads the definitions), overlay row in `make_seasoning.py`.

### Decisions
- **Nutrition vs saturation per seasoning:** recommend salt = nutrition (as now), pepper = saturation, cheese =
  saturation (as now), cream and onion = saturation, ice = no food value, a short effect instead (Fire Resistance 10 s
  or nothing).
- **Dill source:** recommend a cutting-board product of sea pickles (the row's hint) over a new crop.
- **NBT seasonings vs new items:** NBT for dishes, new block items only for popcorn variants.

### Gotchas
- Seasoned food doesn't stack with plain food and loses the seasoning as a recipe input (food.md 3.12 limits).
- With the cap of row 11 (default 2) every new seasoning competes for the same two slots.

### Tests
- **SE-06 Pepper** (2026-10-21, proposed): a food in void pepper's families + Crushed Void Pepper -> the bonus line;
  eating it gives the extra values.
- **FO-new2** (proposed): Dill on cooked cod; Cream and Onion on popcorn; Ice on a juice; each line shows, the bonus
  applies, popcorn variant boxes place.

---

## seasoning #11: Seasoning fold: Barbeque's Delight seasonings into the pack system
Status **Planned** · Work: patcherMod / KubeJS · id `food/seasoning #11`

### Goal
The six BBQ seasonings (cumin, pepper, chili powders; honey mustard, buffalo, barbeque sauces) become pack
seasonings: defined once in seasonings.js, applied by shapeless crafts, with a configurable cap per food.

### Now
- Not started; depends on the port (`largecontent/mod-additions #37`, nothing built, no `backportedBarbeques`
  directory). The port ships the six as upstream durability items (64 / 16 uses, click on a skewer on the grill or
  cutting board, one per skewer, name prefix), barbeques_delight.md 3.3.
- Framework gaps (barbeques_delight.md 3.3.1): `Seasonings.Definition` (Seasonings.java line 59) has `nutrition`,
  `saturation`, `heal`, `effects`, `combines`; no fast eating, always edible, damage on eating or effect transform.
  `Seasonings.canAdd` (lines 138-152) checks only `combines`, no count cap. CEConfig has no `[food]` section
  (config/creativeengineering-common.toml has `[jei]` at line 1917, `[seasons]` 1991, no food section).
- Decisions taken 2026-10-31 (seasonings.md 2, barbeques_delight.md 4.1, 4.3): cap default 2, `grilled` not counted;
  crafts consume one item; sources give about the old use count (4.3 #1); powders may go on raw skewers and copy to
  the cooked one, sauces may not; name prefix becomes a tooltip line.

### Build
1. **patcherMod `food/Seasonings`**: new optional fields in `define` JSON and `Definition`:
   `fast` (bool), `alwaysEdible` (bool), `damage` ({ `type`: fire, `amount` }), `transform` ({ `chanceMul`,
   `durationMul`, `amplifierAdd` }). Apply:
   - `fast`, `alwaysEdible`: these change how the item is eaten, so they cannot wait for `onEaten`. Mixin on
     `Item.getUseDuration` (half when any seasoning is `fast`) and on `Player.canEat` / `Item.use` for
     `alwaysEdible`, both reading `Seasonings.of(stack)`.
   - `damage`: in `onEaten`, `player.hurt(DamageSource.ON_FIRE, amount)` (check the shield damage tags in
     `damage_tags/` classify it like BBQ's).
   - `transform`: in `onEaten`, before the food's own effects are applied, or by removing and re-adding them:
     cleanest is a mixin on `LivingEntity.addEatEffect` (vanilla applies `FoodProperties.getEffects()` there) that
     rewrites each pair through the transforms of the stack's seasonings.
2. **Cap**: CEConfig `[food] maxSeasonings = 2` and `grilledCountsTowardMax = false` (new section);
   `Seasonings.canAdd` refuses when the count of non-`grilled` seasonings would pass the cap. Run the jar once so Forge
   writes the keys, then commit config/creativeengineering-common.toml and copy it to
   `configs_edited/root_config/creativeengineering-common.toml`.
3. **Tooltip / AppleSkin / JEI**: `SeasoningTooltip` lines for the new kinds ("Pepper Powder: eats fast", "Chili
   Powder: always edible, 1 heart fire damage", "Barbeque Sauce: effects x2 duration"); `AppleSkinSeasonings` shows
   nothing new (no hunger values).
4. **Definitions** (seasonings.js): six entries, ids `barbequesdelight:<item>` kept as the item ids, `needs:
   'barbequesdelight'` (the member key), `families: ['skewer']`, `combines` per seasonings.md 4 (decision).
5. **Retire the click use** in the port: remove the cutting-board mixin and the grill click handler, make the six
   plain stackable items (stack 64), keep BBQ's upstream NBT reader only if old saves matter (they don't: not shipped).
6. **Sources scaled** (row 12): every craft and trade that gives a BBQ seasoning gives 64 powders or 16 sauces
   (4.3 #1 default).
7. Docs: food.md 3.12 (new kinds, cap), seasonings.md 1 (Now columns), barbeques_delight.md 4.1 -> built.

### Ids and data
Items `barbequesdelight:cumin_powder`, `pepper_powder`, `chili_powder`, `honey_mustard_sauce`, `buffalo_sauce`,
`barbeque_sauce` (upstream ids, barbeques_delight.md 3.8; unverified until the port exists). New item_dict keys with
the port. CEConfig keys proposed, not present.

### Decisions
- **Sauce pairs**: recommend seasonings.md 4 (sauces exclude each other; each powder pairs with one sauce).
- **Transform on foods with no effects** (cod, salmon, chicken, mushroom skewers, B3): recommend the craft refuses a
  sauce on a food with no effects, instead of upstream's name-only result.

### Gotchas
- `fast` and `alwaysEdible` act before eating, unlike every current kind; they need client and server agreement
  (NBT is synced with the stack, so both sides read it).
- A combo (wrap, sandwich, bibimbap) has no seasoning logic (barbeques_delight.md 3.3.1): the craft loses the
  skewer's seasonings unless `dishes/meals #46` carries the list.

### Tests
- **FO-new3** (proposed): Pepper Powder skewer eats in half the time; Chili Powder skewer is edible at full hunger and
  deals 1 heart of fire damage; Barbeque Sauce doubles the skewer's effect duration; a third seasoning is refused with
  the default cap; `maxSeasonings = 3` allows it after a restart. The skewer side: BBQ-01 to BBQ-21 (drafted,
  barbeques_delight.md 8; 4.3 #14 plans more).

---

## seasoning #13: `grilled` seasoning type
Status **Planned** · Work: patcherMod / KubeJS · id `food/seasoning #13`

### Goal
A skewer cooked on the BBQ Grill carries `grilled` (+2 saturation, capped at the food's hunger); the shapeless cooked
recipe does not add it; it does not count toward the cap unless configured.

### Now
- Nothing built. No framework kind for "applied by a station": every seasoning is applied by `SeasoningRecipe`.
  `saturation` already exists (absolute points capped at the food level, food.md 3.12), so the bonus itself needs no
  new code.
- Depends on row 11 (cap and `grilledCountsTowardMax`), the port's Grill block entity, and the skewer fold
  (`dishes/meals #46`, `#50` raw pack skewers).

### Build
1. seasonings.js: `grilled: { id: 'creativeengineering:grilled', name: 'Grilled', saturation: 2, station: true,
   families: [], add: [] }`. `station: true` = no craft (seasoning.js skips it, as `hasFoods` does) and no item.
2. patcherMod `Seasonings`: a `station` flag on `Definition`; `canAdd` ignores station seasonings for the cap unless
   `[food] grilledCountsTowardMax`; a public `Seasonings.with(stack, List.of(GRILLED))` call for the station.
3. Port Grill block entity: when a raw skewer finishes (after the flip), the result stack gets
   `Seasonings.with(result, grilled)` plus the raw skewer's own powders copied (barbeques_delight.md 4.1, O28). The
   port depends on patcherMod, or calls it reflectively (decide in the port).
4. Tooltip "Grilled: +2 saturation" and AppleSkin come free from the existing handlers (`SeasoningTooltip`,
   `AppleSkinSeasonings`), plus an overlay row in `make_seasoning.py` if it should show (decision 4.3 #9: tooltip yes).

### Decisions
- barbeques_delight.md 4.3 #3 (+2 points, capped: default), #9 (tooltip line: default yes), #10 (combos carry each
  skewer's `grilled`: default yes), #12 (one line + AppleSkin: default).
- **Icon overlay:** recommend none (a grilled skewer already looks cooked).

### Tests
- **FO-new4** (proposed): a raw beef skewer grilled shows "Grilled: +2 saturation" and AppleSkin +2; the shaped
  cooked skewer has no line and does not stack with it; a grilled skewer still takes two more seasonings.

---

## seasoning #12: Seasoning sources and villager trades
Status **Planned** · Work: KubeJS · id `food/seasoning #12`

### Goal
Each BBQ seasoning has a temporary source when the port ships (no butcher trades, bug B2), then one pass over
villager trades for every seasoning (BBQ's six, pack seasonings, salt), one level spread.

### Now
- The port drops BBQ's 6 butcher trades (barbeques_delight.md 2.3, 3.8); the "Source" column of 3.8 is "owner:
  temporary" for all six.
- Trades: pack trades in [economy/trades.js](../../kubejs/server_scripts/economy/trades.js) (MoreJS
  `MoreJSEvents.villagerTrades`, `allLevels` range helper lines 13-16); food-mod trades in CD trees by mod
  (`kubejs/data/creative_delight/kubejs/<mod>/trades.js`, e.g. corn_delight, rusticdelight, veggiesdelight). Salt's own
  butcher trades are on (`ButcherSaltTradesEnabled = true`, salt-common.toml; 4 `salt:salt` trades in the
  2026-10-30 trade dump, swapped to salt dust by the merge). Pack seasonings come from crops or crafts (cheese,
  onion, garlic, sugar, void pepper); no trades.

### Build
1. **Temporary source (with the port):** shapeless crafts in CD `kubejs/data/creative_delight/kubejs/barbequesdelight/recipes.js`
   via `global.NCH`: cumin powder from `i.t_spices` / seeds on the mortar or cutting board, pepper powder from
   `i.crushed_voidpepper` or bell pepper seeds, chili powder from `#forge:chilipepper` (`i.t_chilipepper`, item_dict.js
   2035), the sauces from their namesakes (honey + a mustard source: the pack has no mustard item, decision; chili +
   butter; tomato sauce + sugar). Before the fold, one craft
   gives one durability item; after it, the count of 4.3 #1.
2. **Trade pass** (after row 11): `kubejs/data/creative_delight/kubejs/barbequesdelight/trades.js` for the six on the
   butcher (levels 2 to 4), cheese / onion / garlic on the farmer, salt left to Salt's own trades or replaced by a
   pack trade on the fisherman / butcher at level 1 (`ButcherSaltTradesEnabled = false` then, configs_edited copy).
3. Rerun `/dumpall` for `villager_trades.json`; check every trade names an item that exists (no `salt:salt`).

### Decisions
- **Temporary source:** owner (TODO Decisions column). Recommend the crafts in step 1 (no world gen, no trades).
- **Professions, levels, prices:** recommend one seasoning per level band so no profession sells all six; prices
  near the craft's ingredient value.

### Tests
- **FO-new5** (proposed): JEI shows a recipe for each of the six; after the trade pass, a level 2 to 4 butcher
  offers BBQ seasonings and no trade gives or takes `salt:salt`.

---

## Built, needs play
- `seasoning #2` (Testing): Seasoning JEI page; [food.md](../food.md) 3.11; FO-30, FO-31 (C P).
- `seasoning #6` (Testing): salt merge, all salt Vintage pink, Salt Block ticks; [item_merges.md](../item_merges.md)
  2.9; MG-29 to MG-38; SQ-SALT-01 retest PASSED 2026-10-31 as SQ-CM-10; open: salting craft and cauldron halves
  of SQ-SALT-02 / -03 (S~ C P).
- `seasoning #8` (Testing): pack AppleSkin handler after Salt's (`LOWEST`); [food.md](../food.md) 3.12; FO-54 (C P).
- `seasoning #9` (Testing): seasoned icon overlays (`SeasonedItemModel`, `make_seasoning.py`, Salt's overlay off);
  [food.md](../food.md) 3.12; FO-55 (C P).
