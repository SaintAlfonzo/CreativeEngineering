# Brief: dishes / pickles-and-ferments

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# dishes`, Sub `pickles-and-ferments` (rows 1 to 10, all from the
scratchpad); rechecked 2026-10-31 (row 11 and `merges/dishes #9`, the former row 8, added; built rows condensed). Layout per
[README.md](README.md). Writeups: [vintage_delight.md](../vintage_delight.md) (2.7 recipes, "Planned from the scratchpad"
and "Built 2026-10-25" in section 5, tests in 8; prefix VTD-), [ocean_delight.md](../ocean_delight.md) 5.4,
[veggies_delight.md](../veggies_delight.md). Test ids VTD-56 and VTD-59 to VTD-64 below were proposed here on 2026-10-24
and are not in the writeup yet (VTD-65 to VTD-74 there are other checks).

**Where the code goes** (memory `creative-delight-routing`): food code lives in the Creative Delight trees by mod: server
`kubejs/data/creativedelight/kubejs/vintagedelight/` (`recipes.js`, `cooking.js`, `tags.js`, `jams.js`), Ocean's / Oceanic
`.../oceansdelight/`, startup `development/creativeDelight/src/main/resources/kubejs_startup/vintagedelight/`
(`vintage_items.js`, `item_merges.js`, `compost.js`). Pickling jar recipes through `R.vdPickling(output, inputs, e, id, opts)`
(neoCraftHelper.js line 1217; opts `time`, `secondary`, `container`), pot through `R.fdCooking` (1074), grinding through
`R.foodGrinding` (1602). Tags through `global.tags` (`t.add` / `t.remove`), ids from `global.item`.

**Shared Now** (2026-10-31; Vintage is the port `development/backportedVintage`, recipes in
`src/main/resources/data/vintagedelight/recipes/fermenting/`; Brewin' is the Creative Brewing member
`development/creativeBrewing/members/brewin`):
- Jar recipes: `pickle_from_fermenting`, `pickled_egg_from_fermenting` (5 eggs + salt), `century_egg_from_fermenting`
  (`#forge:eggs` + coal + `#forge:salt`, 216000 ticks), `kimchi_from_fermenting` (4 cabbage leaves + salt + `#forge:chilipepper`
  -> 4 + a pickled pepper), `vinegar_from_jam` (`#forge:jam_jars` + `#forge:mushrooms` -> vinegar jar + 3 organic mash, 2400),
  `vinegar_from_jam_bottles` (`#forge:jam_bottles` + mushrooms -> vinegar bottle + 1 organic mash, 1200). None of these is
  overridden in CD today.
- `kubejs/server_scripts/cooking/alcohol.js` line 15 removes Brewin's keg kimchi, line 16 adds the jar copy
  `brewin_kimchi` (cabbage + `i.vegetables` + kelp -> 2 `i.kimchi`), line 19 removes the pickled pickles honey recipe.
- `#forge:sea_pickles` (`i.t_sea_pickles`) = `minecraft:sea_pickle` + Oceanic `sea_pickle_slices`. No `#forge:pickles` tag.
  `#creativeengineering:pickled_veg` (`i.t_pickled_veg`, CD `vintagedelight/tags.js` line 18) = pickle, pickled sea pickle,
  pickled beetroot / onion / pepper, both kimchis.

## Scope and order

| Row | Change | Status | Note |
|---|---|---|---|
| merges/dishes #9 | Merge kimchis | In Progress | merge pair + one recipe; independent |
| 2 | Pickle recipes take real pickles only | In Progress | needs the per-recipe list below confirmed |
| 9 | Pickled egg for the century egg | In Progress | one recipe |
| 5 | Enzymes from mushrooms | In Progress | new item; before 6 |
| 6 | Vinegar craft | In Progress | needs 5; reconcile with 11 |
| 11 | Vinegar: drop bottle recipe, any jam | Planned | same recipes as 6 |
| 7 | Foul Taste on vinegar foods | In Progress | needs an effect and a way to mark "vinegar added" |
| 1, 3, 4, 10 | Pickled sea pickle; pickle soup; braised sea pickle slices; garlic honey | Testing | see Built, needs play |

---

## pickles-and-ferments #2: Pickle recipes take real pickles only
Status: In Progress / Work: KubeJS / TODO `dishes/pickles-and-ferments #2`

