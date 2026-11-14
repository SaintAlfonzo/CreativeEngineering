# Scratch run r1113, 2026-11-13

Runner: session creativeengineering2-b5 (scratch runner subagent; orchestrator "main"). Owner request (via the
orchestrator): complete the scratch queue (every never-run row, plus pending FAIL / BATCH-FAULT / INCONCLUSIVE reruns),
and scratch test the Create: Power Grid backport (SQ-PWG port rows) phase by phase as it is built, at boot boundaries.

## 0. Performance capture

Perf session `r1113`, opened 01:05 before the first boot. JFR on for every server. Report without `--prune` at the end.

## 1. Scope

Start list: `tools/scratch_query/unrun_rows.py --ids` at 00:40, saved as
`tools/scratch_query/batches/r1113/unrun_at_start.txt`: 547 rows never run (249 server rows). The server rows were
split into four chunks (`chunk1.txt` to `chunk4.txt`) and triaged by four subagents into batches; per-row decisions in
`c1_triage.md` to `c4_triage.md` (same folder). c4_triage.md part 2 lists the pending reruns of r1109 to r1112.

Taken: about 215 server rows in 28 boots (section 5), the reruns in `rerun_*.qb`, and every SQ-PWG port row the
orchestrator names (interleaved at boot boundaries).

Not taken: client rows (CQ-, and server rows whose check needs a window: listed per chunk in the triage files), rows
needing an old save that no surviving jar set can build (c1: RDX-13, NDM-08, CUP-05, CSI-02; c3: DCM-03, CDFO-01,
IDS-03; c4: SWL-01, RDB-05), generator rows that write repo files (SQ-INV-09, -13, -15, -16), rows already recorded
PASS under the same id (c1 triage), superseded rows, SQ-PWG-01 to -08 (held: measuring rows, need grids from later
port phases), and reruns that wait for an owner answer or an unbuilt fix (c4_triage.md part 2).

## 2. Tools

- `tools/scratch_query/prep_srv.sh`: new `COPYDEV=1` (dev jars copied, not hard-linked, while other sessions build)
  and `PGJAR=<path>` (copies the named Power Grid jar into srv/mods only).
- New probes: `pwg_probe.js` (`/zpwg ns|mods`), `c2_probe.js` (`/zc2`), `c3_probe.js` (`/zc3`), `zc4_probe.js`
  (`/zc4`), `zsneak_probe.js`; helpers `c2_famcheck.py`, `c2_legacy_cfg.py`, `cif_carry_check.py`, `rdb_compare.py`.
- Batches `tools/scratch_query/batches/r1113/*.qb`, results `perf/runs/r1113/<batch>/`.

## 3. Jars

| Jar | `mods/` | build/libs used | Rows |
|---|---|---|---|
| Creative Common | 11-12 18:55 | 11-12 20:46 | all |
| Creative Compat, Calendar, Brewing | 11-10 | 11-12 20:46 to 20:47 | all |
| Creative Delight | 11-10 21:20 | 11-11 16:52 | all |
| Creative Engineering, Devtools | 11-10 21:11, 21:23 | 11-10 23:06, 22:38 | all |
| Power Grid port | never in `mods/` | `jar_archive/pwg_gate/` by the name the orchestrator sends | SQ-PWG |

Scratch servers get COPIES of the build jars (`COPYDEV=1`).

## 4. Order

1. SQ-PWG gate rows whenever the orchestrator names them (next boot boundary).
2. Group A full pack: c1_a1, c2_a1, c3_a1 to a3, c4_a, rerun_a and rerun_a2.
3. Group A special sets: c2_l1 + l1b, c2_l2, c2_nobrew, c1_g1 + g2 (old save), c3_co + co2, c3_cc1, cc2, c3_ch1, ch2 +
   ch2b, ch3.
4. Group B: c3_b1 (+ c1_b1), c3_b2, c3_cdw1 to 3, c4_b with rerun_b first.
5. Client rows: none driven this run unless time allows (section 6).

Estimate: about 28 boots, 3 to 4 min boot each plus batches; 6 to 9 hours.

## 5. Row map

