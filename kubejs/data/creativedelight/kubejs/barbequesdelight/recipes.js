// requires: barbequesdelight
// Recipes for the Barbeque's Delight port (Creative Delight member barbequesdelight, barbeques_delight.md 4 steps 3, 7).
// The port ships no recipe data: all 26 of 1.3.0's recipes are here, inputs as the pack's forge: tags. Skewers come from
// the basin and the grill only (O1: no skewer family mirrors, no furnace route); the combos are 1.3.0's (O3; the wrap
// moves to a tortilla in dishes/meals #47, sandwich and bibimbap rework in #48). Recipe ids are creativedelight:<type>/<suffix>.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('barbequesdelight')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		// Stations (1.3.0): grill from an iron trapdoor and 7 iron ingots; basin and tray stonecut from any log
		R.shaped(Item.of(i.bbq_grill), ['ICI', 'I I', 'I I'], {C: i.iron_trapdoor, I: i.ingot_iron}, e, 'bbq_grill')
		R.stonecutting(Item.of(i.bbq_basin, 2), i.any_log, e, 'bbq_basin_from_logs')
		R.stonecutting(Item.of(i.bbq_tray, 4), i.any_log, e, 'bbq_tray_from_logs')

		// Skewering: basin slot x n, stick in the main hand, side x m in the off hand (null = off hand empty)
		R.bbqSkewering(i.raw_beef_skewer, Item.of(i.raw_beef, 2), Item.of(i.cabbage, 2), e, 'raw_beef_skewer')
		R.bbqSkewering(i.raw_chicken_skewer, Item.of(i.raw_chicken, 2), Item.of(i.onion, 2), e, 'raw_chicken_skewer')
		R.bbqSkewering(i.raw_cod_skewer, Item.of(i.t_raw_cod, 2), Item.of(i.tomato, 2), e, 'raw_cod_skewer')
		R.bbqSkewering(i.raw_salmon_skewer, Item.of(i.raw_salmon, 2), Item.of(i.tomato, 2), e, 'raw_salmon_skewer')
		R.bbqSkewering(i.raw_lamb_skewer, Item.of(i.raw_mutton, 4), null, e, 'raw_lamb_skewer')
		R.bbqSkewering(i.raw_mushroom_skewer, Item.of(i.brown_mushroom, 4), null, e, 'raw_mushroom_skewer')
		R.bbqSkewering(i.raw_pork_sausage_skewer, Item.of(i.raw_pork, 4), null, e, 'raw_pork_sausage_skewer')
		R.bbqSkewering(i.raw_potato_skewer, Item.of(i.potato, 2), Item.of(i.t_raw_bacon, 2), e, 'raw_potato_skewer')
		R.bbqSkewering(i.raw_rabbit_skewer, Item.of(i.rabbit, 2), Item.of(i.cabbage, 2), e, 'raw_rabbit_skewer')
		R.bbqSkewering(i.raw_vegetable_skewer, Item.of(i.vegetables, 2), Item.of(i.fruit, 2), e, 'raw_vegetable_skewer')

		// Grilling: ticks for both sides (flip at half; burnt at twice)
		R.bbqGrilling(i.grilled_beef_skewer, i.raw_beef_skewer, 320, e, 'grilled_beef_skewer')
		R.bbqGrilling(i.grilled_chicken_skewer, i.raw_chicken_skewer, 280, e, 'grilled_chicken_skewer')
		R.bbqGrilling(i.grilled_cod_skewer, i.raw_cod_skewer, 240, e, 'grilled_cod_skewer')
		R.bbqGrilling(i.grilled_salmon_skewer, i.raw_salmon_skewer, 240, e, 'grilled_salmon_skewer')
		R.bbqGrilling(i.grilled_lamb_skewer, i.raw_lamb_skewer, 320, e, 'grilled_lamb_skewer')
		R.bbqGrilling(i.grilled_mushroom_skewer, i.raw_mushroom_skewer, 240, e, 'grilled_mushroom_skewer')
		R.bbqGrilling(i.grilled_pork_sausage_skewer, i.raw_pork_sausage_skewer, 360, e, 'grilled_pork_sausage_skewer')
		R.bbqGrilling(i.grilled_potato_skewer, i.raw_potato_skewer, 280, e, 'grilled_potato_skewer')
		R.bbqGrilling(i.grilled_rabbit_skewer, i.raw_rabbit_skewer, 280, e, 'grilled_rabbit_skewer')
		R.bbqGrilling(i.grilled_vegetable_skewer, i.raw_vegetable_skewer, 160, e, 'grilled_vegetable_skewer')

		// Combos (combine: the result keeps the skewers, seasoning included; sticks come back)
		// The wrap is on a tortilla, like every wrap (TODO dishes/meals #47, #15); upstream took bread
		R.bbqCombine(i.kebab_wrap, [i.t_tortilla, i.t_grilled_skewers, i.cabbage], e, 'kebab_wrap')
		R.bbqCombine(i.kebab_sandwich, [i.t_bread, Item.of(i.t_grilled_skewers, 2), i.cabbage, i.onion], e, 'kebab_sandwich')
		R.bbqCombine(i.bibimbap, [i.cooked_rice, i.onion, i.carrot, Item.of(i.t_grilled_skewers, 3)], e, 'bibimbap')
	})
})
