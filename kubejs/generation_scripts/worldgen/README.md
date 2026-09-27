# Structure spawn-frequency / removal framework

One place to retune how often any structure generates — vanilla or modded — or kill it
outright, instead of hunting through per-mod config toggles. KubeJS ignores this `.md`.

## Applied at world load: no script to run after an edit

Everything in this folder is config, read by three server scripts in
`kubejs/server_scripts/worldgen/` every time a world loads (including world creation):

| Config | Applied by | Takes effect |
|---|---|---|
| `structure_config.json` + `structure_tags.json` | `structure_overrides.js` (`ServerEvents.highPriorityData`) | rejoin the world; new chunks only |
| `biome_merges.json` | `biome_merges.js` (`ServerEvents.tags('worldgen/biome')`, via `global.tags`) | `/reload` or rejoin; new chunks only |
| `biome_config.json` | `biome_overrides.js` -> patcherMod `worldgen/BiomeLayoutTuner` (Java solver) | new worlds only |
| `pool_config.json` + `pool_tags.json` | patcherMod `worldgen/PoolTuner` (`ServerAboutToStartEvent`, LOWEST) | rejoin the world; new chunks only |
| `loot_container_swaps.json` | patcherMod `worldgen/LootContainerSwaps` + `mixin/StructureTemplateLootSwapMixin` | rejoin the world; new chunks only |

`highPriorityData` fires inside KubeJS's world-load resource wrap, before the game parses worldgen,
and its files go into KubeJS's top-priority virtual datapack (above `kubejs/data`, Terralith,
Nullscape). Nothing is written to `kubejs/data`. A config error applies nothing for that system
(mods' own defaults are used) and is logged as a KubeJS error. The old node generators that wrote
`kubejs/data` files are archived in `kubejs/archive/worldgen/` for reference; don't run them, their
output would double-apply. The `tools/*.py` scanners are still scripts, but only needed after a
mod update (they refresh the baselines and keep your edits).

## Why structure_set, never the structure itself

A structure's spawn **frequency** (spacing/separation/frequency chance), its **xyz coords**
(`locate_offset`, the grid nudge every placement type supports), and which entries even
compete for a given chunk all live on its `structure_set` — a small 3-field document
(`type` / `placement` / `structures`). The structure's own definition (biomes, jigsaw pools,
size…) is what you'd need to touch to move it to *different* biomes, but that's not what this
framework does — it only ever writes `structure_set` overrides, never touches a structure file.
That keeps every override reconstructable from a handful of known fields, whether the
structure_set's original came from a mod's jar or (for vanilla) a hand-maintained defaults
table — no jigsaw-schema landmines. `biomes` is still recorded per structure in the config, as
informational/reference context, from the mod's own structure json or, for vanilla,
`minecraft`'s `has_structure/*` convenience tags — see `tools/gen_structure_config.py`.

## Files

| File | Concern |
|---|---|
| `tools/gen_structure_config.py` | Scans `mods/*.jar` (plus `kubejs/data/`, for structures shipped as plain datapack data; a jar's set always wins) for every `worldgen/structure_set` + `worldgen/structure` json, adds a hand-maintained vanilla-defaults table for the ~19 structure_sets vanilla bootstraps in Java (no loose json to scan) and a `CODE_REGISTERED_SETS` table for modded sets registered in Java (`ae2:meteorite`), marks structures another mod stops in code (`blocked`, from `CODE_BLOCKS` + the current mod configs), and (re)builds `structure_config.json`. Re-run-safe — never clobbers edits already in `current`/`enabled`. |
| `structure_config.json` | **Per-set source of truth.** One entry per structure_set: `source` (which jar, or `vanilla_known_defaults`), `structures` (id/weight/**enabled**/biomes), `original` (diff baseline — leave alone), `current` (**edit this**: spacing/separation/distance/spread/count/frequency/locate_offset). Every raw placement field is carried (exclusion zones, spread types, mod-specific fields), so overrides reproduce a set exactly. |
| `structure_tags.json` | **Category knobs.** Tags of structures, each with a `multiplier`. A structure's multiplier is the product of every tag it's in, applied on top of `current`. See "Structure tags" below. |
| `kubejs/server_scripts/worldgen/structure_overrides.js` | Runs at world load. Applies `current`, `enabled` and the tag multipliers, and adds a structure_set override for every set that differs from its default (plus `creativeengineering:split/...` sets, see below). Sets that end up at their default get no override. |
| `structure_tag_report.md` | Rewritten on every load: active multipliers, each changed set's multiplier chain and final placement, splits, coverage gaps, and every tag's resolved members. On a config error it lists the errors instead. |
| `kubejs/server_scripts/_lib/dump_structuregen.js` | `/dumpstructuregen` (in-world) → `kubejs/exported/worldgen/structuregen.json`, the LIVE post-merge registry values. Verification tool, not the primary data source — see Workflow. |
| `tools/check_structure_live.py` | After `/dumpstructuregen`: lists live sets missing from `structure_config.json` (a mod registering a set in Java, see below), config sets that aren't live, structures in no set, and checks every written set's spacing/separation/count against the live registry. |
| `tools/structure_tuning_sim.js` | `node tools/structure_tuning_sim.js [report.md] [out/]`: runs the real `structure_overrides.js` in node against the current config + tags and prints the summary line and coverage gaps the game would log. Check edits here before rejoining. |

## Workflow

1. **Adjust.** Open `structure_config.json`. To retune frequency: edit `current.spacing` /
   `current.separation` (`random_spread`) or `current.distance` / `current.spread` /
   `current.count` (`concentric_rings`, strongholds only), or `current.frequency` (0.0–1.0
   chance multiplier — some vanilla sets like mineshafts/buried_treasure already use this
   instead of a wide spacing grid). To nudge grid alignment, edit `current.locate_offset`
   (`[x, y, z]`, integer chunk-ish offset — the literal "xyz coords" knob).
2. **Remove.** Flip a structure's `"enabled"` to `false`. If it's the sole member of its
   structure_set, the generator kills the whole set via `frequency: 0` (always schema-safe,
   whatever the placement type). If it shares a set with structures you're keeping (e.g. one
   village variant out of five), it's just dropped from that set's `structures` list.
