# r1102b slice 2 (scope.txt lines 21-33): boot setup

One JVM at a time: `game_jvms.sh` empty before every boot, `scratch_server.sh stop` then wait until it is empty again.
INST = instance root, RES = the run's results dir (row ids do not repeat across these files). Shell steps run in
OUT/srv. Every boot needs the probes below; prep_srv.sh already copies `zz_zq_probe.js`.

Probes (scratch-only, never in the pack):

    cp "$INST/tools/scratch_query/zp_probe.js"   srv/kubejs/server_scripts/zz_zp_probe.js    # new: /zp sneak|eat|hunger|effects|food|inv
    cp "$INST/tools/fungi_probe/zcount.js"       srv/kubejs/server_scripts/zz_count.js
    cp "$INST/tools/fungi_probe/ztrades.js"      srv/kubejs/server_scripts/zz_trades.js

`zp_probe.js` is new in this slice (untested): `/zp sneak NAME on` sets the bot's shift key so the next `/pbot use` is a
sneak use (also unblocks the sneak half of SQ-DDP-07 / CQ-DDP-01); `/zp eat NAME` eats the main-hand item through
`finishUsingItem` + `ForgeEventFactory.onItemUseFinish`. If `/zp` errors on its first use (Rhino name), rows SQ-FR-02,
SQ-RD-04 (second half), -17, -20 and the `zp inv` checks FAIL on that line only: fix the probe and rerun those rows
(`--rows`).

## O0 then A: the same world (group A superflat, level s2a)

1. O0, old world:

        prep_srv.sh OUT_A s2a A
        cp "$INST/development/creativeDelight/jar_archive/displaced/rusticdelight-forge-1.19.2-1.3.0.jar" \
           "$INST/development/creativeDelight/jar_archive/displaced/Delightful-1.19.2-3.4.1.jar" OUT_A/srv/mods/
        (probes as above)
        boot; qrun.py run OUT_A batches/r1102b/s2_O0.qb RES

   A jar in mods/ beats the nested member (CreativeDelight.warnIfNotNested). CD's Rustic / Delightful scripts may log
   errors against the 1.3.0 ids in this boot: not judged. If the boot does not reach `Done (` with the two old jars,
   remove them, mark SQ-RD-03 / SQ-DF-07 / SQ-RSP-14 (and their `a` rows) "not run: old jars do not boot beside CD",
   and run s2_A.qb on the fresh world anyway (the capture of H then needs `summon marker` once: s2_O0.qb's first rows
   do it, run `qrun.py run ... s2_O0.qb --rows SQ-RD-03a` with the jars out and accept its FAILs).
2. stop; game_jvms.sh empty; `rm OUT_A/srv/mods/rusticdelight-forge-1.19.2-1.3.0.jar OUT_A/srv/mods/Delightful-1.19.2-3.4.1.jar`
3. A: boot; `qrun.py run OUT_A batches/r1102b/s2_A.qb RES`. Leaves `OUT_A/cdq05_on.json` (SQ-CDQ-05) for Coff.

No toml edits. No order constraint inside A beyond the file order (no /reload in it; SQ-INV-17b runs /dumpall near the
end, SQ-FF-02 and SQ-CDQ-05 read its files). Watch: SQ-CN-03 / SQ-CDF-05 / SQ-CDQ-05 use `/dumpcompost` on a dedicated
server; gotchas.md lists compost among the client dumps. If `kubejs/exported/compost/compost.json` is not rewritten (check
its mtime) or `now` is empty, those three rows (and Coff's SQ-CDQ-05b) move to the client block.

Time: O0 about 3 min after `Done (`; A about 55 min (900+ commands at pace 2, cooks of 12 to 25 s, about 400 loot
rolls, /dumpall several minutes).

## B: fresh normal world (level s2b)

    prep_srv.sh OUT_B s2b B          # seed 8675309, structures on
    (probes: zz_count.js is needed; zp / ztrades not used)
    boot; qrun.py run OUT_B batches/r1102b/s2_B.qb RES

Locates (five village types, jungle, beach, savanna, taiga, forest, three deadwood metas, two End biomes) plus about
15 forceload squares of 7 x 7 to 9 x 9 chunks, generated synchronously. A rare-meta locate can run long and cannot be
cancelled (gotchas): the deadwood waitlogs give up after 900 s, the End ones after 600 s; restart if a stop hangs on
an Async Locator search. Time: 35 to 50 min.

## Coff: switches off at boot, fresh normal world (level s2c)

    prep_srv.sh OUT_C s2c B
    (probes: zz_count.js)
    T=OUT_C/srv/config/creativedelight-common.toml
    grep -n "remainderBuffer\|compost\|manualOnlyRoutes\|wildCrops" $T     # keys present? (Forge adds missing ones at load)
    python3 "$INST/tools/scratch_query/qrun.py" toml $T qol remainderBuffer false
    python3 "$INST/tools/scratch_query/qrun.py" toml $T qol compost false
    python3 "$INST/tools/scratch_query/qrun.py" toml $T qol manualOnlyRoutes false
    python3 "$INST/tools/scratch_query/qrun.py" toml $T worldgen wildCrops false
    cp OUT_A/cdq05_on.json OUT_C/                     # after A ran (SQ-CDQ-05b diffs against it)
    boot; qrun.py run OUT_C batches/r1102b/s2_Coff.qb RES

Order: after A. Time: about 8 min (one beach locate and a 7 x 7 forceload).

## N: three removals, fresh normal world (level s2n)

    prep_srv.sh OUT_N s2n B
    (probes: zz_count.js)
    rm OUT_N/srv/mods/fruitsdelight-0.5.11.jar OUT_N/srv/mods/FarmersRespite-1.19-2.0.jar
    rm -r OUT_N/srv/kubejs/data/creativedelight        # the scratch copy only, never $INST/kubejs
    boot; qrun.py run OUT_N batches/r1102b/s2_N.qb RES

No mod in mods/ declares a dependency on fruitsdelight or farmersrespite (mods.toml scan 2026-11-02). Time: about 4 min.

## RDstop: no Rustic donor jar, boot expected to stop

    reuse OUT_N (or any prepped OUT): test -L OUT_N/srv/rustic_upstream && rm OUT_N/srv/rustic_upstream   # symlink only
    boot; wait (Monitor, ~240 s) for 'reads its textures' in srv/logs/latest.log, a new srv/crash-reports/*.txt, or
    'Done (' (= FAIL); kill the recorded pid if the JVM stays up after the FML stop; game_jvms.sh empty
    qrun.py run OUT_N batches/r1102b/s2_RDstop.qb RES
    afterwards: ln -sfn "$INST/rustic_upstream" OUT_N/srv/rustic_upstream if OUT_N boots again

Order: last (it reuses N's dir). Time: about 4 min.

## Summary

| File | Rows | Boot | Needs | Minutes |
|---|---|---|---|---|
| s2_O0.qb | 3 | A world, + 2 old jars | none | 3 |
| s2_A.qb | 79 | same world, old jars out | zp, zcount, ztrades | 55 |
| s2_B.qb | 11 | fresh B | zcount | 35-50 |
| s2_Coff.qb | 4 | fresh B, 4 switches off | zcount, OUT_A/cdq05_on.json | 8 |
| s2_N.qb | 3 | fresh B, 2 jars + CD folder out | zcount | 4 |
| s2_RDstop.qb | 1 | rustic_upstream link removed | none | 4 |

Plus about 2 min per boot. Skipped rows: s2_skip.md.
