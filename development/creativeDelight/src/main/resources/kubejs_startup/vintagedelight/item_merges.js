// requires: vintagedelight
// Food item merges (feature_writeup/item_merges.md 2.9; TODO dishes/pickles-and-ferments #1), Creative Delight's
// group: the server half is data/creativedelight/kubejs/_core/item_merges.js and vintagedelight/recipes.js. The
// items are set up in vintage_items.js (Pickled Sea Pickle registered there). Restart to apply.

var i = global.item
global.addItemMerges('creativedelight', [
	// Brewin' and Chewin's Pickled Pickles go, the Pickled Sea Pickle stays
	[i.pickled_pickles, i.pickled_sea_pickle],
	// One salt: The Salt's salt goes into Vintage's pink salt dust, Vintage's Salt Block into The Salt's
	[i.salt_item, i.salt_dust],
	[i.salt_block, i.salt_salt_block],
	// Mason jars only (decided 2026-11-02, dishes/jams #1, items/jars #9): the removed bottles go into their jars
	[i.vinegar, i.vinegar_jar],
	[i.apple_sauce_bottle, i.apple_sauce_jar],
	[i.pepper_jam_bottle, i.pepper_jam_jar],
	[i.relish_bottle, i.relish_jar],
])
// Vintage's removed sweet berry and glow berry jams (bottle and jar) go into Fruits Delight's, mason jar jams now
if (global.CD.on('fruitsdelight')) global.addItemMerges('creativedelight', [
	[i.sweet_berry_jam_bottle, i.sweetberry_jelly],
	[i.sweet_berry_jam_jar, i.sweetberry_jelly],
	[i.glow_berry_jam_bottle, i.glowberry_jelly],
	[i.glow_berry_jam_jar, i.glowberry_jelly],
])
