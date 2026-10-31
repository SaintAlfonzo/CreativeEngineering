// requires: fruitsdelight
// requires: delightful
// requires: vinery
// Cantaloupe merge (feature_writeup/item_merges.md 2.10), Creative Delight's group: the server half is
// data/creative_delight/kubejs/_core/item_merges.js and fruitsdelight/item_merges.js. Restart to apply.
// Fruits Delight's hamimelon is the Cantaloupe (block, stem, seeds, foods, wild patch); Delightful's cantaloupe is the
// Mini Cantaloupe (sand crop, its seeds). One slice and one popsicle: Delightful's. Names: namechanges.js.
// Apple tree removal (owner 2026-10-30, crops.md 2.12.1): Fruits Delight's apple tree goes, Vinery's stays. Its
// worldgen is off (kubejs/data/fruitsdelight/forge/biome_modifier/apple.json); leaves and sapling merge into Vinery's.

var i = global.item
global.addItemMerges('creative_delight', [
	// Fruits Delight's hamimelon slice goes, Delightful's cantaloupe slice stays
	[i.hamimelon_slice, i.cantaloupe_slice],
	// Fruits Delight's hamimelon popsicle goes, Delightful's cantaloupe popsicle (and its art) stays (owner 2026-10-28)
	[i.hamimelon_popsicle, i.cantaloupe_popsicle],
	// the generated hamimelon can goes, the cantaloupe can stays (same slice now)
	[i.canned_hamimelon, i.canned_cantaloupe],
	// Fruits Delight's apple tree goes, Vinery's apple tree stays
	[i.apple_sapling, i.apple_tree_sapling],
	[i.fruitsdelight_apple_leaves, i.apple_leaves],
])
