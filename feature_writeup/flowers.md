# Wild flowers (`wild_flowers.json`)

## 0. At a glance

| Part | Status | Open |
|---|---|---|
| Rules engine (`wild_flowers.js`): Botania palettes, pack flower patches, existing-feature scale/remove, Flower Patch mixing | BUILT 2026-10-07, offline checks pass, scratch server verified (History); DEPLOYED with pack 1.0.6 (2026-10-14); applied in single player 2026-10-11 | FL-01 to FL-14 |
| First tuning pass (palettes for 124 land biomes, 19 flower entries, 2 existing tweaks) | BUILT | your in-game look |
| CEConfig `[worldgen.systems] wildFlowers` | BUILT, in patcherMod 1.0.5 (ships in 1.0.5), on in the shipped config | |
| Generator + report (`tools/gen_wild_flowers.py` -> [flowers_by_biome.md](flowers_by_biome.md)) | BUILT | |

- **Edit:** [wild_flowers.json](../kubejs/generation_scripts/worldgen/wild_flowers.json). Rejoin the world; only new chunks change.
- **Knobs:** `density` (everything the file adds), `botania.rate/rates/palettes`, per-entry `rate/rates/where/blocks/patch`,
  `flower_patch.share/existing_share/sizes`, `existing.<feature>.scale/remove/flower_patch`.
- **Measure:** `python3 tools/gen_wild_flowers.py`, then read [flowers_by_biome.md](flowers_by_biome.md): Botania color
  coverage (section 1), flower totals (2), gaps (3), per biome (4).
- **Check:** `bash tools/test_wild_flowers.sh`.

## 1. Overview

Before this, most land biomes had only Terralith's `flower_default` (poppy and dandelion, one patch per 32 chunks),
and Botania put all 16 mystical colors in every overworld biome at the same rate (1/128 per color). 51 grassy biomes
(taigas, jungles, savannas, snowy and windswept biomes, Yellowstone at 4.2% of the overworld) had under 0.25 flower
patches per chunk. No flower spawned as a Flower Patch block.

The system, applied at world load like [wild crops](crops.md) (section 2.8):

- **Botania by color.** Each land biome has a palette of the colors that suit it (lavender = purple and magenta, dark
  forest = black, volcanic = black/gray/red, sakura = pink, snow = white/light blue, savanna = orange/red/brown...).
  A Botania patch picks one color from its biome's palette. Rate per biome (flower biomes 1/3, most 1/8, deserts,
  peaks and ice 1/24).
- **Pack flower patches** (`flowers`): weighted vanilla/modded flowers placed where they make sense. They fill the gaps
  (taiga floor, snowfield whites, savanna, jungle orchids, alpine) and push flowers into biomes whose color or climate
  fits (alliums in lavender, pink tulips and clover in sakura, orchids in swamps, lily of the valley under oak and dark oak).
