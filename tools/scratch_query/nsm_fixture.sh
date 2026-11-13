#!/usr/bin/env bash
# Builds the NSM old-save fixture server (scratch_queue.md "SQ-NSM", namespace_move.md 5; also SQ-HCS-09).
#   nsm_fixture.sh OUT [COMMIT=40ed32be]
# 1. NOSWAP=1 prep_srv.sh OUT nsmfix A spawn-animals=true (installed mods/ jars, no dev swaps)
# 2. the Creative jars replaced by COPIES of deploy/latest/mods (1.2.02: old namespaces, no swap lists)
# 3. config/, defaultconfigs/, kubejs/ replaced by the trees of COMMIT (the 1.2.02 build commit), so the old
#    config names (creative_labels-*.toml, exposure_extras-common.toml) and the old scripts are what the jars read;
#    the scratch probes prep_srv.sh adds are put back, plus nsm_fixture.js as /nsmfix
# 4. SQ-NSM-09 / -10 config edits: noteLength 20, textDistance 10, leatherSlots 6
# Then: scratch_server.sh boot OUT, wait_boot.sh, /nsmfix steps (see nsm_fixture.js), stop, keep OUT/srv/nsmfix as
# the untouched fixture. Rows boot copies on the new jars (oldsave_swap.sh plus the Labels and Exposure jars).
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
OUT=${1:?OUT}; COMMIT=${2:-40ed32be}
NOSWAP=1 bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT" nsmfix A spawn-animals=true
SRV="$OUT/srv"; cd "$SRV"
for j in "$INST"/deploy/latest/mods/*.jar; do
  b=$(basename "$j")
  # drop the srv copy of the same mod (any version), then copy the 1.2.02 jar in
  stem=$(echo "$b" | sed -E 's/-[0-9].*$//')
  rm -f mods/"$stem"-[0-9]*.jar
  cp "$j" mods/ && echo "1.2.02 jar: $b"
done
# not in the 1.2.02 server set (deploy/latest/verify/server.installed.sha1): BOP needs the TerraBlender stub that
# only the new Worldgen nests; Devtools is dev-only and built against the new Common
rm -f mods/BiomesOPlenty-*.jar mods/creativedevtools-*.jar
keep=$(mktemp -d)
cp kubejs/server_scripts/zz_*.js "$keep"/ 2>/dev/null || true
cp -r kubejs/data/zq "$keep"/ 2>/dev/null || true
rm -rf config defaultconfigs kubejs
git -C "$INST" archive "$COMMIT" config defaultconfigs kubejs | tar -x
cp "$keep"/zz_*.js kubejs/server_scripts/ 2>/dev/null || true
mkdir -p kubejs/data && cp -r "$keep"/zq kubejs/data/ 2>/dev/null || true
rm -rf "$keep"
cp "$INST/tools/scratch_query/nsm_fixture.js" kubejs/server_scripts/zz_nsm_fixture.js
sed -i -E 's/^(\s*noteLength\s*=\s*).*/\120/' config/creative_labels-common.toml && grep -E '^\s*noteLength' config/creative_labels-common.toml
sed -i -E 's/^(\s*textDistance\s*=\s*).*/\110.0/' config/creative_labels-client.toml && grep -E '^\s*textDistance' config/creative_labels-client.toml
sed -i -E 's/^(\s*leatherSlots\s*=\s*).*/\16/' config/exposure_extras-common.toml && grep -E '^\s*leatherSlots' config/exposure_extras-common.toml
echo "jars in srv/mods not in mod_sides.md:"
for j in mods/*.jar; do grep -qF "$(basename "$j")" "$INST/mod_sides.md" || echo "  $j"; done
