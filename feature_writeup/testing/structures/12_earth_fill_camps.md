# Earth fill on water, camp plinths, metas off coasts, Quark spire teleport (2026-10-19)

User, 2026-10-19: "address the structure earth fill issues i outlined in my scratchpad ... water replacement cancel
should also have a check if the number of water blocks converted is a significant (configurable, go with 50% for now)
portion higher than the solid blocks replaced or left alone by the footprint ... tent structures are consistantly
spawning with 3-4 block high footprint sticking out of the ground ... a disabled by default config switch to turn off
metastructure generation in any coast/river biome. test these changes as well as the quark spire tp on a scratch
server". Plan and reference: [structure_tuning.md](../../structure_tuning.md) 7.5 (and 3.13, 3.15, 3.16). Live notes:
12_earth_fill_camps.scratch.md.

## 1. Changes

| Issue | Fix | Files |
|---|---|---|
| Camps and ruins on shallow lakes pass earth fill ("0 over water": the beard fills water up to 6 deep, so the lake becomes an island under them) | Footprint water share: each earth piece's box, at the layer the start was set onto the surface with, read for water (filled by earth or the beard, or left standing) and dry ground (replaced or left alone). Cancel when water > ground x (1 + `maxWaterSurplus`), default 0.5. Seabed, air and spots under more than `baseLayers` blocks of terrain count as neither. Not for `water_fill/structures` | patcherMod `worldgen/EarthFill`, `CEConfig`, `WorldgenConfig` |
| Campsite Structures' camps on a 4-block dirt plinth | The beard levels the ground to a piece's bottom layer; the camps' start height sinks a 4-layer dirt dome into the ground, so the beard dug the surface down around it. New structure tag `terrain_adaptation/sunk_start`: the beard is raised by the start's sink. The 6 camps tagged; start height -5 -> -4 (grass layer flush, the old features' -5 left a 1-block dip) | patcherMod `worldgen/TerrainAdaptation`, `kubejs/server_scripts/worldgen/terrain_adaptation.js`, `kubejs/data/campsite_structures/worldgen/structure/*.json` |
| Metas on coasts and rivers | `[worldgen.meta_structures] avoidCoasts` (off by default): no part starts in or reaches into `creativeengineering:coast_river`; the 6 water metas in `meta/coast_ok` exempt | patcherMod `worldgen/MetaStructure`, `CEConfig`, `WorldgenConfig`; `biome_fits.json` (biome_tags `coast_river`), `structure_tags.json` (`meta/coast_ok`) |

Offline: `./gradlew build` clean; `tools/test_worldgen_tools.sh` all 10 sections pass (earth fill 64 checks: 7 new
footprint checks, 2 burial checks); `tools/test_structure_groups.sh` node and Rhino identical.

## 2. Scratch server

Dedicated server in the session scratchpad (libraries from `servers/Minecraft1192withForge`, 373 server mods by
`mod_sides.md`), seed 8675309, `structureDensity` 4, `[worldgen.earth_fill] log = "ALL"`, `[worldgen.meta_structures]
log = true` (srv toml only). A scratch-only KubeJS probe (`/zground x z r`: ground heights around a point; `/zcol`:
the top blocks of a column; `/zcfg`: live config values) read the results.

| World | Jar | What |
|---|---|---|
| diag1 | the 13:50 1.0.9 from `mods/` (before) | find the plinth and its cause |
| diag2 | first build | camp A/B, footprint share, Quark teleport |
| diag3 | final build (burial rule, campsites at -4) | camp flush, `avoidCoasts` off/on, share census (Chunky r 400 at 2144 -2592) |

**Camp plinth (ST-199, ST-200).** The armorer campsite at chunk [-7, -9] in all three worlds (same seed):

| World | Camp grass layer | Ground around it | Look |
|---|---|---|---|
| diag1 | y 94 | y 90 (a flat ring dug out of a ~95 surface) | 4-block dirt plinth |
| diag2 (start height -5) | y 94 | y 95 | 1-block dip, no plinth |
| diag3 (start height -4) | y 95 | y 95 | flush |

`[terrain-adaptation] world load: 103 structures tagged, modes {NONE=6, SOFT=91}, 95 trimmed, 6 sunk starts`.

**Footprint share (ST-201, ST-202).**
- diag2: the fisher campsite at [-36, -7], kept by the old check, "footprint 169 water / 0 ground (too wet) ->
  CANCELLED". Same run: false positives underground: Philip's ancient dungeon (bury) and Subterrestrial's cabins,
  whose bottom layers lie deep under the sea (every solid there is seabed) or in aquifers. Fixed with the burial rule.
- diag3, 2,601 inland chunks: 972 starts checked, 40 cancelled, 21 "too wet", 14 by the share alone: a T&T sunflower
  plains village (739 water / 0 ground) and a birch forest village (1,691 / 497) on one lake, two drum towers (2,123 /
  66), a windmill (255 / 0), a big horse stable (505 / 244), T&T outposts (old growth taiga x2, taiga, Tudor), the
  desert watchtower, a Mysticrift jungle tower. No underground structure. The drum tower spot, generated and probed:
  open water at y 62 with a strip of shore on the west. Kept starts with any footprint water: 5 of 926, all villages
  at a shore (113 water / 462 ground up to 470 / 425).

**avoidCoasts (ST-203, ST-204).** `/place structure` at the spawn beach (-80 100 -16, `minecraft:beach`; the tag
answers `locate biome #creativeengineering:coast_river`):

| Meta | Off | On |
|---|---|---|
| `farmers_camp` | built (anchor: a beach fish pond at -90 57 -4) | not placed, no anchor fits |
| `house_camp` | built | not placed, no anchor fits |
| `ruin_scatter` | built | not placed, no anchor fits |

With the switch on, at 2144 -2592 (nearest river 101 blocks away) `ruin_scatter`, `house_camp`, `farmers_camp`,
`ruin_cluster` and `waysign_camps` all built. `meta/coast_ok` resolved to its 6 water metas, and a driftwood flotilla
assembled during the run. The switch did NOT reload live: Forge kept the old value 10 minutes after the toml edit
(`/zcfg`); it took a restart.

**Quark spire teleport (ST-205).** An armor stand teleported from spawn to 300016 100 300016:
- Overworld (hook cancelled by `QuarkSpiralSpiresTeleportMixin`): done in 4 ms; no region file at the target.
- End (hook kept on purpose): 38 s server stall ("766 ticks behind"); the target regions r.585.585 to r.586.586 were
  generated. The first End try found no entity: the stand was summoned right after `forceload` into a chunk not yet
  entity-ticking.

## 3. Deploy state

`mods/creativeengineering-1.0.9.jar` replaced by this build (md5 b82483aa; the 13:50 build was 5b7bb926). Not
deployed. Instance `config/creativeengineering-common.toml` gains `maxWaterSurplus` and `avoidCoasts` on the next start
(Forge adds missing keys).

## 4. Still open

- End teleports into unexplored terrain still stall on Quark's hook. Option: skip it when the target chunk isn't
  loaded, in every dimension.
- `maxWaterSurplus` 0.5 is a first value: look at share-only cancels in game. A village on a lake goes as a whole.
- Sunk-start candidates not tagged: Philip's crypts, ocean fortress, ocean ruins, -1 ruins, T&T nilotic village.
- `avoidCoasts` on in a coastal area also stops land metas whose satellites can't avoid a nearby beach or river
  (at -104 -150, 32 blocks from a gravel beach, three land metas failed with it on; not compared with it off there).
