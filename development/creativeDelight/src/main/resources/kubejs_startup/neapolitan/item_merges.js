// requires: neapolitan
// Food item merges (feature_writeup/item_merges.md 2.2, 2.3), Creative Delight's group: the server half is
// data/creativedelight/kubejs/_core/item_merges.js and neapolitan/item_merges.js. Restart to apply.

var i = global.item
global.addItemMerges('creativedelight', [
	// Neapolitan's milk bottle goes, Farmer's Delight's stays
	[i.neapolitan_milk_bottle, i.farmersdelight_milk_bottle],
	// Neapolitan's chocolate cake goes, Create Crafts & Additions' stays (its texture, spout recipe, slices)
	[i.neapolitan_chocolate_cake, i.chocolate_cake],
])
