# Brief: largecontent / mod-additions

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# largecontent`, Sub `mod-additions`. Only the rows that
gained scratchpad 2026-10-24 scope are covered here (rows 4, 5, 6, 7 folded; rows 13, 14, 15 new); the other rows have
no brief yet. Layout per [README.md](README.md). Feature writeup:
[ends_delight.md](../ends_delight.md), [ocean_delight.md](../ocean_delight.md), [miners_delight.md](../miners_delight.md), [hostile_delight.md](../hostile_delight.md) (survey of the five 2026-10-23 jars) and
[hostile_delight.md](../hostile_delight.md). Jars read 2026-10-24 from `mods/`: `ends_delight-1.19.2-2.1`,
`oceansdelight-1.0.2`, `oceanic_delight-1.0.1`, `miners_delight-1.19.2-1.1.2`, `Hostile_Delight_0.2.2`.

Sweep 2026-10-31 (partial, stopped early): rows 1, 2, 3 written new; the other sections below are still the
2026-10-24 text and were not re-verified in this pass.

## Scope and order

| Row | Status (TODO 2026-10-31) | Section here |
|---|---|---|
| `#1` mods-to-add writeup | Planned | written 2026-10-31 |
| `#2` mod family tags | Planned | written 2026-10-31 |
| `#3` Environmental features | Planned | written 2026-10-31 |
| `#4` End's Delight | Testing | 2026-10-24 text, not re-verified |
| `#5` Ocean's / Oceanic | In Progress | 2026-10-24 text, not re-verified |
| `#6` Hostile Delight | Planned | 2026-10-24 text, not re-verified |
| `#7` Miner's Delight | In Progress | 2026-10-24 text, not re-verified |
| `#13` MCA social expansion | Planned | 2026-10-24 text, not re-verified |
| `#14` Rolls Delight | Planned | 2026-10-24 text, not re-verified |
| `#15` End's succulent / fungus | Testing | 2026-10-24 text, not re-verified |
| `#8` to `#12`, `#16` to `#38` | see TODO | no section yet |

## mod-additions #1: Writeup of mods to add
Status: Planned / Work: docs / TODO `largecontent/mod-additions #1` (source: scratchpad 2026-10-19)

### Goal
One writeup that lists the candidate mods (Sophisticated Storage, the decor mods parked in `disabledmods/`, Tempad), says
what each adds, how it sits with the pack, and a keep / add / skip recommendation for the owner.

### Now (checked 2026-10-31)
- Sophisticated Storage: not on disk. `mods/` has only `sophisticatedbackpacks-1.19.2-3.20.2.1035` and
  `sophisticatedcore-1.19.2-0.6.4.730` (`mod_sides.md` lines 270-271). The pack's Sophisticated compat (Polymorph in the
  crafting upgrade, [fixes_compat.md](../fixes_compat.md) 2.28) hooks every `StorageContainerMenuBase` screen, so it would cover
  Storage too (2.28 notes "Sophisticated Storage is not installed"). A Storage build must match Core 0.6.4.
- Decor jars in `disabledmods/`: `chipped-forge-1.19.2-2.1.5`, `cluttered-2.1.1-1.19.2`, `DustyDecorations_1.19.2Forge_V1.3`,
  `farmhouse_decorations-26.4.0-forge-1.19.2`, `yuushya-1.19.2-forge-2.2.3`, `PaintingsN'DiscsBackport_1.19.2Forge_V1.3.1`.
  No writeup covers them (the jars retired by the feature ports, [feature_porting.md](../feature_porting.md) 1.1, are a
  different set). Yuushya still has leftover rows in [stack_sizes.md](../stack_sizes.md) and [toasts.md](../toasts.md).
- Tempad: `disabledmods/tempad-forge-1.19.2-1.4.5.jar` (2026-07-09); no doc names it.

### Build
1. Per jar: read `mods.toml` (deps, version), count items/blocks (`unzip -l <jar> | grep -c 'models/item/'`), note licence.
2. Overlap check per jar against the installed decor set (Create Deco, Dustrial Decor, Architects Palette, Quark,
   Supplementaries, Storage Delight) and, for Sophisticated Storage, against Storage Drawers, Ender Storage and the pack
   storage blocks; Tempad against Waystones and the pack's travel rules.
