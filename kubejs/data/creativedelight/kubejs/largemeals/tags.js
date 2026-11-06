// requires: largemeals
// Large Meals port tags (feature_writeup/large_meals.md 4 steps 3, 4, 17). Food types come from food_types.json, not from here.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('largemeals')) return
	var i = global.item
	var t = global.tags

	// Brown mushrooms for Pasta with Mushroom Sauce (B4): the vanilla brown mushroom
	t.add(e, i.t_mushrooms_brown, [i.brown_mushroom])
})

ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('largemeals')) return
	var b = global.blocks
	var t = global.tags
	// The feasts break like the FD feasts (knife, BlockRunner's cake and feast tier)
	var FEASTS = [b.omurice_block, b.mushroom_pot_pie_block, b.roasted_mutton_rack_block]
	t.add(e, b.t_fd_mineable_knife, FEASTS)
	t.add(e, b.t_blockrunner_confectionery, FEASTS)
})
