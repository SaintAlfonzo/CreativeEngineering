// requires: vintagedelight
// Food item merges (feature_writeup/item_merges.md 2.9; TODO dishes/pickles-and-ferments #1), Creative Delight's
// group: the server half is data/creative_delight/kubejs/_core/item_merges.js and vintagedelight/recipes.js. The
// items are set up in vintage_items.js (Pickled Sea Pickle registered there). Restart to apply.

var i = global.item
global.addItemMerges('creative_delight', [
	// Brewin' and Chewin's Pickled Pickles go, the Pickled Sea Pickle stays
	[i.pickled_pickles, i.pickled_sea_pickle],
	// One salt: The Salt's salt goes into Vintage's pink salt dust, Vintage's Salt Block into The Salt's
	[i.salt_item, i.salt_dust],
	[i.salt_block, i.salt_salt_block],
])
