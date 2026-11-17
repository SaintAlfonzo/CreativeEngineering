# dead-structure-locate-hang

> Frequency-0 structure sets hang explorer-map/locate searches in 1.19.2; fixed by Creative Common ChunkGeneratorLocateMixin + kubejs loot/06_dead_maps.js; structure groups also empty dead sets so searches never see them; status in feature_writeup/locate.md

Moved from agent memory 2026-11-16 (last edited there 2026-09-27). Repo status docs win over any status line here.

2026-09-27 dedicated-server watchdog crash (debug/crash-2026-09-27_15.32.34-server.txt): a Monobank vault's
first tick unpacked `monobank:monobank/village/<biome>`, which rolls an exploration_map to `minecraft:ruined_portal`.
We disable all ruined portals (structure_overrides.js writes the set at frequency 0), and vanilla
`ChunkGenerator#findNearestMapStructure` (m_223037_) ignores frequency, so it scanned ~10k candidates for 60 s.

**Fix (since [structure-groups](structure-groups.md) emptied sets have no members at all):**
- `mixin/ChunkGeneratorLocateMixin` @Inject RETURN on private `getPlacementsForStructure` (m_223138_, sole caller is
  findNearestMapStructure) -> `worldgen/DeadPlacementFilter` drops placements with `frequency` (f_227024_) <= 0.
  Covers maps, /locate, cartographer trades, dolphins.
- `kubejs/server_scripts/loot/06_dead_maps.js`: pre-roll genericLootTables strip of exploration_map entries whose
  destination is in DEAD_DESTINATIONS, over an explicit TABLES list (5 Monobank tables). Node-tested.

**How to apply:** when disabling another whole structure set, grep jars for `exploration_map` destinations and extend
DEAD_DESTINATIONS/TABLES. Tests ST-15/ST-16 in feature_writeup/structure_tuning.md.
Server copy lives in Trash (see [creengserver-dedicated-server](creengserver-dedicated-server.md)) and still has jar 1.0.0.
Related: [structure-gen-framework](structure-gen-framework.md), [loot-system](loot-system.md), [patchermod-coremod](patchermod-coremod.md).
