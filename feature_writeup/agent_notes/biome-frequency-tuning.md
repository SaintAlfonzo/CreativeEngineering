# biome-frequency-tuning

> Biome frequency tuner (2026-09-25) — per-biome multiplier/replace_with in biome_config.json, solved as offset penalties against /dumpclimate samples, applied at world load by patcherMod BiomeLayoutTuner (Java) via KubeJS highPriorityData; new worlds only

Moved from agent memory 2026-11-16 (last edited there 2026-09-26). Repo status docs win over any status line here.

Built 2026-09-25, pipeline tested on SYNTHETIC samples only — no real `/dumpclimate` run or in-game world yet.

- `tools/gen_biome_config.py` -> `kubejs/generation_scripts/worldgen/biome_config.json` + `biome_baselines/`. Tunable dims: overworld (Terralith inline list, 145 biomes/1697 entries), the_end (Nullscape), the_nether (vanilla preset hardcoded from bytecode). Tectonic's `resourcepacks/legacy` overworld.json is ignored (inactive).
- `kubejs/server_scripts/_lib/dump_climate.js` -> `/dumpclimate [columns] [radius]` -> `kubejs/generation_scripts/worldgen/climate/<ns>__<dim>.json.gz` since 2026-09-30 (compact gzipped, shipped; tuner falls back to the old `exported/` json). **Rhino-untested**: uses ServerChunkCache.randomState(), Climate$Sampler.sample, ChunkGenerator.getBaseHeight, BiomeSource.getNoiseBiome (all javap-verified in 1.19.2 mojmap).
- `gen_biome_overrides.js`: fitness = Σd² + offset² (bytecode-verified), so one penalty per biome on offset² gives an exact, monotone share dial. It solves margins directly (sorted margins, pick between distinct values to avoid overlap plateaus = ties). Offsets are written as (oq+0.5)/1e4 so MC's `(long)(float*1e4)` truncation round-trips exactly. result_pct is brute-forced from the written file.
- Why the offline climate sample instead of headless noise: Tectonic overrides erosion noise + density functions, so reproducing the router outside the game is fragile.
- **Existing saves pin the full biome list in level.dat** (checked: 872 `terralith:` refs per save), so changes apply to new worlds only.

**2026-09-26: node script replaced.** The solver is now `patcherMod/.../worldgen/BiomeLayoutTuner.java`, a line-for-line port called from `kubejs/server_scripts/worldgen/biome_overrides.js` in ServerEvents.highPriorityData. That event fires on world creation too, so a new world gets the layout. It's Java rather than Rhino because it does ~90M fitness evaluations, about 2s even in Java/node. It caches on a SHA-256 of config + baselines + samples, so a /reload without edits costs about 7ms. Stats are written back to biome_config.json only when they changed, using Gson pretty + serializeNulls + disableHtmlEscaping + JS-style whole numbers, which is byte-identical to the old node writeback. An error in one dimension skips only that dimension. Old script: kubejs/archive/worldgen/.

Related: [structure-gen-framework](structure-gen-framework.md) (same runtime pattern, same README).

**Blueprint wraps the biome source (found 2026-09-26).** In a live world the overworld and End
biome source is Blueprint's `ModdedBiomeSource` (level.dat stores it as `blueprint:modded`), not
`MultiNoiseBiomeSource`. The wrapped original is a private field with no getter. `/dumpclimate`
used to skip every dimension because of this ("done in 0.0s", zero files written). It now accepts
the wrapper. Climate comes from `randomState.sampler()`, so the samples are unaffected; only
`live_biome` can include Blueprint slice biomes, which lowers `model_agreement_pct`. Each dump now
records a `biome_source` field. Also: the dev world `saves/Workspace` has **no Nether dimension** in
level.dat (missing since ~July), so the tuner's Nether "needs a climate sample" warning can't be
resolved in that world.

**2026-10-19: rarity floor + dense samples.** The Overworld was resampled at 20,000 columns (88,779 points, 721 KB gz).
The 3,000-column set was 1 sample per 0.033 %, so many shares were noise. `tools/biome_rarity_floor.py` lifts sampled
surface biomes under 0.15 % (and the underground jungle to 1.2 %), paid by the 12 largest land biomes (x0.853).
Oceans, rivers, beaches, skylands, yellowstone, deep dark and shield clearing are left alone. The solve was done on a
scratch server (/reload writes result_pct back). The Overworld solve now takes about 10 s per world load. Never
sampled even then: 7 surface + 3 cave biomes. Mushroom fields caps at 0.055 %.

**2026-10-19 round 2.** BiomeLayoutTuner gained `target_pct` (absolute share, works for never-sampled biomes) and
`target_layer`. /dumpclimatecaves writes `<dim>.caves.json.gz`, which replaces the underground layer (mergeCaves).
The layer is now picked by depth-0 entries (surface biomes have depth 0 and depth 1 entries, caves only depth > 0).
The old biggest-share rule flipped warm river/skylands underground once deep samples existed. biome_rarity_floor.py
targets never-sampled surface biomes at 0.3 % (crater and shield clearing 0.15) and owns every Overworld multiplier
except KEEP and USER_BOOSTS. All 9 targets generate (scratch /locate). Tuff caves can't win anywhere (0.24 offset vs
deep dark). The /dumpclimate default is now 20,000 columns, because /dumpall would have overwritten the dense samples.
