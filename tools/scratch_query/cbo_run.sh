#!/bin/bash
# One port_chain.sh scratch boot for a batch run (r1106cbo, scratch_queue.md "SQ-CBO" / "SQ-CBC"):
# setup, boot, wait, qrun the batch, stop, save the logs into RES, delete OUT.
#   cbo_run.sh NAME MODE BATCH          MODE = chain | cooking | optional | instance (port_chain.sh)
# Env: OUTBASE (required, under the scratchpad), RUN (results under perf/runs/RUN/NAME, default r1106cbo),
#   GROUP (A), ADD / DROP / NOUPSTREAM (passed to port_chain.sh), PRE (bash run in OUT/srv before the boot, e.g. a
#   qrun.py toml edit or a jar copy), REUSE=1 (keep the OUT of an earlier call with the same NAME: restart only),
#   KEEP=1 (leave OUT for a REUSE call), NOQRUN=1 (boot expected to fail: only wait and save logs).
# Prints one status line per step. Refuses to start while any game JVM is up.
set -uo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"; D="$INST/tools/scratch_query"
NAME=${1:?NAME}; MODE=${2:?MODE}; BATCH=${3:-}
OUT="${OUTBASE:?OUTBASE}/$NAME"; RES="$INST/perf/runs/${RUN:-r1106cbo}/$NAME"
[ -n "$(bash "$D/game_jvms.sh")" ] && { echo "$NAME: a game JVM is up, not booting"; bash "$D/game_jvms.sh"; exit 1; }
mkdir -p "$RES"
if [ "${REUSE:-0}" != 1 ]; then
  rm -rf "$OUT"; mkdir -p "$OUT"
  GROUP=${GROUP:-A}
  bash "$D/port_chain.sh" server "$OUT" "$NAME" "$GROUP" "$MODE" > "$RES/setup.txt" 2>&1 \
    || { echo "$NAME: port_chain setup failed"; tail -5 "$RES/setup.txt"; exit 1; }
fi
if [ -n "${PRE:-}" ]; then (cd "$OUT/srv" && eval "$PRE") >> "$RES/setup.txt" 2>&1 || { echo "$NAME: PRE failed"; tail -3 "$RES/setup.txt"; exit 1; }; fi
echo "$NAME: $(ls "$OUT/srv/mods" | wc -l) jars, booting $(date +%H:%M:%S)"
bash "$INST/tools/creative_labels_test/scratch_server.sh" boot "$OUT" >/dev/null
r=$(bash "$D/wait_boot.sh" "$OUT" 600); echo "$NAME: $r" | head -3
if [ "${r%% *}" = DONE ] && [ "${NOQRUN:-0}" != 1 ] && [ -n "$BATCH" ]; then
  python3 "$D/qrun.py" run "$OUT" "$BATCH" "$RES" > "$RES/qrun.txt" 2>&1
  tail -2 "$RES/qrun.txt"
fi
bash "$D/stop_wait.sh" "$OUT" 180 | head -1
tag=$( [ "${REUSE:-0}" = 1 ] && echo "_re" )
gzip -c "$OUT/srv/logs/latest.log" > "$RES/latest$tag.log.gz" 2>/dev/null
for f in startup server; do [ -f "$OUT/srv/logs/kubejs/$f.log" ] && cp "$OUT/srv/logs/kubejs/$f.log" "$RES/kjs_$f$tag.log"; done
cp "$OUT"/srv/crash-reports/*.txt "$RES/" 2>/dev/null
[ "${KEEP:-0}" = 1 ] || rm -rf "$OUT"
echo "$NAME: finished $(date +%H:%M:%S)"
