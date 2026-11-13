#!/usr/bin/env bash
# Boots a copy of the NSM old-save fixture on the new jars (SQ-NSM-02, -03, -05, -07, -09, -10, SQ-HCS-09).
#   nsm_newjars.sh OUT FIXTURE_WORLD OLD_CONFIG_DIR
# FIXTURE_WORLD: the world folder nsm_fixture.sh built (kept untouched; this copies it).
# OLD_CONFIG_DIR: the fixture srv's config/ (holds creative_labels-*.toml and exposure_extras-common.toml with the
# SQ-NSM-09/-10 edits). Those old files are copied in and the new-named ones removed, so the carry-over runs.
# Then: scratch_server.sh boot OUT; wait_boot.sh OUT.
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
OUT=${1:?OUT}; FIX=${2:?fixture world}; OLDCFG=${3:?old config dir}
bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT" nsmfix A spawn-animals=true
SRV="$OUT/srv"; cd "$SRV"
rm -rf nsmfix && cp -a "$FIX" nsmfix
rm -f config/creativelabels-common.toml config/creativelabels-client.toml config/exposureextras-common.toml
for f in creative_labels-common.toml creative_labels-client.toml exposure_extras-common.toml; do cp "$OLDCFG/$f" config/; done
grep -hE '^\s*(noteLength|textDistance|leatherSlots)' config/creative_labels-*.toml config/exposure_extras-common.toml
ls mods | grep -iE '^(creative|exposureB)' | while read -r j; do echo "  $j $(date -r "mods/$j" +%m-%d_%H:%M)"; done
echo "jars in srv/mods not in mod_sides.md:"
for j in mods/*.jar; do grep -qF "$(basename "$j")" "$INST/mod_sides.md" || echo "  $j"; done
