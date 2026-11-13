# Scratch run r1112, 2026-11-12

Runner: session d0fc2fba. Owner request: "execute the scratch test queue". Continues from r1111
([scratch_run_2026-11-11.md](scratch_run_2026-11-11.md) 8): the server rows added since, then the r1111 batch-fault reruns.

## 0. Performance capture

Perf session `r1112`, opened 21:19 before the first boot. Report without `--prune` at the end.

## 1. Scope

Taken, in order:
1. SQ-RP-01 to -06 (Spice of Life: Raspberry Pie Addition, added 2026-11-12), group A fresh world `rp`.
2. r1111 batch-fault reruns with fixed batches: SQ-HH Machines, Crow, Gear (group A); SQ-OB-04, -09; SQ-VH-06 (group B).

Not taken: every client row (CQ-, incl. CQ-RP-01 to -28 and CQ-CHK-01: real player or window); SQ-FLR-02 to -05
(Create rig not built); SQ-BREED-01 (`/dumpbreeding` not written); SQ-VH-11, SQ-OB-11 (no old save); SQ-FDI (Spice
of Life: Carrot Edition jar not in `mods/`); SQ-HH-1574, -1575 (known crash, TODO `largecontent/mod-additions #65`);
row-text faults (rejudged, not rerun; section 8).

## 2. Tools

`tools/scratch_query/batches/r1112/`: `rp1.qb` to `rp3.qb` (SQ-RP, three boots of world `rp`), HH and OB/VH rerun
batches written by subagents from the r1111 verdicts. Results `perf/runs/r1112/<block>/`.

## 3. Jars

| Jar | `mods/` | build/libs used | Rows |
|---|---|---|---|
| Spice of Life: Raspberry Pie Addition | 11-12 20:47, **did not boot** (section 7 21:20); 21:27 fixed build installed | 11-12 21:21 | SQ-RP |
| Creative Common | 11-12 18:55 | 11-12 20:46 | all |
| Creative Compat, Calendar, Brewing | 11-10 | 11-12 20:46 to 20:47 | all |
| Creative Delight | 11-10 21:20 | 11-11 16:52 | SQ-HH, SQ-OB |

Scratch servers get COPIES of the build jars.

## 7. Run notes

