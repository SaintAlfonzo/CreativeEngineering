# worldgen-config-cdevplace

> CEConfig [worldgen] (per-system switches, debug structureDensity, solver constants, ground_match, mass_place) read via Creative Common WorldgenConfig; /cdevplace lays structures/templates out in rows for flat-world testing; status in feature_writeup/structure_tuning.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Built 2026-09-27 on request: "as many worldgen tuning parameters as possible in config", a debug flat multiplier on every
structure frequency, and a way to generate e.g. all of Towns & Towers in rows by building type in a flat world.

- **Config**: CEConfig.WORLDGEN -> toml [worldgen].{systems, debug, pools, biome_solver, ground_match(.sampling), mass_place}.
  Every consumer reads it through worldgen/WorldgenConfig: static getters that fall back to the defaults (= the old
  constants) if the config isn't loaded. The KubeJS scripts call the same class (Java.loadClass, try/catch -> defaults).
  Per-structure tables stay in the JSON files (the user wasn't asked to move them; offer it if they want).
- **Density**: `scalePlacement` in structure_overrides.js. m<1 -> set frequency*m (exact). m>1 -> spacing/separation
  floor(/sqrt m) + frequency absorbs the rounding; capped at spacing = separation + 1 and reported. Rings -> count*m (max
  4095). legacy_type_1 -> default when the frequency changes. With m != 1 every live set is written (127 vs 97).
  structureGroups=false -> groups/standalone ignored, tag errors downgraded to warnings.
- **/cdevplace** (worldgen/MassPlacer): generate like /place structure (biome -> true), move pieces (StructurePiece.move,
  overridden by pool/template pieces) onto the row cursor, placeInChunk per chunk. Rows = the smallest type/* tag (so
  outposts go to camp/fort/tower, not enemy_building). Signs face -Z. Templates mode: JigsawReplacementProcessor, rows by
  folder. T&T templates live under the `kaisyn` namespace (742). T&T by type = camp 5, fort 9, ship 2, shipwreck 1,
  tower 15, village 23, untagged 1.
- **Tests**: tools/test_worldgen_tools.sh (GroundMatchTest 37, MassPlacerTest 30, structure_density_check.js 244);
  test_structure_groups.sh now also checks node/Rhino parity at density 4 / 0.3 / groups off. Sim flags --density= --groups-off.
- Writeup feature_writeup/structure_tuning.md 3.9, 3.10, ST-33..42. The CreEngServer jar is not updated.

Related: [structure-groups](structure-groups.md), [structure-ground-match](structure-ground-match.md), [creative-common-coremod](creative-common-coremod.md), memory `kubejs-headless-rhino-harness`.
- **Watchdog lesson (2026-09-27):** the first /cdevplace generated a whole structure's chunks in one tick; on real terrain a 90-chunk village took >60 s and the dedicated-server watchdog killed the server. Now a per-item state machine (lay out / load one chunk with a 1200-tick TicketType / place one chunk / label) runs until [worldgen.mass_place] tickBudgetMs (40) per tick; perTick is gone. Rerun: 55/56 T&T placed, village_ocean refuses land. Any future bulk worldgen command must be split the same way.
- 2026-10-19: templates mode fixed (MassPlacer.templateIds lists per pack + generated/, vanilla listTemplates threw on a merged `structures.nbt` path). Flat worlds now work with biomeLayout on: 1.19.2 dataPackAwareCodec lets any datapack dimension/overworld.json replace the preset; RegistryLoaderPresetOverworldMixin (m_206762_) puts back a non-multi-noise overworld. ST-41 scratch part passed (17/17 at y -61).
