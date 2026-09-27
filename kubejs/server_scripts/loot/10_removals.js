// Chest loot — TARGETED REMOVALS.  Take an item out of specific tables only.
//
// Removing an item from EVERY chest goes in loot_config.json instead ("remove": true on the item,
// or a "@modid" / "/regex/" pattern) — see 05_tuning.js. That path rewrites the table before the
// roll, so the slot is refilled from the rest of the pool; a LootJS removeLoot() here runs after
// the roll and just leaves the slot empty.
//
// Reload with /reload. Filter tokens: 'ns:id' | '#ns:tag' | '@modid' | /regex/ | ItemFilter.XXX.

LootJS.modifiers(event => {
  const L = global.LOOT

  // ── targeted removals (examples — uncomment & edit) ───────────────────────

  // strip rotten flesh from just the vanilla simple dungeon
  // L.tables(event, 'minecraft:chests/simple_dungeon')
  //   .removeLoot('minecraft:rotten_flesh')
  //   .logName('CE/loot/removals/simple_dungeon_flesh')

  // strip a whole mod's gear from that mod's own dungeon chests
  // L.tables(event, /^somemod:.*chests?\//)
  //   .removeLoot(/^somemod:/)
  //   .logName('CE/loot/removals/somemod_selfloot')
})