- 21:18 RUNNER.md claimed. No game JVM, 21 GB free. Perf session r1112 open.
- 21:20 rp boot 1 CRASHED in mod loading: `Mod solraspberrypie requires solapplepie 2.2.0, Currently, solapplepie is
  1.19.2-2.2.0` (Apple Pie's jar version is `1.19.2-2.2.0`; the range `[2.2.0]` never matches). Blocks the pack's
  client and server too. Fix (boot metadata only, no behaviour change): `mods.toml` range `[1.19.2-2.2.0]`.
  Rebuilt 21:21, rebooted: Done, `Diet ladder: 38 rungs, 5 slots, 14 pool effects`.
- 21:23 rp1.qb: SQ-RP-01, -02 PASS, -05, -06 judged PASS (below). Apple Pie's `solapplepie:food_book` recipe is
  already removed: `diet_log.js` loads from the pack's `kubejs/data/creativedelight/kubejs/` folder, so SQ-RP-06's
  "rebuild" note is wrong (the CD jar copy only matters for a pack without that folder).
- 21:24 boot 2 (ladder edited): SQ-RP-03 PASS, pause list survived the restart (SQ-RP-05b PASS).
- 21:26 boot 3 (ladder broken): SQ-RP-04 PASS. Stopped 21:27; fixed jar installed into `mods/` (no game running).
- 21:30 three subagents writing the HH Machines / Crow / Gear and OB / VH rerun batches.
- 21:36 subagents done: `hh_mach_a.qb` + `hh_mach_a2.qb` (two boots: SQ-HH-320 needs saltCaves false, -274 a
  restart), `b_vh.qb` (SQ-VH-06 with new probe `zq_heapcol.js`, SQ-OB-04, -09), `hh_gear.qb` (24 rows),
  `hh_crow.qb` from `gen_hh_crow.py` (27 rows). Probe changes: `zq potion` prints the registry id, `hh_gear_probe.js`
  hurt/horse lines, `hh_crow_probe.js` tally counts shoulder crows.
- 21:37 hhm boot 1 (world hhm12, salt caves rarity 1 band 0..0). SQ-HH-223 and -233 BATCH-FAULT again: a fresh
  world starts in rain and a trough fills with rain water, then refuses items. `weather clear` plus 20 s (rain level
  fades) and a trough reset fixed both; rerun PASS. SQ-HH-319 regex (`Placed "hearthandharvest:salt_cave"` in 1.19.2).
- 21:50 hhm boot 2 (saltCaves, cornMaze false): SQ-HH-274, -320 PASS.
- 21:55 hhc boot (Boot F crow/salt numbers, Loading Protection protectionDuration 1 in srv config only): hh_crow.qb.
- 22:05 hh_crow.qb: PASS 23, FAIL 4 (461, 474, 479, 482). 461: the creative bot 2 blocks off picked up the 5th
  diamond (BATCH-FAULT; rerun with the bot 8 off PASS). 479: 3 of 4 stages worn in 1500 ticks; rerun with a 150-sample
  watch PASS (block air, 10 StayNearSaltGoal samples). 482 rerun with 80 samples: FAIL again (below). 474: live check,
  salt dust on an unworn `salt:salt_block` places a `vintagedelight:salt[layers=1]` on top (the item's own use), so
  "stage 0 takes none" fails by design of the dust item: owner question.
- 22:14 hhg boot (Loading Protection protectionDuration 1 in srv config): hh_gear.qb, then rowtext.qb (SQ-HH-412,
  -1524, -1529, -1590, SQ-OB-03 with the corrected ids).
- 22:25 hh_gear.qb LOOK 25 judged by a subagent (`perf/runs/r1112/verdicts/hhg.md`): PASS 21, FAIL 1 (1555), BATCH-FAULT
  2 (1540 expect 3 vs 2 with a hoe at Damage 248, behaviour right; 1524 recipe ingredients not printed by `zq makes`),
  INCONCLUSIVE 1 (1534). rowtext.qb: SQ-HH-412, -1590, SQ-OB-03 PASS; -1529 rejudged (below).
- 22:26 bw boot (normal world seed 8675309, `zq_heapcol.js`): Done 82.5 s; b_vh.qb.
- 22:30 b_vh.qb: SQ-VH-06, SQ-OB-04 LOOK (judged below). SQ-OB-09 found no rabbit at all: every summoned animal
  (cows too) vanished at once because prep_srv group B writes `spawn-animals=false` (BATCH-FAULT; batch header now says
  to prep with `spawn-animals=true`). Restarted bw with it on: SQ-OB-09 PASS. (A console `setworldspawn` during the
  check moved the scratch world's spawn; scratch only.)
- 22:35 all servers stopped, perf session r1112 closed and reported (`perf/sessions/r1112/summary.md`, no `--prune`).

## 8. Verdicts

| ID | Result | Evidence |
|---|---|---|
| SQ-RP-01 | PASS (after the mods.toml fix) | `Diet ladder: 38 rungs, 5 slots, 14 pool effects`; no mixin error, no Apple Pie benefits or resetOnDeath warning |
| SQ-RP-02 | PASS | `Seeded ./rp/./serverconfig/solraspberrypie/ladder.json from .../defaultconfigs/...`; diff identical |
| SQ-RP-03 | PASS | one `skipped 1 entries ... unlock nosuchmod:thing at 3.0`; one `V10: flat minecraft:absorption at 4.0 cannot be refreshed safely; skipped`; still 38 rungs |
| SQ-RP-04 | PASS | `DIET LADDER FILE IS BROKEN OR MISSING` block (Unterminated array), no `Diet ladder:` line, `/solrp pause list` answers |
| SQ-RP-05 | PASS | none -> `Paused diet bonuses for 1 players` -> `1 paused: rpb` -> `Resumed ...` -> none; still `1 paused: rpb` after two restarts |
| SQ-RP-06 | PASS | `solraspberrypie:diet_log` (type `diet_book`) and `liquid_diet_log` loaded; `/zq recipes food_book` 0 (Apple Pie's removed, `diet_log.js` loaded), no NCH error |
| SQ-HH-209 | PASS | first useitem CONSUME, Amount 1000; on lava useitem FAIL, Amount still 1000; Test passed |
| SQ-HH-223 | PASS (rain fix) | wheat, carrot, seagrass, alexsmobs:lobster_tail each 1b in its trough; acacia_blossom also taken (RECORD) |
| SQ-HH-233 | PASS (rain fix) | extract 7b, sneak extract empty, refill 8b, wheat drop 8b, Test passed |
| SQ-HH-266 | PASS (rerun with water) | fill accepted 1000, hand water_bucket, tank empty, basin slots empty |
| SQ-HH-274 | PASS | after restart: 208 -60 208 role controller, tank 0 / 32000; members point to it |
| SQ-HH-319 | PASS (regex fixed) | `Placed "hearthandharvest:salt_cave" at 4544, 40, 96`; cave_air 1842, raw rock salt 1060 |
| SQ-HH-320 | PASS | saltCaves false, cornMaze false: `Failed to place feature`, `Failed to place structure`, cave_air 0 |
| SQ-HH-418, -420, -424, -425, -436, -437, -441, -442, -443, -449, -451, -452, -453, -459, -460, -462, -463, -465, -466, -485, -496, -499, -502 | PASS | `perf/runs/r1112/hhc/rows/`, all checks of `hh_crow.qb` met |
| SQ-HH-461 | PASS (rerun, bot moved off) | nest Slots 0 to 3 diamonds, no Slot 4; 5th diamond on the ground (`items total=1`). First pass: a creative bot 2 blocks off picked it up |
| SQ-HH-479 | PASS (rerun, longer watch) | cow licked the block to air within 3000 ticks at saltLickInterval 200; one salt dust dropped |
| SQ-HH-482 | FAIL | cow a (21 blocks) drifts to 20.32 (block distance 20, inside the radius) by a look-around before the goal runs, then makes no lick trip in 1600 ticks at saltLickInterval 200; `seen a StayNearSaltGoal no` in both runs. Cow b at 24 not seen (PASS half). TODO |
| SQ-HH-474 | FAIL (owner question) | stages restore one per dust (4 -> 1 left over 3 uses), waxed well -> waxed lightly with one dust; on stage 0 the dust is used and places `vintagedelight:salt[layers=1]` on top of the block (Vintage Delight item use, H&H handler only covers worn stages) |
| SQ-HH-1505, -1506, -1509, -1511, -1516, -1522, -1535, -1536, -1537, -1539, -1550, -1553, -1556, -1559, -1560, -1566, -1569, -1585, -1603, -1604, -1606 | PASS | `perf/runs/r1112/verdicts/hhg.md` |
| SQ-HH-1540 | PASS (rejudged) | farmland 2 and the hoe broke: a hoe at Damage 248 of 250 breaks on its second till; batch expected 3 |
| SQ-HH-1524 | PASS (rejudged) | max stack 1, max damage 0, tag of 4 hats; recipe from `zq rdetail farmers_hat` on the bw boot (section 7) |
| SQ-HH-1534 | INCONCLUSIVE | `farmland: 1`, `damage=1` (a single till); the same spread worked in 1535, 1536, 1539, 1540 on this boot. Rerun alone with the sneak state printed |
| SQ-HH-1555 | FAIL (mod gap) | 17 polls `count hearthandharvest:hops: 1`; the empty lattice counts as solid, the farmland under it turns to dirt, and hops need farmland: a survival hops column can never grow while that rule is on. TODO |
| SQ-HH-412 | PASS (row text fixed) | `itemtag hearthandharvest:farmers_hats (4)`, no summer hat, a scarecrow recipe per hat |
| SQ-HH-1529 | PASS (rejudged) | r1111 summer hat `xp=0` matches the tag change (summer hat out on purpose); the in-tag case is SQ-HH-1528 (PASS r1111). r1112 farmerbrim rerun read 0 with the bot 2 blocks off (orb absorbed, batch fault) |
| SQ-HH-1590 | PASS (ids fixed) | every listed food in its `creativedelight:food_type/*` tag |
| SQ-OB-03 | PASS (ids fixed) | `hearthandharvest:farmers_hats`: four hats, no summer hat |
| SQ-HH-206 | PASS (rejudged) | r1111: all 8 listed members, none of wine, moonshine, root beer; count 26 because 3 ids merged out (row said 29) |
| SQ-OB-06 | PASS (rejudged) | r1111: 6 kernels, cob 75 %, husk 33 % (SQ-OB-12 says 33 %; this row's 75 % husk is stale) |
| SQ-VH-06 | PASS (owner look) | Heap at -10 117 -2; bedrock at 0 and 127 Test passed; per-surface read inside the Heap (`zq_heapcol.js`, 6526 columns): flesh is the top id of floor (8697 of 14099, 61.7 %, porous flesh 554) and ceiling (11881 of 18267, 65 %); netherrack 10 % / 16 %, Better Fortresses bricks the rest; floor flesh on bedrock 7. Row says "floor and ceiling are flesh": owner question `#73` (8) |
| SQ-OB-04 | PASS | apple, pumpkin, sweet berry cheesecake, grape, syrup cheesecake: crust + 3 fruit + sweet; the `_from_filling` recipes are crust + can |
| SQ-OB-09 | PASS (rerun, spawn-animals on) | config 0.0; adults `count: 50`, babies `count: 36`, RabbitType 99 Test failed |

Totals (rows judged this run): PASS 64 (SQ-RP 6; SQ-HH Machines 7, Crow 25, Gear 23 incl. 2 rejudged, row-text reruns
and rejudges 6; SQ-OB 4; SQ-VH 1, counted once each), FAIL 3 (SQ-HH-474 owner question, -482, -1555), INCONCLUSIVE 1
(SQ-HH-1534). Findings: TODO `largecontent/mod-additions #75` (hops never grow), `#76` (salt lick at the radius edge),
`#73` items 7 and 8; Raspberry Pie mods.toml boot fix (section 7 21:20).

Not run: every client row; SQ-HH-1534 rerun, SQ-HH-1551 (two-boot row), SQ-HH-1607 to -1611 (real player), SQ-HH-1574,
-1575 (known crash), SQ-OB-02 (client), SQ-FLR-02 to -05, SQ-BREED-01, SQ-VH-11, SQ-OB-11, SQ-FDI (as r1111).