| Batch | Boot | Rows |
|---|---|---|
| pwg0.qb | A, level pwg0 | SQ-PWG-09, -10, -11 |
| c1_a1.qb | A | 19 (SALT2, SY, VTD8, STK, CM, MG, SNF, PS) |
| c1_g1 / g2 | A old-save pair | SQ-GLD-16 |
| c1_b1.qb | B | SQ-PST-12 |
| c2_a1.qb | A | 56 (PJL, CFS, STN, BBQS, CBW, BBQW, CMG, CEQ, CEN, CNS, DEP, LGM, ROLL, BBQ2) |
| c2_l1, l1b, l2, nobrew | A legacy config / no Brewing | CMG-02, CEN-02, CEN-05s, CFR-03, -04, DEP-02 |
| c3_a1 to a3 | A, restarts | 51 (DCM, PA, CMV, BPT, CCB, T1, MG, AZW, BOT, FCK, FCB, PG, IP, CIF-01) |
| c3_b1, b2 | B | 20 (ENGM, CBP, CDW, IDS, CIF, MIN-07, FCK-07/08, HSC-89) |
| c3_cdw1 to 3 | B minus one mod | CDW-01, -03 |
| c3_ch*, cc*, co* | reduced sets | ENGM-05, CDFO-02, -03, FCK-02b, MIN-01/03/06, CIF-04, -04b, CMV-02, -03 |
| c4_a.qb | A | 42 (FDI, GAP, BREED, NSM-01, CDNS-11, HCS, FLR, RDB) |
| c4_b.qb + rerun_b.qb | B | NSM-08a, -08, RD-10, GAP-03, HH-328 |
| rerun_a, a2 | A | WO-03/04/06/08/09/34/35t, HCS-05, HH-501, -1534, -1551a/b, -1555h |

## 6. After the run

Write-back per skill part B5; client rows and hand steps listed in section 8 for the owner.

## 7. Run notes

- 00:36 RUNNER.md claimed. No game JVM, 19 GB free. prep_srv.sh gained COPYDEV and PGJAR.
- 00:45 four subagents triaging chunk 1 to 4 into batches (done 01:00 to 01:10).
- 01:05 perf session r1113 open. pwg0 boot started on the build/libs 01:01 Power Grid jar (sha1 bfe387fa2d85).
  Orchestrator correction while booting: use `jar_archive/pwg_gate/backportedPowerGrid-0.6.2-ce-19.2-02.jar`
  (01:04, sha1 20b956052b3d); that boot is discarded and rebooted on -02 with SQ-PWG-11 added.
