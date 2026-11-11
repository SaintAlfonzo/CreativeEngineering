#!/usr/bin/env bash
# Deploys the old-id fixture builder into a scratch server (plan_cd_namespace.md 5.1, SQ-CDNS rows).
#   cdns_fixture.sh OUT SWAP_SNAPSHOT.json
# OUT was prepared with NOSWAP=1 prep_srv.sh, so it holds the old jars. SWAP_SNAPSHOT.json is a copy of cd_namespace.json.
# Writes OUT/srv/kubejs/server_scripts/zz_cdns_{fixture,ids}.js and the scratch loot table cdnsfix:moved; /reload loads them.
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
OUT=${1:?OUT}; SNAP=${2:?swap snapshot json}
SRV="$OUT/srv"
cp "$INST/tools/scratch_query/cdns_fixture.js" "$SRV/kubejs/server_scripts/zz_cdns_fixture.js"
python3 -I - "$SNAP" "$SRV/kubejs/server_scripts/zz_cdns_ids.js" "$SRV/kubejs/data/cdnsfix/loot_tables" <<'PY'
import json, os, sys
snap, out, loot = sys.argv[1:4]
old = sorted({o for v in json.load(open(snap))["replaces"].values() for o in v})
old += ["creativeengineering:wooden_bucket", "creativeengineering:wooden_water_bucket"]
with open(out, "w") as f:
    f.write("global.cdnsIds = " + json.dumps(sorted(set(old))) + "\n")
kinds = ["canned_corn", "culture_beef", "cow_sample_syringe", "oven", "sourdough_block", "cornmeal", "wooden_water_bucket"]
os.makedirs(loot, exist_ok=True)
pools = [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "creativeengineering:" + k}]} for k in kinds]
json.dump({"type": "minecraft:chest", "pools": pools}, open(os.path.join(loot, "moved.json"), "w"))
print(len(set(old)), "old ids")
PY
