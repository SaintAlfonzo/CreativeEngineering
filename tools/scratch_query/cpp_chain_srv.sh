#!/usr/bin/env bash
# Reduced scratch servers for the SQ-CPP rows, built from the pack's own jars (CEPortTesting is left alone).
#   cpp_chain_srv.sh OUT LEVEL chain|compat [prop=value ...]
#     chain   SQ-CPP-04: Creative Brewing chain WITHOUT Creative Engineering
#     compat  SQ-CPP-05: Common + Compat (+ Calendar, Serene Seasons) without Create Recycle or Create
# EXTRA="glob glob" keeps more jars (a missing dependency the boot names). The lists and the build rules live in
# modsets/cpp_chain.toml (plan_rowfix_r1118.md R4 B1); this is `qmodset.py build OUT LEVEL B cpp_chain
# chain|compat [+GLOB ...]`, the same build the driver runs for `@@modset cpp_chain ...`. Refuses while a game JVM is up.
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
OUT=${1:?OUT}; LEVEL=${2:?LEVEL}; MODE=${3:?chain|compat}; shift 3
case "$MODE" in chain|compat) ;; *) echo "mode: chain | compat" >&2; exit 1 ;; esac
args=(cpp_chain "$MODE")
for g in ${EXTRA:-}; do args+=("+$g"); done
python3 "$INST/tools/scratch_query/qmodset.py" build "$OUT" "$LEVEL" B "${args[@]}" "$@"
echo "cpp_chain done ($MODE): $(ls "$OUT/srv/mods" | wc -l) jars"
