# r1102b rerun: setup for r_A.qb, r_B.qb, r_B2.qb

Rows: the slice-2 and slice-3 rows whose first run was a batch fault with no verdict, or OPEN for want of a better
command (perf/runs/r1102b/s2/judged.md and s3/judged.md, "Batch fixes" / "Rerun"), plus SQ-WE-04b. Rows with no
server command that decides them: r_skip.md. One JVM at a time: `game_jvms.sh` empty before every boot,
`scratch_server.sh stop` then wait until it is empty again. INST = instance root, SCR = the run's scratch dir
(`$SCR/r1102b/s1a` already exists), RES = a new results dir `perf/runs/r1102b/r` (keeps the first-run row logs in
s2/ and s3/; row ids repeat on purpose).

## Probe changes in this batch (tools/, scratch-only, none run yet)

- `tools/scratch_query/zq_probe.js`: `rdetail` adds `rdetail+ <id> results:` (every rolled result with count and
  chance: FD cutting, Create processing), `start:` and `step N` (sequenced assembly), `fields:` (public Ingredient fields
  `tool` / `ingredient` / `side` with counts: CD Barbeque's skewering). The main `<id> {type}` line is unchanged, so
  existing counts stay valid. New `/zq bytype <serializer>`. `useitem` tries the off hand when the main hand does not
  consume the action (logs `useitem NAME offhand RESULT`).
- `tools/scratch_query/zact_probe.js`: new `/zact useon X Y Z NAME HITY`: bot NAME uses its main-hand item on the top
  face with the hit at y + HITY (perfBot's `pbot use` hits y + 1.0, outside the Barbeque's grill slot box
  y 0.8225..0.99).
- If a new probe line logs `error:` or is missing, the rows that use it (VDF-04, CDFW-07, BBQ-02 rdetail+ /
  bytype; ROLL-03 / -04 offhand; BBQ-06 / -07 useon) FAIL on the probe, not the pack: fix the probe, rerun with `--rows`.

## Boot 1: r_A.qb on the existing group A superflat level s1a (about 3 min boot + 35 min)

```
O=$SCR/r1102b/s1a; S=$O/srv/kubejs/server_scripts
cp "$INST/tools/scratch_query/zq_probe.js"    $S/zz_zq_probe.js      # updated: rdetail+, bytype, useitem offhand
cp "$INST/tools/scratch_query/zact_probe.js"  $S/zz_zact_probe.js    # updated: useon (also break, used by EN-03, POT-04v)
cp "$INST/tools/scratch_query/zp_probe.js"    $S/zz_zp_probe.js      # /zp inv (RSP-12)
cp "$INST/tools/salt_probe/salt_probe.js"     $S/zz_salt_probe.js    # /salttest (SALT-06 / -08)
cp "$INST/tools/worldedit_probe/we_probe.js"  $S/zz_we_probe.js      # /wetest (WE-04b)
ls $S/zz_count.js $S/zz_trades.js $O/srv/kubejs/data/zq               # from s1 Boot A / prep_srv.sh: must exist
cd $O/srv && sed -i 's/^spawn-animals=.*/spawn-animals=true/' server.properties && grep -n '^spawn-animals' server.properties
grep -n "SaltDissolvingInRain = false" config/salt-common.toml        # SQ-SALT-08 greps it
```

- Extra probes beyond s1_setup.md Boot A: the five `cp` lines above (zq is a re-copy of the updated file).
  Not needed: cdb_en_probe (EN-03's soils / growth already passed; the rerun half uses setblock and `/zact break`),
  pot_probe (POT-04v uses commands).
- Props: `spawn-animals=true` (BM-02's animals, RD-08's squid; with false vanilla discards Animal and WaterAnimal
  entities on their next tick). Difficulty is set to easy by the first row (server.properties says peaceful: the baby
  zombie of BM-02 needs easy).
- Toml edits: none. Jar check: `creativedelight-1.0.0.jar` in srv/mods is the 11-02 14:01 build (same as mods/).
- Order: the file order. The first row summons the home marker `rhome` at 1000 -60 1000 (capture R) and forceloads
  every spot (R lanes, 96..340 x 496..560, 400 400, the salttest strip, wetest 96..127). Slice-2 rows first, then the
  slice-3 rows; no /reload anywhere. EN-03 sets randomTickSpeed 300 for 20 s and back to 3.
- Run: `qrun.py run $O tools/scratch_query/batches/r1102b/r_A.qb $RES`.
- Time: about 35 min after `Done (` (two /salttest of 1-3 min, kettle brews of 12-15 s, 90 loot rolls at pace 0.05,
  BBQ-06's 37 s burn wait, 20 s EN-03 ticking, /wetest).

## Boot 2: r_B.qb, fresh group B world (about 3 min boot + 25-35 min)

```
bash "$INST/tools/scratch_query/prep_srv.sh" "$SCR/r1102b/rb" rb B spawn-animals=true     # seed 8675309, structures on
```

- Default config: the Coff switches stay ON. No r_B row needs them, and SQ-RD-26 / SQ-WCD-02 need wildCrops on.
- Probes: prep_srv.sh copies the updated `zz_zq_probe.js`; nothing else is used.
- Run: `qrun.py run $SCR/r1102b/rb tools/scratch_query/batches/r1102b/r_B.qb $RES`.
- Locates: three savanna villages (spawn-nearest, from 3000 3000, from -3000 3000), one plains village, a jungle from
  2500 -2500, a beach from 2500 2500, a savanna from -2500 2500, then 7 x 7 to 9 x 9 forceloads. A locate cannot be
  cancelled; restart if a stop hangs on an Async Locator search (scratch-test gotchas).

## Boot 2b: r_B2.qb, the same rb world restarted with wildCrops off (about 2 min boot + 4 min)

```
scratch_server.sh stop; wait until game_jvms.sh is empty
T=$SCR/r1102b/rb/srv/config/creativedelight-common.toml
python3 "$INST/tools/scratch_query/qrun.py" toml $T worldgen wildCrops false
grep -n "wildCrops" $T
```

- Only `[worldgen] wildCrops = false`; compost, manualOnlyRoutes and remainderBuffer stay on (SQ-WCD-04 does not need
  them). This is a third JVM start: run it right after boot 2 if SQ-WCD-04 is wanted; otherwise leave it for the next
  run. The row's beach (from -2500 -2500) is far from every chunk r_B.qb generated, so it generates with the switch off.
- Run: `qrun.py run $SCR/r1102b/rb tools/scratch_query/batches/r1102b/r_B2.qb $RES`, then stop and delete the rb dir.

## Summary

| File | Rows | Boot | Needs | Minutes |
|---|---|---|---|---|
| r_A.qb | 28 | s1a superflat (existing), spawn-animals=true | zq, zact (updated), zp, salt, we, zcount, ztrades | 3 + 35 |
| r_B.qb | 5 | fresh B, seed 8675309, default config | zq (updated) | 3 + 30 |
| r_B2.qb | 1 | same rb world, wildCrops = false | zq | 2 + 4 |

Skipped and not-rerun rows: r_skip.md.
