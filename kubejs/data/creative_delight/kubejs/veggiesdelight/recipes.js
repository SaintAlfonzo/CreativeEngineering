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

		//######################### Bread ####################
		// #forge:bread takes every bread, slice and toast (creativeengineering/bread_tags.js). Garlic bread and the slicing
		// recipe turn a bread into more bread, so they stay on the plain loaves (4 garlic bread from a garlic bread, or 4
		// slices from a slice, would copy items).
		R.replaceInput({ id: 'veggiesdelight:garlic_bread' }, i.t_bread, i.t_bread_loaf, e)
		R.replaceInput({ id: 'veggiesdelight:compat/moredelight/cutting/bread_slice' }, i.t_bread, i.t_bread_loaf, e)

		//######################### Juices #########################
		// Fluids like chorus juice (development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js; feature_writeup/veggies_delight.md 4.9). Pot: Veggies
		// Delight's own inputs, re-id'd manual only. Create mixer, heated: the same inputs + 250 mB water -> 250 mB; spout, drain
		// and IE Bottling Machine at 250 mB. The juice and tea rule (TODO food/drinks #8) may change the ratios.
		;[
			['carrot_juice', [i.minecraft_carrot, i.minecraft_carrot, i.any_sweetener_drink]],
			['dandelion_juice', [i.dandelion_leaf, i.dandelion_leaf, i.sugar_cane]],
			['turnip_water', [i.turnip, i.turnip, i.any_sweetener_drink]],
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

		//######################### Mhadjeb removed #########################
		// Both items are in removed_items.js (TODO dishes/meals #32); their recipes, the Vintage cheese variant included.
		R.remove({ output: i.uncooked_mhadjeb }, e)
		R.remove({ output: i.mhadjeb }, e)

		//######################### Gnocchi #########################
		// Potato noodle is Gnocchi and potato noodles are Creamy Gnocchi (namechanges.js; TODO dishes/meals #33). Gnocchi:
		// the potato pot recipe as before, or 2 from sweet potato dough on the board (there is no potato dough yet).
		// Creamy Gnocchi: milk, 2 gnocchi and onion, garlic or pickled onion; Vintage's cheese variant stays.
		R.fdCutting(i.sweet_potato_dough, i.any_knife, Item.of(i.potato_noodle, 2), e, 'gnocchi_from_sweet_potato_dough')
		R.remove({ id: 'veggiesdelight:cooking/potato_noodles' }, e)
		R.fdCooking([i.milk, i.potato_noodle, i.potato_noodle, [i.onion, i.t_crops_garlic, i.pickled_onion]], i.potato_noodles, e,
			'creamy_gnocchi', 1.0, 200, i.bowl)

		//######################### Zucchini quiche #########################
		// Quiche family rule (food_families.md `quiche/whole`): the shaped recipes on a pie crust stay, plus one assembly on
		// the crust like Brewin's quiche. 3 roasted zucchini, the cooked meat, 250 mB milk.
		global.CD.food(e).assemble(i.pie_crust, [i.roasted_zucchini, i.roasted_zucchini, i.roasted_zucchini,
			[i.cooked_mutton, i.cooked_chicken, i.cooked_beef, i.cooked_pork, i.cooked_bacon], { fill: Fluid.of(f.milk, 250) }],
			i.zucchini_quiche)

		//######################### Sliced sweet potato #########################
		// A sweet potato cuts into 3 (TODO dishes/ingredients #26; the item: kubejs_startup/veggiesdelight/veggies_items.js),
		// as a potato into 3 Rustic Delight potato slices (rusticdelight/recipes.js). Slice & Dice's slicer copies the
		// cutting board recipe.
		R.fdCutting(i.sweet_potato, i.any_knife, Item.of(i.sliced_sweet_potato, 3), e, 'sliced_sweet_potato')
	})
})
