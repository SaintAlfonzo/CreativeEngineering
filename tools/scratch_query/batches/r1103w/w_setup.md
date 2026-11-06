# r1102b worldgen slice: boot setup

Batch writer: worldgen slice of run r1102b (scope.txt lines 46, 48, 67, 68, 70, 79). One JVM at a time:
`game_jvms.sh` empty before every boot, `scratch_server.sh stop`, wait for it to be empty again. `INST` = instance root,
`SCR` = the run's scratchpad. **One RES for every w_ batch**: square centres and locate results are captures carried
in `RES/vars.json` from W1 into W1b, W2, W3. Cross-boot files (census jsons, W1 log and world path, entity censuses)
go to `$INST/perf/runs/r1103w/w/`. All shell steps run in `OUT/srv`.

Jars: the instance `mods/` holds patcherMod 1.1.07 and Creative Calendar 1.0.0 of 2026-11-02 14:08 (same files as
build/libs), CD 14:01; prep_srv.sh swaps nothing then. Check the times again right before each boot (gotchas: parallel
installs). JFR: set `CE_PERF=0` on every prep (or `JFR_MAX=200m`); a 1 GB recording per boot is most of the disk.

## Gate before any boot

`bash tools/test_worldgen_tools.sh` (SQ-EFB-00 / -17, also row 1 of w_1_B.qb). At 14:50 today it does not compile:
`EarthFillTest.java:424` declares `basin` twice (the waterReach checks at line 160 added a second one), and
`BiomeLayoutCacheTest` / `MassPlacerTest` import `worldgen.WorldgenFiles`, which now lives in
`creativeengineering.util`. The row says nothing boots until it passes; the failures are in the test code, not the
jar, so the owner decides: fix the tests first, or run the live rows and record EFB-00 / -17 as FAIL (harness).

## Order, disk, time

| Boot | Batch | World (level, seed) | Rows | Disk | Time |
|---|---|---|---|---|---|
| W1 | w_1_B.qb | `wge`, B, 8675309 | 39 queue rows in 90 batch rows (gen and sub-rows) | about 4 GB world + 0.3 GB logs | 4.5 to 5.5 h |
| W1b | w_2_B.qb | `wge` restarted | SQ-WGE-02a, -06d (5 sub-rows) | + few MB | 10 min |
| W2 | w_3_B.qb | `efb6`, B, 8675309, waterReach 6 | SQ-EFB-19, -22, SQ-WGE-06d-x + gen / census | 0.6 GB | 25 min |
| W3 | w_4_B.qb | `efboff`, B, 8675309, earthFill off, timeout 5 | SQ-EFB-16, SQ-WGE-02c | 0.25 GB | 15 min |
| A1-A4 | w_5_A ... w_8_A | `wga`, A (superflat) | SQ-PSW-01, SQ-FTW-05, -06, SQ-IP-15, -16, SQ-WGE-06a, -06b, -06c | 0.15 GB | 4 boots, 20 min |
| F1 | w_9_B.qb | `ftw_on`, B, -8188900049893920259 | SQ-FTW-02 (on), SQ-TIE-01 | 0.6 GB | 20 min |
| F2 | w_10_B.qb | `ftw_off`, B, same seed, firstTickWater off | SQ-FTW-02 (off) | 0.15 GB | 8 min |
| G1 | w_11_B.qb | `cdj5c`, B, 8675309, spawn-animals | SQ-FGD-12m, -15c | 0.25 GB | 20 min (the mushroom locate can take 15) |

Measured base: 90 KB per generated overworld chunk (r1101 w2 world). W1 pregenerates about 35,700 overworld chunks
(PL and HI r 400, ten r 300 squares, deep dark r 200, spawn r 1000), 1,444 Nether and 5,625 End chunks. Everything
kept to the end: about 6.5 GB plus srv copies (120 MB each) and JFR. Free now: 24 GB. Delete W3, A, F1, F2, G1 worlds
after their rows; keep W1 until w_3_B.qb's SQ-WGE-06d-x has run (it reads the W1 world). Total about 6.5 h.

W1 is long; resume with `qrun.py run OUT_W1 batches/r1103w/w_1_B.qb RES --from <ID>`. Cut points: the W1-GEN rows
(about 1.5 h of Chunky), the probe rows (about 75 min, 147 tiles), the censuses (about 45 min). Short on time: drop
`W1-GEN-SP` (spawn r 1000, the longest single step; then SQ-PSW-04 judges only the squares and the deep dark one) and
the two census 128 rows (SQ-WGE-04b-1, -04b).

## W1: w_1_B.qb (group B, fresh)

```
CE_PERF=0 bash $INST/tools/scratch_query/prep_srv.sh $SCR/w1 wge B
T=$SCR/w1/srv/config/creativeworldgen-common.toml
python3 $INST/tools/scratch_query/qrun.py toml $T worldgen.earth_fill log '"ALL"'
python3 $INST/tools/scratch_query/qrun.py toml $T worldgen.meta_structures log true
grep -n 'frequency = 60\|forceSpawnInVillages = true' $SCR/w1/srv/config/waystones-common.toml   # carried (scratch_server.sh copies all of config/)
mkdir -p $INST/perf/runs/r1103w/w
boot; wait for 'Done ('; qrun.py run $SCR/w1 batches/r1103w/w_1_B.qb RES
```
Notes: SQ-EFB-20 (last row) runs `/reload` (drops Chunky) and saves `w1_latest.log` and `w1_srv_path`.
SQ-WGE-01b-S runs `/dumpclimate` (about 5 min on the server thread) and then copies the instance climate samples back
into srv/ so no later load re-solves on the new dump. `/place structure` rows (EFB-07, -08, -15) pick spots relative
to square centres; a "cannot place" answer means pick a spot by hand (LOOK). Probe tiles force-load at most 256 chunks
each and only inside the generated square.