3. **Tune categories.** Set `multiplier` on tags in `structure_tags.json` (see below).
4. **Apply.** Rejoin the world. Worldgen registries (structure_sets included) load when a world
   loads, so `/reload` only refreshes the report, not the running world. The log shows a
   `[structure-tuning]` summary line and `structure_tag_report.md` is rewritten. Revert an edit and
   rejoin: the set falls back to its mod/vanilla default. Only chunks generated afterwards are
   affected (structures are placed at chunk generation, not retroactively).
5. **Verify.** In-world, run `/dumpstructuregen`, then compare the id in
   `kubejs/exported/worldgen/structuregen.json` with the report.

## Structure tags

`structure_tags.json` groups structures so one number tunes a whole category. Each tag has a
`multiplier` (structures per area: 0.5 = half as many, 2 = twice, 0 = removed) and `values`:
`ns:id`, globs (`ns:*`, `towns_and_towers:pillager_outpost_*`), other tags (`#type/village`),
and `!` in front of any of those to exclude. An unknown id/tag is an error that applies no
structure changes at all (logged + listed in the report), so typos never silently do nothing.

**Tags stack by multiplying.** `mod/themissingvillages` 0.5 and `type/village` 0.5 leave a
Missing Villages village at 0.25 and every other village at 0.5. Each tag counts once per structure
even when it reaches it through several nested tags. The report shows every structure's chain.

