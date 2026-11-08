#!/usr/bin/env bash
# Scratch server for the SQ-CCO rows: Creative Common plus ONLY the optional mods it tweaks, none of the other Creative
# jars (Compat, Calendar, Engineering, Equipment, Magic, Delight, Brewing, Devtools, Worldgen, Labels).
#   cc_only_srv.sh OUT LEVEL full|alone|without=<mod key>[,<mod key>...] [prop=value ...]
#     full      Common + every optional mod in MODS below (KubeJS stack always present: Common requires it)
#     alone     Common + the KubeJS stack only (no optional mod)
#     without=  full minus the named keys (iw, trims, we, quark, gravity, nea, beacons, portals, heartstone, seasons)
# Builds on prep_srv.sh (group B normal world, hard links, newest Common build swapped in, probe scripts), then prunes
# srv/mods to the allowlist, empties kubejs/ (keeps the zq probe) and removes config/ and defaultconfigs/ so Common
# writes its own defaults (SQ-CCO-04). Pack config comparisons copy configs_edited/root_config/creativecommon-common.toml.
# Refuses while a game JVM is up (prep_srv.sh does the check).
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
OUT=${1:?OUT}; LEVEL=${2:?LEVEL}; MODE=${3:?full|alone|without=...}; shift 3
bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT" "$LEVEL" B "$@"
SRV="$OUT/srv"; cd "$SRV" || exit 1

# always kept: Forge-side libraries of KubeJS (Common's hard dependency), Common itself, and the dev-only perfBot (bots)
BASE=("creativecommon-*" "kubejs-forge-*" "rhino-forge-*" "architectury-*" "cdevperfbot-*")
# key|jar glob|extra libraries that mod needs
declare -A MODS=(
  [iw]="immersive_weathering-*"        [trims]="armortrims-*"            [we]="worldedit-mod-*"
  [quark]="Quark-3.4-*"                [gravity]="create_gravity-*"      [nea]="nock_enough_arrows-*"
  [beacons]="CerbonsBetterBeacons-*"   [portals]="immersive-portals-*"   [heartstone]="heartstone-*"
  [seasons]="SereneSeasons-*")
declare -A LIBS=(
  [iw]="moonlight-*"  [heartstone]="moonlight-*"  [quark]="AutoRegLib-*"
  # Immersive Portals' core is nested in its jar; Create Gravity needs Create (and its libraries)
  [gravity]="create-1.19.2-*")
KEEP=("${BASE[@]}")
case "$MODE" in
  alone) ;;
  full) for k in "${!MODS[@]}"; do KEEP+=("${MODS[$k]}"); [ -n "${LIBS[$k]:-}" ] && KEEP+=("${LIBS[$k]}"); done ;;
  without=*) drop=",${MODE#without=},"
    for k in "${!MODS[@]}"; do
      case "$drop" in *",$k,"*) continue ;; esac
      KEEP+=("${MODS[$k]}"); [ -n "${LIBS[$k]:-}" ] && KEEP+=("${LIBS[$k]}")
    done ;;
  *) echo "mode: full | alone | without=<keys>" >&2; exit 1 ;;
esac
shopt -s nullglob
for j in mods/*.jar; do
  b=$(basename "$j"); keep=0
  for g in "${KEEP[@]}"; do case "$b" in $g) keep=1 ;; esac; done
  [ $keep = 1 ] || rm -f "$j"
done
# a Create Gravity-only set still needs Create's own library jars (flywheel is nested in 0.5.1; registrate too)
rm -rf config defaultconfigs
find kubejs -mindepth 1 -maxdepth 1 ! -name server_scripts ! -name data -exec rm -rf {} + 2>/dev/null || true
find kubejs/server_scripts -type f ! -name 'zz_zq_probe.js' -delete 2>/dev/null || true
# the pack's kubejs/data (Terralith noise overrides, other Creative jars' data) goes too; only the zq scratch data stays
find kubejs/data -mindepth 1 -maxdepth 1 ! -name zq -exec rm -rf {} + 2>/dev/null || true
echo "cc_only done ($MODE): $(ls mods | wc -l) jars"; ls mods
if ls mods | grep -qiE 'creative(compat|calendar|engineering|equipment|magic|delight|brewing|devtools|worldgen|labels)'; then
  echo "ERROR: another Creative jar is still in mods/" >&2; exit 1
fi
