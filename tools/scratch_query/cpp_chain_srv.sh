#!/usr/bin/env bash
# Reduced scratch servers for the SQ-CPP rows, built from the pack's own jars (CEPortTesting is left alone).
#   cpp_chain_srv.sh OUT LEVEL chain|compat [prop=value ...]
#     chain   SQ-CPP-04: Creative Brewing chain WITHOUT Creative Engineering: KubeJS stack, KubeJS Create / Delight,
#             LootJS, MoreJS, Create, Serene Seasons, Immersive Engineering, Fruits Delight (+ Farmer's Delight and the
#             libraries they need), Common, Compat, Calendar, Delight, Brewing
#     compat  SQ-CPP-05: Common + Compat (+ Calendar, Serene Seasons) without Create Recycle or Create
# Builds on prep_srv.sh (group B normal world, hard links, newest dev builds swapped in, zq probe), then prunes srv/mods
# to the allowlist, empties kubejs/ apart from the probes and the zq data, and removes config/ and defaultconfigs/ so
# every jar writes its defaults. EXTRA="glob glob" keeps more jars (a missing dependency the boot names).
# Refuses while a game JVM is up (prep_srv.sh does the check).
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
OUT=${1:?OUT}; LEVEL=${2:?LEVEL}; MODE=${3:?chain|compat}; shift 3
bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT" "$LEVEL" B "$@"
SRV="$OUT/srv"; cd "$SRV" || exit 1

BASE=("rhino-forge-*" "architectury-*" "kubejs-forge-*" "cdevperfbot-*" "creativecommon-*" "creativecompat-*"
  "creativecalendar-*" "SereneSeasons-*")
case "$MODE" in
  chain) KEEP=("${BASE[@]}" "kubejs-create-forge-*" "kubejsdelight-*" "lootjs-forge-*" "morejs-forge-*"
    "create-1.19.2-*" "ImmersiveEngineering-*" "fruitsdelight-*" "FarmersDelight-*" "creativedelight-*"
    "creativebrewing-*" "l2library-*") ;;
  compat) KEEP=("${BASE[@]}") ;;
  *) echo "mode: chain | compat" >&2; exit 1 ;;
esac
for g in ${EXTRA:-}; do KEEP+=("$g"); done
shopt -s nullglob
for j in mods/*.jar; do
  b=$(basename "$j"); keep=0
  for g in "${KEEP[@]}"; do case "$b" in $g) keep=1 ;; esac; done
  [ $keep = 1 ] || rm -f "$j"
done
rm -rf config defaultconfigs
find kubejs -mindepth 1 -maxdepth 1 ! -name server_scripts ! -name data -exec rm -rf {} + 2>/dev/null || true
find kubejs/server_scripts -type f ! -name 'zz_*_probe.js' -delete 2>/dev/null || true
find kubejs/data -mindepth 1 -maxdepth 1 ! -name zq -exec rm -rf {} + 2>/dev/null || true
echo "cpp_chain done ($MODE): $(ls mods | wc -l) jars"; ls mods
if ls mods | grep -qiE 'creative(engineering|equipment|magic|devtools|worldgen|labels)|create_crush_everything'; then
  echo "ERROR: a jar this set must not hold is in mods/" >&2; exit 1
fi
