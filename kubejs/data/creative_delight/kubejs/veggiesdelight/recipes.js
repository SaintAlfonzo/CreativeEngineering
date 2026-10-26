// requires: veggiesdelight
// Veggies Delight recipe rows (Creative Delight members #2, creative_delight.md 27; moved from the pack's
// server_scripts/cooking/cooking.js and baking.js 2026-10-25, rows unchanged). Recipe ids are creative_delight:<type>/<suffix>
// (global.CD.recipes). Grid-meal moves and heatless routes are QoL-5 (cooking.js beside this file).
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('veggiesdelight')) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Juices #########################
		// Fluids like chorus juice (development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js; feature_writeup/veggies_delight.md 4.9). Pot: Veggies
		// Delight's own inputs, re-id'd manual only. Create mixer, heated: the same inputs + 250 mB water -> 250 mB; spout, drain
		// and IE Bottling Machine at 250 mB. The juice and tea rule (TODO food/drinks #8) may change the ratios.
		;[
			['carrot_juice', [i.minecraft_carrot, i.minecraft_carrot, i.any_sugar]],
			['dandelion_juice', [i.dandelion_leaf, i.dandelion_leaf, i.sugar_cane]],
			['turnip_water', [i.turnip, i.turnip, i.any_sugar]],
		].forEach(j => {
			var item = i[j[0]], fluid = f[j[0]]
			R.remove({ id: 'veggiesdelight:cooking/' + j[0] }, e)
			R.fdCooking(j[1], Item.of(item, 1), e, j[0] + '_manual_only', 1.0, 200, i.glass_bottle)
			R.createMixing(Fluid.of(fluid, 250), j[1].concat([Fluid.of(f.water, 250)]), e, j[0], 'heated')
			R.createFilling(item, [i.glass_bottle, Fluid.of(fluid, 250)], e, j[0])
			R.createEmptying([i.glass_bottle, Fluid.of(fluid, 250)], item, e, j[0])
			R.ieBottling(item, f['t_' + j[0]], i.glass_bottle, e, undefined, 250)
		})

		//######################### Cauliflower bread (oven, baking sheet) #########################
		// Dough baked with the cauliflower, not raw wheat crafted into bread (veggies_delight.md 4.7). Its garlic bread
		// starts from baked bread and keeps its grid recipe. Slot counts and bakeware as the pack's baking.js.
		var n = (id, count) => ({ id: id, count: count })
		var SHEET = { exp: 0.1, time: 200, tool: i.t_bakeware_baking_sheet }
		R.remove({ id: 'veggiesdelight:cauliflower_bread' }, e)
		R.ceBaking([i.any_dough, n(i.cauliflower, 2)], Item.of(i.cauliflower_bread, 1), e, 'cauliflower_bread', SHEET)
	})
})
