# Scratch run 2026-11-19 (r1119)

Owner request 2026-11-19 20:30: "execute the scratch test queue". Scope ruling (owner, 2026-11-19): default plan
only; the r1118 rerun list ([r1118/rerun.txt](r1118/rerun.txt)) and its drive steps 151 to 167 wait for a later run.
Main session as orchestrator; the server rows run in one detached `qrun.py drive` (run id r1119, state in
`perf/runs/r1119/drive/`).

## 0. Performance capture

Perf session `r1119`, opened 20:40 before the first boot (collector pid 4048621). JFR on for every server.

## 1. Scope

Taken: `qrun.py plan` default (saved as `perf/runs/r1119_plan.json`): 338 rows (jar-changed 338) on the changed jars
creativebrewing, creativecalendar, creativecommon, creativecompat, creativedelight, creativedevtools,
creativeengineering, creativeequipment, creativemagic, creativeworldgen, powergrid, storagedelight, tempad. Phases
server 296, worldgen 38, long 4. Plan estimate 194 to 677 min. The driver brings lower-boot rows of multi-boot
sections along: 109 steps, 769 rows (driver estimate about 1031 min).

Not taken: the r1118 rerun list and steps 151 to 167 (owner ruling above); client rows (the driver is server-only);
`--never` / `--failed` / `--rewritten`; the 3 owner playtest rows. `/dumpall` skipped: no planned row needs fresh
dumps (r1118 wanted it for its rerun pass).

Preflight 20:30: `check_all.py --fast` had 2 FAIL (`status_index`, `test_queue_rows`): CQ-RPI-58 to -61 (RP.qb
section 4, committed in e5e41e95) had no steps and no `@draft`; marked `@draft` (client shot rows), then 0 FAIL.
`qrun.py lint` on all 73 planned rows files: 0 errors. Disk 74 GB free. No game JVM up. `jarstamp.py pending`: 0
built, not installed.

## 2. Tools

`qrun.py drive` (qdrive.py), `drive_watch.py` (hang poll), `qrun.py judge` / `table`, `perf_session.sh`. Server dirs
`perf/runs/r1119/sNN`, results `perf/runs/r1119/sNN_res`, driver logs `perf/runs/r1119/drive/`.

## 3. Jars

Every own jar from `mods/` (nothing built and not installed at the start); each step pins its own jars
(`sNN/pinned.json`).

## 4. Order

One drive, plan order: server (steps by rows file), worldgen, long. Steps and rows:
`python3 tools/scratch_query/qrun.py drive status r1119`.

## 5. Row map

Per step in `perf/runs/r1119/drive/state.json`.

## 6. After the run

Judge LOOKs, sort FAIL vs BATCH-FAULT, queue Run log entry, writeup test lists, TODO rows for FAILs, perf report.

## 7. Run notes

- 20:40 drive started (109 steps, 769 rows); step 1 AC.qb section 1 group A.
- 20:52 steps 5-8, 10-13, 15 FAILED in prep: modset port_chain (source ceporttesting) finds only creativecommon in ../CEPortTesting/mods; the chain jars moved to its disabledmods/ (2026-11-13). Same blocker as r1118 handover 5; not fixed (another instance), rows blocked. Steps 1-4, 9, 14: PASS 5, LOOK 6.

- 21:15 step 21 (CDFL.qb section 1, port_chain cooking) failed in prep the same way: 10 steps blocked in all
  (5, 6, 7, 8, 10, 11, 12, 13, 15, 21).
- 21:16 owner: "wrap up the tests gently". `qrun.py drive stop r1119`; the driver stopped after the current row at
  21:17: step 26 (CGE.qb section 2 group B) 2 of 6 rows done, state `stopped, resumable`. No game JVM left; RUNNER.md
  idle. Perf session r1119 stopped and reported (no `--prune`):
  [perf/sessions/r1119/summary.md](../../perf/sessions/r1119/summary.md).

## 8. Final verdicts

STOPPED by the owner 2026-11-19 21:17 at step 26 of 109 (15 steps done, 10 failed in prep, 83 pending, step 26
partial). 31 results: PASS 12, LOOK 19 (not judged), FAIL 0, CRASH 0. Rows of the 10 prep-failed steps did not run.

