#!/usr/bin/env bash
# Builds a scratch dedicated server (srv/) and a throwaway client (cli/) under $1 for the
# item-loss repro. See README.md. Nothing here touches the instance except reading it.
set -euo pipefail
OUT=${1:?usage: setup.sh OUT_DIR}
INST=$(cd "$(dirname "$0")/../.." && pwd)
TRASH_SRV=~/.local/share/Trash/files/CreEngServer
HERE="$INST/tools/item_repro"
mkdir -p "$OUT/srv/mods" "$OUT/cli"

# ---- server: instance jars minus the client-only ones (deploy/latest/client_mods is the list)
cd "$OUT/srv"
ln -sfn "$TRASH_SRV/libraries" libraries
cp "$TRASH_SRV/run.sh" .
echo "-Xmx5G" > user_jvm_args.txt
echo "eula=true" > eula.txt
cat > server.properties <<EOF
online-mode=false
server-port=25599
level-name=imrepro_$(date +%s)
spawn-protection=0
view-distance=4
simulation-distance=4
difficulty=peaceful
max-tick-time=-1
enforce-secure-profile=false
EOF
rm -f mods/*.jar
for j in "$INST"/mods/*.jar; do
  b=$(basename "$j")
  [ -e "$INST/deploy/latest/client_mods/$b" ] && continue
  ln -s "$j" "mods/$b"
done
rm -rf kubejs config defaultconfigs
cp -r "$INST/kubejs" kubejs
cp -r "$INST/config" config
cp -r "$INST/defaultconfigs" defaultconfigs
cp "$HERE/server/imrepro_server.js" kubejs/server_scripts/zz_imrepro_server.js

# ---- client: full copy of mods/kubejs/config (client mods write into these)
cd "$OUT/cli"
rm -rf mods kubejs config
cp -r "$INST/mods" mods
cp -r "$INST/kubejs" kubejs
cp -r "$INST/config" config
cp "$HERE/client/imrepro_client.js" kubejs/client_scripts/zz_imrepro_client.js
grep -v -E '^(resourcePacks|incompatibleResourcePacks|pauseOnLostFocus|renderDistance|simulationDistance|maxFps|soundCategory_master|tutorialStep|onboardAccessibility|skipMultiplayerWarning|joinedFirstServer):' \
  "$INST/options.txt" > options.txt || true
cat >> options.txt <<EOF
pauseOnLostFocus:false
renderDistance:4
simulationDistance:5
maxFps:30
soundCategory_master:0.0
tutorialStep:none
onboardAccessibility:false
skipMultiplayerWarning:true
joinedFirstServer:true
EOF
echo '{"seed": 1, "phase": "p0", "stop": false}' > kubejs/imrepro.json
echo "built $OUT/srv and $OUT/cli"
