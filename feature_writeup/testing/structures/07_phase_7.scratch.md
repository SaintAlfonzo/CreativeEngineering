# M7 scratchpad: Phase 7 (8.0.7, calibration runs)

## State (keep current)

- Constraints (user 2026-10-14): ignore Nether/End; ignore structure-to-structure rates; correctness only.
- Milestone: M7. Tests: ST-52 (+159, 160, 162, 167, 168 from the same lines), ST-169, ST-57, ST-67, ST-53, ST-58,
  ST-54, ST-59. ST-52 and ST-57 are data for the user (numbers per size / limits), not pass/fail.
- **Session 5 (2026-10-19, d2be77ec) restarts M7 from scratch.** The 10-14 attempt (session 4) died right after its
  Chunky start; its server and world st_c are gone, no results were taken.
- Server: session 5 scratchpad srv/. Libraries + run.sh from ~/.local/share/atlauncher/servers/Minecraft1192withForge
  (Forge 1.19.2-43.3.9; the Trash CreEngServer copy is gone). 374 server mods symlinked from mods/ by mod_sides.md
  (45 client skipped); srv/vinery_upstream -> instance vinery_upstream (the Vinery port reads it; boot fails without).
  Jar: **patcherMod 1.0.7** (build/libs, md5 c0bdc0e1, 10-18 16:27), NOT the 1.0.8 in mods/: **1.0.8 crashes a
  dedicated server at mod loading** (Open issues 1). The worldgen package is byte-identical in 1.0.7 and 1.0.8 (jar
  diff: only CEConfig, time/, advancements, oven, IECompat, RemainderBuffer, SeasonHud mixin), so Phase 7 results hold
  for 1.0.8. Over 1.0.6 (Phases 1-6): earth_fill/stone_base, census per-chunk fix, LocateLoads, GroundMatch, PoolTuner.
