# structure-ground-match

> Creative Common worldgen/GroundMatch swaps a structure template's grass/dirt base for sampled local ground where grass isn't native (volcanic peaks ruins); CEConfig [worldgen.ground_match]; status in feature_writeup/structure_tuning.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

2026-09-27: user asked to drop grass/dirt-based structures from terralith:volcanic_peaks. A template scan found Philip's
ancient_ruins + ancient_towers (~half the bottom 2 layers are grass_block/dirt). The user then asked for a system
instead: swap the base for the natural ground wherever it doesn't match.

- Hooks: StructureTemplateGroundMatchMixin (RETURN of Forge processBlockInfos, like the Lootr swaps) +
  StructureStartGroundMatchMixin (placeInChunk m_226850_ HEAD/RETURN sets a ThreadLocal start). It only acts in a
  WorldGenRegion, or on a ServerLevel inside placeInChunk (/place structure). Structure blocks and /place template are skipped.
- Decision per chunk: sample the top solid block + 3 below on this chunk and its 8 neighbors (OCEAN_FLOOR_WG, or
  OCEAN_FLOOR for FULL chunks), skipping columns under any structure piece. A family tag is native when it has
  >= nativeShare of the tops. Otherwise its blocks in the bottom baseLayers get mode TERRAIN (keep existing ground, else
  pick from the sample) / PALETTE / REMOVE. Plants on swapped blocks are dropped.
- Tags: creativeengineering:ground_match/{grass,plants,not_ground} in blocktags.js; the structure tag ground_match/exempt
  is in kubejs/server_scripts/worldgen/ground_match.js.
- Test: GroundMatchTest in tools/test_worldgen_tools.sh (37 checks, pure `rewrite()` split out for it). Java test cp is shared in tools/java_test_cp.sh.
- Config moved to [worldgen.ground_match] (+ .sampling); on/off is [worldgen.systems] groundMatch (see [worldgen-config-cdevplace](worldgen-config-cdevplace.md)).
- Writeup (status): feature_writeup/structure_tuning.md 3.8, ST-27..32.

Related: [structure-groups](structure-groups.md), [jigsaw-pool-tuning](jigsaw-pool-tuning.md), [patchermod-coremod](patchermod-coremod.md), memory `patchermod-headless-smoke-test`.
