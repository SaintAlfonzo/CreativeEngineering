# Biome tuning: making biomes more or less common with a climate-sample solver

## 0. At a glance

| Part | Status |
|---|---|
| `biome_config.json`: a multiplier or a replacement per biome, for every `multi_noise` dimension (Overworld from Terralith, End from Nullscape, vanilla Nether) | DEPLOYED (in the 2026-09-27 initial commit; the same 17 edits until 2026-10-19), applies to worlds created after it. Overworld rarity floor (53 more edits, `tools/biome_rarity_floor.py`) BUILT 2026-10-19, solved on a scratch server, DEPLOYED with 1.0.9 (2026-10-19 15:54) |
| `BiomeLayoutTuner` (patcherMod) solves the edits against climate samples at every world load; `biome_overrides.js` adds the result to KubeJS's virtual datapack | DEPLOYED |
| `/dumpclimate`: climate samples per dimension, shipped gzipped | DEPLOYED (resampled 2026-09-30 and 2026-10-14); Overworld resampled at 20,000 columns and caves sampled 2026-10-19, DEPLOYED with 1.0.9 |
| `tools/gen_biome_config.py`: rebuilds the editable config and baselines after a mod update, keeping edits | in use (`/cdevgen` group `core`) |
| Structure spawn biomes (biome merges) | **retired 2026-10-11** for biome fits ([structure_tuning.md](structure_tuning.md) 7.2, [biome_fits.md](biome_fits.md)) |

- **Pending:** the rarity floor, `target_pct`, the 20,000-column and cave samples (2026-10-19) shipped with 1.0.9 and
  apply to worlds created after it; WG-09 to WG-13 not run in game. WG-01 to WG-08 have never been run. Findings F1 to F5 (section 4); F1 and
  F3 need your call.
