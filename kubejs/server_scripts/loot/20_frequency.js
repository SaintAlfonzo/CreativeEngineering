// Chest loot — STACK SIZE / BONUS POOLS.
//
// How OFTEN an item shows up is tuned in loot_config.json ("frequency": 0..1 per item, or a
// "@modid" / "/regex/" pattern) — see 05_tuning.js. LootJS can't do that: it only sees loot
// after the roll, so a replaceLoot(...withWeight(n)) is a no-op on frequency.
//
// Two levers that DO belong here:
//   count   — stack size. L.capCount(builder, filter, max)  or  .modifyLoot(filter, s => ...)
//   chance  — does an added pool fire at all. builder.randomChance(0-1) as a condition.
//
// Reload with /reload.

LootJS.modifiers(event => {
  const L = global.LOOT

  // ── count caps (examples — uncomment & edit) ──────────────────────────────

  // no chest ever gives more than 8 emeralds
  // L.capCount(L.chestType(event), 'minecraft:emerald', 8)
  //   .logName('CE/loot/frequency/emerald_cap')

  // ── appearance chance of an added pool ──────────────────────────────────

  // only 30% of chests get this bonus pool at all
  // L.chestType(event)
  //   .randomChance(0.3)
  //   .addWeightedLoot(L.entry({ item: 'minecraft:golden_apple', weight: 1 }, 'freq/bonus_apple'))
  //   .logName('CE/loot/frequency/bonus_apple')
})
