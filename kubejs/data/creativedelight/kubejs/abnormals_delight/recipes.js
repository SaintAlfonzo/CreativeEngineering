// requires: creativedelight
// requires: abnormals_delight
// requires: untitledduckmod
// Abnormals Delight recipe changes. Non-member file: recipe ids are creativedelight:<type>/<suffix>
// (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		var F = global.CD.food(e)

		//######################### Duck #########################
		// Abnormals Delight's duck line needs Environmental, so none of it loaded. It is wired to Untitled Duck Mod's
		// raw duck (`#forge:raw_duck`) instead (TODO food/types-and-tuning #10, briefs/food_types-and-tuning.md):
		// raw cut on a board, cooked cut in the cooking trio at FD's times and xp (food_families.md 3.1 `meat`).
		// Passion fruit glazed duck needs Atmospheric and is a removed item; duck noodles has its heatless craft at the
		// end of this file.
		// cut from the duck item only: #forge:raw_duck also holds the fillet, so the tag made a fillet -> 2 fillets loop (SQ-CDFW-07)
		F.meatCut(i.raw_duck, i.duck_fillet)
		F.meatCook(i.duck_fillet, i.cooked_duck_fillet)
		R.fdCooking([i.t_raw_duck, i.pasta, i.carrot, i.vegetables], i.duck_noodles, e, 'duck_noodles', 1.0, 200, i.bowl)
		// Heatless craft (moved from the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, unchanged; rule in
		// farmersdelight/recipes.js "Meals"): pastas take no broth, the pot's bowl
		F.mealCraft([i.t_cooked_duck, i.t_cooked_duck, i.pasta, i.carrot, i.t_soup_veggie_cooked, i.bowl], i.duck_noodles)
	})
})
