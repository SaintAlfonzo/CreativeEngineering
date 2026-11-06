# Brief: alcohol / pack

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# alcohol`, Sub `pack`; checked against the pack 2026-10-31. Layout
per [README.md](README.md). Writeups: [alcohol.md](../alcohol.md) (§11 drink values; test prefix `AL-`, last id AL-96),
[vintage_delight.md](../vintage_delight.md) (§2.7a "what the brews are"), [creative_brewing.md](../creative_brewing.md)
(where brewing content goes next).

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `pack #3` Vintage brews | KubeJS / data | Decide the category and the effect table with the owner first; the recipe side is small |
| - | `pack #1`, `pack #2` | play | Built, needs play (table at the end) |

---

## pack #3: Vintage brews
Status **Planned** / Work: KubeJS / data / id `alcohol/pack #3`

### Goal
Vintage Delight brews: do any add effects? Split them into their own category, and redesign their effects and crafts.

### Now (checked 2026-10-31)
- **Answer to "do any add effects": all six do**, with vanilla effects only (no `vintagedelight:dehydrated`), as the NBT of a
  `minecraft:potion` made in the Pickling Jar. Upstream data:
  `development/backportedVintage/src/main/resources/data/vintagedelight/recipes/fermenting/{goblins_piss, berserks_brew,
  brashing_tonic, dragons_belch, frost_fermenta, oily_oaf}.json`. Effects as numeric ids (amplifier 0 = level I):

  | Brew | Effects (id, level, seconds) | Time | Bottle consumed |
  |---|---|---|---|
  | Goblin's Piss | Speed III 30, Weakness II 30 | 600 | no |
  | Berserker's Brew | Strength III 30, Resistance IV 30, Wither I 20 | 400 | yes |
  | Brashing Tonic | Regeneration II 20, Slowness II 20 | 600 | yes |
  | Dragon's Belch | Jump Boost III 20, Levitation I 20, Nausea I 20 | 400 | no |
  | Frost Fermenta | Fire Resistance I 180, Slowness I 180 | 600 | no |
  | Oily Oaf | Haste II 60, Hunger I 60 | 600 | yes |

  Names are spaced correctly in the data ("Oily Oaf"; the earlier "OilyOaf" note is stale).
- **Berserker's Brew is already rebuilt by the pack** (salt dust -> `#forge:salt`, vintageport #10) in Creative Delight
  [vintagedelight/recipes.js](../../kubejs/data/creativedelight/kubejs/vintagedelight/recipes.js) lines 18-24, with the same
  effects. The other five ship as upstream.
- `R.vdPickling` ([neoCraftHelper.js](../../development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js)
  lines 1211-1244) writes the output through `_nbtItemJson`, so an `Item.of(i.potion, nbt)` output keeps its NBT (the
  Berserker's Brew row proves it); `opts.container` sets the bottle the jar consumes.
- Because they are potion items with NBT, not registered items: no creative tab entry, no food values, not in
  `global.alcohol.drinks` ([alcohol_drinks.js](../../kubejs/startup_scripts/cooking/alcohol_drinks.js) has no
  `vintagedelight` entry), no JEED / effect-description entries.
- The pack's potion system (`kubejs/startup_scripts/cooking/potions.js`, [potions.md](../potions.md)) treats any
  `minecraft:potion` with custom effects like other potions. SQ-VP-12 PASSED: the six brews' effects are vanilla ids.
- Routing: brewing, alcohol and effect content is moving to Creative Brewing (`creativebrewing/move #1`; memory
  `creative-delight-routing`). CB's server tree is `kubejs/data/creativebrewing/kubejs/<modid>/` with
  `global.CB.recipes(() => ...)` (creative_brewing.md 2.4); it has no `vintagedelight/` folder yet.

### Build
1. Decide the category and the per-brew table (Decisions). Keep Goblin's Piss with a fermented spider eye (decided
   2026-10-23).
2. Recipes: one file `kubejs/data/creativebrewing/kubejs/vintagedelight/recipes.js` (`// requires: vintagedelight`, early
   return on `!global.CB`, rows inside `global.CB.recipes`). For each brew, `R.remove({ id: 'vintagedelight:fermenting/<id>' }, e)`
   then `R.vdPickling(output, inputs, e, '<id>', { time, container: i.glass_bottle })`. Move the Berserker's Brew row out of the
   CD file into it, so the six live together. Give every brew `container: i.glass_bottle` (three take no bottle today).
3. Output: with option (b), register six items in a CB startup script (`development/creativeBrewing/src/main/resources/
   kubejs_startup/vintagedelight/brews.js`) as drinkable items with the effects as food effects, add their keys to
   [item_dict.js](../../kubejs/startup_scripts/_lib/item_dict.js), and use `Item.of(i.<key>)`. With (a) or (c), keep
   `Item.of(i.potion, nbt)` and write named effect ids in the NBT.
4. JEED / effect lines: only needed for (b) (the potion route already shows vanilla effect text).

### Ids and data
- Item keys used: `i.potion`, `i.glass_bottle`, `i.salt` (`#forge:salt`), `i.fermented_spider_eye`, plus each brew's inputs
  (vanilla, FD `farmersdelight:tomato_seeds` / `rotten_tomato` / `milk_bottle`, Vintage `vintagedelight:ghost_pepper_seeds`,
  `raw_oats`, `pickled_onion`). Verified in the port data, keys unverified for the Vintage ones.
- With (b), new ids `creativebrewing:<brew>` (unverified that CB registers items from startup scripts yet: it has no item
  rows so far).

### Decisions
- **What "their own category" means.** Options: (a) a separate JEI recipe category for the six jar brews; (b) registered
  items with their own creative-tab group, so they get tooltips, JEED text and food values like the Brewin' drinks;
  (c) just a tooltip line or colour. Recommend (b): real items are the only way to make them stack-distinct, appear in a
  tab, and join `alcohol_drinks.js` if any are alcoholic. Put them in Creative Brewing (routing above).
- **Effects per brew** (the row's decision): the current set is strong (Strength III + Resistance IV, Speed III, Jump Boost
  III); recommend trimming amplifiers to the Brewin' level (II max) and making each cost something (the existing Hunger I,
  Wither I, Slowness).
- Which brews count as alcohol (Intoxication / Nausea, alcohol.md §11): recommend none; they are tonics.

### Gotchas
- `CustomPotionEffects` uses numeric effect ids (3 = Haste, 17 = Hunger): rewriting them as named ids is part of the redesign.
- Goblin's Piss, Dragon's Belch and Frost Fermenta take no bottle from the jar's container slot: a potion out of nothing.
- vintage_delight.md 7 step 13 asks for the six brews to be checked against `PotionMixDedup` and the mixers (VTD-23); with
  (b) they stop being potions, so recheck that note after the change.

### Tests
- AL-97 (proposed) JEI: the six brews sit in the chosen category with their new effects; each jar craft uses one glass bottle;
  names are spaced correctly.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| pack #1 | Rotten-food hunger type for alcohol | Testing | alcohol.md 11; AL-85 to AL-90; open S+ C P T (server part PASSED SQ-OM-00, SQ-OM-04) |
| pack #2 | Wine boosts beyond the cellar (T5) | Testing | [time.md](../time.md) 4 T5, alcohol.md 10.13; VB-76 to VB-80; open S+ P T (server part PASSED SQ-OM-05). Live values: `[time.wine.boosts]` in `config/creativeengineering-common.toml` line 2063 (cellar 2.0) |
