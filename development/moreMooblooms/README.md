# moreMooblooms

**More Mooblooms** (`more_mooblooms`): mooblooms in many flower variants, on a cow of our own. Jar:
`moreMooblooms-<mod_version>.jar`. GPLv3 (LICENSE.txt).

Full reference, decisions and tests: [feature_writeup/mooblooms.md](../../feature_writeup/mooblooms.md) section 4, `MB-`.

## What it does

| Feature | Where |
|---|---|
| `more_mooblooms:moobloom`: a cow (milk, size, sounds) with a synced `Variant`, `Sheared` and `Flowers` | `entity/Moobloom`, `entity/MMEntities` |
| Variants are data: `data/<ns>/moobloom_variants/<path>.json`, 11 shipped (`moobloom` is the default), each with a weighted flower pool (optional entries for other mods' flowers); synced to clients on login and /reload | `variant/`, `network/SyncVariantsPacket` |
| Each of the three flower spots grows its own roll from the pool, saved on the moobloom; re-rolled on regrowth | `Moobloom.refreshFlowers` |
| Two-stage shearing: grown -> the flowers it carries, goes bare; bare -> back to a cow (+ three weighted pool drops). Players, deployers and dispensers all take the same path | `Moobloom.shearInternal` |
| Grazing like a sheep (head-bob too); eating grass regrows the flowers at `regrowChance` | `Moobloom.ate`, `client/MoobloomModel` |
| Breeding and calf growth use the variant's flowers, not wheat. Any two mooblooms breed: a `moobloom_breeding` combination, else same variant breeds true, else random by `breed_weight` | `Moobloom.isFood`, `variant/MoobloomBreeding` |
| A spawn egg used on a moobloom makes a calf of the egg's variant | `MoobloomSpawnEggItem.spawnOffspringFromSpawnEgg` |
| A plain cow fed any variant's flower converts at `feedChance` (babies too, the flower is always used); a flower in several pools picks a variant by `feed_weight` | `entity/CowFeeding` |
| Natural spawning: cows the spawner adds may become mooblooms by `moobloom_spawns` rules + `rareChance` (default 0, no cow rules shipped). Mooblooms a biome modifier spawns directly take their biome's rule | `spawn/MoobloomSpawns`, `mixin/NaturalSpawnerMixin` |
| Witherblooms, rarely, in soul sand valleys (config `witherbloomSpawns`): biome modifier + spawn rule in a built-in pack; witherbloom's `spawns_in_dark_on` lets it (and only it) spawn on soul sand / soul soil without grass or light | `packs/more_mooblooms/witherbloom_spawns/`, `MMEntities.checkMoobloomSpawnRules`, `Moobloom.checkSpawnRules` |
| One spawn egg, one stack per variant (creative + JEI), name and colours follow the variant | `item/MoobloomSpawnEggItem`, `client/ClientSetup` |
| Flowers drawn in three fixed slots (back front, back rear, head) | `client/MoobloomFlowerLayer` |

Config: `config/more_mooblooms-common.toml` (`MMConfig`).

## Content sets

The core is always on (plus the `witherbloom_spawns` pack, a config switch). Everything else only runs when its
mod is installed:

| Set (needs) | Code | Data |
|---|---|---|
| `buzzier_bees` | `compat/BuzzierBeesSet` (removes BB mooblooms on join, trader listing, compost value; `disableSunny`: denies the effect and strips it on join), `mixin/ItemHiddenMixin` (creative hiding), `mixin/TagLoaderStripMixin` (buttercup out of every tag), `mixin/MobSunBurnMixin` (priority 900, ahead of BB's Sunny burn check) | `packs/buzzier_bees/<switch>/`, one sub-pack per config switch |
| `jei` | `compat/jei/MoreMoobloomsJeiPlugin`: egg subtypes, info pages (pool odds, food, shearing), "Moobloom Breeding" (`MoobloomBreedingCategory`: combinations with the miss, breeds true, random calf, parent food) and "Moobloom Shearing" (`MoobloomShearingCategory`: drops per shear, rendered moobloom), "Moobloom Conversion" (`MoobloomConversionCategory`: cow + the variant's food, feed chance and shared-food share, rendered moobloom), hides buttercup / BB egg / Sunny / JEBr's BB moobloom page | - |
| `blueprint` | - | `packs/blueprint/`: Two by Two needs our moobloom |
| `alexsmobs`, `supplementaries`, `abnormals_delight`, `botania`, `delightful` | - | `packs/<mod>/`: the moobloom joins that mod's cow tags |

`MoreMooblooms.addPacks` adds `packs/<mod>/` as hidden, required built-in data packs. The Buzzier Bees sub-packs go on
top (they replace BB's files by path); the pack's `kubejs/data` still loads above them.

**Buzzier Bees is under the Abnormals License.** This jar contains nothing of it: no code, textures, models, lang or
data copied from it, no mixin into it, no compile dependency. It is referred to only by registry ids, and changed
only through our own data files, Forge events and mixins into vanilla classes. See mooblooms.md 4.0.

## Adding a variant

A JSON in any data pack (`kubejs/data/<ns>/moobloom_variants/<path>.json`), a texture at
`assets/<ns>/textures/entity/moobloom/<path>.png` (64x32, cow layout) and a lang key
`entity.more_mooblooms.moobloom.<path>` (`.<ns>.<path>` outside our namespace), the name alone ("Sunbloom").
Format in mooblooms.md 4.4 (variants) and 4.8 (breeding). `python3 tools/check_moobloom_data.py` (instance
root) checks the JSON, textures, lang and breeding files.

The shipped variants and breeding files are generated from the variant table in mooblooms.md 4.4: edit the
table, then `python3 tools/gen_moobloom_variants.py` (instance root; `--check` for a dry run). It also runs
`tools/egg_colors.py`, which sets each variant's egg colours from its coat.

## Build

Part of the root Gradle build:

    ./gradlew :moreMooblooms:build          # -> build/libs/moreMooblooms-<ver>.jar
    ./gradlew :moreMooblooms:installToMods  # copy into ../../mods (not while the game runs)

- `libs/` (git-ignored) holds the compile-only JEI jar copied from `../../mods`.
- No refmap: mixins use SRG names with `remap = false`, the AP target validator is off (same as patcherMod).
- The coats are drawn for this mod (working copies in `../../textureswapper/moobloom/`); nothing of Buzzier
  Bees' art is used.
