---
name: release
description: Stage, deploy and snapshot a pack release (build_deploy.py payload, changelog freeze, release plan). Use when the user wants to cut, stage or record a version.
---

# Release

Reference: [deploy.md](../../../feature_writeup/deploy.md) (read section 0 and the step you are on), agent note
`deploy-pipeline`, [changelog.md](../../../feature_writeup/changelog.md) 3.5-3.6.

Owner rules (deploy.md section 4): **no content test before a deploy** (the scratch queue tests content, not the
release); **Distant Horizons settings are player config**, never pushed after 1.0.4 (`CONFIG_CLASSES` keeps it `player`).

1. **Version.** Ask if not given; the owner sets numbers (format `1.1.NN`). Omitting `--version` bumps the
   patch. Never rebuild a deployed version (`deploy/DEPLOYED.json`).
2. **Pre-checks.**
   - `python3 tools/check_all.py --full` exits 0 (stage runs it too and stops on a FAIL).
   - In-tree jars in `mods/` match their builds: `python3 tools/jarstamp.py pending` empty; build/install any pending
     one. Stamps of a dirty build warn at stage: commit first.
   - Every jar has a `mod_sides.md` row; new kubejs files classified in `KUBEJS_CLASSES`.
   - `unreleased.md` covers the release; `python3 tools/changelog_lint.py` clean.
   - Release plan `feature_writeup/plan_<ver>.md` (if one exists) and README statuses current.
   - No open TODO row says "Blocks the next release" (`grep -n "Blocks the next release" feature_writeup/todo/TODO.md`);
     report any to the owner before staging.
2b. **Clean-up pass** (plan_process_overhaul.md P3.6, D10), before staging. Run the fixers, then the report:
   - `python3 tools/scratch_query/qrun.py archive --apply` (and the superseded sweep), then `python3 tools/status_index.py`;
   - `python3 tools/build_deploy.py --backfill-history <versions>` for any `deploy_missing_bullets`;
   - `python3 tools/jarstamp.py prune`;
   - merge any "Found in rXXXX" section of the scratch-test gotchas into its topic; strip status words from agent
     notes (pointers to the owning doc); merge a note that repeats its writeup into the writeup;
   - mark plans whose rows are all Complete as closed;
   - `python3 tools/cleanup_check.py --report feature_writeup/status/cleanup_<date>.md`: every remaining warning is
     listed there with a reason; hand the list to the owner.
3. **Stage**: `python3 tools/build_deploy.py --notes "..."` (add `--version X`, `--config full`, `--mode install`
   only when asked). Report the preflight warnings and the `- Payload:` line of `deploy/DEPLOY_LOG.md`.
4. **Optional scratch boot** of `deploy/latest/` only if the user asks (memory `scratch-server-verify`).
5. **User copies the payload** and runs `sh apply.sh server|client <folder>`. Then, on their word only:
   `python3 tools/build_deploy.py --snapshot [VER] [--target server|client] [--date ...]`. This freezes
   `changelog/<ver>.md` and restarts `unreleased.md`.
5b. **New server world only** (not an update): the pregen in `deploy/RUNBOOK.md` section 7 (Chunky, then
   `dh pregen`), before anyone joins. Remind the user when a release says the server starts a new world.
6. **Docs**: release row in the plan / [README.md](../../../feature_writeup/README.md) release table; run
   `python3 tools/status_index.py`: the snapshot records each own jar's stamp, so the shipped features derive DEPLOYED
   by themselves (no hand edit). Never edit a deployed version's changelog file.
