# Long-running scratch runner: pick up after r1126q (prompt)

Written 2026-11-26 by the r1126q orchestrator session for the owner to paste into a fresh session. Everything below the
line is the prompt.

---

You are the long-running scratch runner for the CreativeEngineering2 pack. The owner authorizes this run: you may boot
throwaway scratch servers (skill `scratch-test` part B) for up to 72 hours from your first boot. Your goal is to step
through the scratch queue in the priority order below, fixing batch faults as you go, and to leave the queue, the run
doc and RUNNER.md in a state the next session can resume from at any moment.

## Start

1. `python3 tools/session_role.py test`. Read CLAUDE.md, `.claude/skills/scratch-test/SKILL.md` (all of part B) and
   `.claude/skills/scratch-test/gotchas.md`.
2. Read [scratch_run_2026-11-26.md](scratch_run_2026-11-26.md) sections 1 to 7 (the previous run, r1126q: phase W
   writer rewrites, phase R1, its wind-down, the Chunky `/reload` fault and its `qdrive.py preinstall_probes` fix, the
   `drive_stallwait.sh` stall watch) and [RUNNER.md](RUNNER.md). If RUNNER.md is not `State: idle`, stop and ask the
   owner.
3. Claim RUNNER.md as a new run, `r<MMDD>lr` (today's date). Do B1 preflight (`check_all.py --fast`, `game_jvms.sh`
   empty, `df -h /`, `jarstamp.py pending` and `diff`, queue pinned notes). Open the perf session
   (`perf_session.sh start <run>`). Add a new section to today's run doc (or a new `scratch_run_<date>.md`) with scope,
   tier table and run notes.

## Order of work

Build every tier's row list fresh with `python3 tools/scratch_query/qtriage.py` (buckets and risk ranking are in its
docstring); rebuild before each tier, because the previous tier changes row states. Turn a list into a drive with
`qrerun.py LIST --out PLAN.json`, pre-screen it (`qprescreen.py --plan-json PLAN.json`, fix the likely rows first),
then `qrun.py drive start <run><tier> --plan-json PLAN.json`. Server and client rows: server only (see "Not in scope").

0. **Leftovers from r1126q** (short):
   - The drive stopped at step 38 of 51. Steps 38 to 51 are deferred: FGD, FTW, IP, OB, RING, SCAT, ST, SVA, TER, UC,
     VH and SQ-SCAT-08 (long). Resume it with
     `systemd-run --user --unit=r1126q-drive3 --working-directory=<instance> bash perf/runs/r1126q/resume_drive.sh`
     (the script sources env.sh and runs `qrun.py drive resume r1126q`). Steps 38, 39, 40 and 47 use Chunky: confirm
     that their prep logs say "probes copied before boot".
   - The 17 batch-fault fixes waiting for a rerun: `qrerun.py perf/runs/r1126q/rerun.txt`, then
     `qrun.py drive start r1126qr --plan-json perf/runs/r1126q/rerun.plan.json` with env.sh sourced.
   - Three batch faults that need a rewrite before a rerun:
     - SQ-AR-01: the recipe readout prints no ingredients.
     - SQ-HT-02: needs `@@prep A: spawn-animals=true` on HT.qb section 1, which HT-01 and HT-03 share.
     - SQ-IPO-02: no truss formed (Immersive Posts truss mechanics).
   - Jar note:
     - r1126q ran on the installed jars with NOSWAP.
     - Creative Calendar and Creative Loot have newer edc3667aa builds that are built but not installed. Check
       `jarstamp.py pending`; rows for an uninstalled build wait.
   - `check_all.py --fast` failed `test_prefix_lint` at r1126q's start: 29 RP-/CC- ids in
     `project_pages/screenshots.md`, from commit 5b7e8f856. It is not a scratch fault. If it still fails, note it
     and go on.
1. **Jar-changed rows** (last PASS, a jar changed since), highest risk first, server phase:
   `qtriage.py --ids jar_changed --not-phase worldgen,long --min-risk 40 --top 400`. Then the rest of the jar-changed
   server rows in risk order in slices of about 200 (`--top`, minus the ids already run).
2. **Rewritten rows**: rows whose text was rewritten since their last result (the r1126q writers set
   `@reviewed 2026-11-26` on about 180 former batch faults and about 120 rewritten or new rows):
   `qtriage.py --ids bf_rerun,rerun --not-phase worldgen,long --top 0`.
3. **Never-run rows**: `qtriage.py --ids never --not-phase worldgen,long --top 0`.
4. **Worldgen rows** from all of the above (fresh world per group, as the drive does):
   `qtriage.py --ids jar_changed,bf_rerun,rerun,never --phase worldgen --top 2000`.
5. **Then keep stepping through the queue**: long rows (`--phase long,longwait`), the remaining failed rows whose
   jars changed since, and any row that tiers 1 to 4 left as BATCH-FAULT after one fix. When the runnable queue is
   empty, write steps for the server drafts the r1126q writers did not skip on principle (the writers' skip lists are
   in the run doc; a draft is skipped for good when it needs a client, a missing fixture or tool, or an owner ruling),
   lint them, and run them.

Skip, never run: `stale` and `outdated` rows (sources changed or ids gone: rewrite first, then run in tier 5), `hold`
rows (failed, nothing changed since: the same result again), `history` rows (run-log placeholders), superseded and
owner rows.

## Batch faults: fix. Behavior bugs: report only

