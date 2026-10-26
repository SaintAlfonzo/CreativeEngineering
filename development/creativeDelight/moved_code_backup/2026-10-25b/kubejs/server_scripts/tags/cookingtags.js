ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
	console.log('misctagremoval starting')

	// sugar
	t.add(e, 'forge:sugar', [i.glycerol, i.powdered_sugar])

	// flour / dough cleanup
	t.clearAll(e, [i.wheat_flour_pc, i.out_dough])
	t.add(e, 'forge:dusts/flour', i.in_flour)
	// cornmeal: its own flour tag, plus the "flour or cornmeal" tag pancakes, pies and cakes take (TODO dishes/ingredients #1)
	t.add(e, i.t_flour_corn, i.cornmeal)
	t.add(e, i.t_flour_or_cornmeal, [i.flour, i.t_flour_corn])

	// bakeware: one tag per kind (what a baking recipe's `tool` names) + the oven's tool-slot filter
	t.add(e, i.t_bakeware_baking_sheet, i.baking_sheet)
	t.add(e, i.t_bakeware_cake_pan, i.cake_pan)
	t.add(e, i.t_bakeware_pie_tin, i.pie_tin)
	t.add(e, i.t_bakeware_muffin_tin, i.muffin_tin)
	t.add(e, i.t_bakeware, [
		i.t_bakeware_baking_sheet, i.t_bakeware_cake_pan, i.t_bakeware_pie_tin, i.t_bakeware_muffin_tin,
	])

	// ice cubes
	t.add(e, 'forge:ice_cubes', i.ice)

	// redundant egg
	t.clearAll(e, i.fried_egg_incubation)

	// cooking oil: Rustic Delight only presses cotton seeds
	t.add(e, i.t_cooking_oil_ingredients, i.pumpkin_seeds)
	// Cooking for Blockheads oven fuel (ovenRequiresCookingOil): Balm's tag lists only an optional item
	// `forge:cookingoil` (no '#', so not the forge tag) and is otherwise empty. TODO cooking/kitchen #8
	t.add(e, i.t_balm_cooking_oil, i.cooking_oil)

	// sliced potatoes: only More Delight's diced potatoes were in it; Rustic Delight's slices fry the same (TODO dishes/meals #7)
	t.add(e, i.t_sliced_potatoes, i.potato_slices)

	// lasagna meats
	t.add(e, 'brewinandchewin:cooked_horror_lasagna_meats', [
		i.beef_cooked, i.mutton_cooked, i.beef_patty, i.cooked_mutton_chops,
	])

	// cooked meat
	t.add(e, 'forge:cooked_meat', [
		i.beef_cooked, i.beef_patty,
		i.porkchop_cooked, i.cooked_bacon,
		i.chicken_cooked, i.chicken_cuts_cooked,
		i.mutton_cooked, i.cooked_mutton_chops,
		i.cod_cooked, i.cod_slice_cooked,
		i.salmon_cooked, i.salmon_slice_cooked,
		i.rabbit_cooked,
		i.moose_ribs_cooked, i.moose_rib_cooked_loose,
		i.kangaroo_meat_cooked, i.kangaroo_shank_cooked,
		i.catfish_cooked_mob, i.catfish_slice_cooked,
		i.bunfungus_cooked, i.bunfungus_drumstick_cooked,
		i.bison_cooked, i.bison_patty,
		i.centipede_leg_cooked,
		i.venison_chops_cooked,
		i.lobster_tail_cooked,
		i.cooked_crab,
	])


	// Vintage and Fungi Delight crop, seed, crate and mushroom tags: Creative Delight members #3 and #4
	// (kubejs/data/creative_delight/kubejs/vintagedelight/tags.js, fungidelight/tags.js)

	// meals (TODO dishes/meals #1, #2)
	// cooked veggies for heatless soups and stews: the cooked/veggie food types (food_types.json) minus snacks,
	// rice and kelp. Listed by key: foodtypetags.js fills those tags after this script runs.
	t.add(e, i.t_soup_veggie_cooked, [
		i.baked_potato, i.baked_potato_slices, i.boiled_corn, i.grilled_corn,
		i.roasted_bell_pepper_green, i.roasted_bell_pepper_red, i.roasted_bell_pepper_yellow,
		i.boiled_blue_pickerelweed, i.boiled_purple_pickerelweed, i.roasted_adzuki_beans,
		i.cactus_steak, i.kimchi, i.pickled_pickles,
		i.baked_sweet_potato, i.roasted_zucchini,
	])
	// any roasted bell pepper: Veggies Delight's smoked pepper is not ported, its recipes take these
	t.add(e, i.t_roasted_bell_pepper, [i.roasted_bell_pepper_red, i.roasted_bell_pepper_green, i.roasted_bell_pepper_yellow]) // the six new colours: creative_delight rusticdelight/tags.js
	// bell peppers top a pizza (Rustic Delight 1.7.0 adds them to Brewin's tag; our Brewin port reads foods/pizza_topping)
	t.add(e, i.t_pizza_topping, i.any_bell_pepper)
	// exotic fruits bowl: Fruits Delight's fruits, its two melons and the vanilla melon slice
	t.add(e, i.t_exotic_fruit, [
		i.pear, i.hawberry, i.lychee, i.mango, i.persimmon, i.peach, i.orange, i.mangosteen,
		i.bayberry, i.kiwi, i.fig, i.blueberry, i.lemon, i.cranberry, i.pineapple, i.durian_flesh,
		i.hamimelon_slice, i.melon,
	])
	t.add(e, i.t_fruit_salad_slice, [
		i.melon, i.cantaloupe_slice, i.hamimelon_slice, i.pineapple_slice, i.lemon_slice, i.orange_slice,
	])
})
