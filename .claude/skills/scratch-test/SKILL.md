---
name: scratch-test
description: Scratch server and client testing - write or refresh scratch queue rows, plan and execute a scratch run (the whole queue or one feature/phase) on throwaway servers and clients, report run status or ETA. Use for "write scratch tests", "update the scratch queue", "execute the scratch test queue", "scratch test X", "ETA on the scratch testing".
---

# Scratch test

Files: queue [scratch_queue.md](../../../feature_writeup/testing/scratch_queue.md), run docs
`feature_writeup/testing/scratch_run_<date>.md`, runner claim [RUNNER.md](../../../feature_writeup/testing/RUNNER.md),
gotchas [gotchas.md](gotchas.md) (read its "Before the first boot" section before booting, and the rest when anything
odd happens). Tools: `tools/scratch_query/` (prep_srv.sh, qrun.py, game_jvms.sh, zq_probe.js, sp_world.py),
`tools/creative_labels_test/scratch_server.sh setup|boot|cmd|stop OUT`, `tools/perf/session/perf_session.sh`,
`tools/perf/eval/eval_client.py`. Tool usage is in each script's header or docstring; do not retype it.

## Hard rules (every mode)

- **No boot unless asked.** Start a server or client only when the user asked for a run in this conversation. Without
  that, every live check goes into the queue (part A).
- **One game JVM at a time.** `bash tools/scratch_query/game_jvms.sh` must print nothing before every boot and every
  client launch (it also skips Gradle). Never kill a JVM you did not start; record your pids.
- **One runner.** Claim [RUNNER.md](../../../feature_writeup/testing/RUNNER.md) before preflight and release it at the
  end (B1, B7). If another session holds it, do not run: report that, and offer to queue or wait.
- **Scripts, probes, batch files live in `tools/`**, never only in the scratchpad (it gets wiped mid-run).
- Process command lines only with the token redacted (CLAUDE.md). Never `pkill -f` a string in your own command line.

## Part A: queue (write or refresh rows, nothing booted)

Use when a change needs a live check, or for "write scratch tests (but don't run them)", "ensure the queue is up to
date", "collect queued tests".

1. **Now or queue.** For each feature you changed, give the user 2 to 4 lines: "test now" (can break boot: mixin,
   registry, startup script, worldgen registry; hard to debug without this session's context; others depend on it;
   tiny and isolated) or "queue" (plain behaviour check; covered by a headless harness; fits a world group already
   queued; debuggable from the writeup). The user decides; silence = queue.
2. **Where.** New section at the end of the queue: `## <Feature>, <date> ([writeup](../x.md) section N, TODO
   `family #n`)`, with one intro line naming the jar or config state it targets (build time or version, and any
   jar that must be installed first). Rows the owner wants first go into the "Priority run" section. Add one line to
   the queue's header block ("<date>: added SQ-XX at the end, ...").
3. **Row format** (table, as the existing sections): `| ID | Group | Commands | Pass | If it fails |`.
   - ID: `SQ-<CODE>-NN` server, `CQ-<CODE>-NN` client (needs a window or a real player). Reuse the feature's code if
     it has rows already; continue its numbering.
   - Group: a world group from the queue's "World groups" table (A superflat, B normal world, B-nether, ...) or
     `client`. Worldgen-registry changes need a fresh world: say so.
   - Commands: exact console commands, probe commands, config edits (`qrun.py toml ...`). A new probe script goes in
     `tools/` and the row names it.
   - Pass: checkable facts (log regex, count, value), so `qrun.py` can judge it. Things only a person can judge are
     written as a look ("LOOK: ...").
   - If it fails: the files and the likely cause, so a later session does not start cold.
4. **Index it.** Test IDs into the feature writeup's test list; README status "built, scratch test queued"; the TODO
   row names the IDs. Only write `.qb` batch files when asked to build a run (part B2).
5. **Refresh** ("is the queue up to date"): compare `kubejs/assets/creativeengineering/changelog/unreleased.md`,
   recently changed writeups and the latest run doc with the queue. Every player-visible bullet has a row; rows whose
   feature changed get rewritten; superseded rows say "superseded by X"; results stay in the Run log, never the rows.
   Update the "Jars in `mods/` now" line from `ls -l mods/` and the build outputs.

## Part B: run

Use for "execute the scratch queue", "scratch test phase N of X", "run the worldgen rows". The prompt sets the scope;
everything below is the default unless the prompt says otherwise.

### Run defaults

1. **Debug blockers as they come up**: probes, batch regexes, tooling, boot crashes. Fix tooling freely (in
   `tools/`).
2. **No gameplay changes to make a row pass.** A temporary change for a test (config value, scratch datapack, srv-only
   script) lives in the scratch `srv/` only, or is reverted before the run ends. A real bug fix that changes
   behaviour is a finding: report it and add a TODO row; fix it only if the user said to.
3. **Unclear next step**: do what the latest run doc that did the same kind of step did (run docs in
   `feature_writeup/testing/`, newest first), and note the choice.
4. **Perf capture on** for every bulk run: `perf_session.sh start <run id>` before the first boot, JFR on (never
   `CE_PERF=0`), clients get `jcmd <pid> JFR.dump` before they close, `report` without `--prune`. A short targeted run
   (one feature or one carve) may skip the perf session; say so in its run notes.
5. **Dumps and generators first** when any row or TODO needs them: `/dumpall` early, copy back only the server-safe
   dumps (queue setup notes), run the generators the user named.
