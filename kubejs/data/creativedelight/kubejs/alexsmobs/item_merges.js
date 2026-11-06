// requires: alexsmobs
// requires: neapolitan
// Banana merge (feature_writeup/item_merges.md 2.1): Alex's Mobs' banana into Neapolitan's. Moved from the pack's
// server_scripts/other/item_merges.js 2026-10-27, rows unchanged.
ServerEvents.tags('item', e => {
	if (!global.CD) return
	global.tags.add(e, global.item.t_crops_banana, global.item.neapolitan_banana)   // Alex's Mobs' monkeys read #alexsmobs:bananas
})

// Alex's Mobs' banana left a peel when eaten; Neapolitan's does now (the peel keeps its sopa de macaco recipe)
ItemEvents.foodEaten(global.item.neapolitan_banana, e => {
	if (global.CD && e.player) e.player.give(global.item.banana_peel)
})
