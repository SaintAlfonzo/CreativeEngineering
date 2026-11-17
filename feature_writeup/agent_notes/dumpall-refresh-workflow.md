# dumpall-refresh-workflow

> After adding mods - which generators consume which /dump* outputs, in what order, and the hand steps each needs

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Used 2026-10-02 for the 1.0.4 mods (record of the run: feature_writeup/plan_1.0.4.md section 5b).

- `/dumpall` (kubejs/server_scripts/_lib/dump_all.js) now includes `/dumpfood`. `/dumpservermods` was removed 2026-10-27: copy
  server jars with `tools/dump_server_mods.py` (fails until every new jar has a mod_sides.md row). `/dumpnames` only works in single player.
- Order that worked: gen_structure_config -> build_biome_merges -> gen_biome_config -> gen_structure_groups ->
  gen_structure_biome_table / analyze_structure_biome_merges; gen_loot_config -> audit_loot_containers -> loot_census
  (+ `node tools/loot_tuning_sim.js`); missing_names, jei_coverage, stack_sizes, gen_effect_descriptions,
  gen_pack_manifest, blockrunner_scan (+ blockrunner_tag_sim.js); food_catalog (after /dumpfood); placeable_catalog (after /dumpplaceable);
  `gen_compost_values.py --merge`.
- Since 2026-10-27 every Python step above that still exists (build/analyze_*biome_merges are gone) except
  gen_compost_values is a /cdevgen entry
  (`/cdevgen run all dumps`); food_catalog runs before the food reports, which re-read item_dict.js.
- Hand steps: move food_catalog's misfiled new foods ([food-tuning-system](food-tuning-system.md) precedents); tier new walkable blocks in
  blocktags.js ([blockrunner-speed-tiers](blockrunner-speed-tiers.md)); add jei_coverage UNCOVERED entries it names; map new mods in item_dict
  (memory `item-dict-mechanics`); new crops into crops.md ([crops-harvest-writeup](crops-harvest-writeup.md)); update each writeup's status + history.
- /dumpfood can run on a scratch server when the client is closed (memory `scratch-server-verify`); the same boot proves the
  edited scripts load (`[food types]`, `[compost]`, `[loot-tuning]` log lines).
- Gotcha: `pgrep -f <pattern>` in a wait loop matches its own bash command line; use `pgrep -x java`.

**Why:** the user asked to keep every tuning function and writeup current with the latest mods after dumps.
**How to apply:** follow this list after any mod add/remove; the dumps and tools never delete stale per-mod files.
