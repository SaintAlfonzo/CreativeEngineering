// priority: -10001
// ============================================================
// Item merges: total replacements
// ============================================================
// One item is purged and the survivor takes over everything it did. Each entry is
// purged id -> surviving id. Runs after removed_items.js and adds the purged ids to
// global.removedItems (hidden in JEI, item tags stripped, /recipescan logs any recipe that
// still names them). Design and tests: feature_writeup/item_merges.md.
//
// Merges are split by who runs them (item_merges.md 1.1). Each group's recipe, loot and inventory swaps run once,
// from the group's own server script:
//   pack              general merges, here and in removed_woods.js (later split out by mod);
//                     server half in server_scripts/other/item_merges.js
//   creativedelight  food merges, in Creative Delight's startup scripts; server half in
//                     kubejs/data/creativedelight/kubejs/_core/item_merges.js
// A purged id belongs to one group only: global.addItemMerges refuses it a second time, so no item is swapped twice.

global.itemMerges = []      // every pair, all groups (probes and tools read this)
global.itemMergeGroups = { pack: [], creativedelight: [] }

global.addItemMerges = (group, pairs) => {
	var list = global.itemMergeGroups[group]
	if (!list) throw new Error('[itemMerges] unknown group ' + group)
	pairs.forEach(m => {
		var dupe = global.itemMerges.find(o => o[0] === m[0])
		if (dupe) {
			console.error('[itemMerges] ' + m[0] + ' is already merged into ' + dupe[1] + '; ' + group + ' pair skipped')
			return
		}
		if (global.itemMerges.some(o => o[0] === m[1] || o[1] === m[0])) {
			console.warn('[itemMerges] ' + m[0] + ' -> ' + m[1] + ' chains with another merge; point both at the last survivor')
		}
		list.push(m)
		global.itemMerges.push(m)
		global.removedItems.push(m[0])
	})
}

// The server halves call these with their own events; each runs a group once.
global.itemMergeRun = {
	// recipe swap over data and script-added recipes; the second call for a group in the same event is skipped
	recipes(e, group) {
		// identity hash, not the event itself, so the finished event is not kept alive
		var event = e.hashCode()
		if (global.itemMergeRun._event !== event) {
			global.itemMergeRun._event = event
			global.itemMergeRun._done = {}
		}
		if (global.itemMergeRun._done[group]) {
			console.error('[itemMerges] ' + group + ' recipe swap already ran in this recipe event; skipped')
			return
		}
		global.itemMergeRun._done[group] = true
		var pairs = global.itemMergeGroups[group]
		var changed = global.NCH.replaceItems(pairs, e)
		console.log('[itemMerges] ' + group + ': ' + pairs.length + ' merges, ' + changed + ' recipes swapped')
	},

	// LootJS: every table entry for a purged id drops the survivor (same count)
	loot(event, group) {
		var pairs = global.itemMergeGroups[group]
		if (pairs.length === 0) return
		var mod = event.addLootTableModifier(/.*/)
		pairs.forEach(m => { mod = mod.replaceLoot(m[0], m[1], true) })
		mod.logName('CE/item_merges/' + group)
	},

	// PlayerEvents.inventoryChanged: a purged stack in the player's inventory becomes the survivor (same count)
	inventory(e, group) {
		var id = String(e.item.id)
		var merge = global.itemMergeGroups[group].find(m => m[0] === id)
		if (!merge) return
		var inv = e.player.inventory
		for (var k = 0; k < inv.getContainerSize(); k++) {
			var stack = inv.getItem(k)
			if (String(stack.id) === id) inv.setItem(k, Item.of(merge[1], stack.count))
		}
	},
}

var i = global.item

global.addItemMerges('pack', [
	// Decorative Blocks' rocky dirt goes, Twigs' stays (Dave's is a separate block, renamed Pebble Dirt)
	[i.decorative_blocks_rocky_dirt, i.twigs_rocky_dirt],
])

console.log(`[itemMerges] ${global.itemMergeGroups.pack.length} pack merges registered`)
