// requires: creativeworldgen
// Chest loot — CONTEXT-SCOPED ADDITIONS (LootJS).
//
// Putting items in more tables goes pre-roll first, in 07_rules.js (exact odds, part of the pool):
//   L.add(/village/, i.item, { share: 0.02 })              2% of a roll in each village table's main pool
//   L.companion(i.item, i.anchor_item, { rate: 0.5 })      beside every anchor entry, half as likely
//   L.addPool('minecraft:chests/ancient_city', { rolls: 1, entries: [...] })   a whole extra pool
//
// LootJS here is for additions that depend on where the chest is (structure, biome, dimension)
// rather than which table it rolled, or on the player. They land on top of the rolled loot.
// Use global.LOOT.tiers.* (00_lib.js) as reusable add-pools, or L.entry({ item, weight, count, chance }).
//
// Reload with /reload.

LootJS.modifiers(event => {
  const L = global.LOOT

  // ── context-scoped additions (examples — uncomment & edit) ──────────────

  // sprinkle the "common" tier into every chest inside a village structure, whatever its table
  // if (L.tiers.common.length) {
  //   L.addPool(
  //     L.chestType(event).anyStructure(['#minecraft:village'], false),
  //     L.tiers.common, 'redistribute/village_common'
  //   ).logName('CE/loot/redistribute/village_common')
  // }

  // snowballs in every chest in a snowy biome
  // L.chestType(event)
  //   .anyBiome('#minecraft:is_snowy')
  //   .addLoot(L.entry({ item: 'minecraft:snowball', count: 4, chance: 0.5 }, 'redistribute/snowy'))
  //   .logName('CE/loot/redistribute/snowy')
})
