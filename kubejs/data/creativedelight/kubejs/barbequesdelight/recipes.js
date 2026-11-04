// requires: barbequesdelight
// Recipes for the Barbeque's Delight port (Creative Delight member barbequesdelight, barbeques_delight.md 4 steps 3, 7).
// The port ships no recipe data: all 26 of 1.3.0's recipes are here, inputs as the pack's forge: tags. Raw skewers come from
// the basin; cooked ones from the grill, the furnace, smoker and campfire (O33) and a shaped cooked-ingredient craft (O38); the combos are 1.3.0's (O3; the wrap
// moves to a tortilla in dishes/meals #47, sandwich and bibimbap rework in #48). Recipe ids are creativedelight:<type>/<suffix>.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('barbequesdelight')) return

	var i = global.item
	var R = global.NCH

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

		// Skewering: basin slot x n, stick in the main hand, side x m in the off hand (null = off hand empty). Inputs are the
		// creativedelight:skewering/* tags (tags.js, barbeques_delight.md 3.10)
		R.bbqSkewering(i.raw_beef_skewer, { id: i.t_skewering_beef, count: 2 }, { id: i.t_skewering_cabbage, count: 2 }, e, 'raw_beef_skewer')
		R.bbqSkewering(i.raw_chicken_skewer, { id: i.t_skewering_chicken, count: 2 }, { id: i.t_skewering_onion, count: 2 }, e, 'raw_chicken_skewer')
		R.bbqSkewering(i.raw_cod_skewer, { id: i.t_skewering_cod, count: 2 }, { id: i.t_skewering_tomato, count: 2 }, e, 'raw_cod_skewer')
		R.bbqSkewering(i.raw_salmon_skewer, { id: i.t_skewering_salmon, count: 2 }, { id: i.t_skewering_tomato, count: 2 }, e, 'raw_salmon_skewer')
		R.bbqSkewering(i.raw_lamb_skewer, { id: i.t_skewering_mutton, count: 4 }, null, e, 'raw_lamb_skewer')
		R.bbqSkewering(i.raw_mushroom_skewer, { id: i.t_skewering_mushroom, count: 4 }, null, e, 'raw_mushroom_skewer')
		R.bbqSkewering(i.raw_pork_sausage_skewer, { id: i.t_skewering_pork, count: 4 }, null, e, 'raw_pork_sausage_skewer')
		R.bbqSkewering(i.raw_potato_skewer, { id: i.t_skewering_potato, count: 2 }, { id: i.t_skewering_bacon, count: 2 }, e, 'raw_potato_skewer')
		R.bbqSkewering(i.raw_rabbit_skewer, { id: i.t_skewering_rabbit, count: 2 }, { id: i.t_skewering_cabbage, count: 2 }, e, 'raw_rabbit_skewer')
		R.bbqSkewering(i.raw_vegetable_skewer, { id: i.t_skewering_vegetables, count: 2 }, { id: i.t_skewering_fruit, count: 2 }, e, 'raw_vegetable_skewer')

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

		// Furnace, smoker and campfire (dishes/meals #49, O33): a raw skewer cooks into the cooked skewer without the
		// Grill's grilled seasoning (no +2 saturation). Vanilla cooking keeps no NBT, so seasonings on the raw skewer are
		// lost on these routes; only the Grill carries them over.
		var cookedLines = [
			[i.grilled_beef_skewer, i.raw_beef_skewer], [i.grilled_chicken_skewer, i.raw_chicken_skewer],
			[i.grilled_cod_skewer, i.raw_cod_skewer], [i.grilled_salmon_skewer, i.raw_salmon_skewer],
			[i.grilled_lamb_skewer, i.raw_lamb_skewer], [i.grilled_mushroom_skewer, i.raw_mushroom_skewer],
			[i.grilled_pork_sausage_skewer, i.raw_pork_sausage_skewer], [i.grilled_potato_skewer, i.raw_potato_skewer],
			[i.grilled_rabbit_skewer, i.raw_rabbit_skewer], [i.grilled_vegetable_skewer, i.raw_vegetable_skewer],
		]
		cookedLines.forEach(l => {
			var name = l[0].substring(l[0].indexOf(':') + 1)
			R.smelting(l[0], l[1], e, name + '_from_smelting', 0.35)
			R.smoking(l[0], l[1], e, name + '_from_smoking', 0.35)
			R.campfireCooking(l[0], l[1], e, name + '_from_campfire', 0.35)
		})

		// Cooked-ingredient craft (dishes/meals #46, O29, O38 D1, 4.4): shaped and manual only, the line's ingredients
		// cooked where they cook, plus one extra of the line's largest-value ingredient (A below; 3 A + 2 B, or 5 A on
		// the three single-ingredient lines). No grilled (O21). The family's sequenced assembly comes with the family
		// recipes (later, not now). Vegetable line: vegetables taken as the larger (both are tags).
		var twoPart = [
			[i.grilled_beef_skewer, i.cooked_beef, i.cabbage], [i.grilled_chicken_skewer, i.cooked_chicken, i.onion],
			[i.grilled_cod_skewer, i.cooked_cod, i.tomato], [i.grilled_salmon_skewer, i.cooked_salmon, i.tomato],
			[i.grilled_potato_skewer, i.baked_potato, i.cooked_bacon], [i.grilled_rabbit_skewer, i.rabbit_cooked, i.cabbage],
			[i.grilled_vegetable_skewer, i.vegetables, i.fruit],
		]
		twoPart.forEach(l => {
			var name = l[0].substring(l[0].indexOf(':') + 1)
			R.shaped(l[0], [' AB', 'ABA', 'S  '], { A: l[1], B: l[2], S: i.stick }, e, name + '_from_cooked_manual_only')
		})
		var onePart = [
			[i.grilled_lamb_skewer, i.cooked_mutton], [i.grilled_mushroom_skewer, i.brown_mushroom],
			[i.grilled_pork_sausage_skewer, i.cooked_pork],
		]
		onePart.forEach(l => {
			var name = l[0].substring(l[0].indexOf(':') + 1)
			R.shaped(l[0], [' AA', 'AAA', 'S  '], { A: l[1], S: i.stick }, e, name + '_from_cooked_manual_only')
		})

		// Combos (combine: the result keeps the skewers, seasoning included; sticks come back)
		// The wrap is on a tortilla, like every wrap (TODO dishes/meals #47, #15); upstream took bread
		R.bbqCombine(i.kebab_wrap, [i.t_tortilla, i.t_grilled_skewers, i.cabbage], e, 'kebab_wrap')
		R.bbqCombine(i.kebab_sandwich, [i.t_bread, { id: i.t_grilled_skewers, count: 2 }, i.cabbage, i.onion], e, 'kebab_sandwich')
		R.bbqCombine(i.bibimbap, [i.cooked_rice, i.onion, i.carrot, { id: i.t_grilled_skewers, count: 3 }], e, 'bibimbap')
	})
})
