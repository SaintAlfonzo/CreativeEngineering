// priority: -10000
// Removed items lose every item tag. Last of the pack's tag scripts by priority, so tags other pack scripts add to a
// removed item are stripped too. The removed-item recipe scan (/recipescan) that shared this file as recipe_scan.js is
// Creative Devtools' (kubejs/data/creativedevtools/kubejs/dump/recipe_scan.js, creative_devtools.md 2).

// ============================================================
// Tag stripping
// ============================================================
// A global so Creative Delight's tag scripts, which run after every pack script, can strip again at their end
// (kubejs/data/creativedelight/kubejs/_core/removed_item_tags.js).
global.stripRemovedItemTags = event => global.tags.clearAll(event, global.removedItems)
ServerEvents.tags('item', event => global.stripRemovedItemTags(event))
