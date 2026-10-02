#!/bin/bash
# Gathers what's needed to diagnose a crash or lag on this server into one archive (see perf/README.md).
#   perf/report.sh               window = the last 30 minutes (all the recording keeps)
#   perf/report.sh 10m           a shorter window (s, m or h)
#   perf/report.sh 10m lagspike  adds a word to the name
# Writes perf/reports/<date>-<time>[-tag].zip (.tar.gz when zip isn't installed). Copy it to the dev instance
# and run there:  python3 tools/perf/ingest_report.py <archive>
#
# Server running: live JFR dump, thread dumps, logs. Server down (after a crash): the window ends at the last
# log line, and the recording folder the dead JVM left in perf/jfr-repo/ is taken instead.
# Only listed files are copied. Values of password/token/secret keys in server.properties and the server
# configs are blanked out of every copied text file, and player IPs are masked. The world, config/, ops,
# whitelist and bans are never copied (config/ and mods/ are only hashed).
# Run it as the user that runs the server: jcmd and /proc can't see another user's JVM.
cd "$(dirname "$0")/.." || exit 1
SERVER_DIR=$(pwd -P)
AGE=${1:-30m}
TAG=$2
LOOKBACK_H=24   # crash reports, hs_err files, rotated logs and leftover recordings this recent are included

case "$AGE" in
    *[0-9]s) AGE_S=${AGE%s} ;;
    *[0-9]m) AGE_S=$(( ${AGE%m} * 60 )) ;;
    *[0-9]h) AGE_S=$(( ${AGE%h} * 3600 )) ;;
    *) echo "Window must be a number with s, m or h, like 10m (got '$AGE')."; exit 1 ;;
esac
case "$TAG" in *[!A-Za-z0-9_-]*) echo "The tag may only hold letters, digits, - and _."; exit 1 ;; esac

NOW=$(date +%s)
NAME=$(date +%Y%m%d-%H%M%S)${TAG:+-$TAG}
D="$SERVER_DIR/perf/reports/$NAME"
mkdir -p "$D" || exit 1
NOTES="$D/.notes"
: > "$NOTES"
note() { echo "- $*" >> "$NOTES"; }
have() { command -v "$1" > /dev/null 2>&1; }
TO=; have timeout && TO="timeout 60"

# --- the server JVM: a java process whose working folder is this server. Only PIDs are printed: a client's
# full java command line holds its login token.
PID=
for p in $(ps -eo pid=,comm= | awk '$2=="java" {print $1}'); do
    [ "$(readlink -f /proc/$p/cwd 2>/dev/null)" = "$SERVER_DIR" ] && PID=$p
done
HAS_JFR=; JCMD=
if [ -n "$PID" ]; then
    tr '\0' '\n' < /proc/$PID/cmdline 2>/dev/null | grep -q '^-XX:StartFlightRecording=name=ce' && HAS_JFR=1
    JCMD="$(dirname "$(readlink -f /proc/$PID/exe)")/jcmd"
    [ -x "$JCMD" ] || JCMD=$(command -v jcmd)
    [ -n "$JCMD" ] || note "No jcmd (the server's Java is a JRE): no live recording dump, no thread dumps."
    [ -n "$HAS_JFR" ] || note "The server runs without the recording (not started by LaunchServer.sh with the JFR options)."
    END=$NOW
    echo "Server running, PID $PID."
else
    END=$(stat -c %Y logs/latest.log 2>/dev/null || echo "$NOW")
    note "No running server found in $SERVER_DIR (stopped or crashed, or run as another user). The window ends at the last write to logs/latest.log."
    echo "No running server found; the window ends at the last log write ($(date -d "@$END" '+%F %T'))."
fi
START=$(( END - AGE_S ))
LOOKBACK=$(( NOW - LOOKBACK_H * 3600 ))

# --- redaction: secret values from server.properties and server configs, blanked out wherever they appear
LEVEL=$(sed -n 's/^level-name=//p' server.properties 2>/dev/null | tr -d '\r')
SECRETS="$D/.secrets"
{
    grep -iE '^[^#=]*(password|secret|token)[^=]*=' server.properties 2>/dev/null | cut -d= -f2- | tr -d '\r'
    for f in config/*.toml defaultconfigs/*.toml ${LEVEL:+"$LEVEL"/serverconfig/*.toml}; do
        [ -f "$f" ] && sed -nE 's/^[[:space:]]*[A-Za-z_]*([Tt]oken|[Pp]assword|[Ss]ecret|[Aa]pi[_]?[Kk]ey)[A-Za-z_]*[[:space:]]*=[[:space:]]*"([^"]+)".*/\2/p' "$f"
    done
} | awk 'length($0) >= 4' | sort -u > "$SECRETS"

