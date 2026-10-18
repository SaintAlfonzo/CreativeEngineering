# M6 scratchpad: Phase 6 (8.0.6, pregenerated area)

## State (keep current)

- Constraints (user 2026-10-14): ignore Nether/End; ignore structure-to-structure rates (so ST-72, 91, 98, 101 and
  the ST-106 split are skipped); correctness only.
- Milestone: M6. Tests: LO-03, LO-14, LO-04 (census), ST-141, ST-154, ST-114 (pregen log / census reasons), ST-107,
  ST-20, ST-74, ST-142, LO-12. Point 4 (census shares -> weight-stripping Todo) = hand the shares over, no tuning.
- Server: session 4 (d6feb4e4) scratchpad srv/ (373 mods, patcherMod build/libs 1.0.6 of 12:44, md5 5acd69dc).
  Toml = scratchpad toml_m5_base.toml (instance defaults + earth_fill log ALL, meta log true, placement log true),
  density 1. srv-only kubejs/server_scripts/zz_scratch_tick.js (tick gaps) still in place.
- World st_p, seed 8675309, 4 locator threads. Deviation: area centred on 2144 -2592 (92% land) instead of spawn
  (76% ocean): Chunky square radius 1536 blocks (96 chunks), census 96 at 2144 -2592.
- Known while running: census loads stall the server thread (Phase 5 LO-05 bug), only slower, not wrong.
- Running: nothing (stopped 17:40, toml diffed = base). Phase 6 tests all run. Next: writeup 06_phase_6.md.
- Census plan change: LocateCensus counts chunks generated before it as "known" only (its javadoc: run on fresh
  terrain), so the census can NOT run over the pregenerated square. It runs on a separate fresh land-rich square of
  st_p after the pregen, picked with the srv-only /zbmap probe (scratchpad zz_scratch_bmap.js, copied into
  srv/kubejs/server_scripts; loads on the next restart; noise biome source only, no chunk loads).
- Tools (scratchpad): nbtlib.py (seabed_heights reader + int arrays), starts.py (all starts + boxes -> json),
  spacing.py (per set: pairs closer than separation+1, shared cells), lo12.py (palettes below y 48 in full chunks).

## Log (append-only)

- 16:07 M6 started; st_p boot, Done 16:09:22 (93.8 s); 16:09:34 Chunky task: square r 1536 at 2144 -2592.

