---
name: scratch-test
description: Scratch server and client testing - write or refresh scratch queue rows, plan and execute a scratch run (the whole queue or one feature/phase) on throwaway servers and clients, report run status or ETA. Use for "write scratch tests", "update the scratch queue", "execute the scratch test queue", "scratch test X", "ETA on the scratch testing".
---

# Scratch test

Files: queue [scratch_queue.md](../../../feature_writeup/testing/scratch_queue.md), run docs
`feature_writeup/testing/scratch_run_<date>.md`, runner claim [RUNNER.md](../../../feature_writeup/testing/RUNNER.md),
gotchas [gotchas.md](gotchas.md) (read its "Before the first boot" section before booting, and the rest when anything
odd happens). Tools: `tools/scratch_query/` (prep_srv.sh, qrun.py, game_jvms.sh, sp_world.py, qprescreen.py (probable batch
faults in a plan or drive), qrerun.py (rerun list -> drive plan); `zq_probe.js` is retired since 2026-11-16 and no longer copied, its verbs are `/cdevtest` verbs),
**`/cdevtest` and the `/pbot` test verbs** (Creative Devtools, in every prep_srv server: block, inventory, tank, box
count and ages, use / break / kill / loot as a player or fake player, entity and player reads, recipe and tag data,
`tickwarp`, `randomtick`, `watch`, `layerdump`, season types, floating sand, the earth fill `under` judge, item NBT; `/cdevreload compat` re-reads Compat's toml; bots attack, interact, eat with a real ticked use, sneak, look, walk with physics,
inventory, vulnerable, pickup; verb table in [creative_devtools.md](../../../feature_writeup/creative_devtools.md)
"/cdevtest and the perfBot test verbs"). New rows use them before writing a KubeJS probe; a probe is for a one-off
feature reader only. Output `[cdevtest] verb=<verb> key=value ...`, every call ends `done=1` or `error=... done=0`.
`tools/creative_labels_test/scratch_server.sh setup|boot|cmd|stop OUT`, `tools/perf/session/perf_session.sh`,
`tools/perf/eval/eval_client.py`. Tool usage is in each script's header or docstring; do not retype it.

## Hard rules (every mode)

- **No boot unless asked.** Start a server or client only when the user asked for a run in this conversation. Without
  that, every live check goes into the queue (part A).
- **One game JVM at a time.** `bash tools/scratch_query/game_jvms.sh` must print nothing before every boot and every
  client launch (it also skips Gradle). One exception (owner ruling 2026-11-16): a run may have its own dedicated
  server plus its own client up together for rows that need both (`eval_client.py launch ... ownsrv=PID`, `qrun.py
  pair`); then the only JVM listed before the client launch is that server. Never kill a JVM you did not start;
  record your pids.
- **One runner.** Claim [RUNNER.md](../../../feature_writeup/testing/RUNNER.md) before preflight and release it at the
  end (B1, B7). If another session holds it, do not run: report that, and offer to queue or wait.
- **Scripts, probes, batch files live in `tools/`**, never only in the scratchpad (it gets wiped mid-run).
- Process command lines only with the token redacted (CLAUDE.md). Never `pkill -f` a string in your own command line.

## Part A: queue (write or refresh rows, nothing booted)

Use when a change needs a live check, or for "write scratch tests (but don't run them)", "ensure the queue is up to
date", "collect queued tests".

1. **Now or queue.** For each feature you changed, give the user 2 to 4 lines: "test now" (can break boot: mixin,
   registry, startup script, worldgen registry; hard to debug without this session's context; others depend on it;
   tiny and isolated) or "queue" (plain behavior check; covered by a headless harness; fits a world group already
   queued; debuggable from the writeup). The user decides; silence = queue.
