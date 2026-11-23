// priority: -10000
// Strips every item tag from global.removedItems (empty in the jar; a pack pushes into it at startup). The removed-item
// recipe scan (/recipescan) that shared this file as recipe_scan.js is Creative Devtools'
// (kubejs/data/creativedevtools/kubejs/dump/recipe_scan.js, creative_devtools.md 2).

// ============================================================
// Tag stripping helper
// ============================================================
// Called from a ServerEvents.tags('item') handler: the pack's own (server_scripts/tags/removed_item_tags.js), and Creative
// Delight's tag scripts, which run after every pack script, strip again at their end
// (kubejs/data/creativedelight/kubejs/_core/removed_item_tags.js).
global.stripRemovedItemTags = event => global.tags.clearAll(event, global.removedItems)
