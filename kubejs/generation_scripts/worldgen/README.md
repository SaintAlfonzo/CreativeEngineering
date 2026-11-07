# Structure spawn rates: structure groups

One place to set how often every structure generates, vanilla or modded, and how far apart
structures of the same kind keep, instead of hunting through per-mod config toggles. KubeJS
ignores this `.md`. Full writeup + in-game tests: `feature_writeup/structure_tuning.md` (ST-).

## Applied at world load: no script to run after an edit

Everything in this folder is config, read at every world load (including world creation):

| Config | Applied by | Takes effect |
|---|---|---|
| `structure_groups.json` (rates) + `structure_tags.json` (group membership) + `structure_config.json` (baseline) | `kubejs/server_scripts/worldgen/structure_overrides.js` (`ServerEvents.highPriorityData`) + patcherMod `worldgen/StructureFrequencies` | rejoin the world; new chunks only |
| `biome_config.json` | `biome_overrides.js` -> patcherMod `worldgen/BiomeLayoutTuner` (Java solver) | new worlds only |
| `pool_config.json` + `pool_tags.json` | patcherMod `worldgen/PoolTuner` (`ServerAboutToStartEvent`, LOWEST) | rejoin the world; new chunks only |
| `loot_container_swaps.json` | patcherMod `worldgen/LootContainerSwaps` + `mixin/StructureTemplateLootSwapMixin` | rejoin the world; new chunks only |

