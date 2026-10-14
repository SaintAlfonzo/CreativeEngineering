# Biome merges (retired 2026-10-11)

The first system that put structures into Terralith biomes: `biome_merges.json` added biomes to mods' `has_structure/*`
biome tags (additive, 45 tags, 209 biome additions), applied by `server_scripts/worldgen/biome_merges.js` on every tag
load, switch CEConfig `[worldgen.systems] biomeMerges`. Proposals came from `analyze_structure_biome_merges.py`
(themes of modded biomes -> each mod's tags), `build_biome_merges.py` built the approved file.

Replaced by biome fits (`kubejs/generation_scripts/worldgen/biome_fits.json`, feature_writeup/structure_tuning.md 3.17
and 7.1), which replace one structure's list instead of widening a shared tag. Removed on request before the 7.1
`land/*` fits were built, so the structures these merges covered spawn in their mods' own biome lists until then.
Kept for reference only: nothing loads these files (the scripts no longer sit in a KubeJS script folder or `tools/`).
