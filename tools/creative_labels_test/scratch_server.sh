#!/usr/bin/env bash
# Scratch dedicated server for Creative Labels (feature_writeup/sticky_notes.md 6.1).
#   scratch_server.sh setup OUT [LEVEL]   build OUT/srv: superflat creative world, port 25599, offline, every
#                                         non-client jar of mods/ (mod_sides.md) symlinked, kubejs/config copied,
#                                         plus server_probe.js as a scratch-only server script
#   scratch_server.sh boot OUT            start it on a stdin fifo (console.log, logs/latest.log)
#   scratch_server.sh cmd OUT "<command>" send a console command
#   scratch_server.sh stop OUT            stop, then kill the JVM if it hangs after saving
# Every server records JFR (server/perf/ce-server.jfc; CE_PERF=0 at setup turns it off, JFR_MAX caps one boot's
# recording, default 1g). While a perf session is open (tools/perf/session/perf_session.sh), boot, cmd and stop are
# written to its timeline. Nothing else here writes into the instance.
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
HERE="$INST/tools/creative_labels_test"
SRVBASE="$HOME/.local/share/atlauncher/servers/Minecraft1192withForge"
what=${1:?usage: scratch_server.sh setup|boot|cmd|stop OUT ...}
OUT=${2:?OUT dir}
SRV="$OUT/srv"
# perf session timeline (perf.md 14): one line per boot / command / stop while a session is open
tl() { local c="$INST/perf/sessions/CURRENT"; [ -f "$c" ] || return 0
  printf '%s\t%s\t%s\t%s\n' "$(date +%s.%N | cut -c1-14)" "$1" "$SRV" "$2" >> "$INST/perf/sessions/$(cat "$c")/timeline.tsv"; }
case "$what" in
setup)
  LEVEL=${3:-cl_$(date +%s)}
  mkdir -p "$SRV/mods"
  cd "$SRV" || exit 1
  ln -sfn "$SRVBASE/libraries" libraries
  cp "$SRVBASE/run.sh" .
  # ports that read a donor jar refuse to load without their *_upstream folder (Vinery, Miner's)
  for u in "$INST"/*_upstream; do ln -sfn "$u" "$(basename "$u")"; done
  echo "-Xmx6G" > user_jvm_args.txt
  if [ "${CE_PERF:-1}" != 0 ]; then
    # no maxage: the exit dump in perf/exit holds the whole boot, up to JFR_MAX
    mkdir -p perf/exit
    cp "$INST/server/perf/ce-server.jfc" perf/
    echo "-XX:FlightRecorderOptions=stackdepth=256,repository=perf/jfr-repo" >> user_jvm_args.txt
    echo "-XX:StartFlightRecording=name=ce,settings=perf/ce-server.jfc,disk=true,maxsize=${JFR_MAX:-1g},dumponexit=true,filename=perf/exit" >> user_jvm_args.txt
  fi
  echo "eula=true" > eula.txt
  cat > server.properties <<EOF
online-mode=false
server-port=25599
level-name=$LEVEL
level-type=minecraft\:flat
generate-structures=false
spawn-protection=0
view-distance=4
simulation-distance=4
difficulty=peaceful
gamemode=creative
max-tick-time=-1
enforce-secure-profile=false
spawn-monsters=false
spawn-animals=false
EOF
  rm -f mods/*.jar
  grep '^| client |' "$INST/mod_sides.md" | awk -F'`' '{print $4}' > client_jars.txt
  for j in "$INST"/mods/*.jar; do
    b=$(basename "$j")
    grep -qxF "$b" client_jars.txt && continue
    ln -s "$j" "mods/$b"
  done
  rm -rf kubejs config defaultconfigs
  cp -r "$INST/kubejs" kubejs
  cp -r "$INST/config" config
  cp -r "$INST/defaultconfigs" defaultconfigs
  cp "$HERE/server_probe.js" kubejs/server_scripts/zz_cl_probe.js
  echo "srv ready: $(ls mods | wc -l) jars, level $LEVEL"
  ;;
boot)
  cd "$SRV" || exit 1
  rm -f logs/latest.log console.log
  [ -p in.fifo ] || mkfifo in.fifo
  # own feeder pid: stop kills only this one, not other sessions' feeders
  (sleep infinity > in.fifo & echo $! > feeder.pid)
  nohup bash -c 'bash run.sh nogui < in.fifo > console.log 2>&1' >/dev/null 2>&1 &
  tl boot "$(grep -m1 '^level-name=' server.properties | cut -d= -f2) $(grep -m1 '^level-type=' server.properties | cut -d= -f2)"
  echo booting
  ;;
cmd)
  # with no JVM reading the fifo, opening it for write blocks forever
  timeout 10 bash -c 'echo "$1" > "$2"' _ "${3:?command}" "$SRV/in.fifo" || { echo "server not reading" >&2; exit 1; }
  tl cmd "$3"
  ;;
stop)
  timeout 10 bash -c 'echo stop > "$1"' _ "$SRV/in.fifo" || true
  tl stop ""
  for _ in $(seq 60); do
    alive=0
    for p in $(pgrep -x java); do case "$(readlink /proc/$p/cwd)" in "$SRV"*) alive=$p;; esac; done
    [ "$alive" = 0 ] && break
    grep -q "All dimensions are saved" "$SRV/logs/latest.log" 2>/dev/null && sleep 5 && kill -9 "$alive" 2>/dev/null
    sleep 1
  done
  [ -f "$SRV/feeder.pid" ] && kill "$(cat "$SRV/feeder.pid")" 2>/dev/null; rm -f "$SRV/feeder.pid"
  echo stopped
  ;;
esac
