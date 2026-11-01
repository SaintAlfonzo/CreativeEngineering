// requires: brewinandchewin
// Brewin' and Chewin' item tags for its dishes (owner, 2026-10-31: Brewin' dishes stay in Creative Delight, see
// recipes.js here). Moved from the pack's server_scripts/tags/cookingtags.js 2026-10-31, rows unchanged. Nothing in the
// pack removes from these tags, so running after the pack's tag scripts gives the same contents.
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

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
