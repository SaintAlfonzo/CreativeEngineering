// requires: barbequesdelight
// Tags of the Barbeque's Delight port (Creative Delight member barbequesdelight, barbeques_delight.md 4 step 2). The port's
// own data has raw_skewers / grilled_skewers and the mineable tags; the eight raw pack skewers join raw_skewers here. 1.3.0's CarryOn blacklist is dropped (no CarryOn in the pack).
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('barbequesdelight')) return
	var t = global.tags
	var i = global.item

	t.add(e, i.t_raw_skewers, [i.raw_cod_skewer, i.raw_salmon_skewer, i.raw_chicken_skewer, i.raw_mushroom_skewer,
		i.raw_beef_skewer, i.raw_lamb_skewer, i.raw_rabbit_skewer, i.raw_pork_sausage_skewer, i.raw_potato_skewer,
		i.raw_vegetable_skewer])
	// The eight raw pack skewers (plan_meals_rolls_bbq.md C3): powders season them and the Grill takes them, as the BBQ
	// raws; each only while its mod is loaded (the member registers it then)
	;[[i.raw_bbq_stick, 'farmersdelight'], [i.raw_end_barbecue_stick, 'ends_delight'],
		[i.raw_improvised_barbecue_stick, 'miners_delight'], [i.raw_nether_skewer, 'nethersdelight'],
		[i.raw_seafood_skewer, 'oceanic_delight'], [i.raw_turnip_mutton_skewer, 'veggiesdelight'],
		[i.raw_crispy_skewer, 'endersdelight'], [i.raw_caveskewer, 'hostiledelight']].forEach(r => { // G5
		if (global.CD.on(r[1])) t.add(e, i.t_raw_skewers, r[0])
	})
	t.add(e, i.t_grilled_skewers, [i.grilled_cod_skewer, i.grilled_salmon_skewer, i.grilled_chicken_skewer,
		i.grilled_mushroom_skewer, i.grilled_beef_skewer, i.grilled_lamb_skewer, i.grilled_rabbit_skewer,
		i.grilled_pork_sausage_skewer, i.grilled_potato_skewer, i.grilled_vegetable_skewer])

	// Skewer inputs (barbeques_delight.md 3.10): each skewering recipe reads these, not the forge: tags directly. Seeded with
	// 1.3.0's inputs (as nested tags, so they follow the forge: tags); add a variant here and every skewer using that
	// input takes it. Whole items only: no slices in potato
	t.add(e, i.t_skewering_beef, i.raw_beef)
	t.add(e, i.t_skewering_chicken, i.raw_chicken)
	t.add(e, i.t_skewering_cod, i.t_raw_cod)
	t.add(e, i.t_skewering_salmon, i.raw_salmon)
	t.add(e, i.t_skewering_mutton, i.raw_mutton)
	t.add(e, i.t_skewering_mushroom, i.brown_mushroom)
	t.add(e, i.t_skewering_pork, i.raw_pork)
	t.add(e, i.t_skewering_potato, [i.potato, i.sweet_potato])
	t.add(e, i.t_skewering_rabbit, i.rabbit)
	t.add(e, i.t_skewering_vegetables, i.vegetables)
	t.add(e, i.t_skewering_cabbage, i.cabbage)
	t.add(e, i.t_skewering_onion, i.onion)
	t.add(e, i.t_skewering_tomato, i.tomato)
	t.add(e, i.t_skewering_bacon, i.t_raw_bacon)
	t.add(e, i.t_skewering_fruit, i.fruit)

	// The oak basin and tray take every log except the 8 woods with a basin and tray of their own (modded logs included),
	// as FD's spruce cutting board takes planks (cutting_boards.js)
	t.addMatching(e, i.t_bbq_oak_logs, i.any_log, { except: [i.t_spruce_logs, i.t_birch_logs, i.t_jungle_logs,
		i.t_acacia_logs, i.t_dark_oak_logs, i.t_mangrove_logs, i.t_crimson_logs, i.t_warped_logs] })
})

ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('barbequesdelight')) return
	var t = global.tags
	var b = global.blocks

	// The grill's built-in campfire goes out in rain by the pack rule (3.4.1): GrillBlockEntity checks this tag, as
	// Creative Compat's CampfireRain does for the plain campfire
	t.add(e, b.t_campfire_rain_douses, b.bbq_grill)

	// BlockRunner walk speed (step 17): the grill is metal, the basin and tray wood
	t.add(e, b.t_blockrunner_metal, b.bbq_grill)
	t.add(e, b.t_blockrunner_wood, [b.bbq_basin, b.bbq_tray, b.bbq_spruce_basin, b.bbq_spruce_tray, b.bbq_birch_basin,
		b.bbq_birch_tray, b.bbq_jungle_basin, b.bbq_jungle_tray, b.bbq_acacia_basin, b.bbq_acacia_tray, b.bbq_dark_oak_basin,
		b.bbq_dark_oak_tray, b.bbq_mangrove_basin, b.bbq_mangrove_tray, b.bbq_crimson_basin, b.bbq_crimson_tray,
		b.bbq_warped_basin, b.bbq_warped_tray])
})