### Goal
Recipes that mean a pickle take `vintagedelight:pickle` (`i.pickle`): not Pickled Pickles (merged into Pickled Sea Pickle
since 1.1.03) and not raw sea pickles. Squid and pickles swaps the onion for a real pickle. More pickled options in the
cucumber salad belong to `dishes/meals #29`.

### Now (dump `kubejs/exported/food/recipes.json`, 2026-10-30; nothing built for this row)
Recipes taking a sea pickle, by role:
- Pickle stand-ins (swap): `oceanic_delight:squid_and_pickles` (shapeless: grilled squid tentacles, now cooked calamari via
  CD `oceansdelight/recipes.js` line 43; baked potato; `#forge:sea_pickles`; `#forge:crops/onion`; bowl),
  `oceanic_delight:potato_salad` (2 potatoes, `#forge:sea_pickles`, `#forge:crops/cabbage`, bowl), Hostile Delight
  `gsandrecp` guardian sandwich (bread, cooked guardian fillet, kelp, `minecraft:sea_pickle`).
- Sea food (keep the sea pickle): `sea_pickle_roll`, `sea_salad`, `seafood_skewer`, `stuffed_pufferfish` (Oceanic),
  `braised_sea_pickle` (Ocean's, row 4), Hostile `mcocktrecp` marine cocktail, `cucumbertearecp`.
- Already real pickles: Veggies' compat `veggiesdelight:compat/vintagedelight/cooking/pickle_soup` (`i.pickle` only; see Built,
  row 3).

### Build
1. Tag: `i.t_pickles = '#forge:pickles'` in item_dict.js (tag block near `i.t_pickled_veg`, line 1842); CD
   `vintagedelight/tags.js`: `t.add(e, i.t_pickles, i.pickle)`.
2. CD `oceansdelight/recipes.js` (Oceanic branch, after the calamari swaps): `R.replaceInput({ id: 'oceanic_delight:squid_and_pickles' },
   i.t_sea_pickles, i.t_pickles, e)` and the same for the onion slot (`#forge:crops/onion` -> `i.t_pickles`, so 2 pickles;
   or drop the onion, see Decisions); `potato_salad` the same sea pickle swap.
3. Hostile Delight: guardian sandwich `R.replaceInput({ id: 'hostiledelight:gsandrecp' }, i.sea_pickle, i.t_pickles, e)` in a CD
   `hostiledelight/` server script (folder exists).
4. Changelog bullet (dishes that named pickles now take a pickle).

### Decisions
- Which recipes are pickle stand-ins: recommend the three listed (squid and pickles, potato salad, guardian sandwich); confirm.
- Squid and pickles' onion: recommend a second pickle in that slot (the row says swap the onion for a real pickle; the
  sea pickle slot becomes a pickle too).

### Tests
VTD-56 (proposed): JEI shows squid and pickles, potato salad and the guardian sandwich with a pickle, never a sea pickle,
pickled sea pickle or onion; the sea pickle roll and sea salad still take sea pickles.

---

## pickles-and-ferments #5: Enzymes from crushed or milled mushrooms
Status: In Progress / Work: KubeJS / TODO `dishes/pickles-and-ferments #5`

### Goal
Crush or mill mushrooms into an enzyme, the vinegar craft's ferment starter (row 6).

### Now
No enzyme item (2026-10-31: no `enzyme` in item_dict.js, CD startup or scripts). `#forge:mushrooms` (dump) = 16 Botania
mushrooms, Fungi Delight's caps, morel, inky cap, truffle and slices, brown and red, Quark's glow shroom; the vinegar recipes
take this tag today.

### Build
1. Item: `i.enzyme = 'creativeengineering:enzyme'` (pack block of item_dict.js); registered in CD startup
   `vintagedelight/vintage_items.js` (`e.create(i.enzyme).displayName('Enzyme')`, not food); texture in
   `kubejs/assets/creativeengineering/textures/item/enzyme.png` (palette swap of a powder, `textureswapper/`).
2. Input tag `i.t_enzyme_mushrooms = '#creativeengineering:enzyme_mushrooms'` in CD `vintagedelight/tags.js` per Decisions.
3. CD `vintagedelight/recipes.js`: `R.foodGrinding(i.enzyme, i.t_enzyme_mushrooms, e, 'enzyme')` (crushing wheels, millstone,
   mortar).