Above these files sits `config/creativeengineering-common.toml` `[worldgen]` (also in Create's config screen): an on/off
switch for each system above plus ground matching, `[worldgen.debug] structureDensity` (every structure's rate times x,
for testing), the solvers' constants, ground matching and `/cdevplace`. Switching a system off never touches these files;
the game just uses the mods' own values while it's off. See `feature_writeup/structure_tuning.md` 3.9 and 3.10.

`highPriorityData` fires inside KubeJS's world-load resource wrap, before the game parses worldgen,
and its files go into KubeJS's top-priority virtual datapack (above `kubejs/data`, Terralith,
Nullscape). Nothing is written to `kubejs/data`. A config error applies nothing for that system
(mods' own defaults are used) and is logged as a KubeJS error. The `tools/*.py` scripts are only
needed after a mod update or a group membership change.

## The model (since 2026-09-27)

- **Group:** one shared structure_set, `creativeengineering:group/<name>`, holding every structure
  in the tag `group/<name>`. Members share its grid, so **no two members ever start closer than
  `separation + 1` chunks**, whatever mod they come from, and the grid allows at most one member per
  `spacing x spacing` chunks. Groups: `settlements`, `pillager_settlements` (kept 4 chunks off
  settlement grid slots), `ruins`, `surface_dungeons`, `witch_huts` (swamps only, 2026-10-12),
  `ocean_ruins`, `ocean_dungeons` (the monuments' own grid), `sky`, `boats`, `flotsam`, `remote`.
- **Per member:** `weight` decides who wins a slot where several members fit the biome (vanilla set
  weight). `frequency` (0..1) is the chance the winner really spawns; otherwise the slot stays
  **empty**, it does not fall through to another member, so lowering one never makes another more
  common. Where a member is the only one that fits the biome it gets every slot x its frequency.
  `enabled: false` removes it.
- **Standalone:** every structure in no group keeps its own set, listed under `standalone` by set id:
  `placement` holds only the fields you change (the rest is `original` in `structure_config.json`),
  and each structure has the same `frequency` / `enabled` / optional `weight`.
- A structure moved into a group leaves its original set. A set left with no live member is written
  with **no structures** and frequency 0: it can't generate, and `/locate` and explorer maps never
  search it (no hang).
- `frequency < 1` is not vanilla: the script hands the table to patcherMod, which rolls it when a
  structure is about to be stored (`mixin/ChunkGeneratorStructureFrequencyMixin`) and answers
  `/locate`'s "could it start here?" with the same roll (`mixin/StructureCheckFrequencyMixin`).
  Without that class (an older jar in `mods/`) the script applies **nothing**.
- **Meta structures** (2026-10-11): our own structures built from other structures and features (15:
  villages with towers, ruin clusters, sunken ruins, wrecks, rafts, pillager towers/forts/camps with siege weapons, a
  pillager fleet, a driftwood flotilla), defined in `meta_structures.json` and added to the datapack by the script only
  when patcherMod has `MetaStructure` and `MetaSlots`. A meta with an anchor takes a `share` of the slots its anchors
  already won in its group (its own group entry, weight 1, never generates), so no structure's rate changes. Who may
  take part: the `meta/*` tags in `structure_tags.json`, also added as real tags `creativeengineering:meta/*`.
  `/locate` finds a meta by its id and a structure inside one by the structure's id.
  Reference: `feature_writeup/structure_tuning.md` 3.16.
- **Placement priority** (2026-10-12): `priority` in `structure_groups.json` ranks groups (and the fortified towns);
  a structure whose pieces would overlap a higher-ranked one's never starts. A meta's `companions` start whole
  structures in chunks of their own around it (villages around a fortified town). Reference:
  `feature_writeup/structure_tuning.md` 3.22.
- **Replacements** (2026-10-12): a member with `"replaces": "<member id>", "share": 0.1` takes that share of the slots
  the named member wins and keeps, where both can stand, and never a slot by its own weight (keep it at 1). The two
  together are as common as the named one was. Used for the vanilla swamp hut, desert pyramid and jungle temple in their YUNG's versions' slots. Off with
  CEConfig `[worldgen.systems] structureReplacements`. Reference: `feature_writeup/structure_tuning.md` 3.18.

## Files

| File | Concern |
|---|---|
| `structure_groups.json` | **Every rate. Edit this.** `groups.<name>`: `placement` (spacing, separation, salt, optional spread_type / frequency / locate_offset / exclusion_zone) and `structures` (weight, frequency, enabled per member). `standalone.<set id>`: `placement` overrides and `structures`. |
| `structure_tags.json` | Named structure lists: `group/*` decides membership (a structure may be in one group), `mod/*` / `type/*` / `dungeon/*` classify. No multipliers. Values: ids, `*` globs, `#tags`, `!` excludes; an unknown id or tag is an error that applies nothing. |
| `biome_fits.json` | Biome fits: a named biome list per fit (`land/plains`, `land/oak`...) whose structure tag of the same name in `structure_tags.json` spawns there (plus parent fits' biomes) instead of in its mod's list. Biome-type families (plains, forest, taiga, snowy, desert...) and wood families (oak, spruce, acacia, sand, end...), plus `land/any` and `land/sky`: 29 fits, 163 structures (2026-10-12; the old `tower/*` fits were renamed into them). Staged at world load to patcherMod StructureBiomeFits; after editing, run `python3 tools/gen_structure_groups.py --fit-rates` (`fit_keep: true` on a member lets its weight rise to keep its density). See `feature_writeup/structure_tuning.md` 3.17 and the family tables in 7.2. |
| `meta_structures.json` | Meta structure definitions: structure fields + `anchor` + `share` + `parts` (see its `_readme`). Staged at world load; an anchored meta's rate is its `share` of its anchors' slots (its group entry stays weight 1). |
| `structure_config.json` | **Read-only baseline** from `tools/gen_structure_config.py`: every set as the mods/vanilla define it (`original`), members, biomes, `blocked` notes, `meta_structures`. |
| `structure_tag_report.md` | Rewritten by the game at every world load: warnings, each group's grid and members ("alone: 1 per N chunks"), standalone sets written, sets emptied into groups, coverage, tag members. On a config error it lists the errors. |
| `structure_group_estimates.md` | `python3 tools/gen_structure_groups.py --check`: expected structures per area for every structure (mod default vs now), from its biome pool and the real biome areas in `biome_config.json`. |
| `structure_group_migration.md` | The one-time comparison of the groups against the tuning they replaced (0 structures moved by more than 20%). `--migration-report` rebuilds it from `kubejs/archive/worldgen/pre_groups_2026-09-27/`. |
| `tools/gen_structure_groups.py` | Sync (default): adds groups / members / standalone sets missing from `structure_groups.json`, fitted to their mod defaults, removes stale standalone entries of structures that moved into a group, never changes an existing value; then the estimates. `--migrate` was the one-time conversion. |
| `tools/structure_tuning_sim.js` | `node tools/structure_tuning_sim.js [report.md] [out/]`: runs the real script in node; prints the summary, warnings and coverage the game would log; `out/` gets every set json + `frequencies.json`. |
| `tools/test_structure_groups.sh` | node sim + the same script in the pack's own Rhino (must match node byte for byte) + the old-jar refusal + the Java logic test of `StructureFrequencies`. |
| `tools/check_structure_live.py` | After `/dumpstructuregen` in game: live sets missing from the baseline, baseline sets not live, and every set the script writes vs the live spacing / separation / count / members. |

## Workflow

1. **Change a rate:** edit `structure_groups.json`: a member's `frequency` (rarer), `weight` (wins
   more of the slots it shares), `enabled`; a group's `spacing` (overall density) or `separation`
   (minimum gap); a standalone set's `placement` or member `frequency`.
2. **Move a structure between groups** (or out of them): edit the `group/*` tags, then
   `python3 tools/gen_structure_groups.py` (fits the new member, cleans stale entries).
3. **Check offline:** `node tools/structure_tuning_sim.js` (errors) and
   `python3 tools/gen_structure_groups.py --check` (expected rates per structure).
4. **Apply:** rejoin the world. The log shows a `[structure-tuning]` line and a
   `[structure-frequency] N structure(s) in M set(s)` line; `structure_tag_report.md` is rewritten.
   Only chunks generated afterwards are affected.
5. **After a mod update:** `python3 tools/gen_structure_config.py` (baseline), then
   `python3 tools/gen_structure_groups.py` (entries for new structures), then check the new
   structures' tags: new mods need a `mod/*` tag and, if they belong in a group, a group tag.

## How the migration kept the old rates (2026-09-27)

The old model tuned each set separately (`current` edits + tag multipliers, members with different
multipliers split into separate grids), so sets of different mods piled up independently. The
migration measured each structure's old density per biome (its biome pool from every jar's tags +
`biome_merges.json` at the time, biome areas from the climate sample in `biome_config.json`) and fitted each
group's weights and frequencies (least squares on log density, every structure counted equally). Each
group's grid is sized for its most crowded biome, so nothing gets capped; the gain is the gap. Result
over today's biome pools: every structure between x0.89 and x1.13 of its old rate, 135 of 164 within
5%. Standalone sets that the old model had split got one fitted grid too. The archived originals are
in `kubejs/archive/worldgen/pre_groups_2026-09-27/`.

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
- **YUNG's stops three structures in code**, whatever their set says: the vanilla nether fortress
  ("Disable Vanilla Nether Fortresses" = true; the desert pyramid, jungle temple and witch hut switches are false
  since 2026-10-12, so those three can be replacements), the vanilla stronghold (Better Strongholds, always) and Better Dungeons' small nether
  dungeon ("Enable Small Nether Dungeons" = false). The scanner writes a read-only `blocked` note
  on those entries and the report lists them; tuning them does nothing.
- **Other mods' own frequency configs are left at defaults on purpose.** Towns & Towers'
  `structure_rarity.json5` matches its jar sets, and this framework's overrides win (checked
  against the live registry, 2026-09-27).
