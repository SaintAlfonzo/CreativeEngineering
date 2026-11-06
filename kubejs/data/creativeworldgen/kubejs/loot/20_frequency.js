// requires: creativeworldgen
// Chest loot — STACK SIZE / CONDITIONAL BONUS (LootJS).
//
// How OFTEN an item shows up is pre-roll, in 07_rules.js or loot_config.json:
//   L.frequency(i.item, 0.5)                       half as often per roll, every chest
//   L.frequency(i.item, 2, { tables: /village/ })  twice as often, village tables only
// LootJS can't do that: it only sees loot after the roll.
//
// Two levers that DO belong here:
//   count   — stack size. L.capCount(builder, filter, max)  or  .modifyLoot(filter, s => ...)
//   chance  — a bonus that fires only in some places or for some players:
//             builder.anyStructure(...).randomChance(0-1) as conditions.
//
// Reload with /reload.

LootJS.modifiers(event => {
  const L = global.LOOT

  // ── count caps (examples — uncomment & edit) ──────────────────────────────

  // no chest ever gives more than 8 emeralds
  // L.capCount(L.chestType(event), 'minecraft:emerald', 8)
  //   .logName('CE/loot/frequency/emerald_cap')

  // ── context-gated bonus ──────────────────────────────────────────────────

  // 30% of chests in deep dark biomes get an extra echo shard
  // L.chestType(event)
  //   .anyBiome('minecraft:deep_dark')
  //   .randomChance(0.3)
  //   .addLoot(L.entry({ item: 'minecraft:echo_shard' }, 'freq/deep_dark_echo'))
  //   .logName('CE/loot/frequency/deep_dark_echo')
})
