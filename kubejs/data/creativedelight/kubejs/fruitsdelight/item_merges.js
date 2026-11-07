// requires: creativedelight
// requires: fruitsdelight
// requires: delightful
// Cantaloupe merge (feature_writeup/item_merges.md 2.10). Non-member file: recipe ids are
// creativedelight:<type>/<suffix> (global.CD.recipes). Pairs: Creative Delight's startup fruitsdelight/item_merges.js.
ServerEvents.recipes(e => {
	if (!global.CD || !Item.exists(global.item.cantaloupe_seeds)) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		// Seeds: the slice gives Cantaloupe Seeds (Fruits Delight's hamimelon_seeds recipe, input swapped by the merge);
		// one seed turns into the other by hand
		R.shapeless(i.cantaloupe_seeds, [i.hamimelon_seeds], e, 'mini_cantaloupe_seeds')
		R.shapeless(i.hamimelon_seeds, [i.cantaloupe_seeds], e, 'cantaloupe_seeds_from_mini')
		// Popsicle: Fruits Delight's slice recipe is removed (Delightful's two stay, delightful/recipes.js)
		R.remove({ id: 'fruitsdelight:hamimelon_popsicle' }, e)
	})
})

ServerEvents.tags('item', e => {
	if (!global.CD || !Item.exists(global.item.cantaloupe_seeds)) return
	var i = global.item
	// the slice takes the hamimelon slice's exotic fruit tag (fruitsdelight/tags.js); Cantaloupe Seeds are seeds
	global.tags.add(e, i.t_exotic_fruit, i.cantaloupe_slice)
	global.tags.add(e, i.t_seeds, i.hamimelon_seeds)
})