4. Compost value and JEI info line; changelog bullet.

### Decisions
- Which mushrooms: recommend brown, red, the Fungi Delight mushrooms and crimson / warped fungus ("crushed fungus" in row 6);
  leave Botania's coloured mushrooms out (they are Botania's flower ingredients).
- Which machines: recommend what `foodGrinding` gives (crushing wheels, millstone, mortar); skip the IE crusher.

### Tests
VTD-59 (proposed): crushing wheels, millstone and the mortar turn a brown mushroom and a crimson fungus into an enzyme; a
Botania mushroom does not.

---

## pickles-and-ferments #6: Vinegar craft
Status: In Progress / Work: KubeJS / TODO `dishes/pickles-and-ferments #6`

### Goal
The vinegar craft becomes 3 apple mash plus an enzyme.

### Now
Not built. Vinegar recipes as in Shared Now (jam jar or jam bottle + mushroom). `i.vinegar` = `vintagedelight:vinegar_bottle`,
`i.vinegar_jar`; the jar and bottle convert by `vintagedelight:jam/vinegar_jar` and `_deconstruct`. `i.apple_mash` =
`vinery:apple_mash` (Vinery port, Creative Brewing; apple press). `i.organic_mash` comes only from the two vinegar recipes
(vintage_delight.md 2.7) and feeds organic compost.

### Build
1. CD `vintagedelight/recipes.js`: `R.remove` `vintagedelight:fermenting/vinegar_from_jam` (and `_bottles`, row 11).
2. `R.vdPickling(i.vinegar_jar, [Item.of(i.apple_mash, 3), i.enzyme], e, 'vinegar_from_apple_mash', { secondary: Item.of(i.organic_mash, 3) })`
   (jar output keeps the container rule of the old recipe; check whether the jar needs a mason jar input).
3. Row 11's jam route if kept (see there). Changelog bullet.

### Decisions
- Keep organic mash as a secondary output: recommend yes (its only source).
- Replace the jam route or add beside it: row 11 asks for "any jam", so recommend two routes: 3 apple mash + enzyme, and any
  jam + enzyme (the enzyme replaces the raw mushroom in both).

### Gotchas
Apple mash only exists with the Vinery port loaded (Creative Brewing): guard with `Platform.isLoaded('vinery')`.

### Tests
VTD-60 (proposed): Pickling Jar: 3 apple mash + enzyme -> vinegar jar + organic mash; a raw mushroom no longer works.

---

## pickles-and-ferments #11: Vinegar: drop bottle recipe, accept any jam
Status: Planned / Work: KubeJS / TODO `dishes/pickles-and-ferments #11` (related `#6`)

### Goal
Remove the bottle vinegar recipe; any jam makes vinegar in the jar.

### Now (2026-10-31)
- `vinegar_from_jam_bottles` still loads; since the jam bottles were removed (CD startup `vintagedelight/vintage_items.js`
  line 47) `#forge:jam_bottles` holds only `minecraft:honey_bottle` and `vintagedelight:apple_sauce_bottle`.
- `vinegar_from_jam` takes `#forge:jam_jars`: the port's tag (`backportedVintage/.../data/forge/tags/items/jam_jars.json`)
  minus the removed sweet / glow berry jars and the relish jar (CD `vintagedelight/jams.js` line 14) = pepper jam, apple
  sauce, honey, green grape, red grape, cherry jars.
- Not covered: the 22 Fruits Delight jellies, which are in `#forge:jams` (`i.t_forge_jams`; CD `fruitsdelight/tags.js`
  line 17); the grape and cherry jars are in both tags.

### Build
1. CD `vintagedelight/recipes.js`: `R.remove({ id: 'vintagedelight:fermenting/vinegar_from_jam_bottles' }, e)`.
2. Jam slot: `R.replaceInput({ id: 'vintagedelight:fermenting/vinegar_from_jam' }, i.t_jam_jars, [i.t_jam_jars, i.t_forge_jams], e)`
   (or a new tag `#creativeengineering:vinegar_jams` = both, in CD `vintagedelight/tags.js`), unless row 6 rebuilds the recipe;
   then build its jam route with the same input.
