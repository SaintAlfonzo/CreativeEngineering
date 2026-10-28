// requires: oceanic_delight
// Oceanic Delight names. The sponge cake sits on a pie crust and bakes in the pie tin, so it is a pie
// (TODO baking/baked-goods #25, food_families.md `pie/whole`).
ClientEvents.lang('en_us', e => {
	if (!global.CD || !global.CD.on('oceanic_delight')) return
	e.renameItem(global.item.sponge_cake, 'Sponge Pie')
	e.renameItem(global.item.sponge_cake_slice, 'Sponge Pie Slice')
})
