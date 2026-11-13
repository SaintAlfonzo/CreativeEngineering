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

(filled at the end)