- **Files to open:** [biome_config.json](../kubejs/generation_scripts/worldgen/biome_config.json) (the edits and the
  solver's results), [BiomeLayoutTuner.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/BiomeLayoutTuner.java),
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

### 2.1 Current edits (2026-10-19 file)

Share = the biome's % of its layer in the sample set (surface or underground); result = after the last solve.

| Dimension | Biome | Multiplier | Share → result | Solver note |
|---|---|---|---|---|
| Overworld | `terralith:skylands_spring` / `summer` / `autumn` / `winter` | 0.15 | 0.065 / 0.08 / 0.225 / 0.225 → 0.01 / 0.01 / 0.035 / 0.035 % | |
| Overworld | `terralith:volcanic_peaks` | 1.5 | 0.205 → 0.27 % | aimed 0.31 % |
| Overworld | `terralith:yellowstone` | 1.5 | 3.175 → 4.775 % | |
| Overworld | `terralith:yosemite_cliffs`, `yosemite_lowlands` | 1.5 | 0.14 / 0.11 → 0.2 / 0.16 % | |
| Overworld | rarity floor + targets: 41 rare biomes lifted (37 surface, 4 cave), 9 never-spawning biomes targeted (volcanic crater included), 14 donors (tables below) | see below | | 2026-10-19 |
| End | `nullscape:shadowlands` | 0.75 (0.5 before 2026-11-02, row #9) | 18.933 → about 14.2 % (old samples; aimed about 16 % of the 21.3 % share) | confirm in the boot log (SQ-WG-BIOME-SHADOW) |
| End | `nullscape:void_barrens` | 2 (0.75 before 2026-10-30) | 2.71 → 5.41 % (2026-10-26 samples) | |
| End | `nullscape:crystal_peaks` | 3 (0.25 before 2026-10-30) | 1.315 → 3.95 % (2026-10-26 samples) | |
| Nether | `minecraft:nether_wastes` | 2.0 | 34.9 → 46.8 % | aimed 69.8 % |
| Nether | `minecraft:basalt_deltas` | 0.666 | 18.6 → 12.4 % | |
| Nether | `minecraft:crimson_forest` | 0.75 | 21.4 → 16.067 % | |
| Nether | `minecraft:warped_forest` | 0.75 | 7.7 → 5.767 % | |
| Nether | `minecraft:soul_sand_valley` | 1.25 | 17.4 → 18.9 % | aimed 21.8 % |

**Rarity floor and targets (user, 2026-10-19: "allow the rarest biomes to spawn more frequently, take the chance
from the highest frequency biomes"; then "widen the surface biome spawn requirements to allow reasonable spawning
frequency").** `python3 tools/biome_rarity_floor.py` (defaults `--floor 0.15 --cave-floor 1.2 --unsampled 0.3
--donor-min 1.0`), solved on a scratch server:

- Every sampled surface biome under 0.15 % gets the multiplier that lifts it to 0.15 % (39 biomes).
- The 9 surface biomes the samples never hit get `target_pct` 0.3 % (the median sampled share), or 0.15 % for the
  volcanic crater (inside volcanic peaks) and the shield clearing (inside the shield). A multiplier can't scale
  nothing; a target makes their entries win the samples they lose by the smallest margins, which widens their
  climate space evenly in every direction.
- The 12 surface biomes at 1 % or more that are not ocean, river or beach all give up the same fraction (x0.736)
  to pay the 5.4 % the others gain. Oceans, rivers and beaches are left out: cutting them would change how much of
  the world is land, not just the biome mix.
- Underground (measured on `/dumpclimatecaves` samples): caves under 1.2 % lifted to 1.2 % (frostfire, underground
  jungle, mantle, diorite), paid by deep caves and lush caves (x0.941).
- Left out on purpose: the skylands (cut to 0.15 on purpose), yellowstone and the user's other boosts, deep dark
  (ancient cities). The tool owns every other Overworld multiplier and resets the ones it no longer needs to 1.

Never-spawning surface biomes (all found by `/locate biome` in a new scratch world, 71 to 2,548 blocks away):

| Biome | Layer | Multiplier / target | Share → result | Solver note |
|---|---|---:|---|---|
| `minecraft:stony_shore` | surface | target 0.3 % | 0 → 0.305 % |  |
| `terralith:basalt_cliffs` | surface | target 0.3 % | 0 → 0.295 % |  |
| `terralith:caldera` | surface | target 0.3 % | 0 → 0.24 % | aimed 0.3% |
| `terralith:frozen_cliffs` | surface | target 0.3 % | 0 → 0.295 % |  |
| `terralith:granite_cliffs` | surface | target 0.3 % | 0 → 0.305 % |  |
| `terralith:shield_clearing` | surface | target 0.15 % | 0 → 0.155 % |  |
| `terralith:valley_clearing` | surface | target 0.3 % | 0 → 0.3 % |  |
| `terralith:volcanic_crater` | surface | target 0.15 % | 0 → 0.15 % |  |
| `terralith:white_cliffs` | surface | target 0.3 % | 0 → 0.29 % |  |

Lifted:

| Biome | Layer | Multiplier / target | Share → result | Solver note |
|---|---|---:|---|---|
| `terralith:lush_desert` | surface | 30.0 | 0.005 → 0.135 % | aimed 0.15% |
| `terralith:alpha_islands_winter` | surface | 15.0 | 0.01 → 0.15 % |  |
| `terralith:lavender_forest` | surface | 10.0 | 0.015 → 0.15 % |  |
| `terralith:savanna_slopes` | surface | 10.0 | 0.015 → 0.145 % |  |
| `terralith:red_oasis` | surface | 7.5 | 0.02 → 0.15 % |  |
| `terralith:siberian_grove` | surface | 6.0 | 0.025 → 0.15 % |  |
| `terralith:warm_river` | surface | 6.0 | 0.025 → 0.145 % |  |
| `terralith:ancient_sands` | surface | 5.0 | 0.03 → 0.15 % |  |
| `terralith:painted_mountains` | surface | 5.0 | 0.03 → 0.16 % |  |
| `terralith:rocky_shrubland` | surface | 4.29 | 0.035 → 0.15 % |  |
| `minecraft:mushroom_fields` | surface | 3.75 | 0.04 → 0.06 % | aimed 0.15% |
| `terralith:alpha_islands` | surface | 3.75 | 0.04 → 0.15 % |  |
| `terralith:ashen_savanna` | surface | 3.75 | 0.04 → 0.125 % | aimed 0.15% |
| `terralith:snowy_maple_forest` | surface | 3.33 | 0.045 → 0.155 % |  |
| `terralith:cloud_forest` | surface | 2.73 | 0.055 → 0.15 % |  |
| `terralith:desert_canyon` | surface | 2.5 | 0.06 → 0.15 % |  |
| `terralith:amethyst_rainforest` | surface | 2.14 | 0.07 → 0.145 % |  |
| `terralith:desert_oasis` | surface | 2.14 | 0.07 → 0.105 % | aimed 0.15% |
| `terralith:sakura_valley` | surface | 2.0 | 0.075 → 0.15 % |  |
| `terralith:snowy_shield` | surface | 1.76 | 0.085 → 0.145 % |  |
| `minecraft:eroded_badlands` | surface | 1.67 | 0.09 → 0.135 % | aimed 0.15% |
| `terralith:haze_mountain` | surface | 1.67 | 0.09 → 0.15 % |  |
| `terralith:sakura_grove` | surface | 1.58 | 0.095 → 0.15 % |  |
| `terralith:wintry_lowlands` | surface | 1.58 | 0.095 → 0.14 % |  |
| `minecraft:flower_forest` | surface | 1.5 | 0.1 → 0.15 % |  |
| `minecraft:windswept_gravelly_hills` | surface | 1.5 | 0.1 → 0.15 % |  |
| `terralith:white_mesa` | surface | 1.5 | 0.1 → 0.15 % |  |
| `minecraft:ice_spikes` | surface | 1.43 | 0.105 → 0.145 % |  |
| `terralith:amethyst_canyon` | surface | 1.43 | 0.105 → 0.15 % |  |
| `terralith:desert_spires` | surface | 1.43 | 0.105 → 0.155 % |  |
| `minecraft:sunflower_plains` | surface | 1.36 | 0.11 → 0.14 % |  |
| `terralith:emerald_peaks` | surface | 1.36 | 0.11 → 0.15 % |  |
| `terralith:moonlight_grove` | surface | 1.36 | 0.11 → 0.15 % |  |
| `terralith:warped_mesa` | surface | 1.36 | 0.11 → 0.15 % |  |
| `minecraft:windswept_hills` | surface | 1.2 | 0.125 → 0.14 % |  |
| `terralith:bryce_canyon` | surface | 1.15 | 0.13 → 0.16 % |  |
| `terralith:alpine_grove` | surface | 1.11 | 0.135 → 0.15 % |  |
| `terralith:cave/frostfire_caves` | underground | 2.48 | 0.483 → 1.196 % |  |
| `terralith:cave/underground_jungle` | underground | 1.68 | 0.715 → 1.203 % |  |
| `terralith:cave/mantle_caves` | underground | 1.61 | 0.746 → 0.725 % | aimed 1.203% |
| `terralith:cave/diorite_caves` | underground | 1.18 | 1.013 → 1.188 % |  |

Donors:

| Biome | Layer | Multiplier / target | Share → result | Solver note |
|---|---|---:|---|---|
| `terralith:forested_highlands` | surface | 0.736 | 1 → 0.715 % |  |
| `terralith:alpine_highlands` | surface | 0.736 | 1.04 → 0.72 % | aimed 0.765% |
| `terralith:glacial_chasm` | surface | 0.736 | 1.115 → 0.81 % |  |
| `minecraft:meadow` | surface | 0.736 | 1.285 → 0.96 % |  |
| `terralith:shield` | surface | 0.736 | 1.335 → 0.95 % |  |
| `terralith:rocky_mountains` | surface | 0.736 | 1.48 → 1.12 % |  |
| `minecraft:snowy_taiga` | surface | 0.736 | 1.52 → 1.135 % |  |
| `terralith:brushland` | surface | 0.736 | 1.655 → 1.205 % |  |
| `minecraft:taiga` | surface | 0.736 | 1.74 → 1.2 % | aimed 1.28% |
| `minecraft:snowy_plains` | surface | 0.736 | 1.87 → 1.36 % |  |
| `minecraft:plains` | surface | 0.736 | 2.82 → 2.085 % |  |
| `minecraft:forest` | surface | 0.736 | 3.545 → 2.585 % |  |
| `minecraft:lush_caves` | underground | 0.941 | 13.199 → 12.412 % |  |
| `terralith:cave/deep_caves` | underground | 0.941 | 18.167 → 17.623 % |  |

Still out of reach: `terralith:cave/tuff_caves` (never sampled, even by `/dumpclimatecaves`: its entry carries a 0.24
offset and deep dark covers the same climate with none), mantle caves (0.725 %, aimed 1.2 %: its climate space is
all-or-nothing), mushroom fields (about 0.06 %). Caldera stops at 0.24 % of its 0.3 % target.

Samples (2026-10-19): Overworld surface 20,000 points from `/dumpclimate 20000` (radius 20,000; 721 KB gzipped,
was 3,000 / 111 KB), underground 42,071 points from `/dumpclimatecaves 5000` (every 16 blocks from 16 below the
surface to the world bottom; 244 KB), replacing the main dump's four shallow levels; End 7,050, Nether 15,000; all
seed 7379340783510277000. The denser surface set changed many base shares (volcanic peaks 0.47 → 0.205 %,
yellowstone 2.8 → 3.175 %): the 3,000-column set was one sample per 0.033 %. Model agreement: Overworld
96.639 %, Nether 86.4 %, End 80.1 % (End and Nether samples of 2026-10-18; the End's Nullscape biomes
are now measured on the surface layer, 3,000 points, where the 2026-10-14 file measured them underground, hence the
new End shares). The Overworld solve takes about 10 s per world load (about 2 s
before), only when an input changed: the result is cached on the input hashes in memory and in
`biome_layout_cache.json.gz` (2026-10-27, perf.md 11.2 #3).

### 2.2 The fields

- `multiplier`: 1 untouched, 0 removed (its climate space goes to its neighbours), 0.5 half as common, 2 twice. A
  boosted biome grows outward from its own patches into neighbouring climate; it never appears somewhere unrelated.
- `replace_with`: a biome id; every entry of this biome is relabelled. Overrides the multiplier. Needs no samples.
- `target_pct` (2026-10-19): an absolute share of the biome's layer, in place of the multiplier. Works for a biome
  the samples never hit. `target_layer` names the layer for such a biome (default surface).
- Read-only, written by the solver: `entries`, `layer`, `share_pct`, `result_pct`, `note`. `layer`: in the
  Overworld, a biome with a climate entry whose depth range holds 0 is a surface biome, else a cave biome (the 14
  cave biomes). Surface biomes also win deep samples through their depth-1 entries, so "the layer where it holds the
  biggest share" (still used for other dimensions) put warm river and the skylands underground once deep samples
  existed.

### 2.3 Workflow

1. Edit `biome_config.json`.
2. `/reload` or rejoin: the solve runs, `result_pct` and `note` update in the file, and the log has `[biome-tuning]`
   lines.
3. Create a **new** world to use it.
4. After a mod update that changes a dimension's biome list: `python3 tools/gen_biome_config.py` (or `/cdevgen run core`)
   refreshes baselines and entry counts, keeps edits, adds new biomes at 1.0 and drops vanished ones with a warning.
5. If the noise settings change, or a biome is "never hit": `/dumpclimate [columns] [radius]` (default 20,000
   columns since 2026-10-19, about 4.5 minutes) and `/dumpclimatecaves [columns] [radius] [step]` (default 5,000
   columns every 16 blocks, about 20 s) in any world, then `/reload`. They are separate so each can be redone
   alone; `/dumpall` runs both.
6. To re-apply the rarity floor after new samples: `python3 tools/biome_rarity_floor.py`, then `/reload` and check
   `result_pct` and the notes.

## 3. What the pack changes

- **Solver** (`BiomeLayoutTuner.run()`, Java because it brute-forces ~1700 entries against every sample; a port of the
  old node script `gen_biome_overrides.js`, deleted 2026-10-22):
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
    edit costs a hash. The last clean solve is also kept in `generation_scripts/worldgen/biome_layout_cache.json.gz`
    (ships with the pack; key adds a hash of the solver's class files), so a new launch reads it back instead of
    solving. Delete the file to force a solve; a changed config, baseline, sample, solver setting or patcherMod solver
    re-solves on its own and rewrites it. Log line on a hit: `inputs unchanged, reusing the last solve (disk)`.
- **Applying it:** `biome_overrides.js` calls the tuner in `ServerEvents.highPriorityData` (fires during world load,
  world creation included) and adds the files to KubeJS's top-priority virtual datapack, above Terralith and Nullscape.
  Nothing is written to `kubejs/data`.
- **Samples:** `/dumpclimate` evaluates the noise router only (no chunk loads): per random column within the radius, the
  climate at the estimated surface and at 24/48/80/128 blocks below where there's terrain (the Nether uses fixed y
  levels, End void columns y 64), plus the biome the live source picks there. Written gzipped to
  `kubejs/generation_scripts/worldgen/climate/` (~0.8 MB at 20,000 columns) so the pack ships them.
- **Cave samples** (2026-10-19): `/dumpclimatecaves` samples the Overworld every `step` blocks from 16 below the
  estimated surface to the world bottom, into `<dim>.caves.json.gz`. When that file exists the tuner drops the main
  dump's underground points and uses these as the underground layer (`BiomeLayoutTuner.mergeCaves`). The four
  shallow levels never reached depth 1.0 and up, where deep dark, mantle, frostfire and tuff caves live.
- **Targets** (2026-10-19): `target_pct` sets the solver's target count directly instead of multiplier x original
  count; the margin solve is the same, so a never-hit biome gets a negative penalty (its boxes widen evenly).
- **Retired:** biome merges (structure spawn biomes by merged biome groups), replaced 2026-10-11 by biome fits; the old
  scripts (formerly `kubejs/archive/worldgen/`) were deleted 2026-10-22.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. Five targets aren't reached. Your call.** Nether wastes ×2 gets 46.8 % instead of 69.8 %, soul sand valley 18.9
  instead of 21.8, crystal peaks 1.667 instead of 0.767, void barrens 2.767 instead of 2.967 (2026-10-18 samples; the
  current file also has 10 Overworld notes, mostly near misses of the rarity floor). The notes say "out of the
  penalty range (offset 0.6 either way) or its climate space is all-or-nothing (overlapping boxes)". Accept the
  results, or adjust the multipliers to what's reachable so the file says what happens. End half settled 2026-10-30
  (TODO `worldgen/biomes #8`): crystal peaks x3, void barrens x2, both reached (3.95 % / 5.41 % on the 2026-10-26
  samples), no End notes left.
- **F2. Never-sampled biomes. Resolved 2026-10-19.** The 20,000-column dump sampled `skylands_winter`; the 9
  surface biomes still never hit got `target_pct` and all generate in a new world; `/dumpclimatecaves` sampled
  every cave biome but tuff caves, which can't win anywhere (2.1).
- **F3. The skylands are at 15 %, but features target them. Your call.** More Mooblooms spawns in skylands spring,
  and seasons keep the skylands' seasons permanent ([weather.md](weather.md)). At 15 % of an
  already tiny share (under 0.25 % of the surface together) they will be very hard to find. Intended?
- **F4. End model agreement is 80 %** (70 % before the 2026-10-18 samples). The solver's model of which biome wins
  matches the game only 80 % of the time in the End (96.6 % Overworld, 86.4 % Nether), so End results are rough.
  2026-10-21 lead: every mismatch is a live `end_highlands` where the model expects shadowlands, crystal peaks or void
  barrens, and no End dump has ever held crystal peaks live ([briefs/worldgen_biomes.md](briefs/worldgen_biomes.md) #4).
  Scratch 2026-10-26 (SQ-INV-06, one seed): `locate biome` found crystal peaks 1064 blocks and void barrens 1086 blocks
  from 0 0, so the dump gaps were sampling (TODO `worldgen/biomes #8`, close after a second seed). Owner 2026-10-29:
  shadowlands seem not to generate in the End; raise the modded End biomes (TODO `worldgen/biomes #9`). The config
  halves shadowlands on purpose (`multiplier` 0.5, aimed 10.7 % of the End surface, table above), and every model
  mismatch is a live `end_highlands` where the model expects shadowlands, so the live share is likely lower still.
  2026-10-30 (user: make them clearly findable): crystal peaks multiplier 0.25 -> 3 (model 0.61 % -> 3.95 %), void
  barrens 0.75 -> 2 (1.825 % -> 5.41 %); shadowlands untouched (row #9). Raised generously because the live share
  likely sits under the model. Second-seed `locate biome` check: scratch queue SQ-WG30-E03, PARTIAL 2026-10-30 on seed
  8675309: both found from 0 0 and 3000 0 (crystal peaks 1361 / 1647 blocks, void barrens 906 / 1345), only one nearer
  than the old-seed 1,064 / 1,086; a live 20,000-point `getNoiseBiome` sample of the End (r 20,000, y 64) gives
  crystal peaks 5.28 %, void barrens 7.11 %, above the model's 3.95 / 5.41 %.
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
- patcherMod [worldgen/BiomeLayoutTuner.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/BiomeLayoutTuner.java); `CEConfig.Worldgen` (`biomeLayout`, `[worldgen.biome_solver]`).
- [tools/gen_biome_config.py](../tools/gen_biome_config.py) (in `generators.json`, groups `core`, `worldgen`).
- [tools/biome_rarity_floor.py](../tools/biome_rarity_floor.py): writes the rarity-floor multipliers (2.1); rerun after a resample.
- Readers of the config: `gen_structure_biome_table.py`, `gen_spawn_table.py`, `gen_crop_worldgen_table.py`, `structure_biome_coverage.py`, `gen_weather_table.py` (climate).
- Archive: the old node solver and biome merges (formerly `kubejs/archive/worldgen/`) were deleted 2026-10-22; git history has them.

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
- [ ] **WG-09 Rarity floor:** in a world created after 2026-10-19, `/locate biome` for `terralith:lush_desert`,
  `lavender_forest`, `red_oasis`, `siberian_grove` → each found (they were 0.005 to 0.025 % before, now about 0.15 %).
- [ ] **WG-10 Solve time:** create a new world → `latest.log` `[biome-tuning] override minecraft:overworld (72 tuned`,
  and world creation is no more than about 10 s slower than before.
- [ ] **WG-11 Never-spawning biomes:** in a new world, `/locate biome` each of `minecraft:stony_shore`,
  `terralith:basalt_cliffs`, `caldera`, `frozen_cliffs`, `granite_cliffs`, `valley_clearing`, `white_cliffs`,
  `shield_clearing`, `volcanic_crater` → each found (scratch world 2026-10-19: 71 to 2,548 blocks away).
- [ ] **WG-12 /dumpclimatecaves:** `/dumpclimatecaves 500` → `minecraft__overworld.caves.json.gz` rewritten, the
  Nether and End skipped, `/reload` → `biome_config.json` `samples.file` names both files.
- [ ] **WG-13 Cave biomes:** in a new world, `/locate biome terralith:cave/frostfire_caves` and
  `terralith:cave/mantle_caves` → found (they were never sampled before).

## 8. History

- By 2026-09-27 (initial git commit): Java solver (port of the node script), the current 17 edits.
- 2026-09-30: samples resampled on a new seed and gzipped; model agreement 51 % → 99 % (Overworld)
  ([plan_1.0.3.md](plan_1.0.3.md) WG).
- 2026-10-11: biome merges retired for biome fits.
- 2026-10-14: samples refreshed (the current file's dates).
- 2026-10-18: this writeup; findings F1 to F5; tests WG-01 to WG-08. No code changed.
- 2026-10-19: Overworld resampled at 20,000 columns (88,779 points; scratch server, same seed); rarity floor
  (user: rarest biomes more common, paid by the most common): `tools/biome_rarity_floor.py`, 39 lifted, 14 donors,
  solved in game on the scratch server (2.1). F2 mostly resolved. Tests WG-09, WG-10.
- 2026-10-19 (round 2): `target_pct` / `target_layer` in `BiomeLayoutTuner` (patcherMod 1.0.9 rebuilt) and
  `gen_biome_config.py`; `/dumpclimatecaves` + caves-file merge; layer by depth-0 entries (the share rule put
  surface biomes underground once deep samples existed, and the floor tool then gave lush caves x10: caught on the
  scratch server); `/dumpclimate` default 20,000 columns (`/dumpall` would otherwise have overwritten the dense
  samples with 3,000). User: "widen the surface biome spawn requirements" → 9 targets, all found in a new world.
  F2 resolved. Tests WG-11 to WG-13.
- 2026-10-19: rarity floor, `target_pct`, 20,000-column Overworld samples and cave samples DEPLOYED with pack 1.0.9 (15:54,
  server + clients). The live world keeps its layout (new worlds only).
