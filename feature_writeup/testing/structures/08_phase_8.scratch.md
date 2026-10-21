# M8 scratchpad: Phase 8 (8.0.8, two-world comparisons)

## State (keep current)

- Constraints (user 2026-10-14): ignore Nether/End; ignore structure-to-structure rates; correctness only.
- Milestone: M8. Tests: ST-144 (order independence), ST-148 (shaping off), ST-157 second half (peaceful off), ST-145
  (placementPriority off, overlap count; the "about a third" estimate is a rate: skipped).
- Server: same as M7 (session 5 scratchpad srv/, patcherMod 1.0.7 jar = 1.0.8 worldgen; 374 mods). The M7 scratch tag
  patch is REVERTED (srv kubejs/server_scripts = instance copy): Phase 8 runs as shipped (pruning dead).
- Toml = scratchpad toml_m7_base_107.toml (instance + density 4, earth/water fill log ALL, meta + priority logs on).
- Areas: S1 = Chunky square r 400 at -1400 -2592 (inner main square of M7; villages, peaceful drops known).
  S2 = r 256 around the town_villages meta at chunk [-235, -224] (fortified village anchor at -3727 89 -3564, found by
  an M7 locate; kept at default limits) -> villages near a town, for shaping (ST-148).
- Worlds (seed 8675309, 4 locator threads, all fresh):
  st_8a base: west strip r 400 at -2200 -2592, then S1, then S2.
  st_8b base: east strip r 400 at -600 -2592, then S1. (ST-144: compare S1 starts with st_8a.)
  st_8cd: structure_groups.json priority.shape = [] and priority.peaceful removed (srv copy): S1 + S2.
    (ST-148 in S2: villages dropped whole instead of shaped; ST-157 in S1: hostile starts at st_8a's peaceful drops.)
  st_8e: [worldgen.systems] placementPriority = false: S1 (+ S2). (ST-145: villages overlapped by a ranked group's
    structure; st_8a should have none.)
- Running: nothing. M8 tests all run 07:00; next: writeup 08_phase_8.md.

## Log (append-only)

- 04:23 M8 started; st_8a boot. 04:30:21 Done (302 s).
- st_8a Chunky (scratchpad chunky_seq.sh): west strip r 400 at -2200 -2592 (04:30-04:39:52, 2601), S1 (-04:46:08,
  2601), S2 r 256 at -3727 -3564 (-04:54:46, 1089). Log latest_8a.log. Starts (p8.py starts, full chunks of the
  square): a_s1.json 674 starts / 2550 chunks, a_s2.json 219 / 1089.
- S2 metas: town_villages [-235, -224] (fortified village -3727 89 -3564) and [-240, -208] (red roof castle -3860 87
  -3350); 4 companion lines: [-230, -216], [-239, -231] of the first; [-236, -214], [-245, -201] of the second. So
  companions do work once the chunks around a town generate (M7 saw none: its towns came from /locate only).
- Overlap check (p8.py overlap: settlements vs other ranked groups, 3D piece boxes): st_8a S1 3 settlements / 83
  ranked others: 0 overlapped; S2 5 / 19: 0. (M7 st_c main square: 13 / 496: 0.)
- 04:55 st_8b boot (east strip then S1). 05:02:15 Done (300.7 s). Chunky east strip r 400 at -600 -2592 (-05:12:44),
  S1 (-05:19:16). latest_8b.log, b_s1.json 674 starts / 2550 chunks.
- ST-144: p8.py same a_s1.json b_s1.json: 674 vs 674, same id+chunk 674, only A 0, only B 0, same start with
  different piece boxes 0. PASSED (two JVM runs, opposite approach, identical starts and pieces).
- 05:20 st_8cd: srv kubejs/generation_scripts/worldgen/structure_groups.json priority.shape = [] and priority.peaceful
  deleted (re-serialised with json.dumps indent 2; restore = copy the instance file back). Boot; then S1 + S2.
- 05:26:49 st_8cd Done (298 s); no "[placement-priority] N peaceful structure(s)" line (A had "104 peaceful ... keep 70
  hostile ... radius up to 8"). Chunky S1 (-05:35:46), S2 (-05:41:34). latest_8cd.log, cd_s1.json 676, cd_s2.json 221.
- ST-157 (p8.py peace latest_8a.log cd_*.json): A's peaceful drop lines name every candidate a slot tried (several per
  slot). Per slot, CD now has a hostile start: S1 [-95, -169] zombie dungeon, [-88, -175] meta/pillager_stronghold_siege
  (A: dropped near a viking house, r 3); S2 [-231, -234] T&T OGT outpost, [-240, -217] T&T Tudor outpost (A: near
  town_villages, r 8), [-221, -236] OGT outpost (near meta/house_camp, r 3), [-223, -238] eugene:undead_castle (near a
  sawmill, r 3), [-234, -237] zombie dungeon (near a villager forge, r 3). 7 of 7 slots. PASSED.
  In A, the slot [-231, -234] fell through to eugene:fake_trader_camp (kept; hostile via pillager_settlements) while
  the bigger outposts there were dropped: radius is piece-to-piece and the small camp's pieces are > 8 chunks from the
  town's. At [-240, -217] the fake camp was dropped. Correct.
- ST-148 S2: A vs CD same 216 starts with identical pieces (+3 only A, +5 only B = the peaceful differences above);
  the 4 companion villages identical; no `dropped <village>` line in CD. So no village reached a town in S2 and
  shaping never acted: inconclusive. Next: S3 = r 400 at 3450 -1100 (M7 towns [205, -69] fortified village 3289
  -1138, [207, -80] red roof castle 3334 -1300, [229, -56] castle 3684 -874) in CD now, then in A.
- CD S3 (05:43-05:51:48): cd_s3.json 680 starts; companions: [197, -67] of [205, -69], [203, -88] of [207, -80],
  [224, -63] + [233, -50] of [229, -56], [239, -44] of [243, -37] (castle 3908 -570); no `dropped <village>` line.
  (Two towns 11 chunks apart: fine at density 4, the grid shrinks by 2.)
- 05:55 st_8a reboot (structure_groups.json restored = instance copy), S3 pregen (-06:05:18). a_s3.json 671 starts
  (a first read at 651 was taken before the save: redone). A vs CD S3: 669 same, identical pieces in every shared
  start; only A 2 (fake trader camp, ancient towers), only CD 11 (outposts, siege weapons, dungeons, a mansion near the
  towns = peaceful off). Same 5 companions. Overlap check A S3: 9 settlements / 59 ranked others: 0.
- ST-148 so far INCONCLUSIVE: across S2 + S3 (8 towns, 9 companion villages, ~15 grid villages) every village is
  piece-identical with shaping on and off, and no village is dropped with it off: no village ever reached a town.
- Side finding: srv pool_config.json "original" weight 2 (instance file, written by the client 10-18: 1) for 5
  beautify:botanist_house_* entries; "current" 1 in both. Beautify seems to inject them twice on a dedicated server.
- Plan for ST-148: soft-rule loosening (spacing-type, 7.4.8): meta/town_villages companions distance [112, 160] ->
  [64, 96] in srv meta_structures.json, two fresh worlds st_8f (shape on) / st_8g (shape off), S3 in each.
- 06:06 st_8e: [worldgen.systems] placementPriority = false (srv toml); S1 + S3. (ST-145)
- 06:11:15 st_8e Done in 189 s (priority on: ~300 s; the priority pass costs about a third of spawn prep at density 4).
  S1 (-06:17:45), S3 (-06:24:12). latest_8e.log, e_s1.json 678, e_s3.json 681.
- ST-145 (p8.py overlap, settlements vs other ranked groups, 3D piece boxes): priority OFF: S1 1 of 3 settlements
  (minecraft:village_savanna [-82, -151] x philipsruins:field_stone_ruins_rocks [-79, -150], ruins), S3 1 of 4
  (meta/town_villages [205, -69] x meta/ruin_scatter [203, -69], ruins). Priority ON: 0 of 3 (A S1), 0 of 9 (A S3),
  0 of 5 (A S2), 0 of 13 (M7 st_c main square). PASSED; the "about a third" estimate is a rate (skipped).
  Priority off also means no companion villages (PlacementPriority.companions returns at once): S3 has 4 settlements
  instead of 9.
- 06:25 st_8f: placementPriority back on (toml = base); srv meta_structures.json town_villages companions distance
  [64, 96] (soft-rule loosening, put back after); shape on. 06:31:58 Done; S3 pregen (-06:41:46). latest_8f.log,
  f_s3.json 674. 7 companion lines (2 per town for [205, -69], [207, -80], [229, -56]; 1 for [243, -37], not in full
  chunks). scratchpad towns.py (companion vs its town: overlapping piece pairs, closest XZ gap): all 0 overlapping
  pairs; closest gaps: taiga [210, -70] 0 (boxes touch), OGT [201, -68] 1, flower forest [231, -53] 1, taiga
  [210, -75] 7, taiga [204, -85] 12, Tudor [226, -60] 61. Shaped: three run right up to the town and stop.
- 06:42 st_8g: same + structure_groups.json priority.shape = [] (peaceful kept). S3.
- 06:49:20 st_8g Done; S3 (-06:59:00). latest_8g.log, g_s3.json 672. Drop lines: minecraft:village_taiga [210, -70],
  village_old_growth_taiga [201, -68] and [210, -70], exclusives/village_swedish [210, -70], village_flower_forest /
  village_plains / exclusives/village_tudor / village_meadow / village_sunflower_plains [231, -53]: each "overlaps
  creativeengineering:meta/town_villages". These are exactly the three companion slots whose villages touched the town
  in st_8f (gap 0-1); each slot tries other members, only [201, -68] finds one (exclusives/village_swedish, 119 pieces,
  gap 5); [210, -70] and [231, -53] end empty. The companions that stayed clear ([210, -75], [204, -85], [226, -60])
  are piece-identical in F and G. p8.py same f_s3 g_s3: 670 same, 0 piece differences, only F 4 (the 3 shaped
  villages + a camp), only G 2 (Swedish village, a forge). ST-148 PASSED (with the loosened distance).
- 07:00 stopped; srv structure_groups.json, meta_structures.json, toml restored (= instance / base). Instance files
  untouched all phase.

## Results

| Test | Result | Evidence |
|---|---|---|
| ST-144 | PASSED | S1 from the west (st_8a) and from the east (st_8b), two JVM runs: 674 = 674 starts, same chunks, same pieces |
| ST-157 (2nd half) | PASSED | Peaceful off (st_8cd): a hostile start in each of the 7 slots where st_8a logged peaceful drops |
| ST-145 | PASSED (rate skipped) | Priority off: 2 of 7 settlements overlap a ruin; on: 0 of 30 over 4 areas |
| ST-148 | PASSED (companion distance loosened) | Shape on: 3 companions run up to the town, 0 overlaps; off: those 3 slots log `dropped <village> ... overlaps meta/town_villages` |

## Open issues
