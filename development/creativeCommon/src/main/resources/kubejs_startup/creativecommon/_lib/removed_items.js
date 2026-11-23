// priority: -10000
// ============================================================
// Removed item list
// ============================================================
// Empty in the jar. A pack or another jar pushes item ids into it at startup (after this script); the pack hides them
// in JEI and strips their item tags with global.stripRemovedItemTags (server helper,
// data/creativecommon/kubejs/_lib/removed_item_tags.js). Item merges (item_merges.js) add their purged ids here too.

global.hasScannedRemovedItemRecipes = false

global.removedItems = []
