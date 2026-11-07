# M9 scratchpad: Phase 9 (8.0.9, flat world /cdevplace)

## State (keep current)

- Constraints (user 2026-10-14): correctness only; ignore rates.
- Tests: ST-39 (preview), ST-41 (templates), ST-42 (status and stop), ST-40 (all of Towns & Towers in rows; walking the
  rows = [game]).
- Server: M7/M8 scratch server, patcherMod 1.0.7 jar (worldgen = 1.0.8). World st_9: level-type flat,
  generate-structures false, seed 8675309. Toml = base with density 1.0 and fill logs CANCELLED (no worldgen here).
- Running: nothing; Phase 9 done 07:42. Next: writeup 09_phase_9.md.

## Log (append-only)

- 07:03:50 st_9 Done (46.5 s).
- ST-39: `cdevplace preview structures @towns_and_towers` -> "56 structures in 7 row(s) for @towns_and_towers (rows:
  type)"; rows type/camp 5, type/fort 9, type/ship 2, type/shipwreck 1, type/tower 15, type/village 23, (no type/* tag)
  1 (towns_and_towers:mimic_desert): exactly as expected.
  `cdevplace preview templates kaisyn:village/*/houses/*` -> "An unexpected error occurred trying to execute that
  command" (twice; vanilla hides the stack below DEBUG).
- Cause hunt (srv-only probe kubejs/server_scripts/zz_scratch_tpl.js, removed after; copy in scratchpad):
  server.getStructureManager().listTemplates() throws java.lang.StringIndexOutOfBoundsException: begin 11, end 10,
  length 14 at StructureTemplateManager.m_230433_ (line 129, the listResources map lambda: path.substring(11, len-4)).
  A path of length 14 = "structures.nbt" at a namespace root reaches that lambda. Not in any mods/ jar (incl. nested
  jars 2 deep), kubejs/data, the vinery_upstream jars, the zip packs (simplehats, T&T waystones patch). All 418 packs
  listed one by one (pack.getResources(SERVER_DATA, ns, "structures", any)): no path outside "structures/". The merged
  rm.listResources("structures", ...) call throws something JS try/catch cannot catch (an Error). Mixins on the
  resource/template managers: moonlight MultiPackResourceManagerMixin, morejs StructureManagerMixin, kubejs
  inject_resources (MinecraftServerMixin, WorldLoaderPackConfigMixin), mes NamespaceResourceManagerAccessor. Not
  pinned further. ST-41 FAILED (the templates mode can't list anything); 09-27 notes say templates were never run.
- 07:23:50 ST-42: `cdevplace structures by group @philipsruins @hopo` -> "33 structures in 5 row(s) ... (rows: group)"
  (nether_ruins 3, ocean_ruins 6, ruins 13, surface_dungeons 1, no group: standalone 10); `cdevplace status` (07:24:02) ->
  "11/33 started, 10 placed, 0 failed, row 3/5, now philipsruins:ancient_towers (chunks loaded 2/2, placed 0/2)";
  `cdevplace stop` -> "stopped at 18/33" (07:24:05.9); no [cdevplace] line after it (checked 07:24:52). PASSED.
- 07:25 ST-40 on st_9: `execute positioned 2000 0 2000 run cdevplace structures @towns_and_towers` -> 07:28:39 "done: 55
  of 56 placed in x 2000..4692, z 2003..2482, 1 failed: [towns_and_towers:village_ocean: did not generate here (the
  structure turned this terrain down: height, water or room)]". Region check (st40.log + region/entities): 0 box
  overlaps among the 55; 7 row signs at x 1996 ("row type/camp 5 items" ... "row (no type/* tag) 1 item"); an id sign
  at the front corner of all 55; all 29 outposts hold illagers.
- st_9 was NOT flat: level.dat overworld generator minecraft:noise (generate_features 0 did apply). /cdevplace itself
  said "Not a flat world: everything is built at this spot's height" (rows at y 147-204 on hills). Retries: st_9b with
  explicit flat generator-settings JSON -> noise; st_9c with a world datapack datapacks/flat_overworld defining
  data/minecraft/dimension/overworld.json as minecraft:flat (enabled as file/flat_overworld) -> still noise. Cause:
  patcherMod BiomeLayoutTuner (biome_overrides.js, [worldgen.systems] biomeLayout) writes a full
  minecraft:dimension/overworld.json (noise, tuned biome list) into KubeJS's top-priority virtual datapack, above every
  world datapack and preset; Terralith and Tectonic also ship data/minecraft/dimension/overworld.json (mod packs, below
  file packs). st_9d: biomeLayout = false (srv toml) + the world datapack -> minecraft:flat. Recipe for a flat test
  world in this pack: biomeLayout off + a world datapack with a flat overworld dimension (client too, presumably).
- 07:40 ST-40 on st_9d (flat): `cdevplace structures @towns_and_towers` from 0 0 -> 07:41:21 "done: 55 of 56 placed in x
  0..2760, z 3..535", same village_ocean failure; 46 s (terrain run 3.5 min). 0 box overlaps; 55/55 id signs, 7 row
  signs; 29/29 outposts with illagers; 22 of 24 villages with villagers: the wandering trader camp has 2 wandering
  traders + 2 trader llamas (fine), T&T village_forest has no entities and no wood = kaisyn:village/forest_ruins pools
  (an abandoned ruin by design). ST-41 templates preview on flat: same "unexpected error".
- 07:42 stopped; srv toml = base, server.properties = M8 copy.

## Results

| Test | Result | Evidence |
|---|---|---|
| ST-39 | PARTLY | Structures preview exact (56 in 7 rows, per-type counts as written); templates preview throws |
| ST-40 | PASSED (scratch part) | 55/56 on terrain and on a real flat world; village_ocean refused with a reason; signs, no overlaps, illagers |
| ST-41 | FAILED | `/cdevplace templates` throws: vanilla listTemplates StringIndexOutOfBounds on a "structures.nbt" path |
| ST-42 | PASSED | status names the structure and chunk counts; stop at 18/33, nothing after |

## Open issues

1. /cdevplace templates broken (ST-41, ST-39 second half): fix idea: MassPlacer.templateIds lists templates itself
   (per pack, as the probe did, keeping only paths under "structures/") instead of StructureTemplateManager.listTemplates.
2. No flat world while biomeLayout is on (and Terralith/Tectonic override the preset): recipe above.
