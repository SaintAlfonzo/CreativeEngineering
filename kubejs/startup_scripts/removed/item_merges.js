// priority: 90
// ============================================================
// Item merges: the pack's general group
// ============================================================
// Purged id -> surviving id, through Creative Common's global.addItemMerges (its item_merges.js has the mechanism and
// the group rules; design and tests: feature_writeup/item_merges.md). Server half: server_scripts/other/item_merges.js.

var i = global.item

global.addItemMerges('pack', [
	// Decorative Blocks' rocky dirt goes, Twigs' stays (Dave's is a separate block, renamed Pebble Dirt)
	[i.decorative_blocks_rocky_dirt, i.twigs_rocky_dirt],
	// Twigs' azalea flowers -> Immersive Weathering's; the placed block stays Twigs' (Creative Compat
	// IwAzaleaFlowersPlaceMixin places it from Immersive Weathering's item; item_merges.md 2.21)
	[i.azalea_flowers, i.immersive_weathering_azalea_flowers],
	// Little Logistics' wireless components -> Create's Redstone Link (their own recipes are removed in Creative
	// Engineering's transport.js)
	[i.ll_receiver_component, i.redstone_link],
	[i.ll_transmitter_component, i.redstone_link],
])

console.log(`[itemMerges] ${global.itemMergeGroups.pack.length} pack merges registered`)
