// Creative Delight's item merges come from its swap lists (feature_writeup/item_merges.md 6, TODO creativedelight/swappers #2).
// Each data/creativedelight/swaps/merges_*.json in the CD jar names survivor -> [purged ids] under "merges", with the mods it
// "requires"; Common's swap/SwapLists reads them (the same lists remap ids in old worlds and drive the stack rewrite in
// swap/ItemMerges). Here every pair whose required mods are loaded joins the creativedelight group, so the group's server
// half (kubejs/data/creativedelight/kubejs/_core/item_merges.js) swaps recipes and loot, and the purged ids are hidden
// and stripped of tags like every other merge. Per-merge recipe and tag work stays in the purged item's mod folder of
// the server tree. Startup: global.addItemMerges feeds global.removedItems, which the client and tag scripts read at
// load. To add a merge, add the pair to a list file (one place per item), never a second addItemMerges call. Restart to apply.

;(() => {
	var $SwapLists = Java.loadClass('com.SaintAlfonzo.creativecommon.swap.SwapLists')
	var lists = $SwapLists.get().scriptMerges(global.CD.modId)
	var pairs = []
	// Array.from on a Java collection throws in this Rhino; copy by hand (old id -> survivor, list order)
	// The rack and basket survivors exist only while the Cooking for Blockheads compartment is on (its [compartments]
	// switch can be off with CfB installed; the file's requires only checks mods).
	var cfbOn = global.CD.on('cookingforblockheads')
	lists.keySet().forEach(old => {
		var survivor = String(lists.get(old))
		if (!cfbOn && /^creativedelight:.*_(spice_rack|tool_rack|fruit_basket)$/.test(survivor)) return
		pairs.push([String(old), survivor])
	})
	global.addItemMerges('creativedelight', pairs)
	console.info('[creativedelight] ' + pairs.length + ' item merge(s) from the swap lists')
})()
