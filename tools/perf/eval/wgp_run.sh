#!/usr/bin/env bash
# One worldgen A/B world of scratch_queue.md "Worldgen hot spots" (SQ-WGP) and "First-tick water" (SQ-FTW), start to stop:
#   wgp_run.sh OUT LEVEL      settings from the environment (defaults = as shipped):
#     DF=true|false  TC=true|false  GRID=n  CLIFFS=keep|rm  LOG=ALL|CANCELLED  FTW=true|false  EVENTS=on|off
#     PREGEN=400 (Chunky radius in blocks at 4000 4000, 0 = none)  CENSUS=96 (radius in chunks, 0 = none)
# Steps: eval_server.sh setup, toml edits, boot, entity snapshot 120 s after Done (SQ-FTW-02), /forge tps at Done + 240 s,
# /wgp cliffs + dims, pregen with its own JFR recording (perf/pregen.jfr), census, fill lines, stop.
# Results (logs, timings, census, fill.txt, entity census, pregen.jfr) go to RES=perf/wgp/LEVEL in the instance; srv/ is
# deleted at the end unless KEEP=1 (disk).
set -uo pipefail
INST="$(cd "$(dirname "$0")/../../.." && pwd)"
EV="$INST/tools/perf/eval/eval_server.sh"
OUT=${1:?OUT}; LEVEL=${2:?LEVEL}
SRV="$OUT/srv"
RES="${RES:-$INST/perf/wgp/$LEVEL}"
DF=${DF:-true}; TC=${TC:-true}; GRID=${GRID:-4}; CLIFFS=${CLIFFS:-keep}; LOG=${LOG:-CANCELLED}; FTW=${FTW:-true}
EVENTS=${EVENTS:-on}; PREGEN=${PREGEN:-400}; CENSUS=${CENSUS:-96}
# perf-only series: touch perf/wgp/NO_CENSUS to skip the census in every later run
[ -f "$INST/perf/wgp/NO_CENSUS" ] && CENSUS=0
mkdir -p "$RES"
TL="$RES/timeline.txt"
mark() { echo "$(date +%s) $*" | tee -a "$TL"; }
cmd() { bash "$EV" cmd "$OUT" "$1"; }
wait_for() { bash "$EV" wait "$OUT" "$1" "${2:-900}"; }
srv_pid() { for p in $(pgrep -x java); do case "$(readlink /proc/$p/cwd)" in "$SRV"*) echo "$p"; return;; esac; done; }
# waits until the log has N lines matching PAT (the log grows, so grep -c)
wait_count() { for _ in $(seq "${3:-600}"); do [ "$(grep -c -- "$1" "$SRV/logs/latest.log" 2>/dev/null)" -ge "$2" ] && return 0; sleep 1; done; return 1; }

: > "$TL"
mark "level $LEVEL DF=$DF TC=$TC GRID=$GRID CLIFFS=$CLIFFS LOG=$LOG FTW=$FTW EVENTS=$EVENTS PREGEN=$PREGEN CENSUS=$CENSUS"
if [ "$EVENTS" = off ]; then JFR_MAX=400m bash "$EV" setup "$OUT" "$LEVEL" events-off; else JFR_MAX=400m bash "$EV" setup "$OUT" "$LEVEL"; fi
cd "$SRV" || exit 1
T=config/creativeengineering-common.toml
# [worldgen.systems] keys new in this jar may be missing from the copied toml: set or add them
setkey() { # section key value
  if awk -v s="[$1]" -v k="$2" '$0 ~ "^[ \t]*\\[" {in_s = ($0 ~ "^[ \t]*\\" s "[ \t]*$")} in_s && $1 == k {f=1} END {exit !f}' "$T"; then
    awk -v s="[$1]" -v k="$2" -v v="$3" '$0 ~ "^[ \t]*\\[" {in_s = ($0 ~ "^[ \t]*\\" s "[ \t]*$")} in_s && $1 == k {sub(/=.*/, "= " v)} {print}' "$T" > "$T.new"
  else
    awk -v s="[$1]" -v k="$2" -v v="$3" '{print} $0 ~ "^[ \t]*\\" s "[ \t]*$" {print "\t\t" k " = " v}' "$T" > "$T.new"
  fi
  mv "$T.new" "$T"
}
setkey worldgen.systems dimensionFilter "$DF"
setkey worldgen.systems sharedTerrainCache "$TC"
setkey worldgen.systems firstTickWater "$FTW"
setkey worldgen.earth_fill sampleGrid "$GRID"
setkey worldgen.earth_fill log "\"$LOG\""
setkey worldgen.water_fill log "\"$LOG\""
[ "$CLIFFS" = rm ] && rm -f kubejs/data/tectonic/worldgen/density_function/overworld/legacy/cliffs.json
grep -nE '^\s*(dimensionFilter|sharedTerrainCache|firstTickWater|sampleGrid|log) =' "$T" | grep -v 'log = false' >> "$TL"
ls kubejs/data/tectonic/worldgen/density_function/overworld/legacy/ >> "$TL" 2>&1

