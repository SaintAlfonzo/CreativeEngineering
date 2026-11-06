#!/usr/bin/env bash
# Perf session collector (started by perf_session.sh start; perf.md 14). Every 5 s:
# - a scratch server JVM it has not seen (cwd holds server.properties, not the instance itself = the user's game)
#   gets boots/NN_<level>/ with meta.env (pid, cwd, start, level, type, JVM args, jar list hash);
# - each watched JVM gets a proc.tsv row: time, CPU seconds used, RSS MB, threads; logs/latest.log is copied
#   every 30 s;
# - a JVM that is gone gets its exit recording (perf/exit/hotspot-pid-PID-*.jfr) MOVED into the boot folder,
#   plus latest.log, the console tail and crash reports, and an `end` timeline line.
# It never runs a parse while a server is up (that would load the CPU it measures); perf_session.sh process does.
# Exits when stop.req exists and no watched JVM is left.
set -u
D=${1:?session dir}
INST="$(cd "$(dirname "$0")/../../.." && pwd)"
HZ=$(getconf CLK_TCK)
declare -A BOOT CWD
n=$(ls "$D/boots" | wc -l)
tl() { printf '%s\t%s\t%s\t%s\n' "$(date +%s.%N | cut -c1-14)" "$1" "$2" "$3" >> "$D/timeline.tsv"; }
prop() { grep -m1 "^$2=" "$1/server.properties" 2>/dev/null | cut -d= -f2; }

finish() {  # pid
  local p=$1 b=${BOOT[$1]} c=${CWD[$1]}
  sleep 2   # the exit dump is written before the pid goes, but give the file system a moment
  local f
  for f in "$c"/perf/exit/hotspot-pid-"$p"-*.jfr; do [ -f "$f" ] && mv "$f" "$b/" ; done
  # killed: the repository folder of this pid is left behind; its closed chunks still parse
  for f in "$c"/perf/jfr-repo/*_"$p"; do [ -d "$f" ] && ! ls "$b"/*.jfr >/dev/null 2>&1 && cp -r "$f" "$b/repo"; done
  # a reboot of the same srv may already own latest.log; then the 30 s copy is the last one of this boot
  local q newer=0
  for q in "${!CWD[@]}"; do [ "$q" != "$p" ] && [ "${CWD[$q]}" = "$c" ] && newer=1; done
  [ $newer = 0 ] && [ -f "$c/logs/latest.log" ] && cp "$c/logs/latest.log" "$b/latest.log"
  [ $newer = 0 ] && [ -f "$c/console.log" ] && tail -200 "$c/console.log" > "$b/console_tail.log"
  find "$c/crash-reports" -newer "$b/meta.env" -type f -exec cp {} "$b/" \; 2>/dev/null
  echo "END=$(date +%s)" >> "$b/meta.env"
  tl end "$c" "$(basename "$b") $(ls "$b"/*.jfr "$b"/repo 2>/dev/null | wc -l) recording file(s)"
  unset "BOOT[$p]" "CWD[$p]"
}

tick=0
while :; do
  live=" "
  while read -r p c; do
    [ -n "$p" ] || continue
    c=${c% (deleted)}
    [ "$c" = "$INST" ] && continue
    [ -f "$c/server.properties" ] || continue
    # tools/perf/suite runs move their own recordings (perf_suite.md)
    [ -f "$c/perf_suite.owner" ] && continue
    live+="$p "
    if [ -z "${BOOT[$p]:-}" ]; then
      n=$((n + 1))
      lvl=$(prop "$c" level-name)
      b="$D/boots/$(printf %02d $n)_${lvl:-world}"
      mkdir -p "$b"
      st=$(awk '{print $22}' /proc/$p/stat)
      bt=$(awk '/^btime/{print $2}' /proc/stat)
      {
        echo "PID=$p"; echo "CWD=$c"; echo "LEVEL=$lvl"
        echo "TYPE=$(prop "$c" level-type | tr -d '\\')"
        echo "SEED=$(prop "$c" level-seed)"
        echo "START=$((bt + st / HZ))"
        echo "JARS=$(ls -l --time-style=+%s "$c/mods/" 2>/dev/null | awk '{print $5, $6, $7}' | sha1sum | cut -c1-12)"
        echo "JVMARGS='$(tr '\n' ' ' < "$c/user_jvm_args.txt" 2>/dev/null)'"
      } > "$b/meta.env"
      ls -l --time-style=+%m-%d_%H:%M "$c/mods/" 2>/dev/null | grep -E 'creativeengineering|creativedelight|modernfix' > "$b/key_jars.txt"
      printf 'time\tcpu_s\trss_mb\tthreads\n' > "$b/proc.tsv"
      BOOT[$p]=$b; CWD[$p]=$c
      tl seen "$c" "$(basename "$b") pid $p"
    fi
    b=${BOOT[$p]}
    if [ -r /proc/$p/stat ]; then
      cpu=$(awk -v hz="$HZ" '{printf "%.2f", ($14 + $15) / hz}' /proc/$p/stat)
      rss=$(awk '/^VmRSS/{print int($2/1024)}' /proc/$p/status)
      th=$(awk '/^Threads/{print $2}' /proc/$p/status)
      printf '%s\t%s\t%s\t%s\n' "$(date +%s)" "$cpu" "$rss" "$th" >> "$b/proc.tsv"
    fi
    [ $((tick % 6)) = 0 ] && [ -f "$c/logs/latest.log" ] && cp "$c/logs/latest.log" "$b/latest.log"
  done < <(bash "$INST/tools/scratch_query/game_jvms.sh" 2>/dev/null)
  for p in "${!BOOT[@]}"; do
    case "$live" in *" $p "*) ;; *) finish "$p" ;; esac
  done
  if [ -f "$D/stop.req" ] && [ -z "${!BOOT[*]}" ]; then tl collector "-" "exit"; exit 0; fi
  tick=$((tick + 1))
  sleep 5
done
