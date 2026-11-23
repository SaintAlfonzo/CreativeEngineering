// priority: -10000
// Removed items lose every item tag. Last of the pack's tag scripts by priority, so tags other pack scripts add to a
// removed item are stripped too. The list is startup_scripts/removed/removed_items.js plus what jars push; the helper
// is Creative Common's (global.stripRemovedItemTags).
ServerEvents.tags('item', event => global.stripRemovedItemTags(event))
