// requires: creativedelight
// The pack's flavor crafts on foods (flavors were seasonings until food/seasoning #25), and the milled powders.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH.forPack('creativedelight')
	var i = global.item

	global.CD.recipes(() => {

		// ============================================================
		// Pack flavors on foods (Creative Delight's kubejs_startup/creativedelight/flavors.js holds the flavors themselves)
		// ============================================================
		// One craft per flavor: any food in its flavorable tag (flavor_tags.js: families, then single adds and
		// removals) + the flavor item -> the same food carrying it. Plus one craft per pair that combines, both at
		// once (the craft checks the food is in both tags). A food takes a flavor once; flavors that don't combine
		// can't share a food, so garlic popcorn + onion, onion popcorn + garlic, or popcorn + both give the same stack.
		var S = global.flavor
		var flavorable = key => {
			var id = S[key].id
			return '#' + id.substring(0, id.indexOf(':')) + ':flavorable/' + id.substring(id.indexOf(':') + 1)
		}
		var hasFoods = key => (S[key].families || []).length + (S[key].add || []).length > 0 &&
			(!S[key].needs || global.CD.on(S[key].needs))
		var keys = Object.keys(S).filter(hasFoods)
		// a flavor with `extra` parts (Cream and Onion) takes one more ingredient per part in the same craft
		keys.forEach(key => R.flavor(flavorable(key), [key].concat(S[key].extra || []), e, key))
		keys.forEach((a, n) => keys.slice(n + 1).forEach(b => {
			if ((S[a].combines || []).indexOf(b) < 0 && (S[b].combines || []).indexOf(a) < 0) return
			var pair = [a, b].sort()
			R.flavor(flavorable(pair[0]), pair, e, pair.join('_'))
		}))

		// ============================================================
		// Milled powders (food/seasoning #28, R6): onion, garlic and ghost pepper ground into a powder flavor item by
		// the pack's food grinding: 2 from one by Create crushing wheels, millstone or mortar and pestle
		// ============================================================
		R.foodGrinding([Item.of(i.onion_powder, 2)], i.onion, e, 'onion_powder')
		R.foodGrinding([Item.of(i.garlic_powder, 2)], i.t_crops_garlic, e, 'garlic_powder')
		if (global.CD.on('vintagedelight')) R.foodGrinding([Item.of(i.ghost_pepper_powder, 2)], i.ghost_pepper, e, 'ghost_pepper_powder')
	})
})
