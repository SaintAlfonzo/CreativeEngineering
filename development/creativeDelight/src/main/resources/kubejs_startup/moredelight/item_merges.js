// requires: moredelight
// requires: rusticdelight
// Food item merge (feature_writeup/item_merges.md 2.4), Creative Delight's group: the server half is
// data/creative_delight/kubejs/_core/item_merges.js and moredelight/item_merges.js. Restart to apply.

// More Delight's diced potatoes go, Rustic Delight's potato slices stay
var i = global.item
global.addItemMerges('creative_delight', [
	[i.diced_potatoes, i.potato_slices],
])
