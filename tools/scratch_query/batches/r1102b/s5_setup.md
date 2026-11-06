# r1102b slice 5: setup (scope.txt lines 69, 71 to 78, 80)

Rows: SQ-CDJ5 fix rerun (SQ-CDJ-12, SQ-MDP-18, SQ-VTD8-02), Jar fluid pipes (SQ-JFP-01 to -05), SQ-RB2, SQ-DRK, SQ-FCB
(00 to 08), SQ-PST, SQ-PSD, SQ-PSM, SQ-PSF, SQ-PSU. 68 server rows in 4 boots; 10 client rows in s5_skip.md.
INST = instance root, RES = the run's shared results dir. Shell steps run in OUT/srv. All captures start with `S5`
(RES/vars.json is shared with the other slices).

Jars (checked 2026-11-02): mods/ has creativedelight 14:01 (= build/libs, nests Vintage -13 and Miner's -05),
creativeengineering-1.1.07 14:08, creativecalendar 14:08, creativecompat 13:52, creativebrewing 13:52; all newer than
the sources the rows test. Check the times again right before each boot (gotchas.md).

`/cegen` (plan_prescratch_fixes.md, item physics and WTHIT silk-touch generators): no row in this slice depends on it
(both write client data or reports). Not in any s5 batch.

Probes, copied into OUT/srv/kubejs/server_scripts before the first boot of each group (prep_srv.sh already adds /zq):

    cp "$INST/tools/fungi_probe/zcount.js"          "$OUT/srv/kubejs/server_scripts/zz_count.js"
    cp "$INST/tools/scratch_query/s5_probe.js"      "$OUT/srv/kubejs/server_scripts/zz_s5_probe.js"

`s5_probe.js` (new, untested): `/s5 sneak|lclick|break|eat|rest|trim|remainder|spout|drain|containers|selfloop|fluidtag|
potrecipe|jarrecipe`, header lists each. `tools/scratch_query/s5_ratio.py` (new) scores the SQ-PST spring/winter pads
from latest.log.

## Boot A1: s5_A.qb (group A superflat, about 70 min)

    bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT" s5a A difficulty=normal spawn-monsters=true spawn-animals=true spawn-protection=16
    cp probes (above)
    cd "$OUT" || exit 1
    Q="python3 $INST/tools/scratch_query/qrun.py toml"
    # Pickling Jar: the old Vintage file is carried into the CD file at boot (r1102); if srv/config has no
    # vintagedelight-common.toml, set the same keys in creativedelight-common.toml under vintage.pickling_jar instead
    $Q srv/config/vintagedelight-common.toml pickling_jar remainderBuffer true
    $Q srv/config/vintagedelight-common.toml pickling_jar stallWhenFull false
    # stems: no fruit wait, at most 3 fruit (SQ-PSF-05..07 keep one side empty); boost off for SQ-PSF-08
    for k in secondFruitMinutes thirdFruitMinutes fourthFruitMinutes; do $Q srv/config/creativedelight-common.toml stem_fruit $k 0; done
    $Q srv/config/creativedelight-common.toml stem_fruit maxFruits 3
    $Q srv/config/creativedelight-common.toml stem_fruit fertileSoilBoost false
    boot; wait for 'Done ('; qrun.py run OUT batches/r1102b/s5_A.qb RES

Why the props: spiders/shulkers/phantoms need difficulty above peaceful and monsters on; squid and pigs are discarded
on their next tick with spawn-animals=false; spawn-protection=16 is only live while an op is online (SQ-FCB-08 ops a bot
for one check and deops it).

Order inside the file: boot-log rows and probes first, block rows, then the long rows (SQ-PSF-08 reads its stems 600 s
after SQ-PSF-08a by a shell wait; PSF-05..07 at randomTickSpeed 300; SQ-PST pads at randomTickSpeed 100 with Serene
Seasons `season set`; season left at mid_spring), SQ-PSU-04 (night, `pbot remove-all`), and SQ-FCB-07 last (`/reload`;
every kjsexpect/kjsreject row is before it). Long single steps: SQ-VTD8-02 105 s of readings, SQ-RB-22 3 x the recipe
time, SQ-PSD-03 120 s, SQ-PSU-04 6 min.

Sites (offsets from the s5home marker at spawn): rows at x +18..+80, z +20..+110; SQ-PST pad x +37..+74, z +57..+94;
forceloaded far site x +384..+432 (SQ-JFP, SQ-FCB-07 unload and reload their chunk there); SQ-PSU-04 bots at x -200
and +200.

Known risks (batch faults, not defects, if they hit): Create pump direction depends on facing and rotation sign, so the
JFP rows run both facings and are judged by look; SQ-PSF-09 merges the well tank as `Tank:{FluidName,Amount}` (the row
says to read the keys first: the first `data get` shows them); SQ-JFP-03 needs a non-zero partial bottle at the moment
the motor stops.

## Boot A2: s5_A2.qb (same level, about 12 min)

    scratch_server.sh stop OUT; game_jvms.sh empty
    Q="python3 $INST/tools/scratch_query/qrun.py toml"
    $Q "$OUT/srv/config/creativedelight-common.toml" vintage.pickling_jar remainderBuffer false
    $Q "$OUT/srv/config/creativedelight-common.toml" stem_fruit fertileSoilBoost true
    $Q "$OUT/srv/config/creativedelight-common.toml" qol cookwarePickup false
    boot; qrun.py run OUT batches/r1102b/s5_A2.qb RES

SQ-VTD8-02c edits `overflowEjectDelayTicks = 0` live (CdSettings reads the file's config data on each call). If its
look shows the 1 s lag still there, stop, keep the edit, boot, and rerun only `--rows SQ-VTD8-02c`.

## Boot B: s5_B.qb (fresh normal world, about 70 to 100 min)

    bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT_B" s5b B difficulty=normal spawn-monsters=true spawn-animals=true
    cp probes (above, into OUT_B)
    df -h /     # 6 villages x 49 chunks, 2 biome squares of 64 chunks, deep ocean, towers, ships, metas
    boot; qrun.py run OUT_B batches/r1102b/s5_B.qb RES

Order: villages (SQ-PSD-06 a..f, one locate per row because Async Locator answers later), raspberry biomes, deep ocean
(SQ-PSM-08, then -09 with a 600 s natural-spawn wait), lone birch tower (SQ-PSM-03, 300 s of counts), ships and huts,
then the two meta locates (SQ-PSM-04, -02; can run long and cannot be cancelled: gotchas.md) and SQ-PSM-06a last
(locates the struck and a lone monument without loading them, saves). If a meta locate hangs past its waitlog
(1500 / 1800 s), stop the server (kill after "All dimensions are saved") and skip to Boot B2 only if SQ-PSM-06a's
captures (S5SM, S5LM) exist; else rerun `--rows SQ-PSM-06a` on a fresh boot of the same level first.

## Boot B2: s5_B2.qb (same level as B, about 10 min)

    scratch_server.sh stop OUT_B; game_jvms.sh empty
    boot; qrun.py run OUT_B batches/r1102b/s5_B2.qb RES

SQ-PSM-06 (struck) and -06b (control) visit the monuments for the first time after the restart; SQ-PSM-07 at the lone
monument (bot on a glass block above it, 240 s).

## Not queue rows

S5A-00, S5A2-00, S5B-00, S5B2-00 (setup) and S5A-PSF-bot (a bot for the stem rows) appear in results.json; ignore
them unless they FAIL (then the rows after them are suspect). Split rows: SQ-MDP-18b, SQ-RB-21b, SQ-FCB-04b..e,
SQ-PSF-07b (cantaloupe half of -07), SQ-PSF-08a (start of -08), SQ-PSF-08b and SQ-FCB-08b (A2 config halves),
SQ-VTD8-02b and -02c (A2 halves), SQ-PST-11b (mycelium half), SQ-PSD-06b..f (one village each), SQ-PSM-06a (locate
half) and SQ-PSM-06b (control).
