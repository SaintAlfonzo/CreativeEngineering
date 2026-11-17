# iw-soil-wild-plants

> Wild plants/bushes/trees on Immersive Weathering soils - tag worldgen/grass_like_soils + tools/gen_soil_compat.py (65 kubejs/data overrides); scratch-server tested 2026-10-05

Moved from agent memory 2026-11-16 (last edited there 2026-10-05). Repo status docs win over any status line here.

2026-10-05: IW lays humus (Dark Forest), fluvisol/silt (swamp + #c:swamp), vertisol (badlands/desert, over red sand) at
raw_generation, before vegetation. Features testing `matching_blocks grass_block` below skipped them. Cryosol is
top_layer_modification over snow only, so it never meets plants during worldgen.

Fix: block tag `creativeengineering:worldgen/grass_like_soils` (blocktags.js, permafrost excluded). `tools/gen_soil_compat.py`
wraps every below-grass test in any_of(orig, tag) and writes `kubejs/data/<ns>/worldgen/...` overrides. It also rebuilds 5
Java-registered patches (Neapolitan strawberry/adzuki, FD blueberry/cranberry/lemon) via JAVA_BASES. A manifest
(soil_compat_manifest.json) tracks what it owns. /cdevgen id `soil_compat`. Hamimelon pack copy edited by hand. Mint pond and
banana are Java-internal and have no IW-soil biome, so they were left alone. Write-up: crops.md 2.8.1, CR-67/68.

Verified: scratch server boots; `/place feature` on soil pads gave OK on 6 soils and fail on stone/permafrost. Shrinking the
tag to grass + /reload reproduced the old failures, which is a cheap before/after trick for tag-gated worldgen.
Configured-feature datapack overrides of Java-registered features DO take effect (1.19.2 round-trips builtins through JSON).

**How to apply:** rerun the generator after updating Terralith/Vinery/Neapolitan/Fruits Delight/Geophilic. See
[vegetation-worldgen-system](vegetation-worldgen-system.md), memory `scratch-server-verify`.
