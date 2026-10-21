ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
	console.log('misctagremoval starting')

	// sugar
	t.add(e, 'forge:sugar', i.glycerol)

	// flour / dough cleanup
	t.clearAll(e, [i.wheat_flour_pc, i.out_dough])
	t.add(e, 'forge:dusts/flour', i.in_flour)
	// cornmeal: its own flour tag, plus the "flour or cornmeal" tag pancakes, pies and cakes take (TODO food/ingredients #1)
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
})
