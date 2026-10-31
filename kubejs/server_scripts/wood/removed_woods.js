// Removed woods (startup_scripts/_lib/removed_woods.js, feature_writeup/recipes_wood.md 2.6): each purged wood item is
// an item merge into its oak twin. Every recipe that makes a purged item goes here, in one pass, before
// other/item_merges.js swaps the remaining inputs; swapped outputs would only repeat the oak recipes.

ServerEvents.recipes(e => {
	var i = global.item
	var R = global.NCH

	var purged = []
	Object.keys(global.removedWoods).forEach(wood => {
		global.removedWoods[wood].forEach(m => purged.push({ output: m[0] }))
	})
	global.removedWoodTracks.forEach(k => purged.push({ output: i[k] }))
	R.remove(purged, e)

	// Mulberry hedge: Upgrade Aquatic's recipe reads #upgrade_aquatic:river_logs, empty now; an oak log instead (x2 as before)
	R.remove({ id: 'upgrade_aquatic:river_hedge' }, e)
	R.shaped(Item.of(i.river_hedge, 2), ['L', 'W'], { L: i.river_leaves, W: i.log_oak }, e, 'river_hedge')
})
