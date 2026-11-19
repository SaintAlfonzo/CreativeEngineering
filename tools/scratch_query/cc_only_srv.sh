#!/usr/bin/env bash
# Scratch server for the SQ-CCO rows: Creative Common plus ONLY the optional mods it tweaks, none of the other Creative
# jars (Compat, Calendar, Engineering, Equipment, Magic, Delight, Brewing, Devtools, Worldgen, Labels).
#   cc_only_srv.sh OUT LEVEL full|alone|without=<mod key>[,<mod key>...] [prop=value ...]
#     full      Common + every optional mod group (KubeJS stack always present: Common requires it)
#     alone     Common + the KubeJS stack only (no optional mod)
#     without=  full minus the named keys (iw, trims, we, quark, gravity, nea, beacons, portals, heartstone, seasons)
# The lists and the build rules live in modsets/cc_only.toml (plan_rowfix_r1118.md R4 B1); this is
# `qmodset.py build OUT LEVEL B cc_only full|alone [-@KEY ...]`, the same build the driver runs for
# `@@modset cc_only ...`. Refuses while a game JVM is up.
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
OUT=${1:?OUT}; LEVEL=${2:?LEVEL}; MODE=${3:?full|alone|without=...}; shift 3
case "$MODE" in
  full|alone) args=(cc_only "$MODE") ;;
  without=*) args=(cc_only full); IFS=, read -ra ks <<< "${MODE#without=}"; for k in "${ks[@]}"; do args+=("-@$k"); done ;;
  *) echo "mode: full | alone | without=<keys>" >&2; exit 1 ;;
esac
python3 "$INST/tools/scratch_query/qmodset.py" build "$OUT" "$LEVEL" B "${args[@]}" "$@"
echo "cc_only done ($MODE): $(ls "$OUT/srv/mods" | wc -l) jars"
