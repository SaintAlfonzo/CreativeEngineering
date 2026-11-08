# Random-tick audit (F2, `farming/crops #15`)

Investigation only, nothing built. Plan: [plan_farming_cooking_batch.md](../plan_farming_cooking_batch.md) F2, owner
decision Q14. Output: the list below, for approval before any change.

## How it was made (2026-11-07)

- Plant-like blocks: `kubejs/exported/crops/crop_growth_audit.json` (`/dumpcropgrowth`, 2026-11-05): 313 blocks with an
  age-style property or a plant class, each with its class chain.
- Every class in `mods/*.jar` and their nested `META-INF/jarjar` jars, read with a class-file parser (scratch script,
  not kept): which classes declare `randomTick` (`m_213898_`), `isRandomlyTicking` (`m_6724_`) and `tick`
  (`m_213897_`), then `javap -c` on the interesting ones. In 1.19.2 the default `randomTick` calls `tick`, so a block
  with only a `tick` override acts on random ticks too.
- A block random-ticks when `isRandomlyTicking(state)` is true: the `randomTicks()` property flag (set where the block is
  registered, so not visible per class) or an override. Rows marked "flag" tick in every state if the flag is set;
  that is the usual case for these growing blocks, but it is unconfirmed until the dump in "Next step" runs.
- Why it matters: a chunk section counts its random-ticking states; a section with none is skipped. A field of
  full-grown crops that still claims ticks keeps every section busy (3 picks per section per tick at the default
  `randomTickSpeed`), each one a block lookup and a no-op call.

## Already fine (no change)

- Vanilla `CropBlock` and every mod crop on it (most of the 313): `isRandomlyTicking` is `!isMaxAge`. Same for sweet
  berries (age < 3), cocoa (< 2), nether wart (< 3), cave vines / kelp / weeping and twisting vines (age < 25),
  chorus flower (< 5); leaves only while decaying; flowers, tall grass, dripleaf and sea pickles never tick.
- Mods that already stop at the last stage: Farmer's Delight budding crops, Farmer's Respite tea and coffee stems,
  Delightful salmonberry, Creative Delight cantaloupe and chorus flame, Fruits Delight bushes and lemon tree (age < 4),
  Vinery grape bushes (age < 3), Immersive Weathering ivy.
- Needed ticks: saplings, mangrove propagule, melon / pumpkin / Fruits Delight hamimelon stems (make the fruit),
  sugar cane, cactus, bamboo, grass, mycelium, Immersive Weathering soils, sandy and mossable blocks (weathering),
  Fruits Delight and Vinery fruit leaves (fruit regrowth), Vinery jungle grape vine (spreads at full age), Brewin'
  unripe cheese wheels (ripen), Alex's Mobs ender residue and skunk spray (fade), Creative Delight sea grapes (grow
  upward until blocked), vanilla vines and mushrooms (spread).

## Candidates

| # | Block(s) | Mod | Ticks | What the tick does at the last stage | Recommendation |
|---|---|---|---|---|---|
| 1 | `tomatoes`, `rope_tomatoes`, `stick_tomatoes`, `treated_stick_tomatoes` | Farmer's Delight (`TomatoVineBlock`; Supplementaries and Creative Delight subclasses) | `isRandomlyTicking` always true | grows to max age, then only tries to climb a rope above | **Stop** when at max age and no rope above (keep the climb case) |
| 2 | `rice` | Farmer's Delight (`RiceBlock`, `tick`) | flag | at max age, places rice panicles above once; nothing after that | **Stop** at max age when the block above is already panicles |
| 3 | `brown_mushroom_colony`, `red_mushroom_colony` | Farmer's Delight (`MushroomColonyBlock`, `tick`) | flag | `colony_age >= max` returns at once | **Stop** at max colony age |
| 4 | `inky_cap_mushroom_colony`, `morel_mushroom_colony` | Fungi Delight (FD's `MushroomColonyBlock`) | flag | same as 3 | **Stop** at max colony age |
| 5 | `crimson_fungus_colony`, `warped_fungus_colony` | Nether's Delight (`FungusColonyBlock`, `tick`) | flag | same as 3 | **Stop** at max colony age |
| 6 | `hemp` | Immersive Engineering (`HempBlock`, `tick`) | flag | at the last growth stage nothing is left to grow (byte-code read, confirm on the top block) | **Stop** at the last growth stage |
| 7 | `strawberry_bush` | Neapolitan (`StrawberryBushBlock`, `tick`) | flag | max age: no-op | **Stop** at max age |
| 8 | `mint` | Neapolitan (`MintBlock`) | flag | max age: no growth; the spread roll (sprouts mint around it) still runs | **Keep** (spread is the point of mint) |
| 9 | `adzuki_sprouts` | Neapolitan (`AdzukiSproutsBlock`) | flag | max age or flowering: returns | **Stop** at max age |
| 10 | `grapevine_stem`, `grapevine_lattice` | Vinery port (`PaleStemBlock`, `LegacyLatticeStemBlock`) | flag | `isMature` (max age with grapes) returns at once | **Stop** when mature; check the Vinery season mixins first (Creative Calendar) |
| 11 | `stone_lantern`, `deepslate_lantern` | Redeco (`StoneLanternBlock`) | `isRandomlyTicking` always true | mosses over; returns when waxed or fully mossed | **Stop** when waxed or at max mossing |
| 12 | 17 flower bushes (`poppy_bush` ... `wither_rose_bush`) | Floral Enchantment (`*VineBushBlock`) | `isRandomlyTicking` age < 3 | nothing: its growth method has the 1.18 signature (`randomTick(..., java.util.Random)`), which 1.19.2 never calls; the bushes only grow with bone meal | **Decide:** stop the ticks (no change in play), or make them grow (bridge `m_213898_` to the mod's method, a real gameplay change) |

Not counted: blocks outside the plant audit. A class scan for empty `randomTick` / `tick` bodies found only three
(Create metal scaffolding, Vinery dirt path slab, Botania solid vine), all scheduled-tick overrides, not random ticks.

## How to stop them (when approved)

- One Creative Common mixin on `BlockBehaviour.BlockStateBase.isRandomlyTicking` driven by a table (block id ->
  "stop when" predicate on its properties: max age, max colony age, waxed), checked once per state at load and cached,
  so the cost is one field read. Or per-class mixins on `isRandomlyTicking` (cheaper to reason about, more files).
- Mind the section count: a state's ticking flag changes the section's random-ticking count, so a fully grown farm
  section is skipped outright.
- Interactions to check: Creative Calendar crop season mixins, Creative Delight `StemExtraFruit` and soil boosts
  (`MulchBlockCropGrowthMixin`, `FarmBlockUnderCropsMixin`), Immersive Weathering growths (they run from random ticks
  of the block and stop with it; none of the candidates has an IW growth), WTHIT growth lines (unaffected).

## Next step

A `/dumprandomticks` in Creative Devtools: every registered block state, `isRandomlyTicking(state)`, block class and
mod. It confirms the "flag" rows, gives the full registry (not just plants), and makes this list repeatable. Then
the owner approves rows 1 to 12 and the mixin is built with a scratch test (perf suite C04 crop growth before and
after).
