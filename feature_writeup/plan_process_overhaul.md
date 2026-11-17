# Plan: process overhaul (checks, build identity, design intake, status as data, scratch queue tiers)

Status: Planned 2026-11-16. Owner rulings the same evening (section "Owner decisions"). Done the same evening:
P1.5 commit rule (CLAUDE.md) and P1.6 memory move (136 notes to `agent_notes/`). P1.1 to P1.3 BUILT 2026-11-16 night
(offline tests PROC-01 to -09 and -15, -18 pass; PROC-16, -17 need the owner at the dialog; SQ-PROC-01 queued). Why: [process_review.md](process_review.md) (findings 1 to 6). TODO
`tooling/process #1` to `#5`. Code owner: `tools/` and `build-logic/`. Jar changes are limited to a build stamp
added by the shared Gradle plugin; no gameplay change, so no changelog bullet. Phase 2 continues
[plan_scratch_testing_expansion.md](plan_scratch_testing_expansion.md): TX-40, TX-41 and TX-45 were built 2026-11-15;
this plan uses them and builds TX-42, TX-44 and TX-46.

## Goal

1. One command proves the offline state of the pack in under a minute. It runs before every install and at the end of
   every agent session, so rules hold even when nobody remembers them (findings 3, 4).
2. Every own jar names the exact build it came from, and one ledger says what is installed, staged and under test
   (finding 5).
3. Owner decisions are collected before an agent builds, not after (finding 6).
4. Status is written once, as data; README, TODO counts and queue coverage are generated from it (finding 1).
5. Phase 2: a scratch run tests what changed, not the whole queue; rows expire, refresh and fail cleanly (finding 2).

Non-goals:
- Gameplay changes.
- Rewriting existing writeups.
- GameTests (TX-23).
- Client automation (TX-01 to TX-05; stays in the scratch testing plan).
- The recurring weekly consolidation pass (review section 9). P1.6 and P2.5 do one-time clean-ups.

## Phase 1

Order: P1.1 and P1.2 first (small, independent, immediate payoff), then P1.3 (process only), then P1.4 (largest; uses
P1.1 for its lint and P1.2 for jar state).

### P1.1 One check command and gates

**Build:**
1. Repair the 12 failing `tools/test_*.sh` (review section 3). Most only need new paths, packages and generator names
   after the split.
   - For `test_passthrough` (breakStallTicks) and `test_worldgen_tools` (structure snapshot), decide real bug vs stale
     expectation from the code. A real bug becomes a Bug row; a stale expectation is updated with a one-line reason
     in the test.
   - A test whose code was removed is deleted, with a History line in its writeup.
2. `tools/check_all.py`, one entry point:
   - Runs every `tools/test_*.sh` plus `id_owner_lint`, `guard_check`, `optional_ref_audit`, `changelog_lint`, with a
     timeout each.
   - Prints one line per check: `PASS|FAIL|KNOWN name seconds first-error`. Exit code 0 only when nothing FAILed.
   - `--fast` skips anything over 15 s. `--changed` runs only checks whose inputs (a path list per check) changed
     since the last commit.
   - A KNOWN list (`tools/check_known.tsv`: check, pattern, reason, date) holds accepted failures such as the Power
     Grid capacitor band. Any entry older than 30 days is printed as a warning.
   - `texture_provenance` (63 s) and `design_rules_audit` (report only) run with `--full` and in the release gate.
3. Fix or allowlist the 27 `guard_check` hits (Creative Delight 25, Common 1, Brewing 1). Each one is either a missing
   optional-mod guard (a bug) or a gap in the tool's required-mod set.
4. **Gates:**
   - `installToMods` in [ce.forge-mod.gradle](../build-logic/src/main/groovy/ce.forge-mod.gradle) (bundle and repack
     plugins too) runs `check_all.py --fast --changed` first. A FAIL stops the install and names the check;
     `-PskipChecks` (logged in the ledger, P1.2) overrides it.
   - `build_deploy.py` stage runs `check_all.py --full`; errors block the staging.
   - A Claude Code Stop hook in `.claude/settings.json` runs `check_all.py --fast --changed` and shows the FAIL lines,
     so a session cannot end on a broken check unnoticed. The hook reports; it does not block.
