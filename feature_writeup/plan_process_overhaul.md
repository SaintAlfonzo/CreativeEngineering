# Plan: process overhaul (checks, build identity, design intake, status as data, scratch queue tiers, TODO as the source of truth)

<!-- generated:status_index begin open_work sha1=cffa8b23ef94 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 8 open TODO rows (Planned 3, In Progress 2, Testing 3), 2 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `tooling/process #8` | Planned | none | Phase 3: clean-up passes, install baseline, features audit and CSV, TODO as the source of truth |
| `tooling/process #9` | Planned | none | 155 agent notes and other docs not named snake_case |
| `tooling/process #10` | Planned | S | 77 dead ids in scratch queue rows |
| `tooling/process #6` | In Progress | none | Commit per plan phase, push to agent_staging |
| `tooling/process #7` | In Progress | none | Agent notes out of memory |
| `tooling/process #2` | Testing | S | Build stamps and install ledger |
| `tooling/process #3` | Testing | none | Design intake before building |
| `tooling/process #5` | Testing | S | Tiered scratch queue with expiry (phase 2) |
<!-- generated:status_index end open_work -->

Status: Planned 2026-11-16. Owner rulings the same evening (section "Owner decisions"). Done the same evening:
P1.5 commit rule (CLAUDE.md) and P1.6 memory move (136 notes to `agent_notes/`). P1.1 to P1.3 BUILT 2026-11-16 night
(offline tests PROC-01 to -09 and -15, -18 pass; PROC-16, -17 need the owner at the dialog; SQ-PROC-01 queued). P1.4
BUILT the same night. Phase 2 (P2.1 to P2.5) BUILT 2026-11-16 late night, offline tests PROC-20 to -28 pass; live rows
SQ-PROC-10 to -15, CQ-PROC-01 queued; the never-run pass (P2.5 item 3) waits for the owner (Phase 2 build notes). Phase 3 Planned 2026-11-16 (rulings D10 to D17; P3.2 done). Why: [process_review.md](process_review.md) (findings 1 to 6). TODO
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
- Strip the status lines from the 39 notes, or confirm them against the repo, in a one-time pass with P1.4. Done 2026-11-16 ([status pass](agent_notes/status_pass_2026-11-16.md)).
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

## Phase 3: clean-up passes, install baseline, features audit, TODO as the source of truth

