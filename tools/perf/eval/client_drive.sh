#!/usr/bin/env bash
# Drives an eval_client.py client through the perf.md section 10 client run and records the timings:
#   client_drive.sh GAME RES     GAME = eval_client.py game dir (world in saves/PerfEval, the only world), RES = results dir
# Waits for the title screen (first "Resource reload finished"), clicks Singleplayer -> the world -> Play Selected World
# on the 854x480 window, then waits for sp_drive.js's phases (joined, idle, 6 teleports, idle, done) and JEI.
# At "done" it dumps the JFR recording to RES/client.jfr and kills the client. Timeline: RES/client_timeline.txt.
set -uo pipefail
GAME=${1:?GAME}; RES=$(realpath -m "${2:?RES}")
INST="$(cd "$(dirname "$0")/../../.." && pwd)"
LOG="$GAME/logs/latest.log"
TL="$RES/client_timeline.txt"
mkdir -p "$RES"
mark() { echo "$(date +%s) $*" | tee -a "$TL"; }
# waits until LOG has PAT, prints the line; TIMEOUT after SECS
wait_log() { for _ in $(seq "${2:-600}"); do l=$(grep -m1 -F -- "$1" "$LOG" 2>/dev/null) && { echo "$l"; return 0; }; sleep 1; done; echo TIMEOUT; return 1; }
client_pid() { for p in $(pgrep -x java); do case "$(readlink /proc/$p/cwd)" in "$GAME"*) echo "$p"; return;; esac; done; }

mark "drive start"
# the client rotates latest.log a few seconds after launch: wait for a log younger than this driver
T0=$(date +%s); for _ in $(seq 120); do [ -f "$LOG" ] && [ "$(stat -c %Y "$LOG")" -ge "$T0" ] && ! grep -q 'Sound engine started' "$LOG" && break; sleep 1; done
mark "sound: $(wait_log 'Sound engine started' 600 | cut -c1-60)"
mark "title reload: $(wait_log 'Resource reload finished' 600 | sed 's/^.*Resource/Resource/')"
sleep 5
W=$(xdotool search --onlyvisible --name '^Minecraft' | head -1)
[ -z "$W" ] && { mark "no window"; exit 1; }
eval "$(xdotool getwindowgeometry --shell "$W")"
click() { xdotool mousemove --window "$W" "$1" "$2" click 1; sleep "${3:-2}"; }
xdotool windowactivate --sync "$W" 2>/dev/null || true
click 425 235 3      # Singleplayer
click 340 133 1      # first world
mark "play click"
click 267 395 1      # Play Selected World
mark "joined: $(wait_log '[perfeval] phase joined' 900 | cut -c1-40)"
mark "jei: $(wait_log 'Starting JEI GUI' 900 | cut -c1-40)"
mark "explore end: $(wait_log '[perfeval] phase explore_end' 1200 | cut -c1-40)"
mark "done: $(wait_log '[perfeval] phase done' 600 | cut -c1-40)"
P=$(client_pid)
mark "client pid ${P:-none}"
[ -n "$P" ] && jcmd "$P" JFR.dump name=ce filename="$RES/client.jfr" 2>&1 | tail -2 | tee -a "$TL"
cp "$LOG" "$RES/client_latest.log"
[ -n "$P" ] && kill "$P"
for _ in $(seq 30); do [ -z "$(client_pid)" ] && break; sleep 1; done
P=$(client_pid); [ -n "$P" ] && kill -9 "$P"
[ -f "$RES/client.jfr" ] || cp "$GAME/perf/exit.jfr" "$RES/client.jfr" 2>/dev/null
cp "$GAME/logs/kubejs/client.log" "$RES/client_kubejs.log" 2>/dev/null
mark "client stopped"
