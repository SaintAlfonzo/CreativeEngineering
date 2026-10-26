// requires: farmersdelight
// Farmer's Delight 1.3.4 recipe changes on the repacked 1.2.4 (feature_writeup/farmers_delight.md 3, TODO
// largecontent/mod-additions #18). Creative Delight member script: recipe ids are creative_delight:<type>/<suffix>
// (global.CD.recipes). The new blocks and items are in the repack's overlay (members/farmers).
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('farmersdelight')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### New content #########################
		R.shaped(Item.of(i.rope_fence, 3), ['r/r', 'r/r'], { r: i.any_rope, '/': i.stick }, e, 'rope_fence')
		R.shaped(i.rope_fence_gate, ['/r/', '/r/'], { r: i.any_rope, '/': i.stick }, e, 'rope_fence_gate')
		R.shapeless(i.gleaming_salad_block, [i.glow_berries, i.glow_berries, i.honey_bottle, i.tomato, i.golden_carrot,
			i.beetroot, i.farmersdelight_cabbage, i.farmersdelight_cabbage, i.bowl], e, 'gleaming_salad_block')
		// Slices from the placed Farmer's Delight pumpkin pie ([pumpkin_pie] placeAs) go back into a pie
		R.shaped(i.pumpkin_pie, ['##', '##'], { '#': i.fd_pumpkin_pie_slice }, e, 'pumpkin_pie_from_fd_slices')

		//######################### Ingredient changes #########################
		R.replaceInput({ id: 'farmersdelight:cooking/rabbit_stew' }, i.baked_potato, i.potato, e)
		R.replaceInput({ id: 'farmersdelight:cooking/vegetable_noodles' }, i.brown_mushroom, i.mushrooms, e)
		// 1.3.4 names #forge:raw_meat, which in this pack holds only two Miner's Delight items; the pack's raw meat
		// food type is the same idea (beef, pork, chicken, mutton, rabbit and the modded meats)
		R.replaceInput({ id: 'farmersdelight:cooking/dog_food' }, i.t_wolf_prey, i.t_food_raw_meat, e)
		// Cabbage Rolls: the filling tag is widened in farmersdelight/tags.js

		//######################### Salvaging (cutting board, axe) #########################
		// One recipe per wood: every wooden furniture piece gives its plank at 75% (1.2.4: door, sign and trapdoor
		// only, 100%). Vanilla 1.19.2 woods; crimson and warped have no boat.
		var woods = ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'crimson', 'warped']
		woods.forEach(w => {
			['door', 'sign', 'trapdoor'].forEach(p => R.remove({ id: 'farmersdelight:cutting/' + w + '_' + p }, e))
			var parts = ['door_', 'trapdoor_', 'sign_', 'fence_', 'fencegate_', 'pressureplate_', 'button_']
				.map(p => i[p + w]).concat([i[w + '_cabinet']])
			if (i[w + '_boat']) parts.push(i[w + '_boat'])
			R.fdCutting(Ingredient.of(parts), R.AXE, Item.of(i['planks_' + w]).withChance(0.75), e, 'salvaging/' + w + '_furniture')
		})
		woods.filter(w => i[w + '_chest_boat']).forEach(w =>
			R.fdCutting(i[w + '_chest_boat'], R.AXE, [i[w + '_boat'], i.chest], e, 'salvaging/' + w + '_chest_boat'))
		// Minecarts come apart with a hoe, as upstream lists it
		R.fdCutting(i.chest_minecart, i.any_hoe, [i.minecart, i.chest], e, 'salvaging/chest_minecart')
		R.fdCutting(i.furnace_minecart, i.any_hoe, [i.minecart, i.furnace], e, 'salvaging/furnace_minecart')
		R.fdCutting(i.hopper_minecart, i.any_hoe, [i.minecart, i.hopper], e, 'salvaging/hopper_minecart')
		R.fdCutting(i.tnt_minecart, i.any_hoe, [i.minecart, i.tnt], e, 'salvaging/tnt_minecart')
	})
})
