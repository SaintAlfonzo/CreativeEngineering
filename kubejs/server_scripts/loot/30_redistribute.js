// Chest loot — REDISTRIBUTE.  Items you want to appear in MORE places than they do now.
//
// Use global.LOOT.tiers.* (00_lib.js) as reusable add-pools, or
// build entries inline with L.entry({ item, weight, count, chance }).
//
// Scope each addition with a builder condition so it doesn't land in EVERY chest:
//   .anyStructure(['#minecraft:village'], false)   structure tag / id list, matchExact bool
//   .anyBiome('#minecraft:is_overworld')
//   .anyDimension('minecraft:overworld')
//   or just target a set of tables with L.tables(event, /regex/).
//
// Reload with /reload.

LootJS.modifiers(event => {
  const L = global.LOOT

  // ── tier pools into broad table sets (examples — uncomment & edit) ───────

  // sprinkle the "common" tier into every village chest
  // if (L.tiers.common.length) {
  //   L.addPool(
  //     L.chestType(event).anyStructure(['#minecraft:village'], false),
  //     L.tiers.common, 'redistribute/village_common'
  //   ).logName('CE/loot/redistribute/village_common')
  // }

  // put the pack's signature material into every "dungeon-ish" chest
  // L.tables(event, [/:chests?\/.*dungeon/, 'minecraft:chests/simple_dungeon'])
  //   .addWeightedLoot(L.entry({ item: 'creativeengineering:some_item', weight: 6, count: 2 }, 'redistribute/sig'))
  //   .logName('CE/loot/redistribute/signature')

  // guarantee at least one map fragment in every stronghold library
  // L.tables(event, 'minecraft:chests/stronghold_library')
  //   .addLoot(L.entry({ item: 'minecraft:paper', count: 3 }, 'redistribute/library_paper'))
  //   .logName('CE/loot/redistribute/library_paper')
})