- **Existing features** (the biome files' own flower features): any can be rescaled, removed from chosen biomes, or
  given a different Flower Patch share. Poppies/dandelions are gone from snow, desert and bare badlands; swamp
  orchids doubled.
- **Flower Patch everywhere.** A share of every small vanilla flower the system places or rewrites is a Flower Patch
  block with 2, 3 or 4 flowers (default 25% in pack patches, 15% in existing features, sizes 3:2:1).
- **Buzzier Bees clovers** taken over (white clover on lawns and pastures, pink in flower biomes and birch). The
  buttercup stays gone: More Mooblooms removes it pack-wide.

## 2. Reference

### 2.1 Flower sources in this pack

| Source | How it reaches biomes | Controlled by |
|---|---|---|
| Terralith (every overworld biome file) | `minecraft:flower_*`, `forest_flower_*`, `patch_sunflower`, `terralith:*/flowers` (28 features) | `existing` (scale, remove, Flower Patch share) |
| Botania mystical flowers | `botania:add_mystical_flowers` modifier, one random color of 16 | replaced by `botania` (palettes) |
| Buzzier Bees white / pink clover | Java features, modifiers on birch / forest | replaced by `flowers.white_clover`, `flowers.pink_clover` |
| Buzzier Bees buttercup | modifier, sunflower plains | off: More Mooblooms `removeButtercupWorldgen` (built-in pack) |
| Upgrade Aquatic searockets, pickerelweed, flowering rush | Java features on beach / river / swamp | not touched (aquatic) |
| Twigs azalea flowers | modifier `#twigs:azalea_flowers_generate` | not touched (azalea floor litter) |
| Flower Patch | no worldgen of its own | `flower_patch` |
| Bushier Flowers | off (pack, `forge:none`) | see bushier-flowers memory / mob tweaks backlog |

### 2.2 Rules

- **Traits** come from the `biomes` table in [wild_crops.json](../kubejs/generation_scripts/worldgen/wild_crops.json):
  one table for both systems. Ocean and cave biomes never match.
- **Selectors** (`rates`, `existing.remove`): a biome id (has `:`) or a trait clause. Clause = traits that must all
  hold, `!x` must not.
- **Entries** (`flowers`): `where` clauses OR'ed, then `add`, then `except` (same as wild crops).
- **Rates**: patches per chunk (`"1/8"`, `"2"`). `rates` overrides per biome, first match wins; then `density`
  multiplies. A rate becomes a tier: `c<N>` = N per chunk, `r<N>` = one in N chunks (rounded). Biomes on the same tier
  share one feature and one biome tag.
- **Botania**: a palette is space-separated dye colors, `color*N` = N times as likely; `_default` for unlisted biomes,
  `""` = none. Biomes with the same palette and tier share one `simple_random_selector` over Botania's per-color
  patches (Botania's own patch features: 32 tries, 5% tall double flowers).

### 2.3 What gets generated (examples)

At world load into KubeJS's top-priority datapack (above jars and kubejs/data):

| File | Example |
|---|---|
| configured feature | `creativeengineering:wild_flowers/lavender/c1`: `minecraft:flower`, 48 tries, weighted allium 3 (25% as allium patches) + lilac 1 |
| placed feature | same id: `count 1`, `in_square`, heightmap `MOTION_BLOCKING_NO_LEAVES` (forest floors, not tree tops), `biome` |
| biome tag | `#creativeengineering:wild_flowers/lavender/c1` = lavender forest + lavender valley |
| modifier | `creativeengineering:wild_flowers/lavender/c1`: `forge:add_features`, `vegetal_decoration` |
| Botania | `wild_flowers/botania/<n>` (one per palette+tier, about 100) |
| mod modifiers off | `botania:add_mystical_flowers`, the two Buzzier Bees clover modifiers -> `forge:none` |
| existing rewrite | `minecraft:flower_default` configured feature with patch blocks; `minecraft:flower_swamp` placed at rarity 16 |
| existing removal | `wild_flowers/remove/minecraft/flower_default`: `forge:remove_features` over its tag |

### 2.4 Flower Patch conversion

`flowerpatch:<flower>_patch` blocks are `FlowerBlock`s with `flowers=2..4`. For a share `s` and sizes `{2:3,3:2,4:1}`,
each convertible flower becomes weights plain `(1-s)`, 2-patch `s*3/6`, 3-patch `s*2/6`, 4-patch `s/6` (as integers).

| Provider in a feature | Converted how |
|---|---|
| `simple_state_provider` | becomes a weighted provider |
| `weighted_state_provider` | each flower entry split by the weights, others scaled so shares hold |
| `noise_threshold_provider` | low/high lists are picked uniformly: entries repeated by weight |
| `noise_provider` | entries repeated; non-flowers repeated by the same total, so noise bands keep their size (patches cluster in sub-bands) |
| `dual_noise_provider` | not converted (variety picks would collapse). Used by `flower_meadow` (the `meadow` entry adds patch blocks there) and the orchid swamp's flowers (its other flowers come from `swamp_orchids`, which has patches) |

Tall flowers, Botania flowers, clovers and modded flowers have no patch block and stay plain.

### 2.5 Bone meal

Pack patches with only small flowers are `minecraft:flower` features, so bone meal on grass can grow them, but vanilla
only uses a biome's FIRST flower feature, which is usually Terralith's. Converted existing features do yield Flower
Patch blocks from bone meal.

## 3. What the pack changes

First pass (2026-10-07), numbers from [flowers_by_biome.md](flowers_by_biome.md):

- Grassy biomes under 0.25 patches per chunk: 51 -> 0.
- Botania per color, per 1000 overworld chunks: 4.6 each before; after 2.6 (lime, gray) to 8.2 (brown). Black lives in
  dark forest (1/3, black x3), old-growth spruce, mushroom fields and the volcanic/ash biomes.
- Cornflower 14 -> 33, lily of the valley 3.6 -> 22, azure bluet 2.6 -> 11, orange tulip 5 -> 13 per 1000 chunks.

| Entry | Where | Rate |
|---|---|---|
| taiga_floor | coniferous / cold forests, not snowy | 1/4 (Yellowstone 1/3) |
| snow_flowers | snowy grassy, not icy | 1/6 |
| savanna_flowers | savanna, shrubland, grassy badlands, autumn | 1/5 |
| savanna_sunflowers | flat savanna, steppe | 1/24 |
| jungle_flowers / jungle_roses | jungles (not sky) | 1/4 / 1/16 |
| swamp_orchids, marsh_flowers | swamps, ice marsh | 1/6, 1/8 |
| plains_wildflowers | plains, highlands, clearings | 1/6 |
| alpine_flowers | grassy mountains, windswept hills | 1/5 |
| woodland_floor, dark_forest_floor | oak/birch forests, dark oak | 1/6 |
| lavender, sakura, moonlight | those Terralith biomes | 1, 1/2, 1/2 |
| meadow | meadow (patch blocks) | 1/3 |
| white_clover, pink_clover | lawns/pastures; flower biomes and birch | 1/24, 1/12 |

## 4. Limits and decisions

- **Same runtime pattern as wild crops** (highPriorityData, rejoin to apply, CEConfig switch, generator mirror + Rhino
  test). Rejected: writing static files to kubejs/data (needs a script run after every edit).
- **Existing features need their JSON at world load**, and KubeJS can't read jar files. The generator snapshots them
  into `flower_features.json`, like `structure_config.json`. Regenerate after a mod update or `gen_soil_compat.py`; the
  test fails while it is stale. The snapshot is the effective JSON, so the soil-compat overrides survive the rewrite.
- **Existing features are tuned per feature, not per biome**: `scale` is global, `remove` takes them out of chosen biomes.
  A per-biome rate for an existing feature would need remove + pack copy (the wild-crops trick); add a `flowers` entry
  for the extra instead.
- **One feature per palette and rate** keeps each Botania patch a single color from that biome's palette, like Botania's
  own selector. Rejected: one feature per color (the share per biome would depend on palette size and rate together).
- **Rates are relative.** A patch fails on sand, stone and snow; Terralith patches differ in tries. The report counts
  patches, not blocks.
- **Buttercup**: left out on purpose (More Mooblooms removes it everywhere). Naming it is a config problem.
- **Mooblooms follow these flowers** ([mooblooms.md](mooblooms.md) 2.8, 4.9): the moobloom spawn rules were
  hand-translated from this system's inventory on 2026-10-08. After changing `wild_flowers.json`, regenerate
  mooblooms.md 2.8 and retune the affected moobloom rules; nothing does it automatically yet.
- **Old worlds**: only chunks generated after the rejoin change; existing flowers stay.

## 5. File map

| File | Role |
|---|---|
| [wild_flowers.json](../kubejs/generation_scripts/worldgen/wild_flowers.json) | the config |
| [flower_features.json](../kubejs/generation_scripts/worldgen/flower_features.json) | generated snapshot of existing flower features |
| [wild_flowers.js](../kubejs/server_scripts/worldgen/wild_flowers.js) | runtime: tags, features, modifiers, live tag check |
| [wild_crops.json](../kubejs/generation_scripts/worldgen/wild_crops.json) | biome traits (shared) |
| [gen_wild_flowers.py](../tools/gen_wild_flowers.py) | snapshot + report + config checks |
| [test_wild_flowers.sh](../tools/test_wild_flowers.sh), [RhinoWildFlowers.java](../tools/wild_flowers_test/RhinoWildFlowers.java) | offline test |
| [flowers_by_biome.md](flowers_by_biome.md) | generated report |
| `development/patcherMod/.../CEConfig.java`, `worldgen/WorldgenConfig.java` | `wildFlowers` switch |

## 6. Offline checks

`bash tools/test_wild_flowers.sh`:

1. Generator: no config problems, every Botania color generates somewhere, snapshot already current.
2. The real script in the pack's Rhino: every biome tag equals the generator's plan; every modifier points at a filled
   tag and an existing feature; weights are positive integers; each Flower Patch rewrite (configured and inline) is
   identical to the generator's conversion; forge:none count matches.
3. Switch off: no tags, no files.
4. Botania missing: its features and modifier are skipped, everything else still written.

2026-10-07: all four pass (119 tags, 378 files, 18 configured + 1 inline Flower Patch rewrites).

## 7. In-game test suite

Use a new world (or new chunks). `/locate biome` then fly over.

- [ ] **FL-01 Load:** join a new world -> log has `[wild-flowers] 99 Botania palette features, 19 flower features ...` and `... flower/biome pairs in 119 tags` with no problems; no datapack error screen
- [ ] **FL-02 Lavender:** lavender forest/valley -> alliums carpet the ground, some allium patches, lilacs; Botania flowers purple/magenta/light blue (pink in the valley)
- [ ] **FL-03 Dark forest:** black Botania flowers are the most common color; lily of the valley and alliums under the canopy
- [ ] **FL-04 Taiga / Yellowstone:** cornflowers and lily of the valley on the floor (were nearly bare); Botania green/blue/brown/orange
- [ ] **FL-05 Snow:** snowy plains/taiga -> sparse white flowers (bluets, white tulips), no poppies or dandelions; Botania white/light blue
- [ ] **FL-06 Savanna:** orange/red tulips and dandelions, a few sunflower stands on flat savanna; Botania orange/red/brown/yellow
- [ ] **FL-07 Jungle + swamp:** blue orchids common in jungles and swamps, some rose bushes in jungles
- [ ] **FL-08 Sakura:** pink tulips, peonies, pink clover; Botania pink and magenta
- [ ] **FL-09 Flower Patch:** in plains/forest/lavender, some flowers are multi-flower patches (2, 3, 4); breaking one drops that many
- [ ] **FL-10 Clover:** white clover in plains/forests, pink clover in flower forest/meadow/birch
- [ ] **FL-11 Every Botania color:** over a long trip, find all 16 colors (flowers_by_biome.md section 1 lists where)
- [ ] **FL-12 Switch off:** `wildFlowers = false` (needs the rebuilt patcherMod jar), rejoin, new chunks -> Botania random colors everywhere, no pack patches
- [ ] **FL-13 Meadow:** meadow still looks like vanilla's mix, now with a few patch blocks
- [ ] **FL-14 Bone meal:** bone meal on grass in plains -> flowers, sometimes a Flower Patch block

## 8. History

- 2026-10-07: built. Config + first tuning pass, runtime script, generator/report, Rhino test, CEConfig switch (source
  only). Flower Patch configurable by share and size per entry and per existing feature (user request mid-build).
- 2026-10-07: scratch dedicated server (instance mods + kubejs, patcherMod 1.0.5 without the switch): world loads,
  `[wild-flowers] 99 Botania palette features, 19 flower features ... (378 files)` and `291 flower/biome pairs in 119
  tags` with no problems. `/place feature` x4 on 17x17 grass pads: `wild_flowers/lavender/c1` -> 16 allium patches,
  lilacs, plain alliums; Terralith's `minecraft:flower_default` (rewritten) -> 8 of 49 as poppy/dandelion patches
  (setting 15%); `taiga_floor/r4` -> cornflower and lily of the valley patches; `minecraft:flower_plain`
  (noise_threshold) -> cornflower patches. Natural generation across biomes not looked at (FL-02 onwards).
- 2026-10-11: ships in pack 1.0.5 (staged, not deployed): `wild_flowers.json`, `flower_features.json` (snapshot, now a
  runtime file in `KUBEJS_CLASSES`), `wild_flowers.js` and the CEConfig switch in patcherMod 1.0.5. Single player with
  the staged files: `99 Botania palette features, 19 flower features ... 378 files`, `291 flower/biome pairs in 119
  tags`, no errors. Only new chunks change.
- 2026-10-14: pack 1.0.5 was never deployed; everything above that was staged for it ships in pack **1.0.6** (STAGED
  2026-10-14, update from 1.0.4, [plan_1.0.6.md](plan_1.0.6.md)), unchanged.
- 2026-10-19: Terralith's moonlight blue flower patch (`terralith:flower/blue/flowers`) actually generates now. It was
  a `nether_forest_vegetation` patch, which only places on nylium, so it never fired on grass (the flower model here
  counted it anyway); [nether_worldgen.md](nether_worldgen.md) B8 turned it into a vanilla flower patch with the same
  tries and spread and took its warped roots and nether sprouts out. `gen_wild_flowers.py` was rerun so
  `flower_features.json` (which `wild_flowers.js` rebuilds the feature from) has it; flowers_by_biome.md also
  refreshed for the new biome shares ([biome_tuning.md](biome_tuning.md)).
