# r1103 slice A: setup (sliceA_sections.txt)

Rows: SQ-CWG (Creative Worldgen carve), SQ-CDJ6 Salt (SQ-SLT), SQ-CNG, End's member (SQ-ENM), SQ-GLD, SQ-MDL, Pancake
(SQ-PCK), Swappers (SQ-SWP), SQ-CDJ7 (SQ-BBQF, SQ-VTDA, SQ-CDM2, SQ-GLD2, SQ-SALT2, SQ-NEST). 78 server rows: 77 in
batches (some server half only), 1 skipped; 15 client rows skipped (sA_skip.md). Five boots plus two short failing
boots. INST = instance root, SCR = the run's scratch dir (`$SCR/r1103`), RES = the slice's results dir (one dir for
all files: captures `SAH` / `SBH` carry from boot to boot through RES/vars.json). Shell steps run in OUT/srv.

## Facts that changed the plan (checked 2026-11-03, offline)

- The new jars are already in `mods/`: creativeengineering-1.1.08 (11-02 23:20), creativeworldgen-1.0.0 (23:16),
  creativedelight and creativebrewing (11-03 00:08; CD rebuilt again 00:48 in build/libs, prep_srv.sh swaps it in).
  `salt-*`, `create_salt*`, `CannedGoods-*`, `goldenfood-*`, `moreMooblooms-*` are gone from `mods/`;
  `goldenfood_upstream/` and `enders_upstream/` are in place; `endersdelight-1.19-1.2.2.jar` is still in `mods/`.
- So the queue's "boot 1 on the old jars" (SQ-CWG, SQ-CNG) and "old world" (SQ-GLD-01, SQ-ENM-02) cannot use the
  instance's mods/. Boot 1 runs the **last release as a whole**: `deploy/latest/` (1.1.06 full install, 10-31:
  patcherMod 1.1.04, standalone More Mooblooms, Baby Mobs, Canned Goods, Golden Food, Salt, Create: The Salt, Creative
  Delight under its old id `creative_delight`) with its own kubejs/config/defaultconfigs. That is the real upgrade
  path of a player world (1.1.06 to now), so boot 2 also crosses the creative_delight -> creativedelight rename and
  every other change since 1.1.06: judge old-world rows only on their own ids.