# redact <in> <out>: copy a text file with secrets blanked and player addresses (/1.2.3.4:5678) masked
redact() {
    mkdir -p "$(dirname "$2")"
    awk -v sf="$SECRETS" '
        BEGIN { while ((getline s < sf) > 0) sec[++n] = s }
        {
            line = $0
            for (i = 1; i <= n; i++)
                while ((p = index(line, sec[i])) > 0)
                    line = substr(line, 1, p - 1) "<redacted>" substr(line, p + length(sec[i]))
            gsub(/\/[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+(:[0-9]+)?/, "/<ip>", line)
            print line
        }' "$1" > "$2"
}

# excerpt <in> <out>: the lines of a Forge log ([30Sep2026 12:34:56.789] ...) inside the window; lines without
# a timestamp (stack traces) go with the line above them
KEY_A=$(date -d "@$START" +%Y%m%d%H%M%S)
KEY_B=$(date -d "@$END" +%Y%m%d%H%M%S)
excerpt() {
    awk -v a="$KEY_A" -v b="$KEY_B" '
        BEGIN { split("Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec", m, " "); for (i = 1; i <= 12; i++) mo[m[i]] = sprintf("%02d", i) }
        /^\[[0-9][0-9][A-Z][a-z][a-z][0-9][0-9][0-9][0-9] [0-9][0-9]:[0-9][0-9]:[0-9][0-9]/ {
            k = substr($0, 7, 4) mo[substr($0, 4, 3)] substr($0, 2, 2) substr($0, 12, 2) substr($0, 15, 2) substr($0, 18, 2)
            keep = (k "" >= a "" && k "" <= b "")
        }
        keep' "$1" > "$2"
}

# recent <find args...>: files changed since LOOKBACK, plus the newest one either way
recent() {
    { find "$@" -newermt "@$LOOKBACK" 2>/dev/null; find "$@" -printf '%T@ %p\n' 2>/dev/null | sort -n | tail -1 | cut -d' ' -f2-; } | sort -u
}

# --- 1. the recording
if [ -n "$PID" ] && [ -n "$HAS_JFR" ] && [ -n "$JCMD" ]; then
    echo "Dumping the last $AGE of the recording..."
    $TO "$JCMD" "$PID" JFR.dump name=ce filename="$D/recording.jfr" maxage="$AGE" > "$D/.jcmd.out" 2>&1
    if [ ! -s "$D/recording.jfr" ]; then
        if grep -q 'No recordings' "$D/.jcmd.out"; then
            note "The recording has already ended: the JVM is shutting down (a stop or crash in progress). Its exit dump is taken from perf/exit/ when written; run the report again in a minute if previous-run/ is missing."
        else
            note "JFR.dump failed: $(grep -v '^[0-9]*:$' "$D/.jcmd.out" | head -3 | tr '\n' ' ')"
        fi
    fi
fi
if [ -n "$PID" ] && [ ! -s "$D/recording.jfr" ] && [ -n "$HAS_JFR" ]; then
    # no jcmd or the dump failed: the finished chunk files of this run are each a valid recording
    for r in perf/jfr-repo/*_"$PID"/; do
        [ -d "$r" ] && cp -r "$r" "$D/recording" && note "Copied this run's chunk files ($r) instead of a dump; the newest chunk may be cut off."
    done
fi
# the previous run: a stop or a watchdog crash (System.exit) writes perf/exit/<...>.jfr (dumponexit in
# LaunchServer.sh); a hard kill (kill -9, out of memory, JVM crash) leaves its perf/jfr-repo/ folder behind
PREV_EXIT=$(find perf/exit -maxdepth 1 -name '*.jfr' -newermt "@$LOOKBACK" -printf '%T@ %p\n' 2>/dev/null | sort -n | tail -1 | cut -d' ' -f2-)
LEFTOVER=$(for r in perf/jfr-repo/*/; do
    [ -d "$r" ] || continue
    case "$r" in *_"$PID"/) [ -n "$PID" ] && continue ;; esac
    echo "$(stat -c %Y "$r") $r"
done | sort -n | awk -v lb="$LOOKBACK" '$1 >= lb {print $2}')
NEWEST_LEFTOVER=$(echo "$LEFTOVER" | tail -1)
for f in $PREV_EXIT $NEWEST_LEFTOVER; do
    mkdir -p "$D/previous-run"
    cp -r "${f%/}" "$D/previous-run/"
    echo "Took the recording of a previous run: $f"