5. Add a line to CLAUDE.md "Build and verify" naming `check_all.py`, and one to the `release` and `scratch-test` skills
   (preflight).

**Done when:**
- `check_all.py` exits 0 on a clean tree in under 60 s (`--fast`).
- A seeded break fails it: an unguarded optional id in a Creative Delight script, or a test path renamed.
- `installToMods` refuses on that break.

**Tests:** PROC-01 to PROC-05 (offline, in process_review.md, section 7 of the plan below).

### P1.2 Build stamps and an install ledger

**Build:**
1. The shared plugin writes a build stamp into every own jar:
   - `META-INF/MANIFEST.MF` `Implementation-Version: <mod_version>+<git short sha>[.dirty].<yyMMddHHmm>`
   - and `META-INF/ce-build.json` with `{mod_id, mod_version, sha, dirty, built_at, gradle_task}`.
   - Bundles stamp the outer jar and each member.
   - File names stay as they are, so deploy, `replaces_jars` and the `${jarName}-*.jar` matching are untouched.
     Owner decision D1.
2. `installToMods` appends to `jar_archive/installed.tsv`: time, jar, stamp, displaced jar and its stamp,
   `skipChecks` if used.
3. `tools/jarstamp.py`:
   - `jarstamp.py mods/` prints the stamp of every own jar in a folder.
   - `jarstamp.py diff A B` compares two folders: mods, a scratch `srv/mods`, a staged payload, `build/libs`.
   - `jarstamp.py pending` lists every `build/libs` jar whose stamp is not in `mods/` ("built, not installed",
     generated instead of written in plans).
4. Common logs its own stamp and every Creative jar's stamp at boot, in one line: `[creativecommon] builds: ...`.
   A run doc, a crash report and the owner's `latest.log` then all name the exact builds.
5. `build_deploy.py` records stamps in DEPLOYED.json and refuses a payload whose server and client stamps differ for
   the same jar (the 1.1.04 case).
6. Plans stop writing "built HH:MM, not installed" by hand and cite `jarstamp.py pending` instead. Existing plan
   headers stay as history.
7. **Install dialog** (owner ruling 2026-11-16). Policy: a rebuild for testing is installed without asking.
   `installToMods` (after the P1.1 gate) checks for the owner's running game: a `java` process whose cwd is the
   instance root (pid and cwd only, never the command line; scratch servers run elsewhere and do not count).
   - Not running: install, write the ledger row.
   - Running: a `zenity` (fallback `yad`) dialog on the owner's display, titled with the jar and stamp. Buttons:
     - **Install now:** re-checks the game. If it is still running, the dialog comes back with a "game still
       running" line; if not, it installs.
     - **Install on exit:** a detached watcher (`tools/install_watch.py`, pid file in `jar_archive/`) polls the
       game pid, installs when it exits, sends `notify-send`, and writes the ledger row (`on-exit`).
     - **Skip:** nothing installed; ledger row `skipped`; `jarstamp.py pending` keeps listing it.
   - Gradle returns as soon as the owner answers, so an agent session is not blocked by a watcher.
   - Two watchers for the same jar: the newer replaces the older; stamps make that safe.
   - No display, or no dialog tool: behave as Skip, and print the reason.
   - The CLAUDE.md "Build and verify" install line already states this policy; once the dialog is built, drop its
     "until ... ask" clause.

**Done when:**
- Two builds of Common with one change between them print different stamps.
- `jarstamp.py diff mods/ <scratch srv/mods>` shows the difference.
- The boot line appears in a scratch `latest.log`.
- A deploy with mismatched server and client stamps is refused.

**Tests:** PROC-06 to PROC-09, PROC-15 to PROC-18 offline. SQ-PROC-01 (boot line, stamps match the ledger), queued when built.

### P1.3 Design intake before building

A process change with a small helper; no game code.

