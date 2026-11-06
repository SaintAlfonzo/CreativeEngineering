// requires: oceansdelight
// Ocean's Delight recipe changes (feature_writeup/ocean_delight.md). Creative Delight member script: recipe ids are
// creativedelight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('oceansdelight')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Bread ####################
		// Caviar toast is made on toast, not bread. Oceanic's Caesar salad merged into Veggies Delight's (TODO merges/dishes #6;
		// its recipe: veggiesdelight/cooking.js)
		R.remove({ id: 'oceanic_delight:caesar_salad' }, e)
		R.replaceInput({ id: 'oceanic_delight:caviar_toast' }, i.minecraft_bread, i.toast, e)

		//######################### Braised sea pickle #########################
		// Also from 2 Sea Pickle Slices (Oceanic's knife cut gives 2 a pickle) in place of the sea pickle
		// (dishes/pickles-and-ferments #4). Mushrooms, bowl, time and xp as Ocean's own recipe, which stays.
		if (global.CD.on('oceanic_delight'))
			R.fdCooking([i.sea_pickle_slices, i.sea_pickle_slices, i.brown_mushroom, i.brown_mushroom], i.braised_sea_pickle, e,
				'braised_sea_pickle_from_slices', 0.35, 200, i.bowl)

		//######################### Seagrass salad #########################
		// 2 sea grass in place of 1 (creative_delight.md 34, creativedelight/members #24)
		R.remove({ id: 'oceansdelight:seagrass_salad' }, e)
		R.shapeless(i.seagrass_salad, [i.seagrass, i.seagrass, i.bowl], e, 'seagrass_salad')

		//######################### Tentacles to calamari #########################
		// The tentacle items of Ocean's, Oceanic and Miner's Delight are removed (startup_scripts/_lib/removed_items.js,
		// dishes/ocean #4): their own recipes go and every dish that used one takes calamari. The three mods' cooking
		// and cutting recipes of the tentacles are removed by input and output. The calamari tags hold the plain, glowing
		// and sliced forms (raw: rusticdelight/tags.js; cooked: Rustic's cooked calamari and slice), so a slice works too.
		// Dishes that take a tentacle tag get calamari through the tag (oceansdelight/tags.js).
		var tentacles = [i.oceansdelight_tentacles, i.cut_tentacles, i.tentacle_on_a_stick, i.baked_tentacle_on_a_stick]
		if (global.CD.on('oceanic_delight'))
			tentacles = tentacles.concat([i.squid_tentacles, i.glow_squid_tentacles, i.grilled_squid_tentacles])
		R.replaceInput({ id: 'oceansdelight:cooking/squid_rings' }, i.cut_tentacles, i.t_raw_calamari, e)
		if (global.CD.on('oceanic_delight')) {
			R.replaceInput({ id: 'oceanic_delight:glow_squid_stew' }, i.glow_squid_tentacles, i.t_raw_calamari, e)
			// sea pickle roll, shrimp rice, squid salad and takoyaki take the squid tentacle tag, which is empty once the
			// tentacles are removed; raw calamari joins that tag instead (oceansdelight/tags.js), so they need no swap here.
			// (the Seafood Skewer's recipes are barbequesdelight/skewers.js, on cooked calamari already)
			;['squid_and_pickles'].forEach(r =>
				R.replaceInput({ id: 'oceanic_delight:' + r }, i.grilled_squid_tentacles, i.t_cooked_calamari, e))
		}
		// After the swaps: a removal by input runs at once, so before them it took the dishes too (SQ-CM-32).
		tentacles.forEach(t => {
			R.remove({ output: t }, e)
			R.remove({ input: t }, e)
		})
	})
})