2. **Where.** Rows are data (plan_process_overhaul.md P2.1): a new `@@section` in
   `feature_writeup/testing/rows/<CODE>.qb` (a new file for a new code; key = the file's highest + 1). Fields:
   `@@title <SQ-CODE> <Feature>, <date> ([writeup](../x.md) section N, TODO `family #n`)`, `@@date`, `@@jars` (own
   mod ids the rows test: they drive `qrun.py plan`), `@@sources` (the writeup and source paths the rows were written
   from: they drive the stale lint), `@@tier` (T2 default; T1 = a jar's smoke rows, T3 = release gate), `@@phase`
   (run order, step 6: `worldgen` for a section that tests world generation; a single row overrides with `@phase
   worldgen` when it uses `locate` / `cdevplace` / `place` or needs a fresh world, `@phase long` for pregen, perf suite,
   resource pack bench or a row over 30 min; client rows need no tag), one `@@text`
   intro line naming the jar or config state it targets, then `@@table` (or `@@table world` when the second column
   names the world a row needs) and the rows. Format: `qrows.py` docstring.
   Then `python3 tools/status_index.py` renders the section into scratch_queue.md (never edit inside the queue's
   generated blocks). Rows the owner wants first: say so in the hand-over (`qrun.py plan --code CODE`).
3. **Row format**: `## ID | short title`, then the table cells `@group`, `@commands`, `@pass`, `@fails` (one line
   each, as `| ID | Group | Commands | Pass | If it fails |` shows them), then the row's qrun steps. Write the steps
   now when you can (they are written once and every later run uses them); otherwise `@draft <why>` and the first
   runner writes them. `@owner true` for design-feel rows only the owner judges (playtest list).
   - ID: `SQ-<CODE>-NN` server, `CQ-<CODE>-NN` client (needs a window or a real player). `<CODE>` is the feature's prefix from
     [test_prefix_index.md](../../../feature_writeup/testing/test_prefix_index.md) (a new feature adds its row there first);
     continue its numbering.
   - Group: a world group from the queue's "World groups" table (A superflat, B normal world, B-nether, ...) or
     `client`. Worldgen-registry changes need a fresh world: say so.
   - Commands: exact console commands, probe commands, config edits (`qrun.py toml ...`). A new probe script goes in
     `tools/` and the row names it.
   - Pass: checkable facts (log regex, count, value), so `qrun.py` can judge it. Things only a person can judge are
     written as a look ("LOOK: ...").
   - If it fails: the files and the likely cause, so a later session does not start cold.
   - Restart inside a row: `edit CMD` lines (run in `OUT/srv` while the server is down: `qrun.py toml`, a jar swap),
     then `restart [SECS]` (stop, edits, boot, wait for `Done (`; the row and the batch go on on the new boot; the old
     log stays in `srv/logs/qrun_bootN.log`). Never `> stop` in a row that needs the server back: it ends the batch.
   - Boot phases on one world: `@boot N` on the row (default 1) and `@@prep [A:|B:] boot=N: SPEC` section lines (props,
     `toml`, `copy`, `rm`, `run CMD`, `fresh`) for the change before boot N; untagged `@@prep` lines are boot 1's
     (`toml FILE - KEY VALUE` and `qrun.py toml FILE - KEY VALUE` set a top-level key, before the first `[section]`). The
     driver runs boots 1..N in order on one folder; a planned boot-N row brings its section's lower-boot rows along.
     `@@prep run CMD` is a logged shell step in `OUT/srv` (`$INST`, `$OUT`, `$SRV`, `$BOOT`), for fixtures a prep
     action cannot express (a `git archive` config tree, a jar swap). Format: `qrows.py` docstring.
   - Reduced or chain jar sets: `@@modset NAME [VARIANT] [+GLOB|+@GROUP] [-GLOB|-@GROUP] [KEY=VALUE]` on the section
     (`@modset SPEC` on a row, `@modset none` for the full pack; `@@prep boot=N: modset SPEC` switches sets between
     boots, world kept). Manifests: `tools/scratch_query/modsets/*.toml` (`qmodset.py list`, `show SPEC`; format in the
     qmodset.py docstring); a new set is a new manifest, not a new shell script. A jar that must report another
     version (a gate test) is rewritten by `@@prep run python3 "$INST/tools/scratch_query/jar_modver.py" "mods/X-*.jar" V`
     (a copy renamed over the hard link; SQ-CMV-03).
4. **Index it.** Test IDs into the feature writeup's test list; README status "built, scratch test queued"; the TODO
   row names the IDs. `qrun.py lint feature_writeup/testing/rows/<CODE>.qb` (batch lint plus stale rows and dead ids,
   `qrowlint.py`); fix every E. The rows file is the batch: no separate copy in `batches/`.
5. **Refresh** ("is the queue up to date"): compare `kubejs/assets/creativecommon/changelog/unreleased.md`,
   recently changed writeups and the latest run doc with the queue. Every player-visible bullet has a row;
   `python3 tools/scratch_query/qprescreen.py --plan-json PLAN` sorts a plan's rows into probable batch faults,
   risks and stale rows (fix the probable ones first);
   `python3 tools/scratch_query/qrowlint.py` lists rows whose sources changed after they were reviewed (rewrite them,
   then set `@reviewed <date>` on the row or `@@reviewed` on the section) and ids gone from the dumps; superseded rows
   get `@superseded <replacement>`; ids a row names on purpose although the dumps lack them (a negative test's
   removed ids, an old-jar boot's old ids, ids registered after the last `/dumpall`) go in `@absent <ids> (why)`,
   which `qrowlint.py` skips; results stay in the run docs and verdict files, never the rows.
   Update the "Jars in `mods/` now" line from `ls -l mods/` and the build outputs.

## Part B: run

Use for "execute the scratch queue", "scratch test phase N of X", "run the worldgen rows". The prompt sets the scope;
everything below is the default unless the prompt says otherwise.

### Run defaults

1. **Environment faults stop the run; fix them, then resume.** An environment fault is a defect in the test setup,
   not in the feature: tooling, batch steps or regexes, prep, jar set or pinning, world or fixture, boot, perf
   capture, or a verb that misbehaves. When one would invalidate or fail other rows (same cause likely to hit the
   next rows), stop at the row boundary instead of running on, and do not judge the affected rows FAIL. Then: find
   the cause from the logs, fix it, record it in the run notes (cause, fix, which rows to rerun), rerun the affected
   rows, and continue. A fault confined to one row: `BATCH-FAULT` (B4), carry on. Same fault twice after fixes:
   stop and report.
2. **Fix only the environment; analyze the rest later.** Allowed edits: `tools/` (scratch tooling), the row steps in
   `.qb` and run batches, and the scratch `srv/`/`OUT` (props, tomls, jars, world). Not allowed to make a row pass or
   to clear a blocker: anything under `development/`, `kubejs/`, `data/`, `configs_edited/`, tracked `config/`, or
   any other pack code or content. A product bug (wrong behavior, a FAIL with the environment sound, a boot crash
   from our jars) is a finding: log it with evidence (test ID, log lines, suspected file), add a TODO row, run the
   rows that do not depend on it, and leave analysis and the fix to a later session unless the user said to fix it.
   A temporary test change (config value, scratch datapack, srv-only script) lives in `srv/` only, or is reverted
   before the run ends. When unsure whether a fault is environment or product, treat it as product: report, do not edit.
3. **Unclear next step**: do what the latest run doc that did the same kind of step did (run docs in
   `feature_writeup/testing/`, newest first), and note the choice.
4. **Perf capture on** for every bulk run: `perf_session.sh start <run id>` before the first boot, JFR on (never
   `CE_PERF=0`), clients get `jcmd <pid> JFR.dump` before they close, `report` without `--prune`. A short targeted run
   (one feature or one carve) may skip the perf session; say so in its run notes.
5. **Dumps and generators first** when any row or TODO needs them: `/dumpall` early, copy back only the server-safe
   dumps (queue setup notes), run the generators the user named.
6. **Order** (owner, 2026-11-18): queue "Priority run" first, then server rows cheapest first, then any long
   operation (pregen, perf suite, resource pack bench) that a pending change is waiting on (a TODO row in Testing or a
   queued row that needs its result), then worldgen rows (fresh world per group), then client rows, then the remaining
   long operations. `qrun.py plan` and `drive` print and run in this order from each row's phase (`@@phase` /
   `@phase` in the .qb; a `long` row becomes `longwait` when a Testing TODO row's queue column cites it).
   Pinned owner notes at the top of
   the queue override this (for example "client rows wait for wave 8").
7. **Cheap extras**: a queued row that fits the world and jars already up may run alongside; log it.
8. **Skip waits**: `/cdevtest tickwarp N` (full ticks back to back), `/cdevtest randomtick BOX N` (no player needed),
   `/time add`, `gamerule randomTickSpeed`, shorter config intervals in the srv toml (`qrun.py toml`)
   instead of sleeping. 1.19.2 has no `/tick` command (tickwarp replaces it).
9. **Notes as you go**: append to the run doc's "Run notes" at every boot, batch end, FAIL and decision, with clock
   times. Enough for a fresh session to resume if this one stops.
10. **Stop and report** on a boot failure caused by our jars (a product bug, never patched in-run), or an
    environment fault you cannot fix within rule 2. Report: symptom, evidence, rows blocked, rows safe to run.
11. **Fix a simple batch fault and rerun it in-run** (owner, 2026-11-20). The runner, or a judge agent working for
    it, fixes a `BATCH-FAULT` row on the spot when the fix is simple, then reruns the row in the same run; the row
    keeps its id and the rerun is verdict history. **Simple** means all of: the cause is proven from the row's own log
    in about 10 minutes; the fix stays inside the row's steps in its `.qb` (regex or anchor, coordinates, a
    `forceload`, a capture moved into the row, an id renamed to the one in the dumps, a count or value taken from the
    dump or writeup, a wait length) or a few lines of a probe only that row's file uses; the expected behavior is
    the documented one (writeup, plan ruling, changelog), so the edit follows the docs and never just the observed
    output; and `qrun.py lint FILE --rows ID` is clean after it. **Not simple**, judge `BATCH-FAULT`, write the cause
    and carry on: a change to `@modset` / `@@modset`, `@@phase`, `@group` or a section's `@@prep` that other rows
    share; a new probe or a tool change outside the row; docs and code that disagree (that is a product finding,
    rule 2); a second fault on the same row after one fix.
    Rerun: when the step's server is still up (a hand-driven `qrun.py run`), `qrun.py run OUT FILE RES --rows ID` on
    it right away. Under a drive (the server stops at the step's end), `qrerun.py --add perf/runs/RUN/rerun.txt ID`;
    at a step boundary, or at the end of the drive, `qrerun.py perf/runs/RUN/rerun.txt` writes a plan, then
    `qrun.py drive start RUNr --plan-json ...` runs it (stop the main drive at a row boundary first, `drive resume`
    it after). Log each fix in the run notes: `BF-FIX ID: cause -> fix; rerun RESULT`. The same fix applies to
    later rows that will hit the same cause: fix those before the drive reaches them (`qprescreen.py RUN`). A row
    that cannot run in this drive at all (a release gate, a missing payload): append its id to
    `perf/runs/RUN/drive/skip.txt` (read at each step start, no restart) and say why in the run notes.
    Parallel sessions: one owner per `.qb` file at a time (say which files in the run notes or by message); never
    edit the rows of the drive step that is running (`state.json` status `running`), and tell the drive owner
    before any change to a row's `@modset`, since the step key holds it.

### B1. Claim and preflight

- Read RUNNER.md. `State: idle` (or `running` with no game JVM up and `Updated` over 2 h old, after asking the user):
  write your claim (format in the file). Update `Phase`, `ETA`, `Updated` at every phase boundary.
- `python3 tools/check_all.py --fast` exits 0 (an offline FAIL is fixed or reported before a scratch boot).
- `game_jvms.sh` empty; `df -h /` (stop below about 10 GB free); jar check: `python3 tools/jarstamp.py pending` and
  `python3 tools/jarstamp.py diff mods/ <srv>/mods` for the jars the rows need; record the stamps in the run doc. prep_srv.sh swaps in build jars newer than
  `mods/`; a jar the rows need that is in neither: report it, those rows wait. prep_srv.sh then pins the own jars
  (real copies, `OUT/pinned.json`; `qpin.py`); `qrun.py run` refuses a server whose own jars are links, changed after
  prep, or lack a mod_sides.md row. `--no-pin` only with the owner's OK; it is recorded in every verdict file.
- Read the queue header (pinned notes, newest additions), its "World groups" and setup notes, and the latest run
  doc's notes.
- **Pre-screen the plan before the first boot**: `qprescreen.py --plan-json PLAN` (or `qprescreen.py RUN` for a
  drive already planned) lints every step with its own row selection and sorts findings into likely (unset
  captures, unloaded coordinates, dead ids, retired `/zq` output, any E), risk and stale. Fix the likely rows first
  (run default 11's fixes, in the `.qb`); stale rows near the front of the plan get their source diff read and are
  rewritten or marked `@reviewed`. r1120nv skipped this and lost 20 rows to unset home captures in its first 10 steps.

### B2. Plan

- **Bulk run** (most of the queue): new `scratch_run_<YYYY-MM-DD>.md` with sections `0. Performance capture`,
  `1. Scope` (taken and not taken), `2. Tools`, `3. Jars` (table: jar, `mods/`, build/libs, rows that need it),
  `4. Order` (step, setup, run, time estimate), `5. Row map` (queue section, rows, batch), `6. After the run`,
  `7. Run notes`, `8. Final verdicts`. Model: [scratch_run_2026-10-31.md](../../../feature_writeup/testing/scratch_run_2026-10-31.md).
- **Targeted run** (one feature, phase or carve): a new section in today's run doc (or a new doc): owner request,
  batches, results dir, jars, boots, then the verdict table. Model: scratch_run_2026-11-01.md sections 6 and 7.
- **What to run: `qrun.py plan`** (qplan.py docstring). Default: T1 smoke rows of every own jar whose stamp changed
  since its rows last passed, plus the T2 rows whose jars or sources changed. `--never` / `--failed` add those rows;
  `--all` (whole queue) only when the owner asks; `--release` = T3 rows + T1 of every jar + `qrun.py playtest` (owner
  checklist). It prints one `qrun.py run OUT rows/<CODE>.qb RES --rows ...` per section, the drafts that need steps
  first (write them into the .qb once) and the stale rows to rewrite first. `qrun.py states` shows row states.
- **Rows files are the batches**: `qrun.py run OUT feature_writeup/testing/rows/<CODE>.qb RES --rows ...`
  (`--section K`, `--tier T1`). Run-only setup (fixtures, world prep rows) still goes in
  `tools/scratch_query/batches/r<MMDD>/*.qb`. Multi-boot server runs: `qrun.py drive start RUN [plan options]`
  (qdrive.py: one prep + boot per section and world group, state in `perf/runs/RUN/drive/`, RUNNER.md kept current,
  `drive resume RUN` after a dead runner, a watchdog stops the server if the driver dies). Results:
  `perf/runs/<run id>/`. Before any boot, `qrun.py lint BATCH` (codes in `qlint.py`; fix every E, read the W) and,
  for a review by a second agent, `qrun.py dry BATCH` (commands with vars substituted, lint inline, time estimate).
  A rerun with `--rows` / `--from`: lint with the same selection (it reports captures and markers in skipped rows);
  `--res RES` of an earlier run tests each waitlog against the replies earlier rows really got.
- Tell the user the scope and the time estimate in 3 to 6 lines, then start (no need to wait unless scope is unclear).

### B3. Execute

- Per world: `prep_srv.sh OUT LEVEL A|B [prop=value ...]` (OUT under the scratchpad, `cd "$OUT" || exit 1` in
  multi-step commands), srv toml edits, `scratch_server.sh boot OUT`, wait for `Done (` in `OUT/srv/logs/latest.log`
  (or a crash report) with a Monitor or background wait, not sleep loops. Then `qrun.py run OUT BATCH RES`. Its crash
  watch (dead pid, `hs_err_pid*.log`, new `crash-reports/`) ends the row CRASH within seconds and copies the
  evidence to `RES/crash/ID/`; with `--relaunch CMD` (and `--ready`) the game is restarted and the batch goes on
  (qrun.py docstring). A qrun started from a shell that cannot see the game's pids watches files only (it says so).
- `scratch_server.sh stop OUT`, then wait until `game_jvms.sh` is empty before the next boot.
- **Stall watch.** Every foreground wait is capped at `timeout 590`; when one returns without its target line, do a
  check instead of waiting blind, at 10-minute intervals (chain waits; no shorter: each poll re-reads the whole
  context). Interaction = a newer mtime on `OUT/srv/logs/latest.log` or on the newest `RES/rows/*.log` within the
  last 10 minutes. If the server was interacted with in the last 10 minutes, do not check at all: go back to waiting.
  Otherwise, per check, in order: what the current row is doing (a rare-meta `/locate` or pregen can be quiet for
  30+ minutes legitimately); `ls OUT/srv/crash-reports/` (move old ones out first); `game_jvms.sh` (never `ps -f` /
  `pgrep -af`); `tail -n 5` of the log. Then: dead JVM or crash report = judge the row as a crash and move on; JVM
  alive but still silent on the next check = one `jstack` of the scratch pid, record it in the run notes; still
  silent after that (30 minutes of no interaction) = ask the user before `kill -9`, since a kill discards the row.
  Do not run `jstack` as a routine check (it pauses the JVM). **Cap: stop polling after 12 hours** counted from the
  first check of the run: stop waiting, leave the server as is, write the state to RUNNER.md and the run notes, and
  report to the user.
- Client block: start the client with `tools/client_capture/run_captures.py serve [--game DIR] [--world NAME]`
  (capture driver serving `GAME/cdev_channel/`, protocol in client_capture.md 2.11), then `qrun.py client GAME BATCH
  RES`: when `GAME/cdev_channel/` exists qrun drives the driver (commands, `key`, `click`, `shot`, `do JSON` run
  in-process, tick-exact, no focus or typing loss; shots go to `test_captures/judgement/` for the owner); `--xdotool`
  forces the old window mode (`eval_client.py setup/launch`, `sp_world.py`, xdotool hand steps, `RES/shots/`).
  `serve --drop GLOB` / `--config FILE:SECTION:KEY=VALUE` change the client per launch; `--display xvfb` is
  experimental (TX-07), only for blocks that cannot run on the real window, never color rows.
- Client + own server: `qrun.py pair OUT GAME BATCH RES [--drop ...] [--config ...]` (`srv>` / `cli>` lines) and
  `qrun.py matrix SPEC RES [--plan]` (specs in `feature_writeup/testing/matrix_specs/`; outcomes JOINED / REFUSED /
  CRASH-* / HANG, a join is held 30 s, `hold_secs`, so a crash after joining is not a PASS). Refusal regexes are the
  Forge 43.3.9 strings; a refusal read as HANG means a missing string: add it to `qpair.py` from the case's logs.
- Redirects from the user or another session (defer a block, stop): stop at a row boundary, discard a half-run row,
  mark unrun rows "deferred" in the run notes, update RUNNER.md.

### B4. Judge

Turn every LOOK into PASS or FAIL from `RES/rows/<ID>.log`, `rows/<ID>.shell.txt` and `shots/`. A failure caused by
the batch (wrong regex, wrong coordinates) is `BATCH-FAULT`: when the fix is simple, fix it and rerun it in-run
(run default 11); otherwise write the cause for a later rewrite pass (the row keeps its id: reruns are verdict
history, no new `-NNb` ids). qrun itself marks a row `HARNESS` when a regex
does not compile, a capture was left unset by a skipped row, or a shell exit code is the only failure while the log
checks passed. A FAILed row found to be a batch fault: `qrun.py judge RES ID --reclassify BATCH-FAULT --cause "who,
when, why"` (the failed checks stay recorded). Verdict table in the
run doc: `| ID | Result | Evidence |`. qrun writes `RES/verdicts/ID.json` per row (every check with its outcome,
the LOOKs, shots, crash): `qrun.py judge RES` lists the open LOOKs, `qrun.py judge RES ID N VERDICT "evidence"` (or
`--file TSV`) fills one, and `qrun.py table RES [RES ...]` prints the run-doc table and counts. The judge step only
fills LOOKs; a row with shots gets PASS / FAIL only from the owner (`--by owner`) or from text evidence (`--text`).

### B5. Write back

1. Queue: one Run log entry at the top of "Run log" (run id, worlds, seed, perf session, jars, counts, link to the run
   doc); update the header's "Latest" pointer; results stay out of the rows.
2. Feature writeups: results into each test list; README status "built, scratch server PASSED" where all rows passed.
3. FAILs and found bugs: TODO rows (family table) with the test ID; fixes that change behavior wait for the owner.
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
