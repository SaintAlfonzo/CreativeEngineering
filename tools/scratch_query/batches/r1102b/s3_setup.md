# r1102b slice 3: setup per batch file

Slice: scope.txt lines 34-66 (More Delight to SQ-CDFW), without the First-tick water and Census biome ties rows (other
agent). 133 scope ids: 109 as rows of s3_A.qb (114 rows with 5 `b` halves), SQ-FSD-02 and SQ-WE-04b in s3_A2.qb, 23 in
s3_skip.md. No row needs a normal world: the old "group B" rows (MRD, FFH-02, STD-02, ABD, FSD-07) read registries or
place blocks only, so there is no s3_B.qb. SQ-FSD-04 / -06 (the only worldgen ones) PASSED on 2026-10-26.

New in tools/ for this slice: `tools/scratch_query/dumpq.py` (checks values in srv/kubejs/exported dumps: food, gear,
stack, compost, mod, sset, tagdiff) and `tools/scratch_query/zact_probe.js` (`/zact kill` and `/zact break` through a
Forge fake player: kills with a held item, survival breaks with BreakEvent). Updated: `tools/salt_probe/salt_probe.js`
rain cases (now "stays", SaltDissolvingInRain = false since 10-28; plus salt block, rock salt ore, salt bricks in rain
and The Salt's Salt Block beside water, for SQ-SALT-08).

## s3_A.qb (boot A, superflat)

- `prep_srv.sh OUT s3a A spawn-animals=true` (baby squid, cows, sheep and the other summoned animals; with false vanilla
  discards them on their next tick). Check jar times first: CD `creativedelight-1.0.0.jar` (2026-11-02 14:01 or later,
  nests roll_delight, largemeals, barbequesdelight, nethersdelight, baby_mobs), `creativeengineering-1.1.07.jar`,
  `creativedevtools` (dumps, /ceperf), `repackedStorage-...-ce-19.2-01.jar`; no `NethersDelight-1.19-3.1.jar` in
  srv/mods (SQ-NDM-01 checks). scratch_server.sh links every `*_upstream` folder (roll, largemeals, farmers donors).
- srv toml: none to edit. `srv/config/salt-common.toml` must have `SaltDissolvingInRain = false` (copied from the
  instance; SQ-SALT-08 greps it and the configs_edited copy).
- Probes into `srv/kubejs/server_scripts/` before boot (/zq is already there from prep_srv.sh):
  ```
  S=srv/kubejs/server_scripts
  cp "$INST/tools/cdb_en_probe/cdb_en_probe.js"            $S/zz_cdb_en_probe.js   # /cbetest (CDB-06/07, EN-03/04/06)
  cp "$INST/tools/salt_probe/salt_probe.js"                $S/zz_salt_probe.js     # /salttest (SALT-06/08)
  cp "$INST/tools/flower_pot_test/pot_probe.js"            $S/zz_pot_probe.js      # /pottest (POT-03/04)
  cp "$INST/tools/armor_trims_cache_test/atc_probe.js"     $S/zz_atc_probe.js      # /atctest (ATC-02..04)
  cp "$INST/tools/worldedit_probe/we_probe.js"             $S/zz_we_probe.js       # /wetest (WE-03/04)
  cp "$INST/tools/fungi_probe/zcount.js"                   $S/zz_count.js          # /zcount
  cp "$INST/tools/scratch_query/zact_probe.js"             $S/zz_zact_probe.js     # /zact kill|break
  ```
- The perf recording (`ce`) that scratch_server.sh adds must be on (SQ-PF-02 dumps it, SQ-PF-05 reads the handler count).
- Run: `qrun.py run OUT batches/r1102b/s3_A.qb RES`.
- Order inside the file: boot-log and kjsexpect rows first; the three /reload rows (SQ-PF-05, SQ-ATC-04 after it,
  SQ-LGM-10) last. SQ-ATC-03 needs SQ-ATC-02's run in the same boot; SQ-CDFW-03 reads SQ-CDFW-02's /dumptags;
  SQ-POT-03 runs /dumppots first (the probe reads its file).
- Shell steps run in OUT/srv and call `$INST/tools/scratch_query/dumpq.py` (dumps in srv/kubejs/exported) and
  `$INST/tools/perf/perf_report.py`; SQ-PF-02 writes `OUT/pf02_report.txt`.
- Time: about 60 min after `Done (` (583 console commands at pace 2 plus repeats, about 7 min of sleeps, the dumps,
  two /pottest all of 1-3 min, two /salttest, /cbetest dragon, three reloads). Boot about 2 min.

## s3_A2.qb (boot A2, same world restarted without the Farmer's donor)

- `scratch_server.sh stop`, wait until `game_jvms.sh` is empty.
- `test -L "$OUT/srv/farmers_upstream" && rm "$OUT/srv/farmers_upstream"` (the srv symlink only, never
  `$INST/farmers_upstream`).
- Boot, wait for `Done (`, `qrun.py run OUT batches/r1102b/s3_A2.qb RES` (SQ-FSD-02, SQ-WE-04b = SQ-WE-04 after a
  restart).
- Afterwards: `ln -sfn "$INST/farmers_upstream" "$OUT/srv/farmers_upstream"` if this world boots again.
- Time: about 2 min boot + 3 min.

## Not batched

s3_skip.md: 23 whole rows (client, needs a player, old save, already run) and 19 client / player halves of rows that
are batched.
