# water-biome-rules-ipo-driftwood

> biome_rules (water allow-list via patcherMod StructureBiomeRules), IPO as 1-in-8 outpost tower + IPO template-name fix, driftwood placed features x0.25; status in feature_writeup/structure_tuning.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Built 2026-10-03 on request. Writeup: feature_writeup/structure_tuning.md 3.12, tests ST-44..48; plan_1.0.4.md row WG.

- **Biome rules:** `biome_rules.water` in structure_groups.json = {biomes: #is_ocean, #is_river, #c:river; allow: "#water_ok"} (tag in structure_tags.json; includes sky + underground on purpose, user may want those stripped). structure_overrides.js stages to patcherMod `worldgen/StructureBiomeRules` (default-DENY for every registered structure not allowed, incl. future mods); `ChunkGeneratorStructureBiomeRulesMixin` = @ModifyVariable STORE index 14 in tryGenerateStructure (m_223104_) narrowing the biome predicate. CEConfig [worldgen.systems] structureBiomeRules. Python Pack.apply_biome_rules mirrors it (estimates, structures_by_biome.md). Live log line `[structure-biome-rules] water: ... kept out:` (25: 8 Mysticrift towers, terralith:spire, supplementaries:way_sign, zpointers, emptied vanilla shipwrecks).
- **IPO:** IPO overwrote minecraft base_plate.nbt (its plate only links to ipo towers). kubejs/data restores the vanilla plate, copies IPO's to ipo:pillager_outpost/base_plate, base_plates pool 7:1. pool_config.json entries were hand-edited to match: PoolTuner KEEPS an existing `current`, so changing a pool's weights in data also needs pool_config `current` updated or the old value wins.
- IPO v5 pools name `damaged_overgrown` templates that ship as `overgrown_damaged`; fixed pool copies in kubejs/data/ipo (delete if IPO fixes it).
- **Driftwood** = Upgrade Aquatic placed features (Java-registered); kubejs/data/upgrade_aquatic/worldgen/placed_feature overrides at 0.25x.

Related: [structure-groups](structure-groups.md), [jigsaw-pool-tuning](jigsaw-pool-tuning.md), memory `scratch-server-verify`, [release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md).
