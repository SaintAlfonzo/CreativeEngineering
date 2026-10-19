# M9: Phase 9, flat world `/ceplace` (8.0.9)

2026-10-19. Live notes: [09_phase_9.scratch.md](09_phase_9.scratch.md). Result lines are inline in
[structure_tuning.md](../../structure_tuning.md) section 8, with a summary under "8.0.9 Phase 9".

## 1. Setup

- **Server:** the same scratch server (patcherMod 1.0.7 jar, the same `MassPlacer` as 1.0.8), seed 8675309,
  `generate-structures=false`.
- **The flat world took four tries** (3.2):

  | World | How it was set up | Result |
  |---|---|---|
  | `st_9` | `level-type=minecraft:flat` | Normal terrain |
  | `st_9b` | Explicit flat `generator-settings` | Normal terrain |
  | `st_9c` | A world datapack with a flat overworld | Normal terrain |
  | `st_9d` | The same datapack plus `biomeLayout = false` | Flat |

  ST-40 ran on both `st_9` (hills) and `st_9d` (flat).
- **Checks:** the `[ceplace]` log lines (one box per structure) and the region files: signs, entities and blocks.

## 2. Results

| Test | Result | Evidence |
|---|---|---|
| ST-39 | PARTLY | The structures preview is exact: `56 structures in 7 row(s)`, with camp 5, fort 9, ship 2, shipwreck 1, tower 15, village 23 and 1 with no `type/*` tag (`mimic_desert`). The templates preview throws (3.1) |
| ST-40 | PASSED (scratch part) | 55 of 56 placed, on hills (3.5 min) and on the flat world (46 s). The one failure is listed with its reason. Signs, no overlaps, outposts with illagers |
| ST-41 | FAILED | `/ceplace templates ...` throws "An unexpected error occurred" every time, in either world (3.1) |
| ST-42 | PASSED | `by group @philipsruins @hopo`: 33 in 5 rows. The status line names the structure in progress and its chunk counts. `stop` logs `stopped at 18/33`, and nothing is placed after it |

ST-40 details:
- **The failure:** `towns_and_towers:village_ocean: did not generate here (the structure turned this terrain down:
  height, water or room)`. It is an ocean village on dry land, the same as on 09-27.
- **Overlaps:** none among the 55 boxes.
- **Signs:** one in front of each of the 7 rows ("row type/camp 5 items" ...), and one with the id at the front
  corner of all 55 structures.
- **Mobs:** all 29 outposts hold illagers, and 22 of the 24 villages hold villagers. The wandering trader camp has
  its 2 traders and llamas instead. T&T `village_forest` has no villagers and no wood: it builds from the
  `kaisyn:village/forest_ruins` pools, an abandoned ruin by design.
- **Still a client check:** walking the rows to see that villages are complete.

## 3. Bugs and findings

### 3.1 `/ceplace templates` can't list templates (ST-41; MassPlacer, older code)

- **Symptom:** vanilla `StructureTemplateManager.listTemplates()` throws `StringIndexOutOfBoundsException: begin 11,
  end 10, length 14`, in its listResources lambda (it cuts `structures/` off a path that has no slash). A
  scratch-only KubeJS probe found it. The command's preview, placing and suggestions all go through
  `MassPlacer.templateIds`, which calls `listTemplates()`, so the whole templates mode is dead.
- **Cause:** a 14-character path, `structures.nbt`, reaches it at a namespace root.
- **Where it isn't:** any mod jar (including nested jars), `kubejs/data`, the Vinery donor jars or the two zip packs.
  Listing each of the 418 packs one by one returns only proper `structures/...` paths.
- **Where it is:** the merged `ResourceManager.listResources` call fails with an Error that JS can't catch. So the
  bad entry comes in at the resource-manager level. The mixins there are Moonlight's
  `MultiPackResourceManagerMixin`, MoreJS's `StructureManagerMixin`, KubeJS's resource injection and MES's
  accessor. I didn't pin down which one.
- **Fix idea:** `MassPlacer.templateIds` lists the templates itself: per pack, keeping only paths under
  `structures/`, plus the generated folder. It then no longer depends on `listTemplates()`.
- **Background:** the 09-27 notes say the templates mode had never been run.

### 3.2 No flat world in this pack

- **Symptom:** with `level-type=minecraft:flat`, with explicit flat settings, or with a world datapack defining a
  flat overworld, the overworld is created as `minecraft:noise`. `/ceplace` says so ("Not a flat world: everything
  is built at this spot's height"), so the rows climb over hills.
- **Cause 1:** patcherMod's `BiomeLayoutTuner` (`biome_overrides.js`, `[worldgen.systems] biomeLayout`) puts a full
  `minecraft:dimension/overworld.json` into KubeJS's top-priority virtual datapack. That ranks above every world
  datapack and every preset.
- **Cause 2:** Terralith and Tectonic each ship their own `data/minecraft/dimension/overworld.json`. These are mod
  packs, so a world datapack outranks them.
- **Recipe that works** (verified, `st_9d`):
  1. Set `biomeLayout = false`.
  2. Put a world datapack with a flat overworld dimension in `<world>/datapacks/` before the first start (the
     scratchpad's `st_9c/datapacks/flat_overworld`).
  3. Set `biomeLayout` back afterwards: it only matters when a world is created.
- **Also in single player:** the virtual pack is the same there, so a "Superflat" world in your client should come
  out the same way. That is worth knowing for the [game] tests that say "creative flat world". I didn't check it in
  a client.

## 4. Values changed

All scratch-only and restored: `[worldgen.systems] biomeLayout` (`st_9d`), the flat settings in `server.properties`,
the world datapack (inside the scratch worlds), and the probe script (removed).

## 5. Follow-ups for you

*Resolved 2026-10-19 ([11_fixes_1_0_8.md](11_fixes_1_0_8.md)): 3.1 `MassPlacer.templateIds` lists templates per pack; 3.2 `RegistryLoaderPresetOverworldMixin` keeps a flat (or any non-multi-noise) preset overworld, so `level-type=minecraft:flat` and Superflat work with `biomeLayout` on and no world datapack.*


1. **3.1:** fix `MassPlacer.templateIds`, then rerun ST-41 and the second half of ST-39.
2. **3.2:** decide whether `/ceplace` testing needs a flat world. If so, add the recipe to the `/ceplace` docs
   (structure_tuning 3.x) or give `biomeLayout` a "skip flat presets" rule.
3. **ST-40 in game:** walk the rows (villages complete, outposts manned).
4. **Next phase (10):** JFR profiling (LO-10).
