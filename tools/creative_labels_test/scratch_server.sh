#!/usr/bin/env bash
# Scratch dedicated server for Creative Labels (feature_writeup/sticky_notes.md 6.1).
#   scratch_server.sh setup OUT [LEVEL]   build OUT/srv: superflat creative world, port 25599, offline, every
#                                         non-client jar of mods/ (mod_sides.md) symlinked, kubejs/config copied,
#                                         plus server_probe.js as a scratch-only server script
#   scratch_server.sh boot OUT            start it on a stdin fifo (console.log, logs/latest.log)
#   scratch_server.sh cmd OUT "<command>" send a console command
#   scratch_server.sh stop OUT            stop, then kill the JVM if it hangs after saving
# Nothing here writes into the instance.
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
HERE="$INST/tools/creative_labels_test"
SRVBASE="$HOME/.local/share/atlauncher/servers/Minecraft1192withForge"
what=${1:?usage: scratch_server.sh setup|boot|cmd|stop OUT ...}
OUT=${2:?OUT dir}
SRV="$OUT/srv"
case "$what" in
setup)
  LEVEL=${3:-cl_$(date +%s)}
  mkdir -p "$SRV/mods"
  cd "$SRV" || exit 1
  ln -sfn "$SRVBASE/libraries" libraries
  cp "$SRVBASE/run.sh" .
  ln -sfn "$INST/vinery_upstream" vinery_upstream
  echo "-Xmx6G" > user_jvm_args.txt
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
  (sleep infinity > in.fifo &)
  nohup bash -c 'bash run.sh nogui < in.fifo > console.log 2>&1' >/dev/null 2>&1 &
  echo booting
  ;;
cmd)
  echo "${3:?command}" > "$SRV/in.fifo"
  ;;
stop)
  echo stop > "$SRV/in.fifo" || true
  for _ in $(seq 60); do
    alive=0
    for p in $(pgrep -x java); do case "$(readlink /proc/$p/cwd)" in "$SRV"*) alive=$p;; esac; done
    [ "$alive" = 0 ] && break
    grep -q "All dimensions are saved" "$SRV/logs/latest.log" 2>/dev/null && sleep 5 && kill -9 "$alive" 2>/dev/null
    sleep 1
  done
  pkill -fx 'sleep infinity' || true
  echo stopped
  ;;
esac
