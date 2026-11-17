# Plan: process overhaul (checks, build identity, design intake, status as data, scratch queue tiers)

Status: Planned 2026-11-16, nothing built. Why: [process_review.md](process_review.md) (findings 1 to 6). TODO
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
- Commit cadence and the weekly consolidation pass (review section 9: owner habits, not tooling).

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

**Tests:** PO-01 to PO-05 (offline, in process_review.md, section 7 of the plan below).

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

**Done when:**
- Two builds of Common with one change between them print different stamps.
- `jarstamp.py diff mods/ <scratch srv/mods>` shows the difference.
- The boot line appears in a scratch `latest.log`.
- A deploy with mismatched server and client stamps is refused.

**Tests:** PO-06 to PO-09 offline. SQ-PO-01 (boot line, stamps match the ledger), queued when built.

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

**Tests:** PO-10 to PO-14 offline.

## Phase 2: tiered scratch queue with expiry

Starts after P1.2 (stamps) and P1.4 (verdict and feature data). Builds on qrun's lint, dry run and verdict files.

### P2.1 Rows as data (TX-42)

1. Each queue section gets a machine-readable block: a fenced `qb` block under the table, or one `.qb` per section in
   `feature_writeup/testing/rows/`. Owner decision D4.
   - Batches are extracted from it, not rewritten each run.
   - Generalises `hh_queue_to_qb.py`.
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
4. The 387 never-run CQ rows go to the owner playtest checklist, the client automation backlog (scratch plan TX-01 to
   TX-05), or drop. Owner decision D5.
5. [gotchas.md](../.claude/skills/scratch-test/gotchas.md): merge the per-run "Found in rXXXX" sections into topical
   rules. A gotcha now enforced by a check (`grep -c`, jar pinning, unlisted jars) becomes one line naming that check.

**Phase 2 done when:**
- A one-jar change produces a planned run of T1 plus the affected T2 rows, under 1 h.
- The coverage block shows never-run rows below 5%.
- A seeded stale row (source changed) and a seeded dead id are flagged by lint.
- A run with an unlisted jar refuses to start.

## Owner decisions

- D1. Build stamp in the manifest and `ce-build.json` only, file names unchanged (recommended), or also in file names?
- D2. Gates: `installToMods` refuses on a fast-check FAIL (recommended, `-PskipChecks` to override), or warn only?
  Stop hook report-only (recommended) or blocking?
- D3. OK to edit CLAUDE.md ("Done means", "Build and verify", processes line) and the feature-writeup and
  memory-vs-docs memories for P1.3 and P1.4?
- D4. Row data as a fenced block under each section (one file, as today), or `.qb` files next to the queue?
- D5. Never-run client rows: playtest checklist, automation backlog, or drop, per section?
- D6. README index: move the long feature descriptions into the writeups' section 0, so the generated table stays
  short (recommended), or keep them in `features.tsv`?

## Test list (PO-)

Offline (checked by the building agent, results in [process_review.md](process_review.md) History):
- [ ] **PO-01 check_all clean:** `python3 tools/check_all.py --fast` on a clean tree -> exit 0, under 60 s, one line
  per check.
- [ ] **PO-02 seeded break:** add an unguarded optional id to a Creative Delight script -> `guard_check` FAIL, exit 1.
- [ ] **PO-03 install gate:** with the PO-02 break, `./gradlew :creativeDelight:installToMods --offline` -> refused,
  names the check, `mods/` unchanged; with `-PskipChecks` -> installs, ledger row says skipChecks.
- [ ] **PO-04 KNOWN aging:** a KNOWN entry dated 31 days ago -> warning line.
- [ ] **PO-05 Stop hook:** end a session with the PO-02 break -> FAIL lines shown.
- [ ] **PO-06 stamp:** two builds of Common with one change between -> different `Implementation-Version` and
  `ce-build.json`.
- [ ] **PO-07 ledger:** install -> `jar_archive/installed.tsv` row with both stamps.
- [ ] **PO-08 pending:** `jarstamp.py pending` lists a built-not-installed jar and nothing else.
- [ ] **PO-09 deploy mismatch:** staged payload with server and client stamps differing -> `build_deploy.py` refuses.
- [ ] **PO-10 generated README:** `status_index.py` -> README index block regenerated, no hand text lost outside markers.
- [ ] **PO-11 snapshot:** flip one TODO row to Testing -> snapshot counts change by one.
- [ ] **PO-12 verdict to README:** add a verdict file -> the feature's last-run cell updates.
- [ ] **PO-13 legend check:** a row with status "Done" outside the archive -> `--check` FAIL.
- [ ] **PO-14 hand edit:** edit inside a generated block -> `--check` FAIL.

Live (queued when built, per skill `scratch-test`):
- SQ-PO-01: scratch boot shows the `[creativecommon] builds:` line, and its stamps match `jar_archive/installed.tsv`.
- Phase 2 rows (SQ-PO-10 onward) are written when P2.1 is built.

## History

- 2026-11-16: planned from [process_review.md](process_review.md); owner asked for items 1 to 3 and 5 as phase 1,
  item 4 as phase 2.