- 01:08 pwg0b boot (-02 jar, Done 9.9 s). First pass: SQ-PWG-09 BATCH-FAULT (my probe used `ModList`, blocked by
  the KubeJS class filter; the boot-wide bootreject caught the row's own `Unknown item 'powergrid:wire'` reply),
  SQ-PWG-11 probe error `redeclaration of var Graphed` (Rhino: `const` inside `try` in a nested function; changed to
  `let` in `pg_solver_probe.js`). Fixed, `/reload`, rerun 01:13: SQ-PWG-09, -10, -11 PASS (-11 prints I=-5 mA, source
  sign convention; judged on magnitude, asked the orchestrator to confirm the row text). Reported to main 01:16.
- 01:20 c1a1 boot, c1_a1.qb (19 rows). Judged by a subagent (`perf/runs/r1113/verdicts/c1a1.md`): PASS 17,
  BATCH-FAULT 1 (SALT2-08: the srv copies the instance's filled creativedelight-common.toml, carry skips present
  keys), INCONCLUSIVE 1 (SALT2-06, no end-state probe). Rerun batch `fix_c1a1.qb`.
- 01:29 c2a1 boot, c2_a1.qb (56 rows), done 01:44. Judged (`verdicts/c2a1.md`): PASS 41, FAIL 3 (CBW-03 pumpkin pie
  placed as Delightful's block on a cutting board; LGM-13 block-form food families empty; ROLL-12 two roll medley
  cans exist against ROLL-27, owner), INCONCLUSIVE 1 (ROLL-11: every eat gave 4 more food than the item), BATCH-FAULT
  11. Rerun batch `fix_c2a1.qb`.
- 01:40 orchestrator: Power Grid P2 jar `-03` (sha1 95f0de32f458). 01:45 to 02:09 pwg2 boots 1 to 3 (pwg2a/b/c.qb):
  SQ-PWG-12, -14, -18, -19, -20, -22, -23 PASS; -13 FAIL (performance get NPE before any solve: null
  lastMeasurement; works after a solve, 13b); -16 FAIL vs row text (about 0.047 ohm series resistance outside Rwires,
  likely the current gauge's own); -15 FAIL (probe `cut` box kills 4 wires); -17 INCONCLUSIVE (the gauge overheats
  before any wire; its wires dropped as items); -21 INCONCLUSIVE (resistor chunk stays at ticket level 32 next to the
  forced gauge chunk). Reported to main 02:09.
- 02:09 to 02:20 client cli_pwg (PG -03 jar in the client's mods only, sp copy of pwg2): CQ-PWG-08 PARTIAL PASS (no
  mixin or packet errors, P2 blocks and wires render; creative tab and hand steps left for the owner). CQ-PWG-06 not
  run: needs server + client together (one-JVM rule), owner row. Client killed by pid (own, throwaway world).
- 02:17 c3a1 boot, c3_a1.qb (46 rows) to 02:34. Judged (`verdicts/c3a1.md`): PASS 30, FAIL 3 (AZW-01 item_dict
  lists `hearthandharvest:sunflower_stem`, a block without an item; T1-04 vanilla golden tools and armour accept the
  rose gold ingot; DCM-02 `[absent mods]` skips 5 installed mods whose data namespace differs from the mod id),
  BATCH-FAULT 13 (bot aim `setYRot` missing on BotPlayer; `sed -i` config edits not seen by Forge's watcher; ids).
  Rerun batch `fix_c3a1.qb`; `zact_probe.js` gained `break ... keep`. c3_a2's portal rows have nothing to read (no
  pair placed) and wait for the fix boot.
- 02:30 orchestrator: P3 jar `-04` (sha1 74bb78effab4). 02:35 to 02:49 pwg3 boot (pwg3.qb): SQ-PWG-24, -13, -21,
  -15, -25 PASS; -16 PASS on physics (pass line counts the two voltage-gauge sense wires); -17 PASS with a sampling gap
  (overheated flag lives 5 ticks); -26 to -34 void: `pwg_p3_probe.js be` aborted on every gauge
  (`addToGoggleTooltip` reaches `Minecraft`, RuntimeDistCleaner ERROR, probe-made), `feget` and `drop` Rhino errors.
  Probe fixed; -26 to -34 rerun on -05 with P4.
- 02:45 orchestrator: P4 jar `-05` (sha1 d1f81955619d; P3 content unchanged). 02:59 pwg5 boot (pwg5.qb by a subagent:
  P3 -26 to -34 rerun, P4 -35 to -44; `pwg_p4_probe.js kin` got the same goggle and game-time guards).
- 03:17 pwg5 done; judged by a subagent (`verdicts/pwg5.md`): PASS 15, FAIL 3 (-32 device connector pops off the IE
  capacitor; -37 motor speed latched at 59 RPM on AC; -43 servo overheats at the row's 120 V), INCONCLUSIVE 7 (generator
  rows: `zpwg4 wind` Rhino overload). Reported to main 03:22.
- 03:10 orchestrator: P5 jar `-06` (sha1 81c35a983a38, ponder). 03:23 pwg6 boot (pwg6.qb): SQ-PWG-45 FAIL (nbt summary
  clean=27 quirk=6 bad=3: device_connector drops `axis_along_first`, electromagnet drops `facing`,
  generator/parallel_windings `winding.left=false` read as true), -16 PASS (Rpath), -35c PASS, -17 probe fault
  (`watch` getStringUUID), generator rows probe fault again (`Direction.getStepX`). Both probes fixed; qrun stopped
  between rows (SQ-PWG-41 discarded; a `pkill -f` also matched my own shell: gotcha known, avoid). 03:29 pwg6b fresh
  boot for -17 and the generator rows.
- 03:41 client cli_pwg5 on -06 CRASHED at resource load: `Failed to load description for particle powergrid:zap`
  (no `particles/zap.json` in -04 to -07). Reported to main 03:45; CQ-PWG-10, -13, -14 wait for a fixed jar.
- 03:34 pwg6/pwg6b judged (`verdicts/pwg6.md`): every row PASS except SQ-PWG-43 servo (batch ran the old 120 V text),
  SQ-PWG-45 PASS with the expected table wrong (3 upstream-same files). Reported to main 03:40.
- 03:45 c3a1 restarted for c3_a2.qb, then c3_a3.qb (configs from git 85104427, paintings off): SQ-PA-03, -04,
  SQ-CIF-01 PASS (CIF-01's last grep exits 1 on the expected 0: batch), SQ-PG-01b, SQ-IP-05b BATCH-FAULT (no pair from
  c3_a1). An API outage stopped this session 03:48 to about 08:50; the batch had finished on its own.
- 08:50 orchestrator: -08 (sha1 08533f728ab4, P6, zap.json added). 09:04 pwg8 boot (pwg8.qb by a subagent: SQ-PWG-46 to
  -54, reruns -32, -37 AC, -43 servo 40 V, -28, -30, -31, -36; -45 superseded by -47).
- 09:28 pwg8 judged (`verdicts/pwg8.md`): PASS 10 (incl. reruns -32, -37 AC, -43 servo, -28, -30, -31, -36), FAIL 2
  (-48 heater cooled by its own fan: upstream-same, row voltages changed; -54 solar bearing never assembles),
  BATCH-FAULT / INCONCLUSIVE 4 (-49, -51, -53; -50, -52 row values). Reported 09:32. A subagent fixed the P6 rows in the
  queue, `pwg_p6_probe.js` (FluidStack, emission, lastException) and wrote `pwg9p6.qb`.
- 09:32 to 10:04 client cli_pwg5 on -09 (sp copy of pwg8): no crash (zap fix holds), 0 mixin or ponder errors.
  CQ-PWG-14 PASS on the automatable part (36 scenes + index, 75 shots, English titles, no missing models); tour of 13
  P3/P4/P6 areas with Flywheel on and off: models and wires render (CQ-PWG-10/-13/-15/-16/-17 automatable part).
- 09:40 orchestrator: -10 (P8, sha1 c4624ce39cff) supersedes -09 for the server. 10:06 pwg10 boot: pwg10.qb (P7),
  pwg10p6.qb (P6 reruns), pwg10p8.qb (P8) in one boot.
- 10:06 to 10:26 pwg10: P7 rows void (`pwg_p7_probe.js` moveTo resolved to teleportTo), P6 reruns judged
  (`verdicts/pwg10p6.md`: -50, -52 PASS; -54 FAIL, the bearing's contraption entity disappears; -48, -49, -51, -53 batch
  faults with fixes listed), P8 void (`pwg_p8_probe.js` loaded `ModList`: class filter). Probes fixed; 10:26 rerun on
  the same world: SQ-PWG-62's second `zpwg5 nbtall` over the placed structures CRASHED the server 10:27:14
  (StackOverflowError, `WindingBlockEntity.addParallel` recursion; the recursion guard only returns when logs are
  enabled). Reported to main 10:33. 10:36 fresh world pwg10b (pwg10b.qb: P8 then P7).
- 10:36 to 10:58 pwg10b (fresh -10): judged (`verdicts/pwg10b.md`): P7 and P8 rows PASS apart from SQ-PWG-57 FAIL
  (battery charge lost into an AE2 item: upstream-same, fixed as B20 in -14), -63 BATCH-FAULT, row fixes for -60, -64,
  -65, -66 (made in the queue by a subagent). Probes: entity ids via getEncodeId (getKey gave minecraft:pig).
- 11:03 to 11:40 pwg12 (-12, B19 winding fix, P9a) and pwg12r (reruns with fixed probes): judged (`verdicts/pwg12.md`):
  B19 holds (SQ-PWG-71, B19N: windings placed twice, break and re-place, nbtall three times: no crash); SQ-PWG-54 PASS
  (the bearing builds a `create:stationary_contraption`; the old `contraptions 0` was the probe); -67 FAIL (circuit
  component tag 27 of 28); -63 INCONCLUSIVE (module lands in slot 3). Reported to main 11:48.
- 11:51 pwg15 boot (-15, P9c; pwg15.qb: P9b -72, -57 B20 rerun, -73 to -75, P9c -76 to -83). Orchestrator agreed:
  after the -15 server and client blocks, queue boots back to back; Power Grid only at boundaries.

## 8. Final verdicts

Per-boot verdict tables: `perf/runs/r1113/verdicts/` (c1a1, c2a1, c3a1 for the queue; pwg5, pwg6, pwg8, pwg10p6,
pwg10b, pwg12, pwg15, pwg16 for Power Grid; SQ-PWG-09 to -23 judged in section 7 and the messages to main).

Queue rows (124 judged): PASS 91, FAIL 6, BATCH-FAULT 25, INCONCLUSIVE 2.
- FAIL: SQ-AZW-01 (item_dict lists `hearthandharvest:sunflower_stem`, a block without an item); SQ-T1-04 (vanilla golden
  tools and armour accept the rose gold ingot); SQ-DCM-02 (`[absent mods]` skips 5 installed mods whose data namespace
  differs from the mod id); SQ-CBW-03 (pumpkin pie placed as Delightful's block on a cutting board); SQ-LGM-13
  (block-form food families empty); SQ-ROLL-12 (two roll medley cans exist against ROLL-27: owner).
- INCONCLUSIVE: SQ-ROLL-11 (every eat gave 4 more food than the item), SQ-SALT2-06 (no end-state probe).
- BATCH-FAULT rerun batches written, not run: `fix_c1a1.qb`, `fix_c2a1.qb`, `fix_c3a1.qb` (+ restart for SQ-PG-01b,
  SQ-IP-05b).

Power Grid (jars -02 to -16; every phase gate run as built): code findings reported to main and fixed in later jars:
missing `particles/zap.json` (client crash, -04 to -07), winding `addParallel` recursion StackOverflow (server crash,
-10; B19), battery charge lost into AE2 items (SQ-PWG-57; B20), performance counter NPE before a solve (B8). Open at
the stop: SQ-PWG-67 tag 27 of 28 (pack removes `create:copper_nugget`: pack-side decision), SQ-PWG-73 `zpwg9b cord`
"An unexpected error occurred" (probe now traces each step). Row-text fixes were made in the queue as they came up.
Most remaining non-PASS Power Grid rows are probe or batch faults with fixes in place (see section 10).

- 12:12 to about 12:40 pwg16 (-16, P9d): judged later (`verdicts/pwg16.md`): no confirmed Power Grid fault; most rows
  BATCH-FAULT (`ItemStack.is(TagKey)` Rhino ambiguity, the pack's `#forge:plates/copper` holds only the IE plate, mixers
  under vertical motors at speed 0, peaceful removed the witch). This session then stopped on a usage limit until 16:20;
  the pwg16 JVM was gone (no crash report). 16:21 resumed, judged pwg16, wrote `pwg17.qb` and `cq_pwg17.qb`.
- 16:50 owner: wind the run down for a fresh agent. No JVM running; no row half-run.

## 9. P11 Power Grid regression plan (written 2026-11-13 by the P11 prep agent)

Plan step P11.1 ([plan_power_grid.md](../plan_power_grid.md) P11): every runnable SQ-PWG row once more on the final
jar. **Target jar `jar_archive/pwg_gate/backportedPowerGrid-0.6.2-ce-19.2-17.jar`** (sha1 cbc2ddee37ca, port phase
10). If the orchestrator names a later fix build, the runner substitutes it (only SQ-PWG-P11A's version and count
lines change). Batches: `tools/scratch_query/batches/r1113pwg/` (headers hold prep, areas and run lines). Results:
`perf/runs/r1113/pwgr/` and `perf/runs/r1113/pwgperf/`.

Every row block is copied unchanged from the latest r1113 batch that ran it with fixed probes (the source is named
above each block), apart from the SQ-PWG-68 regex and the SQ-PWG-06 thyratron heater (6.5 V) from `verdicts/pwg12.md`
and the level-name paths in shell steps. `pwg_p8_probe.js` now calls `BlockState.use` directly for the modular display:
Create 0.5.1's value box handler takes every fake-player click and replays it at the block centre, so all clicks went
to slot 3 (SQ-PWG-63, not a port fault).

| Boot | World | Batch | Rows | Est. time |
|---|---|---|---|---|
| R1 | pwgr (new; group A superflat, `spawn-animals=true`) | `pwgr1.qb`, then the runner's P10 gate batch with `--rows` SQ-PWG-91 to -96 (not -90) | SQ-PWG-P11A (boot check, all probe checks, the world's one `nbtall` pass), P2 -13, -14, -16, -17, -18a, -19, -20a, -21, -23; P3 -26 to -34; P4 -37, -37cs, -38, -38B, -39 to -44 (with -42s, -44pre); P6 -48 to -54; P7 -56 to -61; P8 -63 to -66; P9a -06, -68 to -71; P9b to P9d -73 to -75, -77 to -83, -85 to -89; P10 -91 to -96 | boot 4 min, rows about 75 min |
| R2 | pwgr, restart (`overheatedDropChance 0.0`) | `pwgr2.qb` | -20 (save and reload), -18b, -15 (cut runs after the reload reads) | 5 min |
| R3 | pwgr, restart (`overheatedDropChance 1.0`, `clientMirror = false`) | `pwgr3.qb` | -22, -18c | 5 min |
| P1 | pwgperf (new; group A superflat; **perf session with JFR**) | `pwgperf.qb` | SQ-PWG-01 to -05, -07 (rebuilds -38 A and B), -08 (server tick only) | boot 4 min, rows about 45 min |
| C1 | throwaway client on R1's world (sp copy), the `-17` jar in the client's mods only | runner's (ponder pass as `cq_pwg14.qb` over the scene lists of CQ-PWG-14, -15, -20, -22, -29; tour as `cq_pwg_tour.qb` over every area) | automatable parts of CQ-PWG-08, -10, -13 to -17, -19, -20, -22, -26 to -29 (models, renderers, 81 ponder scenes, JEI tabs) | 40 min |

**Totals:** 5 boots (2 worlds plus 2 restarts) and one client: 68 + 3 + 2 + 7 = 80 server rows plus 6 P10 rows, about
2.3 hours of server and client time, about 3 hours with judging. Areas (R1 header): P2 X 990-1060 Z 990-1090; P3 and P4
X 1090-1140 Z 990-1260; ponder structures X 1290-1970 Z 1290-1320 (unloaded after the pass); P6 X 1085-1125 Z
1385-1520; P7 X 1095-1115 Z 1525-1590; X 1290-1320 Z 1495-1680 for -54, -60, -63 to -66; P9a X 1090-1165 Z 1670-1800;
P9b to P9d Z 1805-1990; P10 X 1095-1135 Z 1995-2085. No block overlaps; perf grids have their own world and each perf
row unloads the previous grid, so the earlier networks pause.

**Superseded or obsolete (not in any boot):**

| Rows | Why |
|---|---|
| SQ-PWG-09, -10, -11 | P0 / P1 gates; registry and config covered by SQ-PWG-P11A, the solver in the game's class loader by every solving row |
| SQ-PWG-12, -24, -35, -46, -55, -62, -67, -72, -76, -84 | per-phase boot rows (version and counts of their own jar); SQ-PWG-P11A checks -17 and runs every probe's `check` |
| SQ-PWG-25, -36 | per-phase recipe counts and config greps; SQ-PWG-P11A (116 recipes, config grep) |
| SQ-PWG-45, -47 | ponder structure checks, replaced by the 79-file pass in SQ-PWG-P11A (SQ-PWG-90's command) |
| SQ-PWG-90 | the P10 gate row; on -17 its gate run counts, and in R1 SQ-PWG-P11A runs its checks and the structure pass |
| SQ-PWG-35b, -35c | `zpwg4 check` in SQ-PWG-P11A |
| SQ-PWG-B19N | a second `nbtall` over placed structures; one placement pass per world; B19 stays covered by SQ-PWG-71 |
| SQ-PWG-13b, -14r, -21b, P2 reruns from pwg2b | replaced by the pwg3 / pwg6 / pwg6b versions in R1 |
| SQ-PWG-06 and -07 in the measuring section | moved rows (pointers only); -06 runs in R1, -07 in P1 |
| SQ-PWG-08, per-bot network part and its two config reruns | perfBot bots have no client, so they never negotiate sync; the network comparison is owner work (CQ-PWG-01 to -04) |

**Judge against the queue text** for rows whose last verdict asked for a row change not yet in the copied block:
SQ-PWG-49 basin (consumer placed last), -53 open patch, -66 board (build order), -69 (100 ohm load per loop), -70
(overheat cap 185). The pwg16 batch (P9b to P9d, -63) failed on a probe Rhino ambiguity (`ItemStack.is(TagKey)`); R1
needs the runner's fixed `pwg_p9b_probe.js` and `pwg_p9c_probe.js` first.

**Owner work after this run:** every CQ-PWG row part that needs hands or a dedicated server with a client is in
[power_grid.md](../power_grid.md) section 8.1 (owner hand list). D14 and the H7 measurement read the P1 numbers and
the owner's CQ-PWG-01 to -04 (energy_advanced.md section 8).


## 10. Resume point (2026-11-13, wound down at the owner's request)

**Done:** queue group A boots c1_a1, c2_a1, c3_a1 to a3 (judged, section 8); every Power Grid gate from P0 to P9d on
jars -02 to -16 (verdict files in `perf/runs/r1113/verdicts/`); one -09 client (ponder pass CQ-PWG-14, world tour).

**Jar rule:** Power Grid jars only from `jar_archive/pwg_gate/` by exact name; target **`backportedPowerGrid-0.6.2-ce-19.2-17.jar`**
(sha1 cbc2ddee37ca, port phase 10). Copy it into the scratch `OUT/srv/mods` (prep_srv.sh `PGJAR=<path>`) or the throwaway
client's mods only; **never into `mods/`**. Boot with `COPYDEV=1`. Copy all `tools/scratch_query/pwg_*.js` as `zz_*.js`.

**Next, in order:**
1. -17 client gate boot: `eval_client.py setup`, -17 jar into the client's mods, sp copy of a pwg17 world (after step
   2) or any scratch world, `cq_pwg17.qb` (CQ-PWG-24, -26, -22, -27, -28, -29 automatable parts; JEI and right-click
   steps are `look` lines for the owner). Running it after step 2 gives it the P10 builds to tour.
2. -17 server gate boot: `tools/scratch_query/batches/r1113/pwg17.qb` (prep in its header): P10 rows SQ-PWG-90, -91,
   -95, -92, -93, -94, -96, then the reruns of the pwg16 BATCH-FAULT / INCONCLUSIVE rows (-84, -72, -73, -76, -78 to
   -83, -85, -88, -89) and SQ-PWG-63 (recopy `zz_pwg_p8_probe.js`: it now calls `BlockState.use` itself). Send main
   the SQ-PWG-73 cord step lines.
3. Regular queue boots, back to back (Power Grid only at boundaries, unless a jar fixes a crash or a blocked client row):
   `c4_a.qb` (copy SoL-Carrot jar into srv/mods only), `rerun_a.qb` + `rerun_a2.qb` (restart), `fix_c1a1.qb`,
   `fix_c2a1.qb`, `fix_c3a1.qb` (+ a restart for SQ-PG-01b / SQ-IP-05b), `c2_l1.qb` + `c2_l1b.qb`, `c2_l2.qb`,
   `c2_nobrew.qb`, `c1_g1.qb` + `c1_g2.qb` (old-save pair), `c3_co.qb` + `c3_co2.qb`, `c3_cc1.qb`, `c3_cc2.qb`,
   `c3_ch1.qb`, `c3_ch2.qb` + `c3_ch2b.qb`, `c3_ch3.qb`, then group B: `c3_b1.qb` (+ `c1_b1.qb`), `c3_b2.qb`,
   `c3_cdw1` to `3.qb`, `rerun_b.qb` then `c4_b.qb` in one boot. Setup for each is in its header; triage of every chunk
   row in `c1_triage.md` to `c4_triage.md`.
4. P11 regression (section 9): R1, R2, R3, P1 perf (perf session, report the D14 numbers), C1.

**Pending fixes before those batches:**
- `ItemStack.is(TagKey)` in `pwg_p9b_probe.js` / `pwg_p9c_probe.js`: done (Forge tag manager helpers); verify on boot 2.
- P10 gate batch: written (`pwg17.qb`); R1 runs its -91 to -96 with `--rows`.
- Row fixes for -49 (motor and gearbox first, mixer last), -53 (`time set 6000`, MID_SUMMER / MID_WINTER), -66 (motor
  and shaft before the display board), -69 (100 ohm load per loop), -70 (185 C cap) are NOT yet copied into
  `tools/scratch_query/batches/r1113pwg/pwgr1.qb` (the plan copied older blocks); take them from `pwg12.qb`,
  `pwg12r.qb`, `verdicts/pwg12.md` and the queue row text.
- `c3_a3.qb` SQ-CIF-01 last shell step should be `! grep -qE ...` (grep exits 1 on the expected 0).
- SQ-PWG-90 expects `files=57` (probe carries 57 structures) against the queue's 79.

**Owner-only (hand or real player):** CQ-PWG-05, -06 (client and server together), -11, -12, -18, -21, -23, -25; the hand
steps of CQ-PWG-08, -10, -13, -15 to -17, -19, -20, -22, -24, -26 to -29; all other CQ- rows of the queue; the client
halves listed per chunk in `c1_triage.md` to `c4_triage.md`; SQ-ROLL-12 / ROLL-27 can decision; SQ-HH-474 (r1112).

## 11. Continuation run notes (session main, from 17:25)

Resumed from section 10 by the main VS Code session (no orchestrator). Perf session `r1113b` (r1113 was closed).
Order: section 10 step 2 (pwg17 server), step 1 (cq_pwg17 client), step 3 queue boots, step 4 P11 if time allows.

- 17:31 pwg17 prep (COPYDEV=1, -17 jar sha1 cbc2ddee37ca, 13 probes, 79 ponder nbt, protectionDuration 1); boot Done
  in 11 s, scratch pid 4019268. Running pwg17.qb.
- 17:41 pwg17.qb done: PASS 7, FAIL 10, LOOK 10. First FAILs are probe faults (SQ-PWG-90 regex "phase 10)" vs the
  logged "phase 10.0)"; zpwg10 `setTag` on a KubeJS ItemStack; zpwg9b `closeContainer` missing on FakePlayer). Judging and
  probe fixes handed to a subagent (verdicts/pwg17.md, rerun batch pwg17r.qb); server stopped, world kept in the
  scratchpad for the cq_pwg17 tour.
- 17:48 FINDING: `backportedPowerGrid-0.6.2-ce-19.2-17.jar` is in the instance `mods/` (no mod_sides.md row;
  config/powergrid-*.toml written 12:26, so a game ran with it). Not removed (not the runner's). The first c4_a boot
  carried it; stopped, unlinked from srv/mods, fresh world, rebooted Done 10.4 s. prep_srv.sh now drops any Power Grid
  jar unless PGJAR names one.
- 17:52 c4_a.qb running (SoL Carrot jar in srv/mods only).
- 17:54 c4_a CRASH 17:51:58 in SQ-GAP-01: zc4_probe cooktime called getType() on every recipe; Mechanical Extruder's ExtrudingRecipe throws AbstractMethodError (Rhino cannot catch it). Probe fixed (serializer key); results moved to c4_a_crash1; fresh world, rerun from the top.
- 17:57 pwg17 judged by subagent (`perf/runs/r1113/verdicts/pwg17.md`): with qrun's 7 PASS, PASS 19, FAIL 1, BATCH-FAULT 6
  (-90, -95, -73, -80, -81, -82), INCONCLUSIVE 1 (-94: ceiling connector T0=0 with 1.2 A through it). Real FAIL SQ-PWG-91:
  ceiling tile attachments drop the tile OR the attachment (upstream loot tables `ceiling_tile_{lamp,connector,junction,
  solar}.json`: one pool, rolls 1, two entries). Mixer and arm need a meshing cogwheel (new p9c `cogdrive`). Probes fixed
  (p3, p9b, p9c, p10); rerun batch `pwg17r.qb` needs a fresh world pwg17r.
- 18:26 c4_a.qb done: PASS 13, FAIL 15, LOOK 14 (no crash). Judging handed to a subagent (verdicts/c4_a.md). Server stopped, world kept.
- 18:28 rerun_a boot (level r1113ra, crow stealChance 1.0 / carryTicks 200) Done 9.4 s; rerun_a.qb running.
- 18:30 c4_a judged by subagent (`verdicts/c4_a.md`): with qrun's 13 PASS, PASS 31, FAIL 2, BATCH-FAULT 8, INCONCLUSIVE 1
  (RD-11, cotton trade chance). Main fault: zc4 split its argument as a Java String (fixed). Real FAILs: SQ-GAP-04 (golden
  pickaxe and chestplate accept rose gold; `rose_gold_interchange.js` gold-only list built from global.item keys, vanilla
  golden gear has none); SQ-RDB-02 (the SQ-BREED-02 removals from #minecraft:flowers / small_flowers cascade:
  creativeworldgen ground_match/plants and template_trim/plants lose 19 wild crops, creativecompat physics/floats 78 and
  flower_box_plants 74 Botania flowers). Owner notes: sneak-placed vegetable omelet gives Display Delight's
  bnc_vegetable_omelet; golden egg sinks; rose gold block not in #forge:storage_blocks/gold. Rerun batch c4_ar.qb (same
  world).
- 18:43 rerun_a.qb: PASS 5, FAIL 4 (WO-06, HCS-05, HH-501, WO-34), LOOK 4; rerun_a2.qb (restart, hopsRequireFarmland false in srv only): FAIL 2 (HH-1551b, -1555h). Judging handed to a subagent (verdicts/rerun_a.md).
- 18:48 rerun_a / a2 judged by subagent (`verdicts/rerun_a.md`): PASS 11 (WO-03, -04, -06, -08, -08b, -09, -35t, HCS-05,
  HH-1534, -1551a, RRA-00), FAIL 1 (SQ-HH-501: theft works, nest stays empty; CrowSeekShinyGoal nearbyNest searches +-3
  vertically, crow is 5.6 above, flee back-off outlasts carryTicks; TODO #68), BATCH-FAULT 2 (HH-1551b, -1555h: no
  player online on the restart, no random ticks), INCONCLUSIVE 1 (WO-34: inotify instances 140 of 128, owner must raise
  fs.inotify.max_user_instances). Rerun batch rerun_ar.qb (level r1113ra, carryTicks 1200, hops key still false).
- 19:07 c4_ar.qb (c4a world): PASS 2, FAIL 5, LOOK 4; judging handed to a subagent. rerun_ar boot (ra world, carryTicks 1200, hops key false) started.
- 19:14 c4_ar judged (`verdicts/c4_a.md` section c4_ar): HCS-07, HCS-12 PASS; FLR-03, -04 BATCH-FAULT (undrained millstone
  deletes overflow output; wheel count box too low); FLR-05 unheated PASS, heated BATCH-FAULT (empty burner); BREED-03
  isfood PASS, pollination INCONCLUSIVE; FDI-02 hearts PASS (row's max health 20 is stale: Raspberry Pie +2 at 5 foods),
  count INCONCLUSIVE; FDI-03 partial. SQ-GAP-04 real FAIL WIDER: every golden tool, armor piece and the golden apple take
  rose gold; they DO have dict keys, so the `not: { output: goldOnly }` filter in rose_gold_interchange.js is the fault.
  Next rerun batch c4_ar2.qb (same world).
- 19:15 rerun_ar (ra world, carryTicks 1200, hops key false): SQ-HH-1551b PASS, -1555h PASS (count 6), SQ-WO-06r
  INCONCLUSIVE (storm sleep seen, wake not: timed sleep to 76725 outlasted the poll), SQ-HH-501d: with the default
  1200-tick carry the crow DOES put the gold in the nest -> the SQ-HH-501 FAIL is the scratch carryTicks 200 only (TODO #68
  answer).
- 19:19 fix_c1a1: SQ-SALT2-08 PASS (server part: 19 values carried, `.moved`, [salt] 0.5, no salt-common.toml
  recreated; AppleSkin and /reload halves not covered), SQ-SALT2-06 FAIL again with end-state reads (only Nether ice
  melts; TODO creativedelight/members #56). Perf session r1113b stopped and reported (`perf/sessions/r1113b/summary.md`).
  Paused for a new session (context), all JVMs stopped, RUNNER.md idle.

## 12. Resume point (2026-11-13 19:22, continuation paused)

**Done since section 10:** pwg17 (judged), c4_a, rerun_a, rerun_a2, c4_ar, rerun_ar, fix_c1a1 (all judged; section 11).

**Rules found:** the -17 Power Grid jar sits in the instance `mods/` (owner's or another session's; no mod_sides.md row):
prep_srv.sh now drops it unless PGJAR names a jar. Never call `getType()` on recipes in a probe loop (Mechanical
Extruder AbstractMethodError kills the server). `fs.inotify.max_user_instances` is exhausted (140 of 128): SQ-WO-34
waits for the owner to raise it.

**Next, in order:**
1. `fix_c2a1.qb`, `fix_c3a1.qb` (+ restart for SQ-PG-01b / SQ-IP-05b), setup in each header.
2. `c4_ar2.qb` on a reboot of the c4a world (scratchpad c4a is session-local: if gone, prep a fresh r1113c4a and run
   c4_a.qb's setup rows first; see its header).
3. `pwg17r.qb` (fresh world pwg17r, PGJAR -17, probes recopied; setup in the subagent notes of section 11 and the batch
   header), then the -17 client gate `cq_pwg17.qb` (needs a world with SHOW17 built: pwg17r runs SETUP17 + SHOW17? check
   the header; else run pwg17.qb's SHOW17 rows on it).
4. Section 10 step 3 from `c2_l1.qb` on: c2_l1 + l1b, c2_l2, c2_nobrew, c1_g1 + g2, c3_co + co2, c3_cc1, cc2, c3_ch1,
   ch2 + ch2b, ch3, group B (c3_b1 + c1_b1, c3_b2, c3_cdw1 to 3, rerun_b + c4_b).
5. P11 regression (section 9). Pending batch fixes listed in section 10 still apply (pwgr1.qb row fixes, c3_a3 SQ-CIF-01
   grep, SQ-PWG-90 files=57).
