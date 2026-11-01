// priority: -100
// Item merges, the pack's group (startup_scripts/_lib/item_merges.js, feature_writeup/item_merges.md 1.1): the
// purged item's functionality moves to its survivor. Runs late so the swap sees every recipe the other pack scripts
// added. Food merges are Creative Delight's group, run by its own server script
// (kubejs/data/creative_delight/kubejs/_core/item_merges.js); this file never swaps them.
//
//   recipes    every input and output naming a purged id now names the survivor; duplicates that leaves
//              behind are removed below
//   tags       survivors join the purged item's tags where a recipe or mob reads them (the purged id's own
//              tags are stripped by _lib/removed_item_tags.js)
//   loot       any table entry for a purged id drops the survivor (LootJS, same count)
//   inventory  a purged item that still reaches a player (mod code, old chests, old worlds) turns into the
//              survivor, same count

ServerEvents.recipes(e => {
	var i = global.item
	var R = global.NCH

	global.itemMergeRun.recipes(e, 'pack')

	// Rocky dirt. Decorative Blocks' goes into Twigs'; its 4 dirt -> 1 and 1 -> 4 dirt recipes go (the second would
	// turn Twigs' cheap rocky dirt into dirt). Dave's is renamed Pebble Dirt (namechanges.js) and the two remaining
	// recipes swap blocks: pebbles make Pebble Dirt, cobblestone makes Rocky Dirt. Coarse dirt in place of dirt
	// doubles the output.
	R.remove({ id: 'decorative_blocks:rocky_dirt' }, e)
	R.remove({ id: 'decorative_blocks:dirt_from_rocky_dirt' }, e)
	R.remove({ id: 'davebuildingmod:rec_rocky_dirt' }, e)
	R.remove({ id: 'twigs:rocky_dirt' }, e)
	R.shaped(Item.of(i.rocky_dirt, 4), ['DP', 'PD'], { D: i.dirt, P: i.twigs_pebble }, e, 'pebble_dirt')
	R.shaped(Item.of(i.rocky_dirt, 8), ['DP', 'PD'], { D: i.coarse_dirt, P: i.twigs_pebble }, e, 'pebble_dirt_from_coarse_dirt')
	R.shapeless(Item.of(i.twigs_rocky_dirt, 4), [i.dirt, i.dirt, i.cobblestone, i.cobblestone], e, 'rocky_dirt')
	R.shapeless(Item.of(i.twigs_rocky_dirt, 8), [i.coarse_dirt, i.coarse_dirt, i.cobblestone, i.cobblestone], e, 'rocky_dirt_from_coarse_dirt')

	// Permafrost. Immersive Weathering's is the only one in the world (patcherMod QuarkPermafrostStyleMixin);
	// Quark's is Froststone and gets IW's craft with stone for dirt. Its bricks, slabs, stairs and walls keep Quark's.
	R.shapeless(Item.of(i.froststone, 2), [i.packed_ice, i.packed_ice, i.stone, i.stone], e, 'froststone')
})

LootJS.modifiers(event => global.itemMergeRun.loot(event, 'pack'))

PlayerEvents.inventoryChanged(e => global.itemMergeRun.inventory(e, 'pack'))
