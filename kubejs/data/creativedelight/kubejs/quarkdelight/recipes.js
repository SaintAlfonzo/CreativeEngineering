// requires: quarkdelight
// Quark Delight recipe changes: crab bars, grid routes for its pot meals, exotic fruits. Non-member file: recipe ids
// are creativedelight:<type>/<suffix> (global.CD.recipes). Moved from the pack's
// kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Prepared #########################
		// Crab Bar
		//	Quark Delight's raw crab bars (shaped, x4) replaced by a cooked shapeless x2 (hand only) and a raw pot recipe x4
		R.remove({ id: 'quarkdelight:crab_bars' }, e)
		R.shapeless(Item.of(i.crab_bars, 2), [i.cooked_fish, i.cooked_crab, i.bonemeal], e, 'crab_bars_manual_only')
		// Add raw ingredient cooking crab bar recipe
		R.fdCooking([i.raw_fish, i.crab, i.bonemeal], Item.of(i.crab_bars, 4), e, undefined, 1, 200)
		// Sequenced assembly
		if (Platform.isLoaded('create')) {
			let incCrabBars = R.incomplete('crab_bars')
			R.createSequencedAssembly(Item.of(i.crab_bars, 10), i.bonemeal, [
				e.recipes.create.deploying(incCrabBars, [incCrabBars, i.cooked_crab]),
				e.recipes.create.deploying(incCrabBars, [incCrabBars, i.cooked_fish]),
				e.recipes.create.pressing(incCrabBars, incCrabBars)
			], ['crab_bars'], e, 'crab_bars_sequenced_assembly', 2)
		}

		//######################### Meals #########################
		// Heatless meal rule: farmersdelight/recipes.js "Meals". Expensive grid routes for pot meals (TODO dishes/meals #4)
		R.shapeless(i.crab_pasta, [i.cooked_crab, i.cooked_crab, i.pasta, i.t_soup_veggie_cooked, i.bowl], e, 'crab_pasta_cooked_manual_only')
		R.shapeless(i.cavern_explorer_stew, [i.bone_broth, i.glow_shroom, i.glow_shroom, i.glow_ink, i.t_soup_veggie_cooked], e, 'cavern_explorer_stew_cooked_manual_only')
		// Bone broth routes (TODO dishes/ingredients #4), same rule
		R.shapeless(i.miso_with_bamboo_sprouts, [i.bone_broth, i.log_bamboo_stripped, i.cooked_fish, i.onion, i.moss_paste,
			i.dried_kelp, i.dried_kelp], e, 'miso_with_bamboo_sprouts_cooked_manual_only')

		// Exotic fruits: any Fruits Delight fruit (or melon) in the melon slot
		R.remove({ id: 'quarkdelight:bowl_of_exotic_fruits' }, e)
		R.shapeless(i.bowl_of_exotic_fruits, [i.ancient_fruit, i.t_exotic_fruit, i.chorus_fruit, i.bowl], e, 'bowl_of_exotic_fruits_manual_only')
	})
})
