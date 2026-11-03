# r1102b slice 1: rows not batched

The scope tool (`unrun_rows.py`) only sees full row ids, so rows recorded with range notation in the queue Run log
("SQ-CDT-02, -04, -05", "SQ-KS-01 to -04") show as unrun. Those are listed here as "recorded".

| ID | Reason | Evidence |
|---|---|---|
| SQ-MDP-08 | superseded (recorded) | run r1102 split it into SQ-MDP-08a to -08h (`batches/r1102/b1_main.qb`), results in `perf/runs/r1102/b1/results.json` (08c FAIL, TODO `creativedelight/members`) |
| SQ-PR-01 | client | needs a client with Pretty Rain to see rain particles and a client crash; the server half is SQ-PR-00 (in s1_A.qb) |
| SQ-SK-03 | client | single-player world creation (`singleplayer` key of tracking.json); needs the integrated server of a client |
| SQ-SNF-02 | recorded | queue Run log, 2026-10-26 afternoon run: "SQ-SNF-01, -02, -03 PASS ... the 5 season recipes present, none for sugar or `_on_corn_delight_popcorn`" |
| SQ-PS-02 | recorded | queue Run log, 2026-10-25 late run: "SQ-SN-01, SQ-PS-01, -02, SQ-DB-01 ... PASS" |
| SQ-KS-02 | recorded | queue Run log, 2026-10-25 Creative Delight run: "SQ-KS-01 to -04 PASS ... `kubejs/data` reloads" |
| SQ-KS-03 | recorded | same line: "with both copies one runs and `kubejs/data` wins over the world datapack" |
| SQ-KS-04 | recorded | same line: "`requires: not_a_mod` skipped" |
| SQ-KS-05 | superseded | the row itself says "covered by SQ-CDT-01 and SQ-CDT-02"; both PASS in the 2026-10-25 Creative Delight run |
| SQ-CDJ-06 | recorded | queue Run log, 2026-10-25 Creative Delight run: "SQ-CDJ-01, -06 PASS (server half) ... End's gristle 3 and 5 from 10 knife kills"; the client mod-list half is client work |
| SQ-CDJ-08 | client | mod list, config screen, cooking pot GUI and JEI categories |
| SQ-CDT-04 | recorded / jar missing | Run log: "SQ-CDT-02, -04, -05 PASS: ... ns 11/11, requires 2/2, tags 1/1, config 5/5"; a rerun needs `creativedelight-1.0.0-cdtest.jar` (`-Pcd_test_suite` build), not in `development/creativeDelight/build/libs/` |
| SQ-CDT-05 | recorded / stale | Run log: "mirror 57/57 (30 helpers)"; tools/creative_delight_test/README.md: the mirror is frozen since the cooking scripts moved into the CD tree, `--check` reports it stale by design |
| SQ-VTD4-09 | client | JEI Pickling Jar page, carrot cake cut face, Magic Peanut tree look |
| SQ-FDB-08 | needs player | skillet use in the air underwater with beef in the off hand: perfBot has block use only (/zq useitem has no off-hand setup), and the pass is an action-bar message (client) |
| SQ-FDB-12 | client | particles, models, item icons, tooltips, the rice placement message |
