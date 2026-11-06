#!/usr/bin/env bash
# Boot-time bench: Creative Delight jar against the three upstream member jars (ocean_delight / creative_delight.md).
#   bench_boot.sh OUT ROUNDS
# OUT is a scratch dir already set up by tools/creative_labels_test/scratch_server.sh (server stopped). Each round
# boots once without CD (upstream Ocean's, Oceanic, End's jars, pack's kubejs/data/creativedelight moved aside)
# and once with development/creativeDelight/build/libs/creativedelight-<v>.jar, both with the built patcherMod.
# Prints one line per boot: variant, seconds to "Done", KubeJS startup / server script times, recipe event time.
# Nothing here writes into the instance. Scratch servers only; one JVM at a time.
set -uo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
SS="$INST/tools/creative_labels_test/scratch_server.sh"
OUT=${1:?OUT dir}
ROUNDS=${2:-2}
SRV="$OUT/srv"
CDJAR=$(ls "$INST"/development/creativeDelight/build/libs/creativedelight-*.jar | grep -v -- '-cdtest\|-own' | head -1)
PMJAR="$INST/development/creativeCommon/build/libs/creativecommon-1.1.02.jar"

mods() {
  cd "$SRV/mods" || exit 1
  rm -f creativeengineering-*.jar creativedelight-*.jar oceansdelight-* oceanic_delight-* ends_delight-*
  ln -s "$PMJAR" .
  if [ "$1" = cd ]; then
    cp "$CDJAR" .
    [ -d "$OUT/cd_data_off" ] && mv "$OUT/cd_data_off" "$SRV/kubejs/data/creativedelight"
  else
    for j in "$INST"/mods/oceansdelight-* "$INST"/mods/oceanic_delight-* "$INST"/mods/ends_delight-*; do ln -s "$j" .; done
    [ -d "$SRV/kubejs/data/creativedelight" ] && mv "$SRV/kubejs/data/creativedelight" "$OUT/cd_data_off"
  fi
  cd - >/dev/null
}

boot() {
  bash "$SS" boot "$OUT" >/dev/null
  for _ in $(seq 300); do
    [ -f "$SRV/logs/latest.log" ] && grep -q "Done (\|Crash report saved" "$SRV/logs/latest.log" && break
    sleep 2
  done
  python3 -I - "$SRV" "$1" <<'EOF'
import re, sys, datetime
srv, variant = sys.argv[1], sys.argv[2]
lines = open(srv + '/logs/latest.log', errors='replace').read().splitlines()
def ts(l):
    m = re.match(r'\[\d+\w+\d{4} (\d+:\d+:\d+\.\d+)\]', l)
    return datetime.datetime.strptime(m.group(1), '%H:%M:%S.%f') if m else None
t0 = next(ts(l) for l in lines if ts(l))
def find(pat):
    for l in lines:
        if re.search(pat, l) and ts(l): return l
    return ''
done = find(r'Done \(')
st = re.search(r'startup scripts in ([\d.]+) s', find(r'KubeJS startup scripts in'))
sv = re.search(r'server scripts in ([\d.]+) s', find(r'KubeJS server scripts in'))
rc = re.search(r'in ([\d.]+) ms', find(r'Added \d+ recipes'))
print('%-8s done %6.1f s  startup %s s  server %s s  recipes %s ms' % (variant,
    (ts(done) - t0).total_seconds() if done else -1, st and st.group(1), sv and sv.group(1), rc and rc.group(1)))
EOF
  bash "$SS" stop "$OUT" >/dev/null
}

for r in $(seq "$ROUNDS"); do
  mods base; boot base
  mods cd; boot cd
done
