// requires: abnormals_delight
// requires: untitledduckmod
// Abnormals Delight recipe changes. Non-member file: recipe ids are creative_delight:<type>/<suffix>
// (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Duck #########################
		// Abnormals Delight's duck line needs Environmental, so none of it loaded. It is wired to Untitled Duck Mod's
		// raw duck (`#forge:raw_duck`) instead (TODO food/types-and-tuning #10, briefs/food_types-and-tuning.md):
		// raw cut on a board, cooked cut in the cooking trio at FD's times and xp (food_families.md 3.1 `meat`).
		// Passion fruit glazed duck needs Atmospheric and is a removed item; duck noodles has its heatless craft at the
		// end of this file.
		R.fdCutting(i.t_raw_duck, i.any_knife, Item.of(i.duck_fillet, 2), e, 'duck_fillet')
		R.smelting(i.cooked_duck_fillet, i.duck_fillet, e, 'cooked_duck_fillet', 0.35, 200)
		R.smoking(i.cooked_duck_fillet, i.duck_fillet, e, 'cooked_duck_fillet', 0.35, 100)
		R.campfireCooking(i.cooked_duck_fillet, i.duck_fillet, e, 'cooked_duck_fillet', 0.35, 600)
		R.fdCooking([i.t_raw_duck, i.pasta, i.carrot, i.vegetables], i.duck_noodles, e, 'duck_noodles', 1.0, 200, i.bowl)
		// Heatless craft (moved from the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, unchanged; rule in
		// farmersdelight/recipes.js "Meals"): pastas take no broth, the pot's bowl
		R.shapeless(i.duck_noodles, [i.t_cooked_duck, i.t_cooked_duck, i.pasta, i.carrot, i.t_soup_veggie_cooked, i.bowl], e, 'duck_noodles_cooked_manual_only')
	})
})
