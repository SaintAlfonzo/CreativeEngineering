// requires: oceanic_delight
// requires: veggiesdelight
// Food item merge (feature_writeup/item_merges.md 2.15; TODO merges/dishes #6), Creative Delight's group: the server
// half is data/creativedelight/kubejs/_core/item_merges.js and veggiesdelight/cooking.js (one recipe). Restart to apply.

var i = global.item
global.addItemMerges('creativedelight', [
	// One Caesar Salad: Oceanic Delight's goes, Veggies Delight's stays (its texture; shown as "Caesar Salad")
	[i.caesar_salad, i.cesar_salad],
])
