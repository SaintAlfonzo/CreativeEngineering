// Chest loot — TARGETED.  Signature edits to ONE table (or a small named set) at a time.
//
// This is where "I want item X specifically in the end city treasure" lives. For a table
// you're rebuilding wholesale, prefer a datapack override at
//   kubejs/data/<ns>/loot_tables/chests/<x>.json
// plus  event.disableLootModification('<ns>:chests/<x>')  here so LootJS doesn't double-touch it.
//
// Reload with /reload.

LootJS.modifiers(event => {
  const L = global.LOOT

  // ── per-table edits (examples — uncomment & edit) ───────────────────────

  // end city treasure: add a guaranteed pack trophy
  // L.tables(event, 'minecraft:chests/end_city_treasure')
  //   .addLoot(L.entry({ item: 'creativeengineering:some_trophy', weight: 1 }, 'targeted/endcity'))
  //   .logName('CE/loot/targeted/end_city')

  // ancient city: a whole extra pool of 1-2 rolls
  // L.tables(event, 'minecraft:chests/ancient_city')
  //   .pool(pool => {
  //     pool.addLoot(L.entry({ item: 'minecraft:echo_shard', count: 2 }, 'targeted/ancient_echo'))
  //     pool.addLoot(L.entry({ item: 'minecraft:disc_fragment_5', weight: 3 }, 'targeted/ancient_frag'))
  //   })
  //   .logName('CE/loot/targeted/ancient_city_pool')

  // hand-authored full override elsewhere — keep LootJS off this one
  // event.disableLootModification('minecraft:chests/buried_treasure')
})