## W1b: w_2_B.qb (W1 world, restart)

```
stop; game_jvms.sh empty
sed -i '/^\s*searchTimeoutSeconds\s*=/d' $SCR/w1/srv/config/creativeworldgen-common.toml     # SQ-WGE-02a
boot; qrun.py run $SCR/w1 batches/r1103w/w_2_B.qb RES
```

## W2: w_3_B.qb (group B, fresh, waterReach 6)

```
stop W1; game_jvms.sh empty
CE_PERF=0 bash $INST/tools/scratch_query/prep_srv.sh $SCR/w2 efb6 B
T=$SCR/w2/srv/config/creativeworldgen-common.toml
python3 $INST/tools/scratch_query/qrun.py toml $T worldgen.earth_fill log '"ALL"'
python3 $INST/tools/scratch_query/qrun.py toml $T worldgen.earth_fill waterReach 6
boot; qrun.py run $SCR/w2 batches/r1103w/w_3_B.qb RES
```

## W3: w_4_B.qb (group B, fresh, earth fill off)

```
CE_PERF=0 bash $INST/tools/scratch_query/prep_srv.sh $SCR/w3 efboff B
T=$SCR/w3/srv/config/creativeworldgen-common.toml
python3 $INST/tools/scratch_query/qrun.py toml $T worldgen.systems earthFill false
python3 $INST/tools/scratch_query/qrun.py toml $T worldgen.locate searchTimeoutSeconds 5
boot; qrun.py run $SCR/w3 batches/r1103w/w_4_B.qb RES
```

## A1 to A4: w_5_A.qb ... w_8_A.qb (group A, one superflat world)

```
CE_PERF=0 bash $INST/tools/scratch_query/prep_srv.sh $SCR/a wga A
A1: boot; qrun.py run $SCR/a batches/r1103w/w_5_A.qb RES          # SQ-WGE-06c-0 edits srv pool_config.json (pillar #2 current 7)
    stop
A2: python3 $INST/tools/scratch_query/qrun.py toml $SCR/a/srv/config/creativeworldgen-common.toml worldgen.first_tick_water windowTicks 0
    boot; qrun.py run $SCR/a batches/r1103w/w_6_A.qb RES; stop
A3: boot; qrun.py run $SCR/a batches/r1103w/w_7_A.qb RES          # SQ-WGE-06c-3 sets the stored original to 99
    stop
A4: boot; qrun.py run $SCR/a batches/r1103w/w_8_A.qb RES          # ends with /reload + /dumpphysics (SQ-IP-16)
```
SQ-FTW-05 must run within 30 s of the bot reaching x 2000: no pause between `tp` and the checks. SQ-IP-16 runs
`item_physics_audit.py` on a copy inside srv/ (it would otherwise rewrite `feature_writeup/item_physics_audit.md`
and `kubejs/generation_scripts/physics/` in the instance from the instance dump).

## F1, F2: w_9_B.qb, w_10_B.qb (eval seed, first-tick water pair)

```
CE_PERF=0 bash $INST/tools/scratch_query/prep_srv.sh $SCR/f1 ftw_on B level-seed=-8188900049893920259 difficulty=normal spawn-monsters=true spawn-animals=true view-distance=10 simulation-distance=8
boot; qrun.py run $SCR/f1 batches/r1103w/w_9_B.qb RES; stop

CE_PERF=0 bash $INST/tools/scratch_query/prep_srv.sh $SCR/f2 ftw_off B level-seed=-8188900049893920259 difficulty=normal spawn-monsters=true spawn-animals=true view-distance=10 simulation-distance=8
python3 $INST/tools/scratch_query/qrun.py toml $SCR/f2/srv/config/creativeworldgen-common.toml worldgen.systems firstTickWater false
boot; qrun.py run $SCR/f2 batches/r1103w/w_10_B.qb RES; stop
```
The props are eval_server.sh's (the 672-item baseline and the wgp_new census came from those worlds). SQ-TIE-01
compares with `perf/wgp/wgp_new/locate_census.json` (424 s, 10 false negatives).

## G1: w_11_B.qb (Fungi census rerun)

```
df -h /      # the FGDC intro asks for a disk check first
CE_PERF=0 bash $INST/tools/scratch_query/prep_srv.sh $SCR/g1 cdj5c B spawn-animals=true
cp $INST/tools/scratch_query/zq_biomegrid.js $SCR/g1/srv/kubejs/server_scripts/zz_zq_biomegrid.js
boot; qrun.py run $SCR/g1 batches/r1103w/w_11_B.qb RES
```
`/zqb` (new, untested) samples the live biome source without loading chunks and picks the r 150 square with the most
mushroom_fields points; W-G1-MF fails if no square reaches 300 of 361 points (then try a larger search radius or the
row's other-seed fallback). SQ-FGD-14 (another slice) can share this world.

## New helpers (tools/scratch_query/, all pure readers except the probes)

- `efb_judge.py`: `/zq under` lines and `[earth-fill]` lines judged (under, log, vslog), region-file starts (gone,
  starts with biome, layouts across two worlds). Tested on the r1101 w2 world and log.
- `qb_tiles.py`: prints the tile lines (forceload, `/zq under`, say marker, waitlog) used in the batches.
- `climate_share.py`: biome shares of a `/dumpclimate` gz (tested on the shipped End sample).
- `wge04a_check.py`: SQ-WGE-04a weights / notes / estimates (passes on the instance files now).
- `zq_biomegrid.js`: scratch-only `/zqb` command (G1).