3. Changelog bullet (vinegar bottle recipe removed; any jam ferments into vinegar).

### Decisions
- Honey jar and apple sauce jar count as jams (they are in `#forge:jam_jars`): recommend keep.
- Build with row 6 (same recipe): recommend yes.

### Gotchas
Fruits Delight jellies are bowl / bottle items, not mason jars; check the jar's container return so a jelly does not leave
a mason jar behind.

### Tests
VTD-new1 (proposed, numbered when queued): JEI has one jam vinegar recipe and it cycles a Fruits Delight jelly; no bottle
vinegar recipe.

---

## pickles-and-ferments #7: Foul Taste on foods with vinegar added
Status: In Progress / Work: KubeJS / patcherMod / TODO `dishes/pickles-and-ferments #7`

### Goal
Foods that get vinegar added carry a Foul Taste effect, on more foods than today.

### Now (2026-10-31)
- No Foul Taste effect anywhere (`effect_dict.js`, `kubejs/exported/mob_effects.json`, patcherMod `potion/`). Candidates
  that exist: Fruits Delight `fruitsdelight:disgusted` (Disgusted), vanilla Nausea.
- Vinegar as an addition: 80 cans in `kubejs/generation_scripts/food/can_types.json` have a `_vinegar` recipe
  (`tools/gen_can_types.py` lines 71-84: raw veggie, raw fruit, raw fish and more; salads 5 + vinegar). The `_vinegar` recipe
  makes the same can item as the plain and salt recipes.
- Foods with vinegar as an ingredient: only Vintage's cheese curds (and the leather / froglight jar crafts, not food).

### Build
1. Effect: per Decisions. A new one is a patcherMod `MobEffect` (pattern `potion/MagicResistanceEffect.java`), id
   `creativeengineering:foul_taste`, JEED text per [effects.md](../effects.md).
2. Marking "vinegar added": the can item cannot tell, so either (a) the `_vinegar` recipes output the can with an NBT flag and a
   KubeJS `ItemEvents.foodEaten` handler (CD `creativeengineering/canning.js` area) applies the effect, or (b) the effect goes on
   every food whose recipe includes vinegar as an ingredient (cheese curds today, and any dish built later).
3. JEED provider (jeed_providers.js) and tooltip line; changelog bullet.

### Decisions
- Which effect: recommend reuse Disgusted unless the owner wants a pack icon.
- Which foods: recommend (b) only, plus pickled foods if wanted; (a) splits stacks (memory `empty-tag-stacking`: an NBT can
  never stacks with a plain one) and makes vinegar a penalty additive nobody picks.

### Gotchas
Can effects are generated (`can_types.json`); a per-item effect added by hand is overwritten by the next `/cegen` unless
the generator learns it.

### Tests
VTD-61 (proposed): eat a food made with vinegar: the chosen effect for the decided time; the same food without vinegar
(or a salt can): none.

---

## merges/dishes #9: Merge kimchis
Status: In Progress / Work: KubeJS / data / TODO `merges/dishes #9` (was `dishes/pickles-and-ferments #8`;
[item_merges.md](../item_merges.md) section 5, [brief](merges_dishes.md))

### Goal
One kimchi: Vintage Delight's (item and texture) survives, the Brewin' jar copy of its brewing goes, and any pickleable
veggie works as the base in an alternative recipe.

### Now (2026-10-31, not built)
- `i.kimchi` = `brewinandchewin:kimchi` (Creative Brewing's Brewin' member), `i.vintagedelight_kimchi` = `vintagedelight:kimchi`.
- `alcohol.js` line 15 removes the keg recipe (the member still ships `data/brewinandchewin/recipes/fermenting/kimchi.json`);
  line 16 is the jar copy `brewin_kimchi` (cabbage + `i.vegetables` + kelp -> 2).
- Vintage's own: 4 cabbage leaves + salt + chili -> 4 + pickled pepper (2400 ticks).
- Brewin's kimchi also sits in CD `brewinandchewin/tags.js` line 20, `vintagedelight/tags.js` line 19 (`pickled_veg`), CD
  startup `brewinandchewin/compost_values.js` line 17, `tools/gen_can_types.py` line 263 (`canned_kimchi` from both).

