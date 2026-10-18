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
- SESSION 4 (d6feb4e4): at 14:42 every session scratchpad was empty and no java ran, so the 14:14 ground-truth
  searches, batch_l1.json and batch_l2.json are LOST (the log below keeps their summaries). srv/ rebuilt in the
  d6feb4e4 scratchpad (373 mods, same jar md5 5acd69dc), toml: earth_fill log ALL, meta log true, placement log true,
  rest default (the [worldgen.locate] section appears on first boot). World st_l2 booted 14:47 (95.9 s), same load line.
- Running: nothing (stopped 16:04). srv toml = base again (diffed). Scratch-only zz_scratch_tick.js still in srv/kubejs
  (srv copy only). Phase 5 tests ALL RUN. Next: writeup 05_phase_5.md + inline results, then M6 (Phase 6).

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

- 14:47:27 (session 4, st_l2, folds ON, timeout 120) 21 /locate for the L1-only list: 15 hit, 5 timed out at 120 s
  (drum tower 36 misses, spire 30, T&T village_forest 13, village_ocean 13, outpost_mushroom_fields 2 misses but 3636
  candidates). 14:53:47 timeout -> 900 (one write), the 5 again: all hit (drum tower now 16 misses: chunks checked in
  the first run are no longer counted as misses). Folds-on positions (blocks): frosted_dungeon 896 -1552, taiga_cabin
  1904 1008, start_nether_ruin 1040 3184, drum tower 1552 3376, pillager_outpost -1408 -3920, BDT desert_temple -4336
  -704, sculk_dungeon -2144 -3920, witch_circle -4608 -1168, desert_pillager_tower 4183 -3033, ice_cabin 4608 3552,
  T&T village_ocean -4496 -4368, fortified_desert_village 3376 -5632, outpost_snowy_plains 5632 3712, village_snowy
  6192 3456, fisherman_house 848 7328, spire -7632 -720, T&T village_forest -1168 7984, village_classic -2112 25424,
  outpost_mushroom_fields 21280 22864, village_mushroom_fields 21156 -24550, underground_bunker -27552 -32784.
- 14:51:38 / 14:52:16 NEW LEAD (not ours): "Element Reference{processor_list minecraft:empty} is not valid in current
  registry set" at chunk save (9x), then on reload of those structure_starts chunks (LocateLoads) "Invalid pool
  element found" (5x): the piece is dropped from the start. save-all + scratchpad badel.py over st_l2 regions:
  village_desert 3 of 241 jigsaw pieces saved as {}, village_snowy 2 of 90, meta/village_towers 6 of 976 (its village
  anchors). Cause: Cloud Storage 1.3.4 and Domestication Innovation 1.6.1 (same author) add houses to all 5 vanilla
  village house pools with the static ProcessorLists holder (builtin registry), which the world's registry codec
  refuses. So a village whose start chunk unloads before it is placed loses its balloon/pet-shop pieces (normal play:
  locate, maps, fast travel past the edge of generation). The old "minecraft:empty is not valid" log noise IS this.
- 14:55:56 folds OFF (one write, both keys) + timeout 900: 21 ground-truth /locate, nearest first.
- 14:55:56..14:56:15 folds-off ground truth: all 21 answered in 24 s, SAME position as folds on for 21/21. Few
  misses because StructureCheck reuses the per-chunk answers of the folds-on searches (same world); what is left to
  load = the candidates the folds skipped that vanilla's own point check passes: none was a hit. Folds really off:
  checked 14:58 with village_plains (-1792 2784 = the Phase 1 folds-on answer, 32 misses vs 10, biome/placement saved
  0) and igloo (198 misses, biome/placement saved 0).
- 15:00:12 folds back ON (timeout 900): the 4 found only in L2: waysign_camps 1312 3952 (33 misses, 128 s), bastille
  -6469 -2923 (17, 145 s), glacial_hut -7941 -2149 (15, 338 s), snowy_tent 4736 3168 (46 misses, 519 s): all 4 SAME
  as L2 folds off. -> every position compared agrees (210 + 21 + 4 + plains), the 2 that differ are the meta layout issue.