| Family | Tags |
|---|---|
| `mod/*` | One per namespace, plus `mod/yungs` (all 6 YUNG's mods). |
| `type/*` | village, house, camp, fort, tower, ship, enemy_building, ruin, ruined_portal, underwater_ruin, shipwreck, mineshaft, underground, environmental, decoration, sky, nether_environmental, nether_constructed, end_environmental, end_constructed. A structure can have several (a T&T outpost tower is `type/tower` and `type/enemy_building`). |
| `dungeon/*` | `surface_*` / `underground_*` crossed with `vanilla` / `better` (YUNG's replacements) / `modded`, plus the unions `dungeon/surface`, `dungeon/underground`, `dungeon/vanilla`, `dungeon/better`, `dungeon/modded`, `dungeon/classic` (vanilla + better). |
| meta | `dungeons`, `pillager` (vanilla outpost + mansion + all 29 T&T outposts), `nether`, `end`, `ruins`. |

Classification sources: T&T outposts are split by the template folder their start pool uses
(`outpost/camps|forts|towers`, `ships/`). Nether/End environmental vs constructed was decided by each
template's block palette (for example MES is almost all purpur and end-stone bricks, so it is
constructed; Nullscape's dragon skeleton is 100% bone blocks, so it is environmental). Dimensions
come from each structure's resolved biome pool: `philipsruins:start_nether_ruin` actually
spawns in the Overworld, and `twigs:bloodstone_obelisk` spawns in the Nether.

**How a multiplier is applied.** Grid placements (`random_spread` and the YUNG/MES subclasses of
it) place one attempt per spacing x spacing cell, so spacing becomes `floor(spacing / sqrt(m))` and
`frequency` absorbs the rounding, which gives the exact multiplier. Separation scales with spacing.
Where spacing is already 1 (mineshafts, buried treasure), only frequency moves. Strongholds
(`concentric_rings`) scale `count`. A set using `legacy_type_1` (outposts) switches to `default`
when its frequency changes, because legacy only rounds to 1/n. If a limit is hit (frequency
can't exceed 1), the report says what was achieved.

**Shared sets split.** Members of one structure_set share one grid. When tags give them different
multipliers, the heaviest group keeps the set and the others move into new sets
(`creativeengineering:split/<ns>/<path>_<n>`, same placement, different salt). This is exact when the
members spawn in different biomes (T&T's per-biome outposts, vanilla's per-biome villages). Where
their biomes overlap, the split-off members stop competing for the same cells and end up somewhat
more common. Other sets' exclusion zones that point at the original set won't see split-off groups.
The report lists every split.

## Landscape facts

- **This pack's `minecraft:villages` and `minecraft:end_cities` are NOT pure vanilla** —
  Terralith and Nullscape respectively already override them (villages spacing is 26, not
  vanilla's 34). The scanner picks up mod jsons over the vanilla table automatically, so
  `structure_config.json` reflects that; `source` on each entry tells you which.
- **Vanilla entries come from a table in the scanner**, not from a jar (vanilla bootstraps these
  in Java, unlike every mod here which ships plain datapack json). The table was checked against
  `StructureSets` bytecode (javap on the mapped forge jar), including the fields that matter for a
  faithful override: outposts' `frequency` 0.2 + exclusion zone around villages, mineshaft /
  buried-treasure RNG methods, the triangular spread of mansions/monuments/end cities, nether
  complex weights (fortress 2 / bastion 3), and the stronghold's required `preferred_biomes`.
- **YUNG's stops six structures in code**, whatever their set says: vanilla desert pyramids,
  jungle temples, witch huts and nether fortresses ("Disable Vanilla X" = true in each mod's
  config), the vanilla stronghold (Better Strongholds, always) and Better Dungeons' small nether
  dungeon ("Enable Small Nether Dungeons" = false). The scanner writes a read-only `blocked` note
  on those entries and the report lists them; tuning them does nothing.
- **Other mods' own frequency configs are left at defaults on purpose.** Towns & Towers'
  `structure_rarity.json5` matches its jar sets, and this framework's overrides win (checked
  against the live registry, 2026-09-27). Philip's Ruins has `"Enable Configs" = false`, so its
  separation/density/toggles are inactive. Village-house weights (Chef's Delight, Wares,
  Monobank, Domestication Innovation, Advanced Peripherals, PneumaticCraft, Cloud Storage) come
  from those mods' configs and show up as `original` in `pool_config.json`. The full list is in
  `feature_writeup/structure_tuning.md`.
- **One modded set is registered in Java:** `ae2:meteorite` (spacing 32 / separation 8, only
  while `config/ae2/common.json` `generateMeteorites` is true). It's in the scanner's
  `CODE_REGISTERED_SETS` table. `tools/check_structure_live.py` finds any new ones.
- **313 raw structure jsons exist across mod jars but only 200 are referenced by any
  structure_set** — the rest are unused template/leftover files that wouldn't naturally
  generate anyway, so they're not tracked here.

## Biome pool merges (structure -> which biomes it can spawn in)

Separate from the structure_set frequency framework above: this widens which biomes a structure's
spawn pool includes (e.g. Terralith's snowy biomes for igloos / snowy villages), by adding entries to
the additive `has_structure/*` biome tags the structures already reference.

| File | Concern |
|---|---|
| `tools/analyze_structure_biome_merges.py` | Resolves every structure's true biome pool (recursive tags, unioned across all jars) and emits numbered proposals -> `biome_merge_proposals.{md,json}` |
| `tools/build_biome_merges.py` | Built the first `biome_merges.json` from the numbered proposals + decisions (held/modified proposals, global skylands + cave rules, variant matching), and the annotated `biome_merge_suggestions.md` review report. **Re-running it regenerates `biome_merges.json` from its constants, overwriting hand edits.** |
| `biome_merges.json` | **Edit this.** One entry per tag: `add` (biome ids), plus read-only notes: which proposals, which structures, `also_affects` (other structures sharing that tag) |
| `kubejs/server_scripts/worldgen/biome_merges.js` | Adds every `add` biome to its tag on each tag load (additive, the mod's own contents stay). Biome ids the game doesn't have are skipped and logged as warnings. |

Workflow: edit `biome_merges.json`, then `/reload` or rejoin. Only chunks generated afterwards are
affected. `biome_merge_suggestions.md` also lists structures already generating in skylands and
cave biomes and marks variant structures that generate in broader biome definitions.

## Biome frequency tuning (how common each biome is)

Make any biome rarer, more common, removed, or swapped for another, per dimension. Covers every
dimension whose `biome_source` is an inline `multi_noise` list: the Overworld (Terralith's
~1700-entry layout), the End (Nullscape), and the Nether (vanilla's preset, rebuilt as a list).

**Mechanism.** The game picks, per quart-block, the parameter entry with the lowest
`sum(distance outside range)^2 + offset^2` over the 6 climate axes (bytecode-checked,
`Climate.ParameterPoint#fitness`). The tuner gives each tuned biome one penalty added to the
`offset^2` of all its entries. A positive penalty makes it lose the samples it was only barely
winning, and a negative one lets it win samples it was barely losing. Either way it grows or shrinks
at its existing borders, into its climate neighbours. It never shows up somewhere climatically
unrelated. Penalties are solved against real climate samples from the game, so `multiplier: 2`
really means "twice the share of the map it had". The whole dimension json is replaced (a
biome_source has no additive form). Negative penalties become one constant added to every
entry, which leaves the argmin unchanged.

| File | Concern |
|---|---|
| `tools/gen_biome_config.py` | Scans `mods/*.jar` for inline multi_noise `dimension/*.json` (+ the hardcoded vanilla Nether preset) -> `biome_config.json` + `biome_baselines/<ns>__<dim>.json` snapshots. Re-run safe: keeps `multiplier`/`replace_with`. |
| `biome_config.json` | **Edit this.** Per dimension, per biome: `multiplier` (1 = untouched, 0 = removed, 0.5 / 2 = half / double), `replace_with` (biome id, exact swap of its climate space). Read-only: `entries`, `layer`, `share_pct` (original % of its layer), `result_pct` (after last apply), `note` (solver warnings). |
| `kubejs/server_scripts/_lib/dump_climate.js` | `/dumpclimate [columns=3000] [radius=20000]` in-world -> `kubejs/exported/worldgen/climate/<ns>__<dim>.json`: climate noise at the surface + 4 underground depths per column, plus the live biome at each point. Pure noise evaluation, no chunk loading. |
| `kubejs/server_scripts/worldgen/biome_overrides.js` | At every world load / creation, calls patcherMod's `BiomeLayoutTuner` (Java: the solver brute-forces every climate entry against every sample, too slow for Rhino), which solves the penalties and returns the dimension json for each edited dimension; the script adds them to KubeJS's virtual datapack. Stats are written back into the config when they change. Results are cached on the input files, so a `/reload` with nothing edited doesn't re-solve. |

Workflow:
1. **Once per modpack change to worldgen:** in any world, `/dumpclimate`. Climate noise depends
   only on seed + noise settings, not on the biome layout, so one dump stays valid across retunes.
   Rare biomes (< ~0.3%) are only a handful of samples; use `/dumpclimate 20000` for smoother
   numbers (a few seconds more server time).
2. `python3 tools/gen_biome_config.py` (only needed after mod updates; config already exists).
3. Edit `multiplier` / `replace_with` in `biome_config.json`, then `/reload` (or rejoin) to see the
   solve: the log prints a `[biome-tuning]` line per dimension and `result_pct` / `note` update in
   the config. Check `result_pct` against `share_pct`. A `note` appears when a target couldn't be
   met: the penalty is capped at an offset of 0.6 either way, and where entries overlap, the
   climate space inside the overlap flips as a whole block.
4. **Create a new world.** A world's biome layout is copied into its `level.dat` at creation
   (verified: every save here embeds the full Terralith list). Existing saves never pick up a
   change. The solve runs again as the new world's datapacks load, so the fresh world gets it.
5. **Verify:** `/dumpclimate` in the new world, then `/reload`. The log reports whether the live
   biomes match the baseline or the tuned layout, and `model_agreement_pct` shows how well the
   model reproduced the live world.

Notes:
- Multiplier 0 (remove) and `replace_with` need no samples; any other multiplier does (that
  dimension is skipped with an error instead of guessing; the others still apply).
- A tuned biome's target is multiplier x its *original* share, so any space it inherits from a
  removed or replaced neighbour counts toward it (the solver compensates).
- Shares are measured on the biome's own layer: `surface` for normal biomes, `underground` for
  cave biomes (Terralith `cave/*`, deep_dark, dripstone/lush caves).
- Untuned biomes shift as a side effect: whoever borders a shrunk biome absorbs its space, and a
  boosted biome takes from its neighbours. `result_pct` shows the collateral for every biome.
- Structures that key off biomes follow automatically (they check the biome at their spot), so
  removing a biome also removes the chance of anything biome-locked to it.
- Tectonic's `resourcepacks/legacy` also ships an `overworld.json`, but it's a pack that's switched
  off (legacy mode false), so the scanner ignores it and only reads top-level `data/`.

## Jigsaw pool pieces (village houses): remove / rarer / more common

A village house is not a structure. It's one weighted entry ("piece") in a jigsaw template pool
such as `minecraft:village/plains/houses`, so it never appears in `structure_config.json`. Many mods
add their houses to the vanilla pools in code: IE at mod init; Create: Dynamic Village, PneumaticCraft,
Farmer's Delight, Monobank, Wares and Chef's Delight in `ServerAboutToStartEvent`; Cloud Storage and
Domestication Innovation through a mixin. No jar scan can list those, so this system reads the live
pools in the game instead.

| File | Concern |
|---|---|
| `pool_config.json` | **Written by the game at every world load**, from the live pools after every mod has injected its pieces. Per pool, per piece: `original` (mod/vanilla weight, don't edit), `current` (**edit**: the weight), `enabled` (**edit**: false = removed). Your edits survive every rewrite. Edited pieces that disappear (mod removed) are kept with `"missing": true`. |
| `pool_tags.json` | **Category knobs**, same value syntax as `structure_tags.json` (ids, `*` globs, `#tags`, `!` excludes), applied to piece keys. Optional `"pools": [globs]` limits a tag to some pools (e.g. desert villages only). Multipliers multiply onto `current`. |
| `pool_tag_report.md` | Rewritten every load: errors/warnings, **every piece other mods injected into each pool** (the quickest way to find a mod's houses), before/after chances for every retuned pool, tag members. |
| patcherMod `worldgen/PoolTuner` | Does the discovery, merge and apply. |

Semantics: each time a street's jigsaw connector draws from a houses pool, a piece is picked with
chance `weight / pool total`. The weights are relative: lowering one house makes the others (and the
pool's `minecraft:empty` lot) more likely, but a village doesn't get fewer buildings. Fractional
results are fine, because the whole pool is scaled up (at most x10) to keep the ratios. Removing every
piece leaves a lone `minecraft:empty`, so nothing crashes (a start pool like that just generates
nothing; remove whole structures in `structure_config.json` instead). Any error in either file applies
**no** pool tuning (every pool stays at its mod weights) and is listed in the report.

Weights in this pack worth knowing: vanilla houses are 2-4 each. IE's 5 professions are 1 each. Create:
Dynamic Village's 4 houses are **20 each**, so they dominate villages. PneumaticCraft's mechanic house
uses its own config value (`addMechanicHouse`, 8) and Advanced Peripherals uses `villagerStructureWeight`
(10). `pool_config.json` shows what each mod actually injected.

Singleplayer detail: vanilla pools (and everything mods inject into them) are the same Java objects in
every world opened in one game session, and several mods append their pieces again on each world load.
PoolTuner remembers the first weights it saw per pool, so edits never compound and re-added duplicates
are dropped (vanilla behaviour would double Dynamic Village's weight on your second world).

Workflow: load any world once to generate `pool_config.json`, edit it or `pool_tags.json`, rejoin, and
read `pool_tag_report.md`. Only chunks generated afterwards are affected.

## Loot containers and Lootr

Lootr (`config/lootr-common.toml`) already turns every structure chest, barrel, trapped chest and
shulker box that has a loot table into a per-player container, pack-wide. It can't convert other
mods' containers. `loot_container_swaps.json` fixes that: a listed block that a structure template
places **with a loot table** is swapped for a vanilla container (facing carried over), which Lootr then
converts. It's applied through a mixin on Forge's `StructureTemplate.processBlockInfos`, which every
template placement uses (jigsaw or not). Containers without a loot table (player-placed, fixed
contents) are never touched.

Defaults: IE crates (the IE village houses) and Farmer's Delight cabinets/baskets (Chef's Delight houses)
become barrels, and PneumaticCraft reinforced chests (mechanic house) become chests.
`python3 tools/audit_loot_containers.py` writes `loot_container_audit.md`, which lists every loot
container in every template and its fate. Two kinds are left unconverted on purpose. Nullscape's phantom-hive
droppers are part of a mob nest. Monobank's bank blocks are that mod's bank mechanic, not loot.
Chests with fixed contents and no loot table can't become Lootr containers at all (nothing to roll per player).
