// requires: alexsmobs
// requires: neapolitan
// Food item merge (feature_writeup/item_merges.md 2.1), Creative Delight's group: the server half is
// data/creative_delight/kubejs/_core/item_merges.js and alexsmobs/item_merges.js. Restart to apply.

// Alex's Mobs' banana goes, Neapolitan's stays. Leaf drops stay off (config/alexsmobs.toml bananasDropFromLeaves =
// false); eating a banana still leaves a peel.
var i = global.item
global.addItemMerges('creative_delight', [
	[i.banana, i.neapolitan_banana],
])
