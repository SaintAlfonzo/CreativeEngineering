# gameplay / misc: implementation brief

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# gameplay`, Sub `misc` (row 15, from the scratchpad of 2026-10-24;
the older open rows 2, 6, 9 and 14 have no section yet). Layout per [README.md](README.md). Feature writeup:
[fixes_compat.md](../fixes_compat.md) (test prefix `FX-`, last id FX-45 in use).

## Scope and order

One new row, `misc #15`.

---

## misc #15: Immersive Weathering on Upgrade Aquatic coral
Status **Planned** / Work data / config / TODO `gameplay/misc #15`

### Goal
Upgrade Aquatic's corals grow and spread through Immersive Weathering's growth system the way the vanilla corals do.

### Now
- IW 1.2.10 does it for the five vanilla coral kinds with data: `data/immersive_weathering/block_growths/<kind>_coral.json`
  (chance 0.0005, `generate_if_not_too_many` area 2x2x2 needing 6, biome predicate `minecraft:warm_ocean`, per-face
  results: wall fan 50 : wet sponge 1 on the sides, fan 25 : coral 25 : wet sponge 1 upward, `replacing_target` water) and a
  recipe `<kind>_coral_block.json` (4 coral -> block). The Weathering and Growth JEI tab already shows the vanilla ones
  (JW-03: brain coral block -> fan 77%, coral 21%, sponge 2%).
- Upgrade Aquatic 5.1.0 has 11 coral kinds with blocks `<kind>_coral_block`, `<kind>_coral`, `<kind>_coral_fan`,
  `<kind>_coral_wall_fan` (acan, branch, chrome, finger, moss, petal, pillow, prismarine, rock, silk, star, from the
  blockstate list) plus coralstone variants, and its own recipes `<kind>_coral_block`. UA has its own worldgen
  biomes (coral reef); their ids are unverified (the jar has no `worldgen/biome` folder listing here: check the biome
  modifier or the code).
- The repo has no `kubejs/data/immersive_weathering/` yet; `kubejs/data/` holds the pack's other datapack folders, and
  `tools/jei_world/tab_weathering.py` reads IW growths from the mod jars with `kubejs/data` on top, so new files show up on
  the tab after `/cegen run jei_world`.

### Build
1. Generate, do not hand-write: `tools/gen_ua_coral_growths.py` (new, listed in `tools/generators.json`) reads the vanilla
   `brain_coral.json` template from the IW jar and writes 11 files `kubejs/data/immersive_weathering/block_growths/ua_<kind>_coral.json`
   swapping block names (`upgrade_aquatic:<kind>_coral`, fan, wall fan) and the biome predicate.
2. Biome predicate: UA's coral reef biome(s) instead of `warm_ocean` (find the ids first; use a biome tag if UA has one).
3. Recipes (4 coral -> block) already exist in UA; skip IW's.
4. The row's "config" half: `config/immersive_weathering-common.toml` may need nothing; check that block growths are on.

### Decisions
Biomes (UA reef only, or reef plus warm ocean). Recommend reef only. Whether dead corals (UA has `dead_*` kinds: unverified)
also grow: recommend no, as in IW's vanilla files.

### Gotchas
Data only: `/reload`, but IW reads block growths at server data load. The JEI tab grows by one row per kind; the 82-row tab
count in jei_world_tabs.md changes.

### Tests (FX-)
- FX-46 (proposed): near a UA coral reef over time (or `randomTickSpeed 100`): a UA coral block spreads fans and corals;
  JEI Weathering and Growth shows the 11 rows.