- Phase 7 base toml = scratchpad toml_m7_base.toml = instance toml + structureDensity 1.0 -> 4.0, earth_fill log
  CANCELLED -> ALL, water_fill log CANCELLED -> ALL, meta_structures log false -> true, placement_priority log
  false -> true. Restore = scratchpad toml_instance.toml (the instance's own copy is never edited).
- server.properties: seed 8675309, max-tick-time -1, port 25599, offline. asyncLocatorThreads 4 in st_c/serverconfig.
- World st_c. Main square Chunky r 800 at -1400 -2592 (68% land on 10-14). Switch squares (r 400, disjoint, land
  shares from 10-14): 200 -2592 (66%), -1400 -4200 (84%), -1400 -1000 (36%, coast), -3000 -2592 (29%).
- Running: nothing. M7 tests all run 04:20; next: writeup 07_phase_7.md. Worlds st_c (base, tag empty), st_c2 (base +
  tag patch), st_c3 (boots 2-4) in srv/. Digest: scratchpad ef.py <log> [from] [to].

## Plan for the boots

1. Base boot (toml_m7_base): main square -> ST-52, 159, 160, 162, 167, 168, 57, 59 data from latest.log; offline
   region-file probes of cancelled/pruned spots.
2. prune = false (+ templateTrim off if no village is trimmed, for ST-67): square 200 -2592 -> ST-169, ST-67.
3. Earth fill at zero + ST-54 overrides: sizes all [0,0], maxOverWater 0, beardReach 0, overrides plains -1,-1,-1 and
   ancient_ruins 0,0,0: square -1400 -4200 -> ST-53, ST-54.
4. Water fill at zero: maxIntoGround 0, maxIntoAir 0, overrides []: coast square -1400 -1000 -> ST-58.
1b. (added 02:30) Prune diagnostic: base + pruneMaxHanging 0, pruneMaxMound 0, square 200 -2592 r 400: any village
   piece with any hanging earth or mound column must prune. Lines -> prune works, per-piece limits are just loose;
   none -> prune bug.

## Log (append-only)

- 2026-10-14 17:43 (session 4) M7 started; st_c boot, Chunky main square started 17:48; session lost after that.
- 2026-10-19 session 5: server rebuilt as in State; toml written; st_c deleted (new world).

- 01:39 first boot (1.0.8 jar) died in FML: (a) vinery missing vinery_upstream/ (setup gap, linked); (b) Serene
  Seasons failed: MixinTransformerError, ClassMetadataNotFoundException net.minecraft.client.Minecraft while applying
  patcherMod's SereneSeasonsClockMixin (common mixin on sereneseasons.handler.season.SeasonHandler; its onClientTick
  inject uses Minecraft.getInstance()/mc.levelRenderer directly; Mixin's preprocessor resolves every field/method
  owner, and client classes don't exist on a dedicated server). The crashed JVM stays alive after FML crash: kill it.
- 01:41 swapped to the 1.0.7 jar; 01:48:17 Done (303.6 s). 01:48:42 Chunky square r 800 at -1400 -2592 started.
- Spawn chunks (01:43-01:48, not the square): 126 earth lines; circus tent at [-7, -9] = 520 earth (medium, beard),
  63 over air / 30 mound -> CANCELLED (trim on). bighorsestable 446 earth medium cancelled on mound 88.
- Early Chunky rate 1.4 cps (ETA 2 h); watch it.
- 02:18:08 Chunky done: 10201 chunks, 29:26 (8-9 cps after warm-up). save-all; log kept as scratchpad latest_c_main.log;
  digest main_digest.txt / main.json (ef.py 01:48:42..02:18:08); starts_c.json (starts.py, 4363 starts, region files).
- Main square digest (unique id+chunk; a chunk is judged 2x: the placement-priority neighbour pass rebuilds starts, so
  every line appears twice, minutes apart, identical numbers):
  small 1839 starts / 160 cancelled (8%): earth med 64 p90 100 max 149; water nonzero 106 (max 96); air nonzero 90
    (max 94); mound p90 2 max 94. Reasons: water+mound 96, air+mound 31, mound 30, water 2, air 1.
  medium 290 / 22 (7%): earth med 186 p90 302 max 584; air p90 21 max 174; mound p90 7 max 170. Reasons: air+mound 13,
    mound 6, water 1, air 1, water+mound 1.
  large 14 / 5 (35%): earth med 790 p90 1028 max 1907; air med 2 p90 152 max 442; mound p90 174. Reasons mixed.
  huge 1 / 1: eugene:ruined_drum_tower 5570 earth (stone base now counts), 1366 air, 239 mound -> CANCELLED.
  Beard kinds: small bury 807 (all philipsruins:ancient_dungeon), small beard 1027, none 9, medium beard 290.
  Dominated by underground starts: philipsruins:ancient_dungeon 807 (35 cancelled), subterrestrial cabins
  (ocean 181/95, jungle 157/11, deepslate 155/0, default 9/0), terralith underground witch hut/oak cabin/sunken
  tower (157/0, 142/0, 178/10).
- 0 `pruned` lines in the whole run (prune = true). Plain village starts judged ~17 (vanilla savanna/taiga, T&T
  forest/jungle/sparse jungle/birch/meadow/beach/mediterranean/OGT), cancelled: minecraft:village_taiga [-81, -181]
  large 790 earth, 99 air, 71 mound (mound 71 > 64) -> CANCELLED with no prune line; the set then built
  towns_and_towers:village_old_growth_taiga at the same chunk (kept). The other village cancels are meta parts
  (village_towers anchors; metas are not pruned by design). type/village holds minecraft:village_* etc. and
  earth_fill/prune = #type/village, so the tag is right -> diagnostic boot with pruneMax 0/0 next.
- ST-160 sizes: small ruins (forest_ruins, philipsruins ancient_ruins/towers) log small 24/8; vanilla/T&T villages log
  medium or large mostly, BUT small ones exist: village_taiga 128 earth small, T&T village_beach 126 small,
  exclusives/village_mediterranean 42/74 small. No Terralith fortified village in the square.
- ST-159 probes (region heightmaps, start chunk +-1 chunk): T&T village_beach [-104, -157] (96 water, 77 mound,
  CANCELLED): sea floor y 41-51 under up to 21 blocks of water, every sampled column > 6 deep. farmers_structures:cooks
  [-79, -140] (135 air, 125 mound): ground y 54-120 around the chunk = cliff. subterrestrial:ocean_cabin cancels (65 in
  the square, all "72 earth, 72 over water, 44 mound", 5-chunk grid): sea floor y -13..39 under 55-75 blocks of
  water, so the cabin's fixed height is open sea water; the 62 kept ones sit under shallow floors (floor 54-56, box
  y 23-43, 42 fully under the floor). Correct cancels.
