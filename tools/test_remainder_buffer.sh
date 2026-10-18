#!/usr/bin/env bash
# GameTests for the remainder buffer (feature_writeup/remainder_buffer.md section 6): pot, kettle and oven in a dev
# GameTest server with Farmer's Delight 1.2.4 and Farmer's Respite 2.0. Fetches the two jars from the URLs in
# pack/manifest.json the first time. Needs Java 17 (JAVA_HOME or the root gradle.properties' org.gradle.java.home).
# The first run sets up ForgeGradle (several minutes); a run takes about 1-3 minutes after that.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
H="$ROOT/tools/remainder_buffer_test"
mkdir -p "$H/libs"
python3 - "$ROOT/pack/manifest.json" "$H/libs" <<'PY'
import json, os, subprocess, sys, urllib.parse
manifest, libs = sys.argv[1], sys.argv[2]
want = {'farmersdelight': 'farmersdelight-1.2.4.jar', 'farmersrespite': 'farmersrespite-2.0.jar'}
for mod in json.load(open(manifest))['mods']:
    name = want.get(mod.get('modId'))
    if not name or os.path.exists(os.path.join(libs, name)):
        continue
    src = mod['source']
    if src.get('modrinth', {}).get('download'):
        url = src['modrinth']['download']
    else:
        f = src['curseforge']['fileId']
        url = f"https://edge.forgecdn.net/files/{f // 1000}/{f % 1000}/{urllib.parse.quote(mod['file'])}"
    subprocess.run(['curl', '-sSfL', '-o', os.path.join(libs, name), url], check=True)
PY
JAVA_ARG=()
if [ -n "${JAVA_HOME:-}" ]; then JAVA_ARG=("-Dorg.gradle.java.home=$JAVA_HOME"); fi
LOG="$H/run/gametest.log"
mkdir -p "$H/run"
"$ROOT/gradlew" -p "$H" "${JAVA_ARG[@]}" runGameTestServer --console=plain > "$LOG" 2>&1 || true
grep -E "failed!|tests passed|required tests failed|BUILD FAILED|error:" "$LOG" | sed 's/^\[[^]]*\] \[[^]]*\] \[[^]]*\]: //'
grep -q "All [0-9]* required tests passed" "$LOG"
