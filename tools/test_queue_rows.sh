#!/usr/bin/env bash
# Offline tests for the tiered scratch queue (feature_writeup/plan_process_overhaul.md Phase 2, PROC-20 to PROC-28):
# queue rows as data (tools/scratch_query/qrows.py), qrun on a rows file, qrun.py plan, stale and dead-id lint
# (qrowlint.py), jar pinning (qpin.py), HARNESS and judge reclassification, the resumable driver (qdrive.py).
# No game: a fake scratch_server.sh and a temp rows dir (tools/scratch_query/qtests.py).
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/scratch_query/qtests.py 2>&1 | grep -v '^pin: '
