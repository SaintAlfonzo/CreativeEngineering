// requires: veggiesdelight
// Veggies Delight recipe rows (Creative Delight members #2, creative_delight.md 27; moved from the pack's
// server_scripts/cooking/cooking.js and baking.js 2026-10-25, rows unchanged). Recipe ids are creativedelight:<type>/<suffix>
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

		var F = global.CD.food(e)
		var VD_COOKED_MEAT = [i.cooked_beef, i.cooked_chicken, i.cooked_mutton]
		var VD_QUICHE_MEAT = [i.cooked_mutton, i.cooked_chicken, i.cooked_beef, i.cooked_pork, i.cooked_bacon]

		//######################### Zucchini quiche #########################
		// Quiche family rule (food_families.md `quiche/whole`; creativedelight/members #30 R4): one quiche variant beside
		// Brewin's, so one shaped recipe on a pie crust (manual only, as Brewin's) in place of VD's two (milk; Vintage compat:
		// cheese), plus one assembly on the crust. 3 roasted zucchini, the cooked meat, milk or any cheese.
		R.remove({ id: 'veggiesdelight:zucchini_quiche' }, e)
		R.remove({ id: 'veggiesdelight:compat/vintagedelight/zucchini_quiche' }, e)
		R.shaped(i.zucchini_quiche, ['zzz', 'aOm'], { z: i.roasted_zucchini, a: [i.milk, i.any_cheese], O: i.pie_crust, m: VD_QUICHE_MEAT },
			e, 'zucchini_quiche_manual_only')
		F.assemble(i.pie_crust, [i.roasted_zucchini, i.roasted_zucchini, i.roasted_zucchini, VD_QUICHE_MEAT,
			{ fill: Fluid.of(f.milk, 250) }], i.zucchini_quiche)

		//######################### Vegan pizza #########################
		// Pizza family rule (food_families.md `pizza/whole`; owner 2026-11-01, creativedelight/members #20, #30 R5): base
		// row any dough, tomato sauce, any dough; toppings above (VD's own: bell pepper, a vegetable, onion). One shaped
		// recipe (manual only) plus one assembly on the dough.
		R.remove({ id: 'veggiesdelight:vegan_pizza' }, e)
		R.shaped(i.vegan_pizza, ['abc', 'xOx'], { a: i.any_bell_pepper, b: i.vegetables, c: i.onion, x: i.any_dough, O: i.tomato_sauce },
			e, 'vegan_pizza_manual_only')
		F.assemble(i.any_dough, [i.tomato_sauce, i.any_dough, i.any_bell_pepper, i.vegetables, i.onion], i.vegan_pizza)

		//######################### Stuffed zucchini #########################
		// Stuffed family rule (food_families.md `stuffed`; creativedelight/members #30 R6): both VD stuffed zucchinis are one
		// family, crafting (cooking.js, heatless) plus one assembly on the roasted zucchini, as the Stuffed Potato. The boat:
		// meat, then milk (as the stuffed potato). The bowl: 2 more roasted zucchini, meat, tomato sauce (its bowl is the dish's).
		F.assemble(i.roasted_zucchini, [VD_COOKED_MEAT, { fill: Fluid.of(f.milk, 250) }], i.stuffed_zucchini_boat)
		F.assemble(i.roasted_zucchini, [i.roasted_zucchini, i.roasted_zucchini, VD_COOKED_MEAT, i.tomato_sauce], i.stuffed_zucchinis)

		//######################### Vegetarian patty #########################
		// Patty family (creativedelight/members #30 R11): the raw patty cooks by the cooking trio like any cooked cut piece
		// (VD's own furnace, smoker and campfire recipes are replaced by the family's).
		F.veggieCook(i.raw_vegetarian_patty, i.cooked_vegetarian_patty)
		// Burger rule (creativeengineering/recipes.js): shaped, toppings on top, patty in the middle, bread at the bottom,
		// plus one assembly. Started on the patty, as the egg burger: assemblies on bread need a first step of their own.
		R.remove({ id: 'veggiesdelight:vegetarian_burger' }, e)
		F.burgerAssemble(['C T', ' P ', ' B '], { C: i.cabbage, T: i.tomato, P: i.cooked_vegetarian_patty, B: i.t_bread },
			i.vegetarian_burger, [i.cabbage, i.tomato, i.t_bread], i.cooked_vegetarian_patty)
		// VD's shapeless More Delight burgers on its patty go; the patty is in their shaped recipes and assemblies instead
		;['simple_hamburger', 'hamburger_with_egg', 'loaded_hamburger'].forEach(b =>
			R.remove({ id: 'veggiesdelight:compat/moredelight/' + b }, e))

		//######################### Turnip mutton skewer #########################
		// Skewer family rule (food_families.md `skewer`; creativedelight/members #30 R10): shaped, a diagonal skewer as the
		// Crispy Skewer, plus one assembly on the stick.
		R.remove({ id: 'veggiesdelight:turnip_mutton_skewer' }, e)
		F.skewerAssemble(['  M', ' T ', 'S  '], { M: i.cooked_mutton, T: i.turnip, S: i.stick }, i.turnip_mutton_skewer,
			[i.turnip, i.cooked_mutton])

		//######################### Stuffed Bell Peppers removed #########################
		// The feast and its serving are in removedItems (veggies_items.js; TODO merges/dishes #3, M3); Rustic's stay.
		R.remove({ output: i.stuffed_bellpeppers_block }, e)
		R.remove({ output: i.stuffed_bellpepper }, e)

		//######################### Sweet potato dough #########################
		// Dough-for-bread rule (owner 2026-11-01, TODO baking/baked-goods #17, #31): like every plain dough it bakes into
		// bread only in the oven on a baking sheet (#forge:dough, farmersdelight/recipes.js); VD's furnace, smoker and
		// campfire bread go.
		;['smelting', 'smoking', 'campfire_cooking'].forEach(t => R.remove({ id: 'veggiesdelight:bread_from_' + t }, e))

		//######################### Mashed Sweet Potatoes #########################
		// Mashed family (TODO merges/dishes #2, M2): Veggies Delight's is Mashed Sweet Potatoes (client.js) and takes only
		// sweet potatoes or their slices; More Delight's Mashed Potatoes the potatoes (moredelight/recipes.js). One set:
		// Cooking Pot 2 of the type + milk, in a bowl; by hand 3 baked of the type + milk + bowl (cooking.js). VD's pot recipe
		// (sweet potato + potato + milk) and its Vintage cheese version go.
		R.remove({ id: 'veggiesdelight:cooking/mashed_potatoes' }, e)
		R.remove({ id: 'veggiesdelight:compat/vintagedelight/cooking/mashed_potatoes' }, e)
		var sweetMash = [i.sweet_potato, i.sliced_sweet_potato]
		R.fdCooking([sweetMash, sweetMash, i.milk], i.veggiesdelight_mashed_potatoes, e, 'mashed_sweet_potatoes', 1.0, 200, i.bowl)

		//######################### Sliced sweet potato #########################
		// A sweet potato cuts into 3 (TODO dishes/ingredients #26; the item: kubejs_startup/veggiesdelight/veggies_items.js),
		// as a potato into 3 Rustic Delight potato slices (rusticdelight/recipes.js). Slice & Dice's slicer copies the
		// cutting board recipe.
		R.fdCutting(i.sweet_potato, i.any_knife, Item.of(i.sliced_sweet_potato, 3), e, 'sliced_sweet_potato')
	})
})
