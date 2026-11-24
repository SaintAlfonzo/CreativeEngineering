// requires: creativedelight
// requires: salt
// requires: create__the_salt
// Create salting for every saltable food (salt.md 6 S5, TODO food/seasoning #1). Create: The Salt ships one unheated
// mixing (food + salt -> the food {Salted:1b}) for 65 vanilla and Farmer's Delight foods (COVERED below, read from
// the 1.19.2-1.1.0 jar, pinned by sha1 in the member's gradle.properties). Here: every food in #salt:can_be_salted it
// does not cover gets the same mix, and its mixes for foods not in the tag (bone broth, cooked rice, tomato sauce,
// the raw crops) are removed, so Create salts exactly what the grid salts. The plain item input also matches
// already-salted food (the salt is wasted); NBT ingredients are avoided.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH.forPack('creativedelight')
	var i = global.item

	global.CD.recipes(() => {
		var COVERED_PAIRS = [   // [output, Create: The Salt recipe id]
			[i.bacon_and_eggs, 'create__the_salt:baconandeggs'],
			[i.bacon_sandwich, 'create__the_salt:baconsandwich'],
			[i.baked_potato, 'create__the_salt:baked_potato'],
			[i.baked_cod_stew, 'create__the_salt:bakedcodstew'],
			[i.bbq_stick, 'create__the_salt:barbecuestick'],
			[i.beef_patty, 'create__the_salt:beefpatty'],
			[i.beef_stew, 'create__the_salt:beefstew'],
			[i.beetroot_soup, 'create__the_salt:beetroot_soup'],
			[i.bone_broth, 'create__the_salt:bonebroth'],
			[i.minecraft_bread, 'create__the_salt:bread'],
			[i.farmersdelight_cabbage, 'create__the_salt:cabbage'],
			[i.cabbage_leaf, 'create__the_salt:cabbageleaf'],
			[i.cabbage_rolls, 'create__the_salt:cabbagerolls'],
			[i.chicken_sandwich, 'create__the_salt:chickensandwich'],
			[i.chicken_soup, 'create__the_salt:chickensoup'],
			[i.cod_roll, 'create__the_salt:codroll'],
			[i.beef_cooked, 'create__the_salt:cooked_beef_salted'],
			[i.chicken_cooked, 'create__the_salt:cooked_chiken_salted'],
			[i.cod_cooked, 'create__the_salt:cooked_cod'],
			[i.mutton_cooked, 'create__the_salt:cooked_mutton_salted'],
			[i.porkchop_cooked, 'create__the_salt:cooked_porkchop'],
			[i.rabbit_cooked, 'create__the_salt:cooked_rabit_salted'],
			[i.salmon_cooked, 'create__the_salt:cooked_salmon'],
			[i.salmon_slice_cooked, 'create__the_salt:cooked_salmon_slice'],
			[i.cooked_bacon, 'create__the_salt:cookedbacon'],
			[i.chicken_cuts_cooked, 'create__the_salt:cookedchikencuts'],
			[i.cod_slice_cooked, 'create__the_salt:cookedcodslice'],
			[i.cooked_mutton_chops, 'create__the_salt:cookedmuttonchops'],
			[i.cooked_rice, 'create__the_salt:cookedrice'],
			[i.dumplings, 'create__the_salt:dumplings'],
			[i.egg_sandwich, 'create__the_salt:egg_sandwich'],
			[i.fish_stew, 'create__the_salt:fishstew'],
			[i.farmersdelight_fried_egg, 'create__the_salt:fried_egg_salted'],
			[i.fried_rice, 'create__the_salt:friedrice'],
			[i.grilled_salmon, 'create__the_salt:grilledsalmon'],
			[i.hamburger, 'create__the_salt:hamburger'],
			[i.honey_glazed_ham, 'create__the_salt:honeyglazedham'],
			[i.kelp_roll, 'create__the_salt:kelproll'],
			[i.kelp_roll_slice, 'create__the_salt:kelprollslice'],
			[i.mixed_salad, 'create__the_salt:mixed_salad'],
			[i.mushroom_stew, 'create__the_salt:mushroom_stew'],
			[i.mushroom_rice, 'create__the_salt:mushroomrice'],
			[i.mutton_wrap, 'create__the_salt:muttonwrap'],
			[i.nether_salad, 'create__the_salt:nethersalad'],
			[i.noodle_soup, 'create__the_salt:noodlesoup'],
			[i.onion_farmersdelight, 'create__the_salt:onion'],
			[i.pasta_with_meatballs, 'create__the_salt:pastawithmeatballs'],
			[i.pasta_with_mutton, 'create__the_salt:pastawithmuttonchop'],
			[i.pumpkin_soup, 'create__the_salt:pumpkinsoup'],
			[i.rabbit_stew, 'create__the_salt:rabbit_stew'],
			[i.ratatouille, 'create__the_salt:ratatouille'],
			[i.roast_chicken, 'create__the_salt:roastchicken'],
			[i.roasted_mutton_chops, 'create__the_salt:roastedmuttonchops'],
			[i.rotten_flesh, 'create__the_salt:rotten_flesh'],
			[i.salmon_roll, 'create__the_salt:salmonroll'],
			[i.shepherds_pie, 'create__the_salt:shepherdspie'],
			[i.smoked_ham, 'create__the_salt:smoked_ham'],
			[i.squid_ink_pasta, 'create__the_salt:squidinkpasta'],
			[i.steak_and_potatoes, 'create__the_salt:steakandpotatoes'],
			[i.stuffed_potato, 'create__the_salt:stuffedpotato'],
			[i.stuffed_pumpkin, 'create__the_salt:stuffedpumpkin'],
			[i.farmersdelight_tomato, 'create__the_salt:tomato'],
			[i.tomato_sauce, 'create__the_salt:tomato_sauce'],
			[i.vegetable_noodles, 'create__the_salt:vegetablenoodles'],
			[i.vegetable_soup, 'create__the_salt:vegetablesoup'],
		]
		var COVERED = {}   // output id -> recipe id
		COVERED_PAIRS.forEach(p => { COVERED[p[0]] = p[1] })

		var saltable = {}
		var n = 0
		Ingredient.of(i.t_can_be_salted).itemIds.forEach(id => { saltable[String(id)] = true; n++ })
		if (n === 0) {
			console.warn('[salt] #salt:can_be_salted is empty in the recipe event; Create salting skipped')
			return
		}

		var removed = 0
		Object.keys(COVERED).forEach(id => {
			if (!saltable[id]) { R.remove({ id: COVERED[id] }, e); removed++ }
		})

		var added = 0
		Object.keys(saltable).forEach(id => {
			if (COVERED[id]) return
			R.createMixing(Item.of(id, '{Salted:1b}'), [id, i.t_salts], e, 'salting/' + id.replace(':', '_'))
			added++
		})
		console.info('[salt] Create salting: ' + added + ' mixes added, ' + removed + ' Create: The Salt mixes removed')
	})
})
