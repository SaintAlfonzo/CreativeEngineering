# r1118 judge brief (orchestrator 883b649d, 2026-11-18)

Run goal (owner, 2026-11-18): bug discovery and documentation, not fixing. A detached `qrun.py drive r1118` runs 832
server rows in 167 steps (state perf/runs/r1118/drive/state.json); judge subagents triage finished steps; the
orchestrator makes Java/content fixes that block testing and writes the run doc.

Read first: CLAUDE.md, .claude/skills/scratch-test/SKILL.md (part B4), .claude/skills/scratch-test/gotchas.md, then
the earlier judge reports in this folder (j1.md .. latest, rowfix1.md). Known causes already found: setup rows lost in
the move to rows/*.qb (bot spawns restored by rowfix_spawns.py, `@@prep` section key added); sections that need
multi-boot, chain mod sets (port_chain.sh), CC-only sets (cc_only_srv.sh) or legacy-config fixtures still lack setup;
stale ids after the 2026-11-09 namespace moves; setup-row forceloads lost ("That position is not loaded", mobs or items into unloaded chunks: add a forceload to the row); captures set only by rows outside the step ({H}, {PL}, {FO}: give the row its own setup); group B rows built at y -60 (move to group A); `/cetime` -> `/ccaltime`.

Inputs per step index NN: results perf/runs/r1118/sNN_res, server perf/runs/r1118/sNN/srv/logs, driver log
perf/runs/r1118/drive/sNN_qrun.txt. Start with `python3 tools/scratch_query/qrun.py table <RES dirs>`.

Method:
- FAILs explained by a known cause: confirm from the row log, reclassify BATCH-FAULT with a one-line cause; do not
  rebuild multi-boot or fixture setups.
- Effort goes to FAILs not explained by known causes: real bug vs BATCH-FAULT vs HARNESS from RES/rows/ID.log,
  ID.shell.txt, the server log. Grep every server log for `Error occurred while handling event`, crash reports and
  exceptions naming our mods (creative*, powergrid, storagedelight, tempad).
- LOOKs: `qrun.py judge RES ID N VERDICT "evidence"` (text evidence only).
- BATCH-FAULT: fix the row in rows/<CODE>.qb when the fix is clear (keep the id), `qrun.py judge RES ID --reclassify
  BATCH-FAULT --cause "judge-N, 2026-11-18, why"`, `qrun.py lint` the .qb (0 E).

Allowed: rows .qb edits (never MELT.qb: another session's uncommitted work). tools/scratch_query edits only if your
prompt says you own tools this round (backward compatible, py_compile; the driver calls qrun.py per step).
Not allowed: booting servers or clients, drive/state.json, Java/KubeJS/config/gameplay edits, git commits, the run
doc, status_index.py.

Output: perf/runs/r1118/judge/jN.md with a verdict table `| ID | Result | Evidence |`, "Findings" (real bugs: row id,
one-line symptom, likely file/cause, suggested TODO family), "Batch faults" (row, fixed or why not), "Tool fixes",
"Pass up". Append the fixed rerun-worthy row ids as one space-separated line to perf/runs/r1118/judge/rerun.txt.
Final reply under 12 lines: counts, real findings one line each, pass-up items.