- 15:09 stop (clean), console/latest/stats of L2 kept as scratchpad console_l2.log, latest_l2.log, stats_l2.json.
- 15:13:25 st_l1 (fresh, base toml, 4 threads) LO-06: `celocate batch all` (258, 4 threads); a 2nd batch 3 s later
  refused ("A batch is already running"); then the 21 plain /locate on top. 15:39:12 batch done: 234/258 found, 2031
  misses, 1547 s (first L1 batch: 233, 2003, 1526 s); 20 timed_out, 4 not_found (mineshaft_mesa 1 miss = no mesa within
  radius 100; the 3 cave-fit ones). The 21 plain searches: 21/21 "The nearest" = known positions (none timed out this
  time). Batch rows vs every known position (21 + village_plains + igloo): 23/23 same; the 4 L2-only ones timed out
  again at 120 s. No exception after boot except the known village-piece reload errors (2). Saved: batch_l1b.json,
  stats_l1b.json, console_l1b.log, latest_l1b.log.
- 15:43:50 st_l3 (fresh, asyncLocatorThreads 1): `celocate batch philipsruins` 22/24 found, 207 misses, 331 s: 22/22
  positions = the 4-thread batch_l1b (misses differ per row: concurrent searches reuse each other's checked chunks;
  totals 207 vs 188). 15:49:37 `celocate batch minecraft` 18/19, 42 misses, 83 s: 16/18 same, village_desert -4536 -442
  (L1b -4538 -445, L2 -4533 -446) and village_savanna -1292 236 (L1b -1292 229, L2 -1280 236) = the meta layout issue
  again (3 different answers in 3 JVM runs; plains/snowy/taiga villages stable). mineshaft_mesa not_found in both.
- 15:51:20 LO-08: `celocate stats` after only the 2 batches: 43 rows = 43 batch rows; misses/loads/candidates/every
  savedByStage value equal per row, 0 mismatches (totals 249 misses, 280 loads, 141560 candidates).
- 15:51:36 LO-15: batchTimeoutSeconds 120 -> 5 (live, one write), `celocate batch towns_and_towers`: 30/51 found, 21
  timed_out, 192 s. Overshoot: deadline is checked between candidates/loads, a load in flight finishes (worst 31.5 s
  for village_old_growth_taiga, 7 loads; next 10.6 s). 15:55:05 `celocate batch all` (1 thread), 15:55:35 `celocate
  stop`: "stopping every running search ...", report written the same second, 258 rows: 9 found, 4 timed_out, 245
  "error: java.util.concurrent.CancellationException" (the queued ones never started) -> label bug, should read
  "stopped". `celocate status` -> nothing running. batchTimeoutSeconds back to 120.
- 15:57 st_l3 reboot (7.5 s) with srv-only kubejs/server_scripts/zz_scratch_tick.js (gap between tick starts, per 5 s:
  ticks, max gap, gaps > 100 / > 250 ms). Idle: 100 ticks / 5 s, max gap 51-69 ms.
- LO-05 (1 locator thread, takesapillage:pillager_camp from fresh far origins via `execute positioned`):
  A1 nonBlockingLoads true, (20000, 20000): hit 19092 21291 after 10 misses / 11 loads, 45.8 s. 55 s window: 546 ticks
  (9.9 TPS), max gap 16022 ms, 8 gaps > 250 ms; "Can't keep up" 11676 ms and 15971 ms.
  A2 true, (-20000, 20000): timed out at 120 s, 66 misses. 124 s: 2001 ticks (16.1 TPS), max gap 13946 ms, 39 > 250 ms.
  B nonBlockingLoads false (live, one write), (20000, -20000): hit 21328 -19888 after 14 misses, 42.7 s. 51 s: 774
  ticks (15.2 TPS), max gap 7859 ms, 12 > 250 ms. -> non-blocking is NO better than vanilla.
  jstack of the server JVM every 2 s during A2 (scratchpad dumps/): the Server thread sits in
  ServerChunkCache.getChunkFuture (m_8431_, line 194) -> BlockableEventLoop.managedBlock, called from
  LocateLoads.lambda$offThread$0 (LocateLoads.java:82) inside a TickTask. getChunkFuture called ON the server thread
  waits in managedBlock until the chunk is done; LocateLoads hands it to the server (supplyAsync(..., server)), so
  every load blocks the tick exactly like vanilla. Fix (not made, older 7.3 code): add the ticket in the server task,
  then call cache.getChunkFuture(...) from the search thread (off-thread it only schedules getChunkFutureMainThread
  and returns), or call getChunkFutureMainThread directly in the task. Single loads take up to 16 s (huge jigsaw
  starts + earth fill on the worker threads), which is what the server thread waits for.