mark boot
bash "$EV" boot "$OUT" || { mark "boot refused"; exit 1; }
r=$(wait_for "Done (" 1200); mark "done: $r"
# an FML crash leaves the JVM alive and deaf to `stop`: kill it by its cwd
[ "$r" = CRASH ] || [ "$r" = TIMEOUT ] && { cp logs/latest.log "$RES/"; cp -r crash-reports "$RES/" 2>/dev/null; bash "$EV" stop "$OUT"
  p=$(srv_pid); [ -n "$p" ] && { kill -9 "$p"; mark "killed $p"; }; exit 2; }
PID=$(srv_pid); mark "pid $PID"

# SQ-FTW-02: item entities around spawn after the spawn chunks have ticked 120 s
sleep 120
cmd "save-all flush"; wait_count "Saved the game" 1 120
mkdir -p "$RES/ftw_snap"; cp -r "$LEVEL/entities" "$RES/ftw_snap/"
mark "entity snapshot"
python3 "$INST/tools/perf/eval/entity_census.py" "$RES/ftw_snap" > "$RES/entity_census.txt" 2>&1
rm -rf "$RES/ftw_snap"

sleep 120
cmd "forge tps"; sleep 3; mark "tps: $(grep -E 'Overall *:' logs/latest.log | tail -1 | sed 's/^.*Overall/Overall/')"
cmd "wgp cliffs"; cmd "wgp dims"; sleep 5
cmd "ceperf status"; sleep 2

if [ "$PREGEN" -gt 0 ]; then
  jcmd "$PID" JFR.start name=pregen settings="$SRV/perf/ce-server.jfc" disk=true > /dev/null
  cmd "chunky center 4000 4000"; cmd "chunky radius $PREGEN"; sleep 1
  mark "pregen start"; cmd "chunky start"
  r=$(wait_for "Task finished" 1800); mark "pregen: $r"
  jcmd "$PID" JFR.stop name=pregen filename="$RES/pregen.jfr" > /dev/null
  cmd "forge tps"; sleep 3; mark "tps after pregen: $(grep -E 'Overall *:' logs/latest.log | tail -1 | sed 's/^.*Overall/Overall/')"
fi
if [ "$CENSUS" -gt 0 ]; then
  mark "census start"; cmd "celocate census $CENSUS 4000 4000"
  r=$(wait_for "census done" 3600); mark "census: $r"
  cp kubejs/exported/worldgen/locate_census.json "$RES/" 2>/dev/null
fi
grep -E '\[(earth|water)-fill\]' logs/latest.log | sed 's/^.*\(\[\(earth\|water\)-fill\]\)/\1/' | sort -u > "$RES/fill.txt"
grep -c . "$RES/fill.txt" | sed 's/^/fill lines: /' >> "$TL"
mark stop
bash "$EV" stop "$OUT"
for _ in $(seq 60); do [ -z "$(srv_pid)" ] && break; sleep 2; done
p=$(srv_pid); [ -n "$p" ] && { kill -9 "$p"; mark "killed $p"; }
gzip -c logs/latest.log > "$RES/latest.log.gz"
grep -E 'Done \(|Task finished|\[wgp\]|\[ceperf\]|Mean tick time|census|DimensionFilter|Exception|Error occurred while handling event|mixin.*(warn|fail)' logs/latest.log \
  | grep -vE '\[(earth|water)-fill\]' | head -400 > "$RES/key_lines.txt"
cp -r crash-reports "$RES/" 2>/dev/null
mark finished
if [ "${KEEP:-0}" != 1 ]; then cd "$OUT" && rm -rf "$SRV"; fi
