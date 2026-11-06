Run r1102: scratch queue section SQ-CDJ5 (feature_writeup/testing/scratch_queue.md, run plan table).
One runner, one JVM at a time: game_jvms.sh empty before every boot; stop with scratch_server.sh stop and wait for
game_jvms.sh to be empty again. INST = the instance root, OUT = the Boot 0 scratch dir (OUT5 for Boot 5),
RES = one results dir for the whole run (perf/runs/r1102/). Row ids do not repeat across files (Boot 2's half of
SQ-DDP-07 is SQ-DDP-07b), so all files can share RES. Shell steps in the batches run in OUT/srv.

Boot 0 (main session, b0_oldworld.qb, SQ-CDJ-11)
  prep_srv.sh OUT cdj5 A; copy the five displaced jars from development/creativeDelight/jar_archive/displaced/ into
  OUT/srv/mods/. Leaves the marker tagged cdj5home and the old-world blocks at the offsets b1_main.qb's header lists.

Boot 1 (b1_main.qb, then b1_long.qb, same boot)
  cd "$OUT" || exit 1
  # region scan "before" (SQ-CDJ-05 diffs against it)
  python3 -I "$INST/tools/scratch_query/region_ids.py" srv/cdj5/region '(displaydelight|veggiesdelight|vintagedelight|fungidelight|miners_delight):[a-z_]+' > cdj05_before.txt
  # the five displaced jars out (they are the only backported* jars in srv/mods; SQ-CDJ-09 checks none is left)
  rm -f srv/mods/backportedDisplay-1.7.0-ce-19.2-01.jar srv/mods/backportedFungi-1.3-ce-19.2-01.jar \
        srv/mods/backportedMiners-1.4.5-ce-19.2-02.jar srv/mods/backportedVeggies-1.9.2-ce-19.2-04.jar \
        srv/mods/backportedVintage-0.1.6-ce-19.2-10.jar
  ls srv/mods | grep -i backported          # must print nothing
  # config: undo Boot 0's move, then set the two values the move must carry
  rm -f srv/config/*.moved
  cp "$INST/config/creativedelight-common.toml" "$INST/config/displaydelight-common.toml" "$INST/config/vintagedelight-common.toml" srv/config/
  python3 "$INST/tools/scratch_query/qrun.py" toml srv/config/displaydelight-common.toml Features DisableTakeAllInteratction true
  python3 "$INST/tools/scratch_query/qrun.py" toml srv/config/vintagedelight-common.toml pickling_jar remainderBuffer false
  grep -n "^\[display\]\|^\[vintage" srv/config/creativedelight-common.toml   # must print nothing (no [display] / [vintage] yet)
  # live animals (pigs, squid, mooshrooms) and monsters (spiders, silverfish) for the FGD and MDP rows
  sed -i 's/^spawn-animals=.*/spawn-animals=true/; s/^difficulty=.*/difficulty=easy/' srv/server.properties
  # scratch-only probes (never in the pack); /zq is already there from prep_srv.sh
  cp "$INST/tools/fungi_probe/fungi_probe.js" srv/kubejs/server_scripts/zz_fgd_probe.js
  cp "$INST/tools/fungi_probe/zcount.js"      srv/kubejs/server_scripts/zz_count.js
  cp "$INST/tools/fungi_probe/ztrades.js"     srv/kubejs/server_scripts/zz_trades.js
  boot; wait for 'Done ('; qrun.py run OUT batches/r1102/b1_main.qb RES; then qrun.py run OUT batches/r1102/b1_long.qb RES
  b1_main.qb ends with /reload and /dumpall (SQ-CDM-00 onward); b1_long.qb (FGD-07, about 12 min) does not care.
  Time: about 75 min for b1_main (3 x 100 s and 15 s cooks, 2100 loot rolls, /dumpall), 12 min b1_long.

Boot 2 (b2_switches.qb)
  stop; game_jvms.sh empty
  python3 "$INST/tools/scratch_query/qrun.py" toml "$OUT/srv/config/creativedelight-common.toml" members displaydelight false
  python3 "$INST/tools/scratch_query/qrun.py" toml "$OUT/srv/config/creativedelight-common.toml" display takeAll true
  python3 "$INST/tools/scratch_query/qrun.py" toml "$OUT/srv/config/creativedelight-common.toml" vintage.pickling_jar remainderBuffer true
  boot; qrun.py run OUT batches/r1102/b2_switches.qb RES
  The client block (CQ-CDJ-01, CQ-DDP-01 ...) copies this world after setting [members] displaydelight back to true.

Boot 3 (b3_nodonor.qb, boot expected to stop)
  stop; game_jvms.sh empty
  test -L "$OUT/srv/miners_upstream" && rm "$OUT/srv/miners_upstream"     # the srv symlink only, never $INST/miners_upstream
  boot; wait (Monitor, about 240 s timeout) for 'reads its textures' in srv/logs/latest.log, a new srv/crash-reports/*.txt,
  or 'Done (' (= the row FAILs); kill your recorded pid if the JVM stays up after the FML stop; game_jvms.sh empty;
  then qrun.py run OUT batches/r1102/b3_nodonor.qb RES (no console commands in it, it only reads the log).

Boot 4 (b4_wrongdonor.qb, boot expected to stop)
  mkdir "$OUT/srv/miners_upstream" && cp "$INST/miners_upstream/miners_delight-1.20.1-1.4.5-backport.jar" "$OUT/srv/miners_upstream/"
  boot and wait as Boot 3; qrun.py run OUT batches/r1102/b4_wrongdonor.qb RES
  Afterwards: rm -r "$OUT/srv/miners_upstream" && ln -sfn "$INST/miners_upstream" "$OUT/srv/miners_upstream" if the
  cdj5 server is booted again (client block copies the world only, it does not need this).

Boot 5 (b5_groupB.qb, group B, fresh world)
  prep_srv.sh OUT5 cdj5b B spawn-animals=true      # seed 8675309 by default; spawn-animals for the mooshroom rows
  boot; qrun.py run OUT5 batches/r1102/b5_groupB.qb RES
  SQ-MD-01 first (Chunky 400 at spawn, about 15 min); SQ-FGD-12a..e are five Chunky squares of radius 600 (about
  10 min each, longer on a busy machine); the counts are offline (tools/scratch_query/region_blocks.py, written for
  this run, read-only). Short on time: stop after SQ-MD-01 and mark SQ-FGD-12 to -15 deferred, or lower the radius
  as b5_groupB.qb's header says.

Not batched (go to the client block or a hand step): SQ-DDP-07 sneak halves (no sneak in perfBot or /zq: CQ-DDP-01);
SQ-MDP-07 poison cure and eat duration (no eating probe); SQ-MDP-10 copper cup on a cow / fish (entity use:
CQ-MND-01); SQ-MDP-14 Size 7 inventory reload (chunk unload by hand); SQ-MDP-06 Bone Broth pot (inputs from the
rdetail line, by hand); SQ-RDX-04 drinking (food properties only); SQ-RDX-05 JEI half; SQ-CDFW-10 / SQ-FD-01 values
(no /kubejs eval on 1.19.2); SQ-CDFW-11 registry half (CQ-CDJ-01's /cedumpnames).
