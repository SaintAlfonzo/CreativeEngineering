# Biome tuning: making biomes more or less common with a climate-sample solver

## 0. At a glance

| Part | Status |
|---|---|
| `biome_config.json`: a multiplier or a replacement per biome, for every `multi_noise` dimension (Overworld from Terralith, End from Nullscape, vanilla Nether) | DEPLOYED (in the 2026-09-27 initial commit; same 17 edits since), applies to worlds created after it |
| `BiomeLayoutTuner` (patcherMod) solves the edits against climate samples at every world load; `biome_overrides.js` adds the result to KubeJS's virtual datapack | DEPLOYED |
| `/dumpclimate`: climate samples per dimension, shipped gzipped | DEPLOYED (resampled 2026-09-30 and 2026-10-14) |
| `tools/gen_biome_config.py`: rebuilds the editable config and baselines after a mod update, keeping edits | in use (`/cegen` group `core`) |
| Structure spawn biomes (biome merges) | **retired 2026-10-11** for biome fits ([structure_tuning.md](structure_tuning.md) 7.2, [biome_fits.md](biome_fits.md)) |

- **Pending:** nothing built and unshipped. WG-01 to WG-08 have never been run. Findings F1 to F5 (section 4); F1 and
  F3 need your call.
- **Files to open:** [biome_config.json](../kubejs/generation_scripts/worldgen/biome_config.json) (the edits and the
  solver's results), [BiomeLayoutTuner.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/BiomeLayoutTuner.java),
  [biome_overrides.js](../kubejs/server_scripts/worldgen/biome_overrides.js),
  [dump_climate.js](../kubejs/server_scripts/_lib/dump_climate.js), [gen_biome_config.py](../tools/gen_biome_config.py).
- **Knobs:** `biome_config.json` (`multiplier`, `replace_with` per biome); `config/creativeengineering-common.toml`
  `[worldgen] biomeLayout` (on/off) and `[worldgen.biome_solver]` (`maxCandidates` 24, `rounds` 20).
- **Applies to new worlds only:** a world keeps the biome layout it was created with.

## 1. Overview

A `minecraft:multi_noise` biome source is a list of entries, each a box in 7D climate space (temperature, humidity,
continentalness, erosion, depth, weirdness) plus an offset. For every quart-block the game picks the entry with the
lowest fitness: the squared distance outside the box summed over the six axes, plus offset². Terralith's Overworld has
about 1700 entries for 145 biomes; Nullscape's End has 7 for 6; the vanilla Nether preset has 5 (no JSON, read from
the game's code). There is no setting for "make this biome rarer": only the boxes and offsets.

The pack adds one: a multiplier per biome, which a solver turns into an offset penalty measured against real climate
samples from the pack's noise settings (Terralith + Tectonic).

## 2. Reference

### 2.1 Current edits (2026-10-14 file)

Share = the biome's % of its layer in the sample set (surface or underground); result = after the last solve.

| Dimension | Biome | Multiplier | Share → result | Solver note |
|---|---|---|---|---|
| Overworld | `terralith:skylands_spring` / `summer` / `autumn` / `winter` | 0.15 | 0.07 / 0.13 / 0.03 / 0 → 0 / 0.03 / 0 / 0.03 % | winter never sampled |
| Overworld | `terralith:volcanic_peaks` | 1.5 | 0.47 → 0.70 % | |
| Overworld | `terralith:yellowstone` | 1.5 | 2.8 → 4.2 % | |
| Overworld | `terralith:yosemite_cliffs`, `yosemite_lowlands` | 1.5 | 0.07 → 0.10 % each | |
| Overworld | `terralith:volcanic_crater` | 1.5 | 0 → 0 % | never sampled |
| End | `nullscape:shadowlands` | 0.5 | 19.8 → 9.9 % | |
| End | `nullscape:void_barrens` | 0.75 | 6.0 → 4.9 % | aimed 4.5 % |
| End | `nullscape:crystal_peaks` | 0.25 | 7.1 → 5.0 % | aimed 1.8 % |
| Nether | `minecraft:nether_wastes` | 2.0 | 34.9 → 46.8 % | aimed 69.8 % |
| Nether | `minecraft:basalt_deltas` | 0.666 | 18.6 → 12.4 % | |
| Nether | `minecraft:crimson_forest` | 0.75 | 21.4 → 16.1 % | |
| Nether | `minecraft:warped_forest` | 0.75 | 7.7 → 5.8 % | |
| Nether | `minecraft:soul_sand_valley` | 1.25 | 17.4 → 18.9 % | aimed 21.8 % |

Samples: Overworld 13,498 points (3,000 surface), End 7,050, Nether 15,000, all from seed 7379340783510277000.
Model agreement (the solver's nearest-entry model vs the biome the live game picked at each point): Overworld 99.1 %,
Nether 86.7 %, End 70.0 %.

### 2.2 The fields

- `multiplier`: 1 untouched, 0 removed (its climate space goes to its neighbours), 0.5 half as common, 2 twice. A
  boosted biome grows outward from its own patches into neighbouring climate; it never appears somewhere unrelated.
- `replace_with`: a biome id; every entry of this biome is relabelled. Overrides the multiplier. Needs no samples.
- Read-only, written by the solver: `entries`, `layer`, `share_pct`, `result_pct`, `note`.

### 2.3 Workflow

1. Edit `biome_config.json`.
2. `/reload` or rejoin: the solve runs, `result_pct` and `note` update in the file, and the log has `[biome-tuning]`
   lines.
3. Create a **new** world to use it.
4. After a mod update that changes a dimension's biome list: `python3 tools/gen_biome_config.py` (or `/cegen run core`)
   refreshes baselines and entry counts, keeps edits, adds new biomes at 1.0 and drops vanished ones with a warning.
5. If the noise settings change, or a biome is "never hit": `/dumpclimate [columns] [radius]` (default 3000 columns)
   in any world, then `/reload`.

## 3. What the pack changes

- **Solver** (`BiomeLayoutTuner.run()`, Java because it brute-forces ~1700 entries against every sample; a port of the
  old node script `kubejs/archive/worldgen/gen_biome_overrides.js`):
  - For each sample it computes every entry's base fitness. Adding a penalty P to the offset² of every entry of one
    biome makes it lose exactly the samples it was winning by less than P; a negative P lets it win the ones it was
    losing by less than |P|. One number per tuned biome, chosen so the biome's share lands on multiplier × original.
  - Penalties are picked strictly between two distinct margins, never on a plateau (a tie the game would break
    arbitrarily). Negative penalties are realised by adding a constant to every entry (the winner doesn't change), so
    every written offset is non-negative.
  - Rounds (`rounds`) re-solve each biome with the others' penalties in place; `maxCandidates` limits each sample to the
    closest entries.
  - Output: one full dimension JSON per edited dimension (a biome source has no additive form), plus stats written back
    to `biome_config.json` only when they change. Results are cached on the input files' hashes, so a `/reload` with no
    edit costs a hash.
- **Applying it:** `biome_overrides.js` calls the tuner in `ServerEvents.highPriorityData` (fires during world load,
  world creation included) and adds the files to KubeJS's top-priority virtual datapack, above Terralith and Nullscape.
  Nothing is written to `kubejs/data`.
- **Samples:** `/dumpclimate` evaluates the noise router only (no chunk loads): per random column within the radius, the
  climate at the estimated surface and at 24/48/80/128 blocks below where there's terrain (the Nether uses fixed y
  levels, End void columns y 64), plus the biome the live source picks there. Written gzipped to
  `kubejs/generation_scripts/worldgen/climate/` (~0.2 MB) so the pack ships them.
- **Retired:** biome merges (structure spawn biomes by merged biome groups), replaced 2026-10-11 by biome fits; the old
  scripts are in `kubejs/archive/worldgen/`.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. Five targets aren't reached. Your call.** Nether wastes ×2 gets 46.8 % instead of 69.8 %, soul sand valley 18.9
  instead of 21.8, crystal peaks 5.0 instead of 1.8, void barrens 4.9 instead of 4.5. The notes say "out of the
  penalty range (offset 0.6 either way) or its climate space is all-or-nothing (overlapping boxes)". Accept the
  results, or adjust the multipliers to what's reachable so the file says what happens.
- **F2. Two biomes are never sampled.** `skylands_winter` and `volcanic_crater` have share 0, so their multipliers do
  nothing. `/dumpclimate 20000` would sample enough to tune them (the note's own advice).
- **F3. The skylands are at 15 %, but features target them. Your call.** More Mooblooms spawns in skylands spring,
  and seasons keep the skylands' seasons permanent ([weather.md](weather.md)). At 15 % of an
  already tiny share (under 0.25 % of the surface together) they will be very hard to find. Intended?
- **F4. End model agreement is 70 %.** The solver's model of which biome wins matches the game only 70 % of the time in
  the End (99 % Overworld, 87 % Nether), so End results are rough. Likely the End's island noise; not investigated.
- **F5. One seed.** All samples come from one seed; shares are noise-distribution properties, so this should hold for
  other seeds, but it isn't checked.

### Known limits

- **New worlds only**, by the way Minecraft stores layouts. The deployed server world keeps whatever layout it was
  created with.
- **Presets and flat dimensions** (the pack's sky dimensions) can't be tuned; only inline `multi_noise` lists.
- **Structures follow biomes:** making a biome rarer also makes structures that only spawn there rarer.

### Rejected options

- **Editing Terralith's dimension JSON by hand:** 1700 entries, and a mod update would overwrite it.
- **The node solver:** too slow and drifted from the game; replaced by the Java port with a model-agreement check.
- **Biome merges for structure biomes:** replaced by biome fits.

## 5. File map

- [kubejs/generation_scripts/worldgen/biome_config.json](../kubejs/generation_scripts/worldgen/biome_config.json), `biome_baselines/`, `climate/*.json.gz`; [worldgen/README.md](../kubejs/generation_scripts/worldgen/README.md).
- [kubejs/server_scripts/worldgen/biome_overrides.js](../kubejs/server_scripts/worldgen/biome_overrides.js), [kubejs/server_scripts/_lib/dump_climate.js](../kubejs/server_scripts/_lib/dump_climate.js).
- patcherMod [worldgen/BiomeLayoutTuner.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/BiomeLayoutTuner.java); `CEConfig.Worldgen` (`biomeLayout`, `[worldgen.biome_solver]`).
- [tools/gen_biome_config.py](../tools/gen_biome_config.py) (in `generators.json`, groups `core`, `worldgen`).
- Readers of the config: `gen_structure_biome_table.py`, `gen_spawn_table.py`, `gen_crop_worldgen_table.py`, `structure_biome_coverage.py`, `gen_weather_table.py` (climate).
- Archive: `kubejs/archive/worldgen/` (old node solver, biome merges).

## 6. Offline checks

- `python3 tools/gen_biome_config.py` reports added and removed biomes; with no mod change it should be a no-op.
- The solve itself runs in game at `/reload` (log lines `[biome-tuning] ...`); its results land in `biome_config.json`,
  so `git diff` after a reload shows exactly what changed.

## 7. In-game test suite

- [ ] **WG-01 Solve on reload:** `/reload` → `latest.log` has `[biome-tuning] inputs unchanged, reusing the last solve`;
  edit a multiplier and `/reload` → a fresh solve, `result_pct` updated in the file.
- [ ] **WG-02 New world:** create a world, `/locate biome terralith:yellowstone` from a few spots → found more often
  than in a world created with `[worldgen] biomeLayout = false` (same seed).
- [ ] **WG-03 Off switch:** `biomeLayout = false`, `/reload` → no `[biome-tuning]` solve; a new world has Terralith's
  stock layout. Turn it back on.
- [ ] **WG-04 Nether:** in a new world's Nether, fly 2000 blocks → nether wastes clearly the most common, basalt deltas
  less than usual.
- [ ] **WG-05 End:** outer End islands in a new world → shadowlands noticeably rarer than in a world made with
  `biomeLayout = false`; note how crystal peaks compare (F1).
- [ ] **WG-06 Skylands (F3):** `/locate biome terralith:skylands_spring` in a new world → note the distance.
- [ ] **WG-07 Replace:** set `replace_with` on a test biome, `/reload`, new world → that biome never appears; its
  replacement takes its place.
- [ ] **WG-08 /dumpclimate:** `/dumpclimate 500` → three `.json.gz` files rewritten in
  `kubejs/generation_scripts/worldgen/climate/`, a few seconds of server time, no chunk generation.

## 8. History

- By 2026-09-27 (initial git commit): Java solver (port of the node script), the current 17 edits.
- 2026-09-30: samples resampled on a new seed and gzipped; model agreement 51 % → 99 % (Overworld)
  ([plan_1.0.3.md](plan_1.0.3.md) WG).
- 2026-10-11: biome merges retired for biome fits.
- 2026-10-14: samples refreshed (the current file's dates).
- 2026-10-18: this writeup; findings F1 to F5; tests WG-01 to WG-08. No code changed.