**Build:**
1. A project skill `intake` (`.claude/skills/intake/SKILL.md`), used whenever an agent turns owner notes (scratchpad
   blocks, "owner notes" sections, run findings) into a plan:
   - **Split** the notes into single items, one line each, quoting the owner's words.
   - **Classify** each item:
     - *clear*: one reasonable reading.
     - *open*: two or more readings with different results.
     - *conflict*: contradicts an earlier owner ruling; cite it.
     - *premise*: assumes something about the current state. Check that against the code before planning; this
       catches "already done" premises.
   - **Ask** every open and conflict item before building anything that depends on it. Use AskUserQuestion with up to
     4 per call, concrete options and a recommended option. Visual items get a preview: ASCII layout, a
     client-capture screenshot of the current state, or a mock in the scratchpad.
   - **Record**: the plan gets an "Owner rulings" table (item, ruling, date, source). Any rule the agent chose itself
     is written as `agent default, unconfirmed` and listed in the hand-over. Building on an unconfirmed default is
     allowed only for *clear* items.
   - **UI and visual items** (screens, books, HUD, textures, models): the first build is a look-check. Capture
     a screenshot with the existing client capture (`tools/client_capture`) and show it to the owner, before
     writing test rows or polishing.
2. Mark the scratchpad block as taken in, e.g. `(intake 2026-11-16 -> plan_x.md)`, so it is not processed twice. The
   human idea section stays untouched (scratchpad rule).
3. One line in CLAUDE.md "Glossary and processes" naming the skill (owner decision D3).

**Done when:**
- The next scratchpad block taken into a plan has a rulings table.
- Open items were asked before the build.
- The hand-over lists any unconfirmed defaults.

**Measure:** owner reversals per plan. Baseline: about 6 in the 11-10 to 11-16 week (review section 6).

### P1.4 Status as data, docs as generated views

The TODO tables are already structured (`Id | Change | Status | Tests | ...`). This step makes them, plus a small
feature table, the only place status is written, and generates the rest.

**Build:**
1. `feature_writeup/status/features.tsv`, one row per README index feature:

   | Column | Content |
   |---|---|
   | key | feature key |
   | file | writeup |
   | test prefix | |
   | status word | VERIFIED / DEPLOYED / BUILT / BROKEN / reference / planned |
   | jars | own jars the feature lives in |
   | queue codes | `SQ-`/`CQ-` codes |
   | todo ids | |
   | note | one short line; no run results |

   Seeded once by a script from the current README index. The long prose descriptions move to the writeups'
   section 0.
2. `tools/status_index.py`:
   - Reads `features.tsv`, the TODO tables, the scratch verdict files (`RES/verdicts/*.json`, TX-45) and the run
     docs' verdict tables.
   - Reads `jar_archive/installed.tsv` and `jarstamp.py`.
   - Writes three generated blocks, between `<!-- generated:status_index -->` markers:
     1. **README index table:** feature, file, prefix, status word, last run per queue code (run id, PASS / FAIL
        counts, date), installed or pending stamp state, note.
     2. **TODO status snapshot:** counts by status and test tier, always current.
     3. **Queue coverage** in the queue header: rows per code, never-run count, last run. This replaces the
        hand-written "added at the end" banners. Old banners move to a history file once.
   - `--check` mode (run by `check_all.py`) fails on:
     - a status word not in the legend (TODO or features.tsv);
     - a TODO id cited in features.tsv that does not exist;
     - a queue code with no feature;
     - hand edits inside a generated block.
3. Normalise the off-legend statuses once: "Built" to Testing or In Progress, "Done" / "Complete" to the archive,
   "Open" to Planned. 61 rows, done by script with a list for the owner.
4. Update "Done means" in CLAUDE.md and the [feature-writeup pattern](README.md) wording (owner decision D3).
   - A change updates its writeup, its TODO row and its features.tsv row by hand, then runs `status_index.py`.
     README status, snapshot counts and queue banners are never hand-edited.
   - Run results are never pasted into README cells.
5. Memory: re-type the 136 `project` memories. Any that hold status get their facts checked against the repo and
   become a pointer or are deleted, per [memory-vs-docs-policy]. A one-time pass with a list for the owner.
6. deploy.md history: `build_deploy.py` appends the history row itself from DEPLOYED.json, and back-fills 1.2.00 to
   1.2.02 once.

**Done when:**
- README index status, the TODO snapshot and the queue coverage block are generated.
- `status_index.py --check` passes in `check_all.py`.
- A test change (flip one TODO row to Testing, add a verdict file) shows up in all three blocks after one command.

**Tests:** PROC-10 to PROC-14 offline.

### P1.5 Commit per plan phase (rule in place 2026-11-16)

