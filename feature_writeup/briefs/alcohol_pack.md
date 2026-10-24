# Brief: alcohol / pack

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# alcohol`, Sub `pack` (row 3 only; rows 1, 2, 8 are Testing and
need play, not building). Layout per [README.md](README.md). Writeups: [alcohol.md](../alcohol.md) (§11 drink values),
[vintage_delight.md](../vintage_delight.md) (§2.7a "what the brews are").

---

## pack #3: Vintage brews
Status **Planned** / Work: KubeJS / data / id `alcohol/pack #3`

### Goal
Vintage Delight brews: do any add effects? Split them into their own category, and redesign their effects and crafts.

### Now
- **Answer to "do any add effects": all six do**, with vanilla effects only (no `vintagedelight:dehydrated`), as the NBT of a
  `minecraft:potion` made in the Pickling Jar: `data/vintagedelight/recipes/fermenting/{goblins_piss, berserks_brew,
  brashing_tonic, dragons_belch, frost_fermenta, oily_oaf}.json` (container glass bottle, 600 ticks for Oily Oaf; verified in
  the port). Effects and ingredients per brew are in vintage_delight.md §2.7a; the output carries `CustomPotionEffects`
  with numeric ids, `CustomPotionColor` and a text name (Oily Oaf's name is "OilyOaf", no space: a names check).
- Because they are potion items with NBT, not registered items: no creative tab entry, no food values, not in
  `global.alcohol.drinks` (`startup_scripts/cooking/alcohol_drinks.js`), no JEED / effect-description entries for them.
- The pack's potion system (`kubejs/startup_scripts/cooking/potions.js`, potion buckets, [potions.md](../potions.md)) treats
  any `minecraft:potion` with custom effects like other potions.

### Build
1. Decide the category (Decisions), then the redesign table: per brew, new effects and durations (keep Goblin's Piss with a
   fermented spider eye, decided 2026-10-23).
2. Rewrite the six recipes in `alcohol.js` beside the other jar recipes (`R.vdPickling` builds the recipe JSON; it takes an
   item output, so NBT outputs need `Item.of('minecraft:potion', nbt)`; unverified whether `_itemJson` keeps the NBT).
   Remove the originals by id.
3. Category: see Decisions; the JEI side is the Pickling Jar page (`FermentingCategory`) in the port.

### Decisions
- **What "their own category" means.** Options: (a) a separate JEI recipe category for the six jar brews; (b) registered
  items with their own creative-tab group, so they get tooltips, JEED text and food values like the Brewin' drinks;
  (c) just a tooltip line or colour. Recommend (b): real items (`creativeengineering:` or Vintage's own) are the only way
  to make them stack-distinct, appear in a tab, and join `alcohol_drinks.js` if any are alcoholic.
- **Effects per brew** (the row's decision): the current set is strong (Strength III + Resistance IV, Speed III); recommend
  trimming amplifiers to the Brewin' level (II max) and making each cost something (the existing Hunger I, Wither I).
- Which brews count as alcohol (Intoxication / Nausea, alcohol.md §11): recommend none; they are tonics.

### Gotchas
- `CustomPotionEffects` uses numeric effect ids (3 = Haste, 17 = Hunger): rewriting them as named ids is part of the redesign.

### Tests
- AL-97 JEI: the six brews sit in the chosen category with their new effects; the jar crafts match; names are spaced correctly.
