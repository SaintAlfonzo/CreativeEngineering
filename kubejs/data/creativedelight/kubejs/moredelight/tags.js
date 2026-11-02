// requires: moredelight
// More Delight items in pack item tags. Non-member file (as recipes.js): the requires line gates it.
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var i = global.item
	// Mashed Potatoes take salt (The Salt's Salting craft), as Veggies Delight's Mashed Sweet Potatoes do
	// (merges/dishes #2, owner 2026-11-01; veggiesdelight/tags.js)
	global.tags.add(e, i.t_can_be_salted, i.mashed_potatoes)
})