3. Write `feature_writeup/mods_to_add.md` (one table: mod, version, size, deps, overlap, recommendation), link it from
   `feature_writeup/README.md`. Each "add" then goes through the `add-mod` skill as its own TODO row.

### Decisions
- Which to add (the row's decision). Recommend: Sophisticated Storage yes if a Core-0.6.4-compatible build exists (it reuses
  the backpack upgrades and the pack's compat); Chipped no (17 MB, thousands of variants, JEI and stack-size load); the
  smaller decor mods judged one by one in the writeup; Tempad no unless the travel design wants it.

### Tests
None (docs). Each added mod gets its own tests from the `add-mod` skill.

---

## mod-additions #2: Mod family tags
Status: Planned / Work: KubeJS / TODO `largecontent/mod-additions #2` (source: scratchpad 2026-10-19)

### Goal
Item tags that group the items of a mod family: every Create add-on, every Farmer's Delight add-on.

### Now (checked 2026-10-31)
- No such tag exists (grep for `mod_family`, `create_addons`, `delight_addons` in `kubejs/` and the patcherMod
  `kubejs_startup/` finds nothing).
- `global.tags.addMatching` (`development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/tag_helper.js`
  lines 38-54) takes `@modid` entries and globs that may sit in the namespace half (`*_delight:*` becomes the regex
  `^[^:]*_delight:[^:]*$`, `_glob` lines 58-65).
- 28 jars in `mods/` start with `create`; 15 Delight / Farmer's jars. Creative Delight nests FD, Ocean's, Oceanic, End's,
  Rustic, Delightful, H&H and others under their own namespaces (`mod_sides.md` line 436), so namespace globs catch them.

### Build
1. New file `kubejs/server_scripts/tags/modfamilytags.js` (`ServerEvents.tags('item', e => ...)`), only `global.tags`:
   - `creativeengineering:mod_family/create`: `addMatching` with `create:*`, `create*:*` and an explicit `@modid` list for
     add-ons whose namespace does not start with `create` (read from `mod_sides.md`).
   - `creativeengineering:mod_family/farmers_delight`: `*_delight:*`, `farmers*:*`, `farmersdelight:*`, plus `@modid` for the
     odd ones (`brewinandchewin`, `nethersdelight`, `hostiledelight`, `miners_delight`, `endersdelight`, `ends_delight`,
     `rusticdelight`, `delightful`, `fruitsdelight`, `moredelight`, `hearthandharvest`).
   - `except:` the pack's own items that share a namespace but are not add-on content, if any turn up.
2. Say what reads the tags (the row does not): JEI search, quest filters, item filters. Add the user to the writeup.
3. Document in [kubejs_recipes.md](../kubejs_recipes.md) or the tags writeup, with the member list rule.

### Decisions
- Purpose and consumer: not stated in the row. Recommend asking the owner before building; without a consumer the tags are
  dead data.
- Whether `farmersdelight` itself counts as an add-on: recommend yes (the family tag is "FD and its add-ons").

### Gotchas
- A glob in the namespace half also catches unrelated mods (`create*` catches `createdeco`, fine; check `*_delight` does not
  catch a non-food mod).
- Regex entries match every registry id at tag time: one tag per family, not one per item, to keep load cost low.

### Tests
- `TAG-new1` (offline, `/dumptags`): both tags exist and contain items from at least 5 namespaces each.
- `TAG-new2`: no pack-only (`creativeengineering:`) item sits in either tag.
(proposed, numbered when queued)

---

## mod-additions #3: Environmental features
Status: Planned / Work: new jar / data / TODO `largecontent/mod-additions #3` (source: scratchpad 2026-10-21)

### Goal
Get the wanted Environmental (Team Abnormals) features into the pack: cattails (shearable), mud balls (throwable, make pigs
muddy), muddy pigs (drying, flowers on pigs), koi (and their mob-spawn prevention), deer and reindeer with venison; and own
truffle pie art.

### Now (checked 2026-10-31)
- Environmental is not on disk (`mods/`, `disabledmods/`, `DecompiledMods/` have none). Its library is installed:
  `blueprint-1.19.2-6.2.0`, `neapolitan-1.19.2-4.1.0` (Neapolitan monkeys are its own mob, already in the pack).
- Truffle pie: `create_central_kitchen:truffle_pie` gets a block item from Creative Delight,
  `development/creativeDelight/src/main/resources/kubejs_startup/create_central_kitchen/truffle_pie_item.js` (the CCK block
  has no item because Environmental owns the placeable pie); dict key `i.truffle_pie` (`kubejs/startup_scripts/_lib/item_dict.js`
  line 14574), slice `i.truffle_pie_slice` (line 5246). Model `assets/create_central_kitchen/models/item/truffle_pie.json` points
  at FD's chocolate pie sprite (placeholder).
- Venison: `food/types-and-tuning #10` (Testing) removed unused meats including venison from recipes and view, so deer
  would bring it back.

### Build
1. Decide port vs jar (below). For a jar: get Environmental for 1.19.2 on Blueprint 6.2.0 (the 4.x line), then the
   `add-mod` skill (sides row, `/dumpall`, generators), and turn off what is not wanted through its config and KubeJS
   (remove items, recipes, spawns, worldgen biome modifiers).
2. For a port: the `port` skill; a Creative Delight member only for the food parts (truffle pie), other parts in patcherMod
   or a new in-tree mod. Licence check first (Team Abnormals mods: read the jar's licence).
3. Pieces: cattail (block + worldgen through the wild-flowers / wild-plants rules), mud ball (projectile, sets a pig muddy
   state), muddy pig (entity data on vanilla pig, dries over time, flower placement), koi (fish entity, spawn rules, its
   anti-spawn radius), deer / reindeer (entities, spawns per [mob spawns](../mob_spawns.md), venison back in
   `food_dict.js` and the food families).
4. Truffle pie art: Environmental's sprite if the jar comes in (point the CD model at it), else draw one; either way only
   `truffle_pie.json` in Creative Delight changes.

### Decisions
- Port or add the jar, and which parts (the row's decision). Recommend the jar with unwanted content off: Blueprint is
  already present and the features are entity-heavy, which a port would have to rewrite.
- Venison: if deer come in, recommend restoring venison as a pack meat in the produce family rules (undoes part of
  `food/types-and-tuning #10`; say so in that row).

### Gotchas
- Environmental adds many biomes and blocks beyond the wanted list; each needs turning off or the biome-frequency and
  structure-fit work is affected.
- Its own truffle pie block would collide with CD's block item for `create_central_kitchen:truffle_pie` only if CCK switches
  to Environmental's pie when present: check CCK's compat path before installing.

### Tests
- `MA-new1`: cattails generate in swamps and shear to drop.
- `MA-new2`: mud ball on a pig makes it muddy; it dries in rain-free time.
- `MA-new3`: koi spawn and keep hostile spawns out of their radius.
- `MA-new4`: deer / reindeer spawn in their biomes and drop venison.
- `MA-new5`: truffle pie shows its own art in hand and placed.
(proposed, numbered when queued; prefix to settle with the writeup)

---

## mod-additions #4: Integrate End's Delight (alongside Ender's Delight)
Status: Planned / Work: KubeJS / data / TODO `largecontent/mod-additions #4` ([ends_delight.md](../ends_delight.md))

Base work is in the writeup (section 1, checklist section 3). Folded scope (scratchpad 2026-10-24): the succulent and
fungus options, now `largecontent/mod-additions #15` below. Other 2026-10-24 rows that depend on this integration:
`baking/baked-goods #22` (chorus cookie from the uncanny cookie) and `largecontent/mod-additions #15`.

---

## mod-additions #5: Integrate Ocean's Delight and Oceanic Delight
Status: Planned / Work: KubeJS / data / TODO `largecontent/mod-additions #5` ([ocean_delight.md](../ocean_delight.md))

Base work is in the writeup (section 2). Folded scope (scratchpad 2026-10-24): the ocean work is now five rows in
[dishes_ocean.md](dishes_ocean.md): `dishes/ocean #1` fishing net, `#2` fish egg roll, `#3` squid items and the calamari
chain, `#4` tentacles to calamari (the tentacle merge), `#5` caviar. Related: `dishes/meals #10`, `#30` (Caesar salad),
`baking/baked-goods #25` (sponge pie) and `dishes/pickles-and-ferments #4` also take Oceanic items. Those rows set the
survivors the writeup's open decisions asked for (one tentacle tag: calamari).

---

## mod-additions #6: Integrate Hostile Delight
Status: Planned / Work: KubeJS / data / patcherMod / TODO `largecontent/mod-additions #6` ([hostile_delight.md](../hostile_delight.md))

Base work is in the writeup. Folded scope (scratchpad 2026-10-24), two items:
- Hostile Delight's prepared sea pickle (`hostiledelight:preapredseapickle`, the id has the typo; verified in the jar): made by
  a knife on a vanilla sea pickle (`ppicklerecp`), used by the guardian sandwich and the marine cocktail
  (`gsandrecp`, `mcocktrecp`). Probably remove it: a merge into the sliced sea pickle of Oceanic Delight (`sea_pickle_slices`)
  keeps the sandwich and cocktail working; decide with the pickle rows (`dishes/pickles-and-ferments #4`).
- Its cookies go into jars: part of the jar contents list in `items/jars #3`. Hostile cookies are
  `hostiledelight:magmacookie` and the like (full list from `/dumpall`).

---

## mod-additions #7: Miner's Delight: installed jar is OLD, compare with 1.4.5
Status: Planned / Work: docs / port / TODO `largecontent/mod-additions #7` ([miners_delight.md](../miners_delight.md) 10)

Base work is in the writeup (section 4). Folded scope (scratchpad 2026-10-24): investigate the Miner's Delight soup cup (what
it is for, and why JEI shows no recipe). Finding from the installed 1.1.2 jar: the soup cups (`beef_stew_cup`,
`chicken_soup_cup`, `mushroom_stew_cup`, ...: 11 soup and stew cups in all) have no recipe. They come from a data
file, `data/miners_delight/cup_conversion/default_conversions.json`: entries `{bowl, cup}` pairing a soup bowl food with
its cup form (vanilla and Farmer's Delight soups). The Copper Cup (`copper_cup`, shaped from copper ingots) is the container
(`CopperCupItem`, `SolidCupItem` classes), filled by Create filling with water or milk (recipes `fill_water_cup`,
`fill_milk_cup`). How a bowl becomes a cup in play (probably using the soup on a cup or in the Copper Pot) is *unverified*:
decompile `CopperCupItem` with `tools/decompile_mod.py`. JEI shows nothing because no recipe type exists; a JEI info page
(`JEIEvents`) or a KubeJS recipe would be the fix, after deciding whether the cups stay.

---

## mod-additions #13: MCA social expansion
Status: Planned / Work: mods/ / TODO `largecontent/mod-additions #13` ([README](../README.md))

### Goal
Decide whether to add a social expansion mod for MCA (Minecraft Comes Alive) and, if so, which.

### Now
- MCA is `minecraft-comes-alive-7.5.20+1.19.2-universal.jar` (`mod_sides.md` row, both sides). The pack's MCA work:
  villager sleep glitch (`mobs/tweaks #2`), eye colour and shape in the Comb (`mobs/tweaks #12`, built), MCA items in village
  houses (`loot/tables #6`), MCA family group in JEI (`jei/jei-ordering #3`), starter kit combs.
- No social-expansion mod is in `mods/`, `disabledmods/` or `DecompiledMods`; nothing in the repo docs names a candidate.
  Which 1.19.2 mods exist is *unverified*: needs a search (CurseForge / Modrinth for "MCA" addons).
- Karma (`largecontent/karma #1`) is writeup only: good and bad actions move a karma value, with luck past a cutoff. A social
  mod that adds relationship actions would feed it.

### Build
1. Search 1.19.2 MCA add-ons; per candidate record: Forge 1.19.2 build, dependencies, last update, what it adds (new
   interactions, villager jobs, marriage, children), licence.
2. Put the shortlist in the writeup with an overlap check against `mobs/tweaks` (villager AI) and a recommendation.
3. If one is chosen: jar into `mods/`, a `mod_sides.md` row, `/dumpall`, config backup in `configs_edited/`.

### Decisions
Whether to add one at all, and which. Recommend research only for now; add nothing until the karma design says what social
actions it needs.

---

## mod-additions #14: Rolls Delight, when added
Status: Planned / Work: mods/ / KubeJS / TODO `largecontent/mod-additions #14` ([ocean_delight.md](../ocean_delight.md))

### Goal
When a rolls Delight mod is added: any roll works in any roll recipe across the mods, and new roll blocks (placeable rolls).

### Now
- A Roll Delight 1.3.1 (Forge 1.20.1, about 17 items) is in `DecompiledMods`, not installed (`mod-additions #12` is its
  overlap analysis); no rolls Delight is in `mods/`.
- Existing rolls (verified in jars): Farmer's Delight `kelp_roll`, `cod_roll`, `salmon_roll`; Oceanic `egg_roll`, `fish_egg_roll`,
  `sea_pickle_roll`, `shrimp_roll`; Rustic `calamari_roll`; Ocean's `fugu_roll`, `elder_guardian_roll`. Rustic already reads
  a tag, `#rusticdelight:calamari_roll_ingredients`.

### Build
1. Blocked: nothing to do until the jar is added.
2. Then a tag `#creativeengineering:rolls` (every roll item, through `global.tags`) and `R.replaceInput` / widened inputs in the
   recipes that take a roll (sushi plates, Display Delight plated rolls).
3. Placeable rolls: new blocks like Farmer's Delight feasts with 4 servings (see `food/placeable`), through the placeable
   system (`generation_scripts/food/placeable.json`).

### Decisions
Which mod and whether to add it at all (the TODO row's decision). Recommend wait; do `mod-additions #12` first.

---

## mod-additions #15: End's Delight: succulent and fungus options
Status: Planned / Work: KubeJS / data / TODO `largecontent/mod-additions #15` ([ends_delight.md](../ends_delight.md)); see `#4` (folded text)

### Goal
Decide on End's Delight's succulent (is a chorus succulent kept: find where it comes from, then remove and replace it) and
expand the fungus options in its recipes (more End and Nether fungi accepted, with `ports/fungiport`).

### Now
- `ends_delight:chorus_succulent` is a plant block placed by worldgen: `forge/biome_modifier/add_chorus_succulent.json` with
  a configured feature and `loot_tables/blocks/chorus_succulent.json` (verified in the jar). It is an ingredient in 9 food
  recipes (`chorus_sauce`, `end_mixed_salad`, `ender_bamboo_rice`, `shulker_soup`, `enderman_gristle_stew`, `grilled_shulker_block`,
  `dragon_meat_stew_block`, `steamed_dragon_egg_block`, ...) and makes purple dye.
- Fungus options: its recipes name `minecraft:crimson_fungus` (e.g. `ender_noodle`, `dragon_leg_with_sauce_block`,
  `roasted_dragon_steak`). Fungi Delight is in-tree (`development/backportedFungi`) with its own fungus items and tags.

### Build
1. Decide the succulent: keep it (a worldgen End plant) or replace it with an existing End or Nether plant in the 9 recipes
   (`R.replaceInput` with a tag), and remove the worldgen feature (a biome modifier override: `kubejs/data/...` empty
   file for `add_chorus_succulent`).
2. Fungus: a tag `#creativeengineering:cookable_fungi` (crimson, warped and the Fungi Delight edible fungi) via
   `global.tags`; swap `minecraft:crimson_fungus` inputs in End's recipes for it.
3. Needs the integration (`#4`) first: dict keys come from `/dumpall`.

### Decisions
Keep or replace the succulent; recommend replace with chorus fruit or chorus flower (both vanilla and End-native) so no extra
plant needs worldgen tuning. Which fungi count: recommend crimson, warped and Fungi Delight's edible ones.

### Tests
DA-09 End's recipes accept the fungus tag; DA-10 no `chorus_succulent` generates in a new End chunk (if removed).

---

## Built, needs play

None: no `mod-additions` row covered here is at Testing or Complete.

## Writeup corrections made

None.
