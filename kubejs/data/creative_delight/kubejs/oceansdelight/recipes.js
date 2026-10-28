// requires: oceansdelight
// Ocean's Delight recipe changes (feature_writeup/ocean_delight.md). Creative Delight member script: recipe ids are
// creative_delight:<type>/<suffix> (global.CD.recipes).
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('oceansdelight')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Bread ####################
		// Caesar salad takes any bread (creativeengineering/bread_tags.js); caviar toast is made on toast, not bread
		R.replaceInput({ id: 'oceanic_delight:caesar_salad' }, i.minecraft_bread, i.t_bread, e)
		R.replaceInput({ id: 'oceanic_delight:caviar_toast' }, i.minecraft_bread, i.toast, e)

		//######################### Braised sea pickle #########################
		// Also from 2 Sea Pickle Slices (Oceanic's knife cut gives 2 a pickle) in place of the sea pickle
		// (dishes/pickles-and-ferments #4). Mushrooms, bowl, time and xp as Ocean's own recipe, which stays.
		if (global.CD.on('oceanic_delight'))
			R.fdCooking([i.sea_pickle_slices, i.sea_pickle_slices, i.brown_mushroom, i.brown_mushroom], i.braised_sea_pickle, e,
				'braised_sea_pickle_from_slices', 0.35, 200, i.bowl)
	})
})
