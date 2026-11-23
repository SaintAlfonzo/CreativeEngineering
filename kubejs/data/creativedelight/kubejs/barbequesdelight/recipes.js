// requires: creativedelight
// requires: barbequesdelight
// Recipes for the Barbeque's Delight port (Creative Delight member barbequesdelight, barbeques_delight.md 4 steps 3, 7).
// The port ships no recipe data: the stations and combos are here, the skewer lines in skewers.js. Combos are plain shaped /
// meal recipes since plan_meals_rolls_bbq.md C8 (no combine recipe). Recipe ids are creativedelight:<type>/<suffix>.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('barbequesdelight')) return

	var i = global.item
	var R = global.NCH.forPack('creativedelight')

	global.CD.recipes(() => {
		// Stations (1.3.0): grill from an iron trapdoor and 7 iron ingots; basin and tray stonecut from logs, one of each
		// per vanilla wood from that wood's logs (oak is the plain bbq_basin / bbq_tray, from every log without a pair of its
		// own: #creativedelight:bbq_oak_logs, tags.js, so modded logs make the oak pair)
		R.shaped(Item.of(i.bbq_grill), ['ICI', 'I I', 'I I'], {C: i.iron_trapdoor, I: i.ingot_iron}, e, 'bbq_grill')
		var woods = [
			[i.bbq_basin, i.bbq_tray, i.t_bbq_oak_logs, 'oak'], [i.bbq_spruce_basin, i.bbq_spruce_tray, i.t_spruce_logs, 'spruce'],
			[i.bbq_birch_basin, i.bbq_birch_tray, i.t_birch_logs, 'birch'], [i.bbq_jungle_basin, i.bbq_jungle_tray, i.t_jungle_logs, 'jungle'],
			[i.bbq_acacia_basin, i.bbq_acacia_tray, i.t_acacia_logs, 'acacia'],
			[i.bbq_dark_oak_basin, i.bbq_dark_oak_tray, i.t_dark_oak_logs, 'dark_oak'],
			[i.bbq_mangrove_basin, i.bbq_mangrove_tray, i.t_mangrove_logs, 'mangrove'],
			[i.bbq_crimson_basin, i.bbq_crimson_tray, i.t_crimson_logs, 'crimson'],
			[i.bbq_warped_basin, i.bbq_warped_tray, i.t_warped_logs, 'warped'],
		]
		woods.forEach(w => {
			R.stonecutting(Item.of(w[0], 2), w[2], e, 'bbq_' + w[3] + '_basin_from_logs')
			R.stonecutting(Item.of(w[1], 4), w[2], e, 'bbq_' + w[3] + '_tray_from_logs')
		})

		// Skewers (basin, grill, furnace, smoker, campfire, heatless craft): skewers.js, on F.skewer (plan_meals_rolls_bbq.md C2)

		// Combos: Kebab Wrap, Kebab Sandwich and Bibimbap are dropped (owner ruling F6 assumption 4, skewers #1;
		// Bibimbap earlier, plan_wood_bucket_batch_1121.md R18): no recipes, hidden (removed_items.js), still registered so
		// saves keep them.
	})
})
