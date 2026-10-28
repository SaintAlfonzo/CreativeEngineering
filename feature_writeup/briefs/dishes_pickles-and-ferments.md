# Brief: dishes / pickles-and-ferments

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# dishes`, Sub `pickles-and-ferments` (rows 1 to 10, all from the
scratchpad). Layout per [README.md](README.md). Writeups: [vintage_delight.md](../vintage_delight.md) (§2.3 pickling jar,
§2.7 recipes, §3.1 overlaps), [ocean_delight.md](../ocean_delight.md). Pickling recipes go through
`R.vdPickling` (`neoCraftHelper.js` line 1139; opts `time`, `secondary`, `container`), ids from `global.item`, tags through
`global.tags` in [cookingtags.js](../../kubejs/server_scripts/tags/cookingtags.js).

**Shared Now (verified 2026-10-24 against `development/backportedVintage/src/main/resources/data/vintagedelight/recipes`,
the Oceans' / Oceanic Delight jars and `alcohol.js`):**
- Pickling jar recipes (`vintagedelight:fermenting`): `pickle_from_fermenting` (5 `#forge:cucumber` + `#forge:salt` -> 5
  pickles, 2400 ticks), `pickled_egg_from_fermenting` (5 eggs + salt), `century_egg_from_fermenting` (`#forge:eggs` + coal +
  salt, 216000 ticks), `kimchi_from_fermenting` (4 cabbage leaves + salt + `#forge:chilipepper` -> 4 kimchi + a pickled pepper),
  `slime_block_from_fermenting`, `vinegar_from_jam` (+ `_bottles`), plus pickled onion / beetroot / pepper, surstromming.
- Brewin' `pickled_pickles` and `kimchi` (`i.pickled_pickles`, `i.kimchi`, item_dict 11801 / 11806): their keg recipes are
  removed and rebuilt in the jar in `kubejs/server_scripts/cooking/alcohol.js` lines 15-18 (kimchi from cabbage /
  `i.vegetables` / kelp; pickled pickles from 2 sea pickles + glow berries + honey bottle). Vintage's kimchi is
  `i.vintagedelight_kimchi`. Compost rows `compost_values.js` 914-915; `cookingtags.js` line 103 lists both.
- Sea pickle: `minecraft:sea_pickle`, Oceanic Delight `sea_pickle_slices` (2 per sea pickle by knife); `#forge:sea_pickles`
  = both. Pickle: `vintagedelight:pickle` (`i.pickle`). There is no `#forge:pickles` tag in the dump (it predates Vintage).

---

## pickles-and-ferments #1: Remove pickled pickles; pickled sea pickle
Status **Planned** / Work: KubeJS / resource pack / id `dishes/pickles-and-ferments #1`

### Goal
Remove Brewin's Pickled Pickles; add a recoloured pickle texture as the new Pickled Sea Pickle, made without honey.

### Build
1. New item `i.pickled_sea_pickle` (startup items; the pack's item pattern in `kubejs/startup_scripts/items/`), texture
   a recolour of `vintagedelight:item/pickle` in `kubejs/assets/creativeengineering/textures/item/`.
2. `item_merges.js`: `[i.pickled_pickles, i.pickled_sea_pickle]` (swaps recipes, loot, inventories); drop the
   `brewin_pickled_pickles` recipe (`alcohol.js` line 18) and its `canned_pickled_pickles` source (`tools/gen_can_types.py`
   line 263, then `/cegen`); compost row moves to the new item.
3. Craft: `R.vdPickling(Item.of(i.pickled_sea_pickle, 5), [Item.of(i.sea_pickle, 5), i.salt], e, 'pickled_sea_pickle')`.

### Decisions
- **Craft without honey.** Options: 5 sea pickles + salt (the Pickle recipe's pattern); 2 sea pickles + salt + vinegar
  (`i.vinegar`). Recommend the first. Alternative reading: keep the item id, retexture and rename only; recommend the
  merge, because the row says remove.

### Tests
- VTD-55 Pickling jar: sea pickles + salt -> Pickled Sea Pickle; no Pickled Pickles in JEI, creative or loot.

---

## pickles-and-ferments #2: Pickle recipes take real pickles only
Status **Planned** / Work: KubeJS / id `dishes/pickles-and-ferments #2`

### Goal
Recipes that want a pickle accept only `vintagedelight:pickle`: not pickled pickles, not raw sea pickles. Swap the sea
pickle "fake pickles" for real ones; squid and pickles swaps the onion for a real pickle; more pickled options in the
cucumber salad (`dishes/meals #29`).

### Now
`oceanic_delight:squid_and_pickles` (shapeless: grilled squid tentacles, baked potato, `#forge:sea_pickles`,
`#forge:crops/onion`, bowl) and `sea_pickle_roll` (shaped, `#forge:sea_pickles`) take sea pickles; Oceans' `braised_sea_pickle`
is `#4`. Full list of recipes naming `pickled_pickles` or `#forge:sea_pickles`: unverified, re-run `/dumpall`.

### Build
`R.remove({id:'oceanic_delight:squid_and_pickles'})` then `R.shapeless(i.squid_and_pickles /*add dict key*/, [grilled tentacles,
baked potato, i.pickle, i.pickled_onion?, bowl])`; same swap for the roll (`R.shaped`); `R.replaceInput` for the tag.
Add `#forge:pickles` = `[i.pickle]` in `cookingtags.js` (`t.add(e, 'forge:pickles', i.pickle)`) so later rows share it.

### Decisions
- Squid and pickles loses the onion: replace it with the real pickle (the row); recommend `1 pickle` in the onion slot,
  no extra slot.

### Tests
- VTD-56 JEI: squid and pickles and the sea pickle roll list a pickle, never a sea pickle, pickled pickles or onion.

---

## pickles-and-ferments #3: Pickle soup
Status **Planned** / Work: KubeJS / id `dishes/pickles-and-ferments #3`

### Goal
Pickle soup takes pickles, other pickled veggies, and a veggie variant.

### Now
`vintagedelight:cooking/pickle_soup`: pot, `vintagedelight:pickle`, carrot, potato, beetroot, `#forge:salad_ingredients`,
no container listed.

### Build
Replace the pot recipe (`R.remove` + `R.fdCooking`) with the pickle slot as a new tag `#creativeengineering:pickled_veg`
(pickle, pickled beetroot / onion / pepper, kimchi; filled in `cookingtags.js`); add the "veggie variant" as a second
recipe (exact content: Decisions). Mirror `cooking.js`'s `_manual_only` shapeless pattern for the hand craft.

### Decisions
- **Exact ingredients** (the row's own decision). Options: the veggie variant swaps carrot / potato / beetroot for
  `#forge:vegetables`; or it is a no-pickle soup from a pickled veggie. Recommend: pickle slot = the tag, the other
  slots unchanged, and the variant takes `#forge:vegetables/*` for carrot and potato.

### Tests
- VTD-57 Pot: each pickled veggie in the pickle slot makes pickle soup.

---

## pickles-and-ferments #4: Braised sea pickle accepts slices
Status **Planned** / Work: KubeJS / id `dishes/pickles-and-ferments #4`

### Goal
Braised sea pickle also takes a sliced sea pickle.

### Now
`oceansdelight:cooking/braised_sea_pickle`: pot, 1 `minecraft:sea_pickle` + 2 brown mushrooms, bowl. Slices come from
`oceanic_delight:sea_pickle_slices` (a sea pickle cut gives 2). Display Delight has `displaydelight:od_braised_sea_pickle`.

### Build
`R.fdCooking([i.t_sea_pickles?, brown_mushroom, brown_mushroom], ...)` through `#forge:sea_pickles` (it already holds both
items) in place of the single item: remove the mod recipe, add one with the tag; or add a second recipe with 2 slices.

### Decisions
- One slice or two slices for one pickle. Recommend 2 slices (matches the cut yield).

### Tests
- VTD-58 Pot: 2 sea pickle slices + 2 brown mushrooms -> braised sea pickle.

---

## pickles-and-ferments #5: Enzymes from crushed or milled mushrooms
Status **Planned** / Work: KubeJS / id `dishes/pickles-and-ferments #5`

### Goal
Crush or mill mushrooms into enzymes (the vinegar craft uses crushed fungus as its enzyme, `#6`).

### Now
No enzyme item exists (grep of `item_dict.js`: none). `#forge:mushrooms` holds 16 Botania colours plus brown, red and Quark's
glow shroom (exports). Helper: `R.foodGrinding(output, input, e, id, opts)` (`neoCraftHelper.js` ~1440) builds Create crushing,
milling and the mortar craft.

### Build
1. Item `i.enzyme` (startup, new texture).
2. `R.foodGrinding(i.enzyme, <mushrooms>, e, 'enzyme')`; IE crusher through its helper if wanted (unverified).

### Decisions
- **Which mushrooms.** Options: brown and red only; `#forge:mushrooms`; plus crimson and warped fungus ("fungus" in #6).
  Recommend `#forge:mushrooms` minus Botania's (a new tag in `cookingtags.js`) plus the two fungi.
- **Which machines.** Recommend crushing wheels, millstone, mortar (what `foodGrinding` gives); skip the IE crusher.

### Tests
- VTD-59 Crushing wheels and millstone turn a brown mushroom into an enzyme.

---

## pickles-and-ferments #6: Vinegar craft
Status **Planned** / Work: KubeJS / id `dishes/pickles-and-ferments #6`

### Goal
The vinegar craft becomes 3 apple mash plus an enzyme.

### Now
Today: `vinegar_from_jam` (jam jar + `#forge:mushrooms` -> vinegar jar + 3 organic mash, 2400 ticks) and
`vinegar_from_jam_bottles` (bottle form, 1200 ticks, 1 mash). `i.vinegar` = `vintagedelight:vinegar_bottle`.
`i.apple_mash` = `vinery:apple_mash` (Vinery apple press, `alcohol.md` B2.3).

### Build
`R.remove` both recipes by id (`vintagedelight:fermenting/vinegar_from_jam`, `..._bottles`), then
`R.vdPickling(i.vinegar, [Item.of(i.apple_mash, 3), i.enzyme], e, 'vinegar', { container: i.glass_bottle })` and a jar
variant. Depends on `#5`.

### Gotchas
- Organic mash lost its source (vintage_delight.md 2.7 "not a dead item": secondary output of the old recipes; input to
  organic compost): keep it as the `secondary:` output or give it another source.

### Decisions
- Keep organic mash as a secondary output? Recommend yes (`{ secondary: i.organic_mash }`).

### Tests
- VTD-60 Pickling jar: 3 apple mash + enzyme + bottle -> vinegar bottle; the old jam recipes are gone.

---

## pickles-and-ferments #7: Foul Taste on foods with vinegar added
Status **Planned** / Work: KubeJS / patcherMod / id `dishes/pickles-and-ferments #7`

### Goal
Foods that get vinegar added carry a Foul Taste effect, on more foods than today.

### Now
Unverified: no effect named Foul Taste exists in `kubejs/exported/mob_effects.json`, in `effect_dict.js` or the
docs (grep), and no food rule adds one. Vinegar is used as a canning additive (`_vinegar` recipes on 66 cans,
`tools/gen_can_types.py` lines 72-83) and in Vintage's cheese curds only (`cooking.js` has no vinegar rule). So "than
today" cannot be measured; the note may mean the canned additive foods.

### Decisions
- **Which effect.** Options: a new Foul Taste effect in patcherMod; reuse Fruits Delight's `fruitsdelight:disgusted`
  (Disgusted, in `mob_effects.json` line 315); vanilla Nausea. Recommend a new patcherMod effect only if the pack wants
  its own icon; otherwise reuse Disgusted.
- **Which foods.** Options: the 66 `_vinegar` cans; every food with vinegar as an ingredient; pickled foods. Recommend the
  `_vinegar` cans, via the can additive effect list, once the effect is chosen.

### Gotchas
- The can additive's effect plumbing is generated (`can_types.json`); effects there are per-additive (check before building).

### Tests
- VTD-61 Eat a vinegar can: Foul Taste (or the chosen effect) for the decided time; plain can: none.

---

## pickles-and-ferments #8: Merge kimchis
Status **Planned** / Work: KubeJS / data / id `merges/dishes #9`

### Goal
Use Vintage Delight's kimchi, remove the Brewin' brewing of it, and allow any pickleable veggie as the base
(alternative recipe).

### Now
The Brewin' keg recipe is already removed (`alcohol.js` line 15); its replacement is the pack's `brewin_kimchi` jar recipe
(line 16: cabbage / `#forge:vegetables` / kelp -> 2 `brewinandchewin:kimchi`). Vintage's own recipe (cabbage leaf x4 + salt +
chili) outputs `vintagedelight:kimchi`. Both items exist, both canned (`canned_kimchi`), both composted.

### Build
1. `item_merges.js`: `[i.kimchi, i.vintagedelight_kimchi]`; `R.remove` the `brewin_kimchi` jar recipe; `tools/gen_can_types.py`
   line 260 comment and can sources fold to one kimchi (then `/cegen`).
2. Alternative recipe: `R.vdPickling(Item.of(i.vintagedelight_kimchi, 4), [<pickleable veg> x4, i.salt, chili], e, 'kimchi_veg')`
   with the veg slot a tag (`#creativeengineering:pickleable_veg`, new in `cookingtags.js`).

### Decisions
- Which veggies count as pickleable: recommend `#forge:vegetables` minus the poisonous or non-veg ones (cabbage leaf stays
  the primary; kelp as Brewin's had).

### Tests
- VTD-62 JEI: one Kimchi; a non-cabbage vegetable makes kimchi in the jar; the keg has no kimchi recipe.

---

## pickles-and-ferments #9: Pickled egg for the century egg
Status **Planned** / Work: KubeJS / id `dishes/pickles-and-ferments #9`

### Goal
The century egg uses a pickled egg.

### Now
`century_egg_from_fermenting`: `#forge:eggs` + coal + `#forge:salt`, 216000 ticks (3 hours); decided 2026-10-23 ("any egg",
vintage_delight.md §2.7; VTD-35). Pickled egg: 5 eggs + salt in the jar, 2400 ticks.

### Build
`R.remove({id:'vintagedelight:fermenting/century_egg_from_fermenting'})` then
`R.vdPickling(i.century_egg, [i.pickled_egg, i.coal], e, 'century_egg', { time: 216000 })`. Whether salt stays in the craft
is unclear (the pickled egg already has it); recommend coal only.

### Tests
- VTD-63 Pickling jar: pickled egg + coal -> century egg after 3 hours; an unpickled egg no longer works. Supersedes VTD-35.

---

## pickles-and-ferments #10: Fermented garlic honey effects
Status **Planned** / Work: KubeJS / data / id `dishes/pickles-and-ferments #10`

### Goal
Fermented garlic honey gives Regeneration II for 5 seconds and Health Boost (3 hearts) for 30 seconds.

### Now
`veggiesdelight:fermented_garlic_honey` (`i.fermented_garlic_honey`): 7 hunger, Regeneration I 5 s and Health Boost I 5 s
(`development/backportedVeggies/.../ModFoods.java` line 31; `exported/food/foods.json`). The jar recipe ships in the Veggies
port (`recipes/compat/vintagedelight/fermenting/fermented_garlic.json`).

### Build
KubeJS startup `ItemEvents.modification` with a built `FoodProperties` (the pattern of `startup_scripts/cooking/alcohol_drinks.js`
and `food_values.js`), or edit `ModFoods` in the port and rebuild `:backportedVeggies`. Recommend KubeJS (no jar swap).

### Decisions
- **"3 hearts".** Health Boost gives +4 health (2 hearts) per level: I = 2 hearts, II = 4 hearts; 3 hearts is not a level.
  Options: Health Boost I (2), II (4), or the note meant 3 levels. Recommend Health Boost II (4 hearts) and confirm.

### Tests
- VTD-64 Drink it: Regeneration II 0:05 and Health Boost for 0:30; tooltip and JEED agree.

