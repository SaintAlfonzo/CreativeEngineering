# Scratch run 2026-11-16 (r1116)

Owner request 2026-11-15 23:55: "act as an orchestrator agent for the scratch test queue. debug errors as they come up
that dont affect behavior". Main session (f4343884) as orchestrator; each phase runs in one runner subagent (one at a
time, one game JVM at a time). Runners append to sections 7 and 8; the orchestrator keeps sections 1, 4 and 9 current.
Fixes that change behavior are findings (section 9), not made in the run.

## 0. Performance capture

Perf session `r1116`, opened by the orchestrator before the first boot; left open between phases; closed and reported
(no `--prune`) after the last phase.

## 1. Scope

Taken: every queue section added since r1115 that has code, in this order:
1. SQ-TX (SQ-TX-01 to -18) + SQ-TXQ-01 to -03 (qrun lint, crash watch, relaunch). The `/cdevtest` verbs go first:
   later phases may use them.
2. SQ-PS15B-01 to -12, SQ-PTH-25c (pass-through rework) + SQ-PTH-06b (not superseded by PS15B).
3. SQ-PS15 group A rows with code: -01, -02, -03, -05, -06, -07, -09, -10, -13, -14, -15, -16.
4. SQ-PS15 group B rows: -11, -11b, -12, -12b, -12c (seed 8675309).
5. SQ-PTH-28c (perf suite P, pass-through).
6. Client: CQ-TXC-01 to -10, CQ-TXR-01, CQ-PS15-01 to -03, CQ-PS15B-01, -02, CQ-PTH-03b.

Not taken: SQ-PS15-04 (waits on the owner), SQ-PS15-08 (waits on the frozen decor report), CQ-TXP-01, -02 (owner go
for the one-JVM carve-out), SQ-PTH-03b, -11c, -25b, -28b (superseded by SQ-PS15B / SQ-PTH-25c, -28c).

## 2. Tools

prep_srv.sh, qrun.py (lint, run, judge, table), `/cdevtest` and `/pbot` verbs, run_captures.py serve for clients,
perf_suite.py for SQ-PTH-28c. Batches `tools/scratch_query/batches/r1116/`, results `perf/runs/r1116/<phase>/`.

## 3. Jars

| Jar | `mods/` | build/libs | Rows |
|---|---|---|---|
| creativecommon-1.2.0 | 2026-11-15 23:28 | 2026-11-15 23:28 (same) | all |
| creativecompat-1.0.0 | 2026-11-14 23:54 | 2026-11-15 23:28 (swapped in) | PS15B, PTH, PS15-10, TX-17/-18 |
| creativedevtools-1.0.0 (bundle, with cdevperfbot) | 2026-11-15 00:07 | 2026-11-15 15:50 (swapped in; own jar 23:56 has the same classes) | TX, all bot rows |
| creativedelight-1.1.0 | 2026-11-15 15:34 | 2026-11-15 15:34 (same; own 23:56 same classes) | PS15 salt, hops, lunchbox |
| creativeworldgen-1.0.0 | 2026-11-14 22:40 | 2026-11-15 23:57 (swapped in) | PS15-16, -11 |
| backportedPowerGrid | -18 | `jar_archive/pwg_gate/` -19 | PS15-14 (row names its jar) |

Builds carry other sessions' uncommitted edits (Compat, Devtools, Delight).

## 4. Order

| Step | Setup | Run | Estimate |
|---|---|---|---|
| 1 | group A `tx`, then crash/relaunch boots | SQ-TX, SQ-TXQ | 1 h |
| 2 | group A, restarts for toml switches | SQ-PS15B, SQ-PTH-06b, -25c | 1.5 h |
| 3 | group A | SQ-PS15 A rows | 1 h |
| 4 | group B seed 8675309 | SQ-PS15 B rows | 1 h |
| 5 | perf suite P | SQ-PTH-28c | 0.5 h |
| 6 | capture runner client | client rows | 1.5 h |

## 5. Row map

Filled by each runner (queue section, rows, batch file).

| Phase | Queue section | Rows | Batch (`tools/scratch_query/batches/r1116/`) | Results (`perf/runs/r1116/`) |
|---|---|---|---|---|
| 1 | SQ-TX Scratch testing tools, item 3 | SQ-TX-00 (setup), -01 to -18 | `tx.qb` | `p1/tx` |
| 1 | same, reruns after batch and Devtools fixes | -01, -05, -06, -07, -08, -12, -14 (`r` suffix) | `tx_rerun.qb` | `p1/tx_rerun` |
| 1 | same, second boot without Compat | -18b (not runnable, see 7) | `tx18b.qb` | none |
| 1 | SQ-TXQ / CQ-TXP | SQ-TXQ-01 (judged from `p1/tx`, plus an offline lint echo check) | `tx.qb` | `p1/tx` |
| 1 | same | SQ-TXQ-02 (+ unrun guard row -02z) | `txq02.qb` | `p1/txq02` (before the qrun fix), `p1/txq02b` |
| 1 | same | SQ-TXQ-03, -03b | `txq03.qb` | `p1/txq03` |
| 2 | SQ-PS15B / CQ-PS15B Pass-through rework | SQ-PS15B-01..-12 (-12a..-12h), SQ-PTH-25c, -25c2 | `ps15b_a.qb` (generator `tools/scratch_query/ps15b_gen.py`) | `p2/a` |
| 2 | same, boot 2 (`breakStallTicks = 0`) | -09b, -08b, -02b, -02c, -04r, -01r, -10r, -12h2, -12e2, SQ-PTH-06c | `ps15b_b.qb` | `p2/b` |
| 2 | SQ-PTH Pass-through scratch fixes | SQ-PTH-06b (boot 1 half in `ps15b_a.qb`), -06r2 (boot 2 tail), -06r3 + SQ-PS15B-12e3 (boot 3) | `ps15b_c.qb`, `ps15b_d.qb` | `p2/c`, `p2/d` |
| 3 | SQ-PS15 / CQ-PS15 Post-scratch r1115 follow-ups (group A) | discovery (not a row) | `ps15_disc.qb` | `p3/disc` |
| 3 | same, boot 1 (VanishChance 1.0, lick interval 200, lick chances 1.0) | SQ-PS15-09, -01, -02, -03, -05, -06, -07, -10, -13, -16 | `ps15_a.qb` (generator `tools/scratch_query/ps15_gen.py`) | `p3/a` |
| 3 | same, boot 1 tail (Power Grid -19) | SQ-PS15-15, -14 (prereqs SQ-PWG-P11B, SETUP9, -76, SETUP15 from r1115/p2_r1.qb) | `ps15_pwg.qb` | `p3/pwg` |
| 3 | same, boot 2 (SaltItemMeltingEnabled false, VanishChance 0.0) | SQ-PS15-02b, -03b | `ps15_b.qb` | `p3/b` |
| 3 | same, boot 3 (fresh world p3c, upgrade_aquatic + alexsabnormalities out, VanishChance 0.05, Rare Ice insert off) | SQ-PS15-16b, -03c, -01r, -07r | `ps15_c.qb` | `p3/c` |

