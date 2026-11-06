# r1103 slice B: setup per batch file

Slice: `sliceB_sections.txt` (96 SQ- ids). 38 ids run in two boots of ONE superflat level (`sb1`, group A); no
group B boot is needed (every remaining group B / worldgen row is postponed or not runnable, see `sB_skip.md`).
SQ-LBS-05 (old saves) needs the level made on Creative Labels 1.0.0 first: that is boot 1, and boot 2 restarts the
same level on 1.0.1 and runs everything else.

New probes (both untested in game; `node --check` passes):
- `tools/scratch_query/zsb_probe.js` (`/zsb cans | ns | potnbt | rem | remitem | items`): can grid guard scan
  (SQ-CNP-01), namespace item count (SQ-CUP-02), Copper / Cooking Pot NBT and cook time per result (SQ-CUP-03,
  SQ-JSL-02), crafting remainders (SQ-VNG-02), item entities summed by id (SQ-CN-04b). Walks recipes with
  getSerializer / getAllRecipesFor only (no getType).
- `tools/scratch_query/zlbs_probe.js` (`/zlbs labels | refused | centre | refit | dump`): SQ-LBS-01 to -04 (the queue
  rows say `/cltest surface`; that group does not exist in tools/creative_labels_test/server_probe.js, so the parts
  are this new command) and the face_paper NBT dump for SQ-LBS-05.

## Boot 1: sB_1.qb (level sb1, group A, Creative Labels 1.0.0)

```
OUT=<scratchpad>/r1103/sb1
bash $INST/tools/scratch_query/prep_srv.sh "$OUT" sb1 A
cd "$OUT/srv" || exit 1
# old Creative Labels: the deployed 1.0.0 jar in place of the 1.0.1 build jar prep_srv swapped in
rm -f mods/creativeLabels-*.jar && cp "$INST/mods/creativeLabels-1.0.0.jar" mods/
# SQ-CF-02: Spice of Life's server config from configs_edited, before the level's FIRST boot (this one)
mkdir -p defaultconfigs && cp "$INST/configs_edited/server_config/solapplepie-server.toml" defaultconfigs/
cp "$INST/tools/scratch_query/zlbs_probe.js" kubejs/server_scripts/zz_zlbs_probe.js
```
- scratch_server.sh setup already copies `server_probe.js` as `zz_cl_probe.js` (/cltest); /zq is from prep_srv.
- No toml edits. Boot, wait for `Done (`, `qrun.py run OUT batches/r1103/sB_1.qb RES`.
- Then `scratch_server.sh stop OUT`, wait until `game_jvms.sh` is empty. Keep `OUT` (the level and the rotated
  log are SQ-LBS-05's baseline).
- Time: boot about 2 min, rows about 3 min (two /cltest groups, dump, save).

## Boot 2: sB_2.qb (same OUT, level sb1, Creative Labels 1.0.1)

```
cd "$OUT/srv" || exit 1
rm -f mods/creativeLabels-*.jar
cp "$(ls -t $INST/development/creativeLabels/build/libs/creativeLabels-1.0.1*.jar | grep -v sources | head -1)" mods/
cp "$INST/tools/scratch_query/zsb_probe.js" kubejs/server_scripts/zz_zsb_probe.js
# zz_zlbs_probe.js, zz_cl_probe.js, zz_zq_probe.js and defaultconfigs/ stay from boot 1
```
- Do NOT rerun prep_srv.sh (it rebuilds srv/ and the level would be new). Move any `crash-reports/` out first.
- No toml edits. Boot, `Done (`, `qrun.py run OUT batches/r1103/sB_2.qb RES` (same RES as boot 1: SQ-LBS-05a and
  SQ-LBS-05 sit together in results.json).
- Jars the rows need, all in `mods/` or swapped by prep_srv: `creativedelight-1.0.0` (11-03; Miner's -05 without
  cups, Hearth, Vintage -14), `repackedStorage-...-ce-19.2-01` (Storage Delight fuel values), `creativecompat`
  (SpoutGunFarmland), `creativeengineering-1.1.08`, perfBot (`ceperfbot-0.1.0`, added by prep_srv), corn_delight,
  create_misc_and_things, supplementaries, trashcans, BotanyPots. Check jar times right before the boot (parallel
  sessions install jars).
- Order and areas are in the batch header. Rows that need each other: SQ-LBS-04 after -01 and -03; SQ-VNG-02 uses
  SQ-VNG-01's capture `VNGID`; SQ-CUP-04 does the only `/reload` (last row).
- Time: boot about 2 min; rows about 18 min (337 commands, about 13 min of pace and sleeps, plus the pot cook waits
  about 30 s each, /zsb cans, one /reload about 1 min).

## Hand steps for the runner

- SQ-WGE-00b is offline (no server): run its five commands by hand and check `git diff` (gen_nether_swaps.py
  rewrites its JSON and `feature_writeup/nether_swaps.md`). Not in a batch.
- Judge the LOOK rows from `RES/rows/*.log` and `*.shell.txt`; probe faults (a `[zsb] error` / `[zlbs] FAIL ... ERR`
  line) are BATCH-FAULT, not row FAILs. The SQ-LBS-01 / -03 wanted values are the queue rows' numbers; a FAIL line
  prints the measured place, so a near miss (rounding) can be judged by eye.
- Total: 2 boots, about 27 min including boots.