- A batch fault (wrong regex, coordinates, unloaded chunk, missing forceload, wrong id, wait too short, a tool or
  harness defect) is fixed and the row rerun in the same run (skill run default 11). For this run the owner extends
  default 11: a fault whose fix needs a tool change in `tools/scratch_query/` or a section-level `@@prep` / `@@modset`
  change may also be fixed, when you check every other row that shares it (`qprescreen.py`, `qrun.py lint --rows`) and
  log it as `BF-FIX` in the run notes. Fixes follow the documented behavior (writeup, plan ruling, changelog), never
  the observed output. Same row faulting twice after fixes: leave it BATCH-FAULT with the cause and move on.
- An environment fault that would hit the next rows stops the drive at a row boundary until fixed (run default 1).
- A behavior bug (the product does the wrong thing with a sound test) is a finding only: evidence in the run notes
  (test id, log lines, suspected file), a TODO row through `tools/todo.py`, and the run goes on. Never edit
  `development/`, `kubejs/`, `data/`, `configs_edited/` or tracked `config/` to make a row pass. When unsure whether a
  fault is the test or the product, treat it as the product.

## Polling: once an hour, 72 hours

This overrides the skill's 10-minute stall watch and 12-hour cap for this run only.

- Between checks, wait in the background:
  `bash tools/scratch_query/drive_stallwait.sh <RUN> "" 60`, run with `run_in_background` (it returns on a step
  change, a stopped driver, or 60 minutes without a server or row log write). Each time it returns, do one check, then
  start the next wait. Do not poll in a foreground loop and do not check more often than that.
- A check: drive state (`perf/runs/<RUN>/drive/state.json`, `drive.log` tail), `game_jvms.sh` (never `ps -f` /
  `pgrep -af`), the newest crash report, `tail -n 5` of the step's `latest.log`. Then: judge finished steps (spawn a
  `scratch-judge` agent per step batch), apply simple batch-fault fixes, update RUNNER.md (`Phase`, `ETA`, `Updated`)
  and the run notes with the clock time.
- Silent for 60 minutes with the JVM alive: one `jstack` into the run notes. Silent for another 60: stop the drive at
  the row boundary, judge the row from its log, mark it as stalled, and resume the drive (the owner has authorized
  this; do not wait for an answer).
- **Stop after 72 hours** from the first boot. Stop at a row boundary. Then do the B5 write-back: Run log entry,
  test lists, TODO rows, gotchas, `perf_session.sh stop` + `report`. Release RUNNER.md to idle. Hand over with counts,
  the FAILs one line each, deferred tiers and the exact resume command.

## Keep it resumable

- Run notes at every step boundary, fix and decision, with `YYYY-MM-DD HH:MM` times. A fresh session must be able to
  continue from RUNNER.md plus the run doc alone (context compaction will happen in a 72-hour run).
- Commit at each tier boundary: the run doc, the `.qb` fixes, and tool fixes, staged by path. Generated
  `status_index.py` output goes in its own commit. Use the repo's configured author with the Co-Authored-By trailer, and
  push with `git push origin HEAD:agent_staging`. If the push is not a fast-forward, stop committing and note it.
- One `.qb` owner at a time: if you spawn helper agents that edit rows, give each a disjoint set of files and never edit
  the rows of the step that is running.
- Agent briefs follow CLAUDE.md "Agent-to-agent messages". Use `scratch-judge` for judging, `doc-pass` for row text
  passes, and `general-purpose` for tool code.

## Not in scope

Client rows (`CQ-*`, phase client) need the owner's display: skip them unless the queue's pinned notes or the owner say
otherwise. No product fixes, no jar builds or installs, no claude.ai artifacts.

## Open findings from r1126q phase W (add TODO rows in your first write-back if the run doc does not list them yet)

- SQ-SB-04: meta structure spots spawn no evoker, illusioner or zombie in 4400 night ticks, while the lone tower
  control spawns pillagers and vindicators (suspect StructureSpawnBoxes.inMeta / MetaStructure.parts).
- SQ-META31-07: spawn_boxes.json still boxes the witch wagon in witch_huts_full (ST-317 spec conflict); the row may FAIL
  on code.
- SQ-RD-26: wild bell peppers, coffee and cotton counted 0 in jungle and savanna (r1124fxr2).
- SQ-UPG-14: the release_1202 deploy filter drops the Storage Delight jar from boot 1 while the fixture reports OK.
- SQ-RSP-02 (95 recipes vs 84 expected) and SQ-FDB-06 (control farmland not trampled): owner calls.
- SQ-CWG-09: Forgiving Void did not lift the bot in the End (r1120nv); the row is now a hard check.
- Tool gaps blocking rows (code, not row fixes): `/cdevtest rdetail` lacks IE sawmill secondaries and IE arc furnace
  ingredients (SQ-EN-25, SQ-RC-01); devtools TestData.field() reads public fields only (SQ-CDF-06); vintage_probe.js
  Rhino overload ambiguity (SQ-VTD4-08); gen_can_types.py reads only the instance's exported dir (SQ-GLD2-05).
- Doc drift: golden_food.md counts 46/41 (code logs 39); food.md says 31 food types (32 generated); item_merges.md
  section 3 lists the removed mini cantaloupe grid and section 6 names creativeengineering-swaps.toml (real:
  creativecommon-swaps.toml); salt.md sections 7 and 8 disagree (rows follow 8) and name `playerLickChance` (code key
  `saltPlayerLickChance`); biome_tuning.md line 226 "delete the file to force a solve" is stale (the jar copy is used);
  potions.md says `Lingerification: generated N` logs at load (client only); the pumpkin knife drop is 8 in loot.js
  (check the writeup and changelog); the dragon armor 8/13/11/8 in plan_post_scratch_1115 is now 9/12/11/8; the
  wild hops feature is a trunk vine, so TODO scratch-1115-follow-ups #13 may be a false alarm.
