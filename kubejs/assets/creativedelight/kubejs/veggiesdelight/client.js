// requires: veggiesdelight
// Veggies Delight client rows.
ClientEvents.lang('en_us', e => {
	if (!global.CD) return
	var i = global.item
	// One muffin family, no cupcakes (TODO baking/baked-goods #32); the id stays sweet_potato_cupcake
	e.renameItem(i.sweet_potato_cupcake, 'Sweet Potato Muffin')
	// Mashed family split (TODO merges/dishes #2): More Delight's keeps Mashed Potatoes; the id stays mashed_potatoes
	e.renameItem(i.veggiesdelight_mashed_potatoes, 'Mashed Sweet Potatoes')
})