### Build
1. Merge: CD startup `vintagedelight/item_merges.js`, add `[i.kimchi, i.vintagedelight_kimchi]` behind
   `Platform.isLoaded('brewinandchewin')` (the merge swaps recipes and loot, hides the item, rescues inventories).
2. Recipes: delete `alcohol.js` line 16 (`brewin_kimchi`); keep line 15. Alternative recipe in CD `vintagedelight/recipes.js`:
   `R.vdPickling(Item.of(i.vintagedelight_kimchi, 4), [Item.of(i.t_pickleable_veg, 4), i.salt, i.t_chilipepper], e, 'kimchi_from_veg',
   { secondary: i.pickled_pepper })` (`i.t_chilipepper`, item_dict.js line 2035), new tag `i.t_pickleable_veg` in CD
   `vintagedelight/tags.js`.
3. Clean-up: drop `i.kimchi` from the two tag lines and the compost row; `gen_can_types.py` line 263 to one source; `/cegen`.
4. item_merges.md 2.x section, SQ-MG- queue entries (next free number), changelog bullet.

### Decisions
- Pickleable veggies: recommend `#forge:vegetables` (38 items: carrot, potato, beetroot, onion, cucumber, peppers, broccoli,
  cauliflower, turnip, zucchini, garlic...) minus peanut and plain potato slices, plus cabbage leaf and kelp (Brewin's base).

### Tests
VTD-62 (proposed): JEI shows one Kimchi with Vintage's texture; 4 carrots + salt + chili make kimchi in the jar; no
`brewin_kimchi` recipe; an old Brewin' kimchi in an inventory becomes Vintage's.

---

## pickles-and-ferments #9: Pickled egg for the century egg
Status: In Progress / Work: KubeJS / TODO `dishes/pickles-and-ferments #9`

### Goal
The century egg is made from a pickled egg.

### Now
Not built (2026-10-31: no `century_egg` line in CD scripts). Recipe as in Shared Now (`#forge:eggs` + coal + salt, 216000
ticks; "any egg" decided 2026-10-23, vintage_delight.md 2.7, VTD-35). `i.pickled_egg`, `i.century_egg`, `i.coal` exist.

### Build
CD `vintagedelight/recipes.js`: `R.remove({ id: 'vintagedelight:fermenting/century_egg_from_fermenting' }, e)`, then
`R.vdPickling(i.century_egg, [i.pickled_egg, i.coal], e, 'century_egg_from_fermenting', { time: 216000 })`. Changelog bullet.
Update VTD-35 in vintage_delight.md 8.

### Decisions
- Salt in the craft: recommend drop it (the pickled egg already took salt).

### Tests
VTD-63 (proposed): pickled egg + coal -> century egg after 3 hours; a raw egg no longer works. Supersedes VTD-35.

---

## Built, needs play

- `dishes/pickles-and-ferments #1` Remove pickled pickles; pickled sea pickle: [vintage_delight.md](../vintage_delight.md) "Built 2026-10-25"; open: S~ (SQ-CDB-08 JEI, SQ-CDB-12 cans after `/dumpfood` + `/cegen`), C P (VTD-55); Canned Pickled Pickles rename waits for the can regeneration.
- `dishes/pickles-and-ferments #3` Pickle soup: [vintage_delight.md](../vintage_delight.md) "Built 2026-10-25"; open: C P (VTD-57). Veggies' compat pickle soup (`veggiesdelight:compat/vintagedelight/cooking/pickle_soup`, pickle + carrot + potato + turnip or beetroot) is still in the 2026-10-30 dump beside the new recipe.
- `dishes/pickles-and-ferments #4` Braised sea pickle accepts slices: [ocean_delight.md](../ocean_delight.md) 5.4; open: C P (VTD-58).
- `dishes/pickles-and-ferments #10` Fermented garlic honey effects: Built 2026-10-28 (CD startup `veggiesdelight/veggies_foods.js` lines 15-22: Regeneration II 5 s, Health Boost II 30 s, owner chose II), shipped 1.1.04; SQ-CM-05 PASS; open: P T (VTD-64 proposed: drink it, both effects and lengths, tooltip and JEED agree). Fold into [veggies_delight.md](../veggies_delight.md) (drinks table line 118 still says 5 s).