Review finding 7. The owner's rulings:
- Agents commit at the end of each plan phase as the configured author (SaintAlfonzo), with the Co-Authored-By trailer.
- They push `HEAD:agent_staging`, never `main`; the owner merges.
- Only that phase's files are staged; generated output goes in its own commit.

The rule text is in CLAUDE.md "Build and verify". `agent_staging` was 29 commits behind `main` with nothing of its own,
so pushes fast-forward; a push that does not is stopped and asked about.

To build later (small):
- `check_all.py` warns at session end when finished-phase files are still uncommitted, through the Stop hook.
- A short "Merging agent_staging" note in [agent_notes/git-repo-setup.md](agent_notes/git-repo-setup.md).

### P1.6 Agent notes out of memory (done 2026-11-16)

All 136 `project` memories moved verbatim (frontmatter reduced to title and description, plus a moved-on line) into
[agent_notes/](agent_notes/README.md). Its index is built from the memory index lines and imported by CLAUDE.md, so it
still loads every session. Done by a script:
- `[[links]]` between moved notes became relative links; links from the 32 kept memories now name the repo path.
- 37 repo docs that cited `memory `x`` for a moved note now link the note.
- Memory went from 185 files to 48 plus `MEMORY.md`.
- Backups:
  - `~/.claude/projects/<instance>/memory_backup_2026-11-16.tar.gz` (the whole folder before the move);
  - `CLAUDE.md.bak-2026-11-16` (git-ignored).

Impact:

| Area | Before | After |
|---|---|---|
| Loaded every session | MEMORY.md, ~78 lines | MEMORY.md 54 lines (5.8 KB) plus the notes index 52 lines (9 KB) through CLAUDE.md: about the same, since the index lines moved and were not dropped |
| Recall | One-line hooks in MEMORY.md | The same hooks in the imported notes index |
| Versioning | None (outside git) | Tracked, diffable, pushed with commits (repo is private) |
| Reach | This machine's Claude Code only | Any checkout: cloud sessions, other machines, agents given a path |
| Checks | None | Can be linted by `check_all.py` (dead links, status words; P1.4) |
| Staleness | 39 notes carry status words | Same 39, now flagged by the moved-on line ("repo status docs win") |
| Policy | 136 project memories against the memory-vs-docs rule | Memory holds owner rules, user facts and references only; memory `memory-vs-docs-policy` updated |

Follow-ups:
- Strip the status lines from the 39 notes, or confirm them against the repo, in a one-time pass with P1.4.
- Notes that repeat a writeup get merged into it later, with the note becoming a pointer.
- Undo: extract the tarball over the memory folder and restore CLAUDE.md from the backup.

## Phase 2: tiered scratch queue with expiry

Starts after P1.2 (stamps) and P1.4 (verdict and feature data). Builds on qrun's lint, dry run and verdict files.

### P2.1 Rows as data (TX-42)

1. Each queue section becomes one `.qb` file in `feature_writeup/testing/rows/<CODE>.qb` (D4). qrun runs it
   directly; batches are selected from it by metadata, never rewritten per run.
   - `scratch_queue.md` becomes a generated view: per-section tables (id, group, commands summary, pass, if it fails)
     plus the P1.4 coverage block, written by `status_index.py` between markers.
   - Migration generalises `hh_queue_to_qb.py`, newest sections first.
2. Each row carries metadata:
   - `jars` (own jars it tests), `group`, `tier` (P2.2);
   - `sources` (the files or writeup section it was written from, for TX-46);
   - `owner: true` for design-feel rows ("not worth automating" in the scratch plan, section 3.5).
3. Migration by section, newest first. Sections not migrated keep working through the old path until P2.5 archives
   them.

### P2.2 Tiers

| Tier | What | When | Budget |
|---|---|---|---|
| T0 | `check_all.py` (offline) | every install, session end | < 1 min |
| T1 smoke | 3 to 10 rows per own jar: boots, registers, its main command / recipe / block works | a jar's stamp changed | ~10 min per jar |
| T2 feature | the feature's rows | its `jars` or `sources` changed since its last PASS | per feature |
| T3 release gate | staged payload boots as a dedicated server; one boot per optional-mod drop (SQ-DEP pattern); T1 of every jar; a client join | before `build_deploy.py` deploy | ~1 to 2 h |
| Owner | `owner: true` rows and CQ rows nobody automates, collected into a per-release playtest checklist for the owner | release | owner's time |