Planned 2026-11-16 from the owner's notes (intake below; rulings D10 to D17). Why: after Phases 1 and 2, task state is
still written in five places (TODO rows, plan phases, briefs, the writeups' section 0, features.tsv). The generated
views are only as good as their seed data, and some Phase 1 and 2 gaps showed up in use (P3.4). TODO row
`tooling/process #8`. Code owner: `tools/` and `build-logic/`, as before. No gameplay change, so no changelog bullet.

Order: P3.1 first (needs a commit so the stamps are clean). P3.2 is done. P3.3 before P3.5, because P3.5 writes CSV and
derives feature status. P3.4 items are small and independent. P3.6 last, because it reads the TODO CSV.

### Intake (2026-11-16)

| # | Item (owner's words) | Class | Ruling |
|---|---|---|---|
| 1 | "creates clean up passes" | open | D10 |
| 2 | "installing all the pending jars" | premise, open | D11. Premise checked: `jarstamp.py pending` says 0, but it sees stamped jars only. Common and Compat in `build/libs` (built 21:19, unstamped) differ from `mods/`; Power Grid -19 is newer than the -18 in `mods/`, and -18 differs from its own build output. The bundles in `mods/` (Delight, Worldgen, Brewing) match their build output |
| 3 | "relayouts were my personal choice, not an issue now" | clear | D12 |
| 4 | "mark the lint issues found as high priority todo rows" | premise, open | D13. Premise checked: the design-rules findings are already classified (design_rules.md section 9) and planned (plan_rules_fixes.md), but have no open TODO rows; texture redo/swap rows exist (`textures/tools #2`, `#3`) |
| 5 | "power grid tech level wont be changable in world, its a world creation value, admins can force switch with destruction" | conflict (resolved) | D15. energy_advanced.md 5 said worlds "can be upgraded or downgraded"; the plan_power_grid E1 / agent note said Power Grid never goes into `mods/` before stage 5, but -18 is in `mods/` |
| 6 | "schedule an audit and repair of the features.tsv" | clear | P3.3 |
| 7 | "switch the system to use csv instead, csv is default and preferred" | open (scope) | D14 |
| 8 | "add the other obvious fixes i missed from phase 1" | clear | P3.4 |
| 9 | "document the rest" | clear | P3.7 |
| 10 | "build the plan to make todo the source of truth" | open (format, ids) | D16, D17; P3.5 |

### P3.1 Install baseline (D11)

1. Commit the open work of the finished phases, so the stamps are clean (`dirty` only where another session's work is
   uncommitted; acceptable, the stamp names it).
2. Rebuild every own jar with the stamp plugin (`./gradlew build --offline` over all projects), then `installToMods`
   per project (the gate and the dialog apply). Power Grid -19 goes into `mods/`; correct
   [agent_notes/power-grid-port.md](agent_notes/power-grid-port.md) ("never in `mods/`"), plan_power_grid.md E1 and add
   its `mod_sides.md` row.
3. Close the `pending` blind spot: `jarstamp.py pending` also compares unstamped build outputs with `mods/` by sha1 and
   lists the differing ones as `unstamped, differs`.
4. Prune stale outputs: older versioned jars in `build/libs` (Power Grid -14 to -18, Common 1.0.0 and 1.1.0, Vintage
   -11 to -17 and the like) move to `jar_archive/displaced/` by a `jarstamp.py prune` command, so `build/libs` holds only
   the newest build per jar.
5. After the install: SQ-PROC-01 (boot line) and the T1 smoke rows (`rows/SMK.qb`) are the first pinned run; queued,
   run when the owner says so. PROC-16 and -17 (dialog) get done by the owner as the installs happen.
6. The 91 "not installed" phrases in TODO Details cells are stale after this; P3.5 removes the class of phrase.

**Done when:** `jarstamp.py pending` prints nothing, including unstamped differences; `jarstamp.py diff mods/
<build outputs>` is clean; every own jar has a ledger row with a stamp.

**Progress 2026-11-16 (Phase 3 session):** items 3 and 4 built (`jarstamp.py pending` compares unstamped builds by
sha1; `jarstamp.py prune` moved 40 stale outputs to `jar_archive/displaced/pruned/`). Full build at `afea3f23`, then
10 stamped installs (Power Grid -19, Tempad, Calendar, Devtools, Engineering, Labels, Magic, Exposure, Storage, SoL
Raspberry Pie); `mod_sides.md` Power Grid row, E1 and the port note corrected. Common, Delight, Brewing, Equipment,
Worldgen and Compat are installed by the small-fixes session (plan_small_fixes_1116.md), whose code was mid-edit;
their stamped installs complete the baseline, then `pending` is empty. Side effect: every own jar has a new stamp, so
`qrun.py plan` counts 714 rows jar-changed (was 539). Follow-up: qplan could use `jarstamp.content_digest` so a
rebuild of unchanged sources does not stale rows.

### P3.2 Lint findings as high-priority TODO rows (D13; done 2026-11-16)

Rows marked **High priority** in their Details cell until P3.5 adds the Priority column (the migration turns the
marker into `priority=high`):
- `textures/tools #2` (4 `swap` echo tools) and `#3` (27 `redo` textures), from `texture_provenance.py`.
- `tooling/conventions #10`: `id_owner_lint` 8 unresolved (7 JEI plugin uids, 1 network channel).
- `tooling/conventions #11`: the `design_rules_audit` findings through plan_rules_fixes.md (V-01 to V-62).
- `tooling/process #9`: DR-W11, 155 file names not snake_case (mostly the moved agent notes).
- `tooling/process #10`: 77 dead ids in queue rows (Phase 2 `qrowlint.py` baseline).

### P3.3 Features table: audit, repair, CSV (D14)

1. **CSV switch.** Every status and process table moves from TSV to CSV (header row, UTF-8, Python `csv` module, `\n`):
   `status/features.tsv`, `jar_archive/installed.tsv`, `tools/check_known.tsv`, `texture_sources/sources.tsv`,
   `texture_sources/ledger.tsv`, `testing/rows/_lint_baseline.tsv`. One migration script with a round-trip check; the
   files move with `git mv` so history follows.
   - Readers and writers: `status_index.py`, `status_seed.py`, `jarstamp.py`, `install_jar.py`, `check_all.py`,
     `texture_provenance.py`, `scratch_query/qrows.py`, `qplan.py`, `qrowlint.py`, `build_deploy.py`.
   - Text that names the files: CLAUDE.md (texture rule, install line, checks line; backup first, D3),
     texture_sources.md, the skills, agent notes. A grep for `\.tsv` in tools and docs must come back empty for these.
   - New tables are CSV from now on (memory `csv-default-format`; CLAUDE.md Preferences gets one line).
2. **Audit**, every one of the 183 rows, by an agent with a list for the owner (`status/features_audit_<date>.md`):
   - The 14 BROKEN rows first. Most are seed errors (Time system "DEPLOYED with 1.0.8", Creative Labels "DEPLOYED with
     1.1.01", Can parity is a worksheet).
   - `jars`: 96 rows are empty. Fill them from where the code lives (`development/<project>`, script namespace,
     data pack folder).
   - `queue codes`: check the 399 heuristic links against the queue sections' own feature.
   - `todo ids`: dropped as a column. P3.5 links each TODO row to its feature instead (the `feature` field), and the
     view is generated.
3. **Derived status.** The status word becomes computed, with a hand override column (`override`, `override_reason`,
   dated; P3.6 warns on an override older than 30 days):
   - **planned**: every linked TODO row is Planned or Pinned, nothing built.
   - **BUILT**: a linked row is In Progress or Testing, and its jar stamp is not in the last DEPLOYED.json.
   - **DEPLOYED**: the jar stamp is in the last deploy.
   - **VERIFIED**: every linked row is Complete.
   - **BROKEN**: an open Bug row marked as a blocker, or a FAIL verdict on the currently installed stamp.
   - **reference**: no jars and no rows (audits, surveys, worksheets).
4. Rerun the Phase 2 section-jar pass (`qrows.py`), so the queue sections take the repaired jars. The 539 jar-changed
   rows are recounted after P3.1.

**Done when:** no `.tsv` file is left among the tables above; the audit list is handed to the owner; every feature row
has jars or is `reference`; the status words come from the rule (overrides listed).

**Done 2026-11-16:** `tools/table_io.py` (CSV read/write, `#` comment lines kept); `tools/migrate_tsv_csv.py` moved the
five tables with a round-trip check (`rows/_lint_baseline.csv` was already CSV); readers updated (status_index,
status_seed, qrows, qplan, install_jar, check_all, texture_provenance); docs and CLAUDE.md point at `.csv` (backup
`CLAUDE.md.bak-2026-11-16-p3`). Audit: [status/features_audit_2026-11-16.md](status/features_audit_2026-11-16.md),
applied, with an owner decision list. Derived status: `tools/feature_status.py`, written by `status_index.py`;
features.csv gains `override`, `override_reason`, `override_date`. `build_deploy.py --snapshot` now records stamp and
content digest per own jar (`jarstamp.content_digest`); before that, DEPLOYED compares content digests with
`deploy/latest/mods`. `qrows.py jars` reset 100 section jars from the repaired table. Still TSV and out of scope: the
perf tools' own outputs and `texture_audit.py`'s unused-texture list (generated, not status tables).

### P3.4 Phase 1 and 2 fixes

| # | Gap found | Fix |
|---|---|---|
| a | `jarstamp.py pending` is blind to unstamped builds | P3.1 item 3 |
| b | The install gate skips the class-based tests: inside Gradle they report SKIP (a nested Gradle would deadlock), so a Java change is gated only by script tests and lints until `--full` at deploy | The gate runs the class-based tests on the installing project's already compiled `build/classes` (no nested Gradle); a test SKIPs only when another project's classes it needs are older than their sources, and the SKIP line names that project |
| c | `test_worldgen_tools` (75 s) never runs under `--fast`, so never at install | `--changed` runs it whenever its inputs changed, even with `--fast` |
| d | `texture_provenance.py` picks a different source on ties: a read-only run rewrote 22 ledger rows (2026-11-16, reverted) | Stable tie-break (shortest path, then name); a check writes tracked files only with `--write`; `check_all` fails if a check dirtied a tracked file |
| e | Stale overhaul rows: `tooling/process #4` Complete but in the open table; `#6`, `#7` described as left to do | `#6`, `#7` text fixed 2026-11-16; `#4` leaves with the P3.5 migration (archive becomes a view) |
| f | 14 archive rows with an open status, among them the 4 `tooling/design-rules` rows marked Open | Owner list in the first clean-up pass; each goes back to its family or to Complete |
| g | D6 copied 166 README status paragraphs into the writeups' section 0: a new hand-written status copy. The 17 generated writeups were skipped | P3.5 item 5 |
| h | Phase 2 stale-row lint: the P1.4 writeup edits made 3,200+ rows stale. P3.5 edits every writeup again, which would cause the same flood | `qrowlint.py` ignores source changes confined to generated blocks and to the removed section 0 paragraphs (diff-level check), so a status-only edit does not stale a row |
| i | deploy.md disagrees with DEPLOY_LOG ([status pass](agent_notes/status_pass_2026-11-16.md) "Repo docs that disagree"): section 0 snapshot (1.1.03), header ends at 1.1.05, missing bullets 1.1.01 to 1.1.06, TODO `pack/release #7` stale, the 1.2.00 client snapshot recorded twice | `build_deploy.py --backfill-history` for the missing versions, section 0 and header from DEPLOYED.json (generated block), duplicate mark removed, `pack/release #7` rewritten |
| j | Two owner rules live only in agent notes: no content test before a deploy; Distant Horizons settings are player config, never pushed after 1.0.4 | Into deploy.md (rules section) and the `release` skill |
| k | An out-of-date generated block is only a WARN, and nothing reminds an implementing session to regenerate | The Stop hook (`impl` role) prints one line when `status_index.py --check` reports an out-of-date block |
| l | Intake: the first scratchpad write-up after P1.3 (plan_post_scratch_1115.md G) has rulings but no rulings table, and the scratchpad block was deleted instead of marked `(intake ... -> plan)`. plan_small_fixes_1116.md followed the skill | `check_all` warning: a `plan_*.md` created after 2026-11-16 with no "Owner rulings" table |

**Done 2026-11-16:** a (P3.1); b `CE_INSTALL_PROJECT` from the gate, the installing project is never "stale", SKIP lines
shown by the gate; c `--fast --changed` runs a slow check whose own inputs changed; d stable tie-break and `--write`
(P3.3 commit) and `check_all` reports tracked files changed during a run (`dirty_tree`: FAIL under `--full`, WARN
otherwise, since a parallel session's edit shows the same way); passing checks now show their WARN lines; e as
planned; h `qrowlint` compares markdown sources without generated blocks and section 0 paragraphs (PROC-23 extended;
the 3,225 warnings stay: they come from real edits, mostly the 85be1e12 "Meta design planning" commit); i backfill
1.1.01 to 1.1.06, deploy.md header is a generated `deploy` block from DEPLOYED.json, duplicate 1.2.00 client mark
removed from DEPLOYED.json and DEPLOY_LOG; `pack/release #7` waits for P3.5 (TODO.md has another session's
uncommitted edits); j deploy.md section 4 and the release skill; k Stop hook line; l `tools/plan_lint.py`
(check `plan_lint`). f goes to the first clean-up pass (P3.6).

### P3.5 TODO as the single source of truth (D13, D16, D17)

Principle: a **TODO row is the only unit of work and the only place its status is written**. Plans hold design, order
and rulings and cite row ids. Writeups describe the feature. Briefs are design notes for rows. Everything else that
shows status is generated.

1. **Rows as CSV**, one file per `#` category: `feature_writeup/todo/rows/<category>.csv` (22 files). Columns:

   | Column | Content |
   |---|---|
   | id | permanent id (D17) |
   | family, table | the `##` family, and the split table name where a family has several |
   | status | Bug / Planned / Pinned / In Progress / Testing / Complete |
   | priority | high / normal / low (empty = normal; D13) |
   | tests | tier marks, as today (`S+ C P`) |
   | work | where the work lands |
   | change | one line |
   | details | how it is built and what is left: no status or install words (item 6) |
   | decisions | open decisions, or the ruling cited |
   | writeup | link and section |
   | feature | features.csv key (replaces the feature table's `todo ids`) |
   | plan | `plan_x.md` and phase, empty when none |
   | source | where the row came from: `scratchpad 2026-11-16`, a run id, a lint |
   | queue | the queue row ids that verify it (the `tests` S tier is checked against their verdicts) |
   | log | short dated entries: `2026-11-15 built; 2026-11-16 SQ-PS15-02 FAIL` |
   | updated | date of the last change, written by the tool |

   Complete rows stay in their category file. The archive becomes a generated view, so no row ever "moves".
2. **`tools/todo.py`** does every edit, so nobody hand-quotes CSV: `add` (next id per D17), `set <id> field=value`,
   `log <id> text`, `move <id> --family`, `show`, `find`. It validates the status legend, the priority words and the
   links, and writes stable order (status rank, then id). `check_all` gets a `todo_rows` check: every file parses and
   matches the schema. An Edit on the CSV is allowed, but the check catches a broken row.
3. **Generated views** (`status_index.py`, between markers; a hand edit fails `--check`):
   - `TODO.md`: the same layout as today (categories, families, split tables, how-to-read, legend), with the snapshot,
     a **High priority** list at the top and the archive at the bottom. Installed / pending state is shown per row from
     the stamps and the ledger, never typed.
   - Each open `plan_*.md`: a block listing its rows with status, plus the generated `Status:` line.
   - Each writeup: an **Open work** block (rows whose `writeup` or `feature` points at it), replacing section 0 status.
   - features.csv status (P3.3 item 3).
4. **Migration**, by script, one commit for the data, one for the generated output:
   - Parse every TODO.md table (`status_index.py` already does), write the CSVs, regenerate TODO.md, and check the round
     trip: the same rows, cells and order; differences only in the generated parts.
   - The interim `**High priority ...**` markers become `priority=high`.
   - Leading bold status prose in Details (`**BUILT 2026-11-15** ...`, 125 rows; "not installed", 91) moves into `log`.
     Install words are dropped, because they are now generated.
   - The plan-named tables ("Scratch 1115 Follow-ups" 1 and 2, 40 rows under Tooling) are refiled into their feature
     families, with `plan` set. Their ids stay.
   - Stale head prose is removed: the 2026-10-21 priorities paragraph (now the High priority list), the run list ending
     2026-10-28 (now the queue coverage block), "patcherMod" in the Work legend.
5. **Plans, writeups and briefs:**
   - Plans: the open task lists (7 plans with `- [ ]` lists, 19 with a `Status:` header) become rows once. A plan keeps
     design, order, rulings and History, and gets the generated row block. Closed plans stay as history.
   - plan_rules_fixes.md: each section (V-groups) becomes a row under `tooling/conventions #11`'s family, with
     `plan=plan_rules_fixes.md`.
   - Writeups: the section 0 status paragraphs from D6 are removed (the old table is kept verbatim in
     `status/readme_index_2026-11-16.md`) and replaced by the Open work block. Generators of the 17 generated writeups
     keep the block markers, so a regeneration does not drop it.
   - Briefs: status words are a lint warning.
6. **Lint for prose status** (`todo_rows` check, `--changed` only, so old History lines are untouched): new text in
   `details` that matches `not installed`, `installed`, `**BUILT`, `Status was` is a FAIL with a pointer to `log` and
   the generated state.
7. **Ids (D17):** existing ids never change. A new row takes the slug of its current `##` family (`salt #1`,
   `process #11`), numbered per family. `todo.py add` assigns it.
8. **Process text:** CLAUDE.md "Done means" (backup first, D3): "a change updates its TODO row with `tools/todo.py`;
   plans cite row ids; writeups describe". The intake skill writes rows with `todo.py add --source --plan`. The
   [todo-layout note](agent_notes/todo-layout.md) is rewritten to the CSV layout.

**Done when:** TODO.md, the plan row blocks, the writeup Open work blocks and the feature status are all generated
from the CSVs; `status_index.py --check` and `todo_rows` pass; a status change made with `todo.py set` shows in TODO,
the plan, the writeup and the README after one `status_index.py` run.

**Done 2026-11-16:** owner chose a fully generated TODO.md (AskUserQuestion 2026-11-16: "Whole file generated", over
keeping prose by hand around generated tables) and committed the unclaimed TODO edits first (52f9cefb).
`tools/todo_rows.py` (schema, render), `tools/todo.py` (add / set / log / move / show / find / lint),
`tools/todo_migrate.py` (one-time; round trip: same ids and cells, every Details word kept, 0 problems). 1,622 rows in
22 category files plus `archive.csv`; `todo/layout.md` holds headings and prose with placeholders. 291 rows had
leading status prose moved to `log`; 6 high-priority markers became `priority=high`; the 40 plan-table rows were filed
by hand into feature families (map in todo_migrate.py `REFILE`). TODO.md's first line carries its body sha1, so a hand
edit FAILs `--check`. features.csv `todo ids` dropped: the 39 links moved to the rows' `feature` field (7 rows already
linked elsewhere kept theirs). Open work blocks: 174 docs (160 writeups replaced their D6 section 0 paragraph; 19
plans got a block with a generated Status line; `can_parity.md` is generated and skipped). plan_rules_fixes.md sections
2 to 19b are rows `conventions #1` to `#26` (priority high, 19 low; section 2 Complete). `pack/release #7` closed.
Checks: `todo_rows` in check_all (schema; `--changed` FAILs new status words in Details, WARNs in briefs).
Not converted: the `- [ ]` lists left in plans are test lists (SCAT-, PROC-) or closed release plans, not tasks. The
17 generated writeups have no Open work block; their generators would need to keep the markers (follow-up).

### P3.6 Clean-up passes (D10)

1. **Threshold warnings**: `tools/cleanup_check.py`, a WARN-only `cleanup` check in `check_all.py`. Limits in
   `tools/cleanup_thresholds.csv` (check, limit, reason):
   - TODO: In Progress rows not updated in 30 days; Testing rows whose tiers all passed but status not Complete;
     prose-status lint hits.
   - Queue: stale rows, dead ids above the baseline, sections eligible for `qrun.py archive`, never-run share.
   - `gotchas.md`: any per-run "Found in rXXXX" section.
   - Agent notes: status words; notes not linked from any doc.
   - Memory: more than 60 files, or any `project`-typed memory.
   - `check_known.csv` entries older than 30 days (exists today).
   - Build outputs: more than one versioned jar per project in `build/libs`; `jar_archive/displaced/` over 2 GB.
   - deploy.md: a DEPLOY_LOG snapshot with no history bullet.
   - Plans: every row Complete but the plan not closed.
   - features.csv: overrides older than 30 days.
2. **Full pass at each release**: a step in the `release` skill before staging, which runs the fixers and writes
   `status/cleanup_<date>.md` with what changed and a list for the owner:
   - `qrun.py archive --apply` and the superseded sweep;
   - `build_deploy.py --backfill-history`;
   - `jarstamp.py prune`;
   - the gotchas merge;
   - the notes status lint;
   - notes that repeat a writeup merged into it (P1.6 follow-up);
   - closed plans marked;
   - the threshold list brought to zero, or each remaining warning listed with a reason.

**Done when:** `check_all` shows the cleanup warnings; one release ran the pass and wrote its doc.

### P3.7 Documented, not planned

- **Power Grid mode (D15)** is a world creation value; no in-world switching; an admin can force a switch, which is the
  destructive clean-up with a backup first. Recorded in [energy_advanced.md](energy_advanced.md) section 5. The
  command belongs to the energy plan (A9), not here.
- **TODO re-layouts (D12)** are the owner's choice. Review section 7's "TODO re-layouts" cadence row is withdrawn.
  After P3.5 a re-layout is a change of the generator's grouping, not of the data.
- **Cross-jar id moves** (Creative Computers and Creative Economy carving): the swap-list route of memory
  `ids-follow-owning-mod` and `id_owner_lint` cover them. A per-carve checklist lint is possible later; not planned.
- **Release hotfix classes** (client-only code on the server, data format errors, mismatched payloads): covered by the
  Phase 2 T3 release-gate rows and the P1.2 stamp check. No extra lint.
- **Never-run rows** (about 60 %, 877 drafts): the owner decides from [status/never_run_2026-11-16.md](status/never_run_2026-11-16.md).
- **Intake measure** (owner reversals per plan): read from the rulings tables (a changed ruling keeps a History line).
  No tool.
- **Spelling mix, code-span file names (1,930), literal ids (4,079)**: plan_rules_fixes.md (sections 15b, 19b, 7) owns them.
- Still non-goals: GameTests (TX-23), client automation (TX-01 to TX-05).

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
| D18 | 2026-11-16: an agent runs rows that need a client and a dedicated server together (its own server plus its own client, `eval_client.py ... ownsrv=PID` / `qrun.py pair`); the playtest list holds `@owner` rows only. Recorded in the scratch-test skill, gotchas.md and memory `one-game-instance-at-a-time` |
| D19 | 2026-11-16: new data files are CSV, not TSV; only `rows/_lint_baseline.csv` converted (the older TSVs stay) (widened by D14) |
| D4 | Queue row data: one `.qb` file per feature section in `feature_writeup/testing/rows/`, queue tables generated from them (option B below) |
| D10 | Clean-up passes: threshold warnings in `check_all` plus a full pass as a step of each release (AskUserQuestion 2026-11-16; P3.6) |
| D11 | Install all pending jars: rebuild every own jar once with a stamp and install them all through `installToMods`; Power Grid -19 goes into `mods/`, superseding plan_power_grid.md E1 and the power-grid-port note (AskUserQuestion 2026-11-16; P3.1) |
| D12 | TODO re-layouts were the owner's choice, not a process issue (chat 2026-11-16) |
| D13 | Lint findings become high-priority TODO rows; priority is a Priority column (high / normal / low), interim marker in Details until P3.5 (AskUserQuestion 2026-11-16; P3.2) |
| D14 | CSV is the default and preferred table format; every status and process TSV moves to CSV (AskUserQuestion 2026-11-16; P3.3) |
| D15 | Power Grid tech level is a world creation value, not changeable in world; admins can force a switch with the destructive clean-up (chat 2026-11-16; energy_advanced.md 5) |
| D16 | TODO rows live in CSV, one file per category; TODO.md and the plan, writeup and priority views are generated; edits through `tools/todo.py` (AskUserQuestion 2026-11-16; P3.5) |
| D17 | Row ids: existing ids never change; new rows take the current `##` family slug (AskUserQuestion 2026-11-16; P3.5 item 7) |

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
- [x] **PROC-10 generated README:** `status_index.py` -> README index block regenerated, no hand text lost outside markers.
  2026-11-16: README outside the markers equal to HEAD before and after the table; old table verbatim in `status/readme_index_2026-11-16.md`.
- [x] **PROC-11 snapshot:** flip one TODO row to Testing -> snapshot counts change by one.
  2026-11-16: `food/types-and-tuning #1` Planned -> Testing: Planned 494 -> 493, Testing 780 -> 781; reverted.
- [x] **PROC-12 verdict to README:** add a verdict file -> the feature's last-run cell updates.
  2026-11-16: `perf/runs/rPROC12/verdicts/SQ-BRA-01.json` (FAIL) -> BlockRunner cell and queue SQ-BRA row show rPROC12 first; removed.
- [x] **PROC-13 legend check:** a row with status "Done" outside the archive -> `--check` FAIL.
  2026-11-16: FAIL naming TODO.md line and id, exit 1; also checked: off-legend features.tsv word, unknown TODO id, queue code with no feature.
- [x] **PROC-14 hand edit:** edit inside a generated block -> `--check` FAIL.
  2026-11-16: Tooltips status edited in the README block -> FAIL (hand edit, lines 18-202), exit 1.
- [x] **PROC-15 install, game closed:** no java with the instance cwd -> `installToMods` installs, no dialog.
- [ ] **PROC-16 Install now while running:** owner client running -> dialog; Install now -> dialog shown again with
  "game still running"; close the game, Install now -> installed, ledger row.
- [ ] **PROC-17 Install on exit:** dialog -> Install on exit -> Gradle returns; close the game -> jar swapped,
  `notify-send` shown, ledger row `on-exit`; a second rebuild before exit replaces the first watcher.
- [x] **PROC-18 Skip and no display:** Skip -> `mods/` unchanged, `jarstamp.py pending` lists it; `DISPLAY` unset ->
  behaves as Skip with a reason printed.
- [x] **PROC-19 notes move:** 136 notes in `agent_notes/`, memory 48 files plus index, no `[[x]]` link left pointing at
  a moved memory, CLAUDE.md imports the index (2026-11-16, script output and diff).

Phase 2 offline (`tools/test_queue_rows.sh`, run by `check_all.py`; fake scratch server, temp rows dir):
- [x] **PROC-20 rows round trip:** every queue rows block equals its `.qb` section rendered; `qrows.py check` OK.
- [x] **PROC-21 rows file runs:** `qrun.py run` on a rows file runs its steps; a `@draft` row is listed and not run;
  `--section` selects.
- [x] **PROC-22 one-jar plan:** one jar's stamp changed -> `qrun.py plan` = T1 rows of that jar plus its T2 rows, not
  the other jar's; estimate under 1 h.
- [x] **PROC-23 stale and dead id:** a row whose source changed after `@@reviewed` -> stale W (E when the source is in
  the session's changed set and the .qb is not); `setblock ... minecraft:not_a_block_zz` -> dead-id E.
- [x] **PROC-24 pin:** a hard-linked own jar becomes a copy; `qpin.py verify` refuses an unlisted jar and a jar changed
  after prep.
- [x] **PROC-25 unlisted jar:** `qrun.py run` refuses to start (no results written); `--no-pin` runs and records
  `_no_pin` in the verdict stamps.
- [x] **PROC-26 HARNESS:** bad regex, unset capture, shell exit as the only failure -> HARNESS; a real failed expect
  stays FAIL.
- [x] **PROC-27 reclassify:** `judge --reclassify` refused without a cause; with one the verdict changes, checks stay.
- [x] **PROC-28 drive:** state file, two steps done, RUNNER.md claimed and released; a step killed after its first row
  resumes from its second.

Phase 3 (offline, written when built):
- [ ] **PROC-30 pending sees unstamped:** an unstamped `build/libs` jar that differs from `mods/` -> listed as `unstamped, differs`.
- [ ] **PROC-31 baseline:** after P3.1 -> `jarstamp.py pending` empty, every own jar in `mods/` stamped with a ledger row.
- [ ] **PROC-32 prune:** `jarstamp.py prune` leaves one jar per project in `build/libs`, the rest in `jar_archive/displaced/`.
- [ ] **PROC-33 CSV round trip:** each migrated TSV -> CSV -> same rows; no `.tsv` named by the tools or docs listed in P3.3.
- [ ] **PROC-34 derived feature status:** flip a feature's only row Planned -> Testing -> BUILT; its override wins and is listed.
- [ ] **PROC-35 gate runs class tests:** a seeded break in a class-based test's subject -> `installToMods` refuses (no SKIP).
- [ ] **PROC-36 no dirtying check:** `check_all.py --full` on a clean tree leaves `git status` clean; a check that writes a tracked file -> FAIL.
- [ ] **PROC-37 TODO round trip:** the migration regenerates TODO.md with the same rows, cells and order.
- [ ] **PROC-38 one edit, all views:** `todo.py set <id> status=Testing` -> TODO, the plan block, the writeup Open work block and the feature status change after one `status_index.py`.
- [ ] **PROC-39 prose status lint:** new Details text with "not installed" -> `todo_rows` FAIL under `--changed`; old text untouched.
- [ ] **PROC-40 new id:** `todo.py add` under family Salt -> `salt #1` (then `#2`); an old id is never renumbered.
- [ ] **PROC-41 stale lint ignores generated edits:** an edit only inside a writeup's generated block does not stale its queue rows.
- [ ] **PROC-42 cleanup thresholds:** a seeded "Found in rXXXX" gotchas section and a 31-day override -> two WARN lines.

Live (queued when built, per skill `scratch-test`):
- SQ-PROC-01: scratch boot shows the `[creativecommon] builds:` line, and its stamps match `jar_archive/installed.tsv`.
- SQ-PROC-10 to -13 (rows/PROC.qb section 2): prep pins on a real server, builds line equals the pinned stamps, verdict
  files carry stamps, a `kill -9`ed driver leaves no server and resumes.
- SQ-PROC-14, -15, CQ-PROC-01 (section 3, tier T3): the release gate (staged payload boot, one boot per optional-mod
  drop, client join).
- SQ-SMK-01 to -56 (rows/SMK.qb, tier T1): smoke rows of the 16 own jars.

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

P1.4 (items 1 to 3; item 4 by the main session, 5 and 6 by another agent):
- `feature_writeup/status/features.tsv` (183 rows) seeded once by `tools/status_seed.py` from the README index. Status
  word: a legend word in the cell (outside bold run notes) first, BROKEN when a run note says BROKEN, else keyword
  inference (built / installed / carved -> BUILT, audit / survey / analysis -> reference, nothing built / idea only ->
  planned); 37 rows needed inference, listed by the seed run. Jars from name mentions, queue codes from mentions in the
  feature's writeup (379), queue section links (17), test prefix (2), TODO links (1): all 399 codes owned. Jars and
  codes are a heuristic seed for the owner to correct by hand.
- D6: each row's feature and status cells went verbatim into its writeup's section 0 (166 paragraphs, 48 writeups got
  a new section 0); the 17 generated writeups were skipped (their regeneration would drop it); the whole old table is
  in `status/readme_index_2026-11-16.md`.
- `tools/status_index.py`: blocks `<!-- generated:status_index begin NAME sha1=... -->` / `end NAME` in README (index),
  TODO (snapshot) and the queue header (coverage, in a `<details>`). Runs from `scratch*_run_*.md` tables (`| ID |
  Result |`, `| Row | Result |`, `| Verdict | Rows |`) and `perf/runs/<run>/**/verdicts/*.json`; jar state from
  `jarstamp.pending()` and the ledger. Out-of-date block = WARN, hand edit = FAIL. `--check` 0.3 s.
- The 2026-11-03 snapshot prose and the 85 queue banner lines moved once to `status/history.md`.
- Item 3: `tools/status_normalise_todo.py`, 85 rows (27 Done / Complete moved to the archive, Status and Tests kept in
  Details); list in `status/normalise_2026-11-16.md`. Beyond the plan's mapping: "Idea" -> Planned, "Tested" and
  "Installed ..." -> Testing, "In progress" case. 14 archive rows still carry an open status (listed in the snapshot).

P1.4 (notes, deploy history):
- Items 5 and 6 done: 33 agent notes lost their status lines (pointers to the owning docs instead), 31 checked and left; list and doc disagreements in [agent_notes/status_pass_2026-11-16.md](agent_notes/status_pass_2026-11-16.md). `build_deploy.py --snapshot` appends its own deploy.md section 8 bullet from the DEPLOYED.json mark (`append_history_rows`); `--backfill-history VERSION...` added 1.2.00 to 1.2.02 (tested on scratch copies, DEPLOYED.json and DEPLOY_LOG untouched).

## Phase 2 build notes (2026-11-16 late night)

Tools (all in `tools/scratch_query/`, entry point `qrun.py`; each file's docstring is the reference):

| Item | Built | Notes |
|---|---|---|
| P2.1 rows as data | `qrows.py` (format, migrate, check, stats); `qrun.py` reads rows files (`@` lines are metadata, `--section`, `--tier`, rows without steps listed as DRAFT); `status_index.py` renders each section as a generated block `rows_<CODE>_<KEY>` | 196 sections moved in one pass (not newest first: the migration is mechanical and the round trip is exact), 175 files, 3177 rows. Lossless: the queue outside the markers is unchanged but for 3 row lines (whitespace, one missing closing pipe). Cells are split backtick-aware (rows quote grep patterns with bare `\|`). Steps imported from the newest past batch holding the row id, preferring one whose text PASSed in a `results.json`: 2031 rows got steps (573 from a passed batch), 1146 are `@draft`. Imported steps keep `@from <batch>`: setup rows of that batch (fixtures, forceload) are not imported, so a runner reads the batch for them. Section jars: features.tsv jars of the section's own code (a heuristic seed: e.g. MELT lists creativedelight and powergrid, not creativecommon; correct features.tsv, then rerun the jar pass). Owner rows: 4, by the pass text ("owner judges", "looks right"). |
| P2.2 tiers | `qplan.py`: `qrun.py plan` (default, `--jars`, `--never`, `--failed`, `--all`, `--release`, `--client`, `--json`), `states`, `playtest`; T1 smoke rows `rows/SMK.qb` (56 rows, 16 own jars, by a subagent; about 9 min); T3 rows `rows/PROC.qb` section 3 | Row states: current, jar-changed, stale, failed, never, draft, owner, superseded. Jar change = a different pinned identity than the verdict recorded; for results from before pinning, the jar's last install (ledger, else file time) after the PASS day. So today every rebuilt jar counts as changed (539 jar-changed rows): the plan narrows once runs record stamps. Owner playtest list: `@owner` rows only (D10). |
| P2.3 expiry | `qrowlint.py` (stale rows, dead ids), inside `qrun.py lint` for rows files and in `check_all.py` as `queue_rows` | Stale = a source (git, or file time when uncommitted) changed after `@reviewed` / `@@reviewed` / `@@date`. Deviation: stale is a warning in check_all, not an error, because the P1.4 status pass touched almost every writeup today (3200+ stale warnings); it is an error only with `--changed` when the session changed the source but not the .qb, and `qrun.py plan` lists stale rows to rewrite before they run. Dead ids: by position (setblock / fill / execute if block -> block, give / clear -> item, summon -> entity, effect -> effect, `#tag`, NBT item stacks) against the newest dumps (items_by_mod, loot tables, tag dumps, effects, fluids, recipe types; display_names.json of 10-20 as a fallback), cached in `jar_archive/queue_id_index.json`. 77 dead ids found at migration (e.g. `#creativeengineering:crop_seasons/*` after the namespace move, `#creativecompat:passthrough/leaf_pile`) are in `rows/_lint_baseline.csv`: warnings; a new one fails the check. Rerun ids keep their id (skill text). |
| P2.4 harness, pins, driver | `qrun.py` HARNESS (bad regex, capture unset by a skipped row, shell exit as the only failure); `qverdict.py` `judge --reclassify` (scratch plan 3.6); `qpin.py` + `prep_srv.sh` pin step + qrun refusal + `stamps` in every verdict file; `qdrive.py` (`qrun.py drive start/resume/status/stop`) | One harness fault makes the row HARNESS (its dependent checks fail with it). Pinning copies only own jars (third-party jars stay hard links: disk), keys them by project mod id (stamp, or `sha1:` for jars built before stamping). Unlisted = no mod_sides.md row; perfBot and the PGJAR Power Grid jar are allowed. The driver runs server rows only (one prep + boot per rows file section and world group); a watchdog process stops the step's server when the driver dies. Client rows are run by the agent (D18) with `qrun.py client` / `pair`; the driver does not launch clients yet. |
| P2.5 archive | `qrun.py archive [--apply]` (`@@archived`, rendered into `testing/scratch_queue_archive.md` with verdict history, created by the first archived section); `@superseded` rows; `qrun.py never` | Archive: 0 sections eligible today (no section has every row current while pre-pinning results cannot prove it); tested by archiving one section and restoring it. Superseded: 13 rows whose own text says superseded / obsolete / dropped (SQ-CBS-01, CQ-CBS-01, SQ-GLD-15, SQ-HSC-87, -88, -90, -91, SQ-MDP-02, -04, -08, -09, SQ-CBJ-05, SQ-RD-02). Never-run rows: [status/never_run_2026-11-16.md](status/never_run_2026-11-16.md), 163 sections; 1103 never-run rows with steps, 877 drafts, 384 of them client rows. Not run: running them is a scratch run (about 24 to 62 h with the jar-changed and stale rows, `qrun.py plan --never`) and waits for the owner. Gotchas consolidated by a subagent (item 5). |

Phase 2 "done when" against the build:
- A one-jar change plans T1 plus the affected T2 rows under 1 h: met offline (PROC-22); live, after the first pinned run.
- Never-run rows below 5 %: not met (about 60 %); needs the never-run pass above and the drafts' steps.
- Seeded stale row and dead id flagged: met (PROC-23).
- A run with an unlisted jar refuses to start: met (PROC-25).

## History

- 2026-11-16: planned from [process_review.md](process_review.md); owner asked for items 1 to 3 and 5 as phase 1,
  item 4 as phase 2.
- 2026-11-16 evening: rulings D1 to D3 and D5 to D9; D4 detailed, then settled as option B (`.qb` per section).
  - CLAUDE.md backed up, then edited: agent notes line and import, install policy, commit rule, "or agent notes" in
    Done means.
  - P1.6 memory move done.
  - P1.2 item 7 install dialog, P1.5 and P1.6 added.
- 2026-11-16 night: P1.1 to P1.3 built (Build notes above). CLAUDE.md backed up to `CLAUDE.md.bak-2026-11-16-p1`, then: `intake` line, install line (dialog built), Checks line. Tests renamed PO- to PROC- (PO- is potions.md's).
- 2026-11-16 night: P1.4 items 5 and 6 (agent notes status pass, deploy.md history from `--snapshot`) done; P1.6 follow-up 1 closed.
- 2026-11-16 night: P1.4 items 1 to 3 built (Build notes above); PROC-10 to PROC-14 passed offline.
- 2026-11-16 late night: Phase 2 (P2.1 to P2.5) built (Phase 2 build notes above); PROC-20 to PROC-28 passed offline. CLAUDE.md backed up to `CLAUDE.md.bak-2026-11-16-p2`, then: the queue line names the rows files and `qrun.py plan`. Subagents wrote rows/SMK.qb (T1 smoke rows) and consolidated the scratch-test gotchas.
- 2026-11-16 late night: Phase 3 planned from the owner's notes (intake table, rulings D10 to D17): install baseline, lint rows (P3.2 done: high-priority marks and rows in TODO), features audit and CSV, Phase 1 and 2 fixes, TODO rows as CSV with generated views, clean-up passes. Power Grid mode ruling recorded in energy_advanced.md 5. Memory `csv-default-format` added.
