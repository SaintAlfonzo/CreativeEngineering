# Development process review (2026-11-16)

A review of how the pack is built: the human design and agent work loop, the doc set, the test layers, builds, installs
and releases. The question was where errors and inconsistencies come from, what takes longer than it should, and what
runs too often or too rarely. The fixes are planned in [plan_process_overhaul.md](plan_process_overhaul.md).

## 0. At a glance

**README index entry, moved here 2026-11-16 (owner ruling D6).** Feature: Process review and overhaul plan (where errors and lost time come from in the human and agent loop: hand-copied status, a queue that only grows, rotted headless tests, rules held by memory, unidentifiable builds, design gaps filled by assumption; phase 1 check command, build stamps, design intake, status as data; phase 2 tiered scratch queue). Status: review written 2026-11-16; plan rulings D1 to D9 2026-11-16; P1.5 commit rule and P1.6 memory move done, the rest Planned

| # | Finding | Cost today | Plan |
|---|---|---|---|
| 1 | Status is hand-copied into 8 to 10 docs per change and drifts | Stale snapshots, contradicting status words, agent time on bookkeeping | Phase 1, P1.4 |
| 2 | The scratch queue only grows; rows go stale and the harness is noisy | 8 to 16 h runs, 2 to 8 harness faults per real FAIL, 19% of rows never run | Phase 2 |
| 3 | Headless tests rotted after the Creative split; nothing runs them | 12 of 15 test scripts fail; everything falls through to scratch runs | Phase 1, P1.1 |
| 4 | Rules are enforced by memory, not by checks that run by themselves; most lints never run on their own | Bugs found by 12 h scratch runs that a 6 s lint catches | Phase 1, P1.1 |
| 5 | A build cannot be identified: every build of a jar has the same name | Mid-run jar swaps, staged payload mismatches, "built, not installed" sprawl | Phase 1, P1.2 |
| 6 | Design intent reaches the agent as bundled notes; gaps are filled by assumption and reversed after the build | Rework (salt, Diet Log, leaf layers, crow, pass-through) | Phase 1, P1.3 |
| 7 | Commits are week-sized (600 to 2,100 files) | Cannot bisect or revert one feature | Phase 1, P1.5 (rule in place) |

Method: counts from the repo on 2026-11-16, every `tools/test_*.sh` and the six lints run once, and two read-only
agent passes: one classified every FAIL / INCONCLUSIVE across the scratch run files, the other mined the plans, deploy.md,
TODO.md, the scratchpad and git history for rework. Numbers marked ~ are approximate.

## 1. Status is copied by hand and drifts

One change touches its writeup, the [README](README.md) index row, its TODO row, a `plan_*.md`, the scratch queue
body and header banner, a run doc, the changelog, sometimes gotchas.md and memory. Melting (2026-11-14) appears in 10
files. Run results are pasted as prose into README status cells, the queue's Run log and the run docs.

Drift found on 2026-11-16:
- TODO.md "Status snapshot (2026-11-03, counts 2026-11-04)" says 1,220 open rows. A recount of the tables gives
  ~1,357: 705 Testing, 469 Planned, 142 In Progress, 26 Bug, 15 Pinned.
- Statuses that are not in the legend are back after the 11-03 sweep normalised them: 24 "Built", 8 "Built <date>",
  3 "Open", 9 "Done", 17 "Complete" in open tables.
- [deploy.md](deploy.md) history stops at 2026-11-03 ("server still 1.1.05"). DEPLOYED.json has 1.2.00, 1.2.01 and
  1.2.02 after that.
- Stale status words:
  - README Creative Worldgen row says "BROKEN ... NOT installed"; it shipped with 1.2.00.
  - creative_split.md:3-5 says wave 8 is not installed; Common has since deployed.
- TODO.md was re-laid out four times in two weeks: 10-19, 10-24, 10-30 and 11-02.
- Memory has 185 files and ~770 KB. 136 are typed `project`, although the policy says status lives in the repo.
- The queue has 2,917 distinct row ids on 3,177 table lines. Its status words come in three spellings (PASS / pass /
  Pass, open / Open / OPEN, stale / Stale).

The CLAUDE.md "Done means" list is right about *what* must be true. Writing each piece by hand is the problem: every
copy is a place to drift, and agents spend a large share of each session on it.

## 2. The scratch queue only grows

- 7,469 lines, 3,177 row lines, ~40 hand-written "added at the end" banners in the header.
- **Never run:** ~562 row ids (19%) never appear in any run file, `perf/runs` verdict or the Run log. 407 of those
  are from sections added before 11-09. This overcounts somewhat, because id ranges are not expanded. Client rows:
  ~387 of 517 CQ rows (75%) never ran; runs mark the client phase "not run" (scratch_run_2026-11-14.md:441).
- **Wall time:** whole-queue runs took 12 h over 50 boots (r1114), ~8 h over ~48 boots (r1115) and ~16 h (r1113).
  Judging adds 45 min to 1 h. Focused runs take 7 min to 1.5 h.