6. **Order**: queue "Priority run" first, then server rows cheapest first, then worldgen (fresh world per group),
   then client rows, then long operations (pregen, perf suite, resource pack bench). Pinned owner notes at the top of
   the queue override this (for example "client rows wait for wave 8").
7. **Cheap extras**: a queued row that fits the world and jars already up may run alongside; log it.
8. **Skip waits**: `/time add`, `gamerule randomTickSpeed`, shorter config intervals in the srv toml (`qrun.py toml`)
   instead of sleeping. 1.19.2 has no `/tick` command.
9. **Notes as you go**: append to the run doc's "Run notes" at every boot, batch end, FAIL and decision, with clock
   times. Enough for a fresh session to resume if this one stops.
10. **Stop and report** on a boot failure caused by our jars that you cannot fix inside the run defaults.

### B1. Claim and preflight

- Read RUNNER.md. `State: idle` (or `running` with no game JVM up and `Updated` over 2 h old, after asking the user):
  write your claim (format in the file). Update `Phase`, `ETA`, `Updated` at every phase boundary.
- `game_jvms.sh` empty; `df -h /` (stop below about 10 GB free); jar check: `ls -l --time-style=+%m-%d_%H:%M mods/`
  against `development/*/build/libs/` for the jars the rows need. prep_srv.sh swaps in build jars newer than
  `mods/`; a jar the rows need that is in neither: report it, those rows wait.
- Read the queue header (pinned notes, newest additions), its "World groups" and setup notes, and the latest run
  doc's notes.

### B2. Plan

- **Bulk run** (most of the queue): new `scratch_run_<YYYY-MM-DD>.md` with sections `0. Performance capture`,
  `1. Scope` (taken and not taken), `2. Tools`, `3. Jars` (table: jar, `mods/`, build/libs, rows that need it),
  `4. Order` (step, setup, run, time estimate), `5. Row map` (queue section, rows, batch), `6. After the run`,
  `7. Run notes`, `8. Final verdicts`. Model: [scratch_run_2026-10-31.md](../../../feature_writeup/testing/scratch_run_2026-10-31.md).
- **Targeted run** (one feature, phase or carve): a new section in today's run doc (or a new doc): owner request,
  batches, results dir, jars, boots, then the verdict table. Model: scratch_run_2026-11-01.md sections 6 and 7.
- Batches: `tools/scratch_query/batches/r<MMDD>/*.qb` (format in `qrun.py`'s docstring), one file per boot. Results:
  `perf/runs/<run id>/`.
- Tell the user the scope and the time estimate in 3 to 6 lines, then start (no need to wait unless scope is unclear).

### B3. Execute

- Per world: `prep_srv.sh OUT LEVEL A|B [prop=value ...]` (OUT under the scratchpad, `cd "$OUT" || exit 1` in
  multi-step commands), srv toml edits, `scratch_server.sh boot OUT`, wait for `Done (` in `OUT/srv/logs/latest.log`
  (or a crash report) with a Monitor or background wait, not sleep loops. Then `qrun.py run OUT BATCH RES`.
- `scratch_server.sh stop OUT`, then wait until `game_jvms.sh` is empty before the next boot.
- Client block: `eval_client.py setup/launch`, `sp_world.py` to copy scratch worlds in, `qrun.py client`, xdotool for
  hand steps, screenshots to `RES/shots/`.
- Redirects from the user or another session (defer a block, stop): stop at a row boundary, discard a half-run row,
  mark unrun rows "deferred" in the run notes, update RUNNER.md.

### B4. Judge

Turn every LOOK into PASS or FAIL from `RES/rows/<ID>.log`, `rows/<ID>.shell.txt` and `shots/`. A failure caused by
the batch (wrong regex, wrong coordinates) is `BATCH-FAULT`: fix the batch and rerun when cheap. Verdict table in the
run doc: `| ID | Result | Evidence |`.

### B5. Write back

1. Queue: one Run log entry at the top of "Run log" (run id, worlds, seed, perf session, jars, counts, link to the run
   doc); update the header's "Latest" pointer; results stay out of the rows.
2. Feature writeups: results into each test list; README status "built, scratch server PASSED" where all rows passed.
3. FAILs and found bugs: TODO rows (family table) with the test ID; fixes that change behaviour wait for the owner.
4. New gotchas: add them to [gotchas.md](gotchas.md) under their topic (not memory).
5. `perf_session.sh stop` and `report` (no server up while it parses); perf summary path in the Run log entry.

### B6. Hand over

Counts (PASS / FAIL / LOOK left / deferred), the FAILs with one line each, what is deferred and why, which client or
hand steps remain for the user (exact CQ IDs), and any jar the rows need that is not in `mods/`.

### B7. Release

All your JVMs stopped (`game_jvms.sh` empty), RUNNER.md back to `State: idle` with a one-line "Last run" entry.

## Part C: status

For "ETA", "how far is the scratch testing", or a request relayed from another session: read RUNNER.md (it names the
runner session and the run doc). As the runner, reply with: (1) overall ETA with the clock time; (2) each phase:
done / running / pending, time estimate, PASS-FAIL counts for done phases; (3) the row running now; (4) FAILs and
blockers so far, one line each. As another session: read RUNNER.md and the run doc's notes, or message the session
it names (agent-message rules in CLAUDE.md); do not broadcast to every session.
