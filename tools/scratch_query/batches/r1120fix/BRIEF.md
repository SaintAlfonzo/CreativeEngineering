# r1120fix: batch-fault fixes and pre-screen of the scratch queue (2026-11-20)

Owner request 2026-11-20: fix the batch faults of runs r1120 (r1120lr, r1120pot) and r1120nv; find rows that will
probably batch-fault when a run reaches them (stale data, moved ids, unloaded lanes, unset captures, retired output
formats) and fix them first; mark unneeded or superseded rows `@superseded`. Session "Scratch test batch faults"
coordinates; drive r1120nv is running live (session "Scratch tests breakdown" owns it) and boots servers from these
rows files as it goes.

## Inputs (this folder)

- `pending_steps.txt`: `STEP FILE ROWS` for every pending r1120nv drive step (rows comma-separated). Steps run in
  that order, a few minutes each; HH step 57 is about 3.6 h.
- `lint_pending.txt`: `qrun.py lint FILE --rows <step rows>` for each pending step (W stale, W unloaded, W var,
  E var, W waitlog, W cfgedit, W kjs, W boot).
- `deadids_pending.txt`: `qrowlint.py` dead-id warnings for the pending files. Structure, structure-set and
  structure-tag ids are often missing from the dumps although they exist: check `kubejs/exported/` (grep, never read
  whole) and the mod jars before calling one dead.
- `bf_rows.csv`: rows already judged BATCH-FAULT (run, id, file, cause). These already ran: after the fix they need
  a rerun. Section 8.1 of `feature_writeup/testing/scratch_run_2026-11-20.md` gives the r1120lr causes by group.

## Rules

1. Edit only the `.qb` files your prompt assigns you (one owner per file). Never edit `development/`, `kubejs/`,
   `data/`, `config/`, `configs_edited/` or any pack content. A probe script under `tools/` may be edited only when
   your files alone use it (grep `tools/` and `feature_writeup/testing/rows/` first); otherwise report it.
2. Live drive: before editing a file, read `perf/runs/r1120nv/drive/state.json`. Never edit the rows of the step whose
   status is `running` (other sections of that file are fine). Work in drive order: nearest pending step first.
   Do not change a row's or section's `@modset` / `@@modset`, `@@phase` or `@group` (the drive's step key holds
   them; a change makes the step select no rows): list the needed change in your report instead. Never edit
   `qdrive.py` (the drive owner's).
3. Row format and fields: `tools/scratch_query/qrows.py` docstring; skill `.claude/skills/scratch-test/SKILL.md`
   part A; verbs in `feature_writeup/creative_devtools.md` "/cdevtest and the perfBot test verbs"; gotchas in
   `.claude/skills/scratch-test/gotchas.md` (grep it). Row ids never change; no new `-NNb` ids for fixes.
4. Batch fault vs product: a row is fixed to match the CURRENT documented behavior (feature writeup, plan rulings,
   changelog `kubejs/assets/creativecommon/changelog/unreleased.md`, dumps in `kubejs/exported/`). When the writeup
   says X and the code or dumps say Y, or you cannot tell whether a change was intended, do not edit the
   expectation: list it as "suspected product FAIL" in your report.
5. Known fault classes and fixes:
   - `{SAH1..3}` / `{SBH1,3}` unset: these came from the retired r1103 home batches. Give the row its own home
     inside the row (rows are run alone with `--rows`): `> execute unless entity @e[type=minecraft:marker,tag=sahome]
     run summon minecraft:marker ~ ~ ~ {Tags:["sahome"]}`, `> data get entity @e[type=minecraft:marker,tag=sahome,limit=1] Pos`,
     `capture SAH entity data: \[(-?\d+)\.\d+d, (-?\d+)\.\d+d, (-?\d+)\.\d+d\]`, `> forceload add` over the boxes the
     row uses, `sleep 3`. Group B uses `sbhome` / SBH the same way. Model: `feature_writeup/testing/rows/CDJ.qb` (cdj5home).
   - E var / W var for other captures: move or repeat the capture into the row that uses it (rows run alone).
   - W unloaded: add `> forceload add X1 Z1 X2 Z2` over the row's boxes before the first block command (max 256
     chunks), `sleep 3` after.
   - Ids moved to `creativedelight:` / `creativecommon:` / another owning jar (memory rule: ids follow the owning
     mod since 2026-11-09): rewrite to the id in the dumps; tag paths like `food_type/*`, `food_family/*`.
   - Retired `/zq` probe output (`block tags of X:`, `[zq] ...`): `/cdevtest` prints `[cdevtest] verb=<verb> key=value
     ... done=1`, e.g. `verb=tagsof id=X block_tags=...`. Rewrite the regex for the cdevtest line.
   - Stale counts and values (stack sizes, recipe counts, slices): take the current number from the dump or writeup
     and cite it in the row's `@fails` or a comment line.
   - Regex anchors too loose or too tight: anchor on the field, narrow `done=0` rejects to the row's own verb.
6. W stale (row sources changed after `@reviewed`): read the source change (`git log -p --since=<review date> --
   <source>`, `git diff` for uncommitted) and compare with what the rows test. When the change does not touch the
   row's checks, set `@reviewed 2026-11-20` (row) or `@@reviewed 2026-11-20` (section, only when every row in it was
   checked). When it does, rewrite the row, then set `@reviewed`. Do not bulk-mark reviewed without reading the diff.
7. Superseded or unneeded rows: add `@superseded <why; replacement row ids or "feature removed"> (owner request,
   2026-11-20)` when a row tests a removed feature or mod, a pinned old jar set no longer shipped, a duplicate of a
   newer row that checks the same thing, or a case another section took over (e.g. SALT melt cases moved to SQ-MELT).
   qrun lists superseded rows and runs none; the drive counts them done. Do not delete rows (ids are verdict history).
   When unsure, do not supersede: list it as a candidate in your report.
8. W boot (row order vs @boot), W cfgedit, W kjs, W waitlog: fix when the fix is clear; otherwise report.
9. After editing a file: `python3 tools/scratch_query/qrun.py lint FILE` for the whole file AND, for every pending
   step of that file, `qrun.py lint FILE --rows <that step's rows>` (from pending_steps.txt). Fix every E. Then
   `python3 tools/scratch_query/qrowlint.py FILE`. Do not run `status_index.py`, git commands, or any server/client.
10. Rows from `bf_rows.csv`, and rows of steps that already ran (state done) that you change, go in your rerun list.

## Report (your final message, and a CSV)

Write `tools/scratch_query/batches/r1120fix/<GROUP>.csv` with header `id,file,kind,action,detail` where kind is one
of `bf-fixed` (already ran, needs rerun), `prefixed` (pending, fixed before it runs), `reviewed` (stale, checked, no
change), `superseded`, `candidate-supersede`, `suspected-product`, `unfixed` (with why). One row per test id.
Final message: counts per kind, the suspected product FAILs and supersede candidates one line each, any probe or
shared-tool change needed outside your files, lint status per file (E count must be 0).