- **Verdict quality:**
  - Of ~95 final FAIL / INCONCLUSIVE rows, ~68% are real product bugs.
  - About 10 were wrong or stale rows, ~9 harness faults (mostly INCONCLUSIVE), ~3 environment drift, ~8 other
    (not reproduced, upstream behaviour, owner spec).
  - Before judging, the raw picture is much noisier: 2 to 8 BATCH-FAULTs per real FAIL (162 mentions). In one
    batch, 12 qrun FAILs became 2 real ones (scratch_run_2026-11-14.md:392,399).
- **Stale rows:**
  - When the gear moved to Equipment, eight rows' namespace regexes went stale (scratch_run_2026-11-06.md:287-317).
  - r1103 listed 21 stale counts, e.g. "277 generated cans, not 228" (perf/runs/r1103/FINAL.md:61-84).
  - SQ-TX-18 cannot run as written (scratch_run_2026-11-16.md:252).
- **Reruns:** 135 row ids appear in 2 or more run files, 17 in 3 or more, SQ-PG-01b in 5. SQ-TPF-10 went PASS, FAIL,
  PASS over three reruns.
- **Gotchas recorded but hit again:**
  - `grep -c` exits 1 on a zero count, which reads as FAIL. Logged 11-06, hit again 11-07, 11-11 and 11-14.
  - Parallel-session jar swaps: logged four times, hit again 11-02b and 11-15.
  - Unlisted jars in `mods/`: logged 11-06, hit again 11-13.
  - About half of the 570-line [gotchas.md](../.claude/skills/scratch-test/gotchas.md) is per-run "Found in rXXXX"
    sections, not merged topical rules.

The queue does find real bugs. Its cost is that rows are prose with no link to the code or jar they test, so nothing
expires them, nothing refreshes them when a feature moves, and every run starts from "the whole queue".

## 3. Headless tests rotted

All 15 `tools/test_*.sh` were run on 2026-11-16. 3 passed: changelog, food_dict, power_grid_solver; the solver failure
it reports is a documented KNOWN. 12 failed:

| Test | Failure | Kind |
|---|---|---|
| test_cd_food | `kubejs/data/creativedelight/kubejs/creativeengineering/baking.js` not found | path moved by the split |
| test_crop_growth | package `com.SaintAlfonzo.creativedelight.crops` does not exist | package moved |
| test_remainder_buffer | `creativeengineering` packages missing | package moved (stale since the carve) |
| test_shields | `tools/shield_test/com/SaintAlfonzo/creativecommon/shield/*.java` not found | path |
| test_structure_groups | sim has no stub for `creativeworldgen.worldgen.StructurePresence` | harness stub missing |
| test_mob_spawns, test_wild_flowers | `genText` not found | generator API renamed |
| test_bee_nests, test_mushroom_spawns, test_wild_crops | script logged warnings / errors | not triaged |
| test_passthrough | 1 FAILED of 292: toml breakStallTicks 3, overrides empty | possibly real, or config drift |
| test_worldgen_tools | snapshot: 244 whole structures, want 279; 79 unmapped, want 44 | possibly real, or snapshot drift |

Each fix is small. The issue is that nothing runs these tests after a refactor, so they decay silently. The cheapest
test layer has been off since the split, and everything has fallen through to the scratch runs.

## 4. Rules depend on memory

- No Claude Code hooks in `.claude/settings.json` or `settings.local.json`, and no git hooks.
- Six lints exist. Only `changelog_lint` runs automatically, inside `build_deploy.py`. Timings on 2026-11-16:

| Lint | Time | Result on 2026-11-16 |
|---|---|---|
| id_owner_lint | 6 s | 0 flagged, 8 unresolved |
| guard_check | 6 s | **27 unguarded rows** (Creative Delight 25, Common 1, Brewing 1); nobody had seen this |
| optional_ref_audit | 3 s | clean |
| design_rules_audit | 9 s | report only |
| texture_provenance | 63 s | ledger: 27 redo, 4 swap |
| changelog_lint | 0.1 s | 0 errors, 55 warnings (all in deployed files, which may not be edited) |

- Example: after the split, the jars broke on optional-mod references in data. A scratch run found it on 11-14
  (creative_split.md:880), not a pre-build lint. The id-namespace fixes then came as four plans in a row:
  plan_cb_publish (11-04), plan_cb_polish (11-06), plan_owner_ids (11-07), plan_cd_namespace (11-09).
- The pack's rules are good, but CLAUDE.md and memory are the only things enforcing them, and a long session loses them.

## 5. Builds cannot be identified

- `mod_version` is fixed per release (`creativecommon-1.2.0.jar` for every build of Common since 11-08), so the same
  file name covers many builds. "Which build is in `mods/`" can only be answered by a byte compare (done for this
  review: all identical except Power Grid, where build -19 is not in `mods/` and -18 differs from its build output).
