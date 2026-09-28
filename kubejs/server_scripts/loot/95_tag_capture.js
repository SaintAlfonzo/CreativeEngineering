// priority: -1000
// Chest loot — TAG CAPTURE for '#tag' rules in 05_tuning.js.
//
// Loot tables load before tags are bound to the registry, so a '#tag' rule can't ask the registry.
// The tag event of the same reload runs earlier (in the reload's prepare phase, loot tables apply
// after it), so this handler copies the members of every tag a rule names. Priority -1000 so it
// is registered last and runs after every tag script's adds (tags/loottags.js and the rest).

ServerEvents.tags('item', event => {
    global.lootTuning.captureTags(event)
})