- `qrun.py plan` builds a run from this: changed stamps (`jarstamp.py diff` against the last run's pinned set) to
  the T1 rows of those jars, plus the T2 rows whose `jars`/`sources` changed.
- A whole-queue run becomes an explicit `--all`, used only when the owner asks.

### P2.3 Expiry and refresh (TX-46)

1. A row PASSed on stamp S is **current** while its jars still have stamp S, and its `sources` are unchanged.
   Current rows are skipped by `qrun.py plan` and shown as current in the coverage block.
2. A row is **stale** when its `sources` changed after it was written. `qrun.py lint` flags it, and the agent that
   changed the source rewrites the row in the same session. This becomes part of `check_all.py` once rows are data.
3. Ids in row commands (`namespace:path`) are checked against the latest dumps (`kubejs/exported/`). An id that no
   longer exists is a lint error. This catches the namespace-move class of row faults.
4. A row that FAILs, is fixed, and PASSes keeps its id. Reruns are verdict history, not `-NNb` copies. Existing
   `b`/`c` ids stay.

### P2.4 Harness fault classes and pinned jars

1. Finish TX-45's judge reclassification (owner ruling 2026-11-16 in the scratch plan, section 3.6). In addition,
   qrun itself classifies a check as HARNESS, not FAIL, before judging, when:
   - a shell check's exit code is the only failure and the output matches;
   - a regex failed to compile;
   - a marker or capture came from a skipped row (TX-40 lint cases at run time).
2. Every run copies the jars it tests into its own folder (no hard links), records their stamps in the verdict files,
   and refuses to start if `jarstamp.py diff` finds any unlisted jar in `srv/mods`. This closes the parallel-session
   swap and unlisted-jar gotchas.
3. TX-44 resumable run driver, so a dead runner leaves state that can be resumed.

### P2.5 Archive and consolidate (one-time)

1. Sections whose rows are all PASS on jars still installed move to `testing/scratch_queue_archive.md`, with their
   verdict history.
2. Rows for removed features become `superseded`.
3. The ~562 never-run rows: each section's rows are run once as T2 (if migrated) or listed for the owner to keep or
   drop.
4. The 387 never-run CQ rows stay in the queue for now (D5 ruling). Each is converted to the new scratch testing tools
   when those are built (client capture runner, in-process client driving: scratch plan TX-01 to TX-05). They are not
   archived, dropped or moved to the playtest list in this phase.
5. [gotchas.md](../.claude/skills/scratch-test/gotchas.md): merge the per-run "Found in rXXXX" sections into topical
   rules. A gotcha now enforced by a check (`grep -c`, jar pinning, unlisted jars) becomes one line naming that check.

**Phase 2 done when:**
- A one-jar change produces a planned run of T1 plus the affected T2 rows, under 1 h.
- The coverage block shows never-run rows below 5%.
- A seeded stale row (source changed) and a seeded dead id are flagged by lint.
- A run with an unlisted jar refuses to start.

## Owner decisions

Settled 2026-11-16:

| # | Ruling |
|---|---|
| D1 | Build stamp in the manifest and `ce-build.json` only; file names unchanged |
| D2 | `installToMods` refuses on a fast-check FAIL (`-PskipChecks` overrides, logged); Stop hook report-only |
| D3 | CLAUDE.md may be edited, with a backup taken first and the diff shown to the owner after. Done for P1.2's install policy, P1.5 and P1.6 (`CLAUDE.md.bak-2026-11-16`). P1.1, P1.3 and P1.4 add their lines when built, the same way |
| D5 | Client rows stay; converted to the new scratch testing tools when those are built (P2.5 item 4) |
| D6 | Long README feature descriptions move into the writeups' section 0 |
| D7 | Commits: per plan phase, authored as the owner, with the Co-Authored-By trailer, pushed to `agent_staging` (P1.5) |
| D8 | Install a rebuild for testing without asking; when the game runs, a dialog with Install now (re-shown while it still runs) / Install on exit / Skip (P1.2 item 7) |
| D9 | Project memories move into the repo (P1.6, done) |
| D4 | Queue row data: one `.qb` file per feature section in `feature_writeup/testing/rows/`, queue tables generated from them (option B below) |

Background for D4 (settled as option B):

Today a row is a markdown table line (`| ID | Group | Commands | Pass | If it fails |`).
  At run time an agent hand-converts it into a `.qb` batch: qrun's format, a text file of commands, `expect` /
  `waitlog` / `shell` checks and markers (example: `tools/scratch_query/batches/r1113pwg/pwgr1.qb`). Every run
  rewrites the batches, and the two copies drift; that is one source of the stale and wrong rows in review section 2.
  Phase 2 needs the row to exist once, as data qrun can run, with the metadata from P2.1 item 2.

  | | Option A: fenced block under each queue section | Option B: one `.qb` file per section, queue tables generated (chosen) |
  |---|---|---|
  | Where | Inside `scratch_queue.md`, a fenced `qb` block after each section's table | `feature_writeup/testing/rows/<CODE>.qb`, e.g. `rows/MELT.qb`; `scratch_queue.md` becomes a generated view |
  | Human view | Table and data side by side in one file; both hand-kept unless the table is generated from the block | Table generated from the `.qb` (id, group, commands summary, pass, if it fails), so it cannot drift |
  | Queue file size | Grows further (7.5k lines now, roughly doubles) | Shrinks to tables plus the coverage block; old sections archived (P2.5) |
  | qrun | Needs an extractor (as `hh_queue_to_qb.py` does) | Runs the file directly; `qrun.py plan` picks rows by metadata |
  | Agent edits | One large file, conflicts between parallel sessions | One small file per feature; parallel sessions rarely touch the same one |
  | Staleness check (TX-46) | Per block | Per file, using its git history and `sources` |
  | Migration | In place | Newest sections first; unmigrated sections stay readable in the old file until archived |



## Test list (PROC-)

Offline (checked by the building agent, results in [process_review.md](process_review.md) History):
- [x] **PROC-01 check_all clean:** `python3 tools/check_all.py --fast` on a clean tree -> exit 0, under 60 s, one line
  per check.
- [x] **PROC-02 seeded break:** add an unguarded optional id to a Creative Delight script -> `guard_check` FAIL, exit 1.
- [x] **PROC-03 install gate:** with the PROC-02 break, `./gradlew :creativeDelight:installToMods --offline` -> refused,
  names the check, `mods/` unchanged; with `-PskipChecks` -> installs, ledger row says skipChecks.
- [x] **PROC-04 KNOWN aging:** a KNOWN entry dated 31 days ago -> warning line.
- [x] **PROC-05 Stop hook:** end a session with the PROC-02 break -> FAIL lines shown.
- [x] **PROC-06 stamp:** two builds of Common with one change between -> different `Implementation-Version` and
  `ce-build.json`.
- [x] **PROC-07 ledger:** install -> `jar_archive/installed.tsv` row with both stamps.
- [x] **PROC-08 pending:** `jarstamp.py pending` lists a built-not-installed jar and nothing else.
- [x] **PROC-09 deploy mismatch:** staged payload with server and client stamps differing -> `build_deploy.py` refuses.
- [ ] **PROC-10 generated README:** `status_index.py` -> README index block regenerated, no hand text lost outside markers.
- [ ] **PROC-11 snapshot:** flip one TODO row to Testing -> snapshot counts change by one.
- [ ] **PROC-12 verdict to README:** add a verdict file -> the feature's last-run cell updates.
- [ ] **PROC-13 legend check:** a row with status "Done" outside the archive -> `--check` FAIL.
- [ ] **PROC-14 hand edit:** edit inside a generated block -> `--check` FAIL.
- [x] **PROC-15 install, game closed:** no java with the instance cwd -> `installToMods` installs, no dialog.
- [ ] **PROC-16 Install now while running:** owner client running -> dialog; Install now -> dialog shown again with
  "game still running"; close the game, Install now -> installed, ledger row.
- [ ] **PROC-17 Install on exit:** dialog -> Install on exit -> Gradle returns; close the game -> jar swapped,
  `notify-send` shown, ledger row `on-exit`; a second rebuild before exit replaces the first watcher.
- [x] **PROC-18 Skip and no display:** Skip -> `mods/` unchanged, `jarstamp.py pending` lists it; `DISPLAY` unset ->
  behaves as Skip with a reason printed.
- [x] **PROC-19 notes move:** 136 notes in `agent_notes/`, memory 48 files plus index, no `[[x]]` link left pointing at
  a moved memory, CLAUDE.md imports the index (2026-11-16, script output and diff).

Live (queued when built, per skill `scratch-test`):
- SQ-PROC-01: scratch boot shows the `[creativecommon] builds:` line, and its stamps match `jar_archive/installed.tsv`.
- Phase 2 rows (SQ-PROC-10 onward) are written when P2.1 is built.

## Build notes (2026-11-16 night)

P1.1:
- 12 failing tests: 10 repaired (paths, packages, `genText`/`has` in the Rhino harnesses, stubs); passthrough was stale
  `build/classes`, not config drift; the worldgen snapshot was a dump from before the `creativeworldgen` rename (the
  test now maps the namespace and notes it), FitTest 189 -> 190 (corn maze). Two real gaps stay KNOWN in
  `tools/check_known.tsv`: hops vine growth rule (`largecontent/mod-additions #64`), Rustic wild crops in
  `gen_crop_worldgen_table.py` (`farming/wild-crops #11`, new Bug row). `test_remainder_buffer` is now a headless javac
  test; the old GameTest script is `tools/gametest_remainder_buffer.sh` (stale, needs retargeting to Creative Delight).
- guard_check 27 -> 0: real guards added in Common's `botanySoil` and Creative Delight's can effects (`cans.js`, optional
  effect ids); tooltip and table rows in Hearth and Harvest, crop and crow tags, transition items and potions marked or
  guarded; the tool now reads `global.has.mod(...)`, per-element `has(id)` checks and skips generated can parts.
- `check_all.py`: parallel, `--fast` 34 s (skips `test_worldgen_tools`, about 75 s alone), full 102 s. Class-based tests
  compile stale mods first; inside Gradle they SKIP instead (nested Gradle would deadlock), and the gate runs after
  the installing project's `classes`.
- Stop hook: `tools/check_stop_hook.py`, cached on the changed-file state (Stop fires every turn), also counts
  uncommitted tracked files (P1.5 "to build later"). Since 2026-11-16 it follows the role declared with
  `tools/session_role.py` (owner rulings: declared only, undeclared = impl; test = run-doc reminder only; impl counts
  own code files only). Own files come from the session's and its subagents' transcripts (Edit/Write paths, paths
  named in writing Bash commands); `check_all.py --changed-file` takes that list. Plan sessions no longer get
  nagged, and implementers are no longer told to commit plan docs from parallel sessions.

P1.2:
- Stamp time: the commit time for a clean tree, else the newest mtime of the dirty input files, plus a content hash
  (`.dirty-<hash6>`). Plan said build time; that would break the reproducible-jar rule in `ce.forge-mod.gradle`
  (a rebuild of unchanged source must give the same jar). Checked: same source -> same stamp and sha1; one change ->
  new stamp.
- Install: `tools/install_jar.py` (called by `installToMods`). Game detection is a `java` with the instance cwd that
  holds `logs/latest.log` open: the headless test JVMs also run from the instance root, and one even had a `mods/` jar
  open, so cwd alone gave false "game running".
- Deploy: `build_deploy.py` records `stamps` in `history/<version>.json` and the `--snapshot` marks, refuses a mod id
  whose `mods/` and `client_mods/` jars differ in stamp, and warns on dirty builds.

P1.3: skill `.claude/skills/intake/SKILL.md`; CLAUDE.md line. Measure starts with the next scratchpad intake.

## History

- 2026-11-16: planned from [process_review.md](process_review.md); owner asked for items 1 to 3 and 5 as phase 1,
  item 4 as phase 2.
- 2026-11-16 evening: rulings D1 to D3 and D5 to D9; D4 detailed, then settled as option B (`.qb` per section).
  - CLAUDE.md backed up, then edited: agent notes line and import, install policy, commit rule, "or agent notes" in
    Done means.
  - P1.6 memory move done.
  - P1.2 item 7 install dialog, P1.5 and P1.6 added.
- 2026-11-16 night: P1.1 to P1.3 built (Build notes above). CLAUDE.md backed up to `CLAUDE.md.bak-2026-11-16-p1`, then: `intake` line, install line (dialog built), Checks line. Tests renamed PO- to PROC- (PO- is potions.md's).