- 16:11:23 dumpstructuregen on st_p (214 sets, 453 structures; the instance's copy predates 7.4).
- 16:12 partial world (2229 full chunks): spacing.py clean; lo12.py: ores in 97% of sections y -64..47, deepslate 73%,
  air 73%, fluids 22%. Rerun at the end.
- 17:13:59 Chunky done: 37249 chunks, 1:04:10. save-all; latest.log kept as scratchpad latest_p_pregen.log;
  starts.py -> starts_p.json: 3357 starts (37778 full chunks + 6624 structure_starts fringe).
- spacing.py over all 30 random-spread sets with starts: 0 pairs closer than separation+1, 0 shared grid cells. Groups:
  settlements 7 starts (min 13), pillager_settlements 31 (9), surface_dungeons 10 (11), witch_huts 6 (9),
  entrance_dungeons 14 (17), vehicles 5 (17), remote 42 (13), way_signs 35 (11), ruins 105 (9), sky 12 (17),
  ocean_ruins 154 (4), boats 3 (29), flotsam 7 (9).
- drops.py: 15 rank drop lines, loser rank < winner rank in 15/15; 24 peaceful drop lines, all hostile losers, radius =
  the peaceful structure's (21 x 3 remote, 3 x 5 settlements; no big-settlement drop in the area). 22 unique drops: the
  loser's start is absent from its chunk and the winner's start is present in 22/22 (ST-142 scratch part).
- lo12.py full area: 37778 full chunks, 264446 sections y -64..47: ores 97%, deepslate 73%, air 76%, fluids 28%; 91
  sections all air = scattered over Y -3..2, 1-3 per chunk in neighbouring chunks = big caves, not a clip pattern.
- ST-74: 7 driftwood flotillas, all placed with 11-16 logs (meta log "N/N"); piece-centre biomes: 4 ocean, 1 mixed
  beach/ocean/orchid swamp (6 ocean, 6 beach-mixed pieces), 2 "plains" (east of the square). forceload both plains ones:
  every piece centre column is water 6+ deep under the log (plains-biome water body), none on land. forceload removed.
- ST-114 block part: 14 entrance dungeons (11 spider, 3 zombie); the 11 in full chunks: surface block above the box
  centre is grass/leaves/stone, never water; 3 in fringe chunks not checked.
- ST-107: group/witch_huts 6 starts (3 swamp_hut, 3 YUNG witch_hut), none closer than 9 chunks, no shared cell.
  (ST-106 ratio skipped as a rate; its "no slot holds both" part holds.)
- 17:25 restart (7.5 s) with zz_scratch_bmap.js. /zbmap land shares (r 1536): 2144 1200 25%, 5500 -2592 29%,
  -1400 -2592 58%, 2144 -6200 72%, 5500 1200 25%, -1400 1200 83%, 5500 -6200 81%, -1700 1500 90% -> census there.
- 17:25:59 census 96 at -1700 1500: done 17:36:35, 635 s: 100329/100329 slot chunks, 2366 starts, 1 miss, 0 false
  negatives. Totals over 305 structure rows: slots 179710, setSkipped 111242, predictedMaybe 23, known 142971, built
  2366; predictedNo placement 36051, biome 665. Every group set row "known" (village_plains 16/16, spider_dungeon
  36/36, mineshaft 37249/37249). During the census the log had 79 placement drops + 48 earth-fill CANCELLED, none in
  the report (only 1 miss, reason no_start). Saved: census_p.json.
- 17:37 small check `celocate census 4 -9000 9000` (untouched terrain): 218 slots, 1 s; known 351 of 432 member-slots;
  the only predictions: buried_treasure 81 slots (80 placement-no, 1 known) + 1 ancient_city. -> CENSUS BUG: enumerate
  fills each region's list set by set; the first every-chunk set (buried_treasures) predicts then LOADS every chunk,
  so every later set at that chunk finds it loaded (StructureCheck loadedChunks) = "known". Prediction, misses, miss
  reasons and false negatives only cover the first set per chunk. "built" and set shares come from the real starts,
  so they are fine. Fix idea: group a region's slots by chunk, predict every set's members at that chunk, then load
  once and score them all. Older code (locate 3.6, 10-12); recorded, not fixed.
- 17:40 stop; console_p2.log, latest_p_census.log kept.

## Results

| Test | Result | Evidence |
|---|---|---|
| ST-141 | PASSED | 15 rank drops, loser rank below winner in 15/15 |
| ST-154 | PASSED | 24 peaceful drops, all hostile losers, radius = the peaceful structure's (3 or 5) |
| ST-142 | PASSED (scratch part) | 22 unique drops: loser start absent, winner start present; looking at them stays [game] |
| ST-114 | PARTLY | 11 entrance dungeons in full chunks: dry surface above every box centre; the "surface check dropped" count not taken (DEBUG log, and the census reasons are broken) |
| ST-107 | PASSED | 6 witch-group starts, none closer than 9 chunks, no shared cell |
| ST-106 | SKIPPED (rate) | ratio is a rate; its "no slot holds both" part holds (no shared cells) |
| ST-20 | PASSED | Region files: settlements 7 starts >= 13 chunks, ruins 105 >= 9, surface dungeons 10 >= 11; all 30 sets clean |
| ST-74 | PASSED | 7 flotillas, 11-16 logs each; every piece over water 6+ deep, none on a beach |
| LO-12 | PASSED | 37778 full chunks: ores 97%, deepslate 73%, caves 76% of sections below y 48 |
| LO-03 | FAILED (census bug) | 0 false negatives reported, but predictions only cover the first set per chunk |
| LO-14 | FAILED (census bug) | 1 miss in the report while the log shows 127 generation rejects in the square |
| LO-04 | PARTLY | Per-set shares written (built counts are real); comparison with the estimates skipped (rate) |
| ST-72, 91, 98, 101 | SKIPPED (rate) | user: no structure-to-structure rates |

## Open issues

- CENSUS BUG (LO-03/04/14): see 17:37 log. Slots processed set by set; the first set that tries a chunk loads it and
  every later set there is counted "known". Fix: per chunk, predict all members of all sets first, then load once.

## Census per-set shares (census_p.json, radius 96 at -1700 1500; built / slots, valid despite the census bug)

- group/boats: 16 slots, 0 skipped by the set: illagerwarship:warship 0.0625
- group/entrance_dungeons: 36 slots, 0 skipped by the set: betterdungeons:spider_dungeon 0.1944, betterdungeons:zombie_dungeon 0.0278
- group/ocean_ruins: 576 slots, 0 skipped by the set: hopo:underwater_warm_small_ruins 0.0226, revamped_shipwrecks:shipwreck_beached 0.0208, revamped_shipwrecks:shipwreck 0.0087, creativeengineering:meta/shipwreck_debris 0.0035, hopo:underwater_fossils 0.0035, hopo:underwater_small_ruins 0.0035, minecraft:ocean_ruin_warm 0.0017, philipsruins:ocean_ruins 0.0017, towns_and_towers:wreckage_ocean 0.0017
- group/pillager_settlements: 35 slots, 3 skipped by the set: creativeengineering:meta/pillager_stronghold_siege 0.1714, towns_and_towers:pillager_outpost_savanna 0.0857, towns_and_towers:pillager_outpost_sparse_jungle 0.0857, creativeengineering:meta/pillager_tower_siege 0.0571, towns_and_towers:exclusives/pillager_outpost_mediterranean 0.0571, creativeengineering:meta/pillager_camp_ruin 0.0286, creativeengineering:meta/pillager_camp_ruins 0.0286, mysticrift_pillagers_towers_aris:acacia_pillager_tower 0.0286, mysticrift_pillagers_towers_aris:jungle_pillager_tower 0.0286, mysticrift_pillagers_towers_aris:oak_wood_pillager_tower 0.0286, takesapillage:pillager_camp 0.0286, towns_and_towers:pillager_outpost_forest 0.0286
- group/remote: 64 slots, 0 skipped by the set: mini_clock_tower:mini_clock_tower 0.0469, farmers_structures:aquacultures_common 0.0313, farmers_structures:cooks 0.0313, greek_villager_house:greek_house 0.0313, llama_stable:llama_stable 0.0313, ancient_enchantment_temple:temple 0.0156, creativeengineering:meta/house_camp 0.0156, farmers_structures:aquacultures_carp 0.0156, gothic_church:church 0.0156, hobbit_house_:hobbit_house 0.0156, small_villager_greenhouse:small_villager_greenhouse 0.0156, villager_country_house:country_house 0.0156, villager_fish_shop:fish_shop 0.0156, watchtower_building:fresh_watchtower 0.0156
- group/ruins: 140 slots, 0 skipped by the set: philipsruins:field_stone_ruins_rocks 0.1214, philipsruins:ancient_towers 0.1071, philipsruins:ancient_ruins 0.1000, creativeengineering:meta/ruin_cluster 0.0929, creativeengineering:meta/ruin_scatter 0.0786, small_cat_statues:cat_2 0.0429, philipsruins:field_stone_ruins 0.0214, forest_ruins:ruin_3 0.0143, small_cat_statues:cat_1 0.0143, small_cat_statues:cat_3 0.0143, forest_ruins:ruin_5 0.0071, oldvillagerstatue:old_villager_statue 0.0071, philipsruins:badlands_structures 0.0071, philipsruins:level_three_ruins 0.0071, philipsruins:level_two_ruins 0.0071, watchtower_building:abandoned_watchtower 0.0071
- group/settlements: 16 slots, 0 skipped by the set: minecraft:village_savanna 0.1875, towns_and_towers:village_jungle 0.1875, creativeengineering:meta/village_towers 0.1250, creativeengineering:meta/town_villages 0.0625, minecraft:village_plains 0.0625, towns_and_towers:exclusives/village_mediterranean 0.0625, towns_and_towers:village_meadow 0.0625, towns_and_towers:village_sparse_jungle 0.0625
- group/sky: 9 slots, 0 skipped by the set: skyvillages:skyvillage 0.5556, cloudstorage:sky_temple 0.3333, ae2:meteorite 0.1111
- group/surface_dungeons: 148 slots, 0 skipped by the set: betterjungletemples:jungle_temple 0.1014, minecraft:jungle_pyramid 0.0203, philipsruins:badlands_start_dungeon 0.0068
- group/vehicles: 16 slots, 1 skipped by the set: illager_siege_weapons:siege_tower 0.0625, illager_siege_weapons:trebuchet 0.0625
- group/way_signs: 106 slots, 13 skipped by the set: supplementaries:way_sign 0.1981
- group/witch_huts: 145 slots, 0 skipped by the set: betterwitchhuts:witch_hut 0.0069