- **HopoBetterUnderwaterRuins** (added 2026-09-27) moves both vanilla shipwrecks onto beaches and
  rivers and halves their set's spacing; its 3 ruins are in `group/ocean_ruins`, its fossils standalone.
  **Revamped Shipwrecks** (added later that day) also overrides `minecraft:shipwrecks` and wins in
  game (checked live): the vanilla set is empty and `minecraft:shipwreck(_beached)` are in no set.
  Its own two jigsaw wrecks use the same biome tags (so Hopo's beach/river extension still applies)
  and stand in for the vanilla ones in `type/shipwreck` / `group/ocean_ruins`.
- **Ported structure mods:** MCreator one-building jars that crash Forge 43.3.9 (`<init>()`) or are
  pure data are converted to `kubejs/data/<ns>/` by `tools/port_structure_jars.py` (1.20 blocks
  remapped, unknown items dropped, pool entries for missing templates dropped, jar moved to
  `disabledmods/archive/`). Their sets show `source: "kubejs/data"` in the baseline. Philip's Ruins has `"Enable Configs" = false`, so its
  separation/density/toggles are inactive. Village-house weights (Chef's Delight, Wares,
  Monobank, Domestication Innovation, Advanced Peripherals, PneumaticCraft, Cloud Storage) come
  from those mods' configs and show up as `original` in `pool_config.json`. The full list is in
  `feature_writeup/structure_tuning.md`.
- **One modded set is registered in Java:** `ae2:meteorite` (spacing 32 / separation 8, only
  while `config/ae2/common.json` `generateMeteorites` is true). It's in the scanner's
  `CODE_REGISTERED_SETS` table. `tools/check_structure_live.py` finds any new ones.
  - add to sky groups tags, reduce frequency through sky tuning
- **313 raw structure jsons exist across mod jars but only 200 are referenced by any
  structure_set** — the rest are unused template/leftover files that wouldn't naturally
  generate anyway, so they're not tracked here.

## Biome pool merges (retired 2026-10-11)

`biome_merges.json` used to widen structures' spawn pools by adding biomes to the additive `has_structure/*` biome
tags they reference (`biome_merges.js`, CEConfig `biomeMerges`). Retired 2026-10-11 in favour of biome fits
(`biome_fits.json`), which replace one structure's list instead of widening a shared tag. The files, the proposal
analyzer and the builder were deleted from the tree (still in git history); see
`feature_writeup/structure_tuning.md` 7.2.

## Wild crops (which biomes each wild crop and fruit tree spawns in)

Separate from structures, and part of Creative Delight since 2026-10-25 (`feature_writeup/creative_delight.md` 24):
`biome_traits.json` here gives every overworld biome a set of traits (climate, wet/dry, ground, shape); CD's
`wild_crops.json` gives each wild crop a rule over them, plus the mods' biome modifiers it replaces. Reference, choices
and the `CR-47`.. tests: `feature_writeup/crops.md` section 2.8.

| File | Concern |
|---|---|
| `biome_traits.json` | **Edit for a new biome.** Traits per biome; also read by `mushroom_spawns.json`, `wild_flowers.json`, `bee_nests.json`, `mob_spawns.json` and their generators |
| `kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.json` | **Edit this for crops.** `crops` (rule, features, modifiers to switch off, vanilla patches to remove). Copied into the CD jar |
| `kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.js` | At world load: biome tag `creativedelight:wild_crops/<crop>`, mod modifiers -> `forge:none`, one add modifier per crop, one remove modifier for the vanilla patches (highPriorityData). Checks the live tags on `ServerEvents.loaded`. Machinery: the pack's `kubejs/server_scripts/worldgen/_biome_rules.js` |
| `kubejs/data/creativedelight/worldgen/*_feature/wild/` | CD's own features (`creativedelight:wild/*`) where a mod's ground test would block a biome, and copies of the vanilla patches |
| `tools/gen_crop_worldgen_table.py` | Evaluates the same rules -> `feature_writeup/crops_by_biome.md` (biome lists, before/after density, coverage) |
| `tools/test_wild_crops.sh` | Offline: generator + the real script in Rhino, tags must match |

Workflow: edit `wild_crops.json`, run the generator to see the lists, then rejoin (biome modifiers are read at world
load, not on `/reload`). Only new chunks change. `creativedelight-common.toml [worldgen] wildCrops = false` turns it
off. A new biome mod: its biomes show up as unclassified in the generator and the log; add them to `biome_traits.json`.

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
| `kubejs/server_scripts/_lib/dump_climate.js` | `/dumpclimate [columns=3000] [radius=20000]` in-world -> `kubejs/generation_scripts/worldgen/climate/<ns>__<dim>.json.gz` (compact JSON, gzipped, ships with the pack; read with `zcat`): climate noise at the surface + 4 underground depths per column, plus the live biome at each point. Pure noise evaluation, no chunk loading. |
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
Chests with fixed contents and no loot table aren't converted by Lootr. For the structures ported into `kubejs/data`,
`python3 tools/lootr_fixed_containers.py` fixes that: each hand-filled chest/trapped chest/barrel gets a generated loot table
(`<ns>:chests/fixed/<hash>`, one pool per stack, count and item NBT kept, slots scatter) set on its template, so Lootr
converts it. Idempotent; re-run after every `port_structure_jars.py` run. Same-contents jar templates under `mods/` are not
touched (listed in the audit's fixed-contents table).
