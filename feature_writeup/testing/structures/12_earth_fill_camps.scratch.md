# 12 Earth fill + camps: scratch log (2026-10-19)

Live notes while testing; the clean writeup is 12_earth_fill_camps.md. Plan: structure_tuning.md 7.5.

## State

- Code built (patcherMod 1.0.9 build/libs, 14:33): footprint water share (EarthFill), sunk_start beard (TerrainAdaptation),
  avoidCoasts (MetaStructure). Data: biome_fits.json biome_tags.coast_river, structure_tags.json meta/coast_ok,
  terrain_adaptation.js sunk_start = 6 campsites.
- Offline: test_worldgen_tools.sh all 10 sections pass (earth fill 62 checks, 7 new footprint checks);
  test_structure_groups.sh parity identical.
- Scratch: diag1 (old 1.0.9 jar from mods/) done; diag2 (new jar) booting.

## Setup (rebuild if the scratchpad is wiped)

Scratchpad scripts: setup_srv.sh (LEVEL, JAR, SEED env; libraries + run.sh from
~/.local/share/atlauncher/servers/Minecraft1192withForge, kubejs/config/defaultconfigs copied, vinery_upstream linked,
mods symlinked by mod_sides.md, creativeengineering swapped for JAR), boot.sh (fifo + nohup), cmd.sh (echo to fifo),
tune_toml.sh (structureDensity 4, [earth_fill] log ALL, [meta_structures] log true), zz_probe.js (scratch-only
/zground x z r: ground height map relative to the centre, a..i = -1..-9, w = water; /zcol x z n: top n blocks).
server.properties: seed 8675309, port 25599, online-mode false, max-tick-time -1, view 6.

## diag1: old jar, find the tent plinth

- Boot 315 s. locate of the 3 tent mods (big_villager_tent, circus, snowy) timed out at 120 s each from spawn.
- Earth-fill ALL lines showed camps checked during the locates: campsite_structures:armorer_campsite chunk [-7, -9] kept.
- forceload -128 -176 .. -80 -112, /zground -106 -152 14: a round plateau 4 above a flat ring (the ring 4 below
  the centre, terrain rising beyond). /zcol -104 -152: bricks 95-96 (template y5), dirt 94 .. 89; natural grass at
  -96 -152 is y 90. So the template's grass layer (y4) is at 94, 4 above the ground: the dirt dome y0-3 stands out.
- Cause: Campsite templates (13x16x13) are a dirt dome y0-3 (37/73/121/121 blocks) under a grass layer y4; the port
  uses start_height -5 (the old feature placed the template at surface - 5: ArmorerCampsiteFeature bytecode, heightmap
  - 1, then -5). JigsawPlacement puts minY = H - 1 + startHeight (verified in the mapped 1.19.2 bytecode), right.
  But Beardifier levels the ground to minY + groundLevelDelta (the bottom layer), so beard_thin dug the noise
  surface (about 95) down to 90 around the camp and the dome stands on a flat ring.
- Other projected structures with negative start heights (not changed): Philip's ancient_crypt/antiquus_crypta/
  ocean_fortress -5 beard_box, ocean_ruins -3, ancient_ruins/rare_ruin/end_gate_fortress -1, T&T nilotic -1,
  aquacultures_carp -4 (none), unwrecked ship -2 (none).

## diag2: new jar
- Boot 335 s. `[terrain-adaptation] world load: 103 structures tagged, modes {NONE=6, SOFT=91}, 95 trimmed, 6 sunk starts`
  (reflection on JigsawStructure f_227608_/f_227610_ works). Forge added maxWaterSurplus and avoidCoasts to the toml.
- Camp A/B (same seed, same density): armorer camp at chunk [-7, -9]: /zground -106 -152 14 now shows the camp
  plateau ("0", y 94) inside a ring at "1" (y 95, the noise surface H-1), terrain natural beyond; /zcol -104 -152:
  bricks 95-96, dirt 94..90, stone 89. Plinth gone. With -5 the camp sits 1 below the ring (the old feature's dip,
  template air in y5); start_height changed to -4 for all 6 (same layout: dome y0-3, ground y4) so it is flush.
