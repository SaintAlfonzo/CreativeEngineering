// requires: creativeworldgen
// Chest loot — POST-ROLL REMOVALS (LootJS).  Only for what the pre-roll framework can't reach.
//
// Removals go pre-roll first, in 07_rules.js: the entry is taken out of the table before the roll,
// so the slot is refilled from the rest of the pool and the odds stay exact.
//   everywhere:        L.remove(i.some_item)                     (or "remove": true in loot_config.json)
//   specific tables:   L.remove(i.some_item, { tables: 'minecraft:chests/simple_dungeon' })
//   a whole mod:       L.remove('@somemod', { except: [i.keep_me] })
// Items the catalog marks "post_roll_only" (a loot modifier or mod code adds them) are handled
// after the roll by the same calls automatically; the log lists them each reload.
//
// LootJS removal here is for the rest: removals that depend on WHERE the chest is rather than which
// table it rolled (structure, biome, dimension), or on who opened it. A LootJS removal runs after
// the roll and leaves the slot empty.
//
// Reload with /reload. Filter tokens: 'ns:id' | '#ns:tag' | '@modid' | /regex/ | ItemFilter.XXX.

LootJS.modifiers(event => {
  const L = global.LOOT

  // Botania and CC:Tweaked stay out of village chests (owner 2026-11-03, plan_scratch_bugfix_1103 7.3, SQ-GLD-09). Both
  // inject a nested table in code (Botania LootHandler: temple, toolsmith, weaponsmith; CC:Tweaked CommonHooks:
  // cartographer), which the pre-roll framework cannot edit, so their items are taken out after the roll.
  L.tables(event, /^minecraft:chests\/village\//)
    .removeLoot('@botania')
    .removeLoot('@computercraft')
    .logName('CE/loot/removals/village_botania_cc')

  // ── context-scoped removals (examples — uncomment & edit) ─────────────────

  // no rotten flesh in any chest inside a village, whatever table it rolled
  // L.chestType(event)
  //   .anyStructure(['#minecraft:village'], false)
  //   .removeLoot('minecraft:rotten_flesh')
  //   .logName('CE/loot/removals/village_flesh')

  // no fire charges in Nether chests
  // L.chestType(event)
  //   .anyDimension('minecraft:the_nether')
  //   .removeLoot('minecraft:fire_charge')
  //   .logName('CE/loot/removals/nether_fire_charge')
})