- ST-162: square had no mansion. `locate structure minecraft:mansion` from spawn: 13 misses, timed out 120 s; from the
  dark forest at -1368 -3104: answered 2560 -5504 after 41.8 s; 4 mansion starts built on the way (3 dropped by
  peaceful radius: circus tent, eugene:camp x2). 0 `[earth-fill] minecraft:mansion` lines in the whole log.
- ST-57 water fill (main square): hopo underwater_warm_small_ruins 90/0 (ground 0, air max 1), small_ruins 29/0 (all
  0/0), fossils 34/1 (one 111 into ground -> CANCELLED at [-152, -132]), ruins 11/1 (air p90 14, max 25 -> CANCELLED at
  [-123, -142], 16148 water blocks); T&T ocean outpost 2/0 (0/0); sushis 1/0 (0/0); aquacultures_carp 2/2 (ground
  103/144, air 67/26 > 16); aquacultures_common 1/0 (ground 1).
- ST-59 (locates from -1400 -2592, 02:27): swamp fisherman house answered -3072 192 = chunk [-192, 12]: 268 water,
  268 into dry ground (limit -1), 0 air -> kept; another at [-283, -70] 160 ground, 108 air -> CANCELLED (spill).
  Fish pond aquacultures_common answered -2880 -3376 = [-180, -211]: 1 water block, 1 air -> kept (26 misses, 68 s);
  one at [-129, -238] kept by both fills then dropped by placement priority (overlaps meta/village_towers).
  aquacultures_carp on slopes: 26 and 67 into air -> CANCELLED. The common pond template carries only 1 water block.
- 02:29 boot 1b (prune diagnostic, st_c, pruneMaxHanging 0 / pruneMaxMound 0; the 1.0.7 config spec stripped the
  1.0.8-only [time], [advancements], ovenIngredientCounts from the srv toml, harmless here). 02:31:50 Chunky r 400 at
  200 -2592: 2601 chunks, 11:11. Still 0 pruned lines; plain villages with hanging earth: T&T village_beach [-15, -189]
  2 water, village_sparse_jungle [37, -131] 14 air 2 mound, [-36, -119] 1 mound -> none pruned even at 0/0.
