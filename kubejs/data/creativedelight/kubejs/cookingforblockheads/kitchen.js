// requires: cookingforblockheads
// Wood spice racks, tool racks and fruit baskets (Creative Delight member blockheads_additions, kitchen.md 9), and the counter-topper
// block tags. Each takes CfB's own
// recipe shape in its wood's slabs: spice rack 2 slabs in a row, tool rack 3 slabs over 2 iron nuggets. Rubberwood has
// no slab, so its racks take planks. CfB's own spice rack, tool rack and fruit basket are merged into the oak versions
// (cooking/kitchen #24, 2026-11-02): their recipes are removed. Fruit basket: CfB's shape (slab, pressure plate, slab)
// in the wood's slabs, any wooden pressure plate. Recipe ids are creativedelight:shaped/<rack>.

// wood rack key prefix -> its slab (or planks) dict key
var CD_RACK_WOODS = {
	oak: 'slab_oak', spruce: 'slab_spruce', birch: 'slab_birch', jungle: 'slab_jungle', acacia: 'slab_acacia',
	dark_oak: 'slab_dark_oak', mangrove: 'slab_mangrove', crimson: 'slab_crimson', warped: 'slab_warped',
	twisted: 'slab_twisted', dreamwood: 'dreamwood_planks_slab', livingwood: 'livingwood_planks_slab',
	smoked: 'smoked_slab', treated_wood: 'slab_treated_wood_horizontal',
	vertical_treated_wood: 'slab_treated_wood_vertical', rubberwood: 'planks_rubberwood', ancient: 'slab_ancient',
	bamboo: 'bamboo_planks_slab', driftwood: 'slab_driftwood'
}

ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('blockheads_additions')) return
	var i = global.item
	var slabs = Object.keys(CD_RACK_WOODS).map(w => i[CD_RACK_WOODS[w]])
	global.tags.addMatching(e, i.t_rack_slabs_other, [i.wood_slab], { except: slabs })
})

ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('blockheads_additions')) return
	var i = global.item
	var b = global.blocks
	var t = global.tags
	var racks = []
	Object.keys(CD_RACK_WOODS).forEach(w => racks.push(i[w + '_spice_rack'], i[w + '_tool_rack'], i[w + '_fruit_basket']))
	t.mineable(e, 'axe', null, racks)

	// Counter toppers (moved from the pack's blocktags.js 2026-10-31, rows unchanged). CfB worktops are 15 px tall with
	// no full top face, so FD's cutting board, Display Delight's plates and the coaster found no support on them.
	// Toppers on a support stay and are drawn on the worktop (blockheads_additions kitchen/CounterToppers,
	// BlockStateCounterTopperMixin, TODO cooking/kitchen #8, #20).
	t.add(e, b.t_counter_supports, [b.cfb_counter, b.cfb_corner])
	t.addMatching(e, b.t_counter_toppers, [b.cutting_board, b.dd_food_plate, b.dd_small_food_plate, b.dd_plated_foods, b.coaster])

	// CfB oven as a stove: blockheads_additions gives it a `lit` state that follows its burn time (CfbOvenLitMixin), and
	// FD's heat check reads `lit`, so a pot or skillet on it cooks only while it burns fuel. The keg tag nests this one.
	t.add(e, b.t_fd_heat_sources, i.cfb_oven)
})

ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('blockheads_additions')) return
	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		Object.keys(CD_RACK_WOODS).forEach(w => {
			var slab = i[CD_RACK_WOODS[w]]
			R.shaped(Item.of(i[w + '_spice_rack']), ['SS'], { S: slab }, e, w + '_spice_rack')
			R.shaped(Item.of(i[w + '_tool_rack']), ['SSS', 'N N'], { S: slab, N: i.t_balm_iron_nuggets }, e, w + '_tool_rack')
			R.shaped(Item.of(i[w + '_fruit_basket']), ['SPS'], { S: slab, P: i.any_wooden_pressure_plates }, e, w + '_fruit_basket')
		})
		// CfB's own Spice Rack, Tool Rack and Fruit Basket are merged into the oak versions (TODO cooking/kitchen #24,
		// 2026-11-02; CD startup cookingforblockheads/item_merges.js): their recipes go, so only the 20 wood versions are made
		R.remove({ output: i.cfb_fruit_basket }, e)
		R.remove({ output: i.cfb_spice_rack }, e)
		R.remove({ output: i.cfb_tool_rack }, e)
	})
})
