// requires: creativedelight
// requires: barbequesdelight
// Every skewer line on one helper (plan_meals_rolls_bbq.md C2 to C4; skewer family, food_families.md): raw skewer, Grill /
// furnace / smoker / campfire to the cooked one, the heatless cooked-ingredient craft and the assembly. F.skewer is in
// Creative Delight's cd_food.js. The ten Barbeque's Delight lines keep their recipe ids (BBQ-35); the eight pack skewers
// get the raw items the member registers under their own namespaces (PackRawSkewers.java), crafted by hand from raw
// ingredients. Recipe ids are creativedelight:<type>/<suffix>.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('barbequesdelight')) return

	var i = global.item
	var R = global.NCH
	var on = id => global.CD.on(id)

	global.CD.recipes(() => {
		var F = global.CD.food(e)

		//######################### Barbeque's Delight lines #########################
		// Basin: slot x n, stick in the main hand, side x m in the off hand (null = off hand empty), inputs on the
		// creativedelight:skewering/* tags (tags.js, barbeques_delight.md 3.10). Grill ticks for both sides (flip at half,
		// burnt at twice). Cooked-ingredient craft (dishes/meals #46, O29, O38 D1, 4.4): the line's ingredients cooked
		// where they cook plus one extra of the largest-value ingredient (3 A + 2 B, or 5 A on the single-ingredient lines);
		// vegetable line: vegetables taken as the larger (both are tags). Furnace, smoker and campfire (dishes/meals #49,
		// O33) keep no NBT, so seasonings on the raw skewer are lost there; only the Grill carries them.
		var n = (id, count) => ({ id: id, count: count })
		var bbq = [
			[i.raw_beef_skewer, i.grilled_beef_skewer, n(i.t_skewering_beef, 2), n(i.t_skewering_cabbage, 2), 320, [i.cooked_beef, i.cabbage]],
			[i.raw_chicken_skewer, i.grilled_chicken_skewer, n(i.t_skewering_chicken, 2), n(i.t_skewering_onion, 2), 280, [i.cooked_chicken, i.onion]],
			[i.raw_cod_skewer, i.grilled_cod_skewer, n(i.t_skewering_cod, 2), n(i.t_skewering_tomato, 2), 240, [i.cooked_cod, i.tomato]],
			[i.raw_salmon_skewer, i.grilled_salmon_skewer, n(i.t_skewering_salmon, 2), n(i.t_skewering_tomato, 2), 240, [i.cooked_salmon, i.tomato]],
			[i.raw_lamb_skewer, i.grilled_lamb_skewer, n(i.t_skewering_mutton, 4), null, 320, [i.cooked_mutton]],
			[i.raw_mushroom_skewer, i.grilled_mushroom_skewer, n(i.t_skewering_mushroom, 4), null, 240, [i.brown_mushroom]],
			[i.raw_pork_sausage_skewer, i.grilled_pork_sausage_skewer, n(i.t_skewering_pork, 4), null, 360, [i.cooked_pork]],
			[i.raw_potato_skewer, i.grilled_potato_skewer, n(i.t_skewering_potato, 2), n(i.t_skewering_bacon, 2), 280, [i.baked_potato, i.cooked_bacon]],
			[i.raw_rabbit_skewer, i.grilled_rabbit_skewer, n(i.t_skewering_rabbit, 2), n(i.t_skewering_cabbage, 2), 280, [i.rabbit_cooked, i.cabbage]],
			[i.raw_vegetable_skewer, i.grilled_vegetable_skewer, n(i.t_skewering_vegetables, 2), n(i.t_skewering_fruit, 2), 160, [i.vegetables, i.fruit]],
		]
		bbq.forEach(l => F.skewer({ raw: l[0], cooked: l[1], skewering: { main: l[2], side: l[3] }, grill: l[4],
			cookedInputs: l[5], shape: 'bbq' }))

		//######################### Pack skewers #########################
		// Raw craft from the raw ingredients (one of each, a diagonal skewer as the Crispy Skewer; the End Skewer
		// shapeless), cooked by the Grill (ticks of the closest Barbeque's Delight line), furnace, smoker or campfire. The
		// heatless craft from cooked ingredients stays where one exists; the Nether and Cave Skewers have no cooked form
		// of their inputs, so their only route is cooking the raw skewer. Upstream crafts removed.

		// Barbecue Skewer (Farmer's Delight barbecue_stick): raw meat in place of the cooked meat; grill as beef
		if (on('farmersdelight')) {
			R.remove({ id: 'farmersdelight:barbecue_stick' }, e)
			F.skewer({ raw: i.raw_bbq_stick, cooked: i.bbq_stick, grill: 320,
				rawInputs: [i.onion, i.tomato, i.t_food_raw_meat], cookedInputs: [i.onion, i.tomato, i.cooked_meat],
				steps: [i.onion, i.tomato, i.cooked_meat] })
		}
		// End Skewer (End's Delight end_barbecue_stick, owner 2026-11-04): 1 skewer, not 3; Ender's Delight's Shulker
		// Mollusk only (no End's shulker meat, no shulker tag), no endermite meat. The mollusk has no cooked form, so the
		// heatless craft takes it too; the cooking step is the skewer's. Grill as lamb
		if (on('ends_delight') && on('endersdelight')) {
			R.remove({ id: 'ends_delight:food/end_barbecue_stick' }, e)
			F.skewer({ raw: i.raw_end_barbecue_stick, cooked: i.end_barbecue_stick, grill: 320, shape: 'shapeless',
				rawInputs: [i.shulker_mollusk, i.raw_dragon_meat, i.chorus_fruit],
				cookedInputs: [i.shulker_mollusk, i.roasted_dragon_meat, i.chorus_fruit] })
		}
		// Improvised Skewer (Miner's Delight improvised_barbecue_stick): cave carrot, mushroom, bat wing; grill as potato
		if (on('miners_delight')) {
			R.remove({ id: 'miners_delight:improvised_barbecue_stick' }, e)
			F.skewer({ raw: i.raw_improvised_barbecue_stick, cooked: i.improvised_barbecue_stick, grill: 280,
				rawInputs: [i.cave_carrot, i.brown_mushroom, i.bat_wing],
				cookedInputs: [i.baked_cave_carrot, i.brown_mushroom, i.smoked_bat_wing] })
		}
		// Nether Skewer (Nether's Delight): upstream's inputs are raw already (propelpearl, raw strider, blaze rod), so
		// they make the raw skewer; no cooked strider exists, so no heatless craft. Grill as beef
		if (on('nethersdelight')) {
			R.remove({ id: 'nethersdelight:nether_skewer' }, e)
			F.skewer({ raw: i.raw_nether_skewer, cooked: i.nether_skewer, grill: 320, stick: i.blaze_rod,
				rawInputs: [i.propelpearl, i.t_raw_strider] })
		}
		// Seafood Skewer (Oceanic Delight): raw shrimp and calamari; the heatless craft is upstream's (fried shrimp, cooked
		// calamari, sea pickle). Grill as cod
		if (on('oceanic_delight')) {
			R.remove({ id: 'oceanic_delight:seafood_skewer' }, e)
			F.skewer({ raw: i.raw_seafood_skewer, cooked: i.seafood_skewer, grill: 240,
				rawInputs: [i.shrimp, i.t_raw_calamari, i.t_sea_pickles],
				cookedInputs: [i.fried_shrimp, i.t_cooked_calamari, i.t_sea_pickles] })
		}
		// Turnip Mutton Skewer (Veggies Delight): raw mutton and turnip; grill as lamb (creativedelight/members #30 R10)
		if (on('veggiesdelight')) {
			R.remove({ id: 'veggiesdelight:turnip_mutton_skewer' }, e)
			F.skewer({ raw: i.raw_turnip_mutton_skewer, cooked: i.turnip_mutton_skewer, grill: 320,
				rawInputs: [i.raw_mutton, i.turnip], cookedInputs: [i.cooked_mutton, i.turnip],
				steps: [i.turnip, i.cooked_mutton] })
		}
		// Crispy Skewer (Ender's Delight): the popped chorus fruit is the cooked part, so the raw skewer takes two chorus
		// fruit; grill as chicken
		if (on('endersdelight')) {
			R.remove({ id: 'endersdelight:crispy_skewer' }, e)
			F.skewer({ raw: i.raw_crispy_skewer, cooked: i.crispy_skewer, grill: 280,
				rawInputs: [i.mite_crust, i.chorus_fruit, i.chorus_fruit],
				cookedInputs: [i.mite_crust, i.chorus_fruit, i.popped_chorus_fruit],
				steps: [i.chorus_fruit, i.popped_chorus_fruit, i.mite_crust] })
		}
		// Cave Skewer (Hostile Delight caveskewer): glow berries, spider eye, rotten flesh, all raw already; no heatless
		// craft (dishes/meals #58 merges it with Miner's later). Grill as chicken
		if (on('hostiledelight')) {
			R.remove({ id: 'hostiledelight:caveskewerrecp' }, e)
			F.skewer({ raw: i.raw_caveskewer, cooked: i.caveskewer, grill: 280,
				rawInputs: [i.glow_berries, i.spider_eye, i.rotten_flesh] })
		}
	})
})