- 02:43 `locate structure #creativeengineering:earth_fill/prune` -> Async Locator "NamedSet(...earth_fill/prune)[[]]":
  the tag is EMPTY at runtime. `#creativeengineering:type/village` also EMPTY; earth_fill/exempt, stone_base,
  water_fill/ponds are filled. ROOT CAUSE: structure_overrides.js ServerEvents.tags only turns the meta/* tags (and
  the fit tags, way-sign destinations) of structure_tags.json into real structure tags; type/* tags exist only inside
  the script. earth_fill.js puts '#creativeengineering:type/village' in earth_fill/prune -> nested reference to a tag
  nobody fills -> empty -> EarthFill.filter never prunes (structure.is(PRUNE) false). Pruning has never run live
  since it was built (10-13, sim-only). Same for the CEConfig overrides example "#creativeengineering:type/village=...".
  The only other "type/" user is MassPlacer (reads the JSON, fine).
- SCRATCH PATCH 07 (srv copy only, instance untouched): structure_overrides.js meta-tag filter also takes
  'type/village' (`|| t === 'type/village'`). Toml back to base (pruneMax 8/4) = scratchpad toml_m7_base_107.toml.
- 02:45 boot 1c: new world st_c2 (same seed, asyncLocatorThreads 4), patch on, base toml. Plan: Chunky the same main
  square r 800 at -1400 -2592. st_c's main run (tag empty) = the exact prune-off baseline for ST-167 / ST-169.
- 02:52:23 st_c2 Done (302 s). `locate #creativeengineering:earth_fill/prune` now lists 37 structures (patch works).
  02:52:37 Chunky main square r 800 at -1400 -2592; finished 03:21:56 (10201 chunks). Log latest_c2_main.log, digest
  main2_digest.txt / main2.json; compare cmp_prune.py main.json main2.json.
- Prune off (st_c, tag empty) vs on (st_c2, patched), same seed + square: village starts judged 27 -> 25, cancelled
  4 -> 2; 4 pruned lines, all villages (no non-village). Per start:
  minecraft:village_taiga [-81, -181]: off CANCELLED large 790 e / 99 air / 71 mound -> on pruned 5 of 124
    (taiga_tool_smith_1 10 air 6 mound, taiga_shepherds_house_1 78 air 58 mound, taiga_large_farm_1 11 air 7 mound),
    then kept large 611 e / 0 / 0. (Off: the set fell through to T&T village_old_growth_taiga there; on: not needed.)
  minecraft:village_savanna [-142, -130] (village_towers anchor; meta cancelled -> plain anchor): off CANCELLED large
    877 e / 243 water / 174 mound -> on pruned 8 of 171 (savanna_small_house_3 38 w, balloon_stand_savanna 27 w,
    savanna_small_house_4 58 w, incubation savanna_chicken_coop_1 44 w, ...), kept large 626 e / 0 / 0.
  minecraft:village_savanna [-165, -68]: off CANCELLED large 695 / 152 air / 138 mound -> on pruned 1 of 163
    (domesticationinnovation:savanna_petshop 102 air 98 mound) -> 583 e = MEDIUM now, 50 air / 40 mound vs medium
    limits 64 / 24 -> CANCELLED on mound. Under the large limits (160 / 64) it would have been kept: pruning shrinks
    the earth count, so the size class (and its limits) can drop. Finding for the user.
  T&T village_beach [-104, -157]: off CANCELLED small 126 / 96 w / 77 m -> on pruned 2 of 12 (beach_main_house_1 31 w
    31 m) -> 95 / 65 w / 46 m, still CANCELLED (the rest of the overhang is in the root piece). Correct.
  Other sizes unchanged (small 1833 / 160, medium 276 / 22 vs 290 / 22 off: the medium count dropped because the
  fall-through OGT village and meta re-tries no longer happen).
- 03:22 ST-168 locates: `village_taiga` from -1290 -2890 -> -1296 -2896 = chunk [-81, -181]; `village_savanna` from
  -2265 -2075 -> -2272 -2080 = [-142, -130]. Both pruned villages answer at their own chunk.
- ST-167 region probe (st_c2): taiga 119 pieces (= 124 - 5), savanna 163 (= 171 - 8); 0 junctions of kept pieces
  point at a missing piece (the streets end there); no rigid piece has >= 25% of its columns more than 6 above the
  floor. Entities (taiga; savanna area has no entity chunks on disk): 7 MCA villagers + iron golem + cats all inside
  kept pieces; the animals outside the boxes (chickens by a pen, rabbits, a blue jay) stand on the ground; nothing
  floats. Dropped templates partly still listed = other copies of the same template elsewhere in the village.
- 03:24 boot 2: new world st_c3, prune = false, [worldgen.systems] templateTrim = false, earth_fill overrides =
  ["philipsruins:ancient_dungeon=5,7", "terralith:underground/witch_hut=5,7,3"] (ST-160 2- vs 3-number), patch on.
  Plan: Chunky r 160 around the taiga (-1296 -2896), the savanna (-2272 -2080) and the circus tent [-144, -228]
  (-2296 -3640; trim on: 520 earth, kept).
- 03:31:15 st_c3 Done (311.7 s). Chunky r 160 at -1296 -2896 (03:31-03:34), -2272 -2080 (-03:38), -2296 -3640
  (03:44-03:47; my wait loop first sent its start while square 2 still ran, redone). Log latest_c3.log, c3.json.
- ST-169: 0 pruned lines; minecraft:village_taiga [-81, -181] CANCELLED large 790 / 99 air / 71 mound (as st_c), the
  set fell through to T&T village_old_growth_taiga (kept); minecraft:village_savanna [-142, -130] CANCELLED large
  877 / 243 water / 174 mound (as st_c). Both stood (pruned) in st_c2 with prune on. PASSED.
- ST-67: villager_circus_tent [-144, -228] trim off: 784 earth (large, 1 air, kept); trim on (st_c, st_c2): 520
  (medium). 784 - 520 = 264 = the trimmed pad. PASSED. (Trim off moves the tent from medium to large.)
- ST-160 overrides: philipsruins:ancient_dungeon "=5,7" -> 285 lines all (small, 5, 7, 8): mound limit stays the
  size's 8; terralith:underground/witch_hut "=5,7,3" -> 34 lines all (5, 7, 3). PASSED (override part).
- 03:49 boot 3 (st_c3): earth fill at zero: sizes all [0, 0], maxOverWater 0, beardReach 0, overrides
  ["minecraft:village_plains=-1,-1,-1", "minecraft:village_savanna=-1,-1,-1", "philipsruins:ancient_ruins=0,0,0"]
  (savanna added as a second -1 case: plains may not be in the square); prune true, trim true. Square: Chunky r 400
  at -1400 -2592 (inner main square; its taiga-village corner was generated in boot 2 and is skipped). Compare per
  start with st_c2 (same seed, default limits).
- 03:51:03 boot 3 Done (9.4 s); 03:51:17 Chunky r 400 at -1400 -2592 -> 04:00:02 (2601 chunks). latest_c3_zero.log,
  zero.json; cmp_zero.py main2.json zero.json srv/st_c3.
- ST-53/54 (zero vs st_c2 defaults): 560 lines, 384 kept / 176 CANCELLED; limits (0,0,0) on 555, (-1,-1,-1) on 5.
  Kept with any water/air/mound: none. Cancelled with none: none. 85 starts kept at default -> CANCELLED at zero
  (underground oak cabins 7, ancient towers 6, village_taiga 5, T&T sparse jungle villages 4, jungle cabins 4, sunken
  towers 4, ...). village_plains: 0 lines (no plains in the square); village_savanna (-1,-1,-1): 5 lines, all with
  gaps, 0 cancelled. philipsruins:ancient_ruins (0,0,0): cancelled at 3 and 7 air, the 8 with 0 kept. No line for any
  earth_fill/exempt id (Hopo ruins in the square logged water-fill lines only). 22 pruned lines (villages, at 8/4).
- Cancelled starts in full chunks: 57 absent; 6 present = all built in boot 2 or its fringe (default limits), only
  re-judged in boot 3 by the placement-priority neighbour pass. "Slot stays empty" does NOT hold for multi-member
  sets: the set tries its next member (vanilla's loop), e.g. subterrestrial:jungle_cabin -> deepslate_cabin (3),
  village_taiga / terralith:fortified_village / T&T exclusives/village_swedish -> T&T village_old_growth_taiga,
  cartographer house / forest watchtower / eugene:camp / cleric house / tannery / windmill -> fresh_watchtower; the
  replacement is judged by earth fill too (it passed at zero, so it had no gap). Test text, not a bug.
- 04:01 boot 4 (st_c3): water fill at zero: maxIntoGround 0, maxIntoAir 0, overrides [] (earth fill back to base).
  Coast square Chunky r 400 at -1400 -1000 + /locate of the fisherman house and the fish ponds.
- 04:03 boot 4 Done; Chunky r 400 at -1400 -1000 04:03:35 -> 04:14:13 (2601 chunks). The fisherman / pond locates sent
  with the pregen running timed out at 120 s after 1 load each (loads slow under Chunky). latest_c3_water0.log,
  water0.json.
- ST-58: 166 water lines, limits (0, 0) on all; 162 kept, every kept one 0 into ground / 0 into air; 4 CANCELLED:
  hopo:underwater_warm_small_ruins [-122, -63] 115 into ground + 1 air, [-55, -31] 5 air, hopo:underwater_fossils
  [-55, -31] 4 air, swamp fisherman house [-192, 12] 268 into ground (the same start was kept in boot 1 under the
  ponds override). All 4 absent from their chunks (status structure_starts, fringe; [-55, -31] holds a shipwreck,
  [-192, 12] two subterrestrial cabins). Shore probe not possible (fringe chunks, no heightmaps); "into dry ground" =
  reaching onto land by definition. 04:15 `locate aquacultures_common` from -2880 -3376 (no Chunky): 41 misses, not
  found in 120 s; every pond start on the way cancelled: [-180, -211] (stood in boot 1) 1 into air, [-153, -252] 1
  into ground, [-118, -275] 1 into air, aquacultures_carp [-118, -275] 131 ground / 39 air. PASSED.
- ST-57 data (default limits, main square; the coast run's counts are limit-independent): Hopo warm small ruins 90 +
  151 lines, small ruins 29, ruins 11 + 6, fossils 34 + 8: 0 into ground and 0 into air in 97% of lines; nonzero:
  fossils 111 and 115 into ground (2 lines), ruins 14 and 25 into air, warm small 1-5 into air (3), fossils 4 air.
  T&T ocean outpost 2/0, sushis 1/0 (0/0). Ponds: common pond carries 1 water block (0-1 ground/air); carp pond 170
  water, 103-144 into ground, 26-67 into air on slopes; fisherman house 268, all into ground on flat swamp, 108 air on
  a slope.
- 04:20 stopped; instance config/kubejs untouched (the patch and all toml changes were in srv/ only).
- Side finding: /dumpstructuregen is broken: every set has _errors "Cannot find function locateOffset /
  frequencyReductionMethod / frequency / salt / spacing ..." and empty structure lists (214 with field errors). The
  instance's own kubejs/exported/worldgen/structuregen.json of 10-18 18:31 (user's client) is the same; the 10-14
  Phase 6 dump worked. dump_structuregen.js unchanged since 09-27 -> something else changed (mod/jar). Not chased.

## Results

| Test | Result | Evidence |
|---|---|---|
| ST-162 | PASSED | 4 mansion starts built by a /locate (3 dropped by peaceful radius), 0 earth-fill lines |
| ST-159 | PASSED | Cancels probed: village_beach over 21-deep sea, cooks on a 66-block cliff, ocean cabins in open sea |
| ST-59 | PASSED | Pond and swamp house kept (ground uncapped) and found by /locate; slope ones cancel on air 26-108 |
| ST-167 | FAILED as shipped / PASSED with tag fix | Tag empty, 0 prunes; patched: cancels 4 -> 2, probes clean |
| ST-168 | PASSED (with tag fix) | 4 pruned lines, all villages; both pruned villages /locate at their chunk |
| ST-169 | PASSED | prune = false: 0 pruned lines, the same two villages CANCELLED |
| ST-67 | PASSED | Circus tent 784 earth trim off vs 520 on = 264 |
| ST-53 | PASSED (one clause wrong) | At 0: every start with any gap cancels, none without; group slots fall through to a member |
| ST-54 | PASSED | -1,-1,-1 (savanna; no plains here) never cancels; 0,0,0 ruins cancel at 3 and 7 air; exempt never logs |
| ST-58 | PASSED | At 0/0 with no overrides: 4 cancels (shore ruins, swamp house), every pond on a 41-miss search cancels |
| ST-57 | DATA | 97% of Hopo lines 0/0; outliers 111-115 ground, 14-25 air; limits 32/16 cancel 2 of 165 |
| ST-52 | DATA | Per size in the writeup; defaults cancel 8% small, 7% medium, 8% large (prune on) |
| ST-160 | PARTLY | Overrides PASSED (5,7 keeps mound 8; 5,7,3 sets 3); sizes: some villages log small |

## Open issues

2. **type/village is not a real tag -> pruning never runs** (02:43): see Log. Fix options: (a) structure_overrides.js
   adds type/* tags (or just the ones other scripts name) as real tags like meta/*; (b) earth_fill.js lists the
   village ids itself. (a) also makes "#creativeengineering:type/village=..." overrides work. Tested on the scratch
   copy with (a) limited to type/village.
3. **/dumpstructuregen broken** (every set: placement methods not found, structure lists empty; also in the
   instance's 10-18 client dump). Not chased.

0. (Results so far) ST-162 PASSED, ST-59 PASSED, ST-159 PASSED, ST-160 PARTLY (small villages exist; overrides part
   in boot 2), ST-57 data taken, ST-52 data taken, ST-167/168 pending the prune diagnostic.

1. **patcherMod 1.0.8 cannot boot a dedicated server** (found 01:39): SereneSeasonsClockMixin is in the common mixin
   list and references net.minecraft.client.Minecraft inside creativeengineering$sunClient. Fix idea: move the
   client body into a client-only helper (e.g. time/TimeClient static method, returns the new sub-season or null) so
   the mixin method only references patcherMod classes; or split it into a client mixin on the same target. 1.0.8 is
   STAGED (plan_1.0.8.md says nothing has booted on the dedicated server) -> tell the user before they deploy. Not
   fixed (time master, not structure work).
