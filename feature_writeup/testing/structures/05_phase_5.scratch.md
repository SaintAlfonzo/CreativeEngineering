# M5 scratchpad: Phase 5 (8.0.5, batch searches)

## State (keep current)

- Constraints (user 2026-10-14): ignore Nether/End; ignore structure-to-structure rates; correctness only. Sleep an
  hour if the token limit gets close.
- Milestone: M5. Tests: LO-02 (world L1, folds on, batch all from spawn first thing), LO-01 (world L2, folds off,
  same batch; compare), LO-06, LO-15, LO-05 (world L1 again).
- Server: SESSION 3 (c0d10b34) scratchpad srv/ (rebuilt 13:05: the 54725a7d scratchpad was empty, no server was
  running; 373 mods, patcherMod = build/libs creativeengineering-1.0.6.jar, md5 5acd69dc = the one in mods/, 12:44).
  toml = scratchpad toml_m5_base.toml: instance toml + earth_fill log ALL, meta log true, placement log true; density
  1. asyncLocatorThreads 4 in st_l1 + st_l2 serverconfig. Helper scratchpad/cmd.sh "<cmd>".
- Worlds: st_l1 (folds on), st_l2 (foldPlacement + foldBiomes false).
- Running: st_l2 server, srv toml folds OFF + searchTimeoutSeconds 900 (base = toml_m5_base.toml), 18 ground-truth /locate searches since 14:14:47. Next: compare with L1 positions; reboot st_l1 (base toml + searchTimeout 900) for the 4 L2-only + LO-06/15/05.

## Log (append-only)

- 12:45 M5 started.
- 13:05 SESSION 3: user "continue with the structure tuning plan; if there is a running server, kill it and start
  again". No java was running and the 54725a7d scratchpad was empty, so srv/ rebuilt in c0d10b34 scratchpad (see
  state). Phase 5 restarted from step 1 on a NEW st_l1 (the 54725a7d st_l1 is gone).
- 13:09:40 st_l1 boot Done (97.7 s). Load line: 15 groups, 178 sets (143 emptied), 199 original; 112 freqs < 1;
  1 biome rule; 30 fits / 181 structures; 22 metas; 3 replacements; 242 ranked, 1 companion. Placement: 104 peaceful
  in 2 sets keep 70 hostile away (8 chunks).
- 13:09:53 LO-02: `celocate batch all` from console (spawn -80 76): "258 structures from -80 76, radius 100 chunks,
  4 locator thread(s)". Overworld only (console level).
- 13:35:20 L1 batch done: 233/258 found, 2003 misses total, 1526 s (saved as scratchpad batch_l1.json). 21 timed_out
  (120 s): old_illager_statue 92 misses, simply_houses taiga_forge 58 / plains_house 57 / plains_mansion 52, birch
  pillager tower 44, T&T outpost_swamp 44, meta/waysign_camps 34, outpost_snowy_beach 34, butcher_campsite 31, bastille
  24, sushis 22, cherry tower 21, glacial_hut 21, wandering_trader_camp 20, armorer_campsite 18, animal_barn 17,
  fisher_campsite 17, aquacultures_beach 14, outpost_classic 14, snowy_tent 13, farmer_campsite 10. 4 not_found:
  mineshaft_mesa (1 miss, 9.6 s), skeleton_dungeon, bone_dungeon, lost_soul_city (0 misses: the known land/any
  no-cave-biome bug from Phase 2). savedByStage totals: biome 156421, frequency 57478, placement 40670, meta 31618,
  meta_target 8156, point 1099.
- 13:35:33 stop (clean, no AL search running). srv toml: [worldgen.locate] foldPlacement + foldBiomes true -> false
  (lines 1515/1518; base copy = scratchpad toml_m5_base.toml). level-name st_l2, boot.
- 13:39:17 st_l2 Done (96.3 s). 13:39:30 LO-01 batch: 258 structures from -80 76, radius 100, 4 threads. A second
  `celocate batch all` at 13:39:34 refused: "A batch is already running" (good).
- 14:13:08 L2 batch done: 216/258 found, 10660 misses, 2018 s (scratchpad batch_l2.json); 42 timed_out, 0 not_found
  (the 4 L1 not_found are timed_out here). savedByStage L2: placement 0, biome 0 (folds off, as expected), meta 18944,
  frequency 5119, meta_target 1438, point 659.
  Compare (scratchpad compare): 212 found in both: 210 same position, 2 differ: village_desert L1 -4538 -445 / L2 -4533
  -446; village_savanna L1 -1292 229 / L2 -1280 236. Both are anchors of the same meta/village_towers in the same chunk
  ([-284,-29], [-81,15]) in both worlds, but the META LAYOUT DIFFERS between the worlds: L1 desert anchor -4536 -442 +
  towers -4588 -452, -4580 -500; L2 anchor -4533 -446 + towers -4588 -452, -4500 -500. Within one world it repeats
  exactly (L1 13:10 and 13:17 lines identical; L2 13:39 / 13:46 identical). MetaStructure seeds its own random
  (setLargeFeatureWithSalt), so the anchor's own jigsaw assembly (village box) differs between JVM runs -> see Open
  issues. Not a fold problem: same structure, same chunk.
  Found only in L1 (folds on), L2 timed out: 21 (drum tower, spire, sculk_dungeon, start_nether_ruin, T&T forest/
  mushroom/classic/ocean villages, village_snowy, outposts mushroom/snowy_plains, witch_circle, pillager_outpost,
  fisherman_house, desert pillager tower, fortified_desert_village, underground_bunker (L2 1496 misses), desert_temple,
  ice_cabin (935), taiga_cabin (865), frosted_dungeon). Found only in L2: 4 (meta/waysign_camps 1312 3952, bastille
  -6469 -2923, glacial_hut -7941 -2149, snowy_tent 4736 3168). L1 misses <= L2 for 188 of 212.
- 14:14 ground truth for the 21: srv toml searchTimeoutSeconds 120 -> 900 (live edit, one write) on st_l2; 18 plain
  `/locate structure` sent 14:14:47 (skipped the 3 with 865-1496 folds-off misses: bunker, ice/taiga cabin).

## Results

| Test | Result | Evidence |
|---|---|---|

## Open issues
- Jigsaw assembly differs between JVM runs with the same seed (Phase 5 L1 vs L2): meta/village_towers anchors
  village_desert/savanna got different boxes (5-12 blocks) and a different tower spot (80 blocks). Within one run
  stable. Not checked yet: whether plain (non-meta) jigsaw starts differ too (their locate position is the chunk, so
  the batch can't show it); likely cause = a pool's element order coming from a hash-ordered collection somewhere
  (PoolTuner itself keeps order). Matters for Phase 8 two-world comparisons and for /locate of meta parts after a
  reboot. Older system: recorded, not fixed.
