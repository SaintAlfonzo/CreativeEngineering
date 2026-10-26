// requires: alexsdelight
// Alex's Delight recipe changes: bone broth and pasta grid routes, catfish slicing. Non-member file: recipe ids are
// creative_delight:<type>/<suffix> (global.CD.recipes). Moved from the pack's kubejs/server_scripts/cooking/cooking.js
// 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH
	var F = global.foodFamily(e)

	global.CD.recipes(() => {

		//######################### Meals #########################
		// Heatless meal rule: farmersdelight/recipes.js "Meals". Bone broth routes (TODO dishes/ingredients #4), same rule.
		R.shapeless(i.acacia_blossom_soup, [i.bone_broth, i.onion, i.pasta, i.acacia_blossom, i.acacia_blossom], e, 'acacia_blossom_soup_cooked_manual_only')
		R.shapeless(i.kangaroo_stew, [i.bone_broth, i.t_cooked_kangaroo, i.t_cooked_kangaroo, i.carrot, i.beetroot], e, 'kangaroo_stew_cooked_manual_only')
		// Pastas take no broth: the pot's bowl (tomato sauce brings its own)
		R.shapeless(i.kangaroo_pasta, [i.t_cooked_kangaroo, i.t_cooked_kangaroo, i.pasta, i.tomato_sauce], e, 'kangaroo_pasta_cooked_manual_only')
		R.shapeless(i.lobster_pasta, [i.lobster_tail_cooked, i.lobster_tail_cooked, i.pasta, i.tomato_sauce], e, 'lobster_pasta_cooked_manual_only')

		//######################### Protein #########################
		//Fish
		//Increase slicing yield
		R.remove({ id: 'alexsdelight:cutting/raw_catfish_slice' }, e)
		R.fdCutting(i.catfish, i.any_knife, Item.of(i.catfish_slice, 6), e)
	})
})