- Earth-fill log: armorer camp "footprint 0 water / 72 ground -> kept". campsite_structures:fisher_campsite at
  [-36, -7] "0 over water ... footprint 169 water / 0 ground (too wet) -> CANCELLED" (in diag1 it was kept: the beard
  filled the shallow water, so maxOverWater never saw it). forest_watchtower same chunk too wet as well.
- 326 checked, 64 cancelled, 47 too wet. FALSE POSITIVES: philipsruins:ancient_dungeon (bury, underground) x6,
  subterrestrial:deepslate_cabin, subterrestrial:ocean_cabin x~30 (most already over the 24 water limit). Their footprint
  layer is deep underground: under the sea every solid block is SEABED (not ground), aquifer water counts as water.
  Fix: a footprint spot with more than baseLayers (3) blocks of predicted terrain above it is buried, counted as
  neither (Terrain#top). Rebuilt; earth fill tests 64 pass (2 new burial checks).
- Quark spire hook: armor stand summoned at -100 100 -140, `tp @e[tag=zt,limit=1] 300016 100 300016`: done in 4 ms
  (14:45:08.316 -> .320), no region file r.585.585 in the Overworld. End control (hook kept there): forceload 0 0 in
  the End, summon, tp to the same coordinates: 38 s server stall (766 ticks behind), r.585.585 .. r.586.586 created in
  DIM1. So the mixin cancels the hook outside the End, and the hook still runs (and still stalls on new terrain) in the End.
  First End try: "No entity was found" (summoned in a chunk not yet entity-ticking right after forceload); retried after 3 s.

## diag3: final jar (14:55 build), campsites at -4

- Boot 332 s, 6 sunk starts. Camp at [-7, -9]: /zground -106 -152 12 all "0" around the camp, grass layer y 95 =
  surroundings y 95: flush. /zcol -104 -152: bricks 96-97, dirt 95, 94.
- avoidCoasts off: /place structure meta/farmers_camp, house_camp, ruin_scatter at -80 100 -16 (spawn, minecraft:beach;
  locate biome #creativeengineering:coast_river works, "(minecraft:beach) ... 0 blocks away") all built; farmers camp
  anchored on aquacultures_beach at -90 57 -4 (beach at y 57 too). waysign_camps: no anchor fits (with the switch off too).
- avoidCoasts = true edited into the srv toml while running: NOT reloaded live (/zcfg scratch command:
  WorldgenConfig.metaAvoidCoasts() still false 10 min later). Restart (same world): avoidCoasts=true.
- avoidCoasts on: same three at the beach: "not placed, no anchor fits" (sunken_ruin_cluster too, as off: no
  underwater anchor on land). At -104 -150 (a gravel beach 32 blocks away) ruin_scatter/house_camp/waysign_camps fail
  too. At 2144 -2592 (nearest river 101 blocks): ruin_scatter, house_camp, farmers_camp, ruin_cluster, waysign_camps
  all built. `locate structure #creativeengineering:meta/coast_ok`: 6 members; a driftwood_flotilla assembled during it.
- Ratio on the final jar: avoidCoasts off again (restart), Chunky square r 400 at 2144 -2592 (2601 chunks, 11:55).
  972 checked, 40 cancelled, 21 too wet; 14 cancelled by the share alone: T&T village_birch_forest + sunflower_plains
  at [102, -180] (1691/497 and 739/0), 2 drum towers (2123/66 at [155, -192]), windmill 255/0, big horse stable 505/244,
  T&T outposts (old growth taiga x2, taiga, tudor), desert watchtower, mysticrift jungle tower. No underground ones.
  Kept with any footprint water: 5 of 926 (villages: savanna 470/425, tudor 395/568, meadow 805/1262, plains 113/462 x2).
  Probe of the drum tower spot (forceload 2464 -3088 .. 2512 -3040, /zground 2488 -3064 24 2): open water at y 62
  with a shore strip on the west. 0 ERROR lines from our loggers (417 total, recipe-generator noise).