- Boot 0's dumps are in `$SCR/r1103/b0/srv/kubejs/exported/` (00:54 to 00:59, not copied to the instance). Rows read
  them directly through the default of `$B0` (export `B0=$SCR/r1103/b0` to override): SQ-GLD-12 (all.csv: 24
  enchanted golden assemblies already there), SQ-SLT-04 (salt counts), SQ-PCK-04 (placeable: 149 placed links, the
  pancake link present). The 10-30 pre-move baseline for SQ-SLT-04 / SQ-PCK-04 is saved in `sA_baseline/` (the
  instance's exported/ may be overwritten later).
- Generators were not run in Boot 0: SQ-GLD2-05 goes to sA_skip.md. SQ-GLD2-03 uses `/zq food` (no dump needed).

## Probes (copy into OUT/srv/kubejs/server_scripts before each boot of boots 2 to 4; prep_srv.sh adds /zq)

    S=$OUT/srv/kubejs/server_scripts
    cp "$INST/tools/scratch_query/zp_probe.js"     $S/zz_zp_probe.js       # /zp eat, hunger, effects, inv
    cp "$INST/tools/scratch_query/zact_probe.js"   $S/zz_zact_probe.js     # /zact kill (SQ-MDL-03)
    cp "$INST/tools/fungi_probe/zcount.js"         $S/zz_count.js          # /zcount
    cp "$INST/tools/scratch_query/zloot_probe.js"  $S/zz_zloot_probe.js    # /zloot packs (SQ-CWG-04)
    cp "$INST/tools/salt_probe/salt_probe.js"      $S/zz_salt_probe.js     # /salttest (SQ-SLT-03, -05; copied before boot, no /reload)
    cp "$INST/tools/scratch_query/sA_probe.js"     $S/zz_sA_probe.js       # NEW, untested: /sa canuse|alwayseat|cd|anvil|enchtable|enchreg

`sA_probe.js` is new and has not run. If a `[sa] error:` line shows, the rows using it (SQ-GLD-02 canuse, -06 anvil and
enchreg, -07 cd, -08 enchreg, -14 enchtable) FAIL on the probe: fix it in tools/, rerun with `--rows`. Boot 1 gets
no probe (the current /zq loads Creative Calendar classes, absent in 1.1.06).

Jars: a parallel session rebuilt CD at 00:48. Copy the srv jars (no hard links) for every boot, as drive_s5r.sh's
`decouple`: `for j in $OUT/srv/mods/*.jar; do [ "$(stat -c %h "$j")" -gt 1 ] && cp "$j" "$j.tmp" && mv -f "$j.tmp" "$j"; done`

## Boot 1: sA_1.qb, old pack (deploy/latest 1.1.06), group A superflat, level sAo (about 3 min boot + 5 min)

    OUT=$SCR/r1103/sA; D=$INST/deploy/latest
    NOSWAP=1 bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT" sAo A difficulty=normal spawn-monsters=true spawn-animals=true
    cd "$OUT/srv" || exit 1
    rm -f mods/*.jar && cp "$D"/mods/*.jar mods/                     # copies: deploy/ is a release snapshot
    cp "$INST/development/creativeDevtools/members/perfbot/build/libs/ceperfbot-0.1.0.jar" mods/   # SQ-SWP-06o bot
    rm -rf kubejs config defaultconfigs && cp -r "$D/kubejs" "$D/config" "$D/defaultconfigs" .
    for u in vinery_upstream miners_upstream rustic_upstream farmers_upstream; do rm -f "$u"; ln -sfn "$D/$u" "$u"; done
    ls mods | grep -ciE "^(creativeworldgen|creativedelight-|creativebrewing-)"    # must print 0
    boot; wait for Done; qrun.py run "$OUT" "$INST/tools/scratch_query/batches/r1103/sA_1.qb" "$RES"; stop; game_jvms.sh empty

- Props: difficulty normal and monsters on (boot 2's spiders, silverfish, raiders, pillagers; peaceful discards them),
  spawn-animals on (mooshrooms, cows, ducks; with false vanilla discards Animals on their next tick).
- Saves: two canned golems (one Color 11b), a golden cake [bites=2] and an enchanted one [bites=0], two chests of every
  goldenfood item plus 64 golden carrots, an enchanted golden apple and a Golden Food Enchantment book, the four End
  plants on end stone and the dishes chest, an End House at -30 ~ 30 in the End (forceloaded), a placed plains village
  at SAH+160 (forceloaded), a mooshroom pair in a glass pen, two cows, a perfBot `saswp6` whose ender chest holds 4
  alexsmobs:banana. The file ends with `save-all flush`.
- If boot 1 does not reach Done (1.1.06 on this server base): skip it, build boot 2 on a fresh level, and record
  SQ-CWG-08, SQ-CNG-03, SQ-GLD-01, SQ-ENM-02, SQ-SWP-06 as "setup-blocked: old pack does not boot" (their other
  rows still run). If only perfBot fails in 1.1.06, only SQ-SWP-06 is blocked.

## Boot 2: sA_2.qb, current jars on boot 1's world (about 3 min boot + 50 min)

    OUT2=$SCR/r1103/sA2
    bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT2" sAo A difficulty=normal spawn-monsters=true spawn-animals=true
    cp -r "$OUT/srv/sAo" "$OUT2/srv/sAo"                          # the old world (prep_srv.sh does not touch it)
    cd "$OUT2/srv" || exit 1
    (decouple the jars, copy the probes: above)
    rm -f config/creativeworldgen-common.toml                       # SQ-CWG-02: carry-over from the CE file
    python3 "$INST/tools/scratch_query/qrun.py" toml config/creativeengineering-common.toml named_mobs chance 0.02
    grep -nE "^\[(worldgen|forgiving_void|leaf_decay|raids|named_mobs)\]" config/creativeengineering-common.toml   # 5 sections + [perf] mob caps present
    ls mods | grep -iE "^(salt-|create_salt|CannedGoods|goldenfood|moreMooblooms|babyMobs)"                        # must print nothing
    sha1sum goldenfood_upstream/goldenfood-1.19.2-2.0.0.jar                                                         # 4109e4d6...
    boot; qrun.py run "$OUT2" .../sA_2.qb "$RES"; stop; game_jvms.sh empty

- No toml edit beyond the two above. The world keeps its 1.1.06 serverconfig (the real upgrade path).
- Order in the file: boot-log rows (the only kjsexpect row, SQ-MDL-01, is among them; no /reload in this file),
  old-world read-backs (SQ-CWG-08, CNG-03, GLD-01, ENM-02), recipe / tag / item rows, block rows, then the long loot
  rows SQ-MDL-02 to -05 (about 10 000 `loot` commands at pace 0.05: about 15 to 20 min), the raid (SQ-CWG-08b) last.
- Bots: one per row group (sacwg, sacng, saswp2, sagld, sagldc, sachor, savtd, sacdm, sapot, safv, samk, same, saraid);
  SQ-VTDA-12, SQ-CDM2-04 and SQ-SWP-02 relog a bot (`pbot remove` then `pbot spawn`: placeNewPlayer loads the saved
  player data and fires PlayerLoggedInEvent, so the swap login hook runs).
- SQ-CWG-10 runs `tools/build_deploy.py --dry-run` from the instance (writes nothing).
- Known risks (batch faults if they hit): the furnace/hopper and grill rows depend on block-entity ticking in the
  forceloaded area (SA2-00 forceloads x+0..+96, z+0..+96); SQ-BBQF-04 relies on `data get` building a fresh
  villager's offers; SQ-CWG-08 leaf decay needs random ticks (bot `sacwg` within 128 blocks).

## Boot 3: sA_3.qb, same srv and world, four config edits (about 2 min boot + 10 min)

    cd "$OUT2/srv" || exit 1
    Q="python3 $INST/tools/scratch_query/qrun.py toml"
    $Q config/salt-common.toml Dissolving SaltDissolvingChance 1.0                 # SQ-SLT-05
    $Q config/creativeengineering-swaps.toml swappers onPlayerDataLoad false        # SQ-SWP-06
    $Q config/creativeengineering-swaps.toml swappers onCommand false               # SQ-SWP-08
    $Q config/enders_additions-common.toml growth chorusflameGrowthSlow 0           # SQ-ENM-04
    mkdir -p ../moved_loot && mv kubejs/data/creativeworldgen/kubejs/loot/loot_config.json \
       kubejs/data/creativeworldgen/kubejs/loot/loot_index.json ../moved_loot/       # SQ-CWG-05
    boot; qrun.py run "$OUT2" .../sA_3.qb "$RES"; stop; game_jvms.sh empty

- SQ-ENM-04b edits `members.enders_additions false` itself, then `/reload`; SQ-MDL-01b reads the post-reload load.
  Both are the last rows; no kjsexpect after them.

## Boots 3a and 3b: SQ-GLD-10, startup must stop (about 2 min each)

    cd "$OUT2/srv" || exit 1
    rm goldenfood_upstream && mkdir goldenfood_upstream          # removes the srv SYMLINK only; never touch $INST/goldenfood_upstream
    rm -rf crash-reports logs/latest.log; boot; wait_boot.sh "$OUT2" 300   # expect CRASH or GONE, not DONE
    (after an FML error the JVM stays up: kill its recorded pid, then game_jvms.sh empty)
    qrun.py run "$OUT2" .../sA_g10.qb "$RES" --rows SQ-GLD-10a
    cp "$INST/mods/creativeLabels-1.0.0.jar" goldenfood_upstream/goldenfood-1.19.2-2.0.0.jar   # any other jar under the donor name
    rm -rf crash-reports logs/latest.log; boot; wait_boot.sh "$OUT2" 300; kill if needed
    qrun.py run "$OUT2" .../sA_g10.qb "$RES" --rows SQ-GLD-10b
    rm -rf goldenfood_upstream && ln -sfn "$INST/goldenfood_upstream" goldenfood_upstream

## Boot 4: sA_4.qb, fresh group B world without Ender's Delight 1.2.2 (about 3 min boot + 30 to 45 min)

    OUT4=$SCR/r1103/sAb
    bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT4" sAb B difficulty=normal spawn-monsters=true spawn-animals=true
    cd "$OUT4/srv" || exit 1
    rm -f mods/endersdelight-1.19-1.2.2.jar                        # srv copy only; SQ-ENM-03 / -05b
    (decouple, probes: zp, zcount, sA_probe are used)
    df -h /                                                        # about 400 chunks generated (village, meta at 3072, ocean, 3 biomes)
    boot; qrun.py run "$OUT4" .../sA_4.qb "$RES"; stop; game_jvms.sh empty

- If the boot fails without Ender's 1.2.2: that is SQ-ENM-03's FAIL (record the error). Put the jar back, prep a new
  level name, and run `sA_4.qb --from SQ-VTDA-10` (SQ-ENM-05b then also goes unrun: note it).
- Locates last: pillager outpost, plains village, the pillager camp ruin meta (900 s waitlog), warm ocean, swamp, river,
  savanna. A meta locate can run long and cannot be cancelled (gotchas.md): if it hangs past its waitlog, finish the
  file, then `stop` and kill the pid after "All dimensions are saved".
- SQ-CWG-06 places `creativeengineering:meta/meteorite_desert_pyramid` (the only meta with a punch part) at 3072 ~ 3072
  after forceloading 8 x 8 chunks there.

## Rows that need Boot 0 outputs

SQ-GLD-12, SQ-SLT-04, SQ-PCK-04 read `$B0/srv/kubejs/exported/` (recipes/all.csv, tags/item_tags.json,
food/placeable.json). They also have their own live checks, so a missing B0 dir only loses the cross-check.

## Summary

| File | Boot | Group / world | Rows | Minutes |
|---|---|---|---|---|
| sA_1.qb | 1 | A, old pack 1.1.06, level sAo | 5 setup halves (+2 setup rows) | 3 + 5 |
| sA_2.qb | 2 | A, current jars, sAo carried | 64 (+2 setup rows) | 3 + 50 |
| sA_3.qb | 3 | same, 4 config edits | 9 (+1) | 2 + 10 |
| sA_g10.qb | 3a, 3b | same, donor folder broken | 2 | 2 x 2 |
| sA_4.qb | 4 | B fresh, no Ender's 1.2.2, level sAb | 9 (+1) | 3 + 30..45 |

Row ids with suffixes are halves of one queue row: `o` = boot 1 setup (SQ-CWG-08o, CNG-03o, GLD-01o, ENM-02o,
SWP-06o), `b` = second boot or second half (SQ-CWG-02b, -08b, CNG-04b, ENM-04b, ENM-05b, MDL-01b), `a` / `b` of
SQ-GLD-10 = the two failing boots. SA1-00, SA1-99, SA2-00, SA2-99, SA3-00, SA4-00 are setup rows (not queue rows):
ignore unless they FAIL. Total about 2 h 10 min including boots.
