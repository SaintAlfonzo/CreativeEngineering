// requires: endersdelight
// requires: ends_delight
// Food item merge (feature_writeup/item_merges.md 2.14; TODO dishes/ingredients #32), Creative Delight's group: the
// server half is data/creativedelight/kubejs/_core/item_merges.js and endersdelight/recipes.js (duplicate cutting
// recipe), plus the Ender's knife drop switched off in kubejs/data/endersdelight/loot_modifiers. Restart to apply.

var i = global.item
global.addItemMerges('creativedelight', [
	// One shulker meat: End's Delight's. Ender's Delight's raw mollusk and its filet go
	[i.shulker_mollusk, i.shulker_meat],
	[i.shulker_filet, i.shulker_meat_slice],
])
