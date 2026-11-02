// requires: alexsdelight
// Alex's Delight recipe changes: bone broth and pasta grid routes, catfish slicing. Non-member file: recipe ids are
// creativedelight:<type>/<suffix> (global.CD.recipes). Moved from the pack's kubejs/server_scripts/cooking/cooking.js
// 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		var F = global.CD.food(e)

		//######################### Meals #########################
		// Heatless meal rule: farmersdelight/recipes.js "Meals". Bone broth routes (TODO dishes/ingredients #4), same rule.
		F.soupCraft([i.onion, i.pasta, i.acacia_blossom, i.acacia_blossom], i.acacia_blossom_soup)
		F.soupCraft([i.t_cooked_kangaroo, i.t_cooked_kangaroo, i.carrot, i.beetroot], i.kangaroo_stew)
		// Pastas take no broth: the pot's bowl (tomato sauce brings its own)
		F.mealCraft([i.t_cooked_kangaroo, i.t_cooked_kangaroo, i.pasta, i.tomato_sauce], i.kangaroo_pasta)
		F.mealCraft([i.lobster_tail_cooked, i.lobster_tail_cooked, i.pasta, i.tomato_sauce], i.lobster_pasta)

		//######################### Protein #########################
		//Fish
		// Fish slice family (CD.FOOD: 6 slices and a bone meal; Alex's Delight: 2), replacing the mod's cut
		F.fishSlice(i.catfish, i.raw_catfish_slice)
	})
})
