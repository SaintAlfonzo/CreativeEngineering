// requires: fruitsdelight
// requires: delightful
// Cantaloupe merge (feature_writeup/item_merges.md 2.10), Creative Delight's group: the server half is
// data/creative_delight/kubejs/_core/item_merges.js and fruitsdelight/item_merges.js. Restart to apply.
// Fruits Delight's hamimelon is the Cantaloupe (block, stem, seeds, foods, wild patch); Delightful's cantaloupe is the
// Mini Cantaloupe (sand crop, its seeds). One slice and one popsicle: Delightful's. Names: namechanges.js.

var i = global.item
global.addItemMerges('creative_delight', [
	// Fruits Delight's hamimelon slice goes, Delightful's cantaloupe slice stays
	[i.hamimelon_slice, i.cantaloupe_slice],
	// Fruits Delight's hamimelon popsicle goes, Delightful's cantaloupe popsicle (and its art) stays (owner 2026-10-28)
	[i.hamimelon_popsicle, i.cantaloupe_popsicle],
	// the generated hamimelon can goes, the cantaloupe can stays (same slice now)
	[i.canned_hamimelon, i.canned_cantaloupe],
])
