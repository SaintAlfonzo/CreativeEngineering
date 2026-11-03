---
name: release
description: Stage, deploy and snapshot a pack release (build_deploy.py payload, changelog freeze, release plan). Use when the user wants to cut, stage or record a version.
---

# Release

Reference: [deploy.md](../../../feature_writeup/deploy.md) (read section 0 and the step you are on), memory
`deploy-pipeline`, [changelog.md](../../../feature_writeup/changelog.md) 3.5-3.6.

1. **Version.** Ask if not given; the owner sets numbers (format `1.1.NN`). Omitting `--version` bumps the
   patch. Never rebuild a deployed version (`deploy/DEPLOYED.json`).
2. **Pre-checks.**
   - In-tree jars in `mods/` match their builds (`IN_TREE_MODS`); build/install any pending one (game closed).
   - Every jar has a `mod_sides.md` row; new kubejs files classified in `KUBEJS_CLASSES`.
   - `unreleased.md` covers the release; `python3 tools/changelog_lint.py` clean.
   - Release plan `feature_writeup/plan_<ver>.md` (if one exists) and README statuses current.
3. **Stage**: `python3 tools/build_deploy.py --notes "..."` (add `--version X`, `--config full`, `--mode install`
   only when asked). Report the preflight warnings and the `- Payload:` line of `deploy/DEPLOY_LOG.md`.
4. **Optional scratch boot** of `deploy/latest/` only if the user asks (memory `scratch-server-verify`).
5. **User copies the payload** and runs `sh apply.sh server|client <folder>`. Then, on their word only:
   `python3 tools/build_deploy.py --snapshot [VER] [--target server|client] [--date ...]`. This freezes
   `changelog/<ver>.md` and restarts `unreleased.md`.
5b. **New server world only** (not an update): the pregen in `deploy/RUNBOOK.md` section 7 (Chunky, then
   `dh pregen`), before anyone joins. Remind the user when a release says the server starts a new world.
6. **Docs**: release row in the plan / [README.md](../../../feature_writeup/README.md) release table; features
   shipped move to DEPLOYED. Never edit a deployed version's changelog file.
