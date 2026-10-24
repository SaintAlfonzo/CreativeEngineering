# Brief: largecontent / mod-additions

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# largecontent`, Sub `mod-additions`. Only the rows that
gained scratchpad 2026-10-24 scope are covered here (rows 4, 5, 6, 7 folded; rows 13, 14, 15 new); the other rows have
no brief yet. Layout per [README.md](README.md). Feature writeup:
[delight_addons_2026_10.md](../delight_addons_2026_10.md) (survey of the five 2026-10-23 jars; sections 1 to 4 per jar) and
[hostile_delight.md](../hostile_delight.md). Jars read 2026-10-24 from `mods/`: `ends_delight-1.19.2-2.1`,
`oceansdelight-1.0.2`, `oceanic_delight-1.0.1`, `miners_delight-1.19.2-1.1.2`, `Hostile_Delight_0.2.2`.

## mod-additions #4: Integrate End's Delight (alongside Ender's Delight)
Status: Planned / Work: KubeJS / data / TODO `largecontent/mod-additions #4` ([delight_addons_2026_10.md](../delight_addons_2026_10.md) 1)

Base work is in the writeup (section 1, checklist section 3). Folded scope (scratchpad 2026-10-24): the succulent and
fungus options, now `largecontent/mod-additions #15` below. Other 2026-10-24 rows that depend on this integration:
`baking/baked-goods #22` (chorus cookie from the uncanny cookie) and `largecontent/mod-additions #15`.

---

## mod-additions #5: Integrate Ocean's Delight and Oceanic Delight
Status: Planned / Work: KubeJS / data / TODO `largecontent/mod-additions #5` ([delight_addons_2026_10.md](../delight_addons_2026_10.md) 2)

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
Status: Planned / Work: docs / port / TODO `largecontent/mod-additions #7` ([delight_addons_2026_10.md](../delight_addons_2026_10.md) 4)

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
Status: Planned / Work: mods/ / KubeJS / TODO `largecontent/mod-additions #14` ([delight_addons_2026_10.md](../delight_addons_2026_10.md))

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
Status: Planned / Work: KubeJS / data / TODO `largecontent/mod-additions #15` ([delight_addons_2026_10.md](../delight_addons_2026_10.md)); see `#4` (folded text)

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
