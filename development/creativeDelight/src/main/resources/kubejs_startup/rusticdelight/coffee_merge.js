// requires: farmersrespite
// requires: rusticdelight
// Food item merges (feature_writeup/item_merges.md 2.8), Creative Delight's group: the server half is
// data/creativedelight/kubejs/_core/item_merges.js and rusticdelight/coffee_merge.js. Restart to apply.
// Moved from kubejs_startup/farmersrespite/item_merges.js 2026-11-01 (TODO creativedelight/members #11: the coffee
// unification lives in the Rustic member's tree), rows unchanged.

// Farmer's Respite's coffee goes, Rustic Delight's stays: crop, beans, sack, wild bush and the three drinks.
// Rustic Delight's coffees give Caffeinated (Creative Brewing's effect_swaps.js). Berries are the raw crop (Rustic's beans),
// Respite's beans the processed one (Rustic's roasted beans); long coffee folds into plain, strong into dark.
var i = global.item
global.addItemMerges('creativedelight', [
	[i.coffee_berries, i.coffee_beans],
	[i.farmersrespite_coffee_beans, i.roasted_coffee_beans],
	[i.coffee_beans_sack, i.coffee_beans_bag],
	[i.wild_coffee_bush, i.wild_coffee],
	[i.farmersrespite_coffee, i.coffee],
	[i.long_coffee, i.coffee],
	[i.strong_coffee, i.dark_coffee],
])
