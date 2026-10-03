#!/bin/bash
# Records the running singleplayer client (its integrated server runs on "Server thread", so the same
# per-mod report works). No file names to type: dump picks one, report defaults to the newest.
#   tools/perf/sp.sh start               start recording the running game (once per launch)
#   tools/perf/sp.sh dump [10m] [tag]    save the recording (optionally only the last 10m) and build the report
#   tools/perf/sp.sh report [file.jfr]   rebuild a report (default: newest perf/recordings/sp-*.jfr)
#   tools/perf/sp.sh stop                stop recording
cd "$(dirname "$0")/../.." || exit 1
DIR=$(pwd -P)
CMD=$1
shift

report() {
    REC=${1:-$(ls -t perf/recordings/sp-*.jfr 2>/dev/null | head -n 1)}
    if [ -z "$REC" ] || [ ! -f "$REC" ]; then
        echo "No recording found${1:+ at $1}. Run: tools/perf/sp.sh dump"
        exit 1
    fi
    python3 tools/perf/perf_report.py "$REC" --mods mods
}

if [ "$CMD" = report ]; then
    report "$1"
    exit
fi

# The client JVM: a java process whose working folder is this instance.
# Only PIDs are printed - the client's full java command line contains its login token.
PID=
for p in $(ps -eo pid=,comm= | awk '$2=="java" {print $1}'); do
    if [ "$(readlink -f /proc/$p/cwd 2>/dev/null)" = "$DIR" ]; then
        PID=$p
    fi
done
if [ -z "$PID" ]; then
    echo "The game isn't running from $DIR."
    exit 1
fi
JCMD="$(dirname "$(readlink -f /proc/$PID/exe)")/jcmd"
[ -x "$JCMD" ] || JCMD=$(command -v jcmd)
if [ -z "$JCMD" ]; then
    echo "No jcmd found next to the game's Java or on PATH."
    exit 1
fi
jc() { "$JCMD" "$PID" "$@" | grep -v '^[0-9]*:$'; }

case "$CMD" in
    start)
        if jc JFR.check name=ce 2>/dev/null | grep -q 'name=ce'; then
            echo "Already recording."
            exit 0
        fi
        # stackdepth only applies before JFR's first use in this launch; later it is ignored with a warning
        jc JFR.configure stackdepth=256 > /dev/null
        jc JFR.start name=ce settings="$DIR/server/perf/ce-server.jfc" disk=true maxage=30m maxsize=512m
        ;;
    dump)
        AGE=$1
        TAG=$2
        mkdir -p perf/recordings
        OUT="perf/recordings/sp-$(date +%Y%m%d-%H%M%S)${TAG:+-$TAG}.jfr"
        jc JFR.dump name=ce filename="$DIR/$OUT" ${AGE:+maxage=$AGE}
        if [ ! -s "$OUT" ]; then
            echo "Dump failed (see above). Is it recording? Run: tools/perf/sp.sh start"
            exit 1
        fi
        echo "Saved $(du -h "$OUT" | cut -f1) to $OUT"
        report "$OUT"
        ;;
    stop)
        jc JFR.stop name=ce
        ;;
    *)
        sed -n '2,7p' "$0"
        exit 1
        ;;
esac