- About 8 plans each say "built, not installed", each in its own `jar_archive/<plan>/` folder or `build/libs`.
- Effects:
  - Parallel sessions rewrote hard-linked server jars mid-run, crashing the boot with `NoSuchFieldError`
    (scratch_run_2026-11-02b.md:126).
  - The perf suite tested a jar another session had rebuilt (scratch_run_2026-11-15.md:588-591).
  - Release hotfixes of the same kind:
    - 1.0.7: two Brewin' jars with one mod id.
    - 1.0.8: client-only code on the server.
    - 1.1.04: server and client payloads had different jars.
    - 1.1.02: a data format error stopped the pack loading.
    - 1.2.02: shipped a crow spawn crash.
    - 1.1.04 to 1.1.06: three versions in ~5 hours.

## 6. Design intent arrives bundled; gaps are filled by assumption

- The [scratchpad](todo/scratchpad.md) prompt block is one ~300-word paragraph with ~20 changes across melting, salt,
  bamboo, layers and naming. Plans fill the gaps and ask afterwards: "Assumptions I made, tell me if wrong"
  (plan_meals_rolls_bbq.md:88, plan_jei_tab_tweaks.md:33).
- Recent reversals after the build:
  - Salt layers falling (plan_post_scratch_1115.md:269).
  - Acid recipes back to heated (scratchpad.md:5); decision 11 still open.
  - Diet Log back to a single page, after a design that took five owner rounds (raspberry_pie.md:3-5).
  - Leaf layers never become solid (scratchpad.md:48).
  - Crow decision D2 reversed: port Hearth and Harvest's crow instead of Alex's Mobs' (plan_hearth_crow_swap.md:1-8).
  - Pass-through reworked the day it was built (plan_post_scratch_1115b.md).
- Plans built on false premises, where the work was already done: plan_prescratch_fixes.md:9 and
  plan_creative_delight.md:118,121.

None of these were bugs. Each was a design question settled after the work instead of before it. Visual and UI
work (screens, books, textures) is where this costs most, because the owner can only judge it by looking.

## 7. Cadence: too often and not often enough

| Too often | Not often enough |
|---|---|
| Whole-queue scratch runs | Headless tests (effectively never since the split) |
| Hand fan-out of status into every doc, even for small tweaks | Lints (only the changelog lint runs, at release) |
| Reruns; per-run gotcha sections | Commits |
| TODO re-layouts | Installs and releases (built jars of ~8 bundles wait) |
| Rebuilds that are never installed | Owner decision checkpoints before building |
| Hand-written queue header banners | Trimming the queue, gotchas, memory; deploy.md history |

## 8. What the next phase stresses

Earmarked work (README, plan_post_scratch_1115.md, energy_advanced.md, creative_split.md, scratchpad.md):
- Power Grid P11, then advanced energy: multi-jar, with a per-world mode switch whose up/downgrade is destructive.
- The 11-16 salt, melting, layers and Diet Log set: Common, Creative Delight (members) and Compat together.
- The next release, which has to absorb every built-but-uninstalled jar.
- The reserved Creative Computers and Creative Economy jars: more carving, more id moves.
- Scratchpad work in progress: tempad and portals (~30 items), golden food buffs.

Each hits the weak spots above: jar identity (5), cross-jar ids and optional refs (4), rows going stale when
features move (2), and open design questions (6).

## 9. Recommendations

Planned in [plan_process_overhaul.md](plan_process_overhaul.md):
- **Phase 1:**
  - P1.1 one check command and gates.
  - P1.2 build stamps and an install ledger.
  - P1.3 design intake before building.
  - P1.4 status as data, docs as generated views.
- **Phase 2:** a tiered scratch queue with expiry, harness fault classes and pinned jars.

Added after the owner's rulings (2026-11-16 evening):
- P1.5 commit per plan phase, pushed to `agent_staging` (finding 7). The rule is in CLAUDE.md.
- P1.6 project memories moved to [agent_notes/](agent_notes/README.md) (done).
- P1.2 item 7 install dialog.

Not planned: a recurring weekly consolidation pass. P1.6 and P2.5 do one-time clean-ups.

## History

- 2026-11-16: written (review session; read-only apart from this file, the plan and their index rows).
- 2026-11-16 evening: owner rulings recorded in the plan; finding 7 now planned (P1.5); memory move done (P1.6).
- 2026-11-16 night: P1.1 to P1.3 built; offline results in [plan_process_overhaul.md](plan_process_overhaul.md) (tests renamed PO- to PROC-: PO- belongs to potions.md). Section 3 table: 10 of the 12 tests repaired, 3 tests KNOWN on two real gaps (`largecontent/mod-additions #64`, `farming/wild-crops #11`); 27 guard_check hits: 2 real guards added, the rest tool gaps or G5 tables.
- 2026-11-16 late night: plan Phase 2 built (finding 2: tiered queue, rows as data, expiry, pinned jars, resumable driver); offline results PROC-20 to PROC-28 in [plan_process_overhaul.md](plan_process_overhaul.md) test list.
- 2026-11-16 late night: Phase 3 planned in [plan_process_overhaul.md](plan_process_overhaul.md) (clean-up passes, install baseline, features audit and CSV, Phase 1 and 2 fixes, TODO as the single source of truth). Owner ruling D12: the section 7 "TODO re-layouts" row is withdrawn (the owner's choice).