## 6. After the run

Run log entry in the queue, writeup test lists, TODO rows for FAILs, gotchas, perf report, RUNNER.md idle.

## 7. Run notes

- 23:55 orchestrator preflight: RUNNER.md idle, claimed 23:58; game_jvms.sh empty; 67 GB free. Bundle jars of
  Devtools and Delight looked older than their `-own` jars; `:creativeDevtools: :creativeDelight: :creativeWorldgen:
  build` rerun: Gradle leaves a bundle untouched when its inputs hash the same (class CRCs match), so the old mtime is
  not a stale bundle.

### Phase 1 (TX, runner-1)

- 00:00 batches written in `tools/scratch_query/batches/r1116/`: `tx.qb` (SQ-TX-00 setup, -01 to -18), `tx18b.qb`
  (-18 second boot), `txq02.qb`, `txq03.qb`; all lint 0 E, 0 W. prep_srv `p1tx` (group A, level tx): swaps Brewing
  2026-11-14 23:29, Compat 2026-11-15 23:56 (rebuilt after the 23:28 listed in 3; newest build), Devtools bundle 15:50,
  Worldgen 23:57; only cdevperfbot unlisted in mod_sides.md (expected). game_jvms.sh empty.
- 00:00 boot 1 `p1tx`, pid 1277143; Done (10.2 s) 00:01. qrun `tx.qb` -> `perf/runs/r1116/p1/tx` started 00:01.
- 00:07 `tx.qb` end: PASS 7, FAIL 8, LOOK 4. Judged: -01 BATCH-FAULT (my regex wanted `states` after `tickwarp`;
  the verb list is right); -05 BATCH-FAULT (sand schedules its own fall 2 ticks after `setblock`, gone before the
  blocktick at pace 2); -06, -07 FAIL `ticks=2399` / `5999` for 2400 / 6000: Devtools tool bug, the warp counted the
  command's own (normal-speed) tick as warped; -08 BATCH-FAULT (the 40-tick watch ended before the bread went in)
  plus a tool bug: t=0 sampled twice (once when added, once in the same tick's END); -11 xp=10 / 9, not 0: 1.19.2
  drops the orbs inside `die()`, so the queue's `xp=0` is wrong, row PASS otherwise (beef/leather, cooked beef with
  fire); -12 BATCH-FAULT (2 s pace recharges the weak attack to strength=1; the respawn hit also has strength=1, so
  count 3); -14 BATCH-FAULT for the eat half: group A is peaceful, food refilled 10 -> 20 before the eat
  (`result=FAIL`); bow half PASS. -09 LOOK -> PASS (all values equal /zq; awkward + spider eye gives poison, vanilla,
  the queue's "awkward or nothing" was a guess; no pack script removes it). -13, -15, -16 PASS. -17 PASS
  (in_tag 44, d1_out 13 incl. 5 Copycats layers, 1 `class=new` = vintagedelight:salt, D1 question for the owner;
  dump copied to `kubejs/exported/`). -18 first boot PASS (`[cdevreload] compat: creativecompat-common.toml reloaded`).
- 00:08 boot 1 stopped (game_jvms.sh empty). Devtools fix (test tool only, no gameplay): `TestTicks.java` tickwarp
  counts from the tick after the command; watch skips the t=0 resample in the same tick. `:creativeDevtools:build
  --offline` 00:08, not installed.
- 00:09 boot 2 `p1tx2` (fresh group A world, Devtools 00:08 swapped in), pid 1294388; Done 9.9 s. `tx_rerun.qb`
  (-01, -05, -06, -07, -08, -12, -14 with batch fixes, setup row rebuilds chest, field, golem) -> `tx_rerun`.
- 00:13 boot 3 `p1nocompat` (SKIPSWAP=creativecompat, Compat jar removed) for SQ-TX-18 part 2: FML refused at once
  (wait_boot MISSING): Compat is a mandatory dependency of Engineering, Equipment, Worldgen, Brewing, Calendar,
  Delight, Magic AND Devtools (`creativeDevtools` mods.toml `creativecompat mandatory=true`). The row's premise
  (Devtools loads without Compat, `CompatReloadCommand` outside the `isLoaded` guard) cannot be met in any server:
  SQ-TX-18 INCONCLUSIVE, finding in 9. pid 1301883 killed (mine, sat idle after the refusal).
- 00:14 boot 4 `p1tx2` (pid 1304395), SQ-TXQ-02: kill -9 at 00:15:23 from a background shell; qrun judged CRASH,
  evidence copied, "no --relaunch given", guard row -02z not run, but only at 00:16:05 (end of `sleep 60`): the crash
  watch ran before each step and inside waitlog only. Tool fix (tools only): `qrun.py` `sleep` now checks the watch
  every second; docstring updated.
- 00:17 boot 5 `p1tx2` (pid 1311832), SQ-TXQ-02 rerun -> `p1/txq02b`: kill 00:18:09, row CRASH and batch end
  00:18:11 (2 s). PASS.
- 00:19 boot 6 `p1tx2` (pid 1315461), SQ-TXQ-03 with `--relaunch 'bash tools/creative_labels_test/scratch_server.sh
  boot OUT'`: kill 00:19:46, CRASH, "relaunch 1: relaunched (pid 1317665)", -03b PASS, `crash/relaunch_1.out`
  written. PASS. 00:21 pid 1317665 stopped; game_jvms.sh empty.
- 00:12 `tx_rerun.qb` end: PASS 5, LOOK 3 -> all PASS (tickwarp `ticks=2400` / `6000` with the fix; watch t=0..40
  five lines with bread from t=10; weak attack strength=0.04; eat CONSUME use_ticks=32, finish 32 ticks later food
  15 sat 6).
- 00:22 SQ-TXQ-01: lint prints `lint: 1 batch(es), N error(s), M warning(s)`, run prints the same E lines first
  (checked offline with a scratchpad batch holding a bad regex), `verdicts/<ID>.json` has checks / looks / verdict,
  `qrun.py table` matches `results.md`. PASS.
- 00:23 gotchas.md: new section "Found in r1116 phase 1" (peaceful food refill, pace 0 for setblock + blocktick and
  short watches, weak attack charge, kill xp, no boot without Compat).
- Phase 1 done 00:23: 21 rows, PASS 20, INCONCLUSIVE 1 (SQ-TX-18), FAIL 0 after reruns. No jar installed; Devtools
  build 00:08 in `build/libs` only.

### Phase 2 (PS15B, runner-2)

- 00:24 runner-2 preflight: game_jvms.sh empty, 67 GB free. Compat build/libs is now 2026-11-16 00:08 (886838 bytes;
  rebuilt with the phase 1 Devtools build, not the 23:56 listed in 1), Devtools bundle 00:08 (505849). Probe
  `tools/scratch_query/zpth_probe.js` gets an `attrs NAME|TAG` verb (every movement-speed modifier; SQ-PS15B-11).
  Generator `tools/scratch_query/ps15b_gen.py` (from pth_gen_p12.py) writes `batches/r1116/ps15b_a.qb` (boot 1:
  setup, SQ-PS15B-09, -01, -02, -03, -04, -07, -06, -10, -11, -08, SQ-PTH-06b, SQ-PS15B-05, -12a..-12h, SQ-PTH-25c,
  -25c2, save) and `ps15b_b.qb` (boot 2 with `breakStallTicks = 0`: -09b, SQ-PTH-06c, -08b, -02b). Lint 0 E, 12 W
  (all `cfgedit`: the -12 rows edit the live toml on purpose). Live-toml saves by rename (`sed -i`, or a copy + `qrun.py
  toml` + `mv` for the section-scoped `surroundedAt`), the single in-place write with python `r+`.
- 00:30 `COPYDEV=1 prep_srv.sh $S/p2a p2a A difficulty=normal spawn-animals=true spawn-npcs=true spawn-monsters=true`:
  swaps Brewing 2026-11-14 23:29, Compat 00:08, Devtools 00:08, Worldgen 23:57 (copies); only cdevperfbot unlisted.
  zz_zpth_probe.js copied in; srv toml `[debug.passthrough] timing = true` (qrun.py toml) before the boot; the toml is
  the instance's r1115-model file (RING, denseFrom 6, leaf_pile), so boot 1 is SQ-PS15B-09's first boot.
- 00:30 boot 1 `p2a`, pid 1356825, Done 9.96 s 00:31:32; migration line once. qrun `ps15b_a.qb` -> `perf/runs/r1116/p2/a`.
- 00:31 to 00:45 `ps15b_a.qb` (21 rows incl. setup and save): judged below. Boot 1 findings: lone ground web from a
  20-block drop gives the full plain damage (the zombie enters the cell and lands in the same tick); sand layers and
  Supplementaries fodder never slow (1 to 5 layers `mod=none`); the in-place single write of the toml was not applied
  (SQ-PS15B-12e); `tick_web_cube` 1222 ns. Snow 6 and 8 at (1118 / 1124, -60, 1100) had turned to air before -12h ran
  (zombies at y -60; unexplained, no error in the log): -12h redone on fresh snow in boot 2. Batch faults: the 996.2
  snowball came after the 300-tick watch ended; boot 1 jump / sneak sampled every 5 ticks (rerun per tick).
- 00:45 boot 1 stopped (game_jvms.sh empty). srv toml `breakStallTicks = 0` (qrun.py toml); generator adds to
  `ps15b_b.qb`: -02c (996.2), -04r (per-tick jump / sneak), -01r (drop heights -40, -40.3, -40.6, -41, -45, -35 into
  lone ground webs), -10r (ash 2 / 5 reread, fresh snow 6 / 7 / 8), -12h2, -12e2 (same-length `os.write`).
- 00:47 boot 2, pid 1375290, Done 4.1 s; `ps15b_b.qb` -> `p2/b`. -09b no migration line. SQ-PTH-06c BATCH-FAULT: the
  boot 1 items and orbs had reached their vanilla 5 minutes (summoned 00:40, read 00:48), as in r1115. -02c count
  BATCH-FAULT (watchnew re-tagged the still-hanging -02b snowball); the 996.2 snowball itself broke at the centre.
- 00:51 `ps15b_c.qb` on boot 2 (fresh items i7a / i7c, save), stop; 00:53 boot 3, pid 1382349, `ps15b_d.qb` -> `p2/d`:
  -06r3 PASS; -12e3 (in-place `os.write` as the first save of the boot, no rename before it) not applied in 30 s either.
- 00:54 boot 3 stopped, game_jvms.sh empty. All toml switches lived in `$S/p2a/srv` only; no jar installed, no
  code changed outside `tools/` (zpth_probe.js `attrs` verb, ps15b_gen.py).

### Phase 3 (PS15, runner-3)

- 01:01 runner-3 preflight: game_jvms.sh empty, 66 GB free. `COPYDEV=1 PGJAR=jar_archive/pwg_gate/backportedPowerGrid-0.6.2-ce-19.2-19.jar
  prep_srv.sh $S/p3a p3a A difficulty=easy spawn-animals=true spawn-npcs=true`: swaps Compat 00:08, Devtools 00:08,
  Worldgen 23:57 (copies); Common 1.2.0 and CD 1.1.0 as in `mods/` (CD has `LunchboxFoodOnlyMixin`,
  `VineCropNoReplaceMixin`, `SaltLayerMelt`, nested Salt -04); Power Grid -19 (sha1 22d3affb7747) in srv/mods only;
  unlisted only cdevperfbot and the gate jar. Probes zmelt, zp9 and the 15 pwg probes copied as zz_*.js. Srv toml:
  CD `[salt.Melting] SaltLayerVanishChance 1.0` (the key is absent from the instance `config/` file: Forge adds it on
  the next game start; `configs_edited/` has it), hearth `[salt] saltLickInterval 200, saltAnimalStageChance 1.0,
  saltPlayerLickChance 1.0`, loading protection 1 s.
- 01:03 boot 1 `p3a`, pid 1414433, Done 9.8 s. 01:05 `ps15_disc.qb` (vine default states, ids, Salt Melt recipe id
  `creativedelight:shapeless/salt_melt`). Generator `tools/scratch_query/ps15_gen.py` writes `ps15_a/b/c.qb`; all
  lint 0 E 0 W; `ps15_pwg.qb` (prereq rows by qb_extract from r1115/p2_r1.qb, version expect -19) 0 E, 1 W (sync
  `zpwg9c basin` waitlog repeat, harmless).
- 01:07 to 01:13 `ps15_a.qb`. **Rare Ice takes every right click on `minecraft:ice`** (`config/rare-ice.properties`
  `allowInsertingItemsToIce=true`, Rare Ice's own Forge-bus listener): the salt dust click (SQ-PS15-01) and the Salt
  Melt click (SQ-PS15-07) on plain ice turned the ice into `rare-ice:rare_ice` with the item inside (dust 4 -> 3, no
  layer, no melt; the Salt Melt itself went into the ice). Packed ice, blue ice, snow layers, snow block, powder snow
  and every dispenser path are unaffected. FAIL for -01 and -07 as the player sees it; the salt code itself rerun on
  boot 3 with the insert switched off in the srv config only (-01r, -07r PASS).
- SQ-PS15-03 layouts (generator docstring): L1 = the row text (a 1-layer salt layer ON the ice), L2 = Salt Block beside
  the ice, L3 = a 1-layer salt layer beside the ice; ice in 1-deep holes so melt water cannot flow; only the ice cell
  gets `randomtick`. L1 cannot show the vanish chance: when the ice turns to water the layer above loses support and
  pops as salt dust in the same `setBlock`, before the listener rolls (20 of 20 drops at 1.0, 0.0 and 0.05). L3 shows
  it: 1.0 all 20 vanish in the melt tick with no drop, 0.0 all 20 stay, 0.05 20 of 20 stay (p = 0.36 for 0 losses).
- 01:13 to 01:16 `ps15_pwg.qb` on boot 1: SQ-PS15-15 PASS (unheated: acid 250 + water 750, no items); SQ-PS15-14:
  the arm feeds 1 part per grab (circuit 2/3 locked @4.498, 3/3 by the next read, every loose part on the belt x1), but
  a wire connector the arm dropped on the belt before the circuit reached the window rode to the end depot first and
  holds it, so the finished board waits at the belt end (@9.75) and later loose parts queue behind it. FAIL.
- 01:16 boot 1 stopped (game_jvms.sh empty). Srv toml `SaltItemMeltingEnabled false`, `SaltLayerVanishChance 0.0`;
  01:18 boot 2, pid 1433348, Done 4.1 s; `ps15_b.qb` -> `p3/b`: -02b, -03b PASS. Stopped 01:19.
- 01:20 boot 3: upgrade_aquatic-1.19.2-5.1.0 and alexsabnormalities-1.3 moved to `$S/p3a/mods_out/`, fresh world
  `p3c` (the p3a world holds UA blocks), `SaltLayerVanishChance 0.05`, `SaltItemMeltingEnabled true`, srv
  `config/rare-ice.properties` `allowInsertingItemsToIce=false`. pid 1439360, Done 10.6 s; `ps15_c.qb` -> `p3/c`.
  SQ-PS15-16b: the 7 moved worldgen files are quiet and `upgrade_aquatic` is in the off list, but 11 ERROR lines from
  the pack's own `kubejs/data/immersive_weathering/block_growths/ua_*_coral.json` (Unknown registry key
  `upgrade_aquatic:*_coral`): FAIL per the row's failure column (next files to guard). Also WARN lines only: the
  fishing-net loot table names `creativedelight:mod_items/upgrade_aquatic` (allowed by SQ-DEP-09b), a moonlight-style
  config `types.*.upgrade_aquatic` corrected to default (srv copy only).
- 01:24 boot 3 stopped; game_jvms.sh empty. No jar installed, no code changed outside `tools/` (`ps15_gen.py`, batches);
  all config switches lived in `$S/p3a/srv` only. SQ-PS15-16's river walk (mulberry trees, driftwood) needs a world
  with rivers: not run on the flat world.

### Phase 4 (PS15 worldgen, runner-4)

- 01:31 runner-4 preflight: game_jvms.sh empty, 66 GB free. New census probe `tools/scratch_query/zcen_probe.js`
  (`/zcen scan`: chunk-center surface biome, crop rule tag `creativedelight:wild_crops/KEY` membership, block counts,
  chunks and patches (blocks within 6 horizontally) per id, in-tag chunks and patches; `/zcen grid`: a configured
  feature placed as `/place feature` at 5 x 5 points 16 apart, at a canopy column's ground, one above its leaves, or
  a fixed y, placed blocks counted per point). Batch `ps15_wg.qb` (one boot, lint 0 E; W only: synchronous waitlog
  repeats, locate origins unloaded by design). `COPYDEV=1 prep_srv.sh $S/p4b p4b B difficulty=normal` (seed 8675309):
  swaps Common 00:08, CD 15:34, Brewing 2026-11-14 23:29, Compat 00:08, Devtools 00:08, Worldgen 23:57 (copies); unlisted
  only cdevperfbot; probe as `zz_zcen.js`. CD jar 15:34 holds C3 (`TrunkVineFeature` source 15:32).
- Order choice: the hops census (-11) runs before the 25 hops placements of -11b in site 2, so placed vines never
  enter the census; -11b's census figure is -11's. Every `/zcen grid` runs after its box's census.
- 01:32 boot `p4b` (pid 1476322), Done 80.4 s at 01:33. About 01:45 the auto-mode safety classifier stopped giving
  verdicts (9 Bash calls in a row); runner-4 handed back before qrun started. No phase 4 row ran.
- 17:21 STOPPED by the owner. Orchestrator stopped pid 1476322 (`scratch_server.sh stop`), game_jvms.sh empty, perf
  session r1116 closed and reported. Phases 4 to 6 deferred (section 8).

## 8. Final verdicts

### Phase 1

| ID | Result | Evidence |
|---|---|---|
| SQ-TX-01 | PASS | rerun -01r: help lists every verb, bogus/unloaded/missing arg give `error=` + `done=0`, no trace (first run BATCH-FAULT: regex order) |
| SQ-TX-02 | PASS | chest placed `facing=north,type=single,waterlogged=false`, state `be=minecraft:chest shape_top=0.875 biome=minecraft:plains`, be nbt, leaves placed |
| SQ-TX-03 | PASS | `slot=0 item=minecraft:apple*5`, `via=item_handler slots=27 filled=1` x2, `tanks=0`, `energy=none` |
| SQ-TX-04 | PASS | ages `n=256 mean=0.000 hist=0:256`; randomtick `block_ticks=5120 changed=`>100 `equiv_game_ticks=27306`; mean >1; count `on=minecraft:farmland:256` |
| SQ-TX-05 | PASS | rerun -05r (setblock + blocktick in one tick): `before=minecraft:sand after=minecraft:air`, sand at -60 (first run BATCH-FAULT: sand fell by itself) |
| SQ-TX-06 | PASS | rerun -06r after the Devtools fix: `ticks=2400 asked=2400 tps=1530.6`, game time 805 -> 3254, status `running=false` (first run FAIL `ticks=2399`) |
| SQ-TX-07 | PASS | rerun -07r after the fix: `ticks=6000 asked=6000`, wheat mean 0.000 -> 0.578 with bot T near (first run FAIL `ticks=5999`, mean 0.719) |
| SQ-TX-08 | PASS | rerun -08r after the fix: t=0, 10, 20, 30, 40 once each, bread `1:minecraft:bread*2` from t=10, `end=done t=40` (first run: watch over before the bread, t=0 twice) |
| SQ-TX-09 | PASS | recipes 1 = zq 1; food 5 / 0.6 both; logs 91 = 91; states 749140 both; rdetail `part=results`; bytype 504; attrs n=2; sugar -> swiftness; spider eye -> poison (vanilla; queue text "awkward or nothing" wrong) |
| SQ-TX-10 | PASS | break `who=fake broken=true` oak_log -> air, drops oak_log:1; hoe use -> `farmland[moisture=0]`; loot `oak_leaves:10`; useair CONSUME/SUCCESS |
| SQ-TX-11 | PASS | `n=5 killed=5` x2; beef 11 + leather 6; cooked_beef 12 + leather 3 with fire; xp=10 / 9, not 0: orbs drop inside die() in 1.19.2 (queue text wrong) |
| SQ-TX-12 | PASS | rerun -12r (pace 0): protection line; hits 100->94 and respawn 94->88 at strength=1; weak `strength=0.04` (no damage, golem hurt cooldown) |
| SQ-TX-13 | PASS | `health=20 invulnerable=true`; vulnerable on -> `health=18.3333 invulnerable=false`; interact iron ingot `result=CONSUME held_after=empty` |
| SQ-TX-14 | PASS | rerun -14r (difficulty easy for the row; group A peaceful refills food): eat `CONSUME using=true use_ticks=32 food=10`, finish +32 ticks `food=15 sat=6` = bread 5 / 6; bow `release_after=20`, `end=released`, 1 arrow |
| SQ-TX-15 | PASS | `shift=true pose=CROUCHING`; `yaw=90 pitch=30`; break `broken=true`; punch before = after grass_block |
| SQ-TX-16 | PASS | walk `ticks=138 speed_bps=4.2435`; sprint 5.4483; wall `end=stuck`; pickup off: no apple in inv, items `minecraft:apple:1` |
| SQ-TX-17 | PASS | `done=1`, in_tag 44, d1_out 13 (7 coin stacks, logic_cabinet, 5 Copycats layers), `tag_without_property=0`, snow `collision_px=1:0,...,8:14`; `class=new` vintagedelight:salt -> owner D1; dump copied to `kubejs/exported/` |
| SQ-TX-18 | INCONCLUSIVE | boot 1 PASS (`[cdevreload] compat: creativecompat-common.toml reloaded`); boot without Compat impossible: FML refuses, Compat mandatory for 8 own jars incl. Devtools (9) |
| SQ-TXQ-01 | PASS | lint and run print the same E line; `verdicts/*.json` with checks/looks/verdict; `qrun.py table` = `results.md` |
| SQ-TXQ-02 | PASS | `p1/txq02b`: CRASH "JVM pid 1311832 gone with no crash file" 2 s after the kill, `crash/SQ-TXQ-02/logs_latest.log`, "no --relaunch given", next row not run (needed the qrun sleep fix; first try ended only after the 60 s sleep) |
| SQ-TXQ-03 | PASS | CRASH, "relaunch 1: relaunched (pid 1317665)", -03b PASS, `crash/relaunch_1.out` |

Counts: PASS 20, INCONCLUSIVE 1, FAIL 0 (first pass: 2 Devtools tool FAILs and 1 qrun tool fault, fixed; 6 batch faults rerun).

### Phase 2

Results `perf/runs/r1116/p2/a` (boot 1), `b` (boot 2), `c` (boot 2 tail), `d` (boot 3). Server: group A,
difficulty normal, Compat and Devtools 2026-11-16 00:08 build jars (copies). Mob rows on base-speed-0, 100 hp zombies.

| ID | Result | Evidence |
|---|---|---|
| SQ-PS15B-01 | FAIL | plain 16 dmg; lone web 5 up: fall 14.55 -> 12.37 on entering (x0.85), 15 dmg; 3 stacked 10 (x0.63); 3-face web 0 (fall 0, `mod=-0.650`); ripe bush 16. **Lone web at ground level from -40: 16 = plain**: the zombie goes from y -58.77 (fall 18.77) to -60 in one move and lands before the web factor applies. -01r: drops whose tick ends inside the cell scale (18.77 -> 15.96, 13 dmg, x0.81); from -40 and -45 (lands the tick it enters) none. Suspect `PassThrough` fall factor applied after `Entity.move`'s landing (`fallResetClip` / `InsideBlocksMixin` order) |
| SQ-PS15B-02 | PASS | 995.5: snowball 1.44 -> 0.50 -> 0.17 ... under 0.05 for 3 ticks, gone at z 1000.68 (edge cell); pearl gone, bot at z 1000.70; 994.5 and 996.2 (-02c) gone at the center (z 1001.6); boot 2 `breakStallTicks = 0` (-02b): hangs at z 1000.76 for 300 ticks, sinking 0.002 b/t. -02c count BATCH-FAULT (old snowball in the box) |
| SQ-PS15B-03 | PASS | middle row all 5 `-0.500`, corner `-0.250`, strip middle row and strip edge `-0.500`; bot standing middle -0.5, corner -0.25; bot walk x 1002 (5 wheat in 12 blocks) 2.55 b/s vs grass 4.14 |
| SQ-PS15B-04 | PASS | cross `-0.650`; cube center zombie at y -59.000 for 60 ticks (hangs); open-above center zombie -58.9 -> -60 in about 15 ticks (falls, not hanging); bot (-04r per tick): hang -59.000 steady, jump climbs 0.070 b/t, sneak sinks 0.0525 b/t |
| SQ-PS15B-05 | PASS | timing lines during the 60 s with 6 cows in the 7x7 ripe field: `counts=` 13, 2, 5, 4, 1, 3 per 200 ticks (about 17350 hook calls each); right-click harvest of row z 1153 (`cdevtest use` -> `wheat[age=0]`, ages hist 0:7) and an age-0 replant row: `counts=0` x3. Create harvester pass not run |
| SQ-PS15B-06 | PASS | edge cell `-0.500`; break of its inner neighbour: `-0.250` at the next read (0.3 s); seeds planted (`wheat[age=0]`): `-0.500` at once |
| SQ-PS15B-07 | PASS | ripe with 3 age-0 neighbors `-0.500` (dense); seedling `mod=none` |
| SQ-PS15B-08 | PASS | border x 1007 / 1008: wheat interior -0.5 / -0.5, corners -0.25 / -0.25, web cube bottom faces -0.65 / -0.65, center hangs; bot crossings 2.66 / 2.65 b/s, no "moved wrongly"; after the restart (-08b) identical values and speeds |
| SQ-PS15B-09 | PASS | boot 1 on the instance toml: "pass-through tables moved to the 2026-11-15 model: dense_crop neighbors RING -> SIDES; ... cobweb neighbors CUBE -> FACES; ... leaf_pile removed"; toml SIDES 1, FACES 1, RING/CUBE 0, denseFrom 3 x10, surroundedAt 6 x10, layers table, no leaf_pile; boot 2 (-09b) no line |
| SQ-PS15B-10 | FAIL | snow, IW oak leaf pile: 1 none, 2 -0.125, 3 -0.25, 4 -0.375, 5 -0.5, 6-8 none; Supplementaries ash same (2 and 5 reread in -10r); bamboo 2 -0.25, 3 -0.5, 4 none; sawdust 2 -0.1 ... 6 -0.5, 7-9 none; bot walk 5-layer snow 2.13 b/s, 3-layer 3.16, grass 4.17. **IW sand layer and Supplementaries fodder: 1 to 8 all `mod=none`**; both have collision = outline (2 px a layer), so the zombie stands on the top face (y -59.75 at 2 layers) and is never inside; both also carry `creativecompat:sinking` and a BlockRunner surface tag. Suspect `PassThrough.layerWalk` cell / shape test (`PassRule.layerWalk`) |
| SQ-PS15B-11 | PASS | snow 3 / 4 / 5: one modifier only, pass-through `a1386f64-...` -0.25 / -0.375 / -0.5 (no BlockRunner modifier); sand 3 / 4 / 5: only BlockRunner `23237052-... "Block speed boost" -0.015 ADDITION` (0.085), no pass-through modifier (the -10 sand defect); grass: none |
| SQ-PS15B-12 | FAIL | by rename + `/cdevreload compat` (`[cdevreload] compat: creativecompat-common.toml reloaded` each time): timing false 0 lines in 25 s, true lines again; cobweb denseWalk 0.2 -0.65 -> -0.80; breakStallTicks 0 snowball hangs, back to 3 it breaks; surroundedAt 3 open-above-center zombie hangs; layerMaxSlow 0.3 snow 3 -0.35, 5 -0.70; solidBelowFull 0 (-12h2 on fresh snow) 6 -0.583, 7 -0.70, 8 none; restored values none. **In-place save without the command never applied**: -12e (python write), -12e2 (one `os.write`, same length), -12e3 (same, first save of boot 3): `mod` unchanged at 3, 10 and 30 s. Server only (no client for the re-sent table) |
| SQ-PTH-25c | FAIL | two runs, 12 cases, tagged=true, enabled=true: ring_count 107 / 93, ring_count_edge 88 / 87, cube_count 136 / 128, tick_air 125 / 119, tick_lone_crop 444 / 432, tick_field 493 / 486, tick_field_shared 361 / 361, tick_sparse 365 / 360, **tick_web_cube 1222 / 1224** (budget 1000; r1115 2029), tick_web_cube_shared 353 / 368, tick_horse_field 1482 / 1475, item_roll 114 / 113 ns median. Record in blockrunner.md 3.10 and perf_suite.md 12 at write-back |
| SQ-PTH-06b | PASS | boot 1: lone web (i6a) and cube (i6c) items and orbs at identical positions 30 s apart, ng=true; web broken (i6b): t=1 still held, t=2 ng=false, falling from t=3 (one tick after the break, not the same tick); restart half (-06r2 / -06r3, fresh items): same positions after the restart and for 10 s, ng=true. First restart try (-06c) BATCH-FAULT: boot 1 items despawned at their vanilla 5 min |

Counts: PASS 10, FAIL 4 (SQ-PS15B-01, -10, -12, SQ-PTH-25c), INCONCLUSIVE 0; batch faults rerun in the same phase: 4
(-02 996.2 watch, -02c count, -04 5-tick sampling, -06c despawn).

### Phase 3

Results `perf/runs/r1116/p3/a` (boot 1), `pwg` (boot 1 tail), `b` (boot 2), `c` (boot 3). Group A flat, difficulty
easy, Compat / Devtools 00:08 and Worldgen 23:57 build jars (copies), Power Grid -19 in srv/mods only.

| ID | Result | Evidence |
|---|---|---|
| SQ-PS15-01 | FAIL | packed ice, snow 1 and 3: layer placed on top, target kept, dust 4 -> 3. **Plain ice: Rare Ice took the click** (ice -> `rare-ice:rare_ice` holding the dust, no layer, nothing melts). Salt logic itself PASS (-01r, Rare Ice insert off: 3 of 3 layers on plain ice, ice kept; ticks: water, the layer pops for lack of support). Suspect Rare Ice `allowInsertingItemsToIce` (config) vs salt dust's own place |
| SQ-PS15-02 | PASS | air over grass: `salt[layers=1]`; ice -> water; packed -> ice; snow layer -> air; salt layer 3 -> 4; dust 3 in each; air over air and stone: dust dropped. Switch off (-02b): ice kept twice, dust dropped (2 items), dispensers 3 |
| SQ-PS15-03 | PASS | L2 Salt Blocks 20 of 20 kept at 1.0, 0.0, 0.05; L3 (layer beside): 1.0 0 of 20 left, no drops; 0.0 20 left; 0.05 20 left (p 0.36). Row layout L1 (layer on the ice): every layer pops as dust when its ice melts (support loss before the listener), at any chance |
| SQ-PS15-05 | PASS | cow at interval 200, chance 1.0: layer 2 -> 1 (t 8632) -> air (8792), lick `salt:salt_block` until then, then lightly (9032); stack: lick 1 removes the 1-layer block, lick 2 makes the 8-layer 7, lick stays `salt:salt_block` |
| SQ-PS15-06 | PASS | hand and dispenser heavily -> well (dust 3); layer of 2 above heavily -> lightly, layer gone; waxed heavily by dispenser -> waxed well; full lick by hand and by dispenser: `salt[layers=1]` on top, dust 3; no item entities |
| SQ-PS15-07 | FAIL | recipe 2 salt dust + 2 raw rock salt, max damage 64, name "Salt Melt" (lang), model `salt:item/salt`, JEI script present (page itself client). **First click on plain ice put the Salt Melt into a Rare Ice block** (hand empty after). Dispenser: ice -> water `Damage:1`, stone kept `Damage:0`. -07r (insert off): Damage 1..5 for ice, packed, blue, snow block (air), powder snow (air), stone none, use 64 breaks it, use 65 nothing, dispenser PASS |
| SQ-PS15-09 | PASS | `[melting] tags: 6 meltable, 2 slow, 39 salt, 122 fire` (r1115 123), only magma source-driven, no `glowstone_cage_lantern` in the tag or the log |
| SQ-PS15-10 | PASS | tag n=6; hops, red jungle grape, vanilla vine, mulberry vine: stick (`supplementaries:stick`), torch and dirt each went beside the vine (z 381), vine state unchanged 12 of 12; hops on the hops vine added `east=true`; no mixin error |
| SQ-PS15-13 | PASS | server half: Copper Lunchbox handler refuses pumpkin pie, FD apple pie, stick (`notInserted=1`, box empty), takes cooked beef; netherite box refuses the pie; no `LunchboxFoodOnly` line in the log. Screen and tooltip `Food slots: 9` are client (not run) |
| SQ-PS15-14 | FAIL | -19: parts go in one at a time (Assembly 2/3 @4.498 lockedExternally, board 3/3 next read; every loose part x1), depots wire 8 -> 3, resistor 8 -> 4, stone x64. **End depot holds a `wire_connector x1` that rode ahead of the circuit; the board waits at the belt end @9.75** and loose parts queue behind it. Suspect the upstream belt fallback in `ArmInteractionPointMixin` (parts deposited on the belt when no circuit is in the window) |
| SQ-PS15-15 | PASS | unheated basin (stone below): `powergrid:acid 250`, water 750, no items; heated, water first: water gone, flesh and eye left, no acid (recorded, not a FAIL); recipes `creativeengineering:createmixing/powergrid_acid` and `..._in_water` |
| SQ-PS15-16 | FAIL | boot 1 PASS (`upgrade_aquatic` in the on list; `place feature upgrade_aquatic:river_tree`: trunk `minecraft:oak_log` x4, leaves `upgrade_aquatic:river_leaves` x17). Boot without UA (-16b): Done, off list `[upgrade_aquatic]`, no worldgen error, **but 11 ERROR lines from `kubejs/data/immersive_weathering/block_growths/ua_*_coral.json`** (Unknown registry key `upgrade_aquatic:*_coral`). River walk not run (flat world) |

Counts: PASS 8, FAIL 4 (SQ-PS15-01, -07: Rare Ice conflict on plain ice; -14; -16), INCONCLUSIVE 0. Extra sub-rows
-01r, -02b, -03b, -03c, -07r all PASS; prerequisite rows SQ-PWG-P11B, -76 PASS (setup).

### Phases 4 to 6

Stopped by the owner 2026-11-16 17:21; no row ran. Resume: `prep_srv.sh` group B seed 8675309 as in phase 4 notes, then
`qrun.py run OUT batches/r1116/ps15_wg.qb perf/runs/r1116/p4/wg` (probe `zcen_probe.js` not yet live-tested).

| ID | Result | Evidence |
|---|---|---|
| SQ-PS15-11, -11b, -12, -12b, -12c | deferred | phase 4 server up, batch not started (classifier outage, then stop) |
| SQ-PS15-16 river walk | deferred | not run |
| SQ-PTH-28c | deferred | phase 5 not started |
| CQ-TXC-01 to -10, CQ-TXR-01, CQ-PS15-01 to -03, CQ-PS15B-01, -02, CQ-PTH-03b | deferred | phase 6 not started |

## 9. Findings routing

| Finding | Row | Routing |
|---|---|---|
| Devtools mods.toml has `creativecompat mandatory=true` (and 7 other jars need Compat), so the no-Compat half of SQ-TX-18 cannot boot | SQ-TX-18 | owner: make Compat optional in Devtools (packaging), or drop the row's second half |
| `vintagedelight:salt` layers 1-8 dump as `class=new`; 5 Copycats layer blocks as `d1_out`, row expected `new` | SQ-TX-17 | owner D1 question (plan_post_scratch_1115b.md D1) |
| Queue Pass text wrong: SQ-TX-11 `xp=0` (1.19.2 drops orbs on death, xp=10/9); SQ-TX-09 awkward + spider eye = poison (vanilla) | SQ-TX-11, -09 | queue rows rewritten at write-back |
| Ground-level lone web: a 20-block drop that enters and lands in one tick takes full damage (16); fall factor applied after the landing | SQ-PS15B-01 | pass-through owner (Compat `PassThrough.java` fallResetClip / `InsideBlocksMixin` order); behavior fix |
| IW sand layer and Supplementaries fodder never slow (collision = outline, entity stands on top, never inside) | SQ-PS15B-10, -11 | pass-through owner (`PassThrough.layerWalk` / `PassRule.layerWalk` cell test) |
| In-place toml save not picked up in 30 s (3 tries); rename + `/cdevreload compat` works for every switch | SQ-PS15B-12 | owner: is a watcher reload expected? suspect Forge watcher / ModernFix, not `CompatConfigReload` |
| `tick_web_cube` 1222 ns over the 1000 ns budget (r1115 2029); cold case may time a recount | SQ-PTH-25c | pass-through owner; check `PassBenchCommand` cold-case cache clear first |
| Minor: held item released 1 tick after its web breaks (row PASS); snow 6 and 8 at 1118/1124 -60 1100 vanished on boot 1, no log | SQ-PTH-06b, SQ-PS15B-10 | note for the pass-through owner |
| SQ-PS15B-05 Create harvester half not run | SQ-PS15B-05 | stays in the queue |
| Rare Ice (`allowInsertingItemsToIce=true`) swallows every right click on plain ice: salt dust and Salt Melt never act on `minecraft:ice` (pass with the insert off) | SQ-PS15-01, -07 | owner: Rare Ice config or event priority vs salt dust / `SaltMeltItem` |
| Salt layer on top of ice pops as dust when the ice melts (support lost before the vanish listener), so the vanish chance never shows in that layout; beside ice it works | SQ-PS15-03 | Creative Delight salt melting owner; queue row layout too |
| `SaltLayerVanishChance` missing from the instance `config/creativedelight-common.toml` (present in configs_edited; Forge adds it on start) | SQ-PS15-03 | none needed |
| Arm: a loose wire_connector placed on the belt before the circuit takes the end depot; finished board waits at 9.75 | SQ-PS15-14 | Power Grid port owner (`ArmInteractionPointMixin` belt fallback) |
| Without Upgrade Aquatic: 11 ERROR lines from `kubejs/data/immersive_weathering/block_growths/ua_*_coral.json` (unknown `upgrade_aquatic:*_coral`) | SQ-PS15-16 | needs the same guard as the D5 worldgen move |
| Client halves open: SQ-PS15-13 screen/tooltip, SQ-PS15-07 JEI page/icon | SQ-PS15-13, -07 | client phase or queue |
