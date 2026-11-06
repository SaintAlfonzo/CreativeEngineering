// requires: creativeworldgen
// Chest loot — TARGETED (LootJS).  Per-roll custom logic for ONE table or a small named set.
//
// Plain per-table edits are pre-roll, in 07_rules.js: L.add / L.addPool / L.remove / L.frequency with
// a { tables } scope, L.useVanilla to undo a jar's override, L.alias to create a missing table.
// For a table you're rebuilding wholesale, a datapack override at kubejs/data/<ns>/loot_tables/...
// also works (note: overriding a vanilla table stops Forge running mods' code injections into it;
// see restoreInjection).
//
// This file is for what needs code at roll time: .apply(ctx => ...) reading the player, luck, the
// position, or the rolled stacks themselves (e.g. enchant whatever sword rolled).
//
// Reload with /reload.

LootJS.modifiers(event => {
  const L = global.LOOT

  // ── per-roll logic (examples — uncomment & edit) ────────────────────────

  // end city treasure: players with Luck get an extra dragon's breath
  // L.tables(event, 'minecraft:chests/end_city_treasure')
  //   .apply(ctx => { if (ctx.getLuck() > 0) ctx.addLoot(LootEntry.of('minecraft:dragon_breath')) })
  //   .logName('CE/loot/targeted/end_city')

  // hand-authored full override elsewhere — keep LootJS off this one
  // event.disableLootModification('minecraft:chests/buried_treasure')
})
