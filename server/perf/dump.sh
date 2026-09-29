#!/bin/bash
# Saves the server's always-on performance recording to perf/recordings/ (see perf/README.md).
#   perf/dump.sh              everything kept (up to the last 30 minutes)
#   perf/dump.sh 10m          only the last 10 minutes (s, m or h)
#   perf/dump.sh 10m lagspike adds a word to the file name
# Run it on the server machine, as the user that runs the server, while the server is up.
cd "$(dirname "$0")/.." || exit 1
SERVER_DIR=$(pwd -P)
AGE=$1
TAG=$2

# The server JVM: a java process started with our recording, whose working folder is this server.
# Only PIDs are printed - a client's full java command line contains its login token.
PID=
for p in $(ps -eo pid=,comm=,args= | awk '$2=="java" && /StartFlightRecording=name=ce/ {print $1}'); do
    if [ "$(readlink -f /proc/$p/cwd 2>/dev/null)" = "$SERVER_DIR" ]; then
        PID=$p
    fi
done
if [ -z "$PID" ]; then
    echo "No running server with the recording found in $SERVER_DIR."
    echo "Is it running, and was it started with LaunchServer.sh (JVMARGS has -XX:StartFlightRecording)?"
    exit 1
fi

# jcmd from the same Java that runs the server; fall back to one on PATH.
JCMD="$(dirname "$(readlink -f /proc/$PID/exe)")/jcmd"
[ -x "$JCMD" ] || JCMD=$(command -v jcmd)
if [ -z "$JCMD" ]; then
    echo "No jcmd found (the server's Java is a JRE without tools). Without it, copy the finished chunk"
    echo "files instead: perf/jfr-repo/<newest folder>/*.jfr - perf_report.py reads a folder of them."
    exit 1
fi

mkdir -p perf/recordings
OUT="$SERVER_DIR/perf/recordings/server-$(date +%Y%m%d-%H%M%S)${TAG:+-$TAG}.jfr"
"$JCMD" "$PID" JFR.dump name=ce filename="$OUT" ${AGE:+maxage=$AGE} | grep -v '^[0-9]*:$'
if [ -s "$OUT" ]; then
    echo "Saved $(du -h "$OUT" | cut -f1) to $OUT"
    echo "Copy it to the pack's perf/recordings/ and run: python3 tools/perf/perf_report.py perf/recordings/$(basename "$OUT")"
else
    echo "Dump failed (see above)."
    exit 1
fi
