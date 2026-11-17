# vegetation-worldgen-system

> Wild crop placement, owned by Creative Delight since 2026-10-25: rules + applier in kubejs/data/creativedelight/kubejs/_worldgen/, biome traits in the pack's biome_traits.json; crops.md 2.8, creative_delight.md 24

Moved from agent memory 2026-11-16 (last edited there 2026-10-03). Repo status docs win over any status line here.

**Moved into Creative Delight 2026-10-25 (owner: all rules, switch in CDConfig, ids renamed).** Edit
`kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.json` (crops) and `kubejs/generation_scripts/worldgen/biome_traits.json`
(traits, shared with mushrooms/flowers/bees/mob spawns). Ids `creativedelight:wild_crops/<key>`, features `creativedelight:wild/*`
(in `kubejs/data/creativedelight/worldgen/`, copied into the jar by CD's `dataTrees`), switch `creativedelight-common.toml
[worldgen] wildCrops`. Gotcha: KubeJS JsonIO reads files only, so the script falls back to the jar copy via `CDResources.text`.
The paths and ids in the history below are the pre-move ones.

2026-10-03 step 1 (reference): `feature_writeup/crops.md` §2.7 (mods' default spawns, V1-V12) +
`tools/gen_crop_worldgen_table.py` -> `feature_writeup/crops_by_biome.md`.

2026-10-03 step 2 (BUILT from the user's per-crop spec, crops.md §2.8, V13-V20, CR-47..58):
- `kubejs/generation_scripts/worldgen/wild_crops.json`: traits for all 146 overworld biomes (oceans/caves marked,
  never match) + 46 crop rules (`where` clauses OR'ed, traits AND'ed, `!x`, add/except), `disable_modifiers`,
  `remove_features`.
- `kubejs/server_scripts/worldgen/wild_crops.js`: tag `creativeengineering:wild_crops/<key>` via global.tags;
  highPriorityData writes `forge:none` over each mod modifier and one `forge:add_features` per crop; vanilla
  patches listed in Terralith biome files are removed everywhere by ONE `forge:remove_features` and re-added as
  pack copies (`creativeengineering:wild/*`), because Forge removes AFTER adding (re-adding the same id would be
  undone; adding a duplicate risks the feature-order cycle crash). Skips features of unloaded mods (unknown id =
  world won't load). ServerEvents.loaded verifies tags + lists unclassified overworld biomes.
- Pack JSON features where a mod's Java ground test blocks the spec: FD `farmersdelight:wild_crop` codec is fully
  JSON (cabbage/beet on sand|dirt, colonies on mushroom_grow_block|dirt with MOTION_BLOCKING_NO_LEAVES), hamimelon
  on sand|terracotta, cocoa = vanilla `minecraft:jungle_tree` 1 in 3 chunks.
- Side changes: `loot/08_leaf_apples.js` (oak + dark oak, pre-roll genericLootTables, generic event uses full
  table ids), AM `bananasDropFromLeaves = false` (configs_edited backed up), FR Basalt Deltas coffee off.
- CEConfig `[worldgen.systems] wildCrops` + `WorldgenConfig.wildCropsEnabled()`: patcherMod 1.0.4 rebuilt
  (md5 09733ca8); scripts default to on without it.
- Generator evaluates the same rules, prints before/after density (crops_by_biome.md §1) and coverage;
  `tools/test_wild_crops.sh` runs the real script in Rhino (tags must equal the generator's) - passes.

Open for the user: densities shifted hard (sugar cane x0.06, pumpkin x0.19, mint x11, strawberries x7.6) since mod
rates were kept; FR coffee has no source left; pineapple still sand-only; "orchards" had no biome (used skylands
autumn); rubberwood leaves still drop apples; apple + banana merges later.

**How to apply:** edit wild_crops.json, never the mods' modifiers; rerun the generator + test script after edits;
a new biome mod needs its biomes classified. See [crops-harvest-writeup](crops-harvest-writeup.md), [structure-gen-framework](structure-gen-framework.md),
memory `kubejs-headless-rhino-harness`, memory `configs-edited-backup`.
