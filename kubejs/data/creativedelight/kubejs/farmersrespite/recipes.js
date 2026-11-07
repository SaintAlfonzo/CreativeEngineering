// requires: creativedelight
// requires: farmersrespite
// Farmer's Respite's long and strong apple cider in Create and IE machines. Non-member file: recipe ids are
// creativedelight:<type>/<suffix> (global.CD.recipes). Moved from the pack's kubejs/server_scripts/cooking/cooking.js
// 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Apple cider #########################
		// The cider chain and its base fluid: farmersdelight/recipes.js "Apple cider". Long (milk) and strong (honey), like
		// redstone and glowstone: Create mixer 1000 mB + 1 reagent, heated; IE Mixer 1500 mB + 1 reagent, 6400 RF/t.
		if (Platform.isLoaded('create_central_kitchen') && Platform.isLoaded('immersiveengineering')) {
			if (Platform.isLoaded('create')) {
				;[
					[f.long_apple_cider, i.t_milk_bottle, 'long_apple_cider'],
					[f.strong_apple_cider, i.honey_bottle, 'strong_apple_cider'],
				].forEach(([out, reagent, id]) => {
					R.createMixing(Fluid.of(out, 1000), [Fluid.of(f.apple_cider, 1000), reagent], e, id, 'heated')
					R.ieMixer(Fluid.of(out, 1500), reagent, f.t_apple_cider, e, id, 6400)
				})
			}
			R.ieBottling(i.long_apple_cider, f.t_long_apple_cider, i.glass_bottle, e, undefined, 250)
			R.ieBottling(i.strong_apple_cider, f.t_strong_apple_cider, i.glass_bottle, e, undefined, 250)
		}
	})
})