| ID | Result | Evidence |
|---|---|---|
| SQ-AC-01 | PASS | 1 check passed |
| SQ-BBQ2-03 | PASS | 2 checks passed |
| SQ-BBQ2-06 | LOOK | open look 1: a cutting recipe (endersdelight:cutting/shulker_filet or a CD copy) with input endersdelight:shulker_mollusk and 2 endersdelight:shulker_filet (the part=results line) |
| SQ-LGM-13 | LOOK | open look 1: pot pie block feast/whole, serving feast/serving; omurice feast/*; tomato egg soup soup/soup; cod deluxe and hearty lunch meal/plate; both tags list block items |
| SQ-BRA-01 | PASS | 2 checks passed |
| SQ-BRA-02 | LOOK | open look 1: surface_audit.json: asphalt tier asphalt, soul_sand no tier speedFactor 0.4, white_carpet collisionTop 0.0625, errors 0 |
| SQ-BRA-04 | PASS | 19 checks passed |
| SQ-CAL-02 | LOOK | open look 1: calendar file has [seasons], [crop_seasons], [time.*], [perf] seasonsMeltIntervalTicks = 4, [wthit]; CE file lost them |
| SQ-CAL-03 | LOOK | open look 1: pack order lists creativecalendar before creativedelight and creativebrewing; core finished line present |
| SQ-CBB-12 | PASS | 4 checks passed |
| SQ-CBP-07 | LOOK | open look 1: diff of the moved sections against configs_edited/root_config/creativebrewing-common.toml (shell.txt of this row is the section list; the diff is in the run notes) |
| SQ-CBW-02 | LOOK | open look 1: the beef left both boards after the knife use (filled=0), the kill line removed the cut result items (minced beef, farmersdelight:cutting/beef) before the breaks; each break drops 1 of its own board, bare hand and axe |
| SQ-CBW-03 | LOOK | open look 1: board inv: the pie on the board or not placed as FD's board allows (pass: not placed as a block on the board); the bot no longer holds the pie if the board took it |
| SQ-CC-01 | LOOK | open look 1: the mods.toml lines: immersive_portals and heartstone optional with ordering="NONE" |
| SQ-CC-02 | PASS | 6 checks passed |
| SQ-CC-05 | PASS | 5 checks passed |
| SQ-CC-07 | PASS | 5 checks passed |
| SQ-CC-08 | PASS | 6 checks passed |
| SQ-CC-13 | LOOK | open look 1: the summary names Common's server tick, dimension tick, chunk wait and KubeJS handler events |
| SQ-CC-15 | LOOK | open look 1: no mixin error naming gravity; the per-entity Create Gravity logic on overworld entities is judged by SQ-CGE-08 (the End only; overworld vanilla) |
| SQ-CC-16 | LOOK | open look 1: the Done time matches the last run (no multi-second WorldEdit stall); before and after counts equal (no forced generation from the teleport); the Nock Enough Arrows whistle arrow is a client check (CQ) |
| SQ-CC12-03 | LOOK | open look 1: no mixin or class error from our jars (third-party optional-class lines named); no out-of-range WARN; Common files loaded; 0 startup ERROR; N permanent aliases N > 0 |
| SQ-CCO-06 | PASS | 3 checks passed |
| SQ-CDFL-04 | PASS | 2 checks passed |
| SQ-CDNS-02 | PASS | 4 checks passed |
| SQ-CDV-04 | LOOK | open look 1: one *_mech_crafting per metal and wood, netherite, one sequenced assembly, all creativedevtools: |
| SQ-CGE-01 | LOOK | open look 1: Done line present; no WARN/ERROR naming either Create Gravity mixin |
| SQ-CGE-03 | LOOK | open look 1: judged from SQ-CGE-02c lines of the item and TNT types |
| SQ-CGE-04 | LOOK | open look 1: item y stays 61.000 after landing (no climb), falling block removed=true and block 0 61 0 is sand; tnt rests on 61 |
| SQ-CGE-07 | LOOK | open look 1: shell.txt holds the four ok lines (each shell step exits 1 when its check fails) |
| SQ-CGE-13 | LOOK | open look 1: shell.txt: the two boat heights (End, overworld) within 0.05 blocks, both at the water surface (about 65.9) |

## 9. Handover

1. **Judge the 19 open LOOKs**: `qrun.py judge perf/runs/r1119/sNN_res` (s00 to s25), then fill each
   (`qrun.py judge RES ID N VERDICT "evidence"`). Most are log or shell.txt reads (no shots).
2. **Resume**: `setsid nohup python3 tools/scratch_query/qrun.py drive resume r1119 &` continues at step 26 (its first
   row without a result) and runs the 83 pending steps; driver estimate about 960 min. The failed steps are retried
   by a resume too, so fix the blocker below first or they fail again in seconds.
3. **Blocker for the owner (no TODO row yet; same as r1118 handover 5)**: modset `port_chain`
   (`tools/scratch_query/modsets/port_chain.toml`, `source = "ceporttesting"`) needs its chain jars in
   `../CEPortTesting/mods` or `disabledmods/cba_parked/`; since 2026-11-13 they are in CEPortTesting's
   `disabledmods/`. Either move them back, or point the manifest at the pack's jars (`source = "pack"`). Blocked:
   CBA, CBB, CBC, CBO, CBP, CDFL rows of steps 5 to 15 and 21.
4. Not done: writeup test lists and README status words (nothing judged yet); no changelog bullet (test run only,
   nothing player-visible changed).
