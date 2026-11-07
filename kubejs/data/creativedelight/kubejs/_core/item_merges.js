// requires: creativedelight
// priority: -100
// Food item merges, Creative Delight's group (feature_writeup/item_merges.md 1.1, 6). The pairs come from CD's swap
// lists (data/creativedelight/swaps/merges_*.json, fed in by the startup script creativedelight/swap_merges.js); per-merge recipe and tag work is in the purged
// item's mod folder here (<mod>/item_merges.js, vintagedelight/). The pack runs only its own group
// (server_scripts/other/item_merges.js), so each pair is swapped once. Last in the CD tree by priority, so the swap
// sees every recipe the pack and CD scripts added.
ServerEvents.recipes(e => {
	if (!global.CD) return
	global.itemMergeRun.recipes(e, 'creativedelight')
})

LootJS.modifiers(event => {
	if (!global.CD) return
	global.itemMergeRun.loot(event, 'creativedelight')
})

PlayerEvents.inventoryChanged(e => {
	if (!global.CD) return
	global.itemMergeRun.inventory(e, 'creativedelight')
})
