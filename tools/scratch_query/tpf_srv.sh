#!/usr/bin/env bash
# Builds a scratch server for the third-party fix rows (queue SQ-TPF, feature_writeup/thirdparty_fixes.md 7), and is
# the one place later runners get the Packet Fixer / Debugify configs into a scratch dir (neither jar's config is right
# in a plain prep_srv.sh dir: the instance has no debugify.json, so Debugify would run every fix).
#   tpf_srv.sh OUT LEVEL A|B PF [DEBUGIFY_ARGS ...] [-- prop=value ...]
#     PF: gen      delete srv/config/packetfixer.properties (Packet Fixer writes its upstream defaults on boot)
#         shipped  keep the instance's config/packetfixer.properties (what the pack ships, both sides)
#         vanilla|client|server   tools/scratch_query/tpf_pf/<PF>.properties (SQ-TPF-30 variants)
#         none     remove the Packet Fixer jar from srv/mods (and its config)
#     DEBUGIFY_ARGS: passed to tpf_debugify_cfg.py (nothing = every fix off; all; @s1; MC-xxxx ...)
#     NODEBUGIFY=1 in the environment: remove the Debugify jar instead
# Then: probe tools/scratch_query/tpf_probe.js as kubejs/server_scripts/zz_tpf_probe.js, and the list of srv jars
# without a mod_sides.md row (Debugify is expected there).
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
OUT=${1:?OUT}; LEVEL=${2:?LEVEL}; GROUP=${3:?A|B}; PF=${4:?PF}; shift 4
DBG=(); PROPS=()
while [ $# -gt 0 ]; do
  if [ "$1" = "--" ]; then shift; PROPS=("$@"); break; fi
  DBG+=("$1"); shift
done
bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT" "$LEVEL" "$GROUP" "${PROPS[@]+"${PROPS[@]}"}"
SRV="$OUT/srv"; cd "$SRV" || exit 1
for j in packetfixer-3.3.2-1.18-1.20.4-merged.jar debugify-1.1.0-forge-1.19.2.jar; do
  [ -f "mods/$j" ] || cp "$INST/jar_archive/thirdparty_fixes/$j" mods/
done
case "$PF" in
  gen) rm -f config/packetfixer.properties ;;
  shipped) cp "$INST/config/packetfixer.properties" config/packetfixer.properties ;;
  vanilla|client|server) cp "$INST/tools/scratch_query/tpf_pf/$PF.properties" config/packetfixer.properties ;;
  none) rm -f mods/packetfixer-*.jar config/packetfixer.properties ;;
  *) echo "PF must be gen|shipped|vanilla|client|server|none" >&2; exit 1 ;;
esac
if [ "${NODEBUGIFY:-0}" = 1 ]; then
  rm -f mods/debugify-*.jar config/debugify.json; echo "tpf: debugify jar removed"
else
  python3 "$INST/tools/scratch_query/tpf_debugify_cfg.py" config/debugify.json "${DBG[@]+"${DBG[@]}"}"
fi
cp "$INST/tools/scratch_query/tpf_probe.js" kubejs/server_scripts/zz_tpf_probe.js
echo "tpf: packetfixer=$PF ($(ls mods/packetfixer-*.jar 2>/dev/null | wc -l) jar), debugify=${DBG[*]:-off}"
echo -n "tpf: srv jars without a mod_sides.md row: "
for j in mods/*.jar; do grep -qF "$(basename "$j")" "$INST/mod_sides.md" || echo -n "$(basename "$j") "; done; echo
