#!/usr/bin/env bash
# Dumps every loot_tables/*.json from all mod jars + the vanilla jar into one
# browsable folder, organized by mod id. Covers chests, entities (mob drops),
# gameplay (fishing), blocks, and any other loot_tables subfolder a mod adds.
#
# Usage:
#   ./dump_loot_tables.sh /path/to/instance/mods [/path/to/vanilla-or-forge.jar]
#
# Example (typical ATLauncher layout):
#   ./dump_loot_tables.sh ~/ATLauncher/instances/CreativeEngineering/mods

set -euo pipefail

MODS_DIR="${1:?Usage: $0 <mods_dir> [vanilla_or_forge_jar]}"
VANILLA_JAR="${2:-}"
OUTPUT_DIR="./loot_table_dump"

mkdir -p "$OUTPUT_DIR"
rm -rf "${OUTPUT_DIR:?}"/*

extract_loot_tables() {
    local jar="$1"
    local mod_name
    mod_name="$(basename "$jar" .jar)"

    # List only loot_tables JSON entries inside the jar
    local entries
    entries="$(unzip -Z1 "$jar" 2>/dev/null | grep -E '^data/[^/]+/loot_tables/.*\.json$' || true)"

    if [ -z "$entries" ]; then
        return
    fi

    echo "Extracting from: $mod_name"
    local dest="$OUTPUT_DIR/$mod_name"
    mkdir -p "$dest"

    while IFS= read -r entry; do
        [ -z "$entry" ] && continue
        mkdir -p "$dest/$(dirname "$entry")"
        unzip -p "$jar" "$entry" > "$dest/$entry" 2>/dev/null || true
    done <<< "$entries"
}

echo "Scanning mods in: $MODS_DIR"
if [ -d "$MODS_DIR" ]; then
    shopt -s nullglob
    for jar in "$MODS_DIR"/*.jar; do
        extract_loot_tables "$jar"
    done
    shopt -u nullglob
else
    echo "WARNING: mods dir not found at $MODS_DIR" >&2
fi

if [ -n "$VANILLA_JAR" ] && [ -f "$VANILLA_JAR" ]; then
    echo "Extracting vanilla loot tables from: $VANILLA_JAR"
    extract_loot_tables "$VANILLA_JAR"
else
    echo "NOTE: no vanilla/forge jar given (2nd arg) — vanilla's own loot tables were skipped."
    echo "      Pass e.g. ~/.gradle/caches/.../forge-1.19.2-*.jar or your client jar to include them."
fi

echo ""
echo "Done. Loot tables saved under: $OUTPUT_DIR"
echo ""
echo "Category breakdown:"
find "$OUTPUT_DIR" -path "*loot_tables/chests/*"   -name "*.json" | wc -l | xargs echo "  Chest tables:"
find "$OUTPUT_DIR" -path "*loot_tables/entities/*" -name "*.json" | wc -l | xargs echo "  Entity/mob drop tables:"
find "$OUTPUT_DIR" -path "*loot_tables/gameplay/*" -name "*.json" | wc -l | xargs echo "  Gameplay tables (fishing, etc.):"
find "$OUTPUT_DIR" -path "*loot_tables/blocks/*"   -name "*.json" | wc -l | xargs echo "  Block drop tables:"
find "$OUTPUT_DIR" -name "*.json" | wc -l | xargs echo "  Total loot table files:"
