// priority: -10000
// Removed items lose every item tag (_lib/recipe_scan.js in the pack). That strip runs at the end of the pack's tag
// scripts, before any CD script, so CD rows that tag a removed item (food_type_tags.js from food_types.json, the
// Delightful venison) would keep them. This strips again, last in the CD tree.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.stripRemovedItemTags) return
	global.stripRemovedItemTags(e)
})
