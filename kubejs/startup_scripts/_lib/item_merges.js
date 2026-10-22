// priority: -10001
// ============================================================
// Item merges: total replacements
// ============================================================
// One item is purged and the survivor takes over everything it did. Each entry is
// purged id -> surviving id. Runs after removed_items.js and adds the purged ids to
// global.removedItems (hidden in JEI, item tags stripped, recipe_scan.js logs any recipe that
// still names them). The rest is in server_scripts/other/item_merges.js: recipe inputs and
// outputs swapped, loot swapped, and any purged item that reaches a player inventory (mod code,
// old chests) swapped for its survivor. Per-merge recipe and tag work sits in the feature's own
// script. Design and tests: feature_writeup/item_merges.md.

var i = global.item;

global.itemMerges = [
	// Alex's Mobs' banana goes, Neapolitan's stays. Leaf drops stay off
	// (config/alexsmobs.toml bananasDropFromLeaves = false); eating a banana still leaves a peel.
	[i.banana, i.neapolitan_banana],
	// Neapolitan's milk bottle goes, Farmer's Delight's stays
	[i.neapolitan_milk_bottle, i.farmersdelight_milk_bottle],
	// Neapolitan's chocolate cake goes, Create Crafts & Additions' stays (its texture, spout recipe, slices)
	[i.neapolitan_chocolate_cake, i.chocolate_cake],
	// More Delight's diced potatoes go, Rustic Delight's potato slices stay
	[i.diced_potatoes, i.potato_slices],
	// Decorative Blocks' rocky dirt goes, Twigs' stays (Dave's is a separate block, renamed Pebble Dirt)
	[i.decorative_blocks_rocky_dirt, i.twigs_rocky_dirt],
];

global.itemMerges.forEach(m => global.removedItems.push(m[0]));

console.log(`[itemMerges] ${global.itemMerges.length} merges, purged ids added to removedItems`)
