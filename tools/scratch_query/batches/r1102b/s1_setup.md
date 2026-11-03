# r1102b slice 1 (scope.txt lines 1-20): boot setup

INST = instance root, OUT = scratch dir of Boots O and A (one world, level `s1a`), OUT_B = scratch dir of Boot B,
RES = the run's results dir. One game JVM at a time: `game_jvms.sh` empty before every boot. Shell steps run in
`OUT/srv`. Row ids do not repeat across the three files (Boot O's row is `SQ-VTD4-05o`).

## Boot O: s1_O.qb (old world, about 4 min)

```
bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT" s1a A
cd "$OUT/srv" || exit 1
cp "$INST/development/creativeDelight/members/vintage/jar_archive/backportedVintage-0.1.6-ce-19.2-03.jar" mods/
cp "$INST/development/creativeBrewing/jar_archive/displaced/backportedVinery-1.5.5-ce-19.2-06.jar" mods/
cp "$INST/development/creativeBrewing/jar_archive/displaced/backportedBrewin-4.5.0-ce-19.2-04.jar" mods/
```

- prep_srv.sh props: none beyond its defaults. `vinery_upstream/` and `miners_upstream/` symlinks come from prep_srv.sh.
- Probes: none (the file uses vanilla commands only).
- A top-level jar beats the nested copy (SQ-CDJ-02), so these three load instead of the CD / CB members. If the boot
  dies (current CD scripts against the old Vintage -03), drop the Vintage jar and reboot: SQ-VTD4-05 is then
  not run (record "setup"), SQ-CBJ-04 and the CDJ-07 world half still work.
- Boot, wait for `Done (`, `qrun.py run OUT s1_O.qb RES`, then `scratch_server.sh stop`, wait until game_jvms.sh is empty.

## Boot A: s1_A.qb (same world, about 40 min)

```
cd "$OUT/srv" || exit 1
rm -f mods/backportedVintage-0.1.6-ce-19.2-03.jar mods/backportedVinery-1.5.5-ce-19.2-06.jar mods/backportedBrewin-4.5.0-ce-19.2-04.jar
ls mods | grep -i backported      # must print nothing
cp "$INST/tools/vintage_probe/vintage_probe.js"        kubejs/server_scripts/zz_vtd_probe.js
cp "$INST/tools/kitchen_probe/cfb_kitchen_probe.js"    kubejs/server_scripts/zz_kt_probe.js
cp "$INST/tools/fungi_probe/zcount.js"                 kubejs/server_scripts/zz_count.js
cp "$INST/tools/fungi_probe/ztrades.js"                kubejs/server_scripts/zz_trades.js
cp "$INST/tools/scratch_query/zloot_probe.js"          kubejs/server_scripts/zz_zloot_probe.js
cp "$INST/tools/scratch_query/zti_probe.js"            kubejs/server_scripts/zz_zti_probe.js
grep -n "placeAs\|farmlandBlocks" config/farmersdelight-ce-common.toml    # both keys present (rows edit them)
grep -nE "^\[time\]|dayStart" config/creativecalendar-common.toml   # [time] enabled = true (line after), dayStart = 22300.0
```

- srv toml edits before the boot: none. During the run the batch edits `config/farmersdelight-ce-common.toml`
  (SQ-FDB-05 placeAs, SQ-FDB-07 farmlandBlocks; both restored at the row end) and Creative Calendar's `[time] enabled`
  through `/zti off|on` (SQ-TI-01, left on).
- spawn-animals stays false: the only animals are iron golems (SQ-FDB-06) and villagers (FDB-07 and /ztrades), which
  are not discarded. Difficulty is set to easy by the first row (zombies in PS-03 / FDB-11).
- New probes written for this slice: `tools/scratch_query/zloot_probe.js` (/zloot packs|adds, reads
  `global.lootTuning`) and `tools/scratch_query/zti_probe.js` (/zti, Creative Calendar TimeSettings). Neither was run yet:
  if /zloot or /zti logs `error:`, the rows using them (SQ-VTD4-07, SQ-VP-11, SQ-TI-01) FAIL on the probe, not the pack.
- Order constraints: the Boot O read-back rows (VTD4-05, CBJ-04, CDJ-07) first; CBJ-04 sleeps 120 s on purpose;
  config-editing rows (FDB-05, FDB-07, TI-01) near the end; `/reload` only in the last row (SQ-VP-05b). No kjsexpect rows.
- Bots: perfBot bots get the starter kit 10 ticks after their first join; rows that use a bot's hand wait 2 s and
  `clear` it. SQ-SK-01 / -02 rely on that kit (they are the starter-kit rows).
- Long rows: CBJ-04 2 min, FDB-07 3 min, FDB-11 600 loot rolls (about 1 min), LE-05 75 villagers.

## Boot B: s1_B.qb (fresh normal world, about 30 min)

```
bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT_B" s1b B
cp "$INST/tools/fungi_probe/zcount.js" "$OUT_B/srv/kubejs/server_scripts/zz_count.js"
```

- prep_srv.sh props: none extra (group B: normal world, structures on, seed 8675309).
- Probes: /zq (prep_srv.sh) and zz_count.js.
- SQ-FN-03 1600 loot rolls (about 3 min with the forceloads); SQ-VP-15 forceloads 4 x 16 Nether chunks (sync
  generation, `max-tick-time=-1`); SQ-LE-06 runs 9 group locates through Async Locator (up to 15 min; a `stop` during a
  running locate hangs after the save line: kill the recorded pid). LE-06 runs last for that reason.
- No order constraint with Boots O / A; Boot B can run first if the queue needs it.
