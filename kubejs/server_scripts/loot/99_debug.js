// Chest loot — DEBUG.  Inert by default.
//
// Uncomment enableLogging() while tuning: LootJS then prints, for every loot roll, which
// modifiers matched the table and what they did — look for [LootJS] lines in logs/latest.log.
// Open chests in FRESH chunks (Lootr fills each container per-player at open time, so a chest
// a player already opened won't reflect a /reload).

LootJS.modifiers(event => {
  // event.enableLogging()
})