done
OLD=$(ls -d perf/jfr-repo/*/ 2>/dev/null | grep -vxF -e "$NEWEST_LEFTOVER" | grep -v "_${PID:-none}/\$")
[ -n "$OLD" ] && note "Other leftover recording folders, not included (delete them when no longer needed): $(echo $OLD)"

# --- 2. logs
for f in latest debug; do
    if [ -f "logs/$f.log" ]; then
        redact "logs/$f.log" "$D/logs/$f.log"
        excerpt "$D/logs/$f.log" "$D/logs/$f.window.log"
    else
        note "logs/$f.log is missing."
    fi
done
for f in $(recent logs -maxdepth 1 -name '*.log.gz'); do
    gzip -dc "$f" > "$D/.tmp.log" 2>/dev/null && redact "$D/.tmp.log" "$D/logs/$(basename "$f" .gz)"
done
rm -f "$D/.tmp.log"
for f in logs/kubejs/*.log logs/ce-*.log; do
    [ -f "$f" ] && redact "$f" "$D/$f"
done

# --- 3. crashes
for f in $(recent crash-reports -maxdepth 1 -name '*.txt'); do redact "$f" "$D/$f"; done
for f in $(recent . -maxdepth 1 -name 'hs_err_pid*.log'); do redact "$f" "$D/$(basename "$f")"; done

# --- 4. live JVM state (thread dumps a few seconds apart show what is stuck and what moves)
if [ -n "$PID" ] && [ -n "$JCMD" ]; then
    mkdir -p "$D/jvm"
    for n in 1 2 3; do
        $TO "$JCMD" "$PID" Thread.print > "$D/jvm/.t" 2>&1 && redact "$D/jvm/.t" "$D/jvm/threads-$n.txt"
        [ $n -lt 3 ] && sleep 3
    done
    for c in GC.heap_info VM.flags VM.uptime JFR.check; do
        $TO "$JCMD" "$PID" $c > "$D/jvm/$c.txt" 2>&1
    done
    rm -f "$D/jvm/.t"
fi

# --- 5. the machine
{
    echo "== report"; echo "created $(date '+%F %T %z'), window $AGE: $(date -d "@$START" '+%F %T') .. $(date -d "@$END" '+%F %T')"
    echo; echo "== os"; uname -srm; cat /etc/os-release 2>/dev/null | grep -E '^PRETTY_NAME='
    echo; echo "== uptime / load"; uptime
    echo; echo "== cpu"; nproc; grep -m1 'model name' /proc/cpuinfo
    echo; echo "== memory (MB)"; free -m
    echo; echo "== disk (server folder)"; df -h .
    for r in cpu memory io; do
        [ -r /proc/pressure/$r ] && { echo; echo "== pressure $r (share of time something waited for it)"; cat /proc/pressure/$r; }
    done
    echo; echo "== java"
    J=java; [ -n "$PID" ] && J=$(readlink -f /proc/$PID/exe)
    "$J" -version 2>&1
    if [ -n "$PID" ]; then
        echo; echo "== server process (etimes = seconds since start)"; ps -o pid=,etimes=,rss=,vsz=,pcpu=,nlwp=,stat= -p "$PID" | awk '{print "pid " $1 ", up " $2 " s, rss " int($3/1024) " MB, vsz " int($4/1024) " MB, cpu " $5 "%, threads " $6 ", state " $7}'
        echo; echo "== server threads by CPU (top -H)"; top -H -b -n1 -w 200 -p "$PID" 2>/dev/null | sed -n '7,47p'
    fi
    echo; echo "== busiest processes (names only)"; top -b -n1 -w 200 2>/dev/null | sed -n '7,22p' | awk '{print $1, $2, $9, $10, $12}'
    echo; echo "== kernel OOM kills (needs dmesg access)"
    dmesg 2>/dev/null | grep -iE 'out of memory|killed process' | tail -10 || true
} > "$D/system.txt" 2>&1
grep -qi 'killed process' "$D/system.txt" || sed -i '/^== kernel OOM kills/a (none visible)' "$D/system.txt"

# --- 6. server settings that matter for performance (secrets redacted), and what the server runs
[ -f server.properties ] && redact server.properties "$D/server.properties"
[ -f user_jvm_args.txt ] && cp user_jvm_args.txt "$D/"
# hashes only: tools/perf/ingest_report.py matches them against deploy/history/ to name the deployed version
# and list changed configs. serverconfig/ is the world's (<level-name>/serverconfig).
{
    find -L mods config defaultconfigs kubejs -type f ! -path 'kubejs/exported/*' ! -path 'kubejs/probe/*' -print0 2>/dev/null |
        sort -z | xargs -0 -r sha1sum
    [ -n "$LEVEL" ] && [ -d "$LEVEL/serverconfig" ] &&
        (cd "$LEVEL" && find serverconfig -type f -print0 | sort -z | xargs -0 -r sha1sum)
} > "$D/files.sha1"

# --- 7. REPORT.md
rm -f "$SECRETS"
WIN="$D/logs/latest.window.log"
{
    echo "# Server report $NAME"
    echo
    echo "- Created: $(date '+%F %T %z') on $(hostname)"
    echo "- Window: $(date -d "@$START" '+%F %T') to $(date -d "@$END" '+%F %T') ($AGE)"
    if [ -n "$PID" ]; then
        echo "- Server: running, up $(ps -o etimes= -p "$PID" | awk '{printf "%dh %02dm", $1/3600, $1%3600/60}')"
    else
        echo "- Server: not running"
    fi
    echo "- Recording: $( [ -s "$D/recording.jfr" ] && echo "recording.jfr ($(du -h "$D/recording.jfr" | cut -f1))" || { [ -d "$D/recording" ] && echo "recording/ (chunk files)"; } || echo none)"
    echo "- Previous run's recording: $(echo $PREV_EXIT $NEWEST_LEFTOVER | sed 's/^$/none/')"
    echo "- Mods: $(grep -c ' mods/' "$D/files.sha1") jars hashed"
    echo
    echo "## In the window"
    echo
    if [ -f "$WIN" ]; then
        echo "- \"Can't keep up\": $(grep -c "Can't keep up" "$WIN")"
        echo "- ERROR lines: $(grep -c '/ERROR\]' "$WIN"), WARN lines: $(grep -c '/WARN\]' "$WIN")"
        echo "- \"Error occurred while handling event\" (KubeJS): $(grep -c 'Error occurred while handling event' "$WIN")"
        grep "Can't keep up" "$WIN" | tail -5 | sed 's/^/    /'
    else
        echo "- no latest.log"
    fi
    echo
    echo "## Crash reports"
    echo
    ls "$D/crash-reports" 2>/dev/null | while read -r f; do
        echo "- $f: $(grep -m1 '^Description:' "$D/crash-reports/$f" | cut -d' ' -f2-)"
    done
    ls "$D"/hs_err_pid*.log > /dev/null 2>&1 && echo "- JVM crash files: $(cd "$D" && echo hs_err_pid*.log)"
    [ -d "$D/crash-reports" ] || ls "$D"/hs_err_pid*.log > /dev/null 2>&1 || echo "- none"
    echo
    echo "## Notes"
    echo
    if [ -s "$NOTES" ]; then cat "$NOTES"; else echo "- none"; fi
    echo
    echo "## Files"
    echo
    (cd "$D" && find . -type f ! -name '.*' ! -name REPORT.md -printf '%P\n' | sort | while read -r f; do echo "- $f ($(du -h "$f" | cut -f1))"; done)
} > "$D/REPORT.md"
rm -f "$NOTES" "$D/.jcmd.out"

echo
sed -n '/^## In the window/,/^## Files/p' "$D/REPORT.md" | grep -v '^## Files'

# --- 8. archive
cd perf/reports || exit 1
if have zip; then
    ARCH="$NAME.zip"; zip -qr "$ARCH" "$NAME"
else
    ARCH="$NAME.tar.gz"; tar czf "$ARCH" "$NAME"
fi
if [ -s "$ARCH" ]; then
    rm -rf "$NAME"
    echo "Saved $(du -h "$ARCH" | cut -f1) to $SERVER_DIR/perf/reports/$ARCH"
    echo "Copy it to the dev instance and run: python3 tools/perf/ingest_report.py $ARCH"
else
    echo "Packing failed; the files are in $D"
    exit 1
fi
