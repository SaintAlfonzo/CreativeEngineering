// requires: cookingforblockheads
// Kitchen item merges (feature_writeup/kitchen.md 2.7; TODO cooking/kitchen #24, 2026-11-02), Creative Delight's group:
// the server half is data/creativedelight/kubejs/_core/item_merges.js and cookingforblockheads/kitchen.js (CfB's own
// recipes go). Cooking for Blockheads' own Spice Rack, Tool Rack and Fruit Basket are replaced by the wood versions of
// the blockheads_additions member (same CfB blocks and block entities, so kitchens still use them): one in an inventory
// becomes the oak version, a placed one drops it. Restart to apply.

if (global.CD && global.CD.on('blockheads_additions')) {
	var i = global.item
	global.addItemMerges('creativedelight', [
		[i.cfb_spice_rack, i.oak_spice_rack],
		[i.cfb_tool_rack, i.oak_tool_rack],
		[i.cfb_fruit_basket, i.oak_fruit_basket],
	])
}
