// Brewin' and Chewin' item tags, left in the pack for Creative Brewing (TODO creativebrewing/move). Every other food
// and cooking tag moved to Creative Delight 2026-10-25, one tags.js per mod owning the entries
// (kubejs/data/creative_delight/kubejs/<mod>/tags.js; food types, jars and cans in creativeengineering/).
ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
	console.log('misctagremoval starting')

	// lasagna meats
	t.add(e, 'brewinandchewin:cooked_horror_lasagna_meats', [
		i.beef_cooked, i.mutton_cooked, i.beef_patty, i.cooked_mutton_chops,
	])

	// meals (TODO dishes/meals #1, #2)
	// cooked veggies for heatless soups and stews: the rule and the other entries are in Creative Delight
	// (minecraft/tags.js and each mod's tags.js); Brewin's kimchi stays here (its Pickled Pickles merged into Creative
	// Delight's Pickled Sea Pickle, vintagedelight/tags.js).
	t.add(e, i.t_soup_veggie_cooked, [
		i.kimchi,
	])
	// bell peppers top a pizza (Rustic Delight 1.7.0 adds them to Brewin's tag; our Brewin port reads foods/pizza_topping)
	t.add(e, i.t_pizza_topping, i.any_bell_pepper)
})