- 16:03 nonBlockingLoads back to true, toml diffed = base; 16:04 stop.

## Results

| Test | Result | Evidence |
|---|---|---|
| LO-02 | PASSED | 212 found in both batches: 210 same position, 2 differ only by the meta layout issue (same chunk); the 21 L1-only and 4 L2-only finds agree with the other mode at a 900 s timeout; misses 2003 vs 10660; saved placement 40670, biome 156421 |
| LO-06 | PASSED (adapted) | A 2nd parallel batch is refused by design, so: 4-thread batch all + 21 plain /locate at once: no exception, no hang, 21/21 plain and 23/23 batch rows at the known positions; 1-thread compare below |
| LO-06 (1-thread part) | PASSED | 1-thread vs 4-thread batch rows: philipsruins 22/22, minecraft 16/18 same; the 2 = meta layout issue |
| LO-08 | PASSED | 43 stats rows = 43 batch rows, every count equal |
| LO-15 | PASSED (label bug) | 5 s: 21/51 timed_out; stop wrote the 258-row report at once; never-started rows say "error: CancellationException" not "stopped" |
| LO-05 | FAILED | Ticks stall for seconds per load with nonBlockingLoads true (max gap 16.0 s / 13.9 s) as with false (7.9 s); cause: LocateLoads.java:82 runs getChunkFuture on the server thread, which blocks |
| LO-01 | PARTLY | Report written, 258 rows, misses recorded; 42 rows timed_out at the 120 s batch timeout (the 21 that folds on found were all found folds off at 900 s; the rest are rare in both modes or the known cave-fit bug), so "no task times out" does not hold at the default timeout |

## Open issues
- Jigsaw assembly differs between JVM runs with the same seed (Phase 5 L1 vs L2): meta/village_towers anchors
  village_desert/savanna got different boxes (5-12 blocks) and a different tower spot (80 blocks). Within one run
  stable. Not checked yet: whether plain (non-meta) jigsaw starts differ too (their locate position is the chunk, so
  the batch can't show it); likely cause = a pool's element order coming from a hash-ordered collection somewhere
  (PoolTuner itself keeps order). Matters for Phase 8 two-world comparisons and for /locate of meta parts after a
  reboot. Older system: recorded, not fixed.
- Cloud Storage + Domestication Innovation village houses use the builtin ProcessorLists.EMPTY holder -> their pieces
  are saved as {} and dropped when a structure_starts chunk reloads (see 14:51 log). Third-party; possible fix: PoolTuner
  (or a mixin) rebinds each pool element's processors holder to the world's registry. For the user.
- /celocate stop: searches still queued in Async Locator's pool end with a CancellationException, and the batch row
  says "error: java.util.concurrent.CancellationException" instead of "stopped" (LocateCommand.batch whenComplete).
  Report is otherwise complete. Label only; 1.0.7 fix candidate (treat CancellationException as "stopped").
- LO-05 FAILED: LocateLoads non-blocking path blocks the server thread (getChunkFuture inside a server task waits in
  managedBlock). Every /locate, batch, census and map search that loads chunks stalls ticks for the whole chunk
  generation (seconds each). Older system (7.3/locate 3.2); explains the Phase 1/2 lead "9 s lag during a 10-miss
  locate" and "server thread took 31 s to reach stop". Small fix, for 1.0.7, user to decide.
