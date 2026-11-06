# r1103 slice B: rows not batched

Slice: tools/scratch_query/batches/r1103/sliceB_sections.txt, 96 SQ- ids (CQ- rows pinned, not listed). 38 ids are
batched (sB_1.qb: SQ-LBS-05a, the old-save half of SQ-LBS-05; sB_2.qb: 38 rows). The 58 below are not.
Rows marked "from r1102b" keep the reason of that run's skip file (re-checked 2026-11-03: no cdtest jar in
development/creativeDelight/build/libs, no old saves for CSI-02 / NDM-08 / RDX-13, client rows still client).
None of the slice-B ids is in r1102b's unrun rerun batches (r_A, r_B, r_B2, s1_R, s5_R_A*, s5_B2) or in the list of
unfixed r1102b FAILs.

| ID | Reason | Detail | Source |
|---|---|---|---|
| SQ-AC-04 | client | HUD armor bar | from r1102b s3_skip.md |
| SQ-BBQ-08 | needs player | crafting grid result (bots cannot craft) and eating the wrap; recipe ids are in SQ-BBQ-02 | from r1102b s3_skip.md |
| SQ-CDB-11 | client | names (Bayberry Juice on bottle, fluid, can), JEI cacciatore cycling, jar fill shown in the GUI | from r1102b s3_skip.md |
| SQ-CDB-14 | client | JEI uses page of the Sample Syringe; `$WorldRows` is a client JEI script | from r1102b s3_skip.md |
| SQ-CDJ-06 | recorded | queue Run log, 2026-10-25 Creative Delight run: "SQ-CDJ-01, -06 PASS (server half) ... End's gristle 3 and 5 from 10 knife kills"; the client mod-list half is client work | from r1102b s1_skip.md |
| SQ-CDQ-04 | client | tooltips on hover and F3+T reload of `placeableTooltips` (client script tree `assets/creativedelight/kubejs/_qol/`) | from r1102b s2_skip.md |
| SQ-CDQ-07 | client | WTHIT "Growth" lines, `wthitCropLine` read by the client overlay | from r1102b s2_skip.md |
| SQ-CDQ-08 | client | JEI info pages (`// requires: jei`), F3+T | from r1102b s2_skip.md |
| SQ-CDT-04 | recorded / jar missing | Run log: "SQ-CDT-02, -04, -05 PASS: ... ns 11/11, requires 2/2, tags 1/1, config 5/5"; a rerun needs `creativedelight-1.0.0-cdtest.jar` (`-Pcd_test_suite` build), not in `development/creativeDelight/build/libs/` | from r1102b s1_skip.md |
| SQ-CDT-05 | recorded / stale | Run log: "mirror 57/57 (30 helpers)"; tools/creative_delight_test/README.md: the mirror is frozen since the cooking scripts moved into the CD tree, `--check` reports it stale by design | from r1102b s1_skip.md |
| SQ-CM-33 | client | JEI item list order | from r1102b s3_skip.md |
| SQ-CSI-02 | needs player (old save) | needs a save made before the CuriosSlots jar; every r1102b world is new. A fresh bot is SQ-CSI-01 | from r1102b s3_skip.md |
| SQ-CUP-05 | old world, not runnable | needs a copy of a pre-change world holding copper and soup cups (chest and copper pot slot); no such world was kept, and making one needs the pre-11-02 Creative Delight jar (jar_archive/displaced/creative_delight-1.0.0.jar, 11-01, old underscored id) beside the current patcherMod / Calendar / Brewing jars: same class as r1102b HANDOVER 4.6. Owner: keep a save from an old instance or drop the row | r1103 |
| SQ-EN-05 | client | JEI pages and tooltips | from r1102b s3_skip.md |
| SQ-EN-08 | client | 25 retextured items (art); the Shulker Bowl message needs a player eating by hand | from r1102b s3_skip.md |
| SQ-FDB-08 | needs player | skillet use in the air underwater with beef in the off hand: perfBot has block use only (/zq useitem has no off-hand setup), and the pass is an action-bar message (client) | from r1102b s1_skip.md |
| SQ-FDB-12 | client | particles, models, item icons, tooltips, the rice placement message | from r1102b s1_skip.md |
| SQ-FFB-01 | client | needs the `/dumpfood` food export (client-only dump, gotchas.md "Logs and results") copied into the instance, then `tools/food_families.py` | from r1102b s2_skip.md |
| SQ-FSD-04 | stale (already run) | farmers_structures.md 4: 2026-10-26 run PASSED SQ-FSD-04 (all 7 kinds place); the contents half (chests, frames, pots) is a client look | from r1102b s3_skip.md |
| SQ-FSD-06 | stale (already run) | farmers_structures.md 4: SQ-FSD-06 PASSED 2026-10-26 (carp not locatable, squids at 552 blocks); SQ-FSD-07 (s3_A) checks the sets again from the dump | from r1102b s3_skip.md |
| SQ-INV-09 | client | generators read the client dumps (food, stacks, names) and write pack files; hand step after CQ-DUMP (memory `dumpall-refresh-workflow`) | from r1102b s2_skip.md |
| SQ-INV-13 | client | Comfort-food count comes from the food dump (client-only) | from r1102b s2_skip.md |
| SQ-INV-15 | client | `/cegen run jei_world` / `gen_jei_world.py` is an in-game generator run on the instance (writes `generated.json`), after `/dumpworldjei` | from r1102b s2_skip.md |
| SQ-INV-16 | client | `/cegen run loot dumps` is an in-game generator run on the instance (writes `loot_index.json`, `loot_config.json`) | from r1102b s2_skip.md |
| SQ-INV-18 | stale | no `@GameTest` / `GameTestHolder` in any dev mod (grep `development/*/src`, members too) and `tools/time_table.py` does not exist | from r1102b s2_skip.md |
| SQ-KS-02 | recorded | queue Run log, 2026-10-25 Creative Delight run: "SQ-KS-01 to -04 PASS ... `kubejs/data` reloads" | from r1102b s1_skip.md |
| SQ-KS-03 | recorded | same line: "with both copies one runs and `kubejs/data` wins over the world datapack" | from r1102b s1_skip.md |
| SQ-KS-04 | recorded | same line: "`requires: not_a_mod` skipped" | from r1102b s1_skip.md |
| SQ-KS-05 | superseded | the row itself says "covered by SQ-CDT-01 and SQ-CDT-02"; both PASS in the 2026-10-25 Creative Delight run | from r1102b s1_skip.md |
| SQ-LR-01 | client | needs a played session with Distant Horizons generating (DH is a client mod) | from r1102b s3_skip.md |
| SQ-MG-16 | client | group C: JEI, textures, walking feel; the fan wash needs a Create fan setup by hand | from r1102b s3_skip.md |
| SQ-MG-20 | client | JEI search | from r1102b s3_skip.md |
| SQ-NDM-08 | needs player (old save) | needs a save made with the NethersDelight-1.19-3.1 jar | from r1102b s3_skip.md |
| SQ-PR-01 | client | needs a client with Pretty Rain to see rain particles and a client crash; the server half is SQ-PR-00 (in s1_A.qb) | from r1102b s1_skip.md |
| SQ-PS-02 | recorded | queue Run log, 2026-10-25 late run: "SQ-SN-01, SQ-PS-01, -02, SQ-DB-01 ... PASS" | from r1102b s1_skip.md |
| SQ-RD-12 | client | creative tab, textures, names, mod list, JEI | from r1102b s2_skip.md |
| SQ-RD-29 | client | models, tooltips, WTHIT crop line | from r1102b s2_skip.md |
| SQ-RD-30 | client | AppleSkin and JEI (food values are checked server-side in SQ-RD-17 / -21 / -22 by `/zq food`) | from r1102b s2_skip.md |
| SQ-RD-31 | superseded | cutover done: `rusticdelight-forge-1.19.2-1.3.0.jar` is in `development/creativeDelight/jar_archive/displaced/`, `mods/` holds no rusticdelight jar, the port is nested in `mods/creativedelight-1.0.0.jar`; an instance step, not a scratch row | from r1102b s2_skip.md |
| SQ-RD-32 | client | crop and medley stage textures, `template_crop_cross` / `platter.png` client log lines | from r1102b s2_skip.md |
| SQ-RDX-02 | client | JEI search and tooltip | from r1102b s3_skip.md |
| SQ-RDX-13 | needs player (old save) | needs a save with rusticdelight:pancake / fried_dough in a chest and a placed plate, made before the merge | from r1102b s3_skip.md |
| SQ-RSP-08 | needs player | Create pump rig at 16 and 256 rpm with tanks and pipes; "whole 250 mB steps, never above 1000" is watched over time (no probe reads the tank per tick); hand step or client run | from r1102b s2_skip.md |
| SQ-SK-03 | client | single-player world creation (`singleplayer` key of tracking.json); needs the integrated server of a client | from r1102b s1_skip.md |
| SQ-SNF-02 | recorded | queue Run log, 2026-10-26 afternoon run: "SQ-SNF-01, -02, -03 PASS ... the 5 season recipes present, none for sugar or `_on_corn_delight_popcorn`" | from r1102b s1_skip.md |
| SQ-STK-05 | client | `tools/stack_sizes.py` writes feature_writeup/stack_sizes.md from the instance's client /dumpstacks (names); the sizes are SQ-STK-02 to -04 | from r1102b s3_skip.md |
| SQ-SV-02 | client | queue: needs a client connected (block change packets) | from r1102b s3_skip.md |
| SQ-VTD4-09 | client | JEI Pickling Jar page, carrot cake cut face, Magic Peanut tree look | from r1102b s1_skip.md |
| SQ-WCD-05 | stale | "before the CD install, with the old creativedelight jar": the current CD jar is installed (`mods/creativedelight-1.0.0.jar`, 2026-11-02 14:01) and the old-jar state no longer exists | from r1102b s2_skip.md |
| SQ-WGE-00b | offline, hand step | no server: python3 tools/gen_nether_swaps.py, check_worldgen_json.py, gen_structure_groups.py --check, check_moobloom_data.py, node --check end_divers.js. gen_nether_swaps.py rewrites its JSON and feature_writeup/nether_swaps.md, so the runner (or owner) runs it by hand and checks git diff; not put in a batch | r1103 |
| SQ-WGE-09 | worldgen postponed | needs structure or biome generation in a fresh group B world (locate / place structure / census / biome mob counts) | r1103 |
| SQ-WGE-10 | worldgen postponed | needs structure or biome generation in a fresh group B world (locate / place structure / census / biome mob counts) | r1103 |
| SQ-WGE-11 | worldgen postponed | needs structure or biome generation in a fresh group B world (locate / place structure / census / biome mob counts) | r1103 |
| SQ-WGE-12 | worldgen postponed | needs structure or biome generation in a fresh group B world (locate / place structure / census / biome mob counts) | r1103 |
| SQ-WGE-13 | worldgen postponed | needs structure or biome generation in a fresh group B world (locate / place structure / census / biome mob counts) | r1103 |
| SQ-WGE-14 | worldgen postponed | needs structure or biome generation in a fresh group B world (locate / place structure / census / biome mob counts) | r1103 |
| SQ-WGE-15 | worldgen postponed | needs structure or biome generation in a fresh group B world (locate / place structure / census / biome mob counts) | r1103 |
| SQ-WGE-16 | worldgen postponed | needs structure or biome generation in a fresh group B world (locate / place structure / census / biome mob counts) | r1103 |
