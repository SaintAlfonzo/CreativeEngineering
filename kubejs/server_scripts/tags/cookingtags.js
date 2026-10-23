ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
	console.log('misctagremoval starting')

	// sugar
	t.add(e, 'forge:sugar', i.glycerol)

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


	// Vintage Delight port (vintage_delight.md 3.1): oat dough is a #forge:dough (owner, 2026-10-23); cucumber dough
	// stays a named ingredient. Cucumber noodles stay in #forge:pasta until the VTD-40 overlap review.
	t.add(e, i.any_dough, i.oat_dough)
	// Crop, seed and crate tags for the Vintage crops, the Veggies port's pattern (vintage_delight.md 4 step 2)
	t.add(e, 'forge:crops/oat', i.oat)
	t.add(e, 'forge:crops/peanut', i.peanut)
	t.add(e, 'forge:crops/cucumber', i.cucumber)
	t.add(e, 'forge:crops/ghost_pepper', i.ghost_pepper)
	t.add(e, 'forge:crops', ['#forge:crops/oat', '#forge:crops/peanut', '#forge:crops/cucumber', '#forge:crops/ghost_pepper'])
	t.add(e, 'forge:seeds/oat', i.oat_seeds)
	t.add(e, 'forge:seeds/cucumber', i.cucumber_seeds)
	t.add(e, 'forge:seeds/ghost_pepper', i.ghost_pepper_seeds)
	t.add(e, 'forge:seeds', ['#forge:seeds/oat', '#forge:seeds/cucumber', '#forge:seeds/ghost_pepper'])
	t.add(e, 'forge:storage_blocks/oat', i.oat_bale)
	t.add(e, 'forge:storage_blocks/peanut', i.peanut_crate)
	t.add(e, 'forge:storage_blocks/cucumber', i.cucumber_crate)
	t.add(e, 'forge:storage_blocks/ghost_pepper', i.ghost_pepper_crate)
	// Fungi Delight port (fungi_delight.md 5.1.6): every mushroom-type item in #forge:mushrooms and its type tag.
	// The port ships inky cap + cleaned cap, morel, truffle + slice; the cooked cap and the colonies are added here.
	// Dishes and crates stay out (a dish must not count as "any mushroom").
	t.add(e, i.t_mushrooms_inky_cap, [i.inky_cap_mushroom, i.cleaned_cap, i.cooked_cleaned_cap, i.inky_cap_mushroom_colony])
	t.add(e, i.t_mushrooms_morel, [i.morel_mushroom, i.morel_mushroom_colony])
	t.add(e, i.t_mushrooms_truffle, [i.truffle, i.truffle_slice])
	t.add(e, i.mushrooms, [i.t_mushrooms_inky_cap, i.t_mushrooms_morel, i.t_mushrooms_truffle])
	t.add(e, 'forge:storage_blocks/brown_mushroom', i.brown_mushroom_crate)
	t.add(e, 'forge:storage_blocks/red_mushroom', i.red_mushroom_crate)
	t.add(e, 'forge:storage_blocks/inky_cap', i.inky_cap_mushroom_crate)
	t.add(e, 'forge:storage_blocks/morel', i.morel_mushroom_crate)
	t.add(e, 'forge:storage_blocks/truffle', i.truffle_crate)

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
	t.add(e, i.t_roasted_bell_pepper, [i.roasted_bell_pepper_red, i.roasted_bell_pepper_green, i.roasted_bell_pepper_yellow])
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
